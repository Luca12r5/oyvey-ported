package dev.lego.cosmetic;

final class Models3 {
   private Models3() {
   }

   static void registerFace() {
      Cos.add("face_hearts", "Herzbrille", Cos.Slot.FACE, Cos.Rarity.RARE, HdFace::heartGlasses);
      Cos.add("face_stars", "Sternenbrille", Cos.Slot.FACE, Cos.Rarity.RARE, HdFace::starGlasses);
      Cos.add("face_monocle", "Monokel", Cos.Slot.FACE, Cos.Rarity.EPIC, HdFace::monocle);
      Cos.add("face_mustache", "Schnurrbart", Cos.Slot.FACE, Cos.Rarity.COMMON, HdFace::mustache);
      Cos.add("face_clown", "Clownsnase", Cos.Slot.FACE, Cos.Rarity.COMMON, HdFace::clownNose);
      Cos.add("face_visor", "Cyber-Visier", Cos.Slot.FACE, Cos.Rarity.LEGENDARY, HdFace::visor);
      Cos.add("face_masq", "Maskenball", Cos.Slot.FACE, Cos.Rarity.EPIC, HdFace::masquerade);
      Cos.add("face_ninja", "Ninja-Maske", Cos.Slot.FACE, Cos.Rarity.RARE, HdFace::ninja);
   }

   static void registerBack() {
      Cos.add("back_guitar", "E-Gitarre", Cos.Slot.BACK, Cos.Rarity.EPIC, HdBack::guitar);
      Cos.add("back_jetpack", "Jetpack", Cos.Slot.BACK, Cos.Rarity.LEGENDARY, HdBack::jetpack);
      Cos.add("back_shield", "Ritterschild", Cos.Slot.BACK, Cos.Rarity.RARE, HdBack::shield);
      Cos.add("back_quiver", "Köcher", Cos.Slot.BACK, Cos.Rarity.RARE, HdBack::quiver);
      Cos.add("back_rocket", "Rakete", Cos.Slot.BACK, Cos.Rarity.EPIC, HdBack::rocket);
      Cos.add("back_teddy", "Teddy", Cos.Slot.BACK, Cos.Rarity.COMMON, HdBack::teddy);
      Cos.add("back_balloon", "Herzballon", Cos.Slot.BACK, Cos.Rarity.RARE, HdBack::balloon);
   }

   private static void temples(G g, float y, float z) {
      for (int s = 1; s >= -1; s -= 2) {
         g.box("felt_black", s * 4.12F - 0.16F, y - 0.22F, -3.5F, s * 4.12F + 0.16F, y + 0.22F, z, 0.0F, 0.0F, 1.0F, 1.0F);
      }
   }

