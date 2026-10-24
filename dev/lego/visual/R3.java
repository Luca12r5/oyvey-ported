package dev.lego.visual;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import dev.lego.LegoClient;
import dev.lego.core.Mc;
import dev.lego.visual.shader.ShaderUtil;
import java.lang.reflect.Field;
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
   private static RenderType noDepthLayer;
   private static boolean noDepthBroken;
   public static final int CUTOUT = 0;
   public static final int TRANSLUCENT = 1;
   public static final int GLOW = 2;
   private static final Map<Identifier, RenderType> SKY = new HashMap<>();
   private static Method layerOf;
   private static boolean skyBroken = false;

   private R3() {
   }

   private static R3.Batch batch(RenderType layer) {
      if (layer == lastLayer) {
         return lastBatch;
      } else {
         R3.Batch b = BATCH.get(layer);
         if (b == null) {
            b = POOL.isEmpty() ? new R3.Batch() : POOL.pop();
            b.n = 0;
            BATCH.put(layer, b);
         }

         lastLayer = layer;
         lastBatch = b;
         return b;
      }
   }

   private static void emit() {
      int verts = 0;

      for (Entry<RenderType, R3.Batch> e : BATCH.entrySet()) {
         R3.Batch b = e.getValue();
         if (b.n != 0) {
            VertexConsumer vc = consumers.getBuffer(e.getKey());
            float[] a = b.d;

            for (int i = 0; i < b.n; i += 10) {
               vc.addVertex(entry, a[i], a[i + 1], a[i + 2])
                  .setColor(Float.floatToRawIntBits(a[i + 3]))
                  .setUv(a[i + 4], a[i + 5])
                  .setOverlay(OverlayTexture.NO_OVERLAY)
                  .setLight(Float.floatToRawIntBits(a[i + 6]))
                  .setNormal(entry, a[i + 7], a[i + 8], a[i + 9]);
            }

            verts += b.n / 10;
            if (consumers instanceof BufferSource) {
               ((BufferSource)consumers).endBatch(e.getKey());
            }
         }
      }

      lastVertices = verts;
      lastLayers = BATCH.size();

      for (R3.Batch b : BATCH.values()) {
         b.n = 0;
         if (POOL.size() < 64) {
            POOL.push(b);
         }
      }

      BATCH.clear();
      lastLayer = null;
      lastBatch = null;
   }

   public static boolean begin(WorldRenderContext ctx) {
      consumers = ctx.consumers();
      PoseStack m = ctx.matrices();
      if (consumers != null && m != null) {
         entry = m.last();
         Vec3 cam = ctx.worldState().cameraRenderState.pos;
         cx = cam.x;
         cy = cam.y;
         cz = cam.z;

         try {
            Camera c = Mc.mc().gameRenderer.getMainCamera();
            camYaw = c.yRot();
            camPitch = c.xRot();
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

   public static void beginSky(MultiBufferSource imm) {
      consumers = imm;
      entry = new PoseStack().last();
      cz = 0.0;
      cy = 0.0;
      cx = 0.0;
      skyRadius = 90.0;

      try {
         Camera c = Mc.mc().gameRenderer.getMainCamera();
         camYaw = c.yRot();
         camPitch = c.xRot();
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

   private static R3.Batch qNoDepth() {
      if (noDepthLayer == null && !noDepthBroken) {
         try {
            Field snippetField = RenderPipelines.class.getDeclaredField("field_56860");
            snippetField.setAccessible(true);
            Snippet snippet = (Snippet)snippetField.get(null);
            RenderPipeline pipeline = ShaderUtil.register(
               RenderPipeline.builder(snippet)
                  .withLocation("legoclient:pipeline/no_depth_quads")
                  .withCull(false)
                  .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                  .withDepthWrite(false)
                  .build()
            );
            RenderSetup setup = RenderSetup.builder(pipeline).sortOnUpload().createRenderSetup();
            if (layerOf == null) {
               layerOf = RenderType.class.getDeclaredMethod("method_75940", String.class, RenderSetup.class);
               layerOf.setAccessible(true);
            }

            noDepthLayer = (RenderType)layerOf.invoke(null, "legoclient_no_depth", setup);
            LegoClient.LOG("No-depth render layer created successfully");
         } catch (Throwable var4) {
            noDepthBroken = true;
            LegoClient.LOG("No-depth layer failed, using debugQuads: " + var4);
            var4.printStackTrace();
         }
      }

      return batch(noDepthLayer != null ? noDepthLayer : (quadLayer != null ? quadLayer : (quadLayer = RenderTypes.debugQuads())));
   }

   private static void v(R3.Batch vc, double x, double y, double z, int argb) {
      vc.v(x, y, z, argb, 0.0F, 0.0F, 15728880, 0.0F, 1.0F, 0.0F);
   }

   public static void quad(
      double x1,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2,
      double x3,
      double y3,
      double z3,
      double x4,
      double y4,
      double z4,
      int c1,
      int c2,
      int c3,
      int c4
   ) {
      R3.Batch vc = q();
      v(vc, x1, y1, z1, c1);
      v(vc, x2, y2, z2, c2);
      v(vc, x3, y3, z3, c3);
      v(vc, x4, y4, z4, c4);
   }

   public static void quad(
      double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, int c
   ) {
      quad(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, c, c, c, c);
   }

   public static void ring(double x, double y, double z, double r0, double r1, int seg, int cInner, int cOuter, double rot) {
      for (int i = 0; i < seg; i++) {
         double a0 = rot + (Math.PI * 2) * i / seg;
         double a1 = rot + (Math.PI * 2) * (i + 1) / seg;
         double c0 = Math.cos(a0);
         double s0 = Math.sin(a0);
         double c1 = Math.cos(a1);
         double s1 = Math.sin(a1);
         quad(
            x + c0 * r0, y, z + s0 * r0, x + c0 * r1, y, z + s0 * r1, x + c1 * r1, y, z + s1 * r1, x + c1 * r0, y, z + s1 * r0, cInner, cOuter, cOuter, cInner
         );
      }
   }

   public static void ring3(double x, double y, double z, double[] u, double[] w, double r0, double r1, int seg, int c) {
      for (int i = 0; i < seg; i++) {
         double a0 = (Math.PI * 2) * i / seg;
         double a1 = (Math.PI * 2) * (i + 1) / seg;
         double c0 = Math.cos(a0);
         double s0 = Math.sin(a0);
         double c1 = Math.cos(a1);
         double s1 = Math.sin(a1);
         quad(
            x + (u[0] * c0 + w[0] * s0) * r0,
            y + (u[1] * c0 + w[1] * s0) * r0,
            z + (u[2] * c0 + w[2] * s0) * r0,
            x + (u[0] * c0 + w[0] * s0) * r1,
            y + (u[1] * c0 + w[1] * s0) * r1,
            z + (u[2] * c0 + w[2] * s0) * r1,
            x + (u[0] * c1 + w[0] * s1) * r1,
            y + (u[1] * c1 + w[1] * s1) * r1,
            z + (u[2] * c1 + w[2] * s1) * r1,
            x + (u[0] * c1 + w[0] * s1) * r0,
            y + (u[1] * c1 + w[1] * s1) * r0,
            z + (u[2] * c1 + w[2] * s1) * r0,
            c
         );
      }
   }

   public static void cube(double x, double y, double z, double s, int c) {
      double a = s / 2.0;
      box(x - a, y - a, z - a, x + a, y + a, z + a, c);
   }

   public static void box(double x0, double y0, double z0, double x1, double y1, double z1, int c) {
      quad(x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, c);
      quad(x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, c);
      quad(x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, c);
      quad(x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, c);
      quad(x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, c);
      quad(x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, c);
   }

   public static void line(double x1, double y1, double z1, double x2, double y2, double z2, double width, int c1, int c2) {
      double dx = x2 - x1;
      double dy = y2 - y1;
      double dz = z2 - z1;
      double mx = (x1 + x2) / 2.0 - cx;
      double my = (y1 + y2) / 2.0 - cy;
      double mz = (z1 + z2) / 2.0 - cz;
      double sx = dy * mz - dz * my;
      double sy = dz * mx - dx * mz;
      double sz = dx * my - dy * mx;
      double l = Math.sqrt(sx * sx + sy * sy + sz * sz);
      if (!(l < 1.0E-9)) {
         double k = width / 2.0 / l;
         sx *= k;
         sy *= k;
         sz *= k;
         quad(x1 - sx, y1 - sy, z1 - sz, x1 + sx, y1 + sy, z1 + sz, x2 + sx, y2 + sy, z2 + sz, x2 - sx, y2 - sy, z2 - sz, c1, c1, c2, c2);
      }
   }

   public static void boxEdges(double x0, double y0, double z0, double x1, double y1, double z1, double w, int c) {
      double[][] p = new double[][]{{x0, y0, z0}, {x1, y0, z0}, {x1, y0, z1}, {x0, y0, z1}, {x0, y1, z0}, {x1, y1, z0}, {x1, y1, z1}, {x0, y1, z1}};
      int[][] e = new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};

      for (int[] i : e) {
         line(p[i[0]][0], p[i[0]][1], p[i[0]][2], p[i[1]][0], p[i[1]][1], p[i[1]][2], w, c, c);
      }
   }

   private static void noDepthQuad(
      double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, int c
   ) {
      R3.Batch vc = qNoDepth();
      v(vc, x1, y1, z1, c);
      v(vc, x2, y2, z2, c);
      v(vc, x3, y3, z3, c);
      v(vc, x4, y4, z4, c);
   }

   public static void noDepthBox(double x0, double y0, double z0, double x1, double y1, double z1, int c) {
      noDepthQuad(x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, c);
      noDepthQuad(x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, c);
      noDepthQuad(x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, c);
      noDepthQuad(x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, c);
      noDepthQuad(x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, c);
      noDepthQuad(x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, c);
   }

   private static void noDepthLine(double x1, double y1, double z1, double x2, double y2, double z2, double width, int c) {
      double dx = x2 - x1;
      double dy = y2 - y1;
      double dz = z2 - z1;
      double mx = (x1 + x2) / 2.0 - cx;
      double my = (y1 + y2) / 2.0 - cy;
      double mz = (z1 + z2) / 2.0 - cz;
      double sx = dy * mz - dz * my;
      double sy = dz * mx - dx * mz;
      double sz = dx * my - dy * mx;
      double l = Math.sqrt(sx * sx + sy * sy + sz * sz);
      if (!(l < 1.0E-9)) {
         double k = width / 2.0 / l;
         sx *= k;
         sy *= k;
         sz *= k;
         R3.Batch vc = qNoDepth();
         v(vc, x1 - sx, y1 - sy, z1 - sz, c);
         v(vc, x1 + sx, y1 + sy, z1 + sz, c);
         v(vc, x2 + sx, y2 + sy, z2 + sz, c);
         v(vc, x2 - sx, y2 - sy, z2 - sz, c);
      }
   }

   public static void noDepthBoxEdges(double x0, double y0, double z0, double x1, double y1, double z1, double w, int c) {
      double[][] p = new double[][]{{x0, y0, z0}, {x1, y0, z0}, {x1, y0, z1}, {x0, y0, z1}, {x0, y1, z0}, {x1, y1, z0}, {x1, y1, z1}, {x0, y1, z1}};
      int[][] e = new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};

      for (int[] i : e) {
         noDepthLine(p[i[0]][0], p[i[0]][1], p[i[0]][2], p[i[1]][0], p[i[1]][1], p[i[1]][2], w, c);
      }
   }

   private static void tv(R3.Batch vc, double x, double y, double z, float u, float v, int argb) {
      vc.v(x, y, z, argb, u, v, 15728880, 0.0F, 1.0F, 0.0F);
   }

   public static void texQuad(Identifier texture, int mode, double[] p, float[] uv, int argb, int light, float nx, float ny, float nz) {
      R3.Batch vc = layer(
         mode == 2
            ? RenderTypes.entityTranslucentEmissive(texture)
            : (mode == 1 ? RenderTypes.entityTranslucent(texture) : RenderTypes.entityCutoutNoCull(texture))
      );
      int li = mode == 2 ? 15728880 : light;

      for (int k = 0; k < 4; k++) {
         lv(vc, p, uv, k, argb, li, nx, ny, nz);
      }
   }

   private static void lv(R3.Batch vc, double[] p, float[] uv, int k, int argb, int light, float nx, float ny, float nz) {
      vc.v(p[k * 3], p[k * 3 + 1], p[k * 3 + 2], argb, uv[k * 2], uv[k * 2 + 1], light, nx, ny, nz);
   }

   private static R3.Batch layer(RenderType layer) {
      return batch(layer);
   }

   public static void billboard(Identifier texture, double x, double y, double z, double w, double h, float u0, float v0, float u1, float v1, int argb) {
      R3.Batch vc = layer(RenderTypes.entityTranslucent(texture));
      double yaw = Math.toRadians(camYaw);
      double pitch = Math.toRadians(camPitch);
      double rx = -Math.cos(yaw);
      double rz = -Math.sin(yaw);
      double ux = -Math.sin(yaw) * Math.sin(pitch);
      double uy = Math.cos(pitch);
      double uz = Math.cos(yaw) * Math.sin(pitch);
      double hw = w / 2.0;
      double hh = h / 2.0;
      double[][] p = new double[][]{
         {x - rx * hw - ux * hh, y - uy * hh, z - rz * hw - uz * hh, u0, v1},
         {x + rx * hw - ux * hh, y - uy * hh, z + rz * hw - uz * hh, u1, v1},
         {x + rx * hw + ux * hh, y + uy * hh, z + rz * hw + uz * hh, u1, v0},
         {x - rx * hw + ux * hh, y + uy * hh, z - rz * hw + uz * hh, u0, v0}
      };
      double[][] order = new double[][]{p[0], p[1], p[2], p[3], p[3], p[2], p[1], p[0]};

      for (double[] c : order) {
         tv(vc, c[0], c[1], c[2], (float)c[3], (float)c[4], argb);
      }
   }

   public static void billboardRot(Identifier texture, double x, double y, double z, double size, double angle, int argb) {
      R3.Batch vc = layer(RenderTypes.entityTranslucent(texture));
      double yaw = Math.toRadians(camYaw);
      double pitch = Math.toRadians(camPitch);
      double rx = -Math.cos(yaw);
      double rz = -Math.sin(yaw);
      double ux = -Math.sin(yaw) * Math.sin(pitch);
      double uy = Math.cos(pitch);
      double uz = Math.cos(yaw) * Math.sin(pitch);
      double c = Math.cos(angle);
      double s = Math.sin(angle);
      double h = size / 2.0;
      double ax = (rx * c + ux * s) * h;
      double ay = uy * s * h;
      double az = (rz * c + uz * s) * h;
      double bx = (-rx * s + ux * c) * h;
      double by = uy * c * h;
      double bz = (-rz * s + uz * c) * h;
      double[][] p = new double[][]{
         {x - ax - bx, y - ay - by, z - az - bz, 0.0, 1.0},
         {x + ax - bx, y + ay - by, z + az - bz, 1.0, 1.0},
         {x + ax + bx, y + ay + by, z + az + bz, 1.0, 0.0},
         {x - ax + bx, y - ay + by, z - az + bz, 0.0, 0.0}
      };
      double[][] order = new double[][]{p[0], p[1], p[2], p[3], p[3], p[2], p[1], p[0]};

      for (double[] q : order) {
         tv(vc, q[0], q[1], q[2], (float)q[3], (float)q[4], argb);
      }
   }

   public static void glowBillboard(Identifier texture, double x, double y, double z, double size, int argb) {
      R3.Batch vc = layer(RenderTypes.entityTranslucentEmissive(texture));
      double yaw = Math.toRadians(camYaw);
      double pitch = Math.toRadians(camPitch);
      double rx = -Math.cos(yaw);
      double rz = -Math.sin(yaw);
      double ux = -Math.sin(yaw) * Math.sin(pitch);
      double uy = Math.cos(pitch);
      double uz = Math.cos(yaw) * Math.sin(pitch);
      double h = size / 2.0;
      double[][] p = new double[][]{
         {x - rx * h - ux * h, y - uy * h, z - rz * h - uz * h, 0.0, 1.0},
         {x + rx * h - ux * h, y - uy * h, z + rz * h - uz * h, 1.0, 1.0},
         {x + rx * h + ux * h, y + uy * h, z + rz * h + uz * h, 1.0, 0.0},
         {x - rx * h + ux * h, y + uy * h, z - rz * h + uz * h, 0.0, 0.0}
      };
      double[][] order = new double[][]{p[0], p[1], p[2], p[3], p[3], p[2], p[1], p[0]};

      for (double[] q : order) {
         tv(vc, q[0], q[1], q[2], (float)q[3], (float)q[4], argb);
      }
   }

   public static void billboard(Identifier texture, double x, double y, double z, double size, int argb) {
      billboard(texture, x, y, z, size, size, 0.0F, 0.0F, 1.0F, 1.0F, argb);
   }

   public static RenderType skyLayer(Identifier tex) {
      RenderType l = SKY.get(tex);
      if (l != null) {
         return l;
      } else {
         if (!skyBroken) {
            try {
               RenderSetup setup = RenderSetup.builder(RenderPipelines.END_SKY).withTexture("Sampler0", tex).sortOnUpload().createRenderSetup();
               if (layerOf == null) {
                  layerOf = RenderType.class.getDeclaredMethod("method_75940", String.class, RenderSetup.class);
                  layerOf.setAccessible(true);
               }

               l = (RenderType)layerOf.invoke(null, "legoclient_sky", setup);
            } catch (Throwable var3) {
               skyBroken = true;
               LegoClient.LOG("Sky-Layer nicht verfügbar, nutze Ersatz: " + var3);
            }
         }

         if (l == null) {
            l = RenderTypes.entityTranslucent(tex);
         }

         SKY.put(tex, l);
         return l;
      }
   }

   public static void skyQuad(RenderType layer, double[] p0, double[] p1, double[] p2, double[] p3, float[] uv, int argb) {
      R3.Batch vc = layer(layer);
      double[][] p = new double[][]{p0, p1, p2, p3};
      int[] order = new int[]{0, 1, 2, 3, 3, 2, 1, 0};

      for (int i : order) {
         tv(vc, p[i][0], p[i][1], p[i][2], uv[i * 2], uv[i * 2 + 1], argb);
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

   public static int argb(int rgb, double alpha) {
      int a = (int)Math.max(0L, Math.min(255L, Math.round(alpha * 255.0)));
      return a << 24 | rgb & 16777215;
   }

   private static final class Batch {
      float[] d = new float[640];
      int n;

      void v(double x, double y, double z, int argb, float u, float v, int light, float nx, float ny, float nz) {
         if (this.n + 10 > this.d.length) {
            this.d = Arrays.copyOf(this.d, this.d.length * 2);
         }

         float[] a = this.d;
         int i = this.n;
         a[i] = (float)(x - R3.cx);
         a[i + 1] = (float)(y - R3.cy);
         a[i + 2] = (float)(z - R3.cz);
         a[i + 3] = Float.intBitsToFloat(argb);
         a[i + 4] = u;
         a[i + 5] = v;
         a[i + 6] = Float.intBitsToFloat(light);
         a[i + 7] = nx;
         a[i + 8] = ny;
         a[i + 9] = nz;
         this.n = i + 10;
      }
   }
}
