package dev.lego.visual.shader;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;

import java.util.OptionalInt;

public class HandEffect {
    private static HandEffect instance;
    private final net.minecraft.class_310 mc;

    private MaskDiffShader maskDiff;
    private OutlineShader outlineShader;
    private TransparencyShader transparencyShader;

    private GpuTexture sceneBefore, sceneAfter, depthBefore, depthAfter, mask;
    private GpuTextureView sceneBeforeView, sceneAfterView, depthBeforeView, depthAfterView, maskView;
    private int width, height;
    private boolean initialized, captureReady, enabled;

    private boolean outlineEnabled = true;
    private float glowStrength = 20;
    private int fillColor = 0xFFFFFFFF, outlineColor = 0xFFFFFFFF;
    private int outlineWidth = 1, glowMode = 2;
    private boolean useItemColor;

    private boolean transparencyEnabled;
    private float transparencyAlpha = 0.3f;

    public static HandEffect getInstance() {
        if (instance == null) instance = new HandEffect();
        return instance;
    }

    private HandEffect() {
        mc = net.minecraft.class_310.method_1551();
    }

    public void setEnabled(boolean v) {
        enabled = v;
        if (v) initialize();
    }

    public void setOutlineEnabled(boolean v) { outlineEnabled = v; }
    public void setGlowStrength(float v) { glowStrength = v; }
    public void setGlowColors(int c) { fillColor = c; outlineColor = c; }
    public void setUseItemColor(boolean v) { useItemColor = v; }
    public void setOutlineWidth(int v) { outlineWidth = v; }
    public void setGlowMode(int v) { glowMode = v; }
    public void setTransparencyEnabled(boolean v) { transparencyEnabled = v; }
    public void setTransparencyAlpha(float v) { transparencyAlpha = v; }
    public boolean isTransparencyActive() { return enabled && transparencyEnabled; }

    private void initialize() {
        if (!initialized) {
            if (maskDiff != null) maskDiff.close();
            if (outlineShader != null) outlineShader.close();
            if (transparencyShader != null) transparencyShader.close();
            maskDiff = new MaskDiffShader();
            outlineShader = new OutlineShader();
            transparencyShader = new TransparencyShader();
            width = 0; height = 0;
            initialized = true;
        }
    }

    private void resize(int w, int h) {
        if (w != width || h != height || sceneBefore == null) {
            closeTextures();
            sceneBefore = RenderSystem.getDevice().createTexture(() -> "legoclient:hand_before", 13, TextureFormat.RGBA8, w, h, 1, 1);
            sceneBeforeView = RenderSystem.getDevice().createTextureView(sceneBefore);
            sceneAfter = RenderSystem.getDevice().createTexture(() -> "legoclient:hand_after", 13, TextureFormat.RGBA8, w, h, 1, 1);
            sceneAfterView = RenderSystem.getDevice().createTextureView(sceneAfter);
            depthBefore = RenderSystem.getDevice().createTexture(() -> "legoclient:hand_depth_before", 13, TextureFormat.DEPTH32, w, h, 1, 1);
            depthBeforeView = RenderSystem.getDevice().createTextureView(depthBefore);
            depthAfter = RenderSystem.getDevice().createTexture(() -> "legoclient:hand_depth_after", 13, TextureFormat.DEPTH32, w, h, 1, 1);
            depthAfterView = RenderSystem.getDevice().createTextureView(depthAfter);
            mask = RenderSystem.getDevice().createTexture(() -> "legoclient:hand_mask", 13, TextureFormat.RGBA8, w, h, 1, 1);
            maskView = RenderSystem.getDevice().createTextureView(mask);
            width = w; height = h;
        }
    }

    public boolean needsCapture() {
        return enabled && (outlineEnabled || transparencyEnabled);
    }

    public void captureBeforeHands() {
        if (!needsCapture()) return;
        initialize();
        net.minecraft.class_276 fb = mc.method_1522();
        if (fb == null || fb.method_30277() == null) return;
        int tw = fb.field_1482, th = fb.field_1481;
        resize(tw, th);
        CommandEncoder enc = RenderSystem.getDevice().createCommandEncoder();
        enc.copyTextureToTexture(fb.method_30277(), sceneBefore, 0, 0, 0, 0, 0, tw, th);
        if (fb.method_30278() != null)
            enc.copyTextureToTexture(fb.method_30278(), depthBefore, 0, 0, 0, 0, 0, tw, th);
        captureReady = true;
    }

    public void captureAfterHands() {
        if (!needsCapture() || !captureReady) return;
        net.minecraft.class_276 fb = mc.method_1522();
        if (fb == null || fb.method_30277() == null) return;
        CommandEncoder enc = RenderSystem.getDevice().createCommandEncoder();
        enc.copyTextureToTexture(fb.method_30277(), sceneAfter, 0, 0, 0, 0, 0, width, height);
        if (fb.method_30278() != null)
            enc.copyTextureToTexture(fb.method_30278(), depthAfter, 0, 0, 0, 0, 0, width, height);
    }

    public void compositeEffects() {
        if (!needsCapture() || !captureReady) { captureReady = false; return; }
        net.minecraft.class_276 fb = mc.method_1522();
        if (fb == null || fb.method_30277() == null) { captureReady = false; return; }
        maskDiff.renderMask(maskView, sceneBeforeView, sceneAfterView, depthBeforeView, depthAfterView, width, height);
        if (transparencyEnabled) {
            transparencyShader.render(sceneBeforeView, maskView, fb.method_71639(), width, height, transparencyAlpha);
        }
        if (outlineEnabled) {
            outlineShader.render(maskView, fb.method_71639(), width, height,
                    glowStrength, outlineWidth, glowMode, fillColor, outlineColor, sceneAfterView, useItemColor);
        }
        captureReady = false;
    }

    public void close() {
        closeTextures();
        if (maskDiff != null) { maskDiff.close(); maskDiff = null; }
        if (outlineShader != null) { outlineShader.close(); outlineShader = null; }
        if (transparencyShader != null) { transparencyShader.close(); transparencyShader = null; }
        width = 0; height = 0; initialized = false; captureReady = false;
    }

    private void closeTextures() {
        if (sceneBeforeView != null) { sceneBeforeView.close(); sceneBeforeView = null; }
        if (sceneBefore != null) { sceneBefore.close(); sceneBefore = null; }
        if (sceneAfterView != null) { sceneAfterView.close(); sceneAfterView = null; }
        if (sceneAfter != null) { sceneAfter.close(); sceneAfter = null; }
        if (depthBeforeView != null) { depthBeforeView.close(); depthBeforeView = null; }
        if (depthBefore != null) { depthBefore.close(); depthBefore = null; }
        if (depthAfterView != null) { depthAfterView.close(); depthAfterView = null; }
        if (depthAfter != null) { depthAfter.close(); depthAfter = null; }
        if (maskView != null) { maskView.close(); maskView = null; }
        if (mask != null) { mask.close(); mask = null; }
    }
}
