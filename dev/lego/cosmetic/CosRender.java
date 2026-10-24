package dev.lego.cosmetic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import dev.lego.LegoClient;
import dev.lego.core.Mc;
import dev.lego.net.LegoSync;
import dev.lego.perf.Perf;
import dev.lego.ui.Gx;
import dev.lego.ui.McBackend;
import dev.lego.ui.Tx;
import dev.lego.visual.R3;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class CosRender {
   private static final Map<String, Identifier> TEX = new HashMap<>();
   private static final ExecutorService PAINT = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "Lego-Cosmetics");
      t.setDaemon(true);
      t.setPriority(4);
      return t;
   });
   private static final Set<String> QUEUED = ConcurrentHashMap.newKeySet();
   static Identifier skin;
   private static final Map<String, Integer> MODE = new HashMap<>();
   private static String capeBase;
   private static Identifier lastCape;
   private static int warmed = -1;
   private static final CosRender.Built[] BUILT = new CosRender.Built[2];
   private static int flagsKey;
   private static Method stateOf;

   private CosRender() {
   }

   private static String idName(String name) {
      return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_./-]", "_");
   }

   static int mode(String name) {
      Integer m = MODE.get(name);
      if (m != null) {
         return m;
      } else if (name.equals("@skin")) {
         MODE.put(name, 0);
         return 0;
      } else {
         CTex.T t = CTex.peek(name);
         if (t == null) {
            return 0;
         } else {
            int soft = 0;

            for (int i = 0; i < t.argb.length; i += 3) {
               int a = t.argb[i] >>> 24;
               if (a > 12 && a < 235) {
                  soft++;
               }
            }

            m = soft > t.argb.length / 3 * 0.04 ? 1 : 0;
            MODE.put(name, m);
            return m;
         }
      }
   }

   public static Identifier texture(String name) {
      if (name.equals("@skin")) {
         return skin;
      } else {
         Identifier id = TEX.get(name);
         if (id != null) {
            return id;
         } else {
            CTex.T t = CTex.peek(name);
            if (t == null) {
               if (QUEUED.add(name)) {
                  PAINT.execute(() -> {
                     try {
                        CTex.get(name);
                     } catch (Throwable var2x) {
                        LegoClient.LOG("Kosmetik-Textur " + name + ": " + var2x);
                     }
                  });
               }

               return null;
            } else {
               QUEUED.remove(name);
               id = Identifier.fromNamespaceAndPath("legoclient", "cosmetic/" + idName(name));

               try {
                  Tx.register(id, t.argb, t.w, t.h, true);
               } catch (Throwable var4) {
                  LegoClient.LOG("Kosmetik-Textur " + name + ": " + var4);
               }

               TEX.put(name, id);
               return id;
            }
         }
      }
   }

   public static Identifier capeTexture() {
      Cos.Item c = Cos.equipped(Cos.Slot.CAPE);
      String base = c == null ? null : c.cape;
      if (!Objects.equals(base, capeBase)) {
         if (capeBase != null) {
            Iterator<Entry<String, Identifier>> it = TEX.entrySet().iterator();

            while (it.hasNext()) {
               Entry<String, Identifier> e = it.next();
               if (e.getKey().equals(capeBase) || e.getKey().startsWith(capeBase + "#")) {
                  Tx.destroy(e.getValue());
                  it.remove();
               }
            }
         }

         capeBase = base;
         lastCape = null;
         if (base != null) {
            for (int k = 0; k < Capes.frames(base); k++) {
               texture(k == 0 ? base : base + "#" + k);
            }
         }
      }

      if (base == null) {
         return null;
      } else {
         if (base.equals("cape_spotify") || base.equals("spotify")) {
            Identifier live = SpotifyCape.texture();
            if (live != null) {
               return live;
            }
         }

         Identifier t = texture(Capes.frameName(base, (float)System.currentTimeMillis() / 1000.0F));
         if (t != null) {
            lastCape = t;
         }

         return lastCape;
      }
   }

   public static void prewarm() {
      if (warmed != Cos.version) {
         warmed = Cos.version;
         Thread t = new Thread(() -> {
            try {
               Set<String> names = new LinkedHashSet<>();
               G g = new G((tex, p, uv, argb, gl, nx, ny, nz) -> names.add(tex));
               Cos.A a = new Cos.A();
               Cos.renderEquipped(g, a, true);
               Cos.renderEquipped(g, a, false);
               Cos.Item c = Cos.equipped(Cos.Slot.CAPE);
               if (c != null && c.cape != null) {
                  names.add(c.cape);
               }

               for (String n : names) {
                  CTex.get(n);
               }
            } catch (Throwable var6) {
               LegoClient.LOG("Kosmetik vorbereiten: " + var6);
            }
         }, "Lego-Cosmetics");
         t.setDaemon(true);
         t.start();
      }
   }

   public static RenderLayer feature(RenderLayerParent ctx) {
      return new CosRender.Feature(ctx);
   }

   private static void drawOther(PoseStack matrices, SubmitNodeCollector queue, int light, AvatarRenderState state, PlayerModel model) {
      if (Mc.world() != null) {
         Entity other = Mc.world().getEntity(state.id);
         if (other != null) {
            List<String> ids = LegoSync.cosmetics(other.getUUID());
            if (!ids.isEmpty()) {
               Cos.A anim = new Cos.A();
               anim.time = state.ageInTicks / 20.0F;
               anim.sneak = state.isCrouching;
               anim.move = Math.min(1.0F, state.walkAnimationSpeed * 1.4F);

               for (int pass = 0; pass < 2; pass++) {
                  boolean head = pass == 1;
                  Map<String, List<CosRender.Q>> normal = new LinkedHashMap<>();
                  Map<String, List<CosRender.Q>> glow = new LinkedHashMap<>();
                  G g = new G(
                     (tex, p, uv, argb, gl, nx, ny, nz) -> (gl ? glow : normal)
                        .computeIfAbsent(tex, k -> new ArrayList<>())
                        .add(new CosRender.Q(p, uv, argb, nx, ny, nz))
                  );
                  Cos.renderList(g, anim, head, ids);
                  if (!normal.isEmpty() || !glow.isEmpty()) {
                     matrices.pushPose();
                     (head ? model.head : model.body).translateAndRotate(matrices);
                     matrices.scale(-0.0625F, -0.0625F, -0.0625F);
                     submit(matrices, queue, normal, false, light);
                     submit(matrices, queue, glow, true, light);
                     matrices.popPose();
                  }
               }
            }
         }
      }
   }

   private static void draw(PoseStack matrices, SubmitNodeCollector queue, int light, ModelPart part, Cos.A anim, boolean head) {
      long now = System.currentTimeMillis();
      int mq = Math.round(anim.move * 10.0F);
      CosRender.Built b = BUILT[head ? 1 : 0];
      if (b == null || now - b.t > 33L || b.ver != Cos.version || b.sneak != anim.sneak || b.mq != mq || b.flags != flagsKey) {
         Map<String, List<CosRender.Q>> nrm = new LinkedHashMap<>();
         Map<String, List<CosRender.Q>> glw = new LinkedHashMap<>();
         G g = new G(
            (tex, p, uv, argb, gl, nx, ny, nz) -> (gl ? glw : nrm).computeIfAbsent(tex, k -> new ArrayList<>()).add(new CosRender.Q(p, uv, argb, nx, ny, nz))
         );

         try {
            Cos.renderEquipped(g, anim, head);
         } finally {
            Cos.slotTransform = head ? null : Cos.slotTransform;
         }

         smooth(nrm);
         b = new CosRender.Built();
         b.t = now;
         b.ver = Cos.version;
         b.mq = mq;
         b.flags = flagsKey;
         b.sneak = anim.sneak;
         b.normal = nrm;
         b.glow = glw;
         BUILT[head ? 1 : 0] = b;
      }

      Map<String, List<CosRender.Q>> normal = b.normal;
      Map<String, List<CosRender.Q>> glow = b.glow;
      if (!normal.isEmpty() || !glow.isEmpty()) {
         matrices.pushPose();
         part.translateAndRotate(matrices);
         matrices.scale(-0.0625F, -0.0625F, -0.0625F);
         submit(matrices, queue, normal, false, light);
         submit(matrices, queue, glow, true, light);
         matrices.popPose();
      }
   }

   private static void smooth(Map<String, List<CosRender.Q>> quads) {
      int n = 0;

      for (List<CosRender.Q> l : quads.values()) {
         n += l.size();
      }

      if (n != 0 && n <= 20000) {
         float[][] P = new float[n][];
         float[][] N = new float[n][];
         int[] grp = new int[n];
         CosRender.Q[] all = new CosRender.Q[n];
         int i = 0;

         for (Entry<String, List<CosRender.Q>> e : quads.entrySet()) {
            int h = Smooth.group(e.getKey(), false);

            for (CosRender.Q q : e.getValue()) {
               all[i] = q;
               P[i] = q.p;
               N[i] = q.n;
               grp[i] = h;
               i++;
            }
         }

         float[] vn = Smooth.normals(P, N, grp, n);

         for (int k = 0; k < n; k++) {
            all[k].vn = Arrays.copyOfRange(vn, k * 12, k * 12 + 12);
         }
      }
   }

   private static void submit(PoseStack matrices, SubmitNodeCollector queue, Map<String, List<CosRender.Q>> quads, boolean glow, int light) {
      for (Entry<String, List<CosRender.Q>> e : quads.entrySet()) {
         Identifier tex = texture(e.getKey());
         if (tex != null) {
            int md = glow ? 2 : mode(e.getKey());
            RenderType layer = md == 2
               ? RenderTypes.entityTranslucentEmissive(tex)
               : (md == 1 ? RenderTypes.entityTranslucent(tex) : RenderTypes.entityCutoutNoCull(tex));
            List<CosRender.Q> list = e.getValue();
            int li = glow ? 15728880 : light;
            queue.submitCustomGeometry(matrices, layer, (entry, vc) -> {
               for (CosRender.Q q : list) {
                  for (int k = 0; k < 4; k++) {
                     v(vc, entry, q, k, li);
                  }
               }
            });
         }
      }
   }

   private static void v(VertexConsumer vc, Pose entry, CosRender.Q q, int k, int light) {
      vc.addVertex(entry, q.p[k * 3], q.p[k * 3 + 1], q.p[k * 3 + 2])
         .setColor(q.argb)
         .setUv(q.uv[k * 2], q.uv[k * 2 + 1])
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(light)
         .setNormal(entry, q.vn != null ? q.vn[k * 3] : q.n[0], q.vn != null ? q.vn[k * 3 + 1] : q.n[1], q.vn != null ? q.vn[k * 3 + 2] : q.n[2]);
   }

   public static void drawWorld(Cos.Model model, Cos.A a, double x, double y, double z, float yawDeg, float pitchDeg, float rollDeg, float scale, int light) {
      double th = Math.toRadians(yawDeg);
      double s = scale / 16.0;
      double fx = -Math.sin(th);
      double fz = Math.cos(th);
      double rx = -Math.cos(th);
      double rz = -Math.sin(th);
      double cp = Math.cos(Math.toRadians(pitchDeg));
      double sp = Math.sin(Math.toRadians(pitchDeg));
      double cr = Math.cos(Math.toRadians(rollDeg));
      double sr = Math.sin(Math.toRadians(rollDeg));
      Map<String, List<Object[]>> groups = new LinkedHashMap<>();
      G g = new G((tex, p, uv, argb, glowx, nx, ny, nz) -> {
         double[] w = new double[12];

         for (int k = 0; k < 4; k++) {
            double[] qx = local(p[k * 3], p[k * 3 + 1], p[k * 3 + 2], cp, sp, cr, sr);
            w[k * 3] = x + (qx[0] * rx + qx[2] * fx) * s;
            w[k * 3 + 1] = y + qx[1] * s;
            w[k * 3 + 2] = z + (qx[0] * rz + qx[2] * fz) * s;
         }

         double[] n = local(nx, ny, nz, cp, sp, cr, sr);
         float[] wnx = new float[]{(float)(n[0] * rx + n[2] * fx), (float)n[1], (float)(n[0] * rz + n[2] * fz)};
         groups.computeIfAbsent((glowx ? "!" : "") + tex, k -> new ArrayList<>()).add(new Object[]{w, uv, argb, wnx});
      });

      try {
         model.render(g, a);
      } catch (Throwable var44) {
         if (LegoClient.DEBUG) {
            LegoClient.LOG("Modell: " + var44);
         }
      }

      for (Entry<String, List<Object[]>> e : groups.entrySet()) {
         boolean glow = e.getKey().startsWith("!");
         String name = glow ? e.getKey().substring(1) : e.getKey();
         Identifier id = texture(name);
         if (id != null) {
            int md = glow ? 2 : mode(name);

            for (Object[] q : e.getValue()) {
               float[] wn = (float[])q[3];
               R3.texQuad(id, md, (double[])q[0], (float[])q[1], (Integer)q[2], light, wn[0], wn[1], wn[2]);
            }
         }
      }
   }

   private static double[] local(double x, double y, double z, double cp, double sp, double cr, double sr) {
      double y1 = y * cp + z * sp;
      double z1 = -y * sp + z * cp;
      double x2 = x * cr - y1 * sr;
      double y2 = x * sr + y1 * cr;
      return new double[]{x2, y2, z1};
   }

   public static int light(double x, double y, double z) {
      try {
         return LevelRenderer.getLightColor(Mc.world(), BlockPos.containing(x, y, z));
      } catch (Throwable var7) {
         return 15728880;
      }
   }

   public static boolean preview(int x, int y, int w, int h, float yaw, float pitch) {
      LocalPlayer p = Mc.player();
      if (p != null && Gx.B instanceof McBackend) {
         try {
            if (stateOf == null) {
               stateOf = InventoryScreen.class.getDeclaredMethod("method_48472", LivingEntity.class);
               stateOf.setAccessible(true);
            }

            EntityRenderState st = (EntityRenderState)stateOf.invoke(null, p);
            if (st instanceof LivingEntityRenderState ls) {
               ls.bodyRot = 180.0F + yaw;
               ls.yRot = 0.0F;
               ls.xRot = pitch;
            }

            GuiGraphics ctx = ((McBackend)Gx.B).ctx();
            int S = Gx.S;
            int x1 = x / S;
            int y1 = y / S;
            int x2 = (x + w) / S;
            int y2 = (y + h) / S;
            float size = Math.min((x2 - x1) * 0.9F, (y2 - y1) / Math.max(1.2F, st.boundingBoxHeight * 1.15F));
            Gx.push();
            Gx.B.scale(S, S);
            Quaternionf rot = new Quaternionf().rotateZ((float) Math.PI);
            Quaternionf cam = new Quaternionf();
            ctx.submitEntityRenderState(st, size, new Vector3f(0.0F, st.boundingBoxHeight / 2.0F + 0.0625F, 0.0F), rot, cam, x1, y1, x2, y2);
            Gx.pop();
            return true;
         } catch (Throwable var17) {
            if (LegoClient.DEBUG) {
               LegoClient.LOG("Vorschau: " + var17);
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private static final class Built {
      long t;
      int ver;
      int mq;
      int flags;
      boolean sneak;
      Map<String, List<CosRender.Q>> normal;
      Map<String, List<CosRender.Q>> glow;
   }

   static final class Feature extends RenderLayer<AvatarRenderState, PlayerModel> {
      Feature(RenderLayerParent<AvatarRenderState, PlayerModel> ctx) {
         super(ctx);
      }

      public void method_4199(PoseStack matrices, SubmitNodeCollector queue, int light, AvatarRenderState state, float a1, float a2) {
         Perf.begin("Kosmetik");

         try {
            LocalPlayer me = Mc.player();
            if (me == null) {
               return;
            }

            if (state.id != Mc.id(me)) {
               CosRender.drawOther(matrices, queue, light, state, this.getParentModel());
               return;
            }

            if (!Cos.anyEquipped()) {
               return;
            }

            if (!me.isInvisible()) {
               Cos.A anim = new Cos.A();
               anim.time = state.ageInTicks / 20.0F;
               anim.sneak = state.isCrouching;
               anim.move = Math.min(1.0F, state.walkAnimationSpeed * 1.4F);

               try {
                  CosRender.skin = state.skin.body().texturePath();
               } catch (Throwable var17) {
               }

               boolean helmet = !Mc.empty(Mc.armor(39));
               boolean chest = !Mc.empty(Mc.armor(38));
               boolean legs = !Mc.empty(Mc.armor(37));
               Cos.slotTransform = (g, slot) -> {
                  switch (slot) {
                     case HAT:
                        if (helmet) {
                           g.translate(0.0F, 0.9F, 0.0F);
                           g.scale(1.1F);
                        }
                        break;
                     case FACE:
                        if (helmet) {
                           g.translate(0.0F, 0.0F, 0.9F);
                        }
                        break;
                     case BACK:
                     case WINGS:
                        if (chest) {
                           g.translate(0.0F, 0.0F, -1.1F);
                        }
                        break;
                     case AURA:
                        if (chest || legs) {
                           g.scale(1.08F, 1.0F, 1.08F);
                        }
                  }
               };
               CosRender.flagsKey = (helmet ? 1 : 0) | (chest ? 2 : 0) | (legs ? 4 : 0);
               PlayerModel model = this.getParentModel();
               CosRender.draw(matrices, queue, light, model.body, anim, false);
               CosRender.draw(matrices, queue, light, model.head, anim, true);
               return;
            }
         } catch (Throwable var18) {
            if (LegoClient.DEBUG) {
               LegoClient.LOG("Kosmetik-Render: " + var18);
            }

            return;
         } finally {
            Perf.end("Kosmetik");
         }
      }
   }

   private static final class Q {
      final float[] p;
      final float[] uv;
      final float[] n;
      float[] vn;
      final int argb;

      Q(float[] p, float[] uv, int argb, float nx, float ny, float nz) {
         this.p = p;
         this.uv = uv;
         this.argb = argb;
         this.n = new float[]{nx, ny, nz};
      }
   }
}
