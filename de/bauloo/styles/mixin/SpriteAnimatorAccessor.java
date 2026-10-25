package de.bauloo.styles.mixin;

import com.mojang.blaze3d.textures.GpuTextureView;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.texture.SpriteContents.AnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({AnimationState.class})
public interface SpriteAnimatorAccessor {
   @Accessor("frameTexturesByIndex")
   Int2ObjectMap<GpuTextureView> baulooStyles$frames();
}
