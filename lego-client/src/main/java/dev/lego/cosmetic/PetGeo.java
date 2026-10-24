package dev.lego.cosmetic;

final class PetGeo {
   private PetGeo() {
   }

   static float sin(double a) {
      return (float)Math.sin(a);
   }

   static float cos(double a) {
      return (float)Math.cos(a);
   }

   static void blob(G g, String tex, float x, float y, float z, float rx, float ry, float rz, int seg, int rings) {
      g.push();
      g.translate(x, y, z);
      g.scale(rx, ry, rz);
      g.sphere(tex, 1.0F, Math.max(8, seg), Math.max(4, rings), 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
   }

   static void blob2(G g, String tex, float x, float y, float z, float rx, float ryUp, float ryDown, float rz, int seg, int rings) {
      float[][] pt = new float[(rings + 1) * (seg + 1)][];

      for (int i = 0; i <= rings; i++) {
         double t = Math.PI * i / rings - (Math.PI / 2);
         float yy = (float)Math.sin(t);
         float cr = (float)Math.cos(t);
         yy *= yy > 0.0F ? ryUp : ryDown;

         for (int j = 0; j <= seg; j++) {
            double a = (Math.PI * 2) * j / seg;
            pt[i * (seg + 1) + j] = new float[]{x + (float)Math.cos(a) * cr * rx, y + yy, z + (float)Math.sin(a) * cr * rz};
         }
      }

      for (int i = 0; i < rings; i++) {
         for (int j = 0; j < seg; j++) {
            float u0 = (float)j / seg;
            float u1 = (float)(j + 1) / seg;
            float v0 = 1.0F - (float)i / rings;
            float v1 = 1.0F - (float)(i + 1) / rings;
            g.quad(
               tex,
               pt[(i + 1) * (seg + 1) + j],
               pt[(i + 1) * (seg + 1) + j + 1],
               pt[i * (seg + 1) + j + 1],
               pt[i * (seg + 1) + j],
               new float[]{u0, v1, u1, v1, u1, v0, u0, v0}
            );
         }
      }
   }

   static void cone(G g, String tex, float r, float h, int seg, boolean cap) {
      for (int i = 0; i < seg; i++) {
         double a0 = (Math.PI * 2) * i / seg;
         double a1 = (Math.PI * 2) * (i + 1) / seg;
         float c0 = cos(a0);
         float s0 = sin(a0);
         float c1 = cos(a1);
         float s1 = sin(a1);
         float u0 = (float)i / seg;
         float u1 = (float)(i + 1) / seg;
         float[] top = new float[]{0.0F, h, 0.0F};
         g.quad(tex, top, top, new float[]{c1 * r, 0.0F, s1 * r}, new float[]{c0 * r, 0.0F, s0 * r}, new float[]{u0, 0.0F, u1, 0.0F, u1, 1.0F, u0, 1.0F});
         if (cap) {
            float[] m = new float[]{0.0F, 0.0F, 0.0F};
            g.quad(tex, m, new float[]{c0 * r, 0.0F, s0 * r}, new float[]{c1 * r, 0.0F, s1 * r}, m, new float[]{0.5F, 0.9F, u0, 1.0F, u1, 1.0F, 0.5F, 0.9F});
         }
      }
   }

   static void tri(G g, String tex, float[] a, float[] b, float[] c) {
      g.quad(tex, a, b, c, c, new float[]{0.5F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F});
   }

   static void rbox(G g, String tex, float x0, float y0, float z0, float x1, float y1, float z1, float b) {
      float[] lo = new float[]{x0 + b, y0 + b, z0 + b};
      float[] hi = new float[]{x1 - b, y1 - b, z1 - b};
      float[] full = new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F};
      float[] mid = new float[]{0.3F, 0.3F, 0.7F, 0.3F, 0.7F, 0.7F, 0.3F, 0.7F};
      int[][] sq = new int[][]{{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};

      for (int ax = 0; ax < 3; ax++) {
         for (int s = -1; s <= 1; s += 2) {
            int u = (ax + 1) % 3;
            int v = (ax + 2) % 3;
            float[][] pts = new float[4][];

            for (int k = 0; k < 4; k++) {
               float[] p = new float[3];
               p[ax] = (s < 0 ? lo[ax] : hi[ax]) + s * b;
               p[u] = sq[k][0] < 0 ? lo[u] : hi[u];
               p[v] = sq[k][1] < 0 ? lo[v] : hi[v];
               pts[k] = p;
            }

            g.quad(tex, pts[0], pts[1], pts[2], pts[3], full);
         }
      }

      for (int a = 0; a < 3; a++) {
         for (int c = a + 1; c < 3; c++) {
            int t = 3 - a - c;

            for (int sa = -1; sa <= 1; sa += 2) {
               for (int sc = -1; sc <= 1; sc += 2) {
                  float[][] pts = new float[4][];
                  int[] tt = new int[]{-1, 1, 1, -1};

                  for (int k = 0; k < 4; k++) {
                     float[] p = new float[3];
                     p[a] = sa < 0 ? lo[a] : hi[a];
                     p[c] = sc < 0 ? lo[c] : hi[c];
                     p[t] = tt[k] < 0 ? lo[t] : hi[t];
                     if (k < 2) {
                        p[a] += sa * b;
                     } else {
                        p[c] += sc * b;
                     }

                     pts[k] = p;
                  }

                  g.quad(tex, pts[0], pts[1], pts[2], pts[3], mid);
               }
            }
         }
      }

      for (int sx = -1; sx <= 1; sx += 2) {
         for (int sy = -1; sy <= 1; sy += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
               float px = sx < 0 ? lo[0] : hi[0];
               float py = sy < 0 ? lo[1] : hi[1];
               float pz = sz < 0 ? lo[2] : hi[2];
               tri(g, tex, new float[]{px + sx * b, py, pz}, new float[]{px, py + sy * b, pz}, new float[]{px, py, pz + sz * b});
            }
         }
      }
   }

