package dev.lego.cosmetic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.lego.ui.Sound;

/**
 * Knife animations for the equipped LEGO knife (purely visual, no effect on fighting):
 * <ul>
 * <li>draw: when the knife comes into view it is pulled up and (folding knives) flicked open;</li>
 * <li>tap the key: inspect, different per knife (butterfly flips its handles, folding knives
 *     close and snap open, the karambit spins round the finger ring, push daggers twirl, the rest
 *     are turned to the side, looked at and flipped once);</li>
 * <li>hold the key: the knife spins round your hand; released it runs out and lands in the grip.</li>
 * </ul>
 * Rotations are applied to the pose around a pivot, so the cached model is not rebuilt for them;
 * only moving parts (folding) rebuild the model while they move.
 */
public final class KnifeTricks {
   /** Grip of the knife model in model pixels (see {@link Knives}: hand at about (1, -10.6, 0)). */
   private static final float GRIP_X = 1.0F;
   private static final float GRIP_Y = -10.6F;
   private static final float MAX_SPEED = 960.0F;
   private static final float HOLD_FOR_SPIN = 0.22F;
   private static final float DRAW_TIME = 0.7F;

   private static boolean down;
   private static float heldFor;
   private static boolean spinning;
   private static float angle;
   private static float speed;
   private static float inspect = -1.0F;
   private static float drawT = -1.0F;
   private static float open = 1.0F;
   private static String type = "";
   private static long lastNanos;

   private KnifeTricks() {
   }

   static String typeOf(String id) {
      if (id == null || !id.startsWith("knife_")) return "";
      String rest = id.substring(6);
      int cut = rest.indexOf('_');
      return cut < 0 ? rest : rest.substring(0, cut);
   }

   private static boolean folding() {
      return switch (type) {
         case "butterfly", "flip", "navaja", "stiletto", "falchion" -> true;
         default -> false;
      };
   }

   private static float inspectTime() {
      return switch (type) {
         case "butterfly" -> 2.6F;
         case "karambit" -> 1.9F;
         case "daggers" -> 1.6F;
         default -> folding() ? 1.8F : 2.2F;
      };
   }

   public static boolean active() {
      return spinning || speed != 0.0F || angle % 360.0F != 0.0F || inspect >= 0.0F || drawT >= 0.0F;
   }

   /** True while parts of the model move (the model is rebuilt every frame then). */
   static boolean partsMoving() {
      return folding() && (inspect >= 0.0F || drawT >= 0.0F);
   }

   static float open() {
      return open;
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

   private static float smooth(float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      return t * t * (3.0F - 2.0F * t);
   }

   /** Advances the animations for the knife {@code id}; once per drawn frame. */
   static void frame(String id) {
      long now = System.nanoTime();
      float gap = lastNanos == 0L ? 1.0F : (now - lastNanos) / 1.0E9F;
      float dt = Math.min(0.05F, gap);
      lastNanos = now;
      String t = typeOf(id);
      if (!t.equals(type) || gap > 0.4F) {
         // Newly equipped, or the knife comes back into view: draw it.
         type = t;
         drawT = 0.0F;
         inspect = -1.0F;
         Sound.play("minecraft:item.armor.equip_chain", 1.5F, 0.3F);
      }
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
      if (drawT >= 0.0F) {
         drawT += dt;
         if (drawT > DRAW_TIME) drawT = -1.0F;
      }
      if (inspect >= 0.0F) {
         inspect += dt;
         if (inspect > inspectTime()) inspect = -1.0F;
      }
      open = openNow();
   }

   private static float openNow() {
      if (!folding()) return 1.0F;
      if (drawT >= 0.0F) {
         return smooth((drawT - 0.15F) / 0.3F);
      }
      if (inspect >= 0.0F) {
         if (type.equals("butterfly")) {
            // open → closed → open → closed → open, quick flicks.
            float k = inspect / inspectTime();
            return 0.5F + 0.5F * (float)Math.cos(k * Math.PI * 4.0);
         }
         float k = inspect / inspectTime();
         return k < 0.45F ? 1.0F - smooth(k / 0.3F) : smooth((k - 0.55F) / 0.15F);
      }
      return 1.0F;
   }

   /** Rotates the pose around a pivot; the pose is in knife model space (after the 1/16 scale). */
   static void apply(PoseStack pose) {
      if (!active()) {
         return;
      }
      boolean karambit = type.equals("karambit");
      // Karambit tricks turn round the finger ring behind the grip, the rest round the grip.
      float pz = karambit ? -3.5F : 0.0F;
      pose.translate(GRIP_X, GRIP_Y, pz);
      if (drawT >= 0.0F) {
         float k = smooth(drawT / DRAW_TIME);
         pose.translate(0.0F, 6.0F * (1.0F - k), 0.0F);
         pose.mulPose(Axis.XP.rotationDegrees(-70.0F * (1.0F - k)));
         pose.mulPose(Axis.ZP.rotationDegrees(25.0F * (1.0F - k)));
      }
      if (inspect >= 0.0F) {
         float t = inspect, total = inspectTime();
         float in = smooth(t / 0.35F) * (1.0F - smooth((t - (total - 0.35F)) / 0.35F));
         switch (type) {
            case "karambit" -> pose.mulPose(Axis.XP.rotationDegrees(-smooth((t - 0.2F) / (total - 0.5F)) * 720.0F));
            case "daggers" -> pose.mulPose(Axis.YP.rotationDegrees(smooth(t / total) * 360.0F));
            case "butterfly" -> {
               pose.mulPose(Axis.ZP.rotationDegrees(-35.0F * in));
               pose.mulPose(Axis.YP.rotationDegrees(40.0F * in));
            }
            default -> {
               pose.mulPose(Axis.YP.rotationDegrees(65.0F * in));
               pose.mulPose(Axis.ZP.rotationDegrees(-28.0F * in));
               if (!folding()) {
                  pose.mulPose(Axis.XP.rotationDegrees(-smooth((t - 1.05F) / 0.5F) * 360.0F));
               }
            }
         }
      }
      if (angle != 0.0F) {
         // Around the axis through the hand, across the blade: the blade circles the fingers.
         pose.mulPose(Axis.XP.rotationDegrees(-angle));
      }
      pose.translate(-GRIP_X, -GRIP_Y, -pz);
   }
}
