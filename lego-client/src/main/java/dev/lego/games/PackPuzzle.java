package dev.lego.games;

import java.util.ArrayList;
import java.util.List;

import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** Spiele-Paket 4a: Denk- und Brettspiele. */
final class PackPuzzle {
    private PackPuzzle() { }

    static final int[] PAL = {0xFFE3000B, 0xFF006CB7, 0xFFFFCD03, 0xFF00852B, 0xFFFF8A00, 0xFF9B4DCA};

    static void click() { Sound.play("minecraft:ui.button.click", 1.3f, 0.2f); }
    static void good() { Sound.play("minecraft:entity.experience_orb.pickup", 1.3f, 0.3f); }
    static void bad() { Sound.play("minecraft:block.note_block.bass", 0.8f, 0.4f); }
    static void win() { Sound.play("minecraft:entity.player.levelup", 1.2f, 0.4f); }
}

// ====================================================================== Lichter aus
final class LightsOut extends GameView {
    private static final int N = 5;
    private static final float C = 50, OX = 20, OY = 20;
    private final boolean[][] on = new boolean[N][N];
    private int moves;
    private float doneT = -1;

    LightsOut() { super("lightsout"); }
    @Override protected float boardW() { return 400; }
    @Override protected float boardH() { return 290; }
    @Override protected String scoreLabel() { return "Züge"; }
    @Override protected String overTitle() { return "Alle Lichter aus!"; }

    @Override protected void reset() {
        for (boolean[] r : on) java.util.Arrays.fill(r, false);
        int k = 8 + rnd.nextInt(8);
        for (int i = 0; i < k; i++) flip(rnd.nextInt(N), rnd.nextInt(N));
        if (lit() == 0) flip(2, 2);
        moves = 0; doneT = -1;
    }

    private void flip(int r, int c) {
        int[][] d = {{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] q : d) { int rr = r + q[0], cc = c + q[1]; if (rr >= 0 && cc >= 0 && rr < N && cc < N) on[rr][cc] = !on[rr][cc]; }
    }

    private int lit() { int n = 0; for (boolean[] r : on) for (boolean b : r) if (b) n++; return n; }

    @Override protected void update(float dt) { if (doneT >= 0 && (doneT += dt) > 0.8f) { score = moves; gameOver(); } }

    @Override protected void onClick(float x, float y, int b) {
        if (doneT >= 0) return;
        int c = (int) ((x - OX) / C), r = (int) ((y - OY) / C);
        if (x < OX || y < OY || c >= N || r >= N) return;
        flip(r, c); moves++; score = moves; PackPuzzle.click();
        if (lit() == 0) { doneT = 0; PackPuzzle.win(); }
    }

    @Override protected void render() {
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) {
            float x = OX + c * C, y = OY + r * C;
            boolean l = on[r][c];
            if (l) Gx.glow(X(x + C / 2), Y(y + C / 2), S(34), 0x55FFE14D);
            box(x + 3, y + 3, C - 6, C - 6, 9, l ? 0xFFFFE14D : (Style.light ? 0xFFD0D4DC : 0xFF22252E));
            if (l) circle(x + C / 2, y + C / 2, 7, 0xFFFFF6C0);
        }
        float px = 300;
        text("Lichter an", px + 40, 40, 7, Fonts.MEDIUM, Style.sub);
        text(String.valueOf(lit()), px + 40, 62, 18, Fonts.BOLD, Style.text);
        text("Züge", px + 40, 100, 7, Fonts.MEDIUM, Style.sub);
        text(String.valueOf(moves), px + 40, 122, 18, Fonts.BOLD, Style.text);
        text("Klick schaltet das", px + 40, 180, 6.4f, Fonts.REGULAR, Style.sub);
        text("Feld + Nachbarn um", px + 40, 192, 6.4f, Fonts.REGULAR, Style.sub);
    }
}

// ====================================================================== Schiebepuzzle
final class SlidePuzzle extends GameView {
    private static final int N = 4;
    private static final float C = 62, OX = 14, OY = 14;
    private final int[] t = new int[N * N];
    private final float[] ax = new float[N * N], ay = new float[N * N];
    private int moves;
    private float doneT = -1;

    SlidePuzzle() { super("slidepuzzle"); }
    @Override protected float boardW() { return 400; }
    @Override protected float boardH() { return 276; }
    @Override protected String scoreLabel() { return "Züge"; }
    @Override protected String overTitle() { return "Gelöst!"; }

    @Override protected void reset() {
        for (int i = 0; i < N * N; i++) t[i] = (i + 1) % (N * N);
        int e = N * N - 1, last = -1;
        for (int k = 0; k < 400; k++) {
            List<Integer> nb = new ArrayList<>();
            int er = e / N, ec = e % N;
            if (er > 0) nb.add(e - N); if (er < N - 1) nb.add(e + N); if (ec > 0) nb.add(e - 1); if (ec < N - 1) nb.add(e + 1);
            nb.remove(Integer.valueOf(last));
            int n = nb.get(rnd.nextInt(nb.size()));
            t[e] = t[n]; t[n] = 0; last = e; e = n;
        }
        for (int i = 0; i < N * N; i++) { ax[t[i]] = i % N; ay[t[i]] = i / N; }
        moves = 0; doneT = -1;
    }

    private int empty() { for (int i = 0; i < t.length; i++) if (t[i] == 0) return i; return -1; }

    private boolean solved() { for (int i = 0; i < N * N - 1; i++) if (t[i] != i + 1) return false; return true; }

    private void tryMove(int i) {
        int e = empty();
        if (i < 0 || i >= N * N || doneT >= 0) return;
        if (Math.abs(i / N - e / N) + Math.abs(i % N - e % N) != 1) return;
        t[e] = t[i]; t[i] = 0; moves++; score = moves; PackPuzzle.click();
        if (solved()) { doneT = 0; PackPuzzle.win(); }
    }

    @Override protected void onClick(float x, float y, int b) {
        int c = (int) ((x - OX) / C), r = (int) ((y - OY) / C);
        if (x >= OX && y >= OY && c < N && r < N) tryMove(r * N + c);
    }

    @Override protected void onKey(int k) {
        int e = empty();
        if (k == K_LEFT || k == K_A) { if (e % N < N - 1) tryMove(e + 1); }
        else if (k == K_RIGHT || k == K_D) { if (e % N > 0) tryMove(e - 1); }
        else if (k == K_UP || k == K_W) { if (e / N < N - 1) tryMove(e + N); }
        else if (k == K_DOWN || k == K_S) { if (e / N > 0) tryMove(e - N); }
    }

    @Override protected void update(float dt) { if (doneT >= 0 && (doneT += dt) > 0.8f) gameOver(); }

    @Override protected void render() {
        float k = 1 - (float) Math.exp(-Gx.dt * 18);
        box(OX - 4, OY - 4, N * C + 8, N * C + 8, 12, Style.light ? 0x14000000 : 0x14FFFFFF);
        for (int i = 0; i < N * N; i++) {
            int v = t[i];
            if (v == 0) continue;
            ax[v] += (i % N - ax[v]) * k; ay[v] += (i / N - ay[v]) * k;
            float x = OX + ax[v] * C, y = OY + ay[v] * C;
            boolean right = v == i + 1;
            brick(x + 3, y + 3, C - 6, C - 6, right ? 0xFF00852B : PackPuzzle.PAL[(v - 1) / 4 % 4]);
            text(String.valueOf(v), x + C / 2, y + C / 2 + 4, 15, Fonts.BOLD, 0xFFFFFFFF);
        }
        text("Züge: " + moves, 340, 60, 9, Fonts.BOLD, Style.text);
        text("Sortiere 1-15", 340, 100, 6.6f, Fonts.REGULAR, Style.sub);
        text("Klick oder Pfeile", 340, 114, 6.6f, Fonts.REGULAR, Style.sub);
    }
}

