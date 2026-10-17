package dev.lego.games;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** Spiele-Paket 4c: Wissen, Wörter, Karten. */
final class PackMind {
    private PackMind() { }
    static final String[] SUITS = {"♣", "♠", "♥", "♦"};
    static final String[] SUIT_NAMES = {"Kreuz", "Pik", "Herz", "Karo"};
    static final String[] RANKS = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "B", "D", "K", "A"};

    /** Playing card at (x, y) 34 x 48 board units; rank index into RANKS. */
    static void card(GameView v, float x, float y, int suit, int rank, boolean faceUp, boolean hi) {
        if (!faceUp) { v.box(x, y, 34, 48, 5, 0xFF1A4AA0); v.box(x + 3, y + 3, 28, 42, 4, 0xFF2A6AD0); return; }
        v.box(x, y, 34, 48, 5, hi ? 0xFFFFF6C0 : 0xFFF8F8F8);
        int col = suit >= 2 ? 0xFFD01020 : 0xFF15161A;
        v.text(RANKS[rank], x + 9, y + 9, 8, Fonts.BOLD, col);
        v.text(SUITS[suit], x + 17, y + 28, 14, Fonts.BOLD, col);
    }
}

// ====================================================================== Klaviertasten
final class PianoTiles extends GameView {
    private static final float W = 240, H = 300, TH = 70;
    private final List<float[]> tiles = new ArrayList<>();   // col, y, hit
    private float speed, wrongT = -1;
    private int wrongCol = -1;

    PianoTiles() { super("pianotiles"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }

    @Override protected void reset() { tiles.clear(); speed = 150; wrongT = -1; for (int i = 0; i < 6; i++) tiles.add(new float[]{rnd.nextInt(4), -i * TH, 0}); }

    private float[] lowest() { float[] l = null; for (float[] t : tiles) if (t[2] == 0 && (l == null || t[1] > l[1])) l = t; return l; }

    private void press(int col) {
        if (wrongT >= 0) return;
        float[] l = lowest();
        if (l == null) return;
        if ((int) l[0] == col) { l[2] = 1; score++; Sound.play("minecraft:block.note_block.harp", 0.7f + (score % 8) * 0.12f, 0.5f); speed += 2.5f; }
        else { wrongCol = col; wrongT = 0; PackPuzzle.bad(); }
    }

    @Override protected void onClick(float x, float y, int b) { press((int) Math.max(0, Math.min(3, x / (W / 4)))); }
    @Override protected void onKey(int k) { if (k == K_D) press(0); else if (k == 70) press(1); else if (k == 74) press(2); else if (k == 75) press(3); }

    @Override protected void update(float dt) {
        if (wrongT >= 0) { if ((wrongT += dt) > 0.8f) gameOver(); return; }
        for (float[] t : tiles) t[1] += speed * dt;
        float top = 0; for (float[] t : tiles) top = Math.min(top, t[1]);
        while (top > -TH) { top -= TH; tiles.add(new float[]{rnd.nextInt(4), top, 0}); }
        for (float[] t : tiles) if (t[2] == 0 && t[1] > H) { wrongCol = (int) t[0]; wrongT = 0; PackPuzzle.bad(); return; }
        tiles.removeIf(t -> t[1] > H + TH);
    }

    @Override protected void render() {
        box(0, 0, W, H, 0, 0xFFF4F4F4);
        for (int c = 1; c < 4; c++) box(c * W / 4, 0, 0.8f, H, 0, 0xFFD0D0D8);
        for (float[] t : tiles) box(t[0] * W / 4 + 1, t[1] + 1, W / 4 - 2, TH - 2, 3, t[2] == 1 ? 0xFFB0B4BC : 0xFF15161A);
        if (wrongT >= 0 && wrongCol >= 0) box(wrongCol * W / 4, 0, W / 4, H, 0, Gx.withAlpha(0xFFFF3B4C, 0.5f));
        text(String.valueOf(score), W / 2, 22, 16, Fonts.BOLD, 0xFFE3000B);
        text("D  F  J  K", W / 2, H - 10, 6.5f, Fonts.MEDIUM, 0xFF9AA3B2);
    }
}

// ====================================================================== Farb-Reaktion (Stroop)
final class ColorWords extends GameView {
    private static final String[] NAMES = {"ROT", "BLAU", "GELB", "GRÜN"};
    private static final int[] COLS = {0xFFE3000B, 0xFF2E86DE, 0xFFFFCD03, 0xFF27AE60};
    private int word, ink;
    private float left, flash;
    private boolean lastOk;

    ColorWords() { super("colorwords"); }
    @Override protected float boardW() { return 380; }
    @Override protected float boardH() { return 260; }
    @Override protected String overTitle() { return "Zeit um!"; }

    @Override protected void reset() { left = 30; next(); }
    private void next() { word = rnd.nextInt(4); ink = rnd.nextInt(4); if (rnd.nextFloat() < 0.75f) while (ink == word) ink = rnd.nextInt(4); }

    private void answer(int i) {
        lastOk = i == ink; flash = 0.3f;
        if (lastOk) { score++; PackPuzzle.good(); } else { score = Math.max(0, score - 1); PackPuzzle.bad(); }
        next();
    }

    @Override protected void onClick(float x, float y, int b) { for (int i = 0; i < 4; i++) if (in(x, y, 20 + i * 88, 190, 80, 40)) answer(i); }
    @Override protected void onKey(int k) { if (k >= 49 && k <= 52) answer(k - 49); }
    @Override protected void update(float dt) { flash = Math.max(0, flash - dt); left -= dt; if (left <= 0) { left = 0; gameOver(); } }

    @Override protected void render() {
        text("Welche FARBE hat das Wort?", 190, 30, 8, Fonts.BOLD, Style.sub);
        text(NAMES[word], 190, 100, 34, Fonts.BOLD, COLS[ink]);
        if (flash > 0) text(lastOk ? "✔" : "✖", 330, 100, 20, Fonts.BOLD, Gx.withAlpha(lastOk ? 0xFF39D353 : 0xFFFF3B4C, flash / 0.3f));
        for (int i = 0; i < 4; i++) button((i + 1) + "  " + NAMES[i], 20 + i * 88, 190, 80, 40, Gx.mix(COLS[i], 0xFF000000, 0.2f));
        text(String.format(Locale.ROOT, "%.0f s", left), 350, 20, 9, Fonts.BOLD, left < 6 ? Style.danger : Style.text);
    }
}

// ====================================================================== Tipp-Trainer
final class TypingTest extends GameView {
    private static final String[] WORDS = ("haus baum katze hund block stein lego spiel welt licht wasser feuer erde luft sonne mond stern wolke regen schnee berg fluss see meer insel "
            + "brot milch apfel birne kuchen tisch stuhl lampe fenster tuer garten blume gras wald weg strasse auto zug schiff flugzeug rakete ritter burg drache schatz karte "
            + "schwert schild bogen pfeil kiste ofen fackel diamant gold eisen kohle redstone portal zombie creeper spinne schaf kuh schwein huhn pferd").split(" ");
    private final List<String> queue = new ArrayList<>();
    private String typed = "";
    private float left;
    private int typedChars, errors;
    private boolean bad;

