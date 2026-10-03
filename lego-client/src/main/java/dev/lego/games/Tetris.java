package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;

public final class Tetris extends GameView {
   private static final int COLS = 10;
   private static final int ROWS = 20;
   private static final int K_C = 67;
   private static final float CELL = 12.0F;
   private static final float GX = 130.0F;
   private static final float GY = 14.0F;
   private static final float LX = 12.0F;
   private static final float RX = 262.0F;
   private static final float PW = 106.0F;
   private static final String[][] SHAPES = new String[][]{
      {"....", "####", "....", "...."},
      {"##", "##"},
      {".#.", "###", "..."},
      {".##", "##.", "..."},
      {"##.", ".##", "..."},
      {"#..", "###", "..."},
      {"..#", "###", "..."}
   };
   private static final int[] COLORS = new int[]{-13193537, -864969, -7194248, -11821238, -3597815, -16755265, -95720};
   private static final int[][] KICKS = new int[][]{{0, 0}, {-1, 0}, {1, 0}, {0, -1}, {-2, 0}, {2, 0}, {-1, -1}, {1, -1}};
   private int[][] grid = new int[20][10];
   private int type;
   private int rot;
   private int px;
   private int py;
   private boolean active;
   private int hold = -1;
   private boolean canHold;
   private final List<Integer> bag = new ArrayList<>();
   private final List<Integer> queue = new ArrayList<>();
   private int lines;
   private int level;
   private float fall;
   private float lockT;
   private int lockResets;
   private final List<Integer> clearing = new ArrayList<>();
   private float clearT = -1.0F;
   private float visX;
   private float visY;
   private float shake;
   private float levelT = -1.0F;
   private float holdPop;
   private String banner;
   private float bannerT = -1.0F;
   private final List<float[]> parts = new ArrayList<>();

   public Tetris() {
      super("tetris");
   }

   @Override
   protected float boardW() {
      return 380.0F;
   }

   @Override
   protected float boardH() {
      return 268.0F;
   }

   @Override
   protected void reset() {
      this.grid = new int[20][10];
      this.bag.clear();
      this.queue.clear();
      this.parts.clear();
      this.clearing.clear();
      this.clearT = -1.0F;
      this.hold = -1;
      this.lines = 0;
      this.level = 1;
      this.fall = 0.0F;
      this.shake = 0.0F;
      this.levelT = -1.0F;
      this.bannerT = -1.0F;

      while (this.queue.size() < 4) {
         this.queue.add(this.nextFromBag());
      }

      this.spawn(this.queue.remove(0));
   }

   private int nextFromBag() {
      if (this.bag.isEmpty()) {
         for (int var1 = 0; var1 < 7; var1++) {
            this.bag.add(this.rnd.nextInt(this.bag.size() + 1), var1);
         }
      }

      return this.bag.remove(0);
   }

   private void spawn(int var1) {
      this.type = var1;
      this.rot = 0;
      int var2 = SHAPES[var1].length;
      this.px = (10 - var2) / 2;
      this.py = var1 == 0 ? -1 : 0;
      this.active = true;
      this.canHold = true;
      this.fall = 0.0F;
      this.lockT = 0.0F;
      this.lockResets = 0;
      this.visX = this.px;
      this.visY = this.py - 1.5F;
      if (!this.fits(this.type, this.rot, this.px, this.py)) {
         this.active = false;
         this.gameOver();
      }
   }

   private static int[][] cells(int var0, int var1) {
      String[] var2 = SHAPES[var0];
      int var3 = var2.length;
      int[][] var4 = new int[4][];
      int var5 = 0;

      for (int var6 = 0; var6 < var3; var6++) {
         for (int var7 = 0; var7 < var3; var7++) {
            if (var2[var6].charAt(var7) == '#') {
               int var8 = var7;
               int var9 = var6;

               for (int var10 = 0; var10 < (var1 & 3); var10++) {
                  int var11 = var3 - 1 - var9;
                  var9 = var8;
                  var8 = var11;
               }

               var4[var5++] = new int[]{var8, var9};
            }
         }
      }

      return var4;
   }

