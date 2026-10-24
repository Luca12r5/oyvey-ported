package dev.lego.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.lego.visual.AntiVanish;
import net.minecraft.class_634;
import net.minecraft.class_2639;
import net.minecraft.class_2675;
import net.minecraft.class_2703;
import net.minecraft.class_2739;
import net.minecraft.class_2767;
import net.minecraft.class_7439;
import net.minecraft.class_7828;

@Mixin(value = class_634.class, remap = false)
public abstract class AntiVanishMixin {

    @Inject(method = "method_45725(Lnet/minecraft/class_7828;)V", at = @At("TAIL"), remap = false, require = 0)
    private void lego$playerRemove(class_7828 packet, CallbackInfo ci) {
        try {
            List<UUID> ids = new ArrayList<>(packet.comp_1105());
            AntiVanish.onPlayerRemove(ids);
        } catch (Throwable ignored) {}
    }

    @Inject(method = "method_11113(Lnet/minecraft/class_2703;)V", at = @At("TAIL"), remap = false, require = 0)
    private void lego$playerUpdate(class_2703 packet, CallbackInfo ci) {
        try {
            for (var entry : packet.method_46329()) {
                UUID id = entry.comp_1106();
                boolean listed = false;
                String name = null;
                String gameMode = null;
                for (var action : packet.method_46327()) {
                    String aName = action.name();
                    if ("field_40700".equals(aName) || aName.contains("LISTED")) listed = true;
                    if (("field_29136".equals(aName) || aName.contains("ADD")) && entry.comp_1106() != null) {
                        try {
                            var profile = entry.comp_1107();
                            if (profile != null) name = profile.name();
                        } catch (Throwable ignored2) {}
                    }
                    if ("field_29137".equals(aName) || aName.contains("GAME_MODE")) {
                        try {
                            var gm = entry.comp_1110();
                            if (gm != null) gameMode = gm.name().toLowerCase(java.util.Locale.ROOT);
                        } catch (Throwable ignored2) {}
                    }
                }
                AntiVanish.onPlayerUpdate(id, listed, name, gameMode);
            }
        } catch (Throwable ignored) {}
    }

    @Inject(method = "method_43596(Lnet/minecraft/class_7439;)V", at = @At("TAIL"), remap = false, require = 0)
    private void lego$systemChat(class_7439 packet, CallbackInfo ci) {
        try {
            AntiVanish.onSystemChat(packet.comp_763().getString());
        } catch (Throwable ignored) {}
    }

    @Inject(method = "method_11081(Lnet/minecraft/class_2639;)V", at = @At("TAIL"), remap = false, require = 0)
    private void lego$commandSuggestions(class_2639 packet, CallbackInfo ci) {
        try {
            int id = packet.comp_2262();
            List<String> names = new ArrayList<>();
            packet.method_11397().getList().forEach(s -> {
                String text = s.getText();
                if (text != null && !text.isBlank()) names.add(text.trim());
            });
            AntiVanish.onCommandSuggestions(id, names);
        } catch (Throwable ignored) {}
    }

    @Inject(method = "method_11093(Lnet/minecraft/class_2739;)V", at = @At("TAIL"), remap = false, require = 0)
    private void lego$entityMetadata(class_2739 packet, CallbackInfo ci) {
        try {
            AntiVanish.onEntityMetadata(packet.comp_1127());
        } catch (Throwable ignored) {}
    }

    @Inject(method = "method_11146(Lnet/minecraft/class_2767;)V", at = @At("TAIL"), remap = false, require = 0)
    private void lego$sound(class_2767 packet, CallbackInfo ci) {
        try {
            String soundId = packet.method_11894().comp_349().comp_3319().toString();
            AntiVanish.onSound(packet.method_11890() / 8.0, packet.method_11889() / 8.0, packet.method_11893() / 8.0, soundId);
        } catch (Throwable ignored) {}
    }

    @Inject(method = "method_11077(Lnet/minecraft/class_2675;)V", at = @At("TAIL"), remap = false, require = 0)
    private void lego$particle(class_2675 packet, CallbackInfo ci) {
        try {
            String particleId = packet.method_11551().getClass().getSimpleName();
            AntiVanish.onParticle(packet.method_11544(), packet.method_11547(), packet.method_11546(), particleId);
        } catch (Throwable ignored) {}
    }
}
