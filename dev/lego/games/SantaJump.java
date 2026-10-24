package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.RoundRectangle2D.Float;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class SantaJump extends GameView {
   private static final float W = 460.0F;
   private static final float H = 320.0F;
   private static final float GRAVITY = 980.0F;
   private static final float JUMP = -440.0F;
   private static final float SUPER = -800.0F;
   private static final float PW = 46.0F;
   private static final float PH = 13.0F;
   private static final float SANTA = 30.0F;
   private static final int CLOUD = 0;
   private static final int MOVING = 1;
   private static final int ICE = 2;
   private static final int CHIMNEY = 3;
   private static final int[] GIFT_BOX = new int[]{-1890757, -14703780, -12616705, -5157377, -20434};
   private static final int[] GIFT_RIB = new int[]{-736942, -722689, -736942, -722689, -1890757};
   private final List<SantaJump.Plat> plats = new ArrayList<>();
   private final List<SantaJump.Gift> gifts = new ArrayList<>();
   private final List<SantaJump.Storm> storms = new ArrayList<>();
   private final GameFx fx = new GameFx();
   private final float[][] stars = new float[80][3];
   private float x;
   private float y;
   private float vx;
   private float vy;
   private float cam;
   private float genY;
   private float maxH;
   private float squash;
   private float face = 1.0F;
   private float trail;
   private float comboT;
   private float anim;
   private float deadT;
   private float hintT;
   private float superT;
   private float lastMx = -9999.0F;
   private int combo;
   private int bonus;
   private boolean left;
   private boolean right;
   private boolean mouseMode;
   private boolean dying;

   public SantaJump() {
      super("santajump");
      Random r = new Random(4L);

      for (float[] s : this.stars) {
         s[0] = r.nextFloat() * 460.0F;
         s[1] = r.nextFloat() * 320.0F;
         s[2] = 0.3F + r.nextFloat() * 0.7F;
      }
   }

   @Override
   protected float boardW() {
      return 460.0F;
   }

   @Override
   protected float boardH() {
      return 320.0F;
   }

   @Override
   protected void reset() {
      this.plats.clear();
      this.gifts.clear();
      this.storms.clear();
      this.fx.clear();
      this.x = 230.0F;
      this.y = -20.0F;
      this.vx = 0.0F;
      this.vy = -440.0F;
      this.cam = this.y - 320.0F + 70.0F;
      this.maxH = 0.0F;
      this.bonus = 0;
      this.combo = 0;
      this.comboT = 0.0F;
      this.squash = 0.0F;
      this.deadT = 0.0F;
      this.hintT = 0.0F;
      this.superT = 0.0F;
      this.left = this.right = this.dying = false;
      SantaJump.Plat g = new SantaJump.Plat();
      g.x = 0.0F;
      g.y = 0.0F;
      g.w = 460.0F;
      g.type = 0;
      this.plats.add(g);
      this.genY = 0.0F;
      this.generate();
   }

   private float height() {
      return Math.max(0.0F, -this.y) / 5.0F;
   }

   private void generate() {
      while (this.genY > this.cam - 80.0F) {
         float h = -this.genY / 5.0F;
         float gap = Math.min(92.0F, 34.0F + h * 0.018F + this.rnd.nextFloat() * (18.0F + Math.min(34.0F, h * 0.01F)));
         this.genY -= gap;
         SantaJump.Plat p = new SantaJump.Plat();
         p.y = this.genY;
         p.x = 6.0F + this.rnd.nextFloat() * 402.0F;
         float r = this.rnd.nextFloat();
         float movP = Math.min(0.35F, h * 4.0E-4F);
         float chimP = 0.07F;
         if (h > 40.0F && r < chimP) {
            p.type = 3;
            p.w = 44.0F;
         } else if (h > 60.0F && r < chimP + movP) {
            p.type = 1;
            p.vx = (this.rnd.nextBoolean() ? 1 : -1) * (30.0F + Math.min(70.0F, h * 0.02F) + this.rnd.nextFloat() * 20.0F);
         } else {
            p.type = 0;
         }

         this.plats.add(p);
         if (p.type != 3 && this.rnd.nextFloat() < 0.22F) {
            SantaJump.Gift gf = new SantaJump.Gift();
            gf.x = p.x + p.w / 2.0F;
            gf.y = p.y - 20.0F - this.rnd.nextFloat() * 16.0F;
            gf.color = this.rnd.nextInt(GIFT_BOX.length);
            gf.gold = this.rnd.nextFloat() < 0.1F;
            gf.t = this.rnd.nextFloat() * 6.0F;
            this.gifts.add(gf);
         }

         if (h > 25.0F && this.rnd.nextFloat() < Math.min(0.4F, 0.1F + h * 3.0E-4F) && gap > 50.0F) {
            SantaJump.Plat d = new SantaJump.Plat();
            d.type = 2;
            d.y = this.genY + gap * 0.5F;
            d.x = 6.0F + this.rnd.nextFloat() * 402.0F;
            this.plats.add(d);
         }

         if (h > 180.0F && this.rnd.nextFloat() < Math.min(0.09F, 0.02F + h * 4.0E-5F)) {
            SantaJump.Storm s = new SantaJump.Storm();
            s.y = this.genY - gap * 0.5F;
            s.x = 30.0F + this.rnd.nextFloat() * 400.0F;
            s.vx = (this.rnd.nextBoolean() ? 1 : -1) * (20.0F + this.rnd.nextFloat() * 30.0F);
            s.t = this.rnd.nextFloat() * 5.0F;
            this.storms.add(s);
         }
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
   protected void update(float dt) {
      this.fx.update(dt);
      this.squash = Math.max(0.0F, this.squash - dt * 5.0F);
      this.comboT = Math.max(0.0F, this.comboT - dt);
      if (this.comboT <= 0.0F) {
         this.combo = 0;
      }

      this.superT = Math.max(0.0F, this.superT - dt);
      this.hintT += dt;
      if (this.dying) {
         this.deadT += dt;
         this.vy = Math.min(900.0F, this.vy + 980.0F * dt);
         this.y = this.y + this.vy * dt;
         if (this.deadT > 0.9F) {
            this.gameOver();
         }
      } else {
         if (Math.abs(this.mx - this.lastMx) > 0.5F && this.mx >= 0.0F) {
            if (this.lastMx > -9999.0F) {
               this.mouseMode = true;
            }

            this.lastMx = this.mx;
         }

         float target;
         if (this.mouseMode && this.u > 0.0F) {
            float mxB = (this.mx - this.bx) / this.u;
            float d = mxB - this.x;
            target = Math.max(-260.0F, Math.min(260.0F, d * 6.0F));
         } else {
            target = ((this.right ? 1 : 0) - (this.left ? 1 : 0)) * 230;
         }

         this.vx = this.vx + (target - this.vx) * Math.min(1.0F, dt * 12.0F);
         if (Math.abs(this.vx) > 20.0F) {
            this.face = Math.signum(this.vx);
         }

         this.x = this.x + this.vx * dt;
         if (this.x < -15.0F) {
            this.x += 490.0F;
         }

         if (this.x > 475.0F) {
            this.x -= 490.0F;
         }

         float prevY = this.y;
         this.vy = Math.min(700.0F, this.vy + 980.0F * dt);
         this.y = this.y + this.vy * dt;
         this.trail += dt;
         if (this.superT > 0.0F && this.trail > 0.03F) {
            this.trail = 0.0F;
            this.fx
               .add(
                  this.x + (this.rnd.nextFloat() - 0.5F) * 12.0F, this.y - 6.0F, 0.0F, 20.0F, 0.6F, 3.0F + this.rnd.nextFloat() * 3.0F, -736942, 2, 0.0F, 1.0F
               );
         }

         for (SantaJump.Plat p : this.plats) {
            p.t += dt;
            if (p.type == 1) {
               p.x = p.x + p.vx * dt;
               if (p.x < 4.0F) {
                  p.x = 4.0F;
                  p.vx = Math.abs(p.vx);
               }

               if (p.x + p.w > 456.0F) {
                  p.x = 456.0F - p.w;
                  p.vx = -Math.abs(p.vx);
               }
            }

            if (p.broken) {
               p.fall += dt;
            } else if (this.vy > 0.0F && prevY <= p.y + 1.0F && this.y >= p.y && this.x + 9.0F > p.x && this.x - 9.0F < p.x + p.w) {
               if (p.type == 2) {
                  p.broken = true;
                  this.fx.burst(p.x + p.w / 2.0F, p.y + 4.0F, 16, -4201473, 110.0F, 3.2F, 0.7F, 420.0F, 1);
                  Sound.play("minecraft:block.glass.break", 1.4F + this.rnd.nextFloat() * 0.2F, 0.25F);
               } else {
                  this.y = p.y;
                  boolean sup = p.type == 3 && Math.abs(this.x - (p.x + p.w * 0.68F)) < 11.0F;
                  this.vy = sup ? -800.0F : -440.0F;
                  this.squash = 1.0F;
                  this.fx
                     .burst(this.x, p.y + 2.0F, sup ? 18 : 7, sup ? -4734770 : -722689, sup ? 90.0F : 60.0F, sup ? 5.0F : 3.0F, 0.55F, sup ? -60.0F : 40.0F, 4);
                  if (sup) {
                     this.superT = 1.1F;
                     this.fx.shake(0.45F);
                     this.fx.ring(this.x, p.y, 34.0F, -736942, 0.45F);
                     this.fx.popup("Schornstein-Turbo!", this.x, p.y - 26.0F, -736942, 10.0F);
                     Sound.play("minecraft:entity.firework_rocket.launch", 1.1F, 0.4F);
                  } else {
                     Sound.play("minecraft:block.snow.step", 1.2F + this.rnd.nextFloat() * 0.3F, 0.4F);
                  }

                  if (!p.used && p != this.plats.get(0)) {
                     p.used = true;
                  }
               }
            }
         }

         for (int i = this.gifts.size() - 1; i >= 0; i--) {
            SantaJump.Gift g = this.gifts.get(i);
            g.t += dt;
            float gy = g.y + (float)Math.sin(g.t * 3.0F) * 2.5F;
            if (Math.abs(g.x - this.x) < 16.0F && Math.abs(gy - (this.y - 14.0F)) < 20.0F) {
               this.gifts.remove(i);
               this.combo = this.comboT > 0.0F ? this.combo + 1 : 1;
               this.comboT = 2.2F;
               int pts = (g.gold ? 150 : 25) * Math.min(this.combo, 8);
               this.bonus += pts;
               this.fx.confetti(g.x, gy, 18, g.gold ? new int[]{-736942, -5720, -1} : new int[]{GIFT_BOX[g.color], GIFT_RIB[g.color], -1}, 170.0F, 4.0F);
               this.fx.ring(g.x, gy, 20.0F, g.gold ? -736942 : -1, 0.35F);
               this.fx
                  .popup(
                     "+" + pts + (this.combo > 1 ? "  x" + Math.min(this.combo, 8) : ""), g.x, gy - 10.0F, g.gold ? -736942 : -1, this.combo > 1 ? 11.0F : 9.5F
                  );
               Sound.play("minecraft:entity.experience_orb.pickup", 1.0F + Math.min(0.9F, this.combo * 0.1F), 0.35F);
            }
         }

         for (SantaJump.Storm s : this.storms) {
            s.t += dt;
            if (s.dead) {
               s.deadT += dt;
               s.y = s.y + 260.0F * dt * s.deadT * 3.0F;
            } else {
               s.x = s.x + s.vx * dt;
               if (s.x < 22.0F || s.x > 438.0F) {
                  s.vx = -s.vx;
               }

               float dx = this.x - s.x;
               float dy = this.y - 14.0F - s.y;
               if (Math.abs(dx) < 22.0F && Math.abs(dy) < 17.0F) {
                  if (this.vy > 0.0F && this.y - 6.0F < s.y) {
                     s.dead = true;
                     this.vy = -484.0F;
                     this.squash = 1.0F;
                     this.bonus += 100;
                     this.fx.burst(s.x, s.y, 22, -9537392, 140.0F, 5.0F, 0.6F, 120.0F, 0);
                     this.fx.popup("+100 Gewitter weg!", s.x, s.y - 16.0F, -6301441, 10.0F);
                     this.fx.shake(0.35F);
                     Sound.play("minecraft:entity.generic.explode", 1.6F, 0.25F);
                  } else {
                     this.die(true);
                  }
               }
            }
         }

         float want = this.y - 147.2F;
         if (want < this.cam) {
            this.cam = this.cam + (want - this.cam) * Math.min(1.0F, dt * 9.0F);
         }

         this.maxH = Math.max(this.maxH, this.height());
         this.score = (long)this.maxH + this.bonus;
         this.generate();
         this.plats.removeIf(p -> p.y - this.cam > 380.0F || p.fall > 1.5F);
         this.gifts.removeIf(g -> g.y - this.cam > 360.0F);
         this.storms.removeIf(sx -> sx.y - this.cam > 380.0F);
         if (this.y - this.cam > 340.0F) {
            this.die(false);
         }
      }
   }

   private void die(boolean zap) {
      if (!this.dying) {
         this.dying = true;
         this.deadT = 0.0F;
         this.fx.shake(0.8F);
         this.fx.flash(zap ? -4201473 : -1890757, 0.8F);
         if (zap) {
            this.vy = -200.0F;
            Sound.play("minecraft:entity.lightning_bolt.thunder", 1.5F, 0.3F);
         } else {
            Sound.play("minecraft:entity.player.hurt", 0.8F, 0.4F);
         }
      }
   }

   @Override
   protected void render() {
      float dt = Math.min(0.05F, Gx.dt);
      this.anim += dt;
      if (this.state == GameView.State.READY) {
         this.y = -20.0F + (float)(-Math.abs(Math.sin(this.anim * 3.0F))) * 70.0F;
         this.cam = -270.0F;
         this.squash = Math.max(0.0F, 1.0F - (float)Math.abs(Math.sin(this.anim * 3.0F)) * 6.0F);
      }

      float alt = Math.max(0.0F, -this.cam) / 5.0F;
      float k1 = Ease.clamp(alt / 600.0F);
      float k2 = Ease.clamp((alt - 600.0F) / 900.0F);
      int top = Gx.mix(Gx.mix(-15852481, -15003846, k1), -16514036, k2);
      int bot = Gx.mix(Gx.mix(-13804390, -11851154, k1), -15594966, k2);
      this.boxV(0.0F, 0.0F, 460.0F, 320.0F, 0.0F, top, bot);
      float starA = 0.35F + 0.65F * Ease.clamp(alt / 300.0F);

      for (float[] s : this.stars) {
         float sy = ((s[1] - this.cam * 0.08F * s[2]) % 320.0F + 320.0F) % 320.0F;
         float tw = 0.6F + 0.4F * (float)Math.sin(this.anim * 2.0F + s[0]);
         this.circle(s[0], sy, 0.6F + s[2] * 0.9F, Gx.withAlpha(-1, starA * tw * s[2]));
      }

      float my = 60.0F - this.cam * 0.02F;
      Gx.glow(this.X(398.0F), this.Y(my), this.S(80.0F), 872412374);
      this.circle(398.0F, my, 20.0F, -2858);
      this.circle(405.0F, my - 6.0F, 4.0F, 402653184);
      this.circle(390.0F, my + 5.0F, 3.0F, 335544320);
      if (alt > 200.0F) {
         float a = Ease.clamp((alt - 200.0F) / 400.0F) * (1.0F - k2 * 0.6F);
         Gx.pushAlpha(a);
         GameFx.sprite(
            this,
            "sj_aurora",
            0.0F,
            20.0F,
            460.0F,
            140.0F,
            460.0F,
            140.0F,
            SantaJump::paintAurora,
            Gx.withAlpha(-1, 0.6F + 0.2F * (float)Math.sin(this.anim * 0.7F))
         );
         Gx.popAlpha();
      }

      float townY = (0.0F - this.cam) * 0.6F + 128.0F - 30.0F;
      if (townY < 330.0F) {
         GameFx.sprite(this, "sj_town", 0.0F, townY - 70.0F, 460.0F, 110.0F, 460.0F, 110.0F, SantaJump::paintTown, -1);
         if (townY + 40.0F < 320.0F) {
            this.box(0.0F, townY + 40.0F, 460.0F, 320.0F - townY - 40.0F, 0.0F, -16050639);
         }
      }

      Gx.push();
      Gx.translate(this.S(this.fx.sx), this.S(this.fx.sy));

      for (SantaJump.Plat p : this.plats) {
         this.plat(p);
      }

      for (SantaJump.Gift g : this.gifts) {
         float gy = g.y + (float)Math.sin(g.t * 3.0F) * 2.5F - this.cam;
         if (!(gy < -30.0F) && !(gy > 350.0F)) {
            Gx.glow(this.X(g.x), this.Y(gy), this.S(18.0F), g.gold ? 1442103634 : 654311423);
            int c = g.gold ? 5 : g.color;
            GameFx.sprite(this, "sj_gift" + c, g.x - 9.0F, gy - 9.0F, 18.0F, 18.0F, 40.0F, 40.0F, gr -> paintGift(gr, c), -1);
         }
      }

      for (SantaJump.Storm s : this.storms) {
         this.storm(s);
      }

      this.fx.draw(this, 0.0F, this.cam);
      this.santa();
      Gx.pop();
      this.fx.drawPopups(this, 0.0F, this.cam);
      this.box(8.0F, 8.0F, 74.0F, 22.0F, 11.0F, 1711276032);
      Gx.icon("sprint", this.X(20.0F), this.Y(19.0F), 10.0F * this.u, -4201473);
      this.text((int)this.maxH + " m", 50.0F, 19.0F, 9.0F, 3, -1);
      if (this.combo > 1 && this.comboT > 0.0F) {
         float a = Math.min(1.0F, this.comboT * 2.0F);
         this.box(374.0F, 8.0F, 78.0F, 22.0F, 11.0F, Gx.withAlpha(-1890757, 0.85F * a));
         this.text("Combo x" + Math.min(this.combo, 8), 413.0F, 19.0F, 8.5F, 3, Gx.withAlpha(-1, a));
         this.box(380.0F, 26.0F, 66.0F * this.comboT / 2.2F, 2.0F, 1.0F, Gx.withAlpha(-1, a * 0.8F));
      }

      if (this.state == GameView.State.PLAYING && this.hintT < 3.5F) {
         float a = Math.min(1.0F, 3.5F - this.hintT) * (0.6F + 0.4F * (float)Math.sin(this.anim * 5.0F));
         this.box(138.0F, 276.0F, 184.0F, 22.0F, 11.0F, Gx.withAlpha(-16777216, 0.4F * a));
         this.text("← → / A D oder Maus zum Lenken", 230.0F, 287.0F, 7.5F, 3, Gx.withAlpha(-1, a));
      }

      this.fx.drawFlash(this, 460.0F, 320.0F);
   }

   private void plat(SantaJump.Plat p) {
      float py = p.y - this.cam + (p.broken ? p.fall * p.fall * 300.0F : 0.0F);
      if (!(py < -30.0F) && !(py > 360.0F)) {
         if (p == this.plats.get(0) && p.w >= 460.0F) {
            this.boxV(0.0F, py, 460.0F, 320.0F, 0.0F, -1510913, -5651222);

            for (float sx = 8.0F; sx < 460.0F; sx += 34.0F) {
               this.circle(sx, py + 2.0F, 10.0F, -722689);
            }
         } else {
            float a = p.broken ? Math.max(0.0F, 1.0F - p.fall * 1.4F) : 1.0F;
            Gx.pushAlpha(a);
            switch (p.type) {
               case 2:
                  GameFx.sprite(this, "sj_ice" + (p.broken ? 1 : 0), p.x, py - 3.0F, p.w, 15.0F, 46.0F, 15.0F, gr -> paintIce(gr, p.broken), -1);
                  break;
               case 3:
                  float pulse = 0.5F + 0.5F * (float)Math.sin(p.t * 5.0F);
                  GameFx.sprite(this, "sj_chim", p.x - 2.0F, py - 26.0F, p.w + 4.0F, 44.0F, 48.0F, 44.0F, SantaJump::paintChimney, -1);
                  Gx.glow(this.X(p.x + p.w * 0.68F), this.Y(py - 22.0F), this.S(14.0F + pulse * 4.0F), Gx.withAlpha(-20434, 0.25F + pulse * 0.2F));
                  if (this.rnd.nextFloat() < 0.15F && this.state == GameView.State.PLAYING) {
                     this.fx
                        .add(
                           p.x + p.w * 0.68F,
                           p.y - 26.0F,
                           (this.rnd.nextFloat() - 0.5F) * 8.0F,
                           -18.0F,
                           1.2F,
                           3.0F + this.rnd.nextFloat() * 2.0F,
                           1724436700,
                           0,
                           -4.0F,
                           0.2F
                        );
                  }
                  break;
               default:
                  float bob = p.type == 1 ? (float)Math.sin(p.t * 4.0F) * 1.2F : 0.0F;
                  GameFx.sprite(this, "sj_cloud" + p.type, p.x - 3.0F, py - 5.0F + bob, p.w + 6.0F, 21.0F, 52.0F, 21.0F, gr -> paintCloud(gr, p.type == 1), -1);
            }

            Gx.popAlpha();
         }
      }
   }

   private void storm(SantaJump.Storm s) {
      float sy = s.y - this.cam;
      if (!(sy < -40.0F) && !(sy > 360.0F)) {
         boolean zap = (int)(s.t * 3.0F) % 4 == 0;
         float a = s.dead ? Math.max(0.0F, 1.0F - s.deadT * 2.0F) : 1.0F;
         Gx.pushAlpha(a);
         if (zap && !s.dead) {
            Gx.glow(this.X(s.x), this.Y(sy + 14.0F), this.S(34.0F), 1153426431);
         }

         GameFx.sprite(
            this,
            "sj_storm" + (zap ? 1 : 0) + (s.dead ? 1 : 0),
            s.x - 25.0F,
            sy - 17.0F,
            50.0F,
            42.0F,
            50.0F,
            42.0F,
            gr -> paintStorm(gr, zap && !s.dead, s.dead),
            -1
         );
         Gx.popAlpha();
      }
   }

   private void santa() {
      float sy = this.y - this.cam;
      float sq = Ease.outCubic(this.squash);
      float stretch = this.vy < -200.0F ? Math.min(0.12F, (-this.vy - 200.0F) / 3000.0F) : 0.0F;
      float w = 30.0F * (1.0F + 0.22F * sq - stretch);
      float h = 30.0F * (1.0F - 0.2F * sq + stretch);
      if (this.superT > 0.0F) {
         Gx.glow(this.X(this.x), this.Y(sy - h / 2.0F), this.S(34.0F), Gx.withAlpha(-736942, this.superT * 0.4F));
      }

      boolean r = this.face > 0.0F;
      if (this.dying) {
         Gx.pushAlpha(Math.max(0.0F, 1.0F - this.deadT));
      }

      GameFx.spriteAt(
         this,
         "sj_santa" + (r ? 1 : 0) + (this.dying ? 1 : 0),
         30.0F,
         30.0F,
         100.0F,
         100.0F,
         gr -> paintSanta(gr, r, this.dying),
         this.x - w / 2.0F,
         sy - h,
         w,
         h,
         -1
      );
      if (this.x < 15.0F) {
         GameFx.spriteAt(
            this,
            "sj_santa" + (r ? 1 : 0) + (this.dying ? 1 : 0),
            30.0F,
            30.0F,
            100.0F,
            100.0F,
            gr -> paintSanta(gr, r, this.dying),
            this.x + 460.0F - w / 2.0F,
            sy - h,
            w,
            h,
            -1
         );
      }

      if (this.x > 445.0F) {
         GameFx.spriteAt(
            this,
            "sj_santa" + (r ? 1 : 0) + (this.dying ? 1 : 0),
            30.0F,
            30.0F,
            100.0F,
            100.0F,
            gr -> paintSanta(gr, r, this.dying),
            this.x - 460.0F - w / 2.0F,
            sy - h,
            w,
            h,
            -1
         );
      }

      if (this.dying) {
         Gx.popAlpha();
      }
   }

   private static void paintSanta(Graphics2D g, boolean right, boolean hurt) {
      if (!right) {
         g.translate(100, 0);
         g.scale(-1.0, 1.0);
      }

      Color red = new Color(14886459);
      Color redD = new Color(10358820);
      Color fur = new Color(16054527);
      Color furD = new Color(13227754);
      g.setStroke(new BasicStroke(2.2F));
      g.setColor(new Color(1776418));
      g.fill(new Float(28.0F, 86.0F, 18.0F, 13.0F, 7.0F, 7.0F));
      g.fill(new Float(54.0F, 86.0F, 20.0F, 13.0F, 7.0F, 7.0F));
      g.setPaint(new GradientPaint(0.0F, 40.0F, new Color(10514492), 0.0F, 80.0F, new Color(7226914)));
      g.fill(new java.awt.geom.Ellipse2D.Float(6.0F, 44.0F, 30.0F, 36.0F));
      g.setColor(new Color(5913114));
      g.fill(new Float(14.0F, 40.0F, 14.0F, 7.0F, 4.0F, 4.0F));
      g.setPaint(new GradientPaint(0.0F, 44.0F, red, 0.0F, 92.0F, redD));
      g.fill(new Float(22.0F, 44.0F, 58.0F, 48.0F, 30.0F, 30.0F));
      g.setPaint(new GradientPaint(0.0F, 80.0F, fur, 0.0F, 92.0F, furD));
      g.fill(new Float(23.0F, 80.0F, 56.0F, 10.0F, 10.0F, 10.0F));
      g.setColor(new Color(1447452));
      g.fill(new java.awt.geom.Rectangle2D.Float(22.0F, 66.0F, 58.0F, 8.0F));
      g.setColor(new Color(16040274));
      g.setStroke(new BasicStroke(2.6F));
      g.draw(new java.awt.geom.Rectangle2D.Float(54.0F, 64.5F, 11.0F, 11.0F));
      g.setColor(fur);
      g.fill(new java.awt.geom.Rectangle2D.Float(49.0F, 44.0F, 6.0F, 22.0F));
      g.setPaint(new GradientPaint(70.0F, 44.0F, red, 90.0F, 60.0F, redD));
      g.fill(new Float(70.0F, 46.0F, 16.0F, 26.0F, 14.0F, 14.0F));
      g.setColor(fur);
      g.fill(new Float(69.0F, 66.0F, 18.0F, 7.0F, 6.0F, 6.0F));
      g.setColor(new Color(2073436));
      g.fill(new java.awt.geom.Ellipse2D.Float(70.0F, 70.0F, 15.0F, 13.0F));
      g.setPaint(
         new RadialGradientPaint(
            new java.awt.geom.Point2D.Float(55.0F, 28.0F), 22.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(16767420), new Color(15314572)}
         )
      );
      g.fill(new java.awt.geom.Ellipse2D.Float(36.0F, 14.0F, 36.0F, 34.0F));
      GeneralPath b = new GeneralPath();
      b.moveTo(34.0F, 32.0F);
      b.curveTo(34.0F, 62.0F, 50.0F, 70.0F, 56.0F, 70.0F);
      b.curveTo(66.0F, 70.0F, 78.0F, 60.0F, 76.0F, 32.0F);
      b.curveTo(70.0F, 40.0F, 62.0F, 42.0F, 56.0F, 40.0F);
      b.curveTo(48.0F, 42.0F, 40.0F, 40.0F, 34.0F, 32.0F);
      b.closePath();
      g.setPaint(new GradientPaint(0.0F, 32.0F, fur, 0.0F, 70.0F, furD));
      g.fill(b);
      g.setColor(Color.WHITE);
      g.fill(new java.awt.geom.Ellipse2D.Float(47.0F, 34.0F, 12.0F, 8.0F));
      g.fill(new java.awt.geom.Ellipse2D.Float(58.0F, 34.0F, 12.0F, 8.0F));
      g.setColor(new Color(15764090));
      g.fill(new java.awt.geom.Ellipse2D.Float(55.0F, 29.0F, 8.0F, 8.0F));
      g.setColor(new Color(1776418));
      if (hurt) {
         g.setStroke(new BasicStroke(2.4F, 1, 1));
         g.drawLine(47, 23, 53, 29);
         g.drawLine(53, 23, 47, 29);
         g.drawLine(63, 23, 69, 29);
         g.drawLine(69, 23, 63, 29);
      } else {
         g.fill(new java.awt.geom.Ellipse2D.Float(48.0F, 23.0F, 6.0F, 7.0F));
         g.fill(new java.awt.geom.Ellipse2D.Float(64.0F, 23.0F, 6.0F, 7.0F));
         g.setColor(Color.WHITE);
         g.fill(new java.awt.geom.Ellipse2D.Float(50.0F, 24.0F, 2.0F, 2.0F));
         g.fill(new java.awt.geom.Ellipse2D.Float(66.0F, 24.0F, 2.0F, 2.0F));
      }

      g.setColor(new Color(255, 120, 120, 90));
      g.fill(new java.awt.geom.Ellipse2D.Float(41.0F, 30.0F, 8.0F, 5.0F));
      GeneralPath h = new GeneralPath();
      h.moveTo(36.0F, 20.0F);
      h.curveTo(38.0F, 2.0F, 58.0F, -2.0F, 70.0F, 8.0F);
      h.curveTo(76.0F, 12.0F, 74.0F, 18.0F, 72.0F, 20.0F);
      h.closePath();
      g.setPaint(new GradientPaint(40.0F, 0.0F, red, 70.0F, 20.0F, redD));
      g.fill(h);
      GeneralPath tip = new GeneralPath();
      tip.moveTo(40.0F, 12.0F);
      tip.curveTo(30.0F, 4.0F, 18.0F, 6.0F, 12.0F, 16.0F);
      tip.lineTo(20.0F, 20.0F);
      tip.curveTo(26.0F, 14.0F, 32.0F, 14.0F, 40.0F, 18.0F);
      tip.closePath();
      g.fill(tip);
      g.setColor(fur);
      g.fill(new java.awt.geom.Ellipse2D.Float(4.0F, 12.0F, 14.0F, 14.0F));
      g.setPaint(new GradientPaint(0.0F, 14.0F, fur, 0.0F, 24.0F, furD));
      g.fill(new Float(32.0F, 15.0F, 44.0F, 10.0F, 10.0F, 10.0F));
   }

   private static void paintCloud(Graphics2D g, boolean moving) {
      Color top = moving ? new Color(16773592) : new Color(16777215);
      Color bot = moving ? new Color(14924175) : new Color(12111082);
      Shape p = GameFx.union(
         new java.awt.geom.Ellipse2D.Float(2.0F, 6.0F, 18.0F, 14.0F),
         new java.awt.geom.Ellipse2D.Float(12.0F, 1.0F, 18.0F, 16.0F),
         new java.awt.geom.Ellipse2D.Float(24.0F, 2.0F, 16.0F, 15.0F),
         new java.awt.geom.Ellipse2D.Float(33.0F, 6.0F, 17.0F, 14.0F),
         new Float(5.0F, 9.0F, 42.0F, 11.0F, 10.0F, 10.0F)
      );
      g.setColor(new Color(20, 30, 70, 60));
      g.translate(0.0, 1.2);
      g.fill(p);
      g.translate(0.0, -1.2);
      g.setPaint(new GradientPaint(0.0F, 2.0F, top, 0.0F, 20.0F, bot));
      g.fill(p);
      g.setColor(new Color(255, 255, 255, 200));
      g.fill(new java.awt.geom.Ellipse2D.Float(15.0F, 3.5F, 9.0F, 4.0F));
      g.fill(new java.awt.geom.Ellipse2D.Float(27.0F, 4.5F, 7.0F, 3.0F));
      if (moving) {
         g.setColor(new Color(14886459));
         g.fill(new java.awt.geom.Ellipse2D.Float(8.0F, 15.0F, 3.0F, 3.0F));
         g.setColor(new Color(2073436));
         g.fill(new java.awt.geom.Ellipse2D.Float(25.0F, 16.0F, 3.0F, 3.0F));
         g.setColor(new Color(16040274));
         g.fill(new java.awt.geom.Ellipse2D.Float(40.0F, 15.0F, 3.0F, 3.0F));
      }
   }

   private static void paintIce(Graphics2D g, boolean broken) {
      RoundRectangle2D r = new Float(1.0F, 3.0F, 44.0F, 10.0F, 5.0F, 5.0F);
      g.setPaint(new GradientPaint(0.0F, 3.0F, new Color(220, 242, 255, 230), 0.0F, 13.0F, new Color(120, 180, 235, 220)));
      g.fill(r);
      g.setColor(new Color(255, 255, 255, 200));
      g.fill(new Float(4.0F, 4.0F, 36.0F, 2.2F, 2.0F, 2.0F));
      g.setColor(new Color(70, 130, 200, 200));
      g.setStroke(new BasicStroke(0.9F));
      g.drawLine(12, 4, 16, 9);
      g.drawLine(16, 9, 14, 13);
      g.drawLine(28, 3, 25, 8);
      g.drawLine(25, 8, 30, 12);
      g.drawLine(38, 5, 36, 10);
      g.draw(r);
      g.setColor(new Color(200, 232, 255, 220));

      for (int i = 0; i < 5; i++) {
         int x = 6 + i * 8;
         g.fillPolygon(new int[]{x, x + 3, x + 1}, new int[]{13, 13, 15 + i % 2 * 1}, 3);
      }
   }

   private static void paintChimney(Graphics2D g) {
      g.setPaint(new GradientPaint(0.0F, 26.0F, new Color(9059118), 0.0F, 42.0F, new Color(5906969)));
      g.fill(new Float(2.0F, 28.0F, 44.0F, 14.0F, 4.0F, 4.0F));
      g.setColor(new Color(4068879));

      for (int i = 0; i < 6; i++) {
         g.fillRect(4 + i * 7, 35, 1, 6);
      }

      g.setPaint(new GradientPaint(0.0F, 24.0F, Color.WHITE, 0.0F, 32.0F, new Color(13228786)));
      g.fill(new Float(0.0F, 24.0F, 48.0F, 7.0F, 7.0F, 7.0F));
      g.setPaint(new GradientPaint(26.0F, 0.0F, new Color(11684408), 40.0F, 0.0F, new Color(8006174)));
      g.fill(new java.awt.geom.Rectangle2D.Float(26.0F, 6.0F, 14.0F, 20.0F));
      g.setColor(new Color(5905940));
      g.setStroke(new BasicStroke(0.8F));

      for (int r = 0; r < 4; r++) {
         g.drawLine(26, 10 + r * 5, 40, 10 + r * 5);
         int off = r % 2 == 0 ? 33 : 30;
         g.drawLine(off, 10 + r * 5, off, 15 + r * 5);
      }

      g.setColor(new Color(3805708));
      g.fill(new java.awt.geom.Rectangle2D.Float(24.0F, 3.0F, 18.0F, 5.0F));
      g.setColor(Color.WHITE);
      g.fill(new Float(23.0F, 1.0F, 20.0F, 4.0F, 4.0F, 4.0F));
      g.setColor(new Color(16765562));
      g.fillPolygon(new int[]{33, 29, 37}, new int[]{-1, 3, 3}, 3);
   }

   private static void paintStorm(Graphics2D g, boolean zap, boolean dead) {
      Shape p = GameFx.union(
         new java.awt.geom.Ellipse2D.Float(3.0F, 12.0F, 20.0F, 16.0F),
         new java.awt.geom.Ellipse2D.Float(13.0F, 4.0F, 22.0F, 20.0F),
         new java.awt.geom.Ellipse2D.Float(27.0F, 10.0F, 20.0F, 17.0F),
         new Float(6.0F, 16.0F, 38.0F, 12.0F, 11.0F, 11.0F)
      );
      g.setPaint(new GradientPaint(0.0F, 4.0F, dead ? new Color(9213094) : new Color(5989240), 0.0F, 28.0F, dead ? new Color(5922928) : new Color(2764606)));
      g.fill(p);
      g.setColor(new Color(255, 255, 255, 40));
      g.fill(new java.awt.geom.Ellipse2D.Float(17.0F, 7.0F, 10.0F, 4.0F));
      g.setColor(Color.WHITE);
      g.fill(new java.awt.geom.Ellipse2D.Float(16.0F, 14.0F, 7.0F, 7.0F));
      g.fill(new java.awt.geom.Ellipse2D.Float(28.0F, 14.0F, 7.0F, 7.0F));
      g.setColor(new Color(1119002));
      g.fill(new java.awt.geom.Ellipse2D.Float(18.5F, 16.5F, 3.4F, 3.4F));
      g.fill(new java.awt.geom.Ellipse2D.Float(29.5F, 16.5F, 3.4F, 3.4F));
      g.setStroke(new BasicStroke(1.8F, 1, 1));
      g.drawLine(15, 12, 23, 15);
      g.drawLine(36, 12, 28, 15);
      g.drawArc(21, 22, 9, 5, 0, 180);
      if (zap) {
         GeneralPath l = new GeneralPath();
         l.moveTo(24.0F, 27.0F);
         l.lineTo(19.0F, 34.0F);
         l.lineTo(24.0F, 34.0F);
         l.lineTo(20.0F, 42.0F);
         l.lineTo(30.0F, 32.0F);
         l.lineTo(25.0F, 32.0F);
         l.lineTo(29.0F, 27.0F);
         l.closePath();
         g.setColor(new Color(16770667));
         g.fill(l);
      }
   }

   static void paintGift(Graphics2D g, int c) {
      int box = c == 5 ? -736942 : GIFT_BOX[c];
      int rib = c == 5 ? -1 : GIFT_RIB[c];
      g.setPaint(new GradientPaint(0.0F, 12.0F, new Color(Gx.mix(box, -1, 0.2F)), 0.0F, 38.0F, new Color(Gx.mix(box, -16777216, 0.3F))));
      g.fill(new Float(5.0F, 14.0F, 30.0F, 24.0F, 5.0F, 5.0F));
      g.setPaint(new GradientPaint(0.0F, 9.0F, new Color(Gx.mix(box, -1, 0.3F)), 0.0F, 17.0F, new Color(box)));
      g.fill(new Float(3.0F, 10.0F, 34.0F, 8.0F, 4.0F, 4.0F));
      g.setColor(new Color(rib));
      g.fill(new java.awt.geom.Rectangle2D.Float(17.0F, 10.0F, 6.0F, 28.0F));
      g.setStroke(new BasicStroke(3.2F));
      g.drawOval(9, 2, 10, 9);
      g.drawOval(21, 2, 10, 9);
      g.setColor(new Color(255, 255, 255, 90));
      g.fill(new java.awt.geom.Rectangle2D.Float(7.0F, 19.0F, 3.0F, 16.0F));
   }

   private static void paintTown(Graphics2D g) {
      Random r = new Random(9L);
      g.setColor(new Color(1254471));
      g.fill(new java.awt.geom.Ellipse2D.Float(-60.0F, 40.0F, 220.0F, 120.0F));
      g.fill(new java.awt.geom.Ellipse2D.Float(130.0F, 30.0F, 240.0F, 140.0F));
      g.fill(new java.awt.geom.Ellipse2D.Float(250.0F, 44.0F, 260.0F, 120.0F));

      for (int i = 0; i < 14; i++) {
         float tx = 8 + i * 33 + r.nextFloat() * 10.0F;
         float ty = 56.0F + r.nextFloat() * 10.0F;
         float s = 0.7F + r.nextFloat() * 0.5F;
         g.setColor(new Color(932660));
         g.fillPolygon(
            new int[]{(int)tx, (int)(tx - 10.0F * s), (int)(tx + 10.0F * s)}, new int[]{(int)(ty - 26.0F * s), (int)(ty + 6.0F), (int)(ty + 6.0F)}, 3
         );
         g.setColor(new Color(220, 235, 255, 180));
         g.fillPolygon(
            new int[]{(int)tx, (int)(tx - 4.0F * s), (int)(tx + 4.0F * s)}, new int[]{(int)(ty - 26.0F * s), (int)(ty - 16.0F * s), (int)(ty - 16.0F * s)}, 3
         );
      }

      float[] hx = new float[]{10.0F, 70.0F, 150.0F, 215.0F, 262.0F, 312.0F, 366.0F, 414.0F};

      for (int i = 0; i < hx.length; i++) {
         float w = 38.0F + r.nextFloat() * 14.0F;
         float h = 26.0F + r.nextFloat() * 18.0F;
         float x = hx[i];
         float y = 110.0F - h;
         g.setColor(new Color(1715026));
         g.fill(new java.awt.geom.Rectangle2D.Float(x, y, w, h));
         GeneralPath roof = new GeneralPath();
         roof.moveTo(x - 4.0F, y);
         roof.lineTo(x + w / 2.0F, y - 18.0F);
         roof.lineTo(x + w + 4.0F, y);
         roof.closePath();
         g.setColor(new Color(2373478));
         g.fill(roof);
         g.setColor(new Color(15397631));
         g.setStroke(new BasicStroke(3.0F, 1, 1));
         g.drawLine((int)(x - 3.0F), (int)y, (int)(x + w / 2.0F), (int)(y - 17.0F));
         g.drawLine((int)(x + w / 2.0F), (int)(y - 17.0F), (int)(x + w + 3.0F), (int)y);

         for (int wi = 0; wi < 2; wi++) {
            float wx = x + 7.0F + wi * (w - 21.0F);
            float wy = y + 7.0F;
            g.setPaint(
               new RadialGradientPaint(
                  new java.awt.geom.Point2D.Float(wx + 3.5F, wy + 4.0F),
                  12.0F,
                  new float[]{0.0F, 1.0F},
                  new Color[]{new Color(255, 200, 90, 120), new Color(255, 200, 90, 0)}
               )
            );
            g.fill(new java.awt.geom.Ellipse2D.Float(wx - 8.0F, wy - 7.0F, 23.0F, 23.0F));
            g.setColor(new Color(16765562));
            g.fill(new java.awt.geom.Rectangle2D.Float(wx, wy, 7.0F, 8.0F));
         }
      }

      g.setPaint(new GradientPaint(0.0F, 104.0F, new Color(14543355), 0.0F, 110.0F, new Color(11125994)));
      g.fill(new java.awt.geom.Rectangle2D.Float(0.0F, 106.0F, 460.0F, 4.0F));
   }

   private static void paintAurora(Graphics2D g) {
      int[][] cols = new int[][]{{60, 255, 170}, {90, 200, 255}, {190, 120, 255}};

      for (int band = 0; band < 3; band++) {
         int[] c = cols[band];

         for (int x = 0; x < 460; x += 2) {
            double y = 50 + band * 24 + Math.sin(x / 45.0 + band * 1.7) * 18.0 + Math.sin(x / 17.0 + band) * 4.0;
            int a = (int)(80.0 * Math.sin(Math.PI * x / 460.0));
            g.setPaint(new GradientPaint(x, (float)(y - 40.0), new Color(c[0], c[1], c[2], 0), x, (float)y, new Color(c[0], c[1], c[2], a)));
            g.fillRect(x, (int)(y - 40.0), 2, 40);
            g.setPaint(new GradientPaint(x, (float)y, new Color(c[0], c[1], c[2], a), x, (float)(y + 10.0), new Color(c[0], c[1], c[2], 0)));
            g.fillRect(x, (int)y, 2, 10);
         }
      }
   }

   private static final class Gift {
      float x;
      float y;
      float t;
      int color;
      boolean gold;
   }

   private static final class Plat {
      float x;
      float y;
      float w = 46.0F;
      float vx;
      float t;
      float fall;
      int type;
      boolean broken;
      boolean used;
   }

   private static final class Storm {
      float x;
      float y;
      float vx;
      float t;
      boolean dead;
      float deadT;
   }
}
