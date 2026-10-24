package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;

public final class TicTacToe extends GameView {
   private static final float CELL = 80.0F;
   private static final float GX = 16.0F;
   private static final float GY = 16.0F;
   private static final float TILE = 74.0F;
   private static final float PX = 272.0F;
   private static final float PW = 94.0F;
   private static final int[][] LINES = new int[][]{{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}};
   private static final int NONE = 0;
   private static final int WIN = 1;
   private static final int DRAW = 2;
   private static final int LOSS = 3;
   private final int[] b = new int[9];
   private final float[] placed = new float[9];
   private final float[] hoverA = new float[9];
   private int turn = 1;
   private int result = 0;
   private int[] winLine;
   private float think;
   private float endT;
   private float winAt;
   private float clock;
   private float act1 = 1.0F;
   private float act2 = 0.0F;
   private boolean playerStarts = true;
   private int wins;
   private int draws;
   private int losses;
   private final Duels.Duel duel;
   private int round = -1;

   public TicTacToe() {
      super("tictactoe");
      this.duel = null;
   }

   public TicTacToe(Duels.Duel d) {
      super("tictactoe");
      this.duel = d;
      this.playerStarts = d.iStart;
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
      return 380.0F;
   }

   @Override
   protected float boardH() {
      return 266.0F;
   }

   @Override
   protected void reset() {
      for (int i = 0; i < 9; i++) {
         this.b[i] = 0;
         this.placed[i] = 0.0F;
      }

      this.winLine = null;
      this.result = 0;
      this.think = 0.0F;
      this.endT = 0.0F;
      this.turn = this.playerStarts ? 1 : 2;
   }

   @Override
   protected String overTitle() {
      if (this.duel != null && this.duel.oppLeft && this.result == 0) {
         return this.duel.oppName + " ist gegangen";
      } else {
         return this.result == 1 ? "Gewonnen!" : (this.result == 2 ? "Unentschieden" : "Verloren");
      }
   }

   @Override
   protected void update(float dt) {
      if (this.result != 0) {
         this.endT += dt;
         if (this.endT > 1.15F) {
            this.gameOver(this.duel == null);
         }
      } else if (this.duel != null) {
         if (this.duel.oppLeft) {
            this.gameOver(false);
         } else {
            if (this.turn == 2) {
               Integer m = this.duel.take(this.round);
               if (m != null && m >= 0 && m < 9 && this.b[m] == 0) {
                  this.place(m, 2);
               }
            }
         }
      } else {
         if (this.turn == 2) {
            this.think += dt;
            if (this.think >= 0.35F) {
               this.place(this.aiMove(), 2);
            }
         }
      }
   }

   @Override
   protected void onClick(float x, float y, int button) {
      if (button == 0 && this.turn == 1 && this.result == 0) {
         int i = this.cellAt(x, y);
         if (i >= 0 && this.b[i] == 0) {
            if (this.duel != null) {
               this.duel.send(this.round, i);
            }

            this.place(i, 1);
         }
      }
   }

   private int cellAt(float x, float y) {
      int c = (int)Math.floor((x - 16.0F) / 80.0F);
      int r = (int)Math.floor((y - 16.0F) / 80.0F);
      return c >= 0 && r >= 0 && c <= 2 && r <= 2 ? r * 3 + c : -1;
   }

   private void place(int i, int who) {
      if (i >= 0 && this.b[i] == 0) {
         this.b[i] = who;
         this.placed[i] = this.clock;
         Sound.play("minecraft:block.note_block.pling", who == 1 ? 1.5F : 1.05F, 0.3F);
         int[] l = findWin(this.b);
         if (l != null) {
            this.winLine = l;
            this.winAt = this.clock + (who == 1 ? 0.3F : 0.4F);
            this.result = who == 1 ? 1 : 3;
            if (who == 1) {
               this.wins++;
               this.score = 3L;
               Sound.play("minecraft:entity.player.levelup", 1.3F, 0.35F);
            } else {
               this.losses++;
               this.score = 0L;
            }

            this.playerStarts = !this.playerStarts;
         } else if (full(this.b)) {
            this.result = 2;
            this.draws++;
            this.score = 1L;
            this.playerStarts = !this.playerStarts;
            Sound.play("minecraft:block.note_block.chime", 0.9F, 0.3F);
         } else {
            this.turn = 3 - who;
            this.think = 0.0F;
         }
      }
   }