// ====================================================================== Code knacken (Mastermind)
final class Mastermind extends GameView {
    private static final int ROWS = 10, PEGS = 4;
    private final int[] code = new int[PEGS];
    private final int[][] guess = new int[ROWS][PEGS];
    private final int[][] fb = new int[ROWS][2];
    private int row, slot, result;

    Mastermind() { super("mastermind"); }
    @Override protected float boardW() { return 400; }
    @Override protected float boardH() { return 300; }
    @Override protected String overTitle() { return result == 1 ? "Code geknackt!" : "Leider nicht"; }

    @Override protected void reset() {
        for (int i = 0; i < PEGS; i++) code[i] = rnd.nextInt(6);
        for (int[] g : guess) java.util.Arrays.fill(g, -1);
        for (int[] f : fb) java.util.Arrays.fill(f, 0);
        row = 0; slot = 0; result = 0;
    }

    @Override protected void update(float dt) { }

    private void check() {
        for (int v : guess[row]) if (v < 0) return;
        int black = 0, white = 0;
        int[] cc = new int[6], gc = new int[6];
        for (int i = 0; i < PEGS; i++) { if (guess[row][i] == code[i]) black++; else { cc[code[i]]++; gc[guess[row][i]]++; } }
        for (int c = 0; c < 6; c++) white += Math.min(cc[c], gc[c]);
        fb[row][0] = black; fb[row][1] = white;
        if (black == PEGS) { result = 1; score = (ROWS - row) * 10; PackPuzzle.win(); row++; gameOver(); return; }
        row++; slot = 0;
        if (row >= ROWS) { result = 2; score = 0; PackPuzzle.bad(); gameOver(); } else PackPuzzle.click();
    }

    @Override protected void onClick(float x, float y, int b) {
        if (row >= ROWS || result != 0) return;
        for (int c = 0; c < 6; c++) if (Math.hypot(x - (300 + (c % 2) * 40), y - (60 + (c / 2) * 40)) < 15) { guess[row][slot] = c; slot = Math.min(PEGS - 1, slot + 1); for (int i = 0; i < PEGS; i++) if (guess[row][i] < 0) { slot = i; break; } PackPuzzle.click(); return; }
        for (int i = 0; i < PEGS; i++) if (Math.hypot(x - (40 + i * 26), y - (20 + row * 27)) < 12) { if (b == 1) guess[row][i] = -1; slot = i; return; }
        if (in(x, y, 280, 190, 80, 26)) check();
    }

    @Override protected void onKey(int k) { if (k == K_ENTER) check(); if (k >= 49 && k <= 54) { guess[row][slot] = k - 49; slot = Math.min(PEGS - 1, slot + 1); } }

    @Override protected void render() {
        for (int r = 0; r < ROWS; r++) {
            float y = 20 + r * 27;
            if (r == row && result == 0) box(20, y - 12, 220, 24, 8, Gx.withAlpha(Style.accent, 0.14f));
            for (int i = 0; i < PEGS; i++) {
                int v = guess[r][i];
                circle(40 + i * 26, y, 10, v < 0 ? (Style.light ? 0x22000000 : 0x22FFFFFF) : PackPuzzle.PAL[v]);
                if (r == row && i == slot && result == 0) ring(40 + i * 26, y, 11.5f, 1.2f, Style.text);
            }
            for (int i = 0; i < PEGS; i++) {
                int col = i < fb[r][0] ? 0xFF111111 : i < fb[r][0] + fb[r][1] ? 0xFFFFFFFF : (Style.light ? 0x18000000 : 0x18FFFFFF);
                circle(160 + (i % 2) * 10, y - 4 + (i / 2) * 9, 3.5f, col);
                if (col == 0xFFFFFFFF || col == 0xFF111111) ring(160 + (i % 2) * 10, y - 4 + (i / 2) * 9, 3.5f, 0.6f, 0x66888888);
            }
        }
        for (int c = 0; c < 6; c++) circle(300 + (c % 2) * 40, 60 + (c / 2) * 40, 14, PackPuzzle.PAL[c]);
        text("Farbe wählen (1-6)", 320, 30, 6.4f, Fonts.MEDIUM, Style.sub);
        button("Prüfen", 280, 190, 80, 26, Style.accent);
        text("schwarz = richtig", 320, 238, 6, Fonts.REGULAR, Style.sub);
        text("weiß = falscher Platz", 320, 250, 6, Fonts.REGULAR, Style.sub);
        if (result != 0) for (int i = 0; i < PEGS; i++) circle(282 + i * 24, 280, 9, PackPuzzle.PAL[code[i]]);
    }
}

// ====================================================================== Wort-Spiele: gemeinsame Wörter
final class Words {
    private Words() { }
    static final String[] FIVE = {"HAUSE", "BLUME", "ERNTE", "KATZE", "STEIN", "BRIEF", "WOLKE", "LAMPE", "TISCH", "STUHL", "FISCH", "VOGEL", "PFERD", "SONNE", "REGEN", "NEBEL", "BLITZ", "WIESE", "INSEL", "STADT", "BIRNE", "TRAUM", "KUGEL", "SPIEL", "PIZZA", "NUDEL", "KRONE", "SCHAF", "ZEBRA", "TIGER", "PANDA", "ADLER", "HONIG", "KERZE", "BAUER", "RITTER", "FEUER", "WASSER", "KREIS", "PFEIL", "SCHUH", "HOSEN", "MANTEL", "BRUST", "FARBE", "GRUEN", "LEDER", "KISTE", "ANKER", "BOOTE", "FLOSS", "GABEL", "TASSE", "MOEWE", "BIENE", "RAUPE", "KAKAO", "MUSIK", "NOTEN", "GEIGE", "ORGEL", "KLANG", "STERN", "PLANE", "NACHT", "MORGEN", "ABEND", "WOCHE", "MONAT", "JAHRE", "LICHT", "SCHAL", "HANDY", "MAUER", "STROH", "WEIZEN", "MEHL", "BROTE", "KUCHEN", "TORTE", "CREME", "SAHNE", "ZUCKER", "SALAT", "APFEL", "ZWERG", "RIESE", "HEXEN", "BESEN", "ZAUBER", "DRACHE", "BURGEN", "SCHATZ", "KARTE", "LEGOS"};
    static final String[] ANY = {"MINECRAFT", "CREEPER", "DIAMANT", "SPITZHACKE", "ENDERMAN", "NETHER", "SCHWERT", "RUESTUNG", "WERKBANK", "OFEN", "KISTE", "FACKEL", "REDSTONE", "PORTAL", "OBSIDIAN", "DORFBEWOHNER", "SKELETT", "ZOMBIE", "SPINNE", "SCHLEIM", "WOLF", "KATZE", "PAPAGEI", "SCHILDKROETE", "AXOLOTL", "BIENENSTOCK", "LEUCHTSTEIN", "SMARAGD", "ELYTRA", "TOTEM", "TRIDENT", "ANGEL", "BOGEN", "ARMBRUST", "KUERBIS", "MELONE", "KARTOFFEL", "WEIZEN", "ZUCKERROHR", "BAMBUS", "KAKTUS", "LAVA", "WASSER", "SAND", "KIES", "GRANIT", "BASALT", "AMETHYST", "KUPFER", "EISEN", "GOLD", "KOHLE", "LAPISLAZULI", "QUARZ", "BIBLIOTHEK", "ZAUBERTISCH", "AMBOSS", "BRAUSTAND", "LEGOSTEIN", "BAUKASTEN", "RAUMSCHIFF", "POLIZEI", "FEUERWEHR", "PIRATENSCHIFF", "RITTERBURG", "DINOSAURIER", "ACHTERBAHN", "SCHNEEMANN", "WEIHNACHTEN", "GESCHENK", "SCHLITTEN", "RENTIER", "LEBKUCHEN"};

