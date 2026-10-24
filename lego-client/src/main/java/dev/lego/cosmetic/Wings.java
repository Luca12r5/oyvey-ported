package dev.lego.cosmetic;

import java.util.ArrayList;
import java.util.List;

final class Wings {
   private Wings() {
   }

   static void register() {
      Cos.add("wings_angel", "Engelsflügel", Cos.Slot.WINGS, Cos.Rarity.LEGENDARY, (g, a) -> feathered(g, a, "angel", 16.0F, false, 0));
      Cos.add("wings_gold", "Goldflügel", Cos.Slot.WINGS, Cos.Rarity.LEGENDARY, (g, a) -> feathered(g, a, "gold", 16.0F, false, 0));
      Cos.add("wings_phoenix", "Phönixflügel", Cos.Slot.WINGS, Cos.Rarity.LEGENDARY, (g, a) -> feathered(g, a, "phoenix", 17.0F, true, 1));
      Cos.add("wings_fire", "Feuerflügel", Cos.Slot.WINGS, Cos.Rarity.EPIC, (g, a) -> feathered(g, a, "fire", 16.0F, true, 2));
      Cos.add("wings_shadow", "Schattenflügel", Cos.Slot.WINGS, Cos.Rarity.EPIC, (g, a) -> feathered(g, a, "shadow", 16.0F, false, 3));
      Cos.add("wings_owl", "Eulenflügel", Cos.Slot.WINGS, Cos.Rarity.RARE, (g, a) -> feathered(g, a, "owl", 15.0F, false, 0));
      Cos.add("wings_rainbow", "Regenbogenflügel", Cos.Slot.WINGS, Cos.Rarity.EPIC, (g, a) -> feathered(g, a, "rainbow", 15.0F, true, 4));
      Cos.add("wings_galaxy", "Galaxieflügel", Cos.Slot.WINGS, Cos.Rarity.LEGENDARY, (g, a) -> feathered(g, a, "galaxy", 17.0F, true, 5));
      Cos.add("wings_crystal", "Kristallflügel", Cos.Slot.WINGS, Cos.Rarity.LEGENDARY, (g, a) -> feathered(g, a, "crystal", 16.0F, true, 5));
      Cos.add("wings_demon", "Dämonenflügel", Cos.Slot.WINGS, Cos.Rarity.EPIC, (g, a) -> membrane(g, a, "demon", 18.0F, -12969456, 0, false));
      Cos.add("wings_dragon", "Drachenflügel", Cos.Slot.WINGS, Cos.Rarity.LEGENDARY, (g, a) -> membrane(g, a, "dragon", 20.0F, -10868214, 0, false));
      Cos.add(
         "wings_icedragon", "Eisdrachenflügel", Cos.Slot.WINGS, Cos.Rarity.LEGENDARY, (g, a) -> membrane(g, a, "icedragon", 19.0F, -4198401, -7673601, true)
      );
      Cos.add("wings_neon", "Neonflügel", Cos.Slot.WINGS, Cos.Rarity.EPIC, (g, a) -> membrane(g, a, "neon", 17.0F, -15070678, -42256, true));
      Cos.add("wings_bat", "Fledermausflügel", Cos.Slot.WINGS, Cos.Rarity.RARE, (g, a) -> membrane(g, a, "bat", 15.0F, -14017514, 0, false));
      Cos.add("wings_butterfly", "Schmetterling", Cos.Slot.WINGS, Cos.Rarity.EPIC, HdWings::butterfly);
      Cos.add("wings_fairy", "Feenflügel", Cos.Slot.WINGS, Cos.Rarity.EPIC, HdWings::fairy);
      Cos.add("wings_cyber", "Cyberflügel", Cos.Slot.WINGS, Cos.Rarity.LEGENDARY, HdWings::cyber);
   }

