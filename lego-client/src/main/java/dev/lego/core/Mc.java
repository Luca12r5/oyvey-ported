package dev.lego.core;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public final class Mc {
   private static final Map<String, ItemStack> ICONS = new HashMap<>();
   private static final Map<String, ParticleOptions> PARTICLES = new HashMap<>();
   private static Holder<?> nightVision;

   private Mc() {
   }

   public static Minecraft mc() {
      return Minecraft.getInstance();
   }

   public static LocalPlayer player() {
      return mc().player;
   }

   public static ClientLevel world() {
      return mc().level;
   }

   public static boolean inGame() {
      return player() != null && world() != null;
   }

   public static int fps() {
      return mc().getFps();
   }

   public static float tickDelta() {
      try {
         return mc().getDeltaTracker().getGameTimeDeltaPartialTick(false);
      } catch (Throwable var1) {
         return 1.0F;
      }
   }

   public static int ping() {
      LocalPlayer p = player();
      ClientPacketListener nh = mc().getConnection();
      if (p != null && nh != null) {
         PlayerInfo e = nh.getPlayerInfo(p.getUUID());
         return e == null ? -1 : e.getLatency();
      } else {
         return -1;
      }
   }

   public static String serverAddress() {
      if (mc().isLocalServer()) {
         return "Einzelspieler";
      } else {
         ServerData s = mc().getCurrentServer();
         return s == null ? "-" : s.ip;
      }
   }

   public static void command(String cmd) {
      ClientPacketListener nh = mc().getConnection();
      if (nh != null) {
         nh.sendCommand(cmd);
      }
   }

   public static void actionBar(String msg) {
      LocalPlayer p = player();
      if (p != null) {
         p.displayClientMessage(Component.literal(msg), true);
      }
   }

   public static void chat(String msg) {
      LocalPlayer p = player();
      if (p != null) {
         p.displayClientMessage(Component.literal(msg), false);
      }
   }

   public static double x(Entity e) {
      return e.getX();
   }

   public static double y(Entity e) {
      return e.getY();
   }

   public static double z(Entity e) {
      return e.getZ();
   }

   public static double lastX(Entity e) {
      return e.xo;
   }

   public static double lastY(Entity e) {
      return e.yo;
   }

   public static double lastZ(Entity e) {
      return e.zo;
   }

   public static double[] lerpPos(Entity e, float td) {
      return new double[]{lastX(e) + (x(e) - lastX(e)) * td, lastY(e) + (y(e) - lastY(e)) * td, lastZ(e) + (z(e) - lastZ(e)) * td};
   }

   public static float yaw(Entity e) {
      return e.getYRot();
   }

   public static float pitch(Entity e) {
      return e.getXRot();
   }

   public static float height(Entity e) {
      return e.getBbHeight();
   }

   public static float width(Entity e) {
      return e.getBbWidth();
   }

   public static boolean onGround(Entity e) {
      return e.onGround();
   }

   public static int id(Entity e) {
      return e.getId();
   }

   public static String name(Entity e) {
      return e.getName().getString();
   }

   public static double[] velocity(Entity e) {
      Vec3 v = e.getDeltaMovement();
      return new double[]{v.x, v.y, v.z};
   }

   public static boolean isPlayer(Entity e) {
      return e instanceof Player;
   }

   public static boolean isLiving(Entity e) {
      return e instanceof LivingEntity;
   }

   public static boolean isEndCrystal(Entity e) {
      return e instanceof EndCrystal;
   }

   public static boolean isSpectator(Entity e) {
      return e.isSpectator();
   }

   public static boolean isSneaking(Entity e) {
      return e.isShiftKeyDown();
   }

   public static float health(LivingEntity e) {
      return e.getHealth();
   }

   public static float maxHealth(LivingEntity e) {
      return e.getMaxHealth();
   }

   public static float absorption(LivingEntity e) {
      return e.getAbsorptionAmount();
   }

   public static float bodyYaw(LivingEntity e) {
      return e.yBodyRot;
   }

   public static float lastBodyYaw(LivingEntity e) {
      return e.yBodyRotO;
   }

   public static void setBodyYaw(LivingEntity e, float v) {
      e.yBodyRot = v;
   }

   public static List<Entity> entities() {
      List<Entity> out = new ArrayList<>();
      ClientLevel w = world();
      if (w == null) {
         return out;
      } else {
         for (Entity e : w.entitiesForRendering()) {
            out.add(e);
         }

         return out;
      }
   }

   public static List<Player> players() {
      ClientLevel w = world();
      return w == null ? new ArrayList<>() : new ArrayList<>(w.players());
   }

   public static int invSize() {
      LocalPlayer p = player();
      return p == null ? 0 : p.getInventory().getContainerSize();
   }

   public static ItemStack invStack(int i) {
      LocalPlayer p = player();
      return p == null ? null : p.getInventory().getItem(i);
   }

   public static ItemStack armor(int slot) {
      return invStack(slot);
   }

   public static boolean empty(ItemStack s) {
      return s == null || s.isEmpty();
   }

   public static int count(ItemStack s) {
      return empty(s) ? 0 : s.getCount();
   }

   public static String itemKey(ItemStack s) {
      return empty(s) ? "" : s.getItem().getDescriptionId();
   }

   public static int damage(ItemStack s) {
      return s.getDamageValue();
   }

   public static int maxDamage(ItemStack s) {
      return s.getMaxDamage();
   }

   public static boolean damageable(ItemStack s) {
      return s.isDamageableItem();
   }

   public static int countItem(String idPath) {
      int n = 0;
      int size = invSize();

      for (int i = 0; i < size; i++) {
         ItemStack s = invStack(i);
         if (!empty(s) && itemKey(s).endsWith("." + idPath)) {
            n += count(s);
         }
      }

      return n;
   }

   public static ItemStack icon(String idPath) {
      return ICONS.computeIfAbsent(idPath, k -> {
         try {
            Object item = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minecraft", k));
            return item instanceof Item ? new ItemStack((Item)item) : null;
         } catch (Throwable var2) {
            return null;
         }
      });
   }

   public static Collection<MobEffectInstance> effects() {
      LocalPlayer p = player();
      return (Collection<MobEffectInstance>)(p == null ? new ArrayList<>() : p.getActiveEffects());
   }

   public static int food() {
      LocalPlayer p = player();
      return p == null ? 0 : p.getFoodData().getFoodLevel();
   }

   public static float saturation() {
      LocalPlayer p = player();
      if (p == null) {
         return 0.0F;
      } else {
         FoodData h = p.getFoodData();
         return h.getSaturationLevel();
      }
   }

   public static String biome() {
      LocalPlayer p = player();
      ClientLevel w = world();
      if (p != null && w != null) {
         try {
            BlockPos pos = p.blockPosition();
            Holder<?> b = w.getBiome(pos);
            Optional<?> key = b.unwrapKey();
            if (key.isPresent()) {
               return ((ResourceKey)key.get()).identifier().getPath();
            }
         } catch (Throwable var5) {
         }

         return "-";
      } else {
         return "-";
      }
   }

   public static long timeOfDay() {
      ClientLevel w = world();
      return w == null ? 0L : w.getDayTime();
   }

   public static void setTimeOfDay(long t) {
      ClientLevel w = world();
      if (w != null) {
         w.getLevelData().setDayTime(t);
      }
   }

   public static int blockLight() {
      LocalPlayer p = player();
      ClientLevel w = world();
      if (p != null && w != null) {
         try {
            return w.getBrightness(LightLayer.BLOCK, p.blockPosition());
         } catch (Throwable var3) {
            return -1;
         }
      } else {
         return -1;
      }
   }

   public static void clipboard(String s) {
      try {
         mc().keyboardHandler.setClipboard(s);
      } catch (Throwable var2) {
      }
   }

   public static Options options() {
      return mc().options;
   }

   public static boolean windowFocused() {
      try {
         return mc().isWindowActive();
      } catch (Throwable var1) {
         return true;
      }
   }

   public static String typeId(Entity e) {
      try {
         return BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath();
      } catch (Throwable var2) {
         return "";
      }
   }

   public static boolean isDead(LivingEntity e) {
      return e.isDeadOrDying();
   }

   public static void clearWeather() {
      ClientLevel w = world();
      if (w != null) {
         w.setRainLevel(0.0F);
         w.setThunderLevel(0.0F);
      }
   }

   public static ParticleOptions particle(String id) {
      return PARTICLES.computeIfAbsent(id, k -> {
         try {
            Object o = BuiltInRegistries.PARTICLE_TYPE.getValue(Identifier.fromNamespaceAndPath("minecraft", k));
            return o instanceof ParticleOptions ? (ParticleOptions)o : null;
         } catch (Throwable var2) {
            return null;
         }
      });
   }

   public static void spawn(String id, double x, double y, double z, double vx, double vy, double vz) {
      ParticleOptions p = particle(id);
      if (p != null) {
         try {
            mc().particleEngine.createParticle(p, x, y, z, vx, vy, vz);
         } catch (Throwable var15) {
         }
      }
   }

   public static Holder nightVision() {
      if (nightVision == null) {
         try {
            Optional<?> e = BuiltInRegistries.MOB_EFFECT.get(Identifier.fromNamespaceAndPath("minecraft", "night_vision"));
            nightVision = (Holder<?>)e.orElse(null);
         } catch (Throwable var1) {
         }
      }

      return nightVision;
   }

   public static Window window() {
      return mc().getWindow();
   }

   public static boolean keyDown(int key) {
      if (key < 0) {
         return false;
      } else {
         try {
            return key <= 7 ? GLFW.glfwGetMouseButton(window().handle(), key) == 1 : InputConstants.isKeyDown(window(), key);
         } catch (Throwable var2) {
            return false;
         }
      }
   }

   public static boolean mouseDown(int button) {
      try {
         return GLFW.glfwGetMouseButton(window().handle(), button) == 1;
      } catch (Throwable var2) {
         return false;
      }
   }

   public static boolean firstPerson() {
      CameraType p = mc().options.getCameraType();
      return p == null || p.isFirstPerson();
   }

   public static CameraType perspective() {
      return mc().options.getCameraType();
   }

   public static void perspective(CameraType p) {
      mc().options.setCameraType(p);
   }

   public static boolean screenOpen() {
      return mc().screen != null;
   }

   public static boolean pressed(KeyMapping k) {
      return k != null && k.isDown();
   }
}