    static List<String> five() {
        List<String> l = new ArrayList<>();
        for (String w : FIVE) if (w.length() == 5) l.add(w);
        return l;
    }

    static String norm(String s) {
        return s.toUpperCase(java.util.Locale.ROOT).replace("Ä", "AE").replace("Ö", "OE").replace("Ü", "UE").replace("ß", "SS");
    }
}

// ====================================================================== Galgenraten
final class Hangman extends GameView {
    private String word;
    private final java.util.Set<Character> tried = new java.util.HashSet<>();
    private int wrong, streak;
    private float nextT = -1;

    Hangman() { super("hangman"); }
    @Override protected float boardW() { return 420; }
    @Override protected float boardH() { return 300; }
    @Override protected String scoreLabel() { return "Wörter"; }
    @Override protected String overTitle() { return "Gehängt! Wort: " + word; }

    @Override protected void reset() { streak = 0; next(); }

    private void next() { word = Words.ANY[rnd.nextInt(Words.ANY.length)]; tried.clear(); wrong = 0; nextT = -1; }

    private boolean solved() { for (char c : word.toCharArray()) if (!tried.contains(c)) return false; return true; }

    private void guess(char c) {
        if (nextT >= 0 || c < 'A' || c > 'Z' || !tried.add(c)) return;
        if (word.indexOf(c) < 0) { wrong++; PackPuzzle.bad(); if (wrong >= 8) { score = streak; gameOver(); } }
        else { PackPuzzle.good(); if (solved()) { streak++; score = streak; nextT = 0; PackPuzzle.win(); } }
    }

    @Override public boolean chr(String s) {
        if (state == State.PLAYING) for (char ch : Words.norm(s).toCharArray()) guess(ch);
        return true;
    }

    @Override protected void update(float dt) { if (nextT >= 0 && (nextT += dt) > 1.4f) next(); }

    @Override protected void onClick(float x, float y, int b) {
        for (int i = 0; i < 26; i++) if (in(x, y, 20 + (i % 13) * 30, 220 + (i / 13) * 32, 26, 26)) guess((char) ('A' + i));
    }

    @Override protected void render() {
        int st = Style.text;
        // gallows
        if (wrong > 0) line(30, 190, 110, 190, 3, st);
        if (wrong > 1) line(50, 190, 50, 30, 3, st);
        if (wrong > 2) line(50, 30, 120, 30, 3, st);
        if (wrong > 3) line(120, 30, 120, 50, 2, st);
        if (wrong > 4) ring(120, 62, 12, 2, st);
        if (wrong > 5) line(120, 74, 120, 120, 2.4f, st);
        if (wrong > 6) { line(120, 85, 102, 105, 2, st); line(120, 85, 138, 105, 2, st); }
        if (wrong > 7) { line(120, 120, 104, 150, 2, st); line(120, 120, 136, 150, 2, st); }
        // word
        float cw = Math.min(22, 250f / word.length());
        float sx = 160 + (250 - cw * word.length()) / 2;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            float x = sx + i * cw;
            box(x + 2, 120, cw - 4, 2.5f, 1, Style.sub);
            boolean show = tried.contains(c) || state == State.OVER;
            if (show) text(String.valueOf(c), x + cw / 2, 108, cw * 0.62f, Fonts.BOLD, tried.contains(c) ? st : Style.danger);
        }
        text("Fehler: " + wrong + " / 8", 285, 40, 8, Fonts.BOLD, wrong >= 6 ? Style.danger : Style.sub);
        text("Serie: " + streak, 285, 58, 8, Fonts.BOLD, Style.sub);
        if (nextT >= 0) text("Richtig!", 285, 160, 12, Fonts.BOLD, Style.ok);
        for (int i = 0; i < 26; i++) {
            char c = (char) ('A' + i);
            float x = 20 + (i % 13) * 30, y = 220 + (i / 13) * 32;
            boolean t = tried.contains(c);
            int col = !t ? (Style.light ? 0xFFD0D4DC : 0xFF2A2E38) : word.indexOf(c) >= 0 ? 0xFF00852B : 0xFF5A1A20;
            box(x, y, 26, 26, 6, col);
            text(String.valueOf(c), x + 13, y + 14, 9, Fonts.BOLD, t ? 0xAAFFFFFF : st);
        }
    }
}

// ====================================================================== Wortratespiel (Wordle)
final class WordGuess extends GameView {
    private static final int TRIES = 6;
    private String target;
    private final String[] rows = new String[TRIES];
    private final int[][] res = new int[TRIES][5];      // 0 grau, 1 gelb, 2 grün
    private int row, streak;
    private String cur = "";
    private float nextT = -1, shake;
    private final java.util.Map<Character, Integer> keys = new java.util.HashMap<>();
    private final List<String> list = Words.five();

    WordGuess() { super("wordguess"); }
    @Override protected float boardW() { return 400; }
    @Override protected float boardH() { return 320; }
    @Override protected String scoreLabel() { return "Serie"; }
    @Override protected String overTitle() { return "Das Wort war " + target; }

    @Override protected void reset() { streak = 0; next(); }

    private void next() { target = list.get(rnd.nextInt(list.size())); java.util.Arrays.fill(rows, null); row = 0; cur = ""; nextT = -1; keys.clear(); }

    private void submit() {
        if (cur.length() != 5 || nextT >= 0) { shake = 0.3f; return; }
        int[] r = new int[5];
        int[] cnt = new int[26];
        for (int i = 0; i < 5; i++) if (cur.charAt(i) == target.charAt(i)) r[i] = 2; else cnt[target.charAt(i) - 'A']++;
        for (int i = 0; i < 5; i++) if (r[i] == 0 && cnt[cur.charAt(i) - 'A'] > 0) { r[i] = 1; cnt[cur.charAt(i) - 'A']--; }
        rows[row] = cur; res[row] = r;
        for (int i = 0; i < 5; i++) keys.merge(cur.charAt(i), r[i], Math::max);
        row++;
        if (cur.equals(target)) { streak++; score = streak; nextT = 0; PackPuzzle.win(); }
        else if (row >= TRIES) { score = streak; PackPuzzle.bad(); gameOver(); }
        else PackPuzzle.click();
        cur = "";
    }

    @Override public boolean chr(String s) {
        if (state != State.PLAYING || nextT >= 0) return true;
        for (char c : Words.norm(s).toCharArray()) if (c >= 'A' && c <= 'Z' && cur.length() < 5) cur += c;
        return true;
    }

    @Override protected void onKey(int k) {
        if (k == 259 && !cur.isEmpty()) cur = cur.substring(0, cur.length() - 1);
        if (k == K_ENTER) submit();
    }

