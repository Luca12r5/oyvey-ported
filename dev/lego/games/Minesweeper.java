package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class Minesweeper extends GameView {
   private static final int COLS = 12;
   private static final int ROWS = 10;
   private static final int MINES = 18;
   private static final int N = 120;
   private static final float CELL = 26.0F;
   private static final float GX = 20.0F;
   private static final float GY = 42.0F;
   private static final int[] NUM = new int[]{0, -11688961, -12723068, -44462, -5011201, -30107, -14235942, -2039584, -6381922};
   private static final int[] NUM_LIGHT = new int[]{0, -15374912, -13730510, -2937041, -12245088, -7525874, -16743537, -14606047, -10395295};
   private static final int[] DEBRIS = new int[]{-1900533, -13053, -16098364, -6249047, -30208, -9671064};
   private final boolean[] mine = new boolean[120];
   private final boolean[] open = new boolean[120];
   private final boolean[] flag = new boolean[120];
   private final int[] adj = new int[120];
   private final float[] openAt = new float[120];
   private final float[] flagAt = new float[120];
   private boolean generated;
   private boolean lost;
   private boolean won;
   private int flags;
   private int opened;
   private int boom = -1;
   private float startT;
   private float endSecs;
   private float endT;
   private float clock;
   private float boomAt;
   private final List<float[]> parts = new ArrayList<>();

   public Minesweeper() {
      super("minesweeper");
   }

   @Override
   protected float boardW() {
      return 352.0F;
   }

   @Override
   protected float boardH() {
      return 314.0F;
   }

   @Override
   protected void reset() {
      for (int i = 0; i < 120; i++) {
         this.mine[i] = this.open[i] = this.flag[i] = false;
         this.adj[i] = 0;
         this.openAt[i] = this.flagAt[i] = 0.0F;
      }

      this.generated = this.lost = this.won = false;
      this.flags = this.opened = 0;
      this.boom = -1;
      this.startT = this.endSecs = this.endT = 0.0F;
      this.parts.clear();
   }

   @Override
   protected String overTitle() {
      return this.won ? "Geschafft!" : "Verloren";
   }

   private float secs() {
      if (!this.generated) {
         return 0.0F;
      } else {
         return !this.won && !this.lost ? Math.max(0.0F, this.time - this.startT) : this.endSecs;
      }
   }

   @Override
   protected void update(float dt) {
      if (this.won) {
         this.endT += dt;
         if (this.endT > 1.3F) {
            this.gameOver();
         }
      } else if (this.lost) {
         this.endT += dt;
         if (this.endT > 1.8F) {
            this.gameOver(false);
         }
      }
   }

   private int cellAt(float x, float y) {
      int c = (int)Math.floor((x - 20.0F) / 26.0F);
      int r = (int)Math.floor((y - 42.0F) / 26.0F);
      return c >= 0 && r >= 0 && c < 12 && r < 10 ? r * 12 + c : -1;
   }

   private List<Integer> neighbours(int i) {
      List<Integer> l = new ArrayList<>(8);
      int c = i % 12;
      int r = i / 12;

      for (int dy = -1; dy <= 1; dy++) {
         for (int dx = -1; dx <= 1; dx++) {
            if (dx != 0 || dy != 0) {
               int cc = c + dx;
               int rr = r + dy;
               if (cc >= 0 && rr >= 0 && cc < 12 && rr < 10) {
                  l.add(rr * 12 + cc);
               }
            }
         }
      }

      return l;
   }

   private void generate(int safe) {
      List<Integer> forbidden = this.neighbours(safe);
      forbidden.add(safe);
      int placed = 0;

      while (placed < 18) {
         int i = this.rnd.nextInt(120);
         if (!this.mine[i] && !forbidden.contains(i)) {
            this.mine[i] = true;
            placed++;
         }
      }

      for (int i = 0; i < 120; i++) {
         int n = 0;

         for (int j : this.neighbours(i)) {
            if (this.mine[j]) {
               n++;
            }
         }

         this.adj[i] = n;
      }

      this.generated = true;
      this.startT = this.time;
   }

   @Override
   protected void onClick(float x, float y, int button) {
      if (!this.won && !this.lost) {
         int i = this.cellAt(x, y);
         if (i >= 0) {
            if (button == 1) {
               if (!this.open[i]) {
                  this.flag[i] = !this.flag[i];
                  this.flags = this.flags + (this.flag[i] ? 1 : -1);
                  this.flagAt[i] = this.clock;
                  Sound.play("minecraft:block.note_block.hat", this.flag[i] ? 1.6F : 1.2F, 0.35F);
               }
            } else if (!this.flag[i]) {
               if (!this.generated) {
                  this.generate(i);
               }

               if (this.open[i]) {
                  if (this.adj[i] != 0) {
                     int f = 0;

                     for (int j : this.neighbours(i)) {
                        if (this.flag[j]) {
                           f++;
                        }
                     }

                     if (f != this.adj[i]) {
                        Sound.play("minecraft:block.note_block.bass", 1.4F, 0.2F);
                     } else {
                        boolean any = false;

                        for (int jx : this.neighbours(i)) {
                           if (!this.flag[jx] && !this.open[jx]) {
                              if (this.mine[jx]) {
                                 this.explode(jx);
                                 return;
                              }

                              this.reveal(jx, false);
                              any = true;
                           }
                        }

                        if (any) {
                           Sound.play("minecraft:block.stone.break", 1.35F + this.rnd.nextFloat() * 0.2F, 0.25F);
                           this.checkWin();
                        }
                     }
                  }
               } else if (this.mine[i]) {
                  this.explode(i);
               } else {
                  int n = this.reveal(i, true);
                  Sound.play(
                     n > 6 ? "minecraft:block.stone.break" : "minecraft:block.stone.hit",
                     n > 6 ? 1.1F : 1.5F + this.rnd.nextFloat() * 0.2F,
                     n > 6 ? 0.3F : 0.4F
                  );
                  this.checkWin();
               }
            }
         }
      }
   }

   private int reveal(int start, boolean stagger) {
      ArrayDeque<int[]> q = new ArrayDeque<>();
      q.add(new int[]{start, 0});
      int count = 0;

      while (!q.isEmpty()) {
         int[] e = q.poll();
         int i = e[0];
         if (!this.open[i] && !this.flag[i] && !this.mine[i]) {
            this.open[i] = true;
            this.opened++;
            count++;
            this.openAt[i] = this.clock + (stagger ? Math.min(0.5F, e[1] * 0.035F) : 0.0F);
            if (this.adj[i] == 0) {
               for (int j : this.neighbours(i)) {
                  if (!this.open[j] && !this.flag[j]) {
                     q.add(new int[]{j, e[1] + 1});
                  }
               }
            }
         }
      }

      return count;
   }

   private void checkWin() {
      if (this.opened >= 102) {
         this.won = true;
         this.endSecs = Math.max(0.0F, this.time - this.startT);
         this.score = Math.max(1L, 1000L - (long)Math.floor(this.endSecs) * 5L);

         for (int i = 0; i < 120; i++) {
            if (this.mine[i] && !this.flag[i]) {
               this.flag[i] = true;
               this.flagAt[i] = this.clock + 0.15F + this.rnd.nextFloat() * 0.35F;
            }
         }

         this.flags = 18;
         Sound.play("minecraft:entity.player.levelup", 1.2F, 0.4F);
      }
   }

   private void explode(int i) {
      this.lost = true;
      this.boom = i;
      this.boomAt = this.clock;
      this.endSecs = Math.max(0.0F, this.time - this.startT);
      int bc = i % 12;
      int br = i / 12;

      for (int j = 0; j < 120; j++) {
         if (this.mine[j] && !this.flag[j]) {
            this.open[j] = true;
            float d = (float)Math.hypot(j % 12 - bc, j / 12 - br);
            this.openAt[j] = j == i ? this.clock : this.clock + 0.15F + d * 0.05F;
         }
      }

      float cx = 20.0F + bc * 26.0F + 13.0F;
      float cy = 42.0F + br * 26.0F + 13.0F;

      for (int k = 0; k < 34; k++) {
         double a = this.rnd.nextDouble() * Math.PI * 2.0;
         float sp = 50.0F + this.rnd.nextFloat() * 170.0F;
         this.parts
            .add(
               new float[]{
                  cx,
                  cy,
                  (float)Math.cos(a) * sp,
                  (float)Math.sin(a) * sp - 80.0F,
                  0.7F + this.rnd.nextFloat() * 0.7F,
                  DEBRIS[this.rnd.nextInt(DEBRIS.length)],
                  2.5F + this.rnd.nextFloat() * 3.0F
               }
            );
      }

      Sound.play("minecraft:entity.generic.explode", 1.1F, 0.45F);
   }

   @Override
   protected void render() {
      float dt = Gx.dt;
      this.clock += dt;
      float hx = (this.mx - this.bx) / this.u;
      float hy = (this.my - this.by) / this.u;
      boolean active = this.state == GameView.State.PLAYING && !this.won && !this.lost;
      int hover = active ? this.cellAt(hx, hy) : -1;
      boolean chordHover = hover >= 0 && this.open[hover] && this.adj[hover] > 0;
      this.header();
      float shake = this.lost ? Math.max(0.0F, 1.0F - (this.clock - this.boomAt) / 0.4F) : 0.0F;
      Gx.push();
      if (shake > 0.0F) {
         Gx.translate(this.S((this.rnd.nextFloat() - 0.5F) * 5.0F * shake), this.S((this.rnd.nextFloat() - 0.5F) * 5.0F * shake));
      }

      int ground = Style.light ? -2762272 : -15460836;
      int ground2 = Style.light ? -3222822 : -15658216;
      this.box(17.0F, 39.0F, 318.0F, 266.0F, 9.0F, Style.light ? 301989888 : 234881023);

      for (int i = 0; i < 120; i++) {
         int c = i % 12;
         int r = i / 12;
         float x = 20.0F + c * 26.0F;
         float y = 42.0F + r * 26.0F;
         float cx = x + 13.0F;
         float cy = y + 13.0F;
         boolean shownOpen = this.open[i] && this.clock >= this.openAt[i];
         float pa = this.open[i] ? Ease.clamp((this.clock - this.openAt[i]) / 0.2F) : 0.0F;
         if (this.open[i] && shownOpen) {
            int gc = (c + r) % 2 == 0 ? ground : ground2;
            if (this.mine[i] && i == this.boom) {
               gc = -1900533;
            } else if (this.mine[i]) {
               gc = Gx.mix(gc, -1900533, 0.25F);
            }

            if (chordHover && i == hover) {
               gc = Gx.mix(gc, Style.accent, 0.15F);
            }

            this.box(x + 0.6F, y + 0.6F, 24.8F, 24.8F, 4.0F, gc);
            if (this.mine[i]) {
               if (i == this.boom) {
                  Gx.glow(this.X(cx), this.Y(cy), this.S(30.0F), Gx.withAlpha(-30208, 0.5F));
               }

               this.mineIcon(cx, cy, Ease.outBack(pa));
            } else if (this.adj[i] > 0) {
               float s = 0.6F + 0.4F * Ease.outBack(pa);
               int nc = (Style.light ? NUM_LIGHT : NUM)[this.adj[i]];
               Gx.push();
               Gx.scaleAt(this.X(cx), this.Y(cy), s);
               this.text(String.valueOf(this.adj[i]), cx, cy, 11.0F, 3, Gx.withAlpha(nc, pa));
               Gx.pop();
            }
         }

         if (!this.open[i] || pa < 1.0F) {
            float s = this.open[i] ? 1.0F - 0.4F * Ease.outCubic(pa) : 1.0F;
            float a = this.open[i] ? 1.0F - pa : 1.0F;
            int bc = (c + r) % 2 == 0 ? -6972509 : -7564646;
            if (this.won && this.mine[i]) {
               bc = Gx.mix(bc, Style.ok, 0.45F * Ease.clamp((this.clock - this.flagAt[i]) / 0.3F));
            }

            if (i == hover && !this.open[i]) {
               bc = Gx.mix(bc, -1, 0.25F);
            } else if (chordHover && !this.open[i] && !this.flag[i] && this.isNeighbour(hover, i)) {
               bc = Gx.mix(bc, Style.accent, 0.3F);
            }

            Gx.push();
            Gx.pushAlpha(a);
            Gx.scaleAt(this.X(cx), this.Y(cy), s);
            float lift = i == hover && !this.open[i] ? 0.8F : 0.0F;
            this.brick(x + 1.0F, y + 1.0F - lift, 24.0F, 24.0F, bc);
            if (this.flag[i]) {
               float fs = Ease.outBack((this.clock - this.flagAt[i]) / 0.25F);
               if (fs > 0.0F) {
                  Gx.icon("flag", this.X(cx + 0.6F), this.Y(cy + 0.8F - lift), this.S(12.0F) * fs, 1711276032);
                  Gx.icon("flag", this.X(cx), this.Y(cy - lift), this.S(12.0F) * fs, -1900533);
               }

               if (this.lost && !this.mine[i]) {
                  float wa = Ease.clamp((this.clock - this.boomAt - 0.3F) / 0.2F);
                  Gx.icon("x", this.X(cx), this.Y(cy), this.S(17.0F), Gx.withAlpha(-1, wa));
               }
            }

            Gx.popAlpha();
            Gx.pop();
         }
      }

      if (this.lost) {
         float t = this.clock - this.boomAt;
         float bcx = 20.0F + this.boom % 12 * 26.0F + 13.0F;
         float bcy = 42.0F + this.boom / 12 * 26.0F + 13.0F;
         if (t < 0.7F) {
            float p = t / 0.7F;
            Gx.glow(this.X(bcx), this.Y(bcy), this.S(20.0F + 70.0F * Ease.outCubic(p)), Gx.withAlpha(-24003, 0.8F * (1.0F - p)));
            Gx.ringAt(
               this.X(bcx),
               this.Y(bcy),
               this.S(6.0F + 80.0F * Ease.outCubic(p)),
               Math.max(1, this.S(3.0F * (1.0F - p) + 0.5F)),
               Gx.withAlpha(-1, 0.8F * (1.0F - p))
            );
         }

         for (float[] p : this.parts) {
            p[3] += 420.0F * dt;
            p[0] += p[2] * dt;
            p[1] += p[3] * dt;
            p[4] -= dt;
            if (!(p[4] <= 0.0F)) {
               float ax = Math.min(1.0F, p[4] / 0.3F);
               this.box(p[0] - p[6] / 2.0F, p[1] - p[6] / 2.0F, p[6], p[6], 1.0F, Gx.withAlpha((int)p[5], ax));
            }
         }

         this.parts.removeIf(px -> px[4] <= 0.0F);
      }

      Gx.pop();
   }

   private boolean isNeighbour(int a, int b) {
      int ac = a % 12;
      int ar = a / 12;
      int bc = b % 12;
      int br = b / 12;
      return a != b && Math.abs(ac - bc) <= 1 && Math.abs(ar - br) <= 1;
   }

   private void mineIcon(float cx, float cy, float s) {
      if (!(s <= 0.0F)) {
         float r = 5.6F * s;

         for (int k = 0; k < 8; k++) {
            double a = k * Math.PI / 4.0;
            this.circle(cx + (float)Math.cos(a) * r * 1.2F, cy + (float)Math.sin(a) * r * 1.2F, 1.3F * s, -15000286);
         }

         this.circle(cx, cy, r, -15000286);
         this.circle(cx, cy, r * 0.62F, -13618373);
         this.circle(cx - r * 0.35F, cy - r * 0.35F, r * 0.26F, -855638017);
      }
   }

   private void header() {
      float y = 8.0F;
      float h = 26.0F;
      int left = 18 - this.flags;
      this.box(20.0F, y, 76.0F, h, 13.0F, Style.surface);
      Gx.icon("flag", this.X(36.0F), this.Y(y + h / 2.0F), this.S(12.0F), -1900533);
      Gx.textMid(String.valueOf(left), this.X(48.0F), this.Y(y + h / 2.0F), 10.0F * this.u, 3, left < 0 ? Style.danger : Style.text);
      Gx.textRight("Minen", this.X(88.0F), this.Y(y + h / 2.0F + 0.5F), 6.0F * this.u, 2, Style.muted);
      float tw = 76.0F;
      float tx = 332.0F - tw;
      int s = (int)this.secs();
      this.box(tx, y, tw, h, 13.0F, Style.surface);
      Gx.icon("clock", this.X(tx + 16.0F), this.Y(y + h / 2.0F), this.S(11.0F), Style.accent);
      Gx.textMid(String.format("%d:%02d", s / 60, s % 60), this.X(tx + 28.0F), this.Y(y + h / 2.0F), 10.0F * this.u, 3, Style.text);
      int sc = Style.sub;
      String st;
      if (this.won) {
         st = "Alle Minen gefunden!";
         sc = Style.ok;
      } else if (this.lost) {
         st = "BOOM! Mine erwischt";
         sc = Style.danger;
      } else if (!this.generated) {
         st = "Erster Klick ist sicher";
      } else {
         st = Math.round(this.opened * 100.0F / 102.0F) + "% aufgedeckt";
      }

      this.text(st, 176.0F, y + h / 2.0F, 7.4F, 2, sc);
      if (this.generated && !this.won && !this.lost) {
         float pw = 90.0F;
         float px = 176.0F - pw / 2.0F;
         this.box(px, y + h - 3.0F, pw, 2.0F, 1.0F, Style.surface2);
         this.box(px, y + h - 3.0F, pw * this.opened / 102.0F, 2.0F, 1.0F, Style.accent);
      }
   }
}
