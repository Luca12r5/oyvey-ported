package dev.lego.cosmetic;

import java.util.Arrays;

final class PetReal {
   private PetReal() {
   }

   static void real4(G g, Pets.P p, PetReal.R4 r) {
      float k = p.gr;
      float S = (p.preview ? r.babyS * 1.4F : PetRig.lerp(r.babyS, 1.0F, k)) * r.size;
      float hf = PetRig.lerp(r.babyHead, 1.0F, k);
      float lf = PetRig.lerp(r.babyLeg, 1.0F, k);
      float bf = PetRig.lerp(r.babyLen, 1.0F, k);
      float sf = PetRig.lerp(r.babySnout, 1.0F, k);
      float nf = PetRig.lerp(r.babyNeck, 1.0F, k);
      float fat = PetRig.lerp(1.18F, 1.0F, k);
      float L = r.L * lf;
      float zs = r.zs * bf;
      float zh = r.zh * bf;
      float rx = r.rx * fat;
      float ry = r.ry * fat;
      float hipH = L * 0.96F + r.pawR * 0.85F;
      float bodyY = hipH + ry * 0.18F;
      float belly = bodyY - ry;
      p.stepBob = 0.0F;
      float graze = 0.0F;
      if (r.grazer) {
         graze = p.sit;
         p.sit = 0.0F;
         p.bow = Math.max(p.bow, graze * 0.3F);
      }

      g.push();
      g.scale(S);
      Pets.root(g, p);
      PetRig.Frame f = PetRig.frame(p, hipH, zs, zh, L, belly);
      float thick = PetRig.lerp(1.3F, 1.0F, k);
      PetRig.LegStyle fr = new PetRig.LegStyle(r.legUp, r.legLo, r.paw, r.r1f * thick, r.r2f * thick, r.pawR * PetRig.lerp(1.15F, 1.0F, k));
      PetRig.LegStyle hi = new PetRig.LegStyle(r.legUp, r.legLo, r.paw, r.r1h * thick, r.r2h * thick, r.pawR * PetRig.lerp(1.15F, 1.0F, k));
      fr.hoof = hi.hoof = r.hoof;
      fr.pawLen = hi.pawLen = r.pawLen;
      fr.seg = hi.seg = 5;
      PetRig.legs(g, p, f, fr, hi, r.legX * PetRig.lerp(1.1F, 1.0F, k), zs, zh, hipH, L, 0.5F, 0.46F);
      g.push();
      f.apply(g);
      g.rotZ(p.roll);
      float chestZ = zs * 0.45F;
      float hipZ = zh * 0.5F;
      float half = (zs - zh) * 0.5F;
      g.push();
      g.translate(0.0F, bodyY, (chestZ + hipZ) / 2.0F);
      g.scale(1.0F, 1.0F, p.stretch);
      g.translate(0.0F, 0.0F, -(chestZ + hipZ) / 2.0F);
      PetGeo.blob(g, r.body, 0.0F, 0.0F, chestZ, rx, ry, half * 0.95F, 9, 5);
      PetGeo.blob(g, r.body, 0.0F, -ry * 0.05F, hipZ, rx * r.hipF, ry * r.hipF, half * 0.85F, 9, 5);
      if (r.bodyExtra != null) {
         g.push();
         g.translate(0.0F, 0.0F, 0.0F);
         r.bodyExtra.draw(g, p);
         g.pop();
      }

      if (r.tail != null) {
         g.push();
         g.translate(0.0F, ry * r.hipF * 0.45F, hipZ - half * 0.78F);
         g.scale(PetRig.lerp(0.75F, 1.0F, k));
         r.tail.draw(g, p);
         g.pop();
      }

      g.pop();
      float ang = r.neckAng
         + 32 * (p.is("sad") ? 1 : 0)
         + 18.0F * p.crouch
         + 38.0F * p.lie
         + 36.0F * p.sniff
         + 28.0F * p.bow
         - 10 * (p.is("curious") ? 1 : 0)
         - 10.0F * p.sit
         + 85.0F * graze;
      float nl = r.neckLen * nf * (1.0F + 0.25F * graze);
      float nbY = bodyY + ry * 0.42F;
      float nbZ = chestZ + half * 0.55F;
      g.push();
      g.translate(0.0F, nbY, nbZ);
      g.rotX(ang);
      float nr = r.neckR * PetRig.lerp(1.1F, 1.0F, k);
      PetGeo.blob(g, r.body, 0.0F, nl * 0.5F, 0.0F, nr, nl * 0.5F + nr * 0.55F, nr * 1.05F, 8, 5);
      if (r.neckExtra != null) {
         r.neckExtra.draw(g, p);
      }

      g.push();
      g.translate(0.0F, nl * 0.12F + nr * 0.2F, 0.0F);
      PetRig.collar(g, p, nr * 1.02F, 0.2F + 0.05F * k);
      g.pop();
      g.pop();
      float hy = nbY + PetGeo.cos(Math.toRadians(ang)) * nl;
      float hz = nbZ + PetGeo.sin(Math.toRadians(ang)) * nl;
      g.push();
      g.translate(0.0F, hy - p.bob * L * 0.6F + r.skRy * (hf - 1.0F) * 0.35F, hz + r.skRz * (hf - 1.0F) * 0.3F);
      Pets.headTurn(g, p);
      g.rotX(p.nod + r.headDown * PetRig.lerp(0.4F, 1.0F, k) + graze * (45.0F + PetGeo.sin(p.t * 5.0F) * 4.0F));
      g.scale(hf);
      realHead(g, p, r, sf, k);
      g.pop();
      Pets.fx(g, p, hy + r.skRy * hf + 1.4F);
      g.pop();
      PetRig.sparkles(g, p, Math.max(half * 1.4F, rx + 1.2F));
      g.pop();
   }

