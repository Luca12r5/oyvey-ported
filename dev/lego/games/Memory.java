package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

public final class Memory extends GameView {
   private static final String[] ICONS = new String[]{"heart-fill", "star-fill", "bolt", "music", "crown", "gem", "flame", "snow"};
   private static final int[] COLORS = new int[]{-45715, -13053, -12747777, -4883457, -36939, -13703779, -34278, -8397825};
   private static final int PAIRS = 8;
   private static final int N = 16;
   private static final float CARD = 64.0F;
   private static final float GAP = 8.0F;
   private static final float GX = 14.0F;
   private static final float GY = 14.0F;
   private static final float PX = 314.0F;
   private static final float PW = 80.0F;
   private final int[] kind = new int[16];
   private final boolean[] up = new boolean[16];
   private final boolean[] done = new boolean[16];
   private final float[] flip = new float[16];
   private final float[] hoverA = new float[16];
   private final float[] doneAt = new float[16];
   private int first = -1;
   private int second = -1;
   private int moves;
   private int pairs;
   private float wrongT;
   private float endT;
   private float clock;
   private float dealAt;
   private float finishSecs;

   public Memory() {
      super("memory");
   }

   @Override
   protected float boardW() {
      return 408.0F;
   }

   @Override
   protected float boardH() {
      return 308.0F;
   }

   @Override
   protected void reset() {
      for (int var1 = 0; var1 < 16; var1++) {
         this.kind[var1] = var1 / 2;
      }

      for (int var4 = 15; var4 > 0; var4--) {
         int var2 = this.rnd.nextInt(var4 + 1);
         int var3 = this.kind[var4];
         this.kind[var4] = this.kind[var2];
         this.kind[var2] = var3;
      }

      for (int var5 = 0; var5 < 16; var5++) {
         this.up[var5] = this.done[var5] = false;
         this.flip[var5] = 0.0F;
         this.doneAt[var5] = 0.0F;
      }

      this.first = this.second = -1;
      this.moves = this.pairs = 0;
      this.wrongT = this.endT = this.finishSecs = 0.0F;
      this.dealAt = this.clock;
   }

   @Override
   protected String overTitle() {
      return "Geschafft!";
   }

   @Override
   protected void update(float var1) {
      if (this.second >= 0) {
         this.wrongT -= var1;
         if (this.wrongT <= 0.0F) {
            this.flipBack();
         }
      }

      if (this.pairs == 8) {
         this.endT += var1;
         if (this.endT > 1.1F) {
            this.gameOver();
         }
      }
   }

   private void flipBack() {
      if (this.first >= 0) {
         this.up[this.first] = false;
      }

      if (this.second >= 0) {
         this.up[this.second] = false;
      }

      this.first = this.second = -1;
      Sound.play("minecraft:item.book.page_turn", 0.9F, 0.4F);
   }

   private int cardAt(float var1, float var2) {
      int var3 = (int)Math.floor((var1 - 14.0F) / 72.0F);
      int var4 = (int)Math.floor((var2 - 14.0F) / 72.0F);
      if (var3 >= 0 && var4 >= 0 && var3 <= 3 && var4 <= 3) {
         return !(var1 - 14.0F - var3 * 72.0F > 64.0F) && !(var2 - 14.0F - var4 * 72.0F > 64.0F) ? var4 * 4 + var3 : -1;
      } else {
         return -1;
      }
   }

   @Override
   protected void onClick(float var1, float var2, int var3) {
      if (var3 == 0 && this.pairs != 8) {
         int var4 = this.cardAt(var1, var2);
         if (var4 >= 0 && !this.up[var4] && !this.done[var4]) {
            if (this.second >= 0) {
               this.flipBack();
            }

            this.up[var4] = true;
            Sound.play("minecraft:item.book.page_turn", 1.3F, 0.5F);
            if (this.first < 0) {
               this.first = var4;
            } else {
               this.second = var4;
               this.moves++;
               if (this.kind[this.first] == this.kind[this.second]) {
                  this.done[this.first] = this.done[this.second] = true;
                  this.doneAt[this.first] = this.doneAt[this.second] = this.clock + 0.18F;
                  this.pairs++;
                  this.first = this.second = -1;
                  Sound.play("minecraft:entity.experience_orb.pickup", 0.9F + this.pairs * 0.08F, 0.4F);
                  if (this.pairs == 8) {
                     this.finishSecs = this.time;
                     this.score = Math.max(10L, 500L - this.moves * 15L - (long)Math.floor(this.finishSecs));
                     Sound.play("minecraft:entity.player.levelup", 1.2F, 0.4F);
                  }
               } else {
                  this.wrongT = 0.7F;
               }
            }
         }
      }
   }

