package dev.lego.games;

import dev.lego.core.Mc;
import dev.lego.net.LegoSync;
import dev.lego.ui.LegoScreen;
import dev.lego.ui.Sound;
import dev.lego.ui.Toasts;
import dev.lego.ui.View;
import dev.spotifyhud.Json;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.gui.screens.Screen;

public final class Duels {
   public static final Map<String, String> GAMES = new LinkedHashMap<>();
   public static final Map<String, Duels.Invite> INCOMING = new ConcurrentHashMap<>();
   private static final Map<String, Object[]> OUTGOING = new ConcurrentHashMap<>();
   public static volatile Duels.Duel current;
   private static final long INVITE_S = 90L;

   private Duels() {
   }

   private static String myId() {
      return Mc.player() == null ? "" : Mc.player().getUUID().toString();
   }

   private static Map<String, Object> base(String kind, UUID to, String game, String match) {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("id", myId());
      m.put("name", Mc.player() == null ? "" : Mc.name(Mc.player()));
      m.put("to", to.toString());
      m.put("k", kind);
      m.put("g", game);
      m.put("m", match);
      return m;
   }

   private static void post(Map<String, Object> m) {
      try {
         LegoSync.HTTP_POST("https://ntfy.sh/legoclient-duel-v1-q7x4m2", Json.write(m));
      } catch (Throwable var2) {
      }
   }

   public static boolean invited(UUID to) {
      for (Object[] o : OUTGOING.values()) {
         if (o[0].equals(to) && System.currentTimeMillis() / 1000L - (Long)o[2] < 90L) {
            return true;
         }
      }

      return false;
   }

   public static void invite(UUID to, String game) {
      if (GAMES.containsKey(game)) {
         String match = Long.toString(System.nanoTime() ^ (long)(Math.random() * 9.223372E18F), 36).replace("-", "");
         if (match.length() > 16) {
            match = match.substring(0, 16);
         }

         OUTGOING.put(match, new Object[]{to, game, System.currentTimeMillis() / 1000L});
         post(base("inv", to, game, match));
         String n = LegoSync.name(to);
         Toasts.show("gamepad", "Duell", "Einladung zu " + GAMES.get(game) + " an " + (n == null ? "Spieler" : n) + " gesendet", -12604929, 3000L);
      }
   }

   public static void accept(Duels.Invite inv) {
      INCOMING.remove(inv.match());
      post(base("acc", inv.from(), inv.game(), inv.match()));
      start(new Duels.Duel(inv.match(), inv.game(), inv.from(), inv.name(), false));
   }

   public static void decline(Duels.Invite inv) {
      INCOMING.remove(inv.match());
      post(base("dec", inv.from(), inv.game(), inv.match()));
   }

   public static List<Duels.Invite> invites() {
      long now = System.currentTimeMillis() / 1000L;
      INCOMING.values().removeIf(i -> now - i.at() > 90L);
      return new ArrayList<>(INCOMING.values());
   }

   private static void start(Duels.Duel d) {
      Duels.Duel old = current;
      if (old != null && !old.match.equals(d.match)) {
         old.leave();
      }

      current = d;
      String var3 = d.game;

      View v = (View)(switch (var3) {
         case "tictactoe" -> new TicTacToe(d);
         case "rps" -> new RPS(d);
         case "reversi" -> new Reversi(d);
         case "nim" -> new Nim(d);
         default -> new ConnectFour(d);
      });
      Mc.mc().execute(() -> {
         Screen s = Mc.mc().screen;
         if (s != null && !(s instanceof LegoScreen)) {
            Toasts.show("gamepad", "Duell", "Duell gegen " + d.oppName + " startet – schließe das Menü", -14756000, 4000L);
         } else {
            LegoScreen.show(v);
         }
      });
   }

   private static boolean safe(String s, int max) {
      return s != null && !s.isEmpty() && s.length() <= max && s.matches("[A-Za-z0-9_]+");
   }