   static void realHead(G g, Pets.P p, PetReal.R4 r, float sf, float k) {
      PetGeo.blob(g, r.head, 0.0F, 0.0F, 0.0F, r.skRx, r.skRy, r.skRz, 11, 8);
      float sl = r.snLen * sf;
      float sc = r.skRz * 0.42F + sl * 0.5F;
      float srx = r.snR * PetRig.lerp(1.1F, 1.0F, k);
      float sry = r.snR * 0.8F;
      float srz = sl * 0.55F + r.snR * 0.3F;
      PetGeo.blob(g, r.muzzle, 0.0F, r.snY, sc, srx, sry, srz, 8, 5);
      PetGeo.blob(g, Pets.gl(r.nose), 0.0F, r.snY + sry * 0.45F, sc + srz * 0.93F, srx * 0.42F, sry * 0.34F, srx * 0.3F, 6, 3);
      String style = PetRig.style(r.style, k);
      boolean og = g.glow;
      int oc = g.color;
      g.color(-1).glow(false);
      float v1 = 0.6F;
      float spanEl = r.span * (v1 * 192.0F) / 256.0F * r.skRx / r.skRy;
      float el0 = r.eyeEl + 0.458F * spanEl;
      PetGeo.decal(g, "pet_face:" + style + ":" + p.emo, 0.0F, 0.0F, 0.0F, r.skRx, r.skRy, r.skRz, 0.0F, el0, r.span, 0.0F, 0.0F, 1.0F, v1, 1.035F);
      PetGeo.decal(
         g, Pets.eyes(style, p), 0.0F, 0.0F, 0.0F, r.skRx, r.skRy, r.skRz, p.lookX * 0.08F, el0 + p.lookY * 0.05F, r.span, 0.0F, 0.0F, 1.0F, v1, 1.05F
      );
      PetGeo.decal(g, "pet_face:" + style + ":" + p.emo, 0.0F, r.snY, sc, srx, sry, srz, 0.0F, -0.32F, 0.95F, 0.25F, 0.57F, 0.75F, 0.8F, 1.04F, 4, 3);
      g.color(oc).glow(og);
      if (r.ears != null) {
         r.ears.draw(g, p);
      }

      if (r.headExtra != null) {
         r.headExtra.draw(g, p);
      }
   }

   static void chain(G g, Pets.P p, String[] tex, float[] rad, float seg, float curl, float lift, float wag, Pets.Part tip) {
      g.push();
      g.rotY(p.tail * wag + p.tailSway);
      g.rotX(lift + p.tailLift);

      for (int i = 0; i < rad.length; i++) {
         g.rotX(curl);
         g.rotY(p.tail * 0.3F * wag + p.tailSway * 0.4F);
         PetGeo.blob(g, tex[Math.min(i, tex.length - 1)], 0.0F, 0.0F, -seg * 0.5F, rad[i], rad[i], seg * 0.62F + rad[i] * 0.3F, 6, 3);
         g.translate(0.0F, 0.0F, -seg * 0.82F);
      }

      if (tip != null) {
         tip.draw(g, p);
      }

      g.pop();
   }

   static String[] rep(String s, int n) {
      String[] a = new String[n];
      Arrays.fill(a, s);
      return a;
   }

   static void wolf(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      PetReal.R4 r = new PetReal.R4();
      r.body = Pets.fur("wolf");
      r.head = Pets.head("wolf");
      r.muzzle = Pets.col(14473942);
      r.legUp = Pets.col(9343902);
      r.legLo = Pets.col(12172484);
      r.paw = Pets.col(14080220);
      r.style = "wolf";
      r.L = 5.4F;
      r.legX = 1.25F;
      r.zs = 3.0F;
      r.zh = -3.1F;
      r.rx = 1.95F;
      r.ry = 2.25F;
      r.hipF = 0.86F;
      r.neckLen = 2.3F;
      r.neckAng = 44.0F;
      r.neckR = 1.3F;
      r.snLen = 2.1F;
      r.snR = 0.78F;
      r.ears = (gg, pp) -> {
         for (int s = -1; s <= 1; s += 2) {
            Pets.earPointy(gg, pp, Pets.col(8291214), Pets.col(15329250), Pets.col(5133150), s * 1.0F, 1.2F, -0.35F, s, 0.72F, 1.6F, 8.0F);
         }
      };
      r.neckExtra = (gg, pp) -> {
         float m = PetRig.smooth(0.2F, 1.0F, pp.gr);
         PetGeo.blob(gg, r.body, 0.0F, 0.4F, 0.35F, 1.6F * m + 0.1F, 1.5F * m + 0.1F, 1.25F * m + 0.1F, 7, 4);
      };
      String gw = Pets.col(9343902);
      String dk = Pets.col(5133150);
      String lt = Pets.col(14473942);
      r.tail = (gg, pp) -> chain(gg, pp, new String[]{gw, gw, gw, dk, dk}, new float[]{0.5F, 0.7F, 0.78F, 0.7F, 0.48F}, 1.05F, 4.0F, -38.0F, 0.6F, null);
      real4(g, p, r);
   }