   private static int[] findWin(int[] s) {
      for (int[] l : LINES) {
         if (s[l[0]] != 0 && s[l[0]] == s[l[1]] && s[l[1]] == s[l[2]]) {
            return l;
         }
      }

      return null;
   }

   private static boolean full(int[] s) {
      for (int v : s) {
         if (v == 0) {
            return false;
         }
      }

      return true;
   }

   private int aiMove() {
      List<Integer> empty = new ArrayList<>();

      for (int i = 0; i < 9; i++) {
         if (this.b[i] == 0) {
            empty.add(i);
         }
      }

      if (empty.isEmpty()) {
         return -1;
      } else {
         float slip = new float[]{0.55F, 0.25F, 0.08F, 0.0F}[this.aiLevel()];
         if (this.rnd.nextFloat() < slip) {
            return empty.get(this.rnd.nextInt(empty.size()));
         } else {
            int best = Integer.MIN_VALUE;
            List<Integer> bestMoves = new ArrayList<>();

            for (int ix : empty) {
               this.b[ix] = 2;
               int s = this.minimax(false, 1);
               this.b[ix] = 0;
               if (s > best) {
                  best = s;
                  bestMoves.clear();
                  bestMoves.add(ix);
               } else if (s == best) {
                  bestMoves.add(ix);
               }
            }

            return bestMoves.get(this.rnd.nextInt(bestMoves.size()));
         }
      }
   }

   private int minimax(boolean aiTurn, int depth) {
      int[] w = findWin(this.b);
      if (w != null) {
         return this.b[w[0]] == 2 ? 10 - depth : depth - 10;
      } else if (full(this.b)) {
         return 0;
      } else {
         int best = aiTurn ? Integer.MIN_VALUE : Integer.MAX_VALUE;

         for (int i = 0; i < 9; i++) {
            if (this.b[i] == 0) {
               this.b[i] = aiTurn ? 2 : 1;
               int s = this.minimax(!aiTurn, depth + 1);
               this.b[i] = 0;
               best = aiTurn ? Math.max(best, s) : Math.min(best, s);
            }
         }

         return best;
      }
   }

   private static int oColor() {
      int a = Style.accent;
      int dr = (a >> 16 & 0xFF) - 255;
      int dg = (a >> 8 & 0xFF) - 176;
      int db = (a & 0xFF) - 46;
      return dr * dr + dg * dg + db * db < 22500 ? -12604929 : -20434;
   }

   private int pieceColor(int who) {
      return who == 1 ? Style.accent : oColor();
   }

   private void seg(float x1, float y1, float x2, float y2, float th, int col) {
      float px1 = this.bx + x1 * this.u;
      float py1 = this.by + y1 * this.u;
      float px2 = this.bx + x2 * this.u;
      float py2 = this.by + y2 * this.u;
      float len = (float)Math.hypot(px2 - px1, py2 - py1);
      int r = Math.max(1, Math.round(th * this.u / 2.0F));
      int n = Math.max(1, (int)Math.ceil(len / Math.max(1.0F, r * 0.3F)));

      for (int i = 0; i <= n; i++) {
         float t = (float)i / n;
         Gx.circle(Math.round(px1 + (px2 - px1) * t), Math.round(py1 + (py2 - py1) * t), r, col);
      }
   }

   private void drawX(float cx, float cy, float arm, float th, float t, int col) {
      float p1 = Ease.outCubic(t / 0.16F);
      float p2 = Ease.outCubic((t - 0.13F) / 0.16F);
      if (p1 > 0.0F) {
         this.seg(cx - arm, cy - arm, cx - arm + 2.0F * arm * p1, cy - arm + 2.0F * arm * p1, th, col);
      }

      if (p2 > 0.0F) {
         this.seg(cx + arm, cy - arm, cx + arm - 2.0F * arm * p2, cy - arm + 2.0F * arm * p2, th, col);
      }
   }

   private void drawO(float cx, float cy, float rad, float th, float t, int col) {
      float p = Ease.outCubic(t / 0.3F);
      if (!(p <= 0.0F)) {
         if (p >= 1.0F) {
            Gx.ringAt(this.X(cx), this.Y(cy), this.S(rad + th / 2.0F), this.S(th), col);
         } else {
            int r = Math.max(1, Math.round(th * this.u / 2.0F));
            float arc = (float)((Math.PI * 2) * p);
            int n = Math.max(2, (int)Math.ceil(arc * rad * this.u / Math.max(1.0F, r * 0.3F)));

            for (int i = 0; i <= n; i++) {
               double a = (-Math.PI / 2) + arc * i / n;
               Gx.circle(Math.round(this.bx + (cx + (float)Math.cos(a) * rad) * this.u), Math.round(this.by + (cy + (float)Math.sin(a) * rad) * this.u), r, col);
            }
         }
      }
   }