   private boolean fits(int var1, int var2, int var3, int var4) {
      for (int[] var8 : cells(var1, var2)) {
         int var9 = var3 + var8[0];
         int var10 = var4 + var8[1];
         if (var9 < 0 || var9 >= 10 || var10 >= 20) {
            return false;
         }

         if (var10 >= 0 && this.grid[var10][var9] != 0) {
            return false;
         }
      }

      return true;
   }

   private int ghostY() {
      int var1 = this.py;

      while (this.fits(this.type, this.rot, this.px, var1 + 1)) {
         var1++;
      }

      return var1;
   }

   private void touched() {
      if (!this.fits(this.type, this.rot, this.px, this.py + 1) && this.lockResets < 15) {
         this.lockT = 0.0F;
         this.lockResets++;
      }
   }

   @Override
   protected void onKey(int var1) {
      if (this.active && !(this.clearT >= 0.0F)) {
         if (var1 == 263 || var1 == 65) {
            if (this.fits(this.type, this.rot, this.px - 1, this.py)) {
               this.px--;
               this.touched();
               Sound.play("minecraft:block.wood.hit", 1.8F, 0.08F);
            }
         } else if (var1 == 262 || var1 == 68) {
            if (this.fits(this.type, this.rot, this.px + 1, this.py)) {
               this.px++;
               this.touched();
               Sound.play("minecraft:block.wood.hit", 1.8F, 0.08F);
            }
         } else if (var1 == 265 || var1 == 87 || var1 == 88) {
            this.rotate(1);
         } else if (var1 == 90) {
            this.rotate(3);
         } else if (var1 != 264 && var1 != 83) {
            if (var1 == 32) {
               if (this.time < 0.15F) {
                  return;
               }

               int var2 = this.ghostY();
               this.score = this.score + 2L * (var2 - this.py);
               this.py = var2;
               this.visY = this.py;
               this.shake = 0.6F;
               this.lock();
            } else if (var1 == 67) {
               if (!this.canHold) {
                  return;
               }

               int var4 = this.type;
               if (this.hold < 0) {
                  this.hold = var4;
                  this.spawn(this.queue.remove(0));
                  this.queue.add(this.nextFromBag());
               } else {
                  int var3 = this.hold;
                  this.hold = var4;
                  this.spawn(var3);
               }

               this.canHold = false;
               this.holdPop = 1.0F;
               Sound.play("minecraft:item.armor.equip_leather", 1.3F, 0.3F);
            }
         } else if (this.fits(this.type, this.rot, this.px, this.py + 1)) {
            this.py++;
            this.score++;
            this.fall = 0.0F;
         }
      }
   }

   private void rotate(int var1) {
      if (this.type != 1) {
         int var2 = this.rot + var1 & 3;

         for (int[] var6 : KICKS) {
            int var7 = var6[0];
            int var8 = var6[1];
            if (this.fits(this.type, var2, this.px + var7, this.py + var8)) {
               this.rot = var2;
               this.px += var7;
               this.py += var8;
               this.touched();
               Sound.play("minecraft:block.wood.place", 1.6F, 0.12F);
               return;
            }
         }
      }
   }

   private float gravity() {
      return Math.max(0.045F, 0.8F * (float)Math.pow(0.82, this.level - 1));
   }

   @Override
   protected void update(float var1) {
      for (float[] var3 : this.parts) {
         var3[0] += var3[2] * var1;
         var3[1] += var3[3] * var1;
         var3[3] += 260.0F * var1;
         var3[4] += var1;
      }

      this.parts.removeIf(var0 -> var0[4] > 0.9F);
      if (this.levelT >= 0.0F) {
         this.levelT += var1;
         if (this.levelT > 1.6F) {
            this.levelT = -1.0F;
         }
      }

      if (this.bannerT >= 0.0F) {
         this.bannerT += var1;
         if (this.bannerT > 1.3F) {
            this.bannerT = -1.0F;
         }
      }

      if (this.clearT >= 0.0F) {
         this.clearT += var1;
         if (this.clearT >= 0.34F) {
            this.finishClear();
         }
      } else if (this.active) {
         if (this.fits(this.type, this.rot, this.px, this.py + 1)) {
            this.fall += var1;
            this.lockT = 0.0F;
            if (this.fall >= this.gravity()) {
               this.fall = this.fall - this.gravity();
               this.py++;
            }
         } else {
            this.lockT += var1;
            if (this.lockT >= 0.5F) {
               this.lock();
            }
         }
      }
   }

