package dev.lego.cosmetic;

final class PetRig {
   static final float WALK_HZ = 1.55F;
   static final float RUN_HZ = 2.35F;
   static final float WADDLE_HZ = 2.1F;
   static final float HOP_HZ = 1.45F;
   static final float[] WALK_OFF = new float[]{0.0F, 0.5F, 0.53F, 0.03F};
   static final float[] RUN_OFF = new float[]{0.5F, 0.6F, 0.0F, 0.1F};
   static final float WALK_DUTY = 0.6F;
   static final float RUN_DUTY = 0.38F;

   private PetRig() {
   }

   static float clamp01(float v) {
      return v < 0.0F ? 0.0F : (v > 1.0F ? 1.0F : v);
   }

   static float lerp(float a, float b, float t) {
      return a + (b - a) * t;
   }

   static float frac(float x) {
      return x - (float)Math.floor(x);
   }

   static float smooth(float e0, float e1, float x) {
      float t = clamp01((x - e0) / (e1 - e0));
      return t * t * (3.0F - 2.0F * t);
   }

   static float window(float x, float a, float b, float ramp) {
      return smooth(a, a + ramp, x) * (1.0F - smooth(b - ramp, b, x));
   }

   static void gait(Pets.P p) {
      float t = p.t;
      float sp = p.sp;
      p.walkW = clamp01(sp / 0.5F);
      p.runW = smooth(0.62F, 0.92F, sp);
      p.idle = 1.0F - smooth(0.03F, 0.2F, sp);
      p.cw = frac(t * 1.55F);
      p.cr = frac(t * 2.35F);
      float ww = p.walkW * (1.0F - p.runW);
      double aw = (Math.PI * 2) * p.cw;
      double ar = (Math.PI * 2) * p.cr;
      p.bob = ww * 0.045F * PetGeo.sin(aw * 2.0 + 0.6) + p.runW * (0.13F * PetGeo.sin(ar - 0.9) - 0.02F);
      p.roll = ww * 2.6F * PetGeo.sin(aw) + p.runW * 1.2F * PetGeo.sin(ar);
      p.pitch = p.runW * 8.5F * PetGeo.sin(ar + 1.25);
      p.stretch = 1.0F + p.runW * 0.075F * PetGeo.sin(ar - 0.35);
      p.nod = ww * 5.5F * PetGeo.sin(aw * 2.0 + 1.7) - p.pitch * 0.75F;
      p.tailSway = ww * 11.0F * PetGeo.sin(aw + 0.7) + p.runW * 6.0F * PetGeo.sin(ar);
      p.tailLift = p.tailLift + p.runW * 16.0F;
      double wd = (Math.PI * 2) * frac(t * 2.1F);
      p.waddle = p.walkW * 9.0F * PetGeo.sin(wd) + p.idle * 1.2F * PetGeo.sin(t * 1.3F);
      p.waddleBob = p.walkW * 0.35F * Math.abs(PetGeo.sin(wd));
      float id = p.idle;
      int cycle = (int)Math.floor(t / 11.3F);
      float glance = window(frac(t / 11.3F), 0.12F, 0.5F, 0.1F) * id;
      p.headYaw += glance * 34.0F * ((cycle & 1) == 0 ? 1 : -1);
      p.headRoll += glance * 5.0F * ((cycle & 1) == 0 ? 1 : -1);
      p.sniff = window(frac(t / 17.1F + 0.3F), 0.55F, 0.78F, 0.06F) * id;
      p.headPitch = p.headPitch + p.sniff * (20.0F + PetGeo.sin(t * 15.0F) * 3.0F);
      boolean calm = p.is("happy") || p.is("love") || p.is("curious") || p.is("sad");
      p.sit = calm ? window(frac(t / 37.0F), 0.32F, 0.86F, 0.05F) * id : 0.0F;
      p.sniff = p.sniff * (1.0F - p.sit);
      if (p.is("excited")) {
         p.bow = window(frac(t / 4.3F), 0.08F, 0.46F, 0.1F) * id;
         p.y = p.y * (1.0F - p.bow);
      }

      if (p.is("angry")) {
         p.crouch = 1.0F;
      }

      p.lie = p.lie * (1.0F - p.sit);
   }

   static PetRig.Frame frame(Pets.P p, float hipH, float zs, float zh, float L, float belly) {
      PetRig.Frame f = new PetRig.Frame();
      f.pivY = hipH;
      f.pivZ = zh;
      float span = Math.max(0.5F, zs - zh);
      float dropSit = Math.max(0.0F, hipH - Math.max(L * 0.4F, belly * 0.4F));
      float sitAng = (float)Math.toDegrees(Math.asin(Math.min(0.75F, dropSit / span)));
      float bowDrop = Math.max(0.0F, hipH - L * 0.45F) * 0.8F;
      float bowAng = (float)Math.toDegrees(Math.asin(Math.min(0.6F, bowDrop / span)));
      float dropLie = Math.max(0.0F, belly - 0.08F);
      f.set(p.pitch * (1.0F - p.sit) + sitAng * p.sit - bowAng * p.bow);
      f.dy = p.bob * L - dropSit * p.sit - dropLie * p.lie - p.crouch * L * 0.1F;
      return f;
   }

