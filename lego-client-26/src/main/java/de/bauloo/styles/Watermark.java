package de.bauloo.styles;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AfterExtract;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AfterInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;

public final class Watermark {
   private static final Identifier GLOW = Identifier.fromNamespaceAndPath("bauloo_styles", "textures/gui/watermark_glow.png");
   private static final Identifier BADGE = Identifier.fromNamespaceAndPath("bauloo_styles", "textures/gui/watermark_badge.png");
   public static final String[] SOCIALS = new String[]{"youtube", "tiktok", "twitch", "instagram", "x", "kick"};
   private static final int NAME_SPACE = 43;

   private Watermark() {
   }

   public static void register() {
      ScreenEvents.AFTER_INIT.register((AfterInit)(client, screen, width, height) -> {
         if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen) {
            ScreenEvents.afterExtract(screen).register((AfterExtract)(s, ctx, mouseX, mouseY, delta) -> {
               StylesUiConfig.load();
               if (StylesUiConfig.watermark) {
                  draw(ctx, 6, 6, StylesUiConfig.watermarkStyle);
               }
            });
         }
      });
   }

   public static void draw(GuiGraphicsExtractor ctx, int x, int y, int style) {
      if (style == 2) {
         ctx.blit(RenderPipelines.GUI_TEXTURED, BADGE, x, y, 0.0F, 0.0F, 100, 28, 400, 112, 400, 112);
      } else {
         ctx.blit(RenderPipelines.GUI_TEXTURED, GLOW, x, y, 0.0F, 0.0F, 110, 30, 440, 120, 440, 120);
      }
   }

   public static Identifier icon(String social) {
      return Identifier.fromNamespaceAndPath("bauloo_styles", "textures/gui/social/" + social + ".png");
   }

   public static boolean validSocial(String social) {
      for (String s : SOCIALS) {
         if (s.equals(social)) {
            return true;
         }
      }

      return false;
   }

   public static boolean drawBadge(GuiGraphicsExtractor ctx, AbstractWidget button) {
      StylesUiConfig.load();
      String social = StylesUiConfig.socialLogo;
      if (!validSocial(social)) {
         return false;
      } else {
         int bx = button.getX();
         int by = button.getY();
         int size = Math.min(button.getWidth(), button.getHeight());
         int tint = button.isHovered() ? -1 : -1184275;
         ctx.blit(
            RenderPipelines.GUI_TEXTURED,
            icon(social),
            bx + (button.getWidth() - size) / 2,
            by + (button.getHeight() - size) / 2,
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
            Font font = Minecraft.getInstance().font;
            int left = bx + button.getWidth() + 3;
            int width = font.width(name);
            int top = by + (button.getHeight() - 8) / 2;
            if (width <= 43) {
               ctx.text(font, name, left, top, -12566464, false);
            } else {
               double gui = Minecraft.getInstance().getWindow().getGuiScale();
               int k = Math.max(1, (int)Math.floor(gui * 43.0 / width));
               float scale = (float)(k / gui);
               float y = (float)(Math.round((by + (button.getHeight() - 8.0F * scale) / 2.0F) * gui) / gui);
               Matrix3x2fStack m = ctx.pose();
               m.pushMatrix();
               m.translate(left, y);
               m.scale(scale, scale);
               ctx.text(font, font.plainSubstrByWidth(name, (int)(43.0F / scale)), 0, 0, -12566464, false);
               m.popMatrix();
            }
         }

         return true;
      }
   }
}
