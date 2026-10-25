package de.bauloo.styles;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_2960;

public final class LocalCapes {
   public static final String PREFIX = "local_";
   private static final long RESCAN_MS = 3000L;
   private static Map<String, LocalCapes.Entry> entries = Map.of();
   private static String[][] list = new String[0][];
   private static long scanned;
   private static final Map<String, AnimatedCape> CAPES = new HashMap<>();
   private static final Map<String, AnimatedIcon> ICON_ANIMS = new HashMap<>();
   private static String active;

   private LocalCapes() {
   }

   public static Path dir() {
      return FabricLoader.getInstance().getConfigDir().resolve("bauloo-styles").resolve("animated-capes");
   }

   public static String[][] list() {
      long now = System.currentTimeMillis();
      if (now - scanned > 3000L) {
         scanned = now;
         scan();
      }

      return list;
   }

   private static void scan() {
      Map<String, LocalCapes.Entry> found = new HashMap<>();
      List<String[]> rows = new ArrayList<>();
      Path root = dir();
      if (Files.isDirectory(root)) {
         try (Stream<Path> folders = Files.list(root).sorted()) {
            for (Path folder : folders::iterator) {
               if (Files.isDirectory(folder)) {
                  List<Path> frames;
                  try (Stream<Path> files = Files.list(folder)) {
                     frames = files.filter(f -> {
                        String n = f.getFileName().toString().toLowerCase(Locale.ROOT);
                        return n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png");
                     }).sorted().toList();
                  }

                  if (!frames.isEmpty()) {
                     String label = folder.getFileName().toString();
                     String key = "local_" + label.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
                     int fps = 12;
                     Path fpsFile = folder.resolve("fps.txt");
                     if (Files.exists(fpsFile)) {
                        try {
                           fps = Math.max(1, Math.min(60, Integer.parseInt(Files.readString(fpsFile).trim())));
                        } catch (IOException | NumberFormatException var14) {
                        }
                     }

                     boolean tint = Files.exists(folder.resolve("tint")) || Files.exists(folder.resolve("tint.txt"));
                     found.put(key, new LocalCapes.Entry(key, label, frames, fps, tint));
                     rows.add(new String[]{key, label});
                  }
               }
            }
         } catch (IOException e) {
            BaulooStylesClient.LOGGER.warn("Local capes: could not read {}: {}", root, e.toString());
         }
      }

      entries = found;
      list = rows.toArray(new String[0][]);
   }

   public static class_2960 current(String key) {
      LocalCapes.Entry entry = entries.get(key);
      if (entry == null) {
         return null;
      }

      if (active != null && !active.equals(key)) {
         AnimatedCape old = CAPES.remove(active);
         if (old != null) {
            old.release();
         }
      }

      active = key;
      AnimatedCape cape = CAPES.computeIfAbsent(
         key,
         k -> new AnimatedCape(class_2960.method_60655("bauloo_styles", "cosmetics/local/" + k.substring("local_".length())), new AnimatedCape.FrameSource() {
            @Override
            public int count() {
               return entry.frames().size();
            }

            @Override
            public BufferedImage read(int index) throws IOException {
               return ImageIO.read(entry.frames().get(index).toFile());
            }
         }, entry.fps(), entry.tint())
      );
      return cape.current();
   }

   public static class_2960 icon(String key) {
      LocalCapes.Entry entry = entries.get(key);
      if (entry == null) {
         return null;
      }

      AnimatedIcon icon = ICON_ANIMS.computeIfAbsent(
         key,
         k -> new AnimatedIcon(
            class_2960.method_60655("bauloo_styles", "cosmetics/local_icon/" + k.substring("local_".length())), new AnimatedCape.FrameSource() {
               @Override
               public int count() {
                  return entry.frames().size();
               }

               @Override
               public BufferedImage read(int index) throws IOException {
                  return ImageIO.read(entry.frames().get(index).toFile());
               }
            }, entry.fps()
         )
      );
      return icon.current();
   }

   private record Entry(String key, String label, List<Path> frames, int fps, boolean tint) {
   }
}
