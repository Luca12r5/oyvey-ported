package dev.lego.compat;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.joml.Quaternionf;

/** Minecraft 26.3 versions of the calls that differ between game versions. */
public final class McCompat {
   private McCompat() {
   }

   public static void rotate(PoseStack pose, Quaternionf q) {
      pose.rotate(q);
   }

   public static KeyMapping registerKey(KeyMapping key) {
      return KeyMappingHelper.registerKeyMapping(key);
   }

   public static InputConstants.Key boundKey(KeyMapping key) {
      return KeyMappingHelper.getBoundKeyOf(key);
   }

   /** 26.3: post effects are requested per frame through the render state; the LEGO filters are off for now. */
   public static net.minecraft.resources.Identifier currentPostEffect(net.minecraft.client.renderer.GameRenderer gr) {
      return null;
   }

   public static void clearPostEffect(net.minecraft.client.renderer.GameRenderer gr) {
   }

   /** Render distance in blocks times four, like the old far plane. */
   public static float depthFar(net.minecraft.client.renderer.GameRenderer gr) {
      try {
         return net.minecraft.client.Minecraft.getInstance().options.renderDistance().get() * 64.0F;
      } catch (Throwable e) {
         return 512.0F;
      }
   }
}
