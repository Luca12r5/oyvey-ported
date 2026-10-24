package dev.lego.ui;

import dev.lego.core.Lang;

public final class Keys {
   private Keys() {
   }

   public static String name(int k) {
      if (k < 0) {
         return Lang.get("key.none", "Keine");
      } else if (k >= 65 && k <= 90) {
         return String.valueOf((char)k);
      } else if (k >= 48 && k <= 57) {
         return String.valueOf((char)k);
      } else if (k >= 290 && k <= 314) {
         return "F" + (k - 289);
      } else if (k >= 320 && k <= 329) {
         return "Num " + (k - 320);
      } else {
         switch (k) {
            case 0:
               return Lang.get("key.mouse_l", "Maus L");
            case 1:
               return Lang.get("key.mouse_r", "Maus R");
            case 2:
               return Lang.get("key.mouse_m", "Maus M");
            case 3:
               return Lang.get("key.mouse_4", "Maus 4");
            case 4:
               return Lang.get("key.mouse_5", "Maus 5");
            case 32:
               return Lang.get("key.space", "Leertaste");
            case 39:
               return "Ä";
            case 44:
               return ",";
            case 45:
               return "ß";
            case 46:
               return ".";
            case 47:
               return "-";
            case 59:
               return "Ö";
            case 91:
               return "Ü";
            case 96:
               return "^";
            case 257:
               return Lang.get("key.enter", "Enter");
            case 258:
               return Lang.get("key.tab", "Tab");
            case 260:
               return Lang.get("key.insert", "Einfg");
            case 261:
               return Lang.get("key.delete", "Entf");
            case 262:
               return Lang.get("key.right", "Rechts");
            case 263:
               return Lang.get("key.left", "Links");
            case 264:
               return Lang.get("key.down", "Runter");
            case 265:
               return Lang.get("key.up", "Hoch");
            case 266:
               return Lang.get("key.pgup", "Bild hoch");
            case 267:
               return Lang.get("key.pgdn", "Bild runter");
            case 268:
               return Lang.get("key.home", "Pos1");
            case 269:
               return Lang.get("key.end", "Ende");
            case 280:
               return Lang.get("key.caps", "Feststell");
            case 340:
               return Lang.get("key.lshift", "L-Shift");
            case 341:
               return Lang.get("key.lctrl", "L-Strg");
            case 342:
               return Lang.get("key.lalt", "L-Alt");
            case 344:
               return Lang.get("key.rshift", "R-Shift");
            case 345:
               return Lang.get("key.rctrl", "R-Strg");
            case 346:
               return Lang.get("key.ralt", "Alt Gr");
            default:
               return Lang.get("key.unknown", "Taste") + " " + k;
         }
      }
   }
}
