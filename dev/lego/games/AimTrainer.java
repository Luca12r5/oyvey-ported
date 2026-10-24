package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AimTrainer extends GameView {
   private static final float W = 380.0F;
   private static final float H = 270.0F;
   private static final float TOP = 34.0F;
   private static final float DURATION = 30.0F;
   private static final float MAX_R = 18.0F;
   private final List<AimTrainer.Target> targets = new ArrayList<>();
   private final List<float[]> parts = new ArrayList<>();
   private final List<float[]> rings = new ArrayList<>();
   private final List<float[]> pops = new ArrayList<>();
   private final List<float[]> misses = new ArrayList<>();
   private int hits;
   private int shots;
   private int expired;
   private int combo;
   private int bestCombo;
   private float spawnT;
   private float comboPop;

   public AimTrainer() {
      super("aim");
   }

   @Override
   protected float boardW() {
      return 380.0F;
   }

   @Override
   protected float boardH() {
      return 270.0F;
   }

   @Override
   protected String overTitle() {
      return "Zeit um!";
   }

   @Override
   protected void reset() {
      this.targets.clear();
      this.parts.clear();
      this.rings.clear();
      this.pops.clear();
      this.misses.clear();
      this.hits = this.shots = this.expired = this.combo = this.bestCombo = 0;
      this.spawnT = 0.35F;
      this.comboPop = 0.0F;
   }

   private int accuracy() {
      return this.shots == 0 ? 100 : Math.round(this.hits * 100.0F / this.shots);
   }

   private void spawn() {
      float pad = 24.0F;

      for (int tries = 0; tries < 30; tries++) {
         float x = pad + this.rnd.nextFloat() * (380.0F - pad * 2.0F);
         float y = 34.0F + pad + this.rnd.nextFloat() * (236.0F - pad * 2.0F);
         boolean ok = true;

         for (AimTrainer.Target t : this.targets) {
            float dx = t.x - x;
            float dy = t.y - y;
            if (dx * dx + dy * dy < 2190.24F) {
               ok = false;
               break;
            }
         }

         if (ok || tries == 29) {
            float life = 1.45F - 0.3F * Math.min(1.0F, this.time / 30.0F);
            this.targets.add(new AimTrainer.Target(x, y, life));
            return;
         }
      }
   }

   @Override
   protected void onClick(float x, float y, int button) {
      if (button == 0) {
         if (!(y < 30.0F)) {
            this.shots++;
            AimTrainer.Target hit = null;
            float best = Float.MAX_VALUE;

            for (AimTrainer.Target t : this.targets) {
               float r = t.radius();
               float dx = x - t.x;
               float dy = y - t.y;
               float d = (float)Math.sqrt(dx * dx + dy * dy);
               if (d <= r + 1.2F && d / Math.max(1.0F, r) < best) {
                  best = d / Math.max(1.0F, r);
                  hit = t;
               }
            }

            if (hit == null) {
               this.combo = 0;
               this.misses.add(new float[]{x, y, 0.0F});
               Sound.play("minecraft:block.note_block.hat", 0.6F, 0.25F);
            } else {
               this.targets.remove(hit);
               int pts = best <= 0.34F ? 100 : (best <= 0.67F ? 70 : 40);
               this.score += pts;
               this.hits++;
               this.combo++;
               this.bestCombo = Math.max(this.bestCombo, this.combo);
               this.comboPop = 1.0F;
               int col = pts == 100 ? -15043 : (pts == 70 ? Style.accent : -1);
               this.pops.add(new float[]{hit.x, hit.y, 0.0F, pts});
               this.rings.add(new float[]{hit.x, hit.y, 0.0F, hit.radius(), col});

               for (int i = 0; i < 16; i++) {
                  float a = (float)(this.rnd.nextFloat() * Math.PI * 2.0);
                  float s = 50.0F + this.rnd.nextFloat() * 120.0F;
                  int c = i % 3 == 0 ? -1 : (i % 3 == 1 ? this.info.color : col);
                  this.parts
                     .add(
                        new float[]{
                           hit.x,
                           hit.y,
                           (float)Math.cos(a) * s,
                           (float)Math.sin(a) * s - 30.0F,
                           0.0F,
                           0.45F + this.rnd.nextFloat() * 0.35F,
                           2.0F + this.rnd.nextFloat() * 2.5F,
                           c
                        }
                     );
               }

               Sound.play("minecraft:entity.arrow.hit_player", pts == 100 ? 1.25F : (pts == 70 ? 1.05F : 0.85F), 0.35F);
               if (pts == 100) {
                  Sound.play("minecraft:entity.player.attack.crit", 1.2F, 0.2F);
               }

               if (this.targets.isEmpty()) {
                  this.spawnT = Math.min(this.spawnT, 0.08F);
               }
            }
         }
      }
   }

   @Override
   protected void update(float dt) {
      this.comboPop = Math.max(0.0F, this.comboPop - dt * 4.0F);

      for (AimTrainer.Target t : this.targets) {
         t.age += dt;
      }

      for (int i = this.targets.size() - 1; i >= 0; i--) {
         AimTrainer.Target t = this.targets.get(i);
         if (t.age >= t.life) {
            this.targets.remove(i);
            this.expired++;
            this.combo = 0;
            this.rings.add(new float[]{t.x, t.y, 0.0F, 4.0F, -7696228.0F});
         }
      }

      for (float[] p : this.parts) {
         p[0] += p[2] * dt;
         p[1] += p[3] * dt;
         p[3] += 360.0F * dt;
         p[2] *= 0.98F;
         p[4] += dt;
      }

      this.parts.removeIf(p -> p[4] >= p[5]);

      for (float[] r : this.rings) {
         r[2] += dt;
      }

      this.rings.removeIf(r -> r[2] > 0.45F);

      for (float[] p : this.pops) {
         p[2] += dt;
      }

      this.pops.removeIf(p -> p[2] > 0.8F);

      for (float[] m : this.misses) {
         m[2] += dt;
      }

      this.misses.removeIf(m -> m[2] > 0.5F);
      float prog = Math.min(1.0F, this.time / 30.0F);
      int maxTargets = prog < 0.3F ? 2 : 3;
      this.spawnT -= dt;
      if (this.targets.isEmpty() && this.spawnT > 0.12F) {
         this.spawnT = 0.12F;
      }

      if (this.spawnT <= 0.0F && this.targets.size() < maxTargets) {
         this.spawn();
         this.spawnT = 0.62F - 0.22F * prog + this.rnd.nextFloat() * 0.12F;
      }

      if (this.time + dt >= 30.0F) {
         this.targets.clear();
         this.gameOver(true);
      }
   }

   @Override
   protected void render() {
      this.boxV(0.0F, 0.0F, 380.0F, 270.0F, 12.0F, Style.light ? -1117963 : -15724006, Style.light ? -2038806 : -16119024);
      int grid = Style.light ? 201326592 : 184549375;

      for (float x = 20.0F; x < 380.0F; x += 20.0F) {
         this.box(x, 34.0F, 0.6F, 236.0F, 0.0F, grid);
      }

      for (float y = 52.0F; y < 270.0F; y += 20.0F) {
         this.box(0.0F, y, 380.0F, 0.6F, 0.0F, grid);
      }

      Gx.glow(this.X(190.0F), this.Y(145.0F), this.S(170.0F), Gx.withAlpha(this.info.color, 0.05F));

      for (float[] r : this.rings) {
         float k = r[2] / 0.45F;
         float e = Ease.outCubic(k);
         float rad = r[3] + e * 16.0F;
         Gx.ringAt(this.X(r[0]), this.Y(r[1]), this.S(rad), Math.max(1, this.S(2.2F * (1.0F - k) + 0.4F)), Gx.withAlpha((int)r[4], 0.8F * (1.0F - k)));
      }

      for (AimTrainer.Target t : this.targets) {
         this.drawTarget(t);
      }

      for (float[] p : this.parts) {
         float a = 1.0F - p[4] / p[5];
         this.box(p[0] - p[6] / 2.0F, p[1] - p[6] / 2.0F, p[6], p[6], p[6] * 0.25F, Gx.withAlpha((int)p[7], a));
      }

      for (float[] m : this.misses) {
         float k = m[2] / 0.5F;
         float a = 1.0F - k;
         float s = 3.0F + Ease.outCubic(k) * 2.0F;
         Gx.push();
         Gx.pushAlpha(a);
         Gx.icon("x", this.X(m[0]), this.Y(m[1]), this.S(s * 2.4F), Style.danger);
         Gx.popAlpha();
         Gx.pop();
      }

      for (float[] p : this.pops) {
         float k = p[2] / 0.8F;
         float a = k < 0.6F ? 1.0F : 1.0F - (k - 0.6F) / 0.4F;
         float sc = Ease.outBack(Math.min(1.0F, p[2] / 0.18F));
         int pts = (int)p[3];
         int col = pts == 100 ? -15043 : (pts == 70 ? Style.accent : -1);
         Gx.push();
         Gx.scaleAt(this.X(p[0]), this.Y(p[1] - 16.0F - k * 14.0F), 0.6F + 0.4F * sc);
         this.text("+" + pts, p[0], p[1] - 16.0F - k * 14.0F, pts == 100 ? 11.0F : 9.0F, 3, Gx.withAlpha(col, a));
         Gx.pop();
      }

      this.hud();
      this.crosshair();
   }

   private void drawTarget(AimTrainer.Target t) {
      float r = t.radius();
      if (!(r < 0.6F)) {
         float lifeK = t.age / t.life;
         int red = this.info.color;
         int acc = Style.accent;
         Gx.glow(this.X(t.x), this.Y(t.y), this.S(r * 2.1F), Gx.withAlpha(red, 0.28F));
         this.circle(t.x, t.y + r * 0.08F, r * 1.02F, 1426063360);
         this.circle(t.x, t.y, r, Gx.mix(red, -16777216, 0.12F));
         this.circle(t.x, t.y, r * 0.84F, -723465);
         this.circle(t.x, t.y, r * 0.67F, red);
         this.circle(t.x, t.y, r * 0.5F, -723465);
         this.circle(t.x, t.y, r * 0.34F, acc);
         this.circle(t.x, t.y - r * 0.08F, r * 0.2F, Gx.mix(acc, -1, 0.55F));
         if (lifeK > 0.6F) {
            float a = (lifeK - 0.6F) / 0.4F;
            Gx.ringAt(this.X(t.x), this.Y(t.y), this.S(r + 2.5F), Math.max(1, this.S(0.8F)), Gx.withAlpha(Style.danger, 0.6F * a));
         }
      }
   }

   private void hud() {
      float prog = Math.min(1.0F, this.time / 30.0F);
      float left = Math.max(0.0F, 30.0F - this.time);
      this.box(0.0F, 0.0F, 380.0F, 30.0F, 12.0F, Style.light ? 251658240 : 1073741824);
      this.box(0.0F, 12.0F, 380.0F, 18.0F, 0.0F, Style.light ? 251658240 : 1073741824);
      this.box(0.0F, 30.0F, 380.0F, 0.7F, 0.0F, Style.stroke);
      int barCol = left < 5.0F ? Gx.mix(Style.danger, -1, (float)(0.5 + 0.5 * Math.sin(this.time * 12.0F)) * 0.3F) : Style.accent;
      this.box(0.0F, 28.5F, 380.0F, 3.0F, 0.0F, Style.light ? 335544320 : 352321535);
      this.box(0.0F, 28.5F, 380.0F * (1.0F - prog), 3.0F, 0.0F, barCol);
      Gx.glow(this.X(380.0F * (1.0F - prog)), this.Y(30.0F), this.S(10.0F), Gx.withAlpha(barCol, 0.5F));
      float cy = 14.5F;
      String tt = String.format(Locale.ROOT, "%.1f s", left);
      Gx.icon("clock", this.X(166.0F), this.Y(cy), this.S(9.0F), left < 5.0F ? Style.danger : Style.sub);
      Gx.textMid(tt, this.X(173.0F), this.Y(cy), this.S(10.0F), 3, left < 5.0F ? Style.danger : Style.text);
      this.stat(10.0F, cy, "target", "Treffer", String.valueOf(this.hits));
      this.stat(84.0F, cy, "crosshair", "Genauigkeit", this.accuracy() + "%");
      float pop = 1.0F + 0.25F * Ease.outCubic(this.comboPop);
      String cs = "x" + this.combo;
      float cw = Gx.width(cs, this.S(10.0F), 3) / this.u;
      float rx = 370.0F;
      Gx.push();
      Gx.scaleAt(this.X(rx - cw / 2.0F), this.Y(cy), pop);
      Gx.textRight(cs, this.X(rx), this.Y(cy), this.S(10.0F), 3, this.combo >= 5 ? -15043 : (this.combo > 0 ? Style.accent : Style.muted));
      Gx.pop();
      Gx.textRight("COMBO", this.X(rx - cw - 5.0F), this.Y(cy), this.S(6.0F), 3, Style.muted);
      if (this.combo >= 5) {
         Gx.icon("flame", this.X(rx - cw - 40.0F), this.Y(cy), this.S(9.0F), -23235);
      }
   }

   private void stat(float x, float cy, String icon, String label, String value) {
      Gx.icon(icon, this.X(x + 5.0F), this.Y(cy), this.S(9.0F), Style.sub);
      Gx.textMid(label, this.X(x + 13.0F), this.Y(cy - 4.5F), this.S(5.4F), 3, Style.muted);
      Gx.textMid(value, this.X(x + 13.0F), this.Y(cy + 4.2F), this.S(8.5F), 3, Style.text);
   }

   private void crosshair() {
      float gx = (this.mx - this.bx) / this.u;
      float gy = (this.my - this.by) / this.u;
      if (this.state == GameView.State.PLAYING && !(gx < 0.0F) && !(gy < 34.0F) && !(gx > 380.0F) && !(gy > 270.0F)) {
         int x = Math.round(this.mx);
         int y = Math.round(this.my);
         int len = this.S(4.5F);
         int gap = this.S(2.2F);
         int th = Math.max(1, this.S(0.9F));
         int sh = -2013265920;
         int c = -1;

         for (int pass = 0; pass < 2; pass++) {
            int o = pass == 0 ? Math.max(1, this.S(0.5F)) : 0;
            int col = pass == 0 ? sh : c;
            int t2 = th + (pass == 0 ? 2 * o : 0);
            Gx.fill(x - gap - len - o, y - t2 / 2, len + 2 * o, t2, col);
            Gx.fill(x + gap - o, y - t2 / 2, len + 2 * o, t2, col);
            Gx.fill(x - t2 / 2, y - gap - len - o, t2, len + 2 * o, col);
            Gx.fill(x - t2 / 2, y + gap - o, t2, len + 2 * o, col);
         }

         Gx.circle(x, y, Math.max(1, this.S(0.8F)), Style.accent);
      }
   }

   private static final class Target {
      float x;
      float y;
      float age;
      float life;

      Target(float x, float y, float life) {
         this.x = x;
         this.y = y;
         this.life = life;
      }

      float radius() {
         float grow = 0.14F;
         return this.age < grow ? 18.0F * Math.max(0.0F, Ease.outBack(this.age / grow)) : 18.0F * Math.max(0.0F, 1.0F - (this.age - grow) / (this.life - grow));
      }
   }
}
