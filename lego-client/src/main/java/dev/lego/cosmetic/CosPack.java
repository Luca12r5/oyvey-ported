package dev.lego.cosmetic;

final class CosPack {
   private static final Cos.Rarity C = Cos.Rarity.COMMON;
   private static final Cos.Rarity R = Cos.Rarity.RARE;
   private static final Cos.Rarity E = Cos.Rarity.EPIC;
   private static final Cos.Rarity L = Cos.Rarity.LEGENDARY;
   private static final int ORBIT = 0;
   private static final int SPIRAL = 1;
   private static final int RAIN = 2;
   private static final int RISE = 3;
   private static final int RINGS = 4;
   private static final int CROWN = 5;
   private static final int HELIX = 6;
   private static final int BURST = 7;

   private CosPack() {
   }

   static void register() {
      hats();
      faces();
      backs();
      auras();
      wings();
      CapePack.register();
   }

   private static void hat(String id, String name, Cos.Rarity r, Cos.Model m) {
      Cos.add("hat_p4_" + id, name, Cos.Slot.HAT, r, m);
   }

   private static void face(String id, String name, Cos.Rarity r, Cos.Model m) {
      Cos.add("face_p4_" + id, name, Cos.Slot.FACE, r, m);
   }

   private static void back(String id, String name, Cos.Rarity r, Cos.Model m) {
      Cos.add("back_p4_" + id, name, Cos.Slot.BACK, r, m);
   }

   private static void aura(String id, String name, Cos.Rarity r, Cos.Model m) {
      Cos.add("aura_p4_" + id, name, Cos.Slot.AURA, r, m);
   }

   private static void wing(String id, String name, Cos.Rarity r, Cos.Model m) {
      Cos.add("wings_p4_" + id, name, Cos.Slot.WINGS, r, m);
   }

   private static int dark(int c, float k) {
      return 0xFF000000 | Capes.mix(c & 16777215, 0, k);
   }

   private static int light(int c, float k) {
      return 0xFF000000 | Capes.mix(c & 16777215, 16777215, k);
   }

   private static void hats() {
      int[] beanie = new int[]{-2677187, -13728034, -14176672, -812014};
      String[] beanieN = new String[]{"Rote Wollmütze", "Blaue Wollmütze", "Grüne Wollmütze", "Orange Wollmütze"};

      for (int i = 0; i < 4; i++) {
         int c = beanie[i];
         hat("beanie" + i, beanieN[i], C, (g, a) -> beanie(g, a, c));
      }

      int[] cap = new int[]{-1900533, -15000798, -16749385, -1};
      String[] capN = new String[]{"Basecap Rot", "Basecap Schwarz", "Basecap Blau", "Basecap Weiß"};

      for (int i = 0; i < 4; i++) {
         int c = cap[i];
         boolean lg = i == 1;
         hat("cap" + i, capN[i], C, (g, a) -> cap(g, c, lg));
      }

      int[] party = new int[]{-45149, -11549697, -11713};
      String[] partyN = new String[]{"Partyhut Pink", "Partyhut Hellblau", "Partyhut Gelb"};

      for (int i = 0; i < 3; i++) {
         int c = party[i];
         hat("party" + i, partyN[i], R, (g, a) -> partyHat(g, a, c));
      }

      hat("chef", "Sterne-Kochmütze", R, CosPack::chefHat);
      int[] cow = new int[]{-7644629, -13948117, -1518160};
      String[] cowN = new String[]{"Cowboyhut Braun", "Cowboyhut Schwarz", "Cowboyhut Sand"};

      for (int i = 0; i < 3; i++) {
         int c = cow[i];
         hat("cowboy" + i, cowN[i], R, (g, a) -> cowboy(g, c));
      }

      hat("tophat_red", "Zylinder Rot", R, (g, a) -> topHat(g, -5238245, -15000798));
      hat("tophat_white", "Zylinder Weiß", E, (g, a) -> topHat(g, -723724, -1900533));
      hat("fez_red", "Fes Rot", R, (g, a) -> fez(g, a, -4189156));
      hat("fez_blue", "Fes Blau", R, (g, a) -> fez(g, a, -14725200));
      hat("sombrero", "Sombrero", E, CosPack::sombrero);
      hat("grad_black", "Doktorhut", R, (g, a) -> gradCap(g, a, -15000798, -15043));
      hat("grad_blue", "Abschlusshut Blau", R, (g, a) -> gradCap(g, a, -15189364, -1));
      hat("hardhat_y", "Bauhelm Gelb", C, (g, a) -> hardHat(g, -16121));
      hat("hardhat_w", "Bauhelm Weiß", C, (g, a) -> hardHat(g, -855310));
      hat("viking", "Wikingerhelm Eisen", E, (g, a) -> viking(g, -6642771, -857904));
      hat("viking_gold", "Goldener Wikingerhelm", L, (g, a) -> viking(g, -1525460, -1));
      hat("propeller", "Propellermütze Rot-Gelb", R, (g, a) -> propeller(g, a, -1900533, -13053));
      hat("propeller2", "Propellermütze Bunt", R, (g, a) -> propeller(g, a, -13728034, -14176672));
      int[][] flowers = new int[][]{{-36939, -1}, {-11713, -30208}, {-6591489, -8587265}};
      String[] flowerN = new String[]{"Blumenkranz Rosa", "Sonnenblumenkranz", "Blumenkranz Lila"};

      for (int i = 0; i < 3; i++) {
         int[] f = flowers[i];
         hat("flowers" + i, flowerN[i], E, (g, a) -> flowerCrown(g, a, f[0], f[1]));
      }

      hat("antlers", "Hirschgeweih", E, (g, a) -> antlers(g, -5207462, false));
      hat("reindeer", "Rudolph-Geweih", R, (g, a) -> antlers(g, -7710160, true));
      int[] bows = new int[]{-45149, -1900533, -11549697};
      String[] bowN = new String[]{"Haarschleife Pink", "Haarschleife Rot", "Haarschleife Blau"};

      for (int i = 0; i < 3; i++) {
         int c = bows[i];
         hat("bow" + i, bowN[i], C, (g, a) -> bow(g, a, c));
      }

      int[] berets = new int[]{-15000798, -5238245, -13800881};
      String[] beretN = new String[]{"Baskenmütze Schwarz", "Baskenmütze Rot", "Baskenmütze Grün"};

      for (int i = 0; i < 3; i++) {
         int c = berets[i];
         hat("beret" + i, beretN[i], C, (g, a) -> beret(g, c));
      }

      hat("bucket0", "Fischerhut Beige", C, (g, a) -> bucketHat(g, -2570080));
      hat("bucket1", "Fischerhut Tarn", C, (g, a) -> bucketHat(g, -10851526));
      hat("pirate", "Piratenhut", E, CosPack::pirateHat);
      hat("police", "Polizeimütze", R, CosPack::policeCap);
      hat("knight", "Ritterhelm", E, (g, a) -> knight(g, -4669238, -1900533));
      hat("knight_gold", "Goldener Ritterhelm", L, (g, a) -> knight(g, -1525460, -12944641));
      int[] wiz = new int[]{-14017942, -15771078, -8777942};
      String[] wizN = new String[]{"Zauberhut Nachtblau", "Zauberhut Smaragd", "Zauberhut Weinrot"};

      for (int i = 0; i < 3; i++) {
         int c = wiz[i];
         hat("wizard" + i, wizN[i], E, (g, a) -> wizard(g, a, c));
      }

      hat("tiara", "Tiara", E, (g, a) -> tiara(g, a, -2038032, -8587265));
      hat("tiara_rose", "Rosen-Tiara", E, (g, a) -> tiara(g, a, -1525460, -45149));
      hat("bear_ears", "Bärenohren", C, (g, a) -> roundEars(g, -8762840, -2050934, 2.0F));
      hat("panda_ears", "Pandaohren", C, (g, a) -> roundEars(g, -15000798, -12961212, 2.0F));
      hat("mouse_ears", "Mäuseohren", R, (g, a) -> roundEars(g, -15000798, -25920, 2.8F));
      hat("fox_ears", "Fuchsohren-Reif", R, (g, a) -> pointEars(g, -1543126, -1));
      hat("wolf_ears", "Wolfsohren", R, (g, a) -> pointEars(g, -9801600, -3091236));
      hat("unicorn", "Einhorn-Horn", L, CosPack::unicorn);
      hat("alien", "Alien-Fühler", R, (g, a) -> antennae(g, a, -12987565));
      hat("bee", "Bienen-Fühler", C, (g, a) -> antennae(g, a, -15000798));
      int[] halos = new int[]{-42406, -10817281, -5153537};
      String[] haloN = new String[]{"Roter Heiligenschein", "Eisiger Heiligenschein", "Violetter Heiligenschein"};

      for (int i = 0; i < 3; i++) {
         int c = halos[i];
         hat("halo" + i, haloN[i], E, (g, a) -> halo(g, a, c));
      }

      hat("orbit_gold", "Goldener Orbit", L, (g, a) -> orbitCrown(g, a, -11713));
      hat("orbit_ice", "Eis-Orbit", L, (g, a) -> orbitCrown(g, a, -7673601));
      hat("mushroom_red", "Fliegenpilz", R, (g, a) -> mushroom(g, -2677202, true));
      hat("mushroom_brown", "Steinpilz", R, (g, a) -> mushroom(g, -7710160, false));
      hat("chick", "Küken auf dem Kopf", E, CosPack::chick);
      hat("diving", "Taucherhelm", E, CosPack::divingHelmet);
      int[] brick = new int[]{-1900533, -16749385, -13053};
      String[] brickN = new String[]{"Lego-Stein-Hut Rot", "Lego-Stein-Hut Blau", "Lego-Stein-Hut Gelb"};

      for (int i = 0; i < 3; i++) {
         int c = brick[i];
         hat("brick" + i, brickN[i], R, (g, a) -> brickHat(g, c));
      }
   }

   static void beanie(G g, Cos.A a, int c) {
      g.color(c);
      Cos3Geo.lathe(g, "hd_knit", new float[]{4.5F, 4.55F, 4.4F, 3.7F, 2.4F, 0.0F}, new float[]{7.0F, 8.2F, 9.6F, 10.9F, 11.7F, 12.0F}, 20);
      g.color(dark(c, 0.15F));
      Cos3Geo.lathe(g, "hd_knit", new float[]{4.45F, 4.75F, 4.75F, 4.45F}, new float[]{6.0F, 6.2F, 7.8F, 8.0F}, 20);
      g.color(-1);
      Cos3Geo.ball(g, "hd_fluff", 0.0F, 12.6F + Cos3Geo.sin(a.time * 3.0F) * 0.08F, 0.0F, 1.25F, 10);
   }

   static void cap(G g, int c, boolean logo) {
      g.color(c);
      Cos3Geo.lathe(g, "hd_cotton", new float[]{4.45F, 4.5F, 4.2F, 3.2F, 1.6F, 0.0F}, new float[]{6.4F, 7.6F, 8.9F, 9.9F, 10.4F, 10.5F}, 20);
      g.push();
      g.translate(0.0F, 6.5F, 3.6F);
      g.rotX(-8.0F);
      g.color(dark(c, 0.12F));
      Cos3Geo.extrude(g, "hd_cotton", new float[][]{{-3.6F, 0.0F}, {3.6F, 0.0F}, {3.0F, 3.2F}, {1.5F, 4.0F}, {-1.5F, 4.0F}, {-3.0F, 3.2F}}, -0.15F, 0.15F);
      g.pop();
      g.color(light(c, 0.3F));
      Cos3Geo.ball(g, "hd_cotton", 0.0F, 10.45F, 0.0F, 0.45F, 6);
      if (logo) {
         g.color(-1900533);
         Cos3Geo.cbox(g, "hd_brick", 0.0F, 8.6F, 4.15F, 1.6F, 1.0F, 0.3F, 0.1F);
      }
   }

