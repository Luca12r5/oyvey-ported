package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;

public final class Pong extends GameView {
   private static final float W = 380.0F;
   private static final float H = 260.0F;
   private static final float PW = 9.0F;
   private static final float PH = 48.0F;
   private static final float R = 5.0F;
   private static final float MARGIN = 14.0F;
   private static final int WIN = 7;
   private static final float BASE_SPEED = 205.0F;
   private static final float MAX_SPEED = 470.0F;
   private static final float AI_SPEED = 185.0F;
   private static final int AI_COLOR = -3597815;
   private float py;
   private float ay;
   private float bxp;
   private float byp;
   private float vx;
   private float vy;
   private float speed;
   private float serveT;
   private float aiTarget;
   private float aiThink;
   private float aiErr;
   private int pScore;
   private int aScore;
   private int hits;
   private boolean up;
   private boolean down;
   private boolean mouseMode;
   private boolean won;
   private float lastMy = -9999.0F;
   private float pHit;
   private float aHit;
   private float goalFlash;
   private float goalSide;
   private float scorePopP;
   private float scorePopA;
   private float anim;
   private final float[][] trail = new float[10][2];
   private final List<float[]> parts = new ArrayList<>();

   public Pong() {
      super("pong");
   }

   @Override
   protected boolean hasAi() {
      return true;
   }

   @Override
   protected float boardW() {
      return 380.0F;
   }

   @Override
   protected float boardH() {
      return 260.0F;
   }

   @Override
   protected String overTitle() {
      return this.won ? "Gewonnen!" : "Verloren";
   }

   @Override
   protected void reset() {
      this.py = this.ay = 130.0F;
      this.pScore = this.aScore = 0;
      this.up = this.down = false;
      this.won = false;
      this.parts.clear();
      this.pHit = this.aHit = this.goalFlash = this.scorePopP = this.scorePopA = 0.0F;
      this.aiTarget = 130.0F;
      this.serve(this.rnd.nextBoolean() ? 1 : -1);
   }

   private void serve(int dir) {
      this.bxp = 190.0F;
      this.byp = 130.0F;
      this.speed = 205.0F;
      this.hits = 0;
      this.serveT = 0.9F;
      float ang = (float)Math.toRadians(-25.0F + this.rnd.nextFloat() * 50.0F);
      this.vx = dir * (float)Math.cos(ang) * this.speed;
      this.vy = (float)Math.sin(ang) * this.speed;

      for (float[] t : this.trail) {
         t[0] = this.bxp;
         t[1] = this.byp;
      }
   }

   @Override
   protected void onKey(int k) {
      if (k == 265 || k == 87) {
         this.up = true;
         this.mouseMode = false;
      }

      if (k == 264 || k == 83) {
         this.down = true;
         this.mouseMode = false;
      }
   }

   @Override
   protected void onKeyUp(int k) {
      if (k == 265 || k == 87) {
         this.up = false;
      }

      if (k == 264 || k == 83) {
         this.down = false;
      }
   }

