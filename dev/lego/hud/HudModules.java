package dev.lego.hud;

import dev.lego.LegoClient;
import dev.lego.core.Mc;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Fonts;
import dev.lego.ui.UiSettings;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D.Double;
import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

public final class HudModules {
   private HudModules() {
   }

   public static void registerAll() {
      Modules.register(new HudModules.InfoBar());
      Modules.register(new HudModules.Fps());
      Modules.register(new HudModules.Ping());
      Modules.register(new HudModules.Cps());
      Modules.register(new HudModules.Coords());
      Modules.register(new HudModules.Direction());
      Modules.register(new HudModules.Clock());
      Modules.register(new HudModules.GameTime());
      Modules.register(new HudModules.Session());
      Modules.register(new HudModules.Speed());
      Modules.register(new HudModules.Memory());
      Modules.register(new HudModules.Server());
      Modules.register(new HudModules.Armor());
      Modules.register(new HudModules.Effects());
      Modules.register(new HudModules.Keystrokes());
      Modules.register(new HudModules.Totems());
      Modules.register(new HudModules.PvpItems());
      Modules.register(new HudModules.Day());
      Modules.register(new HudModules.Biome());
      Modules.register(new HudModules.Light());
      Modules.register(new HudModules.TargetHud());
      Modules.register(new HudModules.Hunger());
      Modules.register(new HudModules.Health());
      Modules.register(new HudModules.Combo());
      Modules.register(new HudModules.InventoryHud());
   }

   static String sessionTime() {
      long s = LegoClient.sessionSeconds();
      return s >= 3600L ? String.format("%02d:%02d:%02d", s / 3600L, s % 3600L / 60L, s % 60L) : String.format("%02d:%02d", s / 60L, s % 60L);
   }

   static void drawItem(GuiGraphics ctx, ItemStack s, double x, double y, double k) {
      Matrix3x2fStack m = ctx.pose();
      m.pushMatrix();
      m.translate((float)x, (float)y);
      m.scale((float)k, (float)k);
      ctx.renderItem(s, 0, 0);
      m.popMatrix();
   }

   static final class Armor extends HudModule {
      private final Setting.Bool hand = this.add(new Setting.Bool("Hauptitem zeigen", true));
      private final Setting.Mode layout = this.add(new Setting.Mode("Anordnung", 0, "Senkrecht", "Waagerecht"));
      private volatile String[] pct = new String[5];
      private volatile int[] pctVal = new int[5];
      private final ItemStack[] stacks = new ItemStack[5];

      Armor() {
         super("armor", "Rüstung", "Rüstung mit Haltbarkeit in Prozent", 0.006, 0.72);
         this.enabled = true;
         this.icon("armor");
      }

      private boolean horiz() {
         return this.layout.index == 1;
      }

      private int n() {
         return this.hand.get() ? 5 : 4;
      }

      @Override
      public double w() {
         return this.horiz() ? this.n() * 22 + 4 : 44.0;
      }

      @Override
      public double h() {
         return this.horiz() ? 30.0 : this.n() * 19 + 3;
      }

      @Override
      public boolean visible() {
         for (int i = 0; i < 4; i++) {
            if (!Mc.empty(Mc.armor(39 - i))) {
               return true;
            }
         }

         return false;
      }

      @Override
      public String key() {
         StringBuilder k = new StringBuilder();
         String[] p = new String[5];
         int[] pv = new int[5];

         for (int i = 0; i < 5; i++) {
            ItemStack s;
            if (i < 4) {
               s = Mc.armor(39 - i);
            } else {
               s = this.hand.get() && Mc.player() != null ? Mc.player().getMainHandItem() : null;
            }

            this.stacks[i] = s;
            pv[i] = 100;
            if (Mc.empty(s)) {
               p[i] = "";
               k.append("-");
            } else {
               if (Mc.damageable(s) && Mc.maxDamage(s) > 0) {
                  int pc = (int)Math.round(100.0 * (Mc.maxDamage(s) - Mc.damage(s)) / Mc.maxDamage(s));
                  p[i] = pc + "%";
                  pv[i] = pc;
               } else {
                  p[i] = Mc.count(s) > 1 ? String.valueOf(Mc.count(s)) : "";
               }

               k.append(Mc.itemKey(s)).append(p[i]).append(';');
            }
         }

         this.pct = p;
         this.pctVal = pv;
         return k.toString() + this.layout.index;
      }

      @Override
      public void paint(Graphics2D g, Color accent) {
         String[] p = this.pct;
         int[] pv = this.pctVal;

         for (int i = 0; i < this.n(); i++) {
            if (p[i] != null && !p[i].isEmpty()) {
               int v = pv[i];
               Color c = v > 60 ? H.TXT : (v > 25 ? new Color(255, 205, 3) : new Color(255, 80, 80));
               if (this.horiz()) {
                  H.center(g, p[i], 2 + i * 22 + 11, 27.5, 3, 5.8F, c);
               } else {
                  H.text(g, p[i], 21.0, i * 19 + 13, 3, 6.4F, c);
               }
            }
         }
      }

      @Override
      public void overlay(GuiGraphics ctx, int x, int y, double k) {
         for (int i = 0; i < this.n(); i++) {
            ItemStack s = this.stacks[i];
            if (!Mc.empty(s)) {
               if (this.horiz()) {
                  HudModules.drawItem(ctx, s, x + (2 + i * 22 + 3) * k, y + 3.0 * k, k);
               } else {
                  HudModules.drawItem(ctx, s, x + 3.0 * k, y + (i * 19 + 2) * k, k);
               }
            }
         }
      }
   }

