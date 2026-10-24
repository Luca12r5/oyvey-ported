package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.Arrays;
import java.util.Locale;

final class CookieClicker extends GameView {
   private static final String[] NAMES = new String[]{"Klick-Kraft", "Oma", "Bäckerei", "Fabrik", "Lego-Roboter"};
   private static final double[] BASE = new double[]{15.0, 50.0, 400.0, 2500.0, 12000.0};
   private static final double[] CPS = new double[]{0.0, 1.0, 8.0, 50.0, 300.0};
   private final int[] owned = new int[5];
   private double cookies;
   private double total;
   private float left;
   private float pop;

   CookieClicker() {
      super("cookies");
   }

   @Override
   protected float boardW() {
      return 420.0F;
   }

   @Override
   protected float boardH() {
      return 280.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Kekse";
   }

   @Override
   protected String overTitle() {
      return "Zeit um! " + fmt(this.total) + " Kekse gebacken";
   }

   @Override
   protected void reset() {
      Arrays.fill(this.owned, 0);
      this.cookies = this.total = 0.0;
      this.left = 120.0F;
   }

   private double price(int i) {
      return Math.floor(BASE[i] * Math.pow(1.15, this.owned[i]));
   }

   private double perClick() {
      return 1 + this.owned[0];
   }

   private double cps() {
      double s = 0.0;

      for (int i = 1; i < 5; i++) {
         s += this.owned[i] * CPS[i];
      }

      return s;
   }

   static String fmt(double d) {
      return d >= 1000000.0 ? String.format(Locale.ROOT, "%.2f Mio", d / 1000000.0) : String.valueOf((long)d);
   }

   private void add(double d) {
      this.cookies += d;
      this.total += d;
      this.score = (long)this.total;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (Math.hypot(x - 110.0F, y - 140.0F) < 70.0) {
         this.add(this.perClick());
         this.pop = 0.15F;
         Sound.play("minecraft:entity.generic.eat", 1.5F, 0.15F);
      } else {
         for (int i = 0; i < 5; i++) {
            if (in(x, y, 240, 20 + i * 50, 165, 44) && this.cookies >= this.price(i)) {
               this.cookies = this.cookies - this.price(i);
               this.owned[i]++;
               Sound.play("minecraft:entity.experience_orb.pickup", 1.2F, 0.3F);
            }
         }
      }
   }

   @Override
   protected void update(float dt) {
      this.add(this.cps() * dt);
      this.pop = Math.max(0.0F, this.pop - dt);
      this.left -= dt;
      if (this.left <= 0.0F) {
         this.left = 0.0F;
         this.gameOver();
      }
   }

   @Override
   protected void render() {
      float s = 1.0F + this.pop * 0.6F;
      this.circle(110.0F, 140.0F, 64.0F * s, -3635398);
      this.circle(110.0F, 140.0F, 58.0F * s, -2054056);

      for (int i = 0; i < 9; i++) {
         this.circle(
            110.0F + (float)Math.cos(i * 2.1) * 34.0F * s * (i % 3 == 0 ? 0.4F : 1.0F),
            140.0F + (float)Math.sin(i * 2.1) * 34.0F * s * (i % 3 == 0 ? 0.4F : 1.0F),
            6.0F * s,
            -11916780
         );
      }

      this.text(fmt(this.cookies) + " Kekse", 110.0F, 30.0F, 12.0F, 3, Style.text);
      this.text(fmt(this.cps()) + " pro Sekunde", 110.0F, 50.0F, 7.0F, 2, Style.sub);
      this.text(String.format(Locale.ROOT, "%.0f s", this.left), 110.0F, 250.0F, 9.0F, 3, this.left < 15.0F ? Style.danger : Style.sub);

      for (int i = 0; i < 5; i++) {
         boolean can = this.cookies >= this.price(i);
         this.box(
            240.0F,
            20 + i * 50,
            165.0F,
            44.0F,
            8.0F,
            can ? (in(this.bmx(), this.bmy(), 240, 20 + i * 50, 165, 44) ? Style.surfaceHover : Style.surface) : Gx.withAlpha(Style.surface, 0.5F)
         );
         this.textL(NAMES[i] + " (" + this.owned[i] + ")", 248.0F, 34 + i * 50, 7.5F, 3, can ? Style.text : Style.muted);
         this.textL(
            fmt(this.price(i)) + " Kekse · " + (i == 0 ? "+1 pro Klick" : "+" + (int)CPS[i] + "/s"), 248.0F, 50 + i * 50, 6.2F, 2, can ? Style.ok : Style.muted
         );
      }
   }
}
