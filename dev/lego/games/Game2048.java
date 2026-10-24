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
      List<int[]> free = new ArrayList<>();

      for (int r = 0; r < 4; r++) {
         for (int c = 0; c < 4; c++) {
            if (this.grid[r][c] == null) {
               free.add(new int[]{r, c});
            }
         }
      }

      if (!free.isEmpty()) {
         int[] p = free.get(this.rnd.nextInt(free.size()));
         Game2048.Tile t = new Game2048.Tile(this.rnd.nextFloat() < 0.9F ? 2 : 4, p[0], p[1]);
         t.pop = -0.6F;
         this.grid[p[0]][p[1]] = t;
         this.best = Math.max(this.best, t.v);
      }
   }

   @Override
   protected void onKey(int k) {
      if (!(this.overDelay >= 0.0F)) {
         int dr = 0;
         int dc = 0;
         if (k == 263 || k == 65) {
            dc = -1;
         } else if (k == 262 || k == 68) {
            dc = 1;
         } else if (k != 265 && k != 87) {
            if (k != 264 && k != 83) {
               return;
            }

            dr = 1;
         } else {
            dr = -1;
         }

         this.move(dr, dc);
      }
   }

   private void finishAnims() {
      this.ghosts.clear();

      for (Game2048.Tile[] row : this.grid) {
         for (Game2048.Tile t : row) {
            if (t != null) {
               t.move = 1.0F;
               t.pop = Math.max(t.pop, 1.0F);
            }
         }
      }
   }

   private void move(int dr, int dc) {
      this.finishAnims();
      boolean moved = false;
      int gained = 0;

      for (Game2048.Tile[] row : this.grid) {
         for (Game2048.Tile t : row) {
            if (t != null) {
               t.fr = t.r;
               t.fc = t.c;
               t.merged = false;
            }
         }
      }

      for (int line = 0; line < 4; line++) {
         int target = 0;
         Game2048.Tile last = null;

         for (int i = 0; i < 4; i++) {
            int idx = dr <= 0 && dc <= 0 ? i : 3 - i;
            int r = dr != 0 ? idx : line;
            int c = dc != 0 ? idx : line;
            Game2048.Tile tx = this.grid[r][c];
            if (tx != null) {
               this.grid[r][c] = null;
               if (last != null && last.v == tx.v && !last.merged) {
                  last.v *= 2;
                  last.merged = true;
                  last.pop = -0.2F;
                  gained += last.v;
                  this.best = Math.max(this.best, last.v);
                  tx.r = last.r;
                  tx.c = last.c;
                  tx.dying = true;
                  tx.move = 0.0F;
                  this.ghosts.add(tx);
                  moved = true;
               } else {
                  int tIdx = dr <= 0 && dc <= 0 ? target : 3 - target;
                  int nr = dr != 0 ? tIdx : line;
                  int nc = dc != 0 ? tIdx : line;
                  if (nr != r || nc != c) {
                     moved = true;
                  }

                  tx.r = nr;
                  tx.c = nc;
                  tx.move = 0.0F;
                  this.grid[nr][nc] = tx;
                  last = tx;
                  target++;
               }
            }
         }
      }

      if (moved) {
         this.moves++;
         this.score += gained;
         if (gained > 0) {
            this.floats.add(new float[]{gained, 0.0F});
            Sound.play("minecraft:block.note_block.pling", 0.9F + Math.min(1.0F, (float)(Math.log(gained) / Math.log(2.0)) / 11.0F), 0.25F);
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

         for (Game2048.Tile[] row : this.grid) {
            for (Game2048.Tile tx : row) {
               if (tx != null) {
                  tx.move = 1.0F;
               }
            }
         }
      }
   }

   private boolean canMove() {
      for (int r = 0; r < 4; r++) {
         for (int c = 0; c < 4; c++) {
            Game2048.Tile t = this.grid[r][c];
            if (t == null) {
               return true;
            }

            if (c + 1 < 4 && this.grid[r][c + 1] != null && this.grid[r][c + 1].v == t.v) {
               return true;
            }

            if (r + 1 < 4 && this.grid[r + 1][c] != null && this.grid[r + 1][c].v == t.v) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   protected void update(float dt) {
      if (this.winT >= 0.0F) {
         this.winT += dt;
      }

      if (this.winT > 3.2F) {
         this.winT = -1.0F;
      }

      for (float[] f : this.floats) {
         f[1] += dt;
      }

      this.floats.removeIf(fx -> fx[1] > 0.9F);
      if (this.overDelay >= 0.0F) {
         this.overDelay += dt;
         if (this.overDelay > 0.7F) {
            this.gameOver();
         }
      }
   }

   private static int tileColor(int v) {
      switch (v) {
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

   private float cellX(float c) {
      return 20.0F + c * 62.0F;
   }

   private float cellY(float r) {
      return 20.0F + r * 62.0F;
   }

   private void drawTile(Game2048.Tile t) {
      float mt = Ease.outCubic(t.move);
      float r = t.fr + (t.r - t.fr) * mt;
      float c = t.fc + (t.c - t.fc) * mt;
      float x = this.cellX(c);
      float y = this.cellY(r);
      float sc = 1.0F;
      if (t.pop < 1.0F) {
         float p = Math.max(0.0F, t.pop);
         sc = t.merged ? 1.0F + 0.18F * (float)Math.sin(p * Math.PI) : Ease.outBack(p);
         if (!t.merged && t.pop <= 0.0F) {
            return;
         }
      }

      int col = tileColor(t.v);
      boolean dark = Style.brightness(col) > 0.6;
      int txt = dark ? -12963034 : -1;
      Gx.push();
      Gx.scaleAt(this.X(x + 27.0F), this.Y(y + 27.0F), sc);
      if (t.v >= 128) {
         Gx.glow(this.X(x + 27.0F), this.Y(y + 27.0F), this.S(54.0F * (t.v >= 2048 ? 1.1F : 0.8F)), Gx.withAlpha(col, t.v >= 2048 ? 0.55F : 0.25F));
      }

      Gx.shadow(this.X(x), this.Y(y + 1.5F), this.S(54.0F), this.S(54.0F), this.S(8.0F), this.S(3.0F), 0.35F);
      this.boxV(x, y, 54.0F, 54.0F, 8.0F, Gx.mix(col, -1, 0.16F), Gx.mix(col, -16777216, 0.14F));
      Gx.outline(this.X(x), this.Y(y), this.S(54.0F), this.S(54.0F), this.S(8.0F), Math.max(1, this.S(0.9F)), Gx.withAlpha(Gx.mix(col, -16777216, 0.5F), 0.6F));
      float sr = 4.2F;
      float so = 10.5F;

      for (int i = 0; i < 4; i++) {
         float sx = i % 2 == 0 ? x + so : x + 54.0F - so;
         float sy = i < 2 ? y + so : y + 54.0F - so;
         this.circle(sx, sy + 0.8F, sr, Gx.mix(col, -16777216, 0.2F));
         this.circle(sx, sy, sr * 0.88F, Gx.mix(col, -1, 0.2F));
      }

      String s = String.valueOf(t.v);
      float size = s.length() <= 2 ? 22.0F : (s.length() == 3 ? 19.0F : (s.length() == 4 ? 15.5F : 12.5F));
      this.text(s, x + 27.0F + 0.4F, y + 27.0F + 1.2F, size, 3, Gx.withAlpha(-16777216, dark ? 0.08F : 0.25F));
      this.text(s, x + 27.0F, y + 27.0F, size, 3, txt);
      Gx.pop();
   }

   @Override
   protected void render() {
      float dt = Math.min(0.05F, Gx.dt);

      for (Game2048.Tile g : this.ghosts) {
         g.move = Math.min(1.0F, g.move + dt / 0.12F);
      }

      this.ghosts.removeIf(g -> g.move >= 1.0F);

      for (Game2048.Tile[] row : this.grid) {
         for (Game2048.Tile t : row) {
            if (t != null) {
               t.move = Math.min(1.0F, t.move + dt / 0.12F);
               if (t.pop < 1.0F) {
                  t.pop = Math.min(1.0F, t.pop + dt / 0.16F);
               }
            }
         }
      }

      this.shake = Math.max(0.0F, this.shake - dt * 4.0F);
      float sx = this.shake > 0.0F ? (float)Math.sin(this.shake * 30.0F) * 2.5F * this.shake : 0.0F;
      Gx.push();
      Gx.translate(this.S(sx), 0.0F);
      Gx.shadow(this.X(12.0F), this.Y(12.0F), this.S(256.0F), this.S(256.0F), this.S(12.0F), this.S(6.0F), 0.4F);
      this.boxV(12.0F, 12.0F, 256.0F, 256.0F, 12.0F, Style.light ? -2894115 : -15065819, Style.light ? -3617580 : -15460836);

      for (int r = 0; r < 4; r++) {
         for (int c = 0; c < 4; c++) {
            float x = this.cellX(c);
            float y = this.cellY(r);
            this.box(x, y, 54.0F, 54.0F, 8.0F, Style.light ? 369098752 : 218103807);

            for (int i = 0; i < 4; i++) {
               float px = i % 2 == 0 ? x + 10.5F : x + 54.0F - 10.5F;
               float py = i < 2 ? y + 10.5F : y + 54.0F - 10.5F;
               this.circle(px, py, 3.4F, Style.light ? 268435456 : 150994943);
            }
         }
      }

      for (Game2048.Tile g : this.ghosts) {
         this.drawTile(g);
      }

      for (Game2048.Tile[] row : this.grid) {
         for (Game2048.Tile tx : row) {
            if (tx != null && !tx.merged) {
               this.drawTile(tx);
            }
         }
      }

      for (Game2048.Tile[] row : this.grid) {
         for (Game2048.Tile txx : row) {
            if (txx != null && txx.merged) {
               this.drawTile(txx);
            }
         }
      }

      Gx.pop();
      float px = 280.0F;
      float pw = this.boardW() - px - 12.0F;
      this.panel(px, 12.0F, pw, 58.0F, "HÖCHSTER STEIN", String.valueOf(this.best), tileColor(this.best));
      this.panel(px, 78.0F, pw, 58.0F, "ZÜGE", String.valueOf(this.moves), Style.accent);

      for (float[] f : this.floats) {
         float a = 1.0F - Ease.outCubic(f[1] / 0.9F);
         this.text("+" + (int)f[0], px + pw / 2.0F, 162.0F - f[1] * 26.0F, 12.0F, 3, Gx.withAlpha(Style.accent, a));
      }

      float gy = 192.0F;
      this.box(px, gy, pw, 76.0F, 10.0F, Style.light ? 201326592 : 184549375);
      this.text("ZIEL", px + pw / 2.0F, gy + 14.0F, 6.5F, 3, Style.muted);
      float bs = 34.0F;
      float bxp = px + (pw - bs) / 2.0F;
      float byp = gy + 24.0F;
      this.boxV(bxp, byp, bs, bs, 6.0F, Gx.mix(-15043, -1, 0.16F), Gx.mix(-15043, -16777216, 0.14F));
      this.text("2048", bxp + bs / 2.0F, byp + bs / 2.0F, 8.5F, 3, -12963034);
      if (this.won) {
         this.text("geschafft!", px + pw / 2.0F, gy + 67.0F, 6.5F, 3, -15043);
      }

      if (this.winT >= 0.0F) {
         float a = this.winT < 2.6F ? 1.0F : 1.0F - (this.winT - 2.6F) / 0.6F;
         float s = Ease.outBack(Math.min(1.0F, this.winT / 0.45F));
         float cx = 140.0F;
         float cy = 140.0F;
         Gx.pushAlpha(a);
         this.box(12.0F, 12.0F, 256.0F, 256.0F, 12.0F, Gx.rgba(0, 0.6F));
         Gx.glow(this.X(cx), this.Y(cy), this.S(110.0F), 1728038205);
         Gx.push();
         Gx.scaleAt(this.X(cx), this.Y(cy), s);
         this.text("2048!", cx + 1.0F, cy - 4.0F, 40.0F, 3, 1711276032);
         this.text("2048!", cx, cy - 6.0F, 40.0F, 3, -15043);
         this.text("Weiter geht's!", cx, cy + 26.0F, 10.0F, 2, -1);
         Gx.pop();

         for (int i = 0; i < 18; i++) {
            double ang = i / 18.0 * Math.PI * 2.0 + this.winT;
            float d = 40.0F + this.winT * 60.0F;
            this.circle(cx + (float)Math.cos(ang) * d, cy + (float)Math.sin(ang) * d, 2.5F, Gx.withAlpha(tileColor(2 << i % 10), 1.0F - this.winT / 3.2F));
         }

         Gx.popAlpha();
      }
   }

   private void panel(float x, float y, float w, float h, String label, String value, int col) {
      this.box(x, y, w, h, 10.0F, Style.light ? 201326592 : 184549375);
      this.box(x, y + 12.0F, 3.0F, h - 24.0F, 1.5F, col);
      this.text(label, x + w / 2.0F, y + 16.0F, 6.5F, 3, Style.muted);
      this.text(value, x + w / 2.0F, y + 38.0F, 16.0F, 3, Style.text);
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

      Tile(int v, int r, int c) {
         this.v = v;
         this.r = r;
         this.c = c;
         this.fr = r;
         this.fc = c;
      }
   }
}