   static final class Biome extends HudModules.Simple {
      Biome() {
         super("biome", "Biom", "Aktuelles Biom", "leaf", 0.006, 0.52);
      }

      @Override
      void compute() {
         String b = Mc.biome().replace('_', ' ');
         this.value = b.isEmpty() ? "-" : Character.toUpperCase(b.charAt(0)) + b.substring(1);
         this.unit = "";
      }
   }

   public static final class ClickCounter {
      private static final ArrayDeque<Long> L = new ArrayDeque<>();
      private static final ArrayDeque<Long> R = new ArrayDeque<>();
      private static boolean lastL;
      private static boolean lastR;

      public static void frame() {
         boolean l = Mc.mouseDown(0);
         boolean r = Mc.mouseDown(1);
         long now = System.currentTimeMillis();
         boolean count = !Mc.screenOpen();
         if (l && !lastL && count) {
            L.add(now);
         }

         if (r && !lastR && count) {
            R.add(now);
         }

         lastL = l;
         lastR = r;

         while (!L.isEmpty() && now - L.peekFirst() > 1000L) {
            L.pollFirst();
         }

         while (!R.isEmpty() && now - R.peekFirst() > 1000L) {
            R.pollFirst();
         }
      }

      public static int left() {
         return L.size();
      }

      public static int right() {
         return R.size();
      }
   }

   static final class Clock extends HudModules.Simple {
      private final Setting.Bool seconds = this.add(new Setting.Bool("Sekunden", false));
      private final Setting.Bool h12 = this.add(new Setting.Bool("12-Stunden (AM/PM)", false));

      Clock() {
         super("clock", "Uhrzeit", "Echte Uhrzeit", "clock", 0.006, 0.205);
      }

      @Override
      void compute() {
         LocalTime t = LocalTime.now();
         int h = t.getHour();
         String ampm = "";
         if (this.h12.get()) {
            ampm = h < 12 ? "AM" : "PM";
            h = h % 12 == 0 ? 12 : h % 12;
         }

         this.value = this.seconds.get() ? String.format("%02d:%02d:%02d", h, t.getMinute(), t.getSecond()) : String.format("%02d:%02d", h, t.getMinute());
         this.unit = ampm;
      }
   }

   public static final class CombatTracker {
      private static Entity last;
      private static long lastAt;
      private static int combo;
      private static double reach;

      public static void onAttack(Entity target) {
         LocalPlayer p = Mc.player();
         long now = System.currentTimeMillis();
         if (target == last && now - lastAt < 2500L) {
            combo++;
         } else {
            combo = 1;
         }

         last = target;
         lastAt = now;
         if (p != null) {
            reach = Math.sqrt(p.distanceToSqr(target)) - Mc.width(target) / 2.0;
         }
      }

      public static Entity target() {
         Entity looked = Mc.mc().crosshairPickEntity;
         if (looked instanceof Player) {
            return looked;
         } else if (last != null && System.currentTimeMillis() - lastAt < 8000L && last.isAlive()) {
            return last;
         } else {
            return looked instanceof LivingEntity ? looked : null;
         }
      }

      public static int combo() {
         return System.currentTimeMillis() - lastAt > 3000L ? 0 : combo;
      }

      public static double lastReach() {
         return Math.max(0.0, reach);
      }

      public static Entity lastTarget() {
         return last;
      }

      public static long lastAttackAt() {
         return lastAt;
      }
   }

   static final class Combo extends HudModules.Simple {
      Combo() {
         super("combo", "Combo & Reichweite", "Treffer in Folge und Abstand beim letzten Schlag", "target", 0.006, 0.61);
      }

      @Override
      void compute() {
         this.value = HudModules.CombatTracker.combo() + "x";
         this.unit = String.format(Locale.ROOT, "%.2f m", HudModules.CombatTracker.lastReach());
      }
   }

   static final class Coords extends HudModule {
      private final Setting.Bool nether = this.add(new Setting.Bool("Nether-Umrechnung", true));
      private final Setting.Bool facing = this.add(new Setting.Bool("Richtung", true));
      private volatile String l1 = "";
      private volatile String l2 = "";
      private volatile String dir = "";

      Coords() {
         super("coords", "Koordinaten", "X Y Z, Richtung und Nether-Umrechnung", 0.006, 0.66);
         this.enabled = true;
         this.icon("pin");
      }

      @Override
      public double w() {
         double a = H.w(this.l1, 3, 8.0F) + (this.facing.get() ? H.w(this.dir, 2, 7.0F) + 8.0 : 0.0);
         double b = this.nether.get() ? H.w(this.l2, 1, 6.8F) : 0.0;
         return Math.ceil((Math.max(a, b) + (icons() ? 13 : 0) + 16.0) / 4.0) * 4.0;
      }

      @Override
      public double h() {
         return this.nether.get() ? 27.0 : 16.0;
      }

