package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;

public final class Simon extends GameView {
   private static final int[] COLORS = new int[]{-16732339, -1900533, -13053, -16098364};
   private static final String[] NAMES = new String[]{"Grün", "Rot", "Gelb", "Blau"};
   private static final float[] PITCH = new float[]{0.707F, 0.891F, 1.059F, 1.335F};
   private static final float PAD = 132.0F;
   private static final float GAP = 12.0F;
   private static final float W = 380.0F;
   private static final float PY = 12.0F;
   private static final float PXL = 52.0F;
   private final List<Integer> seq = new ArrayList<>();
   private Simon.Phase phase = Simon.Phase.WAIT;
   private float phaseT;
   private float clock;
   private float flash;
   private int idx;
   private int inIdx;
   private int failPad = -1;
   private boolean stepLit;
   private final float[] litT = new float[4];
   private final float[] glow = new float[4];
   private final float[] hoverA = new float[4];
   private final float[] press = new float[4];

   public Simon() {
      super("simon");
   }

   @Override
   protected float boardW() {
      return 380.0F;
   }

   @Override
   protected float boardH() {
      return 328.0F;
   }

   @Override
   protected void reset() {
      this.seq.clear();
      this.seq.add(this.rnd.nextInt(4));
      this.phase = Simon.Phase.WAIT;
      this.phaseT = 0.0F;
      this.idx = this.inIdx = 0;
      this.failPad = -1;
      this.stepLit = false;
      this.flash = 0.0F;

      for (int i = 0; i < 4; i++) {
         this.litT[i] = 0.0F;
      }
   }

   @Override
   protected String overTitle() {
      return "Verloren";
   }

   @Override
   protected String scoreLabel() {
      return "Runden";
   }

   private float stepOn() {
      return Math.max(0.24F, 0.5F - this.seq.size() * 0.02F);
   }

   private float stepGap() {
      return Math.max(0.08F, 0.18F - this.seq.size() * 0.006F);
   }

   private void light(int pad, float dur) {
      this.litT[pad] = dur;
      this.press[pad] = 1.0F;
      Sound.play("minecraft:block.note_block.harp", PITCH[pad], 0.5F);
      Sound.play("minecraft:block.note_block.bell", PITCH[pad], 0.12F);
   }

   @Override
   protected void update(float dt) {
      for (int i = 0; i < 4; i++) {
         this.litT[i] = Math.max(0.0F, this.litT[i] - dt);
      }

      this.phaseT += dt;
      switch (this.phase) {
         case WAIT:
            if (this.phaseT > 0.8F) {
               this.phase = Simon.Phase.SHOW;
               this.phaseT = 0.0F;
               this.idx = 0;
               this.stepLit = false;
            }
            break;
         case SHOW:
            if (!this.stepLit) {
               this.light(this.seq.get(this.idx), this.stepOn());
               this.stepLit = true;
            }

            if (this.phaseT >= this.stepOn() + this.stepGap()) {
               this.idx++;
               this.phaseT = 0.0F;
               this.stepLit = false;
               if (this.idx >= this.seq.size()) {
                  this.phase = Simon.Phase.INPUT;
                  this.inIdx = 0;
               }
            }
         case INPUT:
         default:
            break;
         case SUCCESS:
            if (this.phaseT > 0.9F) {
               this.seq.add(this.rnd.nextInt(4));
               this.phase = Simon.Phase.WAIT;
               this.phaseT = 0.0F;
            }
            break;
         case FAIL:
            if (this.phaseT > 1.1F) {
               this.gameOver();
            }
      }
   }

   private void pressPad(int pad) {
      if (this.phase == Simon.Phase.INPUT) {
         this.light(pad, 0.25F);
         if (this.seq.get(this.inIdx) == pad) {
            this.inIdx++;
            if (this.inIdx >= this.seq.size()) {
               this.score = this.seq.size();
               this.phase = Simon.Phase.SUCCESS;
               this.phaseT = 0.0F;
               this.flash = 1.0F;
               Sound.play("minecraft:entity.experience_orb.pickup", 1.0F + Math.min(0.8F, this.seq.size() * 0.04F), 0.35F);
            }
         } else {
            this.failPad = pad;
            this.phase = Simon.Phase.FAIL;
            this.phaseT = 0.0F;
            Sound.play("minecraft:block.note_block.didgeridoo", 0.6F, 0.6F);
         }
      }
   }

   @Override
   protected void onClick(float x, float y, int button) {
      if (button == 0) {
         int p = this.padAt(x, y);
         if (p >= 0) {
            this.pressPad(p);
         }
      }
   }

   @Override
   protected void onKey(int key) {
      if (key >= 49 && key <= 52) {
         this.pressPad(key - 49);
      } else if (key >= 321 && key <= 324) {
         this.pressPad(key - 321);
      }
   }

