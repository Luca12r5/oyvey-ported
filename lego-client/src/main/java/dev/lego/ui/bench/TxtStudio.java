package dev.lego.ui.bench;

import de.bauloo.styles.BaulooStylesClient;
import de.bauloo.styles.BaulooStylesClient.Tab;
import de.bauloo.styles.BaulooStylesConfigScreen;
import de.bauloo.styles.Cosmetics;
import de.bauloo.styles.MyPacks;
import de.bauloo.styles.StylesCloud;
import de.bauloo.styles.StylesPresets;
import de.bauloo.styles.StylesUploadManager;
import de.bauloo.styles.VipPacks;
import dev.lego.core.Module;
import dev.lego.txt.TxtExtras;
import dev.lego.ui.Ease;
import dev.lego.ui.Env;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.McBackend;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.Identifier;

/**
 * Custom TxT in the work bench: the LEGO menu for everything the Bauloo menu offers. Styles for
 * every group (built in, cloud, uploads, VIP and the parts of your own resource packs, each in
 * the group it belongs to), capes and cosmetics with a live preview, the extras and presets.
 * Every pick only goes into the bench's plan; ANWENDEN loads it all at once.
 */
final class TxtStudio {
   private static final String[] TABS = {"STYLES", "CAPES", "EXTRAS", "PRESETS"};
   private static final String[] TAB_NAMES = {"Styles", "Capes & Kosmetik", "Extras", "Presets"};
   private static final String[] TAB_ICONS = {"palette", "shirt", "sparkle", "bookmark"};
   private static final Cosmetics.Slot[] SLOTS = {Cosmetics.Slot.CAPE, Cosmetics.Slot.MC_CAPE, Cosmetics.Slot.HEAD, Cosmetics.Slot.AURA};
   private static final String[] SLOT_NAMES = {"Capes", "Minecraft-Capes", "Köpfe", "Auren"};

   static String tab = "STYLES";
   static Tab group = Tab.CRYSTAL;
   static Cosmetics.Slot slot = Cosmetics.Slot.CAPE;

   private final BenchView v;
   private float scroll;
   private float target;
   private float max;
   private float yaw = 20.0F;
   private String cacheKey = "";
   private List<Entry> cache = List.of();
   private boolean scanned;

   TxtStudio(BenchView v) {
      this.v = v;
   }

   record Entry(String folder, String name, String kind, Tab tab) {
   }

   private int p(double u) {
      return this.v.p(u);
   }

   private float pf(double u) {
      return this.v.pf(u);
   }

   void draw(int x, int y, int w, int h) {
      if (!this.scanned) {
         this.scanned = true;
         try {
            MyPacks.ensureScanned();
         } catch (Throwable e) {
            dev.lego.LegoClient.LOG("TxT: Packs scannen: " + e);
         }
      }
      // sub tabs
      int th = this.p(26.0);
      int tx = x;
      float fs = this.pf(7.4F);
      for (int i = 0; i < TABS.length; i++) {
         String id = TABS[i];
         int tw = Math.round(Gx.width(TAB_NAMES[i], fs, 2)) + this.p(36.0);
         boolean act = id.equals(tab);
         boolean hv = this.v.hover(tx, y, tw, th);
         float f = Ease.to("txt:tab" + i, act ? 1.0F : (hv ? 0.4F : 0.0F), 16.0F);
         Gx.rect(tx, y, tw, th, th / 2, act ? Gx.withAlpha(-1, 0.10F) : Gx.withAlpha(-1, 0.04F * f));
         int fg = act ? BenchView.TEXT : Gx.mix(BenchView.SUB, BenchView.TEXT, f);
         Gx.icon(TAB_ICONS[i], tx + this.p(15.0), y + th / 2.0F, this.pf(10.0), act ? Style.accent : fg);
         Gx.textMid(TAB_NAMES[i], tx + this.p(26.0), y + th / 2.0F, fs, 2, fg);
         Kit.hit(tx, y, tw, th).click(() -> {
            tab = id;
            this.target = 0.0F;
            this.v.inspect = null;
         });
         tx += tw + this.p(5.0);
      }
      int packs = 0;
      for (MyPacks.Style s : MyPacks.styles()) {
         packs++;
      }
      String info = MyPacks.scanning() ? "Deine Packs werden gelesen… " + MyPacks.scanned() + "/" + MyPacks.total() : packs + " Teile aus " + MyPacks.packCount() + " eigenen Packs";
      Gx.textRight(info, x + w - this.p(4.0), y + th / 2.0F, this.pf(6.6F), 1, BenchView.MUTED);
      int by = y + th + this.p(10.0);
      int bh = h - th - this.p(10.0);
      switch (tab) {
         case "CAPES" -> this.capes(x, by, w, bh);
         case "EXTRAS" -> this.extras(x, by, w, bh);
         case "PRESETS" -> this.presets(x, by, w, bh);
         default -> this.styles(x, by, w, bh);
      }
   }

