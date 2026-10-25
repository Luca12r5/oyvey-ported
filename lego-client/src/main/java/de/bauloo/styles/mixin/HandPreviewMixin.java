package de.bauloo.styles.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.bauloo.styles.HandPreview;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemInHandRenderer.class})
public class HandPreviewMixin {
   @Shadow
   private ItemStack field_4047;
   @Shadow
   private float field_4043;
   @Shadow
   private float field_4053;
   @Unique
   private ItemStack baulooStyles$realMainHand;
   @Unique
   private float baulooStyles$realProgress;
   @Unique
   private float baulooStyles$realLastProgress;

   @Inject(
      method = {"renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V"},
      at = {@At("HEAD")}
   )
   private void baulooStyles$showPreview(float tickProgress, PoseStack matrices, SubmitNodeCollector queue, LocalPlayer player, int light, CallbackInfo ci) {
      ItemStack preview = HandPreview.stack();
      if (preview != null) {
         this.baulooStyles$realMainHand = this.field_4047;
         this.baulooStyles$realProgress = this.field_4043;
         this.baulooStyles$realLastProgress = this.field_4053;
         this.field_4047 = preview;
         this.field_4043 = 1.0F;
         this.field_4053 = 1.0F;
      }
   }

   @Inject(
      method = {"renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V"},
      at = {@At("RETURN")}
   )
   private void baulooStyles$restore(float tickProgress, PoseStack matrices, SubmitNodeCollector queue, LocalPlayer player, int light, CallbackInfo ci) {
      if (this.baulooStyles$realMainHand != null) {
         this.field_4047 = this.baulooStyles$realMainHand;
         this.field_4043 = this.baulooStyles$realProgress;
         this.field_4053 = this.baulooStyles$realLastProgress;
         this.baulooStyles$realMainHand = null;
      }
   }
}
