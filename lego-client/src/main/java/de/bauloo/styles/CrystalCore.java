package de.bauloo.styles;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

public final class CrystalCore {
   public static final String FOLDER = "bauloo_crystal_core";
   public static final String PACK_ID = "file/bauloo_crystal_core";
   private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/end_crystal/end_crystal.png");
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
      Minecraft mc = Minecraft.getInstance();

      try {
         if (StylesUiConfig.coreCustom) {
            BufferedImage source = read(mc);
            if (source == null) {
               return;
            }

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ImageIO.write(recolor(source, StylesUiConfig.coreColor), "png", bytes);
            mc.getTextureManager().register(TEXTURE, new DynamicTexture(TEXTURE::toString, NativeImage.read(bytes.toByteArray())));
            swapped = true;
         } else if (swapped) {
            BufferedImage source = read(mc);
            if (source == null) {
               return;
            }

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ImageIO.write(source, "png", bytes);
            mc.getTextureManager().register(TEXTURE, new DynamicTexture(TEXTURE::toString, NativeImage.read(bytes.toByteArray())));
            swapped = false;
         }
      } catch (RuntimeException | IOException var3) {
         BaulooStylesClient.LOGGER.warn("Crystal core could not be applied: {}", var3.toString());
      }
   }

   static void prepare(Minecraft mc, List<String> enabled) {
      enabled.remove("file/bauloo_crystal_core");
   }

   private static BufferedImage read(Minecraft mc) throws IOException {
      List<Resource> all = mc.getResourceManager().getResourceStack(TEXTURE);

      for (int i = all.size() - 1; i >= 0; i--) {
         Resource resource = all.get(i);
         if (!resource.sourcePackId().contains("bauloo_crystal_core")) {
            BufferedImage var5;
            try (InputStream in = resource.open()) {
               var5 = ImageIO.read(in);
            }

            return var5;
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
