package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;

public final class Game2048 extends GameView {
   private static final int N = 4;
   private static final float GX = 12.0F;
   private static final float GY = 12.0F;
   private static final float GRID = 256.0F;
   private static final float GAP = 8.0F;
   private static final float CS = 54.0F;
   private static final float MOVE_T = 0.12F;
   private Game2048.Tile[][] grid = new Game2048.Tile[4][4];
   private final List<Game2048.Tile> ghosts = new ArrayList<>();
   private final List<float[]> floats = new ArrayList<>();
   private int moves;
   private int best;
   private boolean won;
   private float winT = -1.0F;
   private float overDelay = -1.0F;
   private float shake;

   public Game2048() {
      super("2048");
   }

   @Override
   protected float boardW() {
      return 364.0F;
   }

   @Override
   protected float boardH() {
      return 280.0F;
   }

   @Override
   protected String overTitle() {
      return "Keine Züge mehr";
   }

   @Override
   protected void reset() {
      this.grid = new Game2048.Tile[4][4];
      this.ghosts.clear();
      this.floats.clear();
      this.moves = 0;
      this.best = 0;
      this.won = false;
      this.winT = -1.0F;
      this.overDelay = -1.0F;
      this.shake = 0.0F;
      this.spawn();
      this.spawn();
   }

   private void spawn() {
      ArrayList var1 = new ArrayList();

      for (int var2 = 0; var2 < 4; var2++) {
         for (int var3 = 0; var3 < 4; var3++) {
            if (this.grid[var2][var3] == null) {
               var1.add(new int[]{var2, var3});
            }
         }
      }

      if (!var1.isEmpty()) {
         int[] var4 = (int[])var1.get(this.rnd.nextInt(var1.size()));
         Game2048.Tile var5 = new Game2048.Tile(this.rnd.nextFloat() < 0.9F ? 2 : 4, var4[0], var4[1]);
         var5.pop = -0.6F;
         this.grid[var4[0]][var4[1]] = var5;
         this.best = Math.max(this.best, var5.v);
      }
   }

   @Override
   protected void onKey(int var1) {
      if (!(this.overDelay >= 0.0F)) {
         byte var2 = 0;
         byte var3 = 0;
         if (var1 == 263 || var1 == 65) {
            var3 = -1;
         } else if (var1 == 262 || var1 == 68) {
            var3 = 1;
         } else if (var1 != 265 && var1 != 87) {
            if (var1 != 264 && var1 != 83) {
               return;
            }

            var2 = 1;
         } else {
            var2 = -1;
         }

         this.move(var2, var3);
      }
   }

   private void finishAnims() {
      this.ghosts.clear();

      for (Game2048.Tile[] var4 : this.grid) {
         for (Game2048.Tile var8 : var4) {
            if (var8 != null) {
               var8.move = 1.0F;
               var8.pop = Math.max(var8.pop, 1.0F);
            }
         }
      }
   }

