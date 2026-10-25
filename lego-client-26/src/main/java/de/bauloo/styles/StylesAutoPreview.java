package de.bauloo.styles;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

public final class StylesAutoPreview {
   private static final int SIZE = 512;
   private static final double K = 2.0;
   private static final String T = "assets/minecraft/textures/";

   private StylesAutoPreview() {
   }

   public static boolean render(Path zip, String category, Path target) {
      try {
         boolean var7;
         try (ZipFile file = new ZipFile(zip.toFile())) {
            Map<String, ZipEntry> entries = new HashMap<>();

            for (ZipEntry e : Collections.list(file.entries())) {
               entries.put(e.getName().replace('\\', '/').toLowerCase(Locale.ROOT), e);
            }

            StylesAutoPreview.Textures tex = new StylesAutoPreview.Textures(file, entries);

            BufferedImage image = switch (category) {
               case "crystal" -> crystal(tex);
               case "anchor" -> anchor(tex);
               case "armor" -> armor(tex);
               case "fire" -> fire(tex);
               case "obsidian" -> obsidian(tex);
               case "glowstone" -> glowstone(tex);
               case "stone" -> block(
                  tex.get(true, "assets/minecraft/textures/block/stone.png", "assets/minecraft/textures/blocks/stone.png"), null, 2237482, 11844816
               );
               case "deepslate" -> block(
                  tex.get(true, "assets/minecraft/textures/block/deepslate_top.png", "assets/minecraft/textures/blocks/deepslate_top.png"),
                  tex.get(true, "assets/minecraft/textures/block/deepslate.png", "assets/minecraft/textures/blocks/deepslate.png"),
                  1710626,
                  9213620
               );
               case "snow" -> block(
                  tex.get(true, "assets/minecraft/textures/block/snow.png", "assets/minecraft/textures/blocks/snow.png"),
                  tex.get(true, "assets/minecraft/textures/block/grass_block_snow.png", "assets/minecraft/textures/blocks/grass_side_snowed.png"),
                  2238522,
                  13690111
               );
               case "sword", "pickaxe", "axe" -> tool(tex, category);
               case "totem" -> flat(
                  tex.get(
                     true,
                     "assets/minecraft/textures/item/totem_of_undying.png",
                     "assets/minecraft/textures/items/totem_of_undying.png",
                     "assets/minecraft/textures/items/totem.png"
                  ),
                  3025426,
                  15913562
               );
               case "hotbar" -> hotbar(tex);
               case "inventory" -> inventory(tex);
               case "gap" -> flat(
                  tex.get(
                     true,
                     "assets/minecraft/textures/item/golden_apple.png",
                     "assets/minecraft/textures/items/golden_apple.png",
                     "assets/minecraft/textures/items/apple_golden.png"
                  ),
                  3811856,
                  16762960
               );
               case "pearl" -> flat(
                  tex.get(true, "assets/minecraft/textures/item/ender_pearl.png", "assets/minecraft/textures/items/ender_pearl.png"), 1060908, 4251840
               );
               default -> cloud(tex, category);
            };
            if (image == null) {
               return false;
            }

            var7 = ImageIO.write(image, "png", target.toFile());
         }

         return var7;
      } catch (RuntimeException | IOException var11) {
         BaulooStylesClient.LOGGER.warn("Auto preview for {} failed: {}", category, var11.toString());
         return false;
      }
   }

   private static StylesAutoPreview.Tex solid(int rgb) {
      int[] px = new int[256];
      Arrays.fill(px, 0xFF000000 | rgb);
      return new StylesAutoPreview.Tex(16, 16, px);
   }

   private static double[][] rotY(double a) {
      double c = Math.cos(a);
      double s = Math.sin(a);
      return new double[][]{{c, 0.0, s}, {0.0, 1.0, 0.0}, {-s, 0.0, c}};
   }

   private static double[][] rotX(double a) {
      double c = Math.cos(a);
      double s = Math.sin(a);
      return new double[][]{{1.0, 0.0, 0.0}, {0.0, c, -s}, {0.0, s, c}};
   }

   private static double[][] rotAxis(double[] axis, double a) {
      double n = Math.sqrt(axis[0] * axis[0] + axis[1] * axis[1] + axis[2] * axis[2]);
      double x = axis[0] / n;
      double y = axis[1] / n;
      double z = axis[2] / n;
      double c = Math.cos(a);
      double s = Math.sin(a);
      double t = 1.0 - c;
      return new double[][]{
         {t * x * x + c, t * x * y - s * z, t * x * z + s * y},
         {t * x * y + s * z, t * y * y + c, t * y * z - s * x},
         {t * x * z - s * y, t * y * z + s * x, t * z * z + c}
      };
   }

