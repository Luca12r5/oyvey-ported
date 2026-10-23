package dev.lego.compat;

import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * With Vivecraft (real VR) installed, the client's mixins into Minecraft's render pipeline are not
 * applied at all. Vivecraft rewrites the same methods (GameRenderer, LevelRenderer, ItemInHandRenderer,
 * Gui, Camera, mouse/keyboard input) for its stereo rendering; hooking them as well can break or crash
 * the game when the headset starts. What stays: cosmetics and emotes on the player model, the network
 * sync, the window title, the FOV range and the Visor sound accessors (only fields, no render code).
 */
public final class VrMixinPlugin implements IMixinConfigPlugin {
   private static final Set<String> KEEP_IN_VR = Set.of(
      "dev.lego.mixin.NetworkHandlerMixin", "dev.lego.mixin.PlayerModelMixin", "dev.lego.mixin.PlayerRendererMixin",
      "dev.lego.mixin.EntityMixin", "dev.lego.mixin.WindowTitleMixin", "dev.lego.mixin.FovRangeMixin",
      "dev.visor.mixin.ChannelAccessor", "dev.visor.mixin.SoundEngineAccessor", "dev.visor.mixin.SoundManagerAccessor"
   );
   private static boolean vr;

   @Override
   public void onLoad(String mixinPackage) {
      try {
         vr = FabricLoader.getInstance().isModLoaded("vivecraft");
      } catch (Throwable e) {
         vr = false;
      }
   }

   /** Whether the client runs next to Vivecraft (usable before the client's own init). */
   public static boolean vr() {
      return vr;
   }

   @Override
   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      if (!vr || KEEP_IN_VR.contains(mixinClassName)) return true;
      System.out.println("[LEGO] Vivecraft: " + mixinClassName + " ist im VR-Modus aus");
      return false;
   }

   @Override
   public String getRefMapperConfig() {
      return null;
   }

   @Override
   public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
   }

   @Override
   public List<String> getMixins() {
      return null;
   }

   @Override
   public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }

   @Override
   public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }
}
