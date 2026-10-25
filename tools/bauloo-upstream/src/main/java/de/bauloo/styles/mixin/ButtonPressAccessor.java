package de.bauloo.styles.mixin;

import net.minecraft.class_4185;
import net.minecraft.class_4185.class_4241;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_4185.class)
public interface ButtonPressAccessor {
   @Accessor("field_22767")
   class_4241 baulooStyles$onPress();
}
