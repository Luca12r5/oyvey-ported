package dev.lego.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
   value = {MouseHandler.class},
   remap = false
)
public interface SpatialMouseAccessor {
   @Accessor("field_1783")
   void lego$setMouseGrabbed(boolean var1);

   @Accessor("field_1783")
   boolean lego$getMouseGrabbed();

   @Accessor("field_1784")
   boolean lego$getIgnoreFirstMove();

   @Accessor("field_1795")
   double lego$getRawXpos();

   @Accessor("field_1794")
   double lego$getRawYpos();

   @Accessor("field_1795")
   void lego$setRawXpos(double var1);

   @Accessor("field_1794")
   void lego$setRawYpos(double var1);
}
