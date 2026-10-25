package de.bauloo.styles.mixin;

import de.bauloo.styles.PlayerOutline;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({LevelRenderer.class})
public class OutlineWidthMixin {
   @ModifyArg(
      method = {"render"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/ShaderManager;getPostChain(Lnet/minecraft/resources/Identifier;Ljava/util/Set;)Lnet/minecraft/client/renderer/PostChain;"
      )},
      index = 0
   )
   private Identifier baulooStyles$width(Identifier id) {
      return PlayerOutline.postEffect(id);
   }
}
