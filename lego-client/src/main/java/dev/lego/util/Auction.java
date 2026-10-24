package dev.lego.util;

import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Setting;
import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.LegoScreen;
import dev.lego.ui.Style;
import dev.lego.ui.View;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

public final class Auction extends Module {
   final Setting.Text pattern = this.add(new Setting.Text("Chat-Muster", "Du hast \\$([\\d.,]+) von (\\w+) erhalten", 200, "Regex"));
   final Setting.Num minBid = this.add(new Setting.Num("Mindestgebot", 1000.0, 1.0E8, 1000.0, 100000.0, "$"));
   final Setting.Action open = this.add(new Setting.Action("Auktion starten", "Starten", () -> LegoScreen.show(new Auction.AuctionView())));
   private boolean running;
   private long startTime;
   private long durationMs;
   private String itemName = "";
   private final List<Auction.Bid> bids = new ArrayList<>();
   private Pattern compiled;
   private long lastCompile;

   public Auction() {
      super("auction", "Auktion", "Streamer-Auktions-Tool: Gebote aus dem Chat sammeln", Category.UTILITY);
      this.icon("star");
   }

   private Pattern pat() {
      long now = System.currentTimeMillis();
      if (this.compiled == null || now - this.lastCompile > 5000L) {
         try {
            this.compiled = Pattern.compile(this.pattern.get());
         } catch (Throwable var4) {
            this.compiled = null;
         }

         this.lastCompile = now;
      }

      return this.compiled;
   }

   public void onChat(String msg) {
      if (this.enabled && this.running) {
         Pattern p = this.pat();
         if (p != null) {
            Matcher m = p.matcher(msg);
            if (m.find() && m.groupCount() >= 2) {
               try {
                  String amountStr = m.group(1).replace(".", "").replace(",", ".");
                  double amount = Double.parseDouble(amountStr);
                  String player = m.group(2);
                  if (amount < this.minBid.get()) {
                     return;
                  }

                  this.bids.add(new Auction.Bid(player, amount, System.currentTimeMillis()));
                  if (this.bids.size() > 500) {
                     this.bids.remove(0);
                  }
               } catch (Throwable var8) {
               }
            }
         }
      }
   }

   public void startAuction(String item, int seconds) {
      this.running = true;
      this.startTime = System.currentTimeMillis();
      this.durationMs = seconds * 1000L;
      this.itemName = item;
      this.bids.clear();
   }

   public void stopAuction() {
      this.running = false;
   }

   public boolean isRunning() {
      return this.running;
   }

   public long remaining() {
      if (!this.running) {
         return 0L;
      } else {
         long left = this.durationMs - (System.currentTimeMillis() - this.startTime);
         return Math.max(0L, left);
      }
   }

   public List<Auction.Bid> topBids(int n) {
      List<Auction.Bid> sorted = new ArrayList<>(this.bids);
      sorted.sort((a, b) -> Double.compare(b.amount, a.amount));
      if (sorted.size() > n) {
         sorted = sorted.subList(0, n);
      }

      return sorted;
   }