    @Override protected void update(float dt) { shake = Math.max(0, shake - dt); if (nextT >= 0 && (nextT += dt) > 1.5f) next(); }

    private static final String KB = "QWERTZUIOPASDFGHJKLYXCVBNM";

    @Override protected void onClick(float x, float y, int b) {
        for (int i = 0; i < KB.length(); i++) { float[] p = kpos(i); if (in(x, y, p[0], p[1], 24, 24)) chr(String.valueOf(KB.charAt(i))); }
        if (in(x, y, 312, 296, 60, 20)) submit();
        if (in(x, y, 28, 296, 60, 20)) onKey(259);
    }

    private float[] kpos(int i) { int r = i < 10 ? 0 : i < 19 ? 1 : 2; int c = r == 0 ? i : r == 1 ? i - 10 : i - 19; return new float[]{50 + r * 13 + c * 30, 218 + r * 26}; }

    private int col(int v) { return v == 2 ? 0xFF2E9E4A : v == 1 ? 0xFFD4A20A : 0xFF4A4E58; }

    @Override protected void render() {
        float sx = 200 - 5 * 34 / 2f;
        for (int r = 0; r < TRIES; r++) for (int i = 0; i < 5; i++) {
            float x = sx + i * 34 + (r == row ? (float) Math.sin(shake * 60) * shake * 10 : 0), y = 8 + r * 34;
            String w = rows[r];
            char c = w != null ? w.charAt(i) : r == row && i < cur.length() ? cur.charAt(i) : ' ';
            int bg = w != null ? col(res[r][i]) : (Style.light ? 0xFFE6E8EE : 0xFF1C1F27);
            box(x, y, 30, 30, 5, bg);
            if (w == null) Gx.outline(X(x), Y(y), S(30), S(30), S(5), Math.max(1, S(0.8f)), r == row ? Style.sub : Style.stroke);
            if (c != ' ') text(String.valueOf(c), x + 15, y + 16, 13, Fonts.BOLD, w != null ? 0xFFFFFFFF : Style.text);
        }
        for (int i = 0; i < KB.length(); i++) {
            float[] p = kpos(i);
            Integer v = keys.get(KB.charAt(i));
            box(p[0], p[1], 24, 24, 5, v == null ? (Style.light ? 0xFFD0D4DC : 0xFF2A2E38) : col(v));
            text(String.valueOf(KB.charAt(i)), p[0] + 12, p[1] + 13, 8, Fonts.BOLD, v == null ? Style.text : 0xFFFFFFFF);
        }
        button("⌫", 28, 296, 60, 20, 0xFF5A6270);
        button("Prüfen", 312, 296, 60, 20, Style.accent);
        text("Serie: " + streak, 200, 306, 7, Fonts.BOLD, Style.sub);
        if (nextT >= 0) text("Richtig!", 200, 112, 16, Fonts.BOLD, 0xFF39D353);
    }
}

// ====================================================================== Reversi
final class Reversi extends GameView {
    private static final float C = 33, OX = 10, OY = 10;
    private static final int[] W = {100, -20, 10, 5, 5, 10, -20, 100, -20, -50, -2, -2, -2, -2, -50, -20, 10, -2, 1, 1, 1, 1, -2, 10, 5, -2, 1, 0, 0, 1, -2, 5,
            5, -2, 1, 0, 0, 1, -2, 5, 10, -2, 1, 1, 1, 1, -2, 10, -20, -50, -2, -2, -2, -2, -50, -20, 100, -20, 10, 5, 5, 10, -20, 100};
    private int[] b = new int[64];
    private int turn, ended;
    private float think;
    private String msg = "";
    private final Duels.Duel duel;
    private int duelRound = -1;

    Reversi() { super("reversi"); duel = null; }
    Reversi(Duels.Duel d) { super("reversi"); duel = d; }
    @Override protected void start() { if (duel != null && duel.oppLeft) { back(); return; } super.start(); if (duel != null) duelRound++; }
    @Override protected void back() { if (duel != null) duel.leave(); super.back(); }
    @Override public void removed() { if (duel != null) duel.leave(); super.removed(); }
    @Override protected boolean hasAi() { return duel == null; }
    @Override protected float boardW() { return 400; }
    @Override protected float boardH() { return 284; }
    @Override protected String scoreLabel() { return "Steine"; }
    @Override protected String overTitle() { return ended == 1 ? "Gewonnen!" : ended == 2 ? "Verloren" : "Unentschieden"; }

    @Override protected void reset() { b = new int[64]; b[27] = b[36] = 2; b[28] = b[35] = 1; turn = 1; ended = 0; think = 0; msg = ""; }

    private static List<Integer> flips(int[] b, int p, int who) {
        List<Integer> out = new ArrayList<>();
        if (b[p] != 0) return out;
        int r = p / 8, c = p % 8;
        for (int dr = -1; dr <= 1; dr++) for (int dc = -1; dc <= 1; dc++) {
            if (dr == 0 && dc == 0) continue;
            List<Integer> line = new ArrayList<>();
            int rr = r + dr, cc = c + dc;
            while (rr >= 0 && cc >= 0 && rr < 8 && cc < 8 && b[rr * 8 + cc] == 3 - who) { line.add(rr * 8 + cc); rr += dr; cc += dc; }
            if (!line.isEmpty() && rr >= 0 && cc >= 0 && rr < 8 && cc < 8 && b[rr * 8 + cc] == who) out.addAll(line);
        }
        return out;
    }

    private static List<Integer> moves(int[] b, int who) { List<Integer> m = new ArrayList<>(); for (int i = 0; i < 64; i++) if (!flips(b, i, who).isEmpty()) m.add(i); return m; }

    private static void play(int[] b, int p, int who) { for (int f : flips(b, p, who)) b[f] = who; b[p] = who; }

    private int count(int who) { int n = 0; for (int v : b) if (v == who) n++; return n; }

    private static int eval(int[] b) { int s = 0; for (int i = 0; i < 64; i++) s += b[i] == 2 ? W[i] : b[i] == 1 ? -W[i] : 0; return s + (moves(b, 2).size() - moves(b, 1).size()) * 3; }

    private static int mini(int[] b, int d, int who, int a, int be) {
        if (d == 0) return eval(b);
        List<Integer> ms = moves(b, who);
        if (ms.isEmpty()) { if (moves(b, 3 - who).isEmpty()) { int n = 0; for (int v : b) n += v == 2 ? 1 : v == 1 ? -1 : 0; return n * 1000; } return mini(b, d - 1, 3 - who, a, be); }
        for (int m : ms) {
            int[] c = b.clone(); play(c, m, who);
            int v = mini(c, d - 1, 3 - who, a, be);
            if (who == 2) a = Math.max(a, v); else be = Math.min(be, v);
            if (a >= be) break;
        }
        return who == 2 ? a : be;
    }

