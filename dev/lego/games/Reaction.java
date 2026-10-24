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
   protected String bestText(long best) {
      return best + " ms";
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
         long s = 0L;

         for (int r : this.results) {
            s += r;
         }

         return Math.round((double)s / this.results.size());
      }
   }

   @Override
   protected void onClick(float x, float y, int button) {
      this.press(x, y);
   }

   @Override
   protected void onKey(int k) {
      if (k == 32 || k == 257) {
         this.press(180.0F, 104.0F);
      }
   }

   private void press(float x, float y) {
      if (!this.fresh) {
         switch (this.phase) {
            case WAIT:
               this.phase = Reaction.Phase.EARLY;
               this.phaseT = 0.0F;
               Sound.play("minecraft:block.note_block.bass", 0.6F, 0.4F);
               break;
            case GO:
               long ns = System.nanoTime() - this.greenNs;
               this.lastMs = (int)Math.max(1L, Math.round(ns / 1000000.0));
               this.results.add(this.lastMs);
               this.phase = Reaction.Phase.RESULT;
               this.phaseT = 0.0F;
               this.burst(x, y, rating(this.lastMs)[1]);
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

   private void burst(float x, float y, int col) {
      for (int i = 0; i < 26; i++) {
         float a = (float)(this.rnd.nextFloat() * Math.PI * 2.0);
         float s = 50.0F + this.rnd.nextFloat() * 150.0F;
         int c = i % 3 == 0 ? -1 : col;
         this.parts
            .add(
               new float[]{
                  x, y, (float)Math.cos(a) * s, (float)Math.sin(a) * s - 40.0F, 0.0F, 0.6F + this.rnd.nextFloat() * 0.5F, 2.0F + this.rnd.nextFloat() * 3.0F, c
               }
            );
      }
   }

   @Override
   protected void update(float dt) {
      this.fresh = false;
      this.phaseT += dt;

      for (float[] p : this.parts) {
         p[0] += p[2] * dt;
         p[1] += p[3] * dt;
         p[3] += 380.0F * dt;
         p[2] *= 0.985F;
         p[4] += dt;
      }

      this.parts.removeIf(px -> px[4] >= px[5]);
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

   private static int[] rating(int ms) {
      if (ms < 180) {
         return new int[]{0, -15043};
      } else if (ms < 230) {
         return new int[]{1, -14756000};
      } else if (ms < 280) {
         return new int[]{2, -12597249};
      } else {
         return ms < 350 ? new int[]{3, -7859} : new int[]{4, -34235};
      }
   }

   @Override
   protected void render() {
      float mh = 208.0F;
      int tTop;
      int tBot;
      switch (this.phase) {
         case GO:
            tTop = -13705104;
            tBot = -15423412;
            break;
         case EARLY:
            tTop = -2065890;
            tBot = -6667768;
            break;
         case RESULT:
            int base = Style.light ? -14012352 : -15328732;
            tTop = Gx.mix(base, Style.accent, 0.22F);
            tBot = Gx.mix(base, -16777216, 0.25F);
            break;
         default:
            tTop = -10873310;
            tBot = -14022126;
      }

      float k = Math.min(1.0F, Gx.dt * 12.0F);
      if (this.state != GameView.State.PLAYING) {
         k = 1.0F;
      }

      this.curTop = Gx.mix(this.curTop, tTop, k);
      this.curBot = Gx.mix(this.curBot, tBot, k);
      this.boxV(0.0F, 0.0F, 360.0F, mh, 12.0F, this.curTop, this.curBot);
      this.boxV(0.0F, mh - 14.0F, 360.0F, 14.0F, 0.0F, Gx.mix(this.curTop, this.curBot, (mh - 14.0F) / mh), this.curBot);

      for (float y = 14.0F; y < mh; y += 20.0F) {
         for (float x = 14.0F; x < 360.0F; x += 20.0F) {
            this.circle(x, y + 0.6F, 3.6F, 335544320);
            this.circle(x, y, 3.4F, 218103807);
         }
      }

      float cx = 180.0F;
      float cy = mh / 2.0F - 6.0F;
      String roundTxt = "Runde " + Math.min(5, this.results.size() + (this.phase == Reaction.Phase.RESULT ? 0 : 1)) + " / 5";
      this.pill(roundTxt, cx, 16.0F, 855638016, -570425345);
      switch (this.phase) {
         case WAIT:
            float pulse = (float)(0.5 + 0.5 * Math.sin(this.time * 3.2));
            Gx.glow(this.X(cx), this.Y(cy - 22.0F), this.S(52.0F + pulse * 8.0F), Gx.withAlpha(-45730, 0.22F + pulse * 0.1F));

            for (int i = 0; i < 8; i++) {
               double a = this.time * 4.0F + i * Math.PI / 4.0;
               float a2 = 0.25F + 0.75F * (i / 7.0F);
               this.circle(cx + (float)Math.cos(a) * 17.0F, cy - 22.0F + (float)Math.sin(a) * 17.0F, 2.2F, Gx.withAlpha(-19526, a2));
            }

            Gx.icon("hourglass", this.X(cx), this.Y(cy - 22.0F), this.S(16.0F), -10534);
            this.text("Warte auf Grün…", cx, cy + 16.0F, 19.0F, 3, -1);
            this.text("Klick erst, wenn das Feld grün wird", cx, cy + 34.0F, 8.5F, 2, -1275068417);
            break;
         case GO: {
            float e = Ease.outBack(Math.min(1.0F, this.phaseT / 0.18F));

            for (int i = 0; i < 3; i++) {
               float rtx = (this.phaseT * 1.6F + i / 3.0F) % 1.0F;
               Gx.ringAt(
                  this.X(cx), this.Y(cy - 20.0F), this.S(14.0F + rtx * 70.0F), Math.max(1, this.S(2.2F * (1.0F - rtx))), Gx.withAlpha(-1, 0.5F * (1.0F - rtx))
               );
            }

            Gx.glow(this.X(cx), this.Y(cy - 20.0F), this.S(60.0F), 1442840575);
            Gx.push();
            Gx.scaleAt(this.X(cx), this.Y(cy), 0.6F + 0.4F * e);
            Gx.icon("bolt", this.X(cx), this.Y(cy - 20.0F), this.S(24.0F), -1);
            this.text("JETZT KLICKEN!", cx, cy + 20.0F, 26.0F, 3, -1);
            Gx.pop();
            break;
         }
         case EARLY: {
            float sh = (float)Math.sin(this.phaseT * 60.0F) * 5.0F * Math.max(0.0F, 1.0F - this.phaseT * 3.0F);
            float e = Ease.outBack(Math.min(1.0F, this.phaseT / 0.25F));
            Gx.push();
            Gx.translate(this.S(sh), 0.0F);
            Gx.scaleAt(this.X(cx), this.Y(cy), 0.7F + 0.3F * e);
            Gx.icon("warning", this.X(cx), this.Y(cy - 22.0F), this.S(24.0F), -1);
            this.text("Zu früh!", cx, cy + 16.0F, 24.0F, 3, -1);
            this.text("Warte auf Grün – die Runde wird wiederholt", cx, cy + 36.0F, 8.5F, 2, -855638017);
            Gx.pop();
            float f = Math.min(1.0F, this.phaseT / 1.0F);
            this.box(cx - 40.0F, cy + 50.0F, 80.0F, 3.0F, 1.5F, 1090519039);
            this.box(cx - 40.0F, cy + 50.0F, 80.0F * (1.0F - f), 3.0F, 1.5F, -1);
            break;
         }
         case RESULT: {
            int[] rt = rating(this.lastMs);
            float e = Ease.outBack(Math.min(1.0F, this.phaseT / 0.3F));
            float count = Ease.outCubic(Math.min(1.0F, this.phaseT / 0.35F));
            Gx.glow(this.X(cx), this.Y(cy - 8.0F), this.S(80.0F), Gx.withAlpha(rt[1], 0.22F));
            Gx.push();
            Gx.scaleAt(this.X(cx), this.Y(cy - 8.0F), 0.55F + 0.45F * e);
            int shown = Math.round(this.lastMs * count);
            float nw = Gx.width(String.valueOf(shown), this.S(38.0F), 3) / this.u;
            float uw = Gx.width(" ms", this.S(16.0F), 3) / this.u;
            float left = cx - (nw + uw) / 2.0F;
            Gx.textMid(String.valueOf(shown), this.X(left), this.Y(cy - 12.0F), this.S(38.0F), 3, -1);
            Gx.textMid(" ms", this.X(left + nw), this.Y(cy - 5.0F), this.S(16.0F), 3, -1275068417);
            Gx.pop();
            float e2 = Ease.outCubic(Math.min(1.0F, Math.max(0.0F, this.phaseT - 0.15F) / 0.3F));
            Gx.pushAlpha(e2);
            String r = RATING[rt[0]];
            float rw = Gx.width(r, this.S(10.0F), 3) / this.u + 22.0F;
            this.box(cx - rw / 2.0F, cy + 18.0F + (1.0F - e2) * 6.0F, rw, 18.0F, 9.0F, Gx.withAlpha(rt[1], 0.2F));
            this.text(r, cx, cy + 27.0F + (1.0F - e2) * 6.0F, 10.0F, 3, rt[1]);
            String hint = this.results.size() >= 5 ? "Auswertung…" : "Klick für die nächste Runde";
            this.text(hint, cx, cy + 50.0F, 7.5F, 2, -1711276033);
            Gx.popAlpha();
         }
      }

      for (float[] p : this.parts) {
         float a = 1.0F - p[4] / p[5];
         this.box(p[0] - p[6] / 2.0F, p[1] - p[6] / 2.0F, p[6], p[6], p[6] * 0.25F, Gx.withAlpha((int)p[7], a));
      }

      int stripBg = Style.light ? -2236185 : -15658216;
      this.box(0.0F, mh, 360.0F, 52.0F, 12.0F, stripBg);
      this.box(0.0F, mh, 360.0F, 14.0F, 0.0F, stripBg);
      this.box(0.0F, mh, 360.0F, 0.8F, 0.0F, Style.stroke);
      float chipW = 46.0F;
      float chipH = 32.0F;
      float gap = 6.0F;
      float x0 = 12.0F;
      float y0 = mh + (52.0F - chipH) / 2.0F;

      for (int i = 0; i < 5; i++) {
         float x = x0 + i * (chipW + gap);
         boolean done = i < this.results.size();
         boolean cur = i == this.results.size() && this.state == GameView.State.PLAYING;
         if (done) {
            int ms = this.results.get(i);
            int c = rating(ms)[1];
            float pop = i == this.results.size() - 1 && this.phase == Reaction.Phase.RESULT ? Ease.outBack(Math.min(1.0F, this.phaseT / 0.3F)) : 1.0F;
            Gx.push();
            Gx.scaleAt(this.X(x + chipW / 2.0F), this.Y(y0 + chipH / 2.0F), pop);
            this.box(x, y0, chipW, chipH, 8.0F, Gx.withAlpha(c, 0.16F));
            Gx.outline(this.X(x), this.Y(y0), this.S(chipW), this.S(chipH), this.S(8.0F), Math.max(1, this.S(0.7F)), Gx.withAlpha(c, 0.55F));
            this.text(ms + "", x + chipW / 2.0F, y0 + 13.0F, 10.0F, 3, Style.text);
            this.text("ms", x + chipW / 2.0F, y0 + 24.0F, 6.0F, 2, c);
            Gx.pop();
         } else {
            this.box(x, y0, chipW, chipH, 8.0F, Style.light ? -1117964 : -15065820);
            if (cur) {
               float pulse = (float)(0.5 + 0.5 * Math.sin(this.time * 5.0F));
               Gx.outline(
                  this.X(x), this.Y(y0), this.S(chipW), this.S(chipH), this.S(8.0F), Math.max(1, this.S(0.9F)), Gx.withAlpha(Style.accent, 0.5F + 0.5F * pulse)
               );
            }

            this.text(String.valueOf(i + 1), x + chipW / 2.0F, y0 + chipH / 2.0F, 9.0F, 3, cur ? Style.accent : Style.muted);
         }
      }

      float ax = x0 + 5.0F * (chipW + gap) + 2.0F;
      float aw = 348.0F - ax;
      this.box(ax, y0, aw, chipH, 8.0F, Gx.withAlpha(Style.accent, 0.14F));
      this.text("SCHNITT", ax + aw / 2.0F, y0 + 9.0F, 5.8F, 3, Style.accent);
      this.text(this.results.isEmpty() ? "-" : this.average() + " ms", ax + aw / 2.0F, y0 + 22.0F, 10.0F, 3, Style.text);
   }

   private void pill(String s, float cx, float cy, int bg, int fg) {
      float w = Gx.width(s, this.S(7.5F), 3) / this.u + 18.0F;
      this.box(cx - w / 2.0F, cy - 8.0F, w, 16.0F, 8.0F, bg);
      this.text(s, cx, cy, 7.5F, 3, fg);
   }

   private static enum Phase {
      WAIT,
      GO,
      EARLY,
      RESULT;
   }
}
