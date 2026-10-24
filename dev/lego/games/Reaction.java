package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;

public final class Reaction extends GameView {
   private static final float W = 360.0F;
   private static final float H = 260.0F;
   private static final float STRIP = 52.0F;
   private static final int ROUNDS = 5;
   private static final int RED_TOP = -10873310;
   private static final int RED_BOT = -14022126;
   private static final int GREEN_TOP = -13705104;
   private static final int GREEN_BOT = -15423412;
   private static final int EARLY_TOP = -2065890;
   private static final int EARLY_BOT = -6667768;
   private Reaction.Phase phase = Reaction.Phase.WAIT;
   private float phaseT;
   private float waitDur;
   private long greenNs;
   private int lastMs;
   private boolean fresh;
   private final List<Integer> results = new ArrayList<>();
   private final List<float[]> parts = new ArrayList<>();
   private int curTop = -10873310;
   private int curBot = -14022126;
   private static final String[] RATING = new String[]{"Blitzschnell!", "Sehr gut!", "Gut", "Okay", "Langsam"};

   public Reaction() {
      super("reaction");
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
   protected String scoreLabel() {
      return "Schnitt";
   }

   @Override
   protected String scoreText() {
      return this.results.isEmpty() ? "-" : this.average() + " ms";
   }

   @Override
   protected String bestText(long var1) {
      return var1 + " ms";
   }

   @Override
   protected String overTitle() {
      return "Fertig!";
   }

   @Override
   protected void reset() {
      this.results.clear();
      this.parts.clear();
      this.lastMs = 0;
      this.fresh = true;
      this.enterWait();
      this.curTop = -10873310;
      this.curBot = -14022126;
   }

   private void enterWait() {
      this.phase = Reaction.Phase.WAIT;
      this.phaseT = 0.0F;
      this.waitDur = 1.5F + this.rnd.nextFloat() * 2.5F;
   }

   private long average() {
      if (this.results.isEmpty()) {
         return 0L;
      } else {
         long var1 = 0L;

         for (int var4 : this.results) {
            var1 += var4;
         }

         return Math.round((double)var1 / this.results.size());
      }
   }

   @Override
   protected void onClick(float var1, float var2, int var3) {
      this.press(var1, var2);
   }

   @Override
   protected void onKey(int var1) {
      if (var1 == 32 || var1 == 257) {
         this.press(180.0F, 104.0F);
      }
   }

   private void press(float var1, float var2) {
      if (!this.fresh) {
         switch (this.phase) {
            case WAIT:
               this.phase = Reaction.Phase.EARLY;
               this.phaseT = 0.0F;
               Sound.play("minecraft:block.note_block.bass", 0.6F, 0.4F);
               break;
            case GO:
               long var3 = System.nanoTime() - this.greenNs;
               this.lastMs = (int)Math.max(1L, Math.round(var3 / 1000000.0));
               this.results.add(this.lastMs);
               this.phase = Reaction.Phase.RESULT;
               this.phaseT = 0.0F;
               this.burst(var1, var2, rating(this.lastMs)[1]);
               Sound.play("minecraft:entity.experience_orb.pickup", 0.9F + Math.max(0, 450 - this.lastMs) / 450.0F * 0.9F, 0.35F);
            case EARLY:
            default:
               break;
            case RESULT:
               if (this.results.size() < 5 && this.phaseT > 0.3F) {
                  this.enterWait();
                  Sound.click();
               }
         }
      }
   }

   private void burst(float var1, float var2, int var3) {
      for (int var4 = 0; var4 < 26; var4++) {
         float var5 = (float)(this.rnd.nextFloat() * Math.PI * 2.0);
         float var6 = 50.0F + this.rnd.nextFloat() * 150.0F;
         int var7 = var4 % 3 == 0 ? -1 : var3;
         this.parts
            .add(
               new float[]{
                  var1,
                  var2,
                  (float)Math.cos(var5) * var6,
                  (float)Math.sin(var5) * var6 - 40.0F,
                  0.0F,
                  0.6F + this.rnd.nextFloat() * 0.5F,
                  2.0F + this.rnd.nextFloat() * 3.0F,
                  var7
               }
            );
      }
   }

   @Override
   protected void update(float var1) {
      this.fresh = false;
      this.phaseT += var1;

      for (float[] var3 : this.parts) {
         var3[0] += var3[2] * var1;
         var3[1] += var3[3] * var1;
         var3[3] += 380.0F * var1;
         var3[2] *= 0.985F;
         var3[4] += var1;
      }

      this.parts.removeIf(var0 -> var0[4] >= var0[5]);
      switch (this.phase) {
         case WAIT:
            if (this.phaseT >= this.waitDur) {
               this.phase = Reaction.Phase.GO;
               this.phaseT = 0.0F;
               this.greenNs = System.nanoTime();
               this.curTop = -13705104;
               this.curBot = -15423412;
               Sound.play("minecraft:block.note_block.pling", 1.8F, 0.35F);
            }
         case GO:
         default:
            break;
         case EARLY:
            if (this.phaseT >= 1.0F) {
               this.enterWait();
            }
            break;
         case RESULT:
            if (this.results.size() >= 5) {
               if (this.phaseT >= 1.5F) {
                  this.score = this.average();
                  this.gameOver(true);
               }
            } else if (this.phaseT >= 1.8F) {
               this.enterWait();
            }
      }
   }

   private static int[] rating(int var0) {
      if (var0 < 180) {
         return new int[]{0, -15043};
      } else if (var0 < 230) {
         return new int[]{1, -14756000};
      } else if (var0 < 280) {
         return new int[]{2, -12597249};
      } else {
         return var0 < 350 ? new int[]{3, -7859} : new int[]{4, -34235};
      }
   }

   @Override
   protected void render() {
      float var1 = 208.0F;
      int var2;
      int var3;
      switch (this.phase) {
         case GO:
            var2 = -13705104;
            var3 = -15423412;
            break;
         case EARLY:
            var2 = -2065890;
            var3 = -6667768;
            break;
         case RESULT:
            int var4 = Style.light ? -14012352 : -15328732;
            var2 = Gx.mix(var4, Style.accent, 0.22F);
            var3 = Gx.mix(var4, -16777216, 0.25F);
            break;
         default:
            var2 = -10873310;
            var3 = -14022126;
      }

      float var21 = Math.min(1.0F, Gx.dt * 12.0F);
      if (this.state != GameView.State.PLAYING) {
         var21 = 1.0F;
      }

      this.curTop = Gx.mix(this.curTop, var2, var21);
      this.curBot = Gx.mix(this.curBot, var3, var21);
      this.boxV(0.0F, 0.0F, 360.0F, var1, 12.0F, this.curTop, this.curBot);
      this.boxV(0.0F, var1 - 14.0F, 360.0F, 14.0F, 0.0F, Gx.mix(this.curTop, this.curBot, (var1 - 14.0F) / var1), this.curBot);

      for (float var5 = 14.0F; var5 < var1; var5 += 20.0F) {
         for (float var6 = 14.0F; var6 < 360.0F; var6 += 20.0F) {
            this.circle(var6, var5 + 0.6F, 3.6F, 335544320);
            this.circle(var6, var5, 3.4F, 218103807);
         }
      }

      float var22 = 180.0F;
      float var23 = var1 / 2.0F - 6.0F;
      String var7 = "Runde " + Math.min(5, this.results.size() + (this.phase == Reaction.Phase.RESULT ? 0 : 1)) + " / 5";
      this.pill(var7, var22, 16.0F, 855638016, -570425345);
      switch (this.phase) {
         case WAIT:
            float var26 = (float)(0.5 + 0.5 * Math.sin(this.time * 3.2));
            Gx.glow(this.X(var22), this.Y(var23 - 22.0F), this.S(52.0F + var26 * 8.0F), Gx.withAlpha(-45730, 0.22F + var26 * 0.1F));

            for (int var31 = 0; var31 < 8; var31++) {
               double var36 = this.time * 4.0F + var31 * Math.PI / 4.0;
               float var40 = 0.25F + 0.75F * (var31 / 7.0F);
               this.circle(var22 + (float)Math.cos(var36) * 17.0F, var23 - 22.0F + (float)Math.sin(var36) * 17.0F, 2.2F, Gx.withAlpha(-19526, var40));
            }

            Gx.icon("hourglass", this.X(var22), this.Y(var23 - 22.0F), this.S(16.0F), -10534);
            this.text("Warte auf Grün…", var22, var23 + 16.0F, 19.0F, 3, -1);
            this.text("Klick erst, wenn das Feld grün wird", var22, var23 + 34.0F, 8.5F, 2, -1275068417);
            break;
         case GO:
            float var25 = Ease.outBack(Math.min(1.0F, this.phaseT / 0.18F));

            for (int var30 = 0; var30 < 3; var30++) {
               float var35 = (this.phaseT * 1.6F + var30 / 3.0F) % 1.0F;
               Gx.ringAt(
                  this.X(var22),
                  this.Y(var23 - 20.0F),
                  this.S(14.0F + var35 * 70.0F),
                  Math.max(1, this.S(2.2F * (1.0F - var35))),
                  Gx.withAlpha(-1, 0.5F * (1.0F - var35))
               );
            }

            Gx.glow(this.X(var22), this.Y(var23 - 20.0F), this.S(60.0F), 1442840575);
            Gx.push();
            Gx.scaleAt(this.X(var22), this.Y(var23), 0.6F + 0.4F * var25);
            Gx.icon("bolt", this.X(var22), this.Y(var23 - 20.0F), this.S(24.0F), -1);
            this.text("JETZT KLICKEN!", var22, var23 + 20.0F, 26.0F, 3, -1);
            Gx.pop();
            break;
         case EARLY:
            float var24 = (float)Math.sin(this.phaseT * 60.0F) * 5.0F * Math.max(0.0F, 1.0F - this.phaseT * 3.0F);
            float var29 = Ease.outBack(Math.min(1.0F, this.phaseT / 0.25F));
            Gx.push();
            Gx.translate(this.S(var24), 0.0F);
            Gx.scaleAt(this.X(var22), this.Y(var23), 0.7F + 0.3F * var29);
            Gx.icon("warning", this.X(var22), this.Y(var23 - 22.0F), this.S(24.0F), -1);
            this.text("Zu früh!", var22, var23 + 16.0F, 24.0F, 3, -1);
            this.text("Warte auf Grün – die Runde wird wiederholt", var22, var23 + 36.0F, 8.5F, 2, -855638017);
            Gx.pop();
            float var34 = Math.min(1.0F, this.phaseT / 1.0F);
            this.box(var22 - 40.0F, var23 + 50.0F, 80.0F, 3.0F, 1.5F, 1090519039);
            this.box(var22 - 40.0F, var23 + 50.0F, 80.0F * (1.0F - var34), 3.0F, 1.5F, -1);
            break;
         case RESULT:
            int[] var8 = rating(this.lastMs);
            float var9 = Ease.outBack(Math.min(1.0F, this.phaseT / 0.3F));
            float var10 = Ease.outCubic(Math.min(1.0F, this.phaseT / 0.35F));
            Gx.glow(this.X(var22), this.Y(var23 - 8.0F), this.S(80.0F), Gx.withAlpha(var8[1], 0.22F));
            Gx.push();
            Gx.scaleAt(this.X(var22), this.Y(var23 - 8.0F), 0.55F + 0.45F * var9);
            int var11 = Math.round(this.lastMs * var10);
            float var12 = Gx.width(String.valueOf(var11), this.S(38.0F), 3) / this.u;
            float var13 = Gx.width(" ms", this.S(16.0F), 3) / this.u;
            float var14 = var22 - (var12 + var13) / 2.0F;
            Gx.textMid(String.valueOf(var11), this.X(var14), this.Y(var23 - 12.0F), this.S(38.0F), 3, -1);
            Gx.textMid(" ms", this.X(var14 + var12), this.Y(var23 - 5.0F), this.S(16.0F), 3, -1275068417);
            Gx.pop();
            float var15 = Ease.outCubic(Math.min(1.0F, Math.max(0.0F, this.phaseT - 0.15F) / 0.3F));
            Gx.pushAlpha(var15);
            String var16 = RATING[var8[0]];
            float var17 = Gx.width(var16, this.S(10.0F), 3) / this.u + 22.0F;
            this.box(var22 - var17 / 2.0F, var23 + 18.0F + (1.0F - var15) * 6.0F, var17, 18.0F, 9.0F, Gx.withAlpha(var8[1], 0.2F));
            this.text(var16, var22, var23 + 27.0F + (1.0F - var15) * 6.0F, 10.0F, 3, var8[1]);
            String var18 = this.results.size() >= 5 ? "Auswertung…" : "Klick für die nächste Runde";
            this.text(var18, var22, var23 + 50.0F, 7.5F, 2, -1711276033);
            Gx.popAlpha();
      }

      for (float[] var32 : this.parts) {
         float var37 = 1.0F - var32[4] / var32[5];
         this.box(var32[0] - var32[6] / 2.0F, var32[1] - var32[6] / 2.0F, var32[6], var32[6], var32[6] * 0.25F, Gx.withAlpha((int)var32[7], var37));
      }

      int var28 = Style.light ? -2236185 : -15658216;
      this.box(0.0F, var1, 360.0F, 52.0F, 12.0F, var28);
      this.box(0.0F, var1, 360.0F, 14.0F, 0.0F, var28);
      this.box(0.0F, var1, 360.0F, 0.8F, 0.0F, Style.stroke);
      float var33 = 46.0F;
      float var38 = 32.0F;
      float var39 = 6.0F;
      float var41 = 12.0F;
      float var42 = var1 + (52.0F - var38) / 2.0F;

      for (int var43 = 0; var43 < 5; var43++) {
         float var45 = var41 + var43 * (var33 + var39);
         boolean var47 = var43 < this.results.size();
         boolean var48 = var43 == this.results.size() && this.state == GameView.State.PLAYING;
         if (var47) {
            int var49 = this.results.get(var43);
            int var19 = rating(var49)[1];
            float var20 = var43 == this.results.size() - 1 && this.phase == Reaction.Phase.RESULT ? Ease.outBack(Math.min(1.0F, this.phaseT / 0.3F)) : 1.0F;
            Gx.push();
            Gx.scaleAt(this.X(var45 + var33 / 2.0F), this.Y(var42 + var38 / 2.0F), var20);
            this.box(var45, var42, var33, var38, 8.0F, Gx.withAlpha(var19, 0.16F));
            Gx.outline(this.X(var45), this.Y(var42), this.S(var33), this.S(var38), this.S(8.0F), Math.max(1, this.S(0.7F)), Gx.withAlpha(var19, 0.55F));
            this.text(var49 + "", var45 + var33 / 2.0F, var42 + 13.0F, 10.0F, 3, Style.text);
            this.text("ms", var45 + var33 / 2.0F, var42 + 24.0F, 6.0F, 2, var19);
            Gx.pop();
         } else {
            this.box(var45, var42, var33, var38, 8.0F, Style.light ? -1117964 : -15065820);
            if (var48) {
               float var50 = (float)(0.5 + 0.5 * Math.sin(this.time * 5.0F));
               Gx.outline(
                  this.X(var45),
                  this.Y(var42),
                  this.S(var33),
                  this.S(var38),
                  this.S(8.0F),
                  Math.max(1, this.S(0.9F)),
                  Gx.withAlpha(Style.accent, 0.5F + 0.5F * var50)
               );
            }

            this.text(String.valueOf(var43 + 1), var45 + var33 / 2.0F, var42 + var38 / 2.0F, 9.0F, 3, var48 ? Style.accent : Style.muted);
         }
      }

      float var44 = var41 + 5.0F * (var33 + var39) + 2.0F;
      float var46 = 348.0F - var44;
      this.box(var44, var42, var46, var38, 8.0F, Gx.withAlpha(Style.accent, 0.14F));
      this.text("SCHNITT", var44 + var46 / 2.0F, var42 + 9.0F, 5.8F, 3, Style.accent);
      this.text(this.results.isEmpty() ? "-" : this.average() + " ms", var44 + var46 / 2.0F, var42 + 22.0F, 10.0F, 3, Style.text);
   }

   private void pill(String var1, float var2, float var3, int var4, int var5) {
      float var6 = Gx.width(var1, this.S(7.5F), 3) / this.u + 18.0F;
      this.box(var2 - var6 / 2.0F, var3 - 8.0F, var6, 16.0F, 8.0F, var4);
      this.text(var1, var2, var3, 7.5F, 3, var5);
   }

   private static enum Phase {
      WAIT,
      GO,
      EARLY,
      RESULT;
   }
}
