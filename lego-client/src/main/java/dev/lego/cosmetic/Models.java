package dev.lego.cosmetic;

final class Models {
   private Models() {
   }

   static void registerAll() {
      Cos.cape("lego", "Lego-Umhang", Cos.Rarity.RARE);
      CapeSpecs.registerAll();
      Cos.cape("galaxy", "Galaxie", Cos.Rarity.EPIC);
      Cos.cape("flames", "Flammen", Cos.Rarity.RARE);
      Cos.cape("ice", "Eiskristall", Cos.Rarity.RARE);
      Cos.cape("aurora", "Polarlicht", Cos.Rarity.EPIC);
      Cos.cape("sunset", "Retro-Sonnenuntergang", Cos.Rarity.RARE);
      Cos.cape("carbon", "Carbon", Cos.Rarity.COMMON);
      Cos.cape("neon", "Neon-Grid", Cos.Rarity.EPIC);
      Cos.cape("spotify", "Spotify", Cos.Rarity.LEGENDARY);
      Cos.cape("sakura", "Kirschblüte", Cos.Rarity.EPIC);
      Cos.cape("lava", "Lavastrom", Cos.Rarity.EPIC);
      Cos.cape("ocean", "Tiefsee", Cos.Rarity.RARE);
      Cos.cape("matrix", "Matrix-Code", Cos.Rarity.EPIC);
      Cos.cape("storm", "Gewitter", Cos.Rarity.EPIC);
      Cos.cape("rainbow", "Regenbogen-Seide", Cos.Rarity.RARE);
      Cos.cape("snow", "Winternacht", Cos.Rarity.RARE);
      Cos.cape("fireflies", "Glühwürmchen", Cos.Rarity.EPIC);
      Cos.cape("heart", "Herzschlag", Cos.Rarity.RARE);
      Cos.cape("enchant", "Verzaubert", Cos.Rarity.LEGENDARY);
      Cos.cape("void", "Schwarzes Loch", Cos.Rarity.LEGENDARY);
      Cos.cape("royal", "Königsmantel", Cos.Rarity.LEGENDARY);
      Cos.cape("dragon", "Drachenschuppen", Cos.Rarity.EPIC);
      Cos.cape("camo", "Tarnmuster", Cos.Rarity.COMMON);
      Cos.cape("bricks", "Bausteine", Cos.Rarity.COMMON);
      Cos.cape("racing", "Zielflagge", Cos.Rarity.COMMON);
      Cos.cape("moon", "Mondnacht", Cos.Rarity.RARE);
      Wings.register();
      Cos.add("hat_crown", "Königskrone", Cos.Slot.HAT, Cos.Rarity.LEGENDARY, HdHats::crown);
      Cos.add("hat_halo", "Heiligenschein", Cos.Slot.HAT, Cos.Rarity.EPIC, HdHats::halo);
      Cos.add("hat_tophat", "Zylinder", Cos.Slot.HAT, Cos.Rarity.RARE, HdHats::tophat);
      Cos.add("hat_santa", "Weihnachtsmütze", Cos.Slot.HAT, Cos.Rarity.RARE, HdHats::santa);
      Cos.add("hat_witch", "Hexenhut", Cos.Slot.HAT, Cos.Rarity.EPIC, HdHats::witch);
      Cos.add("hat_cat", "Katzenohren", Cos.Slot.HAT, Cos.Rarity.COMMON, HdHats::catEars);
      Cos.add("hat_bunny", "Hasenohren", Cos.Slot.HAT, Cos.Rarity.COMMON, HdHats::bunnyEars);
      Cos.add("hat_horns", "Teufelshörner", Cos.Slot.HAT, Cos.Rarity.EPIC, HdHats::horns);
      Cos.add("hat_headphones", "Kopfhörer", Cos.Slot.HAT, Cos.Rarity.RARE, HdHats::headphones);
      Models2.register();
      Cos.add("face_sunglasses", "Sonnenbrille", Cos.Slot.FACE, Cos.Rarity.RARE, HdFace::sunglasses);
      Cos.add("face_pixel", "Pixel-Brille", Cos.Slot.FACE, Cos.Rarity.COMMON, HdFace::pixelGlasses);
      Models3.registerFace();
      Cos.add("back_lego", "Lego-Rucksack", Cos.Slot.BACK, Cos.Rarity.EPIC, HdBack::legoBackpack);
      Cos.add("back_sword", "Kristallschwert", Cos.Slot.BACK, Cos.Rarity.RARE, HdBack::backSword);
      Models3.registerBack();
      Auras.register();
      Cos3Models.registerAll();
      CosPack.register();
   }

