package dev.lego.games;

import dev.lego.ui.Gx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

final class SolarPlanet {
   static final int SUN = 0;
   static final int TERRA = 1;
   static final int MARS = 2;
   static final int GAS = 3;
   static final int ICE = 4;
   static final int LAVA = 5;
   static final int OCEAN = 6;
   static final int DESERT = 7;
   static final int MOON = 8;
   static final int S_BURN = 0;
   static final int S_FIRE = 1;
   static final int S_ICE = 2;
   static final int S_GOLD = 3;
   static final int S_TOXIC = 4;
   static final int S_CRYSTAL = 5;
   static final int S_GOO = 6;
   static final int S_FLOOD = 7;
   static final int S_SAND = 8;
   static final int S_CREAM = 9;
   static final int S_CONFETTI = 10;
   static final int S_LAVA = 11;
   static final int S_ANNI = 12;
   static final int S_SHAKE = 13;
   static final int S_EMP = 14;
   static final int S_LEGO = 15;
   static final int S_COW = 16;
   static final int S_PIZZA = 17;
   static final int S_DONUT = 18;
   static final int S_PAINT = 19;
   static final int S_RIFT = 20;
   static final int S_PIANO = 21;
   static final int S_CRACK = 22;
   final int type;
   final String name;
   final String kind;
   final long pop;
   final float baseTemp;
   final float atmo0;
   final int atmoCol;
   final int cloudCol;
   final int cityCol;
   final float size;
   final float tilt;
   final long seed;
   final boolean inhabited;
   final boolean minor;
   float rotSpeed;
   float cloudSpeed;
   float orbitR;
   float orbitA;
   float orbitW;
   SolarPlanet parent;
   float moonDist;
   float moonA;
   float moonW;
   float moonIncl = 0.28F;
   final List<SolarPlanet> moons = new ArrayList<>();
   boolean ringed;
   final float[] ringHp = new float[72];
   int ringVer = 0;
   int[] ringCols = new int[]{-1781072, -5207456, -858420};
   volatile SolarPlanet.Maps hi;
   volatile SolarPlanet.Maps thumb;
   private volatile boolean hiPending;
   private volatile boolean thumbPending;
   private int genToken = 0;
   double rot;
   double crot;
   int ver = 0;
   double killW;
   double gAlive = 1.0;
   double dmgW;
   float core;
   float atmo;
   float tempOff;
   float lightsOff;
   float dark;
   float scale = 1.0F;
   float shake;
   boolean destroyed;
   float destroyedT;
   float rebuildT = 9.0F;
   float hitFlash;
   double deadShown;
   double aliveShown;
   int crackSeed = 1;
   int floorCol;
   int scorchCol;
   int ejectaCol;
   int coreCol;
   private float now;
   private int ringBuilt = -1;
   private long ringBuiltAt;
   private String ringKey;
   static final float RING_IN = 1.28F;
   static final float RING_OUT = 2.3F;
   static final float RING_FLAT = 0.26F;
   static final int[] HEAT = new int[256];
   private static final Map<Long, SolarPlanet.Sphere> SPHERES;

   SolarPlanet(int type, String name, String kind, long pop, float temp, int atmoCol, float atmo, float size, float tilt, long seed, boolean minor) {
      this.type = type;
      this.name = name;
      this.kind = kind;
      this.pop = pop;
      this.baseTemp = temp;
      this.atmoCol = atmoCol;
      this.atmo0 = atmo;
      this.size = size;
      this.tilt = tilt;
      this.seed = seed;
      this.minor = minor;
      this.inhabited = pop > 0L;
      this.atmo = atmo;
      this.rotSpeed = (float)((Math.PI * 2) / (type == 3 ? 70 : (type == 0 ? 160 : 120)));
      this.cloudSpeed = this.rotSpeed * 1.25F;
      this.cloudCol = type == 2 ? -1322312 : (type == 5 ? -11647164 : (type == 7 ? -727344 : -1));
      this.cityCol = type == 6 ? -6297345 : (type == 4 ? -4202241 : -15240);
      switch (type) {
         case 0:
            this.floorCol = -14021628;
            this.scorchCol = -7722488;
            this.ejectaCol = -2880;
            this.coreCol = -32;
            break;
         case 1:
         case 2:
         case 6:
         case 7:
         default:
            this.floorCol = -13884384;
            this.scorchCol = -14542057;
            this.ejectaCol = -6648968;
            this.coreCol = -9824246;
            break;
         case 3:
            this.floorCol = -12901354;
            this.scorchCol = -10864092;
            this.ejectaCol = -992064;
            this.coreCol = -8771056;
            break;
         case 4:
            this.floorCol = -14731952;
            this.scorchCol = -12561302;
            this.ejectaCol = -2560774;
            this.coreCol = -12948824;
            break;
         case 5:
            this.floorCol = -15069686;
            this.scorchCol = -14018028;
            this.ejectaCol = -10862024;
            this.coreCol = -7725048;
            break;
         case 8:
            this.floorCol = -12961738;
            this.scorchCol = -11185586;
            this.ejectaCol = -2566964;
            this.coreCol = -10872310;
      }

      Arrays.fill(this.ringHp, 1.0F);
   }

   SolarPlanet.Maps maps() {
      SolarPlanet.Maps m = this.hi;
      return m != null ? m : this.thumb;
   }

   void request(boolean full) {
      if (this.thumb == null && !this.thumbPending) {
         this.thumbPending = true;
         int tok = this.genToken;
         SolarTex.POOL.execute(() -> {
            SolarPlanet.Maps m = SolarTex.generate(this.type, this.seed, 256, 128, this.inhabited, false);
            if (tok == this.genToken) {
               this.thumb = m;
               this.ver++;
            }

            this.thumbPending = false;
         });
      }

      if (full && this.hi == null && !this.hiPending && !this.destroyed) {
         this.hiPending = true;
         int tok = this.genToken;
         int w = this.type != 0 && !this.minor ? 2048 : 1024;
         SolarTex.POOL.execute(() -> {
            SolarPlanet.Maps m = SolarTex.generate(this.type, this.seed, w, w / 2, this.inhabited, true);
            if (tok == this.genToken) {
               this.hi = m;
               this.ver++;
            }

            this.hiPending = false;
         });
      }
   }

   boolean loading() {
      return this.hi == null;
   }

   boolean damaged() {
      return this.killW > 0.0
         || this.dmgW > 0.0
         || this.core > 0.0F
         || this.gAlive < 1.0
         || this.destroyed
         || this.atmo < this.atmo0 - 0.001F
         || this.scale < 1.0F;
   }

   void release() {
      if (!this.damaged() && !this.hiPending) {
         this.hi = null;
      }
   }

   void repair() {
      this.genToken++;
      this.hi = null;
      this.hiPending = false;
      this.killW = 0.0;
      this.dmgW = 0.0;
      this.gAlive = 1.0;
      this.core = 0.0F;
      this.atmo = this.atmo0;
      this.tempOff = 0.0F;
      this.lightsOff = 0.0F;
      this.dark = 0.0F;
      this.scale = 1.0F;
      this.shake = 0.0F;
      if (this.destroyed) {
         this.rebuildT = 0.0F;
      }

      this.destroyed = false;
      this.destroyedT = 0.0F;
      Arrays.fill(this.ringHp, 1.0F);
      this.ringVer++;
      this.ver++;

      for (SolarPlanet m : this.moons) {
         m.repair();
      }
   }

