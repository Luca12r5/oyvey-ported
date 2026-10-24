package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.util.HashSet;
import java.util.Set;

public final class TableTennis extends GameView {
   private static final float W = 420.0F;
   private static final float H = 280.0F;
   private static final float NEAR_Y = 250.0F;
   private static final float FAR_Y = 95.0F;
   private static final float NEAR_HW = 170.0F;
   private static final float FAR_HW = 92.0F;
   private static final float CX = 210.0F;
   private final Set<Integer> keys = new HashSet<>();
   private float bx;
   private float bz;
   private float bh;
   private float vx;
   private float vz;
   private float vh;
   private float px;
   private float ax;
   private float aiErr;
   private int me;
   private int ai;
   private int rally;
   private boolean bounced;
   private boolean serveYou;
   private float wait;
   private float flash;
   private String msg = "";

   public TableTennis() {
      super("tabletennis");
   }

   @Override
   protected boolean hasAi() {
      return true;
   }

   @Override
   protected float boardW() {
      return 420.0F;
   }

   @Override
   protected float boardH() {
      return 280.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Punkte";
   }

   @Override
   protected String scoreText() {
      return this.me + " : " + this.ai;
   }

   @Override
   protected String overTitle() {
      return this.me > this.ai ? "Gewonnen!" : "Verloren";
   }

   @Override
   protected void reset() {
      this.me = 0;
      this.ai = 0;
      this.px = 0.0F;
      this.ax = 0.0F;
      this.serveYou = true;
      this.keys.clear();
      this.serve();
   }

   private void serve() {
      this.rally = 0;
      this.bounced = false;
      this.wait = 0.8F;
      if (this.serveYou) {
         this.bx = this.px;
         this.bz = 0.03F;
         this.vz = 0.62F;
         this.vx = (this.rnd.nextFloat() - 0.5F) * 0.4F;
      } else {
         this.bx = this.ax;
         this.bz = 0.97F;
         this.vz = -0.62F;
         this.vx = (this.rnd.nextFloat() - 0.5F) * 0.4F;
      }

      this.bh = 0.12F;
      this.vh = 0.95F;
      this.aiErr = (this.rnd.nextFloat() - 0.5F) * 0.3F;
   }

   @Override
   protected void onKey(int k) {
      this.keys.add(k);
   }

   @Override
   protected void onKeyUp(int k) {
      this.keys.remove(k);
   }

   private void point(boolean you, String why) {
      if (you) {
         this.me++;
      } else {
         this.ai++;
      }

      this.msg = (you ? "Punkt für dich! " : "Punkt für die KI – ") + why;
      this.flash = 1.4F;
      this.score = this.me * 10L + (this.me > this.ai ? 5 : 0);
      Sound.play(you ? "minecraft:entity.experience_orb.pickup" : "minecraft:block.note_block.bass", 1.2F, 0.3F);
      if ((this.me >= 11 || this.ai >= 11) && Math.abs(this.me - this.ai) >= 2) {
         this.score = this.me * 10L + (this.me > this.ai ? 100 : 0);
         this.gameOver();
      } else {
         this.serveYou = (this.me + this.ai) / 2 % 2 == 0;
         this.serve();
      }
   }

   @Override
   protected void update(float dt) {
      this.flash = Math.max(0.0F, this.flash - dt);
      float target = this.px;
      if (this.mx >= 0.0F) {
         target = Math.max(-1.2F, Math.min(1.2F, ((this.mx - this.bx0()) / this.u - 210.0F) / 170.0F));
      }

      if (this.keys.contains(65) || this.keys.contains(263)) {
         target = this.px - 2.2F * dt;
      }

      if (this.keys.contains(68) || this.keys.contains(262)) {
         target = this.px + 2.2F * dt;
      }

      this.px = this.px + (target - this.px) * Math.min(1.0F, dt * 18.0F);
      float want = this.vz > 0.0F ? this.bx + this.vx * (1.0F - this.bz) / Math.max(0.2F, this.vz) * 0.6F + this.aiErr : 0.0F;
      float aiSpeed = (1.15F + Math.min(0.9F, this.rally * 0.05F)) * new float[]{0.7F, 0.9F, 1.05F, 1.3F}[this.aiLevel()];
      this.ax = this.ax + Math.max(-aiSpeed * dt, Math.min(aiSpeed * dt, want - this.ax));
      if (this.wait > 0.0F) {
         this.wait -= dt;
      } else {
         this.bx = this.bx + this.vx * dt;
         this.bz = this.bz + this.vz * dt;
         this.bh = this.bh + this.vh * dt;
         this.vh -= 2.4F * dt;
         if (this.bh <= 0.0F) {
            this.bh = 0.0F;
            boolean onTable = Math.abs(this.bx) <= 1.0F && this.bz >= 0.0F && this.bz <= 1.0F;
            if (!onTable) {
               this.point(this.vz < 0.0F, "Ball im Aus");
               return;
            }

            boolean ownSide = this.vz > 0.0F ? this.bz < 0.5F : this.bz > 0.5F;
            if (ownSide) {
               this.point(this.vz < 0.0F, "auf der eigenen Seite");
               return;
            }

            if (this.bounced) {
               this.point(this.vz > 0.0F, "zweimal aufgekommen");
               return;
            }

            this.bounced = true;
            this.vh = Math.abs(this.vh) * 0.72F + 0.12F;
            Sound.play("minecraft:block.wood.hit", 1.8F, 0.25F);
         }

         if (Math.abs(this.bz - 0.5F) < 0.012F && this.bh < 0.05F) {
            this.point(this.vz < 0.0F, "ins Netz");
         } else {
            if (this.vz < 0.0F && this.bz <= 0.02F) {
               if (!this.bounced || !(Math.abs(this.bx - this.px) < 0.32F)) {
                  this.point(false, this.bounced ? "verfehlt" : "nicht aufgekommen");
                  return;
               }

               this.hit(true);
            }

            if (this.vz > 0.0F && this.bz >= 0.98F) {
               if (!this.bounced || !(Math.abs(this.bx - this.ax) < 0.3F) || !(this.rnd.nextFloat() > 0.04F + this.rally * 0.006F)) {
                  this.point(true, "die KI hat verfehlt");
                  return;
               }

               this.hit(false);
            }
         }
      }
   }