    TypingTest() { super("typing"); }
    @Override protected float boardW() { return 420; }
    @Override protected float boardH() { return 240; }
    @Override protected String scoreLabel() { return "Wörter/min"; }
    @Override protected String overTitle() { return "Fertig! " + score + " Wörter pro Minute"; }

    @Override protected void reset() { queue.clear(); for (int i = 0; i < 12; i++) queue.add(WORDS[rnd.nextInt(WORDS.length)]); typed = ""; left = 60; typedChars = 0; errors = 0; bad = false; }

    @Override public boolean chr(String s) {
        if (state != State.PLAYING) return true;
        for (char c : s.toLowerCase(Locale.ROOT).toCharArray()) {
            if (c == ' ') { submit(); continue; }
            typed += c;
            bad = !queue.get(0).startsWith(typed);
            if (queue.get(0).equals(typed)) submit();
        }
        return true;
    }

    private void submit() {
        if (typed.isEmpty()) return;
        if (typed.equals(queue.get(0))) { score++; typedChars += typed.length(); Sound.play("minecraft:block.note_block.hat", 1.6f, 0.2f); }
        else { errors++; PackPuzzle.bad(); }
        queue.remove(0); queue.add(WORDS[rnd.nextInt(WORDS.length)]); typed = ""; bad = false;
    }

    @Override protected void onKey(int k) { if (k == 259 && !typed.isEmpty()) { typed = typed.substring(0, typed.length() - 1); bad = !queue.get(0).startsWith(typed); } }
    @Override protected void update(float dt) { left -= dt; if (left <= 0) { left = 0; gameOver(); } }

    @Override protected void render() {
        float x = 20;
        for (int i = 0; i < 6 && i < queue.size(); i++) {
            String w = queue.get(i);
            int col = i == 0 ? (bad ? Style.danger : Style.accent) : Style.sub;
            textL(w, x, 70, i == 0 ? 14 : 10, Fonts.BOLD, col);
            x += Gx.width(w, (i == 0 ? 14 : 10), Fonts.BOLD) + 14;
            if (x > 400) break;
        }
        box(20, 110, 380, 40, 10, Style.light ? 0xFFE6E8EE : 0xFF1C1F27);
        textL(typed + ((System.currentTimeMillis() / 500) % 2 == 0 ? "|" : ""), 32, 130, 13, Fonts.BOLD, bad ? Style.danger : Style.text);
        text(String.format(Locale.ROOT, "%.0f s", left), 210, 25, 12, Fonts.BOLD, left < 10 ? Style.danger : Style.text);
        text("Richtig: " + score + "   Fehler: " + errors, 210, 180, 8, Fonts.BOLD, Style.sub);
        text("Tippe die Wörter, Leertaste = weiter", 210, 210, 6.6f, Fonts.MEDIUM, Style.muted);
    }
}

// ====================================================================== Kopfrechnen
final class MathQuiz extends GameView {
    private int a, b, op, answer;
    private String input = "";
    private float left, flash;
    private boolean lastOk;
    private int streak;

    MathQuiz() { super("mathquiz"); }
    @Override protected float boardW() { return 380; }
    @Override protected float boardH() { return 250; }
    @Override protected String overTitle() { return "Zeit um!"; }

    @Override protected void reset() { left = 60; streak = 0; next(); }

    private void next() {
        int lvl = (int) Math.min(4, score / 5);
        op = rnd.nextInt(lvl >= 2 ? 4 : 2);
        int max = 10 + lvl * 15;
        switch (op) {
            case 0 -> { a = rnd.nextInt(max) + 1; b = rnd.nextInt(max) + 1; answer = a + b; }
            case 1 -> { a = rnd.nextInt(max) + 1; b = rnd.nextInt(a) + 1; answer = a - b; }
            case 2 -> { a = rnd.nextInt(8 + lvl * 2) + 2; b = rnd.nextInt(9) + 2; answer = a * b; }
            default -> { b = rnd.nextInt(9) + 2; answer = rnd.nextInt(10) + 1; a = b * answer; }
        }
        input = "";
    }

    @Override public boolean chr(String s) { if (state == State.PLAYING) for (char c : s.toCharArray()) if (Character.isDigit(c) && input.length() < 5) input += c; return true; }

    @Override protected void onKey(int k) {
        if (k == 259 && !input.isEmpty()) input = input.substring(0, input.length() - 1);
        if (k == K_ENTER && !input.isEmpty()) {
            lastOk = Integer.parseInt(input) == answer; flash = 0.4f;
            if (lastOk) { streak++; score++; left += 1; PackPuzzle.good(); } else { streak = 0; left -= 3; PackPuzzle.bad(); }
            next();
        }
    }

    @Override protected void update(float dt) { flash = Math.max(0, flash - dt); left -= dt; if (left <= 0) { left = 0; gameOver(); } }

