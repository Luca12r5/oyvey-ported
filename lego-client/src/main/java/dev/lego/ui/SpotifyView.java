package dev.lego.ui;

import dev.spotifyhud.SpotifyApi;
import dev.spotifyhud.SpotifyHudMod;
import java.util.ArrayList;
import java.util.List;

public final class SpotifyView extends View {
   private static final int GREEN = -14756000;
   private String query = "";
   private int tab = 0;
   private volatile List<String[]> results = new ArrayList<>();
   private volatile List<String[]> lists = null;
   private volatile boolean busy;
   private long typedAt;
   private String searched = "";
   private float scroll;
   private float scrollT;

   private static SpotifyApi api() {
      try {
         return SpotifyHudMod.api();
      } catch (Throwable var1) {
         return null;
      }
   }

   @Override
   public float designW() {
      return 420.0F;
   }

   @Override
   public float designH() {
      return 380.0F;
   }

   private void load(Runnable var1) {
      this.busy = true;
      Thread var2 = new Thread(() -> {
         try {
            var1.run();
         } finally {
            this.busy = false;
         }
      }, "Lego Spotify");
      var2.setDaemon(true);
      var2.start();
   }

   @Override
   public void draw() {
      float var1 = Ease.outQuint(this.open);
      Gx.fill(0, 0, this.W, this.H, Gx.rgba(0, 0.4F * this.open));
      int var2 = this.p(400.0);
      int var3 = this.p(360.0);
      int var4 = (this.W - var2) / 2;
      int var5 = (this.H - var3) / 2;
      Gx.pushAlpha(var1);
      Gx.push();
      Gx.scaleAt(this.W / 2.0F, this.H / 2.0F, 0.94F + 0.06F * var1);
      Gx.shadow(var4, var5, var2, var3, this.p(16.0), this.p(12.0), 0.55F);
      Gx.rect(var4, var5, var2, var3, this.p(16.0), -183365102);
      Gx.outline(var4, var5, var2, var3, this.p(16.0), Math.max(1, this.p(0.6)), 587202559);
      Gx.circle(var4 + this.p(26.0), var5 + this.p(26.0), this.p(11.0), -14756000);
      Gx.icon("music", var4 + this.p(26.0), var5 + this.p(26.0), this.pf(11.0), -16053493);
      Gx.text("Spotify", var4 + this.p(44.0), var5 + this.p(16.0), this.pf(13.0), 3, -1);
      SpotifyApi var6 = api();
      String var7 = Env.nowPlaying.get();
      if (var7 != null && !var7.isEmpty()) {
         Gx.textRight(
            Fonts.ellipsize("♪ " + var7, 2, this.pf(7.4F), this.p(200.0)), var4 + var2 - this.p(44.0), var5 + this.p(26.0), this.pf(7.4F), 2, -1426063361
         );
      }

      boolean var8 = this.hover(var4 + var2 - this.p(36.0), var5 + this.p(12.0), this.p(26.0), this.p(26.0));
      Gx.icon("x", var4 + var2 - this.p(23.0), var5 + this.p(25.0), this.pf(11.0), var8 ? -1 : -1996488705);
      Kit.hit(var4 + var2 - this.p(36.0), var5 + this.p(12.0), this.p(26.0), this.p(26.0)).click(() -> this.host.close());
      String[] var9 = new String[]{"Suchen", "Meine Playlists"};
      int var10 = var4 + this.p(16.0);

      for (int var11 = 0; var11 < 2; var11++) {
         int var12 = Math.round(Gx.width(var9[var11], this.pf(8.0), 3)) + this.p(24.0);
         int var13 = this.p(24.0);
         boolean var14 = var11 == this.tab;
         Gx.rect(var10, var5 + this.p(48.0), var12, var13, var13 / 2, var14 ? -1 : 452984831);
         Gx.textCenter(var9[var11], var10 + var12 / 2.0F, var5 + this.p(48.0) + var13 / 2.0F, this.pf(8.0), 3, var14 ? -16777216 : -570425345);
         final int tabIndex = var11;
         Kit.hit(var10, var5 + this.p(48.0), var12, var13).click(() -> {
            this.tab = tabIndex;
            this.scroll = this.scrollT = 0.0F;
            if (tabIndex == 1 && this.lists == null && api() != null) {
               this.load(() -> this.lists = api().playlists());
            }
         });
         var10 += var12 + this.p(8.0);
      }

      int var21 = var5 + this.p(82.0);
      if (this.tab == 0) {
         int var22 = var2 - this.p(32.0);
         int var24 = this.p(28.0);
         Gx.rect(var4 + this.p(16.0), var21, var22, var24, var24 / 2, -14013910);
         Gx.icon("search", var4 + this.p(32.0), var21 + var24 / 2.0F, this.pf(10.0), -1426063361);
         if (this.query.isEmpty()) {
            Gx.textMid("Song oder Künstler eingeben …", var4 + this.p(44.0), var21 + var24 / 2.0F, this.pf(8.0), 1, 2013265919);
         } else {
            float var26 = Gx.textMid(this.query, var4 + this.p(44.0), var21 + var24 / 2.0F, this.pf(8.4F), 2, -1);
            if (System.currentTimeMillis() / 500L % 2L == 0L) {
               Gx.fill(Math.round(var4 + this.p(45.0) + var26), var21 + var24 / 2 - this.p(6.0), Math.max(1, this.p(0.9)), this.p(12.0), -14756000);
            }
         }

         var21 += var24 + this.p(10.0);
         if (!this.query.equals(this.searched) && System.currentTimeMillis() - this.typedAt > 350L && !this.busy && var6 != null) {
            String var27 = this.query;
            this.searched = var27;
            this.load(() -> this.results = api().search(var27));
         }
      }

      List var23 = this.tab == 0 ? this.results : this.lists;
      int var25 = var5 + var3 - this.p(14.0) - var21;
      int var28 = this.p(34.0);
      if (var6 != null && var6.isLoggedIn()) {
         if (!this.busy || var23 != null && !var23.isEmpty()) {
            if (var23 != null && !var23.isEmpty()) {
               float var29 = Math.max(0, var23.size() * var28 - var25);
               this.scrollT = Math.max(0.0F, Math.min(var29, this.scrollT));
               this.scroll = this.scroll + (this.scrollT - this.scroll) * Math.min(1.0F, Gx.dt * 14.0F);
               Gx.clip(var4, var21, var2, var25);
               Kit.clip(var4, var21, var2, var25);
               Kit.hit(var4, var21, var2, var25).scroll(var2x -> this.scrollT = this.scrollT - var2x.floatValue() * var28 * 2.0F);

               for (int var16 = 0; var16 < var23.size(); var16++) {
                  int var17 = Math.round(var21 + var16 * var28 - this.scroll);
                  if (var17 + var28 >= var21 && var17 <= var21 + var25) {
                     String[] var18 = (String[])var23.get(var16);
                     boolean var19 = this.hover(var4 + this.p(10.0), var17, var2 - this.p(20.0), var28 - this.p(2.0));
                     if (var19) {
                        Gx.rect(var4 + this.p(10.0), var17, var2 - this.p(20.0), var28 - this.p(2.0), this.p(8.0), 536870911);
                     }

                     Gx.rect(var4 + this.p(16.0), var17 + this.p(5.0), this.p(24.0), this.p(24.0), this.p(5.0), this.tab == 0 ? -13421773 : -14730710);
                     Gx.icon(
                        var19 ? "play" : (this.tab == 0 ? "music" : "list"),
                        var4 + this.p(28.0),
                        var17 + this.p(17.0),
                        this.pf(10.0),
                        var19 ? -14756000 : -1426063361
                     );
                     Gx.text(Fonts.ellipsize(var18[0], 3, this.pf(8.2F), var2 - this.p(80.0)), var4 + this.p(50.0), var17 + this.p(6.0), this.pf(8.2F), 3, -1);
                     Gx.text(
                        Fonts.ellipsize(var18[1], 1, this.pf(7.0), var2 - this.p(80.0)),
                        var4 + this.p(50.0),
                        var17 + this.p(19.0),
                        this.pf(7.0),
                        1,
                        -1711276033
                     );
                     String var20 = var18[2];
                     Kit.hit(var4 + this.p(10.0), var17, var2 - this.p(20.0), var28 - this.p(2.0)).click(() -> {
                        if (api() != null) {
                           api().playUri(var20);
                        }

                        Toasts.show("spotify", "Spotify", "Spielt: " + var18[0], -14756000, 2200L);
                     });
                  }
               }

               Kit.unclip();
               Gx.unclip();
            } else {
               String var15 = var6.lastBrowseError;
               Gx.textCenter(
                  var15 != null ? var15 : (this.tab == 0 ? (this.query.isEmpty() ? "Tipp etwas ein, um zu suchen" : "Nichts gefunden") : "Keine Playlists"),
                  var4 + var2 / 2.0F,
                  var21 + this.p(40.0),
                  this.pf(8.4F),
                  2,
                  -1426063361
               );
            }
         } else {
            Gx.textCenter("Lädt …", var4 + var2 / 2.0F, var21 + this.p(40.0), this.pf(8.4F), 2, -1426063361);
         }
      } else {
         Gx.textCenter(
            "Spotify ist nicht verbunden – drück H im Spiel und melde dich an.", var4 + var2 / 2.0F, var21 + this.p(40.0), this.pf(8.0), 2, -1426063361
         );
      }

      Gx.pop();
      Gx.popAlpha();
   }

   @Override
   public boolean chr(String var1) {
      if (this.tab != 0) {
         return false;
      } else {
         if (this.query.length() < 60) {
            this.query = this.query + var1;
         }

         this.typedAt = System.currentTimeMillis();
         return true;
      }
   }

   @Override
   public boolean key(int var1, int var2) {
      if (var1 == 259 && this.tab == 0 && !this.query.isEmpty()) {
         this.query = this.query.substring(0, this.query.length() - 1);
         this.typedAt = System.currentTimeMillis();
         return true;
      } else if (var1 == 257 && this.tab == 0) {
         this.typedAt = 0L;
         return true;
      } else {
         return false;
      }
   }
}
