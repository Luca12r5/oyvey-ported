package dev.lego.cosmetic;

import java.util.ArrayDeque;

public final class G {
   private final G.Sink sink;
   private float[] m = identity();
   private final ArrayDeque<float[]> stack = new ArrayDeque<>();
   public int color = -1;
   public boolean glow = false;

   public G(G.Sink sink) {
      this.sink = sink;
   }

   private static float[] identity() {
      return new float[]{1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F};
   }

   public void push() {
      this.stack.push((float[])this.m.clone());
   }

   public void pop() {
      this.m = this.stack.pop();
   }

   private void mul(float[] b) {
      float[] r = new float[16];

      for (int i = 0; i < 4; i++) {
         for (int j = 0; j < 4; j++) {
            float s = 0.0F;

            for (int k = 0; k < 4; k++) {
               s += this.m[i * 4 + k] * b[k * 4 + j];
            }

            r[i * 4 + j] = s;
         }
      }

      this.m = r;
   }

   public G translate(float x, float y, float z) {
      this.mul(new float[]{1.0F, 0.0F, 0.0F, x, 0.0F, 1.0F, 0.0F, y, 0.0F, 0.0F, 1.0F, z, 0.0F, 0.0F, 0.0F, 1.0F});
      return this;
   }

   public G scale(float x, float y, float z) {
      this.mul(new float[]{x, 0.0F, 0.0F, 0.0F, 0.0F, y, 0.0F, 0.0F, 0.0F, 0.0F, z, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F});
      return this;
   }

   public G scale(float s) {
      return this.scale(s, s, s);
   }

   public G rotX(float deg) {
      float c = (float)Math.cos(Math.toRadians(deg));
      float s = (float)Math.sin(Math.toRadians(deg));
      this.mul(new float[]{1.0F, 0.0F, 0.0F, 0.0F, 0.0F, c, -s, 0.0F, 0.0F, s, c, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F});
      return this;
   }

   public G rotY(float deg) {
      float c = (float)Math.cos(Math.toRadians(deg));
      float s = (float)Math.sin(Math.toRadians(deg));
      this.mul(new float[]{c, 0.0F, s, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, -s, 0.0F, c, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F});
      return this;
   }

   public G rotZ(float deg) {
      float c = (float)Math.cos(Math.toRadians(deg));
      float s = (float)Math.sin(Math.toRadians(deg));
      this.mul(new float[]{c, -s, 0.0F, 0.0F, s, c, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F});
      return this;
   }

   private float[] tp(float x, float y, float z) {
      return new float[]{
         this.m[0] * x + this.m[1] * y + this.m[2] * z + this.m[3],
         this.m[4] * x + this.m[5] * y + this.m[6] * z + this.m[7],
         this.m[8] * x + this.m[9] * y + this.m[10] * z + this.m[11]
      };
   }

   public G color(int argb) {
      this.color = argb;
      return this;
   }

   public G glow(boolean g) {
      this.glow = g;
      return this;
   }

   public void quad(String tex, float[] a, float[] b, float[] c, float[] d, float[] uv) {
      float[] A = this.tp(a[0], a[1], a[2]);
      float[] B = this.tp(b[0], b[1], b[2]);
      float[] C = this.tp(c[0], c[1], c[2]);
      float[] D = this.tp(d[0], d[1], d[2]);
      float ux = B[0] - A[0];
      float uy = B[1] - A[1];
      float uz = B[2] - A[2];
      float vx = D[0] - A[0];
      float vy = D[1] - A[1];
      float vz = D[2] - A[2];
      float nx = uy * vz - uz * vy;
      float ny = uz * vx - ux * vz;
      float nz = ux * vy - uy * vx;
      float l = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
      if (l < 1.0E-6F) {
         nx = 0.0F;
         ny = 1.0F;
         nz = 0.0F;
      } else {
         nx /= l;
         ny /= l;
         nz /= l;
      }

      this.sink.quad(tex, new float[]{A[0], A[1], A[2], B[0], B[1], B[2], C[0], C[1], C[2], D[0], D[1], D[2]}, uv, this.color, this.glow, nx, ny, nz);
   }

   public void plane(String tex, float cx, float cy, float w, float h, float u0, float v0, float u1, float v1) {
      float x0 = cx - w / 2.0F;
      float x1 = cx + w / 2.0F;
      float y0 = cy - h / 2.0F;
      float y1 = cy + h / 2.0F;
      this.quad(
         tex,
         new float[]{x0, y1, 0.0F},
         new float[]{x1, y1, 0.0F},
         new float[]{x1, y0, 0.0F},
         new float[]{x0, y0, 0.0F},
         new float[]{u0, v0, u1, v0, u1, v1, u0, v1}
      );
   }

