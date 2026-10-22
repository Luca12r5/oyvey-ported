package dev.visor;

import com.mojang.blaze3d.platform.InputConstants;
import dev.visor.browser.BrowserSession;
import dev.visor.donut.DrDonutt;
import dev.visor.headset.FirstPersonHeadset;
import dev.visor.render.FrameCapture;
import dev.visor.render.PanelSurface;
import dev.visor.render.VisorFx;
import dev.visor.setting.KeySetting;
import dev.visor.setting.VisorSettings;
import dev.visor.sound.VisorSounds;
import dev.visor.ui.BrowserWindow;
import dev.visor.ui.Dock;
import dev.visor.ui.Ease;
import dev.visor.ui.MainPanel;
import dev.visor.ui.Spring;
import dev.visor.ui.TextField;
import dev.visor.ui.Theme;
import dev.visor.ui.UiInput;
import dev.visor.vr.VrMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * The headset state machine: putting it on, using it, taking it off. Owns the timeline of the
 * animations and sounds, where the panels float, what the pointer is aiming at, and which input
 * (mouse, keyboard) the headset currently takes away from the game.
 */
public final class VisorController {
	public enum State { OFF, EQUIPPING, ON, REMOVING }

	/** Which window floats in front of the player: the home panel or the browser. */
	public enum App { HOME, BROWSER }

	// Equip timeline (seconds). Arms + headset move until ~1.2 s (see FirstPersonHeadset).
	static final double EQ_LENS_START = 0.66;
	static final double EQ_LENS_CLOSED = 0.93;
	static final double EQ_LOGO_IN = 1.00;
	static final double EQ_BOOT_SOUND = 1.02;
	static final double EQ_LOGO_OUT = 1.45;
	static final double EQ_LOGO_GONE = 1.65;
	static final double EQ_REVEAL_START = 1.55;
	static final double EQ_REVEAL_END = 2.15;
	static final double EQ_PANEL_START = 1.65;
	static final double EQ_PANEL_END = 2.30;
	static final double EQ_DOCK_START = 1.80;
	static final double EQ_DOCK_END = 2.40;
	static final double EQ_HANDS_START = 1.95;
	static final double EQ_HANDS_END = 2.45;
	static final double EQ_TOTAL = 2.45;
	// Remove timeline.
	static final double RM_PANEL_END = 0.22;
	static final double RM_LENS_START = 0.08;
	static final double RM_LENS_CLOSED = 0.25;
	static final double RM_SOUND = 0.22;
	static final double RM_OPEN_START = 0.31;
	static final double RM_OPEN_END = 0.62;

	static final float PANEL_DROP = -0.02F;
	static final float PANEL_PIXEL = 0.00172F;
	static final float DOCK_RATIO = 0.78F;
	static final float DOCK_DROP_RATIO = 0.37F;
	static final float DOCK_PIXEL = 0.00140F;
	/** How far (blocks) the panels may trail behind the moving player. */
	static final double MAX_TRAIL = 0.30;
	/** Layout pixels the (taller) browser window sits above the home panel's centre. */
	static final float BROWSER_RAISE = 40.0F;
	/** In "stay in place" mode, panels further away than this come back (teleports, respawns). */
	static final double STAY_RESET_DISTANCE = 48.0;
	/** Band (layout pixels) around a window, and around the dock, where the pointer grabs it to move it. */
	static final float GRAB_EDGE = 44.0F;
	static final float GRAB_EDGE_DOCK = 28.0F;
	/** How far (degrees) moved windows can be tilted up or down around the player. */
	static final float MIN_PITCH = -50.0F;
	static final float MAX_PITCH = 60.0F;
	/** Nearest and furthest (blocks from the eyes) a moved window can be. */
	static final float MIN_WINDOW_DISTANCE = 0.9F;
	static final float MAX_WINDOW_DISTANCE = 48.0F;
	/** Seconds without moving before the hands and the cursor make way for a video. */
	static final float WATCH_IDLE = 2.5F;

	private static final VisorController INSTANCE = new VisorController();

	private State state = State.OFF;
	private long frameIndex;
	private double stateTime;
	private long lastNanos;
	private float frameDelta;
	private double clock;
	private boolean firstPersonAnimation;
	private boolean bootSequence;
	private int events;

	public final PanelSurface panel = new PanelSurface(MainPanel.WIDTH, MainPanel.HEIGHT);
	public final PanelSurface browserSurface = new PanelSurface(BrowserWindow.WIDTH, BrowserWindow.HEIGHT);
	public final PanelSurface dock = new PanelSurface(Dock.WIDTH, Dock.HEIGHT);
	private Vec3 anchor = Vec3.ZERO;
	private final Spring yaw = new Spring(0.0F, 7.0F);
	private float layoutYaw;
	private @Nullable Object lastLevel;
	private float partialTick = 1.0F;

	// Moving the windows by their edge: they swing around the player (yaw and pitch), "reach" further
	// away when they were picked up from a distance, and turn to face the player ("face" springs).
	private final Spring pitch = new Spring(0.0F, 7.0F);
	private final Spring reach = new Spring(0.0F, 7.0F);
	private final Spring faceYaw = new Spring(0.0F, 9.0F);
	private final Spring facePitch = new Spring(0.0F, 9.0F);
	private float layoutPitch;
	private float layoutReach;
	private float layoutFaceYaw;
	private float layoutFacePitch;
	private @Nullable PanelSurface grabSurface;
	private float grabViewYaw;
	private float grabViewPitch;
	private float grabYaw;
	private float grabPitch;
	private boolean movingWindows;
	private boolean movingDonut;

	public final UiInput panelInput = new UiInput();
	public final UiInput browserInput = new UiInput();
	public final UiInput dockInput = new UiInput();
	public final MainPanel mainPanel = new MainPanel();
	public final BrowserWindow browserUi = new BrowserWindow();
	public final Dock dockUi = new Dock();
	private App app = App.HOME;
	private final Spring appBlend = new Spring(0.0F, 10.0F);
	private boolean mouseDown;
	private boolean secondaryDown;

	// Keyboard capture: a key binding waiting for its key, a focused text field, or a text box on
	// the web page.
	private @Nullable KeySetting capturingKey;
	private @Nullable TextField focusedText;
	private boolean pageTyping;

	/** Camera relative world position the pointer hits, valid when {@link #pointerHit} is true. */
	public final Vector3f pointerWorld = new Vector3f();
	public boolean pointerHit;
	public boolean pointerOnDock;
	private @Nullable PanelSurface pointerSurface;
	private float pointerU;
	private float pointerV;
	private @Nullable Vec3 pointerTarget;
	/** Window (or the dock) whose edge the pointer is on, or that is being moved, and where on it. */
	public @Nullable PanelSurface edgeSurface;
	public float edgeX;
	public float edgeY;
	/** The pointer is on Doctor Donut, outside the windows. */
	public boolean pointerOnDonut;

