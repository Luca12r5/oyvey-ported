package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;

public final class TicTacToe extends GameView {
   private static final float CELL = 80.0F;
   private static final float GX = 16.0F;
   private static final float GY = 16.0F;
   private static final float TILE = 74.0F;
   private static final float PX = 272.0F;
   private static final float PW = 94.0F;
   private static final int[][] LINES = new int[][]{{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}};
   private static final int NONE = 0;
   private static final int WIN = 1;
   private static final int DRAW = 2;
   private static final int LOSS = 3;
   private final int[] b = new int[9];
   private final float[] placed = new float[9];
   private final float[] hoverA = new float[9];
   private int turn = 1;
   private int result = 0;
   private int[] winLine;
   private float think;
   private float endT;
   private float winAt;
   private float clock;
   private float act1 = 1.0F;
   private float act2 = 0.0F;
   private boolean playerStarts = true;
   private int wins;
   private int draws;
   private int losses;

   public TicTacToe() {
      super("tictactoe");
   }

   @Override
   protected float boardW() {
      return 380.0F;
   }

   @Override
   protected float boardH() {
      return 266.0F;
   }

   @Override
   protected void reset() {
      for (int var1 = 0; var1 < 9; var1++) {
         this.b[var1] = 0;
         this.placed[var1] = 0.0F;
      }

      this.winLine = null;
      this.result = 0;
      this.think = 0.0F;
      this.endT = 0.0F;
      this.turn = this.playerStarts ? 1 : 2;
   }

   @Override
   protected String overTitle() {
      return this.result == 1 ? "Gewonnen!" : (this.result == 2 ? "Unentschieden" : "Verloren");
   }

   @Override
   protected void update(float var1) {
      if (this.result != 0) {
         this.endT += var1;
         if (this.endT > 1.15F) {
            this.gameOver();
         }
      } else {
         if (this.turn == 2) {
            this.think += var1;
            if (this.think >= 0.35F) {
               this.place(this.aiMove(), 2);
            }
         }
      }
   }

   @Override
   protected void onClick(float var1, float var2, int var3) {
      if (var3 == 0 && this.turn == 1 && this.result == 0) {
         int var4 = this.cellAt(var1, var2);
         if (var4 >= 0 && this.b[var4] == 0) {
            this.place(var4, 1);
         }
      }
   }

   private int cellAt(float var1, float var2) {
      int var3 = (int)Math.floor((var1 - 16.0F) / 80.0F);
      int var4 = (int)Math.floor((var2 - 16.0F) / 80.0F);
      return var3 >= 0 && var4 >= 0 && var3 <= 2 && var4 <= 2 ? var4 * 3 + var3 : -1;
   }

   private void place(int var1, int var2) {
      if (var1 >= 0 && this.b[var1] == 0) {
         this.b[var1] = var2;
         this.placed[var1] = this.clock;
         Sound.play("minecraft:block.note_block.pling", var2 == 1 ? 1.5F : 1.05F, 0.3F);
         int[] var3 = findWin(this.b);
         if (var3 != null) {
            this.winLine = var3;
            this.winAt = this.clock + (var2 == 1 ? 0.3F : 0.4F);
            this.result = var2 == 1 ? 1 : 3;
            if (var2 == 1) {
               this.wins++;
               this.score = 3L;
               Sound.play("minecraft:entity.player.levelup", 1.3F, 0.35F);
            } else {
               this.losses++;
               this.score = 0L;
            }

            this.playerStarts = !this.playerStarts;
         } else if (full(this.b)) {
            this.result = 2;
            this.draws++;
            this.score = 1L;
            this.playerStarts = !this.playerStarts;
            Sound.play("minecraft:block.note_block.chime", 0.9F, 0.3F);
         } else {
            this.turn = 3 - var2;
            this.think = 0.0F;
         }
      }
   }

   private static int[] findWin(int[] var0) {
      for (int[] var4 : LINES) {
         if (var0[var4[0]] != 0 && var0[var4[0]] == var0[var4[1]] && var0[var4[1]] == var0[var4[2]]) {
            return var4;
         }
      }

      return null;
   }

   private static boolean full(int[] var0) {
      for (int var4 : var0) {
         if (var4 == 0) {
            return false;
         }
      }

      return true;
   }

