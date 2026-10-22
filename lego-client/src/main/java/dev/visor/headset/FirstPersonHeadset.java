package dev.visor.headset;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.visor.ui.Ease;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Renders the player's own arms bringing the headset to (or away from) the eyes.
 *
 * <p>Everything is placed in camera space (x right, y up, camera looking down -z, in blocks),
 * inside vanilla's first person hand pass, so lighting, skins and sleeves behave exactly like
 * the normal first person arm.</p>
 */
public final class FirstPersonHeadset {
	/** One moment of the animation. */
	private record Key(float time, Vector3f headset, float pitch, float roll, float handSpread, float handForward, Vector3f elbow) {
	}

	// Putting the headset on. Times in seconds from the start of the equip animation.
	private static final Key[] EQUIP = {
		new Key(0.00F, new Vector3f(0.00F, -0.78F, -0.52F), 34.0F, -6.0F, 0.338F, -0.095F, new Vector3f(0.22F, -0.88F, 0.42F)),
		new Key(0.42F, new Vector3f(0.00F, -0.15F, -0.47F), 9.0F, 2.0F, 0.338F, -0.095F, new Vector3f(0.30F, -0.80F, 0.52F)),
		new Key(0.54F, new Vector3f(0.00F, -0.125F, -0.445F), 5.0F, 0.0F, 0.338F, -0.095F, new Vector3f(0.32F, -0.78F, 0.54F)),
		new Key(0.92F, new Vector3f(0.00F, -0.010F, -0.085F), 0.0F, 0.0F, 0.380F, -0.070F, new Vector3f(0.74F, -0.42F, 0.52F)),
		new Key(1.20F, new Vector3f(0.00F, -0.010F, -0.080F), 0.0F, 0.0F, 0.392F, -0.060F, new Vector3f(0.78F, -0.40F, 0.48F)),
	};

	// Taking it off. Times in seconds from the start of the remove animation.
	private static final Key[] REMOVE = {
		new Key(0.00F, new Vector3f(0.00F, -0.010F, -0.080F), 0.0F, 0.0F, 0.392F, -0.060F, new Vector3f(0.78F, -0.40F, 0.48F)),
		new Key(0.30F, new Vector3f(0.00F, -0.020F, -0.110F), 0.0F, 0.0F, 0.386F, -0.065F, new Vector3f(0.74F, -0.42F, 0.52F)),
		new Key(0.64F, new Vector3f(0.00F, -0.120F, -0.430F), 7.0F, -2.0F, 0.338F, -0.095F, new Vector3f(0.32F, -0.78F, 0.54F)),
		new Key(1.18F, new Vector3f(0.00F, -0.860F, -0.500F), 38.0F, 6.0F, 0.338F, -0.095F, new Vector3f(0.22F, -0.90F, 0.40F)),
	};

	private FirstPersonHeadset() {
	}

	public static float equipDuration() {
		return EQUIP[EQUIP.length - 1].time();
	}

	public static float removeDuration() {
		return REMOVE[REMOVE.length - 1].time();
	}

