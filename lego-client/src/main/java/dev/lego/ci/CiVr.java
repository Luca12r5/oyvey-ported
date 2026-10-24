package dev.lego.ci;

import dev.lego.LegoClient;
import dev.lego.core.Mc;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

/**
 * CI only (-Dlegoclient.ci.vr=1, set by apps/launcher/test/real-launch.ts --vr-null): switches
 * Vivecraft to real VR with its headset-less NullVR provider, so the whole VR render path runs
 * together with the LEGO Client on a runner without a headset. Logs when VR runs and when it
 * has stayed up in a world; the test fails without those lines.
 */
public final class CiVr {
   private static final boolean ON = System.getProperty("legoclient.ci.vr") != null;
   private static long titleAt;
   private static long worldAt;
   private static boolean requested;
   private static boolean runningLogged;
   private static boolean worldLogged;
   private static long lastReport;

   private CiVr() {
   }

   public static void tick(Minecraft mc) {
      if (!ON || !FabricLoader.getInstance().isModLoaded("vivecraft")) return;
      long now = System.currentTimeMillis();
      try {
         if (!requested) {
            if (titleAt == 0L) titleAt = now;
            if (now - titleAt < 5000L) return;
            requested = true;
            enableNullVr();
            return;
         }
         Class<?> state = Class.forName("org.vivecraft.client_vr.VRState");
         boolean running = state.getField("VR_RUNNING").getBoolean(null);
         boolean initialized = state.getField("VR_INITIALIZED").getBoolean(null);
         if (running && !runningLogged) {
            runningLogged = true;
            LegoClient.LOG("CI VR running (NullVR)");
         }
         if (now - lastReport > 10_000L) {
            lastReport = now;
            LegoClient.LOG("CI VR state: enabled=" + state.getField("VR_ENABLED").getBoolean(null) + " initialized=" + initialized + " running=" + running + " inGame=" + Mc.inGame());
         }
         if (running && Mc.inGame()) {
            if (worldAt == 0L) worldAt = now;
            if (!worldLogged && now - worldAt > 30_000L) {
               worldLogged = true;
               LegoClient.LOG("CI VR world ok");
            }
         } else {
            worldAt = 0L;
         }
      } catch (Throwable t) {
         if (now - lastReport > 10_000L) {
            lastReport = now;
            LegoClient.LOG("CI VR: " + t);
         }
      }
   }

   /** Same as the VR button with the NullVR provider chosen in Vivecraft's settings. */
   private static void enableNullVr() throws ReflectiveOperationException {
      Class<?> holder = Class.forName("org.vivecraft.client_vr.ClientDataHolderVR");
      Object data = holder.getMethod("getInstance").invoke(null);
      Field settingsField = holder.getField("vrSettings");
      Object settings = settingsField.get(data);
      Class<?> providerEnum = Class.forName("org.vivecraft.client_vr.settings.VRSettings$VRProvider");
      Object nullVr = providerEnum.getMethod("valueOf", String.class).invoke(null, "NULLVR");
      settings.getClass().getField("stereoProviderPluginID").set(settings, nullVr);
      settings.getClass().getField("vrEnabled").setBoolean(settings, true);
      Class<?> state = Class.forName("org.vivecraft.client_vr.VRState");
      state.getField("VR_ENABLED").setBoolean(null, true);
      LegoClient.LOG("CI VR: NullVR requested");
      // Vivecraft starts VR on its next frame when VR_ENABLED is set; call it directly if that does not happen.
      Mc.mc().execute(() -> {
         try {
            if (!state.getField("VR_INITIALIZED").getBoolean(null)) {
               Method init = state.getMethod("initializeVR");
               init.invoke(null);
            }
         } catch (Throwable t) {
            LegoClient.LOG("CI VR init: " + t);
         }
      });
   }
}
