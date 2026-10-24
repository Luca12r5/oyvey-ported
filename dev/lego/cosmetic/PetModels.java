package dev.lego.cosmetic;

final class PetModels {
   private PetModels() {
   }

   static void cat(G g, Cos.A a) {
      Pets.Q4 q = new Pets.Q4();
      q.body = Pets.fur("cat");
      q.head = Pets.head("cat");
      q.leg = Pets.col(16773602);
      q.style = "cat";
      q.nose = 16748456;
      q.noseR = 0.26F;
      q.ears = (gg, p) -> {
         for (int s = -1; s <= 1; s += 2) {
            Pets.earPointy(gg, p, Pets.col(16229457), Pets.col(16757702), s * 1.85F, 2.05F, -0.25F, s, 1.08F, 1.95F, 10.0F);
         }
      };
      q.tail = (gg, p) -> Pets.tail(
         gg,
         p,
         new String[]{Pets.col(16229457), Pets.col(16229457), Pets.col(15239740), Pets.col(14251051)},
         new float[]{0.47F, 0.44F, 0.41F, 0.39F},
         1.15F,
         19.0F,
         30.0F,
         1.0F
      );
      Pets.quad4(g, Pets.pose(a), q);
   }

   static void shiba(G g, Cos.A a) {
      Pets.Q4 q = new Pets.Q4();
      q.body = Pets.fur("shiba");
      q.head = Pets.head("shiba");
      q.leg = Pets.col(16775148);
      q.style = "dog";
      q.nose = 2760740;
      q.noseR = 0.34F;
      q.ears = (gg, p) -> {
         for (int s = -1; s <= 1; s += 2) {
            Pets.earPointy(gg, p, Pets.col(15636296), Pets.col(16773604), s * 1.8F, 2.0F, -0.2F, s, 1.12F, 1.7F, 9.0F);
         }
      };
      q.tail = (gg, p) -> Pets.tail(
         gg,
         p,
         new String[]{Pets.col(15636296), Pets.col(15636296), Pets.col(16035952), Pets.col(16775148)},
         new float[]{0.72F, 0.68F, 0.6F, 0.48F},
         1.0F,
         44.0F,
         62.0F,
         0.8F
      );
      Pets.quad4(g, Pets.pose(a), q);
   }

   static void fox(G g, Cos.A a) {
      Pets.Q4 q = new Pets.Q4();
      q.body = Pets.fur("fox");
      q.head = Pets.head("fox");
      q.leg = Pets.col(4860450);
      q.style = "amber";
      q.nose = 2760740;
      q.noseR = 0.3F;
      q.headRx = 3.45F;
      q.ears = (gg, p) -> {
         for (int s = -1; s <= 1; s += 2) {
            Pets.earPointy(gg, p, Pets.col(16221754), Pets.col(16774120), Pets.col(4860450), s * 1.9F, 2.0F, -0.3F, s, 1.25F, 2.35F, 14.0F);
         }
      };
      q.tail = (gg, p) -> Pets.tail(
         gg,
         p,
         new String[]{Pets.col(16221754), Pets.col(16221754), Pets.col(16221754), Pets.col(16774892)},
         new float[]{0.85F, 1.12F, 1.12F, 0.8F},
         1.25F,
         12.0F,
         18.0F,
         1.0F
      );
      Pets.quad4(g, Pets.pose(a), q);
   }

   static void redPanda(G g, Cos.A a) {
      Pets.Q4 q = new Pets.Q4();
      q.body = Pets.fur("redpanda");
      q.head = Pets.head("redpanda");
      q.leg = Pets.col(4858904);
      q.style = "std";
      q.nose = 2759192;
      q.noseR = 0.3F;
      q.headRx = 3.5F;
      q.ears = (gg, p) -> {
         for (int s = -1; s <= 1; s += 2) {
            Pets.earPointy(gg, p, Pets.col(13852718), Pets.col(16774890), s * 2.0F, 1.9F, -0.3F, s, 1.2F, 1.4F, 22.0F);
         }
      };
      String r = Pets.col(13852718);
      String d = Pets.col(8008728);
      q.tail = (gg, p) -> Pets.tail(gg, p, new String[]{r, d, r, d, r}, new float[]{0.75F, 0.84F, 0.86F, 0.78F, 0.62F}, 1.0F, 9.0F, 16.0F, 1.0F);
      Pets.quad4(g, Pets.pose(a), q);
   }

   static void panda(G g, Cos.A a) {
      Pets.Q4 q = new Pets.Q4();
      q.body = Pets.fur("panda");
      q.head = Pets.head("panda");
      q.leg = Pets.fur("pdark");
      q.style = "rim";
      q.nose = 2763315;
      q.noseR = 0.32F;
      q.bodyRx = 2.4F;
      q.headRx = 3.45F;
      q.ears = (gg, p) -> {
         for (int s = -1; s <= 1; s += 2) {
            Pets.earRound(gg, p, Pets.fur("pdark"), null, s * 2.25F, 1.95F, -0.3F, s, 1.0F);
         }
      };
      q.tail = (gg, p) -> PetGeo.blob(gg, Pets.col(16514039), 0.0F, 0.4F, 0.0F, 0.6F, 0.6F, 0.5F, 7, 4);
      Pets.quad4(g, Pets.pose(a), q);
   }

   static void bear(G g, Cos.A a) {
      Pets.Q4 q = new Pets.Q4();
      q.body = Pets.fur("bear");
      q.head = Pets.head("bear");
      q.leg = Pets.col(10381888);
      q.style = "std";
      q.nose = 4860446;
      q.noseR = 0.36F;
      q.bodyRx = 2.4F;
      q.headRx = 3.4F;
      q.ears = (gg, p) -> {
         for (int s = -1; s <= 1; s += 2) {
            Pets.earRound(gg, p, Pets.col(12157006), Pets.col(15782568), s * 2.25F, 1.9F, -0.25F, s, 0.95F);
         }
      };
      q.tail = (gg, p) -> PetGeo.blob(gg, Pets.col(12157006), 0.0F, 0.3F, 0.0F, 0.55F, 0.55F, 0.5F, 7, 4);
      Pets.quad4(g, Pets.pose(a), q);
   }

   static void hamster(G g, Cos.A a) {
      Pets.Q4 q = new Pets.Q4();
      q.body = Pets.fur("hamster");
      q.head = Pets.head("hamster");
      q.leg = Pets.col(16770262);
      q.style = "std";
      q.nose = 16748456;
      q.noseR = 0.24F;
      q.bodyY = 2.3F;
      q.bodyRx = 2.7F;
      q.bodyRy = 2.25F;
      q.bodyRz = 2.5F;
      q.headY = 5.25F;
      q.headZ = 0.75F;
      q.headRx = 3.7F;
      q.headRy = 2.85F;
      q.headRz = 2.85F;
      q.legR = 0.55F;
      q.legH = 0.8F;
      q.legX = 1.4F;
      q.span = 0.95F;
      q.ears = (gg, p) -> {
         for (int s = -1; s <= 1; s += 2) {
            Pets.earRound(gg, p, Pets.col(15773796), Pets.col(16757702), s * 2.1F, 2.0F, -0.4F, s, 0.78F);
         }
      };
      q.bodyExtra = (gg, p) -> {
         gg.push();
         gg.translate(0.0F, 0.55F, q.bodyRz + 0.05F);
         gg.rotX(-10.0F);
         PetGeo.blob(gg, "pet_berry", 0.0F, 0.0F, 0.1F, 0.62F, 0.7F, 0.5F, 6, 4);
         gg.push();
         gg.translate(0.0F, 0.55F, 0.1F);
         gg.scale(1.0F, 0.5F, 1.0F);
         PetGeo.cone(gg, Pets.col(6079578), 0.5F, 0.6F, 6, false);
         gg.pop();

         for (int s = -1; s <= 1; s += 2) {
            PetGeo.blob(gg, Pets.col(16770262), s * 0.62F, -0.05F, 0.15F, 0.36F, 0.32F, 0.32F, 6, 3);
         }

         gg.pop();
      };
      q.tail = (gg, p) -> PetGeo.blob(gg, Pets.col(15773796), 0.0F, 0.3F, 0.0F, 0.4F, 0.4F, 0.4F, 6, 4);
      Pets.quad4(g, Pets.pose(a), q);
   }