   private void glyph(int who, float cx, float cy, float size, float t, int col) {
      if (who == 1) {
         this.drawX(cx, cy, size, size * 0.38F, t, col);
      } else {
         this.drawO(cx, cy, size * 0.95F, size * 0.38F, t, col);
      }
   }

   @Override
   protected void render() {
      if (this.duel != null && this.state == GameView.State.READY) {
         this.start();
      }

      this.clock = this.clock + Gx.dt;
      float k = 1.0F - (float)Math.exp(-Gx.dt * 14.0F);
      float hx = (this.mx - this.bx) / this.u;
      float hy = (this.my - this.by) / this.u;
      int hover = this.state == GameView.State.PLAYING && this.turn == 1 && this.result == 0 ? this.cellAt(hx, hy) : -1;
      if (hover >= 0 && this.b[hover] != 0) {
         hover = -1;
      }

      int tile = Style.light ? -1 : -15263456;
      boolean winShown = this.winLine != null && this.clock >= this.winAt;
      int winCol = this.winLine != null ? this.pieceColor(this.b[this.winLine[0]]) : Style.accent;
      float wp = this.winLine != null ? Ease.outCubic((this.clock - this.winAt) / 0.3F) : 0.0F;
      this.box(11.0F, 11.0F, 244.0F, 244.0F, 16.0F, Style.light ? 218103808 : 201326591);

      for (int i = 0; i < 9; i++) {
         this.hoverA[i] = this.hoverA[i] + ((i == hover ? 1 : 0) - this.hoverA[i]) * k;
         float x = 16.0F + i % 3 * 80.0F + 3.0F - 3.0F;
         float y = 16.0F + i / 3 * 80.0F + 3.0F - 3.0F;
         float lift = 1.5F * this.hoverA[i];
         boolean inWin = winShown && (this.winLine[0] == i || this.winLine[1] == i || this.winLine[2] == i);
         int col = Gx.mix(tile, Style.accent, 0.1F * this.hoverA[i]);
         if (inWin) {
            col = Gx.mix(tile, winCol, 0.2F * wp);
         }

         this.box(x, y + 2.2F, 74.0F, 74.0F, 13.0F, Style.light ? 369098752 : 1426063360);
         this.boxV(x, y - lift, 74.0F, 74.0F, 13.0F, Gx.mix(col, -1, Style.light ? 0.0F : 0.05F), col);
         Gx.outline(
            this.X(x),
            this.Y(y - lift),
            this.S(74.0F),
            this.S(74.0F),
            this.S(13.0F),
            Math.max(1, this.S(0.6F)),
            inWin ? Gx.withAlpha(winCol, 0.6F * wp) : Gx.mix(Style.stroke, Gx.withAlpha(Style.accent, 0.5F), this.hoverA[i])
         );
         float cx = x + 37.0F;
         float cy = y + 37.0F - lift;
         if (this.b[i] != 0) {
            int pc = this.pieceColor(this.b[i]);
            float t = this.clock - this.placed[i];
            Gx.glow(this.X(cx), this.Y(cy), this.S(40.0F), Gx.withAlpha(pc, (inWin ? 0.32F : 0.1F) * Ease.outCubic(t / 0.3F)));
            this.glyph(this.b[i], cx, cy, 20.0F, t, pc);
         } else if (this.hoverA[i] > 0.02F) {
            this.glyph(1, cx, cy, 20.0F, 1.0F, Gx.mix(col, Style.accent, 0.32F * this.hoverA[i]));
         }
      }

      if (winShown) {
         float x1 = 16.0F + this.winLine[0] % 3 * 80.0F + 40.0F - 3.0F;
         float y1 = 16.0F + this.winLine[0] / 3 * 80.0F + 40.0F - 3.0F;
         float x2 = 16.0F + this.winLine[2] % 3 * 80.0F + 40.0F - 3.0F;
         float y2 = 16.0F + this.winLine[2] / 3 * 80.0F + 40.0F - 3.0F;
         float dx = x2 - x1;
         float dy = y2 - y1;
         float len = (float)Math.hypot(dx, dy);
         float ex = dx / len * 80.0F * 0.36F;
         float ey = dy / len * 80.0F * 0.36F;
         float sx = x1 - ex;
         float sy = y1 - ey;
         float tx = x2 + ex;
         float ty = y2 + ey;
         float lx = sx + (tx - sx) * wp;
         float ly = sy + (ty - sy) * wp;

         for (int i = 0; i <= 6; i++) {
            float f = i / 6.0F * wp;
            Gx.glow(this.X(sx + (tx - sx) * f), this.Y(sy + (ty - sy) * f), this.S(26.0F), Gx.withAlpha(winCol, 0.22F));
         }

         this.seg(sx, sy, lx, ly, 7.5F, Gx.mix(winCol, -16777216, 0.35F));
         this.seg(sx, sy, lx, ly, 5.0F, Gx.mix(winCol, -1, 0.45F));
      }

      this.act1 = this.act1 + ((this.turn == 1 && this.result == 0 ? 1 : 0) - this.act1) * k;
      this.act2 = this.act2 + ((this.turn == 2 && this.result == 0 ? 1 : 0) - this.act2) * k;
      this.playerCard(16.0F, "Du", "Spieler", 1, this.act1);
      this.playerCard(82.0F, this.duel == null ? "KI" : this.duel.oppName, this.duel == null ? "Computer" : "Online", 2, this.act2);
      int sc = Style.text;
      String status;
      if (this.result == 1) {
         status = "Gewonnen!";
         sc = Style.ok;
      } else if (this.result == 3) {
         status = "Verloren";
         sc = Style.danger;
      } else if (this.result == 2) {
         status = "Unentschieden";
         sc = Style.sub;
      } else if (this.turn == 2) {
         status = (this.duel == null ? "KI denkt" : this.duel.oppName + " ist dran") + ".".repeat(1 + (int)(this.clock * 4.0F) % 3);
      } else {
         status = "Du bist dran";
      }

      float sy = 166.0F;
      this.text(status, 319.0F, sy, 9.5F, 3, sc);
      this.text(this.result == 0 ? (this.turn == 1 ? "Setze dein X" : "Bitte warten") : "Runde beendet", 319.0F, sy + 14.0F, 6.8F, 1, Style.sub);
      float ty = 196.0F;
      this.box(272.0F, ty, 94.0F, 60.0F, 12.0F, Style.surface);
      this.tally(287.66666F, ty, this.wins, "Siege", Style.ok);
      this.tally(319.0F, ty, this.draws, "Remis", Style.sub);
      this.tally(350.33334F, ty, this.losses, "Niederl.", Style.danger);
   }