   private void move(int var1, int var2) {
      this.finishAnims();
      boolean var3 = false;
      int var4 = 0;

      for (Game2048.Tile[] var8 : this.grid) {
         for (Game2048.Tile var12 : var8) {
            if (var12 != null) {
               var12.fr = var12.r;
               var12.fc = var12.c;
               var12.merged = false;
            }
         }
      }

      for (int var16 = 0; var16 < 4; var16++) {
         int var18 = 0;
         Game2048.Tile var20 = null;

         for (int var22 = 0; var22 < 4; var22++) {
            int var24 = var1 <= 0 && var2 <= 0 ? var22 : 3 - var22;
            int var26 = var1 != 0 ? var24 : var16;
            int var28 = var2 != 0 ? var24 : var16;
            Game2048.Tile var30 = this.grid[var26][var28];
            if (var30 != null) {
               this.grid[var26][var28] = null;
               if (var20 != null && var20.v == var30.v && !var20.merged) {
                  var20.v *= 2;
                  var20.merged = true;
                  var20.pop = -0.2F;
                  var4 += var20.v;
                  this.best = Math.max(this.best, var20.v);
                  var30.r = var20.r;
                  var30.c = var20.c;
                  var30.dying = true;
                  var30.move = 0.0F;
                  this.ghosts.add(var30);
                  var3 = true;
               } else {
                  int var13 = var1 <= 0 && var2 <= 0 ? var18 : 3 - var18;
                  int var14 = var1 != 0 ? var13 : var16;
                  int var15 = var2 != 0 ? var13 : var16;
                  if (var14 != var26 || var15 != var28) {
                     var3 = true;
                  }

                  var30.r = var14;
                  var30.c = var15;
                  var30.move = 0.0F;
                  this.grid[var14][var15] = var30;
                  var20 = var30;
                  var18++;
               }
            }
         }
      }

      if (var3) {
         this.moves++;
         this.score += var4;
         if (var4 > 0) {
            this.floats.add(new float[]{var4, 0.0F});
            Sound.play("minecraft:block.note_block.pling", 0.9F + Math.min(1.0F, (float)(Math.log(var4) / Math.log(2.0)) / 11.0F), 0.25F);
         } else {
            Sound.play("minecraft:block.wood.hit", 1.4F, 0.18F);
         }

         this.spawn();
         if (!this.won && this.best >= 2048) {
            this.won = true;
            this.winT = 0.0F;
            Sound.play("minecraft:ui.toast.challenge_complete", 1.1F, 0.35F);
         }

         if (!this.canMove()) {
            this.overDelay = 0.0F;
         }
      } else {
         this.shake = 1.0F;

         for (Game2048.Tile[] var23 : this.grid) {
            for (Game2048.Tile var31 : var23) {
               if (var31 != null) {
                  var31.move = 1.0F;
               }
            }
         }
      }
   }

