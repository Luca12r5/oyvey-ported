package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;

public final class Breakout extends GameView {
   private static final float W = 360.0F;
   private static final float H = 290.0F;
   private static final float TOP = 22.0F;
   private static final int COLS = 10;
   private static final int ROWS = 6;
   private static final float BW = 32.0F;
   private static final float BH = 13.0F;
   private static final float GAP = 2.0F;
   private static final float X0 = 11.0F;
   private static final float Y0 = 36.0F;
   private static final float PY = 268.0F;
   private static final float PH = 9.0F;
   private static final float R = 4.5F;
   private static final int[] ROW_COLORS = new int[]{-3597815, -95720, -864969, -11821238, -16755265, -7194248};
   private static final int HARD = -6249047;
   private final List<Breakout.Brick> bricks = new ArrayList<>();
   private final List<float[]> parts = new ArrayList<>();
   private final List<float[]> pops = new ArrayList<>();
   private final float[][] trail = new float[10][2];
   private float padX;
   private float padW;
   private float ballX;
   private float ballY;
   private float vx;
   private float vy;
   private float speed;
   private float stuckT;
   private boolean stuck;
   private boolean left;
   private boolean right;
   private boolean mouseMode;
   private float lastMx = -9999.0F;
   private int lives;
   private int level;
   private int combo;
   private float levelAnim;
   private float bannerT = -1.0F;
   private float lifeFlash;
   private float padHit;

   public Breakout() {
      super("breakout");
   }

   @Override
   protected float boardW() {
      return 360.0F;
   }

   @Override
   protected float boardH() {
      return 290.0F;
   }

   @Override
   protected void reset() {
      this.lives = 3;
      this.level = 1;
      this.padW = 60.0F;
      this.padX = 180.0F;
      this.left = this.right = false;
      this.parts.clear();
      this.pops.clear();
      this.lifeFlash = 0.0F;
      this.build();
      this.bannerT = -1.0F;
   }

   private void build() {
      this.bricks.clear();
      int hardRows = Math.min(4, 1 + (this.level - 1) / 2) + (this.level > 1 ? 1 : 0);

      for (int r = 0; r < 6; r++) {
         for (int c = 0; c < 10; c++) {
            if (pattern(this.level, r, c)) {
               Breakout.Brick b = new Breakout.Brick();
               b.row = r;
               b.col = c;
               b.x = 11.0F + c * 34.0F;
               b.y = 36.0F + r * 15.0F;
               b.maxHp = b.hp = r >= Math.min(hardRows, 2) && (this.level <= 2 || r >= hardRows || (c + r) % 3 != 0) ? 1 : 2;
               b.color = ROW_COLORS[r % ROW_COLORS.length];
               this.bricks.add(b);
            }
         }
      }

      this.levelAnim = 0.0F;
      this.speed = 190.0F * (1.0F + 0.09F * (this.level - 1));
      this.combo = 0;
      this.stick();
   }

   private static boolean pattern(int level, int r, int c) {
      switch ((level - 1) % 4) {
         case 1:
            return Math.abs(c - 4.5F) <= r + 0.5F;
         case 2:
            return (r + c) % 2 == 0 || r == 0 || r == 5;
         case 3:
            return c != 4 && c != 5 || r % 2 == 0;
         default:
            return true;
      }
   }

   private void stick() {
      this.stuck = true;
      this.stuckT = 0.0F;
      this.ballX = this.padX;
      this.ballY = 263.0F;

      for (float[] t : this.trail) {
         t[0] = this.ballX;
         t[1] = this.ballY;
      }
   }

   private void launch() {
      if (this.stuck) {
         this.stuck = false;
         float ang = (float)Math.toRadians(-20.0F + this.rnd.nextFloat() * 40.0F);
         this.vx = (float)Math.sin(ang) * this.speed;
         this.vy = -((float)Math.cos(ang)) * this.speed;
         Sound.play("minecraft:block.note_block.hat", 1.4F, 0.25F);
      }
   }

