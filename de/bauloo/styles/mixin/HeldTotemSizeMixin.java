package de.bauloo.styles.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.bauloo.styles.StylesUiConfig;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemInHandRenderer.class})
public class HeldTotemSizeMixin {
   @Unique
   private boolean baulooStyles$scaled;

   @Inject(
      method = {"renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"},
      at = {@At("HEAD")}
   )
   private void baulooStyles$scaleTotem(
      LivingEntity entity, ItemStack stack, ItemDisplayContext context, PoseStack matrices, SubmitNodeCollector queue, int light, CallbackInfo ci
   ) {
      StylesUiConfig.load();
      int size = !context.firstPerson()
         ? 100
         : (
            stack.is(Items.TOTEM_OF_UNDYING)
               ? StylesUiConfig.totemSize
               : (
                  stack.is(Items.SHIELD)
                     ? StylesUiConfig.shieldSize
                     : (!stack.is(Items.GOLDEN_APPLE) && !stack.is(Items.ENCHANTED_GOLDEN_APPLE) ? 100 : StylesUiConfig.gapSize)
               )
         );
      this.baulooStyles$scaled = size != 100;
      if (this.baulooStyles$scaled) {
         float f = size / 100.0F;
         matrices.pushPose();
         matrices.scale(f, f, f);
      }
   }

   @Inject(
      method = {"renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"},
      at = {@At("RETURN")}
   )
   private void baulooStyles$restore(
      LivingEntity entity, ItemStack stack, ItemDisplayContext context, PoseStack matrices, SubmitNodeCollector queue, int light, CallbackInfo ci
   ) {
      if (this.baulooStyles$scaled) {
         this.baulooStyles$scaled = false;
         matrices.popPose();
      }
   }
}
