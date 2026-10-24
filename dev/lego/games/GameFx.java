package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.util.Random;
import java.util.function.Consumer;

final class GameFx {
   static final int DOT = 0;
   static final int SQUARE = 1;
   static final int SPARK = 2;
   static final int RING = 3;
   static final int FLAKE = 4;
   static final int STREAK = 5;
   private static final int MAX = 700;
   private static final int MAX_POP = 24;
   private final Random rnd = new Random();
   private final float[] px = new float[700];
   private final float[] py = new float[700];
   private final float[] vx = new float[700];
   private final float[] vy = new float[700];
   private final float[] life = new float[700];
   private final float[] max = new float[700];
   private final float[] size = new float[700];
   private final float[] grav = new float[700];
   private final float[] drag = new float[700];
   private final int[] col = new int[700];
   private final int[] kind = new int[700];
   private int n = 0;
   private final String[] popText = new String[24];
   private final float[] popX = new float[24];
   private final float[] popY = new float[24];
   private final float[] popT = new float[24];
   private final float[] popS = new float[24];
   private final int[] popC = new int[24];
   private int pn = 0;
   private float trauma = 0.0F;
   private float shakeT = 0.0F;
   private float flash = 0.0F;
   private int flashCol = -1;
   float sx;
   float sy;

   void clear() {
      this.n = 0;
      this.pn = 0;
      this.trauma = 0.0F;
      this.flash = 0.0F;
      this.sx = this.sy = 0.0F;
   }

   void add(float x, float y, float vx0, float vy0, float lifeS, float sz, int color, int k, float gravity, float dragK) {
      int i;
      if (this.n < 700) {
         i = this.n++;
      } else {
         i = this.rnd.nextInt(700);
      }

      this.px[i] = x;
      this.py[i] = y;
      this.vx[i] = vx0;
      this.vy[i] = vy0;
      this.life[i] = 0.0F;
      this.max[i] = lifeS;
      this.size[i] = sz;
      this.col[i] = color;
      this.kind[i] = k;
      this.grav[i] = gravity;
      this.drag[i] = dragK;
   }

   void burst(float x, float y, int count, int color, float speed, float sz, float lifeS, float gravity, int k) {
      for (int i = 0; i < count; i++) {
         double a = this.rnd.nextDouble() * Math.PI * 2.0;
         float s = speed * (0.35F + this.rnd.nextFloat() * 0.65F);
         this.add(
            x,
            y,
            (float)Math.cos(a) * s,
            (float)Math.sin(a) * s,
            lifeS * (0.6F + this.rnd.nextFloat() * 0.4F),
            sz * (0.6F + this.rnd.nextFloat() * 0.6F),
            color,
            k,
            gravity,
            2.2F
         );
      }
   }

   void confetti(float x, float y, int count, int[] palette, float speed, float sz) {
      for (int i = 0; i < count; i++) {
         double a = (-Math.PI / 2) + (this.rnd.nextDouble() - 0.5) * Math.PI * 1.4;
         float s = speed * (0.4F + this.rnd.nextFloat() * 0.6F);
         this.add(
            x,
            y,
            (float)Math.cos(a) * s,
            (float)Math.sin(a) * s,
            0.9F + this.rnd.nextFloat() * 0.5F,
            sz * (0.6F + this.rnd.nextFloat() * 0.7F),
            palette[this.rnd.nextInt(palette.length)],
            1,
            260.0F,
            1.6F
         );
      }
   }

   void ring(float x, float y, float radius, int color, float lifeS) {
      this.add(x, y, 0.0F, 0.0F, lifeS, radius, color, 3, 0.0F, 0.0F);
   }

   void popup(String text, float x, float y, int color, float sizeUnits) {
      int i = this.pn < 24 ? this.pn++ : 0;
      this.popText[i] = text;
      this.popX[i] = x;
      this.popY[i] = y;
      this.popT[i] = 0.0F;
      this.popC[i] = color;
      this.popS[i] = sizeUnits;
   }

   void shake(float amount) {
      this.trauma = Math.min(1.0F, this.trauma + amount);
   }

   void flash(int color, float amount) {
      this.flashCol = color;
      this.flash = Math.max(this.flash, amount);
   }

   float trauma() {
      return this.trauma;
   }

   void update(float dt) {
      for (int i = 0; i < this.n; i++) {
         this.life[i] = this.life[i] + dt;
         if (this.life[i] >= this.max[i]) {
            this.n--;
            this.copy(this.n, i);
            i--;
         } else {
            float d = Math.max(0.0F, 1.0F - this.drag[i] * dt);
            this.vx[i] = this.vx[i] * d;
            this.vy[i] = this.vy[i] * d;
            this.vy[i] = this.vy[i] + this.grav[i] * dt;
            this.px[i] = this.px[i] + this.vx[i] * dt;
            this.py[i] = this.py[i] + this.vy[i] * dt;
         }
      }

      for (int ix = 0; ix < this.pn; ix++) {
         this.popT[ix] = this.popT[ix] + dt;
         if (this.popT[ix] > 1.1F) {
            this.pn--;
            this.popText[ix] = this.popText[this.pn];
            this.popX[ix] = this.popX[this.pn];
            this.popY[ix] = this.popY[this.pn];
            this.popT[ix] = this.popT[this.pn];
            this.popC[ix] = this.popC[this.pn];
            this.popS[ix] = this.popS[this.pn];
            ix--;
         }
      }

      this.trauma = Math.max(0.0F, this.trauma - dt * 1.6F);
      this.flash = Math.max(0.0F, this.flash - dt * 3.0F);
      this.shakeT += dt;
      float s = this.trauma * this.trauma * 7.0F;
      this.sx = s * (float)(Math.sin(this.shakeT * 83.1) * 0.6 + Math.sin(this.shakeT * 51.7) * 0.4);
      this.sy = s * (float)(Math.cos(this.shakeT * 71.3) * 0.6 + Math.sin(this.shakeT * 43.9) * 0.4);
   }

