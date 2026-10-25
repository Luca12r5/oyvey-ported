package de.bauloo.styles;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Builder;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderSystem.class_5590;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1011;
import net.minecraft.class_10799;
import net.minecraft.class_12137;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3264;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_4587;
import net.minecraft.class_638;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import net.minecraft.class_2874.class_12326;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class CustomSky {
   private static final Pattern LAYER = Pattern.compile("optifine/sky/world0/sky(\\d+)\\.properties");
   private static final String[] OTHER_MODS = new String[]{"skyboxify", "fabricskyboxes", "nuit"};
   private static final List<CustomSky.Layer> LAYERS = new ArrayList<>();
   private static final List<CustomSky.SkyTexture> TEXTURES = new ArrayList<>();
   private static final List<int[]> THUMBS = new ArrayList<>();
   public static final int THUMB = 32;
   private static int layersVersion;
   private static final RenderPipeline[] PIPELINES = new RenderPipeline[CustomSky.Blend.values().length];
   private static GpuBuffer cube;
   private static boolean otherMod;
   private static boolean failedOnce;

   public static int layersVersion() {
      return layersVersion;
   }

   private CustomSky() {
   }

   public static void register() {
      for (String mod : OTHER_MODS) {
         otherMod = otherMod | FabricLoader.getInstance().isModLoaded(mod);
      }

      if (otherMod) {
         BaulooStylesClient.LOGGER.info("[CustomSky] a skybox mod is installed, it draws the skies");
      } else {
         ResourceManagerHelper.get(class_3264.field_14188).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            public class_2960 getFabricId() {
               return class_2960.method_60655("bauloo_styles", "custom_sky");
            }

            public Collection<class_2960> getFabricDependencies() {
               return List.of(ResourceReloadListenerKeys.TEXTURES);
            }

            public void method_14491(class_3300 manager) {
               List<CustomSky.Loaded> loaded = CustomSky.read(manager);
               class_310.method_1551().method_63588(() -> CustomSky.install(loaded));
            }
         });
      }
   }

   public static boolean active() {
      return !otherMod;
   }

   private static List<CustomSky.Loaded> read(class_3300 manager) {
      List<CustomSky.Loaded> out = new ArrayList<>();
      Map<class_2960, class_3298> found = manager.method_14488(
         "optifine/sky/world0", id -> id.method_12836().equals("minecraft") && id.method_12832().endsWith(".properties")
      );

      for (Entry<class_2960, class_3298> e : found.entrySet()) {
         Matcher m = LAYER.matcher(e.getKey().method_12832());
         if (m.matches()) {
            try {
               Properties p = new Properties();
               InputStreamReader reader = new InputStreamReader(e.getValue().method_14482(), StandardCharsets.UTF_8);

               try {
                  p.load(reader);
               } catch (Throwable var15) {
                  try {
                     reader.close();
                  } catch (Throwable var14) {
                     var15.addSuppressed(var14);
                  }

                  throw var15;
               }

               reader.close();
               class_2960 source = source(p.getProperty("source", "./sky" + m.group(1) + ".png").trim());
               if (source != null) {
                  Optional<class_3298> res = manager.method_14486(source);
                  if (res.isEmpty()) {
                     BaulooStylesClient.LOGGER.info("[CustomSky] {}: picture {} not found", e.getKey(), source);
                  } else {
                     class_1011 image;
                     try (InputStream in = res.get().method_14482()) {
                        image = class_1011.method_4309(in);
                     }

                     out.add(new CustomSky.Loaded(layer(Integer.parseInt(m.group(1)), source, p), image));
                  }
               }
            } catch (Exception ex) {
               BaulooStylesClient.LOGGER.warn("[CustomSky] {} could not be read: {}", e.getKey(), ex.toString());
            }
         }
      }

      out.sort((a, b) -> Integer.compare(a.layer().number(), b.layer().number()));
      return out;
   }

   private static class_2960 source(String s) {
      s = s.replace('\\', '/');
      if (s.startsWith("./")) {
         s = "optifine/sky/world0/" + s.substring(2);
      } else if (s.startsWith("assets/minecraft/")) {
         s = s.substring("assets/minecraft/".length());
      }

      if (!s.endsWith(".png")) {
         s = s + ".png";
      }

      return class_2960.method_12829(s.toLowerCase(Locale.ROOT));
   }

   private static CustomSky.Layer layer(int number, class_2960 texture, Properties p) {
      CustomSky.Blend blend;
      try {
         blend = CustomSky.Blend.valueOf(p.getProperty("blend", "add").trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         blend = CustomSky.Blend.ADD;
      }

      int sIn = time(p.getProperty("startFadeIn"));
      int eIn = time(p.getProperty("endFadeIn"));
      int eOut = time(p.getProperty("endFadeOut"));
      boolean fades = sIn >= 0 && eIn >= 0 && eOut >= 0;
      int sOut = time(p.getProperty("startFadeOut"));
      if (fades && sOut < 0) {
         sOut = norm(eOut - norm(eIn - sIn));
      }

      Vector3f axis = new Vector3f(0.0F, 0.0F, 1.0F);
      String a = p.getProperty("axis");
      if (a != null) {
         String[] parts = a.trim().split("\\s+");
         if (parts.length == 3) {
            try {
               Vector3f v = new Vector3f(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2]));
               if (v.lengthSquared() > 1.0E-6F) {
                  axis = v.normalize();
               }
            } catch (NumberFormatException var14) {
            }
         }
      }

      float speed = 1.0F;

      try {
         speed = Float.parseFloat(p.getProperty("speed", "1").trim());
      } catch (NumberFormatException var13) {
      }

      String weather = " " + p.getProperty("weather", "clear").toLowerCase(Locale.ROOT) + " ";
      return new CustomSky.Layer(
         number,
         texture,
         blend,
         fades,
         sIn,
         eIn,
         sOut,
         eOut,
         Boolean.parseBoolean(p.getProperty("rotate", "true").trim()),
         speed,
         axis,
         weather.contains(" clear "),
         weather.contains(" rain "),
         weather.contains(" thunder ")
      );
   }

   private static int time(String s) {
      if (s == null) {
         return -1;
      }

      String[] parts = s.trim().split(":");
      if (parts.length != 2) {
         return -1;
      }

      try {
         int h = Integer.parseInt(parts[0].trim());
         int m = Integer.parseInt(parts[1].trim());
         return norm(h * 1000 + m * 1000 / 60 - 6000);
      } catch (NumberFormatException e) {
         return -1;
      }
   }

   private static int norm(int t) {
      t %= 24000;
      return t < 0 ? t + 24000 : t;
   }

   private static void install(List<CustomSky.Loaded> loaded) {
      for (CustomSky.SkyTexture t : TEXTURES) {
         t.close();
      }

      TEXTURES.clear();
      LAYERS.clear();
      THUMBS.clear();

      for (CustomSky.Loaded l : loaded) {
         try {
            TEXTURES.add(new CustomSky.SkyTexture(l.layer().texture(), l.image()));
            LAYERS.add(l.layer());
            THUMBS.add(thumb(l.image()));
         } catch (Exception e) {
            BaulooStylesClient.LOGGER.warn("[CustomSky] {} could not be uploaded: {}", l.layer().texture(), e.toString());
         } finally {
            l.image().close();
         }
      }

      layersVersion++;
      if (!LAYERS.isEmpty()) {
         BaulooStylesClient.LOGGER.info("[CustomSky] {} sky layers from the packs", LAYERS.size());
      }
   }

   public static void failed(Throwable t) {
      if (!failedOnce) {
         failedOnce = true;
         BaulooStylesClient.LOGGER.warn("[CustomSky] drawing failed", t);
      }
   }

   public static void render(class_4587 matrices, float sunAngle) {
      if (!otherMod && !LAYERS.isEmpty()) {
         class_638 world = class_310.method_1551().field_1687;
         if (world != null && world.method_8597().comp_5220() == class_12326.field_64386) {
            float tick = class_310.method_1551().method_61966().method_60637(false);
            int day = (int)(world.method_8532() % 24000L);
            float rain = world.method_8430(tick);
            float thunder = world.method_8478(tick);
            GpuBuffer vertices = cube();
            class_5590 quads = RenderSystem.getSequentialBuffer(class_5596.field_27382);
            GpuBuffer indices = quads.method_68274(36);
            GpuTextureView color = class_310.method_1551().method_1522().method_71639();
            GpuTextureView depth = class_310.method_1551().method_1522().method_71640();

            for (int i = 0; i < LAYERS.size(); i++) {
               CustomSky.Layer layer = LAYERS.get(i);
               float b = fade(layer, day) * weather(layer, rain, thunder);
               if (!(b <= 1.0E-4F) && (layer.blend() != CustomSky.Blend.REPLACE || !(b < 0.1F))) {
                  Matrix4fStack mv = RenderSystem.getModelViewStack();
                  mv.pushMatrix();
                  mv.mul(matrices.method_23760().method_23761());
                  mv.rotateY((float)Math.toRadians(-90.0));
                  if (layer.rotate()) {
                     mv.rotate(sunAngle * layer.speed(), layer.axis());
                  }

                  GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().method_71106(mv, modulator(layer.blend(), b), new Vector3f(), new Matrix4f());
                  CustomSky.SkyTexture texture = TEXTURES.get(i);
                  RenderPass pass = RenderSystem.getDevice()
                     .createCommandEncoder()
                     .createRenderPass(() -> "Bauloo custom sky", color, OptionalInt.empty(), depth, OptionalDouble.empty());

                  try {
                     pass.setPipeline(pipeline(layer.blend()));
                     RenderSystem.bindDefaultUniforms(pass);
                     pass.setUniform("DynamicTransforms", transforms);
                     pass.bindTexture("Sampler0", texture.view, texture.sampler);
                     pass.setVertexBuffer(0, vertices);
                     pass.setIndexBuffer(indices, quads.method_31924());
                     pass.drawIndexed(0, 0, 36, 1);
                  } catch (Throwable var22) {
                     if (pass != null) {
                        try {
                           pass.close();
                        } catch (Throwable var21) {
                           var22.addSuppressed(var21);
                        }
                     }

                     throw var22;
                  }

                  if (pass != null) {
                     pass.close();
                  }

                  mv.popMatrix();
               }
            }
         }
      }
   }

   private static int[] thumb(class_1011 image) {
      int[] out = new int[1024];
      int fw = image.method_4307() / 3;
      int fh = image.method_4323() / 2;

      for (int y = 0; y < 32; y++) {
         for (int x = 0; x < 32; x++) {
            out[y * 32 + x] = image.method_61940(fw + x * fw / 32, fh + y * fh / 32);
         }
      }

      return out;
   }

   public static boolean hasSky() {
      return !otherMod && !LAYERS.isEmpty();
   }

   public static int[] preview(int ticks, int base) {
      float[] r = new float[1024];
      float[] g = new float[1024];
      float[] b = new float[1024];
      Arrays.fill(r, (base >> 16 & 0xFF) / 255.0F);
      Arrays.fill(g, (base >> 8 & 0xFF) / 255.0F);
      Arrays.fill(b, (base & 0xFF) / 255.0F);

      for (int i = 0; i < LAYERS.size() && i < THUMBS.size(); i++) {
         CustomSky.Layer layer = LAYERS.get(i);
         float k = fade(layer, ticks) * weather(layer, 0.0F, 0.0F);
         if (!(k <= 1.0E-4F) && (layer.blend() != CustomSky.Blend.REPLACE || !(k < 0.1F))) {
            int[] px = THUMBS.get(i);

            for (int p = 0; p < px.length; p++) {
               int c = px[p];
               float a = (c >>> 24 & 0xFF) / 255.0F;
               float sr = (c >> 16 & 0xFF) / 255.0F;
               float sg = (c >> 8 & 0xFF) / 255.0F;
               float sb = (c & 0xFF) / 255.0F;
               switch (layer.blend()) {
                  case ALPHA: {
                     float f = a * k;
                     r[p] += (sr - r[p]) * f;
                     g[p] += (sg - g[p]) * f;
                     b[p] += (sb - b[p]) * f;
                     break;
                  }
                  case ADD: {
                     float f = a * k;
                     r[p] += sr * f;
                     g[p] += sg * f;
                     b[p] += sb * f;
                     break;
                  }
                  case SUBTRACT:
                     r[p] -= sr * k;
                     g[p] -= sg * k;
                     b[p] -= sb * k;
                     break;
                  case MULTIPLY:
                     r[p] *= 1.0F - k + sr * k;
                     g[p] *= 1.0F - k + sg * k;
                     b[p] *= 1.0F - k + sb * k;
                     break;
                  case DODGE:
                     r[p] += sr * k;
                     g[p] += sg * k;
                     b[p] += sb * k;
                     break;
                  case BURN:
                     r[p] *= 1.0F - sr * k;
                     g[p] *= 1.0F - sg * k;
                     b[p] *= 1.0F - sb * k;
                     break;
                  case SCREEN:
                     r[p] = r[p] + sr * k - r[p] * sr * k;
                     g[p] = g[p] + sg * k - g[p] * sg * k;
                     b[p] = b[p] + sb * k - b[p] * sb * k;
                     break;
                  case OVERLAY:
                     r[p] *= 1.0F + sr * k;
                     g[p] *= 1.0F + sg * k;
                     b[p] *= 1.0F + sb * k;
                     break;
                  case REPLACE:
                     r[p] = sr;
                     g[p] = sg;
                     b[p] = sb;
               }
            }
         }
      }

      int[] out = new int[1024];

      for (int p = 0; p < out.length; p++) {
         out[p] = 0xFF000000 | to255(r[p]) << 16 | to255(g[p]) << 8 | to255(b[p]);
      }

      return out;
   }

   private static int to255(float v) {
      return Math.max(0, Math.min(255, Math.round(v * 255.0F)));
   }

   private static float fade(CustomSky.Layer l, int t) {
      if (!l.fades()) {
         return 1.0F;
      } else if (between(t, l.endFadeIn(), l.startFadeOut())) {
         return 1.0F;
      } else if (between(t, l.startFadeIn(), l.endFadeIn())) {
         int len = norm(l.endFadeIn() - l.startFadeIn());
         return len == 0 ? 1.0F : (float)norm(t - l.startFadeIn()) / len;
      } else if (between(t, l.startFadeOut(), l.endFadeOut())) {
         int len = norm(l.endFadeOut() - l.startFadeOut());
         return len == 0 ? 0.0F : 1.0F - (float)norm(t - l.startFadeOut()) / len;
      } else {
         return 0.0F;
      }
   }

   private static boolean between(int t, int a, int b) {
      return a <= b ? t >= a && t <= b : t >= a || t <= b;
   }

   private static float weather(CustomSky.Layer l, float rain, float thunder) {
      float b = 0.0F;
      if (l.clear()) {
         b += 1.0F - rain;
      }

      if (l.rain()) {
         b += rain - thunder;
      }

      if (l.thunder()) {
         b += thunder;
      }

      return Math.max(0.0F, Math.min(1.0F, b));
   }

   private static Vector4f modulator(CustomSky.Blend blend, float b) {
      return switch (blend) {
         case ALPHA, ADD, REPLACE -> new Vector4f(1.0F, 1.0F, 1.0F, b);
         default -> new Vector4f(b, b, b, 1.0F);
         case MULTIPLY -> new Vector4f(b, b, b, b);
      };
   }

   private static RenderPipeline pipeline(CustomSky.Blend blend) {
      RenderPipeline p = PIPELINES[blend.ordinal()];
      if (p != null) {
         return p;
      }

      Builder builder = RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(class_2960.method_60655("bauloo_styles", "pipeline/custom_sky_" + blend.name().toLowerCase(Locale.ROOT)))
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withDepthWrite(false)
         .withVertexFormat(class_290.field_1575, class_5596.field_27382);
      switch (blend) {
         case ALPHA:
            builder.withBlend(BlendFunction.TRANSLUCENT);
            break;
         case ADD:
            builder.withBlend(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE));
            break;
         case SUBTRACT:
            builder.withBlend(new BlendFunction(SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ZERO));
            break;
         case MULTIPLY:
            builder.withBlend(new BlendFunction(SourceFactor.DST_COLOR, DestFactor.ONE_MINUS_SRC_ALPHA));
            break;
         case DODGE:
            builder.withBlend(new BlendFunction(SourceFactor.ONE, DestFactor.ONE));
            break;
         case BURN:
            builder.withBlend(new BlendFunction(SourceFactor.ZERO, DestFactor.ONE_MINUS_SRC_COLOR));
            break;
         case SCREEN:
            builder.withBlend(new BlendFunction(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_COLOR));
            break;
         case OVERLAY:
            builder.withBlend(new BlendFunction(SourceFactor.DST_COLOR, DestFactor.SRC_COLOR));
            break;
         case REPLACE:
            builder.withoutBlend();
      }

      p = builder.build();
      PIPELINES[blend.ordinal()] = p;
      return p;
   }

   private static GpuBuffer cube() {
      if (cube != null) {
         return cube;
      }

      class_9799 allocator = class_9799.method_72201(24 * class_290.field_1575.getVertexSize());

      try {
         class_287 bb = new class_287(allocator, class_5596.field_27382, class_290.field_1575);
         Matrix4f m = new Matrix4f().rotateX(rad(90.0F)).rotateZ(rad(-90.0F));
         side(bb, new Matrix4f(m), 4);
         side(bb, new Matrix4f(m).rotateX(rad(90.0F)), 1);
         side(bb, new Matrix4f(m).rotateX(rad(-90.0F)), 0);
         m.rotateZ(rad(90.0F));
         side(bb, new Matrix4f(m), 5);
         m.rotateZ(rad(90.0F));
         side(bb, new Matrix4f(m), 2);
         m.rotateZ(rad(90.0F));
         side(bb, new Matrix4f(m), 3);
         class_9801 built = bb.method_60800();

         try {
            cube = RenderSystem.getDevice().createBuffer(() -> "Bauloo custom sky cube", 32, built.method_60818());
         } catch (Throwable var8) {
            if (built != null) {
               try {
                  built.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (built != null) {
            built.close();
         }
      } catch (Throwable var9) {
         if (allocator != null) {
            try {
               allocator.close();
            } catch (Throwable var6) {
               var9.addSuppressed(var6);
            }
         }

         throw var9;
      }

      if (allocator != null) {
         allocator.close();
      }

      return cube;
   }

   private static void side(class_287 bb, Matrix4f m, int side) {
      float tx = side % 3 / 3.0F;
      float ty = side / 3 / 2.0F;
      float w = 0.33333334F;
      bb.method_22918(m, -100.0F, -100.0F, -100.0F).method_22913(tx, ty).method_39415(-1);
      bb.method_22918(m, -100.0F, -100.0F, 100.0F).method_22913(tx, ty + 0.5F).method_39415(-1);
      bb.method_22918(m, 100.0F, -100.0F, 100.0F).method_22913(tx + w, ty + 0.5F).method_39415(-1);
      bb.method_22918(m, 100.0F, -100.0F, -100.0F).method_22913(tx + w, ty).method_39415(-1);
   }

   private static float rad(float degrees) {
      return (float)Math.toRadians(degrees);
   }

   private enum Blend {
      ALPHA,
      ADD,
      SUBTRACT,
      MULTIPLY,
      DODGE,
      BURN,
      SCREEN,
      OVERLAY,
      REPLACE;
   }

   private record Layer(
      int number,
      class_2960 texture,
      CustomSky.Blend blend,
      boolean fades,
      int startFadeIn,
      int endFadeIn,
      int startFadeOut,
      int endFadeOut,
      boolean rotate,
      float speed,
      Vector3f axis,
      boolean clear,
      boolean rain,
      boolean thunder
   ) {
   }

   private record Loaded(CustomSky.Layer layer, class_1011 image) {
   }

   private static final class SkyTexture {
      final GpuTexture texture;
      final GpuTextureView view;
      final class_12137 sampler;

      SkyTexture(class_2960 id, class_1011 image) {
         GpuDevice device = RenderSystem.getDevice();
         this.texture = device.createTexture(() -> "Bauloo sky " + id, 5, TextureFormat.RGBA8, image.method_4307(), image.method_4323(), 1, 1);
         this.view = device.createTextureView(this.texture);
         this.sampler = RenderSystem.getSamplerCache()
            .method_75293(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false);
         device.createCommandEncoder().writeToTexture(this.texture, image, 0, 0, 0, 0, image.method_4307(), image.method_4323(), 0, 0);
      }

      void close() {
         this.view.close();
         this.texture.close();
      }
   }
}
