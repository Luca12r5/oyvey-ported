package dev.lego.games;

import java.util.ArrayList;
import java.util.List;

/** All Solar Smash tools (data only). Every tool = one effect engine + its own look and strength. */
final class SolarTools {
    private SolarTools() { }

    enum Kind { BEAM, SHOT, RAIN, SWARM, AREA, GLOBAL, HOLE, SPLIT }

    static final String[] CATS = {"Laser", "Meteore", "Aliens", "Natur", "Kosmos", "Sci-Fi", "Spaß", "Weihnachten", "Physik", "Apokalypse"};
    static final String[] CAT_ICONS = {"bolt", "gem", "eye", "flame", "moon", "chip", "heart", "gift", "target", "skull"};

    static final class Tool {
        final int cat;
        final String name, desc;
        final Kind kind;
        final int color;
        /** crater radius (texels of a 2048 map), damage depth, heat, share of people lost inside, scorch ring */
        float r, depth, heat, kill, scorch;
        int style = SolarPlanet.S_BURN;
        /** projectile count / ships, speed, duration, beam width, sprite (fun kind / ufo kind / -1 rock) */
        int n = 1, sprite = -1;
        float speed = 1, dur = 1, width = 1;
        String sound = "minecraft:entity.generic.explode";

        Tool(int cat, String name, Kind kind, int color, String desc) { this.cat = cat; this.name = name; this.kind = kind; this.color = color; this.desc = desc; }
        Tool dmg(float r, float depth, float heat, float kill) { this.r = r; this.depth = depth; this.heat = heat; this.kill = kill; this.scorch = 1.8f; return this; }
        Tool style(int s) { style = s; return this; }
        Tool n(int n) { this.n = n; return this; }
        Tool speed(float s) { speed = s; return this; }
        Tool dur(float d) { dur = d; return this; }
        Tool width(float w) { width = w; return this; }
        Tool sprite(int s) { sprite = s; return this; }
        Tool scorch(float s) { scorch = s; return this; }
        Tool snd(String s) { sound = s; return this; }
    }

    static final List<Tool> ALL = new ArrayList<>();

    static List<Tool> of(int cat) {
        List<Tool> out = new ArrayList<>();
        for (Tool t : ALL) if (t.cat == cat) out.add(t);
        return out;
    }

    private static Tool add(Tool t) { ALL.add(t); return t; }

    private static final String LASER = "minecraft:block.beacon.power_select", BOOM = "minecraft:entity.generic.explode",
            WHOOSH = "minecraft:entity.firework_rocket.launch", UFO = "minecraft:block.beacon.ambient", MAGIC = "minecraft:block.enchantment_table.use",
            ICE = "minecraft:block.glass.break", SPLASH = "minecraft:entity.player.splash.high_speed", FUNNY = "minecraft:entity.chicken.egg",
            THUNDER = "minecraft:entity.lightning_bolt.thunder", BELL = "minecraft:block.bell.use", DEEP = "minecraft:entity.warden.sonic_boom";

