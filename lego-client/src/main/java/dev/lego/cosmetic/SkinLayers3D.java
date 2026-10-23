package dev.lego.cosmetic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lego.core.Category;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * 3D skin layers: the second skin layer (hat, jacket, sleeves, trousers) as small cubes that
 * stand out from the body, one cube per pixel, instead of the flat vanilla overlay. Transparent
 * pixels are dropped by the cutout render type, so no pixel data has to be read. Only close to the
 * camera; further away the vanilla layer is drawn.
 */
public final class SkinLayers3D extends Module {
   public static SkinLayers3D instance;

   final Setting.Num distance = this.add(new Setting.Num("Sichtweite", 4.0, 32.0, 2.0, 12.0, " Blöcke"));
   final Setting.Num depth = this.add(new Setting.Num("Tiefe", 0.5, 1.5, 0.1, 1.0, " px"));

   /** Per arm model (0 classic, 1 slim): voxel quads per part (x,y,z ×4, u,v ×4, normal) in model pixels. */
   private static final float[][][][] CACHE = new float[2][][][];
   private static float cachedDepth = -1.0F;

   private SkinLayers3D() {
      super("skinlayers3d", "3D-Skin-Ebenen", "Die zweite Skin-Ebene (Hut, Jacke, Ärmel, Hose) als echte 3D-Würfel", Category.VISUALS);
      this.icon("cube");
      this.enabled = true;
      this.fresh();
   }

   public static void register() {
      instance = Modules.register(new SkinLayers3D());
   }

   /** Whether the 3D layers replace the vanilla overlay for this player right now. */
   public static boolean active(AvatarRenderState s) {
      if (instance == null || !instance.enabled) return false;
      double d = instance.distance.get();
      return s.distanceToCameraSq <= d * d;
   }

   /** Called after the model is posed: hides the flat overlay parts that are drawn in 3D instead. */
   public static void hideFlat(PlayerModel m, AvatarRenderState s) {
      if (!active(s)) return;
      m.hat.visible = false;
      m.jacket.visible = false;
      m.leftSleeve.visible = false;
      m.rightSleeve.visible = false;
      m.leftPants.visible = false;
      m.rightPants.visible = false;
   }

   /** Draws the cubes on the posed model (called from the cosmetics render layer). */
   public static void submit(PoseStack pose, SubmitNodeCollector out, int light, AvatarRenderState s, PlayerModel m) {
      if (!active(s)) return;
      Identifier skin;
      try {
         skin = s.skin.body().texturePath();
      } catch (Throwable e) {
         return;
      }
      float[][][] parts = parts(String.valueOf(s.skin.model()).toLowerCase(java.util.Locale.ROOT).contains("slim") ? 1 : 0, (float)instance.depth.get());
      boolean[] show = {s.showHat, s.showJacket, s.showRightSleeve, s.showLeftSleeve, s.showRightPants, s.showLeftPants};
      ModelPart[] base = {m.head, m.body, m.rightArm, m.leftArm, m.rightLeg, m.leftLeg};
      for (int i = 0; i < 6; i++) {
         if (!show[i] || !base[i].visible) continue;
         float[][] quads = parts[i];
         pose.pushPose();
         base[i].translateAndRotate(pose);
         pose.scale(0.0625F, 0.0625F, 0.0625F);
         out.submitCustomGeometry(pose, RenderTypes.entityCutoutNoCull(skin), (p, vc) -> {
            for (float[] q : quads) quad(vc, p, q, light);
         });
         pose.popPose();
      }
   }

   private static void quad(VertexConsumer vc, Pose p, float[] q, int light) {
      for (int k = 0; k < 4; k++) {
         vc.addVertex(p, q[k * 3], q[k * 3 + 1], q[k * 3 + 2])
            .setColor(-1)
            .setUv(q[12 + k * 2], q[12 + k * 2 + 1])
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(p, q[20], q[21], q[22]);
      }
   }

