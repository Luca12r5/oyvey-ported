package de.bauloo.styles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
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
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3264;
import net.minecraft.class_3298;
import net.minecraft.class_3300;

public final class EnchantGlint {
   public static final String PACK_ID = "file/bauloo_glint";
   private static final class_2960[] TEXTURES = new class_2960[]{
      class_2960.method_60656("textures/misc/enchanted_glint_item.png"), class_2960.method_60656("textures/misc/enchanted_glint_armor.png")
   };
   private static boolean swapped;

   private EnchantGlint() {
   }

   public static void register() {
      ResourceManagerHelper.get(class_3264.field_14188).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
         public class_2960 getFabricId() {
            return class_2960.method_60655("bauloo_styles", "enchant_glint");
         }

         public Collection<class_2960> getFabricDependencies() {
            return List.of(ResourceReloadListenerKeys.TEXTURES);
         }

         public void method_14491(class_3300 manager) {
            EnchantGlint.swapped = false;
            class_310.method_1551().method_63588(EnchantGlint::applyLive);
         }
      });
   }

   static void prepare(class_310 mc, List<String> enabled) {
      enabled.remove("file/bauloo_glint");
   }

   public static void applyLive() {
      StylesUiConfig.load();
      class_310 mc = class_310.method_1551();

      try {
         if (StylesUiConfig.glintCustom) {
            byte[] png = tintedPng();
            if (png == null) {
               return;
            }

            for (class_2960 id : TEXTURES) {
               mc.method_1531().method_4616(id, new EnchantGlint.GlintTexture(id, class_1011.method_49277(png)));
            }

            swapped = true;
         } else if (swapped) {
            byte[] plain = plainPng();
            if (plain == null) {
               return;
            }

            for (class_2960 id : TEXTURES) {
               mc.method_1531().method_4616(id, new EnchantGlint.GlintTexture(id, class_1011.method_49277(plain)));
            }

            swapped = false;
         }
      } catch (IOException | RuntimeException e) {
         BaulooStylesClient.LOGGER.warn("Enchant glint could not be applied: {}", e.toString());
      }
   }

   private static byte[] plainPng() throws IOException {
      class_310 mc = class_310.method_1551();

      for (class_2960 id : TEXTURES) {
         Optional<class_3298> resource = mc.method_1478().method_14486(id);
         if (resource.isPresent()) {
            try (InputStream in = resource.get().method_14482()) {
               return in.readAllBytes();
            }
         }
      }

      return null;
   }

   private static byte[] tintedPng() throws IOException {
      try (InputStream in = EnchantGlint.class.getResourceAsStream("/assets/bauloo_styles/glint/white_glint.png")) {
         if (in == null) {
            return null;
         }

         BufferedImage out = tint(ImageIO.read(in));
         ByteArrayOutputStream bytes = new ByteArrayOutputStream();
         ImageIO.write(out, "png", bytes);
         return bytes.toByteArray();
      }
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

   private static final class GlintTexture extends class_1043 {
      GlintTexture(class_2960 id, class_1011 image) {
         super(id::toString, image);
         this.field_63613 = RenderSystem.getSamplerCache().method_75297(FilterMode.LINEAR);
      }
   }
}
