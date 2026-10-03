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

   public static void play(int var0) {
      if (var0 >= 0 && var0 < EmoteLib.ALL.size() && module != null && module.enabled) {
         LocalPlayer var1 = Mc.player();
         if (var1 != null) {
            EmoteLib.Emote var2 = EmoteLib.ALL.get(var0);
            boolean var3 = savedPerspective != null;
            CameraType var4 = savedPerspective;
            current = null;
            fading = null;
            current = var2;
            startMs = System.currentTimeMillis();
            spin = 0.0F;
            startX = Mc.x(var1);
            startZ = Mc.z(var1);
            if (EmoteLib.isCutscene(var2.id)) {
               if (var3) {
                  try {
                     Mc.perspective(var4);
                  } catch (Throwable var7) {
                  }
               }

               savedPerspective = null;
               Cutscenes.begin(var2);
            } else if (var3) {
               savedPerspective = var4;
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
         LocalPlayer var0 = Mc.player();
         if (var0 != null && !Mc.isDead(var0)) {
            float var1 = time();
            if (!current.loop && var1 > current.duration) {
               stop();
            } else {
               double var2 = Mc.x(var0) - startX;
               double var4 = Mc.z(var0) - startZ;
               if (!module.cancelOnMove.get() || !(var2 * var2 + var4 * var4 > 0.09) && !Mc.isSneaking(var0)) {
                  Cutscenes.sounds();
                  if (Cutscenes.active() && Cutscenes.locksYaw()) {
                     Mc.setBodyYaw(var0, Cutscenes.lockYaw());
                  }

                  if (current.spin != 0.0F) {
                     spin = spin + current.spin / 20.0F * module.speed.getF();
                     Mc.setBodyYaw(var0, Mc.yaw(var0) + spin);
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

   public static void pose(ModelPart var0, ModelPart var1, ModelPart var2, ModelPart var3, ModelPart var4, ModelPart var5) {
      float[] var6;
      float var7;
      if (current != null) {
         float var8 = time();
         var6 = current.pose(var8);
         float var9 = Math.min(1.0F, (float)(System.currentTimeMillis() - startMs) / 250.0F);
         float var10 = current.loop ? 1.0F : Math.min(1.0F, (current.duration - var8) / 0.25F);
         var7 = smooth(Math.max(0.0F, Math.min(var9, var10)));
      } else {
         if (fading == null) {
            return;
         }

         var6 = fading;
         var7 = smooth(1.0F - Math.min(1.0F, (float)(System.currentTimeMillis() - stopMs) / 250.0F));
      }

      if (!(var7 <= 0.0F)) {
         apply(var0, var6, 0, var7);
         apply(var1, var6, 3, var7);
         apply(var2, var6, 6, var7);
         apply(var3, var6, 9, var7);
         apply(var4, var6, 12, var7);
         apply(var5, var6, 15, var7);
         float var11 = Float.isNaN(var6[3]) ? 0.0F : var6[3] * var7;
         if (var11 != 0.0F) {
            pivotBend(var0, var11);
            pivotBend(var2, var11);
            pivotBend(var3, var11);
            var0.xRot += var11;
            var2.xRot += var11;
            var3.xRot += var11;
            var1.y = 12.0F - 12.0F * (float)Math.cos(var11);
            var1.z = -12.0F * (float)Math.sin(var11);
         }
      }
   }

   private static void pivotBend(ModelPart var0, float var1) {
      float var2 = var0.y - 12.0F;
      float var3 = var0.z;
      float var4 = (float)Math.cos(var1);
      float var5 = (float)Math.sin(var1);
      var0.y = 12.0F + var2 * var4 - var3 * var5;
      var0.z = var2 * var5 + var3 * var4;
   }

   private static void apply(ModelPart var0, float[] var1, int var2, float var3) {
      if (var0 != null) {
         if (!Float.isNaN(var1[var2])) {
            var0.xRot = lerpAngle(var0.xRot, var1[var2], var3);
         }

         if (!Float.isNaN(var1[var2 + 1])) {
            var0.yRot = lerpAngle(var0.yRot, var1[var2 + 1], var3);
         }

         if (!Float.isNaN(var1[var2 + 2])) {
            var0.zRot = lerpAngle(var0.zRot, var1[var2 + 2], var3);
         }
      }
   }

   private static float lerpAngle(float var0, float var1, float var2) {
      float var3 = (float)Math.IEEEremainder(var1 - var0, Math.PI * 2);
      return var0 + var3 * var2;
   }

   private static float smooth(float var0) {
      return var0 * var0 * (3.0F - 2.0F * var0);
   }

   public static void renderBubble(float var0) {
      if (current != null && current.bubble != null && module.bubbles.get()) {
         LocalPlayer var1 = Mc.player();
         if (var1 != null && !Mc.firstPerson()) {
            Identifier var2 = BUBBLES.get(current.bubble);
            if (var2 == null) {
               var2 = Identifier.fromNamespaceAndPath("legoclient", "emote/" + current.bubble);

               try {
                  Tx.register(var2, Bubbles.paint(current.bubble, 256), 256, 256, true);
               } catch (Throwable var11) {
                  LegoClient.LOG("Emote-Textur: " + var11);
               }

               BUBBLES.put(current.bubble, var2);
            }

            float var3 = time();
            double var4 = Math.min(1.0, var3 / 0.3);
            var4 = 1.0 + 2.70158 * Math.pow(var4 - 1.0, 3.0) + 1.70158 * Math.pow(var4 - 1.0, 2.0);
            double var6 = current.loop ? 1.0 : Math.max(0.0, Math.min(1.0, (current.duration - var3) / 0.3));
            double[] var8 = Mc.lerpPos(var1, var0);
            double var9 = var8[1] + Mc.height(var1) + 0.6 + Math.sin(var3 * 3.0F) * 0.05;
            R3.billboard(var2, var8[0], var9, var8[2], 0.75 * var4, R3.argb(16777215, var6));
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
