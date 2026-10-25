package de.bauloo.styles;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.bauloo.styles.mixin.SpriteContentsAccessor;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class CobwebOutline {
   private static final int COLOR = -251658241;
   private static final int CHUNK_RADIUS = 3;
   private static final int SECTION_RADIUS = 3;
   private static final int MAX_BOXES = 600;
   private static List<BlockPos> webs = List.of();
   private static int tint = 16777215;
   private static Object tintSource;
   private static int tintCached = 16777215;

   private CobwebOutline() {
   }

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK.register(client -> {
         StylesUiConfig.load();
         if (client.level != null && client.player != null && StylesUiConfig.cobwebOutline) {
            webs = scan(client.level, client.player.blockPosition());
         } else if (!webs.isEmpty()) {
            webs = List.of();
         }
      });
      WorldRenderEvents.AFTER_ENTITIES.register(context -> {
         try {
            if (webs.isEmpty() || !StylesUiConfig.cobwebOutline) {
               return;
            }

            PoseStack matrices = context.matrices();
            MultiBufferSource consumers = context.consumers();
            Vec3 cam = context.worldState().cameraRenderState.pos;
            if (matrices == null || consumers == null || cam == null) {
               return;
            }

            VertexConsumer buffer = consumers.getBuffer(RenderTypes.debugQuads());
            Matrix4f m = matrices.last().pose();
            tint = webColor();

            for (BlockPos p : webs) {
               frame(buffer, m, (float)(p.getX() - cam.x), (float)(p.getY() - cam.y), (float)(p.getZ() - cam.z));
            }
         } catch (Throwable var8) {
         }
      });
   }

   public static int color() {
      return webColor();
   }

   private static int webColor() {
      try {
         TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().get(Sheets.BLOCKS_MAPPER.defaultNamespaceApply("cobweb"));
         NativeImage image = ((SpriteContentsAccessor)sprite.contents()).baulooStyles$image();
         if (image == null) {
            return 16777215;
         } else if (image == tintSource) {
            return tintCached;
         } else {
            int w = sprite.contents().width();
            int h = sprite.contents().height();
            long r = 0L;
            long g = 0L;
            long b = 0L;
            long n = 0L;

            for (int y = 0; y < h; y++) {
               for (int x = 0; x < w; x++) {
                  int c = image.getPixel(x, y);
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
         }
      } catch (Throwable var16) {
         return 16777215;
      }
   }

   private static void frame(VertexConsumer b, Matrix4f m, float x, float y, float z) {
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

   private static void bar(VertexConsumer b, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1) {
      int a = 240;
      quad(b, m, a, 1.0F, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
      quad(b, m, a, 0.55F, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
      quad(b, m, a, 0.8F, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
      quad(b, m, a, 0.8F, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
      quad(b, m, a, 0.68F, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
      quad(b, m, a, 0.68F, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
   }

   private static void quad(
      VertexConsumer b,
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
      b.addVertex(m, ax, ay, az).setColor(r, g, bl, alpha);
      b.addVertex(m, bx, by, bz).setColor(r, g, bl, alpha);
      b.addVertex(m, cx, cy, cz).setColor(r, g, bl, alpha);
      b.addVertex(m, dx, dy, dz).setColor(r, g, bl, alpha);
   }

   private static List<BlockPos> scan(ClientLevel world, BlockPos center) {
      List<BlockPos> found = new ArrayList<>();
      int ccx = center.getX() >> 4;
      int ccz = center.getZ() >> 4;
      int csy = center.getY() >> 4;

      for (int cx = ccx - 3; cx <= ccx + 3; cx++) {
         for (int cz = ccz - 3; cz <= ccz + 3; cz++) {
            if (world.getChunkSource().hasChunk(cx, cz)) {
               LevelChunk chunk = world.getChunk(cx, cz);

               for (int sy = Math.max(world.getMinSectionY(), csy - 3); sy <= Math.min(world.getMaxSectionY(), csy + 3); sy++) {
                  LevelChunkSection section = chunk.getSection(world.getSectionIndexFromSectionY(sy));
                  if (!section.hasOnlyAir() && section.maybeHas(s -> s.is(Blocks.COBWEB))) {
                     for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                           for (int x = 0; x < 16; x++) {
                              if (section.getBlockState(x, y, z).is(Blocks.COBWEB)) {
                                 found.add(new BlockPos((cx << 4) + x, (sy << 4) + y, (cz << 4) + z));
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
