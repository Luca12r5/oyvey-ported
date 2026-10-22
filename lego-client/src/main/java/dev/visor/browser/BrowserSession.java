package dev.visor.browser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.visor.VisorClient;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

/**
 * The web browser inside the headset. A real Chromium browser (Chrome or Edge) runs with its own
 * profile, out of sight; this class drives it over the DevTools protocol: it streams the page as
 * JPEG frames (decoded off the render thread), forwards pointer and keyboard input, and tracks
 * the address, title, history and loading state for the browser window.
 *
 * <p>A script in every page (also in embedded frames, see {@code browser/helper.js}) emulates
 * fullscreen and reports the page's main video. For VR the stream switches to "capture": the raw
 * video fills a large page, so the frames carry the whole 360° picture.</p>
 *
 * <p>Render-thread API; all browser communication happens on background threads.</p>
 */
public final class BrowserSession {
	public enum Status { IDLE, STARTING, READY, NO_BROWSER, FAILED }

	/**
	 * A decoded page picture (RGBA, top row first). Whoever takes it must {@link #free()} it.
	 *
	 * @param capture the VR capture this frame belongs to, or null for a frame of the page
	 * @param edges   average colours along the left, right, top and bottom edge (ARGB)
	 */
	public record Frame(int width, int height, ByteBuffer pixels, @Nullable VrCapture capture, int[] edges) {
		public boolean vr() {
			return this.capture != null;
		}

		public void free() {
			STBImage.stbi_image_free(this.pixels);
		}
	}

	/**
	 * What the VR view wants in the stream: the page's main video, cut to {@code region} ("full",
	 * "top" or "left" half for 3D videos), at {@code width} x {@code height} pixels. {@code format}
	 * tells the VR view how to wrap the pictures around the player.
	 */
	public record VrCapture(String format, String region, String quality, int width, int height) {
	}

	/** CSS size of the page at 100 % zoom (16:9, matches the browser window's content area). */
	public static final int VIEW_WIDTH = 1024;
	public static final int VIEW_HEIGHT = 576;
	/** Pixels per CSS pixel of the streamed page: sharp text when the window is seen up close. */
	public static final float DEVICE_SCALE = 1.25F;
	private static final long HIDDEN_GRACE_MILLIS = 1500L;
	private static final long POLL_MILLIS = 100L;
	private static final int WINDOW_LEFT = -24000;
	private static final int WINDOW_TOP = -24000;
	private static final int VR_DECODERS = 2;
	private static final BrowserSession INSTANCE = new BrowserSession();
	private static final String HELPER = loadHelper();

	private static final String FOCUS_SCRIPT = """
		(() => {
		  let e = document.activeElement;
		  while (e && e.shadowRoot && e.shadowRoot.activeElement) e = e.shadowRoot.activeElement;
		  if (!e || e === document.body || e === document.documentElement) return false;
		  if (e.isContentEditable) return true;
		  if (e.tagName === 'TEXTAREA') return !e.readOnly && !e.disabled;
		  if (e.tagName === 'INPUT') {
		    const type = (e.getAttribute('type') || 'text').toLowerCase();
		    return !e.readOnly && !e.disabled
		      && !['button', 'checkbox', 'radio', 'submit', 'reset', 'file', 'image', 'range', 'color', 'hidden'].includes(type);
		  }
		  return false;
		})()""";

	private final ExecutorService worker = Executors.newSingleThreadExecutor(daemon("Visor Browser"));
	private final ExecutorService decoder = Executors.newSingleThreadExecutor(daemon("Visor Browser Frames"));
	private final ExecutorService vrDecoder = Executors.newFixedThreadPool(VR_DECODERS, daemon("Visor VR Frames"));

	private volatile Status status = Status.IDLE;
	private volatile String problem = "";
	private volatile String browserName = "the browser";
	private volatile int generation;
	private @Nullable BrowserProcess process;
	private volatile @Nullable CdpClient cdp;
	private volatile @Nullable String session;
	private volatile @Nullable String targetId;
	private volatile boolean hiddenWindow;

	private volatile String url = "";
	private volatile String title = "";
	private volatile boolean loading;
	private volatile boolean canGoBack;
	private volatile boolean canGoForward;
	private volatile @Nullable String pendingUrl;
	private volatile int cssWidth = VIEW_WIDTH;
	private volatile int cssHeight = VIEW_HEIGHT;

	private final AtomicReference<Frame> latest = new AtomicReference<>();
	private final AtomicInteger frames = new AtomicInteger();
	private volatile long lastAck;
	private volatile long frameNanos = 1_000_000_000L / 60L;
	private volatile boolean screencasting;
	private volatile boolean wantScreencast;
	private volatile long lastVisible;
	private long lastScreencastRetry;
	private long lastWindowCheck;

	private volatile boolean editableFocused;
	private final AtomicBoolean focusChanged = new AtomicBoolean();

	private final Object moveLock = new Object();
	private boolean moveInFlight;
	private float @Nullable [] queuedMove;
	private float lastMoveX = Float.NaN;
	private float lastMoveY = Float.NaN;
	private volatile boolean leftDown;
	private long lastPress;
	private float lastPressX;
	private float lastPressY;
	private int clickCount;

	private volatile @Nullable String helperId;
	private volatile float wantedVolume = 1.0F;
	private volatile float appliedVolume = -1.0F;
	private final AtomicBoolean volumeQueued = new AtomicBoolean();
	private final Set<String> popups = ConcurrentHashMap.newKeySet();
	/** Sessions of embedded frames that run in their own process, with the id of their page script. */
	private final Map<String, String> frameSessions = new ConcurrentHashMap<>();

	// The page's fullscreen and main video, polled while the browser is on screen.
	private volatile boolean pageFullscreen;
	private volatile boolean pageCapturing;
	private volatile @Nullable MediaState media;
	private final AtomicBoolean pollInFlight = new AtomicBoolean();
	private long lastPoll;
	private volatile long lastRelift;

	// VR capture: what is wanted, what the stream carries now.
	private volatile @Nullable VrCapture vrWanted;
	private volatile @Nullable VrCapture vrApplied;
	private volatile boolean vrStream;
	private volatile int vrCssWidth;
	private volatile int vrCssHeight;
	private final AtomicBoolean vrQueued = new AtomicBoolean();
	private final AtomicInteger vrDecoding = new AtomicInteger();
	private final AtomicLong frameSequence = new AtomicLong();
	private long publishedSequence;
	private final AtomicInteger vrFrames = new AtomicInteger();

	private BrowserSession() {
	}

	public static BrowserSession get() {
		return INSTANCE;
	}

	// ------------------------------------------------------------------------------------------
	// State for the UI

	public Status status() {
		return this.status;
	}

	public boolean ready() {
		return this.status == Status.READY;
	}

	/** Started or starting: the window shows it as an open app. */
	public boolean running() {
		return this.status == Status.STARTING || this.status == Status.READY;
	}

	public String problem() {
		return this.problem;
	}

	public String browserName() {
		return this.browserName;
	}