   private void playerCard(float y, String name, String sub, int who, float act) {
      int pc = this.pieceColor(who);
      float h = 58.0F;
      if (act > 0.02F) {
         Gx.glow(this.X(319.0F), this.Y(y + h / 2.0F), this.S(58.0F), Gx.withAlpha(pc, 0.16F * act));
      }

      this.box(272.0F, y, 94.0F, h, 12.0F, Gx.mix(Style.surface, pc, 0.1F * act));
      Gx.outline(
         this.X(272.0F), this.Y(y), this.S(94.0F), this.S(h), this.S(12.0F), Math.max(1, this.S(0.8F)), Gx.mix(Style.stroke, Gx.withAlpha(pc, 0.85F), act)
      );
      this.box(280.0F, y + 13.0F, 32.0F, 32.0F, 9.0F, Gx.withAlpha(pc, 0.14F));
      this.glyph(who, 296.0F, y + 29.0F, 8.0F, 1.0F, pc);
      Gx.textMid(Fonts.ellipsize(name, 3, 10.0F * this.u, this.S(42.0F)), this.X(320.0F), this.Y(y + 23.0F), 10.0F * this.u, 3, Style.text);
      Gx.textMid(sub, this.X(320.0F), this.Y(y + 37.0F), 6.4F * this.u, 1, Style.sub);
      if (act > 0.02F) {
         float pulse = 0.6F + 0.4F * (float)Math.sin(this.clock * 6.0F);
         this.circle(356.0F, y + 10.0F, 2.6F, Gx.withAlpha(pc, act * pulse));
      }
   }

   private void tally(float cx, float y, int n, String label, int col) {
      this.text(String.valueOf(n), cx, y + 24.0F, 13.0F, 3, Style.text);
      this.text(label, cx, y + 43.0F, 5.6F, 2, col);
   }
}
