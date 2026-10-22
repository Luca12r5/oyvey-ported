package dev.lego.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lego.cosmetic.CosRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The equipped LEGO knife in your own right hand in first person (only while that hand is empty). */
@Mixin(AvatarRenderer.class)
public abstract class FirstPersonKnifeMixin {
   @Inject(method = "renderRightHand", at = @At("TAIL"), require = 0)
   private void lego$knife(PoseStack pose, SubmitNodeCollector out, int light, Identifier skin, boolean sleeve, CallbackInfo ci) {
      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player == null || !mc.player.getMainHandItem().isEmpty() || !mc.options.getCameraType().isFirstPerson()) return;
         // While the Visor headset is being put on or taken off, the hands hold the headset.
         if (dev.visor.VisorController.get().state() != dev.visor.VisorController.State.OFF) return;
         CosRender.drawKnifeFirstPerson(pose, out, light);
      } catch (Throwable ignored) {
      }
   }
}
