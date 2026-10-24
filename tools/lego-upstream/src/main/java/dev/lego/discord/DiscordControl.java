package dev.lego.discord;

import dev.lego.core.Category;
import dev.lego.core.Module;
import dev.lego.core.Setting;
import dev.lego.ui.Toasts;

/**
 * Discord control: mute / deafen yourself in Discord from inside Minecraft.
 * Discord itself has NO global mute shortcut by default - you set one once in Discord (Einstellungen -> Tastenbelegung),
 * and this module presses exactly that combination system wide (user32 keybd_event via the JNA library that ships with
 * Minecraft). No login, no token. The state shown is what this client toggled (Discord does not report it back).
 */
public final class DiscordControl extends Module {
    private static final int VK_CTRL = 0x11, VK_SHIFT = 0x10, VK_ALT = 0x12;
    public static boolean muted, deafened;
    public static DiscordControl instance;
    private static final String[] LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");

    final Setting.Num posX = add(new Setting.Num("Position X", 0, 95, 1, 2, "%"));
    final Setting.Num posY = add(new Setting.Num("Position Y", 0, 95, 1, 38, "%"));
    final Setting.Num scale = add(new Setting.Num("Größe", 0.7, 2, 0.1, 1, "x"));
    final Setting.Mode mods = add(new Setting.Mode("Kürzel", 0, "Strg+Umschalt", "Strg+Alt", "Alt+Umschalt"));
    final Setting.Mode muteKey = add(new Setting.Mode("Taste Stumm", 12, LETTERS));
    final Setting.Mode deafKey = add(new Setting.Mode("Taste Taub", 3, LETTERS));
    final Setting.Bool compact = add(new Setting.Bool("Kompakt", false));
    private boolean guided;

    public DiscordControl() {
        super("discord", "Discord-Steuerung", "Mikro & Ton von Discord im Spiel schalten (Knöpfe im Chat anklicken oder Tasten belegen) - einmal in Discord das Kürzel eintragen", Category.UTILITY);
        icon("mute");
        fresh();
        instance = this;
        add(new Setting.Action("Stumm", "Mikrofon stumm / an", DiscordControl::toggleMute));
        add(new Setting.Action("Taub", "Ton komplett aus / an", DiscordControl::toggleDeafen));
        add(new Setting.Action("Anleitung", "So richtest du Discord ein", DiscordControl::guide));
    }

    @Override public void onEnable() { if (!guided) { guided = true; guide(); } }

    public static String combo(boolean mute) {
        if (instance == null) return "";
        String m = instance.mods.get();
        return m + "+" + (mute ? instance.muteKey.get() : instance.deafKey.get());
    }

    /** Explains the one-time setup in Discord (chat, only visible for you). */
    public static void guide() {
        String[] lines = {
                "§9Discord-Steuerung einrichten (einmalig):",
                "§71. Discord öffnen → Zahnrad (Benutzereinstellungen) → §fTastenbelegung",
                "§72. §fTastenbelegung hinzufügen§7 → Aktion §fStummschalten umschalten§7 → Kürzel §f" + combo(true),
                "§73. Nochmal §fTastenbelegung hinzufügen§7 → §fTaubschalten umschalten§7 → Kürzel §f" + combo(false),
                "§74. Fertig! Im Chat (T) die Knöpfe anklicken oder in den Mod-Einstellungen eine Taste belegen."};
        try { for (String l : lines) dev.lego.core.Mc.mc().field_1705.method_1743().method_1812(net.minecraft.class_2561.method_43470(l)); } catch (Throwable ignored) { }
        Toasts.show("discord", "Discord einrichten", "Kürzel " + combo(true) + " / " + combo(false) + " in Discord eintragen - Anleitung im Chat", 0xFF5865F2, 6000);
    }

    public static boolean hudOn() { return instance != null && instance.enabled; }
    static double fx() { return instance.posX.get() / 100.0; }
    static double fy() { return instance.posY.get() / 100.0; }
    static float sc() { return instance.scale.getF(); }
    static boolean compactHud() { return instance.compact.get(); }

    public static void toggleMute() {
        if (press(instance == null ? 'M' : instance.muteKey.get().charAt(0))) {
            muted = !muted;
            Toasts.show("discord", "Discord", muted ? "Mikrofon stumm" : "Mikrofon an", muted ? 0xFFED4245 : 0xFF3BA55D, 1800);
        }
    }

    public static void toggleDeafen() {
        if (press(instance == null ? 'D' : instance.deafKey.get().charAt(0))) {
            deafened = !deafened;
            Toasts.show("discord", "Discord", deafened ? "Taub – du hörst nichts" : "Ton wieder an", deafened ? 0xFFED4245 : 0xFF3BA55D, 1800);
        }
    }

    /** Presses the configured modifier combination + letter system wide. */
    static boolean press(char letter) {
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
            Toasts.show("discord", "Discord", "Geht nur unter Windows", 0xFFED4245, 2500);
            return false;
        }
        try {
            Class<?> f = Class.forName("com.sun.jna.Function");
            Object fn = f.getMethod("getFunction", String.class, String.class).invoke(null, "user32", "keybd_event");
            java.lang.reflect.Method inv = f.getMethod("invokeVoid", Object[].class);
            int mi = instance == null ? 0 : instance.mods.index;
            int[] mod = mi == 0 ? new int[]{VK_CTRL, VK_SHIFT} : mi == 1 ? new int[]{VK_CTRL, VK_ALT} : new int[]{VK_ALT, VK_SHIFT};
            int[] down = {mod[0], mod[1], Character.toUpperCase(letter)};
            // off the render thread: the short pause between press and release must never stall a frame
            new Thread(() -> {
                try {
                    for (int k : down) inv.invoke(fn, (Object) new Object[]{(byte) k, (byte) 0, 0, com.sun.jna.Pointer.NULL});
                    Thread.sleep(60);
                    for (int i = down.length - 1; i >= 0; i--) inv.invoke(fn, (Object) new Object[]{(byte) down[i], (byte) 0, 2, com.sun.jna.Pointer.NULL});
                } catch (Throwable t) { dev.lego.LegoClient.LOG("Discord: " + t); }
            }, "Lego-Discord").start();
            return true;
        } catch (Throwable t) {
            dev.lego.LegoClient.LOG("Discord: " + t);
            Toasts.show("discord", "Discord", "Konnte die Tasten nicht senden", 0xFFED4245, 2500);
            return false;
        }
    }
}