   // ---------------------------------------------------------------- styles

   private List<Entry> entries(Tab g) {
      String key = g + ":" + StylesCloud.version() + ":" + StylesUploadManager.version() + ":" + MyPacks.version() + ":" + VipPacks.version();
      if (key.equals(this.cacheKey)) {
         return this.cache;
      }
      List<Entry> out = new ArrayList<>();
      for (Tab t : BaulooStylesConfigScreen.groupOf(g)) {
         if (t == g) {
            out.add(new Entry(null, "Vanilla", null, t));
         }
         try {
            for (StylesCloud.Style s : StylesCloud.of(t.prefix)) {
               out.add(new Entry(s.folder(), s.name(), "Cloud", t));
            }
         } catch (Throwable ignored) {
         }
         try {
            for (String[] s : StylesUploadManager.sync(t.prefix)) {
               out.add(new Entry(s[0], s[1], "Upload", t));
            }
         } catch (Throwable ignored) {
         }
         for (String[] s : BaulooStylesClient.stylesOf(t)) {
            out.add(new Entry(s[0], s[1], null, t));
         }
         for (MyPacks.Style s : MyPacks.styles()) {
            if (s.category().equals(t.prefix)) {
               out.add(new Entry("mypack:" + s.id(), s.name(), "Dein Pack", t));
            }
         }
         try {
            for (VipPacks.Style s : VipPacks.styles()) {
               if (s.category().equals(t.prefix)) {
                  out.add(new Entry("vippack:" + s.id(), s.name(), "VIP", t));
               }
            }
         } catch (Throwable ignored) {
         }
      }
      this.cacheKey = key;
      this.cache = out;
      return out;
   }

   private void styles(int x, int y, int w, int h) {
      Map<Tab, String> active = BaulooStylesConfigScreen.activeFolders();
      // groups (left)
      int lw = this.p(150.0);
      Gx.rect(x, y, lw, h, this.p(10.0), BenchView.CARD);
      Tab[] groups = BaulooStylesConfigScreen.shownTabs();
      int rh = this.p(22.0);
      int ry = y + this.p(6.0);
      for (Tab g : groups) {
         boolean act = g == group;
         boolean hv = this.v.hover(x, ry, lw, rh);
         if (act || hv) {
            Gx.rect(x + this.p(5.0), ry, lw - this.p(10.0), rh, rh / 2, act ? Gx.withAlpha(Style.accent, 0.9F) : Gx.withAlpha(-1, 0.05F));
         }
         boolean planned = false;
         for (Tab t : BaulooStylesConfigScreen.groupOf(g)) {
            planned |= BenchView.DRAFT.stylePlanned(t);
         }
         int fg = act ? Style.accentText : (hv ? BenchView.TEXT : BenchView.SUB);
         Gx.textMid(Fonts.ellipsize(g.title, 2, this.pf(7.4F), lw - this.p(50.0)), x + this.p(16.0), ry + rh / 2.0F, this.pf(7.4F), 2, fg);
         if (planned) {
            Gx.circle(x + lw - this.p(16.0), ry + rh / 2, this.p(2.5), act ? Style.accentText : Style.accent);
         } else if (active.get(g) != null) {
            Gx.icon("check", x + lw - this.p(16.0), ry + rh / 2.0F, this.pf(8.0), act ? Style.accentText : BenchView.MUTED);
         }
         Kit.hit(x, ry, lw, rh).click(() -> {
            group = g;
            this.target = 0.0F;
         });
         ry += rh + this.p(2.0);
         if (ry > y + h - rh) {
            break;
         }
      }
      // style cards (right)
      int gx = x + lw + this.p(12.0);
      int gw = w - lw - this.p(12.0);
      List<Entry> list = this.entries(group);
      Gx.text(group.title, gx, y, this.pf(10.0), 3, BenchView.TEXT);
      Gx.text(list.size() + " Styles · Klick = auswählen, ANWENDEN lädt alles", gx, y + this.p(14.0), this.pf(6.6F), 1, BenchView.MUTED);
      int top = y + this.p(30.0);
      int areaH = h - this.p(30.0);
      int gap = this.p(10.0);
      int cols = Math.max(2, (gw + gap) / (this.p(118.0) + gap));
      int cw = (gw - gap * (cols - 1)) / cols;
      int ch = Math.round(cw * 0.78F) + this.p(30.0);
      int rows = (list.size() + cols - 1) / cols;
      this.max = Math.max(0, rows * (ch + gap) - areaH);
      this.target = Math.max(0.0F, Math.min(this.max, this.target));
      this.scroll = Ease.lerp(this.scroll, this.target, 0.35F);
      Kit.hit(gx, top, gw, areaH).scroll(a -> this.target -= (float)(a * this.p(60.0)));
      Gx.clip(gx - this.p(4.0), top, gw + this.p(8.0), areaH);
      Kit.clip(gx - this.p(4.0), top, gw + this.p(8.0), areaH);
      for (int i = 0; i < list.size(); i++) {
         Entry e = list.get(i);
         int cx = gx + i % cols * (cw + gap);
         int cy = Math.round(top + this.p(2.0) + i / cols * (ch + gap) - this.scroll);
         if (cy + ch < top || cy > top + areaH) {
            continue;
         }
         this.styleCard(e, cx, cy, cw, ch, active.get(e.tab()));
      }
      Kit.unclip();
      Gx.unclip();
   }

