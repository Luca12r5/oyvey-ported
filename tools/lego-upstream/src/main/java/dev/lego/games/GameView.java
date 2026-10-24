package dev.lego.games;

import java.util.List;
import java.util.Random;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import dev.lego.ui.View;
import dev.lego.ui.hub.HubView;

/**
 * Base class of all mini games: frame, top bar, start / pause / game-over overlays, fixed 60 Hz update loop.
 * Subclasses work in "board units" (design units, scaled by u = pixels per unit).
 */
public abstract class GameView extends View {
    @Override public boolean blur() { return false; }   // blur costs a full-screen pass every frame -> games feel laggy
    public static final int K_LEFT = 263, K_RIGHT = 262, K_UP = 265, K_DOWN = 264, K_SPACE = 32, K_ENTER = 257,
            K_W = 87, K_A = 65, K_S = 83, K_D = 68, K_R = 82, K_P = 80, K_ESC = 256;

    protected enum State { READY, PLAYING, PAUSED, OVER }

    protected final GameInfo info;
    protected final Random rnd = new Random();
    protected State state = State.READY;
    protected long score = 0;
    /** Seconds since the round started (only advances while playing). */
    protected float time = 0;
    private float acc = 0;
    private boolean record = false;
    private long stateAt = System.currentTimeMillis();
    /** Board origin in pixels + pixels per board unit (valid during render / input). */
    protected int bx, by;
    protected float u = 2;

    private boolean initialized = false;

    protected GameView(String id) {
        this.info = GameInfo.get(id);
    }

    // ------------------------------------------------------------------ to implement
    protected abstract float boardW();
    protected abstract float boardH();
    /** New round. */
    protected abstract void reset();
    /** Fixed step update (1/60 s) while playing. */
    protected abstract void update(float dt);
    /** Draw the board: origin (bx, by) in pixels, u pixels per board unit. */
    protected abstract void render();

    protected void onKey(int key) { }
    protected void onKeyUp(int key) { }
    /** Mouse press in board units. */
    protected void onClick(float x, float y, int button) { }
    protected void onRelease(float x, float y, int button) { }
    protected void onDrag(float x, float y) { }
    /** Text shown in the score box. */
    protected String scoreText() { return String.valueOf(score); }
    protected String scoreLabel() { return "Punkte"; }
    protected String bestText(long best) { return String.valueOf(best); }
    /** Title of the game-over overlay (e.g. "Gewonnen!", "Unentschieden"). */
    protected String overTitle() { return "Game Over"; }
    /** true: the game starts with a click / space on the READY screen. */
    protected boolean clickToStart() { return true; }
    /** true: the game has a computer opponent -> difficulty chips on the start / game-over screen. */
    protected boolean hasAi() { return false; }
    /** Chosen AI difficulty: 0 Leicht, 1 Mittel, 2 Schwer, 3 Profi. */
    protected int aiLevel() { return GameInfo.difficulty(info.id); }

    // ------------------------------------------------------------------ helpers for subclasses
    protected void start() {
        reset();
        score = 0;
        time = 0;
        record = false;
        state = State.PLAYING;
        stateAt = System.currentTimeMillis();
    }

    protected void gameOver() { gameOver(true); }

    /** Ends the round; submit = false does not count the score (e.g. aborted / false start). */
    protected void gameOver(boolean submit) {
        if (state == State.OVER) return;
        state = State.OVER;
        stateAt = System.currentTimeMillis();
        record = submit && GameInfo.submit(info.id, score);
        Sound.play(record ? "minecraft:ui.toast.challenge_complete" : "minecraft:block.note_block.bass", record ? 1.2f : 0.7f, 0.35f);
    }

    protected int X(float bu) { return Math.round(bx + bu * u); }
    protected int Y(float bu) { return Math.round(by + bu * u); }
    protected int S(float bu) { return Math.round(bu * u); }

