package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Runner extends GameView {
   private static final float W = 400.0F;
   private static final float H = 240.0F;
   private static final float GY = 196.0F;
   private static final float PX = 56.0F;
   private static final float GRAVITY = 1650.0F;
   private static final float JUMP_V = -470.0F;
   private static final float CUT_V = -170.0F;
   private float py;
   private float vy;
   private float runPhase;
   private float duckAnim;
   private float squash;
   private boolean onGround;
   private boolean jumpHeld;
   private boolean duck;
   private boolean fastFall;
   private float jumpBuffer;
   private float coyote;
   private float speed;
   private float dist;
   private float nextSpawn;
   private float shake;
   private float flash;
   private float deadT;
   private float milestoneT;
   private boolean dead;
   private long lastMilestone;
   private final List<Runner.Obstacle> obstacles = new ArrayList<>();
   private final List<float[]> parts = new ArrayList<>();
   private final float[] cloudX = new float[5];
   private final float[] cloudY = new float[5];
   private final float[] cloudS = new float[5];

   public Runner() {
      super("runner");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 240.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Distanz";
   }

   @Override
   protected void reset() {
      this.py = 196.0F;
      this.vy = 0.0F;
      this.onGround = true;
      this.jumpHeld = this.duck = this.fastFall = this.dead = false;
      this.jumpBuffer = this.coyote = 0.0F;
      this.runPhase = this.duckAnim = this.squash = 0.0F;
      this.speed = 165.0F;
      this.dist = 0.0F;
      this.nextSpawn = 260.0F;
      this.shake = this.flash = this.deadT = this.milestoneT = 0.0F;
      this.lastMilestone = 0L;
      this.obstacles.clear();
      this.parts.clear();

      for (int i = 0; i < this.cloudX.length; i++) {
         this.cloudX[i] = i * (400.0F / this.cloudX.length) + this.rnd.nextFloat() * 40.0F;
         this.cloudY[i] = 22.0F + this.rnd.nextFloat() * 60.0F;
         this.cloudS[i] = 0.7F + this.rnd.nextFloat() * 0.6F;
      }
   }

   private void pressJump() {
      this.jumpHeld = true;
      this.jumpBuffer = 0.12F;
   }

   private void releaseJump() {
      this.jumpHeld = false;
      if (!this.onGround && this.vy < -170.0F) {
         this.vy = -170.0F;
      }
   }

   @Override
   protected void onKey(int k) {
      if (k == 32 || k == 265 || k == 87) {
         this.pressJump();
      }

      if (k == 264 || k == 83) {
         this.duck = true;
      }
   }

   @Override
   protected void onKeyUp(int k) {
      if (k == 32 || k == 265 || k == 87) {
         this.releaseJump();
      }

      if (k == 264 || k == 83) {
         this.duck = false;
      }
   }

   @Override
   protected void onClick(float x, float y, int button) {
      if (button == 0) {
         this.pressJump();
      }
   }

   @Override
   protected void onRelease(float x, float y, int button) {
      if (button == 0) {
         this.releaseJump();
      }
   }

   @Override
   protected void update(float dt) {
      this.shake = Math.max(0.0F, this.shake - dt * 2.2F);
      this.flash = Math.max(0.0F, this.flash - dt * 2.5F);
      this.milestoneT = Math.max(0.0F, this.milestoneT - dt);
      this.squash = Math.max(0.0F, this.squash - dt * 5.0F);
      this.updateParticles(dt);
      if (this.dead) {
         this.deadT += dt;
         if (this.deadT > 0.75F) {
            this.gameOver();
         }
      } else {
         this.speed = Math.min(430.0F, this.speed + dt * 4.2F);
         float move = this.speed * dt;
         this.dist += move;
         this.score = (long)(this.dist / 10.0F);
         if (this.score / 100L > this.lastMilestone) {
            this.lastMilestone = this.score / 100L;
            this.milestoneT = 1.2F;
            Sound.play("minecraft:block.note_block.pling", 1.5F, 0.3F);
         }

         this.runPhase = this.runPhase + dt * (8.0F + this.speed / 28.0F);

         for (int i = 0; i < this.cloudX.length; i++) {
            this.cloudX[i] = this.cloudX[i] - move * 0.12F * this.cloudS[i];
            if (this.cloudX[i] < -70.0F) {
               this.cloudX[i] = 420.0F + this.rnd.nextFloat() * 80.0F;
               this.cloudY[i] = 22.0F + this.rnd.nextFloat() * 60.0F;
               this.cloudS[i] = 0.7F + this.rnd.nextFloat() * 0.6F;
            }
         }

         this.jumpBuffer = Math.max(0.0F, this.jumpBuffer - dt);
         this.coyote = this.onGround ? 0.08F : Math.max(0.0F, this.coyote - dt);
         if (this.jumpBuffer > 0.0F && (this.onGround || this.coyote > 0.0F)) {
            this.vy = -470.0F;
            this.onGround = false;
            this.coyote = 0.0F;
            this.jumpBuffer = 0.0F;
            this.squash = 0.6F;
            this.dust(64.0F, 196.0F, 5);
            Sound.play("minecraft:entity.chicken.egg", 1.6F + this.rnd.nextFloat() * 0.2F, 0.18F);
            if (!this.jumpHeld) {
               this.vy = -272.0F;
            }
         }

         if (!this.onGround) {
            this.fastFall = this.duck;
            this.vy = this.vy + 1650.0F * (this.fastFall ? 2.6F : 1.0F) * dt;
            this.py = this.py + this.vy * dt;
            if (this.py >= 196.0F) {
               this.py = 196.0F;
               this.vy = 0.0F;
               this.onGround = true;
               this.squash = 1.0F;
               this.dust(64.0F, 196.0F, 7);
            }
         }

         this.duckAnim = this.duckAnim + ((this.duck && this.onGround ? 1 : 0) - this.duckAnim) * Math.min(1.0F, dt * 22.0F);
         if (this.onGround && this.rnd.nextFloat() < 0.18F) {
            this.dust(59.0F, 196.0F, 1);
         }

         for (Runner.Obstacle o : this.obstacles) {
            o.x = o.x - move * (o.bird ? 1.12F : 1.0F);
            o.phase += dt * 9.0F;
         }

         this.obstacles.removeIf(ox -> ox.x + ox.w < -30.0F);
         this.nextSpawn -= move;
         if (this.nextSpawn <= 0.0F) {
            this.spawn();
         }

         float[] hb = this.playerBox();

         for (Runner.Obstacle o : this.obstacles) {
            float ix = o.bird ? 4.0F : 2.5F;
            float iy = o.bird ? 3.0F : 2.5F;
            if (hb[0] < o.x + o.w - ix && hb[0] + hb[2] > o.x + ix && hb[1] < o.y + o.h - iy && hb[1] + hb[3] > o.y + iy) {
               this.die();
               return;
            }
         }
      }
   }

   private void die() {
      this.dead = true;
      this.deadT = 0.0F;
      this.shake = 1.0F;
      this.flash = 1.0F;
      Sound.play("minecraft:entity.player.hurt", 1.0F, 0.35F);
      Sound.play("minecraft:block.stone.break", 0.9F, 0.3F);
      float cx = 64.0F;
      float cy = this.py - 15.0F;
      int[] cs = new int[]{Style.accent, -13053, -13931303, -1};

      for (int i = 0; i < 22; i++) {
         float a = (float)(this.rnd.nextFloat() * Math.PI * 2.0);
         float s = 60.0F + this.rnd.nextFloat() * 160.0F;
         this.parts
            .add(
               new float[]{
                  cx,
                  cy,
                  (float)Math.cos(a) * s,
                  (float)Math.sin(a) * s - 80.0F,
                  0.0F,
                  0.7F + this.rnd.nextFloat() * 0.5F,
                  2.0F + this.rnd.nextFloat() * 3.0F,
                  cs[this.rnd.nextInt(cs.length)]
               }
            );
      }
   }

   private void dust(float x, float y, int n) {
      int c = Style.light ? -6647936 : -4607322;

      for (int i = 0; i < n; i++) {
         this.parts
            .add(
               new float[]{
                  x + this.rnd.nextFloat() * 8.0F - 4.0F,
                  y - 1.0F,
                  -this.speed * 0.3F - this.rnd.nextFloat() * 40.0F,
                  -20.0F - this.rnd.nextFloat() * 40.0F,
                  0.0F,
                  0.35F + this.rnd.nextFloat() * 0.25F,
                  1.2F + this.rnd.nextFloat() * 1.6F,
                  c
               }
            );
      }
   }

   private void updateParticles(float dt) {
      for (float[] p : this.parts) {
         p[0] += p[2] * dt;
         p[1] += p[3] * dt;
         p[3] += 500.0F * dt;
         p[4] += dt;
         if (p[1] > 199.0F && p[3] > 0.0F) {
            p[1] = 199.0F;
            p[3] *= -0.35F;
            p[2] *= 0.7F;
         }
      }

      this.parts.removeIf(px -> px[4] >= px[5]);
   }

   private void spawn() {
      Runner.Obstacle o = new Runner.Obstacle();
      boolean birds = this.score > 220L;
      if (birds && this.rnd.nextFloat() < 0.3F) {
         o.bird = true;
         o.w = 22.0F;
         o.h = 13.0F;
         boolean high = this.rnd.nextBoolean();
         o.y = high ? 159.0F : 175.0F;
         o.x = 410.0F;
         o.phase = this.rnd.nextFloat() * 6.0F;
      } else {
         int columns = 1 + this.rnd.nextInt(this.score > 120L ? 3 : 2);
         o.colW = 11 + this.rnd.nextInt(2) * 3;
         o.cols = new int[columns];
         int maxH = 1;

         for (int i = 0; i < columns; i++) {
            o.cols[i] = 1 + this.rnd.nextInt(this.score > 60L ? 4 : 3);
            maxH = Math.max(maxH, o.cols[i]);
         }

         o.w = columns * o.colW;
         o.h = maxH * 10;
         o.y = 196.0F - o.h;
         o.x = 410.0F;
      }

      this.obstacles.add(o);
      float min = 150.0F + this.speed * 0.55F;
      this.nextSpawn = min + this.rnd.nextFloat() * (140.0F + this.speed * 0.3F);
      if (this.rnd.nextFloat() < 0.12F && !o.bird) {
         this.nextSpawn = Math.max(34.0F, o.w + 22.0F);
      }
   }

   private float[] playerBox() {
      return this.duckAnim > 0.5F ? new float[]{53.0F, this.py - 17.0F, 24.0F, 16.0F} : new float[]{58.0F, this.py - 30.0F, 13.0F, 29.0F};
   }

   @Override
   protected void render() {
      float t = this.time;
      float sx = 0.0F;
      float sy = 0.0F;
      if (this.shake > 0.0F) {
         float s = this.shake * this.shake * 5.0F;
         sx = (float)Math.sin(t * 91.0F) * s;
         sy = (float)Math.cos(t * 77.0F) * s;
      }

      Gx.push();
      Gx.translate(this.S(sx), this.S(sy));
      int skyTop = Style.light ? -4204046 : -15590869;
      int skyBot = Style.light ? -726824 : -14475213;
      int g1 = Style.light ? -3557478 : -14014943;
      int g2 = Style.light ? -5202818 : -15067372;
      this.boxV(0.0F, 182.0F, 400.0F, 58.0F, 12.0F, g1, g2);
      this.boxV(0.0F, 0.0F, 400.0F, 196.0F, 12.0F, skyTop, skyBot);
      this.box(0.0F, 182.0F, 400.0F, 15.0F, 0.0F, skyBot);
      Gx.glow(this.X(330.0F), this.Y(52.0F), this.S(70.0F), Gx.withAlpha(Style.light ? -6496 : Style.accent, Style.light ? 0.7F : 0.22F));
      this.circle(330.0F, 52.0F, 13.0F, Style.light ? -8054 : Gx.mix(Style.accent, -724506, 0.85F));
      if (!Style.light) {
         this.circle(326.0F, 49.0F, 2.5F, 402653184);
         this.circle(335.0F, 57.0F, 1.8F, 402653184);
      }

      for (int i = 0; i < this.cloudX.length; i++) {
         this.cloud(this.cloudX[i], this.cloudY[i], this.cloudS[i]);
      }

      this.mountains(this.dist * 0.08F, 150.0F, 0.6F, Style.light ? -4864291 : -14735816);
      this.mountains(this.dist * 0.2F + 97.0F, 110.0F, 0.85F, Style.light ? -7033910 : -13945774);
      this.box(0.0F, 194.8F, 400.0F, 2.4F, 0.0F, Style.light ? -8425899 : -9608620);
      float off = this.dist % 26.0F;

      for (float x = -off; x < 426.0F; x += 26.0F) {
         this.box(x, 203.0F, 12.0F, 2.0F, 1.0F, Style.light ? 1073741824 : 822083583);
      }

      float off2 = this.dist * 1.0F % 61.0F;

      for (float x = -off2; x < 461.0F; x += 61.0F) {
         this.box(x + 20.0F, 213.0F, 5.0F, 2.0F, 1.0F, Style.light ? 805306368 : 587202559);
         this.box(x + 44.0F, 225.0F, 8.0F, 2.0F, 1.0F, Style.light ? 637534208 : 452984831);
      }

      for (Runner.Obstacle o : this.obstacles) {
         if (o.bird) {
            this.bird(o);
         } else {
            this.cactus(o);
         }
      }

      if (this.dead && !(this.deadT < 0.08F)) {
         float a = Math.max(0.0F, 1.0F - this.deadT * 3.0F);
         if (a > 0.0F) {
            Gx.pushAlpha(a);
            this.player();
            Gx.popAlpha();
         }
      } else {
         this.player();
      }

      for (float[] p : this.parts) {
         float a = 1.0F - p[4] / p[5];
         this.box(p[0] - p[6] / 2.0F, p[1] - p[6] / 2.0F, p[6], p[6], p[6] * 0.25F, Gx.withAlpha((int)p[7], a));
      }

      Gx.pop();
      String sc = String.format("%05d", this.score);
      boolean ms = this.milestoneT > 0.0F && (int)(this.milestoneT * 8.0F) % 2 == 0;
      int hud = ms ? Style.accent : (Style.light ? -12959926 : -1644306);
      this.box(314.0F, 8.0F, 78.0F, 22.0F, 11.0F, Style.light ? 1728053247 : 1426063360);
      Gx.icon("sprint", this.X(327.0F), this.Y(19.0F), this.S(10.0F), Gx.withAlpha(Style.accent, 0.9F));
      Gx.textMid(sc, this.X(338.0F), this.Y(19.0F), this.S(10.0F), 3, hud);
      if (GameInfo.hasBest(this.info.id)) {
         String hi = "HI " + String.format("%05d", GameInfo.best(this.info.id));
         Gx.textRight(hi, this.X(308.0F), this.Y(19.0F), this.S(7.0F), 2, Style.light ? -1728053248 : -1711276033);
      }

      float mult = this.speed / 165.0F;
      this.box(8.0F, 8.0F, 50.0F, 22.0F, 11.0F, Style.light ? 1728053247 : 1426063360);
      Gx.icon("speed", this.X(20.0F), this.Y(19.0F), this.S(10.0F), Gx.withAlpha(Style.accent, 0.9F));
      Gx.textMid(String.format(Locale.ROOT, "x%.1f", mult), this.X(28.0F), this.Y(19.0F), this.S(8.0F), 3, Style.light ? -12959926 : -1644306);
      if (this.milestoneT > 0.0F) {
         float k = Math.min(1.0F, (1.2F - this.milestoneT) / 0.25F);
         float a = Math.min(1.0F, this.milestoneT / 0.3F);
         this.text(this.lastMilestone * 100L + "!", 200.0F, 40.0F - 6.0F * k, 14.0F, 3, Gx.withAlpha(Style.accent, a));
      }

      if (this.flash > 0.0F) {
         this.box(-10.0F, -10.0F, 420.0F, 260.0F, 0.0F, Gx.withAlpha(-1, this.flash * this.flash * 0.75F));
      }

      if (this.dead) {
         this.box(-10.0F, -10.0F, 420.0F, 260.0F, 0.0F, Gx.withAlpha(-57296, Math.min(0.18F, this.deadT * 0.4F)));
      }
   }

   private void cloud(float x, float y, float s) {
      int c = Style.light ? -855638017 : 587202559;
      this.box(x, y, 46.0F * s, 12.0F * s, 6.0F * s, c);
      this.box(x + 9.0F * s, y - 7.0F * s, 22.0F * s, 12.0F * s, 6.0F * s, c);
      this.box(x + 22.0F * s, y - 4.0F * s, 16.0F * s, 10.0F * s, 5.0F * s, c);
   }

   private void mountains(float scroll, float period, float scale, int col) {
      float off = scroll % (period * 2.0F);

      for (float x = -off - period; x < 400.0F + period * 2.0F; x += period) {
         int seed = (int)Math.floor((x + scroll) / period + 0.5F);
         float hgt = (40 + (seed * 37 & 31)) * scale;
         int steps = 5;
         float stepH = hgt / steps;

         for (int i = 0; i < steps; i++) {
            float w = period * 0.9F * (1.0F - (float)i / steps);
            float bx0 = x + (period * 0.9F - w) / 2.0F;
            float yy = 196.0F - (i + 1) * stepH;
            this.box(bx0, yy, w, stepH + 1.0F, 1.5F, Gx.mix(col, Style.light ? -1 : -16777216, i * 0.03F));
            this.box(bx0, yy, w, 1.2F, 0.6F, Gx.withAlpha(-1, Style.light ? 0.35F : 0.06F));
            if (i == steps - 1) {
               float sw0 = Math.min(8.0F, w * 0.4F);
               this.box(bx0 + w / 2.0F - sw0 / 2.0F, yy - 2.4F, sw0, 3.0F, 1.2F, Gx.mix(col, -1, 0.08F));
            }
         }
      }
   }

   private void cactus(Runner.Obstacle o) {
      int base = -13780916;

      for (int c = 0; c < o.cols.length; c++) {
         float cx = o.x + c * o.colW;

         for (int i = 0; i < o.cols[c]; i++) {
            int col = Gx.mix(base, (i + c) % 2 == 0 ? -14774470 : -11874462, 0.35F);
            this.brick(cx + 0.3F, 196.0F - (i + 1) * 10, o.colW - 0.6F, 10.0F, col);
         }
      }

      this.box(o.x - 2.0F, 195.5F, o.w + 4.0F, 3.0F, 1.5F, 855638016);
   }

   private void bird(Runner.Obstacle o) {
      float x = o.x;
      float y = o.y;
      float flap = (float)Math.sin(o.phase);
      int body = -1900533;
      int wing = -5242870;
      this.box(x + 4.0F, 195.5F, 14.0F, 2.5F, 1.2F, 570425344);
      float wy = flap * 6.0F;
      this.box(x + 8.0F, y + 3.0F - Math.max(0.0F, wy), 8.0F, Math.abs(wy) + 3.0F, 1.5F, Gx.mix(wing, -16777216, 0.2F));
      this.boxV(x + 3.0F, y + 3.0F, 17.0F, 8.0F, 3.0F, Gx.mix(body, -1, 0.2F), body);
      this.boxV(x, y + 1.0F, 8.0F, 7.0F, 2.5F, Gx.mix(body, -1, 0.2F), body);
      this.box(x + 1.5F, y - 0.8F, 4.0F, 2.0F, 0.8F, Gx.mix(body, -1, 0.3F));
      this.box(x - 4.0F, y + 4.0F, 5.0F, 2.6F, 1.0F, -13053);
      this.circle(x + 2.6F, y + 3.6F, 1.1F, -1);
      this.circle(x + 2.3F, y + 3.6F, 0.55F, -15658735);
      this.box(x + 19.0F, y + 3.0F, 4.0F, 3.0F, 1.0F, wing);
      float fy = -flap * 7.0F;
      if (fy < 0.0F) {
         this.box(x + 7.0F, y + 5.0F + fy, 9.0F, -fy + 2.0F, 1.5F, wing);
      } else {
         this.box(x + 7.0F, y + 6.0F, 9.0F, fy + 2.0F, 1.5F, wing);
      }
   }

   private void player() {
      float x = 56.0F;
      float foot = this.py;
      int skin = -13053;
      int shirt = Style.accent;
      int pants = -13931303;
      int shirtD = Gx.mix(shirt, -16777216, 0.25F);
      int pantsD = Gx.mix(pants, -16777216, 0.3F);
      float st = this.squash * (this.onGround ? 1.0F : -0.6F);
      float air = Math.max(0.0F, 196.0F - foot);
      float sw = Math.max(6.0F, 18.0F - air * 0.12F);
      this.box(x + 8.0F - sw / 2.0F, 195.0F, sw, 3.0F, 1.5F, Gx.withAlpha(-16777216, 0.28F * Math.max(0.2F, 1.0F - air / 90.0F)));
      Gx.push();
      float scY = 1.0F - st * 0.12F;
      Gx.translate(0.0F, 0.0F);
      if (this.duckAnim > 0.02F) {
         float d = this.duckAnim;
         float lp = (float)Math.sin(this.runPhase);
         this.box(x - 2.0F + lp * 2.0F, foot - 5.0F, 7.0F, 5.0F, 1.5F, pantsD);
         this.box(x + 6.0F - lp * 2.0F, foot - 5.0F, 7.0F, 5.0F, 1.5F, pants);
         float ty = foot - 5.0F - 9.0F * d - (1.0F - d) * 14.0F;
         this.boxV(x - 4.0F, ty, 20.0F, 9.0F, 2.5F, Gx.mix(shirt, -1, 0.15F), shirtD);
         this.box(x + 10.0F, ty + 5.0F, 8.0F, 3.0F, 1.5F, skin);
         this.boxV(x + 14.0F, ty - 3.0F, 9.0F, 9.0F, 3.0F, Gx.mix(skin, -1, 0.2F), skin);
         this.box(x + 16.0F, ty - 5.0F, 5.0F, 2.5F, 1.0F, Gx.mix(skin, -1, 0.25F));
         this.circle(x + 20.5F, ty + 0.8F, 0.9F, -15066598);
         this.box(x + 19.0F, ty + 3.2F, 3.0F, 0.9F, 0.4F, -15066598);
      } else {
         float h = 30.0F * scY;
         float k = h / 30.0F;
         float lp = this.onGround ? (float)Math.sin(this.runPhase) : 0.6F;
         float lift1 = this.onGround ? Math.max(0.0F, lp) * 3.0F : 2.0F;
         float lift2 = this.onGround ? Math.max(0.0F, -lp) * 3.0F : 0.0F;
         float legTop = foot - 10.0F * k;
         this.box(x + 2.0F + lp * 3.0F, legTop, 6.0F, 10.0F * k - lift1, 1.5F, pantsD);
         this.box(x + 8.0F - lp * 3.0F, legTop, 6.0F, 10.0F * k - lift2, 1.5F, pants);
         this.box(x + 1.5F, legTop - 1.0F, 13.0F, 3.0F, 1.0F, pants);
         float ap = this.onGround ? -lp : -1.0F;
         this.box(x + 5.0F + ap * 3.0F, legTop - 11.0F * k, 4.0F, 9.0F * k, 2.0F, Gx.mix(shirtD, -16777216, 0.15F));
         float tt = legTop - 11.0F * k;
         this.boxV(x + 1.0F, tt, 15.0F, 11.0F * k, 2.5F, Gx.mix(shirt, -1, 0.18F), shirtD);
         this.box(x + 3.0F, tt + 2.0F, 3.0F, 3.0F, 1.0F, Gx.withAlpha(-1, 0.35F));
         this.box(x + 8.0F - ap * 3.0F, tt + 1.0F, 4.0F, 8.0F * k, 2.0F, shirt);
         this.circle(x + 10.0F - ap * 3.0F, tt + 1.0F + 8.0F * k, 2.0F, skin);
         float ht = tt - 9.0F * k;
         this.box(x + 6.0F, ht + 7.0F * k, 5.0F, 2.0F, 0.8F, skin);
         this.boxV(x + 3.5F, ht, 10.0F, 8.0F * k, 3.0F, Gx.mix(skin, -1, 0.22F), skin);
         this.box(x + 5.5F, ht - 2.2F, 6.0F, 2.6F, 1.0F, Gx.mix(skin, -1, 0.28F));
         this.circle(x + 11.2F, ht + 3.0F, 0.9F, -15066598);
         this.box(x + 9.6F, ht + 5.4F, 3.0F, 0.9F, 0.4F, -15066598);
      }

      Gx.pop();
   }

   private static final class Obstacle {
      float x;
      float y;
      float w;
      float h;
      boolean bird;
      int[] cols;
      float colW;
      float phase;
   }
}
