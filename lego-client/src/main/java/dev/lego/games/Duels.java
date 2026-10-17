package dev.lego.games;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.lego.core.Mc;
import dev.lego.net.LegoAuth;
import dev.lego.ui.LegoScreen;
import dev.lego.ui.Toasts;
import dev.spotifyhud.Json;

/**
 * Duels between LEGO users through the LEGO server (the server is the referee:
 * privacy, blocks, turns and legal moves are checked there). The launcher can
 * take part as well. Invites arrive via an inbox that is polled while the game
 * runs; moves of a running duel are polled faster.
 */
public final class Duels {
    private Duels() { }

    /** Games that can be played as a duel: id -> name (same ids as the server). */
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
            async(() -> LegoAuth.api("POST", "/api/duels/" + match + "/move", Map.of("value", move)), "Zug nicht angenommen");
        }

        /** Leave the duel (the opponent sees it). */
        public void leave() {
            if (left) return;
            left = true;
            if (current == this) current = null;
            async(() -> LegoAuth.api("POST", "/api/duels/" + match + "/leave", Map.of()), null);
        }
    }

    public static final Map<String, Invite> INCOMING = new ConcurrentHashMap<>();
    /** Sent invites: opponent uuid -> {match, game, time s}. */
    private static final Map<UUID, Object[]> OUTGOING = new ConcurrentHashMap<>();
    public static volatile Duel current;
    private static final long INVITE_S = 90;
    private static final ExecutorService IO = Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r, "lego-duels"); t.setDaemon(true); return t; });
    private static volatile long since = 0;
    private static volatile long nextPoll = 0;
    private static volatile boolean polling = false;
    /** Names of LEGO players we heard from (for the list). */
    public static final Map<UUID, String> KNOWN = new ConcurrentHashMap<>();
    public static volatile String lastError = null;

    interface Call { Object run() throws Exception; }

    private static void async(Call c, String errorText) {
        IO.execute(() -> {
            try { c.run(); lastError = null; }
            catch (Exception e) {
                lastError = e.getMessage();
                if (errorText != null) toast("Duell", errorText + ": " + e.getMessage(), 0xFFFF4D5E);
            }
        });
    }

    private static void toast(String title, String text, int color) {
        Mc.mc().execute(() -> Toasts.show("gamepad", title, text, color, 3500));
    }

    // ------------------------------------------------------------------ actions (game thread)
    public static boolean invited(UUID to) {
        Object[] o = OUTGOING.get(to);
        return o != null && System.currentTimeMillis() / 1000 - (long) o[2] < INVITE_S;
    }

    public static void invite(UUID to, String game) {
        if (!GAMES.containsKey(game)) return;
        OUTGOING.put(to, new Object[]{"", game, System.currentTimeMillis() / 1000});
        IO.execute(() -> {
            try {
                Object r = LegoAuth.api("POST", "/api/duels", Map.of("to", to.toString(), "game", game));
                String match = Json.str(Json.obj(r, "duel"), "id", "");
                OUTGOING.put(to, new Object[]{match, game, System.currentTimeMillis() / 1000});
                String n = KNOWN.getOrDefault(to, "Spieler");
                toast("Duell", "Einladung zu " + GAMES.get(game) + " an " + n + " gesendet", 0xFF3FA9FF);
                nextPoll = 0;
            } catch (Exception e) {
                OUTGOING.remove(to);
                toast("Duell", e.getMessage(), 0xFFFF4D5E);
            }
        });
    }

    public static void accept(Invite inv) {
        INCOMING.remove(inv.match());
        IO.execute(() -> {
            try {
                LegoAuth.api("POST", "/api/duels/" + inv.match() + "/accept", Map.of());
                start(new Duel(inv.match(), inv.game(), inv.from(), inv.name(), false));
            } catch (Exception e) {
                toast("Duell", "Annehmen fehlgeschlagen: " + e.getMessage(), 0xFFFF4D5E);
            }
        });
    }

    public static void decline(Invite inv) {
        INCOMING.remove(inv.match());
        async(() -> LegoAuth.api("POST", "/api/duels/" + inv.match() + "/decline", Map.of()), null);
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
            var s = Mc.mc().screen;
            if (s == null || s instanceof LegoScreen) LegoScreen.show(v);
            else Toasts.show("gamepad", "Duell", "Duell gegen " + d.oppName + " startet – schließe das Menü", 0xFF1ED760, 4000);
        });
    }

    // ------------------------------------------------------------------ polling (called every client tick)
    /** Polls the inbox: every 1 s in a duel, every 2 s with open invites, otherwise every 8 s. */
    public static void tick() {
        long now = System.currentTimeMillis();
        if (polling || now < nextPoll || Mc.player() == null) return;
        polling = true;
        long wait = current != null ? 1000 : !OUTGOING.isEmpty() || !INCOMING.isEmpty() ? 2000 : 8000;
        nextPoll = now + wait;
        IO.execute(() -> {
            try {
                Object r = LegoAuth.api("GET", "/api/duels/inbox?since=" + since, null);
                since = Json.num(r, "now", since);
                List<Object> msgs = Json.arr(r, "messages");
                if (msgs != null) for (Object m : msgs) receive(m);
            } catch (Exception e) {
                lastError = e.getMessage();
                nextPoll = System.currentTimeMillis() + 15_000; // offline / not signed in: back off
            } finally {
                polling = false;
            }
        });
    }

    private static void receive(Object msg) {
        String kind = Json.str(msg, "k", ""), game = Json.str(msg, "game", ""), match = Json.str(msg, "match", "");
        Map<String, Object> from = Json.obj(msg, "from");
        UUID fromId;
        try { fromId = UUID.fromString(Json.str(from, "uuid", "")); } catch (IllegalArgumentException e) { return; }
        String name = Json.str(from, "name", "");
        if (!name.matches("[A-Za-z0-9_]{1,16}")) name = "LEGO-Spieler";
        KNOWN.put(fromId, name);
        if (!GAMES.containsKey(game)) return;
        final String n = name;
        switch (kind) {
            case "inv" -> {
                if (INCOMING.size() > 20) return;
                INCOMING.put(match, new Invite(match, fromId, name, game, System.currentTimeMillis() / 1000));
                Mc.mc().execute(() -> {
                    Toasts.show("gamepad", "Duell-Einladung", n + " fordert dich zu " + GAMES.get(game) + " heraus · Spiele → Duelle", 0xFFFFC53D, 6000);
                    dev.lego.ui.Sound.play("minecraft:block.note_block.bell", 1.2f, 0.5f);
                });
            }
            case "acc" -> {
                Object[] o = OUTGOING.remove(fromId);
                if (o == null) return;
                toast("Duell", n + " hat angenommen!", 0xFF1ED760);
                start(new Duel(match, game, fromId, n, true));
            }
            case "dec" -> {
                if (OUTGOING.remove(fromId) != null) toast("Duell", n + " hat abgelehnt", 0xFFFF4D5E);
            }
            case "mv" -> {
                Duel d = current;
                if (d == null || !d.match.equals(match)) return;
                int r = (int) Json.num(msg, "round", -1), v = (int) Json.num(msg, "value", -1);
                if (r < 0 || r > 10_000 || v < 0 || v > 64 || d.moves.size() > 64) return;
                d.moves.add(new int[]{r, v});
            }
            case "bye" -> {
                INCOMING.remove(match);
                Duel d = current;
                if (d != null && d.match.equals(match)) d.oppLeft = true;
            }
            default -> { }
        }
    }
}
