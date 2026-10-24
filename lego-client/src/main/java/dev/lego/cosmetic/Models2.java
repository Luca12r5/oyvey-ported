package dev.lego.cosmetic;

final class Models2 {
   private Models2() {
   }

   static void register() {
      Cos.add("hat_fox", "Fuchsohren", Cos.Slot.HAT, Cos.Rarity.COMMON, HdHats::foxEars);
      Cos.add("hat_cowboy", "Cowboyhut", Cos.Slot.HAT, Cos.Rarity.RARE, HdHats::cowboy);
      Cos.add("hat_beanie", "Bommelmütze", Cos.Slot.HAT, Cos.Rarity.COMMON, HdHats::beanie);
      Cos.add("hat_flowers", "Blumenkranz", Cos.Slot.HAT, Cos.Rarity.RARE, HdHats::flowerCrown);
      Cos.add("hat_viking", "Wikingerhelm", Cos.Slot.HAT, Cos.Rarity.EPIC, HdHats::viking);
      Cos.add("hat_propeller", "Propellermütze", Cos.Slot.HAT, Cos.Rarity.RARE, HdHats::propeller);
      Cos.add("hat_chef", "Kochmütze", Cos.Slot.HAT, Cos.Rarity.COMMON, HdHats::chef);
      Cos.add("hat_party", "Partyhut", Cos.Slot.HAT, Cos.Rarity.COMMON, HdHats::party);
      Cos.add("hat_wizard", "Zaubererhut", Cos.Slot.HAT, Cos.Rarity.LEGENDARY, HdHats::wizard);
      Cos.add("hat_alien", "Alien-Antennen", Cos.Slot.HAT, Cos.Rarity.RARE, HdHats::alien);
      Cos.add("hat_bow", "Große Schleife", Cos.Slot.HAT, Cos.Rarity.COMMON, HdHats::bow);
      Cos.add("hat_mushroom", "Pilzhut", Cos.Slot.HAT, Cos.Rarity.EPIC, HdHats::mushroom);
   }

   static void foxEars(G g, Cos.A a) {
      float twitch = (float)Math.max(0.0, Math.sin(a.time * 2.7F) - 0.8F) * 70.0F;

      for (int s = 1; s >= -1; s -= 2) {
         g.push();
         g.scale(s, 1.0F, 1.0F);
         g.translate(2.3F, 7.7F, 0.2F);
         g.rotZ(-16.0F - (s > 0 ? twitch : 0.0F));
         g.rotX(-8.0F);
         float w = 4.4F;
         float h = 5.4F;
         g.push();
         g.rotY(18.0F);
         g.quad(
            "fox_ear",
            new float[]{-w / 2.0F, 0.0F, 0.0F},
            new float[]{0.0F, 0.0F, 0.0F},
            new float[]{0.0F, h, 0.0F},
            new float[]{-w / 2.0F, h, 0.0F},
            new float[]{0.0F, 1.0F, 0.5F, 1.0F, 0.5F, 0.0F, 0.0F, 0.0F}
         );
         g.pop();
         g.push();
         g.rotY(-18.0F);
         g.quad(
            "fox_ear",
            new float[]{0.0F, 0.0F, 0.0F},
            new float[]{w / 2.0F, 0.0F, 0.0F},
            new float[]{w / 2.0F, h, 0.0F},
            new float[]{0.0F, h, 0.0F},
            new float[]{0.5F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.5F, 0.0F}
         );
         g.pop();
         g.pop();
      }
   }

   static void cowboy(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 7.3F, 0.0F);
      g.rotZ(-4.0F);
      g.color(-3630486);
      int seg = 28;

