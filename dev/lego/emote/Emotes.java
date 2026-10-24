package dev.lego.emote;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Tx;
import dev.lego.visual.R3;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.CameraType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;

public final class Emotes {
   public static Emotes.EmoteModule module;
   private static EmoteLib.Emote current;
   private static long startMs;
   private static long stopMs;
   private static CameraType savedPerspective;
   private static float spin;
   private static double startX;
   private static double startZ;
   private static float[] fading;
   private static final Map<String, Identifier> BUBBLES = new HashMap<>();

   private Emotes() {
   }

   public static void register() {
      module = Modules.register(new Emotes.EmoteModule());
   }

   public static boolean active() {
      return current != null || fading != null;
   }

   public static EmoteLib.Emote current() {
      return current;
   }

   public static void play(int idx) {
      if (idx >= 0 && idx < EmoteLib.ALL.size() && module != null && module.enabled) {
         LocalPlayer p = Mc.player();
         if (p != null) {
            EmoteLib.Emote e = EmoteLib.ALL.get(idx);
            boolean restoreCam = savedPerspective != null;
            CameraType saved = savedPerspective;
            current = null;
            fading = null;
            current = e;
            startMs = System.currentTimeMillis();
            spin = 0.0F;
            startX = Mc.x(p);
            startZ = Mc.z(p);
            if (EmoteLib.isCutscene(e.id)) {
               if (restoreCam) {
                  try {
                     Mc.perspective(saved);
                  } catch (Throwable var7) {
                  }
               }

               savedPerspective = null;
               Cutscenes.begin(e);
            } else if (restoreCam) {
               savedPerspective = saved;
            } else if (module.camera.get() && Mc.firstPerson()) {
               savedPerspective = Mc.perspective();

               try {
                  Mc.perspective(CameraType.THIRD_PERSON_FRONT);
               } catch (Throwable var6) {
               }
            }
         }
      }
   }

   public static void stop() {
      if (current != null) {
         Cutscenes.end();
         fading = current.pose(time());
         stopMs = System.currentTimeMillis();
         current = null;
         if (savedPerspective != null) {
            try {
               Mc.perspective(savedPerspective);
            } catch (Throwable var1) {
            }

            savedPerspective = null;
         }
      }
   }

   private static float time() {
      return (float)(System.currentTimeMillis() - startMs) / 1000.0F * (module == null ? 1.0F : module.speed.getF());
   }

   public static void tick() {
      if (fading != null && System.currentTimeMillis() - stopMs > 260L) {
         fading = null;
      }

      if (current != null) {
         LocalPlayer p = Mc.player();
         if (p != null && !Mc.isDead(p)) {
            float t = time();
            if (!current.loop && t > current.duration) {
               stop();
            } else {
               double dx = Mc.x(p) - startX;
               double dz = Mc.z(p) - startZ;
               if (!module.cancelOnMove.get() || !(dx * dx + dz * dz > 0.09) && !Mc.isSneaking(p)) {
                  Cutscenes.sounds();
                  if (Cutscenes.active() && Cutscenes.locksYaw()) {
                     Mc.setBodyYaw(p, Cutscenes.lockYaw());
                  }

                  if (current.spin != 0.0F) {
                     spin = spin + current.spin / 20.0F * module.speed.getF();
                     Mc.setBodyYaw(p, Mc.yaw(p) + spin);
                  }
               } else {
                  stop();
               }
            }
         } else {
            stop();
         }
      }
   }

   public static void poseOther(
      String id, double startSec, ModelPart head, ModelPart body, ModelPart rightArm, ModelPart leftArm, ModelPart rightLeg, ModelPart leftLeg
   ) {
      EmoteLib.Emote e = EmoteLib.get(id);
      if (e != null && !(startSec <= 0.0)) {
         float t = (float)(System.currentTimeMillis() / 1000.0 - startSec);
         if (t < 0.0F) {
            t = 0.0F;
         }

         if (e.loop || !(t > e.duration)) {
            if (e.loop) {
               t %= Math.max(0.1F, e.duration);
            }

            float[] p = e.pose(t);
            float blend = smooth(Math.min(1.0F, Math.min(t / 0.25F, e.loop ? 1.0F : (e.duration - t) / 0.25F)));
            if (!(blend <= 0.0F)) {
               apply(head, p, 0, blend);
               apply(body, p, 3, blend);
               apply(rightArm, p, 6, blend);
               apply(leftArm, p, 9, blend);
               apply(rightLeg, p, 12, blend);
               apply(leftLeg, p, 15, blend);
            }
         }
      }
   }