   @Override
   protected void onKey(int k) {
      if (k == 263 || k == 65) {
         this.left = true;
         this.mouseMode = false;
      }

      if (k == 262 || k == 68) {
         this.right = true;
         this.mouseMode = false;
      }

      if (k == 32 || k == 265 || k == 87) {
         this.launch();
      }
   }

   @Override
   protected void onKeyUp(int k) {
      if (k == 263 || k == 65) {
         this.left = false;
      }

      if (k == 262 || k == 68) {
         this.right = false;
      }
   }

   @Override
   protected void onClick(float x, float y, int button) {
      if (button == 0) {
         this.launch();
      }
   }

   @Override
   protected void update(float dt) {
      if (Math.abs(this.mx - this.lastMx) > 0.5F && this.mx >= 0.0F) {
         if (this.lastMx > -9999.0F) {
            this.mouseMode = true;
         }

         this.lastMx = this.mx;
      }

      if (this.mouseMode && this.u > 0.0F) {
         float target = (this.mx - this.bx) / this.u;
         this.padX = this.padX + (target - this.padX) * Math.min(1.0F, dt * 25.0F);
      } else {
         float dir = (this.right ? 1 : 0) - (this.left ? 1 : 0);
         this.padX += dir * 320.0F * dt;
      }

      this.padX = Math.max(this.padW / 2.0F + 2.0F, Math.min(360.0F - this.padW / 2.0F - 2.0F, this.padX));
      this.padHit = Math.max(0.0F, this.padHit - dt * 4.0F);

      for (float[] p : this.parts) {
         p[0] += p[2] * dt;
         p[1] += p[3] * dt;
         p[3] += 300.0F * dt;
         p[4] += dt;
      }

      this.parts.removeIf(p -> p[4] > 0.8F);

      for (float[] p : this.pops) {
         p[2] += dt;
      }

      this.pops.removeIf(p -> p[2] > 0.8F);

      for (Breakout.Brick b : this.bricks) {
         b.flash = Math.max(0.0F, b.flash - dt * 5.0F);
      }

      if (this.bannerT >= 0.0F) {
         this.bannerT += dt;
         if (this.bannerT > 1.6F) {
            this.bannerT = -1.0F;
         }
      }

      if (this.stuck) {
         this.ballX = this.padX;
         this.ballY = 263.0F;
         this.stuckT += dt;
         if (this.stuckT > 2.5F) {
            this.launch();
         }

         for (float[] t : this.trail) {
            t[0] = this.ballX;
            t[1] = this.ballY;
         }
      } else {
         for (int i = this.trail.length - 1; i > 0; i--) {
            this.trail[i][0] = this.trail[i - 1][0];
            this.trail[i][1] = this.trail[i - 1][1];
         }

         this.trail[0][0] = this.ballX;
         this.trail[0][1] = this.ballY;
         int steps = 4;
         float h = dt / steps;

         for (int s = 0; s < steps; s++) {
            this.ballX = this.ballX + this.vx * h;
            this.ballY = this.ballY + this.vy * h;
            if (this.ballX < 4.5F) {
               this.ballX = 4.5F;
               this.vx = Math.abs(this.vx);
               this.wallSound();
            }

            if (this.ballX > 355.5F) {
               this.ballX = 355.5F;
               this.vx = -Math.abs(this.vx);
               this.wallSound();
            }

            if (this.ballY < 26.5F) {
               this.ballY = 26.5F;
               this.vy = Math.abs(this.vy);
               this.wallSound();
            }

            if (this.vy > 0.0F
               && this.ballY + 4.5F >= 268.0F
               && this.ballY - 4.5F < 277.0F
               && this.ballX > this.padX - this.padW / 2.0F - 4.5F
               && this.ballX < this.padX + this.padW / 2.0F + 4.5F) {
               float off = Math.max(-1.0F, Math.min(1.0F, (this.ballX - this.padX) / (this.padW / 2.0F)));
               float ang = (float)Math.toRadians(off * 62.0F);
               this.speed = Math.min(this.speed * 1.012F, 190.0F * (1.0F + 0.09F * (this.level - 1)) * 1.35F);
               this.vx = (float)Math.sin(ang) * this.speed;
               this.vy = -((float)Math.cos(ang)) * this.speed;
               this.ballY = 263.5F;
               this.combo = 0;
               this.padHit = 1.0F;
               Sound.play("minecraft:block.note_block.basedrum", 1.2F, 0.3F);
            }

            for (Breakout.Brick b : this.bricks) {
               if (b.alive) {
                  float cx = Math.max(b.x, Math.min(this.ballX, b.x + 32.0F));
                  float cy = Math.max(b.y, Math.min(this.ballY, b.y + 13.0F));
                  float dx = this.ballX - cx;
                  float dy = this.ballY - cy;
                  if (!(dx * dx + dy * dy > 20.25F)) {
                     float penX = Math.min(this.ballX + 4.5F - b.x, b.x + 32.0F - (this.ballX - 4.5F));
                     float penY = Math.min(this.ballY + 4.5F - b.y, b.y + 13.0F - (this.ballY - 4.5F));
                     if (penX < penY) {
                        this.vx = this.ballX < b.x + 16.0F ? -Math.abs(this.vx) : Math.abs(this.vx);
                     } else {
                        this.vy = this.ballY < b.y + 6.5F ? -Math.abs(this.vy) : Math.abs(this.vy);
                     }

                     this.hit(b);
                     break;
                  }
               }
            }

            if (this.ballY - 4.5F > 296.0F) {
               this.lose();
               return;
            }
         }

         if (this.bricks.stream().noneMatch(bx -> bx.alive)) {
            this.nextLevel();
         }
      }
   }

