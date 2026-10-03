package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;

public final class Breakout extends GameView {
   private static final float W = 360.0F;
   private static final float H = 290.0F;
   private static final float TOP = 22.0F;
   private static final int COLS = 10;
   private static final int ROWS = 6;
   private static final float BW = 32.0F;
   private static final float BH = 13.0F;
   private static final float GAP = 2.0F;
   private static final float X0 = 11.0F;
   private static final float Y0 = 36.0F;
   private static final float PY = 268.0F;
   private static final float PH = 9.0F;
   private static final float R = 4.5F;
   private static final int[] ROW_COLORS = new int[]{-3597815, -95720, -864969, -11821238, -16755265, -7194248};
   private static final int HARD = -6249047;
   private final List<Breakout.Brick> bricks = new ArrayList<>();
   private final List<float[]> parts = new ArrayList<>();
   private final List<float[]> pops = new ArrayList<>();
   private final float[][] trail = new float[10][2];
   private float padX;
   private float padW;
   private float ballX;
   private float ballY;
   private float vx;
   private float vy;
   private float speed;
   private float stuckT;
   private boolean stuck;
   private boolean left;
   private boolean right;
   private boolean mouseMode;
   private float lastMx = -9999.0F;
   private int lives;
   private int level;
   private int combo;
   private float levelAnim;
   private float bannerT = -1.0F;
   private float lifeFlash;
   private float padHit;

   public Breakout() {
      super("breakout");
   }

   @Override
   protected float boardW() {
      return 360.0F;
   }

   @Override
   protected float boardH() {
      return 290.0F;
   }

   @Override
   protected void reset() {
      this.lives = 3;
      this.level = 1;
      this.padW = 60.0F;
      this.padX = 180.0F;
      this.left = this.right = false;
      this.parts.clear();
      this.pops.clear();
      this.lifeFlash = 0.0F;
      this.build();
      this.bannerT = -1.0F;
   }

   private void build() {
      this.bricks.clear();
      int var1 = Math.min(4, 1 + (this.level - 1) / 2) + (this.level > 1 ? 1 : 0);

      for (int var2 = 0; var2 < 6; var2++) {
         for (int var3 = 0; var3 < 10; var3++) {
            if (pattern(this.level, var2, var3)) {
               Breakout.Brick var4 = new Breakout.Brick();
               var4.row = var2;
               var4.col = var3;
               var4.x = 11.0F + var3 * 34.0F;
               var4.y = 36.0F + var2 * 15.0F;
               var4.maxHp = var4.hp = var2 >= Math.min(var1, 2) && (this.level <= 2 || var2 >= var1 || (var3 + var2) % 3 != 0) ? 1 : 2;
               var4.color = ROW_COLORS[var2 % ROW_COLORS.length];
               this.bricks.add(var4);
            }
         }
      }

      this.levelAnim = 0.0F;
      this.speed = 190.0F * (1.0F + 0.09F * (this.level - 1));
      this.combo = 0;
      this.stick();
   }

   private static boolean pattern(int var0, int var1, int var2) {
      switch ((var0 - 1) % 4) {
         case 1:
            return Math.abs(var2 - 4.5F) <= var1 + 0.5F;
         case 2:
            return (var1 + var2) % 2 == 0 || var1 == 0 || var1 == 5;
         case 3:
            return var2 != 4 && var2 != 5 || var1 % 2 == 0;
         default:
            return true;
      }
   }

   private void stick() {
      this.stuck = true;
      this.stuckT = 0.0F;
      this.ballX = this.padX;
      this.ballY = 263.0F;

      for (float[] var4 : this.trail) {
         var4[0] = this.ballX;
         var4[1] = this.ballY;
      }
   }

   private void launch() {
      if (this.stuck) {
         this.stuck = false;
         float var1 = (float)Math.toRadians(-20.0F + this.rnd.nextFloat() * 40.0F);
         this.vx = (float)Math.sin(var1) * this.speed;
         this.vy = -((float)Math.cos(var1)) * this.speed;
         Sound.play("minecraft:block.note_block.hat", 1.4F, 0.25F);
      }
   }

