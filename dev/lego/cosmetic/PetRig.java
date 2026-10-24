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

   static float clamp01(float var0) {
      return var0 < 0.0F ? 0.0F : (var0 > 1.0F ? 1.0F : var0);
   }

   static float lerp(float var0, float var1, float var2) {
      return var0 + (var1 - var0) * var2;
   }

   static float frac(float var0) {
      return var0 - (float)Math.floor(var0);
   }

   static float smooth(float var0, float var1, float var2) {
      float var3 = clamp01((var2 - var0) / (var1 - var0));
      return var3 * var3 * (3.0F - 2.0F * var3);
   }

   static float window(float var0, float var1, float var2, float var3) {
      return smooth(var1, var1 + var3, var0) * (1.0F - smooth(var2 - var3, var2, var0));
   }

   static void gait(Pets.P var0) {
      float var1 = var0.t;
      float var2 = var0.sp;
      var0.walkW = clamp01(var2 / 0.5F);
      var0.runW = smooth(0.62F, 0.92F, var2);
      var0.idle = 1.0F - smooth(0.03F, 0.2F, var2);
      var0.cw = frac(var1 * 1.55F);
      var0.cr = frac(var1 * 2.35F);
      float var3 = var0.walkW * (1.0F - var0.runW);
      double var4 = (Math.PI * 2) * var0.cw;
      double var6 = (Math.PI * 2) * var0.cr;
      var0.bob = var3 * 0.045F * PetGeo.sin(var4 * 2.0 + 0.6) + var0.runW * (0.13F * PetGeo.sin(var6 - 0.9) - 0.02F);
      var0.roll = var3 * 2.6F * PetGeo.sin(var4) + var0.runW * 1.2F * PetGeo.sin(var6);
      var0.pitch = var0.runW * 8.5F * PetGeo.sin(var6 + 1.25);
      var0.stretch = 1.0F + var0.runW * 0.075F * PetGeo.sin(var6 - 0.35);
      var0.nod = var3 * 5.5F * PetGeo.sin(var4 * 2.0 + 1.7) - var0.pitch * 0.75F;
      var0.tailSway = var3 * 11.0F * PetGeo.sin(var4 + 0.7) + var0.runW * 6.0F * PetGeo.sin(var6);
      var0.tailLift = var0.tailLift + var0.runW * 16.0F;
      double var8 = (Math.PI * 2) * frac(var1 * 2.1F);
      var0.waddle = var0.walkW * 9.0F * PetGeo.sin(var8) + var0.idle * 1.2F * PetGeo.sin(var1 * 1.3F);
      var0.waddleBob = var0.walkW * 0.35F * Math.abs(PetGeo.sin(var8));
      float var10 = var0.idle;
      int var11 = (int)Math.floor(var1 / 11.3F);
      float var12 = window(frac(var1 / 11.3F), 0.12F, 0.5F, 0.1F) * var10;
      var0.headYaw += var12 * 34.0F * ((var11 & 1) == 0 ? 1 : -1);
      var0.headRoll += var12 * 5.0F * ((var11 & 1) == 0 ? 1 : -1);
      var0.sniff = window(frac(var1 / 17.1F + 0.3F), 0.55F, 0.78F, 0.06F) * var10;
      var0.headPitch = var0.headPitch + var0.sniff * (20.0F + PetGeo.sin(var1 * 15.0F) * 3.0F);
      boolean var13 = var0.is("happy") || var0.is("love") || var0.is("curious") || var0.is("sad");
      var0.sit = var13 ? window(frac(var1 / 37.0F), 0.32F, 0.86F, 0.05F) * var10 : 0.0F;
      var0.sniff = var0.sniff * (1.0F - var0.sit);
      if (var0.is("excited")) {
         var0.bow = window(frac(var1 / 4.3F), 0.08F, 0.46F, 0.1F) * var10;
         var0.y = var0.y * (1.0F - var0.bow);
      }

      if (var0.is("angry")) {
         var0.crouch = 1.0F;
      }

      var0.lie = var0.lie * (1.0F - var0.sit);
   }

   static PetRig.Frame frame(Pets.P var0, float var1, float var2, float var3, float var4, float var5) {
      PetRig.Frame var6 = new PetRig.Frame();
      var6.pivY = var1;
      var6.pivZ = var3;
      float var7 = Math.max(0.5F, var2 - var3);
      float var8 = Math.max(0.0F, var1 - Math.max(var4 * 0.4F, var5 * 0.4F));
      float var9 = (float)Math.toDegrees(Math.asin(Math.min(0.75F, var8 / var7)));
      float var10 = Math.max(0.0F, var1 - var4 * 0.45F) * 0.8F;
      float var11 = (float)Math.toDegrees(Math.asin(Math.min(0.6F, var10 / var7)));
      float var12 = Math.max(0.0F, var5 - 0.08F);
      var6.set(var0.pitch * (1.0F - var0.sit) + var9 * var0.sit - var11 * var0.bow);
      var6.dy = var0.bob * var4 - var8 * var0.sit - var12 * var0.lie - var0.crouch * var4 * 0.1F;
      return var6;
   }

   static void foot(Pets.P var0, int var1, float[] var2) {
      float var3 = (1.0F - var0.sit) * (1.0F - var0.lie) * (1.0F - var0.bow);
      float var4 = var0.walkW * (1.0F - var0.runW) * var3;
      float var5 = var0.runW * var3;
      float var6 = 0.0F;
      float var7 = 0.0F;
      float var8 = 0.0F;
      if (var4 > 0.001F) {
         float[] var9 = step(frac(var0.cw + WALK_OFF[var1]), 0.6F, 0.62F, 0.26F);
         var6 += var9[0] * var4;
         var7 += var9[1] * var4;
         var8 += var9[2] * var4;
      }

      if (var5 > 0.001F) {
         float[] var10 = step(frac(var0.cr + RUN_OFF[var1]), 0.38F, 1.2F, 0.42F);
         var6 += var10[0] * var5;
         var7 += var10[1] * var5;
         var8 += var10[2] * var5;
      }

      var2[0] = var6;
      var2[1] = var7;
      var2[2] = var8;
   }

   private static float[] step(float var0, float var1, float var2, float var3) {
      if (var0 < var1) {
         float var6 = var0 / var1;
         return new float[]{var2 * (0.5F - var6), 0.0F, 0.0F};
      } else {
         float var4 = (var0 - var1) / (1.0F - var1);
         float var5 = var4 * var4 * (3.0F - 2.0F * var4);
         return new float[]{var2 * (-0.5F + var5), var3 * PetGeo.sin(Math.PI * var4), PetGeo.sin(Math.PI * var4)};
      }
   }

   static void ikLeg(G var0, PetRig.LegStyle var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, int var9, float var10) {
      float var11 = var5 - var4;
      float var12 = var6 - var3;
      float var13 = (float)Math.sqrt(var11 * var11 + var12 * var12);
      float var14 = Math.abs(var7 - var8) + 0.08F * (var7 + var8);
      float var15 = (var7 + var8) * 0.995F;
      var13 = Math.max(var14, Math.min(var15, var13));
      double var16 = Math.atan2(var11, -var12);
      double var18 = (var7 * var7 + var13 * var13 - var8 * var8) / (2.0F * var7 * var13);
      double var20 = Math.acos(Math.max(-1.0, Math.min(1.0, var18)));
      double var22 = var16 + var9 * var20;
      float var24 = (float)Math.sin(var22) * var7;
      float var25 = -((float)Math.cos(var22)) * var7;
      float var26 = (float)Math.sin(var16) * var13;
      float var27 = -((float)Math.cos(var16)) * var13;
      double var28 = Math.atan2(var26 - var24, -(var27 - var25));
      float var30 = (float)Math.toDegrees(var22);
      float var31 = (float)Math.toDegrees(var28);
      var0.push();
      var0.translate(var2, var3, var4);
      var0.rotX(-var30);
      int var32 = Math.max(var1.seg, 10);
      PetGeo.blob(var0, var1.up, 0.0F, -var7 * 0.45F, 0.0F, var1.r1, var7 * 0.55F + var1.r1 * 0.25F, var1.r1 * 1.05F, var32, 5);
      var0.translate(0.0F, -var7, 0.0F);
      var0.rotX(-(var31 - var30));
      PetGeo.blob(var0, var1.lo, 0.0F, -var8 * 0.5F, 0.0F, var1.r2, var8 * 0.55F + var1.r2 * 0.2F, var1.r2, var32 - 1, 5);
      var0.translate(0.0F, -var8, 0.0F);
      var0.rotX(var31 + var10);
      if (var1.hoof) {
         var0.cylinder(var1.paw, var1.pawR * 0.95F, var1.pawR * 1.08F, -var1.pawR * 0.9F, var1.pawR * 0.35F, 10, true, 0.0F, 0.0F, 1.0F, 1.0F);
      } else {
         PetGeo.blob2(
            var0,
            var1.paw,
            0.0F,
            -var1.pawR * 0.15F,
            var1.pawR * 0.35F,
            var1.pawR * 1.05F,
            var1.pawR * 0.85F,
            var1.pawR * 0.45F,
            var1.pawR * var1.pawLen,
            var32 - 1,
            5
         );
      }

      var0.pop();
   }

   static void legs(
      G var0,
      Pets.P var1,
      PetRig.Frame var2,
      PetRig.LegStyle var3,
      PetRig.LegStyle var4,
      float var5,
      float var6,
      float var7,
      float var8,
      float var9,
      float var10,
      float var11
   ) {
      float[] var12 = new float[3];

      for (int var13 = 0; var13 < 4; var13++) {
         boolean var14 = var13 < 2;
         int var15 = (var13 & 1) == 0 ? 1 : -1;
         PetRig.LegStyle var16 = var14 ? var3 : var4;
         float var17 = var14 ? var6 : var7;
         float var18 = var2.y(var8, var17);
         float var19 = var2.z(var8, var17);
         foot(var1, var13, var12);
         float var20 = var17 + var12[0] * var9;
         float var21 = var16.pawR * 0.85F + var12[1] * var9;
         if (var14) {
            var20 += var1.lie * var9 * 0.75F + var1.bow * var9 * 0.55F;
         } else {
            var20 += var1.sit * var9 * 0.45F + var1.lie * var9 * 0.5F;
         }

         float var22 = var14 ? var10 : var11;
         float var23 = var14 ? var12[2] * 45.0F : var12[2] * 25.0F;
         ikLeg(var0, var16, var15 * var5, var18, var19, var20, var21, var9 * var22, var9 * (1.0F - var22), var14 ? 1 : -1, var23);
      }
   }

   static void feet(G var0, Pets.P var1, String var2, float var3, float var4, float var5, float var6) {
      for (byte var7 = -1; var7 <= 1; var7 += 2) {
         float var8 = frac(var1.t * 2.1F + (var7 > 0 ? 0.0F : 0.5F));
         float[] var9 = step(var8, 0.55F, var6, 0.55F);
         float var10 = var1.walkW;
         var0.push();
         var0.translate(var7 * var3, 0.3F + var9[1] * var10 * var5 * 1.4F, var4 + var9[0] * var10 * var5 * 2.2F);
         var0.rotY(var7 * 12);
         var0.rotX(-var9[2] * var10 * 25.0F);
         PetGeo.blob2(var0, var2, 0.0F, 0.0F, 0.35F, var5, 0.34F, 0.16F, var5 * 1.4F, 10, 5);
         var0.pop();
      }
   }

   static float[] hop(Pets.P var0) {
      float var1 = smooth(0.02F, 0.35F, var0.sp);
      float var2 = frac(var0.t * 1.45F);
      float var3 = 0.0F;
      float var5 = 0.0F;
      float var4;
      if (var2 < 0.22F) {
         var4 = -PetGeo.sin(Math.PI * var2 / 0.22F) * 0.9F;
      } else if (var2 < 0.8F) {
         float var6 = (var2 - 0.22F) / 0.58F;
         var3 = PetGeo.sin(Math.PI * var6);
         var4 = (1.0F - var6 * 2.0F) * 0.8F;
         var5 = PetGeo.sin(Math.PI * var6);
      } else {
         var4 = -PetGeo.sin(Math.PI * (var2 - 0.8F) / 0.2F);
      }

      return new float[]{var3 * var1, var4 * var1, var5 * var1};
   }

   static float[] flap(Pets.P var0, float var1) {
      double var2 = var0.t * Math.PI * 2.0 * var1;
      float var4 = PetGeo.sin(var2);
      return new float[]{var4, -var4 * 0.5F + 0.25F * PetGeo.sin(var2 * 2.0)};
   }

   static void collar(G var0, Pets.P var1, float var2, float var3) {
      if (var1.lvl >= 4) {
         var0.push();
         var0.torus(Pets.col(14168650), var2, var3, 12, 3, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.translate(0.0F, -var3 * 0.6F, var2 + var3 * 0.4F);
         PetGeo.blob(var0, Pets.gl(var1.lvl >= 5 ? 16762938 : 13159640), 0.0F, -var3 * 1.4F, 0.0F, var3 * 1.5F, var3 * 1.7F, var3 * 0.6F, 6, 3);
         var0.pop();
      }
   }

   static void sparkles(G var0, Pets.P var1, float var2) {
      if (var1.lvl >= 5) {
         byte var3 = 7;

         for (int var4 = 0; var4 < var3; var4++) {
            double var5 = var1.t * 0.6 + var4 * Math.PI * 2.0 / var3;
            float var7 = 0.5F + 0.5F * PetGeo.sin(var1.t * 3.1F + var4 * 1.9F);
            int var8 = (int)(70.0F + 150.0F * var7);
            PetGeo.sprite(
               var0,
               "pet_star",
               PetGeo.cos(var5) * var2,
               0.35F + 0.25F * PetGeo.sin(var1.t * 1.7F + var4),
               PetGeo.sin(var5) * var2,
               0.55F + 0.45F * var7,
               var8 << 24 | 16766554
            );
         }
      }
   }

   static String style(String var0, float var1) {
      int var2 = var1 < 0.34F ? 0 : (var1 < 0.7F ? 1 : 2);
      return var2 == 0 ? var0 : var0 + "." + var2;
   }

   static final class Frame {
      float pu;
      float dy;
      float pivY;
      float pivZ;
      float c = 1.0F;
      float s;

      void set(float var1) {
         this.pu = var1;
         this.c = PetGeo.cos(Math.toRadians(this.pu));
         this.s = PetGeo.sin(Math.toRadians(this.pu));
      }

      float y(float var1, float var2) {
         return this.c * (var1 - this.pivY) + this.s * (var2 - this.pivZ) + this.pivY + this.dy;
      }

      float z(float var1, float var2) {
         return -this.s * (var1 - this.pivY) + this.c * (var2 - this.pivZ) + this.pivZ;
      }

      void apply(G var1) {
         var1.translate(0.0F, this.dy + this.pivY, this.pivZ);
         var1.rotX(-this.pu);
         var1.translate(0.0F, -this.pivY, -this.pivZ);
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

      LegStyle(String var1, String var2, String var3, float var4, float var5, float var6) {
         this.up = var1;
         this.lo = var2;
         this.paw = var3;
         this.r1 = var4;
         this.r2 = var5;
         this.pawR = var6;
      }
   }
}