   private void styleCard(Entry e, int x, int y, int w, int h, String current) {
      String want = BenchView.DRAFT.style(e.tab(), current == null ? dev.lego.txt.StylesBatch.VANILLA : current);
      String mine = e.folder() == null ? dev.lego.txt.StylesBatch.VANILLA : e.folder();
      boolean picked = mine.equals(want);
      boolean planned = picked && BenchView.DRAFT.stylePlanned(e.tab());
      boolean hv = this.v.hover(x, y, w, h);
      float hf = Ease.to("txt:c:" + e.tab() + ":" + e.folder(), hv ? 1.0F : 0.0F, 16.0F);
      int r = this.p(10.0);
      Gx.rect(x, y, w, h, r, Gx.mix(BenchView.CARD, BenchView.CARD_HI, hf));
      int imgH = h - this.p(30.0);
      int pad = this.p(6.0);
      Gx.rect(x + pad, y + pad, w - pad * 2, imgH - pad, this.p(7.0), 0xFF101114);
      if (e.folder() != null) {
         Identifier pic = null;
         try {
            pic = BaulooStylesConfigScreen.preview(e.folder());
         } catch (Throwable ignored) {
         }
         if (pic != null && Gx.B instanceof McBackend mb) {
            mb.texture(pic, x + pad, y + pad, w - pad * 2, imgH - pad, 256, 256, Gx.col(-1));
         } else {
            Gx.icon("image", x + w / 2.0F, y + imgH / 2.0F, this.pf(18.0), BenchView.MUTED);
         }
      } else {
         Gx.icon("reset", x + w / 2.0F, y + imgH / 2.0F, this.pf(18.0), BenchView.MUTED);
      }
      Gx.text(Fonts.ellipsize(e.name(), 2, this.pf(7.6F), w - this.p(16.0)), x + this.p(9.0), y + imgH + this.p(5.0), this.pf(7.6F), 2, BenchView.TEXT);
      String sub = e.kind() != null ? e.kind() : (e.tab() != group ? e.tab().title : "Eingebaut");
      Gx.text(sub, x + this.p(9.0), y + imgH + this.p(16.0), this.pf(6.2F), 1, e.kind() != null && e.kind().equals("Dein Pack") ? Style.accent : BenchView.MUTED);
      if (picked) {
         Gx.outline(x, y, w, h, r, Math.max(1, this.p(planned ? 1.4 : 1.0)), Gx.withAlpha(Style.accent, planned ? 1.0F : 0.7F));
         int bs = this.p(16.0);
         Gx.circle(x + w - bs / 2 - this.p(8.0), y + bs / 2 + this.p(8.0), bs / 2, Style.accent);
         Gx.icon(planned ? "plus" : "check", x + w - bs / 2.0F - this.p(8.0), y + bs / 2.0F + this.p(8.0), this.pf(9.0), Style.accentText);
      } else {
         Gx.outline(x, y, w, h, r, Math.max(1, this.p(0.7)), BenchView.LINE);
      }
      Kit.hit(x, y, w, h).click(() -> {
         BenchView.DRAFT.pickStyle(e.tab(), e.folder(), e.name(), current);
         Sound.pop();
      });
   }

