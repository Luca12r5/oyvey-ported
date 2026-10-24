package dev.lego.games;

import dev.lego.ui.Fonts;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class McQuiz extends GameView {
   private static final String[][] Q = new String[][]{
      {"Wie viele Obsidianblöcke braucht ein Netherportal mindestens?", "10", "14", "8", "12"},
      {"Welches Werkzeug braucht man, um Diamanterz abzubauen?", "Eisen-Spitzhacke", "Stein-Spitzhacke", "Holz-Spitzhacke", "Gold-Spitzhacke"},
      {"Wovor haben Creeper Angst?", "Katzen", "Wölfen", "Wasser", "Fackeln"},
      {"Was droppt ein Enderman?", "Enderperle", "Enderauge", "Lohenrute", "Knochen"},
      {"Woraus craftet man ein Enderauge?", "Enderperle + Lohenstaub", "Enderperle + Zucker", "Diamant + Perle", "Redstone + Perle"},
      {"Auf welcher Höhe findet man Diamanten am häufigsten (1.18+)?", "Y = -58", "Y = 12", "Y = 64", "Y = 100"},
      {"Wie viele Herzen hat ein Spieler normal?", "10", "20", "8", "12"},
      {"Welcher Mob lässt Glut-/Lohenruten fallen?", "Lohe", "Ghast", "Magmawürfel", "Piglin"},
      {"Womit zähmt man einen Wolf?", "Knochen", "Fleisch", "Fisch", "Weizen"},
      {"Womit zähmt man eine Katze?", "Roher Fisch", "Milch", "Weizen", "Knochen"},
      {"Was braucht man für einen Zaubertisch?", "Buch, Diamanten, Obsidian", "Buch, Gold, Stein", "Eisen, Diamant, Holz", "Obsidian, Redstone, Buch"},
      {"Was passiert, wenn man im Nether ein Bett benutzt?", "Es explodiert", "Man schläft", "Nichts", "Es brennt"},
      {"Welches Erz gibt es NUR im Nether?", "Netherquarz", "Smaragd", "Kupfer", "Lapislazuli"},
      {"Wie heißt der Endboss in Minecraft?", "Enderdrache", "Wither", "Wächter", "Warden"},
      {"Welcher Mob ist blind und reagiert auf Geräusche?", "Warden", "Wither", "Phantom", "Vex"},
      {"Was braucht man, um den Wither zu beschwören?", "Seelensand + 3 Wither-Skelett-Schädel", "Obsidian + Schädel", "TNT + Schädel", "Netherrack + Kürbis"},
      {"Was macht ein Totem der Unsterblichkeit?", "Rettet dich vor dem Tod", "Macht unsichtbar", "Heilt Hunger", "Gibt Flügel"},
      {"Welches Biom hat Pilzkühe?", "Pilzinsel", "Dschungel", "Savanne", "Taiga"},
      {"Wie viele Blöcke Eisen braucht ein Eisengolem?", "4", "5", "3", "6"},
      {"Was schützt vor Phantomen?", "Regelmäßig schlafen", "Fackeln", "Schilde", "Wasser"},
      {"Was braucht man, um eine Elytra zu reparieren?", "Phantommembran", "Leder", "Federn", "Eisen"},
      {"Welcher Block leuchtet am hellsten (Stufe 15)?", "Leuchtstein", "Fackel", "Redstone-Fackel", "Magmablock"},
      {"Wie lange dauert ein Minecraft-Tag in Echtzeit?", "20 Minuten", "10 Minuten", "1 Stunde", "24 Minuten"},
      {"Was baut man aus Stöcken und Kohle?", "Fackeln", "Leitern", "Zäune", "Pfeile"},
      {"Welcher Lego-Stein ist der bekannteste?", "2x4-Stein", "1x1-Stein", "3x3-Platte", "6x6-Stein"},
      {"Aus welchem Land kommt Lego?", "Dänemark", "Deutschland", "Schweden", "USA"},
      {"Was bedeutet \"Lego\" auf Dänisch?", "Spiel gut", "Baue mehr", "Kleiner Stein", "Bunt"},
      {"Welche Farbe hat ein Creeper?", "Grün", "Blau", "Grau", "Rot"},
      {"Was droppt ein Skelett?", "Knochen und Pfeile", "Fäden", "Schießpulver", "Federn"},
      {"Was droppt eine Spinne?", "Fäden", "Knochen", "Schleim", "Leder"}
   };
   private final List<Integer> order = new ArrayList<>();
   private int idx;
   private int picked = -1;
   private int right;
   private List<String> answers;
   private float wait;

   McQuiz() {
      super("mcquiz");
   }

   @Override
   protected float boardW() {
      return 420.0F;
   }

   @Override
   protected float boardH() {
      return 270.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Richtig";
   }

   @Override
   protected String overTitle() {
      return this.score + " von 10 richtig";
   }

   @Override
   protected void reset() {
      this.order.clear();

      for (int i = 0; i < Q.length; i++) {
         this.order.add(i);
      }

      Collections.shuffle(this.order, this.rnd);
      this.idx = 0;
      this.load();
   }

   private void load() {
      String[] q = Q[this.order.get(this.idx)];
      this.answers = new ArrayList<>(List.of(q[1], q[2], q[3], q[4]));
      Collections.shuffle(this.answers, this.rnd);
      this.right = this.answers.indexOf(q[1]);
      this.picked = -1;
      this.wait = 0.0F;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (this.picked < 0) {
         for (int i = 0; i < 4; i++) {
            if (in(x, y, 20 + i % 2 * 195, 120 + i / 2 * 62, 185, 52)) {
               this.pick(i);
            }
         }
      }
   }

   @Override
   protected void onKey(int k) {
      if (this.picked < 0 && k >= 49 && k <= 52) {
         this.pick(k - 49);
      }
   }

   private void pick(int i) {
      this.picked = i;
      if (i == this.right) {
         this.score++;
         PackPuzzle.good();
      } else {
         PackPuzzle.bad();
      }
   }

   @Override
   protected void update(float dt) {
      if (this.picked >= 0) {
         if ((this.wait += dt) > 1.3F) {
            this.idx++;
            if (this.idx >= 10) {
               this.gameOver();
            } else {
               this.load();
            }
         }
      }
   }

   @Override
   protected void render() {
      this.text("Frage " + (this.idx + 1) + " / 10", 210.0F, 18.0F, 7.0F, 3, Style.sub);
      List<String> lines = Fonts.wrap(Q[this.order.get(this.idx)][0], 3, 11.0F * this.u, this.S(380.0F), 3);
      float y = 50.0F;

      for (String l : lines) {
         this.text(l, 210.0F, y, 11.0F, 3, Style.text);
         y += 16.0F;
      }

      for (int i = 0; i < 4; i++) {
         float bx0 = 20 + i % 2 * 195;
         float by0 = 120 + i / 2 * 62;
         int col = this.picked < 0
            ? (in(this.bmx(), this.bmy(), bx0, by0, 185.0F, 52.0F) ? Style.surfaceHover : Style.surface)
            : (i == this.right ? -13722038 : (i == this.picked ? -5234646 : Style.surface));
         this.box(bx0, by0, 185.0F, 52.0F, 10.0F, col);
         this.text(
            i + 1 + ")  " + this.answers.get(i), bx0 + 92.0F, by0 + 26.0F, 8.0F, 3, this.picked < 0 || i != this.right && i != this.picked ? Style.text : -1
         );
      }
   }
}
