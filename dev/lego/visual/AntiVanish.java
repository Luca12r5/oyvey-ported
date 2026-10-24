package dev.lego.visual;

import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Gx;
import dev.lego.ui.Style;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.imageio.ImageIO;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class AntiVanish extends Module {
   final Setting.Text staffNames = this.add(new Setting.Text("Staff-Namen", "", 512, "Name1,Name2,..."));
   final Setting.Bool completionProbe = this.add(new Setting.Bool("Completion-Probe", true));
   final Setting.Bool chatNotify = this.add(new Setting.Bool("Chat-Benachrichtigung", true));
   final Setting.Bool showHud = this.add(new Setting.Bool("HUD anzeigen", true));
   final Setting.Bool starDetect = this.add(new Setting.Bool("Stern-Erkennung", true));
   final Setting.Num hudScale = this.add(new Setting.Num("HUD-Größe", 50.0, 150.0, 5.0, 100.0, "%"));
   final Setting.Bool testMode = this.add(new Setting.Bool("Test-Modus", false));
   private static final String PROBE_COMMAND = "minecraft:msg";
   private static final long SIGNAL_WINDOW_MS = 15000L;
   private static final long DETECTION_TTL_MS = 20000L;
   private static final long ANNOUNCE_COOLDOWN_MS = 1500L;
   private static final long RECENT_MESSAGE_TTL_MS = 8000L;
   private static final long TRUSTED_LISTED_MS = 1500L;
   private static final double SENSOR_RANGE = 64.0;
   private static final int MAX_OBS_PER_TICK = 512;
   private final ConcurrentLinkedQueue<AntiVanish.Obs> observations = new ConcurrentLinkedQueue<>();
   private final Map<UUID, AntiVanish.KnownPlayer> knownPlayers = new HashMap<>();
   private final Map<String, AntiVanish.Detection> detections = new LinkedHashMap<>();
   private final Map<String, Long> signalCooldowns = new HashMap<>();
   private final Map<String, Long> announceCooldowns = new HashMap<>();
   private final Deque<Long> announceTimes = new ArrayDeque<>();
   private final Deque<AntiVanish.Signal> signals = new ArrayDeque<>();
   private final Deque<AntiVanish.PendingVanish> pendingVanishes = new ArrayDeque<>();
   private final Map<String, AntiVanish.HiddenSpell> hiddenSpells = new LinkedHashMap<>();
   private final Deque<AntiVanish.RecentMsg> recentMessages = new ArrayDeque<>();
   private final Map<UUID, Long> listedSinceMs = new HashMap<>();
   private final Set<Integer> completionIds = ConcurrentHashMap.newKeySet();
   private final Set<String> staffSet = new HashSet<>();
   private final Set<String> starred = new HashSet<>();
   private volatile List<String> pendingCompNames;
   private boolean serverSendsLeave;
   private int nextCompId = 30000;
   private int tickCnt;
   private long lastCriticalMs;
   private long criticalUntilMs;
   private String criticalName = "";

   public AntiVanish() {
      super("antivanish", "Anti Vanish", "Erkennt unsichtbare Staff-Mitglieder auf dem Server", Category.UTILITY);
      this.icon("eye");
      this.fresh();
   }

   @Override
   public void onEnable() {
      this.resetAll();
      this.parseStaffNames();
      if (Mc.inGame()) {
         this.trackListed();
      }
   }

   @Override
   public void onDisable() {
      this.resetAll();
   }

   @Override
   public void tick() {
      if (!Mc.inGame()) {
         this.resetAll();
      } else {
         this.tickCnt++;
         this.drainObs();
         this.processPendingVanishes();
         this.processCompletionProbe();
         if (this.completionProbe.get() && this.tickCnt % 100 == 0) {
            this.sendCompletionProbe();
         }

         if (this.tickCnt % 10 == 0) {
            this.trackListed();
            this.reviewSpells();
            this.parseStaffNames();
         }

         if (this.tickCnt % 5 == 0) {
            this.scanInvisible();
         }

         this.pruneState();
      }
   }

   public static void onPlayerRemove(List<UUID> ids) {
      AntiVanish m = instance();
      if (m != null && m.enabled) {
         if (ids.size() < 4) {
            for (UUID id : ids) {
               m.observations.offer(AntiVanish.Obs.tabRemove(id));
            }
         }
      }
   }

   public static void onPlayerUpdate(UUID id, boolean listed, String name, String gameMode) {
      AntiVanish m = instance();
      if (m != null && m.enabled) {
         if (listed || name != null || gameMode != null) {
            if (name == null || !listed) {
               if (!listed && name == null) {
                  m.observations.offer(AntiVanish.Obs.tabHide(id));
               }

               if (gameMode != null) {
                  m.observations.offer(AntiVanish.Obs.gamemode(id, gameMode));
               }
            }
         }
      }
   }

   public static void onSystemChat(String text) {
      AntiVanish m = instance();
      if (m != null && m.enabled) {
         if (text != null && !text.isBlank()) {
            m.observations.offer(AntiVanish.Obs.systemChat(text));
         }
      }
   }

   public static boolean onCommandSuggestions(int id, List<String> names) {
      AntiVanish m = instance();
      if (m != null && m.enabled) {
         if (!m.completionIds.remove(id)) {
            return false;
         } else {
            m.pendingCompNames = new ArrayList<>(names);
            return true;
         }
      } else {
         return false;
      }
   }

   public static void onEntityMetadata(int entityId) {
      AntiVanish m = instance();
      if (m != null && m.enabled) {
         m.observations.offer(AntiVanish.Obs.entityMeta(entityId));
      }
   }

   public static void onSound(double x, double y, double z, String soundId) {
      AntiVanish m = instance();
      if (m != null && m.enabled) {
         if (suspiciousSound(soundId)) {
            m.observations.offer(AntiVanish.Obs.position(AntiVanish.ObsType.SOUND, x, y, z, soundId));
         }
      }
   }

   public static void onParticle(double x, double y, double z, String particleId) {
      AntiVanish m = instance();
      if (m != null && m.enabled) {
         if (suspiciousParticle(particleId)) {
            m.observations.offer(AntiVanish.Obs.position(AntiVanish.ObsType.PARTICLE, x, y, z, particleId));
         }
      }
   }

   public void renderHud(int W, int H) {
      if (this.showHud.get() && Mc.inGame()) {
         long now = System.currentTimeMillis();
         List<AntiVanish.HudRow> rows = this.hudRows(now);
         if (!rows.isEmpty()) {
            float scale = this.hudScale.getF() / 100.0F;
            int S = Gx.S;
            int pad = Math.round(8 * S * scale);
            int headSz = Math.round(16 * S * scale);
            int rowH = Math.round(22 * S * scale);
            int panelW = Math.round(180 * S * scale);
            int headerH = Math.round(20 * S * scale);
            int panelH = headerH + rows.size() * rowH + pad;
            int px = W - panelW - pad;
            Gx.rect(px, pad, panelW, panelH, Math.round(8 * S * scale), Gx.withAlpha(Style.bg, 0.85F));
            Gx.outline(px, pad, panelW, panelH, Math.round(8 * S * scale), Math.max(1, S), Style.stroke);
            float titleFs = 7.5F * S * scale;
            Gx.textMid("ANTI VANISH", px + pad, pad + headerH / 2.0F, titleFs, 3, Style.muted);
            String cnt = String.valueOf(rows.size());
            Gx.textRight(cnt, px + panelW - pad, pad + headerH / 2.0F, titleFs, 3, Style.muted);
            int ry = pad + headerH;

            for (AntiVanish.HudRow row : rows) {
               boolean alert = "CRITICAL".equals(row.tag);
               int tagCol = alert ? -47814 : -22016;
               float nameFs = 8.5F * S * scale;
               float tagFs = 6.5F * S * scale;
               Gx.Img face = AntiVanish.FaceSkin.getFace(row.name);
               if (face != null) {
                  Gx.image(face, px + pad, ry + (rowH - headSz) / 2, headSz, headSz, -1);
               } else {
                  Gx.rect(px + pad, ry + (rowH - headSz) / 2, headSz, headSz, Math.round(2 * S * scale), -14013910);
               }

               int textX = px + pad + headSz + Math.round(6 * S * scale);
               Gx.textMid(row.name, textX, ry + rowH / 2.0F, nameFs, 3, Style.text);
               String tag = row.tag;
               float tagW = Gx.width(tag, tagFs, 2);
               Gx.textRight(tag, px + panelW - pad, ry + rowH / 2.0F, tagFs, 2, tagCol);
               ry += rowH;
            }
         }
      }
   }

   private static AntiVanish instance() {
      for (Module m : Modules.ALL) {
         if (m instanceof AntiVanish) {
            return (AntiVanish)m;
         }
      }

      return null;
   }

   private void parseStaffNames() {
      this.staffSet.clear();

      for (String n : this.staffNames.get().split("[,;\\s]+")) {
         String t = n.trim();
         if (!t.isEmpty()) {
            this.staffSet.add(t.toLowerCase(Locale.ROOT));
         }
      }
   }

   private boolean isStaff(String name) {
      if (name == null) {
         return false;
      } else {
         String key = name.toLowerCase(Locale.ROOT);
         return this.staffSet.contains(key) || this.starDetect.get() && this.starred.contains(key);
      }
   }

   private void trackListed() {
      ClientPacketListener nh = Mc.mc().getConnection();
      if (nh != null) {
         long now = System.currentTimeMillis();

         for (PlayerInfo info : nh.getOnlinePlayers()) {
            if (info != null && info.getProfile() != null) {
               UUID uuid = info.getProfile().id();
               if (Mc.player() == null || !Mc.player().getUUID().equals(uuid)) {
                  this.listedSinceMs.putIfAbsent(uuid, now);
                  String name = info.getProfile().name();
                  if (name != null) {
                     if (!this.knownPlayers.containsKey(uuid)) {
                        this.knownPlayers.put(uuid, new AntiVanish.KnownPlayer(uuid, name));
                     }

                     if (this.starDetect.get()) {
                        this.learnStar(name, info);
                     }

                     this.endSpell(name, false, 1000L);
                  }
               }
            }
         }

         if (this.listedSinceMs.size() > 1024) {
            Set<UUID> connected = new HashSet<>();

            for (PlayerInfo infox : nh.getOnlinePlayers()) {
               if (infox != null && infox.getProfile() != null) {
                  connected.add(infox.getProfile().id());
               }
            }

            this.listedSinceMs.keySet().retainAll(connected);
            this.knownPlayers.keySet().retainAll(connected);
         }
      }
   }

   private void learnStar(String name, PlayerInfo info) {
      try {
         Component display = info.getTabListDisplayName();
         if (display != null && hasLeadingStar(display.getString())) {
            this.starred.add(name.toLowerCase(Locale.ROOT));
         }
      } catch (Throwable var4) {
      }
   }

   private static boolean hasLeadingStar(String text) {
      if (text != null && !text.isEmpty()) {
         String clean = stripCodes(text);
         int i = 0;

         while (i < clean.length()) {
            int cp = clean.codePointAt(i);
            i += Character.charCount(cp);
            if (!Character.isWhitespace(cp) && cp != 8203 && cp != 65039) {
               return "★☆✦✧✪✯✰⭐".indexOf(cp) >= 0;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean trustedListed(UUID uuid) {
      Long since = this.listedSinceMs.get(uuid);
      return since != null && System.currentTimeMillis() - since >= 1500L;
   }

   private boolean credible(UUID uuid, String name) {
      return uuid != null && isUsername(name) && this.isStaff(name) && this.trustedListed(uuid);
   }

   private void drainObs() {
      AntiVanish.Obs o;
      for (int i = 0; i < 512 && (o = this.observations.poll()) != null; i++) {
         this.processObs(o);
      }

      while (this.observations.size() > 4096) {
         this.observations.poll();
      }
   }

   private void processObs(AntiVanish.Obs o) {
      switch (o.type) {
         case TAB_REMOVE:
            this.handleTabRemove(o.uuid);
            break;
         case TAB_HIDE:
            this.handleTabHide(o.uuid);
            break;
         case PLAYER_LEFT:
            this.serverSendsLeave = true;
            this.confirmDeparture(o.detail);
            break;
         case SYSTEM_CHAT:
            this.cacheMessage(o.detail);
            break;
         case ENTITY_META:
            this.inspectInvisible(o.entityId);
            break;
         case SOUND:
            this.inspectSound(o);
            break;
         case PARTICLE:
            this.inspectParticle(o);
            break;
         case GAMEMODE:
            this.announceGamemode(o.uuid, o.detail);
      }
   }

   private void handleTabRemove(UUID uuid) {
      if (uuid != null && !Mc.player().getUUID().equals(uuid)) {
         String name = this.knownName(uuid);
         if (this.credible(uuid, name) && isPlausibleName(name)) {
            this.pendingVanishes.removeIf(p -> p.uuid.equals(uuid));
            this.pendingVanishes.addLast(new AntiVanish.PendingVanish(uuid, name, this.tickCnt + 20));
         }
      }
   }

   private void handleTabHide(UUID uuid) {
      if (uuid != null && !Mc.player().getUUID().equals(uuid)) {
         AntiVanish.KnownPlayer kp = this.knownPlayers.get(uuid);
         String name = kp != null ? kp.name : this.knownName(uuid);
         if (this.credible(uuid, name) && isPlausibleName(name)) {
            if (!this.recentMsgMentions(name)) {
               this.reportHidden(uuid, name, "Vanish: aus TAB versteckt", 100);
            }
         }
      }
   }

   private String knownName(UUID uuid) {
      AntiVanish.KnownPlayer kp = this.knownPlayers.get(uuid);
      if (kp != null && kp.name != null) {
         return kp.name;
      } else {
         ClientPacketListener nh = Mc.mc().getConnection();
         if (nh != null) {
            PlayerInfo info = nh.getPlayerInfo(uuid);
            if (info != null && info.getProfile() != null) {
               return info.getProfile().name();
            }
         }

         return null;
      }
   }

   private UUID knownUuid(String name) {
      for (AntiVanish.KnownPlayer kp : this.knownPlayers.values()) {
         if (kp.name.equalsIgnoreCase(name)) {
            return kp.uuid;
         }
      }

      return null;
   }

   private void confirmDeparture(String text) {
      for (AntiVanish.KnownPlayer kp : this.knownPlayers.values()) {
         if (containsName(text, kp.name)) {
            this.pendingVanishes.removeIf(p -> p.uuid.equals(kp.uuid));
            this.hiddenSpells.remove(kp.name.toLowerCase(Locale.ROOT));
            this.detections.remove(kp.uuid.toString());
         }
      }
   }

   private void processPendingVanishes() {
      ClientPacketListener nh = Mc.mc().getConnection();
      if (nh != null) {
         while (!this.pendingVanishes.isEmpty() && this.pendingVanishes.peekFirst().dueTick <= this.tickCnt) {
            AntiVanish.PendingVanish pv = this.pendingVanishes.removeFirst();
            if (nh.getPlayerInfo(pv.uuid) == null) {
               if (this.recentMsgMentions(pv.name)) {
                  this.serverSendsLeave = true;
               } else {
                  Player remaining = Mc.world().getPlayerInAnyDimension(pv.uuid);
                  if (remaining != null && remaining.isPassenger()) {
                     this.reportHidden(pv.uuid, pv.name, "Vanish: Entity noch da", 100);
                  } else {
                     int score = this.serverSendsLeave ? 100 : 30;
                     this.reportHidden(pv.uuid, pv.name, "Vanish: still aus TAB entfernt", score);
                  }
               }
            }
         }
      }
   }

   private void sendCompletionProbe() {
      ClientPacketListener nh = Mc.mc().getConnection();
      if (nh != null) {
         int id = this.nextCompId++;
         if (this.nextCompId > 40000) {
            this.nextCompId = 30000;
         }

         this.completionIds.add(id);

         while (this.completionIds.size() > 8) {
            this.completionIds.remove(this.completionIds.iterator().next());
         }

         try {
            nh.getConnection().send(new ServerboundCommandSuggestionPacket(id, "minecraft:msg "));
         } catch (Throwable var4) {
         }
      }
   }

   private void processCompletionProbe() {
      List<String> names = this.pendingCompNames;
      if (names != null) {
         this.pendingCompNames = null;
         ClientPacketListener nh = Mc.mc().getConnection();
         if (nh != null) {
            Set<String> tabNames = new HashSet<>();

            for (PlayerInfo info : nh.getOnlinePlayers()) {
               if (info != null && info.getProfile() != null && info.getProfile().name() != null) {
                  tabNames.add(info.getProfile().name().toLowerCase(Locale.ROOT));
               }
            }

            String self = Mc.player().getName().getString();
            List<String> hidden = new ArrayList<>();

            for (String n : names) {
               if (this.isStaff(n)
                  && isPlausibleName(n)
                  && !n.equalsIgnoreCase(self)
                  && !tabNames.contains(n.toLowerCase(Locale.ROOT))
                  && !this.recentMsgMentions(n)) {
                  hidden.add(n);
               }
            }

            if (!hidden.isEmpty() && hidden.size() <= 3) {
               for (String nx : hidden) {
                  this.reportHidden(this.knownUuid(nx), nx, "Vanish: versteckt aber ansprechbar", 90);
               }
            }
         }
      }
   }

   private void scanInvisible() {
      double rSq = 4096.0;
      Iterator var3 = Mc.players().iterator();

      while (true) {
         Player p;
         while (true) {
            if (!var3.hasNext()) {
               return;
            }

            p = (Player)var3.next();
            if (p != null && p != Mc.player()) {
               try {
                  if (!p.isInvisible()) {
                     continue;
                  }
                  break;
               } catch (Throwable var6) {
               }
            }
         }

         if (!(p.distanceTo(Mc.player()) > rSq)) {
            String name = profileName(p);
            if (this.credible(p.getUUID(), name)) {
               this.addSignal(AntiVanish.SignalType.INVISIBLE, name, "Unsichtbare Entity", 35, 5000L);
            }
         }
      }
   }

   private void inspectInvisible(int entityId) {
      if (Mc.world().getEntity(entityId) instanceof Player p && p != Mc.player()) {
         try {
            if (!p.isInvisible()) {
               return;
            }
         } catch (Throwable var5) {
            return;
         }

         if (!(p.distanceTo(Mc.player()) > 4096.0)) {
            String name = profileName(p);
            if (this.credible(p.getUUID(), name)) {
               this.addSignal(AntiVanish.SignalType.INVISIBLE, name, "Unsichtbare Entity: Metadata", 35, 5000L);
            }
         }
      }
   }

   private void inspectSound(AntiVanish.Obs o) {
      Vec3 src = new Vec3(o.x, o.y, o.z);
      if (!(src.distanceTo(pos(Mc.player())) > 4096.0)) {
         if (!this.hasVisibleCause(src)) {
            String dir = this.locateDir(src);
            this.addSignal(AntiVanish.SignalType.SOUND, dir, "Sound: " + shortId(o.detail), 14, 3000L);
         }
      }
   }

   private void inspectParticle(AntiVanish.Obs o) {
      Vec3 src = new Vec3(o.x, o.y, o.z);
      if (!(src.distanceTo(pos(Mc.player())) > 4096.0)) {
         if (!this.hasVisibleCause(src)) {
            this.addSignal(AntiVanish.SignalType.PARTICLE, this.locateDir(src), "Partikel: " + shortId(o.detail), 16, 3000L);
         }
      }
   }

   private void announceGamemode(UUID uuid, String mode) {
      if (uuid != null && (Mc.player() == null || !Mc.player().getUUID().equals(uuid))) {
         AntiVanish.KnownPlayer kp = this.knownPlayers.get(uuid);
         if (kp != null && this.isStaff(kp.name)) {
            if ("spectator".equals(mode) && this.credible(uuid, kp.name)) {
               this.reportHidden(uuid, kp.name, "Vanish: Zuschauermodus", 100);
            } else if (this.chatNotify.get()) {
               this.chatInfo(kp.name + " wechselte zu " + mode);
            }
         }
      }
   }

   private void reportHidden(UUID uuid, String name, String reason, int score) {
      String key = uuid != null ? uuid.toString() : name;
      this.upsertDetection(key, name, reason, score, System.currentTimeMillis() + 20000L);
      AntiVanish.FaceSkin.preload(name);
      String spellKey = name.toLowerCase(Locale.ROOT);
      AntiVanish.HiddenSpell spell = this.hiddenSpells.get(spellKey);
      if (spell == null) {
         this.hiddenSpells.put(spellKey, new AntiVanish.HiddenSpell(uuid, name, reason, this.tickCnt));
      }
   }

   private void reviewSpells() {
      for (AntiVanish.HiddenSpell sp : this.hiddenSpells.values()) {
         if (!sp.told && sp.sinceTick < this.tickCnt) {
            sp.told = true;
            this.chatInfo(sp.name + " ist gevanished (" + sp.reason + ")");
         }
      }
   }

   private boolean endSpell(String name, boolean spectator, long settleMs) {
      String key = name.toLowerCase(Locale.ROOT);
      AntiVanish.HiddenSpell sp = this.hiddenSpells.get(key);
      if (sp != null && System.currentTimeMillis() - sp.sinceMs >= settleMs) {
         this.hiddenSpells.remove(key);
         if (sp.uuid != null) {
            this.detections.remove(sp.uuid.toString());
         }

         if (sp.told && this.chatNotify.get()) {
            this.chatInfo(sp.name + " ist wieder sichtbar");
         }

         return true;
      } else {
         return false;
      }
   }

   private void addSignal(AntiVanish.SignalType type, String subject, String reason, int weight, long cooldownMs) {
      long now = System.currentTimeMillis();
      String coolKey = type.name() + "|" + subject.toLowerCase(Locale.ROOT);
      Long last = this.signalCooldowns.get(coolKey);
      if (last == null || now - last >= cooldownMs) {
         this.signalCooldowns.put(coolKey, now);
         this.signals.addLast(new AntiVanish.Signal(type, subject, reason, weight, now));
         this.upsertDetection("signal:" + coolKey, subject, reason, weight, now + 20000L);
         this.announceSignal(coolKey, subject, reason);
      }
   }

   private void announceSignal(String key, String subject, String reason) {
      long now = System.currentTimeMillis();
      Long last = this.announceCooldowns.get(key);
      if (last == null || now - last >= 1500L) {
         while (!this.announceTimes.isEmpty() && now - this.announceTimes.peekFirst() > 4000L) {
            this.announceTimes.removeFirst();
         }

         if (this.announceTimes.size() < 6) {
            this.announceCooldowns.put(key, now);
            this.announceTimes.addLast(now);
            if (this.chatNotify.get()) {
               this.chatInfo(reason + " (" + subject + ")");
            }
         }
      }
   }

   private void upsertDetection(String key, String name, String reason, int score, long expires) {
      AntiVanish.Detection d = this.detections.get(key);
      if (d == null) {
         this.detections.put(key, new AntiVanish.Detection(name, reason, score, expires));
      } else {
         d.name = name;
         d.expiresAt = Math.max(d.expiresAt, expires);
         if (score >= d.score) {
            d.reason = reason;
            d.score = score;
         }
      }
   }

   private void cacheMessage(String text) {
      if (text != null && !text.isBlank()) {
         long now = System.currentTimeMillis();
         this.recentMessages.addLast(new AntiVanish.RecentMsg(text, now));

         while (!this.recentMessages.isEmpty() && (now - this.recentMessages.peekFirst().at > 8000L || this.recentMessages.size() > 64)) {
            this.recentMessages.removeFirst();
         }
      }
   }

   private boolean recentMsgMentions(String name) {
      if (name == null) {
         return false;
      } else {
         long now = System.currentTimeMillis();

         for (AntiVanish.RecentMsg rm : this.recentMessages) {
            if (now - rm.at <= 8000L && looksLikeLeave(rm.text, name)) {
               return true;
            }
         }

         return false;
      }
   }

   private void pruneState() {
      long now = System.currentTimeMillis();
      this.detections.entrySet().removeIf(e -> e.getValue().expiresAt <= now);
      this.signalCooldowns.entrySet().removeIf(e -> now - e.getValue() > 60000L);
      this.announceCooldowns.entrySet().removeIf(e -> now - e.getValue() > 60000L);

      while (!this.signals.isEmpty() && now - this.signals.peekFirst().timeMs > 15000L) {
         this.signals.removeFirst();
      }
   }

   private List<AntiVanish.HudRow> hudRows(long now) {
      List<AntiVanish.HudRow> out = new ArrayList<>();
      if (this.testMode.get()) {
         out.add(new AntiVanish.HudRow(null, "TestAdmin", "CRITICAL"));
         out.add(new AntiVanish.HudRow(null, "StaffMember", "VANISH"));
         out.add(new AntiVanish.HudRow(null, "HiddenMod", "INVIS"));
         return out;
      } else {
         if (now < this.criticalUntilMs) {
            out.add(new AntiVanish.HudRow(null, this.criticalName, "CRITICAL"));
         }

         List<AntiVanish.Detection> sorted = new ArrayList<>();

         for (AntiVanish.Detection d : this.detections.values()) {
            if (d.expiresAt > now && worthShowing(d)) {
               sorted.add(d);
            }
         }

         sorted.sort(Comparator.<AntiVanish.Detection>comparingInt(dx -> dx.score).reversed());
         int added = 0;

         for (AntiVanish.Detection dx : sorted) {
            if (added >= 5) {
               break;
            }

            String tag = hudTag(dx.reason);
            out.add(new AntiVanish.HudRow(this.knownUuid(dx.name), dx.name, tag));
            added++;
         }

         return out;
      }
   }

   private static boolean worthShowing(AntiVanish.Detection d) {
      return d.name != null && !d.name.isBlank() && !"Unknown".equalsIgnoreCase(d.name);
   }

   private static String hudTag(String reason) {
      if (reason == null) {
         return "WATCH";
      } else {
         String l = reason.toLowerCase(Locale.ROOT);
         if (l.startsWith("vanish")) {
            return "VANISH";
         } else if (l.contains("unsichtbar")) {
            return "INVIS";
         } else if (l.startsWith("sound")) {
            return "SOUND";
         } else {
            return l.startsWith("partikel") ? "PARTICLE" : "WATCH";
         }
      }
   }

   private boolean hasVisibleCause(Vec3 src) {
      for (Entity e : Mc.entities()) {
         if (e != Mc.player() && e instanceof Player p && !p.isInvisible() && pos(e).distanceTo(src) <= 16.0) {
            return true;
         }
      }

      return false;
   }

   private String locateDir(Vec3 src) {
      Vec3 me = pos(Mc.player());
      double dx = src.x - me.x;
      double dz = src.z - me.z;
      long dist = Math.round(Math.sqrt(dx * dx + dz * dz));
      String ns = dz < -1.0 ? "N" : (dz > 1.0 ? "S" : "");
      String ew = dx > 1.0 ? "E" : (dx < -1.0 ? "W" : "");
      return dist + "m " + ns + ew;
   }

   private static Vec3 pos(Entity e) {
      return new Vec3(Mc.x(e), Mc.y(e), Mc.z(e));
   }

   private static String profileName(Player p) {
      return p.getGameProfile() != null ? p.getGameProfile().name() : p.getName().getString();
   }

   private void chatInfo(String msg) {
      if (this.chatNotify.get()) {
         Mc.chat("§7[§cAnti Vanish§7] §f" + msg);
      }
   }

   private void resetAll() {
      this.observations.clear();
      this.knownPlayers.clear();
      this.detections.clear();
      this.signalCooldowns.clear();
      this.announceCooldowns.clear();
      this.announceTimes.clear();
      this.signals.clear();
      this.pendingVanishes.clear();
      this.hiddenSpells.clear();
      this.recentMessages.clear();
      this.listedSinceMs.clear();
      this.completionIds.clear();
      this.starred.clear();
      this.pendingCompNames = null;
      this.serverSendsLeave = false;
      this.nextCompId = 30000;
      this.tickCnt = 0;
      this.lastCriticalMs = 0L;
      this.criticalUntilMs = 0L;
      this.criticalName = "";
   }

   private static boolean suspiciousSound(String id) {
      String p = shortId(id);
      return p.contains("chest.open")
         || p.contains("chest.close")
         || p.contains("barrel.open")
         || p.contains("barrel.close")
         || p.contains("shulker_box.open")
         || p.contains("shulker_box.close")
         || p.contains("door.open")
         || p.contains("door.close")
         || p.contains("fence_gate.open")
         || p.contains("fence_gate.close")
         || p.contains("button.click")
         || p.contains("lever.click");
   }

   private static boolean suspiciousParticle(String id) {
      String p = shortId(id);
      return p.contains("crit") || p.contains("enchanted_hit") || p.contains("damage_indicator") || p.contains("smoke") || p.equals("block");
   }

   private static String shortId(String id) {
      if (id == null) {
         return "unknown";
      } else {
         int i = id.indexOf(58);
         return (i >= 0 && i + 1 < id.length() ? id.substring(i + 1) : id).toLowerCase(Locale.ROOT);
      }
   }

   static boolean isUsername(String name) {
      if (name != null && name.length() >= 2 && name.length() <= 32) {
         boolean hasAlnum = false;

         for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if ((c < 'A' || c > 'Z') && (c < 'a' || c > 'z') && (c < '0' || c > '9')) {
               if (c != '_' && c != '.') {
                  return false;
               }
            } else {
               hasAlnum = true;
            }
         }

         return hasAlnum;
      } else {
         return false;
      }
   }

   static boolean isPlausibleName(String name) {
      if (name == null) {
         return false;
      } else {
         String t = name.trim();
         return t.length() >= 3 && t.length() <= 16 && t.indexOf(32) < 0;
      }
   }

   static boolean containsName(String text, String name) {
      return text != null && name != null ? text.toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT)) : false;
   }

   static boolean looksLikeLeave(String message, String name) {
      if (!containsName(message, name)) {
         return false;
      } else {
         String l = message.toLowerCase(Locale.ROOT);
         return l.contains("left the game") || l.contains("left") || l.contains("quit") || l.contains("disconnected");
      }
   }

   static String stripCodes(String text) {
      if (text == null) {
         return "";
      } else {
         StringBuilder out = new StringBuilder(text.length());

         for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == 167 && i + 1 < text.length()) {
               i++;
            } else {
               out.append(c);
            }
         }

         return out.toString();
      }
   }

   private static final class Detection {
      String name;
      String reason;
      int score;
      long expiresAt;

      Detection(String n, String r, int s, long e) {
         this.name = n;
         this.reason = r;
         this.score = s;
         this.expiresAt = e;
      }
   }

   public static final class FaceSkin {
      private static final HttpClient HTTP = HttpClient.newHttpClient();
      private static final Map<String, int[]> FACE_PIXELS = new ConcurrentHashMap<>();
      private static final Set<String> LOADING = ConcurrentHashMap.newKeySet();
      private static final int FACE_SIZE = 8;

      public static Gx.Img getFace(String name) {
         if (name != null && !name.isBlank()) {
            String key = "av_face_" + name.trim().toLowerCase(Locale.ROOT);
            Gx.Img cached = Gx.peek(key);
            if (cached != null) {
               return cached;
            } else {
               int[] px = FACE_PIXELS.get(name.trim().toLowerCase(Locale.ROOT));
               if (px == null) {
                  preload(name);
                  return null;
               } else {
                  return Gx.get(key, 8, () -> new int[][]{px, {8, 8}});
               }
            }
         } else {
            return null;
         }
      }

      public static void preload(String name) {
         if (name != null && !name.isBlank()) {
            String key = name.trim().toLowerCase(Locale.ROOT);
            if (!FACE_PIXELS.containsKey(key) && LOADING.add(key)) {
               HttpRequest req = HttpRequest.newBuilder(
                     URI.create("https://minotar.net/helm/" + URLEncoder.encode(name.trim(), StandardCharsets.UTF_8) + "/8.png")
                  )
                  .header("User-Agent", "Mozilla/5.0")
                  .build();
               HTTP.sendAsync(req, BodyHandlers.ofByteArray()).thenAccept(resp -> {
                  if (resp.statusCode() != 200) {
                     LOADING.remove(key);
                  } else {
                     try {
                        BufferedImage img = ImageIO.read(new ByteArrayInputStream(resp.body()));
                        if (img == null) {
                           LOADING.remove(key);
                           return;
                        }

                        int w = img.getWidth();
                        int h = img.getHeight();
                        int[] pixels = new int[64];

                        for (int y = 0; y < 8; y++) {
                           for (int x = 0; x < 8; x++) {
                              int sx = x * w / 8;
                              int sy = y * h / 8;
                              pixels[y * 8 + x] = img.getRGB(sx, sy);
                           }
                        }

                        FACE_PIXELS.put(key, pixels);
                     } catch (Throwable var10) {
                     }

                     LOADING.remove(key);
                  }
               }).exceptionally(e -> {
                  LOADING.remove(key);
                  return null;
               });
            }
         }
      }
   }

   private static final class HiddenSpell {
      final UUID uuid;
      final String name;
      final String reason;
      final int sinceTick;
      final long sinceMs = System.currentTimeMillis();
      boolean told;

      HiddenSpell(UUID u, String n, String r, int t) {
         this.uuid = u;
         this.name = n;
         this.reason = r;
         this.sinceTick = t;
      }
   }

   private record HudRow(UUID uuid, String name, String tag) {
   }

   private record KnownPlayer(UUID uuid, String name) {
   }

   private record Obs(AntiVanish.ObsType type, UUID uuid, int entityId, double x, double y, double z, String detail) {
      static AntiVanish.Obs tabRemove(UUID id) {
         return new AntiVanish.Obs(AntiVanish.ObsType.TAB_REMOVE, id, -1, 0.0, 0.0, 0.0, "");
      }

      static AntiVanish.Obs tabHide(UUID id) {
         return new AntiVanish.Obs(AntiVanish.ObsType.TAB_HIDE, id, -1, 0.0, 0.0, 0.0, "");
      }

      static AntiVanish.Obs systemChat(String t) {
         return new AntiVanish.Obs(AntiVanish.ObsType.SYSTEM_CHAT, null, -1, 0.0, 0.0, 0.0, t);
      }

      static AntiVanish.Obs gamemode(UUID id, String m) {
         return new AntiVanish.Obs(AntiVanish.ObsType.GAMEMODE, id, -1, 0.0, 0.0, 0.0, m);
      }

      static AntiVanish.Obs entityMeta(int id) {
         return new AntiVanish.Obs(AntiVanish.ObsType.ENTITY_META, null, id, 0.0, 0.0, 0.0, "");
      }

      static AntiVanish.Obs position(AntiVanish.ObsType t, double x, double y, double z, String d) {
         return new AntiVanish.Obs(t, null, -1, x, y, z, d);
      }
   }

   private static enum ObsType {
      TAB_REMOVE,
      TAB_HIDE,
      PLAYER_LEFT,
      SYSTEM_CHAT,
      ENTITY_META,
      SOUND,
      PARTICLE,
      GAMEMODE;
   }

   private record PendingVanish(UUID uuid, String name, int dueTick) {
   }

   private record RecentMsg(String text, long at) {
   }

   private record Signal(AntiVanish.SignalType type, String subject, String reason, int weight, long timeMs) {
   }

   private static enum SignalType {
      VANISH,
      INVISIBLE,
      SOUND,
      PARTICLE;
   }
}
