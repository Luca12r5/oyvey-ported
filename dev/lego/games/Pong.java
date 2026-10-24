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

   private void serve(int var1) {
      this.bxp = 190.0F;
      this.byp = 130.0F;
      this.speed = 205.0F;
      this.hits = 0;
      this.serveT = 0.9F;
      float var2 = (float)Math.toRadians(-25.0F + this.rnd.nextFloat() * 50.0F);
      this.vx = var1 * (float)Math.cos(var2) * this.speed;
      this.vy = (float)Math.sin(var2) * this.speed;

      for (float[] var6 : this.trail) {
         var6[0] = this.bxp;
         var6[1] = this.byp;
      }
   }

   @Override
   protected void onKey(int var1) {
      if (var1 == 265 || var1 == 87) {
         this.up = true;
         this.mouseMode = false;
      }

      if (var1 == 264 || var1 == 83) {
         this.down = true;
         this.mouseMode = false;
      }
   }

   @Override
   protected void onKeyUp(int var1) {
      if (var1 == 265 || var1 == 87) {
         this.up = false;
      }

      if (var1 == 264 || var1 == 83) {
         this.down = false;
      }
   }

   @Override
   protected void update(float var1) {
      if (Math.abs(this.my - this.lastMy) > 0.5F && this.my >= 0.0F) {
         if (this.lastMy > -9999.0F) {
            this.mouseMode = true;
         }

         this.lastMy = this.my;
      }

      if (this.mouseMode && this.u > 0.0F) {
         float var2 = (this.my - this.by) / this.u;
         this.py = this.py + (var2 - this.py) * Math.min(1.0F, var1 * 22.0F);
      } else {
         this.py = this.py + ((this.down ? 1 : 0) - (this.up ? 1 : 0)) * 300 * var1;
      }

      this.py = this.clampPad(this.py);
      this.aiThink -= var1;
      if (this.aiThink <= 0.0F) {
         this.aiThink = 0.1F + this.rnd.nextFloat() * 0.1F;
         if (this.vx > 0.0F && this.serveT <= 0.0F) {
            this.aiTarget = this.predict() + this.aiErr;
         } else {
            this.aiTarget = 130.0F + (this.byp - 130.0F) * 0.3F;
            this.aiErr = (this.rnd.nextFloat() - 0.5F) * (43.199997F + this.hits * 4);
         }
      }

      float var9 = this.aiTarget - this.ay;
      float var3 = (185.0F + Math.min(60, this.hits * 4)) * var1;
      this.ay = this.ay + Math.max(-var3, Math.min(var3, var9 * Math.min(1.0F, var1 * 10.0F) * 6.0F));
      this.ay = this.clampPad(this.ay);
      this.pHit = Math.max(0.0F, this.pHit - var1 * 4.0F);
      this.aHit = Math.max(0.0F, this.aHit - var1 * 4.0F);
      this.scorePopP = Math.max(0.0F, this.scorePopP - var1 * 2.5F);
      this.scorePopA = Math.max(0.0F, this.scorePopA - var1 * 2.5F);

      for (float[] var5 : this.parts) {
         var5[0] += var5[2] * var1;
         var5[1] += var5[3] * var1;
         var5[2] *= 0.96F;
         var5[3] *= 0.96F;
         var5[4] += var1;
      }

      this.parts.removeIf(var0 -> var0[4] > 0.5F);
      if (this.serveT > 0.0F) {
         this.serveT -= var1;

         for (float[] var16 : this.trail) {
            var16[0] = this.bxp;
            var16[1] = this.byp;
         }
      } else {
         for (int var10 = this.trail.length - 1; var10 > 0; var10--) {
            this.trail[var10][0] = this.trail[var10 - 1][0];
            this.trail[var10][1] = this.trail[var10 - 1][1];
         }

         this.trail[0][0] = this.bxp;
         this.trail[0][1] = this.byp;
         byte var11 = 3;
         float var13 = var1 / var11;

         for (int var6 = 0; var6 < var11; var6++) {
            this.bxp = this.bxp + this.vx * var13;
            this.byp = this.byp + this.vy * var13;
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

            float var7 = 23.0F;
            if (this.vx < 0.0F && this.bxp - 5.0F <= var7 && this.bxp - 5.0F > 8.0F && Math.abs(this.byp - this.py) <= 29.0F) {
               this.bounce(this.py, 1);
               this.bxp = var7 + 5.0F;
               this.pHit = 1.0F;
            }

            float var8 = 357.0F;
            if (this.vx > 0.0F && this.bxp + 5.0F >= var8 && this.bxp + 5.0F < 372.0F && Math.abs(this.byp - this.ay) <= 29.0F) {
               this.bounce(this.ay, -1);
               this.bxp = var8 - 5.0F;
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

   private float clampPad(float var1) {
      return Math.max(27.0F, Math.min(233.0F, var1));
   }

   private void bounce(float var1, int var2) {
      float var3 = Math.max(-1.0F, Math.min(1.0F, (this.byp - var1) / 24.0F));
      float var4 = (float)Math.toRadians(var3 * 55.0F);
      this.hits++;
      this.speed = Math.min(470.0F, this.speed * 1.07F);
      this.vx = var2 * (float)Math.cos(var4) * this.speed;
      this.vy = (float)Math.sin(var4) * this.speed;
      int var5 = var2 > 0 ? Style.accent : -3597815;

      for (int var6 = 0; var6 < 8; var6++) {
         float var7 = (float)((this.rnd.nextFloat() - 0.5F) * Math.PI * 0.9F);
         this.parts
            .add(
               new float[]{
                  this.bxp,
                  this.byp,
                  var2 * (float)Math.cos(var7) * (60.0F + this.rnd.nextFloat() * 90.0F),
                  (float)Math.sin(var7) * (60.0F + this.rnd.nextFloat() * 90.0F),
                  0.0F,
                  var5
               }
            );
      }

      Sound.play("minecraft:block.note_block.basedrum", 1.0F + Math.min(0.8F, this.hits * 0.05F), 0.3F);
      if (var2 < 0) {
         this.aiErr = 0.0F;
      }
   }

   private float predict() {
      float var1 = this.bxp;
      float var2 = this.byp;
      float var3 = this.vx;
      float var4 = this.vy;
      float var5 = 352.0F;
      if (var3 <= 0.0F) {
         return 130.0F;
      } else {
         float var6 = (var5 - var1) / var3;
         float var7 = var2 + var4 * var6;
         float var8 = 250.0F;
         float var9 = ((var7 - 5.0F) % (2.0F * var8) + 2.0F * var8) % (2.0F * var8);
         return 5.0F + (var9 > var8 ? 2.0F * var8 - var9 : var9);
      }
   }

   private void point(boolean var1) {
      this.goalFlash = 1.0F;
      this.goalSide = var1 ? 1.0F : -1.0F;

      for (float[] var5 : this.trail) {
         var5[0] = 190.0F;
         var5[1] = 130.0F;
      }

      if (var1) {
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
         this.serve(var1 ? 1 : -1);
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
      float var1 = Math.min(0.05F, Gx.dt);
      this.anim += var1;
      this.goalFlash = Math.max(0.0F, this.goalFlash - var1 * 1.8F);
      this.boxV(0.0F, 0.0F, 380.0F, 260.0F, 12.0F, Style.light ? -1775636 : -15855594, Style.light ? -2433565 : -16119025);

      for (float var2 = 10.0F; var2 < 260.0F; var2 += 16.0F) {
         for (float var3 = 10.0F; var3 < 380.0F; var3 += 16.0F) {
            this.circle(var3, var2, 2.3F, Style.light ? 218103808 : 117440511);
         }
      }

      Gx.glow(this.X(0.0F), this.Y(130.0F), this.S(90.0F), Gx.withAlpha(Style.accent, 0.08F + (this.goalSide < 0.0F ? this.goalFlash * 0.4F : 0.0F)));
      Gx.glow(this.X(380.0F), this.Y(130.0F), this.S(90.0F), Gx.withAlpha(-3597815, 0.08F + (this.goalSide > 0.0F ? this.goalFlash * 0.4F : 0.0F)));

      for (float var5 = 6.0F; var5 < 260.0F; var5 += 14.0F) {
         this.box(188.0F, var5, 4.0F, 8.0F, 2.0F, Style.light ? 436207616 : 352321535);
      }

      Gx.ringAt(this.X(190.0F), this.Y(130.0F), this.S(34.0F), Math.max(1, this.S(1.2F)), Style.light ? 335544320 : 285212671);
      this.scoreNum(this.pScore, 156.0F, this.scorePopP, Style.accent, "DU");
      this.scoreNum(this.aScore, 224.0F, this.scorePopA, Style.light ? -15395302 : -1, "KI");

      for (int var6 = 0; var6 < 7; var6++) {
         this.circle(168.0F - var6 * 7, 62.0F, 2.0F, var6 < this.pScore ? Style.accent : (Style.light ? 436207616 : 419430399));
         this.circle(212.0F + var6 * 7, 62.0F, 2.0F, var6 < this.aScore ? -3597815 : (Style.light ? 436207616 : 419430399));
      }

      this.paddle(14.0F, this.py, Style.accent, this.pHit, 1);
      this.paddle(357.0F, this.ay, -3597815, this.aHit, -1);

      for (float[] var10 : this.parts) {
         float var4 = 1.0F - var10[4] / 0.5F;
         this.circle(var10[0], var10[1], 1.6F * var4 + 0.4F, Gx.withAlpha((int)var10[5], var4));
      }

      if (this.state != GameView.State.OVER) {
         boolean var8 = this.serveT > 0.0F && this.serveT < 5.0F && (int)(this.serveT * 8.0F) % 2 == 0;
         if (this.serveT <= 0.0F) {
            for (int var11 = this.trail.length - 1; var11 >= 0; var11--) {
               float var12 = (1.0F - (float)var11 / this.trail.length) * 0.28F;
               this.circle(
                  this.trail[var11][0],
                  this.trail[var11][1],
                  5.0F * (1.0F - (float)var11 / this.trail.length * 0.6F),
                  Gx.withAlpha(this.vx > 0.0F ? Style.accent : -3597815, var12)
               );
            }
         }

         if (!var8) {
            Gx.glow(this.X(this.bxp), this.Y(this.byp), this.S(20.0F), 1157627903);
            this.circle(this.bxp, this.byp + 0.6F, 5.0F, -3551788);
            this.circle(this.bxp, this.byp, 5.0F, -1);
            Gx.ringAt(this.X(this.bxp), this.Y(this.byp), this.S(3.1F), Math.max(1, this.S(0.7F)), 855638016);
         }
      }

      if (this.state == GameView.State.PLAYING && this.serveT > 0.0F && this.pScore + this.aScore == 0) {
         float var9 = Ease.outCubic(Math.min(1.0F, this.serveT / 0.3F));
         this.text("Erster bei 7 gewinnt!", 190.0F, 180.0F, 8.0F, 3, Gx.withAlpha(Style.sub, var9));
      }
   }

   private void scoreNum(int var1, float var2, float var3, int var4, String var5) {
      float var6 = 1.0F + 0.35F * Ease.outCubic(var3);
      Gx.push();
      Gx.scaleAt(this.X(var2), this.Y(34.0F), var6);
      if (var3 > 0.0F) {
         Gx.glow(this.X(var2), this.Y(34.0F), this.S(34.0F), Gx.withAlpha(var4, var3 * 0.4F));
      }

      this.text(String.valueOf(var1), var2, 34.0F, 30.0F, 3, Gx.withAlpha(var4, 0.92F));
      Gx.pop();
      this.text(var5, var2, 12.0F, 6.5F, 3, Style.muted);
   }

   private void paddle(float var1, float var2, int var3, float var4, int var5) {
      float var6 = var2 - 24.0F;
      float var7 = -var5 * var4 * 2.0F;
      var1 += var7;
      Gx.glow(this.X(var1 + 4.5F), this.Y(var2), this.S(38.4F), Gx.withAlpha(var3, 0.14F + var4 * 0.3F));

      for (int var8 = 0; var8 < 4; var8++) {
         float var9 = var6 + 48.0F * (var8 + 0.5F) / 4.0F - 3.0F;
         float var10 = var5 > 0 ? var1 + 9.0F - 0.5F : var1 - 2.5F;
         this.box(var10, var9, 3.0F, 6.0F, 1.0F, Gx.mix(var3, -1, 0.12F));
      }

      Gx.shadow(this.X(var1), this.Y(var6 + 1.0F), this.S(9.0F), this.S(48.0F), this.S(3.0F), this.S(3.0F), 0.4F);
      this.boxV(var1, var6, 9.0F, 48.0F, 3.0F, Gx.mix(var3, -1, 0.2F), Gx.mix(var3, -16777216, 0.2F));
      this.box(var1 + 1.5F, var6 + 2.0F, 1.5F, 44.0F, 0.75F, 1090519039);
      Gx.outline(
         this.X(var1), this.Y(var6), this.S(9.0F), this.S(48.0F), this.S(3.0F), Math.max(1, this.S(0.5F)), Gx.withAlpha(Gx.mix(var3, -16777216, 0.5F), 0.7F)
      );
      if (var4 > 0.0F) {
         this.box(var1, var6, 9.0F, 48.0F, 3.0F, Gx.withAlpha(-1, var4 * 0.4F));
      }
   }
}
