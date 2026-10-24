package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.RoundRectangle2D.Float;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class Flappy extends GameView {
   private static final float W = 360.0F;
   private static final float H = 280.0F;
   private static final float GROUND = 254.0F;
   private static final float BIRD_X = 88.0F;
   private static final float BW = 24.0F;
   private static final float BH = 17.0F;
   private static final float PW = 40.0F;
   private static final float ROW = 12.0F;
   private static final float SPACING = 165.0F;
   private static final float GRAVITY = 980.0F;
   private static final float FLAP = -268.0F;
   private static final float MAX_FALL = 420.0F;
   private static final int[] COLORS = new int[]{-3597815, -16755265, -11821238, -95720, -7194248, -13193537};
   private static final int BIRD = -864969;
   private final List<Flappy.Pillar> pillars = new ArrayList<>();
   private final List<float[]> puffs = new ArrayList<>();
   private final float[][] clouds = new float[7][3];
   private float y;
   private float vy;
   private float scroll;
   private float wing;
   private float flash;
   private float anim;
   private float scorePop;
   private boolean flying;
   private int colorIdx;

   public Flappy() {
      super("flappy");
      Random r = new Random(7L);

      for (int i = 0; i < this.clouds.length; i++) {
         this.clouds[i][0] = i * 480.0F / this.clouds.length + r.nextFloat() * 30.0F;
         this.clouds[i][1] = 22.0F + r.nextFloat() * 80.0F;
         this.clouds[i][2] = 0.7F + r.nextFloat() * 0.6F;
      }
   }

   @Override
   protected float boardW() {
      return 360.0F;
   }

   @Override
   protected float boardH() {
      return 280.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Säulen";
   }

   @Override
   protected void reset() {
      this.pillars.clear();
      this.puffs.clear();
      this.y = 117.6F;
      this.vy = 0.0F;
      this.wing = 0.0F;
      this.flash = 0.0F;
      this.scorePop = 0.0F;
      this.flying = false;
      this.colorIdx = this.rnd.nextInt(COLORS.length);
   }

   private void flap() {
      if (!this.flying) {
         this.flying = true;
         this.addPillar(420.0F);
      }

      this.vy = -268.0F;
      this.wing = 1.0F;

      for (int i = 0; i < 4; i++) {
         this.puffs.add(new float[]{78.0F, this.y + 4.0F, -40.0F - this.rnd.nextFloat() * 50.0F, 10.0F + this.rnd.nextFloat() * 40.0F - 20.0F, 0.0F});
      }

      Sound.play("minecraft:entity.bat.takeoff", 1.6F + this.rnd.nextFloat() * 0.2F, 0.12F);
   }

   private void addPillar(float x) {
      Flappy.Pillar p = new Flappy.Pillar();
      p.x = x;
      p.gap = Math.max(70.0F, 90.0F - (float)this.score * 0.8F);
      float min = 34.0F + p.gap / 2.0F;
      float max = 224.0F - p.gap / 2.0F;
      p.gapY = min + this.rnd.nextFloat() * (max - min);
      p.color = COLORS[this.colorIdx++ % COLORS.length];
      this.pillars.add(p);
   }

   @Override
   protected void onKey(int k) {
      if (k == 32 || k == 265 || k == 87) {
         this.flap();
      }
   }

   @Override
   protected void onClick(float x, float yy, int button) {
      if (button == 0) {
         this.flap();
      }
   }

   private float speed() {
      return 112.0F + Math.min(40.0F, (float)this.score * 1.2F);
   }

   @Override
   protected void update(float dt) {
      float sp = this.speed();
      this.scroll += sp * dt;
      this.wing = Math.max(0.0F, this.wing - dt * 5.0F);
      this.scorePop = Math.max(0.0F, this.scorePop - dt * 3.0F);

      for (float[] p : this.puffs) {
         p[0] += p[2] * dt;
         p[1] += p[3] * dt;
         p[4] += dt;
      }

      this.puffs.removeIf(px -> px[4] > 0.45F);
      if (!this.flying) {
         this.y = 117.6F + (float)Math.sin(this.time * 5.0F) * 5.0F;
      } else {
         this.vy = Math.min(420.0F, this.vy + 980.0F * dt);
         this.y = this.y + this.vy * dt;
         if (this.y < 8.5F) {
            this.y = 8.5F;
            this.vy = Math.max(this.vy, 0.0F);
         }

         for (Flappy.Pillar p : this.pillars) {
            p.x -= sp * dt;
         }

         this.pillars.removeIf(px -> px.x < -60.0F);
         Flappy.Pillar last = this.pillars.isEmpty() ? null : this.pillars.get(this.pillars.size() - 1);
         if (last == null || last.x < 215.0F) {
            this.addPillar(last == null ? 380.0F : last.x + 165.0F);
         }

         float l = 79.0F;
         float r = 98.0F;
         float t = this.y - 8.5F + 3.0F;
         float b = this.y + 8.5F - 2.0F;

         for (Flappy.Pillar p : this.pillars) {
            if (!p.passed && p.x + 40.0F < 76.0F) {
               p.passed = true;
               this.score++;
               this.scorePop = 1.0F;
               Sound.play("minecraft:entity.experience_orb.pickup", 1.3F + this.rnd.nextFloat() * 0.2F, 0.25F);
            }

            float cl = p.x - 3.0F;
            float cr = p.x + 40.0F + 3.0F;
            if (r > cl && l < cr && (t < p.gapY - p.gap / 2.0F || b > p.gapY + p.gap / 2.0F)) {
               this.crash();
               return;
            }
         }

         if (b >= 254.0F) {
            this.y = 247.5F;
            this.crash();
         }
      }
   }

   private void crash() {
      this.flash = 1.0F;
      Sound.play("minecraft:entity.arrow.hit_player", 0.8F, 0.4F);
      this.gameOver();
   }

   @Override
   protected void render() {
      float dt = Math.min(0.05F, Gx.dt);
      this.anim += dt;
      this.flash = Math.max(0.0F, this.flash - dt * 2.5F);
      if (this.state == GameView.State.READY) {
         this.y = 117.6F + (float)Math.sin(this.anim * 4.0F) * 5.0F;
      }

      this.boxV(0.0F, 0.0F, 360.0F, 266.0F, 12.0F, -14001261, -7091736);
      Gx.glow(this.X(290.0F), this.Y(52.0F), this.S(90.0F), 1442837444);
      this.circle(290.0F, 52.0F, 17.0F, -3656);
      this.circle(290.0F, 52.0F, 14.0F, -1828);
      float wrap = 480.0F;

      for (float[] c : this.clouds) {
         float cx = ((c[0] - this.scroll * 0.12F) % wrap + wrap) % wrap - 60.0F;
         this.cloud(cx, c[1], c[2]);
      }

      this.hills(0.22F, 150.0F, 70.0F, 228.0F, -9459808, 1);
      this.hills(0.45F, 120.0F, 52.0F, 248.0F, -12612518, 2);

      for (Flappy.Pillar p : this.pillars) {
         this.pillar(p);
      }

      this.box(0.0F, 254.0F, 360.0F, 26.0F, 12.0F, -9745874);
      this.boxV(0.0F, 254.0F, 360.0F, 12.0F, 0.0F, -13723058, -14714310);
      this.box(0.0F, 254.0F, 360.0F, 2.0F, 0.0F, -10763146);
      float so = this.scroll % 12.0F;

      for (float sx = -so; sx < 372.0F; sx += 12.0F) {
         this.boxV(sx + 2.5F, 250.5F, 7.0F, 4.5F, 1.2F, -10303106, -13723058);
      }

      for (float sx = -(this.scroll % 24.0F); sx < 384.0F; sx += 24.0F) {
         if (sx > 6.0F && sx < 354.0F) {
            this.box(sx, 266.0F, 1.2F, 10.0F, 0.0F, 855638016);
         }
      }

      this.box(0.0F, 266.0F, 360.0F, 1.5F, 0.0F, 1140850688);

      for (float[] p : this.puffs) {
         float a = 1.0F - p[4] / 0.45F;
         this.circle(p[0], p[1], 2.0F + p[4] * 8.0F, Gx.withAlpha(-1, a * 0.6F));
      }

      this.bird();
      if (this.flying || this.state == GameView.State.OVER) {
         float s = 1.0F + 0.25F * Ease.outCubic(this.scorePop);
         Gx.push();
         Gx.scaleAt(this.X(180.0F), this.Y(34.0F), s);
         String sc = String.valueOf(this.score);

         for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4.0;
            this.text(sc, 180.0F + (float)Math.cos(a) * 1.4F, 34.0F + (float)Math.sin(a) * 1.4F + 0.6F, 26.0F, 3, -1441060294);
         }

         this.text(sc, 180.0F, 34.0F, 26.0F, 3, -1);
         Gx.pop();
      } else if (this.state == GameView.State.PLAYING) {
         float a = 0.6F + 0.4F * (float)Math.sin(this.anim * 5.0F);
         this.box(102.0F, 161.6F, 156.0F, 24.0F, 12.0F, 1426063360);
         this.text("Leertaste oder Klick zum Flattern", 180.0F, 173.6F, 7.6F, 3, Gx.withAlpha(-1, a));
      }

      if (this.flash > 0.0F) {
         this.box(0.0F, 0.0F, 360.0F, 280.0F, 0.0F, Gx.withAlpha(-1, this.flash * 0.55F));
      }
   }

   private void cloud(float x, float y, float s) {
      int c = -1116934;
      this.circle(x, y, 11.0F * s, c);
      this.circle(x + 13.0F * s, y - 5.0F * s, 14.0F * s, c);
      this.circle(x + 28.0F * s, y, 11.0F * s, c);
      this.box(x, y - 1.0F * s, 28.0F * s, 11.0F * s, 5.0F * s, c);
   }

   private void hills(float par, float spacing, float radius, float baseY, int col, int seed) {
      float off = this.scroll * par % spacing;

      for (float hx = -off - spacing; hx < 360.0F + spacing; hx += spacing) {
         int idx = Math.floorMod(Math.round((hx + this.scroll * par) / spacing) * 31 + seed * 7, 5);
         float r = radius * (0.8F + idx * 0.1F);
         this.circle(hx + spacing / 2.0F, baseY + r * 0.55F, r, col);
         if (seed == 2) {
            float top = baseY + r * 0.55F - r;
            this.box(hx + spacing / 2.0F - 9.0F, top - 2.5F, 7.0F, 3.5F, 1.0F, Gx.mix(col, -1, 0.12F));
            this.box(hx + spacing / 2.0F + 2.0F, top - 2.5F, 7.0F, 3.5F, 1.0F, Gx.mix(col, -1, 0.12F));
         }
      }

      this.box(0.0F, baseY, 360.0F, 254.0F - baseY + 1.0F, 0.0F, col);
   }

   private void sideBrick(float x, float yy, float w, float h, int col, int studs) {
      if (studs > 0) {
         float sw = 7.0F;
         float gap = (w - studs * sw) / studs;

         for (int i = 0; i < studs; i++) {
            float sx = x + gap / 2.0F + i * (sw + gap);
            this.boxV(sx, yy - 3.0F, sw, 4.0F, 1.2F, Gx.mix(col, -1, 0.25F), col);
         }
      }

      this.boxV(x, yy, w, h, 2.0F, Gx.mix(col, -1, 0.14F), Gx.mix(col, -16777216, 0.16F));
      this.box(x + 1.5F, yy + 1.0F, w - 3.0F, 1.2F, 0.6F, Gx.withAlpha(-1, 0.28F));
      Gx.outline(this.X(x), this.Y(yy), this.S(w), this.S(h), this.S(2.0F), Math.max(1, this.S(0.6F)), Gx.withAlpha(Gx.mix(col, -16777216, 0.55F), 0.7F));
   }

   private void pillar(Flappy.Pillar p) {
      float top = p.gapY - p.gap / 2.0F;
      float bot = p.gapY + p.gap / 2.0F;
      int col = p.color;
      int dark = Gx.mix(col, -16777216, 0.12F);
      Gx.shadow(this.X(p.x), this.Y(-10.0F), this.S(40.0F), this.S(top + 10.0F), this.S(2.0F), this.S(4.0F), 0.25F);
      Gx.shadow(this.X(p.x), this.Y(bot), this.S(40.0F), this.S(254.0F - bot), this.S(2.0F), this.S(4.0F), 0.25F);
      int i = 0;

      for (float yy = top - 24.0F; yy > -12.0F; i++) {
         this.sideBrick(p.x + (i % 2 == 0 ? 0 : 0), yy, 40.0F, 12.0F, i % 2 == 0 ? col : dark, 0);
         yy -= 12.0F;
      }

      this.sideBrick(p.x - 3.0F, top - 12.0F, 46.0F, 12.0F, Gx.mix(col, -1, 0.08F), 0);
      i = 0;

      for (float yy = bot + 12.0F; yy < 254.0F; i++) {
         this.sideBrick(p.x, yy, 40.0F, 12.0F, i % 2 == 0 ? dark : col, 0);
         yy += 12.0F;
      }

      this.sideBrick(p.x - 3.0F, bot, 46.0F, 12.0F, Gx.mix(col, -1, 0.08F), 3);
   }

   private void bird() {
      float ang = !this.flying && this.state != GameView.State.OVER
         ? (float)Math.sin(this.anim * 4.0F) * 4.0F
         : Math.max(-24.0F, Math.min(55.0F, this.vy / 420.0F * 60.0F));
      int q = Math.round(ang / 3.0F) * 3;
      int px = Math.max(8, this.S(40.0F));
      double rad = Math.toRadians(q);
      Gx.glow(this.X(88.0F), this.Y(this.y), this.S(26.0F), 587199392);
      Gx.Img img = Gx.painted("flappyBird" + q, px, px, g -> paintBird(g, px, rad));
      Gx.image(img, this.X(88.0F) - px / 2, this.Y(this.y) - px / 2, px, px, -1);
      float wx = -4.0F;
      float wy = 2.0F + (this.wing > 0.0F ? -4.0F * this.wing : 0.0F);
      float cos = (float)Math.cos(rad);
      float sin = (float)Math.sin(rad);
      float rx = 88.0F + wx * cos - wy * sin;
      float ry = this.y + wx * sin + wy * cos;
      float wh = 4.0F + 3.0F * this.wing;
      this.boxV(rx - 5.0F, ry - wh / 2.0F, 10.0F, wh, 2.0F, -7558, -2053860);
      Gx.outline(this.X(rx - 5.0F), this.Y(ry - wh / 2.0F), this.S(10.0F), this.S(wh), this.S(2.0F), Math.max(1, this.S(0.5F)), -2005905664);
   }

   private static void paintBird(Graphics2D g, int px, double rad) {
      float k = px / 40.0F;
      g.translate(px / 2.0, px / 2.0);
      g.rotate(rad);
      g.scale(k, k);
      Color base = new Color(-864969, true);
      Color light = new Color(Gx.mix(-864969, -1, 0.3F), true);
      Color dark = new Color(Gx.mix(-864969, -16777216, 0.2F), true);
      Color line = new Color(Gx.mix(-864969, -16777216, 0.55F), true);
      g.setStroke(new BasicStroke(0.8F));

      for (int i = 0; i < 2; i++) {
         RoundRectangle2D st = new Float(-9.5F + i * 11, -11.7F, 8.0F, 4.2F, 2.0F, 2.0F);
         g.setPaint(new GradientPaint(0.0F, -11.5F, light, 0.0F, -8.5F, base));
         g.fill(st);
         g.setColor(line);
         g.draw(st);
      }

      RoundRectangle2D body = new Float(-12.0F, -8.5F, 24.0F, 17.0F, 5.0F, 5.0F);
      g.setPaint(new GradientPaint(0.0F, -8.5F, light, 0.0F, 8.5F, dark));
      g.fill(body);
      g.setColor(new Color(255, 255, 255, 80));
      g.fill(new Float(-10.0F, -7.3F, 20.0F, 1.6F, 1.6F, 1.6F));
      g.setColor(line);
      g.draw(body);
      Path2D beak = new java.awt.geom.Path2D.Float();
      beak.moveTo(11.0, -1.5);
      beak.lineTo(18.0, 1.5);
      beak.lineTo(11.0, 4.5);
      beak.closePath();
      g.setColor(new Color(16681496));
      g.fill(beak);
      g.setColor(new Color(10111488));
      g.draw(beak);
      g.setColor(Color.WHITE);
      g.fill(new java.awt.geom.Ellipse2D.Float(3.2F, -6.2F, 7.0F, 7.0F));
      g.setColor(new Color(8018432));
      g.draw(new java.awt.geom.Ellipse2D.Float(3.2F, -6.2F, 7.0F, 7.0F));
      g.setColor(new Color(1381914));
      g.fill(new java.awt.geom.Ellipse2D.Float(6.4F, -4.4F, 3.2F, 3.4F));
      g.setColor(Color.WHITE);
      g.fill(new java.awt.geom.Ellipse2D.Float(7.2F, -4.1F, 1.1F, 1.1F));
      g.setColor(new Color(255, 110, 80, 90));
      g.fill(new java.awt.geom.Ellipse2D.Float(2.5F, 1.8F, 5.0F, 3.0F));
   }

   private static final class Pillar {
      float x;
      float gapY;
      float gap;
      int color;
      boolean passed;
   }
}
