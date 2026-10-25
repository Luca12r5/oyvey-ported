package de.bauloo.styles;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.List;
import javax.imageio.ImageIO;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1059;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;

public final class AnchorGlow {
   public static final String FOLDER = "bauloo_anchor_glow";
   public static final String PACK_ID = "file/bauloo_anchor_glow";
   private static final String TEX = "textures/block/respawn_anchor_side";
   private static boolean applied;
   private static class_2960 previewId;
   private static long previewStamp = -1L;
   public static int previewW = 16;
   public static int previewH = 16;

   private AnchorGlow() {
   }

   private static Path packDir() {
      return FabricLoader.getInstance().getGameDir().resolve("resourcepacks").resolve("bauloo_anchor_glow");
   }

   private static Path previewFile() {
      return FabricLoader.getInstance().getConfigDir().resolve("bauloo-styles/anchor_glow_preview.png");
   }

   public static void apply() {
      applyLive();
      AnchorPixelGlow.applyLive();
   }

   public static void applyLive() {
      StylesUiConfig.load();
      if (active() || applied) {
         class_310 mc = class_310.method_1551();

         try {
            class_1059 atlas = AtlasWriter.atlas(class_1059.field_5275);
            if (atlas == null) {
               return;
            }

            BufferedImage empty = read(mc, 0);
            if (empty == null) {
               return;
            }

            BufferedImage last = null;
            BufferedImage full = read(mc, 4);

            for (int level = 1; level <= 4; level++) {
               BufferedImage charged = StylesUiConfig.anchorShowFull && full != null ? full : read(mc, level);
               if (charged != null) {
                  BufferedImage out = StylesUiConfig.anchorGlowCustom ? recolor(empty, charged, StylesUiConfig.anchorGlowColor) : charged;
                  LiveRecolor.applyImage(atlas, class_2960.method_60656("block/respawn_anchor_side" + level), out);
                  last = out;
               }
            }

            applied = active();
            if (last != null && active()) {
               Files.createDirectories(previewFile().getParent());
               ImageIO.write(last, "png", previewFile().toFile());
            }
         } catch (IOException | RuntimeException e) {
            BaulooStylesClient.LOGGER.warn("Anchor glow could not be applied: {}", e.toString());
         }
      }
   }

   static void prepare(class_310 mc, List<String> enabled) {
      enabled.remove("file/bauloo_anchor_glow");
   }

   public static boolean needsRebuild() {
      return active();
   }

   private static boolean active() {
      return StylesUiConfig.anchorGlowCustom || StylesUiConfig.anchorShowFull;
   }

   static BufferedImage read(class_310 mc, int level) throws IOException {
      List<class_3298> all = mc.method_1478().method_14489(class_2960.method_60656("textures/block/respawn_anchor_side" + level + ".png"));

      for (int i = all.size() - 1; i >= 0; i--) {
         class_3298 resource = all.get(i);
         if (!resource.method_14480().contains("bauloo_anchor_glow")) {
            try (InputStream in = resource.method_14482()) {
               return ImageIO.read(in);
            }
         }
      }

      return null;
   }

   static BufferedImage recolor(BufferedImage empty, BufferedImage charged, int color) {
      int w = charged.getWidth();
      int h = charged.getHeight();
      int frame = Math.min(w, h);
      BufferedImage out = new BufferedImage(w, h, 2);
      float[] target = Color.RGBtoHSB(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, null);
      int ew = Math.max(1, empty.getWidth());
      int eh = Math.max(1, Math.min(empty.getWidth(), empty.getHeight()));
      boolean[] anders = new boolean[w * h];
      boolean[] glow = new boolean[w * h];
      ArrayDeque<Integer> offen = new ArrayDeque<>();

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int c = charged.getRGB(x, y);
            int e = empty.getRGB(x * ew / w, y % frame * eh / frame);
            if (differs(c, e)) {
               anders[y * w + x] = true;
               float[] hsb = Color.RGBtoHSB(c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF, null);
               if (hsb[2] >= 0.7F && c >>> 24 >= 128) {
                  glow[y * w + x] = true;
                  offen.add(y * w + x);
               }
            }
         }
      }

      while (!offen.isEmpty()) {
         int i = offen.poll();
         int x = i % w;
         int y = i / w;
         int[][] nachbarn = new int[][]{{x - 1, y}, {x + 1, y}, {x, y - 1}, {x, y + 1}};

         for (int[] n : nachbarn) {
            if (n[0] >= 0 && n[0] < w && n[1] >= 0 && n[1] < h && n[1] / frame == y / frame) {
               int j = n[1] * w + n[0];
               if (anders[j] && !glow[j]) {
                  glow[j] = true;
                  offen.add(j);
               }
            }
         }
      }

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int c = charged.getRGB(x, y);
            if (glow[y * w + x]) {
               float[] hsb = Color.RGBtoHSB(c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF, null);
               float sat = target[1] * Math.min(1.0F, 0.35F + (1.0F - hsb[2]) * 1.3F);
               int rgb = Color.HSBtoRGB(target[0], sat, hsb[2]);
               out.setRGB(x, y, c & 0xFF000000 | rgb & 16777215);
            } else {
               out.setRGB(x, y, c);
            }
         }
      }

      return out;
   }

   private static boolean differs(int a, int b) {
      int d = Math.abs((a >> 16 & 0xFF) - (b >> 16 & 0xFF)) + Math.abs((a >> 8 & 0xFF) - (b >> 8 & 0xFF)) + Math.abs((a & 0xFF) - (b & 0xFF));
      return d > 30;
   }

   public static class_2960 preview() {
      Path file = previewFile();

      try {
         if (!Files.exists(file)) {
            return null;
         }

         long stamp = Files.getLastModifiedTime(file).toMillis();
         if (stamp != previewStamp) {
            class_2960 id = class_2960.method_60655("bauloo_styles", "anchor_glow/" + Long.toHexString(stamp));
            class_1011 image = class_1011.method_49277(Files.readAllBytes(file));
            previewW = image.method_4307();
            previewH = image.method_4323();
            class_310.method_1551().method_1531().method_4616(id, new class_1043(id::toString, image));
            previewId = id;
            previewStamp = stamp;
         }

         return previewId;
      } catch (IOException | RuntimeException e) {
         return null;
      }
   }
}