    private int ai() {
        List<Integer> ms = moves(b, 2);
        int lv = aiLevel();
        if (lv == 0 && rnd.nextFloat() < 0.6f) return ms.get(rnd.nextInt(ms.size()));
        int depth = new int[]{1, 1, 3, 5}[lv], best = Integer.MIN_VALUE, pick = ms.get(0);
        for (int m : ms) {
            int[] c = b.clone(); play(c, m, 2);
            int v = depth == 1 ? eval(c) + rnd.nextInt(3) : mini(c, depth - 1, 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (v > best) { best = v; pick = m; }
        }
        return pick;
    }

    private void advance() {
        turn = 3 - turn;
        if (moves(b, turn).isEmpty()) {
            if (moves(b, 3 - turn).isEmpty()) { finish(); return; }
            String oppLabel = duel != null ? duel.oppName : "KI";
            msg = turn == 1 ? "Du musst aussetzen" : oppLabel + " muss aussetzen";
            turn = 3 - turn;
        } else msg = "";
        think = 0;
        if (duel != null) duelRound++;
    }

    private void finish() {
        int p = count(1), k = count(2);
        ended = p > k ? 1 : p < k ? 2 : 3;
        score = ended == 1 ? p : 0;
        if (ended == 1) PackPuzzle.win(); else PackPuzzle.bad();
        gameOver();
    }

    @Override protected void update(float dt) {
        if (duel != null && duel.oppLeft && ended == 0) { ended = 1; score = count(1); gameOver(false); return; }
        if (turn == 2 && ended == 0) {
            if (duel != null) {
                Integer m = duel.take(duelRound);
                if (m != null && m >= 0 && m < 64 && !flips(b, m, 2).isEmpty()) { play(b, m, 2); PackPuzzle.click(); advance(); }
            } else { think += dt; if (think > 0.5f) { play(b, ai(), 2); PackPuzzle.click(); advance(); } }
        }
    }

    @Override protected void onClick(float x, float y, int bt) {
        if (turn != 1 || ended != 0) return;
        int c = (int) ((x - OX) / C), r = (int) ((y - OY) / C);
        if (x < OX || y < OY || c > 7 || r > 7) return;
        int pos = r * 8 + c;
        if (flips(b, pos, 1).isEmpty()) return;
        play(b, pos, 1); PackPuzzle.click();
        if (duel != null) duel.send(duelRound, pos);
        advance();
    }

    @Override protected void render() {
        box(OX - 4, OY - 4, 8 * C + 8, 8 * C + 8, 8, 0xFF1E6B3A);
        List<Integer> legal = turn == 1 ? moves(b, 1) : List.of();
        for (int i = 0; i < 64; i++) {
            float x = OX + (i % 8) * C, y = OY + (i / 8) * C;
            box(x + 1, y + 1, C - 2, C - 2, 3, 0xFF2E8B4E);
            if (b[i] != 0) { circle(x + C / 2, y + C / 2 + 1.5f, C * 0.38f, 0x66000000); circle(x + C / 2, y + C / 2, C * 0.38f, b[i] == 1 ? 0xFF15161A : 0xFFF4F4F4); }
            else if (legal.contains(i)) circle(x + C / 2, y + C / 2, 3.5f, 0x55FFFFFF);
        }
        float px = 300;
        String oppLabel = duel != null ? duel.oppName : "KI";
        circle(px + 10, 40, 10, 0xFF15161A); textL("Du: " + count(1), px + 26, 40, 9, Fonts.BOLD, Style.text);
        circle(px + 10, 70, 10, 0xFFF4F4F4); textL(oppLabel + ": " + count(2), px + 26, 70, 9, Fonts.BOLD, Style.text);
        text(turn == 1 ? "Du bist dran" : oppLabel + " denkt...", px + 45, 110, 8, Fonts.BOLD, turn == 1 ? Style.ok : Style.sub);
        if (!msg.isEmpty()) text(msg, px + 45, 130, 6.6f, Fonts.MEDIUM, Style.danger);
        if (duel == null) text("KI: " + GameInfo.LEVELS[aiLevel()], px + 45, 260, 6.6f, Fonts.MEDIUM, Style.sub);
    }
}

// ====================================================================== Streichhölzer (Nim)
final class Nim extends GameView {
    private final int[] rows = new int[4];
    private int selRow = -1, selN = 0, turn = 1, winner;
    private float think;
    private int wins;
    private final Duels.Duel duel;
    private int duelRound = -1;

    Nim() { super("nim"); duel = null; }
    Nim(Duels.Duel d) { super("nim"); duel = d; }
    @Override protected void start() { if (duel != null && duel.oppLeft) { back(); return; } super.start(); if (duel != null) duelRound++; }
    @Override protected void back() { if (duel != null) duel.leave(); super.back(); }
    @Override public void removed() { if (duel != null) duel.leave(); super.removed(); }
    @Override protected boolean hasAi() { return duel == null; }
    @Override protected float boardW() { return 400; }
    @Override protected float boardH() { return 280; }
    @Override protected String scoreLabel() { return "Siege"; }
    @Override protected String overTitle() { return winner == 1 ? "Gewonnen!" : "Verloren - du hattest das letzte Holz"; }

    @Override protected void reset() { rows[0] = 1; rows[1] = 3; rows[2] = 5; rows[3] = 7; selRow = -1; selN = 0; turn = duel != null ? (duel.iStart ? 1 : 2) : (rnd.nextBoolean() ? 1 : 2); winner = 0; think = 0; }

    private int total() { return rows[0] + rows[1] + rows[2] + rows[3]; }

    private void take(int r, int n) {
        rows[r] -= n; PackPuzzle.click();
        if (total() == 0) { winner = turn == 1 ? 2 : 1; if (winner == 1) { wins++; score = wins; PackPuzzle.win(); } else { score = wins; PackPuzzle.bad(); } gameOver(); return; }
        turn = 3 - turn; think = 0; selRow = -1; selN = 0;
        if (duel != null) duelRound++;
    }

    private int[] aiMove() {
        int lv = aiLevel();
        if (lv < 3 && rnd.nextFloat() < new float[]{0.7f, 0.4f, 0.12f}[lv]) {
            List<Integer> nz = new ArrayList<>();
            for (int i = 0; i < 4; i++) if (rows[i] > 0) nz.add(i);
            int r = nz.get(rnd.nextInt(nz.size()));
            return new int[]{r, 1 + rnd.nextInt(rows[r])};
        }
        // misère nim: play normal nim unless all heaps would be <= 1
        int big = 0, x = 0;
        for (int v : rows) { if (v > 1) big++; x ^= v; }
        for (int i = 0; i < 4; i++) {
            if (rows[i] == 0) continue;
            for (int n = 1; n <= rows[i]; n++) {
                int[] c = rows.clone(); c[i] -= n;
                int b2 = 0, x2 = 0, ones = 0;
                for (int v : c) { if (v > 1) b2++; x2 ^= v; if (v == 1) ones++; }
                boolean good = b2 == 0 ? ones % 2 == 1 : x2 == 0;
                if (good) return new int[]{i, n};
            }
        }
        for (int i = 0; i < 4; i++) if (rows[i] > 0) return new int[]{i, 1};
        return new int[]{0, 0};
    }

    @Override protected void update(float dt) {
        if (duel != null && duel.oppLeft && winner == 0) { winner = 1; wins++; score = wins; gameOver(false); return; }
        if (turn == 2 && winner == 0) {
            if (duel != null) {
                Integer m = duel.take(duelRound);
                if (m != null) { int r = m / 8, n = m % 8; if (r >= 0 && r < 4 && n >= 1 && n <= rows[r]) take(r, n); }
            } else if ((think += dt) > 0.8f) { int[] m = aiMove(); take(m[0], m[1]); }
        }
    }

    private float mx(int r, int i) { return 200 + (i - r) * 28; }