      @Override
      public String key() {
         LocalPlayer p = Mc.player();
         if (p == null) {
            this.l1 = "-";
            this.l2 = "";
            return "none";
         } else {
            int x = (int)Math.floor(Mc.x(p));
            int y = (int)Math.floor(Mc.y(p));
            int z = (int)Math.floor(Mc.z(p));
            this.l1 = x + "  " + y + "  " + z;
            boolean inNether = false;

            try {
               inNether = Mc.world().dimension().identifier().getPath().contains("nether");
            } catch (Throwable var8) {
            }

            this.l2 = inNether ? "Oberwelt  " + x * 8 + "  " + z * 8 : "Nether  " + Math.floorDiv(x, 8) + "  " + Math.floorDiv(z, 8);
            float yaw = (Mc.yaw(p) % 360.0F + 360.0F) % 360.0F;
            String[] d = new String[]{"S", "SW", "W", "NW", "N", "NO", "O", "SO"};
            this.dir = d[Math.round(yaw / 45.0F) % 8];
            return this.l1 + "|" + (this.nether.get() ? this.l2 : "") + "|" + (this.facing.get() ? this.dir : "") + icons();
         }
      }

      @Override
      public void paint(Graphics2D g, Color accent) {
         double x = 7.0;
         if (icons()) {
            H.icon(g, "pin", x + 4.5, 8.0, 9.0, accent);
            x += 13.0;
         }

         H.text(g, this.l1, x, H.base(0.0, 16.0, 8.0F), 3, 8.0F, H.TXT);
         if (this.facing.get()) {
            H.text(g, this.dir, x + H.w(this.l1, 3, 8.0F) + 7.0, H.base(0.0, 16.0, 8.0F), 2, 7.0F, accent);
         }

         if (this.nether.get()) {
            H.text(g, this.l2, x, 22.5, 1, 6.8F, H.SUB);
         }
      }
   }

   static final class Cps extends HudModules.Simple {
      Cps() {
         super("cps", "CPS", "Klicks pro Sekunde (links | rechts)", "mouse", 0.006, 0.16);
      }

      @Override
      void compute() {
         this.value = HudModules.ClickCounter.left() + " | " + HudModules.ClickCounter.right();
         this.unit = "CPS";
      }
   }

   static final class Day extends HudModules.Simple {
      Day() {
         super("day", "Tag-Zähler", "Wie viele Minecraft-Tage vergangen sind", "sun", 0.006, 0.475);
      }

      @Override
      void compute() {
         this.value = "Tag " + (Mc.timeOfDay() / 24000L + 1L);
         this.unit = "";
      }
   }

   static final class Direction extends HudModule {
      private volatile float yaw;

      Direction() {
         super("direction", "Kompass", "Himmelsrichtung als Leiste oben", 0.5, 0.006);
         this.icon("compass");
      }

      @Override
      public double w() {
         return 150.0;
      }

      @Override
      public double h() {
         return 17.0;
      }

      @Override
      public String key() {
         LocalPlayer p = Mc.player();
         this.yaw = p == null ? 0.0F : Mc.yaw(p);
         return String.valueOf(Math.round(this.yaw * 2.0F));
      }

      @Override
      public void paint(Graphics2D g, Color accent) {
         double w = this.w();
         double yw = (this.yaw % 360.0F + 360.0F) % 360.0F;
         Shape old = g.getClip();
         g.clip(new Double(4.0, 0.0, w - 8.0, 17.0, 8.0, 8.0));
         String[] dirs = new String[]{"S", "SW", "W", "NW", "N", "NO", "O", "SO"};

         for (int i = -16; i <= 16; i++) {
            double deg = Math.floor(yw / 15.0) * 15.0 + i * 15;
            double x = w / 2.0 + (deg - yw) * 1.25;
            double fade = Math.max(0.0, 1.0 - Math.abs(x - w / 2.0) / (w / 2.0));
            int d = (int)Math.round((deg % 360.0 + 360.0) % 360.0);
            if (d % 45 == 0) {
               String s = dirs[d / 45 % 8];
               boolean main = s.length() == 1;
               H.center(g, s, x, 11.6, main ? 3 : 2, main ? 8.0F : 6.6F, H.alpha(main ? H.TXT : H.SUB, (int)(255.0 * fade)));
            } else {
               g.setColor(new Color(255, 255, 255, (int)(80.0 * fade)));
               g.fill(new java.awt.geom.Rectangle2D.Double(x - 0.35, 6.0, 0.7, 5.0));
            }
         }

         g.setClip(old);
         g.setColor(accent);
         g.fill(new Double(w / 2.0 - 3.0, 14.0, 6.0, 1.8, 1.8, 1.8));
      }
   }

   static final class Effects extends HudModule {
      private volatile List<String[]> rows = new ArrayList<>();

      Effects() {
         super("effects", "Effekte", "Aktive Trank-Effekte mit Restzeit", 0.994, 0.35);
         this.icon("potion");
      }

      @Override
      public boolean visible() {
         return !this.rows.isEmpty();
      }

      @Override
      public double w() {
         double m = 70.0;

         for (String[] r : this.rows) {
            m = Math.max(m, H.w(r[0], 2, 7.2F) + H.w(r[1], 3, 7.2F) + 26.0);
         }

         return Math.ceil(m / 4.0) * 4.0;
      }

      @Override
      public double h() {
         return Math.max(1, this.rows.size()) * 14 + 4;
      }

      @Override
      public String key() {
         List<String[]> out = new ArrayList<>();

         for (MobEffectInstance e : Mc.effects()) {
            String name = Component.translatable(e.getDescriptionId()).getString();
            int amp = e.getAmplifier();
            if (amp > 0) {
               name = name + " " + roman(amp + 1);
            }

            int d = e.getDuration();
            String t = d >= 0 && d <= 360000 ? String.format("%d:%02d", d / 20 / 60, d / 20 % 60) : "∞";
            out.add(new String[]{name, t, d > 0 && d < 200 ? "1" : "0"});
         }

         this.rows = out;
         StringBuilder k = new StringBuilder();

         for (String[] r : out) {
            k.append(r[0]).append(r[1]);
         }

         return k.toString();
      }

