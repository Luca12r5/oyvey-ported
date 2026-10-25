package de.bauloo.styles;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AfterInit;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AfterRender;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_481;
import net.minecraft.class_490;
import org.joml.Matrix3x2fStack;

public final class Watermark {
   private static final class_2960 GLOW = class_2960.method_60655("bauloo_styles", "textures/gui/watermark_glow.png");
   private static final class_2960 BADGE = class_2960.method_60655("bauloo_styles", "textures/gui/watermark_badge.png");
   public static final String[] SOCIALS = new String[]{"youtube", "tiktok", "twitch", "instagram", "x", "kick"};
   private static final int NAME_SPACE = 43;

   private Watermark() {
   }

   public static void register() {
      ScreenEvents.AFTER_INIT.register((AfterInit)(client, screen, width, height) -> {
         if (screen instanceof class_490 || screen instanceof class_481) {
            ScreenEvents.afterRender(screen).register((AfterRender)(s, ctx, mouseX, mouseY, delta) -> {
               StylesUiConfig.load();
               if (StylesUiConfig.watermark) {
                  draw(ctx, 6, 6, StylesUiConfig.watermarkStyle);
               }
            });
         }
      });
   }

   public static void draw(class_332 ctx, int x, int y, int style) {
      if (style == 2) {
         ctx.method_25302(class_10799.field_56883, BADGE, x, y, 0.0F, 0.0F, 100, 28, 400, 112, 400, 112);
      } else {
         ctx.method_25302(class_10799.field_56883, GLOW, x, y, 0.0F, 0.0F, 110, 30, 440, 120, 440, 120);
      }
   }

   public static class_2960 icon(String social) {
      return class_2960.method_60655("bauloo_styles", "textures/gui/social/" + social + ".png");
   }

   public static boolean validSocial(String social) {
      for (String s : SOCIALS) {
         if (s.equals(social)) {
            return true;
         }
      }

      return false;
   }

   public static boolean drawBadge(class_332 ctx, class_339 button) {
      StylesUiConfig.load();
      String social = StylesUiConfig.socialLogo;
      if (!validSocial(social)) {
         return false;
      }

      int bx = button.method_46426();
      int by = button.method_46427();
      int size = Math.min(button.method_25368(), button.method_25364());
      int tint = button.method_49606() ? -1 : -1184275;
      ctx.method_25293(
         class_10799.field_56883,
         icon(social),
         bx + (button.method_25368() - size) / 2,
         by + (button.method_25364() - size) / 2,
         0.0F,
         0.0F,
         size,
         size,
         128,
         128,
         128,
         128,
         tint
      );
      String name = StylesUiConfig.socialName;
      if (name != null && !name.isBlank()) {
         class_327 font = class_310.method_1551().field_1772;
         int left = bx + button.method_25368() + 3;
         int width = font.method_1727(name);
         int top = by + (button.method_25364() - 8) / 2;
         if (width <= 43) {
            ctx.method_51433(font, name, left, top, -12566464, false);
         } else {
            double gui = class_310.method_1551().method_22683().method_4495();
            int k = Math.max(1, (int)Math.floor(gui * 43.0 / width));
            float scale = (float)(k / gui);
            float y = (float)(Math.round((by + (button.method_25364() - 8.0F * scale) / 2.0F) * gui) / gui);
            Matrix3x2fStack m = ctx.method_51448();
            m.pushMatrix();
            m.translate(left, y);
            m.scale(scale, scale);
            ctx.method_51433(font, font.method_27523(name, (int)(43.0F / scale)), 0, 0, -12566464, false);
            m.popMatrix();
         }
      }

      return true;
   }
}
