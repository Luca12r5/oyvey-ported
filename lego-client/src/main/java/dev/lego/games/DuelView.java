package dev.lego.games;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import dev.lego.net.LegoAuth;
import dev.lego.net.LegoNet;
import dev.spotifyhud.Json;
import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.Style;
import dev.lego.ui.View;
import dev.lego.ui.hub.HubView;

/** Duels: invitations you got + all Lego Client users online, challenge them to a game. */
public final class DuelView extends View {
    private float scroll = 0, target = 0, max = 0;
    /** Friends from the LEGO server (uuid -> name, status), loaded when the view opens. */
    private static final java.util.Map<UUID, String[]> FRIENDS = new java.util.concurrent.ConcurrentHashMap<>();
    private static volatile String loadState = "";

    public DuelView() {
        loadState = "lädt";
        new Thread(() -> {
            try {
                Object r = LegoAuth.api("GET", "/api/friends", null);
                FRIENDS.clear();
                java.util.List<Object> list = Json.arr(r, "friends");
                if (list != null) for (Object f : list) {
                    try { FRIENDS.put(UUID.fromString(Json.str(f, "uuid", "")), new String[]{Json.str(f, "name", "Freund"), Json.str(f, "status", "offline")}); } catch (IllegalArgumentException ignored) { }
                }
                loadState = "ok";
            } catch (Exception e) {
                loadState = String.valueOf(e.getMessage());
            }
        }, "lego-duel-friends").start();
    }

    @Override public boolean blur() { return false; }
    @Override public float designW() { return 600; }
    @Override public float designH() { return 400; }
    @Override public float openMs() { return 320; }

