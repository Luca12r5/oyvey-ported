package dev.lego.visual.shader;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

public class OutlineShader {
   private static final Identifier DOWN_SHADER = Identifier.fromNamespaceAndPath("legoclient", "core/glass_outline_down");
   private static final Identifier UP_SHADER = Identifier.fromNamespaceAndPath("legoclient", "core/glass_outline_up");
   private static final Identifier COMBINE_SHADER = Identifier.fromNamespaceAndPath("legoclient", "core/glass_outline_combine");
   private static final Identifier VERTEX_SHADER = Identifier.fromNamespaceAndPath("legoclient", "core/glass_outline");
   private static final Identifier DOWN_PID = Identifier.fromNamespaceAndPath("legoclient", "pipeline/outline_down");
   private static final Identifier UP_PID = Identifier.fromNamespaceAndPath("legoclient", "pipeline/outline_up");
   private static final Identifier COMBINE_PID = Identifier.fromNamespaceAndPath("legoclient", "pipeline/outline_combine");
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private RenderPipeline downPipeline;
   private RenderPipeline upPipeline;
   private RenderPipeline combinePipeline;
   private final GpuTexture[] mipTextures = new GpuTexture[3];
   private final GpuTextureView[] mipViews = new GpuTextureView[3];
   private final int[] mipW = new int[3];
   private final int[] mipH = new int[3];
   private GpuBuffer blurUbo;
   private GpuBuffer outlineUbo;
   private GpuBuffer vertexBuffer;
   private ByteBuffer blurData;
   private ByteBuffer outlineData;
   private boolean initialized;
   private int width;
   private int height;

   private void ensurePipelines() {
      if (this.downPipeline == null) {
         Snippet snippet = ShaderUtil.getTransformsSnippet();
         this.downPipeline = ShaderUtil.register(
            RenderPipeline.builder(snippet)
               .withLocation(DOWN_PID)
               .withVertexShader(VERTEX_SHADER)
               .withFragmentShader(DOWN_SHADER)
               .withVertexFormat(DefaultVertexFormat.EMPTY, Mode.TRIANGLES)
               .withUniform("KawaseOutlineData", UniformType.UNIFORM_BUFFER)
               .withSampler("Sampler0")
               .withBlend(BlendFunction.LIGHTNING)
               .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
               .withDepthWrite(false)
               .withCull(false)
               .build()
         );
         this.upPipeline = ShaderUtil.register(
            RenderPipeline.builder(snippet)
               .withLocation(UP_PID)
               .withVertexShader(VERTEX_SHADER)
               .withFragmentShader(UP_SHADER)
               .withVertexFormat(DefaultVertexFormat.EMPTY, Mode.TRIANGLES)
               .withUniform("KawaseOutlineData", UniformType.UNIFORM_BUFFER)
               .withSampler("Sampler0")
               .withBlend(BlendFunction.LIGHTNING)
               .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
               .withDepthWrite(false)
               .withCull(false)
               .build()
         );
         this.combinePipeline = ShaderUtil.register(
            RenderPipeline.builder(snippet)
               .withLocation(COMBINE_PID)
               .withVertexShader(VERTEX_SHADER)
               .withFragmentShader(COMBINE_SHADER)
               .withVertexFormat(DefaultVertexFormat.EMPTY, Mode.TRIANGLES)
               .withUniform("OutlineData", UniformType.UNIFORM_BUFFER)
               .withSampler("maskTexture")
               .withSampler("glowTexture")
               .withSampler("sceneTexture")
               .withBlend(BlendFunction.LIGHTNING)
               .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
               .withDepthWrite(false)
               .withCull(false)
               .build()
         );
      }
   }

   public void close() {
      this.closeTextures();
      if (this.blurUbo != null) {
         this.blurUbo.close();
         this.blurUbo = null;
      }

      if (this.outlineUbo != null) {
         this.outlineUbo.close();
         this.outlineUbo = null;
      }

      if (this.vertexBuffer != null) {
         this.vertexBuffer.close();
         this.vertexBuffer = null;
      }

      if (this.blurData != null) {
         MemoryUtil.memFree(this.blurData);
         this.blurData = null;
      }

      if (this.outlineData != null) {
         MemoryUtil.memFree(this.outlineData);
         this.outlineData = null;
      }

      this.initialized = false;
      this.width = 0;
      this.height = 0;
   }

   private void initialize() {
      if (!this.initialized) {
         this.ensurePipelines();
         this.blurData = MemoryUtil.memAlloc(16);
         this.outlineData = MemoryUtil.memAlloc(208);
         ByteBuffer b = MemoryUtil.memAlloc(4);
         b.putInt(0);
         b.flip();
         this.vertexBuffer = RenderSystem.getDevice().createBuffer(() -> "legoclient:outline_vtx", 32, b);
         MemoryUtil.memFree(b);
         this.blurUbo = RenderSystem.getDevice().createBuffer(() -> "legoclient:outline_blur_ubo", 136, 16L);
         this.outlineUbo = RenderSystem.getDevice().createBuffer(() -> "legoclient:outline_combine_ubo", 136, 208L);
         this.initialized = true;
      }
   }

   private void resize(int w, int h) {
      if (w != this.width || h != this.height || this.mipTextures[0] == null) {
         this.closeTextures();
         int mw = w;
         int mh = h;

         for (int i = 0; i < 3; i++) {
            mw = Math.max(1, mw / 2);
            mh = Math.max(1, mh / 2);
            this.mipW[i] = mw;
            this.mipH[i] = mh;
            int fi = i;
            this.mipTextures[i] = RenderSystem.getDevice().createTexture(() -> "legoclient:outline_mip_" + fi, 12, TextureFormat.RGBA8, mw, mh, 1, 1);
            this.mipViews[i] = RenderSystem.getDevice().createTextureView(this.mipTextures[i]);
         }

         this.width = w;
         this.height = h;
      }
   }

