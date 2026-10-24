package dev.lego.visual.shader;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.Minecraft;

public class HandEffect {
   private static HandEffect instance;
   private final Minecraft mc;
   private MaskDiffShader maskDiff;
   private OutlineShader outlineShader;
   private TransparencyShader transparencyShader;
   private GpuTexture sceneBefore;
   private GpuTexture sceneAfter;
   private GpuTexture depthBefore;
   private GpuTexture depthAfter;
   private GpuTexture mask;
   private GpuTextureView sceneBeforeView;
   private GpuTextureView sceneAfterView;
   private GpuTextureView depthBeforeView;
   private GpuTextureView depthAfterView;
   private GpuTextureView maskView;
   private int width;
   private int height;
   private boolean initialized;
   private boolean captureReady;
   private boolean enabled;
   private boolean outlineEnabled = true;
   private float glowStrength = 20.0F;
   private int fillColor = -1;
   private int outlineColor = -1;
   private int outlineWidth = 1;
   private int glowMode = 2;
   private boolean useItemColor;
   private boolean transparencyEnabled;
   private float transparencyAlpha = 0.3F;

   public static HandEffect getInstance() {
      if (instance == null) {
         instance = new HandEffect();
      }

      return instance;
   }

   private HandEffect() {
      this.mc = Minecraft.getInstance();
   }

   public void setEnabled(boolean v) {
      this.enabled = v;
      if (v) {
         this.initialize();
      }
   }

   public void setOutlineEnabled(boolean v) {
      this.outlineEnabled = v;
   }

   public void setGlowStrength(float v) {
      this.glowStrength = v;
   }

   public void setGlowColors(int c) {
      this.fillColor = c;
      this.outlineColor = c;
   }

   public void setUseItemColor(boolean v) {
      this.useItemColor = v;
   }

   public void setOutlineWidth(int v) {
      this.outlineWidth = v;
   }

   public void setGlowMode(int v) {
      this.glowMode = v;
   }

   public void setTransparencyEnabled(boolean v) {
      this.transparencyEnabled = v;
   }

   public void setTransparencyAlpha(float v) {
      this.transparencyAlpha = v;
   }

   public boolean isTransparencyActive() {
      return this.enabled && this.transparencyEnabled;
   }

   private void initialize() {
      if (!this.initialized) {
         if (this.maskDiff != null) {
            this.maskDiff.close();
         }

         if (this.outlineShader != null) {
            this.outlineShader.close();
         }

         if (this.transparencyShader != null) {
            this.transparencyShader.close();
         }

         this.maskDiff = new MaskDiffShader();
         this.outlineShader = new OutlineShader();
         this.transparencyShader = new TransparencyShader();
         this.width = 0;
         this.height = 0;
         this.initialized = true;
      }
   }

   private void resize(int w, int h) {
      if (w != this.width || h != this.height || this.sceneBefore == null) {
         this.closeTextures();
         this.sceneBefore = RenderSystem.getDevice().createTexture(() -> "legoclient:hand_before", 13, TextureFormat.RGBA8, w, h, 1, 1);
         this.sceneBeforeView = RenderSystem.getDevice().createTextureView(this.sceneBefore);
         this.sceneAfter = RenderSystem.getDevice().createTexture(() -> "legoclient:hand_after", 13, TextureFormat.RGBA8, w, h, 1, 1);
         this.sceneAfterView = RenderSystem.getDevice().createTextureView(this.sceneAfter);
         this.depthBefore = RenderSystem.getDevice().createTexture(() -> "legoclient:hand_depth_before", 13, TextureFormat.DEPTH32, w, h, 1, 1);
         this.depthBeforeView = RenderSystem.getDevice().createTextureView(this.depthBefore);
         this.depthAfter = RenderSystem.getDevice().createTexture(() -> "legoclient:hand_depth_after", 13, TextureFormat.DEPTH32, w, h, 1, 1);
         this.depthAfterView = RenderSystem.getDevice().createTextureView(this.depthAfter);
         this.mask = RenderSystem.getDevice().createTexture(() -> "legoclient:hand_mask", 13, TextureFormat.RGBA8, w, h, 1, 1);
         this.maskView = RenderSystem.getDevice().createTextureView(this.mask);
         this.width = w;
         this.height = h;
      }
   }