   static Wings.Pose pose(Cos.A a, float flapScale) {
      Wings.Pose p = new Wings.Pose();
      float t = a.time;
      float mv = a.preview ? 0.0F : Math.max(0.0F, Math.min(1.0F, a.move));
      float fold = a.sneak ? 1.0F : (a.preview ? 0.0F : 0.22F * (1.0F - mv));
      float slow = Geo.sin(t * 1.6) * 5.0F + Geo.sin(t * 0.7 + 1.0) * 3.0F;
      float fast = Geo.sin(t * 7.5) * 26.0F;
      float flap = (slow * (1.0F - mv) + fast * mv) * flapScale * (a.sneak ? 0.35F : 1.0F);
      p.raise = Geo.lerp(14.0F, -62.0F, fold) + flap + mv * 4.0F;
      p.sweep = Geo.lerp(26.0F, 8.0F, fold) - mv * 10.0F + flap * 0.3F;
      p.fold = fold;
      p.flap = flap;
      p.t = t;
      p.mv = mv;
      return p;
   }

   private static void root(G g, Wings.Pose p, int side, float rootX, float rootY) {
      g.scale(side, 1.0F, 1.0F);
      g.translate(rootX, rootY, -2.35F);
      g.rotY(p.sweep);
      g.rotZ(p.raise);
      g.rotX(-8.0F);
   }

