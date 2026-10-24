package dev.lego.ui;

public final class Keys {
   private Keys() {
   }

   public static String name(int var0) {
      if (var0 < 0) {
         return "Keine";
      } else if (var0 >= 65 && var0 <= 90) {
         return String.valueOf((char)var0);
      } else if (var0 >= 48 && var0 <= 57) {
         return String.valueOf((char)var0);
      } else if (var0 >= 290 && var0 <= 314) {
         return "F" + (var0 - 289);
      } else if (var0 >= 320 && var0 <= 329) {
         return "Num " + (var0 - 320);
      } else {
         switch (var0) {
            case 0:
               return "Maus L";
            case 1:
               return "Maus R";
            case 2:
               return "Maus M";
            case 3:
               return "Maus 4";
            case 4:
               return "Maus 5";
            case 32:
               return "Leertaste";
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
               return "Enter";
            case 258:
               return "Tab";
            case 260:
               return "Einfg";
            case 261:
               return "Entf";
            case 262:
               return "Rechts";
            case 263:
               return "Links";
            case 264:
               return "Runter";
            case 265:
               return "Hoch";
            case 266:
               return "Bild hoch";
            case 267:
               return "Bild runter";
            case 268:
               return "Pos1";
            case 269:
               return "Ende";
            case 280:
               return "Feststell";
            case 340:
               return "L-Shift";
            case 341:
               return "L-Strg";
            case 342:
               return "L-Alt";
            case 344:
               return "R-Shift";
            case 345:
               return "R-Strg";
            case 346:
               return "Alt Gr";
            default:
               return "Taste " + var0;
         }
      }
   }
}