   public boolean needsCapture() {
      return this.enabled && (this.outlineEnabled || this.transparencyEnabled);
   }

   public void captureBeforeHands() {
      if (this.needsCapture()) {
         this.initialize();
         RenderTarget fb = this.mc.getMainRenderTarget();
         if (fb != null && fb.getColorTexture() != null) {
            int tw = fb.width;
            int th = fb.height;
            this.resize(tw, th);
            CommandEncoder enc = RenderSystem.getDevice().createCommandEncoder();
            enc.copyTextureToTexture(fb.getColorTexture(), this.sceneBefore, 0, 0, 0, 0, 0, tw, th);
            if (fb.getDepthTexture() != null) {
               enc.copyTextureToTexture(fb.getDepthTexture(), this.depthBefore, 0, 0, 0, 0, 0, tw, th);
            }

            this.captureReady = true;
         }
      }
   }

   public void captureAfterHands() {
      if (this.needsCapture() && this.captureReady) {
         RenderTarget fb = this.mc.getMainRenderTarget();
         if (fb != null && fb.getColorTexture() != null) {
            CommandEncoder enc = RenderSystem.getDevice().createCommandEncoder();
            enc.copyTextureToTexture(fb.getColorTexture(), this.sceneAfter, 0, 0, 0, 0, 0, this.width, this.height);
            if (fb.getDepthTexture() != null) {
               enc.copyTextureToTexture(fb.getDepthTexture(), this.depthAfter, 0, 0, 0, 0, 0, this.width, this.height);
            }
         }
      }
   }

   public void compositeEffects() {
      if (this.needsCapture() && this.captureReady) {
         RenderTarget fb = this.mc.getMainRenderTarget();
         if (fb != null && fb.getColorTexture() != null) {
            this.maskDiff
               .renderMask(this.maskView, this.sceneBeforeView, this.sceneAfterView, this.depthBeforeView, this.depthAfterView, this.width, this.height);
            if (this.transparencyEnabled) {
               this.transparencyShader.render(this.sceneBeforeView, this.maskView, fb.getColorTextureView(), this.width, this.height, this.transparencyAlpha);
            }

            if (this.outlineEnabled) {
               this.outlineShader
                  .render(
                     this.maskView,
                     fb.getColorTextureView(),
                     this.width,
                     this.height,
                     this.glowStrength,
                     this.outlineWidth,
                     this.glowMode,
                     this.fillColor,
                     this.outlineColor,
                     this.sceneAfterView,
                     this.useItemColor
                  );
            }

            this.captureReady = false;
         } else {
            this.captureReady = false;
         }
      } else {
         this.captureReady = false;
      }
   }

   public void close() {
      this.closeTextures();
      if (this.maskDiff != null) {
         this.maskDiff.close();
         this.maskDiff = null;
      }

      if (this.outlineShader != null) {
         this.outlineShader.close();
         this.outlineShader = null;
      }

      if (this.transparencyShader != null) {
         this.transparencyShader.close();
         this.transparencyShader = null;
      }

      this.width = 0;
      this.height = 0;
      this.initialized = false;
      this.captureReady = false;
   }

   private void closeTextures() {
      if (this.sceneBeforeView != null) {
         this.sceneBeforeView.close();
         this.sceneBeforeView = null;
      }

      if (this.sceneBefore != null) {
         this.sceneBefore.close();
         this.sceneBefore = null;
      }

      if (this.sceneAfterView != null) {
         this.sceneAfterView.close();
         this.sceneAfterView = null;
      }

      if (this.sceneAfter != null) {
         this.sceneAfter.close();
         this.sceneAfter = null;
      }

      if (this.depthBeforeView != null) {
         this.depthBeforeView.close();
         this.depthBeforeView = null;
      }

      if (this.depthBefore != null) {
         this.depthBefore.close();
         this.depthBefore = null;
      }

      if (this.depthAfterView != null) {
         this.depthAfterView.close();
         this.depthAfterView = null;
      }

      if (this.depthAfter != null) {
         this.depthAfter.close();
         this.depthAfter = null;
      }

      if (this.maskView != null) {
         this.maskView.close();
         this.maskView = null;
      }

      if (this.mask != null) {
         this.mask.close();
         this.mask = null;
      }
   }
}
