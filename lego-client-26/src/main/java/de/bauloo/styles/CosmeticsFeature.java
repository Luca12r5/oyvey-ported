package de.bauloo.styles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class CosmeticsFeature extends RenderLayer<AvatarRenderState, PlayerModel> {
   private static final float PX = 0.0625F;
   private static final int FULL_BRIGHT = 15728880;

   public CosmeticsFeature(RenderLayerParent<AvatarRenderState, PlayerModel> context) {
      super(context);
   }

   public void submit(PoseStack matrices, SubmitNodeCollector queue, int light, AvatarRenderState state, float limbAngle, float limbDistance) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && state.id == mc.player.getId() && !state.isInvisible) {
         try {
            String head = Cosmetics.current(Cosmetics.Slot.HEAD);
            if (!"none".equals(head)) {
               this.head(matrices, queue, light, state, head);
            }

            String aura = Cosmetics.current(Cosmetics.Slot.AURA);
            if (!"none".equals(aura)) {
               this.aura(matrices, queue, state, aura);
            }
         } catch (RuntimeException var10) {
         }
      }
   }

   private static void vertex(Pose entry, VertexConsumer vc, float x, float y, float z, float u, float v, int color, int light, float nx, float ny, float nz) {
      vc.addVertex(entry, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(entry, nx, ny, nz);
   }

   private void head(PoseStack matrices, SubmitNodeCollector queue, int light, AvatarRenderState state, String key) {
      matrices.pushPose();
      ((PlayerModel)this.getParentModel()).head.translateAndRotate(matrices);
      switch (key) {
         case "halo":
            float bob = (float)Math.sin(state.ageInTicks * 0.08F) * 0.5F;
            matrices.translate(0.0F, (-11.5F + bob) * 0.0625F, 0.0F);
            matrices.mulPose(Axis.YP.rotationDegrees(state.ageInTicks * 2.0F));
            int c = 0xFF000000 | Cosmetics.color();
            queue.submitCustomGeometry(matrices, RenderTypes.entityTranslucentEmissive(Cosmetics.WHITE), (entry, vc) -> {
               ring(entry, vc, 0.2125F, 0.2875F, 0.0F, c, 15728880);
               ring(entry, vc, 0.2875F, 0.35F, 0.0F, 1342177280 | c & 16777215, 15728880);
            });
            break;
         case "crown":
            queue.submitCustomGeometry(
               matrices,
               RenderTypes.entityCutout(Cosmetics.WHITE),
               (entry, vc) -> {
                  int gold = -14276;
                  int dark = -3634146;
                  float rx = 4.35F;
                  box(entry, vc, -rx, -9.6F, -rx, rx, -8.0F, -rx + 0.6F, gold, light);
                  box(entry, vc, -rx, -9.6F, rx - 0.6F, rx, -8.0F, rx, gold, light);
                  box(entry, vc, -rx, -9.6F, -rx, -rx + 0.6F, -8.0F, rx, gold, light);
                  box(entry, vc, rx - 0.6F, -9.6F, -rx, rx, -8.0F, rx, gold, light);
                  float[][] points = new float[][]{
                     {-rx, -rx},
                     {rx - 0.9F, -rx},
                     {-rx, rx - 0.9F},
                     {rx - 0.9F, rx - 0.9F},
                     {-0.45F, -rx},
                     {-0.45F, rx - 0.9F},
                     {-rx, -0.45F},
                     {rx - 0.9F, -0.45F}
                  };

                  for (float[] p : points) {
                     box(entry, vc, p[0], -11.4F, p[1], p[0] + 0.9F, -9.6F, p[1] + 0.9F, dark, light);
                  }

                  box(entry, vc, -0.6F, -9.2F, -rx - 0.2F, 0.6F, -8.4F, -rx + 0.2F, -2084774, light);
                  box(entry, vc, -0.6F, -9.2F, rx - 0.2F, 0.6F, -8.4F, rx + 0.2F, -11886337, light);
               }
            );
            break;
         case "horns":
            queue.submitCustomGeometry(matrices, RenderTypes.entityCutout(Cosmetics.WHITE), (entry, vc) -> {
               for (int side = -1; side <= 1; side += 2) {
                  float[][] parts = new float[][]{{3.0F, -8.4F, 1.8F}, {3.7F, -9.9F, 1.4F}, {4.5F, -11.2F, 1.1F}, {5.4F, -12.2F, 0.8F}, {6.2F, -12.8F, 0.5F}};

                  for (int i = 0; i < parts.length; i++) {
                     float[] p = parts[i];
                     float h = p[2] / 2.0F;
                     int col = 0xFF000000 | StylesTheme.mix(-7728092, -14810104, (float)i / (parts.length - 1));
                     box(entry, vc, side * p[0] - h, p[1] - h, -1.5F - h, side * p[0] + h, p[1] + h, -1.5F + h, col, light);
                  }
               }
            });
            break;
         case "ears":
            queue.submitCustomGeometry(matrices, RenderTypes.entityCutout(Cosmetics.WHITE), (entry, vc) -> {
               int fur = 0xFF000000 | Cosmetics.color();
               int dark = 0xFF000000 | StylesTheme.mix(fur, -16777216, 0.35F);

               for (int side = -1; side <= 1; side += 2) {
                  float x = side * 2.6F;
                  box(entry, vc, x - 1.5F, -9.4F, -0.5F, x + 1.5F, -8.0F, 0.5F, dark, light);
                  box(entry, vc, x - 1.0F, -10.6F, -0.45F, x + 1.0F, -9.4F, 0.45F, fur, light);
                  box(entry, vc, x - 0.5F, -11.5F, -0.4F, x + 0.5F, -10.6F, 0.4F, fur, light);
                  box(entry, vc, x - 0.7F, -10.3F, -0.55F, x + 0.7F, -8.8F, -0.45F, -24892, light);
               }
            });
            break;
         case "pumpkin_head":
            float r = 4.7F;
            queue.submitCustomGeometry(matrices, RenderTypes.entityCutout(Cosmetics.WHITE), (entry, vc) -> {
               int orange = -1017322;
               int ridge = -3579889;
               box(entry, vc, -r, -9.2F, -r, r, 0.4F, r, orange, light);

               for (float x : new float[]{-2.4F, 2.4F}) {
                  box(entry, vc, x - 0.35F, -9.15F, -r - 0.05F, x + 0.35F, 0.35F, -r + 0.2F, ridge, light);
                  box(entry, vc, x - 0.35F, -9.15F, r - 0.2F, x + 0.35F, 0.35F, r + 0.05F, ridge, light);
                  box(entry, vc, -r - 0.05F, -9.15F, x - 0.35F, -r + 0.2F, 0.35F, x + 0.35F, ridge, light);
                  box(entry, vc, r - 0.2F, -9.15F, x - 0.35F, r + 0.05F, 0.35F, x + 0.35F, ridge, light);
               }

               box(entry, vc, -0.7F, -11.0F, -0.7F, 0.7F, -9.2F, 0.7F, -10515918, light);
               pumpkinFace(entry, vc, -r - 0.08F, -r + 0.02F, 0.0F, -14021120, light);
            });
            float flick = 0.82F + 0.18F * (float)Math.sin(state.ageInTicks * 0.31F) * (float)Math.sin(state.ageInTicks * 0.77F);
            int glowColor = Math.round(flick * 255.0F) << 24 | 16762954;
            queue.submitCustomGeometry(
               matrices,
               RenderTypes.entityTranslucentEmissive(Cosmetics.WHITE),
               (entry, vc) -> pumpkinFace(entry, vc, -r - 0.11F, -r - 0.07F, 0.25F, glowColor, 15728880)
            );
            break;
         case "witch_hat":
            queue.submitCustomGeometry(
               matrices,
               RenderTypes.entityCutout(Cosmetics.WHITE),
               (entry, vc) -> {
                  int hat = -14019528;
                  box(entry, vc, -6.8F, -8.7F, -6.8F, 6.8F, -8.1F, 6.8F, -12968884, light);
                  float[][] cone = new float[][]{
                     {-8.7F, -10.8F, 3.9F, 0.0F},
                     {-10.8F, -12.6F, 3.0F, 0.3F},
                     {-12.6F, -14.2F, 2.2F, 0.9F},
                     {-14.2F, -15.5F, 1.5F, 1.7F},
                     {-15.5F, -16.4F, 0.9F, 2.6F},
                     {-16.4F, -17.0F, 0.5F, 3.4F}
                  };

                  for (float[] cx : cone) {
                     box(entry, vc, cx[3] - cx[2], cx[1], -cx[2], cx[3] + cx[2], cx[0], cx[2], hat, light);
                  }

                  box(entry, vc, -4.05F, -10.0F, -4.05F, 4.05F, -8.8F, 4.05F, -7536826, light);
                  box(entry, vc, -0.8F, -9.9F, -4.25F, 0.8F, -8.9F, -4.0F, -11686, light);
               }
            );
            break;
         case "tophat":
            queue.submitCustomGeometry(matrices, RenderTypes.entityCutout(Cosmetics.WHITE), (entry, vc) -> {
               int black = -15198690;
               int band = 0xFF000000 | Cosmetics.color();
               box(entry, vc, -5.5F, -8.7F, -5.5F, 5.5F, -8.0F, 5.5F, black, light);
               box(entry, vc, -3.6F, -15.2F, -3.6F, 3.6F, -8.7F, 3.6F, black, light);
               box(entry, vc, -3.75F, -10.2F, -3.75F, 3.75F, -8.9F, 3.75F, band, light);
            });
      }

      matrices.popPose();
   }

   private static void pumpkinFace(Pose e, VertexConsumer vc, float z0, float z1, float shrink, int color, int light) {
      float s = shrink;

      for (int side = -1; side <= 1; side += 2) {
         float cx = side * 2.3F;
         box(e, vc, cx - 1.1F + s, -5.6F + s, z0, cx + 1.1F - s, -4.6F - s, z1, color, light);
         box(e, vc, cx - 0.5F + s * 0.5F, -6.4F + s, z0, cx + 0.5F - s * 0.5F, -5.6F, z1, color, light);
      }

      box(e, vc, -0.5F + s * 0.5F, -4.2F + s, z0, 0.5F - s * 0.5F, -3.5F, z1, color, light);
      box(e, vc, -3.3F + s, -2.7F + s, z0, 3.3F - s, -1.6F - s, z1, color, light);

      for (float x : new float[]{-2.2F, 0.0F, 2.2F}) {
         box(e, vc, x - 0.4F + s * 0.4F, -3.2F + s, z0, x + 0.4F - s * 0.4F, -2.7F, z1, color, light);
      }

      for (float x : new float[]{-1.1F, 1.1F}) {
         box(e, vc, x - 0.4F + s * 0.4F, -1.6F, z0, x + 0.4F - s * 0.4F, -1.1F - s, z1, color, light);
      }
   }

   private static void box(Pose e, VertexConsumer vc, float x0, float y0, float z0, float x1, float y1, float z1, int color, int light) {
      x0 *= 0.0625F;
      y0 *= 0.0625F;
      z0 *= 0.0625F;
      x1 *= 0.0625F;
      y1 *= 0.0625F;
      z1 *= 0.0625F;
      int shade = color & 0xFF000000 | StylesTheme.mix(color, -16777216, 0.18F);
      face(e, vc, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, color, light, 0.0F, 0.0F, -1.0F);
      face(e, vc, x1, y0, z1, x0, y0, z1, x0, y1, z1, x1, y1, z1, color, light, 0.0F, 0.0F, 1.0F);
      face(e, vc, x0, y0, z1, x0, y0, z0, x0, y1, z0, x0, y1, z1, shade, light, -1.0F, 0.0F, 0.0F);
      face(e, vc, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, shade, light, 1.0F, 0.0F, 0.0F);
      face(e, vc, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0, color, light, 0.0F, -1.0F, 0.0F);
      face(e, vc, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, shade, light, 0.0F, 1.0F, 0.0F);
   }

   private static void face(
      Pose e,
      VertexConsumer vc,
      float ax,
      float ay,
      float az,
      float bx,
      float by,
      float bz,
      float cx,
      float cy,
      float cz,
      float dx,
      float dy,
      float dz,
      int color,
      int light,
      float nx,
      float ny,
      float nz
   ) {
      vertex(e, vc, ax, ay, az, 0.0F, 0.0F, color, light, nx, ny, nz);
      vertex(e, vc, bx, by, bz, 1.0F, 0.0F, color, light, nx, ny, nz);
      vertex(e, vc, cx, cy, cz, 1.0F, 1.0F, color, light, nx, ny, nz);
      vertex(e, vc, dx, dy, dz, 0.0F, 1.0F, color, light, nx, ny, nz);
   }

   private static void ring(Pose e, VertexConsumer vc, float r0, float r1, float y, int color, int light) {
      int seg = 32;

      for (int i = 0; i < seg; i++) {
         double a0 = (Math.PI * 2) * i / seg;
         double a1 = (Math.PI * 2) * (i + 1) / seg;
         float c0 = (float)Math.cos(a0);
         float s0 = (float)Math.sin(a0);
         float c1 = (float)Math.cos(a1);
         float s1 = (float)Math.sin(a1);
         face(e, vc, c0 * r0, y, s0 * r0, c1 * r0, y, s1 * r0, c1 * r1, y, s1 * r1, c0 * r1, y, s0 * r1, color, light, 0.0F, -1.0F, 0.0F);
      }
   }

   private void aura(PoseStack matrices, SubmitNodeCollector queue, AvatarRenderState state, String key) {
      int rgb = Cosmetics.color();
      RenderType glow = RenderTypes.entityTranslucentEmissive(Cosmetics.WHITE);
      float age = state.ageInTicks;
      switch (key) {
         case "orbit":
            queue.submitCustomGeometry(matrices, glow, (entry, vc) -> {
               for (int ix = 0; ix < 3; ix++) {
                  double ax = age * 0.09 + ix * Math.PI * 2.0 / 3.0;
                  float xx = (float)Math.cos(ax) * 11.0F;
                  float zx = (float)Math.sin(ax) * 11.0F;
                  float yx = 11.0F + (float)Math.sin(age * 0.07 + ix * 2) * 3.0F;
                  box(entry, vc, xx - 0.8F, yx - 0.8F, zx - 0.8F, xx + 0.8F, yx + 0.8F, zx + 0.8F, 0xFF000000 | rgb, 15728880);
                  box(entry, vc, xx - 1.6F, yx - 1.6F, zx - 1.6F, xx + 1.6F, yx + 1.6F, zx + 1.6F, 1073741824 | rgb, 15728880);
               }
            });
            break;
         case "ring":
            queue.submitCustomGeometry(matrices, glow, (entry, vc) -> {
               float pulse = (float)(0.6 + 0.4 * Math.sin(age * 0.1));
               ring(entry, vc, 0.5625F, 0.625F, 1.4875F, Math.round(pulse * 230.0F) << 24 | rgb, 15728880);
               float k = age % 40.0F / 40.0F;
               float r = 6.0F + k * 10.0F;
               ring(entry, vc, r * 0.0625F, (r + 0.6F) * 0.0625F, 1.48125F, Math.round((1.0F - k) * 160.0F) << 24 | rgb, 15728880);
            });
            break;
         case "sparkles":
            queue.submitCustomGeometry(matrices, glow, (entry, vc) -> {
               for (int ix = 0; ix < 16; ix++) {
                  float seed = ix * 12.9898F;
                  float phase = (float)((age * 0.012 + (Math.sin(seed) * 0.5 + 0.5)) % 1.0);
                  double ax = seed * 7.13;
                  float rad = 7.0F + (float)(Math.sin(seed * 3.1) * 0.5 + 0.5) * 6.0F;
                  float xx = (float)Math.cos(ax) * rad;
                  float zx = (float)Math.sin(ax) * rad;
                  float yx = 24.0F - phase * 30.0F;
                  float s = 0.5F * (1.0F - phase) + 0.2F;
                  int alpha = Math.round(255.0F * (float)Math.sin(phase * Math.PI));
                  box(entry, vc, xx - s, yx - s, zx - s, xx + s, yx + s, zx + s, alpha << 24 | rgb, 15728880);
               }
            });
            break;
         case "bats":
            for (int i = 0; i < 3; i++) {
               double a = age * 0.06 + i * Math.PI * 2.0 / 3.0;
               float x = (float)Math.cos(a) * 12.0F;
               float z = (float)Math.sin(a) * 12.0F;
               float y = -3.0F + (float)Math.sin(age * 0.09 + i * 2) * 2.5F;
               float tip = (float)Math.sin(age * 0.9 + i * 1.3) * 1.4F;
               matrices.pushPose();
               matrices.translate(x * 0.0625F, y * 0.0625F, z * 0.0625F);
               matrices.mulPose(Axis.YP.rotation((float)(-a)));
               queue.submitCustomGeometry(matrices, RenderTypes.entityCutout(Cosmetics.WHITE), (entry, vc) -> {
                  int body = -15070177;
                  box(entry, vc, -0.6F, -0.6F, -0.9F, 0.6F, 0.6F, 0.9F, body, 15728880);

                  for (int side = -1; side <= 1; side += 2) {
                     float in0 = side * 0.6F;
                     float in1 = side * 2.4F;
                     float out1 = side * 4.2F;
                     box(entry, vc, Math.min(in0, in1), -0.2F + tip * 0.4F, -0.7F, Math.max(in0, in1), 0.15F + tip * 0.4F, 0.7F, body, 15728880);
                     box(entry, vc, Math.min(in1, out1), -0.2F + tip, -0.5F, Math.max(in1, out1), 0.15F + tip, 0.5F, body, 15728880);
                  }
               });
               queue.submitCustomGeometry(matrices, glow, (entry, vc) -> {
                  box(entry, vc, -0.45F, -0.35F, -0.98F, -0.15F, -0.05F, -0.9F, -50373, 15728880);
                  box(entry, vc, 0.15F, -0.35F, -0.98F, 0.45F, -0.05F, -0.9F, -50373, 15728880);
               });
               matrices.popPose();
            }
            break;
         case "souls":
            queue.submitCustomGeometry(
               matrices,
               glow,
               (entry, vc) -> {
                  for (int ix = 0; ix < 10; ix++) {
                     float seed = ix * 12.9898F;
                     float phase = (float)((age * 0.008 + (Math.sin(seed) * 0.5 + 0.5)) % 1.0);
                     double ax = seed * 7.13 + phase * Math.PI * 1.5;
                     float rad = 9.0F - phase * 3.0F;
                     float xx = (float)Math.cos(ax) * rad;
                     float zx = (float)Math.sin(ax) * rad;
                     float yx = 22.0F - phase * 34.0F;
                     float s = 0.7F * (1.0F - phase) + 0.25F;
                     int alpha = Math.round(230.0F * (float)Math.sin(phase * Math.PI));
                     box(entry, vc, xx - s, yx - s, zx - s, xx + s, yx + s, zx + s, alpha << 24 | 14221311, 15728880);
                     box(
                        entry,
                        vc,
                        xx - s * 2.2F,
                        yx - s * 2.2F,
                        zx - s * 2.2F,
                        xx + s * 2.2F,
                        yx + s * 2.2F,
                        zx + s * 2.2F,
                        Math.round(alpha * 0.25F) << 24 | 8386047,
                        15728880
                     );
                  }
               }
            );
      }
   }
}
