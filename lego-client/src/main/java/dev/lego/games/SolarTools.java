package dev.lego.games;

import java.util.ArrayList;
import java.util.List;

final class SolarTools {
   static final String[] CATS = new String[]{"Laser", "Meteore", "Aliens", "Natur", "Kosmos", "Sci-Fi", "Spaß", "Weihnachten"};
   static final String[] CAT_ICONS = new String[]{"bolt", "gem", "eye", "flame", "moon", "chip", "heart", "gift"};
   static final List<SolarTools.Tool> ALL = new ArrayList<>();
   private static final String LASER = "minecraft:block.beacon.power_select";
   private static final String BOOM = "minecraft:entity.generic.explode";
   private static final String WHOOSH = "minecraft:entity.firework_rocket.launch";
   private static final String UFO = "minecraft:block.beacon.ambient";
   private static final String MAGIC = "minecraft:block.enchantment_table.use";
   private static final String ICE = "minecraft:block.glass.break";
   private static final String SPLASH = "minecraft:entity.player.splash.high_speed";
   private static final String FUNNY = "minecraft:entity.chicken.egg";
   private static final String THUNDER = "minecraft:entity.lightning_bolt.thunder";
   private static final String BELL = "minecraft:block.bell.use";
   private static final String DEEP = "minecraft:entity.warden.sonic_boom";

   private SolarTools() {
   }

   static List<SolarTools.Tool> of(int var0) {
      ArrayList var1 = new ArrayList();

      for (SolarTools.Tool var3 : ALL) {
         if (var3.cat == var0) {
            var1.add(var3);
         }
      }

      return var1;
   }

   private static SolarTools.Tool add(SolarTools.Tool var0) {
      ALL.add(var0);
      return var0;
   }

