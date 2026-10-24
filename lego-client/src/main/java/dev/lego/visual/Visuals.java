package dev.lego.visual;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.cosmetic.PetWorld;
import dev.lego.cosmetic.VehicleWorld;
import dev.lego.emote.Cutscenes;
import dev.lego.emote.Emotes;
import dev.lego.hud.HudModules;
import dev.lego.perf.Perf;
import dev.lego.perf.Performance;
import dev.lego.ui.Gx;
import dev.lego.ui.Toasts;
import dev.lego.util.Utility;
import dev.lego.visual.sky.CustomSky;
import dev.lego.visual.sky.SkyExtras;
import java.awt.Color;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class Visuals {
   public static CustomSky sky;
   public static Visuals.ColorFilter filter;
   public static Visuals.JumpCircles jumpCircles;
   public static Visuals.Crystals crystals;
   public static Visuals.TotemEffect totem;
   public static Visuals.HitParticles hit;
   public static Visuals.Trails trails;
   public static Visuals.KillEffect kill;
   public static Visuals.Fullbright fullbright;
   public static Visuals.Zoom zoom;
   public static Visuals.NoHurtCam noHurtCam;
   public static Visuals.TimeChanger time;
   public static Visuals.ClearWeather weather;
   public static Visuals.Crosshair crosshair;
   public static Visuals.CustomFov customFov;
   public static Visuals.GlintColor glint;
   static final String[] PARTICLE_NAMES = new String[]{
      "Kritisch", "Magie", "Endstab", "Totem", "Herzen", "Flammen", "Seelenfeuer", "Feuerwerk", "Funken", "Glühen", "Schnee", "Noten", "Portal", "Kirschblüten"
   };
   static final String[] PARTICLE_IDS = new String[]{
      "crit",
      "enchanted_hit",
      "end_rod",
      "totem_of_undying",
      "heart",
      "flame",
      "soul_fire_flame",
      "firework",
      "electric_spark",
      "glow",
      "snowflake",
      "note",
      "portal",
      "cherry_leaves"
   };
   private static final Map<String, long[]> BREAKER = new HashMap<>();

   private Visuals() {
   }

   static String particle(Setting.Mode m) {
      return PARTICLE_IDS[Math.max(0, Math.min(PARTICLE_IDS.length - 1, m.index))];
   }

   public static void registerAll() {
      sky = Modules.register(new CustomSky());
      SkyExtras.registerAll();
      filter = Modules.register(new Visuals.ColorFilter());
      jumpCircles = Modules.register(new Visuals.JumpCircles());
      crystals = Modules.register(new Visuals.Crystals());
      totem = Modules.register(new Visuals.TotemEffect());
      hit = Modules.register(new Visuals.HitParticles());
      trails = Modules.register(new Visuals.Trails());
      kill = Modules.register(new Visuals.KillEffect());
      // Fullbright removed: seeing in the dark is an advantage over vanilla.
      zoom = Modules.register(new Visuals.Zoom());
      noHurtCam = Modules.register(new Visuals.NoHurtCam());
      time = Modules.register(new Visuals.TimeChanger());
      weather = Modules.register(new Visuals.ClearWeather());
      crosshair = Modules.register(new Visuals.Crosshair());
      customFov = Modules.register(new Visuals.CustomFov());
      glint = Modules.register(new Visuals.GlintColor());
      Visuals2.registerAll();
      NameFx.registerAll();
      Ambient.registerAll();
      WorldFx.registerAll();
      WorldEffects.register();
      Visuals3.registerAll();
   }

   public static boolean renderSkyPass() {
      return false;
   }

   private static void renderSky() {
      boolean person = SkyExtras.person != null && SkyExtras.person.enabled;
      if (sky != null && sky.enabled || person) {
         if (sky != null && sky.enabled) {
            sky.render();
         }

         if (person) {
            SkyExtras.person.render();
         }

         R3.flush();
      }
   }

   private static boolean renderSkyPassOld() {
      if (!Mc.inGame()) {
         return false;
      } else {
         boolean person = SkyExtras.person != null && SkyExtras.person.enabled;
         if ((sky == null || !sky.enabled) && !person) {
            return false;
         } else {
            boolean replace = sky != null && sky.replacesVanilla();
            Perf.begin("Himmel");

            try {
               R3.beginSky(Mc.mc().renderBuffers().bufferSource());
               if (sky.enabled) {
                  sky.render();
               }

               if (person) {
                  SkyExtras.person.render();
               }
            } catch (Throwable var6) {
               LegoClient.LOG("Sky: " + var6);
            } finally {
               R3.end();
               Perf.end("Himmel");
            }

            return replace;
         }
      }
   }

   public static boolean skyReplaced() {
      return false;
   }

   public static void render(WorldRenderContext ctx) {
      if (Mc.inGame()) {
         if (R3.begin(ctx)) {
            try {
               float td = Mc.tickDelta();
               sec("Himmel", Visuals::renderSky);
               sec("Effekte", () -> {
                  if (jumpCircles.enabled) {
                     jumpCircles.render(td);
                  }

                  if (crystals.enabled) {
                     crystals.render(td);
                  }

                  if (totem.enabled) {
                     totem.render();
                  }

                  Visuals2.render(td);
                  Visuals3.render(td);
               });
               boolean ext = Performance.boost != null && Performance.boost.extreme() || Performance.lowMode();
               if (!ext) {
                  sec("Partikel", Ambient::render);
               }

               if (!ext) {
                  sec("Weltpartikel", WorldFx::render);
               }

               sec("Wegpunkte", () -> Utility.renderWorld(td));
               sec("Fake-Check", () -> {
                  if (Utility.fakeCheck != null) {
                     Utility.fakeCheck.renderWorld(td);
                  }
               });
               sec("Namensschild", () -> NameFx.render(td));
               sec("Haustier", () -> PetWorld.render(td));
               sec("Fahrzeug", () -> VehicleWorld.render(td));
               sec("Sky HUD", () -> SkyLyrics.render(td));
               sec("Emotes", () -> {
                  Emotes.renderBubble(td);
                  Cutscenes.renderFx(td);
               });
               sec("Chunk-Grenzen", Visuals::renderChunkBorders);
            } catch (Throwable var6) {
               LegoClient.LOG("Render-Fehler: " + var6);
            } finally {
               Perf.begin("Welt-Zeichnen");
               R3.end();
               Perf.end("Welt-Zeichnen");
            }
         }
      }
   }

   private static void sec(String name, Runnable r) {
      long now = System.currentTimeMillis();
      long[] b = BREAKER.get(name);
      if (b == null || now >= b[2]) {
         Perf.begin(name);

         try {
            r.run();
         } catch (Throwable var9) {
            if (b == null) {
               b = new long[3];
               BREAKER.put(name, b);
            }

            if (now - b[1] > 10000L) {
               b[0] = 0L;
               b[1] = now;
            }

            if (++b[0] <= 3L) {
               LegoClient.LOG(name + ": " + var9);
            }

            if (b[0] >= 5L) {
               b[2] = now + 60000L;
               b[0] = 0L;
               LegoClient.LOG(name + " wird 60 s pausiert (wiederholte Fehler)");
            }
         } finally {
            Perf.end(name);
         }
      }
   }

   public static void onStatus(LivingEntity e, byte status) {
      try {
         if (status == 35 && totem.enabled) {
            totem.onPop(e);
         }

         if (status == 3) {
            if (kill.enabled) {
               kill.onDeath(e);
            } else if (e == HudModules.CombatTracker.lastTarget() && System.currentTimeMillis() - HudModules.CombatTracker.lastAttackAt() < 5000L) {
               Utility.onKill(e);
            }

            totem.onDeath(e);
         }
      } catch (Throwable var3) {
         LegoClient.LOG("Status-Fehler: " + var3);
      }
   }

   public static void onAttack(Entity target) {
      HudModules.CombatTracker.onAttack(target);
      if (hit.enabled) {
         hit.onHit(target);
      }

      Visuals2.onAttack(target);
      Utility.onHit(target);
   }

   static void renderKillBeams() {
      if (kill.enabled) {
         kill.render();
      }
   }

   private static void renderChunkBorders() {
      Module m = Modules.get("u4_chunkborder");
      if (m != null && m.enabled) {
         LocalPlayer p = Mc.player();
         if (p != null) {
            int cx = (int)Math.floor(Mc.x(p)) >> 4 << 4;
            int cz = (int)Math.floor(Mc.z(p)) >> 4 << 4;
            double y0 = Mc.y(p) - 2.0;
            double y1 = Mc.y(p) + 8.0;
            int green = R3.argb(3789651, 0.55);
            int greenFade = R3.argb(3789651, 0.0);

            for (int dx = -1; dx <= 2; dx++) {
               for (int dz = -1; dz <= 2; dz++) {
                  double x = cx + dx * 16;
                  double z = cz + dz * 16;
                  R3.line(x, y0, cz, x, y1, cz, 0.03, green, greenFade);
                  R3.line(cx, y0, z, cx, y1, z, 0.03, green, greenFade);
                  R3.line(x, y0, cz + 16, x, y1, cz + 16, 0.03, green, greenFade);
                  R3.line(cx + 16, y0, z, cx + 16, y1, z, 0.03, green, greenFade);
               }
            }

            int yellowC = R3.argb(16764163, 0.45);
            int yellowF = R3.argb(16764163, 0.0);

            for (int dx = -1; dx <= 2; dx++) {
               for (int dz = -1; dz <= 2; dz++) {
                  double x0 = cx + dx * 16;
                  double z0 = cz + dz * 16;
                  double yb = Mc.y(p) + 0.01;
                  R3.line(x0, yb, cz, x0, yb, cz + 16, 0.02, yellowC, yellowC);
                  R3.line(cx, yb, z0, cx + 16, yb, z0, 0.02, yellowC, yellowC);
               }
            }
         }
      }
   }

   public static final class ClearWeather extends Module {
      ClearWeather() {
         super("clearweather", "Kein Regen", "Regen und Gewitter nur für dich aus", Category.VISUALS);
         this.icon("rain");
      }

      @Override
      public void tick() {
         Mc.clearWeather();
      }
   }

   public static final class ColorFilter extends Module {
      public static final String[] MODES = new String[]{
         "Kräftig", "Extrem", "Pastell", "Schwarz-Weiß", "Warm", "Kalt", "Vintage", "Lila", "Kino", "Dramatisch", "Traum", "Matrix", "Eigene Sättigung"
      };
      public static final String[] IDS = new String[]{
         "vivid", "extreme", "pastel", "grayscale", "warm", "cold", "vintage", "purple", "cinematic", "dramatic", "dream", "matrix", "custom"
      };
      final Setting.Mode mode = this.add(new Setting.Mode("Filter", 0, MODES));
      /** Used by "Eigene Sättigung": 0 % grey … 100 % normal … 200 % very colourful, in 10 % steps. */
      final Setting.Num saturation = this.add(new Setting.Num("Sättigung", 0.0, 200.0, 10.0, 130.0, "%"));

      ColorFilter() {
         super("colorfilter", "Farbsättigung & Filter", "Mehr Farbe, Kino-Look, Schwarz-Weiß, Warm, Kalt, Vintage ...", Category.VISUALS);
         this.icon("palette");
      }

      @Override
      public void tick() {
         if (dev.lego.core.Compat.noOwnRendering()) {
            this.setEnabled(false);
            return;
         }
         String id = IDS[Math.max(0, Math.min(IDS.length - 1, this.mode.index))];
         if (id.equals("custom")) {
            int sat = (int)Math.round(Math.max(0.0, Math.min(200.0, this.saturation.get())) / 10.0) * 10;
            id = "sat_" + sat;
         } else {
            id = "filter_" + id;
         }
         Identifier want = Identifier.fromNamespaceAndPath("legoclient", id);

         try {
            GameRenderer gr = Mc.mc().gameRenderer;
            Identifier cur = dev.lego.compat.McCompat.currentPostEffect(gr);
            if (!want.equals(cur)) {
               dev.lego.compat.McCompat.setPostEffect(gr, want);
            }
         } catch (Throwable var4) {
            LegoClient.LOG("Filter-Fehler: " + var4);
            this.setEnabled(false);
         }
      }

      @Override
      public void onDisable() {
         try {
            GameRenderer gr = Mc.mc().gameRenderer;
            Identifier cur = dev.lego.compat.McCompat.currentPostEffect(gr);
            if (cur != null && "legoclient".equals(cur.getNamespace())) {
               dev.lego.compat.McCompat.clearPostEffect(gr);
            }
         } catch (Throwable var3) {
         }
      }
   }

   public static final class Crosshair extends Module {
      final Setting.Mode style = this.add(new Setting.Mode("Form", 0, "Kreuz", "Punkt", "Kreuz + Punkt", "Kreis", "T-Form", "Diamant", "Pfeile", "Dreieck"));
      final Setting.Color color = this.add(new Setting.Color("Farbe", 8));
      final Setting.Num size = this.add(new Setting.Num("Größe", 2.0, 10.0, 1.0, 5.0, ""));
      final Setting.Num gap = this.add(new Setting.Num("Abstand", 0.0, 6.0, 1.0, 2.0, ""));
      final Setting.Num thick = this.add(new Setting.Num("Dicke", 1.0, 3.0, 1.0, 1.0, ""));
      final Setting.Bool outline = this.add(new Setting.Bool("Umrandung", true));
      final Setting.Bool dynamic = this.add(new Setting.Bool("Auf Gegner rot", true));
      final Setting.Bool animSpin = this.add(new Setting.Bool("Rotation bei Bewegung", false));
      private float spinAngle;

      Crosshair() {
         super("crosshair", "Custom Crosshair", "Eigenes Fadenkreuz mit vielen Formen", Category.VISUALS);
         this.icon("crosshair");
      }

      public void draw(GuiGraphics ctx) {
         int cx = ctx.guiWidth() / 2;
         int cy = ctx.guiHeight() / 2;
         int c = this.color.argb();
         int s = this.size.getI();
         int g = this.gap.getI();
         int t = this.thick.getI();
         if (this.dynamic.get() && Mc.mc().crosshairPickEntity instanceof LivingEntity) {
            c = -45730;
         }

         int o = -1073741824;
         String st = this.style.get();
         if (this.animSpin.get()) {
            LocalPlayer p = Mc.player();
            if (p != null) {
               double dx = Mc.x(p) - Mc.lastX(p);
               double dz = Mc.z(p) - Mc.lastZ(p);
               double speed = Math.sqrt(dx * dx + dz * dz);
               this.spinAngle += (float)(speed * 40.0);
            }
         } else {
            this.spinAngle *= 0.9F;
         }

         if (st.equals("Kreuz") || st.equals("Kreuz + Punkt") || st.equals("T-Form")) {
            if (!st.equals("T-Form")) {
               this.rect(ctx, cx - t / 2, cy - g - s, t, s, c, o);
            }

            this.rect(ctx, cx - t / 2, cy + g + 1, t, s, c, o);
            this.rect(ctx, cx - g - s, cy - t / 2, s, t, c, o);
            this.rect(ctx, cx + g + 1, cy - t / 2, s, t, c, o);
         }

         if (st.equals("Punkt") || st.equals("Kreuz + Punkt")) {
            this.rect(ctx, cx - t / 2, cy - t / 2, Math.max(1, t), Math.max(1, t), c, o);
         }

         if (st.equals("Kreis")) {
            int r = s + g;

            for (int i = 0; i < 32; i++) {
               double a = (Math.PI * 2) * i / 32.0 + Math.toRadians(this.spinAngle);
               int x = cx + (int)Math.round(Math.cos(a) * r);
               int y = cy + (int)Math.round(Math.sin(a) * r);
               this.rect(ctx, x, y, t, t, c, 0);
            }

            this.rect(ctx, cx, cy, 1, 1, c, 0);
         }

         if (st.equals("Diamant")) {
            int r = s + g;
            double ang = Math.toRadians(this.spinAngle);

            for (int i = 0; i < 4; i++) {
               double a = (Math.PI / 2) * i + ang;
               int x = cx + (int)Math.round(Math.cos(a) * r);
               int y = cy + (int)Math.round(Math.sin(a) * r);
               int x2 = cx + (int)Math.round(Math.cos(a + (Math.PI / 2)) * r);
               int y2 = cy + (int)Math.round(Math.sin(a + (Math.PI / 2)) * r);

               for (int j = 0; j <= s; j++) {
                  float f = (float)j / Math.max(1, s);
                  int lx = (int)(x + (x2 - x) * f);
                  int ly = (int)(y + (y2 - y) * f);
                  this.rect(ctx, lx - t / 2, ly - t / 2, t, t, c, 0);
               }
            }

            this.rect(ctx, cx, cy, 1, 1, c, 0);
         }

         if (st.equals("Pfeile")) {
            int r = g + s;
            double ang = Math.toRadians(this.spinAngle);

            for (int i = 0; i < 4; i++) {
               double a = (Math.PI / 2) * i + ang;
               double perpL = a + (Math.PI * 3.0 / 4.0);
               double perpR = a - (Math.PI * 3.0 / 4.0);
               int tipX = cx + (int)Math.round(Math.cos(a) * r);
               int tipY = cy + (int)Math.round(Math.sin(a) * r);
               int armLen = Math.max(2, s / 2 + 1);
               int lx = tipX + (int)Math.round(Math.cos(perpL) * armLen);
               int ly = tipY + (int)Math.round(Math.sin(perpL) * armLen);
               int rx = tipX + (int)Math.round(Math.cos(perpR) * armLen);
               int ry = tipY + (int)Math.round(Math.sin(perpR) * armLen);

               for (int j = 0; j <= armLen; j++) {
                  float f = (float)j / Math.max(1, armLen);
                  this.rect(ctx, (int)(tipX + (lx - tipX) * f) - t / 2, (int)(tipY + (ly - tipY) * f) - t / 2, t, t, c, 0);
                  this.rect(ctx, (int)(tipX + (rx - tipX) * f) - t / 2, (int)(tipY + (ry - tipY) * f) - t / 2, t, t, c, 0);
               }
            }
         }

         if (st.equals("Dreieck")) {
            int r = s + g;
            double ang = Math.toRadians(this.spinAngle) - (Math.PI / 2);
            int[] tx = new int[3];
            int[] ty = new int[3];

            for (int i = 0; i < 3; i++) {
               double a = ang + (Math.PI * 2.0 / 3.0) * i;
               tx[i] = cx + (int)Math.round(Math.cos(a) * r);
               ty[i] = cy + (int)Math.round(Math.sin(a) * r);
            }

            for (int i = 0; i < 3; i++) {
               int x1 = tx[i];
               int y1 = ty[i];
               int x2 = tx[(i + 1) % 3];
               int y2 = ty[(i + 1) % 3];
               int steps = Math.max(s * 2, 4);

               for (int j = 0; j <= steps; j++) {
                  float f = (float)j / steps;
                  this.rect(ctx, (int)(x1 + (x2 - x1) * f) - t / 2, (int)(y1 + (y2 - y1) * f) - t / 2, t, t, c, 0);
               }
            }

            this.rect(ctx, cx, cy, 1, 1, c, 0);
         }
      }

      private void rect(GuiGraphics ctx, int x, int y, int w, int h, int c, int o) {
         if (this.outline.get() && o != 0) {
            ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, o);
         }

         ctx.fill(x, y, x + w, y + h, c);
      }
   }

   public static final class Crystals extends Module {
      final Setting.Color color = this.add(new Setting.Color("Farbe", 5));
      final Setting.Num glowSize = this.add(new Setting.Num("Aura-Größe", 0.3, 2.0, 0.1, 0.8, "x"));
      final Setting.Num pulse = this.add(new Setting.Num("Pulsieren", 0.0, 2.0, 0.1, 1.0, "x"));
      final Setting.Bool beam = this.add(new Setting.Bool("Lichtstrahl", true));
      final Setting.Num beamHeight = this.add(new Setting.Num("Strahl-Höhe", 2.0, 20.0, 1.0, 5.0, ""));
      final Setting.Bool groundGlow = this.add(new Setting.Bool("Boden-Glow", true));
      final Setting.Bool orbitRings = this.add(new Setting.Bool("Orbit-Ringe", false));
      final Setting.Bool edges = this.add(new Setting.Bool("Würfel-Kanten", false));
      final Setting.Color color2 = this.add(new Setting.Color("Farbe 2 (Verlauf)", 7));
      final Setting.Bool gradient = this.add(new Setting.Bool("Verlauf benutzen", false));

      Crystals() {
         super("crystals", "Custom Kristalle", "Farbige Aura, Glow, Orbit-Ringe und Lichtstrahlen um End-Kristalle", Category.VISUALS);
         this.icon("gem");
      }

      void render(float td) {
         double t = System.currentTimeMillis() / 1000.0;

         for (Entity e : Mc.entities()) {
            if (Mc.isEndCrystal(e)) {
               double[] p = Mc.lerpPos(e, td);
               double dx = p[0] - R3.camX();
               double dz = p[2] - R3.camZ();
               if (!(dx * dx + dz * dz > 4096.0)) {
                  double cx = p[0];
                  double cy = p[1] + 1.0;
                  double cz = p[2];
                  double phase = Mc.id(e) * 0.13;
                  int rgb = this.color.argb(phase) & 16777215;
                  int rgb2 = this.gradient.get() ? this.color2.argb(phase + 0.3) & 16777215 : rgb;
                  double s = this.glowSize.get();
                  double pv = this.pulse.get() > 0.0 ? 0.7 + 0.3 * Math.sin(t * 2.5 * this.pulse.get()) : 1.0;
                  double r = 0.55 * s * pv;
                  R3.box(cx - r, cy - r, cz - r, cx + r, cy + r, cz + r, R3.argb(rgb, 0.18 * pv));
                  if (this.edges.get()) {
                     R3.boxEdges(cx - r, cy - r, cz - r, cx + r, cy + r, cz + r, 0.02 * s, R3.argb(rgb, 0.6 * pv));
                  }

                  if (this.beam.get()) {
                     double bh = this.beamHeight.get();
                     R3.line(cx, cy, cz, cx, cy + bh, cz, 0.08 * s, R3.argb(rgb, 0.4 * pv), R3.argb(rgb2, 0.0));
                  }

                  if (this.groundGlow.get()) {
                     R3.ring(cx, p[1] + 0.02, cz, 0.0, 1.2 * s, 32, R3.argb(rgb, 0.3 * pv), R3.argb(rgb, 0.0), 0.0);
                  }

                  if (this.orbitRings.get()) {
                     double orbitR = 0.9 * s;
                     double spin = t * 1.5 + phase * 10.0;
                     R3.ring(cx, cy + Math.sin(spin) * 0.3, cz, orbitR - 0.03, orbitR, 24, R3.argb(rgb, 0.35 * pv), R3.argb(rgb2, 0.15 * pv), spin);
                     R3.ring(
                        cx,
                        cy + Math.sin(spin + 2.0) * 0.25,
                        cz,
                        orbitR * 0.7 - 0.02,
                        orbitR * 0.7,
                        20,
                        R3.argb(rgb2, 0.25 * pv),
                        R3.argb(rgb, 0.1 * pv),
                        -spin * 1.3
                     );
                  }
               }
            }
         }
      }
   }

   public static final class CustomFov extends Module {
      final Setting.Num fov = this.add(new Setting.Num("FOV", 30.0, 160.0, 1.0, 90.0, "°"));

      CustomFov() {
         super("customfov", "Custom FOV", "Setze dein FOV von 30 bis 160", Category.VISUALS);
         this.icon("eye");
      }

      public float apply(float original) {
         return !this.enabled ? original : (float)this.fov.get();
      }
   }

   public static final class Fullbright extends Module {
      Fullbright() {
         super("fullbright", "Fullbright", "Alles hell (wie Nachtsicht, nur für dich)", Category.VISUALS);
         this.icon("sun");
      }

      @Override
      public void tick() {
         LocalPlayer p = Mc.player();
         Holder nv = Mc.nightVision();
         if (p != null && nv != null) {
            if (!p.hasEffect(nv)) {
               p.addEffect(new MobEffectInstance(nv, 1728000, 0, false, false, false));
            }
         }
      }

      @Override
      public void onDisable() {
         LocalPlayer p = Mc.player();
         Holder nv = Mc.nightVision();
         if (p != null && nv != null) {
            p.removeEffect(nv);
         }
      }
   }

   public static final class GlintColor extends Module {
      public final Setting.Color color = this.add(new Setting.Color("Farbe", 6));
      public final Setting.Bool noGlint = this.add(new Setting.Bool("Glint deaktivieren", false));
      public final Setting.Num speed = this.add(new Setting.Num("Geschwindigkeit", 0.2, 3.0, 0.1, 1.0, "x"));
      public final Setting.Num brightness = this.add(new Setting.Num("Helligkeit", 20.0, 100.0, 5.0, 80.0, "%"));

      GlintColor() {
         super("glint", "Custom Glint", "Verzauberungs-Glint Farbe anpassen oder deaktivieren", Category.VISUALS);
         this.icon("enchant");
         this.fresh();
      }

      public boolean shouldHideGlint() {
         return this.enabled && this.noGlint.get();
      }

      public int glintArgb() {
         int rgb = this.color.argb(System.currentTimeMillis() / 1000.0 * this.speed.get() * 0.3) & 16777215;
         int a = (int)(255.0 * this.brightness.get() / 100.0);
         return a << 24 | rgb;
      }

      public float[] glintRgbf() {
         int c = this.glintArgb();
         float b = (float)(this.brightness.get() / 100.0);
         return new float[]{(c >> 16 & 0xFF) / 255.0F * b, (c >> 8 & 0xFF) / 255.0F * b, (c & 0xFF) / 255.0F * b};
      }
   }

   public static final class HitParticles extends Module {
      final Setting.Mode type = this.add(new Setting.Mode("Partikel", 1, Visuals.PARTICLE_NAMES));
      final Setting.Num amount = this.add(new Setting.Num("Menge", 1.0, 30.0, 1.0, 8.0, ""));

      HitParticles() {
         super("hitparticles", "Hit-Partikel", "Extra Partikel wenn du triffst", Category.VISUALS);
         this.icon("hit");
      }

      public void onHit(Entity target) {
         String id = Visuals.particle(this.type);
         double x = Mc.x(target);
         double y = Mc.y(target) + Mc.height(target) * 0.6;
         double z = Mc.z(target);

         for (int i = 0; i < this.amount.getI(); i++) {
            Mc.spawn(
               id,
               x + (Math.random() - 0.5) * 0.6,
               y + (Math.random() - 0.5) * 0.6,
               z + (Math.random() - 0.5) * 0.6,
               (Math.random() - 0.5) * 0.3,
               Math.random() * 0.25,
               (Math.random() - 0.5) * 0.3
            );
         }
      }
   }

   public static final class JumpCircles extends Module {
      final Setting.Color color = this.add(new Setting.Color("Farbe", 11));
      final Setting.Num size = this.add(new Setting.Num("Größe", 0.5, 2.5, 0.1, 1.2, "x"));
      final Setting.Mode style = this.add(
         new Setting.Mode(
            "Stil", 0, "Glow-Ring", "Doppelring", "Welle", "Nova", "Spirale", "Funkenregen", "Säule", "Pulsar", "Stern", "Blitze", "Aufsteigend", "Regenbogen"
         )
      );
      final Setting.Bool everyone = this.add(new Setting.Bool("Alle Spieler", false));
      private final List<double[]> circles = new ArrayList<>();
      private final Map<Integer, Boolean> ground = new HashMap<>();

      JumpCircles() {
         super("jumpcircles", "Jump Circles", "12 Effekte beim Springen: Ringe, Nova, Spirale, Funken, Blitze ...", Category.VISUALS);
         this.icon("jump");
      }

      @Override
      public void tick() {
         LocalPlayer me = Mc.player();
         if (me != null) {
            for (Player p : this.everyone.get() ? Mc.players() : List.of(me)) {
               int id = Mc.id(p);
               boolean og = Mc.onGround(p);
               Boolean was = this.ground.put(id, og);
               if (was != null && was && !og && Mc.velocity(p)[1] > 0.2) {
                  if (this.circles.size() > 24) {
                     this.circles.remove(0);
                  }

                  this.circles.add(new double[]{Mc.x(p), Mc.y(p) + 0.03, Mc.z(p), System.currentTimeMillis()});
               }
            }

            if (this.ground.size() > 200) {
               this.ground.clear();
            }
         }
      }

      private void ringFlat(double[] c, double r, double w, int rgb, double alpha, double rot) {
         int seg = 48;

         for (int i = 0; i < seg; i++) {
            double a0 = rot + (Math.PI * 2) * i / seg;
            double a1 = rot + (Math.PI * 2) * (i + 1) / seg;
            int col = this.color.argb((double)i / seg) & 16777215;
            if (!this.color.rainbow()) {
               col = rgb;
            }

            int ci = R3.argb(col, 0.0);
            int co = R3.argb(col, alpha);
            double c0 = Math.cos(a0);
            double s0 = Math.sin(a0);
            double c1 = Math.cos(a1);
            double s1 = Math.sin(a1);
            R3.quad(
               c[0] + c0 * (r - w * 3.0),
               c[1],
               c[2] + s0 * (r - w * 3.0),
               c[0] + c0 * r,
               c[1],
               c[2] + s0 * r,
               c[0] + c1 * r,
               c[1],
               c[2] + s1 * r,
               c[0] + c1 * (r - w * 3.0),
               c[1],
               c[2] + s1 * (r - w * 3.0),
               ci,
               co,
               co,
               ci
            );
            R3.quad(
               c[0] + c0 * r,
               c[1],
               c[2] + s0 * r,
               c[0] + c0 * (r + w),
               c[1],
               c[2] + s0 * (r + w),
               c[0] + c1 * (r + w),
               c[1],
               c[2] + s1 * (r + w),
               c[0] + c1 * r,
               c[1],
               c[2] + s1 * r,
               co,
               R3.argb(col, 0.0),
               R3.argb(col, 0.0),
               co
            );
         }
      }

      private static double ease(double t) {
         return 1.0 - Math.pow(1.0 - Math.max(0.0, Math.min(1.0, t)), 3.0);
      }

      void render(float td) {
         long now = System.currentTimeMillis();
         double sz = this.size.get();
         Iterator<double[]> it = this.circles.iterator();

         label156:
         while (it.hasNext()) {
            double[] c = it.next();
            double t = (now - c[3]) / 1000.0;
            if (t >= 1.2) {
               it.remove();
            } else {
               double tn = Math.min(1.0, t);
               double fade = 1.0 - tn;
               int rgb = this.color.argb(c[3] % 1000.0 / 1000.0) & 16777215;
               double w = 0.09 * sz;
               Random rnd = new Random((long)c[3]);
               int k;
               switch (this.style.index) {
                  case 0:
                     this.ringFlat(c, sz * (0.25 + 0.75 * ease(tn)), w, rgb, 0.85 * fade, 0.0);
                     continue;
                  case 1:
                     k = 0;

                     while (true) {
                        if (k >= 2) {
                           continue label156;
                        }

                        double tk = Math.max(0.0, Math.min(1.0, tn * (1.0 + k * 0.25) - k * 0.12));
                        if (tk > 0.0) {
                           this.ringFlat(c, sz * (0.25 + 0.75 * ease(tk)) * (1.0 - k * 0.18), w, rgb, 0.85 * (1.0 - tk), 0.0);
                        }

                        k++;
                     }
                  case 2:
                     k = 0;

                     while (true) {
                        if (k >= 3) {
                           continue label156;
                        }

                        double tk = Math.max(0.0, Math.min(1.0, tn * 1.3 - k * 0.18));
                        if (tk > 0.0) {
                           this.ringFlat(c, sz * (0.2 + 0.9 * ease(tk)), w * 0.9, rgb, 0.8 * (1.0 - tk), 0.0);
                        }

                        k++;
                     }
                  case 3:
                     double e = ease(tn * 1.4);
                     this.ringFlat(c, sz * (0.15 + 1.2 * e), w * 1.6, rgb, 0.95 * fade * fade, 0.0);
                     double fr = sz * 0.5 * (1.0 - tn);
                     int core = R3.argb(16777215, 0.8 * fade);
                     R3.quad(
                        c[0] - fr,
                        c[1] + 0.01,
                        c[2] - fr,
                        c[0] + fr,
                        c[1] + 0.01,
                        c[2] - fr,
                        c[0] + fr,
                        c[1] + 0.01,
                        c[2] + fr,
                        c[0] - fr,
                        c[1] + 0.01,
                        c[2] + fr,
                        core
                     );
                     continue;
                  case 4:
                     k = 0;

                     while (true) {
                        if (k >= 2) {
                           continue label156;
                        }

                        for (int i = 0; i < 40; i++) {
                           double f = i / 39.0;
                           double ang = k * Math.PI + f * 5.0 + t * 6.0;
                           double r = sz * (0.15 + 1.0 * f) * ease(tn * 1.2);
                           double a = Math.max(0.0, 1.0 - tn) * (1.0 - f * 0.6);
                           R3.cube(c[0] + Math.cos(ang) * r, c[1] + 0.04, c[2] + Math.sin(ang) * r, 0.1 * sz, R3.argb(rgb, a));
                        }

                        k++;
                     }
                  case 5:
                     k = 0;

                     while (true) {
                        if (k >= 28) {
                           continue label156;
                        }

                        double ang = rnd.nextDouble() * Math.PI * 2.0;
                        double sp = (0.5 + rnd.nextDouble()) * sz;
                        double up = 1.2 + rnd.nextDouble() * 1.4;
                        double x = c[0] + Math.cos(ang) * sp * tn;
                        double z = c[2] + Math.sin(ang) * sp * tn;
                        double y = c[1] + up * t - 3.2 * t * t;
                        if (!(y < c[1])) {
                           R3.cube(x, y, z, 0.07 * sz + 0.03, R3.argb(rgb, fade));
                        }

                        k++;
                     }
                  case 6:
                     double r = sz * 0.55;
                     double h = 0.4 + 2.2 * ease(tn);
                     int seg = 24;

                     for (int i = 0; i < seg; i++) {
                        double a0 = (Math.PI * 2) * i / seg;
                        double a1 = (Math.PI * 2) * (i + 1) / seg;
                        R3.quad(
                           c[0] + Math.cos(a0) * r,
                           c[1],
                           c[2] + Math.sin(a0) * r,
                           c[0] + Math.cos(a1) * r,
                           c[1],
                           c[2] + Math.sin(a1) * r,
                           c[0] + Math.cos(a1) * r * 0.7,
                           c[1] + h,
                           c[2] + Math.sin(a1) * r * 0.7,
                           c[0] + Math.cos(a0) * r * 0.7,
                           c[1] + h,
                           c[2] + Math.sin(a0) * r * 0.7,
                           R3.argb(rgb, 0.45 * fade),
                           R3.argb(rgb, 0.45 * fade),
                           R3.argb(rgb, 0.0),
                           R3.argb(rgb, 0.0)
                        );
                     }

                     this.ringFlat(c, r, w, rgb, 0.8 * fade, 0.0);
                     continue;
                  case 7:
                     k = 0;

                     while (true) {
                        if (k >= 4) {
                           continue label156;
                        }

                        double tk = Math.max(0.0, Math.min(1.0, tn - k * 0.09));
                        if (tk > 0.0) {
                           this.ringFlat(c, sz * (0.2 + 0.8 * ease(tk)), w * 0.6, rgb, 0.9 * (1.0 - tk), 0.0);
                        }

                        k++;
                     }
                  case 8:
                     k = 8;
                     double len = sz * 1.1 * ease(tn * 1.3);
                     double rot = t * 3.0;

                     for (int i = 0; i < k; i++) {
                        double a = rot + (Math.PI * 2) * i / k;
                        R3.line(
                           c[0],
                           c[1] + 0.05,
                           c[2],
                           c[0] + Math.cos(a) * len,
                           c[1] + 0.05,
                           c[2] + Math.sin(a) * len,
                           0.07 * sz,
                           R3.argb(rgb, 0.9 * fade),
                           R3.argb(rgb, 0.0)
                        );
                     }

                     this.ringFlat(c, len * 0.35, w, rgb, 0.7 * fade, rot);
                     continue;
                  case 9:
                     k = 0;

                     while (true) {
                        if (k >= 6) {
                           continue label156;
                        }

                        double a = rnd.nextDouble() * Math.PI * 2.0;
                        double px = c[0];
                        double pz = c[2];
                        int segs = 5;

                        for (int s = 1; s <= segs; s++) {
                           double rr = sz * 1.2 * ease(tn * 1.5) * s / segs;
                           double jit = (rnd.nextDouble() - 0.5) * 0.5;
                           double nx = c[0] + Math.cos(a + jit) * rr;
                           double nz = c[2] + Math.sin(a + jit) * rr;
                           R3.line(px, c[1] + 0.06, pz, nx, c[1] + 0.06, nz, 0.05 * sz, R3.argb(16777215, 0.9 * fade), R3.argb(rgb, 0.9 * fade));
                           px = nx;
                           pz = nz;
                        }

                        k++;
                     }
                  case 10:
                     k = 0;

                     while (true) {
                        if (k >= 4) {
                           continue label156;
                        }

                        double tk = Math.max(0.0, Math.min(1.0, tn * 1.2 - k * 0.12));
                        if (!(tk <= 0.0)) {
                           double[] cc = new double[]{c[0], c[1] + tk * 1.8, c[2]};
                           this.ringFlat(cc, sz * 0.7 * (1.0 - 0.3 * tk), w * 0.8, rgb, 0.85 * (1.0 - tk), t * 2.0 + k);
                        }

                        k++;
                     }
                  default:
                     k = 0;
               }

               for (; k < 4; k++) {
                  double tk = Math.max(0.0, Math.min(1.0, tn * 1.2 - k * 0.08));
                  if (!(tk <= 0.0)) {
                     int col = Color.HSBtoRGB((float)((k * 0.17 + t) % 1.0), 0.75F, 1.0F) & 16777215;
                     this.ringFlat(c, sz * (0.2 + 1.0 * ease(tk)), w, col, 0.85 * (1.0 - tk), 0.0);
                  }
               }

               for (int i = 0; i < 16; i++) {
                  double ang = rnd.nextDouble() * Math.PI * 2.0;
                  double sp = (0.4 + rnd.nextDouble() * 0.9) * sz;
                  int col = Color.HSBtoRGB((float)rnd.nextDouble(), 0.7F, 1.0F) & 16777215;
                  R3.cube(
                     c[0] + Math.cos(ang) * sp * tn, c[1] + 0.8 * t - 1.6 * t * t + 0.1, c[2] + Math.sin(ang) * sp * tn, 0.08 * sz + 0.03, R3.argb(col, fade)
                  );
               }
            }
         }
      }
   }

   public static final class KillEffect extends Module {
      final Setting.Mode type = this.add(new Setting.Mode("Partikel", 6, Visuals.PARTICLE_NAMES));
      final Setting.Bool lightning = this.add(new Setting.Bool("Blitz-Strahl", true));
      private final List<double[]> beams = new ArrayList<>();

      KillEffect() {
         super("killeffect", "Kill-Effekt", "Partikel-Explosion und Lichtstrahl wenn dein Gegner stirbt", Category.VISUALS);
         this.icon("skull");
      }

      public void onDeath(Entity e) {
         Entity lt = HudModules.CombatTracker.lastTarget();
         if (lt != null && Mc.id(e) == Mc.id(lt)) {
            if (System.currentTimeMillis() - HudModules.CombatTracker.lastAttackAt() <= 10000L) {
               String id = Visuals.particle(this.type);

               for (int i = 0; i < 70; i++) {
                  double a = Math.random() * Math.PI * 2.0;
                  double s = 0.1 + Math.random() * 0.35;
                  Mc.spawn(id, Mc.x(e), Mc.y(e) + 0.2 + Math.random() * Mc.height(e), Mc.z(e), Math.cos(a) * s, Math.random() * 0.4, Math.sin(a) * s);
               }

               if (this.lightning.get()) {
                  this.beams.add(new double[]{Mc.x(e), Mc.y(e), Mc.z(e), System.currentTimeMillis()});
               }

               Utility.onKill(e);
            }
         }
      }

      void render() {
         long now = System.currentTimeMillis();
         Iterator<double[]> it = this.beams.iterator();

         while (it.hasNext()) {
            double[] b = it.next();
            double t = (now - b[3]) / 900.0;
            if (t >= 1.0) {
               it.remove();
            } else {
               double a = 1.0 - t;
               int acc = Modules.accentArgb() & 16777215;
               R3.line(b[0], b[1], b[2], b[0], b[1] + 40.0, b[2], 0.5 * a + 0.1, R3.argb(16777215, 0.9 * a), R3.argb(acc, 0.0));
               R3.line(b[0], b[1], b[2], b[0], b[1] + 25.0, b[2], 1.4 * a, R3.argb(acc, 0.35 * a), R3.argb(acc, 0.0));
               R3.ring(b[0], b[1] + 0.05, b[2], t * 3.0, t * 3.0 + 0.25, 40, R3.argb(acc, 0.7 * a), R3.argb(acc, 0.0), 0.0);
            }
         }
      }
   }

   public static final class NoHurtCam extends Module {
      NoHurtCam() {
         super("nohurtcam", "Kein Wackeln", "Kein Bildschirm-Wackeln bei Schaden", Category.VISUALS);
         this.icon("tilt");
      }
   }

   public static final class TimeChanger extends Module {
      final Setting.Num time = this.add(new Setting.Num("Uhrzeit", 0.0, 24000.0, 250.0, 18000.0, ""));
      final Setting.Bool cycle = this.add(new Setting.Bool("Schneller Tag-Nacht-Zyklus", false));
      private double t = -1.0;

      TimeChanger() {
         super("timechanger", "Zeit ändern", "Tag oder Nacht nur für dich", Category.VISUALS);
         this.icon("moon");
      }

      @Override
      public void tick() {
         if (this.cycle.get()) {
            if (this.t < 0.0) {
               this.t = this.time.get();
            }

            this.t = (this.t + 40.0) % 24000.0;
            Mc.setTimeOfDay((long)this.t);
         } else {
            this.t = -1.0;
            Mc.setTimeOfDay((long)this.time.get());
         }
      }
   }

   public static final class TotemEffect extends Module {
      final Setting.Color color = this.add(new Setting.Color("Farbe", 2));
      final Setting.Num rings = this.add(new Setting.Num("Ringe", 1.0, 8.0, 1.0, 3.0, ""));
      final Setting.Num ringSize = this.add(new Setting.Num("Ring-Größe", 0.5, 3.0, 0.1, 1.0, "x"));
      final Setting.Num ringSpeed = this.add(new Setting.Num("Ring-Tempo", 0.3, 3.0, 0.1, 1.0, "x"));
      final Setting.Num ringWidth = this.add(new Setting.Num("Ring-Dicke", 0.02, 0.3, 0.01, 0.06, ""));
      final Setting.Bool particles = this.add(new Setting.Bool("Extra Partikel", true));
      final Setting.Num particleCount = this.add(new Setting.Num("Partikel-Menge", 5.0, 100.0, 5.0, 40.0, ""));
      final Setting.Bool message = this.add(new Setting.Bool("Meldung mit Zähler", true));
      final Setting.Bool screenFlash = this.add(new Setting.Bool("Bildschirm-Blitz", true));
      final Setting.Bool screenIcon = this.add(new Setting.Bool("Totem-Icon auf dem Bildschirm", true));
      final Setting.Num iconSize = this.add(new Setting.Num("Icon-Größe", 0.5, 3.0, 0.1, 1.2, "x"));
      final Setting.Bool shockwave = this.add(new Setting.Bool("Schockwelle", true));
      private final List<double[]> pops = new ArrayList<>();
      private final Map<String, Integer> counts = new HashMap<>();
      private long screenPopAt;

      TotemEffect() {
         super("totem", "Custom Totem", "Ringe, Partikel, Pop-Zähler, Bildschirm-Icon und Schockwelle bei Totems", Category.VISUALS);
         this.icon("totem");
      }

      public void onPop(Entity e) {
         double[] p = new double[]{Mc.x(e), Mc.y(e), Mc.z(e), System.currentTimeMillis(), Mc.height(e)};
         this.pops.add(p);
         if (Mc.isPlayer(e) && Mc.player() != null && Mc.id(e) == Mc.id(Mc.player())) {
            this.screenPopAt = System.currentTimeMillis();
         }

         if (this.particles.get()) {
            int count = this.particleCount.getI();

            for (int i = 0; i < count; i++) {
               double a = Math.random() * Math.PI * 2.0;
               double s = 0.2 + Math.random() * 0.4;
               Mc.spawn(i % 3 == 0 ? "end_rod" : "totem_of_undying", p[0], p[1] + 1.0, p[2], Math.cos(a) * s, 0.2 + Math.random() * 0.5, Math.sin(a) * s);
            }
         }

         if (this.message.get() && Mc.isPlayer(e)) {
            String name = Mc.name(e);
            int n = this.counts.merge(name, 1, Integer::sum);
            Toasts.show("totem", name + " hat gepoppt", n + " Totem" + (n == 1 ? "" : "s") + " bisher", -13053, 3000L);
         }
      }

      public void onDeath(Entity e) {
         if (Mc.isPlayer(e)) {
            this.counts.remove(Mc.name(e));
         }
      }

      void render() {
         long now = System.currentTimeMillis();
         int nRings = this.rings.getI();
         double sz = this.ringSize.get();
         double spd = this.ringSpeed.get();
         double thick = this.ringWidth.get();
         Iterator<double[]> it = this.pops.iterator();

         while (it.hasNext()) {
            double[] p = it.next();
            double t = (now - p[3]) / (1300.0 / spd);
            if (t >= 1.0) {
               it.remove();
            } else {
               double e = 1.0 - Math.pow(1.0 - t, 3.0);

               for (int k = 0; k < nRings; k++) {
                  double frac = nRings > 1 ? (double)k / (nRings - 1) : 0.0;
                  double y = p[1] + p[4] * (0.2 + 0.6 * frac) + e * 0.6;
                  double r = (0.4 + e * (1.2 + frac * 0.6)) * sz;
                  int rgb = this.color.argb(frac * 0.4) & 16777215;
                  R3.ring(p[0], y, p[2], r - thick, r, 40, R3.argb(rgb, 0.9 * (1.0 - t)), R3.argb(rgb, 0.3 * (1.0 - t)), t * 3.0 * spd + k);
               }

               if (this.shockwave.get() && t < 0.4) {
                  double sw = e * 3.0 * sz;
                  int rgb = this.color.argb(0.0) & 16777215;
                  R3.ring(p[0], p[1] + 0.05, p[2], sw - 0.08, sw, 48, R3.argb(rgb, 0.5 * (1.0 - t / 0.4)), R3.argb(rgb, 0.1 * (1.0 - t / 0.4)), 0.0);
               }

               if (this.screenFlash.get() && t < 0.15) {
                  int rgb = this.color.argb(0.0) & 16777215;
                  int W = Mc.mc().getWindow().getScreenWidth();
                  int H = Mc.mc().getWindow().getScreenHeight();
                  Gx.rect(0, 0, W, H, 0, rgb & 16777215 | (int)(255.0 * (1.0 - t / 0.15) * 0.25) << 24);
               }
            }
         }
      }

      public void drawScreenIcon(int W, int H) {
         if (this.screenIcon.get()) {
            long age = System.currentTimeMillis() - this.screenPopAt;
            if (age <= 1200L && age >= 0L) {
               float t = (float)age / 1200.0F;
               float alpha = t < 0.15F ? t / 0.15F : 1.0F - (t - 0.15F) / 0.85F;
               float scale = 0.6F + 0.4F * (1.0F - (1.0F - Math.min(1.0F, t * 4.0F)) * (1.0F - Math.min(1.0F, t * 4.0F)));
               float k = (float)this.iconSize.get() * scale;
               int s = Math.round(W * 0.06F * k);
               int cx = W / 2;
               int cy = H / 2 - s / 2 - Math.round(W * 0.04F);
               int rgb = this.color.argb(0.0);
               Gx.pushAlpha(alpha * alpha);
               Gx.push();
               Gx.scaleAt(cx, cy + s / 2.0F, scale);
               Gx.glow(cx, cy + s / 2, s, Gx.withAlpha(rgb, 0.35F * alpha));
               Gx.icon("totem", cx, cy + s / 2.0F, s * 0.7F, Gx.withAlpha(rgb, 0.9F));
               Gx.pop();
               Gx.popAlpha();
            }
         }
      }
   }

   public static final class Trails extends Module {
      final Setting.Mode type = this.add(new Setting.Mode("Partikel", 2, Visuals.PARTICLE_NAMES));
      final Setting.Bool onlyMoving = this.add(new Setting.Bool("Nur beim Laufen", true));

      Trails() {
         super("trails", "Trails", "Partikelspur hinter dir", Category.VISUALS);
         this.icon("trail");
      }

      @Override
      public void tick() {
         LocalPlayer p = Mc.player();
         if (p != null) {
            double dx = Mc.x(p) - Mc.lastX(p);
            double dz = Mc.z(p) - Mc.lastZ(p);
            if (!this.onlyMoving.get() || !(dx * dx + dz * dz < 4.0E-4)) {
               Mc.spawn(
                  Visuals.particle(this.type), Mc.x(p) + (Math.random() - 0.5) * 0.3, Mc.y(p) + 0.1, Mc.z(p) + (Math.random() - 0.5) * 0.3, 0.0, 0.01, 0.0
               );
            }
         }
      }
   }

   public static final class Zoom extends Module {
      final Setting.Num factor = this.add(new Setting.Num("Zoom", 2.0, 12.0, 0.5, 4.0, "x"));
      final Setting.Bool smooth = this.add(new Setting.Bool("Weich", true));
      final Setting.Bool scroll = this.add(new Setting.Bool("Mausrad ändert Zoom", true));
      final Setting.Bool cinematic = this.add(new Setting.Bool("Filmische Kamera beim Zoomen", false));
      private double current = 1.0;
      private double extra = 0.0;
      public boolean held;
      private boolean savedSmooth;

      Zoom() {
         super("zoom", "Zoom", "Taste C halten zum Zoomen, Mausrad = stärker/schwächer", Category.VISUALS);
         this.enabled = true;
         this.icon("zoom");
      }

      public boolean active() {
         return this.enabled && this.held && !Mc.screenOpen();
      }

      public float apply(float fov) {
         double target = this.active() ? Math.max(1.1, this.factor.get() + this.extra) : 1.0;
         this.current = this.smooth.get() ? this.current + (target - this.current) * 0.25 : target;
         if (Math.abs(this.current - target) < 0.01) {
            this.current = target;
         }

         return (float)(fov / this.current);
      }

      public boolean onScroll(double v) {
         if (this.active() && this.scroll.get()) {
            this.extra = Math.max(
               1.0 - this.factor.get(), Math.min(30.0, this.extra + (v > 0.0 ? 1 : -1) * Math.max(0.5, (this.factor.get() + this.extra) * 0.15))
            );
            return true;
         } else {
            return false;
         }
      }

      @Override
      public void tick() {
         if (!this.held) {
            this.extra = 0.0;
         }

         if (this.cinematic.get()) {
            Options o = Mc.options();
            if (this.active() && !o.smoothCamera) {
               this.savedSmooth = false;
               o.smoothCamera = true;
            } else if (!this.active() && o.smoothCamera && !this.savedSmooth) {
               o.smoothCamera = false;
               this.savedSmooth = true;
            }
         }
      }
   }
}
