package dev.lego.cosmetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Raster {
   private static final float[] KEY = norm(-0.42F, 0.72F, 0.55F);
   private static final float[] FILL = norm(0.75F, 0.1F, 0.45F);
   private static final float[] HALF = norm(-0.42F, 0.72F, 1.55F);
   static final int MQ_SKIN = -9274999;
   static final int MQ_SHIRT = -11380116;
   static final int MQ_PANTS = -12367785;

   private Raster() {
   }

   public static int[] render(Cos.Item item, int w, int h, float yaw, float pitch, float time, boolean mannequin, int bgArgb) {
      return render(item, null, w, h, yaw, pitch, time, mannequin, bgArgb);
   }

   public static int[] renderPose(float[] pose, int w, int h, float yaw, float pitch, int bgArgb) {
      return render(null, pose, w, h, yaw, pitch, 0.0F, true, bgArgb);
   }

   private static float[] norm(float x, float y, float z) {
      float l = (float)Math.sqrt(x * x + y * y + z * z);
      return new float[]{x / l, y / l, z / l};
   }

   private static int[] render(Cos.Item item, float[] pose, int w, int h, float yaw, float pitch, float time, boolean mannequin, int bgArgb) {
      float cy = (float)Math.cos(Math.toRadians(yaw));
      float sy = (float)Math.sin(Math.toRadians(yaw));
      float cp = (float)Math.cos(Math.toRadians(pitch));
      float sp = (float)Math.sin(Math.toRadians(pitch));
      float[] focus = focus(item == null ? null : item.slot);
      List<float[]> qp = new ArrayList<>();
      List<float[]> qn = new ArrayList<>();
      List<float[]> quv = new ArrayList<>();
      List<String> qt = new ArrayList<>();
      List<int[]> qc = new ArrayList<>();
      G.Sink sink = (tex, p, uv, argb, glow, nx, ny, nz) -> {
         float[] q = new float[12];

         for (int i = 0; i < 4; i++) {
            float xx = p[i * 3] - focus[0];
            float yx = p[i * 3 + 1] - focus[1];
            float z = p[i * 3 + 2] - focus[2];
            float x1 = xx * cy - z * sy;
            float z1 = xx * sy + z * cy;
            float y2 = yx * cp - z1 * sp;
            float z2 = yx * sp + z1 * cp;
            q[i * 3] = x1;
            q[i * 3 + 1] = y2;
            q[i * 3 + 2] = z2;
         }

         float nx1 = nx * cy - nz * sy;
         float nz1 = nx * sy + nz * cy;
         float ny2 = ny * cp - nz1 * sp;
         float nz2 = ny * sp + nz1 * cp;
         qp.add(q);
         qn.add(new float[]{nx1, ny2, nz2});
         quv.add(uv);
         qt.add(tex);
         qc.add(new int[]{argb, glow ? 1 : 0});
      };
      G g = new G(sink);
      Cos.A a = new Cos.A();
      a.time = time;
      a.preview = true;
      boolean pet = item != null && item.slot == Cos.Slot.PET;
      boolean veh = item != null && item.slot == Cos.Slot.VEHICLE;
      Vehicles.Stance vst = veh ? Vehicles.stance(item.id) : null;
      boolean man = mannequin && !pet && (!veh || vst != Vehicles.Stance.SIT && vst != Vehicles.Stance.PEDAL && vst != Vehicles.Stance.RIDE);
      if (man) {
         mannequin(g, pose);
      }

      if (item != null) {
         g.push();
         if (item.slot == Cos.Slot.CAPE) {
            Models.capeGeometry(g, a, item.cape);
         } else if (veh) {
            g.translate(0.0F, -24.0F - Math.max(0.0F, Vehicles.lift(item.id, time)), 0.0F);
            item.model.render(g, a);
         } else {
            item.model.render(g, a);
         }

         g.pop();
      }

      if (man && item != null && item.slot != Cos.Slot.HAT && item.slot != Cos.Slot.FACE || pose != null) {
         groundShadow(g);
      }

      int n = qp.size();
      float[][] P = qp.toArray(new float[0][]);
      float[][] N = qn.toArray(new float[0][]);
      int[] grp = new int[n];

      for (int i = 0; i < n; i++) {
         grp[i] = Smooth.group(qt.get(i), qc.get(i)[1] != 0);
      }

      float[] vn = Smooth.normals(P, N, grp, n);
      List<Raster.Tri> tris = new ArrayList<>(n * 2);
      int[][] idx = new int[][]{{0, 1, 2}, {0, 2, 3}};
      Map<String, CTex.T> texCache = new HashMap<>();

      for (int qi = 0; qi < n; qi++) {
         float[] q = P[qi];
         float[] fnn = N[qi];
         float[] uv = quv.get(qi);
         int argb = qc.get(qi)[0];
         boolean glow = qc.get(qi)[1] != 0;
         CTex.T t = texCache.computeIfAbsent(qt.get(qi), CTex::get);
         float cx = (q[0] + q[3] + q[6] + q[9]) / 4.0F;
         float cyy = (q[1] + q[4] + q[7] + q[10]) / 4.0F;
         float cz = (q[2] + q[5] + q[8] + q[11]) / 4.0F;
         float camD = distance(item == null ? null : item.slot);
         float vx = -cx;
         float vy = -cyy;
         float vz = camD - cz;
         float flip = fnn[0] * vx + fnn[1] * vy + fnn[2] * vz < 0.0F ? -1.0F : 1.0F;
         float[] shade = new float[4];
         float[] spc = new float[4];

         for (int k = 0; k < 4; k++) {
            if (glow) {
               shade[k] = 1.0F;
            } else {
               float nx = vn[qi * 12 + k * 3] * flip;
               float ny = vn[qi * 12 + k * 3 + 1] * flip;
               float nz = vn[qi * 12 + k * 3 + 2] * flip;
               float d = nx * KEY[0] + ny * KEY[1] + nz * KEY[2];
               float f = nx * FILL[0] + ny * FILL[1] + nz * FILL[2];
               float hh = Math.max(0.0F, nx * HALF[0] + ny * HALF[1] + nz * HALF[2]);
               float rim = 1.0F - Math.max(0.0F, nz);
               float s = 0.36F + 0.62F * Math.max(0.0F, d) + 0.16F * Math.max(0.0F, f) + 0.2F * rim * rim * rim + 0.07F * Math.max(0.0F, ny);
               float spec = (float)Math.pow(hh, 28.0) * 0.22F;
               shade[k] = s;
               spc[k] = spec;
            }
         }

         for (int[] kk : idx) {
            Raster.Tri tr = new Raster.Tri();

            for (int j = 0; j < 3; j++) {
               System.arraycopy(q, kk[j] * 3, tr.p, j * 3, 3);
               tr.uv[j * 2] = uv[kk[j] * 2];
               tr.uv[j * 2 + 1] = uv[kk[j] * 2 + 1];
               tr.vs[j] = shade[kk[j]];
               tr.sp[j] = spc[kk[j]];
            }

            tr.tex = t;
            tr.color = argb;
            tr.glow = glow;
            tr.depth = (tr.p[2] + tr.p[5] + tr.p[8]) / 3.0F;
            tris.add(tr);
         }
      }

      tris.sort((t1, t2) -> Float.compare(t1.depth, t2.depth));
      int ss = 2;
      int W = w * ss;
      int H = h * ss;
      float[] zb = new float[W * H];
      Arrays.fill(zb, -1.0E9F);
      float[] cr = new float[W * H];
      float[] cg = new float[W * H];
      float[] cb = new float[W * H];
      float[] ca = new float[W * H];
      float camDist = distance(item == null ? null : item.slot);
      float fov = (float)(H * 1.9);

      for (Raster.Tri t : tris) {
         raster(t, W, H, camDist, fov, zb, cr, cg, cb, ca);
      }

      int[] out = new int[w * h];
      float br = (bgArgb >> 16 & 0xFF) / 255.0F;
      float bgg = (bgArgb >> 8 & 0xFF) / 255.0F;
      float bb = (bgArgb & 0xFF) / 255.0F;
      float ba = (bgArgb >>> 24) / 255.0F;

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            float r = 0.0F;
            float gg = 0.0F;
            float b = 0.0F;
            float al = 0.0F;

            for (int sy2 = 0; sy2 < ss; sy2++) {
               for (int sx = 0; sx < ss; sx++) {
                  int i = (y * ss + sy2) * W + x * ss + sx;
                  r += cr[i];
                  gg += cg[i];
                  b += cb[i];
                  al += ca[i];
               }
            }

            int nn = ss * ss;
            r /= nn;
            gg /= nn;
            b /= nn;
            al /= nn;
            float oa = al + ba * (1.0F - al);
            float or = r + br * ba * (1.0F - al);
            float og = gg + bgg * ba * (1.0F - al);
            float ob = b + bb * ba * (1.0F - al);
            if (oa > 1.0E-4F) {
               or /= oa;
               og /= oa;
               ob /= oa;
            }

            out[y * w + x] = clamp(oa) << 24 | clamp(or) << 16 | clamp(og) << 8 | clamp(ob);
         }
      }

      return out;
   }

   private static void groundShadow(G g) {
      int c = g.color;
      boolean gl = g.glow;
      g.color(-1711276032).glow(true);
      g.quad(
         "mannequin_shadow",
         new float[]{-9.0F, -24.05F, -7.0F},
         new float[]{9.0F, -24.05F, -7.0F},
         new float[]{9.0F, -24.05F, 7.0F},
         new float[]{-9.0F, -24.05F, 7.0F},
         new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
      );
      g.color(c).glow(gl);
   }

   private static int clamp(float v) {
      return Math.max(0, Math.min(255, Math.round(v * 255.0F)));
   }

   private static float[] focus(Cos.Slot s) {
      if (s == null) {
         return new float[]{0.0F, -5.0F, 0.0F};
      } else {
         switch (s) {
            case HAT:
               return new float[]{0.0F, 9.0F, 0.0F};
            case FACE:
               return new float[]{0.0F, 5.0F, 0.0F};
            case VEHICLE:
               return new float[]{0.0F, -17.0F, 0.0F};
            case KNIFE:
               return new float[]{1.0F, -10.0F, 4.0F};
            case PET:
               return new float[]{0.0F, 5.0F, 0.0F};
            case AURA:
               return new float[]{0.0F, -11.0F, 0.0F};
            case WINGS:
               return new float[]{0.0F, -6.0F, -4.0F};
            case CAPE:
            case BACK:
               return new float[]{0.0F, -7.0F, -2.0F};
            default:
               return new float[]{0.0F, -6.0F, 0.0F};
         }
      }
   }

   private static float distance(Cos.Slot s) {
      if (s == null) {
         return 82.0F;
      } else {
         switch (s) {
            case HAT:
               return 40.0F;
            case FACE:
               return 28.0F;
            case VEHICLE:
               return 130.0F;
            case KNIFE:
               return 30.0F;
            case PET:
               return 30.0F;
            case AURA:
               return 66.0F;
            case WINGS:
               return 86.0F;
            default:
               return 48.0F;
         }
      }
   }

   private static void raster(Raster.Tri t, int W, int H, float camDist, float fov, float[] zb, float[] cr, float[] cg, float[] cb, float[] ca) {
      float[] sx = new float[3];
      float[] sy = new float[3];
      float[] iz = new float[3];

      for (int i = 0; i < 3; i++) {
         float z = camDist - t.p[i * 3 + 2];
         if (z < 1.0F) {
            return;
         }

         iz[i] = 1.0F / z;
         sx[i] = W / 2.0F - t.p[i * 3] * fov * iz[i];
         sy[i] = H / 2.0F - t.p[i * 3 + 1] * fov * iz[i];
      }

      float area = (sx[1] - sx[0]) * (sy[2] - sy[0]) - (sx[2] - sx[0]) * (sy[1] - sy[0]);
      if (!(Math.abs(area) < 1.0E-6F)) {
         int x0 = Math.max(0, (int)Math.floor(Math.min(sx[0], Math.min(sx[1], sx[2]))));
         int x1 = Math.min(W - 1, (int)Math.ceil(Math.max(sx[0], Math.max(sx[1], sx[2]))));
         int y0 = Math.max(0, (int)Math.floor(Math.min(sy[0], Math.min(sy[1], sy[2]))));
         int y1 = Math.min(H - 1, (int)Math.ceil(Math.max(sy[0], Math.max(sy[1], sy[2]))));
         float tr = (t.color >> 16 & 0xFF) / 255.0F;
         float tg = (t.color >> 8 & 0xFF) / 255.0F;
         float tb = (t.color & 0xFF) / 255.0F;
         float ta = (t.color >>> 24) / 255.0F;

         for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
               float px = x + 0.5F;
               float py = y + 0.5F;
               float w0 = ((sx[1] - px) * (sy[2] - py) - (sx[2] - px) * (sy[1] - py)) / area;
               float w1 = ((sx[2] - px) * (sy[0] - py) - (sx[0] - px) * (sy[2] - py)) / area;
               float w2 = 1.0F - w0 - w1;
               if (!(w0 < -1.0E-4F) && !(w1 < -1.0E-4F) && !(w2 < -1.0E-4F)) {
                  float izz = w0 * iz[0] + w1 * iz[1] + w2 * iz[2];
                  int i = y * W + x;
                  if (!(izz < zb[i] - 1.0E-7F)) {
                     float u = (w0 * t.uv[0] * iz[0] + w1 * t.uv[2] * iz[1] + w2 * t.uv[4] * iz[2]) / izz;
                     float v = (w0 * t.uv[1] * iz[0] + w1 * t.uv[3] * iz[1] + w2 * t.uv[5] * iz[2]) / izz;
                     int c = bilinear(t.tex, u, v);
                     float a = (c >>> 24) / 255.0F * ta;
                     if (!(a < 0.02F)) {
                        float sh = w0 * t.vs[0] + w1 * t.vs[1] + w2 * t.vs[2];
                        float spc = w0 * t.sp[0] + w1 * t.sp[1] + w2 * t.sp[2];
                        float r = (c >> 16 & 0xFF) / 255.0F * tr * sh + spc;
                        float g = (c >> 8 & 0xFF) / 255.0F * tg * sh + spc;
                        float b = (c & 0xFF) / 255.0F * tb * sh + spc;
                        if (t.glow) {
                           r *= 1.1F;
                           g *= 1.1F;
                           b *= 1.1F;
                        }

                        r = Math.min(1.0F, r);
                        g = Math.min(1.0F, g);
                        b = Math.min(1.0F, b);
                        cr[i] = r * a + cr[i] * (1.0F - a);
                        cg[i] = g * a + cg[i] * (1.0F - a);
                        cb[i] = b * a + cb[i] * (1.0F - a);
                        ca[i] = a + ca[i] * (1.0F - a);
                        if (a > 0.5F) {
                           zb[i] = izz;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static int bilinear(CTex.T t, float u, float v) {
      float fx = u * t.w - 0.5F;
      float fy = v * t.h - 0.5F;
      int x0 = (int)Math.floor(fx);
      int y0 = (int)Math.floor(fy);
      float ax = fx - x0;
      float ay = fy - y0;
      int c00 = px(t, x0, y0);
      int c10 = px(t, x0 + 1, y0);
      int c01 = px(t, x0, y0 + 1);
      int c11 = px(t, x0 + 1, y0 + 1);
      int out = 0;

      for (int s = 0; s < 32; s += 8) {
         float a = (c00 >>> s & 0xFF) * (1.0F - ax) + (c10 >>> s & 0xFF) * ax;
         float b = (c01 >>> s & 0xFF) * (1.0F - ax) + (c11 >>> s & 0xFF) * ax;
         out |= (Math.round(a * (1.0F - ay) + b * ay) & 0xFF) << s;
      }

      return out;
   }

   private static int px(CTex.T t, int x, int y) {
      x = Math.max(0, Math.min(t.w - 1, x));
      y = Math.max(0, Math.min(t.h - 1, y));
      return t.argb[y * t.w + x];
   }

   static void mannequin(G g, float[] pose) {
      int oc = g.color;
      float bend = pose != null && !Float.isNaN(pose[3]) ? pose[3] : 0.0F;
      g.push();
      if (bend != 0.0F) {
         g.translate(0.0F, -12.0F, 0.0F);
         g.rotX((float)Math.toDegrees(bend));
         g.translate(0.0F, 12.0F, 0.0F);
      }

      g.color(-9274999);
      part(g, pose, 0, 0.0F, 0.0F, 0.0F, -4.0F, 0.0F, -4.0F, 4.0F, 8.0F, 4.0F, "mannequin_head");
      float[] bodyPose = pose == null ? null : new float[]{Float.NaN, Float.NaN, Float.NaN, Float.NaN, pose[4], pose[5]};
      g.color(-11380116);
      part(g, bodyPose, 3, 0.0F, 0.0F, 0.0F, -4.0F, -12.0F, -2.0F, 4.0F, 0.0F, 2.0F, "mannequin");
      g.color(-9274999);
      part(g, pose, 6, 5.0F, -2.0F, 0.0F, -1.0F, -10.0F, -2.0F, 3.0F, 2.0F, 2.0F, "mannequin");
      part(g, pose, 9, -5.0F, -2.0F, 0.0F, -3.0F, -10.0F, -2.0F, 1.0F, 2.0F, 2.0F, "mannequin");
      g.pop();
      g.color(-12367785);
      part(g, pose, 12, 1.9F, -12.0F, 0.0F, -2.0F, -12.0F, -2.0F, 2.0F, 0.0F, 2.0F, "mannequin");
      part(g, pose, 15, -1.9F, -12.0F, 0.0F, -2.0F, -12.0F, -2.0F, 2.0F, 0.0F, 2.0F, "mannequin");
      g.color(oc);
   }

   private static void part(G g, float[] pose, int idx, float px, float py, float pz, float x0, float y0, float z0, float x1, float y1, float z1, String tex) {
      g.push();
      g.translate(px, py, pz);
      if (pose != null) {
         float p = pose[idx];
         float yw = pose[idx + 1];
         float r = pose[idx + 2];
         if (!Float.isNaN(r)) {
            g.rotZ((float)Math.toDegrees(r));
         }

         if (!Float.isNaN(yw)) {
            g.rotY((float)Math.toDegrees(yw));
         }

         if (!Float.isNaN(p)) {
            g.rotX((float)Math.toDegrees(p));
         }
      }

      Cos3Geo.bbox(g, tex, x0, y0, z0, x1, y1, z1, 0.45F);
      g.pop();
   }

   private static final class Tri {
      float[] p = new float[9];
      float[] uv = new float[6];
      CTex.T tex;
      int color;
      boolean glow;
      float[] vs = new float[3];
      float[] sp = new float[3];
      float depth;
   }
}
