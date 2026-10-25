package de.bauloo.styles.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import de.bauloo.styles.HandPreview;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemInHandRenderer.class})
public class HandPreviewBlockMixin {
   private static final String METHOD = "submitArmWithItem";
   @Unique
   private boolean baulooStyles$blocking;

   @Inject(
      method = {"submitArmWithItem"},
      at = {@At("HEAD")}
   )
   private void baulooStyles$start(
      AbstractClientPlayer player,
      float tickProgress,
      float pitch,
      InteractionHand hand,
      float swingProgress,
      ItemStack stack,
      float equipProgress,
      PoseStack matrices,
      SubmitNodeCollector queue,
      int light,
      CallbackInfo ci
   ) {
      ItemStack preview = HandPreview.stack();
      this.baulooStyles$blocking = hand == InteractionHand.MAIN_HAND && preview != null && preview.is(Items.SHIELD) && stack.is(Items.SHIELD);
   }

   @WrapOperation(
      method = {"submitArmWithItem"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/player/AbstractClientPlayer;isUsingItem()Z"
      )}
   )
   private boolean baulooStyles$using(AbstractClientPlayer player, Operation<Boolean> original) {
      return this.baulooStyles$blocking || (Boolean)original.call(new Object[]{player});
   }

   @WrapOperation(
      method = {"submitArmWithItem"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/player/AbstractClientPlayer;getUseItemRemainingTicks()I"
      )}
   )
   private int baulooStyles$timeLeft(AbstractClientPlayer player, Operation<Integer> original) {
      return this.baulooStyles$blocking ? 72000 : (Integer)original.call(new Object[]{player});
   }

   @WrapOperation(
      method = {"submitArmWithItem"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/player/AbstractClientPlayer;getUsedItemHand()Lnet/minecraft/world/InteractionHand;"
      )}
   )
   private InteractionHand baulooStyles$activeHand(AbstractClientPlayer player, Operation<InteractionHand> original) {
      return this.baulooStyles$blocking ? InteractionHand.MAIN_HAND : (InteractionHand)original.call(new Object[]{player});
   }
}
