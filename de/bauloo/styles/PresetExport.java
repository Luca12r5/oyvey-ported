package de.bauloo.styles;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public final class PresetExport {
   private static final String UPLOAD = "bauloo_upload_";
   private static final String MYPACK = "mypack:";
   private static final String VIPPACK = "vippack:";
   public static volatile boolean busy;
   public static volatile Path lastFolder;

   private PresetExport() {
   }

   public static CompletableFuture<String> export(StylesPresets.Preset preset) {
      busy = true;
      return CompletableFuture.supplyAsync(() -> {
         String var2;
         try {
            return run(preset);
         } catch (Exception var6) {
            BaulooStylesClient.LOGGER.warn("[PresetExport] {} failed: {}", preset.name(), var6.toString());
            var2 = "Export failed: " + var6.getMessage();
         } finally {
            busy = false;
         }

         return var2;
      });
   }

   private static String run(StylesPresets.Preset preset) throws Exception {
      lastFolder = null;
      Path packs = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
      String name = preset.name().replaceAll("[^A-Za-z0-9 _()-]", "").trim();
      if (name.isEmpty()) {
         name = "Preset";
      }

      Path out = packs.resolve(name + " (Bauloo Styles).zip");
      Map<BaulooStylesClient.Tab, Path> uploads = PresetShare.localSources(preset);
      Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer("bauloo_styles");
      Set<String> written = new HashSet<>();
      int exported = 0;
      int vip = 0;
      int drawings = 0;
      int failed = 0;
      Path temp = Files.createTempFile("bauloo-export-", ".zip");

      try {
         ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(temp));

         try {
            String meta = "{\"pack\": {\"description\": \""
               + name.replace("\"", "")
               + " - exported from Bauloo Styles\", \"pack_format\": 75, \"min_format\": 65, \"max_format\": 9999}}\n";
            zip.putNextEntry(new ZipEntry("pack.mcmeta"));
            zip.write(meta.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();

            for (BaulooStylesClient.Tab tab : BaulooStylesClient.Tab.values()) {
               String folder = preset.styles().get(tab);
               if (folder != null) {
                  if (folder.startsWith("vippack:")) {
                     vip++;
                  } else if (DrawScreen.isDrawingFolder(folder)) {
                     drawings++;
                  } else {
                     boolean ok = false;
                     if (folder.startsWith("bauloo_cloud_")) {
                        StylesCloud.Style s = StylesCloud.byFolder(folder);
                        if (s != null && StylesCloud.download(s).get(3L, TimeUnit.MINUTES)) {
                           ok = copyZip(packs.resolve(folder + ".zip"), zip, written);
                        }
                     } else if (folder.startsWith("bauloo_upload_")) {
                        Path source = uploads.get(tab);
                        if (source != null) {
                           ok = Files.isDirectory(source) ? copyTree(source, zip, written) : copyZip(source, zip, written);
                        }
                     } else if (folder.startsWith("mypack:")) {
                        MyPacks.Style s = MyPacks.byId(folder.substring("mypack:".length()));
                        if (s != null) {
                           Path cut = Files.createTempFile("bauloo-export-cut-", ".zip");

                           try {
                              Files.delete(cut);
                              ok = StylesPackFilter.extract(packs.resolve(s.pack()), s.category(), cut) > 0 && copyZip(cut, zip, written);
                           } finally {
                              Files.deleteIfExists(cut);
                           }
                        }
                     } else if (mod.isPresent()) {
                        Optional<Path> root = mod.get().findPath("resourcepacks/" + folder);
                        ok = root.isPresent() && copyTree(root.get(), zip, written);
                     }

                     if (ok) {
                        exported++;
                     } else {
                        failed++;
                        BaulooStylesClient.LOGGER.info("[PresetExport] {}: {} could not be exported", tab.prefix, folder);
                     }
                  }
               }
            }
         } catch (Throwable var341) {
            try {
               zip.close();
            } catch (Throwable var32) {
               var341.addSuppressed(var32);
            }

            throw var341;
         }

         zip.close();
         if (exported == 0) {
            return vip > 0
               ? "VIP styles can't be exported"
               : (drawings > 0 ? "Drawings stay on this PC - nothing to export" : "Nothing to export in this preset");
         }

         Files.createDirectories(packs);
         Files.move(temp, out, StandardCopyOption.REPLACE_EXISTING);
      } finally {
         Files.deleteIfExists(temp);
      }

      lastFolder = packs;
      StringBuilder var361 = new StringBuilder("Saved \"" + out.getFileName() + "\" in your resourcepacks folder\nTurn it on in Options > Resource Packs");
      if (failed > 0) {
         var361.append(" - ").append(failed).append(failed == 1 ? " style is" : " styles are").append(" not on this PC");
      }

      if (vip > 0) {
         var361.append(" - ").append(vip).append(" VIP left out");
      }

      if (drawings > 0) {
         var361.append(" - ").append(drawings).append(drawings == 1 ? " drawing" : " drawings").append(" left out");
      }

      return var361.toString();
   }

   private static boolean wanted(String name) {
      return name.startsWith("assets/") && !name.contains("..") && !name.endsWith("/");
   }

   private static boolean copyZip(Path source, ZipOutputStream zip, Set<String> written) throws IOException {
      if (!Files.isRegularFile(source)) {
         return false;
      } else {
         boolean any = false;

         ZipEntry entry;
         try (ZipInputStream in = new ZipInputStream(Files.newInputStream(source))) {
            while ((entry = in.getNextEntry()) != null) {
               String name = entry.getName().replace('\\', '/');
               if (!entry.isDirectory() && wanted(name)) {
                  any = true;
                  if (written.add(name.toLowerCase(Locale.ROOT))) {
                     zip.putNextEntry(new ZipEntry(name));
                     in.transferTo(zip);
                     zip.closeEntry();
                  }
               }
            }
         }

         return any;
      }
   }

   private static boolean copyTree(Path root, ZipOutputStream zip, Set<String> written) throws IOException {
      if (!Files.isDirectory(root)) {
         return false;
      } else {
         boolean any = false;

         List<Path> files;
         try (Stream<Path> walk = Files.walk(root)) {
            files = walk.filter(x$0 -> Files.isRegularFile(x$0)).toList();
         }

         for (Path file : files) {
            String name = root.relativize(file).toString().replace('\\', '/');
            if (wanted(name)) {
               any = true;
               if (written.add(name.toLowerCase(Locale.ROOT))) {
                  zip.putNextEntry(new ZipEntry(name));

                  try (InputStream in = Files.newInputStream(file)) {
                     in.transferTo(zip);
                  }

                  zip.closeEntry();
               }
            }
         }

         return any;
      }
   }
}
