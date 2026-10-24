package dev.lego.visual;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.net.LegoSync;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Tx;
import dev.spotifyhud.Json;
import dev.spotifyhud.PlayerState;
import dev.spotifyhud.SpotifyHudMod;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.RoundRectangle2D.Double;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class NameFx {
   public static NameFx.Tag tag;

   private NameFx() {
   }

   public static void registerAll() {
      tag = Modules.register(new NameFx.Tag());
   }

   public static void render(float td) {
      if (tag != null && tag.enabled) {
         tag.renderBrick(td);
         tag.renderCard(td);
      }

      if (tag != null && LegoSync.active()) {
         tag.renderOthers(td);
      }
   }

   public static void applyOther(AvatarRenderState state, Object player) {
      try {
         if (!(player instanceof Entity e)) {
            return;
         }

         Map<String, Object> r = LegoSync.remote(e.getUUID());
         if (r == null || state.nameTag == null) {
            return;
         }

         String text = Json.str(r, "t", "");
         String s = text.isBlank() ? Mc.name(e) : text;
         state.nameTag = build(
            s,
            (int)Json.num(r, "s", 0L),
            (int)Json.num(r, "c1", 5L),
            (int)Json.num(r, "c2", 7L),
            Json.bool(r, "b", true),
            Json.bool(r, "m", true),
            Json.num(r, "sp", 1L)
         );
      } catch (Throwable var6) {
      }
   }

   public static void apply(AvatarRenderState state) {
      if (tag != null && tag.enabled) {
         LocalPlayer me = Mc.player();
         if (me != null && !Mc.firstPerson() && !me.isInvisible()) {
            state.nameTag = tag.component(me);
            if (state.nameTagAttachment == null) {
               state.nameTagAttachment = new Vec3(0.0, Mc.height(me) + 0.5, 0.0);
            }
         }
      }
   }

   private static int pal(int idx, double phase) {
      int[] p = Setting.Color.PALETTE;
      int i = Math.max(0, Math.min(p.length - 1, idx));
      if (i == p.length - 1) {
         double t = System.currentTimeMillis() / 2600.0 + phase;
         return Color.HSBtoRGB((float)(t - Math.floor(t)), 0.75F, 1.0F) & 16777215;
      } else {
         return p[i] & 16777215;
      }
   }

   static int colorAt(int style, int c1i, int c2i, int i, double t) {
      int c1 = pal(c1i, i * 0.07);
      int c2 = pal(c2i, i * 0.07 + 0.3);
      switch (style) {
         case 0:
            return Color.HSBtoRGB((float)((t * 0.25 + i * 0.06) % 1.0), 0.7F, 1.0F) & 16777215;
         case 1:
            return lerp(c1, c2, 0.5 + 0.5 * Math.sin(t * 2.0 + i * 0.4));
         case 2:
            return lerp(16726814, 16765499, 0.5 + 0.5 * Math.sin(t * 7.0 + i * 1.7));
         case 3:
            return lerp(8189951, 16777215, 0.5 + 0.5 * Math.sin(t * 2.5 + i * 0.5));
         case 4:
            return lerp(3158064, c1, 0.55 + 0.45 * Math.sin(t * 3.0));
         case 5:
            return (int)(t * 8.0 + i * 7) % 9 == 0 ? 16777215 : c1;
         case 6:
            return lerp(c1, c2, 0.5 + 0.5 * Math.sin(t * 1.5 - i * 0.35));
         default:
            return lerp(756282, 3932028, 0.5 + 0.5 * Math.sin(t * 9.0 + i * 2.3));
      }
   }

   static Component build(String s, int style, int c1, int c2, boolean boldAll, boolean moving, double speed) {
      if (s.length() > 24) {
         s = s.substring(0, 24);
      }

      double t = System.currentTimeMillis() / 1000.0 * speed;
      int n = s.length();
      MutableComponent root = Component.literal("");
      double pos = t * 5.0 % (n + 4) - 2.0;
      int glitch = (int)((long)(t * 10.0) % Math.max(1, n));

      for (int i = 0; i < n; i++) {
         int rgb = colorAt(style, c1, c2, i, t);
         boolean bold = boldAll || moving && Math.abs(i - pos) < 1.0;
         boolean obf = moving && style == 5 && i == glitch && s.charAt(i) != ' ';
         root.append(Component.literal(String.valueOf(s.charAt(i))).withStyle(st -> {
            Style r = st.withColor(rgb);
            if (bold) {
               r = r.withBold(Boolean.TRUE);
            }

            if (obf) {
               r = r.withObfuscated(Boolean.TRUE);
            }

            return r;
         }));
      }

      return root;
   }

   private static int lerp(int a, int b, double t) {
      t = Math.max(0.0, Math.min(1.0, t));
      int r = (int)((a >> 16 & 0xFF) * (1.0 - t) + (b >> 16 & 0xFF) * t);
      int g = (int)((a >> 8 & 0xFF) * (1.0 - t) + (b >> 8 & 0xFF) * t);
      int bl = (int)((a & 0xFF) * (1.0 - t) + (b & 0xFF) * t);
      return r << 16 | g << 8 | bl;
   }

   public static final class Tag extends Module {
      final Setting.Text text = this.add(new Setting.Text("Text (leer = dein Name)", "", 24, "Name"));
      final Setting.Mode style = this.add(new Setting.Mode("Animation", 0, "Regenbogen", "Welle", "Feuer", "Eis", "Neon-Puls", "Glitzer", "Verlauf", "Matrix"));
      final Setting.Bool moving = this.add(new Setting.Bool("Buchstaben bewegen sich", true));
      final Setting.Num speed = this.add(new Setting.Num("Tempo", 0.3, 3.0, 0.1, 1.0, "x"));
      final Setting.Color color = this.add(new Setting.Color("Farbe", 5));
      final Setting.Color color2 = this.add(new Setting.Color("Farbe 2", 7));
      final Setting.Bool bold = this.add(new Setting.Bool("Fetter Name", true));
      final Setting.Bool badge = this.add(new Setting.Bool("LEGO-Symbol", true));
      final Setting.Bool song = this.add(new Setting.Bool("Spotify-Karte über dem Namen", true));
      final Setting.Num cardSize = this.add(new Setting.Num("Karte: Größe", 0.5, 2.0, 0.1, 1.0, "x"));
      final Setting.Num cardLift = this.add(new Setting.Num("Karte: Höhe", -0.5, 1.5, 0.05, 0.0, " Blöcke"));
      final Setting.Num cardSide = this.add(new Setting.Num("Karte: seitlich", -1.5, 1.5, 0.05, 0.0, " Blöcke"));
      final Setting.Num cardAlpha = this.add(new Setting.Num("Karte: Deckkraft", 20.0, 100.0, 5.0, 100.0, "%"));
      final Setting.Color cardColor = this.add(new Setting.Color("Karte: Farbe", 0));
      final Setting.Num scale = this.add(new Setting.Num("Größe", 0.5, 3.0, 0.1, 1.0, "x"));
      final Setting.Bool outline = this.add(new Setting.Bool("Textumrandung", false));
      final Setting.Bool shadow = this.add(new Setting.Bool("Textschatten", true));
      final Setting.Bool background = this.add(new Setting.Bool("Hintergrund", true));
      final Setting.Num bgOpacity = this.add(new Setting.Num("Hintergrund-Deckkraft", 0.0, 100.0, 5.0, 40.0, "%"));
      private Identifier brickTex;
      private static final ExecutorService PAINT = Executors.newSingleThreadExecutor(r -> {
         Thread th = new Thread(r, "Lego-NameCard");
         th.setDaemon(true);
         th.setPriority(4);
         return th;
      });
      private static final int CW = 640;
      private static final int CH = 176;
      private Identifier cardTex;
      private int cardN = 0;
      private volatile int[] ready;
      private volatile boolean busy;
      private String paintedKey = "";
      private String requestedKey = "";
      private long fadeStart = 0L;
      private boolean wasShowing;

      Tag() {
         super("namefx", "Namensschild", "Dein Name-Tag animiert (nur für dich sichtbar) + Spotify-Karte darüber", Category.VISUALS);
         this.icon("tag");
         this.fresh();
      }

      private int colorAt(int i, double t) {
         int c1 = this.color.argb(i * 0.07) & 16777215;
         int c2 = this.color2.argb(i * 0.07 + 0.3) & 16777215;
         switch (this.style.index) {
            case 0:
               return Color.HSBtoRGB((float)((t * 0.25 + i * 0.06) % 1.0), 0.7F, 1.0F) & 16777215;
            case 1:
               return NameFx.lerp(c1, c2, 0.5 + 0.5 * Math.sin(t * 2.0 + i * 0.4));
            case 2:
               return NameFx.lerp(16726814, 16765499, 0.5 + 0.5 * Math.sin(t * 7.0 + i * 1.7));
            case 3:
               return NameFx.lerp(8189951, 16777215, 0.5 + 0.5 * Math.sin(t * 2.5 + i * 0.5));
            case 4:
               return NameFx.lerp(3158064, c1, 0.55 + 0.45 * Math.sin(t * 3.0));
            case 5:
               return (int)(t * 8.0 + i * 7) % 9 == 0 ? 16777215 : c1;
            case 6:
               return NameFx.lerp(c1, c2, 0.5 + 0.5 * Math.sin(t * 1.5 - i * 0.35));
            default:
               return NameFx.lerp(756282, 3932028, 0.5 + 0.5 * Math.sin(t * 9.0 + i * 2.3));
         }
      }

      Component component(LocalPlayer me) {
         String s = this.text.get() != null && !this.text.get().isBlank() ? this.text.get() : Mc.name(me);
         return NameFx.build(s, this.style.index, this.color.index, this.color2.index, this.bold.get(), this.moving.get(), this.speed.get());
      }

      public float getScale() {
         return (float)this.scale.get();
      }

      public boolean hasOutline() {
         return this.outline.get();
      }

      public boolean hasShadow() {
         return this.shadow.get();
      }

      public boolean hasBackground() {
         return this.background.get();
      }

      public float getBgOpacity() {
         return (float)(this.bgOpacity.get() / 100.0);
      }

      public Map<String, Object> syncState() {
         Map<String, Object> m = new LinkedHashMap<>();
         m.put("t", this.text.get() == null ? "" : this.text.get());
         m.put("s", (long)this.style.index);
         m.put("c1", (long)this.color.index);
         m.put("c2", (long)this.color2.index);
         m.put("b", this.bold.get());
         m.put("m", this.moving.get());
         m.put("sp", this.speed.get());
         m.put("lego", this.badge.get());
         m.put("sc", this.scale.get());
         return m;
      }

      private static double mcWidth(String s, boolean bold) {
         double w = 0.0;

         for (char c : s.toCharArray()) {
            int cw = "il!.,:;|'".indexOf(c) >= 0 ? 2 : ("tI[] ".indexOf(c) >= 0 ? 4 : ("fk<>(){}*\"".indexOf(c) >= 0 ? 5 : 6));
            w += cw + 1 + (bold ? 1 : 0);
         }

         return w - 1.0;
      }

      private Identifier brick() {
         if (this.brickTex != null) {
            return this.brickTex;
         } else {
            int w = 160;
            int h = 128;
            BufferedImage img = new BufferedImage(w, h, 2);
            Graphics2D g = img.createGraphics();

            try {
               Gx.hints(g);

               for (int i = 0; i < 2; i++) {
                  int sx = 22 + i * 62;
                  g.setColor(new Color(181, 0, 8));
                  g.fill(new Double(sx, 6.0, 54.0, 34.0, 14.0, 14.0));
                  g.setColor(new Color(255, 74, 82));
                  g.fill(new Double(sx + 3, 4.0, 48.0, 28.0, 14.0, 14.0));
               }

               g.setColor(new Color(181, 0, 8));
               g.fill(new Double(6.0, 28.0, w - 12, h - 32, 22.0, 22.0));
               g.setColor(new Color(227, 0, 11));
               g.fill(new Double(6.0, 24.0, w - 12, h - 38, 22.0, 22.0));
               g.setColor(new Color(255, 255, 255, 70));
               g.fill(new Double(16.0, 32.0, w - 32, 10.0, 8.0, 8.0));
               g.setColor(new Color(0, 0, 0, 120));
               g.setStroke(new BasicStroke(4.0F));
               g.draw(new Double(6.0, 24.0, w - 12, h - 38, 22.0, 22.0));
            } finally {
               g.dispose();
            }

            Identifier id = Identifier.fromNamespaceAndPath("legoclient", "namebrick");
            Tx.register(id, img.getRGB(0, 0, w, h, null, 0, w), w, h, true);
            this.brickTex = id;
            return id;
         }
      }

      void renderOthers(float td) {
         LocalPlayer me = Mc.player();

         for (Player pl : Mc.players()) {
            if (pl != me) {
               Map<String, Object> r = LegoSync.remote(pl.getUUID());
               if (r != null && Json.bool(r, "lego", true)) {
                  double[] p = Mc.lerpPos(pl, td);
                  double dx = p[0] - R3.camX();
                  double dz = p[2] - R3.camZ();
                  if (!(dx * dx + dz * dz > 2304.0)) {
                     String t2 = Json.str(r, "t", "");
                     String s = t2.isBlank() ? Mc.name(pl) : t2;
                     double half = mcWidth(s, Json.bool(r, "b", true)) * 0.025 / 2.0;
                     double ih = 0.22;
                     double iw = ih * 160.0 / 128.0;
                     double off = -(half + 0.04 + iw / 2.0);
                     double yaw = Math.toRadians(R3.camYaw());
                     R3.billboard(
                        this.brick(),
                        p[0] + off * -Math.cos(yaw),
                        p[1] + Mc.height(pl) + 0.5 + 0.14,
                        p[2] + off * -Math.sin(yaw),
                        iw,
                        ih,
                        0.0F,
                        0.0F,
                        1.0F,
                        1.0F,
                        R3.argb(16777215, 1.0)
                     );
                  }
               }
            }
         }
      }

      void renderBrick(float td) {
         LocalPlayer me = Mc.player();
         if (this.badge.get() && me != null && !Mc.firstPerson() && !me.isInvisible()) {
            String s = this.text.get() != null && !this.text.get().isBlank() ? this.text.get() : Mc.name(me);
            if (s.length() > 24) {
               s = s.substring(0, 24);
            }

            double half = mcWidth(s, this.bold.get()) * 0.025 / 2.0;
            double ih = 0.22;
            double iw = ih * 160.0 / 128.0;
            double off = -(half + 0.04 + iw / 2.0);
            double[] p = Mc.lerpPos(me, td);
            double yaw = Math.toRadians(R3.camYaw());
            double y = p[1] + Mc.height(me) + 0.5 + 0.14;
            R3.billboard(this.brick(), p[0] + off * -Math.cos(yaw), y, p[2] + off * -Math.sin(yaw), iw, ih, 0.0F, 0.0F, 1.0F, 1.0F, R3.argb(16777215, 1.0));
         }
      }

      void renderCard(float td) {
         LocalPlayer me = Mc.player();
         if (this.song.get() && me != null && !Mc.firstPerson() && !me.isInvisible()) {
            PlayerState st = null;

            try {
               if (SpotifyHudMod.media() != null) {
                  st = SpotifyHudMod.media().state();
               }
            } catch (Throwable var32) {
            }

            boolean show = st != null && st.hasTrack && st.title != null && !st.title.isEmpty();
            long now = System.currentTimeMillis();
            if (show && !this.wasShowing) {
               this.fadeStart = now;
            }

            this.wasShowing = show;
            if (show) {
               int[] px = this.ready;
               if (px != null) {
                  this.ready = null;
                  Identifier old = this.cardTex;
                  Identifier id = Identifier.fromNamespaceAndPath("legoclient", "namecard/" + this.cardN++);
                  Tx.register(id, px, 640, 176, true);
                  this.cardTex = id;
                  this.paintedKey = this.requestedKey;
                  if (old != null) {
                     Tx.destroy(old);
                  }
               }

               long sec = st.currentProgress() / 1000L;
               Object art = null;
               int artV = 0;

               try {
                  art = SpotifyHudMod.media().art();
                  artV = SpotifyHudMod.media().artVersion();
               } catch (Throwable var31) {
               }

               int accent = this.cardColor.argb() & 16777215;
               String key = st.title + "|" + st.artist + "|" + artV + "|" + (st.playing ? sec : "p" + sec) + "|" + st.playing + "|" + accent;
               if (!key.equals(this.requestedKey) && !this.busy) {
                  this.busy = true;
                  this.requestedKey = key;
                  PlayerState fs = st;
                  Object fart = art;
                  long prog = st.currentProgress();
                  PAINT.execute(() -> {
                     try {
                        this.ready = paint(fs, fart, prog, accent);
                     } catch (Throwable var10x) {
                        LegoClient.LOG("Namenskarte: " + var10x);
                     } finally {
                        this.busy = false;
                     }
                  });
               }

               if (this.cardTex != null) {
                  double[] p = Mc.lerpPos(me, td);
                  double k = this.cardSize.get();
                  double w = 1.5 * k;
                  double h = w * 176.0 / 640.0;
                  double fade = Math.min(1.0, (now - this.fadeStart) / 400.0);
                  double bob = Math.sin(now / 900.0) * 0.015;
                  double y = p[1] + Mc.height(me) + 0.5 + 0.32 + h * 0.5 + bob + this.cardLift.get();
                  double yaw = Math.toRadians(R3.camYaw());
                  double side = this.cardSide.get();
                  R3.billboard(
                     this.cardTex,
                     p[0] + side * -Math.cos(yaw),
                     y,
                     p[2] + side * -Math.sin(yaw),
                     w,
                     h,
                     0.0F,
                     0.0F,
                     1.0F,
                     1.0F,
                     R3.argb(16777215, fade * this.cardAlpha.get() / 100.0)
                  );
               }
            }
         }
      }

      private static int[] paint(PlayerState s, Object art, long progress, int accent) {
         Color acc = new Color(accent);
         BufferedImage img = new BufferedImage(640, 176, 2);
         Graphics2D g = img.createGraphics();

         try {
            Gx.hints(g);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setColor(new Color(16, 17, 22, 238));
            g.fill(new Double(4.0, 4.0, 632.0, 168.0, 44.0, 44.0));
            g.setColor(new Color(255, 255, 255, 34));
            g.setStroke(new BasicStroke(3.0F));
            g.draw(new Double(5.5, 5.5, 629.0, 165.0, 42.0, 42.0));
            int cs = 128;
            int cx = 24;
            int cy = (176 - cs) / 2;
            RoundRectangle2D cover = new Double(cx, cy, cs, cs, 26.0, 26.0);
            if (art instanceof BufferedImage) {
               Shape old = g.getClip();
               g.setClip(cover);
               g.drawImage((BufferedImage)art, cx, cy, cs, cs, null);
               g.setClip(old);
            } else {
               g.setColor(new Color(30, 215, 96));
               g.fill(cover);
               g.setColor(new Color(11, 11, 11));
               g.setFont(Fonts.get(3, 70.0F));
               g.drawString("♪", cx + 38, cy + 90);
            }

            int tx = cx + cs + 24;
            int maxW = 640 - tx - 70;
            g.setColor(Color.WHITE);
            g.setFont(Fonts.get(3, 40.0F));
            g.drawString(Fonts.ellipsize(s.title, 3, 40.0F, maxW), tx, 66);
            g.setColor(new Color(255, 255, 255, 170));
            g.setFont(Fonts.get(2, 30.0F));
            g.drawString(Fonts.ellipsize(s.artist == null ? "" : s.artist, 2, 30.0F, maxW), tx, 106);
            int by = 130;
            int bw = 640 - tx - 30;
            g.setColor(new Color(255, 255, 255, 46));
            g.fill(new Double(tx, by, bw, 10.0, 10.0, 10.0));
            double f = s.durationMs > 0L ? Math.max(0.0, Math.min(1.0, (double)progress / s.durationMs)) : 0.0;
            if (f > 0.0) {
               g.setColor(acc);
               g.fill(new Double(tx, by, Math.max(10.0, bw * f), 10.0, 10.0, 10.0));
            }

            int px = 584;
            int py = 36;
            g.setColor(acc);
            g.fillOval(px, py, 34, 34);
            g.setColor(new Color(11, 11, 11));
            if (s.playing) {
               g.fillRect(px + 10, py + 9, 5, 16);
               g.fillRect(px + 19, py + 9, 5, 16);
            } else {
               g.fillPolygon(new int[]{px + 12, px + 12, px + 25}, new int[]{py + 8, py + 26, py + 17}, 3);
            }
         } finally {
            g.dispose();
         }

         return img.getRGB(0, 0, 640, 176, null, 0, 640);
      }
   }
}