   static void bunny(G g, Cos.A a) {
      Pets.P pp = Pets.pose(a);
      float[] h = PetRig.hop(pp);
      pp.y = pp.y + h[0] * (2.2F + pp.gr * 1.5F);
      pp.sy = pp.sy * (1.0F + h[1] * 0.14F);
      pp.sx = pp.sx * (1.0F - h[1] * 0.07F);
      pp.pitch = h[2] * 14.0F - Math.max(0.0F, -h[1]) * 6.0F;
      pp.walkW = 0.0F;
      pp.runW = 0.0F;
      pp.bob = 0.0F;
      Pets.Q4 q = new Pets.Q4();
      q.body = Pets.fur("bunny");
      q.head = Pets.head("bunny");
      q.leg = Pets.col(16776181);
      q.style = "std";
      q.nose = 16748456;
      q.noseR = 0.24F;
      q.bodyRx = 2.4F;
      q.bodyRy = 2.1F;
      q.headY = 5.9F;
      q.ears = (gg, p) -> {
         for (int s = -1; s <= 1; s += 2) {
            gg.push();
            gg.translate(s * 1.15F, 2.2F, -0.45F);
            float droop = Math.max(0.0F, 1.0F - p.ear);
            gg.rotZ(-s * (7.0F + droop * 72.0F + p.earTwitch * (s > 0 ? 1 : 0)) + PetGeo.sin(p.t * 1.6F + s) * 2.0F);
            gg.rotX(-8.0F - droop * 42.0F + (p.ear > 1.0F ? 6 : 0));
            gg.scale(1.0F, 1.0F + p.gr * 0.15F, 1.0F);
            PetGeo.blob(gg, Pets.col(15850694), 0.0F, 2.0F, 0.0F, 0.78F, 2.35F, 0.44F, 8, 6);
            PetGeo.blob(gg, Pets.col(16758728), 0.0F, 2.0F, 0.24F, 0.46F, 1.85F, 0.24F, 6, 4);
            gg.pop();
         }
      };
      q.tail = (gg, p) -> PetGeo.blob(gg, Pets.col(16777215), 0.0F, 0.5F, 0.0F, 0.8F, 0.8F, 0.7F, 7, 4);
      Pets.quad4(g, pp, q);
   }

   static void unicorn(G g, Cos.A a) {
      Pets.Q4 q = new Pets.Q4();
      q.body = Pets.fur("unicorn");
      q.head = Pets.head("unicorn");
      q.leg = Pets.col(16774399);
      q.style = "gem";
      q.legH = 1.55F;
      q.legR = 0.62F;
      q.bodyY = 2.95F;
      q.headY = 6.45F;
      q.headZ = 1.1F;
      q.ears = (gg, p) -> {
         for (int s = -1; s <= 1; s += 2) {
            Pets.earPointy(gg, p, Pets.col(16776191), Pets.col(16763108), s * 1.95F, 1.95F, -0.45F, s, 0.8F, 1.4F, 18.0F);
         }
      };
      int[] rainbow = new int[]{16751560, 16763274, 16773280, 11071680, 10277631, 13150463};
      q.headExtra = (gg, p) -> {
         gg.push();
         gg.translate(0.0F, 2.55F, 0.95F);
         gg.rotX(18.0F);
         gg.glow(true);
         PetGeo.cone(gg, "pet_horn", 0.5F + p.gr * 0.15F, 2.3F + p.gr * 1.6F + (p.lvl >= 5 ? 0.6F : 0.0F), 8, false);
         gg.glow(false);
         gg.pop();

         for (int i = 0; i < 4; i++) {
            double an = Math.toRadians(84 + i * 34);
            gg.push();
            gg.translate(-0.12F + i % 2 * 0.24F, PetGeo.sin(an) * 2.8F, PetGeo.cos(an) * 2.65F - 0.1F);
            gg.rotX((float)(90.0 - Math.toDegrees(an)));
            gg.rotZ(PetGeo.sin(p.t * 2.0F + i) * 4.0F);
            PetGeo.blob(gg, Pets.col(rainbow[i + 1]), 0.0F, 0.1F, 0.0F, 1.1F, 0.6F, 1.35F, 6, 3);
            gg.pop();
         }

         gg.push();
         gg.translate(0.75F, 2.35F, 1.25F);
         gg.rotZ(-35.0F);
         PetGeo.blob(gg, Pets.col(rainbow[0]), 0.0F, 0.0F, 0.0F, 0.75F, 0.55F, 0.6F, 7, 4);
         gg.pop();
      };
      q.tail = (gg, p) -> Pets.tail(gg, p, new String[]{Pets.col(rainbow[0]), Pets.col(rainbow[4])}, new float[]{0.7F, 0.8F}, 1.3F, -10.0F, 30.0F, 0.8F);
      Pets.quad4(g, Pets.pose(a), q);
   }

   static void miniDragon(G g, Cos.A a) {
      Pets.P pp = Pets.pose(a);
      Pets.Q4 q = new Pets.Q4();
      q.body = Pets.fur("dragon");
      q.head = Pets.head("dragon");
      q.leg = Pets.col(10125548);
      q.style = "amber";
      q.nose = 8611808;
      q.noseR = 0.16F;
      q.noseEl = -0.1F;
      q.ears = (gg, p) -> {
         for (int s = -1; s <= 1; s += 2) {
            gg.push();
            gg.translate(s * 1.3F, 2.3F, -0.5F);
            gg.rotZ(-s * 18);
            gg.rotX(-30.0F);
            PetGeo.cone(gg, Pets.col(16771504), 0.45F, 1.5F, 8, false);
            gg.pop();
            Pets.earPointy(gg, p, Pets.col(11440887), Pets.col(16763368), s * 2.5F, 1.2F, -0.4F, s, 0.7F, 1.3F, 55.0F);
         }
      };
      q.bodyExtra = (gg, p) -> {
         float flap = p.flap * (p.is("sleepy") ? 6.0F : 26.0F + 20.0F * p.runW);
         float ws = 1.0F + p.gr * 0.5F + (p.lvl >= 5 ? 0.2F : 0.0F);

         for (int s = -1; s <= 1; s += 2) {
            gg.push();
            gg.translate(s * 0.9F, 1.45F, -0.5F);
            gg.scale(s * ws, ws, ws);
            gg.rotY(30.0F);
            gg.rotZ(22.0F + flap);
            gg.planeTL("pet_dragon_wing", 4.2F, 3.2F, 0.0F, 0.0F, 1.0F, 1.0F);
            gg.pop();
         }

         for (int i = 0; i < 3; i++) {
            gg.push();
            gg.translate(0.0F, 1.85F - i * 0.25F, -0.4F - i * 0.9F);
            gg.rotX(-20 - i * 10);
            gg.scale(0.5F, 1.0F, 1.0F);
            PetGeo.cone(gg, Pets.col(16766632), 0.45F, 0.8F, 6, false);
            gg.pop();
         }
      };
      q.tail = (gg, p) -> Pets.tail(
         gg,
         p,
         new String[]{Pets.col(11440887), Pets.col(11440887), Pets.col(10125548), Pets.col(16757722)},
         new float[]{0.7F, 0.56F, 0.44F, 0.4F},
         1.1F,
         6.0F,
         8.0F,
         1.2F
      );
      Pets.quad4(g, pp, q);
   }

