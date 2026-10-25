package de.bauloo.styles;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public final class StylesUploadManager {
   private static final Path ROOT = FabricLoader.getInstance().getConfigDir().resolve("bauloo-styles/uploads");
   private static volatile int version;
   private static final Map<String, Path> SOURCES = new ConcurrentHashMap<>();
   private static final Map<String, Identifier> PREVIEWS = new HashMap<>();
   private static final Set<Path> AUTO_TRIED = ConcurrentHashMap.newKeySet();
   private static final String PACK_META = "{\"pack\": {\"description\": \"Bauloo uploaded style - by bauloo\", \"pack_format\": 75, \"min_format\": 65, \"max_format\": 9999}}\n";

   private StylesUploadManager() {
   }

   public static void openFolder() {
      try {
         Files.createDirectories(ROOT);
      } catch (IOException var1) {
      }

      Util.getPlatform().openPath(ROOT);
   }

   public static void open() {
      String[] actions = new String[]{"Add Files", "View My Uploads"};
      int action = JOptionPane.showOptionDialog(null, "Manage your local style uploads.", "Bauloo Styles - My Uploads", -1, -1, null, actions, actions[0]);
      if (action == 1) {
         StringBuilder list = new StringBuilder("Your local uploads:\n\n");

         for (String category : new String[]{"Crystal", "Anchor", "Armor", "Fire", "Sky"}) {
            List<Path> packs = list(category);
            list.append(category).append(" ( ").append(packs.size()).append(" )\n");

            for (Path pack : packs) {
               list.append("  ").append(pack.getFileName()).append('\n');
            }
         }

         JOptionPane.showMessageDialog(null, list.toString(), "Bauloo Styles - My Uploads", 1);
      } else if (action == 0) {
         FileDialog chooser = new FileDialog((Frame)null, "Bauloo Styles - Add Files", 0);
         chooser.setFilenameFilter((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".zip"));
         chooser.setMultipleMode(true);
         chooser.setAlwaysOnTop(true);
         chooser.setVisible(true);
         Path[] files = Arrays.stream(chooser.getFiles()).map(File::toPath).toArray(Path[]::new);
         if (files.length != 0) {
            String[] categories = new String[]{"Crystal", "Anchor", "Armor", "Fire", "Sky"};
            String category = (String)JOptionPane.showInputDialog(
               null, "What do you want to copy?\nChoose the category for the selected files.", "Bauloo Styles - Import", 3, null, categories, "Sky"
            );
            if (category != null) {
               String prefix = category.toLowerCase(Locale.ROOT) + "_";
               int copied = 0;

               try {
                  Path target = ROOT.resolve(prefix.substring(0, prefix.length() - 1));
                  Files.createDirectories(target);

                  for (Path file : files) {
                     Path destination = target.resolve(file.getFileName().toString());
                     Files.copy(file, destination, StandardCopyOption.REPLACE_EXISTING);
                     copied++;
                  }

                  JOptionPane.showMessageDialog(null, copied + " file(s) copied to My Uploads / " + category + ".", "Bauloo Styles", 1);
               } catch (IOException var14) {
                  JOptionPane.showMessageDialog(null, "Could not copy the selected files: " + var14.getMessage(), "Bauloo Styles", 0);
               }
            }
         }
      }
   }

   public static List<Path> list(String category) {
      Path folder = ROOT.resolve(category.toLowerCase(Locale.ROOT));

      try {
         List var3;
         try (Stream<Path> stream = Files.exists(folder) ? Files.list(folder) : Stream.empty()) {
            var3 = stream.filter(p -> Files.isDirectory(p) || p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".zip")).sorted().toList();
         }

         return var3;
      } catch (IOException var7) {
         return List.of();
      }
   }

   public static Path root() {
      return ROOT;
   }

   public static int version() {
      return version;
   }

   public static void changed() {
      version++;
   }

   static String packIdFor(String category, Path file) {
      String safe = file.getFileName().toString().replaceAll("[^A-Za-z0-9._-]", "_");
      return "bauloo_upload_"
         + category.toLowerCase(Locale.ROOT)
         + "_"
         + safe.replaceFirst("\\.zip$", "")
         + (category.equalsIgnoreCase("sky") ? "_skyonly" : "")
         + (Files.isDirectory(file) ? "_folder" : "");
   }

   public static Identifier preview(String id) {
      Path source = SOURCES.get(id);
      if (source == null) {
         return null;
      } else {
         Path png = source.resolveSibling(source.getFileName().toString().replaceFirst("\\.zip$", "") + ".png");

         try {
            if (!Files.exists(png)) {
               autoPreview(source, png);
               return null;
            } else {
               String key = id + "@" + Files.getLastModifiedTime(png).toMillis();
               Identifier cached = PREVIEWS.get(key);
               if (cached != null) {
                  return cached;
               } else {
                  Identifier texture = Identifier.fromNamespaceAndPath("bauloo_styles", "upload/" + Integer.toHexString(key.hashCode()));
                  NativeImage image = PreviewTexture.shrink(NativeImage.read(Files.readAllBytes(png)));
                  Minecraft.getInstance().getTextureManager().register(texture, new PreviewTexture(texture, image));
                  PREVIEWS.put(key, texture);
                  return texture;
               }
            }
         } catch (RuntimeException | IOException var7) {
            return null;
         }
      }
   }

   private static void autoPreview(Path source, Path png) {
      if (AUTO_TRIED.add(source)) {
         String category = source.getParent().getFileName().toString().toLowerCase(Locale.ROOT);
         Thread thread = new Thread(() -> {
            Path temp = null;

            try {
               Path zip = source;
               if (Files.isDirectory(source)) {
                  temp = Files.createTempFile("bauloo-preview-", ".zip");
                  zipFolder(source, temp);
                  zip = temp;
               }

               if ("sky".equals(category)) {
                  StylesUploadServer.skyPreview(zip, png);
               } else {
                  StylesAutoPreview.render(zip, category, png);
               }
            } catch (RuntimeException | IOException var13) {
               BaulooStylesClient.LOGGER.warn("Auto preview for {} failed: {}", source.getFileName(), var13.toString());
            } finally {
               if (temp != null) {
                  try {
                     Files.deleteIfExists(temp);
                  } catch (IOException var12) {
                  }
               }
            }
         }, "Bauloo Styles preview");
         thread.setDaemon(true);
         thread.start();
      }
   }

   private static void zipFolder(Path folder, Path zip) throws IOException {
      try (
         ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip));
         Stream<Path> paths = Files.walk(folder);
      ) {
         for (Path path : paths.toList()) {
            if (!Files.isDirectory(path)) {
               out.putNextEntry(new ZipEntry(folder.relativize(path).toString().replace('\\', '/')));
               Files.copy(path, out);
               out.closeEntry();
            }
         }
      }
   }

   public static boolean delete(String category, String fileName) {
      if (!fileName.matches("[^/\\\\]+\\.zip")) {
         return false;
      } else {
         Path folder = ROOT.resolve(category.toLowerCase(Locale.ROOT));
         Path zip = folder.resolve(fileName);
         if (!zip.normalize().startsWith(folder.normalize())) {
            return false;
         } else {
            try {
               String id = packIdFor(category, zip);
               boolean removed = Files.deleteIfExists(zip);
               Files.deleteIfExists(folder.resolve(fileName.replaceFirst("\\.zip$", "") + ".png"));

               try {
                  Files.deleteIfExists(FabricLoader.getInstance().getGameDir().resolve("resourcepacks").resolve(id + ".zip"));
               } catch (IOException var7) {
               }

               changed();
               return removed;
            } catch (IOException var8) {
               return false;
            }
         }
      }
   }

   public static List<String[]> sync(String category) {
      List<String[]> result = new ArrayList<>();
      Path targetRoot = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");

      try {
         Files.createDirectories(targetRoot);
      } catch (IOException var7) {
         return result;
      }

      for (Path file : list(category)) {
         String id = packIdFor(category, file);
         SOURCES.put(id, file);

         try {
            if (Files.isDirectory(file)) {
               copyFolder(file, targetRoot.resolve(id), category);
               result.add(new String[]{id, file.getFileName().toString()});
               continue;
            }

            Path mirrored = targetRoot.resolve(id + ".zip");
            if (!Files.exists(mirrored) || Files.getLastModifiedTime(file).compareTo(Files.getLastModifiedTime(mirrored)) > 0) {
               normalizePack(file, mirrored, category);
            }
         } catch (IOException var8) {
            BaulooStylesClient.LOGGER.warn("Upload {} could not be prepared: {}", file.getFileName(), var8.toString());
            if (Files.isDirectory(file) || !Files.exists(targetRoot.resolve(id + ".zip"))) {
               continue;
            }
         }

         result.add(new String[]{id, file.getFileName().toString().replaceFirst("\\.zip$", "")});
      }

      return result;
   }

   private static void normalizePack(Path source, Path destination, String category) throws IOException {
      Path temp = Files.createTempFile("bauloo-upload-", ".zip");
      boolean metadata = false;

      try (
         ZipInputStream in = new ZipInputStream(Files.newInputStream(source));
         ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(temp));
      ) {
         ZipEntry entry;
         while ((entry = in.getNextEntry()) != null) {
            if (!entry.isDirectory()
               && (
                  !category.equalsIgnoreCase("sky")
                     || entry.getName().equalsIgnoreCase("pack.mcmeta")
                     || entry.getName().startsWith("assets/minecraft/optifine/sky/")
               )) {
               out.putNextEntry(new ZipEntry(entry.getName()));
               if (entry.getName().equalsIgnoreCase("pack.mcmeta")) {
                  metadata = true;
                  out.write(
                     "{\"pack\": {\"description\": \"Bauloo uploaded style - by bauloo\", \"pack_format\": 75, \"min_format\": 65, \"max_format\": 9999}}\n"
                        .getBytes(StandardCharsets.UTF_8)
                  );
               } else {
                  in.transferTo(out);
               }

               out.closeEntry();
            }
         }

         if (!metadata) {
            out.putNextEntry(new ZipEntry("pack.mcmeta"));
            out.write(
               "{\"pack\": {\"description\": \"Bauloo uploaded style - by bauloo\", \"pack_format\": 75, \"min_format\": 65, \"max_format\": 9999}}\n"
                  .getBytes(StandardCharsets.UTF_8)
            );
            out.closeEntry();
         }
      }

      Files.move(temp, destination, StandardCopyOption.REPLACE_EXISTING);
   }

   private static void copyFolder(Path source, Path destination, String category) throws IOException {
      if (Files.exists(destination)) {
         try (Stream<Path> old = Files.walk(destination)) {
            old.sorted(Comparator.reverseOrder()).forEach(p -> {
               try {
                  Files.deleteIfExists(p);
               } catch (IOException var2x) {
               }
            });
         }
      }

      try (Stream<Path> paths = Files.walk(source)) {
         for (Path path : paths.toList()) {
            if (!Files.isDirectory(path)) {
               Path relative = source.relativize(path);
               String name = relative.toString().replace('\\', '/');
               if (!category.equalsIgnoreCase("sky") || name.equalsIgnoreCase("pack.mcmeta") || name.startsWith("assets/minecraft/optifine/sky/")) {
                  Path out = destination.resolve(relative);
                  Files.createDirectories(out.getParent());
                  Files.copy(path, out, StandardCopyOption.REPLACE_EXISTING);
               }
            }
         }
      }

      Path meta = destination.resolve("pack.mcmeta");
      if (!Files.exists(meta)) {
         Files.writeString(
            meta, "{\"pack\": {\"description\": \"Bauloo uploaded style - by bauloo\", \"pack_format\": 75, \"min_format\": 65, \"max_format\": 9999}}\n"
         );
      }
   }
}