   private int aiMove() {
      ArrayList var1 = new ArrayList();

      for (int var2 = 0; var2 < 9; var2++) {
         if (this.b[var2] == 0) {
            var1.add(var2);
         }
      }

      if (var1.isEmpty()) {
         return -1;
      } else if (this.rnd.nextFloat() < 0.1F) {
         return (Integer)var1.get(this.rnd.nextInt(var1.size()));
      } else {
         int var7 = Integer.MIN_VALUE;
         ArrayList var3 = new ArrayList();

         for (int var5 : var1) {
            this.b[var5] = 2;
            int var6 = this.minimax(false, 1);
            this.b[var5] = 0;
            if (var6 > var7) {
               var7 = var6;
               var3.clear();
               var3.add(var5);
            } else if (var6 == var7) {
               var3.add(var5);
            }
         }

         return (Integer)var3.get(this.rnd.nextInt(var3.size()));
      }
   }

   private int minimax(boolean var1, int var2) {
      int[] var3 = findWin(this.b);
      if (var3 != null) {
         return this.b[var3[0]] == 2 ? 10 - var2 : var2 - 10;
      } else if (full(this.b)) {
         return 0;
      } else {
         int var4 = var1 ? Integer.MIN_VALUE : Integer.MAX_VALUE;

         for (int var5 = 0; var5 < 9; var5++) {
            if (this.b[var5] == 0) {
               this.b[var5] = var1 ? 2 : 1;
               int var6 = this.minimax(!var1, var2 + 1);
               this.b[var5] = 0;
               var4 = var1 ? Math.max(var4, var6) : Math.min(var4, var6);
            }
         }

         return var4;
      }
   }

   private static int oColor() {
      int var0 = Style.accent;
      int var1 = (var0 >> 16 & 0xFF) - 255;
      int var2 = (var0 >> 8 & 0xFF) - 176;
      int var3 = (var0 & 0xFF) - 46;
      return var1 * var1 + var2 * var2 + var3 * var3 < 22500 ? -12604929 : -20434;
   }

   private int pieceColor(int var1) {
      return var1 == 1 ? Style.accent : oColor();
   }

   private void seg(float var1, float var2, float var3, float var4, float var5, int var6) {
      float var7 = this.bx + var1 * this.u;
      float var8 = this.by + var2 * this.u;
      float var9 = this.bx + var3 * this.u;
      float var10 = this.by + var4 * this.u;
      float var11 = (float)Math.hypot(var9 - var7, var10 - var8);
      int var12 = Math.max(1, Math.round(var5 * this.u / 2.0F));
      int var13 = Math.max(1, (int)Math.ceil(var11 / Math.max(1.0F, var12 * 0.3F)));

      for (int var14 = 0; var14 <= var13; var14++) {
         float var15 = (float)var14 / var13;
         Gx.circle(Math.round(var7 + (var9 - var7) * var15), Math.round(var8 + (var10 - var8) * var15), var12, var6);
      }
   }

   private void drawX(float var1, float var2, float var3, float var4, float var5, int var6) {
      float var7 = Ease.outCubic(var5 / 0.16F);
      float var8 = Ease.outCubic((var5 - 0.13F) / 0.16F);
      if (var7 > 0.0F) {
         this.seg(var1 - var3, var2 - var3, var1 - var3 + 2.0F * var3 * var7, var2 - var3 + 2.0F * var3 * var7, var4, var6);
      }

      if (var8 > 0.0F) {
         this.seg(var1 + var3, var2 - var3, var1 + var3 - 2.0F * var3 * var8, var2 - var3 + 2.0F * var3 * var8, var4, var6);
      }
   }

   private void drawO(float var1, float var2, float var3, float var4, float var5, int var6) {
      float var7 = Ease.outCubic(var5 / 0.3F);
      if (!(var7 <= 0.0F)) {
         if (var7 >= 1.0F) {
            Gx.ringAt(this.X(var1), this.Y(var2), this.S(var3 + var4 / 2.0F), this.S(var4), var6);
         } else {
            int var8 = Math.max(1, Math.round(var4 * this.u / 2.0F));
            float var9 = (float)((Math.PI * 2) * var7);
            int var10 = Math.max(2, (int)Math.ceil(var9 * var3 * this.u / Math.max(1.0F, var8 * 0.3F)));

            for (int var11 = 0; var11 <= var10; var11++) {
               double var12 = (-Math.PI / 2) + var9 * var11 / var10;
               Gx.circle(
                  Math.round(this.bx + (var1 + (float)Math.cos(var12) * var3) * this.u),
                  Math.round(this.by + (var2 + (float)Math.sin(var12) * var3) * this.u),
                  var8,
                  var6
               );
            }
         }
      }
   }

