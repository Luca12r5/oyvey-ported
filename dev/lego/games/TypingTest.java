package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class TypingTest extends GameView {
   private static final String[] WORDS = "haus baum katze hund block stein lego spiel welt licht wasser feuer erde luft sonne mond stern wolke regen schnee berg fluss see meer insel brot milch apfel birne kuchen tisch stuhl lampe fenster tuer garten blume gras wald weg strasse auto zug schiff flugzeug rakete ritter burg drache schatz karte schwert schild bogen pfeil kiste ofen fackel diamant gold eisen kohle redstone portal zombie creeper spinne schaf kuh schwein huhn pferd"
      .split(" ");
   private final List<String> queue = new ArrayList<>();
   private String typed = "";
   private float left;
   private int typedChars;
   private int errors;
   private boolean bad;

   TypingTest() {
      super("typing");
   }

   @Override
   protected float boardW() {
      return 420.0F;
   }

   @Override
   protected float boardH() {
      return 240.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Wörter/min";
   }

   @Override
   protected String overTitle() {
      return "Fertig! " + this.score + " Wörter pro Minute";
   }

   @Override
   protected void reset() {
      this.queue.clear();

      for (int i = 0; i < 12; i++) {
         this.queue.add(WORDS[this.rnd.nextInt(WORDS.length)]);
      }

      this.typed = "";
      this.left = 60.0F;
      this.typedChars = 0;
      this.errors = 0;
      this.bad = false;
   }

   @Override
   public boolean chr(String s) {
      if (this.state != GameView.State.PLAYING) {
         return true;
      } else {
         for (char c : s.toLowerCase(Locale.ROOT).toCharArray()) {
            if (c == ' ') {
               this.submit();
            } else {
               this.typed = this.typed + c;
               this.bad = !this.queue.get(0).startsWith(this.typed);
               if (this.queue.get(0).equals(this.typed)) {
                  this.submit();
               }
            }
         }

         return true;
      }
   }

   private void submit() {
      if (!this.typed.isEmpty()) {
         if (this.typed.equals(this.queue.get(0))) {
            this.score++;
            this.typedChars = this.typedChars + this.typed.length();
            Sound.play("minecraft:block.note_block.hat", 1.6F, 0.2F);
         } else {
            this.errors++;
            PackPuzzle.bad();
         }

         this.queue.remove(0);
         this.queue.add(WORDS[this.rnd.nextInt(WORDS.length)]);
         this.typed = "";
         this.bad = false;
      }
   }

   @Override
   protected void onKey(int k) {
      if (k == 259 && !this.typed.isEmpty()) {
         this.typed = this.typed.substring(0, this.typed.length() - 1);
         this.bad = !this.queue.get(0).startsWith(this.typed);
      }
   }

   @Override
   protected void update(float dt) {
      this.left -= dt;
      if (this.left <= 0.0F) {
         this.left = 0.0F;
         this.gameOver();
      }
   }

   @Override
   protected void render() {
      float x = 20.0F;

      for (int i = 0; i < 6 && i < this.queue.size(); i++) {
         String w = this.queue.get(i);
         int col = i == 0 ? (this.bad ? Style.danger : Style.accent) : Style.sub;
         this.textL(w, x, 70.0F, i == 0 ? 14.0F : 10.0F, 3, col);
         x += Gx.width(w, i == 0 ? 14 : 10, 3) + 14.0F;
         if (x > 400.0F) {
            break;
         }
      }

      this.box(20.0F, 110.0F, 380.0F, 40.0F, 10.0F, Style.light ? -1644306 : -14934233);
      this.textL(this.typed + (System.currentTimeMillis() / 500L % 2L == 0L ? "|" : ""), 32.0F, 130.0F, 13.0F, 3, this.bad ? Style.danger : Style.text);
      this.text(String.format(Locale.ROOT, "%.0f s", this.left), 210.0F, 25.0F, 12.0F, 3, this.left < 10.0F ? Style.danger : Style.text);
      this.text("Richtig: " + this.score + "   Fehler: " + this.errors, 210.0F, 180.0F, 8.0F, 3, Style.sub);
      this.text("Tippe die Wörter, Leertaste = weiter", 210.0F, 210.0F, 6.6F, 2, Style.muted);
   }
}
