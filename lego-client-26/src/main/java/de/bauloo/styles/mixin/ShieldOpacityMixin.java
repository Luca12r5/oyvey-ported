package de.bauloo.styles.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.bauloo.styles.ShieldContext;
import de.bauloo.styles.StylesUiConfig;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({ShieldSpecialRenderer.class})
public class ShieldOpacityMixin {
   @Redirect(
      method = {"submit"},
      at = {@At(
         value = "INVOKE",
         ordinal = 0,
         target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;IIILnet/minecraft/client/resources/model/sprite/SpriteId;Lnet/minecraft/client/resources/model/sprite/SpriteGetter;I)V"
      )}
   )
   private void baulooStyles$base(
      SubmitNodeCollector collector,
      Model model,
      Object state,
      PoseStack poseStack,
      int light,
      int overlay,
      int tint,
      SpriteId sprite,
      SpriteGetter sprites,
      int outline
   ) {
      StylesUiConfig.load();
      if (StylesUiConfig.shieldCustom) {
         tint = tint & 0xFF000000 | StylesUiConfig.shieldColor & 16777215;
      }

      ItemDisplayContext context = ShieldContext.current;
      boolean inHand = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
         || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
         || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
         || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
      if (inHand && StylesUiConfig.shieldOpacity < 100) {
         int alpha = Math.round(255 * Math.max(15, StylesUiConfig.shieldOpacity) / 100.0F);
         // 26.3: the translucent pass takes the sprite as a UvMapping and has no crumbling overlay.
         if ((Object)sprites.get(sprite) instanceof net.minecraft.client.renderer.texture.UvMapping uv) {
            collector.submitModel(model, state, poseStack, RenderTypes.entityTranslucent(sprite.atlasLocation()), light, overlay, alpha << 24 | tint & 16777215, uv, outline);
         } else {
            collector.submitModel(model, state, poseStack, light, overlay, tint, sprite, sprites, outline);
         }
      } else {
         collector.submitModel(model, state, poseStack, light, overlay, tint, sprite, sprites, outline);
      }
   }
}