   static void partyHat(G g, Cos.A a, int c) {
      g.push();
      g.translate(1.2F, 8.0F, 0.0F);
      g.rotZ(-12.0F);
      g.color(c);
      Cos3Geo.lathe(g, "hd_party", new float[]{2.6F, 2.0F, 1.2F, 0.4F, 0.0F}, new float[]{0.0F, 2.2F, 4.6F, 6.8F, 7.6F}, 18);
      g.color(-1);
      Cos3Geo.ball(g, "hd_fluff", 0.0F, 7.9F, 0.0F, 0.9F, 8);
      g.color(light(c, 0.5F));
      g.torus("hd_fluff", 2.6F, 0.35F, 18, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
   }

   static void chefHat(G g, Cos.A a) {
      g.color(-460552);
      g.cylinder("hd_cotton", 4.3F, 4.6F, 7.0F, 10.5F, 20, false, 0.0F, 0.0F, 1.0F, 1.0F);

      for (int i = 0; i < 6; i++) {
         double an = (Math.PI * 2) * i / 6.0;
         Cos3Geo.ball(g, "hd_cotton", Cos3Geo.cos(an) * 2.6F, 11.6F, Cos3Geo.sin(an) * 2.6F, 2.6F, 10);
      }

      Cos3Geo.ball(g, "hd_cotton", 0.0F, 12.4F, 0.0F, 2.8F, 10);
   }

   static void cowboy(G g, int c) {
      g.color(c);
      Cos3Geo.lathe(g, "hd_leather", new float[]{7.6F, 7.4F, 5.6F, 4.6F, 4.6F, 4.6F}, new float[]{8.6F, 8.0F, 7.3F, 7.4F, 7.5F, 7.6F}, 24);
      Cos3Geo.lathe(g, "hd_leather", new float[]{4.5F, 4.4F, 4.0F, 2.2F, 0.0F}, new float[]{7.4F, 10.5F, 11.4F, 11.0F, 10.4F}, 20);
      g.color(dark(c, 0.55F));
      g.cylinder("hd_hatband", 4.52F, 4.47F, 7.6F, 8.4F, 20, false, 0.0F, 0.0F, 1.0F, 1.0F);
   }

   static void topHat(G g, int c, int band) {
      g.color(c);
      Cos3Geo.lathe(g, "hd_felt", new float[]{6.2F, 6.0F, 4.4F, 4.4F}, new float[]{8.3F, 7.8F, 7.4F, 7.6F}, 24);
      g.cylinder("hd_felt", 4.1F, 4.4F, 7.6F, 14.2F, 22, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.ring("hd_felt", 0.0F, 4.4F, 14.2F, 22, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(band);
      g.cylinder("hd_satin", 4.48F, 4.44F, 8.0F, 9.1F, 22, false, 0.0F, 0.0F, 1.0F, 1.0F);
   }

   static void fez(G g, Cos.A a, int c) {
      g.color(c);
      g.cylinder("hd_felt", 3.5F, 2.7F, 7.6F, 11.4F, 20, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.ring("hd_felt", 0.0F, 2.7F, 11.4F, 20, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-15000798);
      float sw = Cos3Geo.sin(a.time * 2.0F) * 8.0F;
      g.push();
      g.translate(0.0F, 11.4F, 0.0F);
      g.rotZ(70.0F + sw);
      Geo.tube(g, "hd_cotton", new float[]{0.0F, 0.0F, 0.0F}, new float[]{0.0F, 2.6F, 0.0F}, 0.12F, 0.12F, 5);
      g.translate(0.0F, 2.6F, 0.0F);
      Cos3Geo.lathe(g, "hd_cotton", new float[]{0.0F, 0.5F, 0.2F}, new float[]{-1.3F, -0.2F, 0.2F}, 8);
      g.pop();
   }

   static void sombrero(G g, Cos.A a) {
      g.color(-1521798);
      Cos3Geo.lathe(g, "hd_wood", new float[]{10.5F, 10.2F, 7.0F, 4.6F}, new float[]{8.8F, 8.2F, 7.6F, 7.6F}, 28);
      Cos3Geo.lathe(g, "hd_wood", new float[]{4.5F, 4.2F, 3.4F, 1.8F, 0.0F}, new float[]{7.5F, 11.0F, 12.6F, 13.4F, 13.5F}, 22);
      g.color(-1900533);
      g.torus("hd_cotton", 10.3F, 0.35F, 28, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-13722038);
      g.cylinder("hd_cotton", 4.46F, 4.3F, 8.0F, 9.0F, 22, false, 0.0F, 0.0F, 1.0F, 1.0F);
   }

   static void gradCap(G g, Cos.A a, int c, int tassel) {
      g.color(c);
      Cos3Geo.lathe(g, "hd_felt", new float[]{4.4F, 4.4F, 4.2F}, new float[]{7.0F, 9.4F, 9.6F}, 20);
      g.push();
      g.translate(0.0F, 9.7F, 0.0F);
      g.rotY(45.0F);
      Cos3Geo.cbox(g, "hd_felt", 0.0F, 0.0F, 0.0F, 10.5F, 0.4F, 10.5F, 0.08F);
      g.pop();
      g.color(tassel);
      Cos3Geo.ball(g, "hd_satin", 0.0F, 10.0F, 0.0F, 0.45F, 6);
      float sw = Cos3Geo.sin(a.time * 2.2F) * 0.5F;
      Geo.chain(
         g,
         "hd_satin",
         new float[][]{{0.0F, 10.0F, 0.0F}, {3.4F, 10.0F, -0.4F}, {4.2F + sw * 0.2F, 8.6F, -0.6F}, {4.3F + sw * 0.3F, 7.0F, -0.6F}},
         new float[]{0.12F, 0.12F, 0.12F, 0.12F},
         5
      );
      Cos3Geo.lathe(g, "hd_satin", new float[]{0.2F, 0.55F, 0.5F, 0.0F}, new float[]{7.2F, 6.6F, 5.4F, 5.2F}, 8);
   }

   static void hardHat(G g, int c) {
      g.color(c);
      Cos3Geo.lathe(
         g, "hd_plastic", new float[]{5.6F, 5.4F, 4.6F, 4.6F, 4.3F, 3.2F, 1.4F, 0.0F}, new float[]{7.2F, 6.8F, 6.9F, 8.6F, 10.0F, 11.1F, 11.7F, 11.8F}, 22
      );
      g.color(dark(c, 0.1F));
      Cos3Geo.cbox(g, "hd_plastic", 0.0F, 10.0F, 0.0F, 1.1F, 3.6F, 9.4F, 0.4F);
      g.color(-15000798);
      g.cylinder("hd_rubber", 4.62F, 4.62F, 7.2F, 7.6F, 22, false, 0.0F, 0.0F, 1.0F, 1.0F);
   }

   static void viking(G g, int metal, int horn) {
      g.color(metal);
      Cos3Geo.lathe(g, "hd_steel", new float[]{4.7F, 4.6F, 4.2F, 3.0F, 1.2F, 0.0F}, new float[]{6.4F, 8.6F, 10.2F, 11.4F, 12.0F, 12.1F}, 22);
      g.color(dark(metal, 0.25F));
      g.torus("hd_steel", 4.65F, 0.35F, 22, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      Cos3Geo.cbox(g, "hd_steel", 0.0F, 7.5F, 4.55F, 0.8F, 3.8F, 0.5F, 0.15F);
      g.color(horn);

      for (int s = -1; s <= 1; s += 2) {
         Geo.chain(
            g,
            "hd_horn",
            new float[][]{{s * 4.2F, 9.0F, 0.0F}, {s * 6.0F, 9.6F, 0.0F}, {s * 7.0F, 11.4F, 0.3F}, {s * 7.2F, 13.6F, 0.6F}},
            new float[]{1.1F, 0.85F, 0.5F, 0.08F},
            8
         );
      }
   }

   static void propeller(G g, Cos.A a, int c1, int c2) {
      for (int i = 0; i < 4; i++) {
         g.color(i % 2 == 0 ? c1 : c2);
         g.push();
         g.rotY(i * 90);
         Cos3Geo.lathe(g, "hd_cotton", new float[]{4.45F, 4.4F, 3.8F, 2.4F, 0.0F}, new float[]{6.4F, 8.2F, 9.9F, 11.0F, 11.3F}, 6, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      g.color(-13053);
      g.cylinder("hd_plastic", 0.25F, 0.25F, 11.2F, 12.6F, 6, true, 0.0F, 0.0F, 1.0F, 1.0F);
      g.push();
      g.translate(0.0F, 12.6F, 0.0F);
      g.rotY(a.time * 720.0F);

      for (int i = 0; i < 2; i++) {
         g.color(i == 0 ? -13728034 : -1900533);
         g.push();
         g.rotY(i * 180);
         g.rotX(14.0F);
         Cos3Geo.cbox(g, "hd_plastic", 2.2F, 0.0F, 0.0F, 4.0F, 0.15F, 1.0F, 0.06F);
         g.pop();
      }

      g.pop();
   }

   static void flowerCrown(G g, Cos.A a, int petal, int center) {
      g.color(-12677567);
      g.push();
      g.translate(0.0F, 8.0F, 0.0F);
      g.torus("hd_vine", 4.3F, 0.32F, 22, 6, 0.0F, 0.0F, 1.0F, 1.0F);

      for (int i = 0; i < 9; i++) {
         double an = (Math.PI * 2) * i / 9.0;
         float x = Cos3Geo.cos(an) * 4.4F;
         float z = Cos3Geo.sin(an) * 4.4F;
         g.push();
         g.translate(x, 0.3F, z);
         g.rotY((float)(-Math.toDegrees(an)) + 90.0F);
         g.rotX(-70.0F);
         int col = i % 3 == 0 ? center : petal;
         g.color(col);

         for (int k = 0; k < 5; k++) {
            g.push();
            g.rotY(k * 72);
            Geo.ellipsoid(g, "hd_petal", 0.0F, 0.0F, 0.75F, 0.45F, 0.18F, 0.8F, 6, 3);
            g.pop();
         }

         g.color(i % 3 == 0 ? petal : center);
         Cos3Geo.ball(g, "hd_softglow", 0.0F, 0.15F, 0.0F, 0.35F, 6);
         g.pop();
         g.color(-11555246);
         g.push();
         g.translate(Cos3Geo.cos(an + 0.35) * 4.5F, 0.0F, Cos3Geo.sin(an + 0.35) * 4.5F);
         g.rotY((float)(-Math.toDegrees(an)));
         Geo.ellipsoid(g, "hd_leaf", 0.0F, 0.0F, 0.0F, 0.5F, 0.1F, 0.25F, 5, 3);
         g.pop();
      }

      g.pop();
   }

   static void antlers(G g, int c, boolean nose) {
      g.color(c);

      for (int s = -1; s <= 1; s += 2) {
         Geo.chain(
            g,
            "hd_horn",
            new float[][]{{s * 2.2F, 7.8F, 0.0F}, {s * 2.8F, 9.6F, -0.2F}, {s * 3.6F, 11.6F, -0.4F}, {s * 4.0F, 13.6F, -0.2F}},
            new float[]{0.45F, 0.4F, 0.32F, 0.18F},
            6
         );
         Geo.chain(g, "hd_horn", new float[][]{{s * 3.0F, 10.0F, -0.2F}, {s * 4.6F, 11.0F, 0.0F}, {s * 5.2F, 12.4F, 0.2F}}, new float[]{0.3F, 0.24F, 0.12F}, 6);
         Geo.chain(g, "hd_horn", new float[][]{{s * 3.6F, 11.8F, -0.4F}, {s * 2.6F, 13.0F, -0.4F}, {s * 2.4F, 14.0F, -0.3F}}, new float[]{0.26F, 0.2F, 0.1F}, 6);
      }

      if (nose) {
         g.color(-54742).glow(true);
         Cos3Geo.ball(g, "hd_gloss", 0.0F, 3.0F, 4.4F, 0.75F, 10);
         g.glow(false);
      }
   }

   static void bow(G g, Cos.A a, int c) {
      g.push();
      g.translate(2.2F, 8.2F, 0.5F);
      g.rotZ(-20.0F);
      g.color(c);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.scale(s, 1.0F, 1.0F);
         Cos3Geo.lathe(g, "hd_satin", new float[]{0.3F, 1.4F, 1.3F, 0.0F}, new float[]{0.0F, 0.6F, 2.4F, 3.0F}, 10);
         g.pop();
         g.push();
         g.rotZ(s * 90);
         Cos3Geo.lathe(g, "hd_satin", new float[]{0.3F, 1.2F, 1.1F, 0.0F}, new float[]{0.0F, 0.5F, 2.2F, 2.6F}, 10);
         g.pop();
      }

      g.color(dark(c, 0.15F));
      Cos3Geo.ball(g, "hd_satin", 0.0F, 0.0F, 0.0F, 0.7F, 8);
      g.pop();
   }

   static void beret(G g, int c) {
      g.push();
      g.translate(0.6F, 0.0F, 0.0F);
      g.rotZ(-8.0F);
      g.color(c);
      Cos3Geo.lathe(g, "hd_felt", new float[]{4.4F, 4.7F, 5.2F, 4.6F, 2.6F, 0.0F}, new float[]{7.2F, 7.6F, 8.6F, 9.6F, 10.1F, 10.2F}, 22);
      g.color(dark(c, 0.2F));
      g.cylinder("hd_felt", 0.2F, 0.12F, 10.1F, 10.9F, 6, true, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
   }

   static void bucketHat(G g, int c) {
      g.color(c);
      Cos3Geo.lathe(g, "hd_cotton", new float[]{6.4F, 6.2F, 4.6F, 4.4F, 4.0F, 0.0F}, new float[]{6.0F, 6.4F, 7.6F, 10.0F, 10.6F, 10.7F}, 22);
      g.color(dark(c, 0.2F));
      g.cylinder("hd_stitch", 4.52F, 4.42F, 7.8F, 8.4F, 22, false, 0.0F, 0.0F, 1.0F, 1.0F);
   }

   static void pirateHat(G g, Cos.A a) {
      g.color(-15000798);
      Cos3Geo.lathe(g, "hd_felt", new float[]{4.5F, 4.4F, 3.8F, 0.0F}, new float[]{7.2F, 9.6F, 10.6F, 10.8F}, 20);

      for (int i = 0; i < 3; i++) {
         g.push();
         g.rotY(i * 120);
         g.translate(0.0F, 8.0F, 3.2F);
         g.rotX(-62.0F);
         Cos3Geo.extrude(g, "hd_felt", new float[][]{{-5.0F, 0.0F}, {5.0F, 0.0F}, {3.2F, 3.6F}, {-3.2F, 3.6F}}, -0.15F, 0.15F);
         g.pop();
      }

      g.color(-723724);
      g.push();
      g.translate(0.0F, 9.6F, 4.15F);
      Cos3Geo.ball(g, "hd_bone", 0.0F, 0.4F, 0.0F, 0.8F, 8);
      g.rotZ(45.0F);
      Cos3Geo.cbox(g, "hd_bone", 0.0F, -0.7F, 0.0F, 0.4F, 2.4F, 0.3F, 0.1F);
      g.rotZ(-90.0F);
      Cos3Geo.cbox(g, "hd_bone", 0.0F, -0.7F, 0.0F, 0.4F, 2.4F, 0.3F, 0.1F);
      g.pop();
      g.color(-1525460);
      g.torus("hd_gold", 4.45F, 0.15F, 22, 5, 0.0F, 0.0F, 1.0F, 1.0F);
   }

   static void policeCap(G g, Cos.A a) {
      g.color(-14931371);
      Cos3Geo.lathe(g, "hd_felt", new float[]{4.45F, 4.5F, 5.4F, 5.2F, 0.0F}, new float[]{7.0F, 8.6F, 10.0F, 10.5F, 10.6F}, 22);
      g.color(-15000798);
      g.push();
      g.translate(0.0F, 7.2F, 3.6F);
      g.rotX(18.0F);
      Cos3Geo.extrude(g, "hd_leather", new float[][]{{-3.4F, 0.0F}, {3.4F, 0.0F}, {2.4F, 2.4F}, {-2.4F, 2.4F}}, -0.12F, 0.12F);
      g.pop();
      g.color(-1525460);
      g.push();
      g.translate(0.0F, 9.0F, 4.75F);
      Cos3Geo.gem(g, "hd_gold", 0.9F, 0.4F, 0.4F, 6);
      g.pop();
      g.color(-15000798);
      g.cylinder("hd_hatband", 4.55F, 4.55F, 7.4F, 8.2F, 22, false, 0.0F, 0.0F, 1.0F, 1.0F);
   }

   static void knight(G g, int metal, int plume) {
      g.color(metal);
      Cos3Geo.bbox(g, "hd_steel", -4.6F, -0.4F, -4.6F, 4.6F, 8.8F, 4.6F, 0.6F);
      g.color(dark(metal, 0.7F));

      for (int i = 0; i < 4; i++) {
         Cos3Geo.cbox(g, "hd_steel", 0.0F, 4.6F - i * 0.9F, 4.62F, 6.2F, 0.25F, 0.1F, 0.05F);
      }

      g.color(dark(metal, 0.2F));
      Cos3Geo.cbox(g, "hd_steel", 0.0F, 5.4F, 4.7F, 0.6F, 6.4F, 0.4F, 0.1F);
      g.color(plume);
      Geo.chain(
         g,
         "hd_fluff",
         new float[][]{{0.0F, 8.8F, 1.0F}, {0.0F, 11.0F, 0.0F}, {0.0F, 12.0F, -2.2F}, {0.0F, 11.0F, -4.6F}},
         new float[]{0.6F, 1.0F, 0.9F, 0.3F},
         8
      );
   }

   static void wizard(G g, Cos.A a, int c) {
      g.color(c);
      Cos3Geo.lathe(g, "hd_wizard", new float[]{7.0F, 6.8F, 4.6F}, new float[]{8.0F, 7.6F, 7.4F}, 24);
      g.push();
      g.translate(0.0F, 7.4F, 0.0F);
      float t = a.time;
      float[][] pts = new float[7][];
      float[] rad = new float[7];

      for (int i = 0; i < 7; i++) {
         float f = i / 6.0F;
         pts[i] = new float[]{f * f * 2.6F + Cos3Geo.sin(t * 1.4 + f * 2.0F) * 0.2F * f, f * 9.5F, -f * f * 1.2F};
         rad[i] = 4.5F * (1.0F - f) + 0.15F;
      }

      Geo.chain(g, "hd_wizard", pts, rad, 18);
      g.glow(true);

      for (int i = 0; i < 6; i++) {
         double an = i * 1.7 + t * 0.3;
         float y = 1.2F + i * 1.15F;
         float r = 4.6F * (1.0F - y / 9.5F) + 0.1F;
         g.color(-7558);
         g.push();
         g.translate(Cos3Geo.cos(an) * r + y / 9.5F * (y / 9.5F) * 2.6F, y, Cos3Geo.sin(an) * r);
         Cos3Geo.gem(g, "white", 0.25F, 0.35F, 0.35F, 4);
         g.pop();
      }

      g.glow(false);
      g.pop();
   }

   static void tiara(G g, Cos.A a, int metal, int gem) {
      g.color(metal);
      g.push();
      g.translate(0.0F, 7.6F, 0.0F);

      for (int i = -4; i <= 4; i++) {
         double an = (Math.PI / 2) + i * 0.22;
         g.push();
         g.translate(Cos3Geo.cos(an) * 4.4F, 0.0F, Cos3Geo.sin(an) * 4.4F);
         g.rotY((float)(-Math.toDegrees(an)) + 90.0F);
         float h = 1.6F - Math.abs(i) * 0.14F;
         Cos3Geo.extrude(g, "hd_silver", new float[][]{{-0.5F, 0.0F}, {0.5F, 0.0F}, {0.12F, h}, {-0.12F, h}}, -0.1F, 0.1F);
         g.pop();
      }

      g.torus("hd_silver", 4.4F, 0.16F, 24, 5, 0.0F, 0.0F, 1.0F, 1.0F);
      g.glow(true).color(gem);
      g.push();
      g.translate(0.0F, 2.0F, 4.45F);
      Cos3Geo.gem(g, "hd_gem", 0.6F, 0.7F, 0.9F, 6);
      g.pop();

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 1.5F, 1.3F, 4.2F);
         Cos3Geo.gem(g, "hd_gem", 0.35F, 0.4F, 0.5F, 6);
         g.pop();
      }

      g.glow(false);
      g.pop();
   }

   static void roundEars(G g, int outer, int inner, float r) {
      g.color(-15000798);
      g.torus("hd_plastic", 4.4F, 0.22F, 22, 5, 0.0F, 0.0F, 1.0F, 1.0F);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 3.2F, 8.4F + r * 0.6F, 0.0F);
         g.rotZ(-s * 15);
         g.color(outer);
         Geo.ellipsoid(g, "hd_fur", 0.0F, 0.0F, 0.0F, r, r, 0.7F, 12, 6);
         g.color(inner);
         Geo.ellipsoid(g, "hd_fur", 0.0F, -0.1F, 0.35F, r * 0.62F, r * 0.62F, 0.45F, 10, 5);
         g.pop();
      }
   }

   static void pointEars(G g, int outer, int tip) {
      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 2.8F, 7.8F, 0.2F);
         g.rotZ(-s * 18);
         g.color(outer);
         Cos3Geo.lathe(g, "hd_fur", new float[]{1.7F, 1.5F, 0.9F, 0.3F, 0.0F}, new float[]{0.0F, 1.2F, 2.8F, 4.0F, 4.4F}, 4);
         g.color(tip);
         g.translate(0.0F, 3.3F, 0.0F);
         Cos3Geo.lathe(g, "hd_fur", new float[]{0.62F, 0.3F, 0.0F}, new float[]{0.0F, 0.8F, 1.15F}, 4);
         g.pop();
      }
   }

   static void unicorn(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 7.2F, 2.2F);
      g.rotX(28.0F);
      g.glow(true);
      int n = 8;

      for (int i = 0; i < n; i++) {
         float f0 = (float)i / n;
         float f1 = (i + 1.0F) / n;
         g.color(Geo.hue(a.time * 0.15 + f0 * 0.6, 0.45, 1.0));
         Cos3Geo.lathe(g, "hd_gloss", new float[]{1.0F * (1.0F - f0) + 0.05F, 1.0F * (1.0F - f1) + 0.05F}, new float[]{f0 * 7.0F, f1 * 7.0F}, 10);
      }

      g.glow(false);
      g.pop();
      g.color(-1);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 3.6F, 7.6F, 0.4F);
         g.rotZ(-s * 30);
         Cos3Geo.lathe(g, "hd_fur", new float[]{1.0F, 0.8F, 0.3F, 0.0F}, new float[]{0.0F, 1.0F, 2.2F, 2.5F}, 6);
         g.pop();
      }
   }

   static void antennae(G g, Cos.A a, int ball) {
      for (int s = -1; s <= 1; s += 2) {
         float sw = Cos3Geo.sin(a.time * 3.0F + s) * 0.4F;
         g.color(-15000798);
         Geo.chain(
            g,
            "hd_rubber",
            new float[][]{{s * 1.8F, 7.8F, 0.0F}, {s * 2.4F + sw * 0.3F, 10.0F, 0.2F}, {s * 3.2F + sw, 12.2F, 0.6F}},
            new float[]{0.18F, 0.15F, 0.12F},
            5
         );
         g.color(ball).glow(true);
         Cos3Geo.ball(g, "hd_gloss", s * 3.2F + sw, 12.6F, 0.6F, 0.7F, 10);
         g.glow(false);
      }
   }

   static void halo(G g, Cos.A a, int c) {
      g.push();
      g.translate(0.0F, 10.6F + Cos3Geo.sin(a.time * 2.0F) * 0.3F, 0.0F);
      g.rotX(8.0F);
      g.glow(true).color(c);
      g.torus("hd_softglow", 3.4F, 0.32F, 28, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(Cos3Geo.alpha(0.35, c & 16777215));
      g.torus("hd_softglow", 3.4F, 0.8F, 28, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      g.glow(false);
      g.pop();
   }

   static void orbitCrown(G g, Cos.A a, int c) {
      g.glow(true);

      for (int i = 0; i < 6; i++) {
         double an = a.time * 1.4 + (Math.PI * 2) * i / 6.0;
         float y = 10.4F + Cos3Geo.sin(a.time * 2.0F + i) * 0.5F;
         g.color(c);
         g.push();
         g.translate(Cos3Geo.cos(an) * 4.8F, y, Cos3Geo.sin(an) * 4.8F);
         g.rotY(a.time * 90.0F + i * 30);
         Cos3Geo.gem(g, "hd_gem", 0.55F, 0.8F, 0.8F, 5);
         g.pop();
      }

      g.color(Cos3Geo.alpha(0.4, c & 16777215));
      g.push();
      g.translate(0.0F, 10.4F, 0.0F);
      g.torus("hd_softglow", 4.8F, 0.12F, 32, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.glow(false);
   }

   static void mushroom(G g, int cap, boolean dots) {
      g.color(-857904);
      g.cylinder("hd_mush", 1.6F, 1.9F, 7.6F, 9.6F, 14, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(cap);
      Cos3Geo.lathe(g, "hd_mush", new float[]{0.0F, 5.6F, 6.0F, 5.4F, 3.8F, 1.6F, 0.0F}, new float[]{9.2F, 9.3F, 10.0F, 11.2F, 12.6F, 13.4F, 13.5F}, 24);
      if (dots) {
         g.color(-1);

         for (int i = 0; i < 9; i++) {
            double an = i * 2.4;
            float rr = i == 0 ? 0.0F : 2.4F + i % 3 * 1.0F;
            float y = rr < 0.5F ? 13.5F : (rr < 3.0F ? 12.9F : (rr < 4.0F ? 12.2F : 11.3F));
            Cos3Geo.ball(g, "hd_mush", Cos3Geo.cos(an) * rr, y, Cos3Geo.sin(an) * rr, 0.55F, 6);
         }
      }
   }

   static void chick(G g, Cos.A a) {
      float hop = Math.abs(Cos3Geo.sin(a.time * 3.0F)) * 0.4F;
      g.push();
      g.translate(0.0F, 8.2F + hop, 0.0F);
      g.rotY(Cos3Geo.sin(a.time * 0.7F) * 25.0F);
      g.color(-8118);
      Cos3Geo.ball(g, "hd_fluff", 0.0F, 1.6F, 0.0F, 1.9F, 12);
      Cos3Geo.ball(g, "hd_fluff", 0.0F, 3.8F, 0.6F, 1.3F, 12);
      g.color(-30208);
      g.push();
      g.translate(0.0F, 3.7F, 1.8F);
      g.rotX(90.0F);
      Cos3Geo.lathe(g, "hd_plastic", new float[]{0.4F, 0.0F}, new float[]{0.0F, 0.7F}, 6);
      g.pop();
      g.color(-15000798);

      for (int s = -1; s <= 1; s += 2) {
         Cos3Geo.ball(g, "hd_gloss", s * 0.55F, 4.1F, 1.7F, 0.2F, 5);
      }

      g.color(-867792);

      for (int s = -1; s <= 1; s += 2) {
         Geo.ellipsoid(g, "hd_fluff", s * 1.8F, 1.8F, -0.2F, 0.4F, 1.0F, 1.2F, 6, 4);
      }

      g.pop();
   }

   static void divingHelmet(G g, Cos.A a) {
      g.color(-3635398);
      Cos3Geo.lathe(g, "hd_gold", new float[]{5.4F, 5.6F, 5.6F, 5.2F, 4.0F, 2.0F, 0.0F}, new float[]{-0.6F, 0.4F, 6.0F, 8.4F, 10.0F, 10.9F, 11.0F}, 24);

      for (int i = 0; i < 3; i++) {
         double an = (Math.PI / 2) + (i - 1) * Math.PI / 2.0;
         g.push();
         g.translate(Cos3Geo.cos(an) * 5.3F, 4.6F, Cos3Geo.sin(an) * 5.3F);
         g.rotY((float)(-Math.toDegrees(an)) + 90.0F);
         g.rotX(90.0F);
         g.color(-4688342);
         g.torus("hd_gold", 1.9F, 0.35F, 18, 5, 0.0F, 0.0F, 1.0F, 1.0F);
         g.color(-2002196225);
         g.ring("hd_glass", 0.0F, 1.9F, 0.0F, 18, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }
   }

   static void brickHat(G g, int c) {
      g.color(c);
      Cos3Geo.bbox(g, "hd_brick", -4.6F, 7.4F, -4.6F, 4.6F, 11.4F, 4.6F, 0.35F);

      for (int ix = 0; ix < 2; ix++) {
         for (int iz = 0; iz < 2; iz++) {
            g.push();
            g.translate(-2.3F + ix * 4.6F, 11.4F, -2.3F + iz * 4.6F);
            Cos3Geo.lathe(g, "hd_brick", new float[]{1.45F, 1.45F, 1.35F, 0.0F}, new float[]{-0.02F, 1.0F, 1.15F, 1.18F}, 16);
            g.pop();
         }
      }
   }

   private static void faces() {
      int[] rnd = new int[]{-15000798, -1525460, -1900533};
      String[] rndN = new String[]{"Runde Brille Schwarz", "Runde Brille Gold", "Runde Brille Rot"};

      for (int i = 0; i < 3; i++) {
         int c = rnd[i];
         face("round" + i, rndN[i], C, (g, a) -> roundGlasses(g, c, 1439230207));
      }

      int[] stars = new int[]{-11713, -45149, -11549697};
      String[] starN = new String[]{"Sternenbrille Gold", "Sternenbrille Pink", "Sternenbrille Blau"};

      for (int i = 0; i < 3; i++) {
         int c = stars[i];
         face("star" + i, starN[i], R, (g, a) -> shapeGlasses(g, a, c, true));
      }

      int[] hearts = new int[]{-54694, -30016, -5153537};
      String[] heartN = new String[]{"Herzbrille Rot", "Herzbrille Rosa", "Herzbrille Lila"};

      for (int i = 0; i < 3; i++) {
         int c = hearts[i];
         face("heart" + i, heartN[i], R, (g, a) -> shapeGlasses(g, a, c, false));
      }

      face("monocle_gold", "Monokel Gold", E, (g, a) -> monocle(g, -1525460));
      face("monocle_silver", "Monokel Silber", E, (g, a) -> monocle(g, -3090722));
      face("mustache_brown", "Schnurrbart Braun", C, (g, a) -> mustache(g, -10864098, false));
      face("mustache_black", "Schnurrbart Schwarz", C, (g, a) -> mustache(g, -15000798, false));
      face("mustache_curl", "Zwirbelbart", R, (g, a) -> mustache(g, -12966892, true));
      face("beard_brown", "Vollbart Braun", R, (g, a) -> beard(g, -10864098));
      face("beard_white", "Weihnachtsbart", R, (g, a) -> beard(g, -723724));
      face("clown", "Rote Clownsnase", C, (g, a) -> {
         g.color(-1900533);
         Cos3Geo.ball(g, "hd_gloss", 0.0F, 3.0F, 4.7F, 0.95F, 12);
      });
      face("eyepatch", "Augenklappe", R, CosPack::eyePatch);
      face("bandit_black", "Banditenmaske", R, (g, a) -> bandit(g, -15000798));
      face("bandit_red", "Superhelden-Maske", R, (g, a) -> bandit(g, -4189156));
      face("vr_black", "VR-Brille", E, (g, a) -> vr(g, a, -14539732, -10817281));
      face("vr_white", "VR-Brille Weiß", E, (g, a) -> vr(g, a, -855310, -42256));
      int[] ski = new int[]{-30208, -12987565, -5153537};
      String[] skiN = new String[]{"Skibrille Orange", "Skibrille Grün", "Skibrille Lila"};

      for (int i = 0; i < 3; i++) {
         int c = ski[i];
         face("ski" + i, skiN[i], R, (g, a) -> skiGoggles(g, c));
      }

      face("scuba", "Taucherbrille", R, CosPack::scuba);
      face("ninja_black", "Ninja-Maske Schwarz", R, (g, a) -> ninjaMask(g, -15000798));
      face("ninja_red", "Ninja-Maske Rot", R, (g, a) -> ninjaMask(g, -6680552));
      face("whiskers", "Katzen-Schnurrhaare", C, CosPack::whiskers);
      face("3d", "3D-Brille", C, CosPack::glasses3d);
      face("nerd", "Nerd-Brille", C, (g, a) -> squareGlasses(g, -15000798, 0.55F));
      int[] shut = new int[]{-45149, -12987565, -1};
      String[] shutN = new String[]{"Shutter Shades Pink", "Shutter Shades Grün", "Shutter Shades Weiß"};

      for (int i = 0; i < 3; i++) {
         int c = shut[i];
         face("shutter" + i, shutN[i], R, (g, a) -> shutterShades(g, c));
      }

      face("laser_red", "Laseraugen Rot", L, (g, a) -> laserEyes(g, a, -54742));
      face("laser_blue", "Laseraugen Blau", L, (g, a) -> laserEyes(g, a, -12932865));
      face("headset_black", "Gamer-Headset", E, (g, a) -> headset(g, a, -14539732, -12987565));
      face("headset_pink", "Gamer-Headset Pink", E, (g, a) -> headset(g, a, -30016, -1));
      face("rose", "Rose im Mund", R, CosPack::rose);
      face("lolly", "Lolli", C, CosPack::lolly);
   }

   private static void temples(G g, int c) {
      g.color(c);

      for (int s = -1; s <= 1; s += 2) {
         Cos3Geo.cbox(g, "hd_plastic", s * 4.35F, 4.3F, 1.4F, 0.22F, 0.35F, 6.0F, 0.05F);
      }
   }

   static void roundGlasses(G g, int frame, int lens) {
      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 2.0F, 4.2F, 4.4F);
         g.rotX(90.0F);
         g.color(frame);
         g.torus("hd_plastic", 1.45F, 0.18F, 18, 5, 0.0F, 0.0F, 1.0F, 1.0F);
         g.color(lens);
         g.ring("hd_glass", 0.0F, 1.4F, 0.0F, 18, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      g.color(frame);
      Geo.chain(g, "hd_plastic", new float[][]{{-0.6F, 4.4F, 4.45F}, {0.0F, 4.6F, 4.5F}, {0.6F, 4.4F, 4.45F}}, new float[]{0.14F, 0.14F, 0.14F}, 5);
      temples(g, frame);
   }

   static void squareGlasses(G g, int frame, float thick) {
      g.color(frame);

      for (int s = -1; s <= 1; s += 2) {
         float cx = s * 2.0F;
         Cos3Geo.cbox(g, "hd_plastic", cx, 5.4F, 4.45F, 3.4F, thick, 0.4F, 0.1F);
         Cos3Geo.cbox(g, "hd_plastic", cx, 3.0F, 4.45F, 3.4F, thick, 0.4F, 0.1F);
         Cos3Geo.cbox(g, "hd_plastic", cx - 1.5F, 4.2F, 4.45F, thick, 2.8F, 0.4F, 0.1F);
         Cos3Geo.cbox(g, "hd_plastic", cx + 1.5F, 4.2F, 4.45F, thick, 2.8F, 0.4F, 0.1F);
         g.color(1439230207);
         Cos3Geo.cbox(g, "hd_glass", cx, 4.2F, 4.42F, 2.6F, 2.0F, 0.08F, 0.02F);
         g.color(frame);
      }

      Cos3Geo.cbox(g, "hd_plastic", 0.0F, 4.6F, 4.45F, 1.0F, 0.35F, 0.3F, 0.08F);
      temples(g, frame);
   }

   static void shapeGlasses(G g, Cos.A a, int c, boolean star) {
      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 2.1F, 4.3F, 4.3F);
         float pulse = 1.0F + 0.04F * Cos3Geo.sin(a.time * 4.0F);
         g.scale(pulse);
         g.color(c);
         float[][] poly = star ? starPoly(1.95F, 0.85F, 5) : heartPoly(1.7F);
         Cos3Geo.extrude(g, "hd_plastic", poly, 0.0F, 0.45F);
         g.color(-1711276033);
         float[][] in = star ? starPoly(1.35F, 0.6F, 5) : heartPoly(1.2F);
         Cos3Geo.extrude(g, "hd_glass", in, 0.46F, 0.5F);
         g.pop();
      }

      g.color(c);
      Cos3Geo.cbox(g, "hd_plastic", 0.0F, 4.5F, 4.5F, 0.9F, 0.3F, 0.25F, 0.08F);
      temples(g, c);
   }

   static float[][] starPoly(float ro, float ri, int n) {
      float[][] p = new float[n * 2][];

      for (int i = 0; i < n * 2; i++) {
         double an = (Math.PI / 2) + Math.PI * i / n;
         float r = i % 2 == 0 ? ro : ri;
         p[i] = new float[]{Cos3Geo.cos(an) * r, Cos3Geo.sin(an) * r};
      }

      return p;
   }

   static float[][] heartPoly(float s) {
      int n = 24;
      float[][] p = new float[n][];

      for (int i = 0; i < n; i++) {
         double t = (Math.PI * 2) * i / n;
         float x = (float)(16.0 * Math.pow(Math.sin(t), 3.0)) / 16.0F;
         float y = (float)(13.0 * Math.cos(t) - 5.0 * Math.cos(2.0 * t) - 2.0 * Math.cos(3.0 * t) - Math.cos(4.0 * t)) / 16.0F;
         p[n - 1 - i] = new float[]{x * s, y * s};
      }

      return p;
   }

   static void monocle(G g, int c) {
      g.push();
      g.translate(2.0F, 4.3F, 4.45F);
      g.rotX(90.0F);
      g.color(c);
      g.torus("hd_gold", 1.5F, 0.2F, 20, 5, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(1156117759);
      g.ring("hd_glass", 0.0F, 1.45F, 0.0F, 20, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.color(c);
      Geo.chain(
         g,
         "hd_gold",
         new float[][]{{3.4F, 3.6F, 4.45F}, {3.9F, 2.0F, 4.2F}, {4.2F, 0.4F, 3.4F}, {4.3F, -0.6F, 2.0F}},
         new float[]{0.07F, 0.07F, 0.07F, 0.07F},
         4
      );
   }

   static void mustache(G g, int c, boolean curl) {
      g.color(c);

      for (int s = -1; s <= 1; s += 2) {
         float[][] pts = curl
            ? new float[][]{{0.0F, 2.4F, 4.4F}, {s * 1.2F, 2.3F, 4.45F}, {s * 2.2F, 2.0F, 4.4F}, {s * 2.9F, 2.4F, 4.35F}, {s * 2.8F, 2.9F, 4.35F}}
            : new float[][]{{0.0F, 2.4F, 4.4F}, {s * 1.2F, 2.25F, 4.45F}, {s * 2.3F, 1.8F, 4.4F}, {s * 2.8F, 1.4F, 4.3F}};
         float[] rad = curl ? new float[]{0.5F, 0.45F, 0.32F, 0.2F, 0.1F} : new float[]{0.55F, 0.5F, 0.35F, 0.12F};
         Geo.chain(g, "hd_mustache", pts, rad, 6);
      }
   }

   static void beard(G g, int c) {
      g.color(c);

      for (int i = 0; i < 9; i++) {
         float x = -3.6F + i * 0.9F;
         float len = 3.2F - Math.abs(x) * 0.35F;
         Geo.ellipsoid(g, "hd_fur", x, 1.2F - len * 0.35F, 4.2F - Math.abs(x) * 0.12F, 0.75F, len * 0.6F, 0.55F, 8, 5);
      }

      Geo.ellipsoid(g, "hd_fur", 0.0F, 2.3F, 4.45F, 2.2F, 0.45F, 0.4F, 8, 4);
   }

   static void eyePatch(G g, Cos.A a) {
      g.color(-15000798);
      g.push();
      g.translate(2.0F, 4.3F, 4.35F);
      Geo.ellipsoid(g, "hd_leather", 0.0F, 0.0F, 0.0F, 1.4F, 1.15F, 0.25F, 12, 4);
      g.pop();
      Geo.chain(
         g,
         "hd_leather",
         new float[][]{{-4.25F, 6.6F, 1.0F}, {-3.0F, 6.4F, 4.3F}, {0.0F, 5.5F, 4.5F}, {3.4F, 4.6F, 4.4F}, {4.25F, 3.6F, 1.0F}},
         new float[]{0.12F, 0.12F, 0.12F, 0.12F, 0.12F},
         4
      );
   }

   static void bandit(G g, int c) {
      g.color(c);
      g.cylinder("hd_satin", 4.35F, 4.35F, 3.4F, 5.3F, 24, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-16777216);

      for (int s = -1; s <= 1; s += 2) {
         Geo.ellipsoid(g, "hd_gloss", s * 1.9F, 4.35F, 4.36F, 0.9F, 0.55F, 0.06F, 10, 3);
      }

      g.color(c);

      for (int s = -1; s <= 1; s += 2) {
         Geo.chain(g, "hd_satin", new float[][]{{s * 0.6F, 4.3F, -4.4F}, {s * 1.2F, 3.4F, -5.4F}, {s * 1.6F, 2.0F, -5.8F}}, new float[]{0.35F, 0.3F, 0.15F}, 5);
      }
   }

   static void vr(G g, Cos.A a, int body, int glow) {
      g.color(body);
      Cos3Geo.bbox(g, "hd_plastic", -3.8F, 2.6F, 4.0F, 3.8F, 6.0F, 6.4F, 0.6F);
      g.color(-15000798);

      for (int s = -1; s <= 1; s += 2) {
         Cos3Geo.cbox(g, "hd_rubber", s * 4.3F, 4.3F, 0.6F, 0.3F, 1.2F, 8.0F, 0.1F);
      }

      g.color(glow).glow(true);
      float pulse = 0.6F + 0.4F * Cos3Geo.sin(a.time * 3.0F);
      g.color(Cos3Geo.alpha(pulse, glow & 16777215));
      Cos3Geo.cbox(g, "hd_softglow", 0.0F, 4.3F, 6.45F, 5.6F, 0.5F, 0.08F, 0.02F);
      g.glow(false);
   }

   static void skiGoggles(G g, int lens) {
      g.color(-14539732);
      g.cylinder("hd_rubber", 4.38F, 4.38F, 3.7F, 5.0F, 24, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-855310);
      Cos3Geo.bbox(g, "hd_plastic", -3.6F, 2.8F, 4.0F, 3.6F, 5.8F, 4.9F, 0.6F);
      g.color(lens).glow(true);
      Cos3Geo.bbox(g, "hd_lens", -3.2F, 3.1F, 4.85F, 3.2F, 5.5F, 5.05F, 0.5F);
      g.glow(false);
   }

   static void scuba(G g, Cos.A a) {
      g.color(-13728034);
      Cos3Geo.bbox(g, "hd_rubber", -3.4F, 2.6F, 4.0F, 3.4F, 6.0F, 5.0F, 0.8F);
      g.color(1723854847);
      Cos3Geo.bbox(g, "hd_glass", -2.9F, 3.0F, 4.95F, 2.9F, 5.6F, 5.1F, 0.6F);
      g.color(-14539732);
      g.cylinder("hd_rubber", 4.38F, 4.38F, 4.0F, 4.8F, 24, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-13053);
      Geo.chain(
         g,
         "hd_plastic",
         new float[][]{{3.6F, 1.6F, 4.4F}, {4.6F, 2.4F, 3.6F}, {4.8F, 6.0F, 3.0F}, {4.8F, 9.0F, 2.6F}},
         new float[]{0.3F, 0.3F, 0.3F, 0.32F},
         6
      );
   }

   static void ninjaMask(G g, int c) {
      g.color(c);
      g.cylinder("hd_ninja", 4.4F, 4.4F, -0.2F, 3.4F, 24, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.cylinder("hd_ninja", 4.4F, 4.4F, 5.4F, 8.2F, 24, false, 0.0F, 0.0F, 1.0F, 1.0F);

      for (int s = -1; s <= 1; s += 2) {
         Geo.chain(g, "hd_ninja", new float[][]{{s * 0.6F, 6.8F, -4.4F}, {s * 1.4F, 5.6F, -5.6F}, {s * 2.0F, 3.6F, -6.2F}}, new float[]{0.4F, 0.32F, 0.12F}, 5);
      }
   }

   static void whiskers(G g, Cos.A a) {
      g.color(-30016);

      for (int s = -1; s <= 1; s += 2) {
         Geo.ellipsoid(g, "hd_softglow", s * 2.6F, 2.6F, 4.05F, 0.9F, 0.55F, 0.05F, 10, 3);
      }

      g.color(-15000798);
      Cos3Geo.ball(g, "hd_gloss", 0.0F, 2.9F, 4.2F, 0.35F, 6);

      for (int s = -1; s <= 1; s += 2) {
         for (int i = -1; i <= 1; i++) {
            Geo.tube(g, "hd_plastic", new float[]{s * 1.0F, 2.6F + i * 0.25F, 4.15F}, new float[]{s * 4.6F, 2.6F + i * 0.7F, 4.4F}, 0.05F, 0.03F, 3);
         }
      }
   }

   static void glasses3d(G g, Cos.A a) {
      g.color(-723724);
      Cos3Geo.bbox(g, "hd_plastic", -4.0F, 3.0F, 4.2F, 4.0F, 5.6F, 4.55F, 0.15F);
      g.color(-570480086);
      Cos3Geo.bbox(g, "hd_glass", -3.5F, 3.3F, 4.5F, -0.6F, 5.3F, 4.62F, 0.1F);
      g.color(-584394497);
      Cos3Geo.bbox(g, "hd_glass", 0.6F, 3.3F, 4.5F, 3.5F, 5.3F, 4.62F, 0.1F);
      temples(g, -723724);
   }

   static void shutterShades(G g, int c) {
      g.color(c);
      Cos3Geo.cbox(g, "hd_plastic", 0.0F, 5.4F, 4.45F, 8.0F, 0.4F, 0.4F, 0.1F);
      Cos3Geo.cbox(g, "hd_plastic", 0.0F, 3.0F, 4.45F, 8.0F, 0.4F, 0.4F, 0.1F);

      for (int i = 0; i < 5; i++) {
         float y = 3.4F + i * 0.45F;

         for (int s = -1; s <= 1; s += 2) {
            Cos3Geo.cbox(g, "hd_plastic", s * 2.0F, y, 4.45F, 3.4F, 0.16F, 0.3F, 0.04F);
         }
      }

      temples(g, c);
   }

   static void laserEyes(G g, Cos.A a, int c) {
      g.glow(true);
      float flick = 0.75F + 0.25F * Cos3Geo.sin(a.time * 20.0F);

      for (int s = -1; s <= 1; s += 2) {
         g.color(c);
         Cos3Geo.ball(g, "hd_softglow", s * 2.0F, 4.2F, 4.15F, 0.65F, 8);
         g.color(Cos3Geo.alpha(0.55 * flick, c & 16777215));
         Geo.tube(g, "hd_softglow", new float[]{s * 2.0F, 4.2F, 4.3F}, new float[]{s * 2.0F, 4.0F, 22.0F}, 0.35F, 0.2F, 6);
         g.color(Cos3Geo.alpha(0.9, 16777215));
         Geo.tube(g, "hd_softglow", new float[]{s * 2.0F, 4.2F, 4.3F}, new float[]{s * 2.0F, 4.0F, 22.0F}, 0.12F, 0.08F, 5);
      }

      g.glow(false);
   }

   static void headset(G g, Cos.A a, int body, int light) {
      g.color(body);
      float[][] arc = new float[11][];
      float[] ar = new float[11];

      for (int i = 0; i <= 10; i++) {
         double an = Math.PI * i / 10.0;
         arc[i] = new float[]{-Cos3Geo.cos(an) * 4.75F, 4.4F + Cos3Geo.sin(an) * 4.6F, 0.0F};
         ar[i] = 0.38F;
      }

      Geo.chain(g, "hd_plastic", arc, ar, 6);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 4.6F, 4.4F, 0.0F);
         g.rotZ(90.0F);
         g.color(body);
         g.cylinder("hd_plastic", 1.9F, 1.9F, -0.5F, 0.5F, 18, true, 0.0F, 0.0F, 1.0F, 1.0F);
         g.color(light).glow(true);
         g.torus("hd_softglow", 1.9F, 0.12F, 18, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         g.glow(false);
         g.pop();
      }

      g.color(body);
      Geo.chain(
         g,
         "hd_plastic",
         new float[][]{{-4.7F, 3.6F, 0.6F}, {-4.6F, 2.0F, 2.6F}, {-3.4F, 1.6F, 4.0F}, {-1.4F, 1.8F, 4.6F}},
         new float[]{0.15F, 0.14F, 0.14F, 0.14F},
         5
      );
      g.color(-14539732);
      Cos3Geo.ball(g, "hd_rubber", -1.2F, 1.8F, 4.65F, 0.45F, 6);
   }

   static void rose(G g, Cos.A a) {
      g.color(-13730510);
      Geo.tube(g, "hd_vine", new float[]{-0.6F, 1.9F, 4.4F}, new float[]{-4.6F, 2.3F, 5.6F}, 0.12F, 0.1F, 5);
      g.color(-11555246);
      Geo.ellipsoid(g, "hd_leaf", -2.6F, 2.2F, 5.1F, 0.6F, 0.08F, 0.3F, 5, 3);
      g.color(-2677187);

      for (int i = 0; i < 6; i++) {
         g.push();
         g.translate(1.0F, 2.0F, 4.6F);
         g.rotY(i * 60);
         g.rotX(-30 - i % 2 * 20);
         Geo.ellipsoid(g, "hd_petal", 0.0F, 0.0F, 0.45F, 0.45F, 0.65F, 0.4F, 6, 4);
         g.pop();
      }
   }

   static void lolly(G g, Cos.A a) {
      g.color(-723724);
      Geo.tube(g, "hd_plastic", new float[]{0.4F, 2.0F, 4.3F}, new float[]{2.6F, 1.6F, 7.0F}, 0.1F, 0.1F, 5);
      g.push();
      g.translate(2.9F, 1.55F, 7.4F);
      g.rotY(a.time * 40.0F);
      g.rotX(90.0F);
      int n = 6;

      for (int i = 0; i < n; i++) {
         g.color(i % 2 == 0 ? -45149 : -1);
         g.torus("hd_gloss", 0.3F + i * 0.22F, 0.13F, 14, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      }

      g.pop();
   }

   private static void backs() {
      int[] packs = new int[]{-1900533, -16749385, -14176672, -6599222};
      String[] packN = new String[]{"Schulrucksack Rot", "Schulrucksack Blau", "Wanderrucksack Grün", "Rucksack Lila"};

      for (int i = 0; i < 4; i++) {
         int c = packs[i];
         back("pack" + i, packN[i], C, (g, a) -> backpack(g, a, c));
      }

      back("guitar_ac", "Akustikgitarre", E, (g, a) -> guitar(g, a, -3635398, false));
      back("guitar_el", "Rote E-Gitarre", E, (g, a) -> guitar(g, a, -1900533, true));
      back("shield_wood", "Holzschild", R, (g, a) -> roundShield(g, -7710160, -6642771, 0));
      back("shield_iron", "Eisenschild", R, (g, a) -> roundShield(g, -4669238, -10853776, 1));
      back("shield_gold", "Goldschild", E, (g, a) -> roundShield(g, -1525460, -5238245, 2));
      back("quiver", "Leder-Köcher", R, CosPack::quiver);
      back("jetpack", "Jetpack Silber", L, (g, a) -> jetpack(g, a, -4669238));
      back("jetpack_red", "Raketen-Jetpack", L, (g, a) -> jetpack(g, a, -1900533));
      back("turtle", "Schildkrötenpanzer", E, CosPack::turtle);
      back("sword_iron", "Eisenschwert", C, (g, a) -> sword(g, -2564378, -9811414));
      back("sword_gold", "Goldschwert", R, (g, a) -> sword(g, -11713, -12966892));
      back("sword_dia", "Diamantschwert", E, (g, a) -> sword(g, -10817304, -12966892));
      back("axe", "Holzfälleraxt", R, (g, a) -> axe(g, -4669238, false));
      back("axe_double", "Doppelaxt", E, (g, a) -> axe(g, -2564378, true));
      back("surf_blue", "Surfbrett Blau", R, (g, a) -> board(g, -13728034, -1, 1.0F));
      back("surf_sunset", "Surfbrett Sunset", R, (g, a) -> board(g, -30208, -45149, 1.0F));
      back("skate", "Skateboard Street", R, (g, a) -> board(g, -14539732, -13053, 0.62F));
      back("balloons", "Rote Luftballons", E, (g, a) -> balloons(g, a, false));
      back("balloons_mix", "Bunte Luftballons", E, (g, a) -> balloons(g, a, true));
      back("oxygen", "Sauerstoffflasche", R, CosPack::oxygen);
      back("gift", "Geschenk-Rucksack", R, CosPack::giftPack);
      back("teddy", "Teddy-Rucksack", E, CosPack::teddy);
      back("kite", "Drachen", R, CosPack::kiteBack);
      back("trident", "Dreizack", E, CosPack::trident);
      back("bow", "Langbogen", R, CosPack::longbow);
      back("flag_red", "Fahne Rot", C, (g, a) -> flag(g, a, -1900533, -1, false));
      back("flag_blue", "Fahne Blau", C, (g, a) -> flag(g, a, -16749385, -13053, false));
      back("flag_pirate", "Piratenflagge", R, (g, a) -> flag(g, a, -15000798, -723724, true));
      int[] bricks = new int[]{-1900533, -16749385, -16743125};
      String[] brickN = new String[]{"2x2-Stein Rot", "2x2-Stein Blau", "2x2-Stein Grün"};

      for (int i = 0; i < 3; i++) {
         int c = bricks[i];
         back("brick" + i, brickN[i], R, (g, a) -> brickBack(g, c));
      }

      back("snowboard", "Snowboard", R, (g, a) -> board(g, -12987565, -14539732, 1.1F));
      back("paddle", "Paddel", C, CosPack::paddle);
      back("windmill", "Windrad", E, CosPack::windmill);
      back("clock", "Rücken-Uhr", E, CosPack::clockBack);
      back("rod", "Angelrute", C, CosPack::rodBack);
   }

   static void backpack(G g, Cos.A a, int c) {
      float bob = a.preview ? 0.0F : a.move * 0.15F * Math.abs(Cos3Geo.sin(a.time * 8.0F));
      g.push();
      g.translate(0.0F, -1.6F + bob, -2.1F);
      g.color(c);
      Cos3Geo.bbox(g, "hd_cotton", -3.4F, -9.0F, -3.4F, 3.4F, 0.0F, 0.0F, 0.9F);
      g.color(dark(c, 0.18F));
      Cos3Geo.bbox(g, "hd_cotton", -2.6F, -8.2F, -4.4F, 2.6F, -4.6F, -3.2F, 0.6F);
      g.color(-2564378);
      Cos3Geo.cbox(g, "hd_steel", 0.0F, -4.8F, -4.45F, 1.4F, 0.3F, 0.2F, 0.05F);
      g.color(dark(c, 0.4F));

      for (int s = -1; s <= 1; s += 2) {
         Cos3Geo.cbox(g, "hd_leather", s * 2.2F, -4.0F, 0.15F, 0.9F, 8.0F, 0.3F, 0.1F);
      }

      g.pop();
   }

   static void guitar(G g, Cos.A a, int body, boolean electric) {
      g.push();
      g.translate(0.0F, -6.0F, -2.6F);
      g.rotZ(32.0F);
      g.color(body);
      if (electric) {
         Cos3Geo.extrude(
            g,
            "hd_plastic",
            new float[][]{{-3.2F, -5.0F}, {3.0F, -5.2F}, {3.6F, -2.0F}, {2.0F, 0.0F}, {3.2F, 2.6F}, {1.2F, 3.0F}, {-1.4F, 1.0F}, {-3.4F, 2.6F}, {-3.8F, -1.6F}},
            -0.7F,
            0.0F
         );
      } else {
         g.push();
         g.rotX(90.0F);
         Geo.ellipsoid(g, "guitar_body", 0.0F, 0.35F, -2.2F, 3.6F, 0.75F, 3.2F, 16, 6);
         Geo.ellipsoid(g, "guitar_body", 0.0F, 0.35F, 1.6F, 2.8F, 0.75F, 2.5F, 16, 6);
         g.pop();
      }

      g.color(-12966892);
      Cos3Geo.cbox(g, "hd_wood", 0.0F, 7.5F, -0.4F, 1.1F, 11.0F, 0.5F, 0.1F);
      Cos3Geo.cbox(g, "hd_wood", 0.0F, 13.6F, -0.4F, 1.6F, 2.2F, 0.5F, 0.1F);
      g.color(-2039584);

      for (int i = -1; i <= 1; i++) {
         Geo.tube(g, "hd_steel", new float[]{i * 0.3F, -3.2F, -0.75F}, new float[]{i * 0.3F, 13.0F, -0.7F}, 0.04F, 0.04F, 3);
      }

      g.pop();
      g.color(-12966892);
      Geo.chain(g, "hd_leather", new float[][]{{-3.6F, -0.2F, 0.4F}, {-3.0F, 0.4F, -2.2F}, {2.6F, -10.0F, -2.6F}}, new float[]{0.2F, 0.2F, 0.2F}, 4);
   }

   static void roundShield(G g, int face, int rim, int emblem) {
      g.push();
      g.translate(0.0F, -6.0F, -2.4F);
      g.rotX(90.0F);
      g.color(face);
      Cos3Geo.lathe(g, emblem == 0 ? "hd_wood" : "hd_steel", new float[]{0.0F, 5.4F, 5.6F, 0.0F}, new float[]{0.0F, 0.0F, -0.9F, -1.6F}, 24);
      g.color(rim);
      g.push();
      g.translate(0.0F, -0.4F, 0.0F);
      g.torus("hd_steel", 5.5F, 0.35F, 24, 5, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.color(rim);
      Cos3Geo.ball(g, "hd_steel", 0.0F, -1.55F, 0.0F, 1.2F, 10);
      if (emblem == 2) {
         g.color(-5238245);
         g.push();
         g.translate(0.0F, -1.0F, 0.0F);
         g.rotX(-90.0F);
         Cos3Geo.extrude(g, "hd_satin", starPoly(3.4F, 1.4F, 5), 0.2F, 0.45F);
         g.pop();
      }

      g.pop();
   }

   static void quiver(G g, Cos.A a) {
      g.push();
      g.translate(1.0F, -5.4F, -3.0F);
      g.rotZ(-22.0F);
      g.color(-7710160);
      g.cylinder("hd_leather", 1.5F, 1.3F, -5.0F, 4.0F, 14, true, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1525460);
      g.torus("hd_gold", 1.5F, 0.15F, 14, 4, 0.0F, 0.0F, 1.0F, 1.0F);

      for (int i = 0; i < 5; i++) {
         double an = i * 1.3;
         float x = Cos3Geo.cos(an) * 0.7F;
         float z = Cos3Geo.sin(an) * 0.7F;
         g.color(-9811414);
         Geo.tube(g, "hd_wood", new float[]{x, 3.0F, z}, new float[]{x, 7.4F + i % 2 * 0.6F, z}, 0.1F, 0.1F, 4);
         g.color(i % 2 == 0 ? -723724 : -1900533);
         Cos3Geo.cbox(g, "hd_fluff", x, 6.8F + i % 2 * 0.6F, z, 0.08F, 1.4F, 0.7F, 0.02F);
      }

      g.pop();
   }

   static void jetpack(G g, Cos.A a, int c) {
      g.push();
      g.translate(0.0F, -2.0F, -2.2F);
      g.color(c);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 1.7F, 0.0F, -1.6F);
         g.cylinder("hd_steel", 1.4F, 1.4F, -8.0F, -1.0F, 16, true, 0.0F, 0.0F, 1.0F, 1.0F);
         Cos3Geo.lathe(g, "hd_steel", new float[]{1.4F, 1.0F, 0.0F}, new float[]{-1.0F, -0.2F, 0.2F}, 16);
         g.color(-12960184);
         Cos3Geo.lathe(g, "hd_steel", new float[]{0.7F, 1.0F, 1.0F}, new float[]{-8.0F, -9.0F, -9.2F}, 12);
         g.glow(true);
         float fl = 0.8F + 0.2F * Cos3Geo.sin(a.time * 30.0F + s);
         g.color(-20432);
         Cos3Geo.lathe(g, "c3_flamegrad", new float[]{0.9F, 0.7F, 0.3F, 0.0F}, new float[]{-9.2F, -10.4F * fl, -11.6F * fl, -12.8F * fl}, 10);
         g.glow(false);
         g.color(c);
         g.pop();
      }

      g.color(dark(c, 0.35F));
      Cos3Geo.bbox(g, "hd_steel", -1.0F, -6.0F, -1.6F, 1.0F, -2.0F, 0.0F, 0.3F);
      g.pop();
   }

   static void turtle(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, -6.4F, -2.2F);
      g.rotX(-90.0F);
      g.scale(1.0F, 1.0F, 1.35F);
      g.color(-12682706);
      Cos3Geo.lathe(g, "hd_leather", new float[]{0.0F, 4.4F, 4.6F, 4.3F, 3.2F, 1.6F, 0.0F}, new float[]{0.0F, 0.0F, 0.6F, 1.6F, 2.8F, 3.4F, 3.5F}, 6);
      g.color(-9787313);

      for (int i = 0; i < 6; i++) {
         double an = (Math.PI * 2) * i / 6.0 + (Math.PI / 6);
         g.push();
         g.translate(Cos3Geo.cos(an) * 2.4F, 2.6F, Cos3Geo.sin(an) * 2.4F);
         Cos3Geo.lathe(g, "hd_leather", new float[]{1.1F, 0.9F, 0.0F}, new float[]{0.0F, 0.4F, 0.55F}, 6);
         g.pop();
      }

      g.push();
      g.translate(0.0F, 3.2F, 0.0F);
      Cos3Geo.lathe(g, "hd_leather", new float[]{1.3F, 1.0F, 0.0F}, new float[]{0.0F, 0.35F, 0.45F}, 6);
      g.pop();
      g.pop();
   }

   static void sword(G g, int blade, int grip) {
      g.push();
      g.translate(0.0F, -5.0F, -2.6F);
      g.rotZ(-35.0F);
      g.color(blade);
      Cos3Geo.extrude(g, "hd_steel", new float[][]{{-0.75F, 0.0F}, {0.75F, 0.0F}, {0.75F, 11.5F}, {0.0F, 13.2F}, {-0.75F, 11.5F}}, -0.22F, 0.22F);
      g.color(-10853776);
      Cos3Geo.cbox(g, "hd_steel", 0.0F, -0.3F, 0.0F, 4.0F, 0.6F, 0.8F, 0.2F);
      g.color(grip);
      g.cylinder("hd_leather", 0.4F, 0.4F, -3.6F, -0.6F, 8, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-10853776);
      Cos3Geo.ball(g, "hd_steel", 0.0F, -3.9F, 0.0F, 0.6F, 8);
      g.pop();
   }

   static void axe(G g, int head, boolean dbl) {
      g.push();
      g.translate(0.0F, -5.4F, -2.6F);
      g.rotZ(30.0F);
      g.color(-9811414);
      g.cylinder("hd_wood", 0.4F, 0.4F, -6.0F, 7.0F, 8, true, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(head);

      for (int s = dbl ? -1 : 1; s <= 1; s += 2) {
         g.push();
         g.scale(s, 1.0F, 1.0F);
         Cos3Geo.extrude(g, "hd_steel", new float[][]{{0.3F, 3.6F}, {2.4F, 2.8F}, {3.6F, 4.0F}, {3.8F, 6.6F}, {2.4F, 7.6F}, {0.3F, 6.6F}}, -0.25F, 0.25F);
         g.pop();
      }

      g.pop();
   }

   static void board(G g, int c1, int c2, float len) {
      g.push();
      g.translate(0.0F, -6.0F, -2.6F);
      g.rotZ(12.0F);
      float h = 12.0F * len;
      g.color(c1);
      float[][] poly = new float[20][];

      for (int i = 0; i < 20; i++) {
         double an = (Math.PI * 2) * i / 20.0;
         poly[i] = new float[]{Cos3Geo.cos(an) * 2.8F, Cos3Geo.sin(an) * h};
      }

      Cos3Geo.extrude(g, "hd_plastic", poly, -0.5F, 0.0F);
      g.color(c2);
      Cos3Geo.cbox(g, "hd_plastic", 0.0F, 0.0F, -0.52F, 0.6F, h * 1.7F, 0.06F, 0.02F);
      if (len < 0.8F) {
         g.color(-6642771);

         for (int s = -1; s <= 1; s += 2) {
            Cos3Geo.cbox(g, "hd_steel", 0.0F, s * h * 0.6F, -0.9F, 3.2F, 0.6F, 0.6F, 0.1F);
            g.color(-723724);

            for (int k = -1; k <= 1; k += 2) {
               g.push();
               g.translate(k * 1.7F, s * h * 0.6F, -1.2F);
               g.rotZ(90.0F);
               g.cylinder("hd_rubber", 0.5F, 0.5F, -0.3F, 0.3F, 10, true, 0.0F, 0.0F, 1.0F, 1.0F);
               g.pop();
            }

            g.color(-6642771);
         }
      }

      g.pop();
   }

   static void balloons(G g, Cos.A a, boolean mix) {
      int[] cols = mix ? new int[]{-1900533, -13053, -13728034, -14176672, -45149} : new int[]{-1900533, -3141600, -54726, -1900533, -5238758};

      for (int i = 0; i < 5; i++) {
         double an = i * 1.25 - 2.5;
         float sw = Cos3Geo.sin(a.time * 1.5 + i) * 0.6F;
         float x = Cos3Geo.sin(an) * 4.5F + sw;
         float y = 12.0F + i % 2 * 2.4F + Cos3Geo.cos(a.time + i) * 0.3F;
         float z = -6.5F - Cos3Geo.cos(an) * 1.2F;
         g.color(-723724);
         Geo.tube(g, "hd_cotton", new float[]{0.0F, -4.0F, -2.4F}, new float[]{x, y - 2.4F, z}, 0.04F, 0.04F, 3);
         g.color(cols[i]);
         Geo.ellipsoid(g, "hd_gloss", x, y, z, 2.0F, 2.4F, 2.0F, 12, 8);
      }
   }

   static void oxygen(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, -1.6F, -4.0F);
      g.color(-13053);
      g.cylinder("hd_steel", 1.8F, 1.8F, -10.0F, -1.2F, 18, false, 0.0F, 0.0F, 1.0F, 1.0F);
      Cos3Geo.lathe(g, "hd_steel", new float[]{1.8F, 1.4F, 0.5F, 0.0F}, new float[]{-1.2F, -0.2F, 0.3F, 0.35F}, 18);
      Cos3Geo.lathe(g, "hd_steel", new float[]{1.8F, 1.2F, 0.0F}, new float[]{-10.0F, -10.8F, -11.0F}, 18);
      g.color(-10853776);
      g.cylinder("hd_steel", 0.4F, 0.4F, 0.2F, 1.2F, 8, true, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-14539732);
      Geo.chain(
         g,
         "hd_rubber",
         new float[][]{{0.0F, 1.2F, 0.0F}, {2.4F, 1.8F, 1.0F}, {3.8F, 0.8F, 3.0F}, {4.2F, -1.6F, 4.2F}},
         new float[]{0.25F, 0.25F, 0.25F, 0.25F},
         6
      );
      g.pop();
   }

   static void giftPack(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, -5.0F, -4.4F);
      g.color(-13728034);
      Cos3Geo.cbox(g, "hd_party", 0.0F, 0.0F, 0.0F, 7.0F, 8.0F, 4.4F, 0.3F);
      g.color(-1900533);
      Cos3Geo.cbox(g, "hd_satin", 0.0F, 0.0F, 0.0F, 1.2F, 8.1F, 4.5F, 0.05F);
      Cos3Geo.cbox(g, "hd_satin", 0.0F, 0.0F, 0.0F, 7.1F, 1.2F, 4.5F, 0.05F);
      g.translate(0.0F, 4.2F, 0.0F);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.rotZ(s * 50);
         Cos3Geo.lathe(g, "hd_satin", new float[]{0.3F, 1.2F, 1.0F, 0.0F}, new float[]{0.0F, 0.6F, 2.0F, 2.4F}, 10);
         g.pop();
      }

      g.pop();
   }

   static void teddy(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, -5.4F, -4.4F);
      g.color(-6264774);
      Geo.ellipsoid(g, "hd_fur", 0.0F, -1.6F, 0.0F, 3.0F, 3.4F, 2.4F, 14, 8);
      Geo.ellipsoid(g, "hd_fur", 0.0F, 3.0F, -0.2F, 2.6F, 2.4F, 2.2F, 14, 8);

      for (int s = -1; s <= 1; s += 2) {
         Cos3Geo.ball(g, "hd_fur", s * 2.0F, 5.0F, -0.2F, 1.0F, 8);
         Geo.ellipsoid(g, "hd_fur", s * 3.0F, -0.6F, -0.6F, 1.0F, 1.8F, 1.0F, 8, 6);
         Geo.ellipsoid(g, "hd_fur", s * 1.6F, -4.8F, -0.8F, 1.1F, 1.0F, 1.4F, 8, 6);
      }

      g.color(-2050934);
      Geo.ellipsoid(g, "hd_fur", 0.0F, 2.4F, -2.2F, 1.2F, 0.9F, 0.6F, 10, 5);
      g.color(-15000798);
      Cos3Geo.ball(g, "hd_gloss", 0.0F, 2.8F, -2.75F, 0.35F, 6);

      for (int s = -1; s <= 1; s += 2) {
         Cos3Geo.ball(g, "hd_gloss", s * 0.9F, 3.9F, -2.1F, 0.3F, 6);
      }

      g.pop();
   }

   static void kiteBack(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, -2.0F, -3.0F);
      g.rotZ(8.0F + Cos3Geo.sin(a.time * 1.3F) * 4.0F);
      float[][] d = new float[][]{{0.0F, 7.0F}, {-4.4F, 0.0F}, {0.0F, -8.0F}, {4.4F, 0.0F}};

      for (int i = 0; i < 4; i++) {
         g.color(i % 2 == 0 ? -1900533 : -13053);
         float[] p0 = d[i];
         float[] p1 = d[(i + 1) % 4];
         Cos3Geo.extrude(g, "hd_satin", new float[][]{{0.0F, 0.0F}, p0, p1}, -0.1F, 0.1F);
      }

      g.color(-12966892);
      Geo.tube(g, "hd_wood", new float[]{0.0F, 7.0F, 0.15F}, new float[]{0.0F, -8.0F, 0.15F}, 0.12F, 0.12F, 4);
      Geo.tube(g, "hd_wood", new float[]{-4.4F, 0.0F, 0.15F}, new float[]{4.4F, 0.0F, 0.15F}, 0.12F, 0.12F, 4);
      g.color(-13728034);

      for (int i = 0; i < 4; i++) {
         float y = -9.5F - i * 1.6F;
         float x = Cos3Geo.sin(a.time * 3.0F + i) * 0.8F;
         g.push();
         g.translate(x, y, 0.0F);
         g.rotZ(45.0F);
         Cos3Geo.cbox(g, "hd_satin", 0.0F, 0.0F, 0.0F, 0.9F, 0.9F, 0.08F, 0.02F);
         g.pop();
      }

      g.pop();
   }

   static void trident(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, -5.0F, -2.8F);
      g.rotZ(-28.0F);
      g.color(-13984369);
      g.cylinder("hd_steel", 0.35F, 0.35F, -9.0F, 7.0F, 8, true, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-10817304).glow(true);
      Cos3Geo.cbox(g, "hd_steel", 0.0F, 7.2F, 0.0F, 4.4F, 0.6F, 0.7F, 0.2F);

      for (int i = -1; i <= 1; i++) {
         g.push();
         g.translate(i * 1.9F, 7.4F, 0.0F);
         Cos3Geo.lathe(g, "hd_steel", new float[]{0.35F, 0.35F, 0.0F}, new float[]{0.0F, 3.2F - Math.abs(i) * 0.6F, 4.6F - Math.abs(i) * 0.6F}, 6);
         g.pop();
      }

      g.glow(false);
      g.pop();
   }

   static void longbow(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, -6.0F, -2.6F);
      g.rotZ(20.0F);
      g.color(-7710160);
      float[][] pts = new float[9][];
      float[] rad = new float[9];

      for (int i = 0; i < 9; i++) {
         float f = (i - 4) / 4.0F;
         pts[i] = new float[]{0.0F, f * 10.0F, -(1.0F - f * f) * 2.6F};
         rad[i] = 0.35F - Math.abs(f) * 0.18F;
      }

      Geo.chain(g, "hd_wood", pts, rad, 6);
      g.color(-723724);
      Geo.tube(g, "hd_cotton", new float[]{0.0F, 10.0F, 0.0F}, new float[]{0.0F, -10.0F, 0.0F}, 0.04F, 0.04F, 3);
      g.pop();
   }

   static void flag(G g, Cos.A a, int c1, int c2, boolean skull) {
      g.push();
      g.translate(2.2F, -8.0F, -2.6F);
      g.color(-9811414);
      g.cylinder("hd_wood", 0.22F, 0.22F, 0.0F, 22.0F, 8, true, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1525460);
      Cos3Geo.ball(g, "hd_gold", 0.0F, 22.2F, 0.0F, 0.45F, 8);
      int seg = 8;

      for (int i = 0; i < seg; i++) {
         float f0 = (float)i / seg;
         float f1 = (i + 1.0F) / seg;
         float z0 = Cos3Geo.sin(a.time * 4.0F - f0 * 4.0F) * 0.8F * f0;
         float z1 = Cos3Geo.sin(a.time * 4.0F - f1 * 4.0F) * 0.8F * f1;
         float x0 = f0 * 9.0F;
         float x1 = f1 * 9.0F;
         g.color(skull ? c1 : (f0 < 0.5F ? c1 : c2));
         g.quad(
            "white",
            new float[]{x0, 21.6F, z0},
            new float[]{x1, 21.6F, z1},
            new float[]{x1, 15.6F, z1},
            new float[]{x0, 15.6F, z0},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
         g.quad(
            "white",
            new float[]{x1, 21.6F, z1},
            new float[]{x0, 21.6F, z0},
            new float[]{x0, 15.6F, z0},
            new float[]{x1, 15.6F, z1},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
      }

      if (skull) {
         float z = Cos3Geo.sin(a.time * 4.0F - 2.2F) * 0.8F * 0.5F;
         g.color(c2);
         Cos3Geo.ball(g, "hd_bone", 4.5F, 19.4F, z + 0.2F, 1.1F, 8);
         Cos3Geo.ball(g, "hd_bone", 4.5F, 19.4F, z - 0.2F, 1.1F, 8);
      }

      g.pop();
   }

   static void brickBack(G g, int c) {
      g.push();
      g.translate(0.0F, -1.4F, -2.2F);
      g.color(c);
      Cos3Geo.bbox(g, "hd_brick", -3.4F, -8.0F, -4.0F, 3.4F, 0.0F, 0.0F, 0.3F);

      for (int ix = 0; ix < 2; ix++) {
         for (int iy = 0; iy < 2; iy++) {
            g.push();
            g.translate(-1.7F + ix * 3.4F, -2.0F - iy * 4.0F, -4.0F);
            g.rotX(-90.0F);
            Cos3Geo.lathe(g, "hd_brick", new float[]{1.2F, 1.2F, 1.1F, 0.0F}, new float[]{-0.02F, 0.9F, 1.0F, 1.02F}, 14);
            g.pop();
         }
      }

      g.pop();
   }

   static void paddle(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, -6.0F, -2.6F);
      g.rotZ(-25.0F);
      g.color(-3635398);
      g.cylinder("hd_wood", 0.35F, 0.35F, -9.0F, 6.0F, 8, true, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1900533);
      g.push();
      g.translate(0.0F, 9.0F, 0.0F);
      Geo.ellipsoid(g, "hd_plastic", 0.0F, 0.0F, 0.0F, 1.8F, 3.4F, 0.3F, 12, 6);
      g.pop();
      g.pop();
   }

   static void windmill(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, -2.0F, -2.6F);
      g.color(-723724);
      g.cylinder("hd_plastic", 0.3F, 0.3F, -6.0F, 4.0F, 8, true, 0.0F, 0.0F, 1.0F, 1.0F);
      g.translate(0.0F, 4.0F, -0.6F);
      g.rotZ(a.time * 160.0F);

      for (int i = 0; i < 3; i++) {
         g.push();
         g.rotZ(i * 120);
         g.color(i == 0 ? -1900533 : -723724);
         Cos3Geo.extrude(g, "hd_plastic", new float[][]{{-0.2F, 0.0F}, {0.6F, 0.4F}, {0.8F, 6.0F}, {0.0F, 6.4F}, {-0.3F, 0.6F}}, -0.08F, 0.08F);
         g.pop();
      }

      g.color(-6642771);
      Cos3Geo.ball(g, "hd_steel", 0.0F, 0.0F, 0.0F, 0.6F, 8);
      g.pop();
   }

   static void clockBack(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, -5.0F, -2.4F);
      g.rotX(90.0F);
      g.color(-1525460);
      Cos3Geo.lathe(g, "hd_gold", new float[]{0.0F, 4.6F, 4.8F, 0.0F}, new float[]{0.0F, 0.0F, -1.0F, -1.2F}, 24);
      g.color(-461592);
      g.ring("hd_cotton", 0.0F, 4.2F, -1.21F, 24, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-15000798);

      for (int i = 0; i < 12; i++) {
         double an = (Math.PI * 2) * i / 12.0;
         g.push();
         g.translate(Cos3Geo.cos(an) * 3.6F, -1.25F, Cos3Geo.sin(an) * 3.6F);
         g.rotY((float)(-Math.toDegrees(an)));
         Cos3Geo.cbox(g, "hd_plastic", 0.0F, 0.0F, 0.0F, i % 3 == 0 ? 0.8F : 0.45F, 0.06F, 0.15F, 0.02F);
         g.pop();
      }

      float sec = (float)(System.currentTimeMillis() / 1000.0 % 60.0);
      float min = (float)(System.currentTimeMillis() / 60000.0 % 60.0);
      g.push();
      g.translate(0.0F, -1.3F, 0.0F);
      g.rotY(-min * 6.0F);
      Cos3Geo.cbox(g, "hd_plastic", 0.0F, 0.0F, 1.5F, 0.3F, 0.06F, 3.0F, 0.02F);
      g.pop();
      g.color(-1900533);
      g.push();
      g.translate(0.0F, -1.32F, 0.0F);
      g.rotY(-sec * 6.0F);
      Cos3Geo.cbox(g, "hd_plastic", 0.0F, 0.0F, 1.8F, 0.12F, 0.06F, 3.6F, 0.02F);
      g.pop();
      g.pop();
   }

   static void rodBack(G g, Cos.A a) {
      g.push();
      g.translate(1.0F, -6.0F, -2.6F);
      g.rotZ(-18.0F);
      g.color(-9811414);
      Geo.chain(
         g,
         "hd_wood",
         new float[][]{{0.0F, -6.0F, 0.0F}, {0.0F, 4.0F, 0.0F}, {0.6F, 12.0F, 0.0F}, {1.6F, 17.0F, 0.0F}},
         new float[]{0.35F, 0.25F, 0.16F, 0.08F},
         6
      );
      g.color(-4669238);
      g.push();
      g.translate(0.4F, -3.0F, 0.0F);
      g.rotZ(90.0F);
      g.cylinder("hd_steel", 0.9F, 0.9F, -0.4F, 0.4F, 12, true, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.color(-723724);
      float sw = Cos3Geo.sin(a.time * 1.4F) * 0.8F;
      Geo.tube(g, "hd_cotton", new float[]{1.6F, 17.0F, 0.0F}, new float[]{2.4F + sw, 9.0F, 0.0F}, 0.03F, 0.03F, 3);
      g.color(-1900533);
      Cos3Geo.ball(g, "hd_gloss", 2.4F + sw, 8.6F, 0.0F, 0.45F, 8);
      g.pop();
   }

   private static void auras() {
      Object[][] list = new Object[][]{
         {"heart_orbit", "Herz-Orbit", R, 0, "heart", -45190},
         {"heart_rise", "Aufsteigende Herzen", R, 3, "heart", -30016},
         {"heart_helix", "Herz-Helix", E, 6, "heart", -54694},
         {"heart_burst", "Herzexplosion", E, 7, "heart", -45149},
         {"heart_crown", "Herzkrone", R, 5, "heart", -36966},
         {"heart_rain", "Herzregen", R, 2, "heart", -20280},
         {"snow_orbit", "Schneeflocken-Orbit", R, 0, "snowflake", -3148033},
         {"snow_rain", "Schneeflocken-Regen", R, 2, "snowflake", -1},
         {"snow_spiral", "Frost-Spirale", E, 1, "snowflake", -7673601},
         {"snow_crown", "Eiskranz", E, 5, "snowflake", -4198401},
         {"snow_helix", "Blizzard-Helix", E, 6, "snowflake", -2032897},
         {"snow_rings", "Frostringe", R, 4, "snowflake", -6301441},
         {"flame_spiral", "Flammen-Spirale", E, 1, "flame", -1},
         {"flame_crown", "Feuerkranz", L, 5, "flame", -1},
         {"flame_helix", "Feuer-Helix", L, 6, "flame", -1},
         {"flame_burst", "Flammenstoß", E, 7, "flame", -1},
         {"flame_rings", "Glutringe", E, 4, "flame", -16256},
         {"flame_orbit", "Feuerbälle", E, 0, "flame", -1},
         {"bolt_orbit", "Blitz-Orbit", E, 0, "bolt", -4022},
         {"bolt_rain", "Blitzregen", L, 2, "bolt", -6301441},
         {"bolt_burst", "Elektroschock", L, 7, "bolt", -1},
         {"bolt_crown", "Donnerkrone", L, 5, "bolt", -8118},
         {"bolt_helix", "Spannungsfeld", E, 6, "bolt", -5181185},
         {"spark_gold", "Goldfunken", R, 3, "spark", -11713},
         {"spark_pink", "Pinke Funken", R, 1, "spark", -45149},
         {"spark_blue", "Blaue Funken", R, 6, "spark", -11549697},
         {"spark_green", "Grüne Funken", R, 2, "spark", -12987565},
         {"spark_burst", "Feuerwerk", L, 7, "spark", -1},
         {"spark_rings", "Funkenringe", E, 4, "spark", -7558},
         {"orb_purple_orbit", "Lila Seelen", E, 0, "orb_purple", -1},
         {"orb_purple_helix", "Mystische Helix", E, 6, "orb_purple", -1},
         {"orb_cyan_orbit", "Cyan-Kugeln", R, 0, "orb_cyan", -1},
         {"orb_cyan_rise", "Geisterlichter", E, 3, "orb_cyan", -1},
         {"orb_red_spiral", "Blutmond-Spirale", L, 1, "orb", -54742},
         {"orb_green_rise", "Glühwürmchen-Schwarm", R, 3, "orb", -4653233},
         {"orb_gold_crown", "Goldene Lichterkrone", L, 5, "orb", -11713},
         {"orb_white_rings", "Lichtringe", E, 4, "orb", -1},
         {"orb_rainbow", "Regenbogen-Kugeln", L, 0, "orb", 0},
         {"orb_rainbow_helix", "Regenbogen-Helix", L, 6, "orb", 0},
         {"heart_rainbow", "Regenbogen-Herzen", L, 1, "heart", 0},
         {"note_orbit", "Noten-Orbit", R, 0, "hd_note", -1},
         {"note_rise", "Melodie", R, 3, "hd_note", -8587265},
         {"bubble_rise", "Blubberblasen", C, 3, "hd_bubble", -1},
         {"bubble_orbit", "Blasen-Orbit", C, 0, "hd_bubble", -4198401},
         {"leaf_spiral", "Blätterwirbel", C, 1, "hd_leaf", -9785268},
         {"leaf_rain", "Blätterregen", C, 2, "hd_leaf", -2061782},
         {"petal_rain", "Blütenregen", R, 2, "hd_petal", -25920},
         {"rune_rings", "Runenringe", L, 4, "hd_rune", -5153537}
      };

      for (Object[] o : list) {
         int pat = (Integer)o[3];
         int col = (Integer)o[5];
         String tex = (String)o[4];
         aura((String)o[0], (String)o[1], (Cos.Rarity)o[2], (g, a) -> particleAura(g, a, pat, tex, col));
      }
   }

   static void particleAura(G g, Cos.A a, int pat, String tex, int col) {
      g.glow(true);
      float t = a.time;
      boolean flame = tex.equals("flame");
      switch (pat) {
         case 0:
            for (int i = 0; i < 10; i++) {
               double an = t * 1.2 + (Math.PI * 2) * i / 10.0;
               float y = -12.0F + Cos3Geo.sin(t * 1.5 + i) * 4.0F;
               put(g, tex, col, i, t, Cos3Geo.cos(an) * 7.5F, y, Cos3Geo.sin(an) * 7.5F, 1.6F, 1.0F, flame);
            }
            break;
         case 1:
            for (int i = 0; i < 16; i++) {
               float ph = Cos3Geo.fract(t * 0.35F + i / 16.0F);
               double an = ph * Math.PI * 6.0 + t;
               float r = 7.0F - ph * 3.0F;
               put(
                  g,
                  tex,
                  col,
                  i,
                  t,
                  Cos3Geo.cos(an) * r,
                  -24.0F + ph * 30.0F,
                  Cos3Geo.sin(an) * r,
                  1.5F * (1.0F - ph * 0.5F),
                  (float)Math.sin(ph * Math.PI),
                  flame
               );
            }
            break;
         case 2:
            for (int i = 0; i < 18; i++) {
               float ph = Cos3Geo.fract(t * 0.45F + i * 0.137F);
               float x = (Cos3Geo.fract(i * 0.618F) - 0.5F) * 16.0F;
               float z = (Cos3Geo.fract(i * 0.381F + 0.3F) - 0.5F) * 16.0F;
               put(g, tex, col, i, t, x + Cos3Geo.sin(t + i) * 0.6F, 14.0F - ph * 38.0F, z, 1.3F, (float)Math.sin(ph * Math.PI), flame);
            }
            break;
         case 3:
            for (int i = 0; i < 16; i++) {
               float ph = Cos3Geo.fract(t * 0.3F + i * 0.173F);
               double an = i * 2.4;
               float r = 4.0F + Cos3Geo.fract(i * 0.41F) * 4.0F;
               put(
                  g,
                  tex,
                  col,
                  i,
                  t,
                  Cos3Geo.cos(an) * r + Cos3Geo.sin(t * 2.0F + i) * 0.5F,
                  -24.0F + ph * 34.0F,
                  Cos3Geo.sin(an) * r,
                  1.3F,
                  (float)Math.sin(ph * Math.PI),
                  flame
               );
            }
            break;
         case 4:
            for (int ring = 0; ring < 3; ring++) {
               float ph = Cos3Geo.fract(t * 0.4F + ring / 3.0F);
               float r = 3.0F + ph * 8.0F;
               float y = -23.5F + ring * 0.1F;

               for (int i = 0; i < 12; i++) {
                  double an = (Math.PI * 2) * i / 12.0 + ring;
                  put(g, tex, col, i + ring * 12, t, Cos3Geo.cos(an) * r, y + ph * 2.0F, Cos3Geo.sin(an) * r, 1.2F, 1.0F - ph, flame);
               }
            }
            break;
         case 5:
            for (int i = 0; i < 9; i++) {
               double an = t * 0.8 + (Math.PI * 2) * i / 9.0;
               put(g, tex, col, i, t, Cos3Geo.cos(an) * 5.6F, 5.0F + Cos3Geo.sin(t * 3.0F + i) * 0.4F, Cos3Geo.sin(an) * 5.6F, 1.3F, 1.0F, flame);
            }
            break;
         case 6:
            for (int k = 0; k < 2; k++) {
               for (int i = 0; i < 12; i++) {
                  float f = i / 12.0F;
                  double an = t * 2.0F + f * Math.PI * 4.0 + k * Math.PI;
                  put(g, tex, col, i + k * 12, t, Cos3Geo.cos(an) * 6.5F, -24.0F + f * 32.0F, Cos3Geo.sin(an) * 6.5F, 1.2F, 0.9F, flame);
               }
            }
            break;
         default:
            float ph = Cos3Geo.fract(t * 0.5F);

            for (int i = 0; i < 18; i++) {
               double an = i * 2.399;
               double el = (Cos3Geo.fract(i * 0.37F) - 0.3F) * 1.6;
               float r = 2.0F + ph * 12.0F;
               put(
                  g,
                  tex,
                  col,
                  i,
                  t,
                  (float)(Math.cos(an) * Math.cos(el)) * r,
                  -10.0F + (float)Math.sin(el) * r,
                  (float)(Math.sin(an) * Math.cos(el)) * r,
                  1.4F * (1.0F - ph * 0.6F),
                  1.0F - ph,
                  flame
               );
            }
      }

      g.color(-1).glow(false);
   }

   private static void put(G g, String tex, int col, int i, float t, float x, float y, float z, float s, float alpha, boolean flame) {
      int c = col == 0 ? Geo.hue(t * 0.2 + i * 0.08, 0.75, 1.0) : col;
      g.color(Cos3Geo.alpha(Math.max(0.0F, Math.min(1.0F, alpha)) * ((c >>> 24) / 255.0), c & 16777215));
      Geo.sprite(g, tex, x, y + (flame ? s * 0.6F : 0.0F), z, flame ? s * 1.7F : s);
   }

   private static void wings() {
      Object[][] fea = new Object[][]{
         {"sky", "Himmelsflügel", R, "FFFFFF_BFE6FF_6AB8FF_FFFFFF_none", false, 0},
         {"rose", "Rosenflügel", E, "FFE6EE_FF9AC0_D63A7A_FFFFFF_none", false, 0},
         {"mint", "Minzflügel", R, "F0FFF6_8AF0C0_2AA878_FFFFFF_none", false, 0},
         {"sun", "Sonnenflügel", L, "FFF6C0_FFC030_FF6A00_FFFFFF_flame", true, 2},
         {"night", "Nachtflügel", E, "3A3A6A_1A1A3A_06061A_A8B8FF_stars", true, 5},
         {"raven", "Rabenflügel", R, "3A3E48_1A1C22_050608_6A7080_rim", false, 3},
         {"parrot", "Papageienflügel", E, "FFE04A_39D353_2E86DE_FFFFFF_bars", false, 0},
         {"peacock", "Pfauenflügel", L, "7CF7D0_1E8A8A_0A3A6A_FFD23F_stars", true, 5},
         {"flamingo", "Flamingoflügel", R, "FFE0EA_FF8AB0_E04A7A_FFFFFF_none", false, 0},
         {"silver", "Silberflügel", E, "FFFFFF_D0D6DE_7A8494_FFFFFF_metal", false, 0},
         {"bronze", "Bronzeflügel", R, "FFE0B0_C8873A_6A3A12_FFF0D0_metal", false, 0},
         {"ice", "Frostfedern", E, "FFFFFF_BFF6FF_4ACFFF_FFFFFF_crystal", true, 5},
         {"lava", "Lavaflügel", L, "FFE07A_FF6A00_8A0A00_FFFFFF_fire", true, 2},
         {"toxic", "Giftflügel", E, "F0FFB0_8AE02A_2A6A0A_D0FF6A_rim", true, 3},
         {"violet", "Veilchenflügel", R, "F0E0FF_B88AFF_6A2AC8_FFFFFF_none", false, 0},
         {"candy", "Zuckerwatteflügel", R, "FFFFFF_FFC0E0_9AD8FF_FFFFFF_none", false, 4},
         {"eagle", "Adlerflügel", E, "F4E8D0_8A5A30_3A2414_F4F0E8_bars", false, 0},
         {"dove", "Taubenflügel", C, "FFFFFF_F0F0F0_C8C8D0_FFFFFF_none", false, 0},
         {"storm", "Sturmflügel", L, "E0E8FF_7A8AB0_2A3450_9FD8FF_stars", true, 5},
         {"emerald", "Smaragdflügel", L, "E0FFE8_3AD87A_0A6A3A_FFFFFF_crystal", true, 5}
      };

      for (Object[] o : fea) {
         String style = "x" + o[3];
         boolean gl = (Boolean)o[4];
         int fx = (Integer)o[5];
         wing((String)o[0], (String)o[1], (Cos.Rarity)o[2], (g, a) -> Wings.feathered(g, a, style, 16.0F, gl, fx));
      }

      Object[][] mem = new Object[][]{
         {"wyvern", "Wyvernflügel", E, "3A6A2A_7AC84A_0A2A0A_C8FF8A_0.9", -14009830, 0, false},
         {"crimson", "Blutrote Schwingen", L, "8A0A1A_FF3A4A_2A0006_FFC0C8_0.9", -14022134, -54726, true},
         {"abyss", "Abgrundflügel", L, "0A0A2A_2A2A6A_000008_6A8AFF_0.85", -15724512, -11900161, true},
         {"sand", "Wüstendrachen", R, "C8A060_F0D090_5A4020_FFF0C0_0.92", -10862560, 0, false},
         {"gold_dragon", "Golddrachen", L, "B08A10_FFE04A_4A3000_FFFFC0_0.9", -9811446, -11713, true},
         {"ghost", "Geisterflügel", E, "C8D8FF_FFFFFF_6A7AB0_FFFFFF_0.55", -3090192, -4198401, true},
         {"plasma", "Plasmaflügel", L, "4A0A6A_FF4AF0_10001A_FFC0FF_0.75", -15070678, -42256, true},
         {"magma", "Magmaflügel", L, "6A1000_FF8A1A_1A0400_FFE07A_0.95", -15070714, -30182, true},
         {"jade", "Jadedrachen", E, "1A6A5A_5AF0C8_062A20_C8FFF0_0.85", -16111072, -10817336, true},
         {"moth", "Mottenflügel", R, "8A7A6A_D8C8B0_3A3028_F0E8D8_0.9", -11911112, 0, false}
      };

      for (Object[] o : mem) {
         String style = "x" + o[3];
         int bone = (Integer)o[4];
         int edge = (Integer)o[5];
         boolean gl = (Boolean)o[6];
         wing((String)o[0], (String)o[1], (Cos.Rarity)o[2], (g, a) -> Wings.membrane(g, a, style, 18.0F, bone, edge, gl));
      }
   }
}
