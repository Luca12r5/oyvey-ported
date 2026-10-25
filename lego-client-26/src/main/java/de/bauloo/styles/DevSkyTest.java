package de.bauloo.styles;

import de.bauloo.styles.mixin.ButtonPressAccessor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.client.CameraType;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

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
               if (t[0] == 20
                  && c.getResourcePackRepository().getAvailableIds().contains("file/skytest")
                  && !c.getResourcePackRepository().getSelectedIds().contains("file/skytest")) {
                  c.getResourcePackRepository().addPack("file/skytest");
                  c.options.updateResourcePacks(c.getResourcePackRepository());
                  c.reloadResourcePacks();
                  BaulooStylesClient.LOGGER.info("[SkyTest] sky pack turned on");
               }

               if (c.level != null && c.player != null) {
                  if (t[1] < 0) {
                     t[1] = t[0];
                     if (!c.gui.hud.isHidden()) {
                        c.gui.hud.toggle();
                     }

                     c.gui.setScreen(null);
                     BaulooStylesClient.LOGGER.info("[SkyTest] in world, sky layers: {}", CustomSky.hasSky());
                  }

                  if (!c.gui.hud.isHidden()) {
                     c.gui.hud.toggle();
                  }

                  if (c.gui.screen() != null) {
                     c.gui.setScreen(null);
                  }

                  if (crystal && t[0] - t[1] == 20 && c.getSingleplayerServer() != null) {
                     Vec3 at = new Vec3(c.player.getX(), c.player.getY(), c.player.getZ()).add(c.player.getLookAngle().scale(3.5)).add(0.0, 0.5, 0.0);
                     c.getSingleplayerServer().execute(() -> {
                        ServerLevel w = c.getSingleplayerServer().overworld();
                        w.addFreshEntity(new EndCrystal(w, at.x, at.y, at.z));
                     });
                  }

                  if (pitch > 30.0F && t[0] - t[1] == 20 && c.getSingleplayerServer() != null) {
                     BlockPos feet = c.player.blockPosition();
                     c.getSingleplayerServer().execute(() -> {
                        ServerLevel w = c.getSingleplayerServer().overworld();

                        for (int dx = -6; dx <= 6; dx++) {
                           for (int dz = -6; dz <= 6; dz++) {
                              BlockPos p = feet.offset(dx, -1, dz);
                              w.setBlockAndUpdate(p, Blocks.GRASS_BLOCK.defaultBlockState());
                              w.setBlockAndUpdate(p.above(), (dx + dz) % 2 == 0 ? Blocks.SHORT_GRASS.defaultBlockState() : Blocks.AIR.defaultBlockState());
                           }
                        }
                     });
                  }

                  if (outline && t[0] - t[1] == 10 && c.getSingleplayerServer() != null) {
                     UUID id = c.player.getUUID();
                     BlockPos base = c.player.blockPosition().above(40);
                     c.getSingleplayerServer().execute(() -> {
                        ServerLevel w = c.getSingleplayerServer().overworld();

                        for (int dx = -5; dx <= 5; dx++) {
                           for (int dz = -5; dz <= 5; dz++) {
                              w.setBlockAndUpdate(base.offset(dx, 0, dz), Blocks.SMOOTH_STONE.defaultBlockState());
                           }
                        }

                        ServerPlayer p = c.getSingleplayerServer().getPlayerList().getPlayer(id);
                        if (p != null) {
                           p.teleportTo(base.getX() + 0.5, base.getY() + 1, base.getZ() + 0.5);
                        }
                     });
                  }

                  if (outline && t[0] - t[1] == 20) {
                     c.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                     StylesUiConfig.outline = true;
                     StylesUiConfig.outlineSelf = true;
                     StylesUiConfig.outlineRainbow = false;
                     StylesUiConfig.outlineColor = 16734920;
                     StylesUiConfig.outlineWidth = 2;
                  }

                  c.player.setXRot(pitch);
                  c.player.setYRot(-90.0F);
                  int s = t[0] - t[1] - 200;
                  if (s >= 0) {
                     if (!outline) {
                        int i = s / 60;
                        if (i >= TIMES.length) {
                           StylesUiConfig.timeOverride = -1;
                           BaulooStylesClient.LOGGER.info("[SkyTest] done");
                           c.stop();
                        } else {
                           if (s % 60 == 0) {
                              StylesUiConfig.timeOverride = TIMES[i];
                           } else if (s % 60 == 40) {
                              BaulooStylesClient.LOGGER
                                 .info("[SkyTest] screenshot {} at time {}, world time {}", new Object[]{i, TIMES[i], c.level.getDefaultClockTime() % 24000L});
                              Screenshot.grab(c, false);
                           }
                        }
                     } else {
                        if (s == 40 || s == 120) {
                           BaulooStylesClient.LOGGER.info("[SkyTest] outline screenshot, width {}", StylesUiConfig.outlineWidth);
                           Screenshot.grab(c, false);
                        } else if (s == 80) {
                           StylesUiConfig.outlineWidth = 5;
                           StylesUiConfig.outlineColor = 7722239;
                        } else if (s > 160) {
                           BaulooStylesClient.LOGGER.info("[SkyTest] done");
                           c.stop();
                        }
                     }
                  }
               } else {
                  if (t[0] > 4000) {
                     BaulooStylesClient.LOGGER.info("[SkyTest] no world");
                     c.stop();
                  } else if (c.gui.overlay() == null && c.gui.screen() instanceof TitleScreen && t[0] > 40) {
                     c.gui.setScreen(new SelectWorldScreen(c.gui.screen()));
                  } else if (c.gui.screen() != null && t[0] % 20 == 0) {
                     for (GuiEventListener e : c.gui.screen().children()) {
                        if (e instanceof Button b && b.active && b.getMessage().getString().equals("Create New World")) {
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
