package dev.lego.games;

import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class BallMaze extends GameView {
   private static final int N = 11;
   private static final float C = 24.0F;
   private static final float OX = 8.0F;
   private static final float OY = 8.0F;
   private boolean[][] wall;
   private float bx2;
   private float by2;
   private float vx;
   private float vy;
   private int level;
   private final List<int[]> holes = new ArrayList<>();

   BallMaze() {
      super("ballmaze");
   }

   @Override
   protected float boardW() {
      return 390.0F;
   }

   @Override
   protected float boardH() {
      return 280.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Level";
   }

   @Override
   protected String overTitle() {
      return "In ein Loch gefallen";
   }

   @Override
   protected void reset() {
      this.level = 0;
      this.next();
   }

   private void next() {
      this.level++;
      this.score = this.level - 1;
      this.wall = new boolean[11][11];

      for (boolean[] r : this.wall) {
         Arrays.fill(r, true);
      }

      this.carve(1, 1);
      this.holes.clear();

      for (int i = 0; i < Math.min(8, this.level); i++) {
         int r = 1 + this.rnd.nextInt(9);
         int c = 1 + this.rnd.nextInt(9);
         if (!this.wall[r][c] && (r > 2 || c > 2) && (r < 8 || c < 8)) {
            this.holes.add(new int[]{r, c});
         }
      }

      this.bx2 = 44.0F;
      this.by2 = 44.0F;
      this.vx = this.vy = 0.0F;
   }

   private void carve(int r, int c) {
      this.wall[r][c] = false;
      int[][] d = new int[][]{{0, 2}, {0, -2}, {2, 0}, {-2, 0}};

      for (int i = 3; i > 0; i--) {
         int j = this.rnd.nextInt(i + 1);
         int[] t = d[i];
         d[i] = d[j];
         d[j] = t;
      }

      for (int[] q : d) {
         int rr = r + q[0];
         int cc = c + q[1];
         if (rr > 0 && cc > 0 && rr < 10 && cc < 10 && this.wall[rr][cc]) {
            this.wall[r + q[0] / 2][c + q[1] / 2] = false;
            this.carve(rr, cc);
         }
      }
   }

   private boolean solid(float x, float y) {
      int c = (int)((x - 8.0F) / 24.0F);
      int r = (int)((y - 8.0F) / 24.0F);
      return r < 0 || c < 0 || r >= 11 || c >= 11 || this.wall[r][c];
   }

   @Override
   protected void update(float dt) {
      float tx = Math.max(-1.0F, Math.min(1.0F, (this.bmx() - this.bx2) / 80.0F));
      float ty = Math.max(-1.0F, Math.min(1.0F, (this.bmy() - this.by2) / 80.0F));
      this.vx += tx * 500.0F * dt;
      this.vy += ty * 500.0F * dt;
      this.vx *= 1.0F - 1.5F * dt;
      this.vy *= 1.0F - 1.5F * dt;
      float r = 6.0F;
      float nx = this.bx2 + this.vx * dt;
      float ny = this.by2 + this.vy * dt;
      if (!this.solid(nx - r, this.by2)
         && !this.solid(nx + r, this.by2)
         && !this.solid(nx - r, this.by2 - r + 1.0F)
         && !this.solid(nx + r, this.by2 + r - 1.0F)) {
         this.bx2 = nx;
      } else {
         this.vx = -this.vx * 0.3F;
      }

      if (!this.solid(this.bx2, ny - r)
         && !this.solid(this.bx2, ny + r)
         && !this.solid(this.bx2 - r + 1.0F, ny - r)
         && !this.solid(this.bx2 + r - 1.0F, ny + r)) {
         this.by2 = ny;
      } else {
         this.vy = -this.vy * 0.3F;
      }

      for (int[] h : this.holes) {
         if (Math.hypot(this.bx2 - (8.0F + h[1] * 24.0F + 12.0F), this.by2 - (8.0F + h[0] * 24.0F + 12.0F)) < 7.0) {
            PackPuzzle.bad();
            this.gameOver();
            return;
         }
      }

      if (Math.hypot(this.bx2 - 236.0F, this.by2 - 236.0F) < 9.0) {
         PackPuzzle.win();
         this.next();
      }
   }

   @Override
   protected void render() {
      this.box(8.0F, 8.0F, 264.0F, 264.0F, 6.0F, -2047862);

      for (int r = 0; r < 11; r++) {
         for (int c = 0; c < 11; c++) {
            if (this.wall[r][c]) {
               this.box(8.0F + c * 24.0F, 8.0F + r * 24.0F, 24.0F, 24.0F, 2.0F, -7710160);
            }
         }
      }

      for (int[] h : this.holes) {
         this.circle(8.0F + h[1] * 24.0F + 12.0F, 8.0F + h[0] * 24.0F + 12.0F, 8.0F, -15724528);
      }

      this.circle(236.0F, 236.0F, 9.0F, -12987565);
      this.circle(this.bx2, this.by2, 6.0F, -4669238);
      this.circle(this.bx2 - 2.0F, this.by2 - 2.0F, 2.0F, -1);
      float sx = 290.0F;
      this.textL("Level " + this.level, sx, 30.0F, 9.0F, 3, Style.text);
      this.textL("Maus = neigen", sx, 60.0F, 6.6F, 2, Style.sub);
      this.textL("Ins grüne Ziel,", sx, 80.0F, 6.6F, 2, Style.sub);
      this.textL("nicht in die Löcher", sx, 92.0F, 6.6F, 2, Style.sub);
   }
}