   double alive() {
      if (this.pop > 0L && !this.destroyed) {
         SolarPlanet.Maps m = this.hi;
         double loc = m == null ? 1.0 : Math.max(0.0, 1.0 - this.killW / m.popW);
         return this.pop * loc * Math.max(0.0, this.gAlive);
      } else {
         return 0.0;
      }
   }

   double dead() {
      return this.pop - this.alive();
   }

   float integrity() {
      if (this.destroyed) {
         return 0.0F;
      } else {
         SolarPlanet.Maps m = this.hi;
         double d = m == null ? 0.0 : this.dmgW / m.areaW * 2.6;
         return (float)Math.max(0.0, 1.0 - d - this.core);
      }
   }

   float temp() {
      return this.baseTemp + this.tempOff;
   }

   void killGlobal(double frac) {
      this.gAlive = this.gAlive * Math.max(0.0, 1.0 - frac);
   }

   double[] unproject(double x, double y) {
      double r2 = x * x + y * y;
      if (r2 > 0.9995) {
         double k = 0.9997 / Math.sqrt(r2);
         x *= k;
         y *= k;
         r2 = 0.9994;
      }

      double ct = Math.cos(this.tilt);
      double st = Math.sin(this.tilt);
      double xr = x * ct + y * st;
      double yr = -x * st + y * ct;
      double z = Math.sqrt(Math.max(0.0, 1.0 - r2));
      return new double[]{Math.atan2(xr, z) + this.rot, Math.asin(Math.max(-1.0, Math.min(1.0, -yr)))};
   }

   double[] project(double lon, double lat) {
      double l = lon - this.rot;
      double xr = Math.cos(lat) * Math.sin(l);
      double yr = -Math.sin(lat);
      double z = Math.cos(lat) * Math.cos(l);
      double ct = Math.cos(this.tilt);
      double st = Math.sin(this.tilt);
      return new double[]{xr * ct - yr * st, xr * st + yr * ct, z};
   }