   static void retriever(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      PetReal.R4 r = new PetReal.R4();
      r.body = Pets.fur("retr");
      r.head = Pets.head("retr");
      r.muzzle = Pets.col(15582856);
      r.legUp = Pets.col(14920798);
      r.legLo = Pets.col(15580792);
      r.paw = Pets.col(15779976);
      r.style = "retr";
      r.L = 4.5F;
      r.legX = 1.25F;
      r.zs = 2.8F;
      r.zh = -2.9F;
      r.rx = 2.0F;
      r.ry = 2.15F;
      r.neckLen = 2.0F;
      r.neckAng = 40.0F;
      r.neckR = 1.2F;
      r.skRx = 1.72F;
      r.skRy = 1.62F;
      r.skRz = 1.75F;
      r.snLen = 1.8F;
      r.snR = 0.85F;
      r.snY = -0.5F;
      r.nose = 2760222;
      r.babyS = 0.6F;
      r.ears = (gg, pp) -> {
         float droop = Math.max(0.0F, 1.0F - pp.ear) * 15.0F;
         float perk = Math.max(0.0F, pp.ear - 1.0F) * 90.0F;

         for (int s = -1; s <= 1; s += 2) {
            gg.push();
            gg.translate(s * 1.45F, 0.75F, -0.3F);
            gg.rotZ(s * (14.0F + droop - perk + pp.earTwitch * (s > 0 ? 1 : 0)) + PetGeo.sin(pp.t * 9.0F) * 4.0F * pp.walkW);
            gg.rotX(-6.0F);
            PetGeo.blob(gg, Pets.col(14064206), s * 0.12F, -0.95F, 0.0F, 0.42F, 1.15F, 0.82F, 7, 4);
            gg.pop();
         }
      };
      r.neckExtra = (gg, pp) -> {
         float m = PetRig.smooth(0.3F, 1.0F, pp.gr);
         PetGeo.blob(gg, Pets.col(15912852), 0.0F, 0.2F, 0.55F, 1.15F * m + 0.1F, 1.4F * m + 0.1F, 0.9F * m + 0.1F, 7, 4);
      };
      String c1 = Pets.col(14920798);
      String c2 = Pets.col(15779976);
      r.tail = (gg, pp) -> chain(gg, pp, new String[]{c1, c1, c2, c2, c2}, new float[]{0.42F, 0.58F, 0.66F, 0.58F, 0.4F}, 1.0F, 9.0F, -8.0F, 1.0F, null);
      real4(g, p, r);
   }

   static void arcticFox(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      PetReal.R4 r = new PetReal.R4();
      r.body = Pets.fur("afox");
      r.head = Pets.head("afox");
      r.muzzle = Pets.col(16514303);
      r.legUp = Pets.col(15790840);
      r.legLo = Pets.col(15133426);
      r.paw = Pets.col(16777215);
      r.style = "afox";
      r.size = 0.86F;
      r.L = 3.8F;
      r.legX = 1.1F;
      r.zs = 2.5F;
      r.zh = -2.5F;
      r.rx = 1.75F;
      r.ry = 1.95F;
      r.neckLen = 1.6F;
      r.neckAng = 46.0F;
      r.neckR = 1.15F;
      r.skRx = 1.6F;
      r.skRy = 1.45F;
      r.skRz = 1.6F;
      r.snLen = 1.55F;
      r.snR = 0.62F;
      r.snY = -0.42F;
      r.nose = 1972770;
      r.r1f = 0.62F;
      r.r2f = 0.42F;
      r.r1h = 0.85F;
      r.r2h = 0.44F;
      r.pawR = 0.55F;
      r.babyS = 0.66F;
      r.ears = (gg, pp) -> {
         for (int s = -1; s <= 1; s += 2) {
            Pets.earPointy(gg, pp, Pets.col(15790840), Pets.col(14212842), s * 0.95F, 1.1F, -0.3F, s, 0.7F, 1.05F, 14.0F);
         }
      };
      r.neckExtra = (gg, pp) -> PetGeo.blob(gg, r.body, 0.0F, 0.35F, 0.3F, 1.55F, 1.35F, 1.3F, 8, 5);
      String w = Pets.col(16185596);
      String w2 = Pets.col(15133942);
      r.tail = (gg, pp) -> chain(gg, pp, new String[]{w, w, w2, w, w}, new float[]{0.75F, 1.12F, 1.28F, 1.16F, 0.8F}, 1.15F, 9.0F, -18.0F, 0.8F, null);
      real4(g, p, r);
   }

