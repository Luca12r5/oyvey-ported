// Minecraft 26.3 version of dev/lego/visual/shader/HandEffect.java.
package dev.lego.visual.shader;

/**
 * The hand glow/outline/transparency shaders are written against the 1.21.11 blaze3d API; on 26.3
 * they are not built yet. This keeps the module API so the settings work and simply draws nothing.
 */
public class HandEffect {
   private static final HandEffect INSTANCE = new HandEffect();
   private boolean enabled;
   private boolean transparency;

   public static HandEffect getInstance() {
      return INSTANCE;
   }

   public void setEnabled(boolean v) {
      this.enabled = v;
   }

   public void setOutlineEnabled(boolean v) {
   }

   public void setGlowStrength(float v) {
   }

   public void setGlowColors(int c) {
   }

   public void setUseItemColor(boolean v) {
   }

   public void setOutlineWidth(int v) {
   }

   public void setGlowMode(int v) {
   }

   public void setTransparencyEnabled(boolean v) {
      this.transparency = v;
   }

   public void setTransparencyAlpha(float v) {
   }

   public boolean isTransparencyActive() {
      return false;
   }

   public boolean needsCapture() {
      return false;
   }

   public void captureBeforeHands() {
   }

   public void captureAfterHands() {
   }

   public void compositeEffects() {
   }

   public void close() {
   }
}
