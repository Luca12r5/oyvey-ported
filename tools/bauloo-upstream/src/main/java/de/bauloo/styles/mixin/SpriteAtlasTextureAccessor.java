package de.bauloo.styles.mixin;

import java.util.Map;
import net.minecraft.class_1058;
import net.minecraft.class_1059;
import net.minecraft.class_2960;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_1059.class)
public interface SpriteAtlasTextureAccessor {
   @Accessor("field_5280")
   Map<class_2960, class_1058> baulooStyles$sprites();
}
