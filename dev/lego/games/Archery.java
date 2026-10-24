package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.util.ArrayList;
import java.util.List;

final class Archery extends GameView {
   private static final float W = 400.0F;
   private static final float H = 260.0F;
   private static final float TX = 340.0F;
   private static final float TY = 130.0F;
   private float aimY = 130.0F;
   private float draw;
   private float ax;
   private float ay;
   private float avx;
   private float avy;
   private float wind;
   private float msgT;
   private boolean drawing;
   private boolean flying;
   private int arrows;
   private String msg = "";
   private final List<float[]> hits = new ArrayList<>();

   Archery() {
      super("archery");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 260.0F;
   }

   @Override
   protected String overTitle() {
      return "Köcher leer";
   }

   @Override
   protected void reset() {
      this.arrows = 10;
      this.flying = false;
      this.hits.clear();
      this.wind = (this.rnd.nextFloat() - 0.5F) * 60.0F;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (!this.flying) {
         this.drawing = true;
         this.draw = 0.0F;
      }
   }

   @Override
   protected void onRelease(float x, float y, int b) {
      if (this.drawing) {
         this.drawing = false;
         float p = 260.0F + this.draw * 330.0F;
         float ang = (float)Math.atan2(this.aimY - 150.0F, 300.0);
         this.ax = 50.0F;
         this.ay = 150.0F;
         this.avx = (float)Math.cos(ang) * p;
         this.avy = (float)Math.sin(ang) * p;
         this.flying = true;
         this.arrows--;
         Sound.play("minecraft:entity.arrow.shoot", 1.0F, 0.4F);
      }
   }

   @Override
   protected void update(float dt) {
      this.msgT = Math.max(0.0F, this.msgT - dt);
      this.aimY = Math.max(20.0F, Math.min(240.0F, this.bmy()));
      if (this.drawing) {
         this.draw = Math.min(1.0F, this.draw + dt * 0.9F);
      }

      if (!this.flying) {
         if (this.arrows <= 0) {
            this.gameOver();
         }
      } else {
         this.avy += 220.0F * dt;
         this.avx += 0.0F;
         this.ay = this.ay + this.wind * dt * 0.4F;
         this.ax = this.ax + this.avx * dt;
         this.ay = this.ay + this.avy * dt;
         if (this.ax >= 340.0F) {
            float d = Math.abs(this.ay - 130.0F);
            int pts = d < 6.0F ? 10 : (d < 14.0F ? 8 : (d < 24.0F ? 6 : (d < 34.0F ? 4 : (d < 45.0F ? 2 : 0))));
            this.score += pts;
            this.msg = pts == 10 ? "Volltreffer! +10" : (pts > 0 ? "+" + pts : "Daneben");
            this.msgT = 1.2F;
            if (pts > 0) {
               this.hits.add(new float[]{340.0F, this.ay});
            }

            Sound.play(pts > 0 ? "minecraft:entity.arrow.hit" : "minecraft:block.note_block.bass", 1.2F, 0.4F);
            this.flying = false;
            this.wind = (this.rnd.nextFloat() - 0.5F) * 60.0F;
         } else if (this.ay > 260.0F || this.ay < -40.0F) {
            this.flying = false;
            this.msg = "Daneben";
            this.msgT = 1.0F;
            this.wind = (this.rnd.nextFloat() - 0.5F) * 60.0F;
         }
      }
   }

   @Override
   protected void render() {
      this.boxV(0.0F, 0.0F, 400.0F, 260.0F, 0.0F, -7681793, -3149825);
      this.box(0.0F, 200.0F, 400.0F, 60.0F, 0.0F, -10634418);
      this.box(338.0F, 180.0F, 4.0F, 70.0F, 0.0F, -9811414);
      int[] cols = new int[]{-1, -15000798, -12604929, -1900533, -13053};
      float[] rs = new float[]{45.0F, 34.0F, 24.0F, 14.0F, 6.0F};

      for (int i = 0; i < 5; i++) {
         this.box(336.0F, 130.0F - rs[i], 8.0F, rs[i] * 2.0F, 3.0F, cols[i]);
      }

      for (float[] h : this.hits) {
         this.line(h[0] - 14.0F, h[1], h[0], h[1], 1.2F, -9811414);
      }

      this.line(50.0F, 110.0F, 44.0F, 150.0F, 2.0F, -7710160);
      this.line(44.0F, 150.0F, 50.0F, 190.0F, 2.0F, -7710160);
      float pull = this.drawing ? this.draw * 16.0F : 0.0F;
      this.line(50.0F, 110.0F, 50.0F - pull, 150.0F, 0.7F, -723724);
      this.line(50.0F - pull, 150.0F, 50.0F, 190.0F, 0.7F, -723724);
      if (!this.flying) {
         float ang = (float)Math.atan2(this.aimY - 150.0F, 300.0);
         this.line(50.0F - pull, 150.0F, 50.0F - pull + (float)Math.cos(ang) * 34.0F, 150.0F + (float)Math.sin(ang) * 34.0F, 1.3F, -9811414);
      } else {
         float n = (float)Math.hypot(this.avx, this.avy);
         this.line(this.ax - this.avx / n * 16.0F, this.ay - this.avy / n * 16.0F, this.ax, this.ay, 1.3F, -9811414);
      }

      if (this.drawing) {
         this.box(20.0F, 230.0F, 80.0F, 8.0F, 4.0F, 1426063360);
         this.box(20.0F, 230.0F, 80.0F * this.draw, 8.0F, 4.0F, Gx.mix(-12987565, -50356, this.draw));
      }

      this.textL("Pfeile: " + this.arrows, 8.0F, 12.0F, 8.0F, 3, -15000798);
      this.textL("Wind: " + (this.wind > 0.0F ? "↓ " : "↑ ") + Math.round(Math.abs(this.wind)), 8.0F, 26.0F, 7.0F, 2, -15000798);
      if (this.msgT > 0.0F) {
         this.text(this.msg, 200.0F, 40.0F, 13.0F, 3, Gx.withAlpha(-15000798, Math.min(1.0F, this.msgT)));
      }
   }
}
