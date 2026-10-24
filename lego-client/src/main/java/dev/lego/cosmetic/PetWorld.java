package dev.lego.cosmetic;

import dev.lego.core.Mc;
import dev.lego.emote.Bubbles;
import dev.lego.ui.Toasts;
import dev.lego.ui.Tx;
import dev.lego.visual.R3;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class PetWorld {
   private static double x;
   private static double y;
   private static double z;
   private static double px;
   private static double py;
   private static double pz;
   private static float yaw;
   private static float lastYaw;
   private static boolean placed;
   private static float speed;
   private static String emotion = "happy";
   private static String shownEmotion = "happy";
   private static long emotionUntil;
   private static long bubbleAt;
   private static long lastActive = System.currentTimeMillis();
   private static long nextBlink;
   private static long blinkAt;
   private static long curiousAt;
   private static float lastHealth = -1.0F;
   private static long nextCurious = System.currentTimeMillis() + 20000L;
   private static float spinFrom;
   private static int lastLevel = -1;
   private static final Map<String, Identifier> BUBBLES = new HashMap<>();

   private PetWorld() {
   }

   static float growth() {
      LocalPlayer p = Mc.player();
      int lvl = p == null ? 0 : p.experienceLevel;
      return Math.max(0.0F, Math.min(1.0F, lvl / 40.0F));
   }

   static int level() {
      LocalPlayer p = Mc.player();
      int lvl = p == null ? 0 : p.experienceLevel;
      return lvl >= 35 ? 5 : (lvl >= 25 ? 4 : (lvl >= 15 ? 3 : (lvl >= 5 ? 2 : 1)));
   }

   public static void onHit() {
      set("excited", 1800L);
   }

   public static void onKill() {
      set("excited", 3500L);
   }

   private static void set(String e, long ms) {
      emotion = e;
      emotionUntil = System.currentTimeMillis() + ms;
   }

   public static void tick() {
      LocalPlayer p = Mc.player();
      Cos.Item it = Cos.equipped(Cos.Slot.PET);
      if (p != null && it != null) {
         long now = System.currentTimeMillis();
         px = x;
         py = y;
         pz = z;
         lastYaw = yaw;
         double by = Math.toRadians(Mc.bodyYaw(p));
         double fx = -Math.sin(by);
         double fz = Math.cos(by);
         double rx = -Math.cos(by);
         double rz = -Math.sin(by);
         double off = 0.85 + growth() * 0.45;
         double tx = Mc.x(p) + rx * off - fx * (0.55 + growth() * 0.4);
         double tz = Mc.z(p) + rz * off - fz * (0.55 + growth() * 0.4);
         int lv = level();
         if (lastLevel > 0 && lv > lastLevel) {
            set("excited", 4000L);
            Toasts.show("paw", "Dein Haustier ist gewachsen!", "Stufe " + lv + " von 5", -15043, 4000L);
         }

         lastLevel = lv;
         double ty = ground(tx, Mc.y(p) + 0.6, tz, Mc.y(p));
         if (!placed || (x - tx) * (x - tx) + (z - tz) * (z - tz) > 144.0 || Math.abs(y - ty) > 8.0) {
            px = tx;
            x = tx;
            py = ty;
            y = ty;
            pz = tz;
            z = tz;
            yaw = lastYaw = Mc.yaw(p);
            placed = true;
         }

         double dx = tx - x;
         double dz = tz - z;
         double d = Math.sqrt(dx * dx + dz * dz);
         double k = d > 0.25 ? Math.min(0.35, 0.08 + d * 0.06) : 0.0;
         x += dx * k;
         z += dz * k;
         y = y + (ty - y) * 0.35;
         double moved = Math.sqrt((x - px) * (x - px) + (z - pz) * (z - pz));
         speed = speed + ((float)Math.min(1.0, moved * 5.0) - speed) * 0.3F;
         float want;
         if (moved > 0.02) {
            want = (float)Math.toDegrees(Math.atan2(-(x - px), z - pz));
         } else {
            want = (float)Math.toDegrees(Math.atan2(-(Mc.x(p) - x), Mc.z(p) - z));
         }

         float diff = wrap(want - yaw);
         yaw += diff * 0.25F;
         float hp = Mc.health(p);
         if (lastHealth >= 0.0F && hp < lastHealth - 0.01F) {
            set(p.getLastHurtByMob() != null ? "angry" : "sad", 3000L);
         }

         lastHealth = hp;
         double vel = Math.sqrt(Math.pow(Mc.x(p) - Mc.lastX(p), 2.0) + Math.pow(Mc.z(p) - Mc.lastZ(p), 2.0));
         if (vel > 0.01 || Mc.isSneaking(p) || Mc.mouseDown(0)) {
            lastActive = now;
         }

         if (now > emotionUntil) {
            String e;
            if (hp <= Mc.maxHealth(p) * 0.3F) {
               e = "sad";
            } else if (p.fallDistance > 4.0) {
               e = "dizzy";
            } else if (Mc.isSneaking(p) && moved < 0.05) {
               e = "love";
            } else if (now - lastActive > 25000L) {
               e = "sleepy";
            } else if (p.isSprinting() && vel > 0.2) {
               e = "excited";
            } else if (now > nextCurious) {
               e = "curious";
               emotionUntil = now + 3500L;
               nextCurious = now + 25000L + (long)(Math.random() * 30000.0);
            } else {
               e = "happy";
            }

            emotion = e;
         }

         if (!emotion.equals(shownEmotion)) {
            shownEmotion = emotion;
            bubbleAt = now;
         }

         if (now > nextBlink) {
            blinkAt = now;
            nextBlink = now + 2200L + (long)(Math.random() * 3200.0);
         }
      } else {
         placed = false;
      }
   }

   private static float wrap(float a) {
      a %= 360.0F;
      if (a > 180.0F) {
         a -= 360.0F;
      }

      if (a < -180.0F) {
         a += 360.0F;
      }

      return a;
   }

   private static double ground(double x, double from, double z, double fallback) {
      ClientLevel w = Mc.world();
      if (w == null) {
         return fallback;
      } else {
         MutableBlockPos pos = new MutableBlockPos();
         int bx = (int)Math.floor(x);
         int bz = (int)Math.floor(z);

         for (int by = (int)Math.floor(from); by > from - 6.0; by--) {
            pos.set(bx, by, bz);
            BlockState st = w.getBlockState(pos);
            VoxelShape sh = st.getCollisionShape(w, pos);
            if (!sh.isEmpty()) {
               double top = by + sh.bounds().maxY;
               if (top <= from + 0.01) {
                  return top;
               }
            }
         }

         return fallback;
      }
   }

   public static void render(float td) {
      Cos.Item it = Cos.equipped(Cos.Slot.PET);
      if (it != null && placed && Mc.player() != null) {
         double rx = px + (x - px) * td;
         double ry = py + (y - py) * td;
         double rz = pz + (z - pz) * td;
         float ry2 = lastYaw + wrap(yaw - lastYaw) * td;
         long now = System.currentTimeMillis();
         Cos.A a = new Cos.A();
         a.time = (float)(now % 3600000L) / 1000.0F;
         a.speed = speed;
         a.growth = growth();
         a.level = level();
         a.emotion = shownEmotion;
         float bt = (float)(now - blinkAt) / 140.0F;
         a.blink = bt < 1.0F ? 1.0F - Math.abs(bt * 2.0F - 1.0F) : 0.0F;
         double lx = R3.camX() - rx;
         double lz = R3.camZ() - rz;
         double ly = R3.camY() - (ry + 0.4);
         double th = Math.toRadians(ry2);
         double fwd = lx * -Math.sin(th) + lz * Math.cos(th);
         double right = lx * -Math.cos(th) + lz * -Math.sin(th);
         double ang = Math.atan2(right, Math.max(0.1, fwd));
         a.lookX = (float)Math.max(-1.0, Math.min(1.0, ang / 1.2));
         a.lookY = (float)Math.max(-1.0, Math.min(1.0, Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) / 1.0));
         int light = CosRender.light(rx, ry + 0.3, rz);
         CosRender.drawWorld(it.model, a, rx, ry, rz, ry2, 0.0F, 0.0F, 0.95F, light);
         long age = now - bubbleAt;
         String bubble = bubbleFor(shownEmotion);
         if (bubble != null && age < 2600L) {
            Identifier tex = BUBBLES.get(bubble);
            if (tex == null) {
               tex = Identifier.fromNamespaceAndPath("legoclient", "pet_bubble/" + bubble);

               try {
                  Tx.register(tex, Bubbles.paint(bubble, 128), 128, 128, true);
               } catch (Throwable var38) {
               }

               BUBBLES.put(bubble, tex);
            }

            float in = Math.min(1.0F, (float)age / 250.0F);
            float out = age > 2200L ? 1.0F - (float)(age - 2200L) / 400.0F : 1.0F;
            double pop = 1.0 + 0.25 * Math.sin(Math.min(1.0, age / 300.0) * Math.PI);
            double h = Pets.height(it.id, a.growth) / 16.0 * 0.95 + 0.35 + Math.sin(now / 300.0) * 0.03;
            R3.billboard(tex, rx, ry + h, rz, 0.42 * in * pop, R3.argb(16777215, Math.max(0.0F, out)));
         }
      }
   }

   private static String bubbleFor(String e) {
      switch (e) {
         case "love":
            return "heart";
         case "sleepy":
            return "sleep";
         case "sad":
            return "cry";
         case "curious":
            return "think";
         case "excited":
            return "laugh";
         case "angry":
            return "fire";
         case "dizzy":
            return "zen";
         default:
            return null;
      }
   }
}