   static void horse(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      PetReal.R4 r = new PetReal.R4();
      r.body = Pets.fur("horse");
      r.head = Pets.head("horse");
      r.muzzle = Pets.col(9065014);
      r.legUp = Pets.col(10707002);
      r.legLo = Pets.col(4860958);
      r.paw = Pets.gl(2760736);
      r.style = "horse";
      r.nose = 3811876;
      r.grazer = true;
      r.L = 7.2F;
      r.legX = 1.2F;
      r.zs = 3.6F;
      r.zh = -3.6F;
      r.rx = 2.15F;
      r.ry = 2.55F;
      r.hipF = 0.96F;
      r.neckLen = 3.8F;
      r.neckAng = 30.0F;
      r.neckR = 1.12F;
      r.skRx = 1.3F;
      r.skRy = 1.4F;
      r.skRz = 1.45F;
      r.snLen = 2.8F;
      r.snR = 0.92F;
      r.snY = -0.3F;
      r.headDown = 38.0F;
      r.r1f = 0.72F;
      r.r2f = 0.36F;
      r.r1h = 0.98F;
      r.r2h = 0.38F;
      r.pawR = 0.46F;
      r.hoof = true;
      r.span = 1.1F;
      r.eyeEl = 0.3F;
      r.babyS = 0.55F;
      r.babyLeg = 0.78F;
      r.babyHead = 1.6F;
      r.babyNeck = 0.6F;
      r.babySnout = 0.55F;
      String mane = Pets.col(2364688);
      r.ears = (gg, pp) -> {
         for (int s = -1; s <= 1; s += 2) {
            Pets.earPointy(gg, pp, Pets.col(10707002), Pets.col(5911584), s * 0.7F, 1.15F, -0.4F, s, 0.45F, 1.15F, 6.0F);
         }

         gg.push();
         gg.translate(0.0F, 1.3F, 0.35F);
         gg.rotX(40.0F + PetGeo.sin(pp.t * 2.0F) * 4.0F);
         PetGeo.blob(gg, mane, 0.0F, 0.3F, 0.0F, 0.5F, 0.75F, 0.3F, 6, 3);
         gg.pop();
      };
      r.neckExtra = (gg, pp) -> {
         float m = PetRig.lerp(0.5F, 1.0F, pp.gr);
         float nl = 3.8F * PetRig.lerp(0.6F, 1.0F, pp.gr);

         for (int i = 0; i < 5; i++) {
            float u = i / 4.0F;
            gg.push();
            gg.translate(0.0F, nl * (0.05F + u * 0.95F), -1.0F);
            gg.rotZ(PetGeo.sin(pp.t * 3.0F + i) * 5.0F * (0.3F + pp.walkW));
            PetGeo.blob(gg, mane, 0.0F, 0.0F, 0.0F, 0.32F * m, 0.62F * m, 0.55F * m, 5, 3);
            gg.pop();
         }
      };
      r.tail = (gg, pp) -> chain(
         gg, pp, rep(mane, 5), new float[]{0.4F, 0.58F, 0.64F, 0.56F, 0.4F}, 1.1F, -6.0F - 6.0F * pp.walkW, -58.0F + 25.0F * pp.runW, 0.5F, null
      );
      real4(g, p, r);
   }

   static void deer(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      PetReal.R4 r = new PetReal.R4();
      boolean fawn = a.growth < 0.5F;
      r.body = Pets.fur(fawn ? "fawn" : "deer");
      r.head = Pets.head("deer");
      r.muzzle = Pets.col(12092504);
      r.legUp = Pets.col(11104586);
      r.legLo = Pets.col(12618334);
      r.paw = Pets.gl(2760736);
      r.style = "deer";
      r.nose = 1709076;
      r.grazer = true;
      r.L = 6.6F;
      r.legX = 1.02F;
      r.zs = 3.0F;
      r.zh = -3.1F;
      r.rx = 1.7F;
      r.ry = 2.1F;
      r.hipF = 0.95F;
      r.neckLen = 3.2F;
      r.neckAng = 26.0F;
      r.neckR = 0.92F;
      r.skRx = 1.3F;
      r.skRy = 1.3F;
      r.skRz = 1.45F;
      r.snLen = 1.9F;
      r.snR = 0.68F;
      r.snY = -0.32F;
      r.headDown = 16.0F;
      r.r1f = 0.55F;
      r.r2f = 0.28F;
      r.r1h = 0.8F;
      r.r2h = 0.3F;
      r.pawR = 0.38F;
      r.hoof = true;
      r.span = 1.05F;
      r.eyeEl = 0.26F;
      r.babyS = 0.55F;
      r.babyLeg = 0.75F;
      r.babyHead = 1.65F;
      int lvl = Math.max(1, Math.min(5, a.level));
      r.ears = (gg, pp) -> {
         for (int s = -1; s <= 1; s += 2) {
            Pets.earPointy(gg, pp, Pets.col(11104586), Pets.col(16181466), s * 0.95F, 0.85F, -0.4F, s, 0.72F, 1.75F, 55.0F);
         }
      };
      r.headExtra = (gg, pp) -> antlers(gg, pp, lvl, pp.gr);
      r.tail = (gg, pp) -> {
         gg.rotX(35.0F + pp.tailLift * 0.5F + (!pp.is("excited") && !pp.is("angry") ? 0 : 40));
         PetGeo.blob(gg, Pets.col(16777215), 0.0F, 0.0F, -0.45F, 0.42F, 0.35F, 0.62F, 6, 3);
      };
      real4(g, p, r);
   }