    @Override protected void render() {
        String[] ops = {"+", "−", "×", "÷"};
        text(a + " " + ops[op] + " " + b + " =", 190, 80, 26, Fonts.BOLD, Style.text);
        box(130, 120, 120, 44, 10, flash > 0 ? Gx.withAlpha(lastOk ? 0xFF39D353 : 0xFFFF3B4C, 0.4f) : (Style.light ? 0xFFE6E8EE : 0xFF1C1F27));
        text(input.isEmpty() ? "?" : input, 190, 143, 20, Fonts.BOLD, Style.accent);
        text(String.format(Locale.ROOT, "%.0f s", left), 340, 20, 10, Fonts.BOLD, left < 10 ? Style.danger : Style.text);
        text("Serie: " + streak + "   (richtig +1 s, falsch −3 s)", 190, 200, 7, Fonts.MEDIUM, Style.sub);
        text("Zahl tippen, Enter", 190, 222, 6.6f, Fonts.REGULAR, Style.muted);
    }
}

// ====================================================================== Minecraft-Quiz
final class McQuiz extends GameView {
    private static final String[][] Q = {
            {"Wie viele Obsidianblöcke braucht ein Netherportal mindestens?", "10", "14", "8", "12"},
            {"Welches Werkzeug braucht man, um Diamanterz abzubauen?", "Eisen-Spitzhacke", "Stein-Spitzhacke", "Holz-Spitzhacke", "Gold-Spitzhacke"},
            {"Wovor haben Creeper Angst?", "Katzen", "Wölfen", "Wasser", "Fackeln"},
            {"Was droppt ein Enderman?", "Enderperle", "Enderauge", "Lohenrute", "Knochen"},
            {"Woraus craftet man ein Enderauge?", "Enderperle + Lohenstaub", "Enderperle + Zucker", "Diamant + Perle", "Redstone + Perle"},
            {"Auf welcher Höhe findet man Diamanten am häufigsten (1.18+)?", "Y = -58", "Y = 12", "Y = 64", "Y = 100"},
            {"Wie viele Herzen hat ein Spieler normal?", "10", "20", "8", "12"},
            {"Welcher Mob lässt Glut-/Lohenruten fallen?", "Lohe", "Ghast", "Magmawürfel", "Piglin"},
            {"Womit zähmt man einen Wolf?", "Knochen", "Fleisch", "Fisch", "Weizen"},
            {"Womit zähmt man eine Katze?", "Roher Fisch", "Milch", "Weizen", "Knochen"},
            {"Was braucht man für einen Zaubertisch?", "Buch, Diamanten, Obsidian", "Buch, Gold, Stein", "Eisen, Diamant, Holz", "Obsidian, Redstone, Buch"},
            {"Was passiert, wenn man im Nether ein Bett benutzt?", "Es explodiert", "Man schläft", "Nichts", "Es brennt"},
            {"Welches Erz gibt es NUR im Nether?", "Netherquarz", "Smaragd", "Kupfer", "Lapislazuli"},
            {"Wie heißt der Endboss in Minecraft?", "Enderdrache", "Wither", "Wächter", "Warden"},
            {"Welcher Mob ist blind und reagiert auf Geräusche?", "Warden", "Wither", "Phantom", "Vex"},
            {"Was braucht man, um den Wither zu beschwören?", "Seelensand + 3 Wither-Skelett-Schädel", "Obsidian + Schädel", "TNT + Schädel", "Netherrack + Kürbis"},
            {"Was macht ein Totem der Unsterblichkeit?", "Rettet dich vor dem Tod", "Macht unsichtbar", "Heilt Hunger", "Gibt Flügel"},
            {"Welches Biom hat Pilzkühe?", "Pilzinsel", "Dschungel", "Savanne", "Taiga"},
            {"Wie viele Blöcke Eisen braucht ein Eisengolem?", "4", "5", "3", "6"},
            {"Was schützt vor Phantomen?", "Regelmäßig schlafen", "Fackeln", "Schilde", "Wasser"},
            {"Was braucht man, um eine Elytra zu reparieren?", "Phantommembran", "Leder", "Federn", "Eisen"},
            {"Welcher Block leuchtet am hellsten (Stufe 15)?", "Leuchtstein", "Fackel", "Redstone-Fackel", "Magmablock"},
            {"Wie lange dauert ein Minecraft-Tag in Echtzeit?", "20 Minuten", "10 Minuten", "1 Stunde", "24 Minuten"},
            {"Was baut man aus Stöcken und Kohle?", "Fackeln", "Leitern", "Zäune", "Pfeile"},
            {"Welcher Lego-Stein ist der bekannteste?", "2x4-Stein", "1x1-Stein", "3x3-Platte", "6x6-Stein"},
            {"Aus welchem Land kommt Lego?", "Dänemark", "Deutschland", "Schweden", "USA"},
            {"Was bedeutet \"Lego\" auf Dänisch?", "Spiel gut", "Baue mehr", "Kleiner Stein", "Bunt"},
            {"Welche Farbe hat ein Creeper?", "Grün", "Blau", "Grau", "Rot"},
            {"Was droppt ein Skelett?", "Knochen und Pfeile", "Fäden", "Schießpulver", "Federn"},
            {"Was droppt eine Spinne?", "Fäden", "Knochen", "Schleim", "Leder"},
    };
    private final List<Integer> order = new ArrayList<>();
    private int idx, picked = -1, right;
    private List<String> answers;
    private float wait;

    McQuiz() { super("mcquiz"); }
    @Override protected float boardW() { return 420; }
    @Override protected float boardH() { return 270; }
    @Override protected String scoreLabel() { return "Richtig"; }
    @Override protected String overTitle() { return score + " von 10 richtig"; }

    @Override protected void reset() { order.clear(); for (int i = 0; i < Q.length; i++) order.add(i); Collections.shuffle(order, rnd); idx = 0; load(); }

    private void load() {
        String[] q = Q[order.get(idx)];
        answers = new ArrayList<>(List.of(q[1], q[2], q[3], q[4]));
        Collections.shuffle(answers, rnd);
        right = answers.indexOf(q[1]); picked = -1; wait = 0;
    }

    @Override protected void onClick(float x, float y, int b) {
        if (picked >= 0) return;
        for (int i = 0; i < 4; i++) if (in(x, y, 20 + (i % 2) * 195, 120 + (i / 2) * 62, 185, 52)) pick(i);
    }

    @Override protected void onKey(int k) { if (picked < 0 && k >= 49 && k <= 52) pick(k - 49); }

    private void pick(int i) { picked = i; if (i == right) { score++; PackPuzzle.good(); } else PackPuzzle.bad(); }

    @Override protected void update(float dt) {
        if (picked < 0) return;
        if ((wait += dt) > 1.3f) { idx++; if (idx >= 10) gameOver(); else load(); }
    }

    @Override protected void render() {
        text("Frage " + (idx + 1) + " / 10", 210, 18, 7, Fonts.BOLD, Style.sub);
        List<String> lines = Fonts.wrap(Q[order.get(idx)][0], Fonts.BOLD, 11 * u, S(380), 3);
        float y = 50;
        for (String l : lines) { text(l, 210, y, 11, Fonts.BOLD, Style.text); y += 16; }
        for (int i = 0; i < 4; i++) {
            float bx0 = 20 + (i % 2) * 195, by0 = 120 + (i / 2) * 62;
            int col = picked < 0 ? (in(bmx(), bmy(), bx0, by0, 185, 52) ? Style.surfaceHover : Style.surface) : i == right ? 0xFF2E9E4A : i == picked ? 0xFFB0202A : Style.surface;
            box(bx0, by0, 185, 52, 10, col);
            text((i + 1) + ")  " + answers.get(i), bx0 + 92, by0 + 26, 8, Fonts.BOLD, picked >= 0 && (i == right || i == picked) ? 0xFFFFFFFF : Style.text);
        }
    }
}

// ====================================================================== Buchstabensalat
final class Anagram extends GameView {
    private String word, shuffled, input = "";
    private float left, flash;
    private boolean lastOk;
    private int skips;

