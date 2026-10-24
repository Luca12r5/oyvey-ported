package dev.lego.ui.hub;

import dev.lego.ui.Kit;
import java.util.Locale;

/**
 * The search field every hub section shows (Kosmetik, Emotes, Spiele, Design): typing anywhere
 * starts it, Backspace deletes, Esc or the x clears, Strg+F focuses.
 */
final class HubSearch {
   static final int MAX = 24;
   String text = "";
   boolean focus = false;
   long changedAt = System.currentTimeMillis();

   void draw(HubView view, String id, int x, int y, int w, int h, boolean clickable) {
      view.searchBox(id, x, y, w, h, this.text, this.focus);
      if (clickable) {
         Kit.hit(x, y, w, h).click(() -> {
            if (!this.text.isEmpty() && view.mx > x + w - h) {
               this.set("");
            }

            this.focus = true;
         });
      }
   }

   boolean active() {
      return !this.text.trim().isEmpty();
   }

   /** True when the query is empty or any of the fields contains it (case-insensitive). */
   boolean matches(String... fields) {
      String q = this.text.toLowerCase(Locale.ROOT).trim();
      if (q.isEmpty()) {
         return true;
      }

      for (String f : fields) {
         if (f != null && f.toLowerCase(Locale.ROOT).contains(q)) {
            return true;
         }
      }

      return false;
   }

   void set(String value) {
      this.text = value;
      this.changedAt = System.currentTimeMillis();
   }

   boolean key(int key, int mods) {
      if (key == 259 && !this.text.isEmpty()) {
         this.set(this.text.substring(0, this.text.length() - 1));
         return true;
      } else if (key == 256 && (this.focus || !this.text.isEmpty())) {
         this.set("");
         this.focus = false;
         return true;
      } else if (key == 70 && (mods & 2) != 0) {
         this.focus = true;
         return true;
      } else {
         return false;
      }
   }

   boolean chr(String s) {
      if (s == null || s.isEmpty()) {
         return false;
      }

      if (this.text.length() + s.length() <= MAX) {
         this.set(this.text + s);
      }

      this.focus = true;
      return true;
   }
}