   private void lock() {
      boolean var1 = false;

      for (int[] var5 : cells(this.type, this.rot)) {
         int var6 = this.px + var5[0];
         int var7 = this.py + var5[1];
         if (var7 < 0) {
            var1 = true;
         } else {
            this.grid[var7][var6] = this.type + 1;
         }
      }

      this.active = false;
      Sound.play("minecraft:block.stone.place", 1.2F, 0.25F);
      if (var1) {
         this.gameOver();
      } else {
         this.clearing.clear();

         for (int var8 = 0; var8 < 20; var8++) {
            boolean var10 = true;

            for (int var12 = 0; var12 < 10; var12++) {
               if (this.grid[var8][var12] == 0) {
                  var10 = false;
                  break;
               }
            }

            if (var10) {
               this.clearing.add(var8);
            }
         }

         if (this.clearing.isEmpty()) {
            this.next();
         } else {
            this.clearT = 0.0F;
            int var9 = this.clearing.size();
            Sound.play(
               var9 >= 4 ? "minecraft:ui.toast.challenge_complete" : "minecraft:entity.experience_orb.pickup", var9 >= 4 ? 1.3F : 0.9F + var9 * 0.15F, 0.3F
            );

            for (int var13 : this.clearing) {
               for (int var14 = 0; var14 < 10; var14++) {
                  for (int var15 = 0; var15 < 2; var15++) {
                     this.parts
                        .add(
                           new float[]{
                              130.0F + var14 * 12.0F + 6.0F,
                              14.0F + var13 * 12.0F + 6.0F,
                              (this.rnd.nextFloat() - 0.5F) * 140.0F,
                              -40.0F - this.rnd.nextFloat() * 110.0F,
                              0.0F,
                              COLORS[this.grid[var13][var14] - 1]
                           }
                        );
                  }
               }
            }
         }
      }
   }

   private void finishClear() {
      int var1 = this.clearing.size();

      for (int var3 : this.clearing) {
         for (int var4 = var3; var4 > 0; var4--) {
            this.grid[var4] = this.grid[var4 - 1];
         }

         this.grid[0] = new int[10];
      }

      this.clearing.clear();
      this.clearT = -1.0F;
      this.score = this.score + (long)new int[]{0, 100, 300, 500, 800}[Math.min(4, var1)] * this.level;
      this.banner = var1 == 1 ? null : (var1 == 2 ? "Doppel!" : (var1 == 3 ? "Dreifach!" : "TETRIS!"));
      if (this.banner != null) {
         this.bannerT = 0.0F;
      }

      int var5 = this.level;
      this.lines += var1;
      this.level = 1 + this.lines / 10;
      if (this.level > var5) {
         this.levelT = 0.0F;
         Sound.play("minecraft:entity.player.levelup", 1.2F, 0.3F);
      }

      this.next();
   }

   private void next() {
      this.spawn(this.queue.remove(0));
      this.queue.add(this.nextFromBag());
   }

   private void cell(float var1, float var2, float var3, int var4) {
      this.brick(var1 + 0.3F, var2 + 0.3F, var3 - 0.6F, var3 - 0.6F, var4);
   }

