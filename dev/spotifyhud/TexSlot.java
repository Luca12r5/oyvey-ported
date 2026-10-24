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
   private static final ExecutorService PAINTER = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "LegoClient-Painter");
      t.setDaemon(true);
      t.setPriority(4);
      return t;
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

   public TexSlot(Identifier id) {
      this.id = id;
   }

   public void draw(GuiGraphics ctx, Minecraft mc, int x, int y, int gw, int gh, int pw, int ph, String k, Supplier<int[]> painter) {
      if (pw > 0 && ph > 0) {
         String fullKey = pw + "x" + ph + "|" + k;
         int[] px = this.ready;
         if (px != null && this.readyW == pw && this.readyH == ph) {
            this.ready = null;
            this.upload(mc, px, pw, ph);
            this.shownKey = this.readyKey;
         } else if (px != null) {
            this.ready = null;
         }

         if (!fullKey.equals(this.shownKey) && !this.busy && !fullKey.equals(this.requestedKey)) {
            this.busy = true;
            this.requestedKey = fullKey;
            PAINTER.execute(() -> {
               try {
                  int[] argb = painter.get();
                  if (argb != null && argb.length == pw * ph) {
                     toAbgr(argb);
                     this.readyW = pw;
                     this.readyH = ph;
                     this.readyKey = fullKey;
                     this.ready = argb;
                  }
               } catch (Throwable var9) {
                  SpotifyHudMod.LOG("Painter-Fehler: " + var9);
               } finally {
                  this.busy = false;
               }
            });
         } else if (fullKey.equals(this.requestedKey) && !this.busy && this.ready == null && !fullKey.equals(this.shownKey)) {
            this.requestedKey = null;
         }

         if (this.tex != null && this.shownKey != null) {
            ctx.blit(RenderPipelines.GUI_TEXTURED, this.id, x, y, 0.0F, 0.0F, gw, gh, this.w, this.h, this.w, this.h);
         }
      }
   }

   private void upload(Minecraft mc, int[] abgr, int pw, int ph) {
      TextureManager tm = mc.getTextureManager();
      if (this.tex == null || this.w != pw || this.h != ph) {
         if (this.tex != null) {
            tm.release(this.id);
         }

         NativeImage img = new NativeImage(pw, ph, true);
         this.tex = new DynamicTexture(() -> "legoclient_" + this.id.hashCode(), img);
         tm.register(this.id, this.tex);
         this.w = pw;
         this.h = ph;
      }

      NativeImage img = this.tex.getPixels();
      boolean done = false;
      if (fastUpload) {
         try {
            long ptr = img.getPointer();
            if (ptr != 0L) {
               IntBuffer buf = MemoryUtil.memIntBuffer(ptr, pw * ph);
               buf.put(abgr, 0, pw * ph);
               done = true;
            }
         } catch (Throwable var12) {
            fastUpload = false;
            SpotifyHudMod.LOG("Schneller Upload nicht verfügbar, nutze Fallback: " + var12);
         }
      }

      if (!done) {
         for (int yy = 0; yy < ph; yy++) {
            int row = yy * pw;

            for (int xx = 0; xx < pw; xx++) {
               int c = abgr[row + xx];
               img.setPixel(xx, yy, c & -16711936 | (c & 0xFF) << 16 | c >> 16 & 0xFF);
            }
         }
      }

      this.tex.upload();
   }

   private static void toAbgr(int[] px) {
      for (int i = 0; i < px.length; i++) {
         int c = px[i];
         px[i] = c & -16711936 | (c & 0xFF) << 16 | c >> 16 & 0xFF;
      }
   }

   public void invalidate() {
      this.shownKey = null;
      this.requestedKey = null;
   }
}
