package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;

public final class MazeChase extends GameView {
   private static final String[] MAP = new String[]{
      "###################",
      "#........#........#",
      "#o##.###.#.###.##o#",
      "#.................#",
      "#.##.#.#####.#.##.#",
      "#....#...#...#....#",
      "####.###.#.###.####",
      "####.#.......#.####",
      "####.#.##-##.#.####",
      "    ...#GGG#...    ",
      "####.#.#####.#.####",
      "####.#.......#.####",
      "####.#.#####.#.####",
      "#........#........#",
      "#.##.###.#.###.##.#",
      "#o.#.....P.....#.o#",
      "##.#.#.#####.#.#.##",
      "#....#...#...#....#",
      "#.######.#.######.#",
      "#.................#",
      "###################"
   };
   private static final int C = 19;
   private static final int R = 21;
   private static final float T = 13.0F;
   private static final float W = 380.0F;
   private static final float H = 290.0F;
   private static final float OX = 66.5F;
   private static final float OY = 8.5F;
   private static final int[] DX = new int[]{1, 0, -1, 0};
   private static final int[] DY = new int[]{0, 1, 0, -1};
   private static final int[] HAT = new int[]{-1890757, -12616705, -14703780, -5157377};
   private static final String[] NAMES = new String[]{"Frosti", "Flocke", "Eisbert", "Kufe"};
   private static final int HOUSE = 0;
   private static final int LEAVING = 1;
   private static final int NORMAL = 2;
   private static final int EATEN = 3;
   private static final int ENTERING = 4;
   private static final int[][] CORNER = new int[][]{{17, -1}, {1, -1}, {17, 21}, {1, 21}};
   private final char[][] grid = new char[21][19];
   private final MazeChase.Ent player = new MazeChase.Ent();
   private final MazeChase.Ent[] ghosts = new MazeChase.Ent[4];
   private final GameFx fx = new GameFx();
   private int pellets;
   private int eatenChain;
   private int lives;
   private int level;
   private int pelletsEaten;
   private float frightT;
   private float modeT;
   private float anim;
   private float deathT = -1.0F;
   private float clearT = -1.0F;
   private float readyT;
   private float bonusT;
   private float chomp;
   private float hintT;
   private boolean chase;
   private int modeIdx;
   private static final float[] MODES = new float[]{7.0F, 20.0F, 7.0F, 20.0F, 5.0F, 9999.0F};

   public MazeChase() {
      super("maze");
   }

   @Override
   protected float boardW() {
      return 380.0F;
   }

   @Override
   protected float boardH() {
      return 290.0F;
   }

   @Override
   protected void reset() {
      this.lives = 3;
      this.level = 1;
      this.fx.clear();
      this.hintT = 0.0F;
      this.loadLevel();
   }

   private void loadLevel() {
      this.pellets = 0;
      this.pelletsEaten = 0;

      for (int var1 = 0; var1 < 21; var1++) {
         for (int var2 = 0; var2 < 19; var2++) {
            char var3 = MAP[var1].charAt(var2);
            this.grid[var1][var2] = var3;
            if (var3 == '.' || var3 == 'o') {
               this.pellets++;
            }
         }
      }

      this.bonusT = 0.0F;
      this.clearT = -1.0F;
      this.resetPositions();
   }

   private void resetPositions() {
      this.player.x = 9.0F;
      this.player.y = 15.0F;
      this.player.dir = 2;
      this.player.want = 2;
      this.player.moving = true;

      for (int var1 = 0; var1 < 4; var1++) {
         MazeChase.Ent var2 = this.ghosts[var1] == null ? (this.ghosts[var1] = new MazeChase.Ent()) : this.ghosts[var1];
         var2.x = var1 == 0 ? 9.0F : 7 + var1;
         var2.y = var1 == 0 ? 7.0F : 9.0F;
         var2.dir = 2;
         var2.mode = var1 == 0 ? 2 : 0;
         var2.houseT = var1 == 0 ? 0.0F : var1 * 3.2F / (1.0F + (this.level - 1) * 0.3F);
      }

      this.frightT = 0.0F;
      this.eatenChain = 0;
      this.modeT = 0.0F;
      this.modeIdx = 0;
      this.chase = false;
      this.deathT = -1.0F;
      this.readyT = 1.6F;
   }

   private boolean open(int var1, int var2, boolean var3) {
      if (var2 < 0 || var2 >= 21) {
         return false;
      } else if (var1 >= 0 && var1 < 19) {
         char var4 = this.grid[var2][var1];
         return var4 != '#' && (var4 != '-' || var3) && (var4 != 'G' || var3);
      } else {
         return var2 == 9;
      }
   }