	/**
	 * @param time     seconds since the animation started
	 * @param removing true while taking the headset off
	 */
	public static void render(float time, boolean removing, PoseStack poseStack, SubmitNodeCollector collector, int light) {
		Minecraft minecraft = Minecraft.getInstance();
		AbstractClientPlayer player = minecraft.player;
		if (player == null) {
			return;
		}
		Key[] keys = removing ? REMOVE : EQUIP;
		if (time >= keys[keys.length - 1].time()) {
			return;
		}
		Pose pose = sample(keys, time, removing);
		// A tiny bit of hand-held wobble so it never looks robotic.
		float wobble = Mth.sin(time * 9.0F) * 0.0025F * (1.0F - Ease.window(time, 0.7F, 0.95F));
		pose.headset.add(0.0F, wobble, 0.0F);

		AvatarRenderer<AbstractClientPlayer> renderer = minecraft.getEntityRenderDispatcher().getPlayerRenderer(player);
		Identifier skin = player.getSkin().body().texturePath();

		// Arms: hands grip the sides of the headset, palms facing inwards.
		for (int side = -1; side <= 1; side += 2) {
			boolean right = side > 0;
			Vector3f hand = new Vector3f(pose.headset).add(side * pose.handSpread, -0.010F, pose.handForward);
			Vector3f elbow = new Vector3f(pose.elbow.x * side, pose.elbow.y, pose.elbow.z).normalize();
			Vector3f palm = new Vector3f(-side, 0.0F, 0.0F);
			poseStack.pushPose();
			poseStack.mulPose(armMatrix(hand, elbow, palm, right));
			if (right) {
				renderer.renderRightHand(poseStack, collector, light, skin, player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE));
			} else {
				renderer.renderLeftHand(poseStack, collector, light, skin, player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE));
			}
			poseStack.popPose();
		}

		// The headset itself, cushion side facing the eyes.
		poseStack.pushPose();
		poseStack.translate(pose.headset.x, pose.headset.y, pose.headset.z);
		poseStack.mulPose(new Quaternionf().rotationXYZ(pose.pitch * Mth.DEG_TO_RAD, 0.0F, pose.roll * Mth.DEG_TO_RAD));
		HeadsetRenderer.submit(poseStack, collector, light, false);
		poseStack.popPose();
	}

	private static final class Pose {
		final Vector3f headset = new Vector3f();
		final Vector3f elbow = new Vector3f();
		float pitch;
		float roll;
		float handSpread;
		float handForward;
	}

	private static Pose sample(Key[] keys, float time, boolean removing) {
		int index = 0;
		while (index < keys.length - 2 && time > keys[index + 1].time()) {
			index++;
		}
		Key a = keys[index];
		Key b = keys[index + 1];
		float raw = Ease.clamp01((time - a.time()) / (b.time() - a.time()));
		// Lifting eases out, the final approach to the face eases in and out; leaving accelerates.
		float t = removing ? Ease.inOutCubic(raw) : (index == 0 ? Ease.outCubic(raw) : Ease.inOutCubic(raw));
		Pose pose = new Pose();
		a.headset().lerp(b.headset(), t, pose.headset);
		a.elbow().lerp(b.elbow(), t, pose.elbow);
		pose.pitch = Mth.lerp(t, a.pitch(), b.pitch());
		pose.roll = Mth.lerp(t, a.roll(), b.roll());
		pose.handSpread = Mth.lerp(t, a.handSpread(), b.handSpread());
		pose.handForward = Mth.lerp(t, a.handForward(), b.handForward());
		return pose;
	}

	/**
	 * Pose matrix for {@code AvatarRenderer.render*Hand}: places the hand of the arm model at
	 * {@code hand}, with the forearm running towards {@code elbowDirection}. The arm model part
	 * applies its own pivot and roll afterwards, which is undone here.
	 */
	private static Matrix4f armMatrix(Vector3f hand, Vector3f elbowDirection, Vector3f palmDirection, boolean right) {
		Vector3f yAxis = new Vector3f(elbowDirection).negate().normalize();
		Vector3f palm = new Vector3f(palmDirection).fma(-palmDirection.dot(yAxis), yAxis).normalize();
		Vector3f xAxis = right ? palm : new Vector3f(palm).negate();
		Vector3f zAxis = new Vector3f(xAxis).cross(yAxis).normalize();
		Matrix4f rotation = new Matrix4f().set3x3(new Matrix3f(xAxis, yAxis, zAxis));
		float side = right ? 1.0F : -1.0F;
		Vector3f handLocal = new Vector3f(-side / 16.0F, 8.5F / 16.0F, 0.0F);
		Matrix4f desired = new Matrix4f().translation(hand).mul(rotation).translate(-handLocal.x, -handLocal.y, -handLocal.z);
		// Undo the arm part's own transform: translate(pivot / 16) then rotate Z by +-0.1 rad.
		Matrix4f part = new Matrix4f().translation(-5.0F * side / 16.0F, 2.0F / 16.0F, 0.0F).rotateZ(0.1F * side);
		return desired.mul(part.invert());
	}
}