    @Override
    public void draw() {
        float t = Ease.outQuint(open);
        Gx.fill(0, 0, W, H, Gx.rgba(0x000000, 0.35f * open));
        int pw = p(600), ph = p(400), px = (W - pw) / 2, py = (H - ph) / 2;
        Gx.pushAlpha(t);
        Gx.push();
        Gx.scaleAt(W / 2f, H / 2f, 0.94f + 0.06f * t);
        Gx.shadow(px, py, pw, ph, p(18), p(14), 0.55f);
        Gx.rect(px, py, pw, ph, p(18), Style.bg);
        Gx.outline(px, py, pw, ph, p(18), Math.max(1, p(0.6)), Style.stroke);

        // top bar
        int tb = py + p(14), bs = p(30);
        boolean hb = hover(px + p(14), tb, bs, bs);
        Gx.rect(px + p(14), tb, bs, bs, p(9), hb ? Style.surfaceHover : Style.surface);
        Gx.icon("back", px + p(14) + bs / 2f - pf(1), tb + bs / 2f, pf(14), Style.text);
        Kit.hit(px + p(14), tb, bs, bs).click(this::back);
        Gx.text("Duelle", px + p(54), tb + pf(1), pf(14), Fonts.BOLD, Style.text);
        boolean on = !loadState.isEmpty() && (loadState.equals("ok") || loadState.equals("lädt")), conn = loadState.equals("ok");
        String st = conn ? "Fordere LEGO-Spieler und Freunde heraus · über den LEGO-Server" : loadState.equals("lädt") ? "Verbinde mit dem LEGO-Server…" : "Nicht verbunden: " + loadState;
        Gx.text(st, px + p(54), tb + pf(19), pf(7.4f), Fonts.REGULAR, on && conn ? Style.sub : Style.danger);

        int cx = px + p(16), cw = pw - p(32);
        int y = tb + p(46);

        // invitations
        List<Duels.Invite> inv = Duels.invites();
        for (Duels.Invite i : inv) {
            int h = p(44);
            Gx.rect(cx, y, cw, h, p(12), Gx.withAlpha(0xFFFFC53D, 0.12f));
            Gx.outline(cx, y, cw, h, p(12), Math.max(1, p(0.7)), Gx.withAlpha(0xFFFFC53D, 0.6f));
            Gx.icon("gamepad", cx + p(22), y + h / 2f, pf(14), 0xFFFFC53D);
            Gx.textMid(Fonts.ellipsize(i.name() + " fordert dich heraus: " + Duels.GAMES.get(i.game()), Fonts.BOLD, pf(9), cw - p(260)), cx + p(40), y + h / 2f, pf(9), Fonts.BOLD, Style.text);
            int bw = p(96), bh = p(28), by = y + (h - bh) / 2;
            int ax = cx + cw - bw * 2 - p(18), dx = cx + cw - bw - p(10);
            Kit.button("acc" + i.match(), "Annehmen", "check", ax, by, bw, bh, P, true, hover(ax, by, bw, bh));
            Kit.button("dec" + i.match(), "Ablehnen", "x", dx, by, bw, bh, P, false, hover(dx, by, bw, bh));
            Kit.hit(ax, by, bw, bh).click(() -> Duels.accept(i));
            Kit.hit(dx, by, bw, bh).click(() -> Duels.decline(i));
            y += h + p(8);
        }

        // players
        Gx.textSpaced("LEGO-SPIELER ONLINE", cx + Gx.spacedWidth("LEGO-SPIELER ONLINE", pf(6.6f), Fonts.BOLD, pf(1.2f)) / 2f, y + pf(8), pf(6.6f), Fonts.BOLD, Style.muted, pf(1.2f));
        y += p(18);
        // LEGO players in this world, friends, and anyone who challenged you.
        java.util.LinkedHashMap<UUID, String> all = new java.util.LinkedHashMap<>();
        java.util.Set<String> here = new java.util.HashSet<>();
        if (dev.lego.core.Mc.world() != null) {
            for (var pl : dev.lego.core.Mc.world().players()) {
                UUID u = pl.getUUID();
                if (dev.lego.core.Mc.player() != null && u.equals(dev.lego.core.Mc.player().getUUID())) continue;
                if (LegoNet.get(u) != null) { all.put(u, pl.getName().getString()); here.add(u.toString()); }
            }
        }
        for (var f : FRIENDS.entrySet()) all.putIfAbsent(f.getKey(), f.getValue()[0]);
        for (var k : Duels.KNOWN.entrySet()) all.putIfAbsent(k.getKey(), k.getValue());
        List<UUID> players = new java.util.ArrayList<>(all.keySet());
        players.sort((a, b) -> Boolean.compare(!here.contains(a.toString()), !here.contains(b.toString())));
        int ly = y, lh = py + ph - p(14) - ly;
        int rh = p(74), gap = p(8);
        max = Math.max(0, players.size() * (rh + gap) - gap - lh);
        target = Math.max(0, Math.min(max, target));
        scroll += (target - scroll) * (1 - (float) Math.exp(-Gx.dt * 16));
        Gx.clip(cx - p(4), ly, cw + p(8), lh);
        Kit.clip(cx - p(4), ly, cw + p(8), lh);
        Kit.hit(cx, ly, cw, lh).scroll(a -> target -= a.floatValue() * p(60));
        if (players.isEmpty()) {
            Gx.icon("users", px + pw / 2f, ly + p(50), pf(26), Style.muted);
            Gx.textCenter(on ? "Noch keine LEGO-Spieler oder Freunde" : "Nicht mit dem LEGO-Server verbunden", px + pw / 2f, ly + pf(80), pf(9), Fonts.MEDIUM, Style.sub);
            Gx.textCenter("Füge im Launcher Freunde hinzu oder spiel auf einem Server mit anderen LEGO-Spielern", px + pw / 2f, ly + pf(96), pf(7.4f), Fonts.REGULAR, Style.muted);
        }
        for (int k = 0; k < players.size(); k++) {
            UUID id = players.get(k);
            int ry = Math.round(ly + k * (rh + gap) - scroll);
            if (ry + rh < ly || ry > ly + lh) continue;
            boolean near = here.contains(id.toString());
            Gx.rect(cx, ry, cw, rh, p(12), Style.surface);
            String name = all.get(id);
            String[] fr = FRIENDS.get(id);
            Gx.circle(cx + p(22), ry + p(18), p(12), Gx.withAlpha(Style.accent, 0.18f));
            Gx.icon("user", cx + p(22), ry + pf(18), pf(12), Style.accent);
            Gx.text(Fonts.ellipsize(name == null ? "Lego-Spieler" : name, Fonts.BOLD, pf(9.5f), cw - p(60)), cx + p(42), ry + pf(9), pf(9.5f), Fonts.BOLD, Style.text);
            String where = near ? "In deiner Welt" : fr != null ? ("Freund · " + switch (fr[1]) { case "in_game" -> "spielt gerade"; case "online" -> "online"; case "away" -> "abwesend"; default -> "offline"; }) : "Hat dich herausgefordert";
            Gx.text(where, cx + p(42), ry + pf(25), pf(6.8f), Fonts.REGULAR, near || (fr != null && !"offline".equals(fr[1])) ? Style.ok : Style.sub);
            boolean pending = Duels.invited(id);
            int bh = p(24), by = ry + p(40);
            int gn = Duels.GAMES.size();
            int bgap = p(6);
            int bw = (cw - p(20) - bgap * (gn - 1)) / gn;
            int bx = cx + p(10);
            for (Map.Entry<String, String> g : Duels.GAMES.entrySet()) {
                final String game = g.getKey();
                Kit.button("duel" + id + game, pending ? "..." : g.getValue(), null, bx, by, bw, bh, P, !pending, !pending && hover(bx, by, bw, bh));
                if (!pending) Kit.hit(bx, by, bw, bh).click(() -> Duels.invite(id, game));
                bx += bw + bgap;
            }
        }
        Kit.unclip();
        Gx.unclip();
        Gx.pop();
        Gx.popAlpha();
    }

    private void back() { if (host != null) host.open(dev.lego.ui.bench.BenchView.at("GAMES")); }

    @Override public boolean key(int key, int mods) {
        if (key == 256) { back(); return true; }
        return false;
    }
}