   void paint(double lon, double lat, float r, int style, float depth, float heatAmt, float kill, float scorch, int color) {
      SolarPlanet.Maps m = this.hi;
      if (m != null && !this.destroyed) {
         int W = m.W;
         int H = m.H;
         float k = W / 2048.0F;
         r = Math.max(0.7F, r * k);
         scorch = Math.max(1.0F, scorch);
         float ro = r * scorch;
         double cyD = (0.5 - lat / Math.PI) * H - 0.5;
         double cxD = lon / (Math.PI * 2) * W - 0.5;
         cxD -= Math.floor(cxD / W) * W;
         int y0 = (int)Math.max(0.0, Math.floor(cyD - ro));
         int y1 = (int)Math.min((double)(H - 1), Math.ceil(cyD + ro));
         double kw = 0.0;
         double dw = 0.0;
         int minX = Integer.MAX_VALUE;
         int maxX = Integer.MIN_VALUE;
         int[] alb = m.alb;
         byte[] dmg = m.dmg;
         byte[] heat = m.heat;
         byte[] city = m.city;
         byte[] h0 = m.heat0;
         int sea = m.sea & 255;
         byte[] HASH = SolarTex.HASH;
         boolean star = this.type == 0;

         for (int y = y0; y <= y1; y++) {
            float cl = m.cosRow[y];
            float dy = (float)(y - cyD);
            float q = ro * ro - dy * dy;
            if (!(q < 0.0F)) {
               float half = (float)Math.sqrt(q) / cl;
               if (half > W / 2.0F) {
                  half = W / 2.0F;
               }

               int xa = (int)Math.floor(cxD - half);
               int xb = (int)Math.ceil(cxD + half);
               if (xa < minX) {
                  minX = xa;
               }

               if (xb > maxX) {
                  maxX = xb;
               }

               double rowK = 0.0;
               double rowD = 0.0;

               for (int xx = xa; xx <= xb; xx++) {
                  int x = xx & W - 1;
                  float dx = (float)(xx - cxD) * cl;
                  float d0 = (float)Math.sqrt(dx * dx + dy * dy) / r;
                  int hc = HASH[(xx >> 2) * 31 + (y >> 2) * 977 + (int)(lon * 91.0) & 65535] & 255;
                  int hf = HASH[x * 7 + y * 1031 & 65535] & 255;
                  float d = d0 * (1.0F + (hc - 128) * 0.0017F + (hf - 128) * 5.0E-4F);
                  if (!(d >= scorch)) {
                     int i = y << m.shift | x;
                     int a = alb[i];
                     int sa = a >>> 24;
                     int c = a & 16777215;
                     int dm = dmg[i] & 255;
                     int ht = heat[i] & 255;
                     int ct = city[i] & 255;
                     int ndm = dm;
                     int nht = ht;
                     int nct = ct;
                     int nsa = sa;
                     float in = 1.0F - d;
                     switch (style) {
                        case 0:
                        case 1:
                        case 11:
                        case 12:
                        case 13:
                        case 20:
                        case 22:
                           if (style == 13) {
                              float tx = 1.0F - d / scorch;
                              nct = (int)(ct * (1.0F - kill * tx));
                              c = SolarTex.mix(c, this.scorchCol, tx * 0.12F * depth / 60.0F);
                              ndm = Math.min(255, dm + (int)(depth * 0.08F * tx));
                           } else {
                              if (d < 1.0F) {
                                 float prof = style == 12 ? 1.0F : (float)Math.sqrt(in);
                                 ndm = Math.min(255, dm + (int)(depth * prof * (style == 1 ? 0.12F : 1.0F)));
                                 if (style == 1) {
                                    c = SolarTex.mix(c, this.scorchCol, Math.min(1.0F, in * 1.4F) * 0.85F);
                                 } else {
                                    int fl = SolarTex.mix(this.floorCol, SolarTex.shade(this.floorCol, 1.5), hf / 255.0);
                                    c = SolarTex.mix(c, fl, Math.min(1.0F, in * 2.4F) * Math.min(1.0F, depth / 90.0F + 0.3F));
                                    if (d > 0.72 && style != 12 && style != 22) {
                                       c = SolarTex.mix(c, this.ejectaCol, (1.0F - Math.abs(d - 0.9F) / 0.18F) * 0.35F);
                                    }
                                 }

                                 if (style == 11) {
                                    c = SolarTex.mix(c, -14019312, 0.7F);
                                    nht = Math.min(255, ht + (int)(heatAmt * in));
                                 }

                                 if (style == 20) {
                                    c = SolarTex.mix(c, -15071696, in);
                                 }

                                 nsa = 0;
                                 nct = (int)(ct * (1.0F - Math.min(1.0F, kill * 1.2F)));
                              } else {
                                 float tx = 1.0F - (d - 1.0F) / (scorch - 1.0F);
                                 float s = tx * tx * Math.min(1.0F, depth / 60.0F + 0.35F) * (style == 1 ? 1.0F : 0.8F);
                                 c = SolarTex.mix(c, style == 20 ? -14021052 : this.scorchCol, s);
                                 nct = (int)(ct * (1.0F - kill * Math.min(1.0F, tx * 1.3F)));
                                 if (sa > 1) {
                                    nsa = (int)(sa * (1.0F - tx * 0.6F));
                                 }
                              }

                              if (ndm > 200) {
                                 c = SolarTex.mix(c, this.coreCol, (ndm - 200) / 55.0F * (star ? 0.4F : 1.0F));
                              }

                              if (ndm >= 250 && !star) {
                                 nsa = 1;
                              }

                              float hh = heatAmt * (float)Math.pow(Math.max(0.0F, 1.05F - d / scorch), 1.6);
                              if (style == 1) {
                                 hh *= (hf & 3) == 0 ? 1.4F : 0.7F;
                              }

                              nht = Math.min(255, Math.max(nht, (int)(ht + hh)));
                           }
                           break;
                        case 2: {
                           float t = Math.min(1.0F, (scorch - d) / scorch * 1.6F);
                           int ic = SolarTex.mix(-1510148, -5714712, hf / 255.0);
                           c = SolarTex.mix(c, ic, t * 0.92F);
                           nht = (int)(ht * (1.0F - t));
                           nsa = Math.max(2, (int)(sa + (120 - sa) * t));
                           nct = (int)(ct * (1.0F - kill * t));
                           ndm = Math.min(255, dm + (int)(depth * 0.15F * t));
                           break;
                        }
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 8:
                        case 9:
                        case 19:
                           float tx = Math.min(1.0F, (scorch - d) / scorch * 2.2F);
                           int tc;
                           switch (style) {
                              case 3:
                                 tc = SolarTex.mix(-10134, -4684260, hf / 255.0);
                                 nsa = Math.max(2, (int)(sa + (200 - sa) * tx));
                                 break;
                              case 4:
                                 tc = SolarTex.mix(-8588742, -14775748, hc / 255.0);
                                 nsa = 0;
                                 break;
                              case 5:
                                 tc = SolarTex.mix(-2709249, -9819448, hf / 255.0);
                                 nsa = Math.max(2, (int)(sa + (255 - sa) * tx));
                                 break;
                              case 6:
                                 tc = SolarTex.mix(-6643026, -11906984, hf / 255.0);
                                 nsa = Math.max(2, (int)(sa + (140 - sa) * tx));
                                 break;
                              case 7:
                              default:
                                 tc = SolarTex.mix(color, SolarTex.shade(color, 0.75), hf / 400.0);
                                 nsa = (int)(sa * (1.0F - tx));
                                 break;
                              case 8:
                                 tc = SolarTex.mix(-1916540, -5207984, hf / 255.0);
                                 nsa = (int)(sa * (1.0F - tx));
                                 break;
                              case 9:
                                 tc = (hc & 64) != 0 ? -2312 : SolarTex.mix(-18736, -32850, hf / 255.0);
                                 nsa = (int)(sa * (1.0F - tx));
                           }

                           c = SolarTex.mix(c, tc, tx);
                           if (nsa == 1) {
                              nsa = 2;
                           }

                           nct = (int)(ct * (1.0F - kill * tx));
                           ndm = Math.min(255, dm + (int)(depth * tx * 0.4F));
                           if (heatAmt > 0.0F) {
                              nht = Math.min(255, (int)(ht + heatAmt * tx * in));
                           }
                           break;
                        case 7:
                           int hg = m.hgt[i] & 255;
                           if ((sa <= 150 || !(d > 0.2F)) && (!(hg > sea + depth) || !(d > 0.35F))) {
                              float tx = Math.min(1.0F, (scorch - d) / scorch * 2.5F);
                              c = SolarTex.mix(c, SolarTex.mix(-13734262, -10850726, hf / 400.0), tx * 0.85F);
                              nsa = Math.max(2, (int)(sa + (200 - sa) * tx));
                              nct = (int)(ct * (1.0F - kill * tx));
                              nht = 0;
                           }
                           break;
                        case 10: {
                           float t = Math.min(1.0F, (scorch - d) / scorch * 2.0F);
                           if (((hf ^ hc) & 3) == 0) {
                              int[] cs = new int[]{-50325, -11745, -12856833, -8588742, -4956929, -30208};
                              c = SolarTex.mix(c, cs[hf % cs.length], t);
                              nct = (int)(ct * (1.0F - kill * t));
                           }
                           break;
                        }
                        case 14: {
                           float t = 1.0F - d / scorch;
                           nct = (int)(ct * (1.0F - kill * t));
                           break;
                        }
                        case 15:
                           float ux = dx / r;
                           float uy = dy / r;
                           if (Math.abs(ux) < 0.95F && Math.abs(uy) < 0.5F) {
                              float sx = (ux + 0.95F) / 0.475F;
                              float sy = (uy + 0.5F) / 0.5F;
                              float fx = sx - (int)sx - 0.5F;
                              float fy = sy - (int)sy - 0.5F;
                              boolean stud = fx * fx + fy * fy < 0.09F;
                              boolean ring = fx * fx + fy * fy < 0.13F;
                              c = stud ? SolarTex.shade(color, 1.18) : (ring ? SolarTex.shade(color, 0.72) : color);
                              if (Math.abs(ux) > 0.9F || Math.abs(uy) > 0.45F) {
                                 c = SolarTex.shade(color, 0.62);
                              }

                              nsa = 150;
                              nct = 0;
                              nht = 0;
                              ndm = Math.min(255, dm + 40);
                           } else if (d < scorch) {
                              float tx = 1.0F - (d - 1.0F) / (scorch - 1.0F);
                              c = SolarTex.mix(c, this.scorchCol, Math.max(0.0F, tx) * 0.5F);
                              nct = (int)(ct * (1.0F - kill * Math.max(0.0F, tx)));
                           }
                           break;
                        case 16:
                        case 17:
                        case 18:
                        case 21: {
                           float t = Math.min(1.0F, (scorch - d) / scorch * 2.2F);
                           int tc;
                           if (style == 16) {
                              tc = hc < 110 ? -14935526 : -724502;
                           } else if (style == 17) {
                              tc = d > 0.85F ? -2778550 : (hc > 200 ? -5233382 : -11680);
                           } else if (style == 18) {
                              if (d < 0.45F) {
                                 tc = this.floorCol;
                                 t = in * 0.5F;
                              } else {
                                 tc = d > 0.9F ? -3569074 : ((hf & 15) == 0 ? -10496769 : -32832);
                              }
                           } else {
                              tc = ((int)((dx / r + 2.0F) * 7.0F) & 1) == 0 ? -723728 : -15329766;
                           }

                           c = SolarTex.mix(c, tc, t);
                           nsa = style == 21 ? 120 : 0;
                           nct = (int)(ct * (1.0F - kill * t));
                           ndm = Math.min(255, dm + (int)(depth * t * 0.5F));
                        }
                     }

                     if (h0 != null) {
                        nht = Math.max(nht, h0[i] & 255);
                     }

                     if (ndm >= 180) {
                        nht = Math.max(nht, Math.min(215, (ndm - 180) * 3));
                     }

                     if (nsa == 1 && ndm < 250) {
                        nsa = 0;
                     }

                     alb[i] = nsa << 24 | c & 16777215;
                     if (ndm != dm) {
                        dmg[i] = (byte)ndm;
                        rowD += ndm - dm;
                     }

                     if (nht != ht) {
                        heat[i] = (byte)nht;
                     }

                     if (nct < ct) {
                        city[i] = (byte)nct;
                        rowK += ct - nct;
                     }
                  }
               }

               kw += rowK * cl;
               dw += rowD * cl;
            }
         }

         if (m.cloud != null && (style <= 1 || style == 12 || style == 11) && depth > 20.0F) {
            int cw = m.cW;
            int ch = m.cH;
            float cr = ro * cw / W * 1.2F;
            double ccy = (0.5 - lat / Math.PI) * ch;
            double ccx = lon / (Math.PI * 2) * cw;

            for (int yx = (int)Math.max(0.0, ccy - cr); yx <= Math.min((double)(ch - 1), ccy + cr); yx++) {
               float cl = (float)Math.max(0.02, Math.cos((0.5 - (yx + 0.5) / ch) * Math.PI));
               float dy = (float)(yx - ccy);
               float halfx = (float)Math.sqrt(Math.max(0.0F, cr * cr - dy * dy)) / cl;
               if (halfx > cw / 2.0F) {
                  halfx = cw / 2.0F;
               }

               for (int xxx = (int)(ccx - halfx); xxx <= (int)(ccx + halfx); xxx++) {
                  float dx = (float)(xxx - ccx) * cl;
                  float d = (float)Math.sqrt(dx * dx + dy * dy) / cr;
                  if (!(d >= 1.0F)) {
                     int i = yx * cw + Math.floorMod(xxx, cw);
                     m.cloud[i] = (byte)((m.cloud[i] & 255) * Math.min(1.0F, d * d * 1.2F));
                  }
               }
            }
         }

         if (minX <= maxX) {
            m.markRect(minX, y0, maxX, y1, true, this.now);
         }

         this.killW += kw;
         this.dmgW += dw;
         this.ver++;
      }
   }

