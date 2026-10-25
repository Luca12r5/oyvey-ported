package de.bauloo.styles.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.bauloo.styles.StylesPresence;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(
   value = {PlayerTabOverlay.class},
   priority = 1500
)
public class TabIconMixin {
   @WrapOperation(
      method = {"render"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;getNameForDisplay(Lnet/minecraft/client/multiplayer/PlayerInfo;)Lnet/minecraft/network/chat/Component;"
      )}
   )
   private Component baulooStyles$tornado(PlayerTabOverlay self, PlayerInfo entry, Operation<Component> original) {
      Component name = original.call(self, entry);
      Component icon = StylesPresence.icon(entry.getProfile().id());
      return (Component)(icon == null ? name : Component.empty().append(icon).append(" ").append(name));
   }
}
