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

   SolarPlanet(int var1, String var2, String var3, long var4, float var6, int var7, float var8, float var9, float var10, long var11, boolean var13) {
      this.type = var1;
      this.name = var2;
      this.kind = var3;
      this.pop = var4;
      this.baseTemp = var6;
      this.atmoCol = var7;
      this.atmo0 = var8;
      this.size = var9;
      this.tilt = var10;
      this.seed = var11;
      this.minor = var13;
      this.inhabited = var4 > 0L;
      this.atmo = var8;
      this.rotSpeed = (float)((Math.PI * 2) / (var1 == 3 ? 70 : (var1 == 0 ? 160 : 120)));
      this.cloudSpeed = this.rotSpeed * 1.25F;
      this.cloudCol = var1 == 2 ? -1322312 : (var1 == 5 ? -11647164 : (var1 == 7 ? -727344 : -1));
      this.cityCol = var1 == 6 ? -6297345 : (var1 == 4 ? -4202241 : -15240);
      switch (var1) {
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
      SolarPlanet.Maps var1 = this.hi;
      return var1 != null ? var1 : this.thumb;
   }

   void request(boolean var1) {
      if (this.thumb == null && !this.thumbPending) {
         this.thumbPending = true;
         int var2 = this.genToken;
         SolarTex.POOL.execute(() -> {
            SolarPlanet.Maps var2x = SolarTex.generate(this.type, this.seed, 256, 128, this.inhabited, false);
            if (var2 == this.genToken) {
               this.thumb = var2x;
               this.ver++;
            }

            this.thumbPending = false;
         });
      }

      if (var1 && this.hi == null && !this.hiPending && !this.destroyed) {
         this.hiPending = true;
         int var4 = this.genToken;
         int var3 = this.type != 0 && !this.minor ? 2048 : 1024;
         SolarTex.POOL.execute(() -> {
            SolarPlanet.Maps var3x = SolarTex.generate(this.type, this.seed, var3, var3 / 2, this.inhabited, true);
            if (var4 == this.genToken) {
               this.hi = var3x;
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

      for (SolarPlanet var2 : this.moons) {
         var2.repair();
      }
   }

   double alive() {
      if (this.pop > 0L && !this.destroyed) {
         SolarPlanet.Maps var1 = this.hi;
         double var2 = var1 == null ? 1.0 : Math.max(0.0, 1.0 - this.killW / var1.popW);
         return this.pop * var2 * Math.max(0.0, this.gAlive);
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
         SolarPlanet.Maps var1 = this.hi;
         double var2 = var1 == null ? 0.0 : this.dmgW / var1.areaW * 2.6;
         return (float)Math.max(0.0, 1.0 - var2 - this.core);
      }
   }

   float temp() {
      return this.baseTemp + this.tempOff;
   }

   void killGlobal(double var1) {
      this.gAlive = this.gAlive * Math.max(0.0, 1.0 - var1);
   }

   double[] unproject(double var1, double var3) {
      double var5 = var1 * var1 + var3 * var3;
      if (var5 > 0.9995) {
         double var7 = 0.9997 / Math.sqrt(var5);
         var1 *= var7;
         var3 *= var7;
         var5 = 0.9994;
      }

      double var17 = Math.cos(this.tilt);
      double var9 = Math.sin(this.tilt);
      double var11 = var1 * var17 + var3 * var9;
      double var13 = -var1 * var9 + var3 * var17;
      double var15 = Math.sqrt(Math.max(0.0, 1.0 - var5));
      return new double[]{Math.atan2(var11, var15) + this.rot, Math.asin(Math.max(-1.0, Math.min(1.0, -var13)))};
   }

   double[] project(double var1, double var3) {
      double var5 = var1 - this.rot;
      double var7 = Math.cos(var3) * Math.sin(var5);
      double var9 = -Math.sin(var3);
      double var11 = Math.cos(var3) * Math.cos(var5);
      double var13 = Math.cos(this.tilt);
      double var15 = Math.sin(this.tilt);
      return new double[]{var7 * var13 - var9 * var15, var7 * var15 + var9 * var13, var11};
   }

   void paint(double var1, double var3, float var5, int var6, float var7, float var8, float var9, float var10, int var11) {
      SolarPlanet.Maps var12 = this.hi;
      if (var12 != null && !this.destroyed) {
         int var13 = var12.W;
         int var14 = var12.H;
         float var15 = var13 / 2048.0F;
         var5 = Math.max(0.7F, var5 * var15);
         var10 = Math.max(1.0F, var10);
         float var16 = var5 * var10;
         double var17 = (0.5 - var3 / Math.PI) * var14 - 0.5;
         double var19 = var1 / (Math.PI * 2) * var13 - 0.5;
         var19 -= Math.floor(var19 / var13) * var13;
         int var21 = (int)Math.max(0.0, Math.floor(var17 - var16));
         int var22 = (int)Math.min((double)(var14 - 1), Math.ceil(var17 + var16));
         double var23 = 0.0;
         double var25 = 0.0;
         int var27 = Integer.MAX_VALUE;
         int var28 = Integer.MIN_VALUE;
         int[] var29 = var12.alb;
         byte[] var30 = var12.dmg;
         byte[] var31 = var12.heat;
         byte[] var32 = var12.city;
         byte[] var33 = var12.heat0;
         int var34 = var12.sea & 255;
         byte[] var35 = SolarTex.HASH;
         boolean var36 = this.type == 0;

         for (int var37 = var21; var37 <= var22; var37++) {
            float var38 = var12.cosRow[var37];
            float var39 = (float)(var37 - var17);
            float var40 = var16 * var16 - var39 * var39;
            if (!(var40 < 0.0F)) {
               float var41 = (float)Math.sqrt(var40) / var38;
               if (var41 > var13 / 2.0F) {
                  var41 = var13 / 2.0F;
               }

               int var42 = (int)Math.floor(var19 - var41);
               int var43 = (int)Math.ceil(var19 + var41);
               if (var42 < var27) {
                  var27 = var42;
               }

               if (var43 > var28) {
                  var28 = var43;
               }

               double var44 = 0.0;
               double var46 = 0.0;

               for (int var48 = var42; var48 <= var43; var48++) {
                  int var49 = var48 & var13 - 1;
                  float var50 = (float)(var48 - var19) * var38;
                  float var51 = (float)Math.sqrt(var50 * var50 + var39 * var39) / var5;
                  int var52 = var35[(var48 >> 2) * 31 + (var37 >> 2) * 977 + (int)(var1 * 91.0) & 65535] & 255;
                  int var53 = var35[var49 * 7 + var37 * 1031 & 65535] & 255;
                  float var54 = var51 * (1.0F + (var52 - 128) * 0.0017F + (var53 - 128) * 5.0E-4F);
                  if (!(var54 >= var10)) {
                     int var55 = var37 << var12.shift | var49;
                     int var56 = var29[var55];
                     int var57 = var56 >>> 24;
                     int var58 = var56 & 16777215;
                     int var59 = var30[var55] & 255;
                     int var60 = var31[var55] & 255;
                     int var61 = var32[var55] & 255;
                     int var62 = var59;
                     int var63 = var60;
                     int var64 = var61;
                     int var65 = var57;
                     float var66 = 1.0F - var54;
                     switch (var6) {
                        case 0:
                        case 1:
                        case 11:
                        case 12:
                        case 13:
                        case 20:
                        case 22:
                           if (var6 == 13) {
                              float var95 = 1.0F - var54 / var10;
                              var64 = (int)(var61 * (1.0F - var9 * var95));
                              var58 = SolarTex.mix(var58, this.scorchCol, var95 * 0.12F * var7 / 60.0F);
                              var62 = Math.min(255, var59 + (int)(var7 * 0.08F * var95));
                           } else {
                              if (var54 < 1.0F) {
                                 float var96 = var6 == 12 ? 1.0F : (float)Math.sqrt(var66);
                                 var62 = Math.min(255, var59 + (int)(var7 * var96 * (var6 == 1 ? 0.12F : 1.0F)));
                                 if (var6 == 1) {
                                    var58 = SolarTex.mix(var58, this.scorchCol, Math.min(1.0F, var66 * 1.4F) * 0.85F);
                                 } else {
                                    int var104 = SolarTex.mix(this.floorCol, SolarTex.shade(this.floorCol, 1.5), var53 / 255.0);
                                    var58 = SolarTex.mix(var58, var104, Math.min(1.0F, var66 * 2.4F) * Math.min(1.0F, var7 / 90.0F + 0.3F));
                                    if (var54 > 0.72 && var6 != 12 && var6 != 22) {
                                       var58 = SolarTex.mix(var58, this.ejectaCol, (1.0F - Math.abs(var54 - 0.9F) / 0.18F) * 0.35F);
                                    }
                                 }

                                 if (var6 == 11) {
                                    var58 = SolarTex.mix(var58, -14019312, 0.7F);
                                    var63 = Math.min(255, var60 + (int)(var8 * var66));
                                 }

                                 if (var6 == 20) {
                                    var58 = SolarTex.mix(var58, -15071696, var66);
                                 }

                                 var65 = 0;
                                 var64 = (int)(var61 * (1.0F - Math.min(1.0F, var9 * 1.2F)));
                              } else {
                                 float var97 = 1.0F - (var54 - 1.0F) / (var10 - 1.0F);
                                 float var105 = var97 * var97 * Math.min(1.0F, var7 / 60.0F + 0.35F) * (var6 == 1 ? 1.0F : 0.8F);
                                 var58 = SolarTex.mix(var58, var6 == 20 ? -14021052 : this.scorchCol, var105);
                                 var64 = (int)(var61 * (1.0F - var9 * Math.min(1.0F, var97 * 1.3F)));
                                 if (var57 > 1) {
                                    var65 = (int)(var57 * (1.0F - var97 * 0.6F));
                                 }
                              }

                              if (var62 > 200) {
                                 var58 = SolarTex.mix(var58, this.coreCol, (var62 - 200) / 55.0F * (var36 ? 0.4F : 1.0F));
                              }

                              if (var62 >= 250 && !var36) {
                                 var65 = 1;
                              }

                              float var98 = var8 * (float)Math.pow(Math.max(0.0F, 1.05F - var54 / var10), 1.6);
                              if (var6 == 1) {
                                 var98 *= (var53 & 3) == 0 ? 1.4F : 0.7F;
                              }

                              var63 = Math.min(255, Math.max(var63, (int)(var60 + var98)));
                           }
                           break;
                        case 2:
                           float var94 = Math.min(1.0F, (var10 - var54) / var10 * 1.6F);
                           int var103 = SolarTex.mix(-1510148, -5714712, var53 / 255.0);
                           var58 = SolarTex.mix(var58, var103, var94 * 0.92F);
                           var63 = (int)(var60 * (1.0F - var94));
                           var65 = Math.max(2, (int)(var57 + (120 - var57) * var94));
                           var64 = (int)(var61 * (1.0F - var9 * var94));
                           var62 = Math.min(255, var59 + (int)(var7 * 0.15F * var94));
                           break;
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 8:
                        case 9:
                        case 19:
                           float var93 = Math.min(1.0F, (var10 - var54) / var10 * 2.2F);
                           int var102;
                           switch (var6) {
                              case 3:
                                 var102 = SolarTex.mix(-10134, -4684260, var53 / 255.0);
                                 var65 = Math.max(2, (int)(var57 + (200 - var57) * var93));
                                 break;
                              case 4:
                                 var102 = SolarTex.mix(-8588742, -14775748, var52 / 255.0);
                                 var65 = 0;
                                 break;
                              case 5:
                                 var102 = SolarTex.mix(-2709249, -9819448, var53 / 255.0);
                                 var65 = Math.max(2, (int)(var57 + (255 - var57) * var93));
                                 break;
                              case 6:
                                 var102 = SolarTex.mix(-6643026, -11906984, var53 / 255.0);
                                 var65 = Math.max(2, (int)(var57 + (140 - var57) * var93));
                                 break;
                              case 7:
                              default:
                                 var102 = SolarTex.mix(var11, SolarTex.shade(var11, 0.75), var53 / 400.0);
                                 var65 = (int)(var57 * (1.0F - var93));
                                 break;
                              case 8:
                                 var102 = SolarTex.mix(-1916540, -5207984, var53 / 255.0);
                                 var65 = (int)(var57 * (1.0F - var93));
                                 break;
                              case 9:
                                 var102 = (var52 & 64) != 0 ? -2312 : SolarTex.mix(-18736, -32850, var53 / 255.0);
                                 var65 = (int)(var57 * (1.0F - var93));
                           }

                           var58 = SolarTex.mix(var58, var102, var93);
                           if (var65 == 1) {
                              var65 = 2;
                           }

                           var64 = (int)(var61 * (1.0F - var9 * var93));
                           var62 = Math.min(255, var59 + (int)(var7 * var93 * 0.4F));
                           if (var8 > 0.0F) {
                              var63 = Math.min(255, (int)(var60 + var8 * var93 * var66));
                           }
                           break;
                        case 7:
                           int var92 = var12.hgt[var55] & 255;
                           if ((var57 <= 150 || !(var54 > 0.2F)) && (!(var92 > var34 + var7) || !(var54 > 0.35F))) {
                              float var101 = Math.min(1.0F, (var10 - var54) / var10 * 2.5F);
                              var58 = SolarTex.mix(var58, SolarTex.mix(-13734262, -10850726, var53 / 400.0), var101 * 0.85F);
                              var65 = Math.max(2, (int)(var57 + (200 - var57) * var101));
                              var64 = (int)(var61 * (1.0F - var9 * var101));
                              var63 = 0;
                           }
                           break;
                        case 10:
                           float var91 = Math.min(1.0F, (var10 - var54) / var10 * 2.0F);
                           if (((var53 ^ var52) & 3) == 0) {
                              int[] var100 = new int[]{-50325, -11745, -12856833, -8588742, -4956929, -30208};
                              var58 = SolarTex.mix(var58, var100[var53 % var100.length], var91);
                              var64 = (int)(var61 * (1.0F - var9 * var91));
                           }
                           break;
                        case 14:
                           float var90 = 1.0F - var54 / var10;
                           var64 = (int)(var61 * (1.0F - var9 * var90));
                           break;
                        case 15:
                           float var89 = var50 / var5;
                           float var99 = var39 / var5;
                           if (Math.abs(var89) < 0.95F && Math.abs(var99) < 0.5F) {
                              float var106 = (var89 + 0.95F) / 0.475F;
                              float var70 = (var99 + 0.5F) / 0.5F;
                              float var71 = var106 - (int)var106 - 0.5F;
                              float var72 = var70 - (int)var70 - 0.5F;
                              boolean var73 = var71 * var71 + var72 * var72 < 0.09F;
                              boolean var74 = var71 * var71 + var72 * var72 < 0.13F;
                              var58 = var73 ? SolarTex.shade(var11, 1.18) : (var74 ? SolarTex.shade(var11, 0.72) : var11);
                              if (Math.abs(var89) > 0.9F || Math.abs(var99) > 0.45F) {
                                 var58 = SolarTex.shade(var11, 0.62);
                              }

                              var65 = 150;
                              var64 = 0;
                              var63 = 0;
                              var62 = Math.min(255, var59 + 40);
                           } else if (var54 < var10) {
                              float var69 = 1.0F - (var54 - 1.0F) / (var10 - 1.0F);
                              var58 = SolarTex.mix(var58, this.scorchCol, Math.max(0.0F, var69) * 0.5F);
                              var64 = (int)(var61 * (1.0F - var9 * Math.max(0.0F, var69)));
                           }
                           break;
                        case 16:
                        case 17:
                        case 18:
                        case 21:
                           float var67 = Math.min(1.0F, (var10 - var54) / var10 * 2.2F);
                           int var68;
                           if (var6 == 16) {
                              var68 = var52 < 110 ? -14935526 : -724502;
                           } else if (var6 == 17) {
                              var68 = var54 > 0.85F ? -2778550 : (var52 > 200 ? -5233382 : -11680);
                           } else if (var6 == 18) {
                              if (var54 < 0.45F) {
                                 var68 = this.floorCol;
                                 var67 = var66 * 0.5F;
                              } else {
                                 var68 = var54 > 0.9F ? -3569074 : ((var53 & 15) == 0 ? -10496769 : -32832);
                              }
                           } else {
                              var68 = ((int)((var50 / var5 + 2.0F) * 7.0F) & 1) == 0 ? -723728 : -15329766;
                           }

                           var58 = SolarTex.mix(var58, var68, var67);
                           var65 = var6 == 21 ? 120 : 0;
                           var64 = (int)(var61 * (1.0F - var9 * var67));
                           var62 = Math.min(255, var59 + (int)(var7 * var67 * 0.5F));
                     }

                     if (var33 != null) {
                        var63 = Math.max(var63, var33[var55] & 255);
                     }

                     if (var62 >= 180) {
                        var63 = Math.max(var63, Math.min(215, (var62 - 180) * 3));
                     }

                     if (var65 == 1 && var62 < 250) {
                        var65 = 0;
                     }

                     var29[var55] = var65 << 24 | var58 & 16777215;
                     if (var62 != var59) {
                        var30[var55] = (byte)var62;
                        var46 += var62 - var59;
                     }

                     if (var63 != var60) {
                        var31[var55] = (byte)var63;
                     }

                     if (var64 < var61) {
                        var32[var55] = (byte)var64;
                        var44 += var61 - var64;
                     }
                  }
               }

               var23 += var44 * var38;
               var25 += var46 * var38;
            }
         }

         if (var12.cloud != null && (var6 <= 1 || var6 == 12 || var6 == 11) && var7 > 20.0F) {
            int var78 = var12.cW;
            int var79 = var12.cH;
            float var80 = var16 * var78 / var13 * 1.2F;
            double var81 = (0.5 - var3 / Math.PI) * var79;
            double var82 = var1 / (Math.PI * 2) * var78;

            for (int var83 = (int)Math.max(0.0, var81 - var80); var83 <= Math.min((double)(var79 - 1), var81 + var80); var83++) {
               float var45 = (float)Math.max(0.02, Math.cos((0.5 - (var83 + 0.5) / var79) * Math.PI));
               float var84 = (float)(var83 - var81);
               float var47 = (float)Math.sqrt(Math.max(0.0F, var80 * var80 - var84 * var84)) / var45;
               if (var47 > var78 / 2.0F) {
                  var47 = var78 / 2.0F;
               }

               for (int var85 = (int)(var82 - var47); var85 <= (int)(var82 + var47); var85++) {
                  float var86 = (float)(var85 - var82) * var45;
                  float var87 = (float)Math.sqrt(var86 * var86 + var84 * var84) / var80;
                  if (!(var87 >= 1.0F)) {
                     int var88 = var83 * var78 + Math.floorMod(var85, var78);
                     var12.cloud[var88] = (byte)((var12.cloud[var88] & 255) * Math.min(1.0F, var87 * var87 * 1.2F));
                  }
               }
            }
         }

         if (var27 <= var28) {
            var12.markRect(var27, var21, var28, var22, true, this.now);
         }

         this.killW += var23;
         this.dmgW += var25;
         this.ver++;
      }
   }

   void crack(double var1, double var3, float var5, float var6, float var7, float var8, Random var9) {
      double var10 = var9.nextDouble() * Math.PI * 2.0;
      double var12 = var6 * 0.9 / 2048.0 * Math.PI * 2.0;
      int var14 = (int)(var5 / Math.max(0.5, var6 * 0.9));

      for (int var15 = 0; var15 < var14; var15++) {
         var10 += (var9.nextDouble() - 0.5) * 0.7;
         double var16 = Math.max(0.1, Math.cos(var3));
         var1 += Math.cos(var10) * var12 / var16;
         var3 += Math.sin(var10) * var12;
         if (var3 > 1.5 || var3 < -1.5) {
            var10 = -var10;
            var3 = Math.max(-1.5, Math.min(1.5, var3));
         }

         float var18 = var6 * (1.0F - (float)var15 / var14 * 0.6F);
         this.paint(var1, var3, var18, 22, 230.0F, var7, var8, 2.2F, 0);
         if (var9.nextFloat() < 0.04F && var14 > 20) {
            this.crack(var1, var3, var5 * 0.35F, var6 * 0.7F, var7, var8, var9);
         }
      }
   }

   void rowsPass(int var1, int var2, SolarPlanet.TexelFn var3) {
      SolarPlanet.Maps var4 = this.hi;
      if (var4 != null && !this.destroyed) {
         var1 = Math.max(0, var1);
         var2 = Math.min(var4.H, var2);
         if (var2 > var1) {
            long var5 = cityRows(var4, var1, var2);

            for (int var7 = var1; var7 < var2; var7++) {
               for (int var8 = 0; var8 < var4.W; var8++) {
                  var3.apply(var4, var8, var7, var7 << var4.shift | var8);
               }
            }

            long var11 = cityRows(var4, var1, var2);
            this.killW = this.killW + (double)Math.max(0L, var5 - var11) * var4.cosRow[(var1 + var2) / 2];
            var4.markRect(0, var1, var4.W - 1, var2 - 1, true, this.now);
            this.ver++;
         }
      }
   }

   private static long cityRows(SolarPlanet.Maps var0, int var1, int var2) {
      long var3 = 0L;

      for (int var5 = var1 * var0.W; var5 < var2 * var0.W; var5++) {
         var3 += var0.city[var5] & 255;
      }

      return var3;
   }

   void damageRing(double var1, float var3, float var4) {
      int var5 = this.ringHp.length;
      int var6 = (int)Math.floor((var1 + Math.PI) / (Math.PI * 2) * var5);
      int var7 = Math.max(0, (int)var4);

      for (int var8 = -var7; var8 <= var7; var8++) {
         int var9 = Math.floorMod(var6 + var8, var5);
         this.ringHp[var9] = Math.max(0.0F, this.ringHp[var9] - var3 * (1.0F - (float)Math.abs(var8) / (var7 + 1)));
      }

      this.ringVer++;
   }

   float ringAlive() {
      if (!this.ringed) {
         return 0.0F;
      } else {
         float var1 = 0.0F;

         for (float var5 : this.ringHp) {
            var1 += var5;
         }

         return var1 / this.ringHp.length;
      }
   }

   Gx.Img[] ringImgs(int var1, int var2, float var3, float var4, float var5, float var6) {
      String var7 = "solar:ring:" + this.name + ":" + var1 + "x" + var2;
      Gx.Img var10 = Gx.get(var7 + ":b", 0, () -> new int[][]{new int[var1 * var2], {var1, var2}});
      Gx.Img var11 = Gx.get(var7 + ":f", 0, () -> new int[][]{new int[var1 * var2], {var1, var2}});
      long var12 = System.currentTimeMillis();
      if (!var7.equals(this.ringKey) || this.ringBuilt != this.ringVer && var12 - this.ringBuiltAt > 350L) {
         int[] var14 = new int[var1 * var2];
         int[] var15 = new int[var1 * var2];
         this.buildRing(var14, var15, var1, var2, var3, var4, var5, var6);
         Gx.B.update(var10, var14);
         Gx.B.update(var11, var15);
         this.ringBuilt = this.ringVer;
         this.ringBuiltAt = var12;
         this.ringKey = var7;
      }

      return new Gx.Img[]{var10, var11};
   }

   private void buildRing(int[] var1, int[] var2, int var3, int var4, float var5, float var6, float var7, float var8) {
      double var9 = var3 / 2.0;
      double var11 = var4 / 2.0;
      double var13 = Math.cos(this.tilt);
      double var15 = Math.sin(this.tilt);
      double var17 = Math.asin(0.26F);
      double var19 = Math.cos(var17);
      double var21 = var6 * var13 + var7 * var15;
      double var23 = -var6 * var15 + var7 * var13;
      double var25 = var8;
      int var27 = this.ringHp.length;
      int[] var28 = this.ringCols;

      for (int var29 = 0; var29 < var4; var29++) {
         for (int var30 = 0; var30 < var3; var30++) {
            double var31 = var30 + 0.5 - var9;
            double var33 = var29 + 0.5 - var11;
            double var35 = (var31 * var13 + var33 * var15) / var5;
            double var37 = (-var31 * var15 + var33 * var13) / var5;
            double var39 = var37 / 0.26F;
            double var41 = Math.sqrt(var35 * var35 + var39 * var39);
            if (!(var41 < 1.28F) && !(var41 > 2.3F)) {
               double var43 = (var41 - 1.28F) / 1.02F;
               double var45 = 0.5 + 0.5 * Math.sin(var43 * 41.0) * Math.sin(var43 * 13.0 + 1.3) + 0.25 * Math.sin(var43 * 97.0);
               var45 = Math.max(0.05, Math.min(1.0, var45));
               if (var43 > 0.56 && var43 < 0.62) {
                  var45 *= 0.08;
               }

               if (var43 > 0.86 && var43 < 0.885) {
                  var45 *= 0.25;
               }

               double var47 = var45 * Math.min(1.0, var43 * 9.0) * Math.min(1.0, (1.0 - var43) * 12.0) * 0.9;
               int var49 = SolarTex.mix(var28[0], var28[1], var43);
               var49 = SolarTex.mix(var49, var28[2], SolarTex.clamp01(Math.sin(var43 * 23.0) * 0.5 + 0.2));
               double var50 = Math.atan2(var39, var35);
               int var52 = (int)((var50 + Math.PI) / (Math.PI * 2) * var27) % var27;
               double var53 = this.ringHp[var52];
               if (var53 < 0.999) {
                  int var55 = (int)(var43 * 7.0);
                  double var56 = (SolarTex.HASH[var52 * 7 + var55 * 131 & 65535] & 255) / 255.0;
                  var47 *= SolarTex.clamp01(var53 * 1.8 - var56 * 0.8);
                  var49 = SolarTex.mix(var49, -12965344, (1.0 - var53) * 0.5);
               }

               double var57 = var39 * 0.26F;
               double var59 = -var39 * var19;
               double var61 = var35 * var21 + var57 * var23 + var59 * var25;
               if (var61 < 0.0) {
                  double var63 = var35 - var61 * var21;
                  double var65 = var57 - var61 * var23;
                  double var67 = var59 - var61 * var25;
                  double var69 = var63 * var63 + var65 * var65 + var67 * var67;
                  if (var69 < 1.0) {
                     var47 *= 0.18 + 0.82 * SolarTex.sstep(0.9, 1.0, var69);
                  }
               }

               var49 = SolarTex.shade(var49, 0.75 + 0.35 * SolarTex.clamp01(-var50 * 0.2 + 0.5));
               if (!(var47 < 0.01)) {
                  int var74 = (int)(Math.min(1.0, var47) * 255.0) << 24 | var49 & 16777215;
                  if (var37 < 0.0) {
                     var1[var29 * var3 + var30] = var74;
                  } else {
                     var2[var29 * var3 + var30] = var74;
                  }
               }
            }
         }
      }
   }

   void update(float var1, float var2) {
      this.now = var2;
      this.rot = this.rot + this.rotSpeed * var1;
      this.crot = this.crot + this.cloudSpeed * var1;
      if (this.rot > Math.PI * 2) {
         this.rot -= Math.PI * 2;
      }

      if (this.crot > Math.PI * 2) {
         this.crot -= Math.PI * 2;
      }

      this.tempOff = this.tempOff * (float)Math.pow(0.985, var1);
      this.lightsOff = Math.max(0.0F, this.lightsOff - var1 / 25.0F);
      this.shake = Math.max(0.0F, this.shake - var1 * 1.6F);
      this.hitFlash = Math.max(0.0F, this.hitFlash - var1 * 3.0F);
      if (this.destroyed) {
         this.destroyedT += var1;
      }

      this.rebuildT += var1;
      SolarPlanet.Maps var3 = this.hi;
      if (var3 != null) {
         int var4 = 10;
         int var5 = var3.hot.length;

         for (int var6 = 0; var6 < var5 && var4 > 0; var6++) {
            int var7 = var3.hotPtr = (var3.hotPtr + 1) % var5;
            if (var3.hot[var7]) {
               float var8 = var2 - var3.coolT[var7];
               if (!(var8 < 0.12F)) {
                  var4--;
                  var3.coolT[var7] = var2;
                  if (!this.cool(var3, var7, var8)) {
                     var3.hot[var7] = false;
                  }

                  var3.mip(var7);
                  this.ver++;
               }
            }
         }

         if (var3.dirtyCount > 0) {
            int var9 = 40;

            for (int var10 = 0; var10 < var5 && var9 > 0; var10++) {
               if (var3.dirty[var10]) {
                  var3.mip(var10);
                  var9--;
                  var3.dirtyCount--;
               }
            }

            if (var9 > 0) {
               var3.dirtyCount = 0;
            }
         }
      }
   }

   private boolean cool(SolarPlanet.Maps var1, int var2, float var3) {
      int var4 = var2 % var1.tx * 64;
      int var5 = var2 / var1.tx * 64;
      boolean var6 = false;
      float var7 = 16.0F * var3;

      for (int var8 = var5; var8 < var5 + 64 && var8 < var1.H; var8++) {
         for (int var9 = var4; var9 < var4 + 64 && var9 < var1.W; var9++) {
            int var10 = var8 << var1.shift | var9;
            int var11 = var1.heat[var10] & 255;
            if (var11 != 0) {
               int var12 = var1.dmg[var10] & 255;
               int var13 = var12 >= 180 ? Math.min(215, (var12 - 180) * 3) : 0;
               if (var1.heat0 != null) {
                  var13 = Math.max(var13, var1.heat0[var10] & 255);
               }

               if (var11 > var13) {
                  int var14 = Math.max(var13, var11 - (int)(var7 * (1.0F + var11 / 110.0F) + 0.5F));
                  var1.heat[var10] = (byte)var14;
                  if (var14 > var13) {
                     var6 = true;
                  }
               }
            }
         }
      }

      return var6;
   }

   static SolarPlanet.Sphere sphere(int var0, float var1) {
      long var2 = (long)var0 << 32 ^ Float.floatToIntBits(var1);
      SolarPlanet.Sphere var4 = SPHERES.get(var2);
      if (var4 == null) {
         if (SPHERES.size() > 40) {
            SPHERES.entrySet().removeIf(var0x -> var0x.getValue().D > 300);
         }

         var4 = new SolarPlanet.Sphere(var0, var1);
         SPHERES.put(var2, var4);
      }

      return var4;
   }

   boolean refresh(SolarPlanet.Disc var1, boolean var2, int var3, boolean var4) {
      SolarPlanet.Maps var5 = this.maps();
      if (var5 == null) {
         return false;
      } else {
         boolean var6 = var2 && var5.alb1 != var5.alb && var5 == this.hi;
         int var7 = var6 ? var5.W : var5.W1;
         int var8 = (int)(this.rot / (Math.PI * 2) * 65536.0) & 65535;
         int var9 = (int)(this.crot / (Math.PI * 2) * 65536.0) & 65535;
         int var10 = Math.max(1, 65536 / var7);
         boolean var11 = var1.lastBody != this
            || var1.lastMaps != var5
            || var1.lastHi != var6
            || var1.lastVer != this.ver
            || Math.abs((short)(var8 - var1.lastRot)) >= var10
            || Math.abs((short)(var9 - var1.lastCrot)) >= var10 * 2;
         if (var11 || var4) {
            Arrays.fill(var1.need, true);
            var1.lastBody = this;
            var1.lastMaps = var5;
            var1.lastHi = var6;
            var1.lastVer = this.ver;
            var1.lastRot = var8;
            var1.lastCrot = var9;
         }

         int var12 = 0;
         boolean var13 = true;

         for (int var14 = 0; var14 < var1.slabs; var14++) {
            int var15 = (var1.next + var14) % var1.slabs;
            if (var1.need[var15]) {
               int var16 = var15 * var1.slabH;
               int var17 = Math.min(var1.D, var16 + var1.slabH);
               int var18 = var1.sp.rowStart[var17] - var1.sp.rowStart[var16];
               if (!var4 && var12 > 0 && var12 + var18 > var3) {
                  var13 = false;
               } else {
                  this.render(var1, var15, var5, var6, var8, var9);
                  var1.need[var15] = false;
                  Gx.B.update(var1.img(var15), var1.buf[var15]);
                  var1.uploads++;
                  var12 += var18;
                  var1.next = (var15 + 1) % var1.slabs;
               }
            }
         }

         return var13;
      }
   }

   private void shadeSlab(SolarPlanet.Disc var1, int var2, int var3) {
      SolarPlanet.Sphere var4 = var1.sp;
      float var5 = var1.lx;
      float var6 = var1.ly;
      float var7 = var1.lz;
      float var10 = var7 + 1.0F;
      float var11 = (float)Math.sqrt(var5 * var5 + var6 * var6 + var10 * var10);
      float var8 = var5 / var11;
      float var9 = var6 / var11;
      var10 /= var11;
      boolean var12 = var1.star;

      for (int var13 = var4.rowStart[var2]; var13 < var4.rowStart[var3]; var13++) {
         float var14 = var4.nx[var13];
         float var15 = var4.ny[var13];
         float var16 = var4.nz[var13];
         byte var17 = var4.cov[var13];
         int var19 = 0;
         int var20 = 0;
         int var21 = 0;
         int var18;
         if (var12) {
            var18 = (int)((0.42F + 0.62F * Math.pow(var16, 0.45)) * 256.0);
         } else {
            float var22 = var14 * var5 + var15 * var6 + var16 * var7;
            float var23 = Math.max(0.0F, (var22 + 0.05F) / 1.05F);
            var18 = (int)((0.035F + 0.965F * Math.pow(var23, 0.85)) * 256.0);
            var19 = (int)(SolarTex.sstep(0.1, -0.14, var22) * 255.0);
            float var24 = var14 * var8 + var15 * var9 + var16 * var10;
            var20 = var24 > 0.0F ? (int)(Math.pow(var24, 70.0) * 200.0 + Math.pow(var24, 12.0) * 26.0) : 0;
            double var25 = Math.pow(1.0F - var16, 2.2) * (0.12 + 0.88 * SolarTex.sstep(-0.3, 0.55, var22));
            var21 = (int)Math.min(255.0, var25 * 300.0);
         }

         var1.shade[var13] = Math.min(511, var18) | var19 << 9 | Math.min(255, var20) << 17 | var17 << 25;
         var1.rim[var13] = (byte)var21;
      }
   }

   private void render(SolarPlanet.Disc var1, int var2, SolarPlanet.Maps var3, boolean var4, int var5, int var6) {
      SolarPlanet.Sphere var7 = var1.sp;
      int var8 = var1.D;
      int var9 = var2 * var1.slabH;
      int var10 = Math.min(var8, var9 + var1.slabH);
      if (var1.slabL[var2] != var1.lver) {
         this.shadeSlab(var1, var9, var10);
         var1.slabL[var2] = var1.lver;
      }

      int[] var11 = var4 ? var3.alb : var3.alb1;
      byte[] var12 = var4 ? var3.heat : var3.heat1;
      byte[] var13 = var4 ? var3.city : var3.city1;
      byte[] var14 = var3.cloud;
      int var15 = var4 ? var3.W : var3.W1;
      int var16 = var4 ? var3.H : var3.H1;
      int var17 = var4 ? var3.shift : var3.shift1;
      int var18 = var3.cW;
      int var19 = var3.cH;
      char[] var20 = var7.lat;
      char[] var21 = var7.lon;
      float[] var22 = var7.nz;
      int[] var23 = var1.shade;
      byte[] var24 = var1.rim;
      int[] var25 = var1.buf[var2];
      int[] var26 = HEAT;
      float var27 = Math.max(0.0F, Math.min(1.0F, this.atmo));
      int var28 = (int)((this.atmoCol >> 16 & 0xFF) * var27);
      int var29 = (int)((this.atmoCol >> 8 & 0xFF) * var27);
      int var30 = (int)((this.atmoCol & 0xFF) * var27);
      int var31 = SolarTex.mix(this.cloudCol, -11909048, this.dark);
      int var32 = var31 >> 16 & 0xFF;
      int var33 = var31 >> 8 & 0xFF;
      int var34 = var31 & 0xFF;
      int var35 = (int)(256.0F * Math.min(1.0F, var27 * 1.1F));
      int var36 = (int)(256.0F * Math.max(0.0F, 1.0F - this.lightsOff) * Math.sqrt(Math.max(0.0, this.gAlive)) * (this.destroyed ? 0 : 1));
      int var37 = this.cityCol >> 16 & 0xFF;
      int var38 = this.cityCol >> 8 & 0xFF;
      int var39 = this.cityCol & 0xFF;
      boolean var40 = var1.star;
      byte[] var41 = var4 ? var3.dmg : null;
      float var42 = var1.lx;
      float var43 = var1.ly;
      int var44 = var15 - 1;
      Arrays.fill(var25, 0);

      for (int var45 = var9; var45 < var10; var45++) {
         int var46 = var7.x0[var45];
         int var47 = var7.x1[var45];
         if (var47 >= var46) {
            int var48 = var7.rowStart[var45];
            int var49 = (var45 - var9) * var8 + var46;

            for (int var50 = var46; var50 <= var47; var49++) {
               label155: {
                  char var51 = var20[var48];
                  char var52 = var21[var48];
                  int var53 = var51 * var16 >>> 16;
                  int var54 = (var52 + var5 & 65535) * var15 >>> 16;
                  int var55 = var53 << var17 | var54;
                  int var56 = var11[var55];
                  int var57 = var56 >>> 24;
                  int var58 = var23[var48];
                  int var59 = var58 >>> 25 << 1;
                  if (var57 == 1 && var22[var48] < 0.42F) {
                     var59 = var59 * (int)Math.max(0.0F, (var22[var48] - 0.3F) * 2000.0F) >> 8;
                     if (var59 <= 0) {
                        break label155;
                     }

                     if (var59 > 254) {
                        var59 = 254;
                     }
                  }

                  int var60 = var58 & 511;
                  if (var41 != null && !var40) {
                     int var61 = var41[var55] & 255;
                     int var62 = var55 >> var17;
                     int var63 = var55 & var44;
                     int var64 = var41[var62 << var17 | var63 - 4 & var44] & 255;
                     int var65 = var41[var62 << var17 | var63 + 4 & var44] & 255;
                     int var66 = var62 > 3 ? var41[var55 - 4 * var15] & 255 : var61;
                     int var67 = var62 < var16 - 4 ? var41[var55 + 4 * var15] & 255 : var61;
                     if ((var61 | var64 | var65 | var66 | var67) != 0) {
                        float var68 = ((var65 - var64) * var42 + (var67 - var66) * var43) * 6.0F;
                        var60 = (int)(var60 * (1.0F - var61 / 330.0F) + var68 * Math.max(0.25F, var60 / 300.0F));
                        if (var60 < 0) {
                           var60 = 0;
                        }

                        if (var60 > 480) {
                           var60 = 480;
                        }
                     }
                  }

                  int var72 = (var56 >> 16 & 0xFF) * var60 >> 8;
                  int var73 = (var56 >> 8 & 0xFF) * var60 >> 8;
                  int var74 = (var56 & 0xFF) * var60 >> 8;
                  int var75 = var12[var55] & 255;
                  if (var40) {
                     if (var75 > 0) {
                        int var76 = var26[var75];
                        var72 += var76 >> 16 & 0xFF;
                        var73 += var76 >> 8 & 0xFF;
                        var74 += var76 & 0xFF;
                     }
                  } else {
                     int var77 = var58 >>> 17 & 0xFF;
                     if (var57 > 1 && var77 > 0) {
                        int var78 = var77 * var57 >> 8;
                        var72 += var78;
                        var73 += var78;
                        var74 += var78 + (var78 >> 2);
                     }

                     int var79 = 0;
                     if (var14 != null) {
                        int var80 = var51 * var19 >>> 16;
                        int var82 = (var52 + var6 & 65535) * var18 >>> 16;
                        var79 = (var14[var80 * var18 + var82] & 255) * var35 >> 8;
                        if (var79 > 0) {
                           int var69 = var32 * var60 >> 8;
                           int var70 = var33 * var60 >> 8;
                           int var71 = var34 * var60 >> 8;
                           var72 += (var69 - var72) * var79 >> 8;
                           var73 += (var70 - var73) * var79 >> 8;
                           var74 += (var71 - var74) * var79 >> 8;
                        }
                     }

                     int var81 = var58 >>> 9 & 0xFF;
                     if (var81 > 0) {
                        int var83 = var13[var55] & 255;
                        if (var83 > 0) {
                           int var86 = (var83 * var81 >> 8) * var36 >> 8;
                           var86 = var86 * (300 - var79) >> 8;
                           var72 += var37 * var86 >> 8;
                           var73 += var38 * var86 >> 8;
                           var74 += var39 * var86 >> 8;
                        }
                     }

                     if (var75 > 0) {
                        int var84 = var26[var75];
                        int var88 = 256 - (var79 >> 1);
                        var72 += (var84 >> 16 & 0xFF) * var88 >> 8;
                        var73 += (var84 >> 8 & 0xFF) * var88 >> 8;
                        var74 += (var84 & 0xFF) * var88 >> 8;
                     }

                     int var85 = var24[var48] & 255;
                     if (var85 > 0) {
                        var72 += var28 * var85 >> 8;
                        var73 += var29 * var85 >> 8;
                        var74 += var30 * var85 >> 8;
                     }
                  }

                  if (var72 > 255) {
                     var72 = 255;
                  }

                  if (var73 > 255) {
                     var73 = 255;
                  }

                  if (var74 > 255) {
                     var74 = 255;
                  }

                  var25[var49] = var59 << 24 | var72 << 16 | var73 << 8 | var74;
               }

               var50++;
               var48++;
            }
         }
      }
   }

   static void drawDisc(SolarPlanet.Disc var0, float var1, float var2, float var3, int var4) {
      if (var0.sp != null) {
         float var5 = 2.0F * var3 / var0.D;
         int var6 = Math.round(var1 - var3);
         int var7 = Math.round(var1 + var3) - var6;
         float var8 = var2 - var3;

         for (int var9 = 0; var9 < var0.slabs; var9++) {
            int var10 = Math.round(var8 + var9 * var0.slabH * var5);
            int var11 = Math.round(var8 + (var9 + 1) * var0.slabH * var5);
            if (var11 > var10) {
               Gx.image(var0.img(var9), var6, var10, var7, var11 - var10, var4);
            }
         }
      }
   }

   static int[] snapshot(SolarPlanet.Disc var0) {
      int[] var1 = new int[var0.D * var0.D];

      for (int var2 = 0; var2 < var0.slabs; var2++) {
         int var3 = var2 * var0.slabH;
         int var4 = Math.min(var0.slabH, var0.D - var3);
         System.arraycopy(var0.buf[var2], 0, var1, var3 * var0.D, var4 * var0.D);
      }

      return var1;
   }

   int avgColor() {
      SolarPlanet.Maps var1 = this.maps();
      if (var1 == null) {
         return -8355712;
      } else {
         long var2 = 0L;
         long var4 = 0L;
         long var6 = 0L;
         int var8 = 0;

         for (byte var9 = 0; var9 < var1.alb1.length; var9 += 97) {
            int var10 = var1.alb1[var9];
            var2 += var10 >> 16 & 0xFF;
            var4 += var10 >> 8 & 0xFF;
            var6 += var10 & 0xFF;
            var8++;
         }

         return 0xFF000000 | (int)(var2 / var8) << 16 | (int)(var4 / var8) << 8 | (int)(var6 / var8);
      }
   }

   static {
      for (int var0 = 0; var0 < 256; var0++) {
         double var1 = var0 / 255.0;
         int var3 = SolarTex.ramp(var1, new double[]{0.0, 0.15, 0.4, 0.7, 1.0}, new int[]{-16777216, -10876416, -3000310, -26070, -2872});
         HEAT[var0] = var3 & 16777215;
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

      Disc(String var1) {
         this.key = var1;
      }

      void setup(int var1, float var2, boolean var3) {
         if (this.sp == null || this.D != var1 || this.sp.tilt != var2 || this.star != var3) {
            this.D = var1;
            this.star = var3;
            this.sp = SolarPlanet.sphere(var1, var2);
            this.slabs = var1 <= 200 ? 1 : (var1 <= 420 ? 2 : (var1 <= 760 ? 4 : 8));
            this.slabH = (var1 + this.slabs - 1) / this.slabs;
            this.buf = new int[this.slabs][var1 * this.slabH];
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

      void light(float var1, float var2, float var3) {
         if (!(Math.abs(var1 - this.lx) + Math.abs(var2 - this.ly) + Math.abs(var3 - this.lz) < 0.004F)) {
            this.lx = var1;
            this.ly = var2;
            this.lz = var3;
            this.lver++;
            Arrays.fill(this.need, true);
         }
      }

      Gx.Img img(int var1) {
         int[] var2 = this.buf[var1];
         int var3 = this.D;
         int var4 = this.slabH;
         return Gx.get("solar:" + this.key + ":" + this.D + ":" + var1 + ":" + this.slabs, 0, () -> new int[][]{var2, {var3, var4}});
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

      Maps(int var1, int var2, boolean var3, boolean var4) {
         this.W = var1;
         this.H = var2;
         this.shift = Integer.numberOfTrailingZeros(var1);
         this.alb = new int[var1 * var2];
         this.hgt = new byte[var1 * var2];
         this.heat = new byte[var1 * var2];
         this.dmg = new byte[var1 * var2];
         this.city = new byte[var1 * var2];
         this.heat0 = var4 ? new byte[var1 * var2] : null;
         this.cW = var1 >= 1024 ? var1 / 2 : var1;
         this.cH = var2 * this.cW / var1;
         this.cloud = var3 ? new byte[this.cW * this.cH] : null;
         this.cosRow = new float[var2];

         for (int var5 = 0; var5 < var2; var5++) {
            this.cosRow[var5] = (float)Math.max(0.004, Math.cos((0.5 - (var5 + 0.5) / var2) * Math.PI));
         }

         this.tx = Math.max(1, var1 / 64);
         this.ty = Math.max(1, var2 / 64);
         this.hot = new boolean[this.tx * this.ty];
         this.dirty = new boolean[this.tx * this.ty];
         this.coolT = new float[this.tx * this.ty];
      }

      void finish() {
         double var1 = 0.0;
         double var3 = 0.0;

         for (int var5 = 0; var5 < this.H; var5++) {
            double var6 = this.cosRow[var5];
            double var8 = 0.0;

            for (int var10 = 0; var10 < this.W; var10++) {
               var8 += this.city[var5 * this.W + var10] & 255;
            }

            var1 += var8 * var6;
            var3 += var6 * this.W * 255.0;
         }

         this.popW = Math.max(1.0, var1);
         this.areaW = var3;
         if (this.W > 1024) {
            this.W1 = this.W / 2;
            this.H1 = this.H / 2;
            this.shift1 = this.shift - 1;
            this.alb1 = new int[this.W1 * this.H1];
            this.heat1 = new byte[this.W1 * this.H1];
            this.city1 = new byte[this.W1 * this.H1];

            for (int var11 = 0; var11 < this.tx * this.ty; var11++) {
               this.mip(var11);
            }
         } else {
            this.W1 = this.W;
            this.H1 = this.H;
            this.shift1 = this.shift;
            this.alb1 = this.alb;
            this.heat1 = this.heat;
            this.city1 = this.city;
         }

         for (int var12 = 0; var12 < this.hot.length; var12++) {
            if (this.heat0 != null) {
               this.hot[var12] = false;
            }
         }
      }

      void mip(int var1) {
         this.dirty[var1] = false;
         if (this.alb1 != this.alb) {
            int var2 = var1 % this.tx * 64;
            int var3 = var1 / this.tx * 64;

            for (int var4 = var3; var4 < var3 + 64 && var4 < this.H; var4 += 2) {
               for (int var5 = var2; var5 < var2 + 64 && var5 < this.W; var5 += 2) {
                  int var6 = var4 * this.W + var5;
                  int var7 = var6 + this.W;
                  int var8 = this.alb[var6];
                  int var9 = this.alb[var6 + 1];
                  int var10 = this.alb[var7];
                  int var11 = this.alb[var7 + 1];
                  int var12 = var8 >>> 24;
                  int var13 = var9 >>> 24;
                  int var14 = var10 >>> 24;
                  int var15 = var11 >>> 24;
                  int var16 = var12 != 1 && var13 != 1 && var14 != 1 && var15 != 1 ? var12 + var13 + var14 + var15 >> 2 : 1;
                  if (var16 == 1 && (var12 != 1 || var13 != 1 || var14 != 1 || var15 != 1) && var12 + var13 + var14 + var15 < 8) {
                     var16 = 1;
                  }

                  int var17 = (var8 >> 16 & 0xFF) + (var9 >> 16 & 0xFF) + (var10 >> 16 & 0xFF) + (var11 >> 16 & 0xFF) >> 2;
                  int var18 = (var8 >> 8 & 0xFF) + (var9 >> 8 & 0xFF) + (var10 >> 8 & 0xFF) + (var11 >> 8 & 0xFF) >> 2;
                  int var19 = (var8 & 0xFF) + (var9 & 0xFF) + (var10 & 0xFF) + (var11 & 0xFF) >> 2;
                  int var20 = (var4 >> 1) * this.W1 + (var5 >> 1);
                  this.alb1[var20] = var16 << 24 | var17 << 16 | var18 << 8 | var19;
                  int var21 = Math.max(Math.max(this.heat[var6] & 255, this.heat[var6 + 1] & 255), Math.max(this.heat[var7] & 255, this.heat[var7 + 1] & 255));
                  int var22 = (this.heat[var6] & 255) + (this.heat[var6 + 1] & 255) + (this.heat[var7] & 255) + (this.heat[var7 + 1] & 255) >> 2;
                  this.heat1[var20] = (byte)(var21 + var22 >> 1);
                  this.city1[var20] = (byte)((this.city[var6] & 255) + (this.city[var6 + 1] & 255) + (this.city[var7] & 255) + (this.city[var7 + 1] & 255) >> 2);
               }
            }
         }
      }

      void markRect(int var1, int var2, int var3, int var4, boolean var5, float var6) {
         int var7 = Math.max(0, var2 / 64);
         int var8 = Math.min(this.ty - 1, var4 / 64);
         if (var3 - var1 >= this.W - 1) {
            var1 = 0;
            var3 = this.W - 1;
         }

         for (int var9 = var7; var9 <= var8; var9++) {
            for (int var10 = var1 / 64 - (var1 < 0 ? 1 : 0); var10 <= Math.floorDiv(var3, 64); var10++) {
               int var11 = var9 * this.tx + Math.floorMod(var10, this.tx);
               if (!this.dirty[var11]) {
                  this.dirty[var11] = true;
                  this.dirtyCount++;
               }

               if (var5 && !this.hot[var11]) {
                  this.hot[var11] = true;
                  this.coolT[var11] = var6;
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

      Sphere(int var1, float var2) {
         this.D = var1;
         this.tilt = var2;
         this.x0 = new int[var1];
         this.x1 = new int[var1];
         this.rowStart = new int[var1 + 1];
         double var3 = var1 / 2.0;
         int var5 = 0;

         for (int var6 = 0; var6 < var1; var6++) {
            double var7 = var6 + 0.5 - var3;
            double var9 = Math.sqrt(Math.max(0.0, (var3 + 0.5) * (var3 + 0.5) - var7 * var7));
            int var11 = (int)Math.floor(var3 - var9);
            int var12 = (int)Math.ceil(var3 + var9) - 1;
            var11 = Math.max(0, var11);
            var12 = Math.min(var1 - 1, var12);
            this.x0[var6] = var11;
            this.x1[var6] = var12;
            this.rowStart[var6] = var5;
            if (var12 >= var11) {
               var5 += var12 - var11 + 1;
            }
         }

         this.rowStart[var1] = var5;
         this.n = var5;
         this.lat = new char[this.n];
         this.lon = new char[this.n];
         this.nx = new float[this.n];
         this.ny = new float[this.n];
         this.nz = new float[this.n];
         this.cov = new byte[this.n];
         double var33 = Math.cos(var2);
         double var8 = Math.sin(var2);
         int var10 = 0;

         for (int var35 = 0; var35 < var1; var35++) {
            for (int var37 = this.x0[var35]; var37 <= this.x1[var35]; var10++) {
               double var13 = (var37 + 0.5 - var3) / var3;
               double var15 = (var35 + 0.5 - var3) / var3;
               double var17 = Math.sqrt(var13 * var13 + var15 * var15) * var3;
               double var19 = Math.max(0.0, Math.min(1.0, var3 - var17 + 0.5));
               this.cov[var10] = (byte)(var19 * 127.0);
               double var21 = var13 * var13 + var15 * var15;
               if (var21 > 0.9999) {
                  double var23 = 0.99995 / Math.sqrt(var21);
                  var13 *= var23;
                  var15 *= var23;
                  var21 = 0.9999;
               }

               double var38 = Math.sqrt(1.0 - var21);
               this.nx[var10] = (float)var13;
               this.ny[var10] = (float)var15;
               this.nz[var10] = (float)var38;
               double var25 = var13 * var33 + var15 * var8;
               double var27 = -var13 * var8 + var15 * var33;
               double var29 = Math.asin(Math.max(-1.0, Math.min(1.0, -var27)));
               double var31 = Math.atan2(var25, var38);
               this.lat[var10] = (char)Math.max(0, Math.min(65535, (int)((0.5 - var29 / Math.PI) * 65536.0)));
               this.lon[var10] = (char)((int)Math.round(var31 / (Math.PI * 2) * 65536.0) & 65535);
               var37++;
            }
         }
      }
   }

   interface TexelFn {
      void apply(SolarPlanet.Maps var1, int var2, int var3, int var4);
   }
}