      for (int i = 0; i < seg; i++) {
         double a0 = (Math.PI * 2) * i / seg;
         double a1 = (Math.PI * 2) * (i + 1) / seg;
         float c0 = Geo.cos(a0);
         float s0 = Geo.sin(a0);
         float c1 = Geo.cos(a1);
         float s1 = Geo.sin(a1);
         float ri = 4.6F;
         float ro = 8.4F;
         float y0 = 2.2F * c0 * c0 - 0.4F * s0;
         float y1 = 2.2F * c1 * c1 - 0.4F * s1;
         g.quad(
            "leather",
            new float[]{c0 * ro, y0, s0 * ro * 1.08F},
            new float[]{c1 * ro, y1, s1 * ro * 1.08F},
            new float[]{c1 * ri, 0.0F, s1 * ri},
            new float[]{c0 * ri, 0.0F, s0 * ri},
            new float[]{(float)i / seg, 0.0F, (i + 1.0F) / seg, 0.0F, (i + 1.0F) / seg, 1.0F, (float)i / seg, 1.0F}
         );
      }

      Geo.sqTube(g, "leather", 4.7F, 4.2F, 0.0F, 5.2F, 5.0F, 20, 0.0F, 0.0F, 1.0F, 1.0F);
      Geo.sqDome(g, "leather", 4.2F, 0.9F, 5.2F, 5.0F, 20, 2, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1);
      g.box("leather_dark", -0.4F, 5.6F, -3.2F, 0.4F, 6.2F, 3.2F, 0.0F, 0.0F, 1.0F, 1.0F);
      Geo.sqTube(g, "leather_dark", 4.78F, 4.68F, 0.3F, 1.4F, 5.0F, 20, 0.0F, 0.0F, 1.0F, 1.0F);
      g.push();
      g.translate(0.0F, 0.85F, 4.75F);
      g.color(-8054);
      g.box("gold", -0.8F, -0.6F, 0.0F, 0.8F, 0.6F, 0.3F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.color(-1);
      g.pop();
   }

   static void beanie(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 5.2F, 0.0F);
      g.color(-12948768);
      Geo.sqDome(g, "knit", 4.75F, 4.4F, 1.8F, 5.0F, 22, 5, 0.0F, 0.0F, 1.0F, 1.0F);
      Geo.sqTube(g, "knit", 4.75F, 4.75F, 0.0F, 1.8F, 5.0F, 22, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-723724);
      Geo.sqTube(g, "knit", 4.95F, 4.95F, -0.2F, 1.9F, 5.0F, 22, 0.0F, 0.0F, 4.0F, 1.0F);
      float wob = Geo.sin(a.time * 3.0F) * 6.0F + a.move * 10.0F;
      g.translate(0.0F, 6.1F, 0.0F);
      g.rotZ(wob);
      g.rotX(Geo.sin(a.time * 2.2) * 5.0F);
      g.color(-1);
      g.translate(0.0F, 0.9F, 0.0F);
      g.sphere("fur_white", 1.8F, 12, 8, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
   }

   static void flowerCrown(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 8.15F, 0.0F);
      g.torus("vine", 4.95F, 0.42F, 24, 5, 0.0F, 0.0F, 4.0F, 1.0F);
      String[] tex = new String[]{"flower_pink", "flower_white", "flower_yellow", "flower_blue"};
      int n = 12;

