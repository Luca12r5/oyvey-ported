package dev.lego.cosmetic;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import javax.imageio.ImageIO;

/**
 * Runs the LEGO client's own cosmetic models (G builder, no Minecraft needed) and
 * writes their geometry and textures for the launcher's 3D preview, so the
 * launcher shows exactly the models the game draws.
 *
 * Per item one .bin: "LC3D", uint32 header length, JSON header, then per group
 * int16 positions (x100, 4 vertices × xyz per quad), int16 UVs (x4096),
 * uint8 RGBA per quad. Textures as PNG (max 128 px), index.json lists items.
 */
public final class Export3D {
   private static final float[] TIMES = {0.0F, 0.35F, 0.7F, 1.05F, 1.4F, 1.75F};

   record Q(float[] p, float[] uv, int color) {
   }

   public static void main(String[] args) throws Exception {
      File out = new File(args[0]);
      File items = new File(out, "items");
      File tex = new File(out, "textures");
      items.mkdirs();
      tex.mkdirs();
      Cos.init();
      TreeSet<String> textures = new TreeSet<>();
      StringBuilder index = new StringBuilder("{\"items\":[");
      boolean first = true;
      long bytes = 0;
      for (Cos.Item it : Cos.ALL) {
         if (it.slot == Cos.Slot.CAPE) continue;
         // Animated slots get a few frames (wings flap, auras orbit); the rest one.
         boolean animated = it.slot == Cos.Slot.WINGS || it.slot == Cos.Slot.AURA;
         float[] times = animated ? TIMES : new float[]{0.0F};
         List<Map<String, Map<Boolean, List<Q>>>> frames = new ArrayList<>();
         boolean failed = false;
         for (float t : times) {
            Map<String, Map<Boolean, List<Q>>> groups = new LinkedHashMap<>();
            G g = new G((key, p, uv, color, glow, nx, ny, nz) -> groups.computeIfAbsent(key, k -> new LinkedHashMap<>()).computeIfAbsent(glow, k -> new ArrayList<>()).add(new Q(p, uv, color)));
            Cos.A a = new Cos.A();
            a.preview = true;
            a.time = t;
            try {
               g.color(-1).glow(false);
               it.model.render(g, a);
            } catch (Throwable e) {
               System.err.println("skip " + it.id + ": " + e);
               failed = true;
               break;
            }
            frames.add(groups);
         }
         if (failed || frames.get(0).isEmpty()) continue;
         ByteArrayOutputStream data = new ByteArrayOutputStream();
         StringBuilder head = new StringBuilder();
         head.append("{\"id\":\"").append(it.id).append("\",\"slot\":\"").append(it.slot.name().toLowerCase()).append("\",\"frames\":[");
         for (int f = 0; f < frames.size(); f++) {
            if (f > 0) head.append(',');
            head.append('[');
            boolean gf = true;
            for (Map.Entry<String, Map<Boolean, List<Q>>> e : frames.get(f).entrySet()) {
               for (Map.Entry<Boolean, List<Q>> ge : e.getValue().entrySet()) {
                  List<Q> qs = ge.getValue();
                  if (!e.getKey().equals("@skin")) textures.add(e.getKey());
                  if (!gf) head.append(',');
                  gf = false;
                  head.append("{\"tex\":\"").append(e.getKey().replace("\\", "\\\\").replace("\"", "\\\"")).append("\",\"glow\":").append(ge.getKey()).append(",\"quads\":").append(qs.size()).append('}');
                  ByteBuffer b = ByteBuffer.allocate(qs.size() * (24 + 16 + 4)).order(ByteOrder.LITTLE_ENDIAN);
                  for (Q q : qs) for (float v : q.p) b.putShort((short)Math.max(-32768, Math.min(32767, Math.round(v * 100))));
                  for (Q q : qs) for (float v : q.uv) b.putShort((short)Math.max(-32768, Math.min(32767, Math.round(v * 4096))));
                  for (Q q : qs) {
                     int c = q.color;
                     b.put((byte)(c >> 16)).put((byte)(c >> 8)).put((byte)c).put((byte)(c >>> 24));
                  }
                  data.write(b.array());
               }
            }
            head.append(']');
         }
         head.append("]}");
         byte[] h = head.toString().getBytes(StandardCharsets.UTF_8);
         try (FileOutputStream fo = new FileOutputStream(new File(items, it.id + ".bin"))) {
            ByteBuffer pre = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            pre.put("LC3D".getBytes(StandardCharsets.US_ASCII)).putInt(h.length);
            fo.write(pre.array());
            fo.write(h);
            int pad = (8 + h.length) % 2;
            if (pad != 0) fo.write(0);
            fo.write(data.toByteArray());
            bytes += 8 + h.length + pad + data.size();
         }
         if (!first) index.append(',');
         first = false;
         index.append("{\"id\":\"").append(it.id).append("\",\"slot\":\"").append(it.slot.name().toLowerCase()).append("\",\"frames\":").append(frames.size()).append('}');
      }
      index.append("],\"textures\":{");
      boolean tf = true;
      for (String key : textures) {
         CTex.T t;
         try {
            t = CTex.get(key);
         } catch (Throwable e) {
            System.err.println("texture " + key + ": " + e);
            continue;
         }
         if (t == null) continue;
         String file = key.replaceAll("[^A-Za-z0-9_-]", "_") + ".png";
         BufferedImage img = new BufferedImage(t.w, t.h, BufferedImage.TYPE_INT_ARGB);
         img.setRGB(0, 0, t.w, t.h, t.argb, 0, t.w);
         int max = Math.max(t.w, t.h);
         if (max > 128) {
            int w = Math.max(1, t.w * 128 / max), hh = Math.max(1, t.h * 128 / max);
            BufferedImage s = new BufferedImage(w, hh, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g2 = s.createGraphics();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(img, 0, 0, w, hh, null);
            g2.dispose();
            img = s;
         }
         ImageIO.write(img, "png", new File(tex, file));
         if (!tf) index.append(',');
         tf = false;
         index.append('"').append(key.replace("\\", "\\\\").replace("\"", "\\\"")).append("\":\"").append(file).append('"');
      }
      index.append("}}\n");
      try (FileOutputStream fo = new FileOutputStream(new File(out, "index.json"))) {
         fo.write(index.toString().getBytes(StandardCharsets.UTF_8));
      }
      System.out.println("geometry " + bytes / 1024 + " KB, textures " + textures.size());
      System.exit(0);
   }
}