    Anagram() { super("anagram"); }
    @Override protected float boardW() { return 400; }
    @Override protected float boardH() { return 250; }
    @Override protected String overTitle() { return "Zeit um! Letztes Wort: " + word; }

    @Override protected void reset() { left = 90; skips = 3; next(); }

    private void next() {
        do word = Words.ANY[rnd.nextInt(Words.ANY.length)]; while (word.length() > 9);
        List<Character> cs = new ArrayList<>();
        for (char c : word.toCharArray()) cs.add(c);
        do { Collections.shuffle(cs, rnd); StringBuilder sb = new StringBuilder(); for (char c : cs) sb.append(c); shuffled = sb.toString(); } while (shuffled.equals(word) && word.length() > 1);
        input = "";
    }

    @Override public boolean chr(String s) {
        if (state != State.PLAYING) return true;
        for (char c : Words.norm(s).toCharArray()) if (c >= 'A' && c <= 'Z' && input.length() < word.length()) input += c;
        if (input.equals(word)) { score += word.length(); lastOk = true; flash = 0.5f; PackPuzzle.win(); next(); }
        else if (input.length() == word.length()) { lastOk = false; flash = 0.4f; PackPuzzle.bad(); input = ""; }
        return true;
    }

    @Override protected void onKey(int k) { if (k == 259 && !input.isEmpty()) input = input.substring(0, input.length() - 1); }

    @Override protected void onClick(float x, float y, int b) { if (in(x, y, 150, 200, 100, 26) && skips > 0) { skips--; next(); } }

    @Override protected void update(float dt) { flash = Math.max(0, flash - dt); left -= dt; if (left <= 0) { left = 0; gameOver(); } }

    @Override protected void render() {
        float cw = Math.min(36, 360f / shuffled.length()), sx = 200 - cw * shuffled.length() / 2;
        for (int i = 0; i < shuffled.length(); i++) { brick(sx + i * cw + 2, 40, cw - 4, cw - 4, PackPuzzle.PAL[i % 6]); text(String.valueOf(shuffled.charAt(i)), sx + i * cw + cw / 2, 40 + cw / 2, cw * 0.45f, Fonts.BOLD, 0xFFFFFFFF); }
        for (int i = 0; i < word.length(); i++) {
            box(sx + i * cw + 3, 130, cw - 6, 2.5f, 1, Style.sub);
            if (i < input.length()) text(String.valueOf(input.charAt(i)), sx + i * cw + cw / 2, 116, cw * 0.5f, Fonts.BOLD, Style.text);
        }
        if (flash > 0) text(lastOk ? "Richtig!" : "Falsch", 200, 165, 12, Fonts.BOLD, Gx.withAlpha(lastOk ? 0xFF39D353 : 0xFFFF3B4C, Math.min(1, flash * 2)));
        if (skips > 0) button("Überspringen (" + skips + ")", 150, 200, 100, 26, 0xFF5A6270);
        text(String.format(Locale.ROOT, "%.0f s", left), 370, 15, 9, Fonts.BOLD, left < 10 ? Style.danger : Style.text);
    }
}

// ====================================================================== Wortsuche
final class WordSearch extends GameView {
    private static final int N = 11;
    private static final float C = 22, OX = 10, OY = 10;
    private char[][] g;
    private final List<String> words = new ArrayList<>();
    private final List<int[]> found = new ArrayList<>();   // r0 c0 r1 c1
    private final List<String> foundW = new ArrayList<>();
    private int sr = -1, sc = -1;
    private float left;

    WordSearch() { super("wordsearch"); }
    @Override protected float boardW() { return 420; }
    @Override protected float boardH() { return 262; }
    @Override protected String overTitle() { return foundW.size() == words.size() ? "Alle Wörter gefunden!" : "Zeit um!"; }

    @Override protected void reset() {
        g = new char[N][N];
        words.clear(); found.clear(); foundW.clear();
        int[][] dirs = {{0, 1}, {1, 0}, {1, 1}, {-1, 1}};
        int tries = 0;
        while (words.size() < 7 && tries++ < 500) {
            String w = Words.ANY[rnd.nextInt(Words.ANY.length)];
            if (w.length() > 9 || words.contains(w)) continue;
            int[] d = dirs[rnd.nextInt(dirs.length)];
            int r = rnd.nextInt(N), c = rnd.nextInt(N);
            int er = r + d[0] * (w.length() - 1), ec = c + d[1] * (w.length() - 1);
            if (er < 0 || er >= N || ec >= N) continue;
            boolean ok = true;
            for (int i = 0; i < w.length(); i++) { char ch = g[r + d[0] * i][c + d[1] * i]; if (ch != 0 && ch != w.charAt(i)) ok = false; }
            if (!ok) continue;
            for (int i = 0; i < w.length(); i++) g[r + d[0] * i][c + d[1] * i] = w.charAt(i);
            words.add(w);
        }
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) if (g[r][c] == 0) g[r][c] = (char) ('A' + rnd.nextInt(26));
        left = 180; sr = -1;
    }

    private int[] cell(float x, float y) { int c = (int) ((x - OX) / C), r = (int) ((y - OY) / C); return x < OX || y < OY || c >= N || r >= N ? null : new int[]{r, c}; }

    @Override protected void onClick(float x, float y, int b) { int[] p = cell(x, y); if (p != null) { sr = p[0]; sc = p[1]; } }

    @Override protected void onRelease(float x, float y, int b) {
        int[] p = cell(x, y);
        if (p == null || sr < 0) { sr = -1; return; }
        int dr = Integer.signum(p[0] - sr), dc = Integer.signum(p[1] - sc), len = Math.max(Math.abs(p[0] - sr), Math.abs(p[1] - sc)) + 1;
        if ((p[0] - sr) != 0 && (p[1] - sc) != 0 && Math.abs(p[0] - sr) != Math.abs(p[1] - sc)) { sr = -1; return; }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(g[sr + dr * i][sc + dc * i]);
        String s = sb.toString(), rev = sb.reverse().toString();
        for (String w : words) if (!foundW.contains(w) && (w.equals(s) || w.equals(rev))) {
            foundW.add(w); found.add(new int[]{sr, sc, p[0], p[1]}); score += w.length() * 10; PackPuzzle.good();
            if (foundW.size() == words.size()) { score += (long) left; PackPuzzle.win(); gameOver(); }
        }
        sr = -1;
    }

