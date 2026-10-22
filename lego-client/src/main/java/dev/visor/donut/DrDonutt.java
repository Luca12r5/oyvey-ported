package dev.visor.donut;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.visor.VisorClient;
import dev.visor.module.Category;
import dev.visor.module.Module;
import dev.visor.render.FrameCapture;
import dev.visor.render.UiResources;
import dev.visor.setting.BoolSetting;
import dev.visor.setting.ModeSetting;
import dev.visor.setting.SliderSetting;
import dev.visor.setting.VisorSettings;
import dev.visor.ui.Theme;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

/**
 * DrDonutt: Doctor Donut in 3D, dancing to his song right in front of you. He only exists on
 * this client (no entity, no collisions, nothing is sent to the server), so it works everywhere.
 * With the headset on he can be picked up with the pointer and put down somewhere else.
 */
public final class DrDonutt extends Module {
	public static final String ID = "drdonutt";
	private static final String DANCE = "Dance";
	private static final double VIEW_DISTANCE = 96.0;
	private static final double MUSIC_DISTANCE = 48.0;
	/** Furthest away he can be picked up, and carried (blocks). */
	private static final double REACH = 64.0;
	private static final double NEAREST = 1.5;
	/** Eye height used to turn looking up and down into further and closer while carrying him. */
	private static final double CARRY_EYE_HEIGHT = 1.62;
	private static final CompletableFuture<DonutModel> MODEL = CompletableFuture.supplyAsync(DrDonutt::readModel, task -> {
		Thread thread = new Thread(task, "Visor Doctor Donut loader");
		thread.setDaemon(true);
		thread.start();
	});
	private static @Nullable DrDonutt instance;

	private final ModeSetting animation = new ModeSetting("animation", "Animation", "What he does", DANCE, DANCE, "Wave", "Idle", "Walk", "Run");
	private final ModeSetting placement = new ModeSetting(
		"position", "Position", "Stay where he is put, or walk along with you", VisorSettings.STAY, VisorSettings.STAY, VisorSettings.FOLLOW_ME
	).onChange(this::placementChanged);
	private final SliderSetting size = new SliderSetting("size", "Scale", "1x is about as tall as a player", 3.0, 0.5, 3.0, 0.25, "x")
		.replacesDefault(1.0);
	private final SliderSetting distance = new SliderSetting("distance", "Distance", "How far in front of you he appears", 8.0, 2.0, 12.0, 0.5, " blocks")
		.replacesDefault(3.0);
	private final BoolSetting turntable = new BoolSetting("turntable", "Turntable", "Slowly turn around on the spot", false);
	private final BoolSetting music = new BoolSetting("music", "Music", "Play his song while he dances", true);
	private final SliderSetting volume = new SliderSetting("volume", "Music volume", "Also follows Jukebox/Note Blocks", 0.65, 0.05, 1.0, 0.05, "%", 100.0)
		.visibleWhen(() -> this.music.on());

	private @Nullable DonutRenderer renderer;
	private boolean failed;
	private @Nullable DonutMusic song;
	private @Nullable ClientLevel placedLevel;
	/** Where his feet are, as drawn, and where he is gliding to (carried, or following the player). */
	private @Nullable Vec3 position;
	private @Nullable Vec3 target;
	private Vec3 followOffset = Vec3.ZERO;
	private boolean placeNextFrame;
	private double heading;
	private double headingTarget;
	private double spin;
	private double clipTime;
	private double danceTime;
	private long lastNanos;

	// Picked up with the pointer.
	private boolean pointedAt;
	private float highlight;
	private boolean dragging;
	private double pickHeight;
	private double grabHeight;
	private double carryBearing;
	private double carryDistance;
	private float carryViewYaw;
	private float carryViewPitch;