   @Override
   protected void render() {
      float var1 = Math.min(0.05F, Gx.dt);
      float var2 = 1.0F - (float)Math.exp(-var1 * 28.0F);
      this.visX = this.visX + (this.px - this.visX) * var2;
      this.visY = this.visY + (this.py - this.visY) * var2;
      if (Math.abs(this.px - this.visX) > 3.0F) {
         this.visX = this.px;
      }

      this.shake = Math.max(0.0F, this.shake - var1 * 3.0F);
      this.holdPop = Math.max(0.0F, this.holdPop - var1 * 4.0F);
      float var3 = this.shake > 0.0F ? Ease.outCubic(this.shake) * 2.2F : 0.0F;
      Gx.shadow(this.X(126.0F), this.Y(10.0F), this.S(128.0F), this.S(248.0F), this.S(8.0F), this.S(6.0F), 0.4F);
      this.boxV(126.0F, 10.0F, 128.0F, 248.0F, 8.0F, Style.light ? -2630944 : -15197406, Style.light ? -3288616 : -15592423);
      Gx.push();
      Gx.translate(0.0F, this.S(var3));

      for (int var4 = 0; var4 < 20; var4++) {
         for (int var5 = 0; var5 < 10; var5++) {
            this.circle(130.0F + var5 * 12.0F + 6.0F, 14.0F + var4 * 12.0F + 6.0F, 2.6F, Style.light ? 301989888 : 184549375);
         }
      }

      for (int var12 = 0; var12 < 20; var12++) {
         boolean var17 = this.clearT >= 0.0F && this.clearing.contains(var12);

         for (int var6 = 0; var6 < 10; var6++) {
            int var7 = this.grid[var12][var6];
            if (var7 != 0) {
               if (var17) {
                  float var8 = this.clearT / 0.34F;
                  float var9 = 1.0F - Ease.inOutCubic(Math.max(0.0F, (var8 - 0.35F) / 0.65F));
                  int var10 = Gx.mix(COLORS[var7 - 1], -1, Math.min(1.0F, var8 * 3.0F));
                  float var11 = 12.0F * var9;
                  if (var11 > 0.5F) {
                     this.cell(130.0F + var6 * 12.0F + (12.0F - var11) / 2.0F, 14.0F + var12 * 12.0F + (12.0F - var11) / 2.0F, var11, var10);
                  }
               } else {
                  this.cell(130.0F + var6 * 12.0F, 14.0F + var12 * 12.0F, 12.0F, COLORS[var7 - 1]);
               }
            }
         }

         if (var17) {
            float var23 = 1.0F - this.clearT / 0.34F;
            Gx.glow(this.X(190.0F), this.Y(14.0F + var12 * 12.0F + 6.0F), this.S(70.0F), Gx.withAlpha(-1, var23 * 0.35F));
         }
      }

      if (this.active && this.state != GameView.State.OVER) {
         int var13 = this.ghostY();

         for (int[] var36 : cells(this.type, this.rot)) {
            float var43 = 130.0F + (this.px + var36[0]) * 12.0F;
            float var49 = 14.0F + (var13 + var36[1]) * 12.0F;
            if (var13 + var36[1] >= 0) {
               this.box(var43 + 0.8F, var49 + 0.8F, 10.4F, 10.4F, 2.5F, Gx.withAlpha(COLORS[this.type], 0.13F));
               Gx.outline(
                  this.X(var43 + 0.8F),
                  this.Y(var49 + 0.8F),
                  this.S(10.4F),
                  this.S(10.4F),
                  this.S(2.5F),
                  Math.max(1, this.S(0.7F)),
                  Gx.withAlpha(COLORS[this.type], 0.55F)
               );
            }
         }

         Gx.clip(this.X(126.0F), this.Y(14.0F), this.S(128.0F), this.S(244.0F));

         for (int[] var37 : cells(this.type, this.rot)) {
            float var44 = 130.0F + (this.visX + var37[0]) * 12.0F;
            float var50 = 14.0F + (this.visY + var37[1]) * 12.0F;
            Gx.glow(this.X(var44 + 6.0F), this.Y(var50 + 6.0F), this.S(13.200001F), Gx.withAlpha(COLORS[this.type], 0.16F));
         }

         for (int[] var38 : cells(this.type, this.rot)) {
            this.cell(130.0F + (this.visX + var38[0]) * 12.0F, 14.0F + (this.visY + var38[1]) * 12.0F, 12.0F, COLORS[this.type]);
         }

         Gx.unclip();
      }

      Gx.pop();

      for (float[] var21 : this.parts) {
         float var27 = 1.0F - var21[4] / 0.9F;
         float var32 = 2.6F * (1.0F - var21[4] / 0.9F * 0.5F);
         this.box(var21[0] - var32 / 2.0F, var21[1] - var32 / 2.0F, var32, var32, 0.8F, Gx.withAlpha((int)var21[5], var27));
      }

      this.panelBox(12.0F, 10.0F, 106.0F, 66.0F, "HALTEN");
      if (this.hold >= 0) {
         float var15 = 1.0F + 0.15F * this.holdPop;
         Gx.push();
         Gx.scaleAt(this.X(65.0F), this.Y(50.0F), var15);
         Gx.pushAlpha(this.canHold ? 1.0F : 0.4F);
         this.mini(this.hold, 65.0F, 50.0F, 10.0F);
         Gx.popAlpha();
         Gx.pop();
      } else {
         this.text("C drücken", 65.0F, 50.0F, 6.5F, 2, Style.muted);
      }

      this.stat(12.0F, 84.0F, "LEVEL", String.valueOf(this.level), Style.accent);
      this.stat(12.0F, 134.0F, "REIHEN", String.valueOf(this.lines), -13193537);
      float var16 = this.lines % 10 / 10.0F;
      this.box(22.0F, 174.0F, 86.0F, 3.0F, 1.5F, Style.light ? 402653184 : 352321535);
      if (var16 > 0.0F) {
         this.box(22.0F, 174.0F, 86.0F * var16, 3.0F, 1.5F, Style.accent);
      }

      float var22 = 198.0F;
      String[][] var28 = new String[][]{
         {"chevron-left", "chevron-right", "bewegen"},
         {"chevron-up", null, "drehen"},
         {"chevron-down", null, "schneller"},
         {"#Leer", null, "fallen lassen"},
         {"#C", null, "halten"}
      };

      for (String[] var51 : var28) {
         float var54 = 16.0F;
         var54 += this.keycap(var54, var22, var51[0]) + 2.0F;
         if (var51[1] != null) {
            var54 += this.keycap(var54, var22, var51[1]) + 2.0F;
         }

         Gx.textMid(var51[2], this.X(54.0F), this.Y(var22), 6.4F * this.u, 1, Style.sub);
         var22 += 13.0F;
      }

      this.panelBox(262.0F, 10.0F, 106.0F, 170.0F, "NÄCHSTE");
      this.mini(this.queue.get(0), 315.0F, 52.0F, 11.0F);
      this.box(276.0F, 78.0F, 78.0F, 0.8F, 0.0F, Style.light ? 335544320 : 285212671);

      for (int var34 = 1; var34 < 3; var34++) {
         Gx.pushAlpha(0.75F - (var34 - 1) * 0.2F);
         this.mini(this.queue.get(var34), 315.0F, 56.0F + var34 * 40, 8.0F);
         Gx.popAlpha();
      }

      this.panelBox(262.0F, 188.0F, 106.0F, 62.0F, "TEMPO");
      int var35 = Math.min(10, this.level);

      for (int var40 = 0; var40 < 10; var40++) {
         float var46 = 4.0F + var40 * 1.8F;
         this.box(
            276.0F + var40 * 8.0F,
            240.0F - var46,
            5.0F,
            var46,
            1.5F,
            var40 < var35 ? Gx.mix(-11821238, -3597815, var40 / 9.0F) : (Style.light ? 369098752 : 318767103)
         );
      }

      if (this.bannerT >= 0.0F && this.banner != null) {
         float var41 = this.bannerT / 1.3F;
         float var47 = Ease.outBack(Math.min(1.0F, this.bannerT / 0.3F));
         float var52 = var41 < 0.75F ? 1.0F : 1.0F - (var41 - 0.75F) / 0.25F;
         float var57 = 110.0F - this.bannerT * 8.0F;
         Gx.pushAlpha(var52);
         Gx.push();
         Gx.scaleAt(this.X(190.0F), this.Y(var57), var47);
         Gx.glow(this.X(190.0F), this.Y(var57), this.S(60.0F), Gx.withAlpha(Style.accent, 0.35F));
         this.text(this.banner, 190.6F, var57 + 1.0F, 16.0F, 3, -2013265920);
         this.text(this.banner, 190.0F, var57, 16.0F, 3, -1);
         Gx.pop();
         Gx.popAlpha();
      }

      if (this.levelT >= 0.0F) {
         float var42 = this.levelT / 1.6F;
         float var48 = Ease.outBack(Math.min(1.0F, this.levelT / 0.35F));
         float var53 = var42 < 0.7F ? 1.0F : 1.0F - (var42 - 0.7F) / 0.3F;
         float var58 = 146.0F;
         Gx.pushAlpha(var53);
         Gx.push();
         Gx.scaleAt(this.X(190.0F), this.Y(var58), var48);
         this.box(142.0F, var58 - 15.0F, 96.0F, 30.0F, 10.0F, Gx.withAlpha(Style.accent, 0.92F));
         this.text("Level " + this.level, 190.0F, var58, 13.0F, 3, Style.accentText);
         Gx.pop();
         Gx.popAlpha();
      }
   }

