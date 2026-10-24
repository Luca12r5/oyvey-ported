package dev.lego.games;

final class PackMind {
   static final String[] SUITS = new String[]{"♣", "♠", "♥", "♦"};
   static final String[] SUIT_NAMES = new String[]{"Kreuz", "Pik", "Herz", "Karo"};
   static final String[] RANKS = new String[]{"2", "3", "4", "5", "6", "7", "8", "9", "10", "B", "D", "K", "A"};

   private PackMind() {
   }

   static void card(GameView v, float x, float y, int suit, int rank, boolean faceUp, boolean hi) {
      if (!faceUp) {
         v.box(x, y, 34.0F, 48.0F, 5.0F, -15054176);
         v.box(x + 3.0F, y + 3.0F, 28.0F, 42.0F, 4.0F, -13997360);
      } else {
         v.box(x, y, 34.0F, 48.0F, 5.0F, hi ? -2368 : -460552);
         int col = suit >= 2 ? -3141600 : -15395302;
         v.text(RANKS[rank], x + 9.0F, y + 9.0F, 8.0F, 3, col);
         v.text(SUITS[suit], x + 17.0F, y + 28.0F, 14.0F, 3, col);
      }
   }
}