   public void renderHud(int W, int H) {
      if (this.enabled && this.running) {
         long rem = this.remaining();
         if (rem <= 0L && System.currentTimeMillis() - this.startTime > this.durationMs + 3000L) {
            this.running = false;
         } else {
            int S = Gx.S;
            int pw = 260 * S;
            int ph = 180 * S;
            int px = W - pw - 16 * S;
            int py = 16 * S;
            Gx.rect(px, py, pw, ph, 14 * S, Style.bg);
            Gx.outline(px, py, pw, ph, 14 * S, Math.max(1, S), Style.stroke);
            Gx.text("Auktion", px + 14 * S, py + 10 * S, 14.0F * S, 3, Style.text);
            Gx.text(this.itemName, px + 14 * S, py + 28 * S, 9.0F * S, 1, Style.accent);
            int sec = (int)(rem / 1000L);
            int min = sec / 60;
            String timer = rem <= 0L ? "BEENDET" : String.format("%d:%02d", min, sec % 60);
            int timerCol = rem <= 0L ? -48060 : (rem < 10000L ? -13312 : Style.text);
            Gx.text(timer, px + pw - 14 * S - Gx.width(timer, 16.0F * S, 3), py + 10 * S, 16.0F * S, 3, timerCol);
            int y = py + 46 * S;
            Gx.fill(px + 10 * S, y, pw - 20 * S, Math.max(1, S / 2), Style.stroke);
            y += 6 * S;
            List<Auction.Bid> top = this.topBids(5);
            if (top.isEmpty()) {
               Gx.textCenter("Noch keine Gebote", px + pw / 2.0F, y + 20.0F * S, 9.0F * S, 2, Style.muted);
            }

            for (int i = 0; i < top.size(); i++) {
               Auction.Bid b = top.get(i);
               int c = i == 0 ? -10496 : (i == 1 ? -4144960 : (i == 2 ? -3309774 : Style.text));
               Gx.text(i + 1 + ".", px + 14 * S, y + 1.0F * S, 9.0F * S, 3, c);
               Gx.text(b.player, px + 30 * S, y + 1.0F * S, 9.0F * S, 2, Style.text);
               String amt = formatAmount(b.amount);
               Gx.text(amt, px + pw - 14 * S - Gx.width(amt, 9.0F * S, 3), y + 1.0F * S, 9.0F * S, 3, Style.accent);
               y += 18 * S;
            }

            Gx.text(this.bids.size() + " Gebot" + (this.bids.size() == 1 ? "" : "e"), px + 14 * S, py + ph - 18 * S, 7.5F * S, 1, Style.sub);
         }
      }
   }

   private static String formatAmount(double v) {
      if (v >= 1000000.0) {
         return String.format("$%.1fM", v / 1000000.0);
      } else {
         return v >= 1000.0 ? String.format("$%.0fk", v / 1000.0) : String.format("$%.0f", v);
      }
   }

   public final class AuctionView extends View {
      private int timerSec = 120;
      private String scannedItem = "";

      @Override
      public float designW() {
         return 480.0F;
      }

      @Override
      public float designH() {
         return 340.0F;
      }