    @Override protected void update(float dt) { left -= dt; if (left <= 0) { left = 0; gameOver(); } }

    @Override protected void render() {
        for (int[] f : found) line(OX + f[1] * C + C / 2, OY + f[0] * C + C / 2, OX + f[3] * C + C / 2, OY + f[2] * C + C / 2, 15, 0x6639D353);
        if (sr >= 0) { int[] p = cell(bmx(), bmy()); if (p != null) line(OX + sc * C + C / 2, OY + sr * C + C / 2, OX + p[1] * C + C / 2, OY + p[0] * C + C / 2, 15, 0x553FA9FF); }
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) text(String.valueOf(g[r][c]), OX + c * C + C / 2, OY + r * C + C / 2, 9, Fonts.BOLD, Style.text);
        float y = 30;
        for (String w : words) { boolean f = foundW.contains(w); textL(w, 268, y, 7.5f, Fonts.BOLD, f ? Style.ok : Style.text); if (f) box(266, y, Gx.width(w, 7.5f, Fonts.BOLD) + 4, 1.2f, 0, Style.ok); y += 18; }
        text(String.format(Locale.ROOT, "%.0f s", left), 330, 240, 9, Fonts.BOLD, left < 20 ? Style.danger : Style.sub);
        textL("Ziehe über ein Wort", 268, 210, 6.4f, Fonts.MEDIUM, Style.muted);
    }
}

// ====================================================================== Edelstein-Tausch (Match 3)
final class Match3 extends GameView {
    private static final int N = 8;
    private static final float C = 31, OX = 10, OY = 10;
    private int[][] g = new int[N][N];
    private final float[][] fall = new float[N][N];
    private int sr = -1, sc = -1, combo;
    private float left, settle;

    Match3() { super("match3"); }
    @Override protected float boardW() { return 400; }
    @Override protected float boardH() { return 268; }
    @Override protected String overTitle() { return "Zeit um!"; }

    @Override protected void reset() {
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) do g[r][c] = rnd.nextInt(6); while ((c >= 2 && g[r][c] == g[r][c - 1] && g[r][c] == g[r][c - 2]) || (r >= 2 && g[r][c] == g[r - 1][c] && g[r][c] == g[r - 2][c]));
        left = 90; sr = -1; settle = 0;
    }

    private boolean[][] matches() {
        boolean[][] m = new boolean[N][N];
        for (int r = 0; r < N; r++) for (int c = 0; c < N - 2; c++) if (g[r][c] >= 0 && g[r][c] == g[r][c + 1] && g[r][c] == g[r][c + 2]) m[r][c] = m[r][c + 1] = m[r][c + 2] = true;
        for (int c = 0; c < N; c++) for (int r = 0; r < N - 2; r++) if (g[r][c] >= 0 && g[r][c] == g[r + 1][c] && g[r][c] == g[r + 2][c]) m[r][c] = m[r + 1][c] = m[r + 2][c] = true;
        return m;
    }

    private int clear() {
        boolean[][] m = matches();
        int n = 0;
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) if (m[r][c]) { g[r][c] = -1; n++; }
        if (n == 0) return 0;
        for (int c = 0; c < N; c++) {
            int w = N - 1;
            for (int r = N - 1; r >= 0; r--) if (g[r][c] >= 0) { if (w != r) { g[w][c] = g[r][c]; fall[w][c] = w - r; } w--; }
            for (int r = w; r >= 0; r--) { g[r][c] = rnd.nextInt(6); fall[r][c] = w + 2; }
        }
        return n;
    }

    @Override protected void onClick(float x, float y, int b) {
        if (settle > 0) return;
        int c = (int) ((x - OX) / C), r = (int) ((y - OY) / C);
        if (x < OX || y < OY || c >= N || r >= N) return;
        if (sr < 0) { sr = r; sc = c; return; }
        if (Math.abs(sr - r) + Math.abs(sc - c) != 1) { sr = r; sc = c; return; }
        int t = g[r][c]; g[r][c] = g[sr][sc]; g[sr][sc] = t;
        if (matches()[r][c] || matches()[sr][sc]) { combo = 0; settle = 0.01f; }
        else { t = g[r][c]; g[r][c] = g[sr][sc]; g[sr][sc] = t; PackPuzzle.bad(); }
        sr = -1;
    }

    @Override protected void update(float dt) {
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) fall[r][c] = Math.max(0, fall[r][c] - dt * 10);
        if (settle > 0 && (settle -= dt) <= 0) {
            int n = clear();
            if (n > 0) { combo++; score += n * 10L * combo; Sound.play("minecraft:block.amethyst_block.chime", 1.0f + combo * 0.15f, 0.5f); settle = 0.28f; }
        }
        left -= dt;
        if (left <= 0) { left = 0; gameOver(); }
    }

    @Override protected void render() {
        box(OX - 3, OY - 3, N * C + 6, N * C + 6, 8, Style.light ? 0x14000000 : 0x14FFFFFF);
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) {
            if (g[r][c] < 0) continue;
            float x = OX + c * C, y = OY + (r - fall[r][c]) * C;
            int col = PackPuzzle.PAL[g[r][c]];
            if (r == sr && c == sc) box(x, y, C, C, 6, 0x66FFFFFF);
            switch (g[r][c] % 3) {
                case 0 -> circle(x + C / 2, y + C / 2, C * 0.38f, col);
                case 1 -> box(x + 5, y + 5, C - 10, C - 10, 4, col);
                default -> { circle(x + C / 2, y + C / 2, C * 0.36f, col); box(x + 9, y + 9, C - 18, C - 18, 2, Gx.mix(col, 0xFFFFFFFF, 0.3f)); }
            }
            circle(x + C * 0.38f, y + C * 0.36f, 2.5f, 0x88FFFFFF);
        }
        text(String.format(Locale.ROOT, "%.0f s", left), 330, 40, 12, Fonts.BOLD, left < 10 ? Style.danger : Style.text);
        if (combo > 1) text("Combo x" + combo, 330, 80, 9, Fonts.BOLD, 0xFFFFCD03);
        text("Tausche zwei Nachbarn", 330, 220, 6.2f, Fonts.MEDIUM, Style.sub);
        text("für 3 in einer Reihe", 330, 232, 6.2f, Fonts.MEDIUM, Style.sub);
    }
}