   private static synchronized float[][][] parts(int slim, float depth) {
      if (cachedDepth != depth) {
         CACHE[0] = null;
         CACHE[1] = null;
         cachedDepth = depth;
      }
      if (CACHE[slim] == null) {
         int aw = slim == 1 ? 3 : 4;
         CACHE[slim] = new float[][][]{
            box(-4, -8, -4, 8, 8, 8, 32, 0, depth),                // hat
            box(-4, 0, -2, 8, 12, 4, 16, 32, depth),               // jacket
            box(slim == 1 ? -2 : -3, -2, -2, aw, 12, 4, 40, 32, depth), // right sleeve
            box(-1, -2, -2, aw, 12, 4, 48, 48, depth),             // left sleeve
            box(-2, 0, -2, 4, 12, 4, 0, 32, depth),                // right trouser leg
            box(-2, 0, -2, 4, 12, 4, 0, 48, depth),                // left trouser leg
         };
      }
      return CACHE[slim];
   }

   /**
    * One cube per overlay pixel of a box (model space: y down, front towards -z, player's right
    * towards -x) with the usual skin net at (u, v): top, bottom, right, front, left, back.
    */
   static float[][] box(float x0, float y0, float z0, int w, int h, int d, int u, int v, float t) {
      List<float[]> q = new ArrayList<>();
      float x1 = x0 + w, y1 = y0 + h, z1 = z0 + d;
      // front (z0): texture (u+d+i, v+d+j), x from x0
      for (int i = 0; i < w; i++) for (int j = 0; j < h; j++) cube(q, x0 + i, y0 + j, z0 - t, 1, 1, t, u + d + i, v + d + j);
      // back (z1): (u+2d+w+i, v+d+j), x from x1 backwards
      for (int i = 0; i < w; i++) for (int j = 0; j < h; j++) cube(q, x1 - i - 1, y0 + j, z1, 1, 1, t, u + 2 * d + w + i, v + d + j);
      // right side (x0): (u+i, v+d+j), column d-1 next to the front
      for (int i = 0; i < d; i++) for (int j = 0; j < h; j++) cube(q, x0 - t, y0 + j, z1 - i - 1, t, 1, 1, u + i, v + d + j);
      // left side (x1): (u+d+w+i, v+d+j), column 0 next to the front
      for (int i = 0; i < d; i++) for (int j = 0; j < h; j++) cube(q, x1, y0 + j, z0 + i, t, 1, 1, u + d + w + i, v + d + j);
      // top (y0): (u+d+i, v+j), row d-1 next to the front
      for (int i = 0; i < w; i++) for (int j = 0; j < d; j++) cube(q, x0 + i, y0 - t, z1 - j - 1, 1, t, 1, u + d + i, v + j);
      // bottom (y1): (u+d+w+i, v+j)
      for (int i = 0; i < w; i++) for (int j = 0; j < d; j++) cube(q, x0 + i, y1, z0 + j, 1, t, 1, u + d + w + i, v + j);
      return q.toArray(new float[0][]);
   }

   /** Six faces of a cube at (x,y,z) with size (sx,sy,sz), all showing skin pixel (pu, pv). */
   private static void cube(List<float[]> q, float x, float y, float z, float sx, float sy, float sz, int pu, int pv) {
      float u0 = (pu + 0.05F) / 64.0F, v0 = (pv + 0.05F) / 64.0F, u1 = (pu + 0.95F) / 64.0F, v1 = (pv + 0.95F) / 64.0F;
      float X = x + sx, Y = y + sy, Z = z + sz;
      face(q, x, y, z, X, y, z, X, Y, z, x, Y, z, u0, v0, u1, v1, 0, 0, -1);  // -z
      face(q, X, y, Z, x, y, Z, x, Y, Z, X, Y, Z, u0, v0, u1, v1, 0, 0, 1);   // +z
      face(q, x, y, Z, x, y, z, x, Y, z, x, Y, Z, u0, v0, u1, v1, -1, 0, 0);  // -x
      face(q, X, y, z, X, y, Z, X, Y, Z, X, Y, z, u0, v0, u1, v1, 1, 0, 0);   // +x
      face(q, x, y, Z, X, y, Z, X, y, z, x, y, z, u0, v0, u1, v1, 0, -1, 0);  // -y (up)
      face(q, x, Y, z, X, Y, z, X, Y, Z, x, Y, Z, u0, v0, u1, v1, 0, 1, 0);   // +y (down)
   }

   private static void face(List<float[]> q, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz, float dx, float dy, float dz,
                            float u0, float v0, float u1, float v1, float nx, float ny, float nz) {
      q.add(new float[]{ax, ay, az, bx, by, bz, cx, cy, cz, dx, dy, dz, u0, v0, u1, v0, u1, v1, u0, v1, nx, ny, nz});
   }
}
