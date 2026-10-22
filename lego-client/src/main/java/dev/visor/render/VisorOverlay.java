package dev.visor.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.textures.GpuTextureView;
import dev.visor.VisorController;
import dev.visor.browser.BrowserSession;
import dev.visor.donut.DrDonutt;
import dev.visor.render.text.FontAtlas;
import dev.visor.ui.BootScreen;
import dev.visor.ui.BrowserWindow;
import dev.visor.ui.Dock;
import dev.visor.ui.MainPanel;
import dev.visor.setting.VisorSettings;
import dev.visor.ui.Spring;
import dev.visor.ui.Theme;
import dev.visor.ui.UiInput;
import dev.visor.ui.VrControls;
import dev.visor.vr.VrMode;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Draws everything the headset adds on top of the world, once per frame, right after the world
 * (and before Minecraft's own HUD): the curved panel, the dock, the hands with their pointer ray,
 * the lens effects and the boot logo.
 */
public final class VisorOverlay {
	private static final VisorOverlay INSTANCE = new VisorOverlay();
	private static final Matrix4f IDENTITY = new Matrix4f();
	private static final float HAND_FOV = 70.0F;

	private UiCanvas canvas;
	private long gpuFrame = -1;
	private final GhostHands hands = new GhostHands();
	private final GhostHands.HandState left = new GhostHands.HandState();
	private final GhostHands.HandState right = new GhostHands.HandState();

	// Hand motion state.
	private float smoothYaw;
	private float smoothPitch;
	private boolean motionInit;
	private final Spring pointSpring = new Spring(0.0F, 10.0F);
	private final Spring pinchSpring = new Spring(0.0F, 22.0F);
	private final Spring leftPinchSpring = new Spring(0.0F, 20.0F);
	private final Spring rayAlpha = new Spring(0.0F, 12.0F);
	private final Spring cursorPress = new Spring(0.0F, 26.0F);
	private final Spring cursorHover = new Spring(0.0F, 16.0F);

	// Grabbing things: the glowing edge of a window, and the marks on and under Doctor Donut.
	private final Spring homeEdge = new Spring(0.0F, 14.0F);
	private final Spring browserEdge = new Spring(0.0F, 14.0F);
	private final Spring dockEdge = new Spring(0.0F, 14.0F);
	private final Spring homeHint = new Spring(0.0F, 10.0F);
	private final Spring browserHint = new Spring(0.0F, 10.0F);
	private final Spring donutMark = new Spring(0.0F, 14.0F);
	private final PanelSurface ground = new PanelSurface(200.0F, 200.0F);
	private final PanelSurface tag = new PanelSurface(60.0F, 60.0F);
	private final Spring vrWaiting = new Spring(0.0F, 4.0F);

	private VisorOverlay() {
		this.left.left = true;
	}

	public static VisorOverlay get() {
		return INSTANCE;
	}

	private boolean prepare() {
		Minecraft minecraft = Minecraft.getInstance();
		VisorController controller = VisorController.get();
		if (controller.state() == VisorController.State.OFF || minecraft.level == null || minecraft.player == null) {
			this.motionInit = false;
			return false;
		}
		if (!UiResources.ready() || !FrameCapture.valid) {
			return false;
		}
		if (this.canvas == null) {
			this.canvas = new UiCanvas();
		}
		if (this.gpuFrame != controller.frameIndex()) {
			this.gpuFrame = controller.frameIndex();
			VisorGpu.get().endFrame();
			controller.syncCamera();
		}
		if (BrowserSession.get().running()) {
			BrowserTexture.get().update(controller.frameIndex());
		}
		return true;
	}

	/**
	 * Panels that belong to the world (third person, or "stay in place" in first person) are
	 * drawn right after the world, while its depth buffer is intact, so blocks and the player's
	 * body can hide them. Panels that follow the player in first person float on top instead,
	 * like the interface of a real headset.
	 */
	private static boolean panelsInWorld(Minecraft minecraft) {
		return !minecraft.options.getCameraType().isFirstPerson() || !VisorSettings.follows();
	}

	/** Called right after the world is drawn, while its depth buffer is still intact. */
	public void renderWorldPhase() {
		Minecraft minecraft = Minecraft.getInstance();
		if (!this.prepare()) {
			return;
		}
		RenderTarget main = minecraft.getMainRenderTarget();
		VisorController controller = VisorController.get();
		if (VrMode.get().showsVideo()) {
			return;
		}
		// Before the panels: they write no depth, so whatever comes after them would show through.
		this.renderDonutMarks(minecraft, controller, main.getColorTextureView(), main.getDepthTextureView());
		// Front-facing third person looks at the back of the panels, right in front of the face;
		// leave them out there. A fullscreen video is drawn on top of everything instead.
		if (panelsInWorld(minecraft) && !minecraft.options.getCameraType().isMirrored() && controller.cinema() < 0.001F) {
			this.renderPanels(minecraft, controller, main.getColorTextureView(), main.getDepthTextureView());
		}
	}

	/** Called after the world and its post effects, before Minecraft's HUD. */
	public void render() {
		if (!this.prepare()) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		VisorController controller = VisorController.get();
		VisorGpu gpu = VisorGpu.get();
		RenderTarget main = minecraft.getMainRenderTarget();
		GpuTextureView target = main.getColorTextureView();
		int width = minecraft.getWindow().getWidth();
		int height = minecraft.getWindow().getHeight();
		float dt = controller.frameDelta();
		float time = (float) controller.clock();
		float visibility = controller.visibility;
		boolean hideGui = minecraft.options.hideGui;
		boolean firstPerson = minecraft.options.getCameraType().isFirstPerson();
		VrMode vr = VrMode.get();

		// 0. A VR video takes the place of the world; a fullscreen video dims it.
		if (vr.showsVideo()) {
			VrView.get().render(target, width, height, time, 1.0F, vr.yaw());
		} else if (controller.cinemaDim * visibility > 0.001F) {
			UiCanvas c = this.canvas;
			c.begin(ScreenSurface.INSTANCE);
			c.rect(0, 0, width, height, Theme.alpha(0xFF000000, controller.cinemaDim * visibility));
			c.flush(target, IDENTITY, gpu.screenProjection.getBuffer(width, height), time, 1.0F);
		}

		// 1. Panels that follow the player float "holographically" on top of everything; the others
		// were drawn in the world phase. A fullscreen video always floats on top.
		if (!panelsInWorld(minecraft) || controller.cinema() >= 0.001F) {
			this.renderPanels(minecraft, controller, target, null);
		}
		if (vr.showsVideo()) {
			this.renderVrControls(minecraft, controller, vr, target);
		}

		// 2. Hands and the pointer ray (first person only).
		if (!hideGui && firstPerson && VisorSettings.SHOW_HANDS.on() && controller.handsAlpha * visibility > 0.001F) {
			this.renderHands(minecraft, controller, target, width, height, dt, time, visibility);
		} else {
			this.motionInit = false;
		}

		// 3. Lens effects.
		VisorFx fx = controller.fx;
		if (visibility < 1.0F) {
			fx.vignette *= visibility;
		}
		fx.render(target, width, height, time);

		// While the VR video is on its way: a spinner in the dark.
		float waiting = this.vrWaiting.target(vr.waiting() ? 1.0F : 0.0F).update(dt) * visibility;
		if (waiting > 0.01F && !hideGui) {
			UiCanvas c = this.canvas;
			c.begin(ScreenSurface.INSTANCE);
			drawWaiting(c, width, height, time, waiting);
			c.flush(target, IDENTITY, gpu.screenProjection.getBuffer(width, height), time, 1.0F);
		}

		// 4. Boot logo in screen space.
		if (controller.logoAlpha > 0.001F) {
			UiCanvas c = this.canvas;
			c.begin(ScreenSurface.INSTANCE);
			BootScreen.render(c, width, height, controller.logoAlpha, controller.logoTime, controller.mainPanel.clientName);
			c.flush(target, IDENTITY, gpu.screenProjection.getBuffer(width, height), time, 1.0F);
		}
	}

	private void renderPanels(Minecraft minecraft, VisorController controller, GpuTextureView target, GpuTextureView depth) {
		float dt = controller.frameDelta();
		float time = (float) controller.clock();
		float visibility = controller.visibility;
		if (!minecraft.options.hideGui && visibility > 0.001F) {
			UiCanvas c = this.canvas;
			if (controller.homeAlpha > 0.001F) {
				c.begin(controller.panel);
				c.pushAlpha(controller.homeAlpha * visibility);
				c.scale(controller.homeScale, MainPanel.WIDTH * 0.5F, MainPanel.HEIGHT * 0.5F);
				float edge = this.homeEdge.target(controller.edgeSurface == controller.panel ? 1.0F : 0.0F).update(dt);
				float hint = this.homeHint.target(controller.panelInput.active ? 1.0F : 0.0F).update(dt);
				edgeGlow(c, MainPanel.WIDTH, MainPanel.HEIGHT, Theme.PANEL_RADIUS, edge);
				controller.panelInput.beginFrame();
				controller.mainPanel.render(c, controller.panelInput, dt, time);
				if (controller.panelInput.active) {
					this.drawCursor(c, controller.panelInput.x, controller.panelInput.y, controller, dt, controller.panelInput.hovered() != null);
				}
				edgeFrame(c, controller, controller.panel, MainPanel.WIDTH, MainPanel.HEIGHT, Theme.PANEL_RADIUS, edge, hint, true);
				controller.panelInput.endFrame();
				c.popAlpha();
			}
			if (controller.browserAlpha > 0.001F) {
				c.begin(controller.browserSurface);
				c.pushAlpha(controller.browserAlpha * visibility);
				c.scale(controller.browserScale, BrowserWindow.WIDTH * 0.5F, BrowserWindow.HEIGHT * 0.5F);
				// As a cinema screen the window has no frame to grab.
				float chrome = 1.0F - controller.cinema();
				float edge = this.browserEdge.target(controller.edgeSurface == controller.browserSurface ? 1.0F : 0.0F).update(dt) * chrome;
				float hint = this.browserHint.target(controller.browserInput.active ? 1.0F : 0.0F).update(dt) * chrome;
				edgeGlow(c, BrowserWindow.WIDTH, BrowserWindow.HEIGHT, Theme.PANEL_RADIUS, edge);
				controller.browserInput.beginFrame();
				controller.browserUi.render(c, controller.browserInput, dt, time);
				if (controller.browserInput.active) {
					c.pushAlpha(controller.pointerShown());
					this.drawCursor(c, controller.browserInput.x, controller.browserInput.y, controller, dt, controller.browserInput.hovered() != null);
					c.popAlpha();
				}
				c.pushAlpha(chrome);
				edgeFrame(c, controller, controller.browserSurface, BrowserWindow.WIDTH, BrowserWindow.HEIGHT, Theme.PANEL_RADIUS, edge, hint, true);
				c.popAlpha();
				controller.browserInput.endFrame();
				c.popAlpha();
			}
			if (controller.dockAlpha > 0.001F) {
				// Same projection as the panel, so it joins the same flush; begin() resets the 2D state.
				c.begin(controller.dock);
				c.push();
				c.pushAlpha(controller.dockAlpha * visibility);
				c.scale(controller.dockScale, Dock.WIDTH * 0.5F, Dock.HEIGHT * 0.5F);
				float edge = this.dockEdge.target(controller.edgeSurface == controller.dock ? 1.0F : 0.0F).update(dt);
				edgeGlow(c, Dock.WIDTH, Dock.HEIGHT, 20.0F, edge);
				controller.dockInput.beginFrame();
				controller.dockUi.render(c, controller.dockInput, dt, controller.mainPanel);
				if (controller.dockInput.active) {
					this.drawCursor(c, controller.dockInput.x, controller.dockInput.y, controller, dt, controller.dockInput.hovered() != null);
				}
				edgeFrame(c, controller, controller.dock, Dock.WIDTH, Dock.HEIGHT, 20.0F, edge, 0.0F, false);
				controller.dockInput.endFrame();
				c.popAlpha();
				c.pop();
			}
			GpuBufferSlice projection = VisorGpu.get().worldProjection.getBuffer(FrameCapture.PROJECTION);
			c.flush(target, depth, FrameCapture.VIEW, projection, time, 0.92F, true);
		}
	}

	/** The controls of a VR video, below the view while they are up. */
	private void renderVrControls(Minecraft minecraft, VisorController controller, VrMode vr, GpuTextureView target) {
		float dt = controller.frameDelta();
		float time = (float) controller.clock();
		float alpha = vr.panelAlpha() * controller.visibility;
		vr.input.beginFrame();
		if (alpha > 0.001F && !minecraft.options.hideGui) {
			UiCanvas c = this.canvas;
			c.begin(vr.panel);
			c.pushAlpha(alpha);
			c.scale(0.96F + 0.04F * vr.panelAlpha(), VrControls.WIDTH * 0.5F, VrControls.HEIGHT * 0.5F);
			vr.controls.render(c, alpha > 0.5F ? vr.input : UiInput.INERT, dt, time, vr, BrowserSession.get().media());
			if (vr.input.active) {
				this.drawCursor(c, vr.input.x, vr.input.y, controller, dt, vr.input.hovered() != null);
			}
			c.popAlpha();
			GpuBufferSlice projection = VisorGpu.get().worldProjection.getBuffer(FrameCapture.PROJECTION);
			c.flush(target, null, FrameCapture.VIEW, projection, time, 0.92F, true);
		}
		vr.input.endFrame();
	}

	/** A spinner and a line of text in the middle of the dark, while a VR video starts. */
	private static void drawWaiting(UiCanvas c, int width, int height, float time, float alpha) {
		FontAtlas f = UiResources.atlas();
		float scale = Math.max(1.0F, height / 1080.0F) * 1.2F;
		float cx = width * 0.5F;
		float cy = height * 0.5F;
		c.pushAlpha(alpha);
		float start = (time * 5.5F) % 6.2831855F;
		c.arc(cx, cy - 18 * scale, 20 * scale, 4 * scale, start, 4.2F, Theme.accentBright());
		c.textCentered(f.medium, "Loading the video…", cx, cy + 30 * scale, 16 * scale, Theme.TEXT_SECONDARY);
		c.popAlpha();
	}

	/** Soft glow behind a window whose edge the pointer is on; drawn first, so only its rim shows. */
	private static void edgeGlow(UiCanvas c, float width, float height, float radius, float edge) {
		if (edge > 0.01F) {
			c.shadow(-3, -3, width + 6, height + 6, radius + 3, 22, Theme.alpha(Theme.accentBright(), 0.40F * edge));
		}
	}

	/**
	 * What tells a window can be moved: a ring round it while the pointer is on its edge, the grab
	 * bar below it (as on a real headset) and an open hand as cursor, which closes while moving it.
	 */
	private static void edgeFrame(
		UiCanvas c, VisorController controller, PanelSurface surface, float width, float height, float radius, float edge, float hint, boolean bar
	) {
		boolean holding = controller.movingWindows() && controller.edgeSurface == surface;
		if (edge > 0.01F) {
			c.roundRectStroke(-6, -6, width + 12, height + 12, radius + 6, 2.5F, Theme.alpha(Theme.accentBright(), 0.85F * edge));
		}
		if (bar) {
			float barW = 168.0F + 40.0F * edge;
			float barH = 10.0F;
			float shown = 0.16F + 0.22F * hint + 0.62F * edge;
			int color = Theme.mix(Theme.alpha(Theme.TEXT, shown), Theme.alpha(Theme.accentBright(), shown), holding ? 1.0F : edge * 0.35F);
			c.roundRect((width - barW) * 0.5F, height + 18.0F, barW, barH, barH * 0.5F, color);
		}
		if (controller.edgeSurface == surface) {
			drawGrabCursor(c, controller.edgeX, controller.edgeY, holding);
		}
	}

	/** Cursor over something that can be picked up: an open hand, closed while holding it. */
	private static void drawGrabCursor(UiCanvas c, float x, float y, boolean holding) {
		float r = holding ? 15.0F : 17.0F;
		c.shadow(x - r, y - r + 2.0F, r * 2.0F, r * 2.0F, r, 10, 0x66000000);
		c.circle(x, y, r, Theme.TEXT);
		c.circle(x, y, r - 2.4F, Theme.cursor());
		c.icon(holding ? "hand-grabbing" : "hand-palm", true, x, y, r * 1.15F, Theme.TEXT);
	}

	/** Doctor Donut can be picked up: a ring on the ground round his feet, and the hand on him. */
	private void renderDonutMarks(Minecraft minecraft, VisorController controller, GpuTextureView target, GpuTextureView depth) {
		boolean holding = controller.movingDonut();
		float shown = this.donutMark.target(controller.pointerOnDonut || holding ? 1.0F : 0.0F).update(controller.frameDelta()) * controller.visibility;
		Vec3 feet = DrDonutt.get().feet();
		if (shown < 0.01F || feet == null || minecraft.options.hideGui) {
			return;
		}
		Vec3 camera = FrameCapture.cameraPosition;
		UiCanvas c = this.canvas;
		int accent = Theme.accentBright();
		float radius = (float) DrDonutt.get().radius() * 1.4F + 0.15F;
		this.ground.radius = 0.0F;
		this.ground.pixelSize = radius / 90.0F;
		this.ground.center.set((float) (feet.x - camera.x), (float) (feet.y + 0.03 - camera.y), (float) (feet.z - camera.z));
		this.ground.setOrientation(0.0F, -90.0F);
		c.begin(this.ground);
		c.pushAlpha(shown);
		c.shadow(10, 10, 180, 180, 90, 16, Theme.alpha(accent, 0.30F));
		c.circleStroke(100, 100, 90, holding ? 7.0F : 5.0F, Theme.alpha(accent, 0.95F));
		float pulse = 0.5F + 0.5F * Mth.sin((float) controller.clock() * 4.0F);
		c.circleStroke(100, 100, 70 + pulse * 8.0F, 2.5F, Theme.alpha(Theme.TEXT, 0.35F + 0.25F * pulse));
		c.popAlpha();
		if (controller.pointerHit) {
			// The hand sits on him, turned towards the camera and a little in front of him.
			Vector3f point = new Vector3f(controller.pointerWorld);
			float distance = point.length();
			if (distance > 0.05F) {
				Vector3f toward = new Vector3f(point).div(distance);
				this.tag.radius = 0.0F;
				this.tag.pixelSize = distance * 0.00105F;
				this.tag.center.set(point).fma(-Math.min(0.12F, distance * 0.05F), toward);
				this.tag.setOrientation((float) Math.toDegrees(Math.atan2(-toward.x, toward.z)), (float) Math.toDegrees(Math.asin(Mth.clamp(toward.y, -1.0F, 1.0F))));
				c.begin(this.tag);
				c.pushAlpha(shown);
				drawGrabCursor(c, 30.0F, 30.0F, holding);
				c.popAlpha();
			}
		}
		GpuBufferSlice projection = VisorGpu.get().worldProjection.getBuffer(FrameCapture.PROJECTION);
		c.flush(target, depth, FrameCapture.VIEW, projection, (float) controller.clock(), 0.92F, false);
	}

	/** Quest style cursor: blue dot in a white ring, sitting on the panel surface. */
	private void drawCursor(UiCanvas c, float x, float y, VisorController controller, float dt, boolean overTarget) {
		float press = this.cursorPress.target(controller.mouseDown() ? 1.0F : 0.0F).update(dt);
		float hover = this.cursorHover.target(overTarget ? 1.0F : 0.0F).update(dt);
		float r = (8.5F + hover * 1.5F) * (1.0F - press * 0.18F);
		c.shadow(x - r, y - r + 1.5F, r * 2, r * 2, r, 6, 0x55000000);
		c.circle(x, y, r, Theme.TEXT);
		c.circle(x, y, r - 2.4F + press * 1.2F, Theme.cursor());
	}

	// ------------------------------------------------------------------------------------------

	private void renderHands(Minecraft minecraft, VisorController controller, GpuTextureView target, int width, int height, float dt, float time, float visibility) {
		LocalPlayer player = minecraft.player;
		Camera camera = minecraft.gameRenderer.getMainCamera();
		float yaw = camera.yRot();
		float pitch = camera.xRot();
		if (!this.motionInit) {
			this.smoothYaw = yaw;
			this.smoothPitch = pitch;
			this.motionInit = true;
		}
		// Hands trail the head a little when turning, then catch up.
		float follow = 1.0F - (float) Math.exp(-dt * 11.0F);
		this.smoothYaw += Mth.wrapDegrees(yaw - this.smoothYaw) * follow;
		this.smoothPitch += (pitch - this.smoothPitch) * follow;
		float lagYaw = Mth.clamp(Mth.wrapDegrees(yaw - this.smoothYaw), -14.0F, 14.0F) * Mth.DEG_TO_RAD;
		float lagPitch = Mth.clamp(pitch - this.smoothPitch, -12.0F, 12.0F) * Mth.DEG_TO_RAD;
		Matrix3f lag = new Matrix3f().rotateY(lagYaw * 0.55F).rotateX(lagPitch * 0.55F);

		// Walking bob, subtler than the camera's.
		float partial = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);
		float walk = player.avatarState().getBackwardsInterpolatedWalkDistance(partial);
		float bob = player.avatarState().getInterpolatedBob(partial);
		float bobX = Mth.sin(walk * Mth.PI) * bob * 0.018F;
		float bobY = -Math.abs(Mth.cos(walk * Mth.PI) * bob) * 0.022F;
		float breathe = Mth.sin(time * 1.7F) * 0.0025F;

		boolean pointing = controller.pointerHit;
		float point = this.pointSpring.target(pointing ? 1.0F : 0.25F).update(dt);
		float pinch = this.pinchSpring.target(controller.mouseDown() ? 1.0F : 0.0F).update(dt);
		float leftPinch = this.leftPinchSpring.target(controller.secondaryDown() ? 1.0F : 0.0F).update(dt);
		float alpha = controller.handsAlpha * visibility;
		float rise = controller.handsRise;

		// Right hand: the pointing hand, seen from its thumb side so the fingers overlap naturally,
		// low on the right so it flanks the dock instead of covering it.
		this.right.alpha = alpha;
		this.right.point = point;
		this.right.pinch = pinch;
		this.right.curl = 0.10F;
		this.right.idle = time;
		this.right.wrist.set(0.302F + bobX, -0.272F - rise + bobY + breathe, -0.335F);
		orient(this.right.orientation, new Vector3f(-0.26F, 0.36F, -0.90F), new Vector3f(0.62F, 0.62F, 0.22F));
		applyLag(this.right, lag);

		// Left hand: relaxed and slightly curled; pinches on the secondary button.
		this.left.alpha = alpha;
		this.left.point = 0.0F;
		this.left.pinch = Math.max(leftPinch, 0.0F);
		this.left.curl = 0.30F;
		this.left.idle = time + 2.1F;
		this.left.wrist.set(-0.312F + bobX, -0.266F - rise + bobY - breathe, -0.322F);
		orient(this.left.orientation, new Vector3f(0.24F, 0.40F, -0.88F), new Vector3f(-0.62F, 0.62F, 0.22F));
		applyLag(this.left, lag);

		GhostHands h = this.hands;
		h.begin(width, height, HAND_FOV);
		h.addHand(this.left);
		h.addHand(this.right);

		// Pointer ray from the right hand's pinch point to the cursor on the panel.
		float rayTarget = pointing && VisorSettings.POINTER_RAY.on() ? 1.0F : 0.0F;
		float ray = this.rayAlpha.target(rayTarget).update(dt) * alpha;
		if (ray > 0.01F && controller.pointerHit && VisorSettings.POINTER_RAY.on()) {
			float[] start = h.project(this.right.pinchPoint);
			float[] end = this.projectWorld(controller.pointerWorld, width, height);
			if (start != null && end != null) {
				h.addScreenDeco(start[0], start[1], end[0], end[1], 2.0F * scale(height), 0.7F * scale(height), 0.55F * ray, 0.06F * ray);
			}
			// Pointer origin blob at the fingertips.
			Vector3f tipA = new Vector3f(this.right.pinchPoint);
			Vector3f tipB = new Vector3f(this.right.pinchPoint).lerp(new Vector3f(0, 0, -1), 0.012F);
			h.addDeco(tipA, tipB, 0.0068F, 0.0050F, 0.92F * ray, 0.92F * ray);
		}
		if (leftPinch > 0.02F) {
			// Pinch indicator: a short white stroke ahead of the left hand's fingertips.
			Vector3f a = new Vector3f(this.left.pinchPoint).add(0.010F, 0.010F, -0.006F);
			Vector3f b = new Vector3f(this.left.pinchPoint).add(0.030F, 0.042F, -0.020F);
			h.addDeco(a, b, 0.0035F, 0.0058F, 0.95F * leftPinch * alpha, 0.95F * leftPinch * alpha);
		}
		h.render(target, 1.0F, 2.2F, VisorSettings.HAND_OPACITY.floatValue());
	}

	private static float scale(int height) {
		return Math.max(1.0F, height / 1080.0F);
	}

	private static void orient(Matrix3f out, Vector3f fingers, Vector3f back) {
		Vector3f y = new Vector3f(fingers).normalize();
		Vector3f z = new Vector3f(back).fma(-back.dot(y), y).normalize();
		Vector3f x = new Vector3f(y).cross(z).normalize();
		out.set(x, y, z);
	}

	private static void applyLag(GhostHands.HandState hand, Matrix3f lag) {
		lag.transform(hand.wrist);
		new Matrix3f(lag).mul(hand.orientation, hand.orientation);
	}

	/** Projects a camera-relative world point with the world camera; [x, y] in pixels or null. */
	private float[] projectWorld(Vector3f world, int width, int height) {
		Vector4f clip = new Vector4f(world, 1.0F);
		FrameCapture.VIEW.transform(clip);
		FrameCapture.PROJECTION.transform(clip);
		if (clip.w <= 0.01F) {
			return null;
		}
		return new float[]{(clip.x / clip.w * 0.5F + 0.5F) * width, (0.5F - clip.y / clip.w * 0.5F) * height};
	}
}
