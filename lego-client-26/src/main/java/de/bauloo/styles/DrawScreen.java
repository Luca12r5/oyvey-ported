package de.bauloo.styles;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

public class DrawScreen extends Screen {
   private static final List<DrawScreen.Target> TARGETS = List.of(
      new DrawScreen.Target(BaulooStylesClient.Tab.CRYSTAL, "Crystal", "textures/entity/end_crystal/end_crystal.png"),
      new DrawScreen.Target(BaulooStylesClient.Tab.OBSIDIAN, "Obsidian", "textures/block/obsidian.png"),
      new DrawScreen.Target(BaulooStylesClient.Tab.ANCHOR, "Anchor", "textures/block/respawn_anchor_side0.png"),
      new DrawScreen.Target(BaulooStylesClient.Tab.TOTEM, "Totem", "textures/item/totem_of_undying.png"),
      new DrawScreen.Target(BaulooStylesClient.Tab.PEARL, "Pearl", "textures/item/ender_pearl.png"),
      new DrawScreen.Target(BaulooStylesClient.Tab.GAP, "Gap", "textures/item/golden_apple.png"),
      new DrawScreen.Target(BaulooStylesClient.Tab.SWORD, "Sword", "textures/item/netherite_sword.png")
   );
   public static final String DRAWN = "drawn_";
   private static final String META = "{\"pack\": {\"description\": \"Drawn with Bauloo Styles\", \"pack_format\": 75, \"min_format\": 65, \"max_format\": 9999}}\n";
   private static final int[][] CORE = new int[][]{{80, 0}, {96, 0}, {64, 16}, {80, 16}, {96, 16}, {112, 16}};
   private final Screen parent;
   private int target = 3;
   private DrawScreen.Tool tool = DrawScreen.Tool.PENCIL;
   private boolean mirror;
   private final int[] px = new int[256];
   private int[] base = new int[256];
   private BufferedImage full;
   private final List<int[]> anchorSides = new ArrayList<>();
   private final ArrayDeque<int[]> undo = new ArrayDeque<>();
   private final ArrayDeque<int[]> redo = new ArrayDeque<>();
   private int color = -6595329;
   private float hue = 0.74F;
   private float value = 1.0F;
   private String name = "";
   private boolean nameFocused;
   private String message = "";
   private boolean painting;
   private int dragBar;
   private int canvasX;
   private int canvasY;
   private int cell;
   private int barX;
   private int barW;
   private final List<DrawScreen.Hit> hits = new ArrayList<>();
   private static int count = -1;
   private static int countVersion = -1;

   public DrawScreen(Screen parent) {
      super(Component.literal("Draw your own"));
      this.parent = parent;
   }

   protected void init() {
      if (this.full == null) {
         this.load();
      }
   }

   private DrawScreen.Target current() {
      return TARGETS.get(this.target);
   }

   private static BufferedImage read(String path) {
      Minecraft mc = Minecraft.getInstance();

      try {
         Optional<Resource> resource = mc.getResourceManager().getResource(Identifier.withDefaultNamespace(path));
         if (resource.isEmpty()) {
            return null;
         } else {
            BufferedImage var4;
            try (InputStream in = resource.get().open()) {
               var4 = ImageIO.read(in);
            }

            return var4;
         }
      } catch (RuntimeException | IOException var8) {
         return null;
      }
   }