    protected void box(float x, float y, float w, float h, float r, int argb) { Gx.rect(X(x), Y(y), S(w), S(h), S(r), argb); }
    protected void boxV(float x, float y, float w, float h, float r, int top, int bottom) { Gx.rectV(X(x), Y(y), S(w), S(h), S(r), top, bottom); }
    protected void circle(float cx, float cy, float r, int argb) { Gx.circle(X(cx), Y(cy), Math.max(1, S(r)), argb); }
    protected void text(String s, float cx, float cy, float size, int weight, int argb) { Gx.textCenter(s, X(cx), Y(cy), size * u, weight, argb); }

    /** A glossy Lego stud-brick in board units (w x h), color argb. */
    protected void brick(float x, float y, float w, float h, int argb) {
        float r = Math.min(w, h) * 0.18f;
        boxV(x, y, w, h, r, Gx.mix(argb, 0xFFFFFFFF, 0.18f), Gx.mix(argb, 0xFF000000, 0.12f));
        Gx.outline(X(x), Y(y), S(w), S(h), S(r), Math.max(1, S(Math.min(w, h) * 0.05f)), Gx.withAlpha(Gx.mix(argb, 0xFF000000, 0.45f), 0.7f));
        float sr = Math.min(w, h) * 0.17f;
        int studs = Math.max(1, Math.round(w / h));
        for (int i = 0; i < Math.min(studs, 4); i++) {
            float sx = x + w * (i + 0.5f) / Math.min(studs, 4);
            circle(sx, y + h * 0.42f, sr, Gx.mix(argb, 0xFF000000, 0.2f));
            circle(sx, y + h * 0.38f, sr * 0.9f, Gx.mix(argb, 0xFFFFFFFF, 0.22f));
        }
    }

    /** Thick line with round ends (board units). */
    protected void line(float x1, float y1, float x2, float y2, float th, int argb) {
        float px1 = bx + x1 * u, py1 = by + y1 * u, px2 = bx + x2 * u, py2 = by + y2 * u;
        float len = (float) Math.hypot(px2 - px1, py2 - py1);
        int r = Math.max(1, Math.round(th * u / 2));
        int n = Math.max(1, (int) Math.ceil(len / Math.max(1f, r * 0.45f)));
        for (int i = 0; i <= n; i++) {
            float t = i / (float) n;
            Gx.circle(Math.round(px1 + (px2 - px1) * t), Math.round(py1 + (py2 - py1) * t), r, argb);
        }
    }

    /** Ring outline (board units). */
    protected void ring(float cx, float cy, float r, float th, int argb) { Gx.ringAt(X(cx), Y(cy), Math.max(1, S(r)), Math.max(1, S(th)), argb); }

    /** Mouse position in board units. */
    protected float bmx() { return (mx - bx) / u; }
    protected float bmy() { return (my - by) / u; }

    /** Left-aligned / centered text helpers in board units. */
    protected void textL(String s, float x, float cy, float size, int weight, int argb) { Gx.textMid(s, X(x), Y(cy), size * u, weight, argb); }

    /** Small rounded button in board units; returns true when the mouse is over it. */
    protected boolean button(String label, float x, float y, float w, float h, int base) {
        float hx = bmx(), hy = bmy();
        boolean hv = hx >= x && hy >= y && hx <= x + w && hy <= y + h;
        box(x, y, w, h, Math.min(h / 2, 8), hv ? Gx.mix(base, 0xFFFFFFFF, 0.15f) : base);
        text(label, x + w / 2, y + h / 2, Math.min(9, h * 0.42f), Fonts.BOLD, 0xFFFFFFFF);
        return hv;
    }

    protected static boolean in(float x, float y, float rx, float ry, float rw, float rh) { return x >= rx && y >= ry && x <= rx + rw && y <= ry + rh; }

    // ------------------------------------------------------------------ frame
    /** The window is at least this wide so the title bar never gets cut off on narrow games. */
    private static final float MIN_BOARD = 360;
    @Override public float designW() { return Math.max(boardW(), MIN_BOARD) + 60; }
    @Override public float designH() { return boardH() + 110; }
    @Override public float openMs() { return 320; }

