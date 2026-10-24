package dev.lego.games;

import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

final class PipePuzzle extends GameView {
   private static final int N = 6;
   private static final float C = 40.0F;
   private static final float OX = 20.0F;
   private static final float OY = 14.0F;
   private int[][] pipe = new int[6][6];
   private boolean[][] wet = new boolean[6][6];
   private int moves;
   private int solved;

   PipePuzzle() {
      super("pipes");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 268.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Gelöst";
   }

   @Override
   protected String overTitle() {
      return this.solved + " Rätsel gelöst";
   }

   @Override
   protected void reset() {
      this.solved = 0;
      this.generate();
   }

   private static int rot(int m) {
      return (m << 1 | m >> 3) & 15;
   }

   private void generate() {
      this.pipe = new int[6][6];
      boolean[][] seen = new boolean[6][6];
      ArrayDeque<int[]> st = new ArrayDeque<>();
      st.push(new int[]{0, 0});
      seen[0][0] = true;

      while (!st.isEmpty()) {
         int[] p = st.peek();
         List<int[]> opts = new ArrayList<>();
         int[][] d = new int[][]{{-1, 0, 1, 4}, {0, 1, 2, 8}, {1, 0, 4, 1}, {0, -1, 8, 2}};

         for (int[] q : d) {
            int r = p[0] + q[0];
            int c = p[1] + q[1];
            if (r >= 0 && c >= 0 && r < 6 && c < 6 && !seen[r][c]) {
               opts.add(new int[]{r, c, q[2], q[3]});
            }
         }

         if (opts.isEmpty()) {
            st.pop();
         } else {
            int[] o = opts.get(this.rnd.nextInt(opts.size()));
            this.pipe[p[0]][p[1]] = this.pipe[p[0]][p[1]] | o[2];
            this.pipe[o[0]][o[1]] = this.pipe[o[0]][o[1]] | o[3];
            seen[o[0]][o[1]] = true;
            st.push(new int[]{o[0], o[1]});
         }
      }

      this.pipe[0][0] = this.pipe[0][0] | 8;

      for (int r = 0; r < 6; r++) {
         for (int c = 0; c < 6; c++) {
            int k = this.rnd.nextInt(4);

            for (int i = 0; i < k; i++) {
               this.pipe[r][c] = rot(this.pipe[r][c]);
            }
         }
      }

      this.moves = 0;
      this.flow();
   }

   private void flow() {
      this.wet = new boolean[6][6];
      if ((this.pipe[0][0] & 8) != 0) {
         ArrayDeque<int[]> q = new ArrayDeque<>();
         q.add(new int[]{0, 0});
         this.wet[0][0] = true;
         int[][] d = new int[][]{{-1, 0, 1, 4}, {0, 1, 2, 8}, {1, 0, 4, 1}, {0, -1, 8, 2}};

         while (!q.isEmpty()) {
            int[] p = q.poll();

            for (int[] e : d) {
               int r = p[0] + e[0];
               int c = p[1] + e[1];
               if ((this.pipe[p[0]][p[1]] & e[2]) != 0 && r >= 0 && c >= 0 && r < 6 && c < 6 && (this.pipe[r][c] & e[3]) != 0 && !this.wet[r][c]) {
                  this.wet[r][c] = true;
                  q.add(new int[]{r, c});
               }
            }
         }
      }
   }

   private boolean allWet() {
      for (boolean[] r : this.wet) {
         for (boolean b : r) {
            if (!b) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      int c = (int)((x - 20.0F) / 40.0F);
      int r = (int)((y - 14.0F) / 40.0F);
      if (!(x < 20.0F) && !(y < 14.0F) && c < 6 && r < 6) {
         this.pipe[r][c] = rot(this.pipe[r][c]);
         if (b == 1) {
            this.pipe[r][c] = rot(rot(this.pipe[r][c]));
         }

         this.moves++;
         Sound.play("minecraft:block.iron_trapdoor.close", 1.6F, 0.2F);
         this.flow();
         if (this.allWet()) {
            this.solved++;
            this.score = this.solved;
            PackPuzzle.win();
            this.generate();
         }
      }
   }

   @Override
   protected void onKey(int k) {
      if (k == 257) {
         this.gameOver();
      }
   }

   @Override
   protected void update(float dt) {
   }

   @Override
   protected void render() {
      this.box(6.0F, 29.0F, 14.0F, 10.0F, 2.0F, -13728034);

      for (int r = 0; r < 6; r++) {
         for (int c = 0; c < 6; c++) {
            float x = 20.0F + c * 40.0F;
            float y = 14.0F + r * 40.0F;
            float cx = x + 20.0F;
            float cy = y + 20.0F;
            this.box(x + 1.0F, y + 1.0F, 38.0F, 38.0F, 5.0F, Style.light ? -1644306 : -14934233);
            int col = this.wet[r][c] ? -13728034 : -6642771;
            int m = this.pipe[r][c];
            if ((m & 1) != 0) {
               this.box(cx - 5.0F, y, 10.0F, 20.0F, 0.0F, col);
            }

            if ((m & 4) != 0) {
               this.box(cx - 5.0F, cy, 10.0F, 20.0F, 0.0F, col);
            }

            if ((m & 8) != 0) {
               this.box(x, cy - 5.0F, 20.0F, 10.0F, 0.0F, col);
            }

            if ((m & 2) != 0) {
               this.box(cx, cy - 5.0F, 20.0F, 10.0F, 0.0F, col);
            }

            this.circle(cx, cy, 6.0F, col);
         }
      }

      int n = 0;

      for (boolean[] r : this.wet) {
         for (boolean b : r) {
            if (b) {
               n++;
            }
         }
      }

      this.textL("Wasser: " + n + "/36", 280.0F, 40.0F, 8.0F, 3, Style.text);
      this.textL("Gelöst: " + this.solved, 280.0F, 60.0F, 8.0F, 3, Style.sub);
      this.textL("Klick dreht ein Rohr.", 280.0F, 100.0F, 6.4F, 2, Style.sub);
      this.textL("Alle Rohre müssen", 280.0F, 112.0F, 6.4F, 2, Style.sub);
      this.textL("Wasser bekommen.", 280.0F, 124.0F, 6.4F, 2, Style.sub);
      this.textL("Enter = beenden", 280.0F, 150.0F, 6.4F, 2, Style.muted);
   }
}
