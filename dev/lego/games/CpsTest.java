package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class CpsTest extends GameView {
   private static final float W = 360.0F;
   private static final float H = 260.0F;
   private static final float DURATION = 10.0F;
   private static final int BUCKETS = 20;
   private static final float GRAPH_Y = 196.0F;
   private static final float GRAPH_H = 50.0F;
   private boolean running;
   private boolean finished;
   private long startNs;
   private int clicks;
   private final int[] buckets = new int[20];
   private final List<float[]> ripples = new ArrayList<>();
   private final List<float[]> parts = new ArrayList<>();
   private float counterPop;
   private float finishT;
   private float graphMax = 12.0F;

   public CpsTest() {
      super("cps");
   }

   @Override
   protected float boardW() {
      return 360.0F;
   }

   @Override
   protected float boardH() {
      return 260.0F;
   }

   @Override
   protected String overTitle() {
      return "Fertig!";
   }

   @Override
   protected String scoreLabel() {
      return "CPS";
   }

   @Override
   protected String bestText(long best) {
      return String.format(Locale.ROOT, "%.1f", best / 10.0);
   }

   @Override
   protected String scoreText() {
      return !this.finished && this.state != GameView.State.OVER
         ? String.format(Locale.ROOT, "%.1f", this.liveCps())
         : String.format(Locale.ROOT, "%.1f", this.score / 10.0);
   }

   @Override
   protected void reset() {
      this.running = this.finished = false;
      this.clicks = 0;
      Arrays.fill(this.buckets, 0);
      this.ripples.clear();
      this.parts.clear();
      this.counterPop = this.finishT = 0.0F;
      this.graphMax = 12.0F;
   }

   private float elapsed() {
      return !this.running ? 0.0F : Math.min(10.0F, (float)(System.nanoTime() - this.startNs) / 1.0E9F);
   }

   private float liveCps() {
      return this.running && this.clicks != 0 ? this.clicks / Math.max(1.0F, this.elapsed()) : 0.0F;
   }

   @Override
   protected void onClick(float x, float y, int button) {
      if (!this.finished) {
         if (!this.running) {
            this.running = true;
            this.startNs = System.nanoTime();
         }

         float e = this.elapsed();
         if (!(e >= 10.0F)) {
            this.clicks++;
            this.buckets[Math.min(19, (int)(e / 0.5F))]++;
            this.counterPop = 1.0F;
            this.ripples.add(new float[]{x, y, 0.0F, button});
            int col = button == 0 ? Style.accent : this.info.color;

            for (int i = 0; i < 5; i++) {
               float a = (float)(this.rnd.nextFloat() * Math.PI * 2.0);
               float s = 30.0F + this.rnd.nextFloat() * 70.0F;
               this.parts
                  .add(
                     new float[]{
                        x,
                        y,
                        (float)Math.cos(a) * s,
                        (float)Math.sin(a) * s - 20.0F,
                        0.0F,
                        0.35F + this.rnd.nextFloat() * 0.25F,
                        1.6F + this.rnd.nextFloat() * 1.6F,
                        i % 2 == 0 ? col : -1.0F
                     }
                  );
            }

            Sound.play("minecraft:ui.button.click", 1.5F + this.rnd.nextFloat() * 0.4F, 0.12F);
         }
      }
   }

   @Override
   protected void update(float dt) {
      this.counterPop = Math.max(0.0F, this.counterPop - dt * 6.0F);

      for (float[] r : this.ripples) {
         r[2] += dt;
      }

      this.ripples.removeIf(r -> r[2] > 0.55F);

      for (float[] p : this.parts) {
         p[0] += p[2] * dt;
         p[1] += p[3] * dt;
         p[3] += 300.0F * dt;
         p[4] += dt;
      }

      this.parts.removeIf(px -> px[4] >= px[5]);
      int mb = 0;

      for (int b : this.buckets) {
         mb = Math.max(mb, b * 2);
      }

      this.graphMax = this.graphMax + (Math.max(12, mb + 3) - this.graphMax) * Math.min(1.0F, dt * 6.0F);
      if (this.running && !this.finished && this.elapsed() >= 10.0F) {
         this.finished = true;
         this.score = Math.round(this.clicks / 10.0F * 10.0F);
         Sound.play("minecraft:block.note_block.pling", 1.2F, 0.35F);
      }

      if (this.finished) {
         this.finishT += dt;
         if (this.finishT > 0.6F) {
            this.gameOver(true);
         }
      }
   }

   @Override
   protected void render() {
      float e = this.elapsed();
      float prog = e / 10.0F;
      this.boxV(0.0F, 0.0F, 360.0F, 260.0F, 12.0F, Style.light ? -1183756 : -15657957, Style.light ? -1973013 : -16118767);
      int studA = Style.light ? 268435456 : 201326591;

      for (float y = 12.0F; y < 180.0F; y += 18.0F) {
         for (float x = 12.0F; x < 360.0F; x += 18.0F) {
            this.circle(x, y, 3.0F, studA);
         }
      }

      float heat = this.running ? Math.min(1.0F, this.liveCps() / 14.0F) : 0.0F;
      Gx.glow(this.X(180.0F), this.Y(96.0F), this.S(150.0F), Gx.withAlpha(Style.accent, 0.06F + 0.12F * heat));

      for (float[] r : this.ripples) {
         float k = r[2] / 0.55F;
         float ek = Ease.outCubic(k);
         int col = r[3] == 0.0F ? Style.accent : this.info.color;
         Gx.ringAt(this.X(r[0]), this.Y(r[1]), this.S(4.0F + ek * 26.0F), Math.max(1, this.S(2.4F * (1.0F - k) + 0.3F)), Gx.withAlpha(col, 0.85F * (1.0F - k)));
         if (k < 0.4F) {
            this.circle(r[0], r[1], 3.0F * (1.0F - k / 0.4F), Gx.withAlpha(-1, 0.8F * (1.0F - k / 0.4F)));
         }
      }

      if (!this.running) {
         float pulse = (float)(0.5 + 0.5 * Math.sin(this.time * 4.0F));
         float cx = 180.0F;
         float cy = 92.0F;
         Gx.ringAt(
            this.X(cx), this.Y(cy - 10.0F), this.S(30.0F + pulse * 6.0F), Math.max(1, this.S(1.2F)), Gx.withAlpha(Style.accent, 0.25F + 0.25F * (1.0F - pulse))
         );
         Gx.glow(this.X(cx), this.Y(cy - 10.0F), this.S(46.0F), Gx.withAlpha(Style.accent, 0.18F));
         this.circle(cx, cy - 10.0F, 24.0F, Gx.withAlpha(Style.accent, 0.16F));
         Gx.icon("mouse", this.X(cx), this.Y(cy - 10.0F), this.S(22.0F), Style.accent);
         this.text("Klick um zu starten", cx, cy + 34.0F, 17.0F, 3, Style.text);
         this.text("10 Sekunden  •  Links- und Rechtsklick zählen", cx, cy + 52.0F, 8.0F, 2, Style.sub);
      } else {
         float left = Math.max(0.0F, 10.0F - e);
         this.box(16.0F, 14.0F, 328.0F, 5.0F, 2.5F, Style.light ? 335544320 : 352321535);
         int bc = left < 3.0F ? Style.danger : Style.accent;
         this.box(16.0F, 14.0F, 328.0F * (1.0F - prog), 5.0F, 2.5F, bc);
         Gx.glow(this.X(16.0F + 328.0F * (1.0F - prog)), this.Y(16.5F), this.S(9.0F), Gx.withAlpha(bc, 0.6F));
         Gx.icon("clock", this.X(20.0F), this.Y(31.0F), this.S(9.0F), Style.sub);
         Gx.textMid(String.format(Locale.ROOT, "%.2f s", left), this.X(28.0F), this.Y(31.0F), this.S(9.0F), 3, left < 3.0F ? Style.danger : Style.text);
         Gx.textRight(
            "CPS " + String.format(Locale.ROOT, "%.1f", this.finished ? (float)this.score / 10.0F : this.liveCps()),
            this.X(344.0F),
            this.Y(31.0F),
            this.S(9.0F),
            3,
            Style.accent
         );
         float pop = 1.0F + 0.14F * Ease.outCubic(this.counterPop);
         Gx.push();
         Gx.scaleAt(this.X(180.0F), this.Y(96.0F), pop);
         Gx.glow(this.X(180.0F), this.Y(96.0F), this.S(60.0F), Gx.withAlpha(Style.accent, 0.1F + 0.15F * this.counterPop));
         this.text(String.valueOf(this.clicks), 180.0F, 94.0F, 52.0F, 3, Style.text);
         Gx.pop();
         this.text(this.finished ? "Zeit um!" : "KLICKS", 180.0F, 132.0F, 7.5F, 3, this.finished ? Style.accent : Style.muted);
         if (this.finished) {
            float fe = Ease.outBack(Math.min(1.0F, this.finishT / 0.3F));
            Gx.push();
            Gx.scaleAt(this.X(180.0F), this.Y(156.0F), fe);
            String s = String.format(Locale.ROOT, "%.1f CPS", (float)this.score / 10.0F);
            float w = Gx.width(s, this.S(11.0F), 3) / this.u + 24.0F;
            this.box(180.0F - w / 2.0F, 146.0F, w, 20.0F, 10.0F, Gx.withAlpha(Style.accent, 0.2F));
            this.text(s, 180.0F, 156.0F, 11.0F, 3, Style.accent);
            Gx.pop();
         }
      }

      this.graph(e);

      for (float[] p : this.parts) {
         float a = 1.0F - p[4] / p[5];
         this.box(p[0] - p[6] / 2.0F, p[1] - p[6] / 2.0F, p[6], p[6], p[6] * 0.25F, Gx.withAlpha((int)p[7], a));
      }
   }

   private void graph(float e) {
      float gx = 16.0F;
      float gw = 328.0F;
      float gy = 196.0F;
      float gh = 50.0F;
      this.box(gx - 6.0F, gy - 12.0F, gw + 12.0F, gh + 20.0F, 9.0F, Style.light ? 201326592 : 855638016);
      Gx.textMid("VERLAUF", this.X(gx), this.Y(gy - 5.0F), this.S(5.4F), 3, Style.muted);
      Gx.textRight("CPS / 0,5 s", this.X(gx + gw), this.Y(gy - 5.0F), this.S(5.4F), 3, Style.muted);
      float base = gy + gh - 2.0F;
      float plotH = gh - 6.0F;

      for (int i = 1; i <= 2; i++) {
         this.box(gx, base - plotH * i / 2.0F, gw, 0.5F, 0.0F, Style.light ? 268435456 : 285212671);
      }

      int cur = this.running ? Math.min(19, (int)(e / 0.5F)) : -1;
      float bw = gw / 20.0F;
      float[] px = new float[20];
      float[] py = new float[20];
      int n = 0;

      for (int i = 0; i < 20; i++) {
         float cps = this.buckets[i] * 2;
         float h = plotH * Math.min(1.0F, cps / this.graphMax);
         float x = gx + i * bw;
         boolean active = this.running && (i <= cur || this.finished);
         if (active && h > 0.5F) {
            int c = i == cur && !this.finished ? Gx.mix(Style.accent, -1, 0.25F) : Style.accent;
            this.boxV(x + 1.2F, base - h, bw - 2.4F, h, 1.5F, Gx.withAlpha(c, 0.45F), Gx.withAlpha(c, 0.08F));
         } else {
            this.box(x + 1.2F, base - 1.0F, bw - 2.4F, 1.0F, 0.5F, Style.light ? 335544320 : 352321535);
         }

         if (active && (this.finished || i < cur)) {
            px[n] = x + bw / 2.0F;
            py[n] = base - h;
            n++;
         }
      }

      float step = Math.max(0.6F, 1.2F / this.u * 1.6F);

      for (int i = 0; i + 1 < n; i++) {
         float dx = px[i + 1] - px[i];
         float dy = py[i + 1] - py[i];
         float len = (float)Math.sqrt(dx * dx + dy * dy);

         for (float s = 0.0F; s < len; s += step) {
            float t = s / len;
            this.circle(px[i] + dx * t, py[i] + dy * t, 0.9F, Style.accent);
         }
      }

      for (int i = 0; i < n; i++) {
         boolean last = i == n - 1 && !this.finished;
         this.circle(px[i], py[i], last ? 2.2F : 1.5F, last ? -1 : Style.accent);
         if (last) {
            Gx.glow(this.X(px[i]), this.Y(py[i]), this.S(8.0F), Gx.withAlpha(Style.accent, 0.6F));
         }
      }

      if (this.running && !this.finished) {
         float nx = gx + gw * (e / 10.0F);
         this.box(nx - 0.4F, gy - 1.0F, 0.8F, gh - 1.0F, 0.0F, Gx.withAlpha(Style.accent, 0.35F));
      }
   }
}
