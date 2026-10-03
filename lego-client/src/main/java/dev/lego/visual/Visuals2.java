package dev.lego.visual;

import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.hud.HudModules;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Tx;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class Visuals2 {
   public static Visuals2.BlockOutline outline;
   public static Visuals2.DamageNumbers damage;
   public static Visuals2.TargetMarker target;
   public static Visuals2.PlayerRings rings;
   public static Visuals2.ProjectileTrails projectiles;
   public static Visuals2.DynamicCamera camera;
   public static Visuals2.LowFire lowFire;

   private Visuals2() {
   }

   public static void registerAll() {
      outline = Modules.register(new Visuals2.BlockOutline());
      damage = Modules.register(new Visuals2.DamageNumbers());
      target = Modules.register(new Visuals2.TargetMarker());
      rings = Modules.register(new Visuals2.PlayerRings());
      projectiles = Modules.register(new Visuals2.ProjectileTrails());
      camera = Modules.register(new Visuals2.DynamicCamera());
      lowFire = Modules.register(new Visuals2.LowFire());
      Overlays.registerAll();
   }

   static void render(float var0) {
      if (outline.enabled) {
         outline.render();
      }

      if (rings.enabled) {
         rings.render(var0);
      }

      if (target.enabled) {
         target.render(var0);
      }

      if (projectiles.enabled) {
         projectiles.render();
      }

      Visuals.renderKillBeams();
      if (damage.enabled) {
         damage.render();
      }
   }

   static void onAttack(Entity var0) {
      if (damage.enabled) {
         damage.lastAttack = System.currentTimeMillis();
      }
   }

   public static final class BlockOutline extends Module {
      final Setting.Color color = this.add(new Setting.Color("Farbe", 0));
      final Setting.Num width = this.add(new Setting.Num("Liniendicke", 1.0, 6.0, 0.5, 2.5, ""));
      final Setting.Num fill = this.add(new Setting.Num("Füllung", 0.0, 60.0, 5.0, 15.0, "%"));
      final Setting.Bool pulse = this.add(new Setting.Bool("Pulsieren", true));

      BlockOutline() {
         super("blockoutline", "Block-Outline", "Schöner Rahmen und Füllung um den anvisierten Block", Category.VISUALS);
         this.icon("box");
         this.fresh();
      }

      public boolean replaceVanilla() {
         return this.enabled;
      }

      void render() {
         HitResult var1 = Mc.mc().hitResult;
         if (var1 instanceof BlockHitResult && var1.getType() == Type.BLOCK) {
            BlockPos var2 = ((BlockHitResult)var1).getBlockPos();

            AABB var3;
            try {
               VoxelShape var4 = Mc.world().getBlockState(var2).getShape(Mc.world(), var2);
               if (var4.isEmpty()) {
                  return;
               }

               var3 = var4.bounds();
            } catch (Throwable var25) {
               return;
            }

            double var26 = 0.002;
            double var6 = var2.getX() + var3.minX - var26;
            double var8 = var2.getY() + var3.minY - var26;
            double var10 = var2.getZ() + var3.minZ - var26;
            double var12 = var2.getX() + var3.maxX + var26;
            double var14 = var2.getY() + var3.maxY + var26;
            double var16 = var2.getZ() + var3.maxZ + var26;
            int var18 = this.color.argb() & 16777215;
            double var19 = this.pulse.get() ? 0.75 + 0.25 * Math.sin(System.currentTimeMillis() / 250.0) : 1.0;
            double var21 = Math.sqrt(
               Math.pow((var6 + var12) / 2.0 - R3.camX(), 2.0)
                  + Math.pow((var8 + var14) / 2.0 - R3.camY(), 2.0)
                  + Math.pow((var10 + var16) / 2.0 - R3.camZ(), 2.0)
            );
            double var23 = this.width.get() * 0.004 * Math.max(1.0, var21 * 0.35);
            if (this.fill.get() > 0.0) {
               R3.box(var6, var8, var10, var12, var14, var16, R3.argb(var18, this.fill.get() / 100.0 * var19));
            }

            R3.boxEdges(var6, var8, var10, var12, var14, var16, var23, R3.argb(var18, 0.95 * var19));
         }
      }
   }

   public static final class DamageNumbers extends Module {
      final Setting.Bool onlyMine = this.add(new Setting.Bool("Nur eigene Treffer", true));
      final Setting.Num size = this.add(new Setting.Num("Größe", 0.5, 2.0, 0.1, 1.0, "x"));
      final Setting.Bool hearts = this.add(new Setting.Bool("In Herzen anzeigen", true));
      private final Map<Integer, Float> hp = new HashMap<>();
      private final List<double[]> nums = new ArrayList<>();
      long lastAttack = 0L;

      DamageNumbers() {
         super("damagenumbers", "Schadenszahlen", "Fliegende Zahlen zeigen, wie viel Schaden du machst", Category.VISUALS);
         this.icon("number");
         this.fresh();
      }

      @Override
      public void tick() {
         LocalPlayer var1 = Mc.player();
         if (var1 != null) {
            HashMap var2 = new HashMap();

            for (Entity var4 : Mc.entities()) {
               if (var4 instanceof LivingEntity && var4 != var1 && !(var1.distanceToSqr(var4) > 1024.0)) {
                  LivingEntity var5 = (LivingEntity)var4;
                  float var6 = Mc.health(var5) + Mc.absorption(var5);
                  var2.put(Mc.id(var4), var6);
                  Float var7 = this.hp.get(Mc.id(var4));
                  if (var7 != null && var6 < var7 - 0.01F) {
                     boolean var8 = var4 == HudModules.CombatTracker.lastTarget() && System.currentTimeMillis() - this.lastAttack < 1200L;
                     if (!this.onlyMine.get() || var8) {
                        double[] var9 = new double[]{
                           Mc.x(var4) + (Math.random() - 0.5) * 0.6,
                           Mc.y(var4) + Mc.height(var4) + 0.1,
                           Mc.z(var4) + (Math.random() - 0.5) * 0.6,
                           System.currentTimeMillis(),
                           var7 - var6,
                           Mc.velocity(var1)[1] < -0.05 ? 1.0 : 0.0
                        };
                        this.nums.add(var9);
                     }
                  }
               }
            }

            this.hp.clear();
            this.hp.putAll(var2);
         }
      }

      void render() {
         long var1 = System.currentTimeMillis();
         Iterator var3 = this.nums.iterator();

         while (var3.hasNext()) {
            double[] var4 = (double[])var3.next();
            double var5 = (var1 - var4[3]) / 1100.0;
            if (var5 >= 1.0) {
               var3.remove();
            } else {
               double var7 = this.hearts.get() ? var4[4] / 2.0 : var4[4];
               String var9 = (var7 >= 10.0 ? String.valueOf(Math.round(var7)) : String.format(Locale.ROOT, "%.1f", var7)) + (this.hearts.get() ? "❤" : "");
               int var10 = var4[5] > 0.0 ? -15043 : (var7 >= 4.0 ? -45730 : -1);
               Identifier var11 = Visuals2.TextTex.get(var9, var10);
               double var12 = var5 < 0.15 ? 0.6 + var5 / 0.15 * 0.6 : 1.2 - Math.min(0.2, var5 - 0.15);
               double var14 = 0.35 * this.size.get() * var12;
               double var16 = var14 * Visuals2.TextTex.aspect(var9, var10);
               R3.billboard(
                  var11, var4[0], var4[1] + var5 * 0.8, var4[2], var16, var14, 0.0F, 0.0F, 1.0F, 1.0F, R3.argb(16777215, var5 > 0.7 ? (1.0 - var5) / 0.3 : 1.0)
               );
            }
         }
      }
   }

   public static final class DynamicCamera extends Module {
      final Setting.Num strength = this.add(new Setting.Num("Neigung", 0.5, 4.0, 0.1, 1.5, "°"));
      private float roll = 0.0F;

      DynamicCamera() {
         super("dynamiccamera", "Dynamische Kamera", "Leichte Kamera-Neigung beim seitlichen Laufen", Category.VISUALS);
         this.icon("tilt");
         this.fresh();
      }

      public float roll() {
         LocalPlayer var1 = Mc.player();
         if (var1 != null && this.enabled) {
            double[] var2 = Mc.velocity(var1);
            double var3 = Math.toRadians(Mc.yaw(var1));
            double var5 = var2[0] * Math.cos(var3) + var2[2] * Math.sin(var3);
            float var7 = (float)(-var5 * 8.0 * this.strength.get());
            var7 = Math.max(-this.strength.getF() * 2.0F, Math.min(this.strength.getF() * 2.0F, var7));
            this.roll = this.roll + (var7 - this.roll) * 0.08F;
            return this.roll;
         } else {
            this.roll *= 0.8F;
            return this.roll;
         }
      }
   }

   public static final class LowFire extends Module {
      final Setting.Num height = this.add(new Setting.Num("Absenken", 0.1, 0.6, 0.05, 0.3, ""));

      public double height() {
         return this.height.get();
      }

      LowFire() {
         super("lowfire", "Low Fire", "Feuer-Overlay tiefer, damit du besser siehst", Category.VISUALS);
         this.icon("flame");
         this.fresh();
      }
   }

   public static final class PlayerRings extends Module {
      final Setting.Color color = this.add(new Setting.Color("Farbe", 3));
      final Setting.Num range = this.add(new Setting.Num("Reichweite", 8.0, 64.0, 4.0, 32.0, " Blöcke"));

      PlayerRings() {
         super("playerrings", "Spieler-Ringe", "Leuchtende Ringe unter anderen Spielern", Category.VISUALS);
         this.icon("ring");
         this.fresh();
      }

      void render(float var1) {
         LocalPlayer var2 = Mc.player();
         if (var2 != null) {
            double var3 = System.currentTimeMillis() / 1000.0;

            for (Player var6 : Mc.players()) {
               if (var6 != var2 && !Mc.isSpectator(var6) && !(var2.distanceToSqr(var6) > this.range.get() * this.range.get())) {
                  double[] var7 = Mc.lerpPos(var6, var1);
                  int var8 = this.color.argb(Mc.id(var6) * 0.21) & 16777215;
                  R3.ring(var7[0], var7[1] + 0.02, var7[2], 0.55, 0.62, 36, R3.argb(var8, 0.9), R3.argb(var8, 0.9), 0.0);
                  R3.ring(var7[0], var7[1] + 0.02, var7[2], 0.1, 0.55, 36, R3.argb(var8, 0.0), R3.argb(var8, 0.25 + 0.1 * Math.sin(var3 * 3.0)), 0.0);
               }
            }
         }
      }
   }

   public static final class ProjectileTrails extends Module {
      final Setting.Color color = this.add(new Setting.Color("Farbe", 11));
      final Setting.Num length = this.add(new Setting.Num("Länge", 5.0, 40.0, 1.0, 18.0, ""));
      private final Map<Integer, ArrayDeque<double[]>> trails = new HashMap<>();

      ProjectileTrails() {
         super("projectiletrails", "Geschoss-Spuren", "Leuchtende Spuren hinter Pfeilen, Perlen und Dreizacken", Category.VISUALS);
         this.icon("arrow");
         this.fresh();
      }

      private static boolean projectile(String var0) {
         return var0.equals("arrow")
            || var0.equals("spectral_arrow")
            || var0.equals("trident")
            || var0.equals("ender_pearl")
            || var0.equals("snowball")
            || var0.equals("egg")
            || var0.equals("splash_potion")
            || var0.equals("lingering_potion")
            || var0.equals("wind_charge")
            || var0.equals("fireball");
      }

      @Override
      public void tick() {
         HashSet var1 = new HashSet();

         for (Entity var3 : Mc.entities()) {
            if (projectile(Mc.typeId(var3))) {
               int var4 = Mc.id(var3);
               var1.add(var4);
               ArrayDeque var5 = this.trails.computeIfAbsent(var4, var0 -> new ArrayDeque<>());
               double[] var6 = (double[])var5.peekLast();
               double[] var7 = new double[]{Mc.x(var3), Mc.y(var3), Mc.z(var3)};
               if (var6 == null || Math.abs(var6[0] - var7[0]) + Math.abs(var6[1] - var7[1]) + Math.abs(var6[2] - var7[2]) > 0.02) {
                  var5.add(var7);
               }

               while (var5.size() > this.length.getI()) {
                  var5.poll();
               }
            }
         }

         this.trails.keySet().retainAll(var1);
      }

      void render() {
         for (Entry var2 : this.trails.entrySet()) {
            ArrayList var3 = new ArrayList((Collection)var2.getValue());
            int var4 = var3.size();

            for (int var5 = 1; var5 < var4; var5++) {
               double[] var6 = (double[])var3.get(var5 - 1);
               double[] var7 = (double[])var3.get(var5);
               int var8 = this.color.argb(((Integer)var2.getKey()).intValue() * 0.1 + var5 * 0.03) & 16777215;
               double var9 = (double)(var5 - 1) / var4;
               double var11 = (double)var5 / var4;
               R3.line(var6[0], var6[1], var6[2], var7[0], var7[1], var7[2], 0.08 + 0.1 * var11, R3.argb(var8, 0.8 * var9), R3.argb(var8, 0.8 * var11));
            }
         }
      }
   }

   public static final class TargetMarker extends Module {
      final Setting.Color color = this.add(new Setting.Color("Farbe", 1));
      final Setting.Mode style = this.add(new Setting.Mode("Stil", 0, "Ring + Pfeil", "Nur Ring", "Nur Pfeil"));

      TargetMarker() {
         super("targetmarker", "Ziel-Markierung", "Markiert den Gegner, den du gerade bekämpfst", Category.VISUALS);
         this.icon("target");
         this.fresh();
      }

      void render(float var1) {
         Entity var2 = HudModules.CombatTracker.lastTarget();
         if (var2 != null && var2.isAlive() && System.currentTimeMillis() - HudModules.CombatTracker.lastAttackAt() <= 6000L) {
            double[] var3 = Mc.lerpPos(var2, var1);
            double var4 = System.currentTimeMillis() / 1000.0;
            int var6 = this.color.argb() & 16777215;
            double var7 = Mc.width(var2) * 0.9 + 0.2;
            if (this.style.index != 2) {
               double var9 = var3[1] + (0.5 + 0.5 * Math.sin(var4 * 2.5)) * Mc.height(var2);
               R3.ring(var3[0], var9, var3[2], var7 - 0.05, var7 + 0.05, 40, R3.argb(var6, 0.9), R3.argb(var6, 0.9), var4);
               R3.ring(var3[0], var9, var3[2], var7, var7 + 0.35, 40, R3.argb(var6, 0.35), R3.argb(var6, 0.0), var4);
            }

            if (this.style.index != 1) {
               double var20 = var3[1] + Mc.height(var2) + 0.45 + Math.sin(var4 * 4.0) * 0.08;
               double var11 = var4 * 2.0;
               double var13 = 0.18;
               double var15 = Math.cos(var11) * var13;
               double var17 = Math.sin(var11) * var13;
               int var19 = R3.argb(var6, 0.95);
               R3.quad(
                  var3[0] - var15,
                  var20 + var13 * 1.4,
                  var3[2] - var17,
                  var3[0] + var15,
                  var20 + var13 * 1.4,
                  var3[2] + var17,
                  var3[0],
                  var20,
                  var3[2],
                  var3[0],
                  var20,
                  var3[2],
                  var19
               );
               R3.quad(
                  var3[0] - var17,
                  var20 + var13 * 1.4,
                  var3[2] + var15,
                  var3[0] + var17,
                  var20 + var13 * 1.4,
                  var3[2] - var15,
                  var3[0],
                  var20,
                  var3[2],
                  var3[0],
                  var20,
                  var3[2],
                  var19
               );
            }
         }
      }
   }

   public static final class TextTex {
      private static final Map<String, Identifier> TEX = new HashMap<>();
      private static final Map<String, Double> ASPECT = new HashMap<>();
      private static int n = 0;

      public static Identifier get(String var0, int var1) {
         String var2 = var0 + "|" + var1;
         Identifier var3 = TEX.get(var2);
         if (var3 != null) {
            return var3;
         } else {
            float var4 = 48.0F;
            int var5 = (int)Math.ceil(Fonts.width(var0, 3, var4)) + 16;
            byte var6 = 64;
            BufferedImage var7 = new BufferedImage(var5, var6, 2);
            Graphics2D var8 = var7.createGraphics();
            Gx.hints(var8);
            var8.setFont(Fonts.get(3, var4));
            var8.setColor(new Color(0, 0, 0, 170));

            for (int var9 = -2; var9 <= 2; var9++) {
               for (int var10 = -2; var10 <= 3; var10++) {
                  var8.drawString(var0, 8 + var9, 48 + var10);
               }
            }

            var8.setColor(new Color(var1, true));
            var8.drawString(var0, 8, 48);
            var8.dispose();
            var3 = Identifier.fromNamespaceAndPath("legoclient", "text/" + n++);
            Tx.register(var3, var7.getRGB(0, 0, var5, var6, null, 0, var5), var5, var6, true);
            TEX.put(var2, var3);
            ASPECT.put(var2, (double)var5 / var6);
            if (TEX.size() > 300) {
               for (Identifier var13 : TEX.values()) {
                  Tx.destroy(var13);
               }

               TEX.clear();
               ASPECT.clear();
            }

            return var3;
         }
      }

      public static double aspect(String var0, int var1) {
         Double var2 = ASPECT.get(var0 + "|" + var1);
         return var2 == null ? 2.0 : var2;
      }
   }
}