      @Override
      public void draw() {
         float t = Ease.outQuint(this.open);
         Gx.fill(0, 0, this.W, this.H, Gx.rgba(0, 0.35F * this.open));
         int pw = this.p(480.0);
         int ph = this.p(340.0);
         int px = (this.W - pw) / 2;
         int py = (this.H - ph) / 2;
         Gx.pushAlpha(t);
         Gx.push();
         Gx.scaleAt(this.W / 2.0F, this.H / 2.0F, 0.94F + 0.06F * t);
         Gx.shadow(px, py, pw, ph, this.p(18.0), this.p(14.0), 0.55F);
         Gx.rect(px, py, pw, ph, this.p(18.0), Style.bg);
         Gx.outline(px, py, pw, ph, this.p(18.0), Math.max(1, this.p(0.6)), Style.stroke);
         int tb = py + this.p(14.0);
         int bs = this.p(30.0);
         boolean hb = this.hover(px + this.p(14.0), tb, bs, bs);
         Gx.rect(px + this.p(14.0), tb, bs, bs, this.p(9.0), hb ? Style.surfaceHover : Style.surface);
         Gx.icon("back", px + this.p(14.0) + bs / 2.0F - this.pf(1.0), tb + bs / 2.0F, this.pf(14.0), Style.text);
         Kit.hit(px + this.p(14.0), tb, bs, bs).click(() -> {
            if (this.host != null) {
               this.host.close();
            }
         });
         Gx.text("Auktion", px + this.p(54.0), tb + this.pf(1.0), this.pf(14.0), 3, Style.text);
         Gx.text("Streamer-Auktions-Tool", px + this.p(54.0), tb + this.pf(19.0), this.pf(7.4F), 1, Style.sub);
         int cx = px + this.p(16.0);
         int cw = pw - this.p(32.0);
         int y = tb + this.p(50.0);
         Gx.text("Item (in der Hand):", cx, y, this.pf(9.0), 2, Style.text);
         y += this.p(16.0);
         LocalPlayer me = Mc.player();
         if (me != null) {
            ItemStack held = me.getMainHandItem();
            if (!Mc.empty(held)) {
               this.scannedItem = held.getHoverName().getString();
            }
         }

         Gx.rect(cx, y, cw, this.p(32.0), this.p(10.0), Style.surface);
         Gx.text(
            this.scannedItem.isEmpty() ? "Halte ein Item in der Hand" : this.scannedItem,
            cx + this.p(10.0),
            y + this.pf(8.0),
            this.pf(10.0),
            1,
            this.scannedItem.isEmpty() ? Style.muted : Style.text
         );
         y += this.p(42.0);
         Gx.text("Timer:", cx, y, this.pf(9.0), 2, Style.text);
         y += this.p(16.0);
         int bw = this.p(50.0);
         int bh = this.p(30.0);
         int gap = this.p(8.0);
         String[] times = new String[]{"30s", "1m", "2m", "5m", "10m"};
         int[] secs = new int[]{30, 60, 120, 300, 600};

         for (int i = 0; i < times.length; i++) {
            int bx = cx + i * (bw + gap);
            boolean sel = this.timerSec == secs[i];
            boolean bho = this.hover(bx, y, bw, bh);
            Gx.rect(bx, y, bw, bh, this.p(10.0), sel ? Gx.withAlpha(Style.accent, 0.25F) : (bho ? Style.surfaceHover : Style.surface));
            Gx.outline(bx, y, bw, bh, this.p(10.0), Math.max(1, this.p(0.5)), sel ? Style.accent : Style.stroke);
            Gx.textCenter(times[i], bx + bw / 2.0F, y + bh / 2.0F, this.pf(9.0), sel ? 3 : 2, sel ? Style.accent : Style.text);
            int s = secs[i];
            Kit.hit(bx, y, bw, bh).click(() -> this.timerSec = s);
         }

         y += bh + this.p(20.0);
         if (Auction.this.running) {
            int sbw = this.p(140.0);
            int sbh = this.p(36.0);
            int sbx = cx + (cw - sbw) / 2;
            boolean sh = this.hover(sbx, y, sbw, sbh);
            Gx.rect(sbx, y, sbw, sbh, this.p(12.0), sh ? 1627337523 : 1090466611);
            Gx.textCenter("Auktion stoppen", sbx + sbw / 2.0F, y + sbh / 2.0F, this.pf(10.0), 3, -48060);
            Kit.hit(sbx, y, sbw, sbh).click(() -> {
               Auction.this.stopAuction();
               if (this.host != null) {
                  this.host.close();
               }
            });
         } else {
            int sbw = this.p(140.0);
            int sbh = this.p(36.0);
            int sbx = cx + (cw - sbw) / 2;
            boolean sh = this.hover(sbx, y, sbw, sbh);
            Kit.button("start", "Auktion starten", "star", sbx, y, sbw, sbh, this.P, !this.scannedItem.isEmpty(), sh);
            Kit.hit(sbx, y, sbw, sbh).click(() -> {
               if (!this.scannedItem.isEmpty()) {
                  Auction.this.startAuction(this.scannedItem, this.timerSec);
                  if (this.host != null) {
                     this.host.close();
                  }
               }
            });
         }

         Gx.pop();
         Gx.popAlpha();
      }

      @Override
      public boolean key(int key, int mods) {
         if (key == 256) {
            if (this.host != null) {
               this.host.close();
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public static final class Bid {
      public final String player;
      public final double amount;
      public final long time;

      Bid(String player, double amount, long time) {
         this.player = player;
         this.amount = amount;
         this.time = time;
      }
   }
}
