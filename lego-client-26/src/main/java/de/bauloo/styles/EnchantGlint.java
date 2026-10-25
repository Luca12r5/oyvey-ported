package de.bauloo.styles;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public final class EnchantGlint {
   public static final String PACK_ID = "file/bauloo_glint";
   private static final Identifier[] TEXTURES = new Identifier[]{
      Identifier.withDefaultNamespace("textures/misc/enchanted_glint_item.png"), Identifier.withDefaultNamespace("textures/misc/enchanted_glint_armor.png")
   };
   private static boolean swapped;

   private EnchantGlint() {
   }

   public static void register() {
      ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
         public Identifier getFabricId() {
            return Identifier.fromNamespaceAndPath("bauloo_styles", "enchant_glint");
         }

         public Collection<Identifier> getFabricDependencies() {
            return List.of(ResourceReloadListenerKeys.TEXTURES);
         }

         public void onResourceManagerReload(ResourceManager manager) {
            EnchantGlint.swapped = false;
            Minecraft.getInstance().schedule(EnchantGlint::applyLive);
         }
      });
   }

   static void prepare(Minecraft mc, List<String> enabled) {
      enabled.remove("file/bauloo_glint");
   }

   public static void applyLive() {
      StylesUiConfig.load();
      Minecraft mc = Minecraft.getInstance();

      try {
         if (StylesUiConfig.glintCustom) {
            byte[] png = tintedPng();
            if (png == null) {
               return;
            }

            for (Identifier id : TEXTURES) {
               mc.getTextureManager().register(id, new EnchantGlint.GlintTexture(id, NativeImage.read(png)));
            }

            swapped = true;
         } else if (swapped) {
            byte[] plain = plainPng();
            if (plain == null) {
               return;
            }

            for (Identifier id : TEXTURES) {
               mc.getTextureManager().register(id, new EnchantGlint.GlintTexture(id, NativeImage.read(plain)));
            }

            swapped = false;
         }
      } catch (RuntimeException | IOException var6) {
         BaulooStylesClient.LOGGER.warn("Enchant glint could not be applied: {}", var6.toString());
      }
   }

   private static byte[] plainPng() throws IOException {
      Minecraft mc = Minecraft.getInstance();

      for (Identifier id : TEXTURES) {
         Optional<Resource> resource = mc.getResourceManager().getResource(id);
         if (resource.isPresent()) {
            byte[] var7;
            try (InputStream in = resource.get().open()) {
               var7 = in.readAllBytes();
            }

            return var7;
         }
      }

      return null;
   }

   private static byte[] tintedPng() throws IOException {
      byte[] var3;
      try (InputStream in = EnchantGlint.class.getResourceAsStream("/assets/bauloo_styles/glint/white_glint.png")) {
         if (in == null) {
            return null;
         }

         BufferedImage out = tint(ImageIO.read(in));
         ByteArrayOutputStream bytes = new ByteArrayOutputStream();
         ImageIO.write(out, "png", bytes);
         var3 = bytes.toByteArray();
      }

      return var3;
   }

   private static BufferedImage tint(BufferedImage base) {
      int w = base.getWidth();
      int h = base.getHeight();
      BufferedImage out = new BufferedImage(w, h, 2);

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int c = base.getRGB(x, y);
            float light = Math.max(c >> 16 & 0xFF, Math.max(c >> 8 & 0xFF, c & 0xFF)) / 255.0F;
            int color = colorAt(x, y, w, h);
            int r = Math.min(255, Math.round((color >> 16 & 0xFF) * light * 1.15F));
            int g = Math.min(255, Math.round((color >> 8 & 0xFF) * light * 1.15F));
            int b = Math.min(255, Math.round((color & 0xFF) * light * 1.15F));
            out.setRGB(x, y, c & 0xFF000000 | r << 16 | g << 8 | b);
         }
      }

      return out;
   }

   public static int colorAt(int x, int y, int w, int h) {
      float t = (float)(x + y) / Math.max(1, w + h - 2);
      if (StylesUiConfig.glintRainbow) {
         return Color.HSBtoRGB(t, 0.8F, 1.0F) & 16777215;
      } else if (StylesUiConfig.glintGradient) {
         float wave = t < 0.5F ? t * 2.0F : (1.0F - t) * 2.0F;
         return StylesCrosshair.mix(0xFF000000 | StylesUiConfig.glintColor, 0xFF000000 | StylesUiConfig.glintColor2, wave) & 16777215;
      } else {
         return StylesUiConfig.glintColor & 16777215;
      }
   }

   private static final class GlintTexture extends DynamicTexture {
      GlintTexture(Identifier id, NativeImage image) {
         super(id::toString, image);
         this.sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.LINEAR);
      }
   }
}
