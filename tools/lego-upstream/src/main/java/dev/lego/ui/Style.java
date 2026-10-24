package dev.lego.ui;

import dev.lego.core.Modules;

/** Colors of the client look (depend on the chosen theme + accent). */
public final class Style {
    private Style() { }

    public static final String[] THEMES = {
        "winter", "standard", "midnight", "glass", "purple", "ocean", "sunset", "christmas", "light",
        "neon", "cyberpunk", "forest", "cherry", "gold", "aqua", "ash", "carbon",
        "halloween",
        "volcano", "lavender", "nord", "mint", "rose", "copper", "blood", "storm"
    };
    private static final String[] THEME_NAMES_DE = {
        "Winterzauber", "Standard", "Mitternacht", "Glas", "Lila", "Ozean", "Sunset", "Weihnachten", "Hell",
        "Neon", "Cyberpunk", "Wald", "Kirschblüte", "Gold", "Aqua", "Asche", "Carbon",
        "Halloween",
        "Vulkan", "Lavendel", "Nord", "Minze", "Rose", "Kupfer", "Blut", "Sturm"
    };
    public static String[] THEME_NAMES = THEME_NAMES_DE;
    public static void updateThemeNames() {
        String[] n = new String[THEME_NAMES_DE.length];
        for (int i = 0; i < n.length; i++) n[i] = dev.lego.core.Lang.get("theme." + i, THEME_NAMES_DE[i]);
        THEME_NAMES = n;
    }

    public static int bg, bg2, surface, surface2, surfaceHover, stroke, strokeHi, text, sub, muted, accent, accentText, shadow, danger, ok;
    public static boolean light;
    /** Second accent (gold) used for highlights. */
    public static int gold = 0xFFF4C152;

    /** Recomputes the palette (cheap, call once per frame). */
    /** {bg, surface, surface2, text, sub} of a theme (for previews). */
    public static int[] palette(String theme) {
        String old = Modules.theme;
        Modules.theme = theme;
        update();
        int[] r = {bg, surface, surface2, text, sub, surfaceHover};
        Modules.theme = old;
        update();
        return r;
    }

