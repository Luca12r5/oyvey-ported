package dev.lego.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MouseHandler.class)
public interface SpatialMouseAccessor {
   @Accessor("mouseGrabbed")
   void lego$setMouseGrabbed(boolean var1);

   @Accessor("mouseGrabbed")
   boolean lego$getMouseGrabbed();

   @Accessor("ignoreFirstMove")
   boolean lego$getIgnoreFirstMove();

   @Accessor("xpos")
   double lego$getRawXpos();

   @Accessor("ypos")
   double lego$getRawYpos();

   @Accessor("xpos")
   void lego$setRawXpos(double var1);

   @Accessor("ypos")
   void lego$setRawYpos(double var1);
}