   static void foot(Pets.P p, int i, float[] out) {
      float move = (1.0F - p.sit) * (1.0F - p.lie) * (1.0F - p.bow);
      float ww = p.walkW * (1.0F - p.runW) * move;
      float rw = p.runW * move;
      float z = 0.0F;
      float y = 0.0F;
      float sw = 0.0F;
      if (ww > 0.001F) {
         float[] o = step(frac(p.cw + WALK_OFF[i]), 0.6F, 0.62F, 0.26F);
         z += o[0] * ww;
         y += o[1] * ww;
         sw += o[2] * ww;
      }

      if (rw > 0.001F) {
         float[] o = step(frac(p.cr + RUN_OFF[i]), 0.38F, 1.2F, 0.42F);
         z += o[0] * rw;
         y += o[1] * rw;
         sw += o[2] * rw;
      }

      out[0] = z;
      out[1] = y;
      out[2] = sw;
   }

   private static float[] step(float c, float duty, float stride, float lift) {
      if (c < duty) {
         float u = c / duty;
         return new float[]{stride * (0.5F - u), 0.0F, 0.0F};
      } else {
         float u = (c - duty) / (1.0F - duty);
         float sm = u * u * (3.0F - 2.0F * u);
         return new float[]{stride * (-0.5F + sm), lift * PetGeo.sin(Math.PI * u), PetGeo.sin(Math.PI * u)};
      }
   }

   static void ikLeg(G g, PetRig.LegStyle st, float hx, float hy, float hz, float fz, float fy, float L1, float L2, int bend, float curl) {
      float dz = fz - hz;
      float dyv = fy - hy;
      float d = (float)Math.sqrt(dz * dz + dyv * dyv);
      float dMin = Math.abs(L1 - L2) + 0.08F * (L1 + L2);
      float dMax = (L1 + L2) * 0.995F;
      d = Math.max(dMin, Math.min(dMax, d));
      double a = Math.atan2(dz, -dyv);
      double cosA = (L1 * L1 + d * d - L2 * L2) / (2.0F * L1 * d);
      double al = Math.acos(Math.max(-1.0, Math.min(1.0, cosA)));
      double t1 = a + bend * al;
      float jz = (float)Math.sin(t1) * L1;
      float jy = -((float)Math.cos(t1)) * L1;
      float ez = (float)Math.sin(a) * d;
      float ey = -((float)Math.cos(a)) * d;
      double t2 = Math.atan2(ez - jz, -(ey - jy));
      float d1 = (float)Math.toDegrees(t1);
      float d2 = (float)Math.toDegrees(t2);
      g.push();
      g.translate(hx, hy, hz);
      g.rotX(-d1);
      int hs = Math.max(st.seg, 10);
      PetGeo.blob(g, st.up, 0.0F, -L1 * 0.45F, 0.0F, st.r1, L1 * 0.55F + st.r1 * 0.25F, st.r1 * 1.05F, hs, 5);
      g.translate(0.0F, -L1, 0.0F);
      g.rotX(-(d2 - d1));
      PetGeo.blob(g, st.lo, 0.0F, -L2 * 0.5F, 0.0F, st.r2, L2 * 0.55F + st.r2 * 0.2F, st.r2, hs - 1, 5);
      g.translate(0.0F, -L2, 0.0F);
      g.rotX(d2 + curl);
      if (st.hoof) {
         g.cylinder(st.paw, st.pawR * 0.95F, st.pawR * 1.08F, -st.pawR * 0.9F, st.pawR * 0.35F, 10, true, 0.0F, 0.0F, 1.0F, 1.0F);
      } else {
         PetGeo.blob2(g, st.paw, 0.0F, -st.pawR * 0.15F, st.pawR * 0.35F, st.pawR * 1.05F, st.pawR * 0.85F, st.pawR * 0.45F, st.pawR * st.pawLen, hs - 1, 5);
      }

      g.pop();
   }

   static void legs(
      G g,
      Pets.P p,
      PetRig.Frame f,
      PetRig.LegStyle front,
      PetRig.LegStyle hind,
      float x,
      float zs,
      float zh,
      float hipH,
      float L,
      float frontRatio,
      float hindRatio
   ) {
      float[] o = new float[3];

      for (int i = 0; i < 4; i++) {
         boolean fr = i < 2;
         int side = (i & 1) == 0 ? 1 : -1;
         PetRig.LegStyle st = fr ? front : hind;
         float z0 = fr ? zs : zh;
         float hy = f.y(hipH, z0);
         float hz = f.z(hipH, z0);
         foot(p, i, o);
         float tz = z0 + o[0] * L;
         float ty = st.pawR * 0.85F + o[1] * L;
         if (fr) {
            tz += p.lie * L * 0.75F + p.bow * L * 0.55F;
         } else {
            tz += p.sit * L * 0.45F + p.lie * L * 0.5F;
         }

         float ratio = fr ? frontRatio : hindRatio;
         float curl = fr ? o[2] * 45.0F : o[2] * 25.0F;
         ikLeg(g, st, side * x, hy, hz, tz, ty, L * ratio, L * (1.0F - ratio), fr ? 1 : -1, curl);
      }
   }

