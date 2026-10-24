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
   private static final float MIN_BOARD = 360.0F;
   private boolean dragStarted = false;

   @Override
   public boolean blur() {
      return false;
   }

   protected GameView(String id) {
      this.info = GameInfo.get(id);
   }

   protected abstract float boardW();

   protected abstract float boardH();

   protected abstract void reset();

   protected abstract void update(float var1);

   protected abstract void render();

   protected void onKey(int key) {
   }

   protected void onKeyUp(int key) {
   }

   protected void onClick(float x, float y, int button) {
   }

   protected void onRelease(float x, float y, int button) {
   }

   protected void onDrag(float x, float y) {
   }

   protected String scoreText() {
      return String.valueOf(this.score);
   }

   protected String scoreLabel() {
      return "Punkte";
   }

   protected String bestText(long best) {
      return String.valueOf(best);
   }

   protected String overTitle() {
      return "Game Over";
   }

   protected boolean clickToStart() {
      return true;
   }

   protected boolean hasAi() {
      return false;
   }

   protected int aiLevel() {
      return GameInfo.difficulty(this.info.id);
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

   protected void gameOver(boolean submit) {
      if (this.state != GameView.State.OVER) {
         this.state = GameView.State.OVER;
         this.stateAt = System.currentTimeMillis();
         this.record = submit && GameInfo.submit(this.info.id, this.score);
         Sound.play(this.record ? "minecraft:ui.toast.challenge_complete" : "minecraft:block.note_block.bass", this.record ? 1.2F : 0.7F, 0.35F);
      }
   }

   protected int X(float bu) {
      return Math.round(this.bx + bu * this.u);
   }

   protected int Y(float bu) {
      return Math.round(this.by + bu * this.u);
   }

   protected int S(float bu) {
      return Math.round(bu * this.u);
   }

   protected void box(float x, float y, float w, float h, float r, int argb) {
      Gx.rect(this.X(x), this.Y(y), this.S(w), this.S(h), this.S(r), argb);
   }

   protected void boxV(float x, float y, float w, float h, float r, int top, int bottom) {
      Gx.rectV(this.X(x), this.Y(y), this.S(w), this.S(h), this.S(r), top, bottom);
   }

   protected void circle(float cx, float cy, float r, int argb) {
      Gx.circle(this.X(cx), this.Y(cy), Math.max(1, this.S(r)), argb);
   }

   protected void text(String s, float cx, float cy, float size, int weight, int argb) {
      Gx.textCenter(s, this.X(cx), this.Y(cy), size * this.u, weight, argb);
   }

   protected void brick(float x, float y, float w, float h, int argb) {
      float r = Math.min(w, h) * 0.18F;
      this.boxV(x, y, w, h, r, Gx.mix(argb, -1, 0.18F), Gx.mix(argb, -16777216, 0.12F));
      Gx.outline(
         this.X(x), this.Y(y), this.S(w), this.S(h), this.S(r), Math.max(1, this.S(Math.min(w, h) * 0.05F)), Gx.withAlpha(Gx.mix(argb, -16777216, 0.45F), 0.7F)
      );
      float sr = Math.min(w, h) * 0.17F;
      int studs = Math.max(1, Math.round(w / h));

      for (int i = 0; i < Math.min(studs, 4); i++) {
         float sx = x + w * (i + 0.5F) / Math.min(studs, 4);
         this.circle(sx, y + h * 0.42F, sr, Gx.mix(argb, -16777216, 0.2F));
         this.circle(sx, y + h * 0.38F, sr * 0.9F, Gx.mix(argb, -1, 0.22F));
      }
   }

   protected void line(float x1, float y1, float x2, float y2, float th, int argb) {
      float px1 = this.bx + x1 * this.u;
      float py1 = this.by + y1 * this.u;
      float px2 = this.bx + x2 * this.u;
      float py2 = this.by + y2 * this.u;
      float len = (float)Math.hypot(px2 - px1, py2 - py1);
      int r = Math.max(1, Math.round(th * this.u / 2.0F));
      int n = Math.max(1, (int)Math.ceil(len / Math.max(1.0F, r * 0.45F)));

      for (int i = 0; i <= n; i++) {
         float t = (float)i / n;
         Gx.circle(Math.round(px1 + (px2 - px1) * t), Math.round(py1 + (py2 - py1) * t), r, argb);
      }
   }

   protected void ring(float cx, float cy, float r, float th, int argb) {
      Gx.ringAt(this.X(cx), this.Y(cy), Math.max(1, this.S(r)), Math.max(1, this.S(th)), argb);
   }

   protected float bmx() {
      return (this.mx - this.bx) / this.u;
   }

   protected float bmy() {
      return (this.my - this.by) / this.u;
   }

   protected void textL(String s, float x, float cy, float size, int weight, int argb) {
      Gx.textMid(s, this.X(x), this.Y(cy), size * this.u, weight, argb);
   }

   protected boolean button(String label, float x, float y, float w, float h, int base) {
      float hx = this.bmx();
      float hy = this.bmy();
      boolean hv = hx >= x && hy >= y && hx <= x + w && hy <= y + h;
      this.box(x, y, w, h, Math.min(h / 2.0F, 8.0F), hv ? Gx.mix(base, -1, 0.15F) : base);
      this.text(label, x + w / 2.0F, y + h / 2.0F, Math.min(9.0F, h * 0.42F), 3, -1);
      return hv;
   }

   protected static boolean in(float x, float y, float rx, float ry, float rw, float rh) {
      return x >= rx && y >= ry && x <= rx + rw && y <= ry + rh;
   }

   @Override
   public float designW() {
      return Math.max(this.boardW(), 360.0F) + 60.0F;
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

      float t = Ease.outQuint(this.open);
      Gx.fill(0, 0, this.W, this.H, Gx.rgba(0, 0.35F * this.open));
      int bw = this.p(this.boardW());
      int bh = this.p(this.boardH());
      int pw = Math.max(bw, this.p(360.0)) + this.p(28.0);
      int ph = bh + this.p(72.0);
      int px = (this.W - pw) / 2;
      int py = (this.H - ph) / 2;
      Gx.pushAlpha(t);
      Gx.push();
      Gx.scaleAt(this.W / 2.0F, this.H / 2.0F, 0.94F + 0.06F * t);
      Gx.shadow(px, py, pw, ph, this.p(18.0), this.p(14.0), 0.55F);
      Gx.rect(px, py, pw, ph, this.p(18.0), Style.bg);
      Gx.outline(px, py, pw, ph, this.p(18.0), Math.max(1, this.p(0.6)), Style.stroke);
      int tb = py + this.p(14.0);
      int bs = this.p(30.0);
      boolean hb = this.hover(px + this.p(14.0), tb, bs, bs);
      Gx.rect(px + this.p(14.0), tb, bs, bs, this.p(9.0), hb ? Style.surfaceHover : Style.surface);
      Gx.icon("back", px + this.p(14.0) + bs / 2.0F - this.pf(1.0), tb + bs / 2.0F, this.pf(14.0), Style.text);
      Kit.hit(px + this.p(14.0), tb, bs, bs).click(this::back);
      int is = this.p(30.0);
      Gx.rect(px + this.p(52.0), tb, is, is, this.p(9.0), Gx.withAlpha(this.info.color, 0.2F));
      Gx.icon(this.info.icon, px + this.p(52.0) + is / 2.0F, tb + is / 2.0F, this.pf(16.0), this.info.color);
      Gx.text(Fonts.ellipsize(this.info.name, 3, this.pf(12.5), pw - this.p(290.0)), px + this.p(90.0), tb + this.pf(1.0), this.pf(12.5), 3, Style.text);
      int sw = this.p(84.0);
      int sh = this.p(32.0);
      int sx = px + pw - this.p(14.0) - sw;
      Gx.text(
         Fonts.ellipsize(this.info.tagline, 1, this.pf(7.2F), sx - sw - this.p(18.0) - (px + this.p(90.0))),
         px + this.p(90.0),
         tb + this.pf(18.0),
         this.pf(7.2F),
         1,
         Style.sub
      );
      this.scoreBox(sx, tb - this.p(1.0), sw, sh, "REKORD", GameInfo.hasBest(this.info.id) ? this.bestText(GameInfo.best(this.info.id)) : "-", false);
      this.scoreBox(sx - sw - this.p(8.0), tb - this.p(1.0), sw, sh, this.scoreLabel().toUpperCase(Locale.ROOT), this.scoreText(), true);
      this.bx = px + (pw - bw) / 2;
      this.by = py + this.p(58.0);
      this.u = this.P;
      Gx.rect(this.bx, this.by, bw, bh, this.p(12.0), Style.light ? -1446928 : -16053232);
      Gx.clip(this.bx, this.by, bw, bh);

      try {
         this.render();
      } catch (Throwable var16) {
         LegoClient.LOG("Spiel-Fehler: " + var16);
      }

      Gx.unclip();
      Gx.outline(this.bx, this.by, bw, bh, this.p(12.0), Math.max(1, this.p(0.6)), Style.stroke);
      Kit.hit(this.bx, this.by, bw, bh).drag((mx, my) -> {
         float gx = (mx - this.bx) / this.u;
         float gy = (my - this.by) / this.u;
         if (this.dragStarted) {
            this.onDrag(gx, gy);
         } else {
            this.dragStarted = true;
            this.boardPress(gx, gy, 0);
         }
      }).right(() -> this.boardPress((this.mx - this.bx) / this.u, (this.my - this.by) / this.u, 1));
      this.overlay(this.bx, this.by, bw, bh);
      Gx.pop();
      Gx.popAlpha();
   }

   private void boardPress(float gx, float gy, int button) {
      if (this.state == GameView.State.READY && this.clickToStart()) {
         this.start();
         Sound.click();
      } else {
         if (this.state == GameView.State.PLAYING) {
            this.onClick(gx, gy, button);
         }
      }
   }

   @Override
   public boolean mouseUp(float x, float y, int button) {
      if (this.dragStarted && this.state == GameView.State.PLAYING) {
         this.onRelease((x - this.bx) / this.u, (y - this.by) / this.u, button);
      }

      this.dragStarted = false;
      return super.mouseUp(x, y, button);
   }

   private void scoreBox(int x, int y, int w, int h, String label, String value, boolean main) {
      Gx.rect(x, y, w, h, this.p(8.0), main ? Gx.withAlpha(Style.accent, 0.14F) : Style.surface);
      Gx.textCenter(label, x + w / 2.0F, y + this.pf(9.0), this.pf(5.6F), 3, main ? Style.accent : Style.muted);
      Gx.textCenter(value, x + w / 2.0F, y + this.pf(21.5), this.pf(10.0), 3, Style.text);
   }

   private void overlay(int x, int y, int w, int h) {
      float since = (float)(System.currentTimeMillis() - this.stateAt) / 1000.0F;
      if (this.state != GameView.State.PLAYING) {
         float a = Ease.outCubic(Math.min(1.0F, since / 0.25F));
         Gx.pushAlpha(a);
         Gx.rect(x, y, w, h, this.p(12.0), Gx.rgba(Style.light ? 15987959 : 460811, 0.82F));
         int cx = x + w / 2;
         if (this.state == GameView.State.READY) {
            int cy = y + h / 2 - this.p(56.0);
            Gx.glow(cx, cy, this.p(60.0), Gx.withAlpha(this.info.color, 0.25F));
            Gx.icon(this.info.icon, cx, cy, this.pf(34.0), this.info.color);
            Gx.textCenter(this.info.name, cx, cy + this.pf(38.0), this.pf(15.0), 3, Style.text);
            List<String> lines = Fonts.wrap(this.info.howTo, 1, this.pf(8.2F), w - this.p(60.0), 4);
            float ly = cy + this.pf(58.0);

            for (String l : lines) {
               Gx.textCenter(l, cx, ly, this.pf(8.2F), 1, Style.sub);
               ly += this.pf(12.5);
            }

            float cfs = this.pf(7.4F);
            int cw = Math.round(Gx.width(this.info.controls, cfs, 2)) + this.p(34.0);
            int ch = this.p(22.0);
            int ccx = cx - cw / 2;
            int ccy = Math.round(ly + this.pf(6.0));
            Gx.rect(ccx, ccy, cw, ch, ch / 2, Style.surface2);
            Gx.icon("keys", ccx + this.p(14.0), ccy + ch / 2.0F, this.pf(10.0), Style.sub);
            Gx.textMid(this.info.controls, ccx + this.p(25.0), ccy + ch / 2.0F, cfs, 2, Style.sub);
            float pulse = 0.55F + 0.45F * (float)Math.sin(System.currentTimeMillis() / 350.0);
            Gx.textSpaced(
               this.clickToStart() ? "KLICK ODER LEERTASTE ZUM STARTEN" : "LEERTASTE ZUM STARTEN",
               cx,
               ccy + ch + this.pf(20.0),
               this.pf(7.4F),
               3,
               Gx.withAlpha(Style.accent, pulse),
               this.pf(1.4F)
            );
            if (this.hasAi()) {
               this.levels(cx, Math.round(ccy + ch + this.pf(36.0)));
            }
         } else if (this.state == GameView.State.PAUSED) {
            int cy = y + h / 2 - this.p(30.0);
            Gx.icon("pause", cx, cy, this.pf(30.0), Style.text);
            Gx.textCenter("Pause", cx, cy + this.pf(32.0), this.pf(15.0), 3, Style.text);
            this.buttons(cx, cy + this.p(54.0), "Weiter", "play", () -> {
               this.state = GameView.State.PLAYING;
               this.stateAt = System.currentTimeMillis();
            }, "Neustart", "reset", this::start);
         } else {
            int cy = y + h / 2 - this.p(52.0);
            if (this.record) {
               float s = 1.0F + 0.08F * (float)Math.sin(since * 6.0F);
               Gx.push();
               Gx.scaleAt(cx, cy, s);
               Gx.glow(cx, cy, this.p(60.0), 1442825533);
               Gx.icon("trophy", cx, cy, this.pf(34.0), -15043);
               Gx.pop();
            } else {
               Gx.icon("skull", cx, cy, this.pf(30.0), Style.sub);
            }

            Gx.textCenter(this.record ? "Neuer Rekord!" : this.overTitle(), cx, cy + this.pf(36.0), this.pf(15.0), 3, this.record ? -15043 : Style.text);
            Gx.textCenter(this.scoreLabel() + ": " + this.scoreText(), cx, cy + this.pf(56.0), this.pf(10.0), 2, Style.sub);
            this.buttons(cx, cy + this.p(78.0), "Nochmal", "reset", this::start, "Zurück", "back", this::back);
            if (this.hasAi()) {
               this.levels(cx, cy + this.p(120.0));
            }
         }

         Gx.popAlpha();
      }
   }

   private void levels(int cx, int y) {
      String[] l = GameInfo.LEVELS;
      float fs = this.pf(7.2F);
      int h = this.p(22.0);
      int gap = this.p(5.0);
      int padX = this.p(10.0);
      int[] ws = new int[l.length];
      int total = Math.round(Gx.width("KI:", fs, 3)) + this.p(8.0);

      for (int i = 0; i < l.length; i++) {
         ws[i] = Math.round(Gx.width(l[i], fs, 3)) + padX * 2;
         total += ws[i] + (i > 0 ? gap : 0);
      }

      int x = cx - total / 2;
      Gx.textMid("KI:", x, y + h / 2.0F, fs, 3, Style.sub);
      x += Math.round(Gx.width("KI:", fs, 3)) + this.p(8.0);
      int cur = this.aiLevel();

      for (int i = 0; i < l.length; i++) {
         boolean sel = i == cur;
         boolean hv = this.hover(x, y, ws[i], h);
         Gx.rect(x, y, ws[i], h, h / 2, sel ? Style.accent : (hv ? Style.surfaceHover : Style.surface2));
         Gx.textCenter(l[i], x + ws[i] / 2.0F, y + h / 2.0F, fs, 3, sel ? Style.accentText : Style.text);
         int lv = i;
         Kit.hit(x, y, ws[i], h).click(() -> {
            GameInfo.setDifficulty(this.info.id, lv);
            Sound.click();
         });
         x += ws[i] + gap;
      }
   }

   private void buttons(int cx, int y, String l1, String i1, Runnable a1, String l2, String i2, Runnable a2) {
      int bw = this.p(110.0);
      int bh = this.p(30.0);
      int gap = this.p(10.0);
      int x1 = cx - bw - gap / 2;
      int x2 = cx + gap / 2;
      boolean h1 = this.hover(x1, y, bw, bh);
      boolean h2 = this.hover(x2, y, bw, bh);
      Kit.button("g1" + l1, l1, i1, x1, y, bw, bh, this.P, true, h1);
      Kit.button("g2" + l2, l2, i2, x2, y, bw, bh, this.P, false, h2);
      Kit.hit(x1, y, bw, bh).click(a1);
      Kit.hit(x2, y, bw, bh).click(a2);
   }

   protected void back() {
      if (this.host != null) {
         this.host.open(new HubView(HubView.Section.GAMES));
      }
   }

   @Override
   public boolean key(int key, int mods) {
      if (key < 0) {
         if (this.state == GameView.State.PLAYING) {
            this.onKeyUp(-1 - key);
         }

         return true;
      } else if (key == 256) {
         if (this.state == GameView.State.PLAYING) {
            this.state = GameView.State.PAUSED;
            this.stateAt = System.currentTimeMillis();
         } else {
            this.back();
         }

         return true;
      } else if (key != 80 || this.state != GameView.State.PLAYING && this.state != GameView.State.PAUSED) {
         if (key != 82 || this.state != GameView.State.OVER && this.state != GameView.State.PAUSED) {
            if ((key == 32 || key == 257) && (this.state == GameView.State.READY || this.state == GameView.State.OVER)) {
               this.start();
               if (key == 32 && this.state == GameView.State.PLAYING) {
                  this.onKey(key);
               }

               return true;
            } else {
               if (this.state == GameView.State.PLAYING) {
                  this.onKey(key);
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