   @Override
   protected void onKey(int var1) {
      int var2 = var1 == 262 || var1 == 68 ? 0 : (var1 == 264 || var1 == 83 ? 1 : (var1 != 263 && var1 != 65 ? (var1 != 265 && var1 != 87 ? -1 : 3) : 2));
      if (var2 >= 0) {
         this.player.want = var2;
         if (var2 == (this.player.dir + 2) % 4) {
            this.player.dir = var2;
            this.player.moving = true;
         }
      }
   }

   @Override
   protected void update(float var1) {
      this.fx.update(var1);
      this.hintT += var1;
      this.chomp += var1;
      if (this.readyT > 0.0F) {
         this.readyT -= var1;
      } else if (this.clearT >= 0.0F) {
         this.clearT += var1;
         if (this.clearT > 2.2F) {
            this.level++;
            this.loadLevel();
            this.fx.popup("Level " + this.level, 190.0F, 145.0F, -736942, 18.0F);
         }
      } else if (this.deathT >= 0.0F) {
         this.deathT += var1;
         if (this.deathT > 1.8F) {
            if (this.lives <= 0) {
               this.gameOver();
               return;
            }

            this.resetPositions();
         }
      } else {
         float var2 = Math.min(1.0F, (this.level - 1) / 6.0F);
         if (this.frightT > 0.0F) {
            this.frightT -= var1;
            if (this.frightT <= 0.0F) {
               this.eatenChain = 0;
            }
         } else {
            this.modeT += var1;
            if (this.modeT > MODES[this.modeIdx]) {
               this.modeT = 0.0F;
               this.modeIdx = Math.min(MODES.length - 1, this.modeIdx + 1);
               this.chase = this.modeIdx % 2 == 1;

               for (MazeChase.Ent var6 : this.ghosts) {
                  if (var6.mode == 2) {
                     var6.dir = (var6.dir + 2) % 4;
                  }
               }
            }
         }

         float var12 = (7.0F + var2 * 1.2F) * (this.frightT > 0.0F ? 1.1F : 1.0F);
         this.move(this.player, var12 * var1, -1);
         int var13 = Math.round(this.player.x);
         int var14 = Math.round(this.player.y);
         if (var13 >= 0 && var13 < 19 && Math.abs(this.player.x - var13) < 0.3F && Math.abs(this.player.y - var14) < 0.3F) {
            char var15 = this.grid[var14][var13];
            if (var15 == '.') {
               this.grid[var14][var13] = ' ';
               this.score += 10L;
               this.pellets--;
               this.pelletsEaten++;
               if (this.pelletsEaten % 2 == 0) {
                  Sound.play("minecraft:block.note_block.hat", 1.6F + this.pelletsEaten % 4 * 0.1F, 0.12F);
               }

               this.chomp = 0.0F;
            } else if (var15 == 'o') {
               this.grid[var14][var13] = ' ';
               this.score += 50L;
               this.pellets--;
               this.pelletsEaten++;
               this.frightT = Math.max(2.5F, 7.0F - (this.level - 1) * 0.8F);
               this.eatenChain = 0;

               for (MazeChase.Ent var10 : this.ghosts) {
                  if (var10.mode == 2) {
                     var10.dir = (var10.dir + 2) % 4;
                  }
               }

               this.fx.ring(this.cx(var13), this.cy(var14), 40.0F, -45715, 0.5F);
               this.fx.burst(this.cx(var13), this.cy(var14), 16, -1, 90.0F, 2.0F, 0.5F, 0.0F, 2);
               this.fx.shake(0.2F);
               this.fx.popup("Zuckerstange!", this.cx(var13), this.cy(var14) - 10.0F, -33892, 9.0F);
               Sound.play("minecraft:block.amethyst_block.chime", 1.0F, 0.9F);
            }

            if (this.pelletsEaten == 70 || this.pelletsEaten == 150) {
               this.bonusT = 9.0F;
            }

            if (this.bonusT > 0.0F
               && var13 == 9
               && var14 == 15
               && this.pelletsEaten > 0
               && Math.abs(this.player.x - 9.0F) < 0.4F
               && Math.abs(this.player.y - 15.0F) < 0.4F) {
               int var17 = 100 * this.level + 100;
               this.score += var17;
               this.bonusT = 0.0F;
               this.fx.confetti(this.cx(9.0F), this.cy(15.0F), 20, new int[]{-1890757, -736942, -14703780}, 120.0F, 3.0F);
               this.fx.popup("Geschenk! +" + var17, this.cx(9.0F), this.cy(15.0F) - 10.0F, -736942, 10.0F);
               Sound.play("minecraft:entity.player.levelup", 1.6F, 0.4F);
            }

            if (this.pellets <= 0) {
               this.clearT = 0.0F;
               Sound.play("minecraft:ui.toast.challenge_complete", 1.3F, 0.35F);
               this.fx.popup("Geschafft!", 190.0F, 135.0F, -4653233, 16.0F);
               return;
            }
         }

         this.bonusT = Math.max(0.0F, this.bonusT - var1);

         for (int var16 = 0; var16 < 4; var16++) {
            MazeChase.Ent var18 = this.ghosts[var16];
            switch (var18.mode) {
               case 0:
                  var18.houseT -= var1;
                  var18.y = 9.0F + (float)Math.sin(this.anim * 5.0F + var16) * 0.25F;
                  if (var18.houseT <= 0.0F) {
                     var18.mode = 1;
                     var18.y = 9.0F;
                  }
                  break;
               case 1:
                  float var20 = 3.5F * var1;
                  if (Math.abs(var18.x - 9.0F) > 0.01F) {
                     var18.x = var18.x + Math.signum(9.0F - var18.x) * Math.min(var20, Math.abs(9.0F - var18.x));
                  } else {
                     var18.y -= var20;
                     if (var18.y <= 7.0F) {
                        var18.y = 7.0F;
                        var18.x = 9.0F;
                        var18.mode = 2;
                        var18.dir = 2;
                     }
                  }
                  break;
               case 2:
               case 3:
               default:
                  boolean var19 = this.frightT > 0.0F && var18.mode == 2;
                  float var21 = var18.mode == 3 ? 15.0F : (var19 ? 4.2F : 6.4F + var2 * 1.4F);
                  if (Math.round(var18.y) == 9 && (var18.x < 4.0F || var18.x > 14.0F) && var18.mode != 3) {
                     var21 *= 0.55F;
                  }

                  this.move(var18, var21 * var1, var16);
                  if (var18.mode == 3 && Math.abs(var18.x - 9.0F) < 0.1F && Math.abs(var18.y - 7.0F) < 0.1F) {
                     var18.x = 9.0F;
                     var18.y = 7.0F;
                     var18.mode = 4;
                  }

                  if (var18.mode == 2 && Math.abs(var18.x - this.player.x) + Math.abs(var18.y - this.player.y) < 0.8F) {
                     if (!var19) {
                        this.die();
                        return;
                     }

                     var18.mode = 3;
                     this.eatenChain++;
                     int var11 = 100 << Math.min(4, this.eatenChain);
                     this.score += var11;
                     this.fx.burst(this.cx(var18.x), this.cy(var18.y), 26, -1, 140.0F, 2.6F, 0.6F, 60.0F, 4);
                     this.fx.ring(this.cx(var18.x), this.cy(var18.y), 26.0F, -6301441, 0.4F);
                     this.fx.popup("+" + var11, this.cx(var18.x), this.cy(var18.y) - 8.0F, -6301441, 11.0F);
                     this.fx.shake(0.3F);
                     Sound.play("minecraft:block.snow.break", 1.2F, 0.9F);
                     Sound.play("minecraft:entity.experience_orb.pickup", 0.8F + this.eatenChain * 0.2F, 0.4F);
                  }
                  break;
               case 4:
                  var18.y += 6.0F * var1;
                  if (var18.y >= 9.0F) {
                     var18.y = 9.0F;
                     var18.mode = 1;
                  }
            }
         }
      }
   }