      for (int i = 0; i < n; i++) {
         double an = (Math.PI * 2) * i / n;
         g.push();
         g.translate(Geo.cos(an) * 5.0F, 0.35F + 0.2F * Geo.sin(i * 1.7), Geo.sin(an) * 5.0F);
         g.rotY((float)(-Math.toDegrees(an)) + 90.0F);
         g.rotX(-25.0F + Geo.sin(a.time * 2.0F + i) * 6.0F);
         g.rotZ(a.time * 10.0F + i * 40);
         float s = 2.2F + 0.5F * (i * 7 % 3) / 2.0F;
         g.plane(tex[i % 4], 0.0F, 0.0F, s, s, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      g.pop();
   }

   static void viking(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 6.2F, 0.0F);
      g.color(-2564892);
      Geo.sqDome(g, "iron", 4.8F, 4.2F, 0.6F, 5.0F, 22, 5, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1526694);
      Geo.sqTube(g, "steel", 4.9F, 4.9F, -0.4F, 1.1F, 5.0F, 22, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1);

      for (int i = 0; i < 12; i++) {
         float[] p = Geo.sq((Math.PI * 2) * i / 12.0, 4.95F, 5.0F);
         g.push();
         g.translate(p[0], 0.35F, p[1]);
         g.sphere("steel", 0.28F, 5, 3, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      g.color(-1526694);
      g.box("steel", -0.5F, -2.4F, 4.6F, 0.5F, 0.4F, 5.05F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1);

      for (int s = 1; s >= -1; s -= 2) {
         float[][] pts = new float[7][];
         float[] rad = new float[7];

         for (int i = 0; i < 7; i++) {
            float t = i / 6.0F;
            pts[i] = new float[]{s * (4.4F + t * 4.2F), 1.8F + t * t * 5.5F - t * 0.6F, 0.3F - t * 1.4F};
            rad[i] = 1.25F * (1.0F - t) + 0.08F;
         }

         Geo.chain(g, "ivory", pts, rad, 8);
      }

      g.pop();
   }

   static void propeller(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 6.3F, 0.0F);
      Geo.sqDome(g, "cap_segments", 4.85F, 3.4F, 0.0F, 5.0F, 24, 5, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-14787896);
      int seg = 10;

      for (int i = 0; i < seg; i++) {
         double a0 = Math.PI * (0.15 + 0.7 * i / seg);
         double a1 = Math.PI * (0.15 + 0.7 * (i + 1) / seg);
         g.quad(
            "plastic",
            new float[]{Geo.cos(a0) * 4.9F, 0.1F, Geo.sin(a0) * 4.9F},
            new float[]{Geo.cos(a1) * 4.9F, 0.1F, Geo.sin(a1) * 4.9F},
            new float[]{Geo.cos(a1) * 8.3F, -0.5F, Geo.sin(a1) * 8.3F},
            new float[]{Geo.cos(a0) * 8.3F, -0.5F, Geo.sin(a0) * 8.3F},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
      }

      g.color(-13053);
      g.translate(0.0F, 3.35F, 0.0F);
      g.cylinder("plastic", 0.35F, 0.35F, 0.0F, 1.6F, 8, true, 0.0F, 0.0F, 1.0F, 1.0F);
      g.translate(0.0F, 1.6F, 0.0F);
      g.sphere("gloss", 0.7F, 8, 5, 0.0F, 0.0F, 1.0F, 1.0F);
      g.rotY(a.time * (a.preview ? 260.0F : 720.0F + a.move * 900.0F));
      int[] cols = new int[]{-1900533, -16749385, -16736198};

      for (int b = 0; b < 3; b++) {
         g.push();
         g.rotY(b * 120);
         g.rotX(14.0F);
         g.color(cols[b]);
         g.quad(
            "plastic",
            new float[]{0.4F, 0.0F, -0.9F},
            new float[]{5.4F, 0.0F, -1.2F},
            new float[]{5.4F, 0.0F, 1.2F},
            new float[]{0.4F, 0.0F, 0.9F},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
         g.pop();
      }

      g.color(-1);
      g.pop();
   }

   static void chef(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 6.6F, 0.0F);
      Geo.sqTube(g, "cloth", 4.8F, 4.9F, 0.0F, 5.2F, 5.0F, 22, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1513236);
      Geo.sqTube(g, "cloth", 4.95F, 4.95F, -0.1F, 1.3F, 5.0F, 22, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1);
      float puff = 1.0F + Geo.sin(a.time * 1.5) * 0.03F;

      for (int i = 0; i < 6; i++) {
         double an = (Math.PI * 2) * i / 6.0 + 0.3;
         Geo.ellipsoid(g, "cloth", Geo.cos(an) * 2.9F, 6.2F, Geo.sin(an) * 2.9F, 2.9F * puff, 2.5F * puff, 2.9F * puff, 12, 8);
      }

      Geo.ellipsoid(g, "cloth", 0.0F, 7.2F, 0.0F, 3.2F * puff, 2.6F * puff, 3.2F * puff, 12, 8);
      g.pop();
   }

   static void party(G g, Cos.A a) {
      g.push();
      g.translate(1.2F, 7.9F, 0.0F);
      g.rotZ(-14.0F + Geo.sin(a.time * 2.2) * 4.0F);
      g.cylinder("party_cone", 3.3F, 0.12F, 0.0F, 9.0F, 18, false, 0.0F, 1.0F, 1.0F, 0.0F);
      g.color(-7606);
      g.ring("cloth", 3.0F, 3.5F, 0.1F, 18, 0.0F, 0.0F, 1.0F, 1.0F);
      g.torus("fur_white", 3.35F, 0.45F, 18, 5, 0.0F, 0.0F, 1.0F, 1.0F);
      g.translate(0.0F, 9.1F, 0.0F);
      g.color(-38224);
      g.sphere("fur_white", 1.3F, 10, 7, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      int[] cols = new int[]{-50550, -12924673, -7606, -9764998, -5215489};

      for (int i = 0; i < 12; i++) {
         float ph = (a.time * 0.4F + i / 12.0F) % 1.0F;
         double an = i * 2.4 + a.time * 0.5;
         g.push();
         g.translate(Geo.cos(an) * (4 + i % 3), 20.0F - ph * 16.0F, Geo.sin(an) * (4 + i % 3));
         g.rotY(a.time * 200.0F + i * 40);
         g.rotX(a.time * 150.0F + i * 70);
         g.color(Geo.argb((int)(255.0 * Math.min(1.0, Math.sin(ph * Math.PI) * 2.0)), cols[i % 5]));
         g.plane("plastic", 0.0F, 0.0F, 0.8F, 0.5F, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      g.color(-1);
   }

   static void wizard(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 7.8F, 0.0F);
      g.rotZ(-5.0F);
      g.color(-14016886);
      int seg = 28;

      for (int i = 0; i < seg; i++) {
         double a0 = (Math.PI * 2) * i / seg;
         double a1 = (Math.PI * 2) * (i + 1) / seg;
         float d0 = -0.5F * (1.0F + Geo.sin(a0 * 3.0 + 1.0)) * 0.5F;
         float d1 = -0.5F * (1.0F + Geo.sin(a1 * 3.0 + 1.0)) * 0.5F;
         g.quad(
            "felt_purple",
            new float[]{Geo.cos(a0) * 9.0F, d0, Geo.sin(a0) * 9.0F},
            new float[]{Geo.cos(a1) * 9.0F, d1, Geo.sin(a1) * 9.0F},
            new float[]{Geo.cos(a1) * 4.3F, 0.2F, Geo.sin(a1) * 4.3F},
            new float[]{Geo.cos(a0) * 4.3F, 0.2F, Geo.sin(a0) * 4.3F},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
      }

      g.color(-1);
      float r = 4.5F;
      float y = 0.2F;
      float sway = Geo.sin(a.time * 1.4) * 4.0F;

      for (int i = 0; i < 5; i++) {
         float r2 = r * (i < 4 ? 0.74F : 0.05F);
         float h = 3.0F - i * 0.25F;
         g.cylinder("wizard_cone", r, r2, 0.0F, h, 18, false, 0.0F, 1.0F - i / 5.0F, 1.0F, 1.0F - (i + 1) / 5.0F);
         g.translate(0.0F, h, 0.0F);
         g.rotZ(-6 - i * 5 + sway * 0.3F);
         r = r2;
      }

      g.glow(true);
      float tw = 0.6F + 0.4F * Geo.sin(a.time * 5.0F);
      g.color(Geo.argb((int)(255.0F * tw), 16777215));
      Geo.sprite(g, "spark", 0.0F, 0.4F, 0.0F, 2.4F + tw);
      g.pop();
      g.push();
      g.translate(0.0F, 8.4F, 0.0F);
      g.color(-7558);
      g.cylinder("gold", 4.46F, 4.3F, 0.0F, 1.3F, 20, false, 0.0F, 0.0F, 1.0F, 1.0F);

      for (int i = 0; i < 6; i++) {
         float ph = (a.time * 0.35F + i / 6.0F) % 1.0F;
         double an = i * 1.05 + a.time * 0.9;
         g.color(Geo.argb((int)(255.0 * Math.sin(ph * Math.PI)), 16771232));
         Geo.sprite(g, "spark", Geo.cos(an) * (6.0F + ph * 2.0F), 2.0F + ph * 9.0F, Geo.sin(an) * (6.0F + ph * 2.0F), 1.4F);
      }

      g.color(-1).glow(false);
      g.pop();
   }

   static void alien(G g, Cos.A a) {
      for (int s = 1; s >= -1; s -= 2) {
         float[][] pts = new float[6][];
         float[] rad = new float[6];

         for (int i = 0; i < 6; i++) {
            float t = i / 5.0F;
            float sw = Geo.sin(a.time * 3.2 + s + t * 1.5) * 1.4F * t * t + a.move * t * t * 1.5F;
            pts[i] = new float[]{s * (1.8F + t * 1.8F) + sw * 0.5F, 7.9F + t * 7.0F, sw * 0.8F - t * t * 0.8F};
            rad[i] = 0.32F - t * 0.12F;
         }

         g.color(-11870630);
         Geo.chain(g, "plastic", pts, rad, 6);
         float[] tip = pts[5];
         float pulse = 0.8F + 0.2F * Geo.sin(a.time * 4.0F + s);
         g.push();
         g.translate(tip[0], tip[1] + 0.9F, tip[2]);
         g.color(-8585366);
         g.sphere("gloss", 1.1F, 10, 7, 0.0F, 0.0F, 1.0F, 1.0F);
         g.glow(true);
         g.color(Geo.argb((int)(200.0F * pulse), 16777215));
         Geo.sprite(g, "orb", 0.0F, 0.0F, 0.0F, 4.2F * pulse);
         g.glow(false);
         g.pop();
      }

      g.color(-11870630);
      g.box("plastic", -2.4F, 7.9F, -0.6F, 2.4F, 8.4F, 0.6F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1);
   }

   static void bow(G g, Cos.A a) {
      g.push();
      g.translate(1.6F, 8.6F, 1.2F);
      g.rotZ(-14.0F + Geo.sin(a.time * 2.0F) * 2.0F);
      g.rotY(-10.0F);
      g.color(-42342);

      for (int s = 1; s >= -1; s -= 2) {
         g.push();
         g.translate(s * 2.2F, 0.3F, 0.0F);
         g.rotZ(s * 18);
         g.scale(2.5F, 1.7F, 0.9F);
         g.sphere("satin", 1.0F, 14, 9, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
         g.push();
         g.translate(s * 0.8F, -0.6F, 0.2F);
         g.rotZ(s * 25 + Geo.sin(a.time * 2.5 + s) * 5.0F);
         g.quad(
            "satin",
            new float[]{-0.7F, 0.0F, 0.0F},
            new float[]{0.7F, 0.0F, 0.0F},
            new float[]{0.9F, -3.2F, 0.4F},
            new float[]{-0.4F, -3.0F, 0.4F},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
         g.pop();
      }

      g.color(-2080646);
      g.sphere("satin", 0.95F, 10, 7, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1);
      g.pop();
   }

   static void mushroom(G g, Cos.A a) {
      g.push();
      float bob = Geo.sin(a.time * 2.0F) * 0.15F;
      g.translate(0.0F, 7.4F + bob, 0.0F);
      g.rotZ(Geo.sin(a.time * 1.3) * 2.0F);
      Geo.sqDome(g, "mushroom_cap", 6.4F, 4.6F, 0.6F, 2.6F, 26, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      Geo.sqTube(g, "mushroom_cap", 6.4F, 6.4F, 0.1F, 0.6F, 2.6F, 26, 0.0F, 0.9F, 1.0F, 1.0F);
      g.ring("gill", 4.2F, 6.4F, 0.1F, 26, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
   }
}
