package dev.lego.games;

import dev.lego.ui.Fonts;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.List;

final class Ladders extends GameView {
   private static final int[][] JUMPS = new int[][]{
      {3, 22}, {8, 30}, {20, 41}, {28, 76}, {50, 67}, {71, 92}, {80, 99}, {17, 4}, {54, 34}, {62, 19}, {64, 60}, {87, 24}, {93, 73}, {95, 75}, {98, 79}
   };
   private static final float C = 26.0F;
   private static final float OX = 8.0F;
   private static final float OY = 8.0F;
   private int me;
   private int ai;
   private int turn;
   private int die;
   private int lastWinner;
   private float anim;
   private float think;
   private String msg = "";

   Ladders() {
      super("ladders");
   }

   @Override
   protected float boardW() {
      return 410.0F;
   }

   @Override
   protected float boardH() {
      return 276.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Siege";
   }

   @Override
   protected String overTitle() {
      return this.lastWinner == 1 ? "Gewonnen!" : "Die KI war schneller";
   }

   @Override
   protected void reset() {
      this.me = this.ai = 0;
      this.turn = 1;
      this.die = 0;
      this.msg = "Klick auf den Würfel";
   }

   private float[] pos(int sq) {
      if (sq <= 0) {
         return new float[]{-4.0F, 255.0F};
      } else {
         int i = sq - 1;
         int row = i / 10;
         int col = row % 2 == 0 ? i % 10 : 9 - i % 10;
         return new float[]{8.0F + col * 26.0F + 13.0F, 8.0F + (9 - row) * 26.0F + 13.0F};
      }
   }

   private int move(int p, int d, String who) {
      int n = p + d;
      if (n > 100) {
         n = p;
      }

      for (int[] j : JUMPS) {
         if (j[0] == n) {
            this.msg = who + (j[1] > n ? " klettert die Leiter hoch!" : " rutscht die Schlange runter!");
            n = j[1];
            Sound.play(j[1] > j[0] ? "minecraft:entity.player.levelup" : "minecraft:block.note_block.bass", 1.2F, 0.4F);
         }
      }

      return n;
   }

   private void roll() {
      this.die = 1 + this.rnd.nextInt(6);
      this.anim = 0.4F;
      Sound.play("minecraft:block.wood.hit", 1.6F, 0.35F);
      this.msg = "";
      if (this.turn == 1) {
         this.me = this.move(this.me, this.die, "Du");
         if (this.me == 100) {
            this.lastWinner = 1;
            this.score++;
            PackPuzzle.win();
            this.gameOver();
            return;
         }
      } else {
         this.ai = this.move(this.ai, this.die, "KI");
         if (this.ai == 100) {
            this.lastWinner = 2;
            PackPuzzle.bad();
            this.gameOver();
            return;
         }
      }

      if (this.die != 6) {
         this.turn = 3 - this.turn;
      }

      this.think = 0.0F;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (this.turn == 1 && this.anim <= 0.0F && in(x, y, 300, 40, 60, 60)) {
         this.roll();
      }
   }

   @Override
   protected void onKey(int k) {
      if (this.turn == 1 && this.anim <= 0.0F && k == 32) {
         this.roll();
      }
   }

   @Override
   protected void update(float dt) {
      this.anim = Math.max(0.0F, this.anim - dt);
      if (this.turn == 2 && this.anim <= 0.0F && (this.think += dt) > 0.8F) {
         this.roll();
      }
   }

   @Override
   protected void render() {
      for (int sq = 1; sq <= 100; sq++) {
         float[] p = this.pos(sq);
         this.box(p[0] - 13.0F, p[1] - 13.0F, 26.0F, 26.0F, 0.0F, (sq + (sq - 1) / 10) % 2 == 0 ? -858424 : -2044000);
         this.text(String.valueOf(sq), p[0] - 6.0F, p[1] - 7.0F, 4.5F, 2, -1722138592);
      }

      for (int[] j : JUMPS) {
         float[] a = this.pos(j[0]);
         float[] b2 = this.pos(j[1]);
         this.line(a[0], a[1], b2[0], b2[1], 3.0F, j[1] > j[0] ? -863348176 : -869360054);
         this.circle(b2[0], b2[1], 2.5F, j[1] > j[0] ? -7710160 : -13722038);
      }

      float[] pm = this.pos(this.me);
      float[] pa = this.pos(this.ai);
      this.circle(pm[0] - 4.0F, pm[1], 6.0F, -16749385);
      this.circle(pa[0] + 4.0F, pa[1], 6.0F, -1900533);
      this.box(300.0F, 40.0F, 60.0F, 60.0F, 10.0F, -460552);
      int v = this.anim > 0.0F ? 1 + this.rnd.nextInt(6) : Math.max(1, this.die);
      int[][] pip = new int[][]{new int[0], {4}, {0, 8}, {0, 4, 8}, {0, 2, 6, 8}, {0, 2, 4, 6, 8}, {0, 2, 3, 5, 6, 8}};

      for (int p : pip[v]) {
         this.circle(315 + p % 3 * 15, 55 + p / 3 * 15, 4.5F, -15395302);
      }

      this.text(this.turn == 1 ? "Du würfelst" : "KI würfelt", 330.0F, 120.0F, 8.0F, 3, this.turn == 1 ? -16749385 : -1900533);
      this.text("Du: " + this.me + "   KI: " + this.ai, 330.0F, 140.0F, 7.0F, 3, Style.text);
      List<String> l = Fonts.wrap(this.msg, 2, 6.4F * this.u, this.S(130.0F), 3);
      float y = 170.0F;

      for (String s : l) {
         this.text(s, 330.0F, y, 6.4F, 2, Style.sub);
         y += 11.0F;
      }

      this.text("6 = nochmal würfeln", 330.0F, 250.0F, 6.0F, 2, Style.muted);
   }
}
