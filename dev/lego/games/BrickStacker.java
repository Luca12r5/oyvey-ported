package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class BrickStacker extends GameView {
   private static final float W = 400.0F;
   private static final float H = 320.0F;
   private static final float BH = 16.0F;
   private static final float START_W = 120.0F;
   private static final float BASE_Y = 290.0F;
   private static final float PERFECT = 3.2F;
   private static final int[] COLORS = new int[]{-3597815, -95720, -864969, -11821238, -13193537, -16755265, -7194248, -1790520};
   private final List<BrickStacker.Brick> tower = new ArrayList<>();
   private final List<BrickStacker.Chip> chips = new ArrayList<>();
   private final GameFx fx = new GameFx();
   private final float[][] clouds = new float[8][3];
   private final float[][] stars = new float[60][3];
   private float curX;
   private float curW;
   private float dir;
   private float speed;
   private float cam;
   private float anim;
   private float dropT;
   private float missT;
   private float hintT;
   private int combo;
   private int colorIdx;
   private int bestCombo;
   private boolean missed;

   public BrickStacker() {
      super("stacker");
      Random r = new Random(5L);

      for (float[] c : this.clouds) {
         c[0] = r.nextFloat() * 400.0F;
         c[1] = 60.0F + r.nextFloat() * 1400.0F;
         c[2] = 0.6F + r.nextFloat() * 0.7F;
      }

      for (float[] s : this.stars) {
         s[0] = r.nextFloat() * 400.0F;
         s[1] = r.nextFloat() * 320.0F;
         s[2] = 0.3F + r.nextFloat() * 0.7F;
      }
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
      return "Punkte";
   }

   @Override
   protected void reset() {
      this.tower.clear();
      this.chips.clear();
      this.fx.clear();
      this.colorIdx = new Random().nextInt(COLORS.length);
      BrickStacker.Brick b = new BrickStacker.Brick();
      b.w = 120.0F;
      b.x = 140.0F;
      b.color = COLORS[this.colorIdx++ % COLORS.length];
      this.tower.add(b);
      this.combo = 0;
      this.bestCombo = 0;
      this.cam = 0.0F;
      this.missed = false;
      this.missT = 0.0F;
      this.hintT = 0.0F;
      this.next();
   }

   private float topY(int level) {
      return 290.0F - (level + 1) * 16.0F;
   }

   private void next() {
      BrickStacker.Brick top = this.tower.get(this.tower.size() - 1);
      this.curW = top.w;
      this.dir = this.tower.size() % 2 == 0 ? -1.0F : 1.0F;
      this.curX = this.dir > 0.0F ? -this.curW : 400.0F;
      this.speed = Math.min(300.0F, 118.0F + this.tower.size() * 4.2F);
      this.dropT = 0.0F;
   }

   @Override
   protected void onKey(int k) {
      if (k == 32 || k == 264 || k == 83 || k == 257) {
         this.drop();
      }
   }

   @Override
   protected void onClick(float x, float y, int button) {
      if (button == 0) {
         this.drop();
      }
   }

   private void drop() {
      if (!this.missed && !(this.time < 0.2F) && !(this.dropT < 0.12F)) {
         BrickStacker.Brick top = this.tower.get(this.tower.size() - 1);
         float l = Math.max(this.curX, top.x);
         float r = Math.min(this.curX + this.curW, top.x + top.w);
         float y = this.topY(this.tower.size());
         int col = COLORS[this.colorIdx % COLORS.length];
         if (r - l <= 0.5F) {
            this.missed = true;
            BrickStacker.Chip c = new BrickStacker.Chip();
            c.x = this.curX;
            c.y = y;
            c.w = this.curW;
            c.vx = this.dir * this.speed * 0.3F;
            c.vy = 0.0F;
            c.color = col;
            this.chips.add(c);
            this.fx.shake(0.6F);
            Sound.play("minecraft:block.stone.break", 0.6F, 0.5F);
         } else {
            BrickStacker.Brick b = new BrickStacker.Brick();
            b.color = col;
            this.colorIdx++;
            boolean perfect = Math.abs(this.curX - top.x) <= 3.2F;
            if (perfect) {
               this.combo++;
               this.bestCombo = Math.max(this.bestCombo, this.combo);
               b.x = top.x;
               b.w = top.w;
               if (this.combo >= 3) {
                  float grow = Math.min(8.0F, 120.0F - b.w);
                  if (grow > 0.0F) {
                     b.x -= grow / 2.0F;
                     b.w += grow;
                     b.x = Math.max(4.0F, Math.min(396.0F - b.w, b.x));
                     this.fx.popup("Wächst!", 200.0F, y - 30.0F, -6291531, 9.0F);
                  }
               }

               b.flash = 1.0F;
               this.score = this.score + (1 + this.combo);
               this.fx.ring(b.x + b.w / 2.0F, y + 8.0F, b.w * 0.75F, -1, 0.45F);
               this.fx.popup(this.combo > 1 ? "Perfekt! x" + this.combo : "Perfekt!", b.x + b.w / 2.0F, y - 12.0F, -736942, 11 + Math.min(4, this.combo));
               this.fx.burst(b.x + b.w / 2.0F, y + 8.0F, 14 + this.combo * 2, -5720, 120.0F, 2.4F, 0.6F, 0.0F, 2);
               Sound.play("minecraft:block.note_block.pling", 0.8F + Math.min(1.2F, this.combo * 0.1F), 0.45F);
            } else {
               this.combo = 0;
               b.x = l;
               b.w = r - l;
               BrickStacker.Chip c = new BrickStacker.Chip();
               c.color = col;
               c.y = y;
               if (this.curX < top.x) {
                  c.x = this.curX;
                  c.w = top.x - this.curX;
                  c.vx = -40.0F - this.rnd.nextFloat() * 30.0F;
               } else {
                  c.x = r;
                  c.w = this.curX + this.curW - r;
                  c.vx = 40.0F + this.rnd.nextFloat() * 30.0F;
               }

               c.vy = -40.0F;
               this.chips.add(c);
               this.score++;
               this.fx.popup("+1", b.x + b.w / 2.0F, y - 10.0F, -1, 9.0F);
               Sound.play("minecraft:block.stone.place", 0.9F + this.rnd.nextFloat() * 0.2F, 0.5F);
            }

            this.fx.burst(b.x + 2.0F, y + 16.0F, 5, -1, 40.0F, 2.0F, 0.4F, -20.0F, 0);
            this.fx.burst(b.x + b.w - 2.0F, y + 16.0F, 5, -1, 40.0F, 2.0F, 0.4F, -20.0F, 0);

            for (int i = Math.max(0, this.tower.size() - 5); i < this.tower.size(); i++) {
               this.tower.get(i).wob = 1.0F - (this.tower.size() - i) * 0.18F;
            }

            b.wob = 1.0F;
            this.tower.add(b);
            int lv = this.tower.size() - 1;
            if (lv % 10 == 0) {
               this.fx.popup(lv + " Steine!", 200.0F, y - 34.0F, -33835, 15.0F);
               this.fx.shake(0.2F);
               Sound.play("minecraft:entity.player.levelup", 1.2F, 0.4F);
            }

            this.next();
         }
      }
   }

   @Override
   protected void update(float dt) {
      this.fx.update(dt);
      this.hintT += dt;
      this.dropT += dt;

      for (BrickStacker.Brick b : this.tower) {
         b.wob = Math.max(0.0F, b.wob - dt * 2.5F);
         b.flash = Math.max(0.0F, b.flash - dt * 3.0F);
      }

      for (BrickStacker.Chip c : this.chips) {
         c.t += dt;
         c.vy += 900.0F * dt;
         c.x = c.x + c.vx * dt;
         c.y = c.y + c.vy * dt;
      }

      this.chips.removeIf(cx -> cx.y - this.camY() > 420.0F);
      if (this.missed) {
         this.missT += dt;
         if (this.missT > 0.9F) {
            this.gameOver();
         }
      } else {
         this.curX = this.curX + this.dir * this.speed * dt;
         if (this.curX > 400.0F - this.curW * 0.1F && this.dir > 0.0F) {
            this.dir = -1.0F;
         }

         if (this.curX < -this.curW * 0.9F && this.dir < 0.0F) {
            this.dir = 1.0F;
         }

         float want = Math.max(0.0F, (this.tower.size() - 9) * 16.0F);
         this.cam = this.cam + (want - this.cam) * Math.min(1.0F, dt * 5.0F);
      }
   }

   private float camY() {
      return -this.cam;
   }

   @Override
   protected void render() {
      float dt = Math.min(0.05F, Gx.dt);
      this.anim += dt;
      float alt = this.cam / 16.0F;
      float k1 = Ease.clamp(alt / 40.0F);
      float k2 = Ease.clamp((alt - 40.0F) / 50.0F);
      int top = Gx.mix(Gx.mix(-10509338, -2066854, k1), -16119004, k2);
      int bot = Gx.mix(Gx.mix(-3283206, -539254, k1), -14016946, k2);
      this.boxV(0.0F, 0.0F, 400.0F, 320.0F, 0.0F, top, bot);
      if (k2 > 0.0F) {
         for (float[] s : this.stars) {
            float sy = ((s[1] + this.cam * 0.1F * s[2]) % 320.0F + 320.0F) % 320.0F;
            this.circle(s[0], sy, 0.6F + s[2], Gx.withAlpha(-1, k2 * s[2] * (0.6F + 0.4F * (float)Math.sin(this.anim * 2.0F + s[0]))));
         }
      }

      float sunY = 70.0F + k1 * 60.0F + this.cam * 0.05F;
      if (sunY < 360.0F) {
         Gx.glow(this.X(330.0F), this.Y(sunY), this.S(70.0F), Gx.withAlpha(-6496, 0.5F * (1.0F - k2)));
         this.circle(330.0F, sunY, 16.0F, Gx.withAlpha(Gx.mix(-3132, -26022, k1), 1.0F - k2));
      }

      for (float[] c : this.clouds) {
         float cy = 320.0F - c[1] + this.cam * 0.55F;
         if (!(cy < -30.0F) && !(cy > 350.0F)) {
            float cx = ((c[0] + this.anim * 6.0F * c[2]) % 480.0F + 400.0F + 80.0F) % 480.0F - 40.0F;
            this.cloud(cx, cy, c[2], Gx.withAlpha(-1, 0.75F * (1.0F - k2 * 0.7F)));
         }
      }

      float cityY = 290.0F + this.cam * 0.35F;
      if (cityY - 80.0F < 320.0F) {
         for (int i = 0; i < 11; i++) {
            float bw = 26 + i * 37 % 17;
            float bhh = 30 + i * 53 % 50;
            this.box(i * 40 - 6, cityY - bhh, bw, bhh + 60.0F, 2.0F, Gx.withAlpha(Gx.mix(-9597000, -12965286, k1), 0.55F));
         }
      }

      Gx.push();
      Gx.translate(this.S(this.fx.sx), this.S(this.fx.sy));
      float baseY = 290.0F + this.cam;
      if (baseY < 340.0F) {
         this.boxV(-4.0F, baseY, 408.0F, 40.0F, 0.0F, -12603816, -14452166);

         for (float sx = 4.0F; sx < 400.0F; sx += 12.0F) {
            this.boxV(sx, baseY - 3.0F, 8.0F, 4.0F, 1.2F, -9710205, -12603816);
         }
      }

      int first = Math.max(0, (int)(this.cam / 16.0F) - 2);

      for (int i = first; i < this.tower.size(); i++) {
         BrickStacker.Brick b = this.tower.get(i);
         float y = this.topY(i) + this.cam;
         if (!(y > 340.0F)) {
            if (y < -32.0F) {
               break;
            }

            float wob = (float)Math.sin(b.wob * 14.0F) * b.wob * 1.4F;
            int col = Gx.mix(b.color, -1, b.flash * 0.6F);
            this.sideBrick(b.x + wob, y, b.w, 16.0F, col);
         }
      }

      for (int lv = 10; lv < this.tower.size() + 12; lv += 10) {
         float y = this.topY(lv - 1) + this.cam;
         if (!(y < -10.0F) && !(y > 330.0F)) {
            for (float dx = 0.0F; dx < 400.0F; dx += 10.0F) {
               this.box(dx, y, 5.0F, 0.8F, 0.4F, 1442840575);
            }

            this.box(366.0F, y - 8.0F, 30.0F, 12.0F, 6.0F, 1711276032);
            this.text(String.valueOf(lv), 381.0F, y - 2.0F, 6.5F, 3, -1);
         }
      }

      if (!this.missed && this.state != GameView.State.OVER) {
         float y = this.topY(this.tower.size()) + this.cam - 2.0F - (float)Math.sin(this.anim * 6.0F) * 0.8F;
         int col = COLORS[this.colorIdx % COLORS.length];
         BrickStacker.Brick t = this.tower.get(this.tower.size() - 1);
         float l = Math.max(this.curX, t.x);
         float r = Math.min(this.curX + this.curW, t.x + t.w);
         if (r > l) {
            this.box(l, y + 16.0F + 1.5F, r - l, 1.2F, 0.6F, -1996488705);
         }

         Gx.glow(this.X(this.curX + this.curW / 2.0F), this.Y(y + 8.0F), this.S(this.curW * 0.6F), Gx.withAlpha(col, 0.18F));
         this.sideBrick(this.curX, y, this.curW, 16.0F, col);
         if (Math.abs(this.curX - t.x) <= 3.2F) {
            this.box(this.curX, y - 5.0F, this.curW, 1.5F, 0.7F, -856374958);
         }
      }

      for (BrickStacker.Chip cx : this.chips) {
         float a = Math.max(0.0F, 1.0F - cx.t * 0.9F);
         Gx.pushAlpha(a);
         float sq = 1.0F - Math.min(0.3F, cx.t * 0.3F);
         this.sideBrick(cx.x, cx.y + this.cam, cx.w, 16.0F * sq, cx.color);
         Gx.popAlpha();
      }

      this.fx.draw(this, 0.0F, -this.cam);
      Gx.pop();
      this.box(8.0F, 8.0F, 86.0F, 22.0F, 11.0F, 1426063360);
      Gx.icon("brick", this.X(21.0F), this.Y(19.0F), 10.0F * this.u, -1);
      this.text(this.tower.size() - 1 + " Steine", 58.0F, 19.0F, 8.0F, 3, -1);
      if (this.combo >= 2) {
         this.box(306.0F, 8.0F, 86.0F, 22.0F, 11.0F, Gx.withAlpha(-736942, 0.95F));
         this.text("Perfekt-Serie " + this.combo, 349.0F, 19.0F, 7.4F, 3, -12965376);
      }

      if (this.state == GameView.State.PLAYING && this.hintT < 3.5F && this.tower.size() < 3) {
         float a = Math.min(1.0F, 3.5F - this.hintT) * (0.6F + 0.4F * (float)Math.sin(this.anim * 5.0F));
         this.box(104.0F, 44.0F, 192.0F, 22.0F, 11.0F, Gx.withAlpha(-16777216, 0.45F * a));
         this.text("Leertaste oder Klick zum Absetzen", 200.0F, 55.0F, 7.6F, 3, Gx.withAlpha(-1, a));
      }

      this.fx.drawPopups(this, 0.0F, -this.cam);
      this.fx.drawFlash(this, 400.0F, 320.0F);
   }

   private void cloud(float x, float y, float s, int c) {
      this.circle(x, y, 10.0F * s, c);
      this.circle(x + 12.0F * s, y - 5.0F * s, 13.0F * s, c);
      this.circle(x + 26.0F * s, y, 10.0F * s, c);
      this.box(x, y - 1.0F * s, 26.0F * s, 10.0F * s, 5.0F * s, c);
   }

   private void sideBrick(float x, float y, float w, float h, int col) {
      if (!(w < 0.5F)) {
         int studs = Math.max(1, Math.round(w / 15.0F));
         float sw = Math.min(8.0F, w * 0.6F);
         float gap = w / studs;

         for (int i = 0; i < studs; i++) {
            float sx = x + gap * (i + 0.5F) - sw / 2.0F;
            this.boxV(sx, y - 3.2F, sw, 4.2F, 1.2F, Gx.mix(col, -1, 0.3F), col);
         }

         this.boxV(x, y, w, h, 2.0F, Gx.mix(col, -1, 0.16F), Gx.mix(col, -16777216, 0.18F));
         this.box(x + 1.5F, y + 1.2F, Math.max(0.0F, w - 3.0F), 1.3F, 0.6F, 1090519039);
         this.box(x, y + h - 2.2F, w, 2.2F, 1.0F, Gx.withAlpha(Gx.mix(col, -16777216, 0.35F), 0.6F));
         Gx.outline(
            this.X(x),
            this.Y(y),
            Math.max(1, this.S(w)),
            Math.max(1, this.S(h)),
            this.S(2.0F),
            Math.max(1, this.S(0.55F)),
            Gx.withAlpha(Gx.mix(col, -16777216, 0.55F), 0.7F)
         );
      }
   }

   private static final class Brick {
      float x;
      float w;
      float wob;
      float flash;
      int color;
   }

   private static final class Chip {
      float x;
      float y;
      float w;
      float vx;
      float vy;
      float t;
      int color;
   }
}
