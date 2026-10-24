package dev.lego.ui;

import java.util.ArrayList;
import java.util.List;

/** Small in-game Spotify window: search songs, open your playlists, click = play. Everything stays in Minecraft. */
public final class SpotifyView extends View {
    private static final int GREEN = 0xFF1ED760;
    private String query = "";
    private int tab = 0;               // 0 search, 1 playlists
    private volatile List<String[]> results = new ArrayList<>(), lists = null;
    private volatile boolean busy;
    private long typedAt;
    private String searched = "";
    private volatile String activeQuery = "";
    private float scroll, scrollT;

    private static dev.spotifyhud.SpotifyApi api() { try { return dev.spotifyhud.SpotifyHudMod.api(); } catch (Throwable t) { return null; } }

    @Override public float designW() { return 420; }
    @Override public float designH() { return 380; }

    private void load(Runnable r) {
        busy = true;
        Thread t = new Thread(() -> { try { r.run(); } finally { busy = false; } }, "Lego Spotify");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void draw() {
        float t = Ease.outQuint(open);
        Gx.fill(0, 0, W, H, Gx.rgba(0x000000, 0.4f * open));
        int w = p(400), h = p(360), x = (W - w) / 2, y = (H - h) / 2;
        Gx.pushAlpha(t);
        Gx.push();
        Gx.scaleAt(W / 2f, H / 2f, 0.94f + 0.06f * t);
        Gx.shadow(x, y, w, h, p(16), p(12), 0.55f);
        Gx.rect(x, y, w, h, p(16), 0xF5121212);
        Gx.outline(x, y, w, h, p(16), Math.max(1, p(0.6)), 0x22FFFFFF);
        Gx.circle(x + p(26), y + p(26), p(11), GREEN);
        Gx.icon("music", x + p(26), y + p(26), pf(11), 0xFF0B0B0B);
        Gx.text("Spotify", x + p(44), y + p(16), pf(13), Fonts.BOLD, 0xFFFFFFFF);
        dev.spotifyhud.SpotifyApi a = api();
        String np = Env.nowPlaying.get();
        if (np != null && !np.isEmpty()) Gx.textRight(Fonts.ellipsize("♪ " + np, Fonts.MEDIUM, pf(7.4f), p(200)), x + w - p(44), y + p(26), pf(7.4f), Fonts.MEDIUM, 0xAAFFFFFF);
        boolean hx = hover(x + w - p(36), y + p(12), p(26), p(26));
        Gx.icon("x", x + w - p(23), y + p(25), pf(11), hx ? 0xFFFFFFFF : 0x88FFFFFF);
        Kit.hit(x + w - p(36), y + p(12), p(26), p(26)).click(() -> host.close());
        // tabs
        String[] tabs = {"Suchen", "Meine Playlists"};
        int tx = x + p(16);
        for (int i = 0; i < 2; i++) {
            int tw = Math.round(Gx.width(tabs[i], pf(8), Fonts.BOLD)) + p(24), th = p(24);
            boolean sel = i == tab;
            Gx.rect(tx, y + p(48), tw, th, th / 2, sel ? 0xFFFFFFFF : 0x1AFFFFFF);
            Gx.textCenter(tabs[i], tx + tw / 2f, y + p(48) + th / 2f, pf(8), Fonts.BOLD, sel ? 0xFF000000 : 0xDDFFFFFF);
            final int idx = i;
            Kit.hit(tx, y + p(48), tw, th).click(() -> {
                tab = idx; scroll = scrollT = 0;
                if (idx == 1 && !busy && api() != null) { lists = null; load(() -> { dev.spotifyhud.SpotifyApi sa = api(); if (sa != null) lists = sa.playlists(); }); }
            });
            tx += tw + p(8);
        }
        int ly = y + p(82);
        if (tab == 0) {
            int sw = w - p(32), sh = p(28);
            Gx.rect(x + p(16), ly, sw, sh, sh / 2, 0xFF2A2A2A);
            Gx.icon("search", x + p(32), ly + sh / 2f, pf(10), 0xAAFFFFFF);
            if (query.isEmpty()) Gx.textMid("Song oder Künstler eingeben …", x + p(44), ly + sh / 2f, pf(8), Fonts.REGULAR, 0x77FFFFFF);
            else {
                float qw = Gx.textMid(query, x + p(44), ly + sh / 2f, pf(8.4f), Fonts.MEDIUM, 0xFFFFFFFF);
                if ((System.currentTimeMillis() / 500) % 2 == 0) Gx.fill(Math.round(x + p(45) + qw), ly + sh / 2 - p(6), Math.max(1, p(0.9)), p(12), GREEN);
            }
            ly += sh + p(10);
            // search 350 ms after the last key press
            if (!query.equals(searched) && System.currentTimeMillis() - typedAt > 350 && !busy && a != null) {
                final String q = query;
                searched = q;
                activeQuery = q;
                load(() -> {
                    dev.spotifyhud.SpotifyApi sa = api();
                    if (sa == null) return;
                    List<String[]> r = sa.search(q);
                    if (q.equals(activeQuery)) results = r;
                });
            }
        }
        List<String[]> rows = tab == 0 ? results : lists;
        int lh = y + h - p(14) - ly, rh = p(34);
        if (a == null || !a.isLoggedIn()) {
            Gx.textCenter("Spotify ist nicht verbunden – drück H im Spiel und melde dich an.", x + w / 2f, ly + p(40), pf(8), Fonts.MEDIUM, 0xAAFFFFFF);
        } else if (busy && (rows == null || rows.isEmpty())) {
            Gx.textCenter("Lädt …", x + w / 2f, ly + p(40), pf(8.4f), Fonts.MEDIUM, 0xAAFFFFFF);
        } else if (rows == null || rows.isEmpty()) {
            String e = a.lastBrowseError;
            Gx.textCenter(e != null ? e : tab == 0 ? (query.isEmpty() ? "Tipp etwas ein, um zu suchen" : "Nichts gefunden") : "Keine Playlists", x + w / 2f, ly + p(40), pf(8.4f), Fonts.MEDIUM, 0xAAFFFFFF);
        } else {
            float maxS = Math.max(0, rows.size() * rh - lh);
            scrollT = Math.max(0, Math.min(maxS, scrollT));
            scroll += (scrollT - scroll) * Math.min(1, Gx.dt * 14);
            Gx.clip(x, ly, w, lh);
            Kit.clip(x, ly, w, lh);
            Kit.hit(x, ly, w, lh).scroll(v -> scrollT -= v.floatValue() * rh * 2);
            for (int i = 0; i < rows.size(); i++) {
                int ry = Math.round(ly + i * rh - scroll);
                if (ry + rh < ly || ry > ly + lh) continue;
                String[] r = rows.get(i);
                boolean hv = hover(x + p(10), ry, w - p(20), rh - p(2));
                if (hv) Gx.rect(x + p(10), ry, w - p(20), rh - p(2), p(8), 0x1FFFFFFF);
                Gx.rect(x + p(16), ry + p(5), p(24), p(24), p(5), tab == 0 ? 0xFF333333 : 0xFF1F3A2A);
                Gx.icon(hv ? "play" : tab == 0 ? "music" : "list", x + p(28), ry + p(17), pf(10), hv ? GREEN : 0xAAFFFFFF);
                Gx.text(Fonts.ellipsize(r[0], Fonts.BOLD, pf(8.2f), w - p(80)), x + p(50), ry + p(6), pf(8.2f), Fonts.BOLD, 0xFFFFFFFF);
                Gx.text(Fonts.ellipsize(r[1], Fonts.REGULAR, pf(7f), w - p(80)), x + p(50), ry + p(19), pf(7f), Fonts.REGULAR, 0x99FFFFFF);
                final String uri = r[2];
                Kit.hit(x + p(10), ry, w - p(20), rh - p(2)).click(() -> { if (api() != null) api().playUri(uri); Toasts.show("spotify", "Spotify", "Spielt: " + r[0], GREEN, 2200); });
            }
            Kit.unclip();
            Gx.unclip();
        }
        Gx.pop();
        Gx.popAlpha();
    }

    @Override
    public boolean chr(String s) {
        if (tab != 0) return false;
        if (query.length() < 60) query += s;
        typedAt = System.currentTimeMillis();
        return true;
    }

    @Override
    public boolean key(int key, int mods) {
        if (key == 259 && tab == 0 && !query.isEmpty()) { query = query.substring(0, query.length() - 1); typedAt = System.currentTimeMillis(); return true; }
        if (key == 257 && tab == 0) { typedAt = 0; return true; }
        return false;
    }
}
