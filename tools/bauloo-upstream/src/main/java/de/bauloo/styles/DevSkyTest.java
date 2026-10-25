package de.bauloo.styles;

import de.bauloo.styles.mixin.ButtonPressAccessor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.class_1511;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_318;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_442;
import net.minecraft.class_526;
import net.minecraft.class_5498;

final class DevSkyTest {
   private static final int[] TIMES = new int[]{1000, 6000, 12800, 18000};
   private static final int WAIT = 200;
   private static final int STEP = 60;
   private static float pitch = -30.0F;
   private static boolean crystal;
   private static boolean outline;

   private DevSkyTest() {
   }

   static void start(File file) {
      try {
         String mode = Files.readString(file.toPath());
         if (mode.contains("ground")) {
            pitch = 35.0F;
         }

         crystal = mode.contains("crystal");
         outline = mode.contains("outline");
         if (outline) {
            pitch = 15.0F;
         }

         if (crystal) {
            pitch = 10.0F;
         }
      } catch (IOException var2) {
      }

      int[] t = new int[]{0, -1};
      ClientTickEvents.END_CLIENT_TICK
         .register(
            (EndTick)c -> {
               t[0]++;
               if (t[0] == 20 && c.method_1520().method_29206().contains("file/skytest") && !c.method_1520().method_29210().contains("file/skytest")) {
                  c.method_1520().method_49427("file/skytest");
                  c.field_1690.method_49598(c.method_1520());
                  c.method_1521();
                  BaulooStylesClient.LOGGER.info("[SkyTest] sky pack turned on");
               }

               if (c.field_1687 != null && c.field_1724 != null) {
                  if (t[1] < 0) {
                     t[1] = t[0];
                     c.field_1690.field_1842 = true;
                     c.method_1507(null);
                     BaulooStylesClient.LOGGER.info("[SkyTest] in world, sky layers: {}", CustomSky.hasSky());
                  }

                  c.field_1690.field_1842 = true;
                  if (c.field_1755 != null) {
                     c.method_1507(null);
                  }

                  if (crystal && t[0] - t[1] == 20 && c.method_1576() != null) {
                     class_243 at = new class_243(c.field_1724.method_23317(), c.field_1724.method_23318(), c.field_1724.method_23321())
                        .method_1019(c.field_1724.method_5720().method_1021(3.5))
                        .method_1031(0.0, 0.5, 0.0);
                     c.method_1576().execute(() -> {
                        class_3218 w = c.method_1576().method_30002();
                        w.method_8649(new class_1511(w, at.field_1352, at.field_1351, at.field_1350));
                     });
                  }

                  if (pitch > 30.0F && t[0] - t[1] == 20 && c.method_1576() != null) {
                     class_2338 feet = c.field_1724.method_24515();
                     c.method_1576().execute(() -> {
                        class_3218 w = c.method_1576().method_30002();

                        for (int dx = -6; dx <= 6; dx++) {
                           for (int dz = -6; dz <= 6; dz++) {
                              class_2338 p = feet.method_10069(dx, -1, dz);
                              w.method_8501(p, class_2246.field_10219.method_9564());
                              w.method_8501(p.method_10084(), (dx + dz) % 2 == 0 ? class_2246.field_10479.method_9564() : class_2246.field_10124.method_9564());
                           }
                        }
                     });
                  }

                  if (outline && t[0] - t[1] == 10 && c.method_1576() != null) {
                     UUID id = c.field_1724.method_5667();
                     class_2338 base = c.field_1724.method_24515().method_10086(40);
                     c.method_1576().execute(() -> {
                        class_3218 w = c.method_1576().method_30002();

                        for (int dx = -5; dx <= 5; dx++) {
                           for (int dz = -5; dz <= 5; dz++) {
                              w.method_8501(base.method_10069(dx, 0, dz), class_2246.field_10360.method_9564());
                           }
                        }

                        class_3222 p = c.method_1576().method_3760().method_14602(id);
                        if (p != null) {
                           p.method_5859(base.method_10263() + 0.5, base.method_10264() + 1, base.method_10260() + 0.5);
                        }
                     });
                  }

                  if (outline && t[0] - t[1] == 20) {
                     c.field_1690.method_31043(class_5498.field_26665);
                     StylesUiConfig.outline = true;
                     StylesUiConfig.outlineSelf = true;
                     StylesUiConfig.outlineRainbow = false;
                     StylesUiConfig.outlineColor = 16734920;
                     StylesUiConfig.outlineWidth = 2;
                  }

                  c.field_1724.method_36457(pitch);
                  c.field_1724.method_36456(-90.0F);
                  int s = t[0] - t[1] - 200;
                  if (s >= 0) {
                     if (!outline) {
                        int i = s / 60;
                        if (i >= TIMES.length) {
                           StylesUiConfig.timeOverride = -1;
                           BaulooStylesClient.LOGGER.info("[SkyTest] done");
                           c.method_1592();
                        } else {
                           if (s % 60 == 0) {
                              StylesUiConfig.timeOverride = TIMES[i];
                           } else if (s % 60 == 40) {
                              BaulooStylesClient.LOGGER
                                 .info("[SkyTest] screenshot {} at time {}, world time {}", new Object[]{i, TIMES[i], c.field_1687.method_8532() % 24000L});
                              class_318.method_1659(c.field_1697, c.method_1522(), message -> {});
                           }
                        }
                     } else {
                        if (s == 40 || s == 120) {
                           BaulooStylesClient.LOGGER.info("[SkyTest] outline screenshot, width {}", StylesUiConfig.outlineWidth);
                           class_318.method_1659(c.field_1697, c.method_1522(), message -> {});
                        } else if (s == 80) {
                           StylesUiConfig.outlineWidth = 5;
                           StylesUiConfig.outlineColor = 7722239;
                        } else if (s > 160) {
                           BaulooStylesClient.LOGGER.info("[SkyTest] done");
                           c.method_1592();
                        }
                     }
                  }
               } else {
                  if (t[0] > 4000) {
                     BaulooStylesClient.LOGGER.info("[SkyTest] no world");
                     c.method_1592();
                  } else if (c.method_18506() == null && c.field_1755 instanceof class_442 && t[0] > 40) {
                     c.method_1507(new class_526(c.field_1755));
                  } else if (c.field_1755 != null && t[0] % 20 == 0) {
                     for (class_364 e : c.field_1755.method_25396()) {
                        if (e instanceof class_4185 b && b.field_22763 && b.method_25369().getString().equals("Create New World")) {
                           BaulooStylesClient.LOGGER.info("[SkyTest] pressing Create New World");
                           ((ButtonPressAccessor)b).baulooStyles$onPress().onPress(b);
                           break;
                        }
                     }
                  }
               }
            }
         );
   }
}
