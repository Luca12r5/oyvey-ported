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
import dev.lego.visual.shader.HandEffect;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
   public static Visuals2.ChinaHat chinaHat;
   public static Visuals2.ShaderHands shaderHands;
   public static Visuals2.ShaderSky shaderSky;
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
      chinaHat = Modules.register(new Visuals2.ChinaHat());
      shaderHands = Modules.register(new Visuals2.ShaderHands());
      shaderSky = Modules.register(new Visuals2.ShaderSky());
      projectiles = Modules.register(new Visuals2.ProjectileTrails());
      camera = Modules.register(new Visuals2.DynamicCamera());
      lowFire = Modules.register(new Visuals2.LowFire());
      Overlays.registerAll();
   }

   public static void renderHud(int W, int H) {
      if (shaderHands != null && shaderHands.enabled) {
         shaderHands.tick();
      }
   }

   static void render(float td) {
      if (outline.enabled) {
         outline.render();
      }

      if (rings.enabled) {
         rings.render(td);
      }

      if (chinaHat.enabled) {
         chinaHat.render(td);
      }

      if (shaderSky.enabled) {
         shaderSky.render(td);
      }

      if (target.enabled) {
         target.render(td);
      }

      if (projectiles.enabled) {
         projectiles.render();
      }

      Visuals.renderKillBeams();
      if (damage.enabled) {
         damage.render();
      }
   }

   static void onAttack(Entity e) {
      if (damage.enabled) {
         damage.lastAttack = System.currentTimeMillis();
      }
   }

   public static boolean shaderHandsEnabled() {
      return shaderHands != null && shaderHands.enabled;
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
         HitResult hit = Mc.mc().hitResult;
         if (hit instanceof BlockHitResult && hit.getType() == Type.BLOCK) {
            BlockPos pos = ((BlockHitResult)hit).getBlockPos();

            AABB b;
            try {
               VoxelShape shape = Mc.world().getBlockState(pos).getShape(Mc.world(), pos);
               if (shape.isEmpty()) {
                  return;
               }

               b = shape.bounds();
            } catch (Throwable var25) {
               return;
            }

            double e = 0.002;
            double x0 = pos.getX() + b.minX - e;
            double y0 = pos.getY() + b.minY - e;
            double z0 = pos.getZ() + b.minZ - e;
            double x1 = pos.getX() + b.maxX + e;
            double y1 = pos.getY() + b.maxY + e;
            double z1 = pos.getZ() + b.maxZ + e;
            int rgb = this.color.argb() & 16777215;
            double p = this.pulse.get() ? 0.75 + 0.25 * Math.sin(System.currentTimeMillis() / 250.0) : 1.0;
            double dist = Math.sqrt(
               Math.pow((x0 + x1) / 2.0 - R3.camX(), 2.0) + Math.pow((y0 + y1) / 2.0 - R3.camY(), 2.0) + Math.pow((z0 + z1) / 2.0 - R3.camZ(), 2.0)
            );
            double w = this.width.get() * 0.004 * Math.max(1.0, dist * 0.35);
            if (this.fill.get() > 0.0) {
               R3.box(x0, y0, z0, x1, y1, z1, R3.argb(rgb, this.fill.get() / 100.0 * p));
            }

            R3.boxEdges(x0, y0, z0, x1, y1, z1, w, R3.argb(rgb, 0.95 * p));
         }
      }
   }

   public static final class ChinaHat extends Module {
      final Setting.Color color = this.add(new Setting.Color("Farbe", 11));
      final Setting.Num radius = this.add(new Setting.Num("Radius", 0.45, 1.35, 0.05, 0.82, ""));
      final Setting.Num height = this.add(new Setting.Num("Höhe", 0.08, 0.55, 0.02, 0.28, ""));
      final Setting.Num alpha = this.add(new Setting.Num("Deckkraft", 0.05, 1.0, 0.05, 0.55, ""));
      final Setting.Bool outline = this.add(new Setting.Bool("Umriss", true));
      final Setting.Bool selfOnly = this.add(new Setting.Bool("Nur ich", true));

      ChinaHat() {
         super("chinahat", "China Hat", "Gooba-Style konischer Hut über dem Spieler", Category.VISUALS);
         this.icon("hat");
         this.fresh();
      }

      void render(float td) {
         LocalPlayer me = Mc.player();
         if (me != null) {
            for (Player p : Mc.players()) {
               if ((!this.selfOnly.get() || p == me) && !Mc.isSpectator(p)) {
                  double[] q;
                  if (p == me && Mc.firstPerson()) {
                     q = new double[]{R3.camX(), R3.camY(), R3.camZ()};
                     q[1] -= 0.12;
                  } else {
                     q = Mc.lerpPos(p, td);
                  }

                  double y = q[1] + Mc.height(p) + 0.16;
                  double r = this.radius.get();
                  double h = this.height.get();
                  int seg = 96;

                  for (int i = 0; i < seg; i++) {
                     double a0 = (Math.PI * 2) * i / seg;
                     double a1 = (Math.PI * 2) * (i + 1) / seg;
                     int c0 = this.color.argb((double)i / seg) & 16777215;
                     int c1 = this.color.argb((double)(i + 1) / seg) & 16777215;
                     int ca = R3.argb(c0, this.alpha.get() * 0.55);
                     int cb = R3.argb(c1, this.alpha.get() * 0.55);
                     double x0 = q[0] + Math.cos(a0) * r;
                     double z0 = q[2] + Math.sin(a0) * r;
                     double x1 = q[0] + Math.cos(a1) * r;
                     double z1 = q[2] + Math.sin(a1) * r;
                     R3.quad(q[0], y + h, q[2], x0, y, z0, x1, y, z1, q[0], y + h, q[2], R3.argb(c0, this.alpha.get()), ca, cb, R3.argb(c1, this.alpha.get()));
                     if (this.outline.get()) {
                        R3.line(x0, y, z0, x1, y, z1, 0.025, R3.argb(c0, 0.95), R3.argb(c1, 0.95));
                     }
                  }

                  if (this.outline.get()) {
                     R3.line(
                        q[0],
                        y + h,
                        q[2],
                        q[0] + r,
                        y,
                        q[2],
                        0.018,
                        R3.argb(this.color.argb() & 16777215, 0.85),
                        R3.argb(this.color.argb(0.2) & 16777215, 0.85)
                     );
                  }
               }
            }
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
         LocalPlayer me = Mc.player();
         if (me != null) {
            Map<Integer, Float> now = new HashMap<>();

            for (Entity e : Mc.entities()) {
               if (e instanceof LivingEntity && e != me && !(me.distanceToSqr(e) > 1024.0)) {
                  LivingEntity l = (LivingEntity)e;
                  float h = Mc.health(l) + Mc.absorption(l);
                  now.put(Mc.id(e), h);
                  Float old = this.hp.get(Mc.id(e));
                  if (old != null && h < old - 0.01F) {
                     boolean mine = e == HudModules.CombatTracker.lastTarget() && System.currentTimeMillis() - this.lastAttack < 1200L;
                     if (!this.onlyMine.get() || mine) {
                        double[] p = new double[]{
                           Mc.x(e) + (Math.random() - 0.5) * 0.6,
                           Mc.y(e) + Mc.height(e) + 0.1,
                           Mc.z(e) + (Math.random() - 0.5) * 0.6,
                           System.currentTimeMillis(),
                           old - h,
                           Mc.velocity(me)[1] < -0.05 ? 1.0 : 0.0
                        };
                        this.nums.add(p);
                     }
                  }
               }
            }

            this.hp.clear();
            this.hp.putAll(now);
         }
      }

      void render() {
         long t = System.currentTimeMillis();
         Iterator<double[]> it = this.nums.iterator();

         while (it.hasNext()) {
            double[] n = it.next();
            double a = (t - n[3]) / 1100.0;
            if (a >= 1.0) {
               it.remove();
            } else {
               double amount = this.hearts.get() ? n[4] / 2.0 : n[4];
               String s = (amount >= 10.0 ? String.valueOf(Math.round(amount)) : String.format(Locale.ROOT, "%.1f", amount)) + (this.hearts.get() ? "❤" : "");
               int col = n[5] > 0.0 ? -15043 : (amount >= 4.0 ? -45730 : -1);
               Identifier tex = Visuals2.TextTex.get(s, col);
               double pop = a < 0.15 ? 0.6 + a / 0.15 * 0.6 : 1.2 - Math.min(0.2, a - 0.15);
               double h = 0.35 * this.size.get() * pop;
               double w = h * Visuals2.TextTex.aspect(s, col);
               R3.billboard(tex, n[0], n[1] + a * 0.8, n[2], w, h, 0.0F, 0.0F, 1.0F, 1.0F, R3.argb(16777215, a > 0.7 ? (1.0 - a) / 0.3 : 1.0));
            }
         }
      }
   }

   public static final class DynamicCamera extends Module {
      final Setting.Num strength = this.add(new Setting.Num("Neigung", 0.5, 4.0, 0.1, 1.5, "°"));
      final Setting.Bool landShake = this.add(new Setting.Bool("Landungs-Wackeln", true));
      final Setting.Bool sprintFov = this.add(new Setting.Bool("Sprint-FOV-Boost", false));
      final Setting.Num fovBoost = this.add(new Setting.Num("FOV-Boost", 1.0, 10.0, 1.0, 3.0, "°"));
      private float roll = 0.0F;
      private float shake = 0.0F;
      private float fovExtra = 0.0F;
      private boolean wasOnGround;
      private double prevVelY;

      DynamicCamera() {
         super("dynamiccamera", "Dynamische Kamera", "Kamera-Neigung, Landungs-Wackeln und Sprint-FOV", Category.VISUALS);
         this.icon("tilt");
         this.fresh();
      }

      public float roll() {
         LocalPlayer p = Mc.player();
         if (p != null && this.enabled) {
            double[] v = Mc.velocity(p);
            double yaw = Math.toRadians(Mc.yaw(p));
            double side = v[0] * Math.cos(yaw) + v[2] * Math.sin(yaw);
            float target = (float)(-side * 8.0 * this.strength.get());
            target = Math.max(-this.strength.getF() * 2.0F, Math.min(this.strength.getF() * 2.0F, target));
            this.roll = this.roll + (target - this.roll) * 0.08F;
            if (this.landShake.get()) {
               boolean ground = p.onGround();
               if (ground && !this.wasOnGround && this.prevVelY < -0.4) {
                  this.shake = (float)Math.min(3.0, Math.abs(this.prevVelY) * 2.5);
               }

               this.wasOnGround = ground;
               this.prevVelY = v[1];
            }

            this.shake *= 0.82F;
            float shakeOff = this.shake * (float)Math.sin(System.currentTimeMillis() / 40.0);
            return this.roll + shakeOff;
         } else {
            this.roll *= 0.8F;
            this.shake *= 0.85F;
            return this.roll + this.shake;
         }
      }

      public float fovModify(float fov) {
         if (this.enabled && this.sprintFov.get()) {
            LocalPlayer p = Mc.player();
            float target = p != null && p.isSprinting() ? this.fovBoost.getF() : 0.0F;
            this.fovExtra = this.fovExtra + (target - this.fovExtra) * 0.12F;
            return fov + this.fovExtra;
         } else {
            this.fovExtra *= 0.85F;
            return fov + this.fovExtra;
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

      void render(float td) {
         LocalPlayer me = Mc.player();
         if (me != null) {
            double t = System.currentTimeMillis() / 1000.0;

            for (Player p : Mc.players()) {
               if (p != me && !Mc.isSpectator(p) && !(me.distanceToSqr(p) > this.range.get() * this.range.get())) {
                  double[] pos = Mc.lerpPos(p, td);
                  int rgb = this.color.argb(Mc.id(p) * 0.21) & 16777215;
                  R3.ring(pos[0], pos[1] + 0.02, pos[2], 0.55, 0.62, 36, R3.argb(rgb, 0.9), R3.argb(rgb, 0.9), 0.0);
                  R3.ring(pos[0], pos[1] + 0.02, pos[2], 0.1, 0.55, 36, R3.argb(rgb, 0.0), R3.argb(rgb, 0.25 + 0.1 * Math.sin(t * 3.0)), 0.0);
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

      private static boolean projectile(String id) {
         return id.equals("arrow")
            || id.equals("spectral_arrow")
            || id.equals("trident")
            || id.equals("ender_pearl")
            || id.equals("snowball")
            || id.equals("egg")
            || id.equals("splash_potion")
            || id.equals("lingering_potion")
            || id.equals("wind_charge")
            || id.equals("fireball");
      }

      @Override
      public void tick() {
         Set<Integer> alive = new HashSet<>();

         for (Entity e : Mc.entities()) {
            if (projectile(Mc.typeId(e))) {
               int id = Mc.id(e);
               alive.add(id);
               ArrayDeque<double[]> q = this.trails.computeIfAbsent(id, k -> new ArrayDeque<>());
               double[] last = q.peekLast();
               double[] now = new double[]{Mc.x(e), Mc.y(e), Mc.z(e)};
               if (last == null || Math.abs(last[0] - now[0]) + Math.abs(last[1] - now[1]) + Math.abs(last[2] - now[2]) > 0.02) {
                  q.add(now);
               }

               while (q.size() > this.length.getI()) {
                  q.poll();
               }
            }
         }

         this.trails.keySet().retainAll(alive);
      }

      void render() {
         for (Entry<Integer, ArrayDeque<double[]>> e : this.trails.entrySet()) {
            List<double[]> pts = new ArrayList<>(e.getValue());
            int n = pts.size();

            for (int i = 1; i < n; i++) {
               double[] a = pts.get(i - 1);
               double[] b = pts.get(i);
               int rgb = this.color.argb(e.getKey().intValue() * 0.1 + i * 0.03) & 16777215;
               double fa = (double)(i - 1) / n;
               double fb = (double)i / n;
               R3.line(a[0], a[1], a[2], b[0], b[1], b[2], 0.08 + 0.1 * fb, R3.argb(rgb, 0.8 * fa), R3.argb(rgb, 0.8 * fb));
            }
         }
      }
   }

   public static final class ShaderHands extends Module {
      final Setting.Mode mode = this.add(new Setting.Mode("Modus", 0, "Glow", "Outline", "Neon", "Galaxy"));
      final Setting.Color color = this.add(new Setting.Color("Glow-Farbe", 6));
      final Setting.Num strength = this.add(new Setting.Num("Stärke", 5.0, 40.0, 1.0, 20.0, ""));
      final Setting.Num outlineWidth = this.add(new Setting.Num("Outline-Dicke", 1.0, 6.0, 1.0, 1.0, "px"));
      final Setting.Bool useItemColor = this.add(new Setting.Bool("Item-Farbe", false));
      final Setting.Bool transparency = this.add(new Setting.Bool("Transparenz", false));
      final Setting.Num transAlpha = this.add(new Setting.Num("Transparenz-Stärke", 5.0, 80.0, 5.0, 30.0, "%"));

      ShaderHands() {
         super("shaderhands", "Shader Hands", "GPU Glow-Outline auf den Händen mit verschiedenen Modi", Category.VISUALS);
         this.icon("hand");
         this.fresh();
      }

      @Override
      public void onEnable() {
         HandEffect he = HandEffect.getInstance();
         he.setEnabled(true);
         this.applySettings(he);
      }

      @Override
      public void onDisable() {
         HandEffect.getInstance().setEnabled(false);
      }

      @Override
      public void tick() {
         if (this.enabled) {
            HandEffect he = HandEffect.getInstance();
            this.applySettings(he);
         }
      }

      private void applySettings(HandEffect he) {
         int mi = this.mode.index;
         float str = this.strength.getF();
         if (mi == 2) {
            str *= 1.6F;
         } else if (mi == 3) {
            str *= 0.7F;
         }

         he.setGlowStrength(str);
         he.setOutlineEnabled(mi != 3);
         he.setOutlineWidth((int)this.outlineWidth.get());
         he.setGlowMode(mi == 1 ? 0 : (mi == 2 ? 3 : (mi == 3 ? 1 : 2)));
         int rgb = this.color.argb() | 0xFF000000;
         if (mi == 3) {
            float t = (float)System.currentTimeMillis() / 3000.0F;
            rgb = Color.HSBtoRGB(t % 1.0F, 0.6F, 1.0F) | 0xFF000000;
         }

         he.setGlowColors(rgb);
         he.setUseItemColor(this.useItemColor.get());
         he.setTransparencyEnabled(this.transparency.get());
         he.setTransparencyAlpha(this.transAlpha.getF() / 100.0F);
      }
   }

   public static final class ShaderSky extends Module {
      final Setting.Mode mode = this.add(new Setting.Mode("Modus", 0, "Aurora", "Goobaa", "Schnee", "Himmel", "Sterne", "Glow"));
      final Setting.Num strength = this.add(new Setting.Num("Stärke", 0.05, 1.0, 0.05, 0.85, ""));
      final Setting.Num stars = this.add(new Setting.Num("Sterne", 0.0, 1.0, 0.05, 0.65, ""));
      final Setting.Bool vanillaSky = this.add(new Setting.Bool("Vanilla-Himmel", false));

      ShaderSky() {
         super("shadersky", "Shader Sky", "Gooba-Style Shader-Himmel mit Aurora, Sternen und mehr", Category.VISUALS);
         this.icon("sky");
         this.fresh();
      }

      void render(float td) {
         double cx = R3.camX();
         double cy = R3.camY();
         double cz = R3.camZ();
         float t = (float)System.currentTimeMillis() / 1000.0F;
         double a = this.strength.get();

         int base = switch (this.mode.index) {
            case 0 -> 5504982;
            case 1 -> 16735464;
            case 2 -> 15267583;
            case 3 -> 5810175;
            case 4 -> 16777215;
            default -> 16766570;
         };
         if (!this.vanillaSky.get()) {
            drawSkyBox(cx, cy, cz, base, a, t);
         }

         if (this.mode.index == 0 || this.mode.index == 1 || this.mode.index == 5) {
            for (int i = 0; i < 9; i++) {
               double ph = t * (0.25 + i * 0.03) + i * 0.9;
               double rr = 46 + i * 6;
               int col = Color.HSBtoRGB((float)((ph * 0.08 + i * 0.11) % 1.0), 0.55F, 1.0F) & 16777215;
               R3.ring(cx + Math.cos(ph) * 6.0, cy + 36.0 + i * 1.2, cz + Math.sin(ph) * 6.0, rr, rr + 3.8, 128, R3.argb(col, 0.0), R3.argb(col, 0.35 * a), ph);
            }
         }

         int count = (int)(this.stars.get() * 140.0);

         for (int i = 0; i < count; i++) {
            double h = hash(i, 13);
            double h2 = hash(i, 31);
            double h3 = hash(i, 71);
            double ang = h * Math.PI * 2.0 + t * 0.005;
            double rr = 35.0 + h2 * 95.0;
            double sy = cy + 18.0 + h3 * 78.0;
            double tw = 0.45 + 0.55 * Math.sin(t * 2.2 + i);
            R3.glowBillboard(
               Visuals2.TextTex.get("✦", -1), cx + Math.cos(ang) * rr, sy, cz + Math.sin(ang) * rr, 0.45 + h * 0.75, R3.argb(16777215, 0.36 * a * tw)
            );
         }
      }

      private static void drawSkyBox(double cx, double cy, double cz, int base, double a, float t) {
         double r = 150.0;
         double top = cy + 92.0;
         double low = cy - 8.0;
         int cTop = R3.argb(base, 0.52 * a);
         int cLow = R3.argb(329744, 0.1 * a);
         int cMid = R3.argb(base, 0.28 * a);
         R3.quad(cx - r, top, cz - r, cx + r, top, cz - r, cx + r, top, cz + r, cx - r, top, cz + r, cTop);
         R3.quad(cx - r, low, cz - r, cx + r, low, cz - r, cx + r, top, cz - r, cx - r, top, cz - r, cLow, cLow, cTop, cTop);
         R3.quad(cx + r, low, cz - r, cx + r, low, cz + r, cx + r, top, cz + r, cx + r, top, cz - r, cLow, cLow, cTop, cTop);
         R3.quad(cx + r, low, cz + r, cx - r, low, cz + r, cx - r, top, cz + r, cx + r, top, cz + r, cLow, cLow, cTop, cTop);
         R3.quad(cx - r, low, cz + r, cx - r, low, cz - r, cx - r, top, cz - r, cx - r, top, cz + r, cLow, cLow, cTop, cTop);
         double mid = cy + 18.0;
         R3.ring(cx, mid + 10.0 + Math.sin(t * 0.6) * 2.0, cz, 35.0, 142.0, 128, R3.argb(base, 0.06 * a), cMid, t * 0.025);
      }

      private static double hash(int i, int salt) {
         long x = i * 1103515245L + salt * 12345L;
         x ^= x >>> 16;
         return (x & 65535L) / 65535.0;
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

      void render(float td) {
         Entity t = HudModules.CombatTracker.lastTarget();
         if (t != null && t.isAlive() && System.currentTimeMillis() - HudModules.CombatTracker.lastAttackAt() <= 6000L) {
            double[] p = Mc.lerpPos(t, td);
            double time = System.currentTimeMillis() / 1000.0;
            int rgb = this.color.argb() & 16777215;
            double r = Mc.width(t) * 0.9 + 0.2;
            if (this.style.index != 2) {
               double y = p[1] + (0.5 + 0.5 * Math.sin(time * 2.5)) * Mc.height(t);
               R3.ring(p[0], y, p[2], r - 0.05, r + 0.05, 40, R3.argb(rgb, 0.9), R3.argb(rgb, 0.9), time);
               R3.ring(p[0], y, p[2], r, r + 0.35, 40, R3.argb(rgb, 0.35), R3.argb(rgb, 0.0), time);
            }

            if (this.style.index != 1) {
               double top = p[1] + Mc.height(t) + 0.45 + Math.sin(time * 4.0) * 0.08;
               double a = time * 2.0;
               double s = 0.18;
               double ca = Math.cos(a) * s;
               double sa = Math.sin(a) * s;
               int c = R3.argb(rgb, 0.95);
               R3.quad(p[0] - ca, top + s * 1.4, p[2] - sa, p[0] + ca, top + s * 1.4, p[2] + sa, p[0], top, p[2], p[0], top, p[2], c);
               R3.quad(p[0] - sa, top + s * 1.4, p[2] + ca, p[0] + sa, top + s * 1.4, p[2] - ca, p[0], top, p[2], p[0], top, p[2], c);
            }
         }
      }
   }

   public static final class TextTex {
      private static final Map<String, Identifier> TEX = new HashMap<>();
      private static final Map<String, Double> ASPECT = new HashMap<>();
      private static int n = 0;

      public static Identifier get(String s, int argb) {
         String key = s + "|" + argb;
         Identifier id = TEX.get(key);
         if (id != null) {
            return id;
         } else {
            float size = 48.0F;
            int w = (int)Math.ceil(Fonts.width(s, 3, size)) + 16;
            int h = 64;
            BufferedImage img = new BufferedImage(w, h, 2);
            Graphics2D g = img.createGraphics();
            Gx.hints(g);
            g.setFont(Fonts.get(3, size));
            g.setColor(new Color(0, 0, 0, 170));

            for (int dx = -2; dx <= 2; dx++) {
               for (int dy = -2; dy <= 3; dy++) {
                  g.drawString(s, 8 + dx, 48 + dy);
               }
            }

            g.setColor(new Color(argb, true));
            g.drawString(s, 8, 48);
            g.dispose();
            id = Identifier.fromNamespaceAndPath("legoclient", "text/" + n++);
            Tx.register(id, img.getRGB(0, 0, w, h, null, 0, w), w, h, true);
            TEX.put(key, id);
            ASPECT.put(key, (double)w / h);
            if (TEX.size() > 300) {
               for (Identifier old : TEX.values()) {
                  Tx.destroy(old);
               }

               TEX.clear();
               ASPECT.clear();
            }

            return id;
         }
      }

      public static double aspect(String s, int argb) {
         Double a = ASPECT.get(s + "|" + argb);
         return a == null ? 2.0 : a;
      }
   }
}
