package dev.lego.games;

import dev.lego.ui.Style;

final class PatternMemory extends GameView {
   private int n = 4;
   private int lives;
   private boolean[] pattern;
   private boolean[] clicked;
   private float show;
   private int level;
   private int found;

   PatternMemory() {
      super("patternmemory");
   }

   @Override
   protected float boardW() {
      return 300.0F;
   }

   @Override
   protected float boardH() {
      return 310.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Level";
   }

   @Override
   protected void reset() {
      this.level = 0;
      this.lives = 3;
      this.n = 3;
      this.next();
   }

   private void next() {
      this.level++;
      this.score = this.level - 1;
      if (this.level % 3 == 0 && this.n < 7) {
         this.n++;
      }

      this.pattern = new boolean[this.n * this.n];
      this.clicked = new boolean[this.n * this.n];
      int k = Math.min(this.n * this.n - 2, 2 + this.level);
      int i = 0;

      while (i < k) {
         int p = this.rnd.nextInt(this.n * this.n);
         if (!this.pattern[p]) {
            this.pattern[p] = true;
            i++;
         }
      }

      this.show = 1.4F;
      this.found = 0;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (!(this.show > 0.0F)) {
         float cs = 280.0F / this.n;
         int c = (int)((x - 10.0F) / cs);
         int r = (int)((y - 10.0F) / cs);
         if (c >= 0 && r >= 0 && c < this.n && r < this.n) {
            int i = r * this.n + c;
            if (!this.clicked[i]) {
               this.clicked[i] = true;
               if (this.pattern[i]) {
                  this.found++;
                  PackPuzzle.click();
                  int need = 0;

                  for (boolean p : this.pattern) {
                     if (p) {
                        need++;
                     }
                  }

                  if (this.found == need) {
                     PackPuzzle.win();
                     this.next();
                  }
               } else {
                  this.lives--;
                  PackPuzzle.bad();
                  if (this.lives <= 0) {
                     this.gameOver();
                  } else {
                     this.show = 1.0F;
                     this.clicked = new boolean[this.n * this.n];
                     this.found = 0;
                  }
               }
            }
         }
      }
   }

   @Override
   protected void update(float dt) {
      this.show -= dt;
   }

   @Override
   protected void render() {
      float cs = 280.0F / this.n;

      for (int i = 0; i < this.n * this.n; i++) {
         float x = 10.0F + i % this.n * cs;
         float y = 10.0F + i / this.n * cs;
         boolean lit = this.show > 0.0F ? this.pattern[i] : this.clicked[i];
         int col = !lit ? Style.surface : (this.pattern[i] ? -12604929 : -5234646);
         this.box(x + 2.0F, y + 2.0F, cs - 4.0F, cs - 4.0F, 6.0F, col);
      }

      this.text(
         "Level " + this.level + " · Leben " + this.lives + (this.show > 0.0F ? " · Merken!" : " · Klick die Felder"), 150.0F, 302.0F, 6.6F, 3, Style.sub
      );
   }
}
