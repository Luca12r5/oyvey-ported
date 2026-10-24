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
      for (int i = 0; i < 16; i++) {
         this.kind[i] = i / 2;
      }

      for (int i = 15; i > 0; i--) {
         int j = this.rnd.nextInt(i + 1);
         int t = this.kind[i];
         this.kind[i] = this.kind[j];
         this.kind[j] = t;
      }

      for (int i = 0; i < 16; i++) {
         this.up[i] = this.done[i] = false;
         this.flip[i] = 0.0F;
         this.doneAt[i] = 0.0F;
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
   protected void update(float dt) {
      if (this.second >= 0) {
         this.wrongT -= dt;
         if (this.wrongT <= 0.0F) {
            this.flipBack();
         }
      }

      if (this.pairs == 8) {
         this.endT += dt;
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

   private int cardAt(float x, float y) {
      int c = (int)Math.floor((x - 14.0F) / 72.0F);
      int r = (int)Math.floor((y - 14.0F) / 72.0F);
      if (c >= 0 && r >= 0 && c <= 3 && r <= 3) {
         return !(x - 14.0F - c * 72.0F > 64.0F) && !(y - 14.0F - r * 72.0F > 64.0F) ? r * 4 + c : -1;
      } else {
         return -1;
      }
   }

   @Override
   protected void onClick(float x, float y, int button) {
      if (button == 0 && this.pairs != 8) {
         int i = this.cardAt(x, y);
         if (i >= 0 && !this.up[i] && !this.done[i]) {
            if (this.second >= 0) {
               this.flipBack();
            }

            this.up[i] = true;
            Sound.play("minecraft:item.book.page_turn", 1.3F, 0.5F);
            if (this.first < 0) {
               this.first = i;
            } else {
               this.second = i;
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
      float k = 1.0F - (float)Math.exp(-Gx.dt * 14.0F);
      float fk = Gx.dt / 0.26F;
      float hx = (this.mx - this.bx) / this.u;
      float hy = (this.my - this.by) / this.u;
      int hover = this.state == GameView.State.PLAYING && this.pairs < 8 ? this.cardAt(hx, hy) : -1;

      for (int i = 0; i < 16; i++) {
         float target = !this.up[i] && !this.done[i] ? 0.0F : 1.0F;
         this.flip[i] = this.flip[i] < target ? Math.min(target, this.flip[i] + fk) : Math.max(target, this.flip[i] - fk);
         this.hoverA[i] = this.hoverA[i] + ((i == hover && !this.up[i] && !this.done[i] ? 1 : 0) - this.hoverA[i]) * k;
         float x = 14.0F + i % 4 * 72.0F;
         float y = 14.0F + i / 4 * 72.0F;
         float deal = Ease.outBack((this.clock - this.dealAt - i * 0.03F) / 0.35F);
         if (!(deal <= 0.0F)) {
            this.card(i, x, y, deal);
         }
      }

      this.panel();
   }

   private void card(int i, float x, float y, float deal) {
      float cx = x + 32.0F;
      float cy = y + 32.0F;
      float ang = (float)(Ease.inOutCubic(this.flip[i]) * Math.PI);
      float sx = Math.max(0.02F, Math.abs((float)Math.cos(ang)));
      float s = deal * (1.0F + 0.07F * (float)Math.sin(ang) + 0.04F * this.hoverA[i]);
      boolean front = this.flip[i] > 0.5F;
      float lift = 2.0F * this.hoverA[i] + 3.0F * (float)Math.sin(ang);
      float pop = this.done[i] ? Ease.clamp((this.clock - this.doneAt[i]) / 0.4F) : 0.0F;
      if (pop > 0.0F && pop < 1.0F) {
         s *= 1.0F + 0.1F * (float)Math.sin(pop * Math.PI);
      }

      Gx.push();
      Gx.translateExact(this.X(cx), this.Y(cy));
      Gx.B.scale(sx * s, s);
      Gx.translateExact(-this.X(cx), -this.Y(cy));
      this.box(x + 1.0F, y + 2.0F + lift * 0.5F, 62.0F, 63.0F, 12.0F, Style.light ? 469762048 : 1711276032);
      float yy = y - lift;
      if (front) {
         int col = COLORS[this.kind[i]];
         int face = Style.light ? -1 : -14934233;
         if (this.done[i]) {
            face = Gx.mix(face, col, 0.12F);
         }

         this.boxV(x, yy, 64.0F, 64.0F, 12.0F, Gx.mix(face, -1, Style.light ? 0.0F : 0.05F), face);
         Gx.outline(
            this.X(x),
            this.Y(yy),
            this.S(64.0F),
            this.S(64.0F),
            this.S(12.0F),
            Math.max(1, this.S(this.done[i] ? 1.3F : 0.8F)),
            Gx.withAlpha(col, this.done[i] ? 0.9F : 0.45F)
         );
         Gx.glow(this.X(cx), this.Y(yy + 32.0F), this.S(this.done[i] ? 38.0F : 30.0F), Gx.withAlpha(col, this.done[i] ? 0.4F : 0.25F));
         Gx.icon(ICONS[this.kind[i]], this.X(cx), this.Y(yy + 32.0F), this.S(30.0F), col);
         if (this.done[i] && pop > 0.0F) {
            float ca = Ease.outBack(pop);
            this.circle(x + 64.0F - 9.0F, yy + 9.0F, 5.2F * ca, col);
            Gx.icon("check", this.X(x + 64.0F - 9.0F), this.Y(yy + 9.0F), this.S(7.0F) * ca, -15658735);
         }
      } else {
         int acc = Gx.mix(Style.accent, -1, 0.14F * this.hoverA[i]);
         this.boxV(x, yy, 64.0F, 64.0F, 12.0F, Gx.mix(acc, -1, 0.16F), Gx.mix(acc, -16777216, 0.18F));
         Gx.outline(
            this.X(x), this.Y(yy), this.S(64.0F), this.S(64.0F), this.S(12.0F), Math.max(1, this.S(1.0F)), Gx.withAlpha(Gx.mix(acc, -16777216, 0.5F), 0.8F)
         );
         this.box(x + 4.0F, yy + 2.0F, 56.0F, 1.2F, 1.0F, Gx.withAlpha(-1, 0.25F));

         for (int s2 = 0; s2 < 4; s2++) {
            float stx = x + 64.0F * (s2 % 2 == 0 ? 0.3F : 0.7F);
            float sty = yy + 64.0F * (s2 < 2 ? 0.3F : 0.7F);
            this.circle(stx, sty + 1.3F, 8.0F, Gx.mix(acc, -16777216, 0.3F));
            this.circle(stx, sty, 8.0F, Gx.mix(acc, -1, 0.2F));
            this.circle(stx - 2.4F, sty - 2.4F, 2.2F, Gx.withAlpha(-1, 0.45F));
         }
      }

      Gx.pop();
      if (this.done[i] && pop > 0.0F && pop < 1.0F) {
         Gx.ringAt(
            this.X(cx),
            this.Y(cy),
            this.S(32.0F + 16.0F * Ease.outCubic(pop)),
            Math.max(1, this.S(2.0F * (1.0F - pop))),
            Gx.withAlpha(COLORS[this.kind[i]], 0.8F * (1.0F - pop))
         );
      }
   }

   private void panel() {
      float y = 14.0F;
      this.stat(y, "ZÜGE", String.valueOf(this.moves), -1.0F);
      this.stat(y + 66.0F, "PAARE", this.pairs + " / 8", this.pairs / 8.0F);
      int s = (int)(this.pairs == 8 ? this.finishSecs : this.time);
      this.stat(y + 132.0F, "ZEIT", String.format("%d:%02d", s / 60, s % 60), -1.0F);
      float gy = y + 202.0F;
      float cell = 20.0F;

      for (int i = 0; i < 8; i++) {
         boolean found = false;

         for (int j = 0; j < 16; j++) {
            if (this.done[j] && this.kind[j] == i) {
               found = true;
               break;
            }
         }

         float cx = 314.0F + cell * (i % 4) + cell / 2.0F;
         float cy = gy + i / 4 * 22 + 10.0F;
         this.box(cx - 9.0F, cy - 9.0F, 18.0F, 18.0F, 6.0F, found ? Gx.withAlpha(COLORS[i], 0.16F) : Style.surface);
         Gx.icon(ICONS[i], this.X(cx), this.Y(cy), this.S(10.0F), found ? COLORS[i] : Gx.withAlpha(Style.muted, 0.5F));
      }

      String hint = this.pairs == 8 ? "Alle Paare gefunden!" : (this.second >= 0 ? "Kein Paar" : (this.first >= 0 ? "Noch eine Karte" : "Wähle eine Karte"));
      this.text(hint, 354.0F, gy + 60.0F, 6.6F, 2, this.pairs == 8 ? Style.ok : (this.second >= 0 ? Style.danger : Style.sub));
   }

   private void stat(float y, String label, String value, float progress) {
      float h = 58.0F;
      this.box(314.0F, y, 80.0F, h, 12.0F, Style.surface);
      this.text(label, 354.0F, y + 15.0F, 5.8F, 3, Style.muted);
      this.text(value, 354.0F, y + 34.0F, 13.0F, 3, Style.text);
      if (progress >= 0.0F) {
         float bw = 60.0F;
         this.box(324.0F, y + h - 10.0F, bw, 3.0F, 1.5F, Style.surface2);
         if (progress > 0.0F) {
            this.box(324.0F, y + h - 10.0F, bw * progress, 3.0F, 1.5F, Style.accent);
         }
      }
   }
}