   @Override
   protected void update(float dt) {
      if (Math.abs(this.my - this.lastMy) > 0.5F && this.my >= 0.0F) {
         if (this.lastMy > -9999.0F) {
            this.mouseMode = true;
         }

         this.lastMy = this.my;
      }

      if (this.mouseMode && this.u > 0.0F) {
         float target = (this.my - this.by) / this.u;
         this.py = this.py + (target - this.py) * Math.min(1.0F, dt * 22.0F);
      } else {
         this.py = this.py + ((this.down ? 1 : 0) - (this.up ? 1 : 0)) * 300 * dt;
      }

      this.py = this.clampPad(this.py);
      this.aiThink -= dt;
      if (this.aiThink <= 0.0F) {
         this.aiThink = 0.1F + this.rnd.nextFloat() * 0.1F;
         if (this.vx > 0.0F && this.serveT <= 0.0F) {
            this.aiTarget = this.predict() + this.aiErr;
         } else {
            this.aiTarget = 130.0F + (this.byp - 130.0F) * 0.3F;
            this.aiErr = (this.rnd.nextFloat() - 0.5F) * (43.199997F + this.hits * 4) * new float[]{1.9F, 1.25F, 0.8F, 0.35F}[this.aiLevel()];
         }
      }

      float d = this.aiTarget - this.ay;
      float maxMove = (185.0F + Math.min(60, this.hits * 4)) * new float[]{0.62F, 0.85F, 1.05F, 1.35F}[this.aiLevel()] * dt;
      this.ay = this.ay + Math.max(-maxMove, Math.min(maxMove, d * Math.min(1.0F, dt * 10.0F) * 6.0F));
      this.ay = this.clampPad(this.ay);
      this.pHit = Math.max(0.0F, this.pHit - dt * 4.0F);
      this.aHit = Math.max(0.0F, this.aHit - dt * 4.0F);
      this.scorePopP = Math.max(0.0F, this.scorePopP - dt * 2.5F);
      this.scorePopA = Math.max(0.0F, this.scorePopA - dt * 2.5F);

      for (float[] p : this.parts) {
         p[0] += p[2] * dt;
         p[1] += p[3] * dt;
         p[2] *= 0.96F;
         p[3] *= 0.96F;
         p[4] += dt;
      }

      this.parts.removeIf(p -> p[4] > 0.5F);
      if (this.serveT > 0.0F) {
         this.serveT -= dt;

         for (float[] t : this.trail) {
            t[0] = this.bxp;
            t[1] = this.byp;
         }
      } else {
         for (int i = this.trail.length - 1; i > 0; i--) {
            this.trail[i][0] = this.trail[i - 1][0];
            this.trail[i][1] = this.trail[i - 1][1];
         }

         this.trail[0][0] = this.bxp;
         this.trail[0][1] = this.byp;
         int steps = 3;
         float h = dt / steps;

         for (int s = 0; s < steps; s++) {
            this.bxp = this.bxp + this.vx * h;
            this.byp = this.byp + this.vy * h;
            if (this.byp < 5.0F) {
               this.byp = 5.0F;
               this.vy = Math.abs(this.vy);
               Sound.play("minecraft:block.note_block.hat", 1.7F, 0.12F);
            }

            if (this.byp > 255.0F) {
               this.byp = 255.0F;
               this.vy = -Math.abs(this.vy);
               Sound.play("minecraft:block.note_block.hat", 1.7F, 0.12F);
            }

            float plx = 23.0F;
            if (this.vx < 0.0F && this.bxp - 5.0F <= plx && this.bxp - 5.0F > 8.0F && Math.abs(this.byp - this.py) <= 29.0F) {
               this.bounce(this.py, 1);
               this.bxp = plx + 5.0F;
               this.pHit = 1.0F;
            }

            float arx = 357.0F;
            if (this.vx > 0.0F && this.bxp + 5.0F >= arx && this.bxp + 5.0F < 372.0F && Math.abs(this.byp - this.ay) <= 29.0F) {
               this.bounce(this.ay, -1);
               this.bxp = arx - 5.0F;
               this.aHit = 1.0F;
            }

            if (this.bxp < -10.0F) {
               this.point(false);
               return;
            }

            if (this.bxp > 390.0F) {
               this.point(true);
               return;
            }
         }
      }
   }

   private float clampPad(float y) {
      return Math.max(27.0F, Math.min(233.0F, y));
   }

   private void bounce(float padY, int dir) {
      float off = Math.max(-1.0F, Math.min(1.0F, (this.byp - padY) / 24.0F));
      float ang = (float)Math.toRadians(off * 55.0F);
      this.hits++;
      this.speed = Math.min(470.0F, this.speed * 1.07F);
      this.vx = dir * (float)Math.cos(ang) * this.speed;
      this.vy = (float)Math.sin(ang) * this.speed;
      int col = dir > 0 ? Style.accent : -3597815;

      for (int i = 0; i < 8; i++) {
         float a = (float)((this.rnd.nextFloat() - 0.5F) * Math.PI * 0.9F);
         this.parts
            .add(
               new float[]{
                  this.bxp,
                  this.byp,
                  dir * (float)Math.cos(a) * (60.0F + this.rnd.nextFloat() * 90.0F),
                  (float)Math.sin(a) * (60.0F + this.rnd.nextFloat() * 90.0F),
                  0.0F,
                  col
               }
            );
      }

      Sound.play("minecraft:block.note_block.basedrum", 1.0F + Math.min(0.8F, this.hits * 0.05F), 0.3F);
      if (dir < 0) {
         this.aiErr = 0.0F;
      }
   }

