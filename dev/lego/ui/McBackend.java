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

   public static void begin(GuiGraphics var0) {
      INSTANCE.ctx = var0;
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
   public void translate(float var1, float var2) {
      this.ctx.pose().translate(var1, var2);
   }

   @Override
   public void scale(float var1, float var2) {
      this.ctx.pose().scale(var1, var2);
   }

   @Override
   public void fill(int var1, int var2, int var3, int var4, int var5) {
      this.ctx.fill(var1, var2, var1 + var3, var2 + var4, var5);
   }

   @Override
   public void gradV(int var1, int var2, int var3, int var4, int var5, int var6) {
      this.ctx.fillGradient(var1, var2, var1 + var3, var2 + var4, var5, var6);
   }

   @Override
   public void tex(Gx.Img var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10) {
      this.ctx.blit(RenderPipelines.GUI_TEXTURED, (Identifier)var1.handle, var2, var3, var6, var7, var4, var5, var8, var9, var1.w, var1.h, var10);
   }

   public void texture(Identifier var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      this.ctx.blit(RenderPipelines.GUI_TEXTURED, var1, var2, var3, 0.0F, 0.0F, var4, var5, var6, var7, var6, var7, var8);
   }

   @Override
   public void clip(int var1, int var2, int var3, int var4) {
      this.ctx.enableScissor(var1, var2, var1 + var3, var2 + var4);
   }

   @Override
   public void unclip() {
      this.ctx.disableScissor();
   }

   @Override
   public Object register(int[] var1, int var2, int var3) {
      Identifier var4 = Identifier.fromNamespaceAndPath("legoclient", "ui/" + counter++);
      TEX.put(var4, Tx.register(var4, var1, var2, var3, true));
      return var4;
   }

   @Override
   public void update(Gx.Img var1, int[] var2) {
      DynamicTexture var3 = TEX.get((Identifier)var1.handle);
      if (var3 != null) {
         Tx.update(var3, var2, var1.w, var1.h);
      }
   }

   @Override
   public void destroy(Gx.Img var1) {
      TEX.remove((Identifier)var1.handle);
      Tx.destroy((Identifier)var1.handle);
   }

   @Override
   public int[] screenPx() {
      try {
         Window var1 = Minecraft.getInstance().getWindow();
         return new int[]{var1.getWidth(), var1.getHeight()};
      } catch (Throwable var2) {
         return new int[]{1920, 1080};
      }
   }

   public GuiGraphics ctx() {
      return this.ctx;
   }
}
