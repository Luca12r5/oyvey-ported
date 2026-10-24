package dev.lego.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public final class BrickModel {
   public static final int YAWS = 24;
   public static final int PITCHES = 7;
   public static final int SIZE = 112;
   public static final double[] PITCH = new double[]{-60.0, -30.0, 0.0, 22.0, 40.0, 62.0, 85.0};
   private static volatile int[][] frames;
   private static volatile boolean started;
   private static final Gx.Img[] IMG = new Gx.Img[168];
   private static int uploaded = 0;
   private static final double[] LIGHT = norm(-0.45, 0.85, 0.55);

   private BrickModel() {
   }

   public static void prepare() {
      if (!started) {
         started = true;
         Thread t = new Thread(() -> {
            int[][] f = new int[168][];

            for (int pi = 0; pi < 7; pi++) {
               for (int yi = 0; yi < 24; yi++) {
                  f[pi * 24 + yi] = render(112, yi * 360.0 / 24.0, PITCH[pi]);
               }
            }

            frames = f;
         }, "Lego-Intro-Bricks");
         t.setDaemon(true);
         t.start();
      }
   }

   public static boolean ready() {
      return frames != null;
   }

   public static boolean upload(int n) {
      if (frames == null) {
         return false;
      } else {
         while (n-- > 0 && uploaded < IMG.length) {
            int idx = uploaded;
            IMG[idx] = Gx.get("brick3d:" + idx, 0, () -> new int[][]{frames[idx], {112, 112}});
            uploaded++;
         }

         return uploaded >= IMG.length;
      }
   }

   public static Gx.Img sprite(double yaw, double pitch) {
      int yi = Math.floorMod(Math.round(yaw / 15.0), 24);
      int pi = 0;
      double best = 1.0E9;

      for (int i = 0; i < 7; i++) {
         double d = Math.abs(PITCH[i] - pitch);
         if (d < best) {
            best = d;
            pi = i;
         }
      }

      int idx = pi * 24 + yi;
      Gx.Img img = IMG[idx];
      if (img == null) {
         img = IMG[idx] = Gx.get("brick3d:" + idx, 0, () -> new int[][]{frames[idx], {112, 112}});
      }

      img.used = System.currentTimeMillis();
      return img;
   }

   static int[] render(int size, double yawDeg, double pitchDeg) {
      List<BrickModel.Face> faces = new ArrayList<>();
      double yaw = Math.toRadians(yawDeg);
      double pitch = Math.toRadians(pitchDeg);
      box(faces, -1.0, 0.0, -1.0, 1.0, 1.2, 1.0, yaw, pitch);

      for (int sx = -1; sx <= 1; sx += 2) {
         for (int sz = -1; sz <= 1; sz += 2) {
            stud(faces, sx * 0.5, sz * 0.5, yaw, pitch);
         }
      }

      faces.sort((a, b) -> Double.compare(a.depth, b.depth));
      BufferedImage img = new BufferedImage(size, size, 2);
      Graphics2D g = img.createGraphics();
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
      double sc = size / 4.6;
      double cx = size / 2.0;
      double cy = size / 2.0;

      for (BrickModel.Face f : faces) {
         Path2D path = new java.awt.geom.Path2D.Double();

         for (int i = 0; i < f.p.length; i++) {
            double x = cx + f.p[i][0] * sc;
            double y = cy - f.p[i][1] * sc;
            if (i == 0) {
               path.moveTo(x, y);
            } else {
               path.lineTo(x, y);
            }
         }

         path.closePath();
         int v = (int)Math.max(0.0, Math.min(255.0, 70.0 + 185.0 * f.light));
         g.setColor(new Color(v, v, v));
         g.fill(path);
         g.setColor(new Color(0, 0, 0, 38));
         g.setStroke(new BasicStroke((float)Math.max(0.6, size / 140.0)));
         g.draw(path);
      }

      g.dispose();
      return img.getRGB(0, 0, size, size, null, 0, size);
   }

   private static double[] norm(double x, double y, double z) {
      double l = Math.sqrt(x * x + y * y + z * z);
      return new double[]{x / l, y / l, z / l};
   }

   private static double[] rot(double x, double y, double z, double yaw, double pitch) {
      double x1 = x * Math.cos(yaw) + z * Math.sin(yaw);
      double z1 = -x * Math.sin(yaw) + z * Math.cos(yaw);
      double y0 = y - 0.6;
      double y2 = y0 * Math.cos(pitch) - z1 * Math.sin(pitch);
      double z2 = y0 * Math.sin(pitch) + z1 * Math.cos(pitch);
      double persp = 1.0 + z2 * 0.06;
      return new double[]{x1 * persp, y2 * persp, z2};
   }

   private static void add(List<BrickModel.Face> faces, double[][] local, double[] n, double yaw, double pitch) {
      double[][] p = new double[local.length][];
      double depth = 0.0;

      for (int i = 0; i < local.length; i++) {
         p[i] = rot(local[i][0], local[i][1], local[i][2], yaw, pitch);
         depth += p[i][2];
      }

      double[] rn = rot(n[0], n[1] + 0.6, n[2], yaw, pitch);
      if (!(rn[2] < -0.02)) {
         double lambert = Math.max(0.0, rn[0] * LIGHT[0] + rn[1] * LIGHT[1] + rn[2] * LIGHT[2]);
         faces.add(new BrickModel.Face(p, depth / local.length, 0.32 + 0.68 * lambert));
      }
   }

   private static void box(List<BrickModel.Face> f, double x0, double y0, double z0, double x1, double y1, double z1, double yaw, double pitch) {
      add(f, new double[][]{{x0, y1, z0}, {x1, y1, z0}, {x1, y1, z1}, {x0, y1, z1}}, new double[]{0.0, 1.0, 0.0}, yaw, pitch);
      add(f, new double[][]{{x0, y0, z1}, {x1, y0, z1}, {x1, y0, z0}, {x0, y0, z0}}, new double[]{0.0, -1.0, 0.0}, yaw, pitch);
      add(f, new double[][]{{x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}}, new double[]{0.0, 0.0, 1.0}, yaw, pitch);
      add(f, new double[][]{{x1, y0, z0}, {x0, y0, z0}, {x0, y1, z0}, {x1, y1, z0}}, new double[]{0.0, 0.0, -1.0}, yaw, pitch);
      add(f, new double[][]{{x1, y0, z1}, {x1, y0, z0}, {x1, y1, z0}, {x1, y1, z1}}, new double[]{1.0, 0.0, 0.0}, yaw, pitch);
      add(f, new double[][]{{x0, y0, z0}, {x0, y0, z1}, {x0, y1, z1}, {x0, y1, z0}}, new double[]{-1.0, 0.0, 0.0}, yaw, pitch);
   }

   private static void stud(List<BrickModel.Face> f, double cx, double cz, double yaw, double pitch) {
      int n = 16;
      double r = 0.31;
      double y0 = 1.2;
      double y1 = 1.42;
      double[][] cap = new double[n][];

      for (int i = 0; i < n; i++) {
         double a0 = (Math.PI * 2) * i / n;
         double a1 = (Math.PI * 2) * (i + 1) / n;
         double xa = cx + Math.cos(a0) * r;
         double za = cz + Math.sin(a0) * r;
         double xb = cx + Math.cos(a1) * r;
         double zb = cz + Math.sin(a1) * r;
         double am = (a0 + a1) / 2.0;
         add(f, new double[][]{{xa, y0, za}, {xb, y0, zb}, {xb, y1, zb}, {xa, y1, za}}, new double[]{Math.cos(am), 0.0, Math.sin(am)}, yaw, pitch);
         cap[i] = new double[]{xa, y1, za};
      }

      add(f, cap, new double[]{0.0, 1.0, 0.0}, yaw, pitch);
   }

   private record Face(double[][] p, double depth, double light) {
   }
}