   @Override
   protected void onKey(int var1) {
      if (var1 == 263 || var1 == 65) {
         this.left = true;
         this.mouseMode = false;
      }

      if (var1 == 262 || var1 == 68) {
         this.right = true;
         this.mouseMode = false;
      }

      if (var1 == 32 || var1 == 265 || var1 == 87) {
         this.launch();
      }
   }

   @Override
   protected void onKeyUp(int var1) {
      if (var1 == 263 || var1 == 65) {
         this.left = false;
      }

      if (var1 == 262 || var1 == 68) {
         this.right = false;
      }
   }

   @Override
   protected void onClick(float var1, float var2, int var3) {
      if (var3 == 0) {
         this.launch();
      }
   }

   @Override
   protected void update(float var1) {
      if (Math.abs(this.mx - this.lastMx) > 0.5F && this.mx >= 0.0F) {
         if (this.lastMx > -9999.0F) {
            this.mouseMode = true;
         }

         this.lastMx = this.mx;
      }

      if (this.mouseMode && this.u > 0.0F) {
         float var13 = (this.mx - this.bx) / this.u;
         this.padX = this.padX + (var13 - this.padX) * Math.min(1.0F, var1 * 25.0F);
      } else {
         float var2 = (this.right ? 1 : 0) - (this.left ? 1 : 0);
         this.padX += var2 * 320.0F * var1;
      }

      this.padX = Math.max(this.padW / 2.0F + 2.0F, Math.min(360.0F - this.padW / 2.0F - 2.0F, this.padX));
      this.padHit = Math.max(0.0F, this.padHit - var1 * 4.0F);

      for (float[] var3 : this.parts) {
         var3[0] += var3[2] * var1;
         var3[1] += var3[3] * var1;
         var3[3] += 300.0F * var1;
         var3[4] += var1;
      }

      this.parts.removeIf(var0 -> var0[4] > 0.8F);

      for (float[] var20 : this.pops) {
         var20[2] += var1;
      }

      this.pops.removeIf(var0 -> var0[2] > 0.8F);

      for (Breakout.Brick var21 : this.bricks) {
         var21.flash = Math.max(0.0F, var21.flash - var1 * 5.0F);
      }

      if (this.bannerT >= 0.0F) {
         this.bannerT += var1;
         if (this.bannerT > 1.6F) {
            this.bannerT = -1.0F;
         }
      }

      if (this.stuck) {
         this.ballX = this.padX;
         this.ballY = 263.0F;
         this.stuckT += var1;
         if (this.stuckT > 2.5F) {
            this.launch();
         }

         for (float[] var26 : this.trail) {
            var26[0] = this.ballX;
            var26[1] = this.ballY;
         }
      } else {
         for (int var17 = this.trail.length - 1; var17 > 0; var17--) {
            this.trail[var17][0] = this.trail[var17 - 1][0];
            this.trail[var17][1] = this.trail[var17 - 1][1];
         }

         this.trail[0][0] = this.ballX;
         this.trail[0][1] = this.ballY;
         byte var18 = 4;
         float var22 = var1 / var18;

         for (int var4 = 0; var4 < var18; var4++) {
            this.ballX = this.ballX + this.vx * var22;
            this.ballY = this.ballY + this.vy * var22;
            if (this.ballX < 4.5F) {
               this.ballX = 4.5F;
               this.vx = Math.abs(this.vx);
               this.wallSound();
            }

            if (this.ballX > 355.5F) {
               this.ballX = 355.5F;
               this.vx = -Math.abs(this.vx);
               this.wallSound();
            }

            if (this.ballY < 26.5F) {
               this.ballY = 26.5F;
               this.vy = Math.abs(this.vy);
               this.wallSound();
            }

            if (this.vy > 0.0F
               && this.ballY + 4.5F >= 268.0F
               && this.ballY - 4.5F < 277.0F
               && this.ballX > this.padX - this.padW / 2.0F - 4.5F
               && this.ballX < this.padX + this.padW / 2.0F + 4.5F) {
               float var5 = Math.max(-1.0F, Math.min(1.0F, (this.ballX - this.padX) / (this.padW / 2.0F)));
               float var6 = (float)Math.toRadians(var5 * 62.0F);
               this.speed = Math.min(this.speed * 1.012F, 190.0F * (1.0F + 0.09F * (this.level - 1)) * 1.35F);
               this.vx = (float)Math.sin(var6) * this.speed;
               this.vy = -((float)Math.cos(var6)) * this.speed;
               this.ballY = 263.5F;
               this.combo = 0;
               this.padHit = 1.0F;
               Sound.play("minecraft:block.note_block.basedrum", 1.2F, 0.3F);
            }

            for (Breakout.Brick var27 : this.bricks) {
               if (var27.alive) {
                  float var7 = Math.max(var27.x, Math.min(this.ballX, var27.x + 32.0F));
                  float var8 = Math.max(var27.y, Math.min(this.ballY, var27.y + 13.0F));
                  float var9 = this.ballX - var7;
                  float var10 = this.ballY - var8;
                  if (!(var9 * var9 + var10 * var10 > 20.25F)) {
                     float var11 = Math.min(this.ballX + 4.5F - var27.x, var27.x + 32.0F - (this.ballX - 4.5F));
                     float var12 = Math.min(this.ballY + 4.5F - var27.y, var27.y + 13.0F - (this.ballY - 4.5F));
                     if (var11 < var12) {
                        this.vx = this.ballX < var27.x + 16.0F ? -Math.abs(this.vx) : Math.abs(this.vx);
                     } else {
                        this.vy = this.ballY < var27.y + 6.5F ? -Math.abs(this.vy) : Math.abs(this.vy);
                     }

                     this.hit(var27);
                     break;
                  }
               }
            }

            if (this.ballY - 4.5F > 296.0F) {
               this.lose();
               return;
            }
         }

         if (this.bricks.stream().noneMatch(var0 -> var0.alive)) {
            this.nextLevel();
         }
      }
   }

