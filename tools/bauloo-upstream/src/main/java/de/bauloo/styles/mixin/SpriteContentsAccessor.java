package de.bauloo.styles.mixin;

import net.minecraft.class_1011;
import net.minecraft.class_7764;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_7764.class)
public interface SpriteContentsAccessor {
   @Accessor("field_40539")
   class_1011 baulooStyles$image();
}
