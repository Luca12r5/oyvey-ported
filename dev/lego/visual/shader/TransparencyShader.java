package dev.lego.visual.shader;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

public class TransparencyShader {
   private static final Identifier SHADER_ID = Identifier.fromNamespaceAndPath("legoclient", "core/hand_transparency");
   private static final BlendFunction ALPHA_BLEND = new BlendFunction(
      SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA
   );
   private static final Identifier PIPELINE_ID = Identifier.fromNamespaceAndPath("legoclient", "pipeline/hand_transparency");
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private RenderPipeline pipeline;
   private boolean initialized;
   private GpuBuffer vertexBuffer;
   private GpuBuffer uniformBuffer;
   private ByteBuffer uniformData;

   private void ensurePipeline() {
      if (this.pipeline == null) {
         this.pipeline = ShaderUtil.register(
            RenderPipeline.builder(ShaderUtil.getTransformsSnippet())
               .withLocation(PIPELINE_ID)
               .withVertexShader(SHADER_ID)
               .withFragmentShader(SHADER_ID)
               .withVertexFormat(DefaultVertexFormat.EMPTY, Mode.TRIANGLES)
               .withUniform("TransparencyData", UniformType.UNIFORM_BUFFER)
               .withSampler("SceneBeforeSampler")
               .withSampler("MaskSampler")
               .withBlend(ALPHA_BLEND)
               .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
               .withDepthWrite(false)
               .withCull(false)
               .build()
         );
      }
   }

   private void initialize() {
      if (!this.initialized) {
         this.ensurePipeline();
         this.uniformData = MemoryUtil.memAlloc(16);
         ByteBuffer b = MemoryUtil.memAlloc(4);
         b.putInt(0);
         b.flip();
         this.vertexBuffer = RenderSystem.getDevice().createBuffer(() -> "legoclient:transparency_vtx", 32, b);
         MemoryUtil.memFree(b);
         this.initialized = true;
      }
   }

   public void close() {
      if (this.uniformBuffer != null) {
         this.uniformBuffer.close();
         this.uniformBuffer = null;
      }

      if (this.vertexBuffer != null) {
         this.vertexBuffer.close();
         this.vertexBuffer = null;
      }

      if (this.uniformData != null) {
         MemoryUtil.memFree(this.uniformData);
         this.uniformData = null;
      }

      this.initialized = false;
   }

   public void render(GpuTextureView sceneBeforeView, GpuTextureView maskView, GpuTextureView target, int w, int h, float alpha) {
      this.initialize();
      if (this.pipeline != null) {
         CommandEncoder enc = RenderSystem.getDevice().createCommandEncoder();
         GpuSampler linear = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
         this.uniformData.clear();
         this.uniformData.putFloat(alpha);
         this.uniformData.putFloat(0.0F);
         this.uniformData.putFloat(0.0F);
         this.uniformData.putFloat(0.0F);
         this.uniformData.flip();
         int sz = this.uniformData.remaining();
         if (this.uniformBuffer == null || this.uniformBuffer.size() < sz) {
            if (this.uniformBuffer != null) {
               this.uniformBuffer.close();
            }

            this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "legoclient:transparency_ubo", 136, sz);
         }

         enc.writeToBuffer(this.uniformBuffer.slice(), this.uniformData);
         GpuBufferSlice transforms = RenderSystem.getDynamicUniforms()
            .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

         try (RenderPass pass = enc.createRenderPass(() -> "legoclient:transparency", target, OptionalInt.empty())) {
            pass.setPipeline(this.pipeline);
            pass.setVertexBuffer(0, this.vertexBuffer);
            pass.bindTexture("SceneBeforeSampler", sceneBeforeView, linear);
            pass.bindTexture("MaskSampler", maskView, linear);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.setUniform("TransparencyData", this.uniformBuffer);
            pass.draw(0, 6);
         }
      }
   }
}