   private void glyph(int var1, float var2, float var3, float var4, float var5, int var6) {
      if (var1 == 1) {
         this.drawX(var2, var3, var4, var4 * 0.38F, var5, var6);
      } else {
         this.drawO(var2, var3, var4 * 0.95F, var4 * 0.38F, var5, var6);
      }
   }

   @Override
   protected void render() {
      this.clock = this.clock + Gx.dt;
      float var1 = 1.0F - (float)Math.exp(-Gx.dt * 14.0F);
      float var2 = (this.mx - this.bx) / this.u;
      float var3 = (this.my - this.by) / this.u;
      int var4 = this.state == GameView.State.PLAYING && this.turn == 1 && this.result == 0 ? this.cellAt(var2, var3) : -1;
      if (var4 >= 0 && this.b[var4] != 0) {
         var4 = -1;
      }

      int var5 = Style.light ? -1 : -15263456;
      boolean var6 = this.winLine != null && this.clock >= this.winAt;
      int var7 = this.winLine != null ? this.pieceColor(this.b[this.winLine[0]]) : Style.accent;
      float var8 = this.winLine != null ? Ease.outCubic((this.clock - this.winAt) / 0.3F) : 0.0F;
      this.box(11.0F, 11.0F, 244.0F, 244.0F, 16.0F, Style.light ? 218103808 : 201326591);

      for (int var9 = 0; var9 < 9; var9++) {
         this.hoverA[var9] = this.hoverA[var9] + ((var9 == var4 ? 1 : 0) - this.hoverA[var9]) * var1;
         float var10 = 16.0F + var9 % 3 * 80.0F + 3.0F - 3.0F;
         float var11 = 16.0F + var9 / 3 * 80.0F + 3.0F - 3.0F;
         float var12 = 1.5F * this.hoverA[var9];
         boolean var13 = var6 && (this.winLine[0] == var9 || this.winLine[1] == var9 || this.winLine[2] == var9);
         int var14 = Gx.mix(var5, Style.accent, 0.1F * this.hoverA[var9]);
         if (var13) {
            var14 = Gx.mix(var5, var7, 0.2F * var8);
         }

         this.box(var10, var11 + 2.2F, 74.0F, 74.0F, 13.0F, Style.light ? 369098752 : 1426063360);
         this.boxV(var10, var11 - var12, 74.0F, 74.0F, 13.0F, Gx.mix(var14, -1, Style.light ? 0.0F : 0.05F), var14);
         Gx.outline(
            this.X(var10),
            this.Y(var11 - var12),
            this.S(74.0F),
            this.S(74.0F),
            this.S(13.0F),
            Math.max(1, this.S(0.6F)),
            var13 ? Gx.withAlpha(var7, 0.6F * var8) : Gx.mix(Style.stroke, Gx.withAlpha(Style.accent, 0.5F), this.hoverA[var9])
         );
         float var15 = var10 + 37.0F;
         float var16 = var11 + 37.0F - var12;
         if (this.b[var9] != 0) {
            int var17 = this.pieceColor(this.b[var9]);
            float var18 = this.clock - this.placed[var9];
            Gx.glow(this.X(var15), this.Y(var16), this.S(40.0F), Gx.withAlpha(var17, (var13 ? 0.32F : 0.1F) * Ease.outCubic(var18 / 0.3F)));
            this.glyph(this.b[var9], var15, var16, 20.0F, var18, var17);
         } else if (this.hoverA[var9] > 0.02F) {
            this.glyph(1, var15, var16, 20.0F, 1.0F, Gx.mix(var14, Style.accent, 0.32F * this.hoverA[var9]));
         }
      }

      if (var6) {
         float var26 = 16.0F + this.winLine[0] % 3 * 80.0F + 40.0F - 3.0F;
         float var28 = 16.0F + this.winLine[0] / 3 * 80.0F + 40.0F - 3.0F;
         float var30 = 16.0F + this.winLine[2] % 3 * 80.0F + 40.0F - 3.0F;
         float var32 = 16.0F + this.winLine[2] / 3 * 80.0F + 40.0F - 3.0F;
         float var34 = var30 - var26;
         float var35 = var32 - var28;
         float var36 = (float)Math.hypot(var34, var35);
         float var37 = var34 / var36 * 80.0F * 0.36F;
         float var38 = var35 / var36 * 80.0F * 0.36F;
         float var39 = var26 - var37;
         float var19 = var28 - var38;
         float var20 = var30 + var37;
         float var21 = var32 + var38;
         float var22 = var39 + (var20 - var39) * var8;
         float var23 = var19 + (var21 - var19) * var8;

         for (int var24 = 0; var24 <= 6; var24++) {
            float var25 = var24 / 6.0F * var8;
            Gx.glow(this.X(var39 + (var20 - var39) * var25), this.Y(var19 + (var21 - var19) * var25), this.S(26.0F), Gx.withAlpha(var7, 0.22F));
         }

         this.seg(var39, var19, var22, var23, 7.5F, Gx.mix(var7, -16777216, 0.35F));
         this.seg(var39, var19, var22, var23, 5.0F, Gx.mix(var7, -1, 0.45F));
      }

      this.act1 = this.act1 + ((this.turn == 1 && this.result == 0 ? 1 : 0) - this.act1) * var1;
      this.act2 = this.act2 + ((this.turn == 2 && this.result == 0 ? 1 : 0) - this.act2) * var1;
      this.playerCard(16.0F, "Du", "Spieler", 1, this.act1);
      this.playerCard(82.0F, "KI", "Computer", 2, this.act2);
      int var29 = Style.text;
      String var27;
      if (this.result == 1) {
         var27 = "Gewonnen!";
         var29 = Style.ok;
      } else if (this.result == 3) {
         var27 = "Verloren";
         var29 = Style.danger;
      } else if (this.result == 2) {
         var27 = "Unentschieden";
         var29 = Style.sub;
      } else if (this.turn == 2) {
         var27 = "KI denkt" + ".".repeat(1 + (int)(this.clock * 4.0F) % 3);
      } else {
         var27 = "Du bist dran";
      }

      float var31 = 166.0F;
      this.text(var27, 319.0F, var31, 9.5F, 3, var29);
      this.text(this.result == 0 ? (this.turn == 1 ? "Setze dein X" : "Bitte warten") : "Runde beendet", 319.0F, var31 + 14.0F, 6.8F, 1, Style.sub);
      float var33 = 196.0F;
      this.box(272.0F, var33, 94.0F, 60.0F, 12.0F, Style.surface);
      this.tally(287.66666F, var33, this.wins, "Siege", Style.ok);
      this.tally(319.0F, var33, this.draws, "Remis", Style.sub);
      this.tally(350.33334F, var33, this.losses, "Niederl.", Style.danger);
   }

