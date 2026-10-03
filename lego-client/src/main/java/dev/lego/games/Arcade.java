package dev.lego.games;

public final class Arcade {
   private static boolean done = false;

   private Arcade() {
   }

   public static synchronized void registerAll() {
      if (!done) {
         done = true;
         GameInfo.add(
            new GameInfo(
               "solarsmash",
               "Solar Smash",
               "moon",
               -38358,
               "Zerstöre Planeten mit über 100 Werkzeugen",
               "Ein Sonnensystem in 4K: Klick auf einen Planeten und probier Laser, Meteore, UFOs, Naturkatastrophen, Schwarze Löcher und lustige Werkzeuge aus. Rechts siehst du, wie viele Bewohner noch leben.",
               "Klick = Werkzeug · 1-8 Kategorie · Mausrad = Größe · Leertaste = Pause · R = reparieren · H = UI aus",
               true,
               SolarSmash::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "snake",
               "Snake",
               "trail",
               -14756000,
               "Friss die Steine und werde länger",
               "Steuere die Schlange zu den gelben Lego-Steinen. Jeder Stein macht dich länger und schneller. Beiß dich nicht selbst und fahr nicht gegen die Wand!",
               "Pfeiltasten / WASD",
               true,
               Snake::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "2048",
               "2048",
               "grid",
               -20434,
               "Schiebe gleiche Steine zusammen",
               "Schiebe alle Steine in eine Richtung. Zwei gleiche Zahlen verschmelzen zu einer doppelt so großen. Schaffst du die 2048?",
               "Pfeiltasten / WASD",
               true,
               Game2048::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "flappy",
               "Flappy Lego",
               "wing",
               -12604929,
               "Flieg durch die Lücken",
               "Dein Lego-Stein fällt nach unten. Drück Leertaste oder klick, um nach oben zu flattern. Flieg durch die Lücken zwischen den Säulen.",
               "Leertaste / Klick",
               true,
               Flappy::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "tetris",
               "Lego-Tetris",
               "brick",
               -1900533,
               "Stapel die Steine, räume Reihen ab",
               "Fallende Lego-Steine: Drehe und verschiebe sie so, dass volle Reihen entstehen. Volle Reihen verschwinden und geben Punkte. Mehrere Reihen auf einmal = mehr Punkte!",
               "Pfeile: bewegen/drehen  •  Leertaste: fallen lassen",
               true,
               Tetris::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "breakout",
               "Breakout",
               "hit",
               -30208,
               "Zerstöre alle Steine mit dem Ball",
               "Halte den Ball mit dem Schläger im Spiel und zerstöre alle Lego-Steine. Wo der Ball den Schläger trifft, bestimmt die Richtung.",
               "Maus oder Pfeiltasten",
               true,
               Breakout::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "pong",
               "Pong",
               "gamepad",
               -8587265,
               "Klassiker gegen die KI",
               "Spiele Pong gegen den Computer. Wer zuerst 7 Punkte hat, gewinnt. Deine Punkte zählen für den Rekord.",
               "W/S oder Pfeile / Maus",
               true,
               Pong::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "tictactoe",
               "Tic-Tac-Toe",
               "x",
               -6599222,
               "Drei in einer Reihe gegen die KI",
               "Du bist X, die KI ist O. Wer zuerst drei in einer Reihe hat, gewinnt. Die KI spielt perfekt – schaffst du wenigstens ein Unentschieden?",
               "Maus",
               true,
               TicTacToe::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "connect4",
               "Vier gewinnt",
               "mods",
               -13053,
               "Vier in einer Reihe gegen die KI",
               "Wirf abwechselnd Steine in die Spalten. Wer zuerst vier in einer Reihe hat (waagerecht, senkrecht oder diagonal), gewinnt.",
               "Maus / 1-7",
               true,
               ConnectFour::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "minesweeper",
               "Minesweeper",
               "flag",
               -6642766,
               "Finde alle Minen",
               "Deck alle Felder ohne Mine auf. Die Zahl zeigt, wie viele Minen daneben liegen. Rechtsklick setzt eine Flagge.",
               "Linksklick: aufdecken  •  Rechtsklick: Flagge",
               true,
               Minesweeper::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "memory",
               "Memory",
               "image",
               -36939,
               "Finde alle Paare",
               "Decke immer zwei Karten auf. Gleiche Bilder bleiben offen. Finde alle Paare mit möglichst wenigen Zügen und schnell!",
               "Maus",
               true,
               Memory::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "runner",
               "Parkour-Runner",
               "sprint",
               -4653233,
               "Spring über die Hindernisse",
               "Dein Läufer rennt immer schneller. Spring über Kakteen und duck dich unter Vögeln. Wie weit kommst du?",
               "Leertaste/Pfeil hoch: springen  •  Pfeil runter: ducken",
               true,
               Runner::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "reaction",
               "Reaktionstest",
               "bolt",
               -7859,
               "Wie schnell bist du?",
               "Warte, bis das Feld grün wird, und klick dann so schnell du kannst. Nach 5 Runden bekommst du deinen Durchschnitt in Millisekunden. Zu früh klicken = Fehlstart!",
               "Maus / Leertaste",
               false,
               Reaction::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "aim",
               "Aim-Trainer",
               "target",
               -45730,
               "Triff so viele Ziele wie möglich",
               "Klick die Ziele an, bevor sie verschwinden. Du hast 30 Sekunden. Genauigkeit und Tempo geben Punkte – perfekt als PvP-Training!",
               "Maus",
               true,
               AimTrainer::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "cps",
               "CPS-Test",
               "mouse",
               -16735270,
               "Wie schnell klickst du?",
               "Klick 10 Sekunden lang so schnell du kannst in das Feld. Am Ende siehst du deine Klicks pro Sekunde (CPS).",
               "Maus",
               true,
               CpsTest::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "simon",
               "Farben merken",
               "palette",
               -3703297,
               "Merk dir die Reihenfolge",
               "Die Farbfelder leuchten in einer Reihenfolge auf. Klick sie danach in genau dieser Reihenfolge nach. Jede Runde kommt eine Farbe dazu.",
               "Maus / 1-4",
               true,
               Simon::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "santajump",
               "Weihnachtsmann-Sprung",
               "star",
               -1890757,
               "Hüpf mit dem Weihnachtsmann in den Himmel",
               "Der Weihnachtsmann springt automatisch von Wolke zu Wolke. Lenke ihn nach links und rechts, sammle Geschenke für Combos und nutze Schornsteine als Turbo. Vorsicht: Eisschollen brechen und Gewitterwolken musst du von oben erwischen!",
               "← → / A D oder Maus",
               true,
               SantaJump::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "giftcatch",
               "Geschenke-Fänger",
               "gift",
               -736942,
               "Fang die Geschenke mit dem Schlitten",
               "Geschenke fallen vom Himmel – fang sie mit dem Schlitten auf! Kohle kostet ein Leben, ein verpasstes Geschenk auch. Sterne verdoppeln die Punkte, Uhren bremsen die Zeit. Es wird immer schneller!",
               "Maus oder ← → / A D",
               true,
               GiftCatch::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "snowball",
               "Schneeballschlacht",
               "snow",
               -6301441,
               "Triff die Elfen hinter den Schneewällen",
               "Halte die Maus gedrückt, zieh zurück wie eine Schleuder und lass los. Triff die Elfen, bevor sie sich ducken – weiter hinten gibt es mehr Punkte. Schieß ihre Schneebälle ab und verschone das Rentier! 60 Sekunden.",
               "Maus: ziehen & loslassen",
               true,
               SnowballFight::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "stacker",
               "Lego-Stapler",
               "box",
               -11821238,
               "Stapel die Steine so hoch du kannst",
               "Der Stein fährt hin und her. Setz ihn genau auf den Turm – was übersteht, wird abgeschnitten. Perfekte Treffer geben Bonuspunkte, und nach drei perfekten in Folge wächst der Stein wieder!",
               "Leertaste / Klick",
               true,
               BrickStacker::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "asteroids",
               "Asteroiden",
               "moon",
               -10685697,
               "Zerschieß Asteroiden im Neon-All",
               "Dreh dein Schiff, gib Schub und zerschieße die Asteroiden – große zerfallen in kleinere. Pass auf UFOs auf, die zurückschießen. Der Bildschirmrand führt auf die andere Seite.",
               "← →: drehen  •  ↑: Schub  •  Leertaste: Feuer  •  ↓: Hypersprung",
               true,
               Asteroids::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "invaders",
               "Alien-Abwehr",
               "shield",
               -41816,
               "Halte die Alien-Wellen auf",
               "Wellen von Aliens rücken immer schneller vor. Schieß sie ab, bevor sie unten ankommen, und versteck dich hinter den Eis-Schilden. Power-ups: Dreifachschuss, Schnellfeuer, Schutzschild und Extraleben.",
               "← → bewegen  •  Leertaste: schießen",
               true,
               AlienDefense::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "maze",
               "Labyrinth-Jagd",
               "waypoint",
               -12616705,
               "Friss alle Zuckerperlen im Eislabyrinth",
               "Als Lebkuchenmann sammelst du alle Zuckerperlen ein. Vier Schneemänner jagen dich – jeder auf seine Art. Zuckerstangen drehen den Spieß um: Dann kannst du die Schneemänner schmelzen!",
               "Pfeiltasten / WASD",
               true,
               MazeChase::new
            )
         );
         GameInfo.add(
            new GameInfo(
               "sudoku",
               "Sudoku",
               "number",
               -7362305,
               "Logikrätsel in vier Schwierigkeitsstufen",
               "Fülle jede Zeile, Spalte und jedes 3×3-Feld mit den Zahlen 1 bis 9. Mit Notizen kannst du Kandidaten markieren. Je schneller und fehlerfreier, desto mehr Punkte – drei Fehler und das Rätsel ist verloren.",
               "Maus / 1-9  •  N: Notizen  •  H: Tipp",
               true,
               Sudoku::new
            )
         );
      }
   }
}
