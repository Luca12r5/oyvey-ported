package de.bauloo.styles.mixin;

import com.mojang.blaze3d.textures.GpuTextureView;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.class_7764.class_12298;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_12298.class)
public interface SpriteAnimatorAccessor {
   @Accessor("field_64239")
   Int2ObjectMap<GpuTextureView> baulooStyles$frames();
}