   @Override
   protected void render() {
      this.clock = this.clock + Gx.dt;
      float var1 = 1.0F - (float)Math.exp(-Gx.dt * 14.0F);
      float var2 = Gx.dt / 0.26F;
      float var3 = (this.mx - this.bx) / this.u;
      float var4 = (this.my - this.by) / this.u;
      int var5 = this.state == GameView.State.PLAYING && this.pairs < 8 ? this.cardAt(var3, var4) : -1;

      for (int var6 = 0; var6 < 16; var6++) {
         float var7 = !this.up[var6] && !this.done[var6] ? 0.0F : 1.0F;
         this.flip[var6] = this.flip[var6] < var7 ? Math.min(var7, this.flip[var6] + var2) : Math.max(var7, this.flip[var6] - var2);
         this.hoverA[var6] = this.hoverA[var6] + ((var6 == var5 && !this.up[var6] && !this.done[var6] ? 1 : 0) - this.hoverA[var6]) * var1;
         float var8 = 14.0F + var6 % 4 * 72.0F;
         float var9 = 14.0F + var6 / 4 * 72.0F;
         float var10 = Ease.outBack((this.clock - this.dealAt - var6 * 0.03F) / 0.35F);
         if (!(var10 <= 0.0F)) {
            this.card(var6, var8, var9, var10);
         }
      }

      this.panel();
   }

   private void card(int var1, float var2, float var3, float var4) {
      float var5 = var2 + 32.0F;
      float var6 = var3 + 32.0F;
      float var7 = (float)(Ease.inOutCubic(this.flip[var1]) * Math.PI);
      float var8 = Math.max(0.02F, Math.abs((float)Math.cos(var7)));
      float var9 = var4 * (1.0F + 0.07F * (float)Math.sin(var7) + 0.04F * this.hoverA[var1]);
      boolean var10 = this.flip[var1] > 0.5F;
      float var11 = 2.0F * this.hoverA[var1] + 3.0F * (float)Math.sin(var7);
      float var12 = this.done[var1] ? Ease.clamp((this.clock - this.doneAt[var1]) / 0.4F) : 0.0F;
      if (var12 > 0.0F && var12 < 1.0F) {
         var9 *= 1.0F + 0.1F * (float)Math.sin(var12 * Math.PI);
      }

      Gx.push();
      Gx.translateExact(this.X(var5), this.Y(var6));
      Gx.B.scale(var8 * var9, var9);
      Gx.translateExact(-this.X(var5), -this.Y(var6));
      this.box(var2 + 1.0F, var3 + 2.0F + var11 * 0.5F, 62.0F, 63.0F, 12.0F, Style.light ? 469762048 : 1711276032);
      float var13 = var3 - var11;
      if (var10) {
         int var14 = COLORS[this.kind[var1]];
         int var15 = Style.light ? -1 : -14934233;
         if (this.done[var1]) {
            var15 = Gx.mix(var15, var14, 0.12F);
         }

         this.boxV(var2, var13, 64.0F, 64.0F, 12.0F, Gx.mix(var15, -1, Style.light ? 0.0F : 0.05F), var15);
         Gx.outline(
            this.X(var2),
            this.Y(var13),
            this.S(64.0F),
            this.S(64.0F),
            this.S(12.0F),
            Math.max(1, this.S(this.done[var1] ? 1.3F : 0.8F)),
            Gx.withAlpha(var14, this.done[var1] ? 0.9F : 0.45F)
         );
         Gx.glow(this.X(var5), this.Y(var13 + 32.0F), this.S(this.done[var1] ? 38.0F : 30.0F), Gx.withAlpha(var14, this.done[var1] ? 0.4F : 0.25F));
         Gx.icon(ICONS[this.kind[var1]], this.X(var5), this.Y(var13 + 32.0F), this.S(30.0F), var14);
         if (this.done[var1] && var12 > 0.0F) {
            float var16 = Ease.outBack(var12);
            this.circle(var2 + 64.0F - 9.0F, var13 + 9.0F, 5.2F * var16, var14);
            Gx.icon("check", this.X(var2 + 64.0F - 9.0F), this.Y(var13 + 9.0F), this.S(7.0F) * var16, -15658735);
         }
      } else {
         int var18 = Gx.mix(Style.accent, -1, 0.14F * this.hoverA[var1]);
         this.boxV(var2, var13, 64.0F, 64.0F, 12.0F, Gx.mix(var18, -1, 0.16F), Gx.mix(var18, -16777216, 0.18F));
         Gx.outline(
            this.X(var2),
            this.Y(var13),
            this.S(64.0F),
            this.S(64.0F),
            this.S(12.0F),
            Math.max(1, this.S(1.0F)),
            Gx.withAlpha(Gx.mix(var18, -16777216, 0.5F), 0.8F)
         );
         this.box(var2 + 4.0F, var13 + 2.0F, 56.0F, 1.2F, 1.0F, Gx.withAlpha(-1, 0.25F));

         for (int var19 = 0; var19 < 4; var19++) {
            float var20 = var2 + 64.0F * (var19 % 2 == 0 ? 0.3F : 0.7F);
            float var17 = var13 + 64.0F * (var19 < 2 ? 0.3F : 0.7F);
            this.circle(var20, var17 + 1.3F, 8.0F, Gx.mix(var18, -16777216, 0.3F));
            this.circle(var20, var17, 8.0F, Gx.mix(var18, -1, 0.2F));
            this.circle(var20 - 2.4F, var17 - 2.4F, 2.2F, Gx.withAlpha(-1, 0.45F));
         }
      }

      Gx.pop();
      if (this.done[var1] && var12 > 0.0F && var12 < 1.0F) {
         Gx.ringAt(
            this.X(var5),
            this.Y(var6),
            this.S(32.0F + 16.0F * Ease.outCubic(var12)),
            Math.max(1, this.S(2.0F * (1.0F - var12))),
            Gx.withAlpha(COLORS[this.kind[var1]], 0.8F * (1.0F - var12))
         );
      }
   }