   public static void receive(Object msg, long time, String myId) {
      if (!myId.isEmpty() && myId.equals(Json.str(msg, "to", ""))) {
         if (System.currentTimeMillis() / 1000L - time <= 90L) {
            UUID from;
            try {
               from = UUID.fromString(Json.str(msg, "id", ""));
            } catch (IllegalArgumentException var14) {
               return;
            }

            String kind = Json.str(msg, "k", "");
            String game = Json.str(msg, "g", "");
            String match = Json.str(msg, "m", "");
            String name = Json.str(msg, "name", "");
            if (safe(match, 16) && GAMES.containsKey(game)) {
               if (!name.matches("[A-Za-z0-9_]{1,16}")) {
                  name = "Lego-Spieler";
               }

               switch (kind) {
                  case "inv":
                     if (INCOMING.size() > 20) {
                        return;
                     }

                     INCOMING.put(match, new Duels.Invite(match, from, name, game, time));
                     Toasts.show("gamepad", "Duell-Einladung", name + " fordert dich zu " + GAMES.get(game) + " heraus · Spiele → Duelle", -15043, 6000L);
                     Sound.play("minecraft:block.note_block.bell", 1.2F, 0.5F);
                     break;
                  case "acc":
                     Object[] ox = OUTGOING.remove(match);
                     if (ox == null || !ox[0].equals(from)) {
                        return;
                     }

                     Toasts.show("gamepad", "Duell", name + " hat angenommen!", -14756000, 2500L);
                     start(new Duels.Duel(match, game, from, name, true));
                     break;
                  case "dec":
                     Object[] o = OUTGOING.remove(match);
                     if (o != null && o[0].equals(from)) {
                        Toasts.show("gamepad", "Duell", name + " hat abgelehnt", -45730, 2500L);
                     }
                     break;
                  case "mv":
                     Duels.Duel dx = current;
                     if (dx == null || !dx.match.equals(match) || !dx.opp.equals(from)) {
                        return;
                     }

                     int r = (int)Json.num(msg, "r", -1L);
                     int v = (int)Json.num(msg, "v", -1L);
                     if (r < 0 || r > 10000 || v < 0 || v > 64 || dx.moves.size() > 64) {
                        return;
                     }

                     dx.moves.add(new int[]{r, v});
                     break;
                  case "bye":
                     INCOMING.remove(match);
                     Duels.Duel d = current;
                     if (d != null && d.match.equals(match) && d.opp.equals(from)) {
                        d.oppLeft = true;
                     }
               }
            }
         }
      }
   }

   static {
      GAMES.put("connect4", "Vier gewinnt");
      GAMES.put("tictactoe", "Tic-Tac-Toe");
      GAMES.put("rps", "Schere Stein Papier");
      GAMES.put("reversi", "Reversi");
      GAMES.put("nim", "Nim");
   }

   public static final class Duel {
      public final String match;
      public final String game;
      public final String oppName;
      public final UUID opp;
      public final boolean iStart;
      final ConcurrentLinkedQueue<int[]> moves = new ConcurrentLinkedQueue<>();
      public volatile boolean oppLeft;
      private boolean left;

      Duel(String match, String game, UUID opp, String oppName, boolean iStart) {
         this.match = match;
         this.game = game;
         this.opp = opp;
         this.oppName = oppName;
         this.iStart = iStart;
      }

      public Integer take(int round) {
         for (int[] m : this.moves) {
            if (m[0] < round) {
               this.moves.remove(m);
            } else if (m[0] == round) {
               this.moves.remove(m);
               return m[1];
            }
         }

         return null;
      }

      public void send(int round, int move) {
         Map<String, Object> m = Duels.base("mv", this.opp, this.game, this.match);
         m.put("r", (double)round);
         m.put("v", (double)move);
         Duels.post(m);
      }

      public void leave() {
         if (!this.left) {
            this.left = true;
            if (Duels.current == this) {
               Duels.current = null;
            }

            Duels.post(Duels.base("bye", this.opp, this.game, this.match));
         }
      }
   }

   public record Invite(String match, UUID from, String name, String game, long at) {
   }
}
