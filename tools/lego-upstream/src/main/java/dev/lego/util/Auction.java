package dev.lego.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.LegoScreen;
import dev.lego.ui.Style;
import dev.lego.ui.View;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_746;

/**
 * Streamer auction tool: scan held item, set a timer, track incoming payments from chat.
 * Payment format: [ServerName] Du hast $AMOUNT von PLAYER erhalten.
 */
public final class Auction extends Module {

    final Setting.Text pattern = add(new Setting.Text("Chat-Muster", "Du hast \\$([\\d.,]+) von (\\w+) erhalten", 200, "Regex"));
    final Setting.Num minBid = add(new Setting.Num("Mindestgebot", 1000, 100_000_000, 1000, 100_000, "$"));
    final Setting.Action open = add(new Setting.Action("Auktion starten", "Starten", () -> LegoScreen.show(new AuctionView())));

    private boolean running;
    private long startTime, durationMs;
    private String itemName = "";
    private final List<Bid> bids = new ArrayList<>();
    private Pattern compiled;
    private long lastCompile;

    public Auction() {
        super("auction", "Auktion", "Streamer-Auktions-Tool: Gebote aus dem Chat sammeln", Category.UTILITY);
        icon("star");
    }

    private Pattern pat() {
        long now = System.currentTimeMillis();
        if (compiled == null || now - lastCompile > 5000) {
            try { compiled = Pattern.compile(pattern.get()); } catch (Throwable t) { compiled = null; }
            lastCompile = now;
        }
        return compiled;
    }

    public void onChat(String msg) {
        if (!enabled || !running) return;
        Pattern p = pat();
        if (p == null) return;
        Matcher m = p.matcher(msg);
        if (!m.find() || m.groupCount() < 2) return;
        try {
            String amountStr = m.group(1).replace(".", "").replace(",", ".");
            double amount = Double.parseDouble(amountStr);
            String player = m.group(2);
            if (amount < minBid.get()) return;
            bids.add(new Bid(player, amount, System.currentTimeMillis()));
            if (bids.size() > 500) bids.remove(0);
        } catch (Throwable ignored) { }
    }

    public void startAuction(String item, int seconds) {
        running = true;
        startTime = System.currentTimeMillis();
        durationMs = seconds * 1000L;
        itemName = item;
        bids.clear();
    }

    public void stopAuction() { running = false; }

    public boolean isRunning() { return running; }

    public long remaining() {
        if (!running) return 0;
        long left = durationMs - (System.currentTimeMillis() - startTime);
        return Math.max(0, left);
    }

    public List<Bid> topBids(int n) {
        List<Bid> sorted = new ArrayList<>(bids);
        sorted.sort((a, b) -> Double.compare(b.amount, a.amount));
        if (sorted.size() > n) sorted = sorted.subList(0, n);
        return sorted;
    }

    public void renderHud(int W, int H) {
        if (!enabled || !running) return;
        long rem = remaining();
        if (rem <= 0 && System.currentTimeMillis() - startTime > durationMs + 3000) { running = false; return; }
        int S = Gx.S;
        int pw = 260 * S, ph = 180 * S;
        int px = W - pw - 16 * S, py = 16 * S;
        Gx.rect(px, py, pw, ph, 14 * S, Style.bg);
        Gx.outline(px, py, pw, ph, 14 * S, Math.max(1, S), Style.stroke);
        Gx.text("Auktion", px + 14 * S, py + 10 * S, 14f * S, Fonts.BOLD, Style.text);
        Gx.text(itemName, px + 14 * S, py + 28 * S, 9f * S, Fonts.REGULAR, Style.accent);
        int sec = (int)(rem / 1000), min = sec / 60;
        String timer = rem <= 0 ? "BEENDET" : String.format("%d:%02d", min, sec % 60);
        int timerCol = rem <= 0 ? 0xFFFF4444 : rem < 10000 ? 0xFFFFCC00 : Style.text;
        Gx.text(timer, px + pw - 14 * S - Gx.width(timer, 16f * S, Fonts.BOLD), py + 10 * S, 16f * S, Fonts.BOLD, timerCol);

        int y = py + 46 * S;
        Gx.fill(px + 10 * S, y, pw - 20 * S, Math.max(1, S / 2), Style.stroke);
        y += 6 * S;
        List<Bid> top = topBids(5);
        if (top.isEmpty()) {
            Gx.textCenter("Noch keine Gebote", px + pw / 2f, y + 20f * S, 9f * S, Fonts.MEDIUM, Style.muted);
        }
        for (int i = 0; i < top.size(); i++) {
            Bid b = top.get(i);
            int c = i == 0 ? 0xFFFFD700 : i == 1 ? 0xFFC0C0C0 : i == 2 ? 0xFFCD7F32 : Style.text;
            Gx.text((i + 1) + ".", px + 14 * S, y + 1f * S, 9f * S, Fonts.BOLD, c);
            Gx.text(b.player, px + 30 * S, y + 1f * S, 9f * S, Fonts.MEDIUM, Style.text);
            String amt = formatAmount(b.amount);
            Gx.text(amt, px + pw - 14 * S - Gx.width(amt, 9f * S, Fonts.BOLD), y + 1f * S, 9f * S, Fonts.BOLD, Style.accent);
            y += 18 * S;
        }
        Gx.text(bids.size() + " Gebot" + (bids.size() == 1 ? "" : "e"), px + 14 * S, py + ph - 18 * S, 7.5f * S, Fonts.REGULAR, Style.sub);
    }

