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

   /** Key mapping with a GLFW default key (shared code always speaks GLFW codes). */
   public static KeyMapping keyMapping(String name, int glfw, KeyMapping.Category category) {
      return new KeyMapping(name, glfw, category);
   }

   /** GLFW mouse button (0 left, 1 right, 2 middle) of a game mouse button (the same on 1.21.11). */
   public static int mouseButton(int button) {
      return button;
   }

   /** GLFW modifier bits (1 shift, 2 ctrl, 4 alt, 8 super) of the game's (the same on 1.21.11). */
   public static int glfwMods(int mods) {
      return mods;
   }

   /** GLFW code of a game key code (the same on 1.21.11). */
   public static int glfwKey(int code) {
      return code;
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

   private static java.lang.reflect.Method setPost;

   /** Puts one of the client's full-screen post effects (color filters) on. */
   public static void setPostEffect(net.minecraft.client.renderer.GameRenderer gr, net.minecraft.resources.Identifier id) throws ReflectiveOperationException {
      if (setPost == null) {
         setPost = net.minecraft.client.renderer.GameRenderer.class.getDeclaredMethod(dev.lego.util.Remap.method("net.minecraft.class_757", "method_62904", "(Lnet/minecraft/class_2960;)V"), net.minecraft.resources.Identifier.class);
         setPost.setAccessible(true);
      }
      setPost.invoke(gr, id);
   }

   /** Far plane of the world camera in blocks. */
   public static float depthFar(net.minecraft.client.renderer.GameRenderer gr) {
      return gr.getDepthFar();
   }
}
