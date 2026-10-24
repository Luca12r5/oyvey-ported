package dev.lego.visual.sky;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Toasts;
import dev.lego.ui.Tx;
import dev.lego.visual.R3;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public final class SkyExtras {
   public static SkyExtras.SkyPerson person;

   private SkyExtras() {
   }

   public static void registerAll() {
      person = Modules.register(new SkyExtras.SkyPerson());
   }

   public static final class SkyPerson extends Module {
      final Setting.Mode background = this.add(new Setting.Mode("Hintergrund", 0, "Automatisch entfernen", "Bild ist schon freigestellt", "Nicht entfernen"));
      final Setting.Num tolerance = this.add(new Setting.Num("Toleranz (Hintergrund)", 10.0, 120.0, 5.0, 48.0, ""));
      final Setting.Mode look = this.add(new Setting.Mode("Look", 1, "Normal", "Leuchtend", "Geist", "Sonnenuntergang"));
      final Setting.Num azimuth = this.add(new Setting.Num("Richtung", 0.0, 355.0, 5.0, 180.0, "°"));
      final Setting.Num elevation = this.add(new Setting.Num("Höhe", 0.0, 70.0, 1.0, 22.0, "°"));
      final Setting.Num size = this.add(new Setting.Num("Größe", 20.0, 140.0, 5.0, 75.0, "°"));
      final Setting.Num opacity = this.add(new Setting.Num("Deckkraft", 10.0, 100.0, 5.0, 85.0, "%"));
      final Setting.Mode when = this.add(new Setting.Mode("Zeigen", 0, "Immer", "Nur am Tag", "Nur am Abend", "Nur nachts"));
      final Setting.Bool fadeBottom = this.add(new Setting.Bool("Unten in die Wolken ausblenden", true));
      final Setting.Bool bob = this.add(new Setting.Bool("Sanft schweben", true));
      private final AtomicReference<int[][]> pending = new AtomicReference<>();
      private volatile boolean working;
      private String wantKey;
      private String loadedKey;
      private Identifier tex;
      private Identifier glowTex;
      private int tw = 1;
      private int th = 1;
      private int index = 0;
      private long settingsHash;

      SkyPerson() {
         super("skyperson", "Sky-Figur", "Ein PNG von einem Menschen (oder was du willst) riesig am Himmel - Hintergrund wird entfernt", Category.VISUALS);
         this.icon("image");
         this.fresh();
         this.add(new Setting.Action("Bild", "Nächstes Bild", () -> {
            this.index++;
            this.loadedKey = null;
         }));
         this.add(new Setting.Action("Bilder-Ordner", "Pfad kopieren", () -> {
            Path p = folder();
            Mc.clipboard(p.toString());
            Toasts.show("copy", "Ordner kopiert", "PNG/JPG hier ablegen: " + p);
         }));
         this.add(new Setting.Action("Neu freistellen", "Neu berechnen", () -> this.loadedKey = null));
      }

      static Path folder() {
         Path p = FabricLoader.getInstance().getConfigDir().resolve("legoclient").resolve("sky_people");

         try {
            Files.createDirectories(p);
         } catch (Throwable var2) {
         }

         return p;
      }

      @Override
      public void onDisable() {
         if (this.tex != null) {
            Tx.destroy(this.tex);
         }

         if (this.glowTex != null) {
            Tx.destroy(this.glowTex);
         }

         this.tex = this.glowTex = null;
         this.loadedKey = null;
      }

      private void ensure() {
         long h = this.background.index * 1000L + this.tolerance.getI() + (this.fadeBottom.get() ? 100000 : 0);
         String key = this.index + "|" + h;
         int[][] done = this.pending.getAndSet(null);
         if (done != null) {
            if (this.tex != null) {
               Tx.destroy(this.tex);
            }

            if (this.glowTex != null) {
               Tx.destroy(this.glowTex);
            }

            this.tex = this.glowTex = null;
            if (done[0].length > 1) {
               this.tw = done[2][0];
               this.th = done[2][1];
               this.tex = Identifier.fromNamespaceAndPath("legoclient", "sky/person_" + (System.nanoTime() & 16777215L));
               this.glowTex = Identifier.fromNamespaceAndPath("legoclient", "sky/person_glow_" + (System.nanoTime() & 16777215L));
               Tx.register(this.tex, done[0], this.tw, this.th, true);
               Tx.register(this.glowTex, done[1], this.tw, this.th, true);
            }

            this.loadedKey = this.wantKey;
         }

         if (!key.equals(this.loadedKey) && !this.working) {
            this.wantKey = key;
            this.working = true;
            int bg = this.background.index;
            int tol = this.tolerance.getI();
            int idx = this.index;
            boolean fade = this.fadeBottom.get();
            Thread t = new Thread(() -> {
               try {
                  this.pending.set(process(idx, bg, tol, fade));
               } catch (Throwable var9x) {
                  LegoClient.LOG("Sky-Figur: " + var9x);
                  this.pending.set(new int[][]{{0}});
               } finally {
                  this.working = false;
               }
            }, "LegoClient-SkyPerson");
            t.setDaemon(true);
            t.start();
         }
      }

      private static int[][] process(int idx, int bgMode, int tol, boolean fade) throws Exception {
         List<File> files = new ArrayList<>();
         File[] list = folder().toFile().listFiles();
         if (list != null) {
            for (File f : list) {
               String n = f.getName().toLowerCase(Locale.ROOT);
               if (n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg")) {
                  files.add(f);
               }
            }
         }

         files.sort(Comparator.comparing(File::getName));
         if (files.isEmpty()) {
            Toasts.show("image", "Sky-Figur: kein Bild", "Leg ein PNG in " + folder(), 0, 6000L);
            return new int[][]{{0}};
         } else {
            File fx = files.get(Math.floorMod(idx, files.size()));
            BufferedImage src = ImageIO.read(fx);
            if (src == null) {
               throw new IllegalStateException("Bild nicht lesbar: " + fx.getName());
            } else {
               int w = src.getWidth();
               int h = src.getHeight();
               double k = Math.min(1.0, 1024.0 / Math.max(w, h));
               int W = Math.max(8, (int)Math.round(w * k));
               int H = Math.max(8, (int)Math.round(h * k));
               BufferedImage img = new BufferedImage(W, H, 2);
               Graphics2D g = img.createGraphics();
               g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
               g.drawImage(src, 0, 0, W, H, null);
               g.dispose();
               int[] px = img.getRGB(0, 0, W, H, null, 0, W);
               int transparent = 0;

               for (int c : px) {
                  if (c >>> 24 < 250) {
                     transparent++;
                  }
               }

               boolean hasAlpha = transparent > px.length * 0.02;
               if (bgMode == 0 && !hasAlpha) {
                  removeBackground(px, W, H, tol);
               } else if (bgMode == 2) {
                  for (int i = 0; i < px.length; i++) {
                     px[i] |= -16777216;
                  }
               }

               if (fade) {
                  int from = (int)(H * 0.78);

                  for (int y = from; y < H; y++) {
                     double a = 1.0 - (double)(y - from) / (H - from);
                     a = a * a * (3.0 - 2.0 * a);

                     for (int x = 0; x < W; x++) {
                        int cx = px[y * W + x];
                        px[y * W + x] = (int)((cx >>> 24) * a) << 24 | cx & 16777215;
                     }
                  }
               }

               float[] al = new float[W * H];

               for (int i = 0; i < px.length; i++) {
                  al[i] = (px[i] >>> 24) / 255.0F;
               }

               int r = Math.max(4, W / 40);

               for (int pass = 0; pass < 3; pass++) {
                  al = blur(al, W, H, r);
               }

               int[] glow = new int[W * H];

               for (int i = 0; i < glow.length; i++) {
                  glow[i] = (int)Math.min(255.0, al[i] * 255.0F * 1.4) << 24 | 16777215;
               }

               Toasts.show("image", "Sky-Figur bereit", fx.getName() + (bgMode == 0 && !hasAlpha ? " · Hintergrund entfernt" : ""), 0, 3000L);
               return new int[][]{px, glow, {W, H}};
            }
         }
      }

      static void removeBackground(int[] px, int W, int H, int tol) {
         List<Integer> border = new ArrayList<>();

         for (int x = 0; x < W; x++) {
            border.add(px[x]);
            border.add(px[(H - 1) * W + x]);
         }

         for (int y = 0; y < H; y++) {
            border.add(px[y * W]);
            border.add(px[y * W + W - 1]);
         }

         int[] rs = new int[border.size()];
         int[] gs = new int[border.size()];
         int[] bs = new int[border.size()];

         for (int i = 0; i < border.size(); i++) {
            int c = border.get(i);
            rs[i] = c >> 16 & 0xFF;
            gs[i] = c >> 8 & 0xFF;
            bs[i] = c & 0xFF;
         }

         Arrays.sort(rs);
         Arrays.sort(gs);
         Arrays.sort(bs);
         int br = rs[rs.length / 2];
         int bgc = gs[gs.length / 2];
         int bb = bs[bs.length / 2];
         boolean[] bg = new boolean[W * H];
         ArrayDeque<Integer> q = new ArrayDeque<>();
         double t1 = tol;
         double t2 = tol * 1.4;

         for (int x = 0; x < W; x++) {
            seed(px, bg, q, x, 0, W, br, bgc, bb, t2);
            seed(px, bg, q, x, H - 1, W, br, bgc, bb, t2);
         }

         for (int y = 0; y < H; y++) {
            seed(px, bg, q, 0, y, W, br, bgc, bb, t2);
            seed(px, bg, q, W - 1, y, W, br, bgc, bb, t2);
         }

         int[] dx = new int[]{1, -1, 0, 0};
         int[] dy = new int[]{0, 0, 1, -1};

         while (!q.isEmpty()) {
            int i = q.poll();
            int x = i % W;
            int y = i / W;

            for (int d = 0; d < 4; d++) {
               int nx = x + dx[d];
               int ny = y + dy[d];
               if (nx >= 0 && ny >= 0 && nx < W && ny < H) {
                  int j = ny * W + nx;
                  if (!bg[j]) {
                     double dj = dist(px[j], br, bgc, bb);
                     if (dj < t1 || dist(px[j], px[i]) < t1 * 0.3 && dj < t2 && dist(px[i], br, bgc, bb) < t1 * 0.6) {
                        bg[j] = true;
                        q.add(j);
                     }
                  }
               }
            }
         }

         int[] comp = new int[W * H];
         Arrays.fill(comp, -1);
         List<Integer> sizes = new ArrayList<>();

         for (int i = 0; i < W * H; i++) {
            if (!bg[i] && comp[i] < 0) {
               int id = sizes.size();
               int n = 0;
               comp[i] = id;
               q.add(i);

               while (!q.isEmpty()) {
                  int c = q.poll();
                  n++;
                  int x = c % W;
                  int y = c / W;

                  for (int dxx = 0; dxx < 4; dxx++) {
                     int nx = x + dx[dxx];
                     int ny = y + dy[dxx];
                     if (nx >= 0 && ny >= 0 && nx < W && ny < H) {
                        int j = ny * W + nx;
                        if (!bg[j] && comp[j] < 0) {
                           comp[j] = id;
                           q.add(j);
                        }
                     }
                  }
               }

               sizes.add(n);
            }
         }

         int biggest = 0;

         for (int s : sizes) {
            biggest = Math.max(biggest, s);
         }

         float[] a = new float[W * H];

         for (int ix = 0; ix < W * H; ix++) {
            a[ix] = !bg[ix] && sizes.get(comp[ix]).intValue() >= Math.max(40.0, biggest * 0.04) ? 1.0F : 0.0F;
         }

         a = blur(a, W, H, 1);

         for (int ix = 0; ix < W * H; ix++) {
            float al = a[ix];
            al = Math.max(0.0F, Math.min(1.0F, (al - 0.2F) / 0.75F));
            int c = px[ix];
            int r = c >> 16 & 0xFF;
            int gg = c >> 8 & 0xFF;
            int b = c & 0xFF;
            if (al > 0.02F && al < 0.98F) {
               r = clamp((r - br * (1.0F - al)) / al);
               gg = clamp((gg - bgc * (1.0F - al)) / al);
               b = clamp((b - bb * (1.0F - al)) / al);
            }

            px[ix] = (int)(al * 255.0F) << 24 | r << 16 | gg << 8 | b;
         }
      }

      private static void seed(int[] px, boolean[] bg, ArrayDeque<Integer> q, int x, int y, int W, int br, int bgc, int bb, double t) {
         int i = y * W + x;
         if (!bg[i] && dist(px[i], br, bgc, bb) < t) {
            bg[i] = true;
            q.add(i);
         }
      }

      private static double dist(int c, int r, int g, int b) {
         int dr = (c >> 16 & 0xFF) - r;
         int dg = (c >> 8 & 0xFF) - g;
         int db = (c & 0xFF) - b;
         return Math.sqrt(dr * dr + dg * dg + db * db);
      }

      private static double dist(int c1, int c2) {
         return dist(c1, c2 >> 16 & 0xFF, c2 >> 8 & 0xFF, c2 & 0xFF);
      }

      private static int clamp(float v) {
         return Math.max(0, Math.min(255, Math.round(v)));
      }

      private static float[] blur(float[] src, int W, int H, int r) {
         float[] tmp = new float[W * H];
         float[] out = new float[W * H];

         for (int y = 0; y < H; y++) {
            float s = 0.0F;

            for (int x = -r; x <= r; x++) {
               s += src[y * W + Math.max(0, Math.min(W - 1, x))];
            }

            for (int x = 0; x < W; x++) {
               tmp[y * W + x] = s / (2 * r + 1);
               s += src[y * W + Math.min(W - 1, x + r + 1)] - src[y * W + Math.max(0, x - r)];
            }
         }

         for (int x = 0; x < W; x++) {
            float s = 0.0F;

            for (int y = -r; y <= r; y++) {
               s += tmp[Math.max(0, Math.min(H - 1, y)) * W + x];
            }

            for (int y = 0; y < H; y++) {
               out[y * W + x] = s / (2 * r + 1);
               s += tmp[Math.min(H - 1, y + r + 1) * W + x] - tmp[Math.max(0, y - r) * W + x];
            }
         }

         return out;
      }

      public void render() {
         this.ensure();
         if (this.tex != null) {
            double[] w = CustomSky.dayWeights(Mc.timeOfDay());

            double vis = switch (this.when.index) {
               case 1 -> w[0];
               case 2 -> w[1];
               case 3 -> w[2];
               default -> 1.0;
            };
            if (!(vis < 0.01)) {
               double far = 256.0;

               try {
                  far = dev.lego.compat.McCompat.depthFar(Mc.mc().gameRenderer);
               } catch (Throwable var31) {
               }

               double D = R3.skyRadius > 0.0 ? R3.skyRadius * 0.85 : Math.max(60.0, far * 0.42);
               double t = System.currentTimeMillis() / 1000.0;
               double el = Math.toRadians(this.elevation.get() + (this.bob.get() ? Math.sin(t * 0.35) * 1.2 : 0.0));
               double az = Math.toRadians(this.azimuth.get());
               double[] dir = new double[]{Math.sin(az) * Math.cos(el), Math.sin(el), -Math.cos(az) * Math.cos(el)};
               double[] right = new double[]{Math.cos(az), 0.0, Math.sin(az)};
               double[] up = new double[]{dir[1] * right[2] - dir[2] * right[1], dir[2] * right[0] - dir[0] * right[2], dir[0] * right[1] - dir[1] * right[0]};
               if (up[1] < 0.0) {
                  up[0] = -up[0];
                  up[1] = -up[1];
                  up[2] = -up[2];
               }

               double hh = D * Math.tan(Math.toRadians(this.size.get()) / 2.0);
               double hw = hh * this.tw / this.th;
               double cx = R3.camX() + dir[0] * D;
               double cy = R3.camY() + dir[1] * D;
               double cz = R3.camZ() + dir[2] * D;
               double alpha = this.opacity.get() / 100.0 * vis;
               int tint;
               int glowTint;
               switch (this.look.index) {
                  case 1:
                     tint = R3.argb(16777215, alpha);
                     glowTint = R3.argb(16773590, alpha * 0.45);
                     break;
                  case 2:
                     tint = R3.argb(12572927, alpha * 0.55);
                     glowTint = R3.argb(10274559, alpha * 0.55);
                     break;
                  case 3:
                     tint = R3.argb(16762784, alpha * 0.9);
                     glowTint = R3.argb(16747082, alpha * 0.5);
                     break;
                  default:
                     tint = R3.argb(16777215, alpha);
                     glowTint = 0;
               }

               if (glowTint != 0) {
                  quad(R3.skyLayer(this.glowTex), cx, cy, cz, right, up, hw * 1.12, hh * 1.12, glowTint);
               }

               quad(R3.skyLayer(this.tex), cx, cy, cz, right, up, hw, hh, tint);
            }
         }
      }

      private static void quad(RenderType layer, double cx, double cy, double cz, double[] r, double[] u, double hw, double hh, int c) {
         double[] p0 = new double[]{cx - r[0] * hw + u[0] * hh, cy - r[1] * hw + u[1] * hh, cz - r[2] * hw + u[2] * hh};
         double[] p1 = new double[]{cx + r[0] * hw + u[0] * hh, cy + r[1] * hw + u[1] * hh, cz + r[2] * hw + u[2] * hh};
         double[] p2 = new double[]{cx + r[0] * hw - u[0] * hh, cy + r[1] * hw - u[1] * hh, cz + r[2] * hw - u[2] * hh};
         double[] p3 = new double[]{cx - r[0] * hw - u[0] * hh, cy - r[1] * hw - u[1] * hh, cz - r[2] * hw - u[2] * hh};
         R3.skyQuad(layer, p0, p1, p2, p3, new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}, c);
      }
   }
}
