package dev.lego.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import dev.lego.LegoClient;
import dev.lego.core.Mc;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public final class R3 {
   private static MultiBufferSource consumers;
   private static Pose entry;
   private static double cx;
   private static double cy;
   private static double cz;
   private static float camYaw;
   private static float camPitch;
   private static VertexConsumer quads;
   private static RenderType quadLayer;
   private static final Set<RenderType> used = new LinkedHashSet<>();
   private static final Map<RenderType, R3.Batch> BATCH = new LinkedHashMap<>();
   private static final ArrayDeque<R3.Batch> POOL = new ArrayDeque<>();
   private static RenderType lastLayer;
   private static R3.Batch lastBatch;
   public static int lastVertices;
   public static int lastLayers;
   public static double skyRadius = 0.0;
   public static final int CUTOUT = 0;
   public static final int TRANSLUCENT = 1;
   public static final int GLOW = 2;
   private static final Map<Identifier, RenderType> SKY = new HashMap<>();
   private static Method layerOf;
   private static boolean skyBroken = false;

   private R3() {
   }

   private static R3.Batch batch(RenderType var0) {
      if (var0 == lastLayer) {
         return lastBatch;
      } else {
         R3.Batch var1 = BATCH.get(var0);
         if (var1 == null) {
            var1 = POOL.isEmpty() ? new R3.Batch() : POOL.pop();
            var1.n = 0;
            BATCH.put(var0, var1);
         }

         lastLayer = var0;
         lastBatch = var1;
         return var1;
      }
   }

   private static void emit() {
      int var0 = 0;

      for (Entry var2 : BATCH.entrySet()) {
         R3.Batch var3 = (R3.Batch)var2.getValue();
         if (var3.n != 0) {
            VertexConsumer var4 = consumers.getBuffer((RenderType)var2.getKey());
            float[] var5 = var3.d;

            for (byte var6 = 0; var6 < var3.n; var6 += 10) {
               var4.addVertex(entry, var5[var6], var5[var6 + 1], var5[var6 + 2])
                  .setColor(Float.floatToRawIntBits(var5[var6 + 3]))
                  .setUv(var5[var6 + 4], var5[var6 + 5])
                  .setOverlay(OverlayTexture.NO_OVERLAY)
                  .setLight(Float.floatToRawIntBits(var5[var6 + 6]))
                  .setNormal(entry, var5[var6 + 7], var5[var6 + 8], var5[var6 + 9]);
            }

            var0 += var3.n / 10;
            if (consumers instanceof BufferSource) {
               ((BufferSource)consumers).endBatch((RenderType)var2.getKey());
            }
         }
      }

      lastVertices = var0;
      lastLayers = BATCH.size();

      for (R3.Batch var8 : BATCH.values()) {
         var8.n = 0;
         if (POOL.size() < 64) {
            POOL.push(var8);
         }
      }

      BATCH.clear();
      lastLayer = null;
      lastBatch = null;
   }

   public static boolean begin(WorldRenderContext var0) {
      consumers = var0.consumers();
      PoseStack var1 = var0.matrices();
      if (consumers != null && var1 != null) {
         entry = var1.last();
         Vec3 var2 = var0.worldState().cameraRenderState.pos;
         cx = var2.x;
         cy = var2.y;
         cz = var2.z;

         try {
            Camera var3 = Mc.mc().gameRenderer.getMainCamera();
            camYaw = var3.yRot();
            camPitch = var3.xRot();
         } catch (Throwable var4) {
            camYaw = 0.0F;
            camPitch = 0.0F;
         }

         quads = null;
         used.clear();
         BATCH.clear();
         lastLayer = null;
         lastBatch = null;
         return true;
      } else {
         return false;
      }
   }

   public static void beginSky(MultiBufferSource var0) {
      consumers = var0;
      entry = new PoseStack().last();
      cz = 0.0;
      cy = 0.0;
      cx = 0.0;
      skyRadius = 90.0;

      try {
         Camera var1 = Mc.mc().gameRenderer.getMainCamera();
         camYaw = var1.yRot();
         camPitch = var1.xRot();
      } catch (Throwable var2) {
         camYaw = 0.0F;
         camPitch = 0.0F;
      }

      quads = null;
      used.clear();
      BATCH.clear();
      lastLayer = null;
      lastBatch = null;
   }

   public static double camX() {
      return cx;
   }

   public static double camY() {
      return cy;
   }

   public static double camZ() {
      return cz;
   }

   public static float camYaw() {
      return camYaw;
   }

   public static float camPitch() {
      return camPitch;
   }

   private static R3.Batch q() {
      if (quadLayer == null) {
         quadLayer = RenderTypes.debugQuads();
      }

      return batch(quadLayer);
   }

   private static void v(R3.Batch var0, double var1, double var3, double var5, int var7) {
      var0.v(var1, var3, var5, var7, 0.0F, 0.0F, 15728880, 0.0F, 1.0F, 0.0F);
   }

   public static void quad(
      double var0,
      double var2,
      double var4,
      double var6,
      double var8,
      double var10,
      double var12,
      double var14,
      double var16,
      double var18,
      double var20,
      double var22,
      int var24,
      int var25,
      int var26,
      int var27
   ) {
      R3.Batch var28 = q();
      v(var28, var0, var2, var4, var24);
      v(var28, var6, var8, var10, var25);
      v(var28, var12, var14, var16, var26);
      v(var28, var18, var20, var22, var27);
   }

   public static void quad(
      double var0,
      double var2,
      double var4,
      double var6,
      double var8,
      double var10,
      double var12,
      double var14,
      double var16,
      double var18,
      double var20,
      double var22,
      int var24
   ) {
      quad(var0, var2, var4, var6, var8, var10, var12, var14, var16, var18, var20, var22, var24, var24, var24, var24);
   }

   public static void ring(double var0, double var2, double var4, double var6, double var8, int var10, int var11, int var12, double var13) {
      for (int var15 = 0; var15 < var10; var15++) {
         double var16 = var13 + (Math.PI * 2) * var15 / var10;
         double var18 = var13 + (Math.PI * 2) * (var15 + 1) / var10;
         double var20 = Math.cos(var16);
         double var22 = Math.sin(var16);
         double var24 = Math.cos(var18);
         double var26 = Math.sin(var18);
         quad(
            var0 + var20 * var6,
            var2,
            var4 + var22 * var6,
            var0 + var20 * var8,
            var2,
            var4 + var22 * var8,
            var0 + var24 * var8,
            var2,
            var4 + var26 * var8,
            var0 + var24 * var6,
            var2,
            var4 + var26 * var6,
            var11,
            var12,
            var12,
            var11
         );
      }
   }

   public static void ring3(double var0, double var2, double var4, double[] var6, double[] var7, double var8, double var10, int var12, int var13) {
      for (int var14 = 0; var14 < var12; var14++) {
         double var15 = (Math.PI * 2) * var14 / var12;
         double var17 = (Math.PI * 2) * (var14 + 1) / var12;
         double var19 = Math.cos(var15);
         double var21 = Math.sin(var15);
         double var23 = Math.cos(var17);
         double var25 = Math.sin(var17);
         quad(
            var0 + (var6[0] * var19 + var7[0] * var21) * var8,
            var2 + (var6[1] * var19 + var7[1] * var21) * var8,
            var4 + (var6[2] * var19 + var7[2] * var21) * var8,
            var0 + (var6[0] * var19 + var7[0] * var21) * var10,
            var2 + (var6[1] * var19 + var7[1] * var21) * var10,
            var4 + (var6[2] * var19 + var7[2] * var21) * var10,
            var0 + (var6[0] * var23 + var7[0] * var25) * var10,
            var2 + (var6[1] * var23 + var7[1] * var25) * var10,
            var4 + (var6[2] * var23 + var7[2] * var25) * var10,
            var0 + (var6[0] * var23 + var7[0] * var25) * var8,
            var2 + (var6[1] * var23 + var7[1] * var25) * var8,
            var4 + (var6[2] * var23 + var7[2] * var25) * var8,
            var13
         );
      }
   }

   public static void cube(double var0, double var2, double var4, double var6, int var8) {
      double var9 = var6 / 2.0;
      box(var0 - var9, var2 - var9, var4 - var9, var0 + var9, var2 + var9, var4 + var9, var8);
   }

   public static void box(double var0, double var2, double var4, double var6, double var8, double var10, int var12) {
      quad(var0, var2, var4, var6, var2, var4, var6, var8, var4, var0, var8, var4, var12);
      quad(var0, var2, var10, var6, var2, var10, var6, var8, var10, var0, var8, var10, var12);
      quad(var0, var2, var4, var0, var2, var10, var0, var8, var10, var0, var8, var4, var12);
      quad(var6, var2, var4, var6, var2, var10, var6, var8, var10, var6, var8, var4, var12);
      quad(var0, var8, var4, var6, var8, var4, var6, var8, var10, var0, var8, var10, var12);
      quad(var0, var2, var4, var6, var2, var4, var6, var2, var10, var0, var2, var10, var12);
   }

   public static void line(double var0, double var2, double var4, double var6, double var8, double var10, double var12, int var14, int var15) {
      double var16 = var6 - var0;
      double var18 = var8 - var2;
      double var20 = var10 - var4;
      double var22 = (var0 + var6) / 2.0 - cx;
      double var24 = (var2 + var8) / 2.0 - cy;
      double var26 = (var4 + var10) / 2.0 - cz;
      double var28 = var18 * var26 - var20 * var24;
      double var30 = var20 * var22 - var16 * var26;
      double var32 = var16 * var24 - var18 * var22;
      double var34 = Math.sqrt(var28 * var28 + var30 * var30 + var32 * var32);
      if (!(var34 < 1.0E-9)) {
         double var36 = var12 / 2.0 / var34;
         var28 *= var36;
         var30 *= var36;
         var32 *= var36;
         quad(
            var0 - var28,
            var2 - var30,
            var4 - var32,
            var0 + var28,
            var2 + var30,
            var4 + var32,
            var6 + var28,
            var8 + var30,
            var10 + var32,
            var6 - var28,
            var8 - var30,
            var10 - var32,
            var14,
            var14,
            var15,
            var15
         );
      }
   }

   public static void boxEdges(double var0, double var2, double var4, double var6, double var8, double var10, double var12, int var14) {
      double[][] var15 = new double[][]{
         {var0, var2, var4},
         {var6, var2, var4},
         {var6, var2, var10},
         {var0, var2, var10},
         {var0, var8, var4},
         {var6, var8, var4},
         {var6, var8, var10},
         {var0, var8, var10}
      };
      int[][] var16 = new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};

      for (int[] var20 : var16) {
         line(var15[var20[0]][0], var15[var20[0]][1], var15[var20[0]][2], var15[var20[1]][0], var15[var20[1]][1], var15[var20[1]][2], var12, var14, var14);
      }
   }

   private static void tv(R3.Batch var0, double var1, double var3, double var5, float var7, float var8, int var9) {
      var0.v(var1, var3, var5, var9, var7, var8, 15728880, 0.0F, 1.0F, 0.0F);
   }

   public static void texQuad(Identifier var0, int var1, double[] var2, float[] var3, int var4, int var5, float var6, float var7, float var8) {
      R3.Batch var9 = layer(
         var1 == 2 ? RenderTypes.entityTranslucentEmissive(var0) : (var1 == 1 ? RenderTypes.entityTranslucent(var0) : RenderTypes.entityCutoutNoCull(var0))
      );
      int var10 = var1 == 2 ? 15728880 : var5;

      for (int var11 = 0; var11 < 4; var11++) {
         lv(var9, var2, var3, var11, var4, var10, var6, var7, var8);
      }
   }

   private static void lv(R3.Batch var0, double[] var1, float[] var2, int var3, int var4, int var5, float var6, float var7, float var8) {
      var0.v(var1[var3 * 3], var1[var3 * 3 + 1], var1[var3 * 3 + 2], var4, var2[var3 * 2], var2[var3 * 2 + 1], var5, var6, var7, var8);
   }

   private static R3.Batch layer(RenderType var0) {
      return batch(var0);
   }

   public static void billboard(
      Identifier var0, double var1, double var3, double var5, double var7, double var9, float var11, float var12, float var13, float var14, int var15
   ) {
      R3.Batch var16 = layer(RenderTypes.entityTranslucent(var0));
      double var17 = Math.toRadians(camYaw);
      double var19 = Math.toRadians(camPitch);
      double var21 = -Math.cos(var17);
      double var23 = -Math.sin(var17);
      double var25 = -Math.sin(var17) * Math.sin(var19);
      double var27 = Math.cos(var19);
      double var29 = Math.cos(var17) * Math.sin(var19);
      double var31 = var7 / 2.0;
      double var33 = var9 / 2.0;
      double[][] var35 = new double[][]{
         {var1 - var21 * var31 - var25 * var33, var3 - var27 * var33, var5 - var23 * var31 - var29 * var33, var11, var14},
         {var1 + var21 * var31 - var25 * var33, var3 - var27 * var33, var5 + var23 * var31 - var29 * var33, var13, var14},
         {var1 + var21 * var31 + var25 * var33, var3 + var27 * var33, var5 + var23 * var31 + var29 * var33, var13, var12},
         {var1 - var21 * var31 + var25 * var33, var3 + var27 * var33, var5 - var23 * var31 + var29 * var33, var11, var12}
      };
      double[][] var36 = new double[][]{var35[0], var35[1], var35[2], var35[3], var35[3], var35[2], var35[1], var35[0]};

      for (double[] var40 : var36) {
         tv(var16, var40[0], var40[1], var40[2], (float)var40[3], (float)var40[4], var15);
      }
   }

   public static void billboardRot(Identifier var0, double var1, double var3, double var5, double var7, double var9, int var11) {
      R3.Batch var12 = layer(RenderTypes.entityTranslucent(var0));
      double var13 = Math.toRadians(camYaw);
      double var15 = Math.toRadians(camPitch);
      double var17 = -Math.cos(var13);
      double var19 = -Math.sin(var13);
      double var21 = -Math.sin(var13) * Math.sin(var15);
      double var23 = Math.cos(var15);
      double var25 = Math.cos(var13) * Math.sin(var15);
      double var27 = Math.cos(var9);
      double var29 = Math.sin(var9);
      double var31 = var7 / 2.0;
      double var33 = (var17 * var27 + var21 * var29) * var31;
      double var35 = var23 * var29 * var31;
      double var37 = (var19 * var27 + var25 * var29) * var31;
      double var39 = (-var17 * var29 + var21 * var27) * var31;
      double var41 = var23 * var27 * var31;
      double var43 = (-var19 * var29 + var25 * var27) * var31;
      double[][] var45 = new double[][]{
         {var1 - var33 - var39, var3 - var35 - var41, var5 - var37 - var43, 0.0, 1.0},
         {var1 + var33 - var39, var3 + var35 - var41, var5 + var37 - var43, 1.0, 1.0},
         {var1 + var33 + var39, var3 + var35 + var41, var5 + var37 + var43, 1.0, 0.0},
         {var1 - var33 + var39, var3 - var35 + var41, var5 - var37 + var43, 0.0, 0.0}
      };
      double[][] var46 = new double[][]{var45[0], var45[1], var45[2], var45[3], var45[3], var45[2], var45[1], var45[0]};

      for (double[] var50 : var46) {
         tv(var12, var50[0], var50[1], var50[2], (float)var50[3], (float)var50[4], var11);
      }
   }

   public static void glowBillboard(Identifier var0, double var1, double var3, double var5, double var7, int var9) {
      R3.Batch var10 = layer(RenderTypes.entityTranslucentEmissive(var0));
      double var11 = Math.toRadians(camYaw);
      double var13 = Math.toRadians(camPitch);
      double var15 = -Math.cos(var11);
      double var17 = -Math.sin(var11);
      double var19 = -Math.sin(var11) * Math.sin(var13);
      double var21 = Math.cos(var13);
      double var23 = Math.cos(var11) * Math.sin(var13);
      double var25 = var7 / 2.0;
      double[][] var27 = new double[][]{
         {var1 - var15 * var25 - var19 * var25, var3 - var21 * var25, var5 - var17 * var25 - var23 * var25, 0.0, 1.0},
         {var1 + var15 * var25 - var19 * var25, var3 - var21 * var25, var5 + var17 * var25 - var23 * var25, 1.0, 1.0},
         {var1 + var15 * var25 + var19 * var25, var3 + var21 * var25, var5 + var17 * var25 + var23 * var25, 1.0, 0.0},
         {var1 - var15 * var25 + var19 * var25, var3 + var21 * var25, var5 - var17 * var25 + var23 * var25, 0.0, 0.0}
      };
      double[][] var28 = new double[][]{var27[0], var27[1], var27[2], var27[3], var27[3], var27[2], var27[1], var27[0]};

      for (double[] var32 : var28) {
         tv(var10, var32[0], var32[1], var32[2], (float)var32[3], (float)var32[4], var9);
      }
   }

   public static void billboard(Identifier var0, double var1, double var3, double var5, double var7, int var9) {
      billboard(var0, var1, var3, var5, var7, var7, 0.0F, 0.0F, 1.0F, 1.0F, var9);
   }

   public static RenderType skyLayer(Identifier var0) {
      RenderType var1 = SKY.get(var0);
      if (var1 != null) {
         return var1;
      } else {
         if (!skyBroken) {
            try {
               RenderSetup var2 = RenderSetup.builder(RenderPipelines.END_SKY).withTexture("Sampler0", var0).sortOnUpload().createRenderSetup();
               if (layerOf == null) {
                  layerOf = RenderType.class.getDeclaredMethod("method_75940", String.class, RenderSetup.class);
                  layerOf.setAccessible(true);
               }

               var1 = (RenderType)layerOf.invoke(null, "legoclient_sky", var2);
            } catch (Throwable var3) {
               skyBroken = true;
               LegoClient.LOG("Sky-Layer nicht verfügbar, nutze Ersatz: " + var3);
            }
         }

         if (var1 == null) {
            var1 = RenderTypes.entityTranslucent(var0);
         }

         SKY.put(var0, var1);
         return var1;
      }
   }

   public static void skyQuad(RenderType var0, double[] var1, double[] var2, double[] var3, double[] var4, float[] var5, int var6) {
      R3.Batch var7 = layer(var0);
      double[][] var8 = new double[][]{var1, var2, var3, var4};
      int[] var9 = new int[]{0, 1, 2, 3, 3, 2, 1, 0};

      for (int var13 : var9) {
         tv(var7, var8[var13][0], var8[var13][1], var8[var13][2], var5[var13 * 2], var5[var13 * 2 + 1], var6);
      }
   }

   public static void end() {
      try {
         if (consumers != null) {
            emit();
         }
      } catch (Throwable var4) {
         BATCH.clear();
         lastLayer = null;
         lastBatch = null;
         LegoClient.LOG("R3: " + var4);
      } finally {
         quads = null;
         used.clear();
         consumers = null;
         skyRadius = 0.0;
      }
   }

   public static void flush() {
      if (consumers != null) {
         emit();
      }

      used.clear();
      quads = null;
   }

   public static int argb(int var0, double var1) {
      int var3 = (int)Math.max(0L, Math.min(255L, Math.round(var1 * 255.0)));
      return var3 << 24 | var0 & 16777215;
   }

   private static final class Batch {
      float[] d = new float[640];
      int n;

      void v(double var1, double var3, double var5, int var7, float var8, float var9, int var10, float var11, float var12, float var13) {
         if (this.n + 10 > this.d.length) {
            this.d = Arrays.copyOf(this.d, this.d.length * 2);
         }

         float[] var14 = this.d;
         int var15 = this.n;
         var14[var15] = (float)(var1 - R3.cx);
         var14[var15 + 1] = (float)(var3 - R3.cy);
         var14[var15 + 2] = (float)(var5 - R3.cz);
         var14[var15 + 3] = Float.intBitsToFloat(var7);
         var14[var15 + 4] = var8;
         var14[var15 + 5] = var9;
         var14[var15 + 6] = Float.intBitsToFloat(var10);
         var14[var15 + 7] = var11;
         var14[var15 + 8] = var12;
         var14[var15 + 9] = var13;
         this.n = var15 + 10;
      }
   }
}
