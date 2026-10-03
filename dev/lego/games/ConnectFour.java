package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ConnectFour extends GameView {
   private static final int COLS = 7;
   private static final int ROWS = 6;
   private static final int DEPTH = 5;
   private static final float C = 38.0F;
   private static final float GX = 14.0F;
   private static final float GY = 50.0F;
   private static final float TOPY = 25.0F;
   private static final float PR = 15.0F;
   private static final float PX = 300.0F;
   private static final float PW = 88.0F;
   private static final int YEL = -13053;
   private static final int RED = -1900533;
   private static final int BLUE = -16098364;
   private static final int[] ORDER = new int[]{3, 2, 4, 1, 5, 0, 6};
   private static final int NONE = 0;
   private static final int WIN = 1;
   private static final int DRAW = 2;
   private static final int LOSS = 3;
   private static final int WIN_SCORE = 1000000;
   private final int[][] g = new int[6][7];
   private final float[] colHover = new float[7];
   private int turn = 1;
   private int result = 0;
   private boolean playerStarts = true;
   private float think;
   private float endT;
   private float clock;
   private float winAt;
   private List<int[]> win4;
   private int fr = -1;
   private int fc = -1;
   private int fwho;
   private int bounces;
   private float fy;
   private float fvy;
   private float previewX = -1.0F;
   private float act1 = 1.0F;
   private float act2 = 0.0F;
   private int wins;
   private int draws;
   private int losses;

   public ConnectFour() {
      super("connect4");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 294.0F;
   }

   @Override
   protected void reset() {
      for (int[] var4 : this.g) {
         Arrays.fill(var4, 0);
      }

      this.turn = this.playerStarts ? 1 : 2;
      this.result = 0;
      this.think = 0.0F;
      this.endT = 0.0F;
      this.win4 = null;
      this.fr = this.fc = -1;
   }

   @Override
   protected String overTitle() {
      return this.result == 1 ? "Gewonnen!" : (this.result == 2 ? "Unentschieden" : "Verloren");
   }

   @Override
   protected void update(float var1) {
      if (this.fr < 0) {
         if (this.result != 0) {
            this.endT += var1;
            if (this.endT > 1.3F) {
               this.gameOver();
            }
         } else {
            if (this.turn == 2) {
               this.think += var1;
               if (this.think >= 0.45F) {
                  this.drop(this.aiMove(), 2);
               }
            }
         }
      } else {
         this.fvy += 1700.0F * var1;
         this.fy = this.fy + this.fvy * var1;
         float var2 = 50.0F + this.fr * 38.0F + 19.0F;
         if (this.fy >= var2) {
            this.fy = var2;
            if (this.bounces < 2 && this.fvy > 160.0F) {
               if (this.bounces == 0) {
                  Sound.play("minecraft:block.stone.place", 1.5F + this.rnd.nextFloat() * 0.2F, 0.35F);
               }

               this.fvy = -this.fvy * 0.24F;
               this.bounces++;
            } else {
               this.land();
            }
         }
      }
   }

   @Override
   protected void onClick(float var1, float var2, int var3) {
      if (var3 == 0) {
         int var4 = this.colAt(var1, var2);
         if (var4 >= 0) {
            this.playerDrop(var4);
         }
      }
   }

   @Override
   protected void onKey(int var1) {
      if (var1 >= 49 && var1 <= 55) {
         this.playerDrop(var1 - 49);
      } else if (var1 >= 321 && var1 <= 327) {
         this.playerDrop(var1 - 321);
      }
   }

   private void playerDrop(int var1) {
      if (this.turn == 1 && this.result == 0 && this.fr < 0) {
         this.drop(var1, 1);
      }
   }

   private int colAt(float var1, float var2) {
      if (!(var2 < 0.0F) && !(var2 > 284.0F)) {
         int var3 = (int)Math.floor((var1 - 14.0F) / 38.0F);
         return var3 >= 0 && var3 < 7 ? var3 : -1;
      } else {
         return -1;
      }
   }

   private static int openRow(int[][] var0, int var1) {
      for (int var2 = 5; var2 >= 0; var2--) {
         if (var0[var2][var1] == 0) {
            return var2;
         }
      }

      return -1;
   }

   private void drop(int var1, int var2) {
      if (var1 >= 0) {
         int var3 = openRow(this.g, var1);
         if (var3 >= 0) {
            this.g[var3][var1] = var2;
            this.fr = var3;
            this.fc = var1;
            this.fwho = var2;
            this.fy = 25.0F;
            this.fvy = 90.0F;
            this.bounces = 0;
            this.turn = 3 - var2;
            this.think = 0.0F;
            Sound.play("minecraft:ui.button.click", var2 == 1 ? 1.4F : 1.2F, 0.15F);
         }
      }
   }

   private void land() {
      int var1 = this.fr;
      int var2 = this.fc;
      int var3 = this.fwho;
      this.fr = this.fc = -1;
      List var4 = line(this.g, var1, var2, var3);
      if (var4 != null) {
         this.win4 = var4;
         this.winAt = this.clock;
         this.result = var3 == 1 ? 1 : 3;
         if (var3 == 1) {
            this.wins++;
            this.score = 10 + this.empty() / 2;
            Sound.play("minecraft:entity.player.levelup", 1.3F, 0.35F);
         } else {
            this.losses++;
            this.score = 0L;
         }

         this.playerStarts = !this.playerStarts;
      } else if (this.empty() == 0) {
         this.result = 2;
         this.draws++;
         this.score = 3L;
         this.playerStarts = !this.playerStarts;
      }
   }

   private int empty() {
      int var1 = 0;

      for (int[] var5 : this.g) {
         for (int var9 : var5) {
            if (var9 == 0) {
               var1++;
            }
         }
      }

      return var1;
   }

   private static List<int[]> line(int[][] var0, int var1, int var2, int var3) {
      int[][] var4 = new int[][]{{0, 1}, {1, 0}, {1, 1}, {1, -1}};

      for (int[] var8 : var4) {
         ArrayList var9 = new ArrayList();
         var9.add(new int[]{var1, var2});

         for (byte var10 = -1; var10 <= 1; var10 += 2) {
            int var11 = var1 + var8[0] * var10;

            for (int var12 = var2 + var8[1] * var10; var11 >= 0 && var11 < 6 && var12 >= 0 && var12 < 7 && var0[var11][var12] == var3; var12 += var8[1] * var10) {
               var9.add(new int[]{var11, var12});
               var11 += var8[0] * var10;
            }
         }

         if (var9.size() >= 4) {
            return var9;
         }
      }

      return null;
   }

   private static boolean wins(int[][] var0, int var1, int var2, int var3) {
      int[][] var4 = new int[][]{{0, 1}, {1, 0}, {1, 1}, {1, -1}};

      for (int[] var8 : var4) {
         int var9 = 1;

         for (byte var10 = -1; var10 <= 1; var10 += 2) {
            int var11 = var1 + var8[0] * var10;

            for (int var12 = var2 + var8[1] * var10; var11 >= 0 && var11 < 6 && var12 >= 0 && var12 < 7 && var0[var11][var12] == var3; var12 += var8[1] * var10) {
               var9++;
               var11 += var8[0] * var10;
            }
         }

         if (var9 >= 4) {
            return true;
         }
      }

      return false;
   }

   private int aiMove() {
      int[][] var1 = new int[6][];

      for (int var2 = 0; var2 < 6; var2++) {
         var1[var2] = (int[])this.g[var2].clone();
      }

      int var10 = Integer.MIN_VALUE;
      ArrayList var3 = new ArrayList();

      for (int var7 : ORDER) {
         int var8 = openRow(var1, var7);
         if (var8 >= 0) {
            var1[var8][var7] = 2;
            int var9 = wins(var1, var8, var7, 2) ? 1000005 : -this.negamax(var1, 4, -2147483647, Integer.MAX_VALUE, 1);
            var1[var8][var7] = 0;
            if (var9 > var10) {
               var10 = var9;
               var3.clear();
               var3.add(var7);
            } else if (var9 == var10) {
               var3.add(var7);
            }
         }
      }

      return var3.isEmpty() ? -1 : (Integer)var3.get(this.rnd.nextInt(var3.size()));
   }

   private int negamax(int[][] var1, int var2, int var3, int var4, int var5) {
      boolean var6 = false;

      for (int var7 = 0; var7 < 7; var7++) {
         if (var1[0][var7] == 0) {
            var6 = true;
            break;
         }
      }

      if (!var6) {
         return 0;
      } else if (var2 == 0) {
         int var15 = eval(var1);
         return var5 == 2 ? var15 : -var15;
      } else {
         int var14 = -2147483647;

         for (int var11 : ORDER) {
            int var12 = openRow(var1, var11);
            if (var12 >= 0) {
               var1[var12][var11] = var5;
               int var13 = wins(var1, var12, var11, var5) ? 1000000 + var2 : -this.negamax(var1, var2 - 1, -var4, -var3, 3 - var5);
               var1[var12][var11] = 0;
               if (var13 > var14) {
                  var14 = var13;
               }

               if (var13 > var3) {
                  var3 = var13;
               }

               if (var3 >= var4) {
                  break;
               }
            }
         }

         return var14;
      }
   }

   private static int eval(int[][] var0) {
      byte var1 = 0;

      for (int var2 = 0; var2 < 6; var2++) {
         if (var0[var2][3] == 2) {
            var1 += 3;
         } else if (var0[var2][3] == 1) {
            var1 -= 3;
         }
      }

      int[][] var15 = new int[][]{{0, 1}, {1, 0}, {1, 1}, {1, -1}};

      for (int var3 = 0; var3 < 6; var3++) {
         for (int var4 = 0; var4 < 7; var4++) {
            for (int[] var8 : var15) {
               int var9 = var3 + var8[0] * 3;
               int var10 = var4 + var8[1] * 3;
               if (var9 >= 0 && var9 < 6 && var10 >= 0 && var10 < 7) {
                  int var11 = 0;
                  int var12 = 0;

                  for (int var13 = 0; var13 < 4; var13++) {
                     int var14 = var0[var3 + var8[0] * var13][var4 + var8[1] * var13];
                     if (var14 == 2) {
                        var11++;
                     } else if (var14 == 1) {
                        var12++;
                     }
                  }

                  if (var11 <= 0 || var12 <= 0) {
                     if (var11 == 3) {
                        var1 += 6;
                     } else if (var11 == 2) {
                        var1 += 2;
                     }

                     if (var12 == 3) {
                        var1 -= 7;
                     } else if (var12 == 2) {
                        var1 -= 2;
                     }
                  }
               }
            }
         }
      }

      return var1;
   }

   private void piece(float var1, float var2, int var3, float var4, float var5) {
      if (var5 > 0.0F) {
         var3 = Gx.mix(var3, -16777216, var5);
      }

      float var6 = 15.0F * var4;
      this.circle(var1, var2 + 1.4F * var4, var6, Gx.mix(var3, -16777216, 0.5F));
      this.circle(var1, var2, var6, Gx.mix(var3, -16777216, 0.08F));
      this.circle(var1, var2 - 0.3F * var4, var6 * 0.86F, Gx.mix(var3, -1, 0.1F));
      this.circle(var1, var2 + 0.5F * var4, var6 * 0.52F, Gx.mix(var3, -16777216, 0.16F));
      this.circle(var1, var2 - 0.3F * var4, var6 * 0.47F, Gx.mix(var3, -1, 0.2F));
      this.circle(var1 - var6 * 0.16F, var2 - var6 * 0.18F, var6 * 0.16F, Gx.withAlpha(-1, 0.55F * (1.0F - var5)));
   }

   private float cx(int var1) {
      return 14.0F + var1 * 38.0F + 19.0F;
   }

   private float cy(int var1) {
      return 50.0F + var1 * 38.0F + 19.0F;
   }

   @Override
   protected void render() {
      this.clock = this.clock + Gx.dt;
      float var1 = 1.0F - (float)Math.exp(-Gx.dt * 14.0F);
      float var2 = (this.mx - this.bx) / this.u;
      float var3 = (this.my - this.by) / this.u;
      boolean var4 = this.state == GameView.State.PLAYING && this.turn == 1 && this.result == 0 && this.fr < 0;
      int var5 = var4 ? this.colAt(var2, var3) : -1;
      if (var5 >= 0 && openRow(this.g, var5) < 0) {
         var5 = -1;
      }

      float var6 = this.win4 != null ? Ease.outCubic((this.clock - this.winAt) / 0.4F) : 0.0F;

      for (int var7 = 0; var7 < 7; var7++) {
         this.text(String.valueOf(var7 + 1), this.cx(var7), 25.0F, 7.0F, 3, Gx.withAlpha(Style.sub, 0.35F * (1.0F - this.colHover[var7])));
      }

      float var17 = 8.0F;
      float var8 = 44.0F;
      float var9 = 278.0F;
      float var10 = 240.0F;
      Gx.shadow(this.X(var17), this.Y(var8), this.S(var9), this.S(var10), this.S(14.0F), this.S(8.0F), 0.45F);
      this.boxV(var17, var8, var9, var10, 14.0F, Gx.mix(-16098364, -1, 0.12F), Gx.mix(-16098364, -16777216, 0.2F));
      Gx.outline(this.X(var17), this.Y(var8), this.S(var9), this.S(var10), this.S(14.0F), Math.max(1, this.S(0.8F)), Gx.withAlpha(-16777216, 0.35F));

      for (int var11 = 0; var11 < 7; var11++) {
         this.colHover[var11] = this.colHover[var11] + ((var11 == var5 ? 1 : 0) - this.colHover[var11]) * var1;
         if (this.colHover[var11] > 0.01F) {
            this.box(14.0F + var11 * 38.0F + 2.0F, 47.0F, 34.0F, 234.0F, 12.0F, Gx.withAlpha(-1, 0.1F * this.colHover[var11]));
         }
      }

      int var18 = Style.light ? -15982000 : -16379606;

      for (int var12 = 0; var12 < 6; var12++) {
         for (int var13 = 0; var13 < 7; var13++) {
            this.circle(this.cx(var13), this.cy(var12) - 0.8F, 16.4F, Gx.mix(-16098364, -16777216, 0.45F));
            this.circle(this.cx(var13), this.cy(var12), 15.6F, Gx.mix(-16098364, -1, 0.14F));
            this.circle(this.cx(var13), this.cy(var12), 15.0F, var18);
         }
      }

      for (int var19 = 0; var19 < 6; var19++) {
         for (int var22 = 0; var22 < 7; var22++) {
            if (this.g[var19][var22] != 0 && (var19 != this.fr || var22 != this.fc)) {
               boolean var14 = this.inWin(var19, var22);
               float var15 = this.win4 != null && !var14 ? 0.45F * var6 : 0.0F;
               float var16 = 1.0F;
               if (var14) {
                  var16 = 1.0F + 0.06F * (float)Math.sin((this.clock - this.winAt) * 7.0F) * var6;
                  Gx.glow(this.X(this.cx(var22)), this.Y(this.cy(var19)), this.S(30.0F), Gx.withAlpha(-1, 0.3F * var6));
               }

               this.piece(this.cx(var22), this.cy(var19), this.g[var19][var22] == 1 ? -13053 : -1900533, var16, var15);
               if (var14) {
                  Gx.ringAt(this.X(this.cx(var22)), this.Y(this.cy(var19)), this.S(17.2F), Math.max(1, this.S(1.6F)), Gx.withAlpha(-1, 0.9F * var6));
               }
            }
         }
      }

      if (this.fr >= 0) {
         this.piece(this.cx(this.fc), this.fy, this.fwho == 1 ? -13053 : -1900533, 1.0F, 0.0F);
      }

      if (var5 >= 0) {
         float var20 = this.cx(var5);
         this.previewX = this.previewX < 0.0F ? var20 : this.previewX + (var20 - this.previewX) * (1.0F - (float)Math.exp(-Gx.dt * 22.0F));
         float var23 = (float)Math.sin(this.clock * 5.0F) * 1.2F;
         Gx.glow(this.X(this.previewX), this.Y(25.0F), this.S(26.0F), Gx.withAlpha(-13053, 0.25F));
         this.piece(this.previewX, 25.0F + var23, -13053, 1.0F, 0.0F);
         int var25 = openRow(this.g, var5);
         float var27 = 0.45F + 0.25F * (float)Math.sin(this.clock * 6.0F);
         Gx.ringAt(this.X(var20), this.Y(this.cy(var25)), this.S(14.0F), Math.max(1, this.S(1.4F)), Gx.withAlpha(-13053, var27 * this.colHover[var5]));
      } else {
         this.previewX = -1.0F;
      }

      this.act1 = this.act1 + ((this.turn == 1 && this.result == 0 ? 1 : 0) - this.act1) * var1;
      this.act2 = this.act2 + ((this.turn == 2 && this.result == 0 ? 1 : 0) - this.act2) * var1;
      this.playerCard(8.0F, "Du", "Gelb", -13053, this.act1);
      this.playerCard(72.0F, "KI", "Rot", -1900533, this.act2);
      int var24 = Style.text;
      String var21;
      if (this.result == 1) {
         var21 = "Gewonnen!";
         var24 = Style.ok;
      } else if (this.result == 3) {
         var21 = "Verloren";
         var24 = Style.danger;
      } else if (this.result == 2) {
         var21 = "Unentschieden";
         var24 = Style.sub;
      } else if (this.turn == 2) {
         var21 = "KI denkt" + ".".repeat(1 + (int)(this.clock * 4.0F) % 3);
      } else {
         var21 = "Du bist dran";
      }

      float var26 = 152.0F;
      this.text(var21, 344.0F, var26, 9.5F, 3, var24);
      this.text(this.result == 0 ? "Freie Felder: " + this.empty() : "Runde beendet", 344.0F, var26 + 14.0F, 6.6F, 1, Style.sub);
      float var28 = 180.0F;
      this.box(300.0F, var28, 88.0F, 60.0F, 12.0F, Style.surface);
      this.tally(314.66666F, var28, this.wins, "Siege", Style.ok);
      this.tally(344.0F, var28, this.draws, "Remis", Style.sub);
      this.tally(373.33334F, var28, this.losses, "Niederl.", Style.danger);
      this.text("Tasten 1–7 werfen", 344.0F, var28 + 74.0F, 6.2F, 2, Style.muted);
   }

   private boolean inWin(int var1, int var2) {
      if (this.win4 == null) {
         return false;
      } else {
         for (int[] var4 : this.win4) {
            if (var4[0] == var1 && var4[1] == var2) {
               return true;
            }
         }

         return false;
      }
   }

   private void playerCard(float var1, String var2, String var3, int var4, float var5) {
      float var6 = 56.0F;
      if (var5 > 0.02F) {
         Gx.glow(this.X(344.0F), this.Y(var1 + var6 / 2.0F), this.S(56.0F), Gx.withAlpha(var4, 0.14F * var5));
      }

      this.box(300.0F, var1, 88.0F, var6, 12.0F, Gx.mix(Style.surface, var4, 0.09F * var5));
      Gx.outline(
         this.X(300.0F),
         this.Y(var1),
         this.S(88.0F),
         this.S(var6),
         this.S(12.0F),
         Math.max(1, this.S(0.8F)),
         Gx.mix(Style.stroke, Gx.withAlpha(var4, 0.85F), var5)
      );
      this.piece(324.0F, var1 + var6 / 2.0F, var4, 0.85F, 0.0F);
      Gx.textMid(var2, this.X(346.0F), this.Y(var1 + 22.0F), 10.0F * this.u, 3, Style.text);
      Gx.textMid(var3, this.X(346.0F), this.Y(var1 + 36.0F), 6.4F * this.u, 1, Style.sub);
      if (var5 > 0.02F) {
         float var7 = 0.6F + 0.4F * (float)Math.sin(this.clock * 6.0F);
         this.circle(378.0F, var1 + 10.0F, 2.6F, Gx.withAlpha(var4, var5 * var7));
      }
   }

   private void tally(float var1, float var2, int var3, String var4, int var5) {
      this.text(String.valueOf(var3), var1, var2 + 24.0F, 13.0F, 3, Style.text);
      this.text(var4, var1, var2 + 43.0F, 5.4F, 2, var5);
   }
}