      private static String roman(int n) {
         return new String[]{"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"}[Math.min(10, n)];
      }

      @Override
      public void paint(Graphics2D g, Color accent) {
         List<String[]> r = this.rows;
         double w = this.w();

         for (int i = 0; i < r.size(); i++) {
            double y = 2 + i * 14;
            boolean ending = r.get(i)[2].equals("1");
            H.round(g, 6.0, y + 4.5, 5.0, 5.0, 2.5, ending ? new Color(255, 80, 80) : accent);
            H.text(g, r.get(i)[0], 15.0, y + 10.0, 2, 7.2F, H.TXT);
            H.right(g, r.get(i)[1], w - 7.0, y + 10.0, 3, 7.2F, ending ? new Color(255, 120, 120) : H.SUB);
         }
      }
   }

   static final class Fps extends HudModules.Simple {
      Fps() {
         super("fps", "FPS", "Bilder pro Sekunde", "bolt", 0.006, 0.07);
      }

      @Override
      void compute() {
         this.value = String.valueOf(Mc.fps());
         this.unit = "FPS";
      }
   }

   static final class GameTime extends HudModules.Simple {
      GameTime() {
         super("gametime", "Minecraft-Zeit", "Uhrzeit in der Minecraft-Welt (Tag/Nacht)", "sun", 0.006, 0.25);
      }

      @Override
      void compute() {
         long t = (Mc.timeOfDay() + 6000L) % 24000L;
         int h = (int)(t / 1000L);
         int m = (int)(t % 1000L * 60L / 1000L);
         this.value = String.format("%02d:%02d", h, m);
         long d = Mc.timeOfDay() % 24000L;
         this.unit = d >= 13000L && d < 23000L ? "Nacht" : "Tag";
      }
   }

   static final class Health extends HudModule {
      private volatile float hp;
      private volatile float max;
      private volatile float abs;

      Health() {
         super("health", "Leben", "Deine Herzen als Balken", 0.994, 0.67);
         this.icon("heart");
      }

      @Override
      public double w() {
         return 100.0;
      }

      @Override
      public double h() {
         return 18.0;
      }

      @Override
      public String key() {
         LocalPlayer p = Mc.player();
         if (p == null) {
            return "-";
         } else {
            this.hp = Mc.health(p);
            this.max = Math.max(1.0F, Mc.maxHealth(p));
            this.abs = Mc.absorption(p);
            return this.hp + "|" + this.abs + "|" + this.max;
         }
      }

      @Override
      public void paint(Graphics2D g, Color accent) {
         H.icon(g, "heart-fill", 11.0, 9.0, 9.0, new Color(255, 70, 90));
         H.bar(g, 20.0, 7.5, 50.0, 3.5, this.hp / this.max, new Color(255, 70, 90));
         if (this.abs > 0.0F) {
            H.round(g, 20.0, 7.5, Math.max(3.5, (double)(50.0F * Math.min(1.0F, this.abs / this.max))), 3.5, 1.75, new Color(255, 205, 3, 220));
         }

         H.right(g, String.format(Locale.ROOT, "%.1f", (this.hp + this.abs) / 2.0F), 93.0, H.base(0.0, 18.0, 7.6F), 3, 7.6F, H.TXT);
      }
   }

   static final class Hunger extends HudModule {
      private volatile int food;
      private volatile float sat;

      Hunger() {
         super("hunger", "Hunger & Sättigung", "Essen und Sättigung als Balken", 0.994, 0.6);
         this.icon("food");
      }

      @Override
      public double w() {
         return 100.0;
      }

      @Override
      public double h() {
         return 26.0;
      }

      @Override
      public String key() {
         this.food = Mc.food();
         this.sat = Mc.saturation();
         return this.food + "|" + Math.round(this.sat * 10.0F);
      }

      @Override
      public void paint(Graphics2D g, Color accent) {
         H.text(g, "Hunger", 7.0, 10.5, 2, 6.6F, H.SUB);
         H.right(g, this.food + "/20  •  " + String.format(Locale.ROOT, "%.1f", this.sat), 93.0, 10.5, 3, 6.6F, H.TXT);
         H.bar(g, 7.0, 14.0, 86.0, 3.2, this.food / 20.0, new Color(255, 170, 60));
         H.bar(g, 7.0, 19.5, 86.0, 3.2, this.sat / 20.0, new Color(255, 215, 60));
      }
   }

   static final class InfoBar extends HudModule {
      private final Setting.Bool fps = this.add(new Setting.Bool("FPS", true));
      private final Setting.Bool ping = this.add(new Setting.Bool("Ping", true));
      private final Setting.Bool cps = this.add(new Setting.Bool("CPS", false));
      private final Setting.Bool clock = this.add(new Setting.Bool("Uhrzeit", false));
      private final Setting.Bool session = this.add(new Setting.Bool("Spielzeit", true));
      private final Setting.Bool coords = this.add(new Setting.Bool("Koordinaten", false));
      private final Setting.Bool logo = this.add(new Setting.Bool("Lego-Logo", true));
      private volatile List<String> parts = new ArrayList<>();