   private static void beak(G g, float y, float z, float s, float open, int top, int bot) {
      g.push();
      g.translate(0.0F, y, z);
      g.push();
      g.rotX(90.0F - open * 18.0F);
      g.scale(1.0F, 1.0F, 0.62F);
      PetGeo.cone(g, Pets.gl(top), 0.62F * s, 1.0F * s, 8, true);
      g.pop();
      g.push();
      g.translate(0.0F, -0.12F * s, 0.0F);
      g.rotX(90.0F + open * 26.0F);
      g.scale(1.0F, 1.0F, 0.5F);
      PetGeo.cone(g, Pets.gl(bot), 0.5F * s, 0.72F * s, 8, true);
      g.pop();
      g.pop();
   }

   private static float beakOpen(Pets.P p) {
      String var1 = p.emo;
      switch (var1) {
         case "excited":
            return 0.6F + 0.4F * Math.abs(PetGeo.sin(p.t * 7.0F));
         case "happy":
            return 0.45F;
         case "angry":
            return 0.25F + 0.2F * Math.abs(PetGeo.sin(p.t * 12.0F));
         case "curious":
            return 0.2F;
         default:
            return 0.05F;
      }
   }

   private static float wingFlap(Pets.P p) {
      String var1 = p.emo;
      switch (var1) {
         case "excited":
            return 55.0F;
         case "happy":
            return 22.0F;
         case "love":
            return 14.0F;
         case "angry":
            return 30.0F;
         case "sad":
         case "sleepy":
            return 3.0F;
         default:
            return 10.0F;
      }
   }

   private static void feet2(G g, Pets.P p, String tex, float x, float z, float r) {
      PetRig.feet(g, p, tex, x, z, r, 0.9F);
   }

   private static void birdLegs(G g, Pets.P p, String tex, float x, float h) {
      if (!(h < 0.2F)) {
         for (int s = -1; s <= 1; s += 2) {
            float c = PetRig.frac(p.t * 2.1F + (s > 0 ? 0.0F : 0.5F));
            float lift = p.walkW * Math.max(0.0F, PetGeo.sin((Math.PI * 2) * c)) * 0.45F;
            g.push();
            g.translate(s * x, 0.3F + lift, 0.3F);
            g.cylinder(tex, 0.2F, 0.16F, 0.0F, h + 0.2F, 5, false, 0.0F, 0.0F, 1.0F, 1.0F);
            g.pop();
         }
      }
   }

