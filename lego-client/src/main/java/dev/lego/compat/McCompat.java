package dev.lego.compat;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.joml.Quaternionf;

/**
 * Minecraft calls that differ between game versions (this is the 1.21.11 version; the 26.3
 * build has its own copy in lego-client-26). Shared code calls these instead of the game.
 */
public final class McCompat {
   private McCompat() {
   }

   /** Rotates the pose (1.21.11: mulPose, 26.3: rotate). */
   public static void rotate(PoseStack pose, Quaternionf q) {
      pose.mulPose(q);
   }

   public static KeyMapping registerKey(KeyMapping key) {
      return KeyBindingHelper.registerKeyBinding(key);
   }

   public static InputConstants.Key boundKey(KeyMapping key) {
      return KeyBindingHelper.getBoundKeyOf(key);
   }

   /** The game's current full-screen post effect (or null). */
   public static net.minecraft.resources.Identifier currentPostEffect(net.minecraft.client.renderer.GameRenderer gr) {
      return gr.currentPostEffect();
   }

   public static void clearPostEffect(net.minecraft.client.renderer.GameRenderer gr) {
      gr.clearPostEffect();
   }

   /** Far plane of the world camera in blocks. */
   public static float depthFar(net.minecraft.client.renderer.GameRenderer gr) {
      return gr.getDepthFar();
   }
}