   void crack(double lon, double lat, float len, float width, float heatAmt, float kill, Random r) {
      double ang = r.nextDouble() * Math.PI * 2.0;
      double step = width * 0.9 / 2048.0 * Math.PI * 2.0;
      int n = (int)(len / Math.max(0.5, width * 0.9));

      for (int i = 0; i < n; i++) {
         ang += (r.nextDouble() - 0.5) * 0.7;
         double cl = Math.max(0.1, Math.cos(lat));
         lon += Math.cos(ang) * step / cl;
         lat += Math.sin(ang) * step;
         if (lat > 1.5 || lat < -1.5) {
            ang = -ang;
            lat = Math.max(-1.5, Math.min(1.5, lat));
         }

         float w = width * (1.0F - (float)i / n * 0.6F);
         this.paint(lon, lat, w, 22, 230.0F, heatAmt, kill, 2.2F, 0);
         if (r.nextFloat() < 0.04F && n > 20) {
            this.crack(lon, lat, len * 0.35F, width * 0.7F, heatAmt, kill, r);
         }
      }
   }

   void rowsPass(int y0, int y1, SolarPlanet.TexelFn f) {
      SolarPlanet.Maps m = this.hi;
      if (m != null && !this.destroyed) {
         y0 = Math.max(0, y0);
         y1 = Math.min(m.H, y1);
         if (y1 > y0) {
            long before = cityRows(m, y0, y1);

            for (int y = y0; y < y1; y++) {
               for (int x = 0; x < m.W; x++) {
                  f.apply(m, x, y, y << m.shift | x);
               }
            }

            long after = cityRows(m, y0, y1);
            this.killW = this.killW + (double)Math.max(0L, before - after) * m.cosRow[(y0 + y1) / 2];
            m.markRect(0, y0, m.W - 1, y1 - 1, true, this.now);
            this.ver++;
         }
      }
   }

   private static long cityRows(SolarPlanet.Maps m, int y0, int y1) {
      long s = 0L;

      for (int i = y0 * m.W; i < y1 * m.W; i++) {
         s += m.city[i] & 255;
      }

      return s;
   }

   void damageRing(double ang, float amount, float spread) {
      int n = this.ringHp.length;
      int c = (int)Math.floor((ang + Math.PI) / (Math.PI * 2) * n);
      int s = Math.max(0, (int)spread);

      for (int k = -s; k <= s; k++) {
         int i = Math.floorMod(c + k, n);
         this.ringHp[i] = Math.max(0.0F, this.ringHp[i] - amount * (1.0F - (float)Math.abs(k) / (s + 1)));
      }

      this.ringVer++;
   }

   float ringAlive() {
      if (!this.ringed) {
         return 0.0F;
      } else {
         float s = 0.0F;

         for (float h : this.ringHp) {
            s += h;
         }

         return s / this.ringHp.length;
      }
   }

   Gx.Img[] ringImgs(int w, int h, float rPx, float Lx, float Ly, float Lz) {
      String key = "solar:ring:" + this.name + ":" + w + "x" + h;
      Gx.Img back = Gx.get(key + ":b", 0, () -> new int[][]{new int[w * h], {w, h}});
      Gx.Img front = Gx.get(key + ":f", 0, () -> new int[][]{new int[w * h], {w, h}});
      long t = System.currentTimeMillis();
      if (!key.equals(this.ringKey) || this.ringBuilt != this.ringVer && t - this.ringBuiltAt > 350L) {
         int[] b = new int[w * h];
         int[] f = new int[w * h];
         this.buildRing(b, f, w, h, rPx, Lx, Ly, Lz);
         Gx.B.update(back, b);
         Gx.B.update(front, f);
         this.ringBuilt = this.ringVer;
         this.ringBuiltAt = t;
         this.ringKey = key;
      }

      return new Gx.Img[]{back, front};
   }