   private void die() {
      this.deathT = 0.0F;
      this.lives--;
      this.fx.burst(this.cx(this.player.x), this.cy(this.player.y), 30, -3638726, 120.0F, 3.0F, 0.9F, 80.0F, 1);
      this.fx.burst(this.cx(this.player.x), this.cy(this.player.y), 12, -1, 90.0F, 2.0F, 0.7F, 0.0F, 2);
      this.fx.shake(0.8F);
      this.fx.flash(-50354, 0.5F);
      Sound.play("minecraft:entity.player.hurt_freeze", 0.8F, 0.6F);
   }

   private void move(MazeChase.Ent var1, float var2, int var3) {
      for (int var4 = 0; var2 > 1.0E-5F && var4 < 10; var4++) {
         int var5 = Math.round(var1.x);
         int var6 = Math.round(var1.y);
         if (Math.abs(var1.x - var5) < 0.001F && Math.abs(var1.y - var6) < 0.001F) {
            var1.x = var5;
            var1.y = var6;
            if (var3 < 0) {
               if (this.open(var5 + DX[var1.want], var6 + DY[var1.want], false)) {
                  var1.dir = var1.want;
               }

               if (!this.open(var5 + DX[var1.dir], var6 + DY[var1.dir], false)) {
                  var1.moving = false;
                  return;
               }

               var1.moving = true;
            } else {
               var1.dir = this.chooseDir(var1, var5, var6, var3);
            }
         }

         float var7 = DX[var1.dir] != 0 ? var1.x : var1.y;
         int var8 = DX[var1.dir] != 0 ? DX[var1.dir] : DY[var1.dir];
         float var9 = var8 > 0 ? (float)Math.floor(var7 + 0.001F) + 1.0F : (float)Math.ceil(var7 - 0.001F) - 1.0F;
         float var10 = Math.min(var2, Math.abs(var9 - var7));
         var1.x = var1.x + DX[var1.dir] * var10;
         var1.y = var1.y + DY[var1.dir] * var10;
         var2 -= var10;
         if (var1.x < -1.001F) {
            var1.x += 20.0F;
         }

         if (var1.x > 19.001F) {
            var1.x -= 20.0F;
         }
      }
   }

