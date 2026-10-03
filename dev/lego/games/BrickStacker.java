package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class BrickStacker extends GameView {
   private static final float W = 400.0F;
   private static final float H = 320.0F;
   private static final float BH = 16.0F;
   private static final float START_W = 120.0F;
   private static final float BASE_Y = 290.0F;
   private static final float PERFECT = 3.2F;
   private static final int[] COLORS = new int[]{-3597815, -95720, -864969, -11821238, -13193537, -16755265, -7194248, -1790520};
   private final List<BrickStacker.Brick> tower = new ArrayList<>();
   private final List<BrickStacker.Chip> chips = new ArrayList<>();
   private final GameFx fx = new GameFx();
   private final float[][] clouds = new float[8][3];
   private final float[][] stars = new float[60][3];
   private float curX;
   private float curW;
   private float dir;
   private float speed;
   private float cam;
   private float anim;
   private float dropT;
   private float missT;
   private float hintT;
   private int combo;
   private int colorIdx;
   private int bestCombo;
   private boolean missed;

   public BrickStacker() {
      super("stacker");
      Random var1 = new Random(5L);

      for (float[] var5 : this.clouds) {
         var5[0] = var1.nextFloat() * 400.0F;
         var5[1] = 60.0F + var1.nextFloat() * 1400.0F;
         var5[2] = 0.6F + var1.nextFloat() * 0.7F;
      }

      for (float[] var9 : this.stars) {
         var9[0] = var1.nextFloat() * 400.0F;
         var9[1] = var1.nextFloat() * 320.0F;
         var9[2] = 0.3F + var1.nextFloat() * 0.7F;
      }
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 320.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Punkte";
   }

   @Override
   protected void reset() {
      this.tower.clear();
      this.chips.clear();
      this.fx.clear();
      this.colorIdx = new Random().nextInt(COLORS.length);
      BrickStacker.Brick var1 = new BrickStacker.Brick();
      var1.w = 120.0F;
      var1.x = 140.0F;
      var1.color = COLORS[this.colorIdx++ % COLORS.length];
      this.tower.add(var1);
      this.combo = 0;
      this.bestCombo = 0;
      this.cam = 0.0F;
      this.missed = false;
      this.missT = 0.0F;
      this.hintT = 0.0F;
      this.next();
   }

   private float topY(int var1) {
      return 290.0F - (var1 + 1) * 16.0F;
   }

   private void next() {
      BrickStacker.Brick var1 = this.tower.get(this.tower.size() - 1);
      this.curW = var1.w;
      this.dir = this.tower.size() % 2 == 0 ? -1.0F : 1.0F;
      this.curX = this.dir > 0.0F ? -this.curW : 400.0F;
      this.speed = Math.min(300.0F, 118.0F + this.tower.size() * 4.2F);
      this.dropT = 0.0F;
   }

   @Override
   protected void onKey(int var1) {
      if (var1 == 32 || var1 == 264 || var1 == 83 || var1 == 257) {
         this.drop();
      }
   }

   @Override
   protected void onClick(float var1, float var2, int var3) {
      if (var3 == 0) {
         this.drop();
      }
   }

   private void drop() {
      if (!this.missed && !(this.time < 0.2F) && !(this.dropT < 0.12F)) {
         BrickStacker.Brick var1 = this.tower.get(this.tower.size() - 1);
         float var2 = Math.max(this.curX, var1.x);
         float var3 = Math.min(this.curX + this.curW, var1.x + var1.w);
         float var4 = this.topY(this.tower.size());
         int var5 = COLORS[this.colorIdx % COLORS.length];
         if (var3 - var2 <= 0.5F) {
            this.missed = true;
            BrickStacker.Chip var9 = new BrickStacker.Chip();
            var9.x = this.curX;
            var9.y = var4;
            var9.w = this.curW;
            var9.vx = this.dir * this.speed * 0.3F;
            var9.vy = 0.0F;
            var9.color = var5;
            this.chips.add(var9);
            this.fx.shake(0.6F);
            Sound.play("minecraft:block.stone.break", 0.6F, 0.5F);
         } else {
            BrickStacker.Brick var6 = new BrickStacker.Brick();
            var6.color = var5;
            this.colorIdx++;
            boolean var7 = Math.abs(this.curX - var1.x) <= 3.2F;
            if (var7) {
               this.combo++;
               this.bestCombo = Math.max(this.bestCombo, this.combo);
               var6.x = var1.x;
               var6.w = var1.w;
               if (this.combo >= 3) {
                  float var8 = Math.min(8.0F, 120.0F - var6.w);
                  if (var8 > 0.0F) {
                     var6.x -= var8 / 2.0F;
                     var6.w += var8;
                     var6.x = Math.max(4.0F, Math.min(396.0F - var6.w, var6.x));
                     this.fx.popup("Wächst!", 200.0F, var4 - 30.0F, -6291531, 9.0F);
                  }
               }

               var6.flash = 1.0F;
               this.score = this.score + (1 + this.combo);
               this.fx.ring(var6.x + var6.w / 2.0F, var4 + 8.0F, var6.w * 0.75F, -1, 0.45F);
               this.fx
                  .popup(this.combo > 1 ? "Perfekt! x" + this.combo : "Perfekt!", var6.x + var6.w / 2.0F, var4 - 12.0F, -736942, 11 + Math.min(4, this.combo));
               this.fx.burst(var6.x + var6.w / 2.0F, var4 + 8.0F, 14 + this.combo * 2, -5720, 120.0F, 2.4F, 0.6F, 0.0F, 2);
               Sound.play("minecraft:block.note_block.pling", 0.8F + Math.min(1.2F, this.combo * 0.1F), 0.45F);
            } else {
               this.combo = 0;
               var6.x = var2;
               var6.w = var3 - var2;
               BrickStacker.Chip var10 = new BrickStacker.Chip();
               var10.color = var5;
               var10.y = var4;
               if (this.curX < var1.x) {
                  var10.x = this.curX;
                  var10.w = var1.x - this.curX;
                  var10.vx = -40.0F - this.rnd.nextFloat() * 30.0F;
               } else {
                  var10.x = var3;
                  var10.w = this.curX + this.curW - var3;
                  var10.vx = 40.0F + this.rnd.nextFloat() * 30.0F;
               }

               var10.vy = -40.0F;
               this.chips.add(var10);
               this.score++;
               this.fx.popup("+1", var6.x + var6.w / 2.0F, var4 - 10.0F, -1, 9.0F);
               Sound.play("minecraft:block.stone.place", 0.9F + this.rnd.nextFloat() * 0.2F, 0.5F);
            }

            this.fx.burst(var6.x + 2.0F, var4 + 16.0F, 5, -1, 40.0F, 2.0F, 0.4F, -20.0F, 0);
            this.fx.burst(var6.x + var6.w - 2.0F, var4 + 16.0F, 5, -1, 40.0F, 2.0F, 0.4F, -20.0F, 0);

            for (int var11 = Math.max(0, this.tower.size() - 5); var11 < this.tower.size(); var11++) {
               this.tower.get(var11).wob = 1.0F - (this.tower.size() - var11) * 0.18F;
            }

            var6.wob = 1.0F;
            this.tower.add(var6);
            int var12 = this.tower.size() - 1;
            if (var12 % 10 == 0) {
               this.fx.popup(var12 + " Steine!", 200.0F, var4 - 34.0F, -33835, 15.0F);
               this.fx.shake(0.2F);
               Sound.play("minecraft:entity.player.levelup", 1.2F, 0.4F);
            }

            this.next();
         }
      }
   }

   @Override
   protected void update(float var1) {
      this.fx.update(var1);
      this.hintT += var1;
      this.dropT += var1;

      for (BrickStacker.Brick var3 : this.tower) {
         var3.wob = Math.max(0.0F, var3.wob - var1 * 2.5F);
         var3.flash = Math.max(0.0F, var3.flash - var1 * 3.0F);
      }

      for (BrickStacker.Chip var6 : this.chips) {
         var6.t += var1;
         var6.vy += 900.0F * var1;
         var6.x = var6.x + var6.vx * var1;
         var6.y = var6.y + var6.vy * var1;
      }

      this.chips.removeIf(var1x -> var1x.y - this.camY() > 420.0F);
      if (this.missed) {
         this.missT += var1;
         if (this.missT > 0.9F) {
            this.gameOver();
         }
      } else {
         this.curX = this.curX + this.dir * this.speed * var1;
         if (this.curX > 400.0F - this.curW * 0.1F && this.dir > 0.0F) {
            this.dir = -1.0F;
         }

         if (this.curX < -this.curW * 0.9F && this.dir < 0.0F) {
            this.dir = 1.0F;
         }

         float var5 = Math.max(0.0F, (this.tower.size() - 9) * 16.0F);
         this.cam = this.cam + (var5 - this.cam) * Math.min(1.0F, var1 * 5.0F);
      }
   }

   private float camY() {
      return -this.cam;
   }

   @Override
   protected void render() {
      float var1 = Math.min(0.05F, Gx.dt);
      this.anim += var1;
      float var2 = this.cam / 16.0F;
      float var3 = Ease.clamp(var2 / 40.0F);
      float var4 = Ease.clamp((var2 - 40.0F) / 50.0F);
      int var5 = Gx.mix(Gx.mix(-10509338, -2066854, var3), -16119004, var4);
      int var6 = Gx.mix(Gx.mix(-3283206, -539254, var3), -14016946, var4);
      this.boxV(0.0F, 0.0F, 400.0F, 320.0F, 0.0F, var5, var6);
      if (var4 > 0.0F) {
         for (float[] var10 : this.stars) {
            float var11 = ((var10[1] + this.cam * 0.1F * var10[2]) % 320.0F + 320.0F) % 320.0F;
            this.circle(var10[0], var11, 0.6F + var10[2], Gx.withAlpha(-1, var4 * var10[2] * (0.6F + 0.4F * (float)Math.sin(this.anim * 2.0F + var10[0]))));
         }
      }

      float var16 = 70.0F + var3 * 60.0F + this.cam * 0.05F;
      if (var16 < 360.0F) {
         Gx.glow(this.X(330.0F), this.Y(var16), this.S(70.0F), Gx.withAlpha(-6496, 0.5F * (1.0F - var4)));
         this.circle(330.0F, var16, 16.0F, Gx.withAlpha(Gx.mix(-3132, -26022, var3), 1.0F - var4));
      }

      for (float[] var26 : this.clouds) {
         float var12 = 320.0F - var26[1] + this.cam * 0.55F;
         if (!(var12 < -30.0F) && !(var12 > 350.0F)) {
            float var13 = ((var26[0] + this.anim * 6.0F * var26[2]) % 480.0F + 400.0F + 80.0F) % 480.0F - 40.0F;
            this.cloud(var13, var12, var26[2], Gx.withAlpha(-1, 0.75F * (1.0F - var4 * 0.7F)));
         }
      }

      float var18 = 290.0F + this.cam * 0.35F;
      if (var18 - 80.0F < 320.0F) {
         for (int var20 = 0; var20 < 11; var20++) {
            float var23 = 26 + var20 * 37 % 17;
            float var27 = 30 + var20 * 53 % 50;
            this.box(var20 * 40 - 6, var18 - var27, var23, var27 + 60.0F, 2.0F, Gx.withAlpha(Gx.mix(-9597000, -12965286, var3), 0.55F));
         }
      }

      Gx.push();
      Gx.translate(this.S(this.fx.sx), this.S(this.fx.sy));
      float var21 = 290.0F + this.cam;
      if (var21 < 340.0F) {
         this.boxV(-4.0F, var21, 408.0F, 40.0F, 0.0F, -12603816, -14452166);

         for (float var24 = 4.0F; var24 < 400.0F; var24 += 12.0F) {
            this.boxV(var24, var21 - 3.0F, 8.0F, 4.0F, 1.2F, -9710205, -12603816);
         }
      }

      int var25 = Math.max(0, (int)(this.cam / 16.0F) - 2);

      for (int var28 = var25; var28 < this.tower.size(); var28++) {
         BrickStacker.Brick var33 = this.tower.get(var28);
         float var37 = this.topY(var28) + this.cam;
         if (!(var37 > 340.0F)) {
            if (var37 < -32.0F) {
               break;
            }

            float var14 = (float)Math.sin(var33.wob * 14.0F) * var33.wob * 1.4F;
            int var15 = Gx.mix(var33.color, -1, var33.flash * 0.6F);
            this.sideBrick(var33.x + var14, var37, var33.w, 16.0F, var15);
         }
      }

      for (byte var29 = 10; var29 < this.tower.size() + 12; var29 += 10) {
         float var34 = this.topY(var29 - 1) + this.cam;
         if (!(var34 < -10.0F) && !(var34 > 330.0F)) {
            for (float var38 = 0.0F; var38 < 400.0F; var38 += 10.0F) {
               this.box(var38, var34, 5.0F, 0.8F, 0.4F, 1442840575);
            }

            this.box(366.0F, var34 - 8.0F, 30.0F, 12.0F, 6.0F, 1711276032);
            this.text(String.valueOf((int)var29), 381.0F, var34 - 2.0F, 6.5F, 3, -1);
         }
      }

      if (!this.missed && this.state != GameView.State.OVER) {
         float var30 = this.topY(this.tower.size()) + this.cam - 2.0F - (float)Math.sin(this.anim * 6.0F) * 0.8F;
         int var35 = COLORS[this.colorIdx % COLORS.length];
         BrickStacker.Brick var39 = this.tower.get(this.tower.size() - 1);
         float var41 = Math.max(this.curX, var39.x);
         float var43 = Math.min(this.curX + this.curW, var39.x + var39.w);
         if (var43 > var41) {
            this.box(var41, var30 + 16.0F + 1.5F, var43 - var41, 1.2F, 0.6F, -1996488705);
         }

         Gx.glow(this.X(this.curX + this.curW / 2.0F), this.Y(var30 + 8.0F), this.S(this.curW * 0.6F), Gx.withAlpha(var35, 0.18F));
         this.sideBrick(this.curX, var30, this.curW, 16.0F, var35);
         if (Math.abs(this.curX - var39.x) <= 3.2F) {
            this.box(this.curX, var30 - 5.0F, this.curW, 1.5F, 0.7F, -856374958);
         }
      }

      for (BrickStacker.Chip var36 : this.chips) {
         float var40 = Math.max(0.0F, 1.0F - var36.t * 0.9F);
         Gx.pushAlpha(var40);
         float var42 = 1.0F - Math.min(0.3F, var36.t * 0.3F);
         this.sideBrick(var36.x, var36.y + this.cam, var36.w, 16.0F * var42, var36.color);
         Gx.popAlpha();
      }

      this.fx.draw(this, 0.0F, -this.cam);
      Gx.pop();
      this.box(8.0F, 8.0F, 86.0F, 22.0F, 11.0F, 1426063360);
      Gx.icon("brick", this.X(21.0F), this.Y(19.0F), 10.0F * this.u, -1);
      this.text(this.tower.size() - 1 + " Steine", 58.0F, 19.0F, 8.0F, 3, -1);
      if (this.combo >= 2) {
         this.box(306.0F, 8.0F, 86.0F, 22.0F, 11.0F, Gx.withAlpha(-736942, 0.95F));
         this.text("Perfekt-Serie " + this.combo, 349.0F, 19.0F, 7.4F, 3, -12965376);
      }

      if (this.state == GameView.State.PLAYING && this.hintT < 3.5F && this.tower.size() < 3) {
         float var32 = Math.min(1.0F, 3.5F - this.hintT) * (0.6F + 0.4F * (float)Math.sin(this.anim * 5.0F));
         this.box(104.0F, 44.0F, 192.0F, 22.0F, 11.0F, Gx.withAlpha(-16777216, 0.45F * var32));
         this.text("Leertaste oder Klick zum Absetzen", 200.0F, 55.0F, 7.6F, 3, Gx.withAlpha(-1, var32));
      }

      this.fx.drawPopups(this, 0.0F, -this.cam);
      this.fx.drawFlash(this, 400.0F, 320.0F);
   }

   private void cloud(float var1, float var2, float var3, int var4) {
      this.circle(var1, var2, 10.0F * var3, var4);
      this.circle(var1 + 12.0F * var3, var2 - 5.0F * var3, 13.0F * var3, var4);
      this.circle(var1 + 26.0F * var3, var2, 10.0F * var3, var4);
      this.box(var1, var2 - 1.0F * var3, 26.0F * var3, 10.0F * var3, 5.0F * var3, var4);
   }

   private void sideBrick(float var1, float var2, float var3, float var4, int var5) {
      if (!(var3 < 0.5F)) {
         int var6 = Math.max(1, Math.round(var3 / 15.0F));
         float var7 = Math.min(8.0F, var3 * 0.6F);
         float var8 = var3 / var6;

         for (int var9 = 0; var9 < var6; var9++) {
            float var10 = var1 + var8 * (var9 + 0.5F) - var7 / 2.0F;
            this.boxV(var10, var2 - 3.2F, var7, 4.2F, 1.2F, Gx.mix(var5, -1, 0.3F), var5);
         }

         this.boxV(var1, var2, var3, var4, 2.0F, Gx.mix(var5, -1, 0.16F), Gx.mix(var5, -16777216, 0.18F));
         this.box(var1 + 1.5F, var2 + 1.2F, Math.max(0.0F, var3 - 3.0F), 1.3F, 0.6F, 1090519039);
         this.box(var1, var2 + var4 - 2.2F, var3, 2.2F, 1.0F, Gx.withAlpha(Gx.mix(var5, -16777216, 0.35F), 0.6F));
         Gx.outline(
            this.X(var1),
            this.Y(var2),
            Math.max(1, this.S(var3)),
            Math.max(1, this.S(var4)),
            this.S(2.0F),
            Math.max(1, this.S(0.55F)),
            Gx.withAlpha(Gx.mix(var5, -16777216, 0.55F), 0.7F)
         );
      }
   }

   private static final class Brick {
      float x;
      float w;
      float wob;
      float flash;
      int color;
   }

   private static final class Chip {
      float x;
      float y;
      float w;
      float vx;
      float vy;
      float t;
      int color;
   }
}