    public static void update() {
        accent = Modules.accentArgb();
        String t = Modules.theme;
        light = "light".equals(t);
        switch (t) {
            case "winter":
                bg = 0xF00B1428; bg2 = 0xF0101C35; surface = 0xE8142241; surface2 = 0xEC1A2B50; surfaceHover = 0xF0223660; break;
            case "midnight":
                bg = 0xF20B1020; bg2 = 0xF2101830; surface = 0xFF141D36; surface2 = 0xFF1A2544; surfaceHover = 0xFF212E52; break;
            case "glass":
                bg = 0xB814161C; bg2 = 0xB81A1D24; surface = 0x22FFFFFF; surface2 = 0x33FFFFFF; surfaceHover = 0x40FFFFFF; break;
            case "purple":
                bg = 0xF2150E24; bg2 = 0xF21C1230; surface = 0xFF211638; surface2 = 0xFF2A1C47; surfaceHover = 0xFF332255; break;
            case "ocean":
                bg = 0xF20A171C; bg2 = 0xF20E2027; surface = 0xFF112830; surface2 = 0xFF15313B; surfaceHover = 0xFF1B3B47; break;
            case "sunset":
                bg = 0xF21E0F14; bg2 = 0xF226131A; surface = 0xFF2C161E; surface2 = 0xFF371B25; surfaceHover = 0xFF42212D; break;
            case "christmas":
                bg = 0xF2101A12; bg2 = 0xF2152219; surface = 0xFF18281C; surface2 = 0xFF1E3223; surfaceHover = 0xFF263D2B; break;
            case "halloween":
                bg = 0xF2140A18; bg2 = 0xF21C0E22; surface = 0xFF221230; surface2 = 0xFF2C183C; surfaceHover = 0xFF381E4A; break;
            case "light":
                bg = 0xF5F3F4F7; bg2 = 0xF5E9EBF0; surface = 0xFFFFFFFF; surface2 = 0xFFF1F2F5; surfaceHover = 0xFFE6E8EE; break;
            case "neon":
                bg = 0xF2050510; bg2 = 0xF2080816; surface = 0xFF0C0C1E; surface2 = 0xFF121228; surfaceHover = 0xFF1A1A36; break;
            case "cyberpunk":
                bg = 0xF2140812; bg2 = 0xF21C0C1A; surface = 0xFF220E20; surface2 = 0xFF2C1428; surfaceHover = 0xFF381A34; break;
            case "forest":
                bg = 0xF20C140C; bg2 = 0xF2111C11; surface = 0xFF142214; surface2 = 0xFF1A2C1A; surfaceHover = 0xFF213621; break;
            case "cherry":
                bg = 0xF21C1018; bg2 = 0xF2241420; surface = 0xFF2C1826; surface2 = 0xFF381E30; surfaceHover = 0xFF44253A; break;
            case "gold":
                bg = 0xF2161008; bg2 = 0xF21E160C; surface = 0xFF261C0E; surface2 = 0xFF302412; surfaceHover = 0xFF3C2E18; break;
            case "aqua":
                bg = 0xF2081418; bg2 = 0xF20C1C22; surface = 0xFF0E2228; surface2 = 0xFF122C34; surfaceHover = 0xFF18363E; break;
            case "ash":
                bg = 0xF2181818; bg2 = 0xF21E1E1E; surface = 0xFF262626; surface2 = 0xFF2E2E2E; surfaceHover = 0xFF383838; break;
            case "carbon":
                bg = 0xF20E1012; bg2 = 0xF2141618; surface = 0xFF181C1E; surface2 = 0xFF1E2226; surfaceHover = 0xFF262C30; break;
            case "volcano":
                bg = 0xF21C0C08; bg2 = 0xF224100C; surface = 0xFF2C1410; surface2 = 0xFF381A14; surfaceHover = 0xFF44201A; break;
            case "lavender":
                bg = 0xF2141020; bg2 = 0xF21C1628; surface = 0xFF221C32; surface2 = 0xFF2A223E; surfaceHover = 0xFF342C4C; break;
            case "nord":
                bg = 0xF2202832; bg2 = 0xF2283038; surface = 0xFF2E3640; surface2 = 0xFF363E4A; surfaceHover = 0xFF3E4856; break;
            case "mint":
                bg = 0xF20A1614; bg2 = 0xF20E1E1C; surface = 0xFF122622; surface2 = 0xFF18302C; surfaceHover = 0xFF1E3C36; break;
            case "rose":
                bg = 0xF21A0E14; bg2 = 0xF222141C; surface = 0xFF2A1822; surface2 = 0xFF341E2C; surfaceHover = 0xFF402636; break;
            case "copper":
                bg = 0xF2181210; bg2 = 0xF2201816; surface = 0xFF281E1A; surface2 = 0xFF322620; surfaceHover = 0xFF3E3028; break;
            case "blood":
                bg = 0xF2180808; bg2 = 0xF2220C0C; surface = 0xFF2C0E0E; surface2 = 0xFF381414; surfaceHover = 0xFF441A1A; break;
            case "storm":
                bg = 0xF2101418; bg2 = 0xF2161C22; surface = 0xFF1C2228; surface2 = 0xFF222A32; surfaceHover = 0xFF2A343E; break;
            default:
                bg = 0xF20D0E12; bg2 = 0xF2121318; surface = 0xFF17181E; surface2 = 0xFF1E2027; surfaceHover = 0xFF252832; break;
        }
        if (light) {
            stroke = 0x14000000; strokeHi = 0x2A000000; text = 0xFF15161A; sub = 0xFF5B616E; muted = 0xFF9097A3; shadow = 0x30000000;
        } else {
            stroke = 0x12FFFFFF; strokeHi = 0x26FFFFFF; text = 0xFFF3F4F6; sub = 0xFFA2A8B3; muted = 0xFF626873; shadow = 0x80000000;
        }
        accentText = brightness(accent) > 0.62 ? 0xFF111111 : 0xFFFFFFFF;
        danger = 0xFFFF4D5E;
        ok = 0xFF1ED760;
    }

    public static double brightness(int argb) {
        int r = (argb >> 16) & 255, g = (argb >> 8) & 255, b = argb & 255;
        return (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
    }
}
