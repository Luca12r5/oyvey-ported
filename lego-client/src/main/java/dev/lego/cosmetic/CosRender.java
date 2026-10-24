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

   /** Animated cape texture for any cape id (used for other LEGO players). */
   public static Identifier capeTextureFor(String cape) {
      if (cape == null) return null;
      if (cape.equals("cape_spotify") || cape.equals("spotify")) return null;
      return texture(Capes.frameName(cape, (float)System.currentTimeMillis() / 1000.0F));
   }

   private static final java.util.Map<java.util.UUID, CosRender.Built[]> REMOTE = new java.util.concurrent.ConcurrentHashMap<>();

   /** Builds (cached ~33 ms) and submits the geometry of another player's equipped items. */
   private static final CosRender.Built[] KNIFE_LOCAL = new CosRender.Built[1];
   private static final java.util.Map<java.util.UUID, CosRender.Built[]> KNIFE_REMOTE = new java.util.concurrent.ConcurrentHashMap<>();

   /** Knife cosmetic in the right hand (only while that hand is empty). items == null: the local player's equipped knife. */
   static void drawKnife(java.util.UUID id, java.util.Map<Cos.Slot, String> items, PoseStack var0, SubmitNodeCollector var1, int var2, ModelPart arm, Cos.A var4) {
      drawKnife(id, items, var0, var1, var2, arm, var4, false);
   }

   private static final CosRender.Built[] FINGERS = new CosRender.Built[1];

   static void drawKnife(java.util.UUID id, java.util.Map<Cos.Slot, String> items, PoseStack var0, SubmitNodeCollector var1, int var2, ModelPart arm, Cos.A var4, boolean firstPerson) {
      String key = items == null ? (Cos.equipped(Cos.Slot.KNIFE) == null ? null : Cos.equipped(Cos.Slot.KNIFE).id) : items.get(Cos.Slot.KNIFE);
      if (key == null) return;
      long now = System.currentTimeMillis();
      CosRender.Built[] slot = items == null ? KNIFE_LOCAL : KNIFE_REMOTE.computeIfAbsent(id, k -> new CosRender.Built[1]);
      int ver = key.hashCode();
      CosRender.Built b = slot[0];
      boolean moving = false;
      if (items == null) {
         KnifeTricks.frame(key);
         var4.knifeOpen = KnifeTricks.open();
         moving = KnifeTricks.partsMoving();
      } else {
         var4.knifeOpen = 1.0F;
      }
      if (b == null || now - b.t > 33L || b.ver != ver || moving) {
         LinkedHashMap<String, List<CosRender.Q>> normal = new LinkedHashMap<>();
         LinkedHashMap<String, List<CosRender.Q>> glow = new LinkedHashMap<>();
         G g = new G((tex, p, uv, argb, glowing, n0, n1, n2) -> (glowing ? glow : normal).computeIfAbsent(tex, k -> new ArrayList<>()).add(new CosRender.Q(p, uv, argb, n0, n1, n2)));
         Cos.renderSlot(g, var4, Cos.Slot.KNIFE, items);
         smooth(normal);
         b = new CosRender.Built();
         b.t = now;
         b.ver = ver;
         b.normal = normal;
         b.glow = glow;
         slot[0] = b;
      }
      if (!b.normal.isEmpty() || !b.glow.isEmpty()) {
         var0.pushPose();
         arm.translateAndRotate(var0);
         var0.scale(-0.0625F, -0.0625F, -0.0625F);
         if (firstPerson && KnifeView.showFingers()) {
            // The hand: drawn before the trick rotation so the fingers stay on the hand while the knife turns.
            KnifeTricks.fingers(var4);
            CosRender.Built fb = FINGERS[0];
            if (fb == null || now - fb.t > 33L || KnifeTricks.fingersMoving()) {
               LinkedHashMap<String, List<CosRender.Q>> fn = new LinkedHashMap<>();
               G fg = new G((tex, p, uv, argb, glowing, n0, n1, n2) -> fn.computeIfAbsent(tex, k -> new ArrayList<>()).add(new CosRender.Q(p, uv, argb, n0, n1, n2)));
               Knives.fingers(fg, var4);
               fb = new CosRender.Built();
               fb.t = now;
               fb.normal = fn;
               fb.glow = new LinkedHashMap<>();
               FINGERS[0] = fb;
            }
            net.minecraft.client.player.LocalPlayer me = net.minecraft.client.Minecraft.getInstance().player;
            if (me != null) skin = me.getSkin().body().texturePath();
            submit(var0, var1, fb.normal, false, var2);
         }
         if (items == null) KnifeTricks.apply(var0);
         submit(var0, var1, b.normal, false, var2);
         submit(var0, var1, b.glow, true, var2);
         var0.popPose();
      }
      if (KNIFE_REMOTE.size() > 256) KNIFE_REMOTE.clear();
   }

   private static final Cos.A FIRST_PERSON = new Cos.A();

   /**
    * The equipped knife in your own hand in first person. {@code armSpace} is the pose that
    * AvatarRenderer.renderRightHand received; the arm part's own transform (pivot (-5, 2, 0) px,
    * roll 0.1 rad) is applied here, then the knife is drawn like on the arm in third person.
    */
   public static void drawKnifeFirstPerson(PoseStack armSpace, SubmitNodeCollector out, int light) {
      if (Cos.equipped(Cos.Slot.KNIFE) == null) return;
      FIRST_PERSON.time = (float)(System.currentTimeMillis() % 3600000L) / 1000.0F;
      ModelPart arm = FIRST_PERSON_ARM;
      drawKnife(null, null, armSpace, out, light, arm, FIRST_PERSON, true);
   }

   /** Stand-in for the first-person right arm part: only its rest transform is used. */
   private static final ModelPart FIRST_PERSON_ARM = firstPersonArm();

   private static ModelPart firstPersonArm() {
      ModelPart p = new ModelPart(java.util.List.of(), java.util.Map.of());
      p.setPos(-5.0F, 2.0F, 0.0F);
      p.zRot = 0.1F;
      return p;
   }

   private static final int REMOTE_BUDGET = 3;
   private static long remoteFrame;
   private static int remoteBuilds;
   /** Smooth normals for other players too (costly with many players; off in low mode). */
   static boolean smoothRemote = true;

   static void drawRemote(java.util.UUID id, java.util.Map<Cos.Slot, String> items, PoseStack var0, SubmitNodeCollector var1, int var2, ModelPart var3, Cos.A var4, boolean head) {
      long now = System.currentTimeMillis();
      CosRender.Built[] slot = REMOTE.computeIfAbsent(id, k -> new CosRender.Built[2]);
      int mq = Math.round(var4.move * 10.0F);
      int ver = items.hashCode();
      CosRender.Built b = slot[head ? 1 : 0];
      // Other players: rebuild at most ~12x per second, and only a few per frame (the rest reuse
      // their last geometry) so a crowd of LEGO players does not stall the frame.
      if (now - remoteFrame > 6L) { remoteFrame = now; remoteBuilds = 0; }
      boolean stale = b == null || b.ver != ver || b.sneak != var4.sneak || b.mq != mq || now - b.t > 80L;
      if (stale && (b == null || remoteBuilds < REMOTE_BUDGET)) {
         remoteBuilds++;
         LinkedHashMap<String, List<CosRender.Q>> normal = new LinkedHashMap<>();
         LinkedHashMap<String, List<CosRender.Q>> glow = new LinkedHashMap<>();
         G g = new G((tex, p, uv, argb, glowing, n0, n1, n2) -> (glowing ? glow : normal).computeIfAbsent(tex, k -> new ArrayList<>()).add(new CosRender.Q(p, uv, argb, n0, n1, n2)));
         Cos.renderItems(g, var4, head, items);
         if (smoothRemote && !dev.lego.perf.Performance.lowMode()) smooth(normal);
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
               stateOf = InventoryScreen.class.getDeclaredMethod(dev.lego.util.Remap.method("net.minecraft.class_490", "method_48472", "(Lnet/minecraft/class_1309;)Lnet/minecraft/class_10017;"), LivingEntity.class);
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
         if (p.getMainHandItem().isEmpty()) CosRender.drawKnife(id, e.items(), pose, out, light, model.rightArm, a);
      }

      @Override
      public void submit(PoseStack matrices, SubmitNodeCollector queue, int light, AvatarRenderState state, float a1, float a2) {
         Perf.begin("Kosmetik");

         try {
            try {
               SkinLayers3D.submit(matrices, queue, light, state, this.getParentModel());
            } catch (Throwable e) {
               // never break the player render
            }
            LocalPlayer me = Mc.player();
            if (me == null) {
               return;
            }

            if (state.id != Mc.id(me)) {
               if (dev.lego.net.LegoNet.cosmeticsEnabled()) {
                  this.remote(matrices, queue, light, state);
               }
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
               if (me.getMainHandItem().isEmpty()) CosRender.drawKnife(null, null, matrices, queue, light, model.rightArm, anim);
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
