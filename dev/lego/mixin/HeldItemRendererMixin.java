package dev.lego.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lego.core.Mc;
import dev.lego.visual.Visuals2;
import dev.lego.visual.Visuals3;
import dev.lego.visual.shader.HandEffect;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {ItemInHandRenderer.class},
   remap = false
)
public abstract class HeldItemRendererMixin {
   private static boolean lego$needsEffect() {
      if (Visuals2.shaderHandsEnabled()) {
         return true;
      } else if (Visuals3.totemGhost != null && Visuals3.totemGhost.enabled) {
         LocalPlayer me = Mc.player();
         if (me == null) {
            return false;
         } else {
            ItemStack main = me.getMainHandItem();
            ItemStack off = me.getOffhandItem();
            boolean mainT = !Mc.empty(main) && Visuals3.totemGhost.shouldBeTransparent(main);
            boolean offT = !Mc.empty(off) && Visuals3.totemGhost.shouldBeTransparent(off);
            return mainT || offT;
         }
      } else {
         return false;
      }
   }

   @Inject(
      method = {"method_22976(FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_746;I)V"},
      at = {@At("HEAD")},
      remap = false,
      require = 0
   )
   private void lego$preRenderItem(float tickDelta, PoseStack matrices, SubmitNodeCollector collector, LocalPlayer player, int light, CallbackInfo ci) {
      try {
         if (lego$needsEffect()) {
            HandEffect.getInstance().captureBeforeHands();
         }
      } catch (Throwable var8) {
      }
   }

   @Inject(
      method = {"method_22976(FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_746;I)V"},
      at = {@At("TAIL")},
      remap = false,
      require = 0
   )
   private void lego$postRenderItem(float tickDelta, PoseStack matrices, SubmitNodeCollector collector, LocalPlayer player, int light, CallbackInfo ci) {
      try {
         if (lego$needsEffect()) {
            HandEffect he = HandEffect.getInstance();
            he.captureAfterHands();
            he.compositeEffects();
         }
      } catch (Throwable var8) {
      }
   }
}