    @Override protected void onClick(float x, float y, int b) {
        if (turn != 1 || winner != 0) return;
        if (in(x, y, 150, 240, 100, 26) && selN > 0) { if (duel != null) duel.send(duelRound, selRow * 8 + selN); take(selRow, selN); return; }
        for (int r = 0; r < 4; r++) for (int i = 0; i < rows[r]; i++)
            if (Math.abs(x - mx(r, i)) < 11 && Math.abs(y - (40 + r * 50)) < 22) {
                if (selRow != r) { selRow = r; selN = 0; }
                selN = rows[r] - i;
                PackPuzzle.click();
            }
    }

    @Override protected void render() {
        for (int r = 0; r < 4; r++) for (int i = 0; i < rows[r]; i++) {
            float x = mx(r, i), y = 40 + r * 50;
            boolean sel = r == selRow && i >= rows[r] - selN;
            box(x - 2.5f, y - 18, 5, 36, 2, sel ? 0xFFFFCD03 : 0xFFE8C77A);
            circle(x, y - 19, 4, sel ? 0xFFFFE14D : 0xFFD7263D);
        }
        String oppLabel = duel != null ? duel.oppName : "KI";
        if (turn == 1 && selN > 0) button("Nimm " + selN, 150, 240, 100, 26, Style.accent);
        else text(turn == 1 ? "Wähle Hölzer aus einer Reihe" : oppLabel + " überlegt...", 200, 253, 8, Fonts.BOLD, turn == 1 ? Style.ok : Style.sub);
        text("Wer das letzte Holz nimmt, verliert", 200, 225, 6.4f, Fonts.MEDIUM, Style.sub);
    }
}

// ====================================================================== Käsekästchen
final class DotsBoxes extends GameView {
    private static final int N = 4;
    private static final float S0 = 54, OX = 30, OY = 22;
    private boolean[][] h = new boolean[N + 1][N], v = new boolean[N][N + 1];
    private int[][] owner = new int[N][N];
    private int turn = 1, sp, sa;
    private float think;

    DotsBoxes() { super("dotsboxes"); }
    @Override protected boolean hasAi() { return true; }
    @Override protected float boardW() { return 400; }
    @Override protected float boardH() { return 270; }
    @Override protected String scoreLabel() { return "Kästchen"; }
    @Override protected String overTitle() { return sp > sa ? "Gewonnen!" : sp < sa ? "Verloren" : "Unentschieden"; }

    @Override protected void reset() { h = new boolean[N + 1][N]; v = new boolean[N][N + 1]; owner = new int[N][N]; turn = 1; sp = sa = 0; think = 0; }

    private int sides(int r, int c) { return (h[r][c] ? 1 : 0) + (h[r + 1][c] ? 1 : 0) + (v[r][c] ? 1 : 0) + (v[r][c + 1] ? 1 : 0); }

    /** line: {0=h/1=v, r, c} */
    private boolean draw(int[] l, int who) {
        if (l[0] == 0) { if (h[l[1]][l[2]]) return false; h[l[1]][l[2]] = true; } else { if (v[l[1]][l[2]]) return false; v[l[1]][l[2]] = true; }
        boolean scored = false;
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) if (owner[r][c] == 0 && sides(r, c) == 4) { owner[r][c] = who; scored = true; if (who == 1) sp++; else sa++; }
        PackPuzzle.click();
        if (sp + sa == N * N) { score = sp > sa ? sp : 0; if (sp > sa) PackPuzzle.win(); gameOver(); return true; }
        if (!scored) turn = 3 - turn; else if (who == 1) PackPuzzle.good();
        think = 0;
        return true;
    }

    private List<int[]> free() {
        List<int[]> l = new ArrayList<>();
        for (int r = 0; r <= N; r++) for (int c = 0; c < N; c++) if (!h[r][c]) l.add(new int[]{0, r, c});
        for (int r = 0; r < N; r++) for (int c = 0; c <= N; c++) if (!v[r][c]) l.add(new int[]{1, r, c});
        return l;
    }

    private int maxSidesAfter(int[] l) {
        int m = 0;
        if (l[0] == 0) { if (l[1] > 0) m = Math.max(m, sides(l[1] - 1, l[2]) + 1); if (l[1] < N) m = Math.max(m, sides(l[1], l[2]) + 1); }
        else { if (l[2] > 0) m = Math.max(m, sides(l[1], l[2] - 1) + 1); if (l[2] < N) m = Math.max(m, sides(l[1], l[2]) + 1); }
        return m;
    }

    private int[] ai() {
        List<int[]> f = free();
        int lv = aiLevel();
        if (lv == 0 && rnd.nextFloat() < 0.5f) return f.get(rnd.nextInt(f.size()));
        for (int[] l : f) if (maxSidesAfter(l) == 4) return l;
        List<int[]> safe = new ArrayList<>();
        for (int[] l : f) if (maxSidesAfter(l) <= 2) safe.add(l);
        if (!safe.isEmpty() && (lv >= 1 || rnd.nextBoolean())) return safe.get(rnd.nextInt(safe.size()));
        return f.get(rnd.nextInt(f.size()));
    }

    @Override protected void update(float dt) { if (turn == 2 && (think += dt) > 0.45f) draw(ai(), 2); }

    private int[] lineAt(float x, float y) {
        int[] best = null; float bd = 9;
        for (int r = 0; r <= N; r++) for (int c = 0; c < N; c++) { float d = (float) Math.hypot(x - (OX + c * S0 + S0 / 2), y - (OY + r * S0)); if (d < bd && !h[r][c]) { bd = d; best = new int[]{0, r, c}; } }
        for (int r = 0; r < N; r++) for (int c = 0; c <= N; c++) { float d = (float) Math.hypot(x - (OX + c * S0), y - (OY + r * S0 + S0 / 2)); if (d < bd && !v[r][c]) { bd = d; best = new int[]{1, r, c}; } }
        return best;
    }

    @Override protected void onClick(float x, float y, int b) { if (turn == 1) { int[] l = lineAt(x, y); if (l != null) draw(l, 1); } }

    @Override protected void render() {
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) if (owner[r][c] != 0) box(OX + c * S0 + 3, OY + r * S0 + 3, S0 - 6, S0 - 6, 6, owner[r][c] == 1 ? 0x663FA9FF : 0x66E3000B);
        int idle = Style.light ? 0x22000000 : 0x22FFFFFF;
        for (int r = 0; r <= N; r++) for (int c = 0; c < N; c++) line(OX + c * S0, OY + r * S0, OX + (c + 1) * S0, OY + r * S0, h[r][c] ? 3 : 1.2f, h[r][c] ? Style.text : idle);
        for (int r = 0; r < N; r++) for (int c = 0; c <= N; c++) line(OX + c * S0, OY + r * S0, OX + c * S0, OY + (r + 1) * S0, v[r][c] ? 3 : 1.2f, v[r][c] ? Style.text : idle);
        int[] hl = turn == 1 ? lineAt(bmx(), bmy()) : null;
        if (hl != null) { if (hl[0] == 0) line(OX + hl[2] * S0, OY + hl[1] * S0, OX + (hl[2] + 1) * S0, OY + hl[1] * S0, 3, 0x993FA9FF); else line(OX + hl[2] * S0, OY + hl[1] * S0, OX + hl[2] * S0, OY + (hl[1] + 1) * S0, 3, 0x993FA9FF); }
        for (int r = 0; r <= N; r++) for (int c = 0; c <= N; c++) circle(OX + c * S0, OY + r * S0, 3.5f, Style.text);
        text("Du: " + sp, 330, 60, 11, Fonts.BOLD, 0xFF3FA9FF);
        text("KI: " + sa, 330, 90, 11, Fonts.BOLD, 0xFFE3000B);
        text(turn == 1 ? "Du bist dran" : "KI zieht...", 330, 130, 7.5f, Fonts.BOLD, Style.sub);
        text("Kästchen zu = nochmal", 330, 200, 6.2f, Fonts.REGULAR, Style.sub);
    }
}

