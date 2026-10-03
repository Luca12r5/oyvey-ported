package dev.lego.cosmetic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import dev.lego.LegoClient;
import dev.lego.core.Mc;
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
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class CosRender {
   private static final Map<String, Identifier> TEX = new HashMap<>();
   private static final ExecutorService PAINT = Executors.newSingleThreadExecutor(var0 -> {
      Thread var1 = new Thread(var0, "Lego-Cosmetics");
      var1.setDaemon(true);
      var1.setPriority(4);
      return var1;
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

   private static String idName(String var0) {
      return var0.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_./-]", "_");
   }

   static int mode(String var0) {
      Integer var1 = MODE.get(var0);
      if (var1 != null) {
         return var1;
      } else if (var0.equals("@skin")) {
         MODE.put(var0, 0);
         return 0;
      } else {
         CTex.T var2 = CTex.peek(var0);
         if (var2 == null) {
            return 0;
         } else {
            int var3 = 0;

            for (byte var4 = 0; var4 < var2.argb.length; var4 += 3) {
               int var5 = var2.argb[var4] >>> 24;
               if (var5 > 12 && var5 < 235) {
                  var3++;
               }
            }

            var1 = var3 > var2.argb.length / 3 * 0.04 ? 1 : 0;
            MODE.put(var0, var1);
            return var1;
         }
      }
   }

   public static Identifier texture(String var0) {
      if (var0.equals("@skin")) {
         return skin;
      } else {
         Identifier var1 = TEX.get(var0);
         if (var1 != null) {
            return var1;
         } else {
            CTex.T var2 = CTex.peek(var0);
            if (var2 == null) {
               if (QUEUED.add(var0)) {
                  PAINT.execute(() -> {
                     try {
                        CTex.get(var0);
                     } catch (Throwable var2x) {
                        LegoClient.LOG("Kosmetik-Textur " + var0 + ": " + var2x);
                     }
                  });
               }

               return null;
            } else {
               QUEUED.remove(var0);
               var1 = Identifier.fromNamespaceAndPath("legoclient", "cosmetic/" + idName(var0));

               try {
                  Tx.register(var1, var2.argb, var2.w, var2.h, true);
               } catch (Throwable var4) {
                  LegoClient.LOG("Kosmetik-Textur " + var0 + ": " + var4);
               }

               TEX.put(var0, var1);
               return var1;
            }
         }
      }
   }

   /** Animated cape texture for any cape id (used for other LEGO players). */
   public static Identifier capeTextureFor(String cape) {
      if (cape == null) return null;
      if (cape.equals("cape_spotify") || cape.equals("spotify")) return null;
      return texture(Capes.frameName(cape, (float)System.currentTimeMillis() / 1000.0F));
   }

   private static final java.util.Map<java.util.UUID, CosRender.Built[]> REMOTE = new java.util.concurrent.ConcurrentHashMap<>();

   /** Builds (cached ~33 ms) and submits the geometry of another player's equipped items. */
   static void drawRemote(java.util.UUID id, java.util.Map<Cos.Slot, String> items, PoseStack var0, SubmitNodeCollector var1, int var2, ModelPart var3, Cos.A var4, boolean head) {
      long now = System.currentTimeMillis();
      CosRender.Built[] slot = REMOTE.computeIfAbsent(id, k -> new CosRender.Built[2]);
      int mq = Math.round(var4.move * 10.0F);
      int ver = items.hashCode();
      CosRender.Built b = slot[head ? 1 : 0];
      if (b == null || now - b.t > 33L || b.ver != ver || b.sneak != var4.sneak || b.mq != mq) {
         LinkedHashMap<String, List<CosRender.Q>> normal = new LinkedHashMap<>();
         LinkedHashMap<String, List<CosRender.Q>> glow = new LinkedHashMap<>();
         G g = new G((tex, p, uv, argb, glowing, n0, n1, n2) -> (glowing ? glow : normal).computeIfAbsent(tex, k -> new ArrayList<>()).add(new CosRender.Q(p, uv, argb, n0, n1, n2)));
         Cos.renderItems(g, var4, head, items);
         smooth(normal);
         b = new CosRender.Built();
         b.t = now;
         b.ver = ver;
         b.mq = mq;
         b.sneak = var4.sneak;
         b.normal = normal;
         b.glow = glow;
         slot[head ? 1 : 0] = b;
      }
      if (!b.normal.isEmpty() || !b.glow.isEmpty()) {
         var0.pushPose();
         var3.translateAndRotate(var0);
         var0.scale(-0.0625F, -0.0625F, -0.0625F);
         submit(var0, var1, b.normal, false, var2);
         submit(var0, var1, b.glow, true, var2);
         var0.popPose();
      }
      if (REMOTE.size() > 256) REMOTE.clear();
   }

   public static Identifier capeTexture() {
      Cos.Item var0 = Cos.equipped(Cos.Slot.CAPE);
      String var1 = var0 == null ? null : var0.cape;
      if (!Objects.equals(var1, capeBase)) {
         if (capeBase != null) {
            Iterator var2 = TEX.entrySet().iterator();

            while (var2.hasNext()) {
               Entry var3 = (Entry)var2.next();
               if (((String)var3.getKey()).equals(capeBase) || ((String)var3.getKey()).startsWith(capeBase + "#")) {
                  Tx.destroy((Identifier)var3.getValue());
                  var2.remove();
               }
            }
         }

         capeBase = var1;
         lastCape = null;
         if (var1 != null) {
            for (int var4 = 0; var4 < Capes.frames(var1); var4++) {
               texture(var4 == 0 ? var1 : var1 + "#" + var4);
            }
         }
      }

      if (var1 == null) {
         return null;
      } else {
         if (var1.equals("cape_spotify") || var1.equals("spotify")) {
            Identifier var5 = SpotifyCape.texture();
            if (var5 != null) {
               return var5;
            }
         }

         Identifier var6 = texture(Capes.frameName(var1, (float)System.currentTimeMillis() / 1000.0F));
         if (var6 != null) {
            lastCape = var6;
         }

         return lastCape;
      }
   }

   public static void prewarm() {
      if (warmed != Cos.version) {
         warmed = Cos.version;
         Thread var0 = new Thread(() -> {
            try {
               LinkedHashSet var0x = new LinkedHashSet();
               G var1 = new G((var1x, var2x, var3x, var4, var5x, var6x, var7, var8) -> var0x.add(var1x));
               Cos.A var2 = new Cos.A();
               Cos.renderEquipped(var1, var2, true);
               Cos.renderEquipped(var1, var2, false);
               Cos.Item var3 = Cos.equipped(Cos.Slot.CAPE);
               if (var3 != null && var3.cape != null) {
                  var0x.add(var3.cape);
               }

               for (String var5 : (Iterable<String>) (Iterable<?>) (var0x)) {
                  CTex.get(var5);
               }
            } catch (Throwable var6) {
               LegoClient.LOG("Kosmetik vorbereiten: " + var6);
            }
         }, "Lego-Cosmetics");
         var0.setDaemon(true);
         var0.start();
      }
   }

   public static RenderLayer feature(RenderLayerParent var0) {
      return new CosRender.Feature(var0);
   }

   private static void draw(PoseStack var0, SubmitNodeCollector var1, int var2, ModelPart var3, Cos.A var4, boolean var5) {
      long var6 = System.currentTimeMillis();
      int var8 = Math.round(var4.move * 10.0F);
      CosRender.Built var9 = BUILT[var5 ? 1 : 0];
      if (var9 == null || var6 - var9.t > 33L || var9.ver != Cos.version || var9.sneak != var4.sneak || var9.mq != var8 || var9.flags != flagsKey) {
         LinkedHashMap<String, List<CosRender.Q>> var10 = new LinkedHashMap<>();
         LinkedHashMap<String, List<CosRender.Q>> var11 = new LinkedHashMap<>();
         G var12 = new G(
            (var2x, var3x, var4x, var5x, var6x, var7, var8x, var9x) -> (var6x ? var11 : var10)
               .computeIfAbsent(var2x, var0xx -> new ArrayList<>())
               .add(new CosRender.Q(var3x, var4x, var5x, var7, var8x, var9x))
         );

         try {
            Cos.renderEquipped(var12, var4, var5);
         } finally {
            Cos.slotTransform = var5 ? null : Cos.slotTransform;
         }

         smooth(var10);
         var9 = new CosRender.Built();
         var9.t = var6;
         var9.ver = Cos.version;
         var9.mq = var8;
         var9.flags = flagsKey;
         var9.sneak = var4.sneak;
         var9.normal = var10;
         var9.glow = var11;
         BUILT[var5 ? 1 : 0] = var9;
      }

      Map var16 = var9.normal;
      Map var17 = var9.glow;
      if (!var16.isEmpty() || !var17.isEmpty()) {
         var0.pushPose();
         var3.translateAndRotate(var0);
         var0.scale(-0.0625F, -0.0625F, -0.0625F);
         submit(var0, var1, var16, false, var2);
         submit(var0, var1, var17, true, var2);
         var0.popPose();
      }
   }

   private static void smooth(Map<String, List<CosRender.Q>> var0) {
      int var1 = 0;

      for (List var3 : var0.values()) {
         var1 += var3.size();
      }

      if (var1 != 0 && var1 <= 20000) {
         float[][] var12 = new float[var1][];
         float[][] var13 = new float[var1][];
         int[] var4 = new int[var1];
         CosRender.Q[] var5 = new CosRender.Q[var1];
         int var6 = 0;

         for (Entry var8 : var0.entrySet()) {
            int var9 = Smooth.group((String)var8.getKey(), false);

            for (CosRender.Q var11 : (Iterable<CosRender.Q>) (Iterable<?>) ((List)var8.getValue())) {
               var5[var6] = var11;
               var12[var6] = var11.p;
               var13[var6] = var11.n;
               var4[var6] = var9;
               var6++;
            }
         }

         float[] var14 = Smooth.normals(var12, var13, var4, var1);

         for (int var15 = 0; var15 < var1; var15++) {
            var5[var15].vn = Arrays.copyOfRange(var14, var15 * 12, var15 * 12 + 12);
         }
      }
   }

   private static void submit(PoseStack var0, SubmitNodeCollector var1, Map<String, List<CosRender.Q>> var2, boolean var3, int var4) {
      for (Entry var6 : var2.entrySet()) {
         Identifier var7 = texture((String)var6.getKey());
         if (var7 != null) {
            int var8 = var3 ? 2 : mode((String)var6.getKey());
            RenderType var9 = var8 == 2
               ? RenderTypes.entityTranslucentEmissive(var7)
               : (var8 == 1 ? RenderTypes.entityTranslucent(var7) : RenderTypes.entityCutoutNoCull(var7));
            List var10 = (List)var6.getValue();
            int var11 = var3 ? 15728880 : var4;
            var1.submitCustomGeometry(var0, var9, (var2x, var3x) -> {
               for (CosRender.Q var5 : (Iterable<CosRender.Q>) (Iterable<?>) (var10)) {
                  for (int var6x = 0; var6x < 4; var6x++) {
                     v(var3x, var2x, var5, var6x, var11);
                  }
               }
            });
         }
      }
   }

   private static void v(VertexConsumer var0, Pose var1, CosRender.Q var2, int var3, int var4) {
      var0.addVertex(var1, var2.p[var3 * 3], var2.p[var3 * 3 + 1], var2.p[var3 * 3 + 2])
         .setColor(var2.argb)
         .setUv(var2.uv[var3 * 2], var2.uv[var3 * 2 + 1])
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(var4)
         .setNormal(
            var1,
            var2.vn != null ? var2.vn[var3 * 3] : var2.n[0],
            var2.vn != null ? var2.vn[var3 * 3 + 1] : var2.n[1],
            var2.vn != null ? var2.vn[var3 * 3 + 2] : var2.n[2]
         );
   }

   public static void drawWorld(Cos.Model var0, Cos.A var1, double var2, double var4, double var6, float var8, float var9, float var10, float var11, int var12) {
      double var13 = Math.toRadians(var8);
      double var15 = var11 / 16.0;
      double var17 = -Math.sin(var13);
      double var19 = Math.cos(var13);
      double var21 = -Math.cos(var13);
      double var23 = -Math.sin(var13);
      double var25 = Math.cos(Math.toRadians(var9));
      double var27 = Math.sin(Math.toRadians(var9));
      double var29 = Math.cos(Math.toRadians(var10));
      double var31 = Math.sin(Math.toRadians(var10));
      LinkedHashMap<String, List<Object[]>> var33 = new LinkedHashMap<>();
      G var34 = new G((var25x, var26, var27x, var28, var29x, var30, var31x, var32) -> {
         double[] var33x = new double[12];

         for (int var34x = 0; var34x < 4; var34x++) {
            double[] var35 = local(var26[var34x * 3], var26[var34x * 3 + 1], var26[var34x * 3 + 2], var25, var27, var29, var31);
            var33x[var34x * 3] = var2 + (var35[0] * var21 + var35[2] * var17) * var15;
            var33x[var34x * 3 + 1] = var4 + var35[1] * var15;
            var33x[var34x * 3 + 2] = var6 + (var35[0] * var23 + var35[2] * var19) * var15;
         }

         double[] var36x = local(var30, var31x, var32, var25, var27, var29, var31);
         float[] var37x = new float[]{(float)(var36x[0] * var21 + var36x[2] * var17), (float)var36x[1], (float)(var36x[0] * var23 + var36x[2] * var19)};
         var33.computeIfAbsent((var29x ? "!" : "") + var25x, var0xx -> new ArrayList<>()).add(new Object[]{var33x, var27x, var28, var37x});
      });

      try {
         var0.render(var34, var1);
      } catch (Throwable var44) {
         if (LegoClient.DEBUG) {
            LegoClient.LOG("Modell: " + var44);
         }
      }

      for (Entry var36 : (Iterable<Entry>) (Iterable<?>) (var33.entrySet())) {
         boolean var37 = ((String)var36.getKey()).startsWith("!");
         String var38 = var37 ? ((String)var36.getKey()).substring(1) : (String)var36.getKey();
         Identifier var39 = texture(var38);
         if (var39 != null) {
            int var40 = var37 ? 2 : mode(var38);

            for (Object[] var42 : (Iterable<Object[]>) (Iterable<?>) ((List)var36.getValue())) {
               float[] var43 = (float[])var42[3];
               R3.texQuad(var39, var40, (double[])var42[0], (float[])var42[1], (Integer)var42[2], var12, var43[0], var43[1], var43[2]);
            }
         }
      }
   }

   private static double[] local(double var0, double var2, double var4, double var6, double var8, double var10, double var12) {
      double var14 = var2 * var6 + var4 * var8;
      double var16 = -var2 * var8 + var4 * var6;
      double var18 = var0 * var10 - var14 * var12;
      double var20 = var0 * var12 + var14 * var10;
      return new double[]{var18, var20, var16};
   }

   public static int light(double var0, double var2, double var4) {
      try {
         return LevelRenderer.getLightColor(Mc.world(), BlockPos.containing(var0, var2, var4));
      } catch (Throwable var7) {
         return 15728880;
      }
   }

   public static boolean preview(int var0, int var1, int var2, int var3, float var4, float var5) {
      LocalPlayer var6 = Mc.player();
      if (var6 != null && Gx.B instanceof McBackend) {
         try {
            if (stateOf == null) {
               stateOf = InventoryScreen.class.getDeclaredMethod(dev.lego.util.Remap.method("net.minecraft.class_490", "method_48472", "(Lnet/minecraft/class_1309;)Lnet/minecraft/class_10017;"), LivingEntity.class);
               stateOf.setAccessible(true);
            }

            EntityRenderState var7 = (EntityRenderState)stateOf.invoke(null, var6);
            if (var7 instanceof LivingEntityRenderState var8) {
               var8.bodyRot = 180.0F + var4;
               var8.yRot = 0.0F;
               var8.xRot = var5;
            }

            GuiGraphics var18 = ((McBackend)Gx.B).ctx();
            int var9 = Gx.S;
            int var10 = var0 / var9;
            int var11 = var1 / var9;
            int var12 = (var0 + var2) / var9;
            int var13 = (var1 + var3) / var9;
            float var14 = Math.min((var12 - var10) * 0.9F, (var13 - var11) / Math.max(1.2F, var7.boundingBoxHeight * 1.15F));
            Gx.push();
            Gx.B.scale(var9, var9);
            Quaternionf var15 = new Quaternionf().rotateZ((float) Math.PI);
            Quaternionf var16 = new Quaternionf();
            var18.submitEntityRenderState(
               var7, var14, new Vector3f(0.0F, var7.boundingBoxHeight / 2.0F + 0.0625F, 0.0F), var15, var16, var10, var11, var12, var13
            );
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
      Feature(RenderLayerParent<AvatarRenderState, PlayerModel> var1) {
         super(var1);
      }

      /** Other players: render their LEGO items if the backend reported any. */
      private void remote(PoseStack pose, SubmitNodeCollector out, int light, AvatarRenderState state) {
         if (Mc.mc().level == null || !(Mc.mc().level.getEntity(state.id) instanceof net.minecraft.client.player.AbstractClientPlayer p) || p.isInvisible()) {
            return;
         }
         java.util.UUID id = p.getUUID();
         if (!dev.lego.net.LegoNet.allowed(id)) {
            return;
         }
         dev.lego.net.LegoNet.Entry e = dev.lego.net.LegoNet.get(id);
         if (e == null || e.items().isEmpty()) {
            return;
         }
         Cos.A a = new Cos.A();
         a.time = state.ageInTicks / 20.0F;
         a.sneak = state.isCrouching;
         a.move = Math.min(1.0F, state.walkAnimationSpeed * 1.4F);
         PlayerModel model = this.getParentModel();
         CosRender.drawRemote(id, e.items(), pose, out, light, model.body, a, false);
         CosRender.drawRemote(id, e.items(), pose, out, light, model.head, a, true);
      }

      @Override
      public void submit(PoseStack var1, SubmitNodeCollector var2, int var3, AvatarRenderState var4, float var5, float var6) {
         Perf.begin("Kosmetik");

         try {
            LocalPlayer var7 = Mc.player();
            if (var7 != null && var4.id != Mc.id(var7)) {
               if (dev.lego.net.LegoNet.cosmeticsEnabled()) {
                  this.remote(var1, var2, var3, var4);
               }
               return;
            }

            if (var7 == null || !Cos.anyEquipped()) {
               return;
            }

            if (!var7.isInvisible()) {
               Cos.A var8 = new Cos.A();
               var8.time = var4.ageInTicks / 20.0F;
               var8.sneak = var4.isCrouching;
               var8.move = Math.min(1.0F, var4.walkAnimationSpeed * 1.4F);

               try {
                  CosRender.skin = var4.skin.body().texturePath();
               } catch (Throwable var17) {
               }

               boolean var9 = !Mc.empty(Mc.armor(39));
               boolean var10 = !Mc.empty(Mc.armor(38));
               boolean var11 = !Mc.empty(Mc.armor(37));
               Cos.slotTransform = (var3x, var4x) -> {
                  switch (var4x) {
                     case HAT:
                        if (var9) {
                           var3x.translate(0.0F, 0.9F, 0.0F);
                           var3x.scale(1.1F);
                        }
                        break;
                     case FACE:
                        if (var9) {
                           var3x.translate(0.0F, 0.0F, 0.9F);
                        }
                        break;
                     case BACK:
                     case WINGS:
                        if (var10) {
                           var3x.translate(0.0F, 0.0F, -1.1F);
                        }
                        break;
                     case AURA:
                        if (var10 || var11) {
                           var3x.scale(1.08F, 1.0F, 1.08F);
                        }
                  }
               };
               CosRender.flagsKey = (var9 ? 1 : 0) | (var10 ? 2 : 0) | (var11 ? 4 : 0);
               PlayerModel var12 = this.getParentModel();
               CosRender.draw(var1, var2, var3, var12.body, var8, false);
               CosRender.draw(var1, var2, var3, var12.head, var8, true);
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

      Q(float[] var1, float[] var2, int var3, float var4, float var5, float var6) {
         this.p = var1;
         this.uv = var2;
         this.argb = var3;
         this.n = new float[]{var4, var5, var6};
      }
   }
}