	// Presentation, recomputed every frame.
	public float panelAlpha;
	public float panelScale = 1.0F;
	public float panelPush;
	public float homeAlpha;
	public float homeScale = 1.0F;
	public float browserAlpha;
	public float browserScale = 1.0F;
	public float dockAlpha;
	public float dockScale = 1.0F;
	public float handsAlpha;
	public float handsRise;
	public float logoAlpha;
	public float logoTime;
	public final VisorFx fx = new VisorFx();
	private final Spring screenHide = new Spring(0.0F, 14.0F);
	public float visibility = 1.0F;

	// A fullscreen video: the browser window becomes a big flat cinema screen in a dimmed world.
	private final Spring cinemaBlend = new Spring(0.0F, 9.0F);
	private boolean cinemaTarget;
	private float cinema;
	private final PanelSurface cinemaFrame = new PanelSurface(BrowserWindow.WIDTH, BrowserWindow.HEIGHT);
	/** Where the cinema screen hangs: where the player looked when the video went fullscreen. */
	private final Spring cinemaYaw = new Spring(0.0F, 7.0F);
	private final Spring cinemaPitch = new Spring(0.0F, 7.0F);
	/** How dark the world is behind the cinema screen (0-1). */
	public float cinemaDim;
	// While a video is watched without moving, the hands and the cursor fade away.
	private final Spring pointerShow = new Spring(1.0F, 8.0F);
	private float idle;
	private float lastViewYaw = Float.NaN;
	private float lastViewPitch;
	private Vec3 lastEye = Vec3.ZERO;

	/** Debug/testing hook: when set, time is taken from here instead of the system clock. */
	public Double debugTimeOverride;

	private VisorController() {
		VisorSettings.PANEL_ANCHOR.onChange(this::anchorModeChanged);
	}

	public static VisorController get() {
		return INSTANCE;
	}

	public State state() {
		return this.state;
	}

	public double stateTime() {
		return this.stateTime;
	}

	public double clock() {
		return this.clock;
	}

	public float frameDelta() {
		return this.frameDelta;
	}

	public long frameIndex() {
		return this.frameIndex;
	}

	public boolean active() {
		return this.state != State.OFF;
	}

	public boolean firstPersonAnimation() {
		return this.firstPersonAnimation;
	}

	public boolean mouseDown() {
		return this.mouseDown;
	}

	public boolean secondaryDown() {
		return this.secondaryDown;
	}

	/** A window is being moved by its edge. */
	public boolean movingWindows() {
		return this.movingWindows;
	}

	/** Doctor Donut is being carried with the pointer. */
	public boolean movingDonut() {
		return this.movingDonut;
	}

	/** How far the browser window has turned into the cinema screen of a fullscreen video (0-1, eased). */
	public float cinema() {
		return this.cinema;
	}

	/** The page is fullscreen and shown as a cinema screen (or on its way there). */
	public boolean cinemaTarget() {
		return this.cinemaTarget;
	}

	/** Hands and cursor (0-1): they fade while a video is watched without moving. */
	public float pointerShown() {
		return this.pointerShow.value();
	}

	/** Watching a fullscreen or VR video: the player stays where they are. */
	public boolean freezesMovement() {
		return this.state == State.ON && (this.cinemaTarget || VrMode.get().active());
	}

	/** Whether the headset sits on the player's head right now (for the third person model). */
	public boolean wornOnHead() {
		return switch (this.state) {
			case OFF -> false;
			case ON -> true;
			case EQUIPPING -> !this.firstPersonAnimation || this.stateTime >= 0.90;
			case REMOVING -> !this.firstPersonAnimation ? this.stateTime < RM_PANEL_END : this.stateTime < 0.30;
		};
	}

	// ------------------------------------------------------------------------------------------
	// Commands

	public void toggle() {
		switch (this.state) {
			case OFF -> this.equip();
			case ON -> this.remove();
			default -> {
			}
		}
	}

