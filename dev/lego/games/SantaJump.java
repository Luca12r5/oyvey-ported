package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
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
      Random var1 = new Random(4L);

      for (float[] var5 : this.stars) {
         var5[0] = var1.nextFloat() * 460.0F;
         var5[1] = var1.nextFloat() * 320.0F;
         var5[2] = 0.3F + var1.nextFloat() * 0.7F;
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
      SantaJump.Plat var1 = new SantaJump.Plat();
      var1.x = 0.0F;
      var1.y = 0.0F;
      var1.w = 460.0F;
      var1.type = 0;
      this.plats.add(var1);
      this.genY = 0.0F;
      this.generate();
   }

   private float height() {
      return Math.max(0.0F, -this.y) / 5.0F;
   }

   private void generate() {
      while (this.genY > this.cam - 80.0F) {
         float var1 = -this.genY / 5.0F;
         float var2 = Math.min(92.0F, 34.0F + var1 * 0.018F + this.rnd.nextFloat() * (18.0F + Math.min(34.0F, var1 * 0.01F)));
         this.genY -= var2;
         SantaJump.Plat var3 = new SantaJump.Plat();
         var3.y = this.genY;
         var3.x = 6.0F + this.rnd.nextFloat() * 402.0F;
         float var4 = this.rnd.nextFloat();
         float var5 = Math.min(0.35F, var1 * 4.0E-4F);
         float var6 = 0.07F;
         if (var1 > 40.0F && var4 < var6) {
            var3.type = 3;
            var3.w = 44.0F;
         } else if (var1 > 60.0F && var4 < var6 + var5) {
            var3.type = 1;
            var3.vx = (this.rnd.nextBoolean() ? 1 : -1) * (30.0F + Math.min(70.0F, var1 * 0.02F) + this.rnd.nextFloat() * 20.0F);
         } else {
            var3.type = 0;
         }

         this.plats.add(var3);
         if (var3.type != 3 && this.rnd.nextFloat() < 0.22F) {
            SantaJump.Gift var7 = new SantaJump.Gift();
            var7.x = var3.x + var3.w / 2.0F;
            var7.y = var3.y - 20.0F - this.rnd.nextFloat() * 16.0F;
            var7.color = this.rnd.nextInt(GIFT_BOX.length);
            var7.gold = this.rnd.nextFloat() < 0.1F;
            var7.t = this.rnd.nextFloat() * 6.0F;
            this.gifts.add(var7);
         }

         if (var1 > 25.0F && this.rnd.nextFloat() < Math.min(0.4F, 0.1F + var1 * 3.0E-4F) && var2 > 50.0F) {
            SantaJump.Plat var8 = new SantaJump.Plat();
            var8.type = 2;
            var8.y = this.genY + var2 * 0.5F;
            var8.x = 6.0F + this.rnd.nextFloat() * 402.0F;
            this.plats.add(var8);
         }

         if (var1 > 180.0F && this.rnd.nextFloat() < Math.min(0.09F, 0.02F + var1 * 4.0E-5F)) {
            SantaJump.Storm var9 = new SantaJump.Storm();
            var9.y = this.genY - var2 * 0.5F;
            var9.x = 30.0F + this.rnd.nextFloat() * 400.0F;
            var9.vx = (this.rnd.nextBoolean() ? 1 : -1) * (20.0F + this.rnd.nextFloat() * 30.0F);
            var9.t = this.rnd.nextFloat() * 5.0F;
            this.storms.add(var9);
         }
      }
   }

   @Override
   protected void onKey(int var1) {
      if (var1 == 263 || var1 == 65) {
         this.left = true;
         this.mouseMode = false;
      }

      if (var1 == 262 || var1 == 68) {
         this.right = true;
         this.mouseMode = false;
      }
   }

   @Override
   protected void onKeyUp(int var1) {
      if (var1 == 263 || var1 == 65) {
         this.left = false;
      }

      if (var1 == 262 || var1 == 68) {
         this.right = false;
      }
   }

   @Override
   protected void update(float var1) {
      this.fx.update(var1);
      this.squash = Math.max(0.0F, this.squash - var1 * 5.0F);
      this.comboT = Math.max(0.0F, this.comboT - var1);
      if (this.comboT <= 0.0F) {
         this.combo = 0;
      }

      this.superT = Math.max(0.0F, this.superT - var1);
      this.hintT += var1;
      if (this.dying) {
         this.deadT += var1;
         this.vy = Math.min(900.0F, this.vy + 980.0F * var1);
         this.y = this.y + this.vy * var1;
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

         float var2;
         if (this.mouseMode && this.u > 0.0F) {
            float var3 = (this.mx - this.bx) / this.u;
            float var4 = var3 - this.x;
            var2 = Math.max(-260.0F, Math.min(260.0F, var4 * 6.0F));
         } else {
            var2 = ((this.right ? 1 : 0) - (this.left ? 1 : 0)) * 230;
         }

         this.vx = this.vx + (var2 - this.vx) * Math.min(1.0F, var1 * 12.0F);
         if (Math.abs(this.vx) > 20.0F) {
            this.face = Math.signum(this.vx);
         }

         this.x = this.x + this.vx * var1;
         if (this.x < -15.0F) {
            this.x += 490.0F;
         }

         if (this.x > 475.0F) {
            this.x -= 490.0F;
         }

         float var8 = this.y;
         this.vy = Math.min(700.0F, this.vy + 980.0F * var1);
         this.y = this.y + this.vy * var1;
         this.trail += var1;
         if (this.superT > 0.0F && this.trail > 0.03F) {
            this.trail = 0.0F;
            this.fx
               .add(
                  this.x + (this.rnd.nextFloat() - 0.5F) * 12.0F, this.y - 6.0F, 0.0F, 20.0F, 0.6F, 3.0F + this.rnd.nextFloat() * 3.0F, -736942, 2, 0.0F, 1.0F
               );
         }

         for (SantaJump.Plat var5 : this.plats) {
            var5.t += var1;
            if (var5.type == 1) {
               var5.x = var5.x + var5.vx * var1;
               if (var5.x < 4.0F) {
                  var5.x = 4.0F;
                  var5.vx = Math.abs(var5.vx);
               }

               if (var5.x + var5.w > 456.0F) {
                  var5.x = 456.0F - var5.w;
                  var5.vx = -Math.abs(var5.vx);
               }
            }

            if (var5.broken) {
               var5.fall += var1;
            } else if (this.vy > 0.0F && var8 <= var5.y + 1.0F && this.y >= var5.y && this.x + 9.0F > var5.x && this.x - 9.0F < var5.x + var5.w) {
               if (var5.type == 2) {
                  var5.broken = true;
                  this.fx.burst(var5.x + var5.w / 2.0F, var5.y + 4.0F, 16, -4201473, 110.0F, 3.2F, 0.7F, 420.0F, 1);
                  Sound.play("minecraft:block.glass.break", 1.4F + this.rnd.nextFloat() * 0.2F, 0.25F);
               } else {
                  this.y = var5.y;
                  boolean var6 = var5.type == 3 && Math.abs(this.x - (var5.x + var5.w * 0.68F)) < 11.0F;
                  this.vy = var6 ? -800.0F : -440.0F;
                  this.squash = 1.0F;
                  this.fx
                     .burst(
                        this.x,
                        var5.y + 2.0F,
                        var6 ? 18 : 7,
                        var6 ? -4734770 : -722689,
                        var6 ? 90.0F : 60.0F,
                        var6 ? 5.0F : 3.0F,
                        0.55F,
                        var6 ? -60.0F : 40.0F,
                        4
                     );
                  if (var6) {
                     this.superT = 1.1F;
                     this.fx.shake(0.45F);
                     this.fx.ring(this.x, var5.y, 34.0F, -736942, 0.45F);
                     this.fx.popup("Schornstein-Turbo!", this.x, var5.y - 26.0F, -736942, 10.0F);
                     Sound.play("minecraft:entity.firework_rocket.launch", 1.1F, 0.4F);
                  } else {
                     Sound.play("minecraft:block.snow.step", 1.2F + this.rnd.nextFloat() * 0.3F, 0.4F);
                  }

                  if (!var5.used && var5 != this.plats.get(0)) {
                     var5.used = true;
                  }
               }
            }
         }

         for (int var10 = this.gifts.size() - 1; var10 >= 0; var10--) {
            SantaJump.Gift var13 = this.gifts.get(var10);
            var13.t += var1;
            float var15 = var13.y + (float)Math.sin(var13.t * 3.0F) * 2.5F;
            if (Math.abs(var13.x - this.x) < 16.0F && Math.abs(var15 - (this.y - 14.0F)) < 20.0F) {
               this.gifts.remove(var10);
               this.combo = this.comboT > 0.0F ? this.combo + 1 : 1;
               this.comboT = 2.2F;
               int var7 = (var13.gold ? 150 : 25) * Math.min(this.combo, 8);
               this.bonus += var7;
               this.fx
                  .confetti(
                     var13.x, var15, 18, var13.gold ? new int[]{-736942, -5720, -1} : new int[]{GIFT_BOX[var13.color], GIFT_RIB[var13.color], -1}, 170.0F, 4.0F
                  );
               this.fx.ring(var13.x, var15, 20.0F, var13.gold ? -736942 : -1, 0.35F);
               this.fx
                  .popup(
                     "+" + var7 + (this.combo > 1 ? "  x" + Math.min(this.combo, 8) : ""),
                     var13.x,
                     var15 - 10.0F,
                     var13.gold ? -736942 : -1,
                     this.combo > 1 ? 11.0F : 9.5F
                  );
               Sound.play("minecraft:entity.experience_orb.pickup", 1.0F + Math.min(0.9F, this.combo * 0.1F), 0.35F);
            }
         }

         for (SantaJump.Storm var14 : this.storms) {
            var14.t += var1;
            if (var14.dead) {
               var14.deadT += var1;
               var14.y = var14.y + 260.0F * var1 * var14.deadT * 3.0F;
            } else {
               var14.x = var14.x + var14.vx * var1;
               if (var14.x < 22.0F || var14.x > 438.0F) {
                  var14.vx = -var14.vx;
               }

               float var16 = this.x - var14.x;
               float var17 = this.y - 14.0F - var14.y;
               if (Math.abs(var16) < 22.0F && Math.abs(var17) < 17.0F) {
                  if (this.vy > 0.0F && this.y - 6.0F < var14.y) {
                     var14.dead = true;
                     this.vy = -484.0F;
                     this.squash = 1.0F;
                     this.bonus += 100;
                     this.fx.burst(var14.x, var14.y, 22, -9537392, 140.0F, 5.0F, 0.6F, 120.0F, 0);
                     this.fx.popup("+100 Gewitter weg!", var14.x, var14.y - 16.0F, -6301441, 10.0F);
                     this.fx.shake(0.35F);
                     Sound.play("minecraft:entity.generic.explode", 1.6F, 0.25F);
                  } else {
                     this.die(true);
                  }
               }
            }
         }

         float var12 = this.y - 147.2F;
         if (var12 < this.cam) {
            this.cam = this.cam + (var12 - this.cam) * Math.min(1.0F, var1 * 9.0F);
         }

         this.maxH = Math.max(this.maxH, this.height());
         this.score = (long)this.maxH + this.bonus;
         this.generate();
         this.plats.removeIf(var1x -> var1x.y - this.cam > 380.0F || var1x.fall > 1.5F);
         this.gifts.removeIf(var1x -> var1x.y - this.cam > 360.0F);
         this.storms.removeIf(var1x -> var1x.y - this.cam > 380.0F);
         if (this.y - this.cam > 340.0F) {
            this.die(false);
         }
      }
   }

   private void die(boolean var1) {
      if (!this.dying) {
         this.dying = true;
         this.deadT = 0.0F;
         this.fx.shake(0.8F);
         this.fx.flash(var1 ? -4201473 : -1890757, 0.8F);
         if (var1) {
            this.vy = -200.0F;
            Sound.play("minecraft:entity.lightning_bolt.thunder", 1.5F, 0.3F);
         } else {
            Sound.play("minecraft:entity.player.hurt", 0.8F, 0.4F);
         }
      }
   }

   @Override
   protected void render() {
      float var1 = Math.min(0.05F, Gx.dt);
      this.anim += var1;
      if (this.state == GameView.State.READY) {
         this.y = -20.0F + (float)(-Math.abs(Math.sin(this.anim * 3.0F))) * 70.0F;
         this.cam = -270.0F;
         this.squash = Math.max(0.0F, 1.0F - (float)Math.abs(Math.sin(this.anim * 3.0F)) * 6.0F);
      }

      float var2 = Math.max(0.0F, -this.cam) / 5.0F;
      float var3 = Ease.clamp(var2 / 600.0F);
      float var4 = Ease.clamp((var2 - 600.0F) / 900.0F);
      int var5 = Gx.mix(Gx.mix(-15852481, -15003846, var3), -16514036, var4);
      int var6 = Gx.mix(Gx.mix(-13804390, -11851154, var3), -15594966, var4);
      this.boxV(0.0F, 0.0F, 460.0F, 320.0F, 0.0F, var5, var6);
      float var7 = 0.35F + 0.65F * Ease.clamp(var2 / 300.0F);

      for (float[] var11 : this.stars) {
         float var12 = ((var11[1] - this.cam * 0.08F * var11[2]) % 320.0F + 320.0F) % 320.0F;
         float var13 = 0.6F + 0.4F * (float)Math.sin(this.anim * 2.0F + var11[0]);
         this.circle(var11[0], var12, 0.6F + var11[2] * 0.9F, Gx.withAlpha(-1, var7 * var13 * var11[2]));
      }

      float var14 = 60.0F - this.cam * 0.02F;
      Gx.glow(this.X(398.0F), this.Y(var14), this.S(80.0F), 872412374);
      this.circle(398.0F, var14, 20.0F, -2858);
      this.circle(405.0F, var14 - 6.0F, 4.0F, 402653184);
      this.circle(390.0F, var14 + 5.0F, 3.0F, 335544320);
      if (var2 > 200.0F) {
         float var15 = Ease.clamp((var2 - 200.0F) / 400.0F) * (1.0F - var4 * 0.6F);
         Gx.pushAlpha(var15);
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

      float var16 = (0.0F - this.cam) * 0.6F + 128.0F - 30.0F;
      if (var16 < 330.0F) {
         GameFx.sprite(this, "sj_town", 0.0F, var16 - 70.0F, 460.0F, 110.0F, 460.0F, 110.0F, SantaJump::paintTown, -1);
         if (var16 + 40.0F < 320.0F) {
            this.box(0.0F, var16 + 40.0F, 460.0F, 320.0F - var16 - 40.0F, 0.0F, -16050639);
         }
      }

      Gx.push();
      Gx.translate(this.S(this.fx.sx), this.S(this.fx.sy));

      for (SantaJump.Plat var22 : this.plats) {
         this.plat(var22);
      }

      for (SantaJump.Gift var23 : this.gifts) {
         float var25 = var23.y + (float)Math.sin(var23.t * 3.0F) * 2.5F - this.cam;
         if (!(var25 < -30.0F) && !(var25 > 350.0F)) {
            Gx.glow(this.X(var23.x), this.Y(var25), this.S(18.0F), var23.gold ? 1442103634 : 654311423);
            int var26 = var23.gold ? 5 : var23.color;
            GameFx.sprite(this, "sj_gift" + var26, var23.x - 9.0F, var25 - 9.0F, 18.0F, 18.0F, 40.0F, 40.0F, var1x -> paintGift(var1x, var26), -1);
         }
      }

      for (SantaJump.Storm var24 : this.storms) {
         this.storm(var24);
      }

      this.fx.draw(this, 0.0F, this.cam);
      this.santa();
      Gx.pop();
      this.fx.drawPopups(this, 0.0F, this.cam);
      this.box(8.0F, 8.0F, 74.0F, 22.0F, 11.0F, 1711276032);
      Gx.icon("sprint", this.X(20.0F), this.Y(19.0F), 10.0F * this.u, -4201473);
      this.text((int)this.maxH + " m", 50.0F, 19.0F, 9.0F, 3, -1);
      if (this.combo > 1 && this.comboT > 0.0F) {
         float var20 = Math.min(1.0F, this.comboT * 2.0F);
         this.box(374.0F, 8.0F, 78.0F, 22.0F, 11.0F, Gx.withAlpha(-1890757, 0.85F * var20));
         this.text("Combo x" + Math.min(this.combo, 8), 413.0F, 19.0F, 8.5F, 3, Gx.withAlpha(-1, var20));
         this.box(380.0F, 26.0F, 66.0F * this.comboT / 2.2F, 2.0F, 1.0F, Gx.withAlpha(-1, var20 * 0.8F));
      }

      if (this.state == GameView.State.PLAYING && this.hintT < 3.5F) {
         float var21 = Math.min(1.0F, 3.5F - this.hintT) * (0.6F + 0.4F * (float)Math.sin(this.anim * 5.0F));
         this.box(138.0F, 276.0F, 184.0F, 22.0F, 11.0F, Gx.withAlpha(-16777216, 0.4F * var21));
         this.text("← → / A D oder Maus zum Lenken", 230.0F, 287.0F, 7.5F, 3, Gx.withAlpha(-1, var21));
      }

      this.fx.drawFlash(this, 460.0F, 320.0F);
   }

   private void plat(SantaJump.Plat var1) {
      float var2 = var1.y - this.cam + (var1.broken ? var1.fall * var1.fall * 300.0F : 0.0F);
      if (!(var2 < -30.0F) && !(var2 > 360.0F)) {
         if (var1 == this.plats.get(0) && var1.w >= 460.0F) {
            this.boxV(0.0F, var2, 460.0F, 320.0F, 0.0F, -1510913, -5651222);

            for (float var5 = 8.0F; var5 < 460.0F; var5 += 34.0F) {
               this.circle(var5, var2 + 2.0F, 10.0F, -722689);
            }
         } else {
            float var3 = var1.broken ? Math.max(0.0F, 1.0F - var1.fall * 1.4F) : 1.0F;
            Gx.pushAlpha(var3);
            switch (var1.type) {
               case 2:
                  GameFx.sprite(
                     this, "sj_ice" + (var1.broken ? 1 : 0), var1.x, var2 - 3.0F, var1.w, 15.0F, 46.0F, 15.0F, var1x -> paintIce(var1x, var1.broken), -1
                  );
                  break;
               case 3:
                  float var6 = 0.5F + 0.5F * (float)Math.sin(var1.t * 5.0F);
                  GameFx.sprite(this, "sj_chim", var1.x - 2.0F, var2 - 26.0F, var1.w + 4.0F, 44.0F, 48.0F, 44.0F, SantaJump::paintChimney, -1);
                  Gx.glow(this.X(var1.x + var1.w * 0.68F), this.Y(var2 - 22.0F), this.S(14.0F + var6 * 4.0F), Gx.withAlpha(-20434, 0.25F + var6 * 0.2F));
                  if (this.rnd.nextFloat() < 0.15F && this.state == GameView.State.PLAYING) {
                     this.fx
                        .add(
                           var1.x + var1.w * 0.68F,
                           var1.y - 26.0F,
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
                  float var4 = var1.type == 1 ? (float)Math.sin(var1.t * 4.0F) * 1.2F : 0.0F;
                  GameFx.sprite(
                     this,
                     "sj_cloud" + var1.type,
                     var1.x - 3.0F,
                     var2 - 5.0F + var4,
                     var1.w + 6.0F,
                     21.0F,
                     52.0F,
                     21.0F,
                     var1x -> paintCloud(var1x, var1.type == 1),
                     -1
                  );
            }

            Gx.popAlpha();
         }
      }
   }

   private void storm(SantaJump.Storm var1) {
      float var2 = var1.y - this.cam;
      if (!(var2 < -40.0F) && !(var2 > 360.0F)) {
         boolean var3 = (int)(var1.t * 3.0F) % 4 == 0;
         float var4 = var1.dead ? Math.max(0.0F, 1.0F - var1.deadT * 2.0F) : 1.0F;
         Gx.pushAlpha(var4);
         if (var3 && !var1.dead) {
            Gx.glow(this.X(var1.x), this.Y(var2 + 14.0F), this.S(34.0F), 1153426431);
         }

         GameFx.sprite(
            this,
            "sj_storm" + (var3 ? 1 : 0) + (var1.dead ? 1 : 0),
            var1.x - 25.0F,
            var2 - 17.0F,
            50.0F,
            42.0F,
            50.0F,
            42.0F,
            var2x -> paintStorm(var2x, var3 && !var1.dead, var1.dead),
            -1
         );
         Gx.popAlpha();
      }
   }

   private void santa() {
      float var1 = this.y - this.cam;
      float var2 = Ease.outCubic(this.squash);
      float var3 = this.vy < -200.0F ? Math.min(0.12F, (-this.vy - 200.0F) / 3000.0F) : 0.0F;
      float var4 = 30.0F * (1.0F + 0.22F * var2 - var3);
      float var5 = 30.0F * (1.0F - 0.2F * var2 + var3);
      if (this.superT > 0.0F) {
         Gx.glow(this.X(this.x), this.Y(var1 - var5 / 2.0F), this.S(34.0F), Gx.withAlpha(-736942, this.superT * 0.4F));
      }

      boolean var6 = this.face > 0.0F;
      if (this.dying) {
         Gx.pushAlpha(Math.max(0.0F, 1.0F - this.deadT));
      }

      GameFx.spriteAt(
         this,
         "sj_santa" + (var6 ? 1 : 0) + (this.dying ? 1 : 0),
         30.0F,
         30.0F,
         100.0F,
         100.0F,
         var2x -> paintSanta(var2x, var6, this.dying),
         this.x - var4 / 2.0F,
         var1 - var5,
         var4,
         var5,
         -1
      );
      if (this.x < 15.0F) {
         GameFx.spriteAt(
            this,
            "sj_santa" + (var6 ? 1 : 0) + (this.dying ? 1 : 0),
            30.0F,
            30.0F,
            100.0F,
            100.0F,
            var2x -> paintSanta(var2x, var6, this.dying),
            this.x + 460.0F - var4 / 2.0F,
            var1 - var5,
            var4,
            var5,
            -1
         );
      }

      if (this.x > 445.0F) {
         GameFx.spriteAt(
            this,
            "sj_santa" + (var6 ? 1 : 0) + (this.dying ? 1 : 0),
            30.0F,
            30.0F,
            100.0F,
            100.0F,
            var2x -> paintSanta(var2x, var6, this.dying),
            this.x - 460.0F - var4 / 2.0F,
            var1 - var5,
            var4,
            var5,
            -1
         );
      }

      if (this.dying) {
         Gx.popAlpha();
      }
   }

   private static void paintSanta(Graphics2D var0, boolean var1, boolean var2) {
      if (!var1) {
         var0.translate(100, 0);
         var0.scale(-1.0, 1.0);
      }

      Color var3 = new Color(14886459);
      Color var4 = new Color(10358820);
      Color var5 = new Color(16054527);
      Color var6 = new Color(13227754);
      var0.setStroke(new BasicStroke(2.2F));
      var0.setColor(new Color(1776418));
      var0.fill(new Float(28.0F, 86.0F, 18.0F, 13.0F, 7.0F, 7.0F));
      var0.fill(new Float(54.0F, 86.0F, 20.0F, 13.0F, 7.0F, 7.0F));
      var0.setPaint(new GradientPaint(0.0F, 40.0F, new Color(10514492), 0.0F, 80.0F, new Color(7226914)));
      var0.fill(new java.awt.geom.Ellipse2D.Float(6.0F, 44.0F, 30.0F, 36.0F));
      var0.setColor(new Color(5913114));
      var0.fill(new Float(14.0F, 40.0F, 14.0F, 7.0F, 4.0F, 4.0F));
      var0.setPaint(new GradientPaint(0.0F, 44.0F, var3, 0.0F, 92.0F, var4));
      var0.fill(new Float(22.0F, 44.0F, 58.0F, 48.0F, 30.0F, 30.0F));
      var0.setPaint(new GradientPaint(0.0F, 80.0F, var5, 0.0F, 92.0F, var6));
      var0.fill(new Float(23.0F, 80.0F, 56.0F, 10.0F, 10.0F, 10.0F));
      var0.setColor(new Color(1447452));
      var0.fill(new java.awt.geom.Rectangle2D.Float(22.0F, 66.0F, 58.0F, 8.0F));
      var0.setColor(new Color(16040274));
      var0.setStroke(new BasicStroke(2.6F));
      var0.draw(new java.awt.geom.Rectangle2D.Float(54.0F, 64.5F, 11.0F, 11.0F));
      var0.setColor(var5);
      var0.fill(new java.awt.geom.Rectangle2D.Float(49.0F, 44.0F, 6.0F, 22.0F));
      var0.setPaint(new GradientPaint(70.0F, 44.0F, var3, 90.0F, 60.0F, var4));
      var0.fill(new Float(70.0F, 46.0F, 16.0F, 26.0F, 14.0F, 14.0F));
      var0.setColor(var5);
      var0.fill(new Float(69.0F, 66.0F, 18.0F, 7.0F, 6.0F, 6.0F));
      var0.setColor(new Color(2073436));
      var0.fill(new java.awt.geom.Ellipse2D.Float(70.0F, 70.0F, 15.0F, 13.0F));
      var0.setPaint(
         new RadialGradientPaint(
            new java.awt.geom.Point2D.Float(55.0F, 28.0F), 22.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(16767420), new Color(15314572)}
         )
      );
      var0.fill(new java.awt.geom.Ellipse2D.Float(36.0F, 14.0F, 36.0F, 34.0F));
      GeneralPath var7 = new GeneralPath();
      var7.moveTo(34.0F, 32.0F);
      var7.curveTo(34.0F, 62.0F, 50.0F, 70.0F, 56.0F, 70.0F);
      var7.curveTo(66.0F, 70.0F, 78.0F, 60.0F, 76.0F, 32.0F);
      var7.curveTo(70.0F, 40.0F, 62.0F, 42.0F, 56.0F, 40.0F);
      var7.curveTo(48.0F, 42.0F, 40.0F, 40.0F, 34.0F, 32.0F);
      var7.closePath();
      var0.setPaint(new GradientPaint(0.0F, 32.0F, var5, 0.0F, 70.0F, var6));
      var0.fill(var7);
      var0.setColor(Color.WHITE);
      var0.fill(new java.awt.geom.Ellipse2D.Float(47.0F, 34.0F, 12.0F, 8.0F));
      var0.fill(new java.awt.geom.Ellipse2D.Float(58.0F, 34.0F, 12.0F, 8.0F));
      var0.setColor(new Color(15764090));
      var0.fill(new java.awt.geom.Ellipse2D.Float(55.0F, 29.0F, 8.0F, 8.0F));
      var0.setColor(new Color(1776418));
      if (var2) {
         var0.setStroke(new BasicStroke(2.4F, 1, 1));
         var0.drawLine(47, 23, 53, 29);
         var0.drawLine(53, 23, 47, 29);
         var0.drawLine(63, 23, 69, 29);
         var0.drawLine(69, 23, 63, 29);
      } else {
         var0.fill(new java.awt.geom.Ellipse2D.Float(48.0F, 23.0F, 6.0F, 7.0F));
         var0.fill(new java.awt.geom.Ellipse2D.Float(64.0F, 23.0F, 6.0F, 7.0F));
         var0.setColor(Color.WHITE);
         var0.fill(new java.awt.geom.Ellipse2D.Float(50.0F, 24.0F, 2.0F, 2.0F));
         var0.fill(new java.awt.geom.Ellipse2D.Float(66.0F, 24.0F, 2.0F, 2.0F));
      }

      var0.setColor(new Color(255, 120, 120, 90));
      var0.fill(new java.awt.geom.Ellipse2D.Float(41.0F, 30.0F, 8.0F, 5.0F));
      GeneralPath var8 = new GeneralPath();
      var8.moveTo(36.0F, 20.0F);
      var8.curveTo(38.0F, 2.0F, 58.0F, -2.0F, 70.0F, 8.0F);
      var8.curveTo(76.0F, 12.0F, 74.0F, 18.0F, 72.0F, 20.0F);
      var8.closePath();
      var0.setPaint(new GradientPaint(40.0F, 0.0F, var3, 70.0F, 20.0F, var4));
      var0.fill(var8);
      GeneralPath var9 = new GeneralPath();
      var9.moveTo(40.0F, 12.0F);
      var9.curveTo(30.0F, 4.0F, 18.0F, 6.0F, 12.0F, 16.0F);
      var9.lineTo(20.0F, 20.0F);
      var9.curveTo(26.0F, 14.0F, 32.0F, 14.0F, 40.0F, 18.0F);
      var9.closePath();
      var0.fill(var9);
      var0.setColor(var5);
      var0.fill(new java.awt.geom.Ellipse2D.Float(4.0F, 12.0F, 14.0F, 14.0F));
      var0.setPaint(new GradientPaint(0.0F, 14.0F, var5, 0.0F, 24.0F, var6));
      var0.fill(new Float(32.0F, 15.0F, 44.0F, 10.0F, 10.0F, 10.0F));
   }

   private static void paintCloud(Graphics2D var0, boolean var1) {
      Color var2 = var1 ? new Color(16773592) : new Color(16777215);
      Color var3 = var1 ? new Color(14924175) : new Color(12111082);
      Area var4 = GameFx.union(
         new java.awt.geom.Ellipse2D.Float(2.0F, 6.0F, 18.0F, 14.0F),
         new java.awt.geom.Ellipse2D.Float(12.0F, 1.0F, 18.0F, 16.0F),
         new java.awt.geom.Ellipse2D.Float(24.0F, 2.0F, 16.0F, 15.0F),
         new java.awt.geom.Ellipse2D.Float(33.0F, 6.0F, 17.0F, 14.0F),
         new Float(5.0F, 9.0F, 42.0F, 11.0F, 10.0F, 10.0F)
      );
      var0.setColor(new Color(20, 30, 70, 60));
      var0.translate(0.0, 1.2);
      var0.fill(var4);
      var0.translate(0.0, -1.2);
      var0.setPaint(new GradientPaint(0.0F, 2.0F, var2, 0.0F, 20.0F, var3));
      var0.fill(var4);
      var0.setColor(new Color(255, 255, 255, 200));
      var0.fill(new java.awt.geom.Ellipse2D.Float(15.0F, 3.5F, 9.0F, 4.0F));
      var0.fill(new java.awt.geom.Ellipse2D.Float(27.0F, 4.5F, 7.0F, 3.0F));
      if (var1) {
         var0.setColor(new Color(14886459));
         var0.fill(new java.awt.geom.Ellipse2D.Float(8.0F, 15.0F, 3.0F, 3.0F));
         var0.setColor(new Color(2073436));
         var0.fill(new java.awt.geom.Ellipse2D.Float(25.0F, 16.0F, 3.0F, 3.0F));
         var0.setColor(new Color(16040274));
         var0.fill(new java.awt.geom.Ellipse2D.Float(40.0F, 15.0F, 3.0F, 3.0F));
      }
   }

   private static void paintIce(Graphics2D var0, boolean var1) {
      Float var2 = new Float(1.0F, 3.0F, 44.0F, 10.0F, 5.0F, 5.0F);
      var0.setPaint(new GradientPaint(0.0F, 3.0F, new Color(220, 242, 255, 230), 0.0F, 13.0F, new Color(120, 180, 235, 220)));
      var0.fill(var2);
      var0.setColor(new Color(255, 255, 255, 200));
      var0.fill(new Float(4.0F, 4.0F, 36.0F, 2.2F, 2.0F, 2.0F));
      var0.setColor(new Color(70, 130, 200, 200));
      var0.setStroke(new BasicStroke(0.9F));
      var0.drawLine(12, 4, 16, 9);
      var0.drawLine(16, 9, 14, 13);
      var0.drawLine(28, 3, 25, 8);
      var0.drawLine(25, 8, 30, 12);
      var0.drawLine(38, 5, 36, 10);
      var0.draw(var2);
      var0.setColor(new Color(200, 232, 255, 220));

      for (int var3 = 0; var3 < 5; var3++) {
         int var4 = 6 + var3 * 8;
         var0.fillPolygon(new int[]{var4, var4 + 3, var4 + 1}, new int[]{13, 13, 15 + var3 % 2 * 1}, 3);
      }
   }

   private static void paintChimney(Graphics2D var0) {
      var0.setPaint(new GradientPaint(0.0F, 26.0F, new Color(9059118), 0.0F, 42.0F, new Color(5906969)));
      var0.fill(new Float(2.0F, 28.0F, 44.0F, 14.0F, 4.0F, 4.0F));
      var0.setColor(new Color(4068879));

      for (int var1 = 0; var1 < 6; var1++) {
         var0.fillRect(4 + var1 * 7, 35, 1, 6);
      }

      var0.setPaint(new GradientPaint(0.0F, 24.0F, Color.WHITE, 0.0F, 32.0F, new Color(13228786)));
      var0.fill(new Float(0.0F, 24.0F, 48.0F, 7.0F, 7.0F, 7.0F));
      var0.setPaint(new GradientPaint(26.0F, 0.0F, new Color(11684408), 40.0F, 0.0F, new Color(8006174)));
      var0.fill(new java.awt.geom.Rectangle2D.Float(26.0F, 6.0F, 14.0F, 20.0F));
      var0.setColor(new Color(5905940));
      var0.setStroke(new BasicStroke(0.8F));

      for (int var3 = 0; var3 < 4; var3++) {
         var0.drawLine(26, 10 + var3 * 5, 40, 10 + var3 * 5);
         int var2 = var3 % 2 == 0 ? 33 : 30;
         var0.drawLine(var2, 10 + var3 * 5, var2, 15 + var3 * 5);
      }

      var0.setColor(new Color(3805708));
      var0.fill(new java.awt.geom.Rectangle2D.Float(24.0F, 3.0F, 18.0F, 5.0F));
      var0.setColor(Color.WHITE);
      var0.fill(new Float(23.0F, 1.0F, 20.0F, 4.0F, 4.0F, 4.0F));
      var0.setColor(new Color(16765562));
      var0.fillPolygon(new int[]{33, 29, 37}, new int[]{-1, 3, 3}, 3);
   }

   private static void paintStorm(Graphics2D var0, boolean var1, boolean var2) {
      Area var3 = GameFx.union(
         new java.awt.geom.Ellipse2D.Float(3.0F, 12.0F, 20.0F, 16.0F),
         new java.awt.geom.Ellipse2D.Float(13.0F, 4.0F, 22.0F, 20.0F),
         new java.awt.geom.Ellipse2D.Float(27.0F, 10.0F, 20.0F, 17.0F),
         new Float(6.0F, 16.0F, 38.0F, 12.0F, 11.0F, 11.0F)
      );
      var0.setPaint(new GradientPaint(0.0F, 4.0F, var2 ? new Color(9213094) : new Color(5989240), 0.0F, 28.0F, var2 ? new Color(5922928) : new Color(2764606)));
      var0.fill(var3);
      var0.setColor(new Color(255, 255, 255, 40));
      var0.fill(new java.awt.geom.Ellipse2D.Float(17.0F, 7.0F, 10.0F, 4.0F));
      var0.setColor(Color.WHITE);
      var0.fill(new java.awt.geom.Ellipse2D.Float(16.0F, 14.0F, 7.0F, 7.0F));
      var0.fill(new java.awt.geom.Ellipse2D.Float(28.0F, 14.0F, 7.0F, 7.0F));
      var0.setColor(new Color(1119002));
      var0.fill(new java.awt.geom.Ellipse2D.Float(18.5F, 16.5F, 3.4F, 3.4F));
      var0.fill(new java.awt.geom.Ellipse2D.Float(29.5F, 16.5F, 3.4F, 3.4F));
      var0.setStroke(new BasicStroke(1.8F, 1, 1));
      var0.drawLine(15, 12, 23, 15);
      var0.drawLine(36, 12, 28, 15);
      var0.drawArc(21, 22, 9, 5, 0, 180);
      if (var1) {
         GeneralPath var4 = new GeneralPath();
         var4.moveTo(24.0F, 27.0F);
         var4.lineTo(19.0F, 34.0F);
         var4.lineTo(24.0F, 34.0F);
         var4.lineTo(20.0F, 42.0F);
         var4.lineTo(30.0F, 32.0F);
         var4.lineTo(25.0F, 32.0F);
         var4.lineTo(29.0F, 27.0F);
         var4.closePath();
         var0.setColor(new Color(16770667));
         var0.fill(var4);
      }
   }

   static void paintGift(Graphics2D var0, int var1) {
      int var2 = var1 == 5 ? -736942 : GIFT_BOX[var1];
      int var3 = var1 == 5 ? -1 : GIFT_RIB[var1];
      var0.setPaint(new GradientPaint(0.0F, 12.0F, new Color(Gx.mix(var2, -1, 0.2F)), 0.0F, 38.0F, new Color(Gx.mix(var2, -16777216, 0.3F))));
      var0.fill(new Float(5.0F, 14.0F, 30.0F, 24.0F, 5.0F, 5.0F));
      var0.setPaint(new GradientPaint(0.0F, 9.0F, new Color(Gx.mix(var2, -1, 0.3F)), 0.0F, 17.0F, new Color(var2)));
      var0.fill(new Float(3.0F, 10.0F, 34.0F, 8.0F, 4.0F, 4.0F));
      var0.setColor(new Color(var3));
      var0.fill(new java.awt.geom.Rectangle2D.Float(17.0F, 10.0F, 6.0F, 28.0F));
      var0.setStroke(new BasicStroke(3.2F));
      var0.drawOval(9, 2, 10, 9);
      var0.drawOval(21, 2, 10, 9);
      var0.setColor(new Color(255, 255, 255, 90));
      var0.fill(new java.awt.geom.Rectangle2D.Float(7.0F, 19.0F, 3.0F, 16.0F));
   }

   private static void paintTown(Graphics2D var0) {
      Random var1 = new Random(9L);
      var0.setColor(new Color(1254471));
      var0.fill(new java.awt.geom.Ellipse2D.Float(-60.0F, 40.0F, 220.0F, 120.0F));
      var0.fill(new java.awt.geom.Ellipse2D.Float(130.0F, 30.0F, 240.0F, 140.0F));
      var0.fill(new java.awt.geom.Ellipse2D.Float(250.0F, 44.0F, 260.0F, 120.0F));

      for (int var2 = 0; var2 < 14; var2++) {
         float var3 = 8 + var2 * 33 + var1.nextFloat() * 10.0F;
         float var4 = 56.0F + var1.nextFloat() * 10.0F;
         float var5 = 0.7F + var1.nextFloat() * 0.5F;
         var0.setColor(new Color(932660));
         var0.fillPolygon(
            new int[]{(int)var3, (int)(var3 - 10.0F * var5), (int)(var3 + 10.0F * var5)},
            new int[]{(int)(var4 - 26.0F * var5), (int)(var4 + 6.0F), (int)(var4 + 6.0F)},
            3
         );
         var0.setColor(new Color(220, 235, 255, 180));
         var0.fillPolygon(
            new int[]{(int)var3, (int)(var3 - 4.0F * var5), (int)(var3 + 4.0F * var5)},
            new int[]{(int)(var4 - 26.0F * var5), (int)(var4 - 16.0F * var5), (int)(var4 - 16.0F * var5)},
            3
         );
      }

      float[] var12 = new float[]{10.0F, 70.0F, 150.0F, 215.0F, 262.0F, 312.0F, 366.0F, 414.0F};

      for (int var13 = 0; var13 < var12.length; var13++) {
         float var14 = 38.0F + var1.nextFloat() * 14.0F;
         float var15 = 26.0F + var1.nextFloat() * 18.0F;
         float var6 = var12[var13];
         float var7 = 110.0F - var15;
         var0.setColor(new Color(1715026));
         var0.fill(new java.awt.geom.Rectangle2D.Float(var6, var7, var14, var15));
         GeneralPath var8 = new GeneralPath();
         var8.moveTo(var6 - 4.0F, var7);
         var8.lineTo(var6 + var14 / 2.0F, var7 - 18.0F);
         var8.lineTo(var6 + var14 + 4.0F, var7);
         var8.closePath();
         var0.setColor(new Color(2373478));
         var0.fill(var8);
         var0.setColor(new Color(15397631));
         var0.setStroke(new BasicStroke(3.0F, 1, 1));
         var0.drawLine((int)(var6 - 3.0F), (int)var7, (int)(var6 + var14 / 2.0F), (int)(var7 - 17.0F));
         var0.drawLine((int)(var6 + var14 / 2.0F), (int)(var7 - 17.0F), (int)(var6 + var14 + 3.0F), (int)var7);

         for (int var9 = 0; var9 < 2; var9++) {
            float var10 = var6 + 7.0F + var9 * (var14 - 21.0F);
            float var11 = var7 + 7.0F;
            var0.setPaint(
               new RadialGradientPaint(
                  new java.awt.geom.Point2D.Float(var10 + 3.5F, var11 + 4.0F),
                  12.0F,
                  new float[]{0.0F, 1.0F},
                  new Color[]{new Color(255, 200, 90, 120), new Color(255, 200, 90, 0)}
               )
            );
            var0.fill(new java.awt.geom.Ellipse2D.Float(var10 - 8.0F, var11 - 7.0F, 23.0F, 23.0F));
            var0.setColor(new Color(16765562));
            var0.fill(new java.awt.geom.Rectangle2D.Float(var10, var11, 7.0F, 8.0F));
         }
      }

      var0.setPaint(new GradientPaint(0.0F, 104.0F, new Color(14543355), 0.0F, 110.0F, new Color(11125994)));
      var0.fill(new java.awt.geom.Rectangle2D.Float(0.0F, 106.0F, 460.0F, 4.0F));
   }

   private static void paintAurora(Graphics2D var0) {
      int[][] var1 = new int[][]{{60, 255, 170}, {90, 200, 255}, {190, 120, 255}};

      for (int var2 = 0; var2 < 3; var2++) {
         int[] var3 = var1[var2];

         for (byte var4 = 0; var4 < 460; var4 += 2) {
            double var5 = 50 + var2 * 24 + Math.sin(var4 / 45.0 + var2 * 1.7) * 18.0 + Math.sin(var4 / 17.0 + var2) * 4.0;
            int var7 = (int)(80.0 * Math.sin(Math.PI * var4 / 460.0));
            var0.setPaint(
               new GradientPaint(
                  var4, (float)(var5 - 40.0), new Color(var3[0], var3[1], var3[2], 0), var4, (float)var5, new Color(var3[0], var3[1], var3[2], var7)
               )
            );
            var0.fillRect(var4, (int)(var5 - 40.0), 2, 40);
            var0.setPaint(
               new GradientPaint(
                  var4, (float)var5, new Color(var3[0], var3[1], var3[2], var7), var4, (float)(var5 + 10.0), new Color(var3[0], var3[1], var3[2], 0)
               )
            );
            var0.fillRect(var4, (int)var5, 2, 10);
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