   private static BufferedImage scaled(BufferedImage src, int w, int h) {
      BufferedImage out = new BufferedImage(w, h, 2);

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            out.setRGB(x, y, src.getRGB(x * src.getWidth() / w, y * src.getHeight() / h));
         }
      }

      return out;
   }

   private static int[] grid(BufferedImage img, int ox, int oy) {
      int[] g = new int[256];

      for (int y = 0; y < 16; y++) {
         for (int x = 0; x < 16; x++) {
            g[y * 16 + x] = img.getRGB(ox + x, oy + y);
         }
      }

      return g;
   }

   private void load() {
      DrawScreen.Target t = this.current();
      this.anchorSides.clear();
      BufferedImage img = read(t.texture());
      if (img == null) {
         this.message = "Could not load the " + t.label().toLowerCase(Locale.ROOT) + " texture";
         this.base = new int[256];
      } else if (t.tab() == BaulooStylesClient.Tab.CRYSTAL) {
         this.full = scaled(img, 128, 64);
         this.base = grid(this.full, 80, 16);
      } else {
         this.full = scaled(img.getSubimage(0, 0, img.getWidth(), Math.min(img.getHeight(), img.getWidth())), 16, 16);
         this.base = grid(this.full, 0, 0);
         if (t.tab() == BaulooStylesClient.Tab.ANCHOR) {
            for (int i = 0; i <= 4; i++) {
               BufferedImage side = read("textures/block/respawn_anchor_side" + i + ".png");
               this.anchorSides
                  .add(
                     side == null
                        ? (int[])this.base.clone()
                        : grid(scaled(side.getSubimage(0, 0, side.getWidth(), Math.min(side.getHeight(), side.getWidth())), 16, 16), 0, 0)
                  );
            }
         }
      }

      System.arraycopy(this.base, 0, this.px, 0, 256);
      this.undo.clear();
      this.redo.clear();
   }

   private void remember() {
      this.undo.push((int[])this.px.clone());
      if (this.undo.size() > 60) {
         this.undo.removeLast();
      }

      this.redo.clear();
   }

   private void set(int x, int y) {
      int value = this.tool == DrawScreen.Tool.ERASER ? this.base[y * 16 + x] : this.color;
      this.px[y * 16 + x] = value;
      if (this.mirror) {
         this.px[y * 16 + 15 - x] = this.tool == DrawScreen.Tool.ERASER ? this.base[y * 16 + 15 - x] : this.color;
      }
   }

   private void fill(int x, int y) {
      int from = this.px[y * 16 + x];
      if (from != this.color) {
         ArrayDeque<int[]> todo = new ArrayDeque<>();
         todo.add(new int[]{x, y});

         while (!todo.isEmpty()) {
            int[] p = todo.poll();
            if (p[0] >= 0 && p[0] <= 15 && p[1] >= 0 && p[1] <= 15 && this.px[p[1] * 16 + p[0]] == from) {
               this.px[p[1] * 16 + p[0]] = this.color;
               todo.add(new int[]{p[0] + 1, p[1]});
               todo.add(new int[]{p[0] - 1, p[1]});
               todo.add(new int[]{p[0], p[1] + 1});
               todo.add(new int[]{p[0], p[1] - 1});
            }
         }
      }
   }

   private void paintAt(double mx, double my, boolean start) {
      int x = (int)Math.floor((mx - this.canvasX) / this.cell);
      int y = (int)Math.floor((my - this.canvasY) / this.cell);
      if (x >= 0 && x <= 15 && y >= 0 && y <= 15) {
         if (this.tool == DrawScreen.Tool.PICK) {
            if (this.px[y * 16 + x] >>> 24 != 0) {
               this.setColor(this.px[y * 16 + x]);
            }

            this.tool = DrawScreen.Tool.PENCIL;
         } else {
            if (start) {
               this.remember();
            }

            if (this.tool == DrawScreen.Tool.FILL) {
               if (start) {
                  this.fill(x, y);
               }
            } else {
               this.set(x, y);
            }
         }
      }
   }

   private void setColor(int argb) {
      this.color = argb | 0xFF000000;
      float[] hsv = StylesTheme.toHsv(this.color & 16777215);
      this.hue = hsv[0];
      this.value = hsv[2];
   }

   private void colorFromBars() {
      this.color = 0xFF000000 | StylesTheme.hsv(this.hue, this.value > 0.98F ? 0.75F : 0.85F, Math.max(0.08F, this.value));
   }

   private List<Integer> used() {
      Map<Integer, Integer> count = new HashMap<>();

      for (int p : this.px) {
         if (p >>> 24 != 0) {
            count.merge(p, 1, Integer::sum);
         }
      }

      List<Integer> out = new ArrayList<>(count.keySet());
      out.sort((a, b) -> count.get(b) - count.get(a));
      return out.size() > 12 ? out.subList(0, 12) : out;
   }

   private String slug() {
      String s = this.name.trim().replaceAll("[^A-Za-z0-9 _-]", "").replace(' ', '_');
      return s.isEmpty() ? "" : s.substring(0, Math.min(24, s.length()));
   }

   private static byte[] png(BufferedImage img) throws IOException {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ImageIO.write(img, "png", out);
      return out.toByteArray();
   }

   private static BufferedImage image16(int[] g) {
      BufferedImage img = new BufferedImage(16, 16, 2);

      for (int i = 0; i < 256; i++) {
         img.setRGB(i % 16, i / 16, g[i]);
      }

      return img;
   }

   private void save() {
      String slug = this.slug();
      if (slug.isEmpty()) {
         this.message = "Give your drawing a name first";
         this.nameFocused = true;
      } else {
         DrawScreen.Target t = this.current();

         try {
            Map<String, byte[]> files = new LinkedHashMap<>();
            String dir = "assets/minecraft/";
            if (t.tab() == BaulooStylesClient.Tab.CRYSTAL) {
               BufferedImage sheet = new BufferedImage(128, 64, 2);
               sheet.getGraphics().drawImage(this.full, 0, 0, null);

               for (int[] f : CORE) {
                  for (int i = 0; i < 256; i++) {
                     sheet.setRGB(f[0] + i % 16, f[1] + i / 16, this.px[i]);
                  }
               }

               files.put(dir + t.texture(), png(sheet));
            } else if (t.tab() != BaulooStylesClient.Tab.ANCHOR) {
               files.put(dir + t.texture(), png(image16(this.px)));
            } else {
               int[] empty = this.anchorSides.isEmpty() ? this.base : this.anchorSides.get(0);

               for (int level = 0; level <= 4; level++) {
                  int[] side = this.anchorSides.isEmpty() ? this.base : this.anchorSides.get(level);
                  int[] out = (int[])this.px.clone();

                  for (int i = 0; i < 256; i++) {
                     if (side[i] != empty[i]) {
                        out[i] = side[i];
                     }
                  }

                  files.put(dir + "textures/block/respawn_anchor_side" + level + ".png", png(image16(out)));
               }
            }

            Path folder = StylesUploadManager.root().resolve(t.tab().prefix);
            Files.createDirectories(folder);
            Path zip = folder.resolve("drawn_" + slug + ".zip");

            try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
               out.putNextEntry(new ZipEntry("pack.mcmeta"));
               out.write(
                  "{\"pack\": {\"description\": \"Drawn with Bauloo Styles\", \"pack_format\": 75, \"min_format\": 65, \"max_format\": 9999}}\n"
                     .getBytes(StandardCharsets.UTF_8)
               );
               out.closeEntry();

               for (Entry<String, byte[]> e : files.entrySet()) {
                  out.putNextEntry(new ZipEntry(e.getKey()));
                  out.write(e.getValue());
                  out.closeEntry();
               }
            }

            Files.write(folder.resolve("drawn_" + slug + ".png"), png(scaled(image16(this.px), 128, 128)));
            StylesUploadManager.changed();
            StylesUploadManager.sync(t.tab().prefix);
            count = -1;
            this.message = "Saved - pick it in " + t.tab().title + " (it is marked LOCAL)";
         } catch (IOException var13) {
            this.message = "Could not save: " + var13.getMessage();
         }
      }
   }

   public static int count() {
      if (count < 0 || countVersion != StylesUploadManager.version()) {
         int n = 0;

         for (DrawScreen.Target t : TARGETS) {
            for (Path p : StylesUploadManager.list(t.tab().prefix)) {
               if (p.getFileName().toString().startsWith("drawn_")) {
                  n++;
               }
            }
         }

         count = n;
         countVersion = StylesUploadManager.version();
      }

      return count;
   }

   public static boolean isDrawing(Path upload) {
      return upload != null && upload.getFileName().toString().startsWith("drawn_");
   }

   public static boolean isDrawingFolder(String folder) {
      return folder != null && folder.startsWith("bauloo_upload_") && folder.contains("_drawn_");
   }

   private static void box(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int color) {
      ctx.fill(x + 1, y, x + w - 1, y + h, color);
      ctx.fill(x, y + 1, x + 1, y + h - 1, color);
      ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
   }

   private static void outline(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int color) {
      ctx.fill(x + 1, y, x + w - 1, y + 1, color);
      ctx.fill(x + 1, y + h - 1, x + w - 1, y + h, color);
      ctx.fill(x, y + 1, x + 1, y + h - 1, color);
      ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
   }

   private boolean in(double mx, double my, int x, int y, int w, int h) {
      return mx >= x && mx < x + w && my >= y && my < y + h;
   }

   private void button(GuiGraphicsExtractor ctx, int x, int y, int w, int h, String label, boolean on, boolean primary, int mx, int my, Runnable action) {
      StylesTheme c = StylesUiConfig.theme();
      boolean hover = this.in(mx, my, x, y, w, h);
      int fill = primary ? (hover ? c.accentBright : c.accent) : (on ? c.accentTint : (hover ? c.surfaceHover : c.surface2));
      box(ctx, x, y, w, h, fill);
      if (on) {
         outline(ctx, x, y, w, h, c.accent);
      }

      int tc = primary ? c.onAccent : (on ? c.accentBright : (hover ? c.text : c.textSoft));
      ctx.text(this.font, label, x + (w - this.font.width(label)) / 2, y + (h - 8) / 2, tc, false);
      this.hits.add(new DrawScreen.Hit(x, y, w, h, action));
   }

   public void extractRenderState(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
      StylesTheme c = StylesUiConfig.theme();
      this.hits.clear();
      ctx.fill(0, 0, this.width, this.height, c.backdrop);
      int x0 = 8;
      int y0 = 8;
      int x1 = this.width - 8;
      int y1 = this.height - 8;
      box(ctx, x0, y0, x1 - x0, y1 - y0, c.frame);
      ctx.text(this.font, "Draw your own", x0 + 10, y0 + 9, c.text, false);
      String sub = "Paint a texture pixel by pixel. Your drawings stay on this PC - only you can use them.";
      ctx.text(this.font, this.font.plainSubstrByWidth(sub, x1 - x0 - 90), x0 + 10, y0 + 21, c.textDim, false);
      this.button(ctx, x1 - 60, y0 + 8, 50, 16, "BACK", false, false, mx, my, this::onClose);
      int cx = x0 + 10;

      for (int i = 0; i < TARGETS.size(); i++) {
         String label = TARGETS.get(i).label();
         int w = this.font.width(label) + 14;
         int index = i;
         this.button(ctx, cx, y0 + 36, w, 16, label, i == this.target, false, mx, my, () -> {
            if (index != this.target) {
               this.target = index;
               this.full = null;
               this.load();
               this.message = "";
            }
         });
         cx += w + 4;
      }

      int top = y0 + 60;
      int bottom = y1 - 10;
      int tx = x0 + 10;
      int tw = 54;
      String[] names = new String[]{"Pencil", "Eraser", "Fill", "Pick"};
      int ty = top;

      for (int i = 0; i < names.length; i++) {
         DrawScreen.Tool t = DrawScreen.Tool.values()[i];
         this.button(ctx, tx, ty, tw, 18, names[i], this.tool == t, false, mx, my, () -> this.tool = t);
         ty += 22;
      }

      this.button(ctx, tx, ty, tw, 18, "Mirror", this.mirror, false, mx, my, () -> this.mirror = !this.mirror);
      ty += 22;
      this.button(ctx, tx, ty, tw, 18, "Undo", false, false, mx, my, () -> {
         if (!this.undo.isEmpty()) {
            this.redo.push((int[])this.px.clone());
            System.arraycopy(this.undo.pop(), 0, this.px, 0, 256);
         }
      });
      ty += 22;
      this.button(ctx, tx, ty, tw, 18, "Redo", false, false, mx, my, () -> {
         if (!this.redo.isEmpty()) {
            this.undo.push((int[])this.px.clone());
            System.arraycopy(this.redo.pop(), 0, this.px, 0, 256);
         }
      });
      int sideW = Math.max(150, Math.min(220, (x1 - x0) / 3));
      int room = Math.min(bottom - top, x1 - 10 - sideW - 12 - (tx + tw + 10));
      this.cell = Math.max(4, room / 16);
      int size = this.cell * 16;
      this.canvasX = tx + tw + 10;
      this.canvasY = top;
      box(ctx, this.canvasX - 3, this.canvasY - 3, size + 6, size + 6, c.base);

      for (int y = 0; y < 16; y++) {
         for (int x = 0; x < 16; x++) {
            int p = this.px[y * 16 + x];
            int px0 = this.canvasX + x * this.cell;
            int py0 = this.canvasY + y * this.cell;
            if (p >>> 24 < 255) {
               int check = (x + y) % 2 == 0 ? -14015952 : -14936542;
               ctx.fill(px0, py0, px0 + this.cell, py0 + this.cell, check);
            }

            if (p >>> 24 != 0) {
               ctx.fill(px0, py0, px0 + this.cell, py0 + this.cell, p);
            }
         }
      }

      if (this.cell >= 8) {
         for (int k = 1; k < 16; k++) {
            ctx.fill(this.canvasX + k * this.cell, this.canvasY, this.canvasX + k * this.cell + 1, this.canvasY + size, 352321535);
            ctx.fill(this.canvasX, this.canvasY + k * this.cell, this.canvasX + size, this.canvasY + k * this.cell + 1, 352321535);
         }
      }

      if (this.mirror) {
         ctx.fill(this.canvasX + size / 2, this.canvasY, this.canvasX + size / 2 + 1, this.canvasY + size, -1996501158);
      }

      if (this.in(mx, my, this.canvasX, this.canvasY, size, size)) {
         int hx = this.canvasX + (mx - this.canvasX) / this.cell * this.cell;
         int hy = this.canvasY + (my - this.canvasY) / this.cell * this.cell;
         outline(ctx, hx - 1, hy - 1, this.cell + 2, this.cell + 2, -12454);
      }

      int sx = this.canvasX + size + 12;
      int sw = x1 - 10 - sx;
      box(ctx, sx, top, sw, 92, c.surface);
      ctx.text(this.font, "COLOUR", sx + 8, top + 7, c.textDim, false);
      box(ctx, sx + 8, top + 19, 18, 18, this.color);
      ctx.text(this.font, String.format("#%06X", this.color & 16777215), sx + 32, top + 24, c.text, false);
      int barW = sw - 16;
      this.barX = sx + 8;
      this.barW = barW;
      int hueY = top + 42;

      for (int i = 0; i < barW; i++) {
         ctx.fill(sx + 8 + i, hueY, sx + 9 + i, hueY + 6, 0xFF000000 | StylesTheme.hsv((float)i / barW, 0.8F, 1.0F));
      }

      int hk = sx + 8 + Math.round(this.hue * (barW - 1));
      ctx.fill(hk - 1, hueY - 2, hk + 1, hueY + 8, -1);
      int valY = hueY + 10;

      for (int i = 0; i < barW; i++) {
         ctx.fill(sx + 8 + i, valY, sx + 9 + i, valY + 6, 0xFF000000 | StylesTheme.hsv(this.hue, 0.85F, 0.08F + 0.92F * i / barW));
      }

      int vk = sx + 8 + Math.round(this.value * (barW - 1));
      ctx.fill(vk - 1, valY - 2, vk + 1, valY + 8, -1);
      this.hits.add(new DrawScreen.Hit(sx + 8, hueY - 3, barW, 10, () -> this.dragBar = 1));
      this.hits.add(new DrawScreen.Hit(sx + 8, valY - 3, barW, 10, () -> this.dragBar = 2));
      int px0x = sx + 8;
      int py0x = valY + 12;
      List<Integer> used = this.used();
      int sz = Math.max(8, Math.min(12, (barW - 33) / 12));

      for (int i = 0; i < used.size(); i++) {
         int col = used.get(i);
         int bx = px0x + i * (sz + 3);
         box(ctx, bx, py0x, sz, sz, col);
         this.hits.add(new DrawScreen.Hit(bx, py0x, sz, sz, () -> this.setColor(col)));
      }

      int prevY = top + 98;
      int prevH = Math.min(70, Math.max(40, bottom - prevY - 120));
      box(ctx, sx, prevY, sw, prevH, c.surface);
      ctx.text(this.font, "PREVIEW", sx + 8, prevY + 7, c.textDim, false);
      int pc = Math.max(1, (prevH - 22) / 16);
      int pvx = sx + sw - 10 - pc * 16;

      for (int i = 0; i < 256; i++) {
         if (this.px[i] >>> 24 != 0) {
            int x = pvx + i % 16 * pc;
            int y = prevY + 16 + i / 16 * pc;
            ctx.fill(x, y, x + pc, y + pc, this.px[i]);
         }
      }

      ctx.text(this.font, "Updates while", sx + 8, prevY + 22, c.textSoft, false);
      ctx.text(this.font, "you draw", sx + 8, prevY + 32, c.textSoft, false);
      int ny = prevY + prevH + 6;
      box(ctx, sx, ny, sw, 62, c.surface);
      ctx.text(this.font, "NAME", sx + 8, ny + 7, c.textDim, false);
      box(ctx, sx + 8, ny + 18, sw - 16, 16, c.base);
      if (this.nameFocused) {
         outline(ctx, sx + 8, ny + 18, sw - 16, 16, c.accent);
      }

      String shown = this.name.isEmpty() && !this.nameFocused
         ? "Name your drawing"
         : this.name + (this.nameFocused && System.currentTimeMillis() / 500L % 2L == 0L ? "_" : "");
      ctx.text(this.font, shown, sx + 12, ny + 22, this.name.isEmpty() && !this.nameFocused ? c.textDim : c.text, false);
      this.hits.add(new DrawScreen.Hit(sx + 8, ny + 18, sw - 16, 16, () -> this.nameFocused = true));
      int half = (sw - 20) / 2;
      this.button(ctx, sx + 8, ny + 40, half, 16, "START OVER", false, false, mx, my, () -> {
         this.remember();
         System.arraycopy(this.base, 0, this.px, 0, 256);
      });
      this.button(ctx, sx + 12 + half, ny + 40, half, 16, "SAVE", false, true, mx, my, this::save);
      int ly = ny + 68;
      if (ly + 26 <= bottom) {
         box(ctx, sx, ly, sw, 26, -14807274);

         for (String line : this.wrap("Only for you. Drawings are never sent anywhere: presets, keys and exports leave them out.", sw - 12)) {
            ctx.text(this.font, line, sx + 6, ly + 4, -15669, false);
            ly += 10;
            if (ly > ny + 68 + 16) {
               break;
            }
         }
      }

      if (!this.message.isEmpty()) {
         ctx.text(this.font, this.font.plainSubstrByWidth(this.message, x1 - x0 - 20), x0 + 10, y1 - 14, -9183056, false);
      }
   }

   private List<String> wrap(String text, int w) {
      List<String> lines = new ArrayList<>();
      StringBuilder line = new StringBuilder();

      for (String word : text.split(" ")) {
         String next = line.isEmpty() ? word : line + " " + word;
         if (this.font.width(next) > w && !line.isEmpty()) {
            lines.add(line.toString());
            line = new StringBuilder(word);
         } else {
            line = new StringBuilder(next);
         }
      }

      if (!line.isEmpty()) {
         lines.add(line.toString());
      }

      return lines;
   }

   public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
      double mx = click.x();
      double my = click.y();
      if (this.in(mx, my, this.canvasX, this.canvasY, this.cell * 16, this.cell * 16)) {
         this.nameFocused = false;
         if (click.button() == 1) {
            int x = (int)((mx - this.canvasX) / this.cell);
            int y = (int)((my - this.canvasY) / this.cell);
            if (this.px[y * 16 + x] >>> 24 != 0) {
               this.setColor(this.px[y * 16 + x]);
            }

            return true;
         } else {
            this.painting = true;
            this.paintAt(mx, my, true);
            return true;
         }
      } else {
         for (int i = this.hits.size() - 1; i >= 0; i--) {
            DrawScreen.Hit h = this.hits.get(i);
            if (this.in(mx, my, h.x(), h.y(), h.w(), h.h())) {
               this.nameFocused = false;
               h.action().run();
               if (this.dragBar != 0) {
                  this.dragBars(mx);
               }

               return true;
            }
         }

         this.nameFocused = false;
         return super.mouseClicked(click, doubled);
      }
   }

   private void dragBars(double mx) {
      float t = (float)Math.max(0.0, Math.min(1.0, (mx - this.barX) / Math.max(1, this.barW - 1)));
      if (this.dragBar == 1) {
         this.hue = t;
      } else {
         this.value = t;
      }

      this.colorFromBars();
   }

   public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
      if (this.painting && this.tool != DrawScreen.Tool.FILL && this.tool != DrawScreen.Tool.PICK) {
         this.paintAt(click.x(), click.y(), false);
         return true;
      } else if (this.dragBar != 0) {
         this.dragBars(click.x());
         return true;
      } else {
         return super.mouseDragged(click, offsetX, offsetY);
      }
   }

   public boolean mouseReleased(MouseButtonEvent click) {
      this.painting = false;
      this.dragBar = 0;
      return super.mouseReleased(click);
   }

   public boolean keyPressed(KeyEvent input) {
      int key = input.key();
      boolean ctrl = (input.modifiers() & 2) != 0;
      if (this.nameFocused) {
         if (key == 259 && !this.name.isEmpty()) {
            this.name = this.name.substring(0, this.name.length() - 1);
            return true;
         }

         if (key == 257) {
            this.nameFocused = false;
            this.save();
            return true;
         }

         if (key == 256) {
            this.nameFocused = false;
            return true;
         }
      }

      if (ctrl && key == 90 && !this.undo.isEmpty()) {
         this.redo.push((int[])this.px.clone());
         System.arraycopy(this.undo.pop(), 0, this.px, 0, 256);
         return true;
      } else if (ctrl && key == 89 && !this.redo.isEmpty()) {
         this.undo.push((int[])this.px.clone());
         System.arraycopy(this.redo.pop(), 0, this.px, 0, 256);
         return true;
      } else {
         return super.keyPressed(input);
      }
   }

   public boolean charTyped(CharacterEvent input) {
      if (this.nameFocused && input.isAllowedChatCharacter()) {
         String typed = input.codepointAsString();
         if (typed.matches("[A-Za-z0-9 _-]") && this.name.length() < 20) {
            this.name = this.name + typed;
         }

         return true;
      } else {
         return false;
      }
   }

   public void onClose() {
      Minecraft.getInstance().gui.setScreen(this.parent);
   }

   public boolean isPauseScreen() {
      return false;
   }

   private record Hit(int x, int y, int w, int h, Runnable action) {
   }

   private record Target(BaulooStylesClient.Tab tab, String label, String texture) {
   }

   private static enum Tool {
      PENCIL,
      ERASER,
      FILL,
      PICK;
   }
}