   // ---------------------------------------------------------------- capes & cosmetics

   private void capes(int x, int y, int w, int h) {
      int lw = this.p(150.0);
      Gx.rect(x, y, lw, h, this.p(10.0), BenchView.CARD);
      int rh = this.p(24.0);
      int ry = y + this.p(6.0);
      for (int i = 0; i < SLOTS.length; i++) {
         Cosmetics.Slot s = SLOTS[i];
         boolean act = s == slot;
         boolean hv = this.v.hover(x, ry, lw, rh);
         if (act || hv) {
            Gx.rect(x + this.p(5.0), ry, lw - this.p(10.0), rh, rh / 2, act ? Gx.withAlpha(Style.accent, 0.9F) : Gx.withAlpha(-1, 0.05F));
         }
         int fg = act ? Style.accentText : (hv ? BenchView.TEXT : BenchView.SUB);
         Gx.textMid(SLOT_NAMES[i], x + this.p(16.0), ry + rh / 2.0F, this.pf(7.6F), 2, fg);
         if (BenchView.DRAFT.cosmeticPlanned(s)) {
            Gx.circle(x + lw - this.p(16.0), ry + rh / 2, this.p(2.5), act ? Style.accentText : Style.accent);
         }
         Kit.hit(x, ry, lw, rh).click(() -> {
            slot = s;
            this.target = 0.0F;
         });
         ry += rh + this.p(2.0);
      }
      // preview (right)
      int pw = this.p(170.0);
      int px = x + w - pw;
      Gx.rect(px, y, pw, h, this.p(10.0), BenchView.CARD);
      Gx.textCenter("Vorschau", px + pw / 2.0F, y + this.p(12.0), this.pf(7.0), 2, BenchView.SUB);
      // grid (middle)
      int gx = x + lw + this.p(12.0);
      int gw = px - gx - this.p(12.0);
      List<String[]> items = new ArrayList<>();
      items.add(new String[]{"none", "Keins"});
      for (String[] e : Cosmetics.of(slot)) {
         if (!"none".equals(e[0])) {
            items.add(e);
         }
      }
      String want = BenchView.DRAFT.cosmetic(slot);
      int gap = this.p(10.0);
      int cols = Math.max(2, (gw + gap) / (this.p(92.0) + gap));
      int cw = (gw - gap * (cols - 1)) / cols;
      int ch = Math.round(cw * 1.2F) + this.p(22.0);
      int rows = (items.size() + cols - 1) / cols;
      this.max = Math.max(0, rows * (ch + gap) - h);
      this.target = Math.max(0.0F, Math.min(this.max, this.target));
      this.scroll = Ease.lerp(this.scroll, this.target, 0.35F);
      Kit.hit(gx, y, gw, h).scroll(a -> this.target -= (float)(a * this.p(60.0)));
      String hovered = null;
      Gx.clip(gx - this.p(4.0), y, gw + this.p(8.0), h);
      Kit.clip(gx - this.p(4.0), y, gw + this.p(8.0), h);
      for (int i = 0; i < items.size(); i++) {
         String key = items.get(i)[0];
         String name = items.get(i)[1];
         int cx = gx + i % cols * (cw + gap);
         int cy = Math.round(y + i / cols * (ch + gap) - this.scroll);
         if (cy + ch < y || cy > y + h) {
            continue;
         }
         boolean hv = this.v.hover(cx, cy, cw, ch);
         if (hv) {
            hovered = key;
         }
         boolean picked = key.equals(want);
         boolean planned = picked && BenchView.DRAFT.cosmeticPlanned(slot);
         int r = this.p(10.0);
         Gx.rect(cx, cy, cw, ch, r, hv ? BenchView.CARD_HI : BenchView.CARD);
         int ih = ch - this.p(22.0);
         int pad = this.p(6.0);
         Gx.rect(cx + pad, cy + pad, cw - pad * 2, ih - pad, this.p(7.0), 0xFF101114);
         if (!"none".equals(key)) {
            this.cosmeticIcon(key, cx + pad, cy + pad, cw - pad * 2, ih - pad);
         } else {
            Gx.icon("x", cx + cw / 2.0F, cy + ih / 2.0F, this.pf(16.0), BenchView.MUTED);
         }
         Gx.textCenter(Fonts.ellipsize(name, 2, this.pf(7.0F), cw - this.p(10.0)), cx + cw / 2.0F, cy + ih + this.p(9.0), this.pf(7.0F), 2, BenchView.TEXT);
         Gx.outline(cx, cy, cw, ch, r, Math.max(1, this.p(picked ? (planned ? 1.4 : 1.0) : 0.7)), picked ? Gx.withAlpha(Style.accent, planned ? 1.0F : 0.7F) : BenchView.LINE);
         Kit.hit(cx, cy, cw, ch).click(() -> {
            BenchView.DRAFT.pickCosmetic(slot, key);
            Sound.pop();
         });
      }
      Kit.unclip();
      Gx.unclip();
      // live preview of the hovered or planned cosmetic on your own player
      int areaY = y + this.p(24.0);
      int areaH = h - this.p(30.0);
      Kit.hit(px, areaY, pw, areaH).drag((mx, my) -> this.yaw = (mx - (px + pw / 2.0F)) / this.pf(1.0F));
      String show = hovered != null ? hovered : want;
      try {
         Cosmetics.tryOn(slot, show);
         if (!Env.playerPreview.draw(px + this.p(10.0), areaY, pw - this.p(20.0), areaH, this.yaw, 0.0F)) {
            Gx.textCenter("Nur in einer Welt", px + pw / 2.0F, areaY + areaH / 2.0F, this.pf(7.0), 1, BenchView.MUTED);
         }
      } catch (Throwable e) {
         Gx.textCenter("Keine Vorschau", px + pw / 2.0F, areaY + areaH / 2.0F, this.pf(7.0), 1, BenchView.MUTED);
      } finally {
         Cosmetics.tryOn(null, null);
      }
   }