   private void buildRing(int[] back, int[] front, int w, int h, float rPx, float Lx, float Ly, float Lz) {
      double cx = w / 2.0;
      double cy = h / 2.0;
      double ct = Math.cos(this.tilt);
      double st = Math.sin(this.tilt);
      double inc = Math.asin(0.26F);
      double ci = Math.cos(inc);
      double lx = Lx * ct + Ly * st;
      double ly = -Lx * st + Ly * ct;
      double lz = Lz;
      int n = this.ringHp.length;
      int[] cols = this.ringCols;

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            double dx = x + 0.5 - cx;
            double dy = y + 0.5 - cy;
            double ux = (dx * ct + dy * st) / rPx;
            double uy = (-dx * st + dy * ct) / rPx;
            double uz = uy / 0.26F;
            double r = Math.sqrt(ux * ux + uz * uz);
            if (!(r < 1.28F) && !(r > 2.3F)) {
               double t = (r - 1.28F) / 1.02F;
               double band = 0.5 + 0.5 * Math.sin(t * 41.0) * Math.sin(t * 13.0 + 1.3) + 0.25 * Math.sin(t * 97.0);
               band = Math.max(0.05, Math.min(1.0, band));
               if (t > 0.56 && t < 0.62) {
                  band *= 0.08;
               }

               if (t > 0.86 && t < 0.885) {
                  band *= 0.25;
               }

               double a = band * Math.min(1.0, t * 9.0) * Math.min(1.0, (1.0 - t) * 12.0) * 0.9;
               int c = SolarTex.mix(cols[0], cols[1], t);
               c = SolarTex.mix(c, cols[2], SolarTex.clamp01(Math.sin(t * 23.0) * 0.5 + 0.2));
               double ang = Math.atan2(uz, ux);
               int s = (int)((ang + Math.PI) / (Math.PI * 2) * n) % n;
               double hp = this.ringHp[s];
               if (hp < 0.999) {
                  int tr = (int)(t * 7.0);
                  double hsh = (SolarTex.HASH[s * 7 + tr * 131 & 65535] & 255) / 255.0;
                  a *= SolarTex.clamp01(hp * 1.8 - hsh * 0.8);
                  c = SolarTex.mix(c, -12965344, (1.0 - hp) * 0.5);
               }

               double py = uz * 0.26F;
               double pz = -uz * ci;
               double pl = ux * lx + py * ly + pz * lz;
               if (pl < 0.0) {
                  double qx = ux - pl * lx;
                  double qy = py - pl * ly;
                  double qz = pz - pl * lz;
                  double dd = qx * qx + qy * qy + qz * qz;
                  if (dd < 1.0) {
                     a *= 0.18 + 0.82 * SolarTex.sstep(0.9, 1.0, dd);
                  }
               }

               c = SolarTex.shade(c, 0.75 + 0.35 * SolarTex.clamp01(-ang * 0.2 + 0.5));
               if (!(a < 0.01)) {
                  int v = (int)(Math.min(1.0, a) * 255.0) << 24 | c & 16777215;
                  if (uy < 0.0) {
                     back[y * w + x] = v;
                  } else {
                     front[y * w + x] = v;
                  }
               }
            }
         }
      }
   }

   void update(float dt, float simTime) {
      this.now = simTime;
      this.rot = this.rot + this.rotSpeed * dt;
      this.crot = this.crot + this.cloudSpeed * dt;
      if (this.rot > Math.PI * 2) {
         this.rot -= Math.PI * 2;
      }

      if (this.crot > Math.PI * 2) {
         this.crot -= Math.PI * 2;
      }

      this.tempOff = this.tempOff * (float)Math.pow(0.985, dt);
      this.lightsOff = Math.max(0.0F, this.lightsOff - dt / 25.0F);
      this.shake = Math.max(0.0F, this.shake - dt * 1.6F);
      this.hitFlash = Math.max(0.0F, this.hitFlash - dt * 3.0F);
      if (this.destroyed) {
         this.destroyedT += dt;
      }

      this.rebuildT += dt;
      SolarPlanet.Maps m = this.hi;
      if (m != null) {
         int budget = 10;
         int nt = m.hot.length;

         for (int k = 0; k < nt && budget > 0; k++) {
            int t = m.hotPtr = (m.hotPtr + 1) % nt;
            if (m.hot[t]) {
               float el = simTime - m.coolT[t];
               if (!(el < 0.12F)) {
                  budget--;
                  m.coolT[t] = simTime;
                  if (!this.cool(m, t, el)) {
                     m.hot[t] = false;
                  }

                  m.mip(t);
                  this.ver++;
               }
            }
         }

         if (m.dirtyCount > 0) {
            int b2 = 40;

            for (int t = 0; t < nt && b2 > 0; t++) {
               if (m.dirty[t]) {
                  m.mip(t);
                  b2--;
                  m.dirtyCount--;
               }
            }

            if (b2 > 0) {
               m.dirtyCount = 0;
            }
         }
      }
   }

   private boolean cool(SolarPlanet.Maps m, int t, float el) {
      int x0 = t % m.tx * 64;
      int y0 = t / m.tx * 64;
      boolean any = false;
      float rate = 16.0F * el;

      for (int y = y0; y < y0 + 64 && y < m.H; y++) {
         for (int x = x0; x < x0 + 64 && x < m.W; x++) {
            int i = y << m.shift | x;
            int h = m.heat[i] & 255;
            if (h != 0) {
               int dm = m.dmg[i] & 255;
               int fl = dm >= 180 ? Math.min(215, (dm - 180) * 3) : 0;
               if (m.heat0 != null) {
                  fl = Math.max(fl, m.heat0[i] & 255);
               }

               if (h > fl) {
                  int nh = Math.max(fl, h - (int)(rate * (1.0F + h / 110.0F) + 0.5F));
                  m.heat[i] = (byte)nh;
                  if (nh > fl) {
                     any = true;
                  }
               }
            }
         }
      }

      return any;
   }

   static SolarPlanet.Sphere sphere(int D, float tilt) {
      long key = (long)D << 32 ^ Float.floatToIntBits(tilt);
      SolarPlanet.Sphere s = SPHERES.get(key);
      if (s == null) {
         if (SPHERES.size() > 40) {
            SPHERES.entrySet().removeIf(e -> e.getValue().D > 300);
         }

         s = new SolarPlanet.Sphere(D, tilt);
         SPHERES.put(key, s);
      }

      return s;
   }

   boolean refresh(SolarPlanet.Disc dc, boolean hiLevel, int pxBudget, boolean force) {
      SolarPlanet.Maps m = this.maps();
      if (m == null) {
         return false;
      } else {
         boolean useHi = hiLevel && m.alb1 != m.alb && m == this.hi;
         int mw = useHi ? m.W : m.W1;
         int r16 = (int)(this.rot / (Math.PI * 2) * 65536.0) & 65535;
         int c16 = (int)(this.crot / (Math.PI * 2) * 65536.0) & 65535;
         int texStep = Math.max(1, 65536 / mw);
         boolean changed = dc.lastBody != this
            || dc.lastMaps != m
            || dc.lastHi != useHi
            || dc.lastVer != this.ver
            || Math.abs((short)(r16 - dc.lastRot)) >= texStep
            || Math.abs((short)(c16 - dc.lastCrot)) >= texStep * 2;
         if (changed || force) {
            Arrays.fill(dc.need, true);
            dc.lastBody = this;
            dc.lastMaps = m;
            dc.lastHi = useHi;
            dc.lastVer = this.ver;
            dc.lastRot = r16;
            dc.lastCrot = c16;
         }

         int done = 0;
         boolean all = true;

         for (int k = 0; k < dc.slabs; k++) {
            int s = (dc.next + k) % dc.slabs;
            if (dc.need[s]) {
               int r0 = s * dc.slabH;
               int r1 = Math.min(dc.D, r0 + dc.slabH);
               int px = dc.sp.rowStart[r1] - dc.sp.rowStart[r0];
               if (!force && done > 0 && done + px > pxBudget) {
                  all = false;
               } else {
                  this.render(dc, s, m, useHi, r16, c16);
                  dc.need[s] = false;
                  Gx.B.update(dc.img(s), dc.buf[s]);
                  dc.uploads++;
                  done += px;
                  dc.next = (s + 1) % dc.slabs;
               }
            }
         }

         return all;
      }
   }

   private void shadeSlab(SolarPlanet.Disc dc, int r0, int r1) {
      SolarPlanet.Sphere sp = dc.sp;
      float Lx = dc.lx;
      float Ly = dc.ly;
      float Lz = dc.lz;
      float hz = Lz + 1.0F;
      float hl = (float)Math.sqrt(Lx * Lx + Ly * Ly + hz * hz);
      float hx = Lx / hl;
      float hy = Ly / hl;
      hz /= hl;
      boolean star = dc.star;

      for (int i = sp.rowStart[r0]; i < sp.rowStart[r1]; i++) {
         float nx = sp.nx[i];
         float ny = sp.ny[i];
         float nz = sp.nz[i];
         int cv = sp.cov[i];
         int night = 0;
         int spec = 0;
         int rm = 0;
         int lit;
         if (star) {
            lit = (int)((0.42F + 0.62F * Math.pow(nz, 0.45)) * 256.0);
         } else {
            float d = nx * Lx + ny * Ly + nz * Lz;
            float l = Math.max(0.0F, (d + 0.05F) / 1.05F);
            lit = (int)((0.035F + 0.965F * Math.pow(l, 0.85)) * 256.0);
            night = (int)(SolarTex.sstep(0.1, -0.14, d) * 255.0);
            float sh = nx * hx + ny * hy + nz * hz;
            spec = sh > 0.0F ? (int)(Math.pow(sh, 70.0) * 200.0 + Math.pow(sh, 12.0) * 26.0) : 0;
            double rr = Math.pow(1.0F - nz, 2.2) * (0.12 + 0.88 * SolarTex.sstep(-0.3, 0.55, d));
            rm = (int)Math.min(255.0, rr * 300.0);
         }

         dc.shade[i] = Math.min(511, lit) | night << 9 | Math.min(255, spec) << 17 | cv << 25;
         dc.rim[i] = (byte)rm;
      }
   }

   private void render(SolarPlanet.Disc dc, int s, SolarPlanet.Maps m, boolean useHi, int r16, int c16) {
      SolarPlanet.Sphere sp = dc.sp;
      int D = dc.D;
      int r0 = s * dc.slabH;
      int r1 = Math.min(D, r0 + dc.slabH);
      if (dc.slabL[s] != dc.lver) {
         this.shadeSlab(dc, r0, r1);
         dc.slabL[s] = dc.lver;
      }

      int[] alb = useHi ? m.alb : m.alb1;
      byte[] heat = useHi ? m.heat : m.heat1;
      byte[] city = useHi ? m.city : m.city1;
      byte[] cloud = m.cloud;
      int mw = useHi ? m.W : m.W1;
      int mh = useHi ? m.H : m.H1;
      int sh = useHi ? m.shift : m.shift1;
      int cw = m.cW;
      int chh = m.cH;
      char[] lat = sp.lat;
      char[] lon = sp.lon;
      float[] nz = sp.nz;
      int[] shade = dc.shade;
      byte[] rim = dc.rim;
      int[] out = dc.buf[s];
      int[] HP = HEAT;
      float a = Math.max(0.0F, Math.min(1.0F, this.atmo));
      int atR = (int)((this.atmoCol >> 16 & 0xFF) * a);
      int atG = (int)((this.atmoCol >> 8 & 0xFF) * a);
      int atB = (int)((this.atmoCol & 0xFF) * a);
      int cc = SolarTex.mix(this.cloudCol, -11909048, this.dark);
      int clR = cc >> 16 & 0xFF;
      int clG = cc >> 8 & 0xFF;
      int clB = cc & 0xFF;
      int cloudMul = (int)(256.0F * Math.min(1.0F, a * 1.1F));
      int cityMul = (int)(256.0F * Math.max(0.0F, 1.0F - this.lightsOff) * Math.sqrt(Math.max(0.0, this.gAlive)) * (this.destroyed ? 0 : 1));
      int ciR = this.cityCol >> 16 & 0xFF;
      int ciG = this.cityCol >> 8 & 0xFF;
      int ciB = this.cityCol & 0xFF;
      boolean star = dc.star;
      byte[] dm = useHi ? m.dmg : null;
      float blx = dc.lx;
      float bly = dc.ly;
      int wmask = mw - 1;
      Arrays.fill(out, 0);

      for (int r = r0; r < r1; r++) {
         int xs = sp.x0[r];
         int xe = sp.x1[r];
         if (xe >= xs) {
            int i = sp.rowStart[r];
            int o = (r - r0) * D + xs;

            for (int x = xs; x <= xe; o++) {
               label155: {
                  int la = lat[i];
                  int lo = lon[i];
                  int row = la * mh >>> 16;
                  int col = (lo + r16 & 65535) * mw >>> 16;
                  int idx = row << sh | col;
                  int c = alb[idx];
                  int sa = c >>> 24;
                  int shd = shade[i];
                  int cov = shd >>> 25 << 1;
                  if (sa == 1 && nz[i] < 0.42F) {
                     cov = cov * (int)Math.max(0.0F, (nz[i] - 0.3F) * 2000.0F) >> 8;
                     if (cov <= 0) {
                        break label155;
                     }

                     if (cov > 254) {
                        cov = 254;
                     }
                  }

                  int lit = shd & 511;
                  if (dm != null && !star) {
                     int d0 = dm[idx] & 255;
                     int rowI = idx >> sh;
                     int colI = idx & wmask;
                     int dl = dm[rowI << sh | colI - 4 & wmask] & 255;
                     int dr = dm[rowI << sh | colI + 4 & wmask] & 255;
                     int du = rowI > 3 ? dm[idx - 4 * mw] & 255 : d0;
                     int dd = rowI < mh - 4 ? dm[idx + 4 * mw] & 255 : d0;
                     if ((d0 | dl | dr | du | dd) != 0) {
                        float bump = ((dr - dl) * blx + (dd - du) * bly) * 6.0F;
                        lit = (int)(lit * (1.0F - d0 / 330.0F) + bump * Math.max(0.25F, lit / 300.0F));
                        if (lit < 0) {
                           lit = 0;
                        }

                        if (lit > 480) {
                           lit = 480;
                        }
                     }
                  }

                  int R = (c >> 16 & 0xFF) * lit >> 8;
                  int G = (c >> 8 & 0xFF) * lit >> 8;
                  int B = (c & 0xFF) * lit >> 8;
                  int h = heat[idx] & 255;
                  if (star) {
                     if (h > 0) {
                        int e = HP[h];
                        R += e >> 16 & 0xFF;
                        G += e >> 8 & 0xFF;
                        B += e & 0xFF;
                     }
                  } else {
                     int sp2 = shd >>> 17 & 0xFF;
                     if (sa > 1 && sp2 > 0) {
                        int k = sp2 * sa >> 8;
                        R += k;
                        G += k;
                        B += k + (k >> 2);
                     }

                     int cl = 0;
                     if (cloud != null) {
                        int crow = la * chh >>> 16;
                        int ccol = (lo + c16 & 65535) * cw >>> 16;
                        cl = (cloud[crow * cw + ccol] & 255) * cloudMul >> 8;
                        if (cl > 0) {
                           int cr = clR * lit >> 8;
                           int cg = clG * lit >> 8;
                           int cb = clB * lit >> 8;
                           R += (cr - R) * cl >> 8;
                           G += (cg - G) * cl >> 8;
                           B += (cb - B) * cl >> 8;
                        }
                     }

                     int night = shd >>> 9 & 0xFF;
                     if (night > 0) {
                        int ct = city[idx] & 255;
                        if (ct > 0) {
                           int k = (ct * night >> 8) * cityMul >> 8;
                           k = k * (300 - cl) >> 8;
                           R += ciR * k >> 8;
                           G += ciG * k >> 8;
                           B += ciB * k >> 8;
                        }
                     }

                     if (h > 0) {
                        int e = HP[h];
                        int f = 256 - (cl >> 1);
                        R += (e >> 16 & 0xFF) * f >> 8;
                        G += (e >> 8 & 0xFF) * f >> 8;
                        B += (e & 0xFF) * f >> 8;
                     }

                     int rm = rim[i] & 255;
                     if (rm > 0) {
                        R += atR * rm >> 8;
                        G += atG * rm >> 8;
                        B += atB * rm >> 8;
                     }
                  }

                  if (R > 255) {
                     R = 255;
                  }

                  if (G > 255) {
                     G = 255;
                  }

                  if (B > 255) {
                     B = 255;
                  }

                  out[o] = cov << 24 | R << 16 | G << 8 | B;
               }

               x++;
               i++;
            }
         }
      }
   }

   static void drawDisc(SolarPlanet.Disc dc, float cx, float cy, float r, int argb) {
      if (dc.sp != null) {
         float k = 2.0F * r / dc.D;
         int x = Math.round(cx - r);
         int w = Math.round(cx + r) - x;
         float top = cy - r;

         for (int s = 0; s < dc.slabs; s++) {
            int ya = Math.round(top + s * dc.slabH * k);
            int yb = Math.round(top + (s + 1) * dc.slabH * k);
            if (yb > ya) {
               Gx.image(dc.img(s), x, ya, w, yb - ya, argb);
            }
         }
      }
   }

   static int[] snapshot(SolarPlanet.Disc dc) {
      int[] px = new int[dc.D * dc.D];

      for (int s = 0; s < dc.slabs; s++) {
         int r0 = s * dc.slabH;
         int rows = Math.min(dc.slabH, dc.D - r0);
         System.arraycopy(dc.buf[s], 0, px, r0 * dc.D, rows * dc.D);
      }

      return px;
   }

   int avgColor() {
      SolarPlanet.Maps m = this.maps();
      if (m == null) {
         return -8355712;
      } else {
         long r = 0L;
         long g = 0L;
         long b = 0L;
         int n = 0;

         for (int i = 0; i < m.alb1.length; i += 97) {
            int c = m.alb1[i];
            r += c >> 16 & 0xFF;
            g += c >> 8 & 0xFF;
            b += c & 0xFF;
            n++;
         }

         return 0xFF000000 | (int)(r / n) << 16 | (int)(g / n) << 8 | (int)(b / n);
      }
   }

   static {
      for (int h = 0; h < 256; h++) {
         double t = h / 255.0;
         int c = SolarTex.ramp(t, new double[]{0.0, 0.15, 0.4, 0.7, 1.0}, new int[]{-16777216, -10876416, -3000310, -26070, -2872});
         HEAT[h] = c & 16777215;
      }

      SPHERES = new HashMap<>();
   }

   static final class Disc {
      final String key;
      int D;
      int slabs;
      int slabH;
      int[][] buf;
      SolarPlanet.Sphere sp;
      int[] shade;
      byte[] rim;
      float lx = 9.0F;
      float ly;
      float lz;
      int lver = 0;
      int[] slabL;
      boolean[] need;
      int next;
      int lastRot = Integer.MIN_VALUE;
      int lastCrot = Integer.MIN_VALUE;
      int lastVer = -1;
      SolarPlanet lastBody;
      SolarPlanet.Maps lastMaps;
      boolean lastHi;
      boolean star;
      int uploads;

      Disc(String key) {
         this.key = key;
      }

      void setup(int D, float tilt, boolean star) {
         if (this.sp == null || this.D != D || this.sp.tilt != tilt || this.star != star) {
            this.D = D;
            this.star = star;
            this.sp = SolarPlanet.sphere(D, tilt);
            this.slabs = D <= 200 ? 1 : (D <= 420 ? 2 : (D <= 760 ? 4 : 8));
            this.slabH = (D + this.slabs - 1) / this.slabs;
            this.buf = new int[this.slabs][D * this.slabH];
            this.shade = new int[this.sp.n];
            this.rim = new byte[this.sp.n];
            this.slabL = new int[this.slabs];
            Arrays.fill(this.slabL, -1);
            this.need = new boolean[this.slabs];
            Arrays.fill(this.need, true);
            this.lx = 9.0F;
            this.lastVer = -1;
         }
      }

      void light(float x, float y, float z) {
         if (!(Math.abs(x - this.lx) + Math.abs(y - this.ly) + Math.abs(z - this.lz) < 0.004F)) {
            this.lx = x;
            this.ly = y;
            this.lz = z;
            this.lver++;
            Arrays.fill(this.need, true);
         }
      }

      Gx.Img img(int s) {
         int[] b = this.buf[s];
         int w = this.D;
         int h = this.slabH;
         return Gx.get("solar:" + this.key + ":" + this.D + ":" + s + ":" + this.slabs, 0, () -> new int[][]{b, {w, h}});
      }
   }

   static final class Maps {
      final int W;
      final int H;
      final int shift;
      final int cW;
      final int cH;
      final int[] alb;
      final byte[] hgt;
      final byte[] heat;
      final byte[] dmg;
      final byte[] city;
      final byte[] cloud;
      final byte[] heat0;
      final float[] cosRow;
      int W1;
      int H1;
      int shift1;
      int[] alb1;
      byte[] heat1;
      byte[] city1;
      double popW;
      double areaW;
      byte sea;
      final int TS = 64;
      final int tx;
      final int ty;
      final boolean[] hot;
      final boolean[] dirty;
      final float[] coolT;
      int hotPtr;
      int dirtyCount;

      Maps(int W, int H, boolean clouds, boolean lava) {
         this.W = W;
         this.H = H;
         this.shift = Integer.numberOfTrailingZeros(W);
         this.alb = new int[W * H];
         this.hgt = new byte[W * H];
         this.heat = new byte[W * H];
         this.dmg = new byte[W * H];
         this.city = new byte[W * H];
         this.heat0 = lava ? new byte[W * H] : null;
         this.cW = W >= 1024 ? W / 2 : W;
         this.cH = H * this.cW / W;
         this.cloud = clouds ? new byte[this.cW * this.cH] : null;
         this.cosRow = new float[H];

         for (int y = 0; y < H; y++) {
            this.cosRow[y] = (float)Math.max(0.004, Math.cos((0.5 - (y + 0.5) / H) * Math.PI));
         }

         this.tx = Math.max(1, W / 64);
         this.ty = Math.max(1, H / 64);
         this.hot = new boolean[this.tx * this.ty];
         this.dirty = new boolean[this.tx * this.ty];
         this.coolT = new float[this.tx * this.ty];
      }

      void finish() {
         double pw = 0.0;
         double aw = 0.0;

         for (int y = 0; y < this.H; y++) {
            double c = this.cosRow[y];
            double s = 0.0;

            for (int x = 0; x < this.W; x++) {
               s += this.city[y * this.W + x] & 255;
            }

            pw += s * c;
            aw += c * this.W * 255.0;
         }

         this.popW = Math.max(1.0, pw);
         this.areaW = aw;
         if (this.W > 1024) {
            this.W1 = this.W / 2;
            this.H1 = this.H / 2;
            this.shift1 = this.shift - 1;
            this.alb1 = new int[this.W1 * this.H1];
            this.heat1 = new byte[this.W1 * this.H1];
            this.city1 = new byte[this.W1 * this.H1];

            for (int t = 0; t < this.tx * this.ty; t++) {
               this.mip(t);
            }
         } else {
            this.W1 = this.W;
            this.H1 = this.H;
            this.shift1 = this.shift;
            this.alb1 = this.alb;
            this.heat1 = this.heat;
            this.city1 = this.city;
         }

         for (int t = 0; t < this.hot.length; t++) {
            if (this.heat0 != null) {
               this.hot[t] = false;
            }
         }
      }

      void mip(int t) {
         this.dirty[t] = false;
         if (this.alb1 != this.alb) {
            int x0 = t % this.tx * 64;
            int y0 = t / this.tx * 64;

            for (int y = y0; y < y0 + 64 && y < this.H; y += 2) {
               for (int x = x0; x < x0 + 64 && x < this.W; x += 2) {
                  int i = y * this.W + x;
                  int j = i + this.W;
                  int a = this.alb[i];
                  int b = this.alb[i + 1];
                  int c = this.alb[j];
                  int d = this.alb[j + 1];
                  int sa = a >>> 24;
                  int sb = b >>> 24;
                  int sc = c >>> 24;
                  int sd = d >>> 24;
                  int s = sa != 1 && sb != 1 && sc != 1 && sd != 1 ? sa + sb + sc + sd >> 2 : 1;
                  if (s == 1 && (sa != 1 || sb != 1 || sc != 1 || sd != 1) && sa + sb + sc + sd < 8) {
                     s = 1;
                  }

                  int r = (a >> 16 & 0xFF) + (b >> 16 & 0xFF) + (c >> 16 & 0xFF) + (d >> 16 & 0xFF) >> 2;
                  int g = (a >> 8 & 0xFF) + (b >> 8 & 0xFF) + (c >> 8 & 0xFF) + (d >> 8 & 0xFF) >> 2;
                  int bl = (a & 0xFF) + (b & 0xFF) + (c & 0xFF) + (d & 0xFF) >> 2;
                  int o = (y >> 1) * this.W1 + (x >> 1);
                  this.alb1[o] = s << 24 | r << 16 | g << 8 | bl;
                  int h = Math.max(Math.max(this.heat[i] & 255, this.heat[i + 1] & 255), Math.max(this.heat[j] & 255, this.heat[j + 1] & 255));
                  int h2 = (this.heat[i] & 255) + (this.heat[i + 1] & 255) + (this.heat[j] & 255) + (this.heat[j + 1] & 255) >> 2;
                  this.heat1[o] = (byte)(h + h2 >> 1);
                  this.city1[o] = (byte)((this.city[i] & 255) + (this.city[i + 1] & 255) + (this.city[j] & 255) + (this.city[j + 1] & 255) >> 2);
               }
            }
         }
      }

      void markRect(int x0, int y0, int x1, int y1, boolean heatToo, float now) {
         int ty0 = Math.max(0, y0 / 64);
         int ty1 = Math.min(this.ty - 1, y1 / 64);
         if (x1 - x0 >= this.W - 1) {
            x0 = 0;
            x1 = this.W - 1;
         }

         for (int yy = ty0; yy <= ty1; yy++) {
            for (int xx = x0 / 64 - (x0 < 0 ? 1 : 0); xx <= Math.floorDiv(x1, 64); xx++) {
               int t = yy * this.tx + Math.floorMod(xx, this.tx);
               if (!this.dirty[t]) {
                  this.dirty[t] = true;
                  this.dirtyCount++;
               }

               if (heatToo && !this.hot[t]) {
                  this.hot[t] = true;
                  this.coolT[t] = now;
               }
            }
         }
      }
   }

   static final class Sphere {
      final int D;
      final float tilt;
      final int[] x0;
      final int[] x1;
      final int[] rowStart;
      final char[] lat;
      final char[] lon;
      final float[] nx;
      final float[] ny;
      final float[] nz;
      final byte[] cov;
      final int n;

      Sphere(int D, float tilt) {
         this.D = D;
         this.tilt = tilt;
         this.x0 = new int[D];
         this.x1 = new int[D];
         this.rowStart = new int[D + 1];
         double R = D / 2.0;
         int cnt = 0;

         for (int y = 0; y < D; y++) {
            double yy = y + 0.5 - R;
            double hw = Math.sqrt(Math.max(0.0, (R + 0.5) * (R + 0.5) - yy * yy));
            int a = (int)Math.floor(R - hw);
            int b = (int)Math.ceil(R + hw) - 1;
            a = Math.max(0, a);
            b = Math.min(D - 1, b);
            this.x0[y] = a;
            this.x1[y] = b;
            this.rowStart[y] = cnt;
            if (b >= a) {
               cnt += b - a + 1;
            }
         }

         this.rowStart[D] = cnt;
         this.n = cnt;
         this.lat = new char[this.n];
         this.lon = new char[this.n];
         this.nx = new float[this.n];
         this.ny = new float[this.n];
         this.nz = new float[this.n];
         this.cov = new byte[this.n];
         double ct = Math.cos(tilt);
         double st = Math.sin(tilt);
         int i = 0;

         for (int yx = 0; yx < D; yx++) {
            for (int x = this.x0[yx]; x <= this.x1[yx]; i++) {
               double fx = (x + 0.5 - R) / R;
               double fy = (yx + 0.5 - R) / R;
               double rp = Math.sqrt(fx * fx + fy * fy) * R;
               double cv = Math.max(0.0, Math.min(1.0, R - rp + 0.5));
               this.cov[i] = (byte)(cv * 127.0);
               double r2 = fx * fx + fy * fy;
               if (r2 > 0.9999) {
                  double k = 0.99995 / Math.sqrt(r2);
                  fx *= k;
                  fy *= k;
                  r2 = 0.9999;
               }

               double fz = Math.sqrt(1.0 - r2);
               this.nx[i] = (float)fx;
               this.ny[i] = (float)fy;
               this.nz[i] = (float)fz;
               double xr = fx * ct + fy * st;
               double yr = -fx * st + fy * ct;
               double la = Math.asin(Math.max(-1.0, Math.min(1.0, -yr)));
               double lo = Math.atan2(xr, fz);
               this.lat[i] = (char)Math.max(0, Math.min(65535, (int)((0.5 - la / Math.PI) * 65536.0)));
               this.lon[i] = (char)((int)Math.round(lo / (Math.PI * 2) * 65536.0) & 65535);
               x++;
            }
         }
      }
   }

   interface TexelFn {
      void apply(SolarPlanet.Maps var1, int var2, int var3, int var4);
   }
}