   private float predict() {
      float x = this.bxp;
      float y = this.byp;
      float dvx = this.vx;
      float dvy = this.vy;
      float tx = 352.0F;
      if (dvx <= 0.0F) {
         return 130.0F;
      } else {
         float t = (tx - x) / dvx;
         float ny = y + dvy * t;
         float span = 250.0F;
         float m = ((ny - 5.0F) % (2.0F * span) + 2.0F * span) % (2.0F * span);
         return 5.0F + (m > span ? 2.0F * span - m : m);
      }
   }

   private void point(boolean player) {
      this.goalFlash = 1.0F;
      this.goalSide = player ? 1.0F : -1.0F;

      for (float[] t : this.trail) {
         t[0] = 190.0F;
         t[1] = 130.0F;
      }

      if (player) {
         this.pScore++;
         this.score = this.pScore;
         this.scorePopP = 1.0F;
         Sound.play("minecraft:entity.experience_orb.pickup", 1.2F, 0.35F);
      } else {
         this.aScore++;
         this.scorePopA = 1.0F;
         Sound.play("minecraft:block.note_block.bass", 0.8F, 0.35F);
      }

      if (this.pScore < 7 && this.aScore < 7) {
         this.serve(player ? 1 : -1);
      } else {
         this.won = this.pScore >= 7;
         if (this.won) {
            this.score = this.pScore + 5;
         }

         this.bxp = 190.0F;
         this.byp = 130.0F;
         this.serveT = 99.0F;
         this.gameOver();
      }
   }

   @Override
   protected void render() {
      float dt = Math.min(0.05F, Gx.dt);
      this.anim += dt;
      this.goalFlash = Math.max(0.0F, this.goalFlash - dt * 1.8F);
      this.boxV(0.0F, 0.0F, 380.0F, 260.0F, 12.0F, Style.light ? -1775636 : -15855594, Style.light ? -2433565 : -16119025);

      for (float y = 10.0F; y < 260.0F; y += 16.0F) {
         for (float x = 10.0F; x < 380.0F; x += 16.0F) {
            this.circle(x, y, 2.3F, Style.light ? 218103808 : 117440511);
         }
      }

      Gx.glow(this.X(0.0F), this.Y(130.0F), this.S(90.0F), Gx.withAlpha(Style.accent, 0.08F + (this.goalSide < 0.0F ? this.goalFlash * 0.4F : 0.0F)));
      Gx.glow(this.X(380.0F), this.Y(130.0F), this.S(90.0F), Gx.withAlpha(-3597815, 0.08F + (this.goalSide > 0.0F ? this.goalFlash * 0.4F : 0.0F)));

      for (float y = 6.0F; y < 260.0F; y += 14.0F) {
         this.box(188.0F, y, 4.0F, 8.0F, 2.0F, Style.light ? 436207616 : 352321535);
      }

      Gx.ringAt(this.X(190.0F), this.Y(130.0F), this.S(34.0F), Math.max(1, this.S(1.2F)), Style.light ? 335544320 : 285212671);
      this.scoreNum(this.pScore, 156.0F, this.scorePopP, Style.accent, "DU");
      this.scoreNum(this.aScore, 224.0F, this.scorePopA, Style.light ? -15395302 : -1, "KI");

      for (int i = 0; i < 7; i++) {
         this.circle(168.0F - i * 7, 62.0F, 2.0F, i < this.pScore ? Style.accent : (Style.light ? 436207616 : 419430399));
         this.circle(212.0F + i * 7, 62.0F, 2.0F, i < this.aScore ? -3597815 : (Style.light ? 436207616 : 419430399));
      }

      this.paddle(14.0F, this.py, Style.accent, this.pHit, 1);
      this.paddle(357.0F, this.ay, -3597815, this.aHit, -1);

      for (float[] p : this.parts) {
         float a = 1.0F - p[4] / 0.5F;
         this.circle(p[0], p[1], 1.6F * a + 0.4F, Gx.withAlpha((int)p[5], a));
      }

      if (this.state != GameView.State.OVER) {
         boolean blink = this.serveT > 0.0F && this.serveT < 5.0F && (int)(this.serveT * 8.0F) % 2 == 0;
         if (this.serveT <= 0.0F) {
            for (int i = this.trail.length - 1; i >= 0; i--) {
               float a = (1.0F - (float)i / this.trail.length) * 0.28F;
               this.circle(
                  this.trail[i][0],
                  this.trail[i][1],
                  5.0F * (1.0F - (float)i / this.trail.length * 0.6F),
                  Gx.withAlpha(this.vx > 0.0F ? Style.accent : -3597815, a)
               );
            }
         }

         if (!blink) {
            Gx.glow(this.X(this.bxp), this.Y(this.byp), this.S(20.0F), 1157627903);
            this.circle(this.bxp, this.byp + 0.6F, 5.0F, -3551788);
            this.circle(this.bxp, this.byp, 5.0F, -1);
            Gx.ringAt(this.X(this.bxp), this.Y(this.byp), this.S(3.1F), Math.max(1, this.S(0.7F)), 855638016);
         }
      }

      if (this.state == GameView.State.PLAYING && this.serveT > 0.0F && this.pScore + this.aScore == 0) {
         float a = Ease.outCubic(Math.min(1.0F, this.serveT / 0.3F));
         this.text("Erster bei 7 gewinnt!", 190.0F, 180.0F, 8.0F, 3, Gx.withAlpha(Style.sub, a));
      }
   }

