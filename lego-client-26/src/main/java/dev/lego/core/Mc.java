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
      LocalPlayer var0 = player();
      ClientPacketListener var1 = mc().getConnection();
      if (var0 != null && var1 != null) {
         PlayerInfo var2 = var1.getPlayerInfo(var0.getUUID());
         return var2 == null ? -1 : var2.getLatency();
      } else {
         return -1;
      }
   }

   public static String serverAddress() {
      if (mc().isLocalServer()) {
         return "Einzelspieler";
      } else {
         ServerData var0 = mc().getCurrentServer();
         return var0 == null ? "-" : var0.ip;
      }
   }

   public static void command(String var0) {
      ClientPacketListener var1 = mc().getConnection();
      if (var1 != null) {
         var1.sendCommand(var0);
      }
   }

   public static void actionBar(String var0) {
      LocalPlayer var1 = player();
      if (var1 != null) {
         var1.displayClientMessage(Component.literal(var0), true);
      }
   }

   public static void chat(String var0) {
      LocalPlayer var1 = player();
      if (var1 != null) {
         var1.displayClientMessage(Component.literal(var0), false);
      }
   }

   public static double x(Entity var0) {
      return var0.getX();
   }

   public static double y(Entity var0) {
      return var0.getY();
   }

   public static double z(Entity var0) {
      return var0.getZ();
   }

   public static double lastX(Entity var0) {
      return var0.xo;
   }

   public static double lastY(Entity var0) {
      return var0.yo;
   }

   public static double lastZ(Entity var0) {
      return var0.zo;
   }

   public static double[] lerpPos(Entity var0, float var1) {
      return new double[]{
         lastX(var0) + (x(var0) - lastX(var0)) * var1, lastY(var0) + (y(var0) - lastY(var0)) * var1, lastZ(var0) + (z(var0) - lastZ(var0)) * var1
      };
   }

   public static float yaw(Entity var0) {
      return var0.getYRot();
   }

   public static float pitch(Entity var0) {
      return var0.getXRot();
   }

   public static float height(Entity var0) {
      return var0.getBbHeight();
   }

   public static float width(Entity var0) {
      return var0.getBbWidth();
   }

   public static boolean onGround(Entity var0) {
      return var0.onGround();
   }

   public static int id(Entity var0) {
      return var0.getId();
   }

   public static String name(Entity var0) {
      return var0.getName().getString();
   }

   public static double[] velocity(Entity var0) {
      Vec3 var1 = var0.getDeltaMovement();
      return new double[]{var1.x, var1.y, var1.z};
   }

   public static boolean isPlayer(Entity var0) {
      return var0 instanceof Player;
   }

   public static boolean isLiving(Entity var0) {
      return var0 instanceof LivingEntity;
   }

   public static boolean isEndCrystal(Entity var0) {
      return var0 instanceof EndCrystal;
   }

   public static boolean isSpectator(Entity var0) {
      return var0.isSpectator();
   }

   public static boolean isSneaking(Entity var0) {
      return var0.isShiftKeyDown();
   }

   public static float health(LivingEntity var0) {
      return var0.getHealth();
   }

   public static float maxHealth(LivingEntity var0) {
      return var0.getMaxHealth();
   }

   public static float absorption(LivingEntity var0) {
      return var0.getAbsorptionAmount();
   }

   public static float bodyYaw(LivingEntity var0) {
      return var0.yBodyRot;
   }

   public static float lastBodyYaw(LivingEntity var0) {
      return var0.yBodyRotO;
   }

   public static void setBodyYaw(LivingEntity var0, float var1) {
      var0.yBodyRot = var1;
   }

   public static List<Entity> entities() {
      ArrayList var0 = new ArrayList();
      ClientLevel var1 = world();
      if (var1 == null) {
         return var0;
      } else {
         for (Entity var3 : var1.entitiesForRendering()) {
            var0.add(var3);
         }

         return var0;
      }
   }

   public static List<Player> players() {
      ClientLevel var0 = world();
      return var0 == null ? new ArrayList<>() : new ArrayList<>(var0.players());
   }

   public static int invSize() {
      LocalPlayer var0 = player();
      return var0 == null ? 0 : var0.getInventory().getContainerSize();
   }

   public static ItemStack invStack(int var0) {
      LocalPlayer var1 = player();
      return var1 == null ? null : var1.getInventory().getItem(var0);
   }

   public static ItemStack armor(int var0) {
      return invStack(var0);
   }

   public static boolean empty(ItemStack var0) {
      return var0 == null || var0.isEmpty();
   }

   public static int count(ItemStack var0) {
      return empty(var0) ? 0 : var0.getCount();
   }

   public static String itemKey(ItemStack var0) {
      return empty(var0) ? "" : var0.getItem().getDescriptionId();
   }

   public static int damage(ItemStack var0) {
      return var0.getDamageValue();
   }

   public static int maxDamage(ItemStack var0) {
      return var0.getMaxDamage();
   }

   public static boolean damageable(ItemStack var0) {
      return var0.isDamageableItem();
   }

   public static int countItem(String var0) {
      int var1 = 0;
      int var2 = invSize();

      for (int var3 = 0; var3 < var2; var3++) {
         ItemStack var4 = invStack(var3);
         if (!empty(var4) && itemKey(var4).endsWith("." + var0)) {
            var1 += count(var4);
         }
      }

      return var1;
   }

   public static ItemStack icon(String var0) {
      return ICONS.computeIfAbsent(var0, var0x -> {
         try {
            Object var1 = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minecraft", var0x));
            return var1 instanceof Item ? new ItemStack((Item)var1) : null;
         } catch (Throwable var2) {
            return null;
         }
      });
   }

   public static Collection<MobEffectInstance> effects() {
      LocalPlayer var0 = player();
      return (Collection<MobEffectInstance>)(var0 == null ? new ArrayList<>() : var0.getActiveEffects());
   }

   public static int food() {
      LocalPlayer var0 = player();
      return var0 == null ? 0 : var0.getFoodData().getFoodLevel();
   }

   public static float saturation() {
      LocalPlayer var0 = player();
      if (var0 == null) {
         return 0.0F;
      } else {
         FoodData var1 = var0.getFoodData();
         return var1.getSaturationLevel();
      }
   }

   public static String biome() {
      LocalPlayer var0 = player();
      ClientLevel var1 = world();
      if (var0 != null && var1 != null) {
         try {
            BlockPos var2 = var0.blockPosition();
            Holder var3 = var1.getBiome(var2);
            Optional var4 = var3.unwrapKey();
            if (var4.isPresent()) {
               return ((ResourceKey)var4.get()).identifier().getPath();
            }
         } catch (Throwable var5) {
         }

         return "-";
      } else {
         return "-";
      }
   }

   public static long timeOfDay() {
      ClientLevel var0 = world();
      return var0 == null ? 0L : var0.getDayTime();
   }

   public static void setTimeOfDay(long var0) {
      ClientLevel var2 = world();
      if (var2 != null) {
         var2.getLevelData().setDayTime(var0);
      }
   }

   public static int blockLight() {
      LocalPlayer var0 = player();
      ClientLevel var1 = world();
      if (var0 != null && var1 != null) {
         try {
            return var1.getBrightness(LightLayer.BLOCK, var0.blockPosition());
         } catch (Throwable var3) {
            return -1;
         }
      } else {
         return -1;
      }
   }

   public static void clipboard(String var0) {
      try {
         mc().keyboardHandler.setClipboard(var0);
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

   public static String typeId(Entity var0) {
      try {
         return BuiltInRegistries.ENTITY_TYPE.getKey(var0.getType()).getPath();
      } catch (Throwable var2) {
         return "";
      }
   }

   public static boolean isDead(LivingEntity var0) {
      return var0.isDeadOrDying();
   }

   public static void clearWeather() {
      ClientLevel var0 = world();
      if (var0 != null) {
         var0.setRainLevel(0.0F);
         var0.setThunderLevel(0.0F);
      }
   }

   public static ParticleOptions particle(String var0) {
      return PARTICLES.computeIfAbsent(var0, var0x -> {
         try {
            Object var1 = BuiltInRegistries.PARTICLE_TYPE.getValue(Identifier.fromNamespaceAndPath("minecraft", var0x));
            return var1 instanceof ParticleOptions ? (ParticleOptions)var1 : null;
         } catch (Throwable var2) {
            return null;
         }
      });
   }

   public static void spawn(String var0, double var1, double var3, double var5, double var7, double var9, double var11) {
      ParticleOptions var13 = particle(var0);
      if (var13 != null) {
         try {
            mc().particleEngine.createParticle(var13, var1, var3, var5, var7, var9, var11);
         } catch (Throwable var15) {
         }
      }
   }

   public static Holder nightVision() {
      if (nightVision == null) {
         try {
            Optional var0 = BuiltInRegistries.MOB_EFFECT.get(Identifier.fromNamespaceAndPath("minecraft", "night_vision"));
            nightVision = (Holder<?>)var0.orElse(null);
         } catch (Throwable var1) {
         }
      }

      return nightVision;
   }

   public static Window window() {
      return mc().getWindow();
   }

   public static boolean keyDown(int var0) {
      if (var0 < 0) {
         return false;
      } else {
         try {
            return var0 <= 7 ? GLFW.glfwGetMouseButton(window().handle(), var0) == 1 : InputConstants.isKeyDown(window(), var0);
         } catch (Throwable var2) {
            return false;
         }
      }
   }

   public static boolean mouseDown(int var0) {
      try {
         return GLFW.glfwGetMouseButton(window().handle(), var0) == 1;
      } catch (Throwable var2) {
         return false;
      }
   }

   public static boolean firstPerson() {
      CameraType var0 = mc().options.getCameraType();
      return var0 == null || var0.isFirstPerson();
   }

   public static CameraType perspective() {
      return mc().options.getCameraType();
   }

   public static void perspective(CameraType var0) {
      mc().options.setCameraType(var0);
   }

   public static boolean screenOpen() {
      return mc().screen != null;
   }

   public static boolean pressed(KeyMapping var0) {
      return var0 != null && var0.isDown();
   }
}
