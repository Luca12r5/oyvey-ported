package de.bauloo.styles.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.bauloo.styles.StylesUiConfig;
import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ShieldSpecialRenderer.class})
public class ShieldOpacityMixin {
   @Unique
   private boolean baulooStyles$seeThrough;

   @Inject(
      method = {"submit(Lnet/minecraft/core/component/DataComponentMap;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V"},
      at = {@At("HEAD")}
   )
   private void baulooStyles$start(
      DataComponentMap data,
      ItemDisplayContext context,
      PoseStack matrices,
      SubmitNodeCollector queue,
      int light,
      int overlay,
      boolean glint,
      int seed,
      CallbackInfo ci
   ) {
      StylesUiConfig.load();
      this.baulooStyles$seeThrough = (
            context.firstPerson() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
         )
         && StylesUiConfig.shieldOpacity < 100;
   }

   @Redirect(
      method = {"submit(Lnet/minecraft/core/component/DataComponentMap;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/model/object/equipment/ShieldModel;renderType(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"
      )
   )
   private RenderType baulooStyles$layer(ShieldModel model, Identifier texture) {
      return this.baulooStyles$seeThrough ? RenderTypes.entityTranslucent(texture) : model.renderType(texture);
   }

   @ModifyArg(
      method = {"submit(Lnet/minecraft/core/component/DataComponentMap;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ZZILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;I)V"
      ),
      index = 8
   )
   private int baulooStyles$tint(int tint) {
      if (StylesUiConfig.shieldCustom) {
         tint = tint & 0xFF000000 | StylesUiConfig.shieldColor & 16777215;
      }

      if (!this.baulooStyles$seeThrough) {
         return tint;
      } else {
         int alpha = Math.round(255 * Math.max(15, StylesUiConfig.shieldOpacity) / 100.0F);
         return alpha << 24 | tint & 16777215;
      }
   }
}
