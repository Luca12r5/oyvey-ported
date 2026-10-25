package de.bauloo.styles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStarted;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3264;
import net.minecraft.class_3283;
import net.minecraft.class_3300;

public final class GeneratedPacks {
   private GeneratedPacks() {
   }

   public static boolean anyActive() {
      return false;
   }

   public static void applyAll() {
      AnchorGlow.applyLive();
      AnchorPixelGlow.applyLive();
      CrystalCore.applyLive();
      EnchantGlint.applyLive();
      FireColor.applyLive();
      ShieldColor.applyLive();
      GrassColor.applyLive();
   }

   public static void register() {
      ClientLifecycleEvents.CLIENT_STARTED.register((ClientStarted)client -> dropOldPacks());
      ResourceManagerHelper.get(class_3264.field_14188).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
         public class_2960 getFabricId() {
            return class_2960.method_60655("bauloo_styles", "live_textures");
         }

         public Collection<class_2960> getFabricDependencies() {
            return List.of(ResourceReloadListenerKeys.TEXTURES);
         }

         public void method_14491(class_3300 manager) {
            class_310.method_1551().method_63588(() -> {
               CrystalCore.applyLive();
               GrassColor.applyLive();
            });
         }
      });
   }

   public static void dropOldPacks() {
      class_310 mc = class_310.method_1551();
      class_3283 packs = mc.method_1520();
      List<String> enabled = new ArrayList<>(packs.method_29210());
      int before = enabled.size();
      AnchorGlow.prepare(mc, enabled);
      CrystalCore.prepare(mc, enabled);
      EnchantGlint.prepare(mc, enabled);
      if (enabled.size() != before) {
         packs.method_14447(enabled);
         mc.field_1690.method_49598(packs);
      }
   }
}