// ====================================================================== Klötzchen-Klick (SameGame)
final class SameGame extends GameView {
    private static final int COLS = 14, ROWS = 10;
    private static final float C = 21, OX = 6, OY = 8;
    private int[][] g = new int[COLS][ROWS];   // [col][row from bottom], -1 empty
    private int hover = 0;

    SameGame() { super("samegame"); }
    @Override protected float boardW() { return COLS * C + 12; }
    @Override protected float boardH() { return ROWS * C + 30; }
    @Override protected String overTitle() { return "Keine Gruppen mehr"; }

    @Override protected void reset() { for (int c = 0; c < COLS; c++) for (int r = 0; r < ROWS; r++) g[c][r] = rnd.nextInt(4); }

    private List<int[]> group(int c, int r) {
        List<int[]> out = new ArrayList<>();
        if (c < 0 || r < 0 || c >= COLS || r >= ROWS || g[c][r] < 0) return out;
        int col = g[c][r];
        boolean[][] seen = new boolean[COLS][ROWS];
        java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<>();
        q.add(new int[]{c, r}); seen[c][r] = true;
        while (!q.isEmpty()) {
            int[] p = q.poll(); out.add(p);
            for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) { int a = p[0] + d[0], b = p[1] + d[1]; if (a >= 0 && b >= 0 && a < COLS && b < ROWS && !seen[a][b] && g[a][b] == col) { seen[a][b] = true; q.add(new int[]{a, b}); } }
        }
        return out;
    }

    private int[] at(float x, float y) { int c = (int) ((x - OX) / C), r = (int) ((OY + ROWS * C - y) / C); return new int[]{c, r}; }

    @Override protected void onClick(float x, float y, int b) {
        int[] p = at(x, y);
        List<int[]> gr = group(p[0], p[1]);
        if (gr.size() < 2) return;
        for (int[] q : gr) g[q[0]][q[1]] = -1;
        score += (long) (gr.size() - 1) * (gr.size() - 1);
        Sound.play("minecraft:entity.item.pickup", 0.8f + Math.min(1, gr.size() / 15f), 0.4f);
        // gravity + collapse empty columns
        for (int c = 0; c < COLS; c++) { int w = 0; for (int r = 0; r < ROWS; r++) if (g[c][r] >= 0) { g[c][w] = g[c][r]; if (w != r) g[c][r] = -1; w++; } }
        int w = 0;
        for (int c = 0; c < COLS; c++) if (g[c][0] >= 0) { if (w != c) { g[w] = g[c]; g[c] = new int[ROWS]; java.util.Arrays.fill(g[c], -1); } w++; }
        boolean any = false, empty = true;
        for (int c = 0; c < COLS; c++) for (int r = 0; r < ROWS; r++) { if (g[c][r] >= 0) empty = false; if (group(c, r).size() >= 2) any = true; }
        if (empty) score += 1000;
        if (!any) { PackPuzzle.win(); gameOver(); }
    }

    @Override protected void update(float dt) { }

    @Override protected void render() {
        int[] p = at(bmx(), bmy());
        List<int[]> hg = group(p[0], p[1]);
        java.util.Set<Long> hs = new java.util.HashSet<>();
        if (hg.size() >= 2) for (int[] q : hg) hs.add(q[0] * 100L + q[1]);
        for (int c = 0; c < COLS; c++) for (int r = 0; r < ROWS; r++) if (g[c][r] >= 0) {
            int col = PackPuzzle.PAL[g[c][r]];
            if (hs.contains(c * 100L + r)) col = Gx.mix(col, 0xFFFFFFFF, 0.3f);
            brick(OX + c * C + 0.5f, OY + (ROWS - 1 - r) * C + 0.5f, C - 1, C - 1, col);
        }
        text(hg.size() >= 2 ? hg.size() + " Steine = +" + (hg.size() - 1) * (hg.size() - 1) : "Klick auf Gruppen ab 2 gleichen Steinen", (COLS * C + 12) / 2, OY + ROWS * C + 12, 7, Fonts.BOLD, Style.sub);
    }
}

// ====================================================================== Kniffel
final class Kniffel extends GameView {
    private static final String[] CAT = {"Einer", "Zweier", "Dreier", "Vierer", "Fünfer", "Sechser", "Dreierpasch", "Viererpasch", "Full House", "Kl. Straße", "Gr. Straße", "Kniffel", "Chance"};
    private final int[] dice = new int[5];
    private final boolean[] hold = new boolean[5];
    private final int[] sheet = new int[13];
    private int rolls;
    private float rollAnim;

    Kniffel() { super("kniffel"); }
    @Override protected float boardW() { return 420; }
    @Override protected float boardH() { return 280; }
    @Override protected String overTitle() { return "Endstand: " + score; }

    @Override protected void reset() { java.util.Arrays.fill(sheet, -1); java.util.Arrays.fill(hold, false); rolls = 0; roll(); }

    private void roll() { if (rolls >= 3) return; for (int i = 0; i < 5; i++) if (!hold[i]) dice[i] = 1 + rnd.nextInt(6); rolls++; rollAnim = 0.35f; Sound.play("minecraft:block.wood.hit", 1.6f, 0.35f); }

    private int value(int cat) {
        int[] cnt = new int[7];
        int sum = 0;
        for (int d : dice) { cnt[d]++; sum += d; }
        int max = 0; boolean three = false, two = false;
        for (int v = 1; v <= 6; v++) { max = Math.max(max, cnt[v]); if (cnt[v] == 3) three = true; if (cnt[v] == 2) two = true; }
        int run = 0, best = 0;
        for (int v = 1; v <= 6; v++) { run = cnt[v] > 0 ? run + 1 : 0; best = Math.max(best, run); }
        return switch (cat) {
            case 0, 1, 2, 3, 4, 5 -> cnt[cat + 1] * (cat + 1);
            case 6 -> max >= 3 ? sum : 0;
            case 7 -> max >= 4 ? sum : 0;
            case 8 -> (three && two) || max == 5 ? 25 : 0;
            case 9 -> best >= 4 ? 30 : 0;
            case 10 -> best >= 5 ? 40 : 0;
            case 11 -> max == 5 ? 50 : 0;
            default -> sum;
        };
    }

    private int total() {
        int up = 0, all = 0;
        for (int i = 0; i < 13; i++) if (sheet[i] >= 0) { all += sheet[i]; if (i < 6) up += sheet[i]; }
        return all + (up >= 63 ? 35 : 0);
    }