   private void wallSound() {
      Sound.play("minecraft:block.note_block.hat", 1.8F, 0.12F);
   }

   private void hit(Breakout.Brick b) {
      b.hp--;
      b.flash = 1.0F;
      if (b.hp <= 0) {
         b.alive = false;
         this.combo++;
         int pts = (b.maxHp > 1 ? 20 : 10) * this.level + (this.combo > 1 ? (this.combo - 1) * 2 : 0);
         this.score += pts;
         this.pops.add(new float[]{b.x + 16.0F, b.y + 6.5F, 0.0F, pts});

         for (int i = 0; i < 12; i++) {
            this.particle(b, b.maxHp > 1 ? -6249047 : b.color, 2.0F + this.rnd.nextFloat() * 1.8F);
         }

         Sound.play("minecraft:block.stone.break", 1.1F + Math.min(0.6F, this.combo * 0.06F), 0.3F);
      } else {
         Sound.play("minecraft:block.anvil.land", 1.9F, 0.12F);

         for (int i = 0; i < 4; i++) {
            this.particle(b, -6249047, 1.5F);
         }
      }
   }

   private void particle(Breakout.Brick b, int col, float size) {
      this.parts
         .add(
            new float[]{
               b.x + this.rnd.nextFloat() * 32.0F,
               b.y + this.rnd.nextFloat() * 13.0F,
               (this.rnd.nextFloat() - 0.5F) * 160.0F,
               -30.0F - this.rnd.nextFloat() * 90.0F,
               0.0F,
               col,
               size
            }
         );
   }

   private void lose() {
      this.lives--;
      this.lifeFlash = 1.0F;
      Sound.play("minecraft:entity.arrow.hit_player", 0.7F, 0.4F);
      if (this.lives <= 0) {
         this.gameOver();
      } else {
         this.speed = 190.0F * (1.0F + 0.09F * (this.level - 1));
         this.stick();
      }
   }