   private int chooseDir(MazeChase.Ent var1, int var2, int var3, int var4) {
      boolean var5 = this.frightT > 0.0F && var1.mode == 2;
      int var6;
      int var7;
      if (var1.mode == 3) {
         var6 = 9;
         var7 = 7;
      } else if (!this.chase) {
         var6 = CORNER[var4][0];
         var7 = CORNER[var4][1];
      } else {
         int var8 = Math.round(this.player.x);
         int var9 = Math.round(this.player.y);
         switch (var4) {
            case 1:
               var6 = var8 + DX[this.player.dir] * 4;
               var7 = var9 + DY[this.player.dir] * 4;
               break;
            case 2:
               float var18 = (var8 - var2) * (var8 - var2) + (var9 - var3) * (var9 - var3);
               if (var18 > 64.0F) {
                  var6 = var8;
                  var7 = var9;
               } else {
                  var6 = CORNER[2][0];
                  var7 = CORNER[2][1];
               }
               break;
            case 3:
               int var10 = var8 + DX[this.player.dir] * 2;
               int var11 = var9 + DY[this.player.dir] * 2;
               var6 = var10 * 2 - Math.round(this.ghosts[0].x);
               var7 = var11 * 2 - Math.round(this.ghosts[0].y);
               break;
            default:
               var6 = var8;
               var7 = var9;
         }
      }

      int var16 = -1;
      float var17 = Float.MAX_VALUE;
      int var19 = var5 ? this.rnd.nextInt(4) : 0;

      for (int var20 = 0; var20 < 4; var20++) {
         int var12 = var5 ? (var19 + var20) % 4 : new int[]{3, 2, 1, 0}[var20];
         if (var12 != (var1.dir + 2) % 4) {
            int var13 = var2 + DX[var12];
            int var14 = var3 + DY[var12];
            if (this.open(var13, var14, false)) {
               if (var5) {
                  return var12;
               }

               float var15 = (var13 - var6) * (var13 - var6) + (var14 - var7) * (var14 - var7);
               if (var15 < var17) {
                  var17 = var15;
                  var16 = var12;
               }
            }
         }
      }

      if (var16 < 0) {
         var16 = (var1.dir + 2) % 4;
      }

      return var16;
   }

   private float cx(float var1) {
      return 66.5F + var1 * 13.0F + 6.5F;
   }

   private float cy(float var1) {
      return 8.5F + var1 * 13.0F + 6.5F;
   }

