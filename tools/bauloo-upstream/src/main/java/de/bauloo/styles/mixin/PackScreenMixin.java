package de.bauloo.styles.mixin;

import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.class_5375;
import net.minecraft.class_5369.class_5371;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(class_5375.class)
public final class PackScreenMixin {
   @ModifyVariable(method = "method_74711", at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private Stream<class_5371> baulooStyles$hideBuiltIns(Stream<class_5371> packs) {
      return packs.filter(
         pack -> {
            String name = pack.method_48276().toLowerCase(Locale.ROOT);
            return !name.startsWith("bauloo_styles:")
               && !name.contains("bauloo_upload_")
               && !name.contains("bauloo_cloud_")
               && !name.contains("bauloo_anchor_glow")
               && !name.contains("bauloo_crystal_core")
               && !name.contains("bauloo_glint")
               && !pack.method_29653().getString().toLowerCase(Locale.ROOT).contains("built-in: bauloo styles");
         }
      );
   }
}