   private float bx0() {
      return super.bx;
   }

   private void hit(boolean you) {
      this.rally++;
      this.bounced = false;
      float off = you ? this.bx - this.px : this.bx - this.ax;
      float sp = 0.62F + Math.min(0.45F, this.rally * 0.03F);
      this.vz = you ? sp : -sp;
      this.vx = off * 1.6F + (this.rnd.nextFloat() - 0.5F) * 0.25F - (you ? 0.0F : this.ax * 0.2F);
      this.bh = Math.max(this.bh, 0.1F);
      float flight = (0.62F + (this.rnd.nextFloat() - 0.5F) * 0.12F) / sp;
      this.vh = (1.2F * flight * flight - this.bh) / flight;
      this.aiErr = (this.rnd.nextFloat() - 0.5F) * (0.25F + Math.min(0.3F, this.rally * 0.02F)) * new float[]{1.7F, 1.2F, 0.85F, 0.5F}[this.aiLevel()];
      Sound.play("minecraft:block.bamboo_wood.hit", you ? 1.5F : 1.3F, 0.35F);
   }

   private float ty(float z) {
      return 250.0F + -155.0F * z;
   }

   private float hw(float z) {
      return 170.0F + -78.0F * z;
   }

   @Override
   protected void render() {
      this.boxV(0.0F, 0.0F, 420.0F, 280.0F, 10.0F, -15064522, -15920611);

      for (int i = 0; i < 40; i++) {
         float z0 = i / 40.0F;
         float z1 = (i + 1) / 40.0F;
         float y0 = this.ty(z0);
         float y1 = this.ty(z1);
         float w = this.hw(z0);
         this.box(210.0F - w, y1, w * 2.0F, y0 - y1 + 0.6F, 0.0F, i % 2 == 0 ? -14717000 : -14915667);
         this.box(210.0F - w, y1, 2.0F, y0 - y1 + 0.6F, 0.0F, -723724);
         this.box(210.0F + w - 2.0F, y1, 2.0F, y0 - y1 + 0.6F, 0.0F, -723724);
      }

      this.box(209.0F, 95.0F, 2.0F, 155.0F, 0.0F, -1426787084);
      this.box(40.0F, 249.0F, 340.0F, 3.0F, 0.0F, -723724);
      this.box(118.0F, 94.0F, 184.0F, 2.0F, 0.0F, -723724);
      this.box(40.0F, 252.0F, 340.0F, 8.0F, 2.0F, -15581589);
      float ny = this.ty(0.5F);
      float nw = this.hw(0.5F);
      this.box(210.0F - nw - 6.0F, ny - 14.0F, nw * 2.0F + 12.0F, 14.0F, 1.0F, 1728053247);
      this.box(210.0F - nw - 6.0F, ny - 15.0F, nw * 2.0F + 12.0F, 2.0F, 1.0F, -723724);
      this.drawBat(210.0F + this.ax * this.hw(1.0F), 85.0F, 0.6F, -14803422);
      float sy = this.ty(this.bz);
      float sx = 210.0F + this.bx * this.hw(this.bz);
      float sc = 0.6F + 0.4F * (1.0F - this.bz);
      this.circle(sx, sy, 4.0F * sc, 1426063360);
      this.circle(sx, sy - this.bh * 120.0F * sc, 4.2F * sc, -2848);
      this.circle(sx - 1.0F, sy - this.bh * 120.0F * sc - 1.0F, 1.6F * sc, -1);
      this.drawBat(210.0F + this.px * this.hw(0.0F), 262.0F, 1.0F, -1900533);
      this.text(this.me + " : " + this.ai, 210.0F, 18.0F, 13.0F, 3, -1);
      this.text("Rally " + this.rally, 210.0F, 34.0F, 6.5F, 2, -1711276033);
      if (this.flash > 0.0F) {
         this.text(this.msg, 210.0F, 110.0F, 8.0F, 3, Gx.withAlpha(-13053, Math.min(1.0F, this.flash)));
      }

      if (this.wait > 0.0F && this.state == GameView.State.PLAYING) {
         this.text(this.serveYou ? "Dein Aufschlag" : "Aufschlag KI", 210.0F, 210.0F, 7.0F, 3, -855638017);
      }
   }

   private void drawBat(float x, float y, float s, int col) {
      this.box(x - 2.5F * s, y + 6.0F * s, 5.0F * s, 12.0F * s, 2.0F * s, -7710158);
      this.circle(x, y, 13.0F * s, Gx.mix(col, -16777216, 0.25F));
      this.circle(x, y - 1.0F * s, 12.0F * s, col);
   }
}