   @Override
   protected void render() {
      float var1 = Math.min(0.05F, Gx.dt);
      this.anim += var1;
      this.boxV(0.0F, 0.0F, 380.0F, 290.0F, 0.0F, -16116944, -15457718);
      boolean var2 = this.clearT >= 0.0F && (int)(this.clearT * 6.0F) % 2 == 0 && this.clearT > 0.3F;
      GameFx.sprite(this, "mz_walls" + (var2 ? 1 : 0), 63.5F, 5.5F, 253.0F, 279.0F, 253.0F, 279.0F, var1x -> paintWalls(var1x, var2), -1);
      Gx.push();
      Gx.translate(this.S(this.fx.sx), this.S(this.fx.sy));

      for (int var3 = 0; var3 < 21; var3++) {
         for (int var4 = 0; var4 < 19; var4++) {
            char var5 = this.grid[var3][var4];
            if (var5 == '.') {
               this.circle(this.cx(var4), this.cy(var3), 1.35F, -722689);
            } else if (var5 == 'o') {
               float var6 = 1.0F + 0.12F * (float)Math.sin(this.anim * 6.0F + var4);
               Gx.glow(this.X(this.cx(var4)), this.Y(this.cy(var3)), this.S(11.0F * var6), 1442794861);
               GameFx.sprite(
                  this, "mz_cane", this.cx(var4) - 6.0F * var6, this.cy(var3) - 6.0F * var6, 12.0F * var6, 12.0F * var6, 24.0F, 24.0F, MazeChase::paintCane, -1
               );
            }
         }
      }

      if (this.bonusT > 0.0F) {
         float var7 = 1.0F + 0.08F * (float)Math.sin(this.anim * 8.0F);
         Gx.glow(this.X(this.cx(9.0F)), this.Y(this.cy(15.0F)), this.S(12.0F), 1442103634);
         GameFx.sprite(
            this,
            "sj_gift0",
            this.cx(9.0F) - 6.5F * var7,
            this.cy(15.0F) - 6.5F * var7,
            13.0F * var7,
            13.0F * var7,
            40.0F,
            40.0F,
            var0 -> SantaJump.paintGift(var0, 0),
            Gx.withAlpha(-1, Math.min(1.0F, this.bonusT))
         );
      }

      for (int var8 = 0; var8 < 4; var8++) {
         this.ghost(this.ghosts[var8], var8);
      }

      this.playerSprite();
      this.fx.draw(this, 0.0F, 0.0F);
      Gx.pop();
      this.text("LEVEL", 33.25F, 24.0F, 6.0F, 3, -1711276033);
      this.text(String.valueOf(this.level), 33.25F, 38.0F, 13.0F, 3, -736942);
      this.text("LEBEN", 33.25F, 62.0F, 6.0F, 3, -1711276033);

      for (int var9 = 0; var9 < this.lives; var9++) {
         GameFx.sprite(this, "mz_ginger0", 26.25F, 70 + var9 * 17, 14.0F, 14.0F, 30.0F, 30.0F, var0 -> paintGinger(var0, 0), -1);
      }

      float var10 = 346.75F;
      this.text("PERLEN", var10, 24.0F, 6.0F, 3, -1711276033);
      this.text(String.valueOf(this.pellets), var10, 38.0F, 11.0F, 3, -722689);
      if (this.frightT > 0.0F) {
         this.text("POWER", var10, 62.0F, 6.0F, 3, -1711276033);
         float var11 = this.frightT / Math.max(2.5F, 7.0F - (this.level - 1) * 0.8F);
         this.box(var10 - 3.0F, 70.0F, 6.0F, 60.0F, 3.0F, 1157627903);
         this.box(
            var10 - 3.0F, 70.0F + 60.0F * (1.0F - var11), 6.0F, 60.0F * var11, 3.0F, this.frightT < 2.0F && (int)(this.anim * 8.0F) % 2 == 0 ? -1 : -45715
         );
      }

      for (int var12 = 0; var12 < 4; var12++) {
         float var15 = 220.0F + var12 * 15;
         this.circle(var10 - 16.0F, var15, 3.0F, HAT[var12]);
         Gx.textMid(NAMES[var12], this.X(var10 - 10.0F), this.Y(var15), 5.8F * this.u, 2, -1426063361);
      }

      if (this.readyT > 0.0F && this.state == GameView.State.PLAYING) {
         float var13 = Math.min(1.0F, this.readyT * 2.0F);
         GameFx.outlined(this, "Bereit?", this.cx(9.0F), this.cy(11.0F), 12.0F, Gx.withAlpha(-736942, var13), Gx.withAlpha(-16777216, var13 * 0.8F));
      }

      if (this.state == GameView.State.PLAYING && this.hintT < 4.0F) {
         float var14 = Math.min(1.0F, 4.0F - this.hintT);
         this.text("Pfeiltasten / WASD", this.cx(9.0F), this.cy(13.0F) + 1.0F, 6.5F, 3, Gx.withAlpha(-1, var14 * 0.8F));
      }

      this.fx.drawPopups(this, 0.0F, 0.0F);
      this.fx.drawFlash(this, 380.0F, 290.0F);
   }

