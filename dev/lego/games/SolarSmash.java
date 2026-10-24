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
      SolarPlanet gas = new SolarPlanet(3, "Jovara", "Gasriese", 0L, -140.0F, -10072, 1.2F, 1.45F, 0.35F, 41L, false);
      gas.ringed = true;
      this.add(gas, 10.3F, 3.3F);
      this.add(new SolarPlanet(4, "Glacia", "Eisplanet", 350000000L, -95.0F, -4200193, 0.8F, 0.75F, 0.5F, 51L, false), 12.0F, 5.0F);
      this.add(new SolarPlanet(8, "Selene", "Mond", 0L, -20.0F, -2566964, 0.0F, 0.4F, 0.1F, 61L, true), 13.3F, 0.3F);

      for (SolarPlanet b : this.bodies) {
         b.request(false);
      }
   }

   private void add(SolarPlanet b, float orbit, float angle) {
      b.orbitR = orbit;
      b.orbitA = angle;
      b.orbitW = orbit <= 0.0F ? 0.0F : (float)(0.25 / Math.pow(orbit, 1.2));
      this.bodies.add(b);
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
      List<SolarTools.Tool> l = this.tools();
      return l.get(Math.max(0, Math.min(l.size() - 1, this.tool)));
   }

   private void focus(SolarPlanet b, float sx, float sy, float sr) {
      if (this.focus != b) {
         if (this.focus != null) {
            this.focus.release();
         }

         this.focus = b;
         this.fx = new SolarFx(b);
      }

      b.request(true);
      this.fsx = sx;
      this.fsy = sy;
      this.fsr = sr;
      this.zoomTarget = 1.0F;
      Sound.play("minecraft:block.beacon.activate", 1.6F, 0.4F);
   }

   @Override
   public void draw() {
      float dt = Math.min(0.05F, Gx.dt) * (this.paused ? 0.0F : (this.slow ? 0.25F : 1.0F));
      this.sim += dt;
      this.zoom = this.zoom + (this.zoomTarget - this.zoom) * Math.min(1.0F, Gx.dt * 4.5F);
      if (this.zoomTarget == 0.0F && this.zoom < 0.01F) {
         this.zoom = 0.0F;
      }

      for (SolarPlanet b : this.bodies) {
         b.orbitA = b.orbitA + b.orbitW * dt * (this.zoom > 0.5F ? 0.1F : 1.0F);
         b.update(dt, this.sim);
      }

      if (this.fx != null && this.zoom > 0.3F) {
         this.fx.update(dt);
      }

      if (this.focus != null && this.focus.destroyed && this.focus.destroyedT > 0.1F && this.focus.destroyedT < 0.2F) {
         this.destroyedCount++;
      }

      this.background();
      float sysA = 1.0F - Ease.inOutCubic(Math.min(1.0F, this.zoom * 1.6F));
      if (sysA > 0.01F) {
         Gx.pushAlpha(sysA);
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
      Gx.Img neb = Gx.get("solar:neb", 0, () -> SolarTex.nebula(1280, 720, 5L));
      float par = this.zoom * 0.03F;
      if (neb != null) {
         Gx.image(
            neb,
            Math.round(-this.W * (0.02F + par)),
            Math.round(-this.H * (0.02F + par)),
            Math.round(this.W * (1.04F + par * 2.0F)),
            Math.round(this.H * (1.04F + par * 2.0F)),
            -1
         );
      }

      Gx.Img s1 = Gx.get("solar:stars1", 0, () -> SolarTex.stars(1024, 900, 0.8F, 9L));
      Gx.Img s2 = Gx.get("solar:stars2", 0, () -> SolarTex.stars(512, 160, 1.0F, 19L));
      if (s1 != null) {
         for (int x = 0; x < this.W; x += 1024) {
            for (int y = 0; y < this.H; y += 1024) {
               Gx.image(s1, x, y, 1024, 1024, -1);
            }
         }
      }

      float tw = 0.75F + 0.25F * (float)Math.sin(this.sim * 1.3);
      if (s2 != null) {
         for (int x = -((int)(this.zoom * 60.0F) % 1024); x < this.W; x += 1024) {
            for (int y = 0; y < this.H; y += 1024) {
               Gx.image(s2, x, y, 1024, 1024, Gx.withAlpha(-1, tw));
            }
         }
      }
   }

   private void drawSystem() {
      float cx = this.W * 0.5F;
      float cy = this.H * 0.52F;
      float unit = Math.min(this.W, this.H) / 30.0F;
      SolarPlanet hoverB = null;
      float hx = 0.0F;
      float hy = 0.0F;
      float hr = 0.0F;
      int budget = 120000;
      Gx.Img orb = Gx.get("solar:orbit", 0, () -> SolarTex.orbitRing(1024, 1.2F));

      for (SolarPlanet b : this.bodies) {
         if (!(b.orbitR <= 0.0F) && orb != null) {
            float rx = b.orbitR * unit * 1.25F;
            float ry = rx * 0.42F;
            Gx.image(orb, Math.round(cx - rx), Math.round(cy - ry), Math.round(rx * 2.0F), Math.round(ry * 2.0F), 1090519039);
         }
      }

      List<SolarPlanet> order = new ArrayList<>(this.bodies);
      order.sort((a, c) -> Float.compare((float)Math.sin(a.orbitA) * a.orbitR, (float)Math.sin(c.orbitA) * c.orbitR));

      for (SolarPlanet bx : order) {
         float rx = bx.orbitR * unit * 1.25F;
         float ry = rx * 0.42F;
         float x = cx + (float)Math.cos(bx.orbitA) * rx;
         float y = cy + (float)Math.sin(bx.orbitA) * ry;
         float r = bx.size * unit * (bx.type == 0 ? 1.4F : 0.9F) * (0.85F + 0.15F * (float)Math.sin(bx.orbitA));
         if (bx.type == 0) {
            this.sunGlow(x, y, r);
         }

         SolarPlanet.Disc dc = this.small.computeIfAbsent(bx, k -> new SolarPlanet.Disc("s" + k.name));
         int D = Math.max(16, Math.round(r * 2.0F));
         dc.setup(D, bx.tilt, bx.type == 0);
         float lx = cx - x;
         float ly = cy - y;
         float ll = (float)Math.hypot(lx, ly) + 0.001F;
         if (bx.type == 0) {
            dc.light(0.0F, 0.0F, 1.0F);
         } else {
            dc.light(lx / ll * 0.95F, ly / ll * 0.95F, 0.3F);
         }

         if (bx.refresh(dc, false, budget, false)) {
            budget += 0;
         } else {
            budget = 0;
         }

         if (bx.destroyed) {
            this.debrisRing(x, y, r);
         } else {
            if (bx.atmo > 0.05F) {
               this.halo(x, y, r, bx.atmoCol, 0.35F * bx.atmo);
            }

            SolarPlanet.drawDisc(dc, x, y, r, -1);
         }

         if (this.hover(
            Math.round(x - r - this.p(6.0)), Math.round(y - r - this.p(6.0)), Math.round(r * 2.0F + this.p(12.0)), Math.round(r * 2.0F + this.p(12.0))
         )) {
            hoverB = bx;
            hx = x;
            hy = y;
            hr = r;
         }

         Kit.hit(Math.round(x - r - this.p(6.0)), Math.round(y - r - this.p(6.0)), Math.round(r * 2.0F + this.p(12.0)), Math.round(r * 2.0F + this.p(12.0)))
            .click(() -> this.focus(b, x, y, r));
      }

      if (hoverB != null) {
         Gx.ringAt(Math.round(hx), Math.round(hy), Math.round(hr + this.p(5.0)), Math.max(1, this.p(1.0)), -1426063361);
         this.label(
            hoverB.name.toUpperCase(Locale.ROOT), hoverB.kind + (hoverB.pop > 0L ? " · " + big(hoverB.alive()) + " Bewohner" : ""), hx, hy - hr - this.p(14.0)
         );
      }

      Gx.textSpaced("SOLAR SMASH", this.W / 2.0F, this.p(34.0), this.pf(22.0), 3, -1, this.pf(6.0));
      Gx.textSpaced("KLICK AUF EINEN PLANETEN", this.W / 2.0F, this.p(58.0), this.pf(7.4F), 2, -1711276033, this.pf(2.4F));
   }

   private void label(String t, String sub, float cx, float bottom) {
      float w = Math.max(Gx.spacedWidth(t, this.pf(9.0), 3, this.pf(2.0)), Gx.width(sub, this.pf(7.0), 2)) + this.p(24.0);
      int x = Math.round(cx - w / 2.0F);
      int y = Math.round(bottom - this.p(34.0));
      Gx.rect(x, y, Math.round(w), this.p(32.0), this.p(9.0), -871756520);
      Gx.outline(x, y, Math.round(w), this.p(32.0), this.p(9.0), Math.max(1, this.p(0.6)), 872415231);
      Gx.textSpaced(t, cx, y + this.p(11.0), this.pf(9.0), 3, -1, this.pf(2.0));
      Gx.textCenter(sub, cx, y + this.p(23.0), this.pf(7.0), 2, -1426063361);
   }

   private void sunGlow(float x, float y, float r) {
      Gx.Img co = Gx.get("solar:corona", 0, () -> SolarTex.corona(512, 3L));
      float pulse = 1.0F + 0.03F * (float)Math.sin(this.sim * 2.0F);
      if (co != null) {
         int d = Math.round(r * 5.2F * pulse);
         Gx.image(co, Math.round(x - d / 2.0F), Math.round(y - d / 2.0F), d, d, -1);
      }

      Gx.glow(Math.round(x), Math.round(y), Math.round(r * 3.2F), 1442820160);
   }

   private void halo(float x, float y, float r, int col, float a) {
      Gx.Img h = Gx.get("solar:halo", 0, () -> SolarTex.halo(256));
      if (h != null) {
         int d = Math.round(r * 2.5F);
         Gx.image(h, Math.round(x - d / 2.0F), Math.round(y - d / 2.0F), d, d, Gx.withAlpha(col, Math.min(1.0F, a)));
      }
   }

   private void debrisRing(float x, float y, float r) {
      Gx.Img rk = Gx.get("solar:fx:rock1", 0, () -> SolarTex.rock(64, 12L, -1));
      if (rk != null) {
         for (int i = 0; i < 18; i++) {
            double a = i * 0.7 + this.sim * 0.2;
            double d = r * (0.6 + i % 5 * 0.2);
            int s = Math.max(2, Math.round(r * (0.12F + i % 3 * 0.06F)));
            Gx.image(rk, (int)Math.round(x + Math.cos(a) * d - s / 2.0), (int)Math.round(y + Math.sin(a) * d * 0.6 - s / 2.0), s, s, -7701910);
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
      SolarPlanet b = this.focus;
      float k = Ease.inOutCubic(this.zoom);
      float shake = b.shake * this.p(6.0);
      float cx = this.fsx + (this.pcx() - this.fsx) * k + (this.fx.rnd.nextFloat() - 0.5F) * shake;
      float cy = this.fsy + (this.pcy() - this.fsy) * k + (this.fx.rnd.nextFloat() - 0.5F) * shake;
      float R = this.fsr + (this.pR() - this.fsr) * k;
      Gx.pushAlpha(Math.min(1.0F, this.zoom * 2.0F));
      if (b.type != 0) {
         this.sunGlow(-this.W * 0.08F, -this.H * 0.12F, Math.min(this.W, this.H) * 0.12F);
      }

      this.fx.drawBack(cx, cy, R);
      float Rs = R * b.scale;
      if (!b.destroyed) {
         if (b.atmo > 0.02F) {
            this.halo(cx, cy, Rs, b.atmoCol, 0.55F * b.atmo / Math.max(0.3F, b.atmo0));
         }

         if (b.type == 0) {
            this.sunGlow(cx, cy, Rs);
         }

         Gx.Img[] ring = null;
         if (b.ringed && b.ringAlive() > 0.02F) {
            int rw = Math.max(8, Math.round(Rs * 2.35F));
            int rh = Math.max(8, Math.round(rw * 0.62F));
            ring = b.ringImgs(rw, rh, Rs / 2.0F, -0.55F, -0.45F, 0.7F);
            Gx.image(ring[0], Math.round(cx - rw), Math.round(cy - rh), rw * 2, rh * 2, -1);
         }

         int D = Math.max(32, Math.min(1400, Math.round(Rs * 2.0F)));
         this.big.setup(D, b.tilt, b.type == 0);
         if (b.type == 0) {
            this.big.light(0.0F, 0.0F, 1.0F);
         } else {
            this.big.light(-0.62F, -0.42F, 0.66F);
         }

         b.refresh(this.big, true, 170000, false);
         int col = b.hitFlash > 0.01F ? Gx.mix(-1, -12112, b.hitFlash) : -1;
         SolarPlanet.drawDisc(this.big, cx, cy, Rs, col);
         if (ring != null) {
            Gx.image(ring[1], Math.round(cx - ring[1].w), Math.round(cy - ring[1].h), ring[1].w * 2, ring[1].h * 2, -1);
         }

         if (b.loading() && this.zoom > 0.9F) {
            Gx.textCenter("Oberfläche wird in 4K erzeugt …", cx, cy + Rs + this.p(18.0), this.pf(8.0), 2, -1426063361);
         }
      }

      this.fx.drawFront(cx, cy, R);
      if (this.zoom > 0.95F && !this.overUi()) {
         SolarTools.Tool t = this.current();
         float s = Math.max((float)this.p(6.0), R * Math.min(0.5F, t.r / 1024.0F) * this.fx.sizeMul);
         Gx.ringAt(Math.round(this.mx), Math.round(this.my), Math.round(s), Math.max(1, this.p(0.8)), Gx.withAlpha(t.color, 0.8F));
         Gx.fill(Math.round(this.mx) - this.p(4.0), Math.round(this.my), this.p(8.0), Math.max(1, this.p(0.8)), -855638017);
         Gx.fill(Math.round(this.mx), Math.round(this.my) - this.p(4.0), Math.max(1, this.p(0.8)), this.p(8.0), -855638017);
      }

      Gx.popAlpha();
      if (this.mouseHeld && this.fx.beamOn) {
         this.fx.beamX = (this.mx - cx) / R;
         this.fx.beamY = (this.my - cy) / R;
      }
   }

   private boolean overUi() {
      return this.my > this.uiBottom || this.my < this.uiTop || this.zoom > 0.5F && this.mx > this.W - this.p(250.0);
   }

   private void hud() {
      this.uiTop = this.p(64.0);
      this.uiBottom = this.H - (this.zoom > 0.5F ? this.p(this.drawerOpen ? 132.0 : 44.0) : this.p(40.0));
      Gx.gradientV(0, 0, this.W, this.p(70.0), -872415232, 0);
      int bx = this.p(16.0);
      int by = this.p(14.0);
      this.button("back", this.zoom > 0.5F ? "System" : "Zurück", bx, by, () -> {
         if (this.zoomTarget > 0.5F) {
            this.zoomTarget = 0.0F;
            this.fx.release();
         } else {
            this.host.open(new HubView(HubView.Section.GAMES));
         }
      });
      if (this.zoom > 0.5F && this.focus != null) {
         this.button("reset", "Reparieren", bx + this.p(96.0), by, () -> {
            this.focus.repair();
            this.fx.clear();
            Sound.play("minecraft:block.beacon.activate", 1.2F, 0.5F);
         });
         this.button(this.slow ? "check" : "clock", this.slow ? "Zeitlupe an" : "Zeitlupe", bx + this.p(206.0), by, () -> this.slow = !this.slow);
         this.button(this.paused ? "play" : "pause", this.paused ? "Weiter" : "Pause", bx + this.p(316.0), by, () -> this.paused = !this.paused);
         this.button("eye", "UI aus (H)", bx + this.p(416.0), by, () -> this.hideUi = true);
         Gx.textSpaced(this.focus.name.toUpperCase(Locale.ROOT), this.W / 2.0F + this.p(40.0), this.p(30.0), this.pf(16.0), 3, -1, this.pf(5.0));
         Gx.textSpaced(this.focus.kind.toUpperCase(Locale.ROOT), this.W / 2.0F + this.p(40.0), this.p(50.0), this.pf(7.0), 2, -1711276033, this.pf(2.4F));
         this.info();
         this.drawer();
      } else {
         Gx.textRight(this.destroyedCount + " Planeten zerstört", this.W - this.p(18.0), this.p(28.0), this.pf(8.0), 2, -1711276033);
      }
   }

   private void button(String icon, String label, int x, int y, Runnable r) {
      float fs = this.pf(7.6F);
      int w = Math.round(Gx.width(label, fs, 3)) + this.p(34.0);
      int h = this.p(26.0);
      boolean hv = this.hover(x, y, w, h);
      float a = Ease.to("ss:b" + label, hv ? 1.0F : 0.0F, 14.0F);
      Gx.rect(x, y, w, h, h / 2, Gx.mix(-1340860378, -534369212, a));
      Gx.outline(x, y, w, h, h / 2, Math.max(1, this.p(0.6)), Gx.mix(872415231, -1996488705, a));
      Gx.icon(icon, x + this.p(14.0), y + h / 2.0F, this.pf(10.0), -1);
      Gx.textMid(label, x + this.p(24.0), y + h / 2.0F, fs, 3, -1);
      Kit.hit(x, y, w, h).click(r);
   }

   private void info() {
      SolarPlanet b = this.focus;
      int w = this.p(230.0);
      int x = this.W - w - this.p(16.0);
      int y = this.p(78.0);
      int h = this.p(220.0);
      Gx.rect(x, y, w, h, this.p(14.0), -938865384);
      Gx.outline(x, y, w, h, this.p(14.0), Math.max(1, this.p(0.6)), 721420287);
      b.aliveShown = b.aliveShown + (b.alive() - b.aliveShown) * Math.min(1.0F, Gx.dt * 3.0F);
      b.deadShown = b.deadShown + (b.dead() - b.deadShown) * Math.min(1.0F, Gx.dt * 3.0F);
      int yy = y + this.p(18.0);
      this.cap("BEVÖLKERUNG", x + this.p(16.0), yy);
      Gx.text(b.pop <= 0L ? "unbewohnt" : fmt(b.aliveShown), x + this.p(16.0), yy + this.p(8.0), this.pf(15.0), 3, -9633892);
      yy += this.p(40.0);
      this.cap("AUSGELÖSCHT", x + this.p(16.0), yy);
      Gx.text(fmt(b.deadShown), x + this.p(16.0), yy + this.p(8.0), this.pf(13.0), 3, -42390);
      yy += this.p(38.0);
      float surv = b.pop <= 0L ? 0.0F : (float)(b.aliveShown / b.pop);
      this.bar(x + this.p(16.0), yy, w - this.p(32.0), "Überlebende", b.pop <= 0L ? "–" : pct(surv), surv, -9633892);
      yy += this.p(30.0);
      float integ = b.integrity();
      this.bar(x + this.p(16.0), yy, w - this.p(32.0), "Zerstörung", pct(1.0F - integ), 1.0F - integ, -30150);
      yy += this.p(30.0);
      Gx.textMid("Temperatur", x + this.p(16.0), yy + this.p(4.0), this.pf(7.2F), 2, -1426063361);
      Gx.textRight(Math.round(b.temp()) + " °C", x + w - this.p(16.0), yy + this.p(4.0), this.pf(7.6F), 3, -1);
      yy += this.p(16.0);
      Gx.textMid("Atmosphäre", x + this.p(16.0), yy + this.p(4.0), this.pf(7.2F), 2, -1426063361);
      Gx.textRight(b.atmo0 <= 0.0F ? "keine" : pct(b.atmo / b.atmo0), x + w - this.p(16.0), yy + this.p(4.0), this.pf(7.6F), 3, -1);
      if (b.destroyed) {
         Gx.textSpaced("PLANET ZERSTÖRT", x + w / 2.0F, y + h - this.p(14.0), this.pf(7.6F), 3, -42390, this.pf(2.0));
      }
   }

   private void cap(String s, int x, int y) {
      float w = Gx.spacedWidth(s, this.pf(6.4F), 3, this.pf(1.6F));
      Gx.textSpaced(s, x + w / 2.0F, y, this.pf(6.4F), 3, -1996488705, this.pf(1.6F));
   }

   private void bar(int x, int y, int w, String label, String val, float f, int col) {
      Gx.textMid(label, x, y + this.p(4.0), this.pf(7.2F), 2, -1426063361);
      Gx.textRight(val, x + w, y + this.p(4.0), this.pf(7.6F), 3, -1);
      int bh = Math.max(3, this.p(4.0));
      Gx.rect(x, y + this.p(12.0), w, bh, bh / 2, 587202559);
      Gx.rect(x, y + this.p(12.0), Math.max(bh, Math.round(w * Math.max(0.0F, Math.min(1.0F, f)))), bh, bh / 2, col);
   }

   private void drawer() {
      int h = this.p(this.drawerOpen ? 122.0 : 34.0);
      int y = this.H - h - this.p(10.0);
      int x = this.p(16.0);
      int w = this.W - this.p(32.0) - this.p(246.0);
      Gx.rect(x, y, w, h, this.p(14.0), -804647656);
      Gx.outline(x, y, w, h, this.p(14.0), Math.max(1, this.p(0.6)), 721420287);
      int cx = x + this.p(10.0);
      int cy = y + this.p(6.0);

      for (int i = 0; i < SolarTools.CATS.length; i++) {
         String c = i + 1 + "  " + SolarTools.CATS[i];
         float fs = this.pf(7.2F);
         int cw = Math.round(Gx.width(c, fs, 3)) + this.p(30.0);
         int ch = this.p(22.0);
         boolean sel = i == this.cat;
         boolean hv = this.hover(cx, cy, cw, ch);
         Gx.rect(cx, cy, cw, ch, ch / 2, sel ? -1890757 : (hv ? 872415231 : 352321535));
         Gx.icon(SolarTools.CAT_ICONS[i], cx + this.p(12.0), cy + ch / 2.0F, this.pf(9.0), -1);
         Gx.textMid(c, cx + this.p(22.0), cy + ch / 2.0F, fs, 3, sel ? -1 : -855638017);
         int idx = i;
         Kit.hit(cx, cy, cw, ch).click(() -> {
            this.cat = idx;
            this.tool = 0;
            this.drawerOpen = true;
         });
         cx += cw + this.p(6.0);
      }

      int tg = this.p(24.0);
      Gx.icon(this.drawerOpen ? "shrink" : "expand", x + w - this.p(16.0), y + this.p(17.0), this.pf(10.0), -1426063361);
      Kit.hit(x + w - tg - this.p(4.0), y + this.p(4.0), tg, tg).click(() -> this.drawerOpen = !this.drawerOpen);
      if (this.drawerOpen) {
         List<SolarTools.Tool> l = this.tools();
         int tw = this.p(92.0);
         int th = this.p(78.0);
         int ty = y + this.p(36.0);
         int tx = x + this.p(10.0);
         int gap = this.p(6.0);
         int per = Math.max(1, (w - this.p(20.0)) / (tw + gap));
         int page = this.tool / per;
         int start = page * per;
         SolarTools.Tool hoverT = null;
         int hoverX = 0;

         for (int i = start; i < Math.min(l.size(), start + per); i++) {
            SolarTools.Tool t = l.get(i);
            int bx = tx + (i - start) * (tw + gap);
            boolean sel = i == this.tool;
            boolean hv = this.hover(bx, ty, tw, th);
            float a = Ease.to("ss:t" + t.name, sel ? 1.0F : (hv ? 0.5F : 0.0F), 16.0F);
            Gx.rect(bx, ty, tw, th, this.p(10.0), Gx.mix(452984831, Gx.withAlpha(t.color, 0.28F), a));
            Gx.outline(bx, ty, tw, th, this.p(10.0), Math.max(1, this.p(sel ? 1.2 : 0.6)), sel ? t.color : 587202559);
            Gx.glow(bx + tw / 2, ty + this.p(26.0), this.p(20.0), Gx.withAlpha(t.color, 0.25F + 0.25F * a));
            Gx.circle(bx + tw / 2, ty + this.p(26.0), this.p(12.0), Gx.mix(t.color, -16777216, 0.35F));
            Gx.icon(iconOf(t), bx + tw / 2.0F, ty + this.p(26.0), this.pf(13.0), -1);
            List<String> nl = Fonts.wrap(t.name, 3, this.pf(6.8F), tw - this.p(8.0), 2);

            for (int k = 0; k < nl.size(); k++) {
               Gx.textCenter(nl.get(k), bx + tw / 2.0F, ty + this.p(52 + k * 10), this.pf(6.8F), 3, -1);
            }

            int idx = i;
            Kit.hit(bx, ty, tw, th).click(() -> this.tool = idx);
            if (hv) {
               hoverT = t;
               hoverX = bx + tw / 2;
            }
         }

         int pages = (l.size() + per - 1) / per;
         if (pages > 1) {
            Gx.textRight(page + 1 + " / " + pages + "   ◀ ▶", x + w - this.p(44.0), y + this.p(17.0), this.pf(7.0), 2, -1711276033);
            Kit.hit(x + w - this.p(90.0), y + this.p(6.0), this.p(40.0), this.p(22.0)).click(() -> this.tool = (this.tool / per + 1) % pages * per);
         }

         if (hoverT != null) {
            Kit.tooltip(hoverT.desc, hoverX, ty - this.p(4.0), this.P);
         }

         Gx.textMid(
            l.size() + " Werkzeuge · " + SolarTools.ALL.size() + " insgesamt · Mausrad = Größe " + Math.round(this.fx.sizeMul * 100.0F) + "%",
            x + this.p(10.0),
            y + h - this.p(8.0),
            this.pf(6.4F),
            2,
            2013265919
         );
      }
   }

   private static String iconOf(SolarTools.Tool t) {
      switch (t.kind) {
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
            return t.cat == 6 ? "heart" : (t.cat == 7 ? "gift" : "gem");
      }
   }

   private static String fmt(double v) {
      long n = Math.round(Math.max(0.0, v));
      return String.format(Locale.GERMANY, "%,d", n);
   }

   private static String big(double v) {
      if (v >= 1.0E9) {
         return String.format(Locale.GERMANY, "%.1f Mrd.", v / 1.0E9);
      } else {
         return v >= 1000000.0 ? String.format(Locale.GERMANY, "%.1f Mio.", v / 1000000.0) : fmt(v);
      }
   }

   private static String pct(float f) {
      return String.format(Locale.GERMANY, "%.1f %%", Math.max(0.0F, Math.min(1.0F, f)) * 100.0F);
   }

   @Override
   public boolean mouseDown(float x, float y, int button, boolean dbl) {
      if (Kit.down(x, y, button)) {
         return true;
      } else if (this.zoom < 0.9F || this.focus == null || this.fx == null) {
         return false;
      } else if (button == 1) {
         this.fx.release();
         return true;
      } else {
         float R = this.pR();
         this.mouseHeld = true;
         this.fx.fire(this.current(), (x - this.pcx()) / R, (y - this.pcy()) / R);
         return true;
      }
   }

   @Override
   public boolean mouseUp(float x, float y, int button) {
      this.mouseHeld = false;
      if (this.fx != null) {
         this.fx.release();
      }

      return Kit.up();
   }

   @Override
   public boolean scroll(float x, float y, double amount) {
      if (Kit.scroll(x, y, amount)) {
         return true;
      } else {
         if (this.fx != null) {
            this.fx.sizeMul = Math.max(0.3F, Math.min(4.0F, this.fx.sizeMul * (amount > 0.0 ? 1.12F : 0.89285713F)));
         }

         return true;
      }
   }

   @Override
   public boolean key(int key, int mods) {
      if (key < 0) {
         return false;
      } else if (key >= 49 && key <= 56) {
         this.cat = key - 49;
         this.tool = 0;
         this.drawerOpen = true;
         return true;
      } else if (key == 262) {
         this.tool = (this.tool + 1) % this.tools().size();
         return true;
      } else if (key == 263) {
         this.tool = (this.tool - 1 + this.tools().size()) % this.tools().size();
         return true;
      } else if (key == 32) {
         this.paused = !this.paused;
         return true;
      } else if (key == 72) {
         this.hideUi = !this.hideUi;
         return true;
      } else if (key == 82 && this.focus != null) {
         this.focus.repair();
         this.fx.clear();
         return true;
      } else if (key == 256) {
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
      long total = 0L;

      for (SolarPlanet b : this.bodies) {
         total += Math.round(b.dead() / 1000000.0);
      }

      GameInfo.submit("solarsmash", this.destroyedCount * 1000L + total);
   }
}
