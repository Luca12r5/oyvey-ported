package dev.lego.cosmetic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.lego.ui.Sound;

/**
 * Knife tricks for the equipped LEGO knife (purely visual, no effect on fighting):
 * tap the key to inspect the knife (turn it to the side, look at it, flip it once), hold the key
 * to spin it around your hand. Released, the spin runs out and the knife is caught at the grip.
 * The rotation is applied to the pose around the grip, so the cached model is not rebuilt.
 */
public final class KnifeTricks {
   /** Grip of the knife model in model pixels (see {@link Knives}: hand at about (1, -10.6, 0)). */
   private static final float GRIP_X = 1.0F;
   private static final float GRIP_Y = -10.6F;
   private static final float INSPECT_TIME = 2.2F;
   private static final float MAX_SPEED = 960.0F;
   private static final float HOLD_FOR_SPIN = 0.22F;

   private static boolean down;
   private static float heldFor;
   private static boolean spinning;
   private static float angle;
   private static float speed;
   private static float inspect = -1.0F;
   private static long lastNanos;

   private KnifeTricks() {
   }

   public static boolean active() {
      return spinning || speed != 0.0F || angle % 360.0F != 0.0F || inspect >= 0.0F;
   }

   /** Called every client tick with the key state. */
   public static void key(boolean pressed) {
      if (pressed && !down) {
         heldFor = 0.0F;
      }
      if (!pressed && down && !spinning && heldFor < HOLD_FOR_SPIN && inspect < 0.0F) {
         inspect = 0.0F;
         Sound.play("minecraft:item.armor.equip_iron", 1.6F, 0.35F);
      }
      down = pressed;
   }

   /** Advances the animation; once per frame. */
   static void frame() {
      long now = System.nanoTime();
      float dt = lastNanos == 0L ? 0.016F : Math.min(0.05F, (now - lastNanos) / 1.0E9F);
      lastNanos = now;
      if (down) {
         heldFor += dt;
      }
      if (down && heldFor >= HOLD_FOR_SPIN) {
         if (!spinning) {
            spinning = true;
            inspect = -1.0F;
            Sound.play("minecraft:item.trident.throw", 1.8F, 0.2F);
         }
         speed = Math.min(MAX_SPEED, speed + 2600.0F * dt);
      } else if (spinning) {
         // Run out, then settle on the next full turn so the knife lands in the grip.
         speed = Math.max(140.0F, speed - 900.0F * dt);
         float target = (float)Math.ceil(angle / 360.0F) * 360.0F;
         if (speed <= 140.0F && target - angle < 10.0F) {
            spinning = false;
         }
      } else if (speed != 0.0F || angle % 360.0F != 0.0F) {
         float target = (float)Math.ceil(angle / 360.0F) * 360.0F;
         angle += Math.min(target - angle, Math.max(30.0F, (target - angle) * 12.0F) * dt);
         if (target - angle < 0.5F) {
            angle = 0.0F;
            speed = 0.0F;
            Sound.play("minecraft:item.armor.equip_chain", 1.4F, 0.3F);
         }
      }
      if (spinning) {
         angle += speed * dt;
      }
      if (inspect >= 0.0F) {
         inspect += dt;
         if (inspect > INSPECT_TIME) {
            inspect = -1.0F;
         }
      }
   }

   private static float smooth(float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      return t * t * (3.0F - 2.0F * t);
   }

   /** Rotates the pose around the grip; the pose is in knife model space (after the 1/16 scale). */
   static void apply(PoseStack pose) {
      frame();
      if (!active()) {
         return;
      }
      pose.translate(GRIP_X, GRIP_Y, 0.0F);
      if (inspect >= 0.0F) {
         // Up and to the side, a long look, one flip, back into the hand.
         float in = smooth(inspect / 0.35F) * (1.0F - smooth((inspect - (INSPECT_TIME - 0.35F)) / 0.35F));
         float flip = smooth((inspect - 1.05F) / 0.5F) * 360.0F;
         pose.mulPose(Axis.YP.rotationDegrees(65.0F * in));
         pose.mulPose(Axis.ZP.rotationDegrees(-28.0F * in));
         pose.mulPose(Axis.XP.rotationDegrees(-flip));
      }
      if (angle != 0.0F) {
         // Around the axis through the hand, across the blade: the blade circles the fingers.
         pose.mulPose(Axis.XP.rotationDegrees(-angle));
      }
      pose.translate(-GRIP_X, -GRIP_Y, 0.0F);
   }
}
