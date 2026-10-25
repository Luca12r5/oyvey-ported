package de.bauloo.styles;

import de.bauloo.styles.mixin.ParticleManagerInvoker;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.Game;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents.AfterEntities;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_12249;
import net.minecraft.class_124;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1538;
import net.minecraft.class_1657;
import net.minecraft.class_2246;
import net.minecraft.class_2394;
import net.minecraft.class_2398;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2588;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_3419;
import net.minecraft.class_3730;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_5819;
import net.minecraft.class_638;
import net.minecraft.class_703;
import net.minecraft.class_7833;
import net.minecraft.class_1113.class_1114;
import net.minecraft.class_327.class_6415;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class KillEffect {
   public static final String[][] EFFECTS = new String[][]{
      {"lightning", "Lightning"}, {"shockwave", "Shockwave"}, {"pillar", "Light Pillar"}, {"popup", "Kill Popup"}, {"soul", "Soul Rise"}
   };
   private static final long MY_KILL_MS = 5000L;
   private static final Map<Integer, Long> HITS = new HashMap<>();
   private static final Map<String, Long> HIT_NAMES = new HashMap<>();
   private static long lastAnchorUse;
   private static final long ANCHOR_KILL_MS = 3000L;
   public static final Set<class_1297> SILENT = Collections.newSetFromMap(new WeakHashMap<>());
   private static int nextId = -1000000;
   private static int streak;
   private static final Map<Integer, Long> PLAYED = new HashMap<>();
   private static final Map<String, ArrayDeque<class_243>> TRAIL = new HashMap<>();
   private static final int TRAIL_TICKS = 6;
   private static final class_2960 KILL_SOUND = class_2960.method_60655("bauloo_styles", "kill");
   private static final List<double[]> RUNNING = new ArrayList<>();
   private static final int WAVES = 10;
   private static final List<KillEffect.Fx> FX = new ArrayList<>();

   private KillEffect() {
   }

   public static boolean valid(String effect) {
      for (String[] e : EFFECTS) {
         if (e[0].equals(effect)) {
            return true;
         }
      }

      return false;
   }

   public static void onAttack(class_1297 target) {
      if (HITS.size() > 128) {
         HITS.clear();
      }

      HITS.put(target.method_5628(), System.currentTimeMillis());
      if (target instanceof class_1657 player) {
         if (HIT_NAMES.size() > 128) {
            HIT_NAMES.clear();
         }

         HIT_NAMES.put(player.method_5477().getString(), System.currentTimeMillis());
      }
   }

   public static void onDeath(class_1297 entity) {
      StylesUiConfig.load();
      class_310 mc = class_310.method_1551();
      if (entity instanceof class_1657 && entity != mc.field_1724) {
         Long hit = HITS.get(entity.method_5628());
         if (valid(StylesUiConfig.killEffect) || StylesUiConfig.killSound) {
            boolean mine = hit != null && System.currentTimeMillis() - hit <= 5000L;
            if (!StylesUiConfig.killOnlyMine || mine) {
               long now = System.currentTimeMillis();
               Long last = PLAYED.get(entity.method_5628());
               if (last == null || now - last >= 3000L) {
                  if (PLAYED.size() > 64) {
                     PLAYED.clear();
                  }

                  PLAYED.put(entity.method_5628(), now);
                  class_243 now2 = new class_243(entity.method_23317(), entity.method_23318(), entity.method_23321());
                  String name = entity.method_5477().getString();
                  class_243 at = beforeDeath(name, now2);
                  if (mine) {
                     streak++;
                  }

                  if (mine) {
                     killSound();
                  }

                  if (valid(StylesUiConfig.killEffect)) {
                     play(StylesUiConfig.killEffect, at, name, mine ? streak : 0);
                  }
               }
            }
         }
      }
   }

   private static class_243 beforeDeath(String name, class_243 now) {
      ArrayDeque<class_243> trail = TRAIL.get(name);
      if (trail != null && !trail.isEmpty()) {
         class_243[] seen = trail.toArray(new class_243[0]);
         class_243 pick = seen[seen.length - 1];

         for (int i = seen.length - 1; i > 0 && i >= seen.length - 4; i--) {
            if (seen[i].method_1025(seen[i - 1]) > 9.0) {
               return seen[i - 1];
            }

            pick = seen[i - 1];
         }

         return now != null && !(now.method_1025(pick) > 9.0) ? now : pick;
      } else {
         return now;
      }
   }

   private static void track(class_310 client) {
      Set<String> near = new HashSet<>();

      for (class_1657 player : client.field_1687.method_18456()) {
         if (player != client.field_1724 && !(player.method_5858(client.field_1724) > 4096.0)) {
            String name = player.method_5477().getString();
            near.add(name);
            ArrayDeque<class_243> trail = TRAIL.computeIfAbsent(name, k -> new ArrayDeque<>());
            trail.addLast(new class_243(player.method_23317(), player.method_23318(), player.method_23321()));

            while (trail.size() > 6) {
               trail.removeFirst();
            }
         }
      }

      if (TRAIL.size() > 64) {
         TRAIL.keySet().retainAll(near);
      }
   }

   public static void killSound() {
      if (StylesUiConfig.killSound) {
         class_310.method_1551()
            .method_1483()
            .method_4873(
               new class_1109(
                  KILL_SOUND,
                  class_3419.field_15250,
                  StylesUiConfig.killVolume / 100.0F,
                  1.0F,
                  class_1113.method_43221(),
                  false,
                  0,
                  class_1114.field_5478,
                  0.0,
                  0.0,
                  0.0,
                  true
               )
            );
      }
   }

   public static void test() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null) {
         class_243 look = mc.field_1724.method_5828(1.0F);
         class_243 at = mc.field_1724.method_73189().method_1031(look.field_1352 * 3.5, 0.0, look.field_1350 * 3.5);
         String effect = valid(StylesUiConfig.killEffect) ? StylesUiConfig.killEffect : "shockwave";
         killSound();
         play(effect, at, "Test Dummy", Math.max(1, streak + 1));
      }
   }

   private static void onMessage(class_2561 message) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null && mc.field_1687 != null) {
         String me = mc.field_1724.method_5477().getString();
         String victim = null;
         boolean mine = false;
         boolean otherKiller = false;
         if (message.method_10851() instanceof class_2588 t && t.method_11022().startsWith("death.")) {
            Object[] args = t.method_11023();
            if (args.length == 0) {
               return;
            }

            victim = argText(args[0]);

            for (int i = 1; i < args.length; i++) {
               String arg = argText(args[i]);
               if (arg.equals(me)) {
                  mine = true;
               } else if (i == 1 && !arg.isEmpty() && !arg.startsWith("[")) {
                  otherKiller = true;
               }
            }
         } else {
            String plain = message.getString();
            Matcher m = Pattern.compile("^(\\w{1,16}) (?:was|got|has been) .* by (\\w{1,16})").matcher(plain);
            if (!m.find()) {
               return;
            }

            victim = m.group(1);
            mine = m.group(2).equals(me);
            otherKiller = !mine;
         }

         if (victim != null) {
            if (victim.equals(me)) {
               streak = 0;
            } else {
               if (!mine && !otherKiller) {
                  long now = System.currentTimeMillis();
                  Long hit = HIT_NAMES.get(victim);
                  if (hit != null && now - hit <= 5000L) {
                     mine = true;
                  } else if (now - lastAnchorUse <= 3000L) {
                     class_243 near = beforeDeath(victim, null);
                     mine = near != null && near.method_1025(mc.field_1724.method_73189()) < 144.0;
                  }
               }

               for (class_1657 player : mc.field_1687.method_18456()) {
                  if (player.method_5477().getString().equals(victim)) {
                     Long lastHit = HITS.get(player.method_5628());
                     if (mine && (lastHit == null || System.currentTimeMillis() - lastHit > 5000L)) {
                        HITS.put(player.method_5628(), System.currentTimeMillis());
                     }

                     if (otherKiller) {
                        HITS.remove(player.method_5628());
                     }

                     onDeath(player);
                     return;
                  }
               }

               if (TRAIL.containsKey(victim) && (mine || !StylesUiConfig.killOnlyMine)) {
                  class_243 at = beforeDeath(victim, null);
                  playOnce(-victim.hashCode(), at, victim, mine);
               }
            }
         }
      }
   }

   private static String argText(Object arg) {
      return arg instanceof class_2561 text ? text.getString() : String.valueOf(arg);
   }

   private static void playOnce(int id, class_243 at, String name, boolean mine) {
      StylesUiConfig.load();
      if (valid(StylesUiConfig.killEffect) || StylesUiConfig.killSound) {
         long now = System.currentTimeMillis();
         Long last = PLAYED.get(id);
         if (last == null || now - last >= 3000L) {
            PLAYED.put(id, now);
            if (mine) {
               streak++;
            }

            if (mine) {
               killSound();
            }

            if (valid(StylesUiConfig.killEffect)) {
               play(StylesUiConfig.killEffect, at, name, mine ? streak : 0);
            }
         }
      }
   }

   public static void register() {
      UseBlockCallback.EVENT.register((UseBlockCallback)(player, world, hand, hit) -> {
         if (player == class_310.method_1551().field_1724 && hit != null && world.method_8320(hit.method_17777()).method_27852(class_2246.field_23152)) {
            lastAnchorUse = System.currentTimeMillis();
         }

         return class_1269.field_5811;
      });
      ClientReceiveMessageEvents.GAME.register((Game)(message, overlay) -> {
         if (!overlay) {
            try {
               onMessage(message);
            } catch (RuntimeException e) {
               BaulooStylesClient.LOGGER.warn("Kill effect: could not read a message: {}", e.toString());
            }
         }
      });
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         if (client.field_1687 != null && client.field_1724 != null) {
            track(client);
            if (client.field_1724.method_29504()) {
               streak = 0;
            }
         }

         if (!RUNNING.isEmpty()) {
            if (client.field_1687 == null) {
               RUNNING.clear();
            } else {
               Iterator<double[]> it = RUNNING.iterator();

               while (it.hasNext()) {
                  double[] run = it.next();
                  int left = (int)run[3];
                  lightningWave(run[0], run[1], run[2], 10 - left);
                  if (left <= 1) {
                     it.remove();
                  } else {
                     run[3] = left - 1;
                  }
               }
            }
         }
      });
      WorldRenderEvents.AFTER_ENTITIES.register((AfterEntities)context -> {
         if (!FX.isEmpty()) {
            try {
               class_4587 matrices = context.matrices();
               class_4597 consumers = context.consumers();
               class_243 cam = context.worldState().field_63082.field_63078;
               Quaternionf look = context.worldState().field_63082.field_63081;
               if (matrices != null && consumers != null && cam != null && look != null) {
                  render(matrices, consumers, cam, look);
               }
            } catch (Throwable e) {
               FX.clear();
            }
         }
      });
   }

   public static void play(String effect, class_243 at, String name, int kills) {
      class_310 mc = class_310.method_1551();
      class_638 world = mc.field_1687;
      if (world != null) {
         if ("lightning".equals(effect)) {
            class_1538 bolt = (class_1538)class_1299.field_6112.method_5883(world, class_3730.field_16461);
            if (bolt != null) {
               bolt.method_5838(nextId--);
               bolt.method_24203(at.field_1352, at.field_1351, at.field_1350);
               bolt.method_29498(true);
               SILENT.add(bolt);
               world.method_53875(bolt);
            }

            lightningWave(at.field_1352, at.field_1351, at.field_1350, 0);
            RUNNING.add(new double[]{at.field_1352, at.field_1351, at.field_1350, 9.0});
         } else {
            if (FX.size() > 24) {
               FX.remove(0);
            }

            if (!"shatter".equals(effect)) {
               FX.add(new KillEffect.Fx(effect, at, name, kills, StylesUiConfig.killColor & 16777215));
            }
         }
      }
   }

   private static void spawn(class_638 world, class_2394 effect, double x, double y, double z, double vx, double vy, double vz) {
      class_310 mc = class_310.method_1551();

      try {
         class_703 particle = ((ParticleManagerInvoker)mc.field_1713).baulooStyles$create(effect, x, y, z, vx, vy, vz);
         if (particle != null) {
            mc.field_1713.method_3058(particle);
            return;
         }
      } catch (RuntimeException var16) {
      }

      world.method_8466(effect, true, true, x, y, z, vx, vy, vz);
   }

   private static void lightningWave(double x, double y, double z, int n) {
      class_638 world = class_310.method_1551().field_1687;
      if (world != null) {
         class_5819 r = world.method_8409();
         boolean first = n == 0;

         for (int i = 0; i < (first ? 50 : 12); i++) {
            spawn(
               world,
               class_2398.field_29644,
               x + r.method_43059() * 0.5,
               y + r.method_43058() * 2.2,
               z + r.method_43059() * 0.5,
               r.method_43059() * 0.3,
               r.method_43058() * 0.3,
               r.method_43059() * 0.3
            );
         }

         for (int i = 0; i < (first ? 24 : 4); i++) {
            spawn(
               world,
               class_2398.field_11207,
               x + r.method_43059() * 0.4,
               y + r.method_43058() * 2.0,
               z + r.method_43059() * 0.4,
               r.method_43059() * 0.05,
               r.method_43058() * 0.08,
               r.method_43059() * 0.05
            );
         }
      }
   }

   private static void render(class_4587 matrices, class_4597 consumers, class_243 cam, Quaternionf look) {
      long now = System.currentTimeMillis();
      FX.removeIf(fxx -> now - fxx.start > fxx.life());

      for (KillEffect.Fx fx : FX) {
         float t = (float)(now - fx.start) / (float)fx.life();
         float ms = (float)(now - fx.start);
         matrices.method_22903();
         matrices.method_22904(fx.x - cam.field_1352, fx.y - cam.field_1351, fx.z - cam.field_1350);
         switch (fx.type) {
            case "shockwave":
               shockwave(matrices, consumers, fx, ms);
               break;
            case "pillar":
               pillar(matrices, consumers, fx, t);
               break;
            case "popup":
               popup(matrices, consumers, fx, ms, look);
               break;
            case "shatter":
               shatter(matrices, consumers, fx, ms / 1000.0F);
               break;
            default:
               soul(matrices, consumers, fx, t, look);
         }

         matrices.method_22909();
      }
   }

   private static void shockwave(class_4587 matrices, class_4597 consumers, KillEffect.Fx fx, float ms) {
      class_4588 glow = consumers.method_73477(class_12249.method_76003());
      Matrix4f m = matrices.method_23760().method_23761();

      for (int ring = 0; ring < 2; ring++) {
         float t = (ms - ring * 180) / 900.0F;
         if (!(t <= 0.0F) && !(t >= 1.0F)) {
            float radius = 0.4F + (ring == 0 ? 5.0F : 3.6F) * easeOut(t);
            float fade = (float)Math.pow(1.0F - t, 1.4);
            float width = 0.18F + 0.5F * (1.0F - t);
            int bright = argb(fade, ring == 0 ? fx.color : lighten(fx.color, 0.5F));
            int none = argb(0.0F, fx.color);
            annulus(glow, m, 0.03F, radius - width, radius, none, bright);
            annulus(glow, m, 0.03F, radius, radius + width * 0.6F, bright, none);
            wall(glow, m, radius, 0.03F, 0.03F + 0.9F * (1.0F - t), bright, none);
         }
      }

      float flash = 1.0F - ms / 250.0F;
      if (flash > 0.0F) {
         annulus(glow, m, 0.04F, 0.0F, 1.6F, argb(flash, 16777215), argb(0.0F, fx.color));
      }
   }

   private static void pillar(class_4587 matrices, class_4597 consumers, KillEffect.Fx fx, float t) {
      class_4588 glow = consumers.method_73477(class_12249.method_76003());
      Matrix4f m = matrices.method_23760().method_23761();
      float grow = easeOut(Math.min(1.0F, t / 0.12F));
      float height = 2.0F + 40.0F * grow;
      float fade = t < 0.25F ? 1.0F : 1.0F - (t - 0.25F) / 0.75F;
      fade *= fade;
      float thin = 1.0F - 0.6F * t;
      matrices.method_22903();
      matrices.method_22907(class_7833.field_40716.rotationDegrees(t * 240.0F));
      m = matrices.method_23760().method_23761();
      tube(glow, m, 0.9F * thin, height, argb(fade * 0.25F, fx.color), argb(0.0F, fx.color));
      tube(glow, m, 0.45F * thin, height, argb(fade * 0.7F, fx.color), argb(0.0F, fx.color));
      tube(glow, m, 0.16F * thin, height, argb(fade, 16777215), argb(fade * 0.2F, 16777215));
      matrices.method_22909();
      m = matrices.method_23760().method_23761();
      annulus(glow, m, 0.03F, 0.0F, 1.8F * thin, argb(fade * 0.8F, fx.color), argb(0.0F, fx.color));
      if (t < 0.35F) {
         float k = t / 0.35F;
         float r = 0.5F + 2.5F * easeOut(k);
         annulus(glow, m, 0.05F + k * 0.3F, r - 0.15F, r + 0.15F, argb(1.0F - k, 16777215), argb(0.0F, fx.color));
      }
   }

   private static void popup(class_4587 matrices, class_4597 consumers, KillEffect.Fx fx, float ms, Quaternionf look) {
      class_327 font = class_310.method_1551().field_1772;
      float t = ms / (float)fx.life();
      float pop = ms < 180.0F ? 1.6F - 0.6F * easeOut(ms / 180.0F) : 1.0F;
      float fade = t > 0.75F ? 1.0F - (t - 0.75F) / 0.25F : Math.min(1.0F, ms / 120.0F);
      float rise = 2.3F + 0.9F * easeOut(t);
      matrices.method_22903();
      matrices.method_46416(0.0F, rise, 0.0F);
      matrices.method_22907(look);
      float s = 0.045F * pop;
      matrices.method_22905(s, -s, s);
      class_2561 big = class_2561.method_43470(fx.streak > 0 ? "+1 KILL" : "KILL").method_27692(class_124.field_1067);
      label(font, matrices, consumers, big, 0.0F, fx.color, fade);
      matrices.method_22905(0.55F, 0.55F, 0.55F);
      label(font, matrices, consumers, class_2561.method_43470(fx.name), 20.0F, 16777215, fade);
      if (fx.streak >= 2) {
         class_2561 row = class_2561.method_43470(fx.streak + "x STREAK").method_27692(class_124.field_1067);
         label(font, matrices, consumers, row, 32.0F, lighten(fx.color, 0.45F), fade);
      }

      matrices.method_22909();
   }

   private static void label(class_327 font, class_4587 matrices, class_4597 consumers, class_2561 text, float y, int rgb, float fade) {
      int alpha = Math.max(5, Math.round(fade * 255.0F));
      float x = -font.method_27525(text) / 2.0F;
      Matrix4f m = matrices.method_23760().method_23761();
      font.method_27522(text, x + 1.0F, y + 1.0F, Math.max(5, alpha / 2) << 24 | darken(rgb), false, m, consumers, class_6415.field_33994, 0, 15728880);
      matrices.method_22903();
      matrices.method_46416(0.0F, 0.0F, -0.02F);
      font.method_27522(text, x, y, alpha << 24 | rgb, false, matrices.method_23760().method_23761(), consumers, class_6415.field_33994, 0, 15728880);
      matrices.method_22909();
   }

   private static void shatter(class_4587 matrices, class_4597 consumers, KillEffect.Fx fx, float sec) {
      class_4588 solid = consumers.method_73477(class_12249.method_76023());
      class_4588 glow = consumers.method_73477(class_12249.method_76003());
      float gravity = 16.0F;
      float fade = sec > 1.9F ? 1.0F - (sec - 1.9F) / 0.7F : 1.0F;

      for (float[] b : fx.bits) {
         float land = landTime(b[1], b[4], gravity);
         float ft = Math.min(sec, land);
         float px = b[0] + b[3] * ft * 0.6F;
         float pz = b[2] + b[5] * ft * 0.6F;
         float py = Math.max(0.0F, b[1] + b[4] * ft - 0.5F * gravity * ft * ft);
         float size = b[6] * (sec > 1.9F ? fade : 1.0F);
         if (!(size <= 0.005F)) {
            int rgb = b[7] < 0.25F ? 16777215 : (b[7] < 0.6F ? fx.color : darken(fx.color));
            matrices.method_22903();
            matrices.method_46416(px, py + size / 2.0F, pz);
            matrices.method_22907(class_7833.field_40716.rotationDegrees(b[8] * ft));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(b[8] * 0.7F * ft));
            Matrix4f m = matrices.method_23760().method_23761();
            cube(solid, m, size / 2.0F, argb(fade, rgb));
            if (sec < land) {
               cube(glow, m, size * 0.9F, argb(0.25F * fade, fx.color));
            }

            matrices.method_22909();
         }
      }

      if (sec < 0.3F) {
         Matrix4f m = matrices.method_23760().method_23761();
         float k = sec / 0.3F;
         annulus(glow, m, 0.03F, 0.0F, 1.2F + k * 1.5F, argb(1.0F - k, 16777215), argb(0.0F, fx.color));
      }
   }

   private static void soul(class_4587 matrices, class_4597 consumers, KillEffect.Fx fx, float t, Quaternionf look) {
      class_4588 glow = consumers.method_73477(class_12249.method_76003());
      float rise = 3.2F * easeIn(t);
      float fade = t < 0.1F ? t / 0.1F : 1.0F - (t - 0.1F) / 0.9F;
      float sway = (float)Math.sin(t * Math.PI * 3.0) * 6.0F;
      Vector3f forward = new Vector3f(0.0F, 0.0F, -1.0F).rotate(look);
      float yaw = (float)Math.toDegrees(Math.atan2(forward.x, forward.z));
      matrices.method_22903();
      matrices.method_46416(0.0F, rise, 0.0F);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(yaw + 180.0F + sway));
      float px = 0.05875F;
      int body = argb(0.55F * fade, fx.color);
      int shell = argb(0.18F * fade, lighten(fx.color, 0.4F));
      float arms = 20.0F + 110.0F * easeOut(Math.min(1.0F, t * 2.0F));
      part(matrices, glow, 0.0F, 28.0F, 0.0F, 4.0F, 4.0F, 4.0F, px, body, shell);
      part(matrices, glow, 0.0F, 18.0F, 0.0F, 4.0F, 6.0F, 2.0F, px, body, shell);
      part(matrices, glow, -2.0F, 6.0F, 0.0F, 2.0F, 6.0F, 2.0F, px, body, shell);
      part(matrices, glow, 2.0F, 6.0F, 0.0F, 2.0F, 6.0F, 2.0F, px, body, shell);

      for (int side = -1; side <= 1; side += 2) {
         matrices.method_22903();
         matrices.method_46416(side * 6 * px, 23.0F * px, 0.0F);
         matrices.method_22907(class_7833.field_40718.rotationDegrees(side * arms));
         part(matrices, glow, 0.0F, -5.0F, 0.0F, 2.0F, 6.0F, 2.0F, px, body, shell);
         matrices.method_22909();
      }

      matrices.method_22909();
      Matrix4f m = matrices.method_23760().method_23761();
      annulus(glow, m, 0.03F, 0.0F, 1.3F, argb(0.6F * (1.0F - t), fx.color), argb(0.0F, fx.color));
   }

   private static void part(class_4587 matrices, class_4588 glow, float cx, float cy, float cz, float hx, float hy, float hz, float px, int body, int shell) {
      matrices.method_22903();
      matrices.method_46416(cx * px, cy * px, cz * px);
      Matrix4f m = matrices.method_23760().method_23761();
      box(glow, m, hx * px, hy * px, hz * px, body);
      box(glow, m, (hx + 0.6F) * px, (hy + 0.6F) * px, (hz + 0.6F) * px, shell);
      matrices.method_22909();
   }

   private static void annulus(class_4588 b, Matrix4f m, float y, float r1, float r2, int c1, int c2) {
      int seg = 48;

      for (int i = 0; i < seg; i++) {
         double a0 = (Math.PI * 2) * i / seg;
         double a1 = (Math.PI * 2) * (i + 1) / seg;
         float c0 = (float)Math.cos(a0);
         float s0 = (float)Math.sin(a0);
         float cc = (float)Math.cos(a1);
         float s1 = (float)Math.sin(a1);
         quad(b, m, c0 * r1, y, s0 * r1, c1, cc * r1, y, s1 * r1, c1, cc * r2, y, s1 * r2, c2, c0 * r2, y, s0 * r2, c2);
      }
   }

   private static void wall(class_4588 b, Matrix4f m, float r, float y1, float y2, int c1, int c2) {
      int seg = 48;

      for (int i = 0; i < seg; i++) {
         double a0 = (Math.PI * 2) * i / seg;
         double a1 = (Math.PI * 2) * (i + 1) / seg;
         float x0 = (float)Math.cos(a0) * r;
         float z0 = (float)Math.sin(a0) * r;
         float x1 = (float)Math.cos(a1) * r;
         float z1 = (float)Math.sin(a1) * r;
         quad(b, m, x0, y1, z0, c1, x1, y1, z1, c1, x1, y2, z1, c2, x0, y2, z0, c2);
      }
   }

   private static void tube(class_4588 b, Matrix4f m, float r, float h, int bottom, int top) {
      int seg = 12;

      for (int i = 0; i < seg; i++) {
         double a0 = (Math.PI * 2) * i / seg;
         double a1 = (Math.PI * 2) * (i + 1) / seg;
         float x0 = (float)Math.cos(a0) * r;
         float z0 = (float)Math.sin(a0) * r;
         float x1 = (float)Math.cos(a1) * r;
         float z1 = (float)Math.sin(a1) * r;
         quad(b, m, x0, 0.0F, z0, bottom, x1, 0.0F, z1, bottom, x1, h, z1, top, x0, h, z0, top);
      }
   }

   private static void box(class_4588 b, Matrix4f m, float hx, float hy, float hz, int c) {
      quad(b, m, -hx, -hy, -hz, c, hx, -hy, -hz, c, hx, hy, -hz, c, -hx, hy, -hz, c);
      quad(b, m, -hx, -hy, hz, c, hx, -hy, hz, c, hx, hy, hz, c, -hx, hy, hz, c);
      quad(b, m, -hx, -hy, -hz, c, -hx, -hy, hz, c, -hx, hy, hz, c, -hx, hy, -hz, c);
      quad(b, m, hx, -hy, -hz, c, hx, -hy, hz, c, hx, hy, hz, c, hx, hy, -hz, c);
      quad(b, m, -hx, hy, -hz, c, hx, hy, -hz, c, hx, hy, hz, c, -hx, hy, hz, c);
      quad(b, m, -hx, -hy, -hz, c, hx, -hy, -hz, c, hx, -hy, hz, c, -hx, -hy, hz, c);
   }

   private static void cube(class_4588 b, Matrix4f m, float h, int c) {
      box(b, m, h, h, h, c);
   }

   private static void quad(
      class_4588 b,
      Matrix4f m,
      float x1,
      float y1,
      float z1,
      int c1,
      float x2,
      float y2,
      float z2,
      int c2,
      float x3,
      float y3,
      float z3,
      int c3,
      float x4,
      float y4,
      float z4,
      int c4
   ) {
      b.method_22918(m, x1, y1, z1).method_39415(c1);
      b.method_22918(m, x2, y2, z2).method_39415(c2);
      b.method_22918(m, x3, y3, z3).method_39415(c3);
      b.method_22918(m, x4, y4, z4).method_39415(c4);
      b.method_22918(m, x4, y4, z4).method_39415(c4);
      b.method_22918(m, x3, y3, z3).method_39415(c3);
      b.method_22918(m, x2, y2, z2).method_39415(c2);
      b.method_22918(m, x1, y1, z1).method_39415(c1);
   }

   private static float landTime(float y0, float v, float g) {
      return (float)((v + Math.sqrt(v * v + 2.0F * g * y0)) / g);
   }

   private static int argb(float alpha, int rgb) {
      int a = Math.round(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F);
      return a << 24 | rgb & 16777215;
   }

   private static int lighten(int rgb, float k) {
      int r = rgb >> 16 & 0xFF;
      int g = rgb >> 8 & 0xFF;
      int b = rgb & 0xFF;
      r += Math.round((255 - r) * k);
      g += Math.round((255 - g) * k);
      b += Math.round((255 - b) * k);
      return r << 16 | g << 8 | b;
   }

   private static int darken(int rgb) {
      return rgb >> 1 & 8355711;
   }

   private static float easeOut(float t) {
      float k = 1.0F - Math.max(0.0F, Math.min(1.0F, t));
      return 1.0F - k * k * k;
   }

   private static float easeIn(float t) {
      float k = Math.max(0.0F, Math.min(1.0F, t));
      return k * k;
   }

   private static final class Fx {
      final String type;
      final double x;
      final double y;
      final double z;
      final long start = System.currentTimeMillis();
      final String name;
      final int streak;
      final int color;
      float[][] bits;

      Fx(String type, class_243 at, String name, int streak, int color) {
         this.type = type;
         this.x = at.field_1352;
         this.y = at.field_1351;
         this.z = at.field_1350;
         this.name = name;
         this.streak = streak;
         this.color = color;
      }

      long life() {
         return switch (this.type) {
            case "shockwave" -> 1100L;
            case "pillar" -> 1600L;
            case "popup" -> 2400L;
            case "shatter" -> 2600L;
            default -> 2200L;
         };
      }

      void makeBits(class_5819 r) {
         this.bits = new float[70][];

         for (int i = 0; i < this.bits.length; i++) {
            double a = r.method_43058() * Math.PI * 2.0;
            double speed = 1.2 + r.method_43058() * 3.2;
            this.bits[i] = new float[]{
               (float)(r.method_43059() * 0.25),
               (float)(0.2 + r.method_43058() * 1.6),
               (float)(r.method_43059() * 0.25),
               (float)(Math.cos(a) * speed),
               (float)(2.5 + r.method_43058() * 4.5),
               (float)(Math.sin(a) * speed),
               (float)(0.07 + r.method_43058() * 0.11),
               (float)r.method_43058(),
               (float)(r.method_43059() * 720.0)
            };
         }
      }
   }
}
