package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D.Float;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class AlienDefense extends GameView {
   private static final float W = 380.0F;
   private static final float H = 300.0F;
   private static final float PY = 272.0F;
   private static final float COLS = 9.0F;
   private static final float ROWS = 5.0F;
   private static final float SX = 30.0F;
   private static final float SY = 22.0F;
   private static final float AW = 22.0F;
   private static final float AH = 16.0F;
   private static final int[] TYPE_COL = new int[]{-41816, -10685697, -10685697, -4653233, -4653233};
   private static final int[] TYPE_PTS = new int[]{30, 20, 20, 10, 10};
   private static final int P_TRIPLE = 0;
   private static final int P_RAPID = 1;
   private static final int P_SHIELD = 2;
   private static final int P_LIFE = 3;
   private static final String[] P_ICON = new String[]{"sparkle", "bolt", "shield", "heart-fill"};
   private static final int[] P_COL = new int[]{-20434, -3462, -10685697, -45715};
   private static final String[][][] ART = new String[][][]{
      {
            {"...XXXXX...", ".XXXXXXXXX.", "XXWOXXXWOXX", "XXXXXXXXXXX", "XXXXXXXXXXX", ".X.X.X.X.X.", "X.X.X.X.X.X", "..........."},
            {"...XXXXX...", ".XXXXXXXXX.", "XXWOXXXWOXX", "XXXXXXXXXXX", "XXXXXXXXXXX", "X.X.X.X.X.X", ".X.X.X.X.X.", "..........."}
      },
      {
            {"....XXX....", "..XXXXXXX..", ".XXWWWWWXX.", ".XWWWOOWWX.", ".XXWWWWWXX.", "..XXXXXXX..", ".X..X.X..X.", "X..X...X..X"},
            {"....XXX....", "..XXXXXXX..", ".XXWWWWWXX.", ".XWWOOWWWX.", ".XXWWWWWXX.", "..XXXXXXX..", "..X.X.X.X..", ".X.X...X.X."}
      },
      {
            {".....X.....", "....XXX....", "...XXXXX...", "..XWOXWOX..", ".XXXXXXXXX.", "XX.XXXXX.XX", "X...X.X...X", "...X...X..."},
            {".....X.....", "....XXX....", "...XXXXX...", "..XOWXOWX..", ".XXXXXXXXX.", "XX.XXXXX.XX", ".X..X.X..X.", "X...X.X...X"}
      }
   };
   private final List<AlienDefense.Alien> aliens = new ArrayList<>();
   private final List<AlienDefense.Shot> shots = new ArrayList<>();
   private final List<AlienDefense.Power> powers = new ArrayList<>();
   private final GameFx fx = new GameFx();
   private final boolean[][][] shields = new boolean[4][8][12];
   private static final float CELL = 3.2F;
   private static final float SHY = 226.0F;
   private float fX;
   private float fY;
   private float fDir;
   private float stepT;
   private float px;
   private float fireT;
   private float deadT;
   private float invul;
   private float ufoX = -999.0F;
   private float ufoT;
   private float ufoDir;
   private float bannerT = -1.0F;
   private float anim;
   private float tripleT;
   private float rapidT;
   private float hintT;
   private boolean left;
   private boolean right;
   private boolean firing;
   private boolean dead;
   private boolean bubble;
   private int lives;
   private int wave;
   private int frame;
   private int ufoPts;

   public AlienDefense() {
      super("invaders");
   }

   @Override
   protected float boardW() {
      return 380.0F;
   }

   @Override
   protected float boardH() {
      return 300.0F;
   }

   @Override
   protected void reset() {
      this.shots.clear();
      this.powers.clear();
      this.fx.clear();
      this.lives = 3;
      this.wave = 0;
      this.px = 190.0F;
      this.dead = false;
      this.invul = 0.0F;
      this.bubble = false;
      this.tripleT = this.rapidT = 0.0F;
      this.hintT = 0.0F;
      this.left = this.right = this.firing = false;
      this.buildShields();
      this.nextWave();
   }

   private void buildShields() {
      for (int s = 0; s < 4; s++) {
         for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 12; c++) {
               boolean on = true;
               if (r == 0 && (c < 2 || c > 9)) {
                  on = false;
               }

               if (r == 1 && (c < 1 || c > 10)) {
                  on = false;
               }

               if (r >= 5 && c >= 3 && c <= 8) {
                  on = false;
               }

               this.shields[s][r][c] = on;
            }
         }
      }
   }

   private float shieldX(int s) {
      return 46 + s * 88 - 19.2F;
   }

   private void nextWave() {
      this.wave++;
      this.aliens.clear();

      for (int r = 0; r < 5.0F; r++) {
         for (int c = 0; c < 9.0F; c++) {
            AlienDefense.Alien a = new AlienDefense.Alien();
            a.row = r;
            a.col = c;
            a.type = r == 0 ? 0 : (r < 3 ? 1 : 2);
            this.aliens.add(a);
         }
      }

      this.fX = 30.0F;
      this.fY = 44 + Math.min(40, (this.wave - 1) * 8);
      this.fDir = 1.0F;
      this.ufoT = 14.0F + this.rnd.nextFloat() * 8.0F;
      this.bannerT = 0.0F;
      if (this.wave > 1) {
         Sound.play("minecraft:entity.player.levelup", 0.9F, 0.4F);
      }
   }

   private int alive() {
      int n = 0;

      for (AlienDefense.Alien a : this.aliens) {
         if (a.alive) {
            n++;
         }
      }

      return n;
   }

   @Override
   protected void onKey(int k) {
      if (k == 263 || k == 65) {
         this.left = true;
      }

      if (k == 262 || k == 68) {
         this.right = true;
      }

      if (k == 32 || k == 265 || k == 87) {
         this.firing = true;
      }
   }

   @Override
   protected void onKeyUp(int k) {
      if (k == 263 || k == 65) {
         this.left = false;
      }

      if (k == 262 || k == 68) {
         this.right = false;
      }

      if (k == 32 || k == 265 || k == 87) {
         this.firing = false;
      }
   }

   @Override
   protected void update(float dt) {
      this.fx.update(dt);
      this.hintT += dt;
      if (this.bannerT >= 0.0F) {
         this.bannerT += dt;
         if (this.bannerT > 1.8F) {
            this.bannerT = -1.0F;
         }
      }

      this.fireT = Math.max(0.0F, this.fireT - dt);
      this.invul = Math.max(0.0F, this.invul - dt);
      this.tripleT = Math.max(0.0F, this.tripleT - dt);
      this.rapidT = Math.max(0.0F, this.rapidT - dt);
      if (this.dead) {
         this.deadT += dt;
         if (this.deadT > 1.4F) {
            if (this.lives <= 0) {
               this.gameOver();
               return;
            }

            this.dead = false;
            this.invul = 1.6F;
            this.px = 190.0F;
         }
      } else {
         this.px = this.px + ((this.right ? 1 : 0) - (this.left ? 1 : 0)) * 175 * dt;
         this.px = Math.max(14.0F, Math.min(366.0F, this.px));
         if (this.firing && this.fireT <= 0.0F) {
            this.fire();
         }
      }

      int n = this.alive();
      float speed = 14.0F + (45.0F - n) * 1.25F + this.wave * 3.5F + (n <= 3 ? 40 : 0);
      this.fX = this.fX + this.fDir * speed * dt;
      float minX = 999.0F;
      float maxX = -999.0F;
      float maxY = -999.0F;

      for (AlienDefense.Alien a : this.aliens) {
         if (a.alive) {
            minX = Math.min(minX, this.fX + a.col * 30.0F);
            maxX = Math.max(maxX, this.fX + a.col * 30.0F + 22.0F);
            maxY = Math.max(maxY, this.fY + a.row * 22.0F + 16.0F);
         }
      }

      if (this.fDir > 0.0F && maxX > 374.0F || this.fDir < 0.0F && minX < 6.0F) {
         this.fDir = -this.fDir;
         this.fY += 8.0F;
      }

      this.stepT += dt * (0.8F + speed / 30.0F);
      if (this.stepT > 1.0F) {
         this.stepT = 0.0F;
         this.frame ^= 1;
         Sound.play("minecraft:block.note_block.bass", this.frame == 0 ? 0.6F : 0.55F, 0.18F);
      }

      for (AlienDefense.Alien ax : this.aliens) {
         ax.hitT = Math.max(0.0F, ax.hitT - dt);
      }

      if (maxY > 226.0F) {
         for (AlienDefense.Alien ax : this.aliens) {
            if (ax.alive) {
               this.eraseShield(this.fX + ax.col * 30.0F + 11.0F, this.fY + ax.row * 22.0F + 8.0F, 12.0F);
            }
         }
      }

      if (maxY >= 264.0F && !this.dead) {
         this.lives = 0;
         this.crash();
      }

      float rate = 0.6F + this.wave * 0.2F + (45.0F - n) * 0.02F;
      if (n > 0 && this.rnd.nextFloat() < rate * dt) {
         List<AlienDefense.Alien> bottom = new ArrayList<>();

         for (int c = 0; c < 9.0F; c++) {
            AlienDefense.Alien low = null;

            for (AlienDefense.Alien axx : this.aliens) {
               if (axx.alive && axx.col == c && (low == null || axx.row > low.row)) {
                  low = axx;
               }
            }

            if (low != null) {
               bottom.add(low);
            }
         }

         AlienDefense.Alien sh = bottom.get(this.rnd.nextInt(bottom.size()));
         if (this.rnd.nextFloat() < 0.5F) {
            for (AlienDefense.Alien axxx : bottom) {
               if (Math.abs(this.fX + axxx.col * 30.0F + 11.0F - this.px) < 22.0F) {
                  sh = axxx;
               }
            }
         }

         AlienDefense.Shot s = new AlienDefense.Shot();
         s.enemy = true;
         s.x = this.fX + sh.col * 30.0F + 11.0F;
         s.y = this.fY + sh.row * 22.0F + 16.0F;
         s.vy = 120 + this.wave * 8;
         this.shots.add(s);
      }

      this.ufoT -= dt;
      if (this.ufoX < -900.0F && this.ufoT <= 0.0F) {
         this.ufoDir = this.rnd.nextBoolean() ? 1.0F : -1.0F;
         this.ufoX = this.ufoDir > 0.0F ? -30.0F : 410.0F;
         this.ufoPts = 50 * (2 + this.rnd.nextInt(5));
         Sound.play("minecraft:block.beacon.ambient", 2.0F, 0.5F);
      }

      if (this.ufoX > -900.0F) {
         this.ufoX = this.ufoX + this.ufoDir * 70.0F * dt;
         if (this.ufoX < -40.0F || this.ufoX > 420.0F) {
            this.ufoX = -999.0F;
            this.ufoT = 16.0F + this.rnd.nextFloat() * 10.0F;
         }
      }

      for (int i = this.shots.size() - 1; i >= 0; i--) {
         AlienDefense.Shot s = this.shots.get(i);
         s.x = s.x + s.vx * dt;
         s.y = s.y + s.vy * dt;
         if (s.y < 16.0F || s.y > 304.0F || s.x < -4.0F || s.x > 384.0F) {
            this.shots.remove(i);
         } else if (this.hitShield(s.x, s.y)) {
            this.shots.remove(i);
            this.fx.burst(s.x, s.y, 5, s.enemy ? -33835 : -8587265, 50.0F, 1.6F, 0.3F, 0.0F, 1);
         } else if (!s.enemy) {
            boolean used = false;

            for (AlienDefense.Alien axxxx : this.aliens) {
               if (axxxx.alive) {
                  float axxxxx = this.fX + axxxx.col * 30.0F;
                  float ay = this.fY + axxxx.row * 22.0F;
                  if (s.x > axxxxx && s.x < axxxxx + 22.0F && s.y > ay && s.y < ay + 16.0F) {
                     this.kill(axxxx, axxxxx + 11.0F, ay + 8.0F);
                     used = true;
                     break;
                  }
               }
            }

            if (!used && this.ufoX > -900.0F && Math.abs(s.x - this.ufoX) < 16.0F && Math.abs(s.y - 28.0F) < 7.0F) {
               this.score = this.score + this.ufoPts;
               this.fx.burst(this.ufoX, 28.0F, 30, -41816, 160.0F, 2.6F, 0.7F, 0.0F, 1);
               this.fx.ring(this.ufoX, 28.0F, 40.0F, -41816, 0.5F);
               this.fx.popup("Mutterschiff! +" + this.ufoPts, this.ufoX, 40.0F, -41816, 12.0F);
               this.fx.shake(0.4F);
               Sound.play("minecraft:entity.generic.explode", 1.3F, 0.4F);
               this.ufoX = -999.0F;
               this.ufoT = 16.0F + this.rnd.nextFloat() * 10.0F;
               used = true;
            }

            if (used) {
               this.shots.remove(i);
            }
         } else if (!this.dead && this.invul <= 0.0F && Math.abs(s.x - this.px) < 12.0F && s.y > 264.0F && s.y < 280.0F) {
            this.shots.remove(i);
            if (this.bubble) {
               this.bubble = false;
               this.invul = 0.8F;
               this.fx.ring(this.px, 272.0F, 30.0F, -10685697, 0.4F);
               this.fx.popup("Schild!", this.px, 252.0F, -10685697, 9.0F);
               Sound.play("minecraft:item.shield.block", 1.2F, 0.5F);
            } else {
               this.crash();
            }
         }
      }

      for (AlienDefense.Shot axxxxx : this.shots) {
         if (!axxxxx.enemy) {
            for (AlienDefense.Shot b : this.shots) {
               if (b.enemy && Math.abs(axxxxx.x - b.x) < 3.0F && Math.abs(axxxxx.y - b.y) < 6.0F) {
                  axxxxx.y = -99.0F;
                  b.y = 399.0F;
                  this.fx.burst(axxxxx.x, (axxxxx.y + b.y) / 2.0F, 4, -1, 40.0F, 1.5F, 0.2F, 0.0F, 2);
               }
            }
         }
      }

      for (int ix = this.powers.size() - 1; ix >= 0; ix--) {
         AlienDefense.Power p = this.powers.get(ix);
         p.t += dt;
         p.y += 60.0F * dt;
         if (!this.dead && Math.abs(p.x - this.px) < 16.0F && Math.abs(p.y - 272.0F) < 10.0F) {
            this.powers.remove(ix);
            this.collect(p);
         } else if (p.y > 310.0F) {
            this.powers.remove(ix);
         }
      }

      if (n == 0) {
         this.nextWave();
      }
   }

   private void fire() {
      int mine = 0;

      for (AlienDefense.Shot s : this.shots) {
         if (!s.enemy) {
            mine++;
         }
      }

      boolean rapid = this.rapidT > 0.0F;
      boolean triple = this.tripleT > 0.0F;
      if (mine < (rapid ? 6 : (triple ? 3 : 2))) {
         this.fireT = rapid ? 0.14F : 0.38F;

         for (int k = triple ? -1 : 0; k <= (triple ? 1 : 0); k++) {
            AlienDefense.Shot sx = new AlienDefense.Shot();
            sx.x = this.px + k * 4;
            sx.y = 262.0F;
            sx.vy = -340.0F;
            sx.vx = k * 60;
            this.shots.add(sx);
         }

         this.fx.add(this.px, 261.0F, 0.0F, 0.0F, 0.1F, 4.0F, -8587265, 2, 0.0F, 0.0F);
         Sound.play("minecraft:block.note_block.bit", 1.8F, 0.2F);
      }
   }

   private void kill(AlienDefense.Alien a, float cx, float cy) {
      a.alive = false;
      int pts = TYPE_PTS[a.row];
      this.score += pts;
      int col = TYPE_COL[a.row];
      this.fx.burst(cx, cy, 16, col, 120.0F, 2.4F, 0.55F, 60.0F, 1);
      this.fx.ring(cx, cy, 16.0F, col, 0.3F);
      this.fx.popup("+" + pts, cx, cy - 6.0F, col, 8.0F);
      this.fx.shake(0.07F);
      Sound.play("minecraft:entity.generic.explode", 2.0F, 0.12F);
      if (this.rnd.nextFloat() < 0.07F) {
         AlienDefense.Power p = new AlienDefense.Power();
         p.x = cx;
         p.y = cy;
         float r = this.rnd.nextFloat();
         p.kind = r < 0.35F ? 0 : (r < 0.7F ? 1 : (r < 0.9F ? 2 : 3));
         this.powers.add(p);
      }
   }

   private void collect(AlienDefense.Power p) {
      this.fx.popup(switch (p.kind) {
         case 0 -> {
            this.tripleT = 9.0F;
            yield "Dreifachschuss!";
         }
         case 1 -> {
            this.rapidT = 9.0F;
            yield "Schnellfeuer!";
         }
         case 2 -> {
            this.bubble = true;
            yield "Schutzschild!";
         }
         default -> {
            this.lives = Math.min(5, this.lives + 1);
            yield "+1 Leben!";
         }
      }, this.px, 248.0F, P_COL[p.kind], 11.0F);
      this.fx.burst(this.px, 266.0F, 18, P_COL[p.kind], 120.0F, 2.4F, 0.6F, 0.0F, 2);
      Sound.play("minecraft:block.amethyst_block.chime", 1.3F, 0.8F);
   }

   private void crash() {
      if (!this.dead) {
         this.dead = true;
         this.deadT = 0.0F;
         this.lives = Math.max(0, this.lives - 1);
         this.fx.burst(this.px, 272.0F, 36, -8587265, 170.0F, 3.0F, 0.9F, 80.0F, 1);
         this.fx.burst(this.px, 272.0F, 20, -19641, 110.0F, 2.6F, 0.7F, 0.0F, 2);
         this.fx.ring(this.px, 272.0F, 44.0F, -8587265, 0.5F);
         this.fx.shake(0.85F);
         this.fx.flash(-50354, 0.6F);
         this.tripleT = this.rapidT = 0.0F;
         Sound.play("minecraft:entity.generic.explode", 0.8F, 0.5F);
      }
   }

   private boolean hitShield(float x, float y) {
      for (int s = 0; s < 4; s++) {
         float sx = this.shieldX(s);
         int c = (int)Math.floor((x - sx) / 3.2F);
         int r = (int)Math.floor((y - 226.0F) / 3.2F);
         if (c >= 0 && c < 12 && r >= 0 && r < 8 && this.shields[s][r][c]) {
            this.shields[s][r][c] = false;

            for (int k = 0; k < 4; k++) {
               int rr = r + this.rnd.nextInt(3) - 1;
               int cc = c + this.rnd.nextInt(3) - 1;
               if (rr >= 0 && rr < 8 && cc >= 0 && cc < 12) {
                  this.shields[s][rr][cc] = false;
               }
            }

            return true;
         }
      }

      return false;
   }

   private void eraseShield(float x, float y, float rad) {
      for (int s = 0; s < 4; s++) {
         float sx = this.shieldX(s);

         for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 12; c++) {
               if (this.shields[s][r][c] && Math.abs(sx + c * 3.2F + 1.6F - x) < rad && Math.abs(226.0F + r * 3.2F + 1.6F - y) < rad) {
                  this.shields[s][r][c] = false;
               }
            }
         }
      }
   }

   @Override
   protected void render() {
      float dt = Math.min(0.05F, Gx.dt);
      this.anim += dt;
      GameFx.sprite(this, "ad_bg", 0.0F, 0.0F, 380.0F, 300.0F, 380.0F, 300.0F, AlienDefense::paintBg, -1);
      Gx.push();
      Gx.translate(this.S(this.fx.sx), this.S(this.fx.sy));
      if (this.ufoX > -900.0F) {
         Gx.glow(this.X(this.ufoX), this.Y(28.0F), this.S(26.0F), 1157586088);
         GameFx.sprite(
            this,
            "ad_mother" + (int)(this.anim * 6.0F) % 2,
            this.ufoX - 17.0F,
            20.0F,
            34.0F,
            16.0F,
            34.0F,
            16.0F,
            gr -> paintMother(gr, (int)(this.anim * 6.0F) % 2),
            -1
         );
      }

      for (AlienDefense.Alien a : this.aliens) {
         if (a.alive) {
            float ax = this.fX + a.col * 30.0F;
            float ay = this.fY + a.row * 22.0F;
            int t = a.type;
            int fr = this.frame;
            int row = a.row;
            float bob = (float)Math.sin(this.anim * 3.0F + a.col * 0.6F) * 0.8F;
            Gx.glow(this.X(ax + 11.0F), this.Y(ay + 8.0F), this.S(14.0F), Gx.withAlpha(TYPE_COL[row], 0.14F));
            GameFx.sprite(this, "ad_alien" + t + fr + row, ax, ay + bob, 22.0F, 16.0F, 22.0F, 16.0F, gr -> paintAlien(gr, t, fr, TYPE_COL[row]), -1);
         }
      }

      for (int s = 0; s < 4; s++) {
         float sx = this.shieldX(s);

         for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 12; c++) {
               if (this.shields[s][r][c]) {
                  this.box(
                     sx + c * 3.2F, 226.0F + r * 3.2F, 3.4F, 3.4F, 0.0F, r != 0 && this.shields[s][r - 1][c] ? Gx.mix(-4201473, -8410920, r / 8.0F) : -722689
                  );
               }
            }
         }
      }

      for (AlienDefense.Shot s : this.shots) {
         if (s.enemy) {
            float zig = (int)(s.y / 5.0F) % 2 == 0 ? -1.0F : 1.0F;
            Gx.glow(this.X(s.x), this.Y(s.y), this.S(6.0F), 1728011432);
            this.box(s.x - 1.0F + zig * 0.6F, s.y - 5.0F, 2.0F, 3.0F, 0.5F, -25907);
            this.box(s.x - 1.0F - zig * 0.6F, s.y - 2.0F, 2.0F, 3.0F, 0.5F, -25907);
         } else {
            Gx.glow(this.X(s.x), this.Y(s.y), this.S(6.0F), 1719465983);
            this.box(s.x - 1.0F, s.y - 5.0F, 2.0F, 8.0F, 1.0F, -1507841);
         }
      }

      for (AlienDefense.Power p : this.powers) {
         float sx = 1.0F + 0.1F * (float)Math.sin(p.t * 8.0F);
         Gx.glow(this.X(p.x), this.Y(p.y), this.S(14.0F), Gx.withAlpha(P_COL[p.kind], 0.4F));
         this.circle(p.x, p.y, 7.0F * sx, -15723478);
         Gx.ringAt(this.X(p.x), this.Y(p.y), this.S(7.0F * sx), Math.max(1, this.S(1.2F)), P_COL[p.kind]);
         Gx.icon(P_ICON[p.kind], this.X(p.x), this.Y(p.y), 8.0F * this.u, P_COL[p.kind]);
      }

      if (!this.dead && (this.invul <= 0.0F || (int)(this.anim * 12.0F) % 2 == 0)) {
         if (this.bubble) {
            Gx.glow(this.X(this.px), this.Y(272.0F), this.S(22.0F), 861729535);
            Gx.ringAt(this.X(this.px), this.Y(271.0F), this.S(15.0F), Math.max(1, this.S(1.0F)), -2007174401);
         }

         if (this.tripleT > 0.0F || this.rapidT > 0.0F) {
            Gx.glow(this.X(this.px), this.Y(272.0F), this.S(18.0F), Gx.withAlpha(this.tripleT > 0.0F ? -20434 : -3462, 0.3F));
         }

         GameFx.sprite(this, "ad_player", this.px - 13.0F, 261.0F, 26.0F, 18.0F, 26.0F, 18.0F, AlienDefense::paintPlayer, -1);
      }

      this.fx.draw(this, 0.0F, 0.0F);
      Gx.pop();

      for (int i = 0; i < this.lives; i++) {
         GameFx.sprite(this, "ad_player", 8 + i * 17, 6.0F, 15.0F, 10.4F, 26.0F, 18.0F, AlienDefense::paintPlayer, -570425345);
      }

      GameFx.outlined(this, "Welle " + this.wave, 348.0F, 12.0F, 7.5F, -4653233, -1442840576);
      float py = 24.0F;
      if (this.tripleT > 0.0F) {
         this.bar(372.0F, py, P_ICON[0], P_COL[0], this.tripleT / 9.0F);
         py += 12.0F;
      }

      if (this.rapidT > 0.0F) {
         this.bar(372.0F, py, P_ICON[1], P_COL[1], this.rapidT / 9.0F);
      }

      if (this.bannerT >= 0.0F) {
         float t = this.bannerT / 1.8F;
         float sx = Ease.outBack(Math.min(1.0F, this.bannerT / 0.35F));
         float ax = t < 0.7F ? 1.0F : 1.0F - (t - 0.7F) / 0.3F;
         Gx.pushAlpha(ax);
         Gx.push();
         Gx.scaleAt(this.X(190.0F), this.Y(165.0F), sx);
         GameFx.outlined(this, "WELLE " + this.wave, 190.0F, 165.0F, 20.0F, -4653233, -871753216);
         Gx.pop();
         Gx.popAlpha();
      }

      if (this.state == GameView.State.PLAYING && this.hintT < 3.5F) {
         float ax = Math.min(1.0F, 3.5F - this.hintT) * (0.6F + 0.4F * (float)Math.sin(this.anim * 5.0F));
         this.text("← → bewegen  •  Leertaste schießen", 190.0F, 198.00002F, 7.6F, 3, Gx.withAlpha(-1, ax));
      }

      this.fx.drawPopups(this, 0.0F, 0.0F);
      this.fx.drawFlash(this, 380.0F, 300.0F);
   }

   private void bar(float right, float y, String icon, int col, float frac) {
      float w = 48.0F;
      this.box(right - w, y - 5.0F, w, 10.0F, 5.0F, 1711276032);
      Gx.icon(icon, this.X(right - w + 6.0F), this.Y(y), 6.5F * this.u, col);
      this.box(right - w + 12.0F, y - 1.2F, w - 17.0F, 2.4F, 1.2F, 872415231);
      this.box(right - w + 12.0F, y - 1.2F, (w - 17.0F) * frac, 2.4F, 1.2F, col);
   }

   private static void paintAlien(Graphics2D g, int type, int frame, int argb) {
      String[] rows = ART[type][frame];
      Color top = new Color(Gx.mix(argb, -1, 0.35F));
      Color bot = new Color(Gx.mix(argb, -16777216, 0.25F));
      float p = 2.0F;

      for (int r = 0; r < rows.length; r++) {
         for (int c = 0; c < rows[r].length(); c++) {
            char ch = rows[r].charAt(c);
            if (ch != '.') {
               float x = c * p;
               float y = r * p;
               if (ch == 'X') {
                  g.setPaint(new GradientPaint(0.0F, 0.0F, top, 0.0F, 16.0F, bot));
               } else if (ch == 'W') {
                  g.setColor(Color.WHITE);
               } else {
                  g.setColor(new Color(1183774));
               }

               g.fill(new Float(x, y, p + 0.15F, p + 0.15F, 0.8F, 0.8F));
            }
         }
      }
   }

   private static void paintPlayer(Graphics2D g) {
      g.setPaint(new GradientPaint(0.0F, 8.0F, new Color(10483711), 0.0F, 18.0F, new Color(2002864)));
      GeneralPath base = new GeneralPath();
      base.moveTo(1.0F, 17.0F);
      base.lineTo(3.0F, 9.0F);
      base.lineTo(23.0F, 9.0F);
      base.lineTo(25.0F, 17.0F);
      base.closePath();
      g.fill(base);
      g.setColor(new Color(937574));
      g.setStroke(new BasicStroke(0.7F));
      g.draw(base);
      g.setPaint(new GradientPaint(0.0F, 3.0F, new Color(16777215), 0.0F, 10.0F, new Color(8189951)));
      g.fill(new Float(10.0F, 2.0F, 6.0F, 9.0F, 2.0F, 2.0F));
      g.setColor(new Color(16777215));
      g.fill(new Float(11.5F, 0.0F, 3.0F, 3.0F, 1.0F, 1.0F));
      g.setColor(new Color(12581887));

      for (int i = 0; i < 3; i++) {
         g.fill(new Float(4.5F + i * 6.8F + (i == 1 ? 20 : 0) * 0, 7.2F, 3.4F, 2.0F, 1.0F, 1.0F));
      }

      g.setColor(new Color(255, 80, 120));
      g.fill(new java.awt.geom.Ellipse2D.Float(5.0F, 12.0F, 2.4F, 2.4F));
      g.fill(new java.awt.geom.Ellipse2D.Float(18.6F, 12.0F, 2.4F, 2.4F));
   }

   private static void paintMother(Graphics2D g, int frame) {
      GeneralPath body = new GeneralPath();
      body.moveTo(1.0F, 10.0F);
      body.curveTo(4.0F, 5.0F, 30.0F, 5.0F, 33.0F, 10.0F);
      body.curveTo(30.0F, 14.0F, 4.0F, 14.0F, 1.0F, 10.0F);
      body.closePath();
      g.setPaint(new GradientPaint(0.0F, 5.0F, new Color(16751309), 0.0F, 14.0F, new Color(10494042)));
      g.fill(body);
      g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(15269375), 0.0F, 7.0F, new Color(8189951)));
      g.fill(new java.awt.geom.Ellipse2D.Float(11.0F, 1.0F, 12.0F, 9.0F));

      for (int i = 0; i < 5; i++) {
         g.setColor((i + frame) % 2 == 0 ? new Color(16773754) : new Color(6091519));
         g.fill(new java.awt.geom.Ellipse2D.Float(5.0F + i * 5.5F, 9.3F, 2.2F, 2.2F));
      }
   }

   private static void paintBg(Graphics2D g) {
      g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(328718), 0.0F, 300.0F, new Color(1380402)));
      g.fillRect(0, 0, 380, 300);
      Random r = new Random(8L);

      for (int i = 0; i < 180; i++) {
         float s = 0.4F + r.nextFloat() * 1.1F;
         g.setColor(new Color(255, 255, 255, 30 + r.nextInt(140)));
         g.fill(new java.awt.geom.Ellipse2D.Float(r.nextFloat() * 380.0F, r.nextFloat() * 280.0F, s, s));
      }

      g.setPaint(
         new RadialGradientPaint(
            new java.awt.geom.Point2D.Float(300.0F, 90.0F),
            120.0F,
            new float[]{0.0F, 1.0F},
            new Color[]{new Color(180, 60, 140, 50), new Color(180, 60, 140, 0)}
         )
      );
      g.fillRect(0, 0, 380, 300);
      g.setPaint(new GradientPaint(0.0F, 284.0F, new Color(2767470), 0.0F, 300.0F, new Color(1186362)));
      g.fill(new java.awt.geom.Ellipse2D.Float(-200.0F, 284.0F, 780.0F, 200.0F));
      g.setColor(new Color(200, 225, 255, 160));
      g.setStroke(new BasicStroke(1.2F));
      g.draw(new java.awt.geom.Ellipse2D.Float(-200.0F, 284.0F, 780.0F, 200.0F));
   }

   private static final class Alien {
      int row;
      int col;
      int type;
      boolean alive = true;
      float hitT;
   }

   private static final class Power {
      float x;
      float y;
      float t;
      int kind;
   }

   private static final class Shot {
      float x;
      float y;
      float vx;
      float vy;
      boolean enemy;
   }
}
