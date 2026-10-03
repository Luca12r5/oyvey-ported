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

   static String particle(Setting.Mode var0) {
      return PARTICLE_IDS[Math.max(0, Math.min(PARTICLE_IDS.length - 1, var0.index))];
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
      fullbright = Modules.register(new Visuals.Fullbright());
      zoom = Modules.register(new Visuals.Zoom());
      noHurtCam = Modules.register(new Visuals.NoHurtCam());
      time = Modules.register(new Visuals.TimeChanger());
      weather = Modules.register(new Visuals.ClearWeather());
      crosshair = Modules.register(new Visuals.Crosshair());
      Visuals2.registerAll();
      NameFx.registerAll();
      Ambient.registerAll();
      WorldFx.registerAll();
   }

   public static boolean renderSkyPass() {
      return false;
   }

   private static void renderSky() {
      boolean var0 = SkyExtras.person != null && SkyExtras.person.enabled;
      if (sky != null && sky.enabled || var0) {
         if (sky != null && sky.enabled) {
            sky.render();
         }

         if (var0) {
            SkyExtras.person.render();
         }

         R3.flush();
      }
   }

   private static boolean renderSkyPassOld() {
      if (!Mc.inGame()) {
         return false;
      } else {
         boolean var0 = SkyExtras.person != null && SkyExtras.person.enabled;
         if ((sky == null || !sky.enabled) && !var0) {
            return false;
         } else {
            boolean var1 = sky != null && sky.replacesVanilla();
            Perf.begin("Himmel");

            try {
               R3.beginSky(Mc.mc().renderBuffers().bufferSource());
               if (sky.enabled) {
                  sky.render();
               }

               if (var0) {
                  SkyExtras.person.render();
               }
            } catch (Throwable var6) {
               LegoClient.LOG("Sky: " + var6);
            } finally {
               R3.end();
               Perf.end("Himmel");
            }

            return var1;
         }
      }
   }

   public static boolean skyReplaced() {
      return false;
   }

   public static void render(WorldRenderContext var0) {
      if (Mc.inGame()) {
         if (R3.begin(var0)) {
            try {
               float var1 = Mc.tickDelta();
               sec("Himmel", Visuals::renderSky);
               sec("Effekte", () -> {
                  if (jumpCircles.enabled) {
                     jumpCircles.render(var1);
                  }

                  if (crystals.enabled) {
                     crystals.render(var1);
                  }

                  if (totem.enabled) {
                     totem.render();
                  }

                  Visuals2.render(var1);
               });
               boolean var2 = Performance.boost != null && Performance.boost.extreme() || Performance.lowMode();
               if (!var2) {
                  sec("Partikel", Ambient::render);
               }

               if (!var2) {
                  sec("Weltpartikel", WorldFx::render);
               }

               sec("Wegpunkte", () -> Utility.renderWorld(var1));
               sec("Namensschild", () -> NameFx.render(var1));
               sec("Haustier", () -> PetWorld.render(var1));
               sec("Fahrzeug", () -> VehicleWorld.render(var1));
               sec("Emotes", () -> {
                  Emotes.renderBubble(var1);
                  Cutscenes.renderFx(var1);
               });
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

   private static void sec(String var0, Runnable var1) {
      long var2 = System.currentTimeMillis();
      long[] var4 = BREAKER.get(var0);
      if (var4 == null || var2 >= var4[2]) {
         Perf.begin(var0);

         try {
            var1.run();
         } catch (Throwable var9) {
            if (var4 == null) {
               var4 = new long[3];
               BREAKER.put(var0, var4);
            }

            if (var2 - var4[1] > 10000L) {
               var4[0] = 0L;
               var4[1] = var2;
            }

            if (++var4[0] <= 3L) {
               LegoClient.LOG(var0 + ": " + var9);
            }

            if (var4[0] >= 5L) {
               var4[2] = var2 + 60000L;
               var4[0] = 0L;
               LegoClient.LOG(var0 + " wird 60 s pausiert (wiederholte Fehler)");
            }
         } finally {
            Perf.end(var0);
         }
      }
   }

   public static void onStatus(LivingEntity var0, byte var1) {
      try {
         if (var1 == 35 && totem.enabled) {
            totem.onPop(var0);
         }

         if (var1 == 3) {
            if (kill.enabled) {
               kill.onDeath(var0);
            } else if (var0 == HudModules.CombatTracker.lastTarget() && System.currentTimeMillis() - HudModules.CombatTracker.lastAttackAt() < 5000L) {
               Utility.onKill(var0);
            }

            totem.onDeath(var0);
         }
      } catch (Throwable var3) {
         LegoClient.LOG("Status-Fehler: " + var3);
      }
   }

   public static void onAttack(Entity var0) {
      HudModules.CombatTracker.onAttack(var0);
      if (hit.enabled) {
         hit.onHit(var0);
      }

      Visuals2.onAttack(var0);
      Utility.onHit(var0);
   }

   static void renderKillBeams() {
      if (kill.enabled) {
         kill.render();
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
         "Kräftig", "Extrem", "Pastell", "Schwarz-Weiß", "Warm", "Kalt", "Vintage", "Lila", "Kino", "Dramatisch", "Traum", "Matrix"
      };
      public static final String[] IDS = new String[]{
         "vivid", "extreme", "pastel", "grayscale", "warm", "cold", "vintage", "purple", "cinematic", "dramatic", "dream", "matrix"
      };
      final Setting.Mode mode = this.add(new Setting.Mode("Filter", 0, MODES));
      private static Method setPost;

      ColorFilter() {
         super("colorfilter", "Farbsättigung & Filter", "Mehr Farbe, Kino-Look, Schwarz-Weiß, Warm, Kalt, Vintage ...", Category.VISUALS);
         this.icon("palette");
      }

      @Override
      public void tick() {
         Identifier var1 = Identifier.fromNamespaceAndPath("legoclient", "filter_" + IDS[Math.max(0, Math.min(IDS.length - 1, this.mode.index))]);

         try {
            GameRenderer var2 = Mc.mc().gameRenderer;
            Identifier var3 = var2.currentPostEffect();
            if (!var1.equals(var3)) {
               setPost(var2, var1);
            }
         } catch (Throwable var4) {
            LegoClient.LOG("Filter-Fehler: " + var4);
            this.setEnabled(false);
         }
      }

      private static void setPost(GameRenderer var0, Identifier var1) throws Exception {
         if (setPost == null) {
            setPost = GameRenderer.class.getDeclaredMethod(dev.lego.util.Remap.method("net.minecraft.class_757", "method_62904", "(Lnet/minecraft/class_2960;)V"), Identifier.class);
            setPost.setAccessible(true);
         }

         setPost.invoke(var0, var1);
      }

      @Override
      public void onDisable() {
         try {
            GameRenderer var1 = Mc.mc().gameRenderer;
            Identifier var2 = var1.currentPostEffect();
            if (var2 != null && "legoclient".equals(var2.getNamespace())) {
               var1.clearPostEffect();
            }
         } catch (Throwable var3) {
         }
      }
   }

   public static final class Crosshair extends Module {
      final Setting.Mode style = this.add(new Setting.Mode("Form", 0, "Kreuz", "Punkt", "Kreuz + Punkt", "Kreis", "T-Form"));
      final Setting.Color color = this.add(new Setting.Color("Farbe", 8));
      final Setting.Num size = this.add(new Setting.Num("Größe", 2.0, 10.0, 1.0, 5.0, ""));
      final Setting.Num gap = this.add(new Setting.Num("Abstand", 0.0, 6.0, 1.0, 2.0, ""));
      final Setting.Num thick = this.add(new Setting.Num("Dicke", 1.0, 3.0, 1.0, 1.0, ""));
      final Setting.Bool outline = this.add(new Setting.Bool("Umrandung", true));
      final Setting.Bool dynamic = this.add(new Setting.Bool("Auf Gegner rot", true));

      Crosshair() {
         super("crosshair", "Custom Crosshair", "Eigenes Fadenkreuz", Category.VISUALS);
         this.icon("crosshair");
      }

      public void draw(GuiGraphics var1) {
         int var2 = var1.guiWidth() / 2;
         int var3 = var1.guiHeight() / 2;
         int var4 = this.color.argb();
         int var5 = this.size.getI();
         int var6 = this.gap.getI();
         int var7 = this.thick.getI();
         if (this.dynamic.get() && Mc.mc().crosshairPickEntity instanceof LivingEntity) {
            var4 = -45730;
         }

         int var8 = -1073741824;
         String var9 = this.style.get();
         if (var9.equals("Kreuz") || var9.equals("Kreuz + Punkt") || var9.equals("T-Form")) {
            if (!var9.equals("T-Form")) {
               this.rect(var1, var2 - var7 / 2, var3 - var6 - var5, var7, var5, var4, var8);
            }

            this.rect(var1, var2 - var7 / 2, var3 + var6 + 1, var7, var5, var4, var8);
            this.rect(var1, var2 - var6 - var5, var3 - var7 / 2, var5, var7, var4, var8);
            this.rect(var1, var2 + var6 + 1, var3 - var7 / 2, var5, var7, var4, var8);
         }

         if (var9.equals("Punkt") || var9.equals("Kreuz + Punkt")) {
            this.rect(var1, var2 - var7 / 2, var3 - var7 / 2, Math.max(1, var7), Math.max(1, var7), var4, var8);
         }

         if (var9.equals("Kreis")) {
            int var10 = var5 + var6;

            for (int var11 = 0; var11 < 32; var11++) {
               double var12 = (Math.PI * 2) * var11 / 32.0;
               int var14 = var2 + (int)Math.round(Math.cos(var12) * var10);
               int var15 = var3 + (int)Math.round(Math.sin(var12) * var10);
               this.rect(var1, var14, var15, var7, var7, var4, 0);
            }

            this.rect(var1, var2, var3, 1, 1, var4, 0);
         }
      }

      private void rect(GuiGraphics var1, int var2, int var3, int var4, int var5, int var6, int var7) {
         if (this.outline.get() && var7 != 0) {
            var1.fill(var2 - 1, var3 - 1, var2 + var4 + 1, var3 + var5 + 1, var7);
         }

         var1.fill(var2, var3, var2 + var4, var3 + var5, var6);
      }
   }

   public static final class Crystals extends Module {
      final Setting.Color color = this.add(new Setting.Color("Farbe", 5));
      final Setting.Num speed = this.add(new Setting.Num("Drehgeschwindigkeit", 0.2, 4.0, 0.1, 1.0, "x"));
      final Setting.Num size = this.add(new Setting.Num("Ring-Größe", 0.5, 2.0, 0.1, 1.0, "x"));
      final Setting.Bool orbit = this.add(new Setting.Bool("Kreisende Würfel", true));
      final Setting.Bool glow = this.add(new Setting.Bool("Boden-Glow", true));
      final Setting.Bool beam = this.add(new Setting.Bool("Lichtstrahl", true));

      Crystals() {
         super("crystals", "Custom Kristalle", "Ringe, Würfel, Strahl und Glow um End-Kristalle", Category.VISUALS);
         this.icon("gem");
      }

      void render(float var1) {
         double var2 = System.currentTimeMillis() / 1000.0 * this.speed.get();

         for (Entity var5 : Mc.entities()) {
            if (Mc.isEndCrystal(var5)) {
               double[] var6 = Mc.lerpPos(var5, var1);
               double var7 = var6[0] - R3.camX();
               double var9 = var6[2] - R3.camZ();
               if (!(var7 * var7 + var9 * var9 > 4096.0)) {
                  double var11 = var6[0];
                  double var13 = var6[1] + 1.0;
                  double var15 = var6[2];
                  int var17 = this.color.argb(Mc.id(var5) * 0.13) & 16777215;
                  double var18 = this.size.get();

                  for (int var20 = 0; var20 < 2; var20++) {
                     double var21 = var2 * (var20 == 0 ? 1.6 : -1.2) + var20;
                     double var23 = 0.6 + var20 * 0.5;
                     double[] var25 = new double[]{Math.cos(var21), 0.0, Math.sin(var21)};
                     double[] var26 = new double[]{-Math.sin(var21) * Math.cos(var23), Math.sin(var23), Math.cos(var21) * Math.cos(var23)};
                     R3.ring3(var11, var13, var15, var25, var26, 0.95 * var18, 1.05 * var18, 40, R3.argb(var17, 0.8));
                  }

                  if (this.orbit.get()) {
                     for (int var27 = 0; var27 < 4; var27++) {
                        double var29 = var2 * 2.2 + var27 * Math.PI / 2.0;
                        R3.cube(
                           var11 + Math.cos(var29) * 1.3 * var18,
                           var13 + Math.sin(var2 * 3.0 + var27) * 0.25,
                           var15 + Math.sin(var29) * 1.3 * var18,
                           0.14 * var18,
                           R3.argb(var17, 0.9)
                        );
                     }
                  }

                  if (this.beam.get()) {
                     R3.line(var11, var13, var15, var11, var13 + 6.0, var15, 0.12 * var18, R3.argb(var17, 0.55), R3.argb(var17, 0.0));
                  }

                  if (this.glow.get()) {
                     double var28 = 0.5 + 0.5 * Math.sin(var2 * 2.0);
                     R3.ring(var11, var6[1] + 0.02, var15, 0.0, 1.4 * var18, 32, R3.argb(var17, 0.35 + 0.2 * var28), R3.argb(var17, 0.0), 0.0);
                  }
               }
            }
         }
      }
   }

   public static final class Fullbright extends Module {
      Fullbright() {
         super("fullbright", "Fullbright", "Alles hell (wie Nachtsicht, nur für dich)", Category.VISUALS);
         this.icon("sun");
      }

      @Override
      public void tick() {
         LocalPlayer var1 = Mc.player();
         Holder var2 = Mc.nightVision();
         if (var1 != null && var2 != null) {
            if (!var1.hasEffect(var2)) {
               var1.addEffect(new MobEffectInstance(var2, 1728000, 0, false, false, false));
            }
         }
      }

      @Override
      public void onDisable() {
         LocalPlayer var1 = Mc.player();
         Holder var2 = Mc.nightVision();
         if (var1 != null && var2 != null) {
            var1.removeEffect(var2);
         }
      }
   }

   public static final class HitParticles extends Module {
      final Setting.Mode type = this.add(new Setting.Mode("Partikel", 1, Visuals.PARTICLE_NAMES));
      final Setting.Num amount = this.add(new Setting.Num("Menge", 1.0, 30.0, 1.0, 8.0, ""));

      HitParticles() {
         super("hitparticles", "Hit-Partikel", "Extra Partikel wenn du triffst", Category.VISUALS);
         this.icon("hit");
      }

      public void onHit(Entity var1) {
         String var2 = Visuals.particle(this.type);
         double var3 = Mc.x(var1);
         double var5 = Mc.y(var1) + Mc.height(var1) * 0.6;
         double var7 = Mc.z(var1);

         for (int var9 = 0; var9 < this.amount.getI(); var9++) {
            Mc.spawn(
               var2,
               var3 + (Math.random() - 0.5) * 0.6,
               var5 + (Math.random() - 0.5) * 0.6,
               var7 + (Math.random() - 0.5) * 0.6,
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
         LocalPlayer var1 = Mc.player();
         if (var1 != null) {
            for (Player var4 : this.everyone.get() ? Mc.players() : List.of(var1)) {
               int var5 = Mc.id(var4);
               boolean var6 = Mc.onGround(var4);
               Boolean var7 = this.ground.put(var5, var6);
               if (var7 != null && var7 && !var6 && Mc.velocity(var4)[1] > 0.2) {
                  if (this.circles.size() > 24) {
                     this.circles.remove(0);
                  }

                  this.circles.add(new double[]{Mc.x(var4), Mc.y(var4) + 0.03, Mc.z(var4), System.currentTimeMillis()});
               }
            }

            if (this.ground.size() > 200) {
               this.ground.clear();
            }
         }
      }

      private void ringFlat(double[] var1, double var2, double var4, int var6, double var7, double var9) {
         byte var11 = 48;

         for (int var12 = 0; var12 < var11; var12++) {
            double var13 = var9 + (Math.PI * 2) * var12 / var11;
            double var15 = var9 + (Math.PI * 2) * (var12 + 1) / var11;
            int var17 = this.color.argb((double)var12 / var11) & 16777215;
            if (!this.color.rainbow()) {
               var17 = var6;
            }

            int var18 = R3.argb(var17, 0.0);
            int var19 = R3.argb(var17, var7);
            double var20 = Math.cos(var13);
            double var22 = Math.sin(var13);
            double var24 = Math.cos(var15);
            double var26 = Math.sin(var15);
            R3.quad(
               var1[0] + var20 * (var2 - var4 * 3.0),
               var1[1],
               var1[2] + var22 * (var2 - var4 * 3.0),
               var1[0] + var20 * var2,
               var1[1],
               var1[2] + var22 * var2,
               var1[0] + var24 * var2,
               var1[1],
               var1[2] + var26 * var2,
               var1[0] + var24 * (var2 - var4 * 3.0),
               var1[1],
               var1[2] + var26 * (var2 - var4 * 3.0),
               var18,
               var19,
               var19,
               var18
            );
            R3.quad(
               var1[0] + var20 * var2,
               var1[1],
               var1[2] + var22 * var2,
               var1[0] + var20 * (var2 + var4),
               var1[1],
               var1[2] + var22 * (var2 + var4),
               var1[0] + var24 * (var2 + var4),
               var1[1],
               var1[2] + var26 * (var2 + var4),
               var1[0] + var24 * var2,
               var1[1],
               var1[2] + var26 * var2,
               var19,
               R3.argb(var17, 0.0),
               R3.argb(var17, 0.0),
               var19
            );
         }
      }

      private static double ease(double var0) {
         return 1.0 - Math.pow(1.0 - Math.max(0.0, Math.min(1.0, var0)), 3.0);
      }

      void render(float var1) {
         long var2 = System.currentTimeMillis();
         double var4 = this.size.get();
         Iterator var6 = this.circles.iterator();

         label156:
         while (var6.hasNext()) {
            double[] var7 = (double[])var6.next();
            double var8 = (var2 - var7[3]) / 1000.0;
            if (var8 >= 1.2) {
               var6.remove();
            } else {
               double var10 = Math.min(1.0, var8);
               double var12 = 1.0 - var10;
               int var14 = this.color.argb(var7[3] % 1000.0 / 1000.0) & 16777215;
               double var15 = 0.09 * var4;
               Random var17 = new Random((long)var7[3]);
               int var18;
               switch (this.style.index) {
                  case 0:
                     this.ringFlat(var7, var4 * (0.25 + 0.75 * ease(var10)), var15, var14, 0.85 * var12, 0.0);
                     continue;
                  case 1:
                     var18 = 0;

                     while (true) {
                        if (var18 >= 2) {
                           continue label156;
                        }

                        double var54 = Math.max(0.0, Math.min(1.0, var10 * (1.0 + var18 * 0.25) - var18 * 0.12));
                        if (var54 > 0.0) {
                           this.ringFlat(var7, var4 * (0.25 + 0.75 * ease(var54)) * (1.0 - var18 * 0.18), var15, var14, 0.85 * (1.0 - var54), 0.0);
                        }

                        var18++;
                     }
                  case 2:
                     var18 = 0;

                     while (true) {
                        if (var18 >= 3) {
                           continue label156;
                        }

                        double var53 = Math.max(0.0, Math.min(1.0, var10 * 1.3 - var18 * 0.18));
                        if (var53 > 0.0) {
                           this.ringFlat(var7, var4 * (0.2 + 0.9 * ease(var53)), var15 * 0.9, var14, 0.8 * (1.0 - var53), 0.0);
                        }

                        var18++;
                     }
                  case 3:
                     double var43 = ease(var10 * 1.4);
                     this.ringFlat(var7, var4 * (0.15 + 1.2 * var43), var15 * 1.6, var14, 0.95 * var12 * var12, 0.0);
                     double var56 = var4 * 0.5 * (1.0 - var10);
                     int var63 = R3.argb(16777215, 0.8 * var12);
                     R3.quad(
                        var7[0] - var56,
                        var7[1] + 0.01,
                        var7[2] - var56,
                        var7[0] + var56,
                        var7[1] + 0.01,
                        var7[2] - var56,
                        var7[0] + var56,
                        var7[1] + 0.01,
                        var7[2] + var56,
                        var7[0] - var56,
                        var7[1] + 0.01,
                        var7[2] + var56,
                        var63
                     );
                     continue;
                  case 4:
                     var18 = 0;

                     while (true) {
                        if (var18 >= 2) {
                           continue label156;
                        }

                        for (int var52 = 0; var52 < 40; var52++) {
                           double var55 = var52 / 39.0;
                           double var62 = var18 * Math.PI + var55 * 5.0 + var8 * 6.0;
                           double var69 = var4 * (0.15 + 1.0 * var55) * ease(var10 * 1.2);
                           double var72 = Math.max(0.0, 1.0 - var10) * (1.0 - var55 * 0.6);
                           R3.cube(var7[0] + Math.cos(var62) * var69, var7[1] + 0.04, var7[2] + Math.sin(var62) * var69, 0.1 * var4, R3.argb(var14, var72));
                        }

                        var18++;
                     }
                  case 5:
                     var18 = 0;

                     while (true) {
                        if (var18 >= 28) {
                           continue label156;
                        }

                        double var51 = var17.nextDouble() * Math.PI * 2.0;
                        double var61 = (0.5 + var17.nextDouble()) * var4;
                        double var67 = 1.2 + var17.nextDouble() * 1.4;
                        double var70 = var7[0] + Math.cos(var51) * var61 * var10;
                        double var73 = var7[2] + Math.sin(var51) * var61 * var10;
                        double var74 = var7[1] + var67 * var8 - 3.2 * var8 * var8;
                        if (!(var74 < var7[1])) {
                           R3.cube(var70, var74, var73, 0.07 * var4 + 0.03, R3.argb(var14, var12));
                        }

                        var18++;
                     }
                  case 6:
                     double var40 = var4 * 0.55;
                     double var20 = 0.4 + 2.2 * ease(var10);
                     byte var22 = 24;

                     for (int var66 = 0; var66 < var22; var66++) {
                        double var68 = (Math.PI * 2) * var66 / var22;
                        double var71 = (Math.PI * 2) * (var66 + 1) / var22;
                        R3.quad(
                           var7[0] + Math.cos(var68) * var40,
                           var7[1],
                           var7[2] + Math.sin(var68) * var40,
                           var7[0] + Math.cos(var71) * var40,
                           var7[1],
                           var7[2] + Math.sin(var71) * var40,
                           var7[0] + Math.cos(var71) * var40 * 0.7,
                           var7[1] + var20,
                           var7[2] + Math.sin(var71) * var40 * 0.7,
                           var7[0] + Math.cos(var68) * var40 * 0.7,
                           var7[1] + var20,
                           var7[2] + Math.sin(var68) * var40 * 0.7,
                           R3.argb(var14, 0.45 * var12),
                           R3.argb(var14, 0.45 * var12),
                           R3.argb(var14, 0.0),
                           R3.argb(var14, 0.0)
                        );
                     }

                     this.ringFlat(var7, var40, var15, var14, 0.8 * var12, 0.0);
                     continue;
                  case 7:
                     var18 = 0;

                     while (true) {
                        if (var18 >= 4) {
                           continue label156;
                        }

                        double var50 = Math.max(0.0, Math.min(1.0, var10 - var18 * 0.09));
                        if (var50 > 0.0) {
                           this.ringFlat(var7, var4 * (0.2 + 0.8 * ease(var50)), var15 * 0.6, var14, 0.9 * (1.0 - var50), 0.0);
                        }

                        var18++;
                     }
                  case 8:
                     byte var38 = 8;
                     double var49 = var4 * 1.1 * ease(var10 * 1.3);
                     double var60 = var8 * 3.0;

                     for (int var65 = 0; var65 < var38; var65++) {
                        double var24 = var60 + (Math.PI * 2) * var65 / var38;
                        R3.line(
                           var7[0],
                           var7[1] + 0.05,
                           var7[2],
                           var7[0] + Math.cos(var24) * var49,
                           var7[1] + 0.05,
                           var7[2] + Math.sin(var24) * var49,
                           0.07 * var4,
                           R3.argb(var14, 0.9 * var12),
                           R3.argb(var14, 0.0)
                        );
                     }

                     this.ringFlat(var7, var49 * 0.35, var15, var14, 0.7 * var12, var60);
                     continue;
                  case 9:
                     var18 = 0;

                     while (true) {
                        if (var18 >= 6) {
                           continue label156;
                        }

                        double var48 = var17.nextDouble() * Math.PI * 2.0;
                        double var59 = var7[0];
                        double var64 = var7[2];
                        byte var25 = 5;

                        for (int var26 = 1; var26 <= var25; var26++) {
                           double var27 = var4 * 1.2 * ease(var10 * 1.5) * var26 / var25;
                           double var29 = (var17.nextDouble() - 0.5) * 0.5;
                           double var31 = var7[0] + Math.cos(var48 + var29) * var27;
                           double var33 = var7[2] + Math.sin(var48 + var29) * var27;
                           R3.line(
                              var59,
                              var7[1] + 0.06,
                              var64,
                              var31,
                              var7[1] + 0.06,
                              var33,
                              0.05 * var4,
                              R3.argb(16777215, 0.9 * var12),
                              R3.argb(var14, 0.9 * var12)
                           );
                           var59 = var31;
                           var64 = var33;
                        }

                        var18++;
                     }
                  case 10:
                     var18 = 0;

                     while (true) {
                        if (var18 >= 4) {
                           continue label156;
                        }

                        double var47 = Math.max(0.0, Math.min(1.0, var10 * 1.2 - var18 * 0.12));
                        if (!(var47 <= 0.0)) {
                           double[] var58 = new double[]{var7[0], var7[1] + var47 * 1.8, var7[2]};
                           this.ringFlat(var58, var4 * 0.7 * (1.0 - 0.3 * var47), var15 * 0.8, var14, 0.85 * (1.0 - var47), var8 * 2.0 + var18);
                        }

                        var18++;
                     }
                  default:
                     var18 = 0;
               }

               for (; var18 < 4; var18++) {
                  double var19 = Math.max(0.0, Math.min(1.0, var10 * 1.2 - var18 * 0.08));
                  if (!(var19 <= 0.0)) {
                     int var21 = Color.HSBtoRGB((float)((var18 * 0.17 + var8) % 1.0), 0.75F, 1.0F) & 16777215;
                     this.ringFlat(var7, var4 * (0.2 + 1.0 * ease(var19)), var15, var21, 0.85 * (1.0 - var19), 0.0);
                  }
               }

               for (int var35 = 0; var35 < 16; var35++) {
                  double var46 = var17.nextDouble() * Math.PI * 2.0;
                  double var57 = (0.4 + var17.nextDouble() * 0.9) * var4;
                  int var23 = Color.HSBtoRGB((float)var17.nextDouble(), 0.7F, 1.0F) & 16777215;
                  R3.cube(
                     var7[0] + Math.cos(var46) * var57 * var10,
                     var7[1] + 0.8 * var8 - 1.6 * var8 * var8 + 0.1,
                     var7[2] + Math.sin(var46) * var57 * var10,
                     0.08 * var4 + 0.03,
                     R3.argb(var23, var12)
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

      public void onDeath(Entity var1) {
         if (var1 == HudModules.CombatTracker.lastTarget()) {
            if (System.currentTimeMillis() - HudModules.CombatTracker.lastAttackAt() <= 5000L) {
               String var2 = Visuals.particle(this.type);

               for (int var3 = 0; var3 < 70; var3++) {
                  double var4 = Math.random() * Math.PI * 2.0;
                  double var6 = 0.1 + Math.random() * 0.35;
                  Mc.spawn(
                     var2,
                     Mc.x(var1),
                     Mc.y(var1) + 0.2 + Math.random() * Mc.height(var1),
                     Mc.z(var1),
                     Math.cos(var4) * var6,
                     Math.random() * 0.4,
                     Math.sin(var4) * var6
                  );
               }

               if (this.lightning.get()) {
                  this.beams.add(new double[]{Mc.x(var1), Mc.y(var1), Mc.z(var1), System.currentTimeMillis()});
               }

               Utility.onKill(var1);
            }
         }
      }

      void render() {
         long var1 = System.currentTimeMillis();
         Iterator var3 = this.beams.iterator();

         while (var3.hasNext()) {
            double[] var4 = (double[])var3.next();
            double var5 = (var1 - var4[3]) / 900.0;
            if (var5 >= 1.0) {
               var3.remove();
            } else {
               double var7 = 1.0 - var5;
               int var9 = Modules.accentArgb() & 16777215;
               R3.line(var4[0], var4[1], var4[2], var4[0], var4[1] + 40.0, var4[2], 0.5 * var7 + 0.1, R3.argb(16777215, 0.9 * var7), R3.argb(var9, 0.0));
               R3.line(var4[0], var4[1], var4[2], var4[0], var4[1] + 25.0, var4[2], 1.4 * var7, R3.argb(var9, 0.35 * var7), R3.argb(var9, 0.0));
               R3.ring(var4[0], var4[1] + 0.05, var4[2], var5 * 3.0, var5 * 3.0 + 0.25, 40, R3.argb(var9, 0.7 * var7), R3.argb(var9, 0.0), 0.0);
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
      final Setting.Bool particles = this.add(new Setting.Bool("Extra Partikel", true));
      final Setting.Bool message = this.add(new Setting.Bool("Meldung mit Zähler", true));
      private final List<double[]> pops = new ArrayList<>();
      private final Map<String, Integer> counts = new HashMap<>();

      TotemEffect() {
         super("totem", "Custom Totem", "Ringe, Partikel und Pop-Zähler bei Totems", Category.VISUALS);
         this.icon("totem");
      }

      public void onPop(Entity var1) {
         double[] var2 = new double[]{Mc.x(var1), Mc.y(var1), Mc.z(var1), System.currentTimeMillis(), Mc.height(var1)};
         this.pops.add(var2);
         if (this.particles.get()) {
            for (int var3 = 0; var3 < 40; var3++) {
               double var4 = Math.random() * Math.PI * 2.0;
               double var6 = 0.2 + Math.random() * 0.4;
               Mc.spawn(
                  var3 % 3 == 0 ? "end_rod" : "totem_of_undying",
                  var2[0],
                  var2[1] + 1.0,
                  var2[2],
                  Math.cos(var4) * var6,
                  0.2 + Math.random() * 0.5,
                  Math.sin(var4) * var6
               );
            }
         }

         if (this.message.get() && Mc.isPlayer(var1)) {
            String var8 = Mc.name(var1);
            int var9 = this.counts.merge(var8, 1, Integer::sum);
            Toasts.show("totem", var8 + " hat gepoppt", var9 + " Totem" + (var9 == 1 ? "" : "s") + " bisher", -13053, 3000L);
         }
      }

      public void onDeath(Entity var1) {
         if (Mc.isPlayer(var1)) {
            this.counts.remove(Mc.name(var1));
         }
      }

      void render() {
         long var1 = System.currentTimeMillis();
         Iterator var3 = this.pops.iterator();

         while (var3.hasNext()) {
            double[] var4 = (double[])var3.next();
            double var5 = (var1 - var4[3]) / 1300.0;
            if (var5 >= 1.0) {
               var3.remove();
            } else {
               double var7 = 1.0 - Math.pow(1.0 - var5, 3.0);

               for (int var9 = 0; var9 < 3; var9++) {
                  double var10 = var4[1] + var4[4] * (0.2 + 0.35 * var9) + var7 * 0.6;
                  double var12 = 0.4 + var7 * (1.2 + var9 * 0.3);
                  int var14 = this.color.argb(var9 * 0.2) & 16777215;
                  R3.ring(
                     var4[0],
                     var10,
                     var4[2],
                     var12 - 0.06,
                     var12,
                     40,
                     R3.argb(var14, 0.9 * (1.0 - var5)),
                     R3.argb(var14, 0.3 * (1.0 - var5)),
                     var5 * 3.0 + var9
                  );
               }
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
         LocalPlayer var1 = Mc.player();
         if (var1 != null) {
            double var2 = Mc.x(var1) - Mc.lastX(var1);
            double var4 = Mc.z(var1) - Mc.lastZ(var1);
            if (!this.onlyMoving.get() || !(var2 * var2 + var4 * var4 < 4.0E-4)) {
               Mc.spawn(
                  Visuals.particle(this.type),
                  Mc.x(var1) + (Math.random() - 0.5) * 0.3,
                  Mc.y(var1) + 0.1,
                  Mc.z(var1) + (Math.random() - 0.5) * 0.3,
                  0.0,
                  0.01,
                  0.0
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

      public float apply(float var1) {
         double var2 = this.active() ? Math.max(1.1, this.factor.get() + this.extra) : 1.0;
         this.current = this.smooth.get() ? this.current + (var2 - this.current) * 0.25 : var2;
         if (Math.abs(this.current - var2) < 0.01) {
            this.current = var2;
         }

         return (float)(var1 / this.current);
      }

      public boolean onScroll(double var1) {
         if (this.active() && this.scroll.get()) {
            this.extra = Math.max(
               1.0 - this.factor.get(), Math.min(30.0, this.extra + (var1 > 0.0 ? 1 : -1) * Math.max(0.5, (this.factor.get() + this.extra) * 0.15))
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
            Options var1 = Mc.options();
            if (this.active() && !var1.smoothCamera) {
               this.savedSmooth = false;
               var1.smoothCamera = true;
            } else if (!this.active() && var1.smoothCamera && !this.savedSmooth) {
               var1.smoothCamera = false;
               this.savedSmooth = true;
            }
         }
      }
   }
}