	public String url() {
		return this.url;
	}

	public String title() {
		return this.title;
	}

	public boolean loading() {
		return this.loading;
	}

	public boolean canGoBack() {
		return this.canGoBack;
	}

	public boolean canGoForward() {
		return this.canGoForward;
	}

	public int cssWidth() {
		return this.cssWidth;
	}

	public int cssHeight() {
		return this.cssHeight;
	}

	public int frameCount() {
		return this.frames.get();
	}

	/** Frames of the VR capture decoded so far. */
	public int vrFrameCount() {
		return this.vrFrames.get();
	}

	public boolean editableFocused() {
		return this.editableFocused;
	}

	/** True once after the page's keyboard focus was re-checked (see {@link #editableFocused()}). */
	public boolean pollFocusChange() {
		return this.focusChanged.getAndSet(false);
	}

	/** The newest decoded frame, or null when nothing new arrived. The caller frees it. */
	public @Nullable Frame takeFrame() {
		return this.latest.getAndSet(null);
	}

	/** The page shows something in (emulated) fullscreen. */
	public boolean pageFullscreen() {
		return this.ready() && this.pageFullscreen;
	}

	/** The page's main video, or null when there is none. */
	public @Nullable MediaState media() {
		return this.ready() ? this.media : null;
	}

	/** The stream carries the VR capture (not the page) and its first frames are on their way. */
	public boolean vrStreaming() {
		return this.vrStream && this.vrApplied != null && Objects.equals(this.vrApplied, this.vrWanted);
	}

	// ------------------------------------------------------------------------------------------
	// Lifecycle

	/** Starts the browser if needed and (when given) opens {@code url}. */
	public void open(@Nullable String url) {
		if (url != null) {
			this.pendingUrl = url;
		}
		switch (this.status) {
			case READY -> {
				String target = this.pendingUrl;
				this.pendingUrl = null;
				if (target != null) {
					this.navigate(target);
				}
			}
			case STARTING -> {
			}
			default -> this.start();
		}
	}

	private void start() {
		int gen = ++this.generation;
		this.status = Status.STARTING;
		this.problem = "";
		this.resetPageState();
		String language = language();
		this.worker.execute(() -> this.startBlocking(gen, language));
	}

	/** Closes the browser (asynchronously). */
	public void close() {
		if (this.status == Status.IDLE) {
			return;
		}
		this.generation++;
		this.status = Status.IDLE;
		this.pendingUrl = null;
		this.vrWanted = null;
		this.resetPageState();
		this.worker.execute(() -> this.shutdownBlocking(true));
	}

	/** Closes the browser and waits (bounded) until it is gone; used when the game stops. */
	public void shutdown() {
		this.close();
		try {
			this.worker.submit(() -> {
			}).get(5, TimeUnit.SECONDS);
		} catch (Exception ignored) {
		}
	}

	/** Render thread, every frame the browser window or the VR view is drawn: keeps the stream running. */
	public void markVisible() {
		this.lastVisible = System.currentTimeMillis();
		if (!this.wantScreencast) {
			this.wantScreencast = true;
			this.worker.execute(this::updateScreencast);
		}
	}

	/** Client tick: stops the page stream while nobody looks at it (sound keeps playing). */
	public void tick() {
		long now = System.currentTimeMillis();
		if (this.wantScreencast && now - this.lastVisible > HIDDEN_GRACE_MILLIS) {
			this.wantScreencast = false;
			this.worker.execute(this::updateScreencast);
		} else if (this.wantScreencast && !this.screencasting && this.status == Status.READY && now - this.lastScreencastRetry > 500L) {
			this.lastScreencastRetry = now;
			this.worker.execute(this::updateScreencast);
		}
		if (this.hiddenWindow && this.status == Status.READY && now - this.lastWindowCheck > 1000L) {
			this.lastWindowCheck = now;
			this.worker.execute(this::guardWindow);
		}
		if (this.status == Status.READY && (this.wantScreencast || this.pageFullscreen || this.vrWanted != null)) {
			this.poll(now);
		}
	}

	/** Safety net: the hidden window must never end up on a monitor (fullscreen, maximised, moved). */
	private void guardWindow() {
		CdpClient client = this.cdp;
		String target = this.targetId;
		if (client == null || target == null) {
			return;
		}
		try {
			JsonObject window = client.send("Browser.getWindowForTarget", params("targetId", target), null).get(2, TimeUnit.SECONDS);
			JsonObject bounds = window.getAsJsonObject("bounds");
			String state = bounds.has("windowState") ? bounds.get("windowState").getAsString() : "normal";
			int left = bounds.has("left") ? bounds.get("left").getAsInt() : WINDOW_LEFT;
			int top = bounds.has("top") ? bounds.get("top").getAsInt() : WINDOW_TOP;
			if (!"normal".equals(state)) {
				JsonObject normal = params("windowState", "normal");
				client.send("Browser.setWindowBounds", params("windowId", window.get("windowId").getAsInt(), "bounds", normal), null).get(2, TimeUnit.SECONDS);
				this.placeWindow(client, target);
				VisorClient.LOGGER.info("Put the headset browser window back off screen (it was {})", state);
			} else if (left > WINDOW_LEFT / 2 || top > WINDOW_TOP / 2) {
				this.placeWindow(client, target);
			}
		} catch (Exception ignored) {
		}
	}

	private void startBlocking(int gen, String language) {
		try {
			BrowserLocator.Found found = BrowserLocator.find(BrowserShortcuts.executableOverride());
			if (found == null) {
				if (gen == this.generation) {
					this.status = Status.NO_BROWSER;
				}
				return;
			}
			this.browserName = found.name();
			boolean hidden = BrowserLocator.isWindows();
			this.hiddenWindow = hidden;
			Path profile = FabricLoader.getInstance().getGameDir().resolve("visor").resolve("browser");
			BrowserProcess launched = BrowserProcess.launch(found.executable(), profile, hidden, language);
			this.process = launched;
			if (gen != this.generation) {
				this.shutdownBlocking(false);
				return;
			}
			CdpClient client = CdpClient.connect(launched.devtools, event -> this.onEvent(gen, event), () -> this.onDisconnected(gen));
			this.cdp = client;
			JsonObject version = client.send("Browser.getVersion", null, null).get(10, TimeUnit.SECONDS);
			client.send("Target.setDiscoverTargets", params("discover", true), null).get(10, TimeUnit.SECONDS);
			client.send("Browser.setDownloadBehavior", params("behavior", "deny"), null).exceptionally(error -> null);
			String target = this.createTarget(client, hidden);
			this.targetId = target;
			if (hidden) {
				this.hideWindowSoon(launched.pid());
			}
			JsonObject attached = client.send("Target.attachToTarget", params("targetId", target, "flatten", true), null).get(10, TimeUnit.SECONDS);
			String sid = attached.get("sessionId").getAsString();
			this.session = sid;
			this.setUpPage(client, sid, version, hidden);
			if (gen != this.generation) {
				this.shutdownBlocking(false);
				return;
			}
			this.status = Status.READY;
			VisorClient.LOGGER.info("Headset browser ready ({}, {})", found.name(), version.has("product") ? version.get("product").getAsString() : "?");
			String first = this.pendingUrl;
			this.pendingUrl = null;
			if (first != null) {
				client.send("Page.navigate", params("url", first), sid);
			}
			this.updateScreencast();
		} catch (Exception e) {
			VisorClient.LOGGER.warn("The headset browser could not start", e);
			if (gen == this.generation) {
				this.problem = describe(e);
				this.status = Status.FAILED;
			}
			this.shutdownBlocking(false);
		}
	}