	public DrDonutt() {
		super(ID, "DrDonutt", "Doctor Donut in 3D, dancing right in front of you", "disco-ball", Category.VISOR);
		this.group("Doctor Donut", this.animation, this.placement, this.size, this.distance, this.turntable);
		this.group("Music", this.music, this.volume);
		UiResources.preload(DonutRenderer.FRONT);
		UiResources.preload(DonutRenderer.BACK);
		instance = this;
	}

	public static DrDonutt get() {
		if (instance == null) {
			throw new IllegalStateException("DrDonutt is not registered yet");
		}
		return instance;
	}

	private static DonutModel readModel() {
		try (InputStream stream = DrDonutt.class.getResourceAsStream("/assets/visor/models/doctor_donut.ddm")) {
			return DonutModel.read(stream);
		} catch (IOException e) {
			throw new IllegalStateException("Could not read Doctor Donut", e);
		}
	}

	/** Puts him in front of the player, switching the module on when needed. */
	public void summon() {
		this.placeNextFrame = true;
		this.dragging = false;
		if (this.song != null) {
			this.song.stop();
		}
		this.setEnabled(true);
	}

	/** Where the dance is right now, for the pictures of him in the menu. */
	public double danceTime(double fallbackClock) {
		return this.enabled() && this.position != null && this.animation.is(DANCE) ? this.danceTime : fallbackClock;
	}

	/** What he is doing, as chosen in his settings. */
	public String animation() {
		return this.animation.get();
	}

	/** Whether he walks along with the player instead of staying where he was put. */
	public boolean following() {
		return this.placement.is(VisorSettings.FOLLOW_ME);
	}

	/** His feet, or null while he is not out there. */
	public @Nullable Vec3 feet() {
		return this.enabled() && this.placedLevel == Minecraft.getInstance().level ? this.position : null;
	}

	/** Radius (blocks) of his body, as used for picking him up. */
	public double radius() {
		DonutRenderer renderer = this.renderer;
		return (renderer == null ? 0.33 : renderer.model().bounds().radius()) * this.size.get();
	}

	private double height() {
		DonutRenderer renderer = this.renderer;
		return (renderer == null ? 1.83 : renderer.model().bounds().height()) * this.size.get();
	}

	/** The renderer once the model is loaded; null while loading or when it could not be read. */
	public @Nullable DonutRenderer renderer() {
		if (this.renderer == null && !this.failed && MODEL.isDone()) {
			try {
				this.renderer = new DonutRenderer(MODEL.join());
			} catch (RuntimeException e) {
				this.failed = true;
				VisorClient.LOGGER.error("Doctor Donut could not be loaded", e);
			}
		}
		return this.renderer;
	}

	@Override
	protected void onEnable() {
		this.placeNextFrame = true;
	}

	@Override
	protected void onDisable() {
		this.position = null;
		this.target = null;
		this.placedLevel = null;
		this.dragging = false;
		this.highlight = 0.0F;
		if (this.song != null) {
			this.song.stop();
		}
	}

