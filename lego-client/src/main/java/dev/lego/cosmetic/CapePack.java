package dev.lego.cosmetic;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Double;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

final class CapePack {
   private static final Cos.Rarity C = Cos.Rarity.COMMON;
   private static final Cos.Rarity R = Cos.Rarity.RARE;
   private static final Cos.Rarity E = Cos.Rarity.EPIC;
   private static final Cos.Rarity L = Cos.Rarity.LEGENDARY;
   private static final List<Object[]> NAMES = new ArrayList<>();

   private CapePack() {
   }

   private static void cape(String id, String name, Cos.Rarity r, int frames, float fps, int trimHi, int trimLo, int lining, Capes.Painter p) {
      Capes.def("p4" + id, frames, fps, trimHi, trimLo, lining, p);
      NAMES.add(new Object[]{"p4" + id, name, r});
   }

   static void defineAll() {
      int gold = 16769162;
      int goldLo = 10119698;
      int silver = 16777215;
      int silverLo = 8029332;
      int[][] sv = new int[][]{{14876683, 16777215}, {1776418, 16764163}, {27831, 16777215}, {3046706, 16052448}, {6953882, 16756960}};
      String[] svN = new String[]{"Streifen Rot-Weiß", "Streifen Schwarz-Gelb", "Streifen Blau-Weiß", "Streifen Grün-Creme", "Streifen Lila-Rosa"};

      for (int i = 0; i < sv.length; i++) {
         int[] c = sv[i];
         cape(
            "stripes" + i,
            svN[i],
            C,
            1,
            0.0F,
            silver,
            silverLo,
            c[0],
            (g, im, ph, k, n) -> Capes.field(im, (u, v, x, y) -> (int)(u * 7.0) % 2 == 0 ? c[0] : c[1])
         );
      }

      int[][] dg = new int[][]{{16732067, 16765503}, {3789651, 666130}, {5227519, 662074}, {16747008, 2755072}, {16777215, 14876683}};
      String[] dgN = new String[]{"Bonbon-Streifen", "Neon-Diagonale Grün", "Neon-Diagonale Blau", "Warnstreifen", "Zuckerstangen-Cape"};

      for (int i = 0; i < dg.length; i++) {
         int[] c = dg[i];
         cape("diag" + i, dgN[i], R, 16, 10.0F, gold, goldLo, c[1], (g, im, ph, k, n) -> Capes.field(im, (u, v, x, y) -> {
            double s = fract(u * 4.0 + v * 2.5 - (double)k / n);
            return Capes.mix(c[0], c[1], Capes.smooth(0.45, 0.55, s) - Capes.smooth(0.95, 1.0, s));
         }));
      }

      int[][] wv = new int[][]{
         {666218, 3049182, 10479871}, {3803690, 13120122, 16756944}, {670234, 3059034, 13172656}, {2755072, 14704666, 16769146}, {1052714, 6965984, 14733567}
      };
      String[] wvN = new String[]{"Meereswellen", "Rosa Wellen", "Grüne Wellen", "Sonnenwellen", "Violette Wellen"};

      for (int i = 0; i < wv.length; i++) {
         int[] c = wv[i];
         cape("waves" + i, wvN[i], R, 16, 9.0F, silver, silverLo, c[0], (g, im, ph, k, n) -> Capes.field(im, (u, v, x, y) -> {
            double w = Math.sin(v * 18.0 + Math.sin(u * 6.0 + ph) * 1.6 - ph * 2.0) * 0.5 + 0.5;
            return Capes.ramp(w * 0.85 + v * 0.15, c[0], c[1], c[2]);
         }));
      }

      int[][] ck = new int[][]{{1776418, 16053492}, {14876683, 1776418}, {8014560, 3789768}};
      String[] ckN = new String[]{"Schachbrett", "Rot-Schwarz kariert", "Retro-Karo"};

      for (int i = 0; i < ck.length; i++) {
         int[] c = ck[i];
         cape(
            "check" + i,
            ckN[i],
            C,
            1,
            0.0F,
            silver,
            silverLo,
            c[0],
            (g, im, ph, k, n) -> Capes.field(im, (u, v, x, y) -> ((int)(u * 6.0) + (int)(v * 10.0)) % 2 == 0 ? c[0] : c[1])
         );
      }

      int[][] pk = new int[][]{{14876683, 16777215}, {1776418, 16732067}, {27831, 16764163}, {16769258, 5227519}};
      String[] pkN = new String[]{"Punkte Rot", "Punkte Pink", "Punkte Gelb-Blau", "Pastell-Punkte"};

      for (int i = 0; i < pk.length; i++) {
         int[] c = pk[i];
         cape("dots" + i, pkN[i], C, 8, 6.0F, silver, silverLo, c[0], (g, im, ph, k, n) -> Capes.field(im, (u, v, x, y) -> {
            double gx = u * 5.0;
            double gy = v * 8.0 + (int)(u * 5.0) % 2 * 0.5;
            double dx = fract(gx) - 0.5;
            double dy = fract(gy) - 0.5;
            double r = 0.27 + 0.05 * Math.sin(ph + (int)gx * 1.3 + (int)gy);
            return Math.hypot(dx, dy * 0.85) < r ? c[1] : c[0];
         }));
      }

      int[][] sf = new int[][]{{329242, 1708112, 16777215}, {1706538, 5905002, 16765168}, {6682, 674394, 12582896}, {0, 1710618, 16769146}};
      String[] sfN = new String[]{"Sternennacht", "Lila Kosmos", "Polarnacht", "Goldene Sterne"};

      for (int i = 0; i < sf.length; i++) {
         int[] c = sf[i];
         int seed = 11 + i;
         cape("stars" + i, sfN[i], E, 12, 6.0F, gold, goldLo, c[0], (g, im, ph, k, n) -> {
            Capes.field(im, (u, v, x, y) -> Capes.ramp(v * 0.8 + Capes.fbm(u * 3.0, v * 5.0, 0, 0, 3, seed) * 0.4, c[0], c[1]));
            Random r = new Random(seed);

            for (int s = 0; s < 70; s++) {
               double sx = r.nextDouble() * 160.0;
               double sy = r.nextDouble() * 256.0;
               double sz = 0.8 + r.nextDouble() * 2.2;
               double off = r.nextDouble() * 6.28;
               Capes.star(g, sx, sy, sz, c[2], 0.35 + 0.65 * (0.5 + 0.5 * Math.sin(ph + off)));
            }
         });
      }

      Object[][] em = new Object[][]{
         {"heart", "Großes Herz", 3803668, 11538986, 16732026},
         {"star", "Großer Stern", 659504, 2767514, 16765503},
         {"bolt", "Blitz-Emblem", 1052698, 2763332, 16773194},
         {"diamond", "Diamant", 399914, 674410, 8189951},
         {"moon", "Halbmond", 657952, 1712208, 16773312},
         {"sun", "Sonne", 16747034, 16765503, 16777215},
         {"cross", "Ritterkreuz", 16053492, 14211296, 12588060},
         {"brick", "Lego-Emblem", 16764163, 15906816, 14876683}
      };

      for (Object[] o : em) {
         String sh = (String)o[0];
         int bg0 = (Integer)o[2];
         int bg1 = (Integer)o[3];
         int fg = (Integer)o[4];
         cape("emb_" + sh, (String)o[1], E, 12, 8.0F, gold, goldLo, bg0, (g, im, ph, k, n) -> {
            Capes.field(im, (u, v, x, y) -> Capes.mix(bg1, bg0, Math.hypot(u - 0.5, (v - 0.42) * 0.7) * 1.6));
            double pulse = 0.5 + 0.5 * Math.sin(ph);
            Capes.addDot(im, 80.0, 107.52, 70.0 + pulse * 10.0, fg & 16777215, 0.25 + 0.2 * pulse);
            g.setColor(new Color(fg));
            g.fill(shape(sh, 80.0, 107.52, 52.0));
            g.setColor(new Color(255, 255, 255, 70));
            g.fill(shape(sh, 76.0, 101.52, 22.0));
         });
      }

      int[][] cv = new int[][]{{1776418, 3789651}, {662074, 16732067}, {16053492, 27831}};
      String[] cvN = new String[]{"Pfeile Grün", "Pfeile Pink", "Pfeile Blau"};

      for (int i = 0; i < cv.length; i++) {
         int[] c = cv[i];
         cape(
            "chevron" + i,
            cvN[i],
            R,
            16,
            10.0F,
            silver,
            silverLo,
            c[0],
            (g, im, ph, k, n) -> Capes.field(im, (u, v, x, y) -> fract(v * 6.0 - Math.abs(u - 0.5) * 2.4 - (double)k / n) < 0.42 ? c[1] : c[0])
         );
      }

      Object[][] fl = new Object[][]{
         {"de", "Deutschland", new int[]{0, 14483456, 16764416}, true},
         {"at", "Österreich", new int[]{15542585, 16777215, 15542585}, true},
         {"it", "Italien", new int[]{37446, 16777215, 13511479}, false},
         {"fr", "Frankreich", new int[]{21924, 16777215, 15679797}, false},
         {"nl", "Niederlande", new int[]{11410472, 16777215, 2180747}, true},
         {"be", "Belgien", new int[]{0, 16636452, 15676224}, false},
         {"pl", "Polen", new int[]{16777215, 14423100}, true},
         {"ua", "Ukraine", new int[]{22455, 16766720}, true}
      };

      for (Object[] o : fl) {
         int[] c = (int[])o[2];
         boolean horiz = (Boolean)o[3];
         cape(
            "flag_" + o[0],
            (String)o[1],
            C,
            1,
            0.0F,
            silver,
            silverLo,
            c[0],
            (g, im, ph, k, n) -> Capes.field(im, (u, v, x, y) -> c[Math.min(c.length - 1, (int)((horiz ? v : u) * c.length))])
         );
      }

      cape("flag_ch", "Schweiz", C, 1, 0.0F, silver, silverLo, 14297372, (g, im, ph, k, n) -> Capes.field(im, (u, v, x, y) -> {
         double dx = Math.abs(u - 0.5);
         double dy = Math.abs(v - 0.45) * 256.0 / 160.0;
         return (!(dx < 0.1) || !(dy < 0.32)) && (!(dy < 0.1) || !(dx < 0.32)) ? 14297372 : 16777215;
      }));
      cape(
         "flag_se",
         "Schweden",
         C,
         1,
         0.0F,
         silver,
         silverLo,
         27303,
         (g, im, ph, k, n) -> Capes.field(im, (u, v, x, y) -> !(Math.abs(u - 0.38) < 0.09) && !(Math.abs(v - 0.42) < 0.055) ? 27303 : 16698368)
      );
      int[][] pl = new int[][]{
         {1048618, 8006368, 16739056, 16777215},
         {2586, 674442, 3856639, 14745599},
         {662016, 3836426, 12123946, 16318432},
         {1703936, 10492416, 16747034, 16773296},
         {657930, 3816004, 10134450, 16777215}
      };
      String[] plN = new String[]{"Plasma", "Ozean-Plasma", "Toxisch", "Feuer-Plasma", "Rauch"};

      for (int i = 0; i < pl.length; i++) {
         int[] c = pl[i];
         int seed = 40 + i;
         cape("plasma" + i, plN[i], L, 16, 8.0F, gold, goldLo, c[0], (g, im, ph, k, n) -> Capes.field(im, (u, v, x, y) -> {
            double ox = Math.cos(ph) * 0.8;
            double oy = Math.sin(ph) * 0.8;
            double q = Capes.fbm(u * 3.0 + ox, v * 5.0 + oy, 0, 0, 4, seed);
            double w = Capes.fbm(u * 3.0 + q * 2.0 - ox, v * 5.0 + q * 2.0 - oy, 0, 0, 4, seed + 7);
            return Capes.ramp(w, c[0], c[1], c[2], c[3]);
         }));
      }

      int[][] sc = new int[][]{{670250, 2797178}, {3803658, 13647914}, {1710650, 9075424}};
      String[] scN = new String[]{"Smaragdschuppen", "Feuerschuppen", "Amethystschuppen"};

      for (int i = 0; i < sc.length; i++) {
         int[] c = sc[i];
         cape("scales" + i, scN[i], E, 12, 6.0F, gold, goldLo, c[0], (g, im, ph, k, n) -> Capes.field(im, (u, v, x, y) -> {
            double gx = u * 6.0;
            double gy = v * 12.0;
            double row = Math.floor(gy);
            double sx = fract(gx + row % 2.0 * 0.5) - 0.5;
            double sy = fract(gy);
            double d = Math.hypot(sx, sy * 0.9);
            double shine = 0.5 + 0.5 * Math.sin(ph - v * 6.0 + u * 2.0);
            return d < 0.5 ? Capes.mix(c[1], c[0], d * 1.6 + 0.2 * (1.0 - shine)) : c[0];
         }));
      }

      int[][] gl = new int[][]{{16732067, 8006368}, {5227519, 3789651}, {16765503, 16731946}, {1776418, 4868698}, {16777215, 10475775}};
      String[] glN = new String[]{"Glitzer Pink-Lila", "Glitzer Türkis", "Glitzer Sonnenuntergang", "Schwarzer Glitzer", "Eisglitzer"};

      for (int i = 0; i < gl.length; i++) {
         int[] c = gl[i];
         int seed = 70 + i;
         cape("glitter" + i, glN[i], R, 8, 6.0F, silver, silverLo, c[1], (g, im, ph, k, n) -> {
            Capes.field(im, (u, v, x, y) -> Capes.mix(c[0], c[1], v));
            Random r = new Random(seed);

            for (int s = 0; s < 120; s++) {
               double sx = r.nextDouble() * 160.0;
               double sy = r.nextDouble() * 256.0;
               double off = r.nextDouble() * 6.28;
               double a = Math.max(0.0, Math.sin(ph + off));
               if (a > 0.05) {
                  Capes.star(g, sx, sy, 0.8 + r.nextDouble() * 1.4, 16777215, a);
               }
            }
         });
      }
   }