   private float keycap(float var1, float var2, String var3) {
      boolean var4 = var3.startsWith("#");
      float var5 = var4 ? Math.max(11.0F, Gx.width(var3.substring(1), 5.6F * this.u, 3) / this.u + 7.0F) : 11.0F;
      float var6 = 10.5F;
      this.box(var1, var2 - var6 / 2.0F, var5, var6, 3.0F, Style.light ? 402653184 : 419430399);
      this.box(var1, var2 + var6 / 2.0F - 1.2F, var5, 1.2F, 0.6F, Style.light ? 335544320 : 285212671);
      if (var4) {
         this.text(var3.substring(1), var1 + var5 / 2.0F, var2 - 0.3F, 5.6F, 3, Style.text);
      } else {
         Gx.icon(var3, this.X(var1 + var5 / 2.0F), this.Y(var2 - 0.3F), 7.0F * this.u, Style.text);
      }

      return var5;
   }

   private void panelBox(float var1, float var2, float var3, float var4, String var5) {
      this.box(var1, var2, var3, var4, 10.0F, Style.light ? 201326592 : 184549375);
      this.text(var5, var1 + var3 / 2.0F, var2 + 11.0F, 6.2F, 3, Style.muted);
   }

   private void stat(float var1, float var2, String var3, String var4, int var5) {
      this.box(var1, var2, 106.0F, 44.0F, 10.0F, Style.light ? 201326592 : 184549375);
      this.box(var1, var2 + 10.0F, 3.0F, 24.0F, 1.5F, var5);
      this.text(var3, var1 + 53.0F, var2 + 12.0F, 6.2F, 3, Style.muted);
      this.text(var4, var1 + 53.0F, var2 + 29.0F, 14.0F, 3, Style.text);
   }

   private void mini(int var1, float var2, float var3, float var4) {
      int[][] var5 = cells(var1, 0);
      int var6 = 9;
      int var7 = -9;
      int var8 = 9;
      int var9 = -9;

      for (int[] var13 : var5) {
         var6 = Math.min(var6, var13[0]);
         var7 = Math.max(var7, var13[0]);
         var8 = Math.min(var8, var13[1]);
         var9 = Math.max(var9, var13[1]);
      }

      float var16 = (var7 - var6 + 1) * var4;
      float var17 = (var9 - var8 + 1) * var4;

      for (int[] var15 : var5) {
         this.cell(var2 - var16 / 2.0F + (var15[0] - var6) * var4, var3 - var17 / 2.0F + (var15[1] - var8) * var4, var4, COLORS[var1]);
      }
   }
}