   private int padAt(float x, float y) {
      for (int i = 0; i < 4; i++) {
         float px = this.padX(i);
         float py = this.padY(i);
         if (x >= px && y >= py && x < px + 132.0F && y < py + 132.0F) {
            float dx = x - 190.0F;
            float dy = y - 150.0F;
            if (dx * dx + dy * dy < 1444.0F) {
               return -1;
            }

            return i;
         }
      }

      return -1;
   }

   private float padX(int i) {
      return 52.0F + i % 2 * 144.0F;
   }

   private float padY(int i) {
      return 12.0F + i / 2 * 144.0F;
   }

   @Override
   protected void render() {
      float dt = Gx.dt;
      this.clock += dt;
      float k = 1.0F - (float)Math.exp(-dt * 16.0F);
      float hx = (this.mx - this.bx) / this.u;
      float hy = (this.my - this.by) / this.u;
      boolean canInput = this.state == GameView.State.PLAYING && this.phase == Simon.Phase.INPUT;
      int hover = canInput ? this.padAt(hx, hy) : -1;
      this.flash = Math.max(0.0F, this.flash - dt * 1.6F);

      for (int i = 0; i < 4; i++) {
         this.glow[i] = this.glow[i] + ((this.litT[i] > 0.0F ? 1 : 0) - this.glow[i]) * (this.litT[i] > 0.0F ? 1.0F - (float)Math.exp(-dt * 30.0F) : k * 0.7F);
         this.hoverA[i] = this.hoverA[i] + ((i == hover ? 1 : 0) - this.hoverA[i]) * k;
         this.press[i] = Math.max(0.0F, this.press[i] - dt * 5.0F);
         this.pad(i);
      }

      float hcx = 190.0F;
      float hcy = 150.0F;
      int hub = Style.light ? -789257 : -16053232;
      this.circle(hcx, hcy, 40.0F, hub);
      int ringCol = this.phase == Simon.Phase.FAIL ? Style.danger : (this.phase == Simon.Phase.SUCCESS ? Style.ok : Style.accent);
      if (this.flash > 0.0F) {
         Gx.glow(this.X(hcx), this.Y(hcy), this.S(70.0F), Gx.withAlpha(Style.ok, 0.5F * this.flash));
      }

      this.circle(hcx, hcy, 33.0F, Style.surface);
      float prog = this.phase == Simon.Phase.INPUT
         ? (float)this.inIdx / this.seq.size()
         : (this.phase == Simon.Phase.SHOW ? (float)(this.idx + (this.stepLit ? 1 : 0)) / this.seq.size() : (this.phase == Simon.Phase.SUCCESS ? 1.0F : 0.0F));
      Gx.ringAt(this.X(hcx), this.Y(hcy), this.S(33.0F), Math.max(1, this.S(2.2F)), Style.surface2);
      this.arc(hcx, hcy, 31.9F, 2.2F, prog, ringCol);
      this.text("RUNDE", hcx, hcy - 11.0F, 5.6F, 3, Style.muted);
      float bump = 1.0F + 0.25F * this.flash;
      Gx.push();
      Gx.scaleAt(this.X(hcx), this.Y(hcy + 5.0F), bump);
      this.text(String.valueOf(this.seq.size()), hcx, hcy + 5.0F, 17.0F, 3, Style.text);
      Gx.pop();
      int sc = Style.text;
      String st;
      switch (this.phase) {
         case WAIT:
         case SHOW:
            st = "Merken…";
            sc = Style.sub;
            break;
         case INPUT:
            st = "Du bist dran!";
            sc = Style.accent;
            break;
         case SUCCESS:
            st = "Richtig!";
            sc = Style.ok;
            break;
         default:
            st = "Falsch! Richtig war " + NAMES[this.seq.get(this.inIdx)];
            sc = Style.danger;
      }

      float sy = 305.0F;
      if (this.phase == Simon.Phase.INPUT) {
         float p = 1.0F + 0.04F * (float)Math.sin(this.clock * 5.0F);
         Gx.push();
         Gx.scaleAt(this.X(190.0F), this.Y(sy), p);
         this.text(st, 190.0F, sy, 10.0F, 3, sc);
         Gx.pop();
      } else {
         this.text(st, 190.0F, sy, 10.0F, 3, sc);
      }

      int n = this.seq.size();
      if (n <= 24) {
         float dw = 7.0F;
         float total = n * dw;

         for (int i = 0; i < n; i++) {
            float dx = 190.0F - total / 2.0F + dw * i + dw / 2.0F;
            boolean filled = this.phase == Simon.Phase.INPUT
               ? i < this.inIdx
               : (this.phase == Simon.Phase.SHOW ? i < this.idx + (this.stepLit ? 1 : 0) : this.phase == Simon.Phase.SUCCESS);
            this.circle(dx, sy + 14.0F, 2.0F, filled ? ringCol : Style.surface2);
         }
      } else {
         this.text((this.phase == Simon.Phase.INPUT ? this.inIdx : 0) + " / " + n, 190.0F, sy + 14.0F, 6.5F, 2, Style.sub);
      }
   }