   static void feathered(G g, Cos.A a, String style, float S, boolean glow, int fx) {
      Wings.Pose p = pose(a, 1.0F);
      String fea = "fea_" + style;
      String cov = "cov_" + style;

      for (int side = 1; side >= -1; side -= 2) {
         g.push();
         root(g, p, side, 0.9F, -2.2F);
         g.glow(glow);
         float fold = p.fold;
         float spanK = 1.0F - 0.18F * fold;
         float bend = 3.5F + p.flap * 0.18F;
         int np = 9;

         for (int i = 0; i < np; i++) {
            float f = (float)i / (np - 1);
            float s = 0.58F + 0.42F * f;
            float th = Geo.lerp(16.0F, 50.0F, f) * (1.0F - 0.65F * fold);
            float L = S * (0.74F + 0.26F * Geo.sin(Math.PI * (0.3 + 0.55 * f))) * (fx == 2 ? 0.9F + 0.12F * Geo.sin(p.t * 9.0F + i * 1.7) : 1.0F);
            tint(g, fx, s, 0.86F, p.t);
            feather(g, fea, edge(S, s, spanK, 0.1F + i * 0.012F), th, L, S * 0.17F, 4, bend);
         }

         int ns = 11;

         for (int i = 0; i < ns; i++) {
            float f = (float)i / (ns - 1);
            float s = 0.03F + 0.57F * f;
            float th = Geo.lerp(-8.0F, 16.0F, f) * (1.0F - 0.5F * fold);
            float L = S * (0.54F + 0.14F * f) * (fx == 2 ? 0.9F + 0.12F * Geo.sin(p.t * 8.0F + i * 2.1) : 1.0F);
            tint(g, fx, s, 0.93F, p.t);
            feather(g, fea, edge(S, s, spanK, 0.0F + i * 0.01F), th, L, S * 0.16F, 3, bend * 0.8F);
         }

         int nc = 13;

         for (int i = 0; i < nc; i++) {
            float f = (float)i / (nc - 1);
            float s = 0.02F + 0.9F * f;
            float th = (f < 0.62F ? Geo.lerp(-6.0F, 16.0F, f / 0.62F) : Geo.lerp(18.0F, 55.0F, (f - 0.62F) / 0.38F)) * (1.0F - 0.55F * fold);
            float L = S * (0.34F - 0.1F * f);
            tint(g, fx, s, 1.0F, p.t);
            feather(g, cov, edge(S, s, spanK, -0.22F), th, L, S * 0.15F, 2, bend * 0.5F);
         }

         int nm = 14;

         for (int i = 0; i < nm; i++) {
            float f = (float)i / (nm - 1);
            float s = 0.0F + 0.96F * f;
            float th = Geo.lerp(0.0F, 40.0F, f) * (1.0F - 0.5F * fold);
            tint(g, fx, s, 1.0F, p.t);
            float[] e = edge(S, s, spanK, -0.42F);
            e[1] += 0.35F;
            feather(g, cov, e, th, S * (0.19F - 0.05F * f), S * 0.13F, 1, 0.0F);
         }

         int m = 10;
         float[][] arm = new float[m + 1][];
         float[] rr = new float[m + 1];
         float[] rz = new float[m + 1];

         for (int i = 0; i <= m; i++) {
            float f = (float)i / m;
            float[] e = edge(S, f * 0.97F, spanK, -0.3F);
            arm[i] = new float[]{e[0], e[1] + 0.2F, e[2]};
            rr[i] = S * 0.05F * (1.2F - f * 0.75F);
            rz[i] = S * 0.045F * (1.2F - f * 0.75F);
         }

         tint(g, fx, 0.5F, 1.0F, p.t);
         XmasGeo.sweep(g, cov, arm, rr, rz, null, 6, 1.0F);
         g.color(-1);
         if (fx != 1 && fx != 2) {
            if (fx == 3) {
               g.glow(true);

               for (int i = 0; i < 6; i++) {
                  float ph = (p.t * 0.6F + i / 6.0F) % 1.0F;
                  float s = 0.2F + 0.75F * (i * 0.47F % 1.0F);
                  rz = edge(S, s, spanK, 0.4F);
                  float L = S * 0.7F + ph * 6.0F;
                  g.color(Geo.argb((int)(140.0 * Math.sin(ph * Math.PI)), 16777215));
                  Geo.sprite(g, "orb_purple", rz[0] + s * 4.0F, rz[1] - L, rz[2] - 0.5F, 3.0F + ph * 3.0F);
               }
            } else if (fx == 5) {
               g.glow(true);

               for (int i = 0; i < 8; i++) {
                  float tw = 0.5F + 0.5F * Geo.sin(p.t * 3.0F + i * 2.3F);
                  float s = i * 0.37F % 1.0F;
                  rz = edge(S, 0.1F + 0.85F * s, spanK, -0.8F);
                  g.color(Geo.argb((int)(255.0F * tw), 16777215));
                  Geo.sprite(g, "spark", rz[0] + 2.0F, rz[1] - S * (0.15F + 0.4F * (i * 0.53F % 1.0F)), rz[2] - 0.6F, 1.2F + 1.6F * tw);
               }
            }
         } else {
            g.glow(true);

            for (int i = 0; i < 7; i++) {
               float ph = (p.t * 1.3F + i * 0.37F) % 1.0F;
               float s = 0.25F + 0.7F * (i * 0.61F % 1.0F);
               rz = edge(S, s, spanK, 0.3F);
               float th = (float)Math.toRadians(Geo.lerp(-8.0F, 60.0F, s));
               float L = S * (0.55F + 0.35F * s) + ph * 5.0F;
               float x = rz[0] + (float)Math.sin(th) * L;
               float y = rz[1] - (float)Math.cos(th) * L - ph * 3.0F;
               g.color(Geo.argb((int)(230.0 * Math.sin(ph * Math.PI)), 16777215));
               Geo.sprite(g, "flame", x, y + 1.5F, rz[2], 3.2F * (1.0F - ph * 0.5F));
            }
         }

         g.color(-1).glow(false);
         g.pop();
      }
   }

   private static void tint(G g, int fx, float f, float bright, float t) {
      if (fx == 4) {
         int c = Geo.hue(f * 0.75 + t * 0.12, 0.5, bright);
         g.color(c);
      } else {
         int v = (int)(255.0F * bright);
         g.color(0xFF000000 | v << 16 | v << 8 | v);
      }
   }

   private static float[] edge(float S, float s, float spanK, float z) {
      float x = S * s * spanK;
      float y = S * (0.26F * Geo.sin(Math.PI * s * 0.92) + 0.1F * s);
      float zz = -S * 0.09F * Geo.sin(Math.PI * s) + z;
      return new float[]{x, y, zz};
   }

