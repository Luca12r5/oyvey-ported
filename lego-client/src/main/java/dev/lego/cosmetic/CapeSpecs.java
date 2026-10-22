package dev.lego.cosmetic;

import dev.spotifyhud.Json;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Capes drawn from design data ("CapeSpec"): the modern catalog capes (cape_m_*)
 * and capes players designed in the launcher (shared over the LEGO backend as
 * validated data, never as images). Port of packages/shared/src/cape.ts; the
 * symbol shapes, font and presets come from assets/legoclient/data/capes.json,
 * which tools/gen-cape-assets.mjs writes from the same source.
 */
public final class CapeSpecs {
   static final int FW = 80;
   static final int FH = 128;
   private static final double[][] SS = {{0.25, 0.25}, {0.75, 0.25}, {0.25, 0.75}, {0.75, 0.75}};

   /** Symbol id -> parts (op add/sub, polygon as x0,y0,x1,y1,...). */
   private static final Map<String, List<Part>> SYMBOLS = new LinkedHashMap<>();
   private static final Map<String, String[]> FONT = new LinkedHashMap<>();
   private static final Map<String, Object> PRESETS = new LinkedHashMap<>();
   private static final Map<String, String> NAMES = new LinkedHashMap<>();
   /** Designed capes seen via the backend, by spec id. */
   private static final Map<String, Object> CUSTOM = new ConcurrentHashMap<>();

   private record Part(boolean add, double[] poly) {
   }

   private CapeSpecs() {
   }

   static {
      try (InputStream in = CapeSpecs.class.getResourceAsStream("/assets/legoclient/data/capes.json")) {
         if (in != null) {
            Object root = Json.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            Map<String, Object> sym = Json.obj(root, "symbols");
            if (sym != null) {
               for (Map.Entry<String, Object> e : sym.entrySet()) {
                  List<Part> parts = new ArrayList<>();
                  if (e.getValue() instanceof List<?> list) {
                     for (Object p : list) {
                        List<Object> pts = Json.arr(p, "poly");
                        if (pts == null) continue;
                        double[] poly = new double[pts.size() * 2];
                        for (int i = 0; i < pts.size(); i++) {
                           if (pts.get(i) instanceof List<?> xy && xy.size() == 2) {
                              poly[i * 2] = ((Number)xy.get(0)).doubleValue();
                              poly[i * 2 + 1] = ((Number)xy.get(1)).doubleValue();
                           }
                        }
                        parts.add(new Part("add".equals(Json.str(p, "op", "add")), poly));
                     }
                  }
                  SYMBOLS.put(e.getKey(), parts);
               }
            }
            Map<String, Object> font = Json.obj(root, "font");
            if (font != null) {
               for (Map.Entry<String, Object> e : font.entrySet()) {
                  if (e.getValue() instanceof List<?> rows) FONT.put(e.getKey(), rows.stream().map(String::valueOf).toArray(String[]::new));
               }
            }
            Map<String, Object> presets = Json.obj(root, "presets");
            if (presets != null) PRESETS.putAll(presets);
            Map<String, Object> names = Json.obj(root, "names");
            if (names != null) names.forEach((k, v) -> NAMES.put(k, String.valueOf(v)));
         }
      } catch (Exception ignored) {
      }
   }

   /** Registers the catalog capes (cape_m_*) and the slot for a designed cape. */
   static void registerAll() {
      int i = 0;
      for (String id : PRESETS.keySet()) {
         Cos.cape(id, NAMES.getOrDefault(id, id), i++ % 3 == 0 ? Cos.Rarity.EPIC : Cos.Rarity.RARE);
      }
      Cos.cape("custom", "Eigenes Cape", Cos.Rarity.COMMON);
   }

   /** Remembers a designed cape from the backend; returns its texture name or null. */
   public static String remember(String id, Object spec) {
      if (id == null || !id.matches("[0-9a-f]{8}") || !(spec instanceof Map)) return null;
      if (CUSTOM.size() > 256) CUSTOM.clear();
      CUSTOM.putIfAbsent(id, spec);
      return "capespec_" + id;
   }

   /** Paints "m_<preset>" or "spec_<id>", optionally "#<frame>" (CTex names cape_m_* / capespec_*). */
   static CTex.T paint(String name) {
      int frame = 0;
      int hash = name.indexOf('#');
      if (hash >= 0) {
         try {
            frame = Integer.parseInt(name.substring(hash + 1));
         } catch (NumberFormatException ignored) {
         }
         name = name.substring(0, hash);
      }
      Object spec = name.startsWith("spec_") ? CUSTOM.get(name.substring(5)) : PRESETS.get(name);
      return sheet(spec, frame);
   }