   private void pad(int i) {
      float x = this.padX(i);
      float y = this.padY(i);
      int base = COLORS[i];
      float g = this.glow[i];
      boolean failHint = this.phase == Simon.Phase.FAIL && i == this.seq.get(this.inIdx) && (int)(this.phaseT * 6.0F) % 2 == 0;
      if (failHint) {
         g = Math.max(g, 0.8F);
      }

      boolean wrong = this.phase == Simon.Phase.FAIL && i == this.failPad;
      int dim = Gx.mix(base, Style.light ? -1446928 : -16053232, 0.55F - 0.12F * this.hoverA[i]);
      int col = Gx.mix(dim, Gx.mix(base, -1, 0.18F), g);
      float s = 1.0F - 0.035F * Ease.outCubic(this.press[i]) + 0.015F * this.hoverA[i];
      float shake = wrong ? (float)Math.sin(this.phaseT * 60.0F) * 3.0F * Math.max(0.0F, 1.0F - this.phaseT * 2.0F) : 0.0F;
      float cx = x + 66.0F + shake;
      float cy = y + 66.0F;
      if (g > 0.01F) {
         Gx.glow(this.X(cx), this.Y(cy), this.S(125.4F), Gx.withAlpha(base, 0.55F * g));
      }

      Gx.push();
      Gx.scaleAt(this.X(cx), this.Y(cy), s);
      float px = x + shake;
      this.box(px, y + 3.0F, 132.0F, 132.0F, 22.0F, Gx.mix(col, -16777216, 0.5F));
      this.box(px, y, 132.0F, 132.0F, 22.0F, Gx.mix(col, -16777216, 0.14F));
      this.box(px + 3.0F, y + 2.0F, 126.0F, 125.0F, 19.0F, Gx.mix(col, -1, 0.06F));
      Gx.outline(
         this.X(px), this.Y(y), this.S(132.0F), this.S(132.0F), this.S(22.0F), Math.max(1, this.S(1.0F)), Gx.withAlpha(Gx.mix(col, -16777216, 0.5F), 0.8F)
      );
      if (wrong) {
         Gx.outline(this.X(px), this.Y(y), this.S(132.0F), this.S(132.0F), this.S(22.0F), Math.max(1, this.S(2.5F)), Style.danger);
      }

      for (int k = 0; k < 4; k++) {
         float sx = px + 132.0F * (k % 2 == 0 ? 0.3F : 0.7F);
         float sy = y + 132.0F * (k < 2 ? 0.3F : 0.7F);
         this.circle(sx, sy + 2.2F, 15.0F, Gx.mix(col, -16777216, 0.3F));
         this.circle(sx, sy, 15.0F, Gx.mix(col, -1, 0.14F));
         this.circle(sx - 4.5F, sy - 4.5F, 3.6F, Gx.withAlpha(-1, 0.2F + 0.35F * g));
      }

      float lx = i % 2 == 0 ? px + 16.0F : px + 132.0F - 16.0F;
      float ly = i < 2 ? y + 16.0F : y + 132.0F - 16.0F;
      this.circle(lx, ly, 8.0F, Gx.withAlpha(-16777216, 0.25F));
      this.text(String.valueOf(i + 1), lx, ly, 7.5F, 3, Gx.withAlpha(-1, 0.75F));
      Gx.pop();
   }

   private void arc(float cx, float cy, float r, float th, float p, int col) {
      if (!(p <= 0.0F)) {
         if (p >= 1.0F) {
            Gx.ringAt(this.X(cx), this.Y(cy), this.S(r + th / 2.0F), Math.max(1, this.S(th)), col);
         } else {
            int rad = Math.max(1, Math.round(th * this.u / 2.0F));
            float a = (float)((Math.PI * 2) * p);
            int n = Math.max(2, (int)Math.ceil(a * r * this.u / Math.max(1.0F, rad * 0.4F)));

            for (int i = 0; i <= n; i++) {
               double ang = (-Math.PI / 2) + a * i / n;
               Gx.circle(
                  Math.round(this.bx + (cx + (float)Math.cos(ang) * r) * this.u), Math.round(this.by + (cy + (float)Math.sin(ang) * r) * this.u), rad, col
               );
            }
         }
      }
   }

   private static enum Phase {
      WAIT,
      SHOW,
      INPUT,
      SUCCESS,
      FAIL;
   }
}
