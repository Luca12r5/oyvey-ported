package de.bauloo.styles.mixin;

import de.bauloo.styles.Watermark;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ImageButton.class})
public abstract class SocialBadgeMixin {
   @Shadow
   @Final
   protected WidgetSprites sprites;

   @Inject(
      method = {"renderContents"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void baulooStyles$badge(GuiGraphics ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (this.sprites.enabled().getPath().contains("recipe_book") && Watermark.drawBadge(ctx, (ImageButton)(Object)this)) {
         ci.cancel();
      }
   }
}
