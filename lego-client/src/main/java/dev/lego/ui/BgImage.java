package dev.lego.ui;

import dev.lego.LegoClient;
import dev.lego.core.Modules;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public final class BgImage {
   private static Identifier texId;
   private static Gx.Img cachedImg;
   private static String loadedPath = "";
   private static int imgW;
   private static int imgH;

   private BgImage() {
   }

   public static void pick() {
      new Thread(() -> {
         try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = stack.mallocPointer(4);
            filters.put(stack.UTF8("*.png"));
            filters.put(stack.UTF8("*.jpg"));
            filters.put(stack.UTF8("*.jpeg"));
            filters.put(stack.UTF8("*.bmp"));
            filters.flip();
            String result = TinyFileDialogs.tinyfd_openFileDialog("Hintergrundbild wählen", "", filters, "Bilder (png, jpg, bmp)", false);
            if (result != null && !result.isEmpty()) {
               UiSettings.bgImage = result;
               Modules.scheduleSave();
               loadedPath = "";
            }
         } catch (Throwable var5) {
            LegoClient.LOG("Hintergrundbild-Dialog: " + var5);
         }
      }, "bg-pick").start();
   }

   public static void clear() {
      loadedPath = "";
      if (texId != null) {
         Tx.destroy(texId);
         texId = null;
         cachedImg = null;
      }
   }

   private static void load(String path) {
      if (!path.equals(loadedPath)) {
         loadedPath = path;
         if (texId != null) {
            Tx.destroy(texId);
            texId = null;
         }

         if (!path.isEmpty()) {
            new Thread(() -> {
               try {
                  BufferedImage raw = ImageIO.read(new File(path));
                  if (raw == null) {
                     return;
                  }

                  int w = raw.getWidth();
                  int h = raw.getHeight();
                  if (w > 2048 || h > 2048) {
                     double s = Math.min(2048.0 / w, 2048.0 / h);
                     w = (int)(w * s);
                     h = (int)(h * s);
                  }

                  BufferedImage argb = new BufferedImage(w, h, 2);
                  Graphics2D g = argb.createGraphics();
                  g.drawImage(raw, 0, 0, w, h, null);
                  g.dispose();
                  int[] px = argb.getRGB(0, 0, w, h, null, 0, w);
                  imgW = w;
                  imgH = h;
                  Identifier id = Identifier.fromNamespaceAndPath("legoclient", "bg_user");
                  int fw = w;
                  int fh = h;
                  Minecraft.getInstance().execute(() -> {
                     try {
                        Tx.register(id, px, fw, fh, true);
                        texId = id;
                        cachedImg = new Gx.Img(id, fw, fh, 0);
                     } catch (Throwable var5x) {
                        LegoClient.LOG("BG-Textur: " + var5x);
                     }
                  });
               } catch (Throwable var10) {
                  LegoClient.LOG("Hintergrundbild laden: " + var10);
               }
            }, "bg-load").start();
         }
      }
   }

   public static void render(int W, int H) {
      String path = UiSettings.bgImage;
      if (!path.isEmpty()) {
         load(path);
         if (texId != null && cachedImg != null) {
            Gx.Img img = cachedImg;
            float ar = (float)imgW / imgH;
            float sar = (float)W / H;
            int dw;
            int dh;
            if (ar > sar) {
               dh = H;
               dw = Math.round(H * ar);
            } else {
               dw = W;
               dh = Math.round(W / ar);
            }

            Gx.image(img, (W - dw) / 2, (H - dh) / 2, dw, dh, -1);
         }
      }
   }
}