// ====================================================================== Schiffe versenken
final class Battleship extends GameView {
    private static final int N = 8;
    private static final int[] SHIPS = {4, 3, 3, 2, 2};
    private static final float C = 22, PX = 12, EX = 214, OY = 40;
    private int[][] mine, enemy;          // 0 Wasser, 1 Schiff
    private int[][] myShots, aiShots;     // 0 nichts, 1 Wasser, 2 Treffer
    private int turn;
    private float think;
    private final List<int[]> targets = new ArrayList<>();
    private String msg = "";

    Battleship() { super("battleship"); }
    @Override protected boolean hasAi() { return true; }
    @Override protected float boardW() { return 400; }
    @Override protected float boardH() { return 260; }
    @Override protected String scoreLabel() { return "Treffer"; }
    @Override protected String overTitle() { return left(enemy, myShots) == 0 ? "Flotte versenkt - gewonnen!" : "Deine Flotte ist gesunken"; }

    private int[][] place() {
        int[][] g = new int[N][N];
        for (int s : SHIPS) while (true) {
            boolean hor = rnd.nextBoolean();
            int r = rnd.nextInt(hor ? N : N - s + 1), c = rnd.nextInt(hor ? N - s + 1 : N);
            boolean ok = true;
            for (int i = 0; i < s && ok; i++) { int rr = r + (hor ? 0 : i), cc = c + (hor ? i : 0); for (int dr = -1; dr <= 1; dr++) for (int dc = -1; dc <= 1; dc++) { int a = rr + dr, b2 = cc + dc; if (a >= 0 && b2 >= 0 && a < N && b2 < N && g[a][b2] == 1) ok = false; } }
            if (!ok) continue;
            for (int i = 0; i < s; i++) g[r + (hor ? 0 : i)][c + (hor ? i : 0)] = 1;
            break;
        }
        return g;
    }

    private static int left(int[][] ships, int[][] shots) { int n = 0; for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) if (ships[r][c] == 1 && shots[r][c] != 2) n++; return n; }

    @Override protected void reset() { mine = place(); enemy = place(); myShots = new int[N][N]; aiShots = new int[N][N]; turn = 1; think = 0; targets.clear(); msg = ""; score = 0; }

    @Override protected void onClick(float x, float y, int b) {
        if (turn != 1) return;
        int c = (int) ((x - EX) / C), r = (int) ((y - OY) / C);
        if (x < EX || y < OY || c >= N || r >= N || myShots[r][c] != 0) return;
        boolean hit = enemy[r][c] == 1;
        myShots[r][c] = hit ? 2 : 1;
        if (hit) { score++; Sound.play("minecraft:entity.generic.explode", 1.6f, 0.25f); msg = "Treffer!"; if (left(enemy, myShots) == 0) { score += left(mine, aiShots) * 5; PackPuzzle.win(); gameOver(); } }
        else { Sound.play("minecraft:entity.generic.splash", 1.2f, 0.3f); msg = "Wasser"; turn = 2; think = 0; }
    }

    private int[] aiPick() {
        int lv = aiLevel();
        while (!targets.isEmpty() && lv > 0) { int[] t = targets.remove(targets.size() - 1); if (t[0] >= 0 && t[1] >= 0 && t[0] < N && t[1] < N && aiShots[t[0]][t[1]] == 0) return t; }
        List<int[]> f = new ArrayList<>();
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) if (aiShots[r][c] == 0 && (lv < 3 || (r + c) % 2 == 0)) f.add(new int[]{r, c});
        if (f.isEmpty()) for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) if (aiShots[r][c] == 0) f.add(new int[]{r, c});
        return f.get(rnd.nextInt(f.size()));
    }

    @Override protected void update(float dt) {
        if (turn != 2 || (think += dt) < 0.7f) return;
        think = 0;
        int[] p = aiPick();
        boolean hit = mine[p[0]][p[1]] == 1;
        aiShots[p[0]][p[1]] = hit ? 2 : 1;
        if (hit) {
            Sound.play("minecraft:entity.generic.explode", 1.2f, 0.25f);
            targets.add(new int[]{p[0] + 1, p[1]}); targets.add(new int[]{p[0] - 1, p[1]}); targets.add(new int[]{p[0], p[1] + 1}); targets.add(new int[]{p[0], p[1] - 1});
            if (left(mine, aiShots) == 0) { PackPuzzle.bad(); gameOver(); }
        } else { turn = 1; Sound.play("minecraft:entity.generic.splash", 1.0f, 0.25f); }
    }

    private void grid(float ox, int[][] ships, int[][] shots, boolean showShips) {
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) {
            float x = ox + c * C, y = OY + r * C;
            box(x + 1, y + 1, C - 2, C - 2, 3, 0xFF1A4A7A);
            if (showShips && ships[r][c] == 1) box(x + 3, y + 3, C - 6, C - 6, 3, 0xFF9AA3AD);
            if (shots[r][c] == 1) circle(x + C / 2, y + C / 2, 3, 0xCCFFFFFF);
            if (shots[r][c] == 2) { circle(x + C / 2, y + C / 2, 7, 0xFFE3000B); circle(x + C / 2, y + C / 2, 3, 0xFFFFB030); }
        }
    }

    @Override protected void render() {
        text("Deine Flotte", PX + N * C / 2, 20, 8, Fonts.BOLD, Style.sub);
        text("Gegner - hier schießen", EX + N * C / 2, 20, 8, Fonts.BOLD, turn == 1 ? Style.ok : Style.sub);
        grid(PX, mine, aiShots, true);
        grid(EX, enemy, myShots, state == State.OVER);
        text(turn == 1 ? "Du bist dran" + (msg.isEmpty() ? "" : " · " + msg) : "Gegner zielt...", 200, 238, 8, Fonts.BOLD, Style.text);
    }
}

// ====================================================================== Türme von Hanoi
final class Hanoi extends GameView {
    private final List<List<Integer>> pegs = new ArrayList<>();
    private int n, moves, held = -1;
    private float doneT = -1;

    Hanoi() { super("hanoi"); }
    @Override protected float boardW() { return 420; }
    @Override protected float boardH() { return 260; }
    @Override protected String scoreLabel() { return "Scheiben"; }
    @Override protected String overTitle() { return "Alle Türme geschafft!"; }

    @Override protected void reset() { n = 3; setup(); score = 0; }

    private void setup() {
        pegs.clear();
        for (int i = 0; i < 3; i++) pegs.add(new ArrayList<>());
        for (int i = n; i >= 1; i--) pegs.get(0).add(i);
        moves = 0; held = -1; doneT = -1;
    }

    private float px(int p) { return 75 + p * 135; }

    @Override protected void onClick(float x, float y, int b) {
        if (doneT >= 0) return;
        int p = x < 142 ? 0 : x < 277 ? 1 : 2;
        List<Integer> peg = pegs.get(p);
        if (held < 0) { if (!peg.isEmpty()) { held = p; PackPuzzle.click(); } return; }
        if (p == held) { held = -1; return; }
        List<Integer> from = pegs.get(held);
        int d = from.get(from.size() - 1);
        if (!peg.isEmpty() && peg.get(peg.size() - 1) < d) { PackPuzzle.bad(); held = -1; return; }
        from.remove(from.size() - 1); peg.add(d); moves++; held = -1; PackPuzzle.click();
        if (pegs.get(2).size() == n) { doneT = 0; score = n; PackPuzzle.win(); }
    }

