package dev.lego.mixin;

import com.mojang.authlib.GameProfile;
import dev.lego.visual.AntiVanish;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {ClientPacketListener.class},
   remap = false
)
public abstract class AntiVanishMixin {
   @Inject(
      method = {"method_45725(Lnet/minecraft/class_7828;)V"},
      at = {@At("TAIL")},
      remap = false,
      require = 0
   )
   private void lego$playerRemove(ClientboundPlayerInfoRemovePacket packet, CallbackInfo ci) {
      try {
         List<UUID> ids = new ArrayList<>(packet.profileIds());
         AntiVanish.onPlayerRemove(ids);
      } catch (Throwable var4) {
      }
   }

   @Inject(
      method = {"method_11113(Lnet/minecraft/class_2703;)V"},
      at = {@At("TAIL")},
      remap = false,
      require = 0
   )
   private void lego$playerUpdate(ClientboundPlayerInfoUpdatePacket packet, CallbackInfo ci) {
      try {
         for (Entry entry : packet.entries()) {
            UUID id = entry.profileId();
            boolean listed = false;
            String name = null;
            String gameMode = null;

            for (Action action : packet.actions()) {
               String aName = action.name();
               if ("field_40700".equals(aName) || aName.contains("LISTED")) {
                  listed = true;
               }

               if (("field_29136".equals(aName) || aName.contains("ADD")) && entry.profileId() != null) {
                  try {
                     GameProfile profile = entry.profile();
                     if (profile != null) {
                        name = profile.name();
                     }
                  } catch (Throwable var14) {
                  }
               }

               if ("field_29137".equals(aName) || aName.contains("GAME_MODE")) {
                  try {
                     GameType gm = entry.gameMode();
                     if (gm != null) {
                        gameMode = gm.name().toLowerCase(Locale.ROOT);
                     }
                  } catch (Throwable var13) {
                  }
               }
            }

            AntiVanish.onPlayerUpdate(id, listed, name, gameMode);
         }
      } catch (Throwable var15) {
      }
   }

   @Inject(
      method = {"method_43596(Lnet/minecraft/class_7439;)V"},
      at = {@At("TAIL")},
      remap = false,
      require = 0
   )
   private void lego$systemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
      try {
         AntiVanish.onSystemChat(packet.content().getString());
      } catch (Throwable var4) {
      }
   }

   @Inject(
      method = {"method_11081(Lnet/minecraft/class_2639;)V"},
      at = {@At("TAIL")},
      remap = false,
      require = 0
   )
   private void lego$commandSuggestions(ClientboundCommandSuggestionsPacket packet, CallbackInfo ci) {
      try {
         int id = packet.id();
         List<String> names = new ArrayList<>();
         packet.toSuggestions().getList().forEach(s -> {
            String text = s.getText();
            if (text != null && !text.isBlank()) {
               names.add(text.trim());
            }
         });
         AntiVanish.onCommandSuggestions(id, names);
      } catch (Throwable var5) {
      }
   }

   @Inject(
      method = {"method_11093(Lnet/minecraft/class_2739;)V"},
      at = {@At("TAIL")},
      remap = false,
      require = 0
   )
   private void lego$entityMetadata(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
      try {
         AntiVanish.onEntityMetadata(packet.id());
      } catch (Throwable var4) {
      }
   }

   @Inject(
      method = {"method_11146(Lnet/minecraft/class_2767;)V"},
      at = {@At("TAIL")},
      remap = false,
      require = 0
   )
   private void lego$sound(ClientboundSoundPacket packet, CallbackInfo ci) {
      try {
         String soundId = packet.getSound().value().location().toString();
         AntiVanish.onSound(packet.getX() / 8.0, packet.getY() / 8.0, packet.getZ() / 8.0, soundId);
      } catch (Throwable var4) {
      }
   }

   @Inject(
      method = {"method_11077(Lnet/minecraft/class_2675;)V"},
      at = {@At("TAIL")},
      remap = false,
      require = 0
   )
   private void lego$particle(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
      try {
         String particleId = packet.getParticle().getClass().getSimpleName();
         AntiVanish.onParticle(packet.getX(), packet.getY(), packet.getZ(), particleId);
      } catch (Throwable var4) {
      }
   }
}
