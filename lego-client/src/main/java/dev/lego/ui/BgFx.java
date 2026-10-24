package dev.lego.ui;

import dev.lego.core.Lang;
import java.awt.Color;

public final class BgFx {
   private static final String[] NAMES_DE = new String[]{
      "Keine", "Aurora", "Partikel", "Sterne", "Wellen", "Bokeh", "Synthwave", "Schnee", "Nebel", "Matrix", "Warp 3D", "Würfel 3D", "Bausteine 3D", "Tunnel 3D"
   };
   public static final int COUNT = NAMES_DE.length;
   public static String[] NAMES = NAMES_DE;

   private BgFx() {
   }

   public static void updateNames() {
      String[] n = new String[NAMES_DE.length];

      for (int i = 0; i < n.length; i++) {
         n[i] = Lang.get("bgfx." + i, NAMES_DE[i]);
      }

      NAMES = n;
   }

   private static float fract(double v) {
      return (float)(v - Math.floor(v));
   }

   private static float hash(int i, int k) {
      int h = i * 374761393 + k * 668265263;
      h = (h ^ h >>> 13) * 1274126177;
      return ((h ^ h >>> 16) & 16777215) / 1.6777215E7F;
   }

   public static void drawIn(int x, int y, int w, int h, float a) {
      if (UiSettings.bgStyle > 0) {
         Gx.clip(x, y, w, h);
         Gx.push();
         Gx.translate(x, y);
         draw(w, h, a);
         Gx.pop();
         Gx.unclip();
      }
   }