   private void wallSound() {
      Sound.play("minecraft:block.note_block.hat", 1.8F, 0.12F);
   }

   private void hit(Breakout.Brick var1) {
      var1.hp--;
      var1.flash = 1.0F;
      if (var1.hp <= 0) {
         var1.alive = false;
         this.combo++;
         int var4 = (var1.maxHp > 1 ? 20 : 10) * this.level + (this.combo > 1 ? (this.combo - 1) * 2 : 0);
         this.score += var4;
         this.pops.add(new float[]{var1.x + 16.0F, var1.y + 6.5F, 0.0F, var4});

         for (int var3 = 0; var3 < 12; var3++) {
            this.particle(var1, var1.maxHp > 1 ? -6249047 : var1.color, 2.0F + this.rnd.nextFloat() * 1.8F);
         }

         Sound.play("minecraft:block.stone.break", 1.1F + Math.min(0.6F, this.combo * 0.06F), 0.3F);
      } else {
         Sound.play("minecraft:block.anvil.land", 1.9F, 0.12F);

         for (int var2 = 0; var2 < 4; var2++) {
            this.particle(var1, -6249047, 1.5F);
         }
      }
   }

   private void particle(Breakout.Brick var1, int var2, float var3) {
      this.parts
         .add(
            new float[]{
               var1.x + this.rnd.nextFloat() * 32.0F,
               var1.y + this.rnd.nextFloat() * 13.0F,
               (this.rnd.nextFloat() - 0.5F) * 160.0F,
               -30.0F - this.rnd.nextFloat() * 90.0F,
               0.0F,
               var2,
               var3
            }
         );
   }

   private void lose() {
      this.lives--;
      this.lifeFlash = 1.0F;
      Sound.play("minecraft:entity.arrow.hit_player", 0.7F, 0.4F);
      if (this.lives <= 0) {
         this.gameOver();
      } else {
         this.speed = 190.0F * (1.0F + 0.09F * (this.level - 1));
         this.stick();
      }
   }

