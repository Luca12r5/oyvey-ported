package dev.lego.games;

import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class WaterSort extends GameView {
   private static final int CAP = 4;
   private final List<List<Integer>> tubes = new ArrayList<>();
   private int sel = -1;
   private int level;
   private int moves;
   private float doneT = -1.0F;

   WaterSort() {
      super("watersort");
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
      return "Level";
   }

   @Override
   protected String overTitle() {
      return "Aufgegeben";
   }

   @Override
   protected void reset() {
      this.level = 0;
      this.next();
   }

   private void next() {
      this.level++;
      this.score = this.level - 1;
      int colors = Math.min(6, 2 + this.level);
      List<Integer> all = new ArrayList<>();

      for (int c = 0; c < colors; c++) {
         for (int i = 0; i < 4; i++) {
            all.add(c);
         }
      }

      do {
         Collections.shuffle(all, this.rnd);
      } while (this.sortedAlready(all, colors));

      this.tubes.clear();

      for (int t = 0; t < colors; t++) {
         this.tubes.add(new ArrayList<>(all.subList(t * 4, t * 4 + 4)));
      }

      this.tubes.add(new ArrayList<>());
      this.tubes.add(new ArrayList<>());
      this.sel = -1;
      this.moves = 0;
      this.doneT = -1.0F;
   }

   private boolean sortedAlready(List<Integer> all, int colors) {
      for (int t = 0; t < colors; t++) {
         int v = all.get(t * 4);

         for (int i = 1; i < 4; i++) {
            if (all.get(t * 4 + i) != v) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean solved() {
      for (List<Integer> t : this.tubes) {
         if (!t.isEmpty()) {
            if (t.size() != 4) {
               return false;
            }

            for (int v : t) {
               if (v != t.get(0)) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   private float tx(int i) {
      return 200 - this.tubes.size() * 22 + i * 44 + 8;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (!(this.doneT >= 0.0F)) {
         for (int i = 0; i < this.tubes.size(); i++) {
            if (in(x, y, this.tx(i), 60.0F, 28.0F, 140.0F)) {
               if (this.sel < 0) {
                  if (!this.tubes.get(i).isEmpty()) {
                     this.sel = i;
                  }

                  return;
               }

               if (this.sel == i) {
                  this.sel = -1;
                  return;
               }

               List<Integer> from = this.tubes.get(this.sel);
               List<Integer> to = this.tubes.get(i);
               int top = from.get(from.size() - 1);
               if (to.size() < 4 && (to.isEmpty() || to.get(to.size() - 1) == top)) {
                  while (!from.isEmpty() && from.get(from.size() - 1) == top && to.size() < 4) {
                     to.add(from.remove(from.size() - 1));
                  }

                  this.moves++;
                  Sound.play("minecraft:item.bucket.empty", 1.4F, 0.3F);
                  if (this.solved()) {
                     this.doneT = 0.0F;
                     PackPuzzle.win();
                  }
               } else {
                  PackPuzzle.bad();
               }

               this.sel = -1;
               return;
            }
         }
      }
   }

   @Override
   protected void onKey(int k) {
      if (k == 82) {
         this.level--;
         this.next();
      }

      if (k == 257) {
         this.gameOver();
      }
   }

   @Override
   protected void update(float dt) {
      if (this.doneT >= 0.0F && (this.doneT += dt) > 1.0F) {
         this.next();
      }
   }

   @Override
   protected void render() {
      for (int i = 0; i < this.tubes.size(); i++) {
         float x = this.tx(i);
         float y = i == this.sel ? 48.0F : 60.0F;
         this.box(x - 2.0F, y - 2.0F, 32.0F, 144.0F, 12.0F, Style.light ? 855638016 : 1442840575);
         this.box(x, y, 28.0F, 140.0F, 11.0F, Style.light ? -723724 : -15724008);
         List<Integer> t = this.tubes.get(i);

         for (int k = 0; k < t.size(); k++) {
            this.box(x + 3.0F, y + 140.0F - 3.0F - (k + 1) * 33, 22.0F, 32.0F, k == 0 ? 9.0F : 2.0F, PackPuzzle.PAL[t.get(k)]);
         }
      }

      this.text("Level " + this.level + " · Züge " + this.moves, 200.0F, 20.0F, 8.0F, 3, Style.text);
      this.text("Gieß gleiche Farben zusammen · R = neu mischen · Enter = beenden", 200.0F, 230.0F, 6.2F, 2, Style.sub);
      if (this.doneT >= 0.0F) {
         this.text("Sortiert!", 200.0F, 40.0F, 12.0F, 3, Style.ok);
      }
   }
}
