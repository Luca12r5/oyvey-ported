package dev.lego.visual;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Tx;
import dev.spotifyhud.PlayerState;
import dev.spotifyhud.SpotifyHudMod;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D.Double;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public final class NameFx {
   public static NameFx.Tag tag;

   private NameFx() {
   }

   public static void registerAll() {
      tag = Modules.register(new NameFx.Tag());
   }

   public static void render(float var0) {
      if (tag != null && tag.enabled) {
         tag.renderBrick(var0);
         tag.renderCard(var0);
      }
   }

   public static void apply(AvatarRenderState var0) {
      if (tag != null && tag.enabled) {
         LocalPlayer var1 = Mc.player();
         if (var1 != null && !Mc.firstPerson() && !var1.isInvisible()) {
            var0.nameTag = tag.component(var1);
            if (var0.nameTagAttachment == null) {
               var0.nameTagAttachment = new Vec3(0.0, Mc.height(var1) + 0.5, 0.0);
            }
         }
      }
   }

   private static int lerp(int var0, int var1, double var2) {
      var2 = Math.max(0.0, Math.min(1.0, var2));
      int var4 = (int)((var0 >> 16 & 0xFF) * (1.0 - var2) + (var1 >> 16 & 0xFF) * var2);
      int var5 = (int)((var0 >> 8 & 0xFF) * (1.0 - var2) + (var1 >> 8 & 0xFF) * var2);
      int var6 = (int)((var0 & 0xFF) * (1.0 - var2) + (var1 & 0xFF) * var2);
      return var4 << 16 | var5 << 8 | var6;
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
      private Identifier brickTex;
      private static final ExecutorService PAINT = Executors.newSingleThreadExecutor(var0 -> {
         Thread var1 = new Thread(var0, "Lego-NameCard");
         var1.setDaemon(true);
         var1.setPriority(4);
         return var1;
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

      private int colorAt(int var1, double var2) {
         int var4 = this.color.argb(var1 * 0.07) & 16777215;
         int var5 = this.color2.argb(var1 * 0.07 + 0.3) & 16777215;
         switch (this.style.index) {
            case 0:
               return Color.HSBtoRGB((float)((var2 * 0.25 + var1 * 0.06) % 1.0), 0.7F, 1.0F) & 16777215;
            case 1:
               return NameFx.lerp(var4, var5, 0.5 + 0.5 * Math.sin(var2 * 2.0 + var1 * 0.4));
            case 2:
               return NameFx.lerp(16726814, 16765499, 0.5 + 0.5 * Math.sin(var2 * 7.0 + var1 * 1.7));
            case 3:
               return NameFx.lerp(8189951, 16777215, 0.5 + 0.5 * Math.sin(var2 * 2.5 + var1 * 0.5));
            case 4:
               return NameFx.lerp(3158064, var4, 0.55 + 0.45 * Math.sin(var2 * 3.0));
            case 5:
               return (int)(var2 * 8.0 + var1 * 7) % 9 == 0 ? 16777215 : var4;
            case 6:
               return NameFx.lerp(var4, var5, 0.5 + 0.5 * Math.sin(var2 * 1.5 - var1 * 0.35));
            default:
               return NameFx.lerp(756282, 3932028, 0.5 + 0.5 * Math.sin(var2 * 9.0 + var1 * 2.3));
         }
      }

      Component component(LocalPlayer var1) {
         String var2 = this.text.get() != null && !this.text.get().isBlank() ? this.text.get() : Mc.name(var1);
         if (var2.length() > 24) {
            var2 = var2.substring(0, 24);
         }

         double var3 = System.currentTimeMillis() / 1000.0 * this.speed.get();
         int var5 = var2.length();
         MutableComponent var6 = Component.literal("");
         double var7 = var3 * 5.0 % (var5 + 4) - 2.0;
         int var9 = (int)((long)(var3 * 10.0) % Math.max(1, var5));

         for (int var10 = 0; var10 < var5; var10++) {
            int var11 = this.colorAt(var10, var3);
            boolean var12 = this.bold.get() || this.moving.get() && Math.abs(var10 - var7) < 1.0;
            boolean var13 = this.moving.get() && this.style.index == 5 && var10 == var9 && var2.charAt(var10) != ' ';
            var6.append(Component.literal(String.valueOf(var2.charAt(var10))).withStyle(var3x -> {
               Style var4 = var3x.withColor(var11);
               if (var12) {
                  var4 = var4.withBold(Boolean.TRUE);
               }

               if (var13) {
                  var4 = var4.withObfuscated(Boolean.TRUE);
               }

               return var4;
            }));
         }

         return var6;
      }

      private static double mcWidth(String var0, boolean var1) {
         double var2 = 0.0;

         for (char var7 : var0.toCharArray()) {
            int var8 = "il!.,:;|'".indexOf(var7) >= 0 ? 2 : ("tI[] ".indexOf(var7) >= 0 ? 4 : ("fk<>(){}*\"".indexOf(var7) >= 0 ? 5 : 6));
            var2 += var8 + 1 + (var1 ? 1 : 0);
         }

         return var2 - 1.0;
      }

      private Identifier brick() {
         if (this.brickTex != null) {
            return this.brickTex;
         } else {
            short var1 = 160;
            short var2 = 128;
            BufferedImage var3 = new BufferedImage(var1, var2, 2);
            Graphics2D var4 = var3.createGraphics();

            try {
               Gx.hints(var4);

               for (int var5 = 0; var5 < 2; var5++) {
                  int var6 = 22 + var5 * 62;
                  var4.setColor(new Color(181, 0, 8));
                  var4.fill(new Double(var6, 6.0, 54.0, 34.0, 14.0, 14.0));
                  var4.setColor(new Color(255, 74, 82));
                  var4.fill(new Double(var6 + 3, 4.0, 48.0, 28.0, 14.0, 14.0));
               }

               var4.setColor(new Color(181, 0, 8));
               var4.fill(new Double(6.0, 28.0, var1 - 12, var2 - 32, 22.0, 22.0));
               var4.setColor(new Color(227, 0, 11));
               var4.fill(new Double(6.0, 24.0, var1 - 12, var2 - 38, 22.0, 22.0));
               var4.setColor(new Color(255, 255, 255, 70));
               var4.fill(new Double(16.0, 32.0, var1 - 32, 10.0, 8.0, 8.0));
               var4.setColor(new Color(0, 0, 0, 120));
               var4.setStroke(new BasicStroke(4.0F));
               var4.draw(new Double(6.0, 24.0, var1 - 12, var2 - 38, 22.0, 22.0));
            } finally {
               var4.dispose();
            }

            Identifier var10 = Identifier.fromNamespaceAndPath("legoclient", "namebrick");
            Tx.register(var10, var3.getRGB(0, 0, var1, var2, null, 0, var1), var1, var2, true);
            this.brickTex = var10;
            return var10;
         }
      }

      void renderBrick(float var1) {
         LocalPlayer var2 = Mc.player();
         if (this.badge.get() && var2 != null && !Mc.firstPerson() && !var2.isInvisible()) {
            String var3 = this.text.get() != null && !this.text.get().isBlank() ? this.text.get() : Mc.name(var2);
            if (var3.length() > 24) {
               var3 = var3.substring(0, 24);
            }

            double var4 = mcWidth(var3, this.bold.get()) * 0.025 / 2.0;
            double var6 = 0.22;
            double var8 = var6 * 160.0 / 128.0;
            double var10 = -(var4 + 0.04 + var8 / 2.0);
            double[] var12 = Mc.lerpPos(var2, var1);
            double var13 = Math.toRadians(R3.camYaw());
            double var15 = var12[1] + Mc.height(var2) + 0.5 + 0.14;
            R3.billboard(
               this.brick(),
               var12[0] + var10 * -Math.cos(var13),
               var15,
               var12[2] + var10 * -Math.sin(var13),
               var8,
               var6,
               0.0F,
               0.0F,
               1.0F,
               1.0F,
               R3.argb(16777215, 1.0)
            );
         }
      }

      void renderCard(float var1) {
         LocalPlayer var2 = Mc.player();
         if (this.song.get() && var2 != null && !Mc.firstPerson() && !var2.isInvisible()) {
            PlayerState var3 = null;

            try {
               if (SpotifyHudMod.media() != null) {
                  var3 = SpotifyHudMod.media().state();
               }
            } catch (Throwable var32) {
            }

            boolean var4 = var3 != null && var3.hasTrack && var3.title != null && !var3.title.isEmpty();
            long var5 = System.currentTimeMillis();
            if (var4 && !this.wasShowing) {
               this.fadeStart = var5;
            }

            this.wasShowing = var4;
            if (var4) {
               int[] var7 = this.ready;
               if (var7 != null) {
                  this.ready = null;
                  Identifier var8 = this.cardTex;
                  Identifier var9 = Identifier.fromNamespaceAndPath("legoclient", "namecard/" + this.cardN++);
                  Tx.register(var9, var7, 640, 176, true);
                  this.cardTex = var9;
                  this.paintedKey = this.requestedKey;
                  if (var8 != null) {
                     Tx.destroy(var8);
                  }
               }

               long var33 = var3.currentProgress() / 1000L;
               Object var10 = null;
               int var11 = 0;

               try {
                  var10 = SpotifyHudMod.media().art();
                  var11 = SpotifyHudMod.media().artVersion();
               } catch (Throwable var31) {
               }

               int var12 = this.cardColor.argb() & 16777215;
               String var13 = var3.title + "|" + var3.artist + "|" + var11 + "|" + (var3.playing ? var33 : "p" + var33) + "|" + var3.playing + "|" + var12;
               if (!var13.equals(this.requestedKey) && !this.busy) {
                  this.busy = true;
                  this.requestedKey = var13;
                  PlayerState var14 = var3;
                  Object var15 = var10;
                  long var16 = var3.currentProgress();
                  PAINT.execute(() -> {
                     try {
                        this.ready = paint(var14, var15, var16, var12);
                     } catch (Throwable var10x) {
                        LegoClient.LOG("Namenskarte: " + var10x);
                     } finally {
                        this.busy = false;
                     }
                  });
               }

               if (this.cardTex != null) {
                  double[] var34 = Mc.lerpPos(var2, var1);
                  double var35 = this.cardSize.get();
                  double var17 = 1.5 * var35;
                  double var19 = var17 * 176.0 / 640.0;
                  double var21 = Math.min(1.0, (var5 - this.fadeStart) / 400.0);
                  double var23 = Math.sin(var5 / 900.0) * 0.015;
                  double var25 = var34[1] + Mc.height(var2) + 0.5 + 0.32 + var19 * 0.5 + var23 + this.cardLift.get();
                  double var27 = Math.toRadians(R3.camYaw());
                  double var29 = this.cardSide.get();
                  R3.billboard(
                     this.cardTex,
                     var34[0] + var29 * -Math.cos(var27),
                     var25,
                     var34[2] + var29 * -Math.sin(var27),
                     var17,
                     var19,
                     0.0F,
                     0.0F,
                     1.0F,
                     1.0F,
                     R3.argb(16777215, var21 * this.cardAlpha.get() / 100.0)
                  );
               }
            }
         }
      }

      private static int[] paint(PlayerState var0, Object var1, long var2, int var4) {
         Color var5 = new Color(var4);
         BufferedImage var6 = new BufferedImage(640, 176, 2);
         Graphics2D var7 = var6.createGraphics();

         try {
            Gx.hints(var7);
            var7.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            var7.setColor(new Color(16, 17, 22, 238));
            var7.fill(new Double(4.0, 4.0, 632.0, 168.0, 44.0, 44.0));
            var7.setColor(new Color(255, 255, 255, 34));
            var7.setStroke(new BasicStroke(3.0F));
            var7.draw(new Double(5.5, 5.5, 629.0, 165.0, 42.0, 42.0));
            short var8 = 128;
            byte var9 = 24;
            int var10 = (176 - var8) / 2;
            Double var11 = new Double(var9, var10, var8, var8, 26.0, 26.0);
            if (var1 instanceof BufferedImage) {
               Shape var12 = var7.getClip();
               var7.setClip(var11);
               var7.drawImage((BufferedImage)var1, var9, var10, var8, var8, null);
               var7.setClip(var12);
            } else {
               var7.setColor(new Color(30, 215, 96));
               var7.fill(var11);
               var7.setColor(new Color(11, 11, 11));
               var7.setFont(Fonts.get(3, 70.0F));
               var7.drawString("♪", var9 + 38, var10 + 90);
            }

            int var23 = var9 + var8 + 24;
            int var13 = 640 - var23 - 70;
            var7.setColor(Color.WHITE);
            var7.setFont(Fonts.get(3, 40.0F));
            var7.drawString(Fonts.ellipsize(var0.title, 3, 40.0F, var13), var23, 66);
            var7.setColor(new Color(255, 255, 255, 170));
            var7.setFont(Fonts.get(2, 30.0F));
            var7.drawString(Fonts.ellipsize(var0.artist == null ? "" : var0.artist, 2, 30.0F, var13), var23, 106);
            short var14 = 130;
            int var15 = 640 - var23 - 30;
            var7.setColor(new Color(255, 255, 255, 46));
            var7.fill(new Double(var23, var14, var15, 10.0, 10.0, 10.0));
            double var16 = var0.durationMs > 0L ? Math.max(0.0, Math.min(1.0, (double)var2 / var0.durationMs)) : 0.0;
            if (var16 > 0.0) {
               var7.setColor(var5);
               var7.fill(new Double(var23, var14, Math.max(10.0, var15 * var16), 10.0, 10.0, 10.0));
            }

            short var18 = 584;
            byte var19 = 36;
            var7.setColor(var5);
            var7.fillOval(var18, var19, 34, 34);
            var7.setColor(new Color(11, 11, 11));
            if (var0.playing) {
               var7.fillRect(var18 + 10, var19 + 9, 5, 16);
               var7.fillRect(var18 + 19, var19 + 9, 5, 16);
            } else {
               var7.fillPolygon(new int[]{var18 + 12, var18 + 12, var18 + 25}, new int[]{var19 + 8, var19 + 26, var19 + 17}, 3);
            }
         } finally {
            var7.dispose();
         }

         return var6.getRGB(0, 0, 640, 176, null, 0, 640);
      }
   }
}