   private void ghost(MazeChase.Ent var1, int var2) {
      float var3 = this.cx(var1.x);
      float var4 = this.cy(var1.y) + (var1.mode == 2 ? (float)Math.sin(this.anim * 8.0F + var2) * 0.5F : 0.0F);
      float var5 = 17.0F;
      boolean var6 = this.frightT > 0.0F && (var1.mode == 2 || var1.mode == 0 || var1.mode == 1);
      if (var1.mode == 3 || var1.mode == 4) {
         int var9 = var1.dir;
         GameFx.sprite(
            this, "mz_eyes" + var9 + var2, var3 - var5 / 2.0F, var4 - var5 / 2.0F, var5, var5, 30.0F, 30.0F, var2x -> paintEyes(var2x, var9, HAT[var2]), -1
         );
      } else if (!var6) {
         int var8 = var1.dir;
         Gx.glow(this.X(var3), this.Y(var4), this.S(12.0F), Gx.withAlpha(HAT[var2], 0.18F));
         GameFx.sprite(
            this,
            "mz_snow" + var2 + var8,
            var3 - var5 / 2.0F,
            var4 - var5 / 2.0F - 1.0F,
            var5,
            var5,
            30.0F,
            30.0F,
            var2x -> paintSnowman(var2x, HAT[var2], var8, false, false),
            -1
         );
      } else {
         boolean var7 = this.frightT < 2.0F && (int)(this.anim * 8.0F) % 2 == 0;
         GameFx.sprite(
            this,
            "mz_fright" + (var7 ? 1 : 0),
            var3 - var5 / 2.0F,
            var4 - var5 / 2.0F - 1.0F,
            var5,
            var5,
            30.0F,
            30.0F,
            var1x -> paintSnowman(var1x, -12616705, 0, true, var7),
            -1
         );
      }
   }

   private void playerSprite() {
      float var1 = this.cx(this.player.x);
      float var2 = this.cy(this.player.y);
      float var3 = 16.0F;
      if (this.deathT >= 0.0F) {
         float var8 = Ease.clamp(this.deathT / 1.2F);
         if (!(var8 >= 1.0F)) {
            Gx.push();
            Gx.scaleAt(this.X(var1), this.Y(var2), 1.0F - var8);
            GameFx.sprite(
               this,
               "mz_ginger1",
               var1 - var3 / 2.0F,
               var2 - var3 / 2.0F,
               var3,
               var3,
               30.0F,
               30.0F,
               var0 -> paintGinger(var0, 1),
               Gx.withAlpha(-1, 1.0F - var8)
            );
            Gx.pop();
         }
      } else {
         int var4 = this.player.moving && this.chomp < 0.12F ? 1 : 0;
         float var5 = this.player.moving && this.state == GameView.State.PLAYING ? (float)Math.abs(Math.sin(this.anim * 14.0F)) * 1.2F : 0.0F;
         float var6 = DX[this.player.dir] * 1.2F;
         if (this.frightT > 0.0F) {
            Gx.glow(this.X(var1), this.Y(var2), this.S(14.0F), 1157582189);
         }

         GameFx.sprite(
            this, "mz_ginger" + var4, var1 - var3 / 2.0F + var6, var2 - var3 / 2.0F - var5, var3, var3, 30.0F, 30.0F, var1x -> paintGinger(var1x, var4), -1
         );
      }
   }

   private static void paintWalls(Graphics2D var0, boolean var1) {
      var0.translate(3, 3);
      Area var2 = new Area();

      for (int var3 = 0; var3 < 21; var3++) {
         for (int var4 = 0; var4 < 19; var4++) {
            if (MAP[var3].charAt(var4) == '#') {
               var2.add(new Area(new java.awt.geom.Rectangle2D.Float(var4 * 13.0F, var3 * 13.0F, 13.0F, 13.0F)));
            }
         }
      }

      var0.setColor(var1 ? new Color(3824288) : new Color(1452122));
      var0.fill(var2);
      var0.setStroke(new BasicStroke(5.0F, 1, 1));
      var0.setColor(new Color(159, 216, 255, var1 ? 120 : 45));
      var0.draw(var2);
      var0.setStroke(new BasicStroke(1.6F, 1, 1));
      var0.setColor(var1 ? Color.WHITE : new Color(10475775));
      var0.draw(var2);
      var0.setColor(new Color(244, 248, 255, 215));

      for (int var6 = 0; var6 < 21; var6++) {
         for (int var7 = 0; var7 < 19; var7++) {
            if (cap(var6, var7)) {
               int var5 = var7;

               while (var5 + 1 < 19 && cap(var6, var5 + 1)) {
                  var5++;
               }

               var0.fill(new java.awt.geom.RoundRectangle2D.Float(var7 * 13.0F + 1.0F, var6 * 13.0F + 0.9F, (var5 - var7 + 1) * 13.0F - 2.0F, 3.2F, 3.0F, 3.0F));
               var7 = var5;
            }
         }
      }

      var0.setColor(new Color(16743381));
      var0.fill(new java.awt.geom.Rectangle2D.Float(117.0F, 109.3F, 13.0F, 2.4F));
   }

   private static boolean cap(int var0, int var1) {
      return MAP[var0].charAt(var1) == '#' && (var0 == 0 || MAP[var0 - 1].charAt(var1) != '#');
   }