    @Override protected void update(float dt) {
        if (doneT >= 0 && (doneT += dt) > 1.2f) { if (n >= 7) gameOver(); else { n++; setup(); } }
    }

    @Override protected void render() {
        box(10, 220, 400, 10, 4, 0xFF8A5A30);
        for (int p = 0; p < 3; p++) {
            box(px(p) - 3, 70, 6, 150, 3, 0xFFB08A5A);
            List<Integer> peg = pegs.get(p);
            for (int i = 0; i < peg.size(); i++) {
                int d = peg.get(i);
                float w = 24 + d * 14, y = 205 - i * 16;
                if (p == held && i == peg.size() - 1) y = 40;
                brick(px(p) - w / 2, y, w, 14, PackPuzzle.PAL[(d - 1) % 6]);
            }
        }
        text(n + " Scheiben · Züge: " + moves + " (min. " + ((1 << n) - 1) + ")", 210, 16, 8, Fonts.BOLD, Style.text);
        if (doneT >= 0) text("Geschafft!", 210, 110, 16, Fonts.BOLD, Style.ok);
    }
}

// ====================================================================== Solitär-Brett
final class PegSolitaire extends GameView {
    private static final float C = 32, OX = 50, OY = 18;
    private final int[] g = new int[49];   // -1 außerhalb, 0 Loch, 1 Stift
    private int sel = -1, removed;

    PegSolitaire() { super("pegsolitaire"); }
    @Override protected float boardW() { return 400; }
    @Override protected float boardH() { return 262; }
    @Override protected String scoreLabel() { return "Entfernt"; }
    @Override protected String overTitle() { return removed == 31 ? "Perfekt - nur noch einer!" : "Keine Züge mehr"; }

    @Override protected void reset() {
        for (int i = 0; i < 49; i++) { int r = i / 7, c = i % 7; g[i] = (r < 2 || r > 4) && (c < 2 || c > 4) ? -1 : 1; }
        g[24] = 0; sel = -1; removed = 0;
    }

    private boolean canMove() {
        for (int i = 0; i < 49; i++) if (g[i] == 1) for (int[] d : new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}}) {
            int r = i / 7 + d[0] * 2, c = i % 7 + d[1] * 2;
            if (r >= 0 && c >= 0 && r < 7 && c < 7 && g[r * 7 + c] == 0 && g[(i / 7 + d[0]) * 7 + i % 7 + d[1]] == 1) return true;
        }
        return false;
    }

    @Override protected void onClick(float x, float y, int b) {
        int c = (int) ((x - OX) / C), r = (int) ((y - OY) / C);
        if (x < OX || y < OY || c > 6 || r > 6) return;
        int i = r * 7 + c;
        if (g[i] == 1) { sel = i; PackPuzzle.click(); return; }
        if (g[i] != 0 || sel < 0) return;
        int sr = sel / 7, sc = sel % 7;
        if (!((Math.abs(sr - r) == 2 && sc == c) || (Math.abs(sc - c) == 2 && sr == r))) return;
        int mid = ((sr + r) / 2) * 7 + (sc + c) / 2;
        if (g[mid] != 1) return;
        g[sel] = 0; g[mid] = 0; g[i] = 1; sel = -1; removed++; score = removed; PackPuzzle.good();
        if (!canMove()) { if (removed == 31) PackPuzzle.win(); gameOver(); }
    }

    @Override protected void update(float dt) { }

    @Override protected void render() {
        for (int i = 0; i < 49; i++) {
            if (g[i] < 0) continue;
            float x = OX + (i % 7) * C + C / 2, y = OY + (i / 7) * C + C / 2;
            circle(x, y, 11, Style.light ? 0x22000000 : 0x33000000);
            circle(x, y, 7, Style.light ? 0xFFB0B4BC : 0xFF101218);
            if (g[i] == 1) { circle(x, y - 1, 10, i == sel ? 0xFFFFCD03 : 0xFFE3000B); circle(x - 3, y - 4, 3, 0x55FFFFFF); }
        }
        text("Übrig: " + (32 - removed), 340, 40, 9, Fonts.BOLD, Style.text);
        text("Springe über einen", 340, 80, 6.2f, Fonts.REGULAR, Style.sub);
        text("Stift ins Loch dahinter", 340, 92, 6.2f, Fonts.REGULAR, Style.sub);
    }
}

// ====================================================================== Farbflut
final class FloodIt extends GameView {
    private static final int N = 12, MAX = 22;
    private static final float C = 20, OX = 12, OY = 12;
    private int[][] g = new int[N][N];
    private int moves;

    FloodIt() { super("floodit"); }
    @Override protected float boardW() { return 400; }
    @Override protected float boardH() { return 264; }
    @Override protected String overTitle() { return done() ? "Alles eine Farbe!" : "Keine Züge mehr"; }

    @Override protected void reset() { for (int[] r : g) for (int c = 0; c < N; c++) r[c] = rnd.nextInt(6); moves = 0; }

    private boolean done() { int v = g[0][0]; for (int[] r : g) for (int x : r) if (x != v) return false; return true; }

    private void pick(int col) {
        int old = g[0][0];
        if (col == old) return;
        java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<>();
        boolean[][] seen = new boolean[N][N];
        q.add(new int[]{0, 0}); seen[0][0] = true;
        while (!q.isEmpty()) {
            int[] p = q.poll();
            g[p[0]][p[1]] = col;
            for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                int r = p[0] + d[0], c = p[1] + d[1];
                if (r >= 0 && c >= 0 && r < N && c < N && !seen[r][c] && g[r][c] == old) { seen[r][c] = true; q.add(new int[]{r, c}); }
            }
        }
        moves++; PackPuzzle.click();
        if (done()) { score = (MAX - moves + 1) * 10; PackPuzzle.win(); gameOver(); }
        else if (moves >= MAX) { score = 0; PackPuzzle.bad(); gameOver(); }
    }

    @Override protected void onClick(float x, float y, int b) {
        for (int i = 0; i < 6; i++) if (Math.hypot(x - (300 + (i % 2) * 44), y - (60 + (i / 2) * 44)) < 18) pick(i);
        int c = (int) ((x - OX) / C), r = (int) ((y - OY) / C);
        if (x >= OX && y >= OY && c < N && r < N) pick(g[r][c]);
    }

    @Override protected void onKey(int k) { if (k >= 49 && k <= 54) pick(k - 49); }
    @Override protected void update(float dt) { }

    @Override protected void render() {
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) box(OX + c * C, OY + r * C, C + 0.5f, C + 0.5f, 0, PackPuzzle.PAL[g[r][c]]);
        for (int i = 0; i < 6; i++) { circle(300 + (i % 2) * 44, 60 + (i / 2) * 44, 17, PackPuzzle.PAL[i]); text(String.valueOf(i + 1), 300 + (i % 2) * 44, 61 + (i / 2) * 44, 8, Fonts.BOLD, 0xCCFFFFFF); }
        text("Züge: " + moves + " / " + MAX, 322, 210, 9, Fonts.BOLD, moves > MAX - 4 ? Style.danger : Style.text);
        text("Färbe alles in eine Farbe", 322, 236, 6.2f, Fonts.REGULAR, Style.sub);
    }
}