   static void decal(
      G g,
      String tex,
      float cx,
      float cy,
      float cz,
      float rx,
      float ry,
      float rz,
      float az0,
      float el0,
      float spanAz,
      float u0,
      float v0,
      float u1,
      float v1,
      float lift
   ) {
      decal(g, tex, cx, cy, cz, rx, ry, rz, az0, el0, spanAz, u0, v0, u1, v1, lift, 6, 5);
   }

   static void decal(
      G g,
      String tex,
      float cx,
      float cy,
      float cz,
      float rx,
      float ry,
      float rz,
      float az0,
      float el0,
      float spanAz,
      float u0,
      float v0,
      float u1,
      float v1,
      float lift,
      int nu,
      int nv
   ) {
      float spanEl = spanAz * ((v1 - v0) * 192.0F) / ((u1 - u0) * 256.0F) * rx / ry;
      float[][] pts = new float[(nu + 1) * (nv + 1)][];

      for (int j = 0; j <= nv; j++) {
         for (int i = 0; i <= nu; i++) {
            double az = az0 + spanAz * (1.0 - 2.0 * i / nu);
            double elv = el0 + spanEl * (1.0 - 2.0 * j / nv);
            pts[j * (nu + 1) + i] = new float[]{
               cx + (float)(Math.sin(az) * Math.cos(elv)) * rx * lift,
               cy + (float)Math.sin(elv) * ry * lift,
               cz + (float)(Math.cos(az) * Math.cos(elv)) * rz * lift
            };
         }
      }

      for (int j = 0; j < nv; j++) {
         for (int i = 0; i < nu; i++) {
            float ua = u0 + (u1 - u0) * i / nu;
            float ub = u0 + (u1 - u0) * (i + 1) / nu;
            float va = v0 + (v1 - v0) * j / nv;
            float vb = v0 + (v1 - v0) * (j + 1) / nv;
            g.quad(
               tex,
               pts[j * (nu + 1) + i],
               pts[j * (nu + 1) + i + 1],
               pts[(j + 1) * (nu + 1) + i + 1],
               pts[(j + 1) * (nu + 1) + i],
               new float[]{ua, va, ub, va, ub, vb, ua, vb}
            );
         }
      }
   }

   static void flat(G g, String tex, float cx, float cy, float z, float w, float h) {
      float x0 = cx + w / 2.0F;
      float x1 = cx - w / 2.0F;
      float y0 = cy + h / 2.0F;
      float y1 = cy - h / 2.0F;
      g.quad(
         tex,
         new float[]{x0, y0, z},
         new float[]{x1, y0, z},
         new float[]{x1, y1, z},
         new float[]{x0, y1, z},
         new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
      );
   }

   static void sprite(G g, String tex, float x, float y, float z, float s, int argb) {
      int oc = g.color;
      boolean og = g.glow;
      g.color(argb).glow(true);
      g.push();
      g.translate(x, y, z);
      g.rotY(25.0F);
      g.plane(tex, 0.0F, 0.0F, s, s, 0.0F, 0.0F, 1.0F, 1.0F);
      g.rotY(90.0F);
      g.plane(tex, 0.0F, 0.0F, s, s, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.color(oc).glow(og);
   }
}
