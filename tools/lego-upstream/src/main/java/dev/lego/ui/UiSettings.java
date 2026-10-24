package dev.lego.ui;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.lego.core.Modules;

/** Global look settings (design page), persisted in config/legoclient.json under "ui". */
public final class UiSettings {
    private UiSettings() { }

    public static final String[] HUD_STYLES = {"Clean", "Minimal", "Spotify"};
    public static int hudStyle = 0;
    public static double hudOpacity = 0.55;
    public static boolean hudIcons = false;
    public static boolean hudShadow = true;
    public static boolean hudAccentLine = false;
    public static boolean sounds = true;
    public static boolean blur = true;
    /** Size of the whole client UI (1 = design size). */
    public static double uiScale = 0.75;
    /** Animation speed multiplier (higher = faster). */
    public static double animSpeed = 2.0;
    public static boolean animations = true;
    /** Christmas decorations (snow, lights, snow caps ...). */
    public static boolean xmas = true;
    /** Halloween decorations (embers, bats, cobwebs, pumpkins ...). */
    public static boolean halloween = true;
    /** Click GUI: background animation (BgFx.NAMES), its strength and how transparent the glass window is. */
    public static int bgStyle = 1;
    public static double bgIntensity = 0.75;
    public static double glass = 0.55;
    /** Brick start animation when Minecraft opens. */
    public static boolean intro = true;
    /** The light line gliding along the top edge of the click GUI. */
    public static boolean guiLine = true;
    /** User background image file path (empty = none). */
    public static String bgImage = "";
    /** Click GUI style: 0 = Standard, 1 = Modern, 2 = Glass. */
    public static int guiStyle = 0;
    public static final String[] GUI_STYLES = {"Standard", "Modern", "Glass"};
    public static int language = 0;

    /** Multiplier for all UI animation speeds. */
    public static float anim() { return animations ? (float) animSpeed : 60f; }

    public static void register() {
        Modules.persist("ui", new Modules.Persist() {
            @Override public Object save() {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("hudStyle", (long) hudStyle);
                m.put("hudOpacity", hudOpacity);
                m.put("hudIcons", hudIcons);
                m.put("hudShadow", hudShadow);
                m.put("hudAccentLine", hudAccentLine);
                m.put("sounds", sounds);
                m.put("blur", blur);
                m.put("uiScale", uiScale);
                m.put("animSpeed", animSpeed);
                m.put("animations", animations);
                m.put("xmas", xmas);
                m.put("halloween", halloween);
                m.put("bgStyle", (long) bgStyle);
                m.put("bgIntensity", bgIntensity);
                m.put("glass", glass);
                m.put("intro", intro);
                m.put("guiLine", guiLine);
                m.put("bgImage", bgImage);
                m.put("guiStyle", (long) guiStyle);
                m.put("language", (long) language);
                return m;
            }
            @Override public void load(Object o) {
                hudStyle = (int) Math.max(0, Math.min(HUD_STYLES.length - 1, dev.spotifyhud.Json.num(o, "hudStyle", hudStyle)));
                hudOpacity = Math.max(0, Math.min(1, dev.spotifyhud.Json.dbl(o, "hudOpacity", hudOpacity)));
                hudIcons = dev.spotifyhud.Json.bool(o, "hudIcons", hudIcons);
                hudShadow = dev.spotifyhud.Json.bool(o, "hudShadow", hudShadow);
                hudAccentLine = dev.spotifyhud.Json.bool(o, "hudAccentLine", hudAccentLine);
                sounds = dev.spotifyhud.Json.bool(o, "sounds", sounds);
                blur = dev.spotifyhud.Json.bool(o, "blur", blur);
                uiScale = Math.max(0.6, Math.min(1.3, dev.spotifyhud.Json.dbl(o, "uiScale", uiScale)));
                animSpeed = Math.max(0.5, Math.min(3, dev.spotifyhud.Json.dbl(o, "animSpeed", animSpeed)));
                animations = dev.spotifyhud.Json.bool(o, "animations", animations);
                xmas = dev.spotifyhud.Json.bool(o, "xmas", xmas);
                halloween = dev.spotifyhud.Json.bool(o, "halloween", halloween);
                bgStyle = (int) Math.max(0, Math.min(BgFx.COUNT - 1, dev.spotifyhud.Json.num(o, "bgStyle", bgStyle)));
                bgIntensity = Math.max(0.1, Math.min(1, dev.spotifyhud.Json.dbl(o, "bgIntensity", bgIntensity)));
                intro = dev.spotifyhud.Json.bool(o, "intro", intro);
                guiLine = dev.spotifyhud.Json.bool(o, "guiLine", guiLine);
                glass = Math.max(0.15, Math.min(1, dev.spotifyhud.Json.dbl(o, "glass", glass)));
                bgImage = dev.spotifyhud.Json.str(o, "bgImage", bgImage);
                guiStyle = (int) Math.max(0, Math.min(GUI_STYLES.length - 1, dev.spotifyhud.Json.num(o, "guiStyle", guiStyle)));
                language = (int) Math.max(0, Math.min(dev.lego.core.Lang.LANGUAGES.length - 1, dev.spotifyhud.Json.num(o, "language", language)));
                dev.lego.core.Lang.setLanguage(language);
                BgFx.updateNames();
                Style.updateThemeNames();
                dev.lego.core.Setting.Color.updateNames();
                Sound.uiSounds = sounds;
            }
        });
    }
}
