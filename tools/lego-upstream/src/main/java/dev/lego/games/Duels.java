package dev.lego.games;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import dev.lego.core.Mc;
import dev.lego.net.LegoSync;
import dev.lego.ui.LegoScreen;
import dev.lego.ui.Toasts;
import dev.spotifyhud.Json;

/**
 * Duels between Lego Client users (over the Lego-Sync relay, separate channel): invite -> accept -> moves.
 * Messages: {id, name, to, k = inv | acc | dec | mv | bye, g = game, m = match id, r = round, v = move}.
 * Everything that arrives is untrusted: checked against the running match, the opponent and the game rules.
 */
public final class Duels {
    private Duels() { }

    /** Games that can be played as a duel: id -> name. */
    public static final Map<String, String> GAMES = new LinkedHashMap<>();
    static {
        GAMES.put("connect4", "Vier gewinnt");
        GAMES.put("tictactoe", "Tic-Tac-Toe");
        GAMES.put("rps", "Schere Stein Papier");
        GAMES.put("reversi", "Reversi");
        GAMES.put("nim", "Nim");
    }

    public record Invite(String match, UUID from, String name, String game, long at) { }

    /** A running duel. */
    public static final class Duel {
        public final String match, game, oppName;
        public final UUID opp;
        /** true = you start round 0 (the one who invited). */
        public final boolean iStart;
        final ConcurrentLinkedQueue<int[]> moves = new ConcurrentLinkedQueue<>();
        public volatile boolean oppLeft;
        private boolean left;

        Duel(String match, String game, UUID opp, String oppName, boolean iStart) {
            this.match = match; this.game = game; this.opp = opp; this.oppName = oppName; this.iStart = iStart;
        }

        /** Next move of the opponent for this round (or null); moves of later rounds stay queued. */
        public Integer take(int round) {
            for (int[] m : moves) {
                if (m[0] < round) { moves.remove(m); continue; }
                if (m[0] == round) { moves.remove(m); return m[1]; }
            }
            return null;
        }

        public void send(int round, int move) {
            Map<String, Object> m = base("mv", opp, game, match);
            m.put("r", (double) round);
            m.put("v", (double) move);
            post(m);
        }

        /** Leave the duel (the opponent sees it). */
        public void leave() {
            if (left) return;
            left = true;
            if (current == this) current = null;
            post(base("bye", opp, game, match));
        }
    }

    public static final Map<String, Invite> INCOMING = new ConcurrentHashMap<>();
    /** Sent invites: match -> {opponent uuid, game, time}. */
    private static final Map<String, Object[]> OUTGOING = new ConcurrentHashMap<>();
    public static volatile Duel current;
    private static final long INVITE_S = 90;

    private static String myId() { return Mc.player() == null ? "" : Mc.player().method_5667().toString(); }

