package de.bauloo.styles.mixin;

import de.bauloo.styles.KillEffect;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MultiPlayerGameMode.class})
public class KillEffectHitMixin {
   @Inject(
      method = {"attack"},
      at = {@At("HEAD")}
   )
   private void baulooStyles$hit(Player player, Entity target, CallbackInfo ci) {
      KillEffect.onAttack(target);
   }
}
