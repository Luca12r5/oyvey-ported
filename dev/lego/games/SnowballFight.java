package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Point2D.Float;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class SnowballFight extends GameView {
   private static final float W = 400.0F;
   private static final float H = 270.0F;
   private static final float G = 400.0F;
   private static final float BALL = 3.6F;
   private static final float ROUND = 60.0F;
   private static final float OX = 64.0F;
   private static final float OY = 192.0F;
   private static final float[] SLOT_X = new float[]{150.0F, 222.0F, 294.0F, 360.0F};
   private static final float[] SLOT_G = new float[]{238.0F, 222.0F, 204.0F, 184.0F};
   private static final float WALL_W = 38.0F;
   private static final float WALL_H = 20.0F;
   private static final float ELF_H = 30.0F;
   private final SnowballFight.Elf[] elves = new SnowballFight.Elf[4];
   private final List<SnowballFight.Ball> balls = new ArrayList<>();
   private final GameFx fx = new GameFx();
   private final float[][] splats = new float[6][4];
   private SnowballFight.Balloon balloon;
   private float spawnT;
   private float reload;
   private float throwAnim;
   private float anim;
   private float balloonT;
   private float hintT;
   private float dragX;
   private float dragY;
   private float curX;
   private float curY;
   private boolean aiming;
   private int combo;
   private int hits;
   private int throwsN;
   private int bestCombo;

   public SnowballFight() {
      super("snowball");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 270.0F;
   }

   @Override
   protected String overTitle() {
      return "Zeit um!";
   }

   @Override
   protected void reset() {
      for (int i = 0; i < this.elves.length; i++) {
         this.elves[i] = null;
      }

      this.balls.clear();
      this.fx.clear();

      for (float[] s : this.splats) {
         s[3] = 0.0F;
      }

      this.balloon = null;
      this.spawnT = 0.8F;
      this.reload = 0.0F;
      this.throwAnim = 0.0F;
      this.balloonT = 9.0F;
      this.hintT = 0.0F;
      this.aiming = false;
      this.combo = this.hits = this.throwsN = this.bestCombo = 0;
   }

   private float left() {
      return Math.max(0.0F, 60.0F - this.time);
   }

   private int mult() {
      return Math.min(5, 1 + this.combo / 3);
   }

   private static float groundAt(float x) {
      float g = 246.0F;

      for (int i = 0; i < SLOT_X.length; i++) {
         float dx = (x - SLOT_X[i]) / 62.0F;
         if (Math.abs(dx) < 1.6F) {
            g = Math.min(g, SLOT_G[i] + dx * dx * 22.0F);
         }
      }

      if (x < 92.0F) {
         g = Math.min(g, 240.0F);
      }

      return g;
   }

   @Override
   protected void onClick(float x, float y, int button) {
      if (button == 0) {
         this.aiming = true;
         this.dragX = this.curX = x;
         this.dragY = this.curY = y;
      }
   }

   @Override
   protected void onDrag(float x, float y) {
      if (this.aiming) {
         this.curX = x;
         this.curY = y;
      }
   }

   @Override
   protected void onRelease(float x, float y, int button) {
      if (this.aiming) {
         this.aiming = false;
         this.curX = x;
         this.curY = y;
         float[] v = this.launchVel();
         if (v != null) {
            if (!(this.reload > 0.0F)) {
               SnowballFight.Ball b = new SnowballFight.Ball();
               b.x = 64.0F;
               b.y = 192.0F;
               b.vx = v[0];
               b.vy = v[1];
               this.balls.add(b);
               this.reload = 0.32F;
               this.throwAnim = 1.0F;
               this.throwsN++;
               Sound.play("minecraft:entity.snowball.throw", 0.8F + v[2] * 0.4F, 0.5F);
            }
         }
      }
   }

   private float[] launchVel() {
      float dx = this.dragX - this.curX;
      float dy = this.dragY - this.curY;
      float len = (float)Math.sqrt(dx * dx + dy * dy);
      if (len < 6.0F) {
         return null;
      } else {
         float p = Math.min(1.0F, len / 90.0F);
         float sp = 150.0F + 300.0F * p;
         return new float[]{dx / len * sp, dy / len * sp, p};
      }
   }

   @Override
   protected void update(float dt) {
      this.fx.update(dt);
      this.hintT += dt;
      this.reload = Math.max(0.0F, this.reload - dt);
      this.throwAnim = Math.max(0.0F, this.throwAnim - dt * 4.0F);

      for (float[] s : this.splats) {
         s[3] = Math.max(0.0F, s[3] - dt * 0.55F);
      }

      if (this.left() <= 0.0F) {
         this.gameOver();
      } else {
         float diff = Ease.clamp(this.time / 60.0F);
         this.spawnT -= dt;
         if (this.spawnT <= 0.0F) {
            this.spawnT = Ease.lerp(1.1F, 0.5F, diff) * (0.7F + this.rnd.nextFloat() * 0.6F);
            int s = this.rnd.nextInt(4);
            if (this.elves[s] == null) {
               SnowballFight.Elf e = new SnowballFight.Elf();
               e.slot = s;
               e.stay = Ease.lerp(2.0F, 1.05F, diff) * (0.8F + this.rnd.nextFloat() * 0.4F);
               float r = this.rnd.nextFloat();
               e.gold = r < 0.08F;
               e.deer = !e.gold && r < 0.2F;
               this.elves[s] = e;
            }
         }

         for (int i = 0; i < this.elves.length; i++) {
            SnowballFight.Elf e = this.elves[i];
            if (e != null) {
               e.t += dt;
               if (e.hitT >= 0.0F) {
                  e.hitT += dt;
                  e.up = Math.max(0.0F, e.up - dt * 3.0F);
                  if (e.hitT > 0.6F) {
                     this.elves[i] = null;
                  }
               } else {
                  if (e.t < 0.22F) {
                     e.up = Ease.outBack(e.t / 0.22F);
                  } else if (e.t < 0.22F + e.stay) {
                     e.up = 1.0F;
                  } else {
                     e.up = Math.max(0.0F, 1.0F - (e.t - 0.22F - e.stay) / 0.2F);
                     if (e.up <= 0.0F) {
                        this.elves[i] = null;
                        continue;
                     }
                  }

                  if (!e.deer && !e.thrown && e.t > 0.22F + e.stay * 0.55F && this.rnd.nextFloat() < 0.012F + diff * 0.02F) {
                     e.thrown = true;
                     SnowballFight.Ball b = new SnowballFight.Ball();
                     b.enemy = true;
                     b.x = SLOT_X[i] + 6.0F;
                     b.y = SLOT_G[i] - 20.0F - 18.0F;
                     float T = 1.25F - diff * 0.25F;
                     float tx = 58.0F;
                     float ty = 186.0F;
                     b.vx = (tx - b.x) / T;
                     b.vy = (ty - b.y) / T - 200.0F * T;
                     this.balls.add(b);
                     Sound.play("minecraft:entity.snowball.throw", 1.3F, 0.3F);
                  }
               }
            }
         }

         this.balloonT -= dt;
         if (this.balloon == null && this.balloonT <= 0.0F) {
            this.balloon = new SnowballFight.Balloon();
            this.balloon.x = 420.0F;
            this.balloon.y = 40.0F + this.rnd.nextFloat() * 40.0F;
            this.balloonT = 12.0F + this.rnd.nextFloat() * 8.0F;
         }

         if (this.balloon != null) {
            this.balloon.t += dt;
            if (this.balloon.hit) {
               this.balloon.hitT += dt;
               this.balloon.y = this.balloon.y + 90.0F * dt * (1.0F + this.balloon.hitT * 4.0F);
               if (this.balloon.hitT > 1.0F) {
                  this.balloon = null;
               }
            } else {
               this.balloon.x -= 38.0F * dt;
               if (this.balloon.x < -30.0F) {
                  this.balloon = null;
               }
            }
         }

         for (int ix = this.balls.size() - 1; ix >= 0; ix--) {
            SnowballFight.Ball b = this.balls.get(ix);
            b.t += dt;
            b.vy += 400.0F * dt;
            b.x = b.x + b.vx * dt;
            b.y = b.y + b.vy * dt;
            if (!b.enemy && b.t % 0.05F < dt) {
               this.fx.add(b.x, b.y, 0.0F, 0.0F, 0.25F, 1.6F, -1996488705, 0, 0.0F, 0.0F);
            }

            if (this.collide(b)) {
               this.balls.remove(ix);
            } else if (b.x > 420.0F || b.x < -20.0F || b.y > 290.0F) {
               this.balls.remove(ix);
               if (!b.enemy) {
                  this.miss();
               }
            }
         }
      }
   }

   private boolean collide(SnowballFight.Ball b) {
      if (b.enemy) {
         if (b.x < 84.0F && b.y > 168.0F) {
            this.splat(b.x, b.y);

            for (float[] s : this.splats) {
               if (s[3] <= 0.05F) {
                  s[0] = 60.0F + this.rnd.nextFloat() * 280.0F;
                  s[1] = 50.0F + this.rnd.nextFloat() * 160.0F;
                  s[2] = 38.0F + this.rnd.nextFloat() * 26.0F;
                  s[3] = 1.0F;
                  break;
               }
            }

            this.time += 3.0F;
            this.fx.popup("-3 s", 70.0F, 160.0F, -30070, 12.0F);
            this.fx.shake(0.6F);
            this.combo = 0;
            Sound.play("minecraft:block.snow.break", 0.6F, 0.9F);
            return true;
         }

         for (SnowballFight.Ball o : this.balls) {
            if (!o.enemy && o != b) {
               float dx = o.x - b.x;
               float dy = o.y - b.y;
               if (dx * dx + dy * dy < 81.0F) {
                  o.t = 99.0F;
                  o.x = -999.0F;
                  this.score = this.score + 5L * this.mult();
                  this.fx.burst(b.x, b.y, 18, -1, 130.0F, 3.0F, 0.5F, 200.0F, 4);
                  this.fx.ring(b.x, b.y, 18.0F, -6301441, 0.3F);
                  this.fx.popup("Abgefangen! +" + 5 * this.mult(), b.x, b.y - 8.0F, -6301441, 9.0F);
                  Sound.play("minecraft:block.powder_snow.break", 1.4F, 0.6F);
                  return true;
               }
            }
         }
      } else {
         for (int i = 0; i < this.elves.length; i++) {
            SnowballFight.Elf e = this.elves[i];
            if (e != null && !(e.hitT >= 0.0F) && !(e.up < 0.35F)) {
               float top = SLOT_G[i] - 20.0F - 30.0F * e.up;
               if (b.x > SLOT_X[i] - 11.0F && b.x < SLOT_X[i] + 11.0F && b.y > top - 2.0F && b.y < SLOT_G[i] - 20.0F + 2.0F) {
                  this.hitElf(e, b);
                  return true;
               }
            }
         }

         if (this.balloon != null && !this.balloon.hit) {
            float dx = b.x - this.balloon.x;
            float dy = b.y - (this.balloon.y - 6.0F);
            if (dx * dx + dy * dy < 150.0F) {
               this.balloon.hit = true;
               this.combo++;
               int pts = 40 * this.mult();
               this.score += pts;
               this.fx.confetti(this.balloon.x, this.balloon.y, 30, new int[]{-1890757, -736942, -14703780, -1}, 170.0F, 4.0F);
               this.fx.popup("Ballon! +" + pts, this.balloon.x, this.balloon.y - 14.0F, -736942, 12.0F);
               this.fx.shake(0.3F);
               Sound.play("minecraft:entity.firework_rocket.blast", 1.2F, 0.5F);
               return true;
            }
         }
      }

      for (int ix = 0; ix < SLOT_X.length; ix++) {
         float wx = SLOT_X[ix] - 19.0F;
         float wy = SLOT_G[ix] - 20.0F;
         if (b.x > wx && b.x < wx + 38.0F && b.y > wy && b.y < SLOT_G[ix] + 6.0F) {
            this.splat(b.x, b.y);
            if (!b.enemy) {
               this.miss();
            }

            return true;
         }
      }

      if (b.x > 20.0F && b.x < 84.0F && b.y > 196.0F && !b.enemy) {
         return false;
      } else if (b.y > groundAt(b.x)) {
         this.splat(b.x, groundAt(b.x));
         if (!b.enemy) {
            this.miss();
         }

         return true;
      } else {
         return false;
      }
   }

   private void hitElf(SnowballFight.Elf e, SnowballFight.Ball b) {
      e.hitT = 0.0F;
      float x = SLOT_X[e.slot];
      float y = SLOT_G[e.slot] - 20.0F - 18.0F;
      this.fx.burst(b.x, b.y, 16, -1, 140.0F, 3.2F, 0.55F, 250.0F, 4);
      if (e.deer) {
         this.combo = 0;
         this.score = Math.max(0L, this.score - 30L);
         this.fx.popup("Nicht das Rentier! -30", x, y - 16.0F, -30070, 10.0F);
         this.fx.shake(0.4F);
         Sound.play("minecraft:entity.goat.hurt", 1.2F, 0.5F);
      } else {
         this.combo++;
         this.hits++;
         this.bestCombo = Math.max(this.bestCombo, this.combo);
         int base = 10 + e.slot * 5 + (e.gold ? 40 : 0);
         int pts = base * this.mult();
         this.score += pts;
         if (e.gold) {
            this.time -= 3.0F;
            this.fx.popup("+3 s", x, y - 30.0F, -6291531, 10.0F);
         }

         this.fx.popup("+" + pts + (this.mult() > 1 ? " x" + this.mult() : ""), x, y - 14.0F, e.gold ? -736942 : -1, 11.0F);
         this.fx.burst(x, y - 6.0F, 8, e.gold ? -736942 : -8090, 90.0F, 2.2F, 0.6F, 0.0F, 2);
         this.fx.shake(0.18F);
         if (this.combo % 5 == 0) {
            this.fx.popup(this.combo + "er Serie!", 200.0F, 60.0F, -33835, 15.0F);
            Sound.play("minecraft:entity.player.levelup", 1.4F, 0.3F);
         }

         Sound.play("minecraft:entity.player.hurt_freeze", 1.3F, 0.5F);
         Sound.play("minecraft:entity.experience_orb.pickup", 1.0F + Math.min(1.0F, this.combo * 0.05F), 0.3F);
      }
   }

   private void splat(float x, float y) {
      this.fx.burst(x, y, 10, -1, 90.0F, 2.8F, 0.45F, 300.0F, 4);
      Sound.play("minecraft:block.snow.hit", 1.2F, 0.4F);
   }

   private void miss() {
      this.combo = 0;
   }

   @Override
   protected void render() {
      float dt = Math.min(0.05F, Gx.dt);
      this.anim += dt;
      GameFx.sprite(this, "sb_bg", 0.0F, 0.0F, 400.0F, 270.0F, 400.0F, 270.0F, SnowballFight::paintSky, -1);
      if (this.balloon != null) {
         float by0 = this.balloon.y + (float)Math.sin(this.balloon.t * 2.0F) * 4.0F;
         Gx.pushAlpha(this.balloon.hit ? Math.max(0.0F, 1.0F - this.balloon.hitT) : 1.0F);
         GameFx.sprite(
            this,
            "sb_balloon" + (this.balloon.hit ? 1 : 0),
            this.balloon.x - 12.0F,
            by0 - 22.0F,
            24.0F,
            40.0F,
            24.0F,
            40.0F,
            gr -> paintBalloon(gr, this.balloon.hit),
            -1
         );
         Gx.popAlpha();
      }

      Gx.push();
      Gx.translate(this.S(this.fx.sx), this.S(this.fx.sy));

      for (int i = SLOT_X.length - 1; i >= 0; i--) {
         SnowballFight.Elf e = this.elves[i];
         float wy = SLOT_G[i] - 20.0F;
         if (e != null) {
            this.elf(e, i, wy);
         }

         GameFx.sprite(this, "sb_hill", SLOT_X[i] - 100.0F, SLOT_G[i] - 3.0F, 200.0F, 70.0F, 200.0F, 70.0F, SnowballFight::paintHill, -1);
         GameFx.sprite(this, "sb_wall", SLOT_X[i] - 19.0F - 2.0F, wy - 4.0F, 42.0F, 28.0F, 42.0F, 28.0F, SnowballFight::paintWall, -1);
      }

      this.box(0.0F, 240.0F, 400.0F, 40.0F, 0.0F, -1511172);
      this.kid();
      GameFx.sprite(this, "sb_fort", 16.0F, 196.0F, 74.0F, 50.0F, 74.0F, 50.0F, SnowballFight::paintFort, -1);

      for (SnowballFight.Ball b : this.balls) {
         if (!(b.x < -100.0F)) {
            this.circle(b.x + 0.8F, b.y + 1.0F, 3.6F, 857747536);
            this.circle(b.x, b.y, 3.6F, b.enemy ? -2234120 : -1);
            this.circle(b.x - 1.0F, b.y - 1.0F, 1.6199999F, -1);
         }
      }

      if (this.aiming && this.state == GameView.State.PLAYING) {
         float[] v = this.launchVel();
         if (v != null) {
            float x = 64.0F;
            float y = 192.0F;
            float vx = v[0];
            float vy = v[1];

            for (int k = 0; k < 22; k++) {
               for (int s = 0; s < 4; s++) {
                  vy += 4.8F;
                  x += vx * 0.012F;
                  y += vy * 0.012F;
               }

               if (y > groundAt(x) || x > 400.0F) {
                  break;
               }

               float a = (1.0F - k / 22.0F) * (this.reload > 0.0F ? 0.35F : 0.9F);
               this.circle(x, y, 2.1F - k * 0.04F, Gx.withAlpha(-15057007, a * 0.5F));
               this.circle(x, y, 1.4F - k * 0.03F, Gx.withAlpha(-1, a));
            }

            int pc = Gx.mix(-11872659, -45730, v[2]);
            this.box(20.0F, 150.0F, 6.0F, 34.0F, 3.0F, 1711276032);
            this.box(20.0F, 150.0F + 34.0F * (1.0F - v[2]), 6.0F, 34.0F * v[2], 3.0F, pc);
         }

         this.circle(this.dragX, this.dragY, 3.0F, 1728053247);
         Gx.ringAt(this.X(this.dragX), this.Y(this.dragY), this.S(7.0F), Math.max(1, this.S(1.0F)), 1728053247);
         this.circle(this.curX, this.curY, 2.5F, -1426063361);
      }

      this.fx.draw(this, 0.0F, 0.0F);
      Gx.pop();

      for (float[] s : this.splats) {
         if (!(s[3] <= 0.0F)) {
            float a = Math.min(1.0F, s[3] * 1.6F);
            float sz = s[2] * (1.0F + 0.15F * (1.0F - Ease.clamp((1.0F - s[3]) * 8.0F)));
            GameFx.sprite(
               this,
               "sb_splat",
               s[0] - sz / 2.0F,
               s[1] - sz / 2.0F + (1.0F - s[3]) * 20.0F,
               sz,
               sz,
               64.0F,
               64.0F,
               SnowballFight::paintSplat,
               Gx.withAlpha(-1, a)
            );
         }
      }

      float lf = this.left() / 60.0F;
      this.box(8.0F, 8.0F, 384.0F, 7.0F, 3.5F, 1426063360);
      int tc = lf < 0.2F ? Gx.mix(-45730, -1, 0.5F + 0.5F * (float)Math.sin(this.anim * 12.0F)) : -6301441;
      this.box(8.0F, 8.0F, 384.0F * lf, 7.0F, 3.5F, tc);
      this.box(182.0F, 18.0F, 36.0F, 15.0F, 7.5F, 1711276032);
      this.text(String.format(Locale.ROOT, "%.0f s", Math.ceil(this.left())), 200.0F, 25.5F, 7.5F, 3, -1);
      this.box(8.0F, 18.0F, 104.0F, 15.0F, 7.5F, 1711276032);
      this.text("Treffer " + this.hits + (this.throwsN > 0 ? "  •  " + Math.round(100.0F * this.hits / this.throwsN) + " %" : ""), 60.0F, 25.5F, 6.8F, 2, -1);
      if (this.combo >= 2) {
         this.box(322.0F, 18.0F, 70.0F, 15.0F, 7.5F, Gx.withAlpha(-1890757, 0.9F));
         this.text("Serie " + this.combo + "  x" + this.mult(), 357.0F, 25.5F, 6.8F, 3, -1);
      }

      if (this.reload > 0.0F) {
         this.box(54.0F, 212.0F, 20.0F * (1.0F - this.reload / 0.32F), 2.0F, 1.0F, -1426063361);
      }

      if (this.state == GameView.State.PLAYING && this.hintT < 4.0F && !this.aiming) {
         float a = Math.min(1.0F, 4.0F - this.hintT) * (0.6F + 0.4F * (float)Math.sin(this.anim * 5.0F));
         this.box(80.0F, 110.0F, 240.0F, 22.0F, 11.0F, Gx.withAlpha(-16777216, 0.45F * a));
         this.text("Maus gedrückt halten, zurückziehen & loslassen!", 200.0F, 121.0F, 7.4F, 3, Gx.withAlpha(-1, a));
      }

      this.fx.drawPopups(this, 0.0F, 0.0F);
      this.fx.drawFlash(this, 400.0F, 270.0F);
   }

   private void elf(SnowballFight.Elf e, int i, float wy) {
      float x = SLOT_X[i];
      float top = wy - 30.0F * e.up + (e.hitT >= 0.0F ? e.hitT * 10.0F : 0.0F);
      String key = e.deer ? "sb_deer" : (e.gold ? "sb_elf_g" : "sb_elf");
      boolean hit = e.hitT >= 0.0F;
      if (e.gold) {
         Gx.glow(this.X(x), this.Y(top + 10.0F), this.S(26.0F), 1156890962);
      }

      float wob = hit ? 0.0F : (float)Math.sin(e.t * 9.0F) * 0.6F;
      GameFx.sprite(this, key + (hit ? "h" : ""), x - 14.0F + wob, top - 4.0F, 28.0F, 38.0F, 28.0F, 38.0F, gr -> paintElf(gr, e.deer, e.gold, hit), -1);
      if (hit && !e.deer) {
         for (int k = 0; k < 3; k++) {
            double a = this.anim * 6.0F + k * 2.1;
            Gx.icon("star-fill", this.X(x + (float)Math.cos(a) * 10.0F), this.Y(top + 2.0F + (float)Math.sin(a) * 3.0F), 5.0F * this.u, -8090);
         }
      }
   }

   private void kid() {
      float lift = Ease.outCubic(this.throwAnim) * 3.0F;
      float aimLean = this.aiming ? -1.5F : 0.0F;
      GameFx.sprite(
         this,
         "sb_kid" + (this.throwAnim > 0.3F ? 1 : 0),
         38.0F + aimLean,
         164.0F - lift,
         34.0F,
         44.0F,
         34.0F,
         44.0F,
         gr -> paintKid(gr, this.throwAnim > 0.3F),
         -1
      );
   }

   private static void paintSky(Graphics2D g) {
      g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(7317736), 0.0F, 200.0F, new Color(14281983)));
      g.fillRect(0, 0, 400, 270);
      g.setPaint(
         new RadialGradientPaint(
            new Float(330.0F, 50.0F), 70.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(255, 250, 225, 200), new Color(255, 250, 225, 0)}
         )
      );
      g.fill(new java.awt.geom.Ellipse2D.Float(260.0F, -20.0F, 140.0F, 140.0F));
      g.setColor(new Color(16775388));
      g.fill(new java.awt.geom.Ellipse2D.Float(318.0F, 38.0F, 24.0F, 24.0F));
      g.setColor(new Color(11126764));
      GeneralPath m = new GeneralPath();
      m.moveTo(0.0F, 190.0F);
      m.lineTo(40.0F, 140.0F);
      m.lineTo(80.0F, 165.0F);
      m.lineTo(140.0F, 110.0F);
      m.lineTo(200.0F, 160.0F);
      m.lineTo(260.0F, 120.0F);
      m.lineTo(320.0F, 150.0F);
      m.lineTo(370.0F, 115.0F);
      m.lineTo(400.0F, 135.0F);
      m.lineTo(400.0F, 270.0F);
      m.lineTo(0.0F, 270.0F);
      m.closePath();
      g.fill(m);
      g.setColor(new Color(16054527));
      GeneralPath caps = new GeneralPath();
      caps.moveTo(128.0F, 121.0F);
      caps.lineTo(140.0F, 110.0F);
      caps.lineTo(154.0F, 124.0F);
      caps.lineTo(146.0F, 121.0F);
      caps.lineTo(140.0F, 126.0F);
      caps.lineTo(134.0F, 120.0F);
      caps.closePath();
      caps.moveTo(250.0F, 128.0F);
      caps.lineTo(260.0F, 120.0F);
      caps.lineTo(272.0F, 130.0F);
      caps.lineTo(262.0F, 128.0F);
      caps.closePath();
      caps.moveTo(360.0F, 123.0F);
      caps.lineTo(370.0F, 115.0F);
      caps.lineTo(382.0F, 124.0F);
      caps.lineTo(371.0F, 122.0F);
      caps.closePath();
      g.fill(caps);
      g.setColor(new Color(255, 255, 255, 200));
      g.fill(
         GameFx.union(
            new java.awt.geom.Ellipse2D.Float(40.0F, 40.0F, 40.0F, 18.0F),
            new java.awt.geom.Ellipse2D.Float(58.0F, 30.0F, 36.0F, 24.0F),
            new java.awt.geom.Ellipse2D.Float(80.0F, 40.0F, 36.0F, 18.0F)
         )
      );
      g.fill(
         GameFx.union(
            new java.awt.geom.Ellipse2D.Float(190.0F, 60.0F, 34.0F, 14.0F),
            new java.awt.geom.Ellipse2D.Float(204.0F, 52.0F, 30.0F, 20.0F),
            new java.awt.geom.Ellipse2D.Float(222.0F, 60.0F, 30.0F, 14.0F)
         )
      );
      Random r = new Random(3L);

      for (int i = 0; i < 30; i++) {
         float tx = i * 14 + r.nextFloat() * 6.0F;
         float ty = 186.0F + r.nextFloat() * 10.0F;
         float s = 0.5F + r.nextFloat() * 0.4F;
         g.setColor(new Color(6194867));
         g.fillPolygon(new int[]{(int)tx, (int)(tx - 9.0F * s), (int)(tx + 9.0F * s)}, new int[]{(int)(ty - 34.0F * s), (int)ty, (int)ty}, 3);
      }

      g.setColor(new Color(13623541));
      g.fillRect(0, 192, 400, 80);
   }

   private static void paintHill(Graphics2D g) {
      GeneralPath p = new GeneralPath();
      p.moveTo(0.0F, 70.0F);

      for (int x = 0; x <= 200; x += 5) {
         float dx = (x - 100) / 62.0F;
         p.lineTo(x, 3.0F + dx * dx * 22.0F);
      }

      p.lineTo(200.0F, 70.0F);
      p.closePath();
      g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(16777215), 0.0F, 60.0F, new Color(12177390)));
      g.fill(p);
      g.setColor(new Color(150, 180, 225, 120));
      g.setStroke(new BasicStroke(1.2F));
      g.draw(p);
   }

   private static void paintWall(Graphics2D g) {
      g.setColor(new Color(60, 90, 140, 50));
      g.fill(new java.awt.geom.RoundRectangle2D.Float(2.0F, 8.0F, 40.0F, 20.0F, 6.0F, 6.0F));

      for (int row = 0; row < 2; row++) {
         for (int c = 0; c < 3 - row; c++) {
            float x = 2 + c * 13 + row * 6.5F;
            float y = 16 - row * 9;
            RoundRectangle2D b = new java.awt.geom.RoundRectangle2D.Float(x, y, 12.5F, 10.0F, 5.0F, 5.0F);
            g.setPaint(new GradientPaint(0.0F, y, new Color(16777215), 0.0F, y + 10.0F, new Color(13031666)));
            g.fill(b);
            g.setColor(new Color(150, 180, 225));
            g.setStroke(new BasicStroke(0.8F));
            g.draw(b);
         }
      }

      g.setColor(new Color(16054527));
      g.fill(new java.awt.geom.RoundRectangle2D.Float(0.0F, 24.0F, 42.0F, 4.0F, 4.0F, 4.0F));
   }

   private static void paintFort(Graphics2D g) {
      GeneralPath p = new GeneralPath();
      p.moveTo(0.0F, 50.0F);
      p.lineTo(0.0F, 20.0F);
      p.curveTo(4.0F, 10.0F, 14.0F, 10.0F, 18.0F, 16.0F);
      p.curveTo(24.0F, 6.0F, 36.0F, 6.0F, 42.0F, 16.0F);
      p.curveTo(48.0F, 8.0F, 60.0F, 10.0F, 66.0F, 20.0F);
      p.lineTo(74.0F, 50.0F);
      p.closePath();
      g.setPaint(new GradientPaint(0.0F, 8.0F, new Color(16777215), 0.0F, 50.0F, new Color(12111339)));
      g.fill(p);
      g.setColor(new Color(150, 180, 225));
      g.setStroke(new BasicStroke(1.0F));
      g.draw(p);

      for (int i = 0; i < 4; i++) {
         float x = 8 + i * 8 - (i == 3 ? 12 : 0);
         float y = i == 3 ? 36.0F : 42.0F;
         g.setPaint(new RadialGradientPaint(new Float(x + 2.0F, y + 2.0F), 5.0F, new float[]{0.0F, 1.0F}, new Color[]{Color.WHITE, new Color(13228786)}));
         g.fill(new java.awt.geom.Ellipse2D.Float(x, y, 8.0F, 8.0F));
      }
   }

   private static void paintKid(Graphics2D g, boolean throwing) {
      Color coat = new Color(3108832);
      Color coatD = new Color(1720209);
      g.setPaint(new GradientPaint(0.0F, 20.0F, coat, 0.0F, 44.0F, coatD));
      g.fill(new java.awt.geom.RoundRectangle2D.Float(8.0F, 22.0F, 18.0F, 20.0F, 8.0F, 8.0F));
      g.setColor(new Color(14886459));
      g.fill(new java.awt.geom.RoundRectangle2D.Float(8.0F, 20.0F, 18.0F, 5.0F, 4.0F, 4.0F));
      g.fill(new java.awt.geom.RoundRectangle2D.Float(8.0F, 22.0F, 5.0F, 10.0F, 3.0F, 3.0F));
      g.setColor(new Color(16767420));
      g.fill(new java.awt.geom.Ellipse2D.Float(9.0F, 6.0F, 16.0F, 16.0F));
      g.setColor(new Color(1776418));
      g.fill(new java.awt.geom.Ellipse2D.Float(18.0F, 12.0F, 2.4F, 2.8F));
      g.setColor(new Color(255, 120, 120, 120));
      g.fill(new java.awt.geom.Ellipse2D.Float(19.0F, 16.0F, 4.0F, 2.4F));
      g.setColor(new Color(6961690));
      g.setStroke(new BasicStroke(1.2F, 1, 1));
      g.drawArc(17, 15, 5, 3, 200, 140);
      g.setColor(new Color(14886459));
      g.fill(new java.awt.geom.Arc2D.Float(8.0F, 3.0F, 18.0F, 16.0F, 0.0F, 180.0F, 1));
      g.setColor(Color.WHITE);
      g.fill(new java.awt.geom.RoundRectangle2D.Float(8.0F, 9.0F, 18.0F, 4.0F, 4.0F, 4.0F));
      g.fill(new java.awt.geom.Ellipse2D.Float(14.0F, 0.0F, 6.0F, 6.0F));
      g.setColor(coat);
      g.setStroke(new BasicStroke(5.0F, 1, 1));
      if (throwing) {
         g.drawLine(22, 26, 31, 16);
      } else {
         g.drawLine(12, 27, 5, 21);
      }

      g.setColor(Color.WHITE);
      if (!throwing) {
         g.fill(new java.awt.geom.Ellipse2D.Float(0.5F, 15.0F, 7.0F, 7.0F));
         g.setColor(new Color(13228786));
         g.fill(new java.awt.geom.Ellipse2D.Float(3.0F, 19.0F, 3.0F, 2.0F));
      }
   }

   private static void paintElf(Graphics2D g, boolean deer, boolean gold, boolean hit) {
      if (deer) {
         g.setColor(new Color(7226914));
         g.setStroke(new BasicStroke(2.0F, 1, 1));
         g.drawLine(9, 10, 5, 2);
         g.drawLine(6, 5, 2, 5);
         g.drawLine(19, 10, 23, 2);
         g.drawLine(22, 5, 26, 5);
         g.setPaint(new GradientPaint(0.0F, 8.0F, new Color(10514492), 0.0F, 30.0F, new Color(7226914)));
         g.fill(new java.awt.geom.Ellipse2D.Float(6.0F, 8.0F, 16.0F, 18.0F));
         g.fill(new java.awt.geom.RoundRectangle2D.Float(8.0F, 22.0F, 12.0F, 16.0F, 6.0F, 6.0F));
         g.setColor(new Color(14267530));
         g.fill(new java.awt.geom.Ellipse2D.Float(9.0F, 16.0F, 10.0F, 9.0F));
         g.setColor(new Color(14886459));
         g.fill(new java.awt.geom.Ellipse2D.Float(11.0F, 18.0F, 6.0F, 5.0F));
         g.setColor(Color.WHITE);
         g.fill(new java.awt.geom.Ellipse2D.Float(15.0F, 19.0F, 1.5F, 1.5F));
         g.setColor(new Color(1776418));
         g.fill(new java.awt.geom.Ellipse2D.Float(9.0F, 13.0F, 3.0F, 3.0F));
         g.fill(new java.awt.geom.Ellipse2D.Float(16.0F, 13.0F, 3.0F, 3.0F));
      } else {
         Color hat = gold ? new Color(16040274) : new Color(2073436);
         Color hatD = gold ? new Color(12092970) : new Color(941620);
         g.setPaint(new GradientPaint(0.0F, 24.0F, hat, 0.0F, 38.0F, hatD));
         g.fill(new java.awt.geom.RoundRectangle2D.Float(7.0F, 24.0F, 14.0F, 14.0F, 6.0F, 6.0F));
         g.setColor(new Color(14886459));
         g.fill(new java.awt.geom.RoundRectangle2D.Float(6.0F, 23.0F, 16.0F, 4.0F, 3.0F, 3.0F));
         g.setColor(new Color(16239013));
         g.fillPolygon(new int[]{7, 1, 8}, new int[]{15, 11, 19}, 3);
         g.fillPolygon(new int[]{21, 27, 20}, new int[]{15, 11, 19}, 3);
         g.fill(new java.awt.geom.Ellipse2D.Float(6.0F, 10.0F, 16.0F, 15.0F));
         g.setColor(new Color(1776418));
         if (hit) {
            g.setStroke(new BasicStroke(1.2F, 1, 1));
            g.drawLine(9, 14, 12, 17);
            g.drawLine(12, 14, 9, 17);
            g.drawLine(16, 14, 19, 17);
            g.drawLine(19, 14, 16, 17);
            g.setColor(Color.WHITE);
            g.fill(GameFx.union(new java.awt.geom.Ellipse2D.Float(5.0F, 11.0F, 12.0F, 10.0F), new java.awt.geom.Ellipse2D.Float(11.0F, 14.0F, 10.0F, 9.0F)));
         } else {
            g.fill(new java.awt.geom.Ellipse2D.Float(9.5F, 14.0F, 2.6F, 3.0F));
            g.fill(new java.awt.geom.Ellipse2D.Float(16.0F, 14.0F, 2.6F, 3.0F));
            g.setColor(new Color(11879727));
            g.setStroke(new BasicStroke(1.1F, 1, 1));
            g.drawArc(11, 17, 6, 4, 200, 140);
            g.setColor(new Color(255, 120, 120, 120));
            g.fill(new java.awt.geom.Ellipse2D.Float(7.5F, 18.0F, 3.5F, 2.0F));
            g.fill(new java.awt.geom.Ellipse2D.Float(17.5F, 18.0F, 3.5F, 2.0F));
         }

         GeneralPath h = new GeneralPath();
         h.moveTo(5.0F, 12.0F);
         h.curveTo(8.0F, 2.0F, 14.0F, -1.0F, 25.0F, 3.0F);
         h.lineTo(22.0F, 6.0F);
         h.curveTo(22.0F, 8.0F, 23.0F, 10.0F, 23.0F, 12.0F);
         h.closePath();
         g.setPaint(new GradientPaint(5.0F, 0.0F, hat, 23.0F, 12.0F, hatD));
         g.fill(h);
         g.setColor(gold ? Color.WHITE : new Color(16040274));
         g.fill(new java.awt.geom.Ellipse2D.Float(23.0F, 1.0F, 5.0F, 5.0F));
         g.setColor(new Color(14886459));
         g.fill(new java.awt.geom.RoundRectangle2D.Float(4.5F, 10.0F, 19.0F, 3.5F, 3.0F, 3.0F));
      }
   }

   private static void paintBalloon(Graphics2D g, boolean popped) {
      if (!popped) {
         g.setPaint(new RadialGradientPaint(new Float(9.0F, 7.0F), 14.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(16743048), new Color(12917294)}));
         g.fill(new java.awt.geom.Ellipse2D.Float(2.0F, 0.0F, 20.0F, 24.0F));
         g.setColor(new Color(255, 255, 255, 150));
         g.fill(new java.awt.geom.Ellipse2D.Float(6.0F, 4.0F, 5.0F, 7.0F));
      }

      g.setColor(new Color(4477030));
      g.setStroke(new BasicStroke(0.7F));
      g.drawLine(12, 24, 12, 30);
      g.setColor(new Color(2073436));
      g.fill(new java.awt.geom.RoundRectangle2D.Float(6.0F, 29.0F, 12.0F, 10.0F, 3.0F, 3.0F));
      g.setColor(new Color(16040274));
      g.fillRect(11, 29, 2, 10);
      g.fillRect(6, 32, 12, 2);
   }

   private static void paintSplat(Graphics2D g) {
      Random r = new Random(5L);
      g.setColor(new Color(255, 255, 255, 235));
      g.fill(new java.awt.geom.Ellipse2D.Float(14.0F, 14.0F, 36.0F, 34.0F));

      for (int i = 0; i < 9; i++) {
         double a = i * Math.PI * 2.0 / 9.0 + r.nextDouble() * 0.4;
         float d = 18.0F + r.nextFloat() * 10.0F;
         float s = 7.0F + r.nextFloat() * 7.0F;
         g.fill(new java.awt.geom.Ellipse2D.Float(32.0F + (float)Math.cos(a) * d - s / 2.0F, 32.0F + (float)Math.sin(a) * d - s / 2.0F, s, s));
      }

      g.setColor(new Color(200, 220, 245, 200));
      g.fill(new java.awt.geom.Ellipse2D.Float(24.0F, 34.0F, 18.0F, 8.0F));
      g.setColor(Color.WHITE);
      g.fill(new java.awt.geom.Ellipse2D.Float(22.0F, 20.0F, 10.0F, 8.0F));
   }

   private static final class Ball {
      float x;
      float y;
      float vx;
      float vy;
      float t;
      boolean enemy;
   }

   private static final class Balloon {
      float x;
      float y;
      float t;
      boolean hit;
      float hitT;
   }

   private static final class Elf {
      int slot;
      float t;
      float up;
      float stay;
      float hitT = -1.0F;
      boolean gold;
      boolean deer;
      boolean thrown;
   }
}