    private static Map<String, Object> base(String kind, UUID to, String game, String match) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", myId());
        m.put("name", Mc.player() == null ? "" : Mc.name(Mc.player()));
        m.put("to", to.toString());
        m.put("k", kind);
        m.put("g", game);
        m.put("m", match);
        return m;
    }

    private static void post(Map<String, Object> m) {
        try {
            LegoSync.HTTP_POST(LegoSync.DUEL_RELAY, Json.write(m));
        } catch (Throwable ignored) { }
    }

    // ------------------------------------------------------------------ actions (game thread)
    public static boolean invited(UUID to) {
        for (Object[] o : OUTGOING.values()) if (o[0].equals(to) && System.currentTimeMillis() / 1000 - (long) o[2] < INVITE_S) return true;
        return false;
    }

    public static void invite(UUID to, String game) {
        if (!GAMES.containsKey(game)) return;
        String match = Long.toString(System.nanoTime() ^ (long) (Math.random() * Long.MAX_VALUE), 36).replace("-", "");
        if (match.length() > 16) match = match.substring(0, 16);
        OUTGOING.put(match, new Object[]{to, game, System.currentTimeMillis() / 1000});
        post(base("inv", to, game, match));
        String n = LegoSync.name(to);
        Toasts.show("gamepad", "Duell", "Einladung zu " + GAMES.get(game) + " an " + (n == null ? "Spieler" : n) + " gesendet", 0xFF3FA9FF, 3000);
    }

    public static void accept(Invite inv) {
        INCOMING.remove(inv.match());
        post(base("acc", inv.from(), inv.game(), inv.match()));
        start(new Duel(inv.match(), inv.game(), inv.from(), inv.name(), false));
    }

    public static void decline(Invite inv) {
        INCOMING.remove(inv.match());
        post(base("dec", inv.from(), inv.game(), inv.match()));
    }

    public static List<Invite> invites() {
        long now = System.currentTimeMillis() / 1000;
        INCOMING.values().removeIf(i -> now - i.at() > INVITE_S);
        return new ArrayList<>(INCOMING.values());
    }

    private static void start(Duel d) {
        Duel old = current;
        if (old != null && !old.match.equals(d.match)) old.leave();
        current = d;
        dev.lego.ui.View v = switch (d.game) {
            case "tictactoe" -> new TicTacToe(d);
            case "rps" -> new RPS(d);
            case "reversi" -> new Reversi(d);
            case "nim" -> new Nim(d);
            default -> new ConnectFour(d);
        };
        Mc.mc().execute(() -> {
            var s = Mc.mc().field_1755;
            if (s == null || s instanceof LegoScreen) LegoScreen.show(v);
            else Toasts.show("gamepad", "Duell", "Duell gegen " + d.oppName + " startet – schließe das Menü", 0xFF1ED760, 4000);
        });
    }

    // ------------------------------------------------------------------ receiving (relay thread)
    private static boolean safe(String s, int max) { return s != null && !s.isEmpty() && s.length() <= max && s.matches("[A-Za-z0-9_]+"); }

    public static void receive(Object msg, long time, String myId) {
        if (myId.isEmpty() || !myId.equals(Json.str(msg, "to", ""))) return;
        if (System.currentTimeMillis() / 1000 - time > INVITE_S) return;   // old (replayed) message
        UUID from;
        try { from = UUID.fromString(Json.str(msg, "id", "")); } catch (IllegalArgumentException e) { return; }
        String kind = Json.str(msg, "k", ""), game = Json.str(msg, "g", ""), match = Json.str(msg, "m", "");
        String name = Json.str(msg, "name", "");
        if (!safe(match, 16) || !GAMES.containsKey(game)) return;
        if (!name.matches("[A-Za-z0-9_]{1,16}")) name = "Lego-Spieler";
        switch (kind) {
            case "inv" -> {
                if (INCOMING.size() > 20) return;
                INCOMING.put(match, new Invite(match, from, name, game, time));
                Toasts.show("gamepad", "Duell-Einladung", name + " fordert dich zu " + GAMES.get(game) + " heraus · Spiele → Duelle", 0xFFFFC53D, 6000);
                dev.lego.ui.Sound.play("minecraft:block.note_block.bell", 1.2f, 0.5f);
            }
            case "acc" -> {
                Object[] o = OUTGOING.remove(match);
                if (o == null || !o[0].equals(from)) return;
                Toasts.show("gamepad", "Duell", name + " hat angenommen!", 0xFF1ED760, 2500);
                start(new Duel(match, game, from, name, true));
            }
            case "dec" -> {
                Object[] o = OUTGOING.remove(match);
                if (o != null && o[0].equals(from)) Toasts.show("gamepad", "Duell", name + " hat abgelehnt", 0xFFFF4D5E, 2500);
            }
            case "mv" -> {
                Duel d = current;
                if (d == null || !d.match.equals(match) || !d.opp.equals(from)) return;
                int r = (int) Json.num(msg, "r", -1), v = (int) Json.num(msg, "v", -1);
                if (r < 0 || r > 10_000 || v < 0 || v > 64 || d.moves.size() > 64) return;
                d.moves.add(new int[]{r, v});
            }
            case "bye" -> {
                INCOMING.remove(match);
                Duel d = current;
                if (d != null && d.match.equals(match) && d.opp.equals(from)) d.oppLeft = true;
            }
            default -> { }
        }
    }
}
