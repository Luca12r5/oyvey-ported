package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Random;

public final class Sudoku extends GameView {
   private static final float W = 400.0F;
   private static final float H = 300.0F;
   private static final float CS = 29.0F;
   private static final float BG = 3.0F;
   private static final float GX = 14.0F;
   private static final float GY = 14.0F;
   private static final float PX = 300.0F;
   private static final float GS = 267.0F;
   private static final int SEL = -12604929;
   private static final String[] DIFF = new String[]{"Leicht", "Mittel", "Schwer", "Experte"};
   private static final int[] CLUES = new int[]{40, 32, 27, 23};
   private static final int[] BASE = new int[]{1000, 2000, 3500, 5000};
   private static final int[] DIFF_COL = new int[]{-11872659, -12604929, -20434, -45730};
   private static final int MAX_MISTAKES = 3;
   private final int[] sol = new int[81];
   private final int[] cell = new int[81];
   private final boolean[] given = new boolean[81];
   private final int[] notes = new int[81];
   private final float[] pop = new float[81];
   private final float[] wave = new float[81];
   private final float[] wrongT = new float[81];
   private final ArrayDeque<int[]> undo = new ArrayDeque<>();
   private final GameFx fx = new GameFx();
   private int diff = -1;
   private int sel = 40;
   private int mistakes;
   private int hints;
   private float elapsed;
   private float anim;
   private float winT = -1.0F;
   private boolean noteMode;
   private boolean lost;

   public Sudoku() {
      super("sudoku");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 300.0F;
   }

   @Override
   protected String overTitle() {
      return this.lost ? "Zu viele Fehler" : "Gelöst!";
   }

   @Override
   protected String scoreLabel() {
      return "Punkte";
   }

   @Override
   protected void reset() {
      this.diff = -1;
      this.fx.clear();
      Arrays.fill(this.cell, 0);
      Arrays.fill(this.notes, 0);
      Arrays.fill(this.given, false);
      this.undo.clear();
      this.mistakes = 0;
      this.hints = 0;
      this.elapsed = 0.0F;
      this.winT = -1.0F;
      this.lost = false;
      this.noteMode = false;
      this.sel = 40;
   }

   private void newPuzzle(int var1) {
      this.diff = var1;
      Random var2 = new Random();
      Arrays.fill(this.sol, 0);
      fill(this.sol, 0, var2);
      System.arraycopy(this.sol, 0, this.cell, 0, 81);
      int[] var3 = new int[81];
      int var4 = 0;

      while (var4 < 81) {
         var3[var4] = var4++;
      }

      for (int var9 = 80; var9 > 0; var9--) {
         int var5 = var2.nextInt(var9 + 1);
         int var6 = var3[var9];
         var3[var9] = var3[var5];
         var3[var5] = var6;
      }

      var4 = 81;
      int[] var11 = new int[81];

      for (int var12 = 0; var12 < 81 && var4 > CLUES[var1]; var12++) {
         int var7 = var3[var12];
         int var8 = this.cell[var7];
         this.cell[var7] = 0;
         System.arraycopy(this.cell, 0, var11, 0, 81);
         if (count(var11, 2) != 1) {
            this.cell[var7] = var8;
         } else {
            var4--;
         }
      }

      for (int var13 = 0; var13 < 81; var13++) {
         this.given[var13] = this.cell[var13] != 0;
         this.notes[var13] = 0;
         this.pop[var13] = 0.0F;
         this.wave[var13] = Ease.clamp((var13 / 9 + var13 % 9) * 0.04F) * -1.0F;
         this.wrongT[var13] = 0.0F;
      }

      this.undo.clear();
      this.mistakes = 0;
      this.hints = 0;
      this.elapsed = 0.0F;
      this.sel = 40;
      Sound.play("minecraft:block.note_block.chime", 1.2F, 0.4F);
   }

