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

      for (int var1 = 0; var1 < 4; var1++) {
         this.litT[var1] = 0.0F;
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

   private void light(int var1, float var2) {
      this.litT[var1] = var2;
      this.press[var1] = 1.0F;
      Sound.play("minecraft:block.note_block.harp", PITCH[var1], 0.5F);
      Sound.play("minecraft:block.note_block.bell", PITCH[var1], 0.12F);
   }

   @Override
   protected void update(float var1) {
      for (int var2 = 0; var2 < 4; var2++) {
         this.litT[var2] = Math.max(0.0F, this.litT[var2] - var1);
      }

      this.phaseT += var1;
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

   private void pressPad(int var1) {
      if (this.phase == Simon.Phase.INPUT) {
         this.light(var1, 0.25F);
         if (this.seq.get(this.inIdx) == var1) {
            this.inIdx++;
            if (this.inIdx >= this.seq.size()) {
               this.score = this.seq.size();
               this.phase = Simon.Phase.SUCCESS;
               this.phaseT = 0.0F;
               this.flash = 1.0F;
               Sound.play("minecraft:entity.experience_orb.pickup", 1.0F + Math.min(0.8F, this.seq.size() * 0.04F), 0.35F);
            }
         } else {
            this.failPad = var1;
            this.phase = Simon.Phase.FAIL;
            this.phaseT = 0.0F;
            Sound.play("minecraft:block.note_block.didgeridoo", 0.6F, 0.6F);
         }
      }
   }

   @Override
   protected void onClick(float var1, float var2, int var3) {
      if (var3 == 0) {
         int var4 = this.padAt(var1, var2);
         if (var4 >= 0) {
            this.pressPad(var4);
         }
      }
   }

   @Override
   protected void onKey(int var1) {
      if (var1 >= 49 && var1 <= 52) {
         this.pressPad(var1 - 49);
      } else if (var1 >= 321 && var1 <= 324) {
         this.pressPad(var1 - 321);
      }
   }

   private int padAt(float var1, float var2) {
      for (int var3 = 0; var3 < 4; var3++) {
         float var4 = this.padX(var3);
         float var5 = this.padY(var3);
         if (var1 >= var4 && var2 >= var5 && var1 < var4 + 132.0F && var2 < var5 + 132.0F) {
            float var6 = var1 - 190.0F;
            float var7 = var2 - 150.0F;
            if (var6 * var6 + var7 * var7 < 1444.0F) {
               return -1;
            }

            return var3;
         }
      }

      return -1;
   }

   private float padX(int var1) {
      return 52.0F + var1 % 2 * 144.0F;
   }

   private float padY(int var1) {
      return 12.0F + var1 / 2 * 144.0F;
   }

   @Override
   protected void render() {
      float var1 = Gx.dt;
      this.clock += var1;
      float var2 = 1.0F - (float)Math.exp(-var1 * 16.0F);
      float var3 = (this.mx - this.bx) / this.u;
      float var4 = (this.my - this.by) / this.u;
      boolean var5 = this.state == GameView.State.PLAYING && this.phase == Simon.Phase.INPUT;
      int var6 = var5 ? this.padAt(var3, var4) : -1;
      this.flash = Math.max(0.0F, this.flash - var1 * 1.6F);

      for (int var7 = 0; var7 < 4; var7++) {
         this.glow[var7] = this.glow[var7]
            + ((this.litT[var7] > 0.0F ? 1 : 0) - this.glow[var7]) * (this.litT[var7] > 0.0F ? 1.0F - (float)Math.exp(-var1 * 30.0F) : var2 * 0.7F);
         this.hoverA[var7] = this.hoverA[var7] + ((var7 == var6 ? 1 : 0) - this.hoverA[var7]) * var2;
         this.press[var7] = Math.max(0.0F, this.press[var7] - var1 * 5.0F);
         this.pad(var7);
      }

      float var22 = 190.0F;
      float var8 = 150.0F;
      int var9 = Style.light ? -789257 : -16053232;
      this.circle(var22, var8, 40.0F, var9);
      int var10 = this.phase == Simon.Phase.FAIL ? Style.danger : (this.phase == Simon.Phase.SUCCESS ? Style.ok : Style.accent);
      if (this.flash > 0.0F) {
         Gx.glow(this.X(var22), this.Y(var8), this.S(70.0F), Gx.withAlpha(Style.ok, 0.5F * this.flash));
      }

      this.circle(var22, var8, 33.0F, Style.surface);
      float var11 = this.phase == Simon.Phase.INPUT
         ? (float)this.inIdx / this.seq.size()
         : (this.phase == Simon.Phase.SHOW ? (float)(this.idx + (this.stepLit ? 1 : 0)) / this.seq.size() : (this.phase == Simon.Phase.SUCCESS ? 1.0F : 0.0F));
      Gx.ringAt(this.X(var22), this.Y(var8), this.S(33.0F), Math.max(1, this.S(2.2F)), Style.surface2);
      this.arc(var22, var8, 31.9F, 2.2F, var11, var10);
      this.text("RUNDE", var22, var8 - 11.0F, 5.6F, 3, Style.muted);
      float var12 = 1.0F + 0.25F * this.flash;
      Gx.push();
      Gx.scaleAt(this.X(var22), this.Y(var8 + 5.0F), var12);
      this.text(String.valueOf(this.seq.size()), var22, var8 + 5.0F, 17.0F, 3, Style.text);
      Gx.pop();
      int var14 = Style.text;
      String var13;
      switch (this.phase) {
         case WAIT:
         case SHOW:
            var13 = "Merken…";
            var14 = Style.sub;
            break;
         case INPUT:
            var13 = "Du bist dran!";
            var14 = Style.accent;
            break;
         case SUCCESS:
            var13 = "Richtig!";
            var14 = Style.ok;
            break;
         default:
            var13 = "Falsch! Richtig war " + NAMES[this.seq.get(this.inIdx)];
            var14 = Style.danger;
      }

      float var15 = 305.0F;
      if (this.phase == Simon.Phase.INPUT) {
         float var16 = 1.0F + 0.04F * (float)Math.sin(this.clock * 5.0F);
         Gx.push();
         Gx.scaleAt(this.X(190.0F), this.Y(var15), var16);
         this.text(var13, 190.0F, var15, 10.0F, 3, var14);
         Gx.pop();
      } else {
         this.text(var13, 190.0F, var15, 10.0F, 3, var14);
      }

      int var24 = this.seq.size();
      if (var24 <= 24) {
         float var17 = 7.0F;
         float var18 = var24 * var17;

         for (int var19 = 0; var19 < var24; var19++) {
            float var20 = 190.0F - var18 / 2.0F + var17 * var19 + var17 / 2.0F;
            boolean var21 = this.phase == Simon.Phase.INPUT
               ? var19 < this.inIdx
               : (this.phase == Simon.Phase.SHOW ? var19 < this.idx + (this.stepLit ? 1 : 0) : this.phase == Simon.Phase.SUCCESS);
            this.circle(var20, var15 + 14.0F, 2.0F, var21 ? var10 : Style.surface2);
         }
      } else {
         this.text((this.phase == Simon.Phase.INPUT ? this.inIdx : 0) + " / " + var24, 190.0F, var15 + 14.0F, 6.5F, 2, Style.sub);
      }
   }

   private void pad(int var1) {
      float var2 = this.padX(var1);
      float var3 = this.padY(var1);
      int var4 = COLORS[var1];
      float var5 = this.glow[var1];
      boolean var6 = this.phase == Simon.Phase.FAIL && var1 == this.seq.get(this.inIdx) && (int)(this.phaseT * 6.0F) % 2 == 0;
      if (var6) {
         var5 = Math.max(var5, 0.8F);
      }

      boolean var7 = this.phase == Simon.Phase.FAIL && var1 == this.failPad;
      int var8 = Gx.mix(var4, Style.light ? -1446928 : -16053232, 0.55F - 0.12F * this.hoverA[var1]);
      int var9 = Gx.mix(var8, Gx.mix(var4, -1, 0.18F), var5);
      float var10 = 1.0F - 0.035F * Ease.outCubic(this.press[var1]) + 0.015F * this.hoverA[var1];
      float var11 = var7 ? (float)Math.sin(this.phaseT * 60.0F) * 3.0F * Math.max(0.0F, 1.0F - this.phaseT * 2.0F) : 0.0F;
      float var12 = var2 + 66.0F + var11;
      float var13 = var3 + 66.0F;
      if (var5 > 0.01F) {
         Gx.glow(this.X(var12), this.Y(var13), this.S(125.4F), Gx.withAlpha(var4, 0.55F * var5));
      }

      Gx.push();
      Gx.scaleAt(this.X(var12), this.Y(var13), var10);
      float var14 = var2 + var11;
      this.box(var14, var3 + 3.0F, 132.0F, 132.0F, 22.0F, Gx.mix(var9, -16777216, 0.5F));
      this.box(var14, var3, 132.0F, 132.0F, 22.0F, Gx.mix(var9, -16777216, 0.14F));
      this.box(var14 + 3.0F, var3 + 2.0F, 126.0F, 125.0F, 19.0F, Gx.mix(var9, -1, 0.06F));
      Gx.outline(
         this.X(var14),
         this.Y(var3),
         this.S(132.0F),
         this.S(132.0F),
         this.S(22.0F),
         Math.max(1, this.S(1.0F)),
         Gx.withAlpha(Gx.mix(var9, -16777216, 0.5F), 0.8F)
      );
      if (var7) {
         Gx.outline(this.X(var14), this.Y(var3), this.S(132.0F), this.S(132.0F), this.S(22.0F), Math.max(1, this.S(2.5F)), Style.danger);
      }

      for (int var15 = 0; var15 < 4; var15++) {
         float var16 = var14 + 132.0F * (var15 % 2 == 0 ? 0.3F : 0.7F);
         float var17 = var3 + 132.0F * (var15 < 2 ? 0.3F : 0.7F);
         this.circle(var16, var17 + 2.2F, 15.0F, Gx.mix(var9, -16777216, 0.3F));
         this.circle(var16, var17, 15.0F, Gx.mix(var9, -1, 0.14F));
         this.circle(var16 - 4.5F, var17 - 4.5F, 3.6F, Gx.withAlpha(-1, 0.2F + 0.35F * var5));
      }

      float var18 = var1 % 2 == 0 ? var14 + 16.0F : var14 + 132.0F - 16.0F;
      float var19 = var1 < 2 ? var3 + 16.0F : var3 + 132.0F - 16.0F;
      this.circle(var18, var19, 8.0F, Gx.withAlpha(-16777216, 0.25F));
      this.text(String.valueOf(var1 + 1), var18, var19, 7.5F, 3, Gx.withAlpha(-1, 0.75F));
      Gx.pop();
   }

   private void arc(float var1, float var2, float var3, float var4, float var5, int var6) {
      if (!(var5 <= 0.0F)) {
         if (var5 >= 1.0F) {
            Gx.ringAt(this.X(var1), this.Y(var2), this.S(var3 + var4 / 2.0F), Math.max(1, this.S(var4)), var6);
         } else {
            int var7 = Math.max(1, Math.round(var4 * this.u / 2.0F));
            float var8 = (float)((Math.PI * 2) * var5);
            int var9 = Math.max(2, (int)Math.ceil(var8 * var3 * this.u / Math.max(1.0F, var7 * 0.4F)));

            for (int var10 = 0; var10 <= var9; var10++) {
               double var11 = (-Math.PI / 2) + var8 * var10 / var9;
               Gx.circle(
                  Math.round(this.bx + (var1 + (float)Math.cos(var11) * var3) * this.u),
                  Math.round(this.by + (var2 + (float)Math.sin(var11) * var3) * this.u),
                  var7,
                  var6
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
