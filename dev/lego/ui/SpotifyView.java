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
   private volatile String activeQuery = "";
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

   private void load(Runnable r) {
      this.busy = true;
      Thread t = new Thread(() -> {
         try {
            r.run();
         } finally {
            this.busy = false;
         }
      }, "Lego Spotify");
      t.setDaemon(true);
      t.start();
   }

   @Override
   public void draw() {
      float t = Ease.outQuint(this.open);
      Gx.fill(0, 0, this.W, this.H, Gx.rgba(0, 0.4F * this.open));
      int w = this.p(400.0);
      int h = this.p(360.0);
      int x = (this.W - w) / 2;
      int y = (this.H - h) / 2;
      Gx.pushAlpha(t);
      Gx.push();
      Gx.scaleAt(this.W / 2.0F, this.H / 2.0F, 0.94F + 0.06F * t);
      Gx.shadow(x, y, w, h, this.p(16.0), this.p(12.0), 0.55F);
      Gx.rect(x, y, w, h, this.p(16.0), -183365102);
      Gx.outline(x, y, w, h, this.p(16.0), Math.max(1, this.p(0.6)), 587202559);
      Gx.circle(x + this.p(26.0), y + this.p(26.0), this.p(11.0), -14756000);
      Gx.icon("music", x + this.p(26.0), y + this.p(26.0), this.pf(11.0), -16053493);
      Gx.text("Spotify", x + this.p(44.0), y + this.p(16.0), this.pf(13.0), 3, -1);
      SpotifyApi a = api();
      String np = Env.nowPlaying.get();
      if (np != null && !np.isEmpty()) {
         Gx.textRight(Fonts.ellipsize("♪ " + np, 2, this.pf(7.4F), this.p(200.0)), x + w - this.p(44.0), y + this.p(26.0), this.pf(7.4F), 2, -1426063361);
      }

      boolean hx = this.hover(x + w - this.p(36.0), y + this.p(12.0), this.p(26.0), this.p(26.0));
      Gx.icon("x", x + w - this.p(23.0), y + this.p(25.0), this.pf(11.0), hx ? -1 : -1996488705);
      Kit.hit(x + w - this.p(36.0), y + this.p(12.0), this.p(26.0), this.p(26.0)).click(() -> this.host.close());
      String[] tabs = new String[]{"Suchen", "Meine Playlists"};
      int tx = x + this.p(16.0);

      for (int i = 0; i < 2; i++) {
         int tw = Math.round(Gx.width(tabs[i], this.pf(8.0), 3)) + this.p(24.0);
         int th = this.p(24.0);
         boolean sel = i == this.tab;
         Gx.rect(tx, y + this.p(48.0), tw, th, th / 2, sel ? -1 : 452984831);
         Gx.textCenter(tabs[i], tx + tw / 2.0F, y + this.p(48.0) + th / 2.0F, this.pf(8.0), 3, sel ? -16777216 : -570425345);
         Kit.hit(tx, y + this.p(48.0), tw, th).click(() -> {
            this.tab = i;
            this.scroll = this.scrollT = 0.0F;
            if (i == 1 && !this.busy && api() != null) {
               this.lists = null;
               this.load(() -> {
                  SpotifyApi sa = api();
                  if (sa != null) {
                     this.lists = sa.playlists();
                  }
               });
            }
         });
         tx += tw + this.p(8.0);
      }

      int ly = y + this.p(82.0);
      if (this.tab == 0) {
         int sw = w - this.p(32.0);
         int sh = this.p(28.0);
         Gx.rect(x + this.p(16.0), ly, sw, sh, sh / 2, -14013910);
         Gx.icon("search", x + this.p(32.0), ly + sh / 2.0F, this.pf(10.0), -1426063361);
         if (this.query.isEmpty()) {
            Gx.textMid("Song oder Künstler eingeben …", x + this.p(44.0), ly + sh / 2.0F, this.pf(8.0), 1, 2013265919);
         } else {
            float qw = Gx.textMid(this.query, x + this.p(44.0), ly + sh / 2.0F, this.pf(8.4F), 2, -1);
            if (System.currentTimeMillis() / 500L % 2L == 0L) {
               Gx.fill(Math.round(x + this.p(45.0) + qw), ly + sh / 2 - this.p(6.0), Math.max(1, this.p(0.9)), this.p(12.0), -14756000);
            }
         }

         ly += sh + this.p(10.0);
         if (!this.query.equals(this.searched) && System.currentTimeMillis() - this.typedAt > 350L && !this.busy && a != null) {
            String q = this.query;
            this.searched = q;
            this.activeQuery = q;
            this.load(() -> {
               SpotifyApi sa = api();
               if (sa != null) {
                  List<String[]> rx = sa.search(q);
                  if (q.equals(this.activeQuery)) {
                     this.results = rx;
                  }
               }
            });
         }
      }

      List<String[]> rows = this.tab == 0 ? this.results : this.lists;
      int lh = y + h - this.p(14.0) - ly;
      int rh = this.p(34.0);
      if (a != null && a.isLoggedIn()) {
         if (!this.busy || rows != null && !rows.isEmpty()) {
            if (rows != null && !rows.isEmpty()) {
               float maxS = Math.max(0, rows.size() * rh - lh);
               this.scrollT = Math.max(0.0F, Math.min(maxS, this.scrollT));
               this.scroll = this.scroll + (this.scrollT - this.scroll) * Math.min(1.0F, Gx.dt * 14.0F);
               Gx.clip(x, ly, w, lh);
               Kit.clip(x, ly, w, lh);
               Kit.hit(x, ly, w, lh).scroll(v -> this.scrollT = this.scrollT - v.floatValue() * rh * 2.0F);

               for (int i = 0; i < rows.size(); i++) {
                  int ry = Math.round(ly + i * rh - this.scroll);
                  if (ry + rh >= ly && ry <= ly + lh) {
                     String[] r = rows.get(i);
                     boolean hv = this.hover(x + this.p(10.0), ry, w - this.p(20.0), rh - this.p(2.0));
                     if (hv) {
                        Gx.rect(x + this.p(10.0), ry, w - this.p(20.0), rh - this.p(2.0), this.p(8.0), 536870911);
                     }

                     Gx.rect(x + this.p(16.0), ry + this.p(5.0), this.p(24.0), this.p(24.0), this.p(5.0), this.tab == 0 ? -13421773 : -14730710);
                     Gx.icon(hv ? "play" : (this.tab == 0 ? "music" : "list"), x + this.p(28.0), ry + this.p(17.0), this.pf(10.0), hv ? -14756000 : -1426063361);
                     Gx.text(Fonts.ellipsize(r[0], 3, this.pf(8.2F), w - this.p(80.0)), x + this.p(50.0), ry + this.p(6.0), this.pf(8.2F), 3, -1);
                     Gx.text(Fonts.ellipsize(r[1], 1, this.pf(7.0), w - this.p(80.0)), x + this.p(50.0), ry + this.p(19.0), this.pf(7.0), 1, -1711276033);
                     String uri = r[2];
                     Kit.hit(x + this.p(10.0), ry, w - this.p(20.0), rh - this.p(2.0)).click(() -> {
                        if (api() != null) {
                           api().playUri(uri);
                        }

                        Toasts.show("spotify", "Spotify", "Spielt: " + r[0], -14756000, 2200L);
                     });
                  }
               }

               Kit.unclip();
               Gx.unclip();
            } else {
               String e = a.lastBrowseError;
               Gx.textCenter(
                  e != null ? e : (this.tab == 0 ? (this.query.isEmpty() ? "Tipp etwas ein, um zu suchen" : "Nichts gefunden") : "Keine Playlists"),
                  x + w / 2.0F,
                  ly + this.p(40.0),
                  this.pf(8.4F),
                  2,
                  -1426063361
               );
            }
         } else {
            Gx.textCenter("Lädt …", x + w / 2.0F, ly + this.p(40.0), this.pf(8.4F), 2, -1426063361);
         }
      } else {
         Gx.textCenter("Spotify ist nicht verbunden – drück H im Spiel und melde dich an.", x + w / 2.0F, ly + this.p(40.0), this.pf(8.0), 2, -1426063361);
      }

      Gx.pop();
      Gx.popAlpha();
   }

   @Override
   public boolean chr(String s) {
      if (this.tab != 0) {
         return false;
      } else {
         if (this.query.length() < 60) {
            this.query = this.query + s;
         }

         this.typedAt = System.currentTimeMillis();
         return true;
      }
   }

   @Override
   public boolean key(int key, int mods) {
      if (key == 259 && this.tab == 0 && !this.query.isEmpty()) {
         this.query = this.query.substring(0, this.query.length() - 1);
         this.typedAt = System.currentTimeMillis();
         return true;
      } else if (key == 257 && this.tab == 0) {
         this.typedAt = 0L;
         return true;
      } else {
         return false;
      }
   }
}