   private static boolean fill(int[] var0, int var1, Random var2) {
      if (var1 == 81) {
         return true;
      } else if (var0[var1] != 0) {
         return fill(var0, var1 + 1, var2);
      } else {
         int[] var3 = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};

         for (int var4 = 8; var4 > 0; var4--) {
            int var5 = var2.nextInt(var4 + 1);
            int var6 = var3[var4];
            var3[var4] = var3[var5];
            var3[var5] = var6;
         }

         for (int var7 : var3) {
            if (ok(var0, var1, var7)) {
               var0[var1] = var7;
               if (fill(var0, var1 + 1, var2)) {
                  return true;
               }

               var0[var1] = 0;
            }
         }

         return false;
      }
   }

   private static boolean ok(int[] var0, int var1, int var2) {
      int var3 = var1 / 9;
      int var4 = var1 % 9;
      int var5 = var3 / 3 * 3;
      int var6 = var4 / 3 * 3;

      for (int var7 = 0; var7 < 9; var7++) {
         if (var0[var3 * 9 + var7] == var2 || var0[var7 * 9 + var4] == var2) {
            return false;
         }

         if (var0[(var5 + var7 / 3) * 9 + var6 + var7 % 3] == var2) {
            return false;
         }
      }

      return true;
   }

   private static int cand(int[] var0, int var1) {
      int var2 = var1 / 9;
      int var3 = var1 % 9;
      int var4 = var2 / 3 * 3;
      int var5 = var3 / 3 * 3;
      int var6 = 0;

      for (int var7 = 0; var7 < 9; var7++) {
         var6 |= 1 << var0[var2 * 9 + var7] | 1 << var0[var7 * 9 + var3] | 1 << var0[(var4 + var7 / 3) * 9 + var5 + var7 % 3];
      }

      return ~var6 & 1022;
   }

   private static int count(int[] var0, int var1) {
      int var2 = -1;
      int var3 = 0;
      int var4 = 10;

      for (int var5 = 0; var5 < 81; var5++) {
         if (var0[var5] == 0) {
            int var6 = cand(var0, var5);
            int var7 = Integer.bitCount(var6);
            if (var7 == 0) {
               return 0;
            }

            if (var7 < var4) {
               var4 = var7;
               var2 = var5;
               var3 = var6;
               if (var7 == 1) {
                  break;
               }
            }
         }
      }

      if (var2 < 0) {
         return 1;
      } else {
         int var8 = 0;

         for (int var9 = 1; var9 <= 9; var9++) {
            if ((var3 & 1 << var9) != 0) {
               var0[var2] = var9;
               var8 += count(var0, var1 - var8);
               var0[var2] = 0;
               if (var8 >= var1) {
                  return var8;
               }
            }
         }

         return var8;
      }
   }

   private void place(int var1) {
      if (this.diff >= 0 && !(this.winT >= 0.0F) && !this.given[this.sel]) {
         if (this.noteMode && var1 != 0) {
            if (this.cell[this.sel] == 0) {
               this.undo.push(new int[]{this.sel, this.cell[this.sel], this.notes[this.sel]});
               this.notes[this.sel] = this.notes[this.sel] ^ 1 << var1;
               this.pop[this.sel] = 0.6F;
               Sound.play("minecraft:block.note_block.hat", 1.8F, 0.15F);
            }
         } else if (this.cell[this.sel] != var1) {
            this.undo.push(new int[]{this.sel, this.cell[this.sel], this.notes[this.sel]});
            this.cell[this.sel] = var1;
            this.notes[this.sel] = 0;
            if (var1 == 0) {
               Sound.play("minecraft:block.wood.hit", 1.5F, 0.2F);
            } else {
               this.pop[this.sel] = 1.0F;
               if (var1 != this.sol[this.sel]) {
                  this.mistakes++;
                  this.wrongT[this.sel] = 1.0F;
                  this.fx.shake(0.35F);
                  this.fx.popup("-100", this.cx(this.sel % 9), this.cy(this.sel / 9) - 8.0F, -38037, 9.0F);
                  Sound.play("minecraft:block.note_block.bass", 0.6F, 0.5F);
                  if (this.mistakes >= 3) {
                     this.lost = true;
                     this.winT = -1.0F;
                     this.gameOver(false);
                  }
               } else {
                  int var2 = this.sel / 9;
                  int var3 = this.sel % 9;

                  for (int var4 = 0; var4 < 81; var4++) {
                     if (var4 / 9 == var2 || var4 % 9 == var3 || var4 / 27 == this.sel / 27 && var4 % 9 / 3 == var3 / 3) {
                        this.notes[var4] = this.notes[var4] & ~(1 << var1);
                     }
                  }

                  Sound.play("minecraft:block.note_block.pling", 1.0F + var1 * 0.08F, 0.3F);
                  this.checkUnits(this.sel);
                  if (this.solved()) {
                     this.win();
                  }
               }
            }
         }
      }
   }

   private void checkUnits(int var1) {
      int var2 = var1 / 9;
      int var3 = var1 % 9;
      boolean var4 = true;
      boolean var5 = true;
      boolean var6 = true;

      for (int var7 = 0; var7 < 9; var7++) {
         if (this.cell[var2 * 9 + var7] != this.sol[var2 * 9 + var7]) {
            var4 = false;
         }

         if (this.cell[var7 * 9 + var3] != this.sol[var7 * 9 + var3]) {
            var5 = false;
         }

         int var8 = (var2 / 3 * 3 + var7 / 3) * 9 + var3 / 3 * 3 + var7 % 3;
         if (this.cell[var8] != this.sol[var8]) {
            var6 = false;
         }
      }

      int var12 = 0;

      for (int var13 = 0; var13 < 81; var13++) {
         int var9 = var13 / 9;
         int var10 = var13 % 9;
         boolean var11 = var4 && var9 == var2 || var5 && var10 == var3 || var6 && var9 / 3 == var2 / 3 && var10 / 3 == var3 / 3;
         if (var11) {
            this.wave[var13] = -(Math.abs(var9 - var2) + Math.abs(var10 - var3)) * 0.035F;
            var12++;
         }
      }

      if (var12 > 0) {
         this.fx.burst(this.cx(var3), this.cy(var2), 12, -12604929, 90.0F, 2.0F, 0.5F, 0.0F, 2);
         Sound.play("minecraft:entity.experience_orb.pickup", 1.3F, 0.3F);
      }

      int var14 = this.cell[var1];
      int var15 = 0;

      for (int var16 = 0; var16 < 81; var16++) {
         if (this.cell[var16] == var14 && this.sol[var16] == var14) {
            var15++;
         }
      }

      if (var15 == 9) {
         this.fx.popup("Alle " + var14 + "er!", this.cx(var3), this.cy(var2) - 12.0F, -736942, 9.0F);
      }
   }

   private boolean solved() {
      for (int var1 = 0; var1 < 81; var1++) {
         if (this.cell[var1] != this.sol[var1]) {
            return false;
         }
      }

      return true;
   }

   private void win() {
      this.winT = 0.0F;
      this.score = this.liveScore();

      for (int var1 = 0; var1 < 81; var1++) {
         this.wave[var1] = -(var1 / 9 + var1 % 9) * 0.03F;
      }

      for (int var2 = 0; var2 < 6; var2++) {
         this.fx.confetti(41.0F + var2 * 45, 74.0F + var2 % 2 * 110, 26, new int[]{-1890757, -736942, -14703780, -12604929, -1}, 190.0F, 4.0F);
      }

      this.fx.shake(0.3F);
      Sound.play("minecraft:ui.toast.challenge_complete", 1.2F, 0.4F);
   }

   private void hint() {
      if (this.diff >= 0 && !(this.winT >= 0.0F)) {
         int var1 = -1;
         if (!this.given[this.sel] && this.cell[this.sel] != this.sol[this.sel]) {
            var1 = this.sel;
         } else {
            for (int var2 = 0; var2 < 81; var2++) {
               if (this.cell[var2] != this.sol[var2]) {
                  var1 = var2;
                  break;
               }
            }
         }

         if (var1 >= 0) {
            this.hints++;
            this.sel = var1;
            boolean var4 = this.noteMode;
            this.noteMode = false;
            int var3 = this.mistakes;
            this.place(this.sol[var1]);
            this.mistakes = var3;
            this.noteMode = var4;
            this.fx.popup("Tipp -150", this.cx(var1 % 9), this.cy(var1 / 9) - 10.0F, -6301441, 9.0F);
         }
      }
   }

   private void doUndo() {
      if (!this.undo.isEmpty() && !(this.winT >= 0.0F)) {
         int[] var1 = this.undo.pop();
         this.cell[var1[0]] = var1[1];
         this.notes[var1[0]] = var1[2];
         this.sel = var1[0];
         this.pop[this.sel] = 0.5F;
         Sound.play("minecraft:block.wood.hit", 1.2F, 0.2F);
      }
   }

   private long liveScore() {
      return this.diff < 0 ? 0L : Math.max(100L, BASE[this.diff] - (long)(this.elapsed * 2.0F) - this.mistakes * 100L - this.hints * 150L);
   }

   @Override
   protected void onKey(int var1) {
      if (this.diff < 0) {
         if (var1 >= 49 && var1 <= 52) {
            this.newPuzzle(var1 - 49);
         }
      } else {
         int var2 = var1 >= 49 && var1 <= 57 ? var1 - 48 : (var1 >= 321 && var1 <= 329 ? var1 - 320 : -1);
         if (var2 > 0) {
            this.place(var2);
         } else {
            if (var1 == 48 || var1 == 320 || var1 == 259 || var1 == 261) {
               this.place(0);
            } else if (var1 == 78) {
               this.noteMode = !this.noteMode;
            } else if (var1 == 90 || var1 == 85) {
               this.doUndo();
            } else if (var1 == 72) {
               this.hint();
            } else if (var1 == 263 || var1 == 65) {
               this.sel = this.sel / 9 * 9 + (this.sel % 9 + 8) % 9;
            } else if (var1 == 262 || var1 == 68) {
               this.sel = this.sel / 9 * 9 + (this.sel % 9 + 1) % 9;
            } else if (var1 == 265 || var1 == 87) {
               this.sel = (this.sel + 72) % 81;
            } else if (var1 == 264 || var1 == 83) {
               this.sel = (this.sel + 9) % 81;
            }
         }
      }
   }

   @Override
   protected void onClick(float var1, float var2, int var3) {
      if (this.diff < 0) {
         for (int var6 = 0; var6 < 4; var6++) {
            float[] var7 = this.diffRect(var6);
            if (var1 >= var7[0] && var1 <= var7[0] + var7[2] && var2 >= var7[1] && var2 <= var7[1] + var7[3]) {
               this.newPuzzle(var6);
               return;
            }
         }
      } else {
         int var4 = cellAt(var1, var2);
         if (var4 >= 0) {
            this.sel = var4;
            if (var3 == 1) {
               this.place(0);
            }

            Sound.click();
         } else {
            int var5 = this.padAt(var1, var2);
            if (var5 >= 1 && var5 <= 9) {
               this.place(var5);
            } else if (var5 == 10) {
               this.noteMode = !this.noteMode;
            } else if (var5 == 11) {
               this.place(0);
            } else if (var5 == 12) {
               this.doUndo();
            } else if (var5 == 13) {
               this.hint();
            }
         }
      }
   }

   private float[] diffRect(int var1) {
      return new float[]{50.0F + var1 * 76, 118.0F, 70.0F, 92.0F};
   }

   private int padAt(float var1, float var2) {
      for (int var3 = 0; var3 < 9; var3++) {
         float var4 = 300.0F + var3 % 3 * 30;
         float var5 = 96 + var3 / 3 * 30;
         if (var1 >= var4 && var1 < var4 + 27.0F && var2 >= var5 && var2 < var5 + 27.0F) {
            return var3 + 1;
         }
      }

      for (int var6 = 0; var6 < 4; var6++) {
         float var7 = 300.0F + var6 % 2 * 45;
         float var8 = 190 + var6 / 2 * 27;
         if (var1 >= var7 && var1 < var7 + 42.0F && var2 >= var8 && var2 < var8 + 24.0F) {
            return 10 + var6;
         }
      }

      return -1;
   }

   @Override
   protected void update(float var1) {
      this.fx.update(var1);

      for (int var2 = 0; var2 < 81; var2++) {
         this.pop[var2] = Math.max(0.0F, this.pop[var2] - var1 * 4.0F);
         if (this.wave[var2] < 1.0F) {
            this.wave[var2] = this.wave[var2] + var1 * 1.6F;
         }

         this.wrongT[var2] = Math.max(0.0F, this.wrongT[var2] - var1 * 2.0F);
      }

      if (this.diff >= 0 && this.winT < 0.0F) {
         this.elapsed += var1;
         this.score = this.liveScore();
      }

      if (this.winT >= 0.0F) {
         this.winT += var1;
         if ((int)(this.winT * 10.0F) % 3 == 0 && this.winT < 1.5F) {
            this.fx
               .confetti(
                  14.0F + this.rnd.nextFloat() * 270.0F, 14.0F + this.rnd.nextFloat() * 270.0F, 3, new int[]{-1890757, -736942, -14703780, -1}, 120.0F, 3.5F
               );
         }

         if (this.winT > 2.2F) {
            this.gameOver();
         }
      }
   }

   private static float colX(int var0) {
      return 14.0F + var0 * 29.0F + var0 / 3 * 3.0F;
   }

   private static float rowY(int var0) {
      return 14.0F + var0 * 29.0F + var0 / 3 * 3.0F;
   }

   private float cx(int var1) {
      return colX(var1) + 14.5F;
   }

   private float cy(int var1) {
      return rowY(var1) + 14.5F;
   }

   private static int cellAt(float var0, float var1) {
      for (int var2 = 0; var2 < 9; var2++) {
         float var3 = rowY(var2);
         if (!(var1 < var3) && !(var1 >= var3 + 29.0F)) {
            for (int var4 = 0; var4 < 9; var4++) {
               float var5 = colX(var4);
               if (var0 >= var5 && var0 < var5 + 29.0F) {
                  return var2 * 9 + var4;
               }
            }
         }
      }

      return -1;
   }

   @Override
   protected void render() {
      float var1 = Math.min(0.05F, Gx.dt);
      this.anim += var1;
      boolean var2 = Style.light;
      int var3 = var2 ? -1051912 : -15985104;
      int var4 = var2 ? -1906703 : -15655362;
      this.boxV(0.0F, 0.0F, 400.0F, 300.0F, 0.0F, var3, var4);

      for (int var5 = 0; var5 < 16; var5++) {
         float var6 = (var5 * 83.3F + this.anim * 4.0F) % 400.0F;
         float var7 = (var5 * 47.1F + this.anim * (5 + var5 % 4)) % 300.0F;
         this.circle(var6, var7, 1.0F + var5 % 3 * 0.5F, var2 ? 335544320 : 419430399);
      }

      if (this.diff < 0 && this.state != GameView.State.READY) {
         this.chooser();
      } else if (this.diff < 0) {
         this.gridPreview();
      } else {
         float var32 = (this.mx - this.bx) / this.u;
         float var33 = (this.my - this.by) / this.u;
         Gx.push();
         Gx.translate(this.S(this.fx.sx), this.S(this.fx.sy));
         int var34 = var2 ? -15395302 : -789258;
         int var8 = var2 ? -1 : -15326134;
         int var9 = -12604929;
         int var10 = var2 ? -14852144 : -7354113;
         Gx.shadow(this.X(14.0F), this.Y(14.0F), this.S(267.0F), this.S(267.0F), this.S(6.0F), this.S(6.0F), 0.4F);
         this.box(12.0F, 12.0F, 271.0F, 271.0F, 6.0F, var2 ? -14011568 : -16446952);
         int var11 = this.sel / 9;
         int var12 = this.sel % 9;
         int var13 = this.cell[this.sel];
         int var14 = cellAt(var32, var33);

         for (int var15 = 0; var15 < 81; var15++) {
            int var16 = var15 / 9;
            int var17 = var15 % 9;
            float var18 = colX(var17) + 0.5F;
            float var19 = rowY(var16) + 0.5F;
            float var20 = 28.0F;
            float var21 = 28.0F;
            int var22 = var8;
            boolean var23 = var16 == var11 || var17 == var12 || var16 / 3 == var11 / 3 && var17 / 3 == var12 / 3;
            if (var23) {
               var22 = Gx.mix(var8, var9, 0.1F);
            }

            if (var13 != 0 && this.cell[var15] == var13) {
               var22 = Gx.mix(var8, var9, 0.28F);
            }

            if (var15 == this.sel) {
               var22 = Gx.mix(var8, var9, 0.45F);
            } else if (var15 == var14) {
               var22 = Gx.mix(var22, var2 ? -16777216 : -1, 0.06F);
            }

            float var24 = this.wave[var15];
            if (var24 > 0.0F && var24 < 1.0F) {
               var22 = Gx.mix(var22, -736942, (float)Math.sin(var24 * Math.PI) * 0.55F);
            }

            if (this.wrongT[var15] > 0.0F) {
               var22 = Gx.mix(var22, -45730, this.wrongT[var15] * 0.5F);
            }

            this.box(var18, var19, var20, var21, 2.2F, var22);
            int var25 = this.cell[var15];
            if (var25 == 0) {
               if (this.notes[var15] != 0) {
                  for (int var51 = 1; var51 <= 9; var51++) {
                     if ((this.notes[var15] & 1 << var51) != 0) {
                        float var55 = var18 + var20 * ((var51 - 1) % 3 + 0.5F) / 3.0F;
                        float var59 = var19 + var21 * ((var51 - 1) / 3 + 0.5F) / 3.0F;
                        boolean var29 = var51 == var13;
                        if (var29) {
                           this.circle(var55, var59, 3.8F, Gx.withAlpha(var9, 0.35F));
                        }

                        this.text(String.valueOf(var51), var55, var59, 6.2F, 2, var29 ? var34 : (var2 ? -8748912 : -7562568));
                     }
                  }
               }
            } else {
               boolean var26 = !this.given[var15] && var25 != this.sol[var15];
               float var27 = 1.0F + 0.35F * Ease.outCubic(this.pop[var15]) + (var24 > 0.0F && var24 < 1.0F ? 0.18F * (float)Math.sin(var24 * Math.PI) : 0.0F);
               int var28 = this.given[var15] ? var34 : (var26 ? -42390 : var10);
               Gx.push();
               Gx.scaleAt(this.X(var18 + var20 / 2.0F), this.Y(var19 + var21 / 2.0F), var27);
               this.text(String.valueOf(var25), var18 + var20 / 2.0F, var19 + var21 / 2.0F, this.given[var15] ? 15.0F : 14.5F, this.given[var15] ? 3 : 2, var28);
               Gx.pop();
            }
         }

         float var35 = colX(var12);
         float var36 = rowY(var11);
         float var37 = Ease.to("sd_selx", var35, 22.0F);
         float var38 = Ease.to("sd_sely", var36, 22.0F);
         Gx.outline(this.X(var37), this.Y(var38), this.S(29.0F), this.S(29.0F), this.S(3.0F), Math.max(1, this.S(1.4F)), var9);
         this.fx.draw(this, 0.0F, 0.0F);
         Gx.pop();
         int var39 = DIFF_COL[this.diff];
         this.box(300.0F, 14.0F, 87.0F, 20.0F, 10.0F, Gx.withAlpha(var39, 0.2F));
         this.text(DIFF[this.diff], 343.5F, 24.0F, 8.0F, 3, var39);
         int var40 = (int)this.elapsed / 60;
         int var41 = (int)this.elapsed % 60;
         Gx.icon("clock", this.X(308.0F), this.Y(47.0F), 9.0F * this.u, Style.sub);
         Gx.textMid(String.format("%d:%02d", var40, var41), this.X(317.0F), this.Y(47.0F), 10.0F * this.u, 3, var34);

         for (int var42 = 0; var42 < 3; var42++) {
            boolean var44 = var42 < this.mistakes;
            Gx.icon(var44 ? "x" : "heart-fill", this.X(358.0F + var42 * 11), this.Y(47.0F), 8.0F * this.u, var44 ? -45730 : Gx.withAlpha(-45715, 0.9F));
         }

         this.text("Fehler " + this.mistakes + "/3  •  Tipps " + this.hints, 343.5F, 64.0F, 6.0F, 2, Style.sub);
         int var43 = 0;

         for (int var45 = 0; var45 < 81; var45++) {
            if (this.cell[var45] == this.sol[var45]) {
               var43++;
            }
         }

         this.box(300.0F, 74.0F, 87.0F, 4.0F, 2.0F, var2 ? 570425344 : 587202559);
         this.box(300.0F, 74.0F, 87 * var43 / 81.0F, 4.0F, 2.0F, var39);
         int var46 = this.padAt(var32, var33);

         for (int var47 = 0; var47 < 9; var47++) {
            int var49 = var47 + 1;
            int var52 = 0;

            for (int var56 = 0; var56 < 81; var56++) {
               if (this.cell[var56] == var49 && this.sol[var56] == var49) {
                  var52++;
               }
            }

            float var57 = 300.0F + var47 % 3 * 30;
            float var60 = 96 + var47 / 3 * 30;
            boolean var62 = var52 >= 9;
            boolean var30 = var46 == var49;
            int var31 = var62 ? (var2 ? 268435456 : 285212671) : (var30 ? Gx.mix(var8, var9, 0.35F) : var8);
            this.box(var57, var60, 27.0F, 27.0F, 6.0F, var31);
            if (this.noteMode && !var62) {
               Gx.outline(this.X(var57), this.Y(var60), this.S(27.0F), this.S(27.0F), this.S(6.0F), Math.max(1, this.S(0.8F)), Gx.withAlpha(var9, 0.6F));
            }

            this.text(
               String.valueOf(var49), var57 + 13.5F, var60 + 12.5F, this.noteMode ? 10.0F : 13.0F, 3, var62 ? Style.muted : (this.noteMode ? Style.sub : var34)
            );
            if (!var62) {
               this.text(String.valueOf(9 - var52), var57 + 22.0F, var60 + 22.0F, 5.0F, 2, Style.muted);
            }
         }

         String[] var48 = new String[]{"Notizen", "Radieren", "Zurück", "Tipp"};
         String[] var50 = new String[]{"wand", "trash", "reset", "bolt"};

         for (int var53 = 0; var53 < 4; var53++) {
            float var58 = 300.0F + var53 % 2 * 45;
            float var61 = 190 + var53 / 2 * 27;
            boolean var63 = var53 == 0 && this.noteMode;
            boolean var64 = var46 == 10 + var53;
            this.box(var58, var61, 42.0F, 24.0F, 6.0F, var63 ? -12604929 : (var64 ? Gx.mix(var8, var9, 0.3F) : var8));
            Gx.icon(var50[var53], this.X(var58 + 21.0F), this.Y(var61 + 9.0F), 8.5F * this.u, var63 ? -1 : Style.sub);
            this.text(var48[var53] + (var53 == 0 ? (this.noteMode ? " an" : " aus") : ""), var58 + 21.0F, var61 + 18.5F, 5.0F, 3, var63 ? -1 : Style.sub);
         }

         this.text("1-9  •  N Notizen  •  H Tipp", 343.5F, 256.0F, 5.2F, 2, Style.muted);
         this.text("Z Zurück  •  Entf Radieren", 343.5F, 265.0F, 5.2F, 2, Style.muted);
         if (this.winT >= 0.0F) {
            float var54 = Ease.outBack(Math.min(1.0F, this.winT / 0.4F));
            Gx.push();
            Gx.scaleAt(this.X(147.5F), this.Y(147.5F), var54);
            this.box(77.5F, 125.5F, 140.0F, 44.0F, 14.0F, Gx.withAlpha(-16051160, 0.92F));
            this.text("Gelöst!", 147.5F, 142.5F, 15.0F, 3, -736942);
            this.text(this.liveScore() + " Punkte", 147.5F, 158.5F, 7.5F, 2, -1);
            Gx.pop();
         }

         this.fx.drawPopups(this, 0.0F, 0.0F);
      }
   }

   private void gridPreview() {
      String var1 = "530070000600195000098000060800060003400803001700020006060000280000419005000080079";
      this.box(12.0F, 12.0F, 271.0F, 271.0F, 6.0F, Style.light ? -14011568 : -16446952);

      for (int var2 = 0; var2 < 81; var2++) {
         int var3 = var2 / 9;
         int var4 = var2 % 9;
         float var5 = colX(var4) + 0.5F;
         float var6 = rowY(var3) + 0.5F;
         this.box(var5, var6, 28.0F, 28.0F, 2.2F, Style.light ? -1 : -15326134);
         char var7 = var1.charAt(var2);
         if (var7 != '0') {
            this.text(String.valueOf(var7), var5 + 14.5F - 0.5F, var6 + 14.5F - 0.5F, 14.0F, 3, Style.light ? -15395302 : -789258);
         }
      }
   }

   private void chooser() {
      this.text("Wähle den Schwierigkeitsgrad", 200.0F, 78.0F, 13.0F, 3, Style.text);
      this.text("Jedes Rätsel hat genau eine Lösung", 200.0F, 96.0F, 7.5F, 1, Style.sub);
      float var1 = (this.mx - this.bx) / this.u;
      float var2 = (this.my - this.by) / this.u;

      for (int var3 = 0; var3 < 4; var3++) {
         float[] var4 = this.diffRect(var3);
         boolean var5 = var1 >= var4[0] && var1 <= var4[0] + var4[2] && var2 >= var4[1] && var2 <= var4[1] + var4[3];
         float var6 = Ease.to("sd_d" + var3, var5 ? 1.0F : 0.0F, 14.0F);
         float var7 = var6 * 3.0F;
         int var8 = DIFF_COL[var3];
         Gx.shadow(this.X(var4[0]), this.Y(var4[1] - var7), this.S(var4[2]), this.S(var4[3]), this.S(10.0F), this.S(4.0F + var6 * 4.0F), 0.3F + var6 * 0.2F);
         this.boxV(var4[0], var4[1] - var7, var4[2], var4[3], 10.0F, Gx.mix(Style.surface, var8, 0.18F + 0.15F * var6), Gx.mix(Style.surface, var8, 0.05F));
         Gx.outline(
            this.X(var4[0]),
            this.Y(var4[1] - var7),
            this.S(var4[2]),
            this.S(var4[3]),
            this.S(10.0F),
            Math.max(1, this.S(0.8F)),
            Gx.withAlpha(var8, 0.4F + 0.5F * var6)
         );

         for (int var9 = 0; var9 <= var3; var9++) {
            Gx.icon("star-fill", this.X(var4[0] + var4[2] / 2.0F + (var9 - var3 / 2.0F) * 9.0F), this.Y(var4[1] - var7 + 22.0F), 8.0F * this.u, var8);
         }

         this.text(DIFF[var3], var4[0] + var4[2] / 2.0F, var4[1] - var7 + 44.0F, 9.5F, 3, Style.text);
         this.text(CLUES[var3] + " Vorgaben", var4[0] + var4[2] / 2.0F, var4[1] - var7 + 59.0F, 6.2F, 2, Style.sub);
         this.text("bis " + BASE[var3] + " P.", var4[0] + var4[2] / 2.0F, var4[1] - var7 + 72.0F, 6.2F, 2, var8);
         this.text(String.valueOf(var3 + 1), var4[0] + var4[2] - 8.0F, var4[1] - var7 + 9.0F, 5.5F, 3, Style.muted);
      }

      this.text("Klick oder Taste 1-4", 200.0F, 232.0F, 7.0F, 2, Style.muted);
   }
}