   private void panel() {
      float var1 = 14.0F;
      this.stat(var1, "ZÜGE", String.valueOf(this.moves), -1.0F);
      this.stat(var1 + 66.0F, "PAARE", this.pairs + " / 8", this.pairs / 8.0F);
      int var2 = (int)(this.pairs == 8 ? this.finishSecs : this.time);
      this.stat(var1 + 132.0F, "ZEIT", String.format("%d:%02d", var2 / 60, var2 % 60), -1.0F);
      float var3 = var1 + 202.0F;
      float var4 = 20.0F;

      for (int var5 = 0; var5 < 8; var5++) {
         boolean var6 = false;

         for (int var7 = 0; var7 < 16; var7++) {
            if (this.done[var7] && this.kind[var7] == var5) {
               var6 = true;
               break;
            }
         }

         float var10 = 314.0F + var4 * (var5 % 4) + var4 / 2.0F;
         float var8 = var3 + var5 / 4 * 22 + 10.0F;
         this.box(var10 - 9.0F, var8 - 9.0F, 18.0F, 18.0F, 6.0F, var6 ? Gx.withAlpha(COLORS[var5], 0.16F) : Style.surface);
         Gx.icon(ICONS[var5], this.X(var10), this.Y(var8), this.S(10.0F), var6 ? COLORS[var5] : Gx.withAlpha(Style.muted, 0.5F));
      }

      String var9 = this.pairs == 8 ? "Alle Paare gefunden!" : (this.second >= 0 ? "Kein Paar" : (this.first >= 0 ? "Noch eine Karte" : "Wähle eine Karte"));
      this.text(var9, 354.0F, var3 + 60.0F, 6.6F, 2, this.pairs == 8 ? Style.ok : (this.second >= 0 ? Style.danger : Style.sub));
   }

   private void stat(float var1, String var2, String var3, float var4) {
      float var5 = 58.0F;
      this.box(314.0F, var1, 80.0F, var5, 12.0F, Style.surface);
      this.text(var2, 354.0F, var1 + 15.0F, 5.8F, 3, Style.muted);
      this.text(var3, 354.0F, var1 + 34.0F, 13.0F, 3, Style.text);
      if (var4 >= 0.0F) {
         float var6 = 60.0F;
         this.box(324.0F, var1 + var5 - 10.0F, var6, 3.0F, 1.5F, Style.surface2);
         if (var4 > 0.0F) {
            this.box(324.0F, var1 + var5 - 10.0F, var6 * var4, 3.0F, 1.5F, Style.accent);
         }
      }
   }
}