   public static void draw(int W, int H, float a) {
      int style = UiSettings.bgStyle;
      if (style > 0 && style < NAMES.length && !(a <= 0.01F)) {
         float k = (float)UiSettings.bgIntensity * a;
         double t = System.currentTimeMillis() / 1000.0;
         int acc = Style.accent & 16777215;
         int acc2 = Gx.mix(Style.accent, -9806593, 0.6F) & 16777215;
         int S = Math.max(W, H);
         switch (style) {
            case 1:
               for (int ixx = 0; ixx < 5; ixx++) {
                  float x = W * fract(0.13 + ixx * 0.21 + t * (0.01 + ixx * 0.004));
                  float y = H * (0.18F + 0.12F * (float)Math.sin(t * 0.3 + ixx * 1.7));
                  Gx.glow(Math.round(x), Math.round(y), Math.round(S * (0.32F + 0.06F * ixx)), Gx.rgba(ixx % 2 == 0 ? acc : acc2, 0.22F * k));
               }

               Gx.gradientV(0, 0, W, H / 2, Gx.rgba(acc2, 0.1F * k), 0);
               break;
            case 2:
               for (int ixx = 0; ixx < 90; ixx++) {
                  float x = W * hash(ixx, 1) + (float)Math.sin(t * 0.6 + ixx) * S * 0.01F;
                  float y = H * (1.0F - fract(hash(ixx, 2) + t * (0.02 + hash(ixx, 3) * 0.05)));
                  int r = Math.max(1, Math.round(S * (0.0015F + hash(ixx, 4) * 0.003F)));
                  Gx.circle(Math.round(x), Math.round(y), r, Gx.rgba(ixx % 3 == 0 ? acc : 16777215, (0.25F + 0.5F * hash(ixx, 5)) * k));
               }
               break;
            case 3:
               for (int ixx = 0; ixx < 140; ixx++) {
                  float tw = 0.4F + 0.6F * (float)Math.abs(Math.sin(t * (0.8 + hash(ixx, 3) * 2.0F) + ixx));
                  int r = Math.max(1, Math.round(S * (8.0E-4F + hash(ixx, 4) * 0.0016F)));
                  Gx.circle(Math.round(W * hash(ixx, 1)), Math.round(H * hash(ixx, 2)), r, Gx.rgba(16777215, 0.75F * tw * k));
               }

               float p = fract(t / 6.0);
               if (p < 0.25F) {
                  float q = p / 0.25F;

                  for (int j = 0; j < 14; j++) {
                     float f = q - j * 0.012F;
                     if (f < 0.0F) {
                        return;
                     }

                     Gx.circle(
                        Math.round(W * (0.15F + 0.6F * f)),
                        Math.round(H * (0.12F + 0.25F * f)),
                        Math.max(1, Math.round(S * 0.002F * (1.0F - j / 14.0F))),
                        Gx.rgba(16777215, (1.0F - j / 14.0F) * k)
                     );
                  }
               }
               break;
            case 4:
               int cols = 72;
               int cw = W / cols + 1;

               for (int w = 0; w < 3; w++) {
                  for (int c = 0; c < cols; c++) {
                     double x = (double)c / cols;
                     float y = H * (0.68F + w * 0.08F) + (float)(Math.sin(x * 9.0 + t * (0.8 + w * 0.3) + w) * H * 0.03);
                     Gx.gradientV(
                        c * W / cols, Math.round(y), cw, H - Math.round(y), Gx.rgba(w == 1 ? acc2 : acc, 0.16F * k), Gx.rgba(w == 1 ? acc2 : acc, 0.02F * k)
                     );
                  }
               }
               break;
            case 5:
               for (int ixx = 0; ixx < 22; ixx++) {
                  float x = W * fract(hash(ixx, 1) + t * (0.004 + hash(ixx, 2) * 0.01));
                  float y = H * (hash(ixx, 3) + 0.04F * (float)Math.sin(t * 0.5 + ixx));
                  int r = Math.round(S * (0.03F + hash(ixx, 4) * 0.06F));
                  Gx.circle(Math.round(x), Math.round(y), r, Gx.rgba(ixx % 2 == 0 ? acc : acc2, (0.05F + 0.07F * hash(ixx, 5)) * k));
               }
               break;
            case 6:
               int hy = Math.round(H * 0.62F);
               int sr = Math.round(S * 0.12F);
               Gx.glow(W / 2, hy - sr / 2, Math.round(sr * 2.4F), Gx.rgba(16732067, 0.22F * k));
               Gx.circle(W / 2, hy - sr / 2, sr, Gx.rgba(16738970, 0.55F * k));

               for (int ixx = 0; ixx < 6; ixx++) {
                  Gx.fill(W / 2 - sr, hy - sr / 2 + ixx * sr / 6 + sr / 10, sr * 2, Math.max(1, sr / 22 + ixx), Gx.rgba(722710, 0.7F * k));
               }

               Gx.fill(0, hy, W, H - hy, Gx.rgba(1181215, 0.55F * k));

               for (int ixx = 0; ixx < 14; ixx++) {
                  float f = fract(ixx / 14.0 + t * 0.12);
                  int y = hy + Math.round((H - hy) * f * f);
                  Gx.fill(0, y, W, Math.max(1, Math.round(1.0F + f * 2.0F)), Gx.rgba(16732120, (0.15F + 0.45F * f) * k));
               }
               break;
            case 7:
               for (int ixx = 0; ixx < 130; ixx++) {
                  float sp = 0.04F + hash(ixx, 2) * 0.06F;
                  float y = H * fract(hash(ixx, 1) + t * sp);
                  float x = W * hash(ixx, 3) + (float)Math.sin(t * (0.6 + hash(ixx, 4)) + ixx) * S * 0.012F;
                  int r = Math.max(1, Math.round(S * (0.0012F + hash(ixx, 5) * 0.0025F)));
                  Gx.circle(Math.round(x), Math.round(y), r, Gx.rgba(16777215, (0.35F + 0.45F * hash(ixx, 6)) * k));
               }
               break;
            case 8:
               for (int ixx = 0; ixx < 6; ixx++) {
                  float x = W * (0.5F + 0.42F * (float)Math.sin(t * 0.07 * (1.0 + ixx * 0.3) + ixx * 2.1));
                  float y = H * (0.5F + 0.38F * (float)Math.cos(t * 0.05 * (1.0 + ixx * 0.25) + ixx));
                  int col = Color.HSBtoRGB(fract(t * 0.02 + ixx * 0.16), 0.65F, 1.0F) & 16777215;
                  Gx.glow(Math.round(x), Math.round(y), Math.round(S * 0.35F), Gx.rgba(col, 0.16F * k));
               }
               break;
            case 9:
               int cols2 = 46;
               int cs = Math.max(2, W / cols2);
               int bs = Math.max(2, cs / 2);

               for (int c = 0; c < cols2; c++) {
                  float head = fract(hash(c, 1) + t * (0.12 + hash(c, 2) * 0.18));
                  int len = 10 + Math.round(hash(c, 3) * 10.0F);

                  for (int j = 0; j < len; j++) {
                     float y = head - j * 0.022F;
                     if (y < 0.0F) {
                        break;
                     }

                     float f = 1.0F - (float)j / len;
                     Gx.fill(c * cs + cs / 4, Math.round(y * H), bs, bs, Gx.rgba(j == 0 ? 14221288 : 2883447, f * 0.55F * k));
                  }
               }
               break;
            case 10:
               float cx = W / 2.0F;
               float cy = H / 2.0F;

               for (int ix = 0; ix < 160; ix++) {
                  float z = 1.0F - fract(hash(ix, 1) + t * (0.18 + hash(ix, 4) * 0.12));
                  float px = (hash(ix, 2) - 0.5F) * 2.0F;
                  float py = (hash(ix, 3) - 0.5F) * 2.0F;
                  float sx = cx + px / Math.max(0.04F, z) * S * 0.09F;
                  float sy = cy + py / Math.max(0.04F, z) * S * 0.09F;
                  if (!(sx < -20.0F) && !(sy < -20.0F) && !(sx > W + 20) && !(sy > H + 20)) {
                     float near = 1.0F - z;
                     int r = Math.max(1, Math.round(S * 0.0035F * near * near + 1.0F));
                     float z2 = Math.min(1.0F, z + 0.035F);
                     float tx = cx + px / Math.max(0.04F, z2) * S * 0.09F;
                     float tyy = cy + py / Math.max(0.04F, z2) * S * 0.09F;

                     for (int s = 1; s <= 3; s++) {
                        Gx.circle(
                           Math.round(sx + (tx - sx) * s / 3.0F),
                           Math.round(sy + (tyy - sy) * s / 3.0F),
                           Math.max(1, r - s / 2),
                           Gx.rgba(s == 1 ? 16777215 : acc, near * k * (0.5F - s * 0.12F))
                        );
                     }

                     Gx.circle(Math.round(sx), Math.round(sy), r, Gx.rgba(16777215, Math.min(1.0F, near * 1.4F) * k));
                  }
               }
               break;
            case 11:
               for (int c = 0; c < 6; c++) {
                  float ccx = W * (0.15F + 0.7F * hash(c, 1));
                  float ccy = H * (0.2F + 0.6F * hash(c, 2));
                  float size = S * (0.05F + 0.05F * hash(c, 3)) * (1.0F + 0.15F * (float)Math.sin(t + c));
                  double ay = t * (0.4 + hash(c, 4) * 0.5);
                  double ax = t * (0.3 + hash(c, 5) * 0.4);
                  float[][] pts = new float[8][];

                  for (int ix = 0; ix < 8; ix++) {
                     double x = (ix & 1) == 0 ? -1.0 : 1.0;
                     double y = (ix & 2) == 0 ? -1.0 : 1.0;
                     double z = (ix & 4) == 0 ? -1.0 : 1.0;
                     double x1 = x * Math.cos(ay) + z * Math.sin(ay);
                     double z1 = -x * Math.sin(ay) + z * Math.cos(ay);
                     double y1 = y * Math.cos(ax) - z1 * Math.sin(ax);
                     double z2 = y * Math.sin(ax) + z1 * Math.cos(ax);
                     float pr = (float)(3.0 / (4.0 + z2));
                     pts[ix] = new float[]{ccx + (float)x1 * size * pr, ccy + (float)y1 * size * pr, (float)z2};
                  }

                  int[][] edges = new int[][]{{0, 1}, {2, 3}, {4, 5}, {6, 7}, {0, 2}, {1, 3}, {4, 6}, {5, 7}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};

                  for (int[] e : edges) {
                     float[] a2 = pts[e[0]];
                     float[] b2 = pts[e[1]];
                     float depth = 0.55F - (a2[2] + b2[2]) * 0.18F;

                     for (int s = 0; s <= 10; s++) {
                        float q = s / 10.0F;
                        Gx.circle(
                           Math.round(a2[0] + (b2[0] - a2[0]) * q),
                           Math.round(a2[1] + (b2[1] - a2[1]) * q),
                           Math.max(1, Math.round(S * 0.0016F)),
                           Gx.rgba(c % 2 == 0 ? acc : acc2, depth * k)
                        );
                     }
                  }

                  for (float[] pt : pts) {
                     Gx.circle(Math.round(pt[0]), Math.round(pt[1]), Math.max(1, Math.round(S * 0.003F)), Gx.rgba(16777215, (0.5F - pt[2] * 0.2F) * k));
                  }
               }
               break;
            case 12:
               int[] cols3 = new int[]{14876683, 16764163, 27831, 4955978, 16053492};

               for (int ix = 0; ix < 34; ix++) {
                  float depth = 0.25F + 0.75F * hash(ix, 1);
                  float x = W * fract(hash(ix, 2) + t * 0.012 * depth);
                  float y = H * (hash(ix, 3) + 0.05F * (float)Math.sin(t * 0.6 * depth + ix));
                  int s = Math.round(S * 0.035F * depth);
                  int col = cols3[ix % cols3.length];
                  float al = (0.25F + 0.55F * depth) * k;
                  int bx = Math.round(x);
                  int by = Math.round(y);
                  Gx.rect(bx, by + Math.max(1, s / 6), s * 2, s, Math.max(1, s / 5), Gx.rgba(Gx.mix(col | 0xFF000000, -16777216, 0.4F) & 16777215, al));
                  Gx.rect(bx, by, s * 2, s, Math.max(1, s / 5), Gx.rgba(col, al));

                  for (int st = 0; st < 2; st++) {
                     Gx.circle(bx + s / 2 + st * s, by + s / 3, Math.max(1, s / 4), Gx.rgba(Gx.mix(col | 0xFF000000, -1, 0.3F) & 16777215, al));
                  }
               }
               break;
            case 13:
               float cx2 = W / 2.0F + (float)Math.sin(t * 0.5) * W * 0.05F;
               float cy2 = H / 2.0F + (float)Math.cos(t * 0.4) * H * 0.05F;

               for (int i = 0; i < 18; i++) {
                  float z = 1.0F - fract(i / 18.0 + t * 0.15);
                  float r = S * 0.06F / Math.max(0.05F, z);
                  if (!(r > S * 1.4F)) {
                     int dots = 40;
                     float near = 1.0F - z;

                     for (int d = 0; d < dots; d++) {
                        double ang = (Math.PI * 2) * d / dots + t * 0.3 + i * 0.2;
                        Gx.circle(
                           Math.round(cx2 + (float)Math.cos(ang) * r),
                           Math.round(cy2 + (float)Math.sin(ang) * r),
                           Math.max(1, Math.round(S * 0.003F * near + 1.0F)),
                           Gx.rgba(i % 2 == 0 ? acc : acc2, near * near * 0.8F * k)
                        );
                     }
                  }
               }
         }
      }
   }
}
