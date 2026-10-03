package dev.lego.games;

import dev.lego.LegoClient;
import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import dev.lego.ui.View;
import dev.lego.ui.hub.HubView;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public abstract class GameView extends View {
   public static final int K_LEFT = 263;
   public static final int K_RIGHT = 262;
   public static final int K_UP = 265;
   public static final int K_DOWN = 264;
   public static final int K_SPACE = 32;
   public static final int K_ENTER = 257;
   public static final int K_W = 87;
   public static final int K_A = 65;
   public static final int K_S = 83;
   public static final int K_D = 68;
   public static final int K_R = 82;
   public static final int K_P = 80;
   public static final int K_ESC = 256;
   protected final GameInfo info;
   protected final Random rnd = new Random();
   protected GameView.State state = GameView.State.READY;
   protected long score = 0L;
   protected float time = 0.0F;
   private float acc = 0.0F;
   private boolean record = false;
   private long stateAt = System.currentTimeMillis();
   protected int bx;
   protected int by;
   protected float u = 2.0F;
   private boolean initialized = false;
   private boolean dragStarted = false;

   @Override
   public boolean blur() {
      return false;
   }

   protected GameView(String var1) {
      this.info = GameInfo.get(var1);
   }

   protected abstract float boardW();

   protected abstract float boardH();

   protected abstract void reset();

   protected abstract void update(float var1);

   protected abstract void render();

   protected void onKey(int var1) {
   }

   protected void onKeyUp(int var1) {
   }

   protected void onClick(float var1, float var2, int var3) {
   }

   protected void onRelease(float var1, float var2, int var3) {
   }

   protected void onDrag(float var1, float var2) {
   }

   protected String scoreText() {
      return String.valueOf(this.score);
   }

   protected String scoreLabel() {
      return "Punkte";
   }

   protected String bestText(long var1) {
      return String.valueOf(var1);
   }

   protected String overTitle() {
      return "Game Over";
   }

   protected boolean clickToStart() {
      return true;
   }

   protected void start() {
      this.reset();
      this.score = 0L;
      this.time = 0.0F;
      this.record = false;
      this.state = GameView.State.PLAYING;
      this.stateAt = System.currentTimeMillis();
   }

   protected void gameOver() {
      this.gameOver(true);
   }

   protected void gameOver(boolean var1) {
      if (this.state != GameView.State.OVER) {
         this.state = GameView.State.OVER;
         this.stateAt = System.currentTimeMillis();
         this.record = var1 && GameInfo.submit(this.info.id, this.score);
         // Counted results go to the LEGO server, which awards LEGO Coins (daily cap).
         if (var1) dev.lego.net.LegoAuth.reportGame(this.info.id, this.score, this.record);
         Sound.play(this.record ? "minecraft:ui.toast.challenge_complete" : "minecraft:block.note_block.bass", this.record ? 1.2F : 0.7F, 0.35F);
      }
   }

   protected int X(float var1) {
      return Math.round(this.bx + var1 * this.u);
   }

   protected int Y(float var1) {
      return Math.round(this.by + var1 * this.u);
   }

   protected int S(float var1) {
      return Math.round(var1 * this.u);
   }

   protected void box(float var1, float var2, float var3, float var4, float var5, int var6) {
      Gx.rect(this.X(var1), this.Y(var2), this.S(var3), this.S(var4), this.S(var5), var6);
   }

   protected void boxV(float var1, float var2, float var3, float var4, float var5, int var6, int var7) {
      Gx.rectV(this.X(var1), this.Y(var2), this.S(var3), this.S(var4), this.S(var5), var6, var7);
   }

   protected void circle(float var1, float var2, float var3, int var4) {
      Gx.circle(this.X(var1), this.Y(var2), Math.max(1, this.S(var3)), var4);
   }

   protected void text(String var1, float var2, float var3, float var4, int var5, int var6) {
      Gx.textCenter(var1, this.X(var2), this.Y(var3), var4 * this.u, var5, var6);
   }

   protected void brick(float var1, float var2, float var3, float var4, int var5) {
      float var6 = Math.min(var3, var4) * 0.18F;
      this.boxV(var1, var2, var3, var4, var6, Gx.mix(var5, -1, 0.18F), Gx.mix(var5, -16777216, 0.12F));
      Gx.outline(
         this.X(var1),
         this.Y(var2),
         this.S(var3),
         this.S(var4),
         this.S(var6),
         Math.max(1, this.S(Math.min(var3, var4) * 0.05F)),
         Gx.withAlpha(Gx.mix(var5, -16777216, 0.45F), 0.7F)
      );
      float var7 = Math.min(var3, var4) * 0.17F;
      int var8 = Math.max(1, Math.round(var3 / var4));

      for (int var9 = 0; var9 < Math.min(var8, 4); var9++) {
         float var10 = var1 + var3 * (var9 + 0.5F) / Math.min(var8, 4);
         this.circle(var10, var2 + var4 * 0.42F, var7, Gx.mix(var5, -16777216, 0.2F));
         this.circle(var10, var2 + var4 * 0.38F, var7 * 0.9F, Gx.mix(var5, -1, 0.22F));
      }
   }

   @Override
   public float designW() {
      return this.boardW() + 60.0F;
   }

   @Override
   public float designH() {
      return this.boardH() + 110.0F;
   }

   @Override
   public float openMs() {
      return 320.0F;
   }

   @Override
   public final void draw() {
      if (!this.initialized) {
         this.initialized = true;
         this.reset();
      }

      if (this.state == GameView.State.PLAYING) {
         for (this.acc = this.acc + Math.min(0.1F, Gx.dt); this.acc >= 0.016666668F && this.state == GameView.State.PLAYING; this.acc -= 0.016666668F) {
            this.update(0.016666668F);
            this.time += 0.016666668F;
         }
      } else {
         this.acc = 0.0F;
      }

      float var1 = Ease.outQuint(this.open);
      Gx.fill(0, 0, this.W, this.H, Gx.rgba(0, 0.35F * this.open));
      int var2 = this.p(this.boardW());
      int var3 = this.p(this.boardH());
      int var4 = var2 + this.p(28.0);
      int var5 = var3 + this.p(72.0);
      int var6 = (this.W - var4) / 2;
      int var7 = (this.H - var5) / 2;
      Gx.pushAlpha(var1);
      Gx.push();
      Gx.scaleAt(this.W / 2.0F, this.H / 2.0F, 0.94F + 0.06F * var1);
      Gx.shadow(var6, var7, var4, var5, this.p(18.0), this.p(14.0), 0.55F);
      Gx.rect(var6, var7, var4, var5, this.p(18.0), Style.bg);
      Gx.outline(var6, var7, var4, var5, this.p(18.0), Math.max(1, this.p(0.6)), Style.stroke);
      int var8 = var7 + this.p(14.0);
      int var9 = this.p(30.0);
      boolean var10 = this.hover(var6 + this.p(14.0), var8, var9, var9);
      Gx.rect(var6 + this.p(14.0), var8, var9, var9, this.p(9.0), var10 ? Style.surfaceHover : Style.surface);
      Gx.icon("back", var6 + this.p(14.0) + var9 / 2.0F - this.pf(1.0), var8 + var9 / 2.0F, this.pf(14.0), Style.text);
      Kit.hit(var6 + this.p(14.0), var8, var9, var9).click(this::back);
      int var11 = this.p(30.0);
      Gx.rect(var6 + this.p(52.0), var8, var11, var11, this.p(9.0), Gx.withAlpha(this.info.color, 0.2F));
      Gx.icon(this.info.icon, var6 + this.p(52.0) + var11 / 2.0F, var8 + var11 / 2.0F, this.pf(16.0), this.info.color);
      Gx.text(Fonts.ellipsize(this.info.name, 3, this.pf(12.5), var4 - this.p(290.0)), var6 + this.p(90.0), var8 + this.pf(1.0), this.pf(12.5), 3, Style.text);
      int var12 = this.p(84.0);
      int var13 = this.p(32.0);
      int var14 = var6 + var4 - this.p(14.0) - var12;
      Gx.text(
         Fonts.ellipsize(this.info.tagline, 1, this.pf(7.2F), var14 - var12 - this.p(18.0) - (var6 + this.p(90.0))),
         var6 + this.p(90.0),
         var8 + this.pf(18.0),
         this.pf(7.2F),
         1,
         Style.sub
      );
      this.scoreBox(var14, var8 - this.p(1.0), var12, var13, "REKORD", GameInfo.hasBest(this.info.id) ? this.bestText(GameInfo.best(this.info.id)) : "-", false);
      this.scoreBox(var14 - var12 - this.p(8.0), var8 - this.p(1.0), var12, var13, this.scoreLabel().toUpperCase(Locale.ROOT), this.scoreText(), true);
      this.bx = var6 + this.p(14.0);
      this.by = var7 + this.p(58.0);
      this.u = this.P;
      Gx.rect(this.bx, this.by, var2, var3, this.p(12.0), Style.light ? -1446928 : -16053232);
      Gx.clip(this.bx, this.by, var2, var3);

      try {
         this.render();
      } catch (Throwable var16) {
         LegoClient.LOG("Spiel-Fehler: " + var16);
      }

      Gx.unclip();
      Gx.outline(this.bx, this.by, var2, var3, this.p(12.0), Math.max(1, this.p(0.6)), Style.stroke);
      Kit.hit(this.bx, this.by, var2, var3).drag((var1x, var2x) -> {
         float var3x = (var1x - this.bx) / this.u;
         float var4x = (var2x - this.by) / this.u;
         if (this.dragStarted) {
            this.onDrag(var3x, var4x);
         } else {
            this.dragStarted = true;
            this.boardPress(var3x, var4x, 0);
         }
      }).right(() -> this.boardPress((this.mx - this.bx) / this.u, (this.my - this.by) / this.u, 1));
      this.overlay(this.bx, this.by, var2, var3);
      Gx.pop();
      Gx.popAlpha();
   }

   private void boardPress(float var1, float var2, int var3) {
      if (this.state == GameView.State.READY && this.clickToStart()) {
         this.start();
         Sound.click();
      } else {
         if (this.state == GameView.State.PLAYING) {
            this.onClick(var1, var2, var3);
         }
      }
   }

   @Override
   public boolean mouseUp(float var1, float var2, int var3) {
      if (this.dragStarted && this.state == GameView.State.PLAYING) {
         this.onRelease((var1 - this.bx) / this.u, (var2 - this.by) / this.u, var3);
      }

      this.dragStarted = false;
      return super.mouseUp(var1, var2, var3);
   }

   private void scoreBox(int var1, int var2, int var3, int var4, String var5, String var6, boolean var7) {
      Gx.rect(var1, var2, var3, var4, this.p(8.0), var7 ? Gx.withAlpha(Style.accent, 0.14F) : Style.surface);
      Gx.textCenter(var5, var1 + var3 / 2.0F, var2 + this.pf(9.0), this.pf(5.6F), 3, var7 ? Style.accent : Style.muted);
      Gx.textCenter(var6, var1 + var3 / 2.0F, var2 + this.pf(21.5), this.pf(10.0), 3, Style.text);
   }

   private void overlay(int var1, int var2, int var3, int var4) {
      float var5 = (float)(System.currentTimeMillis() - this.stateAt) / 1000.0F;
      if (this.state != GameView.State.PLAYING) {
         float var6 = Ease.outCubic(Math.min(1.0F, var5 / 0.25F));
         Gx.pushAlpha(var6);
         Gx.rect(var1, var2, var3, var4, this.p(12.0), Gx.rgba(Style.light ? 15987959 : 460811, 0.82F));
         int var7 = var1 + var3 / 2;
         if (this.state == GameView.State.READY) {
            int var8 = var2 + var4 / 2 - this.p(56.0);
            Gx.glow(var7, var8, this.p(60.0), Gx.withAlpha(this.info.color, 0.25F));
            Gx.icon(this.info.icon, var7, var8, this.pf(34.0), this.info.color);
            Gx.textCenter(this.info.name, var7, var8 + this.pf(38.0), this.pf(15.0), 3, Style.text);
            List var9 = Fonts.wrap(this.info.howTo, 1, this.pf(8.2F), var3 - this.p(60.0), 4);
            float var10 = var8 + this.pf(58.0);

            for (String var12 : (Iterable<String>) (Iterable<?>) (var9)) {
               Gx.textCenter(var12, var7, var10, this.pf(8.2F), 1, Style.sub);
               var10 += this.pf(12.5);
            }

            float var20 = this.pf(7.4F);
            int var21 = Math.round(Gx.width(this.info.controls, var20, 2)) + this.p(34.0);
            int var13 = this.p(22.0);
            int var14 = var7 - var21 / 2;
            int var15 = Math.round(var10 + this.pf(6.0));
            Gx.rect(var14, var15, var21, var13, var13 / 2, Style.surface2);
            Gx.icon("keys", var14 + this.p(14.0), var15 + var13 / 2.0F, this.pf(10.0), Style.sub);
            Gx.textMid(this.info.controls, var14 + this.p(25.0), var15 + var13 / 2.0F, var20, 2, Style.sub);
            float var16 = 0.55F + 0.45F * (float)Math.sin(System.currentTimeMillis() / 350.0);
            Gx.textSpaced(
               this.clickToStart() ? "KLICK ODER LEERTASTE ZUM STARTEN" : "LEERTASTE ZUM STARTEN",
               var7,
               var15 + var13 + this.pf(20.0),
               this.pf(7.4F),
               3,
               Gx.withAlpha(Style.accent, var16),
               this.pf(1.4F)
            );
         } else if (this.state == GameView.State.PAUSED) {
            int var17 = var2 + var4 / 2 - this.p(30.0);
            Gx.icon("pause", var7, var17, this.pf(30.0), Style.text);
            Gx.textCenter("Pause", var7, var17 + this.pf(32.0), this.pf(15.0), 3, Style.text);
            this.buttons(var7, var17 + this.p(54.0), "Weiter", "play", () -> {
               this.state = GameView.State.PLAYING;
               this.stateAt = System.currentTimeMillis();
            }, "Neustart", "reset", this::start);
         } else {
            int var18 = var2 + var4 / 2 - this.p(52.0);
            if (this.record) {
               float var19 = 1.0F + 0.08F * (float)Math.sin(var5 * 6.0F);
               Gx.push();
               Gx.scaleAt(var7, var18, var19);
               Gx.glow(var7, var18, this.p(60.0), 1442825533);
               Gx.icon("trophy", var7, var18, this.pf(34.0), -15043);
               Gx.pop();
            } else {
               Gx.icon("skull", var7, var18, this.pf(30.0), Style.sub);
            }

            Gx.textCenter(this.record ? "Neuer Rekord!" : this.overTitle(), var7, var18 + this.pf(36.0), this.pf(15.0), 3, this.record ? -15043 : Style.text);
            Gx.textCenter(this.scoreLabel() + ": " + this.scoreText(), var7, var18 + this.pf(56.0), this.pf(10.0), 2, Style.sub);
            int coins = dev.lego.net.LegoAuth.lastCoins;
            if (coins > 0) Gx.textCenter("+" + coins + " LEGO Coins", var7, var18 + this.pf(71.0), this.pf(9.0), 2, -15043);
            this.buttons(var7, var18 + this.p(88.0), "Nochmal", "reset", this::start, "Zurück", "back", this::back);
         }

         Gx.popAlpha();
      }
   }

   private void buttons(int var1, int var2, String var3, String var4, Runnable var5, String var6, String var7, Runnable var8) {
      int var9 = this.p(110.0);
      int var10 = this.p(30.0);
      int var11 = this.p(10.0);
      int var12 = var1 - var9 - var11 / 2;
      int var13 = var1 + var11 / 2;
      boolean var14 = this.hover(var12, var2, var9, var10);
      boolean var15 = this.hover(var13, var2, var9, var10);
      Kit.button("g1" + var3, var3, var4, var12, var2, var9, var10, this.P, true, var14);
      Kit.button("g2" + var6, var6, var7, var13, var2, var9, var10, this.P, false, var15);
      Kit.hit(var12, var2, var9, var10).click(var5);
      Kit.hit(var13, var2, var9, var10).click(var8);
   }

   protected void back() {
      if (this.host != null) {
         this.host.open(new HubView(HubView.Section.GAMES));
      }
   }

   @Override
   public boolean key(int var1, int var2) {
      if (var1 < 0) {
         if (this.state == GameView.State.PLAYING) {
            this.onKeyUp(-1 - var1);
         }

         return true;
      } else if (var1 == 256) {
         if (this.state == GameView.State.PLAYING) {
            this.state = GameView.State.PAUSED;
            this.stateAt = System.currentTimeMillis();
         } else {
            this.back();
         }

         return true;
      } else if (var1 != 80 || this.state != GameView.State.PLAYING && this.state != GameView.State.PAUSED) {
         if (var1 != 82 || this.state != GameView.State.OVER && this.state != GameView.State.PAUSED) {
            if ((var1 == 32 || var1 == 257) && (this.state == GameView.State.READY || this.state == GameView.State.OVER)) {
               this.start();
               if (var1 == 32 && this.state == GameView.State.PLAYING) {
                  this.onKey(var1);
               }

               return true;
            } else {
               if (this.state == GameView.State.PLAYING) {
                  this.onKey(var1);
               }

               return true;
            }
         } else {
            this.start();
            return true;
         }
      } else {
         this.state = this.state == GameView.State.PLAYING ? GameView.State.PAUSED : GameView.State.PLAYING;
         this.stateAt = System.currentTimeMillis();
         return true;
      }
   }

   protected static enum State {
      READY,
      PLAYING,
      PAUSED,
      OVER;
   }
}
