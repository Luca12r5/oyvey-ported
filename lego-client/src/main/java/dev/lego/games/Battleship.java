package dev.lego.games;

import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;

final class Battleship extends GameView {
   private static final int N = 8;
   private static final int[] SHIPS = new int[]{4, 3, 3, 2, 2};
   private static final float C = 22.0F;
   private static final float PX = 12.0F;
   private static final float EX = 214.0F;
   private static final float OY = 40.0F;
   private int[][] mine;
   private int[][] enemy;
   private int[][] myShots;
   private int[][] aiShots;
   private int turn;
   private float think;
   private final List<int[]> targets = new ArrayList<>();
   private String msg = "";

   Battleship() {
      super("battleship");
   }

   @Override
   protected boolean hasAi() {
      return true;
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 260.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Treffer";
   }

   @Override
   protected String overTitle() {
      return left(this.enemy, this.myShots) == 0 ? "Flotte versenkt - gewonnen!" : "Deine Flotte ist gesunken";
   }

   private int[][] place() {
      int[][] g = new int[8][8];

      for (int s : SHIPS) {
         boolean hor;
         int r;
         int c;
         boolean ok;
         do {
            hor = this.rnd.nextBoolean();
            r = this.rnd.nextInt(hor ? 8 : 8 - s + 1);
            c = this.rnd.nextInt(hor ? 8 - s + 1 : 8);
            ok = true;

            for (int i = 0; i < s && ok; i++) {
               int rr = r + (hor ? 0 : i);
               int cc = c + (hor ? i : 0);

               for (int dr = -1; dr <= 1; dr++) {
                  for (int dc = -1; dc <= 1; dc++) {
                     int a = rr + dr;
                     int b2 = cc + dc;
                     if (a >= 0 && b2 >= 0 && a < 8 && b2 < 8 && g[a][b2] == 1) {
                        ok = false;
                     }
                  }
               }
            }
         } while (!ok);

         for (int i = 0; i < s; i++) {
            g[r + (hor ? 0 : i)][c + (hor ? i : 0)] = 1;
         }
      }

      return g;
   }

   private static int left(int[][] ships, int[][] shots) {
      int n = 0;

      for (int r = 0; r < 8; r++) {
         for (int c = 0; c < 8; c++) {
            if (ships[r][c] == 1 && shots[r][c] != 2) {
               n++;
            }
         }
      }

      return n;
   }

   @Override
   protected void reset() {
      this.mine = this.place();
      this.enemy = this.place();
      this.myShots = new int[8][8];
      this.aiShots = new int[8][8];
      this.turn = 1;
      this.think = 0.0F;
      this.targets.clear();
      this.msg = "";
      this.score = 0L;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (this.turn == 1) {
         int c = (int)((x - 214.0F) / 22.0F);
         int r = (int)((y - 40.0F) / 22.0F);
         if (!(x < 214.0F) && !(y < 40.0F) && c < 8 && r < 8 && this.myShots[r][c] == 0) {
            boolean hit = this.enemy[r][c] == 1;
            this.myShots[r][c] = hit ? 2 : 1;
            if (hit) {
               this.score++;
               Sound.play("minecraft:entity.generic.explode", 1.6F, 0.25F);
               this.msg = "Treffer!";
               if (left(this.enemy, this.myShots) == 0) {
                  this.score = this.score + left(this.mine, this.aiShots) * 5;
                  PackPuzzle.win();
                  this.gameOver();
               }
            } else {
               Sound.play("minecraft:entity.generic.splash", 1.2F, 0.3F);
               this.msg = "Wasser";
               this.turn = 2;
               this.think = 0.0F;
            }
         }
      }
   }

   private int[] aiPick() {
      int lv = this.aiLevel();

      while (!this.targets.isEmpty() && lv > 0) {
         int[] t = this.targets.remove(this.targets.size() - 1);
         if (t[0] >= 0 && t[1] >= 0 && t[0] < 8 && t[1] < 8 && this.aiShots[t[0]][t[1]] == 0) {
            return t;
         }
      }

      List<int[]> f = new ArrayList<>();

      for (int r = 0; r < 8; r++) {
         for (int c = 0; c < 8; c++) {
            if (this.aiShots[r][c] == 0 && (lv < 3 || (r + c) % 2 == 0)) {
               f.add(new int[]{r, c});
            }
         }
      }

      if (f.isEmpty()) {
         for (int r = 0; r < 8; r++) {
            for (int cx = 0; cx < 8; cx++) {
               if (this.aiShots[r][cx] == 0) {
                  f.add(new int[]{r, cx});
               }
            }
         }
      }

      return f.get(this.rnd.nextInt(f.size()));
   }

   @Override
   protected void update(float dt) {
      if (this.turn == 2 && !((this.think += dt) < 0.7F)) {
         this.think = 0.0F;
         int[] p = this.aiPick();
         boolean hit = this.mine[p[0]][p[1]] == 1;
         this.aiShots[p[0]][p[1]] = hit ? 2 : 1;
         if (hit) {
            Sound.play("minecraft:entity.generic.explode", 1.2F, 0.25F);
            this.targets.add(new int[]{p[0] + 1, p[1]});
            this.targets.add(new int[]{p[0] - 1, p[1]});
            this.targets.add(new int[]{p[0], p[1] + 1});
            this.targets.add(new int[]{p[0], p[1] - 1});
            if (left(this.mine, this.aiShots) == 0) {
               PackPuzzle.bad();
               this.gameOver();
            }
         } else {
            this.turn = 1;
            Sound.play("minecraft:entity.generic.splash", 1.0F, 0.25F);
         }
      }
   }

   private void grid(float ox, int[][] ships, int[][] shots, boolean showShips) {
      for (int r = 0; r < 8; r++) {
         for (int c = 0; c < 8; c++) {
            float x = ox + c * 22.0F;
            float y = 40.0F + r * 22.0F;
            this.box(x + 1.0F, y + 1.0F, 20.0F, 20.0F, 3.0F, -15054214);
            if (showShips && ships[r][c] == 1) {
               this.box(x + 3.0F, y + 3.0F, 16.0F, 16.0F, 3.0F, -6642771);
            }

            if (shots[r][c] == 1) {
               this.circle(x + 11.0F, y + 11.0F, 3.0F, -855638017);
            }

            if (shots[r][c] == 2) {
               this.circle(x + 11.0F, y + 11.0F, 7.0F, -1900533);
               this.circle(x + 11.0F, y + 11.0F, 3.0F, -20432);
            }
         }
      }
   }

   @Override
   protected void render() {
      this.text("Deine Flotte", 100.0F, 20.0F, 8.0F, 3, Style.sub);
      this.text("Gegner - hier schießen", 302.0F, 20.0F, 8.0F, 3, this.turn == 1 ? Style.ok : Style.sub);
      this.grid(12.0F, this.mine, this.aiShots, true);
      this.grid(214.0F, this.enemy, this.myShots, this.state == GameView.State.OVER);
      this.text(this.turn == 1 ? "Du bist dran" + (this.msg.isEmpty() ? "" : " · " + this.msg) : "Gegner zielt...", 200.0F, 238.0F, 8.0F, 3, Style.text);
   }
}