   private void cosmeticIcon(String key, int x, int y, int w, int h) {
      Identifier id;
      try {
         id = Cosmetics.icon(slot, key);
      } catch (Throwable e) {
         id = null;
      }
      if (id == null || !(Gx.B instanceof McBackend mb)) {
         Gx.icon("shirt", x + w / 2.0F, y + h / 2.0F, this.pf(16.0), BenchView.MUTED);
         return;
      }
      if (slot == Cosmetics.Slot.CAPE || slot == Cosmetics.Slot.MC_CAPE) {
         // the front of the cape (10x16 of the 64x32 layout), kept upright
         int fw = Math.min(w, Math.round(h * 10.0F / 16.0F));
         int fh = Math.round(fw * 16.0F / 10.0F);
         mb.textureRegion(id, x + (w - fw) / 2, y + (h - fh) / 2, fw, fh, 1.0F, 1.0F, 10, 16, 64, 32, Gx.col(-1));
      } else {
         int s = Math.min(w, h);
         mb.texture(id, x + (w - s) / 2, y + (h - s) / 2, s, s, 64, 64, Gx.col(-1));
      }
   }

   // ---------------------------------------------------------------- extras

   private void extras(int x, int y, int w, int h) {
      TxtExtras.pullAll();
      float ins = Ease.to("bench:ins", this.v.inspect != null ? 1.0F : 0.0F, 14.0F);
      int toolW = Math.round(this.p(270.0) * ins);
      List<Module> mods = new ArrayList<>(TxtExtras.all());
      this.v.wall(mods, x, y, w - toolW - (toolW > 0 ? this.p(8.0) : 0), h);
      if (toolW > this.p(4.0)) {
         this.v.toolbox(x + w - toolW, y - this.p(4.0), toolW, h + this.p(4.0), ins);
      }
   }

   // ---------------------------------------------------------------- presets