   static {
      SolarTools.Kind var0 = SolarTools.Kind.BEAM;
      SolarTools.Kind var1 = SolarTools.Kind.SHOT;
      SolarTools.Kind var2 = SolarTools.Kind.RAIN;
      SolarTools.Kind var3 = SolarTools.Kind.SWARM;
      SolarTools.Kind var4 = SolarTools.Kind.AREA;
      SolarTools.Kind var5 = SolarTools.Kind.GLOBAL;
      SolarTools.Kind var6 = SolarTools.Kind.HOLE;
      SolarTools.Kind var7 = SolarTools.Kind.SPLIT;
      add(new SolarTools.Tool(0, "Dauerlaser", var0, -53176, "Klassischer roter Laser – halten zum Brennen"))
         .dmg(9.0F, 22.0F, 120.0F, 0.5F)
         .width(1.0F)
         .snd("minecraft:block.beacon.power_select");
      add(new SolarTools.Tool(0, "Pulslaser", var0, -12779654, "Schnelle grüne Pulse"))
         .dmg(11.0F, 30.0F, 90.0F, 0.5F)
         .width(0.8F)
         .speed(3.0F)
         .snd("minecraft:block.beacon.power_select");
      add(new SolarTools.Tool(0, "Blauer Schneider", var0, -12605185, "Dünn, heiß, präzise"))
         .dmg(6.0F, 30.0F, 170.0F, 0.6F)
         .width(0.5F)
         .snd("minecraft:block.beacon.power_select");
      add(new SolarTools.Tool(0, "Violetter Strahl", var0, -5022465, "Breiter Energiestrahl"))
         .dmg(15.0F, 26.0F, 110.0F, 0.55F)
         .width(1.8F)
         .snd("minecraft:block.beacon.power_select");
      add(new SolarTools.Tool(0, "Goldstrahl", var0, -14006, "Verwandelt die Oberfläche in Gold"))
         .dmg(14.0F, 10.0F, 40.0F, 0.3F)
         .style(3)
         .width(1.4F)
         .snd("minecraft:block.enchantment_table.use");
      add(new SolarTools.Tool(0, "Eisstrahl", var0, -6297345, "Friert alles ein"))
         .dmg(16.0F, 8.0F, 0.0F, 0.4F)
         .style(2)
         .width(1.5F)
         .snd("minecraft:block.glass.break");
      add(new SolarTools.Tool(0, "Todesstrahl", var0, -61392, "Riesiger Superlaser"))
         .dmg(34.0F, 60.0F, 255.0F, 0.9F)
         .width(4.0F)
         .snd("minecraft:entity.warden.sonic_boom");
      add(new SolarTools.Tool(0, "Planetenbohrer", var0, -30166, "Bohrt tief bis zum Kern"))
         .dmg(7.0F, 90.0F, 255.0F, 0.8F)
         .style(11)
         .width(1.2F)
         .snd("minecraft:block.beacon.power_select");
      add(new SolarTools.Tool(0, "Regenbogenlaser", var0, -41768, "Wechselt ständig die Farbe"))
         .dmg(12.0F, 24.0F, 100.0F, 0.5F)
         .style(19)
         .width(1.3F)
         .snd("minecraft:block.enchantment_table.use");
      add(new SolarTools.Tool(0, "Giftlaser", var0, -6488260, "Hinterlässt giftige Flecken"))
         .dmg(13.0F, 14.0F, 40.0F, 0.7F)
         .style(4)
         .width(1.2F)
         .snd("minecraft:block.beacon.power_select");
      add(new SolarTools.Tool(0, "Kristalllaser", var0, -4197121, "Lässt Kristalle wachsen"))
         .dmg(13.0F, 16.0F, 30.0F, 0.4F)
         .style(5)
         .width(1.2F)
         .snd("minecraft:block.enchantment_table.use");
      add(new SolarTools.Tool(0, "Doppellaser", var0, -40896, "Zwei Strahlen gleichzeitig"))
         .dmg(10.0F, 26.0F, 130.0F, 0.55F)
         .width(1.0F)
         .n(2)
         .snd("minecraft:block.beacon.power_select");
      add(new SolarTools.Tool(0, "Laser-Fächer", var0, -46486, "Fünf Strahlen in einer Reihe"))
         .dmg(8.0F, 20.0F, 110.0F, 0.5F)
         .width(0.7F)
         .n(5)
         .snd("minecraft:block.beacon.power_select");
      add(new SolarTools.Tool(0, "Schmelzstrahl", var0, -20434, "Macht Gestein zu Lava"))
         .dmg(18.0F, 20.0F, 255.0F, 0.7F)
         .style(11)
         .width(2.0F)
         .snd("minecraft:block.beacon.power_select");
      add(new SolarTools.Tool(0, "Antimaterie-Strahl", var0, -1378561, "Löscht Materie einfach aus"))
         .dmg(20.0F, 80.0F, 60.0F, 0.95F)
         .style(12)
         .width(2.4F)
         .snd("minecraft:entity.warden.sonic_boom");
      add(new SolarTools.Tool(0, "Mini-Laser", var0, -32608, "Zum Zeichnen auf dem Planeten"))
         .dmg(4.0F, 12.0F, 90.0F, 0.2F)
         .width(0.35F)
         .snd("minecraft:block.beacon.power_select");
      add(new SolarTools.Tool(1, "Kleiner Meteor", var1, -5207446, "Ein Stein aus dem All")).dmg(18.0F, 60.0F, 120.0F, 0.8F).speed(1.3F);
      add(new SolarTools.Tool(1, "Großer Asteroid", var1, -7701910, "Ein richtiger Brocken")).dmg(60.0F, 140.0F, 200.0F, 0.95F).speed(0.8F).scorch(2.4F);
      add(new SolarTools.Tool(1, "Planetenkiller", var1, -9807286, "Ein Asteroid, halb so groß wie der Mond"))
         .dmg(160.0F, 255.0F, 255.0F, 1.0F)
         .speed(0.5F)
         .scorch(2.2F)
         .snd("minecraft:entity.warden.sonic_boom");
      add(new SolarTools.Tool(1, "Eiskomet", var1, -4200193, "Komet mit glitzerndem Schweif"))
         .dmg(34.0F, 80.0F, 60.0F, 0.85F)
         .style(2)
         .speed(1.5F)
         .snd("minecraft:block.glass.break");
      add(new SolarTools.Tool(1, "Eisenmeteor", var1, -6643542, "Superschwer und schnell")).dmg(26.0F, 170.0F, 200.0F, 0.95F).speed(2.2F);
      add(new SolarTools.Tool(1, "Glühender Meteor", var1, -38358, "Brennt schon im Anflug")).dmg(32.0F, 90.0F, 255.0F, 0.9F).style(1).speed(1.2F);
      add(new SolarTools.Tool(1, "Meteorschauer", var2, -5207446, "Viele kleine Meteore")).dmg(12.0F, 50.0F, 120.0F, 0.75F).n(24).dur(3.0F).speed(1.4F);
      add(new SolarTools.Tool(1, "Kometenregen", var2, -6299393, "Eiskometen im Dauerfeuer"))
         .dmg(16.0F, 50.0F, 40.0F, 0.7F)
         .style(2)
         .n(18)
         .dur(3.0F)
         .speed(1.6F)
         .snd("minecraft:block.glass.break");
      add(new SolarTools.Tool(1, "Lava-Brocken", var2, -42470, "Glühende Felsen regnen herab")).dmg(18.0F, 60.0F, 255.0F, 0.85F).style(11).n(14).dur(2.5F);
      add(new SolarTools.Tool(1, "Kristallasteroid", var1, -4656897, "Zerspringt in Kristalle"))
         .dmg(40.0F, 70.0F, 40.0F, 0.8F)
         .style(5)
         .speed(1.1F)
         .snd("minecraft:block.glass.break");
      add(new SolarTools.Tool(1, "Goldasteroid", var1, -11446, "Reich, aber tödlich"))
         .dmg(38.0F, 70.0F, 60.0F, 0.7F)
         .style(3)
         .speed(1.0F)
         .snd("minecraft:block.bell.use");
      add(new SolarTools.Tool(1, "Mondsturz", var1, -2566964, "Ein ganzer Mond stürzt ab"))
         .dmg(230.0F, 255.0F, 255.0F, 1.0F)
         .speed(0.35F)
         .scorch(1.8F)
         .snd("minecraft:entity.warden.sonic_boom");
      add(new SolarTools.Tool(1, "Staubwolke", var2, -3624822, "Hunderte winzige Einschläge"))
         .dmg(6.0F, 30.0F, 60.0F, 0.4F)
         .style(8)
         .n(60)
         .dur(4.0F)
         .speed(2.0F);
      add(new SolarTools.Tool(1, "Doppelasteroid", var2, -7701910, "Zwei Brocken auf einmal")).dmg(44.0F, 120.0F, 200.0F, 0.95F).n(2).dur(0.5F).speed(0.9F);
      add(new SolarTools.Tool(2, "UFO-Schwarm", var3, -9633892, "Kleine UFOs schießen Laser"))
         .dmg(7.0F, 26.0F, 110.0F, 0.5F)
         .n(8)
         .dur(9.0F)
         .sprite(0)
         .snd("minecraft:block.beacon.ambient");
      add(new SolarTools.Tool(2, "Mutterschiff", var3, -5022465, "Ein riesiges Schiff mit Todesstrahl"))
         .dmg(28.0F, 60.0F, 200.0F, 0.85F)
         .n(1)
         .dur(10.0F)
         .sprite(1)
         .width(3.0F)
         .snd("minecraft:entity.warden.sonic_boom");
      add(new SolarTools.Tool(2, "Entführungsstrahl", var3, -6488088, "UFOs entführen Bewohner"))
         .dmg(12.0F, 2.0F, 0.0F, 0.9F)
         .style(14)
         .n(5)
         .dur(10.0F)
         .sprite(4)
         .snd("minecraft:block.beacon.ambient");
      add(new SolarTools.Tool(2, "Alien-Invasion", var3, -9633892, "Eine ganze Flotte"))
         .dmg(9.0F, 30.0F, 130.0F, 0.6F)
         .n(22)
         .dur(12.0F)
         .sprite(0)
         .snd("minecraft:block.beacon.ambient");
      add(new SolarTools.Tool(2, "Kampfläufer", var2, -7667860, "Riesige Roboter landen und schießen"))
         .dmg(16.0F, 30.0F, 90.0F, 0.7F)
         .style(0)
         .n(6)
         .dur(4.0F)
         .sprite(-2)
         .speed(0.7F);
      add(new SolarTools.Tool(2, "Glibber-Aliens", var3, -5177540, "Überziehen alles mit Schleim"))
         .dmg(14.0F, 10.0F, 0.0F, 0.6F)
         .style(6)
         .n(6)
         .dur(10.0F)
         .sprite(3)
         .snd("minecraft:block.beacon.ambient");
      add(new SolarTools.Tool(2, "Kristall-Aliens", var3, -4197121, "Verwandeln die Welt in Kristall"))
         .dmg(13.0F, 12.0F, 20.0F, 0.5F)
         .style(5)
         .n(7)
         .dur(10.0F)
         .sprite(0)
         .snd("minecraft:block.enchantment_table.use");
      add(new SolarTools.Tool(2, "Terraformer", var3, -22454, "Macht den Planeten zur Wüste"))
         .dmg(20.0F, 8.0F, 40.0F, 0.5F)
         .style(8)
         .n(4)
         .dur(12.0F)
         .sprite(2)
         .width(2.0F)
         .snd("minecraft:block.beacon.ambient");
      add(new SolarTools.Tool(2, "Pulsar-Kreuzer", var3, -46454, "Schnelle Pulse aus der Umlaufbahn"))
         .dmg(9.0F, 34.0F, 150.0F, 0.6F)
         .n(3)
         .dur(8.0F)
         .sprite(3)
         .speed(3.0F)
         .snd("minecraft:block.beacon.power_select");
      add(new SolarTools.Tool(2, "Alien-Bohrschiff", var3, -30166, "Bohrt Löcher bis zum Kern"))
         .dmg(8.0F, 110.0F, 255.0F, 0.8F)
         .style(11)
         .n(2)
         .dur(10.0F)
         .sprite(2)
         .snd("minecraft:entity.warden.sonic_boom");
      add(new SolarTools.Tool(2, "Eis-Aliens", var3, -6297345, "Bringen eine Eiszeit"))
         .dmg(22.0F, 6.0F, 0.0F, 0.5F)
         .style(2)
         .n(5)
         .dur(10.0F)
         .sprite(3)
         .snd("minecraft:block.glass.break");
      add(new SolarTools.Tool(2, "Gift-Flotte", var3, -6488260, "Verseuchen ganze Kontinente"))
         .dmg(16.0F, 8.0F, 20.0F, 0.8F)
         .style(4)
         .n(8)
         .dur(11.0F)
         .sprite(0)
         .snd("minecraft:block.beacon.ambient");
      add(new SolarTools.Tool(2, "Stromausfall-UFOs", var3, -8593153, "Schalten alle Lichter aus"))
         .dmg(18.0F, 0.0F, 0.0F, 0.05F)
         .style(14)
         .n(6)
         .dur(8.0F)
         .sprite(4)
         .snd("minecraft:block.beacon.ambient");
      add(new SolarTools.Tool(3, "Vulkanausbruch", var4, -42470, "Lava bricht aus dem Boden"))
         .dmg(40.0F, 70.0F, 255.0F, 0.8F)
         .style(11)
         .dur(3.0F)
         .snd("minecraft:entity.generic.explode");
      add(new SolarTools.Tool(3, "Supervulkan", var4, -50678, "Ein Kontinent versinkt in Lava"))
         .dmg(110.0F, 90.0F, 255.0F, 0.95F)
         .style(11)
         .dur(5.0F)
         .snd("minecraft:entity.warden.sonic_boom");
      add(new SolarTools.Tool(3, "Erdbeben", var7, -5207446, "Risse ziehen sich über den Boden"))
         .dmg(0.0F, 60.0F, 60.0F, 0.6F)
         .style(13)
         .width(1.0F)
         .snd("minecraft:entity.lightning_bolt.thunder");
      add(new SolarTools.Tool(3, "Megabeben", var7, -7706038, "Der Planet bekommt tiefe Spalten"))
         .dmg(0.0F, 120.0F, 180.0F, 0.8F)
         .style(13)
         .width(2.2F)
         .snd("minecraft:entity.warden.sonic_boom");
      add(new SolarTools.Tool(3, "Tsunami", var4, -12608257, "Riesige Flutwelle"))
         .dmg(70.0F, 8.0F, 0.0F, 0.7F)
         .style(7)
         .dur(3.0F)
         .snd("minecraft:entity.player.splash.high_speed");
      add(new SolarTools.Tool(3, "Sintflut", var5, -12608257, "Der Meeresspiegel steigt überall"))
         .dmg(0.0F, 0.0F, 0.0F, 0.35F)
         .style(7)
         .dur(6.0F)
         .snd("minecraft:entity.player.splash.high_speed");
      add(new SolarTools.Tool(3, "Eiszeit", var5, -4200193, "Der ganze Planet friert ein"))
         .dmg(0.0F, 0.0F, 0.0F, 0.3F)
         .style(2)
         .dur(6.0F)
         .snd("minecraft:block.glass.break");
      add(new SolarTools.Tool(3, "Hitzewelle", var5, -30150, "Die Temperatur steigt und steigt"))
         .dmg(0.0F, 0.0F, 0.0F, 0.25F)
         .style(1)
         .dur(6.0F)
         .snd("minecraft:entity.generic.explode");
      add(new SolarTools.Tool(3, "Waldbrand", var4, -34262, "Feuer breitet sich aus"))
         .dmg(60.0F, 12.0F, 200.0F, 0.5F)
         .style(1)
         .dur(4.0F)
         .snd("minecraft:entity.generic.explode");
      add(new SolarTools.Tool(3, "Sandsturm", var4, -2047862, "Alles wird unter Sand begraben"))
         .dmg(80.0F, 6.0F, 20.0F, 0.4F)
         .style(8)
         .dur(4.0F)
         .snd("minecraft:entity.firework_rocket.launch");
      add(new SolarTools.Tool(3, "Blitzgewitter", var2, -1511169, "Tausende Blitze"))
         .dmg(5.0F, 20.0F, 150.0F, 0.3F)
         .style(0)
         .n(50)
         .dur(4.0F)
         .speed(9.0F)
         .snd("minecraft:entity.lightning_bolt.thunder");
      add(new SolarTools.Tool(3, "Sonnensturm", var5, -12182, "Ein Plasma-Ausbruch der Sonne"))
         .dmg(0.0F, 0.0F, 0.0F, 0.2F)
         .style(14)
         .dur(5.0F)
         .snd("minecraft:entity.warden.sonic_boom");
      add(new SolarTools.Tool(3, "Giftiger Nebel", var4, -6488260, "Eine grüne Wolke zieht übers Land"))
         .dmg(70.0F, 4.0F, 10.0F, 0.8F)
         .style(4)
         .dur(4.0F)
         .snd("minecraft:entity.firework_rocket.launch");
      add(new SolarTools.Tool(4, "Schwarzes Loch", var6, -15070678, "Verschlingt den Planeten langsam"))
         .dmg(0.0F, 0.0F, 0.0F, 1.0F)
         .dur(14.0F)
         .width(1.0F)
         .snd("minecraft:entity.warden.sonic_boom");
      add(new SolarTools.Tool(4, "Mini-Loch", var6, -14017990, "Klein, aber gierig"))
         .dmg(0.0F, 0.0F, 0.0F, 1.0F)
         .dur(10.0F)
         .width(0.5F)
         .snd("minecraft:entity.warden.sonic_boom");
      add(new SolarTools.Tool(4, "Weißes Loch", var6, -1, "Spuckt Energie statt sie zu fressen"))
         .dmg(0.0F, 0.0F, 0.0F, 1.0F)
         .dur(10.0F)
         .width(0.8F)
         .snd("minecraft:block.enchantment_table.use");
      add(new SolarTools.Tool(4, "Wurmloch", var4, -5022465, "Reißt ein Stück aus der Oberfläche"))
         .dmg(60.0F, 200.0F, 40.0F, 1.0F)
         .style(20)
         .dur(2.5F)
         .snd("minecraft:entity.warden.sonic_boom");
      add(new SolarTools.Tool(4, "Gammablitz", var5, -2043649, "Ein Strahlungsblitz aus dem All"))
         .dmg(0.0F, 0.0F, 0.0F, 0.7F)
         .style(14)
         .dur(3.0F)
         .snd("minecraft:entity.warden.sonic_boom");
      add(new SolarTools.Tool(4, "Supernova-Welle", var5, -8054, "Eine Sternexplosion in der Nähe"))
         .dmg(0.0F, 0.0F, 0.0F, 0.9F)
         .style(1)
         .dur(4.0F)
         .snd("minecraft:entity.warden.sonic_boom");
      add(new SolarTools.Tool(4, "Antimaterie-Kugel", var1, -1378561, "Löscht alles im Umkreis aus"))
         .dmg(90.0F, 255.0F, 40.0F, 1.0F)
         .style(12)
         .speed(1.1F)
         .snd("minecraft:entity.warden.sonic_boom");
      add(new SolarTools.Tool(4, "Neutronenstern", var1, -4204289, "Unglaublich dicht und schwer"))
         .dmg(120.0F, 255.0F, 255.0F, 1.0F)
         .speed(0.6F)
         .snd("minecraft:entity.warden.sonic_boom");
      add(new SolarTools.Tool(4, "Zeitriss", var7, -5022465, "Ein leuchtender Riss in der Zeit"))
         .dmg(0.0F, 160.0F, 40.0F, 0.9F)
         .style(20)
         .width(1.6F)
         .snd("minecraft:block.enchantment_table.use");
      add(new SolarTools.Tool(4, "Gravitationshammer", var7, -7689985, "Zerbricht die Kruste"))
         .dmg(0.0F, 200.0F, 120.0F, 0.9F)
         .style(13)
         .width(3.0F)
         .snd("minecraft:entity.warden.sonic_boom");
      add(new SolarTools.Tool(4, "Planetenspalter", var7, -46550, "Spaltet den Planeten in zwei Hälften"))
         .dmg(0.0F, 255.0F, 255.0F, 1.0F)
         .style(11)
         .width(5.0F)
         .snd("minecraft:entity.warden.sonic_boom");
      add(new SolarTools.Tool(4, "Sternenstaub", var2, -3920, "Glitzernder Regen aus dem All"))
         .dmg(5.0F, 10.0F, 30.0F, 0.1F)
         .style(3)
         .n(80)
         .dur(4.0F)
         .speed(2.0F)
         .snd("minecraft:block.enchantment_table.use");
      add(new SolarTools.Tool(5, "Plasmakanone", var1, -12787457, "Eine Kugel aus reinem Plasma"))
         .dmg(40.0F, 100.0F, 255.0F, 0.9F)
         .speed(2.0F)
         .snd("minecraft:block.beacon.power_select");
      add(new SolarTools.Tool(5, "Railgun", var1, -7677697, "Extrem schnelles Projektil"))
         .dmg(14.0F, 200.0F, 200.0F, 0.95F)
         .speed(5.0F)
         .snd("minecraft:block.beacon.power_select");
      add(new SolarTools.Tool(5, "Orbitalkanone", var2, -20406, "Salven aus der Umlaufbahn"))
         .dmg(20.0F, 90.0F, 200.0F, 0.9F)
         .n(8)
         .dur(2.0F)
         .speed(3.0F)
         .snd("minecraft:entity.generic.explode");
      add(new SolarTools.Tool(5, "Nanoschwarm", var4, -6643542, "Winzige Roboter fressen alles"))
         .dmg(90.0F, 30.0F, 0.0F, 0.9F)
         .style(6)
         .dur(6.0F)
         .snd("minecraft:entity.firework_rocket.launch");
      add(new SolarTools.Tool(5, "EMP-Welle", var5, -8593153, "Alle Lichter gehen aus"))
         .dmg(0.0F, 0.0F, 0.0F, 0.02F)
         .style(14)
         .dur(2.0F)
         .snd("minecraft:entity.lightning_bolt.thunder");
      add(new SolarTools.Tool(5, "Kristallvirus", var4, -4197121, "Kristalle wachsen über den Planeten"))
         .dmg(100.0F, 20.0F, 10.0F, 0.6F)
         .style(5)
         .dur(6.0F)
         .snd("minecraft:block.enchantment_table.use");
      add(new SolarTools.Tool(5, "Stern-Vernichter", var0, -9633940, "Eine original Super-Station schießt"))
         .dmg(70.0F, 255.0F, 255.0F, 1.0F)
         .width(6.0F)
         .snd("minecraft:entity.warden.sonic_boom");
      add(new SolarTools.Tool(5, "Ionen-Sturm", var2, -8607489, "Blaue Energieblitze"))
         .dmg(10.0F, 40.0F, 160.0F, 0.5F)
         .n(30)
         .dur(3.0F)
         .speed(6.0F)
         .snd("minecraft:entity.lightning_bolt.thunder");
      add(new SolarTools.Tool(5, "Singularitätsbombe", var1, -14017974, "Implodiert statt zu explodieren"))
         .dmg(130.0F, 255.0F, 60.0F, 1.0F)
         .style(12)
         .speed(0.8F)
         .snd("minecraft:entity.warden.sonic_boom");
      add(new SolarTools.Tool(5, "Drohnenschwarm", var3, -38342, "Hunderte kleine Drohnen"))
         .dmg(5.0F, 20.0F, 90.0F, 0.4F)
         .n(30)
         .dur(9.0F)
         .sprite(5)
         .speed(2.0F)
         .snd("minecraft:block.beacon.ambient");
      add(new SolarTools.Tool(5, "Gravitationsbombe", var1, -7705857, "Presst die Kruste zusammen"))
         .dmg(70.0F, 180.0F, 60.0F, 0.95F)
         .style(13)
         .speed(1.0F)
         .snd("minecraft:entity.warden.sonic_boom");
      add(new SolarTools.Tool(5, "Frostbombe", var1, -4200193, "Friert einen Kontinent ein"))
         .dmg(90.0F, 40.0F, 0.0F, 0.6F)
         .style(2)
         .speed(1.2F)
         .snd("minecraft:block.glass.break");
      add(new SolarTools.Tool(6, "Riesen-Lego-Stein", var1, -1900533, "Der größte Lego-Stein der Welt"))
         .dmg(60.0F, 120.0F, 20.0F, 0.9F)
         .style(15)
         .sprite(0)
         .speed(0.9F)
         .snd("minecraft:entity.chicken.egg");
      add(new SolarTools.Tool(6, "Lego-Regen", var2, -13053, "Bunte Steine überall"))
         .dmg(18.0F, 50.0F, 10.0F, 0.6F)
         .style(15)
         .sprite(0)
         .n(20)
         .dur(3.0F)
         .snd("minecraft:entity.chicken.egg");
      add(new SolarTools.Tool(6, "Kuh-Meteor", var1, -724502, "Muuuh!"))
         .dmg(40.0F, 80.0F, 10.0F, 0.7F)
         .style(16)
         .sprite(1)
         .speed(0.9F)
         .snd("minecraft:entity.cow.ambient");
      add(new SolarTools.Tool(6, "Riesentorte", var1, -12064, "Alles voller Sahne"))
         .dmg(70.0F, 20.0F, 0.0F, 0.5F)
         .style(9)
         .sprite(2)
         .speed(0.8F)
         .snd("minecraft:entity.player.splash.high_speed");
      add(new SolarTools.Tool(6, "Quietscheente", var1, -11446, "Eine gigantische Gummiente"))
         .dmg(50.0F, 60.0F, 0.0F, 0.6F)
         .style(7)
         .sprite(3)
         .speed(0.8F)
         .snd("minecraft:entity.chicken.egg");
      add(new SolarTools.Tool(6, "Bowlingkugel", var1, -12961190, "Strike!"))
         .dmg(46.0F, 160.0F, 30.0F, 0.9F)
         .sprite(4)
         .speed(1.3F)
         .snd("minecraft:entity.generic.explode");
      add(new SolarTools.Tool(6, "Pizza-Meteor", var1, -20406, "Mit extra Käse"))
         .dmg(55.0F, 60.0F, 60.0F, 0.7F)
         .style(17)
         .sprite(5)
         .speed(0.9F)
         .snd("minecraft:entity.chicken.egg");
      add(new SolarTools.Tool(6, "Donut-Regen", var2, -30016, "Süß und zerstörerisch"))
         .dmg(22.0F, 40.0F, 10.0F, 0.5F)
         .style(18)
         .sprite(6)
         .n(14)
         .dur(3.0F)
         .snd("minecraft:entity.chicken.egg");
      add(new SolarTools.Tool(6, "Fallender Flügel", var1, -15066598, "Ein Konzertflügel aus dem All"))
         .dmg(44.0F, 110.0F, 10.0F, 0.8F)
         .style(21)
         .sprite(7)
         .speed(1.0F)
         .snd("minecraft:block.note_block.harp");
      add(new SolarTools.Tool(6, "Riesen-Minifigur", var1, -13053, "Eine Lego-Figur so groß wie ein Land"))
         .dmg(70.0F, 120.0F, 20.0F, 0.85F)
         .style(15)
         .sprite(8)
         .speed(0.7F)
         .snd("minecraft:entity.chicken.egg");
      add(new SolarTools.Tool(6, "Gummibärchen-Regen", var2, -46486, "Bunt, klebrig, gefährlich"))
         .dmg(16.0F, 30.0F, 0.0F, 0.4F)
         .style(6)
         .sprite(9)
         .n(24)
         .dur(3.0F)
         .snd("minecraft:entity.chicken.egg");
      add(new SolarTools.Tool(6, "Discokugel", var1, -1513217, "Party auf dem ganzen Planeten"))
         .dmg(50.0F, 70.0F, 20.0F, 0.6F)
         .style(10)
         .sprite(10)
         .speed(0.9F)
         .snd("minecraft:block.note_block.bell");
      add(new SolarTools.Tool(6, "Farbbombe", var1, -41768, "Malt den Planeten bunt an"))
         .dmg(80.0F, 4.0F, 0.0F, 0.1F)
         .style(19)
         .speed(1.2F)
         .snd("minecraft:entity.player.splash.high_speed");
      add(new SolarTools.Tool(7, "Riesen-Schneeball", var1, -722689, "Ein Schneeball so groß wie ein Mond"))
         .dmg(80.0F, 60.0F, 0.0F, 0.7F)
         .style(2)
         .speed(1.0F)
         .snd("minecraft:block.glass.break");
      add(new SolarTools.Tool(7, "Schneesturm", var4, -1510657, "Alles versinkt im Schnee"))
         .dmg(110.0F, 4.0F, 0.0F, 0.35F)
         .style(2)
         .dur(5.0F)
         .snd("minecraft:entity.firework_rocket.launch");
      add(new SolarTools.Tool(7, "Geschenke-Regen", var2, -1890757, "Pakete fallen vom Himmel"))
         .dmg(20.0F, 40.0F, 10.0F, 0.5F)
         .style(10)
         .sprite(-3)
         .n(20)
         .dur(3.0F)
         .snd("minecraft:block.bell.use");
      add(new SolarTools.Tool(7, "Zuckerstangen-Laser", var0, -50610, "Rot-weiß gestreift"))
         .dmg(12.0F, 26.0F, 120.0F, 0.5F)
         .style(19)
         .width(1.4F)
         .snd("minecraft:block.beacon.power_select");
      add(new SolarTools.Tool(7, "Lebkuchen-Meteor", var1, -4885702, "Knusprig und hart"))
         .dmg(46.0F, 90.0F, 60.0F, 0.8F)
         .style(8)
         .speed(1.0F)
         .snd("minecraft:entity.chicken.egg");
      add(new SolarTools.Tool(7, "Rentier-Staffel", var3, -5207462, "Fliegende Rentiere werfen Geschenke"))
         .dmg(12.0F, 30.0F, 10.0F, 0.4F)
         .style(10)
         .n(9)
         .dur(9.0F)
         .sprite(6)
         .snd("minecraft:block.bell.use");
      add(new SolarTools.Tool(7, "Eiszapfen-Regen", var2, -4200193, "Spitze Eiszapfen"))
         .dmg(10.0F, 60.0F, 0.0F, 0.5F)
         .style(2)
         .n(36)
         .dur(3.0F)
         .speed(2.5F)
         .snd("minecraft:block.glass.break");
      add(new SolarTools.Tool(7, "Weihnachtsstern", var1, -11446, "Ein strahlender Stern schlägt ein"))
         .dmg(70.0F, 90.0F, 255.0F, 0.8F)
         .style(3)
         .speed(0.8F)
         .snd("minecraft:block.bell.use");
      add(new SolarTools.Tool(7, "Lichterketten-Strahl", var0, -14006, "Bunte Lichter brennen sich ein"))
         .dmg(10.0F, 18.0F, 90.0F, 0.4F)
         .style(10)
         .width(1.0F)
         .n(3)
         .snd("minecraft:block.bell.use");
      add(new SolarTools.Tool(7, "Schneemann-Armee", var2, -722689, "Schneemänner landen überall"))
         .dmg(14.0F, 20.0F, 0.0F, 0.4F)
         .style(2)
         .sprite(-4)
         .n(16)
         .dur(4.0F)
         .speed(0.8F)
         .snd("minecraft:block.glass.break");
      add(new SolarTools.Tool(7, "Kohle-Regen", var2, -14013910, "Für alle, die unartig waren"))
         .dmg(12.0F, 50.0F, 120.0F, 0.6F)
         .style(0)
         .n(30)
         .dur(3.0F)
         .speed(1.8F)
         .snd("minecraft:entity.generic.explode");
   }

