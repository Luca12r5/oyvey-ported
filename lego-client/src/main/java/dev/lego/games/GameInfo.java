package dev.lego.games;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Registry of all mini games + highscores (persisted in config/legoclient.json under "games"). */
public final class GameInfo {
    public final String id, name, icon, tagline, howTo, controls;
    /** Accent color of the game card. */
    public final int color;
    /** true = higher score is better, false = lower is better (e.g. reaction time in ms). */
    public final boolean higherIsBetter;
    public final Supplier<? extends dev.lego.ui.View> factory;

    public GameInfo(String id, String name, String icon, int color, String tagline, String howTo, String controls, boolean higherIsBetter, Supplier<? extends dev.lego.ui.View> factory) {
        this.id = id; this.name = name; this.icon = icon; this.color = color; this.tagline = tagline; this.howTo = howTo;
        this.controls = controls; this.higherIsBetter = higherIsBetter; this.factory = factory;
    }

    public static final List<GameInfo> ALL = new ArrayList<>();
    private static final Map<String, Long> BEST = new LinkedHashMap<>();
    private static final Map<String, Long> PLAYS = new LinkedHashMap<>();
    /** AI difficulty per game (index into LEVELS), default "Mittel". */
    public static final String[] LEVELS = {"Leicht", "Mittel", "Schwer", "Profi"};
    private static final Map<String, Long> DIFF = new LinkedHashMap<>();

    public static int difficulty(String id) { Long d = DIFF.get(id); return d == null ? 1 : (int) Math.max(0, Math.min(LEVELS.length - 1, d)); }

    public static void setDifficulty(String id, int level) {
        DIFF.put(id, (long) Math.max(0, Math.min(LEVELS.length - 1, level)));
        dev.lego.core.Modules.scheduleSave();
    }

    public static void add(GameInfo g) { ALL.add(g); }

    public static GameInfo get(String id) {
        for (GameInfo g : ALL) if (g.id.equals(id)) return g;
        return null;
    }

    public static boolean hasBest(String id) { return BEST.containsKey(id); }
    public static long best(String id) { Long b = BEST.get(id); return b == null ? 0 : b; }
    public static long plays(String id) { Long b = PLAYS.get(id); return b == null ? 0 : b; }

    /** Records a finished round; returns true if it is a new record. */
    public static boolean submit(String id, long score) {
        GameInfo g = get(id);
        PLAYS.put(id, plays(id) + 1);
        boolean high = g == null || g.higherIsBetter;
        if (high && score <= 0) { dev.lego.core.Modules.scheduleSave(); return false; }
        boolean better = !BEST.containsKey(id) || (high ? score > best(id) : score < best(id));
        if (better) BEST.put(id, score);
        dev.lego.core.Modules.scheduleSave();
        return better;
    }

    public static void registerPersist() {
        dev.lego.core.Modules.persist("games", new dev.lego.core.Modules.Persist() {
            @Override public Object save() {
                Map<String, Object> m = new LinkedHashMap<>();
                Map<String, Object> b = new LinkedHashMap<>(BEST), p = new LinkedHashMap<>(PLAYS);
                m.put("best", b);
                m.put("plays", p);
                m.put("diff", new LinkedHashMap<String, Object>(DIFF));
                return m;
            }
            @SuppressWarnings("unchecked")
            @Override public void load(Object o) {
                if (!(o instanceof Map)) return;
                Object b = ((Map<String, Object>) o).get("best"), p = ((Map<String, Object>) o).get("plays"), d = ((Map<String, Object>) o).get("diff");
                if (d instanceof Map) for (Map.Entry<String, Object> e : ((Map<String, Object>) d).entrySet()) if (e.getValue() instanceof Number) DIFF.put(e.getKey(), ((Number) e.getValue()).longValue());
                if (b instanceof Map) for (Map.Entry<String, Object> e : ((Map<String, Object>) b).entrySet()) if (e.getValue() instanceof Number) BEST.put(e.getKey(), ((Number) e.getValue()).longValue());
                if (p instanceof Map) for (Map.Entry<String, Object> e : ((Map<String, Object>) p).entrySet()) if (e.getValue() instanceof Number) PLAYS.put(e.getKey(), ((Number) e.getValue()).longValue());
            }
        });
    }
}
