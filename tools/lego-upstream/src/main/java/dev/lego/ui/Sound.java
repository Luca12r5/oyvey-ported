package dev.lego.ui;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.class_1109;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3414;

/** Plays vanilla sounds by id (client only). */
public final class Sound {
    private Sound() { }

    public static boolean uiSounds = true;
    private static final Map<String, class_3414> EVENTS = new HashMap<>();
    private static long lastClick = 0;

    public static void play(String id, float pitch, float volume) {
        try {
            class_3414 ev = EVENTS.computeIfAbsent(id, k -> class_3414.method_47908(class_2960.method_60654(k)));
            class_310.method_1551().method_1483().method_4873(class_1109.method_4757(ev, pitch, volume));
        } catch (Throwable ignored) { }
    }

    public static void click() {
        if (!uiSounds) return;
        long now = System.currentTimeMillis();
        if (now - lastClick < 40) return;
        lastClick = now;
        play("minecraft:ui.button.click", 1.35f, 0.18f);
    }

    public static void pop() { if (uiSounds) play("minecraft:entity.item.pickup", 1.6f, 0.25f); }
    public static void open() { if (uiSounds) play("minecraft:block.note_block.chime", 1.8f, 0.12f); }
}
