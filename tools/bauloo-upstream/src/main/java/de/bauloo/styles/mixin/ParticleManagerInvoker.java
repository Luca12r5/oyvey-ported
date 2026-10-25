package de.bauloo.styles.mixin;

import net.minecraft.class_2394;
import net.minecraft.class_702;
import net.minecraft.class_703;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_702.class)
public interface ParticleManagerInvoker {
   @Invoker("method_3055")
   class_703 baulooStyles$create(class_2394 var1, double var2, double var4, double var6, double var8, double var10, double var12);
}
