package dev.spotifyhud;

import com.mojang.blaze3d.platform.NativeImage;
import java.nio.IntBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryUtil;

public final class TexSlot {
   private static final ExecutorService PAINTER = Executors.newSingleThreadExecutor(var0 -> {
      Thread var1 = new Thread(var0, "LegoClient-Painter");
      var1.setDaemon(true);
      var1.setPriority(4);
      return var1;
   });
   private static volatile boolean fastUpload = true;
   final Identifier id;
   private DynamicTexture tex;
   private int w;
   private int h;
   private String shownKey = null;
   private volatile boolean busy;
   private volatile int[] ready;
   private volatile String readyKey;
   private volatile int readyW;
   private volatile int readyH;
   private String requestedKey;

   public TexSlot(Identifier var1) {
      this.id = var1;
   }

   public void draw(GuiGraphics var1, Minecraft var2, int var3, int var4, int var5, int var6, int var7, int var8, String var9, Supplier<int[]> var10) {
      if (var7 > 0 && var8 > 0) {
         String var11 = var7 + "x" + var8 + "|" + var9;
         int[] var12 = this.ready;
         if (var12 != null && this.readyW == var7 && this.readyH == var8) {
            this.ready = null;
            this.upload(var2, var12, var7, var8);
            this.shownKey = this.readyKey;
         } else if (var12 != null) {
            this.ready = null;
         }

         if (!var11.equals(this.shownKey) && !this.busy && !var11.equals(this.requestedKey)) {
            this.busy = true;
            this.requestedKey = var11;
            PAINTER.execute(() -> {
               try {
                  int[] var5x = (int[])var10.get();
                  if (var5x != null && var5x.length == var7 * var8) {
                     toAbgr(var5x);
                     this.readyW = var7;
                     this.readyH = var8;
                     this.readyKey = var11;
                     this.ready = var5x;
                  }
               } catch (Throwable var9x) {
                  SpotifyHudMod.LOG("Painter-Fehler: " + var9x);
               } finally {
                  this.busy = false;
               }
            });
         } else if (var11.equals(this.requestedKey) && !this.busy && this.ready == null && !var11.equals(this.shownKey)) {
            this.requestedKey = null;
         }

         if (this.tex != null && this.shownKey != null) {
            var1.blit(RenderPipelines.GUI_TEXTURED, this.id, var3, var4, 0.0F, 0.0F, var5, var6, this.w, this.h, this.w, this.h);
         }
      }
   }

   private void upload(Minecraft var1, int[] var2, int var3, int var4) {
      TextureManager var5 = var1.getTextureManager();
      if (this.tex == null || this.w != var3 || this.h != var4) {
         if (this.tex != null) {
            var5.release(this.id);
         }

         NativeImage var6 = new NativeImage(var3, var4, true);
         this.tex = new DynamicTexture(() -> "legoclient_" + this.id.hashCode(), var6);
         var5.register(this.id, this.tex);
         this.w = var3;
         this.h = var4;
      }

      NativeImage var13 = this.tex.getPixels();
      boolean var7 = false;
      if (fastUpload) {
         try {
            long var8 = var13.getPointer();
            if (var8 != 0L) {
               IntBuffer var10 = MemoryUtil.memIntBuffer(var8, var3 * var4);
               var10.put(var2, 0, var3 * var4);
               var7 = true;
            }
         } catch (Throwable var12) {
            fastUpload = false;
            SpotifyHudMod.LOG("Schneller Upload nicht verfügbar, nutze Fallback: " + var12);
         }
      }

      if (!var7) {
         for (int var14 = 0; var14 < var4; var14++) {
            int var9 = var14 * var3;

            for (int var15 = 0; var15 < var3; var15++) {
               int var11 = var2[var9 + var15];
               var13.setPixel(var15, var14, var11 & -16711936 | (var11 & 0xFF) << 16 | var11 >> 16 & 0xFF);
            }
         }
      }

      this.tex.upload();
   }

   private static void toAbgr(int[] var0) {
      for (int var1 = 0; var1 < var0.length; var1++) {
         int var2 = var0[var1];
         var0[var1] = var2 & -16711936 | (var2 & 0xFF) << 16 | var2 >> 16 & 0xFF;
      }
   }

   public void invalidate() {
      this.shownKey = null;
      this.requestedKey = null;
   }
}
