package de.bauloo.styles;

import com.mojang.blaze3d.platform.NativeImage;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

public final class AnchorGlow {
   public static final String FOLDER = "bauloo_anchor_glow";
   public static final String PACK_ID = "file/bauloo_anchor_glow";
   private static final String TEX = "textures/block/respawn_anchor_side";
   private static boolean applied;
   private static Identifier previewId;
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
         Minecraft mc = Minecraft.getInstance();

         try {
            TextureAtlas atlas = AtlasWriter.atlas(TextureAtlas.LOCATION_BLOCKS);
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
                  LiveRecolor.applyImage(atlas, Identifier.withDefaultNamespace("block/respawn_anchor_side" + level), out);
                  last = out;
               }
            }

            applied = active();
            if (last != null && active()) {
               Files.createDirectories(previewFile().getParent());
               ImageIO.write(last, "png", previewFile().toFile());
            }
         } catch (RuntimeException | IOException var8) {
            BaulooStylesClient.LOGGER.warn("Anchor glow could not be applied: {}", var8.toString());
         }
      }
   }

   static void prepare(Minecraft mc, List<String> enabled) {
      enabled.remove("file/bauloo_anchor_glow");
   }

   public static boolean needsRebuild() {
      return active();
   }

   private static boolean active() {
      return StylesUiConfig.anchorGlowCustom || StylesUiConfig.anchorShowFull;
   }

   static BufferedImage read(Minecraft mc, int level) throws IOException {
      List<Resource> all = mc.getResourceManager().getResourceStack(Identifier.withDefaultNamespace("textures/block/respawn_anchor_side" + level + ".png"));

      for (int i = all.size() - 1; i >= 0; i--) {
         Resource resource = all.get(i);
         if (!resource.sourcePackId().contains("bauloo_anchor_glow")) {
            BufferedImage var6;
            try (InputStream in = resource.open()) {
               var6 = ImageIO.read(in);
            }

            return var6;
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
         int xx = i % w;
         int y = i / w;
         int[][] nachbarn = new int[][]{{xx - 1, y}, {xx + 1, y}, {xx, y - 1}, {xx, y + 1}};

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
         for (int xx = 0; xx < w; xx++) {
            int c = charged.getRGB(xx, y);
            if (glow[y * w + xx]) {
               float[] hsb = Color.RGBtoHSB(c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF, null);
               float sat = target[1] * Math.min(1.0F, 0.35F + (1.0F - hsb[2]) * 1.3F);
               int rgb = Color.HSBtoRGB(target[0], sat, hsb[2]);
               out.setRGB(xx, y, c & 0xFF000000 | rgb & 16777215);
            } else {
               out.setRGB(xx, y, c);
            }
         }
      }

      return out;
   }

   private static boolean differs(int a, int b) {
      int d = Math.abs((a >> 16 & 0xFF) - (b >> 16 & 0xFF)) + Math.abs((a >> 8 & 0xFF) - (b >> 8 & 0xFF)) + Math.abs((a & 0xFF) - (b & 0xFF));
      return d > 30;
   }

   public static Identifier preview() {
      Path file = previewFile();

      try {
         if (!Files.exists(file)) {
            return null;
         } else {
            long stamp = Files.getLastModifiedTime(file).toMillis();
            if (stamp != previewStamp) {
               Identifier id = Identifier.fromNamespaceAndPath("bauloo_styles", "anchor_glow/" + Long.toHexString(stamp));
               NativeImage image = NativeImage.read(Files.readAllBytes(file));
               previewW = image.getWidth();
               previewH = image.getHeight();
               Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(id::toString, image));
               previewId = id;
               previewStamp = stamp;
            }

            return previewId;
         }
      } catch (RuntimeException | IOException var5) {
         return null;
      }
   }
}
