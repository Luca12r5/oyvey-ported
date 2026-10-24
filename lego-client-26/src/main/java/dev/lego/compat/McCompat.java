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

   /**
    * Key mapping with a GLFW default key. 26.3 reads keys through SDL3 with its own codes; a raw
    * GLFW number there is out of range and crashes KeyMapping.setAll when a world opens.
    */
   public static KeyMapping keyMapping(String name, int glfw, KeyMapping.Category category) {
      return new KeyMapping(name, InputConstants.Type.KEYBOARD, Keys.toGame(glfw), category);
   }

   /**
    * GLFW mouse button of a 26.3 (SDL3) button: SDL counts 1 left, 2 middle, 3 right; the shared
    * UI code expects GLFW's 0 left, 1 right, 2 middle (with SDL's numbers no click ever hit).
    */
   public static int mouseButton(int button) {
      return switch (button) {
         case 1 -> 0;
         case 2 -> 2;
         case 3 -> 1;
         default -> Math.max(-1, button - 1);
      };
   }

   /** GLFW modifier bits of SDL3's (left/right pairs: shift 3, ctrl 192, alt 768, super 3072). */
   public static int glfwMods(int mods) {
      int r = 0;
      if ((mods & InputConstants.MOD_SHIFT) != 0) r |= 1;
      if ((mods & InputConstants.MOD_CONTROL) != 0) r |= 2;
      if ((mods & InputConstants.MOD_ALT) != 0) r |= 4;
      if ((mods & InputConstants.MOD_SUPER) != 0) r |= 8;
      return r;
   }

   /** GLFW code of a 26.3 key code (-1 when it has none). */
   public static int glfwKey(int code) {
      return Keys.toGlfw(code);
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