   public void planeTL(String tex, float w, float h, float u0, float v0, float u1, float v1) {
      this.quad(
         tex,
         new float[]{0.0F, 0.0F, 0.0F},
         new float[]{w, 0.0F, 0.0F},
         new float[]{w, -h, 0.0F},
         new float[]{0.0F, -h, 0.0F},
         new float[]{u0, v0, u1, v0, u1, v1, u0, v1}
      );
   }

   public void box(String tex, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1) {
      float[] uv = new float[]{u0, v0, u1, v0, u1, v1, u0, v1};
      this.quad(tex, new float[]{x0, y1, z1}, new float[]{x1, y1, z1}, new float[]{x1, y0, z1}, new float[]{x0, y0, z1}, uv);
      this.quad(tex, new float[]{x1, y1, z0}, new float[]{x0, y1, z0}, new float[]{x0, y0, z0}, new float[]{x1, y0, z0}, uv);
      this.quad(tex, new float[]{x0, y1, z0}, new float[]{x0, y1, z1}, new float[]{x0, y0, z1}, new float[]{x0, y0, z0}, uv);
      this.quad(tex, new float[]{x1, y1, z1}, new float[]{x1, y1, z0}, new float[]{x1, y0, z0}, new float[]{x1, y0, z1}, uv);
      this.quad(tex, new float[]{x0, y1, z0}, new float[]{x1, y1, z0}, new float[]{x1, y1, z1}, new float[]{x0, y1, z1}, uv);
      this.quad(tex, new float[]{x0, y0, z1}, new float[]{x1, y0, z1}, new float[]{x1, y0, z0}, new float[]{x0, y0, z0}, uv);
   }

   public void box6(String tex, float x0, float y0, float z0, float x1, float y1, float z1, float[][] f) {
      this.quad(tex, new float[]{x0, y1, z1}, new float[]{x1, y1, z1}, new float[]{x1, y0, z1}, new float[]{x0, y0, z1}, r(f[0]));
      this.quad(tex, new float[]{x1, y1, z0}, new float[]{x0, y1, z0}, new float[]{x0, y0, z0}, new float[]{x1, y0, z0}, r(f[1]));
      this.quad(tex, new float[]{x0, y1, z0}, new float[]{x0, y1, z1}, new float[]{x0, y0, z1}, new float[]{x0, y0, z0}, r(f[2]));
      this.quad(tex, new float[]{x1, y1, z1}, new float[]{x1, y1, z0}, new float[]{x1, y0, z0}, new float[]{x1, y0, z1}, r(f[3]));
      this.quad(tex, new float[]{x0, y1, z0}, new float[]{x1, y1, z0}, new float[]{x1, y1, z1}, new float[]{x0, y1, z1}, r(f[4]));
      this.quad(tex, new float[]{x0, y0, z1}, new float[]{x1, y0, z1}, new float[]{x1, y0, z0}, new float[]{x0, y0, z0}, r(f[5]));
   }

   private static float[] r(float[] q) {
      return new float[]{q[0], q[1], q[2], q[1], q[2], q[3], q[0], q[3]};
   }

   public void cylinder(String tex, float r0, float r1, float y0, float y1, int seg, boolean caps, float u0, float v0, float u1, float v1) {
      for (int i = 0; i < seg; i++) {
         double a0 = (Math.PI * 2) * i / seg;
         double a1 = (Math.PI * 2) * (i + 1) / seg;
         float c0 = (float)Math.cos(a0);
         float s0 = (float)Math.sin(a0);
         float c1 = (float)Math.cos(a1);
         float s1 = (float)Math.sin(a1);
         float uu0 = u0 + (u1 - u0) * i / seg;
         float uu1 = u0 + (u1 - u0) * (i + 1) / seg;
         this.quad(
            tex,
            new float[]{c0 * r1, y1, s0 * r1},
            new float[]{c1 * r1, y1, s1 * r1},
            new float[]{c1 * r0, y0, s1 * r0},
            new float[]{c0 * r0, y0, s0 * r0},
            new float[]{uu0, v0, uu1, v0, uu1, v1, uu0, v1}
         );
         if (caps) {
            float um = (u0 + u1) / 2.0F;
            float vm = (v0 + v1) / 2.0F;
            this.quad(
               tex,
               new float[]{0.0F, y1, 0.0F},
               new float[]{0.0F, y1, 0.0F},
               new float[]{c1 * r1, y1, s1 * r1},
               new float[]{c0 * r1, y1, s0 * r1},
               new float[]{um, vm, um, vm, um, v0, um, v0}
            );
            if (r0 > 0.01F) {
               this.quad(
                  tex,
                  new float[]{0.0F, y0, 0.0F},
                  new float[]{c0 * r0, y0, s0 * r0},
                  new float[]{c1 * r0, y0, s1 * r0},
                  new float[]{0.0F, y0, 0.0F},
                  new float[]{um, vm, um, v1, um, v1, um, vm}
               );
            }
         }
      }
   }

