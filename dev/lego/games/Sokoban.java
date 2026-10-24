package dev.lego.games;

import dev.lego.ui.Sound;
import dev.lego.ui.Style;

final class Sokoban extends GameView {
   private static final String[][] LEVELS = new String[][]{
      {"#######", "#.    #", "# $   #", "#  @  #", "#     #", "#######"},
      {"########", "#  .   #", "# $$ . #", "#  @   #", "#      #", "########"},
      {"#######", "#. .  #", "# $#$ #", "#  @  #", "# $ . #", "#######"},
      {"  #####", "###   #", "#.@$  #", "### $.#", "#.##$ #", "# # . ##", "#$ *$$.#", "#   .  #", "########"},
      {"########", "#      #", "# .**. #", "# $  $ #", "#  @   #", "########"},
      {"#########", "#   #   #", "# $ . $ #", "##.#@#.##", "# $ . $ #", "#   #   #", "#########"}
   };
   private char[][] map;
   private boolean[][] goal;
   private int pr;
   private int pc;
   private int level;
   private int moves;
   private float doneT = -1.0F;

   Sokoban() {
      super("sokoban");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 270.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Level";
   }

   @Override
   protected String overTitle() {
      return "Alle Level geschafft!";
   }

   @Override
   protected void reset() {
      this.level = 0;
      this.score = 0L;
      this.load();
   }

   private void load() {
      String[] l = LEVELS[this.level];
      int w = 0;

      for (String s : l) {
         w = Math.max(w, s.length());
      }

      this.map = new char[l.length][w];
      this.goal = new boolean[l.length][w];

      for (int r = 0; r < l.length; r++) {
         for (int c = 0; c < w; c++) {
            char ch = c < l[r].length() ? l[r].charAt(c) : 32;
            if (ch == '.' || ch == '*' || ch == '+') {
               this.goal[r][c] = true;
            }

            if (ch == '@' || ch == '+') {
               this.pr = r;
               this.pc = c;
               ch = ' ';
            }

            if (ch == '*') {
               ch = '$';
            }

            if (ch == '.') {
               ch = ' ';
            }

            this.map[r][c] = ch;
         }
      }

      this.moves = 0;
      this.doneT = -1.0F;
   }

   private boolean done() {
      for (int r = 0; r < this.map.length; r++) {
         for (int c = 0; c < this.map[0].length; c++) {
            if (this.goal[r][c] && this.map[r][c] != '$') {
               return false;
            }
         }
      }

      return true;
   }

   private void move(int dr, int dc) {
      if (!(this.doneT >= 0.0F)) {
         int r = this.pr + dr;
         int c = this.pc + dc;
         if (this.map[r][c] != '#') {
            if (this.map[r][c] == '$') {
               int r2 = r + dr;
               int c2 = c + dc;
               if (this.map[r2][c2] != ' ') {
                  return;
               }

               this.map[r2][c2] = '$';
               this.map[r][c] = ' ';
               Sound.play("minecraft:block.stone.place", 1.2F, 0.3F);
            }

            this.pr = r;
            this.pc = c;
            this.moves++;
            if (this.done()) {
               this.doneT = 0.0F;
               this.score = this.level + 1;
               PackPuzzle.win();
            }
         }
      }
   }

   @Override
   protected void onKey(int k) {
      if (k == 265 || k == 87) {
         this.move(-1, 0);
      } else if (k == 264 || k == 83) {
         this.move(1, 0);
      } else if (k == 263 || k == 65) {
         this.move(0, -1);
      } else if (k == 262 || k == 68) {
         this.move(0, 1);
      } else if (k == 82) {
         this.load();
      }
   }

   @Override
   protected void update(float dt) {
      if (this.doneT >= 0.0F && (this.doneT += dt) > 1.0F) {
         this.level++;
         if (this.level >= LEVELS.length) {
            this.gameOver();
         } else {
            this.load();
         }
      }
   }

   @Override
   protected void render() {
      float C = Math.min(28.0F, Math.min(380.0F / this.map[0].length, 230.0F / this.map.length));
      float ox = 200.0F - this.map[0].length * C / 2.0F;
      float oy = 125.0F - this.map.length * C / 2.0F;

      for (int r = 0; r < this.map.length; r++) {
         for (int c = 0; c < this.map[0].length; c++) {
            float x = ox + c * C;
            float y = oy + r * C;
            if (this.map[r][c] == '#') {
               this.brick(x, y, C, C, -7710160);
            } else {
               this.box(x, y, C, C, 0.0F, Style.light ? -1644306 : -14934233);
               if (this.goal[r][c]) {
                  this.circle(x + C / 2.0F, y + C / 2.0F, C * 0.18F, -12987565);
               }

               if (this.map[r][c] == '$') {
                  this.brick(x + 2.0F, y + 2.0F, C - 4.0F, C - 4.0F, this.goal[r][c] ? -13722038 : -30208);
               }
            }
         }
      }

      float x = ox + this.pc * C;
      float y = oy + this.pr * C;
      this.circle(x + C / 2.0F, y + C / 2.0F, C * 0.36F, -13053);
      this.circle(x + C / 2.0F - 3.0F, y + C / 2.0F - 2.0F, 1.5F, -15000798);
      this.circle(x + C / 2.0F + 3.0F, y + C / 2.0F - 2.0F, 1.5F, -15000798);
      this.textL("Level " + (this.level + 1) + "/" + LEVELS.length + " · Züge " + this.moves + " · R = neu", 8.0F, 260.0F, 6.8F, 3, Style.sub);
      if (this.doneT >= 0.0F) {
         this.text("Geschafft!", 200.0F, 30.0F, 14.0F, 3, Style.ok);
      }
   }
}
