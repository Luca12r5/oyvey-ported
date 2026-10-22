package dev.visor.vr;

import dev.visor.VisorController;
import dev.visor.browser.BrowserSession;
import dev.visor.browser.MediaState;
import dev.visor.render.BrowserTexture;
import dev.visor.render.PanelSurface;
import dev.visor.setting.VisorSettings;
import dev.visor.sound.VisorSounds;
import dev.visor.ui.Ease;
import dev.visor.ui.Spring;
import dev.visor.ui.UiInput;
import dev.visor.ui.VrControls;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * Watching a video in VR: the video replaces the world all around the player. The headset's
 * lenses close like an iris, the browser streams the raw video (see
 * {@link BrowserSession#setVrCapture}), and the lenses open again inside the video. Looking
 * around is looking around in the video; a click brings up the controls below the view.
 */
public final class VrMode {
	public enum Phase { OFF, ENTERING, ON, LEAVING, REVEAL }

	private static final VrMode INSTANCE = new VrMode();
	/** Seconds the lenses take to close before the video, to open onto it, and the same on the way out. */
	private static final double CLOSE = 0.32;
	private static final double OPEN = 0.65;
	private static final double LEAVE_CLOSE = 0.26;
	private static final double LEAVE_OPEN = 0.5;
	/** The lenses open after this long even without a picture (a dark room with a spinner). */
	private static final double WAIT_LIMIT = 6.0;
	/** Seconds the controls stay up without the pointer on them while the video plays. */
	private static final double PANEL_LINGER = 4.0;
	private static final float PANEL_DISTANCE = 1.3F;
	private static final float PANEL_PIXEL = 0.00142F;

	public final PanelSurface panel = new PanelSurface(VrControls.WIDTH, VrControls.HEIGHT);
	public final UiInput input = new UiInput();
	public final VrControls controls = new VrControls();

	private Phase phase = Phase.OFF;
	private double time;
	private VrFormat format = VrFormat.SPHERE;
	private float yaw;
	private boolean leavePageFullscreen;
	private final Spring panelShown = new Spring(0.0F, 12.0F);
	private boolean panelWanted;
	private double panelIdle;
	private float panelYaw;
	private float panelPitch;
	private boolean wasPaused;
	private final Set<String> opened = new HashSet<>();
	private boolean lastFullscreen;
	private long lastLook;
	private BrowserSession.@Nullable VrCapture wanted;

	private VrMode() {
	}

	public static VrMode get() {
		return INSTANCE;
	}

	public Phase phase() {
		return this.phase;
	}

	public boolean active() {
		return this.phase != Phase.OFF;
	}

	/** The video is drawn around the player instead of the world. */
	public boolean showsVideo() {
		return this.phase == Phase.ON || this.phase == Phase.LEAVING;
	}

	/** The headset's windows are out of sight: inside the video, or the lenses closed on the way in. */
	public boolean hidesPanels() {
		return this.showsVideo() || this.phase == Phase.ENTERING && this.time >= CLOSE;
	}

	/** Waiting in the dark for the first picture of the video. */
	public boolean waiting() {
		return this.phase == Phase.ENTERING && this.time >= CLOSE || this.phase == Phase.ON && BrowserTexture.get().vr() == null;
	}

	/** How far the lenses are closed (0 open, 1 black), for the lens effect. */
	public float curtain() {
		return switch (this.phase) {
			case OFF -> 0.0F;
			case ENTERING -> Ease.smooth((float) (this.time / CLOSE));
			case ON -> this.time < OPEN ? 1.0F - Ease.smoother((float) (this.time / OPEN)) : 0.0F;
			case LEAVING -> Ease.smooth((float) (this.time / LEAVE_CLOSE));
			case REVEAL -> 1.0F - Ease.smoother((float) (this.time / LEAVE_OPEN));
		};
	}

	public VrFormat format() {
		return this.format;
	}

	/** Minecraft yaw (degrees) the middle of the video faces. */
	public float yaw() {
		return this.yaw;
	}

	public float panelAlpha() {
		return this.panelShown.value();
	}

	/** The page plays a VR video the browser window can offer to open in VR. */
	public boolean offers(@Nullable MediaState media) {
		return this.phase == Phase.OFF && media != null && media.vr() && !media.ad();
	}

	// ------------------------------------------------------------------------------------------
	// Commands

	public void enter(VrFormat format) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		this.format = format;
		if (this.phase == Phase.ENTERING || this.phase == Phase.ON) {
			return;
		}
		MediaState media = BrowserSession.get().media();
		if (media != null) {
			this.opened.add(media.id());
		}
		this.phase = Phase.ENTERING;
		this.time = 0.0;
		this.yaw = player.getViewYRot(1.0F);
		this.leavePageFullscreen = BrowserSession.get().pageFullscreen();
		this.input.clear();
		this.showPanel(player);
		this.wasPaused = media == null || media.paused();
		VisorController.get().stopKeyboardCapture();
		VisorSounds.play(VisorSounds.PANEL_OPEN, 0.7F);
		this.requestCapture(media);
	}

	/** Back to the browser, through the closing and opening lenses. */
	public void exit() {
		if (this.phase == Phase.ENTERING || this.phase == Phase.ON) {
			this.phase = Phase.LEAVING;
			this.time = 0.0;
			this.input.clear();
			VisorSounds.play(VisorSounds.PANEL_CLOSE, 0.7F);
		}
	}

	/** Leave at once (the headset comes off, the browser closed). */
	public void exitNow() {
		if (this.phase == Phase.OFF) {
			return;
		}
		this.phase = Phase.OFF;
		this.input.clear();
		this.panelShown.snap(0.0F);
		this.panelWanted = false;
		this.finish();
	}

	public void setFormat(VrFormat format) {
		this.format = format;
	}

	/** The middle of the video goes where the player looks now. */
	public void recenter() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null && this.active()) {
			this.yaw = player.getViewYRot(1.0F);
			this.showPanel(player);
		}
	}

	private void finish() {
		BrowserSession session = BrowserSession.get();
		session.setVrCapture(null);
		this.wanted = null;
		BrowserTexture.get().clearVr();
		if (this.leavePageFullscreen && session.pageFullscreen()) {
			session.exitFullscreen();
		}
		this.leavePageFullscreen = false;
		// The player may have turned all the way round inside the video: the browser comes back in front.
		VisorController.get().recenter(false);
	}

	private void showPanel(LocalPlayer player) {
		this.panelWanted = true;
		this.panelIdle = 0.0;
		this.panelYaw = player.getViewYRot(1.0F);
		// A little below where the player looks, so the video stays in view.
		this.panelPitch = Mth.clamp(player.getViewXRot(1.0F) + 20.0F, -35.0F, 65.0F);
	}

	private void requestCapture(@Nullable MediaState media) {
		// Ads (and anything the site shows instead of the video) are flat.
		VrFormat shown = media != null && media.ad() ? VrFormat.FLAT : this.format;
		boolean high = VisorSettings.VR_QUALITY.is("High");
		int[] size = shown.captureSize(high, media != null ? media.width() : 0, media != null ? media.height() : 0);
		this.wanted = new BrowserSession.VrCapture(shown.code, shown.region, high ? "high" : "standard", size[0], size[1]);
		BrowserSession.get().setVrCapture(this.wanted);
	}

	// ------------------------------------------------------------------------------------------
	// Input (render thread, from the controller)

	/** @return true when the press was used */
	public boolean onMouseButton(int button, boolean pressed) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || !this.showsVideo()) {
			return true;
		}
		boolean onPanel = this.input.active && this.panelShown.value() > 0.5F;
		switch (button) {
			case 0 -> {
				if (pressed && !onPanel) {
					// A click into the video brings up the controls, or sends them away.
					if (this.panelWanted) {
						this.panelWanted = false;
					} else {
						this.showPanel(player);
						VisorSounds.play(VisorSounds.MENU_OPEN, 0.5F);
					}
					return true;
				}
				this.input.down = pressed && onPanel;
				if (pressed) {
					this.input.pressed = true;
				} else {
					this.input.released = true;
				}
			}
			case 1 -> {
				if (pressed) {
					if (this.panelWanted) {
						this.panelWanted = false;
					} else {
						this.showPanel(player);
						VisorSounds.play(VisorSounds.MENU_OPEN, 0.5F);
					}
				}
			}
			case 2 -> {
				if (pressed) {
					this.recenter();
					VisorSounds.play(VisorSounds.RECENTER, 0.6F);
				}
			}
			default -> {
			}
		}
		return true;
	}

	/** Video keys while in VR. @return true when the key was used */
	public boolean onKey(int key, int action) {
		if (!this.showsVideo()) {
			return false;
		}
		if (action != 1 && action != 2) {
			return isVideoKey(key);
		}
		BrowserSession session = BrowserSession.get();
		switch (key) {
			case GLFW.GLFW_KEY_SPACE, GLFW.GLFW_KEY_K -> {
				if (action == 1) {
					session.mediaCommand("toggle", 0, 0);
				}
			}
			case GLFW.GLFW_KEY_LEFT -> session.mediaCommand("seek", -5, 0);
			case GLFW.GLFW_KEY_RIGHT -> session.mediaCommand("seek", 5, 0);
			case GLFW.GLFW_KEY_J -> session.mediaCommand("seek", -10, 0);
			case GLFW.GLFW_KEY_L -> session.mediaCommand("seek", 10, 0);
			case GLFW.GLFW_KEY_M -> {
				if (action == 1) {
					session.mediaCommand("mute", 0, 0);
				}
			}
			default -> {
				return false;
			}
		}
		return true;
	}

	private static boolean isVideoKey(int key) {
		return key == GLFW.GLFW_KEY_SPACE || key == GLFW.GLFW_KEY_K || key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_RIGHT
			|| key == GLFW.GLFW_KEY_J || key == GLFW.GLFW_KEY_L || key == GLFW.GLFW_KEY_M;
	}

	/** Pointer ray against the controls; layout coordinates of the hit, or null. */
	public float @Nullable [] raycast(Vector3f origin, Vector3f direction) {
		if (!this.showsVideo() || this.panelShown.value() < 0.5F) {
			return null;
		}
		float[] hit = this.panel.raycast(origin, direction);
		if (hit == null || hit[0] < 0 || hit[0] > VrControls.WIDTH || hit[1] < 0 || hit[1] > VrControls.HEIGHT) {
			return null;
		}
		return hit;
	}

	/** Places the controls (camera relative), facing the player's eyes. */
	public void layout(Vec3 eye, Vec3 camera) {
		float yawRad = this.panelYaw * Mth.DEG_TO_RAD;
		float pitchRad = this.panelPitch * Mth.DEG_TO_RAD;
		float horizontal = Mth.cos(pitchRad);
		Vector3f forward = new Vector3f(-Mth.sin(yawRad) * horizontal, -Mth.sin(pitchRad), Mth.cos(yawRad) * horizontal);
		float shown = this.panelShown.value();
		// Rises into place a little as it appears.
		float distance = PANEL_DISTANCE + (1.0F - shown) * 0.12F;
		this.panel.radius = 0.0F;
		this.panel.pixelSize = PANEL_PIXEL;
		this.panel.center.set((float) (eye.x - camera.x), (float) (eye.y - camera.y), (float) (eye.z - camera.z)).fma(distance, forward);
		this.panel.setOrientation(this.panelYaw, -this.panelPitch);
	}

	// ------------------------------------------------------------------------------------------
	// Per frame

	/**
	 * @param usable   the headset is on with a web page in front
	 * @param noScreen no Minecraft screen (chat, inventory...) is open
	 */
	public void update(float dt, LocalPlayer player, boolean usable, boolean noScreen) {
		BrowserSession session = BrowserSession.get();
		MediaState media = session.media();
		this.time += dt;
		boolean fullscreen = session.pageFullscreen();
		boolean fullscreenNow = fullscreen && !this.lastFullscreen;
		this.lastFullscreen = fullscreen;
		switch (this.phase) {
			case OFF -> {
				this.panelShown.snap(0.0F);
				if (usable && noScreen && media != null && !media.ad()) {
					if (fullscreenNow && media.vr()) {
						// Fullscreen on a VR video: straight into VR.
						this.enter(VrFormat.of(media.format()));
						this.leavePageFullscreen = true;
					} else if (VisorSettings.VR_VIDEOS.is(VisorSettings.VR_AUTOMATIC) && media.certainty() == MediaState.SURE
						&& !media.format().isEmpty() && !media.paused() && media.time() > 0.5 && !this.opened.contains(media.id())) {
						this.enter(VrFormat.of(media.format()));
					}
				}
			}
			case ENTERING -> {
				this.requestCapture(media);
				session.markVisible();
				boolean picture = this.wanted != null && this.wanted.equals(BrowserTexture.get().capture());
				if (!session.ready() || !usable) {
					this.exitNow();
				} else if (this.time >= CLOSE && (picture || this.time >= WAIT_LIMIT)) {
					this.phase = Phase.ON;
					this.time = 0.0;
				}
			}
			case ON -> {
				this.requestCapture(media);
				session.markVisible();
				if (!session.ready() || !usable) {
					this.exit();
				} else if (player.hurtTime > 0) {
					// Getting hurt pulls the player out of the video, to see what is going on.
					VisorSounds.play(VisorSounds.DENY, 0.8F);
					this.exit();
				}
				this.followHead(player, media);
			}
			case LEAVING -> {
				session.markVisible();
				if (this.time >= LEAVE_CLOSE) {
					this.phase = Phase.REVEAL;
					this.time = 0.0;
					this.panelShown.snap(0.0F);
					this.finish();
				}
			}
			case REVEAL -> {
				if (this.time >= LEAVE_OPEN) {
					this.phase = Phase.OFF;
				}
			}
		}
		if (this.showsVideo()) {
			boolean paused = media != null && (media.paused() || media.ended());
			if (paused && !this.wasPaused && !this.panelWanted) {
				this.showPanel(player);
			}
			this.wasPaused = paused;
			this.panelIdle = this.input.active ? 0.0 : this.panelIdle + dt;
			if (this.panelWanted && !paused && this.panelIdle > PANEL_LINGER) {
				this.panelWanted = false;
			}
			this.panelShown.target(this.panelWanted ? 1.0F : 0.0F).update(dt);
		}
	}

	/** YouTube's spherical sound follows its own camera: keep that camera where the player looks. */
	private void followHead(LocalPlayer player, @Nullable MediaState media) {
		long now = System.currentTimeMillis();
		if (media == null || !media.youtube() || media.ad() || this.format.projection == VrFormat.PROJECTION_FLAT || now - this.lastLook < 100L) {
			return;
		}
		this.lastLook = now;
		float relative = Mth.wrapDegrees(player.getViewYRot(1.0F) - this.yaw);
		BrowserSession.get().mediaCommand("look", -relative, -player.getViewXRot(1.0F));
	}
}
