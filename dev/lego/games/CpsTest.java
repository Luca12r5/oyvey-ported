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
   protected String bestText(long var1) {
      return String.format(Locale.ROOT, "%.1f", var1 / 10.0);
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
   protected void onClick(float var1, float var2, int var3) {
      if (!this.finished) {
         if (!this.running) {
            this.running = true;
            this.startNs = System.nanoTime();
         }

         float var4 = this.elapsed();
         if (!(var4 >= 10.0F)) {
            this.clicks++;
            this.buckets[Math.min(19, (int)(var4 / 0.5F))]++;
            this.counterPop = 1.0F;
            this.ripples.add(new float[]{var1, var2, 0.0F, var3});
            int var5 = var3 == 0 ? Style.accent : this.info.color;

            for (int var6 = 0; var6 < 5; var6++) {
               float var7 = (float)(this.rnd.nextFloat() * Math.PI * 2.0);
               float var8 = 30.0F + this.rnd.nextFloat() * 70.0F;
               this.parts
                  .add(
                     new float[]{
                        var1,
                        var2,
                        (float)Math.cos(var7) * var8,
                        (float)Math.sin(var7) * var8 - 20.0F,
                        0.0F,
                        0.35F + this.rnd.nextFloat() * 0.25F,
                        1.6F + this.rnd.nextFloat() * 1.6F,
                        var6 % 2 == 0 ? var5 : -1.0F
                     }
                  );
            }

            Sound.play("minecraft:ui.button.click", 1.5F + this.rnd.nextFloat() * 0.4F, 0.12F);
         }
      }
   }

   @Override
   protected void update(float var1) {
      this.counterPop = Math.max(0.0F, this.counterPop - var1 * 6.0F);

      for (float[] var3 : this.ripples) {
         var3[2] += var1;
      }

      this.ripples.removeIf(var0 -> var0[2] > 0.55F);

      for (float[] var9 : this.parts) {
         var9[0] += var9[2] * var1;
         var9[1] += var9[3] * var1;
         var9[3] += 300.0F * var1;
         var9[4] += var1;
      }

      this.parts.removeIf(var0 -> var0[4] >= var0[5]);
      int var8 = 0;

      for (int var6 : this.buckets) {
         var8 = Math.max(var8, var6 * 2);
      }

      this.graphMax = this.graphMax + (Math.max(12, var8 + 3) - this.graphMax) * Math.min(1.0F, var1 * 6.0F);
      if (this.running && !this.finished && this.elapsed() >= 10.0F) {
         this.finished = true;
         this.score = Math.round(this.clicks / 10.0F * 10.0F);
         Sound.play("minecraft:block.note_block.pling", 1.2F, 0.35F);
      }

      if (this.finished) {
         this.finishT += var1;
         if (this.finishT > 0.6F) {
            this.gameOver(true);
         }
      }
   }

   @Override
   protected void render() {
      float var1 = this.elapsed();
      float var2 = var1 / 10.0F;
      this.boxV(0.0F, 0.0F, 360.0F, 260.0F, 12.0F, Style.light ? -1183756 : -15657957, Style.light ? -1973013 : -16118767);
      int var3 = Style.light ? 268435456 : 201326591;

      for (float var4 = 12.0F; var4 < 180.0F; var4 += 18.0F) {
         for (float var5 = 12.0F; var5 < 360.0F; var5 += 18.0F) {
            this.circle(var5, var4, 3.0F, var3);
         }
      }

      float var11 = this.running ? Math.min(1.0F, this.liveCps() / 14.0F) : 0.0F;
      Gx.glow(this.X(180.0F), this.Y(96.0F), this.S(150.0F), Gx.withAlpha(Style.accent, 0.06F + 0.12F * var11));

      for (float[] var6 : this.ripples) {
         float var7 = var6[2] / 0.55F;
         float var8 = Ease.outCubic(var7);
         int var9 = var6[3] == 0.0F ? Style.accent : this.info.color;
         Gx.ringAt(
            this.X(var6[0]),
            this.Y(var6[1]),
            this.S(4.0F + var8 * 26.0F),
            Math.max(1, this.S(2.4F * (1.0F - var7) + 0.3F)),
            Gx.withAlpha(var9, 0.85F * (1.0F - var7))
         );
         if (var7 < 0.4F) {
            this.circle(var6[0], var6[1], 3.0F * (1.0F - var7 / 0.4F), Gx.withAlpha(-1, 0.8F * (1.0F - var7 / 0.4F)));
         }
      }

      if (!this.running) {
         float var13 = (float)(0.5 + 0.5 * Math.sin(this.time * 4.0F));
         float var16 = 180.0F;
         float var19 = 92.0F;
         Gx.ringAt(
            this.X(var16),
            this.Y(var19 - 10.0F),
            this.S(30.0F + var13 * 6.0F),
            Math.max(1, this.S(1.2F)),
            Gx.withAlpha(Style.accent, 0.25F + 0.25F * (1.0F - var13))
         );
         Gx.glow(this.X(var16), this.Y(var19 - 10.0F), this.S(46.0F), Gx.withAlpha(Style.accent, 0.18F));
         this.circle(var16, var19 - 10.0F, 24.0F, Gx.withAlpha(Style.accent, 0.16F));
         Gx.icon("mouse", this.X(var16), this.Y(var19 - 10.0F), this.S(22.0F), Style.accent);
         this.text("Klick um zu starten", var16, var19 + 34.0F, 17.0F, 3, Style.text);
         this.text("10 Sekunden  •  Links- und Rechtsklick zählen", var16, var19 + 52.0F, 8.0F, 2, Style.sub);
      } else {
         float var14 = Math.max(0.0F, 10.0F - var1);
         this.box(16.0F, 14.0F, 328.0F, 5.0F, 2.5F, Style.light ? 335544320 : 352321535);
         int var17 = var14 < 3.0F ? Style.danger : Style.accent;
         this.box(16.0F, 14.0F, 328.0F * (1.0F - var2), 5.0F, 2.5F, var17);
         Gx.glow(this.X(16.0F + 328.0F * (1.0F - var2)), this.Y(16.5F), this.S(9.0F), Gx.withAlpha(var17, 0.6F));
         Gx.icon("clock", this.X(20.0F), this.Y(31.0F), this.S(9.0F), Style.sub);
         Gx.textMid(String.format(Locale.ROOT, "%.2f s", var14), this.X(28.0F), this.Y(31.0F), this.S(9.0F), 3, var14 < 3.0F ? Style.danger : Style.text);
         Gx.textRight(
            "CPS " + String.format(Locale.ROOT, "%.1f", this.finished ? (float)this.score / 10.0F : this.liveCps()),
            this.X(344.0F),
            this.Y(31.0F),
            this.S(9.0F),
            3,
            Style.accent
         );
         float var20 = 1.0F + 0.14F * Ease.outCubic(this.counterPop);
         Gx.push();
         Gx.scaleAt(this.X(180.0F), this.Y(96.0F), var20);
         Gx.glow(this.X(180.0F), this.Y(96.0F), this.S(60.0F), Gx.withAlpha(Style.accent, 0.1F + 0.15F * this.counterPop));
         this.text(String.valueOf(this.clicks), 180.0F, 94.0F, 52.0F, 3, Style.text);
         Gx.pop();
         this.text(this.finished ? "Zeit um!" : "KLICKS", 180.0F, 132.0F, 7.5F, 3, this.finished ? Style.accent : Style.muted);
         if (this.finished) {
            float var22 = Ease.outBack(Math.min(1.0F, this.finishT / 0.3F));
            Gx.push();
            Gx.scaleAt(this.X(180.0F), this.Y(156.0F), var22);
            String var23 = String.format(Locale.ROOT, "%.1f CPS", (float)this.score / 10.0F);
            float var10 = Gx.width(var23, this.S(11.0F), 3) / this.u + 24.0F;
            this.box(180.0F - var10 / 2.0F, 146.0F, var10, 20.0F, 10.0F, Gx.withAlpha(Style.accent, 0.2F));
            this.text(var23, 180.0F, 156.0F, 11.0F, 3, Style.accent);
            Gx.pop();
         }
      }

      this.graph(var1);

      for (float[] var18 : this.parts) {
         float var21 = 1.0F - var18[4] / var18[5];
         this.box(var18[0] - var18[6] / 2.0F, var18[1] - var18[6] / 2.0F, var18[6], var18[6], var18[6] * 0.25F, Gx.withAlpha((int)var18[7], var21));
      }
   }

   private void graph(float var1) {
      float var2 = 16.0F;
      float var3 = 328.0F;
      float var4 = 196.0F;
      float var5 = 50.0F;
      this.box(var2 - 6.0F, var4 - 12.0F, var3 + 12.0F, var5 + 20.0F, 9.0F, Style.light ? 201326592 : 855638016);
      Gx.textMid("VERLAUF", this.X(var2), this.Y(var4 - 5.0F), this.S(5.4F), 3, Style.muted);
      Gx.textRight("CPS / 0,5 s", this.X(var2 + var3), this.Y(var4 - 5.0F), this.S(5.4F), 3, Style.muted);
      float var6 = var4 + var5 - 2.0F;
      float var7 = var5 - 6.0F;

      for (int var8 = 1; var8 <= 2; var8++) {
         this.box(var2, var6 - var7 * var8 / 2.0F, var3, 0.5F, 0.0F, Style.light ? 268435456 : 285212671);
      }

      int var20 = this.running ? Math.min(19, (int)(var1 / 0.5F)) : -1;
      float var9 = var3 / 20.0F;
      float[] var10 = new float[20];
      float[] var11 = new float[20];
      int var12 = 0;

      for (int var13 = 0; var13 < 20; var13++) {
         float var14 = this.buckets[var13] * 2;
         float var15 = var7 * Math.min(1.0F, var14 / this.graphMax);
         float var16 = var2 + var13 * var9;
         boolean var17 = this.running && (var13 <= var20 || this.finished);
         if (var17 && var15 > 0.5F) {
            int var18 = var13 == var20 && !this.finished ? Gx.mix(Style.accent, -1, 0.25F) : Style.accent;
            this.boxV(var16 + 1.2F, var6 - var15, var9 - 2.4F, var15, 1.5F, Gx.withAlpha(var18, 0.45F), Gx.withAlpha(var18, 0.08F));
         } else {
            this.box(var16 + 1.2F, var6 - 1.0F, var9 - 2.4F, 1.0F, 0.5F, Style.light ? 335544320 : 352321535);
         }

         if (var17 && (this.finished || var13 < var20)) {
            var10[var12] = var16 + var9 / 2.0F;
            var11[var12] = var6 - var15;
            var12++;
         }
      }

      float var21 = Math.max(0.6F, 1.2F / this.u * 1.6F);

      for (int var22 = 0; var22 + 1 < var12; var22++) {
         float var25 = var10[var22 + 1] - var10[var22];
         float var27 = var11[var22 + 1] - var11[var22];
         float var28 = (float)Math.sqrt(var25 * var25 + var27 * var27);

         for (float var29 = 0.0F; var29 < var28; var29 += var21) {
            float var19 = var29 / var28;
            this.circle(var10[var22] + var25 * var19, var11[var22] + var27 * var19, 0.9F, Style.accent);
         }
      }

      for (int var23 = 0; var23 < var12; var23++) {
         boolean var26 = var23 == var12 - 1 && !this.finished;
         this.circle(var10[var23], var11[var23], var26 ? 2.2F : 1.5F, var26 ? -1 : Style.accent);
         if (var26) {
            Gx.glow(this.X(var10[var23]), this.Y(var11[var23]), this.S(8.0F), Gx.withAlpha(Style.accent, 0.6F));
         }
      }

      if (this.running && !this.finished) {
         float var24 = var2 + var3 * (var1 / 10.0F);
         this.box(var24 - 0.4F, var4 - 1.0F, 0.8F, var5 - 1.0F, 0.0F, Gx.withAlpha(Style.accent, 0.35F));
      }
   }
}