   private static void paintCane(Graphics2D var0) {
      var0.setStroke(new BasicStroke(5.0F, 1, 1));
      GeneralPath var1 = new GeneralPath();
      var1.moveTo(9.0F, 22.0F);
      var1.lineTo(9.0F, 9.0F);
      var1.curveTo(9.0F, 2.0F, 18.0F, 2.0F, 18.0F, 9.0F);
      var0.setColor(Color.WHITE);
      var0.draw(var1);
      var0.setColor(new Color(14886459));
      var0.setStroke(new BasicStroke(5.0F, 0, 1, 1.0F, new float[]{2.6F, 2.6F}, 0.0F));
      var0.draw(var1);
   }

   private static void paintGinger(Graphics2D var0, int var1) {
      Color var2 = new Color(13138234);
      Color var3 = new Color(9326368);
      Color var4 = Color.WHITE;
      Area var5 = GameFx.union(
         new java.awt.geom.Ellipse2D.Float(9.0F, 1.0F, 12.0F, 12.0F),
         new java.awt.geom.RoundRectangle2D.Float(10.0F, 11.0F, 10.0F, 11.0F, 5.0F, 5.0F),
         new java.awt.geom.RoundRectangle2D.Float(3.0F, 12.0F, 24.0F, 5.0F, 5.0F, 5.0F),
         new java.awt.geom.RoundRectangle2D.Float(9.0F, 18.0F, 5.0F, 11.0F, 5.0F, 5.0F),
         new java.awt.geom.RoundRectangle2D.Float(16.0F, 18.0F, 5.0F, 11.0F, 5.0F, 5.0F)
      );
      var0.setPaint(new GradientPaint(0.0F, 0.0F, var2, 0.0F, 30.0F, var3));
      var0.fill(var5);
      var0.setColor(new Color(6960400));
      var0.setStroke(new BasicStroke(0.8F));
      var0.draw(var5);
      var0.setColor(var4);
      var0.setStroke(new BasicStroke(1.1F, 1, 1));
      var0.drawPolyline(new int[]{4, 5, 6, 7}, new int[]{14, 15, 14, 15}, 4);
      var0.drawPolyline(new int[]{23, 24, 25, 26}, new int[]{14, 15, 14, 15}, 4);
      var0.drawLine(10, 26, 13, 26);
      var0.drawLine(17, 26, 20, 26);
      var0.setColor(new Color(1776418));
      var0.fill(new java.awt.geom.Ellipse2D.Float(11.6F, 4.5F, 2.2F, 2.4F));
      var0.fill(new java.awt.geom.Ellipse2D.Float(16.2F, 4.5F, 2.2F, 2.4F));
      if (var1 == 1) {
         var0.setColor(new Color(5905940));
         var0.fill(new java.awt.geom.Ellipse2D.Float(12.5F, 8.0F, 5.0F, 3.4F));
      } else {
         var0.setColor(var4);
         var0.drawArc(12, 7, 6, 3, 200, 140);
      }

      var0.setColor(new Color(14886459));
      var0.fill(new java.awt.geom.Ellipse2D.Float(13.8F, 13.0F, 2.6F, 2.6F));
      var0.setColor(new Color(2073436));
      var0.fill(new java.awt.geom.Ellipse2D.Float(13.8F, 17.0F, 2.6F, 2.6F));
   }