   private boolean canMove() {
      for (int var1 = 0; var1 < 4; var1++) {
         for (int var2 = 0; var2 < 4; var2++) {
            Game2048.Tile var3 = this.grid[var1][var2];
            if (var3 == null) {
               return true;
            }

            if (var2 + 1 < 4 && this.grid[var1][var2 + 1] != null && this.grid[var1][var2 + 1].v == var3.v) {
               return true;
            }

            if (var1 + 1 < 4 && this.grid[var1 + 1][var2] != null && this.grid[var1 + 1][var2].v == var3.v) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   protected void update(float var1) {
      if (this.winT >= 0.0F) {
         this.winT += var1;
      }

      if (this.winT > 3.2F) {
         this.winT = -1.0F;
      }

      for (float[] var3 : this.floats) {
         var3[1] += var1;
      }

      this.floats.removeIf(var0 -> var0[1] > 0.9F);
      if (this.overDelay >= 0.0F) {
         this.overDelay += var1;
         if (this.overDelay > 0.7F) {
            this.gameOver();
         }
      }
   }

   private static int tileColor(int var0) {
      switch (var0) {
         case 2:
            return -1778229;
         case 4:
            return -864969;
         case 8:
            return -95720;
         case 16:
            return -1550805;
         case 32:
            return -3597815;
         case 64:
            return -3125096;
         case 128:
            return -7194248;
         case 256:
            return -16755265;
         case 512:
            return -13193537;
         case 1024:
            return -11821238;
         case 2048:
            return -15043;
         default:
            return -13945792;
      }
   }

   private float cellX(float var1) {
      return 20.0F + var1 * 62.0F;
   }

   private float cellY(float var1) {
      return 20.0F + var1 * 62.0F;
   }

   private void drawTile(Game2048.Tile var1) {
      float var2 = Ease.outCubic(var1.move);
      float var3 = var1.fr + (var1.r - var1.fr) * var2;
      float var4 = var1.fc + (var1.c - var1.fc) * var2;
      float var5 = this.cellX(var4);
      float var6 = this.cellY(var3);
      float var7 = 1.0F;
      if (var1.pop < 1.0F) {
         float var8 = Math.max(0.0F, var1.pop);
         var7 = var1.merged ? 1.0F + 0.18F * (float)Math.sin(var8 * Math.PI) : Ease.outBack(var8);
         if (!var1.merged && var1.pop <= 0.0F) {
            return;
         }
      }

      int var16 = tileColor(var1.v);
      boolean var9 = Style.brightness(var16) > 0.6;
      int var10 = var9 ? -12963034 : -1;
      Gx.push();
      Gx.scaleAt(this.X(var5 + 27.0F), this.Y(var6 + 27.0F), var7);
      if (var1.v >= 128) {
         Gx.glow(
            this.X(var5 + 27.0F), this.Y(var6 + 27.0F), this.S(54.0F * (var1.v >= 2048 ? 1.1F : 0.8F)), Gx.withAlpha(var16, var1.v >= 2048 ? 0.55F : 0.25F)
         );
      }

      Gx.shadow(this.X(var5), this.Y(var6 + 1.5F), this.S(54.0F), this.S(54.0F), this.S(8.0F), this.S(3.0F), 0.35F);
      this.boxV(var5, var6, 54.0F, 54.0F, 8.0F, Gx.mix(var16, -1, 0.16F), Gx.mix(var16, -16777216, 0.14F));
      Gx.outline(
         this.X(var5), this.Y(var6), this.S(54.0F), this.S(54.0F), this.S(8.0F), Math.max(1, this.S(0.9F)), Gx.withAlpha(Gx.mix(var16, -16777216, 0.5F), 0.6F)
      );
      float var11 = 4.2F;
      float var12 = 10.5F;

      for (int var13 = 0; var13 < 4; var13++) {
         float var14 = var13 % 2 == 0 ? var5 + var12 : var5 + 54.0F - var12;
         float var15 = var13 < 2 ? var6 + var12 : var6 + 54.0F - var12;
         this.circle(var14, var15 + 0.8F, var11, Gx.mix(var16, -16777216, 0.2F));
         this.circle(var14, var15, var11 * 0.88F, Gx.mix(var16, -1, 0.2F));
      }

      String var17 = String.valueOf(var1.v);
      float var18 = var17.length() <= 2 ? 22.0F : (var17.length() == 3 ? 19.0F : (var17.length() == 4 ? 15.5F : 12.5F));
      this.text(var17, var5 + 27.0F + 0.4F, var6 + 27.0F + 1.2F, var18, 3, Gx.withAlpha(-16777216, var9 ? 0.08F : 0.25F));
      this.text(var17, var5 + 27.0F, var6 + 27.0F, var18, 3, var10);
      Gx.pop();
   }

   @Override
   protected void render() {
      float var1 = Math.min(0.05F, Gx.dt);

      for (Game2048.Tile var3 : this.ghosts) {
         var3.move = Math.min(1.0F, var3.move + var1 / 0.12F);
      }

      this.ghosts.removeIf(var0 -> var0.move >= 1.0F);

      for (Game2048.Tile[] var5 : this.grid) {
         for (Game2048.Tile var9 : var5) {
            if (var9 != null) {
               var9.move = Math.min(1.0F, var9.move + var1 / 0.12F);
               if (var9.pop < 1.0F) {
                  var9.pop = Math.min(1.0F, var9.pop + var1 / 0.16F);
               }
            }
         }
      }

      this.shake = Math.max(0.0F, this.shake - var1 * 4.0F);
      float var18 = this.shake > 0.0F ? (float)Math.sin(this.shake * 30.0F) * 2.5F * this.shake : 0.0F;
      Gx.push();
      Gx.translate(this.S(var18), 0.0F);
      Gx.shadow(this.X(12.0F), this.Y(12.0F), this.S(256.0F), this.S(256.0F), this.S(12.0F), this.S(6.0F), 0.4F);
      this.boxV(12.0F, 12.0F, 256.0F, 256.0F, 12.0F, Style.light ? -2894115 : -15065819, Style.light ? -3617580 : -15460836);

      for (int var20 = 0; var20 < 4; var20++) {
         for (int var25 = 0; var25 < 4; var25++) {
            float var30 = this.cellX(var25);
            float var35 = this.cellY(var20);
            this.box(var30, var35, 54.0F, 54.0F, 8.0F, Style.light ? 369098752 : 218103807);

            for (int var40 = 0; var40 < 4; var40++) {
               float var45 = var40 % 2 == 0 ? var30 + 10.5F : var30 + 54.0F - 10.5F;
               float var49 = var40 < 2 ? var35 + 10.5F : var35 + 54.0F - 10.5F;
               this.circle(var45, var49, 3.4F, Style.light ? 268435456 : 150994943);
            }
         }
      }

      for (Game2048.Tile var26 : this.ghosts) {
         this.drawTile(var26);
      }

      for (Game2048.Tile[] var36 : this.grid) {
         for (Game2048.Tile var10 : var36) {
            if (var10 != null && !var10.merged) {
               this.drawTile(var10);
            }
         }
      }

      for (Game2048.Tile[] var37 : this.grid) {
         for (Game2048.Tile var53 : var37) {
            if (var53 != null && var53.merged) {
               this.drawTile(var53);
            }
         }
      }

      Gx.pop();
      float var24 = 280.0F;
      float var29 = this.boardW() - var24 - 12.0F;
      this.panel(var24, 12.0F, var29, 58.0F, "HÖCHSTER STEIN", String.valueOf(this.best), tileColor(this.best));
      this.panel(var24, 78.0F, var29, 58.0F, "ZÜGE", String.valueOf(this.moves), Style.accent);

      for (float[] var38 : this.floats) {
         float var43 = 1.0F - Ease.outCubic(var38[1] / 0.9F);
         this.text("+" + (int)var38[0], var24 + var29 / 2.0F, 162.0F - var38[1] * 26.0F, 12.0F, 3, Gx.withAlpha(Style.accent, var43));
      }

      float var34 = 192.0F;
      this.box(var24, var34, var29, 76.0F, 10.0F, Style.light ? 201326592 : 184549375);
      this.text("ZIEL", var24 + var29 / 2.0F, var34 + 14.0F, 6.5F, 3, Style.muted);
      float var39 = 34.0F;
      float var44 = var24 + (var29 - var39) / 2.0F;
      float var48 = var34 + 24.0F;
      this.boxV(var44, var48, var39, var39, 6.0F, Gx.mix(-15043, -1, 0.16F), Gx.mix(-15043, -16777216, 0.14F));
      this.text("2048", var44 + var39 / 2.0F, var48 + var39 / 2.0F, 8.5F, 3, -12963034);
      if (this.won) {
         this.text("geschafft!", var24 + var29 / 2.0F, var34 + 67.0F, 6.5F, 3, -15043);
      }

      if (this.winT >= 0.0F) {
         float var52 = this.winT < 2.6F ? 1.0F : 1.0F - (this.winT - 2.6F) / 0.6F;
         float var54 = Ease.outBack(Math.min(1.0F, this.winT / 0.45F));
         float var11 = 140.0F;
         float var12 = 140.0F;
         Gx.pushAlpha(var52);
         this.box(12.0F, 12.0F, 256.0F, 256.0F, 12.0F, Gx.rgba(0, 0.6F));
         Gx.glow(this.X(var11), this.Y(var12), this.S(110.0F), 1728038205);
         Gx.push();
         Gx.scaleAt(this.X(var11), this.Y(var12), var54);
         this.text("2048!", var11 + 1.0F, var12 - 4.0F, 40.0F, 3, 1711276032);
         this.text("2048!", var11, var12 - 6.0F, 40.0F, 3, -15043);
         this.text("Weiter geht's!", var11, var12 + 26.0F, 10.0F, 2, -1);
         Gx.pop();

         for (int var13 = 0; var13 < 18; var13++) {
            double var14 = var13 / 18.0 * Math.PI * 2.0 + this.winT;
            float var16 = 40.0F + this.winT * 60.0F;
            this.circle(
               var11 + (float)Math.cos(var14) * var16,
               var12 + (float)Math.sin(var14) * var16,
               2.5F,
               Gx.withAlpha(tileColor(2 << var13 % 10), 1.0F - this.winT / 3.2F)
            );
         }

         Gx.popAlpha();
      }
   }

   private void panel(float var1, float var2, float var3, float var4, String var5, String var6, int var7) {
      this.box(var1, var2, var3, var4, 10.0F, Style.light ? 201326592 : 184549375);
      this.box(var1, var2 + 12.0F, 3.0F, var4 - 24.0F, 1.5F, var7);
      this.text(var5, var1 + var3 / 2.0F, var2 + 16.0F, 6.5F, 3, Style.muted);
      this.text(var6, var1 + var3 / 2.0F, var2 + 38.0F, 16.0F, 3, Style.text);
   }

   private static final class Tile {
      int v;
      int r;
      int c;
      float fr;
      float fc;
      float move = 1.0F;
      float pop = 1.0F;
      boolean merged;
      boolean dying;

      Tile(int var1, int var2, int var3) {
         this.v = var1;
         this.r = var2;
         this.c = var3;
         this.fr = var2;
         this.fc = var3;
      }
   }
}