   private static Shape shape(String sh, double cx, double cy, double s) {
      Path2D p = new Double();
      switch (sh) {
         case "heart":
            for (int ixxx = 0; ixxx <= 40; ixxx++) {
               double t = (Math.PI * 2) * ixxx / 40.0;
               double x = 16.0 * Math.pow(Math.sin(t), 3.0) / 16.0;
               double y = -(13.0 * Math.cos(t) - 5.0 * Math.cos(2.0 * t) - 2.0 * Math.cos(3.0 * t) - Math.cos(4.0 * t)) / 16.0;
               if (ixxx == 0) {
                  p.moveTo(cx + x * s, cy + y * s);
               } else {
                  p.lineTo(cx + x * s, cy + y * s);
               }
            }
            break;
         case "star":
            for (int ixx = 0; ixx < 10; ixx++) {
               double an = (-Math.PI / 2) + Math.PI * ixx / 5.0;
               double r = ixx % 2 == 0 ? s : s * 0.42;
               if (ixx == 0) {
                  p.moveTo(cx + Math.cos(an) * r, cy + Math.sin(an) * r);
               } else {
                  p.lineTo(cx + Math.cos(an) * r, cy + Math.sin(an) * r);
               }
            }
            break;
         case "bolt":
            double[][] b = new double[][]{{0.15, -1.0}, {-0.55, 0.1}, {-0.05, 0.1}, {-0.25, 1.0}, {0.55, -0.15}, {0.05, -0.15}, {0.35, -1.0}};

            for (int ix = 0; ix < b.length; ix++) {
               if (ix == 0) {
                  p.moveTo(cx + b[ix][0] * s, cy + b[ix][1] * s);
               } else {
                  p.lineTo(cx + b[ix][0] * s, cy + b[ix][1] * s);
               }
            }
            break;
         case "diamond":
            p.moveTo(cx, cy - s);
            p.lineTo(cx + s * 0.7, cy - s * 0.3);
            p.lineTo(cx, cy + s);
            p.lineTo(cx - s * 0.7, cy - s * 0.3);
            break;
         case "moon":
            Area a = new Area(new java.awt.geom.Ellipse2D.Double(cx - s, cy - s, s * 2.0, s * 2.0));
            a.subtract(new Area(new java.awt.geom.Ellipse2D.Double(cx - s * 0.45, cy - s * 1.15, s * 2.0, s * 2.0)));
            return a;
         case "sun":
            for (int i = 0; i < 24; i++) {
               double an = (Math.PI * 2) * i / 24.0;
               double r = i % 2 == 0 ? s : s * 0.72;
               if (i == 0) {
                  p.moveTo(cx + Math.cos(an) * r, cy + Math.sin(an) * r);
               } else {
                  p.lineTo(cx + Math.cos(an) * r, cy + Math.sin(an) * r);
               }
            }
            break;
         case "cross":
            double w = s * 0.32;
            p.append(new java.awt.geom.Rectangle2D.Double(cx - w / 2.0, cy - s, w, s * 2.0), false);
            p.append(new java.awt.geom.Rectangle2D.Double(cx - s * 0.7, cy - s * 0.45, s * 1.4, w), false);
            return p;
         default:
            p.append(new java.awt.geom.RoundRectangle2D.Double(cx - s, cy - s * 0.55, s * 2.0, s * 1.1, s * 0.2, s * 0.2), false);

            for (int ixxxx = -1; ixxxx <= 1; ixxxx += 2) {
               p.append(new java.awt.geom.Ellipse2D.Double(cx + ixxxx * s * 0.5 - s * 0.28, cy - s * 0.85, s * 0.56, s * 0.4), false);
            }

            return p;
      }

      p.closePath();
      return p;
   }

   private static double fract(double v) {
      return v - Math.floor(v);
   }

   static void register() {
      Capes.frames("cape_lego");

      for (Object[] o : NAMES) {
         Cos.cape((String)o[0], (String)o[1], (Cos.Rarity)o[2]);
      }
   }
}
