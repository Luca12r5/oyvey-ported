package de.bauloo.styles.mixin;

import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionModel.Entry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({PackSelectionScreen.class})
public final class PackScreenMixin {
   @ModifyVariable(
      method = {"filterEntries"},
      at = {@At("HEAD")},
      argsOnly = true,
      ordinal = 0
   )
   private Stream<Entry> baulooStyles$hideBuiltIns(Stream<Entry> packs) {
      return packs.filter(
         pack -> {
            String name = pack.getId().toLowerCase(Locale.ROOT);
            return !name.startsWith("bauloo_styles:")
               && !name.contains("bauloo_upload_")
               && !name.contains("bauloo_cloud_")
               && !name.contains("bauloo_anchor_glow")
               && !name.contains("bauloo_crystal_core")
               && !name.contains("bauloo_glint")
               && !pack.getExtendedDescription().getString().toLowerCase(Locale.ROOT).contains("built-in: bauloo styles");
         }
      );
   }
}
