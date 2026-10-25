package de.bauloo.styles;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import java.io.File;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

public class BaulooStylesClient implements ClientModInitializer {
   public static final String MOD_ID = "bauloo_styles";
   public static final Logger LOGGER = LoggerFactory.getLogger("Bauloo Styles");
   public static final String[][] CRYSTAL = new String[][]{
      {"crystal_invisible", "Invisible"},
      {"crystal_seethrough", "See-Through"},
      {"crystal_aqua", "Aqua"},
      {"crystal_blueberry", "Blueberry"},
      {"crystal_purple", "Marlow"},
      {"crystal_lavender", "Lavender"},
      {"crystal_evantii", "Evantii"},
      {"crystal_doge", "Doge"},
      {"crystal_sixuii", "Sixuii"}
   };
   public static final String[][] ANCHOR = new String[][]{
      {"anchor_bauloo", "Bauloo Private"},
      {"anchor_manos", "Manos Purple Mashup"},
      {"anchor_xgomme", "xGomme Mashup"},
      {"anchor_k1rbekesu", "k1rbe & Kesu"},
      {"anchor_vanillaenh", "Vanilla Enhanced"},
      {"anchor_kesumain", "Kesu Main"},
      {"anchor_vaunta", "Vaunta"},
      {"anchor_lavander", "Lavander"},
      {"anchor_kesieoptimal", "Kesie Optimal"},
      {"anchor_faithful", "Faithful Overlay"},
      {"anchor_orchard", "Orchard"},
      {"anchor_purpedit", "Purp Edit"},
      {"anchor_kesielight", "Kesie BlueBerry Light"},
      {"anchor_clearvision", "Clear Vision"}
   };
   public static final String[][] ARMOR = new String[][]{
      {"armor_old1192", "1.19.2 Vanilla"},
      {"armor_kesu", "Kesu Private"},
      {"armor_crey", "Crey Private"},
      {"armor_marlow", "Marlow"},
      {"armor_greendemon", "GreenDemon"},
      {"armor_sakura", "Sakura Tweaks"},
      {"armor_vanillaenh", "Vanilla Enhanced"},
      {"armor_legend", "The6Legend"}
   };
   public static final String[][] SWORD = new String[][]{
      {"sword_doge", "Doge"},
      {"sword_kesieblueberry", "Kesie BlueBerry"},
      {"sword_marlow", "Marlow"},
      {"sword_lavander", "Lavander"},
      {"sword_bauloo", "Bauloo Private"},
      {"sword_killas", "Killa's Lightmode"},
      {"sword_ggrav", "GGrav Private"}
   };
   public static final String[][] GLOWSTONE = new String[][]{
      {"glowstone_kesumain", "Kesu Main"},
      {"glowstone_mashup", "Mashup"},
      {"glowstone_taurt", "Taurt"},
      {"glowstone_purpleprivate", "Purple Private"},
      {"glowstone_aquaplus", "AquaPlus"},
      {"glowstone_kesieblueberry", "Kesie BlueBerry"},
      {"glowstone_manos", "Manos Purple Mashup"}
   };
   public static final String[][] PICKAXE = new String[][]{
      {"pickaxe_doge", "Doge"},
      {"pickaxe_marlow", "Marlow"},
      {"pickaxe_dylan", "DYLAN Mashup"},
      {"pickaxe_clearvision", "Clear Vision"},
      {"pickaxe_daquavious", "Daquavious"},
      {"pickaxe_lavander", "Lavander"},
      {"pickaxe_bauloo", "Bauloo Private"}
   };
   public static final String[][] INVENTORY = new String[][]{
      {"inventory_amaranth", "Amaranth Clear"}, {"inventory_blossom", "Blossom"}, {"inventory_doge", "Doge Private"}
   };
   public static final String[][] FIRE = new String[][]{
      {"fire_kesu", "Kesu Fire"},
      {"fire_ultralow", "Ultra Low Fire"},
      {"fire_cowarted", "Cowarted Low Fire"},
      {"fire_16x", "Fire 16x"},
      {"fire_32x", "Fire 32x"},
      {"fire_none", "No Fire"}
   };
   public static final String[][] SKY = new String[0][];
   public static final String[][] DEEPSLATE = new String[][]{
      {"deepslate_lavender", "Lavender"},
      {"deepslate_blue", "Blue"},
      {"deepslate_ice", "Ice"},
      {"deepslate_red", "Red"},
      {"deepslate_pink", "Pink"},
      {"deepslate_purple", "Purple"},
      {"deepslate_black", "Black"},
      {"deepslate_white", "White"}
   };
   public static final String[][] STONE = new String[][]{
      {"stone_white", "White"},
      {"stone_lavender", "Lavender"},
      {"stone_blue", "Blue"},
      {"stone_ice", "Ice"},
      {"stone_red", "Red"},
      {"stone_pink", "Pink"},
      {"stone_purple", "Purple"},
      {"stone_black", "Black"}
   };
   public static final String[][] SNOW = new String[][]{
      {"snow_grey", "Grey"},
      {"snow_white", "White"},
      {"snow_black", "Black"},
      {"snow_blue", "Blue"},
      {"snow_ice", "Ice"},
      {"snow_red", "Red"},
      {"snow_pink", "Pink"},
      {"snow_purple", "Purple"},
      {"snow_lavender", "Lavender"}
   };
   public static final String[][] GRASS = new String[][]{
      {"grass_lush", "Lush Green"},
      {"grass_dark", "Dark Green"},
      {"grass_lime", "Lime"},
      {"grass_white", "White"},
      {"grass_black", "Black"},
      {"grass_blue", "Blue"},
      {"grass_ice", "Ice"},
      {"grass_red", "Red"},
      {"grass_pink", "Pink"},
      {"grass_purple", "Purple"},
      {"grass_lavender", "Lavender"}
   };
   public static final String[][] GAP = new String[0][];
   public static final String[][] PEARL = new String[0][];
   public static final String[][] HOTBAR = new String[0][];
   public static final String[][] AXE = new String[0][];
   public static final String[][] NONE = new String[0][];
   public static final String[][] TOTEM = new String[0][];
   public static final String[][] OBSIDIAN = new String[0][];
   private boolean hotkeyDown;