   static void heartGlasses(G g, Cos.A a) {
      float p = 1.0F + 0.06F * (float)Math.max(0.0, Math.sin(a.time * 6.0F));
      g.push();
      g.translate(0.0F, 3.6F, 4.25F);
      g.scale(p, p, 1.0F);
      g.plane("heart_glasses", 0.0F, 0.0F, 9.2F, 3.45F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.color(-2092705);
      temples(g, 4.1F, 4.2F);
      g.color(-1);
   }

   static void starGlasses(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 3.6F, 4.25F);
      g.plane("star_glasses", 0.0F, 0.0F, 9.4F, 3.5F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.glow(true);
      float tw = 0.5F + 0.5F * Geo.sin(a.time * 3.0F);
      g.color(Geo.argb((int)(255.0F * tw), 16777215));
      Geo.sprite(g, "spark", 3.6F, 1.2F, 0.2F, 1.2F + tw);
      g.color(Geo.argb((int)(255.0F * (1.0F - tw)), 16777215));
      Geo.sprite(g, "spark", -2.2F, -1.0F, 0.2F, 1.0F + (1.0F - tw));
      g.glow(false).color(-1531888);
      g.pop();
      temples(g, 3.8F, 4.2F);
      g.color(-1);
   }

   static void monocle(G g, Cos.A a) {
      g.push();
      g.translate(-1.9F, 3.7F, 4.25F);
      g.color(-8054);
      g.push();
      g.rotX(90.0F);
      g.torus("gold", 1.45F, 0.2F, 20, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.color(-1);
      g.plane("monocle_glass", 0.0F, 0.0F, 2.9F, 2.9F, 0.0F, 0.0F, 1.0F, 1.0F);
      float sw = Geo.sin(a.time * 1.8) * 0.3F;
      int n = 14;
      g.color(-8054);

      for (int i = 0; i < n; i++) {
         float t = (float)i / (n - 1);
         float x = -1.2F - t * 2.2F + sw * t;
         float y = -1.4F - (float)Math.sin(t * Math.PI) * 2.8F - t * 0.5F;
         g.push();
         g.translate(x, y, 0.1F - t * 0.4F);
         g.sphere("gold", 0.17F, 5, 3, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      g.color(-1);
      g.pop();
   }

   static void mustache(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 1.55F, 4.22F);
      g.rotZ(Geo.sin(a.time * 2.2) * 1.5F);
      float tw = Math.max(0.0F, Geo.sin(a.time * 1.3)) * 0.1F;
      g.scale(1.0F + tw, 1.0F, 1.0F);
      g.push();
      g.rotY(6.0F);
      g.quad(
         "mustache",
         new float[]{-3.8F, 1.15F, 0.0F},
         new float[]{0.0F, 1.15F, 0.0F},
         new float[]{0.0F, -1.15F, 0.0F},
         new float[]{-3.8F, -1.15F, 0.0F},
         new float[]{0.0F, 0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.0F, 1.0F}
      );
      g.pop();
      g.push();
      g.rotY(-6.0F);
      g.quad(
         "mustache",
         new float[]{0.0F, 1.15F, 0.0F},
         new float[]{3.8F, 1.15F, 0.0F},
         new float[]{3.8F, -1.15F, 0.0F},
         new float[]{0.0F, -1.15F, 0.0F},
         new float[]{0.5F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.5F, 1.0F}
      );
      g.pop();
      g.pop();
   }

   static void clownNose(G g, Cos.A a) {
      float sq = 1.0F + 0.05F * Geo.sin(a.time * 3.0F);
      g.push();
      g.translate(0.0F, 2.6F, 4.9F);
      g.scale(sq, 1.0F / sq, sq);
      g.color(-54742);
      g.sphere("gloss", 1.25F, 14, 9, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1);
      g.pop();
   }

   static void visor(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 3.7F, 0.0F);
      int seg = 10;
      float R = 5.0F;
      float h = 2.8F;

      for (int i = 0; i < seg; i++) {
         double a0 = Math.PI * (0.12 + 0.76 * i / seg);
         double a1 = Math.PI * (0.12 + 0.76 * (i + 1) / seg);
         float[] p0 = Geo.sq(a0, R, 5.0F);
         float[] p1 = Geo.sq(a1, R, 5.0F);
         float u0 = 1.0F - (float)i / seg;
         float u1 = 1.0F - (i + 1.0F) / seg;
         g.quad(
            "visor",
            new float[]{p0[0], h / 2.0F, p0[1]},
            new float[]{p1[0], h / 2.0F, p1[1]},
            new float[]{p1[0], -h / 2.0F, p1[1]},
            new float[]{p0[0], -h / 2.0F, p0[1]},
            new float[]{u0, 0.0F, u1, 0.0F, u1, 1.0F, u0, 1.0F}
         );
      }

      g.glow(true);
      float sweep = 0.5F + 0.5F * Geo.sin(a.time * 2.4);
      double an = Math.PI * (0.2 + 0.6 * sweep);
      float[] p = Geo.sq(an, R + 0.08F, 5.0F);
      g.color(-50598);
      g.push();
      g.translate(p[0], 0.0F, p[1]);
      g.rotY((float)(-Math.toDegrees(an)) + 90.0F);
      g.plane("glow_line", 0.0F, 0.0F, 1.6F, 1.3F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1275119014);
      g.plane("orb", 0.0F, 0.0F, 4.0F, 3.0F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.color(-13965569);

      for (int s = 1; s >= -1; s -= 2) {
         g.box("white", s * 4.5F - 0.15F, -0.2F, -1.4F, s * 4.5F + 0.15F, 0.2F, 1.2F, 0.0F, 0.0F, 1.0F, 1.0F);
      }

      g.glow(false);
      g.color(-14012874);
      g.box("steel", -4.4F, -0.7F, -4.4F, -4.05F, 0.7F, -1.4F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.box("steel", 4.05F, -0.7F, -4.4F, 4.4F, 0.7F, -1.4F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.box("steel", -4.4F, -0.7F, -4.4F, 4.4F, 0.7F, -4.05F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1);
      g.pop();
   }

   static void masquerade(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 3.9F, 4.25F);
      g.plane("mask_masq", 0.0F, 0.0F, 9.6F, 4.8F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.translate(3.6F, 1.8F, 0.1F);
      g.rotZ(-28.0F + Geo.sin(a.time * 2.0F) * 5.0F);
      g.plane("feather_plume", 0.0F, 2.6F, 1.6F, 6.0F, 0.0F, 1.0F, 1.0F, 0.0F);
      g.rotZ(22.0F);
      g.plane("feather_plume", 0.0F, 2.2F, 1.4F, 5.0F, 0.0F, 1.0F, 1.0F, 0.0F);
      g.pop();
   }

   static void ninja(G g, Cos.A a) {
      g.push();
      g.color(-14539732);
      Geo.sqTube(g, "cloth", 4.35F, 4.35F, -0.1F, 3.0F, 5.0F, 20, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-3662294);
      Geo.sqTube(g, "cloth", 4.45F, 4.45F, 5.4F, 6.6F, 5.0F, 20, 0.0F, 0.0F, 1.0F, 1.0F);
      float fl = Geo.sin(a.time * 4.0F) * 10.0F + a.move * 25.0F;

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 0.6F, 6.0F, -4.4F);
         g.rotY(s * 15);
         g.rotX(-20.0F - fl * 0.5F + s * 5);
         g.quad(
            "cloth",
            new float[]{-0.5F, 0.0F, 0.0F},
            new float[]{0.5F, 0.0F, 0.0F},
            new float[]{0.4F, -5.0F, -0.2F},
            new float[]{-0.6F, -5.0F, -0.2F},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
         g.pop();
      }

      g.color(-1);
      g.pop();
   }

   static void guitar(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, -7.0F, -2.9F);
      g.rotZ(-32.0F + Geo.sin(a.time * 1.5) * 1.2F);
      g.rotY(180.0F);
      g.color(-1);
      Geo.ellipsoid(g, "guitar_body", 0.0F, -2.4F, 0.0F, 3.6F, 3.4F, 0.8F, 16, 8);
      Geo.ellipsoid(g, "guitar_body", 0.0F, 1.6F, 0.0F, 2.7F, 2.5F, 0.78F, 16, 8);
      g.box("wood", -0.55F, 3.5F, -0.25F, 0.55F, 12.5F, 0.35F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-15066594);
      g.box("plastic", -0.95F, 12.5F, -0.25F, 0.95F, 15.0F, 0.3F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1);

      for (int i = 0; i < 3; i++) {
         for (int s = -1; s <= 1; s += 2) {
            g.box("steel", s * 0.95F - 0.25F, 12.9F + i * 0.7F, -0.05F, s * 0.95F + 0.25F, 13.2F + i * 0.7F, 0.15F, 0.0F, 0.0F, 1.0F, 1.0F);
         }
      }

      g.quad(
         "strings",
         new float[]{-0.45F, 12.5F, 0.42F},
         new float[]{0.45F, 12.5F, 0.42F},
         new float[]{0.45F, -3.5F, 0.85F},
         new float[]{-0.45F, -3.5F, 0.85F},
         new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
      );
      g.color(-15066594);
      g.box("plastic", -1.3F, -3.9F, 0.6F, 1.3F, -3.3F, 0.95F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.color(-14018030);
      g.push();
      g.rotZ(-40.0F);
      g.box("leather_dark", -0.6F, -9.0F, 2.05F, 0.6F, 3.0F, 2.35F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.box("leather_dark", -0.6F, -9.0F, -2.35F, 0.6F, 3.0F, -2.05F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.color(-1);
   }

   static void jetpack(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, -1.5F, -2.1F);
      g.color(-12960184);
      g.box("steel", -3.4F, -8.5F, -1.6F, 3.4F, -0.8F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 2.1F, -1.5F, -2.9F);
         g.color(-1512206);
         g.cylinder("steel", 1.85F, 1.85F, -7.5F, 0.0F, 14, false, 0.0F, 0.0F, 1.0F, 1.0F);
         g.push();
         g.scale(1.0F, 0.6F, 1.0F);
         g.sphere("steel", 1.85F, 14, 6, 0.0F, 0.5F, 1.0F, 1.0F);
         g.pop();
         g.color(-1900533);
         g.cylinder("plastic", 1.9F, 1.9F, -2.2F, -1.3F, 14, false, 0.0F, 0.0F, 1.0F, 1.0F);
         g.color(-14012874);
         g.cylinder("iron", 1.3F, 0.8F, -9.2F, -7.5F, 12, false, 0.0F, 0.0F, 1.0F, 1.0F);
         float fl = 1.0F + 0.25F * Geo.sin(a.time * 22.0F + s) + a.move * 0.6F;
         g.glow(true);
         g.color(-1);
         g.cross("flame", 0.0F, -9.2F - 2.6F * fl, 0.0F, 1.9F, 5.2F * fl, 0.0F, 1.0F, 1.0F, 0.0F);
         g.color(-1711276033);
         g.cross("orb", 0.0F, -9.4F, 0.0F, 3.2F, 3.2F, 0.0F, 0.0F, 1.0F, 1.0F);

         for (int i = 0; i < 3; i++) {
            float ph = (a.time * 2.2F + i / 3.0F + (s > 0 ? 0.5F : 0.0F)) % 1.0F;
            g.color(Geo.argb((int)(140.0F * (1.0F - ph)), 16777215));
            Geo.sprite(g, "orb_purple", Geo.sin(i * 2.1 + a.time) * ph * 1.5F, -11.0F - ph * 7.0F * fl, Geo.cos(i * 2.1) * ph, 1.5F + ph * 3.0F);
         }

         g.glow(false);
         g.pop();
      }

      g.color(-1);

      for (int s = -1; s <= 1; s += 2) {
         g.color(-14012874);
         g.box("leather_dark", s * 2.2F - 0.5F, -7.0F, 2.0F, s * 2.2F + 0.5F, 0.35F, 2.3F, 0.0F, 0.0F, 1.0F, 1.0F);
         g.box("leather_dark", s * 2.2F - 0.5F, 0.0F, -2.1F, s * 2.2F + 0.5F, 0.35F, 2.3F, 0.0F, 0.0F, 1.0F, 1.0F);
      }

      g.color(-1);
      g.pop();
   }

   static void shield(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, -6.2F, -2.5F);
      g.rotZ(8.0F);
      g.push();
      g.rotX(90.0F);
      g.color(-6643024);
      g.cylinder("steel", 6.2F, 6.2F, -0.2F, 0.6F, 28, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1);

      for (int i = 0; i < 28; i++) {
         double a0 = (Math.PI * 2) * i / 28.0;
         double a1 = (Math.PI * 2) * (i + 1) / 28.0;

         for (float yy : new float[]{-0.2F, 0.6F}) {
            Geo.tri(
               g,
               "shield_face",
               new float[]{0.0F, yy, 0.0F},
               new float[]{Geo.cos(a0) * 6.2F, yy, Geo.sin(a0) * 6.2F},
               new float[]{Geo.cos(a1) * 6.2F, yy, Geo.sin(a1) * 6.2F},
               new float[]{0.5F, 0.5F},
               new float[]{0.5F + 0.5F * Geo.cos(a0), 0.5F + 0.5F * Geo.sin(a0)},
               new float[]{0.5F + 0.5F * Geo.cos(a1), 0.5F + 0.5F * Geo.sin(a1)}
            );
         }
      }

      g.color(-2564892);
      g.torus("steel", 6.2F, 0.5F, 28, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-8054);
      g.translate(0.0F, -0.8F, 0.0F);
      g.sphere("gold", 1.3F, 10, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.color(-1);
      g.pop();
   }

   static void quiver(G g, Cos.A a) {
      g.push();
      g.translate(1.5F, -5.5F, -3.4F);
      g.rotZ(-24.0F);
      g.color(-1);
      g.cylinder("leather", 1.8F, 1.6F, -6.0F, 5.0F, 14, true, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-8054);
      g.cylinder("gold", 1.9F, 1.9F, 4.0F, 4.8F, 14, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.cylinder("gold", 1.7F, 1.7F, -5.2F, -4.5F, 14, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1);

      for (int i = 0; i < 5; i++) {
         float ox = -0.9F + i % 3 * 0.9F;
         float oz = -0.5F + i / 3 * 1.0F;
         float h = 7.4F + i % 2 * 0.8F;
         g.push();
         g.translate(ox, 0.0F, oz);
         g.rotZ((i - 2) * 3);
         g.box("wood", -0.12F, 2.0F, -0.12F, 0.12F, h, 0.12F, 0.0F, 0.0F, 1.0F, 1.0F);
         g.plane("arrow_fletch", 0.0F, h - 0.6F, 1.2F, 2.4F, 0.0F, 0.0F, 1.0F, 1.0F);
         g.rotY(90.0F);
         g.plane("arrow_fletch", 0.0F, h - 0.6F, 1.2F, 2.4F, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      g.pop();
      g.color(-12966376);
      g.push();
      g.rotZ(38.0F);
      g.box("leather_dark", -0.5F, -9.0F, 2.05F, 0.5F, 3.0F, 2.3F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.color(-1);
   }

   static void rocket(G g, Cos.A a) {
      g.push();
      float shake = a.move * 0.15F * Geo.sin(a.time * 40.0F);
      g.translate(shake, -4.0F + 0.2F * Geo.sin(a.time * 2.0F), -4.6F);
      g.cylinder("rocket_body", 2.2F, 2.2F, -6.0F, 3.0F, 16, false, 0.0F, 1.0F, 1.0F, 0.0F);
      g.color(-1900533);
      g.cylinder("plastic", 2.2F, 0.05F, 3.0F, 7.0F, 16, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1);
      g.push();
      g.translate(0.0F, 0.8F, -2.18F);
      g.rotY(180.0F);
      g.plane("window", 0.0F, 0.0F, 2.2F, 2.2F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.color(-1900533);

      for (int i = 0; i < 4; i++) {
         g.push();
         g.rotY(45 + i * 90);
         g.quad(
            "plastic",
            new float[]{0.0F, -2.5F, 2.1F},
            new float[]{0.0F, -6.8F, 2.1F},
            new float[]{0.0F, -7.5F, 4.2F},
            new float[]{0.0F, -4.8F, 4.0F},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
         g.pop();
      }

      g.color(-12960184);
      g.cylinder("iron", 1.3F, 1.6F, -7.2F, -6.0F, 12, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.glow(true);
      float fl = 0.8F + 0.2F * Geo.sin(a.time * 25.0F) + a.move * 0.8F;
      g.color(-1);
      g.cross("flame", 0.0F, -7.2F - 2.4F * fl, 0.0F, 2.2F, 4.8F * fl, 0.0F, 1.0F, 1.0F, 0.0F);
      g.color(-1996488705);
      g.cross("orb", 0.0F, -7.4F, 0.0F, 3.6F, 3.6F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.glow(false).color(-1);
      g.pop();
   }

   static void teddy(G g, Cos.A a) {
      g.push();
      g.translate(0.8F, -4.2F, -4.2F);
      g.rotZ(Geo.sin(a.time * 1.6) * 4.0F + a.move * Geo.sin(a.time * 8.0F) * 6.0F);
      g.rotY(180.0F);
      Geo.ellipsoid(g, "fur_brown", 0.0F, -2.2F, 0.0F, 2.4F, 2.8F, 1.9F, 12, 8);
      g.color(-1521512);
      Geo.ellipsoid(g, "fur_brown", 0.0F, -2.4F, 1.1F, 1.5F, 1.8F, 0.9F, 10, 6);
      g.color(-1);
      Geo.ellipsoid(g, "fur_brown", 0.0F, 1.8F, 0.0F, 2.3F, 2.1F, 2.0F, 14, 9);
      g.push();
      g.translate(0.0F, 1.6F, 2.02F);
      g.plane("teddy_face", 0.0F, 0.0F, 3.4F, 3.4F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();

      for (int s = -1; s <= 1; s += 2) {
         Geo.ellipsoid(g, "fur_brown", s * 1.7F, 3.6F, 0.0F, 0.9F, 0.9F, 0.55F, 10, 6);
         Geo.ellipsoid(g, "fur_brown", s * 2.4F, -1.4F, 0.4F, 0.8F, 1.5F, 0.8F, 8, 6);
         Geo.ellipsoid(g, "fur_brown", s * 1.3F, -4.8F, 0.6F, 0.9F, 0.8F, 1.1F, 8, 6);
      }

      g.color(-2080678);
      g.push();
      g.translate(0.0F, -0.2F, 1.6F);
      g.box("satin", -1.0F, -0.35F, 0.0F, 1.0F, 0.35F, 0.5F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.color(-1);
      g.pop();
   }

   static void balloon(G g, Cos.A a) {
      float t = a.time;
      float bx = 6.0F + Geo.sin(t * 0.9) * 1.0F;
      float by = 1.0F + Geo.sin(t * 1.3) * 0.8F;
      float bz = -6.0F + Geo.cos(t * 0.7) * 1.0F;
      float[] hand = new float[]{2.5F, -9.0F, -2.3F};
      int n = 10;
      float[][] pts = new float[n][];
      float[] rad = new float[n];

      for (int i = 0; i < n; i++) {
         float k = (float)i / (n - 1);
         pts[i] = new float[]{
            Geo.lerp(hand[0], bx, k) + Geo.sin(k * Math.PI) * 0.8F * Geo.sin(t * 2.0F), Geo.lerp(hand[1], by - 3.4F, k), Geo.lerp(hand[2], bz, k)
         };
         rad[i] = 0.06F;
      }

      g.color(-723724);
      Geo.chain(g, "white", pts, rad, 3);
      g.push();
      g.translate(bx, by, bz);
      g.rotZ(Geo.sin(t * 1.1) * 8.0F);
      g.rotY(t * 25.0F);
      g.color(-54694);
      Geo.ellipsoid(g, "gloss", -1.3F, 0.6F, 0.0F, 1.9F, 1.9F, 1.6F, 16, 10);
      Geo.ellipsoid(g, "gloss", 1.3F, 0.6F, 0.0F, 1.9F, 1.9F, 1.6F, 16, 10);
      g.push();
      g.rotZ(180.0F);
      g.translate(0.0F, -0.2F, 0.0F);
      g.scale(1.0F, 1.0F, 0.8F);
      g.cylinder("gloss", 2.85F, 0.2F, 0.0F, 3.4F, 14, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.color(-2092998);
      g.cylinder("plastic", 0.3F, 0.45F, -3.6F, -3.1F, 6, true, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1);
      g.pop();
   }
}