   static void antlers(G g, Pets.P p, int lvl, float k) {
      if (lvl >= 2) {
         String bone = Pets.col(15259576);
         float len = lvl == 2 ? 0.55F : (lvl == 3 ? 1.6F : (lvl == 4 ? 2.5F : 3.3F));

         for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 0.6F, 1.05F, -0.1F);
            g.rotZ(-s * 22);
            g.rotX(-18.0F);
            g.cylinder(bone, 0.17F, 0.11F, 0.0F, len, 5, false, 0.0F, 0.0F, 1.0F, 1.0F);
            if (lvl >= 4) {
               for (int i = 0; i < (lvl == 5 ? 3 : 2); i++) {
                  g.push();
                  g.translate(0.0F, len * (0.35F + i * 0.25F), 0.0F);
                  g.rotX(48.0F);
                  g.rotZ(-s * 10);
                  g.cylinder(bone, 0.1F, 0.05F, 0.0F, len * (0.42F - i * 0.08F), 4, false, 0.0F, 0.0F, 1.0F, 1.0F);
                  g.pop();
               }

               g.push();
               g.translate(0.0F, len, 0.0F);
               g.rotZ(-s * 25);
               g.cylinder(bone, 0.1F, 0.04F, 0.0F, len * 0.35F, 4, false, 0.0F, 0.0F, 1.0F, 1.0F);
               g.pop();
            } else {
               g.translate(0.0F, len, 0.0F);
               PetGeo.blob(g, bone, 0.0F, 0.0F, 0.0F, 0.14F, 0.14F, 0.14F, 4, 2);
            }

            g.pop();
         }
      }
   }

   static void lion(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      PetReal.R4 r = new PetReal.R4();
      r.body = Pets.fur("lion");
      r.head = Pets.head("lion");
      r.muzzle = Pets.col(16180160);
      r.legUp = Pets.col(14723429);
      r.legLo = Pets.col(15251577);
      r.paw = Pets.col(15781014);
      r.style = "lion";
      r.nose = 11560026;
      r.L = 4.9F;
      r.legX = 1.45F;
      r.zs = 3.2F;
      r.zh = -3.3F;
      r.rx = 2.3F;
      r.ry = 2.4F;
      r.hipF = 0.88F;
      r.neckLen = 1.9F;
      r.neckAng = 50.0F;
      r.neckR = 1.5F;
      r.skRx = 1.9F;
      r.skRy = 1.8F;
      r.skRz = 1.9F;
      r.snLen = 1.4F;
      r.snR = 1.05F;
      r.snY = -0.6F;
      r.r1f = 0.95F;
      r.r2f = 0.62F;
      r.r1h = 1.2F;
      r.r2h = 0.6F;
      r.pawR = 0.75F;
      r.pawLen = 1.15F;
      r.span = 0.95F;
      String mane = Pets.col(10114598);
      String mane2 = Pets.col(12085806);
      r.ears = (gg, pp) -> {
         for (int s = -1; s <= 1; s += 2) {
            Pets.earRound(gg, pp, Pets.col(14195285), Pets.col(16180160), s * 1.35F, 1.3F, -0.35F, s, 0.58F);
         }
      };
      r.headExtra = (gg, pp) -> {
         float m = PetRig.smooth(0.15F, 1.0F, pp.gr);
         if (!(m <= 0.01F)) {
            for (int i = 0; i < 10; i++) {
               double an = (Math.PI * 2) * i / 10.0 + 0.3;
               float wob = PetGeo.sin(pp.t * 2.2F + i) * 0.06F;
               gg.push();
               gg.translate(PetGeo.cos(an) * 1.75F, PetGeo.sin(an) * 1.7F + 0.1F, -0.55F);
               gg.rotZ((float)Math.toDegrees(an) - 90.0F);
               gg.rotX(-25.0F);
               gg.scale(m * (1.0F + wob));
               PetGeo.cone(gg, i % 2 == 0 ? mane : mane2, 0.95F, 2.0F, 4, false);
               gg.pop();
            }

            PetGeo.blob(gg, mane, 0.0F, 0.1F, -0.9F, 2.1F * m + 0.4F, 2.1F * m + 0.4F, 1.3F * m + 0.3F, 7, 4);
         }
      };
      r.neckExtra = (gg, pp) -> {
         float m = PetRig.smooth(0.15F, 1.0F, pp.gr);
         if (m > 0.01F) {
            PetGeo.blob(gg, mane, 0.0F, 1.2F, -0.2F, 2.0F * m, 1.9F * m + 0.3F, 1.9F * m, 7, 4);
         }
      };
      String t = Pets.col(14723429);
      r.tail = (gg, pp) -> chain(
         gg,
         pp,
         rep(t, 5),
         new float[]{0.3F, 0.27F, 0.25F, 0.24F, 0.22F},
         1.4F,
         -7.0F,
         -15.0F,
         0.8F,
         (g2, p2) -> PetGeo.blob(g2, mane, 0.0F, 0.0F, 0.1F, 0.48F, 0.48F, 0.62F, 5, 3)
      );
      real4(g, p, r);
   }

   static void tiger(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      PetReal.R4 r = new PetReal.R4();
      r.body = Pets.fur("tiger");
      r.head = Pets.head("tiger");
      r.muzzle = Pets.col(16774890);
      r.legUp = Pets.fur("tiger");
      r.legLo = Pets.col(15763756);
      r.paw = Pets.col(16773600);
      r.style = "tiger";
      r.nose = 13132906;
      r.L = 5.0F;
      r.legX = 1.4F;
      r.zs = 3.3F;
      r.zh = -3.4F;
      r.rx = 2.15F;
      r.ry = 2.3F;
      r.hipF = 0.9F;
      r.neckLen = 1.9F;
      r.neckAng = 52.0F;
      r.neckR = 1.4F;
      r.skRx = 1.85F;
      r.skRy = 1.72F;
      r.skRz = 1.85F;
      r.snLen = 1.35F;
      r.snR = 1.0F;
      r.snY = -0.58F;
      r.r1f = 0.92F;
      r.r2f = 0.6F;
      r.r1h = 1.15F;
      r.r2h = 0.58F;
      r.pawR = 0.72F;
      r.pawLen = 1.15F;
      r.span = 0.95F;
      r.ears = (gg, pp) -> {
         for (int s = -1; s <= 1; s += 2) {
            Pets.earRound(gg, pp, Pets.col(14712862), Pets.col(16774890), s * 1.3F, 1.25F, -0.3F, s, 0.55F);
         }

         float m = PetRig.lerp(0.4F, 1.0F, pp.gr);

         for (int s = -1; s <= 1; s += 2) {
            gg.push();
            gg.translate(s * 1.5F, -0.55F, -0.2F);
            gg.rotY(-s * 35);
            gg.rotZ(-s * 118);
            gg.scale(1.0F, 1.0F, 0.5F);
            PetGeo.cone(gg, Pets.col(16774890), 0.5F * m, 0.75F * m, 4, false);
            gg.pop();
         }
      };
      String o = Pets.col(15763756);
      String d = Pets.col(2365980);
      r.tail = (gg, pp) -> chain(gg, pp, new String[]{o, d, o, d, o}, new float[]{0.36F, 0.34F, 0.33F, 0.32F, 0.3F}, 1.35F, -5.0F, -18.0F, 0.8F, null);
      real4(g, p, r);
   }

   static void drake(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      PetReal.R4 r = new PetReal.R4();
      int lvl = Math.max(1, Math.min(5, a.level));
      r.body = Pets.fur("drake");
      r.head = Pets.head("drake");
      r.muzzle = Pets.fur("drake");
      r.legUp = Pets.col(8150230);
      r.legLo = Pets.col(6966464);
      r.paw = Pets.col(5914792);
      r.style = "drake";
      r.nose = 5126814;
      r.L = 4.6F;
      r.legX = 1.6F;
      r.zs = 3.2F;
      r.zh = -3.3F;
      r.rx = 2.2F;
      r.ry = 2.3F;
      r.hipF = 0.95F;
      r.neckLen = 3.0F;
      r.neckAng = 36.0F;
      r.neckR = 1.05F;
      r.skRx = 1.55F;
      r.skRy = 1.4F;
      r.skRz = 1.8F;
      r.snLen = 2.0F;
      r.snR = 0.85F;
      r.snY = -0.3F;
      r.r1f = 0.8F;
      r.r2f = 0.55F;
      r.r1h = 1.15F;
      r.r2h = 0.62F;
      r.pawR = 0.68F;
      r.pawLen = 1.5F;
      r.babyS = 0.58F;
      String horn = Pets.col(15917752);
      String spike = Pets.col(15915424);
      boolean fire = lvl >= 3 && (p.is("angry") || p.is("excited"));
      r.ears = (gg, pp) -> {
         float hl = 0.8F + lvl * 0.45F;

         for (int s = -1; s <= 1; s += 2) {
            gg.push();
            gg.translate(s * 0.75F, 1.0F, -0.6F);
            gg.rotZ(-s * 18);
            gg.rotX(-55.0F);
            PetGeo.cone(gg, horn, 0.38F, hl, 7, false);
            gg.pop();
            if (lvl >= 5) {
               gg.push();
               gg.translate(s * 1.25F, 0.4F, -0.7F);
               gg.rotZ(-s * 60);
               gg.rotX(-50.0F);
               PetGeo.cone(gg, horn, 0.26F, hl * 0.55F, 6, false);
               gg.pop();
            }
         }
      };
      r.headExtra = (gg, pp) -> {
         if (fire) {
            for (int i = 0; i < 4; i++) {
               float ph = PetRig.frac(pp.t * 2.2F + i * 0.25F);
               int al = (int)(230.0F * (1.0F - ph));
               PetGeo.sprite(
                  gg,
                  "pet_star",
                  PetGeo.sin(pp.t * 9.0F + i) * 0.3F,
                  -0.4F + ph * 0.6F,
                  3.6F + ph * 3.2F,
                  0.6F + ph * 1.2F,
                  al << 24 | (i % 2 == 0 ? 16751146 : 16765514)
               );
            }
         }
      };
      r.bodyExtra = (gg, pp) -> {
         for (int i = 0; i < 5; i++) {
            gg.push();
            gg.translate(0.0F, 2.2F - Math.abs(i - 1.5F) * 0.12F, 2.2F - i * 1.3F);
            gg.rotX(-25.0F);
            gg.scale(0.45F, 1.0F, 1.0F);
            PetGeo.cone(gg, spike, 0.5F, 0.75F + (lvl >= 4 ? 0.35F : 0.0F), 5, false);
            gg.pop();
         }

         float span = PetRig.lerp(2.4F, 6.2F, pp.gr) + (lvl >= 5 ? 1.8F : 0.0F);
         float spread = Math.max(Math.max(pp.runW, pp.is("excited") ? 1.0F : 0.0F), lvl >= 5 ? 0.35F + 0.65F * pp.walkW : 0.25F * pp.walkW);
         float[] fl = PetRig.flap(pp, 1.6F);
         float flap = fl[0] * 38.0F * spread;
         String wt = "pet_dwing:4E3A9E:" + (lvl >= 5 ? "E07AE8" : "B89AF5");

         for (int s = -1; s <= 1; s += 2) {
            gg.push();
            gg.translate(s * 1.3F, 1.9F, 1.6F);
            gg.scale(s, 1.0F, 1.0F);
            gg.rotY(PetRig.lerp(-70.0F, 5.0F, spread));
            gg.rotZ(PetRig.lerp(-8.0F, 22.0F, spread) + flap);
            gg.rotX(PetRig.lerp(70.0F, 88.0F, spread));
            gg.planeTL(wt, span, span * 0.72F, 0.0F, 0.0F, 1.0F, 1.0F);
            gg.pop();
         }
      };
      String b = Pets.col(8150230);
      String b2 = Pets.col(6966464);
      r.tail = (gg, pp) -> chain(
         gg, pp, new String[]{b, b, b2, b2, b2, b2}, new float[]{0.85F, 0.7F, 0.56F, 0.45F, 0.36F, 0.28F}, 1.3F, 3.0F, -14.0F, 0.9F, (g2, p2) -> {
            g2.push();
            g2.rotX(-90.0F);
            g2.scale(1.0F, 1.0F, 0.3F);
            PetGeo.cone(g2, spike, 0.7F, 1.2F, 4, false);
            g2.pop();
         }
      );
      real4(g, p, r);
   }

   static void eagle(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      float k = p.gr;
      float S = p.preview ? 1.0F : PetRig.lerp(0.72F, 1.4F, k);
      float fly = PetRig.smooth(0.15F, 0.5F, p.sp);
      float[] fl = PetRig.flap(p, 2.1F);
      float glide = PetRig.window(PetRig.frac(p.t / 3.1F), 0.45F, 0.95F, 0.1F) * p.runW;
      float amp = (p.is("sleepy") ? 12 : 42) * (1.0F - glide * 0.85F);
      float stretchW = PetRig.window(PetRig.frac(p.t / 14.0F), 0.62F, 0.76F, 0.04F) * p.idle * (p.is("sleepy") ? 0 : 1);
      float exc = p.is("excited") ? 1.0F : 0.0F;
      float spread = Math.max(fly, Math.max(stretchW, exc * 0.8F));
      p.stepBob = 0.0F;
      g.push();
      g.scale(S);
      float alt = fly * (6.5F + fl[1] * 0.9F * (1.0F - glide));
      g.translate(0.0F, alt, 0.0F);
      Pets.root(g, p);
      float legLen = PetRig.lerp(1.2F, 1.9F, k);
      String yel = Pets.gl(15906874);

      for (int s = -1; s <= 1; s += 2) {
         float c = PetRig.frac(p.t * 2.1F + (s > 0 ? 0.0F : 0.5F));
         float lift = (1.0F - fly) * p.walkW * Math.max(0.0F, PetGeo.sin((Math.PI * 2) * c)) * 0.5F;
         g.push();
         g.translate(s * 0.75F, legLen + 0.25F, -0.2F);
         g.rotX(fly * 70.0F - (1.0F - fly) * p.walkW * PetGeo.sin((Math.PI * 2) * c) * 22.0F);
         g.translate(0.0F, lift, 0.0F);
         g.cylinder(yel, 0.2F, 0.17F, -legLen, 0.0F, 5, false, 0.0F, 0.0F, 1.0F, 1.0F);
         g.translate(0.0F, -legLen, 0.0F);

         for (int t = -1; t <= 1; t++) {
            g.push();
            g.rotY(t * 28);
            PetGeo.blob(g, yel, 0.0F, -0.1F, 0.4F, 0.14F, 0.12F, 0.45F, 5, 2);
            g.pop();
         }

         g.pop();
      }

      float bodyUp = PetRig.lerp(40.0F, 4.0F, fly) + p.waddle * 0.0F;
      g.push();
      g.translate(0.0F, legLen + 1.6F, 0.0F);
      g.rotZ(p.waddle * (1.0F - fly) * 0.8F + p.roll);
      g.rotX(-bodyUp);
      float bR = PetRig.lerp(1.35F, 1.45F, k);
      PetGeo.blob(g, Pets.fur("eagle"), 0.0F, 0.0F, 0.0F, bR, bR * 1.05F, PetRig.lerp(2.0F, 2.7F, k), 11, 6);
      g.push();
      g.translate(0.0F, 0.1F, -PetRig.lerp(1.8F, 2.5F, k));
      g.rotX(80.0F - fly * 70.0F + p.tailLift * 0.3F);
      g.rotY(180.0F + p.tailSway * 0.5F);
      g.plane("pet_tailfan:F4F0E8:D8D0C0", 0.0F, -PetRig.lerp(1.0F, 1.5F, k), PetRig.lerp(1.9F, 2.6F, k), PetRig.lerp(2.0F, 3.0F, k), 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      float span = PetRig.lerp(3.2F, 7.4F, k);
      float chord = PetRig.lerp(1.9F, 3.1F, k);
      float fold = 1.0F - spread;

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * bR * 0.8F, bR * 0.55F, 0.9F);
         g.scale(s, 1.0F, 1.0F);
         float flap = fly > 0.01F ? fl[0] * amp * fly : 0.0F;
         float stretch = stretchW * (25.0F + PetGeo.sin(p.t * 3.0F) * 8.0F) + exc * (30.0F + PetGeo.sin(p.t * 12.0F) * 25.0F);
         g.rotY(80.0F * fold);
         g.rotZ(PetRig.lerp(-12.0F, 6.0F, spread) + flap + stretch - (p.is("sad") ? 10 : 0));
         g.rotX(90.0F - fold * 10.0F);
         g.planeTL("pet_feather:5B3B25:2A1A10", span * PetRig.lerp(0.62F, 1.0F, spread), chord, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      g.pop();
      g.push();
      float hy = legLen + 1.6F + PetRig.lerp(1.9F, 0.5F, fly);
      float hz = PetRig.lerp(0.9F, 2.9F, fly) * PetRig.lerp(1.0F, 1.2F, k);
      g.translate(0.0F, hy - fl[1] * 0.2F * fly, hz);
      Pets.headTurn(g, p);
      float hs = PetRig.lerp(1.45F, 1.0F, k);
      g.scale(hs);
      float rx = 1.15F;
      float ry = 1.1F;
      float rz = 1.2F;
      PetGeo.blob(g, Pets.fur("eagleh"), 0.0F, 0.0F, 0.0F, rx, ry, rz, 12, 8);
      String st = PetRig.style("eagle", k);
      boolean og = g.glow;
      g.color(-1).glow(false);
      PetGeo.decal(g, "pet_face:" + st + ":" + p.emo, 0.0F, 0.0F, 0.0F, rx, ry, rz, 0.0F, 0.25F, 1.15F, 0.0F, 0.0F, 1.0F, 0.6F, 1.035F);
      PetGeo.decal(g, Pets.eyes(st, p), 0.0F, 0.0F, 0.0F, rx, ry, rz, p.lookX * 0.08F, 0.25F + p.lookY * 0.05F, 1.15F, 0.0F, 0.0F, 1.0F, 0.6F, 1.05F);
      g.glow(og);
      g.push();
      g.translate(0.0F, -0.1F, rz * 0.8F);
      g.rotX(80.0F);
      g.scale(1.0F, 1.0F, 0.8F);
      PetGeo.cone(g, Pets.gl(15906874), 0.42F, 0.95F, 7, true);
      g.pop();
      g.push();
      g.translate(0.0F, -0.25F, rz * 0.8F + 0.85F);
      g.rotX(160.0F);
      PetGeo.cone(g, Pets.gl(3811872), 0.14F, 0.35F, 5, false);
      g.pop();
      float open = p.is("excited") ? 0.5F + 0.5F * Math.abs(PetGeo.sin(p.t * 7.0F)) : (p.is("angry") ? 0.6F : 0.0F);
      g.push();
      g.translate(0.0F, -0.35F, rz * 0.75F);
      g.rotX(95.0F + open * 25.0F);
      g.scale(1.0F, 1.0F, 0.5F);
      PetGeo.cone(g, Pets.gl(15247402), 0.32F, 0.6F, 6, false);
      g.pop();
      g.pop();
      Pets.fx(g, p, hy + ry * hs + 1.3F);
      g.pop();
      if (fly < 0.5F) {
         PetRig.sparkles(g, p, 3.2F);
      }
   }

   static final class R4 {
      String body;
      String head;
      String muzzle;
      String legUp;
      String legLo;
      String paw;
      String style = "wolf";
      int nose = 1972766;
      float size = 1.0F;
      float L = 5.0F;
      float legX = 1.3F;
      float zs = 3.0F;
      float zh = -3.0F;
      float rx = 2.0F;
      float ry = 2.2F;
      float hipF = 0.9F;
      float neckLen = 2.3F;
      float neckAng = 42.0F;
      float neckR = 1.2F;
      float skRx = 1.65F;
      float skRy = 1.55F;
      float skRz = 1.75F;
      float snLen = 2.0F;
      float snR = 0.8F;
      float snY = -0.45F;
      float headDown = 0.0F;
      float r1f = 0.7F;
      float r2f = 0.45F;
      float r1h = 1.0F;
      float r2h = 0.48F;
      float pawR = 0.55F;
      float pawLen = 1.3F;
      boolean hoof;
      boolean grazer;
      float span = 0.9F;
      float eyeEl = 0.22F;
      Pets.Part ears;
      Pets.Part tail;
      Pets.Part headExtra;
      Pets.Part bodyExtra;
      Pets.Part neckExtra;
      float babyS = 0.56F;
      float babyHead = 1.75F;
      float babyLeg = 0.5F;
      float babyLen = 0.72F;
      float babySnout = 0.45F;
      float babyNeck = 0.45F;
   }
}