   public static String[][] stylesOf(BaulooStylesClient.Tab tab) {
      return switch (tab) {
         case CRYSTAL -> CRYSTAL;
         case OBSIDIAN -> OBSIDIAN;
         case ANCHOR -> ANCHOR;
         case GLOWSTONE -> GLOWSTONE;
         case ARMOR -> ARMOR;
         case SWORD -> SWORD;
         case PICKAXE -> PICKAXE;
         case AXE -> AXE;
         case GAP -> GAP;
         case PEARL -> PEARL;
         case TOTEM -> TOTEM;
         case HOTBAR -> HOTBAR;
         case INVENTORY -> INVENTORY;
         case FIRE -> FIRE;
         case SKY -> SKY;
         case DEEPSLATE -> DEEPSLATE;
         case STONE -> STONE;
         case SNOW -> SNOW;
         case GRASS -> GRASS;
         case EXTRA1, EXTRA2, EXTRA3, EXTRA4, EXTRA5, EXTRA6, EXTRA7, EXTRA8 -> NONE;
      };
   }

   @Override
   public void onInitializeClient() {
      if (Boolean.getBoolean("bauloo.audit")) {
         ClientLifecycleEvents.CLIENT_STARTED.register(mc -> {
            try {
               MixinEnvironment.getCurrentEnvironment().audit();
               LOGGER.info("[Audit] all mixins applied");
            } catch (Throwable var3) {
               LOGGER.error("[Audit] mixin audit failed", var3);
            }

            File skyTest = new File(String.valueOf(mc.gameDirectory), "bauloo-skytest");
            if (skyTest.exists()) {
               DevSkyTest.start(skyTest);
            } else {
               int[] ticks = new int[]{0, -1};
               ClientTickEvents.END_CLIENT_TICK.register(c -> {
                  ticks[0]++;
                  if (ticks[1] < 0 && c.getOverlay() == null && c.screen instanceof TitleScreen) {
                     ticks[1] = ticks[0];
                     c.setScreen(new BaulooStylesConfigScreen(null));
                  } else if (ticks[1] >= 0 && ticks[0] == ticks[1] + 80) {
                     LOGGER.info("[Audit] menu open: {}", c.screen == null ? "none" : c.screen.getClass().getSimpleName());
                  } else if (ticks[1] >= 0 && ticks[0] > ticks[1] + 80) {
                     int step = ticks[0] - ticks[1] - 80;
                     int stop = step / 40;
                     if (!(stop < BaulooStylesConfigScreen.auditStops() && c.screen instanceof BaulooStylesConfigScreen menu)) {
                        LOGGER.info("[Audit] tour done");
                        c.stop();
                     } else if (step % 40 == 1) {
                        menu.auditShow(stop);
                     } else if (step % 40 == 30) {
                        LOGGER.info("[Audit] screenshot {}", stop);
                        Screenshot.grab(c.gameDirectory, c.getMainRenderTarget(), message -> {});
                     }
                  } else if (ticks[0] == 1200) {
                     LOGGER.info("[Audit] menu never opened");
                     c.stop();
                  }
               });
            }
         });
      }

      StylesUiConfig.load();
      CrystalHitbox.register();
      CobwebOutline.register();
      CustomSky.register();
      EnchantGlint.register();
      GeneratedPacks.register();
      SpotifyHud.register();
      StylesPresence.register();
      KillEffect.register();
      LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
         if (renderer instanceof AvatarRenderer<?> player) {
            helper.register(new CosmeticsFeature(player));
         }
      });
      BlockRenderLayerMap.putBlock(Blocks.RESPAWN_ANCHOR, ChunkSectionLayer.CUTOUT);
      this.registerHotkey();
      StylesCloud.refresh(true);
      StylesBlock.start();
      ClientLifecycleEvents.CLIENT_STARTED.register(client -> BlurCompat.apply());
      FabricLoader.getInstance()
         .getModContainer("bauloo_styles")
         .ifPresent(
            container -> {
               for (BaulooStylesClient.Tab tab : BaulooStylesClient.Tab.values()) {
                  for (String[] style : stylesOf(tab)) {
                     ResourceManagerHelper.registerBuiltinResourcePack(
                        Identifier.fromNamespaceAndPath("bauloo_styles", style[0]), container, Component.literal(style[1]), ResourcePackActivationType.NORMAL
                     );
                  }
               }
            }
         );
   }

   private void registerHotkey() {
      ClientTickEvents.END_CLIENT_TICK.register(client -> {
         StylesStats.tick();
         long reopen = BaulooStylesConfigScreen.reopenAtMs;
         if (reopen > 0L && System.currentTimeMillis() >= reopen) {
            BaulooStylesConfigScreen.reopenAtMs = 0L;
            if (client.screen == null && client.player != null) {
               client.setScreen(new BaulooStylesConfigScreen(null));
            }
         }

         if (!StylesHotkey.isSet()) {
            this.hotkeyDown = false;
         } else {
            Key key = StylesHotkey.get();
            boolean down;
            if (key.getType() == Type.MOUSE) {
               down = GLFW.glfwGetMouseButton(client.getWindow().handle(), key.getValue()) == 1;
            } else {
               down = InputConstants.isKeyDown(client.getWindow(), key.getValue());
            }

            if (down && !this.hotkeyDown && client.screen == null && client.player != null) {
               client.setScreen(new BaulooStylesConfigScreen(null));
            }

            this.hotkeyDown = down;
         }
      });
   }

   public static enum Tab {
      CRYSTAL("crystal", "Crystal"),
      OBSIDIAN("obsidian", "Obsidian"),
      ANCHOR("anchor", "Anchor"),
      GLOWSTONE("glowstone", "Glowstone"),
      ARMOR("armor", "Armor"),
      SWORD("sword", "Tools"),
      PICKAXE("pickaxe", "Pickaxe"),
      AXE("axe", "Axe"),
      GAP("gap", "Gaps & Pearls"),
      PEARL("pearl", "Pearl"),
      TOTEM("totem", "Totem"),
      HOTBAR("hotbar", "HUD"),
      INVENTORY("inventory", "Inventory"),
      FIRE("fire", "Fire"),
      SKY("sky", "Sky"),
      DEEPSLATE("deepslate", "Deepslate"),
      STONE("stone", "Stone"),
      SNOW("snow", "Snow"),
      GRASS("grass", "Grass"),
      EXTRA1("extra1", "Extra 1"),
      EXTRA2("extra2", "Extra 2"),
      EXTRA3("extra3", "Extra 3"),
      EXTRA4("extra4", "Extra 4"),
      EXTRA5("extra5", "Extra 5"),
      EXTRA6("extra6", "Extra 6"),
      EXTRA7("extra7", "Extra 7"),
      EXTRA8("extra8", "Extra 8");

      public String prefix;
      public String title;
      public String icon = "minecraft:paper";
      public boolean bound;
      public static int bindVersion;

      private Tab(String prefix, String title) {
         this.prefix = prefix;
         this.title = title;
      }

      public boolean isCloudSlot() {
         return this.name().startsWith("EXTRA");
      }

      public static void bindCloudSlots() {
         bindVersion++;

         for (BaulooStylesClient.Tab t : values()) {
            if (t.isCloudSlot()) {
               int slot = t.ordinal() - EXTRA1.ordinal() + 1;
               CloudCategories.Def d = null;

               for (CloudCategories.Def c : CloudCategories.visible()) {
                  if (c.slot() == slot) {
                     d = c;
                  }
               }

               t.bound = d != null;
               t.prefix = d != null ? d.id() : "extra" + slot;
               t.title = d != null ? d.name() : "Extra " + slot;
               t.icon = d != null ? d.icon() : "minecraft:paper";
            }
         }
      }
   }
}
