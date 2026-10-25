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
   private ItemStack mainHandItem;
   @Shadow
   private float mainHandHeight;
   @Shadow
   private float oMainHandHeight;
   @Unique
   private ItemStack baulooStyles$realMainHand;
   @Unique
   private float baulooStyles$realProgress;
   @Unique
   private float baulooStyles$realLastProgress;

   @Inject(
      method = {"submitHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V"},
      at = {@At("HEAD")}
   )
   private void baulooStyles$showPreview(float tickProgress, PoseStack matrices, SubmitNodeCollector queue, LocalPlayer player, int light, CallbackInfo ci) {
      ItemStack preview = HandPreview.stack();
      if (preview != null) {
         this.baulooStyles$realMainHand = this.mainHandItem;
         this.baulooStyles$realProgress = this.mainHandHeight;
         this.baulooStyles$realLastProgress = this.oMainHandHeight;
         this.mainHandItem = preview;
         this.mainHandHeight = 1.0F;
         this.oMainHandHeight = 1.0F;
      }
   }

   @Inject(
      method = {"submitHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V"},
      at = {@At("RETURN")}
   )
   private void baulooStyles$restore(float tickProgress, PoseStack matrices, SubmitNodeCollector queue, LocalPlayer player, int light, CallbackInfo ci) {
      if (this.baulooStyles$realMainHand != null) {
         this.mainHandItem = this.baulooStyles$realMainHand;
         this.mainHandHeight = this.baulooStyles$realProgress;
         this.oMainHandHeight = this.baulooStyles$realLastProgress;
         this.baulooStyles$realMainHand = null;
      }
   }
}