      InfoBar() {
         super("infobar", "Info-Leiste", "Lego | FPS | Ping | Spielzeit – alles in einer sauberen Zeile", 0.006, 0.008);
         this.enabled = true;
         this.icon("list");
         this.fresh();
      }

      @Override
      public double w() {
         double w = 8.0;
         List<String> p = this.parts;
         if (this.logo.get()) {
            w += H.w("Lego", 3, 8.4F) + 12.0;
         }

         for (int i = 0; i < p.size(); i++) {
            w += H.w(p.get(i), 2, 7.8F) + (i < p.size() - 1 ? 13 : 0);
         }

         return Math.ceil((w + 8.0) / 4.0) * 4.0;
      }

      @Override
      public double h() {
         return 17.0;
      }

      @Override
      public String key() {
         List<String> p = new ArrayList<>();
         if (this.fps.get()) {
            p.add(Mc.fps() + " FPS");
         }

         if (this.ping.get()) {
            int pg = Mc.ping();
            p.add(pg < 0 ? "- ms" : pg + "ms");
         }

         if (this.cps.get()) {
            p.add(HudModules.ClickCounter.left() + " CPS");
         }

         if (this.clock.get()) {
            p.add(LocalTime.now().toString().substring(0, 5));
         }

         if (this.session.get()) {
            p.add(HudModules.sessionTime());
         }

         if (this.coords.get()) {
            LocalPlayer pl = Mc.player();
            if (pl != null) {
               p.add((int)Math.floor(Mc.x(pl)) + " " + (int)Math.floor(Mc.y(pl)) + " " + (int)Math.floor(Mc.z(pl)));
            }
         }

         this.parts = p;
         return String.join("|", p) + this.logo.get();
      }

      @Override
      public void paint(Graphics2D g, Color accent) {
         double x = 8.0;
         double base = H.base(0.0, 17.0, 7.8F);
         if (this.logo.get()) {
            g.setPaint(new GradientPaint((float)x, 0.0F, accent, (float)(x + 22.0), 0.0F, H.mix(accent, Color.WHITE, 0.45)));
            g.setFont(Fonts.get(3, 8.4F));
            g.drawString("Lego", (float)x, (float)H.base(0.0, 17.0, 8.4F));
            x += H.w("Lego", 3, 8.4F) + 6.0;
            sep(g, x, accent);
            x += 6.0;
         }

         List<String> p = this.parts;

         for (int i = 0; i < p.size(); i++) {
            H.text(g, p.get(i), x, base, 2, 7.8F, H.TXT);
            x += H.w(p.get(i), 2, 7.8F);
            if (i < p.size() - 1) {
               x += 6.5;
               sep(g, x, accent);
               x += 6.5;
            }
         }
      }

      private static void sep(Graphics2D g, double x, Color accent) {
         g.setColor(new Color(255, 255, 255, 60));
         g.fill(new Double(x - 0.5, 4.5, 1.0, 8.0, 1.0, 1.0));
      }
   }

   static final class InventoryHud extends HudModule {
      private final Setting.Bool showHotbar = this.add(new Setting.Bool("Hotbar zeigen", true));
      private final Setting.Bool showArmor = this.add(new Setting.Bool("Rüstung zeigen", true));
      private final Setting.Bool showOffhand = this.add(new Setting.Bool("Offhand zeigen", true));
      private final ItemStack[] items = new ItemStack[41];
      private volatile int held;

      InventoryHud() {
         super("invhud", "Inventar HUD", "Dein Inventar als Overlay auf dem Bildschirm", 0.5, 0.75);
         this.icon("backpack");
      }

      private int rows() {
         return (this.showHotbar.get() ? 4 : 3) + (this.showArmor.get() ? 1 : 0);
      }

      @Override
      public double w() {
         return 186.0;
      }

      @Override
      public double h() {
         int r = this.rows();
         double base = r * 20 + 6;
         if (this.showHotbar.get()) {
            base += 4.0;
         }

         return base;
      }

      @Override
      public boolean visible() {
         return Mc.player() != null;
      }

      @Override
      public String key() {
         StringBuilder k = new StringBuilder();
         LocalPlayer p = Mc.player();
         if (p == null) {
            return "-";
         } else {
            ItemStack main = p.getMainHandItem();
            this.held = -1;

            for (int s = 0; s < 9; s++) {
               if (Mc.invStack(s) == main) {
                  this.held = s;
                  break;
               }
            }

            for (int i = 0; i < 36; i++) {
               this.items[i] = Mc.invStack(i);
               k.append(Mc.itemKey(this.items[i])).append(Mc.count(this.items[i])).append(';');
            }

            for (int i = 36; i < 40; i++) {
               this.items[i] = Mc.armor(i);
               k.append(Mc.itemKey(this.items[i])).append(';');
            }

            this.items[40] = p.getOffhandItem();
            k.append(Mc.itemKey(this.items[40])).append(this.showHotbar.get()).append(this.showArmor.get()).append(this.showOffhand.get()).append(this.held);
            return k.toString();
         }
      }

      @Override
      public void paint(Graphics2D g, Color accent) {
         Color slot = new Color(30, 32, 40, 140);
         Color slotA = new Color(30, 32, 40, 180);
         double y = 3.0;
         if (this.showArmor.get()) {
            for (int i = 0; i < 4; i++) {
               H.round(g, 3 + i * 20, y, 18.0, 18.0, 3.0, slotA);
            }

            if (this.showOffhand.get()) {
               H.round(g, 103.0, y, 18.0, 18.0, 3.0, slotA);
               H.center(g, "OH", 112.0, y + 14.0, 1, 4.5F, H.DIM);
            }

            y += 22.0;
         }

         for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
               H.round(g, 3 + col * 20, y, 18.0, 18.0, 3.0, slot);
            }

