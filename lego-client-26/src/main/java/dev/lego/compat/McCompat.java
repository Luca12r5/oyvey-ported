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

   /**
    * 26.3 keeps a list of requested full-screen post effects (spectator view, server effects…). The
    * client's color filter is one entry in that list, recognised by its "legoclient" namespace.
    */
   public static net.minecraft.resources.Identifier currentPostEffect(net.minecraft.client.renderer.GameRenderer gr) {
      for (net.minecraft.resources.Identifier id : gr.getRequestedPostEffects()) if ("legoclient".equals(id.getNamespace())) return id;
      return null;
   }

   public static void clearPostEffect(net.minecraft.client.renderer.GameRenderer gr) {
      gr.getRequestedPostEffects().removeIf(id -> "legoclient".equals(id.getNamespace()));
   }

   public static void setPostEffect(net.minecraft.client.renderer.GameRenderer gr, net.minecraft.resources.Identifier id) {
      java.util.List<net.minecraft.resources.Identifier> list = gr.getRequestedPostEffects();
      list.removeIf(x -> "legoclient".equals(x.getNamespace()));
      list.add(id);
   }

   /** Render distance in blocks times four, like the old far plane. */
   public static float depthFar(net.minecraft.client.renderer.GameRenderer gr) {
      try {
         return net.minecraft.client.Minecraft.getInstance().options.renderDistance().get() * 64.0F;
      } catch (Throwable e) {
         return 512.0F;
      }
   }

   /** 26.3's Util.OS has no openUri any more: hand https links to the system opener. */
   public static void openUri(String uri) throws java.io.IOException {
      if (!uri.startsWith("https://")) throw new IllegalArgumentException("only https links");
      String os = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT);
      String[] cmd = os.contains("win") ? new String[]{"rundll32", "url.dll,FileProtocolHandler", uri}
         : os.contains("mac") ? new String[]{"open", uri} : new String[]{"xdg-open", uri};
      new ProcessBuilder(cmd).start();
   }
}