    @Override
    public final void draw() {
        if (!initialized) { initialized = true; reset(); }
        // fixed-step update
        if (state == State.PLAYING) {
            acc += Math.min(0.1f, Gx.dt);
            while (acc >= 1 / 60f && state == State.PLAYING) {
                update(1 / 60f);
                time += 1 / 60f;
                acc -= 1 / 60f;
            }
        } else acc = 0;

        float t = Ease.outQuint(open);
        Gx.fill(0, 0, W, H, Gx.rgba(0x000000, 0.35f * open));
        int bw = p(boardW()), bh = p(boardH());
        int pw = Math.max(bw, p(MIN_BOARD)) + p(28), ph = bh + p(72);
        int px = (W - pw) / 2, py = (H - ph) / 2;
        Gx.pushAlpha(t);
        Gx.push();
        Gx.scaleAt(W / 2f, H / 2f, 0.94f + 0.06f * t);
        Gx.shadow(px, py, pw, ph, p(18), p(14), 0.55f);
        Gx.rect(px, py, pw, ph, p(18), Style.bg);
        Gx.outline(px, py, pw, ph, p(18), Math.max(1, p(0.6)), Style.stroke);

        // top bar
        int tb = py + p(14);
        int bs = p(30);
        boolean hb = hover(px + p(14), tb, bs, bs);
        Gx.rect(px + p(14), tb, bs, bs, p(9), hb ? Style.surfaceHover : Style.surface);
        Gx.icon("back", px + p(14) + bs / 2f - pf(1), tb + bs / 2f, pf(14), Style.text);
        Kit.hit(px + p(14), tb, bs, bs).click(this::back);
        int is = p(30);
        Gx.rect(px + p(52), tb, is, is, p(9), Gx.withAlpha(info.color, 0.2f));
        Gx.icon(info.icon, px + p(52) + is / 2f, tb + is / 2f, pf(16), info.color);
        Gx.text(Fonts.ellipsize(info.name, Fonts.BOLD, pf(12.5f), pw - p(290)), px + p(90), tb + pf(1), pf(12.5f), Fonts.BOLD, Style.text);
        // score boxes
        int sw = p(84), sh = p(32);
        int sx = px + pw - p(14) - sw;
        Gx.text(Fonts.ellipsize(info.tagline, Fonts.REGULAR, pf(7.2f), sx - sw - p(18) - (px + p(90))), px + p(90), tb + pf(18), pf(7.2f), Fonts.REGULAR, Style.sub);
        scoreBox(sx, tb - p(1), sw, sh, "REKORD", GameInfo.hasBest(info.id) ? bestText(GameInfo.best(info.id)) : "-", false);
        scoreBox(sx - sw - p(8), tb - p(1), sw, sh, scoreLabel().toUpperCase(java.util.Locale.ROOT), scoreText(), true);

        // board
        bx = px + (pw - bw) / 2;
        by = py + p(58);
        u = P;
        Gx.rect(bx, by, bw, bh, p(12), Style.light ? 0xFFE9EBF0 : 0xFF0B0C10);
        Gx.clip(bx, by, bw, bh);
        try { render(); } catch (Throwable e) { dev.lego.LegoClient.LOG("Spiel-Fehler: " + e); }
        Gx.unclip();
        Gx.outline(bx, by, bw, bh, p(12), Math.max(1, p(0.6)), Style.stroke);
        // board input
        Kit.hit(bx, by, bw, bh).drag((mx, my) -> {
            float gx = (mx - bx) / u, gy = (my - by) / u;
            if (dragStarted) onDrag(gx, gy);
            else { dragStarted = true; boardPress(gx, gy, 0); }
        }).right(() -> boardPress((this.mx - bx) / u, (this.my - by) / u, 1));

        // overlays
        overlay(bx, by, bw, bh);
        Gx.pop();
        Gx.popAlpha();
    }

    private boolean dragStarted = false;

