package dev.lego.hud;

import dev.lego.core.Mc;
import dev.lego.core.Modules;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

public final class HudPack {
   private static int pos = 0;
   static double traveled;
   static double maxY = Double.NaN;
   static double minY = Double.NaN;
   static double lastX = Double.NaN;
   static double lastZ;
   static double damageTaken;
   static double healed;
   static double lastHp = -1.0;
   static double topSpeed;
   static int jumps;
   static int minFps = Integer.MAX_VALUE;
   static int maxFps;
   static long fpsSum;
   static long fpsN;
   static long lastWorldTime = -1L;
   static long lastWorldReal;
   static double tps = 20.0;
   static boolean wasOnGround = true;
   static final long START = System.currentTimeMillis();
   static long swStart = 0L;
   static long swAcc = 0L;

   private HudPack() {
   }

   private static double[] spot() {
      int i = pos++;
      return new double[]{0.02 + i / 16 * 0.19, 0.1 + i % 16 * 0.052};
   }

   private static HudPack.Fn add(String id, String name, String desc, String icon, Supplier<String[]> f) {
      return Modules.register(new HudPack.Fn(id, name, desc, icon, f, spot()));
   }

   private static String[] v(Object value, String unit) {
      return new String[]{String.valueOf(value), unit};
   }

   private static String f1(double d) {
      return String.format(Locale.ROOT, "%.1f", d);
   }

   private static String f2(double d) {
      return String.format(Locale.ROOT, "%.2f", d);
   }

   public static void tick() {
      LocalPlayer p = Mc.player();
      int fps = Mc.fps();
      if (fps > 0 && p != null) {
         minFps = Math.min(minFps, fps);
         maxFps = Math.max(maxFps, fps);
         fpsSum += fps;
         fpsN++;
      }

      if (p == null) {
         lastX = Double.NaN;
         lastHp = -1.0;
      } else {
         double x = Mc.x(p);
         double y = Mc.y(p);
         double z = Mc.z(p);
         if (!Double.isNaN(lastX)) {
            double d = Math.hypot(x - lastX, z - lastZ);
            if (d < 20.0) {
               traveled += d;
               topSpeed = Math.max(topSpeed, d * 20.0 * 3.6);
            }
         }

         lastX = x;
         lastZ = z;
         maxY = Double.isNaN(maxY) ? y : Math.max(maxY, y);
         minY = Double.isNaN(minY) ? y : Math.min(minY, y);
         boolean ground = Mc.onGround(p);
         if (wasOnGround && !ground && Mc.velocity(p)[1] > 0.3) {
            jumps++;
         }

         wasOnGround = ground;
         float hp = Mc.health(p);
         if (lastHp >= 0.0) {
            if (hp < lastHp) {
               damageTaken = damageTaken + (lastHp - hp);
            } else if (hp > lastHp) {
               healed = healed + (hp - lastHp);
            }
         }

         lastHp = hp;
         ClientLevel w = Mc.world();
         if (w != null) {
            long wt = w.getDayTime();
            long now = System.currentTimeMillis();
            if (lastWorldTime >= 0L && wt >= lastWorldTime) {
               if (now - lastWorldReal >= 3000L) {
                  double measured = (wt - lastWorldTime) / ((now - lastWorldReal) / 1000.0);
                  if (wt != lastWorldTime) {
                     tps = tps * 0.6 + Math.min(20.0, measured) * 0.4;
                  }

                  lastWorldTime = wt;
                  lastWorldReal = now;
               }
            } else {
               lastWorldTime = wt;
               lastWorldReal = now;
            }
         }
      }
   }

   private static String dur(long ms) {
      long s = ms / 1000L;
      return s >= 3600L
         ? String.format(Locale.ROOT, "%d:%02d:%02d", s / 3600L, s / 60L % 60L, s % 60L)
         : String.format(Locale.ROOT, "%d:%02d", s / 60L, s % 60L);
   }

   public static void registerAll() {
      add("date", "Datum", "Heutiges Datum", "clock", () -> v(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), ""));
      add("stopwatch", "Stoppuhr", "Läuft, solange das Modul an ist - aus/an setzt sie zurück", "hourglass", () -> {
         if (swStart == 0L) {
            swStart = System.currentTimeMillis();
         }

         return v(dur(System.currentTimeMillis() - swStart), "");
      }).off = () -> swStart = 0L;
      add("tps", "TPS (geschätzt)", "Geschätzte Server-TPS aus der Weltzeit (20 = flüssig)", "pulse", () -> v(f1(tps), "TPS"));
      add(
         "online",
         "Spieler online",
         "Wie viele Spieler auf dem Server sind (Tab-Liste)",
         "users",
         () -> v(Mc.mc().getConnection().getOnlinePlayers().size(), "online")
      );
   }

   static final class Fn extends HudModules.Simple {
      private final Supplier<String[]> f;
      Runnable off;

      @Override
      public void onDisable() {
         if (this.off != null) {
            this.off.run();
         }
      }

      Fn(String id, String name, String desc, String icon, Supplier<String[]> f, double[] at) {
         super("h4_" + id, name, desc, icon, at[0], at[1]);
         this.f = f;
         this.fresh();
      }

      @Override
      void compute() {
         String[] r;
         try {
            r = this.f.get();
         } catch (Throwable var3) {
            r = null;
         }

         if (r == null) {
            r = new String[]{"-", ""};
         }

         this.value = r[0];
         this.unit = r.length > 1 ? r[1] : "";
      }
   }
}
