package dev.lego.games;

import java.util.function.Supplier;

/** Registriert die Spiele aus Paket 4 (PackPuzzle, PackArcade, PackMind, PackMore). */
final class PackGames {
    private PackGames() { }

    private static void g(String id, String name, String icon, int color, String tag, String how, String keys, boolean high, Supplier<? extends dev.lego.ui.View> f) {
        GameInfo.add(new GameInfo(id, name, icon, color, tag, how, keys, high, f));
    }

    static void registerAll() {
        // ---- Denken & Brett
        g("lightsout", "Lichter aus", "light", 0xFFFFE14D, "Schalte alle Lichter aus",
                "Ein Klick schaltet ein Feld und seine vier Nachbarn um. Schaffst du es, alle Lichter auszuschalten - mit möglichst wenigen Zügen?", "Maus", false, LightsOut::new);
        g("slidepuzzle", "Schiebepuzzle", "grid", 0xFF3FA9FF, "Sortiere die Steine 1 bis 15",
                "Schiebe die Steine in die Lücke, bis sie der Reihe nach von 1 bis 15 liegen. Weniger Züge = besserer Rekord.", "Maus / Pfeiltasten", false, SlidePuzzle::new);
        g("mastermind", "Code knacken", "lock", 0xFF9B4DCA, "Errate den geheimen Farbcode",
                "Die KI hat 4 Farben versteckt. Nach jedem Versuch siehst du: schwarz = richtige Farbe am richtigen Platz, weiß = richtige Farbe am falschen Platz. Du hast 10 Versuche.", "Maus / 1-6, Enter", true, Mastermind::new);
        g("hangman", "Galgenraten", "chat", 0xFFFF8A00, "Errate das Minecraft-Wort",
                "Rate Buchstabe für Buchstabe das gesuchte Wort aus der Minecraft- und Lego-Welt. Nach 8 Fehlern ist Schluss. Wie viele Wörter schaffst du am Stück?", "Tastatur / Maus", true, Hangman::new);
        g("wordguess", "Wort-Raten", "list", 0xFF2E9E4A, "Finde das Wort mit 5 Buchstaben",
                "Du hast 6 Versuche. Grün = Buchstabe richtig, Gelb = kommt vor, aber woanders, Grau = kommt nicht vor. Wie lang wird deine Serie?", "Tastatur, Enter, Rücktaste", true, WordGuess::new);
        g("reversi", "Reversi", "grid", 0xFF1E6B3A, "Dreh die Steine des Gegners um",
                "Setze so, dass du gegnerische Steine einschließt - sie werden deine Farbe. Wer am Ende mehr Steine hat, gewinnt. Die KI-Stärke kannst du wählen.", "Maus", true, Reversi::new);
        g("nim", "Streichhölzer", "flame", 0xFFD7263D, "Wer das letzte Holz nimmt, verliert",
                "Nimm abwechselnd mit der KI beliebig viele Hölzer aus EINER Reihe. Wer das letzte Holz nehmen muss, verliert. Auf \"Profi\" spielt die KI perfekt!", "Maus", true, Nim::new);
        g("dotsboxes", "Käsekästchen", "grid", 0xFF3FA9FF, "Schließe mehr Kästchen als die KI",
                "Zieht abwechselnd Striche zwischen den Punkten. Wer ein Kästchen schließt, bekommt es und darf nochmal. Gib der KI keine dritte Seite!", "Maus", true, DotsBoxes::new);
        g("battleship", "Schiffe versenken", "target", 0xFF1A5AA0, "Versenke die Flotte der KI",
                "Schieß auf das rechte Feld und finde alle 5 Schiffe der KI, bevor sie deine Flotte versenkt. Bei einem Treffer darfst du nochmal.", "Maus", true, Battleship::new);
        g("hanoi", "Türme von Hanoi", "brick", 0xFFFF8A00, "Versetze den ganzen Turm",
                "Bring alle Scheiben auf den rechten Stab. Du darfst nur die oberste Scheibe bewegen und nie eine große auf eine kleine legen. Jede Runde kommt eine Scheibe dazu.", "Maus", true, Hanoi::new);
        g("pegsolitaire", "Solitär-Brett", "target", 0xFFE3000B, "Bis nur noch ein Stift übrig ist",
                "Springe mit einem Stift über einen anderen in ein freies Loch - der übersprungene wird entfernt. Schaffst du es bis auf einen einzigen Stift?", "Maus", true, PegSolitaire::new);
        g("floodit", "Farbflut", "palette", 0xFF00852B, "Färbe das ganze Feld in 22 Zügen",
                "Wähle eine Farbe: Das Feld oben links und alle verbundenen Felder nehmen sie an. Färbe das ganze Brett einfarbig, bevor die Züge ausgehen.", "Maus / 1-6", true, FloodIt::new);
        // ---- Arcade & Geschick
        g("frogger", "Straßenfrosch", "jump", 0xFF39D353, "Über Straße und Fluss",
                "Bring den Frosch nach oben: Weiche den Autos aus und hüpf auf die Baumstämme im Fluss - ins Wasser fallen ist tödlich. Jede Runde wird es schneller.", "Pfeiltasten / WASD", true, Frogger::new);
        g("caveflight", "Höhlenflug", "wing", 0xFFE3000B, "Flieg so weit wie möglich",
                "Halte die Maus oder Leertaste gedrückt, um zu steigen, lass los, um zu sinken. Berühre nicht die Höhlenwände - sie werden immer enger.", "Maus halten / Leertaste", true, CaveFlight::new);
        g("missiles", "Raketenabwehr", "projectile", 0xFFFF4D5E, "Beschütze die Städte",
                "Raketen fallen auf deine Städte. Klick in den Himmel: Deine Abwehrrakete explodiert dort und zerstört alles in der Nähe. Halte so lange durch, wie du kannst!", "Maus", true, MissileCommand::new);
        g("lander", "Mondlandung", "moon", 0xFF9AA3B2, "Lande sanft auf der grünen Plattform",
                "Gib Schub, um den Fall zu bremsen, und kipp die Fähre nach links oder rechts. Lande langsam, gerade und genau auf der Plattform. Treibstoff ist knapp!", "W/Leertaste Schub · A/D kippen", true, LunarLander::new);
        g("lightcycles", "Lichtrenner", "trail", 0xFF3AD8FF, "Schneide der KI den Weg ab",
                "Dein Lichtrenner zieht eine Mauer hinter sich her. Wer zuerst gegen eine Mauer fährt, verliert. Locke die KI in eine Falle!", "Pfeiltasten / WASD", true, LightCycles::new);
        g("airhockey", "Air Hockey", "hit", 0xFF006CB7, "Erster mit 7 Toren gewinnt",
                "Steuere deinen Schläger mit der Maus (untere Hälfte) und schieß die Scheibe ins obere Tor. Pass auf dein eigenes Tor auf!", "Maus", true, AirHockey::new);
        g("plinko", "Plinko", "star", 0xFF7A2AE0, "Lass die Kugeln fallen",
                "Klick oben, um eine Kugel fallen zu lassen. Sie springt durch die Stifte und landet in einem Fach - die Ränder geben die meisten Punkte. 10 Kugeln.", "Maus", true, Plinko::new);
        g("basketball", "Korbwurf", "target", 0xFFE8742A, "Triff so oft wie möglich in 45 s",
                "Zieh den Ball nach hinten und lass los - wie eine Schleuder. Nach jedem Korb wandert der Korb an eine neue Stelle. Weite Würfe geben 3 Punkte.", "Maus ziehen", true, Basketball::new);
        g("minigolf", "Minigolf", "flag", 0xFF3E9E4A, "6 Bahnen mit möglichst wenig Schlägen",
                "Zieh von der Kugel weg und lass los - je weiter, desto fester der Schlag. Spiel über die Banden und um die Hindernisse herum ins Loch.", "Maus ziehen", false, MiniGolf::new);
        g("archery", "Bogenschießen", "arrow", 0xFF8A5A30, "10 Pfeile, triff die Mitte",
                "Ziele mit der Maus (hoch/runter), halte die Maustaste gedrückt, um den Bogen zu spannen, und lass los. Achte auf den Wind!", "Maus halten + loslassen", true, Archery::new);
        g("darts", "Darts", "crosshair", 0xFF00852B, "9 Darts - so viele Punkte wie möglich",
                "Deine Hand zittert leicht - warte auf den richtigen Moment und klick. Der äußere Ring zählt doppelt, der innere dreifach, die Mitte 50!", "Maus", true, Darts::new);
        // ---- Wissen, Wörter, Karten
        g("pianotiles", "Klaviertasten", "music", 0xFF15161A, "Tippe nur die schwarzen Tasten",
                "Schwarze Tasten rutschen nach unten. Tippe immer die unterste - triffst du eine weiße oder verpasst eine, ist das Lied vorbei. Es wird immer schneller!", "Maus / D F J K", true, PianoTiles::new);
        g("colorwords", "Farb-Verwirrung", "palette", 0xFFE3000B, "Klick die FARBE, nicht das Wort",
                "Das Wort ROT kann blau geschrieben sein! Wähle immer die Farbe, in der das Wort geschrieben ist. 30 Sekunden - falsche Antworten kosten Punkte.", "Maus / 1-4", true, ColorWords::new);
        g("typing", "Tipp-Trainer", "keyboard", 0xFF3FA9FF, "Wie viele Wörter pro Minute?",
                "Tippe die angezeigten Wörter so schnell und genau wie möglich ab. Nach 60 Sekunden siehst du deine Wörter pro Minute.", "Tastatur", true, TypingTest::new);
        g("mathquiz", "Kopfrechnen", "number", 0xFFFFB02E, "Rechne schnell im Kopf",
                "Löse so viele Aufgaben wie möglich. Richtig gibt eine Sekunde extra, falsch kostet drei. Es wird nach und nach schwerer - auch mal und geteilt!", "Zahlen + Enter", true, MathQuiz::new);
        g("mcquiz", "Minecraft-Quiz", "info", 0xFF39D353, "10 Fragen zu Minecraft & Lego",
                "Wie gut kennst du dich aus? Beantworte 10 zufällige Fragen über Minecraft und Lego - wähle immer eine von vier Antworten.", "Maus / 1-4", true, McQuiz::new);
        g("anagram", "Buchstabensalat", "list", 0xFFFF6FB5, "Bring die Buchstaben in Ordnung",
                "Die Buchstaben eines Minecraft-Wortes sind durcheinander. Tippe das richtige Wort! Lange Wörter geben mehr Punkte. Du hast 90 Sekunden.", "Tastatur", true, Anagram::new);
        g("wordsearch", "Wortsuche", "search", 0xFF00A3DA, "Finde 7 versteckte Wörter",
                "Im Buchstabengitter verstecken sich Minecraft-Wörter - waagerecht, senkrecht oder schräg. Zieh mit der Maus über ein Wort, um es zu markieren.", "Maus ziehen", true, WordSearch::new);
        g("match3", "Edelstein-Tausch", "gem", 0xFFB15CFF, "Drei gleiche in eine Reihe",
                "Tausche zwei benachbarte Steine, damit drei oder mehr gleiche in einer Reihe stehen. Kettenreaktionen geben Combo-Punkte! 90 Sekunden.", "Maus", true, Match3::new);
        g("samegame", "Klötzchen-Klick", "brick", 0xFFFF8A00, "Große Gruppen geben viele Punkte",
                "Klick auf eine Gruppe aus mindestens 2 gleichen Steinen, um sie zu entfernen. Je größer die Gruppe, desto mehr Punkte. Räumst du alles ab, gibt es 1000 Bonus!", "Maus", true, SameGame::new);
        g("kniffel", "Kniffel", "gamepad", 0xFFE3000B, "Würfelspiel-Klassiker",
                "Würfle bis zu dreimal pro Runde, halte gute Würfel fest und trag das Ergebnis in eine der 13 Kategorien ein. Schaffst du oben 63 Punkte, gibt es 35 Bonus.", "Maus / Leertaste würfeln", true, Kniffel::new);
        g("higherlower", "Höher oder Tiefer", "arrow", 0xFF2E9E4A, "Rate die nächste Karte",
                "Ist die nächste Karte höher oder tiefer als die aktuelle? Wie lange hält deine Glückssträhne?", "Maus / Pfeil hoch/runter", true, HigherLower::new);
        g("maumau", "Mau-Mau", "list", 0xFF1E6B3A, "Karten loswerden gegen die KI",
                "Leg eine Karte mit gleicher Farbe oder gleichem Wert. 7 = der andere zieht 2, 8 = aussetzen, Bube = Farbe wünschen. Wer zuerst keine Karten mehr hat, gewinnt.", "Maus", true, MauMau::new);
        // ---- Solitär, Physik, Duelle
        g("klondike", "Solitär", "list", 0xFF1E6B3A, "Der Karten-Klassiker",
                "Sortiere alle Karten nach Farbe von Ass bis König auf die vier Ablagen oben. Im Spielfeld legst du absteigend und abwechselnd rot/schwarz. Klick = auswählen, Ziel anklicken = ablegen, Rechtsklick = direkt auf die Ablage.", "Maus", true, Klondike::new);
        g("numbermemory", "Zahlengedächtnis", "number", 0xFF9B4DCA, "Merk dir immer längere Zahlen",
                "Eine Zahl erscheint kurz - tippe sie danach aus dem Kopf ein. Jede Runde kommt eine Ziffer dazu. Wie viele Ziffern schaffst du?", "Zahlen + Enter", true, NumberMemory::new);
        g("pinball", "Flipper", "target", 0xFF7A2AE0, "Halte die Kugel im Spiel",
                "Benutze die Flipper, damit die Kugel nicht nach unten fällt, und triff die Bumper für Punkte. Du hast 3 Kugeln.", "A/D oder Pfeile · Leertaste beide", true, Pinball::new);
        g("cannons", "Kanonen-Duell", "projectile", 0xFF8A5A30, "Triff die gegnerische Kanone",
                "Stell Winkel und Kraft ein und feuere über den Berg. Achte auf den Wind! Wer zuerst 3 Treffer landet, gewinnt.", "W/S Winkel · A/D Kraft · Leertaste Feuer", true, CannonDuel::new);
        g("catapult", "Katapult", "boost", 0xFFE3000B, "Schieß die Creeper von den Türmen",
                "Zieh die Kugel in der Schleuder zurück und lass los. Triff die Creeper oder lass die Türme auf sie stürzen. 3 Schüsse pro Level.", "Maus ziehen", true, Catapult::new);
        g("dodge", "Ausweichen", "run", 0xFF3FA9FF, "Weiche den fallenden Steinen aus",
                "Steuere deinen Stein mit der Maus und weiche allem aus, was von oben fällt. Wie viele Sekunden überlebst du?", "Maus", true, Dodge::new);
        g("colorswitch", "Farbwechsel", "palette", 0xFF9B4DFF, "Nur durch die passende Farbe",
                "Spring mit dem Ball durch die drehenden Ringe - aber nur durch den Teil mit deiner Farbe! Die bunten Kugeln ändern deine Farbe.", "Klick / Leertaste", true, ColorSwitch::new);
        g("balloonpop", "Ballon-Platzen", "gift", 0xFFFF6FB5, "Lass die Ballons platzen",
                "Klick die aufsteigenden Ballons an. Goldene geben 50 Punkte, schwarze Bomben kosten 30. 45 Sekunden!", "Maus", true, BalloonPop::new);
        g("fishing", "Angeln", "rain", 0xFF2E86DE, "Zieh im richtigen Moment",
                "Wirf die Angel aus und warte. Sobald der Schwimmer zuckt, schnell klicken! Zu früh oder zu spät - und der Fisch ist weg. 60 Sekunden.", "Maus", true, Fishing::new);
        g("cookies", "Keks-Klicker", "food", 0xFFC8873A, "Backe so viele Kekse wie möglich",
                "Klick auf den Keks und kauf dir Omas, Bäckereien und Fabriken, die automatisch weiterbacken. Wie viele Kekse schaffst du in 2 Minuten?", "Maus", true, CookieClicker::new);
        g("ladders", "Leiterspiel", "jump", 0xFF8A5A30, "Leitern hoch, Schlangen runter",
                "Würfle abwechselnd mit der KI. Leitern bringen dich nach oben, Schlangen rutschen nach unten. Wer zuerst Feld 100 erreicht, gewinnt. Bei einer 6 darfst du nochmal.", "Maus / Leertaste", true, Ladders::new);
        g("tanks", "Panzer-Duell", "target", 0xFF006CB7, "Zerstöre den gegnerischen Panzer",
                "Fahr mit WASD, ziele mit der Maus und schieß mit Klick. Schüsse prallen einmal von Wänden ab. 3 Treffer gewinnen.", "WASD · Maus zielen · Klick schießen", true, TankDuel::new);
        g("bubbles", "Blasen-Shooter", "sparkle", 0xFF3AD8FF, "Drei gleiche Blasen platzen",
                "Schieß Blasen nach oben. Triffst du 3 oder mehr gleiche Farben, platzen sie - und alles, was daran hängt, fällt runter. Alle 7 Schüsse rückt die Decke nach.", "Maus zielen + Klick", true, BubbleShooter::new);
        // ---- Logik & Brett
        g("gravityrun", "Schwerkraft-Lauf", "bolt", 0xFF3AD8FF, "Dreh die Schwerkraft um",
                "Dein Stein rennt automatisch. Klick, um die Schwerkraft umzudrehen - dann läufst du an der Decke. Weiche den roten Blöcken oben und unten aus!", "Klick / Leertaste", true, GravityRun::new);
        g("ballmaze", "Kugel-Labyrinth", "compass", 0xFFC8A060, "Rolle die Kugel ins Ziel",
                "Neige das Brett mit der Maus: Die Kugel rollt in Richtung Mauszeiger. Bring sie ins grüne Ziel, ohne in ein Loch zu fallen. Jedes Level hat mehr Löcher.", "Maus", true, BallMaze::new);
        g("sokoban", "Kistenschieber", "box", 0xFFFF8A00, "Schieb alle Kisten auf die Punkte",
                "Schiebe die Kisten auf die grünen Zielpunkte. Du kannst Kisten nur schieben, nicht ziehen - pass auf, dass keine in einer Ecke stecken bleibt! R startet das Level neu.", "Pfeiltasten / WASD · R", true, Sokoban::new);
        g("nonogram", "Nonogramm", "grid", 0xFF9B4DCA, "Male das versteckte Muster",
                "Die Zahlen am Rand sagen, wie viele Felder in Folge gefüllt sind. Fülle die richtigen Felder - nach 3 Fehlern ist Schluss. Rechtsklick markiert ein leeres Feld.", "Linksklick füllen · Rechtsklick Kreuz", true, Nonogram::new);
        g("make24", "Rechne 24", "number", 0xFFFFB02E, "Aus 4 Zahlen genau 24 machen",
                "Klick eine Zahl, dann eine Rechenart, dann die zweite Zahl. Am Ende muss genau 24 übrig bleiben. Jede Aufgabe ist lösbar! 2 Minuten.", "Maus", true, Make24::new);
        g("snakeduel", "Schlangen-Duell", "trail", 0xFFE3000B, "Zwei Schlangen, ein Feld",
                "Du (blau) und die KI-Schlange (rot) jagen dieselben Steine. Wer gegen eine Wand, sich selbst oder die andere Schlange fährt, verliert.", "Pfeiltasten / WASD", true, SnakeDuel::new);
        g("squash", "Squash", "hit", 0xFF39D353, "Halte den Ball im Spiel",
                "Schlag den Ball mit deinem Schläger immer wieder gegen die Wand. Er wird mit jedem Treffer schneller.", "Maus", true, Squash::new);
        g("ski", "Abfahrtsski", "snow", 0xFF3FA9FF, "Rase den Berg hinunter",
                "Steuere mit der Maus um Bäume und Felsen. Fahr durch die Tore für Bonus. Bei drei Zusammenstößen ist die Fahrt vorbei.", "Maus", true, Ski::new);
        g("pipes", "Rohr-Puzzle", "wifi", 0xFF2E86DE, "Lass das Wasser überall hinfließen",
                "Dreh die Rohre per Klick, bis vom Wasserhahn links oben aus jedes Rohr Wasser bekommt. Wie viele Rätsel löst du?", "Maus · Enter = beenden", true, PipePuzzle::new);
        g("watersort", "Farb-Sortierer", "flask", 0xFFFF4FA3, "Jede Farbe in ihr eigenes Glas",
                "Klick ein Glas und dann ein anderes, um die oberste Farbe umzugießen. Es geht nur auf die gleiche Farbe oder in ein leeres Glas. Sortiere alles!", "Maus · R neu · Enter beenden", true, WaterSort::new);
        g("checkers", "Dame", "crown", 0xFF8A5A30, "Der Brettspiel-Klassiker gegen die KI",
                "Zieh schräg nach vorne und überspring gegnerische Steine, um sie zu schlagen. Schlagen ist Pflicht! Wer die letzte Reihe erreicht, wird zur Dame.", "Maus", true, Checkers::new);
        g("mill", "Mühle", "grid", 0xFFC8A060, "Bilde Mühlen und nimm Steine",
                "Setze abwechselnd 9 Steine, dann zieht ihr sie. Drei in einer Linie sind eine Mühle - dann darfst du einen Stein der KI nehmen. Wer nur noch 2 Steine hat, verliert.", "Maus", true, Mill::new);
        // ---- Sport, Reaktion, Gedächtnis
        g("timesense", "Zeitgefühl", "hourglass", 0xFF9B4DCA, "Stoppe genau nach X Sekunden",
                "Klick zum Starten und klick nochmal, wenn du denkst, dass genau die angezeigte Zeit vorbei ist - ganz ohne Uhr! 5 Runden.", "Klick / Leertaste", true, TimeSense::new);
        g("penalty", "Elfmeter", "target", 0xFF2E9E4A, "10 Elfmeter gegen den Torwart",
                "Ziele mit der Maus, halte die Taste für die Schusskraft und lass los. Zu fest geschossen fliegt der Ball drüber! Die Stärke des Torwarts kannst du wählen.", "Maus halten + loslassen", true, Penalty::new);
        g("bowling", "Bowling", "target", 0xFF1A2A6A, "Räum alle 10 Kegel ab",
                "Verschiebe die Kugel mit A/D und zieh mit der Maus nach oben - seitliches Ziehen gibt Effet. 10 Durchgänge mit je 2 Würfen.", "A/D · Maus ziehen", true, Bowling::new);
        g("billiards", "Billard", "target", 0xFF1E7A3A, "Versenke alle 9 Kugeln",
                "Zieh von der weißen Kugel weg und lass los. Versenke alle bunten Kugeln mit höchstens 15 Stößen - aber nicht die weiße!", "Maus ziehen", true, Billiards::new);
        g("starfighter", "Sternenjäger", "star", 0xFF7CF7FF, "Weltraum-Shooter",
                "Steuere dein Raumschiff mit der Maus - es schießt automatisch. Weiche den roten Schüssen aus. Lila Gegner brauchen drei Treffer.", "Maus", true, StarFighter::new);
        g("lilyhop", "Seerosen-Sprung", "jump", 0xFF39D353, "Spring von Blatt zu Blatt",
                "Halte die Maustaste gedrückt, um die Sprungweite aufzuladen, und lass los. Triff das nächste Seerosenblatt - sonst landet der Frosch im Wasser!", "Maus halten + loslassen", true, LilyHop::new);
        g("keepyuppy", "Kopfball", "hit", 0xFF8AC8FF, "Halte den Ball in der Luft",
                "Klick auf den Ball, um ihn hochzukicken. Wo du ihn triffst, bestimmt die Richtung. Lass ihn nicht auf den Boden fallen!", "Maus", true, KeepyUppy::new);
        g("numberhunt", "Zahlen-Jagd", "number", 0xFF00A3DA, "Klick 1 bis 25 so schnell du kannst",
                "Die Zahlen 1 bis 25 sind durcheinander. Klick sie der Reihe nach an - je schneller, desto besser. Trainiert deinen Blick!", "Maus", false, NumberHunt::new);
        g("patternmemory", "Muster-Gedächtnis", "eye", 0xFF3FA9FF, "Merk dir die blauen Felder",
                "Einige Felder leuchten kurz auf. Merk sie dir und klick sie danach an. Jedes Level werden es mehr. 3 Leben.", "Maus", true, PatternMemory::new);
        g("highway", "Autobahn", "car", 0xFFF4F4F4, "Weiche dem Verkehr aus",
                "Wechsle mit den Pfeiltasten die Spur und überhole die langsameren Autos. Es wird immer schneller!", "Pfeiltasten / A D / Klick", true, Highway::new);
        g("goldminer", "Goldgräber", "gem", 0xFFFFD23F, "Angle Gold und Diamanten",
                "Der Haken schwingt hin und her. Klick, um ihn auszuwerfen. Große Goldklumpen sind schwer und langsam, Diamanten leicht und wertvoll. Erreiche das Geldziel!", "Klick / Leertaste", true, GoldMiner::new);
        g("iceslide", "Eisrutsche", "snow", 0xFF9FD8FF, "Rutsch den Pinguin ins Ziel",
                "Auf dem Eis rutscht der Pinguin, bis er gegen einen Stein stößt. Finde den Weg zum grünen Haus - mit möglichst wenigen Zügen.", "Pfeiltasten / WASD · R", true, IceSlide::new);
    }
}
