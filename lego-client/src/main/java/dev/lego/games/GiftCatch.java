package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.geom.GeneralPath;
import java.awt.geom.Ellipse2D.Float;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class GiftCatch extends GameView {
   private static final float W = 400.0F;
   private static final float H = 280.0F;
   private static final float GROUND = 258.0F;
   private static final float SLEIGH_Y = 236.0F;
   private static final float SW = 66.0F;
   private static final int GIFT = 0;
   private static final int GOLD = 1;
   private static final int COAL = 2;
   private static final int STAR = 3;
   private static final int CLOCK = 4;
   private static final int HEART = 5;
   private static final int[] BULBS = new int[]{-50354, -14006, -12853110, -11884289, -33835};
   private final List<GiftCatch.Item> items = new ArrayList<>();
   private final List<Integer> pile = new ArrayList<>();
   private final GameFx fx = new GameFx();
   private float sx;
   private float svx;
   private float spawnT;
   private float squash;
   private float lean;
   private float anim;
   private float doubleT;
   private float slowT;
   private float hurtT;
   private float lastMx = -9999.0F;
   private float hintT;
   private int lives;
   private int combo;
   private int caught;
   private int bestCombo;
   private boolean left;
   private boolean right;
   private boolean mouseMode;

   public GiftCatch() {
      super("giftcatch");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 280.0F;
   }

   @Override
   protected void reset() {
      this.items.clear();
      this.pile.clear();
      this.fx.clear();
      this.sx = 200.0F;
      this.svx = 0.0F;
      this.spawnT = 0.6F;
      this.squash = 0.0F;
      this.lean = 0.0F;
      this.lives = 3;
      this.combo = 0;
      this.caught = 0;
      this.bestCombo = 0;
      this.doubleT = this.slowT = this.hurtT = this.hintT = 0.0F;
      this.left = this.right = false;
   }

   private float level() {
      return Math.min(1.0F, this.time / 150.0F);
   }

   private int mult() {
      return (this.combo >= 25 ? 4 : (this.combo >= 12 ? 3 : (this.combo >= 5 ? 2 : 1))) * (this.doubleT > 0.0F ? 2 : 1);
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
      this.hintT += var1;
      this.doubleT = Math.max(0.0F, this.doubleT - var1);
      this.slowT = Math.max(0.0F, this.slowT - var1);
      this.hurtT = Math.max(0.0F, this.hurtT - var1);
      this.squash = Math.max(0.0F, this.squash - var1 * 4.0F);
      float var2 = var1 * (this.slowT > 0.0F ? 0.5F : 1.0F);
      if (Math.abs(this.mx - this.lastMx) > 0.5F && this.mx >= 0.0F) {
         if (this.lastMx > -9999.0F) {
            this.mouseMode = true;
         }

         this.lastMx = this.mx;
      }

      float var3 = this.sx;
      if (this.mouseMode && this.u > 0.0F) {
         float var8 = (this.mx - this.bx) / this.u;
         this.sx = this.sx + (var8 - this.sx) * Math.min(1.0F, var1 * 16.0F);
      } else {
         float var4 = (this.right ? 1 : 0) - (this.left ? 1 : 0);
         this.svx = this.svx + (var4 * 330.0F - this.svx) * Math.min(1.0F, var1 * 10.0F);
         this.sx = this.sx + this.svx * var1;
      }

      this.sx = Math.max(35.0F, Math.min(365.0F, this.sx));
      float var9 = (this.sx - var3) / var1;
      this.lean = this.lean + (Math.max(-1.0F, Math.min(1.0F, var9 / 300.0F)) - this.lean) * Math.min(1.0F, var1 * 10.0F);
      this.spawnT -= var2;
      if (this.spawnT <= 0.0F) {
         float var5 = this.level();
         this.spawnT = Ease.lerp(1.05F, 0.34F, var5) * (0.75F + this.rnd.nextFloat() * 0.5F);
         this.spawn(var5);
      }

      for (int var10 = this.items.size() - 1; var10 >= 0; var10--) {
         GiftCatch.Item var6 = this.items.get(var10);
         var6.t += var2;
         var6.y = var6.y + var6.vy * var2;
         var6.x = var6.x + (float)Math.cos(var6.t * 2.2F + var6.sway) * var6.sway * 6.0F * var2;
         float var7 = var6.size / 2.0F;
         if (var6.y + var7 >= 228.0F && var6.y - var7 <= 242.0F && Math.abs(var6.x - this.sx) < 33.0F + var7 * 0.4F) {
            this.items.remove(var10);
            this.catchItem(var6);
         } else if (var6.y - var7 > 258.0F) {
            this.items.remove(var10);
            if (var6.type != 0 && var6.type != 1) {
               this.fx.burst(var6.x, 258.0F, 6, -1445121, 50.0F, 3.0F, 0.4F, 100.0F, 4);
            } else {
               this.miss(var6);
            }
         }
      }
   }

   private void spawn(float var1) {
      GiftCatch.Item var2 = new GiftCatch.Item();
      float var3 = this.rnd.nextFloat();
      float var4 = 0.12F + var1 * 0.22F;
      if (var3 < var4) {
         var2.type = 2;
      } else if (var3 < var4 + 0.04F) {
         var2.type = 1;
      } else if (var3 < var4 + 0.065F) {
         var2.type = this.rnd.nextInt(3) == 0 && this.lives < 3 ? 5 : (this.rnd.nextBoolean() ? 3 : 4);
      } else {
         var2.type = 0;
      }

      var2.color = this.rnd.nextInt(5);
      var2.size = var2.type == 0 ? 17.0F + this.rnd.nextFloat() * 5.0F : (var2.type == 2 ? 17.0F : 18.0F);
      var2.x = 16.0F + this.rnd.nextFloat() * 368.0F;
      var2.y = -14.0F;
      var2.vy = Ease.lerp(72.0F, 175.0F, var1) * (0.85F + this.rnd.nextFloat() * 0.3F) * (var2.type == 1 ? 1.25F : 1.0F);
      var2.sway = this.rnd.nextFloat() * 2.5F;
      var2.t = this.rnd.nextFloat() * 6.0F;
      this.items.add(var2);
   }

   private void catchItem(GiftCatch.Item var1) {
      this.squash = 1.0F;
      float var2 = 222.0F;
      switch (var1.type) {
         case 2:
            this.lives--;
            this.combo = 0;
            this.hurtT = 0.6F;
            this.fx.burst(var1.x, var2, 22, -14013904, 150.0F, 4.5F, 0.7F, 200.0F, 0);
            this.fx.burst(var1.x, var2, 10, -34258, 120.0F, 2.2F, 0.5F, 100.0F, 2);
            this.fx.shake(0.7F);
            this.fx.flash(-50354, 0.7F);
            this.fx.popup("Kohle!", var1.x, var2 - 10.0F, -38037, 12.0F);
            if (!this.pile.isEmpty()) {
               this.pile.remove(this.pile.size() - 1);
            }

            Sound.play("minecraft:entity.generic.extinguish_fire", 0.8F, 0.4F);
            if (this.lives <= 0) {
               this.gameOver();
            }

            return;
         case 3:
            this.doubleT = 7.0F;
            this.fx.burst(var1.x, var2, 24, -736942, 170.0F, 3.0F, 0.8F, 0.0F, 2);
            this.fx.popup("Doppelte Punkte!", var1.x, var2 - 14.0F, -736942, 11.0F);
            Sound.play("minecraft:block.amethyst_block.chime", 1.4F, 0.8F);
            return;
         case 4:
            this.slowT = 6.0F;
            this.fx.ring(var1.x, var2, 60.0F, -8587265, 0.6F);
            this.fx.popup("Zeitlupe!", var1.x, var2 - 14.0F, -8587265, 11.0F);
            Sound.play("minecraft:block.note_block.chime", 0.7F, 0.5F);
            return;
         case 5:
            this.lives = Math.min(3, this.lives + 1);
            this.fx.burst(var1.x, var2, 16, -45715, 120.0F, 3.0F, 0.7F, 0.0F, 2);
            this.fx.popup("+1 Leben", var1.x, var2 - 14.0F, -38006, 11.0F);
            Sound.play("minecraft:entity.player.levelup", 1.6F, 0.35F);
            return;
         default:
            this.combo++;
            this.bestCombo = Math.max(this.bestCombo, this.combo);
            this.caught++;
            int var3 = var1.type == 1 ? 50 : 10;
            int var4 = var3 * this.mult();
            this.score += var4;
            this.pile.add(var1.type == 1 ? 5 : var1.color);
            if (this.pile.size() > 5) {
               this.pile.remove(0);
            }

            this.fx.confetti(var1.x, var2, var1.type == 1 ? 26 : 14, var1.type == 1 ? new int[]{-736942, -5720, -1} : BULBS, 150.0F, 3.5F);
            this.fx.ring(var1.x, var2 + 4.0F, 26.0F, var1.type == 1 ? -736942 : -1, 0.3F);
            this.fx.popup("+" + var4, var1.x, var2 - 8.0F, var1.type == 1 ? -736942 : -1, var1.type == 1 ? 13.0F : 10.0F);
            if (this.combo == 5 || this.combo == 12 || this.combo == 25) {
               this.fx.popup("Combo x" + (this.combo >= 25 ? 4 : (this.combo >= 12 ? 3 : 2)) + "!", 200.0F, 90.0F, -33835, 15.0F);
               this.fx.shake(0.25F);
               Sound.play("minecraft:entity.player.levelup", 1.3F, 0.3F);
            }

            Sound.play("minecraft:entity.item.pickup", 1.0F + Math.min(1.0F, this.combo * 0.03F), 0.35F);
      }
   }

   private void miss(GiftCatch.Item var1) {
      this.lives--;
      this.combo = 0;
      this.hurtT = 0.5F;
      this.fx.confetti(var1.x, 254.0F, 12, new int[]{-7696487, -1445121}, 90.0F, 3.0F);
      this.fx.burst(var1.x, 258.0F, 10, -1445121, 70.0F, 3.0F, 0.5F, 60.0F, 4);
      this.fx.popup("Verpasst!", var1.x, 238.0F, -20304, 10.0F);
      this.fx.shake(0.35F);
      Sound.play("minecraft:block.snow.break", 0.7F, 0.6F);
      if (this.lives <= 0) {
         this.gameOver();
      }
   }

   @Override
   protected void render() {
      float var1 = Math.min(0.05F, Gx.dt);
      this.anim += var1;
      if (this.state == GameView.State.READY) {
         this.sx = 200.0F + (float)Math.sin(this.anim * 1.3F) * 80.0F;
      }

      GameFx.sprite(this, "gc_bg", 0.0F, 0.0F, 400.0F, 280.0F, 380.0F, 280.0F, GiftCatch::paintBackground, -1);

      for (int var2 = 0; var2 < 40; var2++) {
         float var3 = (var2 * 97.3F + this.anim * (6 + var2 % 5 * 3)) % 400.0F;
         float var4 = (var2 * 53.7F + this.anim * (14 + var2 % 7 * 4)) % 280.0F;
         this.circle(var3 + (float)Math.sin(this.anim + var2) * 4.0F, var4, 0.8F + var2 % 3 * 0.5F, -1996488705);
      }

      if (this.slowT > 0.0F) {
         this.box(0.0F, 0.0F, 400.0F, 280.0F, 0.0F, Gx.withAlpha(-8587265, 0.08F + 0.04F * (float)Math.sin(this.anim * 6.0F)));
      }

      Gx.push();
      Gx.translate(this.S(this.fx.sx), this.S(this.fx.sy));
      this.lights();

      for (GiftCatch.Item var8 : this.items) {
         this.item(var8);
      }

      this.sleigh();
      this.fx.draw(this, 0.0F, 0.0F);
      Gx.pop();

      for (int var6 = 0; var6 < 3; var6++) {
         boolean var9 = var6 < this.lives;
         float var12 = var9 && this.lives == 1 ? 1.0F + 0.12F * (float)Math.sin(this.anim * 10.0F) : 1.0F;
         Gx.icon(var9 ? "heart-fill" : "heart", this.X(16 + var6 * 15), this.Y(34.0F), 11.0F * this.u * var12, var9 ? -45715 : 1442840575);
      }

      int var7 = this.mult();
      if (this.combo > 0 || var7 > 1) {
         float var10 = 70.0F;
         this.box(400.0F - var10 - 8.0F, 25.0F, var10, 18.0F, 9.0F, var7 > 1 ? Gx.withAlpha(-1890757, 0.9F) : 1711276032);
         this.text((var7 > 1 ? "x" + var7 + "  " : "") + this.combo + " Combo", 400.0F - var10 / 2.0F - 8.0F, 34.0F, 7.0F, 3, -1);
      }

      float var11 = 50.0F;
      if (this.doubleT > 0.0F) {
         this.power(392.0F, var11, "star-fill", -736942, this.doubleT / 7.0F);
         var11 += 16.0F;
      }

      if (this.slowT > 0.0F) {
         this.power(392.0F, var11, "clock", -8587265, this.slowT / 6.0F);
      }

      if (this.state == GameView.State.PLAYING && this.hintT < 3.5F) {
         float var13 = Math.min(1.0F, 3.5F - this.hintT) * (0.6F + 0.4F * (float)Math.sin(this.anim * 5.0F));
         this.box(90.0F, 120.0F, 220.0F, 22.0F, 11.0F, Gx.withAlpha(-16777216, 0.45F * var13));
         this.text("Maus oder ← → – fang die Geschenke, keine Kohle!", 200.0F, 131.0F, 7.2F, 3, Gx.withAlpha(-1, var13));
      }

      this.fx.drawPopups(this, 0.0F, 0.0F);
      if (this.hurtT > 0.0F) {
         this.box(0.0F, 0.0F, 400.0F, 280.0F, 0.0F, Gx.withAlpha(-57280, this.hurtT * 0.25F));
      }

      this.fx.drawFlash(this, 400.0F, 280.0F);
   }

   private void power(float var1, float var2, String var3, int var4, float var5) {
      float var6 = 52.0F;
      this.box(var1 - var6, var2 - 7.0F, var6, 14.0F, 7.0F, 1711276032);
      Gx.icon(var3, this.X(var1 - var6 + 8.0F), this.Y(var2), 8.0F * this.u, var4);
      this.box(var1 - var6 + 15.0F, var2 - 1.5F, var6 - 21.0F, 3.0F, 1.5F, 872415231);
      this.box(var1 - var6 + 15.0F, var2 - 1.5F, (var6 - 21.0F) * var5, 3.0F, 1.5F, var4);
   }

   private void lights() {
      for (int var1 = 0; var1 < 3; var1++) {
         float var2 = var1 * 400.0F / 3.0F;
         float var3 = (var1 + 1) * 400.0F / 3.0F;

         for (int var4 = 0; var4 <= 24; var4++) {
            float var5 = var4 / 24.0F;
            float var6 = Ease.lerp(var2, var3, var5);
            float var7 = 6.0F + (float)Math.sin(var5 * Math.PI) * 12.0F;
            this.circle(var6, var7, 0.7F, -15062498);
         }

         for (int var10 = 1; var10 < 6; var10++) {
            float var11 = var10 / 6.0F;
            float var12 = Ease.lerp(var2, var3, var11);
            float var13 = 7.0F + (float)Math.sin(var11 * Math.PI) * 12.0F;
            int var8 = BULBS[(var1 * 5 + var10) % BULBS.length];
            float var9 = 0.55F + 0.45F * (float)Math.sin(this.anim * 3.0F + var1 * 2 + var10 * 1.7F);
            Gx.glow(this.X(var12), this.Y(var13 + 3.0F), this.S(9.0F), Gx.withAlpha(var8, 0.45F * var9));
            this.box(var12 - 1.3F, var13 - 1.0F, 2.6F, 2.2F, 0.5F, -14009810);
            this.boxV(var12 - 2.0F, var13 + 0.8F, 4.0F, 5.5F, 2.0F, Gx.mix(var8, -1, 0.3F + 0.3F * var9), Gx.mix(var8, -16777216, 0.3F * (1.0F - var9)));
         }
      }
   }

   private void item(GiftCatch.Item var1) {
      float var2 = var1.size;
      float var3 = 1.0F + 0.05F * (float)Math.sin(var1.t * 7.0F);
      float var4 = var1.x;
      float var5 = var1.y;
      switch (var1.type) {
         case 2:
            Gx.glow(this.X(var4), this.Y(var5), this.S(var2), 872372766);
            GameFx.sprite(this, "gc_coal", var4 - var2 / 2.0F, var5 - var2 / 2.0F, var2, var2, 40.0F, 40.0F, GiftCatch::paintCoal, -1);
            break;
         case 3:
            Gx.glow(this.X(var4), this.Y(var5), this.S(var2 * 1.4F), 1727316306);
            Gx.icon("star-fill", this.X(var4), this.Y(var5), var2 * this.u * var3, -736942);
            break;
         case 4:
            Gx.glow(this.X(var4), this.Y(var5), this.S(var2 * 1.4F), 1434253311);
            this.circle(var4, var5, var2 / 2.0F, -14929072);
            Gx.icon("clock", this.X(var4), this.Y(var5), var2 * 0.8F * this.u, -8587265);
            break;
         case 5:
            Gx.glow(this.X(var4), this.Y(var5), this.S(var2 * 1.4F), 1728007533);
            Gx.icon("heart-fill", this.X(var4), this.Y(var5), var2 * this.u * var3, -45715);
            break;
         default:
            int var6 = var1.type == 1 ? 5 : var1.color;
            if (var1.type == 1) {
               Gx.glow(this.X(var4), this.Y(var5), this.S(var2 * 1.5F), 1727316306);
            }

            float var7 = var2 * var3;
            float var8 = var2 * (2.0F - var3);
            GameFx.spriteAt(
               this,
               "sj_gift" + var6,
               var2,
               var2,
               40.0F,
               40.0F,
               var1x -> SantaJump.paintGift(var1x, var6),
               var4 - var7 / 2.0F,
               var5 - var8 / 2.0F,
               var7,
               var8,
               -1
            );
      }
   }

   private void sleigh() {
      float var1 = Ease.outCubic(this.squash);
      float var2 = 66.0F * (1.0F + 0.1F * var1);
      float var3 = 30.0F * (1.0F - 0.12F * var1);
      float var4 = this.sx - var2 / 2.0F;
      float var5 = 258.0F - var3;
      this.box(this.sx - 29.699999F, 256.0F, 59.399998F, 4.0F, 2.0F, 855638016);
      if (this.doubleT > 0.0F) {
         Gx.glow(this.X(this.sx), this.Y(236.0F), this.S(50.0F), Gx.withAlpha(-736942, 0.25F));
      }

      for (int var6 = 0; var6 < this.pile.size(); var6++) {
         int var7 = this.pile.get(var6);
         float var8 = this.sx - 20.0F + var6 % 3 * 14 - this.lean * 3.0F * (var6 / 3 + 1);
         float var9 = 234.0F - var6 / 3 * 9 + var1 * 3.0F;
         GameFx.sprite(this, "sj_gift" + var7 + "s", var8 - 7.0F, var9 - 7.0F, 14.0F, 14.0F, 40.0F, 40.0F, var1x -> SantaJump.paintGift(var1x, var7), -1);
      }

      GameFx.spriteAt(this, "gc_sleigh", 66.0F, 30.0F, 66.0F, 30.0F, GiftCatch::paintSleigh, var4, var5, var2, var3, -1);
   }

   private static void paintBackground(Graphics2D var0) {
      var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(660528), 0.0F, 260.0F, new Color(2772614)));
      var0.fillRect(0, 0, 380, 280);
      Random var1 = new Random(12L);

      for (int var2 = 0; var2 < 70; var2++) {
         float var3 = var1.nextFloat() * 380.0F;
         float var4 = var1.nextFloat() * 170.0F;
         float var5 = 0.6F + var1.nextFloat() * 1.4F;
         var0.setColor(new Color(255, 255, 255, 60 + var1.nextInt(150)));
         var0.fill(new Float(var3, var4, var5, var5));
      }

      var0.setPaint(
         new RadialGradientPaint(
            new java.awt.geom.Point2D.Float(300.0F, 70.0F),
            60.0F,
            new float[]{0.0F, 1.0F},
            new Color[]{new Color(255, 244, 214, 90), new Color(255, 244, 214, 0)}
         )
      );
      var0.fill(new Float(240.0F, 10.0F, 120.0F, 120.0F));
      var0.setColor(new Color(16774358));
      var0.fill(new Float(286.0F, 56.0F, 28.0F, 28.0F));
      var0.setColor(new Color(0, 0, 0, 25));
      var0.fill(new Float(300.0F, 62.0F, 6.0F, 6.0F));
      var0.setColor(new Color(1783137));
      var0.fill(new Float(-80.0F, 170.0F, 300.0F, 160.0F));
      var0.fill(new Float(160.0F, 160.0F, 320.0F, 170.0F));
      var0.setColor(new Color(2311541));
      var0.fill(new Float(60.0F, 195.0F, 320.0F, 140.0F));

      for (int var14 = 0; var14 < 14; var14++) {
         float var16 = 6 + var14 * 28 + var1.nextFloat() * 8.0F;
         float var19 = 224.0F + var1.nextFloat() * 8.0F;
         float var23 = 0.6F + var1.nextFloat() * 0.6F;
         var0.setColor(new Color(866867));

         for (int var6 = 0; var6 < 3; var6++) {
            float var7 = var19 - var6 * 9 * var23;
            float var8 = (14.0F - var6 * 3.5F) * var23;
            var0.fillPolygon(new int[]{(int)var16, (int)(var16 - var8), (int)(var16 + var8)}, new int[]{(int)(var7 - 14.0F * var23), (int)var7, (int)var7}, 3);
         }

         var0.setColor(new Color(230, 240, 255, 170));
         var0.fillPolygon(
            new int[]{(int)var16, (int)(var16 - 4.0F * var23), (int)(var16 + 4.0F * var23)},
            new int[]{(int)(var19 - 32.0F * var23), (int)(var19 - 25.0F * var23), (int)(var19 - 25.0F * var23)},
            3
         );
      }

      float[] var15 = new float[]{30.0F, 150.0F, 270.0F};

      for (float var25 : var15) {
         float var26 = 46.0F;
         float var27 = 30.0F;
         float var9 = 244.0F - var27;
         var0.setColor(new Color(3811898));
         var0.fill(new java.awt.geom.Rectangle2D.Float(var25, var9, var26, var27));
         GeneralPath var10 = new GeneralPath();
         var10.moveTo(var25 - 5.0F, var9 + 1.0F);
         var10.lineTo(var25 + var26 / 2.0F, var9 - 18.0F);
         var10.lineTo(var25 + var26 + 5.0F, var9 + 1.0F);
         var10.closePath();
         var0.setColor(new Color(4861494));
         var0.fill(var10);
         var0.setColor(new Color(16054527));
         var0.setStroke(new BasicStroke(4.0F, 1, 1));
         var0.drawLine((int)(var25 - 4.0F), (int)var9, (int)(var25 + var26 / 2.0F), (int)(var9 - 17.0F));
         var0.drawLine((int)(var25 + var26 / 2.0F), (int)(var9 - 17.0F), (int)(var25 + var26 + 4.0F), (int)var9);

         for (int var11 = 0; var11 < 2; var11++) {
            float var12 = var25 + 8.0F + var11 * 22;
            float var13 = var9 + 9.0F;
            var0.setPaint(
               new RadialGradientPaint(
                  new java.awt.geom.Point2D.Float(var12 + 4.0F, var13 + 4.0F),
                  14.0F,
                  new float[]{0.0F, 1.0F},
                  new Color[]{new Color(255, 190, 90, 130), new Color(255, 190, 90, 0)}
               )
            );
            var0.fill(new Float(var12 - 10.0F, var13 - 10.0F, 28.0F, 28.0F));
            var0.setColor(new Color(16764782));
            var0.fill(new java.awt.geom.Rectangle2D.Float(var12, var13, 8.0F, 9.0F));
            var0.setColor(new Color(3811898));
            var0.fill(new java.awt.geom.Rectangle2D.Float(var12 + 3.5F, var13, 1.0F, 9.0F));
         }
      }

      GeneralPath var18 = new GeneralPath();
      var18.moveTo(0.0F, 280.0F);
      var18.lineTo(0.0F, 254.0F);

      for (byte var21 = 0; var21 <= 380; var21 += 10) {
         var18.lineTo(var21, 252.0 + Math.sin(var21 / 37.0) * 3.0);
      }

      var18.lineTo(380.0F, 280.0F);
      var18.closePath();
      var0.setPaint(new GradientPaint(0.0F, 250.0F, new Color(16054527), 0.0F, 280.0F, new Color(11914219)));
      var0.fill(var18);
      var0.setColor(new Color(255, 255, 255, 200));

      for (int var22 = 0; var22 < 60; var22++) {
         var0.fill(new Float(var1.nextFloat() * 380.0F, 258.0F + var1.nextFloat() * 20.0F, 1.5F, 1.5F));
      }
   }

   private static void paintSleigh(Graphics2D var0) {
      var0.setColor(new Color(16040274));
      var0.setStroke(new BasicStroke(2.4F, 1, 1));
      GeneralPath var1 = new GeneralPath();
      var1.moveTo(4.0F, 27.0F);
      var1.lineTo(56.0F, 27.0F);
      var1.curveTo(64.0F, 27.0F, 66.0F, 20.0F, 60.0F, 17.0F);
      var0.draw(var1);
      var0.drawLine(14, 27, 14, 22);
      var0.drawLine(44, 27, 44, 22);
      GeneralPath var2 = new GeneralPath();
      var2.moveTo(2.0F, 6.0F);
      var2.curveTo(0.0F, 16.0F, 6.0F, 23.0F, 16.0F, 23.0F);
      var2.lineTo(52.0F, 23.0F);
      var2.curveTo(60.0F, 23.0F, 64.0F, 16.0F, 62.0F, 8.0F);
      var2.lineTo(56.0F, 10.0F);
      var2.lineTo(10.0F, 10.0F);
      var2.closePath();
      var0.setPaint(new GradientPaint(0.0F, 6.0F, new Color(15742538), 0.0F, 23.0F, new Color(9309728)));
      var0.fill(var2);
      var0.setColor(new Color(6162452));
      var0.setStroke(new BasicStroke(1.0F));
      var0.draw(var2);
      var0.setColor(new Color(16040274));
      var0.setStroke(new BasicStroke(1.6F, 1, 1));
      var0.drawLine(10, 11, 56, 11);
      var0.drawArc(22, 13, 8, 7, 0, 300);
      var0.drawArc(36, 13, 8, 7, 240, 300);
      var0.setColor(new Color(255, 255, 255, 90));
      var0.fill(new java.awt.geom.RoundRectangle2D.Float(12.0F, 13.0F, 6.0F, 2.0F, 2.0F, 2.0F));
      var0.setColor(new Color(12917294));
      var0.fill(new Float(57.0F, 3.0F, 8.0F, 8.0F));
      var0.setColor(new Color(16040274));
      var0.fill(new Float(59.5F, 5.5F, 3.0F, 3.0F));
      var0.setColor(new Color(16054527));
      var0.fill(new java.awt.geom.RoundRectangle2D.Float(1.0F, 4.0F, 12.0F, 4.0F, 4.0F, 4.0F));
   }

   private static void paintCoal(Graphics2D var0) {
      GeneralPath var1 = new GeneralPath();
      var1.moveTo(8.0F, 14.0F);
      var1.lineTo(18.0F, 5.0F);
      var1.lineTo(30.0F, 8.0F);
      var1.lineTo(36.0F, 20.0F);
      var1.lineTo(31.0F, 33.0F);
      var1.lineTo(17.0F, 36.0F);
      var1.lineTo(6.0F, 28.0F);
      var1.closePath();
      var0.setPaint(new GradientPaint(0.0F, 5.0F, new Color(4868693), 0.0F, 36.0F, new Color(1184278)));
      var0.fill(var1);
      var0.setColor(new Color(6974072));
      var0.fill(new Polygon(new int[]{18, 30, 22}, new int[]{5, 8, 14}, 3));
      var0.setColor(new Color(255, 110, 40, 200));
      var0.fill(new Float(14.0F, 24.0F, 4.0F, 3.0F));
      var0.fill(new Float(26.0F, 18.0F, 3.0F, 3.0F));
      var0.setColor(new Color(16738874));
      var0.setStroke(new BasicStroke(2.0F, 1, 1));
      var0.drawLine(13, 15, 18, 17);
      var0.drawLine(29, 15, 24, 17);
      var0.fill(new Float(14.0F, 17.0F, 3.0F, 3.0F));
      var0.fill(new Float(24.0F, 17.0F, 3.0F, 3.0F));
      var0.drawArc(16, 25, 10, 5, 0, 180);
   }

   private static final class Item {
      float x;
      float y;
      float vy;
      float size;
      float t;
      float sway;
      int type;
      int color;
   }
}