   private void nextLevel() {
      this.level++;
      this.score = this.score + 50L * (this.level - 1);
      this.bannerT = 0.0F;
      Sound.play("minecraft:entity.player.levelup", 1.1F, 0.35F);
      this.build();
   }

   @Override
   protected void render() {
      float dt = Math.min(0.05F, Gx.dt);
      this.levelAnim += dt;
      this.lifeFlash = Math.max(0.0F, this.lifeFlash - dt * 1.5F);

      for (float y = 30.0F; y < 290.0F; y += 16.0F) {
         for (float x = 8.0F; x < 360.0F; x += 16.0F) {
            this.circle(x, y, 2.4F, Style.light ? 234881024 : 134217727);
         }
      }

      this.box(8.0F, 21.2F, 344.0F, 0.8F, 0.0F, Style.light ? 369098752 : 352321535);

      for (int i = 0; i < 3; i++) {
         boolean on = i < this.lives;
         float s = on ? 1.0F : 0.85F;
         int col = on ? -45730 : (Style.light ? 855638016 : 872415231);
         if (!on && i == this.lives && this.lifeFlash > 0.0F) {
            col = Gx.mix(col, -45730, this.lifeFlash);
         }

         Gx.icon(on ? "heart-fill" : "heart", this.X(12 + i * 15), this.Y(11.0F), 10.0F * this.u * s, col);
      }

      this.text("Level " + this.level, 330.0F, 11.0F, 7.5F, 3, Style.accent);
      int left = 0;

      for (Breakout.Brick b : this.bricks) {
         if (b.alive) {
            left++;
         }
      }

      this.text(left + " Steine", 180.0F, 11.0F, 6.8F, 2, Style.sub);

      for (Breakout.Brick bx : this.bricks) {
         if (bx.alive) {
            float t = Ease.stagger(this.levelAnim, bx.row * 10 + bx.col, 0.012F, 0.35F);
            if (!(t <= 0.0F)) {
               float sc = Ease.outBack(t);
               int col = bx.maxHp > 1 ? (bx.hp > 1 ? -6249047 : Gx.mix(-6249047, -10854554, 0.35F)) : bx.color;
               col = Gx.mix(col, -1, bx.flash * 0.6F);
               Gx.push();
               Gx.scaleAt(this.X(bx.x + 16.0F), this.Y(bx.y + 6.5F), sc);
               Gx.shadow(this.X(bx.x), this.Y(bx.y + 1.0F), this.S(32.0F), this.S(13.0F), this.S(2.5F), this.S(2.0F), 0.3F);
               this.brick(bx.x, bx.y, 32.0F, 13.0F, col);
               if (bx.maxHp > 1 && bx.hp == 1) {
                  this.box(bx.x + 7.0F, bx.y + 3.0F, 1.0F, 7.0F, 0.5F, -2011160026);
                  this.box(bx.x + 7.0F, bx.y + 9.0F, 6.0F, 1.0F, 0.5F, -2011160026);
                  this.box(bx.x + 22.0F, bx.y + 2.0F, 1.0F, 5.0F, 0.5F, -2011160026);
                  this.box(bx.x + 18.0F, bx.y + 6.0F, 5.0F, 1.0F, 0.5F, -2011160026);
               }

               Gx.pop();
            }
         }
      }

      for (float[] p : this.parts) {
         float a = 1.0F - p[4] / 0.8F;
         this.box(p[0] - p[6] / 2.0F, p[1] - p[6] / 2.0F, p[6], p[6], 0.6F, Gx.withAlpha((int)p[5], a));
      }

      for (float[] p : this.pops) {
         float a = 1.0F - Ease.outCubic(p[2] / 0.8F);
         this.text("+" + (int)p[3], p[0], p[1] - p[2] * 22.0F, 7.5F, 3, Gx.withAlpha(-1, a));
      }

      float px = this.padX - this.padW / 2.0F;
      float pyy = 268.0F + this.padHit * 1.5F;
      Gx.glow(this.X(this.padX), this.Y(pyy + 4.5F), this.S(this.padW * 0.7F), Gx.withAlpha(Style.accent, 0.18F + this.padHit * 0.2F));
      Gx.shadow(this.X(px), this.Y(pyy + 1.0F), this.S(this.padW), this.S(9.0F), this.S(3.0F), this.S(3.0F), 0.4F);
      this.boxV(px, pyy, this.padW, 9.0F, 3.0F, Gx.mix(Style.accent, -1, 0.2F), Gx.mix(Style.accent, -16777216, 0.2F));
      Gx.outline(
         this.X(px),
         this.Y(pyy),
         this.S(this.padW),
         this.S(9.0F),
         this.S(3.0F),
         Math.max(1, this.S(0.5F)),
         Gx.withAlpha(Gx.mix(Style.accent, -16777216, 0.5F), 0.7F)
      );

      for (int i = 0; i < 6; i++) {
         float sx = px + this.padW * (i + 0.5F) / 6.0F;
         this.box(sx - 3.0F, pyy - 2.4F, 6.0F, 3.0F, 1.0F, Gx.mix(Style.accent, -1, 0.12F));
      }

      this.box(px + 2.0F, pyy + 1.2F, this.padW - 4.0F, 1.2F, 0.6F, 1157627903);
      if (this.state != GameView.State.OVER || this.ballY < 290.0F) {
         if (!this.stuck) {
            for (int i = this.trail.length - 1; i >= 0; i--) {
               float a = (1.0F - (float)i / this.trail.length) * 0.3F;
               this.circle(this.trail[i][0], this.trail[i][1], 4.5F * (1.0F - (float)i / this.trail.length * 0.6F), Gx.withAlpha(Style.accent, a));
            }
         }

         Gx.glow(this.X(this.ballX), this.Y(this.ballY), this.S(18.0F), 1157627903);
         this.circle(this.ballX, this.ballY, 4.5F, -1);
         this.circle(this.ballX - 1.35F, this.ballY - 1.35F, 1.8000001F, -1);
         this.circle(this.ballX + 0.4F, this.ballY + 0.6F, 3.375F, -1578256);
         this.circle(this.ballX - 1.125F, this.ballY - 1.125F, 1.5749999F, -1);
      }

      if (this.stuck && this.state == GameView.State.PLAYING) {
         float a = 0.55F + 0.45F * (float)Math.sin(this.levelAnim * 5.0F);
         this.text("Leertaste oder Klick zum Abschießen", 180.0F, 234.0F, 7.0F, 3, Gx.withAlpha(Style.sub, a));
      }

      if (this.lifeFlash > 0.0F) {
         this.box(0.0F, 0.0F, 360.0F, 290.0F, 12.0F, Gx.withAlpha(-45730, this.lifeFlash * 0.18F));
      }

      if (this.bannerT >= 0.0F) {
         float t = this.bannerT / 1.6F;
         float s = Ease.outBack(Math.min(1.0F, this.bannerT / 0.35F));
         float a = t < 0.75F ? 1.0F : 1.0F - (t - 0.75F) / 0.25F;
         float cy = 174.0F;
         Gx.pushAlpha(a);
         Gx.push();
         Gx.scaleAt(this.X(180.0F), this.Y(cy), s);
         Gx.glow(this.X(180.0F), this.Y(cy), this.S(80.0F), Gx.withAlpha(Style.accent, 0.3F));
         this.box(120.0F, cy - 16.0F, 120.0F, 32.0F, 12.0F, Gx.withAlpha(Style.accent, 0.95F));
         this.text("Level " + this.level, 180.0F, cy, 13.0F, 3, Style.accentText);
         Gx.pop();
         Gx.popAlpha();
      }
   }

   private static final class Brick {
      float x;
      float y;
      int hp;
      int maxHp;
      int color;
      int row;
      int col;
      float flash;
      boolean alive = true;
   }
}