   public static void pose(ModelPart head, ModelPart body, ModelPart rightArm, ModelPart leftArm, ModelPart rightLeg, ModelPart leftLeg) {
      float[] p;
      float blend;
      if (current != null) {
         float t = time();
         p = current.pose(t);
         float in = Math.min(1.0F, (float)(System.currentTimeMillis() - startMs) / 250.0F);
         float out = current.loop ? 1.0F : Math.min(1.0F, (current.duration - t) / 0.25F);
         blend = smooth(Math.max(0.0F, Math.min(in, out)));
      } else {
         if (fading == null) {
            return;
         }

         p = fading;
         blend = smooth(1.0F - Math.min(1.0F, (float)(System.currentTimeMillis() - stopMs) / 250.0F));
      }

      if (!(blend <= 0.0F)) {
         apply(head, p, 0, blend);
         apply(body, p, 3, blend);
         apply(rightArm, p, 6, blend);
         apply(leftArm, p, 9, blend);
         apply(rightLeg, p, 12, blend);
         apply(leftLeg, p, 15, blend);
         float bend = Float.isNaN(p[3]) ? 0.0F : p[3] * blend;
         if (bend != 0.0F) {
            pivotBend(head, bend);
            pivotBend(rightArm, bend);
            pivotBend(leftArm, bend);
            head.xRot += bend;
            rightArm.xRot += bend;
            leftArm.xRot += bend;
            body.y = 12.0F - 12.0F * (float)Math.cos(bend);
            body.z = -12.0F * (float)Math.sin(bend);
         }
      }
   }

   private static void pivotBend(ModelPart part, float a) {
      float ry = part.y - 12.0F;
      float rz = part.z;
      float c = (float)Math.cos(a);
      float s = (float)Math.sin(a);
      part.y = 12.0F + ry * c - rz * s;
      part.z = ry * s + rz * c;
   }

   private static void apply(ModelPart part, float[] p, int i, float b) {
      if (part != null) {
         if (!Float.isNaN(p[i])) {
            part.xRot = lerpAngle(part.xRot, p[i], b);
         }

         if (!Float.isNaN(p[i + 1])) {
            part.yRot = lerpAngle(part.yRot, p[i + 1], b);
         }

         if (!Float.isNaN(p[i + 2])) {
            part.zRot = lerpAngle(part.zRot, p[i + 2], b);
         }
      }
   }

   private static float lerpAngle(float a, float b, float t) {
      float d = (float)Math.IEEEremainder(b - a, Math.PI * 2);
      return a + d * t;
   }

   private static float smooth(float t) {
      return t * t * (3.0F - 2.0F * t);
   }

   public static void renderBubble(float td) {
      if (current != null && current.bubble != null && module.bubbles.get()) {
         LocalPlayer p = Mc.player();
         if (p != null && !Mc.firstPerson()) {
            Identifier tex = BUBBLES.get(current.bubble);
            if (tex == null) {
               tex = Identifier.fromNamespaceAndPath("legoclient", "emote/" + current.bubble);

               try {
                  Tx.register(tex, Bubbles.paint(current.bubble, 256), 256, 256, true);
               } catch (Throwable var11) {
                  LegoClient.LOG("Emote-Textur: " + var11);
               }

               BUBBLES.put(current.bubble, tex);
            }

            float t = time();
            double pop = Math.min(1.0, t / 0.3);
            pop = 1.0 + 2.70158 * Math.pow(pop - 1.0, 3.0) + 1.70158 * Math.pow(pop - 1.0, 2.0);
            double fade = current.loop ? 1.0 : Math.max(0.0, Math.min(1.0, (current.duration - t) / 0.3));
            double[] pos = Mc.lerpPos(p, td);
            double y = pos[1] + Mc.height(p) + 0.6 + Math.sin(t * 3.0F) * 0.05;
            R3.billboard(tex, pos[0], y, pos[2], 0.75 * pop, R3.argb(16777215, fade));
         }
      }
   }

   public static final class EmoteModule extends Module {
      public final Setting.Bool camera = this.add(new Setting.Bool("Kamera nach vorne drehen", true));
      public final Setting.Bool cancelOnMove = this.add(new Setting.Bool("Bei Bewegung abbrechen", true));
      public final Setting.Bool bubbles = this.add(new Setting.Bool("Sprechblasen", true));
      public final Setting.Num speed = this.add(new Setting.Num("Tempo", 0.5, 2.0, 0.1, 1.0, "x"));

      EmoteModule() {
         super("emotes", "Emotes", "Taste B halten: Emote-Rad · " + EmoteLib.ALL.size() + " Emotes, nur für dich sichtbar", Category.EMOTES);
         this.enabled = true;
         this.icon("emote");
      }
   }
}
