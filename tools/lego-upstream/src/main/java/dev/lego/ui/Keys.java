package dev.lego.ui;

/** Readable names of GLFW key codes. */
public final class Keys {
    private Keys() { }

    public static String name(int k) {
        if (k < 0) return dev.lego.core.Lang.get("key.none", "Keine");
        if (k >= 65 && k <= 90) return String.valueOf((char) k);
        if (k >= 48 && k <= 57) return String.valueOf((char) k);
        if (k >= 290 && k <= 314) return "F" + (k - 289);
        if (k >= 320 && k <= 329) return "Num " + (k - 320);
        switch (k) {
            case 0: return dev.lego.core.Lang.get("key.mouse_l", "Maus L");
            case 1: return dev.lego.core.Lang.get("key.mouse_r", "Maus R");
            case 2: return dev.lego.core.Lang.get("key.mouse_m", "Maus M");
            case 3: return dev.lego.core.Lang.get("key.mouse_4", "Maus 4");
            case 4: return dev.lego.core.Lang.get("key.mouse_5", "Maus 5");
            case 32: return dev.lego.core.Lang.get("key.space", "Leertaste");
            case 340: return dev.lego.core.Lang.get("key.lshift", "L-Shift");
            case 344: return dev.lego.core.Lang.get("key.rshift", "R-Shift");
            case 341: return dev.lego.core.Lang.get("key.lctrl", "L-Strg");
            case 345: return dev.lego.core.Lang.get("key.rctrl", "R-Strg");
            case 342: return dev.lego.core.Lang.get("key.lalt", "L-Alt");
            case 346: return dev.lego.core.Lang.get("key.ralt", "Alt Gr");
            case 258: return dev.lego.core.Lang.get("key.tab", "Tab");
            case 280: return dev.lego.core.Lang.get("key.caps", "Feststell");
            case 257: return dev.lego.core.Lang.get("key.enter", "Enter");
            case 262: return dev.lego.core.Lang.get("key.right", "Rechts");
            case 263: return dev.lego.core.Lang.get("key.left", "Links");
            case 264: return dev.lego.core.Lang.get("key.down", "Runter");
            case 265: return dev.lego.core.Lang.get("key.up", "Hoch");
            case 260: return dev.lego.core.Lang.get("key.insert", "Einfg");
            case 261: return dev.lego.core.Lang.get("key.delete", "Entf");
            case 268: return dev.lego.core.Lang.get("key.home", "Pos1");
            case 269: return dev.lego.core.Lang.get("key.end", "Ende");
            case 266: return dev.lego.core.Lang.get("key.pgup", "Bild hoch");
            case 267: return dev.lego.core.Lang.get("key.pgdn", "Bild runter");
            case 96: return "^"; case 44: return ","; case 46: return "."; case 45: return "ß"; case 47: return "-"; case 59: return "Ö"; case 39: return "Ä"; case 91: return "Ü";
            default: return dev.lego.core.Lang.get("key.unknown", "Taste") + " " + k;
        }
    }
}