   static boolean isPreset(String name) {
      return PRESETS.containsKey(name);
   }

   // ------------------------------------------------------------ renderer

   private static int hex(Object v, int d) {
      if (v instanceof String s && s.matches("#[0-9a-fA-F]{6}")) return Integer.parseInt(s.substring(1), 16);
      return d;
   }

   private static double clamp(double v, double lo, double hi) {
      return v < lo ? lo : v > hi ? hi : v;
   }

   private static double[] rgb(int c) {
      return new double[]{c >> 16 & 255, c >> 8 & 255, c & 255};
   }

   private static double[] lerp(double[] a, double[] b, double t) {
      return new double[]{a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t};
   }

   /** Same integer hash as capeHash in cape.ts. */
   static double hash(int x, int y, int seed) {
      int n = x * 374761393 + y * 668265263 + seed * 982451653;
      n = (n ^ (n >>> 13)) * 1274126177;
      n ^= n >>> 16;
      return Integer.toUnsignedLong(n) / 4294967296.0;
   }

   private static boolean inPoly(double[] p, double x, double y) {
      boolean in = false;
      int n = p.length / 2;
      for (int i = 0, j = n - 1; i < n; j = i++) {
         double xi = p[i * 2], yi = p[i * 2 + 1], xj = p[j * 2], yj = p[j * 2 + 1];
         if ((yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) in = !in;
      }
      return in;
   }

   private static boolean inShape(List<Part> parts, double x, double y) {
      boolean on = false;
      for (Part p : parts) if (inPoly(p.poly, x, y)) on = p.add;
      return on;
   }

   private static double[] stops(double[][] cs, double t) {
      t = clamp(t, 0, 1);
      if (cs.length < 3) return lerp(cs[0], cs[1], t);
      return t < 0.5 ? lerp(cs[0], cs[1], t * 2) : lerp(cs[1], cs[2], (t - 0.5) * 2);
   }

   private static double[] background(String type, double[][] cs, double angle, int x, int y, int w, int h, double t) {
      double u = (x + 0.5) / w, v = (y + 0.5) / h;
      double a = angle * Math.PI / 180, ca = Math.cos(a), sa = Math.sin(a);
      double p = Math.PI * 2 * t;
      switch (type) {
         case "gradient": {
            double den = Math.abs(ca) * 0.5 + Math.abs(sa) * 0.5;
            return stops(cs, ((u - 0.5) * ca + (v - 0.5) * sa) / (den == 0 ? 1 : den) * 0.5 + 0.5 + 0.12 * Math.sin(p));
         }
         case "radial":
            return stops(cs, Math.hypot(u - 0.5, (v - 0.38) * ((double)h / w) * 0.62) / 0.62 * (1 + 0.08 * Math.sin(p)));
         case "stripes": {
            int band = (int)Math.floor((x * ca + y * sa) / (w / 5.0) + 1000 + t * 5);
            return cs[band % cs.length];
         }
         case "checker":
            return cs[(x / 10 + y / 10) % 2];
         case "galaxy": {
            double[] c = lerp(cs[0], cs[1], clamp(Math.hypot(u - 0.55, v - 0.45) * 1.6, 0, 1));
            double[] neb = cs.length > 2 ? cs[2] : cs[1];
            double d = Math.hypot((u - 0.32) * 1.2, v - 0.62);
            c = lerp(c, neb, clamp(0.55 - d * 1.4, 0, 1));
            double s = hash(x, y, 7);
            if (s > 0.985) c = lerp(c, new double[]{255, 255, 255}, (0.6 + (s - 0.985) * 26) * (0.55 + 0.45 * Math.sin(p * 2 + s * 400)));
            return c;
         }
         case "waves":
            return stops(cs, 0.5 + 0.5 * Math.sin(v * Math.PI * 2 * 3 + Math.sin(u * Math.PI * 2) * 1.5 + p));
         case "split":
            return u + v * ((double)h / w) * 0.62 < 0.5 + 0.31 * ((double)h / w) ? cs[0] : cs[1];
         default:
            return cs[0];
      }
   }

   /** Frames of an animated cape (CAPE_FRAMES in cape.ts). */
   static final int FRAMES = 16;

   /** Whether a preset ("m_...") or a designed cape ("spec_<id>") is animated. */
   static boolean animated(String name) {
      Object spec = name.startsWith("spec_") ? CUSTOM.get(name.substring(5)) : PRESETS.get(name);
      return spec != null && Json.bool(spec, "anim", false);
   }

   /** Cape front 80x128 as RGB doubles; {@code frame} only matters for animated capes. */
   static double[][] front(Object spec, int frame) {
      int W = FW, H = FH;
      boolean anim = Json.bool(spec, "anim", false);
      double t = anim ? Math.floorMod(frame, FRAMES) / (double)FRAMES : 0;
      double ph = Math.PI * 2 * t;
      Map<String, Object> bg = Json.obj(spec, "bg");
      String type = Json.str(bg, "type", "solid");
      List<Object> colors = bg == null ? null : Json.arr(bg, "colors");
      List<double[]> cl = new ArrayList<>();
      if (colors != null) for (Object c : colors) if (cl.size() < 3) cl.add(rgb(hex(c, 0x1b1b3a)));
      while (cl.size() < 2) cl.add(rgb(cl.isEmpty() ? 0x1b1b3a : 0x6d28d9));
      double[][] cs = cl.toArray(new double[0][]);
      double angle = Json.dbl(bg, "angle", 90);
      double[][] px = new double[W * H][];
      for (int y = 0; y < H; y++) for (int x = 0; x < W; x++) px[y * W + x] = background(type, cs, angle, x, y, W, H, t);

      List<Object> symbols = Json.arr(spec, "symbols");
      if (symbols != null) {
         int count = 0;
         int si = -1;
         for (Object s : symbols) {
            if (count++ >= 6) break;
            List<Part> parts = SYMBOLS.get(Json.str(s, "id", ""));
            if (parts == null) continue;
            si++;
            // Animated: each symbol breathes (size) and floats (height), a little out of step.
            double pulse = anim ? 1 + 0.07 * Math.sin(ph + si * 1.3) : 1;
            double flt = anim ? 0.018 * Math.sin(ph + si * 0.9) : 0;
            double glowK = anim ? 0.4 + 0.25 * (0.5 + 0.5 * Math.sin(ph * 2 + si)) : 0.55;
            double cx = clamp(Json.dbl(s, "x", 0.5), 0, 1) * W, cy = (clamp(Json.dbl(s, "y", 0.5), 0, 1) + flt) * H;
            double half = clamp(Json.dbl(s, "size", 0.5), 0.1, 1) * pulse * W / 2;
            double a = Json.dbl(s, "rot", 0) * Math.PI / 180, ca = Math.cos(a), sa = Math.sin(a);
            float[] cov = new float[W * H];
            int x0 = Math.max(0, (int)Math.floor(cx - half * 1.5)), x1 = Math.min(W - 1, (int)Math.ceil(cx + half * 1.5));
            int y0 = Math.max(0, (int)Math.floor(cy - half * 1.5)), y1 = Math.min(H - 1, (int)Math.ceil(cy + half * 1.5));
            for (int y = y0; y <= y1; y++) for (int x = x0; x <= x1; x++) {
               int hit = 0;
               for (double[] o : SS) {
                  double dx = x + o[0] - cx, dy = y + o[1] - cy;
                  double uu = (dx * ca + dy * sa) / half, vv = (-dx * sa + dy * ca) / half;
                  if (uu >= -1.01 && uu <= 1.01 && vv >= -1.01 && vv <= 1.01 && inShape(parts, uu, vv)) hit++;
               }
               cov[y * W + x] = hit / 4f;
            }
            double[] col = rgb(hex(((Map<?, ?>)s).get("color"), 0xffffff));
            if (Json.bool(s, "glow", false)) {
               int R = 4;
               for (int y = Math.max(0, y0 - R); y <= Math.min(H - 1, y1 + R); y++) for (int x = Math.max(0, x0 - R); x <= Math.min(W - 1, x1 + R); x++) {
                  double g = 0;
                  for (int dy = -R; dy <= R; dy++) for (int dx = -R; dx <= R; dx++) {
                     int xx = x + dx, yy = y + dy;
                     if (xx < 0 || yy < 0 || xx >= W || yy >= H) continue;
                     float c = cov[yy * W + xx];
                     if (c > 0) g = Math.max(g, c * (1 - Math.hypot(dx, dy) / (R + 1)));
                  }
                  if (g > 0) px[y * W + x] = lerp(px[y * W + x], col, g * glowK);
               }
            }
            for (int y = y0; y <= y1; y++) for (int x = x0; x <= x1; x++) {
               float c = cov[y * W + x];
               if (c > 0) px[y * W + x] = lerp(px[y * W + x], col, c);
            }
         }
      }

      Map<String, Object> text = Json.obj(spec, "text");
      String value = text == null ? "" : Json.str(text, "value", "");
      if (!value.isEmpty() && value.length() <= 10) {
         int len = value.length();
         int scale = (int)Math.max(1, Math.min(clamp(Json.num(text, "size", 2), 1, 3) * 2, Math.floor((W - 6) / (double)Math.max(1, len * 6 - 1))));
         int tw = (len * 6 - 1) * scale, th = 7 * scale;
         int ox = (int)Math.round((W - tw) / 2.0), oy = (int)Math.round(clamp(Json.dbl(text, "y", 0.8), 0, 1) * H - th / 2.0);
         double[] col = rgb(hex(text.get("color"), 0xffffff));
         double[][] colors2 = {{0, 0, 0}, col};
         double[] alphas = {0.45, 1};
         int[] offs = {1, 0};
         for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < len; i++) {
               String[] g = FONT.getOrDefault(String.valueOf(value.charAt(i)), FONT.get(" "));
               if (g == null) continue;
               for (int gy = 0; gy < 7; gy++) for (int gx = 0; gx < 5; gx++) {
                  if (g[gy].charAt(gx) != '1') continue;
                  for (int sy = 0; sy < scale; sy++) for (int sx = 0; sx < scale; sx++) {
                     int x = ox + (i * 6 + gx) * scale + sx + offs[pass], y = oy + gy * scale + sy + offs[pass];
                     if (x >= 0 && y >= 0 && x < W && y < H) px[y * W + x] = lerp(px[y * W + x], colors2[pass], alphas[pass]);
                  }
               }
            }
         }
      }