   private static void paintSnowman(Graphics2D var0, int var1, int var2, boolean var3, boolean var4) {
      Color var5 = var3 ? (var4 ? Color.WHITE : new Color(8366335)) : Color.WHITE;
      Color var6 = var3 ? (var4 ? new Color(13228786) : new Color(3102152)) : new Color(12176875);
      Area var7 = GameFx.union(new java.awt.geom.Ellipse2D.Float(5.0F, 12.0F, 20.0F, 16.0F), new java.awt.geom.Ellipse2D.Float(8.0F, 3.0F, 14.0F, 13.0F));
      if (var3) {
         var7.add(new Area(new java.awt.geom.RoundRectangle2D.Float(4.0F, 22.0F, 22.0F, 7.0F, 6.0F, 6.0F)));
      }

      var0.setPaint(new GradientPaint(0.0F, 4.0F, var5, 0.0F, 28.0F, var6));
      var0.fill(var7);
      if (var3) {
         var0.setColor(var6);
         var0.fill(new java.awt.geom.Ellipse2D.Float(6.0F, 26.0F, 4.0F, 4.0F));
         var0.fill(new java.awt.geom.Ellipse2D.Float(14.0F, 27.0F, 3.0F, 3.0F));
         var0.fill(new java.awt.geom.Ellipse2D.Float(21.0F, 26.0F, 4.0F, 4.0F));
         var0.setColor(var4 ? new Color(14886459) : Color.WHITE);
         var0.setStroke(new BasicStroke(1.2F, 1, 1));
         var0.fill(new java.awt.geom.Ellipse2D.Float(11.0F, 8.0F, 2.6F, 2.6F));
         var0.fill(new java.awt.geom.Ellipse2D.Float(17.0F, 8.0F, 2.6F, 2.6F));
         var0.drawPolyline(new int[]{10, 12, 14, 16, 18, 20}, new int[]{14, 13, 14, 13, 14, 13}, 6);
      } else {
         var0.setColor(new Color(var1));
         var0.fill(new java.awt.geom.RoundRectangle2D.Float(7.5F, 13.5F, 15.0F, 3.4F, 3.0F, 3.0F));
         var0.fill(new java.awt.geom.RoundRectangle2D.Float(17.0F, 15.0F, 3.4F, 7.0F, 2.0F, 2.0F));
         float var8 = DX[var2] * 1.2F;
         float var9 = DY[var2] * 1.0F;
         var0.setColor(new Color(1776418));
         var0.fill(new java.awt.geom.Ellipse2D.Float(11.0F + var8, 7.0F + var9, 2.8F, 2.8F));
         var0.fill(new java.awt.geom.Ellipse2D.Float(16.2F + var8, 7.0F + var9, 2.8F, 2.8F));
         var0.setStroke(new BasicStroke(1.0F, 1, 1));
         var0.drawLine(10, 6, 13, 7);
         var0.drawLine(20, 6, 17, 7);
         var0.setColor(new Color(16747038));
         int var10 = var2 == 0 ? 1 : (var2 == 2 ? -1 : 0);
         var0.fillPolygon(new int[]{14, 16, 15 + var10 * 6}, new int[]{10, 10, 12 + (var2 == 1 ? 2 : 0)}, 3);
         var0.setColor(new Color(2763312));
         var0.fill(new java.awt.geom.Ellipse2D.Float(14.0F, 19.0F, 2.0F, 2.0F));
         var0.fill(new java.awt.geom.Ellipse2D.Float(14.0F, 23.0F, 2.0F, 2.0F));
         var0.setColor(new Color(1776418));
         var0.fill(new java.awt.geom.RoundRectangle2D.Float(7.5F, 3.5F, 15.0F, 2.4F, 1.0F, 1.0F));
         var0.fill(new java.awt.geom.RoundRectangle2D.Float(10.0F, -0.5F, 10.0F, 5.0F, 1.0F, 1.0F));
         var0.setColor(new Color(var1));
         var0.fill(new java.awt.geom.Rectangle2D.Float(10.0F, 2.4F, 10.0F, 1.6F));
      }
   }

   private static void paintEyes(Graphics2D var0, int var1, int var2) {
      float var3 = DX[var1] * 1.2F;
      float var4 = DY[var1] * 1.0F;
      var0.setColor(Color.WHITE);
      var0.fill(new java.awt.geom.Ellipse2D.Float(9.0F, 8.0F, 6.0F, 6.0F));
      var0.fill(new java.awt.geom.Ellipse2D.Float(15.5F, 8.0F, 6.0F, 6.0F));
      var0.setColor(new Color(1776418));
      var0.fill(new java.awt.geom.Ellipse2D.Float(10.5F + var3, 9.5F + var4, 3.0F, 3.0F));
      var0.fill(new java.awt.geom.Ellipse2D.Float(17.0F + var3, 9.5F + var4, 3.0F, 3.0F));
      var0.setColor(new Color(16747038));
      var0.fillPolygon(new int[]{14, 16, 15}, new int[]{14, 14, 19}, 3);
      var0.setColor(new Color(1776418));
      var0.fill(new java.awt.geom.RoundRectangle2D.Float(10.0F, 1.0F, 10.0F, 5.0F, 1.0F, 1.0F));
      var0.fill(new java.awt.geom.RoundRectangle2D.Float(7.5F, 5.0F, 15.0F, 2.0F, 1.0F, 1.0F));
      var0.setColor(new Color(var2));
      var0.fill(new java.awt.geom.Rectangle2D.Float(10.0F, 3.8F, 10.0F, 1.4F));
      var0.setPaint(
         new RadialGradientPaint(
            new java.awt.geom.Point2D.Float(15.0F, 15.0F),
            10.0F,
            new float[]{0.0F, 1.0F},
            new Color[]{new Color(255, 255, 255, 40), new Color(255, 255, 255, 0)}
         )
      );
      var0.fill(new java.awt.geom.Ellipse2D.Float(5.0F, 5.0F, 20.0F, 20.0F));
   }

   private static final class Ent {
      float x;
      float y;
      int dir = 2;
      int want = 2;
      int mode;
      float houseT;
      float speedMul = 1.0F;
      boolean moving = true;
   }
}
