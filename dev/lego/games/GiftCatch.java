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
   protected void update(float rdt) {
      this.fx.update(rdt);
      this.hintT += rdt;
      this.doubleT = Math.max(0.0F, this.doubleT - rdt);
      this.slowT = Math.max(0.0F, this.slowT - rdt);
      this.hurtT = Math.max(0.0F, this.hurtT - rdt);
      this.squash = Math.max(0.0F, this.squash - rdt * 4.0F);
      float dt = rdt * (this.slowT > 0.0F ? 0.5F : 1.0F);
      if (Math.abs(this.mx - this.lastMx) > 0.5F && this.mx >= 0.0F) {
         if (this.lastMx > -9999.0F) {
            this.mouseMode = true;
         }

         this.lastMx = this.mx;
      }

      float prev = this.sx;
      if (this.mouseMode && this.u > 0.0F) {
         float target = (this.mx - this.bx) / this.u;
         this.sx = this.sx + (target - this.sx) * Math.min(1.0F, rdt * 16.0F);
      } else {
         float dir = (this.right ? 1 : 0) - (this.left ? 1 : 0);
         this.svx = this.svx + (dir * 330.0F - this.svx) * Math.min(1.0F, rdt * 10.0F);
         this.sx = this.sx + this.svx * rdt;
      }

      this.sx = Math.max(35.0F, Math.min(365.0F, this.sx));
      float v = (this.sx - prev) / rdt;
      this.lean = this.lean + (Math.max(-1.0F, Math.min(1.0F, v / 300.0F)) - this.lean) * Math.min(1.0F, rdt * 10.0F);
      this.spawnT -= dt;
      if (this.spawnT <= 0.0F) {
         float lv = this.level();
         this.spawnT = Ease.lerp(1.05F, 0.34F, lv) * (0.75F + this.rnd.nextFloat() * 0.5F);
         this.spawn(lv);
      }

      for (int i = this.items.size() - 1; i >= 0; i--) {
         GiftCatch.Item it = this.items.get(i);
         it.t += dt;
         it.y = it.y + it.vy * dt;
         it.x = it.x + (float)Math.cos(it.t * 2.2F + it.sway) * it.sway * 6.0F * dt;
         float half = it.size / 2.0F;
         if (it.y + half >= 228.0F && it.y - half <= 242.0F && Math.abs(it.x - this.sx) < 33.0F + half * 0.4F) {
            this.items.remove(i);
            this.catchItem(it);
         } else if (it.y - half > 258.0F) {
            this.items.remove(i);
            if (it.type != 0 && it.type != 1) {
               this.fx.burst(it.x, 258.0F, 6, -1445121, 50.0F, 3.0F, 0.4F, 100.0F, 4);
            } else {
               this.miss(it);
            }
         }
      }
   }

   private void spawn(float lv) {
      GiftCatch.Item it = new GiftCatch.Item();
      float r = this.rnd.nextFloat();
      float coalP = 0.12F + lv * 0.22F;
      if (r < coalP) {
         it.type = 2;
      } else if (r < coalP + 0.04F) {
         it.type = 1;
      } else if (r < coalP + 0.065F) {
         it.type = this.rnd.nextInt(3) == 0 && this.lives < 3 ? 5 : (this.rnd.nextBoolean() ? 3 : 4);
      } else {
         it.type = 0;
      }

      it.color = this.rnd.nextInt(5);
      it.size = it.type == 0 ? 17.0F + this.rnd.nextFloat() * 5.0F : (it.type == 2 ? 17.0F : 18.0F);
      it.x = 16.0F + this.rnd.nextFloat() * 368.0F;
      it.y = -14.0F;
      it.vy = Ease.lerp(72.0F, 175.0F, lv) * (0.85F + this.rnd.nextFloat() * 0.3F) * (it.type == 1 ? 1.25F : 1.0F);
      it.sway = this.rnd.nextFloat() * 2.5F;
      it.t = this.rnd.nextFloat() * 6.0F;
      this.items.add(it);
   }

   private void catchItem(GiftCatch.Item it) {
      this.squash = 1.0F;
      float y = 222.0F;
      switch (it.type) {
         case 2:
            this.lives--;
            this.combo = 0;
            this.hurtT = 0.6F;
            this.fx.burst(it.x, y, 22, -14013904, 150.0F, 4.5F, 0.7F, 200.0F, 0);
            this.fx.burst(it.x, y, 10, -34258, 120.0F, 2.2F, 0.5F, 100.0F, 2);
            this.fx.shake(0.7F);
            this.fx.flash(-50354, 0.7F);
            this.fx.popup("Kohle!", it.x, y - 10.0F, -38037, 12.0F);
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
            this.fx.burst(it.x, y, 24, -736942, 170.0F, 3.0F, 0.8F, 0.0F, 2);
            this.fx.popup("Doppelte Punkte!", it.x, y - 14.0F, -736942, 11.0F);
            Sound.play("minecraft:block.amethyst_block.chime", 1.4F, 0.8F);
            return;
         case 4:
            this.slowT = 6.0F;
            this.fx.ring(it.x, y, 60.0F, -8587265, 0.6F);
            this.fx.popup("Zeitlupe!", it.x, y - 14.0F, -8587265, 11.0F);
            Sound.play("minecraft:block.note_block.chime", 0.7F, 0.5F);
            return;
         case 5:
            this.lives = Math.min(3, this.lives + 1);
            this.fx.burst(it.x, y, 16, -45715, 120.0F, 3.0F, 0.7F, 0.0F, 2);
            this.fx.popup("+1 Leben", it.x, y - 14.0F, -38006, 11.0F);
            Sound.play("minecraft:entity.player.levelup", 1.6F, 0.35F);
            return;
         default:
            this.combo++;
            this.bestCombo = Math.max(this.bestCombo, this.combo);
            this.caught++;
            int base = it.type == 1 ? 50 : 10;
            int pts = base * this.mult();
            this.score += pts;
            this.pile.add(it.type == 1 ? 5 : it.color);
            if (this.pile.size() > 5) {
               this.pile.remove(0);
            }

            this.fx.confetti(it.x, y, it.type == 1 ? 26 : 14, it.type == 1 ? new int[]{-736942, -5720, -1} : BULBS, 150.0F, 3.5F);
            this.fx.ring(it.x, y + 4.0F, 26.0F, it.type == 1 ? -736942 : -1, 0.3F);
            this.fx.popup("+" + pts, it.x, y - 8.0F, it.type == 1 ? -736942 : -1, it.type == 1 ? 13.0F : 10.0F);
            if (this.combo == 5 || this.combo == 12 || this.combo == 25) {
               this.fx.popup("Combo x" + (this.combo >= 25 ? 4 : (this.combo >= 12 ? 3 : 2)) + "!", 200.0F, 90.0F, -33835, 15.0F);
               this.fx.shake(0.25F);
               Sound.play("minecraft:entity.player.levelup", 1.3F, 0.3F);
            }

            Sound.play("minecraft:entity.item.pickup", 1.0F + Math.min(1.0F, this.combo * 0.03F), 0.35F);
      }
   }

   private void miss(GiftCatch.Item it) {
      this.lives--;
      this.combo = 0;
      this.hurtT = 0.5F;
      this.fx.confetti(it.x, 254.0F, 12, new int[]{-7696487, -1445121}, 90.0F, 3.0F);
      this.fx.burst(it.x, 258.0F, 10, -1445121, 70.0F, 3.0F, 0.5F, 60.0F, 4);
      this.fx.popup("Verpasst!", it.x, 238.0F, -20304, 10.0F);
      this.fx.shake(0.35F);
      Sound.play("minecraft:block.snow.break", 0.7F, 0.6F);
      if (this.lives <= 0) {
         this.gameOver();
      }
   }

   @Override
   protected void render() {
      float dt = Math.min(0.05F, Gx.dt);
      this.anim += dt;
      if (this.state == GameView.State.READY) {
         this.sx = 200.0F + (float)Math.sin(this.anim * 1.3F) * 80.0F;
      }

      GameFx.sprite(this, "gc_bg", 0.0F, 0.0F, 400.0F, 280.0F, 380.0F, 280.0F, GiftCatch::paintBackground, -1);

      for (int i = 0; i < 40; i++) {
         float fx0 = (i * 97.3F + this.anim * (6 + i % 5 * 3)) % 400.0F;
         float fy = (i * 53.7F + this.anim * (14 + i % 7 * 4)) % 280.0F;
         this.circle(fx0 + (float)Math.sin(this.anim + i) * 4.0F, fy, 0.8F + i % 3 * 0.5F, -1996488705);
      }

      if (this.slowT > 0.0F) {
         this.box(0.0F, 0.0F, 400.0F, 280.0F, 0.0F, Gx.withAlpha(-8587265, 0.08F + 0.04F * (float)Math.sin(this.anim * 6.0F)));
      }

      Gx.push();
      Gx.translate(this.S(this.fx.sx), this.S(this.fx.sy));
      this.lights();

      for (GiftCatch.Item it : this.items) {
         this.item(it);
      }

      this.sleigh();
      this.fx.draw(this, 0.0F, 0.0F);
      Gx.pop();

      for (int i = 0; i < 3; i++) {
         boolean on = i < this.lives;
         float pulse = on && this.lives == 1 ? 1.0F + 0.12F * (float)Math.sin(this.anim * 10.0F) : 1.0F;
         Gx.icon(on ? "heart-fill" : "heart", this.X(16 + i * 15), this.Y(34.0F), 11.0F * this.u * pulse, on ? -45715 : 1442840575);
      }

      int m = this.mult();
      if (this.combo > 0 || m > 1) {
         float w = 70.0F;
         this.box(400.0F - w - 8.0F, 25.0F, w, 18.0F, 9.0F, m > 1 ? Gx.withAlpha(-1890757, 0.9F) : 1711276032);
         this.text((m > 1 ? "x" + m + "  " : "") + this.combo + " Combo", 400.0F - w / 2.0F - 8.0F, 34.0F, 7.0F, 3, -1);
      }

      float py = 50.0F;
      if (this.doubleT > 0.0F) {
         this.power(392.0F, py, "star-fill", -736942, this.doubleT / 7.0F);
         py += 16.0F;
      }

      if (this.slowT > 0.0F) {
         this.power(392.0F, py, "clock", -8587265, this.slowT / 6.0F);
      }

      if (this.state == GameView.State.PLAYING && this.hintT < 3.5F) {
         float a = Math.min(1.0F, 3.5F - this.hintT) * (0.6F + 0.4F * (float)Math.sin(this.anim * 5.0F));
         this.box(90.0F, 120.0F, 220.0F, 22.0F, 11.0F, Gx.withAlpha(-16777216, 0.45F * a));
         this.text("Maus oder ← → – fang die Geschenke, keine Kohle!", 200.0F, 131.0F, 7.2F, 3, Gx.withAlpha(-1, a));
      }

      this.fx.drawPopups(this, 0.0F, 0.0F);
      if (this.hurtT > 0.0F) {
         this.box(0.0F, 0.0F, 400.0F, 280.0F, 0.0F, Gx.withAlpha(-57280, this.hurtT * 0.25F));
      }

      this.fx.drawFlash(this, 400.0F, 280.0F);
   }

   private void power(float right, float y, String icon, int col, float frac) {
      float w = 52.0F;
      this.box(right - w, y - 7.0F, w, 14.0F, 7.0F, 1711276032);
      Gx.icon(icon, this.X(right - w + 8.0F), this.Y(y), 8.0F * this.u, col);
      this.box(right - w + 15.0F, y - 1.5F, w - 21.0F, 3.0F, 1.5F, 872415231);
      this.box(right - w + 15.0F, y - 1.5F, (w - 21.0F) * frac, 3.0F, 1.5F, col);
   }

   private void lights() {
      for (int seg = 0; seg < 3; seg++) {
         float x0 = seg * 400.0F / 3.0F;
         float x1 = (seg + 1) * 400.0F / 3.0F;

         for (int i = 0; i <= 24; i++) {
            float t = i / 24.0F;
            float lx = Ease.lerp(x0, x1, t);
            float ly = 6.0F + (float)Math.sin(t * Math.PI) * 12.0F;
            this.circle(lx, ly, 0.7F, -15062498);
         }

         for (int i = 1; i < 6; i++) {
            float t = i / 6.0F;
            float lx = Ease.lerp(x0, x1, t);
            float ly = 7.0F + (float)Math.sin(t * Math.PI) * 12.0F;
            int c = BULBS[(seg * 5 + i) % BULBS.length];
            float tw = 0.55F + 0.45F * (float)Math.sin(this.anim * 3.0F + seg * 2 + i * 1.7F);
            Gx.glow(this.X(lx), this.Y(ly + 3.0F), this.S(9.0F), Gx.withAlpha(c, 0.45F * tw));
            this.box(lx - 1.3F, ly - 1.0F, 2.6F, 2.2F, 0.5F, -14009810);
            this.boxV(lx - 2.0F, ly + 0.8F, 4.0F, 5.5F, 2.0F, Gx.mix(c, -1, 0.3F + 0.3F * tw), Gx.mix(c, -16777216, 0.3F * (1.0F - tw)));
         }
      }
   }

   private void item(GiftCatch.Item it) {
      float s = it.size;
      float wob = 1.0F + 0.05F * (float)Math.sin(it.t * 7.0F);
      float x = it.x;
      float y = it.y;
      switch (it.type) {
         case 2:
            Gx.glow(this.X(x), this.Y(y), this.S(s), 872372766);
            GameFx.sprite(this, "gc_coal", x - s / 2.0F, y - s / 2.0F, s, s, 40.0F, 40.0F, GiftCatch::paintCoal, -1);
            break;
         case 3:
            Gx.glow(this.X(x), this.Y(y), this.S(s * 1.4F), 1727316306);
            Gx.icon("star-fill", this.X(x), this.Y(y), s * this.u * wob, -736942);
            break;
         case 4:
            Gx.glow(this.X(x), this.Y(y), this.S(s * 1.4F), 1434253311);
            this.circle(x, y, s / 2.0F, -14929072);
            Gx.icon("clock", this.X(x), this.Y(y), s * 0.8F * this.u, -8587265);
            break;
         case 5:
            Gx.glow(this.X(x), this.Y(y), this.S(s * 1.4F), 1728007533);
            Gx.icon("heart-fill", this.X(x), this.Y(y), s * this.u * wob, -45715);
            break;
         default:
            int c = it.type == 1 ? 5 : it.color;
            if (it.type == 1) {
               Gx.glow(this.X(x), this.Y(y), this.S(s * 1.5F), 1727316306);
            }

            float w = s * wob;
            float h = s * (2.0F - wob);
            GameFx.spriteAt(this, "sj_gift" + c, s, s, 40.0F, 40.0F, gr -> SantaJump.paintGift(gr, c), x - w / 2.0F, y - h / 2.0F, w, h, -1);
      }
   }

   private void sleigh() {
      float sq = Ease.outCubic(this.squash);
      float w = 66.0F * (1.0F + 0.1F * sq);
      float h = 30.0F * (1.0F - 0.12F * sq);
      float x = this.sx - w / 2.0F;
      float y = 258.0F - h;
      this.box(this.sx - 29.699999F, 256.0F, 59.399998F, 4.0F, 2.0F, 855638016);
      if (this.doubleT > 0.0F) {
         Gx.glow(this.X(this.sx), this.Y(236.0F), this.S(50.0F), Gx.withAlpha(-736942, 0.25F));
      }

      for (int i = 0; i < this.pile.size(); i++) {
         int c = this.pile.get(i);
         float gx = this.sx - 20.0F + i % 3 * 14 - this.lean * 3.0F * (i / 3 + 1);
         float gy = 234.0F - i / 3 * 9 + sq * 3.0F;
         GameFx.sprite(this, "sj_gift" + c + "s", gx - 7.0F, gy - 7.0F, 14.0F, 14.0F, 40.0F, 40.0F, gr -> SantaJump.paintGift(gr, c), -1);
      }

      GameFx.spriteAt(this, "gc_sleigh", 66.0F, 30.0F, 66.0F, 30.0F, GiftCatch::paintSleigh, x, y, w, h, -1);
   }

   private static void paintBackground(Graphics2D g) {
      g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(660528), 0.0F, 260.0F, new Color(2772614)));
      g.fillRect(0, 0, 380, 280);
      Random r = new Random(12L);

      for (int i = 0; i < 70; i++) {
         float x = r.nextFloat() * 380.0F;
         float y = r.nextFloat() * 170.0F;
         float s = 0.6F + r.nextFloat() * 1.4F;
         g.setColor(new Color(255, 255, 255, 60 + r.nextInt(150)));
         g.fill(new Float(x, y, s, s));
      }

      g.setPaint(
         new RadialGradientPaint(
            new java.awt.geom.Point2D.Float(300.0F, 70.0F),
            60.0F,
            new float[]{0.0F, 1.0F},
            new Color[]{new Color(255, 244, 214, 90), new Color(255, 244, 214, 0)}
         )
      );
      g.fill(new Float(240.0F, 10.0F, 120.0F, 120.0F));
      g.setColor(new Color(16774358));
      g.fill(new Float(286.0F, 56.0F, 28.0F, 28.0F));
      g.setColor(new Color(0, 0, 0, 25));
      g.fill(new Float(300.0F, 62.0F, 6.0F, 6.0F));
      g.setColor(new Color(1783137));
      g.fill(new Float(-80.0F, 170.0F, 300.0F, 160.0F));
      g.fill(new Float(160.0F, 160.0F, 320.0F, 170.0F));
      g.setColor(new Color(2311541));
      g.fill(new Float(60.0F, 195.0F, 320.0F, 140.0F));

      for (int i = 0; i < 14; i++) {
         float tx = 6 + i * 28 + r.nextFloat() * 8.0F;
         float ty = 224.0F + r.nextFloat() * 8.0F;
         float s = 0.6F + r.nextFloat() * 0.6F;
         g.setColor(new Color(866867));

         for (int k = 0; k < 3; k++) {
            float ky = ty - k * 9 * s;
            float kw = (14.0F - k * 3.5F) * s;
            g.fillPolygon(new int[]{(int)tx, (int)(tx - kw), (int)(tx + kw)}, new int[]{(int)(ky - 14.0F * s), (int)ky, (int)ky}, 3);
         }

         g.setColor(new Color(230, 240, 255, 170));
         g.fillPolygon(
            new int[]{(int)tx, (int)(tx - 4.0F * s), (int)(tx + 4.0F * s)}, new int[]{(int)(ty - 32.0F * s), (int)(ty - 25.0F * s), (int)(ty - 25.0F * s)}, 3
         );
      }

      float[] hx = new float[]{30.0F, 150.0F, 270.0F};

      for (float x : hx) {
         float w = 46.0F;
         float h = 30.0F;
         float y = 244.0F - h;
         g.setColor(new Color(3811898));
         g.fill(new java.awt.geom.Rectangle2D.Float(x, y, w, h));
         GeneralPath roof = new GeneralPath();
         roof.moveTo(x - 5.0F, y + 1.0F);
         roof.lineTo(x + w / 2.0F, y - 18.0F);
         roof.lineTo(x + w + 5.0F, y + 1.0F);
         roof.closePath();
         g.setColor(new Color(4861494));
         g.fill(roof);
         g.setColor(new Color(16054527));
         g.setStroke(new BasicStroke(4.0F, 1, 1));
         g.drawLine((int)(x - 4.0F), (int)y, (int)(x + w / 2.0F), (int)(y - 17.0F));
         g.drawLine((int)(x + w / 2.0F), (int)(y - 17.0F), (int)(x + w + 4.0F), (int)y);

         for (int k = 0; k < 2; k++) {
            float wx = x + 8.0F + k * 22;
            float wy = y + 9.0F;
            g.setPaint(
               new RadialGradientPaint(
                  new java.awt.geom.Point2D.Float(wx + 4.0F, wy + 4.0F),
                  14.0F,
                  new float[]{0.0F, 1.0F},
                  new Color[]{new Color(255, 190, 90, 130), new Color(255, 190, 90, 0)}
               )
            );
            g.fill(new Float(wx - 10.0F, wy - 10.0F, 28.0F, 28.0F));
            g.setColor(new Color(16764782));
            g.fill(new java.awt.geom.Rectangle2D.Float(wx, wy, 8.0F, 9.0F));
            g.setColor(new Color(3811898));
            g.fill(new java.awt.geom.Rectangle2D.Float(wx + 3.5F, wy, 1.0F, 9.0F));
         }
      }

      GeneralPath snow = new GeneralPath();
      snow.moveTo(0.0F, 280.0F);
      snow.lineTo(0.0F, 254.0F);

      for (int x = 0; x <= 380; x += 10) {
         snow.lineTo(x, 252.0 + Math.sin(x / 37.0) * 3.0);
      }

      snow.lineTo(380.0F, 280.0F);
      snow.closePath();
      g.setPaint(new GradientPaint(0.0F, 250.0F, new Color(16054527), 0.0F, 280.0F, new Color(11914219)));
      g.fill(snow);
      g.setColor(new Color(255, 255, 255, 200));

      for (int i = 0; i < 60; i++) {
         g.fill(new Float(r.nextFloat() * 380.0F, 258.0F + r.nextFloat() * 20.0F, 1.5F, 1.5F));
      }
   }

   private static void paintSleigh(Graphics2D g) {
      g.setColor(new Color(16040274));
      g.setStroke(new BasicStroke(2.4F, 1, 1));
      GeneralPath run = new GeneralPath();
      run.moveTo(4.0F, 27.0F);
      run.lineTo(56.0F, 27.0F);
      run.curveTo(64.0F, 27.0F, 66.0F, 20.0F, 60.0F, 17.0F);
      g.draw(run);
      g.drawLine(14, 27, 14, 22);
      g.drawLine(44, 27, 44, 22);
      GeneralPath b = new GeneralPath();
      b.moveTo(2.0F, 6.0F);
      b.curveTo(0.0F, 16.0F, 6.0F, 23.0F, 16.0F, 23.0F);
      b.lineTo(52.0F, 23.0F);
      b.curveTo(60.0F, 23.0F, 64.0F, 16.0F, 62.0F, 8.0F);
      b.lineTo(56.0F, 10.0F);
      b.lineTo(10.0F, 10.0F);
      b.closePath();
      g.setPaint(new GradientPaint(0.0F, 6.0F, new Color(15742538), 0.0F, 23.0F, new Color(9309728)));
      g.fill(b);
      g.setColor(new Color(6162452));
      g.setStroke(new BasicStroke(1.0F));
      g.draw(b);
      g.setColor(new Color(16040274));
      g.setStroke(new BasicStroke(1.6F, 1, 1));
      g.drawLine(10, 11, 56, 11);
      g.drawArc(22, 13, 8, 7, 0, 300);
      g.drawArc(36, 13, 8, 7, 240, 300);
      g.setColor(new Color(255, 255, 255, 90));
      g.fill(new java.awt.geom.RoundRectangle2D.Float(12.0F, 13.0F, 6.0F, 2.0F, 2.0F, 2.0F));
      g.setColor(new Color(12917294));
      g.fill(new Float(57.0F, 3.0F, 8.0F, 8.0F));
      g.setColor(new Color(16040274));
      g.fill(new Float(59.5F, 5.5F, 3.0F, 3.0F));
      g.setColor(new Color(16054527));
      g.fill(new java.awt.geom.RoundRectangle2D.Float(1.0F, 4.0F, 12.0F, 4.0F, 4.0F, 4.0F));
   }

   private static void paintCoal(Graphics2D g) {
      GeneralPath p = new GeneralPath();
      p.moveTo(8.0F, 14.0F);
      p.lineTo(18.0F, 5.0F);
      p.lineTo(30.0F, 8.0F);
      p.lineTo(36.0F, 20.0F);
      p.lineTo(31.0F, 33.0F);
      p.lineTo(17.0F, 36.0F);
      p.lineTo(6.0F, 28.0F);
      p.closePath();
      g.setPaint(new GradientPaint(0.0F, 5.0F, new Color(4868693), 0.0F, 36.0F, new Color(1184278)));
      g.fill(p);
      g.setColor(new Color(6974072));
      g.fill(new Polygon(new int[]{18, 30, 22}, new int[]{5, 8, 14}, 3));
      g.setColor(new Color(255, 110, 40, 200));
      g.fill(new Float(14.0F, 24.0F, 4.0F, 3.0F));
      g.fill(new Float(26.0F, 18.0F, 3.0F, 3.0F));
      g.setColor(new Color(16738874));
      g.setStroke(new BasicStroke(2.0F, 1, 1));
      g.drawLine(13, 15, 18, 17);
      g.drawLine(29, 15, 24, 17);
      g.fill(new Float(14.0F, 17.0F, 3.0F, 3.0F));
      g.fill(new Float(24.0F, 17.0F, 3.0F, 3.0F));
      g.drawArc(16, 25, 10, 5, 0, 180);
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