    @Override protected void onClick(float x, float y, int b) {
        for (int i = 0; i < 5; i++) if (in(x, y, 20 + i * 46, 30, 40, 40) && rolls > 0) { hold[i] = !hold[i]; PackPuzzle.click(); }
        if (in(x, y, 60, 90, 140, 30)) roll();
        for (int i = 0; i < 13; i++) if (sheet[i] < 0 && in(x, y, 250, 10 + i * 19.5f, 160, 18)) {
            sheet[i] = value(i); score = total(); PackPuzzle.good();
            java.util.Arrays.fill(hold, false); rolls = 0;
            boolean done = true; for (int s : sheet) if (s < 0) done = false;
            if (done) { PackPuzzle.win(); gameOver(); } else roll();
            return;
        }
    }

    @Override protected void onKey(int k) { if (k == K_SPACE || k == K_R) roll(); if (k >= 49 && k <= 53) hold[k - 49] = !hold[k - 49]; }
    @Override protected void update(float dt) { rollAnim = Math.max(0, rollAnim - dt); }

    private void die(float x, float y, int v, boolean held) {
        box(x, y, 40, 40, 8, held ? 0xFFFFE14D : 0xFFF8F8F8);
        int[][] pip = {{}, {4}, {0, 8}, {0, 4, 8}, {0, 2, 6, 8}, {0, 2, 4, 6, 8}, {0, 2, 3, 5, 6, 8}};
        for (int p : pip[v]) circle(x + 10 + (p % 3) * 10, y + 10 + (p / 3) * 10, 3.2f, 0xFF15161A);
    }

    @Override protected void render() {
        for (int i = 0; i < 5; i++) {
            float j = rollAnim > 0 && !hold[i] ? (float) Math.sin(rollAnim * 60 + i) * 3 : 0;
            die(20 + i * 46, 30 + j, rollAnim > 0 && !hold[i] ? 1 + rnd.nextInt(6) : dice[i], hold[i]);
        }
        if (rolls < 3) button("Würfeln (" + (3 - rolls) + ")", 60, 90, 140, 30, Style.accent);
        else text("Wähle rechts eine Kategorie", 130, 105, 7, Fonts.BOLD, Style.sub);
        text("Klick auf Würfel = halten", 130, 140, 6.4f, Fonts.MEDIUM, Style.muted);
        int up = 0; for (int i = 0; i < 6; i++) if (sheet[i] > 0) up += sheet[i];
        text("Oben: " + up + " / 63 (Bonus 35)", 130, 175, 7, Fonts.BOLD, up >= 63 ? Style.ok : Style.sub);
        text("Gesamt: " + total(), 130, 200, 11, Fonts.BOLD, Style.text);
        for (int i = 0; i < 13; i++) {
            float y = 10 + i * 19.5f;
            boolean free = sheet[i] < 0, hv = free && in(bmx(), bmy(), 250, y, 160, 18);
            box(250, y, 160, 18, 5, hv ? Style.surfaceHover : Style.surface);
            textL(CAT[i], 256, y + 9, 6.8f, Fonts.MEDIUM, Style.text);
            String v = free ? (rolls > 0 ? String.valueOf(value(i)) : "") : String.valueOf(sheet[i]);
            text(v, 395, y + 9, 7, Fonts.BOLD, free ? Gx.withAlpha(Style.accent, 0.8f) : Style.text);
        }
    }
}

// ====================================================================== Höher oder Tiefer
final class HigherLower extends GameView {
    private int cur, curS, next, nextS, streak;
    private float reveal = -1;
    private boolean ok;

    HigherLower() { super("higherlower"); }
    @Override protected float boardW() { return 380; }
    @Override protected float boardH() { return 240; }
    @Override protected String scoreLabel() { return "Serie"; }
    @Override protected String overTitle() { return "Falsch geraten!"; }

    @Override protected void reset() { cur = rnd.nextInt(13); curS = rnd.nextInt(4); streak = 0; reveal = -1; }

    private void guess(boolean higher) {
        if (reveal >= 0) return;
        do { next = rnd.nextInt(13); nextS = rnd.nextInt(4); } while (next == cur && nextS == curS);
        ok = next == cur || (higher ? next > cur : next < cur);
        reveal = 0;
        if (ok) { streak++; score = streak; PackPuzzle.good(); } else PackPuzzle.bad();
    }

    @Override protected void onClick(float x, float y, int b) { if (in(x, y, 220, 60, 120, 36)) guess(true); if (in(x, y, 220, 110, 120, 36)) guess(false); }
    @Override protected void onKey(int k) { if (k == K_UP || k == K_W) guess(true); if (k == K_DOWN || k == K_S) guess(false); }

    @Override protected void update(float dt) {
        if (reveal >= 0 && (reveal += dt) > 1.0f) { if (!ok) { gameOver(); return; } cur = next; curS = nextS; reveal = -1; }
    }

    @Override protected void render() {
        Gx.push(); Gx.scaleAt(X(40 + 17), Y(70 + 24), 2f);
        PackMind.card(this, 40, 70, curS, cur, true, false);
        Gx.pop();
        if (reveal >= 0) { Gx.push(); Gx.scaleAt(X(140 + 17), Y(70 + 24), 1.6f); PackMind.card(this, 140, 70, nextS, next, true, false); Gx.pop(); text(ok ? "✔" : "✖", 157, 150, 18, Fonts.BOLD, ok ? 0xFF39D353 : 0xFFFF3B4C); }
        button("▲ Höher", 220, 60, 120, 36, 0xFF2E9E4A);
        button("▼ Tiefer", 220, 110, 120, 36, 0xFFB0202A);
        text("Serie: " + streak, 280, 180, 10, Fonts.BOLD, Style.text);
        text("Gleiche Karte zählt als richtig · Ass ist hoch", 190, 222, 6.4f, Fonts.MEDIUM, Style.muted);
    }
}

// ====================================================================== Mau-Mau
final class MauMau extends GameView {
    // Karten 7, 8, 9, 10, B, D, K, A (Index 5..12 in RANKS)
    private final List<int[]> deck = new ArrayList<>(), pile = new ArrayList<>(), me = new ArrayList<>(), ai = new ArrayList<>();
    private int turn, wish = -1, draw2;
    private boolean choosing, skipNext;
    private float think;
    private String msg = "";
    private int wins;

