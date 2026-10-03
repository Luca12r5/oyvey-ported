package dev.lego.util;

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
import java.util.Set;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

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
   public static Utility.LowHpWarning lowHp;
   public static Utility.TotemWarning totemWarn;
   public static Utility.ArmorWarning armorWarn;
   public static Utility.EffectWarning effectWarn;
   public static Utility.HitSound hitSound;
   public static Utility.KillSound killSound;
   public static Utility.MentionPing mention;
   public static Utility.AntiSpam antiSpam;
   public static Utility.WordFilter wordFilter;
   public static Utility.Macros macros;
   public static Utility.ShareCoords shareCoords;
   public static Utility.BreakReminder breakReminder;
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
      autoRespawn = Modules.register(new Utility.AutoRespawn());
      waypoints = Modules.register(new Utility.Waypoints());
      deathPoint = Modules.register(new Utility.DeathPoint());
      lowHp = Modules.register(new Utility.LowHpWarning());
      totemWarn = Modules.register(new Utility.TotemWarning());
      armorWarn = Modules.register(new Utility.ArmorWarning());
      effectWarn = Modules.register(new Utility.EffectWarning());
      hitSound = Modules.register(new Utility.HitSound());
      killSound = Modules.register(new Utility.KillSound());
      mention = Modules.register(new Utility.MentionPing());
      antiSpam = Modules.register(new Utility.AntiSpam());
      wordFilter = Modules.register(new Utility.WordFilter());
      macros = Modules.register(new Utility.Macros());
      shareCoords = Modules.register(new Utility.ShareCoords());
      breakReminder = Modules.register(new Utility.BreakReminder());
      Utility.Waypoints.registerPersist();
   }

   public static void onHit(Entity var0) {
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

   public static void onKill(Entity var0) {
      try {
         if (!KILLED.add(Mc.id(var0))) {
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

         if (autoGG.enabled && var0 instanceof Player) {
            autoGG.trigger();
         }
      } catch (Throwable var2) {
         LegoClient.LOG("onKill: " + var2);
      }
   }

   public static boolean allowMessage(Component var0, boolean var1) {
      if (!var1 && var0 != null) {
         try {
            String var2 = var0.getString();
            if (wordFilter.enabled && wordFilter.blocks(var2)) {
               return false;
            }

            if (antiSpam.enabled && antiSpam.blocks(var2)) {
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

   public static void onMessage(Component var0, boolean var1) {
      if (!var1 && var0 != null) {
         try {
            if (mention.enabled) {
               mention.check(var0.getString());
            }
         } catch (Throwable var3) {
            LegoClient.LOG("Chat: " + var3);
         }
      }
   }

   public static void renderWorld(float var0) {
      if (waypoints != null) {
         waypoints.render();
      }
   }

   public static void onKey(int var0) {
      if (macros.enabled) {
         macros.onKey(var0);
      }
   }

   static String dimension() {
      try {
         return Mc.world().dimension().identifier().toString();
      } catch (Throwable var1) {
         return "?";
      }
   }

   static String dimName(String var0) {
      if (var0.endsWith("the_nether")) {
         return "Nether";
      } else if (var0.endsWith("the_end")) {
         return "End";
      } else {
         return var0.endsWith("overworld") ? "Oberwelt" : var0;
      }
   }

   public static Player lookedAtPlayer(double var0) {
      LocalPlayer var2 = Mc.player();
      if (var2 == null) {
         return null;
      } else {
         Vec3 var3 = var2.getEyePosition(1.0F);
         Vec3 var4 = var2.getViewVector(1.0F);
         Player var5 = null;
         double var6 = Double.MAX_VALUE;

         for (Player var9 : Mc.players()) {
            if (var9 != var2 && !Mc.isSpectator(var9)) {
               double var10 = Mc.x(var9) - var3.x;
               double var12 = Mc.y(var9) + Mc.height(var9) / 2.0F - var3.y;
               double var14 = Mc.z(var9) - var3.z;
               double var16 = var10 * var4.x + var12 * var4.y + var14 * var4.z;
               if (!(var16 <= 0.0) && !(var16 > var0)) {
                  double var18 = var10 - var4.x * var16;
                  double var20 = var12 - var4.y * var16;
                  double var22 = var14 - var4.z * var16;
                  double var24 = Math.sqrt(var18 * var18 + var22 * var22);
                  if (var24 <= Mc.width(var9) / 2.0F + 0.3 && Math.abs(var20) <= Mc.height(var9) / 2.0F + 0.3 && var16 < var6) {
                     var6 = var16;
                     var5 = var9;
                  }
               }
            }
         }

         return var5;
      }
   }

   public static boolean runAction(Module var0) {
      if (var0 == shareCoords) {
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

      boolean blocks(String var1) {
         long var2 = System.currentTimeMillis();

         while (!this.recent.isEmpty() && var2 - this.recent.peekFirst()[1] > this.window.get() * 1000.0) {
            this.recent.pollFirst();
         }

         String var4 = var1.trim().toLowerCase(Locale.ROOT);
         if (var4.isEmpty()) {
            return false;
         } else {
            for (Object[] var6 : this.recent) {
               if (var6[0].equals(var4)) {
                  this.hidden++;
                  return true;
               }
            }

            this.recent.add(new Object[]{var4, var2});
            if (this.recent.size() > 100) {
               this.recent.pollFirst();
            }

            return false;
         }
      }
   }

   public static final class ArmorWarning extends Module {
      final Setting.Num limit = this.add(new Setting.Num("Grenze", 5.0, 40.0, 5.0, 15.0, "%"));
      private final boolean[] warned = new boolean[4];
      private static final String[] NAMES = new String[]{"Helm", "Brustplatte", "Hose", "Schuhe"};

      ArmorWarning() {
         super("armorwarn", "Rüstungs-Warnung", "Warnt, wenn ein Rüstungsteil fast kaputt ist", Category.UTILITY);
         this.icon("armor");
         this.enabled = true;
         this.fresh();
      }

      @Override
      public void tick() {
         for (int var1 = 0; var1 < 4; var1++) {
            ItemStack var2 = Mc.armor(39 - var1);
            if (!Mc.empty(var2) && Mc.damageable(var2)) {
               double var3 = 1.0 - (double)Mc.damage(var2) / Math.max(1, Mc.maxDamage(var2));
               if (var3 * 100.0 <= this.limit.get()) {
                  if (!this.warned[var1]) {
                     this.warned[var1] = true;
                     Toasts.show("armor", NAMES[var1] + " fast kaputt", Math.round(var3 * 100.0) + "% Haltbarkeit", -30208, 3500L);
                     Sound.play("minecraft:entity.item.break", 1.3F, 0.5F);
                  }
               } else if (var3 * 100.0 > this.limit.get() + 5.0) {
                  this.warned[var1] = false;
               }
            } else {
               this.warned[var1] = false;
            }
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
         long var1 = System.currentTimeMillis();
         if (!(var1 - this.last < this.cooldown.get() * 1000.0)) {
            this.last = var1;
            this.pendingAt = var1 + (long)(this.delay.get() * 1000.0);
         }
      }

      @Override
      public void tick() {
         if (this.pendingAt != 0L && System.currentTimeMillis() >= this.pendingAt) {
            this.pendingAt = 0L;
            String var1 = this.message.get().trim();
            if (!var1.isEmpty() && Mc.mc().getConnection() != null) {
               Mc.mc().getConnection().sendChat(var1);
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
         LocalPlayer var1 = Mc.player();
         if (var1 != null) {
            if (!(Mc.mc().screen instanceof DeathScreen) && !Mc.isDead(var1)) {
               this.seen = 0L;
            } else {
               if (this.seen == 0L) {
                  this.seen = System.currentTimeMillis();
               }

               if (System.currentTimeMillis() - this.seen >= this.delay.get() * 1000.0) {
                  var1.respawn();
                  if (Mc.mc().screen instanceof DeathScreen) {
                     Mc.mc().setScreen(null);
                  }

                  this.seen = 0L;
               }
            }
         }
      }
   }

   public static final class BreakReminder extends Module {
      final Setting.Num minutes = this.add(new Setting.Num("Alle", 15.0, 180.0, 15.0, 60.0, " min"));
      private long since = 0L;

      BreakReminder() {
         super("breakreminder", "Pausen-Erinnerung", "Erinnert dich nach einer Weile an eine kurze Pause", Category.UTILITY);
         this.icon("clock");
         this.fresh();
      }

      @Override
      public void tick() {
         long var1 = System.currentTimeMillis();
         if (this.since == 0L) {
            this.since = var1;
         }

         if (var1 - this.since >= this.minutes.get() * 60000.0) {
            this.since = var1;
            Toasts.show("clock", "Zeit für eine Pause", "Du spielst seit " + this.minutes.getI() + " Minuten - kurz aufstehen & trinken", -16735270, 8000L);
            Sound.play("minecraft:block.note_block.chime", 1.2F, 0.6F);
         }
      }

      @Override
      public void onEnable() {
         this.since = 0L;
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
         LocalPlayer var1 = Mc.player();
         if (var1 != null) {
            boolean var2 = Mc.isDead(var1);
            if (var2 && !this.wasDead) {
               Utility.POINTS.removeIf(var0 -> var0.death);
               double var3 = Math.floor(Mc.x(var1)) + 0.5;
               double var5 = Math.floor(Mc.y(var1));
               double var7 = Math.floor(Mc.z(var1)) + 0.5;
               Utility.POINTS.add(new Utility.Waypoint("Tod", Utility.dimension(), var3, var5, var7, -50356, true));
               Modules.scheduleSave();
               String var9 = (int)var3 + " " + (int)var5 + " " + (int)var7;
               Toasts.show("skull", "Todespunkt gespeichert", var9 + " · " + Utility.dimName(Utility.dimension()), -45730, 5000L);
               if (this.chat.get()) {
                  Mc.chat("§c☠ §7Todespunkt: §f" + var9 + " §8(" + Utility.dimName(Utility.dimension()) + ")");
               }
            }

            this.wasDead = var2;
         }
      }
   }

   public static final class EffectWarning extends Module {
      final Setting.Num seconds = this.add(new Setting.Num("Vorwarnung", 3.0, 30.0, 1.0, 5.0, " s"));
      private final Set<String> warned = new HashSet<>();

      EffectWarning() {
         super("effectwarn", "Effekt-Warnung", "Meldet, kurz bevor ein Trank-Effekt ausläuft", Category.UTILITY);
         this.icon("potion");
         this.fresh();
      }

      @Override
      public void tick() {
         HashSet var1 = new HashSet();

         for (MobEffectInstance var4 : Mc.effects()) {
            String var5 = var4.getDescriptionId();
            var1.add(var5);
            int var6 = var4.getDuration();
            if (var6 > 0 && var6 <= this.seconds.get() * 20.0) {
               if (this.warned.add(var5)) {
                  Toasts.show("potion", Component.translatable(var5).getString() + " läuft aus", "noch " + Math.max(1, var6 / 20) + " s", -6599222, 2500L);
                  Sound.play("minecraft:block.note_block.chime", 1.4F, 0.5F);
               }
            } else if (var6 > this.seconds.get() * 20.0 + 40.0) {
               this.warned.remove(var5);
            }
         }

         this.warned.retainAll(var1);
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

      public void look(double var1, double var3) {
         this.yaw += (float)var1 * 0.15F;
         this.pitch = Math.max(-90.0F, Math.min(90.0F, this.pitch + (float)var3 * 0.15F));
      }

      @Override
      public void tick() {
         LocalPlayer var1 = Mc.player();
         if (var1 != null) {
            boolean var2 = this.keyDown() && !Mc.screenOpen();
            boolean var3;
            if (this.toggle.get()) {
               if (var2 && !this.lastDown) {
                  this.toggled = !this.toggled;
               }

               var3 = this.toggled;
            } else {
               var3 = var2;
            }

            this.lastDown = var2;
            if (var3 && !this.active) {
               this.active = true;
               this.yaw = Mc.yaw(var1);
               this.pitch = Mc.pitch(var1);
               if (this.front.get()) {
                  this.savedPerspective = Mc.perspective();
                  Mc.perspective(CameraType.THIRD_PERSON_BACK);
               }
            } else if (!var3 && this.active) {
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

   public static final class LowHpWarning extends Module {
      final Setting.Num limit = this.add(new Setting.Num("Grenze", 2.0, 14.0, 1.0, 6.0, " ♥"));
      final Setting.Bool sound = this.add(new Setting.Bool("Warnton", true));
      private boolean warned;

      LowHpWarning() {
         super("lowhp", "Low-HP-Warnung", "Warnt dich mit Ton + Hinweis, wenn dein Leben niedrig ist", Category.UTILITY);
         this.icon("heart");
         this.enabled = true;
         this.fresh();
      }

      @Override
      public void tick() {
         LocalPlayer var1 = Mc.player();
         if (var1 != null && !var1.isCreative() && !Mc.isDead(var1)) {
            float var2 = Mc.health(var1) + Mc.absorption(var1);
            if (var2 <= this.limit.get() * 2.0) {
               if (!this.warned) {
                  this.warned = true;
                  Toasts.show("heart", "Wenig Leben!", String.format(Locale.ROOT, "%.1f ♥ übrig", var2 / 2.0F), -50356, 2500L);
                  if (this.sound.get()) {
                     Sound.play("minecraft:block.note_block.bit", 0.7F, 0.9F);
                  }
               }
            } else if (var2 > this.limit.get() * 2.0 + 2.0) {
               this.warned = false;
            }
         } else {
            this.warned = false;
         }
      }
   }

   public static final class Macros extends Module {
      final Setting.Text[] text = new Setting.Text[4];
      final Setting.Mode[] key = new Setting.Mode[4];
      private long last = 0L;

      Macros() {
         super("macros", "Makros", "Taste drückt = Nachricht oder /Befehl senden (z.B. /home, /spawn)", Category.UTILITY);
         this.icon("macro");
         this.fresh();
         String[] var1 = new String[]{"/spawn", "/home", "", ""};

         for (int var2 = 0; var2 < 4; var2++) {
            this.text[var2] = this.add(new Setting.Text("Makro " + (var2 + 1), var1[var2], 120, "Text oder /befehl"));
            this.key[var2] = this.add(new Setting.Mode("Taste " + (var2 + 1), var2 < 2 ? var2 + 1 : 0, Utility.KEY_NAMES));
         }
      }

      void onKey(int var1) {
         if (System.currentTimeMillis() - this.last >= 400L) {
            for (int var2 = 0; var2 < 4; var2++) {
               if (Utility.KEY_CODES[this.key[var2].index] == var1) {
                  String var3 = this.text[var2].get().trim();
                  if (!var3.isEmpty() && Mc.mc().getConnection() != null) {
                     this.last = System.currentTimeMillis();
                     if (var3.startsWith("/")) {
                        Mc.command(var3.substring(1));
                     } else {
                        Mc.mc().getConnection().sendChat(var3);
                     }

                     Toasts.show("macro", "Makro " + (var2 + 1), var3, 0, 1500L);
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
         super("mention", "Erwähnungs-Ping", "Ton + Hinweis, wenn jemand deinen Namen im Chat schreibt", Category.UTILITY);
         this.icon("bell");
         this.enabled = true;
         this.fresh();
      }

      void check(String var1) {
         LocalPlayer var2 = Mc.player();
         if (var2 != null) {
            String var3 = Mc.name(var2);
            String var4 = var1.toLowerCase(Locale.ROOT);
            if (!var4.startsWith("<" + var3.toLowerCase(Locale.ROOT) + ">") && !var4.startsWith(var3.toLowerCase(Locale.ROOT) + ":")) {
               boolean var5 = var4.contains(var3.toLowerCase(Locale.ROOT));

               for (String var9 : this.extra.get().split(",")) {
                  var9 = var9.trim().toLowerCase(Locale.ROOT);
                  if (var9.length() >= 2 && var4.contains(var9)) {
                     var5 = true;
                  }
               }

               if (var5 && System.currentTimeMillis() - this.last >= 1500L) {
                  this.last = System.currentTimeMillis();
                  Sound.play("minecraft:block.note_block.pling", 1.8F, 0.8F);
                  if (this.toast.get()) {
                     Toasts.show("chat", "Du wurdest erwähnt", var1.length() > 80 ? var1.substring(0, 80) + "…" : var1);
                  }
               }
            }
         }
      }
   }

   public static final class SeeModule extends Module {
      final Setting.Mode command;
      final Setting.Num range = this.add(new Setting.Num("Reichweite", 4.0, 128.0, 2.0, 64.0, " Blöcke"));

      SeeModule(String var1, String var2, String var3, String[] var4) {
         super(var1, var2, var3, Category.UTILITY);
         this.command = this.add(new Setting.Mode("Befehl", 0, var4));
         this.enabled = true;
         this.icon(var1.equals("invsee") ? "backpack" : "box");
      }

      public void use() {
         if (this.enabled) {
            Player var1 = Utility.lookedAtPlayer(this.range.get());
            if (var1 == null) {
               Toasts.show("user", this.name, "Kein Spieler im Fadenkreuz", -45730, 1800L);
            } else {
               Mc.command(this.command.get() + " " + Mc.name(var1));
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
         LocalPlayer var1 = Mc.player();
         if (var1 == null) {
            Toasts.show("copy", this.name, "Nur in einer Welt möglich");
         } else {
            String var2 = "X: "
               + (int)Math.floor(Mc.x(var1))
               + " Y: "
               + (int)Math.floor(Mc.y(var1))
               + " Z: "
               + (int)Math.floor(Mc.z(var1))
               + " ("
               + Utility.dimName(Utility.dimension())
               + ")";
            if (this.target.index == 1 && Mc.mc().getConnection() != null) {
               Mc.mc().getConnection().sendChat(var2);
            } else {
               Mc.clipboard(var2);
               Toasts.show("copy", "Koordinaten kopiert", var2);
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
         LocalPlayer var1 = Mc.player();
         if (var1 != null && !Mc.screenOpen()) {
            if (!Boolean.TRUE.equals(Mc.options().toggleSprint().get())) {
               Mc.options().keySprint.setDown(true);
               if (this.hud.get() && var1.isSprinting()) {
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

   public static final class TotemWarning extends Module {
      final Setting.Bool sound = this.add(new Setting.Bool("Warnton", true));
      private int last = -1;

      TotemWarning() {
         super("totemwarn", "Totem-Warnung", "Meldet, wenn du ein Totem verbrauchst und wie viele noch übrig sind", Category.UTILITY);
         this.icon("totem");
         this.enabled = true;
         this.fresh();
      }

      @Override
      public void tick() {
         int var1 = Mc.countItem("totem_of_undying");
         if (this.last > 0 && var1 < this.last) {
            Toasts.show("totem", var1 == 0 ? "Letztes Totem weg!" : "Totem verbraucht", var1 + " übrig", var1 == 0 ? -50356 : -13053, 3000L);
            if (this.sound.get()) {
               Sound.play("minecraft:block.note_block.bell", var1 == 0 ? 0.6F : 1.2F, 0.8F);
            }
         }

         this.last = var1;
      }

      @Override
      public void onDisable() {
         this.last = -1;
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

      Waypoint(String var1, String var2, double var3, double var5, double var7, int var9, boolean var10) {
         this.name = var1;
         this.dim = var2;
         this.x = var3;
         this.y = var5;
         this.z = var7;
         this.color = var9;
         this.death = var10;
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
            for (int var0 = Utility.POINTS.size() - 1; var0 >= 0; var0--) {
               if (Utility.POINTS.get(var0).dim.equals(Utility.dimension())) {
                  Toasts.show("trash", "Wegpunkt gelöscht", Utility.POINTS.remove(var0).name);
                  Modules.scheduleSave();
                  return;
               }
            }

            Toasts.show("waypoint", "Wegpunkte", "Keine Wegpunkte in dieser Dimension");
         }));
         this.add(new Setting.Action("Alle löschen", "Alle löschen", () -> {
            Utility.POINTS.removeIf(var0 -> var0.dim.equals(Utility.dimension()));
            Modules.scheduleSave();
            Toasts.show("trash", "Wegpunkte", "Alle Wegpunkte dieser Dimension gelöscht");
         }));
      }

      public static void addHere() {
         LocalPlayer var0 = Mc.player();
         if (var0 == null) {
            Toasts.show("waypoint", "Wegpunkte", "Nur in einer Welt möglich");
         } else {
            int var1 = 1;

            for (Utility.Waypoint var3 : Utility.POINTS) {
               if (!var3.death) {
                  var1++;
               }
            }

            String var4 = "Punkt " + var1;
            Utility.POINTS
               .add(
                  new Utility.Waypoint(
                     var4,
                     Utility.dimension(),
                     Math.floor(Mc.x(var0)) + 0.5,
                     Math.floor(Mc.y(var0)),
                     Math.floor(Mc.z(var0)) + 0.5,
                     Utility.WP_COLORS[(var1 - 1) % Utility.WP_COLORS.length],
                     false
                  )
               );
            Modules.scheduleSave();
            Toasts.show(
               "waypoint",
               "Wegpunkt gesetzt",
               var4 + " · " + (int)Math.floor(Mc.x(var0)) + " " + (int)Math.floor(Mc.y(var0)) + " " + (int)Math.floor(Mc.z(var0))
            );
         }
      }

      void render() {
         LocalPlayer var1 = Mc.player();
         if (var1 != null) {
            String var2 = Utility.dimension();
            double var3 = R3.camX();
            double var5 = R3.camY();
            double var7 = R3.camZ();
            double var9 = System.currentTimeMillis() / 1000.0;

            for (Utility.Waypoint var12 : new ArrayList<>(Utility.POINTS)) {
               if (var12.dim.equals(var2) && (var12.death ? Utility.deathPoint.enabled : this.enabled)) {
                  double var13 = var12.x - var3;
                  double var15 = var12.y + 1.0 - var5;
                  double var17 = var12.z - var7;
                  double var19 = Math.sqrt(var13 * var13 + var15 * var15 + var17 * var17);
                  if (!(var19 > this.maxDist.get())) {
                     int var21 = var12.color & 16777215;
                     double var22 = var19 > 96.0 ? 96.0 / var19 : 1.0;
                     double var24 = var3 + var13 * var22;
                     double var26 = var7 + var17 * var22;
                     double var28 = var5 + (var12.y - var5) * var22;
                     if (this.beams.get()) {
                        double var30 = 0.18 * var22 * Math.max(1.0, var19 / 24.0);
                        double var32 = 0.35 + 0.1 * Math.sin(var9 * 2.0 + var12.x);
                        R3.box(var24 - var30, var28, var26 - var30, var24 + var30, var28 + 220.0 * var22, var26 + var30, R3.argb(var21, var32));
                        R3.box(
                           var24 - var30 * 0.35,
                           var28,
                           var26 - var30 * 0.35,
                           var24 + var30 * 0.35,
                           var28 + 220.0 * var22,
                           var26 + var30 * 0.35,
                           R3.argb(16777215, 0.5)
                        );
                        R3.ring(
                           var24,
                           var28 + 0.03 * var22,
                           var26,
                           0.3 * var22,
                           0.9 * var22 * Math.max(1.0, var19 / 24.0),
                           32,
                           R3.argb(var21, 0.6),
                           R3.argb(var21, 0.0),
                           var9
                        );
                     }

                     if (this.labels.get()) {
                        long var36 = var19 < 50.0 ? Math.round(var19) : (var19 < 500.0 ? Math.round(var19 / 5.0) * 5L : Math.round(var19 / 25.0) * 25L);
                        String var37 = var12.name + "  " + var36 + "m";
                        Identifier var33 = Visuals2.TextTex.get(var37, 0xFF000000 | var21);
                        double var34 = Math.max(0.35, var19 * var22 * 0.045);
                        R3.billboard(
                           var33,
                           var24,
                           var28 + 2.4 * var22 + var34,
                           var26,
                           var34 * Visuals2.TextTex.aspect(var37, 0xFF000000 | var21),
                           var34,
                           0.0F,
                           0.0F,
                           1.0F,
                           1.0F,
                           -1
                        );
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
                  ArrayList var1 = new ArrayList();

                  for (Utility.Waypoint var3 : Utility.POINTS) {
                     LinkedHashMap var4 = new LinkedHashMap();
                     var4.put("name", var3.name);
                     var4.put("dim", var3.dim);
                     var4.put("x", var3.x);
                     var4.put("y", var3.y);
                     var4.put("z", var3.z);
                     var4.put("color", (long)var3.color);
                     var4.put("death", var3.death);
                     var4.put("server", Mc.inGame() ? Mc.serverAddress() : "");
                     var1.add(var4);
                  }

                  return var1;
               }

               @Override
               public void load(Object var1) {
                  if (var1 instanceof List) {
                     Utility.POINTS.clear();

                     for (Object var3 : (List)var1) {
                        if (var3 instanceof Map var4) {
                           try {
                              Utility.POINTS
                                 .add(
                                    new Utility.Waypoint(
                                       String.valueOf(var4.get("name")),
                                       String.valueOf(var4.get("dim")),
                                       ((Number)var4.get("x")).doubleValue(),
                                       ((Number)var4.get("y")).doubleValue(),
                                       ((Number)var4.get("z")).doubleValue(),
                                       ((Number)var4.get("color")).intValue(),
                                       Boolean.TRUE.equals(var4.get("death"))
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

      boolean blocks(String var1) {
         String var2 = var1.toLowerCase(Locale.ROOT);

         for (String var6 : this.words.get().split(",")) {
            var6 = var6.trim().toLowerCase(Locale.ROOT);
            if (var6.length() >= 2 && var2.contains(var6)) {
               return true;
            }
         }

         return false;
      }
   }
}