	private String createTarget(CdpClient client, boolean hidden) throws Exception {
		JsonObject plain = params("url", "about:blank");
		if (!hidden) {
			return client.send("Target.createTarget", plain, null).get(10, TimeUnit.SECONDS).get("targetId").getAsString();
		}
		// A normal window, opened without focus and far off screen, so it never shows up or
		// steals the keyboard from the game.
		plain.addProperty("newWindow", true);
		plain.addProperty("background", true);
		JsonObject placed = plain.deepCopy();
		placed.addProperty("left", WINDOW_LEFT);
		placed.addProperty("top", WINDOW_TOP);
		placed.addProperty("width", this.windowWidth());
		placed.addProperty("height", this.windowHeight());
		String id;
		try {
			id = client.send("Target.createTarget", placed, null).get(10, TimeUnit.SECONDS).get("targetId").getAsString();
		} catch (ExecutionException e) {
			// Older browsers do not know the window bounds; place the window afterwards.
			id = client.send("Target.createTarget", plain, null).get(10, TimeUnit.SECONDS).get("targetId").getAsString();
		}
		this.placeWindow(client, id);
		return id;
	}

	private int windowWidth() {
		return this.cssWidth + 64;
	}

	private int windowHeight() {
		return this.cssHeight + 220;
	}

	private void placeWindow(CdpClient client, String target) {
		try {
			JsonObject window = client.send("Browser.getWindowForTarget", params("targetId", target), null).get(5, TimeUnit.SECONDS);
			JsonObject bounds = params("left", WINDOW_LEFT, "top", WINDOW_TOP, "width", this.windowWidth(), "height", this.windowHeight());
			client.send("Browser.setWindowBounds", params("windowId", window.get("windowId").getAsInt(), "bounds", bounds), null).get(5, TimeUnit.SECONDS);
		} catch (Exception e) {
			VisorClient.LOGGER.debug("Could not place the headset browser window", e);
		}
	}