    private void boardPress(float gx, float gy, int button) {
        if (state == State.READY && clickToStart()) { start(); Sound.click(); return; }
        if (state == State.PLAYING) onClick(gx, gy, button);
    }

    @Override
    public boolean mouseUp(float x, float y, int button) {
        if (dragStarted && state == State.PLAYING) onRelease((x - bx) / u, (y - by) / u, button);
        dragStarted = false;
        return super.mouseUp(x, y, button);
    }

    private void scoreBox(int x, int y, int w, int h, String label, String value, boolean main) {
        Gx.rect(x, y, w, h, p(8), main ? Gx.withAlpha(Style.accent, 0.14f) : Style.surface);
        Gx.textCenter(label, x + w / 2f, y + pf(9), pf(5.6f), Fonts.BOLD, main ? Style.accent : Style.muted);
        Gx.textCenter(value, x + w / 2f, y + pf(21.5f), pf(10), Fonts.BOLD, Style.text);
    }

    private void overlay(int x, int y, int w, int h) {
        float since = (System.currentTimeMillis() - stateAt) / 1000f;
        if (state == State.PLAYING) return;
        float a = Ease.outCubic(Math.min(1, since / 0.25f));
        Gx.pushAlpha(a);
        Gx.rect(x, y, w, h, p(12), Gx.rgba(Style.light ? 0xF3F4F7 : 0x07080B, 0.82f));
        int cx = x + w / 2;
        if (state == State.READY) {
            int cy = y + h / 2 - p(56);
            Gx.glow(cx, cy, p(60), Gx.withAlpha(info.color, 0.25f));
            Gx.icon(info.icon, cx, cy, pf(34), info.color);
            Gx.textCenter(info.name, cx, cy + pf(38), pf(15), Fonts.BOLD, Style.text);
            List<String> lines = Fonts.wrap(info.howTo, Fonts.REGULAR, pf(8.2f), w - p(60), 4);
            float ly = cy + pf(58);
            for (String l : lines) { Gx.textCenter(l, cx, ly, pf(8.2f), Fonts.REGULAR, Style.sub); ly += pf(12.5f); }
            // controls chip
            float cfs = pf(7.4f);
            int cw = Math.round(Gx.width(info.controls, cfs, Fonts.MEDIUM)) + p(34), ch = p(22);
            int ccx = cx - cw / 2, ccy = Math.round(ly + pf(6));
            Gx.rect(ccx, ccy, cw, ch, ch / 2, Style.surface2);
            Gx.icon("keys", ccx + p(14), ccy + ch / 2f, pf(10), Style.sub);
            Gx.textMid(info.controls, ccx + p(25), ccy + ch / 2f, cfs, Fonts.MEDIUM, Style.sub);
            float pulse = 0.55f + 0.45f * (float) Math.sin(System.currentTimeMillis() / 350.0);
            Gx.textSpaced(clickToStart() ? "KLICK ODER LEERTASTE ZUM STARTEN" : "LEERTASTE ZUM STARTEN", cx, ccy + ch + pf(20), pf(7.4f), Fonts.BOLD, Gx.withAlpha(Style.accent, pulse), pf(1.4f));
            if (hasAi()) levels(cx, Math.round(ccy + ch + pf(36)));
        } else if (state == State.PAUSED) {
            int cy = y + h / 2 - p(30);
            Gx.icon("pause", cx, cy, pf(30), Style.text);
            Gx.textCenter("Pause", cx, cy + pf(32), pf(15), Fonts.BOLD, Style.text);
            buttons(cx, cy + p(54), "Weiter", "play", () -> { state = State.PLAYING; stateAt = System.currentTimeMillis(); }, "Neustart", "reset", this::start);
        } else {
            int cy = y + h / 2 - p(52);
            if (record) {
                float s = 1 + 0.08f * (float) Math.sin(since * 6);
                Gx.push();
                Gx.scaleAt(cx, cy, s);
                Gx.glow(cx, cy, p(60), 0x55FFC53D);
                Gx.icon("trophy", cx, cy, pf(34), 0xFFFFC53D);
                Gx.pop();
            } else Gx.icon("skull", cx, cy, pf(30), Style.sub);
            Gx.textCenter(record ? "Neuer Rekord!" : overTitle(), cx, cy + pf(36), pf(15), Fonts.BOLD, record ? 0xFFFFC53D : Style.text);
            Gx.textCenter(scoreLabel() + ": " + scoreText(), cx, cy + pf(56), pf(10), Fonts.MEDIUM, Style.sub);
            buttons(cx, cy + p(78), "Nochmal", "reset", this::start, "Zurück", "back", this::back);
            if (hasAi()) levels(cx, cy + p(120));
        }
        Gx.popAlpha();
    }