   private void presets(int x, int y, int w, int h) {
      Map<Tab, String> active = BaulooStylesConfigScreen.activeFolders();
      int bw = this.p(190.0);
      int bh = this.p(26.0);
      String[][] actions = {
         {"Aktuelle Styles speichern", "bookmark"}, {"Eigene Packs neu lesen", "refresh"}, {"Upload-Ordner öffnen", "image"}, {"Upload-Seite öffnen", "globe"}
      };
      for (int i = 0; i < actions.length; i++) {
         int ax = x + i % 2 * (bw + this.p(8.0));
         int ay = y + i / 2 * (bh + this.p(8.0));
         boolean hv = this.v.hover(ax, ay, bw, bh);
         Gx.rect(ax, ay, bw, bh, bh / 2, hv ? BenchView.CARD_HI : BenchView.CARD);
         Gx.outline(ax, ay, bw, bh, bh / 2, Math.max(1, this.p(0.7)), BenchView.LINE);
         Gx.icon(actions[i][1], ax + this.p(15.0), ay + bh / 2.0F, this.pf(10.0), Style.accent);
         Gx.textMid(actions[i][0], ax + this.p(27.0), ay + bh / 2.0F, this.pf(7.4F), 2, BenchView.TEXT);
         int idx = i;
         Kit.hit(ax, ay, bw, bh).click(() -> {
            try {
               switch (idx) {
                  case 0 -> StylesPresets.add(StylesPresets.nextName(), new HashMap<>(active), new HashMap<>());
                  case 1 -> MyPacks.rescan();
                  case 2 -> StylesUploadManager.openFolder();
                  default -> StylesUploadManager.open();
               }
            } catch (Throwable e) {
               dev.lego.LegoClient.LOG("TxT Presets: " + e);
            }
         });
      }
      int ly = y + 2 * (bh + this.p(8.0)) + this.p(8.0);
      List<StylesPresets.Preset> all = StylesPresets.all();
      Gx.text(all.size() + " Presets", x, ly, this.pf(8.0), 2, BenchView.TEXT);
      ly += this.p(16.0);
      int rh = this.p(30.0);
      int areaH = y + h - ly;
      this.max = Math.max(0, all.size() * (rh + this.p(6.0)) - areaH);
      this.target = Math.max(0.0F, Math.min(this.max, this.target));
      this.scroll = Ease.lerp(this.scroll, this.target, 0.35F);
      int top = ly;
      Kit.hit(x, top, w, areaH).scroll(a -> this.target -= (float)(a * this.p(50.0)));
      Gx.clip(x, top, w, areaH);
      Kit.clip(x, top, w, areaH);
      for (int i = 0; i < all.size(); i++) {
         StylesPresets.Preset pr = all.get(i);
         int ry = Math.round(top + i * (rh + this.p(6.0)) - this.scroll);
         Gx.rect(x, ry, w, rh, this.p(9.0), BenchView.CARD);
         Gx.textMid(Fonts.ellipsize(pr.name(), 2, this.pf(8.0F), w / 3.0F), x + this.p(12.0), ry + rh / 2.0F, this.pf(8.0F), 2, BenchView.TEXT);
         String sub = pr.styles().size() + " Styles" + (pr.author() != null ? " · von " + pr.author() : "");
         Gx.textMid(sub, x + w / 3.0F + this.p(20.0), ry + rh / 2.0F, this.pf(6.8F), 1, BenchView.MUTED);
         int lb = this.p(80.0);
         int lx = x + w - lb - this.p(6.0) - (pr.locked() ? 0 : this.p(34.0));
         boolean lh = this.v.hover(lx, ry + this.p(5.0), lb, rh - this.p(10.0));
         Gx.rect(lx, ry + this.p(5.0), lb, rh - this.p(10.0), (rh - this.p(10.0)) / 2, lh ? Gx.mix(Style.accent, -1, 0.1F) : Style.accent);
         Gx.textCenter("Einplanen", lx + lb / 2.0F, ry + rh / 2.0F, this.pf(7.0F), 2, Style.accentText);
         Kit.hit(lx, ry + this.p(5.0), lb, rh - this.p(10.0)).click(() -> {
            for (Map.Entry<Tab, String> e : pr.styles().entrySet()) {
               BenchView.DRAFT.pickStyle(e.getKey(), e.getValue(), e.getValue(), active.get(e.getKey()));
            }
            Sound.pop();
         });
         if (!pr.locked()) {
            int dx = x + w - this.p(34.0);
            boolean dh = this.v.hover(dx, ry + this.p(5.0), this.p(28.0), rh - this.p(10.0));
            Gx.icon("trash", dx + this.p(14.0), ry + rh / 2.0F, this.pf(10.0), dh ? Style.danger : BenchView.MUTED);
            Kit.hit(dx, ry + this.p(5.0), this.p(28.0), rh - this.p(10.0)).click(() -> StylesPresets.remove(pr));
         }
      }
      if (all.isEmpty()) {
         Gx.text("Noch keine Presets – speichere deine aktuellen Styles mit dem Knopf oben.", x, top + this.p(4.0), this.pf(7.0), 1, BenchView.MUTED);
      }
      Kit.unclip();
      Gx.unclip();
   }

   static String lower(String s) {
      return s.toLowerCase(Locale.ROOT);
   }
}