            y += 20.0;
         }

         if (this.showHotbar.get()) {
            y += 4.0;

            for (int col = 0; col < 9; col++) {
               boolean sel = col == this.held;
               H.round(g, 3 + col * 20, y, 18.0, 18.0, 3.0, sel ? H.alpha(accent, 80) : new Color(30, 32, 40, 160));
               if (sel) {
                  H.round(g, 3 + col * 20, y, 18.0, 18.0, 3.0, H.alpha(accent, 40));
               }
            }
         }
      }

      @Override
      public void overlay(GuiGraphics ctx, int x, int y, double k) {
         double dy = 3.0 * k;
         if (this.showArmor.get()) {
            for (int i = 0; i < 4; i++) {
               ItemStack s = this.items[39 - i];
               if (!Mc.empty(s)) {
                  HudModules.drawItem(ctx, s, x + (3 + i * 20 + 1) * k, y + (dy + 1.0 * k), k);
               }
            }

            if (this.showOffhand.get()) {
               ItemStack s = this.items[40];
               if (!Mc.empty(s)) {
                  HudModules.drawItem(ctx, s, x + 104.0 * k, y + (dy + 1.0 * k), k);
               }
            }

            dy += 22.0 * k;
         }

         for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
               ItemStack s = this.items[9 + row * 9 + col];
               if (!Mc.empty(s)) {
                  HudModules.drawItem(ctx, s, x + (3 + col * 20 + 1) * k, y + dy + 1.0 * k, k);
               }
            }

            dy += 20.0 * k;
         }

         if (this.showHotbar.get()) {
            dy += 4.0 * k;

            for (int colx = 0; colx < 9; colx++) {
               ItemStack s = this.items[colx];
               if (!Mc.empty(s)) {
                  HudModules.drawItem(ctx, s, x + (3 + colx * 20 + 1) * k, y + dy + 1.0 * k, k);
               }
            }
         }
      }
   }

   static final class Keystrokes extends HudModule {
      private final Setting.Bool mouse = this.add(new Setting.Bool("Maustasten + CPS", true));
      private final Setting.Bool space = this.add(new Setting.Bool("Leertaste", true));
      private volatile int mask;
      private volatile String cps = "";
      private final float[] glow = new float[7];
      private long lastPaint = System.nanoTime();

      Keystrokes() {
         super("keystrokes", "Tastenanzeige", "WASD, Maustasten und Leertaste mit Animation", 0.006, 0.83);
         this.icon("keys");
      }

      @Override
      public double w() {
         return 64.0;
      }

      @Override
      public double h() {
         return 42 + (this.mouse.get() ? 21 : 0) + (this.space.get() ? 14 : 0);
      }

      @Override
      public String key() {
         Options o = Mc.mc().options;
         int m = 0;
         if (Mc.pressed(o.keyUp)) {
            m |= 1;
         }

         if (Mc.pressed(o.keyLeft)) {
            m |= 2;
         }

         if (Mc.pressed(o.keyDown)) {
            m |= 4;
         }

         if (Mc.pressed(o.keyRight)) {
            m |= 8;
         }

         if (Mc.pressed(o.keyJump)) {
            m |= 16;
         }

         if (Mc.pressed(o.keyAttack)) {
            m |= 32;
         }

         if (Mc.pressed(o.keyUse)) {
            m |= 64;
         }

         this.mask = m;
         this.cps = HudModules.ClickCounter.left() + "|" + HudModules.ClickCounter.right();
         boolean fading = false;

         for (float f : this.glow) {
            if (f > 0.01F && f < 0.99F) {
               fading = true;
            }
         }

         return m + "|" + this.cps + "|" + this.mouse.get() + this.space.get() + (fading ? System.nanoTime() / 16000000L : 0L);
      }

      @Override
      public boolean panel() {
         return false;
      }

      @Override
      public void paint(Graphics2D g, Color accent) {
         long now = System.nanoTime();
         float dt = Math.min(0.1F, (float)(now - this.lastPaint) / 1.0E9F);
         this.lastPaint = now;
         int m = this.mask;

         for (int i = 0; i < 7; i++) {
            boolean down = (m & 1 << i) != 0;
            this.glow[i] = down ? 1.0F : Math.max(0.0F, this.glow[i] - dt * 6.0F);
         }

         key(g, "W", 22.0, 0.0, 20.0, 20.0, this.glow[0], accent);
         key(g, "A", 0.0, 21.0, 20.0, 20.0, this.glow[1], accent);
         key(g, "S", 22.0, 21.0, 20.0, 20.0, this.glow[2], accent);
         key(g, "D", 44.0, 21.0, 20.0, 20.0, this.glow[3], accent);
         double y = 42.0;
         if (this.mouse.get()) {
            String[] c = this.cps.split("\\|");
            key(g, "LMB", 0.0, y, 31.0, 20.0, this.glow[5], accent);
            key(g, "RMB", 33.0, y, 31.0, 20.0, this.glow[6], accent);
            H.center(g, (c.length > 0 ? c[0] : "0") + " CPS", 15.5, y + 17.0, 2, 4.6F, H.mix(H.SUB, Color.BLACK, this.glow[5]));
            H.center(g, (c.length > 1 ? c[1] : "0") + " CPS", 48.5, y + 17.0, 2, 4.6F, H.mix(H.SUB, Color.BLACK, this.glow[6]));
            y += 21.0;
         }

         if (this.space.get()) {
            key(g, "", 0.0, y, 64.0, 13.0, this.glow[4], accent);
            g.setColor(H.mix(H.TXT, new Color(20, 20, 20), this.glow[4]));
            g.fill(new Double(22.0, y + 5.7, 20.0, 1.6, 1.6, 1.6));
         }
      }

      private static void key(Graphics2D g, String s, double x, double y, double w, double h, float down, Color accent) {
         int a = (int)(255.0 * Math.max(0.35, UiSettings.hudOpacity + 0.2));
         g.setColor(H.mix(new Color(12, 12, 16, Math.min(255, a)), accent, down));
         g.fill(new Double(x, y, w, h, 7.0, 7.0));
         g.setColor(new Color(255, 255, 255, (int)(18.0F * (1.0F - down))));
         g.draw(new Double(x + 0.3, y + 0.3, w - 0.6, h - 0.6, 7.0, 7.0));
         if (!s.isEmpty()) {
            boolean small = s.length() > 1;
            H.center(g, s, x + w / 2.0, small ? y + 9.5 : y + h / 2.0 + 3.0, 3, small ? 6.2F : 8.4F, H.mix(H.TXT, new Color(18, 18, 18), down));
         }
      }
   }

   static final class Light extends HudModules.Simple {
      Light() {
         super("light", "Lichtlevel", "Blocklicht an deinen Füßen (unter 1 können Monster spawnen)", "sun", 0.006, 0.565);
         this.fresh();
      }

      @Override
      void compute() {
         int l = Mc.blockLight();
         this.value = l < 0 ? "-" : String.valueOf(l);
         this.unit = l == 0 ? "Monster!" : "Licht";
      }
   }

   static final class Memory extends HudModules.Simple {
      Memory() {
         super("memory", "Arbeitsspeicher", "RAM-Verbrauch von Minecraft", "chip", 0.006, 0.385);
      }

      @Override
      void compute() {
         Runtime r = Runtime.getRuntime();
         long used = r.totalMemory() - r.freeMemory() >> 20;
         long max = r.maxMemory() >> 20;
         this.value = used * 100L / Math.max(1L, max) + "%";
         this.unit = used / 10L * 10L + "/" + max + " MB";
      }
   }

   static final class Ping extends HudModules.Simple {
      Ping() {
         super("ping", "Ping", "Verbindung zum Server in Millisekunden", "wifi", 0.006, 0.115);
      }

      @Override
      void compute() {
         int p = Mc.ping();
         this.value = p < 0 ? "-" : String.valueOf(p);
         this.unit = "ms";
      }
   }

   static final class PvpItems extends HudModule {
      private static final String[] IDS = new String[]{"end_crystal", "obsidian", "golden_apple", "enchanted_golden_apple", "ender_pearl", "experience_bottle"};
      private volatile int[] counts = new int[IDS.length];

      PvpItems() {
         super("pvpitems", "PvP-Items", "Kristalle, Obsidian, Äpfel, Perlen, XP", 0.56, 0.8);
         this.icon("sword");
      }

      @Override
      public double w() {
         return IDS.length * 24 + 4;
      }

      @Override
      public double h() {
         return 29.0;
      }

      @Override
      public String key() {
         int[] c = new int[IDS.length];
         StringBuilder k = new StringBuilder();

         for (int i = 0; i < IDS.length; i++) {
            c[i] = Mc.countItem(IDS[i]);
            k.append(c[i]).append(',');
         }

         this.counts = c;
         return k.toString();
      }

      @Override
      public void paint(Graphics2D g, Color accent) {
         int[] c = this.counts;

         for (int i = 0; i < c.length; i++) {
            H.center(g, String.valueOf(c[i]), 2 + i * 24 + 12, 26.5, 3, 6.2F, c[i] == 0 ? H.DIM : H.TXT);
         }
      }

      @Override
      public void overlay(GuiGraphics ctx, int x, int y, double k) {
         for (int i = 0; i < IDS.length; i++) {
            ItemStack icon = Mc.icon(IDS[i]);
            if (icon != null) {
               HudModules.drawItem(ctx, icon, x + (2 + i * 24 + 4) * k, y + 2.0 * k, k);
            }
         }
      }
   }

   static final class Server extends HudModules.Simple {
      Server() {
         super("server", "Server-IP", "Adresse des Servers", "globe", 0.006, 0.43);
      }

      @Override
      void compute() {
         this.value = Mc.serverAddress();
         this.unit = "";
      }
   }

   static final class Session extends HudModules.Simple {
      Session() {
         super("session", "Spielzeit", "Wie lange du schon auf dem Server bist", "hourglass", 0.006, 0.295);
      }

      @Override
      void compute() {
         this.value = HudModules.sessionTime();
         this.unit = "";
      }
   }

   abstract static class Simple extends HudModule {
      private final String icon;
      protected volatile String value = "";
      protected volatile String unit = "";

      Simple(String id, String name, String desc, String icon, double fx, double fy) {
         super(id, name, desc, fx, fy);
         this.icon = icon;
         this.icon(icon);
      }

      abstract void compute();

      @Override
      public double w() {
         return simpleWidth(this.value, this.unit);
      }

      @Override
      public double h() {
         return 16.0;
      }

      @Override
      public String key() {
         this.compute();
         return this.value + "|" + this.unit + "|" + icons();
      }

      @Override
      public void paint(Graphics2D g, Color accent) {
         simplePaint(g, this.icon, this.value, this.unit, this.w(), accent);
      }
   }

   static final class Speed extends HudModules.Simple {
      Speed() {
         super("speed", "Geschwindigkeit", "Blöcke pro Sekunde", "speed", 0.006, 0.34);
      }

      @Override
      void compute() {
         LocalPlayer p = Mc.player();
         if (p == null) {
            this.value = "-";
            this.unit = "";
         } else {
            double dx = Mc.x(p) - Mc.lastX(p);
            double dz = Mc.z(p) - Mc.lastZ(p);
            this.value = String.format(Locale.ROOT, "%.1f", Math.sqrt(dx * dx + dz * dz) * 20.0);
            this.unit = "b/s";
         }
      }
   }

   static final class TargetHud extends HudModule {
      private volatile String name = "";
      private volatile String info = "";
      private volatile float hp;
      private volatile float max;
      private volatile float abs;
      private volatile boolean has;
      private volatile double shownHp = -1.0;

      TargetHud() {
         super("target", "Target HUD", "Leben und Abstand des Gegners mit Animation", 0.5, 0.62);
         this.enabled = true;
         this.icon("target");
      }

      @Override
      public boolean visible() {
         return this.has;
      }

      @Override
      public double w() {
         return 130.0;
      }

      @Override
      public double h() {
         return 36.0;
      }

      @Override
      public String key() {
         Entity t = HudModules.CombatTracker.target();
         LocalPlayer p = Mc.player();
         if (t instanceof LivingEntity && p != null) {
            LivingEntity l = (LivingEntity)t;
            this.has = true;
            this.name = Mc.name(t);
            this.hp = Mc.health(l);
            this.max = Math.max(1.0F, Mc.maxHealth(l));
            this.abs = Mc.absorption(l);
            this.info = String.format(Locale.ROOT, "%.1f m", Math.sqrt(p.distanceToSqr(t)));
            if (this.shownHp < 0.0) {
               this.shownHp = this.hp;
            }

            boolean animating = Math.abs(this.shownHp - this.hp) > 0.05;
            return this.name + this.hp + "|" + this.abs + "|" + this.info + (animating ? "|" + System.nanoTime() / 16000000L : "");
         } else {
            this.has = false;
            this.shownHp = -1.0;
            return "none";
         }
      }

      @Override
      public void paint(Graphics2D g, Color accent) {
         double w = this.w();
         this.shownHp = this.shownHp + (this.hp - this.shownHp) * 0.25;
         H.round(g, 5.0, 5.0, 26.0, 26.0, 8.0, H.alpha(accent, 50));
         H.center(g, this.name.isEmpty() ? "?" : this.name.substring(0, 1).toUpperCase(Locale.ROOT), 18.0, 22.5, 3, 13.0F, accent);
         H.text(g, H.ellipsize(this.name, w - 80.0, 3, 8.0F), 37.0, 14.0, 3, 8.0F, H.TXT);
         H.right(g, this.info, w - 7.0, 14.0, 2, 6.6F, H.SUB);
         double frac = this.hp / this.max;
         double shown = this.shownHp / this.max;
         Color hc = frac > 0.6 ? accent : (frac > 0.3 ? new Color(255, 205, 3) : new Color(255, 70, 70));
         H.bar(g, 37.0, 19.0, w - 44.0, 4.5, Math.max(frac, shown), new Color(255, 255, 255, 90));
         H.round(g, 37.0, 19.0, Math.max(4.5, (w - 44.0) * Math.max(0.0, Math.min(1.0, frac))), 4.5, 2.25, hc);
         if (this.abs > 0.0F) {
            H.round(g, 37.0, 19.0, Math.max(4.5, (w - 44.0) * Math.min(1.0F, this.abs / this.max)), 4.5, 2.25, new Color(255, 205, 3, 210));
         }

         H.text(g, String.format(Locale.ROOT, "%.1f", (this.hp + this.abs) / 2.0F), 37.0, 31.5, 3, 6.8F, H.TXT);
         H.text(g, "Herzen", 37.0 + H.w(String.format(Locale.ROOT, "%.1f", (this.hp + this.abs) / 2.0F), 3, 6.8F) + 3.0, 31.5, 1, 6.2F, H.SUB);
      }
   }

   static final class Totems extends HudModule {
      private volatile int n;

      Totems() {
         super("totems", "Totem-Zähler", "Wie viele Totems du hast", 0.44, 0.8);
         this.icon("totem");
      }

      @Override
      public double w() {
         return 44.0;
      }

      @Override
      public double h() {
         return 20.0;
      }

      @Override
      public String key() {
         this.n = Mc.countItem("totem_of_undying");
         return String.valueOf(this.n);
      }

      @Override
      public void paint(Graphics2D g, Color accent) {
         H.text(g, String.valueOf(this.n), 23.0, H.base(0.0, 20.0, 9.0F), 3, 9.0F, this.n == 0 ? new Color(255, 90, 90) : H.TXT);
      }

      @Override
      public void overlay(GuiGraphics ctx, int x, int y, double k) {
         ItemStack icon = Mc.icon("totem_of_undying");
         if (icon != null) {
            HudModules.drawItem(ctx, icon, x + 4.0 * k, y + 2.0 * k, k);
         }
      }
   }
}