    /** KI-Stärke: four chips, centered at cx. */
    private void levels(int cx, int y) {
        String[] l = GameInfo.LEVELS;
        float fs = pf(7.2f);
        int h = p(22), gap = p(5), padX = p(10);
        int[] ws = new int[l.length];
        int total = Math.round(Gx.width("KI:", fs, Fonts.BOLD)) + p(8);
        for (int i = 0; i < l.length; i++) { ws[i] = Math.round(Gx.width(l[i], fs, Fonts.BOLD)) + padX * 2; total += ws[i] + (i > 0 ? gap : 0); }
        int x = cx - total / 2;
        Gx.textMid("KI:", x, y + h / 2f, fs, Fonts.BOLD, Style.sub);
        x += Math.round(Gx.width("KI:", fs, Fonts.BOLD)) + p(8);
        int cur = aiLevel();
        for (int i = 0; i < l.length; i++) {
            boolean sel = i == cur, hv = hover(x, y, ws[i], h);
            Gx.rect(x, y, ws[i], h, h / 2, sel ? Style.accent : hv ? Style.surfaceHover : Style.surface2);
            Gx.textCenter(l[i], x + ws[i] / 2f, y + h / 2f, fs, Fonts.BOLD, sel ? Style.accentText : Style.text);
            final int lv = i;
            Kit.hit(x, y, ws[i], h).click(() -> { GameInfo.setDifficulty(info.id, lv); Sound.click(); });
            x += ws[i] + gap;
        }
    }

    private void buttons(int cx, int y, String l1, String i1, Runnable a1, String l2, String i2, Runnable a2) {
        int bw = p(110), bh = p(30), gap = p(10);
        int x1 = cx - bw - gap / 2, x2 = cx + gap / 2;
        boolean h1 = hover(x1, y, bw, bh), h2 = hover(x2, y, bw, bh);
        Kit.button("g1" + l1, l1, i1, x1, y, bw, bh, P, true, h1);
        Kit.button("g2" + l2, l2, i2, x2, y, bw, bh, P, false, h2);
        Kit.hit(x1, y, bw, bh).click(a1);
        Kit.hit(x2, y, bw, bh).click(a2);
    }

    protected void back() {
        if (host != null) host.open(new HubView(HubView.Section.GAMES));
    }

    // ------------------------------------------------------------------ input
    @Override
    public boolean key(int key, int mods) {
        if (key < 0) { if (state == State.PLAYING) onKeyUp(-1 - key); return true; }
        if (key == K_ESC) {
            if (state == State.PLAYING) { state = State.PAUSED; stateAt = System.currentTimeMillis(); }
            else back();
            return true;
        }
        if (key == K_P && (state == State.PLAYING || state == State.PAUSED)) {
            state = state == State.PLAYING ? State.PAUSED : State.PLAYING;
            stateAt = System.currentTimeMillis();
            return true;
        }
        if (key == K_R && (state == State.OVER || state == State.PAUSED)) { start(); return true; }
        if ((key == K_SPACE || key == K_ENTER) && (state == State.READY || state == State.OVER)) { start(); if (key == K_SPACE && state == State.PLAYING) onKey(key); return true; }
        if (state == State.PLAYING) onKey(key);
        return true;
    }
}
