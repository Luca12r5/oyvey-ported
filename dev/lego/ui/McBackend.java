package dev.lego.ui;

import com.mojang.blaze3d.platform.Window;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public final class McBackend implements Gx.Backend {
   public static final McBackend INSTANCE = new McBackend();
   private GuiGraphics ctx;
   private static int counter = 0;
   private static final Map<Identifier, DynamicTexture> TEX = new HashMap<>();

   private McBackend() {
   }

   public static void begin(GuiGraphics c) {
      INSTANCE.ctx = c;
      Gx.begin(INSTANCE);
   }

   @Override
   public int guiScale() {
      try {
         return Minecraft.getInstance().getWindow().getGuiScale();
      } catch (Throwable var2) {
         return 2;
      }
   }

   @Override
   public void push() {
      this.ctx.pose().pushMatrix();
   }

   @Override
   public void pop() {
      this.ctx.pose().popMatrix();
   }

   @Override
   public void translate(float x, float y) {
      this.ctx.pose().translate(x, y);
   }

   @Override
   public void scale(float x, float y) {
      this.ctx.pose().scale(x, y);
   }

   @Override
   public void fill(int x, int y, int w, int h, int argb) {
      this.ctx.fill(x, y, x + w, y + h, argb);
   }

   @Override
   public void gradV(int x, int y, int w, int h, int top, int bottom) {
      this.ctx.fillGradient(x, y, x + w, y + h, top, bottom);
   }

   @Override
   public void tex(Gx.Img t, int x, int y, int w, int h, int u, int v, int rw, int rh, int argb) {
      this.ctx.blit(RenderPipelines.GUI_TEXTURED, (Identifier)t.handle, x, y, u, v, w, h, rw, rh, t.w, t.h, argb);
   }

   public void texture(Identifier id, int x, int y, int w, int h, int texW, int texH, int argb) {
      this.ctx.blit(RenderPipelines.GUI_TEXTURED, id, x, y, 0.0F, 0.0F, w, h, texW, texH, texW, texH, argb);
   }

   @Override
   public void clip(int x, int y, int w, int h) {
      this.ctx.enableScissor(x, y, x + w, y + h);
   }

   @Override
   public void unclip() {
      this.ctx.disableScissor();
   }

   @Override
   public Object register(int[] argb, int w, int h) {
      Identifier id = Identifier.fromNamespaceAndPath("legoclient", "ui/" + counter++);
      TEX.put(id, Tx.register(id, argb, w, h, true));
      return id;
   }

   @Override
   public void update(Gx.Img t, int[] argb) {
      DynamicTexture tex = TEX.get((Identifier)t.handle);
      if (tex != null) {
         Tx.update(tex, argb, t.w, t.h);
      }
   }

   @Override
   public void destroy(Gx.Img t) {
      TEX.remove((Identifier)t.handle);
      Tx.destroy((Identifier)t.handle);
   }

   @Override
   public int[] screenPx() {
      try {
         Window w = Minecraft.getInstance().getWindow();
         return new int[]{w.getWidth(), w.getHeight()};
      } catch (Throwable var2) {
         return new int[]{1920, 1080};
      }
   }

   public GuiGraphics ctx() {
      return this.ctx;
   }
}
