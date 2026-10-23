package dev.lego.emote;

import dev.lego.LegoClient;
import dev.lego.ui.Sound;
import dev.spotifyhud.Json;
import java.io.DataInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import net.minecraft.client.model.geom.ModelPart;

/**
 * The launcher's emotes (car, chair, rocket, dances …) in the client. They are baked by
 * tools/emotes/bake.mts from apps/launcher/src/renderer/emotes.ts: 20 frames per second, and per
 * frame for each body part its pivot, rotation and scale in Minecraft's model space. Playing them
 * moves, turns and scales the parts exactly like on the launcher's play screen; the second skin
 * layer is a child of each part and follows.
 */
public final class BakedEmotes {
   private static final int PER_PART = 9;
   private static final int PER_FRAME = 6 * PER_PART;

   /** One baked emote: frames × 54 values (pivot ×100, angles ×10000, scale ×1000). */
   public static final class Clip {
      public final String cat;
      public final float fps;
      final short[] data;
      final int frames;

      Clip(String cat, float fps, short[] data, int frames) {
         this.cat = cat;
         this.fps = fps;
         this.data = data;
         this.frames = frames;
      }
   }

   private BakedEmotes() {
   }

   /** Registers every baked emote whose launcher id is not taken yet (ids get the prefix "lx_"). */
   static void addAll() {
      try (InputStream meta = BakedEmotes.class.getResourceAsStream("/assets/legoclient/emotes/baked.json");
           InputStream bin = BakedEmotes.class.getResourceAsStream("/assets/legoclient/emotes/baked.bin")) {
         if (meta == null || bin == null) return;
         Object root = Json.parse(new String(meta.readAllBytes(), StandardCharsets.UTF_8));
         float fps = (float)Json.dbl(root, "fps", 20.0);
         byte[] raw = bin.readAllBytes();
         short[] all = new short[raw.length / 2];
         try (DataInputStream in = new DataInputStream(new java.io.ByteArrayInputStream(raw))) {
            for (int i = 0; i < all.length; i++) all[i] = in.readShort();
         }
         List<Object> list = Json.arr(root, "emotes");
         int added = 0;
         for (Object o : list == null ? List.<Object>of() : list) {
            String id = "lx_" + Json.str(o, "id", "");
            if (EmoteLib.get(id) != null) continue;
            int frames = (int)Json.num(o, "frames", 0L);
            int offset = (int)Json.num(o, "offset", 0L);
            if (frames <= 1 || (offset + frames) * PER_FRAME > all.length) continue;
            short[] data = new short[frames * PER_FRAME];
            System.arraycopy(all, offset * PER_FRAME, data, 0, data.length);
            String cat = Json.str(o, "cat", "dance");
            Clip clip = new Clip(cat, fps, data, frames);
            float dur = (float)Json.dbl(o, "dur", frames / fps);
            String icon = switch (cat) { case "transform" -> "brick"; case "gesture" -> "emote"; default -> "music"; };
            EmoteLib.addGenerated(id, Json.str(o, "name", id), icon, dur, false, null, 0.0F, (t, p) -> { });
            EmoteLib.Emote e = EmoteLib.get(id);
            if (e != null) e.clip = clip;
            added++;
         }
         LegoClient.LOG("Launcher-Emotes geladen: " + added);
      } catch (Throwable e) {
         LegoClient.LOG("Launcher-Emotes: " + e);
      }
   }

   /** Poses the six parts for time t (seconds) of the clip. */
   static void apply(Clip c, float t, ModelPart... parts) {
      float f = Math.max(0.0F, Math.min(c.frames - 1.001F, t * c.fps));
      int a = (int)f;
      float k = f - a;
      int b = Math.min(c.frames - 1, a + 1);
      for (int i = 0; i < 6 && i < parts.length; i++) {
         ModelPart m = parts[i];
         if (m == null) continue;
         int oa = a * PER_FRAME + i * PER_PART, ob = b * PER_FRAME + i * PER_PART;
         m.x = lerp(c.data[oa], c.data[ob], k) / 100.0F;
         m.y = lerp(c.data[oa + 1], c.data[ob + 1], k) / 100.0F;
         m.z = lerp(c.data[oa + 2], c.data[ob + 2], k) / 100.0F;
         m.xRot = angle(c.data[oa + 3], c.data[ob + 3], k);
         m.yRot = angle(c.data[oa + 4], c.data[ob + 4], k);
         m.zRot = angle(c.data[oa + 5], c.data[ob + 5], k);
         m.xScale = lerp(c.data[oa + 6], c.data[ob + 6], k) / 1000.0F;
         m.yScale = lerp(c.data[oa + 7], c.data[ob + 7], k) / 1000.0F;
         m.zScale = lerp(c.data[oa + 8], c.data[ob + 8], k) / 1000.0F;
      }
   }

   private static float lerp(short a, short b, float k) {
      return a + (b - a) * k;
   }

   private static float angle(short a, short b, float k) {
      float x = a / 10000.0F, y = b / 10000.0F;
      float d = (float)Math.IEEEremainder(y - x, Math.PI * 2);
      return x + d * k;
   }

   /** Sound when an emote starts (Minecraft sounds instead of the launcher's synthesizer). */
   static void started(EmoteLib.Emote e) {
      String id = e.id.startsWith("lx_") ? e.id.substring(3) : e.id;
      String s = switch (id) {
         case "car", "tank", "train", "scooter" -> "minecraft:entity.minecart.riding";
         case "rocket" -> "minecraft:entity.firework_rocket.launch";
         case "plane", "heli", "windmill", "arrow", "superhero", "cartwheel", "backflip" -> "minecraft:entity.phantom.flap";
         case "boat" -> "minecraft:entity.boat.paddle_water";
         case "explode" -> "minecraft:entity.generic.explode";
         case "snake", "spider" -> "minecraft:entity.spider.ambient";
         case "zombie" -> "minecraft:entity.zombie.ambient";
         case "bed", "sleep" -> "minecraft:entity.fox.sleep";
         case "throne", "cheer", "salute" -> "minecraft:entity.player.levelup";
         case "ball", "jelly", "hop", "jumpingjack" -> "minecraft:entity.slime.jump";
         case "chicken" -> "minecraft:entity.chicken.ambient";
         default -> e.clip != null && e.clip.cat.equals("transform") ? "minecraft:block.stone.place" : "minecraft:block.note_block.bell";
      };
      Sound.play(s, 1.0F, 0.6F);
   }
}