	/** Switching to "follow me" keeps him at the spot next to the player where he is now. */
	private void placementChanged() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null && this.position != null) {
			this.followOffset = this.position.subtract(player.position());
		}
		this.target = null;
	}

	/** End of every client tick: no world, no music. */
	public void tick(Minecraft minecraft) {
		if ((!this.enabled() || minecraft.level == null) && this.song != null) {
			this.song.stop();
		}
	}

	// ------------------------------------------------------------------------------------------
	// Picking him up with the headset's pointer

	/** Called every frame by the headset: whether its pointer is on him. */
	public void pointedAt(boolean pointedAt) {
		this.pointedAt = pointedAt;
	}

	/**
	 * Distance along the pointer ray to him (a cylinder round his body), or -1 when the ray misses
	 * him or a block is in the way.
	 */
	public double pick(Vec3 eye, Vec3 look) {
		Minecraft minecraft = Minecraft.getInstance();
		Vec3 feet = this.position;
		if (!this.enabled() || feet == null || this.renderer == null || minecraft.level == null || minecraft.level != this.placedLevel) {
			return -1.0;
		}
		double radius = Math.max(0.2, this.radius());
		double top = feet.y + this.height();
		double ox = eye.x - feet.x;
		double oz = eye.z - feet.z;
		double c = ox * ox + oz * oz - radius * radius;
		if (c <= 0.0) {
			// Standing inside him.
			return -1.0;
		}
		double a = look.x * look.x + look.z * look.z;
		double b = 2.0 * (ox * look.x + oz * look.z);
		double disc = b * b - 4.0 * a * c;
		double t = -1.0;
		if (a > 1.0E-9 && disc >= 0.0) {
			double side = (-b - Math.sqrt(disc)) / (2.0 * a);
			double y = eye.y + look.y * side;
			if (side > 0.0 && y >= feet.y && y <= top) {
				t = side;
			}
		}
		if (t < 0.0 && eye.y > top && look.y < -1.0E-6) {
			// Looking down onto the top of a small Doctor Donut.
			double cap = (top - eye.y) / look.y;
			double cx = ox + look.x * cap;
			double cz = oz + look.z * cap;
			if (cap > 0.0 && cx * cx + cz * cz <= radius * radius) {
				t = cap;
			}
		}
		if (t < 0.0 || t > REACH) {
			return -1.0;
		}
		Vec3 hit = eye.add(look.scale(t));
		BlockHitResult block = minecraft.level.clip(new ClipContext(eye, hit, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, minecraft.player));
		if (block.getType() != HitResult.Type.MISS) {
			return -1.0;
		}
		this.pickHeight = hit.y - feet.y;
		return t;
	}

	/** Picks him up where the pointer is on him; false when he is not out there. */
	public boolean beginDrag(LocalPlayer player, float partialTick) {
		Vec3 feet = this.feet();
		if (feet == null) {
			return false;
		}
		Vec3 eye = player.getEyePosition(partialTick);
		double dx = feet.x - eye.x;
		double dz = feet.z - eye.z;
		this.carryDistance = Mth.clamp(Math.sqrt(dx * dx + dz * dz), NEAREST, REACH);
		this.carryBearing = Math.toDegrees(Math.atan2(-dx, dz));
		this.carryViewYaw = player.getViewYRot(partialTick);
		this.carryViewPitch = player.getViewXRot(partialTick);
		this.grabHeight = Mth.clamp(this.pickHeight, 0.0, this.height());
		this.target = feet;
		this.dragging = true;
		return true;
	}

	/**
	 * Follows the pointer while he is held: turning carries him round the player, and looking down
	 * or up brings him closer or sends him further away, as if looking at the ground at his feet.
	 */
	public boolean drag(LocalPlayer player, float partialTick) {
		ClientLevel level = Minecraft.getInstance().level;
		Vec3 feet = this.feet();
		if (!this.dragging || feet == null || level == null) {
			this.dragging = false;
			return false;
		}
		Vec3 eye = player.getEyePosition(partialTick);
		double bearing = this.carryBearing + player.getViewYRot(partialTick) - this.carryViewYaw;
		double angle = Math.atan2(CARRY_EYE_HEIGHT, this.carryDistance) + Math.toRadians(player.getViewXRot(partialTick) - this.carryViewPitch);
		angle = Mth.clamp(angle, Math.atan2(CARRY_EYE_HEIGHT, REACH), Math.atan2(CARRY_EYE_HEIGHT, NEAREST));
		double reach = CARRY_EYE_HEIGHT / Math.tan(angle);
		double yaw = Math.toRadians(bearing);
		double x = eye.x - Math.sin(yaw) * reach;
		double z = eye.z + Math.cos(yaw) * reach;
		double y = ground(level, player, x, Math.max(feet.y, eye.y) + 2.0, z, 18.0, feet.y);
		this.target = new Vec3(x, y, z);
		this.headingTarget = 180.0 - bearing;
		return true;
	}

	/** Scrolling while holding him pushes him away or pulls him closer. */
	public void push(float amount) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (!this.dragging || player == null) {
			return;
		}
		// Start again from where he is now, a little further or closer, so the pointer keeps its grip.
		this.carryDistance = Mth.clamp(this.currentCarryDistance(player) * (1.0 + 0.1 * amount), NEAREST, REACH);
		this.carryViewPitch = player.getViewXRot(1.0F);
		this.carryBearing += player.getViewYRot(1.0F) - this.carryViewYaw;
		this.carryViewYaw = player.getViewYRot(1.0F);
	}

	private double currentCarryDistance(LocalPlayer player) {
		Vec3 spot = this.target != null ? this.target : this.position;
		if (spot == null) {
			return this.carryDistance;
		}
		Vec3 eye = player.getEyePosition(1.0F);
		return Math.sqrt((spot.x - eye.x) * (spot.x - eye.x) + (spot.z - eye.z) * (spot.z - eye.z));
	}

	/** Lets go of him: he stays where he was put down, or keeps that spot next to the player. */
	public void endDrag() {
		if (!this.dragging) {
			return;
		}
		this.dragging = false;
		LocalPlayer player = Minecraft.getInstance().player;
		Vec3 spot = this.target != null ? this.target : this.position;
		if (player != null && spot != null) {
			this.followOffset = spot.subtract(player.position());
		}
	}

	/** The point on him the pointer holds on to while he is carried. */
	public @Nullable Vec3 grabPoint() {
		Vec3 feet = this.feet();
		LocalPlayer player = Minecraft.getInstance().player;
		if (feet == null || player == null) {
			return null;
		}
		Vec3 toward = player.getEyePosition(1.0F).subtract(feet);
		Vec3 flat = new Vec3(toward.x, 0.0, toward.z);
		Vec3 front = flat.lengthSqr() > 1.0E-6 ? flat.normalize().scale(this.radius()) : Vec3.ZERO;
		return feet.add(front.x, this.grabHeight, front.z);
	}

	// ------------------------------------------------------------------------------------------

	/** Draws him into the world; called before the translucent layer, so water and glass stay in front. */
	public void renderWorld() {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		ClientLevel level = minecraft.level;
		if (!this.enabled() || player == null || level == null || !FrameCapture.valid) {
			return;
		}
		DonutRenderer renderer = this.renderer();
		if (renderer == null) {
			return;
		}
		if (this.placeNextFrame || this.placedLevel != level || this.position == null) {
			this.place(player, level);
		}
		long now = System.nanoTime();
		double dt = this.lastNanos == 0L || minecraft.isPaused() ? 0.0 : Math.min(0.1, (now - this.lastNanos) / 1.0E9);
		this.lastNanos = now;
		this.move(player, level, dt, minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true));

		Vec3 camera = FrameCapture.cameraPosition;
		Vec3 feet = this.position;
		double distanceSqr = feet.distanceToSqr(camera);
		String clip = this.animation.get();
		double time;
		if (clip.equals(DANCE)) {
			if (this.song == null) {
				this.song = new DonutMusic(renderer.model().duration(DANCE));
			}
			boolean audible = this.music.on() && distanceSqr < MUSIC_DISTANCE * MUSIC_DISTANCE;
			time = this.song.update(audible, this.volume.floatValue());
			this.danceTime = time;
		} else {
			if (this.song != null) {
				this.song.stop();
			}
			this.clipTime += dt;
			time = this.clipTime;
		}
		if (this.turntable.on()) {
			this.spin = (this.spin + dt * 24.0) % 360.0;
		}
		float lit = this.dragging ? 1.0F : this.pointedAt ? 0.6F : 0.0F;
		this.highlight += (lit - this.highlight) * (float) (1.0 - Math.exp(-dt * 14.0));
		if (distanceSqr > VIEW_DISTANCE * VIEW_DISTANCE) {
			return;
		}

		float scale = this.size.floatValue();
		Matrix4f modelView = new Matrix4f(FrameCapture.VIEW)
			.translate((float) (feet.x - camera.x), (float) (feet.y - camera.y), (float) (feet.z - camera.z))
			.rotateY((float) Math.toRadians(this.heading + this.spin))
			.scale(scale);
		int light = LevelRenderer.getLightColor(level, BlockPos.containing(feet.x, feet.y + 1.2 * scale, feet.z));
		RenderTarget target = minecraft.getMainRenderTarget();
		try {
			renderer.draw(
				DonutRenderer.Slot.WORLD, target.getColorTextureView(), target.getDepthTextureView(), false, modelView,
				RenderSystem.getProjectionMatrixBuffer(), clip, time, LightTexture.block(light), LightTexture.sky(light),
				Theme.accentBright(), this.highlight
			);
		} catch (RuntimeException e) {
			VisorClient.LOGGER.error("Doctor Donut could not be drawn; switching DrDonutt off", e);
			this.setEnabled(false);
		}
	}

	/** Glides towards where he is carried or, in "follow me" mode, his spot next to the player. */
	private void move(LocalPlayer player, ClientLevel level, double dt, float partialTick) {
		Vec3 feet = this.position;
		if (feet == null) {
			return;
		}
		if (!this.dragging && this.following()) {
			Vec3 spot = player.getPosition(partialTick).add(this.followOffset);
			if (this.target == null || Math.abs(this.target.x - spot.x) + Math.abs(this.target.z - spot.z) > 0.03) {
				double base = player.getY() + this.followOffset.y;
				this.target = new Vec3(spot.x, ground(level, player, spot.x, base + 1.5, spot.z, 8.0, base), spot.z);
			}
		}
		if (this.target != null) {
			if (feet.distanceToSqr(this.target) > 32.0 * 32.0) {
				// Too far behind (a teleport): just be there.
				this.position = this.target;
			} else {
				double rate = this.dragging ? 18.0 : 5.0;
				this.position = feet.lerp(this.target, 1.0 - Math.exp(-dt * rate));
			}
		}
		if (this.dragging || this.following()) {
			// Carried or walking along, he keeps facing the player.
			Vec3 toward = this.position.subtract(player.position());
			if (toward.horizontalDistanceSqr() > 0.01) {
				this.headingTarget = 180.0 - Math.toDegrees(Math.atan2(-toward.x, toward.z));
			}
		}
		this.heading += Mth.wrapDegrees(this.headingTarget - this.heading) * (1.0 - Math.exp(-dt * 8.0));
	}

	/** In front of the player, on the ground, facing them. */
	private void place(LocalPlayer player, ClientLevel level) {
		double yaw = Math.toRadians(player.getYRot());
		double reach = this.distance.get();
		Vec3 feet = player.position();
		double x = feet.x - Math.sin(yaw) * reach;
		double z = feet.z + Math.cos(yaw) * reach;
		this.position = new Vec3(x, ground(level, player, x, feet.y + 1.5, z, 7.5, feet.y), z);
		this.target = null;
		this.followOffset = this.position.subtract(feet);
		this.heading = 180.0 - player.getYRot();
		this.headingTarget = this.heading;
		this.placedLevel = level;
		this.placeNextFrame = false;
		this.dragging = false;
		this.spin = 0.0;
		this.clipTime = 0.0;
	}

	/**
	 * Height of the ground found looking down from {@code from} over {@code depth} blocks, or
	 * {@code fallback} when there is none.
	 */
	private static double ground(ClientLevel level, LocalPlayer player, double x, double from, double z, double depth, double fallback) {
		BlockHitResult hit = level.clip(new ClipContext(
			new Vec3(x, from, z), new Vec3(x, from - depth, z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player
		));
		if (hit.getType() == HitResult.Type.MISS || hit.isInside()) {
			return fallback;
		}
		return hit.getLocation().y;
	}
}
