package dev.lego.visual.shader;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.OptionalInt;

public class MaskDiffShader {
    private static final net.minecraft.class_2960 SHADER_ID = net.minecraft.class_2960.method_60655("legoclient", "core/mask_diff");
    private static final BlendFunction REPLACE_BLEND = new BlendFunction(SourceFactor.ONE, DestFactor.ZERO, SourceFactor.ONE, DestFactor.ZERO);
    private static final net.minecraft.class_2960 PIPELINE_ID = net.minecraft.class_2960.method_60655("legoclient", "pipeline/mask_diff");
    private static final Vector3f MODEL_OFFSET = new Vector3f(0, 0, 0);
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1, 1, 1, 1);

    private RenderPipeline pipeline;
    private boolean initialized;
    private GpuBuffer vertexBuffer;
    private GpuBuffer uniformBuffer;
    private ByteBuffer uniformData;

    private void ensurePipeline() {
        if (pipeline != null) return;
        pipeline = ShaderUtil.register(
                RenderPipeline.builder(ShaderUtil.getTransformsSnippet())
                        .withLocation(PIPELINE_ID)
                        .withVertexShader(SHADER_ID)
                        .withFragmentShader(SHADER_ID)
                        .withVertexFormat(net.minecraft.class_290.field_60033,
                                com.mojang.blaze3d.vertex.VertexFormat.class_5596.field_27379)
                        .withUniform("MaskData", net.minecraft.class_10789.field_60031)
                        .withSampler("BeforeSampler")
                        .withSampler("AfterSampler")
                        .withSampler("DepthBeforeSampler")
                        .withSampler("DepthAfterSampler")
                        .withBlend(REPLACE_BLEND)
                        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                        .withDepthWrite(false)
                        .withCull(false)
                        .build());
    }

    private void writeUniforms(int w, int h) {
        uniformData.clear();
        uniformData.putFloat((float) w);
        uniformData.putFloat((float) h);
        uniformData.putFloat(0);
        uniformData.putFloat(0);
        uniformData.flip();
    }

    private void initialize() {
        if (!initialized) {
            ensurePipeline();
            uniformData = MemoryUtil.memAlloc(16);
            ByteBuffer b = MemoryUtil.memAlloc(4);
            b.putInt(0);
            b.flip();
            vertexBuffer = RenderSystem.getDevice().createBuffer(() -> "legoclient:mask_diff_vtx", 32, b);
            MemoryUtil.memFree(b);
            initialized = true;
        }
    }

    public void close() {
        if (uniformBuffer != null) { uniformBuffer.close(); uniformBuffer = null; }
        if (vertexBuffer != null) { vertexBuffer.close(); vertexBuffer = null; }
        if (uniformData != null) { MemoryUtil.memFree(uniformData); uniformData = null; }
        initialized = false;
    }

    public void renderMask(GpuTextureView maskOut, GpuTextureView sceneBefore, GpuTextureView sceneAfter,
                           GpuTextureView depthBefore, GpuTextureView depthAfter, int w, int h) {
        initialize();
        if (pipeline == null) return;
        CommandEncoder enc = RenderSystem.getDevice().createCommandEncoder();
        net.minecraft.class_12137 linear = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
        net.minecraft.class_12137 nearest = RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST);
        writeUniforms(w, h);
        int sz = uniformData.remaining();
        if (uniformBuffer == null || uniformBuffer.size() < sz) {
            if (uniformBuffer != null) uniformBuffer.close();
            uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "legoclient:mask_diff_ubo", 136, (long) sz);
        }
        enc.writeToBuffer(uniformBuffer.slice(), uniformData);
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().method_71106(
                RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
        try (RenderPass pass = enc.createRenderPass(() -> "legoclient:mask_diff", maskOut, OptionalInt.of(0))) {
            pass.setPipeline(pipeline);
            pass.setVertexBuffer(0, vertexBuffer);
            pass.bindTexture("BeforeSampler", sceneBefore, linear);
            pass.bindTexture("AfterSampler", sceneAfter, linear);
            pass.bindTexture("DepthBeforeSampler", depthBefore, nearest);
            pass.bindTexture("DepthAfterSampler", depthAfter, nearest);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.setUniform("MaskData", uniformBuffer);
            pass.draw(0, 6);
        }
    }
}