   static void chick(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      float k = p.gr;
      g.push();
      Pets.root(g, p);
      g.translate(0.0F, p.waddleBob, 0.0F);
      float legH = k * 1.3F;
      feet2(g, p, Pets.gl(16753724), 1.1F, 0.4F, 0.55F + k * 0.1F);
      birdLegs(g, p, Pets.gl(16753724), 1.0F, legH);
      float by = 3.15F + legH - p.lie * 0.5F;
      float rx = 3.0F;
      float ry = 2.85F + k * 0.2F;
      float rz = 2.85F + k * 0.6F;
      g.push();
      g.translate(0.0F, by, 0.0F);
      g.rotZ(p.waddle);
      g.rotY(p.waddle * 0.4F);
      float fl = wingFlap(p);
      float fs = p.is("excited") ? 18.0F : 9.0F;

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 2.7F, 0.1F, -0.2F);
         g.rotZ(s * (12.0F + fl * (0.5F + 0.5F * PetGeo.sin(p.t * fs))) + (p.is("sad") ? -s * 10 : 0) + s * p.walkW * 10.0F * Math.abs(PetGeo.sin(p.t * 9.0F)));
         PetGeo.blob(g, Pets.col(16767050), s * 0.1F, -0.9F, 0.0F, 0.45F, 1.2F + k * 0.3F, 0.95F + k * 0.5F, 7, 4);
         g.pop();
      }

      g.push();
      g.translate(0.0F, 0.3F + k * 0.8F, -rz + 0.2F);
      g.rotX(-30.0F - k * 25.0F + p.tailSway * 0.5F);
      PetGeo.blob(g, Pets.col(k > 0.5F ? 16762938 : 16767050), 0.0F, 0.0F, -0.3F, 0.8F + k * 0.3F, 0.5F + k * 0.9F, 0.6F + k * 0.4F, 7, 4);
      g.pop();
      g.rotY(p.headYaw * 0.6F);
      g.rotX(p.headPitch * 0.5F + p.walkW * 6.0F * PetGeo.sin(p.t * Math.PI * 2.0 * 2.1F * 2.0));
      g.rotZ(p.headRoll);
      PetGeo.blob(g, Pets.fur("chick"), 0.0F, 0.0F, 0.0F, rx, ry, rz, 16, 11);
      Pets.face(g, p, PetRig.style("beak", k), 0.0F, 0.0F, 0.0F, rx, ry, rz, 0.95F, 0.28F);
      beak(g, PetGeo.sin(0.16F) * ry, PetGeo.cos(0.16F) * rz * 0.95F, 0.85F, beakOpen(p), 16753724, 15764010);
      if (k > 0.35F) {
         float m = Math.min(1.0F, (k - 0.35F) / 0.45F);

         for (int i = -1; i <= 1; i++) {
            PetGeo.blob(
               g,
               Pets.gl(15216698),
               0.0F,
               ry - 0.1F + (1 - Math.abs(i)) * 0.35F * m,
               0.9F + i * 0.7F,
               0.25F * m + 0.05F,
               0.55F * m + 0.05F,
               0.4F * m + 0.05F,
               5,
               3
            );
         }

         PetGeo.blob(g, Pets.gl(15216698), 0.0F, -0.6F, rz * 0.95F + 0.15F, 0.25F * m, 0.45F * m, 0.2F * m, 5, 3);
      } else {
         for (int i = -1; i <= 1; i++) {
            g.push();
            g.translate(i * 0.3F, ry - 0.2F, 0.2F);
            g.rotZ(-i * 28 + PetGeo.sin(p.t * 3.0F) * 6.0F);
            PetGeo.blob(g, Pets.col(16770154), 0.0F, 0.55F, 0.0F, 0.26F, 0.7F, 0.26F, 6, 4);
            g.pop();
         }
      }

      g.pop();
      Pets.fx(g, p, by + ry + 1.5F);
      g.pop();
   }

   static void penguin(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      float k = p.gr;
      g.push();
      Pets.root(g, p);
      g.translate(0.0F, p.waddleBob * 0.7F, 0.0F);
      feet2(g, p, Pets.gl(16753724), 1.2F, 0.3F, 0.6F + k * 0.15F);
      float by = 3.55F + k * 1.1F - p.lie * 0.6F;
      float rx = 2.75F;
      float ry = 3.3F + k * 1.3F;
      float rz = 2.55F;
      g.push();
      g.translate(0.0F, by, 0.0F);
      g.rotZ(p.waddle * 1.2F);
      g.rotY(p.waddle * 0.5F);
      float fl = wingFlap(p);
      float fs = p.is("excited") ? 16.0F : 7.0F;

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 2.45F, 0.5F + k * 0.5F, -0.1F);
         g.rotZ(s * (14.0F + fl * (0.5F + 0.5F * PetGeo.sin(p.t * fs)) + p.walkW * 18.0F) - p.waddle * 0.8F + (p.is("sad") ? -s * 8 : 0));
         PetGeo.blob(g, Pets.col(3424874), s * 0.15F, -1.2F - k * 0.4F, 0.0F, 0.42F, 1.6F + k * 0.6F, 0.9F, 7, 4);
         g.pop();
      }

      g.rotY(p.headYaw * 0.6F);
      g.rotX(p.headPitch * 0.4F);
      g.rotZ(p.headRoll * 0.8F);
      PetGeo.blob(g, Pets.fur("penguin"), 0.0F, 0.0F, 0.0F, rx, ry, rz, 18, 12);
      Pets.face(g, p, PetRig.style("beak", k), 0.0F, (ry - 3.3F) * 0.6F, 0.0F, rx, 3.3F, rz, 0.9F, 0.42F);
      beak(g, PetGeo.sin(0.3F) * 3.3F + (ry - 3.3F) * 0.6F, PetGeo.cos(0.3F) * rz * 0.95F, 0.85F + k * 0.2F, beakOpen(p), 16753724, 15764010);
      if (k > 0.4F) {
         float m = Math.min(1.0F, (k - 0.4F) / 0.4F);

         for (int s = -1; s <= 1; s += 2) {
            PetGeo.blob(g, Pets.gl(16761402), s * 1.9F, ry * 0.45F, 1.2F, 0.35F * m, 0.8F * m, 0.5F * m, 5, 3);
         }
      }

      g.pop();
      Pets.fx(g, p, by + ry + 1.2F);
      g.pop();
   }

   static void owl(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      float k = p.gr;
      float fly = PetRig.smooth(0.45F, 0.85F, p.sp);
      float[] fs2 = PetRig.flap(p, 2.4F);
      g.push();
      Pets.root(g, p);
      g.translate(0.0F, fly * (5.0F + fs2[1] * 0.8F) + p.waddleBob * (1.0F - fly), 0.0F);
      if (fly < 0.9F) {
         feet2(g, p, Pets.gl(15905354), 1.0F, 0.5F, 0.45F + k * 0.1F);
      }

      float by = 2.75F + k * 0.4F - p.lie * 0.4F;
      g.push();
      g.translate(0.0F, by, 0.0F);
      g.rotZ(p.waddle * (1.0F - fly));
      g.rotX(fly * 25.0F);
      PetGeo.blob(g, Pets.fur("owl"), 0.0F, 0.0F, 0.0F, 2.8F, 2.6F + k * 0.6F, 2.6F, 14, 8);
      float fl = wingFlap(p);
      float fs = p.is("excited") ? 16.0F : 8.0F;

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 2.5F, 1.0F, -0.3F);
         float ground = s * (8.0F + fl * (0.5F + 0.5F * PetGeo.sin(p.t * fs))) + (p.is("sad") ? -s * 6 : 0);
         float air = s * (70.0F + fs2[0] * 55.0F);
         g.rotZ(ground * (1.0F - fly) + air * fly);
         PetGeo.blob(g, Pets.col(9068608), s * 0.15F, -1.1F - k * 0.3F, 0.0F, 0.5F, 1.6F + k * 0.7F + fly * 0.6F, 1.3F + k * 0.3F, 7, 4);
         g.pop();
      }

      g.pop();
      float hs = 1.0F - k * 0.12F;
      g.push();
      g.translate(0.0F, 5.75F + k * 0.7F - p.lie * 0.7F, 0.25F);
      Pets.headTurn(g, p);
      g.scale(hs);
      float rx = 3.2F;
      float ry = 2.6F;
      float rz = 2.75F;
      PetGeo.blob(g, Pets.head("owl"), 0.0F, 0.0F, 0.0F, rx, ry, rz, 17, 11);
      Pets.face(g, p, PetRig.style("owl", k), 0.0F, 0.0F, 0.0F, rx, ry, rz, 1.0F, 0.0F);
      g.push();
      g.translate(0.0F, -0.25F, rz * 0.96F);
      g.rotX(150.0F);
      g.scale(1.0F, 1.0F, 0.7F);
      PetGeo.cone(g, Pets.gl(15905354), 0.42F, 1.0F, 8, true);
      g.pop();

      for (int s = -1; s <= 1; s += 2) {
         Pets.earPointy(g, p, Pets.col(9068608), Pets.col(10975311), s * 2.1F, 1.7F, -0.2F, s, 0.75F + k * 0.15F, 1.6F + k * 0.9F, 28.0F);
      }

      g.pop();
      Pets.fx(g, p, 5.75F + k * 0.7F + 2.6F * hs + 1.4F);
      g.pop();
   }

   static void frog(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      float[] h = PetRig.hop(p);
      p.y = p.y + h[0] * 2.4F;
      p.sy = p.sy * (1.0F + h[1] * 0.12F);
      p.sx = p.sx * (1.0F - h[1] * 0.06F);
      p.stepBob = 0.0F;
      float air = h[2];
      g.push();
      Pets.root(g, p);
      g.rotX(-air * 18.0F);
      String green = Pets.col(8835424);
      String light = Pets.col(10936446);
      float squat = p.lie * 0.4F + Math.max(0.0F, -h[1]) * 0.35F;

      for (int s = -1; s <= 1; s += 2) {
         PetGeo.blob(g, green, s * 2.15F, 1.05F - squat - air * 0.3F, -0.9F - air * 1.0F, 1.05F - air * 0.2F, 0.95F - air * 0.25F, 1.5F + air * 0.9F, 7, 4);
         PetGeo.blob(g, light, s * 2.45F, 0.22F + air * 0.1F, 0.35F - air * 2.6F, 0.85F, 0.24F, 1.15F, 8, 4);
         g.push();
         g.translate(s * 1.3F, 1.4F - squat, 1.35F);
         g.rotX(s * p.walk * 0.6F);
         PetGeo.blob(g, green, 0.0F, -0.6F, 0.0F, 0.42F, 0.8F, 0.42F, 6, 3);
         PetGeo.blob(g, light, 0.0F, -1.25F, 0.2F, 0.55F, 0.18F, 0.55F, 5, 3);
         g.pop();
      }

      PetGeo.blob(g, Pets.fur("frog"), 0.0F, 2.1F - squat, -0.3F, 2.6F, 2.0F, 2.45F, 11, 6);
      g.push();
      g.translate(0.0F, 4.25F - squat * 1.6F, 0.55F);
      Pets.headTurn(g, p);
      float rx = 3.5F;
      float ry = 2.2F;
      float rz = 2.85F;
      PetGeo.blob(g, Pets.head("frog"), 0.0F, 0.0F, 0.0F, rx, ry, rz, 15, 10);
      boolean og = g.glow;
      PetGeo.decal(g, "pet_face:blob:" + p.emo, 0.0F, 0.0F, 0.0F, rx, ry, rz, 0.0F, 0.28F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.035F);
      String eyes = Pets.eyes("blob", p);

      for (int s = -1; s <= 1; s += 2) {
         float bx = s * 1.55F;
         float byy = 1.65F;
         float bz = 0.5F;
         float r = 1.3F;
         PetGeo.blob(g, green, bx, byy, bz, r, r * 0.95F, r, 9, 6);
         float u0 = s > 0 ? 0.0546875F : 0.4453125F;
         PetGeo.decal(g, eyes, bx, byy, bz, r, r * 0.95F, r, p.lookX * 0.1F, 0.12F + p.lookY * 0.06F, 1.2F, u0, 0.104166664F, u0 + 0.5F, 0.7708333F, 1.05F);
      }

      g.glow(og);
      g.pop();
      Pets.fx(g, p, 7.2F);
      g.pop();
   }

   static void axolotl(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      g.push();
      Pets.root(g, p);
      String pink = Pets.col(16758990);

      for (int s = -1; s <= 1; s += 2) {
         for (int f = 0; f < 2; f++) {
            g.push();
            float z = f == 0 ? 0.9F : -2.1F;
            g.translate(s * 1.65F, 0.75F - p.lie * 0.3F, z);
            g.rotZ(-s * 35);
            g.rotX((f == 0 ? s : -s) * p.walk);
            PetGeo.blob(g, pink, 0.0F, -0.35F, 0.0F, 0.36F, 0.62F, 0.36F, 7, 4);
            g.pop();
         }
      }

      PetGeo.blob(g, Pets.fur("axolotl"), 0.0F, 1.6F - p.lie * 0.35F, -0.6F, 2.0F, 1.45F, 2.9F, 12, 7);
      g.push();
      g.translate(0.0F, 1.85F - p.lie * 0.35F, -3.0F);
      g.rotY(p.tail * 1.2F + PetGeo.sin(p.t * 3.0F) * 8.0F + PetGeo.sin(p.t * 9.0F) * 16.0F * p.walkW);
      PetGeo.blob(g, Pets.col(16763096), 0.0F, 0.1F, -1.5F, 0.32F, 1.05F, 1.9F, 8, 5);
      g.pop();
      g.push();
      g.translate(0.0F, 3.45F - p.lie * 0.6F, 1.35F);
      Pets.headTurn(g, p);
      float rx = 3.4F;
      float ry = 2.35F;
      float rz = 2.55F;
      PetGeo.blob(g, Pets.head("axolotl"), 0.0F, 0.0F, 0.0F, rx, ry, rz, 15, 10);
      Pets.face(g, p, "blob", 0.0F, 0.0F, 0.0F, rx, ry, rz, 1.05F, -0.02F);
      float droop = Math.max(0.0F, 1.0F - p.ear) * 30.0F;

      for (int s = -1; s <= 1; s += 2) {
         for (int i = 0; i < 3; i++) {
            g.push();
            g.translate(s * 2.75F, 1.0F - i * 0.8F, -0.6F);
            g.rotZ(-s * (62 - i * 42 + droop) + PetGeo.sin(p.t * 4.0F + i) * 7.0F);
            g.scale(1.0F, 1.0F + p.gr * 0.5F, 1.0F);
            PetGeo.blob(g, Pets.col(15890335), 0.0F, 0.9F, 0.0F, 0.32F, 1.05F, 0.28F, 6, 4);
            g.pop();
         }
      }

      g.pop();
      Pets.fx(g, p, 7.0F);
      g.pop();
   }

   static void turtle(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      g.push();
      Pets.root(g, p);
      String skin = Pets.col(10476418);
      Pets.legs4(g, p, skin, 2.0F, 1.6F, -1.6F, 0.72F, 1.05F);
      float drop = p.lie * 0.6F;
      PetGeo.blob2(g, "pet_shell", 0.0F, 1.95F - drop, -0.2F, 3.25F, 2.55F, 0.75F, 3.45F, 16, 10);
      g.push();
      g.translate(0.0F, 1.5F - drop, -3.4F);
      g.rotY(p.tail);
      g.rotX(-100.0F);
      PetGeo.cone(g, skin, 0.4F, 0.9F, 6, false);
      g.pop();
      g.push();
      g.translate(0.0F, 3.0F - drop, 3.45F - p.lie * 1.2F);
      Pets.headTurn(g, p);
      float rx = 2.65F;
      float ry = 2.3F;
      float rz = 2.3F;
      PetGeo.blob(g, Pets.head("turtle"), 0.0F, 0.0F, 0.0F, rx, ry, rz, 17, 11);
      Pets.face(g, p, "std", 0.0F, 0.0F, 0.0F, rx, ry, rz, 1.05F, 0.02F);
      g.pop();
      Pets.fx(g, p, 6.2F);
      g.pop();
   }

   static void bee(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      g.push();
      Pets.root(g, p);
      g.translate(0.0F, 4.3F * p.hover + PetGeo.sin(p.t * 2.6F) * 0.4F - p.flap * 0.18F, 0.0F);
      g.rotX(p.sp * 16.0F);
      g.push();
      g.translate(0.0F, -0.3F, -1.3F);
      g.rotX(-12.0F + PetGeo.sin(p.t * 2.0F) * 4.0F);
      PetGeo.blob(g, Pets.fur("bee"), 0.0F, 0.0F, -0.5F, 2.15F, 2.0F, 2.5F, 14, 8);
      g.push();
      g.translate(0.0F, -0.3F, -2.9F);
      g.rotX(-95.0F);
      PetGeo.cone(g, Pets.col(3811882), 0.35F, 0.8F, 6, false);
      g.pop();
      g.pop();
      float fl = p.flap * (p.is("sleepy") ? 10 : 34);
      int oc = g.color;
      g.color(-654311425).glow(true);

      for (int s = -1; s <= 1; s += 2) {
         for (int w = 0; w < 2; w++) {
            g.push();
            g.translate(s * 0.7F, 1.6F, -0.9F - w * 0.6F);
            g.scale(s, 1.0F, 1.0F);
            g.rotY(35 + w * 20);
            g.rotZ(25 - w * 12 + fl);
            g.scale(1.0F + p.gr * 0.3F);
            g.plane("pet_wing", 1.4F - w * 0.3F, 0.5F, 2.8F - w * 0.7F, 1.8F - w * 0.5F, 0.0F, 0.0F, 1.0F, 1.0F);
            g.pop();
         }
      }

      g.color(oc).glow(false);
      g.push();
      g.translate(0.0F, 0.9F, 1.15F);
      Pets.headTurn(g, p);
      float rx = 2.6F;
      float ry = 2.45F;
      float rz = 2.35F;
      PetGeo.blob(g, Pets.head("bee"), 0.0F, 0.0F, 0.0F, rx, ry, rz, 18, 11);
      Pets.face(g, p, "blob", 0.0F, 0.0F, 0.0F, rx, ry, rz, 1.0F, 0.0F);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 0.8F, ry - 0.3F, 0.3F);
         float droop = Math.max(0.0F, 1.0F - p.ear);
         g.rotZ(-s * (18.0F + droop * 40.0F) + PetGeo.sin(p.t * 5.0F + s) * 6.0F);
         g.rotX(-12.0F + droop * 30.0F);
         g.cylinder(Pets.col(3811882), 0.09F, 0.09F, 0.0F, 1.4F, 6, false, 0.0F, 0.0F, 1.0F, 1.0F);
         PetGeo.blob(g, Pets.gl(3811882), 0.0F, 1.55F, 0.0F, 0.36F, 0.36F, 0.36F, 7, 4);
         g.pop();
      }

      g.pop();

      for (int s = -1; s <= 1; s += 2) {
         PetGeo.blob(g, Pets.col(3811882), s * 0.8F, -1.9F, -0.4F, 0.22F, 0.5F, 0.22F, 6, 3);
      }

      Pets.fx(g, p, 3.8F);
      g.pop();
   }

   static void bat(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      g.push();
      Pets.root(g, p);
      g.translate(0.0F, 4.6F * p.hover + PetGeo.sin(p.t * 3.0F) * 0.3F - p.flap * 0.45F, 0.0F);
      g.rotX(p.sp * 14.0F);
      float fl = p.flap * (p.is("sleepy") ? 8.0F : (p.is("excited") ? 48.0F : 36.0F + p.sp * 10.0F));

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 1.9F, 0.7F, -0.5F);
         g.scale(s, 1.0F, 1.0F);
         g.rotY(22.0F);
         g.rotZ(-8.0F + fl);
         g.scale(1.0F + p.gr * 0.4F);
         g.planeTL("pet_bat_wing", 4.6F, 2.3F, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      for (int s = -1; s <= 1; s += 2) {
         PetGeo.blob(g, Pets.col(4864618), s * 0.8F, -2.5F, 0.0F, 0.3F, 0.35F, 0.3F, 6, 3);
      }

      g.push();
      Pets.headTurn(g, p);
      float rx = 2.8F;
      float ry = 2.65F;
      float rz = 2.55F;
      PetGeo.blob(g, Pets.head("bat"), 0.0F, 0.0F, 0.0F, rx, ry, rz, 18, 12);
      Pets.face(g, p, "gem", 0.0F, 0.0F, 0.0F, rx, ry, rz, 1.0F, 0.05F);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 0.32F, -0.95F, rz * 0.93F);
         g.rotX(180.0F);
         PetGeo.cone(g, Pets.col(16777215), 0.14F, 0.38F, 5, false);
         g.pop();
      }

      for (int s = -1; s <= 1; s += 2) {
         Pets.earPointy(g, p, Pets.col(7232660), Pets.col(16757712), s * 1.55F, 1.9F, -0.2F, s, 1.05F, 2.2F, 16.0F);
      }

      g.pop();
      Pets.fx(g, p, 3.6F);
      g.pop();
   }

   static void ghostPet(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      g.push();
      Pets.root(g, p);
      g.translate(0.0F, 2.4F * p.hover + 0.8F + PetGeo.sin(p.t * 2.0F) * 0.5F, 0.0F);
      int oc = g.color;
      g.color(-385875969);

      for (int k = 0; k < 8; k++) {
         double an = (Math.PI * 2) * k / 8.0 + 0.2;
         float wy = PetGeo.sin(p.t * 4.0F + k * 1.3F) * 0.3F;
         PetGeo.blob(g, Pets.fur("ghost"), PetGeo.cos(an) * 2.05F, -0.2F + wy, PetGeo.sin(an) * 1.85F, 1.0F, 1.0F, 1.0F, 7, 4);
      }

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 2.8F, 1.6F, 0.4F);
         g.rotZ(-s * (30.0F + (!p.is("excited") && !p.is("happy") ? 0.0F : PetGeo.sin(p.t * 8.0F) * 25.0F) - (p.is("sad") ? 30 : 0)));
         PetGeo.blob(g, Pets.fur("ghost"), 0.0F, -0.6F, 0.0F, 0.55F, 0.95F, 0.55F, 7, 4);
         g.pop();
      }

      g.push();
      g.translate(0.0F, 2.1F, 0.0F);
      Pets.headTurn(g, p);
      float rx = 3.0F;
      float ry = 3.1F;
      float rz = 2.8F;
      PetGeo.blob(g, Pets.fur("ghost"), 0.0F, 0.0F, 0.0F, rx, ry, rz, 18, 12);
      g.color(oc);
      Pets.face(g, p, "blob", 0.0F, 0.0F, 0.0F, rx, ry, rz, 1.0F, 0.1F);
      g.pop();
      g.color(oc);
      Pets.fx(g, p, 6.4F);
      g.pop();
   }

   static void jelly(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      g.push();
      Pets.root(g, p);
      float pulse = PetGeo.sin(p.t * (p.is("excited") ? 6 : 3));
      g.translate(0.0F, 3.9F * p.hover + 0.6F + PetGeo.sin(p.t * 1.8F) * 0.6F + pulse * 0.2F, 0.0F);
      int oc = g.color;
      boolean og = g.glow;
      g.color(-788529153).glow(true);

      for (int k = 0; k < 6; k++) {
         double an = (Math.PI * 2) * k / 6.0 + 0.5;
         float bx = PetGeo.cos(an) * 1.6F;
         float bz = PetGeo.sin(an) * 1.6F;
         float len = (3.4F - k % 2 * 0.6F) * (1.0F + p.gr * 0.45F);
         float px = 0.0F;
         float py = 0.0F;

         for (int i = 0; i < 5; i++) {
            float t0 = i / 5.0F;
            float t1 = (i + 1) / 5.0F;
            float x0 = PetGeo.sin(p.t * 3.0F + k + t0 * 4.0F) * 0.45F * t0;
            float x1 = PetGeo.sin(p.t * 3.0F + k + t1 * 4.0F) * 0.45F * t1;
            float w0 = 0.3F * (1.0F - t0 * 0.6F);
            float w1 = 0.3F * (1.0F - t1 * 0.6F);
            float y0 = -t0 * len;
            float y1 = -t1 * len;

            for (int pl = 0; pl < 2; pl++) {
               float cx = pl == 0 ? 1.0F : 0.0F;
               float cz = pl == 0 ? 0.0F : 1.0F;
               g.quad(
                  "pet_jelly",
                  new float[]{bx + x0 - w0 * cx, y0, bz - w0 * cz},
                  new float[]{bx + x0 + w0 * cx, y0, bz + w0 * cz},
                  new float[]{bx + x1 + w1 * cx, y1, bz + w1 * cz},
                  new float[]{bx + x1 - w1 * cx, y1, bz - w1 * cz},
                  new float[]{0.2F, 0.6F + t0 * 0.3F, 0.3F, 0.6F + t0 * 0.3F, 0.3F, 0.6F + t1 * 0.3F, 0.2F, 0.6F + t1 * 0.3F}
               );
            }

            px = x1;
            py = y1;
         }

         PetGeo.blob(g, "pet_jelly", bx + px, py, bz, 0.2F, 0.2F, 0.2F, 5, 3);
      }

      for (int k = 0; k < 10; k++) {
         double an = (Math.PI * 2) * k / 10.0;
         PetGeo.blob(g, "pet_jelly", PetGeo.cos(an) * 2.55F, 0.35F, PetGeo.sin(an) * 2.55F, 0.6F, 0.45F, 0.6F, 6, 3);
      }

      g.color(-587202561);
      g.push();
      g.translate(0.0F, 0.45F, 0.0F);
      Pets.headTurn(g, p);
      float rx = 3.05F * (1.0F + pulse * 0.04F);
      float ry = 2.9F * (1.0F - pulse * 0.05F);
      float rz = 3.0F * (1.0F + pulse * 0.04F);
      PetGeo.blob2(g, "pet_jelly", 0.0F, 0.0F, 0.0F, rx, ry, 0.55F, rz, 16, 10);
      g.color(oc).glow(false);
      Pets.face(g, p, "blob", 0.0F, 0.9F, 0.0F, rx, ry * 0.72F, rz, 1.0F, -0.05F);
      g.pop();
      g.color(oc).glow(og);
      Pets.fx(g, p, 4.4F);
      g.pop();
   }

   static void fish(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      g.push();
      Pets.root(g, p);
      float cy = 4.25F + PetGeo.sin(p.t * 1.6F) * 0.3F;
      float R = 3.9F;
      g.push();
      g.translate(0.0F, cy + PetGeo.sin(p.t * 2.2F) * 0.35F, 0.3F);
      g.rotY(PetGeo.sin(p.t * 0.8F) * 14.0F + p.headYaw * 0.8F);
      g.rotX(p.headPitch * 0.6F);
      g.rotZ(p.headRoll * 0.8F);
      float rx = 2.35F;
      float ry = 2.15F;
      float rz = 2.5F;
      g.push();
      g.translate(0.0F, 0.0F, -rz + 0.2F);
      g.rotY(90.0F + p.tail * 1.4F + PetGeo.sin(p.t * 7.0F) * 18.0F);
      g.plane("pet_fin", 1.2F, 0.0F, 2.4F, 2.6F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.push();
      g.translate(0.0F, ry - 0.25F, -0.5F);
      g.rotX(-20.0F + PetGeo.sin(p.t * 3.0F) * 6.0F);
      PetGeo.blob(g, Pets.col(16756848), 0.0F, 0.45F, 0.0F, 0.22F, 0.75F, 1.0F, 6, 4);
      g.pop();

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * (rx - 0.2F), -0.5F, 0.1F);
         g.rotZ(-s * (50.0F + PetGeo.sin(p.t * 6.0F + s) * 18.0F));
         PetGeo.blob(g, Pets.col(16756848), 0.0F, -0.6F, 0.0F, 0.18F, 0.7F, 0.45F, 6, 4);
         g.pop();
      }

      PetGeo.blob(g, Pets.fur("fish"), 0.0F, 0.0F, 0.0F, rx, ry, rz, 16, 10);
      Pets.face(g, p, "blue", 0.0F, 0.0F, 0.0F, rx, ry, rz, 0.95F, 0.05F);
      g.pop();
      PetGeo.blob(g, "pet_bubble", 0.0F, cy, 0.0F, R, R, R, 18, 11);

      for (int k = 0; k < 2; k++) {
         float ph = ((p.t * 0.4F + k * 0.5F) % 1.0F + 1.0F) % 1.0F;
         PetGeo.blob(g, "pet_bubble", 1.2F - k * 2.1F, cy + R + ph * 2.5F, 0.3F, 0.3F + k * 0.12F, 0.3F + k * 0.12F, 0.3F + k * 0.12F, 6, 4);
      }

      Pets.fx(g, p, cy + R + 0.8F);
      g.pop();
   }

   static void cloud(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      g.push();
      Pets.root(g, p);
      float hv = 2.2F * p.hover + 1.0F + PetGeo.sin(p.t * 1.7F) * 0.45F;
      g.translate(0.0F, hv, 0.0F);
      int oc = g.color;
      boolean angry = p.is("angry");
      g.color(angry ? -4933432 : (p.is("sad") ? -2301202 : -1));
      String t = Pets.fur("cloud");
      g.push();
      Pets.headTurn(g, p);

      for (int s = -1; s <= 1; s += 2) {
         PetGeo.blob2(g, t, s * 2.6F, 0.8F, -0.2F, 1.9F, 1.7F, 0.9F, 1.8F, 10, 6);
         PetGeo.blob2(g, t, s * 1.15F, 3.0F, -0.6F, 1.75F, 1.6F, 1.2F, 1.6F, 10, 6);
      }

      PetGeo.blob2(g, t, 0.0F, 1.6F, -1.4F, 2.3F, 2.2F, 1.2F, 1.6F, 10, 6);
      float rx = 3.1F;
      float ry = 2.5F;
      float rz = 2.6F;
      PetGeo.blob2(g, t, 0.0F, 1.6F, 0.0F, rx, ry, 1.1F, rz, 16, 10);
      g.color(oc);
      Pets.face(g, p, "blob", 0.0F, 1.6F, 0.0F, rx, ry, rz, 0.95F, -0.08F);
      g.pop();
      if (!p.is("sad") && !angry) {
         if (p.is("happy") || p.is("excited")) {
            for (int k = 0; k < 2; k++) {
               double an = p.t * 1.5 + k * Math.PI;
               PetGeo.sprite(g, "pet_star", PetGeo.cos(an) * 3.8F, 2.4F + PetGeo.sin(an * 2.0) * 0.8F, PetGeo.sin(an) * 2.0F, 0.9F, -520093697);
            }
         }
      } else {
         for (int k = 0; k < 4; k++) {
            float ph = ((p.t * 1.3F + k * 0.27F) % 1.0F + 1.0F) % 1.0F;
            int al = (int)(230.0F * (1.0F - ph));
            float x = -1.8F + k * 1.2F;
            float y = 0.5F - ph * hv * 0.9F;
            float z = 0.4F * (k % 2);
            if (angry && k == 1) {
               PetGeo.sprite(g, "pet_star", x, y, z, 1.2F, al << 24 | 16777215);
            } else {
               g.color(al << 24 | 16777215).glow(true);
               PetGeo.blob(g, Pets.gl(8374527), x, y, z, 0.16F, 0.36F, 0.16F, 6, 3);
               g.color(oc).glow(false);
            }
         }
      }

      Pets.fx(g, p, 5.6F);
      g.pop();
   }

   static void slime(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      float[] h = PetRig.hop(p);
      p.y = p.y + h[0] * 2.0F;
      p.stepBob = 0.0F;
      g.push();
      Pets.root(g, p);
      float sq = PetGeo.sin(p.t * 3.0F) * 0.04F - h[1] * 0.16F;
      float rx = 3.7F * (1.0F + sq);
      float ry = 3.0F * (1.0F - sq);
      float rz = 3.4F * (1.0F + sq);
      g.push();
      g.translate(0.0F, ry * 0.92F, 0.0F);
      PetGeo.blob(g, Pets.gl(4173411), 0.6F, -0.4F, -0.9F, 1.45F, 1.25F, 1.35F, 7, 4);
      Pets.headTurn(g, p);
      int oc = g.color;
      g.color(-922746881);
      PetGeo.blob2(g, Pets.gl(9236648), 0.0F, 0.0F, 0.0F, rx, ry, ry * 0.85F, rz, 18, 12);
      g.color(oc);
      Pets.face(g, p, "blob", 0.0F, 0.0F, 0.0F, rx, ry, rz, 1.0F, 0.02F);
      g.pop();
      Pets.fx(g, p, ry * 1.9F + 1.2F);
      g.pop();
   }

   static void mushroom(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      g.push();
      Pets.root(g, p);
      feet2(g, p, Pets.col(15257512), 0.95F, 0.1F, 0.55F);
      float drop = p.lie * 0.4F;
      float rx = 2.35F;
      float ry = 2.3F;
      float rz = 2.15F;
      g.push();
      g.translate(0.0F, 2.45F - drop, 0.0F);
      g.rotZ(PetGeo.sin(p.t * 11.0F) * 5.0F * p.sp);
      PetGeo.blob(g, Pets.fur("mush"), 0.0F, 0.0F, 0.0F, rx, ry, rz, 17, 11);
      Pets.face(g, p, "blob", 0.0F, 0.0F, 0.0F, rx, ry, rz, 1.0F, 0.02F);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 2.2F, -0.2F, 0.2F);
         g.rotZ(-s * (25.0F + (p.is("excited") ? Math.abs(PetGeo.sin(p.t * 7.0F)) * 60.0F : 0.0F) - (p.is("sad") ? 15 : 0)));
         PetGeo.blob(g, Pets.col(16773596), 0.0F, -0.55F, 0.0F, 0.35F, 0.7F, 0.35F, 6, 4);
         g.pop();
      }

      g.push();
      g.translate(0.0F, 2.0F, -0.1F);
      g.rotY(p.headYaw * 0.4F);
      g.rotX(p.headPitch * 0.6F - 6.0F);
      g.rotZ(p.headRoll * 0.8F + PetGeo.sin(p.t * 2.1F) * 3.0F);
      PetGeo.blob2(g, "pet_cap", 0.0F, 0.0F, 0.0F, 3.9F, 2.5F, 0.75F, 3.7F, 16, 10);
      g.pop();
      g.pop();
      Pets.fx(g, p, 7.4F);
      g.pop();
   }

   static void brickBuddy(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      p.y = p.y + Math.abs(PetGeo.sin(p.t * 6.0F)) * 0.8F * p.sp;
      g.push();
      Pets.root(g, p);
      String yel = Pets.gl(16764163);
      String red = "pet_brick:E3000B";

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 1.3F, 0.95F, 0.2F);
         g.rotX(s * p.walk);
         PetGeo.blob(g, yel, 0.0F, -0.45F, 0.2F, 0.6F, 0.55F, 0.8F, 7, 4);
         g.pop();
      }

      g.push();
      g.rotY(p.headYaw * 0.5F);
      g.translate(0.0F, 3.0F, 0.0F);
      g.rotX(p.headPitch * 0.5F);
      g.rotZ(p.headRoll * 0.7F);
      g.translate(0.0F, -3.0F, 0.0F);
      PetGeo.rbox(g, red, -3.0F, 0.8F, -3.0F, 3.0F, 5.0F, 3.0F, 0.4F);

      for (int ix = -1; ix <= 1; ix += 2) {
         for (int iz = -1; iz <= 1; iz += 2) {
            g.push();
            g.translate(ix * 1.5F, 5.0F, iz * 1.5F);
            g.cylinder(red, 1.0F, 1.0F, -0.05F, 0.75F, 12, true, 0.2F, 0.1F, 0.8F, 0.9F);
            g.pop();
         }
      }

      Pets.faceFlat(g, p, "blob", 0.0F, 2.9F, 3.02F, 5.4F, 4.05F);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 3.0F, 3.0F, 0.3F);
         float wave = p.is("excited")
            ? 50.0F + PetGeo.sin(p.t * 9.0F) * 40.0F
            : (p.is("happy") ? 20.0F + PetGeo.sin(p.t * 4.0F) * 12.0F : (p.is("sad") ? -5.0F : 12.0F));
         g.rotZ(-s * wave);
         PetGeo.blob(g, yel, s * 0.25F, -0.7F, 0.0F, 0.42F, 0.85F, 0.42F, 7, 4);
         g.pop();
      }

      g.pop();
      Pets.fx(g, p, 7.2F);
      g.pop();
   }

   static void robot(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      g.push();
      Pets.root(g, p);
      String metal = "pet_metal:C9D6F0";
      String dark = "pet_metal:6A7A96";
      String accent = Pets.gl(8370431);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 1.25F, 0.9F, 0.0F);
         g.rotX(s * p.walk);
         PetGeo.blob(g, dark, 0.0F, -0.45F, 0.25F, 0.75F, 0.5F, 1.0F, 7, 4);
         g.pop();
      }

      PetGeo.rbox(g, metal, -2.0F, 1.0F, -1.7F, 2.0F, 3.9F, 1.7F, 0.45F);
      PetGeo.sprite(g, "pet_heart", 0.0F, 2.45F, 1.74F, 1.3F, -1);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 2.2F, 3.4F, 0.0F);
         float wave = p.is("excited")
            ? 120.0F + PetGeo.sin(p.t * 10.0F) * 30.0F
            : (p.is("happy") ? 20.0F + PetGeo.sin(p.t * 4.0F) * 10.0F : (p.is("sad") ? 2.0F : 10.0F));
         g.rotX(-s * p.walk);
         g.rotZ(-s * wave);
         g.cylinder(dark, 0.3F, 0.3F, -1.4F, 0.0F, 8, false, 0.0F, 0.0F, 1.0F, 1.0F);
         PetGeo.blob(g, accent, 0.0F, -1.6F, 0.0F, 0.5F, 0.5F, 0.5F, 7, 4);
         g.pop();
      }

      g.push();
      g.translate(0.0F, 6.35F - p.lie * 0.5F, 0.2F);
      Pets.headTurn(g, p);
      PetGeo.rbox(g, metal, -3.1F, -2.3F, -2.5F, 3.1F, 2.3F, 2.5F, 0.6F);
      PetGeo.flat(g, "pet_screen", 0.0F, -0.05F, 2.52F, 5.2F, 3.7F);
      Pets.faceFlat(g, p, "led", 0.0F, -0.05F, 2.55F, 4.9F, 3.68F);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 3.1F, 0.0F, 0.0F);
         g.rotZ(90.0F);
         g.cylinder(accent, 0.85F, 0.85F, -0.35F, 0.35F, 10, true, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      g.push();
      g.translate(0.0F, 2.3F, 0.0F);
      g.rotZ(PetGeo.sin(p.t * 3.0F) * 10.0F + (p.is("sad") ? 35 : 0));
      g.cylinder(dark, 0.1F, 0.1F, 0.0F, 1.3F, 6, false, 0.0F, 0.0F, 1.0F, 1.0F);
      boolean og = g.glow;
      g.glow(true);
      int led = p.is("love") ? 16740296 : (p.is("angry") ? 16734810 : (p.is("sleepy") ? 6978303 : 7336959));
      PetGeo.blob(g, Pets.gl(led), 0.0F, 1.6F, 0.0F, 0.48F, 0.48F, 0.48F, 7, 4);
      g.glow(og);
      g.pop();
      g.pop();
      Pets.fx(g, p, 10.2F);
      g.pop();
   }
}
