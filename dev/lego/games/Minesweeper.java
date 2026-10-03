package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class Minesweeper extends GameView {
   private static final int COLS = 12;
   private static final int ROWS = 10;
   private static final int MINES = 18;
   private static final int N = 120;
   private static final float CELL = 26.0F;
   private static final float GX = 20.0F;
   private static final float GY = 42.0F;
   private static final int[] NUM = new int[]{0, -11688961, -12723068, -44462, -5011201, -30107, -14235942, -2039584, -6381922};
   private static final int[] NUM_LIGHT = new int[]{0, -15374912, -13730510, -2937041, -12245088, -7525874, -16743537, -14606047, -10395295};
   private static final int[] DEBRIS = new int[]{-1900533, -13053, -16098364, -6249047, -30208, -9671064};
   private final boolean[] mine = new boolean[120];
   private final boolean[] open = new boolean[120];
   private final boolean[] flag = new boolean[120];
   private final int[] adj = new int[120];
   private final float[] openAt = new float[120];
   private final float[] flagAt = new float[120];
   private boolean generated;
   private boolean lost;
   private boolean won;
   private int flags;
   private int opened;
   private int boom = -1;
   private float startT;
   private float endSecs;
   private float endT;
   private float clock;
   private float boomAt;
   private final List<float[]> parts = new ArrayList<>();

   public Minesweeper() {
      super("minesweeper");
   }

   @Override
   protected float boardW() {
      return 352.0F;
   }

   @Override
   protected float boardH() {
      return 314.0F;
   }

   @Override
   protected void reset() {
      for (int var1 = 0; var1 < 120; var1++) {
         this.mine[var1] = this.open[var1] = this.flag[var1] = false;
         this.adj[var1] = 0;
         this.openAt[var1] = this.flagAt[var1] = 0.0F;
      }

      this.generated = this.lost = this.won = false;
      this.flags = this.opened = 0;
      this.boom = -1;
      this.startT = this.endSecs = this.endT = 0.0F;
      this.parts.clear();
   }

   @Override
   protected String overTitle() {
      return this.won ? "Geschafft!" : "Verloren";
   }

   private float secs() {
      if (!this.generated) {
         return 0.0F;
      } else {
         return !this.won && !this.lost ? Math.max(0.0F, this.time - this.startT) : this.endSecs;
      }
   }

   @Override
   protected void update(float var1) {
      if (this.won) {
         this.endT += var1;
         if (this.endT > 1.3F) {
            this.gameOver();
         }
      } else if (this.lost) {
         this.endT += var1;
         if (this.endT > 1.8F) {
            this.gameOver(false);
         }
      }
   }

   private int cellAt(float var1, float var2) {
      int var3 = (int)Math.floor((var1 - 20.0F) / 26.0F);
      int var4 = (int)Math.floor((var2 - 42.0F) / 26.0F);
      return var3 >= 0 && var4 >= 0 && var3 < 12 && var4 < 10 ? var4 * 12 + var3 : -1;
   }

   private List<Integer> neighbours(int var1) {
      ArrayList var2 = new ArrayList(8);
      int var3 = var1 % 12;
      int var4 = var1 / 12;

      for (int var5 = -1; var5 <= 1; var5++) {
         for (int var6 = -1; var6 <= 1; var6++) {
            if (var6 != 0 || var5 != 0) {
               int var7 = var3 + var6;
               int var8 = var4 + var5;
               if (var7 >= 0 && var8 >= 0 && var7 < 12 && var8 < 10) {
                  var2.add(var8 * 12 + var7);
               }
            }
         }
      }

      return var2;
   }

   private void generate(int var1) {
      List var2 = this.neighbours(var1);
      var2.add(var1);
      int var3 = 0;

      while (var3 < 18) {
         int var4 = this.rnd.nextInt(120);
         if (!this.mine[var4] && !var2.contains(var4)) {
            this.mine[var4] = true;
            var3++;
         }
      }

      for (int var8 = 0; var8 < 120; var8++) {
         int var5 = 0;

         for (int var7 : this.neighbours(var8)) {
            if (this.mine[var7]) {
               var5++;
            }
         }

         this.adj[var8] = var5;
      }

      this.generated = true;
      this.startT = this.time;
   }

   @Override
   protected void onClick(float var1, float var2, int var3) {
      if (!this.won && !this.lost) {
         int var4 = this.cellAt(var1, var2);
         if (var4 >= 0) {
            if (var3 == 1) {
               if (!this.open[var4]) {
                  this.flag[var4] = !this.flag[var4];
                  this.flags = this.flags + (this.flag[var4] ? 1 : -1);
                  this.flagAt[var4] = this.clock;
                  Sound.play("minecraft:block.note_block.hat", this.flag[var4] ? 1.6F : 1.2F, 0.35F);
               }
            } else if (!this.flag[var4]) {
               if (!this.generated) {
                  this.generate(var4);
               }

               if (this.open[var4]) {
                  if (this.adj[var4] != 0) {
                     int var9 = 0;

                     for (int var7 : this.neighbours(var4)) {
                        if (this.flag[var7]) {
                           var9++;
                        }
                     }

                     if (var9 != this.adj[var4]) {
                        Sound.play("minecraft:block.note_block.bass", 1.4F, 0.2F);
                     } else {
                        boolean var10 = false;

                        for (int var8 : this.neighbours(var4)) {
                           if (!this.flag[var8] && !this.open[var8]) {
                              if (this.mine[var8]) {
                                 this.explode(var8);
                                 return;
                              }

                              this.reveal(var8, false);
                              var10 = true;
                           }
                        }

                        if (var10) {
                           Sound.play("minecraft:block.stone.break", 1.35F + this.rnd.nextFloat() * 0.2F, 0.25F);
                           this.checkWin();
                        }
                     }
                  }
               } else if (this.mine[var4]) {
                  this.explode(var4);
               } else {
                  int var5 = this.reveal(var4, true);
                  Sound.play(
                     var5 > 6 ? "minecraft:block.stone.break" : "minecraft:block.stone.hit",
                     var5 > 6 ? 1.1F : 1.5F + this.rnd.nextFloat() * 0.2F,
                     var5 > 6 ? 0.3F : 0.4F
                  );
                  this.checkWin();
               }
            }
         }
      }
   }

   private int reveal(int var1, boolean var2) {
      ArrayDeque var3 = new ArrayDeque();
      var3.add(new int[]{var1, 0});
      int var4 = 0;

      while (!var3.isEmpty()) {
         int[] var5 = (int[])var3.poll();
         int var6 = var5[0];
         if (!this.open[var6] && !this.flag[var6] && !this.mine[var6]) {
            this.open[var6] = true;
            this.opened++;
            var4++;
            this.openAt[var6] = this.clock + (var2 ? Math.min(0.5F, var5[1] * 0.035F) : 0.0F);
            if (this.adj[var6] == 0) {
               for (int var8 : this.neighbours(var6)) {
                  if (!this.open[var8] && !this.flag[var8]) {
                     var3.add(new int[]{var8, var5[1] + 1});
                  }
               }
            }
         }
      }

      return var4;
   }

   private void checkWin() {
      if (this.opened >= 102) {
         this.won = true;
         this.endSecs = Math.max(0.0F, this.time - this.startT);
         this.score = Math.max(1L, 1000L - (long)Math.floor(this.endSecs) * 5L);

         for (int var1 = 0; var1 < 120; var1++) {
            if (this.mine[var1] && !this.flag[var1]) {
               this.flag[var1] = true;
               this.flagAt[var1] = this.clock + 0.15F + this.rnd.nextFloat() * 0.35F;
            }
         }

         this.flags = 18;
         Sound.play("minecraft:entity.player.levelup", 1.2F, 0.4F);
      }
   }

   private void explode(int var1) {
      this.lost = true;
      this.boom = var1;
      this.boomAt = this.clock;
      this.endSecs = Math.max(0.0F, this.time - this.startT);
      int var2 = var1 % 12;
      int var3 = var1 / 12;

      for (int var4 = 0; var4 < 120; var4++) {
         if (this.mine[var4] && !this.flag[var4]) {
            this.open[var4] = true;
            float var5 = (float)Math.hypot(var4 % 12 - var2, var4 / 12 - var3);
            this.openAt[var4] = var4 == var1 ? this.clock : this.clock + 0.15F + var5 * 0.05F;
         }
      }

      float var10 = 20.0F + var2 * 26.0F + 13.0F;
      float var11 = 42.0F + var3 * 26.0F + 13.0F;

      for (int var6 = 0; var6 < 34; var6++) {
         double var7 = this.rnd.nextDouble() * Math.PI * 2.0;
         float var9 = 50.0F + this.rnd.nextFloat() * 170.0F;
         this.parts
            .add(
               new float[]{
                  var10,
                  var11,
                  (float)Math.cos(var7) * var9,
                  (float)Math.sin(var7) * var9 - 80.0F,
                  0.7F + this.rnd.nextFloat() * 0.7F,
                  DEBRIS[this.rnd.nextInt(DEBRIS.length)],
                  2.5F + this.rnd.nextFloat() * 3.0F
               }
            );
      }

      Sound.play("minecraft:entity.generic.explode", 1.1F, 0.45F);
   }

   @Override
   protected void render() {
      float var1 = Gx.dt;
      this.clock += var1;
      float var2 = (this.mx - this.bx) / this.u;
      float var3 = (this.my - this.by) / this.u;
      boolean var4 = this.state == GameView.State.PLAYING && !this.won && !this.lost;
      int var5 = var4 ? this.cellAt(var2, var3) : -1;
      boolean var6 = var5 >= 0 && this.open[var5] && this.adj[var5] > 0;
      this.header();
      float var7 = this.lost ? Math.max(0.0F, 1.0F - (this.clock - this.boomAt) / 0.4F) : 0.0F;
      Gx.push();
      if (var7 > 0.0F) {
         Gx.translate(this.S((this.rnd.nextFloat() - 0.5F) * 5.0F * var7), this.S((this.rnd.nextFloat() - 0.5F) * 5.0F * var7));
      }

      int var8 = Style.light ? -2762272 : -15460836;
      int var9 = Style.light ? -3222822 : -15658216;
      this.box(17.0F, 39.0F, 318.0F, 266.0F, 9.0F, Style.light ? 301989888 : 234881023);

      for (int var10 = 0; var10 < 120; var10++) {
         int var11 = var10 % 12;
         int var12 = var10 / 12;
         float var13 = 20.0F + var11 * 26.0F;
         float var14 = 42.0F + var12 * 26.0F;
         float var15 = var13 + 13.0F;
         float var16 = var14 + 13.0F;
         boolean var17 = this.open[var10] && this.clock >= this.openAt[var10];
         float var18 = this.open[var10] ? Ease.clamp((this.clock - this.openAt[var10]) / 0.2F) : 0.0F;
         if (this.open[var10] && var17) {
            int var19 = (var11 + var12) % 2 == 0 ? var8 : var9;
            if (this.mine[var10] && var10 == this.boom) {
               var19 = -1900533;
            } else if (this.mine[var10]) {
               var19 = Gx.mix(var19, -1900533, 0.25F);
            }

            if (var6 && var10 == var5) {
               var19 = Gx.mix(var19, Style.accent, 0.15F);
            }

            this.box(var13 + 0.6F, var14 + 0.6F, 24.8F, 24.8F, 4.0F, var19);
            if (this.mine[var10]) {
               if (var10 == this.boom) {
                  Gx.glow(this.X(var15), this.Y(var16), this.S(30.0F), Gx.withAlpha(-30208, 0.5F));
               }

               this.mineIcon(var15, var16, Ease.outBack(var18));
            } else if (this.adj[var10] > 0) {
               float var20 = 0.6F + 0.4F * Ease.outBack(var18);
               int var21 = (Style.light ? NUM_LIGHT : NUM)[this.adj[var10]];
               Gx.push();
               Gx.scaleAt(this.X(var15), this.Y(var16), var20);
               this.text(String.valueOf(this.adj[var10]), var15, var16, 11.0F, 3, Gx.withAlpha(var21, var18));
               Gx.pop();
            }
         }

         if (!this.open[var10] || var18 < 1.0F) {
            float var32 = this.open[var10] ? 1.0F - 0.4F * Ease.outCubic(var18) : 1.0F;
            float var33 = this.open[var10] ? 1.0F - var18 : 1.0F;
            int var34 = (var11 + var12) % 2 == 0 ? -6972509 : -7564646;
            if (this.won && this.mine[var10]) {
               var34 = Gx.mix(var34, Style.ok, 0.45F * Ease.clamp((this.clock - this.flagAt[var10]) / 0.3F));
            }

            if (var10 == var5 && !this.open[var10]) {
               var34 = Gx.mix(var34, -1, 0.25F);
            } else if (var6 && !this.open[var10] && !this.flag[var10] && this.isNeighbour(var5, var10)) {
               var34 = Gx.mix(var34, Style.accent, 0.3F);
            }

            Gx.push();
            Gx.pushAlpha(var33);
            Gx.scaleAt(this.X(var15), this.Y(var16), var32);
            float var22 = var10 == var5 && !this.open[var10] ? 0.8F : 0.0F;
            this.brick(var13 + 1.0F, var14 + 1.0F - var22, 24.0F, 24.0F, var34);
            if (this.flag[var10]) {
               float var23 = Ease.outBack((this.clock - this.flagAt[var10]) / 0.25F);
               if (var23 > 0.0F) {
                  Gx.icon("flag", this.X(var15 + 0.6F), this.Y(var16 + 0.8F - var22), this.S(12.0F) * var23, 1711276032);
                  Gx.icon("flag", this.X(var15), this.Y(var16 - var22), this.S(12.0F) * var23, -1900533);
               }

               if (this.lost && !this.mine[var10]) {
                  float var24 = Ease.clamp((this.clock - this.boomAt - 0.3F) / 0.2F);
                  Gx.icon("x", this.X(var15), this.Y(var16), this.S(17.0F), Gx.withAlpha(-1, var24));
               }
            }

            Gx.popAlpha();
            Gx.pop();
         }
      }

      if (this.lost) {
         float var25 = this.clock - this.boomAt;
         float var26 = 20.0F + this.boom % 12 * 26.0F + 13.0F;
         float var27 = 42.0F + this.boom / 12 * 26.0F + 13.0F;
         if (var25 < 0.7F) {
            float var28 = var25 / 0.7F;
            Gx.glow(this.X(var26), this.Y(var27), this.S(20.0F + 70.0F * Ease.outCubic(var28)), Gx.withAlpha(-24003, 0.8F * (1.0F - var28)));
            Gx.ringAt(
               this.X(var26),
               this.Y(var27),
               this.S(6.0F + 80.0F * Ease.outCubic(var28)),
               Math.max(1, this.S(3.0F * (1.0F - var28) + 0.5F)),
               Gx.withAlpha(-1, 0.8F * (1.0F - var28))
            );
         }

         for (float[] var30 : this.parts) {
            var30[3] += 420.0F * var1;
            var30[0] += var30[2] * var1;
            var30[1] += var30[3] * var1;
            var30[4] -= var1;
            if (!(var30[4] <= 0.0F)) {
               float var31 = Math.min(1.0F, var30[4] / 0.3F);
               this.box(var30[0] - var30[6] / 2.0F, var30[1] - var30[6] / 2.0F, var30[6], var30[6], 1.0F, Gx.withAlpha((int)var30[5], var31));
            }
         }

         this.parts.removeIf(var0 -> var0[4] <= 0.0F);
      }

      Gx.pop();
   }

   private boolean isNeighbour(int var1, int var2) {
      int var3 = var1 % 12;
      int var4 = var1 / 12;
      int var5 = var2 % 12;
      int var6 = var2 / 12;
      return var1 != var2 && Math.abs(var3 - var5) <= 1 && Math.abs(var4 - var6) <= 1;
   }

   private void mineIcon(float var1, float var2, float var3) {
      if (!(var3 <= 0.0F)) {
         float var4 = 5.6F * var3;

         for (int var5 = 0; var5 < 8; var5++) {
            double var6 = var5 * Math.PI / 4.0;
            this.circle(var1 + (float)Math.cos(var6) * var4 * 1.2F, var2 + (float)Math.sin(var6) * var4 * 1.2F, 1.3F * var3, -15000286);
         }

         this.circle(var1, var2, var4, -15000286);
         this.circle(var1, var2, var4 * 0.62F, -13618373);
         this.circle(var1 - var4 * 0.35F, var2 - var4 * 0.35F, var4 * 0.26F, -855638017);
      }
   }

   private void header() {
      float var1 = 8.0F;
      float var2 = 26.0F;
      int var3 = 18 - this.flags;
      this.box(20.0F, var1, 76.0F, var2, 13.0F, Style.surface);
      Gx.icon("flag", this.X(36.0F), this.Y(var1 + var2 / 2.0F), this.S(12.0F), -1900533);
      Gx.textMid(String.valueOf(var3), this.X(48.0F), this.Y(var1 + var2 / 2.0F), 10.0F * this.u, 3, var3 < 0 ? Style.danger : Style.text);
      Gx.textRight("Minen", this.X(88.0F), this.Y(var1 + var2 / 2.0F + 0.5F), 6.0F * this.u, 2, Style.muted);
      float var4 = 76.0F;
      float var5 = 332.0F - var4;
      int var6 = (int)this.secs();
      this.box(var5, var1, var4, var2, 13.0F, Style.surface);
      Gx.icon("clock", this.X(var5 + 16.0F), this.Y(var1 + var2 / 2.0F), this.S(11.0F), Style.accent);
      Gx.textMid(String.format("%d:%02d", var6 / 60, var6 % 60), this.X(var5 + 28.0F), this.Y(var1 + var2 / 2.0F), 10.0F * this.u, 3, Style.text);
      int var8 = Style.sub;
      String var7;
      if (this.won) {
         var7 = "Alle Minen gefunden!";
         var8 = Style.ok;
      } else if (this.lost) {
         var7 = "BOOM! Mine erwischt";
         var8 = Style.danger;
      } else if (!this.generated) {
         var7 = "Erster Klick ist sicher";
      } else {
         var7 = Math.round(this.opened * 100.0F / 102.0F) + "% aufgedeckt";
      }

      this.text(var7, 176.0F, var1 + var2 / 2.0F, 7.4F, 2, var8);
      if (this.generated && !this.won && !this.lost) {
         float var9 = 90.0F;
         float var10 = 176.0F - var9 / 2.0F;
         this.box(var10, var1 + var2 - 3.0F, var9, 2.0F, 1.0F, Style.surface2);
         this.box(var10, var1 + var2 - 3.0F, var9 * this.opened / 102.0F, 2.0F, 1.0F, Style.accent);
      }
   }
}
