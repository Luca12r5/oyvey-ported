package dev.lego.util;

import com.mojang.authlib.GameProfile;
import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.cosmetic.PetWorld;
import dev.lego.ui.Sound;
import dev.lego.ui.Toasts;
import dev.lego.visual.Overlays;
import dev.lego.visual.R3;
import dev.lego.visual.Visuals2;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class Utility {
   public static Utility.SeeModule invsee;
   public static Utility.SeeModule ecsee;
   public static Utility.ToggleSprint sprint;
   public static Utility.ToggleSneak sneak;
   public static Utility.Freelook freelook;
   public static Utility.AutoGG autoGG;
   public static Utility.AutoRespawn autoRespawn;
   public static Utility.Waypoints waypoints;
   public static Utility.DeathPoint deathPoint;
   public static Utility.HitSound hitSound;
   public static Utility.KillSound killSound;
   public static Utility.MentionPing mention;
   public static Utility.AntiSpam antiSpam;
   public static Utility.WordFilter wordFilter;
   public static Utility.Macros macros;
   public static Utility.ShareCoords shareCoords;
   public static FakeCheck fakeCheck;
   public static Auction auction;
   public static FightPreset fightPreset;
   private static final Set<Integer> KILLED = new HashSet<>();
   public static final List<Utility.Waypoint> POINTS = new ArrayList<>();
   private static final int[] WP_COLORS = new int[]{-14756000, -16735270, -13053, -36939, -6599222, -30208, -8587265};
   static final String[] SOUND_NAMES = new String[]{"Pling", "Glocke", "Klick", "XP", "Amethyst", "Bass"};
   static final String[] SOUND_IDS = new String[]{
      "minecraft:block.note_block.pling",
      "minecraft:block.note_block.bell",
      "minecraft:block.note_block.hat",
      "minecraft:entity.experience_orb.pickup",
      "minecraft:block.amethyst_block.chime",
      "minecraft:block.note_block.bass"
   };
   static final String[] KEY_NAMES = new String[]{
      "Aus", "F6", "F7", "F8", "F9", "Num 1", "Num 2", "Num 3", "Num 4", "Num 5", "Num 6", "Num 7", "Num 8", "Num 9"
   };
   static final int[] KEY_CODES = new int[]{-1, 295, 296, 297, 298, 321, 322, 323, 324, 325, 326, 327, 328, 329};

   private Utility() {
   }

   public static void registerAll() {
      invsee = Modules.register(
         new Utility.SeeModule(
            "invsee", "InvSee", "Spieler ansehen + V: öffnet sein Inventar (/invsee) - braucht Server-Rechte", new String[]{"invsee", "openinv", "inv"}
         )
      );
      ecsee = Modules.register(
         new Utility.SeeModule(
            "ecsee",
            "EcSee",
            "Spieler ansehen + G: öffnet seine Enderkiste (/ecsee) - braucht Server-Rechte",
            new String[]{"ecsee", "enderchest", "ec", "echest", "endersee"}
         )
      );
      sprint = Modules.register(new Utility.ToggleSprint());
      sneak = Modules.register(new Utility.ToggleSneak());
      freelook = Modules.register(new Utility.Freelook());
      autoGG = Modules.register(new Utility.AutoGG());
      // Auto-Respawn removed: automation that plays for you is not a fair helper.
      waypoints = Modules.register(new Utility.Waypoints());
      deathPoint = Modules.register(new Utility.DeathPoint());
      hitSound = Modules.register(new Utility.HitSound());
      killSound = Modules.register(new Utility.KillSound());
      mention = Modules.register(new Utility.MentionPing());
      antiSpam = Modules.register(new Utility.AntiSpam());
      wordFilter = Modules.register(new Utility.WordFilter());
      macros = Modules.register(new Utility.Macros());
      shareCoords = Modules.register(new Utility.ShareCoords());
      Utility.Waypoints.registerPersist();
      fakeCheck = Modules.register(new FakeCheck());
      auction = Modules.register(new Auction());
      fightPreset = Modules.register(new FightPreset());
      // Head Search (skull ESP through walls) is excluded.
   }

   public static void onHit(Entity target) {
      try {
         if (hitSound.enabled) {
            hitSound.play();
         }

         PetWorld.onHit();
         if (Overlays.hitmarker != null && Overlays.hitmarker.enabled) {
            Overlays.hitmarker.onHit();
         }
      } catch (Throwable var2) {
         LegoClient.LOG("onHit: " + var2);
      }
   }

   public static void onKill(Entity e) {
      try {
         if (!KILLED.add(Mc.id(e))) {
            return;
         }

         if (KILLED.size() > 200) {
            KILLED.clear();
         }

         if (killSound.enabled) {
            killSound.play();
         }

         PetWorld.onKill();
         if (Overlays.hitmarker != null && Overlays.hitmarker.enabled) {
            Overlays.hitmarker.onKill();
         }

         if (autoGG.enabled && e instanceof Player) {
            autoGG.trigger();
         }
      } catch (Throwable var2) {
         LegoClient.LOG("onKill: " + var2);
      }
   }

   public static boolean allowMessage(Component msg, boolean overlay) {
      if (!overlay && msg != null) {
         try {
            String s = msg.getString();
            if (wordFilter.enabled && wordFilter.blocks(s)) {
               return false;
            }

            if (antiSpam.enabled && antiSpam.blocks(s)) {
               return false;
            }
         } catch (Throwable var3) {
            LegoClient.LOG("Chat-Filter: " + var3);
         }

         return true;
      } else {
         return true;
      }
   }

   public static void onMessage(Component msg, boolean overlay) {
      if (!overlay && msg != null) {
         try {
            String s = msg.getString();
            if (mention.enabled) {
               mention.check(s);
            }

            if (auction != null) {
               auction.onChat(s);
            }
         } catch (Throwable var3) {
            LegoClient.LOG("Chat: " + var3);
         }
      }
   }

   public static void renderWorld(float td) {
      if (waypoints != null) {
         waypoints.render();
      }
   }

   public static void onKey(int key) {
      if (macros.enabled) {
         macros.onKey(key);
      }
   }

   static String dimension() {
      try {
         return Mc.world().dimension().identifier().toString();
      } catch (Throwable var1) {
         return "?";
      }
   }

   static String dimName(String dim) {
      if (dim.endsWith("the_nether")) {
         return "Nether";
      } else if (dim.endsWith("the_end")) {
         return "End";
      } else {
         return dim.endsWith("overworld") ? "Oberwelt" : dim;
      }
   }

   public static Player lookedAtPlayer(double maxRange) {
      LocalPlayer me = Mc.player();
      if (me == null) {
         return null;
      } else {
         Vec3 eye = me.getEyePosition(1.0F);
         Vec3 look = me.getViewVector(1.0F);
         Player best = null;
         double bestDist = Double.MAX_VALUE;

         for (Player p : Mc.players()) {
            if (p != me && !Mc.isSpectator(p)) {
               double cx = Mc.x(p) - eye.x;
               double cy = Mc.y(p) + Mc.height(p) / 2.0F - eye.y;
               double cz = Mc.z(p) - eye.z;
               double along = cx * look.x + cy * look.y + cz * look.z;
               if (!(along <= 0.0) && !(along > maxRange)) {
                  double px = cx - look.x * along;
                  double py = cy - look.y * along;
                  double pz = cz - look.z * along;
                  double horiz = Math.sqrt(px * px + pz * pz);
                  if (horiz <= Mc.width(p) / 2.0F + 0.3 && Math.abs(py) <= Mc.height(p) / 2.0F + 0.3 && along < bestDist) {
                     bestDist = along;
                     best = p;
                  }
               }
            }
         }

         return best;
      }
   }

   public static boolean runAction(Module m) {
      if (m == shareCoords) {
         shareCoords.share();
         return true;
      } else {
         return false;
      }
   }

   public static final class AntiSpam extends Module {
      final Setting.Num window = this.add(new Setting.Num("Zeitfenster", 2.0, 60.0, 1.0, 10.0, " s"));
      private final ArrayDeque<Object[]> recent = new ArrayDeque<>();
      private int hidden = 0;

      AntiSpam() {
         super("antispam", "Anti-Spam", "Blendet gleiche Nachrichten aus, die kurz hintereinander kommen", Category.UTILITY);
         this.icon("filter");
         this.fresh();
      }

      boolean blocks(String s) {
         long now = System.currentTimeMillis();

         while (!this.recent.isEmpty() && now - (Long)this.recent.peekFirst()[1] > this.window.get() * 1000.0) {
            this.recent.pollFirst();
         }

         String k = s.trim().toLowerCase(Locale.ROOT);
         if (k.isEmpty()) {
            return false;
         } else {
            for (Object[] r : this.recent) {
               if (r[0].equals(k)) {
                  this.hidden++;
                  return true;
               }
            }

            this.recent.add(new Object[]{k, now});
            if (this.recent.size() > 100) {
               this.recent.pollFirst();
            }

            return false;
         }
      }
   }

   public static final class AutoGG extends Module {
      final Setting.Text message = this.add(new Setting.Text("Nachricht", "gg", 100, "z.B. gg wp"));
      final Setting.Num delay = this.add(new Setting.Num("Verzögerung", 0.0, 5.0, 0.5, 1.0, " s"));
      final Setting.Num cooldown = this.add(new Setting.Num("Abklingzeit", 2.0, 60.0, 1.0, 10.0, " s"));
      private long last = 0L;
      private long pendingAt = 0L;

      AutoGG() {
         super("autogg", "Auto-GG", "Schreibt nach einem Spieler-Kill automatisch deine Nachricht", Category.UTILITY);
         this.icon("gg");
         this.fresh();
      }

      void trigger() {
         long now = System.currentTimeMillis();
         if (!(now - this.last < this.cooldown.get() * 1000.0)) {
            this.last = now;
            this.pendingAt = now + (long)(this.delay.get() * 1000.0);
         }
      }

      @Override
      public void tick() {
         if (this.pendingAt != 0L && System.currentTimeMillis() >= this.pendingAt) {
            this.pendingAt = 0L;
            String m = this.message.get().trim();
            if (!m.isEmpty() && Mc.mc().getConnection() != null) {
               Mc.mc().getConnection().sendChat(m);
            }
         }
      }
   }

   public static final class AutoRespawn extends Module {
      final Setting.Num delay = this.add(new Setting.Num("Verzögerung", 0.0, 3.0, 0.25, 0.5, " s"));
      private long seen = 0L;

      AutoRespawn() {
         super("autorespawn", "Auto-Respawn", "Respawnt dich automatisch nach dem Tod", Category.UTILITY);
         this.icon("respawn");
         this.fresh();
      }

      @Override
      public void tick() {
         LocalPlayer p = Mc.player();
         if (p != null) {
            if (!(Mc.mc().screen instanceof DeathScreen) && !Mc.isDead(p)) {
               this.seen = 0L;
            } else {
               if (this.seen == 0L) {
                  this.seen = System.currentTimeMillis();
               }

               if (System.currentTimeMillis() - this.seen >= this.delay.get() * 1000.0) {
                  p.respawn();
                  if (Mc.mc().screen instanceof DeathScreen) {
                     Mc.mc().setScreen(null);
                  }

                  this.seen = 0L;
               }
            }
         }
      }
   }

   public static final class DeathPoint extends Module {
      final Setting.Bool chat = this.add(new Setting.Bool("Koordinaten in den Chat (nur für dich)", true));
      private boolean wasDead;

      DeathPoint() {
         super("deathpoint", "Todespunkt", "Merkt sich, wo du gestorben bist (roter Wegpunkt)", Category.UTILITY);
         this.icon("skull");
         this.enabled = true;
         this.fresh();
      }

      @Override
      public void tick() {
         LocalPlayer p = Mc.player();
         if (p != null) {
            boolean dead = Mc.isDead(p);
            if (dead && !this.wasDead) {
               Utility.POINTS.removeIf(w -> w.death);
               double x = Math.floor(Mc.x(p)) + 0.5;
               double y = Math.floor(Mc.y(p));
               double z = Math.floor(Mc.z(p)) + 0.5;
               Utility.POINTS.add(new Utility.Waypoint("Tod", Utility.dimension(), x, y, z, -50356, true));
               Modules.scheduleSave();
               String c = (int)x + " " + (int)y + " " + (int)z;
               Toasts.show("skull", "Todespunkt gespeichert", c + " · " + Utility.dimName(Utility.dimension()), -45730, 5000L);
               if (this.chat.get()) {
                  Mc.chat("§c☠ §7Todespunkt: §f" + c + " §8(" + Utility.dimName(Utility.dimension()) + ")");
               }
            }

            this.wasDead = dead;
         }
      }
   }

   public static final class Freelook extends Module {
      final Setting.Mode keyMode = this.add(new Setting.Mode("Taste", 0, "Linke Alt", "Maus 4", "Maus 5", "Z", "X"));
      final Setting.Bool toggle = this.add(new Setting.Bool("Umschalten statt halten", false));
      final Setting.Bool front = this.add(new Setting.Bool("3. Person während Freelook", true));
      private boolean active;
      private boolean lastDown;
      private boolean toggled;
      private float yaw;
      private float pitch;
      private CameraType savedPerspective;

      Freelook() {
         super("freelook", "Freelook", "Taste halten: Kamera frei um dich drehen, ohne dass du dich umdrehst", Category.UTILITY);
         this.icon("freelook");
         this.fresh();
      }

      private boolean keyDown() {
         switch (this.keyMode.index) {
            case 1:
               return Mc.mouseDown(3);
            case 2:
               return Mc.mouseDown(4);
            case 3:
               return Mc.keyDown(90);
            case 4:
               return Mc.keyDown(88);
            default:
               return Mc.keyDown(342);
         }
      }

      public boolean active() {
         return this.enabled && this.active;
      }

      public float yaw() {
         return this.yaw;
      }

      public float pitch() {
         return this.pitch;
      }

      public void look(double dx, double dy) {
         this.yaw += (float)dx * 0.15F;
         this.pitch = Math.max(-90.0F, Math.min(90.0F, this.pitch + (float)dy * 0.15F));
      }

      @Override
      public void tick() {
         LocalPlayer p = Mc.player();
         if (p != null) {
            boolean down = this.keyDown() && !Mc.screenOpen();
            boolean want;
            if (this.toggle.get()) {
               if (down && !this.lastDown) {
                  this.toggled = !this.toggled;
               }

               want = this.toggled;
            } else {
               want = down;
            }

            this.lastDown = down;
            if (want && !this.active) {
               this.active = true;
               this.yaw = Mc.yaw(p);
               this.pitch = Mc.pitch(p);
               if (this.front.get()) {
                  this.savedPerspective = Mc.perspective();
                  Mc.perspective(CameraType.THIRD_PERSON_BACK);
               }
            } else if (!want && this.active) {
               this.stop();
            }
         }
      }

      private void stop() {
         this.active = false;
         if (this.savedPerspective != null) {
            Mc.perspective(this.savedPerspective);
            this.savedPerspective = null;
         }
      }

      @Override
      public void onDisable() {
         this.toggled = false;
         if (this.active) {
            this.stop();
         }
      }
   }

   public static final class HitSound extends Module {
      final Setting.Mode sound = this.add(new Setting.Mode("Ton", 2, Utility.SOUND_NAMES));
      final Setting.Num volume = this.add(new Setting.Num("Lautstärke", 10.0, 100.0, 5.0, 50.0, "%"));
      final Setting.Num pitch = this.add(new Setting.Num("Tonhöhe", 0.5, 2.0, 0.1, 1.4, ""));

      HitSound() {
         super("hitsound", "Hit-Sound", "Spielt einen Ton, wenn du triffst", Category.UTILITY);
         this.icon("sound");
         this.fresh();
      }

      void play() {
         Sound.play(Utility.SOUND_IDS[this.sound.index], this.pitch.getF(), this.volume.getF() / 100.0F);
      }
   }

   public static final class KillSound extends Module {
      final Setting.Mode sound = this.add(new Setting.Mode("Ton", 3, Utility.SOUND_NAMES));
      final Setting.Num volume = this.add(new Setting.Num("Lautstärke", 10.0, 100.0, 5.0, 70.0, "%"));

      KillSound() {
         super("killsound", "Kill-Sound", "Spielt einen Ton, wenn du jemanden besiegst", Category.UTILITY);
         this.icon("kill");
         this.fresh();
      }

      void play() {
         Sound.play(Utility.SOUND_IDS[this.sound.index], 1.0F, this.volume.getF() / 100.0F);
         Sound.play(Utility.SOUND_IDS[this.sound.index], 1.5F, this.volume.getF() / 140.0F);
      }
   }

   public static final class Macros extends Module {
      final Setting.Text[] text = new Setting.Text[4];
      final Setting.Mode[] key = new Setting.Mode[4];
      private long last = 0L;

      Macros() {
         super("macros", "Auto Text", "Taste drücken = Nachricht oder /Befehl senden (z.B. /home, /spawn)", Category.UTILITY);
         this.icon("macro");
         this.fresh();
         String[] defs = new String[]{"/spawn", "/home", "", ""};

         for (int i = 0; i < 4; i++) {
            this.text[i] = this.add(new Setting.Text("Auto Text " + (i + 1), defs[i], 120, "Text oder /befehl"));
            this.key[i] = this.add(new Setting.Mode("Taste " + (i + 1), i < 2 ? i + 1 : 0, Utility.KEY_NAMES));
         }
      }

      void onKey(int k) {
         if (System.currentTimeMillis() - this.last >= 400L) {
            for (int i = 0; i < 4; i++) {
               if (Utility.KEY_CODES[this.key[i].index] == k) {
                  String s = this.text[i].get().trim();
                  if (!s.isEmpty() && Mc.mc().getConnection() != null) {
                     this.last = System.currentTimeMillis();
                     if (s.startsWith("/")) {
                        Mc.command(s.substring(1));
                     } else {
                        Mc.mc().getConnection().sendChat(s);
                     }

                     Toasts.show("macro", "Auto Text " + (i + 1), s, 0, 1500L);
                  }
               }
            }
         }
      }
   }

   public static final class MentionPing extends Module {
      final Setting.Text extra = this.add(new Setting.Text("Weitere Wörter", "", 120, "Komma-getrennt, z.B. Clan,Spitzname"));
      final Setting.Bool toast = this.add(new Setting.Bool("Hinweis anzeigen", true));
      private long last = 0L;

      MentionPing() {
         super("mention", "Erwähnungs-Ping", "Ton + Hinweis bei @Name im Chat", Category.UTILITY);
         this.icon("bell");
         this.enabled = true;
         this.fresh();
      }

      void check(String s) {
         LocalPlayer p = Mc.player();
         if (p != null) {
            String me = Mc.name(p);
            String low = s.toLowerCase(Locale.ROOT);
            if (!low.startsWith("<" + me.toLowerCase(Locale.ROOT) + ">") && !low.startsWith(me.toLowerCase(Locale.ROOT) + ":")) {
               boolean hit = low.contains("@" + me.toLowerCase(Locale.ROOT));

               for (String w : this.extra.get().split(",")) {
                  w = w.trim().toLowerCase(Locale.ROOT);
                  if (w.length() >= 2 && low.contains("@" + w)) {
                     hit = true;
                  }
               }

               if (hit && System.currentTimeMillis() - this.last >= 1500L) {
                  this.last = System.currentTimeMillis();
                  Sound.play("minecraft:block.note_block.pling", 1.8F, 0.8F);
                  if (this.toast.get()) {
                     Toasts.show("chat", "Du wurdest erwähnt", s.length() > 80 ? s.substring(0, 80) + "…" : s);
                  }
               }
            }
         }
      }
   }

   public static final class SeeModule extends Module {
      final Setting.Mode command;
      final Setting.Num range = this.add(new Setting.Num("Reichweite", 4.0, 128.0, 2.0, 64.0, " Blöcke"));

      SeeModule(String id, String name, String desc, String[] commands) {
         super(id, name, desc, Category.UTILITY);
         this.command = this.add(new Setting.Mode("Befehl", 0, commands));
         this.enabled = true;
         this.icon(id.equals("invsee") ? "backpack" : "box");
      }

      public void use() {
         if (this.enabled) {
            Player target = Utility.lookedAtPlayer(this.range.get());
            if (target == null) {
               Toasts.show("user", this.name, "Kein Spieler im Fadenkreuz", -45730, 1800L);
            } else {
               Mc.command(this.command.get() + " " + Mc.name(target));
            }
         }
      }
   }

   public static final class ShareCoords extends Module {
      final Setting.Mode target = this.add(new Setting.Mode("Ziel", 0, "Zwischenablage", "Chat senden"));

      ShareCoords() {
         super("sharecoords", "Koordinaten teilen", "Taste belegen: kopiert deine Koordinaten oder sendet sie in den Chat", Category.UTILITY);
         this.icon("copy");
         this.fresh();
         this.add(new Setting.Action("Jetzt", "Koordinaten teilen", this::share));
      }

      @Override
      public boolean isToggleable() {
         return false;
      }

      public void share() {
         LocalPlayer p = Mc.player();
         if (p == null) {
            Toasts.show("copy", this.name, "Nur in einer Welt möglich");
         } else {
            String s = "X: "
               + (int)Math.floor(Mc.x(p))
               + " Y: "
               + (int)Math.floor(Mc.y(p))
               + " Z: "
               + (int)Math.floor(Mc.z(p))
               + " ("
               + Utility.dimName(Utility.dimension())
               + ")";
            if (this.target.index == 1 && Mc.mc().getConnection() != null) {
               Mc.mc().getConnection().sendChat(s);
            } else {
               Mc.clipboard(s);
               Toasts.show("copy", "Koordinaten kopiert", s);
            }
         }
      }
   }

   public static final class ToggleSneak extends Module {
      private Boolean saved;

      ToggleSneak() {
         super("togglesneak", "Toggle Sneak", "Schleichen-Taste einmal drücken = an, nochmal = aus", Category.UTILITY);
         this.icon("sneak");
         this.fresh();
      }

      @Override
      public void onEnable() {
         try {
            this.saved = Mc.options().toggleCrouch().get();
            Mc.options().toggleCrouch().set(true);
         } catch (Throwable var2) {
         }
      }

      @Override
      public void onDisable() {
         try {
            Mc.options().toggleCrouch().set(this.saved == null ? Boolean.FALSE : this.saved);
         } catch (Throwable var2) {
         }
      }
   }

   public static final class ToggleSprint extends Module {
      final Setting.Bool hud = this.add(new Setting.Bool("Status in der Actionbar", false));

      ToggleSprint() {
         super("togglesprint", "Toggle Sprint", "Du sprintest automatisch, sobald du nach vorne läufst (wie Vanilla-Sprint gehalten)", Category.UTILITY);
         this.icon("sprint");
         this.enabled = true;
         this.fresh();
      }

      @Override
      public void tick() {
         LocalPlayer p = Mc.player();
         if (p != null && !Mc.screenOpen()) {
            if (!Boolean.TRUE.equals(Mc.options().toggleSprint().get())) {
               Mc.options().keySprint.setDown(true);
               if (this.hud.get() && p.isSprinting()) {
                  Mc.actionBar("§7[§aSprint§7]");
               }
            }
         }
      }

      @Override
      public void onDisable() {
         try {
            Mc.options().keySprint.setDown(false);
         } catch (Throwable var2) {
         }
      }
   }

   public static final class Waypoint {
      public String name;
      public String dim;
      public double x;
      public double y;
      public double z;
      public int color;
      public boolean death;

      Waypoint(String name, String dim, double x, double y, double z, int color, boolean death) {
         this.name = name;
         this.dim = dim;
         this.x = x;
         this.y = y;
         this.z = z;
         this.color = color;
         this.death = death;
      }
   }

   public static final class Waypoints extends Module {
      final Setting.Bool beams = this.add(new Setting.Bool("Lichtstrahl", true));
      final Setting.Bool labels = this.add(new Setting.Bool("Name + Entfernung", true));
      final Setting.Num maxDist = this.add(new Setting.Num("Max. Entfernung", 100.0, 10000.0, 100.0, 3000.0, " Blöcke"));

      Waypoints() {
         super("waypoints", "Wegpunkte", "Markiere Orte mit Lichtstrahl und Entfernung - nur für dich sichtbar", Category.UTILITY);
         this.icon("waypoint");
         this.enabled = true;
         this.fresh();
         this.add(new Setting.Action("Hier setzen", "Wegpunkt setzen", Utility.Waypoints::addHere));
         this.add(new Setting.Action("Letzten löschen", "Löschen", () -> {
            for (int i = Utility.POINTS.size() - 1; i >= 0; i--) {
               if (Utility.POINTS.get(i).dim.equals(Utility.dimension())) {
                  Toasts.show("trash", "Wegpunkt gelöscht", Utility.POINTS.remove(i).name);
                  Modules.scheduleSave();
                  return;
               }
            }

            Toasts.show("waypoint", "Wegpunkte", "Keine Wegpunkte in dieser Dimension");
         }));
         this.add(new Setting.Action("Alle löschen", "Alle löschen", () -> {
            Utility.POINTS.removeIf(w -> w.dim.equals(Utility.dimension()));
            Modules.scheduleSave();
            Toasts.show("trash", "Wegpunkte", "Alle Wegpunkte dieser Dimension gelöscht");
         }));
      }

      public static void addHere() {
         LocalPlayer p = Mc.player();
         if (p == null) {
            Toasts.show("waypoint", "Wegpunkte", "Nur in einer Welt möglich");
         } else {
            int n = 1;

            for (Utility.Waypoint w : Utility.POINTS) {
               if (!w.death) {
                  n++;
               }
            }

            String name = "Punkt " + n;
            Utility.POINTS
               .add(
                  new Utility.Waypoint(
                     name,
                     Utility.dimension(),
                     Math.floor(Mc.x(p)) + 0.5,
                     Math.floor(Mc.y(p)),
                     Math.floor(Mc.z(p)) + 0.5,
                     Utility.WP_COLORS[(n - 1) % Utility.WP_COLORS.length],
                     false
                  )
               );
            Modules.scheduleSave();
            Toasts.show(
               "waypoint", "Wegpunkt gesetzt", name + " · " + (int)Math.floor(Mc.x(p)) + " " + (int)Math.floor(Mc.y(p)) + " " + (int)Math.floor(Mc.z(p))
            );
         }
      }

      void render() {
         LocalPlayer p = Mc.player();
         if (p != null) {
            String dim = Utility.dimension();
            double cx = R3.camX();
            double cy = R3.camY();
            double cz = R3.camZ();
            double t = System.currentTimeMillis() / 1000.0;

            for (Utility.Waypoint w : new ArrayList<>(Utility.POINTS)) {
               if (w.dim.equals(dim) && (w.death ? Utility.deathPoint.enabled : this.enabled)) {
                  double dx = w.x - cx;
                  double dy = w.y + 1.0 - cy;
                  double dz = w.z - cz;
                  double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                  if (!(dist > this.maxDist.get())) {
                     int rgb = w.color & 16777215;
                     double k = dist > 96.0 ? 96.0 / dist : 1.0;
                     double x = cx + dx * k;
                     double z = cz + dz * k;
                     double baseY = cy + (w.y - cy) * k;
                     if (this.beams.get()) {
                        double bw = 0.18 * k * Math.max(1.0, dist / 24.0);
                        double pulse = 0.35 + 0.1 * Math.sin(t * 2.0 + w.x);
                        R3.box(x - bw, baseY, z - bw, x + bw, baseY + 220.0 * k, z + bw, R3.argb(rgb, pulse));
                        R3.box(x - bw * 0.35, baseY, z - bw * 0.35, x + bw * 0.35, baseY + 220.0 * k, z + bw * 0.35, R3.argb(16777215, 0.5));
                        R3.ring(x, baseY + 0.03 * k, z, 0.3 * k, 0.9 * k * Math.max(1.0, dist / 24.0), 32, R3.argb(rgb, 0.6), R3.argb(rgb, 0.0), t);
                     }

                     if (this.labels.get()) {
                        long dd = dist < 50.0 ? Math.round(dist) : (dist < 500.0 ? Math.round(dist / 5.0) * 5L : Math.round(dist / 25.0) * 25L);
                        String s = w.name + "  " + dd + "m";
                        Identifier tex = Visuals2.TextTex.get(s, 0xFF000000 | rgb);
                        double h = Math.max(0.35, dist * k * 0.045);
                        R3.billboard(tex, x, baseY + 2.4 * k + h, z, h * Visuals2.TextTex.aspect(s, 0xFF000000 | rgb), h, 0.0F, 0.0F, 1.0F, 1.0F, -1);
                     }
                  }
               }
            }
         }
      }

      static void registerPersist() {
         Modules.persist(
            "waypoints",
            new Modules.Persist() {
               @Override
               public Object save() {
                  List<Object> out = new ArrayList<>();

                  for (Utility.Waypoint w : Utility.POINTS) {
                     Map<String, Object> m = new LinkedHashMap<>();
                     m.put("name", w.name);
                     m.put("dim", w.dim);
                     m.put("x", w.x);
                     m.put("y", w.y);
                     m.put("z", w.z);
                     m.put("color", (long)w.color);
                     m.put("death", w.death);
                     m.put("server", Mc.inGame() ? Mc.serverAddress() : "");
                     out.add(m);
                  }

                  return out;
               }

               @Override
               public void load(Object o) {
                  if (o instanceof List) {
                     Utility.POINTS.clear();

                     for (Object e : (List)o) {
                        if (e instanceof Map<?, ?> m) {
                           try {
                              Utility.POINTS
                                 .add(
                                    new Utility.Waypoint(
                                       String.valueOf(m.get("name")),
                                       String.valueOf(m.get("dim")),
                                       ((Number)m.get("x")).doubleValue(),
                                       ((Number)m.get("y")).doubleValue(),
                                       ((Number)m.get("z")).doubleValue(),
                                       ((Number)m.get("color")).intValue(),
                                       Boolean.TRUE.equals(m.get("death"))
                                    )
                                 );
                           } catch (Throwable var6) {
                           }
                        }
                     }
                  }
               }
            }
         );
      }
   }

   public static final class WordFilter extends Module {
      final Setting.Text words = this.add(new Setting.Text("Wörter", "", 200, "Komma-getrennt"));

      WordFilter() {
         super("wordfilter", "Wort-Filter", "Versteckt Chat-Nachrichten mit bestimmten Wörtern", Category.UTILITY);
         this.icon("filter");
         this.fresh();
      }

      boolean blocks(String s) {
         String low = s.toLowerCase(Locale.ROOT);

         for (String w : this.words.get().split(",")) {
            w = w.trim().toLowerCase(Locale.ROOT);
            if (w.length() >= 2 && low.contains(w)) {
               return true;
            }
         }

         return false;
      }
   }
}