   static void feet(G g, Pets.P p, String tex, float x, float z, float r, float stride) {
      for (int s = -1; s <= 1; s += 2) {
         float c = frac(p.t * 2.1F + (s > 0 ? 0.0F : 0.5F));
         float[] o = step(c, 0.55F, stride, 0.55F);
         float w = p.walkW;
         g.push();
         g.translate(s * x, 0.3F + o[1] * w * r * 1.4F, z + o[0] * w * r * 2.2F);
         g.rotY(s * 12);
         g.rotX(-o[2] * w * 25.0F);
         PetGeo.blob2(g, tex, 0.0F, 0.0F, 0.35F, r, 0.34F, 0.16F, r * 1.4F, 10, 5);
         g.pop();
      }
   }

   static float[] hop(Pets.P p) {
      float amt = smooth(0.02F, 0.35F, p.sp);
      float c = frac(p.t * 1.45F);
      float h = 0.0F;
      float air = 0.0F;
      float sq;
      if (c < 0.22F) {
         sq = -PetGeo.sin(Math.PI * c / 0.22F) * 0.9F;
      } else if (c < 0.8F) {
         float u = (c - 0.22F) / 0.58F;
         h = PetGeo.sin(Math.PI * u);
         sq = (1.0F - u * 2.0F) * 0.8F;
         air = PetGeo.sin(Math.PI * u);
      } else {
         sq = -PetGeo.sin(Math.PI * (c - 0.8F) / 0.2F);
      }

      return new float[]{h * amt, sq * amt, air * amt};
   }

   static float[] flap(Pets.P p, float hz) {
      double ph = p.t * Math.PI * 2.0 * hz;
      float w = PetGeo.sin(ph);
      return new float[]{w, -w * 0.5F + 0.25F * PetGeo.sin(ph * 2.0)};
   }

   static void collar(G g, Pets.P p, float r, float thick) {
      if (p.lvl >= 4) {
         g.push();
         g.torus(Pets.col(14168650), r, thick, 12, 3, 0.0F, 0.0F, 1.0F, 1.0F);
         g.translate(0.0F, -thick * 0.6F, r + thick * 0.4F);
         PetGeo.blob(g, Pets.gl(p.lvl >= 5 ? 16762938 : 13159640), 0.0F, -thick * 1.4F, 0.0F, thick * 1.5F, thick * 1.7F, thick * 0.6F, 6, 3);
         g.pop();
      }
   }

   static void sparkles(G g, Pets.P p, float radius) {
      if (p.lvl >= 5) {
         int n = 7;

         for (int k = 0; k < n; k++) {
            double an = p.t * 0.6 + k * Math.PI * 2.0 / n;
            float tw = 0.5F + 0.5F * PetGeo.sin(p.t * 3.1F + k * 1.9F);
            int al = (int)(70.0F + 150.0F * tw);
            PetGeo.sprite(
               g,
               "pet_star",
               PetGeo.cos(an) * radius,
               0.35F + 0.25F * PetGeo.sin(p.t * 1.7F + k),
               PetGeo.sin(an) * radius,
               0.55F + 0.45F * tw,
               al << 24 | 16766554
            );
         }
      }
   }

   static String style(String base, float gr) {
      int st = gr < 0.34F ? 0 : (gr < 0.7F ? 1 : 2);
      return st == 0 ? base : base + "." + st;
   }

   static final class Frame {
      float pu;
      float dy;
      float pivY;
      float pivZ;
      float c = 1.0F;
      float s;

      void set(float pitchUp) {
         this.pu = pitchUp;
         this.c = PetGeo.cos(Math.toRadians(this.pu));
         this.s = PetGeo.sin(Math.toRadians(this.pu));
      }

      float y(float y, float z) {
         return this.c * (y - this.pivY) + this.s * (z - this.pivZ) + this.pivY + this.dy;
      }

      float z(float y, float z) {
         return -this.s * (y - this.pivY) + this.c * (z - this.pivZ) + this.pivZ;
      }

      void apply(G g) {
         g.translate(0.0F, this.dy + this.pivY, this.pivZ);
         g.rotX(-this.pu);
         g.translate(0.0F, -this.pivY, -this.pivZ);
      }
   }

   static final class LegStyle {
      String up;
      String lo;
      String paw;
      float r1;
      float r2;
      float pawR;
      float pawLen = 1.3F;
      boolean hoof;
      int seg = 6;

      LegStyle(String up, String lo, String paw, float r1, float r2, float pawR) {
         this.up = up;
         this.lo = lo;
         this.paw = paw;
         this.r1 = r1;
         this.r2 = r2;
         this.pawR = pawR;
      }
   }
}
