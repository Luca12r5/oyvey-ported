package dev.lego.games;

import dev.lego.ui.Sound;
import dev.lego.ui.Style;

public final class RPS extends GameView {
   private static final String[] NAMES = new String[]{"Stein", "Schere", "Papier"};
   private static final int[][] BEATS = new int[][]{{1, 0}, {2, 1}, {0, 2}};
   private final Duels.Duel duel;
   private int round = -1;
   private int myScore;
   private int oppScore;
   private int myPick = -1;
   private int oppPick = -1;
   private float revealAt;
   private float clock;
   private boolean revealed;

   public RPS() {
      super("rps");
      this.duel = null;
   }

   public RPS(Duels.Duel d) {
      super("rps");
      this.duel = d;
   }

   @Override
   protected void start() {
      if (this.duel != null && this.duel.oppLeft) {
         this.back();
      } else {
         super.start();
         if (this.duel != null) {
            this.round++;
         }
      }
   }

   @Override
   protected void back() {
      if (this.duel != null) {
         this.duel.leave();
      }

      super.back();
   }

   @Override
   public void removed() {
      if (this.duel != null) {
         this.duel.leave();
      }

      super.removed();
   }

   @Override
   protected boolean hasAi() {
      return this.duel == null;
   }

   @Override
   protected float boardW() {
      return 360.0F;
   }

   @Override
   protected float boardH() {
      return 220.0F;
   }

   @Override
   protected void reset() {
      this.myPick = this.oppPick = -1;
      this.revealed = false;
      this.revealAt = 0.0F;
      this.clock = 0.0F;
   }

   @Override
   protected String overTitle() {
      if (this.duel != null && this.duel.oppLeft) {
         return this.duel.oppName + " ist gegangen";
      } else {
         return this.myScore >= 3 ? "Gewonnen!" : "Verloren";
      }
   }

   @Override
   protected void update(float dt) {
      this.clock += dt;
      if (this.state == GameView.State.PLAYING) {
         if (this.duel != null && this.duel.oppLeft) {
            this.gameOver(false);
         } else {
            if (this.myPick >= 0 && this.oppPick < 0) {
               if (this.duel != null) {
                  Integer m = this.duel.take(this.round);
                  if (m != null && m >= 0 && m <= 2) {
                     this.oppPick = m;
                  }
               } else {
                  int diff = GameInfo.difficulty("rps");
                  if (diff <= 1) {
                     this.oppPick = (int)(Math.random() * 3.0);
                  } else {
                     this.oppPick = BEATS[this.myPick][1];
                  }

                  if (diff == 2 && Math.random() < 0.35) {
                     this.oppPick = (int)(Math.random() * 3.0);
                  }
               }
            }

            if (this.myPick >= 0 && this.oppPick >= 0 && !this.revealed) {
               if (this.revealAt == 0.0F) {
                  this.revealAt = this.clock + (this.duel != null ? 0.3F : 0.6F);
               }

               if (this.clock >= this.revealAt) {
                  this.revealed = true;
                  int res = this.myPick == this.oppPick ? 0 : (BEATS[this.myPick][0] == this.oppPick ? 1 : -1);
                  if (res == 1) {
                     this.myScore++;
                     Sound.play("minecraft:entity.experience_orb.pickup", 1.2F, 0.6F);
                  } else if (res == -1) {
                     this.oppScore++;
                     Sound.play("minecraft:entity.villager.no", 0.8F, 0.6F);
                  } else {
                     Sound.play("minecraft:block.note_block.hat", 1.0F, 0.4F);
                  }
               }
            }

            if (this.revealed && this.clock > this.revealAt + 1.5F) {
               if (this.myScore >= 3 || this.oppScore >= 3) {
                  this.gameOver(this.duel == null);
                  return;
               }

               this.myPick = this.oppPick = -1;
               this.revealed = false;
               this.revealAt = 0.0F;
               if (this.duel != null) {
                  this.round++;
               }
            }
         }
      }
   }

   @Override
   protected void onClick(float x, float y, int bt) {
      if (this.myPick < 0 && this.state == GameView.State.PLAYING) {
         float bw2 = this.S(90.0F);
         float bh2 = this.S(90.0F);
         float gap = this.S(12.0F);
         float startX = this.boardW() / 2.0F - 1.5F * bw2 - gap;
         float by2 = this.S(60.0F);

         for (int i = 0; i < 3; i++) {
            float bx = startX + i * (bw2 + gap);
            if (x >= bx && x <= bx + bw2 && y >= by2 && y <= by2 + bh2) {
               this.myPick = i;
               if (this.duel != null) {
                  this.duel.send(this.round, i);
               }

               Sound.play("minecraft:ui.button.click", 1.0F, 0.5F);
               return;
            }
         }
      }
   }

   @Override
   protected void render() {
      float cx = this.boardW() / 2.0F;
      this.text(this.myScore + " : " + this.oppScore, cx, this.S(10.0F), this.S(14.0F), 3, -1);
      this.text("Best of 5", cx, this.S(28.0F), this.S(7.0F), 1, -1996488705);
      float bw2 = this.S(90.0F);
      float bh2 = this.S(90.0F);
      float gap = this.S(12.0F);
      float startX = cx - 1.5F * bw2 - gap;
      float by2 = this.S(60.0F);

      for (int i = 0; i < 3; i++) {
         float bx = startX + i * (bw2 + gap);
         boolean picked = this.myPick == i;
         int bg = picked ? 1157627903 : 301989887;
         this.box(bx, by2, bw2, bh2, this.S(12.0F), bg);
         if (picked) {
            this.box(bx, by2, bw2, bh2, this.S(12.0F), Style.accent & 1157627903);
         }

         this.text(NAMES[i], bx + bw2 / 2.0F, by2 + bh2 / 2.0F, this.S(10.0F), 3, picked ? Style.accent : -855638017);
      }

      if (this.revealed) {
         float ry = by2 + bh2 + this.S(16.0F);
         int res = this.myPick == this.oppPick ? 0 : (BEATS[this.myPick][0] == this.oppPick ? 1 : -1);
         int col = res == 1 ? -14756000 : (res == -1 ? -45730 : -15043);
         String txt = res == 1 ? "Du gewinnst!" : (res == -1 ? "Gegner gewinnt" : "Unentschieden");
         this.text(NAMES[this.myPick] + "  vs  " + NAMES[this.oppPick], cx, ry, this.S(9.0F), 3, col);
         this.text(txt, cx, ry + this.S(14.0F), this.S(7.5F), 2, col);
      } else if (this.myPick >= 0) {
         float ry = by2 + bh2 + this.S(16.0F);
         this.text(this.duel != null ? "Warte auf " + this.duel.oppName + " ..." : "Gegner wählt ...", cx, ry, this.S(8.0F), 2, -1996488705);
      }
   }
}
