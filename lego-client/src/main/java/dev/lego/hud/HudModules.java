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
      Modules.register(new Speedometer());
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
   }

   static String sessionTime() {
      long var0 = LegoClient.sessionSeconds();
      return var0 >= 3600L ? String.format("%02d:%02d:%02d", var0 / 3600L, var0 % 3600L / 60L, var0 % 60L) : String.format("%02d:%02d", var0 / 60L, var0 % 60L);
   }

   static void drawItem(GuiGraphics var0, ItemStack var1, double var2, double var4, double var6) {
      Matrix3x2fStack var8 = var0.pose();
      var8.pushMatrix();
      var8.translate((float)var2, (float)var4);
      var8.scale((float)var6, (float)var6);
      var0.renderItem(var1, 0, 0);
      var8.popMatrix();
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
         for (int var1 = 0; var1 < 4; var1++) {
            if (!Mc.empty(Mc.armor(39 - var1))) {
               return true;
            }
         }

         return false;
      }

      @Override
      public String key() {
         StringBuilder var1 = new StringBuilder();
         String[] var2 = new String[5];
         int[] var3 = new int[5];

         for (int var4 = 0; var4 < 5; var4++) {
            ItemStack var5;
            if (var4 < 4) {
               var5 = Mc.armor(39 - var4);
            } else {
               var5 = this.hand.get() && Mc.player() != null ? Mc.player().getMainHandItem() : null;
            }

            this.stacks[var4] = var5;
            var3[var4] = 100;
            if (Mc.empty(var5)) {
               var2[var4] = "";
               var1.append("-");
            } else {
               if (Mc.damageable(var5) && Mc.maxDamage(var5) > 0) {
                  int var6 = (int)Math.round(100.0 * (Mc.maxDamage(var5) - Mc.damage(var5)) / Mc.maxDamage(var5));
                  var2[var4] = var6 + "%";
                  var3[var4] = var6;
               } else {
                  var2[var4] = Mc.count(var5) > 1 ? String.valueOf(Mc.count(var5)) : "";
               }

               var1.append(Mc.itemKey(var5)).append(var2[var4]).append(';');
            }
         }

         this.pct = var2;
         this.pctVal = var3;
         return var1.toString() + this.layout.index;
      }

      @Override
      public void paint(Graphics2D var1, Color var2) {
         String[] var3 = this.pct;
         int[] var4 = this.pctVal;

         for (int var5 = 0; var5 < this.n(); var5++) {
            if (var3[var5] != null && !var3[var5].isEmpty()) {
               int var6 = var4[var5];
               Color var7 = var6 > 60 ? H.TXT : (var6 > 25 ? new Color(255, 205, 3) : new Color(255, 80, 80));
               if (this.horiz()) {
                  H.center(var1, var3[var5], 2 + var5 * 22 + 11, 27.5, 3, 5.8F, var7);
               } else {
                  H.text(var1, var3[var5], 21.0, var5 * 19 + 13, 3, 6.4F, var7);
               }
            }
         }
      }

      @Override
      public void overlay(GuiGraphics var1, int var2, int var3, double var4) {
         for (int var6 = 0; var6 < this.n(); var6++) {
            ItemStack var7 = this.stacks[var6];
            if (!Mc.empty(var7)) {
               if (this.horiz()) {
                  HudModules.drawItem(var1, var7, var2 + (2 + var6 * 22 + 3) * var4, var3 + 3.0 * var4, var4);
               } else {
                  HudModules.drawItem(var1, var7, var2 + 3.0 * var4, var3 + (var6 * 19 + 2) * var4, var4);
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
         String var1 = Mc.biome().replace('_', ' ');
         this.value = var1.isEmpty() ? "-" : Character.toUpperCase(var1.charAt(0)) + var1.substring(1);
         this.unit = "";
      }
   }

   public static final class ClickCounter {
      private static final ArrayDeque<Long> L = new ArrayDeque<>();
      private static final ArrayDeque<Long> R = new ArrayDeque<>();
      private static boolean lastL;
      private static boolean lastR;

      public static void frame() {
         boolean var0 = Mc.mouseDown(0);
         boolean var1 = Mc.mouseDown(1);
         long var2 = System.currentTimeMillis();
         boolean var4 = !Mc.screenOpen();
         if (var0 && !lastL && var4) {
            L.add(var2);
         }

         if (var1 && !lastR && var4) {
            R.add(var2);
         }

         lastL = var0;
         lastR = var1;

         while (!L.isEmpty() && var2 - L.peekFirst() > 1000L) {
            L.pollFirst();
         }

         while (!R.isEmpty() && var2 - R.peekFirst() > 1000L) {
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
         LocalTime var1 = LocalTime.now();
         int var2 = var1.getHour();
         String var3 = "";
         if (this.h12.get()) {
            var3 = var2 < 12 ? "AM" : "PM";
            var2 = var2 % 12 == 0 ? 12 : var2 % 12;
         }

         this.value = this.seconds.get()
            ? String.format("%02d:%02d:%02d", var2, var1.getMinute(), var1.getSecond())
            : String.format("%02d:%02d", var2, var1.getMinute());
         this.unit = var3;
      }
   }

   public static final class CombatTracker {
      private static Entity last;
      private static long lastAt;
      private static int combo;
      private static double reach;

      public static void onAttack(Entity var0) {
         LocalPlayer var1 = Mc.player();
         long var2 = System.currentTimeMillis();
         if (var0 == last && var2 - lastAt < 2500L) {
            combo++;
         } else {
            combo = 1;
         }

         last = var0;
         lastAt = var2;
         if (var1 != null) {
            reach = Math.sqrt(var1.distanceToSqr(var0)) - Mc.width(var0) / 2.0;
         }
      }

      public static Entity target() {
         Entity var0 = Mc.mc().crosshairPickEntity;
         if (var0 instanceof Player) {
            return var0;
         } else if (last != null && System.currentTimeMillis() - lastAt < 8000L && last.isAlive()) {
            return last;
         } else {
            return var0 instanceof LivingEntity ? var0 : null;
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
         double var1 = H.w(this.l1, 3, 8.0F) + (this.facing.get() ? H.w(this.dir, 2, 7.0F) + 8.0 : 0.0);
         double var3 = this.nether.get() ? H.w(this.l2, 1, 6.8F) : 0.0;
         return Math.ceil((Math.max(var1, var3) + (icons() ? 13 : 0) + 16.0) / 4.0) * 4.0;
      }

      @Override
      public double h() {
         return this.nether.get() ? 27.0 : 16.0;
      }

      @Override
      public String key() {
         LocalPlayer var1 = Mc.player();
         if (var1 == null) {
            this.l1 = "-";
            this.l2 = "";
            return "none";
         } else {
            int var2 = (int)Math.floor(Mc.x(var1));
            int var3 = (int)Math.floor(Mc.y(var1));
            int var4 = (int)Math.floor(Mc.z(var1));
            this.l1 = var2 + "  " + var3 + "  " + var4;
            boolean var5 = false;

            try {
               var5 = Mc.world().dimension().identifier().getPath().contains("nether");
            } catch (Throwable var8) {
            }

            this.l2 = var5 ? "Oberwelt  " + var2 * 8 + "  " + var4 * 8 : "Nether  " + Math.floorDiv(var2, 8) + "  " + Math.floorDiv(var4, 8);
            float var6 = (Mc.yaw(var1) % 360.0F + 360.0F) % 360.0F;
            String[] var7 = new String[]{"S", "SW", "W", "NW", "N", "NO", "O", "SO"};
            this.dir = var7[Math.round(var6 / 45.0F) % 8];
            return this.l1 + "|" + (this.nether.get() ? this.l2 : "") + "|" + (this.facing.get() ? this.dir : "") + icons();
         }
      }

      @Override
      public void paint(Graphics2D var1, Color var2) {
         double var3 = 7.0;
         if (icons()) {
            H.icon(var1, "pin", var3 + 4.5, 8.0, 9.0, var2);
            var3 += 13.0;
         }

         H.text(var1, this.l1, var3, H.base(0.0, 16.0, 8.0F), 3, 8.0F, H.TXT);
         if (this.facing.get()) {
            H.text(var1, this.dir, var3 + H.w(this.l1, 3, 8.0F) + 7.0, H.base(0.0, 16.0, 8.0F), 2, 7.0F, var2);
         }

         if (this.nether.get()) {
            H.text(var1, this.l2, var3, 22.5, 1, 6.8F, H.SUB);
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
         LocalPlayer var1 = Mc.player();
         this.yaw = var1 == null ? 0.0F : Mc.yaw(var1);
         return String.valueOf(Math.round(this.yaw * 2.0F));
      }

      @Override
      public void paint(Graphics2D var1, Color var2) {
         double var3 = this.w();
         double var5 = (this.yaw % 360.0F + 360.0F) % 360.0F;
         Shape var7 = var1.getClip();
         var1.clip(new Double(4.0, 0.0, var3 - 8.0, 17.0, 8.0, 8.0));
         String[] var8 = new String[]{"S", "SW", "W", "NW", "N", "NO", "O", "SO"};

         for (int var9 = -16; var9 <= 16; var9++) {
            double var10 = Math.floor(var5 / 15.0) * 15.0 + var9 * 15;
            double var12 = var3 / 2.0 + (var10 - var5) * 1.25;
            double var14 = Math.max(0.0, 1.0 - Math.abs(var12 - var3 / 2.0) / (var3 / 2.0));
            int var16 = (int)Math.round((var10 % 360.0 + 360.0) % 360.0);
            if (var16 % 45 == 0) {
               String var17 = var8[var16 / 45 % 8];
               boolean var18 = var17.length() == 1;
               H.center(var1, var17, var12, 11.6, var18 ? 3 : 2, var18 ? 8.0F : 6.6F, H.alpha(var18 ? H.TXT : H.SUB, (int)(255.0 * var14)));
            } else {
               var1.setColor(new Color(255, 255, 255, (int)(80.0 * var14)));
               var1.fill(new java.awt.geom.Rectangle2D.Double(var12 - 0.35, 6.0, 0.7, 5.0));
            }
         }

         var1.setClip(var7);
         var1.setColor(var2);
         var1.fill(new Double(var3 / 2.0 - 3.0, 14.0, 6.0, 1.8, 1.8, 1.8));
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
         double var1 = 70.0;

         for (String[] var4 : this.rows) {
            var1 = Math.max(var1, H.w(var4[0], 2, 7.2F) + H.w(var4[1], 3, 7.2F) + 26.0);
         }

         return Math.ceil(var1 / 4.0) * 4.0;
      }

      @Override
      public double h() {
         return Math.max(1, this.rows.size()) * 14 + 4;
      }

      @Override
      public String key() {
         ArrayList var1 = new ArrayList();

         for (MobEffectInstance var4 : Mc.effects()) {
            String var5 = Component.translatable(var4.getDescriptionId()).getString();
            int var6 = var4.getAmplifier();
            if (var6 > 0) {
               var5 = var5 + " " + roman(var6 + 1);
            }

            int var7 = var4.getDuration();
            String var8 = var7 >= 0 && var7 <= 360000 ? String.format("%d:%02d", var7 / 20 / 60, var7 / 20 % 60) : "∞";
            var1.add(new String[]{var5, var8, var7 > 0 && var7 < 200 ? "1" : "0"});
         }

         this.rows = var1;
         StringBuilder var9 = new StringBuilder();

         for (String[] var11 : (Iterable<String[]>) (Iterable<?>) (var1)) {
            var9.append(var11[0]).append(var11[1]);
         }

         return var9.toString();
      }

      private static String roman(int var0) {
         return new String[]{"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"}[Math.min(10, var0)];
      }

      @Override
      public void paint(Graphics2D var1, Color var2) {
         List var3 = this.rows;
         double var4 = this.w();

         for (int var6 = 0; var6 < var3.size(); var6++) {
            double var7 = 2 + var6 * 14;
            boolean var9 = ((String[])var3.get(var6))[2].equals("1");
            H.round(var1, 6.0, var7 + 4.5, 5.0, 5.0, 2.5, var9 ? new Color(255, 80, 80) : var2);
            H.text(var1, ((String[])var3.get(var6))[0], 15.0, var7 + 10.0, 2, 7.2F, H.TXT);
            H.right(var1, ((String[])var3.get(var6))[1], var4 - 7.0, var7 + 10.0, 3, 7.2F, var9 ? new Color(255, 120, 120) : H.SUB);
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
         long var1 = (Mc.timeOfDay() + 6000L) % 24000L;
         int var3 = (int)(var1 / 1000L);
         int var4 = (int)(var1 % 1000L * 60L / 1000L);
         this.value = String.format("%02d:%02d", var3, var4);
         long var5 = Mc.timeOfDay() % 24000L;
         this.unit = var5 >= 13000L && var5 < 23000L ? "Nacht" : "Tag";
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
         LocalPlayer var1 = Mc.player();
         if (var1 == null) {
            return "-";
         } else {
            this.hp = Mc.health(var1);
            this.max = Math.max(1.0F, Mc.maxHealth(var1));
            this.abs = Mc.absorption(var1);
            return this.hp + "|" + this.abs + "|" + this.max;
         }
      }

      @Override
      public void paint(Graphics2D var1, Color var2) {
         H.icon(var1, "heart-fill", 11.0, 9.0, 9.0, new Color(255, 70, 90));
         H.bar(var1, 20.0, 7.5, 50.0, 3.5, this.hp / this.max, new Color(255, 70, 90));
         if (this.abs > 0.0F) {
            H.round(var1, 20.0, 7.5, Math.max(3.5, (double)(50.0F * Math.min(1.0F, this.abs / this.max))), 3.5, 1.75, new Color(255, 205, 3, 220));
         }

         H.right(var1, String.format(Locale.ROOT, "%.1f", (this.hp + this.abs) / 2.0F), 93.0, H.base(0.0, 18.0, 7.6F), 3, 7.6F, H.TXT);
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
      public void paint(Graphics2D var1, Color var2) {
         H.text(var1, "Hunger", 7.0, 10.5, 2, 6.6F, H.SUB);
         H.right(var1, this.food + "/20  •  " + String.format(Locale.ROOT, "%.1f", this.sat), 93.0, 10.5, 3, 6.6F, H.TXT);
         H.bar(var1, 7.0, 14.0, 86.0, 3.2, this.food / 20.0, new Color(255, 170, 60));
         H.bar(var1, 7.0, 19.5, 86.0, 3.2, this.sat / 20.0, new Color(255, 215, 60));
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
         double var1 = 8.0;
         List var3 = this.parts;
         if (this.logo.get()) {
            var1 += H.w("Lego", 3, 8.4F) + 12.0;
         }

         for (int var4 = 0; var4 < var3.size(); var4++) {
            var1 += H.w((String)var3.get(var4), 2, 7.8F) + (var4 < var3.size() - 1 ? 13 : 0);
         }

         return Math.ceil((var1 + 8.0) / 4.0) * 4.0;
      }

      @Override
      public double h() {
         return 17.0;
      }

      @Override
      public String key() {
         ArrayList var1 = new ArrayList();
         if (this.fps.get()) {
            var1.add(Mc.fps() + " FPS");
         }

         if (this.ping.get()) {
            int var2 = Mc.ping();
            var1.add(var2 < 0 ? "- ms" : var2 + "ms");
         }

         if (this.cps.get()) {
            var1.add(HudModules.ClickCounter.left() + " CPS");
         }

         if (this.clock.get()) {
            var1.add(LocalTime.now().toString().substring(0, 5));
         }

         if (this.session.get()) {
            var1.add(HudModules.sessionTime());
         }

         if (this.coords.get()) {
            LocalPlayer var3 = Mc.player();
            if (var3 != null) {
               var1.add((int)Math.floor(Mc.x(var3)) + " " + (int)Math.floor(Mc.y(var3)) + " " + (int)Math.floor(Mc.z(var3)));
            }
         }

         this.parts = var1;
         return String.join("|", var1) + this.logo.get();
      }

      @Override
      public void paint(Graphics2D var1, Color var2) {
         double var3 = 8.0;
         double var5 = H.base(0.0, 17.0, 7.8F);
         if (this.logo.get()) {
            var1.setPaint(new GradientPaint((float)var3, 0.0F, var2, (float)(var3 + 22.0), 0.0F, H.mix(var2, Color.WHITE, 0.45)));
            var1.setFont(Fonts.get(3, 8.4F));
            var1.drawString("Lego", (float)var3, (float)H.base(0.0, 17.0, 8.4F));
            var3 += H.w("Lego", 3, 8.4F) + 6.0;
            sep(var1, var3, var2);
            var3 += 6.0;
         }

         List var7 = this.parts;

         for (int var8 = 0; var8 < var7.size(); var8++) {
            H.text(var1, (String)var7.get(var8), var3, var5, 2, 7.8F, H.TXT);
            var3 += H.w((String)var7.get(var8), 2, 7.8F);
            if (var8 < var7.size() - 1) {
               var3 += 6.5;
               sep(var1, var3, var2);
               var3 += 6.5;
            }
         }
      }

      private static void sep(Graphics2D var0, double var1, Color var3) {
         var0.setColor(new Color(255, 255, 255, 60));
         var0.fill(new Double(var1 - 0.5, 4.5, 1.0, 8.0, 1.0, 1.0));
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
         Options var1 = Mc.mc().options;
         int var2 = 0;
         if (Mc.pressed(var1.keyUp)) {
            var2 |= 1;
         }

         if (Mc.pressed(var1.keyLeft)) {
            var2 |= 2;
         }

         if (Mc.pressed(var1.keyDown)) {
            var2 |= 4;
         }

         if (Mc.pressed(var1.keyRight)) {
            var2 |= 8;
         }

         if (Mc.pressed(var1.keyJump)) {
            var2 |= 16;
         }

         if (Mc.pressed(var1.keyAttack)) {
            var2 |= 32;
         }

         if (Mc.pressed(var1.keyUse)) {
            var2 |= 64;
         }

         this.mask = var2;
         this.cps = HudModules.ClickCounter.left() + "|" + HudModules.ClickCounter.right();
         boolean var3 = false;

         for (float var7 : this.glow) {
            if (var7 > 0.01F && var7 < 0.99F) {
               var3 = true;
            }
         }

         return var2 + "|" + this.cps + "|" + this.mouse.get() + this.space.get() + (var3 ? System.nanoTime() / 16000000L : 0L);
      }

      @Override
      public boolean panel() {
         return false;
      }

      @Override
      public void paint(Graphics2D var1, Color var2) {
         long var3 = System.nanoTime();
         float var5 = Math.min(0.1F, (float)(var3 - this.lastPaint) / 1.0E9F);
         this.lastPaint = var3;
         int var6 = this.mask;

         for (int var7 = 0; var7 < 7; var7++) {
            boolean var8 = (var6 & 1 << var7) != 0;
            this.glow[var7] = var8 ? 1.0F : Math.max(0.0F, this.glow[var7] - var5 * 6.0F);
         }

         key(var1, "W", 22.0, 0.0, 20.0, 20.0, this.glow[0], var2);
         key(var1, "A", 0.0, 21.0, 20.0, 20.0, this.glow[1], var2);
         key(var1, "S", 22.0, 21.0, 20.0, 20.0, this.glow[2], var2);
         key(var1, "D", 44.0, 21.0, 20.0, 20.0, this.glow[3], var2);
         double var10 = 42.0;
         if (this.mouse.get()) {
            String[] var9 = this.cps.split("\\|");
            key(var1, "LMB", 0.0, var10, 31.0, 20.0, this.glow[5], var2);
            key(var1, "RMB", 33.0, var10, 31.0, 20.0, this.glow[6], var2);
            H.center(var1, (var9.length > 0 ? var9[0] : "0") + " CPS", 15.5, var10 + 17.0, 2, 4.6F, H.mix(H.SUB, Color.BLACK, this.glow[5]));
            H.center(var1, (var9.length > 1 ? var9[1] : "0") + " CPS", 48.5, var10 + 17.0, 2, 4.6F, H.mix(H.SUB, Color.BLACK, this.glow[6]));
            var10 += 21.0;
         }

         if (this.space.get()) {
            key(var1, "", 0.0, var10, 64.0, 13.0, this.glow[4], var2);
            var1.setColor(H.mix(H.TXT, new Color(20, 20, 20), this.glow[4]));
            var1.fill(new Double(22.0, var10 + 5.7, 20.0, 1.6, 1.6, 1.6));
         }
      }

      private static void key(Graphics2D var0, String var1, double var2, double var4, double var6, double var8, float var10, Color var11) {
         int var12 = (int)(255.0 * Math.max(0.35, UiSettings.hudOpacity + 0.2));
         var0.setColor(H.mix(new Color(12, 12, 16, Math.min(255, var12)), var11, var10));
         var0.fill(new Double(var2, var4, var6, var8, 7.0, 7.0));
         var0.setColor(new Color(255, 255, 255, (int)(18.0F * (1.0F - var10))));
         var0.draw(new Double(var2 + 0.3, var4 + 0.3, var6 - 0.6, var8 - 0.6, 7.0, 7.0));
         if (!var1.isEmpty()) {
            boolean var13 = var1.length() > 1;
            H.center(
               var0, var1, var2 + var6 / 2.0, var13 ? var4 + 9.5 : var4 + var8 / 2.0 + 3.0, 3, var13 ? 6.2F : 8.4F, H.mix(H.TXT, new Color(18, 18, 18), var10)
            );
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
         int var1 = Mc.blockLight();
         this.value = var1 < 0 ? "-" : String.valueOf(var1);
         this.unit = var1 == 0 ? "Monster!" : "Licht";
      }
   }

   static final class Memory extends HudModules.Simple {
      Memory() {
         super("memory", "Arbeitsspeicher", "RAM-Verbrauch von Minecraft", "chip", 0.006, 0.385);
      }

      @Override
      void compute() {
         Runtime var1 = Runtime.getRuntime();
         long var2 = var1.totalMemory() - var1.freeMemory() >> 20;
         long var4 = var1.maxMemory() >> 20;
         this.value = var2 * 100L / Math.max(1L, var4) + "%";
         this.unit = var2 / 10L * 10L + "/" + var4 + " MB";
      }
   }

   static final class Ping extends HudModules.Simple {
      Ping() {
         super("ping", "Ping", "Verbindung zum Server in Millisekunden", "wifi", 0.006, 0.115);
      }

      @Override
      void compute() {
         int var1 = Mc.ping();
         this.value = var1 < 0 ? "-" : String.valueOf(var1);
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
         int[] var1 = new int[IDS.length];
         StringBuilder var2 = new StringBuilder();

         for (int var3 = 0; var3 < IDS.length; var3++) {
            var1[var3] = Mc.countItem(IDS[var3]);
            var2.append(var1[var3]).append(',');
         }

         this.counts = var1;
         return var2.toString();
      }

      @Override
      public void paint(Graphics2D var1, Color var2) {
         int[] var3 = this.counts;

         for (int var4 = 0; var4 < var3.length; var4++) {
            H.center(var1, String.valueOf(var3[var4]), 2 + var4 * 24 + 12, 26.5, 3, 6.2F, var3[var4] == 0 ? H.DIM : H.TXT);
         }
      }

      @Override
      public void overlay(GuiGraphics var1, int var2, int var3, double var4) {
         for (int var6 = 0; var6 < IDS.length; var6++) {
            ItemStack var7 = Mc.icon(IDS[var6]);
            if (var7 != null) {
               HudModules.drawItem(var1, var7, var2 + (2 + var6 * 24 + 4) * var4, var3 + 2.0 * var4, var4);
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

      Simple(String var1, String var2, String var3, String var4, double var5, double var7) {
         super(var1, var2, var3, var5, var7);
         this.icon = var4;
         this.icon(var4);
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
      public void paint(Graphics2D var1, Color var2) {
         simplePaint(var1, this.icon, this.value, this.unit, this.w(), var2);
      }
   }

   static final class Speed extends HudModules.Simple {
      Speed() {
         super("speed", "Geschwindigkeit", "Blöcke pro Sekunde", "speed", 0.006, 0.34);
      }

      @Override
      void compute() {
         LocalPlayer var1 = Mc.player();
         if (var1 == null) {
            this.value = "-";
            this.unit = "";
         } else {
            double var2 = Mc.x(var1) - Mc.lastX(var1);
            double var4 = Mc.z(var1) - Mc.lastZ(var1);
            this.value = String.format(Locale.ROOT, "%.1f", Math.sqrt(var2 * var2 + var4 * var4) * 20.0);
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
         Entity var1 = HudModules.CombatTracker.target();
         LocalPlayer var2 = Mc.player();
         if (var1 instanceof LivingEntity && var2 != null) {
            LivingEntity var3 = (LivingEntity)var1;
            this.has = true;
            this.name = Mc.name(var1);
            this.hp = Mc.health(var3);
            this.max = Math.max(1.0F, Mc.maxHealth(var3));
            this.abs = Mc.absorption(var3);
            this.info = String.format(Locale.ROOT, "%.1f m", Math.sqrt(var2.distanceToSqr(var1)));
            if (this.shownHp < 0.0) {
               this.shownHp = this.hp;
            }

            boolean var4 = Math.abs(this.shownHp - this.hp) > 0.05;
            return this.name + this.hp + "|" + this.abs + "|" + this.info + (var4 ? "|" + System.nanoTime() / 16000000L : "");
         } else {
            this.has = false;
            this.shownHp = -1.0;
            return "none";
         }
      }

      @Override
      public void paint(Graphics2D var1, Color var2) {
         double var3 = this.w();
         this.shownHp = this.shownHp + (this.hp - this.shownHp) * 0.25;
         H.round(var1, 5.0, 5.0, 26.0, 26.0, 8.0, H.alpha(var2, 50));
         H.center(var1, this.name.isEmpty() ? "?" : this.name.substring(0, 1).toUpperCase(Locale.ROOT), 18.0, 22.5, 3, 13.0F, var2);
         H.text(var1, H.ellipsize(this.name, var3 - 80.0, 3, 8.0F), 37.0, 14.0, 3, 8.0F, H.TXT);
         H.right(var1, this.info, var3 - 7.0, 14.0, 2, 6.6F, H.SUB);
         double var5 = this.hp / this.max;
         double var7 = this.shownHp / this.max;
         Color var9 = var5 > 0.6 ? var2 : (var5 > 0.3 ? new Color(255, 205, 3) : new Color(255, 70, 70));
         H.bar(var1, 37.0, 19.0, var3 - 44.0, 4.5, Math.max(var5, var7), new Color(255, 255, 255, 90));
         H.round(var1, 37.0, 19.0, Math.max(4.5, (var3 - 44.0) * Math.max(0.0, Math.min(1.0, var5))), 4.5, 2.25, var9);
         if (this.abs > 0.0F) {
            H.round(var1, 37.0, 19.0, Math.max(4.5, (var3 - 44.0) * Math.min(1.0F, this.abs / this.max)), 4.5, 2.25, new Color(255, 205, 3, 210));
         }

         H.text(var1, String.format(Locale.ROOT, "%.1f", (this.hp + this.abs) / 2.0F), 37.0, 31.5, 3, 6.8F, H.TXT);
         H.text(var1, "Herzen", 37.0 + H.w(String.format(Locale.ROOT, "%.1f", (this.hp + this.abs) / 2.0F), 3, 6.8F) + 3.0, 31.5, 1, 6.2F, H.SUB);
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
      public void paint(Graphics2D var1, Color var2) {
         H.text(var1, String.valueOf(this.n), 23.0, H.base(0.0, 20.0, 9.0F), 3, 9.0F, this.n == 0 ? new Color(255, 90, 90) : H.TXT);
      }

      @Override
      public void overlay(GuiGraphics var1, int var2, int var3, double var4) {
         ItemStack var6 = Mc.icon("totem_of_undying");
         if (var6 != null) {
            HudModules.drawItem(var1, var6, var2 + 4.0 * var4, var3 + 2.0 * var4, var4);
         }
      }
   }
}
