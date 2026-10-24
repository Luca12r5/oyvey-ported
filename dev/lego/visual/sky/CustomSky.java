package dev.lego.visual.sky;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Setting;
import dev.lego.ui.Toasts;
import dev.lego.ui.Tx;
import dev.lego.visual.R3;
import dev.spotifyhud.Anim;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public final class CustomSky extends Module {
   private static final String[] MODES = new String[SkyGen.NAMES.length + 3];
   private static final int[] RES = new int[]{768, 1024, 1536};
   public final Setting.Mode mode = this.add(new Setting.Mode("Himmel", 0, MODES));
   public final Setting.Mode quality = this.add(new Setting.Mode("Qualität", 1, "Normal", "HD", "Ultra 4K"));
   public final Setting.Mode daySky = this.add(new Setting.Mode("Auto: Tag-Himmel", idx("anime"), SkyGen.NAMES));
   public final Setting.Mode duskSky = this.add(new Setting.Mode("Auto: Abend-Himmel", idx("goldenhour"), SkyGen.NAMES));
   public final Setting.Mode nightSky = this.add(new Setting.Mode("Auto: Nacht-Himmel", idx("milkyway"), SkyGen.NAMES));
   public final Setting.Bool fog = this.add(new Setting.Bool("Nebel an Himmel anpassen", true));
   public final Setting.Num fogDensity = this.add(new Setting.Num("Nebel-Dichte", 0.0, 100.0, 5.0, 0.0, "%"));
   public final Setting.Color fogColor = this.add(new Setting.Color("Nebel-Farbe", 8));
   public final Setting.Num fogStart = this.add(new Setting.Num("Nebel-Start", 0.0, 100.0, 5.0, 20.0, "%"));
   public final Setting.Num speed = this.add(new Setting.Num("Bewegung", 0.0, 4.0, 0.1, 1.0, "x"));
   public final Setting.Num brightness = this.add(new Setting.Num("Helligkeit", 0.2, 1.0, 0.05, 0.9, ""));
   public final Setting.Bool shooting = this.add(new Setting.Bool("Sternschnuppen", true));
   public final Setting.Bool nightOnly = this.add(new Setting.Bool("Nur nachts einblenden", false));
   public final Setting.Bool overworldOnly = this.add(new Setting.Bool("Nur Oberwelt", true));
   private final AtomicReference<int[]> pending = new AtomicReference<>();
   private volatile String generating = null;
   private String loadedKey = null;
   private String pendingKey = null;
   private int pendingW;
   private int pendingH;
   private Identifier tex = null;
   private boolean equirect = false;
   private int imageIndex = 0;
   private final LinkedHashMap<String, Identifier> loaded = new LinkedHashMap<>();
   private final Map<String, Boolean> equi = new HashMap<>();
   private static final Set<Identifier> LINEAR = new HashSet<>();

   private static int idx(String id) {
      for (int i = 0; i < SkyGen.IDS.length; i++) {
         if (SkyGen.IDS[i].equals(id)) {
            return i;
         }
      }

      return 0;
   }

   public CustomSky() {
      super("sky", "Custom Sky", "Realistische HD-Himmel, Sky-Resourcepacks und eigene Bilder", Category.VISUALS);
      this.icon("cloud");
      this.fresh();
      this.add(new Setting.Action("Sky-Packs", "Neu laden", () -> {
         SkyPacks.invalidate();
         SkyPacks.layers();
         Toasts.show("cloud", "Custom Sky", SkyPacks.info());
      }));
      this.add(new Setting.Action("Eigene Bilder", "Nächstes Bild", () -> {
         this.imageIndex++;
         this.loadedKey = null;
      }));
      this.add(new Setting.Action("Bilder-Ordner", "Pfad kopieren", () -> {
         Path p = folder();
         Mc.clipboard(p.toString());
         Toasts.show("copy", "Ordner kopiert", p.toString());
      }));
   }

   public static Path folder() {
      Path p = FabricLoader.getInstance().getConfigDir().resolve("legoclient").resolve("skies");

      try {
         Files.createDirectories(p);
      } catch (Throwable var2) {
      }

      return p;
   }

   private boolean builtIn() {
      return this.mode.index < SkyGen.IDS.length;
   }

   private boolean packs() {
      return this.mode.index == SkyGen.IDS.length;
   }

   private boolean auto() {
      return this.mode.index == SkyGen.IDS.length + 2;
   }

   @Override
   public void onDisable() {
      this.free();
   }

   private void free() {
      for (Identifier t : this.loaded.values()) {
         Tx.destroy(t);
      }

      this.loaded.clear();
      this.tex = null;
      this.loadedKey = null;
   }

   private static Path cacheDir() {
      Path p = FabricLoader.getInstance().getConfigDir().resolve("legoclient").resolve("skycache");

      try {
         Files.createDirectories(p);
      } catch (Throwable var2) {
      }

      return p;
   }

   private Identifier texture(String key) {
      Identifier t = this.loaded.get(key);
      if (t != null) {
         return t;
      } else {
         int[] px = this.pending.get();
         if (px != null && key.equals(this.pendingKey)) {
            this.pending.set(null);
            t = Identifier.fromNamespaceAndPath("legoclient", "sky/" + key.replace('@', '_').replace('.', '_').toLowerCase(Locale.ROOT));
            Tx.register(t, px, this.pendingW, this.pendingH, true);
            this.loaded.put(key, t);
            this.equi.put(key, this.equirect);

            while (this.loaded.size() > 3) {
               String first = this.loaded.keySet().iterator().next();
               Tx.destroy(this.loaded.remove(first));
            }

            return t;
         } else if (this.generating != null) {
            return null;
         } else {
            this.generating = key;
            Thread th = new Thread(() -> {
               try {
                  if (key.startsWith("img@")) {
                     this.loadImage(key);
                  } else {
                     String id = key.substring(0, key.indexOf(64));
                     int n = Integer.parseInt(key.substring(key.indexOf(64) + 1));
                     File f = cacheDir().resolve(id + "_" + n + "_v3.png").toFile();
                     int[] img = null;
                     if (f.isFile()) {
                        try {
                           BufferedImage bi = ImageIO.read(f);
                           if (bi != null && bi.getWidth() == n * 3 && bi.getHeight() == n * 2) {
                              img = bi.getRGB(0, 0, n * 3, n * 2, null, 0, n * 3);
                           }
                        } catch (Throwable var13) {
                        }
                     }

                     if (img == null) {
                        img = SkyGen.generate(id, n);

                        try {
                           BufferedImage bi = new BufferedImage(n * 3, n * 2, 1);
                           bi.setRGB(0, 0, n * 3, n * 2, img, 0, n * 3);
                           ImageIO.write(bi, "png", f);
                        } catch (Throwable var12) {
                        }
                     }

                     for (int i = 0; i < img.length; i++) {
                        img[i] |= -16777216;
                     }

                     this.pendingW = n * 3;
                     this.pendingH = n * 2;
                     this.equirect = false;
                     this.pendingKey = key;
                     this.pending.set(img);
                  }
               } catch (Throwable var14) {
                  LegoClient.LOG("Sky-Generierung: " + var14);
               } finally {
                  this.generating = null;
               }
            }, "LegoClient-Sky");
            th.setDaemon(true);
            th.setPriority(1);
            th.start();
            return null;
         }
      }
   }

   private String presetKey(int i) {
      return SkyGen.IDS[Math.max(0, Math.min(SkyGen.IDS.length - 1, i))] + "@" + RES[this.quality.index];
   }

   private List<Object[]> plan(long tod) {
      List<Object[]> out = new ArrayList<>();
      if (this.auto()) {
         double[] w = dayWeights(tod);
         int[] ids = new int[]{this.daySky.index, this.duskSky.index, this.nightSky.index};

         for (int i = 0; i < 3; i++) {
            if (w[i] > 0.001) {
               out.add(new Object[]{this.presetKey(ids[i]), w[i], SkyGen.IDS[ids[i]]});
            }
         }

         out.sort((x, y) -> Double.compare((Double)y[1], (Double)x[1]));
      } else if (this.builtIn()) {
         out.add(new Object[]{this.presetKey(this.mode.index), 1.0, SkyGen.IDS[this.mode.index]});
      } else {
         out.add(new Object[]{"img@" + this.imageIndex, 1.0, null});
      }

      return out;
   }

   static double[] dayWeights(long t) {
      t = Math.floorMod(t, 24000L);
      double dusk = Math.max(bump(t, 12600.0, 1400.0), bump(t, 23300.0, 1200.0) + bump(t - 24000L, -700.0, 1200.0));
      double night = t >= 13600L && t <= 22400L
         ? 1.0
         : (t > 12600L && t < 13600L ? (t - 12600L) / 1000.0 : (t > 22400L && t < 23400L ? 1.0 - (t - 22400L) / 1000.0 : 0.0));
      double day = Math.max(0.0, 1.0 - night - dusk);
      night = Math.max(0.0, night - dusk * 0.5);
      double s = day + dusk + night;
      return new double[]{day / s, dusk / s, night / s};
   }

   private static double bump(double t, double c, double halfWidth) {
      double d = Math.abs(t - c) / halfWidth;
      return d >= 1.0 ? 0.0 : 0.5 + 0.5 * Math.cos(d * Math.PI);
   }

   public int fogTint() {
      if (this.enabled && this.fog.get() && !this.packs() && this.inOverworld()) {
         List<Object[]> p = this.plan(Mc.timeOfDay());
         double r = 0.0;
         double g = 0.0;
         double b = 0.0;
         double ws = 0.0;

         for (Object[] e : p) {
            if (e[2] != null && this.loaded.containsKey((String)e[0])) {
               int c = SkyGen.fogColor((String)e[2]);
               double w = (Double)e[1];
               r += (c >> 16 & 0xFF) * w;
               g += (c >> 8 & 0xFF) * w;
               b += (c & 0xFF) * w;
               ws += w;
            }
         }

         return ws <= 0.0 ? 0 : 0xFF000000 | (int)(r / ws) << 16 | (int)(g / ws) << 8 | (int)(b / ws);
      } else {
         return 0;
      }
   }

   public boolean replacesVanilla() {
      if (this.enabled && this.inOverworld()) {
         if (this.packs()) {
            return !SkyPacks.layers().isEmpty();
         } else {
            for (Object[] e : this.plan(Mc.timeOfDay())) {
               if (this.loaded.containsKey((String)e[0])) {
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private boolean inOverworld() {
      if (!this.overworldOnly.get()) {
         return true;
      } else {
         try {
            return Mc.world().dimension().identifier().getPath().equals("overworld");
         } catch (Throwable var2) {
            return true;
         }
      }
   }

   private void loadImage(String key) throws Exception {
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
         Toasts.show("image", "Keine Bilder", "Leg Himmel-Bilder in " + folder());
         this.pendingW = 1;
         this.pendingH = 1;
         this.equirect = false;
         this.pendingKey = key;
         this.pending.set(new int[]{0});
      } else {
         File fx = files.get(Math.floorMod(this.imageIndex, files.size()));
         BufferedImage img = ImageIO.read(fx);
         int w = img.getWidth();
         int h = img.getHeight();
         if (w > 8192) {
            int nh = h * 8192 / w;
            BufferedImage s = new BufferedImage(8192, nh, 2);
            Graphics2D g = s.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(img, 0, 0, 8192, nh, null);
            g.dispose();
            img = s;
            w = 8192;
            h = nh;
         }

         this.equirect = Math.abs((double)w / h - 2.0) < 0.2;
         this.pendingW = w;
         this.pendingH = h;
         this.pendingKey = key;
         this.pending.set(img.getRGB(0, 0, w, h, null, 0, w));
         Toasts.show("image", "Himmel geladen", fx.getName());
      }
   }

   public void render() {
      if (this.inOverworld()) {
         double far = 256.0;

         try {
            far = Mc.mc().gameRenderer.getDepthFar();
         } catch (Throwable var35) {
         }

         double view = Mc.mc().options.getEffectiveRenderDistance() * 16.0;
         double R = R3.skyRadius > 0.0 ? R3.skyRadius : Math.max(96.0, Math.min(far * 0.8, Math.max(view * 1.8, 140.0)));
         long tod = Mc.timeOfDay() % 24000L;
         double vis = 1.0;
         if (this.nightOnly.get()) {
            vis = nightFactor(tod);
         }

         if (!(vis <= 0.001)) {
            double bright = this.brightness.get();
            if (this.packs()) {
               for (SkyPacks.Layer l : SkyPacks.layers()) {
                  double a = l.alpha(tod) * vis;
                  if (!(a <= 0.003)) {
                     double ang = l.rotate ? celestial(tod) * 360.0 * l.speed : 0.0;
                     int c = argb(bright * (l.add ? 0.9 : 1.0), a * (l.add ? 0.8 : 1.0));
                     if (l.strip != null) {
                        linearOnce(l.strip);
                        drawCube(R3.skyLayer(l.strip), null, R, l.axis, ang, c);
                     } else if (l.faces != null) {
                        RenderType[] fl = new RenderType[6];

                        for (int i = 0; i < 6; i++) {
                           if (l.faces[i] != null) {
                              linearOnce(l.faces[i]);
                              fl[i] = R3.skyLayer(l.faces[i]);
                           }
                        }

                        drawCube(null, fl, R, l.axis, ang, c);
                     }
                  }
               }
            } else {
               double t = System.currentTimeMillis() / 1000.0;
               double ang = t * 0.6 * this.speed.get();
               boolean anyStorm = false;
               boolean stars = false;

               for (Object[] e : this.plan(tod)) {
                  String key = (String)e[0];
                  double w = (Double)e[1];
                  String id = (String)e[2];
                  Identifier tx = this.texture(key);
                  if (tx != null) {
                     boolean storm = id != null && (id.equals("storm") || id.equals("thunder"));
                     double flash = storm ? lightning(t) : 0.0;
                     anyStorm |= flash > 0.01;
                     String kind = id == null ? "" : SkyGen.kind(id);
                     stars |= w > 0.4 && (kind.equals("night") || kind.equals("space"));
                     int c = argb(Math.min(1.0, bright + flash * 0.4), vis * w);
                     RenderType layer = R3.skyLayer(tx);
                     if (Boolean.TRUE.equals(this.equi.get(key))) {
                        drawSphere(layer, R, ang, c);
                     } else {
                        drawCube(layer, null, R, new double[]{0.0, 1.0, 0.0}, ang, c);
                     }

                     if (flash > 0.01) {
                        int fc = R3.argb(14542591, flash * 0.35 * vis * w);
                        double r = R * 0.5;
                        R3.box(R3.camX() - r, R3.camY() - r * 0.2, R3.camZ() - r, R3.camX() + r, R3.camY() + r, R3.camZ() + r, fc);
                     }
                  }
               }

               if (this.shooting.get() && stars) {
                  shootingStars(R * 0.97, vis);
               }
            }
         }
      }
   }

   private static void linearOnce(Identifier id) {
      if (LINEAR.add(id)) {
         try {
            Tx.linear(Tx.tm().getTexture(id));
         } catch (Throwable var2) {
         }
      }
   }

   private static double celestial(long tod) {
      double d = tod / 24000.0 - 0.25;
      d -= Math.floor(d);
      double e = 0.5 - Math.cos(d * Math.PI) / 2.0;
      return (d * 2.0 + e) / 3.0;
   }

   private static double nightFactor(long t) {
      if (t >= 13500L && t <= 22500L) {
         return 1.0;
      } else if (t > 12000L && t < 13500L) {
         return (t - 12000L) / 1500.0;
      } else {
         return t > 22500L && t < 24000L ? 1.0 - (t - 22500L) / 1500.0 : 0.0;
      }
   }

   private static double lightning(double t) {
      int slot = (int)Math.floor(t / 7.0);
      double local = t - slot * 7.0;
      if (Anim.rnd(slot, 77) > 0.55) {
         return 0.0;
      } else {
         double start = Anim.rnd(slot, 78) * 5.0;
         double x = local - start;
         return !(x < 0.0) && !(x > 0.5) ? Math.max(0.0, Math.sin(x / 0.5 * Math.PI)) * (0.6 + 0.4 * Math.sin(x * 60.0)) : 0.0;
      }
   }

   private static int argb(double bright, double alpha) {
      int v = (int)Math.max(0L, Math.min(255L, Math.round(bright * 255.0)));
      int a = (int)Math.max(0L, Math.min(255L, Math.round(alpha * 255.0)));
      return a << 24 | v << 16 | v << 8 | v;
   }

   private static double[] rot(double[] v, double[] axis, double deg) {
      if (deg == 0.0) {
         return v;
      } else {
         double l = Math.sqrt(axis[0] * axis[0] + axis[1] * axis[1] + axis[2] * axis[2]);
         if (l < 1.0E-9) {
            return v;
         } else {
            double kx = axis[0] / l;
            double ky = axis[1] / l;
            double kz = axis[2] / l;
            double a = Math.toRadians(deg);
            double c = Math.cos(a);
            double s = Math.sin(a);
            double dot = kx * v[0] + ky * v[1] + kz * v[2];
            double cx = ky * v[2] - kz * v[1];
            double cy = kz * v[0] - kx * v[2];
            double cz = kx * v[1] - ky * v[0];
            return new double[]{v[0] * c + cx * s + kx * dot * (1.0 - c), v[1] * c + cy * s + ky * dot * (1.0 - c), v[2] * c + cz * s + kz * dot * (1.0 - c)};
         }
      }
   }

   private static void drawCube(RenderType strip, RenderType[] faces, double R, double[] axis, double ang, int color) {
      double[] d = new double[3];
      double cx = R3.camX();
      double cy = R3.camY();
      double cz = R3.camZ();

      for (int f = 0; f < 6; f++) {
         RenderType layer = strip != null ? strip : (faces == null ? null : faces[f]);
         if (layer != null) {
            int col = f % 3;
            int row = f / 3;
            int segs = 4;

            for (int i = 0; i < segs; i++) {
               for (int j = 0; j < segs; j++) {
                  double u0 = (double)i / segs;
                  double u1 = (double)(i + 1) / segs;
                  double v0 = (double)j / segs;
                  double v1 = (double)(j + 1) / segs;
                  double[][] p = new double[4][];
                  double[][] uvs = new double[][]{{u0, v0}, {u1, v0}, {u1, v1}, {u0, v1}};
                  float[] uv = new float[8];

                  for (int k = 0; k < 4; k++) {
                     SkyGen.dir(f, uvs[k][0], uvs[k][1], d);
                     double m = Math.max(Math.abs(d[0]), Math.max(Math.abs(d[1]), Math.abs(d[2])));
                     double[] q = rot(new double[]{d[0] / m, d[1] / m, d[2] / m}, axis, ang);
                     p[k] = new double[]{cx + q[0] * R * 0.577, cy + q[1] * R * 0.577, cz + q[2] * R * 0.577};
                     if (strip != null) {
                        double inset = 2.4414062E-4F;
                        double uu = Math.max(inset, Math.min(1.0 - inset, uvs[k][0]));
                        double vv = Math.max(inset, Math.min(1.0 - inset, uvs[k][1]));
                        uv[k * 2] = (float)((col + uu) / 3.0);
                        uv[k * 2 + 1] = (float)((row + vv) / 2.0);
                     } else {
                        uv[k * 2] = (float)uvs[k][0];
                        uv[k * 2 + 1] = (float)uvs[k][1];
                     }
                  }

                  R3.skyQuad(layer, p[0], p[1], p[2], p[3], uv, color);
               }
            }
         }
      }
   }

   private static void drawSphere(RenderType layer, double R, double ang, int color) {
      int lon = 48;
      int lat = 24;
      double cx = R3.camX();
      double cy = R3.camY();
      double cz = R3.camZ();
      double r = R * 0.9;

      for (int i = 0; i < lon; i++) {
         for (int j = 0; j < lat; j++) {
            double u0 = (double)i / lon;
            double u1 = (double)(i + 1) / lon;
            double v0 = (double)j / lat;
            double v1 = (double)(j + 1) / lat;
            double[][] p = new double[][]{sph(u0, v0, ang), sph(u1, v0, ang), sph(u1, v1, ang), sph(u0, v1, ang)};

            for (double[] q : p) {
               q[0] = cx + q[0] * r;
               q[1] = cy + q[1] * r;
               q[2] = cz + q[2] * r;
            }

            R3.skyQuad(
               layer, p[0], p[1], p[2], p[3], new float[]{(float)u0, (float)v0, (float)u1, (float)v0, (float)u1, (float)v1, (float)u0, (float)v1}, color
            );
         }
      }
   }

   private static double[] sph(double u, double v, double ang) {
      double lonA = u * Math.PI * 2.0 + Math.toRadians(ang);
      double latA = (Math.PI / 2) - v * Math.PI;
      return new double[]{Math.cos(latA) * Math.sin(lonA), Math.sin(latA), -Math.cos(latA) * Math.cos(lonA)};
   }

   private static void shootingStars(double R, double vis) {
      double cx = R3.camX();
      double cy = R3.camY();
      double cz = R3.camZ();

      for (int k = 0; k < 2; k++) {
         double period = k == 0 ? 3.1 : 4.7;
         double dur = 1.1;
         double tt = System.currentTimeMillis() / 1000.0 + k * 1.7;
         int slot = (int)Math.floor(tt / period);
         double local = tt - slot * period;
         if (!(local > dur) && !(Anim.rnd(slot, 90 + k) > 0.7)) {
            double p = local / dur;
            double az = Anim.rnd(slot, 91 + k) * Math.PI * 2.0;
            double h = 0.35 + Anim.rnd(slot, 92 + k) * 0.45;
            double len = 0.12;
            double fade = p < 0.2 ? p / 0.2 : (1.0 - p) / 0.8;
            double hh = h - p * 0.25;
            double a2 = az + p * 0.35;
            double[] head = dir(Math.asin(hh), a2);
            double[] tail = dir(Math.asin(Math.min(1.0, hh + len * 0.6)), a2 - len);
            R3.line(
               cx + tail[0] * R,
               cy + tail[1] * R,
               cz + tail[2] * R,
               cx + head[0] * R,
               cy + head[1] * R,
               cz + head[2] * R,
               R * 0.004,
               R3.argb(11585791, 0.0),
               R3.argb(16777215, 0.95 * fade * vis)
            );
         }
      }
   }

   private static double[] dir(double elev, double az) {
      double c = Math.cos(elev);
      return new double[]{Math.cos(az) * c, Math.sin(elev), Math.sin(az) * c};
   }

   static {
      System.arraycopy(SkyGen.NAMES, 0, MODES, 0, SkyGen.NAMES.length);
      MODES[SkyGen.NAMES.length] = "Resourcepack";
      MODES[SkyGen.NAMES.length + 1] = "Eigenes Bild";
      MODES[SkyGen.NAMES.length + 2] = "Automatisch (Tageszeit)";
   }
}
