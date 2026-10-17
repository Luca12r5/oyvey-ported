package dev.lego.ui;

/**
 * Short start animation on the first title screen (about 3 s): LEGO bricks drop
 * into the letters L-E-G-O, snap with a small bounce, a pumpkin glow passes
 * behind them and "CLIENT" fades in. Click or Esc skips. Disabled with
 * -Dlegoclient.intro=0 (the launcher passes it when its own intro is off).
 */
public final class StartupView extends View {
   private static final String[][] FONT = {
      {"#....", "#....", "#....", "#....", "#####"},
      {"#####", "#....", "####.", "#....", "#####"},
      {".####", "#....", "#.###", "#...#", ".###."},
      {".###.", "#...#", "#...#", "#...#", ".###."}
   };
   private static final int[] COLORS = {0xFFC91A1A, 0xFFF2B632, 0xFFC91A1A, 0xFFF2B632};
   private static final float TOTAL = 3.3F;
   private final long start = System.nanoTime();
   private final float[] delay;
   private final int[][] cells;
   private int lastLanded = 0;

   public StartupView() {
      int n = 0;
      for (String[] letter : FONT) {
         for (String row : letter) {
            for (char c : row.toCharArray()) {
               if (c == '#') n++;
            }
         }
      }
      this.cells = new int[n][3];
      this.delay = new float[n];
      int i = 0;
      for (int l = 0; l < FONT.length; l++) {
         for (int y = FONT[l].length - 1; y >= 0; y--) {
            String row = FONT[l][y];
            for (int x = 0; x < row.length(); x++) {
               if (row.charAt(x) == '#') {
                  this.cells[i][0] = l * 6 + x;
                  this.cells[i][1] = y;
                  this.cells[i][2] = l;
                  // Bottom rows first, letter by letter, with a little jitter.
                  this.delay[i] = 0.15F + l * 0.32F + (4 - y) * 0.07F + (float)((x * 37 + y * 11) % 7) * 0.012F;
                  i++;
               }
            }
         }
      }
   }

   public static boolean enabled() {
      return !"0".equals(System.getProperty("legoclient.intro"));
   }

   @Override
   public float openMs() {
      return 1.0F;
   }

   @Override
   public float closeMs() {
      return 260.0F;
   }

   @Override
   public boolean blur() {
      return false;
   }

   @Override
   public float designW() {
      return 640.0F;
   }

   @Override
   public float designH() {
      return 360.0F;
   }

   private float time() {
      return (System.nanoTime() - this.start) / 1.0E9F;
   }

   private static float clamp(float v) {
      return Math.max(0.0F, Math.min(1.0F, v));
   }

   private static float outBack(float t) {
      float c = 1.7F;
      float u = t - 1.0F;
      return 1.0F + (c + 1.0F) * u * u * u + c * u * u;
   }

   @Override
   public void draw() {
      float t = this.time();
      if (t > TOTAL && this.host != null && !this.host.closing()) {
         this.host.close();
      }
      float fadeOut = 1.0F - clamp((t - (TOTAL - 0.35F)) / 0.35F);
      Gx.fill(0, 0, this.W, this.H, Gx.rgba(0x050308, fadeOut));
      Gx.pushAlpha(fadeOut);
      int cols = 23;
      float cell = Math.min(this.W * 0.62F / cols, this.H * 0.3F / 5.0F);
      float ox = (this.W - cols * cell) / 2.0F;
      float oy = this.H * 0.42F - 2.5F * cell;
      // Pumpkin glow sweeping behind the logo once it stands.
      float glow = clamp((t - 1.7F) / 0.4F) * (1.0F - clamp((t - 2.7F) / 0.5F) * 0.6F);
      if (glow > 0.0F) {
         Gx.glow(Math.round(this.W / 2.0F), Math.round(oy + 2.5F * cell), Math.round(cols * cell * 0.7F), Gx.withAlpha(0xFFFF7A1A, 0.35F * glow));
      }
      int landed = 0;
      for (int i = 0; i < this.cells.length; i++) {
         float k = clamp((t - this.delay[i]) / 0.38F);
         if (k <= 0.0F) {
            continue;
         }
         if (k >= 1.0F) {
            landed++;
         }
         float e = outBack(k);
         float x = ox + this.cells[i][0] * cell;
         float yTarget = oy + this.cells[i][1] * cell;
         float y = yTarget - (1.0F - e) * this.H * 0.7F;
         int color = COLORS[this.cells[i][2]];
         int bx = Math.round(x + cell * 0.04F);
         int by = Math.round(y + cell * 0.04F);
         int bs = Math.max(2, Math.round(cell * 0.92F));
         // Brick body with darker bottom edge and a stud on top.
         Gx.fill(bx, by, bs, bs, color);
         Gx.fill(bx, by + bs - Math.max(1, bs / 6), bs, Math.max(1, bs / 6), Gx.mix(color, 0xFF000000, 0.35F));
         Gx.fill(bx, by, bs, Math.max(1, bs / 10), Gx.mix(color, 0xFFFFFFFF, 0.25F));
         int sw = Math.max(2, Math.round(bs * 0.5F));
         int sh = Math.max(1, Math.round(bs * 0.16F));
         Gx.fill(bx + (bs - sw) / 2, by - sh, sw, sh, Gx.mix(color, 0xFFFFFFFF, 0.12F));
      }
      if (landed > this.lastLanded) {
         this.lastLanded = landed;
         if (landed % 3 == 0) {
            Sound.click();
         }
      }
      float sub = clamp((t - 2.0F) / 0.4F);
      if (sub > 0.0F) {
         Gx.pushAlpha(sub);
         Gx.textCenter("C L I E N T", this.W / 2.0F, oy + 6.4F * cell, Math.max(8.0F, cell * 0.9F), 3, 0xFFFFD9A8);
         Gx.popAlpha();
      }
      Gx.textRight("Klick = überspringen", this.W - this.pf(14.0), this.H - this.pf(12.0), this.pf(6.5F), 1, Gx.withAlpha(0xFFFFFFFF, 0.35F));
      Gx.popAlpha();
   }

   @Override
   public boolean mouseDown(float x, float y, int button, boolean dbl) {
      if (this.host != null) {
         this.host.close();
      }
      return true;
   }
}