    private static String formatAmount(double v) {
        if (v >= 1_000_000) return String.format("$%.1fM", v / 1_000_000);
        if (v >= 1000) return String.format("$%.0fk", v / 1000);
        return String.format("$%.0f", v);
    }

    public static final class Bid {
        public final String player;
        public final double amount;
        public final long time;
        Bid(String player, double amount, long time) { this.player = player; this.amount = amount; this.time = time; }
    }

    // ================================================================== auction setup view
    public final class AuctionView extends View {
        private int timerSec = 120;
        private String scannedItem = "";

        @Override public float designW() { return 480; }
        @Override public float designH() { return 340; }

        @Override
        public void draw() {
            float t = Ease.outQuint(open);
            Gx.fill(0, 0, W, H, Gx.rgba(0x000000, 0.35f * open));
            int pw = p(480), ph = p(340), px = (W - pw) / 2, py = (H - ph) / 2;
            Gx.pushAlpha(t);
            Gx.push();
            Gx.scaleAt(W / 2f, H / 2f, 0.94f + 0.06f * t);
            Gx.shadow(px, py, pw, ph, p(18), p(14), 0.55f);
            Gx.rect(px, py, pw, ph, p(18), Style.bg);
            Gx.outline(px, py, pw, ph, p(18), Math.max(1, p(0.6)), Style.stroke);

            int tb = py + p(14), bs = p(30);
            boolean hb = hover(px + p(14), tb, bs, bs);
            Gx.rect(px + p(14), tb, bs, bs, p(9), hb ? Style.surfaceHover : Style.surface);
            Gx.icon("back", px + p(14) + bs / 2f - pf(1), tb + bs / 2f, pf(14), Style.text);
            Kit.hit(px + p(14), tb, bs, bs).click(() -> { if (host != null) host.close(); });
            Gx.text("Auktion", px + p(54), tb + pf(1), pf(14), Fonts.BOLD, Style.text);
            Gx.text("Streamer-Auktions-Tool", px + p(54), tb + pf(19), pf(7.4f), Fonts.REGULAR, Style.sub);

            int cx = px + p(16), cw = pw - p(32);
            int y = tb + p(50);

            Gx.text("Item (in der Hand):", cx, y, pf(9), Fonts.MEDIUM, Style.text);
            y += p(16);
            class_746 me = Mc.player();
            if (me != null) {
                class_1799 held = me.method_6047();
                if (!Mc.empty(held)) scannedItem = held.method_7964().getString();
            }
            Gx.rect(cx, y, cw, p(32), p(10), Style.surface);
            Gx.text(scannedItem.isEmpty() ? "Halte ein Item in der Hand" : scannedItem, cx + p(10), y + pf(8), pf(10), Fonts.REGULAR, scannedItem.isEmpty() ? Style.muted : Style.text);
            y += p(42);

            Gx.text("Timer:", cx, y, pf(9), Fonts.MEDIUM, Style.text);
            y += p(16);
            int bw = p(50), bh = p(30), gap = p(8);
            String[] times = {"30s", "1m", "2m", "5m", "10m"};
            int[] secs = {30, 60, 120, 300, 600};
            for (int i = 0; i < times.length; i++) {
                int bx = cx + i * (bw + gap);
                boolean sel = timerSec == secs[i];
                boolean bho = hover(bx, y, bw, bh);
                Gx.rect(bx, y, bw, bh, p(10), sel ? Gx.withAlpha(Style.accent, 0.25f) : bho ? Style.surfaceHover : Style.surface);
                Gx.outline(bx, y, bw, bh, p(10), Math.max(1, p(0.5)), sel ? Style.accent : Style.stroke);
                Gx.textCenter(times[i], bx + bw / 2f, y + bh / 2f, pf(9), sel ? Fonts.BOLD : Fonts.MEDIUM, sel ? Style.accent : Style.text);
                final int s = secs[i];
                Kit.hit(bx, y, bw, bh).click(() -> timerSec = s);
            }
            y += bh + p(20);

            if (running) {
                int sbw = p(140), sbh = p(36), sbx = cx + (cw - sbw) / 2;
                boolean sh = hover(sbx, y, sbw, sbh);
                Gx.rect(sbx, y, sbw, sbh, p(12), sh ? 0x60FF3333 : 0x40FF3333);
                Gx.textCenter("Auktion stoppen", sbx + sbw / 2f, y + sbh / 2f, pf(10), Fonts.BOLD, 0xFFFF4444);
                Kit.hit(sbx, y, sbw, sbh).click(() -> { stopAuction(); if (host != null) host.close(); });
            } else {
                int sbw = p(140), sbh = p(36), sbx = cx + (cw - sbw) / 2;
                boolean sh = hover(sbx, y, sbw, sbh);
                Kit.button("start", "Auktion starten", "star", sbx, y, sbw, sbh, P, !scannedItem.isEmpty(), sh);
                Kit.hit(sbx, y, sbw, sbh).click(() -> {
                    if (!scannedItem.isEmpty()) {
                        startAuction(scannedItem, timerSec);
                        if (host != null) host.close();
                    }
                });
            }
            Gx.pop();
            Gx.popAlpha();
        }

        @Override public boolean key(int key, int mods) {
            if (key == 256) { if (host != null) host.close(); return true; }
            return false;
        }
    }
}
