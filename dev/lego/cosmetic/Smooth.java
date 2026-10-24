package dev.lego.cosmetic;

import java.util.Arrays;
import java.util.HashMap;

final class Smooth {
   static final float CREASE = 0.42F;
   static final int FLAT = Integer.MIN_VALUE;

   private Smooth() {
   }

   static int group(String tex, boolean glow) {
      return !tex.contains("crystal") && !tex.contains("gem") ? tex.hashCode() * 7 + (glow ? 1 : 0) : Integer.MIN_VALUE;
   }

   static float[] normals(float[][] p, float[][] n, int[] grp, int cnt) {
      HashMap<Long, int[]> map = new HashMap<>(cnt * 3);
      long[] keys = new long[cnt * 4];

      for (int q = 0; q < cnt; q++) {
         if (grp[q] != Integer.MIN_VALUE) {
            float[] pp = p[q];

            for (int k = 0; k < 4; k++) {
               long key = key(pp[k * 3], pp[k * 3 + 1], pp[k * 3 + 2], grp[q]);
               keys[q * 4 + k] = key;
               int[] l = map.get(key);
               if (l == null) {
                  l = new int[5];
                  map.put(key, l);
               } else if (l[0] + 1 >= l.length) {
                  l = Arrays.copyOf(l, l.length * 2);
                  map.put(key, l);
               }

               if (l[0] == 0 || l[l[0]] != q) {
                  l[++l[0]] = q;
               }
            }
         }
      }

      float[] out = new float[cnt * 12];

      for (int qx = 0; qx < cnt; qx++) {
         float[] fn = n[qx];
         if (grp[qx] == Integer.MIN_VALUE) {
            for (int k = 0; k < 4; k++) {
               out[qx * 12 + k * 3] = fn[0];
               out[qx * 12 + k * 3 + 1] = fn[1];
               out[qx * 12 + k * 3 + 2] = fn[2];
            }
         } else {
            for (int k = 0; k < 4; k++) {
               int[] lx = map.get(keys[qx * 4 + k]);
               float sx = 0.0F;
               float sy = 0.0F;
               float sz = 0.0F;

               for (int i = 1; i <= lx[0]; i++) {
                  float[] o = n[lx[i]];
                  if (!(o[0] * fn[0] + o[1] * fn[1] + o[2] * fn[2] < 0.42F)) {
                     sx += o[0];
                     sy += o[1];
                     sz += o[2];
                  }
               }

               float len = (float)Math.sqrt(sx * sx + sy * sy + sz * sz);
               if (len < 1.0E-5F) {
                  sx = fn[0];
                  sy = fn[1];
                  sz = fn[2];
                  len = 1.0F;
               }

               out[qx * 12 + k * 3] = sx / len;
               out[qx * 12 + k * 3 + 1] = sy / len;
               out[qx * 12 + k * 3 + 2] = sz / len;
            }
         }
      }

      return out;
   }

   private static long key(float x, float y, float z, int g) {
      long qx = Math.round(x * 40.0F) & 1048575;
      long qy = Math.round(y * 40.0F) & 1048575;
      long qz = Math.round(z * 40.0F) & 1048575;
      return (qx << 40 | qy << 20 | qz) * 31L + g;
   }
}