   public void ring(String tex, float rIn, float rOut, float y, int seg, float u0, float v0, float u1, float v1) {
      for (int i = 0; i < seg; i++) {
         double a0 = (Math.PI * 2) * i / seg;
         double a1 = (Math.PI * 2) * (i + 1) / seg;
         float c0 = (float)Math.cos(a0);
         float s0 = (float)Math.sin(a0);
         float c1 = (float)Math.cos(a1);
         float s1 = (float)Math.sin(a1);
         float uu0 = u0 + (u1 - u0) * i / seg;
         float uu1 = u0 + (u1 - u0) * (i + 1) / seg;
         this.quad(
            tex,
            new float[]{c0 * rOut, y, s0 * rOut},
            new float[]{c1 * rOut, y, s1 * rOut},
            new float[]{c1 * rIn, y, s1 * rIn},
            new float[]{c0 * rIn, y, s0 * rIn},
            new float[]{uu0, v0, uu1, v0, uu1, v1, uu0, v1}
         );
      }
   }

   public void torus(String tex, float R, float r, int seg, int side, float u0, float v0, float u1, float v1) {
      for (int i = 0; i < seg; i++) {
         double a0 = (Math.PI * 2) * i / seg;
         double a1 = (Math.PI * 2) * (i + 1) / seg;

         for (int j = 0; j < side; j++) {
            double b0 = (Math.PI * 2) * j / side;
            double b1 = (Math.PI * 2) * (j + 1) / side;
            float[] p00 = tor(R, r, a0, b0);
            float[] p10 = tor(R, r, a1, b0);
            float[] p11 = tor(R, r, a1, b1);
            float[] p01 = tor(R, r, a0, b1);
            float uu0 = u0 + (u1 - u0) * i / seg;
            float uu1 = u0 + (u1 - u0) * (i + 1) / seg;
            float vv0 = v0 + (v1 - v0) * j / side;
            float vv1 = v0 + (v1 - v0) * (j + 1) / side;
            this.quad(tex, p00, p10, p11, p01, new float[]{uu0, vv0, uu1, vv0, uu1, vv1, uu0, vv1});
         }
      }
   }

   private static float[] tor(float R, float r, double a, double b) {
      double c = R + r * Math.cos(b);
      return new float[]{(float)(Math.cos(a) * c), (float)(r * Math.sin(b)), (float)(Math.sin(a) * c)};
   }

   public void sphere(String tex, float r, int seg, int rings, float u0, float v0, float u1, float v1) {
      for (int i = 0; i < rings; i++) {
         double t0 = Math.PI * i / rings - (Math.PI / 2);
         double t1 = Math.PI * (i + 1) / rings - (Math.PI / 2);

         for (int j = 0; j < seg; j++) {
            double a0 = (Math.PI * 2) * j / seg;
            double a1 = (Math.PI * 2) * (j + 1) / seg;
            float[] p00 = sph(r, t0, a0);
            float[] p01 = sph(r, t0, a1);
            float[] p11 = sph(r, t1, a1);
            float[] p10 = sph(r, t1, a0);
            float uu0 = u0 + (u1 - u0) * j / seg;
            float uu1 = u0 + (u1 - u0) * (j + 1) / seg;
            float vv0 = v1 - (v1 - v0) * i / rings;
            float vv1 = v1 - (v1 - v0) * (i + 1) / rings;
            this.quad(tex, p10, p11, p01, p00, new float[]{uu0, vv1, uu1, vv1, uu1, vv0, uu0, vv0});
         }
      }
   }

   private static float[] sph(float r, double t, double a) {
      return new float[]{(float)(Math.cos(t) * Math.cos(a) * r), (float)(Math.sin(t) * r), (float)(Math.cos(t) * Math.sin(a) * r)};
   }

   public void cross(String tex, float cx, float cy, float cz, float w, float h, float u0, float v0, float u1, float v1) {
      this.push();
      this.translate(cx, cy, cz);
      this.plane(tex, 0.0F, 0.0F, w, h, u0, v0, u1, v1);
      this.rotY(90.0F);
      this.plane(tex, 0.0F, 0.0F, w, h, u0, v0, u1, v1);
      this.pop();
   }

   public interface Sink {
      void quad(String var1, float[] var2, float[] var3, int var4, boolean var5, float var6, float var7, float var8);
   }
}
