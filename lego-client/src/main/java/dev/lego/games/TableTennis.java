package dev.lego.games;

import java.util.HashSet;
import java.util.Set;

import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;

/**
 * Tischtennis in 3D perspective: you play at the near end of the table, the AI at the far end. The ball flies in a
 * real arc, bounces on the table and gets spin from where it hits your bat. First to 11 points wins.
 */
public final class TableTennis extends GameView {
    private static final float W = 420, H = 280;
    private static final float NEAR_Y = 250, FAR_Y = 95, NEAR_HW = 170, FAR_HW = 92, CX = 210;
    private final Set<Integer> keys = new HashSet<>();
    private float bx, bz, bh, vx, vz, vh;      // ball: x -1..1 across, z 0 (you) .. 1 (AI), height
    private float px, ax, aiErr;               // bats
    private int me, ai, rally;
    private boolean bounced, serveYou;
    private float wait, flash;
    private String msg = "";

    public TableTennis() { super("tabletennis"); }

    @Override protected boolean hasAi() { return true; }

    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Punkte"; }
    @Override protected String scoreText() { return me + " : " + ai; }
    @Override protected String overTitle() { return me > ai ? "Gewonnen!" : "Verloren"; }

    @Override
    protected void reset() {
        me = 0; ai = 0; px = 0; ax = 0; serveYou = true;
        keys.clear();
        serve();
    }

    private void serve() {
        rally = 0;
        bounced = false;
        wait = 0.8f;
        if (serveYou) { bx = px; bz = 0.03f; vz = 0.62f; vx = (rnd.nextFloat() - 0.5f) * 0.4f; }
        else { bx = ax; bz = 0.97f; vz = -0.62f; vx = (rnd.nextFloat() - 0.5f) * 0.4f; }
        // high serve: it flies over the net and lands on the other half
        bh = 0.12f; vh = 0.95f;
        aiErr = (rnd.nextFloat() - 0.5f) * 0.3f;
    }

    @Override protected void onKey(int k) { keys.add(k); }
    @Override protected void onKeyUp(int k) { keys.remove(k); }

    private void point(boolean you, String why) {
        if (you) me++; else ai++;
        msg = (you ? "Punkt für dich! " : "Punkt für die KI – ") + why;
        flash = 1.4f;
        score = me * 10L + (me > ai ? 5 : 0);
        dev.lego.ui.Sound.play(you ? "minecraft:entity.experience_orb.pickup" : "minecraft:block.note_block.bass", 1.2f, 0.3f);
        if ((me >= 11 || ai >= 11) && Math.abs(me - ai) >= 2) { score = me * 10L + (me > ai ? 100 : 0); gameOver(); return; }
        serveYou = ((me + ai) / 2) % 2 == 0;
        serve();
    }

    @Override
    protected void update(float dt) {
        flash = Math.max(0, flash - dt);
        // your bat: mouse, or A/D
        float target = px;
        if (mx >= 0) target = Math.max(-1.2f, Math.min(1.2f, ((mx - bx0()) / u - CX) / NEAR_HW));
        if (keys.contains(K_A) || keys.contains(K_LEFT)) target = px - 2.2f * dt;
        if (keys.contains(K_D) || keys.contains(K_RIGHT)) target = px + 2.2f * dt;
        px += (target - px) * Math.min(1, dt * 18);
        // AI bat follows the ball with a little delay + error
        float want = vz > 0 ? bx + vx * (1 - bz) / Math.max(0.2f, vz) * 0.6f + aiErr : 0;
        float aiSpeed = (1.15f + Math.min(0.9f, rally * 0.05f)) * new float[]{0.7f, 0.9f, 1.05f, 1.3f}[aiLevel()];
        ax += Math.max(-aiSpeed * dt, Math.min(aiSpeed * dt, want - ax));
        if (wait > 0) { wait -= dt; return; }
        // ball physics
        bx += vx * dt; bz += vz * dt; bh += vh * dt; vh -= 2.4f * dt;
        if (bh <= 0) {
            bh = 0;
            boolean onTable = Math.abs(bx) <= 1 && bz >= 0 && bz <= 1;
            if (!onTable) { point(vz < 0, "Ball im Aus"); return; }
            boolean ownSide = vz > 0 ? bz < 0.5f : bz > 0.5f;
            if (ownSide) { point(vz < 0, "auf der eigenen Seite"); return; }
            if (bounced) { point(vz > 0, "zweimal aufgekommen"); return; }
            bounced = true;
            vh = Math.abs(vh) * 0.72f + 0.12f;
            dev.lego.ui.Sound.play("minecraft:block.wood.hit", 1.8f, 0.25f);
        }
        // net: ball must be high enough when crossing the middle
        if (Math.abs(bz - 0.5f) < 0.012f && bh < 0.05f) { point(vz < 0, "ins Netz"); return; }
        // your end
        if (vz < 0 && bz <= 0.02f) {
            if (bounced && Math.abs(bx - px) < 0.32f) hit(true);
            else { point(false, bounced ? "verfehlt" : "nicht aufgekommen"); return; }
        }
        // AI end
        if (vz > 0 && bz >= 0.98f) {
            if (bounced && Math.abs(bx - ax) < 0.3f && rnd.nextFloat() > 0.04f + rally * 0.006f) hit(false);
            else { point(true, "die KI hat verfehlt"); return; }
        }
    }

