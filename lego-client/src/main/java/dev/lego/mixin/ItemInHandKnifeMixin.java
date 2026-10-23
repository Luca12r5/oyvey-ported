package dev.lego.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.lego.cosmetic.KnifeView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * First person: while a sword (or axe, see the "Messer-Ansicht" module) is in the main hand and a
 * LEGO knife is equipped, the arm holding the knife is drawn instead of the item. The arm uses
 * the same pose as vanilla's empty hand; the knife itself is added in FirstPersonKnifeMixin.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandKnifeMixin {
   @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true, require = 0)
   private void lego$knifeInsteadOfItem(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swing, ItemStack stack,
         float equip, PoseStack pose, SubmitNodeCollector out, int light, CallbackInfo ci) {
      try {
         if (hand != InteractionHand.MAIN_HAND || stack.isEmpty() || player.getMainArm() != HumanoidArm.RIGHT || !KnifeView.replaces(stack)) return;
         if (dev.lego.core.Compat.vivecraft || dev.lego.compat.VisorBridge.active()) return;
         ci.cancel();
         // Vanilla's empty-hand arm pose (right arm).
         float g = Mth.sqrt(swing);
         float h = -0.3F * Mth.sin(g * (float)Math.PI);
         float i = 0.4F * Mth.sin(g * ((float)Math.PI * 2.0F));
         float j = -0.4F * Mth.sin(swing * (float)Math.PI);
         pose.pushPose();
         pose.translate(h + 0.64000005F, i - 0.6F + equip * -0.6F, j - 0.71999997F);
         pose.mulPose(Axis.YP.rotationDegrees(45.0F));
         float k = Mth.sin(swing * swing * (float)Math.PI);
         float l = Mth.sin(g * (float)Math.PI);
         pose.mulPose(Axis.YP.rotationDegrees(l * 70.0F));
         pose.mulPose(Axis.ZP.rotationDegrees(k * -20.0F));
         pose.translate(-1.0F, 3.6F, 3.5F);
         pose.mulPose(Axis.ZP.rotationDegrees(120.0F));
         pose.mulPose(Axis.XP.rotationDegrees(200.0F));
         pose.mulPose(Axis.YP.rotationDegrees(-135.0F));
         pose.translate(5.6F, 0.0F, 0.0F);
         AvatarRenderer<AbstractClientPlayer> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getPlayerRenderer(player);
         KnifeView.replacing = true;
         try {
            renderer.renderRightHand(pose, out, light, player.getSkin().body().texturePath(), player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE));
         } finally {
            KnifeView.replacing = false;
            pose.popPose();
         }
      } catch (Throwable ignored) {
      }
   }
}
