package de.bauloo.styles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStarted;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ResourceManager;

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
      ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
         public Identifier getFabricId() {
            return Identifier.fromNamespaceAndPath("bauloo_styles", "live_textures");
         }

         public Collection<Identifier> getFabricDependencies() {
            return List.of(ResourceReloadListenerKeys.TEXTURES);
         }

         public void onResourceManagerReload(ResourceManager manager) {
            Minecraft.getInstance().schedule(() -> {
               CrystalCore.applyLive();
               GrassColor.applyLive();
            });
         }
      });
   }

   public static void dropOldPacks() {
      Minecraft mc = Minecraft.getInstance();
      PackRepository packs = mc.getResourcePackRepository();
      List<String> enabled = new ArrayList<>(packs.getSelectedIds());
      int before = enabled.size();
      AnchorGlow.prepare(mc, enabled);
      CrystalCore.prepare(mc, enabled);
      EnchantGlint.prepare(mc, enabled);
      if (enabled.size() != before) {
         packs.setSelected(enabled);
         mc.options.updateResourcePacks(packs);
      }
   }
}
