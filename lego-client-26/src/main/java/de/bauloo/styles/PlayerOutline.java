package de.bauloo.styles;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

public final class PlayerOutline {
   public static volatile boolean previewSelf;
   private static final float EDGE = 0.08F;
   private static final Map<Integer, long[]> SIGHT = new HashMap<>();

   private PlayerOutline() {
   }

   public static boolean outlines(Entity entity) {
      if (previewSelf && entity == Minecraft.getInstance().player) {
         return true;
      } else {
         StylesUiConfig.load();
         if (StylesUiConfig.outline && entity instanceof Player && !entity.isCurrentlyGlowing()) {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) {
               return false;
            } else if (entity == client.player) {
               return StylesUiConfig.outlineSelf;
            } else {
               return entity.isInvisible() && !wearsArmor((Player)entity) ? false : StylesUiConfig.outlineOthers && canSee(client, entity);
            }
         } else {
            return false;
         }
      }
   }

   private static boolean canSee(Minecraft client, Entity entity) {
      long tick = client.level == null ? 0L : client.level.getGameTime();
      long[] cached = SIGHT.get(entity.getId());
      if (cached != null && cached[0] == tick) {
         return cached[1] == 1L;
      } else {
         if (SIGHT.size() > 256) {
            SIGHT.clear();
         }

         boolean visible = client.player.hasLineOfSight(entity);
         SIGHT.put(entity.getId(), new long[]{tick, visible ? 1L : 0L});
         return visible;
      }
   }

   private static boolean wearsArmor(Player player) {
      for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
         if (!player.getItemBySlot(slot).isEmpty()) {
            return true;
         }
      }

      return false;
   }

   public static int sliderColor(float t) {
      if (t < 0.08F) {
         return 16777215;
      } else {
         return t > 0.92F ? 0 : StylesTheme.hsv((t - 0.08F) / 0.84000003F, 0.65F, 1.0F);
      }
   }

   public static float sliderPos(int rgb) {
      rgb &= 16777215;
      float[] hsv = StylesTheme.toHsv(rgb);
      if (hsv[2] < 0.15F) {
         return 0.96F;
      } else {
         return hsv[1] < 0.15F ? 0.04F : 0.08F + hsv[0] * 0.84000003F;
      }
   }

   public static Identifier postEffect(Identifier vanilla) {
      StylesUiConfig.load();
      if (!StylesUiConfig.outline && !previewSelf) {
         return vanilla;
      } else {
         int width = Math.max(1, Math.min(6, StylesUiConfig.outlineWidth));
         return Identifier.fromNamespaceAndPath("bauloo_styles", "neon_" + width);
      }
   }

   public static int color() {
      if (StylesUiConfig.outlineRainbow) {
         float hue = (float)(System.currentTimeMillis() % 4000L) / 4000.0F;
         return Color.HSBtoRGB(hue, 0.7F, 1.0F) & 16777215;
      } else {
         return StylesUiConfig.outlineColor & 16777215;
      }
   }
}
