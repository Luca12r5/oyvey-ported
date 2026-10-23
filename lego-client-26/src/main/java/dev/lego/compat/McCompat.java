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
}
