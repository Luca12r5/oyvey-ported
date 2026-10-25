package de.bauloo.styles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.bauloo.styles.mixin.ParticleManagerInvoker;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class KillEffect {
   public static final String[][] EFFECTS = new String[][]{
      {"lightning", "Lightning"}, {"shockwave", "Shockwave"}, {"pillar", "Light Pillar"}, {"popup", "Kill Popup"}, {"soul", "Soul Rise"}
   };
   private static final long MY_KILL_MS = 5000L;
   private static final Map<Integer, Long> HITS = new HashMap<>();
   private static final Map<String, Long> HIT_NAMES = new HashMap<>();
   private static long lastAnchorUse;
   private static final long ANCHOR_KILL_MS = 3000L;
   public static final Set<Entity> SILENT = Collections.newSetFromMap(new WeakHashMap<>());
   private static int nextId = -1000000;
   private static int streak;
   private static final Map<Integer, Long> PLAYED = new HashMap<>();
   private static final Map<String, ArrayDeque<Vec3>> TRAIL = new HashMap<>();
   private static final int TRAIL_TICKS = 6;
   private static final Identifier KILL_SOUND = Identifier.fromNamespaceAndPath("bauloo_styles", "kill");
   private static final List<double[]> RUNNING = new ArrayList<>();
   private static final int WAVES = 10;
   private static final List<KillEffect.Fx> FX = new ArrayList<>();

   private KillEffect() {
   }

   public static boolean valid(String effect) {
      for (String[] e : EFFECTS) {
         if (e[0].equals(effect)) {
            return true;
         }
      }

      return false;
   }

   public static void onAttack(Entity target) {
      if (HITS.size() > 128) {
         HITS.clear();
      }

      HITS.put(target.getId(), System.currentTimeMillis());
      if (target instanceof Player player) {
         if (HIT_NAMES.size() > 128) {
            HIT_NAMES.clear();
         }

         HIT_NAMES.put(player.getName().getString(), System.currentTimeMillis());
      }
   }

   public static void onDeath(Entity entity) {
      StylesUiConfig.load();
      Minecraft mc = Minecraft.getInstance();
      if (entity instanceof Player && entity != mc.player) {
         Long hit = HITS.get(entity.getId());
         if (valid(StylesUiConfig.killEffect) || StylesUiConfig.killSound) {
            boolean mine = hit != null && System.currentTimeMillis() - hit <= 5000L;
            if (!StylesUiConfig.killOnlyMine || mine) {
               long now = System.currentTimeMillis();
               Long last = PLAYED.get(entity.getId());
               if (last == null || now - last >= 3000L) {
                  if (PLAYED.size() > 64) {
                     PLAYED.clear();
                  }

                  PLAYED.put(entity.getId(), now);
                  Vec3 now2 = new Vec3(entity.getX(), entity.getY(), entity.getZ());
                  String name = entity.getName().getString();
                  Vec3 at = beforeDeath(name, now2);
                  if (mine) {
                     streak++;
                  }

                  if (mine) {
                     killSound();
                  }

                  if (valid(StylesUiConfig.killEffect)) {
                     play(StylesUiConfig.killEffect, at, name, mine ? streak : 0);
                  }
               }
            }
         }
      }
   }

   private static Vec3 beforeDeath(String name, Vec3 now) {
      ArrayDeque<Vec3> trail = TRAIL.get(name);
      if (trail != null && !trail.isEmpty()) {
         Vec3[] seen = trail.toArray(new Vec3[0]);
         Vec3 pick = seen[seen.length - 1];

         for (int i = seen.length - 1; i > 0 && i >= seen.length - 4; i--) {
            if (seen[i].distanceToSqr(seen[i - 1]) > 9.0) {
               return seen[i - 1];
            }

            pick = seen[i - 1];
         }

         return now != null && !(now.distanceToSqr(pick) > 9.0) ? now : pick;
      } else {
         return now;
      }
   }

   private static void track(Minecraft client) {
      Set<String> near = new HashSet<>();

      for (Player player : client.level.players()) {
         if (player != client.player && !(player.distanceToSqr(client.player) > 4096.0)) {
            String name = player.getName().getString();
            near.add(name);
            ArrayDeque<Vec3> trail = TRAIL.computeIfAbsent(name, k -> new ArrayDeque<>());
            trail.addLast(new Vec3(player.getX(), player.getY(), player.getZ()));

            while (trail.size() > 6) {
               trail.removeFirst();
            }
         }
      }

      if (TRAIL.size() > 64) {
         TRAIL.keySet().retainAll(near);
      }
   }

   public static void killSound() {
      if (StylesUiConfig.killSound) {
         Minecraft.getInstance()
            .getSoundManager()
            .play(
               new SimpleSoundInstance(
                  KILL_SOUND,
                  SoundSource.MASTER,
                  StylesUiConfig.killVolume / 100.0F,
                  1.0F,
                  SoundInstance.createUnseededRandom(),
                  false,
                  0,
                  Attenuation.NONE,
                  0.0,
                  0.0,
                  0.0,
                  true
               )
            );
      }
   }

   public static void test() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         Vec3 look = mc.player.getViewVector(1.0F);
         Vec3 at = mc.player.position().add(look.x * 3.5, 0.0, look.z * 3.5);
         String effect = valid(StylesUiConfig.killEffect) ? StylesUiConfig.killEffect : "shockwave";
         killSound();
         play(effect, at, "Test Dummy", Math.max(1, streak + 1));
      }
   }

   private static void onMessage(Component message) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         String me = mc.player.getName().getString();
         String victim = null;
         boolean mine = false;
         boolean otherKiller = false;
         if (message.getContents() instanceof TranslatableContents t && t.getKey().startsWith("death.")) {
            Object[] args = t.getArgs();
            if (args.length == 0) {
               return;
            }

            victim = argText(args[0]);

            for (int i = 1; i < args.length; i++) {
               String arg = argText(args[i]);
               if (arg.equals(me)) {
                  mine = true;
               } else if (i == 1 && !arg.isEmpty() && !arg.startsWith("[")) {
                  otherKiller = true;
               }
            }
         } else {
            String plain = message.getString();
            Matcher m = Pattern.compile("^(\\w{1,16}) (?:was|got|has been) .* by (\\w{1,16})").matcher(plain);
            if (!m.find()) {
               return;
            }

            victim = m.group(1);
            mine = m.group(2).equals(me);
            otherKiller = !mine;
         }

         if (victim != null) {
            if (victim.equals(me)) {
               streak = 0;
            } else {
               if (!mine && !otherKiller) {
                  long now = System.currentTimeMillis();
                  Long hit = HIT_NAMES.get(victim);
                  if (hit != null && now - hit <= 5000L) {
                     mine = true;
                  } else if (now - lastAnchorUse <= 3000L) {
                     Vec3 near = beforeDeath(victim, null);
                     mine = near != null && near.distanceToSqr(mc.player.position()) < 144.0;
                  }
               }

               for (Player player : mc.level.players()) {
                  if (player.getName().getString().equals(victim)) {
                     Long lastHit = HITS.get(player.getId());
                     if (mine && (lastHit == null || System.currentTimeMillis() - lastHit > 5000L)) {
                        HITS.put(player.getId(), System.currentTimeMillis());
                     }

                     if (otherKiller) {
                        HITS.remove(player.getId());
                     }

                     onDeath(player);
                     return;
                  }
               }

               if (TRAIL.containsKey(victim) && (mine || !StylesUiConfig.killOnlyMine)) {
                  Vec3 at = beforeDeath(victim, null);
                  playOnce(-victim.hashCode(), at, victim, mine);
               }
            }
         }
      }
   }

   private static String argText(Object arg) {
      return arg instanceof Component text ? text.getString() : String.valueOf(arg);
   }

   private static void playOnce(int id, Vec3 at, String name, boolean mine) {
      StylesUiConfig.load();
      if (valid(StylesUiConfig.killEffect) || StylesUiConfig.killSound) {
         long now = System.currentTimeMillis();
         Long last = PLAYED.get(id);
         if (last == null || now - last >= 3000L) {
            PLAYED.put(id, now);
            if (mine) {
               streak++;
            }

            if (mine) {
               killSound();
            }

            if (valid(StylesUiConfig.killEffect)) {
               play(StylesUiConfig.killEffect, at, name, mine ? streak : 0);
            }
         }
      }
   }

   public static void register() {
      UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
         if (player == Minecraft.getInstance().player && hit != null && world.getBlockState(hit.getBlockPos()).is(Blocks.RESPAWN_ANCHOR)) {
            lastAnchorUse = System.currentTimeMillis();
         }

         return InteractionResult.PASS;
      });
      ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
         if (!overlay) {
            try {
               onMessage(message);
            } catch (RuntimeException var3) {
               BaulooStylesClient.LOGGER.warn("Kill effect: could not read a message: {}", var3.toString());
            }
         }
      });
      ClientTickEvents.END_CLIENT_TICK.register(client -> {
         if (client.level != null && client.player != null) {
            track(client);
            if (client.player.isDeadOrDying()) {
               streak = 0;
            }
         }

         if (!RUNNING.isEmpty()) {
            if (client.level == null) {
               RUNNING.clear();
            } else {
               Iterator<double[]> it = RUNNING.iterator();

               while (it.hasNext()) {
                  double[] run = it.next();
                  int left = (int)run[3];
                  lightningWave(run[0], run[1], run[2], 10 - left);
                  if (left <= 1) {
                     it.remove();
                  } else {
                     run[3] = left - 1;
                  }
               }
            }
         }
      });
      WorldRenderEvents.AFTER_ENTITIES.register(context -> {
         if (!FX.isEmpty()) {
            try {
               PoseStack matrices = context.matrices();
               MultiBufferSource consumers = context.consumers();
               Vec3 cam = context.worldState().cameraRenderState.pos;
               Quaternionf look = context.worldState().cameraRenderState.orientation;
               if (matrices != null && consumers != null && cam != null && look != null) {
                  render(matrices, consumers, cam, look);
               }
            } catch (Throwable var5) {
               FX.clear();
            }
         }
      });
   }

   public static void play(String effect, Vec3 at, String name, int kills) {
      Minecraft mc = Minecraft.getInstance();
      ClientLevel world = mc.level;
      if (world != null) {
         if ("lightning".equals(effect)) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(world, EntitySpawnReason.TRIGGERED);
            if (bolt != null) {
               bolt.setId(nextId--);
               bolt.snapTo(at.x, at.y, at.z);
               bolt.setVisualOnly(true);
               SILENT.add(bolt);
               world.addEntity(bolt);
            }

            lightningWave(at.x, at.y, at.z, 0);
            RUNNING.add(new double[]{at.x, at.y, at.z, 9.0});
         } else {
            if (FX.size() > 24) {
               FX.remove(0);
            }

            if (!"shatter".equals(effect)) {
               FX.add(new KillEffect.Fx(effect, at, name, kills, StylesUiConfig.killColor & 16777215));
            }
         }
      }
   }

   private static void spawn(ClientLevel world, ParticleOptions effect, double x, double y, double z, double vx, double vy, double vz) {
      Minecraft mc = Minecraft.getInstance();

      try {
         Particle particle = ((ParticleManagerInvoker)mc.particleEngine).baulooStyles$create(effect, x, y, z, vx, vy, vz);
         if (particle != null) {
            mc.particleEngine.add(particle);
            return;
         }
      } catch (RuntimeException var16) {
      }

      world.addParticle(effect, true, true, x, y, z, vx, vy, vz);
   }

   private static void lightningWave(double x, double y, double z, int n) {
      ClientLevel world = Minecraft.getInstance().level;
      if (world != null) {
         RandomSource r = world.getRandom();
         boolean first = n == 0;

         for (int i = 0; i < (first ? 50 : 12); i++) {
            spawn(
               world,
               ParticleTypes.ELECTRIC_SPARK,
               x + r.nextGaussian() * 0.5,
               y + r.nextDouble() * 2.2,
               z + r.nextGaussian() * 0.5,
               r.nextGaussian() * 0.3,
               r.nextDouble() * 0.3,
               r.nextGaussian() * 0.3
            );
         }

         for (int i = 0; i < (first ? 24 : 4); i++) {
            spawn(
               world,
               ParticleTypes.END_ROD,
               x + r.nextGaussian() * 0.4,
               y + r.nextDouble() * 2.0,
               z + r.nextGaussian() * 0.4,
               r.nextGaussian() * 0.05,
               r.nextDouble() * 0.08,
               r.nextGaussian() * 0.05
            );
         }
      }
   }

   private static void render(PoseStack matrices, MultiBufferSource consumers, Vec3 cam, Quaternionf look) {
      long now = System.currentTimeMillis();
      FX.removeIf(fxx -> now - fxx.start > fxx.life());

      for (KillEffect.Fx fx : FX) {
         float t = (float)(now - fx.start) / (float)fx.life();
         float ms = (float)(now - fx.start);
         matrices.pushPose();
         matrices.translate(fx.x - cam.x, fx.y - cam.y, fx.z - cam.z);
         String var10 = fx.type;
         switch (var10) {
            case "shockwave":
               shockwave(matrices, consumers, fx, ms);
               break;
            case "pillar":
               pillar(matrices, consumers, fx, t);
               break;
            case "popup":
               popup(matrices, consumers, fx, ms, look);
               break;
            case "shatter":
               shatter(matrices, consumers, fx, ms / 1000.0F);
               break;
            default:
               soul(matrices, consumers, fx, t, look);
         }

         matrices.popPose();
      }
   }

   private static void shockwave(PoseStack matrices, MultiBufferSource consumers, KillEffect.Fx fx, float ms) {
      VertexConsumer glow = consumers.getBuffer(RenderTypes.lightning());
      Matrix4f m = matrices.last().pose();

      for (int ring = 0; ring < 2; ring++) {
         float t = (ms - ring * 180) / 900.0F;
         if (!(t <= 0.0F) && !(t >= 1.0F)) {
            float radius = 0.4F + (ring == 0 ? 5.0F : 3.6F) * easeOut(t);
            float fade = (float)Math.pow(1.0F - t, 1.4);
            float width = 0.18F + 0.5F * (1.0F - t);
            int bright = argb(fade, ring == 0 ? fx.color : lighten(fx.color, 0.5F));
            int none = argb(0.0F, fx.color);
            annulus(glow, m, 0.03F, radius - width, radius, none, bright);
            annulus(glow, m, 0.03F, radius, radius + width * 0.6F, bright, none);
            wall(glow, m, radius, 0.03F, 0.03F + 0.9F * (1.0F - t), bright, none);
         }
      }

      float flash = 1.0F - ms / 250.0F;
      if (flash > 0.0F) {
         annulus(glow, m, 0.04F, 0.0F, 1.6F, argb(flash, 16777215), argb(0.0F, fx.color));
      }
   }

   private static void pillar(PoseStack matrices, MultiBufferSource consumers, KillEffect.Fx fx, float t) {
      VertexConsumer glow = consumers.getBuffer(RenderTypes.lightning());
      Matrix4f m = matrices.last().pose();
      float grow = easeOut(Math.min(1.0F, t / 0.12F));
      float height = 2.0F + 40.0F * grow;
      float fade = t < 0.25F ? 1.0F : 1.0F - (t - 0.25F) / 0.75F;
      fade *= fade;
      float thin = 1.0F - 0.6F * t;
      matrices.pushPose();
      matrices.mulPose(Axis.YP.rotationDegrees(t * 240.0F));
      m = matrices.last().pose();
      tube(glow, m, 0.9F * thin, height, argb(fade * 0.25F, fx.color), argb(0.0F, fx.color));
      tube(glow, m, 0.45F * thin, height, argb(fade * 0.7F, fx.color), argb(0.0F, fx.color));
      tube(glow, m, 0.16F * thin, height, argb(fade, 16777215), argb(fade * 0.2F, 16777215));
      matrices.popPose();
      m = matrices.last().pose();
      annulus(glow, m, 0.03F, 0.0F, 1.8F * thin, argb(fade * 0.8F, fx.color), argb(0.0F, fx.color));
      if (t < 0.35F) {
         float k = t / 0.35F;
         float r = 0.5F + 2.5F * easeOut(k);
         annulus(glow, m, 0.05F + k * 0.3F, r - 0.15F, r + 0.15F, argb(1.0F - k, 16777215), argb(0.0F, fx.color));
      }
   }

   private static void popup(PoseStack matrices, MultiBufferSource consumers, KillEffect.Fx fx, float ms, Quaternionf look) {
      Font font = Minecraft.getInstance().font;
      float t = ms / (float)fx.life();
      float pop = ms < 180.0F ? 1.6F - 0.6F * easeOut(ms / 180.0F) : 1.0F;
      float fade = t > 0.75F ? 1.0F - (t - 0.75F) / 0.25F : Math.min(1.0F, ms / 120.0F);
      float rise = 2.3F + 0.9F * easeOut(t);
      matrices.pushPose();
      matrices.translate(0.0F, rise, 0.0F);
      matrices.mulPose(look);
      float s = 0.045F * pop;
      matrices.scale(s, -s, s);
      Component big = Component.literal(fx.streak > 0 ? "+1 KILL" : "KILL").withStyle(ChatFormatting.BOLD);
      label(font, matrices, consumers, big, 0.0F, fx.color, fade);
      matrices.scale(0.55F, 0.55F, 0.55F);
      label(font, matrices, consumers, Component.literal(fx.name), 20.0F, 16777215, fade);
      if (fx.streak >= 2) {
         Component row = Component.literal(fx.streak + "x STREAK").withStyle(ChatFormatting.BOLD);
         label(font, matrices, consumers, row, 32.0F, lighten(fx.color, 0.45F), fade);
      }

      matrices.popPose();
   }

   private static void label(Font font, PoseStack matrices, MultiBufferSource consumers, Component text, float y, int rgb, float fade) {
      int alpha = Math.max(5, Math.round(fade * 255.0F));
      float x = -font.width(text) / 2.0F;
      Matrix4f m = matrices.last().pose();
      font.drawInBatch(text, x + 1.0F, y + 1.0F, Math.max(5, alpha / 2) << 24 | darken(rgb), false, m, consumers, DisplayMode.SEE_THROUGH, 0, 15728880);
      matrices.pushPose();
      matrices.translate(0.0F, 0.0F, -0.02F);
      font.drawInBatch(text, x, y, alpha << 24 | rgb, false, matrices.last().pose(), consumers, DisplayMode.SEE_THROUGH, 0, 15728880);
      matrices.popPose();
   }

   private static void shatter(PoseStack matrices, MultiBufferSource consumers, KillEffect.Fx fx, float sec) {
      VertexConsumer solid = consumers.getBuffer(RenderTypes.debugQuads());
      VertexConsumer glow = consumers.getBuffer(RenderTypes.lightning());
      float gravity = 16.0F;
      float fade = sec > 1.9F ? 1.0F - (sec - 1.9F) / 0.7F : 1.0F;

      for (float[] b : fx.bits) {
         float land = landTime(b[1], b[4], gravity);
         float ft = Math.min(sec, land);
         float px = b[0] + b[3] * ft * 0.6F;
         float pz = b[2] + b[5] * ft * 0.6F;
         float py = Math.max(0.0F, b[1] + b[4] * ft - 0.5F * gravity * ft * ft);
         float size = b[6] * (sec > 1.9F ? fade : 1.0F);
         if (!(size <= 0.005F)) {
            int rgb = b[7] < 0.25F ? 16777215 : (b[7] < 0.6F ? fx.color : darken(fx.color));
            matrices.pushPose();
            matrices.translate(px, py + size / 2.0F, pz);
            matrices.mulPose(Axis.YP.rotationDegrees(b[8] * ft));
            matrices.mulPose(Axis.XP.rotationDegrees(b[8] * 0.7F * ft));
            Matrix4f m = matrices.last().pose();
            cube(solid, m, size / 2.0F, argb(fade, rgb));
            if (sec < land) {
               cube(glow, m, size * 0.9F, argb(0.25F * fade, fx.color));
            }

            matrices.popPose();
         }
      }

      if (sec < 0.3F) {
         Matrix4f m = matrices.last().pose();
         float k = sec / 0.3F;
         annulus(glow, m, 0.03F, 0.0F, 1.2F + k * 1.5F, argb(1.0F - k, 16777215), argb(0.0F, fx.color));
      }
   }

   private static void soul(PoseStack matrices, MultiBufferSource consumers, KillEffect.Fx fx, float t, Quaternionf look) {
      VertexConsumer glow = consumers.getBuffer(RenderTypes.lightning());
      float rise = 3.2F * easeIn(t);
      float fade = t < 0.1F ? t / 0.1F : 1.0F - (t - 0.1F) / 0.9F;
      float sway = (float)Math.sin(t * Math.PI * 3.0) * 6.0F;
      Vector3f forward = new Vector3f(0.0F, 0.0F, -1.0F).rotate(look);
      float yaw = (float)Math.toDegrees(Math.atan2(forward.x, forward.z));
      matrices.pushPose();
      matrices.translate(0.0F, rise, 0.0F);
      matrices.mulPose(Axis.YP.rotationDegrees(yaw + 180.0F + sway));
      float px = 0.05875F;
      int body = argb(0.55F * fade, fx.color);
      int shell = argb(0.18F * fade, lighten(fx.color, 0.4F));
      float arms = 20.0F + 110.0F * easeOut(Math.min(1.0F, t * 2.0F));
      part(matrices, glow, 0.0F, 28.0F, 0.0F, 4.0F, 4.0F, 4.0F, px, body, shell);
      part(matrices, glow, 0.0F, 18.0F, 0.0F, 4.0F, 6.0F, 2.0F, px, body, shell);
      part(matrices, glow, -2.0F, 6.0F, 0.0F, 2.0F, 6.0F, 2.0F, px, body, shell);
      part(matrices, glow, 2.0F, 6.0F, 0.0F, 2.0F, 6.0F, 2.0F, px, body, shell);

      for (int side = -1; side <= 1; side += 2) {
         matrices.pushPose();
         matrices.translate(side * 6 * px, 23.0F * px, 0.0F);
         matrices.mulPose(Axis.ZP.rotationDegrees(side * arms));
         part(matrices, glow, 0.0F, -5.0F, 0.0F, 2.0F, 6.0F, 2.0F, px, body, shell);
         matrices.popPose();
      }

      matrices.popPose();
      Matrix4f m = matrices.last().pose();
      annulus(glow, m, 0.03F, 0.0F, 1.3F, argb(0.6F * (1.0F - t), fx.color), argb(0.0F, fx.color));
   }

   private static void part(PoseStack matrices, VertexConsumer glow, float cx, float cy, float cz, float hx, float hy, float hz, float px, int body, int shell) {
      matrices.pushPose();
      matrices.translate(cx * px, cy * px, cz * px);
      Matrix4f m = matrices.last().pose();
      box(glow, m, hx * px, hy * px, hz * px, body);
      box(glow, m, (hx + 0.6F) * px, (hy + 0.6F) * px, (hz + 0.6F) * px, shell);
      matrices.popPose();
   }

   private static void annulus(VertexConsumer b, Matrix4f m, float y, float r1, float r2, int c1, int c2) {
      int seg = 48;

      for (int i = 0; i < seg; i++) {
         double a0 = (Math.PI * 2) * i / seg;
         double a1 = (Math.PI * 2) * (i + 1) / seg;
         float c0 = (float)Math.cos(a0);
         float s0 = (float)Math.sin(a0);
         float cc = (float)Math.cos(a1);
         float s1 = (float)Math.sin(a1);
         quad(b, m, c0 * r1, y, s0 * r1, c1, cc * r1, y, s1 * r1, c1, cc * r2, y, s1 * r2, c2, c0 * r2, y, s0 * r2, c2);
      }
   }

   private static void wall(VertexConsumer b, Matrix4f m, float r, float y1, float y2, int c1, int c2) {
      int seg = 48;

      for (int i = 0; i < seg; i++) {
         double a0 = (Math.PI * 2) * i / seg;
         double a1 = (Math.PI * 2) * (i + 1) / seg;
         float x0 = (float)Math.cos(a0) * r;
         float z0 = (float)Math.sin(a0) * r;
         float x1 = (float)Math.cos(a1) * r;
         float z1 = (float)Math.sin(a1) * r;
         quad(b, m, x0, y1, z0, c1, x1, y1, z1, c1, x1, y2, z1, c2, x0, y2, z0, c2);
      }
   }

   private static void tube(VertexConsumer b, Matrix4f m, float r, float h, int bottom, int top) {
      int seg = 12;

      for (int i = 0; i < seg; i++) {
         double a0 = (Math.PI * 2) * i / seg;
         double a1 = (Math.PI * 2) * (i + 1) / seg;
         float x0 = (float)Math.cos(a0) * r;
         float z0 = (float)Math.sin(a0) * r;
         float x1 = (float)Math.cos(a1) * r;
         float z1 = (float)Math.sin(a1) * r;
         quad(b, m, x0, 0.0F, z0, bottom, x1, 0.0F, z1, bottom, x1, h, z1, top, x0, h, z0, top);
      }
   }

   private static void box(VertexConsumer b, Matrix4f m, float hx, float hy, float hz, int c) {
      quad(b, m, -hx, -hy, -hz, c, hx, -hy, -hz, c, hx, hy, -hz, c, -hx, hy, -hz, c);
      quad(b, m, -hx, -hy, hz, c, hx, -hy, hz, c, hx, hy, hz, c, -hx, hy, hz, c);
      quad(b, m, -hx, -hy, -hz, c, -hx, -hy, hz, c, -hx, hy, hz, c, -hx, hy, -hz, c);
      quad(b, m, hx, -hy, -hz, c, hx, -hy, hz, c, hx, hy, hz, c, hx, hy, -hz, c);
      quad(b, m, -hx, hy, -hz, c, hx, hy, -hz, c, hx, hy, hz, c, -hx, hy, hz, c);
      quad(b, m, -hx, -hy, -hz, c, hx, -hy, -hz, c, hx, -hy, hz, c, -hx, -hy, hz, c);
   }

   private static void cube(VertexConsumer b, Matrix4f m, float h, int c) {
      box(b, m, h, h, h, c);
   }

   private static void quad(
      VertexConsumer b,
      Matrix4f m,
      float x1,
      float y1,
      float z1,
      int c1,
      float x2,
      float y2,
      float z2,
      int c2,
      float x3,
      float y3,
      float z3,
      int c3,
      float x4,
      float y4,
      float z4,
      int c4
   ) {
      b.addVertex(m, x1, y1, z1).setColor(c1);
      b.addVertex(m, x2, y2, z2).setColor(c2);
      b.addVertex(m, x3, y3, z3).setColor(c3);
      b.addVertex(m, x4, y4, z4).setColor(c4);
      b.addVertex(m, x4, y4, z4).setColor(c4);
      b.addVertex(m, x3, y3, z3).setColor(c3);
      b.addVertex(m, x2, y2, z2).setColor(c2);
      b.addVertex(m, x1, y1, z1).setColor(c1);
   }

   private static float landTime(float y0, float v, float g) {
      return (float)((v + Math.sqrt(v * v + 2.0F * g * y0)) / g);
   }

   private static int argb(float alpha, int rgb) {
      int a = Math.round(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F);
      return a << 24 | rgb & 16777215;
   }

   private static int lighten(int rgb, float k) {
      int r = rgb >> 16 & 0xFF;
      int g = rgb >> 8 & 0xFF;
      int b = rgb & 0xFF;
      r += Math.round((255 - r) * k);
      g += Math.round((255 - g) * k);
      b += Math.round((255 - b) * k);
      return r << 16 | g << 8 | b;
   }

   private static int darken(int rgb) {
      return rgb >> 1 & 8355711;
   }

   private static float easeOut(float t) {
      float k = 1.0F - Math.max(0.0F, Math.min(1.0F, t));
      return 1.0F - k * k * k;
   }

   private static float easeIn(float t) {
      float k = Math.max(0.0F, Math.min(1.0F, t));
      return k * k;
   }

   private static final class Fx {
      final String type;
      final double x;
      final double y;
      final double z;
      final long start = System.currentTimeMillis();
      final String name;
      final int streak;
      final int color;
      float[][] bits;

      Fx(String type, Vec3 at, String name, int streak, int color) {
         this.type = type;
         this.x = at.x;
         this.y = at.y;
         this.z = at.z;
         this.name = name;
         this.streak = streak;
         this.color = color;
      }

      long life() {
         String var1 = this.type;

         return switch (var1) {
            case "shockwave" -> 1100L;
            case "pillar" -> 1600L;
            case "popup" -> 2400L;
            case "shatter" -> 2600L;
            default -> 2200L;
         };
      }

      void makeBits(RandomSource r) {
         this.bits = new float[70][];

         for (int i = 0; i < this.bits.length; i++) {
            double a = r.nextDouble() * Math.PI * 2.0;
            double speed = 1.2 + r.nextDouble() * 3.2;
            this.bits[i] = new float[]{
               (float)(r.nextGaussian() * 0.25),
               (float)(0.2 + r.nextDouble() * 1.6),
               (float)(r.nextGaussian() * 0.25),
               (float)(Math.cos(a) * speed),
               (float)(2.5 + r.nextDouble() * 4.5),
               (float)(Math.sin(a) * speed),
               (float)(0.07 + r.nextDouble() * 0.11),
               (float)r.nextDouble(),
               (float)(r.nextGaussian() * 720.0)
            };
         }
      }
   }
}