	private void hideWindowSoon(long pid) {
		WindowHider.hide(pid);
		for (long delay : new long[]{300L, 1200L, 3000L}) {
			CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS).execute(() -> WindowHider.hide(pid));
		}
	}

	private void setUpPage(CdpClient client, String sid, JsonObject version, boolean hidden) throws Exception {
		client.send("Page.enable", null, sid).get(10, TimeUnit.SECONDS);
		client.send("Page.setInterceptFileChooserDialog", params("enabled", true), sid).exceptionally(error -> null);
		client.send("Emulation.setFocusEmulationEnabled", params("enabled", true), sid).exceptionally(error -> null);
		this.applyViewport(client, sid);
		if (!hidden && version.has("userAgent")) {
			String agent = version.get("userAgent").getAsString().replace("HeadlessChrome", "Chrome");
			client.send("Emulation.setUserAgentOverride", params("userAgent", agent), sid).exceptionally(error -> null);
		}
		this.installHelper(client, sid, this.wantedVolume);
		// Page loads go through BrowserPolicy (https only, safe search, YouTube restricted mode).
		client.send("Fetch.enable", params("patterns", documentPatterns()), sid).get(5, TimeUnit.SECONDS);
		// Embedded frames from other sites run in their own process: they get the page script too.
		client.send("Target.setAutoAttach", autoAttach(), sid).exceptionally(error -> null);
	}

	private static JsonArray documentPatterns() {
		JsonArray patterns = new JsonArray();
		patterns.add(params("urlPattern", "*", "resourceType", "Document", "requestStage", "Request"));
		return patterns;
	}

	private static JsonObject autoAttach() {
		JsonArray filter = new JsonArray();
		filter.add(params("type", "iframe"));
		return params("autoAttach", true, "waitForDebuggerOnStart", true, "flatten", true, "filter", filter);
	}

	private void applyViewport(CdpClient client, String sid) throws Exception {
		JsonObject metrics = params("width", this.cssWidth, "height", this.cssHeight, "deviceScaleFactor", DEVICE_SCALE, "mobile", false);
		client.send("Emulation.setDeviceMetricsOverride", metrics, sid).get(5, TimeUnit.SECONDS);
	}

	private void installHelper(CdpClient client, String sid, float volume) throws Exception {
		String source = helperScript(volume);
		String previous = this.helperId;
		JsonObject added = client.send("Page.addScriptToEvaluateOnNewDocument", params("source", source), sid).get(5, TimeUnit.SECONDS);
		this.helperId = added.get("identifier").getAsString();
		if (previous != null) {
			client.send("Page.removeScriptToEvaluateOnNewDocument", params("identifier", previous), sid);
		}
		this.appliedVolume = volume;
		client.send("Runtime.evaluate", params("expression", source), sid);
		for (Map.Entry<String, String> frame : this.frameSessions.entrySet()) {
			this.installFrameHelper(client, frame.getKey(), source, frame.getValue());
			client.send("Runtime.evaluate", params("expression", source), frame.getKey());
		}
	}

	private CompletableFuture<Void> installFrameHelper(CdpClient client, String frame, String source, @Nullable String previous) {
		return client.send("Page.addScriptToEvaluateOnNewDocument", params("source", source), frame).thenAccept(added -> {
			this.frameSessions.put(frame, added.get("identifier").getAsString());
			if (previous != null && !previous.isEmpty()) {
				client.send("Page.removeScriptToEvaluateOnNewDocument", params("identifier", previous), frame);
			}
		});
	}

	/** An embedded frame in its own process: page script, nested frames, then let it run. */
	private void onFrameAttached(JsonObject p) {
		CdpClient client = this.cdp;
		String frame = string(p, "sessionId");
		JsonObject info = p.has("targetInfo") ? p.getAsJsonObject("targetInfo") : new JsonObject();
		if (client == null || frame.isEmpty()) {
			return;
		}
		CompletableFuture<?> setup;
		if ("iframe".equals(string(info, "type"))) {
			this.frameSessions.put(frame, "");
			setup = client.send("Page.enable", null, frame)
				.thenCompose(ignored -> client.send("Page.setInterceptFileChooserDialog", params("enabled", true), frame).exceptionally(error -> null))
				.thenCompose(ignored -> client.send("Fetch.enable", params("patterns", documentPatterns()), frame).exceptionally(error -> null))
				.thenCompose(ignored -> this.installFrameHelper(client, frame, helperScript(this.wantedVolume), null))
				.thenCompose(ignored -> client.send("Target.setAutoAttach", autoAttach(), frame));
		} else {
			setup = CompletableFuture.completedFuture(null);
		}
		// Whatever happened, the frame must not stay paused.
		setup.whenComplete((result, error) -> client.send("Runtime.runIfWaitingForDebugger", null, frame));
	}

	private void updateScreencast() {
		CdpClient client = this.cdp;
		String sid = this.session;
		if (client == null || sid == null || this.status != Status.READY) {
			return;
		}
		if (this.wantScreencast && !this.screencasting) {
			this.screencasting = true;
			client.send("Page.startScreencast", this.screencastOptions(), sid).exceptionally(error -> {
				this.screencasting = false;
				return null;
			});
		} else if (!this.wantScreencast && this.screencasting) {
			this.screencasting = false;
			client.send("Page.stopScreencast", null, sid);
		}
	}

	private JsonObject screencastOptions() {
		VrCapture vr = this.vrStream ? this.vrApplied : null;
		if (vr != null) {
			return params("format", "jpeg", "quality", 85, "everyNthFrame", 1, "maxWidth", vr.width(), "maxHeight", vr.height());
		}
		return params(
			"format", "jpeg", "quality", 82, "everyNthFrame", 1,
			"maxWidth", Math.round(this.cssWidth * DEVICE_SCALE), "maxHeight", Math.round(this.cssHeight * DEVICE_SCALE)
		);
	}

	private void shutdownBlocking(boolean graceful) {
		CdpClient client = this.cdp;
		this.cdp = null;
		this.session = null;
		this.targetId = null;
		this.screencasting = false;
		this.helperId = null;
		this.appliedVolume = -1.0F;
		this.frameSessions.clear();
		this.vrApplied = null;
		this.vrStream = false;
		if (client != null) {
			if (graceful && client.isOpen()) {
				try {
					client.send("Browser.close", null, null).get(2, TimeUnit.SECONDS);
				} catch (Exception ignored) {
				}
			}
			client.close();
		}
		BrowserProcess running = this.process;
		this.process = null;
		if (running != null) {
			running.stop(graceful ? 3000L : 300L);
		}
		Frame frame = this.latest.getAndSet(null);
		if (frame != null) {
			frame.free();
		}
	}

	private void onDisconnected(int gen) {
		if (gen != this.generation || (this.status != Status.READY && this.status != Status.STARTING)) {
			return;
		}
		this.problem = "The browser was closed.";
		this.status = Status.FAILED;
		this.worker.execute(() -> this.shutdownBlocking(false));
	}

	private void resetPageState() {
		this.url = "";
		this.title = "";
		this.loading = false;
		this.canGoBack = false;
		this.canGoForward = false;
		this.editableFocused = false;
		this.leftDown = false;
		this.popups.clear();
		this.pageFullscreen = false;
		this.pageCapturing = false;
		this.media = null;
		synchronized (this.moveLock) {
			this.moveInFlight = false;
			this.queuedMove = null;
		}
		this.lastMoveX = Float.NaN;
		this.lastMoveY = Float.NaN;
	}

	// ------------------------------------------------------------------------------------------
	// Events (socket thread)

	private void onEvent(int gen, CdpClient.Event event) {
		if (gen != this.generation) {
			return;
		}
		JsonObject p = event.params();
		String from = event.sessionId();
		boolean page = from != null && from.equals(this.session);
		switch (event.method()) {
			case "Page.screencastFrame" -> {
				if (page) {
					this.onFrame(p);
				}
			}
			case "Page.frameNavigated" -> {
				JsonObject frame = p.has("frame") ? p.getAsJsonObject("frame") : null;
				if (page && frame != null && !frame.has("parentId")) {
					this.url = string(frame, "url") + string(frame, "urlFragment");
					this.editableFocused = false;
					this.focusChanged.set(true);
					// A new document starts without fullscreen; a VR capture has to be set up again.
					this.pageFullscreen = false;
					this.pageCapturing = false;
					this.refreshHistory();
				}
			}
			case "Page.navigatedWithinDocument" -> {
				if (page && Objects.equals(string(p, "frameId"), this.targetId)) {
					this.url = string(p, "url");
					this.refreshHistory();
				}
			}
			case "Page.frameStartedLoading" -> {
				if (page && Objects.equals(string(p, "frameId"), this.targetId)) {
					this.loading = true;
				}
			}
			case "Page.frameStoppedLoading" -> {
				if (page && Objects.equals(string(p, "frameId"), this.targetId)) {
					this.loading = false;
				}
			}
			case "Page.javascriptDialogOpening" -> {
				CdpClient client = this.cdp;
				if (client != null && from != null) {
					client.send("Page.handleJavaScriptDialog", params("accept", true), from);
				}
			}
			case "Fetch.requestPaused" -> this.onRequestPaused(from, p);
			case "Target.attachedToTarget" -> this.onFrameAttached(p);
			case "Target.detachedFromTarget" -> this.frameSessions.remove(string(p, "sessionId"));
			case "Target.targetCreated" -> this.onTarget(p.getAsJsonObject("targetInfo"), true);
			case "Target.targetInfoChanged" -> this.onTarget(p.getAsJsonObject("targetInfo"), false);
			case "Target.targetDestroyed", "Target.targetCrashed" -> {
				String id = string(p, "targetId");
				this.popups.remove(id);
				if (id.equals(this.targetId) && this.status == Status.READY) {
					this.problem = "The page was closed.";
					this.status = Status.FAILED;
					this.worker.execute(() -> this.shutdownBlocking(true));
				}
			}
			default -> {
			}
		}
	}

	/** A document request held by Fetch: allow, rewrite (safe search), upgrade to https or block it. */
	private void onRequestPaused(@Nullable String sid, JsonObject p) {
		CdpClient client = this.cdp;
		if (client == null || sid == null) {
			return;
		}
		String requestId = string(p, "requestId");
		JsonObject request = p.has("request") ? p.getAsJsonObject("request") : new JsonObject();
		String url = string(request, "url") + string(request, "urlFragment");
		BrowserPolicy.Decision d = BrowserPolicy.check(url);
		switch (d.kind()) {
			case ALLOW, REWRITE -> {
				JsonObject cont = params("requestId", requestId);
				if (d.kind() == BrowserPolicy.Kind.REWRITE) {
					cont.addProperty("url", d.url());
				}
				if (BrowserPolicy.youtube(d.url())) {
					JsonArray headers = new JsonArray();
					if (request.has("headers") && request.get("headers").isJsonObject()) {
						for (var e : request.getAsJsonObject("headers").entrySet()) {
							headers.add(params("name", e.getKey(), "value", e.getValue().getAsString()));
						}
					}
					headers.add(params("name", "YouTube-Restrict", "value", "Moderate"));
					cont.add("headers", headers);
				}
				client.send("Fetch.continueRequest", cont, sid).exceptionally(error -> null);
			}
			case REDIRECT -> {
				client.send("Fetch.failRequest", params("requestId", requestId, "errorReason", "BlockedByClient"), sid).exceptionally(error -> null);
				if (sid.equals(this.session)) {
					client.send("Page.navigate", params("url", d.url()), sid).exceptionally(error -> null);
				}
			}
			case BLOCK -> client.send("Fetch.failRequest", params("requestId", requestId, "errorReason", "BlockedByClient"), sid).exceptionally(error -> null);
		}
	}

	private void onTarget(@Nullable JsonObject info, boolean created) {
		if (info == null) {
			return;
		}
		String id = string(info, "targetId");
		if (id.equals(this.targetId)) {
			this.title = string(info, "title");
			return;
		}
		if (!"page".equals(string(info, "type"))) {
			return;
		}
		boolean popup = this.popups.contains(id) || (created && Objects.equals(string(info, "openerId"), this.targetId));
		if (!popup) {
			return;
		}
		// The headset shows one page: a popup's address is opened in the main page instead.
		this.popups.add(id);
		String popupUrl = string(info, "url");
		if (!popupUrl.isEmpty() && !"about:blank".equals(popupUrl)) {
			this.popups.remove(id);
			this.send("Page.navigate", params("url", popupUrl));
			this.closeTarget(id);
		} else if (created) {
			CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute(() -> {
				if (this.popups.remove(id)) {
					this.closeTarget(id);
				}
			});
		}
	}

	private void closeTarget(String id) {
		CdpClient client = this.cdp;
		String ours = this.targetId;
		if (client != null) {
			client.send("Target.closeTarget", params("targetId", id), null);
			if (ours != null) {
				client.send("Target.activateTarget", params("targetId", ours), null);
			}
		}
	}

	private void refreshHistory() {
		this.send("Page.getNavigationHistory", null).thenAccept(history -> {
			JsonArray entries = history.has("entries") ? history.getAsJsonArray("entries") : new JsonArray();
			int index = history.has("currentIndex") ? history.get("currentIndex").getAsInt() : 0;
			this.canGoBack = index > 0;
			this.canGoForward = index < entries.size() - 1;
		});
	}

	private void onFrame(JsonObject p) {
		JsonElement data = p.get("data");
		JsonElement ack = p.get("sessionId");
		if (data == null || ack == null) {
			return;
		}
		String base64 = data.getAsString();
		int ackId = ack.getAsInt();
		if (this.vrStream) {
			this.onVrFrame(p, base64, ackId);
			return;
		}
		this.decoder.execute(() -> {
			Frame frame = decode(base64, null);
			if (frame != null) {
				this.publish(frame, this.frameSequence.incrementAndGet());
				this.frames.incrementAndGet();
			}
			// The browser waits for the acknowledgement before sending the next frame: this caps
			// the stream at the chosen frame rate.
			long wait = this.frameNanos - (System.nanoTime() - this.lastAck);
			if (wait > 0L) {
				LockSupport.parkNanos(wait);
			}
			this.lastAck = System.nanoTime();
			this.send("Page.screencastFrameAck", params("sessionId", ackId));
		});
	}

	/**
	 * VR frames are large: they are acknowledged at once and decoded on several threads, and the
	 * newest finished one wins. Frames still showing the page from before the switch are skipped.
	 */
	private void onVrFrame(JsonObject p, String base64, int ackId) {
		this.send("Page.screencastFrameAck", params("sessionId", ackId));
		JsonObject meta = p.has("metadata") ? p.getAsJsonObject("metadata") : new JsonObject();
		double width = meta.has("deviceWidth") ? meta.get("deviceWidth").getAsDouble() : this.vrCssWidth;
		double height = meta.has("deviceHeight") ? meta.get("deviceHeight").getAsDouble() : this.vrCssHeight;
		if (Math.abs(width - this.vrCssWidth) > 2.0 || Math.abs(height - this.vrCssHeight) > 2.0) {
			return;
		}
		VrCapture capture = this.vrApplied;
		if (capture == null || this.vrDecoding.get() >= VR_DECODERS) {
			return;
		}
		long sequence = this.frameSequence.incrementAndGet();
		this.vrDecoding.incrementAndGet();
		this.vrDecoder.execute(() -> {
			try {
				Frame frame = decode(base64, capture);
				if (frame != null && this.vrStream) {
					this.publish(frame, sequence);
					this.vrFrames.incrementAndGet();
				} else if (frame != null) {
					frame.free();
				}
			} finally {
				this.vrDecoding.decrementAndGet();
			}
		});
	}

	private void publish(Frame frame, long sequence) {
		Frame previous;
		synchronized (this.latest) {
			if (sequence < this.publishedSequence) {
				frame.free();
				return;
			}
			this.publishedSequence = sequence;
			previous = this.latest.getAndSet(frame);
		}
		if (previous != null) {
			previous.free();
		}
	}

	private static @Nullable Frame decode(String base64, @Nullable VrCapture capture) {
		byte[] jpeg;
		try {
			jpeg = Base64.getDecoder().decode(base64);
		} catch (IllegalArgumentException e) {
			return null;
		}
		ByteBuffer input = MemoryUtil.memAlloc(jpeg.length);
		try (MemoryStack stack = MemoryStack.stackPush()) {
			input.put(jpeg).flip();
			IntBuffer width = stack.mallocInt(1);
			IntBuffer height = stack.mallocInt(1);
			IntBuffer channels = stack.mallocInt(1);
			ByteBuffer pixels = STBImage.stbi_load_from_memory(input, width, height, channels, 4);
			if (pixels == null) {
				return null;
			}
			int w = width.get(0);
			int h = height.get(0);
			return new Frame(w, h, pixels, capture, edgeColors(pixels, w, h));
		} finally {
			MemoryUtil.memFree(input);
		}
	}

	/** Average colours of thin bands along the four edges, for the glow around a video. */
	private static int[] edgeColors(ByteBuffer pixels, int width, int height) {
		int[] colors = new int[4];
		int steps = 24;
		for (int side = 0; side < 4; side++) {
			long r = 0;
			long g = 0;
			long b = 0;
			for (int i = 0; i < steps; i++) {
				float along = (i + 0.5F) / steps;
				for (int depth = 0; depth < 3; depth++) {
					float into = 0.01F + depth * 0.025F;
					int x = switch (side) {
						case 0 -> Math.round(into * (width - 1));
						case 1 -> Math.round((1.0F - into) * (width - 1));
						default -> Math.round(along * (width - 1));
					};
					int y = switch (side) {
						case 2 -> Math.round(into * (height - 1));
						case 3 -> Math.round((1.0F - into) * (height - 1));
						default -> Math.round(along * (height - 1));
					};
					int index = (y * width + x) * 4;
					r += pixels.get(index) & 0xFF;
					g += pixels.get(index + 1) & 0xFF;
					b += pixels.get(index + 2) & 0xFF;
				}
			}
			int n = steps * 3;
			colors[side] = 0xFF000000 | (int) (r / n) << 16 | (int) (g / n) << 8 | (int) (b / n);
		}
		return colors;
	}

	// ------------------------------------------------------------------------------------------
	// The page's fullscreen and video

	private void poll(long now) {
		if (now - this.lastPoll < POLL_MILLIS || !this.pollInFlight.compareAndSet(false, true)) {
			return;
		}
		this.lastPoll = now;
		this.evaluate("window.__visor && window.__visor.state ? window.__visor.state() : ''").whenComplete((text, error) -> {
			this.pollInFlight.set(false);
			if (error != null || text == null || text.isEmpty()) {
				return;
			}
			try {
				JsonObject state = JsonParser.parseString(text).getAsJsonObject();
				this.pageFullscreen = state.has("fs") && state.get("fs").getAsInt() != 0;
				this.pageCapturing = state.has("vr") && state.get("vr").getAsInt() != 0;
				this.media = MediaState.parse(state.get("m"));
			} catch (RuntimeException e) {
				VisorClient.LOGGER.debug("Bad page state {}", text, e);
				return;
			}
			// The page lost the capture (a new document): lift the video again.
			if (this.vrStream && !this.pageCapturing && this.media != null && System.currentTimeMillis() - this.lastRelift > 1000L) {
				this.lastRelift = System.currentTimeMillis();
				VrCapture applied = this.vrApplied;
				if (applied != null) {
					this.evaluate(captureScript(applied));
				}
			}
		});
	}

	/** Ends the page's fullscreen (Escape in the headset). */
	public void exitFullscreen() {
		this.pageFullscreen = false;
		this.evaluate("window.__visor && window.__visor.exitFullscreen()");
	}

	/**
	 * A command for the page's main video: "toggle" (play/pause), "seek" (by {@code a} seconds),
	 * "seekTo" (fraction {@code a} of the video), "mute", "skip" (a skippable ad), "fullscreen",
	 * or "look" (yaw {@code a} and pitch {@code b} for spherical sound).
	 */
	public void mediaCommand(String name, double a, double b) {
		this.evaluate(String.format(Locale.ROOT, "window.__visor && window.__visor.command('%s', %.4f, %.4f)", name, a, b));
	}

	/** Switches the stream to the VR capture (or back to the page with null). */
	public void setVrCapture(@Nullable VrCapture capture) {
		if (Objects.equals(capture, this.vrWanted)) {
			return;
		}
		this.vrWanted = capture;
		if (this.vrQueued.compareAndSet(false, true)) {
			this.worker.execute(this::applyVr);
		}
	}

	private void applyVr() {
		this.vrQueued.set(false);
		CdpClient client = this.cdp;
		String sid = this.session;
		VrCapture wanted = this.vrWanted;
		if (client == null || sid == null || this.status != Status.READY || Objects.equals(wanted, this.vrApplied)) {
			return;
		}
		try {
			// Stop the stream first: no frame of the old layout may arrive after the switch.
			if (this.screencasting) {
				this.screencasting = false;
				client.send("Page.stopScreencast", null, sid).get(2, TimeUnit.SECONDS);
			}
			this.vrStream = false;
			Frame stale = this.latest.getAndSet(null);
			if (stale != null) {
				stale.free();
			}
			if (wanted != null) {
				int width = Math.round(wanted.width() / DEVICE_SCALE);
				int height = Math.round(wanted.height() / DEVICE_SCALE);
				this.evaluateNow(client, sid, captureScript(wanted));
				JsonObject metrics = params("width", width, "height", height, "deviceScaleFactor", DEVICE_SCALE, "mobile", false);
				client.send("Emulation.setDeviceMetricsOverride", metrics, sid).get(5, TimeUnit.SECONDS);
				this.vrCssWidth = width;
				this.vrCssHeight = height;
				this.vrApplied = wanted;
				this.vrStream = true;
				VisorClient.LOGGER.info("VR capture on: {} {}x{}", wanted.region(), wanted.width(), wanted.height());
			} else {
				this.evaluateNow(client, sid, "window.__visor && window.__visor.stopCapture()");
				this.applyViewport(client, sid);
				this.vrApplied = null;
				VisorClient.LOGGER.info("VR capture off");
			}
			this.updateScreencast();
		} catch (Exception e) {
			VisorClient.LOGGER.warn("Could not switch the VR capture", e);
		}
		if (!Objects.equals(this.vrWanted, this.vrApplied) && this.vrQueued.compareAndSet(false, true)) {
			this.worker.execute(this::applyVr);
		}
	}

	private static String captureScript(VrCapture capture) {
		return "window.__visor && window.__visor.startCapture('" + capture.region() + "', '" + capture.quality() + "')";
	}

	private void evaluateNow(CdpClient client, String sid, String expression) throws Exception {
		client.send("Runtime.evaluate", params("expression", expression, "returnByValue", true), sid).get(5, TimeUnit.SECONDS);
	}

	// ------------------------------------------------------------------------------------------
	// Commands (render thread)

	public void navigate(String url) {
		if (this.status != Status.READY) {
			this.open(url);
			return;
		}
		this.send("Page.navigate", params("url", url));
	}

	public void back() {
		this.goHistory(-1);
	}

	public void forward() {
		this.goHistory(1);
	}

	private void goHistory(int step) {
		this.send("Page.getNavigationHistory", null).thenAccept(history -> {
			JsonArray entries = history.getAsJsonArray("entries");
			int index = history.get("currentIndex").getAsInt() + step;
			if (entries != null && index >= 0 && index < entries.size()) {
				this.send("Page.navigateToHistoryEntry", params("entryId", entries.get(index).getAsJsonObject().get("id").getAsInt()));
			}
		});
	}

	public void reload() {
		this.send("Page.reload", null);
	}

	public void stopLoading() {
		this.send("Page.stopLoading", null);
	}

	/** Pointer position in CSS pixels of the page; coalesced so the browser never falls behind. */
	public void mouseMove(float x, float y) {
		if (this.status != Status.READY || (Math.abs(x - this.lastMoveX) < 0.35F && Math.abs(y - this.lastMoveY) < 0.35F)) {
			return;
		}
		this.lastMoveX = x;
		this.lastMoveY = y;
		float[] move = {x, y, this.leftDown ? 1.0F : 0.0F};
		synchronized (this.moveLock) {
			if (this.moveInFlight) {
				this.queuedMove = move;
				return;
			}
			this.moveInFlight = true;
		}
		this.sendMove(move);
	}

	private void sendMove(float[] move) {
		boolean down = move[2] > 0.0F;
		this.send("Input.dispatchMouseEvent", mouseParams("mouseMoved", move[0], move[1], down ? "left" : "none", down ? 1 : 0, 0))
			.whenComplete((result, error) -> {
				float[] next;
				synchronized (this.moveLock) {
					next = this.queuedMove;
					this.queuedMove = null;
					if (next == null) {
						this.moveInFlight = false;
					}
				}
				if (next != null) {
					this.sendMove(next);
				}
			});
	}

	public void mouseButton(boolean pressed, float x, float y) {
		if (this.status != Status.READY) {
			return;
		}
		if (pressed) {
			long now = System.currentTimeMillis();
			boolean again = now - this.lastPress < 420L && Math.abs(x - this.lastPressX) < 6.0F && Math.abs(y - this.lastPressY) < 6.0F;
			this.clickCount = again ? Math.min(3, this.clickCount + 1) : 1;
			this.lastPress = now;
			this.lastPressX = x;
			this.lastPressY = y;
			synchronized (this.moveLock) {
				this.queuedMove = null;
			}
		}
		this.leftDown = pressed;
		this.lastMoveX = x;
		this.lastMoveY = y;
		this.send("Input.dispatchMouseEvent", mouseParams(pressed ? "mousePressed" : "mouseReleased", x, y, "left", pressed ? 1 : 0, this.clickCount));
		if (!pressed) {
			this.checkFocus();
		}
	}

	/** {@code deltaY} in CSS pixels, positive scrolls down. */
	public void wheel(float x, float y, float deltaY) {
		if (this.status != Status.READY) {
			return;
		}
		this.send("Input.dispatchMouseEvent", params(
			"type", "mouseWheel", "x", x, "y", y, "deltaX", 0, "deltaY", deltaY, "button", "none", "buttons", 0, "pointerType", "mouse"
		));
	}

	/** A key press (GLFW key code) for the focused element of the page. */
	public void key(int glfwKey, int glfwModifiers) {
		this.dispatchKey(keyInfo(glfwKey, glfwModifiers), glfwKey, glfwModifiers);
	}

	/**
	 * A key of a video player (space, letters, digits, arrows...) as a real key press, so the
	 * page's own shortcuts work while a video plays fullscreen.
	 */
	public void videoKey(int glfwKey, int glfwModifiers) {
		KeyInfo info = keyInfo(glfwKey, glfwModifiers);
		this.dispatchKey(info != null ? info : printableKey(glfwKey, glfwModifiers), glfwKey, glfwModifiers);
	}

	/** Keys {@link #videoKey} knows how to send. */
	public static boolean isVideoKey(int key) {
		return key == GLFW.GLFW_KEY_SPACE || (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9)
			|| key == GLFW.GLFW_KEY_K || key == GLFW.GLFW_KEY_J || key == GLFW.GLFW_KEY_L || key == GLFW.GLFW_KEY_M
			|| key == GLFW.GLFW_KEY_F || key == GLFW.GLFW_KEY_C || key == GLFW.GLFW_KEY_N || key == GLFW.GLFW_KEY_P
			|| key == GLFW.GLFW_KEY_COMMA || key == GLFW.GLFW_KEY_PERIOD
			|| key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_DOWN
			|| key == GLFW.GLFW_KEY_HOME || key == GLFW.GLFW_KEY_END;
	}

	private void dispatchKey(@Nullable KeyInfo info, int glfwKey, int glfwModifiers) {
		if (info == null || this.status != Status.READY) {
			return;
		}
		int modifiers = cdpModifiers(glfwModifiers);
		JsonObject down = params(
			"type", info.text() != null ? "keyDown" : "rawKeyDown", "key", info.key(), "code", info.code(),
			"windowsVirtualKeyCode", info.virtualKey(), "nativeVirtualKeyCode", info.virtualKey(), "modifiers", modifiers
		);
		if (info.text() != null) {
			down.addProperty("text", info.text());
			down.addProperty("unmodifiedText", info.text());
		}
		this.send("Input.dispatchKeyEvent", down);
		this.send("Input.dispatchKeyEvent", params(
			"type", "keyUp", "key", info.key(), "code", info.code(),
			"windowsVirtualKeyCode", info.virtualKey(), "nativeVirtualKeyCode", info.virtualKey(), "modifiers", modifiers
		));
		if (glfwKey == GLFW.GLFW_KEY_ENTER || glfwKey == GLFW.GLFW_KEY_KP_ENTER || glfwKey == GLFW.GLFW_KEY_TAB) {
			CompletableFuture.delayedExecutor(300L, TimeUnit.MILLISECONDS).execute(this::checkFocus);
		}
	}

	public void insertText(String text) {
		if (!text.isEmpty() && this.status == Status.READY) {
			this.send("Input.insertText", params("text", text));
		}
	}

	/** Asks the page whether a text field has the keyboard focus (answer via {@link #pollFocusChange()}). */
	public void checkFocus() {
		if (this.status != Status.READY) {
			return;
		}
		this.evaluate(FOCUS_SCRIPT).thenAccept(value -> {
			this.editableFocused = "true".equals(value);
			this.focusChanged.set(true);
		});
	}

	public void blurPage() {
		this.evaluate("(() => { const e = document.activeElement; if (e && e.blur) e.blur(); })()");
	}

	public void pauseMedia() {
		this.evaluate("document.querySelectorAll('video, audio').forEach(m => { try { m.pause(); } catch (e) {} })");
	}

	/** Volume of videos and music in the page (0-1), applied to every page from now on. */
	public void setMediaVolume(float volume) {
		if (Math.abs(volume - this.wantedVolume) < 0.004F && Math.abs(volume - this.appliedVolume) < 0.004F) {
			return;
		}
		this.wantedVolume = volume;
		if (this.status == Status.READY && this.volumeQueued.compareAndSet(false, true)) {
			this.worker.execute(() -> {
				this.volumeQueued.set(false);
				CdpClient client = this.cdp;
				String sid = this.session;
				if (client != null && sid != null && Math.abs(this.wantedVolume - this.appliedVolume) >= 0.004F) {
					try {
						this.installHelper(client, sid, this.wantedVolume);
					} catch (Exception e) {
						VisorClient.LOGGER.debug("Could not change the browser volume", e);
					}
				}
			});
		}
	}

	/** Most page frames per second that are streamed (30 or 60). */
	public void setFrameRate(int fps) {
		this.frameNanos = 1_000_000_000L / Math.max(1, fps);
	}

	/** Page zoom: a smaller CSS viewport shown at the same size makes everything bigger. */
	public void setZoom(float zoom) {
		int width = Math.round(VIEW_WIDTH / zoom);
		int height = Math.round(VIEW_HEIGHT / zoom);
		if (width == this.cssWidth && height == this.cssHeight) {
			return;
		}
		this.cssWidth = width;
		this.cssHeight = height;
		if (this.status == Status.READY) {
			this.worker.execute(() -> {
				CdpClient client = this.cdp;
				String sid = this.session;
				String target = this.targetId;
				if (client == null || sid == null || this.vrApplied != null) {
					return;
				}
				try {
					if (this.hiddenWindow && target != null) {
						this.placeWindow(client, target);
					}
					this.applyViewport(client, sid);
					if (this.screencasting) {
						client.send("Page.stopScreencast", null, sid).get(2, TimeUnit.SECONDS);
						this.screencasting = false;
						this.updateScreencast();
					}
				} catch (Exception e) {
					VisorClient.LOGGER.debug("Could not change the page zoom", e);
				}
			});
		}
	}

	/** Runs a script in the page; completes with its result as text. */
	public CompletableFuture<String> evaluate(String expression) {
		return this.send("Runtime.evaluate", params("expression", expression, "returnByValue", true, "awaitPromise", true)).thenApply(result -> {
			JsonElement inner = result.get("result");
			JsonElement value = inner != null && inner.isJsonObject() ? inner.getAsJsonObject().get("value") : null;
			if (value == null || value.isJsonNull()) {
				return "";
			}
			return value.isJsonPrimitive() ? value.getAsString() : value.toString();
		});
	}

	private CompletableFuture<JsonObject> send(String method, @Nullable JsonObject params) {
		CdpClient client = this.cdp;
		String sid = this.session;
		if (client == null || sid == null) {
			return CompletableFuture.failedFuture(new IllegalStateException("The browser is not running"));
		}
		return client.send(method, params, sid);
	}

	// ------------------------------------------------------------------------------------------
	// Helpers

	private record KeyInfo(String key, String code, int virtualKey, @Nullable String text) {
	}

	private static @Nullable KeyInfo keyInfo(int key, int modifiers) {
		KeyInfo named = switch (key) {
			case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> new KeyInfo("Enter", "Enter", 13, "\r");
			case GLFW.GLFW_KEY_BACKSPACE -> new KeyInfo("Backspace", "Backspace", 8, null);
			case GLFW.GLFW_KEY_TAB -> new KeyInfo("Tab", "Tab", 9, null);
			case GLFW.GLFW_KEY_DELETE -> new KeyInfo("Delete", "Delete", 46, null);
			case GLFW.GLFW_KEY_LEFT -> new KeyInfo("ArrowLeft", "ArrowLeft", 37, null);
			case GLFW.GLFW_KEY_UP -> new KeyInfo("ArrowUp", "ArrowUp", 38, null);
			case GLFW.GLFW_KEY_RIGHT -> new KeyInfo("ArrowRight", "ArrowRight", 39, null);
			case GLFW.GLFW_KEY_DOWN -> new KeyInfo("ArrowDown", "ArrowDown", 40, null);
			case GLFW.GLFW_KEY_HOME -> new KeyInfo("Home", "Home", 36, null);
			case GLFW.GLFW_KEY_END -> new KeyInfo("End", "End", 35, null);
			case GLFW.GLFW_KEY_PAGE_UP -> new KeyInfo("PageUp", "PageUp", 33, null);
			case GLFW.GLFW_KEY_PAGE_DOWN -> new KeyInfo("PageDown", "PageDown", 34, null);
			default -> null;
		};
		if (named != null) {
			return named;
		}
		boolean shortcut = (modifiers & (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SUPER)) != 0;
		if (shortcut && key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) {
			char letter = (char) ('a' + (key - GLFW.GLFW_KEY_A));
			return new KeyInfo(String.valueOf(letter), "Key" + Character.toUpperCase(letter), 'A' + (key - GLFW.GLFW_KEY_A), null);
		}
		return null;
	}

	/** Space, letters, digits, comma and period as keys that also type their character. */
	private static @Nullable KeyInfo printableKey(int key, int modifiers) {
		boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
		if (key == GLFW.GLFW_KEY_SPACE) {
			return new KeyInfo(" ", "Space", 32, " ");
		}
		if (key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) {
			char letter = (char) ((shift ? 'A' : 'a') + (key - GLFW.GLFW_KEY_A));
			return new KeyInfo(String.valueOf(letter), "Key" + Character.toUpperCase(letter), 'A' + (key - GLFW.GLFW_KEY_A), String.valueOf(letter));
		}
		if (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9) {
			String digit = String.valueOf(key - GLFW.GLFW_KEY_0);
			return new KeyInfo(digit, "Digit" + digit, '0' + (key - GLFW.GLFW_KEY_0), digit);
		}
		if (key == GLFW.GLFW_KEY_COMMA) {
			return new KeyInfo(shift ? "<" : ",", "Comma", 188, shift ? "<" : ",");
		}
		if (key == GLFW.GLFW_KEY_PERIOD) {
			return new KeyInfo(shift ? ">" : ".", "Period", 190, shift ? ">" : ".");
		}
		return null;
	}

	private static int cdpModifiers(int glfw) {
		int modifiers = 0;
		if ((glfw & GLFW.GLFW_MOD_ALT) != 0) {
			modifiers |= 1;
		}
		if ((glfw & GLFW.GLFW_MOD_CONTROL) != 0) {
			modifiers |= 2;
		}
		if ((glfw & GLFW.GLFW_MOD_SUPER) != 0) {
			modifiers |= 4;
		}
		if ((glfw & GLFW.GLFW_MOD_SHIFT) != 0) {
			modifiers |= 8;
		}
		return modifiers;
	}

	private static JsonObject mouseParams(String type, float x, float y, String button, int buttons, int clickCount) {
		return params("type", type, "x", x, "y", y, "button", button, "buttons", buttons, "clickCount", clickCount, "pointerType", "mouse");
	}

	static JsonObject params(Object... keyValues) {
		JsonObject object = new JsonObject();
		for (int i = 0; i + 1 < keyValues.length; i += 2) {
			String key = (String) keyValues[i];
			Object value = keyValues[i + 1];
			if (value instanceof JsonElement element) {
				object.add(key, element);
			} else if (value instanceof Boolean bool) {
				object.addProperty(key, bool);
			} else if (value instanceof Number number) {
				object.addProperty(key, number);
			} else {
				object.addProperty(key, String.valueOf(value));
			}
		}
		return object;
	}

	private static String string(JsonObject object, String key) {
		JsonElement element = object.get(key);
		return element != null && element.isJsonPrimitive() ? element.getAsString() : "";
	}

	private static String describe(Throwable error) {
		Throwable cause = error;
		while ((cause instanceof ExecutionException || cause instanceof java.util.concurrent.CompletionException) && cause.getCause() != null) {
			cause = cause.getCause();
		}
		String message = cause.getMessage();
		return message == null || message.isBlank() ? cause.getClass().getSimpleName() : message;
	}

	private static String language() {
		try {
			String code = Minecraft.getInstance().getLanguageManager().getSelected();
			String[] parts = code.split("_");
			return parts.length == 2 ? parts[0] + "-" + parts[1].toUpperCase(Locale.ROOT) : code;
		} catch (RuntimeException e) {
			return "en-US";
		}
	}

	private static ThreadFactory daemon(String name) {
		return runnable -> {
			Thread thread = new Thread(runnable, name);
			thread.setDaemon(true);
			return thread;
		};
	}

	private static String loadHelper() {
		try (InputStream in = BrowserSession.class.getResourceAsStream("/assets/visor/browser/helper.js")) {
			if (in == null) {
				throw new IOException("missing");
			}
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException("The headset browser's page script is missing from the mod", e);
		}
	}

	/** The page script (see {@code browser/helper.js}) with the volume filled in. */
	private static String helperScript(float volume) {
		return HELPER.replace("%VOLUME%", String.format(Locale.ROOT, "%.3f", volume));
	}
}