   private void scoreNum(int v, float cx, float pop, int col, String label) {
      float s = 1.0F + 0.35F * Ease.outCubic(pop);
      Gx.push();
      Gx.scaleAt(this.X(cx), this.Y(34.0F), s);
      if (pop > 0.0F) {
         Gx.glow(this.X(cx), this.Y(34.0F), this.S(34.0F), Gx.withAlpha(col, pop * 0.4F));
      }

      this.text(String.valueOf(v), cx, 34.0F, 30.0F, 3, Gx.withAlpha(col, 0.92F));
      Gx.pop();
      this.text(label, cx, 12.0F, 6.5F, 3, Style.muted);
   }

   private void paddle(float x, float cy, int col, float hit, int side) {
      float y = cy - 24.0F;
      float off = -side * hit * 2.0F;
      x += off;
      Gx.glow(this.X(x + 4.5F), this.Y(cy), this.S(38.4F), Gx.withAlpha(col, 0.14F + hit * 0.3F));

      for (int i = 0; i < 4; i++) {
         float sy = y + 48.0F * (i + 0.5F) / 4.0F - 3.0F;
         float sx = side > 0 ? x + 9.0F - 0.5F : x - 2.5F;
         this.box(sx, sy, 3.0F, 6.0F, 1.0F, Gx.mix(col, -1, 0.12F));
      }

      Gx.shadow(this.X(x), this.Y(y + 1.0F), this.S(9.0F), this.S(48.0F), this.S(3.0F), this.S(3.0F), 0.4F);
      this.boxV(x, y, 9.0F, 48.0F, 3.0F, Gx.mix(col, -1, 0.2F), Gx.mix(col, -16777216, 0.2F));
      this.box(x + 1.5F, y + 2.0F, 1.5F, 44.0F, 0.75F, 1090519039);
      Gx.outline(this.X(x), this.Y(y), this.S(9.0F), this.S(48.0F), this.S(3.0F), Math.max(1, this.S(0.5F)), Gx.withAlpha(Gx.mix(col, -16777216, 0.5F), 0.7F));
      if (hit > 0.0F) {
         this.box(x, y, 9.0F, 48.0F, 3.0F, Gx.withAlpha(-1, hit * 0.4F));
      }
   }
}