   private void playerCard(float var1, String var2, String var3, int var4, float var5) {
      int var6 = this.pieceColor(var4);
      float var7 = 58.0F;
      if (var5 > 0.02F) {
         Gx.glow(this.X(319.0F), this.Y(var1 + var7 / 2.0F), this.S(58.0F), Gx.withAlpha(var6, 0.16F * var5));
      }

      this.box(272.0F, var1, 94.0F, var7, 12.0F, Gx.mix(Style.surface, var6, 0.1F * var5));
      Gx.outline(
         this.X(272.0F),
         this.Y(var1),
         this.S(94.0F),
         this.S(var7),
         this.S(12.0F),
         Math.max(1, this.S(0.8F)),
         Gx.mix(Style.stroke, Gx.withAlpha(var6, 0.85F), var5)
      );
      this.box(280.0F, var1 + 13.0F, 32.0F, 32.0F, 9.0F, Gx.withAlpha(var6, 0.14F));
      this.glyph(var4, 296.0F, var1 + 29.0F, 8.0F, 1.0F, var6);
      Gx.textMid(var2, this.X(320.0F), this.Y(var1 + 23.0F), 10.0F * this.u, 3, Style.text);
      Gx.textMid(var3, this.X(320.0F), this.Y(var1 + 37.0F), 6.4F * this.u, 1, Style.sub);
      if (var5 > 0.02F) {
         float var8 = 0.6F + 0.4F * (float)Math.sin(this.clock * 6.0F);
         this.circle(356.0F, var1 + 10.0F, 2.6F, Gx.withAlpha(var6, var5 * var8));
      }
   }

   private void tally(float var1, float var2, int var3, String var4, int var5) {
      this.text(String.valueOf(var3), var1, var2 + 24.0F, 13.0F, 3, Style.text);
      this.text(var4, var1, var2 + 43.0F, 5.6F, 2, var5);
   }
}