   static void capePreview(G g, Cos.A a) {
   }

   static void capeGeometry(G g, Cos.A a, String tex) {
      String t = Capes.frameName(tex, a.time);
      g.push();
      g.translate(0.0F, 0.0F, -2.25F);
      g.rotX(a.sneak ? 22.0F : 6.0F + a.move * 20.0F + (float)Math.sin(a.time * 1.3) * 1.5F);
      float W = 512.0F;
      float H = 256.0F;
      float fu0 = 8.0F / W;
      float fu1 = 88.0F / W;
      float iu0 = 96.0F / W;
      float iu1 = 176.0F / W;
      int rows = 4;
      float y = 0.0F;
      float z = 0.0F;

      for (int r = 0; r < rows; r++) {
         float ang = (float)Math.toRadians(r * 3.0 + Math.sin(a.time * 1.7 - r * 0.8) * 1.2);
         float h = 16.0F / rows;
         float y1 = y - h * (float)Math.cos(ang);
         float z1 = z - h * (float)Math.sin(ang);
         float v0 = (8.0F + 128.0F * r / rows) / H;
         float v1 = (8.0F + 128.0F * (r + 1) / rows) / H;
         g.quad(
            t,
            new float[]{-5.0F, y, z - 1.0F},
            new float[]{5.0F, y, z - 1.0F},
            new float[]{5.0F, y1, z1 - 1.0F},
            new float[]{-5.0F, y1, z1 - 1.0F},
            new float[]{fu0, v0, fu1, v0, fu1, v1, fu0, v1}
         );
         g.quad(
            t,
            new float[]{5.0F, y, z},
            new float[]{-5.0F, y, z},
            new float[]{-5.0F, y1, z1},
            new float[]{5.0F, y1, z1},
            new float[]{iu0, v0, iu1, v0, iu1, v1, iu0, v1}
         );
         g.quad(
            t,
            new float[]{-5.0F, y, z},
            new float[]{-5.0F, y, z - 1.0F},
            new float[]{-5.0F, y1, z1 - 1.0F},
            new float[]{-5.0F, y1, z1},
            new float[]{0.0F, v0, 8.0F / W, v0, 8.0F / W, v1, 0.0F, v1}
         );
         g.quad(
            t,
            new float[]{5.0F, y, z - 1.0F},
            new float[]{5.0F, y, z},
            new float[]{5.0F, y1, z1},
            new float[]{5.0F, y1, z1 - 1.0F},
            new float[]{88.0F / W, v0, 96.0F / W, v0, 96.0F / W, v1, 88.0F / W, v1}
         );
         if (r == rows - 1) {
            g.quad(
               t,
               new float[]{-5.0F, y1, z1},
               new float[]{5.0F, y1, z1},
               new float[]{5.0F, y1, z1 - 1.0F},
               new float[]{-5.0F, y1, z1 - 1.0F},
               new float[]{88.0F / W, 0.0F, 168.0F / W, 0.0F, 168.0F / W, 8.0F / H, 88.0F / W, 8.0F / H}
            );
         }

         y = y1;
         z = z1;
      }

      g.quad(
         t,
         new float[]{-5.0F, 0.0F, 0.0F},
         new float[]{5.0F, 0.0F, 0.0F},
         new float[]{5.0F, 0.0F, -1.0F},
         new float[]{-5.0F, 0.0F, -1.0F},
         new float[]{8.0F / W, 0.0F, 88.0F / W, 0.0F, 88.0F / W, 8.0F / H, 8.0F / W, 8.0F / H}
      );
      g.pop();
   }