   static enum Kind {
      BEAM,
      SHOT,
      RAIN,
      SWARM,
      AREA,
      GLOBAL,
      HOLE,
      SPLIT;
   }

   static final class Tool {
      final int cat;
      final String name;
      final String desc;
      final SolarTools.Kind kind;
      final int color;
      float r;
      float depth;
      float heat;
      float kill;
      float scorch;
      int style = 0;
      int n = 1;
      int sprite = -1;
      float speed = 1.0F;
      float dur = 1.0F;
      float width = 1.0F;
      String sound = "minecraft:entity.generic.explode";

      Tool(int var1, String var2, SolarTools.Kind var3, int var4, String var5) {
         this.cat = var1;
         this.name = var2;
         this.kind = var3;
         this.color = var4;
         this.desc = var5;
      }

      SolarTools.Tool dmg(float var1, float var2, float var3, float var4) {
         this.r = var1;
         this.depth = var2;
         this.heat = var3;
         this.kill = var4;
         this.scorch = 1.8F;
         return this;
      }

      SolarTools.Tool style(int var1) {
         this.style = var1;
         return this;
      }

      SolarTools.Tool n(int var1) {
         this.n = var1;
         return this;
      }

      SolarTools.Tool speed(float var1) {
         this.speed = var1;
         return this;
      }

      SolarTools.Tool dur(float var1) {
         this.dur = var1;
         return this;
      }

      SolarTools.Tool width(float var1) {
         this.width = var1;
         return this;
      }

      SolarTools.Tool sprite(int var1) {
         this.sprite = var1;
         return this;
      }

      SolarTools.Tool scorch(float var1) {
         this.scorch = var1;
         return this;
      }

      SolarTools.Tool snd(String var1) {
         this.sound = var1;
         return this;
      }
   }
}
