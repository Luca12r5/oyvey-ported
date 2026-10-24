package dev.lego.visual.shader;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.OptionalInt;

public class OutlineShader {
    private static final net.minecraft.class_2960 DOWN_SHADER = net.minecraft.class_2960.method_60655("legoclient", "core/glass_outline_down");
    private static final net.minecraft.class_2960 UP_SHADER = net.minecraft.class_2960.method_60655("legoclient", "core/glass_outline_up");
    private static final net.minecraft.class_2960 COMBINE_SHADER = net.minecraft.class_2960.method_60655("legoclient", "core/glass_outline_combine");
    private static final net.minecraft.class_2960 VERTEX_SHADER = net.minecraft.class_2960.method_60655("legoclient", "core/glass_outline");

    private static final net.minecraft.class_2960 DOWN_PID = net.minecraft.class_2960.method_60655("legoclient", "pipeline/outline_down");
    private static final net.minecraft.class_2960 UP_PID = net.minecraft.class_2960.method_60655("legoclient", "pipeline/outline_up");
    private static final net.minecraft.class_2960 COMBINE_PID = net.minecraft.class_2960.method_60655("legoclient", "pipeline/outline_combine");

    private static final Vector3f MODEL_OFFSET = new Vector3f(0, 0, 0);
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1, 1, 1, 1);
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();

    private RenderPipeline downPipeline, upPipeline, combinePipeline;

    private final GpuTexture[] mipTextures = new GpuTexture[3];
    private final GpuTextureView[] mipViews = new GpuTextureView[3];
    private final int[] mipW = new int[3], mipH = new int[3];
    private GpuBuffer blurUbo, outlineUbo, vertexBuffer;
    private ByteBuffer blurData, outlineData;
    private boolean initialized;
    private int width, height;

    private void ensurePipelines() {
        if (downPipeline != null) return;
        RenderPipeline.Snippet snippet = ShaderUtil.getTransformsSnippet();
        downPipeline = ShaderUtil.register(
                RenderPipeline.builder(snippet)
                        .withLocation(DOWN_PID)
                        .withVertexShader(VERTEX_SHADER)
                        .withFragmentShader(DOWN_SHADER)
                        .withVertexFormat(net.minecraft.class_290.field_60033,
                                com.mojang.blaze3d.vertex.VertexFormat.class_5596.field_27379)
                        .withUniform("KawaseOutlineData", net.minecraft.class_10789.field_60031)
                        .withSampler("Sampler0")
                        .withBlend(BlendFunction.LIGHTNING)
                        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                        .withDepthWrite(false).withCull(false).build());

        upPipeline = ShaderUtil.register(
                RenderPipeline.builder(snippet)
                        .withLocation(UP_PID)
                        .withVertexShader(VERTEX_SHADER)
                        .withFragmentShader(UP_SHADER)
                        .withVertexFormat(net.minecraft.class_290.field_60033,
                                com.mojang.blaze3d.vertex.VertexFormat.class_5596.field_27379)
                        .withUniform("KawaseOutlineData", net.minecraft.class_10789.field_60031)
                        .withSampler("Sampler0")
                        .withBlend(BlendFunction.LIGHTNING)
                        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                        .withDepthWrite(false).withCull(false).build());

        combinePipeline = ShaderUtil.register(
                RenderPipeline.builder(snippet)
                        .withLocation(COMBINE_PID)
                        .withVertexShader(VERTEX_SHADER)
                        .withFragmentShader(COMBINE_SHADER)
                        .withVertexFormat(net.minecraft.class_290.field_60033,
                                com.mojang.blaze3d.vertex.VertexFormat.class_5596.field_27379)
                        .withUniform("OutlineData", net.minecraft.class_10789.field_60031)
                        .withSampler("maskTexture").withSampler("glowTexture").withSampler("sceneTexture")
                        .withBlend(BlendFunction.LIGHTNING)
                        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                        .withDepthWrite(false).withCull(false).build());
    }

    public void close() {
        closeTextures();
        if (blurUbo != null) { blurUbo.close(); blurUbo = null; }
        if (outlineUbo != null) { outlineUbo.close(); outlineUbo = null; }
        if (vertexBuffer != null) { vertexBuffer.close(); vertexBuffer = null; }
        if (blurData != null) { MemoryUtil.memFree(blurData); blurData = null; }
        if (outlineData != null) { MemoryUtil.memFree(outlineData); outlineData = null; }
        initialized = false; width = 0; height = 0;
    }

    private void initialize() {
        if (!initialized) {
            ensurePipelines();
            blurData = MemoryUtil.memAlloc(16);
            outlineData = MemoryUtil.memAlloc(208);
            ByteBuffer b = MemoryUtil.memAlloc(4); b.putInt(0); b.flip();
            vertexBuffer = RenderSystem.getDevice().createBuffer(() -> "legoclient:outline_vtx", 32, b);
            MemoryUtil.memFree(b);
            blurUbo = RenderSystem.getDevice().createBuffer(() -> "legoclient:outline_blur_ubo", 136, 16L);
            outlineUbo = RenderSystem.getDevice().createBuffer(() -> "legoclient:outline_combine_ubo", 136, 208L);
            initialized = true;
        }
    }

    private void resize(int w, int h) {
        if (w != width || h != height || mipTextures[0] == null) {
            closeTextures();
            int mw = w, mh = h;
            for (int i = 0; i < 3; i++) {
                mw = Math.max(1, mw / 2); mh = Math.max(1, mh / 2);
                mipW[i] = mw; mipH[i] = mh;
                int fi = i;
                mipTextures[i] = RenderSystem.getDevice().createTexture(() -> "legoclient:outline_mip_" + fi, 12, TextureFormat.RGBA8, mw, mh, 1, 1);
                mipViews[i] = RenderSystem.getDevice().createTextureView(mipTextures[i]);
            }
            width = w; height = h;
        }
    }

    private void closeTextures() {
        for (int i = 0; i < 3; i++) {
            if (mipViews[i] != null) { mipViews[i].close(); mipViews[i] = null; }
            if (mipTextures[i] != null) { mipTextures[i].close(); mipTextures[i] = null; }
        }
    }

    private void uploadBlur(CommandEncoder enc, float hpx, float hpy, float offset) {
        blurData.clear();
        blurData.putFloat(hpx).putFloat(hpy).putFloat(offset).putFloat(0);
        blurData.flip();
        enc.writeToBuffer(blurUbo.slice(), blurData);
    }

    private void putColor(int c) {
        outlineData.putFloat(((c >> 16) & 0xFF) / 255f);
        outlineData.putFloat(((c >> 8) & 0xFF) / 255f);
        outlineData.putFloat((c & 0xFF) / 255f);
        outlineData.putFloat(((c >> 24) & 0xFF) / 255f);
    }

    private void uploadOutline(CommandEncoder enc, int w, int h, int fillColor, int outColor, int outWidth, int glowMode, float shimmerT, boolean useItem, float maxGlow) {
        outlineData.clear();
        putColor(fillColor);
        putColor(outColor);
        outlineData.putFloat(1f / w).putFloat(1f / h).putFloat(outWidth).putFloat(0);
        outlineData.putFloat(glowMode).putFloat(0).putFloat(shimmerT).putFloat(0);
        outlineData.putFloat(0.04f).putFloat(useItem ? 1 : 0).putFloat(maxGlow).putFloat(0);
        for (int i = 0; i < 32; i++) outlineData.putFloat(0);
        outlineData.flip();
        enc.writeToBuffer(outlineUbo.slice(), outlineData);
    }

    public void render(GpuTextureView mask, GpuTextureView framebuffer, int w, int h,
                       float strength, int outWidth, int glowMode, int fillColor, int outColor,
                       GpuTextureView sceneAfter, boolean useItem) {
        initialize();
        if (downPipeline == null) return;
        resize(w, h);
        CommandEncoder enc = RenderSystem.getDevice().createCommandEncoder();
        net.minecraft.class_12137 linear = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
        GpuTextureView src = mask;
        int sw = w, sh = h;

        for (int i = 0; i < 3; i++) {
            uploadBlur(enc, 0.5f / sw, 0.5f / sh, 1);
            GpuBufferSlice tr = RenderSystem.getDynamicUniforms().method_71106(
                    RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
            GpuTextureView s = src; int fi = i;
            try (RenderPass pass = enc.createRenderPass(() -> "legoclient:outline_down_" + fi, mipViews[i], OptionalInt.of(0))) {
                pass.setPipeline(downPipeline);
                pass.setVertexBuffer(0, vertexBuffer);
                pass.bindTexture("Sampler0", s, linear);
                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("DynamicTransforms", tr);
                pass.setUniform("KawaseOutlineData", blurUbo);
                pass.draw(0, 6);
            }
            src = mipViews[i]; sw = mipW[i]; sh = mipH[i];
        }

        for (int j = 2; j > 0; j--) {
            float up = Math.max(0.5f, strength / 4f);
            uploadBlur(enc, 0.5f / mipW[j], 0.5f / mipH[j], Math.min(up, 4));
            GpuBufferSlice tr = RenderSystem.getDynamicUniforms().method_71106(
                    RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
            int fj = j;
            try (RenderPass pass = enc.createRenderPass(() -> "legoclient:outline_up_" + (fj - 1), mipViews[fj - 1], OptionalInt.of(0))) {
                pass.setPipeline(upPipeline);
                pass.setVertexBuffer(0, vertexBuffer);
                pass.bindTexture("Sampler0", mipViews[fj], linear);
                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("DynamicTransforms", tr);
                pass.setUniform("KawaseOutlineData", blurUbo);
                pass.draw(0, 6);
            }
        }

        float maxGlow = Math.max(8, strength + outWidth + 6);
        uploadOutline(enc, w, h, fillColor, outColor, outWidth, glowMode, 0, useItem, maxGlow);
        GpuBufferSlice tr = RenderSystem.getDynamicUniforms().method_71106(
                RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
        try (RenderPass pass = enc.createRenderPass(() -> "legoclient:outline_combine", framebuffer, OptionalInt.empty())) {
            pass.setPipeline(combinePipeline);
            pass.setVertexBuffer(0, vertexBuffer);
            pass.bindTexture("maskTexture", mask, linear);
            pass.bindTexture("glowTexture", mipViews[0], linear);
            pass.bindTexture("sceneTexture", sceneAfter, linear);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", tr);
            pass.setUniform("OutlineData", outlineUbo);
            pass.draw(0, 6);
        }
    }
}
