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
         for (int i = 0; i < 7; i++) {
            this.bag.add(this.rnd.nextInt(this.bag.size() + 1), i);
         }
      }

      return this.bag.remove(0);
   }

   private void spawn(int t) {
      this.type = t;
      this.rot = 0;
      int n = SHAPES[t].length;
      this.px = (10 - n) / 2;
      this.py = t == 0 ? -1 : 0;
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

   private static int[][] cells(int t, int r) {
      String[] s = SHAPES[t];
      int n = s.length;
      int[][] out = new int[4][];
      int k = 0;

      for (int y = 0; y < n; y++) {
         for (int x = 0; x < n; x++) {
            if (s[y].charAt(x) == '#') {
               int cx = x;
               int cy = y;

               for (int i = 0; i < (r & 3); i++) {
                  int tx = n - 1 - cy;
                  cy = cx;
                  cx = tx;
               }

               out[k++] = new int[]{cx, cy};
            }
         }
      }

      return out;
   }

   private boolean fits(int t, int r, int x, int y) {
      for (int[] c : cells(t, r)) {
         int gx = x + c[0];
         int gy = y + c[1];
         if (gx < 0 || gx >= 10 || gy >= 20) {
            return false;
         }

         if (gy >= 0 && this.grid[gy][gx] != 0) {
            return false;
         }
      }

      return true;
   }

   private int ghostY() {
      int y = this.py;

      while (this.fits(this.type, this.rot, this.px, y + 1)) {
         y++;
      }

      return y;
   }

   private void touched() {
      if (!this.fits(this.type, this.rot, this.px, this.py + 1) && this.lockResets < 15) {
         this.lockT = 0.0F;
         this.lockResets++;
      }
   }

   @Override
   protected void onKey(int k) {
      if (this.active && !(this.clearT >= 0.0F)) {
         if (k == 263 || k == 65) {
            if (this.fits(this.type, this.rot, this.px - 1, this.py)) {
               this.px--;
               this.touched();
               Sound.play("minecraft:block.wood.hit", 1.8F, 0.08F);
            }
         } else if (k == 262 || k == 68) {
            if (this.fits(this.type, this.rot, this.px + 1, this.py)) {
               this.px++;
               this.touched();
               Sound.play("minecraft:block.wood.hit", 1.8F, 0.08F);
            }
         } else if (k == 265 || k == 87 || k == 88) {
            this.rotate(1);
         } else if (k == 90) {
            this.rotate(3);
         } else if (k != 264 && k != 83) {
            if (k == 32) {
               if (this.time < 0.15F) {
                  return;
               }

               int g = this.ghostY();
               this.score = this.score + 2L * (g - this.py);
               this.py = g;
               this.visY = this.py;
               this.shake = 0.6F;
               this.lock();
            } else if (k == 67) {
               if (!this.canHold) {
                  return;
               }

               int cur = this.type;
               if (this.hold < 0) {
                  this.hold = cur;
                  this.spawn(this.queue.remove(0));
                  this.queue.add(this.nextFromBag());
               } else {
                  int h = this.hold;
                  this.hold = cur;
                  this.spawn(h);
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

   private void rotate(int dir) {
      if (this.type != 1) {
         int nr = this.rot + dir & 3;

         for (int[] kk : KICKS) {
            int kx = kk[0];
            int ky = kk[1];
            if (this.fits(this.type, nr, this.px + kx, this.py + ky)) {
               this.rot = nr;
               this.px += kx;
               this.py += ky;
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
   protected void update(float dt) {
      for (float[] p : this.parts) {
         p[0] += p[2] * dt;
         p[1] += p[3] * dt;
         p[3] += 260.0F * dt;
         p[4] += dt;
      }

      this.parts.removeIf(px -> px[4] > 0.9F);
      if (this.levelT >= 0.0F) {
         this.levelT += dt;
         if (this.levelT > 1.6F) {
            this.levelT = -1.0F;
         }
      }

      if (this.bannerT >= 0.0F) {
         this.bannerT += dt;
         if (this.bannerT > 1.3F) {
            this.bannerT = -1.0F;
         }
      }

      if (this.clearT >= 0.0F) {
         this.clearT += dt;
         if (this.clearT >= 0.34F) {
            this.finishClear();
         }
      } else if (this.active) {
         if (this.fits(this.type, this.rot, this.px, this.py + 1)) {
            this.fall += dt;
            this.lockT = 0.0F;
            if (this.fall >= this.gravity()) {
               this.fall = this.fall - this.gravity();
               this.py++;
            }
         } else {
            this.lockT += dt;
            if (this.lockT >= 0.5F) {
               this.lock();
            }
         }
      }
   }

   private void lock() {
      boolean above = false;

      for (int[] c : cells(this.type, this.rot)) {
         int gx = this.px + c[0];
         int gy = this.py + c[1];
         if (gy < 0) {
            above = true;
         } else {
            this.grid[gy][gx] = this.type + 1;
         }
      }

      this.active = false;
      Sound.play("minecraft:block.stone.place", 1.2F, 0.25F);
      if (above) {
         this.gameOver();
      } else {
         this.clearing.clear();

         for (int y = 0; y < 20; y++) {
            boolean full = true;

            for (int x = 0; x < 10; x++) {
               if (this.grid[y][x] == 0) {
                  full = false;
                  break;
               }
            }

            if (full) {
               this.clearing.add(y);
            }
         }

         if (this.clearing.isEmpty()) {
            this.next();
         } else {
            this.clearT = 0.0F;
            int n = this.clearing.size();
            Sound.play(n >= 4 ? "minecraft:ui.toast.challenge_complete" : "minecraft:entity.experience_orb.pickup", n >= 4 ? 1.3F : 0.9F + n * 0.15F, 0.3F);

            for (int y : this.clearing) {
               for (int xx = 0; xx < 10; xx++) {
                  for (int i = 0; i < 2; i++) {
                     this.parts
                        .add(
                           new float[]{
                              130.0F + xx * 12.0F + 6.0F,
                              14.0F + y * 12.0F + 6.0F,
                              (this.rnd.nextFloat() - 0.5F) * 140.0F,
                              -40.0F - this.rnd.nextFloat() * 110.0F,
                              0.0F,
                              COLORS[this.grid[y][xx] - 1]
                           }
                        );
                  }
               }
            }
         }
      }
   }

   private void finishClear() {
      int n = this.clearing.size();

      for (int y : this.clearing) {
         for (int yy = y; yy > 0; yy--) {
            this.grid[yy] = this.grid[yy - 1];
         }

         this.grid[0] = new int[10];
      }

      this.clearing.clear();
      this.clearT = -1.0F;
      this.score = this.score + (long)new int[]{0, 100, 300, 500, 800}[Math.min(4, n)] * this.level;
      this.banner = n == 1 ? null : (n == 2 ? "Doppel!" : (n == 3 ? "Dreifach!" : "TETRIS!"));
      if (this.banner != null) {
         this.bannerT = 0.0F;
      }

      int oldLevel = this.level;
      this.lines += n;
      this.level = 1 + this.lines / 10;
      if (this.level > oldLevel) {
         this.levelT = 0.0F;
         Sound.play("minecraft:entity.player.levelup", 1.2F, 0.3F);
      }

      this.next();
   }

   private void next() {
      this.spawn(this.queue.remove(0));
      this.queue.add(this.nextFromBag());
   }

   private void cell(float x, float y, float s, int col) {
      this.brick(x + 0.3F, y + 0.3F, s - 0.6F, s - 0.6F, col);
   }

   @Override
   protected void render() {
      float dt = Math.min(0.05F, Gx.dt);
      float k = 1.0F - (float)Math.exp(-dt * 28.0F);
      this.visX = this.visX + (this.px - this.visX) * k;
      this.visY = this.visY + (this.py - this.visY) * k;
      if (Math.abs(this.px - this.visX) > 3.0F) {
         this.visX = this.px;
      }

      this.shake = Math.max(0.0F, this.shake - dt * 3.0F);
      this.holdPop = Math.max(0.0F, this.holdPop - dt * 4.0F);
      float sy = this.shake > 0.0F ? Ease.outCubic(this.shake) * 2.2F : 0.0F;
      Gx.shadow(this.X(126.0F), this.Y(10.0F), this.S(128.0F), this.S(248.0F), this.S(8.0F), this.S(6.0F), 0.4F);
      this.boxV(126.0F, 10.0F, 128.0F, 248.0F, 8.0F, Style.light ? -2630944 : -15197406, Style.light ? -3288616 : -15592423);
      Gx.push();
      Gx.translate(0.0F, this.S(sy));

      for (int y = 0; y < 20; y++) {
         for (int x = 0; x < 10; x++) {
            this.circle(130.0F + x * 12.0F + 6.0F, 14.0F + y * 12.0F + 6.0F, 2.6F, Style.light ? 301989888 : 184549375);
         }
      }

      for (int y = 0; y < 20; y++) {
         boolean clr = this.clearT >= 0.0F && this.clearing.contains(y);

         for (int x = 0; x < 10; x++) {
            int v = this.grid[y][x];
            if (v != 0) {
               if (clr) {
                  float t = this.clearT / 0.34F;
                  float sc = 1.0F - Ease.inOutCubic(Math.max(0.0F, (t - 0.35F) / 0.65F));
                  int col = Gx.mix(COLORS[v - 1], -1, Math.min(1.0F, t * 3.0F));
                  float cs = 12.0F * sc;
                  if (cs > 0.5F) {
                     this.cell(130.0F + x * 12.0F + (12.0F - cs) / 2.0F, 14.0F + y * 12.0F + (12.0F - cs) / 2.0F, cs, col);
                  }
               } else {
                  this.cell(130.0F + x * 12.0F, 14.0F + y * 12.0F, 12.0F, COLORS[v - 1]);
               }
            }
         }

         if (clr) {
            float a = 1.0F - this.clearT / 0.34F;
            Gx.glow(this.X(190.0F), this.Y(14.0F + y * 12.0F + 6.0F), this.S(70.0F), Gx.withAlpha(-1, a * 0.35F));
         }
      }

      if (this.active && this.state != GameView.State.OVER) {
         int g = this.ghostY();

         for (int[] c : cells(this.type, this.rot)) {
            float xx = 130.0F + (this.px + c[0]) * 12.0F;
            float y = 14.0F + (g + c[1]) * 12.0F;
            if (g + c[1] >= 0) {
               this.box(xx + 0.8F, y + 0.8F, 10.4F, 10.4F, 2.5F, Gx.withAlpha(COLORS[this.type], 0.13F));
               Gx.outline(
                  this.X(xx + 0.8F),
                  this.Y(y + 0.8F),
                  this.S(10.4F),
                  this.S(10.4F),
                  this.S(2.5F),
                  Math.max(1, this.S(0.7F)),
                  Gx.withAlpha(COLORS[this.type], 0.55F)
               );
            }
         }

         Gx.clip(this.X(126.0F), this.Y(14.0F), this.S(128.0F), this.S(244.0F));

         for (int[] cx : cells(this.type, this.rot)) {
            float xx = 130.0F + (this.visX + cx[0]) * 12.0F;
            float y = 14.0F + (this.visY + cx[1]) * 12.0F;
            Gx.glow(this.X(xx + 6.0F), this.Y(y + 6.0F), this.S(13.200001F), Gx.withAlpha(COLORS[this.type], 0.16F));
         }

         for (int[] cx : cells(this.type, this.rot)) {
            this.cell(130.0F + (this.visX + cx[0]) * 12.0F, 14.0F + (this.visY + cx[1]) * 12.0F, 12.0F, COLORS[this.type]);
         }

         Gx.unclip();
      }

      Gx.pop();

      for (float[] p : this.parts) {
         float a = 1.0F - p[4] / 0.9F;
         float s = 2.6F * (1.0F - p[4] / 0.9F * 0.5F);
         this.box(p[0] - s / 2.0F, p[1] - s / 2.0F, s, s, 0.8F, Gx.withAlpha((int)p[5], a));
      }

      this.panelBox(12.0F, 10.0F, 106.0F, 66.0F, "HALTEN");
      if (this.hold >= 0) {
         float sc = 1.0F + 0.15F * this.holdPop;
         Gx.push();
         Gx.scaleAt(this.X(65.0F), this.Y(50.0F), sc);
         Gx.pushAlpha(this.canHold ? 1.0F : 0.4F);
         this.mini(this.hold, 65.0F, 50.0F, 10.0F);
         Gx.popAlpha();
         Gx.pop();
      } else {
         this.text("C drücken", 65.0F, 50.0F, 6.5F, 2, Style.muted);
      }

      this.stat(12.0F, 84.0F, "LEVEL", String.valueOf(this.level), Style.accent);
      this.stat(12.0F, 134.0F, "REIHEN", String.valueOf(this.lines), -13193537);
      float prog = this.lines % 10 / 10.0F;
      this.box(22.0F, 174.0F, 86.0F, 3.0F, 1.5F, Style.light ? 402653184 : 352321535);
      if (prog > 0.0F) {
         this.box(22.0F, 174.0F, 86.0F * prog, 3.0F, 1.5F, Style.accent);
      }

      float hy = 198.0F;
      String[][] help = new String[][]{
         {"chevron-left", "chevron-right", "bewegen"},
         {"chevron-up", null, "drehen"},
         {"chevron-down", null, "schneller"},
         {"#Leer", null, "fallen lassen"},
         {"#C", null, "halten"}
      };

      for (String[] h : help) {
         float kx = 16.0F;
         kx += this.keycap(kx, hy, h[0]) + 2.0F;
         if (h[1] != null) {
            kx += this.keycap(kx, hy, h[1]) + 2.0F;
         }

         Gx.textMid(h[2], this.X(54.0F), this.Y(hy), 6.4F * this.u, 1, Style.sub);
         hy += 13.0F;
      }

      this.panelBox(262.0F, 10.0F, 106.0F, 170.0F, "NÄCHSTE");
      this.mini(this.queue.get(0), 315.0F, 52.0F, 11.0F);
      this.box(276.0F, 78.0F, 78.0F, 0.8F, 0.0F, Style.light ? 335544320 : 285212671);

      for (int i = 1; i < 3; i++) {
         Gx.pushAlpha(0.75F - (i - 1) * 0.2F);
         this.mini(this.queue.get(i), 315.0F, 56.0F + i * 40, 8.0F);
         Gx.popAlpha();
      }

      this.panelBox(262.0F, 188.0F, 106.0F, 62.0F, "TEMPO");
      int bars = Math.min(10, this.level);

      for (int i = 0; i < 10; i++) {
         float bh = 4.0F + i * 1.8F;
         this.box(276.0F + i * 8.0F, 240.0F - bh, 5.0F, bh, 1.5F, i < bars ? Gx.mix(-11821238, -3597815, i / 9.0F) : (Style.light ? 369098752 : 318767103));
      }

      if (this.bannerT >= 0.0F && this.banner != null) {
         float t = this.bannerT / 1.3F;
         float s = Ease.outBack(Math.min(1.0F, this.bannerT / 0.3F));
         float a = t < 0.75F ? 1.0F : 1.0F - (t - 0.75F) / 0.25F;
         float cy = 110.0F - this.bannerT * 8.0F;
         Gx.pushAlpha(a);
         Gx.push();
         Gx.scaleAt(this.X(190.0F), this.Y(cy), s);
         Gx.glow(this.X(190.0F), this.Y(cy), this.S(60.0F), Gx.withAlpha(Style.accent, 0.35F));
         this.text(this.banner, 190.6F, cy + 1.0F, 16.0F, 3, -2013265920);
         this.text(this.banner, 190.0F, cy, 16.0F, 3, -1);
         Gx.pop();
         Gx.popAlpha();
      }

      if (this.levelT >= 0.0F) {
         float t = this.levelT / 1.6F;
         float s = Ease.outBack(Math.min(1.0F, this.levelT / 0.35F));
         float a = t < 0.7F ? 1.0F : 1.0F - (t - 0.7F) / 0.3F;
         float cy = 146.0F;
         Gx.pushAlpha(a);
         Gx.push();
         Gx.scaleAt(this.X(190.0F), this.Y(cy), s);
         this.box(142.0F, cy - 15.0F, 96.0F, 30.0F, 10.0F, Gx.withAlpha(Style.accent, 0.92F));
         this.text("Level " + this.level, 190.0F, cy, 13.0F, 3, Style.accentText);
         Gx.pop();
         Gx.popAlpha();
      }
   }

   private float keycap(float x, float cy, String what) {
      boolean txt = what.startsWith("#");
      float w = txt ? Math.max(11.0F, Gx.width(what.substring(1), 5.6F * this.u, 3) / this.u + 7.0F) : 11.0F;
      float h = 10.5F;
      this.box(x, cy - h / 2.0F, w, h, 3.0F, Style.light ? 402653184 : 419430399);
      this.box(x, cy + h / 2.0F - 1.2F, w, 1.2F, 0.6F, Style.light ? 335544320 : 285212671);
      if (txt) {
         this.text(what.substring(1), x + w / 2.0F, cy - 0.3F, 5.6F, 3, Style.text);
      } else {
         Gx.icon(what, this.X(x + w / 2.0F), this.Y(cy - 0.3F), 7.0F * this.u, Style.text);
      }

      return w;
   }

   private void panelBox(float x, float y, float w, float h, String title) {
      this.box(x, y, w, h, 10.0F, Style.light ? 201326592 : 184549375);
      this.text(title, x + w / 2.0F, y + 11.0F, 6.2F, 3, Style.muted);
   }

   private void stat(float x, float y, String label, String value, int col) {
      this.box(x, y, 106.0F, 44.0F, 10.0F, Style.light ? 201326592 : 184549375);
      this.box(x, y + 10.0F, 3.0F, 24.0F, 1.5F, col);
      this.text(label, x + 53.0F, y + 12.0F, 6.2F, 3, Style.muted);
      this.text(value, x + 53.0F, y + 29.0F, 14.0F, 3, Style.text);
   }

   private void mini(int t, float cx, float cy, float s) {
      int[][] cs = cells(t, 0);
      int minX = 9;
      int maxX = -9;
      int minY = 9;
      int maxY = -9;

      for (int[] c : cs) {
         minX = Math.min(minX, c[0]);
         maxX = Math.max(maxX, c[0]);
         minY = Math.min(minY, c[1]);
         maxY = Math.max(maxY, c[1]);
      }

      float w = (maxX - minX + 1) * s;
      float h = (maxY - minY + 1) * s;

      for (int[] c : cs) {
         this.cell(cx - w / 2.0F + (c[0] - minX) * s, cy - h / 2.0F + (c[1] - minY) * s, s, COLORS[t]);
      }
   }
}
