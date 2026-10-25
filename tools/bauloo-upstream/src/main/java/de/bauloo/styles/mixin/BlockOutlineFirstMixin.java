package de.bauloo.styles.mixin;

import de.bauloo.styles.BlockOutline;
import net.minecraft.class_11658;
import net.minecraft.class_12074;
import net.minecraft.class_12249;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_761;
import net.minecraft.class_9974;
import net.minecraft.class_4597.class_4598;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = class_761.class, priority = 100)
public abstract class BlockOutlineFirstMixin {
   @Inject(method = "method_62210", at = @At("HEAD"), cancellable = true)
   private void baulooStyles$ourOutlineFirst(class_4598 consumers, class_4587 matrices, boolean translucent, class_11658 world, CallbackInfo ci) {
      if (BlockOutline.active()) {
         class_12074 outline = world.field_63083;
         ci.cancel();
         if (outline != null && outline.comp_4933() == translucent) {
            class_243 cam = world.field_63082.field_63078;
            class_2338 pos = outline.comp_4932();
            class_4588 lines = consumers.method_73477(class_12249.method_76015());
            class_9974.method_62296(
               matrices,
               lines,
               outline.comp_4935(),
               pos.method_10263() - cam.field_1352,
               pos.method_10264() - cam.field_1351,
               pos.method_10260() - cam.field_1350,
               BlockOutline.color(),
               BlockOutline.width()
            );
            consumers.method_37104();
         }
      }
   }
}