    static {
        final int B = SolarPlanet.S_BURN, F = SolarPlanet.S_FIRE, I = SolarPlanet.S_ICE, GO = SolarPlanet.S_GOLD, TX = SolarPlanet.S_TOXIC,
                CR = SolarPlanet.S_CRYSTAL, GOO = SolarPlanet.S_GOO, FL = SolarPlanet.S_FLOOD, SA = SolarPlanet.S_SAND, CM = SolarPlanet.S_CREAM,
                CO = SolarPlanet.S_CONFETTI, LV = SolarPlanet.S_LAVA, AN = SolarPlanet.S_ANNI, SH = SolarPlanet.S_SHAKE, EMP = SolarPlanet.S_EMP,
                LG = SolarPlanet.S_LEGO, COW = SolarPlanet.S_COW, PZ = SolarPlanet.S_PIZZA, DN = SolarPlanet.S_DONUT, PT = SolarPlanet.S_PAINT,
                RF = SolarPlanet.S_RIFT, PI = SolarPlanet.S_PIANO;
        final Kind BM = Kind.BEAM, SHT = Kind.SHOT, RN = Kind.RAIN, SW = Kind.SWARM, AR = Kind.AREA, GL = Kind.GLOBAL, HO = Kind.HOLE, SP = Kind.SPLIT;

        // ---------------------------------------------------------------- 0 Laser (16)
        add(new Tool(0, "Dauerlaser", BM, 0xFFFF3048, "Klassischer roter Laser – halten zum Brennen")).dmg(9, 22, 120, 0.5f).width(1).snd(LASER);
        add(new Tool(0, "Pulslaser", BM, 0xFF3CFF7A, "Schnelle grüne Pulse")).dmg(11, 30, 90, 0.5f).width(0.8f).speed(3).snd(LASER);
        add(new Tool(0, "Blauer Schneider", BM, 0xFF3FA8FF, "Dünn, heiß, präzise")).dmg(6, 30, 170, 0.6f).width(0.5f).snd(LASER);
        add(new Tool(0, "Violetter Strahl", BM, 0xFFB35CFF, "Breiter Energiestrahl")).dmg(15, 26, 110, 0.55f).width(1.8f).snd(LASER);
        add(new Tool(0, "Goldstrahl", BM, 0xFFFFC94A, "Verwandelt die Oberfläche in Gold")).dmg(14, 10, 40, 0.3f).style(GO).width(1.4f).snd(MAGIC);
        add(new Tool(0, "Eisstrahl", BM, 0xFF9FE8FF, "Friert alles ein")).dmg(16, 8, 0, 0.4f).style(I).width(1.5f).snd(ICE);
        add(new Tool(0, "Todesstrahl", BM, 0xFFFF1030, "Riesiger Superlaser")).dmg(34, 60, 255, 0.9f).width(4f).snd(DEEP);
        add(new Tool(0, "Planetenbohrer", BM, 0xFFFF8A2A, "Bohrt tief bis zum Kern")).dmg(7, 90, 255, 0.8f).style(LV).width(1.2f).snd(LASER);
        add(new Tool(0, "Regenbogenlaser", BM, 0xFFFF5CD8, "Wechselt ständig die Farbe")).dmg(12, 24, 100, 0.5f).style(PT).width(1.3f).snd(MAGIC);
        add(new Tool(0, "Giftlaser", BM, 0xFF9CFF3C, "Hinterlässt giftige Flecken")).dmg(13, 14, 40, 0.7f).style(TX).width(1.2f).snd(LASER);
        add(new Tool(0, "Kristalllaser", BM, 0xFFBFF4FF, "Lässt Kristalle wachsen")).dmg(13, 16, 30, 0.4f).style(CR).width(1.2f).snd(MAGIC);
        add(new Tool(0, "Doppellaser", BM, 0xFFFF6040, "Zwei Strahlen gleichzeitig")).dmg(10, 26, 130, 0.55f).width(1).n(2).snd(LASER);
        add(new Tool(0, "Laser-Fächer", BM, 0xFFFF4A6A, "Fünf Strahlen in einer Reihe")).dmg(8, 20, 110, 0.5f).width(0.7f).n(5).snd(LASER);
        add(new Tool(0, "Schmelzstrahl", BM, 0xFFFFB02E, "Macht Gestein zu Lava")).dmg(18, 20, 255, 0.7f).style(LV).width(2).snd(LASER);
        add(new Tool(0, "Antimaterie-Strahl", BM, 0xFFEAF6FF, "Löscht Materie einfach aus")).dmg(20, 80, 60, 0.95f).style(AN).width(2.4f).snd(DEEP);
        add(new Tool(0, "Mini-Laser", BM, 0xFFFF80A0, "Zum Zeichnen auf dem Planeten")).dmg(4, 12, 90, 0.2f).width(0.35f).snd(LASER);

        // ---------------------------------------------------------------- 1 Meteore (14)
        add(new Tool(1, "Kleiner Meteor", SHT, 0xFFB08A6A, "Ein Stein aus dem All")).dmg(18, 60, 120, 0.8f).speed(1.3f);
        add(new Tool(1, "Großer Asteroid", SHT, 0xFF8A7A6A, "Ein richtiger Brocken")).dmg(60, 140, 200, 0.95f).speed(0.8f).scorch(2.4f);
        add(new Tool(1, "Planetenkiller", SHT, 0xFF6A5A4A, "Ein Asteroid, halb so groß wie der Mond")).dmg(160, 255, 255, 1f).speed(0.5f).scorch(2.2f).snd(DEEP);
        add(new Tool(1, "Eiskomet", SHT, 0xFFBFE8FF, "Komet mit glitzerndem Schweif")).dmg(34, 80, 60, 0.85f).style(I).speed(1.5f).snd(ICE);
        add(new Tool(1, "Eisenmeteor", SHT, 0xFF9AA0AA, "Superschwer und schnell")).dmg(26, 170, 200, 0.95f).speed(2.2f);
        add(new Tool(1, "Glühender Meteor", SHT, 0xFFFF6A2A, "Brennt schon im Anflug")).dmg(32, 90, 255, 0.9f).style(F).speed(1.2f);
        add(new Tool(1, "Meteorschauer", RN, 0xFFB08A6A, "Viele kleine Meteore")).dmg(12, 50, 120, 0.75f).n(24).dur(3).speed(1.4f);
        add(new Tool(1, "Kometenregen", RN, 0xFF9FE0FF, "Eiskometen im Dauerfeuer")).dmg(16, 50, 40, 0.7f).style(I).n(18).dur(3).speed(1.6f).snd(ICE);
        add(new Tool(1, "Lava-Brocken", RN, 0xFFFF5A1A, "Glühende Felsen regnen herab")).dmg(18, 60, 255, 0.85f).style(LV).n(14).dur(2.5f);
        add(new Tool(1, "Kristallasteroid", SHT, 0xFFB8F0FF, "Zerspringt in Kristalle")).dmg(40, 70, 40, 0.8f).style(CR).speed(1.1f).snd(ICE);
        add(new Tool(1, "Goldasteroid", SHT, 0xFFFFD34A, "Reich, aber tödlich")).dmg(38, 70, 60, 0.7f).style(GO).speed(1).snd(BELL);
        add(new Tool(1, "Mondsturz", SHT, 0xFFD8D4CC, "Ein ganzer Mond stürzt ab")).dmg(230, 255, 255, 1f).speed(0.35f).scorch(1.8f).snd(DEEP);
        add(new Tool(1, "Staubwolke", RN, 0xFFC8B08A, "Hunderte winzige Einschläge")).dmg(6, 30, 60, 0.4f).style(SA).n(60).dur(4).speed(2);
        add(new Tool(1, "Doppelasteroid", RN, 0xFF8A7A6A, "Zwei Brocken auf einmal")).dmg(44, 120, 200, 0.95f).n(2).dur(0.5f).speed(0.9f);

        // ---------------------------------------------------------------- 2 Aliens (13)
        add(new Tool(2, "UFO-Schwarm", SW, 0xFF6CFF9C, "Kleine UFOs schießen Laser")).dmg(7, 26, 110, 0.5f).n(8).dur(9).sprite(0).snd(UFO);
        add(new Tool(2, "Mutterschiff", SW, 0xFFB35CFF, "Ein riesiges Schiff mit Todesstrahl")).dmg(28, 60, 200, 0.85f).n(1).dur(10).sprite(1).width(3).snd(DEEP);
        add(new Tool(2, "Entführungsstrahl", SW, 0xFF9CFFE8, "UFOs entführen Bewohner")).dmg(12, 2, 0, 0.9f).style(EMP).n(5).dur(10).sprite(4).snd(UFO);
        add(new Tool(2, "Alien-Invasion", SW, 0xFF6CFF9C, "Eine ganze Flotte")).dmg(9, 30, 130, 0.6f).n(22).dur(12).sprite(0).snd(UFO);
        add(new Tool(2, "Kampfläufer", RN, 0xFF8AFF6C, "Riesige Roboter landen und schießen")).dmg(16, 30, 90, 0.7f).style(B).n(6).dur(4).sprite(-2).speed(0.7f);
        add(new Tool(2, "Glibber-Aliens", SW, 0xFFB0FF3C, "Überziehen alles mit Schleim")).dmg(14, 10, 0, 0.6f).style(GOO).n(6).dur(10).sprite(3).snd(UFO);
        add(new Tool(2, "Kristall-Aliens", SW, 0xFFBFF4FF, "Verwandeln die Welt in Kristall")).dmg(13, 12, 20, 0.5f).style(CR).n(7).dur(10).sprite(0).snd(MAGIC);
        add(new Tool(2, "Terraformer", SW, 0xFFFFA84A, "Macht den Planeten zur Wüste")).dmg(20, 8, 40, 0.5f).style(SA).n(4).dur(12).sprite(2).width(2).snd(UFO);
        add(new Tool(2, "Pulsar-Kreuzer", SW, 0xFFFF4A8A, "Schnelle Pulse aus der Umlaufbahn")).dmg(9, 34, 150, 0.6f).n(3).dur(8).sprite(3).speed(3).snd(LASER);
        add(new Tool(2, "Alien-Bohrschiff", SW, 0xFFFF8A2A, "Bohrt Löcher bis zum Kern")).dmg(8, 110, 255, 0.8f).style(LV).n(2).dur(10).sprite(2).snd(DEEP);
        add(new Tool(2, "Eis-Aliens", SW, 0xFF9FE8FF, "Bringen eine Eiszeit")).dmg(22, 6, 0, 0.5f).style(I).n(5).dur(10).sprite(3).snd(ICE);
        add(new Tool(2, "Gift-Flotte", SW, 0xFF9CFF3C, "Verseuchen ganze Kontinente")).dmg(16, 8, 20, 0.8f).style(TX).n(8).dur(11).sprite(0).snd(UFO);
        add(new Tool(2, "Stromausfall-UFOs", SW, 0xFF7CE0FF, "Schalten alle Lichter aus")).dmg(18, 0, 0, 0.05f).style(EMP).n(6).dur(8).sprite(4).snd(UFO);

        // ---------------------------------------------------------------- 3 Natur (13)
        add(new Tool(3, "Vulkanausbruch", AR, 0xFFFF5A1A, "Lava bricht aus dem Boden")).dmg(40, 70, 255, 0.8f).style(LV).dur(3).snd(BOOM);
        add(new Tool(3, "Supervulkan", AR, 0xFFFF3A0A, "Ein Kontinent versinkt in Lava")).dmg(110, 90, 255, 0.95f).style(LV).dur(5).snd(DEEP);
        add(new Tool(3, "Erdbeben", SP, 0xFFB08A6A, "Risse ziehen sich über den Boden")).dmg(0, 60, 60, 0.6f).style(SH).width(1).snd(THUNDER);
        add(new Tool(3, "Megabeben", SP, 0xFF8A6A4A, "Der Planet bekommt tiefe Spalten")).dmg(0, 120, 180, 0.8f).style(SH).width(2.2f).snd(DEEP);
        add(new Tool(3, "Tsunami", AR, 0xFF3F9CFF, "Riesige Flutwelle")).dmg(70, 8, 0, 0.7f).style(FL).dur(3).snd(SPLASH);
        add(new Tool(3, "Sintflut", GL, 0xFF3F9CFF, "Der Meeresspiegel steigt überall")).dmg(0, 0, 0, 0.35f).style(FL).dur(6).snd(SPLASH);
        add(new Tool(3, "Eiszeit", GL, 0xFFBFE8FF, "Der ganze Planet friert ein")).dmg(0, 0, 0, 0.3f).style(I).dur(6).snd(ICE);
        add(new Tool(3, "Hitzewelle", GL, 0xFFFF8A3A, "Die Temperatur steigt und steigt")).dmg(0, 0, 0, 0.25f).style(F).dur(6).snd(BOOM);
        add(new Tool(3, "Waldbrand", AR, 0xFFFF7A2A, "Feuer breitet sich aus")).dmg(60, 12, 200, 0.5f).style(F).dur(4).snd(BOOM);
        add(new Tool(3, "Sandsturm", AR, 0xFFE0C08A, "Alles wird unter Sand begraben")).dmg(80, 6, 20, 0.4f).style(SA).dur(4).snd(WHOOSH);
        add(new Tool(3, "Blitzgewitter", RN, 0xFFE8F0FF, "Tausende Blitze")).dmg(5, 20, 150, 0.3f).style(B).n(50).dur(4).speed(9).snd(THUNDER);
        add(new Tool(3, "Sonnensturm", GL, 0xFFFFD06A, "Ein Plasma-Ausbruch der Sonne")).dmg(0, 0, 0, 0.2f).style(EMP).dur(5).snd(DEEP);
        add(new Tool(3, "Giftiger Nebel", AR, 0xFF9CFF3C, "Eine grüne Wolke zieht übers Land")).dmg(70, 4, 10, 0.8f).style(TX).dur(4).snd(WHOOSH);

        // ---------------------------------------------------------------- 4 Kosmos (12)
        add(new Tool(4, "Schwarzes Loch", HO, 0xFF1A0A2A, "Verschlingt den Planeten langsam")).dmg(0, 0, 0, 1f).dur(14).width(1).snd(DEEP);
        add(new Tool(4, "Mini-Loch", HO, 0xFF2A1A3A, "Klein, aber gierig")).dmg(0, 0, 0, 1f).dur(10).width(0.5f).snd(DEEP);
        add(new Tool(4, "Weißes Loch", HO, 0xFFFFFFFF, "Spuckt Energie statt sie zu fressen")).dmg(0, 0, 0, 1f).dur(10).width(0.8f).snd(MAGIC);
        add(new Tool(4, "Wurmloch", AR, 0xFFB35CFF, "Reißt ein Stück aus der Oberfläche")).dmg(60, 200, 40, 1f).style(RF).dur(2.5f).snd(DEEP);
        add(new Tool(4, "Gammablitz", GL, 0xFFE0D0FF, "Ein Strahlungsblitz aus dem All")).dmg(0, 0, 0, 0.7f).style(EMP).dur(3).snd(DEEP);
        add(new Tool(4, "Supernova-Welle", GL, 0xFFFFE08A, "Eine Sternexplosion in der Nähe")).dmg(0, 0, 0, 0.9f).style(F).dur(4).snd(DEEP);
        add(new Tool(4, "Antimaterie-Kugel", SHT, 0xFFEAF6FF, "Löscht alles im Umkreis aus")).dmg(90, 255, 40, 1f).style(AN).speed(1.1f).snd(DEEP);
        add(new Tool(4, "Neutronenstern", SHT, 0xFFBFD8FF, "Unglaublich dicht und schwer")).dmg(120, 255, 255, 1f).speed(0.6f).snd(DEEP);
        add(new Tool(4, "Zeitriss", SP, 0xFFB35CFF, "Ein leuchtender Riss in der Zeit")).dmg(0, 160, 40, 0.9f).style(RF).width(1.6f).snd(MAGIC);
        add(new Tool(4, "Gravitationshammer", SP, 0xFF8AA8FF, "Zerbricht die Kruste")).dmg(0, 200, 120, 0.9f).style(SH).width(3f).snd(DEEP);
        add(new Tool(4, "Planetenspalter", SP, 0xFFFF4A2A, "Spaltet den Planeten in zwei Hälften")).dmg(0, 255, 255, 1f).style(LV).width(5f).snd(DEEP);
        add(new Tool(4, "Sternenstaub", RN, 0xFFFFF0B0, "Glitzernder Regen aus dem All")).dmg(5, 10, 30, 0.1f).style(GO).n(80).dur(4).speed(2).snd(MAGIC);

        // ---------------------------------------------------------------- 5 Sci-Fi (12)
        add(new Tool(5, "Plasmakanone", SHT, 0xFF3CE0FF, "Eine Kugel aus reinem Plasma")).dmg(40, 100, 255, 0.9f).speed(2).snd(LASER);
        add(new Tool(5, "Railgun", SHT, 0xFF8AD8FF, "Extrem schnelles Projektil")).dmg(14, 200, 200, 0.95f).speed(5).snd(LASER);
        add(new Tool(5, "Orbitalkanone", RN, 0xFFFFB04A, "Salven aus der Umlaufbahn")).dmg(20, 90, 200, 0.9f).n(8).dur(2).speed(3).snd(BOOM);
        add(new Tool(5, "Nanoschwarm", AR, 0xFF9AA0AA, "Winzige Roboter fressen alles")).dmg(90, 30, 0, 0.9f).style(GOO).dur(6).snd(WHOOSH);
        add(new Tool(5, "EMP-Welle", GL, 0xFF7CE0FF, "Alle Lichter gehen aus")).dmg(0, 0, 0, 0.02f).style(EMP).dur(2).snd(THUNDER);
        add(new Tool(5, "Kristallvirus", AR, 0xFFBFF4FF, "Kristalle wachsen über den Planeten")).dmg(100, 20, 10, 0.6f).style(CR).dur(6).snd(MAGIC);
        add(new Tool(5, "Stern-Vernichter", BM, 0xFF6CFF6C, "Eine original Super-Station schießt")).dmg(70, 255, 255, 1f).width(6).snd(DEEP);
        add(new Tool(5, "Ionen-Sturm", RN, 0xFF7CA8FF, "Blaue Energieblitze")).dmg(10, 40, 160, 0.5f).n(30).dur(3).speed(6).snd(THUNDER);
        add(new Tool(5, "Singularitätsbombe", SHT, 0xFF2A1A4A, "Implodiert statt zu explodieren")).dmg(130, 255, 60, 1f).style(AN).speed(0.8f).snd(DEEP);
        add(new Tool(5, "Drohnenschwarm", SW, 0xFFFF6A3A, "Hunderte kleine Drohnen")).dmg(5, 20, 90, 0.4f).n(30).dur(9).sprite(5).speed(2).snd(UFO);
        add(new Tool(5, "Gravitationsbombe", SHT, 0xFF8A6AFF, "Presst die Kruste zusammen")).dmg(70, 180, 60, 0.95f).style(SH).speed(1).snd(DEEP);
        add(new Tool(5, "Frostbombe", SHT, 0xFFBFE8FF, "Friert einen Kontinent ein")).dmg(90, 40, 0, 0.6f).style(I).speed(1.2f).snd(ICE);

        // ---------------------------------------------------------------- 6 Spaß (13)
        add(new Tool(6, "Riesen-Lego-Stein", SHT, 0xFFE3000B, "Der größte Lego-Stein der Welt")).dmg(60, 120, 20, 0.9f).style(LG).sprite(0).speed(0.9f).snd(FUNNY);
        add(new Tool(6, "Lego-Regen", RN, 0xFFFFCD03, "Bunte Steine überall")).dmg(18, 50, 10, 0.6f).style(LG).sprite(0).n(20).dur(3).snd(FUNNY);
        add(new Tool(6, "Kuh-Meteor", SHT, 0xFFF4F1EA, "Muuuh!")).dmg(40, 80, 10, 0.7f).style(COW).sprite(1).speed(0.9f).snd("minecraft:entity.cow.ambient");
        add(new Tool(6, "Riesentorte", SHT, 0xFFFFD0E0, "Alles voller Sahne")).dmg(70, 20, 0, 0.5f).style(CM).sprite(2).speed(0.8f).snd(SPLASH);
        add(new Tool(6, "Quietscheente", SHT, 0xFFFFD34A, "Eine gigantische Gummiente")).dmg(50, 60, 0, 0.6f).style(FL).sprite(3).speed(0.8f).snd(FUNNY);
        add(new Tool(6, "Bowlingkugel", SHT, 0xFF3A3A5A, "Strike!")).dmg(46, 160, 30, 0.9f).sprite(4).speed(1.3f).snd(BOOM);
        add(new Tool(6, "Pizza-Meteor", SHT, 0xFFFFB04A, "Mit extra Käse")).dmg(55, 60, 60, 0.7f).style(PZ).sprite(5).speed(0.9f).snd(FUNNY);
        add(new Tool(6, "Donut-Regen", RN, 0xFFFF8AC0, "Süß und zerstörerisch")).dmg(22, 40, 10, 0.5f).style(DN).sprite(6).n(14).dur(3).snd(FUNNY);
        add(new Tool(6, "Fallender Flügel", SHT, 0xFF1A1A1A, "Ein Konzertflügel aus dem All")).dmg(44, 110, 10, 0.8f).style(PI).sprite(7).speed(1).snd("minecraft:block.note_block.harp");
        add(new Tool(6, "Riesen-Minifigur", SHT, 0xFFFFCD03, "Eine Lego-Figur so groß wie ein Land")).dmg(70, 120, 20, 0.85f).style(LG).sprite(8).speed(0.7f).snd(FUNNY);
        add(new Tool(6, "Gummibärchen-Regen", RN, 0xFFFF4A6A, "Bunt, klebrig, gefährlich")).dmg(16, 30, 0, 0.4f).style(GOO).sprite(9).n(24).dur(3).snd(FUNNY);
        add(new Tool(6, "Discokugel", SHT, 0xFFE8E8FF, "Party auf dem ganzen Planeten")).dmg(50, 70, 20, 0.6f).style(CO).sprite(10).speed(0.9f).snd("minecraft:block.note_block.bell");
        add(new Tool(6, "Farbbombe", SHT, 0xFFFF5CD8, "Malt den Planeten bunt an")).dmg(80, 4, 0, 0.1f).style(PT).speed(1.2f).snd(SPLASH);

        // ---------------------------------------------------------------- 7 Weihnachten (11)
        add(new Tool(7, "Riesen-Schneeball", SHT, 0xFFF4F8FF, "Ein Schneeball so groß wie ein Mond")).dmg(80, 60, 0, 0.7f).style(I).speed(1).snd(ICE);
        add(new Tool(7, "Schneesturm", AR, 0xFFE8F2FF, "Alles versinkt im Schnee")).dmg(110, 4, 0, 0.35f).style(I).dur(5).snd(WHOOSH);
        add(new Tool(7, "Geschenke-Regen", RN, 0xFFE3263B, "Pakete fallen vom Himmel")).dmg(20, 40, 10, 0.5f).style(CO).sprite(-3).n(20).dur(3).snd(BELL);
        add(new Tool(7, "Zuckerstangen-Laser", BM, 0xFFFF3A4E, "Rot-weiß gestreift")).dmg(12, 26, 120, 0.5f).style(PT).width(1.4f).snd(LASER);
        add(new Tool(7, "Lebkuchen-Meteor", SHT, 0xFFB5733A, "Knusprig und hart")).dmg(46, 90, 60, 0.8f).style(SA).speed(1).snd(FUNNY);
        add(new Tool(7, "Rentier-Staffel", SW, 0xFFB08A5A, "Fliegende Rentiere werfen Geschenke")).dmg(12, 30, 10, 0.4f).style(CO).n(9).dur(9).sprite(6).snd(BELL);
        add(new Tool(7, "Eiszapfen-Regen", RN, 0xFFBFE8FF, "Spitze Eiszapfen")).dmg(10, 60, 0, 0.5f).style(I).n(36).dur(3).speed(2.5f).snd(ICE);
        add(new Tool(7, "Weihnachtsstern", SHT, 0xFFFFD34A, "Ein strahlender Stern schlägt ein")).dmg(70, 90, 255, 0.8f).style(GO).speed(0.8f).snd(BELL);
        add(new Tool(7, "Lichterketten-Strahl", BM, 0xFFFFC94A, "Bunte Lichter brennen sich ein")).dmg(10, 18, 90, 0.4f).style(CO).width(1).n(3).snd(BELL);
        add(new Tool(7, "Schneemann-Armee", RN, 0xFFF4F8FF, "Schneemänner landen überall")).dmg(14, 20, 0, 0.4f).style(I).sprite(-4).n(16).dur(4).speed(0.8f).snd(ICE);
        add(new Tool(7, "Kohle-Regen", RN, 0xFF2A2A2A, "Für alle, die unartig waren")).dmg(12, 50, 120, 0.6f).style(B).n(30).dur(3).speed(1.8f).snd(BOOM);
        // ---------------------------------------------------------------- 8 Physik (20)
        add(new Tool(8, "Railgun", SHT, 0xFF7FD8FF, "Ein Projektil mit Überschall, schlägt sofort ein")).dmg(14, 140, 200, 0.7f).speed(4f).snd(DEEP);
        add(new Tool(8, "Gauss-Salve", RN, 0xFF9FE0FF, "Viele kleine Hochgeschwindigkeits-Geschosse")).dmg(7, 70, 160, 0.5f).n(28).dur(2).speed(3f).snd(LASER);
        add(new Tool(8, "Kinetischer Stab", SHT, 0xFFC8C8D0, "Wolfram-Stab aus dem Orbit (\"Rods from God\")")).dmg(26, 200, 220, 0.9f).speed(3f).snd(BOOM);
        add(new Tool(8, "Mond-Crash", SHT, 0xFFBDBDBD, "Ein ganzer Mond stürzt ab")).dmg(170, 255, 255, 1f).style(LV).speed(0.5f).sprite(-1).snd(DEEP);
        add(new Tool(8, "Planetenkollision", SHT, 0xFFB57A4A, "Ein zweiter Planet rammt diesen")).dmg(260, 255, 255, 1f).style(LV).speed(0.4f).sprite(-1).snd(DEEP);
        add(new Tool(8, "Asteroiden-Gürtel", RN, 0xFF9A8A7A, "Hunderte Gesteinsbrocken")).dmg(12, 60, 120, 0.5f).n(60).dur(5).speed(1.4f).snd(BOOM);
        add(new Tool(8, "Neutronenstern-Vorbeiflug", GL, 0xFFDDE8FF, "Die Gezeiten zerreißen die Kruste")).dmg(0, 90, 60, 0.6f).style(SH).dur(4).snd(DEEP);
        add(new Tool(8, "Gezeitenwelle", AR, 0xFF3A8AE0, "Ozeane schwappen über die Kontinente")).dmg(140, 6, 0, 0.55f).style(FL).dur(4).snd(SPLASH);
        add(new Tool(8, "Tsunami", AR, 0xFF2A6AD0, "Eine Flutwelle läuft um den Planeten")).dmg(120, 8, 0, 0.6f).style(FL).dur(5).snd(SPLASH);
        add(new Tool(8, "Supervulkan", AR, 0xFFFF5A1A, "Ein Vulkan bricht in riesigem Ausmaß aus")).dmg(70, 80, 255, 0.85f).style(LV).dur(5).snd(BOOM);
        add(new Tool(8, "Kontinentalbeben", AR, 0xFF9A7A5A, "Risse ziehen durch die Kruste")).dmg(90, 40, 20, 0.5f).style(SH).dur(4).snd(BOOM);
        add(new Tool(8, "Magnetsturm", GL, 0xFF8AFFC8, "Polarlichter und Stromausfall überall")).dmg(0, 4, 0, 0.3f).style(EMP).dur(5).snd(UFO);
        add(new Tool(8, "Sonnenwind", GL, 0xFFFFE08A, "Die Atmosphäre wird weggeblasen")).dmg(0, 10, 120, 0.4f).style(SA).dur(4).snd(WHOOSH);
        add(new Tool(8, "Gamma-Blitz", GL, 0xFFE8F6FF, "Ein Gammastrahlenblitz trifft eine Seite")).dmg(0, 30, 255, 0.9f).style(B).dur(2).snd(DEEP);
        add(new Tool(8, "Wurmloch", HO, 0xFF6A3AFF, "Saugt alles in ein anderes Universum")).dmg(80, 255, 30, 1f).style(RF).dur(4).snd(DEEP);
        add(new Tool(8, "Mini-Schwarzes-Loch", HO, 0xFF0A0A12, "Klein, aber sehr hungrig")).dmg(40, 255, 40, 1f).style(AN).dur(3).snd(DEEP);
        add(new Tool(8, "Plasma-Kugel", SHT, 0xFFFF6AE8, "Heißes Plasma, frisst sich ein")).dmg(36, 70, 255, 0.7f).style(F).speed(1.2f).snd(LASER);
        add(new Tool(8, "Schockwelle", AR, 0xFFFFFFFF, "Eine Druckwelle rast über die Oberfläche")).dmg(130, 12, 40, 0.5f).style(SH).dur(2).snd(THUNDER);
        add(new Tool(8, "Kometenschauer", RN, 0xFFBFE8FF, "Eis-Kometen mit langen Schweifen")).dmg(18, 70, 30, 0.5f).style(I).n(16).dur(4).speed(1.6f).snd(WHOOSH);
        add(new Tool(8, "Kernschmelze", AR, 0xFFFFA02A, "Der Kern kocht über")).dmg(100, 120, 255, 0.9f).style(LV).dur(4).snd(BOOM);

        // ---------------------------------------------------------------- 9 Apokalypse (20)
        add(new Tool(9, "Planetenknacker", SP, 0xFFFF3A2A, "Reißt riesige Spalten bis zum Kern")).dmg(0, 255, 255, 1f).style(LV).width(3f).snd(DEEP);
        add(new Tool(9, "Antimaterie-Bombe", SHT, 0xFFEAF6FF, "Materie und Antimaterie löschen sich aus")).dmg(160, 255, 120, 1f).style(AN).speed(1f).snd(DEEP);
        add(new Tool(9, "Sternen-Supernova", GL, 0xFFFFF0B0, "Der Stern explodiert, alles verglüht")).dmg(0, 120, 255, 1f).style(F).dur(4).snd(DEEP);
        add(new Tool(9, "Armada", SW, 0xFF8AA0C8, "Eine ganze Raumflotte greift an")).dmg(10, 40, 160, 0.6f).n(40).dur(10).sprite(5).speed(1.6f).snd(LASER);
        add(new Tool(9, "Nano-Schwarm", SW, 0xFF9AFFE0, "Winzige Maschinen fressen die Oberfläche")).dmg(6, 30, 10, 0.7f).style(TX).n(60).dur(10).sprite(5).speed(2.2f).snd(UFO);
        add(new Tool(9, "Zeitbombe", SHT, 0xFFFFD34A, "Tickt, dann ein riesiger Knall")).dmg(150, 220, 255, 1f).style(F).speed(0.5f).snd(BOOM);
        add(new Tool(9, "Todesstern-Strahl", BM, 0xFF3AFF6A, "Ein grüner Planetenzerstörer")).dmg(60, 255, 255, 1f).style(LV).width(6f).snd(DEEP);
        add(new Tool(9, "Kettenreaktion", RN, 0xFFFF7A2A, "Eine Explosion löst die nächste aus")).dmg(40, 120, 255, 0.8f).style(F).n(14).dur(4).speed(1f).snd(BOOM);
        add(new Tool(9, "Atomwinter", GL, 0xFFDDE6F0, "Asche verdunkelt, Eis bedeckt alles")).dmg(0, 6, 0, 0.6f).style(I).dur(5).snd(ICE);
        add(new Tool(9, "Virus-Ausbruch", GL, 0xFF9CFF3C, "Eine Seuche breitet sich aus")).dmg(0, 4, 0, 0.9f).style(TX).dur(6).snd(UFO);
        add(new Tool(9, "Riss im Raum", HO, 0xFFB35CFF, "Die Realität reißt auf")).dmg(120, 255, 60, 1f).style(RF).dur(5).snd(DEEP);
        add(new Tool(9, "Kern-Laser", BM, 0xFFFF8A2A, "Schneidet bis zum Kern und darüber hinaus")).dmg(30, 255, 255, 1f).style(LV).width(3f).snd(LASER);
        add(new Tool(9, "Meteor-Apokalypse", RN, 0xFF7A5A4A, "Tausend Meteore auf einmal")).dmg(30, 150, 255, 0.9f).style(F).n(80).dur(6).speed(1.4f).snd(BOOM);
        add(new Tool(9, "Sonnen-Sturz", SHT, 0xFFFFE07A, "Ein kleiner Stern fällt auf den Planeten")).dmg(240, 255, 255, 1f).style(F).speed(0.4f).snd(DEEP);
        add(new Tool(9, "Quantenbombe", SHT, 0xFF8AE0FF, "Ist gleichzeitig explodiert und nicht")).dmg(120, 200, 120, 0.95f).style(AN).speed(1f).snd(MAGIC);
        add(new Tool(9, "Gravitationskollaps", GL, 0xFF6A5AFF, "Der Planet fällt in sich zusammen")).dmg(0, 255, 60, 1f).style(SH).dur(5).snd(DEEP);
        add(new Tool(9, "Strahlungsgürtel", GL, 0xFFE8FF6A, "Unsichtbar, aber tödlich")).dmg(0, 2, 30, 0.95f).style(TX).dur(4).snd(UFO);
        add(new Tool(9, "Lava-Ozean", AR, 0xFFFF4A10, "Die Oberfläche wird zu Lava")).dmg(220, 30, 255, 0.95f).style(LV).dur(6).snd(BOOM);
        add(new Tool(9, "Riesen-Lego-Armada", SW, 0xFFE3000B, "Fliegende Lego-Raumschiffe")).dmg(14, 50, 60, 0.6f).style(LG).n(30).dur(10).sprite(5).speed(1.6f).snd(FUNNY);
        add(new Tool(9, "Alles auf einmal", RN, 0xFFFF2A6A, "Von allem etwas – das große Finale")).dmg(50, 200, 255, 1f).style(AN).n(40).dur(6).speed(1.2f).snd(DEEP);
    }
}