   private static void feather(G g, String tex, float[] p, float th, float len, float w, int segs, float bend) {
      g.push();
      g.translate(p[0], p[1], p[2]);
      g.rotZ(th);
      g.rotY(-6.0F);
      int n = segs + (segs >= 3 ? 1 : 0);
      float l = len / n;
      float b = bend * segs / n;
      float cz = w * 0.12F;

      for (int j = 0; j < n; j++) {
         float v0 = (float)j / n;
         float v1 = (float)(j + 1) / n;
         float w0 = w * (j == n - 1 && n > 1 ? 1.0F : 1.0F);
         float w1 = w * (j == n - 1 ? 0.55F : 1.0F);
         g.quad(
            tex,
            new float[]{-w0 / 2.0F, 0.0F, -cz * 0.3F},
            new float[]{0.0F, 0.0F, cz},
            new float[]{0.0F, -l, cz},
            new float[]{-w1 / 2.0F, -l, -cz * 0.3F},
            new float[]{0.0F, v0, 0.5F, v0, 0.5F, v1, 0.5F - 0.5F * w1 / w, v1}
         );
         g.quad(
            tex,
            new float[]{0.0F, 0.0F, cz},
            new float[]{w0 / 2.0F, 0.0F, -cz * 0.3F},
            new float[]{w1 / 2.0F, -l, -cz * 0.3F},
            new float[]{0.0F, -l, cz},
            new float[]{0.5F, v0, 1.0F, v0, 0.5F + 0.5F * w1 / w, v1, 0.5F, v1}
         );
         g.translate(0.0F, -l, 0.0F);
         g.rotX(-b);
      }

      g.pop();
   }

   private static float[][] memPoints(float S, float fold, float flap) {
      float[] O = new float[]{0.0F, 0.0F, 0.0F};
      float a1 = (float)Math.toRadians(34.0F - fold * 10.0F);
      float[] E = new float[]{O[0] + 0.42F * S * Geo.cos(a1), O[1] + 0.42F * S * Geo.sin(a1), 0.0F};
      float a2 = (float)Math.toRadians(-6.0F - fold * 22.0F);
      float[] W = new float[]{E[0] + 0.44F * S * Geo.cos(a2), E[1] + 0.44F * S * Geo.sin(a2), -0.6F};
      float[] ang = new float[]{30.0F, -6.0F, -40.0F, -72.0F};
      float[] len = new float[]{0.64F, 0.74F, 0.62F, 0.5F};
      float[][] out = new float[][]{O, E, W, null, null, null, null};

      for (int k = 0; k < 4; k++) {
         float an = (float)Math.toRadians(Geo.lerp(ang[k], -95.0F, fold * 0.35F) - flap * 0.1F * k);
         float L = len[k] * S * (1.0F - 0.15F * fold);
         out[3 + k] = new float[]{W[0] + L * Geo.cos(an), W[1] + L * Geo.sin(an), W[2] - 0.12F * L};
      }

      return out;
   }