    MauMau() { super("maumau"); }
    @Override protected boolean hasAi() { return true; }
    @Override protected float boardW() { return 420; }
    @Override protected float boardH() { return 290; }
    @Override protected String scoreLabel() { return "Siege"; }
    @Override protected String overTitle() { return me.isEmpty() ? "Mau-Mau! Gewonnen" : "Die KI hat gewonnen"; }

    @Override protected void reset() {
        deck.clear(); pile.clear(); me.clear(); ai.clear();
        for (int s = 0; s < 4; s++) for (int r = 5; r <= 12; r++) deck.add(new int[]{s, r});
        Collections.shuffle(deck, rnd);
        for (int i = 0; i < 5; i++) { me.add(deck.remove(0)); ai.add(deck.remove(0)); }
        int[] top; do { top = deck.remove(0); if (top[1] == 9 || top[1] == 5 || top[1] == 6) deck.add(top); } while (top[1] == 9 || top[1] == 5 || top[1] == 6);
        pile.add(top);
        turn = 1; wish = -1; draw2 = 0; choosing = false; think = 0; msg = ""; score = wins;
    }

    private int[] top() { return pile.get(pile.size() - 1); }

    private boolean fits(int[] c) {
        if (draw2 > 0) return c[1] == 5;           // auf eine 7 nur 7
        if (c[1] == 9) return true;                 // Bube auf alles
        int suit = wish >= 0 ? wish : top()[0];
        return c[0] == suit || (wish < 0 && c[1] == top()[1]);
    }

    private int[] drawCard() {
        if (deck.isEmpty()) { int[] t = pile.remove(pile.size() - 1); deck.addAll(pile); pile.clear(); pile.add(t); Collections.shuffle(deck, rnd); }
        return deck.isEmpty() ? null : deck.remove(0);
    }

    private void play(List<int[]> hand, int[] c, int who) {
        hand.remove(c); pile.add(c); wish = -1;
        Sound.play("minecraft:item.book.page_turn", 1.2f, 0.5f);
        if (hand.isEmpty()) { if (who == 1) { wins++; score = wins; PackPuzzle.win(); } else PackPuzzle.bad(); gameOver(); return; }
        if (c[1] == 5) draw2 += 2;
        if (c[1] == 6) skipNext = true;
        if (c[1] == 9) {
            if (who == 1) { choosing = true; return; }
            int[] cnt = new int[4]; for (int[] k : ai) cnt[k[0]]++;
            int best = 0; for (int s = 1; s < 4; s++) if (cnt[s] > cnt[best]) best = s;
            wish = best; msg = "KI wünscht sich " + PackMind.SUIT_NAMES[best];
        }
        endTurn();
    }

    private void endTurn() {
        if (skipNext) { skipNext = false; msg = turn == 1 ? "KI muss aussetzen" : "Du musst aussetzen"; think = 0; return; }
        turn = 3 - turn; think = 0;
    }

    private void takePenalty(List<int[]> hand) {
        int n = Math.max(1, draw2);
        for (int i = 0; i < n; i++) { int[] c = drawCard(); if (c != null) hand.add(c); }
        draw2 = 0;
    }

    @Override protected void update(float dt) {
        if (turn != 2 || choosing) return;
        if ((think += dt) < 0.9f) return;
        List<int[]> ok = new ArrayList<>();
        for (int[] c : ai) if (fits(c)) ok.add(c);
        if (ok.isEmpty()) { takePenalty(ai); msg = "KI zieht"; endTurn(); return; }
        int lv = aiLevel();
        int[] pick = ok.get(rnd.nextInt(ok.size()));
        if (lv >= 2) for (int[] c : ok) if (c[1] != 9) { pick = c; if (c[1] == 5 || c[1] == 6) break; }
        play(ai, pick, 2);
    }

    @Override protected void onClick(float x, float y, int b) {
        if (turn != 1) return;
        if (choosing) { for (int s = 0; s < 4; s++) if (in(x, y, 100 + s * 56, 120, 50, 40)) { wish = s; choosing = false; msg = "Du wünschst dir " + PackMind.SUIT_NAMES[s]; endTurn(); } return; }
        if (in(x, y, 250, 110, 34, 48)) { takePenalty(me); msg = ""; endTurn(); return; }
        float w = Math.min(38, 380f / Math.max(1, me.size()));
        for (int i = me.size() - 1; i >= 0; i--) if (in(x, y, 20 + i * w, 220, 34, 48)) { int[] c = me.get(i); if (fits(c)) play(me, c, 1); else PackPuzzle.bad(); return; }
    }

    @Override protected void render() {
        box(0, 0, 420, 290, 10, 0xFF1E6B3A);
        for (int i = 0; i < ai.size(); i++) PackMind.card(this, 20 + i * Math.min(30, 380f / ai.size()), 10, 0, 0, false, false);
        PackMind.card(this, 190, 110, top()[0], top()[1], true, false);
        PackMind.card(this, 250, 110, 0, 0, false, false);
        text("ziehen", 267, 166, 6, Fonts.MEDIUM, 0xCCFFFFFF);
        if (wish >= 0) text("Wunsch: " + PackMind.SUITS[wish] + " " + PackMind.SUIT_NAMES[wish], 100, 134, 8, Fonts.BOLD, 0xFFFFE14D);
        if (draw2 > 0) text("+" + draw2 + " ziehen!", 100, 150, 8, Fonts.BOLD, 0xFFFF6A6A);
        float w = Math.min(38, 380f / Math.max(1, me.size()));
        for (int i = 0; i < me.size(); i++) { int[] c = me.get(i); boolean hv = turn == 1 && in(bmx(), bmy(), 20 + i * w, 220, Math.min(w, 34), 48); PackMind.card(this, 20 + i * w, hv ? 212 : 220, c[0], c[1], true, turn == 1 && fits(c)); }
        text(turn == 1 ? (choosing ? "Wähle eine Farbe" : "Du bist dran") : "KI überlegt...", 340, 134, 8, Fonts.BOLD, 0xFFFFFFFF);
        if (!msg.isEmpty()) text(msg, 340, 152, 6.5f, Fonts.MEDIUM, 0xCCFFFFFF);
        if (choosing) { box(90, 110, 240, 60, 10, 0xEE101418); for (int s = 0; s < 4; s++) button(PackMind.SUITS[s], 100 + s * 56, 120, 50, 40, s >= 2 ? 0xFFB0202A : 0xFF2A2E38); }
        text("7 = +2 · 8 = Aussetzen · Bube = Wünschen", 210, 200, 6, Fonts.MEDIUM, 0xAAFFFFFF);
    }
}