   private void copy(int from, int to) {
      this.px[to] = this.px[from];
      this.py[to] = this.py[from];
      this.vx[to] = this.vx[from];
      this.vy[to] = this.vy[from];
      this.life[to] = this.life[from];
      this.max[to] = this.max[from];
      this.size[to] = this.size[from];
      this.col[to] = this.col[from];
      this.kind[to] = this.kind[from];
      this.grav[to] = this.grav[from];
      this.drag[to] = this.drag[from];
   }

   void draw(GameView g, float ox, float oy) {
      for (int i = 0; i < this.n; i++) {
         float t = this.life[i] / this.max[i];
         float a = 1.0F - t;
         float x = this.px[i] - ox;
         float y = this.py[i] - oy;
         float s = this.size[i];
         int c = this.col[i];
         switch (this.kind[i]) {
            case 1:
               g.box(x - s / 2.0F, y - s / 2.0F, s, s * 0.7F, s * 0.2F, Gx.withAlpha(c, Math.min(1.0F, a * 1.6F)));
               break;
            case 2:
               Gx.glow(g.X(x), g.Y(y), Math.max(1, g.S(s * 2.4F)), Gx.withAlpha(c, a * 0.5F));
               g.circle(x, y, s * (1.0F - t * 0.6F), Gx.withAlpha(Gx.mix(c, -1, 0.5F), a));
               break;
            case 3:
               float r = s * Ease.outCubic(t);
               Gx.ringAt(g.X(x), g.Y(y), Math.max(2, g.S(r)), Math.max(1, g.S(Math.max(0.6F, 2.2F * a))), Gx.withAlpha(c, a));
               break;
            case 4:
               g.circle(x, y, s * (0.6F + 0.4F * a), Gx.withAlpha(c, a * 0.9F));
               break;
            case 5:
               float l = Math.min(4.0F, (Math.abs(this.vx[i]) + Math.abs(this.vy[i])) * 0.02F + 1.0F);
               float nx = this.vx[i];
               float ny = this.vy[i];
               float len = (float)Math.sqrt(nx * nx + ny * ny) + 0.001F;

               for (int k = 0; k < 3; k++) {
                  g.circle(x - nx / len * k * s * l * 0.5F, y - ny / len * k * s * l * 0.5F, s * (1.0F - k * 0.25F), Gx.withAlpha(c, a * (1.0F - k * 0.3F)));
               }
               break;
            default:
               g.circle(x, y, s * (0.5F + 0.5F * a), Gx.withAlpha(c, a));
         }
      }
   }

   void drawPopups(GameView g, float ox, float oy) {
      for (int i = 0; i < this.pn; i++) {
         float t = this.popT[i] / 1.1F;
         float sc = t < 0.18F ? Ease.outBack(t / 0.18F) : 1.0F;
         float a = t < 0.65F ? 1.0F : 1.0F - (t - 0.65F) / 0.35F;
         float x = this.popX[i] - ox;
         float y = this.popY[i] - oy - Ease.outCubic(t) * 18.0F;
         Gx.push();
         Gx.scaleAt(g.X(x), g.Y(y), Math.max(0.05F, sc));
         outlined(g, this.popText[i], x, y, this.popS[i], Gx.withAlpha(this.popC[i], a), Gx.withAlpha(-15723748, a * 0.8F));
         Gx.pop();
      }
   }

   void drawFlash(GameView g, float w, float h) {
      if (this.flash > 0.01F) {
         g.box(0.0F, 0.0F, w, h, 0.0F, Gx.withAlpha(this.flashCol, this.flash * 0.45F));
      }
   }

   static void sprite(GameView g, String key, float x, float y, float w, float h, float dw, float dh, Consumer<Graphics2D> paint, int tint) {
      int pw = Math.max(2, g.S(w));
      int ph = Math.max(2, g.S(h));
      Gx.Img im = Gx.painted(key, pw, ph, gr -> {
         gr.scale(pw / dw, ph / dh);
         paint.accept(gr);
      });
      Gx.image(im, g.X(x), g.Y(y), pw, ph, tint);
   }

   static void spriteAt(
      GameView g, String key, float bw, float bh, float dw, float dh, Consumer<Graphics2D> paint, float x, float y, float w, float h, int tint
   ) {
      int pw = Math.max(2, g.S(bw));
      int ph = Math.max(2, g.S(bh));
      Gx.Img im = Gx.painted(key, pw, ph, gr -> {
         gr.scale(pw / dw, ph / dh);
         paint.accept(gr);
      });
      Gx.image(im, g.X(x), g.Y(y), Math.max(1, g.S(w)), Math.max(1, g.S(h)), tint);
   }

   static Area union(Shape... shapes) {
      Area a = new Area();

      for (Shape s : shapes) {
         a.add(new Area(s));
      }

      return a;
   }

   static Color c(int argb) {
      return new Color(argb, true);
   }

   static void outlined(GameView g, String s, float x, float y, float size, int color, int outline) {
      float o = Math.max(0.6F, size * 0.07F);

      for (int k = 0; k < 8; k++) {
         double an = k * Math.PI / 4.0;
         g.text(s, x + (float)Math.cos(an) * o, y + (float)Math.sin(an) * o + o * 0.3F, size, 3, outline);
      }

      g.text(s, x, y, size, 3, color);
   }
}
