package dev.lego.games;

import dev.lego.core.Mc;
import dev.lego.net.LegoSync;
import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.Style;
import dev.lego.ui.View;
import dev.lego.ui.hub.HubView;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.client.player.AbstractClientPlayer;

public final class DuelView extends View {
   private float scroll = 0.0F;
   private float target = 0.0F;
   private float max = 0.0F;

   @Override
   public boolean blur() {
      return false;
   }

   @Override
   public float designW() {
      return 600.0F;
   }

   @Override
   public float designH() {
      return 400.0F;
   }

   @Override
   public float openMs() {
      return 320.0F;
   }

   @Override
   public void draw() {
      float t = Ease.outQuint(this.open);
      Gx.fill(0, 0, this.W, this.H, Gx.rgba(0, 0.35F * this.open));
      int pw = this.p(600.0);
      int ph = this.p(400.0);
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
      Kit.hit(px + this.p(14.0), tb, bs, bs).click(this::back);
      Gx.text("Duelle", px + this.p(54.0), tb + this.pf(1.0), this.pf(14.0), 3, Style.text);
      boolean on = LegoSync.active();
      boolean conn = LegoSync.connected();
      String st = !on
         ? "Lego-Sync ist aus – in den Mods einschalten"
         : (conn ? "Fordere andere Lego-Client-Spieler heraus · live verbunden" : "Verbinde mit Lego-Sync…");
      Gx.text(st, px + this.p(54.0), tb + this.pf(19.0), this.pf(7.4F), 1, on && conn ? Style.sub : Style.danger);
      int cx = px + this.p(16.0);
      int cw = pw - this.p(32.0);
      int y = tb + this.p(46.0);

      for (Duels.Invite i : Duels.invites()) {
         int h = this.p(44.0);
         Gx.rect(cx, y, cw, h, this.p(12.0), Gx.withAlpha(-15043, 0.12F));
         Gx.outline(cx, y, cw, h, this.p(12.0), Math.max(1, this.p(0.7)), Gx.withAlpha(-15043, 0.6F));
         Gx.icon("gamepad", cx + this.p(22.0), y + h / 2.0F, this.pf(14.0), -15043);
         Gx.textMid(
            Fonts.ellipsize(i.name() + " fordert dich heraus: " + Duels.GAMES.get(i.game()), 3, this.pf(9.0), cw - this.p(260.0)),
            cx + this.p(40.0),
            y + h / 2.0F,
            this.pf(9.0),
            3,
            Style.text
         );
         int bw = this.p(96.0);
         int bh = this.p(28.0);
         int by = y + (h - bh) / 2;
         int ax = cx + cw - bw * 2 - this.p(18.0);
         int dx = cx + cw - bw - this.p(10.0);
         Kit.button("acc" + i.match(), "Annehmen", "check", ax, by, bw, bh, this.P, true, this.hover(ax, by, bw, bh));
         Kit.button("dec" + i.match(), "Ablehnen", "x", dx, by, bw, bh, this.P, false, this.hover(dx, by, bw, bh));
         Kit.hit(ax, by, bw, bh).click(() -> Duels.accept(i));
         Kit.hit(dx, by, bw, bh).click(() -> Duels.decline(i));
         y += h + this.p(8.0);
      }

      Gx.textSpaced(
         "LEGO-SPIELER ONLINE",
         cx + Gx.spacedWidth("LEGO-SPIELER ONLINE", this.pf(6.6F), 3, this.pf(1.2F)) / 2.0F,
         y + this.pf(8.0),
         this.pf(6.6F),
         3,
         Style.muted,
         this.pf(1.2F)
      );
      y += this.p(18.0);
      List<UUID> players = LegoSync.players();
      Set<String> here = new HashSet<>();
      if (Mc.world() != null) {
         for (AbstractClientPlayer pl : Mc.world().players()) {
            here.add(pl.getUUID().toString());
         }
      }

      players.sort((a, b) -> Boolean.compare(!here.contains(a.toString()), !here.contains(b.toString())));
      int ly = y;
      int lh = py + ph - this.p(14.0) - y;
      int rh = this.p(74.0);
      int gap = this.p(8.0);
      this.max = Math.max(0, players.size() * (rh + gap) - gap - lh);
      this.target = Math.max(0.0F, Math.min(this.max, this.target));
      this.scroll = this.scroll + (this.target - this.scroll) * (1.0F - (float)Math.exp(-Gx.dt * 16.0F));
      Gx.clip(cx - this.p(4.0), y, cw + this.p(8.0), lh);
      Kit.clip(cx - this.p(4.0), y, cw + this.p(8.0), lh);
      Kit.hit(cx, y, cw, lh).scroll(a -> this.target = this.target - a.floatValue() * this.p(60.0));
      if (players.isEmpty()) {
         Gx.icon("users", px + pw / 2.0F, y + this.p(50.0), this.pf(26.0), Style.muted);
         Gx.textCenter(
            on ? "Gerade ist kein anderer Lego-Spieler online" : "Lego-Sync ist ausgeschaltet", px + pw / 2.0F, y + this.pf(80.0), this.pf(9.0), 2, Style.sub
         );
         Gx.textCenter("Sobald jemand mit dem Lego Client spielt, erscheint er hier", px + pw / 2.0F, y + this.pf(96.0), this.pf(7.4F), 1, Style.muted);
      }

      for (int k = 0; k < players.size(); k++) {
         UUID id = players.get(k);
         int ry = Math.round(ly + k * (rh + gap) - this.scroll);
         if (ry + rh >= ly && ry <= ly + lh) {
            boolean near = here.contains(id.toString());
            Gx.rect(cx, ry, cw, rh, this.p(12.0), Style.surface);
            String name = LegoSync.name(id);
            Gx.circle(cx + this.p(22.0), ry + this.p(18.0), this.p(12.0), Gx.withAlpha(Style.accent, 0.18F));
            Gx.icon("user", cx + this.p(22.0), ry + this.pf(18.0), this.pf(12.0), Style.accent);
            Gx.text(
               Fonts.ellipsize(name == null ? "Lego-Spieler" : name, 3, this.pf(9.5), cw - this.p(60.0)),
               cx + this.p(42.0),
               ry + this.pf(9.0),
               this.pf(9.5),
               3,
               Style.text
            );
            Gx.text(near ? "In deiner Welt" : "Online (anderer Server)", cx + this.p(42.0), ry + this.pf(25.0), this.pf(6.8F), 1, near ? Style.ok : Style.sub);
            boolean pending = Duels.invited(id);
            int bh = this.p(24.0);
            int by = ry + this.p(40.0);
            int gn = Duels.GAMES.size();
            int bgap = this.p(6.0);
            int bw = (cw - this.p(20.0) - bgap * (gn - 1)) / gn;
            int bx = cx + this.p(10.0);

            for (Entry<String, String> g : Duels.GAMES.entrySet()) {
               String game = g.getKey();
               Kit.button("duel" + id + game, pending ? "..." : g.getValue(), null, bx, by, bw, bh, this.P, !pending, !pending && this.hover(bx, by, bw, bh));
               if (!pending) {
                  Kit.hit(bx, by, bw, bh).click(() -> Duels.invite(id, game));
               }

               bx += bw + bgap;
            }
         }
      }

      Kit.unclip();
      Gx.unclip();
      Gx.pop();
      Gx.popAlpha();
   }

   private void back() {
      if (this.host != null) {
         this.host.open(new HubView(HubView.Section.GAMES));
      }
   }

   @Override
   public boolean key(int key, int mods) {
      if (key == 256) {
         this.back();
         return true;
      } else {
         return false;
      }
   }
}
