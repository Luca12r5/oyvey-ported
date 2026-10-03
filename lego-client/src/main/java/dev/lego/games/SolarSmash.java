package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.Sound;
import dev.lego.ui.View;
import dev.lego.ui.hub.HubView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SolarSmash extends View {
   private final List<SolarPlanet> bodies = new ArrayList<>();
   private SolarPlanet focus;
   private SolarFx fx;
   private float zoom;
   private float zoomTarget;
   private int cat = 0;
   private int tool = 0;
   private boolean paused;
   private boolean slow;
   private boolean hideUi;
   private boolean drawerOpen = true;
   private float sim;
   private boolean mouseHeld;
   private long destroyedCount;
   private final SolarPlanet.Disc big = new SolarPlanet.Disc("big");
   private final Map<SolarPlanet, SolarPlanet.Disc> small = new HashMap<>();
   private float fsx;
   private float fsy;
   private float fsr;
   private int uiTop;
   private int uiBottom;

   public SolarSmash() {
      this.add(new SolarPlanet(0, "Helios", "Stern", 0L, 5500.0F, -20416, 1.0F, 2.2F, 0.1F, 1L, false), 0.0F, 0.0F);
      this.add(new SolarPlanet(5, "Vulkanis", "Lavaplanet", 40000000L, 420.0F, -38358, 0.6F, 0.55F, 0.2F, 7L, false), 3.4F, 0.9F);
      this.add(new SolarPlanet(7, "Dunaria", "Wüstenwelt", 900000000L, 48.0F, -14192, 0.7F, 0.7F, 0.3F, 13L, false), 4.6F, 2.4F);
      this.add(new SolarPlanet(1, "Terra", "Erdähnlich", 8100000000L, 15.0F, -9784065, 1.0F, 0.85F, 0.41F, 3L, false), 5.9F, 4.1F);
      this.add(new SolarPlanet(2, "Karos", "Rostwelt", 2000000L, -60.0F, -26006, 0.35F, 0.62F, 0.44F, 21L, false), 7.1F, 5.6F);
      this.add(new SolarPlanet(6, "Aquaris", "Wasserwelt", 2400000000L, 22.0F, -11874049, 1.1F, 0.9F, 0.2F, 31L, false), 8.5F, 1.4F);
      SolarPlanet var1 = new SolarPlanet(3, "Jovara", "Gasriese", 0L, -140.0F, -10072, 1.2F, 1.45F, 0.35F, 41L, false);
      var1.ringed = true;
      this.add(var1, 10.3F, 3.3F);
      this.add(new SolarPlanet(4, "Glacia", "Eisplanet", 350000000L, -95.0F, -4200193, 0.8F, 0.75F, 0.5F, 51L, false), 12.0F, 5.0F);
      this.add(new SolarPlanet(8, "Selene", "Mond", 0L, -20.0F, -2566964, 0.0F, 0.4F, 0.1F, 61L, true), 13.3F, 0.3F);

      for (SolarPlanet var3 : this.bodies) {
         var3.request(false);
      }
   }

   private void add(SolarPlanet var1, float var2, float var3) {
      var1.orbitR = var2;
      var1.orbitA = var3;
      var1.orbitW = var2 <= 0.0F ? 0.0F : (float)(0.25 / Math.pow(var2, 1.2));
      this.bodies.add(var1);
   }

   @Override
   public boolean blur() {
      return false;
   }

   @Override
   public float designW() {
      return 900.0F;
   }

   @Override
   public float designH() {
      return 520.0F;
   }

   @Override
   public float openMs() {
      return 380.0F;
   }

   private List<SolarTools.Tool> tools() {
      return SolarTools.of(this.cat);
   }

   private SolarTools.Tool current() {
      List var1 = this.tools();
      return (SolarTools.Tool)var1.get(Math.max(0, Math.min(var1.size() - 1, this.tool)));
   }

   private void focus(SolarPlanet var1, float var2, float var3, float var4) {
      if (this.focus != var1) {
         if (this.focus != null) {
            this.focus.release();
         }

         this.focus = var1;
         this.fx = new SolarFx(var1);
      }

      var1.request(true);
      this.fsx = var2;
      this.fsy = var3;
      this.fsr = var4;
      this.zoomTarget = 1.0F;
      Sound.play("minecraft:block.beacon.activate", 1.6F, 0.4F);
   }

   @Override
   public void draw() {
      float var1 = Math.min(0.05F, Gx.dt) * (this.paused ? 0.0F : (this.slow ? 0.25F : 1.0F));
      this.sim += var1;
      this.zoom = this.zoom + (this.zoomTarget - this.zoom) * Math.min(1.0F, Gx.dt * 4.5F);
      if (this.zoomTarget == 0.0F && this.zoom < 0.01F) {
         this.zoom = 0.0F;
      }

      for (SolarPlanet var3 : this.bodies) {
         var3.orbitA = var3.orbitA + var3.orbitW * var1 * (this.zoom > 0.5F ? 0.1F : 1.0F);
         var3.update(var1, this.sim);
      }

      if (this.fx != null && this.zoom > 0.3F) {
         this.fx.update(var1);
      }

      if (this.focus != null && this.focus.destroyed && this.focus.destroyedT > 0.1F && this.focus.destroyedT < 0.2F) {
         this.destroyedCount++;
      }

      this.background();
      float var4 = 1.0F - Ease.inOutCubic(Math.min(1.0F, this.zoom * 1.6F));
      if (var4 > 0.01F) {
         Gx.pushAlpha(var4);
         this.drawSystem();
         Gx.popAlpha();
      }

      if (this.focus != null && this.zoom > 0.02F) {
         this.drawPlanet();
      }

      if (this.fx != null && this.fx.tintA > 0.01F) {
         Gx.fill(0, 0, this.W, this.H, Gx.withAlpha(this.fx.tint, this.fx.tintA * 0.35F));
      }

      if (this.fx != null && this.fx.flash > 0.01F) {
         Gx.fill(0, 0, this.W, this.H, Gx.withAlpha(-2848, this.fx.flash * 0.8F));
      }

      if (!this.hideUi) {
         this.hud();
      } else {
         Gx.textRight("H = Oberfläche einblenden", this.W - this.p(16.0), this.H - this.p(14.0), this.pf(7.0), 2, 1728053247);
      }
   }

   private void background() {
      Gx.fill(0, 0, this.W, this.H, -16579574);
      Gx.Img var1 = Gx.get("solar:neb", 0, () -> SolarTex.nebula(1280, 720, 5L));
      float var2 = this.zoom * 0.03F;
      if (var1 != null) {
         Gx.image(
            var1,
            Math.round(-this.W * (0.02F + var2)),
            Math.round(-this.H * (0.02F + var2)),
            Math.round(this.W * (1.04F + var2 * 2.0F)),
            Math.round(this.H * (1.04F + var2 * 2.0F)),
            -1
         );
      }

      Gx.Img var3 = Gx.get("solar:stars1", 0, () -> SolarTex.stars(1024, 900, 0.8F, 9L));
      Gx.Img var4 = Gx.get("solar:stars2", 0, () -> SolarTex.stars(512, 160, 1.0F, 19L));
      if (var3 != null) {
         for (short var5 = 0; var5 < this.W; var5 += 1024) {
            for (short var6 = 0; var6 < this.H; var6 += 1024) {
               Gx.image(var3, var5, var6, 1024, 1024, -1);
            }
         }
      }

      float var8 = 0.75F + 0.25F * (float)Math.sin(this.sim * 1.3);
      if (var4 != null) {
         for (int var9 = -((int)(this.zoom * 60.0F) % 1024); var9 < this.W; var9 += 1024) {
            for (short var7 = 0; var7 < this.H; var7 += 1024) {
               Gx.image(var4, var9, var7, 1024, 1024, Gx.withAlpha(-1, var8));
            }
         }
      }
   }

   private void drawSystem() {
      float var1 = this.W * 0.5F;
      float var2 = this.H * 0.52F;
      float var3 = Math.min(this.W, this.H) / 30.0F;
      SolarPlanet var4 = null;
      float var5 = 0.0F;
      float var6 = 0.0F;
      float var7 = 0.0F;
      int var8 = 120000;
      Gx.Img var9 = Gx.get("solar:orbit", 0, () -> SolarTex.orbitRing(1024, 1.2F));

      for (SolarPlanet var11 : this.bodies) {
         if (!(var11.orbitR <= 0.0F) && var9 != null) {
            float var12 = var11.orbitR * var3 * 1.25F;
            float var13 = var12 * 0.42F;
            Gx.image(var9, Math.round(var1 - var12), Math.round(var2 - var13), Math.round(var12 * 2.0F), Math.round(var13 * 2.0F), 1090519039);
         }
      }

      ArrayList<SolarPlanet> var27 = new ArrayList<>(this.bodies);
      var27.sort((var0, var1x) -> Float.compare((float)Math.sin(var0.orbitA) * var0.orbitR, (float)Math.sin(var1x.orbitA) * var1x.orbitR));

      for (SolarPlanet var29 : var27) {
         float var30 = var29.orbitR * var3 * 1.25F;
         float var14 = var30 * 0.42F;
         float var15 = var1 + (float)Math.cos(var29.orbitA) * var30;
         float var16 = var2 + (float)Math.sin(var29.orbitA) * var14;
         float var17 = var29.size * var3 * (var29.type == 0 ? 1.4F : 0.9F) * (0.85F + 0.15F * (float)Math.sin(var29.orbitA));
         if (var29.type == 0) {
            this.sunGlow(var15, var16, var17);
         }

         SolarPlanet.Disc var18 = this.small.computeIfAbsent(var29, var0 -> new SolarPlanet.Disc("s" + var0.name));
         int var19 = Math.max(16, Math.round(var17 * 2.0F));
         var18.setup(var19, var29.tilt, var29.type == 0);
         float var20 = var1 - var15;
         float var21 = var2 - var16;
         float var22 = (float)Math.hypot(var20, var21) + 0.001F;
         if (var29.type == 0) {
            var18.light(0.0F, 0.0F, 1.0F);
         } else {
            var18.light(var20 / var22 * 0.95F, var21 / var22 * 0.95F, 0.3F);
         }

         if (var29.refresh(var18, false, var8, false)) {
            var8 += 0;
         } else {
            var8 = 0;
         }

         if (var29.destroyed) {
            this.debrisRing(var15, var16, var17);
         } else {
            if (var29.atmo > 0.05F) {
               this.halo(var15, var16, var17, var29.atmoCol, 0.35F * var29.atmo);
            }

            SolarPlanet.drawDisc(var18, var15, var16, var17, -1);
         }

         if (this.hover(
            Math.round(var15 - var17 - this.p(6.0)),
            Math.round(var16 - var17 - this.p(6.0)),
            Math.round(var17 * 2.0F + this.p(12.0)),
            Math.round(var17 * 2.0F + this.p(12.0))
         )) {
            var4 = var29;
            var5 = var15;
            var6 = var16;
            var7 = var17;
         }

         Kit.hit(
               Math.round(var15 - var17 - this.p(6.0)),
               Math.round(var16 - var17 - this.p(6.0)),
               Math.round(var17 * 2.0F + this.p(12.0)),
               Math.round(var17 * 2.0F + this.p(12.0))
            )
            .click(() -> this.focus(var29, var15, var16, var17));
      }

      if (var4 != null) {
         Gx.ringAt(Math.round(var5), Math.round(var6), Math.round(var7 + this.p(5.0)), Math.max(1, this.p(1.0)), -1426063361);
         this.label(
            var4.name.toUpperCase(Locale.ROOT), var4.kind + (var4.pop > 0L ? " · " + big(var4.alive()) + " Bewohner" : ""), var5, var6 - var7 - this.p(14.0)
         );
      }

      Gx.textSpaced("SOLAR SMASH", this.W / 2.0F, this.p(34.0), this.pf(22.0), 3, -1, this.pf(6.0));
      Gx.textSpaced("KLICK AUF EINEN PLANETEN", this.W / 2.0F, this.p(58.0), this.pf(7.4F), 2, -1711276033, this.pf(2.4F));
   }

   private void label(String var1, String var2, float var3, float var4) {
      float var5 = Math.max(Gx.spacedWidth(var1, this.pf(9.0), 3, this.pf(2.0)), Gx.width(var2, this.pf(7.0), 2)) + this.p(24.0);
      int var6 = Math.round(var3 - var5 / 2.0F);
      int var7 = Math.round(var4 - this.p(34.0));
      Gx.rect(var6, var7, Math.round(var5), this.p(32.0), this.p(9.0), -871756520);
      Gx.outline(var6, var7, Math.round(var5), this.p(32.0), this.p(9.0), Math.max(1, this.p(0.6)), 872415231);
      Gx.textSpaced(var1, var3, var7 + this.p(11.0), this.pf(9.0), 3, -1, this.pf(2.0));
      Gx.textCenter(var2, var3, var7 + this.p(23.0), this.pf(7.0), 2, -1426063361);
   }

   private void sunGlow(float var1, float var2, float var3) {
      Gx.Img var4 = Gx.get("solar:corona", 0, () -> SolarTex.corona(512, 3L));
      float var5 = 1.0F + 0.03F * (float)Math.sin(this.sim * 2.0F);
      if (var4 != null) {
         int var6 = Math.round(var3 * 5.2F * var5);
         Gx.image(var4, Math.round(var1 - var6 / 2.0F), Math.round(var2 - var6 / 2.0F), var6, var6, -1);
      }

      Gx.glow(Math.round(var1), Math.round(var2), Math.round(var3 * 3.2F), 1442820160);
   }

   private void halo(float var1, float var2, float var3, int var4, float var5) {
      Gx.Img var6 = Gx.get("solar:halo", 0, () -> SolarTex.halo(256));
      if (var6 != null) {
         int var7 = Math.round(var3 * 2.5F);
         Gx.image(var6, Math.round(var1 - var7 / 2.0F), Math.round(var2 - var7 / 2.0F), var7, var7, Gx.withAlpha(var4, Math.min(1.0F, var5)));
      }
   }

   private void debrisRing(float var1, float var2, float var3) {
      Gx.Img var4 = Gx.get("solar:fx:rock1", 0, () -> SolarTex.rock(64, 12L, -1));
      if (var4 != null) {
         for (int var5 = 0; var5 < 18; var5++) {
            double var6 = var5 * 0.7 + this.sim * 0.2;
            double var8 = var3 * (0.6 + var5 % 5 * 0.2);
            int var10 = Math.max(2, Math.round(var3 * (0.12F + var5 % 3 * 0.06F)));
            Gx.image(
               var4,
               (int)Math.round(var1 + Math.cos(var6) * var8 - var10 / 2.0),
               (int)Math.round(var2 + Math.sin(var6) * var8 * 0.6 - var10 / 2.0),
               var10,
               var10,
               -7701910
            );
         }
      }
   }

   private float pcx() {
      return this.W * 0.46F;
   }

   private float pcy() {
      return this.H * 0.46F;
   }

   private float pR() {
      return Math.min(this.W * 0.23F, this.H * 0.3F);
   }

   private void drawPlanet() {
      SolarPlanet var1 = this.focus;
      float var2 = Ease.inOutCubic(this.zoom);
      float var3 = var1.shake * this.p(6.0);
      float var4 = this.fsx + (this.pcx() - this.fsx) * var2 + (this.fx.rnd.nextFloat() - 0.5F) * var3;
      float var5 = this.fsy + (this.pcy() - this.fsy) * var2 + (this.fx.rnd.nextFloat() - 0.5F) * var3;
      float var6 = this.fsr + (this.pR() - this.fsr) * var2;
      Gx.pushAlpha(Math.min(1.0F, this.zoom * 2.0F));
      if (var1.type != 0) {
         this.sunGlow(-this.W * 0.08F, -this.H * 0.12F, Math.min(this.W, this.H) * 0.12F);
      }

      this.fx.drawBack(var4, var5, var6);
      float var7 = var6 * var1.scale;
      if (!var1.destroyed) {
         if (var1.atmo > 0.02F) {
            this.halo(var4, var5, var7, var1.atmoCol, 0.55F * var1.atmo / Math.max(0.3F, var1.atmo0));
         }

         if (var1.type == 0) {
            this.sunGlow(var4, var5, var7);
         }

         Gx.Img[] var8 = null;
         if (var1.ringed && var1.ringAlive() > 0.02F) {
            int var9 = Math.max(8, Math.round(var7 * 2.35F));
            int var10 = Math.max(8, Math.round(var9 * 0.62F));
            var8 = var1.ringImgs(var9, var10, var7 / 2.0F, -0.55F, -0.45F, 0.7F);
            Gx.image(var8[0], Math.round(var4 - var9), Math.round(var5 - var10), var9 * 2, var10 * 2, -1);
         }

         int var12 = Math.max(32, Math.min(1400, Math.round(var7 * 2.0F)));
         this.big.setup(var12, var1.tilt, var1.type == 0);
         if (var1.type == 0) {
            this.big.light(0.0F, 0.0F, 1.0F);
         } else {
            this.big.light(-0.62F, -0.42F, 0.66F);
         }

         var1.refresh(this.big, true, 170000, false);
         int var14 = var1.hitFlash > 0.01F ? Gx.mix(-1, -12112, var1.hitFlash) : -1;
         SolarPlanet.drawDisc(this.big, var4, var5, var7, var14);
         if (var8 != null) {
            Gx.image(var8[1], Math.round(var4 - var8[1].w), Math.round(var5 - var8[1].h), var8[1].w * 2, var8[1].h * 2, -1);
         }

         if (var1.loading() && this.zoom > 0.9F) {
            Gx.textCenter("Oberfläche wird in 4K erzeugt …", var4, var5 + var7 + this.p(18.0), this.pf(8.0), 2, -1426063361);
         }
      }

      this.fx.drawFront(var4, var5, var6);
      if (this.zoom > 0.95F && !this.overUi()) {
         SolarTools.Tool var11 = this.current();
         float var13 = Math.max((float)this.p(6.0), var6 * Math.min(0.5F, var11.r / 1024.0F) * this.fx.sizeMul);
         Gx.ringAt(Math.round(this.mx), Math.round(this.my), Math.round(var13), Math.max(1, this.p(0.8)), Gx.withAlpha(var11.color, 0.8F));
         Gx.fill(Math.round(this.mx) - this.p(4.0), Math.round(this.my), this.p(8.0), Math.max(1, this.p(0.8)), -855638017);
         Gx.fill(Math.round(this.mx), Math.round(this.my) - this.p(4.0), Math.max(1, this.p(0.8)), this.p(8.0), -855638017);
      }

      Gx.popAlpha();
      if (this.mouseHeld && this.fx.beamOn) {
         this.fx.beamX = (this.mx - var4) / var6;
         this.fx.beamY = (this.my - var5) / var6;
      }
   }

   private boolean overUi() {
      return this.my > this.uiBottom || this.my < this.uiTop || this.zoom > 0.5F && this.mx > this.W - this.p(250.0);
   }

   private void hud() {
      this.uiTop = this.p(64.0);
      this.uiBottom = this.H - (this.zoom > 0.5F ? this.p(this.drawerOpen ? 132.0 : 44.0) : this.p(40.0));
      Gx.gradientV(0, 0, this.W, this.p(70.0), -872415232, 0);
      int var1 = this.p(16.0);
      int var2 = this.p(14.0);
      this.button("back", this.zoom > 0.5F ? "System" : "Zurück", var1, var2, () -> {
         if (this.zoomTarget > 0.5F) {
            this.zoomTarget = 0.0F;
            this.fx.release();
         } else {
            this.host.open(new HubView(HubView.Section.GAMES));
         }
      });
      if (this.zoom > 0.5F && this.focus != null) {
         this.button("reset", "Reparieren", var1 + this.p(96.0), var2, () -> {
            this.focus.repair();
            this.fx.clear();
            Sound.play("minecraft:block.beacon.activate", 1.2F, 0.5F);
         });
         this.button(this.slow ? "check" : "clock", this.slow ? "Zeitlupe an" : "Zeitlupe", var1 + this.p(206.0), var2, () -> this.slow = !this.slow);
         this.button(this.paused ? "play" : "pause", this.paused ? "Weiter" : "Pause", var1 + this.p(316.0), var2, () -> this.paused = !this.paused);
         this.button("eye", "UI aus (H)", var1 + this.p(416.0), var2, () -> this.hideUi = true);
         Gx.textSpaced(this.focus.name.toUpperCase(Locale.ROOT), this.W / 2.0F + this.p(40.0), this.p(30.0), this.pf(16.0), 3, -1, this.pf(5.0));
         Gx.textSpaced(this.focus.kind.toUpperCase(Locale.ROOT), this.W / 2.0F + this.p(40.0), this.p(50.0), this.pf(7.0), 2, -1711276033, this.pf(2.4F));
         this.info();
         this.drawer();
      } else {
         Gx.textRight(this.destroyedCount + " Planeten zerstört", this.W - this.p(18.0), this.p(28.0), this.pf(8.0), 2, -1711276033);
      }
   }

   private void button(String var1, String var2, int var3, int var4, Runnable var5) {
      float var6 = this.pf(7.6F);
      int var7 = Math.round(Gx.width(var2, var6, 3)) + this.p(34.0);
      int var8 = this.p(26.0);
      boolean var9 = this.hover(var3, var4, var7, var8);
      float var10 = Ease.to("ss:b" + var2, var9 ? 1.0F : 0.0F, 14.0F);
      Gx.rect(var3, var4, var7, var8, var8 / 2, Gx.mix(-1340860378, -534369212, var10));
      Gx.outline(var3, var4, var7, var8, var8 / 2, Math.max(1, this.p(0.6)), Gx.mix(872415231, -1996488705, var10));
      Gx.icon(var1, var3 + this.p(14.0), var4 + var8 / 2.0F, this.pf(10.0), -1);
      Gx.textMid(var2, var3 + this.p(24.0), var4 + var8 / 2.0F, var6, 3, -1);
      Kit.hit(var3, var4, var7, var8).click(var5);
   }

   private void info() {
      SolarPlanet var1 = this.focus;
      int var2 = this.p(230.0);
      int var3 = this.W - var2 - this.p(16.0);
      int var4 = this.p(78.0);
      int var5 = this.p(220.0);
      Gx.rect(var3, var4, var2, var5, this.p(14.0), -938865384);
      Gx.outline(var3, var4, var2, var5, this.p(14.0), Math.max(1, this.p(0.6)), 721420287);
      var1.aliveShown = var1.aliveShown + (var1.alive() - var1.aliveShown) * Math.min(1.0F, Gx.dt * 3.0F);
      var1.deadShown = var1.deadShown + (var1.dead() - var1.deadShown) * Math.min(1.0F, Gx.dt * 3.0F);
      int var6 = var4 + this.p(18.0);
      this.cap("BEVÖLKERUNG", var3 + this.p(16.0), var6);
      Gx.text(var1.pop <= 0L ? "unbewohnt" : fmt(var1.aliveShown), var3 + this.p(16.0), var6 + this.p(8.0), this.pf(15.0), 3, -9633892);
      var6 += this.p(40.0);
      this.cap("AUSGELÖSCHT", var3 + this.p(16.0), var6);
      Gx.text(fmt(var1.deadShown), var3 + this.p(16.0), var6 + this.p(8.0), this.pf(13.0), 3, -42390);
      var6 += this.p(38.0);
      float var7 = var1.pop <= 0L ? 0.0F : (float)(var1.aliveShown / var1.pop);
      this.bar(var3 + this.p(16.0), var6, var2 - this.p(32.0), "Überlebende", var1.pop <= 0L ? "–" : pct(var7), var7, -9633892);
      var6 += this.p(30.0);
      float var8 = var1.integrity();
      this.bar(var3 + this.p(16.0), var6, var2 - this.p(32.0), "Zerstörung", pct(1.0F - var8), 1.0F - var8, -30150);
      var6 += this.p(30.0);
      Gx.textMid("Temperatur", var3 + this.p(16.0), var6 + this.p(4.0), this.pf(7.2F), 2, -1426063361);
      Gx.textRight(Math.round(var1.temp()) + " °C", var3 + var2 - this.p(16.0), var6 + this.p(4.0), this.pf(7.6F), 3, -1);
      var6 += this.p(16.0);
      Gx.textMid("Atmosphäre", var3 + this.p(16.0), var6 + this.p(4.0), this.pf(7.2F), 2, -1426063361);
      Gx.textRight(var1.atmo0 <= 0.0F ? "keine" : pct(var1.atmo / var1.atmo0), var3 + var2 - this.p(16.0), var6 + this.p(4.0), this.pf(7.6F), 3, -1);
      if (var1.destroyed) {
         Gx.textSpaced("PLANET ZERSTÖRT", var3 + var2 / 2.0F, var4 + var5 - this.p(14.0), this.pf(7.6F), 3, -42390, this.pf(2.0));
      }
   }

   private void cap(String var1, int var2, int var3) {
      float var4 = Gx.spacedWidth(var1, this.pf(6.4F), 3, this.pf(1.6F));
      Gx.textSpaced(var1, var2 + var4 / 2.0F, var3, this.pf(6.4F), 3, -1996488705, this.pf(1.6F));
   }

   private void bar(int var1, int var2, int var3, String var4, String var5, float var6, int var7) {
      Gx.textMid(var4, var1, var2 + this.p(4.0), this.pf(7.2F), 2, -1426063361);
      Gx.textRight(var5, var1 + var3, var2 + this.p(4.0), this.pf(7.6F), 3, -1);
      int var8 = Math.max(3, this.p(4.0));
      Gx.rect(var1, var2 + this.p(12.0), var3, var8, var8 / 2, 587202559);
      Gx.rect(var1, var2 + this.p(12.0), Math.max(var8, Math.round(var3 * Math.max(0.0F, Math.min(1.0F, var6)))), var8, var8 / 2, var7);
   }

   private void drawer() {
      int var1 = this.p(this.drawerOpen ? 122.0 : 34.0);
      int var2 = this.H - var1 - this.p(10.0);
      int var3 = this.p(16.0);
      int var4 = this.W - this.p(32.0) - this.p(246.0);
      Gx.rect(var3, var2, var4, var1, this.p(14.0), -804647656);
      Gx.outline(var3, var2, var4, var1, this.p(14.0), Math.max(1, this.p(0.6)), 721420287);
      int var5 = var3 + this.p(10.0);
      int var6 = var2 + this.p(6.0);

      for (int var7 = 0; var7 < SolarTools.CATS.length; var7++) {
         String var8 = var7 + 1 + "  " + SolarTools.CATS[var7];
         float var9 = this.pf(7.2F);
         int var10 = Math.round(Gx.width(var8, var9, 3)) + this.p(30.0);
         int var11 = this.p(22.0);
         boolean var12 = var7 == this.cat;
         boolean var13 = this.hover(var5, var6, var10, var11);
         Gx.rect(var5, var6, var10, var11, var11 / 2, var12 ? -1890757 : (var13 ? 872415231 : 352321535));
         Gx.icon(SolarTools.CAT_ICONS[var7], var5 + this.p(12.0), var6 + var11 / 2.0F, this.pf(9.0), -1);
         Gx.textMid(var8, var5 + this.p(22.0), var6 + var11 / 2.0F, var9, 3, var12 ? -1 : -855638017);
         int var14 = var7;
         Kit.hit(var5, var6, var10, var11).click(() -> {
            this.cat = var14;
            this.tool = 0;
            this.drawerOpen = true;
         });
         var5 += var10 + this.p(6.0);
      }

      int var27 = this.p(24.0);
      Gx.icon(this.drawerOpen ? "shrink" : "expand", var3 + var4 - this.p(16.0), var2 + this.p(17.0), this.pf(10.0), -1426063361);
      Kit.hit(var3 + var4 - var27 - this.p(4.0), var2 + this.p(4.0), var27, var27).click(() -> this.drawerOpen = !this.drawerOpen);
      if (this.drawerOpen) {
         List var28 = this.tools();
         int var29 = this.p(92.0);
         int var30 = this.p(78.0);
         int var31 = var2 + this.p(36.0);
         int var32 = var3 + this.p(10.0);
         int var33 = this.p(6.0);
         int var34 = Math.max(1, (var4 - this.p(20.0)) / (var29 + var33));
         int var15 = this.tool / var34;
         int var16 = var15 * var34;
         SolarTools.Tool var17 = null;
         int var18 = 0;

         for (int var19 = var16; var19 < Math.min(var28.size(), var16 + var34); var19++) {
            SolarTools.Tool var20 = (SolarTools.Tool)var28.get(var19);
            int var21 = var32 + (var19 - var16) * (var29 + var33);
            boolean var22 = var19 == this.tool;
            boolean var23 = this.hover(var21, var31, var29, var30);
            float var24 = Ease.to("ss:t" + var20.name, var22 ? 1.0F : (var23 ? 0.5F : 0.0F), 16.0F);
            Gx.rect(var21, var31, var29, var30, this.p(10.0), Gx.mix(452984831, Gx.withAlpha(var20.color, 0.28F), var24));
            Gx.outline(var21, var31, var29, var30, this.p(10.0), Math.max(1, this.p(var22 ? 1.2 : 0.6)), var22 ? var20.color : 587202559);
            Gx.glow(var21 + var29 / 2, var31 + this.p(26.0), this.p(20.0), Gx.withAlpha(var20.color, 0.25F + 0.25F * var24));
            Gx.circle(var21 + var29 / 2, var31 + this.p(26.0), this.p(12.0), Gx.mix(var20.color, -16777216, 0.35F));
            Gx.icon(iconOf(var20), var21 + var29 / 2.0F, var31 + this.p(26.0), this.pf(13.0), -1);
            List var25 = Fonts.wrap(var20.name, 3, this.pf(6.8F), var29 - this.p(8.0), 2);

            for (int var26 = 0; var26 < var25.size(); var26++) {
               Gx.textCenter((String)var25.get(var26), var21 + var29 / 2.0F, var31 + this.p(52 + var26 * 10), this.pf(6.8F), 3, -1);
            }

            int var36 = var19;
            Kit.hit(var21, var31, var29, var30).click(() -> this.tool = var36);
            if (var23) {
               var17 = var20;
               var18 = var21 + var29 / 2;
            }
         }

         int var35 = (var28.size() + var34 - 1) / var34;
         if (var35 > 1) {
            Gx.textRight(var15 + 1 + " / " + var35 + "   ◀ ▶", var3 + var4 - this.p(44.0), var2 + this.p(17.0), this.pf(7.0), 2, -1711276033);
            Kit.hit(var3 + var4 - this.p(90.0), var2 + this.p(6.0), this.p(40.0), this.p(22.0))
               .click(() -> this.tool = (this.tool / var34 + 1) % var35 * var34);
         }

         if (var17 != null) {
            Kit.tooltip(var17.desc, var18, var31 - this.p(4.0), this.P);
         }

         Gx.textMid(
            var28.size() + " Werkzeuge · " + SolarTools.ALL.size() + " insgesamt · Mausrad = Größe " + Math.round(this.fx.sizeMul * 100.0F) + "%",
            var3 + this.p(10.0),
            var2 + var1 - this.p(8.0),
            this.pf(6.4F),
            2,
            2013265919
         );
      }
   }

   private static String iconOf(SolarTools.Tool var0) {
      switch (var0.kind) {
         case BEAM:
            return "bolt";
         case SWARM:
            return "eye";
         case HOLE:
            return "moon";
         case GLOBAL:
            return "globe";
         case SPLIT:
            return "hit";
         case AREA:
            return "flame";
         case RAIN:
            return "rain";
         default:
            return var0.cat == 6 ? "heart" : (var0.cat == 7 ? "gift" : "gem");
      }
   }

   private static String fmt(double var0) {
      long var2 = Math.round(Math.max(0.0, var0));
      return String.format(Locale.GERMANY, "%,d", var2);
   }

   private static String big(double var0) {
      if (var0 >= 1.0E9) {
         return String.format(Locale.GERMANY, "%.1f Mrd.", var0 / 1.0E9);
      } else {
         return var0 >= 1000000.0 ? String.format(Locale.GERMANY, "%.1f Mio.", var0 / 1000000.0) : fmt(var0);
      }
   }

   private static String pct(float var0) {
      return String.format(Locale.GERMANY, "%.1f %%", Math.max(0.0F, Math.min(1.0F, var0)) * 100.0F);
   }

   @Override
   public boolean mouseDown(float var1, float var2, int var3, boolean var4) {
      if (Kit.down(var1, var2, var3)) {
         return true;
      } else if (this.zoom < 0.9F || this.focus == null || this.fx == null) {
         return false;
      } else if (var3 == 1) {
         this.fx.release();
         return true;
      } else {
         float var5 = this.pR();
         this.mouseHeld = true;
         this.fx.fire(this.current(), (var1 - this.pcx()) / var5, (var2 - this.pcy()) / var5);
         return true;
      }
   }

   @Override
   public boolean mouseUp(float var1, float var2, int var3) {
      this.mouseHeld = false;
      if (this.fx != null) {
         this.fx.release();
      }

      return Kit.up();
   }

   @Override
   public boolean scroll(float var1, float var2, double var3) {
      if (Kit.scroll(var1, var2, var3)) {
         return true;
      } else {
         if (this.fx != null) {
            this.fx.sizeMul = Math.max(0.3F, Math.min(4.0F, this.fx.sizeMul * (var3 > 0.0 ? 1.12F : 0.89285713F)));
         }

         return true;
      }
   }

   @Override
   public boolean key(int var1, int var2) {
      if (var1 < 0) {
         return false;
      } else if (var1 >= 49 && var1 <= 56) {
         this.cat = var1 - 49;
         this.tool = 0;
         this.drawerOpen = true;
         return true;
      } else if (var1 == 262) {
         this.tool = (this.tool + 1) % this.tools().size();
         return true;
      } else if (var1 == 263) {
         this.tool = (this.tool - 1 + this.tools().size()) % this.tools().size();
         return true;
      } else if (var1 == 32) {
         this.paused = !this.paused;
         return true;
      } else if (var1 == 72) {
         this.hideUi = !this.hideUi;
         return true;
      } else if (var1 == 82 && this.focus != null) {
         this.focus.repair();
         this.fx.clear();
         return true;
      } else if (var1 == 256) {
         if (this.zoomTarget > 0.5F) {
            this.zoomTarget = 0.0F;
            if (this.fx != null) {
               this.fx.release();
            }
         } else {
            this.host.open(new HubView(HubView.Section.GAMES));
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public void onEscape() {
   }

   @Override
   public void removed() {
      long var1 = 0L;

      for (SolarPlanet var4 : this.bodies) {
         var1 += Math.round(var4.dead() / 1000000.0);
      }

      GameInfo.submit("solarsmash", this.destroyedCount * 1000L + var1);
   }
}
