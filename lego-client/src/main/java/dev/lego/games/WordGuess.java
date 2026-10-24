package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Style;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class WordGuess extends GameView {
   private static final int TRIES = 6;
   private String target;
   private final String[] rows = new String[6];
   private final int[][] res = new int[6][5];
   private int row;
   private int streak;
   private String cur = "";
   private float nextT = -1.0F;
   private float shake;
   private final Map<Character, Integer> keys = new HashMap<>();
   private final List<String> list = Words.five();
   private static final String KB = "QWERTZUIOPASDFGHJKLYXCVBNM";

   WordGuess() {
      super("wordguess");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 320.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Serie";
   }

   @Override
   protected String overTitle() {
      return "Das Wort war " + this.target;
   }

   @Override
   protected void reset() {
      this.streak = 0;
      this.next();
   }

   private void next() {
      this.target = this.list.get(this.rnd.nextInt(this.list.size()));
      Arrays.fill(this.rows, null);
      this.row = 0;
      this.cur = "";
      this.nextT = -1.0F;
      this.keys.clear();
   }

   private void submit() {
      if (this.cur.length() == 5 && !(this.nextT >= 0.0F)) {
         int[] r = new int[5];
         int[] cnt = new int[26];

         for (int i = 0; i < 5; i++) {
            if (this.cur.charAt(i) == this.target.charAt(i)) {
               r[i] = 2;
            } else {
               cnt[this.target.charAt(i) - 'A']++;
            }
         }

         for (int ix = 0; ix < 5; ix++) {
            if (r[ix] == 0 && cnt[this.cur.charAt(ix) - 'A'] > 0) {
               r[ix] = 1;
               cnt[this.cur.charAt(ix) - 'A']--;
            }
         }

         this.rows[this.row] = this.cur;
         this.res[this.row] = r;

         for (int ixx = 0; ixx < 5; ixx++) {
            this.keys.merge(this.cur.charAt(ixx), r[ixx], Math::max);
         }

         this.row++;
         if (this.cur.equals(this.target)) {
            this.streak++;
            this.score = this.streak;
            this.nextT = 0.0F;
            PackPuzzle.win();
         } else if (this.row >= 6) {
            this.score = this.streak;
            PackPuzzle.bad();
            this.gameOver();
         } else {
            PackPuzzle.click();
         }

         this.cur = "";
      } else {
         this.shake = 0.3F;
      }
   }

   @Override
   public boolean chr(String s) {
      if (this.state == GameView.State.PLAYING && !(this.nextT >= 0.0F)) {
         for (char c : Words.norm(s).toCharArray()) {
            if (c >= 'A' && c <= 'Z' && this.cur.length() < 5) {
               this.cur = this.cur + c;
            }
         }

         return true;
      } else {
         return true;
      }
   }

   @Override
   protected void onKey(int k) {
      if (k == 259 && !this.cur.isEmpty()) {
         this.cur = this.cur.substring(0, this.cur.length() - 1);
      }

      if (k == 257) {
         this.submit();
      }
   }

   @Override
   protected void update(float dt) {
      this.shake = Math.max(0.0F, this.shake - dt);
      if (this.nextT >= 0.0F && (this.nextT += dt) > 1.5F) {
         this.next();
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
      for (int i = 0; i < "QWERTZUIOPASDFGHJKLYXCVBNM".length(); i++) {
         float[] p = this.kpos(i);
         if (in(x, y, p[0], p[1], 24.0F, 24.0F)) {
            this.chr(String.valueOf("QWERTZUIOPASDFGHJKLYXCVBNM".charAt(i)));
         }
      }

      if (in(x, y, 312, 296, 60, 20)) {
         this.submit();
      }

      if (in(x, y, 28, 296, 60, 20)) {
         this.onKey(259);
      }
   }

   private float[] kpos(int i) {
      int r = i < 10 ? 0 : (i < 19 ? 1 : 2);
      int c = r == 0 ? i : (r == 1 ? i - 10 : i - 19);
      return new float[]{50 + r * 13 + c * 30, 218 + r * 26};
   }

   private int col(int v) {
      return v == 2 ? -13722038 : (v == 1 ? -2842102 : -11907496);
   }

   @Override
   protected void render() {
      float sx = 115.0F;

      for (int r = 0; r < 6; r++) {
         for (int i = 0; i < 5; i++) {
            float x = sx + i * 34 + (r == this.row ? (float)Math.sin(this.shake * 60.0F) * this.shake * 10.0F : 0.0F);
            float y = 8 + r * 34;
            String w = this.rows[r];
            char c = w != null ? w.charAt(i) : (r == this.row && i < this.cur.length() ? this.cur.charAt(i) : 32);
            int bg = w != null ? this.col(this.res[r][i]) : (Style.light ? -1644306 : -14934233);
            this.box(x, y, 30.0F, 30.0F, 5.0F, bg);
            if (w == null) {
               Gx.outline(this.X(x), this.Y(y), this.S(30.0F), this.S(30.0F), this.S(5.0F), Math.max(1, this.S(0.8F)), r == this.row ? Style.sub : Style.stroke);
            }

            if (c != ' ') {
               this.text(String.valueOf(c), x + 15.0F, y + 16.0F, 13.0F, 3, w != null ? -1 : Style.text);
            }
         }
      }

      for (int i = 0; i < "QWERTZUIOPASDFGHJKLYXCVBNM".length(); i++) {
         float[] p = this.kpos(i);
         Integer v = this.keys.get("QWERTZUIOPASDFGHJKLYXCVBNM".charAt(i));
         this.box(p[0], p[1], 24.0F, 24.0F, 5.0F, v == null ? (Style.light ? -3091236 : -14012872) : this.col(v));
         this.text(String.valueOf("QWERTZUIOPASDFGHJKLYXCVBNM".charAt(i)), p[0] + 12.0F, p[1] + 13.0F, 8.0F, 3, v == null ? Style.text : -1);
      }

      this.button("⌫", 28.0F, 296.0F, 60.0F, 20.0F, -10853776);
      this.button("Prüfen", 312.0F, 296.0F, 60.0F, 20.0F, Style.accent);
      this.text("Serie: " + this.streak, 200.0F, 306.0F, 7.0F, 3, Style.sub);
      if (this.nextT >= 0.0F) {
         this.text("Richtig!", 200.0F, 112.0F, 16.0F, 3, -12987565);
      }
   }
}
