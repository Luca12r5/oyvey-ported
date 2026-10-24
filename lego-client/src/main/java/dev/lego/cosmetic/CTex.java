package dev.lego.cosmetic;

import dev.lego.ui.Gx;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Path2D.Double;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Consumer;

public final class CTex {
   private static final Map<String, CTex.T> CACHE = new HashMap<>();
   private static final int FRAME_CACHE = 96;
   private static final LinkedHashMap<String, CTex.T> FRAMES = new LinkedHashMap<String, CTex.T>(64, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<String, CTex.T> e) {
         return this.size() > 96;
      }
   };
   private static final Object MAPS = new Object();
   private static final Object PAINTING = new Object();

   private CTex() {
   }

   private static String key(String name) {
      if (name.startsWith("cape_") && name.indexOf(35) > 0) {
         int h = name.indexOf(35);
         String base = name.substring(0, h);
         int n = Capes.frames(base);

         int k;
         try {
            k = Math.floorMod(Integer.parseInt(name.substring(h + 1)), Math.max(1, n));
         } catch (NumberFormatException var6) {
            k = 0;
         }

         return k != 0 && n > 1 ? base + "#" + k : base;
      } else {
         return name;
      }
   }

   private static CTex.T lookup(String key) {
      synchronized (MAPS) {
         return key.indexOf(35) > 0 && key.startsWith("cape_") ? FRAMES.get(key) : CACHE.get(key);
      }
   }

   public static CTex.T peek(String name) {
      return lookup(key(name));
   }

   public static CTex.T get(String name) {
      String key = key(name);
      CTex.T t = lookup(key);
      if (t != null) {
         return t;
      } else {
         synchronized (PAINTING) {
            t = lookup(key);
            if (t != null) {
               return t;
            } else {
               t = paint(key);
               synchronized (MAPS) {
                  if (key.indexOf(35) > 0 && key.startsWith("cape_")) {
                     FRAMES.put(key, t);
                  } else {
                     CACHE.put(key, t);
                  }
               }

               return t;
            }
         }
      }
   }

   private static CTex.T make(int w, int h, Consumer<Graphics2D> p) {
      BufferedImage img = new BufferedImage(w, h, 2);
      Graphics2D g = img.createGraphics();
      Gx.hints(g);
      p.accept(g);
      g.dispose();
      int[] px = img.getRGB(0, 0, w, h, null, 0, w);
      bleed(px, w, h);
      return new CTex.T(w, h, px);
   }

   private static void bleed(int[] px, int w, int h) {
      for (int pass = 0; pass < 3; pass++) {
         int[] src = (int[])px.clone();

         for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
               int i = y * w + x;
               if (src[i] >>> 24 == 0) {
                  int r = 0;
                  int g = 0;
                  int b = 0;
                  int n = 0;

                  for (int dy = -1; dy <= 1; dy++) {
                     for (int dx = -1; dx <= 1; dx++) {
                        int xx = x + dx;
                        int yy = y + dy;
                        if (xx >= 0 && yy >= 0 && xx < w && yy < h) {
                           int c = src[yy * w + xx];
                           if (c >>> 24 != 0 && ((c & 16777215) != 0 || c >>> 24 != 0) && c >>> 24 >= 8) {
                              r += c >> 16 & 0xFF;
                              g += c >> 8 & 0xFF;
                              b += c & 0xFF;
                              n++;
                           }
                        }
                     }
                  }

                  if (n > 0) {
                     px[i] = r / n << 16 | g / n << 8 | b / n;
                  }
               }
            }
         }
      }
   }

   static Color c(int rgb) {
      return new Color(rgb);
   }

   static Color ca(int rgb, int a) {
      return new Color(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, a);
   }

   static CTex.T of(BufferedImage img) {
      int w = img.getWidth();
      int h = img.getHeight();
      int[] px = img.getRGB(0, 0, w, h, null, 0, w);
      bleed(px, w, h);
      return new CTex.T(w, h, px);
   }

   private static CTex.T paint(String name) {
      if (name.startsWith("pet")) {
         CTex.T pt = PetTex.paint(name);
         if (pt != null) {
            return pt;
         }
      }

      if (name.startsWith("veh")) {
         CTex.T vt = Vehicles.tex(name);
         if (vt != null) {
            return vt;
         }
      }

      switch (name) {
         case "white":
            return make(16, 16, g -> {
               g.setColor(Color.WHITE);
               g.fillRect(0, 0, 16, 16);
            });
         case "wing_angel":
            return feathers(16777215, 14476271, 10464966, 16176506);
         case "wing_gold":
            return feathers(16773544, 15909424, 12157210, 16777215);
         case "wing_phoenix":
            return feathers(16774064, 16747038, 13639455, 16769658);
         case "wing_shadow":
            return feathers(7040896, 3027264, 921366, 10185727);
         case "wing_demon":
            return membrane(3803922, 9313060, 1705224, 16730938, false);
         case "wing_icedragon":
            return membrane(998246, 3785983, 663098, 13629183, true);
         case "wing_neon":
            return membrane(1181215, 2887242, 459278, 13851647, true);
         case "wing_butterfly":
            return butterfly();
         case "gold":
            return metal(16774064, 15251756, 10119698);
         case "silver":
            return metal(16777215, 13226202, 8029332);
         case "gems":
            return gems();
         case "halo":
            return make(256, 32, g -> {
               g.setPaint(new GradientPaint(0.0F, 0.0F, c(16775112), 0.0F, 32.0F, c(16762173)));
               g.fillRect(0, 0, 256, 32);
               g.setColor(ca(16777215, 150));
               g.fillRect(0, 6, 256, 5);
            });
         case "felt_black":
            return felt(1447451, 2237227, 7L);
         case "felt_red":
            return felt(12850716, 14689835, 11L);
         case "felt_purple":
            return felt(2757186, 4070750, 13L);
         case "fur_white":
            return fur();
         case "band_red":
            return make(64, 16, g -> {
               g.setPaint(new GradientPaint(0.0F, 0.0F, c(14891578), 0.0F, 16.0F, c(9311252)));
               g.fillRect(0, 0, 64, 16);
            });
         case "band_green":
            return make(64, 16, g -> {
               g.setPaint(new GradientPaint(0.0F, 0.0F, c(2875514), 0.0F, 16.0F, c(1279039)));
               g.fillRect(0, 0, 64, 16);
            });
         case "cat_ear":
            return catEar();
         case "bunny_ear":
            return bunnyEar();
         case "horn":
            return make(64, 128, g -> {
               g.setPaint(new LinearGradientPaint(0.0F, 0.0F, 0.0F, 128.0F, new float[]{0.0F, 0.55F, 1.0F}, new Color[]{c(1836038), c(9049108), c(15220794)}));
               g.fillRect(0, 0, 64, 128);
               g.setColor(ca(0, 60));

               for (int i = 0; i < 128; i += 9) {
                  g.fillRect(0, i, 64, 3);
               }

               g.setPaint(new GradientPaint(0.0F, 0.0F, ca(16777215, 70), 64.0F, 0.0F, ca(16777215, 0)));
               g.fillRect(0, 0, 22, 128);
            });
         case "headphone":
            return make(64, 64, g -> {
               g.setPaint(new GradientPaint(0.0F, 0.0F, c(2895414), 0.0F, 64.0F, c(1184536)));
               g.fillRect(0, 0, 64, 64);
               g.setColor(ca(16777215, 22));

               for (int i = 0; i < 64; i += 4) {
                  g.drawLine(0, i, 64, i);
               }
            });
         case "cushion":
            return make(64, 64, g -> {
               g.setPaint(new RadialGradientPaint(32.0F, 32.0F, 34.0F, new float[]{0.0F, 1.0F}, new Color[]{c(3816776), c(1447708)}));
               g.fillRect(0, 0, 64, 64);
               g.setColor(c(2021216));
               g.setStroke(new BasicStroke(5.0F));
               g.drawOval(9, 9, 46, 46);
            });
         case "sunglasses":
            return sunglasses();
         case "pixelglasses":
            return pixelGlasses();
         case "lego_red":
            return legoPlastic(14876683);
         case "lego_yellow":
            return legoPlastic(16764163);
         case "lego_blue":
            return legoPlastic(27831);
         case "lego_green":
            return legoPlastic(34091);
         case "sword":
            return sword();
         case "strap":
            return make(32, 32, g -> {
               g.setPaint(new GradientPaint(0.0F, 0.0F, c(4862498), 32.0F, 0.0F, c(2759184)));
               g.fillRect(0, 0, 32, 32);
            });
         case "flame":
            return flame();
         case "snowflake":
            return snowflake();
         case "heart":
            return heartSprite();
         case "orb":
            return orb(16777215);
         case "orb_purple":
            return orb(13073919);
         case "orb_cyan":
            return orb(8189951);
         case "spark":
            return spark();
         case "bolt":
            return boltSprite();
         case "mannequin_head":
         case "mannequin":
            return Tex2.pix(64, 64, (u, v, xx, y) -> {
               double n = Capes.fbm(u * 6.0, v * 6.0, 6, 6, 3, name.length() * 17 + 5);
               return 0xFF000000 | Capes.shade(16777215, 0.93 + 0.05 * n + 0.025 * (Capes.hash(xx, y, 77) - 0.5));
            });
         case "mannequin_shadow":
            return Tex2.pix(64, 64, (u, v, xx, y) -> {
               double d = Math.hypot((u - 0.5) * 2.0, (v - 0.5) * 2.4);
               double a = Math.pow(Math.max(0.0, 1.0 - d), 1.6);
               return Tex2.argb(0, a);
            });
         default:
            if (name.startsWith("capespec_")) {
               return CapeSpecs.paint("spec_" + name.substring(9));
            } else if (name.startsWith("cape_") && CapeSpecs.isPreset(name.substring(5).replaceFirst("#\\d+$", ""))) {
               return CapeSpecs.paint(name.substring(5));
            } else if (name.startsWith("cape_")) {
               return Capes.paint(name.substring(5));
            } else {
               if (name.startsWith("c3_") || name.equals("@skin")) {
                  CTex.T x = Cos3Tex.paint(name);
                  if (x != null) {
                     return x;
                  }
               }

               if (name.startsWith("xm_")) {
                  CTex.T x = XmasTex.paint(name);
                  if (x != null) {
                     return x;
                  }
               }

               if (name.startsWith("hd_")) {
                  CTex.T x = HdTex.paint(name);
                  if (x != null) {
                     return x;
                  }
               }

               CTex.T x = Tex2.paint(name);
               return x != null ? x : make(16, 16, g -> {
                  g.setColor(Color.MAGENTA);
                  g.fillRect(0, 0, 16, 16);
               });
            }
      }
   }

   private static CTex.T feathers(int light, int mid, int dark, int tip) {
      int W = 256;
      int H = 320;
      return make(W, H, g -> {
         Random r = new Random(7L);

         for (int i = 0; i < 9; i++) {
            double t = i / 8.0;
            double bx = 214.0 - t * 72.0;
            double by = 30.0 + t * 8.0;
            double ang = Math.toRadians(76.0 + t * 26.0 + r.nextGaussian() * 1.2);
            double len = 280.0 - t * 70.0;
            feather(g, bx, by, ang, len, 34.0, light, mid, dark, tip, 0.6);
         }

         for (int i = 0; i < 11; i++) {
            double t = i / 10.0;
            double bx = 146.0 - t * 132.0;
            double by = 36.0 - t * 10.0;
            double ang = Math.toRadians(100.0 + t * 12.0 + r.nextGaussian() * 1.2);
            double len = 205.0 - t * 85.0;
            feather(g, bx, by, ang, len, 32.0, light, mid, dark, tip, 0.35);
         }

         for (int row = 1; row >= 0; row--) {
            for (int i = 0; i < 12; i++) {
               double t = i / 11.0;
               double bx = 222.0 - t * 212.0;
               double by = 24 + row * 18 - t * 6.0;
               double ang = Math.toRadians(80.0 + t * 22.0);
               double len = row == 0 ? 52.0 + (1.0 - t) * 20.0 : 82.0 + (1.0 - t) * 26.0;
               feather(g, bx, by, ang, len, 30.0, light, mid, dark, tip, 0.1);
            }
         }

         g.setPaint(new GradientPaint(0.0F, 0.0F, c(light), W, 40.0F, c(mid)));
         Path2D arm = new Double();
         arm.moveTo(0.0, 2.0);
         arm.curveTo(70.0, -4.0, 170.0, 4.0, 252.0, 22.0);
         arm.lineTo(246.0, 44.0);
         arm.curveTo(170.0, 30.0, 80.0, 30.0, 0.0, 36.0);
         arm.closePath();
         g.fill(arm);
         g.setColor(ca(dark, 90));
         g.setStroke(new BasicStroke(2.0F));
         g.draw(arm);
         g.setColor(ca(16777215, 120));
         g.setStroke(new BasicStroke(2.5F, 1, 1));
         g.draw(new java.awt.geom.CubicCurve2D.Double(4.0, 8.0, 70.0, 3.0, 170.0, 10.0, 246.0, 26.0));
      });
   }

   private static void feather(Graphics2D g, double x, double y, double ang, double len, double wid, int light, int mid, int dark, int tip, double tipAmt) {
      AffineTransform old = g.getTransform();
      g.translate(x, y);
      g.rotate(ang - (Math.PI / 2));
      GeneralPath f = new GeneralPath();
      f.moveTo(0.0F, 0.0F);
      f.curveTo(wid * 0.7, len * 0.15, wid * 0.62, len * 0.8, 0.0, len);
      f.curveTo(-wid * 0.45, len * 0.82, -wid * 0.52, len * 0.2, 0.0, 0.0);
      f.closePath();
      g.setPaint(
         new LinearGradientPaint(
            0.0F, 0.0F, 0.0F, (float)len, new float[]{0.0F, 0.6F, 1.0F}, new Color[]{c(light), c(mid), CTex.Art2.mix(c(mid), c(tip), tipAmt)}
         )
      );
      g.fill(f);
      g.setPaint(new GradientPaint((float)(-wid) / 2.0F, 0.0F, ca(0, 0), (float)wid / 2.0F, 0.0F, ca(dark, 120)));
      g.fill(f);
      g.setColor(ca(dark, 150));
      g.setStroke(new BasicStroke(1.4F));
      g.draw(f);
      g.setColor(ca(16777215, 150));
      g.setStroke(new BasicStroke(1.2F));
      g.draw(new java.awt.geom.QuadCurve2D.Double(0.0, 2.0, wid * 0.12, len * 0.5, 0.0, len * 0.94));
      g.setColor(ca(dark, 55));
      g.setStroke(new BasicStroke(0.8F));

      for (double s = 0.12; s < 0.9; s += 0.07) {
         g.drawLine(0, (int)(len * s), (int)(wid * 0.42), (int)(len * s + wid * 0.35));
      }

      g.setTransform(old);
   }

   private static CTex.T membrane(int base, int light, int dark, int edge, boolean glowEdge) {
      int W = 256;
      int H = 320;
      return make(
         W,
         H,
         g -> {
            double[][] tips = new double[][]{{250.0, 30.0}, {240.0, 150.0}, {190.0, 260.0}, {110.0, 312.0}, {30.0, 250.0}};
            Path2D m = new Double();
            m.moveTo(4.0, 8.0);
            m.lineTo(tips[0][0], tips[0][1]);

            for (int i = 1; i < tips.length; i++) {
               double px = tips[i - 1][0];
               double py = tips[i - 1][1];
               double nx = tips[i][0];
               double ny = tips[i][1];
               double mx = (px + nx) / 2.0;
               double my = (py + ny) / 2.0;
               double ix = mx - (mx - 20.0) * 0.22;
               double iy = my - (my - 10.0) * 0.22;
               m.quadTo(ix, iy, nx, ny);
            }

            m.quadTo(10.0, 140.0, 4.0, 40.0);
            m.closePath();
            g.setPaint(new RadialGradientPaint(10.0F, 10.0F, 330.0F, new float[]{0.0F, 0.55F, 1.0F}, new Color[]{c(dark), c(base), c(light)}));
            g.fill(m);
            g.setClip(m);
            Random r = new Random(3L);
            g.setColor(ca(light, 70));

            for (int i = 0; i < 40; i++) {
               double[] t = tips[r.nextInt(tips.length)];
               double s = 0.3 + r.nextDouble() * 0.6;
               g.setStroke(new BasicStroke(0.8F));
               g.draw(
                  new java.awt.geom.QuadCurve2D.Double(
                     10.0 + (t[0] - 10.0) * s * 0.5,
                     10.0 + (t[1] - 10.0) * s * 0.5,
                     10.0 + (t[0] - 10.0) * s + r.nextGaussian() * 16.0,
                     10.0 + (t[1] - 10.0) * s + r.nextGaussian() * 16.0,
                     10.0 + (t[0] - 10.0) * s * 1.2,
                     10.0 + (t[1] - 10.0) * s * 1.2
                  )
               );
            }

            g.setClip(null);
            if (glowEdge) {
               for (int i = 6; i >= 1; i--) {
                  g.setColor(ca(edge, 20 + (6 - i) * 25));
                  g.setStroke(new BasicStroke(i * 1.6F, 1, 1));
                  g.draw(m);
               }
            } else {
               g.setColor(ca(edge, 150));
               g.setStroke(new BasicStroke(2.0F));
               g.draw(m);
            }

            for (double[] t : tips) {
               g.setStroke(new BasicStroke(7.0F, 1, 1));
               g.setColor(c(dark));
               g.draw(new java.awt.geom.QuadCurve2D.Double(6.0, 10.0, (t[0] + 6.0) / 2.0 + 10.0, (t[1] + 10.0) / 2.0 - 12.0, t[0], t[1]));
               g.setStroke(new BasicStroke(3.0F, 1, 1));
               g.setColor(glowEdge ? c(edge) : CTex.Art2.mix(c(light), c(edge), 0.3));
               g.draw(new java.awt.geom.QuadCurve2D.Double(6.0, 10.0, (t[0] + 6.0) / 2.0 + 10.0, (t[1] + 10.0) / 2.0 - 12.0, t[0], t[1]));
               g.setColor(c(dark));
               g.fill(new java.awt.geom.Ellipse2D.Double(t[0] - 4.0, t[1] - 4.0, 8.0, 8.0));
            }

            g.setColor(c(edge));
            Path2D claw = new Double();
            claw.moveTo(246.0, 26.0);
            claw.lineTo(256.0, 8.0);
            claw.lineTo(238.0, 22.0);
            claw.closePath();
            g.fill(claw);
         }
      );
   }

   private static CTex.T butterfly() {
      int W = 256;
      int H = 320;
      return make(
         W,
         H,
         g -> {
            Path2D up = new Double();
            up.moveTo(6.0, 30.0);
            up.curveTo(90.0, -30.0, 250.0, -10.0, 246.0, 80.0);
            up.curveTo(242.0, 140.0, 150.0, 170.0, 8.0, 150.0);
            up.closePath();
            Path2D lo = new Double();
            lo.moveTo(8.0, 150.0);
            lo.curveTo(120.0, 150.0, 210.0, 200.0, 190.0, 270.0);
            lo.curveTo(170.0, 330.0, 60.0, 320.0, 6.0, 230.0);
            lo.closePath();

            for (Path2D p : new Path2D[]{lo, up}) {
               g.setPaint(
                  new RadialGradientPaint(
                     20.0F, 120.0F, 260.0F, new float[]{0.0F, 0.45F, 0.8F, 1.0F}, new Color[]{c(1780735), c(4172287), c(10178047), c(1312799)}
                  )
               );
               g.fill(p);
               g.setColor(c(722450));
               g.setStroke(new BasicStroke(10.0F));
               g.draw(p);
            }

            g.setClip(up);
            g.setColor(ca(0, 120));
            g.setStroke(new BasicStroke(2.2F));

            for (int i = 0; i < 6; i++) {
               g.drawLine(8, 40 + i * 4, 250, -10 + i * 36);
            }

            g.setClip(null);
            g.setColor(Color.WHITE);
            double[][] dots = new double[][]{{225.0, 40.0}, {232.0, 70.0}, {214.0, 100.0}, {190.0, 130.0}, {170.0, 262.0}, {150.0, 285.0}, {120.0, 298.0}};

            for (double[] d : dots) {
               g.fill(new java.awt.geom.Ellipse2D.Double(d[0] - 5.0, d[1] - 5.0, 10.0, 10.0));
            }

            g.setColor(ca(16769357, 230));
            g.fill(new java.awt.geom.Ellipse2D.Double(150.0, 55.0, 34.0, 34.0));
            g.setColor(c(722450));
            g.fill(new java.awt.geom.Ellipse2D.Double(158.0, 63.0, 18.0, 18.0));
         }
      );
   }

   private static CTex.T metal(int hi, int mid, int lo) {
      return make(128, 128, g -> {
         g.setPaint(new LinearGradientPaint(0.0F, 0.0F, 0.0F, 128.0F, new float[]{0.0F, 0.35F, 0.55F, 1.0F}, new Color[]{c(hi), c(mid), c(lo), c(mid)}));
         g.fillRect(0, 0, 128, 128);
         Random r = new Random(5L);

         for (int i = 0; i < 400; i++) {
            g.setColor(ca(r.nextBoolean() ? 16777215 : 0, 14));
            g.fillRect(r.nextInt(128), r.nextInt(128), 1 + r.nextInt(8), 1);
         }
      });
   }

   private static CTex.T gems() {
      return make(
         128,
         32,
         g -> {
            int[] cols = new int[]{14684511, 2003199, 3066993, 10181046};

            for (int i = 0; i < 4; i++) {
               g.setPaint(
                  new RadialGradientPaint(
                     i * 32 + 12,
                     10.0F,
                     22.0F,
                     new float[]{0.0F, 0.4F, 1.0F},
                     new Color[]{Color.WHITE, c(cols[i]), CTex.Art2.mix(c(cols[i]), Color.BLACK, 0.5)}
                  )
               );
               g.fillRect(i * 32, 0, 32, 32);
            }
         }
      );
   }

   private static CTex.T felt(int a, int b, long seed) {
      return make(64, 64, g -> {
         g.setPaint(new GradientPaint(0.0F, 0.0F, c(b), 0.0F, 64.0F, c(a)));
         g.fillRect(0, 0, 64, 64);
         Random r = new Random(seed);

         for (int i = 0; i < 700; i++) {
            g.setColor(ca(r.nextBoolean() ? 16777215 : 0, 10 + r.nextInt(14)));
            g.fillRect(r.nextInt(64), r.nextInt(64), 1, 1);
         }
      });
   }

   private static CTex.T fur() {
      return make(64, 64, g -> {
         g.setColor(c(16053494));
         g.fillRect(0, 0, 64, 64);
         Random r = new Random(9L);

         for (int i = 0; i < 300; i++) {
            int x = r.nextInt(64);
            int y = r.nextInt(64);
            g.setColor(ca(r.nextBoolean() ? 16777215 : 13225174, 120));
            g.fill(new java.awt.geom.Ellipse2D.Double(x - 3, y - 3, 6.0, 6.0));
         }
      });
   }

   private static CTex.T catEar() {
      return make(64, 64, g -> {
         Path2D o = new Double();
         o.moveTo(2.0, 62.0);
         o.quadTo(8.0, 20.0, 30.0, 2.0);
         o.quadTo(56.0, 22.0, 62.0, 62.0);
         o.closePath();
         g.setColor(c(2829621));
         g.fill(o);
         Path2D in = new Double();
         in.moveTo(14.0, 60.0);
         in.quadTo(18.0, 30.0, 31.0, 16.0);
         in.quadTo(46.0, 32.0, 50.0, 60.0);
         in.closePath();
         g.setPaint(new GradientPaint(0.0F, 16.0F, c(16757703), 0.0F, 60.0F, c(16743074)));
         g.fill(in);
      });
   }

   private static CTex.T bunnyEar() {
      return make(64, 160, g -> {
         RoundRectangle2D o = new java.awt.geom.RoundRectangle2D.Double(4.0, 2.0, 56.0, 156.0, 56.0, 90.0);
         g.setColor(c(16053494));
         g.fill(o);
         RoundRectangle2D in = new java.awt.geom.RoundRectangle2D.Double(18.0, 18.0, 28.0, 128.0, 28.0, 60.0);
         g.setPaint(new GradientPaint(0.0F, 18.0F, c(16761556), 0.0F, 146.0F, c(16748465)));
         g.fill(in);
      });
   }

   private static CTex.T sunglasses() {
      return make(
         256,
         64,
         g -> {
            g.setColor(c(723727));
            RoundRectangle2D l = new java.awt.geom.RoundRectangle2D.Double(10.0, 10.0, 104.0, 46.0, 22.0, 30.0);
            RoundRectangle2D r = new java.awt.geom.RoundRectangle2D.Double(142.0, 10.0, 104.0, 46.0, 22.0, 30.0);
            g.fill(l);
            g.fill(r);
            g.fillRect(100, 14, 56, 8);

            for (RoundRectangle2D lens : new RoundRectangle2D[]{l, r}) {
               g.setPaint(
                  new LinearGradientPaint(
                     (float)lens.getX(),
                     12.0F,
                     (float)lens.getX() + 100.0F,
                     56.0F,
                     new float[]{0.0F, 0.5F, 1.0F},
                     new Color[]{c(3808350), c(1315882), c(670282)}
                  )
               );
               g.fill(
                  new java.awt.geom.RoundRectangle2D.Double(lens.getX() + 5.0, lens.getY() + 5.0, lens.getWidth() - 10.0, lens.getHeight() - 10.0, 16.0, 24.0)
               );
               g.setColor(ca(16777215, 110));
               g.setStroke(new BasicStroke(4.0F, 1, 1));
               g.drawLine((int)lens.getX() + 18, 22, (int)lens.getX() + 40, 18);
            }
         }
      );
   }

   private static CTex.T pixelGlasses() {
      return make(64, 16, g -> {
         g.setColor(Color.BLACK);
         g.fillRect(0, 0, 64, 4);
         g.fillRect(4, 4, 24, 8);
         g.fillRect(36, 4, 24, 8);
         g.fillRect(8, 12, 16, 4);
         g.fillRect(40, 12, 16, 4);
         g.setColor(Color.WHITE);
         g.fillRect(8, 4, 4, 4);
         g.fillRect(12, 8, 4, 4);
         g.fillRect(40, 4, 4, 4);
         g.fillRect(44, 8, 4, 4);
      });
   }

   private static CTex.T legoPlastic(int rgb) {
      return make(64, 64, g -> {
         Color base = c(rgb);
         g.setPaint(new GradientPaint(0.0F, 0.0F, CTex.Art2.mix(base, Color.WHITE, 0.18), 64.0F, 64.0F, CTex.Art2.mix(base, Color.BLACK, 0.12)));
         g.fillRect(0, 0, 64, 64);
         g.setColor(CTex.Art2.mix(base, Color.BLACK, 0.35));
         g.setStroke(new BasicStroke(2.0F));
         g.drawRect(1, 1, 62, 62);
         g.setColor(ca(16777215, 60));
         g.drawLine(3, 3, 60, 3);
      });
   }

   private static CTex.T sword() {
      return make(128, 128, g -> {
         AffineTransform o = g.getTransform();
         g.translate(64, 64);
         g.rotate(Math.toRadians(-45.0));
         Path2D b = new Double();
         b.moveTo(-7.0, -18.0);
         b.lineTo(-7.0, -76.0);
         b.lineTo(0.0, -88.0);
         b.lineTo(7.0, -76.0);
         b.lineTo(7.0, -18.0);
         b.closePath();
         g.setPaint(new GradientPaint(-7.0F, 0.0F, c(8385535), 7.0F, 0.0F, c(2071492)));
         g.fill(b);
         g.setColor(ca(16777215, 190));
         g.fillRect(-2, -80, 3, 60);
         g.setColor(c(4927107));
         g.fill(new java.awt.geom.RoundRectangle2D.Double(-22.0, -20.0, 44.0, 9.0, 6.0, 6.0));
         g.setColor(c(3811866));
         g.fillRect(-4, -12, 8, 30);
         g.setColor(c(14725962));
         g.fill(new java.awt.geom.Ellipse2D.Double(-6.0, 16.0, 12.0, 12.0));
         g.setTransform(o);
      });
   }

   private static CTex.T flame() {
      return make(64, 96, g -> {
         Path2D f = new Double();
         f.moveTo(32.0, 2.0);
         f.curveTo(46.0, 30.0, 62.0, 50.0, 56.0, 72.0);
         f.curveTo(50.0, 92.0, 14.0, 92.0, 8.0, 72.0);
         f.curveTo(2.0, 52.0, 20.0, 36.0, 32.0, 2.0);
         f.closePath();
         g.setPaint(new LinearGradientPaint(0.0F, 0.0F, 0.0F, 96.0F, new float[]{0.0F, 0.45F, 1.0F}, new Color[]{ca(16726554, 0), c(16742938), c(16769899)}));
         g.fill(f);
         g.setPaint(new RadialGradientPaint(32.0F, 72.0F, 22.0F, new float[]{0.0F, 1.0F}, new Color[]{ca(16777215, 230), ca(16769899, 0)}));
         g.fill(new java.awt.geom.Ellipse2D.Double(10.0, 50.0, 44.0, 44.0));
      });
   }

   private static CTex.T snowflake() {
      return make(64, 64, g -> {
         g.translate(32, 32);
         g.setColor(Color.WHITE);
         g.setStroke(new BasicStroke(3.2F, 1, 1));

         for (int i = 0; i < 6; i++) {
            g.drawLine(0, 0, 0, -28);
            g.drawLine(0, -16, -8, -24);
            g.drawLine(0, -16, 8, -24);
            g.rotate(Math.PI / 3);
         }
      });
   }

   private static CTex.T heartSprite() {
      return make(64, 64, g -> {
         GeneralPath h = new GeneralPath();
         h.moveTo(32.0F, 58.0F);
         h.curveTo(32.0F, 58.0F, 4.0F, 40.0F, 4.0F, 20.0F);
         h.curveTo(4.0F, 8.0F, 14.0F, 3.0F, 22.0F, 3.0F);
         h.curveTo(28.0F, 3.0F, 31.0F, 7.0F, 32.0F, 11.0F);
         h.curveTo(33.0F, 7.0F, 36.0F, 3.0F, 42.0F, 3.0F);
         h.curveTo(50.0F, 3.0F, 60.0F, 8.0F, 60.0F, 20.0F);
         h.curveTo(60.0F, 40.0F, 32.0F, 58.0F, 32.0F, 58.0F);
         h.closePath();
         g.setPaint(new GradientPaint(0.0F, 0.0F, c(16743080), 0.0F, 64.0F, c(14684511)));
         g.fill(h);
         g.setColor(ca(16777215, 150));
         g.fill(new java.awt.geom.Ellipse2D.Double(14.0, 10.0, 12.0, 9.0));
      });
   }

   private static CTex.T orb(int rgb) {
      return make(
         64,
         64,
         g -> {
            g.setPaint(
               new RadialGradientPaint(
                  32.0F, 32.0F, 32.0F, new float[]{0.0F, 0.25F, 0.6F, 1.0F}, new Color[]{ca(16777215, 255), ca(rgb, 230), ca(rgb, 70), ca(rgb, 0)}
               )
            );
            g.fillRect(0, 0, 64, 64);
         }
      );
   }

   private static CTex.T spark() {
      return make(64, 64, g -> {
         g.setPaint(new RadialGradientPaint(32.0F, 32.0F, 30.0F, new float[]{0.0F, 1.0F}, new Color[]{ca(16777215, 200), ca(16777215, 0)}));
         g.fillRect(0, 0, 64, 64);
         g.setColor(Color.WHITE);
         Path2D s = new Double();
         s.moveTo(32.0, 2.0);
         s.lineTo(36.0, 28.0);
         s.lineTo(62.0, 32.0);
         s.lineTo(36.0, 36.0);
         s.lineTo(32.0, 62.0);
         s.lineTo(28.0, 36.0);
         s.lineTo(2.0, 32.0);
         s.lineTo(28.0, 28.0);
         s.closePath();
         g.fill(s);
      });
   }

   private static CTex.T boltSprite() {
      return make(64, 96, g -> {
         Path2D b = new Double();
         b.moveTo(38.0, 2.0);
         b.lineTo(8.0, 54.0);
         b.lineTo(30.0, 54.0);
         b.lineTo(22.0, 94.0);
         b.lineTo(58.0, 36.0);
         b.lineTo(36.0, 36.0);
         b.closePath();

         for (int i = 6; i >= 1; i--) {
            g.setColor(ca(8189951, 25));
            g.setStroke(new BasicStroke(i * 2.5F));
            g.draw(b);
         }

         g.setColor(c(15138303));
         g.fill(b);
      });
   }

   static Rectangle2D rect(double x, double y, double w, double h) {
      return new java.awt.geom.Rectangle2D.Double(x, y, w, h);
   }

   static final class Art2 {
      static Color mix(Color a, Color b, double t) {
         t = Math.max(0.0, Math.min(1.0, t));
         return new Color(
            (int)(a.getRed() + (b.getRed() - a.getRed()) * t),
            (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            (int)(a.getBlue() + (b.getBlue() - a.getBlue()) * t),
            (int)(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t)
         );
      }
   }

   public static final class T {
      public final int w;
      public final int h;
      public final int[] argb;

      T(int w, int h, int[] argb) {
         this.w = w;
         this.h = h;
         this.argb = argb;
      }

      public int sample(float u, float v) {
         int x = Math.max(0, Math.min(this.w - 1, (int)(u * this.w)));
         int y = Math.max(0, Math.min(this.h - 1, (int)(v * this.h)));
         return this.argb[y * this.w + x];
      }
   }
}
