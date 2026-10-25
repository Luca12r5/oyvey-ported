package de.bauloo.styles;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;

public final class CrystalCore {
   public static final String FOLDER = "bauloo_crystal_core";
   public static final String PACK_ID = "file/bauloo_crystal_core";
   private static final class_2960 TEXTURE = class_2960.method_60656("textures/entity/end_crystal/end_crystal.png");
   private static boolean swapped;

   private CrystalCore() {
   }

   private static Path packDir() {
      return FabricLoader.getInstance().getGameDir().resolve("resourcepacks").resolve("bauloo_crystal_core");
   }

   public static boolean needsRebuild() {
      StylesUiConfig.load();
      return StylesUiConfig.coreCustom;
   }

   public static void apply() {
      applyLive();
   }

   public static void applyLive() {
      StylesUiConfig.load();
      class_310 mc = class_310.method_1551();

      try {
         if (StylesUiConfig.coreCustom) {
            BufferedImage source = read(mc);
            if (source == null) {
               return;
            }

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ImageIO.write(recolor(source, StylesUiConfig.coreColor), "png", bytes);
            mc.method_1531().method_4616(TEXTURE, new class_1043(TEXTURE::toString, class_1011.method_49277(bytes.toByteArray())));
            swapped = true;
         } else if (swapped) {
            BufferedImage source = read(mc);
            if (source == null) {
               return;
            }

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ImageIO.write(source, "png", bytes);
            mc.method_1531().method_4616(TEXTURE, new class_1043(TEXTURE::toString, class_1011.method_49277(bytes.toByteArray())));
            swapped = false;
         }
      } catch (IOException | RuntimeException e) {
         BaulooStylesClient.LOGGER.warn("Crystal core could not be applied: {}", e.toString());
      }
   }

   static void prepare(class_310 mc, List<String> enabled) {
      enabled.remove("file/bauloo_crystal_core");
   }

   private static BufferedImage read(class_310 mc) throws IOException {
      List<class_3298> all = mc.method_1478().method_14489(TEXTURE);

      for (int i = all.size() - 1; i >= 0; i--) {
         class_3298 resource = all.get(i);
         if (!resource.method_14480().contains("bauloo_crystal_core")) {
            try (InputStream in = resource.method_14482()) {
               return ImageIO.read(in);
            }
         }
      }

      return null;
   }

   private static BufferedImage recolor(BufferedImage src, int color) {
      int w = src.getWidth();
      int h = src.getHeight();
      BufferedImage out = new BufferedImage(w, h, 2);
      float[] target = Color.RGBtoHSB(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, null);
      int x0 = w / 2;
      int y1 = h / 2;

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int c = src.getRGB(x, y);
            if (x >= x0 && y < y1 && c >>> 24 > 0) {
               float[] hsb = Color.RGBtoHSB(c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF, null);
               int rgb = Color.HSBtoRGB(target[0], target[1], Math.max(hsb[2], target[2] * 0.35F));
               out.setRGB(x, y, c & 0xFF000000 | rgb & 16777215);
            } else {
               out.setRGB(x, y, c);
            }
         }
      }

      return out;
   }
}