   private void closeTextures() {
      for (int i = 0; i < 3; i++) {
         if (this.mipViews[i] != null) {
            this.mipViews[i].close();
            this.mipViews[i] = null;
         }

         if (this.mipTextures[i] != null) {
            this.mipTextures[i].close();
            this.mipTextures[i] = null;
         }
      }
   }

   private void uploadBlur(CommandEncoder enc, float hpx, float hpy, float offset) {
      this.blurData.clear();
      this.blurData.putFloat(hpx).putFloat(hpy).putFloat(offset).putFloat(0.0F);
      this.blurData.flip();
      enc.writeToBuffer(this.blurUbo.slice(), this.blurData);
   }

   private void putColor(int c) {
      this.outlineData.putFloat((c >> 16 & 0xFF) / 255.0F);
      this.outlineData.putFloat((c >> 8 & 0xFF) / 255.0F);
      this.outlineData.putFloat((c & 0xFF) / 255.0F);
      this.outlineData.putFloat((c >> 24 & 0xFF) / 255.0F);
   }

   private void uploadOutline(
      CommandEncoder enc, int w, int h, int fillColor, int outColor, int outWidth, int glowMode, float shimmerT, boolean useItem, float maxGlow
   ) {
      this.outlineData.clear();
      this.putColor(fillColor);
      this.putColor(outColor);
      this.outlineData.putFloat(1.0F / w).putFloat(1.0F / h).putFloat(outWidth).putFloat(0.0F);
      this.outlineData.putFloat(glowMode).putFloat(0.0F).putFloat(shimmerT).putFloat(0.0F);
      this.outlineData.putFloat(0.04F).putFloat(useItem ? 1.0F : 0.0F).putFloat(maxGlow).putFloat(0.0F);

      for (int i = 0; i < 32; i++) {
         this.outlineData.putFloat(0.0F);
      }

      this.outlineData.flip();
      enc.writeToBuffer(this.outlineUbo.slice(), this.outlineData);
   }

   public void render(
      GpuTextureView mask,
      GpuTextureView framebuffer,
      int w,
      int h,
      float strength,
      int outWidth,
      int glowMode,
      int fillColor,
      int outColor,
      GpuTextureView sceneAfter,
      boolean useItem
   ) {
      this.initialize();
      if (this.downPipeline != null) {
         this.resize(w, h);
         CommandEncoder enc = RenderSystem.getDevice().createCommandEncoder();
         GpuSampler linear = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
         GpuTextureView src = mask;
         int sw = w;
         int sh = h;

         for (int i = 0; i < 3; i++) {
            this.uploadBlur(enc, 0.5F / sw, 0.5F / sh, 1.0F);
            GpuBufferSlice tr = RenderSystem.getDynamicUniforms()
               .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
            GpuTextureView s = src;

            try (RenderPass pass = enc.createRenderPass(() -> "legoclient:outline_down_" + i, this.mipViews[i], OptionalInt.of(0))) {
               pass.setPipeline(this.downPipeline);
               pass.setVertexBuffer(0, this.vertexBuffer);
               pass.bindTexture("Sampler0", s, linear);
               RenderSystem.bindDefaultUniforms(pass);
               pass.setUniform("DynamicTransforms", tr);
               pass.setUniform("KawaseOutlineData", this.blurUbo);
               pass.draw(0, 6);
            }

            src = this.mipViews[i];
            sw = this.mipW[i];
            sh = this.mipH[i];
         }

         for (int j = 2; j > 0; j--) {
            float up = Math.max(0.5F, strength / 4.0F);
            this.uploadBlur(enc, 0.5F / this.mipW[j], 0.5F / this.mipH[j], Math.min(up, 4.0F));
            GpuBufferSlice tr = RenderSystem.getDynamicUniforms()
               .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
            int fj = j;

            try (RenderPass pass = enc.createRenderPass(() -> "legoclient:outline_up_" + (fj - 1), this.mipViews[fj - 1], OptionalInt.of(0))) {
               pass.setPipeline(this.upPipeline);
               pass.setVertexBuffer(0, this.vertexBuffer);
               pass.bindTexture("Sampler0", this.mipViews[fj], linear);
               RenderSystem.bindDefaultUniforms(pass);
               pass.setUniform("DynamicTransforms", tr);
               pass.setUniform("KawaseOutlineData", this.blurUbo);
               pass.draw(0, 6);
            }
         }

         float maxGlow = Math.max(8.0F, strength + outWidth + 6.0F);
         this.uploadOutline(enc, w, h, fillColor, outColor, outWidth, glowMode, 0.0F, useItem, maxGlow);
         GpuBufferSlice tr = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

         try (RenderPass pass = enc.createRenderPass(() -> "legoclient:outline_combine", framebuffer, OptionalInt.empty())) {
            pass.setPipeline(this.combinePipeline);
            pass.setVertexBuffer(0, this.vertexBuffer);
            pass.bindTexture("maskTexture", mask, linear);
            pass.bindTexture("glowTexture", this.mipViews[0], linear);
            pass.bindTexture("sceneTexture", sceneAfter, linear);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", tr);
            pass.setUniform("OutlineData", this.outlineUbo);
            pass.draw(0, 6);
         }
      }
   }
}
