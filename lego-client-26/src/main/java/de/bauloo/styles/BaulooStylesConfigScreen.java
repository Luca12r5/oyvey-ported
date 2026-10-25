package de.bauloo.styles;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.InputConstants.Type;
import de.bauloo.styles.mixin.SpriteContentsAccessor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class BaulooStylesConfigScreen extends Screen {
   private static final String PREFIX = "bauloo_styles:";
   private static final String UPLOAD = "bauloo_upload_";
   private static final String CLOUD = "bauloo_cloud_";
   private static final int MARGIN = 12;
   private static final int HEADER_H = 38;
   private static final int FOOTER_H = 28;
   private static final int GAP = 8;
   private static final int LABEL_H = 17;
   private final Screen parent;
   private final boolean skyboxifyInstalled = true;
   private final List<String[]> entries = new ArrayList<>();
   private final List<Integer> visible = new ArrayList<>();
   private final Map<BaulooStylesClient.Tab, Integer> uploadCounts = new EnumMap<>(BaulooStylesClient.Tab.class);
   private final List<BaulooStylesConfigScreen.Hit> hits = new ArrayList<>();
   private BaulooStylesClient.Tab tab = BaulooStylesClient.Tab.CRYSTAL;
   private int focusedIndex;
   private int hoveredEntry = -1;
   private String query = "";
   private boolean searchFocused;
   private boolean favoritesOnly;
   private boolean hexFocused;
   private boolean socialFocused;
   private String hexDraft = "";
   private boolean bindingKey;
   private boolean compactThemes;
   private static boolean extrasView;
   private static boolean myPacksView;
   private int myPacksVersion = -1;
   private static final String MYPACK = "mypack:";
   private static final String VIPPACK = "vippack:";
   private static boolean vipView;
   private int vipVersion = -1;
   private static boolean cosmeticsView;
   private static Cosmetics.Slot cosSlot = Cosmetics.Slot.CAPE;
   private float cosYaw = 180.0F;
   private Cosmetics.Slot cosYawSlot;
   private float cosDragYaw;
   private long cosLastFrame;
   private static boolean publicView;
   static volatile long reopenAtMs;
   private static boolean presetsView;
   private boolean presetNaming;
   private boolean keyEntering;
   private String keyDraft = "";
   private String shareConfirmKey;
   private long shareConfirmUntil;
   private StylesPresets.Preset pendingPreset;
   private String updateAsk;
   private WhatsNew.Notes whatsNew;
   private boolean whatsNewChecked;
   private boolean resetAsk;
   private static final String RESET_NAME = "Reset";
   private boolean pendingRemember;
   private String extrasStatus = "";
   private String presetDraft = "";
   private String zoomFolder;
   private String zoomName;
   private String tooltip;
   private int tooltipX;
   private int tooltipY;
   private double gridScroll;
   private int gridMaxScroll;
   private int gridTop;
   private int gridBottom;
   private int gridLeft;
   private int gridRight;
   private double themeScroll;
   private int themeMaxScroll;
   private int themeTop;
   private int themeBottom;
   private int themeLeft;
   private int themeRight;
   private BaulooStylesConfigScreen.Drag drag = BaulooStylesConfigScreen.Drag.NONE;
   private int dragX;
   private int dragW;
   private int dragY;
   private double dragStartMouse;
   private float dragStartT;
   private int hotkeyX;
   private int hotkeyY;
   private int hotkeyW;
   private StylesTheme c;
   private Map<BaulooStylesClient.Tab, String> active = new EnumMap<>(BaulooStylesClient.Tab.class);
   private static final Map<String, Optional<BaulooStylesConfigScreen.Clip>> CLIPS = new HashMap<>();
   private static final long CLIP_DELAY_MS = 450L;
   private int hoverTrack = -1;
   private long hoverSince;
   private int cloudVersion = -1;
   private int bindVersion = -1;
   private static String earlyBound;
   private int uploadVersion = -1;
   private String pendingCloud;
   private static final Map<BaulooStylesClient.Tab, String> PENDING = new EnumMap<>(BaulooStylesClient.Tab.class);
   private static final String VANILLA_PICK = "\u0000vanilla";
   private static final int PENDING_COLOR = -15811;
   private static final Map<String, Identifier> BUILT_IN = new HashMap<>();
   private int hitClipTop = Integer.MIN_VALUE;
   private int hitClipBottom = Integer.MAX_VALUE;
   private double extrasScroll;
   private int extrasMaxScroll;
   private int extrasTop;
   private int extrasBottom;
   private int extrasLeft;
   private int extrasRight;
   private static final int[] SMALL = new int[]{2, 1};
   private static final int[] CARD = new int[]{3, 1, 1};
   private static final int[] PANEL = new int[]{5, 3, 2, 1, 1};
   private static final double UI_SCALE = 2.0;
   private int realWidth;
   private int realHeight;
   private boolean scaled;
   private static final Identifier TORNADO = Identifier.fromNamespaceAndPath("bauloo_styles", "textures/gui/tornado.png");
   private boolean pendingMyPack;
   private int scrollDrag;
   private int scrollTop;
   private int scrollH;
   private int scrollThumbH;
   private double scrollGrab;
   private static final int GROUP_H = 14;
   private static final int[][] SKY_STOPS = new int[][]{
      {0, 16755310},
      {1500, 8566015},
      {6000, 7252735},
      {11000, 9218800},
      {12500, 16742480},
      {13500, 3942490},
      {18000, 790050},
      {22500, 2631760},
      {23500, 16750180},
      {24000, 16755310}
   };
   private static int[] trackColors = new int[0];
   private static int trackKey;
   private static Identifier skyPreviewId;
   private static int skyPreviewTicks = Integer.MIN_VALUE;
   private static int skyPreviewKey;
   private static final String[] EXTRA_GROUPS = new String[]{"All", "Combat", "World", "HUD & Menu"};
   private static String extraKey = "OUTLINE";
   private static int extraGroup;
   private static boolean extraOpen;
   private double tileScroll;
   private int tileMaxScroll;
   private int tileTop;
   private int tileBottom;
   private int tileLeft;
   private int tileRight;
   private String extraOnly;
   private static final int[] OUTLINE_SWATCHES = new int[]{16734920, 16730714, 16758087, 7143306, 7722239, 11766015, 16777215, 0};
   private static final int[] PICK_SWATCHES = new int[]{11766015, 16734920, 16730714, 16758087, 7143306, 7722239, 5213439, 16777215};
   private static final int PICK_HITBOX = 0;
   private static final int PICK_CROSSHAIR = 1;
   private static final int PICK_CRYSTAL_BOX = 2;
   private static final int PICK_ANCHOR = 3;
   private static final int PICK_CORE = 4;
   private static final int PICK_GLINT = 6;
   private static final int PICK_FIRE = 7;
   private static final int PICK_SHIELD = 8;
   private static final int PICK_BLOCK = 9;
   private static final int[] BLOCK_SWATCHES = new int[]{0, 11766015, 16734920, 16730714, 16758087, 7143306, 7722239, 16777215};
   private static boolean huePreview;
   private int huePick;
   private static Identifier shieldPreviewId;
   private static String shieldPreviewKey = "";
   private final EndCrystalRenderState crystalState = new EndCrystalRenderState();
   private static boolean presetsPublic;
   private static String lastUsedLook;
   private static boolean publicAsked;
   private double publicScroll;
   private int publicMaxScroll;
   private int publicTop;
   private int publicBottom;
   private int publicLeft;
   private int publicRight;
   private CameraType outlineView;

   public BaulooStylesConfigScreen(Screen parent) {
      super(Component.literal("Bauloo Styles"));
      this.parent = parent;
      StylesUiConfig.load();
      StylesCloud.refresh(false);
      this.rebuild();
   }

   protected void init() {
      super.init();
      if (StylesBlock.isBlocked() && this.minecraft != null) {
         StylesBlock.enforce();
         this.minecraft
            .schedule(
               () -> this.minecraft
                  .gui
                  .setScreen(
                     new AlertScreen(
                        () -> this.minecraft.gui.setScreen(this.parent),
                        Component.literal("Bauloo Styles"),
                        Component.literal("Bauloo Styles is not available for this account.")
                     )
                  )
            );
      }
   }

   private void rebuild() {
      this.cloudVersion = StylesCloud.version();
      this.bindVersion = BaulooStylesClient.Tab.bindVersion;
      this.uploadVersion = StylesUploadManager.version();
      this.entries.clear();
      if (MyPacks.shown()) {
         MyPacks.ensureScanned();
      }

      if (myPacksView && vipView) {
         this.rebuildVip();
      } else if (myPacksView) {
         this.rebuildMyPacks();
      } else {
         for (BaulooStylesClient.Tab group : groupOf(this.tab)) {
            String g = group.name();
            this.entries.add(new String[]{null, "Vanilla", null, g});

            for (StylesCloud.Style style : StylesCloud.of(group.prefix)) {
               this.entries.add(new String[]{style.folder(), style.name(), "cloud", g});
            }

            for (String[] style : StylesUploadManager.sync(group.prefix)) {
               this.entries.add(new String[]{style[0], style[1], "upload", g});
            }

            for (String[] style : BaulooStylesClient.stylesOf(group)) {
               this.entries.add(new String[]{style[0], style[1], null, g});
            }
         }

         for (BaulooStylesClient.Tab value : BaulooStylesClient.Tab.values()) {
            this.uploadCounts.put(value, StylesUploadManager.list(value.title).size());
         }

         this.active = activeFolders();
         this.focusedIndex = 0;

         for (int i = 0; i < this.entries.size(); i++) {
            if (this.isActive(i)) {
               this.focusedIndex = i;
               break;
            }
         }

         this.gridScroll = 0.0;
         this.applyFilter();
      }
   }

   private void rebuildMyPacks() {
      MyPacks.ensureScanned();
      this.myPacksVersion = MyPacks.version();

      for (MyPacks.Style style : MyPacks.styles()) {
         BaulooStylesClient.Tab group = this.tabOf("mypack:" + style.id());
         this.entries.add(new String[]{"mypack:" + style.id(), style.name(), "mypack", group.name()});
      }

      this.active = activeFolders();
      this.focusedIndex = Math.min(this.focusedIndex, Math.max(0, this.entries.size() - 1));
      this.applyFilter();
   }

   private void rebuildVip() {
      VipPacks.refresh(false);
      this.vipVersion = VipPacks.version();
      List<VipPacks.Style> sorted = new ArrayList<>(VipPacks.styles());
      sorted.sort(
         Comparator.<VipPacks.Style>comparingInt(st -> this.tabOf("vippack:" + st.id()).ordinal()).thenComparing(st -> st.name().toLowerCase(Locale.ROOT))
      );

      for (VipPacks.Style style : sorted) {
         BaulooStylesClient.Tab group = this.tabOf("vippack:" + style.id());
         this.entries.add(new String[]{"vippack:" + style.id(), style.name(), "vippack", group.name()});
      }

      this.active = activeFolders();
      this.focusedIndex = Math.min(this.focusedIndex, Math.max(0, this.entries.size() - 1));
      this.applyFilter();
   }

   private void useVipPack(String folder, BaulooStylesClient.Tab target) {
      VipPacks.Style style = VipPacks.byId(folder.substring("vippack:".length()));
      if (style != null && !this.pendingMyPack) {
         this.pendingMyPack = true;
         Thread thread = new Thread(() -> {
            String id = VipPacks.use(style);
            Minecraft.getInstance().execute(() -> {
               this.pendingMyPack = false;
               if (id != null && Minecraft.getInstance().gui.screen() == this) {
                  this.applyAndRefresh(id, target);
               }
            });
         }, "Bauloo Styles VIP pack");
         thread.setDaemon(true);
         thread.start();
      }
   }

   private void applyFilter() {
      this.visible.clear();
      String q = this.query.trim().toLowerCase(Locale.ROOT);

      for (int i = 0; i < this.entries.size(); i++) {
         String[] e = this.entries.get(i);
         if ((!this.favoritesOnly || e[0] != null && StylesUiConfig.isFavorite(e[0])) && (q.isEmpty() || e[1].toLowerCase(Locale.ROOT).contains(q))) {
            this.visible.add(i);
         }
      }
   }

   private int favoriteCount() {
      int n = 0;

      for (String[] e : this.entries) {
         if (e[0] != null && StylesUiConfig.isFavorite(e[0])) {
            n++;
         }
      }

      return n;
   }

   public static int auditStops() {
      return shownTabs().length + 2;
   }

   public void auditShow(int stop) {
      this.whatsNew = null;
      BaulooStylesClient.Tab[] tabs = shownTabs();
      if (stop < tabs.length) {
         this.switchTab(tabs[stop]);
         this.focusedIndex = Math.min(1, this.entries.size() - 1);
      } else if (stop == tabs.length) {
         extrasView = true;
      } else {
         extrasView = false;
         myPacksView = true;
         this.rebuild();
      }
   }

   private void switchTab(BaulooStylesClient.Tab value) {
      if (myPacksView) {
         myPacksView = false;
         this.tab = value;
         this.query = "";
         this.searchFocused = false;
         extrasView = false;
         publicView = false;
         cosmeticsView = false;
         this.rebuild();
      } else {
         extrasView = false;
         publicView = false;
         cosmeticsView = false;
         if (value != this.tab) {
            this.tab = value;
            this.query = "";
            this.searchFocused = false;
            this.rebuild();
         }
      }
   }

   private static String packId(String folder) {
      if (folder.startsWith("bauloo_upload_")) {
         return folder.endsWith("_folder") ? "file/" + folder : "file/" + folder + ".zip";
      } else {
         return folder.startsWith("bauloo_cloud_") ? "file/" + folder + ".zip" : "bauloo_styles:" + folder;
      }
   }

   private static boolean isPackOf(BaulooStylesClient.Tab value, String id) {
      return id.startsWith("bauloo_styles:" + value.prefix + "_")
         || id.startsWith("file/bauloo_upload_" + value.prefix + "_")
         || id.startsWith("file/bauloo_cloud_" + value.prefix + "_");
   }

   private static Map<BaulooStylesClient.Tab, String> activeFolders() {
      Map<BaulooStylesClient.Tab, String> map = new EnumMap<>(BaulooStylesClient.Tab.class);

      for (String id : Minecraft.getInstance().getResourcePackRepository().getSelectedIds()) {
         for (BaulooStylesClient.Tab value : BaulooStylesClient.Tab.values()) {
            if (id.startsWith("bauloo_styles:" + value.prefix + "_")) {
               map.put(value, id.substring("bauloo_styles:".length()));
            } else if (id.startsWith("file/bauloo_upload_" + value.prefix + "_") || id.startsWith("file/bauloo_cloud_" + value.prefix + "_")) {
               String folder = id.substring("file/".length());
               if (folder.endsWith(".zip")) {
                  folder = folder.substring(0, folder.length() - 4);
               }

               map.put(value, folder);
            }
         }
      }

      return map;
   }

   private BaulooStylesClient.Tab tabOf(int index) {
      String[] e = this.entries.get(index);
      return e.length > 3 && e[3] != null ? BaulooStylesClient.Tab.valueOf(e[3]) : this.tab;
   }

   private static BaulooStylesClient.Tab[] shownTabs() {
      return Arrays.stream(BaulooStylesClient.Tab.values())
         .filter(
            t -> t != BaulooStylesClient.Tab.PICKAXE
               && t != BaulooStylesClient.Tab.AXE
               && t != BaulooStylesClient.Tab.GLOWSTONE
               && t != BaulooStylesClient.Tab.PEARL
               && (!t.isCloudSlot() || t.bound)
         )
         .toArray(BaulooStylesClient.Tab[]::new);
   }

   private static BaulooStylesClient.Tab[] groupOf(BaulooStylesClient.Tab value) {
      return switch (value) {
         case SWORD -> new BaulooStylesClient.Tab[]{BaulooStylesClient.Tab.SWORD, BaulooStylesClient.Tab.PICKAXE, BaulooStylesClient.Tab.AXE};
         case GAP -> new BaulooStylesClient.Tab[]{BaulooStylesClient.Tab.GAP, BaulooStylesClient.Tab.PEARL};
         case ANCHOR -> new BaulooStylesClient.Tab[]{BaulooStylesClient.Tab.ANCHOR, BaulooStylesClient.Tab.GLOWSTONE};
         default -> new BaulooStylesClient.Tab[]{value};
      };
   }

   private boolean isActive(int index) {
      String folder = this.entries.get(index)[0];
      String current = this.active.get(this.tabOf(index));
      return folder == null ? current == null : folder.equals(current);
   }

   private String activeName(BaulooStylesClient.Tab value) {
      if (value == BaulooStylesClient.Tab.SWORD) {
         String tools = this.oneName(BaulooStylesClient.Tab.SWORD).replace(" Sword", "")
            + " / "
            + this.oneName(BaulooStylesClient.Tab.PICKAXE).replace(" Pickaxe", "");
         return this.active.get(BaulooStylesClient.Tab.AXE) == null ? tools : tools + " / " + this.oneName(BaulooStylesClient.Tab.AXE).replace(" Axe", "");
      } else if (value == BaulooStylesClient.Tab.GAP) {
         return this.oneName(BaulooStylesClient.Tab.GAP) + " / " + this.oneName(BaulooStylesClient.Tab.PEARL);
      } else {
         return value == BaulooStylesClient.Tab.ANCHOR && this.active.get(BaulooStylesClient.Tab.GLOWSTONE) != null
            ? this.oneName(BaulooStylesClient.Tab.ANCHOR) + " / " + this.oneName(BaulooStylesClient.Tab.GLOWSTONE)
            : this.oneName(value);
      }
   }

   private String oneName(BaulooStylesClient.Tab value) {
      String folder = this.active.get(value);
      if (folder == null) {
         return "Vanilla";
      } else {
         for (String[] style : BaulooStylesClient.stylesOf(value)) {
            if (style[0].equals(folder)) {
               return style[1];
            }
         }

         StylesCloud.Style cloud = StylesCloud.byFolder(folder);
         if (cloud != null) {
            return cloud.name();
         } else {
            String name = folder.startsWith("bauloo_upload_" + value.prefix + "_")
               ? folder.substring(("bauloo_upload_" + value.prefix + "_").length())
               : folder;
            return name.replace("_skyonly", "").replace("_folder", "");
         }
      }
   }

   private int styleCount(BaulooStylesClient.Tab value) {
      int count = BaulooStylesClient.stylesOf(value).length + this.uploadCounts.getOrDefault(value, 0) + StylesCloud.of(value.prefix).size();
      return value == BaulooStylesClient.Tab.SWORD
         ? count + this.styleCount(BaulooStylesClient.Tab.PICKAXE) + this.styleCount(BaulooStylesClient.Tab.AXE)
         : (
            value == BaulooStylesClient.Tab.GAP
               ? count + this.styleCount(BaulooStylesClient.Tab.PEARL)
               : (value == BaulooStylesClient.Tab.ANCHOR ? count + this.styleCount(BaulooStylesClient.Tab.GLOWSTONE) : count)
         );
   }

   private static ItemStack stackOf(Item item) {
      return item.builtInRegistryHolder().areComponentsBound() ? new ItemStack(item) : ItemStack.EMPTY;
   }

   private static ItemStack icon(BaulooStylesClient.Tab value) {
      return stackOf(iconItem(value));
   }

   private static void itemIcon(GuiGraphicsExtractor ctx, Item item, int x, int y) {
      if (item.builtInRegistryHolder().areComponentsBound()) {
         ctx.item(new ItemStack(item), x, y);
      } else if (item == Items.CHEST) {
         Identifier chest = Identifier.withDefaultNamespace("textures/entity/chest/normal.png");
         ctx.blit(RenderPipelines.GUI_TEXTURED, chest, x + 1, y + 1, 14.0F, 14.0F, 14, 5, 14, 5, 64, 64, -1);
         ctx.blit(RenderPipelines.GUI_TEXTURED, chest, x + 1, y + 6, 14.0F, 33.0F, 14, 10, 14, 10, 64, 64, -1);
         ctx.blit(RenderPipelines.GUI_TEXTURED, chest, x + 7, y + 4, 1.0F, 1.0F, 2, 4, 2, 4, 64, 64, -1);
      } else {
         String name = BuiltInRegistries.ITEM.getKey(item).getPath();

         String texture = switch (name) {
            case "respawn_anchor" -> "block/respawn_anchor_side4";
            case "glowstone", "deepslate", "stone", "obsidian" -> "block/" + name;
            case "snow_block" -> "block/snow";
            default -> "item/" + name;
         };
         ctx.blit(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("textures/" + texture + ".png"), x, y, 0.0F, 0.0F, 16, 16, 16, 16, 16, 16, -1);
      }
   }

   private static Item iconItem(BaulooStylesClient.Tab value) {
      if (value.isCloudSlot()) {
         Identifier id = Identifier.tryParse(value.icon);
         Item item = id == null ? Items.PAPER : (Item)BuiltInRegistries.ITEM.getValue(id);
         return item == Items.AIR ? Items.PAPER : item;
      } else {
         return switch (value) {
            case SWORD -> Items.NETHERITE_SWORD;
            case GAP -> Items.GOLDEN_APPLE;
            case ANCHOR -> Items.RESPAWN_ANCHOR;
            case CRYSTAL -> Items.END_CRYSTAL;
            case GLOWSTONE -> Items.GLOWSTONE;
            case ARMOR -> Items.NETHERITE_CHESTPLATE;
            case PICKAXE -> Items.NETHERITE_PICKAXE;
            case AXE -> Items.NETHERITE_AXE;
            case TOTEM -> Items.TOTEM_OF_UNDYING;
            case PEARL -> Items.ENDER_PEARL;
            case HOTBAR -> Items.ITEM_FRAME;
            case INVENTORY -> Items.CHEST;
            case FIRE -> Items.FIRE_CHARGE;
            case SKY -> Items.ELYTRA;
            case DEEPSLATE -> Items.DEEPSLATE;
            case STONE -> Items.STONE;
            case SNOW -> Items.SNOW_BLOCK;
            case GRASS -> Items.GRASS_BLOCK;
            case EXTRA1, EXTRA2, EXTRA3, EXTRA4, EXTRA5, EXTRA6, EXTRA7, EXTRA8 -> Items.PAPER;
            case OBSIDIAN -> Items.OBSIDIAN;
         };
      }
   }

   private void choose(String folder, BaulooStylesClient.Tab target) {
      if (StylesUiConfig.applyInstantly && folder != null && folder.startsWith("mypack:")) {
         this.useMyPack(folder, target);
      } else if (StylesUiConfig.applyInstantly && folder != null && folder.startsWith("vippack:")) {
         this.useVipPack(folder, target);
      } else if (StylesUiConfig.applyInstantly) {
         this.applyAndRefresh(folder, target);
      } else {
         if (target == BaulooStylesClient.Tab.SKY && folder != null) {
         }

         if (Objects.equals(folder, this.active.get(target))) {
            PENDING.remove(target);
         } else {
            PENDING.put(target, folder == null ? "\u0000vanilla" : folder);
         }
      }
   }

   private boolean isPending(int index) {
      String picked = PENDING.get(this.tabOf(index));
      String folder = this.entries.get(index)[0];
      return picked != null && picked.equals(folder == null ? "\u0000vanilla" : folder);
   }

   private static boolean pendingIn(BaulooStylesClient.Tab value) {
      for (BaulooStylesClient.Tab t : groupOf(value)) {
         if (PENDING.containsKey(t)) {
            return true;
         }
      }

      return false;
   }

   private void applyPending(boolean reopen) {
      this.applyPending(reopen, true);
   }

   private void applyPending(boolean reopen, boolean download) {
      if (!PENDING.isEmpty()) {
         Map<BaulooStylesClient.Tab, String> convert = new EnumMap<>(BaulooStylesClient.Tab.class);
         PENDING.forEach((t, f) -> {
            if (f.startsWith("mypack:") || f.startsWith("vippack:")) {
               convert.put(t, f);
            }
         });
         if (!convert.isEmpty()) {
            this.pendingCloud = "\u0000batch";
            Thread thread = new Thread(() -> {
               Map<BaulooStylesClient.Tab, String> ids = new EnumMap<>(BaulooStylesClient.Tab.class);
               convert.forEach((t, f) -> {
                  String id = null;
                  if (f.startsWith("mypack:")) {
                     MyPacks.Style s = MyPacks.byId(f.substring("mypack:".length()));
                     id = s == null ? null : MyPacks.use(s);
                  } else {
                     VipPacks.Style s = VipPacks.byId(f.substring("vippack:".length()));
                     id = s == null ? null : VipPacks.use(s);
                  }

                  ids.put(t, id == null ? "" : id);
               });
               Minecraft.getInstance().execute(() -> {
                  this.pendingCloud = null;
                  ids.forEach((t, id) -> {
                     if (id.isEmpty()) {
                        PENDING.remove(t);
                     } else {
                        PENDING.put(t, id);
                     }
                  });
                  this.applyPending(reopen && Minecraft.getInstance().gui.screen() == this, download);
               });
            }, "Bauloo Styles pack picks");
            thread.setDaemon(true);
            thread.start();
         } else {
            List<CompletableFuture<Boolean>> downloads = new ArrayList<>();

            for (Entry<BaulooStylesClient.Tab, String> e : new ArrayList<>(PENDING.entrySet())) {
               StylesCloud.Style cloud = StylesCloud.byFolder(e.getValue());
               if (cloud != null && !StylesCloud.isReady(cloud)) {
                  if (download) {
                     downloads.add(StylesCloud.download(cloud));
                  } else {
                     PENDING.remove(e.getKey());
                     BaulooStylesClient.LOGGER.warn("[Batch] {} could not be downloaded, skipped", cloud.id());
                     StylesPublicPresets.say("\"" + cloud.name() + "\" could not be downloaded");
                  }
               }
            }

            if (!downloads.isEmpty()) {
               this.pendingCloud = "\u0000batch";
               CompletableFuture.allOf(downloads.toArray(new CompletableFuture[0])).whenComplete((ok, error) -> Minecraft.getInstance().schedule(() -> {
                  this.pendingCloud = null;
                  this.applyPending(reopen && Minecraft.getInstance().gui.screen() == this, false);
               }));
            } else if (!PENDING.isEmpty()) {
               Map<BaulooStylesClient.Tab, String> todo = new EnumMap<>(PENDING);
               PENDING.clear();
               Minecraft mc = Minecraft.getInstance();
               PackRepository packs = mc.getResourcePackRepository();
               packs.reload();
               List<String> enabled = new ArrayList<>(packs.getSelectedIds());

               for (Entry<BaulooStylesClient.Tab, String> ex : todo.entrySet()) {
                  BaulooStylesClient.Tab value = ex.getKey();
                  if (value == BaulooStylesClient.Tab.SKY) {
                  }

                  enabled.removeIf(id -> isPackOf(value, id));
                  if (!"\u0000vanilla".equals(ex.getValue()) && packs.getAvailableIds().contains(packId(ex.getValue()))) {
                     enabled.add(packId(ex.getValue()));
                  }
               }

               packs.setSelected(enabled);
               mc.options.updateResourcePacks(packs);
               mc.reloadResourcePacks();
               if (reopen) {
                  BaulooStylesConfigScreen next = new BaulooStylesConfigScreen(this.parent);
                  next.tab = this.tab;
                  next.query = this.query;
                  next.favoritesOnly = this.favoritesOnly;
                  next.rebuild();
                  next.gridScroll = this.gridScroll;
                  mc.gui.setScreen(next);
               }
            }
         }
      }
   }

   private void applyAndRefresh(String folder) {
      this.applyAndRefresh(folder, this.tab);
   }

   private void applyAndRefresh(String folder, BaulooStylesClient.Tab target) {
      if (target == BaulooStylesClient.Tab.SKY && folder != null) {
      }

      StylesCloud.Style cloud = StylesCloud.byFolder(folder);
      if (cloud != null && !StylesCloud.isReady(cloud)) {
         this.pendingCloud = folder;
         BaulooStylesClient.Tab startedIn = this.tab;
         StylesCloud.download(cloud).thenAccept(ok -> Minecraft.getInstance().schedule(() -> {
            if (Minecraft.getInstance().gui.screen() == this && folder.equals(this.pendingCloud) && this.tab == startedIn) {
               this.pendingCloud = null;
               if (ok) {
                  this.applyAndRefresh(folder, target);
               }
            }
         }));
      } else {
         this.pendingCloud = null;
         Minecraft mc = Minecraft.getInstance();
         PackRepository packs = mc.getResourcePackRepository();
         packs.reload();
         List<String> enabled = new ArrayList<>(packs.getSelectedIds());
         enabled.removeIf(id -> isPackOf(target, id));
         if (folder != null) {
            String id = packId(folder);
            if (packs.getAvailableIds().contains(id)) {
               enabled.add(id);
            }
         }

         packs.setSelected(enabled);
         mc.options.updateResourcePacks(packs);
         mc.reloadResourcePacks();
         BaulooStylesConfigScreen next = new BaulooStylesConfigScreen(this.parent);
         next.tab = this.tab;
         next.query = this.query;
         next.favoritesOnly = this.favoritesOnly;
         next.rebuild();
         next.gridScroll = this.gridScroll;
         mc.gui.setScreen(next);
      }
   }

   private static Identifier preview(String folder) {
      StylesCloud.Style cloud = StylesCloud.byFolder(folder);
      if (cloud != null) {
         return StylesCloud.preview(cloud);
      } else if (folder.startsWith("bauloo_upload_")) {
         return StylesUploadManager.preview(folder);
      } else if (folder.startsWith("mypack:")) {
         return MyPacks.preview(folder.substring("mypack:".length()));
      } else {
         return folder.startsWith("vippack:") ? VipPacks.preview(folder.substring("vippack:".length())) : builtIn(folder);
      }
   }

   private static Identifier builtIn(String folder) {
      Identifier file = Identifier.fromNamespaceAndPath("bauloo_styles", "textures/preview/" + folder + ".png");
      Identifier ready = BUILT_IN.get(folder);
      if (ready != null) {
         return ready;
      } else {
         Minecraft mc = Minecraft.getInstance();

         try {
            Optional<Resource> resource = mc.getResourceManager().getResource(file);
            if (resource.isEmpty()) {
               BUILT_IN.put(folder, file);
               return file;
            } else {
               NativeImage image;
               try (InputStream in = resource.get().open()) {
                  image = PreviewTexture.shrink(NativeImage.read(in.readAllBytes()));
               }

               Identifier sharp = Identifier.fromNamespaceAndPath("bauloo_styles", "sharp_preview/" + folder);
               mc.getTextureManager().register(sharp, new PreviewTexture(sharp, image));
               BUILT_IN.put(folder, sharp);
               return sharp;
            }
         } catch (RuntimeException | IOException var11) {
            BUILT_IN.put(folder, file);
            return file;
         }
      }
   }

   private static BaulooStylesConfigScreen.Clip clip(String folder) {
      return folder != null && !folder.startsWith("bauloo_upload_") && !folder.startsWith("bauloo_cloud_") && folder.matches("[a-z0-9_./-]+")
         ? CLIPS.computeIfAbsent(
               folder,
               f -> {
                  Identifier meta = Identifier.fromNamespaceAndPath("bauloo_styles", "textures/preview_clip/" + f + ".json");

                  try {
                     Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(meta);
                     if (resource.isEmpty()) {
                        return Optional.empty();
                     } else {
                        Optional var7;
                        try (BufferedReader reader = resource.get().openAsReader()) {
                           JsonObject o = JsonParser.parseReader(reader).getAsJsonObject();
                           BaulooStylesConfigScreen.Clip clip = new BaulooStylesConfigScreen.Clip(
                              o.get("frames").getAsInt(), o.get("fps").getAsInt(), o.get("size").getAsInt(), o.get("columns").getAsInt()
                           );
                           boolean sane = clip.frames() > 1 && clip.fps() > 0 && clip.size() > 0 && clip.columns() > 0;
                           var7 = sane ? Optional.of(clip) : Optional.empty();
                        }

                        return var7;
                     }
                  } catch (Exception var10) {
                     return Optional.empty();
                  }
               }
            )
            .orElse(null)
         : null;
   }

   private boolean clipPlaying(int index) {
      return index == this.hoverTrack && System.currentTimeMillis() - this.hoverSince >= 450L && clip(this.entries.get(index)[0]) != null;
   }

   private void drawClip(GuiGraphicsExtractor ctx, String folder, int x, int y, int w, int h) {
      BaulooStylesConfigScreen.Clip clip = clip(folder);
      if (clip != null && w > 0 && h > 0) {
         long elapsed = System.currentTimeMillis() - this.hoverSince - 450L;
         int frame = (int)(Math.max(0L, elapsed) * clip.fps() / 1000L % clip.frames());
         int s = clip.size();
         int rows = (clip.frames() + clip.columns() - 1) / clip.columns();
         int regionW = s;
         int regionH = s;
         float u = frame % clip.columns() * s;
         float v = frame / clip.columns() * s;
         if (w > h) {
            regionH = Math.max(1, s * h / w);
            v += (s - regionH) / 2.0F;
         } else if (h > w) {
            regionW = Math.max(1, s * w / h);
            u += (s - regionW) / 2.0F;
         }

         ctx.blit(
            RenderPipelines.GUI_TEXTURED,
            Identifier.fromNamespaceAndPath("bauloo_styles", "textures/preview_clip/" + folder + ".png"),
            x,
            y,
            u,
            v,
            w,
            h,
            regionW,
            regionH,
            clip.columns() * s,
            rows * s
         );
      } else {
         this.drawPreview(ctx, folder, x, y, w, h);
      }
   }

   private BaulooStylesClient.Tab tabOf(String folder) {
      StylesCloud.Style cloud = StylesCloud.byFolder(folder);
      String rest = cloud != null
         ? cloud.category()
         : (
            folder.startsWith("bauloo_upload_")
               ? folder.substring("bauloo_upload_".length())
               : (
                  folder.startsWith("mypack:")
                     ? folder.substring("mypack:".length())
                     : (folder.startsWith("vippack:") ? folder.substring("vippack:".length()) : folder)
               )
         );

      for (BaulooStylesClient.Tab value : BaulooStylesClient.Tab.values()) {
         if (rest.startsWith(value.prefix)) {
            return value;
         }
      }

      return this.tab;
   }

   private BaulooStylesConfigScreen.Layout layout() {
      BaulooStylesConfigScreen.Layout l = new BaulooStylesConfigScreen.Layout();
      l.fw = Math.max(300, Math.min(this.width - 24, Math.max(620, Math.round(this.width * 0.86F))));
      l.fh = Math.max(200, Math.min(this.height - 24, Math.max(340, Math.round(this.height * 0.92F))));
      l.fx = (this.width - l.fw) / 2;
      l.fy = Math.max(8, (this.height - l.fh) / 2);
      l.bodyY = l.fy + 38;
      l.footerY = l.fy + l.fh - 28;
      l.side = l.fw >= 560;
      l.railW = clamp(l.fw / 5, 116, 160);
      l.sideW = l.side ? clamp(l.fw / 4, 158, 206) : 0;
      l.sideX = l.fx + l.fw - l.sideW - 10;
      l.mainX = l.fx + l.railW + 12;
      l.mainW = (l.side ? l.sideX - 8 : l.fx + l.fw - 12) - l.mainX;
      return l;
   }

   private static int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private static boolean inside(double x, double y, int left, int top, int w, int h) {
      return x >= left && x < left + w && y >= top && y < top + h;
   }

   private void hit(int x, int y, int w, int h, BaulooStylesConfigScreen.Action action) {
      this.hits.add(new BaulooStylesConfigScreen.Hit(x, y, w, h, this.hitClipTop, this.hitClipBottom, action));
   }

   private int textWidth(String value) {
      return this.font.width(value);
   }

   private void text(GuiGraphicsExtractor ctx, String value, int x, int y, int color) {
      ctx.text(this.font, value, x, y, color, true);
   }

   private void textRight(GuiGraphicsExtractor ctx, String value, int right, int y, int color) {
      this.text(ctx, value, right - this.textWidth(value), y, color);
   }

   private void textCenter(GuiGraphicsExtractor ctx, String value, int x, int w, int y, int color) {
      this.text(ctx, value, x + (w - this.textWidth(value)) / 2, y, color);
   }

   private void scaled(GuiGraphicsExtractor ctx, String value, float x, float y, int color, float scale, boolean shadow) {
      Matrix3x2fStack m = ctx.pose();
      m.pushMatrix();
      m.translate(x, y);
      m.scale(scale, scale);
      ctx.text(this.font, value, 0, 0, color, shadow);
      m.popMatrix();
   }

   private void pageTitle(GuiGraphicsExtractor ctx, String value, int x, int y) {
      this.scaled(ctx, value, x, y - 4, this.c.text, 1.5F, true);
   }

   private int titleWidth(String value) {
      return this.scaledWidth(value, 1.5F);
   }

   private int scaledWidth(String value, float scale) {
      return Math.round(this.textWidth(value) * scale);
   }

   private String fitText(String value, int availableWidth) {
      if (availableWidth <= 0) {
         return "";
      } else if (this.textWidth(value) <= availableWidth) {
         return value;
      } else {
         String shortValue = value;

         while (!shortValue.isEmpty() && this.textWidth(shortValue + "...") > availableWidth) {
            shortValue = shortValue.substring(0, shortValue.length() - 1);
         }

         return shortValue + "...";
      }
   }

   private static void rounded(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int[] corner, int color) {
      int r = Math.min(corner.length, Math.min(w, h) / 2);

      for (int i = 0; i < r; i++) {
         int in = corner[i];
         ctx.fill(x + in, y + i, x + w - in, y + i + 1, color);
         ctx.fill(x + in, y + h - 1 - i, x + w - in, y + h - i, color);
      }

      ctx.fill(x, y + r, x + w, y + h - r, color);
   }

   private static void roundCut(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int[] corner, int background) {
      int r = Math.min(corner.length, Math.min(w, h) / 2);

      for (int i = 0; i < r; i++) {
         int in = corner[i];
         if (in > 0) {
            ctx.fill(x, y + i, x + in, y + i + 1, background);
            ctx.fill(x + w - in, y + i, x + w, y + i + 1, background);
            ctx.fill(x, y + h - 1 - i, x + in, y + h - i, background);
            ctx.fill(x + w - in, y + h - 1 - i, x + w, y + h - i, background);
         }
      }
   }

   private static void soft(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int color) {
      if (w >= 6 && h >= 6) {
         rounded(ctx, x, y, w, h, SMALL, color);
      } else if (w >= 3 && h >= 3) {
         ctx.fill(x + 1, y, x + w - 1, y + h, color);
         ctx.fill(x, y + 1, x + 1, y + h - 1, color);
         ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
      } else {
         ctx.fill(x, y, x + w, y + h, color);
      }
   }

   private static void softLine(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int color) {
      if (w >= 6 && h >= 6) {
         ctx.fill(x + 2, y, x + w - 2, y + 1, color);
         ctx.fill(x + 2, y + h - 1, x + w - 2, y + h, color);
         ctx.fill(x, y + 2, x + 1, y + h - 2, color);
         ctx.fill(x + w - 1, y + 2, x + w, y + h - 2, color);
         ctx.fill(x + 1, y + 1, x + 2, y + 2, color);
         ctx.fill(x + w - 2, y + 1, x + w - 1, y + 2, color);
         ctx.fill(x + 1, y + h - 2, x + 2, y + h - 1, color);
         ctx.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, color);
      } else {
         ctx.fill(x + 1, y, x + w - 1, y + 1, color);
         ctx.fill(x + 1, y + h - 1, x + w - 1, y + h, color);
         ctx.fill(x, y + 1, x + 1, y + h - 1, color);
         ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
      }
   }

   private static void cutCorners(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int background) {
      if (w >= 6 && h >= 6) {
         roundCut(ctx, x, y, w, h, SMALL, background);
      } else {
         ctx.fill(x, y, x + 1, y + 1, background);
         ctx.fill(x + w - 1, y, x + w, y + 1, background);
         ctx.fill(x, y + h - 1, x + 1, y + h, background);
         ctx.fill(x + w - 1, y + h - 1, x + w, y + h, background);
      }
   }

   private void drawPreview(GuiGraphicsExtractor ctx, String folder, int x, int y, int w, int h) {
      if (w > 0 && h > 0) {
         Identifier picture = preview(folder);
         if (picture == null) {
            ctx.fill(x, y, x + w, y + h, this.c.surface2);
            if (w >= 18 && h >= 18) {
               itemIcon(ctx, iconItem(this.tabOf(folder)), x + (w - 16) / 2, y + (h - 16) / 2);
            }
         } else {
            int tex = 256;
            int regionW = tex;
            int regionH = tex;
            float u = 0.0F;
            float v = 0.0F;
            if (w > h) {
               regionH = Math.max(1, tex * h / w);
               v = (tex - regionH) / 2.0F;
            } else if (h > w) {
               regionW = Math.max(1, tex * w / h);
               u = (tex - regionW) / 2.0F;
            }

            ctx.blit(RenderPipelines.GUI_TEXTURED, picture, x, y, u, v, w, h, regionW, regionH, tex, tex);
            if (folder.startsWith("sword_") || folder.startsWith("pickaxe_")) {
               int frame = (int)(System.currentTimeMillis() / 90L % 12L);
               Identifier glint = Identifier.fromNamespaceAndPath("bauloo_styles", "textures/preview_glint/" + folder + "_" + frame + ".png");
               ctx.blit(RenderPipelines.GUI_TEXTURED, glint, x, y, u, v, w, h, regionW, regionH, tex, tex, glintTint());
            }
         }
      }
   }

   private static int glintTint() {
      if (StylesUiConfig.glintRainbow) {
         return StylesTheme.hsv((float)(System.currentTimeMillis() % 4000L) / 4000.0F, 0.6F, 1.0F) | -1073741824;
      } else {
         int rgb = StylesUiConfig.glintCustom ? StylesUiConfig.glintColor & 16777215 : 10510591;
         return -1073741824 | rgb;
      }
   }

   private void drawThumb(GuiGraphicsExtractor ctx, BaulooStylesClient.Tab value, int x, int y, int size, int background) {
      String folder = this.active.get(value);
      if (folder == null) {
         soft(ctx, x, y, size, size, this.c.surface2);
         itemIcon(ctx, iconItem(value), x + (size - 16) / 2, y + (size - 16) / 2);
      } else {
         this.drawPreview(ctx, folder, x, y, size, size);
         cutCorners(ctx, x, y, size, size, background);
      }
   }

   private void button(GuiGraphicsExtractor ctx, int x, int y, int w, int h, String label, boolean hover, BaulooStylesConfigScreen.ButtonStyle style) {
      int fill;
      int color;
      switch (style) {
         case PRIMARY:
            fill = hover ? this.c.accentBright : this.c.accent;
            color = this.c.onAccent;
            break;
         case DISABLED:
            fill = this.c.surface;
            color = this.c.textDim;
            break;
         default:
            fill = hover ? this.c.surfaceHover : this.c.surface2;
            color = hover ? this.c.text : this.c.textSoft;
      }

      soft(ctx, x, y, w, h, fill);
      if (!label.isEmpty()) {
         String fitted = this.fitText(label, w - 8);
         ctx.text(this.font, fitted, x + (w - this.textWidth(fitted)) / 2, y + (h - 8) / 2, color, style != BaulooStylesConfigScreen.ButtonStyle.PRIMARY);
      }
   }

   private float uiFactor() {
      double real = Minecraft.getInstance().getWindow().getGuiScale();
      return real <= 0.0 ? 1.0F : (float)(2.0 / real);
   }

   private void enterScale() {
      if (!this.scaled) {
         float f = this.uiFactor();
         this.realWidth = this.width;
         this.realHeight = this.height;
         this.width = Math.round(this.realWidth / f);
         this.height = Math.round(this.realHeight / f);
         this.scaled = true;
      }
   }

   private void leaveScale() {
      if (this.scaled) {
         this.width = this.realWidth;
         this.height = this.realHeight;
         this.scaled = false;
      }
   }

   private MouseButtonEvent scaledClick(MouseButtonEvent click) {
      float f = this.uiFactor();
      return new MouseButtonEvent(click.x() / f, click.y() / f, click.buttonInfo());
   }

   public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
      float f = this.uiFactor();
      this.enterScale();
      Matrix3x2fStack m = ctx.pose();
      m.pushMatrix();
      m.scale(f, f);

      try {
         this.renderUi(ctx, Math.round(mouseX / f), Math.round(mouseY / f), delta);
      } finally {
         m.popMatrix();
         this.leaveScale();
      }
   }

   public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
      this.enterScale();

      boolean var3;
      try {
         var3 = this.clickedUi(this.scaledClick(click), doubled);
      } finally {
         this.leaveScale();
      }

      return var3;
   }

   public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
      float f = this.uiFactor();
      this.enterScale();

      boolean var7;
      try {
         var7 = this.draggedUi(this.scaledClick(click), offsetX / f, offsetY / f);
      } finally {
         this.leaveScale();
      }

      return var7;
   }

   public boolean mouseReleased(MouseButtonEvent click) {
      this.enterScale();

      boolean var2;
      try {
         var2 = this.releasedUi(this.scaledClick(click));
      } finally {
         this.leaveScale();
      }

      return var2;
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      float f = this.uiFactor();
      this.enterScale();

      boolean var10;
      try {
         var10 = this.scrolledUi(mouseX / f, mouseY / f, horizontalAmount, verticalAmount);
      } finally {
         this.leaveScale();
      }

      return var10;
   }

   private void renderUi(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
      if (this.liveDrag()) {
         this.drawLiveSlider(ctx);
      } else {
         super.extractRenderState(ctx, mouseX, mouseY, delta);
         String access = VipPacks.earlyActive() + "/" + VipPacks.admin();
         if (!access.equals(earlyBound)) {
            earlyBound = access;
            BaulooStylesClient.Tab.bindCloudSlots();
         }

         if (this.cloudVersion != StylesCloud.version()
            || this.uploadVersion != StylesUploadManager.version()
            || this.bindVersion != BaulooStylesClient.Tab.bindVersion) {
            double keepScroll = this.gridScroll;
            this.rebuild();
            this.gridScroll = keepScroll;
         }

         this.c = StylesUiConfig.theme();
         this.active = activeFolders();
         this.hits.clear();
         this.tooltip = null;
         BaulooStylesConfigScreen.Layout l = this.layout();
         if (!this.whatsNewChecked) {
            this.whatsNewChecked = true;
            this.whatsNew = WhatsNew.due();
         }

         if (this.updateAsk == null && this.whatsNew == null) {
            String v = StylesUpdater.availableVersion;
            if (v != null && !v.equals(StylesUiConfig.updateSeen)) {
               this.updateAsk = v;
            }
         }

         boolean modal = this.zoomFolder != null || this.pendingPreset != null || this.updateAsk != null || this.resetAsk || this.whatsNew != null;
         int mx = modal ? -10000 : mouseX;
         int my = modal ? -10000 : mouseY;
         ctx.fill(0, 0, this.width, this.height, this.c.backdrop);
         this.drawGlassPanel(ctx, l);
         if (this.c.spooky) {
            HalloweenFx.behind(ctx, l.fx, l.fy, l.fw, 38, l.mainX, l.mainW, l.footerY);
         }

         this.drawHeader(ctx, l, mx, my);
         this.drawRail(ctx, l, mx, my);
         this.hoveredEntry = -1;
         Cosmetics.tryOn(null, null);
         if (cosmeticsView && !publicView) {
            this.drawCosmetics(ctx, l, mx, my);
         } else if (publicView) {
            this.drawPublicView(ctx, l, mx, my);
         } else if (extrasView) {
            this.drawExtrasTiles(ctx, l, mx, my);
         } else if (!l.side && presetsView) {
            this.pageTitle(ctx, "Presets", l.mainX, l.bodyY + 11);
            this.drawPresets(ctx, l.mainX, l.bodyY + 30, l.mainW, l.footerY - 10, mx, my);
         } else if (!l.side && this.compactThemes) {
            this.pageTitle(ctx, "Themes", l.mainX, l.bodyY + 11);
            this.drawThemes(ctx, l.mainX, l.bodyY + 30, l.mainW, l.footerY - 10, mx, my, this.c.frame);
         } else {
            this.drawMain(ctx, l, mx, my);
         }

         if (l.side && !extrasView && !publicView && !cosmeticsView) {
            this.drawSide(ctx, l, mx, my);
         }

         this.drawFooter(ctx, l, mx, my);
         if (this.c.spooky) {
            HalloweenFx.front(ctx, l.fx, l.fy, l.fw, l.fh, 38, l.footerY, l.side, l.sideX, l.sideW);
         }

         if (this.tooltip != null) {
            int tw = this.textWidth(this.tooltip) + 10;
            int tx = Math.min(this.tooltipX, this.width - tw - 4);
            soft(ctx, tx, this.tooltipY, tw, 14, this.c.surface2);
            softLine(ctx, tx, this.tooltipY, tw, 14, this.c.borderStrong);
            this.text(ctx, this.tooltip, tx + 5, this.tooltipY + 3, this.c.text);
         }

         if (this.pendingPreset != null) {
            this.drawExtrasAsk(ctx, mouseX, mouseY);
         }

         if (this.updateAsk != null && this.pendingPreset == null) {
            this.drawUpdateAsk(ctx, mouseX, mouseY);
         }

         if (this.whatsNew != null && this.pendingPreset == null && this.updateAsk == null) {
            this.drawWhatsNew(ctx, mouseX, mouseY);
         }

         if (this.resetAsk) {
            this.drawResetAsk(ctx, mouseX, mouseY);
         }

         if (this.zoomFolder != null) {
            this.drawZoom(ctx);
         }
      }
   }

   private void drawHeader(GuiGraphicsExtractor ctx, BaulooStylesConfigScreen.Layout l, int mx, int my) {
      int x = l.fx;
      int y = l.fy;
      ctx.fill(x + 10, y + 38 - 1, x + l.fw - 10, y + 38, this.c.borderDim);
      String version = StylesUpdater.installedVersion == null ? "" : "v" + StylesUpdater.installedVersion;
      int badgeEnd = x + 33 + Math.max(this.textWidth("Bauloo Styles"), this.scaledWidth(version, 0.8F));
      if (this.c.spooky) {
         HalloweenFx.moon(ctx, x, y, l.fw, 38);
         HalloweenFx.pumpkin(ctx, x + 7, y + 6, 24);
         int bx = badgeEnd + 10;
         int bw = this.scaledWidth("HALLOWEEN", 0.75F) + 10;
         soft(ctx, bx, y + 13, bw, 12, this.c.accent);
         this.scaled(ctx, "HALLOWEEN", bx + 5, y + 16, this.c.onAccent, 0.75F, false);
         badgeEnd = bx + bw;
      } else {
         ctx.blit(RenderPipelines.GUI_TEXTURED, TORNADO, x + 9, y + 8, 0.0F, 0.0F, 20, 20, 240, 240, 240, 240, this.c.accent);
      }

      this.text(ctx, "Bauloo Styles", x + 33, y + 11, this.c.text);
      this.scaled(ctx, version, x + 33, y + 23, this.c.textDim, 0.8F, false);
      BaulooStylesClient.Tab[] tabs = shownTabs();
      int size = 18;
      int step = size + 5;
      int stripW = tabs.length * step - 5;
      int sx = x + l.fw - 14 - stripW;
      int sy = y + 9;
      int left = badgeEnd + 20;
      if (this.c.spooky) {
         HalloweenFx.bats(ctx, left, sx - this.scaledWidth("LOADOUT", 0.75F) - 16, y + 6);
      }

      if (sx - this.scaledWidth("LOADOUT", 0.75F) - 8 >= left) {
         this.scaled(ctx, "LOADOUT", sx - this.scaledWidth("LOADOUT", 0.75F) - 8, sy + 7, this.c.textDim, 0.75F, false);

         for (int i = 0; i < tabs.length; i++) {
            BaulooStylesClient.Tab t = tabs[i];
            int tx = sx + i * step;
            boolean hover = inside(mx, my, tx, sy, size, size + 5);
            this.drawThumb(ctx, t, tx, sy, size, this.c.header);
            if (t == this.tab) {
               soft(ctx, tx + 2, sy + size + 2, size - 4, 2, this.c.accent);
            } else if (hover) {
               soft(ctx, tx + 5, sy + size + 2, size - 10, 2, this.c.textSoft);
            }

            if (hover) {
               this.tooltip = t.title + ": " + this.activeName(t);
               this.tooltipX = tx - 20;
               this.tooltipY = sy + size + 7;
            }

            this.hit(tx, sy, size, size + 5, (b, px, py) -> this.switchTab(t));
         }
      }
   }

   private void drawRail(GuiGraphicsExtractor ctx, BaulooStylesConfigScreen.Layout l, int mx, int my) {
      int x = l.fx;
      int y = l.bodyY;
      int w = l.railW;
      ctx.fill(x + w, y + 8, x + w + 1, l.footerY - 8, this.c.borderDim);
      int cw = this.textWidth("COLLECTIONS");
      boolean hover = inside(mx, my, x + 9, y + 8, cw + 6, 16);
      this.text(ctx, "COLLECTIONS", x + 12, y + 12, !extrasView ? this.c.accent : (hover ? this.c.text : this.c.textDim));
      if (!extrasView) {
         soft(ctx, x + 12, y + 23, cw, 2, this.c.accent);
      }

      this.hit(x + 9, y + 8, cw + 6, 16, (b, px, py) -> {
         extrasView = false;
         publicView = false;
         cosmeticsView = false;
      });
      String label = "EXTRAS";
      hover = (boolean)this.textWidth(label);
      int lx = x + w - 10 - hover;
      if (lx > x + 12 + this.textWidth("COLLECTIONS") + 6) {
         boolean hoverx = inside(mx, my, lx - 3, y + 8, hover + 6, 16);
         this.text(ctx, label, lx, y + 12, extrasView ? this.c.accent : (hoverx ? this.c.text : this.c.textSoft));
         if (extrasView) {
            soft(ctx, lx, y + 23, hover, 2, this.c.accent);
         }

         this.hit(lx - 3, y + 8, hover + 6, 16, (b, px, py) -> {
            extrasView = !extrasView;
            publicView = false;
            cosmeticsView = false;
            this.searchFocused = false;
         });
      }

      cw = y + 30;
      hover = (boolean)(shownTabs().length + 2 + (MyPacks.shown() ? 1 : 0) + (VipPacks.shown() ? 1 : 0));
      lx = (l.footerY - cw - 26) / hover < 17 ? 1 : 4;
      int rowH = Math.max(12, Math.min(30, (l.footerY - cw - 26) / hover - lx));

      for (BaulooStylesClient.Tab t : shownTabs()) {
         int rx = x + 7;
         int rw = w - 14;
         boolean selected = t == this.tab && !extrasView && !cosmeticsView && !myPacksView;
         boolean hoverx = inside(mx, my, rx, cw, rw, rowH);
         int rowBg = this.c.rail;
         if (selected) {
            soft(ctx, rx, cw, rw, rowH, this.c.surface2);
            soft(ctx, rx, cw + 5, 2, rowH - 10, this.c.accent);
            rowBg = this.c.surface2;
         } else if (hoverx) {
            soft(ctx, rx, cw, rw, rowH, this.c.surface);
            rowBg = this.c.surface;
         }

         boolean compact = rowH < 30;
         int thumb = compact ? rowH - 6 : 20;
         this.drawThumb(ctx, t, rx + 7, cw + (rowH - thumb) / 2, thumb, rowBg);
         String count = String.valueOf(this.styleCount(t));
         this.textRight(ctx, count, rx + rw - 7, cw + (rowH - 8) / 2, selected ? this.c.accent : this.c.textDim);
         if (pendingIn(t)) {
            int dx = rx + rw - 7 - this.textWidth(count) - 7;
            int dy = cw + rowH / 2 - 2;
            ctx.fill(dx, dy, dx + 4, dy + 4, -15811);
         }

         int textW = rw - 34 - this.textWidth(count) - 12;
         if (compact) {
            this.text(ctx, this.fitText(t.title, textW), rx + 34, cw + (rowH - 8) / 2, selected ? this.c.text : this.c.textSoft);
         } else {
            this.text(ctx, this.fitText(t.title, textW), rx + 34, cw + 5, selected ? this.c.text : this.c.textSoft);
            boolean vanilla = this.active.get(t) == null;
            this.scaled(
               ctx,
               this.fitText(this.activeName(t), Math.round(textW / 0.8F)),
               rx + 34,
               cw + 17,
               vanilla ? this.c.textDim : (selected ? this.c.accentBright : this.c.textSoft),
               0.8F,
               false
            );
         }

         BaulooStylesClient.Tab target = t;
         this.hit(rx, cw, rw, rowH, (b, px, py) -> this.switchTab(target));
         cw += rowH + lx;
      }

      ctx.fill(x + 12, cw + 1, x + w - 12, cw + 2, this.c.borderDim);
      cw += 6;
      if (MyPacks.shown()) {
         int rxx = x + 7;
         int rwx = w - 14;
         boolean selectedx = myPacksView && !vipView && !extrasView && !cosmeticsView && !publicView;
         boolean hoverxx = inside(mx, my, rxx, cw, rwx, rowH);
         if (selectedx) {
            soft(ctx, rxx, cw, rwx, rowH, this.c.surface2);
            soft(ctx, rxx, cw + 5, 2, rowH - 10, this.c.accent);
         } else if (hoverxx) {
            soft(ctx, rxx, cw, rwx, rowH, this.c.surface);
         }

         int thumbx = rowH < 30 ? rowH - 6 : 20;
         soft(ctx, rxx + 7, cw + (rowH - thumbx) / 2, thumbx, thumbx, this.c.accentDark);
         ctx.item(stackOf(Items.BOOKSHELF), rxx + 7 + (thumbx - 16) / 2, cw + (rowH - 16) / 2);
         String countx = MyPacks.scanning() ? "..." : String.valueOf(MyPacks.styles().size());
         this.textRight(ctx, countx, rxx + rwx - 7, cw + (rowH - 8) / 2, selectedx ? this.c.text : this.c.textDim);
         this.text(
            ctx, this.fitText("My Packs", rwx - 34 - this.textWidth(countx) - 14), rxx + 34, cw + (rowH - 8) / 2, selectedx ? this.c.text : this.c.textSoft
         );
         this.hit(rxx, cw, rwx, rowH, (b, px, py) -> {
            myPacksView = true;
            vipView = false;
            cosmeticsView = false;
            extrasView = false;
            publicView = false;
            this.searchFocused = false;
            this.query = "";
            this.favoritesOnly = false;
            this.gridScroll = 0.0;
            this.focusedIndex = 0;
            this.rebuild();
         });
         cw += rowH + lx;
      }

      if (VipPacks.shown()) {
         int rxx = x + 7;
         int rwx = w - 14;
         boolean selectedx = myPacksView && vipView && !extrasView && !cosmeticsView && !publicView;
         boolean hoverxx = inside(mx, my, rxx, cw, rwx, rowH);
         if (selectedx) {
            soft(ctx, rxx, cw, rwx, rowH, this.c.surface2);
            soft(ctx, rxx, cw + 5, 2, rowH - 10, -15811);
         } else if (hoverxx) {
            soft(ctx, rxx, cw, rwx, rowH, this.c.surface);
         }

         int thumbx = rowH < 30 ? rowH - 6 : 20;
         soft(ctx, rxx + 7, cw + (rowH - thumbx) / 2, thumbx, thumbx, -10860004);
         ctx.item(stackOf(Items.ENCHANTED_BOOK), rxx + 7 + (thumbx - 16) / 2, cw + (rowH - 16) / 2);
         VipPacks.refresh(false);
         String countx = VipPacks.loading() && VipPacks.styles().isEmpty() ? "..." : String.valueOf(VipPacks.styles().size());
         this.textRight(ctx, countx, rxx + rwx - 7, cw + (rowH - 8) / 2, selectedx ? this.c.text : this.c.textDim);
         int tagW = this.scaledWidth("VIP", 0.7F) + 6;
         int labelW = rwx - 34 - this.textWidth(countx) - 14 - tagW - 4;
         String labelx = this.fitText("Bauloo's Packs", labelW);
         this.text(ctx, labelx, rxx + 34, cw + (rowH - 8) / 2, selectedx ? this.c.text : this.c.textSoft);
         int tx = rxx + 34 + this.textWidth(labelx) + 4;
         soft(ctx, tx, cw + (rowH - 9) / 2, tagW, 9, -15811);
         this.scaled(ctx, "VIP", tx + 3, cw + (rowH - 9) / 2 + 2, -15068416, 0.7F, false);
         this.hit(rxx, cw, rwx, rowH, (b, px, py) -> {
            myPacksView = true;
            vipView = true;
            cosmeticsView = false;
            extrasView = false;
            publicView = false;
            this.searchFocused = false;
            this.query = "";
            this.favoritesOnly = false;
            this.gridScroll = 0.0;
            this.focusedIndex = 0;
            VipPacks.refresh(true);
            this.rebuild();
         });
         cw += rowH + lx;
      }

      int rxx = x + 7;
      int rwx = w - 14;
      boolean selectedx = cosmeticsView && !publicView;
      if (selectedx && !StylesUiConfig.cosmeticsSeen) {
         StylesUiConfig.cosmeticsSeen = true;
         StylesUiConfig.save();
      }

      boolean hoverxx = inside(mx, my, rxx, cw, rwx, rowH);
      if (selectedx) {
         soft(ctx, rxx, cw, rwx, rowH, this.c.surface2);
         soft(ctx, rxx, cw + 5, 2, rowH - 10, this.c.accent);
      } else if (hoverxx) {
         soft(ctx, rxx, cw, rwx, rowH, this.c.surface);
      }

      int thumbx = rowH < 30 ? rowH - 6 : 20;
      soft(ctx, rxx + 7, cw + (rowH - thumbx) / 2, thumbx, thumbx, this.c.accentDark);
      itemIcon(ctx, Items.ELYTRA, rxx + 7 + (thumbx - 16) / 2, cw + (rowH - 16) / 2);
      int worn = 0;

      for (Cosmetics.Slot slot : Cosmetics.Slot.values()) {
         if (Cosmetics.valid(slot, Cosmetics.saved(slot))) {
            worn++;
         }
      }

      int bw = 0;
      if (!StylesUiConfig.cosmeticsSeen) {
         String badge = "NEW";
         bw = this.scaledWidth(badge, 0.7F) + 6;
         soft(ctx, rxx + rwx - 7 - bw, cw + (rowH - 9) / 2, bw, 9, this.c.accent);
         this.scaled(ctx, badge, rxx + rwx - 4 - bw, cw + (rowH - 9) / 2 + 2, this.c.onAccent, 0.7F, false);
      }

      int textW = rwx - 34 - bw - 12;
      if (rowH < 30) {
         this.text(ctx, this.fitText("Cosmetics", textW), rxx + 34, cw + (rowH - 8) / 2, selectedx ? this.c.text : this.c.textSoft);
      } else {
         this.text(ctx, this.fitText("Cosmetics", textW), rxx + 34, cw + 5, selectedx ? this.c.text : this.c.textSoft);
         this.scaled(
            ctx,
            this.fitText(worn == 0 ? "Nothing worn" : worn + " worn", Math.round(textW / 0.8F)),
            rxx + 34,
            cw + 17,
            worn == 0 ? this.c.textDim : (selectedx ? this.c.accentBright : this.c.textSoft),
            0.8F,
            false
         );
      }

      this.hit(rxx, cw, rwx, rowH, (b, px, py) -> {
         cosmeticsView = true;
         myPacksView = false;
         extrasView = false;
         publicView = false;
         this.searchFocused = false;
      });
      cw += rowH + lx;
      rxx = x + 7;
      rwx = w - 14;
      selectedx = inside(mx, my, rxx, cw, rwx, rowH);
      if (selectedx) {
         soft(ctx, rxx, cw, rwx, rowH, this.c.surface);
      }

      hoverxx = (boolean)(rowH < 30 ? rowH - 6 : 20);
      soft(ctx, rxx + 7, cw + (rowH - hoverxx) / 2, hoverxx, hoverxx, this.c.accentDark);
      ctx.item(stackOf(Items.WRITABLE_BOOK), rxx + 7 + (hoverxx - 16) / 2, cw + (rowH - 16) / 2);
      String countx = String.valueOf(DrawScreen.count());
      this.textRight(ctx, countx, rxx + rwx - 7, cw + (rowH - 8) / 2, this.c.textDim);
      this.text(
         ctx, this.fitText("My Drawings", rwx - 34 - this.textWidth(countx) - 14), rxx + 34, cw + (rowH - 8) / 2, selectedx ? this.c.text : this.c.textSoft
      );
      this.hit(rxx, cw, rwx, rowH, (b, px, py) -> Minecraft.getInstance().gui.setScreen(new DrawScreen(this)));
      cw += rowH + lx;
      rxx = cw + 8;
      if (rxx + 34 < l.footerY) {
         ctx.fill(x + 12, rxx, x + w - 12, rxx + 1, this.c.borderDim);
         boolean hoverxxx = inside(mx, my, x + 7, rxx + 6, w - 14, 28);
         if (hoverxxx) {
            soft(ctx, x + 7, rxx + 6, w - 14, 28, this.c.surface);
         }

         selectedx = (boolean)0;

         for (int n : this.uploadCounts.values()) {
            selectedx += n;
         }

         this.text(ctx, "PRIVATE UPLOADS", x + 12, rxx + 10, hoverxxx ? this.c.textSoft : this.c.textDim);
         String line = hoverxxx ? "Upload a texture pack" : selectedx + (selectedx == 1 ? " pack on this PC" : " packs on this PC");
         this.text(ctx, this.fitText(line, w - 24), x + 12, rxx + 22, hoverxxx ? this.c.accent : this.c.textDim);
         this.hit(x + 7, rxx + 6, w - 14, 28, (b, px, py) -> StylesUploadServer.open());
      }
   }

   private void drawMain(GuiGraphicsExtractor ctx, BaulooStylesConfigScreen.Layout l, int mx, int my) {
      int x = l.mainX;
      int y = l.bodyY;
      int w = l.mainW;
      if (myPacksView) {
         this.drawMyPacksMain(ctx, l, mx, my);
      } else {
         this.pageTitle(ctx, this.tab.title, x, y + 11);
         if (this.tab == BaulooStylesClient.Tab.SKY) {
         }

         String part1 = this.styleCount(this.tab) + " styles  -  ";
         this.text(ctx, part1, x, y + 24, this.c.textDim);
         this.text(
            ctx,
            this.fitText(this.activeName(this.tab), w / 3),
            x + this.textWidth(part1),
            y + 24,
            this.active.get(this.tab) == null ? this.c.textSoft : -9183056
         );
         if (this.tab == BaulooStylesClient.Tab.INVENTORY) {
            this.headerToggle(
               ctx, x, y + 36, "CLEAR BACKGROUND", StylesUiConfig.invClearBackground, mx, my, "No dark veil or blur behind your inventory and chests", () -> {
                  StylesUiConfig.invClearBackground = !StylesUiConfig.invClearBackground;
                  BlurCompat.apply();
               }
            );
         } else if (this.tab == BaulooStylesClient.Tab.SKY) {
            this.drawTimeChanger(ctx, x, y + 36, mx, my);
         } else if (this.cobwebTab()) {
            this.headerToggle(
               ctx,
               x,
               y + 36,
               "COBWEB OUTLINE",
               StylesUiConfig.cobwebOutline,
               mx,
               my,
               "A white box around every cobweb near you",
               () -> StylesUiConfig.cobwebOutline = !StylesUiConfig.cobwebOutline
            );
         }

         int right = x + w;
         String favCount = String.valueOf(this.favoriteCount());
         String favLabel = "Favorites";
         int favW = this.textWidth("★ ") + this.textWidth(favLabel) + 4 + this.textWidth(favCount);
         int allW = this.textWidth("All");
         int favX = right - favW;
         int allX = favX - 14 - allW;
         int ty = y + 14;
         boolean allHover = inside(mx, my, allX - 3, ty - 3, allW + 6, 16);
         this.text(ctx, "All", allX, ty, !this.favoritesOnly ? this.c.text : (allHover ? this.c.textSoft : this.c.textDim));
         if (!this.favoritesOnly) {
            soft(ctx, allX, ty + 11, allW, 2, this.c.accent);
         }

         this.hit(allX - 3, ty - 3, allW + 6, 16, (b, px, py) -> {
            this.favoritesOnly = false;
            this.gridScroll = 0.0;
            this.applyFilter();
         });
         boolean favHover = inside(mx, my, favX - 3, ty - 3, favW + 6, 16);
         this.text(ctx, "★", favX, ty, -12454);
         int fx2 = favX + this.textWidth("★ ");
         this.text(ctx, favLabel, fx2, ty, this.favoritesOnly ? this.c.text : (favHover ? this.c.textSoft : this.c.textDim));
         this.text(ctx, favCount, fx2 + this.textWidth(favLabel) + 4, ty, this.favoritesOnly ? this.c.accent : this.c.textDim);
         if (this.favoritesOnly) {
            soft(ctx, favX, ty + 11, favW, 2, this.c.accent);
         }

         this.hit(favX - 3, ty - 3, favW + 6, 16, (b, px, py) -> {
            this.favoritesOnly = true;
            this.gridScroll = 0.0;
            this.applyFilter();
         });
         int titleW = Math.max(this.titleWidth(this.tab.title), this.textWidth(this.styleCount(this.tab) + " styles  -  " + this.activeName(this.tab)));
         int searchW = Math.min(130, allX - 16 - (x + titleW + 12));
         if (searchW >= 64) {
            int sx = allX - 16 - searchW;
            int sy = y + 10;
            boolean hover = inside(mx, my, sx, sy, searchW, 16);
            soft(ctx, sx, sy, searchW, 16, !hover && !this.searchFocused ? this.c.surface : this.c.surface2);
            if (this.searchFocused) {
               soft(ctx, sx + 2, sy + 14, searchW - 4, 1, this.c.accent);
            }

            softLine(ctx, sx + 5, sy + 4, 6, 6, this.c.textDim);
            ctx.fill(sx + 10, sy + 10, sx + 12, sy + 12, this.c.textDim);
            int textX = sx + 16;
            int textW = searchW - 20;
            if (this.query.isEmpty() && !this.searchFocused) {
               this.text(ctx, this.fitText("Search", textW), textX, sy + 4, this.c.textDim);
            } else {
               boolean blink = this.searchFocused && System.currentTimeMillis() / 500L % 2L == 0L;
               String shown = this.query;

               while (!shown.isEmpty() && this.textWidth(shown + "_") > textW) {
                  shown = shown.substring(1);
               }

               this.text(ctx, shown + (blink ? "_" : ""), textX, sy + 4, this.c.text);
            }

            this.hit(sx, sy, searchW, 16, (b, px, py) -> this.searchFocused = true);
         }

         ctx.fill(x, y + 36, x + w, y + 37, this.c.borderDim);
         this.drawGrid(
            ctx,
            x,
            y + (this.tab != BaulooStylesClient.Tab.INVENTORY && this.tab != BaulooStylesClient.Tab.SKY && !this.cobwebTab() ? 44 : 58),
            w,
            l.footerY - 10,
            mx,
            my
         );
      }
   }

   private void drawMyPacksMain(GuiGraphicsExtractor ctx, BaulooStylesConfigScreen.Layout l, int mx, int my) {
      if (vipView) {
         this.drawVipMain(ctx, l, mx, my);
      } else if (!MyPacks.shown()) {
         myPacksView = false;
         this.rebuild();
      } else {
         int x = l.mainX;
         int y = l.bodyY;
         int w = l.mainW;
         if (this.myPacksVersion != MyPacks.version()) {
            double keep = this.gridScroll;
            this.rebuild();
            this.gridScroll = keep;
         }

         this.pageTitle(ctx, "My Packs", x, y + 11);
         String state = MyPacks.scanning()
            ? "Scanning your packs  " + MyPacks.scanned() + " / " + MyPacks.total()
            : MyPacks.packCount() + " packs in your folder  -  " + MyPacks.styles().size() + " styles";
         this.text(ctx, this.fitText(state, w / 2), x, y + 24, MyPacks.scanning() ? this.c.accent : this.c.textDim);
         String label = MyPacks.scanning() ? "SCANNING ..." : "RESCAN";
         int bw = this.textWidth(label) + 16;
         int bx = x + w - bw;
         this.button(
            ctx,
            bx,
            y + 9,
            bw,
            16,
            label,
            inside(mx, my, bx, y + 9, bw, 16),
            MyPacks.scanning() ? BaulooStylesConfigScreen.ButtonStyle.DISABLED : BaulooStylesConfigScreen.ButtonStyle.NORMAL
         );
         this.hit(bx, y + 9, bw, 16, (b, px, py) -> MyPacks.rescan());
         int titleW = Math.max(this.titleWidth("My Packs"), this.textWidth(state));
         int searchW = Math.min(130, bx - 16 - (x + Math.min(titleW, w / 2) + 12));
         if (searchW >= 64) {
            int sx = bx - 12 - searchW;
            int sy = y + 9;
            boolean hover = inside(mx, my, sx, sy, searchW, 16);
            soft(ctx, sx, sy, searchW, 16, !hover && !this.searchFocused ? this.c.surface : this.c.surface2);
            if (this.searchFocused) {
               soft(ctx, sx + 2, sy + 14, searchW - 4, 1, this.c.accent);
            }

            softLine(ctx, sx + 5, sy + 4, 6, 6, this.c.textDim);
            ctx.fill(sx + 10, sy + 10, sx + 12, sy + 12, this.c.textDim);
            String shown = this.query.isEmpty() && !this.searchFocused ? "Search a pack" : this.query;
            this.text(ctx, this.fitText(shown, searchW - 20), sx + 16, sy + 4, this.query.isEmpty() ? this.c.textDim : this.c.text);
            this.hit(sx, sy, searchW, 16, (b, px, py) -> this.searchFocused = true);
         }

         ctx.fill(x, y + 36, x + w, y + 37, this.c.borderDim);
         if (this.entries.isEmpty()) {
            String why = MyPacks.scanning()
               ? "Looking through your resourcepacks folder ..."
               : "No styles found. Put texture packs (.zip) into your resourcepacks folder, then RESCAN.";
            this.text(ctx, this.fitText(why, w), x, y + 52, this.c.textDim);
         } else {
            this.drawGrid(ctx, x, y + 44, w, l.footerY - 10, mx, my);
         }
      }
   }

   private void drawVipMain(GuiGraphicsExtractor ctx, BaulooStylesConfigScreen.Layout l, int mx, int my) {
      if (!VipPacks.shown()) {
         myPacksView = false;
         vipView = false;
         this.rebuild();
      } else {
         int x = l.mainX;
         int y = l.bodyY;
         int w = l.mainW;
         if (this.vipVersion != VipPacks.version()) {
            double keep = this.gridScroll;
            this.rebuild();
            this.gridScroll = keep;
         }

         this.pageTitle(ctx, "Bauloo's Packs", x, y + 11);
         int tagW = this.scaledWidth("VIP", 0.75F) + 8;
         int tx = x + this.titleWidth("Bauloo's Packs") + 6;
         soft(ctx, tx, y + 10, tagW, 10, -15811);
         this.scaled(ctx, "VIP", tx + 4, y + 12, -15068416, 0.75F, false);
         String state = !VipPacks.status().isEmpty()
            ? VipPacks.status()
            : (VipPacks.loading() && VipPacks.styles().isEmpty() ? "Loading bauloo's packs ..." : VipPacks.styles().size() + " styles from bauloo's own packs");
         this.text(ctx, this.fitText(state, w / 2), x, y + 24, VipPacks.status().isEmpty() ? this.c.textDim : -15811);
         int titleW = Math.max(this.titleWidth("Bauloo's Packs") + tagW + 6, this.textWidth(state));
         int searchW = Math.min(150, x + w - 16 - (x + Math.min(titleW, w / 2) + 12));
         if (searchW >= 64) {
            int sx = x + w - searchW;
            int sy = y + 9;
            boolean hover = inside(mx, my, sx, sy, searchW, 16);
            soft(ctx, sx, sy, searchW, 16, !hover && !this.searchFocused ? this.c.surface : this.c.surface2);
            if (this.searchFocused) {
               soft(ctx, sx + 2, sy + 14, searchW - 4, 1, this.c.accent);
            }

            softLine(ctx, sx + 5, sy + 4, 6, 6, this.c.textDim);
            ctx.fill(sx + 10, sy + 10, sx + 12, sy + 12, this.c.textDim);
            String shown = this.query.isEmpty() && !this.searchFocused ? "Search a pack" : this.query;
            this.text(ctx, this.fitText(shown, searchW - 20), sx + 16, sy + 4, this.query.isEmpty() ? this.c.textDim : this.c.text);
            this.hit(sx, sy, searchW, 16, (b, px, py) -> this.searchFocused = true);
         }

         ctx.fill(x, y + 36, x + w, y + 37, this.c.borderDim);
         if (this.entries.isEmpty()) {
            this.text(ctx, this.fitText(VipPacks.loading() ? "Loading ..." : "No styles here yet - bauloo is still adding them", w), x, y + 52, this.c.textDim);
         } else {
            this.drawGrid(ctx, x, y + 44, w, l.footerY - 10, mx, my);
         }
      }
   }

   private void useMyPack(String folder, BaulooStylesClient.Tab target) {
      MyPacks.Style style = MyPacks.byId(folder.substring("mypack:".length()));
      if (style != null && !this.pendingMyPack) {
         this.pendingMyPack = true;
         Thread thread = new Thread(() -> {
            String id = MyPacks.use(style);
            Minecraft.getInstance().execute(() -> {
               this.pendingMyPack = false;
               if (id != null && Minecraft.getInstance().gui.screen() == this) {
                  this.applyAndRefresh(id, target);
               }
            });
         }, "Bauloo Styles my pack");
         thread.setDaemon(true);
         thread.start();
      }
   }

   private void drawGrid(GuiGraphicsExtractor ctx, int x, int top, int w, int bottom, int mx, int my) {
      this.gridLeft = x;
      this.gridRight = x + w;
      this.gridTop = top;
      this.gridBottom = bottom;
      int h = bottom - top;
      int count = this.visible.size();
      if (count == 0) {
         String msg = this.favoritesOnly && this.query.isEmpty() ? "No favorites yet - hover a style and press F" : "No styles match \"" + this.query + "\"";
         this.textCenter(ctx, this.fitText(msg, w), x, w, top + h / 2 - 4, this.c.textDim);
         this.gridMaxScroll = 0;
      } else {
         int labelH = 17;
         int baseCols = w >= 500 ? 4 : (w >= 290 ? 3 : 2);
         int cols = baseCols;
         int cardW = 0;
         int cardH = 0;
         boolean fits = false;

         for (int tryCols = Math.min(baseCols, count); tryCols <= 8; tryCols++) {
            int cw = (w - (tryCols - 1) * 8) / tryCols;
            int[][] spots = this.place(tryCols);
            int rows = spots[count - 1][0] + 1;
            int headersH = spots[count - 1][2] * 14;
            int fitH = Math.min(cw * 9 / 10, (h - headersH - (rows - 1) * 8) / rows - labelH);
            if (fitH >= 30 && fitH >= cw * 0.55F) {
               cols = tryCols;
               cardW = cw;
               cardH = fitH;
               fits = true;
               break;
            }
         }

         if (!fits) {
            cols = myPacksView ? Math.max(baseCols, Math.min(7, w / 56)) : baseCols;
            cardW = (w - (cols - 1) * 8) / cols;
            cardH = Math.max(34, myPacksView ? cardW * 7 / 10 : cardW * 3 / 4);
         }

         int step = cardH + labelH + 8;
         int[][] spots = this.place(cols);
         int rows = spots[count - 1][0] + 1;
         int contentH = rows * step - 8 + spots[count - 1][2] * 14;
         this.gridMaxScroll = Math.max(0, contentH - h);
         this.gridScroll = Math.max(0.0, Math.min(this.gridScroll, (double)this.gridMaxScroll));
         ctx.enableScissor(x - 2, top - 2, x + w + 2, bottom + 1);

         for (int k = 0; k < count; k++) {
            int index = this.visible.get(k);
            int cx = x + spots[k][1] * (cardW + 8);
            int cy = top + spots[k][0] * step + spots[k][2] * 14 - (int)this.gridScroll;
            if (spots[k][2] > 0 && (k == 0 || spots[k][2] != spots[k - 1][2])) {
               int hy = cy - 14 + 1;
               if (hy + 14 > top - 2 && hy < bottom) {
                  String title = switch (this.tabOf(index)) {
                     case SWORD -> "SWORDS";
                     case GAP -> "GAPS";
                     default -> "ANCHORS";
                     case CRYSTAL -> "CRYSTALS";
                     case GLOWSTONE -> "GLOWSTONE";
                     case ARMOR -> "ARMOR";
                     case PICKAXE -> "PICKAXES";
                     case AXE -> "AXES";
                     case TOTEM -> "TOTEMS";
                     case PEARL -> "PEARLS";
                     case HOTBAR -> "HOTBARS";
                     case FIRE -> "FIRE";
                     case SKY -> "SKIES";
                     case OBSIDIAN -> "OBSIDIAN";
                  };
                  this.text(ctx, title, x, hy, this.c.accent);
                  int lx = x + this.textWidth(title) + 6;
                  ctx.fill(lx, hy + 4, x + w, hy + 5, this.c.borderDim);
               }
            }

            if (cy + cardH + labelH >= top - 2 && cy <= bottom) {
               boolean hover = inside(mx, my, cx, cy, cardW, cardH + labelH) && my >= top && my < bottom;
               if (hover) {
                  this.hoveredEntry = index;
                  if (this.hoverTrack != index) {
                     this.hoverTrack = index;
                     this.hoverSince = System.currentTimeMillis();
                  }
               }

               this.drawCard(ctx, index, cx, cy, cardW, cardH, hover);
               this.hits.add(new BaulooStylesConfigScreen.Hit(cx, cy, cardW, cardH + labelH, top, bottom, (b, px, py) -> {
                  this.focusedIndex = index;
                  String folder = this.entries.get(index)[0];
                  if (b == 1) {
                     if (folder != null) {
                        this.zoomFolder = folder;
                        this.zoomName = this.entries.get(index)[1];
                     }
                  } else if (b == 0) {
                     this.choose(folder, this.tabOf(index));
                  }
               }));
            }
         }

         ctx.disableScissor();
         if (this.hoveredEntry < 0) {
            this.hoverTrack = -1;
         }

         if (this.gridMaxScroll > 0) {
            int trackX = x + w + 4;
            soft(ctx, trackX, top, 2, h, this.c.borderDim);
            int thumbH = Math.max(16, h * h / contentH);
            int thumbY = top + (int)((h - thumbH) * (this.gridScroll / this.gridMaxScroll));
            this.scrollBar(ctx, 1, trackX, top, h, thumbY, thumbH, mx, my);
         }
      }
   }

   private void scrollBar(GuiGraphicsExtractor ctx, int which, int trackX, int top, int h, int thumbY, int thumbH, int mx, int my) {
      boolean hover = this.scrollDrag == which || inside(mx, my, trackX - 4, top, 10, h);
      soft(ctx, hover ? trackX - 1 : trackX, thumbY, hover ? 4 : 2, thumbH, this.c.accent);
      this.hit(trackX - 4, top, 10, h, (b, px, py) -> {
         this.scrollDrag = which;
         this.scrollTop = top;
         this.scrollH = h;
         this.scrollThumbH = thumbH;
         this.scrollGrab = py >= thumbY && py < thumbY + thumbH ? py - thumbY : thumbH / 2.0;
         this.dragScroll(py);
      });
   }

   private void dragScroll(double mouseY) {
      double free = Math.max(1, this.scrollH - this.scrollThumbH);
      double t = Math.max(0.0, Math.min(1.0, (mouseY - this.scrollGrab - this.scrollTop) / free));
      if (this.scrollDrag == 1) {
         this.gridScroll = t * this.gridMaxScroll;
      } else if (this.scrollDrag == 2) {
         this.extrasScroll = t * this.extrasMaxScroll;
      }
   }

   private int[][] place(int cols) {
      boolean grouped = (myPacksView || groupOf(this.tab).length > 1) && !this.favoritesOnly && this.query.isBlank();
      int[][] out = new int[this.visible.size()][];
      int row = -1;
      int col = cols;
      int titles = 0;
      BaulooStylesClient.Tab last = null;

      for (int k = 0; k < this.visible.size(); k++) {
         BaulooStylesClient.Tab group = this.tabOf(this.visible.get(k));
         if (grouped && group != last) {
            row++;
            col = 0;
            titles++;
            last = group;
         } else if (col >= cols) {
            row++;
            col = 0;
         }

         out[k] = new int[]{row, col, titles};
         col++;
      }

      return out;
   }

   private int drawCobwebPreview(GuiGraphicsExtractor ctx, int px, int y, int pw) {
      int box = 76;
      soft(ctx, px, y, box, box, this.c.base);
      Minecraft mc = Minecraft.getInstance();
      TextureAtlasSprite sprite = mc.getAtlasManager().get(Sheets.BLOCKS_MAPPER.defaultNamespaceApply("cobweb"));
      int s = 64;
      int x0 = px + (box - s) / 2;
      int y0 = y + (box - s) / 2;
      ctx.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x0, y0, s, s);
      int t = Math.max(1, Math.round(s * StylesUiConfig.cobwebOutlineWidth / 64.0F));
      int color = (StylesUiConfig.cobwebOutline ? -268435456 : 1342177280) | CobwebOutline.color();
      ctx.fill(x0, y0, x0 + s, y0 + t, color);
      ctx.fill(x0, y0 + s - t, x0 + s, y0 + s, color);
      ctx.fill(x0, y0 + t, x0 + t, y0 + s - t, color);
      ctx.fill(x0 + s - t, y0 + t, x0 + s, y0 + s - t, color);
      int tx = px + box + 8;
      int tw = pw - box - 8;
      this.text(ctx, this.fitText("Color follows", tw), tx, y + 14, this.c.textDim);
      this.text(ctx, this.fitText("your cobweb style", tw), tx, y + 25, this.c.textDim);
      return y + box;
   }

   private static int skyColor(float ticks) {
      for (int i = 0; i < SKY_STOPS.length - 1; i++) {
         int a = SKY_STOPS[i][0];
         int b = SKY_STOPS[i + 1][0];
         if (ticks >= a && ticks <= b) {
            float f = (ticks - a) / (b - a);
            int ca = SKY_STOPS[i][1];
            int cb = SKY_STOPS[i + 1][1];
            int r = Math.round((ca >> 16 & 0xFF) + ((cb >> 16 & 0xFF) - (ca >> 16 & 0xFF)) * f);
            int g = Math.round((ca >> 8 & 0xFF) + ((cb >> 8 & 0xFF) - (ca >> 8 & 0xFF)) * f);
            int bl = Math.round((ca & 0xFF) + ((cb & 0xFF) - (ca & 0xFF)) * f);
            return 0xFF000000 | r << 16 | g << 8 | bl;
         }
      }

      return 0xFF000000 | SKY_STOPS[0][1];
   }

   private static String clock(int ticks) {
      int m = (int)((ticks / 1000.0 * 60.0 + 360.0) % 1440.0);
      return String.format(Locale.ROOT, "%02d:%02d", m / 60, m % 60);
   }

   private static int[] trackColors(int w) {
      int key = w * 31 + (CustomSky.hasSky() ? CustomSky.layersVersion() : -1);
      if (trackColors.length == w && trackKey == key) {
         return trackColors;
      } else {
         int[] out = new int[w];

         for (int i = 0; i < w; i++) {
            int ticks = Math.round((float)i / Math.max(1, w - 1) * 23999.0F);
            int base = skyColor(ticks);
            if (!CustomSky.hasSky()) {
               out[i] = base;
            } else {
               int[] px = CustomSky.preview(ticks, base);
               long r = 0L;
               long g = 0L;
               long b = 0L;

               for (int c : px) {
                  r += c >> 16 & 0xFF;
                  g += c >> 8 & 0xFF;
                  b += c & 0xFF;
               }

               out[i] = 0xFF000000 | (int)(r / px.length) << 16 | (int)(g / px.length) << 8 | (int)(b / px.length);
            }
         }

         trackColors = out;
         trackKey = key;
         return out;
      }
   }

   private void drawSkyPreview(GuiGraphicsExtractor ctx, int x, int y, int size, int ticks) {
      int t = ticks >= 0 ? ticks : (Minecraft.getInstance().level != null ? (int)(Minecraft.getInstance().level.getDefaultClockTime() % 24000L) : 6000);
      int key = CustomSky.hasSky() ? CustomSky.layersVersion() : -1;
      if (skyPreviewId == null || Math.abs(t - skyPreviewTicks) >= 100 || key != skyPreviewKey) {
         int n = 32;
         int[] px = CustomSky.hasSky() ? CustomSky.preview(t, skyColor(t)) : null;
         NativeImage image = new NativeImage(n, n, false);

         for (int py = 0; py < n; py++) {
            for (int pxx = 0; pxx < n; pxx++) {
               image.setPixel(pxx, py, px != null ? px[py * n + pxx] : skyColor(t));
            }
         }

         Identifier id = Identifier.fromNamespaceAndPath("bauloo_styles", "sky_time_preview");
         Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(id::toString, image));
         skyPreviewId = id;
         skyPreviewTicks = t;
         skyPreviewKey = key;
      }

      ctx.blit(RenderPipelines.GUI_TEXTURED, skyPreviewId, x, y, 0.0F, 0.0F, size, size, size, size);
      softLine(ctx, x - 1, y - 1, size + 2, size + 2, this.c.borderDim);
   }

   private void skyTrack(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int ticks) {
      int[] colors = trackColors(w);

      for (int i = 0; i < w; i++) {
         ctx.fill(x + i, y, x + i + 1, y + h, colors[i]);
      }

      if (ticks >= 0) {
         int kx = x + Math.round(ticks / 24000.0F * (w - 1));
         soft(ctx, kx - 2, y - 2, 5, h + 4, -16777216);
         ctx.fill(kx - 1, y - 1, kx + 1, y + h + 1, -1);
      }
   }

   private void drawTimeChanger(GuiGraphicsExtractor ctx, int x, int y, int mx, int my) {
      this.text(ctx, "TIME", x, y + 4, this.c.textDim);
      int tx = x + this.textWidth("TIME") + 8;
      int bw = this.textWidth("SERVER") + 14;
      int labelW = this.textWidth("Server time");
      int tw = Math.max(60, Math.min(160, this.layout().mainW - (tx - x) - labelW - bw - 24));
      int ticks = StylesUiConfig.timeOverride;
      this.skyTrack(ctx, tx, y + 5, tw, 6, ticks);
      this.hit(tx, y, tw, 16, (b, x2, y2) -> {
         this.drag = BaulooStylesConfigScreen.Drag.TIME;
         this.dragW = Math.min(260, this.width - 80);
         this.dragX = (this.width - this.dragW) / 2;
         this.dragY = this.height - 90;
         float start = (float)Math.max(0.0, Math.min(1.0, (x2 - tx) / Math.max(1, tw - 1)));
         StylesUiConfig.timeOverride = Math.round(start * 23999.0F);
         this.dragStartMouse = this.dragX + start * (this.dragW - 1);
         this.dragStartT = start;
      });
      this.text(ctx, ticks >= 0 ? clock(ticks) : "Server time", tx + tw + 8, y + 4, ticks >= 0 ? this.c.text : this.c.textDim);
      int bx = tx + tw + 8 + labelW + 10;
      boolean on = ticks < 0;
      boolean hover = inside(mx, my, bx, y, bw, 16);
      soft(ctx, bx, y, bw, 16, on ? this.c.accent : (hover ? this.c.textDim : this.c.borderDim));
      soft(ctx, bx + 1, y + 1, bw - 2, 14, on ? StylesTheme.withAlpha(this.c.accentDark, 255) : (hover ? this.c.surfaceHover : this.c.surface2));
      this.text(ctx, "SERVER", bx + 7, y + 4, on ? this.c.text : (hover ? this.c.textSoft : this.c.textDim));
      this.hit(bx, y, bw, 16, (b, px, py) -> {
         StylesUiConfig.timeOverride = -1;
         StylesUiConfig.save();
      });
      this.drawSkyPreview(ctx, bx + bw + 10, y - 8, 30, ticks);
      if (hover) {
         this.tooltip = "The real time of the server";
         this.tooltipX = mx + 8;
         this.tooltipY = my + 10;
      }
   }

   private void drawLiveTime(GuiGraphicsExtractor ctx) {
      this.thirdPerson(false);
      HandPreview.clear();
      int w = this.dragW;
      int x = this.dragX;
      int y = this.dragY;
      int ticks = Math.max(0, StylesUiConfig.timeOverride);
      soft(ctx, x - 8, y - 22, w + 16, 40, StylesTheme.withAlpha(this.c.base, 208));
      this.text(ctx, "TIME OF DAY", x, y - 14, this.c.textDim);
      this.textRight(ctx, clock(ticks), x + w, y - 14, this.c.text);
      this.skyTrack(ctx, x, y, w, 8, ticks);
   }

   private boolean cobwebTab() {
      return "cobweb".equals(this.tab.prefix);
   }

   private void headerToggle(GuiGraphicsExtractor ctx, int x, int y, String label, boolean on, int mx, int my, String tip, Runnable flip) {
      int w = this.textWidth(label) + 12 + 22;
      int h = 16;
      boolean hover = inside(mx, my, x, y, w, h);
      soft(ctx, x, y, w, h, on ? this.c.accent : (hover ? this.c.textDim : this.c.borderDim));
      soft(ctx, x + 1, y + 1, w - 2, h - 2, on ? StylesTheme.withAlpha(this.c.accentDark, 255) : (hover ? this.c.surfaceHover : this.c.surface2));
      this.text(ctx, label, x + 6, y + 4, on ? this.c.text : (hover ? this.c.textSoft : this.c.textDim));
      int sx = x + w - 22;
      soft(ctx, sx, y + 4, 16, 8, on ? this.c.accent : this.c.base);
      soft(ctx, on ? sx + 9 : sx + 1, y + 5, 6, 6, on ? this.c.onAccent : (hover ? this.c.text : this.c.textDim));
      this.hit(x, y, w, h, (b, px, py) -> {
         flip.run();
         StylesUiConfig.save();
      });
      if (hover) {
         this.tooltip = tip;
         this.tooltipX = mx + 8;
         this.tooltipY = my + 10;
      }
   }

   private void drawCard(GuiGraphicsExtractor ctx, int index, int cx, int cy, int cardW, int cardH, boolean hover) {
      this.drawGlassCard(ctx, index, cx, cy, cardW, cardH, hover);
   }

   private void drawGlassPanel(GuiGraphicsExtractor ctx, BaulooStylesConfigScreen.Layout l) {
      for (int s = 3; s >= 1; s--) {
         rounded(ctx, l.fx - s * 2, l.fy - s * 2 + 3, l.fw + s * 4, l.fh + s * 4, PANEL, 301989888);
      }

      rounded(ctx, l.fx, l.fy, l.fw, l.fh, PANEL, this.c.frame);
      int edge = 385875967;
      ctx.fill(l.fx + 5, l.fy, l.fx + l.fw - 5, l.fy + 1, edge);
      ctx.fill(l.fx + 5, l.fy + l.fh - 1, l.fx + l.fw - 5, l.fy + l.fh, edge);
      ctx.fill(l.fx, l.fy + 5, l.fx + 1, l.fy + l.fh - 5, edge);
      ctx.fill(l.fx + l.fw - 1, l.fy + 5, l.fx + l.fw, l.fy + l.fh - 5, edge);
   }

   private void drawGlassCard(GuiGraphicsExtractor ctx, int index, int cx, int cy, int cardW, int cardH, boolean hover) {
      String[] e = this.entries.get(index);
      String folder = e[0];
      boolean isActive = this.isActive(index);
      boolean upload = "upload".equals(e[2]);
      boolean marked = this.isPending(index);
      int fullH = cardH + 17;
      if (isActive || marked) {
         rounded(ctx, cx - 1, cy - 1, cardW + 2, fullH + 2, CARD, marked ? -15811 : this.c.accent);
      }

      int box = hover ? this.c.surfaceHover : this.c.surface;
      rounded(ctx, cx, cy, cardW, fullH, CARD, box);
      int px = cx + 3;
      int py = cy + 3;
      int pw = cardW - 6;
      int ph = cardH - 3;
      if (folder == null) {
         soft(ctx, px, py, pw, ph, this.c.surface2);
         float k = Math.max(1.0F, Math.min(3.0F, (ph - 10) / 16.0F));
         Matrix3x2fStack m = ctx.pose();
         m.pushMatrix();
         m.translate(px + pw / 2.0F - 8.0F * k, py + ph / 2.0F - 8.0F * k);
         m.scale(k, k);
         itemIcon(ctx, iconItem(this.tabOf(index)), 0, 0);
         m.popMatrix();
      } else {
         if (this.clipPlaying(index)) {
            this.drawClip(ctx, folder, px, py, pw, ph);
         } else {
            this.drawPreview(ctx, folder, px, py, pw, ph);
         }

         if (hover) {
            ctx.fill(px, py, px + pw, py + ph, 318767103);
         }

         roundCut(ctx, px, py, pw, ph, SMALL, box);
      }

      String tag = isActive ? "EQUIPPED" : null;
      int tagFill = this.c.accent;
      int tagText = this.c.onAccent;
      StylesCloud.Style cloud = StylesCloud.byFolder(folder);
      if (tag == null && ph >= 30 && upload) {
         tag = "LOCAL";
         tagFill = -1073741824;
         tagText = this.c.textSoft;
      } else if (tag == null && ph >= 30 && cloud != null && cloud.isNew()) {
         tag = "NEW";
         tagFill = -251658241;
         tagText = -15921646;
      }

      if (tag != null) {
         int tw = this.scaledWidth(tag, 0.7F) + 8;
         soft(ctx, px + 3, py + 3, tw, 10, tagFill);
         this.scaled(ctx, tag, px + 7, py + 5.5F, tagText, 0.7F, false);
      }

      if (marked) {
         int tw = this.scaledWidth("SELECTED", 0.7F) + 8;
         soft(ctx, px + pw - 3 - tw, py + 3, tw, 10, -15811);
         this.scaled(ctx, "SELECTED", px + pw - tw + 1, py + 5.5F, -15068416, 0.7F, false);
      }

      if (cloud != null && (StylesCloud.isDownloading(cloud) || folder.equals(this.pendingCloud))) {
         soft(ctx, px, py, pw, ph, StylesTheme.withAlpha(this.c.base, 176));
         String dots = ".".repeat((int)(System.currentTimeMillis() / 400L % 4L));
         this.textCenter(ctx, "Downloading" + dots, px, pw, py + ph / 2 - 4, this.c.accent);
      }

      boolean fav = folder != null && StylesUiConfig.isFavorite(folder);
      int nameW = cardW - 12 - (fav ? 10 : 0);
      this.text(ctx, this.fitText(e[1], nameW), cx + 6, cy + cardH + 5, marked ? -15811 : (isActive ? this.c.text : (hover ? this.c.text : this.c.textSoft)));
      if (fav) {
         this.text(ctx, "★", cx + cardW - 12, cy + cardH + 5, -12454);
      }
   }

   private void drawSide(GuiGraphicsExtractor ctx, BaulooStylesConfigScreen.Layout l, int mx, int my) {
      int x = l.sideX;
      int y = l.bodyY + 8;
      int w = l.sideW;
      int bottom = l.footerY - 10;
      soft(ctx, x, y, w, bottom - y, this.c.surface);
      int px = x + 10;
      int pw = w - 20;
      int ty = y + 10;
      String[] labels = new String[]{"INSPECTOR", "THEMES"};
      int gap = 14;
      int tx = px;

      for (int i = 0; i < labels.length; i++) {
         boolean on = !presetsView && i == 1 == StylesUiConfig.themesTab;
         int tw = this.textWidth(labels[i]);
         boolean hover = inside(mx, my, tx - 3, ty - 3, tw + 6, 16);
         this.text(ctx, labels[i], tx, ty, on ? this.c.text : (hover ? this.c.textSoft : this.c.textDim));
         if (on) {
            soft(ctx, tx, ty + 11, tw, 2, this.c.accent);
         }

         int index = i;
         this.hit(tx - 3, ty - 3, tw + 6, 16, (b, px2, py2) -> {
            presetsView = false;
            this.presetNaming = false;
            StylesUiConfig.themesTab = index == 1;
            this.hexFocused = false;
            StylesUiConfig.save();
         });
         tx += tw + gap;
      }

      ctx.fill(px, ty + 15, px + pw, ty + 16, this.c.borderDim);
      int contentTop = ty + 23;
      if (presetsView) {
         this.drawPresets(ctx, px, contentTop, pw, bottom - 8, mx, my);
      } else if (StylesUiConfig.themesTab) {
         this.drawThemes(ctx, px, contentTop, pw, bottom - 8, mx, my, this.c.surface);
      } else {
         this.drawInspector(ctx, px, contentTop, pw, bottom - 10, mx, my);
      }
   }

   private void drawInspector(GuiGraphicsExtractor ctx, int px, int top, int pw, int bottom, int mx, int my) {
      if (this.entries.isEmpty()) {
         String why = myPacksView && MyPacks.scanning() ? "Scanning your packs ..." : "Nothing to show yet";
         this.text(ctx, this.fitText(why, pw), px, top + 4, this.c.textDim);
      } else {
         int shown = this.hoveredEntry >= 0 ? this.hoveredEntry : Math.min(this.focusedIndex, this.entries.size() - 1);
         if (shown < 0 || shown >= this.entries.size()) {
            shown = 0;
         }

         String[] e = this.entries.get(shown);
         String folder = e[0];
         boolean isActive = this.isActive(shown);
         boolean upload = "upload".equals(e[2]);
         int smallY = bottom - 16;
         int applyY = smallY - 21;
         int infoH = 44;
         int size = Math.max(24, Math.min(pw, applyY - 10 - top - infoH));
         int previewX = px + (pw - size) / 2;
         if (folder == null) {
            soft(ctx, previewX, top, size, size, this.c.surface2);
            itemIcon(ctx, iconItem(this.tabOf(shown)), previewX + (size - 16) / 2, top + size / 2 - 14);
            this.textCenter(ctx, e[1], previewX, size, top + size / 2 + 6, this.c.textDim);
         } else {
            if (this.clipPlaying(shown)) {
               this.drawClip(ctx, folder, previewX, top, size, size);
            } else {
               this.drawPreview(ctx, folder, previewX, top, size, size);
            }

            cutCorners(ctx, previewX, top, size, size, this.c.surface);
         }

         if (isActive) {
            softLine(ctx, previewX - 1, top - 1, size + 2, size + 2, this.c.accent);
         }

         int iy = top + size + 8;
         if (isActive) {
            int pillW = this.textWidth("ACTIVE") + 10;
            soft(ctx, px, iy, pillW, 12, StylesTheme.withAlpha(-9183056, 51));
            this.text(ctx, "ACTIVE", px + 5, iy + 2, -9183056);
            this.text(ctx, this.fitText(e[1], pw - pillW - 6), px + pillW + 6, iy + 2, this.c.text);
         } else {
            this.text(ctx, this.fitText(e[1], pw), px, iy + 2, this.c.text);
         }

         MyPacks.Style mine = folder != null && folder.startsWith("mypack:") ? MyPacks.byId(folder.substring("mypack:".length())) : null;
         VipPacks.Style vip = folder != null && folder.startsWith("vippack:") ? VipPacks.byId(folder.substring("vippack:".length())) : null;
         String kind = folder == null
            ? "Default look"
            : (
               mine != null
                  ? "From your pack: " + mine.pack()
                  : (
                     vip != null
                        ? "Bauloo's Packs - VIP"
                        : (upload ? "Local upload" : ("cloud".equals(e[2]) ? "By bauloo - cloud" : this.tab.title + " style"))
                  )
            );
         this.text(ctx, this.fitText(kind, pw), px, iy + 16, this.c.textDim);
         if (!isActive) {
            String now = mine == null && vip == null
               ? "Now: " + this.activeName(this.tab)
               : "Only the " + this.tabOf(shown).title.toLowerCase(Locale.ROOT) + " part is used";
            this.text(ctx, this.fitText(now, pw), px, iy + 28, this.c.textDim);
         }

         if (this.tab == BaulooStylesClient.Tab.SKY && folder != null) {
         }

         boolean skyLocked = false;
         if (isActive) {
            this.textCenter(ctx, this.fitText("Left-click a style to apply", pw), px, pw, applyY + 5, this.c.textDim);
         } else if (skyLocked) {
            this.button(ctx, px, applyY, pw, 17, "Requires Skyboxify", false, BaulooStylesConfigScreen.ButtonStyle.DISABLED);
         } else {
            boolean hover = inside(mx, my, px, applyY, pw, 17);
            String label = folder == null ? "USE VANILLA" : "APPLY";
            if (!StylesUiConfig.applyInstantly) {
               label = this.isPending(shown) ? "SELECTED" : (folder == null ? "SELECT VANILLA" : "SELECT");
            }

            this.button(ctx, px, applyY, pw, 17, label, hover, BaulooStylesConfigScreen.ButtonStyle.PRIMARY);
            BaulooStylesClient.Tab targetTab = this.tabOf(shown);
            this.hit(px, applyY, pw, 17, (b, x2, y2) -> this.choose(folder, targetTab));
         }

         int halfW = (pw - 4) / 2;
         int favX = px + halfW + 4;
         int favW = pw - halfW - 4;
         if (folder == null) {
            this.button(ctx, px, smallY, halfW, 16, "ZOOM", false, BaulooStylesConfigScreen.ButtonStyle.DISABLED);
            this.button(ctx, favX, smallY, favW, 16, "FAVORITE", false, BaulooStylesConfigScreen.ButtonStyle.DISABLED);
         } else {
            boolean zoomHover = inside(mx, my, px, smallY, halfW, 16);
            this.button(ctx, px, smallY, halfW, 16, "ZOOM", zoomHover, BaulooStylesConfigScreen.ButtonStyle.NORMAL);
            String targetName = e[1];
            this.hit(px, smallY, halfW, 16, (b, x2, y2) -> {
               this.zoomFolder = folder;
               this.zoomName = targetName;
            });
            boolean fav = StylesUiConfig.isFavorite(folder);
            boolean favHover = inside(mx, my, favX, smallY, favW, 16);
            this.button(ctx, favX, smallY, favW, 16, "", favHover, BaulooStylesConfigScreen.ButtonStyle.NORMAL);
            String label = this.fitText(fav ? "SAVED" : "FAVORITE", favW - 18);
            int lw = this.textWidth("★ ") + this.textWidth(label);
            int lx = favX + (favW - lw) / 2;
            this.text(ctx, "★", lx, smallY + 4, fav ? -12454 : this.c.textDim);
            this.text(ctx, label, lx + this.textWidth("★ "), smallY + 4, fav ? -12454 : (favHover ? this.c.text : this.c.textSoft));
            this.hit(favX, smallY, favW, 16, (b, x2, y2) -> this.toggleFavorite(folder));
         }
      }
   }

   private boolean want(String key) {
      return this.extraOnly == null || this.extraOnly.equals(key);
   }

   private static List<BaulooStylesConfigScreen.ExtraTile> extraTiles() {
      List<BaulooStylesConfigScreen.ExtraTile> list = new ArrayList<>();
      list.add(new BaulooStylesConfigScreen.ExtraTile("OUTLINE", "Player Outline", "Combat", Items.ARMOR_STAND, "Glow around players"));
      list.add(new BaulooStylesConfigScreen.ExtraTile("CRYSTAL", "Crystal", "Combat", Items.END_CRYSTAL, "Spin speed and core color"));
      list.add(new BaulooStylesConfigScreen.ExtraTile("TOTEM", "Totem", "Combat", Items.TOTEM_OF_UNDYING, "Size in hand and pop"));
      list.add(new BaulooStylesConfigScreen.ExtraTile("SHIELD", "Shield", "Combat", Items.SHIELD, "See-through, size, color"));
      list.add(new BaulooStylesConfigScreen.ExtraTile("GAP", "Gap", "Combat", Items.GOLDEN_APPLE, "Size in hand"));
      list.add(new BaulooStylesConfigScreen.ExtraTile("ANCHOR", "Anchor Charge", "Combat", Items.RESPAWN_ANCHOR, "Glow color, look full"));
      list.add(new BaulooStylesConfigScreen.ExtraTile("KILL", "Kill Effect", "Combat", Items.NETHERITE_SWORD, "Effect and kill sound"));
      list.add(new BaulooStylesConfigScreen.ExtraTile("HITBOX", "Hitbox", "Combat", Items.TARGET, "F3+B box color"));
      list.add(new BaulooStylesConfigScreen.ExtraTile("CRYSTALBOX", "Crystal Box", "Combat", Items.GLASS, "Box of invisible crystals"));
      list.add(new BaulooStylesConfigScreen.ExtraTile("COBWEB", "Cobweb", "World", Items.COBWEB, "Outline around cobwebs"));
      list.add(new BaulooStylesConfigScreen.ExtraTile("FIRE", "Fire", "World", Items.BLAZE_POWDER, "Own fire color"));
      list.add(new BaulooStylesConfigScreen.ExtraTile("BLOCK", "Block Outline", "World", Items.GRASS_BLOCK, "Color and thickness"));
      list.add(new BaulooStylesConfigScreen.ExtraTile("CROSSHAIR", "Crosshair", "World", Items.SPYGLASS, "Color and gradient"));
      list.add(new BaulooStylesConfigScreen.ExtraTile("GLINT", "Enchant Glint", "World", Items.EXPERIENCE_BOTTLE, "Color, gradient, rainbow"));
      list.add(new BaulooStylesConfigScreen.ExtraTile("SOCIAL", "Social Badge", "HUD & Menu", Items.NAME_TAG, "Logo on the recipe book button"));
      list.add(new BaulooStylesConfigScreen.ExtraTile("TABICON", "Tab Icon", "HUD & Menu", Items.WRITABLE_BOOK, "Tornado next to your name"));
      list.add(new BaulooStylesConfigScreen.ExtraTile("MENU", "Menu", "HUD & Menu", Items.COMPARATOR, "Apply instantly or all at once"));
      return list;
   }

   private static Boolean extraOn(String key) {
      return switch (key) {
         case "OUTLINE" -> StylesUiConfig.outline;
         case "FIRE" -> pickOn(7);
         case "COBWEB" -> StylesUiConfig.cobwebOutline;
         case "HITBOX" -> pickOn(0);
         case "CRYSTALBOX" -> pickOn(2);
         case "ANCHOR" -> pickOn(3);
         case "CROSSHAIR" -> pickOn(1);
         case "BLOCK" -> pickOn(9);
         case "GLINT" -> pickOn(6);
         case "SPOTIFY" -> StylesUiConfig.spotifyHud;
         case "TABICON" -> StylesUiConfig.showInTab;
         case "MENU" -> StylesUiConfig.applyInstantly;
         default -> null;
      };
   }

   private void toggleExtra(String key) {
      switch (key) {
         case "OUTLINE":
            StylesUiConfig.outline = !StylesUiConfig.outline;
            break;
         case "FIRE":
            setPick(7, !pickOn(7), null);
            break;
         case "COBWEB":
            StylesUiConfig.cobwebOutline = !StylesUiConfig.cobwebOutline;
            break;
         case "HITBOX":
            setPick(0, !pickOn(0), null);
            break;
         case "CRYSTALBOX":
            setPick(2, !pickOn(2), null);
            break;
         case "ANCHOR":
            setPick(3, !pickOn(3), null);
            break;
         case "CROSSHAIR":
            setPick(1, !pickOn(1), null);
            break;
         case "BLOCK":
            setPick(9, !pickOn(9), null);
            break;
         case "GLINT":
            setPick(6, !pickOn(6), null);
            break;
         case "SPOTIFY":
            StylesUiConfig.spotifyHud = !StylesUiConfig.spotifyHud;
            break;
         case "TABICON":
            StylesUiConfig.showInTab = !StylesUiConfig.showInTab;
            StylesPresence.settingChanged();
            break;
         case "MENU":
            StylesUiConfig.applyInstantly = !StylesUiConfig.applyInstantly;
            PENDING.clear();
            break;
         default:
            return;
      }

      StylesUiConfig.save();
   }

   private static String percent(int value) {
      return value == 100 ? "Normal" : value + "%";
   }

   private static String extraValue(String key) {
      return switch (key) {
         case "CRYSTAL" -> StylesUiConfig.crystalSpin == 0 ? "Stopped" : percent(StylesUiConfig.crystalSpin);
         case "TOTEM" -> percent(StylesUiConfig.totemSize);
         case "SHIELD" -> StylesUiConfig.shieldOpacity >= 100 ? "Normal" : StylesUiConfig.shieldOpacity + "%";
         case "GAP" -> percent(StylesUiConfig.gapSize);
         case "SOCIAL" -> Watermark.validSocial(StylesUiConfig.socialLogo) ? "On" : "Off";
         case "KILL" -> {
            for (String[] effect : KillEffect.EFFECTS) {
               if (effect[0].equals(StylesUiConfig.killEffect)) {
                  yield effect[1];
               }
            }

            yield "Off";
         }
         default -> "";
      };
   }

   private void drawExtrasTiles(GuiGraphicsExtractor ctx, BaulooStylesConfigScreen.Layout l, int mx, int my) {
      int x = l.mainX;
      int y = l.bodyY;
      int full = l.fx + l.fw - 10 - x;
      boolean split = full >= 430;
      int setW = split ? clamp(full * 36 / 100, 190, 260) : full;
      int gridW = split ? full - setW - 12 : full;
      List<BaulooStylesConfigScreen.ExtraTile> all = extraTiles();
      BaulooStylesConfigScreen.ExtraTile picked = all.get(0);

      for (BaulooStylesConfigScreen.ExtraTile t : all) {
         if (t.key().equals(extraKey)) {
            picked = t;
         }
      }

      extraKey = picked.key();
      if (!split && extraOpen) {
         this.drawExtraSettings(ctx, picked, x, y + 8, full, l.footerY - 10, mx, my, true);
      } else {
         this.pageTitle(ctx, "Extras", x, y + 11);
         this.text(ctx, this.fitText("Click a tile to change it - the switch turns it on or off", gridW), x, y + 24, this.c.textDim);
         int chipX = x;
         int chipY = y + 38;

         for (int i = 0; i < EXTRA_GROUPS.length; i++) {
            String group = EXTRA_GROUPS[i];
            int n = 0;

            for (BaulooStylesConfigScreen.ExtraTile tx : all) {
               if (i == 0 || tx.group().equals(group)) {
                  n++;
               }
            }

            String count = String.valueOf(n);
            int cw = this.textWidth(group) + this.textWidth(count) + 18;
            boolean on = extraGroup == i;
            boolean hover = inside(mx, my, chipX, chipY, cw, 14);
            soft(ctx, chipX, chipY, cw, 14, on ? this.c.text : (hover ? this.c.surfaceHover : this.c.surface));
            ctx.text(this.font, group, chipX + 7, chipY + 3, on ? this.c.base : (hover ? this.c.text : this.c.textSoft), false);
            ctx.text(this.font, count, chipX + 11 + this.textWidth(group), chipY + 3, on ? this.c.textDim : this.c.textDim, false);
            this.hit(chipX, chipY, cw, 14, (b, px, py) -> {
               extraGroup = i;
               this.tileScroll = 0.0;
            });
            chipX += cw + 5;
         }

         List<BaulooStylesConfigScreen.ExtraTile> shown = new ArrayList<>();

         for (BaulooStylesConfigScreen.ExtraTile txx : all) {
            if (extraGroup == 0 || txx.group().equals(EXTRA_GROUPS[extraGroup])) {
               shown.add(txx);
            }
         }

         this.tileTop = y + 60;
         this.tileBottom = l.footerY - 8;
         this.tileLeft = x;
         this.tileRight = x + gridW;
         int gap = 7;
         int cols = Math.max(1, (gridW + gap) / 124);
         int tw = (gridW - (cols - 1) * gap) / cols;
         int th = 46;
         int rows = (shown.size() + cols - 1) / cols;
         int viewH = this.tileBottom - this.tileTop;
         this.tileMaxScroll = Math.max(0, rows * (th + gap) - gap + 2 - viewH);
         this.tileScroll = Math.max(0.0, Math.min(this.tileScroll, (double)this.tileMaxScroll));
         ctx.enableScissor(this.tileLeft - 2, this.tileTop - 2, this.tileRight + 2, this.tileBottom);
         this.hitClipTop = this.tileTop;
         this.hitClipBottom = this.tileBottom;

         try {
            for (int k = 0; k < shown.size(); k++) {
               int txxx = x + k % cols * (tw + gap);
               int ty = this.tileTop + 1 + k / cols * (th + gap) - (int)this.tileScroll;
               if (ty + th >= this.tileTop && ty <= this.tileBottom) {
                  this.drawExtraTile(ctx, shown.get(k), txxx, ty, tw, th, mx, my);
               }
            }
         } finally {
            this.hitClipTop = Integer.MIN_VALUE;
            this.hitClipBottom = Integer.MAX_VALUE;
            ctx.disableScissor();
         }

         if (this.tileMaxScroll > 0) {
            int thumbH = Math.max(16, viewH * viewH / (viewH + this.tileMaxScroll));
            int thumbY = this.tileTop + (int)((viewH - thumbH) * (this.tileScroll / this.tileMaxScroll));
            soft(ctx, this.tileRight + 4, thumbY, 2, thumbH, this.c.accent);
         }

         if (split) {
            this.drawExtraSettings(ctx, picked, x + gridW + 12, y + 8, setW, l.footerY - 10, mx, my, false);
         }
      }
   }

   private void drawExtraTile(GuiGraphicsExtractor ctx, BaulooStylesConfigScreen.ExtraTile t, int tx, int ty, int tw, int th, int mx, int my) {
      boolean selected = t.key().equals(extraKey);
      boolean hover = inside(mx, my, tx, ty, tw, th) && my >= this.tileTop && my < this.tileBottom;
      if (selected) {
         rounded(ctx, tx - 1, ty - 1, tw + 2, th + 2, CARD, this.c.accent);
      }

      rounded(ctx, tx, ty, tw, th, CARD, selected ? this.c.surface2 : (hover ? this.c.surfaceHover : this.c.surface));
      soft(ctx, tx + 5, ty + 5, 22, 22, this.c.base);
      ctx.item(stackOf(t.icon()), tx + 8, ty + 8);
      int textX = tx + 32;
      this.text(ctx, this.fitText(t.name(), tw - 36), textX, ty + 7, !selected && !hover ? this.c.textSoft : this.c.text);
      this.scaled(ctx, this.fitText(t.desc(), Math.round((tw - 36) / 0.75F)), textX, ty + 18, this.c.textDim, 0.75F, false);
      this.scaled(ctx, t.group().toUpperCase(Locale.ROOT), tx + 6, ty + th - 10, this.c.textDim, 0.65F, false);
      String key = t.key();
      this.hit(tx, ty, tw, th, (b, px, py) -> {
         if (!key.equals(extraKey)) {
            this.extrasScroll = 0.0;
         }

         extraKey = key;
         extraOpen = true;
      });
      Boolean on = extraOn(key);
      if (on != null) {
         int swW = 20;
         int swH = 10;
         int sx = tx + tw - swW - 6;
         int sy = ty + th - swH - 5;
         soft(ctx, sx, sy, swW, swH, on ? this.c.accentDark : this.c.base);
         softLine(ctx, sx, sy, swW, swH, on ? this.c.accent : this.c.borderDim);
         soft(ctx, on ? sx + swW - 8 : sx + 2, sy + 2, 6, 6, on ? this.c.accent : this.c.textDim);
         this.hit(sx - 3, sy - 3, swW + 6, swH + 6, (b, px, py) -> this.toggleExtra(key));
      } else {
         String value = extraValue(key);
         boolean changed = !value.equals("Normal") && !value.equals("Off");
         int vw = this.scaledWidth(value, 0.75F) + 8;
         int vx = tx + tw - vw - 6;
         int vy = ty + th - 14;
         soft(ctx, vx, vy, vw, 10, changed ? this.c.accentTint : this.c.base);
         this.scaled(ctx, value, vx + 4, vy + 2.5F, changed ? this.c.accentBright : this.c.textSoft, 0.75F, false);
      }
   }

   private void drawExtraSettings(
      GuiGraphicsExtractor ctx, BaulooStylesConfigScreen.ExtraTile t, int x, int top, int w, int bottom, int mx, int my, boolean back
   ) {
      soft(ctx, x, top, w, bottom - top, this.c.surface);
      int px = x + 10;
      int pw = w - 20;
      int y = top + 9;
      this.scaled(ctx, "EXTRAS  >  " + t.group().toUpperCase(Locale.ROOT), px, y, this.c.textDim, 0.7F, false);
      if (back) {
         int bw = this.textWidth("BACK") + 14;
         int bx = px + pw - bw;
         this.button(ctx, bx, y - 3, bw, 14, "BACK", inside(mx, my, bx, y - 3, bw, 14), BaulooStylesConfigScreen.ButtonStyle.NORMAL);
         this.hit(bx, y - 3, bw, 14, (b, px2, py2) -> extraOpen = false);
      }

      y += 10;
      this.scaled(ctx, this.fitText(t.name(), Math.round((pw - 28) / 1.4F)), px, y, this.c.text, 1.4F, true);
      Boolean on = extraOn(t.key());
      if (on != null && !back) {
         int swW = 22;
         int swH = 11;
         int sx = px + pw - swW;
         soft(ctx, sx, y, swW, swH, on ? this.c.accentDark : this.c.base);
         softLine(ctx, sx, y, swW, swH, on ? this.c.accent : this.c.borderDim);
         soft(ctx, on ? sx + swW - 9 : sx + 2, y + 2, 7, 7, on ? this.c.accent : this.c.textDim);
         String key = t.key();
         this.hit(sx - 3, y - 3, swW + 6, swH + 6, (b, px2, py2) -> this.toggleExtra(key));
      }

      y += 15;
      this.scaled(ctx, this.fitText(t.desc(), Math.round(pw / 0.8F)), px, y, this.c.textDim, 0.8F, false);
      y += 11;
      ctx.fill(px, y, px + pw, y + 1, this.c.borderDim);
      y += 4;
      this.extrasTop = y;
      this.extrasBottom = bottom - 4;
      this.extrasLeft = x;
      this.extrasRight = x + w;
      int viewH = this.extrasBottom - this.extrasTop;
      int start = this.extrasTop + 6 - (int)this.extrasScroll;
      ctx.enableScissor(this.extrasLeft, this.extrasTop, this.extrasRight, this.extrasBottom);
      this.hitClipTop = this.extrasTop;
      this.hitClipBottom = this.extrasBottom;
      this.extraOnly = t.key();

      int end;
      try {
         end = this.drawOutline(ctx, px, start, pw, 1073741823, mx, my, 0, 0);
      } finally {
         this.extraOnly = null;
         this.hitClipTop = Integer.MIN_VALUE;
         this.hitClipBottom = Integer.MAX_VALUE;
         ctx.disableScissor();
      }

      this.extrasMaxScroll = Math.max(0, end - start + 6 - viewH);
      this.extrasScroll = Math.max(0.0, Math.min(this.extrasScroll, (double)this.extrasMaxScroll));
      if (this.extrasMaxScroll > 0) {
         int trackX = this.extrasRight - 5;
         soft(ctx, trackX, this.extrasTop + 4, 2, viewH - 8, this.c.borderDim);
         int thumbH = Math.max(16, (viewH - 8) * viewH / (viewH + this.extrasMaxScroll));
         int thumbY = this.extrasTop + 4 + (int)((viewH - 8 - thumbH) * (this.extrasScroll / this.extrasMaxScroll));
         this.scrollBar(ctx, 2, trackX, this.extrasTop + 4, viewH - 8, thumbY, thumbH, mx, my);
      }
   }

   private int drawOutline(GuiGraphicsExtractor ctx, int px, int top, int pw, int bottom, int mx, int my, int secondX, int thirdX) {
      int y = top;
      if (this.want("OUTLINE")) {
         y = this.sectionHeader(ctx, px, top, pw, "PLAYER OUTLINE");
         y = this.switchRow(ctx, px, y, pw, "Outline on", StylesUiConfig.outline, mx, my, () -> StylesUiConfig.outline = !StylesUiConfig.outline);
         y = this.switchRow(ctx, px, y, pw, "Myself (F5)", StylesUiConfig.outlineSelf, mx, my, () -> StylesUiConfig.outlineSelf = !StylesUiConfig.outlineSelf);
         y = this.switchRow(
            ctx, px, y, pw, "Other players", StylesUiConfig.outlineOthers, mx, my, () -> StylesUiConfig.outlineOthers = !StylesUiConfig.outlineOthers
         );
         this.text(ctx, this.fitText("Only while you can see them", pw), px, y - 4, this.c.textDim);
         y += 12;
         int hueH = 6;

         for (int i = 0; i < pw; i++) {
            ctx.fill(px + i, y, px + i + 1, y + hueH, 0xFF000000 | PlayerOutline.sliderColor((float)i / Math.max(1, pw - 1)));
         }

         cutCorners(ctx, px, y, pw, hueH, this.c.surface);
         if (!StylesUiConfig.outlineRainbow) {
            int knobX = px + Math.round(PlayerOutline.sliderPos(StylesUiConfig.outlineColor) * (pw - 1));
            soft(ctx, knobX - 2, y - 2, 4, hueH + 4, -16777216);
            ctx.fill(knobX - 1, y - 1, knobX + 1, y + hueH + 1, -1);
         }

         this.hit(px, y - 4, pw, hueH + 8, (b, x2, y2) -> {
            this.drag = BaulooStylesConfigScreen.Drag.OUTLINE_HUE;
            this.dragW = Math.min(260, this.width - 80);
            this.dragX = (this.width - this.dragW) / 2;
            this.dragY = this.height - 90;
            float start = (float)((x2 - px) / Math.max(1, pw - 1));
            StylesUiConfig.outlineColor = PlayerOutline.sliderColor(Math.max(0.0F, Math.min(1.0F, start)));
            StylesUiConfig.outlineRainbow = false;
            this.dragStartMouse = this.dragX + start * (this.dragW - 1);
            this.dragStartT = Math.max(0.0F, Math.min(1.0F, start));
         });
         y += hueH + 8;
         int sw = Math.max(10, (pw - (OUTLINE_SWATCHES.length - 1) * 4) / OUTLINE_SWATCHES.length);

         for (int i = 0; i < OUTLINE_SWATCHES.length; i++) {
            int sx = px + i * (sw + 4);
            int color = OUTLINE_SWATCHES[i];
            boolean selected = !StylesUiConfig.outlineRainbow && (StylesUiConfig.outlineColor & 16777215) == color;
            soft(ctx, sx, y, sw, 10, 0xFF000000 | color);
            if (selected || inside(mx, my, sx, y, sw, 10)) {
               softLine(ctx, sx - 1, y - 1, sw + 2, 12, selected ? this.c.text : this.c.textDim);
            }

            this.hit(sx, y, sw, 10, (b, x2, y2) -> {
               StylesUiConfig.outlineColor = color;
               StylesUiConfig.outlineRainbow = false;
               StylesUiConfig.save();
            });
         }

         y += 16;
         y = this.switchRow(
            ctx, px, y, pw, "Rainbow", StylesUiConfig.outlineRainbow, mx, my, () -> StylesUiConfig.outlineRainbow = !StylesUiConfig.outlineRainbow
         );
         int width = StylesUiConfig.outlineWidth;
         y = this.sliderRow(
            ctx,
            px,
            y,
            pw,
            "Thickness",
            width == 2 ? "Normal" : String.valueOf(width),
            (width - 1) / 5.0F,
            BaulooStylesConfigScreen.Drag.OUTLINE_WIDTH,
            mx,
            my,
            () -> StylesUiConfig.outlineWidth = 2
         );
         y += 8;
      }

      if (this.want("CRYSTAL")) {
         y = this.sectionHeader(ctx, px, y, pw, "CRYSTAL");
         int spin = StylesUiConfig.crystalSpin;
         y = this.sliderRow(
            ctx,
            px,
            y,
            pw,
            "Spin speed",
            spin == 100 ? "Normal" : (spin == 0 ? "Stopped" : spin + "%"),
            spin / 1000.0F,
            BaulooStylesConfigScreen.Drag.CRYSTAL_SPIN,
            mx,
            my,
            () -> StylesUiConfig.crystalSpin = 100
         );
         int boxH = Math.min(84, bottom - y - 4);
         if (boxH >= 40) {
            int boxW = pw - 50;
            soft(ctx, px, y, boxW, boxH, this.c.base);
            if ("crystal_invisible".equals(this.active.get(BaulooStylesClient.Tab.CRYSTAL))) {
               int boxColor = StylesUiConfig.crystalBoxCustom ? 0xFF000000 | StylesUiConfig.crystalBoxColor : -1;
               int cube = Math.min(boxW, boxH) - 16;
               this.drawBoxPreview(ctx, px + (boxW - cube) / 2, y + 8, cube, boxColor);
               this.textCenter(ctx, this.fitText("Invisible style: only the box", boxW - 6), px, boxW, y + boxH - 11, this.c.textDim);
            } else {
               this.drawCrystalPreview(ctx, px, y, boxW, boxH);
            }

            int bx = px + boxW + 6;
            int bw = pw - boxW - 6;
            boolean stopHover = inside(mx, my, bx, y, bw, 14);
            this.button(
               ctx, bx, y, bw, 14, "STOP", stopHover, spin == 0 ? BaulooStylesConfigScreen.ButtonStyle.PRIMARY : BaulooStylesConfigScreen.ButtonStyle.NORMAL
            );
            this.hit(bx, y, bw, 14, (b, x2, y2) -> {
               StylesUiConfig.crystalSpin = 0;
               StylesUiConfig.save();
            });
            y += boxH + 10;
         }

         this.text(ctx, "Core color", px, y, this.c.textSoft);
         y += 12;
         y = this.colorPicker(ctx, px, y, pw, mx, my, 4);
         y += 4;
      }

      if (this.want("FIRE")) {
         y = this.sectionHeader(ctx, px, y, pw, "FIRE");
         y = this.colorPicker(ctx, px, y, pw, mx, my, 7);
         y += 4;
      }

      int leftEnd = y;
      if (secondX > 0) {
         px = secondX;
         y = top;
      }

      int middleEnd = 0;
      if (this.want("GAP")) {
         if (this.extraOnly == null) {
            Matrix3x2fStack gm = ctx.pose();
            gm.pushMatrix();
            gm.translate(px + 6 + this.textWidth("GAP") + 4, y - 1);
            gm.scale(0.75F, 0.75F);
            ctx.item(stackOf(Items.GOLDEN_APPLE), 0, 0);
            gm.popMatrix();
         }

         y = this.sectionHeader(ctx, px, y, pw, "GAP");
         int gap = StylesUiConfig.gapSize;
         y = this.sliderRow(
            ctx,
            px,
            y,
            pw,
            "Gap size",
            gap == 100 ? "Normal" : gap + "%",
            (gap - 20) / 130.0F,
            BaulooStylesConfigScreen.Drag.GAP_SIZE,
            mx,
            my,
            () -> StylesUiConfig.gapSize = 100
         );
         y += 2;
      }

      if (this.want("COBWEB")) {
         y = this.sectionHeader(ctx, px, y, pw, "COBWEB");
         y = this.switchRow(ctx, px, y, pw, "Outline", StylesUiConfig.cobwebOutline, mx, my, () -> StylesUiConfig.cobwebOutline = !StylesUiConfig.cobwebOutline);
         int web = StylesUiConfig.cobwebOutlineWidth;
         String webPx = web % 4 == 0 ? String.valueOf(web / 4) : String.valueOf(web / 4.0F).replaceAll("0+$", "");
         y = this.sliderRow(
            ctx,
            px,
            y,
            pw,
            "Thickness",
            webPx + " px",
            (web - 1) / 15.0F,
            BaulooStylesConfigScreen.Drag.COBWEB_WIDTH,
            mx,
            my,
            () -> StylesUiConfig.cobwebOutlineWidth = 2
         );
         y = this.drawCobwebPreview(ctx, px, y, pw);
         y += 6;
      }

      if (this.want("SHIELD")) {
         y = this.sectionHeader(ctx, px, y, pw, "SHIELD");
         int opacity = StylesUiConfig.shieldOpacity;
         y = this.sliderRow(
            ctx,
            px,
            y,
            pw,
            "See-through",
            opacity >= 100 ? "Normal" : opacity + "%",
            (opacity - 15) / 85.0F,
            BaulooStylesConfigScreen.Drag.SHIELD,
            mx,
            my,
            () -> StylesUiConfig.shieldOpacity = 100
         );
         int shieldSize = StylesUiConfig.shieldSize;
         y = this.sliderRow(
            ctx,
            px,
            y,
            pw,
            "Size in hand",
            shieldSize == 100 ? "Normal" : shieldSize + "%",
            (shieldSize - 20) / 130.0F,
            BaulooStylesConfigScreen.Drag.SHIELD_SIZE,
            mx,
            my,
            () -> StylesUiConfig.shieldSize = 100
         );
         y = this.colorPicker(ctx, px, y, pw, mx, my, 8);
         y += 2;
      }

      if (this.want("TOTEM")) {
         y = this.sectionHeader(ctx, px, y, pw, "TOTEM");
         int totem = StylesUiConfig.totemSize;
         y = this.sliderRow(
            ctx,
            px,
            y,
            pw,
            "Size in hand",
            totem == 100 ? "Normal" : totem + "%",
            (totem - 20) / 130.0F,
            BaulooStylesConfigScreen.Drag.TOTEM,
            mx,
            my,
            () -> StylesUiConfig.totemSize = 100
         );
         int pop = StylesUiConfig.totemPopSize;
         y = this.sliderRow(
            ctx,
            px,
            y,
            pw,
            "Pop size",
            pop == 100 ? "Normal" : pop + "%",
            (pop - 20) / 130.0F,
            BaulooStylesConfigScreen.Drag.TOTEM_POP,
            mx,
            my,
            () -> StylesUiConfig.totemPopSize = 100
         );
         boolean testHover = inside(mx, my, px, y - 2, pw, 14);
         this.button(ctx, px, y - 2, pw, 14, "TEST POP", testHover, BaulooStylesConfigScreen.ButtonStyle.NORMAL);
         this.hit(px, y - 2, pw, 14, (b, x2, y2) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
               mc.gui.setScreen(null);
               mc.gameRenderer.displayItemActivation(stackOf(Items.TOTEM_OF_UNDYING));
               reopenAtMs = System.currentTimeMillis() + 2200L;
            }
         });
         y += 22;
      }

      if (this.want("HITBOX")) {
         y = this.sectionHeader(ctx, px, y, pw, "HITBOX");
         y = this.colorPicker(ctx, px, y, pw, mx, my, 0);
      }

      if (this.want("CRYSTALBOX")) {
         y = this.sectionHeader(ctx, px, y, pw, "INVISIBLE CRYSTAL BOX");
         y = this.colorPicker(ctx, px, y, pw, mx, my, 2);
      }

      if (this.want("SPOTIFY")) {
      }

      if (this.want("TABICON")) {
         y = this.sectionHeader(ctx, px, y, pw, "TAB ICON");
         y = this.switchRow(ctx, px, y, pw, "Show me in tab", StylesUiConfig.showInTab, mx, my, () -> {
            StylesUiConfig.showInTab = !StylesUiConfig.showInTab;
            StylesPresence.settingChanged();
         });
         this.scaled(
            ctx, this.fitText("A tornado in front of your name for others with Bauloo Styles", Math.round(pw / 0.8F)), px, y - 4, this.c.textDim, 0.8F, false
         );
         y += 10;
      }

      if (this.want("MENU")) {
         y = this.sectionHeader(ctx, px, y, pw, "MENU");
         y = this.switchRow(ctx, px, y, pw, "Apply instantly", StylesUiConfig.applyInstantly, mx, my, () -> {
            StylesUiConfig.applyInstantly = !StylesUiConfig.applyInstantly;
            PENDING.clear();
         });
         this.scaled(
            ctx,
            this.fitText(
               StylesUiConfig.applyInstantly ? "Every click reloads right away" : "Clicks select styles, APPLY loads them all at once", Math.round(pw / 0.8F)
            ),
            px,
            y - 4,
            this.c.textDim,
            0.8F,
            false
         );
         y += 10;
      }

      if (this.want("SOCIAL")) {
         y = this.sectionHeader(ctx, px, y, pw, "SOCIAL BADGE");
         String[] socials = Watermark.SOCIALS;
         int socialCount = socials.length + 1;
         int tile = Math.max(12, Math.min(22, (pw - (socialCount - 1) * 3) / socialCount));

         for (int i = 0; i < socialCount; i++) {
            int tx = px + i * (tile + 3);
            String value = i == 0 ? "off" : socials[i - 1];
            boolean chosen = value.equals(StylesUiConfig.socialLogo) || i == 0 && !Watermark.validSocial(StylesUiConfig.socialLogo);
            boolean hover = inside(mx, my, tx, y, tile, tile);
            if (i == 0) {
               soft(ctx, tx, y, tile, tile, this.c.base);
               this.textCenter(ctx, "Off", tx, tile, y + (tile - 8) / 2, chosen ? this.c.text : this.c.textDim);
            } else {
               ctx.blit(
                  RenderPipelines.GUI_TEXTURED, Watermark.icon(value), tx, y, 0.0F, 0.0F, tile, tile, 128, 128, 128, 128, !chosen && !hover ? -1711276033 : -1
               );
            }

            if (chosen || hover) {
               softLine(ctx, tx - 1, y - 1, tile + 2, tile + 2, chosen ? this.c.accent : this.c.textDim);
            }

            this.hit(tx, y, tile, tile, (b, x2, y2) -> {
               StylesUiConfig.socialLogo = value;
               StylesUiConfig.save();
            });
         }

         y += tile + 6;
         boolean nameHover = inside(mx, my, px, y, pw, 15);
         soft(ctx, px, y, pw, 15, !this.socialFocused && !nameHover ? this.c.base : this.c.surface2);
         if (this.socialFocused) {
            softLine(ctx, px, y, pw, 15, this.c.accent);
         }

         boolean blink = this.socialFocused && System.currentTimeMillis() / 500L % 2L == 0L;
         String shown = StylesUiConfig.socialName.isEmpty() && !this.socialFocused ? "Your name..." : StylesUiConfig.socialName + (blink ? "_" : "");
         this.text(ctx, this.fitText(shown, pw - 10), px + 5, y + 4, StylesUiConfig.socialName.isEmpty() && !this.socialFocused ? this.c.textDim : this.c.text);
         this.hit(px, y, pw, 15, (b, x2, y2) -> this.socialFocused = true);
         y += 19;
         this.text(ctx, this.fitText("Shows on the recipe book button", pw), px, y, this.c.textDim);
         y += 14;
      }

      if (thirdX > 0) {
         middleEnd = y;
         px = thirdX;
         y = top;
      }

      if (this.want("ANCHOR")) {
         y = this.sectionHeader(ctx, px, y, pw, "ANCHOR CHARGE");
         y = this.colorPicker(ctx, px, y, pw, mx, my, 3);
         y = this.switchRow(ctx, px, y, pw, "Look full at 1 charge", StylesUiConfig.anchorShowFull, mx, my, () -> {
            StylesUiConfig.anchorShowFull = !StylesUiConfig.anchorShowFull;
            StylesUiConfig.save();
            AnchorGlow.apply();
         });
         this.text(ctx, this.fitText("1 glowstone already looks full", pw), px, y - 4, this.c.textDim);
         y += 14;
         y = this.switchRow(ctx, px, y, pw, "Glowing pixels", StylesUiConfig.anchorPixelGlow, mx, my, () -> {
            StylesUiConfig.anchorPixelGlow = !StylesUiConfig.anchorPixelGlow;
            StylesUiConfig.save();
            AnchorPixelGlow.applyLive();
         });
         this.text(ctx, this.fitText("The small bright pixels glow in the dark", pw), px, y - 4, this.c.textDim);
         y += 14;
      }

      if (this.want("CROSSHAIR")) {
         y = this.sectionHeader(ctx, px, y, pw, "CROSSHAIR");
         y = this.colorPicker(ctx, px, y, pw, mx, my, 1);
         y = this.switchRow(ctx, px, y, pw, "Gradient", StylesUiConfig.crosshairGradient, mx, my, () -> {
            StylesUiConfig.crosshairGradient = !StylesUiConfig.crosshairGradient;
            if (StylesUiConfig.crosshairGradient) {
               StylesUiConfig.crosshairCustom = true;
            }
         });
         if (StylesUiConfig.crosshairGradient) {
            this.text(ctx, "Fades to", px, y, this.c.textSoft);
            y += 12;
            int n = PICK_SWATCHES.length;
            int gw = Math.max(8, (pw - (n - 1) * 4) / n);

            for (int i = 0; i < n; i++) {
               int sx = px + i * (gw + 4);
               int swatch = PICK_SWATCHES[i];
               boolean selected = (StylesUiConfig.crosshairColor2 & 16777215) == swatch;
               soft(ctx, sx, y, gw, 12, 0xFF000000 | swatch);
               if (selected || inside(mx, my, sx, y, gw, 12)) {
                  softLine(ctx, sx - 1, y - 1, gw + 2, 14, selected ? this.c.text : this.c.textDim);
               }

               this.hit(sx, y, gw, 12, (b, x2, y2) -> {
                  StylesUiConfig.crosshairColor2 = swatch;
                  StylesUiConfig.save();
               });
            }

            y += 18;

            for (int i = 0; i < pw; i++) {
               ctx.fill(
                  px + i,
                  y,
                  px + i + 1,
                  y + 6,
                  StylesCrosshair.mix(
                     0xFF000000 | StylesUiConfig.crosshairColor,
                     0xFF000000 | StylesUiConfig.crosshairColor2,
                     StylesCrosshair.shaped((float)i / Math.max(1, pw - 1))
                  )
               );
            }

            cutCorners(ctx, px, y, pw, 6, this.c.surface);
            y += 14;
            int strength = StylesUiConfig.gradientStrength;
            y = this.sliderRow(
               ctx,
               px,
               y,
               pw,
               "Strength",
               strength == 100 ? "Normal" : strength + "%",
               (strength - 50) / 350.0F,
               BaulooStylesConfigScreen.Drag.GRADIENT,
               mx,
               my,
               () -> StylesUiConfig.gradientStrength = 100
            );
         }
      }

      if (this.want("BLOCK")) {
         y = this.sectionHeader(ctx, px, y, pw, "BLOCK OUTLINE");
         y = this.colorPicker(ctx, px, y, pw, mx, my, 9);
         int blockWidth = StylesUiConfig.blockOutlineWidth;
         y = this.sliderRow(
            ctx,
            px,
            y,
            pw,
            "Thickness",
            blockWidth == 1 ? "Thin" : String.valueOf(blockWidth),
            (blockWidth - 1) / 5.0F,
            BaulooStylesConfigScreen.Drag.BLOCK_WIDTH,
            mx,
            my,
            () -> StylesUiConfig.blockOutlineWidth = 3
         );
      }

      if (this.want("KILL")) {
         y = this.sectionHeader(ctx, px, y, pw, "KILL EFFECT");
         int chipX = px;
         int chipH = 12;
         String[][] effects = new String[KillEffect.EFFECTS.length + 1][];
         effects[0] = new String[]{"off", "Off"};
         System.arraycopy(KillEffect.EFFECTS, 0, effects, 1, KillEffect.EFFECTS.length);

         for (String[] effect : effects) {
            int cw = this.font.width(effect[1]) + 10;
            if (chipX > px && chipX + cw > px + pw) {
               chipX = px;
               y += chipH + 4;
            }

            boolean chosenx = effect[0].equals(StylesUiConfig.killEffect) || effect[0].equals("off") && !KillEffect.valid(StylesUiConfig.killEffect);
            boolean hoverx = inside(mx, my, chipX, y, cw, chipH);
            soft(ctx, chipX, y, cw, chipH, chosenx ? this.c.accentDark : this.c.base);
            softLine(ctx, chipX, y, cw, chipH, chosenx ? this.c.accent : (hoverx ? this.c.textDim : this.c.borderDim));
            this.text(ctx, effect[1], chipX + 5, y + 2, chosenx ? this.c.text : (hoverx ? this.c.textSoft : this.c.textDim));
            String key = effect[0];
            this.hit(chipX, y, cw, chipH, (b, x2, y2) -> {
               StylesUiConfig.killEffect = key;
               StylesUiConfig.save();
            });
            chipX += cw + 4;
         }

         y += chipH + 8;
         y = this.switchRow(
            ctx, px, y, pw, "Only my kills", StylesUiConfig.killOnlyMine, mx, my, () -> StylesUiConfig.killOnlyMine = !StylesUiConfig.killOnlyMine
         );
         y = this.switchRow(ctx, px, y, pw, "cpvp.gg kill sound", StylesUiConfig.killSound, mx, my, () -> {
            StylesUiConfig.killSound = !StylesUiConfig.killSound;
            KillEffect.killSound();
         });
         if (StylesUiConfig.killSound) {
            int vol = StylesUiConfig.killVolume;
            y = this.sliderRow(ctx, px, y, pw, "Volume", vol + "%", (vol - 5) / 95.0F, BaulooStylesConfigScreen.Drag.KILL_VOLUME, mx, my, () -> {
               StylesUiConfig.killVolume = 100;
               KillEffect.killSound();
            });
         }

         y = this.switchRow(
            ctx,
            px,
            y,
            pw,
            "No explosion sounds",
            StylesUiConfig.noExplosionSound,
            mx,
            my,
            () -> StylesUiConfig.noExplosionSound = !StylesUiConfig.noExplosionSound
         );
         if (KillEffect.valid(StylesUiConfig.killEffect) && !"lightning".equals(StylesUiConfig.killEffect)) {
            int killSw = Math.max(10, (pw - (PICK_SWATCHES.length - 1) * 4) / PICK_SWATCHES.length);

            for (int i = 0; i < PICK_SWATCHES.length; i++) {
               int sx = px + i * (killSw + 4);
               int swatch = PICK_SWATCHES[i];
               boolean selected = (StylesUiConfig.killColor & 16777215) == swatch;
               soft(ctx, sx, y, killSw, 10, 0xFF000000 | swatch);
               if (selected || inside(mx, my, sx, y, killSw, 10)) {
                  softLine(ctx, sx - 1, y - 1, killSw + 2, 12, selected ? this.c.text : this.c.textDim);
               }

               this.hit(sx, y, killSw, 10, (b, x2, y2) -> {
                  StylesUiConfig.killColor = swatch;
                  StylesUiConfig.save();
               });
            }

            y += 16;
         }

         boolean killTestHover = inside(mx, my, px, y - 2, pw, 14);
         this.button(ctx, px, y - 2, pw, 14, "TEST", killTestHover, BaulooStylesConfigScreen.ButtonStyle.NORMAL);
         this.hit(px, y - 2, pw, 14, (b, x2, y2) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
               mc.gui.setScreen(null);
               KillEffect.test();
               reopenAtMs = System.currentTimeMillis() + 2500L;
            }
         });
         y += 22;
      }

      if (this.want("GLINT")) {
         y = this.sectionHeader(ctx, px, y, pw, "ENCHANT GLINT");
         y = this.colorPicker(ctx, px, y, pw, mx, my, 6);
         y = this.switchRow(ctx, px, y, pw, "Gradient (two colors)", StylesUiConfig.glintGradient, mx, my, () -> {
            StylesUiConfig.glintGradient = !StylesUiConfig.glintGradient;
            if (StylesUiConfig.glintGradient) {
               StylesUiConfig.glintRainbow = false;
               StylesUiConfig.glintCustom = true;
            }

            StylesUiConfig.save();
            EnchantGlint.applyLive();
         });
         if (StylesUiConfig.glintGradient) {
            this.text(ctx, "Fades to", px, y, this.c.textSoft);
            y += 12;
            int n = PICK_SWATCHES.length;
            int gw = Math.max(8, (pw - (n - 1) * 4) / n);

            for (int i = 0; i < n; i++) {
               int sx = px + i * (gw + 4);
               int swatch = PICK_SWATCHES[i];
               boolean selected = (StylesUiConfig.glintColor2 & 16777215) == swatch;
               soft(ctx, sx, y, gw, 12, 0xFF000000 | swatch);
               if (selected || inside(mx, my, sx, y, gw, 12)) {
                  softLine(ctx, sx - 1, y - 1, gw + 2, 14, selected ? this.c.text : this.c.textDim);
               }

               this.hit(sx, y, gw, 12, (b, x2, y2) -> {
                  StylesUiConfig.glintColor2 = swatch;
                  StylesUiConfig.save();
                  EnchantGlint.applyLive();
               });
            }

            y += 18;
         }

         y = this.switchRow(ctx, px, y, pw, "Rainbow", StylesUiConfig.glintRainbow, mx, my, () -> {
            StylesUiConfig.glintRainbow = !StylesUiConfig.glintRainbow;
            if (StylesUiConfig.glintRainbow) {
               StylesUiConfig.glintGradient = false;
               StylesUiConfig.glintCustom = true;
            }

            StylesUiConfig.save();
            EnchantGlint.applyLive();
         });

         for (int i = 0; i < pw; i++) {
            int color = StylesUiConfig.glintCustom ? EnchantGlint.colorAt(i, 0, pw, 1) : 8405247;
            ctx.fill(px + i, y, px + i + 1, y + 6, 0xFF000000 | color);
         }

         cutCorners(ctx, px, y, pw, 6, this.c.surface);
         y += 16;
      }

      return Math.max(Math.max(leftEnd, middleEnd), y);
   }

   private static boolean pickOn(int kind) {
      return switch (kind) {
         case 0 -> StylesUiConfig.hitboxCustom;
         case 1 -> StylesUiConfig.crosshairCustom;
         default -> StylesUiConfig.crystalBoxCustom;
         case 3 -> StylesUiConfig.anchorGlowCustom;
         case 4 -> StylesUiConfig.coreCustom;
         case 6 -> StylesUiConfig.glintCustom;
         case 7 -> StylesUiConfig.fireCustom;
         case 8 -> StylesUiConfig.shieldCustom;
         case 9 -> StylesUiConfig.blockOutlineCustom;
      };
   }

   private static int pickColor(int kind) {
      return switch (kind) {
         case 0 -> StylesUiConfig.hitboxColor;
         case 1 -> StylesUiConfig.crosshairColor;
         default -> StylesUiConfig.crystalBoxColor;
         case 3 -> StylesUiConfig.anchorGlowColor;
         case 4 -> StylesUiConfig.coreColor;
         case 6 -> StylesUiConfig.glintColor;
         case 7 -> StylesUiConfig.fireColor;
         case 8 -> StylesUiConfig.shieldColor;
         case 9 -> StylesUiConfig.blockOutlineColor;
      };
   }

   private static void setPick(int kind, Boolean on, Integer color) {
      switch (kind) {
         case 0:
            if (on != null) {
               StylesUiConfig.hitboxCustom = on;
            }

            if (color != null) {
               StylesUiConfig.hitboxColor = color;
            }
            break;
         case 1:
            if (on != null) {
               StylesUiConfig.crosshairCustom = on;
            }

            if (color != null) {
               StylesUiConfig.crosshairColor = color;
            }
            break;
         case 2:
         case 5:
         default:
            if (on != null) {
               StylesUiConfig.crystalBoxCustom = on;
            }

            if (color != null) {
               StylesUiConfig.crystalBoxColor = color;
            }
            break;
         case 3:
            if (on != null) {
               StylesUiConfig.anchorGlowCustom = on;
            }

            if (color != null) {
               StylesUiConfig.anchorGlowColor = color;
            }

            if (!huePreview) {
               StylesUiConfig.save();
               AnchorGlow.apply();
            }
            break;
         case 4:
            if (on != null) {
               StylesUiConfig.coreCustom = on;
            }

            if (color != null) {
               StylesUiConfig.coreColor = color;
            }

            if (!huePreview) {
               StylesUiConfig.save();
               CrystalCore.apply();
            }
            break;
         case 6:
            if (on != null) {
               StylesUiConfig.glintCustom = on;
            }

            if (color != null) {
               StylesUiConfig.glintColor = color;
               StylesUiConfig.glintRainbow = false;
            }

            if (!huePreview) {
               StylesUiConfig.save();
               EnchantGlint.applyLive();
            }
            break;
         case 7:
            if (on != null) {
               StylesUiConfig.fireCustom = on;
            }

            if (color != null) {
               StylesUiConfig.fireColor = color;
            }

            if (!huePreview) {
               StylesUiConfig.save();
               FireColor.applyLive();
            }
            break;
         case 8:
            if (on != null) {
               StylesUiConfig.shieldCustom = on;
            }

            if (color != null) {
               StylesUiConfig.shieldColor = color;
            }

            if (!huePreview) {
               StylesUiConfig.save();
               ShieldColor.applyLive();
            }
            break;
         case 9:
            if (on != null) {
               StylesUiConfig.blockOutlineCustom = on;
            }

            if (color != null) {
               StylesUiConfig.blockOutlineColor = color;
            }
      }
   }

   private int colorPicker(GuiGraphicsExtractor ctx, int px, int y, int pw, int mx, int my, int kind) {
      boolean on = pickOn(kind);
      y = this.switchRow(ctx, px, y, pw, "Own color", on, mx, my, () -> setPick(kind, !pickOn(kind), null));
      int color = 0xFF000000 | pickColor(kind);
      int shown = on ? color : -1;
      int box = kind == 6 ? 118 : 76;
      soft(ctx, px, y, box, box, this.c.base);
      if (kind == 0) {
         this.drawHitboxPreview(ctx, px, y, box, shown);
      } else if (kind == 1) {
         this.drawCrosshairPreview(ctx, px, y, box, shown);
      } else if (kind == 3) {
         this.drawAnchorPreview(ctx, px, y, box, on);
      } else if (kind == 4) {
         this.drawCrystalPreview(ctx, px, y, box, box);
      } else if (kind == 6) {
         this.drawGlintPreview(ctx, px, y, box);
      } else if (kind == 7) {
         this.drawFirePreview(ctx, px, y, box);
      } else if (kind == 8) {
         this.drawShieldPreview(ctx, px, y, box);
      } else if (kind == 9) {
         this.drawBlockOutlinePreview(ctx, px, y, box, on);
      } else {
         this.drawBoxPreview(ctx, px, y, box, shown);
      }

      int sx0 = px + box + 8;
      int perRow = 4;
      int sw = Math.max(10, (pw - box - 8 - (perRow - 1) * 4) / perRow);
      int[] swatches = kind == 9 ? BLOCK_SWATCHES : PICK_SWATCHES;

      for (int i = 0; i < swatches.length; i++) {
         int sx = sx0 + i % perRow * (sw + 4);
         int sy = y + 4 + i / perRow * 18;
         int swatch = swatches[i];
         boolean selected = on && (color & 16777215) == swatch;
         soft(ctx, sx, sy, sw, 12, 0xFF000000 | swatch);
         if (selected || inside(mx, my, sx, sy, sw, 12)) {
            softLine(ctx, sx - 1, sy - 1, sw + 2, 14, selected ? this.c.text : this.c.textDim);
         }

         this.hit(sx, sy, sw, 12, (b, x2, y2) -> {
            setPick(kind, true, swatch);
            StylesUiConfig.save();
         });
      }

      int rows = (swatches.length + perRow - 1) / perRow;
      int barY = y + 4 + rows * 18 + 4;
      int barW = perRow * sw + (perRow - 1) * 4;
      int barH = 6;

      for (int i = 0; i < barW; i++) {
         ctx.fill(sx0 + i, barY, sx0 + i + 1, barY + barH, 0xFF000000 | PlayerOutline.sliderColor((float)i / Math.max(1, barW - 1)));
      }

      cutCorners(ctx, sx0, barY, barW, barH, this.c.surface);
      if (on) {
         int knobX = sx0 + Math.round(PlayerOutline.sliderPos(color) * (barW - 1));
         soft(ctx, knobX - 2, barY - 2, 4, barH + 4, -16777216);
         ctx.fill(knobX - 1, barY - 1, knobX + 1, barY + barH + 1, -1);
      }

      this.hit(sx0, barY - 4, barW, barH + 8, (b, x2, y2) -> {
         this.drag = BaulooStylesConfigScreen.Drag.PICK_HUE;
         this.huePick = kind;
         this.dragX = sx0;
         this.dragW = barW;
         this.updateDrag(x2);
      });
      return y + box + 12;
   }

   private void drawFirePreview(GuiGraphicsExtractor ctx, int x, int y, int size) {
      Minecraft mc = Minecraft.getInstance();
      TextureAtlasSprite fire = mc.getAtlasManager().get(ModelBakery.FIRE_0);
      int s = size - 12;
      ctx.blitSprite(RenderPipelines.GUI_TEXTURED, fire, x + 6, y + 6, s, s);
   }

   private void drawShieldPreview(GuiGraphicsExtractor ctx, int x, int y, int size) {
      Minecraft mc = Minecraft.getInstance();
      TextureAtlasSprite sprite = mc.getAtlasManager().get(Sheets.SHIELD_BASE_NO_PATTERN);
      NativeImage source = ((SpriteContentsAccessor)sprite.contents()).baulooStyles$image();
      if (source != null) {
         String key = StylesUiConfig.shieldCustom + ":" + StylesUiConfig.shieldColor + ":" + System.identityHashCode(source);
         if (shieldPreviewId == null || !key.equals(shieldPreviewKey)) {
            int unit = Math.max(1, sprite.contents().width() / 64);
            int w = 12 * unit;
            int h = 22 * unit;
            NativeImage image = new NativeImage(w, h, false);
            int color = StylesUiConfig.shieldColor;

            for (int py = 0; py < h; py++) {
               for (int px = 0; px < w; px++) {
                  int c = source.getPixel(unit + px, unit + py);
                  if (StylesUiConfig.shieldCustom) {
                     float lum = (0.3F * (c >> 16 & 0xFF) + 0.59F * (c >> 8 & 0xFF) + 0.11F * (c & 0xFF)) / 255.0F;
                     float v = Math.min(1.0F, 0.35F + lum * 1.3F);
                     int r = Math.round((color >> 16 & 0xFF) * v);
                     int g = Math.round((color >> 8 & 0xFF) * v);
                     int b = Math.round((color & 0xFF) * v);
                     c = c & 0xFF000000 | r << 16 | g << 8 | b;
                  }

                  image.setPixel(px, py, c);
               }
            }

            Identifier id = Identifier.fromNamespaceAndPath("bauloo_styles", "shield_preview");
            mc.getTextureManager().register(id, new DynamicTexture(id::toString, image));
            shieldPreviewId = id;
            shieldPreviewKey = key;
         }

         int h = size - 12;
         int w = Math.round(h * 12 / 22.0F);
         ctx.blit(RenderPipelines.GUI_TEXTURED, shieldPreviewId, x + (size - w) / 2, y + 6, 0.0F, 0.0F, w, h, w, h);
      }
   }

   private static void previewEntity(
      GuiGraphicsExtractor ctx, int x1, int y1, int x2, int y2, int scale, float yOffset, float lookX, float lookY, LivingEntity entity
   ) {
      OutlinePreview.hideName = true;

      try {
         int[] box = guiRect(ctx, x1, y1, x2, y2);
         int[] look = guiRect(ctx, Math.round(lookX), Math.round(lookY), Math.round(lookX), Math.round(lookY));
         InventoryScreen.extractEntityInInventoryFollowsMouse(
            ctx, box[0], box[1], box[2], box[3], Math.round(scale * guiScale(ctx)), yOffset, look[0], look[1], entity
         );
      } finally {
         OutlinePreview.hideName = false;
      }
   }

   private void drawGlintPreview(GuiGraphicsExtractor ctx, int x, int y, int size) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         int scale = Math.round(size * 0.4F);
         previewEntity(ctx, x + 2, y + 2, x + size - 2, y + size - 2, scale, 0.0625F, x + size / 2.0F, y + size * 0.45F, mc.player);
      }
   }

   private void drawAnchorPreview(GuiGraphicsExtractor ctx, int x, int y, int size, boolean on) {
      Identifier glow = !on && !StylesUiConfig.anchorShowFull ? null : AnchorGlow.preview();
      int s = size - 12;
      if (glow != null) {
         ctx.blit(
            RenderPipelines.GUI_TEXTURED,
            glow,
            x + 6,
            y + 6,
            0.0F,
            0.0F,
            s,
            s,
            AnchorGlow.previewW,
            AnchorGlow.previewH,
            AnchorGlow.previewW,
            AnchorGlow.previewH
         );
      } else {
         Matrix3x2fStack m = ctx.pose();
         m.pushMatrix();
         m.translate(x + size / 2.0F, y + size / 2.0F);
         m.scale(3.0F, 3.0F);
         itemIcon(ctx, Items.RESPAWN_ANCHOR, -8, -8);
         m.popMatrix();
      }
   }

   private void drawBlockOutlinePreview(GuiGraphicsExtractor ctx, int x, int y, int size, boolean on) {
      TextureAtlasSprite dirt = Minecraft.getInstance()
         .getAtlasManager()
         .get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, Identifier.withDefaultNamespace("block/dirt")));
      int half = size / 2;

      for (int i = 0; i < 4; i++) {
         ctx.blitSprite(RenderPipelines.GUI_TEXTURED, dirt, x + i % 2 * half, y + i / 2 * half, half, half);
      }

      int color = on ? 0xFF000000 | StylesUiConfig.blockOutlineColor : 1711276032;
      int t = on ? StylesUiConfig.blockOutlineWidth : 1;
      int s = size / 2;
      int d = size / 6;
      int fx = x + (size - s - d) / 2;
      int fy = y + (size - s - d) / 2 + d;
      ctx.fill(fx, fy, fx + s, fy + t, color);
      ctx.fill(fx, fy + s - t, fx + s, fy + s, color);
      ctx.fill(fx, fy, fx + t, fy + s, color);
      ctx.fill(fx + s - t, fy, fx + s, fy + s, color);
      ctx.fill(fx + d, fy - d, fx + d + s, fy - d + t, color);
      ctx.fill(fx + d + s - t, fy - d, fx + d + s, fy - d + s, color);

      for (int i = 0; i <= d; i++) {
         ctx.fill(fx + i, fy - i, fx + i + t, fy - i + t, color);
         ctx.fill(fx + s - t + i, fy - i, fx + s + i, fy - i + t, color);
         ctx.fill(fx + s - t + i, fy + s - t - i, fx + s + i, fy + s - i, color);
      }
   }

   private int drawSpotifyPreview(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      float scale = Math.min(1.0F, (w - 12) / 200.0F);
      int h = Math.round(46.0F * scale) + 12;
      soft(ctx, x, y, w, h, this.c.base);
      String title = SpotifyNowPlaying.title;
      if (!StylesUiConfig.spotifyHud) {
         this.text(ctx, this.fitText("Off", w - 12), x + 6, y + h / 2 - 4, this.c.textDim);
      } else if (title != null) {
         Matrix3x2fStack m = ctx.pose();
         m.pushMatrix();
         m.translate(x + (w - 200.0F * scale) / 2.0F, y + 6);
         m.scale(scale, scale);
         SpotifyHud.draw(ctx, this.font, 0, 0, title, SpotifyNowPlaying.artist, 1.0F);
         m.popMatrix();
      } else {
         String why = SpotifyNowPlaying.appOpen ? "Spotify is paused" : "Open the Spotify app and play a song";
         this.text(ctx, this.fitText(why, w - 12), x + 6, y + h / 2 - 4, this.c.textDim);
      }

      y += h + 4;
      boolean hover = inside(mx, my, x, y, w, 14);
      this.button(ctx, x, y, w, 14, "MOVE", hover, BaulooStylesConfigScreen.ButtonStyle.NORMAL);
      this.hit(x, y, w, 14, (b, x2, y2) -> Minecraft.getInstance().gui.setScreen(new SpotifyMoveScreen(this)));
      y += 18;
      this.text(ctx, this.fitText("Spotify app only. Cover from Deezer / iTunes.", w), x, y, this.c.textDim);
      return y + 14;
   }

   private void drawBoxPreview(GuiGraphicsExtractor ctx, int x, int y, int size, int color) {
      int s = size / 2;
      int d = size / 6;
      int fx = x + (size - s - d) / 2;
      int fy = y + (size - s - d) / 2 + d;
      softLine(ctx, fx + d, fy - d, s, s, StylesTheme.withAlpha(color, 153));
      softLine(ctx, fx, fy, s, s, color);

      for (int i = 0; i <= d; i++) {
         ctx.fill(fx + i, fy - i, fx + i + 1, fy - i + 1, color);
         ctx.fill(fx + s - 1 + i, fy - i, fx + s + i, fy - i + 1, color);
         ctx.fill(fx + i, fy + s - 1 - i, fx + i + 1, fy + s - i, color);
         ctx.fill(fx + s - 1 + i, fy + s - 1 - i, fx + s + i, fy + s - i, color);
      }
   }

   private void drawHitboxPreview(GuiGraphicsExtractor ctx, int x, int y, int size, int color) {
      Minecraft mc = Minecraft.getInstance();
      int scale = Math.round(size * 0.36F);
      int hw = Math.round(0.6F * scale);
      int hh = Math.round(1.8F * scale);
      if (mc.player != null) {
         previewEntity(ctx, x + 2, y + 2, x + size - 2, y + size - 2, scale, 0.0625F, x + size / 2.0F, y + size / 2.0F, mc.player);
      }

      softLine(ctx, x + (size - hw) / 2, y + (size - hh) / 2, hw, hh, color);
   }

   private void drawCrosshairPreview(GuiGraphicsExtractor ctx, int x, int y, int size, int color) {
      Matrix3x2fStack m = ctx.pose();
      m.pushMatrix();
      m.translate(x + size / 2.0F, y + size / 2.0F);
      m.scale(3.0F, 3.0F);
      if (StylesUiConfig.crosshairCustom) {
         StylesCrosshair.draw(ctx, Identifier.withDefaultNamespace("hud/crosshair"), -8, -8, 15, 15);
      } else {
         ctx.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("hud/crosshair"), -8, -8, 15, 15, color);
      }

      m.popMatrix();
   }

   private int sectionHeader(GuiGraphicsExtractor ctx, int px, int y, int pw, String title) {
      if (this.extraOnly != null) {
         return y;
      } else {
         soft(ctx, px, y, 2, 9, this.c.accent);
         this.text(ctx, title, px + 6, y + 1, this.c.text);
         ctx.fill(px, y + 13, px + pw, y + 14, this.c.borderDim);
         return y + 20;
      }
   }

   private int sliderRow(
      GuiGraphicsExtractor ctx, int px, int y, int pw, String label, String value, float t, BaulooStylesConfigScreen.Drag kind, int mx, int my, Runnable reset
   ) {
      int rw = 36;
      this.text(ctx, this.fitText(label, pw - rw - 60), px, y, this.c.textSoft);
      this.textRight(ctx, value, px + pw - rw - 6, y, this.c.text);
      boolean resetHover = inside(mx, my, px + pw - rw, y - 3, rw, 13);
      this.textCenter(ctx, "RESET", px + pw - rw, rw, y, resetHover ? this.c.accent : this.c.textDim);
      this.hit(px + pw - rw, y - 3, rw, 13, (b, x2, y2) -> {
         reset.run();
         StylesUiConfig.save();
      });
      y += 13;
      float clamped = Math.max(0.0F, Math.min(1.0F, t));
      int fill = px + Math.round(clamped * (pw - 1));
      soft(ctx, px, y + 1, pw, 4, this.c.base);
      soft(ctx, px, y + 1, Math.max(3, fill - px), 4, this.c.accentDark);
      soft(ctx, fill - 2, y - 2, 5, 10, this.c.accent);
      this.hit(px, y - 4, pw, 14, (b, x2, y2) -> {
         this.drag = kind;
         this.dragX = px;
         this.dragW = pw;
         this.dragY = y;
         this.dragStartMouse = x2;
         this.dragStartT = clamped;
         if (!this.liveDrag()) {
            this.updateDrag(x2);
         }
      });
      return y + 16;
   }

   private void drawCrystalPreview(GuiGraphicsExtractor ctx, int x, int y, int w, int h) {
      this.crystalState.entityType = EntityTypes.END_CRYSTAL;
      float realAge = (float)(System.currentTimeMillis() % 1000000L) / 50.0F;
      ((CrystalAge)this.crystalState).baulooStyles$setRealAge(realAge);
      this.crystalState.ageInTicks = realAge * CrystalAge.factor();
      this.crystalState.showsBottom = false;
      this.crystalState.beamOffset = null;
      this.crystalState.lightCoords = 15728880;
      this.crystalState.boundingBoxWidth = 2.0F;
      this.crystalState.boundingBoxHeight = 2.0F;
      float scale = Math.min(w, h) * 0.38F;
      Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
      Quaternionf camera = new Quaternionf().rotateX(0.35F);
      rotation.mul(camera);
      int[] box = guiRect(ctx, x + 1, y + 1, x + w - 1, y + h - 1);
      ctx.entity(this.crystalState, scale * guiScale(ctx), new Vector3f(0.0F, 1.1F, 0.0F), rotation, camera, box[0], box[1], box[2], box[3]);
   }

   private void drawCosmetics(GuiGraphicsExtractor ctx, BaulooStylesConfigScreen.Layout l, int mx, int my) {
      int left = l.mainX;
      int right = l.side ? l.sideX + l.sideW : l.mainX + l.mainW;
      int top = l.bodyY + 8;
      int bottom = l.footerY - 10;
      this.text(ctx, "Cosmetics", left, top + 3, this.c.text);
      this.scaled(ctx, "Only you see them - nothing is sent to the server", left, top + 15, this.c.textDim, 0.8F, false);
      int y0 = top + 30;
      int full = right - left;
      int pw = Math.max(110, Math.min(200, full * 36 / 100));
      int ph = bottom - y0;
      soft(ctx, left, y0, pw, ph, this.c.surface);
      ctx.fillGradient(left + 1, y0 + ph / 2, left + pw - 1, bottom - 1, 0, StylesTheme.withAlpha(this.c.accentDark, 144));
      boolean overPreview = inside(mx, my, left, y0, pw, ph);
      boolean backFirst = cosSlot == Cosmetics.Slot.CAPE || cosSlot == Cosmetics.Slot.MC_CAPE;
      if (this.cosYawSlot != cosSlot) {
         this.cosYawSlot = cosSlot;
         this.cosYaw = backFirst ? 0.0F : 180.0F;
      }

      this.hit(left, y0, pw, ph, (b, x2, y2) -> {
         this.drag = BaulooStylesConfigScreen.Drag.COS_TURN;
         this.dragStartMouse = x2;
         this.dragW = pw;
         this.cosDragYaw = this.cosYaw;
      });
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         drawCosmeticPlayer(ctx, mc.player, left + 4, y0 + 4, left + pw - 4, bottom - 22, this.cosYaw);
      } else {
         this.textCenter(ctx, "Join a world to see yourself", left, pw, y0 + ph / 2 - 4, this.c.textDim);
      }

      this.scaled(
         ctx,
         this.drag == BaulooStylesConfigScreen.Drag.COS_TURN ? "Turning ..." : "Click and drag to turn",
         left + 8,
         bottom - 14,
         overPreview ? this.c.textSoft : this.c.textDim,
         0.8F,
         false
      );
      int gx = left + pw + 12;
      int gw = right - gx;
      int sy = y0;
      int slots = Cosmetics.Slot.values().length;
      int slotW = (gw - (slots - 1) * 6) / slots;

      for (int i = 0; i < slots; i++) {
         Cosmetics.Slot slot = Cosmetics.Slot.values()[i];
         int sx = gx + i * (slotW + 6);
         boolean on = slot == cosSlot;
         boolean hover = inside(mx, my, sx, sy, slotW, 26);
         soft(ctx, sx, sy, slotW, 26, on ? this.c.surface2 : (hover ? this.c.surface : this.c.base));
         if (on) {
            soft(ctx, sx, sy + 24, slotW, 2, this.c.accent);
         }

         this.text(ctx, this.fitText(slot.title.toUpperCase(Locale.ROOT), slotW - 8), sx + 6, sy + 4, on ? this.c.text : this.c.textSoft);
         String worn = Cosmetics.label(slot, Cosmetics.saved(slot));
         this.scaled(
            ctx, this.fitText(worn, Math.round((slotW - 8) / 0.75F)), sx + 6, sy + 15, "None".equals(worn) ? this.c.textDim : this.c.accentBright, 0.75F, false
         );
         this.hit(sx, sy, slotW, 26, (b, px, py) -> cosSlot = slot);
      }

      String[][] items = Cosmetics.of(cosSlot);
      int cardsTop = sy + 34;
      boolean capePage = cosSlot == Cosmetics.Slot.CAPE || cosSlot == Cosmetics.Slot.MC_CAPE;
      int optionsH = capePage ? 76 : 44;
      int cardsBottom = bottom - optionsH;
      int gap = 6;
      int cols = gw >= 420 ? 5 : (gw >= 300 ? 4 : 3);

      while (true) {
         int cw = (gw - (cols - 1) * gap) / cols;
         int rows = (items.length + 1 + cols - 1) / cols;
         int ch = Math.min(cw - 10, (cardsBottom - cardsTop - (rows - 1) * gap) / rows - 11);
         if (ch >= 30 || cols >= 9) {
            ch = Math.max(24, ch);
            String saved = Cosmetics.saved(cosSlot);

            for (int i = 0; i <= items.length; i++) {
               String key = i == 0 ? "none" : items[i - 1][0];
               String label = i == 0 ? "None" : items[i - 1][1];
               int cx = gx + i % cols * (cw + gap);
               int cy = cardsTop + i / cols * (ch + 11 + gap);
               if (cy + ch > cardsBottom + 4) {
                  break;
               }

               boolean hover = inside(mx, my, cx, cy, cw, ch + 11);
               boolean worn = key.equals(saved) || i == 0 && !Cosmetics.valid(cosSlot, saved);
               soft(ctx, cx, cy, cw, ch, hover ? this.c.surfaceHover : this.c.surface);
               if (i == 0) {
                  this.textCenter(ctx, "-", cx, cw, cy + ch / 2 - 4, this.c.textDim);
               } else {
                  this.drawCosmeticIcon(ctx, cosSlot, key, cx + 3, cy + 3, cw - 6, ch - 6);
               }

               if (worn) {
                  softLine(ctx, cx - 1, cy - 1, cw + 2, ch + 2, this.c.accent);
                  soft(ctx, cx + 4, cy + 4, 5, 5, -9183056);
               } else if (hover) {
                  softLine(ctx, cx - 1, cy - 1, cw + 2, ch + 2, this.c.textSoft);
               }

               this.textCenter(ctx, this.fitText(label, cw), cx, cw, cy + ch + 2, worn ? this.c.accentBright : (hover ? this.c.text : this.c.textSoft));
               if (hover) {
                  Cosmetics.tryOn(cosSlot, key);
               }

               Cosmetics.Slot slot = cosSlot;
               this.hit(cx, cy, cw, ch + 11, (b, px, py) -> Cosmetics.save(slot, key));
            }

            int oy = bottom - optionsH + 6;
            ctx.fill(gx, oy - 4, gx + gw, oy - 3, this.c.borderDim);
            this.text(ctx, "COLOR", gx, oy + 2, this.c.textDim);
            int swX = gx + this.textWidth("COLOR") + 10;
            int swW = Math.max(10, Math.min(18, (gw - (swX - gx) - 70 - 28) / 8));

            for (int i = 0; i < PICK_SWATCHES.length; i++) {
               int sx = swX + i * (swW + 4);
               int swatch = PICK_SWATCHES[i];
               boolean chosen = !StylesUiConfig.cosRainbow && (StylesUiConfig.cosColor & 16777215) == swatch;
               soft(ctx, sx, oy, swW, 12, 0xFF000000 | swatch);
               if (chosen || inside(mx, my, sx, oy, swW, 12)) {
                  softLine(ctx, sx - 1, oy - 1, swW + 2, 14, chosen ? this.c.text : this.c.textDim);
               }

               this.hit(sx, oy, swW, 12, (b, px, py) -> {
                  StylesUiConfig.cosColor = swatch;
                  StylesUiConfig.cosRainbow = false;
                  StylesUiConfig.save();
               });
            }

            int rainX = swX + 8 * (swW + 4) + 6;
            int rainW = right - rainX;
            if (rainW >= 50) {
               this.switchRow(
                  ctx,
                  rainX,
                  oy + 1,
                  Math.min(90, rainW),
                  "Rainbow",
                  StylesUiConfig.cosRainbow,
                  mx,
                  my,
                  () -> StylesUiConfig.cosRainbow = !StylesUiConfig.cosRainbow
               );
            }

            this.scaled(ctx, "Tints halo, orbit, ring, sparkles, ears, hat band and recolorable capes", gx, oy + 18, this.c.textDim, 0.7F, false);
            if (capePage) {
               this.switchRow(
                  ctx,
                  gx,
                  oy + 30,
                  Math.min(gw, 200),
                  "Cape on the elytra too",
                  StylesUiConfig.cosCapeElytra,
                  mx,
                  my,
                  () -> StylesUiConfig.cosCapeElytra = !StylesUiConfig.cosCapeElytra
               );
               this.switchRow(
                  ctx,
                  gx,
                  oy + 44,
                  Math.min(gw, 200),
                  "Recolor cape",
                  StylesUiConfig.cosCapeTint,
                  mx,
                  my,
                  () -> StylesUiConfig.cosCapeTint = !StylesUiConfig.cosCapeTint
               );
            }

            return;
         }

         cols++;
      }
   }

   private void drawCosmeticIcon(GuiGraphicsExtractor ctx, Cosmetics.Slot slot, String key, int x, int y, int w, int h) {
      Identifier icon = Cosmetics.icon(slot, key);
      if (slot == Cosmetics.Slot.MC_CAPE) {
         if (icon == null) {
            this.textCenter(ctx, "...", x, w, y + h / 2 - 4, this.c.textDim);
         } else {
            int tw = Cosmetics.MC_WIDTH.getOrDefault(key, 64);
            int u = tw / 64;
            int iw = Math.round(h * 10 / 16.0F);
            ctx.blit(RenderPipelines.GUI_TEXTURED, icon, x + (w - iw) / 2, y, u, u, iw, h, 10 * u, 16 * u, tw, tw / 2);
         }
      } else if (slot == Cosmetics.Slot.CAPE && "color".equals(key)) {
         int iw = Math.round(h * 20 / 32.0F);
         ctx.blit(RenderPipelines.GUI_TEXTURED, icon, x + (w - iw) / 2, y, 2.0F, 2.0F, iw, h, 20, 32, 128, 64);
      } else {
         int tw = slot == Cosmetics.Slot.CAPE ? 80 : 128;
         int th = 128;
         float k = Math.min((float)w / tw, (float)h / th);
         int iw = Math.round(tw * k);
         int ih = Math.round(th * k);
         ctx.blit(RenderPipelines.GUI_TEXTURED, icon, x + (w - iw) / 2, y + (h - ih) / 2, 0.0F, 0.0F, iw, ih, tw, th, tw, th);
      }
   }

   private static int[] guiRect(GuiGraphicsExtractor ctx, int x1, int y1, int x2, int y2) {
      Vector2f a = ctx.pose().transformPosition(x1, y1, new Vector2f());
      Vector2f b = ctx.pose().transformPosition(x2, y2, new Vector2f());
      return new int[]{Math.round(a.x), Math.round(a.y), Math.round(b.x), Math.round(b.y)};
   }

   private static float guiScale(GuiGraphicsExtractor ctx) {
      Matrix3x2f m = ctx.pose();
      return (float)Math.sqrt(m.m00() * m.m00() + m.m01() * m.m01());
   }

   private static void drawCosmeticPlayer(GuiGraphicsExtractor ctx, LivingEntity entity, int x1, int y1, int x2, int y2, float yaw) {
      EntityRenderState state = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity).createRenderState(entity, 1.0F);
      state.lightCoords = 15728880;
      state.shadowPieces.clear();
      state.outlineColor = 0;
      if (state instanceof LivingEntityRenderState living) {
         living.bodyRot = yaw;
         living.yRot = 0.0F;
         living.xRot = 0.0F;
         living.boundingBoxWidth = living.boundingBoxWidth / living.scale;
         living.boundingBoxHeight = living.boundingBoxHeight / living.scale;
         living.scale = 1.0F;
      }

      float size = Math.min((float)(x2 - x1), (y2 - y1) / 2.3F) * 0.95F;
      Quaternionf camera = new Quaternionf().rotateX(0.12F);
      Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI).mul(camera);
      OutlinePreview.hideName = true;

      try {
         int[] box = guiRect(ctx, x1, y1, x2, y2);
         ctx.entity(
            state, size * guiScale(ctx), new Vector3f(0.0F, state.boundingBoxHeight / 2.0F + 0.1F, 0.0F), rotation, camera, box[0], box[1], box[2], box[3]
         );
      } finally {
         OutlinePreview.hideName = false;
      }
   }

   private int switchRow(GuiGraphicsExtractor ctx, int px, int y, int pw, String label, boolean on, int mx, int my, Runnable toggle) {
      int swW = 20;
      int swH = 10;
      int sx = px + pw - swW;
      boolean hover = inside(mx, my, px, y - 2, pw, 14);
      this.text(ctx, this.fitText(label, pw - swW - 6), px, y + 1, !on && !hover ? this.c.textSoft : this.c.text);
      soft(ctx, sx, y, swW, swH, on ? this.c.accentDark : this.c.base);
      softLine(ctx, sx, y, swW, swH, on ? this.c.accent : this.c.borderDim);
      soft(ctx, on ? sx + swW - 8 : sx + 2, y + 2, 6, 6, on ? this.c.accent : this.c.textDim);
      this.hit(px, y - 2, pw, 14, (b, x2, y2) -> {
         toggle.run();
         StylesUiConfig.save();
      });
      return y + 18;
   }

   private void savePreset() {
      StylesPresets.add(this.presetDraft, this.active, StylesUiConfig.extras());
      this.presetNaming = false;
      this.presetDraft = "";
   }

   private boolean presetIsActive(StylesPresets.Preset preset) {
      for (BaulooStylesClient.Tab value : BaulooStylesClient.Tab.values()) {
         if (!Objects.equals(preset.styles().get(value), this.active.get(value))) {
            return false;
         }
      }

      return true;
   }

   private void drawPresets(GuiGraphicsExtractor ctx, int px, int top, int pw, int bottom, int mx, int my) {
      int y = top;
      String[] labels = new String[]{"MY LOOKS", "PUBLIC"};
      int tx0 = px;

      for (int i = 0; i < 2; i++) {
         boolean on = i == 1 == presetsPublic;
         int lw = this.textWidth(labels[i]);
         boolean hover = inside(mx, my, tx0 - 3, y - 3, lw + 6, 16);
         this.text(ctx, labels[i], tx0, y, on ? this.c.accent : (hover ? this.c.text : this.c.textDim));
         if (on) {
            soft(ctx, tx0, y + 11, lw, 2, this.c.accent);
         }

         boolean toPublic = i == 1;
         this.hit(tx0 - 3, y - 3, lw + 6, 16, (b, x2, y2) -> {
            presetsPublic = toPublic;
            this.presetNaming = false;
            if (toPublic) {
               StylesPublicPresets.refresh();
            }
         });
         tx0 += lw + 16;
      }

      y += 20;
      if (!publicAsked) {
         publicAsked = true;
         StylesPublicPresets.refresh();
      }

      if (presetsPublic) {
         this.drawPublicPresets(ctx, px, y, pw, bottom, mx, my);
      } else {
         String[] steps = new String[]{"Pick styles", "Save look", "Share it"};
         int stepW = (pw - 8) / 3;

         for (int i = 0; i < 3; i++) {
            int sx = px + i * (stepW + 4);
            soft(ctx, sx, y, stepW, 18, this.c.base);
            soft(ctx, sx + 3, y + 4, 10, 10, this.c.accent);
            this.scaled(ctx, String.valueOf(i + 1), sx + 6, y + 6, this.c.onAccent, 0.8F, false);
            this.scaled(ctx, this.fitText(steps[i], Math.round((stepW - 18) / 0.75F)), sx + 16, y + 6, this.c.textSoft, 0.75F, false);
         }

         y += 24;
         if (this.presetNaming) {
            this.scaled(ctx, "Name your look:", px, y, this.c.textDim, 0.8F, false);
            y += 10;
            soft(ctx, px, y, pw, 17, this.c.surface2);
            soft(ctx, px + 2, y + 15, pw - 4, 1, this.c.accent);
            boolean blink = System.currentTimeMillis() / 500L % 2L == 0L;
            this.text(ctx, this.fitText(this.presetDraft + (blink ? "_" : ""), pw - 10), px + 5, y + 5, this.c.text);
            y += 21;
            int half = (pw - 4) / 2;
            boolean saveHover = inside(mx, my, px, y, half, 16);
            this.button(ctx, px, y, half, 16, "SAVE", saveHover, BaulooStylesConfigScreen.ButtonStyle.PRIMARY);
            this.hit(px, y, half, 16, (b, x2, y2) -> this.savePreset());
            boolean cancelHover = inside(mx, my, px + half + 4, y, pw - half - 4, 16);
            this.button(ctx, px + half + 4, y, pw - half - 4, 16, "CANCEL", cancelHover, BaulooStylesConfigScreen.ButtonStyle.NORMAL);
            this.hit(px + half + 4, y, pw - half - 4, 16, (b, x2, y2) -> this.presetNaming = false);
            y += 22;
         } else {
            boolean hover = inside(mx, my, px, y, pw, 20);
            this.button(ctx, px, y, pw, 20, "+  SAVE MY LOOK", hover, BaulooStylesConfigScreen.ButtonStyle.PRIMARY);
            this.hit(px, y, pw, 20, (b, x2, y2) -> {
               this.presetNaming = true;
               this.presetDraft = StylesPresets.nextName();
               this.searchFocused = false;
               this.hexFocused = false;
            });
            y += 26;
         }

         String status = StylesPublicPresets.status();
         if (!status.isEmpty()) {
            y += this.statusLines(ctx, status, px, y, pw, false);
         }

         int listBottom = bottom;
         if (lastUsedLook != null && StylesUiConfig.hasBackup()) {
            int barY = bottom - 20;
            soft(ctx, px, barY, pw, 20, StylesTheme.withAlpha(-9183056, 48));
            int undoW = this.textWidth("UNDO") + 12;
            this.scaled(ctx, this.fitText("Using \"" + lastUsedLook + "\"", Math.round((pw - undoW - 12) / 0.8F)), px + 5, barY + 7, -9183056, 0.8F, false);
            boolean undoHover = inside(mx, my, px + pw - undoW - 3, barY + 3, undoW, 14);
            this.button(ctx, px + pw - undoW - 3, barY + 3, undoW, 14, "UNDO", undoHover, BaulooStylesConfigScreen.ButtonStyle.NORMAL);
            this.hit(px + pw - undoW - 3, barY + 3, undoW, 14, (b, x2, y2) -> {
               Map<String, String> before = StylesUiConfig.extras();
               if (StylesUiConfig.restoreBackup()) {
                  this.refreshExtras(before, true);
               }

               lastUsedLook = null;
            });
            listBottom = barY - 6;
         }

         List<StylesPresets.Preset> presets = StylesPresets.all();
         if (presets.isEmpty()) {
            this.text(ctx, this.fitText("No looks saved yet", pw), px, y + 4, this.c.textSoft);
            this.scaled(ctx, this.fitText("Pick your styles, then SAVE MY LOOK", Math.round(pw / 0.8F)), px, y + 17, this.c.textDim, 0.8F, false);
         } else {
            int cardH = 56;
            int btnW = (pw - 12 - 12) / 4;
            ctx.enableScissor(px - 2, y, px + pw + 2, listBottom);

            for (StylesPresets.Preset preset : presets) {
               if (y > listBottom) {
                  break;
               }

               boolean wearing = this.presetIsActive(preset);
               StylesPublicPresets.Shared shared = sharedCopy(preset);
               soft(ctx, px, y, pw, cardH, wearing ? this.c.surface2 : this.c.base);
               if (wearing) {
                  softLine(ctx, px, y, pw, cardH, this.c.accent);
               }

               int nameW = pw - 12 - (shared != null ? 40 : 0) - (wearing ? 50 : 0);
               String name = this.fitText(preset.name(), nameW);
               this.text(ctx, name, px + 6, y + 5, this.c.text);
               if (wearing) {
                  this.scaled(ctx, "WEARING", px + 10 + this.textWidth(name), y + 6, -9183056, 0.75F, false);
               }

               if (shared != null) {
                  int bw = this.scaledWidth("PUBLIC", 0.75F) + 8;
                  soft(ctx, px + pw - 6 - bw, y + 4, bw, 10, this.c.accentDark);
                  this.scaled(ctx, "PUBLIC", px + pw - 2 - bw, y + 6, this.c.accentBright, 0.75F, false);
               }

               this.drawPresetThumbs(ctx, preset.styles(), px + 6, y + 17, px + pw - 6);
               int by = y + cardH - 18;
               int ux = px + 6;
               boolean useHover = inside(mx, my, ux, by, btnW, 14) && my < listBottom;
               this.button(ctx, ux, by, btnW, 14, "USE", useHover, BaulooStylesConfigScreen.ButtonStyle.PRIMARY);
               this.hits.add(new BaulooStylesConfigScreen.Hit(ux, by, btnW, 14, top, listBottom, (b, x2, y2) -> this.askPreset(preset)));
               int sx = ux + btnW + 4;
               boolean shareHover = inside(mx, my, sx, by, btnW, 14) && my < listBottom;
               String shareLabel = preset.locked() ? "LOCKED" : (shared == null ? "SHARE" : (shareHover ? "UNSHARE" : "SHARED"));
               this.button(ctx, sx, by, btnW, 14, shareLabel, shareHover, BaulooStylesConfigScreen.ButtonStyle.NORMAL);
               this.hits.add(new BaulooStylesConfigScreen.Hit(sx, by, btnW, 14, top, listBottom, (b, x2, y2) -> {
                  if (preset.locked()) {
                     StylesPublicPresets.say("A preset from a key can't be shared on");
                  } else if (shared == null) {
                     this.confirmShare(preset, false);
                  } else {
                     StylesPublicPresets.remove(shared);
                  }
               }));
               int ex = sx + btnW + 4;
               boolean exportHover = inside(mx, my, ex, by, btnW, 14) && my < listBottom;
               boolean vip = VipPacks.shown();
               this.button(
                  ctx,
                  ex,
                  by,
                  btnW,
                  14,
                  PresetExport.busy ? "..." : "EXPORT",
                  exportHover,
                  vip ? BaulooStylesConfigScreen.ButtonStyle.NORMAL : BaulooStylesConfigScreen.ButtonStyle.DISABLED
               );
               if (exportHover) {
                  this.tooltip = vip ? "Saves this look as a texture pack in your resourcepacks folder" : "VIP only: saves this look as a texture pack";
                  this.tooltipX = mx + 8;
                  this.tooltipY = my + 10;
               }

               this.hits.add(new BaulooStylesConfigScreen.Hit(ex, by, btnW, 14, top, listBottom, (b, x2, y2) -> exportPreset(preset)));
               int dx = ex + btnW + 4;
               boolean delHover = inside(mx, my, dx, by, btnW, 14) && my < listBottom;
               this.button(ctx, dx, by, btnW, 14, "DELETE", delHover, BaulooStylesConfigScreen.ButtonStyle.NORMAL);
               this.hits.add(new BaulooStylesConfigScreen.Hit(dx, by, btnW, 14, top, listBottom, (b, x2, y2) -> StylesPresets.remove(preset)));
               y += cardH + 5;
            }

            ctx.disableScissor();
         }
      }
   }

   private int statusLines(GuiGraphicsExtractor ctx, String status, int x, int y, int w, boolean right) {
      String[] lines = status.split("\n");

      for (int i = 0; i < lines.length; i++) {
         String line = this.fitText(lines[i], w);
         int color = i == 0 ? -9183056 : this.c.textSoft;
         if (right) {
            this.textRight(ctx, line, x + w, y + i * 11, color);
         } else {
            this.text(ctx, line, x, y + i * 11, color);
         }
      }

      return lines.length * 11 + 1;
   }

   private static void exportPreset(StylesPresets.Preset preset) {
      if (!PresetExport.busy) {
         if (!VipPacks.shown()) {
            StylesPublicPresets.say("Export is for VIPs - get VIP in bauloo's Discord");
         } else {
            StylesPublicPresets.say("Exporting \"" + preset.name() + "\" ...");
            PresetExport.export(preset).thenAccept(msg -> {
               StylesPublicPresets.say(msg);
               Path folder = PresetExport.lastFolder;
               if (folder != null) {
                  Minecraft.getInstance().execute(() -> Util.getPlatform().openPath(folder));
               }
            });
         }
      }
   }

   private static StylesPublicPresets.Shared sharedCopy(StylesPresets.Preset preset) {
      for (StylesPublicPresets.Shared shared : StylesPublicPresets.all()) {
         if (shared.mine() && shared.name().equals(preset.name())) {
            return shared;
         }
      }

      return null;
   }

   private void drawPublicView(GuiGraphicsExtractor ctx, BaulooStylesConfigScreen.Layout l, int mx, int my) {
      int left = l.mainX;
      int right = l.side ? l.sideX + l.sideW : l.mainX + l.mainW;
      int y = l.bodyY + 8;
      if (!publicAsked) {
         publicAsked = true;
         StylesPublicPresets.refresh();
      }

      this.text(ctx, "Presets", left, y + 3, this.c.text);
      int tx = left + this.textWidth("Presets") + 18;
      String[] labels = new String[]{"MY LOOKS", "PUBLIC"};

      for (int i = 0; i < 2; i++) {
         boolean on = i == 1 == presetsPublic;
         int lw = this.textWidth(labels[i]);
         boolean hover = inside(mx, my, tx - 3, y, lw + 6, 16);
         this.text(ctx, labels[i], tx, y + 3, on ? this.c.accent : (hover ? this.c.text : this.c.textDim));
         if (on) {
            soft(ctx, tx, y + 14, lw, 2, this.c.accent);
         }

         boolean toPublic = i == 1;
         this.hit(tx - 3, y, lw + 6, 16, (b, x2, y2) -> {
            presetsPublic = toPublic;
            this.presetNaming = false;
            this.publicScroll = 0.0;
            StylesPublicPresets.refresh();
         });
         tx += lw + 16;
      }

      if (!presetsPublic) {
         int saveW = 150;
         int sx = right - saveW;
         if (this.keyEntering) {
            int fieldW = saveW + 60;
            int fx = right - fieldW - 116;
            soft(ctx, fx, y - 1, fieldW, 17, this.c.surface2);
            soft(ctx, fx + 2, y + 14, fieldW - 4, 1, this.c.accent);
            boolean blink = System.currentTimeMillis() / 500L % 2L == 0L;
            String shownKey = this.keyDraft.isEmpty() && !blink ? "Paste the key (Ctrl+V)" : this.keyDraft + (blink ? "_" : "");
            this.text(ctx, this.fitText(shownKey, fieldW - 10), fx + 5, y + 4, this.keyDraft.isEmpty() ? this.c.textDim : this.c.text);
            boolean addHover = inside(mx, my, right - 112, y - 1, 54, 17);
            this.button(ctx, right - 112, y - 1, 54, 17, "ADD", addHover, BaulooStylesConfigScreen.ButtonStyle.PRIMARY);
            this.hit(right - 112, y - 1, 54, 17, (b, x2, y2) -> this.redeemKey());
            boolean cancelHover = inside(mx, my, right - 54, y - 1, 54, 17);
            this.button(ctx, right - 54, y - 1, 54, 17, "CANCEL", cancelHover, BaulooStylesConfigScreen.ButtonStyle.NORMAL);
            this.hit(right - 54, y - 1, 54, 17, (b, x2, y2) -> this.keyEntering = false);
         } else if (this.presetNaming) {
            int fieldW = saveW + 60;
            int fx = right - fieldW - 116;
            soft(ctx, fx, y - 1, fieldW, 17, this.c.surface2);
            soft(ctx, fx + 2, y + 14, fieldW - 4, 1, this.c.accent);
            boolean blink = System.currentTimeMillis() / 500L % 2L == 0L;
            this.text(ctx, this.fitText(this.presetDraft + (blink ? "_" : ""), fieldW - 10), fx + 5, y + 4, this.c.text);
            boolean saveHover = inside(mx, my, right - 112, y - 1, 54, 17);
            this.button(ctx, right - 112, y - 1, 54, 17, "SAVE", saveHover, BaulooStylesConfigScreen.ButtonStyle.PRIMARY);
            this.hit(right - 112, y - 1, 54, 17, (b, x2, y2) -> this.savePreset());
            boolean cancelHover = inside(mx, my, right - 54, y - 1, 54, 17);
            this.button(ctx, right - 54, y - 1, 54, 17, "CANCEL", cancelHover, BaulooStylesConfigScreen.ButtonStyle.NORMAL);
            this.hit(right - 54, y - 1, 54, 17, (b, x2, y2) -> this.presetNaming = false);
         } else {
            boolean hover = inside(mx, my, sx, y - 2, saveW, 19);
            this.button(ctx, sx, y - 2, saveW, 19, "+  SAVE MY LOOK", hover, BaulooStylesConfigScreen.ButtonStyle.PRIMARY);
            this.hit(sx, y - 2, saveW, 19, (b, x2, y2) -> {
               this.presetNaming = true;
               this.presetDraft = StylesPresets.nextName();
               this.searchFocused = false;
               this.hexFocused = false;
            });
            int kw = this.textWidth("PRESET KEY") + 20;
            int kx = sx - 6 - kw;
            boolean keyHover = inside(mx, my, kx, y - 2, kw, 19);
            this.button(ctx, kx, y - 2, kw, 19, "PRESET KEY", keyHover, BaulooStylesConfigScreen.ButtonStyle.NORMAL);
            this.hit(kx, y - 2, kw, 19, (b, x2, y2) -> {
               this.keyEntering = true;
               this.keyDraft = "";
               this.searchFocused = false;
               this.hexFocused = false;
            });
            if (keyHover) {
               this.tooltip = "Add a preset a friend sent you as a key";
               this.tooltipX = mx + 8;
               this.tooltipY = my + 10;
            }
         }
      }

      y += 26;
      if (!presetsPublic) {
         String[] steps = new String[]{"Pick your styles", "SAVE MY LOOK", "SHARE it with everyone"};
         int sx = left;

         for (int i = 0; i < 3; i++) {
            int sw = this.textWidth(steps[i]) + 28;
            soft(ctx, sx, y, sw, 18, this.c.surface);
            soft(ctx, sx + 4, y + 4, 10, 10, this.c.accent);
            this.scaled(ctx, String.valueOf(i + 1), sx + 7, y + 6, this.c.onAccent, 0.8F, false);
            this.text(ctx, steps[i], sx + 19, y + 5, this.c.textSoft);
            sx += sw + 6;
         }
      } else {
         this.text(ctx, "Looks other players shared. Press USE to try one - your Extras are saved first.", left, y + 5, this.c.textDim);
      }

      String status = StylesPublicPresets.status();
      if (!status.isEmpty()) {
         int half = (right - left) / 2;
         this.statusLines(ctx, status, right - half, y + 2, half, true);
      }

      y += 26;
      int listBottom = l.footerY - 10;
      if (lastUsedLook != null && StylesUiConfig.hasBackup()) {
         int barY = listBottom - 20;
         soft(ctx, left, barY, right - left, 20, StylesTheme.withAlpha(-9183056, 48));
         this.text(ctx, this.fitText("Using \"" + lastUsedLook + "\" - your own Extras are saved", right - left - 80), left + 8, barY + 6, -9183056);
         int undoW = 60;
         boolean undoHover = inside(mx, my, right - undoW - 4, barY + 3, undoW, 14);
         this.button(ctx, right - undoW - 4, barY + 3, undoW, 14, "UNDO", undoHover, BaulooStylesConfigScreen.ButtonStyle.NORMAL);
         this.hit(right - undoW - 4, barY + 3, undoW, 14, (b, x2, y2) -> {
            Map<String, String> before = StylesUiConfig.extras();
            if (StylesUiConfig.restoreBackup()) {
               this.refreshExtras(before, true);
            }

            lastUsedLook = null;
         });
         listBottom = barY - 8;
      }

      List<String> names = new ArrayList<>();
      List<String> subs = new ArrayList<>();
      List<Map<BaulooStylesClient.Tab, String>> looks = new ArrayList<>();
      List<Boolean> mine = new ArrayList<>();
      List<List<BaulooStylesConfigScreen.CardButton>> buttons = new ArrayList<>();
      if (presetsPublic) {
         for (StylesPublicPresets.Shared shared : StylesPublicPresets.all()) {
            names.add(shared.name());
            subs.add("by " + shared.author());
            looks.add(shared.styles());
            mine.add(shared.mine());
            List<BaulooStylesConfigScreen.CardButton> row = new ArrayList<>();
            row.add(new BaulooStylesConfigScreen.CardButton("USE", BaulooStylesConfigScreen.ButtonStyle.PRIMARY, () -> this.useShared(shared)));
            if (shared.mine() || StylesPublicPresets.admin()) {
               row.add(new BaulooStylesConfigScreen.CardButton("REMOVE", BaulooStylesConfigScreen.ButtonStyle.NORMAL, () -> StylesPublicPresets.remove(shared)));
            }

            buttons.add(row);
         }
      } else {
         for (StylesPresets.Preset preset : StylesPresets.all()) {
            StylesPublicPresets.Shared shared = sharedCopy(preset);
            boolean wearing = this.presetIsActive(preset);
            names.add(preset.name());
            String from = preset.author() != null ? "by " + preset.author() : null;
            subs.add(
               wearing
                  ? "WEARING" + (shared != null ? "  -  PUBLIC" : (from != null ? "  -  " + from : ""))
                  : (shared != null ? "PUBLIC" : (from != null ? from : "Only on this PC"))
            );
            looks.add(preset.styles());
            mine.add(wearing);
            List<BaulooStylesConfigScreen.CardButton> row = new ArrayList<>();
            row.add(new BaulooStylesConfigScreen.CardButton("USE", BaulooStylesConfigScreen.ButtonStyle.PRIMARY, () -> this.askPreset(preset)));
            if (!preset.locked()) {
               row.add(
                  shared == null
                     ? new BaulooStylesConfigScreen.CardButton("SHARE", BaulooStylesConfigScreen.ButtonStyle.NORMAL, () -> this.confirmShare(preset, false))
                     : new BaulooStylesConfigScreen.CardButton("UNSHARE", BaulooStylesConfigScreen.ButtonStyle.NORMAL, () -> StylesPublicPresets.remove(shared))
               );
               row.add(new BaulooStylesConfigScreen.CardButton("KEY", BaulooStylesConfigScreen.ButtonStyle.NORMAL, () -> this.confirmShare(preset, true)));
            }

            row.add(
               new BaulooStylesConfigScreen.CardButton(
                  PresetExport.busy ? "..." : "EXPORT",
                  VipPacks.shown() ? BaulooStylesConfigScreen.ButtonStyle.NORMAL : BaulooStylesConfigScreen.ButtonStyle.DISABLED,
                  () -> exportPreset(preset)
               )
            );
            row.add(new BaulooStylesConfigScreen.CardButton("DELETE", BaulooStylesConfigScreen.ButtonStyle.NORMAL, () -> StylesPresets.remove(preset)));
            buttons.add(row);
         }
      }

      this.publicTop = y;
      this.publicBottom = listBottom;
      this.publicLeft = left;
      this.publicRight = right;
      if (names.isEmpty()) {
         if (presetsPublic) {
            this.text(ctx, StylesPublicPresets.loading() ? "Loading..." : "Nothing shared yet", left, y + 6, this.c.textSoft);
         } else {
            this.text(ctx, "No looks saved yet", left, y + 6, this.c.textSoft);
            this.text(ctx, "Pick your styles on the left, then press SAVE MY LOOK", left, y + 20, this.c.textDim);
         }
      } else {
         int gap = 8;
         int cardW = 190;
         int cols = Math.max(1, (right - left + gap) / (cardW + gap));
         cardW = (right - left - gap * (cols - 1)) / cols;
         int thumb = 22;
         int cardH = 30 + 2 * (thumb + 4) + 24;
         int rows = (names.size() + cols - 1) / cols;
         this.publicMaxScroll = Math.max(0, rows * (cardH + gap) - (this.publicBottom - this.publicTop));
         this.publicScroll = Math.max(0.0, Math.min(this.publicScroll, (double)this.publicMaxScroll));
         ctx.enableScissor(left, this.publicTop, right, this.publicBottom);
         this.hitClipTop = this.publicTop;
         this.hitClipBottom = this.publicBottom;

         for (int i = 0; i < names.size(); i++) {
            int cx = left + i % cols * (cardW + gap);
            int cy = this.publicTop - (int)this.publicScroll + i / cols * (cardH + gap);
            if (cy <= this.publicBottom && cy + cardH >= this.publicTop) {
               boolean highlight = mine.get(i);
               soft(ctx, cx, cy, cardW, cardH, highlight ? this.c.surface2 : this.c.surface);
               if (highlight) {
                  softLine(ctx, cx, cy, cardW, cardH, this.c.accent);
               }

               this.text(ctx, this.fitText(names.get(i), cardW - 12), cx + 6, cy + 5, this.c.text);
               String sub = subs.get(i);
               int subColor = sub.startsWith("WEARING")
                  ? -9183056
                  : (!sub.startsWith("PUBLIC") && (!sub.startsWith("by") || !highlight) ? this.c.textDim : this.c.accentBright);
               this.scaled(ctx, this.fitText(sub, Math.round((cardW - 12) / 0.8F)), cx + 6, cy + 16, subColor, 0.8F, false);
               BaulooStylesClient.Tab[] tabs = shownTabs();
               int perRow = (tabs.length + 1) / 2;

               for (int t = 0; t < tabs.length; t++) {
                  int ttx = cx + 6 + t % perRow * (thumb + 4);
                  int tty = cy + 30 + t / perRow * (thumb + 4);
                  if (ttx + thumb <= cx + cardW - 4) {
                     String folder = looks.get(i).get(tabs[t]);
                     if (folder == null) {
                        soft(ctx, ttx, tty, thumb, thumb, this.c.base);
                     } else {
                        this.drawPreview(ctx, folder, ttx, tty, thumb, thumb);
                        cutCorners(ctx, ttx, tty, thumb, thumb, highlight ? this.c.surface2 : this.c.surface);
                     }

                     if (inside(mx, my, ttx, tty, thumb, thumb) && my >= this.publicTop && my < this.publicBottom) {
                        this.tooltip = tabs[t].title + ": " + (folder == null ? "Vanilla" : folder);
                        this.tooltipX = mx + 8;
                        this.tooltipY = my + 8;
                     }
                  }
               }

               List<BaulooStylesConfigScreen.CardButton> row = buttons.get(i);
               int by = cy + cardH - 20;
               int bw = (cardW - 12 - (row.size() - 1) * 4) / row.size();

               for (int k = 0; k < row.size(); k++) {
                  BaulooStylesConfigScreen.CardButton cb = row.get(k);
                  int bx = cx + 6 + k * (bw + 4);
                  boolean hover = inside(mx, my, bx, by, bw, 15) && my >= this.publicTop && my < this.publicBottom;
                  this.button(ctx, bx, by, bw, 15, cb.label(), hover, cb.style());
                  this.hit(bx, by, bw, 15, (b, x2, y2) -> cb.action().run());
               }
            }
         }

         this.hitClipTop = Integer.MIN_VALUE;
         this.hitClipBottom = Integer.MAX_VALUE;
         ctx.disableScissor();
      }
   }

   private void drawPresetThumbs(GuiGraphicsExtractor ctx, Map<BaulooStylesClient.Tab, String> styles, int x, int y, int maxX) {
      for (BaulooStylesClient.Tab value : BaulooStylesClient.Tab.values()) {
         if (x + 12 > maxX) {
            break;
         }

         String folder = styles.get(value);
         if (folder == null) {
            soft(ctx, x, y, 12, 12, this.c.surface);
         } else {
            this.drawPreview(ctx, folder, x, y, 12, 12);
         }

         x += 14;
      }
   }

   private void drawPublicPresets(GuiGraphicsExtractor ctx, int px, int top, int pw, int bottom, int mx, int my) {
      this.scaled(ctx, this.fitText("Looks other players shared. Press USE to try one.", Math.round(pw / 0.8F)), px, top, this.c.textDim, 0.8F, false);
      int y = top + 12;
      String status = StylesPublicPresets.status();
      if (!status.isEmpty()) {
         y += this.statusLines(ctx, status, px, y, pw, false);
      }

      y += 4;
      List<StylesPublicPresets.Shared> shared = StylesPublicPresets.all();
      if (shared.isEmpty()) {
         this.text(ctx, StylesPublicPresets.loading() ? "Loading..." : "Nothing shared yet", px, y, this.c.textSoft);
         this.scaled(ctx, this.fitText("Share yours with SHARE in MY LOOKS", Math.round(pw / 0.8F)), px, y + 13, this.c.textDim, 0.8F, false);
      } else {
         int cardH = 56;
         ctx.enableScissor(px - 2, y, px + pw + 2, bottom);

         for (StylesPublicPresets.Shared preset : shared) {
            if (y > bottom) {
               break;
            }

            soft(ctx, px, y, pw, cardH, this.c.base);
            this.text(ctx, this.fitText(preset.name(), pw - 12), px + 6, y + 5, this.c.text);
            this.scaled(
               ctx,
               this.fitText("by " + preset.author(), Math.round((pw - 12) / 0.8F)),
               px + 6,
               y + 16,
               preset.mine() ? this.c.accent : this.c.textSoft,
               0.8F,
               false
            );
            this.drawPresetThumbs(ctx, preset.styles(), px + 6, y + 27, px + pw - 60);
            int useW = 44;
            int ux = px + pw - useW - 6;
            int uy = y + cardH - 20;
            boolean useHover = inside(mx, my, ux, uy, useW, 14) && my < bottom;
            this.button(ctx, ux, uy, useW, 14, "USE", useHover, BaulooStylesConfigScreen.ButtonStyle.PRIMARY);
            this.hits.add(new BaulooStylesConfigScreen.Hit(ux, uy, useW, 14, top, bottom, (b, x2, y2) -> this.useShared(preset)));
            if (preset.mine()) {
               int rw = this.textWidth("REMOVE") + 10;
               int rx = ux - rw - 4;
               boolean remHover = inside(mx, my, rx, uy, rw, 14) && my < bottom;
               this.button(ctx, rx, uy, rw, 14, "REMOVE", remHover, BaulooStylesConfigScreen.ButtonStyle.NORMAL);
               this.hits.add(new BaulooStylesConfigScreen.Hit(rx, uy, rw, 14, top, bottom, (b, x2, y2) -> StylesPublicPresets.remove(preset)));
            }

            y += cardH + 5;
         }

         ctx.disableScissor();
      }
   }

   private void confirmShare(StylesPresets.Preset preset, boolean code) {
      int locals = PresetShare.localCount(preset);
      String confirm = preset.name() + (code ? "#key" : "#share");
      long now = System.currentTimeMillis();
      if (locals <= 0 || confirm.equals(this.shareConfirmKey) && now < this.shareConfirmUntil) {
         this.shareConfirmKey = null;
         if (!code) {
            StylesPublicPresets.share(preset);
         } else {
            StylesPublicPresets.say("Making a key...");
            Thread thread = new Thread(() -> {
               try {
                  String key = PresetShare.makeCode(preset, StylesPublicPresets::say);
                  Minecraft.getInstance().execute(() -> {
                     Minecraft.getInstance().keyboardHandler.setClipboard(key);
                     StylesPublicPresets.say("Key " + key + " copied - works once, for 7 days");
                  });
               } catch (Exception var2x) {
                  StylesPublicPresets.say(var2x.getMessage() == null ? "Could not reach the server" : var2x.getMessage());
               }
            }, "Bauloo Styles preset key");
            thread.setDaemon(true);
            thread.start();
         }
      } else {
         this.shareConfirmKey = confirm;
         this.shareConfirmUntil = now + 8000L;
         StylesPublicPresets.say(locals + (locals == 1 ? " style" : " styles") + " from your PC get uploaded with it - click again");
      }
   }

   private void redeemKey() {
      String key = this.keyDraft.trim();
      this.keyEntering = false;
      if (!key.isEmpty()) {
         StylesPublicPresets.say("Checking the key...");
         Thread thread = new Thread(() -> {
            try {
               String name = PresetShare.redeem(key, StylesPublicPresets::say);
               StylesPublicPresets.say("Added \"" + name + "\" to your looks");
            } catch (Exception var2x) {
               StylesPublicPresets.say(var2x.getMessage() == null ? "Could not reach the server" : var2x.getMessage());
            }
         }, "Bauloo Styles preset key");
         thread.setDaemon(true);
         thread.start();
      }
   }

   private void useShared(StylesPublicPresets.Shared shared) {
      if (shared.local().isEmpty()) {
         this.askPreset(new StylesPresets.Preset(shared.name(), shared.styles(), shared.extras()));
      } else {
         Thread thread = new Thread(() -> {
            try {
               Map<BaulooStylesClient.Tab, String> styles = new EnumMap<>(BaulooStylesClient.Tab.class);
               styles.putAll(shared.styles());
               styles.putAll(PresetShare.install(shared.local(), StylesPublicPresets::say));
               StylesPublicPresets.say("");
               Minecraft.getInstance().execute(() -> {
                  if (Minecraft.getInstance().gui.screen() == this) {
                     this.askPreset(new StylesPresets.Preset(shared.name(), styles, shared.extras()));
                  }
               });
            } catch (Exception var3) {
               StylesPublicPresets.say(var3.getMessage() == null ? "Could not reach the server" : var3.getMessage());
            }
         }, "Bauloo Styles shared styles");
         thread.setDaemon(true);
         thread.start();
      }
   }

   private void askPreset(StylesPresets.Preset preset) {
      this.extrasStatus = "";
      StylesUiConfig.presetBackup = true;
      if (preset.extras().isEmpty()) {
         lastUsedLook = null;
         this.applyPreset(preset, false);
      } else {
         this.pendingPreset = preset;
         this.pendingRemember = false;
      }
   }

   private void choosePreset(boolean withExtras) {
      StylesPresets.Preset preset = this.pendingPreset;
      this.pendingPreset = null;
      lastUsedLook = withExtras ? preset.name() : null;
      if (this.pendingRemember) {
         StylesUiConfig.presetExtras = withExtras ? "with" : "without";
         StylesUiConfig.save();
      }

      this.applyPreset(preset, withExtras);
   }

   private void drawExtrasAsk(GuiGraphicsExtractor ctx, int mx, int my) {
      this.hits.clear();
      this.tooltip = null;
      ctx.fill(0, 0, this.width, this.height, -1342177280);
      int w = Math.min(300, this.width - 20);
      int h = 104;
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      soft(ctx, x, y, w, h, this.c.surface);
      softLine(ctx, x, y, w, h, this.c.accent);
      this.text(ctx, this.fitText("Use \"" + this.pendingPreset.name() + "\"", w - 24), x + 12, y + 10, this.c.text);
      this.text(ctx, this.fitText("Copy its Extras too? (colors, outline, glint, ...)", w - 24), x + 12, y + 28, this.c.textSoft);
      this.scaled(
         ctx, this.fitText("Your own Extras are saved first - UNDO brings them back.", Math.round((w - 24) / 0.8F)), x + 12, y + 42, -9183056, 0.8F, false
      );
      int bw = (w - 24 - 8) / 3;
      int by = y + 68;
      String[] labels = new String[]{"WITH EXTRAS", "STYLES ONLY", "CANCEL"};

      for (int i = 0; i < 3; i++) {
         int bx = x + 12 + i * (bw + 4);
         boolean hover = inside(mx, my, bx, by, bw, 20);
         this.button(ctx, bx, by, bw, 20, labels[i], hover, i == 0 ? BaulooStylesConfigScreen.ButtonStyle.PRIMARY : BaulooStylesConfigScreen.ButtonStyle.NORMAL);
         int choice = i;
         this.hit(bx, by, bw, 20, (b, x2, y2) -> {
            if (choice == 2) {
               this.pendingPreset = null;
            } else {
               this.choosePreset(choice == 0);
            }
         });
      }
   }

   private void drawUpdateAsk(GuiGraphicsExtractor ctx, int mx, int my) {
      this.hits.clear();
      this.tooltip = null;
      ctx.fill(0, 0, this.width, this.height, -1342177280);
      int w = Math.min(300, this.width - 20);
      List<String[]> notes = StylesUpdater.availableNotes;
      int notesH = notes.isEmpty() ? 0 : 20 + notes.size() * 13 + 8;
      int h = 112 + notesH;
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      soft(ctx, x, y, w, h, this.c.surface);
      softLine(ctx, x, y, w, h, this.c.accent);
      ctx.blit(RenderPipelines.GUI_TEXTURED, TORNADO, x + 12, y + 10, 0.0F, 0.0F, 20, 20, 240, 240, 240, 240, this.c.accent);
      this.text(ctx, this.fitText("New version of Bauloo Styles", w - 50), x + 38, y + 11, this.c.text);
      String have = (StylesUpdater.installedVersion == null ? "?" : StylesUpdater.installedVersion) + "  ->  ";
      this.text(ctx, have, x + 38, y + 23, this.c.textDim);
      this.text(ctx, this.updateAsk, x + 38 + this.textWidth(have), y + 23, this.c.accent);
      this.text(ctx, this.fitText("Get it from the website and put it into", w - 24), x + 12, y + 44, this.c.textSoft);
      this.text(ctx, this.fitText("your mods folder instead of the old one.", w - 24), x + 12, y + 56, this.c.textSoft);
      if (!notes.isEmpty()) {
         soft(ctx, x + 10, y + 68, w - 20, notesH - 4, this.c.base);
         this.scaled(ctx, "WHAT'S NEW", x + 16, y + 73, this.c.accent, 1.3F, false);
         int ny = y + 92;

         for (String[] note : notes) {
            int chip = "FIX".equals(note[0]) ? -12454 : ("NEW".equals(note[0]) ? this.c.accent : this.c.surface2);
            int cw = this.scaledWidth(note[0], 0.7F) + 8;
            soft(ctx, x + 14, ny + 1, cw, 9, chip);
            this.scaled(ctx, note[0], x + 18, ny + 2, "GONE".equals(note[0]) ? this.c.text : -16117996, 0.7F, false);
            this.scaled(ctx, this.fitText(note[1], Math.round((w - 40 - cw) / 0.85F)), x + 20 + cw, ny + 1.5F, this.c.text, 0.85F, false);
            ny += 13;
         }
      }

      int inner = w - 24;
      int small = (inner - 8) / 4;
      int by = y + 80 + notesH;
      int[] bw = new int[]{inner - 4 - small, small};
      String[] labels = new String[]{"DOWNLOAD", "LATER"};
      int[] choices = new int[]{1, 2};
      int bx = x + 12;

      for (int i = 0; i < labels.length; i++) {
         boolean hover = inside(mx, my, bx, by, bw[i], 20);
         this.button(
            ctx, bx, by, bw[i], 20, labels[i], hover, i == 0 ? BaulooStylesConfigScreen.ButtonStyle.PRIMARY : BaulooStylesConfigScreen.ButtonStyle.NORMAL
         );
         int choice = choices[i];
         this.hit(bx, by, bw[i], 20, (b, x2, y2) -> this.answerUpdate(choice));
         bx += bw[i] + 4;
      }
   }

   private List<String> wrap(String value, int width, float scale) {
      List<String> lines = new ArrayList<>();
      String line = "";

      for (String word : value.split(" ")) {
         String next = line.isEmpty() ? word : line + " " + word;
         if (this.scaledWidth(next, scale) > width && !line.isEmpty()) {
            lines.add(line);
            line = word;
         } else {
            line = next;
         }
      }

      if (!line.isEmpty()) {
         lines.add(line);
      }

      return lines;
   }

   private void drawWhatsNew(GuiGraphicsExtractor ctx, int mx, int my) {
      this.hits.clear();
      this.tooltip = null;
      WhatsNew.Notes n = this.whatsNew;
      ctx.fill(0, 0, this.width, this.height, -1342177280);
      int w = Math.min(330, this.width - 20);
      boolean heroPic = !n.heroCape().isEmpty() && BuiltInCapes.icon(n.heroCape()) != null;
      int heroH = heroPic ? 70 : 24 + this.wrap(n.heroText(), w - 36, 0.8F).size() * 9;
      int rowH = 15;
      int h = 46 + heroH + 8 + 18 + n.items().size() * rowH + 34;
      int x = (this.width - w) / 2;
      int y = Math.max(6, (this.height - h) / 2);
      soft(ctx, x, y, w, h, this.c.surface);
      softLine(ctx, x, y, w, h, this.c.accent);
      ctx.blit(RenderPipelines.GUI_TEXTURED, TORNADO, x + 12, y + 11, 0.0F, 0.0F, 22, 22, 240, 240, 240, 240, this.c.accent);
      this.text(ctx, this.fitText("Bauloo Styles updated", w - 120), x + 42, y + 12, this.c.text);
      String from = n.from().isEmpty() ? "" : n.from() + "  ->  ";
      this.scaled(ctx, from, x + 42, y + 25, this.c.textDim, 0.8F, false);
      this.scaled(ctx, n.to(), x + 42 + this.scaledWidth(from, 0.8F), y + 25, this.c.accent, 0.8F, false);
      String badge = "INSTALLED";
      int bw = this.scaledWidth(badge, 0.7F) + 10;
      soft(ctx, x + w - 12 - bw, y + 13, bw, 11, -9183056);
      this.scaled(ctx, badge, x + w - 7 - bw, y + 15, -16378866, 0.7F, false);
      int hy = y + 42;
      soft(ctx, x + 10, hy, w - 20, heroH, this.c.base);
      Identifier cape = n.heroCape().isEmpty() ? null : BuiltInCapes.icon(n.heroCape());
      if (cape != null) {
         ctx.blit(RenderPipelines.GUI_TEXTURED, cape, x + 16, hy + 6, 0.0F, 0.0F, 36, 58, 36, 58, 36, 58);
      }

      int tx = cape != null ? x + 60 : x + 18;
      int tw = x + w - 18 - tx;
      this.text(ctx, this.fitText(n.heroTitle(), tw), tx, hy + 7, this.c.text);
      int ly = hy + 20;

      for (String line : this.wrap(n.heroText(), tw, 0.8F)) {
         if (ly > hy + heroH - 9) {
            break;
         }

         this.scaled(ctx, line, tx, ly, this.c.textSoft, 0.8F, false);
         ly += 9;
      }

      this.scaled(ctx, "WHAT'S NEW", x + 14, hy + heroH + 8, this.c.accent, 1.3F, false);
      int ry = hy + heroH + 6 + 18;

      for (String[] item : n.items()) {
         ctx.fill(x + 10, ry - 2, x + w - 10, ry - 1, this.c.borderDim);
         String toExtras = item[0];

         int chipColor = switch (toExtras) {
            case "NEW" -> this.c.accent;
            case "FIX" -> -12454;
            default -> this.c.surface2;
         };
         int chipText = "GONE".equals(item[0]) ? this.c.text : -16117996;
         int cw = this.scaledWidth(item[0], 0.7F) + 8;
         soft(ctx, x + 12, ry + 1, cw, 9, chipColor);
         this.scaled(ctx, item[0], x + 16, ry + 2, chipText, 0.7F, false);
         this.scaled(ctx, this.fitText(item[1], Math.round((w - 40 - cw) / 0.85F)), x + 18 + cw, ry + 1.5F, this.c.text, 0.85F, false);
         ry += rowH;
      }

      int by = y + h - 28;
      int half = (w - 24 - 4) / 2;
      boolean hoverOpen = inside(mx, my, x + 12, by, half, 20);
      boolean toExtras = "extras".equals(n.open());
      boolean toMyPacks = "mypacks".equals(n.open());
      BaulooStylesClient.Tab toTab = null;

      for (BaulooStylesClient.Tab t : shownTabs()) {
         if (t.prefix.equals(n.open())) {
            toTab = t;
         }
      }

      BaulooStylesClient.Tab target = toTab;
      String openLabel = toMyPacks
         ? "OPEN MY PACKS"
         : (target != null ? "OPEN " + target.title.toUpperCase(Locale.ROOT) : (toExtras ? "OPEN EXTRAS" : "OPEN COSMETICS"));
      this.button(ctx, x + 12, by, half, 20, this.fitText(openLabel, half - 8), hoverOpen, BaulooStylesConfigScreen.ButtonStyle.PRIMARY);
      this.hit(x + 12, by, half, 20, (b, x2, y2) -> {
         this.whatsNew = null;
         if (toMyPacks) {
            myPacksView = true;
            vipView = false;
            cosmeticsView = false;
            extrasView = false;
            publicView = false;
            this.gridScroll = 0.0;
            this.focusedIndex = 0;
            this.rebuild();
         } else if (target != null) {
            cosmeticsView = false;
            this.switchTab(target);
         } else {
            cosmeticsView = !toExtras;
            extrasView = toExtras;
            publicView = false;
            this.searchFocused = false;
         }
      });
      boolean hoverOk = inside(mx, my, x + 16 + half, by, half, 20);
      this.button(ctx, x + 16 + half, by, half, 20, "GOT IT", hoverOk, BaulooStylesConfigScreen.ButtonStyle.NORMAL);
      this.hit(x + 16 + half, by, half, 20, (b, x2, y2) -> this.whatsNew = null);
   }

   private void drawResetAsk(GuiGraphicsExtractor ctx, int mx, int my) {
      this.hits.clear();
      this.tooltip = null;
      ctx.fill(0, 0, this.width, this.height, -1342177280);
      int w = Math.min(300, this.width - 20);
      int h = 112;
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      soft(ctx, x, y, w, h, this.c.surface);
      softLine(ctx, x, y, w, h, -42386);
      this.text(ctx, "Reset everything?", x + 12, y + 10, this.c.text);
      this.text(ctx, this.fitText("All styles go back to vanilla, every Extra", w - 24), x + 12, y + 28, this.c.textSoft);
      this.text(ctx, this.fitText("and cosmetic back to default.", w - 24), x + 12, y + 40, this.c.textSoft);
      this.scaled(ctx, this.fitText("Your saved looks, menu theme and hotkey stay.", Math.round((w - 24) / 0.8F)), x + 12, y + 55, -9183056, 0.8F, false);
      this.scaled(ctx, this.fitText("UNDO brings your Extras back.", Math.round((w - 24) / 0.8F)), x + 12, y + 65, -9183056, 0.8F, false);
      int bw = (w - 24 - 6) / 2;
      int by = y + 80;
      boolean hoverReset = inside(mx, my, x + 12, by, bw, 20);
      soft(ctx, x + 12, by, bw, 20, hoverReset ? -37248 : -2078628);
      this.textCenter(ctx, "RESET EVERYTHING", x + 12, bw, by + 6, -1);
      this.hit(x + 12, by, bw, 20, (b, x2, y2) -> this.resetEverything());
      int cx = x + 12 + bw + 6;
      this.button(ctx, cx, by, bw, 20, "CANCEL", inside(mx, my, cx, by, bw, 20), BaulooStylesConfigScreen.ButtonStyle.NORMAL);
      this.hit(cx, by, bw, 20, (b, x2, y2) -> this.resetAsk = false);
   }

   private void resetEverything() {
      this.resetAsk = false;
      StylesUiConfig.cosCape = "none";
      StylesUiConfig.cosHead = "none";
      StylesUiConfig.cosAura = "none";
      StylesUiConfig.cosColor = 11766015;
      StylesUiConfig.cosRainbow = false;
      StylesUiConfig.cosCapeElytra = true;
      StylesUiConfig.presetBackup = true;
      StylesUiConfig.save();
      lastUsedLook = "Reset";
      this.applyPreset(new StylesPresets.Preset("Reset", new EnumMap<>(BaulooStylesClient.Tab.class), Map.of()), true);
   }

   private void answerUpdate(int choice) {
      String version = this.updateAsk;
      this.updateAsk = null;
      StylesUiConfig.updateSeen = version;
      StylesUiConfig.save();
      if (choice == 1 && this.minecraft != null) {
         ConfirmLinkScreen.confirmLinkNow(this, "https://baulooprac.xyz", true);
      }
   }

   private void checkbox(GuiGraphicsExtractor ctx, int x, int y, String label, boolean on, int mx, int my, Runnable toggle) {
      int w = 14 + (int)(this.textWidth(label) * 0.8F);
      boolean hover = inside(mx, my, x - 2, y - 2, w + 4, 13);
      soft(ctx, x, y, 9, 9, on ? this.c.accent : this.c.surface2);
      if (!on) {
         softLine(ctx, x, y, 9, 9, hover ? this.c.accent : this.c.border);
      }

      this.scaled(ctx, label, x + 13, y + 1, hover ? this.c.text : this.c.textSoft, 0.8F, false);
      this.hit(x - 2, y - 2, w + 4, 13, (b, x2, y2) -> toggle.run());
   }

   private void refreshExtras(Map<String, String> before, boolean reloadPacks) {
      EnchantGlint.applyLive();
      FireColor.applyLive();
      ShieldColor.applyLive();
      Map<String, String> after = StylesUiConfig.extras();
      boolean packs = false;

      for (String key : List.of("anchorGlowCustom", "anchorGlowColor", "anchorShowFull", "coreCustom", "coreColor")) {
         packs |= !Objects.equals(before.get(key), after.get(key));
      }

      if (packs && reloadPacks) {
         GeneratedPacks.applyAll();
      }
   }

   private void tellExtras(String name) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         String what = "Reset".equals(name) ? "§bEverything is reset." : "§b" + name + " §7is on.";
         mc.player.sendSystemMessage(Component.literal(what + " §fUNDO §7in MY LOOKS puts your Extras back."));
      }
   }

   private void applyPreset(StylesPresets.Preset preset, boolean withExtras) {
      List<CompletableFuture<Boolean>> downloads = new ArrayList<>();

      for (String folder : preset.styles().values()) {
         StylesCloud.Style cloud = StylesCloud.byFolder(folder);
         if (cloud != null && !StylesCloud.isReady(cloud)) {
            downloads.add(StylesCloud.download(cloud));
         }
      }

      if (!downloads.isEmpty()) {
         CompletableFuture.allOf(downloads.toArray(new CompletableFuture[0])).whenComplete((ok, error) -> Minecraft.getInstance().schedule(() -> {
            if (Minecraft.getInstance().gui.screen() == this) {
               this.applyPreset(preset, withExtras);
            }
         }));
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (withExtras) {
            Map<String, String> before = StylesUiConfig.extras();
            StylesUiConfig.applyPresetExtras(preset.extras());
            this.refreshExtras(before, false);
            this.tellExtras(preset.name());
         }

         PackRepository packs = mc.getResourcePackRepository();
         packs.reload();
         List<String> enabled = new ArrayList<>(packs.getSelectedIds());

         for (BaulooStylesClient.Tab value : BaulooStylesClient.Tab.values()) {
            if (value == BaulooStylesClient.Tab.SKY) {
            }

            enabled.removeIf(id -> isPackOf(value, id));
            String folderx = preset.styles().get(value);
            if (folderx != null && packs.getAvailableIds().contains(packId(folderx))) {
               enabled.add(packId(folderx));
            }
         }

         packs.setSelected(enabled);
         mc.options.updateResourcePacks(packs);
         mc.reloadResourcePacks();
         BaulooStylesConfigScreen next = new BaulooStylesConfigScreen(this.parent);
         next.tab = this.tab;
         next.query = this.query;
         next.favoritesOnly = this.favoritesOnly;
         next.rebuild();
         mc.gui.setScreen(next);
      }
   }

   private void drawThemes(GuiGraphicsExtractor ctx, int px, int top, int pw, int bottom, int mx, int my, int background) {
      this.themeLeft = px;
      this.themeRight = px + pw;
      this.themeTop = top;
      this.themeBottom = bottom;
      int viewH = bottom - top;
      int y0 = top - (int)this.themeScroll;
      int clipTop = top;
      int clipBottom = bottom;
      ctx.enableScissor(px - 2, top, px + pw + 2, bottom);
      this.text(ctx, "PRESET", px, y0, this.c.textDim);
      int y = y0 + 13;
      int presetW = (pw - 4) / 2;
      int presetH = 15;

      for (int i = 0; i < StylesTheme.PRESETS.length; i++) {
         StylesTheme.Preset preset = StylesTheme.PRESETS[i];
         int cx = px + i % 2 * (presetW + 4);
         int cy = y + i / 2 * (presetH + 3);
         boolean selected = (StylesUiConfig.accent & 16777215) == preset.accent() && StylesUiConfig.background == preset.background();
         boolean hover = inside(mx, my, cx, cy, presetW, presetH) && my >= clipTop && my < clipBottom;
         soft(ctx, cx, cy, presetW, presetH, selected ? this.c.surface2 : (hover ? this.c.surfaceHover : this.c.base));
         if (selected) {
            softLine(ctx, cx, cy, presetW, presetH, this.c.accentDark);
         }

         soft(ctx, cx + 4, cy + 4, 7, 7, 0xFF000000 | preset.accent());
         this.text(ctx, this.fitText(preset.name(), presetW - 18), cx + 15, cy + 4, selected ? this.c.text : this.c.textSoft);
         this.hits.add(new BaulooStylesConfigScreen.Hit(cx, cy, presetW, presetH, clipTop, clipBottom, (b, x2, y2) -> {
            StylesUiConfig.accent = preset.accent();
            StylesUiConfig.background = preset.background();
            this.hexFocused = false;
            StylesUiConfig.save();
         }));
      }

      y += (StylesTheme.PRESETS.length + 1) / 2 * (presetH + 3) + 8;
      this.text(ctx, "ACCENT", px, y, this.c.textDim);
      y += 13;
      int hueH = 6;

      for (int i = 0; i < pw; i++) {
         ctx.fill(px + i, y, px + i + 1, y + hueH, 0xFF000000 | StylesTheme.hsv((float)i / Math.max(1, pw - 1), 0.65F, 1.0F));
      }

      cutCorners(ctx, px, y, pw, hueH, background);
      float hue = StylesTheme.toHsv(StylesUiConfig.accent)[0];
      int knobX = px + Math.round(hue * (pw - 1));
      soft(ctx, knobX - 2, y - 2, 4, hueH + 4, -16777216);
      ctx.fill(knobX - 1, y - 1, knobX + 1, y + hueH + 1, -1);
      this.hits.add(new BaulooStylesConfigScreen.Hit(px, y - 4, pw, hueH + 8, clipTop, clipBottom, (b, x2, y2) -> {
         this.drag = BaulooStylesConfigScreen.Drag.HUE;
         this.dragX = px;
         this.dragW = pw;
         this.updateDrag(x2);
      }));
      y += hueH + 7;
      boolean fieldHover = inside(mx, my, px, y, pw, 15) && my >= clipTop && my < clipBottom;
      soft(ctx, px, y, pw, 15, !this.hexFocused && !fieldHover ? this.c.base : this.c.surface2);
      if (this.hexFocused) {
         soft(ctx, px + 2, y + 13, pw - 4, 1, this.c.accent);
      }

      soft(ctx, px + 4, y + 4, 7, 7, this.c.accent);
      boolean blink = this.hexFocused && System.currentTimeMillis() / 500L % 2L == 0L;
      String hex = this.hexFocused ? this.hexDraft + (blink ? "_" : "") : String.format("%06X", StylesUiConfig.accent & 16777215);
      this.text(ctx, "#" + hex, px + 15, y + 4, this.hexFocused ? this.c.text : this.c.textSoft);
      this.hits.add(new BaulooStylesConfigScreen.Hit(px, y, pw, 15, clipTop, clipBottom, (b, x2, y2) -> {
         this.hexFocused = true;
         this.hexDraft = "";
      }));
      y += 23;
      this.text(ctx, "OPACITY", px, y, this.c.textDim);
      this.textRight(ctx, StylesUiConfig.opacity + "%", px + pw, y, this.c.textSoft);
      y += 14;
      float t = (StylesUiConfig.opacity - 60) / 40.0F;
      int fillX = px + Math.round(t * (pw - 1));
      soft(ctx, px, y + 1, pw, 4, this.c.base);
      soft(ctx, px, y + 1, Math.max(3, fillX - px), 4, this.c.accentDark);
      soft(ctx, fillX - 2, y - 2, 5, 10, this.c.accent);
      this.hits.add(new BaulooStylesConfigScreen.Hit(px, y - 4, pw, 14, clipTop, clipBottom, (b, x2, y2) -> {
         this.drag = BaulooStylesConfigScreen.Drag.OPACITY;
         this.dragX = px;
         this.dragW = pw;
         this.updateDrag(x2);
      }));
      y += 18;
      boolean resetHover = inside(mx, my, px, y, 50, 15) && my >= clipTop && my < clipBottom;
      this.button(ctx, px, y, 50, 15, "RESET", resetHover, BaulooStylesConfigScreen.ButtonStyle.NORMAL);
      this.hits.add(new BaulooStylesConfigScreen.Hit(px, y, 50, 15, clipTop, clipBottom, (b, x2, y2) -> {
         StylesUiConfig.resetTheme();
         this.hexFocused = false;
      }));
      y += 15;
      ctx.disableScissor();
      int contentH = y - y0;
      this.themeMaxScroll = Math.max(0, contentH - viewH);
      this.themeScroll = Math.max(0.0, Math.min(this.themeScroll, (double)this.themeMaxScroll));
      if (this.themeMaxScroll > 0) {
         int trackX = px + pw + 4;
         soft(ctx, trackX, top, 2, viewH, this.c.borderDim);
         int thumbH = Math.max(16, viewH * viewH / contentH);
         int thumbY = top + (int)((viewH - thumbH) * (this.themeScroll / this.themeMaxScroll));
         soft(ctx, trackX, thumbY, 2, thumbH, this.c.accent);
      }
   }

   private void drawFooter(GuiGraphicsExtractor ctx, BaulooStylesConfigScreen.Layout l, int mx, int my) {
      int y = l.footerY;
      ctx.fill(l.fx + 10, y, l.fx + l.fw - 10, y + 1, this.c.borderDim);
      int by = y + 6;
      int bh = 16;
      int x = l.fx + 10;
      int uploadsW = this.textWidth("UPLOAD") + 16;
      this.button(ctx, x, by, uploadsW, bh, "UPLOAD", inside(mx, my, x, by, uploadsW, bh), BaulooStylesConfigScreen.ButtonStyle.NORMAL);
      this.hit(x, by, uploadsW, bh, (b, px, py) -> StylesUploadServer.open());
      x += uploadsW + 6;
      int presetsW = this.textWidth("PRESETS") + 16;
      this.button(
         ctx,
         x,
         by,
         presetsW,
         bh,
         "PRESETS",
         inside(mx, my, x, by, presetsW, bh),
         publicView ? BaulooStylesConfigScreen.ButtonStyle.PRIMARY : BaulooStylesConfigScreen.ButtonStyle.NORMAL
      );
      this.hit(x, by, presetsW, bh, (b, px, py) -> {
         publicView = !publicView;
         presetsView = false;
         this.presetNaming = false;
         this.compactThemes = false;
         this.hexFocused = false;
         if (publicView) {
            extrasView = false;
            cosmeticsView = false;
            StylesPublicPresets.refresh();
         }
      });
      x += presetsW + 6;
      if (!l.side) {
         String label = this.compactThemes ? "STYLES" : "THEMES";
         int tw = this.textWidth(label) + 16;
         this.button(
            ctx,
            x,
            by,
            tw,
            bh,
            label,
            inside(mx, my, x, by, tw, bh),
            this.compactThemes ? BaulooStylesConfigScreen.ButtonStyle.PRIMARY : BaulooStylesConfigScreen.ButtonStyle.NORMAL
         );
         this.hit(x, by, tw, bh, (b, px, py) -> {
            this.compactThemes = !this.compactThemes;
            presetsView = false;
            this.hexFocused = false;
         });
         x += tw + 6;
      }

      int resetW = this.textWidth("RESET") + 16;
      this.button(ctx, x, by, resetW, bh, "RESET", inside(mx, my, x, by, resetW, bh), BaulooStylesConfigScreen.ButtonStyle.NORMAL);
      this.hit(x, by, resetW, bh, (b, px, py) -> this.resetAsk = true);
      x += resetW + 6;
      String key = this.bindingKey ? "PRESS A KEY" : "OPEN: " + StylesHotkey.label();
      this.hotkeyW = Math.max(70, this.textWidth(key) + 16);
      this.hotkeyX = l.fx + l.fw - 10 - this.hotkeyW;
      this.hotkeyY = by;
      this.button(
         ctx,
         this.hotkeyX,
         by,
         this.hotkeyW,
         bh,
         key,
         inside(mx, my, this.hotkeyX, by, this.hotkeyW, bh),
         this.bindingKey ? BaulooStylesConfigScreen.ButtonStyle.PRIMARY : BaulooStylesConfigScreen.ButtonStyle.NORMAL
      );
      this.hit(this.hotkeyX, by, this.hotkeyW, bh, (b, px, py) -> this.bindingKey = !this.bindingKey);
      String dc = "discord.gg/bauloo";
      int dcW = this.textWidth(dc) + 16;
      int dcX = this.hotkeyX - 6 - dcW;
      boolean dcShown = dcX > x + 4;
      if (dcShown) {
         boolean dcHover = inside(mx, my, dcX, by, dcW, bh);
         soft(ctx, dcX, by, dcW, bh, dcHover ? -9734155 : -10983950);
         ctx.text(this.font, dc, dcX + 8, by + (bh - 8) / 2, -1, true);
         this.hit(dcX, by, dcW, bh, (b, px, py) -> ConfirmLinkScreen.confirmLinkNow(this, "https://discord.gg/bauloo", true));
      }

      int edge = dcShown ? dcX : this.hotkeyX;
      if (StylesReview.shown()) {
         String fb = StylesReview.busy() ? "★ FEEDBACK ..." : "★ FEEDBACK";
         int fbW = this.textWidth(fb) + 16;
         int fbX = edge - 6 - fbW;
         if (fbX > x + 4) {
            boolean fbHover = inside(mx, my, fbX, by, fbW, bh);
            soft(ctx, fbX, by, fbW, bh, fbHover ? -11654 : -673731);
            ctx.text(this.font, fb, fbX + 8, by + (bh - 8) / 2, -14937083, false);
            this.hit(fbX, by, fbW, bh, (b, px, py) -> StylesReview.open());
            edge = fbX;
            String note = StylesReview.status();
            if (note != null) {
               int nw = this.scaledWidth(note, 0.8F);
               if (fbX - 8 - nw > x + 4) {
                  this.scaled(ctx, note, fbX - 8 - nw, by + 5, this.c.textSoft, 0.8F, false);
                  edge = fbX - 8 - nw;
               }
            }
         }
      }

      if (VipPacks.admin()) {
         boolean on = StylesUiConfig.earlyAccess;
         String ea = on ? "EARLY ACCESS ON" : "EARLY ACCESS OFF";
         int eaW = this.textWidth(ea) + 16;
         int eaX = edge - 6 - eaW;
         if (eaX > x + 4) {
            boolean eaHover = inside(mx, my, eaX, by, eaW, bh);
            if (on) {
               soft(ctx, eaX, by, eaW, bh, eaHover ? -9808608 : -11912170);
               softLine(ctx, eaX, by, eaW, bh, -15811);
            } else {
               soft(ctx, eaX, by, eaW, bh, eaHover ? this.c.surfaceHover : this.c.surface);
            }

            ctx.text(this.font, ea, eaX + 8, by + (bh - 8) / 2, on ? -15811 : this.c.textDim, false);
            this.hit(eaX, by, eaW, bh, (b, px, py) -> {
               StylesUiConfig.earlyAccess = !StylesUiConfig.earlyAccess;
               StylesUiConfig.save();
            });
            if (eaHover) {
               this.tooltip = on ? "Early styles show (VIP) - click to turn off" : "Click to see early styles (VIP)";
               this.tooltipX = mx + 8;
               this.tooltipY = my - 14;
            }

            edge = eaX;
         }
      }

      if (!PENDING.isEmpty()) {
         int n = PENDING.size();
         String applyLabel = this.pendingCloud != null && this.pendingCloud.startsWith("\u0000")
            ? "DOWNLOADING ..."
            : "APPLY " + n + (n == 1 ? " CHANGE" : " CHANGES");
         int aw = this.textWidth(applyLabel) + 16;
         int ax = edge - 6 - aw;
         boolean aHover = inside(mx, my, ax, by, aw, bh);
         soft(ctx, ax, by, aw, bh, aHover ? -11654 : -15811);
         ctx.text(this.font, applyLabel, ax + 8, by + (bh - 8) / 2, -14937083, false);
         this.hit(ax, by, aw, bh, (b, px, py) -> this.applyPending(true));
         int cw = this.textWidth("CANCEL") + 16;
         int cxBtn = ax - 6 - cw;
         this.button(ctx, cxBtn, by, cw, bh, "CANCEL", inside(mx, my, cxBtn, by, cw, bh), BaulooStylesConfigScreen.ButtonStyle.NORMAL);
         this.hit(cxBtn, by, cw, bh, (b, px, py) -> PENDING.clear());
         String info = n + (n == 1 ? " change selected" : " changes selected");
         if (this.textWidth(info) < cxBtn - 10 - (x + 10)) {
            this.textRight(ctx, info, cxBtn - 10, by + 4, -15811);
         }
      } else {
         String[] hints = new String[]{StylesUiConfig.applyInstantly ? "LEFT-CLICK APPLY" : "LEFT-CLICK SELECT", "RIGHT-CLICK ZOOM", "F FAVORITE", "/ SEARCH"};
         int left = x + 10;
         int right = edge - 10;
         int shown = hints.length;

         String line;
         for (line = ""; shown > 0; shown--) {
            line = String.join("  •  ", Arrays.copyOf(hints, shown));
            if (this.textWidth(line) <= right - left) {
               break;
            }
         }

         if (shown > 0) {
            this.textCenter(ctx, line, left, right - left, by + 4, this.c.textDim);
         }
      }
   }

   private void drawZoom(GuiGraphicsExtractor ctx) {
      ctx.fill(0, 0, this.width, this.height, StylesTheme.withAlpha(this.c.base, 238));
      int size = (int)(Math.min(this.width, this.height - 30) * 0.8F);
      int x = (this.width - size) / 2;
      int y = Math.max(8, (this.height - size) / 2 - 12);
      this.drawPreview(ctx, this.zoomFolder, x, y, size, size);
      cutCorners(ctx, x, y, size, size, StylesTheme.withAlpha(this.c.base, 238));
      softLine(ctx, x - 1, y - 1, size + 2, size + 2, this.c.accent);
      this.textCenter(ctx, this.fitText(this.zoomName, this.width - 16), 0, this.width, y + size + 8, this.c.text);
      this.textCenter(ctx, "Click anywhere to close", 0, this.width, y + size + 20, this.c.textDim);
   }

   private void toggleFavorite(String folder) {
      StylesUiConfig.toggleFavorite(folder);
      if (this.favoritesOnly) {
         this.applyFilter();
      }
   }

   private void updateDrag(double mouseX) {
      if (this.drag == BaulooStylesConfigScreen.Drag.COS_TURN) {
         this.cosYaw = ((this.cosDragYaw - (float)(mouseX - this.dragStartMouse) / Math.max(1, this.dragW) * 360.0F) % 360.0F + 360.0F) % 360.0F;
      } else {
         float t = (float)Math.max(0.0, Math.min(1.0, (mouseX - this.dragX) / Math.max(1, this.dragW - 1)));
         if (this.liveDrag()) {
            t = (float)Math.max(0.0, Math.min(1.0, this.dragStartT + (mouseX - this.dragStartMouse) / Math.max(1, this.dragW - 1)));
         }

         if (this.drag == BaulooStylesConfigScreen.Drag.HUE) {
            float[] hsv = StylesTheme.toHsv(StylesUiConfig.accent);
            float s = hsv[1] < 0.35F ? 0.65F : hsv[1];
            float v = hsv[2] < 0.75F ? 1.0F : hsv[2];
            StylesUiConfig.accent = StylesTheme.hsv(t, s, v);
         } else if (this.drag == BaulooStylesConfigScreen.Drag.OPACITY) {
            StylesUiConfig.opacity = Math.round(60.0F + t * 40.0F);
         } else if (this.drag == BaulooStylesConfigScreen.Drag.GRADIENT) {
            StylesUiConfig.gradientStrength = Math.round((50.0F + t * 350.0F) / 10.0F) * 10;
         } else if (this.drag == BaulooStylesConfigScreen.Drag.KILL_VOLUME) {
            StylesUiConfig.killVolume = Math.round((5.0F + t * 95.0F) / 5.0F) * 5;
         } else if (this.drag == BaulooStylesConfigScreen.Drag.TOTEM_POP) {
            StylesUiConfig.totemPopSize = Math.round((20.0F + t * 130.0F) / 5.0F) * 5;
         } else if (this.drag == BaulooStylesConfigScreen.Drag.CRYSTAL_SPIN) {
            StylesUiConfig.crystalSpin = Math.round(t * 1000.0F / 10.0F) * 10;
         } else if (this.drag == BaulooStylesConfigScreen.Drag.SHIELD_SIZE) {
            StylesUiConfig.shieldSize = Math.round((20.0F + t * 130.0F) / 5.0F) * 5;
         } else if (this.drag == BaulooStylesConfigScreen.Drag.TIME) {
            StylesUiConfig.timeOverride = Math.round(t * 23999.0F);
         } else if (this.drag == BaulooStylesConfigScreen.Drag.COBWEB_WIDTH) {
            StylesUiConfig.cobwebOutlineWidth = 1 + Math.round(t * 15.0F);
         } else if (this.drag == BaulooStylesConfigScreen.Drag.GAP_SIZE) {
            StylesUiConfig.gapSize = Math.round((20.0F + t * 130.0F) / 5.0F) * 5;
         } else if (this.drag == BaulooStylesConfigScreen.Drag.TOTEM) {
            StylesUiConfig.totemSize = Math.round((20.0F + t * 130.0F) / 5.0F) * 5;
         } else if (this.drag == BaulooStylesConfigScreen.Drag.SHIELD) {
            StylesUiConfig.shieldOpacity = Math.round(15.0F + t * 85.0F);
         } else if (this.drag == BaulooStylesConfigScreen.Drag.PICK_HUE) {
            huePreview = true;
            setPick(this.huePick, true, PlayerOutline.sliderColor(t));
         } else if (this.drag == BaulooStylesConfigScreen.Drag.OUTLINE_HUE) {
            StylesUiConfig.outlineColor = PlayerOutline.sliderColor(t);
            StylesUiConfig.outlineRainbow = false;
         } else if (this.drag == BaulooStylesConfigScreen.Drag.BLOCK_WIDTH) {
            StylesUiConfig.blockOutlineWidth = 1 + Math.round(t * 5.0F);
         } else if (this.drag == BaulooStylesConfigScreen.Drag.OUTLINE_WIDTH) {
            StylesUiConfig.outlineWidth = 1 + Math.round(t * 5.0F);
         }
      }
   }

   private void commitHex() {
      if (this.hexDraft.length() == 6) {
         try {
            StylesUiConfig.accent = Integer.parseInt(this.hexDraft, 16) & 16777215;
            StylesUiConfig.save();
         } catch (NumberFormatException var2) {
         }
      }

      this.hexFocused = false;
      this.hexDraft = "";
   }

   private boolean clickedUi(MouseButtonEvent click, boolean doubled) {
      double mouseX = click.x();
      double mouseY = click.y();
      int button = click.button();
      if (this.zoomFolder != null) {
         this.zoomFolder = null;
         this.zoomName = null;
         return true;
      } else if (this.bindingKey) {
         if (!inside(mouseX, mouseY, this.hotkeyX, this.hotkeyY, this.hotkeyW, 16)) {
            StylesHotkey.set(Type.MOUSE.getOrCreate(button));
         }

         this.bindingKey = false;
         return true;
      } else {
         this.searchFocused = false;
         this.socialFocused = false;
         if (this.hexFocused) {
            this.commitHex();
         }

         for (int i = this.hits.size() - 1; i >= 0; i--) {
            BaulooStylesConfigScreen.Hit h = this.hits.get(i);
            if (h.contains(mouseX, mouseY)) {
               h.action().run(button, mouseX, mouseY);
               return true;
            }
         }

         return super.mouseClicked(click, doubled);
      }
   }

   public void extractBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
      if (!this.liveDrag()) {
         super.extractBackground(ctx, mouseX, mouseY, delta);
      }
   }

   private boolean liveDrag() {
      return this.drag == BaulooStylesConfigScreen.Drag.TOTEM
         || this.drag == BaulooStylesConfigScreen.Drag.SHIELD_SIZE
         || this.drag == BaulooStylesConfigScreen.Drag.SHIELD
         || this.drag == BaulooStylesConfigScreen.Drag.GAP_SIZE
         || this.drag == BaulooStylesConfigScreen.Drag.TIME
         || this.drag == BaulooStylesConfigScreen.Drag.OUTLINE_HUE
         || this.drag == BaulooStylesConfigScreen.Drag.OUTLINE_WIDTH;
   }

   private void thirdPerson(boolean on) {
      Minecraft mc = Minecraft.getInstance();
      PlayerOutline.previewSelf = on;
      if (on) {
         if (this.outlineView == null) {
            this.outlineView = mc.options.getCameraType();
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
         }
      } else if (this.outlineView != null) {
         mc.options.setCameraType(this.outlineView);
         this.outlineView = null;
      }
   }

   private void drawLiveSlider(GuiGraphicsExtractor ctx) {
      this.c = StylesUiConfig.theme();
      if (this.drag == BaulooStylesConfigScreen.Drag.OUTLINE_HUE) {
         this.drawLiveHue(ctx);
      } else if (this.drag == BaulooStylesConfigScreen.Drag.OUTLINE_WIDTH) {
         this.drawLiveWidth(ctx);
      } else if (this.drag == BaulooStylesConfigScreen.Drag.TIME) {
         this.drawLiveTime(ctx);
      } else {
         this.thirdPerson(false);
         HandPreview.show(
            stackOf(
               this.drag == BaulooStylesConfigScreen.Drag.TOTEM
                  ? Items.TOTEM_OF_UNDYING
                  : (this.drag == BaulooStylesConfigScreen.Drag.GAP_SIZE ? Items.GOLDEN_APPLE : Items.SHIELD)
            )
         );
         String label;
         String value;
         float t;
         if (this.drag == BaulooStylesConfigScreen.Drag.SHIELD) {
            label = "SHIELD OPACITY";
            value = StylesUiConfig.shieldOpacity >= 100 ? "Normal" : StylesUiConfig.shieldOpacity + "%";
            t = (StylesUiConfig.shieldOpacity - 15) / 85.0F;
         } else {
            int size = this.drag == BaulooStylesConfigScreen.Drag.TOTEM
               ? StylesUiConfig.totemSize
               : (this.drag == BaulooStylesConfigScreen.Drag.GAP_SIZE ? StylesUiConfig.gapSize : StylesUiConfig.shieldSize);
            label = this.drag == BaulooStylesConfigScreen.Drag.TOTEM
               ? "TOTEM SIZE"
               : (this.drag == BaulooStylesConfigScreen.Drag.GAP_SIZE ? "GAP SIZE" : "SHIELD SIZE");
            value = size == 100 ? "Normal" : size + "%";
            t = (size - 20) / 130.0F;
         }

         int w = this.dragW;
         int x = this.dragX;
         int y = this.dragY;
         soft(ctx, x - 8, y - 22, w + 16, 36, StylesTheme.withAlpha(this.c.base, 208));
         this.text(ctx, label, x, y - 14, this.c.textDim);
         this.textRight(ctx, value, x + w, y - 14, this.c.text);
         int fill = x + Math.round(t * (w - 1));
         soft(ctx, x, y + 1, w, 4, this.c.surface);
         soft(ctx, x, y + 1, Math.max(3, fill - x), 4, this.c.accentDark);
         soft(ctx, fill - 2, y - 2, 5, 10, this.c.accent);
      }
   }

   private boolean draggedUi(MouseButtonEvent click, double offsetX, double offsetY) {
      if (this.scrollDrag != 0) {
         this.dragScroll(click.y());
         return true;
      } else if (this.drag != BaulooStylesConfigScreen.Drag.NONE) {
         this.updateDrag(click.x());
         return true;
      } else {
         return super.mouseDragged(click, offsetX, offsetY);
      }
   }

   private void drawLiveHue(GuiGraphicsExtractor ctx) {
      this.thirdPerson(true);
      HandPreview.clear();
      int w = this.dragW;
      int x = this.dragX;
      int y = this.dragY;
      soft(ctx, x - 8, y - 22, w + 16, 40, StylesTheme.withAlpha(this.c.base, 208));
      this.text(ctx, "OUTLINE COLOR", x, y - 14, this.c.textDim);
      int shown = StylesUiConfig.outlineRainbow ? PlayerOutline.color() : StylesUiConfig.outlineColor;
      this.textRight(ctx, String.format("#%06X", shown & 16777215), x + w, y - 14, 0xFF000000 | shown);

      for (int i = 0; i < w; i++) {
         ctx.fill(x + i, y, x + i + 1, y + 8, 0xFF000000 | PlayerOutline.sliderColor((float)i / Math.max(1, w - 1)));
      }

      int knob = x + Math.round(PlayerOutline.sliderPos(StylesUiConfig.outlineColor) * (w - 1));
      soft(ctx, knob - 2, y - 3, 5, 14, -16777216);
      ctx.fill(knob - 1, y - 2, knob + 1, y + 10, -1);
   }

   private void drawLiveWidth(GuiGraphicsExtractor ctx) {
      this.thirdPerson(true);
      HandPreview.clear();
      int w = this.dragW;
      int x = this.dragX;
      int y = this.dragY;
      int width = StylesUiConfig.outlineWidth;
      float t = (width - 1) / 5.0F;
      soft(ctx, x - 8, y - 22, w + 16, 36, StylesTheme.withAlpha(this.c.base, 208));
      this.text(ctx, "OUTLINE THICKNESS", x, y - 14, this.c.textDim);
      this.textRight(ctx, width == 2 ? "Normal" : String.valueOf(width), x + w, y - 14, this.c.text);
      int fill = x + Math.round(t * (w - 1));
      soft(ctx, x, y + 1, w, 4, this.c.surface);
      soft(ctx, x, y + 1, Math.max(3, fill - x), 4, this.c.accentDark);
      soft(ctx, fill - 2, y - 2, 5, 10, this.c.accent);
   }

   private boolean releasedUi(MouseButtonEvent click) {
      if (this.scrollDrag != 0) {
         this.scrollDrag = 0;
         return true;
      } else if (this.drag != BaulooStylesConfigScreen.Drag.NONE) {
         boolean volume = this.drag == BaulooStylesConfigScreen.Drag.KILL_VOLUME;
         if (this.drag == BaulooStylesConfigScreen.Drag.PICK_HUE) {
            huePreview = false;
            setPick(this.huePick, true, pickColor(this.huePick));
         }

         this.drag = BaulooStylesConfigScreen.Drag.NONE;
         this.thirdPerson(false);
         HandPreview.clear();
         StylesUiConfig.save();
         if (volume) {
            KillEffect.killSound();
         }

         return true;
      } else {
         return super.mouseReleased(click);
      }
   }

   private boolean scrolledUi(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (this.zoomFolder != null) {
         return true;
      } else if (publicView && mouseX >= this.publicLeft && mouseX < this.publicRight && mouseY >= this.publicTop && mouseY < this.publicBottom) {
         this.publicScroll = Math.max(0.0, Math.min((double)this.publicMaxScroll, this.publicScroll - verticalAmount * 18.0));
         return true;
      } else if (!extrasView
         || !(mouseX >= this.tileLeft)
         || !(mouseX < this.tileRight + 8)
         || !(mouseY >= this.tileTop)
         || !(mouseY < this.tileBottom)
         || extraOpen && this.tileLeft == this.extrasLeft) {
         if (extrasView && mouseX >= this.extrasLeft && mouseX < this.extrasRight && mouseY >= this.extrasTop && mouseY < this.extrasBottom) {
            this.extrasScroll = Math.max(0.0, Math.min((double)this.extrasMaxScroll, this.extrasScroll - verticalAmount * 18.0));
            return true;
         } else if (!(mouseX >= this.themeLeft)
            || !(mouseX < this.themeRight + 8)
            || !(mouseY >= this.themeTop)
            || !(mouseY < this.themeBottom)
            || this.themeMaxScroll <= 0
            || (!StylesUiConfig.themesTab || presetsView) && !this.compactThemes) {
            if (mouseX >= this.gridLeft && mouseX < this.gridRight + 8 && mouseY >= this.gridTop && mouseY < this.gridBottom && this.gridMaxScroll > 0) {
               this.gridScroll = Math.max(0.0, Math.min((double)this.gridMaxScroll, this.gridScroll - verticalAmount * 24.0));
               return true;
            } else {
               return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            }
         } else {
            this.themeScroll = Math.max(0.0, Math.min((double)this.themeMaxScroll, this.themeScroll - verticalAmount * 18.0));
            return true;
         }
      } else {
         this.tileScroll = Math.max(0.0, Math.min((double)this.tileMaxScroll, this.tileScroll - verticalAmount * 18.0));
         return true;
      }
   }

   public boolean keyPressed(KeyEvent input) {
      int key = input.key();
      if (this.bindingKey) {
         StylesHotkey.set(key == 256 ? InputConstants.UNKNOWN : Type.KEYSYM.getOrCreate(key));
         this.bindingKey = false;
         return true;
      } else if (this.zoomFolder != null) {
         if (key == 256) {
            this.zoomFolder = null;
            this.zoomName = null;
         }

         return true;
      } else if (this.pendingPreset != null) {
         if (key == 256) {
            this.pendingPreset = null;
         }

         return true;
      } else if (this.updateAsk != null) {
         if (key == 256) {
            this.answerUpdate(2);
         }

         return true;
      } else if (this.whatsNew != null) {
         if (key == 256 || key == 257) {
            this.whatsNew = null;
         }

         return true;
      } else if (this.resetAsk) {
         if (key == 256) {
            this.resetAsk = false;
         }

         return true;
      } else if (this.searchFocused) {
         if (key == 256) {
            if (this.query.isEmpty()) {
               this.searchFocused = false;
            } else {
               this.query = "";
               this.applyFilter();
            }
         } else if (key == 259 && !this.query.isEmpty()) {
            this.query = this.query.substring(0, this.query.length() - 1);
            this.gridScroll = 0.0;
            this.applyFilter();
         } else if (key == 257 || key == 335 || key == 258) {
            this.searchFocused = false;
         }

         return true;
      } else if (this.keyEntering) {
         if (key == 256) {
            this.keyEntering = false;
         } else if (input.isPaste()) {
            String pasted = Minecraft.getInstance().keyboardHandler.getClipboard().replaceAll("[^A-Za-z0-9-]", "");
            this.keyDraft = (this.keyDraft + pasted).toUpperCase(Locale.ROOT);
            if (this.keyDraft.length() > 12) {
               this.keyDraft = this.keyDraft.substring(0, 12);
            }
         } else if (key == 259 && !this.keyDraft.isEmpty()) {
            this.keyDraft = this.keyDraft.substring(0, this.keyDraft.length() - 1);
         } else if (key == 257 || key == 335) {
            this.redeemKey();
         }

         return true;
      } else if (this.presetNaming) {
         if (key == 256) {
            this.presetNaming = false;
         } else if (key == 259 && !this.presetDraft.isEmpty()) {
            this.presetDraft = this.presetDraft.substring(0, this.presetDraft.length() - 1);
         } else if (key == 257 || key == 335) {
            this.savePreset();
         }

         return true;
      } else if (this.socialFocused) {
         if (key == 256 || key == 257 || key == 335) {
            this.socialFocused = false;
         } else if (key == 259 && !StylesUiConfig.socialName.isEmpty()) {
            StylesUiConfig.socialName = StylesUiConfig.socialName.substring(0, StylesUiConfig.socialName.length() - 1);
            StylesUiConfig.save();
         }

         return true;
      } else if (!this.hexFocused) {
         if (key == 70) {
            int index = this.hoveredEntry >= 0 ? this.hoveredEntry : this.focusedIndex;
            if (index >= 0 && index < this.entries.size() && this.entries.get(index)[0] != null) {
               this.toggleFavorite(this.entries.get(index)[0]);
            }

            return true;
         } else if (key >= 49 && key < 49 + Math.min(9, shownTabs().length)) {
            this.switchTab(shownTabs()[key - 49]);
            return true;
         } else {
            return super.keyPressed(input);
         }
      } else {
         if (key == 256) {
            this.hexFocused = false;
            this.hexDraft = "";
         } else if (key == 259 && !this.hexDraft.isEmpty()) {
            this.hexDraft = this.hexDraft.substring(0, this.hexDraft.length() - 1);
         } else if (key == 257 || key == 335) {
            this.commitHex();
         }

         return true;
      }
   }

   public boolean charTyped(CharacterEvent input) {
      if (!input.isAllowedChatCharacter()) {
         return false;
      } else {
         String typed = input.codepointAsString();
         if (this.searchFocused) {
            if (this.query.length() < 24) {
               this.query = this.query + typed;
               this.gridScroll = 0.0;
               this.applyFilter();
            }

            return true;
         } else if (this.keyEntering) {
            if (typed.matches("[A-Za-z0-9-]") && this.keyDraft.length() < 12) {
               this.keyDraft = this.keyDraft + typed.toUpperCase(Locale.ROOT);
            }

            return true;
         } else if (this.presetNaming) {
            if (this.presetDraft.length() < 20) {
               this.presetDraft = this.presetDraft + typed;
            }

            return true;
         } else if (this.socialFocused) {
            if (StylesUiConfig.socialName.length() < 16) {
               StylesUiConfig.socialName = StylesUiConfig.socialName + typed;
               StylesUiConfig.save();
            }

            return true;
         } else if (this.hexFocused) {
            if (typed.matches("[0-9a-fA-F]") && this.hexDraft.length() < 6) {
               this.hexDraft = this.hexDraft + typed.toUpperCase(Locale.ROOT);
               if (this.hexDraft.length() == 6) {
                  this.commitHex();
               }
            }

            return true;
         } else if ("/".equals(typed)) {
            this.searchFocused = true;
            return true;
         } else {
            return super.charTyped(input);
         }
      }
   }

   public void removed() {
      Cosmetics.tryOn(null, null);
      this.thirdPerson(false);
      HandPreview.clear();
      super.removed();
   }

   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.parent);
         this.applyPending(false);
      }
   }

   private interface Action {
      void run(int var1, double var2, double var4);
   }

   private static enum ButtonStyle {
      NORMAL,
      PRIMARY,
      DISABLED;
   }

   private record CardButton(String label, BaulooStylesConfigScreen.ButtonStyle style, Runnable action) {
   }

   private record Clip(int frames, int fps, int size, int columns) {
   }

   private static enum Drag {
      NONE,
      HUE,
      OPACITY,
      OUTLINE_HUE,
      OUTLINE_WIDTH,
      BLOCK_WIDTH,
      SHIELD,
      TOTEM,
      SHIELD_SIZE,
      GAP_SIZE,
      PICK_HUE,
      COBWEB_WIDTH,
      TIME,
      COS_TURN,
      CRYSTAL_SPIN,
      TOTEM_POP,
      GRADIENT,
      KILL_VOLUME;
   }

   private record ExtraTile(String key, String name, String group, Item icon, String desc) {
   }

   private record Hit(int x, int y, int w, int h, int clipTop, int clipBottom, BaulooStylesConfigScreen.Action action) {
      boolean contains(double mx, double my) {
         return mx >= this.x && mx < this.x + this.w && my >= this.y && my < this.y + this.h && my >= this.clipTop && my < this.clipBottom;
      }
   }

   private static final class Layout {
      int fx;
      int fy;
      int fw;
      int fh;
      int bodyY;
      int footerY;
      int railW;
      int mainX;
      int mainW;
      int sideX;
      int sideW;
      boolean side;
   }
}