	public void equip() {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.screen != null || this.state != State.OFF) {
			return;
		}
		this.firstPersonAnimation = minecraft.options.getCameraType().isFirstPerson() && VisorSettings.HEADSET_ANIMATION.on();
		this.bootSequence = this.firstPersonAnimation && VisorSettings.BOOT_ANIMATION.on();
		this.setState(State.EQUIPPING);
		if (!this.firstPersonAnimation) {
			// Without the arms in view, skip straight to the panels appearing.
			this.stateTime = EQ_PANEL_START - 0.05;
			this.events = 0b011;
			VisorSounds.play(VisorSounds.PANEL_OPEN, 0.7F);
			this.events |= 0b100;
		}
		this.place(player, 1.0F, true);
		this.panelInput.clear();
		this.dockInput.clear();
	}

	public void remove() {
		if (this.state != State.ON) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		this.firstPersonAnimation = minecraft.options.getCameraType().isFirstPerson() && VisorSettings.HEADSET_ANIMATION.on();
		this.stopMoving(false);
		VrMode.get().exitNow();
		if (this.cinemaTarget) {
			// Put back on, the headset shows the browser window again, not the fullscreen video.
			BrowserSession.get().exitFullscreen();
		}
		this.mainPanel.closeSheet();
		this.browserUi.closeSheet();
		this.stopKeyboardCapture();
		this.pauseBrowserMedia();
		this.setState(State.REMOVING);
	}

	public void forceOff() {
		if (this.state != State.OFF) {
			this.pauseBrowserMedia();
		}
		this.setState(State.OFF);
		this.panelInput.clear();
		this.browserInput.clear();
		this.dockInput.clear();
		this.mouseDown = false;
		this.secondaryDown = false;
		this.stopMoving(false);
		this.edgeSurface = null;
		this.pointerOnDonut = false;
		DrDonutt.get().pointedAt(false);
		this.stopKeyboardCapture();
		VrMode.get().exitNow();
		this.cinemaTarget = false;
		this.cinemaBlend.snap(0.0F);
		this.cinema = 0.0F;
		this.cinemaDim = 0.0F;
		this.fx.reset();
	}

	private void pauseBrowserMedia() {
		if (VisorSettings.BROWSER_PAUSE.on() && BrowserSession.get().ready()) {
			BrowserSession.get().pauseMedia();
		}
	}

	/** Re-centres the panels (or a fullscreen video) in front of where the player is looking now. */
	public void recenter() {
		this.recenter(true);
	}

	public void recenter(boolean sound) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || this.state != State.ON) {
			return;
		}
		this.stopMoving(false);
		if (this.cinemaTarget) {
			this.cinemaYaw.target(this.cinemaYaw.value() + Mth.wrapDegrees(player.getViewYRot(1.0F) - this.cinemaYaw.value()));
			this.cinemaPitch.target(player.getViewXRot(1.0F));
		} else {
			this.place(player, 1.0F, !VisorSettings.follows());
		}
		if (sound) {
			VisorSounds.play(VisorSounds.RECENTER, 0.7F);
		}
	}

	/** Switching back to "follow me" brings the panels round to where the player looks. */
	private void anchorModeChanged() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null && this.state == State.ON && VisorSettings.follows()) {
			this.place(player, 1.0F, false);
		}
	}

	// ------------------------------------------------------------------------------------------
	// Apps: the home panel and the browser window

	public App app() {
		return this.app;
	}

	public void showHome() {
		if (this.app == App.HOME) {
			return;
		}
		this.app = App.HOME;
		this.stopPageTyping(false);
		this.browserInput.clear();
		this.panelInput.clear();
		VisorSounds.play(VisorSounds.TAB, 0.7F);
	}

	/** Shows the browser window; opens {@code url} when given, otherwise keeps the current page. */
	public void showBrowser(@Nullable String url) {
		this.mainPanel.closeSheet();
		this.stopKeyboardCapture();
		this.browserUi.open(url);
		if (this.app != App.BROWSER) {
			this.app = App.BROWSER;
			this.panelInput.clear();
			this.browserInput.clear();
			VisorSounds.play(VisorSounds.PANEL_OPEN, 0.6F);
		}
	}

	public void toggleBrowser() {
		if (this.app == App.BROWSER) {
			this.showHome();
		} else {
			this.showBrowser(null);
		}
	}

	/** Quits the browser and returns to the home panel. */
	public void closeBrowser() {
		this.closeBrowser(true);
	}

	public void closeBrowser(boolean sound) {
		this.stopPageTyping(false);
		VrMode.get().exitNow();
		boolean wasOpen = BrowserSession.get().running() || this.app == App.BROWSER;
		BrowserSession.get().close();
		this.browserUi.reset();
		if (this.app == App.BROWSER) {
			this.app = App.HOME;
			this.browserInput.clear();
			this.panelInput.clear();
		}
		if (wasOpen && sound) {
			VisorSounds.play(VisorSounds.PANEL_CLOSE, 0.6F);
		}
	}

	/** A settings sheet is open on the visible window. */
	public boolean sheetOpen() {
		return this.app == App.BROWSER ? this.browserUi.sheetOpen() : this.mainPanel.sheetOpen();
	}

	private void setState(State state) {
		this.state = state;
		this.stateTime = 0.0;
		this.events = 0;
	}

	private void place(LocalPlayer player, float partialTick, boolean snap) {
		float target = player.getViewYRot(partialTick);
		if (snap) {
			this.yaw.snap(target);
			this.anchor = player.getEyePosition(partialTick);
			this.pitch.snap(0.0F);
			this.reach.snap(0.0F);
			this.faceYaw.snap(0.0F);
			this.facePitch.snap(0.0F);
		} else {
			this.yaw.target(this.yaw.value() + Mth.wrapDegrees(target - this.yaw.value()));
			this.pitch.target(0.0F);
			this.reach.target(0.0F);
		}
	}

	// ------------------------------------------------------------------------------------------
	// Moving things: a window grabbed by its edge, or Doctor Donut himself

	private boolean moving() {
		return this.movingWindows || this.movingDonut;
	}

	private void startMovingWindows() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || this.edgeSurface == null) {
			return;
		}
		if (!VisorSettings.follows()) {
			this.reanchor(player);
		}
		this.grabSurface = this.edgeSurface;
		this.grabViewYaw = player.getViewYRot(this.partialTick);
		this.grabViewPitch = player.getViewXRot(this.partialTick);
		this.grabYaw = this.yaw.value();
		this.grabPitch = this.pitch.value();
		this.movingWindows = true;
		VisorSounds.play(VisorSounds.CLICK, 0.55F);
	}

	/** The windows swing around the player exactly as far as the pointer turned since they were grabbed. */
	private void moveWindows(LocalPlayer player, float partialTick) {
		this.yaw.snap(this.grabYaw + player.getViewYRot(partialTick) - this.grabViewYaw);
		this.pitch.snap(Mth.clamp(this.grabPitch - (player.getViewXRot(partialTick) - this.grabViewPitch), MIN_PITCH, MAX_PITCH));
	}

	/**
	 * Windows that stay in place are measured from where the player stands now instead of where they
	 * were put, so they move with the pointer one to one. They stay where they are and turn round to
	 * face the player.
	 */
	private void reanchor(LocalPlayer player) {
		Vec3 eye = player.getEyePosition(this.partialTick);
		if (eye.distanceToSqr(this.anchor) < 0.25) {
			return;
		}
		float base = VisorSettings.PANEL_DISTANCE.floatValue();
		float oldYaw = this.yaw.value();
		float oldPitch = this.pitch.value();
		Vector3f forward = direction(oldYaw, oldPitch).mul(base + this.reach.value());
		Vec3 offset = this.anchor.add(forward.x, forward.y, forward.z).subtract(eye);
		double length = offset.length();
		if (length < 1.0E-3) {
			return;
		}
		float newYaw = (float) Math.toDegrees(Math.atan2(-offset.x, offset.z));
		float newPitch = (float) Math.toDegrees(Math.asin(Mth.clamp(offset.y / length, -1.0, 1.0)));
		this.anchor = eye;
		this.yaw.snap(oldYaw + Mth.wrapDegrees(newYaw - oldYaw));
		this.pitch.snap(Mth.clamp(newPitch, MIN_PITCH, MAX_PITCH));
		this.reach.snap(Mth.clamp((float) length, MIN_WINDOW_DISTANCE, MAX_WINDOW_DISTANCE) - base);
		this.faceYaw.snap(this.faceYaw.value() + oldYaw - this.yaw.value());
		this.facePitch.snap(this.facePitch.value() + oldPitch - this.pitch.value());
	}

	private void startMovingDonut() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null && DrDonutt.get().beginDrag(player, this.partialTick)) {
			this.movingDonut = true;
			VisorSounds.play(VisorSounds.CLICK, 0.55F);
		}
	}

	private void stopMoving(boolean sound) {
		if (this.movingWindows) {
			this.movingWindows = false;
			this.grabSurface = null;
			if (sound) {
				VisorSounds.play(VisorSounds.TAB, 0.5F);
			}
		}
		if (this.movingDonut) {
			this.movingDonut = false;
			DrDonutt.get().endDrag();
			if (sound) {
				VisorSounds.play(VisorSounds.TAB, 0.5F);
			}
		}
	}

	/** Unit vector from the anchor towards windows at this yaw and pitch (degrees). */
	private static Vector3f direction(float yawDegrees, float pitchDegrees) {
		float yawRad = yawDegrees * Mth.DEG_TO_RAD;
		float pitchRad = pitchDegrees * Mth.DEG_TO_RAD;
		float horizontal = Mth.cos(pitchRad);
		return new Vector3f(-Mth.sin(yawRad) * horizontal, Mth.sin(pitchRad), Mth.cos(yawRad) * horizontal);
	}

	/** Whether a hit lies within {@code band} pixels around a {@code width} x {@code height} layout. */
	private static boolean nearEdge(float[] hit, float width, float height, float band) {
		return hit[0] >= -band && hit[0] <= width + band && hit[1] >= -band && hit[1] <= height + band;
	}

	// ------------------------------------------------------------------------------------------
	// Keyboard capture

	public void startKeyCapture(KeySetting setting) {
		this.focusedText = null;
		this.capturingKey = setting;
	}

	public boolean isCapturing(KeySetting setting) {
		return this.capturingKey == setting;
	}

	public void focusText(TextField field) {
		this.capturingKey = null;
		this.pageTyping = false;
		this.focusedText = field;
	}

	public void unfocusText(TextField field) {
		if (this.focusedText == field) {
			this.focusedText = null;
		}
	}

	public boolean isFocused(TextField field) {
		return this.focusedText == field;
	}

	public void stopKeyboardCapture() {
		this.capturingKey = null;
		this.focusedText = null;
		this.pageTyping = false;
	}

	/** A text box on the web page has the focus: keys go to the page until Escape. */
	public void startPageTyping() {
		this.capturingKey = null;
		this.focusedText = null;
		this.pageTyping = true;
	}

	public void stopPageTyping(boolean blurPage) {
		if (this.pageTyping) {
			this.pageTyping = false;
			if (blurPage) {
				BrowserSession.get().blurPage();
			}
		}
	}

	public boolean isPageTyping() {
		return this.pageTyping;
	}

	/** True while typed keys belong to the headset UI instead of the game. */
	public boolean capturesKeyboard() {
		return this.state == State.ON && (this.capturingKey != null || this.focusedText != null || this.pageTyping);
	}

	/** @return true when the key event was used by the headset */
	public boolean onKey(KeyEvent event, int action) {
		if (this.state == State.ON && !this.capturesKeyboard() && Minecraft.getInstance().screen == null) {
			// Watching a video: its keys (pause, seek, mute...) go to the video instead of the game.
			if (VrMode.get().onKey(event.key(), action)) {
				return true;
			}
			if (this.cinemaTarget && BrowserSession.isVideoKey(event.key())) {
				if (action != 0) {
					BrowserSession.get().videoKey(event.key(), event.modifiers());
				}
				return true;
			}
		}
		if (!this.capturesKeyboard() || Minecraft.getInstance().screen != null) {
			return false;
		}
		if (action == 0) {
			return true;
		}
		int key = event.key();
		if (this.capturingKey != null) {
			if (action != 1) {
				return true;
			}
			if (key == GLFW.GLFW_KEY_ESCAPE) {
				this.capturingKey = null;
				VisorSounds.play(VisorSounds.DENY, 0.6F);
			} else if (key == GLFW.GLFW_KEY_BACKSPACE || key == GLFW.GLFW_KEY_DELETE) {
				this.capturingKey.set(KeySetting.NONE);
				this.capturingKey = null;
				VisorSounds.play(VisorSounds.CLICK, 0.7F);
			} else if (key != InputConstants.UNKNOWN.getValue()) {
				this.capturingKey.set(key);
				this.capturingKey = null;
				VisorSounds.play(VisorSounds.KEYBIND, 0.8F);
			}
			return true;
		}
		boolean control = (event.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0;
		if (this.pageTyping) {
			if (key == GLFW.GLFW_KEY_ESCAPE) {
				if (action == 1) {
					this.stopPageTyping(true);
					VisorSounds.play(VisorSounds.CLICK, 0.5F);
				}
			} else if (control && key == GLFW.GLFW_KEY_V) {
				if (action == 1) {
					BrowserSession.get().insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
				}
			} else {
				BrowserSession.get().key(key, event.modifiers());
			}
			return true;
		}
		TextField field = this.focusedText;
		if (field != null) {
			switch (key) {
				case GLFW.GLFW_KEY_ESCAPE -> this.focusedText = null;
				case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
					this.focusedText = null;
					field.submit();
				}
				case GLFW.GLFW_KEY_BACKSPACE -> field.backspace(control);
				default -> {
					if (control && key == GLFW.GLFW_KEY_A) {
						field.selectAll();
					} else if (control && key == GLFW.GLFW_KEY_V && action == 1) {
						field.type(Minecraft.getInstance().keyboardHandler.getClipboard().replace('\n', ' ').replace('\r', ' '));
					}
				}
			}
		}
		return true;
	}

	public boolean onChar(CharacterEvent event) {
		if (!this.capturesKeyboard() || Minecraft.getInstance().screen != null) {
			return false;
		}
		if (this.pageTyping) {
			BrowserSession.get().insertText(event.codepointAsString());
			return true;
		}
		if (this.focusedText == null) {
			return false;
		}
		this.focusedText.type(event.codepointAsString());
		return true;
	}

	// ------------------------------------------------------------------------------------------
	// Mouse input (called from mixins, render thread)

	/** @return true when the click was used by the headset and must not reach the game */
	public boolean onMouseButton(int button, boolean pressed) {
		Minecraft minecraft = Minecraft.getInstance();
		if (this.state == State.OFF || minecraft.screen != null) {
			return false;
		}
		if (this.state != State.ON) {
			return true;
		}
		this.idle = 0.0F;
		VrMode vr = VrMode.get();
		if (vr.hidesPanels()) {
			// Inside a VR video only its controls can be used.
			if (button == 0) {
				this.mouseDown = pressed;
			} else if (button == 1) {
				this.secondaryDown = pressed;
			}
			return vr.onMouseButton(button, pressed);
		}
		UiInput target = this.pointerOnDock ? this.dockInput : this.windowInput();
		UiInput[] inputs = {this.panelInput, this.browserInput, this.dockInput};
		switch (button) {
			case 0 -> {
				if (pressed && !this.moving() && (this.edgeSurface != null || this.pointerOnDonut)) {
					// Grabbed by its edge (or Doctor Donut himself): it follows the pointer until the button is let go.
					this.mouseDown = true;
					if (this.edgeSurface != null) {
						this.startMovingWindows();
					} else {
						this.startMovingDonut();
					}
					return true;
				}
				if (!pressed) {
					this.stopMoving(true);
				}
				this.mouseDown = pressed;
				for (UiInput input : inputs) {
					input.down = pressed && input == target;
					if (!pressed) {
						input.released = true;
					}
				}
				if (pressed) {
					target.pressed = true;
				}
			}
			case 1 -> {
				this.secondaryDown = pressed;
				if (pressed) {
					target.secondaryPressed = true;
				}
			}
			case 2 -> {
				if (pressed) {
					this.recenter();
				}
			}
			default -> {
			}
		}
		return true;
	}

	public boolean onScroll(double amount) {
		if (this.state != State.ON || Minecraft.getInstance().screen != null) {
			return false;
		}
		if (VrMode.get().hidesPanels()) {
			return true;
		}
		if (this.movingWindows) {
			// Scrolling while holding the windows pushes them away or pulls them closer.
			float base = VisorSettings.PANEL_DISTANCE.floatValue();
			float distance = (base + this.reach.value()) * (1.0F + 0.08F * (float) amount);
			this.reach.snap(Mth.clamp(distance, MIN_WINDOW_DISTANCE, MAX_WINDOW_DISTANCE) - base);
			return true;
		}
		if (this.movingDonut) {
			DrDonutt.get().push((float) amount);
			return true;
		}
		if (!this.pointerHit) {
			return false;
		}
		(this.pointerOnDock ? this.dockInput : this.windowInput()).scroll += (float) amount;
		return true;
	}

	private UiInput windowInput() {
		return this.app == App.BROWSER ? this.browserInput : this.panelInput;
	}

	/**
	 * Escape closes the innermost thing first: key capture, typing, a VR video, a fullscreen video,
	 * settings, then the headset.
	 */
	public boolean onEscape() {
		if (this.state == State.ON) {
			VrMode vr = VrMode.get();
			if (this.capturingKey != null || this.focusedText != null) {
				this.stopKeyboardCapture();
			} else if (this.pageTyping) {
				this.stopPageTyping(true);
			} else if (vr.active()) {
				vr.exit();
			} else if (this.cinemaTarget) {
				BrowserSession.get().exitFullscreen();
				this.cinemaTarget = false;
				VisorSounds.play(VisorSounds.PANEL_CLOSE, 0.5F);
			} else if (this.browserUi.sheetOpen()) {
				this.browserUi.closeSheet();
			} else if (this.mainPanel.sheetOpen()) {
				this.mainPanel.closeSheet();
			} else {
				this.remove();
			}
			return true;
		}
		return this.state != State.OFF;
	}

	/** While the headset is active the player can walk and look around, but not attack or use items. */
	public boolean blocksInteraction() {
		return this.state != State.OFF && Minecraft.getInstance().screen == null;
	}

	// ------------------------------------------------------------------------------------------
	// Per frame update (start of GameRenderer.render)

	public void beginFrame(float partialTick) {
		this.frameIndex++;
		this.partialTick = partialTick;
		long now = System.nanoTime();
		float dt = this.lastNanos == 0L ? 0.0F : (now - this.lastNanos) / 1.0E9F;
		this.lastNanos = now;
		dt = Math.max(0.0F, Math.min(dt, 0.1F));
		if (this.debugTimeOverride != null) {
			dt = 0.0F;
			this.stateTime = this.debugTimeOverride;
		}
		this.frameDelta = dt;
		this.clock += dt;

		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (minecraft.level == null || player == null) {
			if (this.state != State.OFF) {
				this.forceOff();
			}
			return;
		}
		if (this.state == State.OFF) {
			return;
		}
		if (player.isDeadOrDying()) {
			this.forceOff();
			return;
		}
		this.stateTime += dt;
		this.runTimeline();
		if (this.state == State.OFF) {
			return;
		}

		// "Follow me": panels follow the player's position softly but keep their direction (like a
		// real headset); the trailing distance is capped, so walking never brings a panel too close.
		// "Stay in place": panels keep their spot in the world, so you can walk up to them, past
		// them and look at their back.
		Vec3 eye = player.getEyePosition(partialTick);
		if (VisorSettings.follows()) {
			float follow = 1.0F - (float) Math.exp(-dt * 10.0F);
			this.anchor = this.anchor.lerp(eye, follow);
			Vec3 offset = this.anchor.subtract(eye);
			double length = offset.length();
			if (length > 8.0) {
				this.anchor = eye;
			} else if (length > MAX_TRAIL) {
				this.anchor = eye.add(offset.scale(MAX_TRAIL / length));
			}
		} else if (this.lastLevel != minecraft.level || this.anchor.distanceToSqr(eye) > STAY_RESET_DISTANCE * STAY_RESET_DISTANCE) {
			// After a teleport, respawn or a trip to another dimension they come back in front.
			this.place(player, partialTick, true);
		}
		this.lastLevel = minecraft.level;
		if (this.movingWindows) {
			this.moveWindows(player, partialTick);
		} else if (this.movingDonut && !DrDonutt.get().drag(player, partialTick)) {
			this.stopMoving(false);
		}
		this.layoutYaw = this.yaw.update(dt);
		this.layoutPitch = this.pitch.update(dt);
		this.layoutReach = this.reach.update(dt);
		this.layoutFaceYaw = this.faceYaw.update(dt);
		this.layoutFacePitch = this.facePitch.update(dt);
		this.appBlend.target(this.app == App.BROWSER ? 1.0F : 0.0F).update(dt);
		this.lastEye = eye;
		this.updateWatching(minecraft, player, dt);
		this.computePresentation();
		this.layoutSurfaces(this.layoutYaw, FrameCapture.valid ? FrameCapture.cameraPosition : this.anchor);
		this.updatePointer(player, partialTick);
		float hide = this.screenHide.target(minecraft.screen != null ? 1.0F : 0.0F).update(dt);
		this.visibility = 1.0F - hide;
	}

	/** Fullscreen and VR videos: the cinema screen, the VR mode, and the hands making way for them. */
	private void updateWatching(Minecraft minecraft, LocalPlayer player, float dt) {
		VrMode vr = VrMode.get();
		boolean inBrowser = this.state == State.ON && this.app == App.BROWSER && !this.browserUi.showsStartPage();
		vr.update(dt, player, inBrowser, minecraft.screen == null);
		boolean wasCinema = this.cinemaTarget;
		this.cinemaTarget = inBrowser && BrowserSession.get().pageFullscreen() && !vr.active();
		if (this.cinemaTarget && !wasCinema && this.cinemaBlend.value() < 0.01F) {
			// A fullscreen video appears where the player looks, filling the view.
			this.cinemaYaw.snap(player.getViewYRot(this.partialTick));
			this.cinemaPitch.snap(player.getViewXRot(this.partialTick));
		}
		this.cinemaYaw.update(dt);
		this.cinemaPitch.update(dt);
		this.cinema = Ease.inOutCubic(this.cinemaBlend.target(this.cinemaTarget ? 1.0F : 0.0F).update(dt));

		float viewYaw = player.getViewYRot(this.partialTick);
		float viewPitch = player.getViewXRot(this.partialTick);
		boolean turned = Float.isNaN(this.lastViewYaw)
			|| Math.abs(Mth.wrapDegrees(viewYaw - this.lastViewYaw)) > 0.02F || Math.abs(viewPitch - this.lastViewPitch) > 0.02F;
		this.lastViewYaw = viewYaw;
		this.lastViewPitch = viewPitch;
		this.idle = turned ? 0.0F : this.idle + dt;
		boolean watching = this.cinemaTarget || vr.showsVideo();
		boolean needed = !watching || this.idle < WATCH_IDLE || vr.panelAlpha() > 0.3F || this.mouseDown;
		this.pointerShow.target(needed ? 1.0F : 0.0F).update(dt);
	}

	private void runTimeline() {
		if (this.state == State.EQUIPPING) {
			if (this.firstPersonAnimation && !this.bootSequence && this.stateTime > EQ_LENS_CLOSED + 0.06 && this.stateTime < EQ_REVEAL_START) {
				// Start-up screen switched off: go from dark straight to the reveal.
				this.stateTime = EQ_REVEAL_START;
				this.events |= 0b010;
			}
			double t = this.stateTime;
			if (this.event(0, t >= 0.0) && this.firstPersonAnimation) {
				VisorSounds.play(VisorSounds.EQUIP, 1.0F);
			}
			if (this.event(1, t >= EQ_BOOT_SOUND)) {
				VisorSounds.play(VisorSounds.BOOT, 1.0F);
			}
			if (this.event(2, t >= EQ_PANEL_START)) {
				VisorSounds.play(VisorSounds.PANEL_OPEN, 0.7F);
			}
			if (t >= EQ_TOTAL) {
				this.setState(State.ON);
			}
		} else if (this.state == State.REMOVING) {
			double t = this.stateTime;
			if (this.event(0, t >= 0.0)) {
				VisorSounds.play(VisorSounds.PANEL_CLOSE, 0.7F);
			}
			if (this.event(1, t >= RM_SOUND && this.firstPersonAnimation)) {
				VisorSounds.play(VisorSounds.REMOVE, 1.0F);
			}
			double end = this.firstPersonAnimation ? FirstPersonHeadset.removeDuration() : RM_PANEL_END + 0.05;
			if (t >= end) {
				this.forceOff();
			}
		}
	}

	private boolean event(int bit, boolean condition) {
		int mask = 1 << bit;
		if (condition && (this.events & mask) == 0) {
			this.events |= mask;
			return true;
		}
		return false;
	}

	/**
	 * Re-anchors the surfaces to this frame's camera, captured while the world rendered (the
	 * layout in {@link #beginFrame} used the previous frame's camera). Without this, panels that
	 * stay in place would lag one frame behind the world while walking.
	 */
	public void syncCamera() {
		if (this.state == State.OFF || !FrameCapture.valid) {
			return;
		}
		this.layoutSurfaces(this.layoutYaw, FrameCapture.cameraPosition);
		if (this.pointerHit && this.pointerSurface != null) {
			this.pointerSurface.map(this.pointerU, this.pointerV, this.pointerWorld);
		} else if (this.pointerHit && this.pointerTarget != null) {
			this.pointAtWorld(this.pointerTarget, FrameCapture.cameraPosition);
		}
	}

	private void layoutSurfaces(float yawDegrees, Vec3 camera) {
		// The windows and the dock hang around the anchor as one piece: towards yaw, tilted up or down
		// by pitch, pushed "reach" further away, and turned by the face springs while they turn round.
		float yawRad = yawDegrees * Mth.DEG_TO_RAD;
		float pitchRad = this.layoutPitch * Mth.DEG_TO_RAD;
		Vector3f forward = direction(yawDegrees, this.layoutPitch);
		Vector3f up = new Vector3f(Mth.sin(yawRad) * Mth.sin(pitchRad), Mth.cos(pitchRad), -Mth.cos(yawRad) * Mth.sin(pitchRad));
		float facingYaw = yawDegrees + this.layoutFaceYaw;
		float facingPitch = this.layoutPitch + this.layoutFacePitch;
		Vector3f base = new Vector3f((float) (this.anchor.x - camera.x), (float) (this.anchor.y - camera.y), (float) (this.anchor.z - camera.z));
		float distanceSetting = VisorSettings.PANEL_DISTANCE.floatValue();
		float scale = VisorSettings.PANEL_SCALE.floatValue() * distanceSetting / 1.62F;

		float distance = distanceSetting + this.panelPush;
		this.panel.radius = VisorSettings.PANEL_SHAPE.is("Curved") ? distanceSetting : 0.0F;
		this.panel.pixelSize = PANEL_PIXEL * scale;
		this.panel.center.set(base).fma(distance + this.layoutReach, forward).fma(-PANEL_DROP, up);
		this.panel.setOrientation(facingYaw, (float) Math.toDegrees(Math.atan2(-PANEL_DROP, distance)) + facingPitch);

		// The browser window shares the home panel's curve and spot, a little higher as it is taller.
		this.browserSurface.radius = this.panel.radius;
		this.browserSurface.pixelSize = this.panel.pixelSize;
		this.browserSurface.right.set(this.panel.right);
		this.browserSurface.up.set(this.panel.up);
		this.browserSurface.forward.set(this.panel.forward);
		this.browserSurface.center.set(this.panel.center).fma(BROWSER_RAISE * this.panel.pixelSize, this.panel.up);
		if (this.cinema > 0.0001F) {
			this.layoutCinema(base);
		}
		VrMode.get().layout(this.lastEye, camera);

		float dockDistance = distanceSetting * DOCK_RATIO + this.panelPush * 0.6F;
		float dockDrop = distanceSetting * DOCK_DROP_RATIO * VisorSettings.PANEL_SCALE.floatValue();
		this.dock.radius = 0.0F;
		this.dock.pixelSize = DOCK_PIXEL * scale;
		this.dock.center.set(base).fma(dockDistance + this.layoutReach, forward).fma(-dockDrop, up);
		this.dock.setOrientation(facingYaw, (float) Math.toDegrees(Math.atan2(-dockDrop, dockDistance)) + facingPitch);
	}

	/**
	 * A fullscreen video: the browser window turns into a big flat screen, straight ahead where the
	 * player looked when it went fullscreen, as tall as the "Fullscreen size" part of the view.
	 */
	private void layoutCinema(Vector3f eye) {
		PanelSurface surface = this.browserSurface;
		float distance = new Vector3f(surface.center).sub(eye).length();
		if (distance < 1.0E-3F) {
			return;
		}
		float t = this.cinema;
		float elevation = -this.cinemaPitch.value();
		this.cinemaFrame.setOrientation(this.cinemaYaw.value(), elevation);
		Vector3f center = direction(this.cinemaYaw.value(), elevation).mul(distance).add(eye);
		surface.center.lerp(center, t);
		float tanHalf = FrameCapture.valid && FrameCapture.PROJECTION.m11() > 0.01F
			? 1.0F / FrameCapture.PROJECTION.m11()
			: (float) Math.tan(Math.toRadians(Minecraft.getInstance().options.fov().get() * 0.5));
		float height = 2.0F * distance * tanHalf * VisorSettings.FULLSCREEN_SIZE.floatValue();
		float curvature = surface.radius > 0.0F ? (1.0F - t) / surface.radius : 0.0F;
		surface.radius = curvature > 1.0E-4F ? 1.0F / curvature : 0.0F;
		surface.pixelSize = Ease.lerp(t, surface.pixelSize, height / BrowserWindow.CINEMA_H);
		surface.forward.lerp(this.cinemaFrame.forward, t).normalize();
		surface.right.lerp(this.cinemaFrame.right, t).normalize();
		surface.up.set(surface.right).cross(surface.forward).normalize();
		surface.right.set(surface.forward).cross(surface.up).normalize();
	}

	private void computePresentation() {
		double t = this.stateTime;
		VisorFx fx = this.fx;
		fx.reset();
		float vignette = VisorSettings.VIGNETTE.floatValue();
		this.logoAlpha = 0.0F;
		switch (this.state) {
			case EQUIPPING -> {
				float panelIn = Ease.window(t, EQ_PANEL_START, EQ_PANEL_END);
				this.panelAlpha = Ease.smooth(Ease.window(t, EQ_PANEL_START, EQ_PANEL_START + 0.35));
				this.panelScale = Ease.lerp(Ease.outBack(panelIn), 0.90F, 1.0F);
				this.panelPush = (1.0F - Ease.outCubic(panelIn)) * 0.22F;
				float dockIn = Ease.window(t, EQ_DOCK_START, EQ_DOCK_END);
				this.dockAlpha = Ease.smooth(Ease.window(t, EQ_DOCK_START, EQ_DOCK_START + 0.3));
				this.dockScale = Ease.lerp(Ease.outBack(dockIn), 0.88F, 1.0F);
				float handsIn = Ease.window(t, EQ_HANDS_START, EQ_HANDS_END);
				this.handsAlpha = Ease.smooth(handsIn);
				this.handsRise = (1.0F - Ease.outCubic(handsIn)) * 0.09F;
				if (this.firstPersonAnimation) {
					fx.lensMerge = t >= EQ_REVEAL_START ? 1.0F : 0.0F;
					fx.lensMask = t >= EQ_LENS_START && t < EQ_REVEAL_END ? 1.0F : 0.0F;
					fx.lensClosure = t < EQ_REVEAL_START
						? Ease.smoother(Ease.window(t, EQ_LENS_START, EQ_LENS_CLOSED))
						: 1.0F - Ease.smoother(Ease.window(t, EQ_REVEAL_START, EQ_REVEAL_END));
					fx.blackout = t < EQ_REVEAL_START
						? Ease.window(t, EQ_LENS_CLOSED - 0.05, EQ_LENS_CLOSED + 0.02)
						: 1.0F - Ease.smooth(Ease.window(t, EQ_REVEAL_START, EQ_REVEAL_START + 0.4));
					fx.vignette = vignette * Ease.smooth(Ease.window(t, EQ_REVEAL_START, EQ_REVEAL_END));
					if (this.bootSequence) {
						this.logoAlpha = Ease.smooth(Ease.window(t, EQ_LOGO_IN, EQ_LOGO_IN + 0.18))
							* (1.0F - Ease.smooth(Ease.window(t, EQ_LOGO_OUT, EQ_LOGO_GONE)));
						this.logoTime = (float) (t - EQ_LOGO_IN);
					}
				}
			}
			case ON -> {
				this.panelAlpha = 1.0F;
				this.panelScale = 1.0F;
				this.panelPush = 0.0F;
				this.dockAlpha = 1.0F;
				this.dockScale = 1.0F;
				this.handsAlpha = 1.0F;
				this.handsRise = 0.0F;
				fx.vignette = this.firstPersonView() ? vignette : 0.0F;
			}
			case REMOVING -> {
				float out = Ease.smooth(Ease.window(t, 0.0, RM_PANEL_END));
				this.panelAlpha = 1.0F - out;
				this.panelScale = Ease.lerp(out, 1.0F, 0.94F);
				this.panelPush = out * 0.10F;
				float dockOut = Ease.smooth(Ease.window(t, 0.0, RM_PANEL_END * 0.8));
				this.dockAlpha = 1.0F - dockOut;
				this.dockScale = Ease.lerp(dockOut, 1.0F, 0.92F);
				float handsOut = Ease.smooth(Ease.window(t, 0.0, 0.18));
				this.handsAlpha = 1.0F - handsOut;
				this.handsRise = handsOut * 0.07F;
				if (this.firstPersonAnimation) {
					fx.vignette = vignette * (1.0F - Ease.window(t, 0.0, 0.3));
					fx.lensMerge = t >= RM_OPEN_START ? 1.0F : 0.0F;
					fx.lensMask = t < RM_OPEN_END ? 1.0F : 0.0F;
					fx.lensClosure = t < RM_OPEN_START
						? Ease.smoother(Ease.window(t, RM_LENS_START, RM_LENS_CLOSED))
						: 1.0F - Ease.smoother(Ease.window(t, RM_OPEN_START, RM_OPEN_END));
					fx.blackout = Ease.window(t, RM_LENS_CLOSED - 0.05, RM_LENS_CLOSED)
						* (1.0F - Ease.smooth(Ease.window(t, RM_OPEN_START, RM_OPEN_START + 0.14)));
				}
			}
			default -> {
			}
		}
		float blend = this.appBlend.value();
		this.homeAlpha = this.panelAlpha * (1.0F - blend);
		this.browserAlpha = this.panelAlpha * blend;
		this.homeScale = this.panelScale * (1.0F - 0.035F * blend);
		this.browserScale = this.panelScale * (0.955F + 0.045F * blend);

		// Watching video: a fullscreen video dims the world and sends the dock away; inside a VR
		// video the windows are gone, and the lenses close and open on the way in and out.
		VrMode vr = VrMode.get();
		if (vr.hidesPanels()) {
			this.homeAlpha = 0.0F;
			this.browserAlpha = 0.0F;
			this.dockAlpha = 0.0F;
		}
		this.dockAlpha *= 1.0F - this.cinema;
		this.browserScale = Ease.lerp(this.cinema, this.browserScale, 1.0F);
		fx.vignette *= 1.0F - 0.85F * Math.max(this.cinema, vr.showsVideo() ? 0.5F : 0.0F);
		this.cinemaDim = this.cinema * VisorSettings.CINEMA_DIM.floatValue();
		this.handsAlpha *= this.pointerShow.value();
		float curtain = vr.curtain();
		if (curtain > 0.001F) {
			fx.lensMask = 1.0F;
			fx.lensMerge = 1.0F;
			fx.lensClosure = Math.max(fx.lensClosure, curtain);
		}
		if (this.logoAlpha > 0.0F) {
			int accent = Theme.accentBright();
			fx.glowR = ((accent >> 16) & 0xFF) / 255.0F;
			fx.glowG = ((accent >> 8) & 0xFF) / 255.0F;
			fx.glowB = (accent & 0xFF) / 255.0F;
			fx.glow = this.logoAlpha * 0.30F;
		}
	}

	private boolean firstPersonView() {
		return Minecraft.getInstance().options.getCameraType().isFirstPerson();
	}

	private void updatePointer(LocalPlayer player, float partialTick) {
		Vec3 camera = FrameCapture.valid ? FrameCapture.cameraPosition : player.getEyePosition(partialTick);
		Vec3 eye = player.getEyePosition(partialTick);
		Vec3 look = player.getViewVector(partialTick);
		Vector3f origin = new Vector3f((float) (eye.x - camera.x), (float) (eye.y - camera.y), (float) (eye.z - camera.z));
		Vector3f direction = new Vector3f((float) look.x, (float) look.y, (float) look.z);
		boolean usable = this.state == State.ON && Minecraft.getInstance().screen == null;
		boolean browser = this.app == App.BROWSER;
		PanelSurface window = browser ? this.browserSurface : this.panel;
		float windowWidth = browser ? BrowserWindow.WIDTH : MainPanel.WIDTH;
		float windowHeight = browser ? BrowserWindow.HEIGHT : MainPanel.HEIGHT;
		float windowAlpha = browser ? this.browserAlpha : this.homeAlpha;
		DrDonutt donut = DrDonutt.get();
		PanelSurface previousEdge = this.edgeSurface;
		boolean wasOnDonut = this.pointerOnDonut;
		this.pointerSurface = null;
		this.pointerTarget = null;
		this.edgeSurface = null;
		this.pointerOnDonut = false;

		VrMode vr = VrMode.get();
		if (vr.hidesPanels()) {
			// Inside a VR video only its controls can be pointed at.
			this.stopMoving(false);
			donut.pointedAt(false);
			this.pointerOnDock = false;
			this.dockInput.active = false;
			this.panelInput.active = false;
			this.browserInput.active = false;
			float[] hit = usable ? vr.raycast(origin, direction) : null;
			vr.input.active = hit != null;
			this.pointerHit = hit != null;
			if (hit != null) {
				vr.input.x = hit[0];
				vr.input.y = hit[1];
				this.pointAt(vr.panel, hit);
			}
			if (!this.mouseDown) {
				vr.input.down = false;
			}
			return;
		}
		vr.input.active = false;

		if (this.moving() && !usable) {
			// A screen opened while holding on: the button release will not come here any more.
			this.stopMoving(false);
			this.mouseDown = false;
		}
		if (this.moving()) {
			// Whatever is being moved keeps the pointer; the windows take no input meanwhile.
			this.pointerOnDock = false;
			this.pointerHit = true;
			this.dockInput.active = false;
			this.panelInput.active = false;
			this.browserInput.active = false;
			Vec3 held = this.movingDonut ? donut.grabPoint() : null;
			if (this.movingWindows && this.grabSurface != null) {
				this.edgeSurface = this.grabSurface;
				this.pointAt(this.grabSurface, new float[]{this.edgeX, this.edgeY});
			} else if (held != null) {
				this.pointAtWorld(held, camera);
			} else {
				this.pointerHit = false;
			}
			donut.pointedAt(this.movingDonut);
			return;
		}

		float[] dockHit = usable && this.dockAlpha > 0.5F && !this.sheetOpen() ? this.dock.raycast(origin, direction) : null;
		boolean onDock = dockHit != null && dockHit[0] >= 0 && dockHit[0] <= Dock.WIDTH && dockHit[1] >= 0 && dockHit[1] <= Dock.HEIGHT;
		float[] windowHit = usable && !onDock && windowAlpha > 0.3F ? window.raycast(origin, direction) : null;
		boolean onWindow = windowHit != null && windowHit[0] >= 0 && windowHit[0] <= windowWidth && windowHit[1] >= 0 && windowHit[1] <= windowHeight;
		// Just outside a window (or the dock) is its edge, where it can be grabbed and moved; further
		// out Doctor Donut can be picked up where he stands.
		boolean free = usable && !onDock && !onWindow && this.cinema < 0.01F;
		if (free && windowHit != null && nearEdge(windowHit, windowWidth, windowHeight, GRAB_EDGE)) {
			this.edgeSurface = window;
			this.edgeX = windowHit[0];
			this.edgeY = windowHit[1];
		} else if (free && dockHit != null && nearEdge(dockHit, Dock.WIDTH, Dock.HEIGHT, GRAB_EDGE_DOCK)) {
			this.edgeSurface = this.dock;
			this.edgeX = dockHit[0];
			this.edgeY = dockHit[1];
		}
		double donutDistance = free && this.edgeSurface == null ? donut.pick(eye, look) : -1.0;
		this.pointerOnDonut = donutDistance >= 0.0;
		donut.pointedAt(this.pointerOnDonut);
		if (VisorSettings.HOVER_SOUNDS.on() && (this.edgeSurface != null && previousEdge == null || this.pointerOnDonut && !wasOnDonut)) {
			VisorSounds.play(VisorSounds.HOVER, 0.55F, 1.0F);
		}

		this.pointerOnDock = onDock;
		this.pointerHit = onDock || onWindow || this.edgeSurface != null || this.pointerOnDonut;
		this.dockInput.active = onDock;
		this.panelInput.active = onWindow && !browser;
		this.browserInput.active = onWindow && browser;
		if (this.edgeSurface != null) {
			this.pointAt(this.edgeSurface, new float[]{this.edgeX, this.edgeY});
		} else if (this.pointerOnDonut) {
			this.pointAtWorld(eye.add(look.scale(donutDistance)), camera);
		}
		if (onDock) {
			this.dockInput.x = dockHit[0];
			this.dockInput.y = dockHit[1];
			this.pointAt(this.dock, dockHit);
		} else if (onWindow) {
			UiInput input = browser ? this.browserInput : this.panelInput;
			input.x = windowHit[0];
			input.y = windowHit[1];
			this.pointAt(window, windowHit);
		}
		if (!this.mouseDown) {
			this.panelInput.down = false;
			this.browserInput.down = false;
			this.dockInput.down = false;
		}
	}

	private void pointAt(PanelSurface surface, float[] hit) {
		this.pointerSurface = surface;
		this.pointerU = hit[0];
		this.pointerV = hit[1];
		surface.map(hit[0], hit[1], this.pointerWorld);
	}

	/** Points at a spot in the world (on Doctor Donut) instead of on a window. */
	private void pointAtWorld(Vec3 point, Vec3 camera) {
		this.pointerTarget = point;
		this.pointerWorld.set((float) (point.x - camera.x), (float) (point.y - camera.y), (float) (point.z - camera.z));
	}
}