   static void membrane(G g, Cos.A a, String style, float S, int boneColor, int edgeGlow, boolean glowMem) {
      Wings.Pose p = pose(a, 1.2F);
      float[][] ref = memPoints(S, 0.0F, 0.0F);
      float[][] pt = memPoints(S, p.fold, p.flap);
      float[] B = new float[]{0.4F, -0.8F * S, 0.2F};
      float[] Bref = B;
      String mem = "mem_" + style;
      float minX = -0.05F * S;
      float maxX = 1.35F * S;
      float maxY = 0.62F * S;
      float minY = -0.95F * S;

      for (int side = 1; side >= -1; side -= 2) {
         g.push();
         root(g, p, side, 1.0F, -1.6F);
         List<float[]> e = new ArrayList<>();
         List<float[]> er = new ArrayList<>();

         for (int k = 0; k < 4; k++) {
            float[] ta = pt[3 + k];
            float[] tb = k < 3 ? pt[4 + k] : B;
            float[] ra = ref[3 + k];
            float[] rb = k < 3 ? ref[4 + k] : Bref;
            int sub = 5;

            for (int i = 0; i < sub; i++) {
               float t = (float)i / sub;
               e.add(scallop(ta, tb, pt[2], t, k < 3 ? 0.26F : 0.12F));
               er.add(scallop(ra, rb, ref[2], t, k < 3 ? 0.26F : 0.12F));
            }
         }

         e.add(B);
         er.add(Bref);
         e.add(pt[0]);
         er.add(ref[0]);
         e.add(pt[1]);
         er.add(ref[1]);
         float[] W = pt[2];
         float[] Wr = ref[2];
         g.glow(glowMem);
         g.color(-1);

         for (int i = 0; i + 1 < e.size(); i++) {
            float[] uw = uv(Wr, minX, maxX, minY, maxY);
            float[] u1 = uv(er.get(i), minX, maxX, minY, maxY);
            float[] u2 = uv(er.get(i + 1), minX, maxX, minY, maxY);
            Geo.tri(g, mem, W, e.get(i), e.get(i + 1), uw, u1, u2);
         }

         g.glow(false);
         g.color(boneColor);
         Geo.tube(g, "bone", pt[0], pt[1], 0.62F, 0.5F, 6);
         Geo.tube(g, "bone", pt[1], pt[2], 0.5F, 0.38F, 6);

         for (int k = 0; k < 4; k++) {
            float[] tip = pt[3 + k];
            float[] mid = new float[]{Geo.lerp(W[0], tip[0], 0.5F), Geo.lerp(W[1], tip[1], 0.5F) + 0.4F, Geo.lerp(W[2], tip[2], 0.5F) - 0.25F};
            Geo.tube(g, "bone", W, mid, 0.34F, 0.24F, 5);
            Geo.tube(g, "bone", mid, tip, 0.24F, 0.06F, 5);
         }

         g.push();
         g.translate(pt[1][0], pt[1][1], pt[1][2]);
         g.sphere("bone", 0.6F, 6, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
         g.push();
         g.translate(W[0], W[1], W[2]);
         g.sphere("bone", 0.5F, 6, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
         g.color(-1515312);
         Geo.tube(g, "horn", W, new float[]{W[0] + 0.6F, W[1] + 1.9F, W[2] - 0.2F}, 0.34F, 0.02F, 5);
         if (edgeGlow != 0) {
            g.glow(true);
            g.color(edgeGlow);

            for (int i = 0; i + 1 < e.size() - 3; i++) {
               Geo.tube(g, "white", e.get(i), e.get(i + 1), 0.16F, 0.16F, 4);
            }

            for (int k = 0; k < 4; k++) {
               Geo.tube(g, "white", W, pt[3 + k], 0.1F, 0.05F, 4);
            }
         }

         g.color(-1).glow(false);
         g.pop();
      }
   }

   private static float[] scallop(float[] a, float[] b, float[] w, float t, float pull) {
      float mx = (a[0] + b[0]) / 2.0F;
      float my = (a[1] + b[1]) / 2.0F;
      float mz = (a[2] + b[2]) / 2.0F;
      float cx = mx + (w[0] - mx) * pull;
      float cy = my + (w[1] - my) * pull;
      float cz = mz + (w[2] - mz) * pull;
      float u = 1.0F - t;
      return new float[]{
         u * u * a[0] + 2.0F * u * t * cx + t * t * b[0], u * u * a[1] + 2.0F * u * t * cy + t * t * b[1], u * u * a[2] + 2.0F * u * t * cz + t * t * b[2]
      };
   }

   private static float[] uv(float[] p, float minX, float maxX, float minY, float maxY) {
      return new float[]{(p[0] - minX) / (maxX - minX), (maxY - p[1]) / (maxY - minY)};
   }

   static void butterfly(G g, Cos.A a) {
      float t = a.time;
      float mv = a.preview ? 0.0F : a.move;
      float flutter = a.sneak ? 72.0F : Geo.lerp(18.0F + 22.0F * (0.5F + 0.5F * Geo.sin(t * 2.6)), 20.0F + 45.0F * (0.5F + 0.5F * Geo.sin(t * 12.0F)), mv);

      for (int side = 1; side >= -1; side -= 2) {
         g.push();
         g.scale(side, 1.0F, 1.0F);
         g.translate(0.5F, 2.5F, -2.4F);
         g.rotY(flutter);
         g.rotZ(8.0F);
         grid(g, "wing_butterfly2", 13.5F, 17.0F, 5, 6, 0.014F, true);
         g.pop();
      }

      g.color(-15396836);
      Geo.tube(g, "felt_black", new float[]{0.0F, 2.5F, -2.55F}, new float[]{0.0F, -9.0F, -2.55F}, 0.7F, 0.4F, 6);
      g.color(-1);
   }

   private static void grid(G g, String tex, float w, float h, int nx, int ny, float curve, boolean tl) {
      for (int j = 0; j < ny; j++) {
         for (int i = 0; i < nx; i++) {
            float u0 = (float)i / nx;
            float u1 = (float)(i + 1) / nx;
            float v0 = (float)j / ny;
            float v1 = (float)(j + 1) / ny;
            g.quad(
               tex,
               gp(w, h, u0, v0, curve),
               gp(w, h, u1, v0, curve),
               gp(w, h, u1, v1, curve),
               gp(w, h, u0, v1, curve),
               new float[]{u0, v0, u1, v0, u1, v1, u0, v1}
            );
         }
      }
   }

   private static float[] gp(float w, float h, float u, float v, float curve) {
      float x = u * w;
      float y = -v * h;
      return new float[]{x, y, -curve * (x * x + y * y * 0.4F)};
   }

   static void fairy(G g, Cos.A a) {
      float t = a.time;
      float mv = a.preview ? 0.0F : a.move;
      float fl = a.sneak ? 70.0F : Geo.lerp(22.0F + 18.0F * (0.5F + 0.5F * Geo.sin(t * 3.4)), 20.0F + 40.0F * (0.5F + 0.5F * Geo.sin(t * 16.0F)), mv);
      g.glow(true);

      for (int side = 1; side >= -1; side -= 2) {
         for (int lobe = 0; lobe < 2; lobe++) {
            g.push();
            g.scale(side, 1.0F, 1.0F);
            g.translate(0.6F, lobe == 0 ? -1.5F : -4.0F, -2.4F);
            g.rotY(fl + (lobe == 0 ? 0 : 8) + Geo.sin(t * 3.4 + lobe) * 3.0F);
            g.rotZ(lobe == 0 ? 28.0F : -34.0F);
            float w = lobe == 0 ? 17.0F : 12.0F;
            float h = lobe == 0 ? 8.5F : 6.0F;
            g.color(-419430401);

            for (int i = 0; i < 4; i++) {
               float u0 = i / 4.0F;
               float u1 = (i + 1) / 4.0F;
               float z0 = -0.012F * (u0 * w) * (u0 * w);
               float z1 = -0.012F * (u1 * w) * (u1 * w);
               g.quad(
                  "wing_fairy",
                  new float[]{u0 * w, h / 2.0F, z0},
                  new float[]{u1 * w, h / 2.0F, z1},
                  new float[]{u1 * w, -h / 2.0F, z1},
                  new float[]{u0 * w, -h / 2.0F, z0},
                  new float[]{u0, 0.0F, u1, 0.0F, u1, 1.0F, u0, 1.0F}
               );
            }

            g.pop();
         }
      }

      for (int i = 0; i < 10; i++) {
         float ph = (t * 0.5F + i / 10.0F) % 1.0F;
         float x = Geo.sin(i * 2.7F) * 9.0F;
         float z = -4.0F - i % 3 * 1.5F;
         g.color(Geo.argb((int)(255.0 * Math.sin(ph * Math.PI)), 16777215));
         Geo.sprite(g, i % 2 == 0 ? "spark" : "orb_cyan", x + Geo.sin(t * 2.0F + i) * 1.2F, -2.0F - ph * 16.0F, z, 1.6F * (1.0F - ph * 0.5F));
      }

      g.color(-1).glow(false);
   }

   static void cyber(G g, Cos.A a) {
      Wings.Pose p = pose(a, 0.8F);
      float S = 17.0F;

      for (int side = 1; side >= -1; side -= 2) {
         g.push();
         root(g, p, side, 1.0F, -2.0F);
         float[] O = new float[]{0.0F, 0.0F, 0.0F};
         float[] E = new float[]{S * 0.4F, S * 0.22F, -0.3F};
         float[] W = new float[]{S * 0.82F * (1.0F - 0.15F * p.fold), S * 0.26F, -0.8F};
         g.color(-1);
         Geo.tube(g, "mech_frame", O, E, 0.9F, 0.75F, 6);
         Geo.tube(g, "mech_frame", E, W, 0.75F, 0.55F, 6);
         g.glow(true);
         g.color(-13965569);
         Geo.tube(g, "white", new float[]{O[0], O[1] + 0.8F, O[2]}, new float[]{E[0], E[1] + 0.7F, E[2]}, 0.14F, 0.14F, 4);
         Geo.tube(g, "white", new float[]{E[0], E[1] + 0.7F, E[2]}, new float[]{W[0], W[1] + 0.5F, W[2]}, 0.14F, 0.14F, 4);
         Geo.sprite(g, "orb_cyan", E[0], E[1], E[2] - 0.8F, 2.6F + 0.6F * Geo.sin(p.t * 4.0F));
         Geo.sprite(g, "orb_cyan", W[0], W[1], W[2] - 0.8F, 2.2F + 0.6F * Geo.sin(p.t * 4.0F + 1.0F));
         g.glow(false);
         int n = 8;

         for (int i = 0; i < n; i++) {
            float f = (float)i / (n - 1);
            float[] at = new float[]{Geo.lerp(1.5F, W[0], f), Geo.lerp(0.5F, W[1], f) - 0.3F, Geo.lerp(0.0F, W[2], f) - 0.2F + i * 0.05F};
            float th = Geo.lerp(-4.0F, 62.0F, f) * (1.0F - 0.6F * p.fold) + Geo.sin(p.t * 2.0F + i) * 1.5F;
            float L = S * (0.5F + 0.42F * Geo.sin(Math.PI * (0.25 + 0.6 * f)));
            g.push();
            g.translate(at[0], at[1], at[2]);
            g.rotZ(th);
            g.rotY(-8.0F);
            float w = S * 0.14F;
            float[][] uvs = new float[][]{
               {0.0F, 0.0F, 1.0F, 1.0F},
               {0.0F, 0.0F, 1.0F, 1.0F},
               {0.0F, 0.0F, 0.1F, 1.0F},
               {0.9F, 0.0F, 1.0F, 1.0F},
               {0.0F, 0.0F, 1.0F, 0.05F},
               {0.0F, 0.95F, 1.0F, 1.0F}
            };
            g.color(-1);
            g.box6("mech_blade", -w / 2.0F, -L, -0.18F, w / 2.0F, 0.0F, 0.18F, uvs);
            g.glow(true);
            g.color(Geo.argb(200 + (int)(55.0F * Geo.sin(p.t * 5.0F + i)), 2811647));
            g.quad(
               "glow_line",
               new float[]{-w / 2.0F, -0.3F, -0.2F},
               new float[]{w / 2.0F, -0.3F, -0.2F},
               new float[]{w / 2.0F, -0.8F, -0.2F},
               new float[]{-w / 2.0F, -0.8F, -0.2F},
               new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
            );
            g.glow(false);
            g.pop();
         }

         g.color(-1);
         g.pop();
      }

      g.color(-1);
      g.box("mech_frame", -2.2F, -5.5F, -3.3F, 2.2F, -0.8F, -2.05F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.glow(true);
      g.color(-13965569);
      g.push();
      g.translate(0.0F, 0.0F, -3.36F);
      g.plane("glow_line", 0.0F, -3.1F, 3.4F, 0.7F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.glow(false);
      g.color(-1);
   }

   static final class Pose {
      float raise;
      float sweep;
      float fold;
      float flap;
      float t;
      float mv;
   }
}
