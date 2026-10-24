package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ConnectFour extends GameView {
   private static final int COLS = 7;
   private static final int ROWS = 6;
   private static final float C = 38.0F;
   private static final float GX = 14.0F;
   private static final float GY = 50.0F;
   private static final float TOPY = 25.0F;
   private static final float PR = 15.0F;
   private static final float PX = 300.0F;
   private static final float PW = 88.0F;
   private static final int YEL = -13053;
   private static final int RED = -1900533;
   private static final int BLUE = -16098364;
   private static final int[] ORDER = new int[]{3, 2, 4, 1, 5, 0, 6};
   private static final int NONE = 0;
   private static final int WIN = 1;
   private static final int DRAW = 2;
   private static final int LOSS = 3;
   private static final int WIN_SCORE = 1000000;
   private final int[][] g = new int[6][7];
   private final float[] colHover = new float[7];
   private int turn = 1;
   private int result = 0;
   private boolean playerStarts = true;
   private float think;
   private float endT;
   private float clock;
   private float winAt;
   private List<int[]> win4;
   private int fr = -1;
   private int fc = -1;
   private int fwho;
   private int bounces;
   private float fy;
   private float fvy;
   private float previewX = -1.0F;
   private float act1 = 1.0F;
   private float act2 = 0.0F;
   private int wins;
   private int draws;
   private int losses;
   private final Duels.Duel duel;
   private int round = -1;

   private int depth() {
      return new int[]{1, 3, 5, 7}[this.aiLevel()];
   }

   public ConnectFour() {
      super("connect4");
      this.duel = null;
   }

   public ConnectFour(Duels.Duel d) {
      super("connect4");
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

   private String oppName() {
      return this.duel == null ? "KI" : this.duel.oppName;
   }

   @Override
   protected boolean hasAi() {
      return this.duel == null;
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 294.0F;
   }

   @Override
   protected void reset() {
      for (int[] r : this.g) {
         Arrays.fill(r, 0);
      }

      this.turn = this.playerStarts ? 1 : 2;
      this.result = 0;
      this.think = 0.0F;
      this.endT = 0.0F;
      this.win4 = null;
      this.fr = this.fc = -1;
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
      if (this.fr < 0) {
         if (this.result != 0) {
            this.endT += dt;
            if (this.endT > 1.3F) {
               this.gameOver(this.duel == null);
            }
         } else if (this.duel != null) {
            if (this.duel.oppLeft) {
               this.gameOver(false);
            } else {
               if (this.turn == 2) {
                  Integer m = this.duel.take(this.round);
                  if (m != null && m >= 0 && m < 7 && openRow(this.g, m) >= 0) {
                     this.drop(m, 2);
                  }
               }
            }
         } else {
            if (this.turn == 2) {
               this.think += dt;
               if (this.think >= 0.45F) {
                  this.drop(this.aiMove(), 2);
               }
            }
         }
      } else {
         this.fvy += 1700.0F * dt;
         this.fy = this.fy + this.fvy * dt;
         float ty = 50.0F + this.fr * 38.0F + 19.0F;
         if (this.fy >= ty) {
            this.fy = ty;
            if (this.bounces < 2 && this.fvy > 160.0F) {
               if (this.bounces == 0) {
                  Sound.play("minecraft:block.stone.place", 1.5F + this.rnd.nextFloat() * 0.2F, 0.35F);
               }

               this.fvy = -this.fvy * 0.24F;
               this.bounces++;
            } else {
               this.land();
            }
         }
      }
   }

   @Override
   protected void onClick(float x, float y, int button) {
      if (button == 0) {
         int c = this.colAt(x, y);
         if (c >= 0) {
            this.playerDrop(c);
         }
      }
   }

   @Override
   protected void onKey(int key) {
      if (key >= 49 && key <= 55) {
         this.playerDrop(key - 49);
      } else if (key >= 321 && key <= 327) {
         this.playerDrop(key - 321);
      }
   }

   private void playerDrop(int c) {
      if (this.turn == 1 && this.result == 0 && this.fr < 0 && c >= 0 && c < 7 && openRow(this.g, c) >= 0) {
         if (this.duel != null) {
            this.duel.send(this.round, c);
         }

         this.drop(c, 1);
      }
   }

   private int colAt(float x, float y) {
      if (!(y < 0.0F) && !(y > 284.0F)) {
         int c = (int)Math.floor((x - 14.0F) / 38.0F);
         return c >= 0 && c < 7 ? c : -1;
      } else {
         return -1;
      }
   }

   private static int openRow(int[][] b, int c) {
      for (int r = 5; r >= 0; r--) {
         if (b[r][c] == 0) {
            return r;
         }
      }

      return -1;
   }

   private void drop(int c, int who) {
      if (c >= 0) {
         int r = openRow(this.g, c);
         if (r >= 0) {
            this.g[r][c] = who;
            this.fr = r;
            this.fc = c;
            this.fwho = who;
            this.fy = 25.0F;
            this.fvy = 90.0F;
            this.bounces = 0;
            this.turn = 3 - who;
            this.think = 0.0F;
            Sound.play("minecraft:ui.button.click", who == 1 ? 1.4F : 1.2F, 0.15F);
         }
      }
   }

   private void land() {
      int r = this.fr;
      int c = this.fc;
      int who = this.fwho;
      this.fr = this.fc = -1;
      List<int[]> w = line(this.g, r, c, who);
      if (w != null) {
         this.win4 = w;
         this.winAt = this.clock;
         this.result = who == 1 ? 1 : 3;
         if (who == 1) {
            this.wins++;
            this.score = 10 + this.empty() / 2;
            Sound.play("minecraft:entity.player.levelup", 1.3F, 0.35F);
         } else {
            this.losses++;
            this.score = 0L;
         }

         this.playerStarts = !this.playerStarts;
      } else if (this.empty() == 0) {
         this.result = 2;
         this.draws++;
         this.score = 3L;
         this.playerStarts = !this.playerStarts;
      }
   }

   private int empty() {
      int n = 0;

      for (int[] row : this.g) {
         for (int v : row) {
            if (v == 0) {
               n++;
            }
         }
      }

      return n;
   }

   private static List<int[]> line(int[][] b, int r, int c, int who) {
      int[][] dirs = new int[][]{{0, 1}, {1, 0}, {1, 1}, {1, -1}};

      for (int[] d : dirs) {
         List<int[]> l = new ArrayList<>();
         l.add(new int[]{r, c});

         for (int s = -1; s <= 1; s += 2) {
            int rr = r + d[0] * s;

            for (int cc = c + d[1] * s; rr >= 0 && rr < 6 && cc >= 0 && cc < 7 && b[rr][cc] == who; cc += d[1] * s) {
               l.add(new int[]{rr, cc});
               rr += d[0] * s;
            }
         }

         if (l.size() >= 4) {
            return l;
         }
      }

      return null;
   }

   private static boolean wins(int[][] b, int r, int c, int who) {
      int[][] dirs = new int[][]{{0, 1}, {1, 0}, {1, 1}, {1, -1}};

      for (int[] d : dirs) {
         int n = 1;

         for (int s = -1; s <= 1; s += 2) {
            int rr = r + d[0] * s;

            for (int cc = c + d[1] * s; rr >= 0 && rr < 6 && cc >= 0 && cc < 7 && b[rr][cc] == who; cc += d[1] * s) {
               n++;
               rr += d[0] * s;
            }
         }

         if (n >= 4) {
            return true;
         }
      }

      return false;
   }

   private int aiMove() {
      if (this.aiLevel() == 0 && this.rnd.nextFloat() < 0.3F) {
         List<Integer> open = new ArrayList<>();

         for (int c = 0; c < 7; c++) {
            if (openRow(this.g, c) >= 0) {
               open.add(c);
            }
         }

         if (!open.isEmpty()) {
            return open.get(this.rnd.nextInt(open.size()));
         }
      }

      int[][] b = new int[6][];

      for (int r = 0; r < 6; r++) {
         b[r] = (int[])this.g[r].clone();
      }

      int best = Integer.MIN_VALUE;
      List<Integer> moves = new ArrayList<>();

      for (int cx : ORDER) {
         int r = openRow(b, cx);
         if (r >= 0) {
            b[r][cx] = 2;
            int s = wins(b, r, cx, 2) ? 1000000 + this.depth() : -this.negamax(b, this.depth() - 1, -2147483647, Integer.MAX_VALUE, 1);
            b[r][cx] = 0;
            if (s > best) {
               best = s;
               moves.clear();
               moves.add(cx);
            } else if (s == best) {
               moves.add(cx);
            }
         }
      }

      return moves.isEmpty() ? -1 : moves.get(this.rnd.nextInt(moves.size()));
   }

   private int negamax(int[][] b, int depth, int alpha, int beta, int who) {
      boolean any = false;

      for (int c = 0; c < 7; c++) {
         if (b[0][c] == 0) {
            any = true;
            break;
         }
      }

      if (!any) {
         return 0;
      } else if (depth == 0) {
         int e = eval(b);
         return who == 2 ? e : -e;
      } else {
         int best = -2147483647;

         for (int cx : ORDER) {
            int r = openRow(b, cx);
            if (r >= 0) {
               b[r][cx] = who;
               int s = wins(b, r, cx, who) ? 1000000 + depth : -this.negamax(b, depth - 1, -beta, -alpha, 3 - who);
               b[r][cx] = 0;
               if (s > best) {
                  best = s;
               }

               if (s > alpha) {
                  alpha = s;
               }

               if (alpha >= beta) {
                  break;
               }
            }
         }

         return best;
      }
   }

   private static int eval(int[][] b) {
      int s = 0;

      for (int r = 0; r < 6; r++) {
         if (b[r][3] == 2) {
            s += 3;
         } else if (b[r][3] == 1) {
            s -= 3;
         }
      }

      int[][] dirs = new int[][]{{0, 1}, {1, 0}, {1, 1}, {1, -1}};

      for (int rx = 0; rx < 6; rx++) {
         for (int c = 0; c < 7; c++) {
            for (int[] d : dirs) {
               int er = rx + d[0] * 3;
               int ec = c + d[1] * 3;
               if (er >= 0 && er < 6 && ec >= 0 && ec < 7) {
                  int a = 0;
                  int p = 0;

                  for (int k = 0; k < 4; k++) {
                     int v = b[rx + d[0] * k][c + d[1] * k];
                     if (v == 2) {
                        a++;
                     } else if (v == 1) {
                        p++;
                     }
                  }

                  if (a <= 0 || p <= 0) {
                     if (a == 3) {
                        s += 6;
                     } else if (a == 2) {
                        s += 2;
                     }

                     if (p == 3) {
                        s -= 7;
                     } else if (p == 2) {
                        s -= 2;
                     }
                  }
               }
            }
         }
      }

      return s;
   }

   private void piece(float cx, float cy, int col, float scale, float dim) {
      if (dim > 0.0F) {
         col = Gx.mix(col, -16777216, dim);
      }

      float r = 15.0F * scale;
      this.circle(cx, cy + 1.4F * scale, r, Gx.mix(col, -16777216, 0.5F));
      this.circle(cx, cy, r, Gx.mix(col, -16777216, 0.08F));
      this.circle(cx, cy - 0.3F * scale, r * 0.86F, Gx.mix(col, -1, 0.1F));
      this.circle(cx, cy + 0.5F * scale, r * 0.52F, Gx.mix(col, -16777216, 0.16F));
      this.circle(cx, cy - 0.3F * scale, r * 0.47F, Gx.mix(col, -1, 0.2F));
      this.circle(cx - r * 0.16F, cy - r * 0.18F, r * 0.16F, Gx.withAlpha(-1, 0.55F * (1.0F - dim)));
   }

   private float cx(int c) {
      return 14.0F + c * 38.0F + 19.0F;
   }

   private float cy(int r) {
      return 50.0F + r * 38.0F + 19.0F;
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
      boolean myTurn = this.state == GameView.State.PLAYING && this.turn == 1 && this.result == 0 && this.fr < 0;
      int hc = myTurn ? this.colAt(hx, hy) : -1;
      if (hc >= 0 && openRow(this.g, hc) < 0) {
         hc = -1;
      }

      float wp = this.win4 != null ? Ease.outCubic((this.clock - this.winAt) / 0.4F) : 0.0F;

      for (int c = 0; c < 7; c++) {
         this.text(String.valueOf(c + 1), this.cx(c), 25.0F, 7.0F, 3, Gx.withAlpha(Style.sub, 0.35F * (1.0F - this.colHover[c])));
      }

      float fx = 8.0F;
      float fyy = 44.0F;
      float fw = 278.0F;
      float fh = 240.0F;
      Gx.shadow(this.X(fx), this.Y(fyy), this.S(fw), this.S(fh), this.S(14.0F), this.S(8.0F), 0.45F);
      this.boxV(fx, fyy, fw, fh, 14.0F, Gx.mix(-16098364, -1, 0.12F), Gx.mix(-16098364, -16777216, 0.2F));
      Gx.outline(this.X(fx), this.Y(fyy), this.S(fw), this.S(fh), this.S(14.0F), Math.max(1, this.S(0.8F)), Gx.withAlpha(-16777216, 0.35F));

      for (int c = 0; c < 7; c++) {
         this.colHover[c] = this.colHover[c] + ((c == hc ? 1 : 0) - this.colHover[c]) * k;
         if (this.colHover[c] > 0.01F) {
            this.box(14.0F + c * 38.0F + 2.0F, 47.0F, 34.0F, 234.0F, 12.0F, Gx.withAlpha(-1, 0.1F * this.colHover[c]));
         }
      }

      int hole = Style.light ? -15982000 : -16379606;

      for (int r = 0; r < 6; r++) {
         for (int cx = 0; cx < 7; cx++) {
            this.circle(this.cx(cx), this.cy(r) - 0.8F, 16.4F, Gx.mix(-16098364, -16777216, 0.45F));
            this.circle(this.cx(cx), this.cy(r), 15.6F, Gx.mix(-16098364, -1, 0.14F));
            this.circle(this.cx(cx), this.cy(r), 15.0F, hole);
         }
      }

      for (int r = 0; r < 6; r++) {
         for (int cx = 0; cx < 7; cx++) {
            if (this.g[r][cx] != 0 && (r != this.fr || cx != this.fc)) {
               boolean inWin = this.inWin(r, cx);
               float dim = this.win4 != null && !inWin ? 0.45F * wp : 0.0F;
               float sc = 1.0F;
               if (inWin) {
                  sc = 1.0F + 0.06F * (float)Math.sin((this.clock - this.winAt) * 7.0F) * wp;
                  Gx.glow(this.X(this.cx(cx)), this.Y(this.cy(r)), this.S(30.0F), Gx.withAlpha(-1, 0.3F * wp));
               }

               this.piece(this.cx(cx), this.cy(r), this.g[r][cx] == 1 ? -13053 : -1900533, sc, dim);
               if (inWin) {
                  Gx.ringAt(this.X(this.cx(cx)), this.Y(this.cy(r)), this.S(17.2F), Math.max(1, this.S(1.6F)), Gx.withAlpha(-1, 0.9F * wp));
               }
            }
         }
      }

      if (this.fr >= 0) {
         this.piece(this.cx(this.fc), this.fy, this.fwho == 1 ? -13053 : -1900533, 1.0F, 0.0F);
      }

      if (hc >= 0) {
         float tx = this.cx(hc);
         this.previewX = this.previewX < 0.0F ? tx : this.previewX + (tx - this.previewX) * (1.0F - (float)Math.exp(-Gx.dt * 22.0F));
         float bob = (float)Math.sin(this.clock * 5.0F) * 1.2F;
         Gx.glow(this.X(this.previewX), this.Y(25.0F), this.S(26.0F), Gx.withAlpha(-13053, 0.25F));
         this.piece(this.previewX, 25.0F + bob, -13053, 1.0F, 0.0F);
         int lr = openRow(this.g, hc);
         float pa = 0.45F + 0.25F * (float)Math.sin(this.clock * 6.0F);
         Gx.ringAt(this.X(tx), this.Y(this.cy(lr)), this.S(14.0F), Math.max(1, this.S(1.4F)), Gx.withAlpha(-13053, pa * this.colHover[hc]));
      } else {
         this.previewX = -1.0F;
      }

      this.act1 = this.act1 + ((this.turn == 1 && this.result == 0 ? 1 : 0) - this.act1) * k;
      this.act2 = this.act2 + ((this.turn == 2 && this.result == 0 ? 1 : 0) - this.act2) * k;
      this.playerCard(8.0F, "Du", "Gelb", -13053, this.act1);
      this.playerCard(72.0F, this.oppName(), this.duel == null ? "Rot" : "Online · Rot", -1900533, this.act2);
      int scx = Style.text;
      String status;
      if (this.result == 1) {
         status = "Gewonnen!";
         scx = Style.ok;
      } else if (this.result == 3) {
         status = "Verloren";
         scx = Style.danger;
      } else if (this.result == 2) {
         status = "Unentschieden";
         scx = Style.sub;
      } else if (this.turn == 2) {
         status = (this.duel == null ? "KI denkt" : this.duel.oppName + " ist dran") + ".".repeat(1 + (int)(this.clock * 4.0F) % 3);
      } else {
         status = "Du bist dran";
      }

      float sy = 152.0F;
      this.text(status, 344.0F, sy, 9.5F, 3, scx);
      this.text(this.result == 0 ? "Freie Felder: " + this.empty() : "Runde beendet", 344.0F, sy + 14.0F, 6.6F, 1, Style.sub);
      float ty = 180.0F;
      this.box(300.0F, ty, 88.0F, 60.0F, 12.0F, Style.surface);
      this.tally(314.66666F, ty, this.wins, "Siege", Style.ok);
      this.tally(344.0F, ty, this.draws, "Remis", Style.sub);
      this.tally(373.33334F, ty, this.losses, "Niederl.", Style.danger);
      this.text("Tasten 1–7 werfen", 344.0F, ty + 74.0F, 6.2F, 2, Style.muted);
   }

   private boolean inWin(int r, int c) {
      if (this.win4 == null) {
         return false;
      } else {
         for (int[] p : this.win4) {
            if (p[0] == r && p[1] == c) {
               return true;
            }
         }

         return false;
      }
   }

   private void playerCard(float y, String name, String sub, int pc, float act) {
      float h = 56.0F;
      if (act > 0.02F) {
         Gx.glow(this.X(344.0F), this.Y(y + h / 2.0F), this.S(56.0F), Gx.withAlpha(pc, 0.14F * act));
      }

      this.box(300.0F, y, 88.0F, h, 12.0F, Gx.mix(Style.surface, pc, 0.09F * act));
      Gx.outline(
         this.X(300.0F), this.Y(y), this.S(88.0F), this.S(h), this.S(12.0F), Math.max(1, this.S(0.8F)), Gx.mix(Style.stroke, Gx.withAlpha(pc, 0.85F), act)
      );
      this.piece(324.0F, y + h / 2.0F, pc, 0.85F, 0.0F);
      Gx.textMid(Fonts.ellipsize(name, 3, 10.0F * this.u, this.S(38.0F)), this.X(346.0F), this.Y(y + 22.0F), 10.0F * this.u, 3, Style.text);
      Gx.textMid(sub, this.X(346.0F), this.Y(y + 36.0F), 6.4F * this.u, 1, Style.sub);
      if (act > 0.02F) {
         float pulse = 0.6F + 0.4F * (float)Math.sin(this.clock * 6.0F);
         this.circle(378.0F, y + 10.0F, 2.6F, Gx.withAlpha(pc, act * pulse));
      }
   }

   private void tally(float cx, float y, int n, String label, int col) {
      this.text(String.valueOf(n), cx, y + 24.0F, 13.0F, 3, Style.text);
      this.text(label, cx, y + 43.0F, 5.4F, 2, col);
   }
}