      if (Json.bool(spec, "shine", false)) {
         for (int y = 0; y < H; y++) for (int x = 0; x < W; x++) {
            double d = (x + y * 0.55) / W;
            double at = anim ? -0.3 + 1.9 * t : 0.62;
            double k = Math.max(0, 1 - Math.abs(d - at) / 0.14) * 0.22;
            if (k > 0) px[y * W + x] = lerp(px[y * W + x], new double[]{255, 255, 255}, k);
         }
      }

      Map<String, Object> border = Json.obj(spec, "border");
      int bw = (int)clamp(Json.num(border, "width", 0), 0, 8);
      if (bw > 0) {
         double[] bc = rgb(hex(border.get("color"), 0xffffff));
         for (int y = 0; y < H; y++) for (int x = 0; x < W; x++) if (x < bw || y < bw || x >= W - bw || y >= H - bw) px[y * W + x] = bc;
      }
      return px;
   }

   private static int argb(double[] c) {
      return 0xFF000000 | (int)Math.round(clamp(c[0], 0, 255)) << 16 | (int)Math.round(clamp(c[1], 0, 255)) << 8 | (int)Math.round(clamp(c[2], 0, 255));
   }

   /** Full 512x256 sheet in the vanilla 64x32 cape layout (x8), like renderCapeSheet. */
   static CTex.T sheet(Object spec) {
      return sheet(spec, 0);
   }

   static CTex.T sheet(Object spec, int frame) {
      if (!(spec instanceof Map)) spec = Map.of();
      int W = 512, H = 256;
      double[][] f = front(spec, frame);
      Map<String, Object> bg = Json.obj(spec, "bg");
      List<Object> colors = bg == null ? null : Json.arr(bg, "colors");
      double[] base = rgb(hex(colors != null && !colors.isEmpty() ? colors.get(0) : null, 0x1b1b3a));
      int back = argb(lerp(base, new double[]{0, 0, 0}, 0.35));
      Map<String, Object> border = Json.obj(spec, "border");
      int edge = Json.num(border, "width", 0) > 0 ? argb(rgb(hex(border.get("color"), 0xffffff))) : argb(lerp(base, new double[]{0, 0, 0}, 0.2));
      int[] out = new int[W * H];
      java.util.Arrays.fill(out, back);
      for (int y = 0; y < FH; y++) for (int x = 0; x < FW; x++) out[(8 + y) * W + 8 + x] = argb(f[y * FW + x]);
      fill(out, W, 0, 8, 8, 128, edge);
      fill(out, W, 88, 8, 8, 128, edge);
      fill(out, W, 8, 0, 80, 8, edge);
      fill(out, W, 88, 0, 80, 8, edge);
      return new CTex.T(W, H, out);
   }

   private static void fill(int[] out, int W, int x0, int y0, int w, int h, int c) {
      for (int y = y0; y < y0 + h; y++) for (int x = x0; x < x0 + w; x++) out[y * W + x] = c;
   }
}
