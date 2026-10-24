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

   private void newPuzzle(int d) {
      this.diff = d;
      Random r = new Random();
      Arrays.fill(this.sol, 0);
      fill(this.sol, 0, r);
      System.arraycopy(this.sol, 0, this.cell, 0, 81);
      int[] order = new int[81];
      int i = 0;

      while (i < 81) {
         order[i] = i++;
      }

      for (int ix = 80; ix > 0; ix--) {
         int j = r.nextInt(ix + 1);
         int t = order[ix];
         order[ix] = order[j];
         order[j] = t;
      }

      i = 81;
      int[] tmp = new int[81];

      for (int k = 0; k < 81 && i > CLUES[d]; k++) {
         int ix = order[k];
         int keep = this.cell[ix];
         this.cell[ix] = 0;
         System.arraycopy(this.cell, 0, tmp, 0, 81);
         if (count(tmp, 2) != 1) {
            this.cell[ix] = keep;
         } else {
            i--;
         }
      }

      for (int ix = 0; ix < 81; ix++) {
         this.given[ix] = this.cell[ix] != 0;
         this.notes[ix] = 0;
         this.pop[ix] = 0.0F;
         this.wave[ix] = Ease.clamp((ix / 9 + ix % 9) * 0.04F) * -1.0F;
         this.wrongT[ix] = 0.0F;
      }

      this.undo.clear();
      this.mistakes = 0;
      this.hints = 0;
      this.elapsed = 0.0F;
      this.sel = 40;
      Sound.play("minecraft:block.note_block.chime", 1.2F, 0.4F);
   }

   private static boolean fill(int[] g, int pos, Random r) {
      if (pos == 81) {
         return true;
      } else if (g[pos] != 0) {
         return fill(g, pos + 1, r);
      } else {
         int[] d = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};

         for (int i = 8; i > 0; i--) {
            int j = r.nextInt(i + 1);
            int t = d[i];
            d[i] = d[j];
            d[j] = t;
         }

         for (int v : d) {
            if (ok(g, pos, v)) {
               g[pos] = v;
               if (fill(g, pos + 1, r)) {
                  return true;
               }

               g[pos] = 0;
            }
         }

         return false;
      }
   }

   private static boolean ok(int[] g, int pos, int v) {
      int r = pos / 9;
      int c = pos % 9;
      int br = r / 3 * 3;
      int bc = c / 3 * 3;

      for (int k = 0; k < 9; k++) {
         if (g[r * 9 + k] == v || g[k * 9 + c] == v) {
            return false;
         }

         if (g[(br + k / 3) * 9 + bc + k % 3] == v) {
            return false;
         }
      }

      return true;
   }

   private static int cand(int[] g, int pos) {
      int r = pos / 9;
      int c = pos % 9;
      int br = r / 3 * 3;
      int bc = c / 3 * 3;
      int used = 0;

      for (int k = 0; k < 9; k++) {
         used |= 1 << g[r * 9 + k] | 1 << g[k * 9 + c] | 1 << g[(br + k / 3) * 9 + bc + k % 3];
      }

      return ~used & 1022;
   }

   private static int count(int[] g, int limit) {
      int best = -1;
      int bm = 0;
      int bc = 10;

      for (int i = 0; i < 81; i++) {
         if (g[i] == 0) {
            int m = cand(g, i);
            int n = Integer.bitCount(m);
            if (n == 0) {
               return 0;
            }

            if (n < bc) {
               bc = n;
               best = i;
               bm = m;
               if (n == 1) {
                  break;
               }
            }
         }
      }

      if (best < 0) {
         return 1;
      } else {
         int total = 0;

         for (int v = 1; v <= 9; v++) {
            if ((bm & 1 << v) != 0) {
               g[best] = v;
               total += count(g, limit - total);
               g[best] = 0;
               if (total >= limit) {
                  return total;
               }
            }
         }

         return total;
      }
   }

   private void place(int v) {
      if (this.diff >= 0 && !(this.winT >= 0.0F) && !this.given[this.sel]) {
         if (this.noteMode && v != 0) {
            if (this.cell[this.sel] == 0) {
               this.undo.push(new int[]{this.sel, this.cell[this.sel], this.notes[this.sel]});
               this.notes[this.sel] = this.notes[this.sel] ^ 1 << v;
               this.pop[this.sel] = 0.6F;
               Sound.play("minecraft:block.note_block.hat", 1.8F, 0.15F);
            }
         } else if (this.cell[this.sel] != v) {
            this.undo.push(new int[]{this.sel, this.cell[this.sel], this.notes[this.sel]});
            this.cell[this.sel] = v;
            this.notes[this.sel] = 0;
            if (v == 0) {
               Sound.play("minecraft:block.wood.hit", 1.5F, 0.2F);
            } else {
               this.pop[this.sel] = 1.0F;
               if (v != this.sol[this.sel]) {
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
                  int r = this.sel / 9;
                  int c = this.sel % 9;

                  for (int k = 0; k < 81; k++) {
                     if (k / 9 == r || k % 9 == c || k / 27 == this.sel / 27 && k % 9 / 3 == c / 3) {
                        this.notes[k] = this.notes[k] & ~(1 << v);
                     }
                  }

                  Sound.play("minecraft:block.note_block.pling", 1.0F + v * 0.08F, 0.3F);
                  this.checkUnits(this.sel);
                  if (this.solved()) {
                     this.win();
                  }
               }
            }
         }
      }
   }

   private void checkUnits(int p) {
      int r = p / 9;
      int c = p % 9;
      boolean row = true;
      boolean col = true;
      boolean box = true;

      for (int k = 0; k < 9; k++) {
         if (this.cell[r * 9 + k] != this.sol[r * 9 + k]) {
            row = false;
         }

         if (this.cell[k * 9 + c] != this.sol[k * 9 + c]) {
            col = false;
         }

         int bi = (r / 3 * 3 + k / 3) * 9 + c / 3 * 3 + k % 3;
         if (this.cell[bi] != this.sol[bi]) {
            box = false;
         }
      }

      int done = 0;

      for (int k = 0; k < 81; k++) {
         int kr = k / 9;
         int kc = k % 9;
         boolean in = row && kr == r || col && kc == c || box && kr / 3 == r / 3 && kc / 3 == c / 3;
         if (in) {
            this.wave[k] = -(Math.abs(kr - r) + Math.abs(kc - c)) * 0.035F;
            done++;
         }
      }

      if (done > 0) {
         this.fx.burst(this.cx(c), this.cy(r), 12, -12604929, 90.0F, 2.0F, 0.5F, 0.0F, 2);
         Sound.play("minecraft:entity.experience_orb.pickup", 1.3F, 0.3F);
      }

      int v = this.cell[p];
      int cnt = 0;

      for (int kx = 0; kx < 81; kx++) {
         if (this.cell[kx] == v && this.sol[kx] == v) {
            cnt++;
         }
      }

      if (cnt == 9) {
         this.fx.popup("Alle " + v + "er!", this.cx(c), this.cy(r) - 12.0F, -736942, 9.0F);
      }
   }

   private boolean solved() {
      for (int i = 0; i < 81; i++) {
         if (this.cell[i] != this.sol[i]) {
            return false;
         }
      }

      return true;
   }

   private void win() {
      this.winT = 0.0F;
      this.score = this.liveScore();

      for (int k = 0; k < 81; k++) {
         this.wave[k] = -(k / 9 + k % 9) * 0.03F;
      }

      for (int i = 0; i < 6; i++) {
         this.fx.confetti(41.0F + i * 45, 74.0F + i % 2 * 110, 26, new int[]{-1890757, -736942, -14703780, -12604929, -1}, 190.0F, 4.0F);
      }

      this.fx.shake(0.3F);
      Sound.play("minecraft:ui.toast.challenge_complete", 1.2F, 0.4F);
   }

   private void hint() {
      if (this.diff >= 0 && !(this.winT >= 0.0F)) {
         int target = -1;
         if (!this.given[this.sel] && this.cell[this.sel] != this.sol[this.sel]) {
            target = this.sel;
         } else {
            for (int i = 0; i < 81; i++) {
               if (this.cell[i] != this.sol[i]) {
                  target = i;
                  break;
               }
            }
         }

         if (target >= 0) {
            this.hints++;
            this.sel = target;
            boolean nm = this.noteMode;
            this.noteMode = false;
            int m = this.mistakes;
            this.place(this.sol[target]);
            this.mistakes = m;
            this.noteMode = nm;
            this.fx.popup("Tipp -150", this.cx(target % 9), this.cy(target / 9) - 10.0F, -6301441, 9.0F);
         }
      }
   }

   private void doUndo() {
      if (!this.undo.isEmpty() && !(this.winT >= 0.0F)) {
         int[] u0 = this.undo.pop();
         this.cell[u0[0]] = u0[1];
         this.notes[u0[0]] = u0[2];
         this.sel = u0[0];
         this.pop[this.sel] = 0.5F;
         Sound.play("minecraft:block.wood.hit", 1.2F, 0.2F);
      }
   }

   private long liveScore() {
      return this.diff < 0 ? 0L : Math.max(100L, BASE[this.diff] - (long)(this.elapsed * 2.0F) - this.mistakes * 100L - this.hints * 150L);
   }

   @Override
   protected void onKey(int k) {
      if (this.diff < 0) {
         if (k >= 49 && k <= 52) {
            this.newPuzzle(k - 49);
         }
      } else {
         int v = k >= 49 && k <= 57 ? k - 48 : (k >= 321 && k <= 329 ? k - 320 : -1);
         if (v > 0) {
            this.place(v);
         } else {
            if (k == 48 || k == 320 || k == 259 || k == 261) {
               this.place(0);
            } else if (k == 78) {
               this.noteMode = !this.noteMode;
            } else if (k == 90 || k == 85) {
               this.doUndo();
            } else if (k == 72) {
               this.hint();
            } else if (k == 263 || k == 65) {
               this.sel = this.sel / 9 * 9 + (this.sel % 9 + 8) % 9;
            } else if (k == 262 || k == 68) {
               this.sel = this.sel / 9 * 9 + (this.sel % 9 + 1) % 9;
            } else if (k == 265 || k == 87) {
               this.sel = (this.sel + 72) % 81;
            } else if (k == 264 || k == 83) {
               this.sel = (this.sel + 9) % 81;
            }
         }
      }
   }

   @Override
   protected void onClick(float x, float y, int button) {
      if (this.diff < 0) {
         for (int d = 0; d < 4; d++) {
            float[] r = this.diffRect(d);
            if (x >= r[0] && x <= r[0] + r[2] && y >= r[1] && y <= r[1] + r[3]) {
               this.newPuzzle(d);
               return;
            }
         }
      } else {
         int hit = cellAt(x, y);
         if (hit >= 0) {
            this.sel = hit;
            if (button == 1) {
               this.place(0);
            }

            Sound.click();
         } else {
            int b = this.padAt(x, y);
            if (b >= 1 && b <= 9) {
               this.place(b);
            } else if (b == 10) {
               this.noteMode = !this.noteMode;
            } else if (b == 11) {
               this.place(0);
            } else if (b == 12) {
               this.doUndo();
            } else if (b == 13) {
               this.hint();
            }
         }
      }
   }

   private float[] diffRect(int d) {
      return new float[]{50.0F + d * 76, 118.0F, 70.0F, 92.0F};
   }

   private int padAt(float x, float y) {
      for (int i = 0; i < 9; i++) {
         float bx0 = 300.0F + i % 3 * 30;
         float by0 = 96 + i / 3 * 30;
         if (x >= bx0 && x < bx0 + 27.0F && y >= by0 && y < by0 + 27.0F) {
            return i + 1;
         }
      }

      for (int ix = 0; ix < 4; ix++) {
         float bx0 = 300.0F + ix % 2 * 45;
         float by0 = 190 + ix / 2 * 27;
         if (x >= bx0 && x < bx0 + 42.0F && y >= by0 && y < by0 + 24.0F) {
            return 10 + ix;
         }
      }

      return -1;
   }

   @Override
   protected void update(float dt) {
      this.fx.update(dt);

      for (int i = 0; i < 81; i++) {
         this.pop[i] = Math.max(0.0F, this.pop[i] - dt * 4.0F);
         if (this.wave[i] < 1.0F) {
            this.wave[i] = this.wave[i] + dt * 1.6F;
         }

         this.wrongT[i] = Math.max(0.0F, this.wrongT[i] - dt * 2.0F);
      }

      if (this.diff >= 0 && this.winT < 0.0F) {
         this.elapsed += dt;
         this.score = this.liveScore();
      }

      if (this.winT >= 0.0F) {
         this.winT += dt;
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

   private static float colX(int c) {
      return 14.0F + c * 29.0F + c / 3 * 3.0F;
   }

   private static float rowY(int r) {
      return 14.0F + r * 29.0F + r / 3 * 3.0F;
   }

   private float cx(int c) {
      return colX(c) + 14.5F;
   }

   private float cy(int r) {
      return rowY(r) + 14.5F;
   }

   private static int cellAt(float x, float y) {
      for (int r = 0; r < 9; r++) {
         float ry = rowY(r);
         if (!(y < ry) && !(y >= ry + 29.0F)) {
            for (int c = 0; c < 9; c++) {
               float rx = colX(c);
               if (x >= rx && x < rx + 29.0F) {
                  return r * 9 + c;
               }
            }
         }
      }

      return -1;
   }

   @Override
   protected void render() {
      float dt = Math.min(0.05F, Gx.dt);
      this.anim += dt;
      boolean lightT = Style.light;
      int bg0 = lightT ? -1051912 : -15985104;
      int bg1 = lightT ? -1906703 : -15655362;
      this.boxV(0.0F, 0.0F, 400.0F, 300.0F, 0.0F, bg0, bg1);

      for (int i = 0; i < 16; i++) {
         float x = (i * 83.3F + this.anim * 4.0F) % 400.0F;
         float y = (i * 47.1F + this.anim * (5 + i % 4)) % 300.0F;
         this.circle(x, y, 1.0F + i % 3 * 0.5F, lightT ? 335544320 : 419430399);
      }

      if (this.diff < 0 && this.state != GameView.State.READY) {
         this.chooser();
      } else if (this.diff < 0) {
         this.gridPreview();
      } else {
         float mbx = (this.mx - this.bx) / this.u;
         float mby = (this.my - this.by) / this.u;
         Gx.push();
         Gx.translate(this.S(this.fx.sx), this.S(this.fx.sy));
         int text = lightT ? -15395302 : -789258;
         int cellBg = lightT ? -1 : -15326134;
         int accent = -12604929;
         int userCol = lightT ? -14852144 : -7354113;
         Gx.shadow(this.X(14.0F), this.Y(14.0F), this.S(267.0F), this.S(267.0F), this.S(6.0F), this.S(6.0F), 0.4F);
         this.box(12.0F, 12.0F, 271.0F, 271.0F, 6.0F, lightT ? -14011568 : -16446952);
         int sr = this.sel / 9;
         int sc = this.sel % 9;
         int sv = this.cell[this.sel];
         int hoverCell = cellAt(mbx, mby);

         for (int i = 0; i < 81; i++) {
            int r = i / 9;
            int c = i % 9;
            float x = colX(c) + 0.5F;
            float y = rowY(r) + 0.5F;
            float w = 28.0F;
            float h = 28.0F;
            int bgc = cellBg;
            boolean peer = r == sr || c == sc || r / 3 == sr / 3 && c / 3 == sc / 3;
            if (peer) {
               bgc = Gx.mix(cellBg, accent, 0.1F);
            }

            if (sv != 0 && this.cell[i] == sv) {
               bgc = Gx.mix(cellBg, accent, 0.28F);
            }

            if (i == this.sel) {
               bgc = Gx.mix(cellBg, accent, 0.45F);
            } else if (i == hoverCell) {
               bgc = Gx.mix(bgc, lightT ? -16777216 : -1, 0.06F);
            }

            float wv = this.wave[i];
            if (wv > 0.0F && wv < 1.0F) {
               bgc = Gx.mix(bgc, -736942, (float)Math.sin(wv * Math.PI) * 0.55F);
            }

            if (this.wrongT[i] > 0.0F) {
               bgc = Gx.mix(bgc, -45730, this.wrongT[i] * 0.5F);
            }

            this.box(x, y, w, h, 2.2F, bgc);
            int v = this.cell[i];
            if (v == 0) {
               if (this.notes[i] != 0) {
                  for (int n = 1; n <= 9; n++) {
                     if ((this.notes[i] & 1 << n) != 0) {
                        float nx = x + w * ((n - 1) % 3 + 0.5F) / 3.0F;
                        float ny = y + h * ((n - 1) / 3 + 0.5F) / 3.0F;
                        boolean hl = n == sv;
                        if (hl) {
                           this.circle(nx, ny, 3.8F, Gx.withAlpha(accent, 0.35F));
                        }

                        this.text(String.valueOf(n), nx, ny, 6.2F, 2, hl ? text : (lightT ? -8748912 : -7562568));
                     }
                  }
               }
            } else {
               boolean wrong = !this.given[i] && v != this.sol[i];
               float s = 1.0F + 0.35F * Ease.outCubic(this.pop[i]) + (wv > 0.0F && wv < 1.0F ? 0.18F * (float)Math.sin(wv * Math.PI) : 0.0F);
               int col = this.given[i] ? text : (wrong ? -42390 : userCol);
               Gx.push();
               Gx.scaleAt(this.X(x + w / 2.0F), this.Y(y + h / 2.0F), s);
               this.text(String.valueOf(v), x + w / 2.0F, y + h / 2.0F, this.given[i] ? 15.0F : 14.5F, this.given[i] ? 3 : 2, col);
               Gx.pop();
            }
         }

         float selX = colX(sc);
         float selY = rowY(sr);
         float ax = Ease.to("sd_selx", selX, 22.0F);
         float ay = Ease.to("sd_sely", selY, 22.0F);
         Gx.outline(this.X(ax), this.Y(ay), this.S(29.0F), this.S(29.0F), this.S(3.0F), Math.max(1, this.S(1.4F)), accent);
         this.fx.draw(this, 0.0F, 0.0F);
         Gx.pop();
         int dc = DIFF_COL[this.diff];
         this.box(300.0F, 14.0F, 87.0F, 20.0F, 10.0F, Gx.withAlpha(dc, 0.2F));
         this.text(DIFF[this.diff], 343.5F, 24.0F, 8.0F, 3, dc);
         int m = (int)this.elapsed / 60;
         int s = (int)this.elapsed % 60;
         Gx.icon("clock", this.X(308.0F), this.Y(47.0F), 9.0F * this.u, Style.sub);
         Gx.textMid(String.format("%d:%02d", m, s), this.X(317.0F), this.Y(47.0F), 10.0F * this.u, 3, text);

         for (int i = 0; i < 3; i++) {
            boolean used = i < this.mistakes;
            Gx.icon(used ? "x" : "heart-fill", this.X(358.0F + i * 11), this.Y(47.0F), 8.0F * this.u, used ? -45730 : Gx.withAlpha(-45715, 0.9F));
         }

         this.text("Fehler " + this.mistakes + "/3  •  Tipps " + this.hints, 343.5F, 64.0F, 6.0F, 2, Style.sub);
         int filled = 0;

         for (int i = 0; i < 81; i++) {
            if (this.cell[i] == this.sol[i]) {
               filled++;
            }
         }

         this.box(300.0F, 74.0F, 87.0F, 4.0F, 2.0F, lightT ? 570425344 : 587202559);
         this.box(300.0F, 74.0F, 87 * filled / 81.0F, 4.0F, 2.0F, dc);
         int hover = this.padAt(mbx, mby);

         for (int ix = 0; ix < 9; ix++) {
            int v = ix + 1;
            int cnt = 0;

            for (int k = 0; k < 81; k++) {
               if (this.cell[k] == v && this.sol[k] == v) {
                  cnt++;
               }
            }

            float bx0 = 300.0F + ix % 3 * 30;
            float by0 = 96 + ix / 3 * 30;
            boolean done = cnt >= 9;
            boolean hv = hover == v;
            int bgcx = done ? (lightT ? 268435456 : 285212671) : (hv ? Gx.mix(cellBg, accent, 0.35F) : cellBg);
            this.box(bx0, by0, 27.0F, 27.0F, 6.0F, bgcx);
            if (this.noteMode && !done) {
               Gx.outline(this.X(bx0), this.Y(by0), this.S(27.0F), this.S(27.0F), this.S(6.0F), Math.max(1, this.S(0.8F)), Gx.withAlpha(accent, 0.6F));
            }

            this.text(String.valueOf(v), bx0 + 13.5F, by0 + 12.5F, this.noteMode ? 10.0F : 13.0F, 3, done ? Style.muted : (this.noteMode ? Style.sub : text));
            if (!done) {
               this.text(String.valueOf(9 - cnt), bx0 + 22.0F, by0 + 22.0F, 5.0F, 2, Style.muted);
            }
         }

         String[] lab = new String[]{"Notizen", "Radieren", "Zurück", "Tipp"};
         String[] ico = new String[]{"wand", "trash", "reset", "bolt"};

         for (int ix = 0; ix < 4; ix++) {
            float bx0x = 300.0F + ix % 2 * 45;
            float by0x = 190 + ix / 2 * 27;
            boolean on = ix == 0 && this.noteMode;
            boolean hvx = hover == 10 + ix;
            this.box(bx0x, by0x, 42.0F, 24.0F, 6.0F, on ? -12604929 : (hvx ? Gx.mix(cellBg, accent, 0.3F) : cellBg));
            Gx.icon(ico[ix], this.X(bx0x + 21.0F), this.Y(by0x + 9.0F), 8.5F * this.u, on ? -1 : Style.sub);
            this.text(lab[ix] + (ix == 0 ? (this.noteMode ? " an" : " aus") : ""), bx0x + 21.0F, by0x + 18.5F, 5.0F, 3, on ? -1 : Style.sub);
         }

         this.text("1-9  •  N Notizen  •  H Tipp", 343.5F, 256.0F, 5.2F, 2, Style.muted);
         this.text("Z Zurück  •  Entf Radieren", 343.5F, 265.0F, 5.2F, 2, Style.muted);
         if (this.winT >= 0.0F) {
            float t = Ease.outBack(Math.min(1.0F, this.winT / 0.4F));
            Gx.push();
            Gx.scaleAt(this.X(147.5F), this.Y(147.5F), t);
            this.box(77.5F, 125.5F, 140.0F, 44.0F, 14.0F, Gx.withAlpha(-16051160, 0.92F));
            this.text("Gelöst!", 147.5F, 142.5F, 15.0F, 3, -736942);
            this.text(this.liveScore() + " Punkte", 147.5F, 158.5F, 7.5F, 2, -1);
            Gx.pop();
         }

         this.fx.drawPopups(this, 0.0F, 0.0F);
      }
   }

   private void gridPreview() {
      String demo = "530070000600195000098000060800060003400803001700020006060000280000419005000080079";
      this.box(12.0F, 12.0F, 271.0F, 271.0F, 6.0F, Style.light ? -14011568 : -16446952);

      for (int i = 0; i < 81; i++) {
         int r = i / 9;
         int c = i % 9;
         float x = colX(c) + 0.5F;
         float y = rowY(r) + 0.5F;
         this.box(x, y, 28.0F, 28.0F, 2.2F, Style.light ? -1 : -15326134);
         char ch = demo.charAt(i);
         if (ch != '0') {
            this.text(String.valueOf(ch), x + 14.5F - 0.5F, y + 14.5F - 0.5F, 14.0F, 3, Style.light ? -15395302 : -789258);
         }
      }
   }

   private void chooser() {
      this.text("Wähle den Schwierigkeitsgrad", 200.0F, 78.0F, 13.0F, 3, Style.text);
      this.text("Jedes Rätsel hat genau eine Lösung", 200.0F, 96.0F, 7.5F, 1, Style.sub);
      float mbx = (this.mx - this.bx) / this.u;
      float mby = (this.my - this.by) / this.u;

      for (int d = 0; d < 4; d++) {
         float[] r = this.diffRect(d);
         boolean hv = mbx >= r[0] && mbx <= r[0] + r[2] && mby >= r[1] && mby <= r[1] + r[3];
         float h = Ease.to("sd_d" + d, hv ? 1.0F : 0.0F, 14.0F);
         float lift = h * 3.0F;
         int col = DIFF_COL[d];
         Gx.shadow(this.X(r[0]), this.Y(r[1] - lift), this.S(r[2]), this.S(r[3]), this.S(10.0F), this.S(4.0F + h * 4.0F), 0.3F + h * 0.2F);
         this.boxV(r[0], r[1] - lift, r[2], r[3], 10.0F, Gx.mix(Style.surface, col, 0.18F + 0.15F * h), Gx.mix(Style.surface, col, 0.05F));
         Gx.outline(this.X(r[0]), this.Y(r[1] - lift), this.S(r[2]), this.S(r[3]), this.S(10.0F), Math.max(1, this.S(0.8F)), Gx.withAlpha(col, 0.4F + 0.5F * h));

         for (int k = 0; k <= d; k++) {
            Gx.icon("star-fill", this.X(r[0] + r[2] / 2.0F + (k - d / 2.0F) * 9.0F), this.Y(r[1] - lift + 22.0F), 8.0F * this.u, col);
         }

         this.text(DIFF[d], r[0] + r[2] / 2.0F, r[1] - lift + 44.0F, 9.5F, 3, Style.text);
         this.text(CLUES[d] + " Vorgaben", r[0] + r[2] / 2.0F, r[1] - lift + 59.0F, 6.2F, 2, Style.sub);
         this.text("bis " + BASE[d] + " P.", r[0] + r[2] / 2.0F, r[1] - lift + 72.0F, 6.2F, 2, col);
         this.text(String.valueOf(d + 1), r[0] + r[2] - 8.0F, r[1] - lift + 9.0F, 5.5F, 3, Style.muted);
      }

      this.text("Klick oder Taste 1-4", 200.0F, 232.0F, 7.0F, 2, Style.muted);
   }
}