   private static double[][] mul(double[][] a, double[][] b) {
      double[][] r = new double[3][3];

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 3; j++) {
            r[i][j] = a[i][0] * b[0][j] + a[i][1] * b[1][j] + a[i][2] * b[2][j];
         }
      }

      return r;
   }

   private static double[][] scale(double[][] m, double k) {
      double[][] r = new double[3][3];

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 3; j++) {
            r[i][j] = m[i][j] * k;
         }
      }

      return r;
   }

   private static double[] apply(double[][] m, double x, double y, double z) {
      return new double[]{m[0][0] * x + m[0][1] * y + m[0][2] * z, m[1][0] * x + m[1][1] * y + m[1][2] * z, m[2][0] * x + m[2][1] * y + m[2][2] * z};
   }

   private static void raster(int[] buf, double[] p0, double[] p1, double[] p2, double[] t0, double[] t1, double[] t2, StylesAutoPreview.Tex tex, double shade) {
      int minX = Math.max(0, (int)Math.floor(Math.min(p0[0], Math.min(p1[0], p2[0]))));
      int minY = Math.max(0, (int)Math.floor(Math.min(p0[1], Math.min(p1[1], p2[1]))));
      int maxX = Math.min(511, (int)Math.ceil(Math.max(p0[0], Math.max(p1[0], p2[0]))));
      int maxY = Math.min(511, (int)Math.ceil(Math.max(p0[1], Math.max(p1[1], p2[1]))));
      double den = (p1[1] - p2[1]) * (p0[0] - p2[0]) + (p2[0] - p1[0]) * (p0[1] - p2[1]);
      if (!(Math.abs(den) < 1.0E-6)) {
         for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
               double px = x + 0.5;
               double py = y + 0.5;
               double a = ((p1[1] - p2[1]) * (px - p2[0]) + (p2[0] - p1[0]) * (py - p2[1])) / den;
               double b = ((p2[1] - p0[1]) * (px - p2[0]) + (p0[0] - p2[0]) * (py - p2[1])) / den;
               double c = 1.0 - a - b;
               if (!(a < -1.0E-4) && !(b < -1.0E-4) && !(c < -1.0E-4)) {
                  int u = (int)Math.max(0.0, Math.min((double)(tex.w - 1), a * t0[0] + b * t1[0] + c * t2[0]));
                  int v = (int)Math.max(0.0, Math.min((double)(tex.h - 1), a * t0[1] + b * t1[1] + c * t2[1]));
                  int col = tex.argb[v * tex.w + u];
                  double alpha = (col >>> 24 & 0xFF) / 255.0;
                  if (!(alpha <= 0.0)) {
                     int idx = y * 512 + x;
                     int dst = buf[idx];
                     int r = (int)((col >> 16 & 0xFF) * shade * alpha + (dst >> 16 & 0xFF) * (1.0 - alpha));
                     int g = (int)((col >> 8 & 0xFF) * shade * alpha + (dst >> 8 & 0xFF) * (1.0 - alpha));
                     int bl = (int)((col & 0xFF) * shade * alpha + (dst & 0xFF) * (1.0 - alpha));
                     buf[idx] = 0xFF000000 | Math.min(255, r) << 16 | Math.min(255, g) << 8 | Math.min(255, bl);
                  }
               }
            }
         }
      }
   }

   private static int[] backdrop(int top, int bottom, int glowRgb, int glowAlpha, double cx, double cy, double rx, double ry) {
      cx *= 2.0;
      cy *= 2.0;
      rx *= 2.0;
      ry *= 2.0;
      int[] buf = new int[262144];

      for (int y = 0; y < 512; y++) {
         double t = y / 512.0;

         for (int x = 0; x < 512; x++) {
            double r = (top >> 16 & 0xFF) * (1.0 - t) + (bottom >> 16 & 0xFF) * t;
            double g = (top >> 8 & 0xFF) * (1.0 - t) + (bottom >> 8 & 0xFF) * t;
            double b = (top & 0xFF) * (1.0 - t) + (bottom & 0xFF) * t;
            double dx = (x - cx) / rx;
            double dy = (y - cy) / ry;
            double d = Math.sqrt(dx * dx + dy * dy);
            double k = d >= 1.3 ? 0.0 : Math.pow(Math.max(0.0, 1.0 - d / 1.3), 2.0) * glowAlpha / 255.0;
            r = r * (1.0 - k) + (glowRgb >> 16 & 0xFF) * k;
            g = g * (1.0 - k) + (glowRgb >> 8 & 0xFF) * k;
            b = b * (1.0 - k) + (glowRgb & 0xFF) * k;
            buf[y * 512 + x] = 0xFF000000 | (int)r << 16 | (int)g << 8 | (int)b;
         }
      }

      return buf;
   }

   private static void shadow(int[] buf, double cx, double cy, double rx, double ry, int alpha) {
      cx *= 2.0;
      cy *= 2.0;
      rx *= 2.0;
      ry *= 2.0;

      for (int y = 0; y < 512; y++) {
         for (int x = 0; x < 512; x++) {
            double dx = (x - cx) / (rx * 1.25);
            double dy = (y - cy) / (ry * 1.6);
            double d = Math.sqrt(dx * dx + dy * dy);
            if (!(d >= 1.0)) {
               double k = (1.0 - d) * (1.0 - d) * alpha / 255.0;
               int c = buf[y * 512 + x];
               buf[y * 512 + x] = 0xFF000000
                  | (int)((c >> 16 & 0xFF) * (1.0 - k)) << 16
                  | (int)((c >> 8 & 0xFF) * (1.0 - k)) << 8
                  | (int)((c & 0xFF) * (1.0 - k));
            }
         }
      }
   }

   private static BufferedImage image(int[] buf) {
      BufferedImage img = new BufferedImage(512, 512, 1);
      img.setRGB(0, 0, 512, 512, buf, 0, 512);
      return img;
   }

   private static BufferedImage crystal(StylesAutoPreview.Textures t) {
      StylesAutoPreview.Tex tex = t.get(
         false, "assets/minecraft/textures/entity/end_crystal/end_crystal.png", "assets/minecraft/textures/entity/endercrystal/endercrystal.png"
      );
      if (tex == null) {
         return null;
      } else {
         StylesAutoPreview.Tex obsidian = t.get(true, "assets/minecraft/textures/block/obsidian.png", "assets/minecraft/textures/blocks/obsidian.png");
         if (obsidian == null) {
            obsidian = vanilla("obsidian", 1314846);
         }

         int[] buf = backdrop(2234932, 526094, 12472550, 80, 128.0, 98.0, 90.0, 90.0);
         StylesAutoPreview.Scene sc = new StylesAutoPreview.Scene(90.0, 0.34, 0.62, 430.0, new double[]{0.0, 7.0, 0.0}, 6.0);
         sc.blockFaces(obsidian, obsidian, new double[]{1.0, 0.75, 0.6, 0.6, 0.75});
         double s45 = Math.sin(Math.PI / 4);
         double[] axis = new double[]{s45, 0.0, s45};
         double spin = 0.5;
         double[][] outer = mul(rotY(spin), rotAxis(axis, Math.toRadians(60.0)));
         double[][] inner = mul(mul(outer, rotY(spin * 1.3)), rotAxis(axis, Math.toRadians(60.0)));
         double[][] core = mul(mul(inner, rotY(spin)), rotAxis(axis, Math.toRadians(60.0)));
         double[] pos = new double[]{0.0, 14.0, 0.0};
         double[] o = new double[]{-4.0, -4.0, -4.0};
         double[] size = new double[]{8.0, 8.0, 8.0};
         sc.box(tex, 32.0, 0.0, o, size, scale(core, 1.17045), pos, 0.0, false);
         sc.box(tex, 0.0, 0.0, o, size, scale(inner, 1.33875), pos, 0.0, false);
         sc.box(tex, 0.0, 0.0, o, size, scale(outer, 1.53), pos, 0.0, false);
         sc.render(buf);
         return image(buf);
      }
   }

   private static BufferedImage anchor(StylesAutoPreview.Textures t) {
      StylesAutoPreview.Tex top = t.get(
         true, "assets/minecraft/textures/block/respawn_anchor_top.png", "assets/minecraft/textures/block/respawn_anchor_top_off.png"
      );
      StylesAutoPreview.Tex side = t.get(
         true, "assets/minecraft/textures/block/respawn_anchor_side4.png", "assets/minecraft/textures/block/respawn_anchor_side0.png"
      );
      if (top != null && side != null) {
         int[] buf = backdrop(2366516, 657426, 9853695, 60, 128.0, 120.0, 80.0, 80.0);
         shadow(buf, 128.0, 220.0, 80.0, 16.0, 150);
         StylesAutoPreview.Scene sc = new StylesAutoPreview.Scene(60.0, 0.5, 0.62, 520.0, new double[]{0.0, -8.0, 0.0}, 6.0);
         sc.blockFaces(top, side, new double[]{1.0, 0.78, 0.6, 0.6, 0.78});
         sc.render(buf);
         return image(buf);
      } else {
         return null;
      }
   }

   private static BufferedImage armor(StylesAutoPreview.Textures t) {
      List<StylesAutoPreview.Tex[]> figures = new ArrayList<>();

      for (String m : new String[]{"netherite", "diamond", "iron", "gold", "chainmail"}) {
         StylesAutoPreview.Tex body = t.get(
            false, "assets/minecraft/textures/entity/equipment/humanoid/" + m + ".png", "assets/minecraft/textures/models/armor/" + m + "_layer_1.png"
         );
         StylesAutoPreview.Tex legs = t.get(
            false, "assets/minecraft/textures/entity/equipment/humanoid_leggings/" + m + ".png", "assets/minecraft/textures/models/armor/" + m + "_layer_2.png"
         );
         if (body != null) {
            figures.add(new StylesAutoPreview.Tex[]{body, legs});
         }

         if (figures.size() == 2) {
            break;
         }
      }

      if (figures.isEmpty()) {
         return null;
      } else {
         int[] buf = backdrop(1844274, 527120, 5942015, 50, 128.0, 115.0, 88.0, 85.0);
         StylesAutoPreview.Scene sc = new StylesAutoPreview.Scene(100.0, 0.16, 0.45, 470.0, new double[]{0.0, 16.0, 0.0}, 4.0);
         double[][] id = new double[][]{{1.0, 0.0, 0.0}, {0.0, 1.0, 0.0}, {0.0, 0.0, 1.0}};
         double[] zero = new double[]{0.0, 0.0, 0.0};
         double[] offsets = figures.size() == 1 ? new double[]{0.0} : new double[]{-11.0, 11.0};

         for (int i = 0; i < figures.size(); i++) {
            StylesAutoPreview.Tex bodyx = figures.get(i)[0];
            StylesAutoPreview.Tex legsx = figures.get(i)[1];
            double ox = offsets[i];
            shadow(buf, 128.0 + ox * 5.2, 226.0, 34.0, 8.0, 120);
            if (legsx != null) {
               sc.box(legsx, 16.0, 16.0, new double[]{ox - 4.0, 12.0, -2.0}, new double[]{8.0, 12.0, 4.0}, id, zero, 0.3, false);
               sc.box(legsx, 0.0, 16.0, new double[]{ox - 4.0, 0.0, -2.0}, new double[]{4.0, 12.0, 4.0}, id, zero, 0.3, false);
               sc.box(legsx, 0.0, 16.0, new double[]{ox, 0.0, -2.0}, new double[]{4.0, 12.0, 4.0}, id, zero, 0.3, true);
            }

            sc.box(bodyx, 0.0, 0.0, new double[]{ox - 4.0, 24.0, -4.0}, new double[]{8.0, 8.0, 8.0}, id, zero, 0.7, false);
            sc.box(bodyx, 16.0, 16.0, new double[]{ox - 4.0, 12.0, -2.0}, new double[]{8.0, 12.0, 4.0}, id, zero, 0.6, false);
            sc.box(bodyx, 40.0, 16.0, new double[]{ox - 8.0, 12.0, -2.0}, new double[]{4.0, 12.0, 4.0}, id, zero, 0.6, false);
            sc.box(bodyx, 40.0, 16.0, new double[]{ox + 4.0, 12.0, -2.0}, new double[]{4.0, 12.0, 4.0}, id, zero, 0.6, true);
            sc.box(bodyx, 0.0, 16.0, new double[]{ox - 4.0, 0.0, -2.0}, new double[]{4.0, 12.0, 4.0}, id, zero, 0.6, false);
            sc.box(bodyx, 0.0, 16.0, new double[]{ox, 0.0, -2.0}, new double[]{4.0, 12.0, 4.0}, id, zero, 0.6, true);
         }

         sc.render(buf);
         return image(buf);
      }
   }

   private static BufferedImage fire(StylesAutoPreview.Textures t) throws IOException {
      StylesAutoPreview.Tex flame = t.get(true, "assets/minecraft/textures/block/fire_0.png", "assets/minecraft/textures/blocks/fire_layer_0.png");
      if (flame == null) {
         return null;
      } else {
         BufferedImage out;
         try (InputStream in = StylesAutoPreview.class.getResourceAsStream("/assets/bauloo_styles/upload/fire_background.png")) {
            if (in == null) {
               return null;
            }

            BufferedImage bg = ImageIO.read(in);
            out = new BufferedImage(512, 512, 1);
            out.getGraphics().drawImage(bg, 0, 0, 512, 512, null);
         }

         int size = 471;
         int left = (512 - size) / 2;
         int top = 409 - size;

         for (int y = 0; y < size; y++) {
            int dy = top + y;
            if (dy >= 0 && dy < 512) {
               for (int x = 0; x < size; x++) {
                  int col = flame.argb[y * flame.h / size * flame.w + x * flame.w / size];
                  double a = (col >>> 24 & 0xFF) / 255.0;
                  if (!(a <= 0.0)) {
                     int dst = out.getRGB(left + x, dy);
                     int r = (int)((col >> 16 & 0xFF) * a + (dst >> 16 & 0xFF) * (1.0 - a));
                     int g = (int)((col >> 8 & 0xFF) * a + (dst >> 8 & 0xFF) * (1.0 - a));
                     int b = (int)((col & 0xFF) * a + (dst & 0xFF) * (1.0 - a));
                     out.setRGB(left + x, dy, 0xFF000000 | r << 16 | g << 8 | b);
                  }
               }
            }
         }

         return out;
      }
   }

   private static BufferedImage obsidian(StylesAutoPreview.Textures t) {
      StylesAutoPreview.Tex obsidian = t.get(true, "assets/minecraft/textures/block/obsidian.png", "assets/minecraft/textures/blocks/obsidian.png");
      if (obsidian == null) {
         return null;
      } else {
         int[] buf = backdrop(2234932, 526094, 9195750, 60, 128.0, 120.0, 80.0, 80.0);
         shadow(buf, 128.0, 220.0, 80.0, 16.0, 150);
         StylesAutoPreview.Scene sc = new StylesAutoPreview.Scene(60.0, 0.5, 0.62, 520.0, new double[]{0.0, -8.0, 0.0}, 6.0);
         sc.blockFaces(obsidian, obsidian, new double[]{1.0, 0.78, 0.6, 0.6, 0.78});
         sc.render(buf);
         return image(buf);
      }
   }

   private static StylesAutoPreview.Tex cropSquare(StylesAutoPreview.Tex t, double[] c) {
      int x0 = (int)Math.round(c[0] * t.w);
      int y0 = (int)Math.round(c[1] * t.h);
      int w = Math.max(1, Math.min(t.w - x0, (int)Math.round(c[2] * t.w)));
      int h = Math.max(1, Math.min(t.h - y0, (int)Math.round(c[3] * t.h)));
      int s = Math.max(w, h);
      int[] px = new int[s * s];
      int ox = (s - w) / 2;
      int oy = (s - h) / 2;

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            px[(oy + y) * s + ox + x] = t.argb[(y0 + y) * t.w + x0 + x];
         }
      }

      return new StylesAutoPreview.Tex(s, s, px);
   }

   private static BufferedImage cloud(StylesAutoPreview.Textures t, String category) {
      CloudCategories.Def d = CloudCategories.get(category);
      if (d != null && d.previewTop() != null) {
         StylesAutoPreview.Tex top = t.get(true, d.previewTop());
         if (top != null && d.crop() != null) {
            top = cropSquare(top, d.crop());
         }

         return "block".equals(d.previewType())
            ? block(top, d.previewSide() == null ? null : t.get(true, d.previewSide()), 2234932, 11832575)
            : flat(top, 2234932, 11832575);
      } else {
         return null;
      }
   }

   private static BufferedImage block(StylesAutoPreview.Tex top, StylesAutoPreview.Tex side, int backTop, int glow) {
      if (top == null && side == null) {
         return null;
      } else {
         StylesAutoPreview.Tex up = top != null ? top : side;
         StylesAutoPreview.Tex around = side != null ? side : up;
         int[] buf = backdrop(backTop, 526094, glow, 60, 128.0, 120.0, 80.0, 80.0);
         shadow(buf, 128.0, 220.0, 80.0, 16.0, 150);
         StylesAutoPreview.Scene sc = new StylesAutoPreview.Scene(60.0, 0.5, 0.62, 520.0, new double[]{0.0, -8.0, 0.0}, 6.0);
         sc.blockFaces(up, around, new double[]{1.0, 0.8, 0.62, 0.62, 0.8});
         sc.render(buf);
         return image(buf);
      }
   }

   private static BufferedImage glowstone(StylesAutoPreview.Textures t) {
      StylesAutoPreview.Tex glow = t.get(true, "assets/minecraft/textures/block/glowstone.png", "assets/minecraft/textures/blocks/glowstone.png");
      if (glow == null) {
         return null;
      } else {
         int[] buf = backdrop(2761234, 657414, 16762960, 60, 128.0, 120.0, 80.0, 80.0);
         shadow(buf, 128.0, 220.0, 80.0, 16.0, 150);
         StylesAutoPreview.Scene sc = new StylesAutoPreview.Scene(60.0, 0.5, 0.62, 520.0, new double[]{0.0, -8.0, 0.0}, 6.0);
         sc.blockFaces(glow, glow, new double[]{1.0, 0.85, 0.7, 0.7, 0.85});
         sc.render(buf);
         return image(buf);
      }
   }

   private static BufferedImage tool(StylesAutoPreview.Textures t, String kind) {
      StylesAutoPreview.Tex item = t.get(
         true,
         "assets/minecraft/textures/item/netherite_" + kind + ".png",
         "assets/minecraft/textures/item/diamond_" + kind + ".png",
         "assets/minecraft/textures/items/netherite_" + kind + ".png",
         "assets/minecraft/textures/items/diamond_" + kind + ".png"
      );
      return flat(item, 1844274, 5942015);
   }

   private static BufferedImage hotbar(StylesAutoPreview.Textures t) {
      StylesAutoPreview.Tex bar = t.get(false, "assets/minecraft/textures/gui/sprites/hud/hotbar.png");
      if (bar == null) {
         return null;
      } else {
         StylesAutoPreview.Tex sel = t.get(false, "assets/minecraft/textures/gui/sprites/hud/hotbar_selection.png");
         int[] buf = backdrop(1976866, 527120, 8051104, 40, 128.0, 128.0, 110.0, 60.0);
         int w = 471;
         int h = Math.max(1, w * 22 / 182);
         int left = (512 - w) / 2;
         int top = (512 - h) / 2;
         blit(buf, bar, left, top, w, h);
         if (sel != null) {
            int sw = w * 24 / 182;
            int sh = sw * sel.h / Math.max(1, sel.w);
            blit(buf, sel, left - sw / 24, top - sw / 24, sw, sh);
         }

         return image(buf);
      }
   }

   private static BufferedImage inventory(StylesAutoPreview.Textures t) {
      StylesAutoPreview.Tex inv = t.get(false, "assets/minecraft/textures/gui/container/inventory.png");
      if (inv == null) {
         return null;
      } else {
         int k = Math.max(1, inv.w / 256);
         int cw = Math.min(inv.w, 176 * k);
         int ch = Math.min(inv.h, 166 * k);
         int[] px = new int[cw * ch];

         for (int y = 0; y < ch; y++) {
            System.arraycopy(inv.argb, y * inv.w, px, y * cw, cw);
         }

         StylesAutoPreview.Tex part = new StylesAutoPreview.Tex(cw, ch, px);
         int[] buf = backdrop(1974832, 527120, 10135807, 40, 128.0, 128.0, 110.0, 90.0);
         int w = 440;
         int h = w * ch / cw;
         blit(buf, part, (512 - w) / 2, (512 - h) / 2, w, h);
         return image(buf);
      }
   }

   private static void blit(int[] buf, StylesAutoPreview.Tex tex, int x0, int y0, int w, int h) {
      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int px = x0 + x;
            int py = y0 + y;
            if (px >= 0 && py >= 0 && px < 512 && py < 512) {
               int col = tex.argb[y * tex.h / h * tex.w + x * tex.w / w];
               double a = (col >>> 24 & 0xFF) / 255.0;
               if (!(a <= 0.0)) {
                  int i = py * 512 + px;
                  int dst = buf[i];
                  int r = (int)((col >> 16 & 0xFF) * a + (dst >> 16 & 0xFF) * (1.0 - a));
                  int g = (int)((col >> 8 & 0xFF) * a + (dst >> 8 & 0xFF) * (1.0 - a));
                  int b = (int)((col & 0xFF) * a + (dst & 0xFF) * (1.0 - a));
                  buf[i] = 0xFF000000 | r << 16 | g << 8 | b;
               }
            }
         }
      }
   }

   private static BufferedImage flat(StylesAutoPreview.Tex item, int backTop, int glow) {
      if (item == null) {
         return null;
      } else {
         int[] buf = backdrop(backTop, 527120, glow, 50, 128.0, 128.0, 90.0, 90.0);
         int size = 358;
         int left = (512 - size) / 2;

         for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
               int col = item.argb[y * item.h / size * item.w + x * item.w / size];
               double a = (col >>> 24 & 0xFF) / 255.0;
               if (!(a <= 0.0)) {
                  int i = (left + y) * 512 + left + x;
                  int dst = buf[i];
                  int r = (int)((col >> 16 & 0xFF) * a + (dst >> 16 & 0xFF) * (1.0 - a));
                  int g = (int)((col >> 8 & 0xFF) * a + (dst >> 8 & 0xFF) * (1.0 - a));
                  int b = (int)((col & 0xFF) * a + (dst & 0xFF) * (1.0 - a));
                  buf[i] = 0xFF000000 | r << 16 | g << 8 | b;
               }
            }
         }

         return image(buf);
      }
   }

   private static StylesAutoPreview.Tex vanilla(String block, int fallbackRgb) {
      try {
         Identifier id = Identifier.fromNamespaceAndPath("minecraft", "textures/block/" + block + ".png");
         Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(id);
         if (resource.isPresent()) {
            try (InputStream in = resource.get().open()) {
               BufferedImage img = ImageIO.read(in);
               if (img != null) {
                  return StylesAutoPreview.Tex.of(img).firstFrame();
               }

               return solid(fallbackRgb);
            }
         }
      } catch (Exception var9) {
      }

      return solid(fallbackRgb);
   }

   private record Face(double depth, double[][] pts, double[][] uv, StylesAutoPreview.Tex tex, double shade) {
   }

   private static final class Scene {
      final List<StylesAutoPreview.Face> faces = new ArrayList<>();
      final double camDist;
      final double fov;
      final double screenY;
      final double[][] view;
      final double[] center;

      Scene(double camDist, double pitch, double yaw, double fov, double[] center, double screenY) {
         this.camDist = camDist;
         this.fov = fov;
         this.view = StylesAutoPreview.mul(StylesAutoPreview.rotX(-pitch), StylesAutoPreview.rotY(yaw));
         this.center = center;
         this.screenY = screenY;
      }

      double[] project(double[] p) {
         double[] v = StylesAutoPreview.apply(this.view, p[0] - this.center[0], p[1] - this.center[1], p[2] - this.center[2]);
         double z = v[2] + this.camDist;
         double f = this.fov * 2.0 / z;
         return new double[]{256.0 + v[0] * f, 256.0 + this.screenY * 2.0 - v[1] * f, z};
      }

      void add(StylesAutoPreview.Tex tex, double[][] pts3, double[][] uv, double shade) {
         if (tex != null) {
            double[][] pts2 = new double[4][];
            double depth = 0.0;

            for (int i = 0; i < 4; i++) {
               double[] p = this.project(pts3[i]);
               pts2[i] = new double[]{p[0], p[1]};
               depth += p[2] / 4.0;
            }

            this.faces.add(new StylesAutoPreview.Face(depth, pts2, uv, tex, shade));
         }
      }

      void quad(StylesAutoPreview.Tex tex, double[][] pts3, double shade) {
         if (tex != null) {
            this.add(tex, pts3, new double[][]{{0.0, 0.0}, {tex.w, 0.0}, {tex.w, tex.h}, {0.0, tex.h}}, shade);
         }
      }

      void box(StylesAutoPreview.Tex tex, double u, double v, double[] origin, double[] size, double[][] model, double[] pos, double inflate, boolean mirror) {
         if (tex != null) {
            double kx = tex.w / 64.0;
            double ky = tex.h / 32.0;
            double w = size[0];
            double h = size[1];
            double d = size[2];
            double x0 = origin[0] - inflate;
            double y0 = origin[1] - inflate;
            double z0 = origin[2] - inflate;
            double x1 = x0 + w + 2.0 * inflate;
            double y1 = y0 + h + 2.0 * inflate;
            double z1 = z0 + d + 2.0 * inflate;
            Function<double[], double[]> P = c -> {
               double[] r = StylesAutoPreview.apply(model, c[0], c[1], c[2]);
               return new double[]{pos[0] + r[0], pos[1] + r[1], pos[2] + r[2]};
            };
            double[][][] quads = new double[][][]{
               {{x1, y1, z0}, {x0, y1, z0}, {x0, y0, z0}, {x1, y0, z0}},
               {{x0, y1, z1}, {x1, y1, z1}, {x1, y0, z1}, {x0, y0, z1}},
               {{x0, y1, z0}, {x0, y1, z1}, {x0, y0, z1}, {x0, y0, z0}},
               {{x1, y1, z1}, {x1, y1, z0}, {x1, y0, z0}, {x1, y0, z1}},
               {{x0, y1, z0}, {x1, y1, z0}, {x1, y1, z1}, {x0, y1, z1}},
               {{x0, y0, z1}, {x1, y0, z1}, {x1, y0, z0}, {x0, y0, z0}}
            };
            double[][] rects = new double[][]{
               {u + d, v + d, w, h}, {u + d + w + d, v + d, w, h}, {u, v + d, d, h}, {u + d + w, v + d, d, h}, {u + d, v, w, d}, {u + d + w, v, w, d}
            };
            double[] shades = new double[]{0.8, 0.8, 0.65, 0.65, 1.0, 0.5};

            for (int f = 0; f < 6; f++) {
               double[][] pts = new double[4][];

               for (int i = 0; i < 4; i++) {
                  pts[i] = P.apply(quads[f][i]);
               }

               double ru = rects[f][0] * kx;
               double rv = rects[f][1] * ky;
               double rw = rects[f][2] * kx;
               double rh = rects[f][3] * ky;
               double[][] uv = new double[][]{{ru, rv}, {ru + rw, rv}, {ru + rw, rv + rh}, {ru, rv + rh}};
               if (mirror) {
                  uv = new double[][]{uv[1], uv[0], uv[3], uv[2]};
               }

               this.add(tex, pts, uv, shades[f]);
            }
         }
      }

      void blockFaces(StylesAutoPreview.Tex top, StylesAutoPreview.Tex side, double[] shades) {
         double x = -8.0;
         double y = -16.0;
         double z = -8.0;
         double s = 16.0;
         double x1 = x + s;
         double y1 = y + s;
         double z1 = z + s;
         this.quad(top, new double[][]{{x, y1, z}, {x1, y1, z}, {x1, y1, z1}, {x, y1, z1}}, shades[0]);
         this.quad(side, new double[][]{{x1, y1, z}, {x, y1, z}, {x, y, z}, {x1, y, z}}, shades[1]);
         this.quad(side, new double[][]{{x, y1, z1}, {x1, y1, z1}, {x1, y, z1}, {x, y, z1}}, shades[4]);
         this.quad(side, new double[][]{{x, y1, z}, {x, y1, z1}, {x, y, z1}, {x, y, z}}, shades[3]);
         this.quad(side, new double[][]{{x1, y1, z1}, {x1, y1, z}, {x1, y, z}, {x1, y, z1}}, shades[2]);
      }

      void render(int[] buf) {
         this.faces.sort(Comparator.comparingDouble(fx -> -fx.depth()));

         for (StylesAutoPreview.Face f : this.faces) {
            StylesAutoPreview.raster(buf, f.pts[0], f.pts[1], f.pts[2], f.uv[0], f.uv[1], f.uv[2], f.tex, f.shade);
            StylesAutoPreview.raster(buf, f.pts[0], f.pts[2], f.pts[3], f.uv[0], f.uv[2], f.uv[3], f.tex, f.shade);
         }
      }
   }

   private record Tex(int w, int h, int[] argb) {
      static StylesAutoPreview.Tex of(BufferedImage img) {
         return new StylesAutoPreview.Tex(img.getWidth(), img.getHeight(), img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth()));
      }

      StylesAutoPreview.Tex firstFrame() {
         if (this.h > this.w && this.h % this.w == 0) {
            int[] frame = new int[this.w * this.w];
            System.arraycopy(this.argb, 0, frame, 0, this.w * this.w);
            return new StylesAutoPreview.Tex(this.w, this.w, frame);
         } else {
            return this;
         }
      }
   }

   private record Textures(ZipFile file, Map<String, ZipEntry> entries) {
      StylesAutoPreview.Tex get(boolean frame, String... candidates) {
         for (String c : candidates) {
            ZipEntry e = this.entries.get(c.toLowerCase(Locale.ROOT));
            if (e != null) {
               try (InputStream in = this.file.getInputStream(e)) {
                  BufferedImage img = ImageIO.read(in);
                  if (img != null && img.getWidth() <= 4096 && img.getHeight() <= 4096) {
                     StylesAutoPreview.Tex t = StylesAutoPreview.Tex.of(img);
                     return frame ? t.firstFrame() : t;
                  }
               } catch (IOException var14) {
               }
            }
         }

         return null;
      }
   }
}
