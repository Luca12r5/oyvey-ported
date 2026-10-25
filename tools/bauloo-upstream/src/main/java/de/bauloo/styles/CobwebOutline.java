package de.bauloo.styles;

import de.bauloo.styles.mixin.SpriteContentsAccessor;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents.AfterEntities;
import net.minecraft.class_1011;
import net.minecraft.class_1058;
import net.minecraft.class_12249;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4722;
import net.minecraft.class_638;
import org.joml.Matrix4f;

public final class CobwebOutline {
   private static final int COLOR = -251658241;
   private static final int CHUNK_RADIUS = 3;
   private static final int SECTION_RADIUS = 3;
   private static final int MAX_BOXES = 600;
   private static List<class_2338> webs = List.of();
   private static int tint = 16777215;
   private static Object tintSource;
   private static int tintCached = 16777215;

   private CobwebOutline() {
   }

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         StylesUiConfig.load();
         if (client.field_1687 != null && client.field_1724 != null && StylesUiConfig.cobwebOutline) {
            webs = scan(client.field_1687, client.field_1724.method_24515());
         } else {
            if (!webs.isEmpty()) {
               webs = List.of();
            }
         }
      });
      WorldRenderEvents.AFTER_ENTITIES
         .register(
            (AfterEntities)context -> {
               try {
                  if (webs.isEmpty() || !StylesUiConfig.cobwebOutline) {
                     return;
                  }

                  class_4587 matrices = context.matrices();
                  class_4597 consumers = context.consumers();
                  class_243 cam = context.worldState().field_63082.field_63078;
                  if (matrices == null || consumers == null || cam == null) {
                     return;
                  }

                  class_4588 buffer = consumers.method_73477(class_12249.method_76023());
                  Matrix4f m = matrices.method_23760().method_23761();
                  tint = webColor();

                  for (class_2338 p : webs) {
                     frame(
                        buffer,
                        m,
                        (float)(p.method_10263() - cam.field_1352),
                        (float)(p.method_10264() - cam.field_1351),
                        (float)(p.method_10260() - cam.field_1350)
                     );
                  }
               } catch (Throwable var8) {
               }
            }
         );
   }

   public static int color() {
      return webColor();
   }

   private static int webColor() {
      try {
         class_1058 sprite = class_310.method_1551().method_72703().method_73030(class_4722.field_56356.method_67274("cobweb"));
         class_1011 image = ((SpriteContentsAccessor)sprite.method_45851()).baulooStyles$image();
         if (image == null) {
            return 16777215;
         }

         if (image == tintSource) {
            return tintCached;
         }

         int w = sprite.method_45851().method_45807();
         int h = sprite.method_45851().method_45815();
         long r = 0L;
         long g = 0L;
         long b = 0L;
         long n = 0L;

         for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
               int c = image.method_61940(x, y);
               int a = c >>> 24;
               if (a >= 64) {
                  r += c >> 16 & 0xFF;
                  g += c >> 8 & 0xFF;
                  b += c & 0xFF;
                  n++;
               }
            }
         }

         int color = 16777215;
         if (n > 0L) {
            float[] hsb = Color.RGBtoHSB((int)(r / n), (int)(g / n), (int)(b / n), null);
            color = hsb[1] < 0.1F ? 16777215 : Color.HSBtoRGB(hsb[0], Math.max(0.55F, hsb[1]), 1.0F) & 16777215;
         }

         tintSource = image;
         tintCached = color;
         return color;
      } catch (Throwable t) {
         return 16777215;
      }
   }

   private static void frame(class_4588 b, Matrix4f m, float x, float y, float z) {
      float T = StylesUiConfig.cobwebOutlineWidth / 64.0F;
      float i = 1.0F - T;

      for (float a : new float[]{0.0F, i}) {
         for (float c : new float[]{0.0F, i}) {
            bar(b, m, x, y + a, z + c, x + 1.0F, y + a + T, z + c + T);
            bar(b, m, x + a, y, z + c, x + a + T, y + 1.0F, z + c + T);
            bar(b, m, x + a, y + c, z, x + a + T, y + c + T, z + 1.0F);
         }
      }
   }

   private static void bar(class_4588 b, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1) {
      int a = 240;
      quad(b, m, a, 1.0F, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
      quad(b, m, a, 0.55F, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
      quad(b, m, a, 0.8F, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
      quad(b, m, a, 0.8F, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
      quad(b, m, a, 0.68F, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
      quad(b, m, a, 0.68F, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
   }

   private static void quad(
      class_4588 b,
      Matrix4f m,
      int alpha,
      float shade,
      float ax,
      float ay,
      float az,
      float bx,
      float by,
      float bz,
      float cx,
      float cy,
      float cz,
      float dx,
      float dy,
      float dz
   ) {
      int r = Math.round((tint >> 16 & 0xFF) * shade);
      int g = Math.round((tint >> 8 & 0xFF) * shade);
      int bl = Math.round((tint & 0xFF) * shade);
      b.method_22918(m, ax, ay, az).method_1336(r, g, bl, alpha);
      b.method_22918(m, bx, by, bz).method_1336(r, g, bl, alpha);
      b.method_22918(m, cx, cy, cz).method_1336(r, g, bl, alpha);
      b.method_22918(m, dx, dy, dz).method_1336(r, g, bl, alpha);
   }

   private static List<class_2338> scan(class_638 world, class_2338 center) {
      List<class_2338> found = new ArrayList<>();
      int ccx = center.method_10263() >> 4;
      int ccz = center.method_10260() >> 4;
      int csy = center.method_10264() >> 4;

      for (int cx = ccx - 3; cx <= ccx + 3; cx++) {
         for (int cz = ccz - 3; cz <= ccz + 3; cz++) {
            if (world.method_2935().method_12123(cx, cz)) {
               class_2818 chunk = world.method_8497(cx, cz);

               for (int sy = Math.max(world.method_32891(), csy - 3); sy <= Math.min(world.method_31597(), csy + 3); sy++) {
                  class_2826 section = chunk.method_38259(world.method_31603(sy));
                  if (!section.method_38292() && section.method_19523(s -> s.method_27852(class_2246.field_10343))) {
                     for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                           for (int x = 0; x < 16; x++) {
                              if (section.method_12254(x, y, z).method_27852(class_2246.field_10343)) {
                                 found.add(new class_2338((cx << 4) + x, (sy << 4) + y, (cz << 4) + z));
                                 if (found.size() >= 600) {
                                    return found;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return found;
   }
}