   private void nextLevel() {
      this.level++;
      this.score = this.score + 50L * (this.level - 1);
      this.bannerT = 0.0F;
      Sound.play("minecraft:entity.player.levelup", 1.1F, 0.35F);
      this.build();
   }

   @Override
   protected void render() {
      float var1 = Math.min(0.05F, Gx.dt);
      this.levelAnim += var1;
      this.lifeFlash = Math.max(0.0F, this.lifeFlash - var1 * 1.5F);

      for (float var2 = 30.0F; var2 < 290.0F; var2 += 16.0F) {
         for (float var3 = 8.0F; var3 < 360.0F; var3 += 16.0F) {
            this.circle(var3, var2, 2.4F, Style.light ? 234881024 : 134217727);
         }
      }

      this.box(8.0F, 21.2F, 344.0F, 0.8F, 0.0F, Style.light ? 369098752 : 352321535);

      for (int var9 = 0; var9 < 3; var9++) {
         boolean var11 = var9 < this.lives;
         float var4 = var11 ? 1.0F : 0.85F;
         int var5 = var11 ? -45730 : (Style.light ? 855638016 : 872415231);
         if (!var11 && var9 == this.lives && this.lifeFlash > 0.0F) {
            var5 = Gx.mix(var5, -45730, this.lifeFlash);
         }

         Gx.icon(var11 ? "heart-fill" : "heart", this.X(12 + var9 * 15), this.Y(11.0F), 10.0F * this.u * var4, var5);
      }

      this.text("Level " + this.level, 330.0F, 11.0F, 7.5F, 3, Style.accent);
      int var10 = 0;

      for (Breakout.Brick var17 : this.bricks) {
         if (var17.alive) {
            var10++;
         }
      }

      this.text(var10 + " Steine", 180.0F, 11.0F, 6.8F, 2, Style.sub);

      for (Breakout.Brick var18 : this.bricks) {
         if (var18.alive) {
            float var22 = Ease.stagger(this.levelAnim, var18.row * 10 + var18.col, 0.012F, 0.35F);
            if (!(var22 <= 0.0F)) {
               float var6 = Ease.outBack(var22);
               int var7 = var18.maxHp > 1 ? (var18.hp > 1 ? -6249047 : Gx.mix(-6249047, -10854554, 0.35F)) : var18.color;
               var7 = Gx.mix(var7, -1, var18.flash * 0.6F);
               Gx.push();
               Gx.scaleAt(this.X(var18.x + 16.0F), this.Y(var18.y + 6.5F), var6);
               Gx.shadow(this.X(var18.x), this.Y(var18.y + 1.0F), this.S(32.0F), this.S(13.0F), this.S(2.5F), this.S(2.0F), 0.3F);
               this.brick(var18.x, var18.y, 32.0F, 13.0F, var7);
               if (var18.maxHp > 1 && var18.hp == 1) {
                  this.box(var18.x + 7.0F, var18.y + 3.0F, 1.0F, 7.0F, 0.5F, -2011160026);
                  this.box(var18.x + 7.0F, var18.y + 9.0F, 6.0F, 1.0F, 0.5F, -2011160026);
                  this.box(var18.x + 22.0F, var18.y + 2.0F, 1.0F, 5.0F, 0.5F, -2011160026);
                  this.box(var18.x + 18.0F, var18.y + 6.0F, 5.0F, 1.0F, 0.5F, -2011160026);
               }

               Gx.pop();
            }
         }
      }

      for (float[] var19 : this.parts) {
         float var23 = 1.0F - var19[4] / 0.8F;
         this.box(var19[0] - var19[6] / 2.0F, var19[1] - var19[6] / 2.0F, var19[6], var19[6], 0.6F, Gx.withAlpha((int)var19[5], var23));
      }

      for (float[] var20 : this.pops) {
         float var24 = 1.0F - Ease.outCubic(var20[2] / 0.8F);
         this.text("+" + (int)var20[3], var20[0], var20[1] - var20[2] * 22.0F, 7.5F, 3, Gx.withAlpha(-1, var24));
      }

      float var16 = this.padX - this.padW / 2.0F;
      float var21 = 268.0F + this.padHit * 1.5F;
      Gx.glow(this.X(this.padX), this.Y(var21 + 4.5F), this.S(this.padW * 0.7F), Gx.withAlpha(Style.accent, 0.18F + this.padHit * 0.2F));
      Gx.shadow(this.X(var16), this.Y(var21 + 1.0F), this.S(this.padW), this.S(9.0F), this.S(3.0F), this.S(3.0F), 0.4F);
      this.boxV(var16, var21, this.padW, 9.0F, 3.0F, Gx.mix(Style.accent, -1, 0.2F), Gx.mix(Style.accent, -16777216, 0.2F));
      Gx.outline(
         this.X(var16),
         this.Y(var21),
         this.S(this.padW),
         this.S(9.0F),
         this.S(3.0F),
         Math.max(1, this.S(0.5F)),
         Gx.withAlpha(Gx.mix(Style.accent, -16777216, 0.5F), 0.7F)
      );

      for (int var25 = 0; var25 < 6; var25++) {
         float var29 = var16 + this.padW * (var25 + 0.5F) / 6.0F;
         this.box(var29 - 3.0F, var21 - 2.4F, 6.0F, 3.0F, 1.0F, Gx.mix(Style.accent, -1, 0.12F));
      }

      this.box(var16 + 2.0F, var21 + 1.2F, this.padW - 4.0F, 1.2F, 0.6F, 1157627903);
      if (this.state != GameView.State.OVER || this.ballY < 290.0F) {
         if (!this.stuck) {
            for (int var26 = this.trail.length - 1; var26 >= 0; var26--) {
               float var30 = (1.0F - (float)var26 / this.trail.length) * 0.3F;
               this.circle(
                  this.trail[var26][0], this.trail[var26][1], 4.5F * (1.0F - (float)var26 / this.trail.length * 0.6F), Gx.withAlpha(Style.accent, var30)
               );
            }
         }

         Gx.glow(this.X(this.ballX), this.Y(this.ballY), this.S(18.0F), 1157627903);
         this.circle(this.ballX, this.ballY, 4.5F, -1);
         this.circle(this.ballX - 1.35F, this.ballY - 1.35F, 1.8000001F, -1);
         this.circle(this.ballX + 0.4F, this.ballY + 0.6F, 3.375F, -1578256);
         this.circle(this.ballX - 1.125F, this.ballY - 1.125F, 1.5749999F, -1);
      }

      if (this.stuck && this.state == GameView.State.PLAYING) {
         float var27 = 0.55F + 0.45F * (float)Math.sin(this.levelAnim * 5.0F);
         this.text("Leertaste oder Klick zum Abschießen", 180.0F, 234.0F, 7.0F, 3, Gx.withAlpha(Style.sub, var27));
      }

      if (this.lifeFlash > 0.0F) {
         this.box(0.0F, 0.0F, 360.0F, 290.0F, 12.0F, Gx.withAlpha(-45730, this.lifeFlash * 0.18F));
      }

      if (this.bannerT >= 0.0F) {
         float var28 = this.bannerT / 1.6F;
         float var31 = Ease.outBack(Math.min(1.0F, this.bannerT / 0.35F));
         float var33 = var28 < 0.75F ? 1.0F : 1.0F - (var28 - 0.75F) / 0.25F;
         float var8 = 174.0F;
         Gx.pushAlpha(var33);
         Gx.push();
         Gx.scaleAt(this.X(180.0F), this.Y(var8), var31);
         Gx.glow(this.X(180.0F), this.Y(var8), this.S(80.0F), Gx.withAlpha(Style.accent, 0.3F));
         this.box(120.0F, var8 - 16.0F, 120.0F, 32.0F, 12.0F, Gx.withAlpha(Style.accent, 0.95F));
         this.text("Level " + this.level, 180.0F, var8, 13.0F, 3, Style.accentText);
         Gx.pop();
         Gx.popAlpha();
      }
   }

   private static final class Brick {
      float x;
      float y;
      int hp;
      int maxHp;
      int color;
      int row;
      int col;
      float flash;
      boolean alive = true;
   }
}