    /** Board origin in pixels (the ball field "bx" hides the one from GameView). */
    private float bx0() { return super.bx; }

    private void hit(boolean you) {
        rally++;
        bounced = false;
        float off = you ? (bx - px) : (bx - ax);
        float sp = 0.62f + Math.min(0.45f, rally * 0.03f);
        vz = you ? sp : -sp;
        vx = off * 1.6f + (rnd.nextFloat() - 0.5f) * 0.25f - (you ? 0 : ax * 0.2f);
        bh = Math.max(bh, 0.1f);
        // aim the arc so the ball lands around 70 % into the other half (never on your own side, rarely out)
        float flight = (0.62f + (rnd.nextFloat() - 0.5f) * 0.12f) / sp;
        vh = (1.2f * flight * flight - bh) / flight;
        aiErr = (rnd.nextFloat() - 0.5f) * (0.25f + Math.min(0.3f, rally * 0.02f)) * new float[]{1.7f, 1.2f, 0.85f, 0.5f}[aiLevel()];
        dev.lego.ui.Sound.play("minecraft:block.bamboo_wood.hit", you ? 1.5f : 1.3f, 0.35f);
    }

    // ------------------------------------------------------------------ render
    private float ty(float z) { return NEAR_Y + (FAR_Y - NEAR_Y) * z; }
    private float hw(float z) { return NEAR_HW + (FAR_HW - NEAR_HW) * z; }

    @Override
    protected void render() {
        boxV(0, 0, W, H, 10, 0xFF1A2236, 0xFF0D121D);
        // floor + table (drawn as thin perspective slices)
        for (int i = 0; i < 40; i++) {
            float z0 = i / 40f, z1 = (i + 1) / 40f;
            float y0 = ty(z0), y1 = ty(z1);
            float w = hw(z0);
            box(CX - w, y1, w * 2, y0 - y1 + 0.6f, 0, i % 2 == 0 ? 0xFF1F6FB8 : 0xFF1C67AD);
            box(CX - w, y1, 2, y0 - y1 + 0.6f, 0, 0xFFF4F4F4);
            box(CX + w - 2, y1, 2, y0 - y1 + 0.6f, 0, 0xFFF4F4F4);
        }
        box(CX - 1, FAR_Y, 2, NEAR_Y - FAR_Y, 0, 0xAAF4F4F4);
        box(CX - NEAR_HW, NEAR_Y - 1, NEAR_HW * 2, 3, 0, 0xFFF4F4F4);
        box(CX - FAR_HW, FAR_Y - 1, FAR_HW * 2, 2, 0, 0xFFF4F4F4);
        box(CX - NEAR_HW, NEAR_Y + 2, NEAR_HW * 2, 8, 2, 0xFF123E6B);
        // net
        float ny = ty(0.5f), nw = hw(0.5f);
        box(CX - nw - 6, ny - 14, nw * 2 + 12, 14, 1, 0x66FFFFFF);
        box(CX - nw - 6, ny - 15, nw * 2 + 12, 2, 1, 0xFFF4F4F4);
        // AI bat (far)
        drawBat(CX + ax * hw(1), FAR_Y - 10, 0.6f, 0xFF1E1E22);
        // ball + shadow
        float sy = ty(bz), sx = CX + bx * hw(bz), sc = 0.6f + 0.4f * (1 - bz);
        circle(sx, sy, 4 * sc, 0x55000000);
        circle(sx, sy - bh * 120 * sc, 4.2f * sc, 0xFFFFF4E0);
        circle(sx - 1, sy - bh * 120 * sc - 1, 1.6f * sc, 0xFFFFFFFF);
        // your bat (near)
        drawBat(CX + px * hw(0), NEAR_Y + 12, 1.0f, 0xFFE3000B);
        text(me + " : " + ai, CX, 18, 13, Fonts.BOLD, 0xFFFFFFFF);
        text("Rally " + rally, CX, 34, 6.5f, Fonts.MEDIUM, 0x99FFFFFF);
        if (flash > 0) text(msg, CX, H / 2f - 30, 8, Fonts.BOLD, Gx.withAlpha(0xFFFFCD03, Math.min(1, flash)));
        if (wait > 0 && state == State.PLAYING) text(serveYou ? "Dein Aufschlag" : "Aufschlag KI", CX, NEAR_Y - 40, 7, Fonts.BOLD, 0xCCFFFFFF);
    }

    private void drawBat(float x, float y, float s, int col) {
        box(x - 2.5f * s, y + 6 * s, 5 * s, 12 * s, 2 * s, 0xFF8A5A32);
        circle(x, y, 13 * s, Gx.mix(col, 0xFF000000, 0.25f));
        circle(x, y - 1 * s, 12 * s, col);
    }
}