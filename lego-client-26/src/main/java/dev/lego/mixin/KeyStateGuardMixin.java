package dev.lego.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.3 reads InputConstants.isKeyDown straight from SDL's keyboard state array, so any key code
 * outside it (an unbound or old GLFW code from a config) throws and crashes KeyMapping.setAll
 * when a world opens. Such keys simply count as not pressed.
 */
@Mixin(InputConstants.class)
public abstract class KeyStateGuardMixin {
   /** SDL_SCANCODE_COUNT: size of SDL_GetKeyboardState. */
   @Unique
   private static final int LEGO$SCANCODES = 512;
   @Unique
   private static boolean lego$logged;

   @Inject(method = "isKeyDown(I)Z", at = @At("HEAD"), cancellable = true, require = 0)
   private static void lego$guard(int key, CallbackInfoReturnable<Boolean> cir) {
      if (key >= 0 && key < LEGO$SCANCODES) return;
      if (!lego$logged) {
         lego$logged = true;
         System.out.println("[LegoClient] key code " + key + " is outside the 26.3 keyboard state, counted as not pressed");
      }
      cir.setReturnValue(false);
   }
}