   static void crown(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 8.05F, 0.0F);
      g.rotZ(-6.0F);
      g.cylinder("gold", 4.55F, 4.55F, 0.0F, 2.3F, 20, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.cylinder("gold", 4.35F, 4.35F, 0.0F, 2.3F, 20, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.ring("gold", 4.35F, 4.55F, 2.3F, 20, 0.0F, 0.0F, 1.0F, 1.0F);

      for (int i = 0; i < 8; i++) {
         g.push();
         g.rotY(i * 45 + 22.5F);
         g.translate(0.0F, 2.3F, 4.45F);
         float hh = i % 2 == 0 ? 3.2F : 2.3F;
         g.quad(
            "gold",
            new float[]{-1.4F, 0.0F, 0.0F},
            new float[]{1.4F, 0.0F, 0.0F},
            new float[]{0.05F, hh, 0.0F},
            new float[]{-0.05F, hh, 0.0F},
            new float[]{0.0F, 1.0F, 1.0F, 1.0F, 0.5F, 0.0F, 0.5F, 0.0F}
         );
         g.push();
         g.translate(0.0F, hh + 0.35F, 0.0F);
         g.sphere("gems", 0.55F, 8, 5, i % 4 * 0.25F, 0.0F, i % 4 * 0.25F + 0.25F, 1.0F);
         g.pop();
         g.pop();
      }

      for (int i = 0; i < 4; i++) {
         g.push();
         g.rotY(i * 90);
         g.translate(0.0F, 1.15F, 4.62F);
         g.box("gems", -0.6F, -0.6F, -0.2F, 0.6F, 0.6F, 0.25F, i % 4 * 0.25F, 0.0F, i % 4 * 0.25F + 0.25F, 1.0F);
         g.pop();
      }

      g.pop();
   }

   static void halo(G g, Cos.A a) {
      float bob = (float)Math.sin(a.time * 2.2F) * 0.45F;
      g.push();
      g.translate(0.0F, 11.4F + bob, 0.0F);
      g.rotX(-10.0F);
      g.rotY(a.time * 40.0F);
      g.glow(true);
      g.torus("halo", 4.5F, 0.5F, 28, 8, 0.0F, 0.0F, 1.0F, 1.0F);
      float pulse = 0.75F + 0.25F * (float)Math.sin(a.time * 3.0F);
      g.color((int)(110.0F * pulse) << 24 | 16771496);
      g.torus("orb", 4.5F, 1.4F, 20, 6, 0.2F, 0.2F, 0.8F, 0.8F);

      for (int i = 0; i < 5; i++) {
         double an = (Math.PI * 2) * i / 5.0;
         float tw = 0.5F + 0.5F * (float)Math.sin(a.time * 4.0F + i * 1.9);
         g.color((int)(255.0F * tw) << 24 | 16777215);
         Geo.sprite(g, "spark", (float)Math.cos(an) * 4.5F, 0.3F, (float)Math.sin(an) * 4.5F, 1.0F + tw * 1.4F);
      }

      g.color(-1).glow(false);
      g.pop();
   }

   static void tophat(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 7.95F, 0.0F);
      g.rotZ(-4.0F);
      g.cylinder("felt_black", 6.9F, 6.9F, 0.0F, 0.55F, 24, true, 0.0F, 0.0F, 1.0F, 1.0F);
      g.cylinder("felt_black", 4.15F, 4.3F, 0.55F, 8.2F, 24, true, 0.0F, 0.0F, 1.0F, 1.0F);
      g.cylinder("band_red", 4.25F, 4.33F, 0.6F, 2.2F, 24, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
   }

   static void santa(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 7.4F, 0.0F);
      g.cylinder("felt_red", 4.35F, 3.1F, 1.4F, 6.2F, 20, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.push();
      g.translate(0.0F, 6.2F, 0.0F);
      float sway = (float)Math.sin(a.time * 1.6F) * 5.0F;
      g.rotZ(-55.0F + sway);
      g.rotX(-10.0F);
      g.cylinder("felt_red", 3.1F, 0.5F, 0.0F, 5.5F, 16, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.translate(0.0F, 5.6F, 0.0F);
      g.sphere("fur_white", 1.35F, 12, 8, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.cylinder("fur_white", 4.75F, 4.75F, 0.0F, 1.9F, 24, true, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
   }

   static void witch(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 7.8F, 0.0F);
      g.rotZ(-6.0F);
      g.cylinder("felt_purple", 8.2F, 8.2F, 0.0F, 0.45F, 28, true, 0.0F, 0.0F, 1.0F, 1.0F);
      g.cylinder("felt_purple", 4.3F, 3.1F, 0.45F, 5.2F, 22, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.cylinder("band_green", 4.28F, 4.0F, 0.5F, 1.8F, 22, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.push();
      g.translate(0.0F, 1.15F, 4.2F);
      g.box("gold", -1.1F, -0.8F, 0.0F, 1.1F, 0.8F, 0.35F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.translate(0.0F, 5.2F, 0.0F);
      g.rotZ(-18.0F);
      g.rotX(8.0F);
      g.cylinder("felt_purple", 3.1F, 1.5F, 0.0F, 4.2F, 18, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.translate(0.0F, 4.2F, 0.0F);
      g.rotZ(-35.0F);
      g.cylinder("felt_purple", 1.5F, 0.1F, 0.0F, 3.6F, 14, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
   }

   static void catEars(G g, Cos.A a) {
      float twitch = (float)Math.max(0.0, Math.sin(a.time * 3.1F) - 0.85F) * 60.0F;

      for (int s = 1; s >= -1; s -= 2) {
         g.push();
         g.scale(s, 1.0F, 1.0F);
         g.translate(2.4F, 7.6F, 0.4F);
         g.rotZ(-14.0F - (s > 0 ? twitch : 0.0F));
         g.plane("cat_ear", 0.0F, 2.4F, 3.8F, 4.2F, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }
   }

   static void bunnyEars(G g, Cos.A a) {
      for (int s = 1; s >= -1; s -= 2) {
         g.push();
         g.scale(s, 1.0F, 1.0F);
         g.translate(1.8F, 7.8F, 0.0F);
         float wob = (float)Math.sin(a.time * 2.4F + s) * 6.0F + a.move * 12.0F;
         g.rotX(-8.0F - wob);
         g.rotZ(-10.0F);
         g.plane("bunny_ear", 0.0F, 4.6F, 3.0F, 9.2F, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }
   }

   static void horns(G g, Cos.A a) {
      for (int s = 1; s >= -1; s -= 2) {
         g.push();
         g.scale(s, 1.0F, 1.0F);
         g.translate(2.6F, 7.6F, 1.2F);
         g.rotZ(-35.0F);
         float r = 1.2F;

         for (int i = 0; i < 6; i++) {
            float r2 = r * 0.78F;
            g.cylinder("horn", r, r2, 0.0F, 1.4F, 10, i == 5, 0.0F, 1.0F - i / 6.0F, 1.0F, 1.0F - (i + 1) / 6.0F);
            g.translate(0.0F, 1.35F, 0.0F);
            g.rotZ(14.0F);
            g.rotX(-4.0F);
            r = r2;
         }

         g.pop();
      }
   }

   static void headphones(G g, Cos.A a) {
      g.push();
      int seg = 12;

      for (int i = 0; i < seg; i++) {
         double a0 = Math.PI * i / seg;
         double a1 = Math.PI * (i + 1) / seg;
         float R = 5.3F;
         float x0 = (float)Math.cos(a0) * R;
         float y0 = 3.8F + (float)Math.sin(a0) * R * 0.95F;
         float x1 = (float)Math.cos(a1) * R;
         float y1 = 3.8F + (float)Math.sin(a1) * R * 0.95F;
         float nx0 = (float)Math.cos(a0) * 0.8F;
         float ny0 = (float)Math.sin(a0) * 0.8F;
         float nx1 = (float)Math.cos(a1) * 0.8F;
         float ny1 = (float)Math.sin(a1) * 0.8F;
         float z0 = -0.9F;
         float z1 = 0.9F;
         g.quad(
            "headphone",
            new float[]{x0 + nx0, y0 + ny0, z0},
            new float[]{x0 + nx0, y0 + ny0, z1},
            new float[]{x1 + nx1, y1 + ny1, z1},
            new float[]{x1 + nx1, y1 + ny1, z0},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
         g.quad(
            "headphone",
            new float[]{x0, y0, z1},
            new float[]{x0, y0, z0},
            new float[]{x1, y1, z0},
            new float[]{x1, y1, z1},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
         g.quad(
            "headphone",
            new float[]{x0, y0, z1},
            new float[]{x0 + nx0, y0 + ny0, z1},
            new float[]{x1 + nx1, y1 + ny1, z1},
            new float[]{x1, y1, z1},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
         g.quad(
            "headphone",
            new float[]{x0 + nx0, y0 + ny0, z0},
            new float[]{x0, y0, z0},
            new float[]{x1, y1, z0},
            new float[]{x1 + nx1, y1 + ny1, z0},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
      }

      for (int s = 1; s >= -1; s -= 2) {
         g.push();
         g.translate(s * 4.2F, 3.4F, 0.0F);
         g.rotZ(s * -90);
         g.cylinder("headphone", 2.5F, 2.5F, 0.0F, 1.8F, 18, true, 0.0F, 0.0F, 1.0F, 1.0F);
         g.translate(0.0F, 1.82F, 0.0F);
         g.cylinder("cushion", 2.2F, 2.2F, 0.0F, 0.05F, 18, true, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      g.pop();
   }

   static void sunglasses(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 3.6F, 4.12F);
      g.plane("sunglasses", 0.0F, 0.0F, 9.0F, 2.25F, 0.0F, 0.0F, 1.0F, 1.0F);

      for (int s = 1; s >= -1; s -= 2) {
         g.push();
         g.translate(s * 4.3F, 0.4F, 0.0F);
         g.box("felt_black", -0.18F, -0.25F, -5.5F, 0.18F, 0.25F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      g.pop();
   }

   static void pixelGlasses(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 3.5F, 4.1F);
      g.plane("pixelglasses", 0.0F, 0.0F, 8.0F, 2.0F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
   }

   static void legoBackpack(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, -1.4F, -2.1F);
      float[][] f = new float[][]{
         {0.0F, 0.0F, 1.0F, 1.0F},
         {0.0F, 0.0F, 1.0F, 1.0F},
         {0.0F, 0.0F, 1.0F, 1.0F},
         {0.0F, 0.0F, 1.0F, 1.0F},
         {0.0F, 0.0F, 1.0F, 1.0F},
         {0.0F, 0.0F, 1.0F, 1.0F}
      };
      g.box6("lego_red", -3.2F, -8.2F, -3.4F, 3.2F, 0.0F, 0.0F, f);

      for (int ix = 0; ix < 2; ix++) {
         for (int iz = 0; iz < 2; iz++) {
            g.push();
            g.translate(-1.5F + ix * 3, 0.0F, -0.85F - iz * 1.7F);
            g.cylinder("lego_red", 0.95F, 0.95F, 0.0F, 0.9F, 14, true, 0.1F, 0.1F, 0.9F, 0.9F);
            g.pop();
         }
      }

      g.box("lego_yellow", -2.2F, -6.6F, -3.55F, 2.2F, -4.4F, -3.3F, 0.0F, 0.0F, 1.0F, 1.0F);

      for (int s = 1; s >= -1; s -= 2) {
         g.box("strap", s * 2.1F - 0.55F, -6.0F, 0.0F, s * 2.1F + 0.55F, 0.3F, 0.35F, 0.0F, 0.0F, 1.0F, 1.0F);
         g.box("strap", s * 2.1F - 0.55F, 0.0F, 0.0F, s * 2.1F + 0.55F, 0.35F, 4.4F, 0.0F, 0.0F, 1.0F, 1.0F);
         g.box("strap", s * 2.1F - 0.55F, -5.5F, 4.1F, s * 2.1F + 0.55F, 0.35F, 4.45F, 0.0F, 0.0F, 1.0F, 1.0F);
      }

      g.pop();
   }

   static void backSword(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, -5.5F, -2.6F);
      g.rotZ(-40.0F);
      float L = 14.0F;
      float w = 1.3F;
      g.glow(false);
      g.quad(
         "sword_blade",
         new float[]{-w, 2.0F, -0.1F},
         new float[]{w, 2.0F, -0.1F},
         new float[]{w, 2.0F + L, -0.1F},
         new float[]{-w, 2.0F + L, -0.1F},
         new float[]{0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F}
      );
      g.quad(
         "sword_blade",
         new float[]{-w, 2.0F + L, -0.1F},
         new float[]{w, 2.0F + L, -0.1F},
         new float[]{0.0F, 4.0F + L, -0.1F},
         new float[]{0.0F, 4.0F + L, -0.1F},
         new float[]{0.0F, 0.0F, 1.0F, 0.0F, 0.5F, 0.0F, 0.5F, 0.0F}
      );
      g.quad(
         "sword_blade",
         new float[]{-w, 2.0F, -0.5F},
         new float[]{w, 2.0F, -0.5F},
         new float[]{w, 2.0F + L, -0.5F},
         new float[]{-w, 2.0F + L, -0.5F},
         new float[]{0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F}
      );
      g.quad(
         "sword_blade",
         new float[]{-w, 2.0F + L, -0.5F},
         new float[]{w, 2.0F + L, -0.5F},
         new float[]{0.0F, 4.0F + L, -0.5F},
         new float[]{0.0F, 4.0F + L, -0.5F},
         new float[]{0.0F, 0.0F, 1.0F, 0.0F, 0.5F, 0.0F, 0.5F, 0.0F}
      );
      g.box("sword_blade", -w, 2.0F, -0.5F, w, 2.0F + L, -0.1F, 0.4F, 0.1F, 0.6F, 0.9F);
      g.glow(true);
      float sh = a.time * 0.6F % 1.0F;
      g.color(-1432684801);
      g.box("orb", -w * 0.5F, 2.0F + L * sh, -0.55F, w * 0.5F, 2.0F + L * sh + 2.0F, -0.05F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.glow(false);
      g.color(-10863968);
      g.box("steel", -3.2F, 1.1F, -0.8F, 3.2F, 2.1F, 0.2F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1);
      g.box("leather_dark", -0.5F, -2.8F, -0.6F, 0.5F, 1.1F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-8054);
      g.push();
      g.translate(0.0F, -3.4F, -0.3F);
      g.sphere("gold", 0.85F, 10, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.push();
      g.translate(0.0F, 1.6F, 0.25F);
      g.color(-1);
      g.sphere("gems", 0.5F, 8, 5, 0.25F, 0.0F, 0.5F, 1.0F);
      g.pop();
      g.color(-1);
      g.pop();
   }
}
