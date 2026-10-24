package dev.lego.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lego.cosmetic.KnifeView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.3: knife instead of a sword (or axe, see "Messer-Ansicht") in first person. For this frame the
 * main-hand item in the render state is taken away, so the game draws its own empty-hand arm;
 * FirstPersonKnifeMixin then puts the LEGO knife into that hand.
 */
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class ItemInHandKnifeMixin {
   @Unique
   private static ItemStack lego$hidden;

   @Inject(method = "submitHandsWithItems", at = @At("HEAD"), require = 0)
   private void lego$hideItem(float partialTick, PoseStack pose, SubmitNodeCollector out, PlayerRenderState player, FirstPersonHandsAndItemsRenderState hands, CallbackInfo ci) {
      lego$hidden = null;
      try {
         ItemStack stack = hands.mainHandItem;
         Minecraft mc = Minecraft.getInstance();
         if (stack == null || stack.isEmpty() || mc.player == null || mc.player.getMainArm() != HumanoidArm.RIGHT || !KnifeView.replaces(stack)) return;
         if (dev.lego.core.Compat.vivecraft || dev.lego.compat.VisorBridge.active()) return;
         lego$hidden = stack;
         hands.mainHandItem = ItemStack.EMPTY;
         KnifeView.replacing = true;
      } catch (Throwable ignored) {
      }
   }

   @Inject(method = "submitHandsWithItems", at = @At("RETURN"), require = 0)
   private void lego$restoreItem(float partialTick, PoseStack pose, SubmitNodeCollector out, PlayerRenderState player, FirstPersonHandsAndItemsRenderState hands, CallbackInfo ci) {
      if (lego$hidden == null) return;
      hands.mainHandItem = lego$hidden;
      lego$hidden = null;
      KnifeView.replacing = false;
   }
}
