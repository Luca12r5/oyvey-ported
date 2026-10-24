package dev.lego.util;

import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.LegoScreen;
import dev.lego.ui.Style;
import dev.lego.ui.View;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

public final class FightPreset extends Module {
   final Setting.Action manage = this.add(new Setting.Action("Presets verwalten", "Öffnen", () -> LegoScreen.show(new FightPreset.PresetListView())));
   private static final List<FightPreset.Preset> presets = new ArrayList<>();
   private static int active = -1;

   public FightPreset() {
      super("fightpreset", "Kampf-Presets", "Inventar-Vorlagen: zeigt im Inventar, wo was hin muss", Category.UTILITY);
      this.icon("shield");
   }

   public static FightPreset.Preset activePreset() {
      return active >= 0 && active < presets.size() ? presets.get(active) : null;
   }

   public void renderInventoryOverlay(GuiGraphics ctx) {
      if (this.enabled) {
         FightPreset.Preset p = activePreset();
         if (p != null) {
            LocalPlayer me = Mc.player();
            if (me != null) {
               Matrix3x2fStack mat = ctx.pose();

               for (int i = 0; i < 41; i++) {
                  String expected = p.slots[i];
                  if (expected != null && !expected.isEmpty()) {
                     ItemStack current = Mc.invStack(i);
                     String currentKey = Mc.empty(current) ? "" : Mc.itemKey(current);
                     int[] pos = slotScreenPos(i, ctx.guiWidth(), ctx.guiHeight());
                     if (pos != null) {
                        int sx = pos[0];
                        int sy = pos[1];
                        int sw = 16;
                        boolean correct = currentKey.endsWith("." + expected) || currentKey.equals(expected);
                        boolean empty = Mc.empty(current);
                        int overlay = correct ? Gx.rgba(52292, 0.25F) : Gx.rgba(16720418, 0.2F);
                        Gx.fill(sx, sy, sw, sw, overlay);
                        if (empty) {
                           ItemStack ghost = p.ghostStack(i);
                           if (ghost != null) {
                              mat.pushMatrix();
                              Gx.pushAlpha(0.3F);
                              mat.translate(sx, sy);
                              ctx.renderItem(ghost, 0, 0);
                              Gx.popAlpha();
                              mat.popMatrix();
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static int[] slotScreenPos(int slot, int screenW, int screenH) {
      int gw = 176;
      int gh = 166;
      int gx = (screenW - gw) / 2;
      int gy = (screenH - gh) / 2;
      if (slot >= 0 && slot <= 8) {
         return new int[]{gx + 8 + slot * 18, gy + 142};
      } else if (slot >= 9 && slot <= 35) {
         int r = (slot - 9) / 9;
         int c = (slot - 9) % 9;
         return new int[]{gx + 8 + c * 18, gy + 84 + r * 18};
      } else if (slot == 36) {
         return new int[]{gx + 8, gy + 62};
      } else if (slot == 37) {
         return new int[]{gx + 8, gy + 44};
      } else if (slot == 38) {
         return new int[]{gx + 8, gy + 26};
      } else if (slot == 39) {
         return new int[]{gx + 8, gy + 8};
      } else {
         return slot == 40 ? new int[]{gx + 77, gy + 62} : null;
      }
   }

   public static void saveCurrentInventory(String name) {
      LocalPlayer me = Mc.player();
      if (me != null) {
         FightPreset.Preset p = new FightPreset.Preset(name);

         for (int i = 0; i < 41; i++) {
            ItemStack s = Mc.invStack(i);
            if (!Mc.empty(s)) {
               p.slots[i] = Mc.itemKey(s);
               String key = Mc.itemKey(s);
               int dot = key.lastIndexOf(46);
               p.itemIds[i] = dot >= 0 ? key.substring(dot + 1) : key;
            }
         }

         presets.add(p);
         active = presets.size() - 1;
         Modules.scheduleSave();
      }
   }

   public static final class Preset {
      public String name;
      public final String[] slots = new String[41];
      public final String[] itemIds = new String[41];

      Preset(String name) {
         this.name = name;
      }

      public ItemStack ghostStack(int i) {
         return this.itemIds[i] == null ? null : Mc.icon(this.itemIds[i]);
      }
   }

   public final class PresetListView extends View {
      private String newName = "";
      private float scroll = 0.0F;
      private float target = 0.0F;

      @Override
      public float designW() {
         return 520.0F;
      }

      @Override
      public float designH() {
         return 400.0F;
      }

      @Override
      public void draw() {
         float t = Ease.outQuint(this.open);
         Gx.fill(0, 0, this.W, this.H, Gx.rgba(0, 0.35F * this.open));
         int pw = this.p(520.0);
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
         Kit.hit(px + this.p(14.0), tb, bs, bs).click(() -> {
            if (this.host != null) {
               this.host.close();
            }
         });
         Gx.text("Kampf-Presets", px + this.p(54.0), tb + this.pf(1.0), this.pf(14.0), 3, Style.text);
         Gx.text("Inventar-Vorlagen speichern und aktivieren", px + this.p(54.0), tb + this.pf(19.0), this.pf(7.4F), 1, Style.sub);
         int cx = px + this.p(16.0);
         int cw = pw - this.p(32.0);
         int y = tb + this.p(46.0);
         int ih = this.p(32.0);
         Gx.rect(cx, y, cw - this.p(160.0), ih, this.p(10.0), Style.surface);
         Gx.outline(cx, y, cw - this.p(160.0), ih, this.p(10.0), Math.max(1, this.p(0.5)), Style.stroke);
         Gx.text(
            this.newName.isEmpty() ? "Preset-Name..." : this.newName,
            cx + this.p(10.0),
            y + this.pf(8.0),
            this.pf(10.0),
            1,
            this.newName.isEmpty() ? Style.muted : Style.text
         );
         int sbw = this.p(70.0);
         int sbx = cx + cw - this.p(150.0);
         boolean sh = this.hover(sbx, y, sbw, ih);
         Kit.button("save", "Speichern", "save", sbx, y, sbw, ih, this.P, true, sh);
         Kit.hit(sbx, y, sbw, ih).click(() -> {
            String n = this.newName.isEmpty() ? "Preset " + (FightPreset.presets.size() + 1) : this.newName;
            FightPreset.saveCurrentInventory(n);
            this.newName = "";
         });
         int dbw = this.p(80.0);
         int dbx = cx + cw - dbw;
         boolean dh = FightPreset.active >= 0 && FightPreset.active < FightPreset.presets.size() && this.hover(dbx, y, dbw, ih);
         Kit.button("deact", "Deaktivieren", "x", dbx, y, dbw, ih, this.P, FightPreset.active >= 0, dh);
         Kit.hit(dbx, y, dbw, ih).click(() -> FightPreset.active = -1);
         y += ih + this.p(14.0);
         int ly = y;
         int lh = py + ph - this.p(14.0) - y;
         int rh = this.p(50.0);
         int gap = this.p(6.0);
         float max = Math.max(0, FightPreset.presets.size() * (rh + gap) - gap - lh);
         this.target = Math.max(0.0F, Math.min(max, this.target));
         this.scroll = this.scroll + (this.target - this.scroll) * Math.min(1.0F, Gx.dt * 16.0F);
         Gx.clip(cx - this.p(4.0), y, cw + this.p(8.0), lh);
         Kit.clip(cx - this.p(4.0), y, cw + this.p(8.0), lh);
         Kit.hit(cx, y, cw, lh).scroll(a -> this.target = this.target - a.floatValue() * this.p(60.0));

         for (int i = 0; i < FightPreset.presets.size(); i++) {
            int ry = Math.round(ly + i * (rh + gap) - this.scroll);
            if (ry + rh >= ly && ry <= ly + lh) {
               FightPreset.Preset pr = FightPreset.presets.get(i);
               boolean isActive = i == FightPreset.active;
               Gx.rect(cx, ry, cw, rh, this.p(12.0), isActive ? Gx.withAlpha(Style.accent, 0.12F) : Style.surface);
               if (isActive) {
                  Gx.outline(cx, ry, cw, rh, this.p(12.0), Math.max(1, this.p(0.7)), Style.accent);
               }

               Gx.text(pr.name, cx + this.p(14.0), ry + this.pf(8.0), this.pf(10.0), 3, Style.text);
               int items = 0;

               for (String s : pr.slots) {
                  if (s != null && !s.isEmpty()) {
                     items++;
                  }
               }

               Gx.text(items + " Items", cx + this.p(14.0), ry + this.pf(24.0), this.pf(7.5), 1, Style.sub);
               int abw = this.p(70.0);
               int abh = this.p(26.0);
               int aby = ry + (rh - abh) / 2;
               int abx = cx + cw - abw * 2 - this.p(14.0);
               boolean asel = !isActive && this.hover(abx, aby, abw, abh);
               Kit.button("sel" + i, isActive ? "Aktiv" : "Wählen", null, abx, aby, abw, abh, this.P, !isActive, asel);
               int idx = i;
               Kit.hit(abx, aby, abw, abh).click(() -> FightPreset.active = idx);
               int rbx = cx + cw - abw - this.p(6.0);
               boolean rh2 = this.hover(rbx, aby, abw, abh);
               Gx.rect(rbx, aby, abw, abh, this.p(8.0), rh2 ? 1090466611 : 553595699);
               Gx.textCenter("Löschen", rbx + abw / 2.0F, aby + abh / 2.0F, this.pf(8.0), 2, -48060);
               Kit.hit(rbx, aby, abw, abh).click(() -> {
                  FightPreset.presets.remove(idx);
                  if (FightPreset.active >= FightPreset.presets.size()) {
                     FightPreset.active = FightPreset.presets.size() - 1;
                  }

                  Modules.scheduleSave();
               });
            }
         }

         if (FightPreset.presets.isEmpty()) {
            Gx.icon("shield", px + pw / 2.0F, ly + this.p(30.0), this.pf(22.0), Style.muted);
            Gx.textCenter("Keine Presets gespeichert", px + pw / 2.0F, ly + this.pf(60.0), this.pf(9.0), 2, Style.sub);
            Gx.textCenter("Rüste dich aus und klick 'Speichern'", px + pw / 2.0F, ly + this.pf(76.0), this.pf(7.5), 1, Style.muted);
         }

         Kit.unclip();
         Gx.unclip();
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
         } else if (key == 259 && !this.newName.isEmpty()) {
            this.newName = this.newName.substring(0, this.newName.length() - 1);
            return true;
         } else {
            return false;
         }
      }

      @Override
      public boolean chr(String s) {
         if (s.length() == 1 && this.newName.length() < 24) {
            this.newName = this.newName + s;
            return true;
         } else {
            return false;
         }
      }
   }
}
