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
      for (int var1 = 0; var1 < 4; var1++) {
         for (int var2 = 0; var2 < 8; var2++) {
            for (int var3 = 0; var3 < 12; var3++) {
               boolean var4 = true;
               if (var2 == 0 && (var3 < 2 || var3 > 9)) {
                  var4 = false;
               }

               if (var2 == 1 && (var3 < 1 || var3 > 10)) {
                  var4 = false;
               }

               if (var2 >= 5 && var3 >= 3 && var3 <= 8) {
                  var4 = false;
               }

               this.shields[var1][var2][var3] = var4;
            }
         }
      }
   }

   private float shieldX(int var1) {
      return 46 + var1 * 88 - 19.2F;
   }

   private void nextWave() {
      this.wave++;
      this.aliens.clear();

      for (int var1 = 0; var1 < 5.0F; var1++) {
         for (int var2 = 0; var2 < 9.0F; var2++) {
            AlienDefense.Alien var3 = new AlienDefense.Alien();
            var3.row = var1;
            var3.col = var2;
            var3.type = var1 == 0 ? 0 : (var1 < 3 ? 1 : 2);
            this.aliens.add(var3);
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
      int var1 = 0;

      for (AlienDefense.Alien var3 : this.aliens) {
         if (var3.alive) {
            var1++;
         }
      }

      return var1;
   }

   @Override
   protected void onKey(int var1) {
      if (var1 == 263 || var1 == 65) {
         this.left = true;
      }

      if (var1 == 262 || var1 == 68) {
         this.right = true;
      }

      if (var1 == 32 || var1 == 265 || var1 == 87) {
         this.firing = true;
      }
   }

   @Override
   protected void onKeyUp(int var1) {
      if (var1 == 263 || var1 == 65) {
         this.left = false;
      }

      if (var1 == 262 || var1 == 68) {
         this.right = false;
      }

      if (var1 == 32 || var1 == 265 || var1 == 87) {
         this.firing = false;
      }
   }

   @Override
   protected void update(float var1) {
      this.fx.update(var1);
      this.hintT += var1;
      if (this.bannerT >= 0.0F) {
         this.bannerT += var1;
         if (this.bannerT > 1.8F) {
            this.bannerT = -1.0F;
         }
      }

      this.fireT = Math.max(0.0F, this.fireT - var1);
      this.invul = Math.max(0.0F, this.invul - var1);
      this.tripleT = Math.max(0.0F, this.tripleT - var1);
      this.rapidT = Math.max(0.0F, this.rapidT - var1);
      if (this.dead) {
         this.deadT += var1;
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
         this.px = this.px + ((this.right ? 1 : 0) - (this.left ? 1 : 0)) * 175 * var1;
         this.px = Math.max(14.0F, Math.min(366.0F, this.px));
         if (this.firing && this.fireT <= 0.0F) {
            this.fire();
         }
      }

      int var2 = this.alive();
      float var3 = 14.0F + (45.0F - var2) * 1.25F + this.wave * 3.5F + (var2 <= 3 ? 40 : 0);
      this.fX = this.fX + this.fDir * var3 * var1;
      float var4 = 999.0F;
      float var5 = -999.0F;
      float var6 = -999.0F;

      for (AlienDefense.Alien var8 : this.aliens) {
         if (var8.alive) {
            var4 = Math.min(var4, this.fX + var8.col * 30.0F);
            var5 = Math.max(var5, this.fX + var8.col * 30.0F + 22.0F);
            var6 = Math.max(var6, this.fY + var8.row * 22.0F + 16.0F);
         }
      }

      if (this.fDir > 0.0F && var5 > 374.0F || this.fDir < 0.0F && var4 < 6.0F) {
         this.fDir = -this.fDir;
         this.fY += 8.0F;
      }

      this.stepT += var1 * (0.8F + var3 / 30.0F);
      if (this.stepT > 1.0F) {
         this.stepT = 0.0F;
         this.frame ^= 1;
         Sound.play("minecraft:block.note_block.bass", this.frame == 0 ? 0.6F : 0.55F, 0.18F);
      }

      for (AlienDefense.Alien var18 : this.aliens) {
         var18.hitT = Math.max(0.0F, var18.hitT - var1);
      }

      if (var6 > 226.0F) {
         for (AlienDefense.Alien var19 : this.aliens) {
            if (var19.alive) {
               this.eraseShield(this.fX + var19.col * 30.0F + 11.0F, this.fY + var19.row * 22.0F + 8.0F, 12.0F);
            }
         }
      }

      if (var6 >= 264.0F && !this.dead) {
         this.lives = 0;
         this.crash();
      }

      float var17 = 0.6F + this.wave * 0.2F + (45.0F - var2) * 0.02F;
      if (var2 > 0 && this.rnd.nextFloat() < var17 * var1) {
         ArrayList var20 = new ArrayList();

         for (int var9 = 0; var9 < 9.0F; var9++) {
            AlienDefense.Alien var10 = null;

            for (AlienDefense.Alien var12 : this.aliens) {
               if (var12.alive && var12.col == var9 && (var10 == null || var12.row > var10.row)) {
                  var10 = var12;
               }
            }

            if (var10 != null) {
               var20.add(var10);
            }
         }

         AlienDefense.Alien var24 = (AlienDefense.Alien)var20.get(this.rnd.nextInt(var20.size()));
         if (this.rnd.nextFloat() < 0.5F) {
            for (AlienDefense.Alien var32 : var20) {
               if (Math.abs(this.fX + var32.col * 30.0F + 11.0F - this.px) < 22.0F) {
                  var24 = var32;
               }
            }
         }

         AlienDefense.Shot var29 = new AlienDefense.Shot();
         var29.enemy = true;
         var29.x = this.fX + var24.col * 30.0F + 11.0F;
         var29.y = this.fY + var24.row * 22.0F + 16.0F;
         var29.vy = 120 + this.wave * 8;
         this.shots.add(var29);
      }

      this.ufoT -= var1;
      if (this.ufoX < -900.0F && this.ufoT <= 0.0F) {
         this.ufoDir = this.rnd.nextBoolean() ? 1.0F : -1.0F;
         this.ufoX = this.ufoDir > 0.0F ? -30.0F : 410.0F;
         this.ufoPts = 50 * (2 + this.rnd.nextInt(5));
         Sound.play("minecraft:block.beacon.ambient", 2.0F, 0.5F);
      }

      if (this.ufoX > -900.0F) {
         this.ufoX = this.ufoX + this.ufoDir * 70.0F * var1;
         if (this.ufoX < -40.0F || this.ufoX > 420.0F) {
            this.ufoX = -999.0F;
            this.ufoT = 16.0F + this.rnd.nextFloat() * 10.0F;
         }
      }

      for (int var21 = this.shots.size() - 1; var21 >= 0; var21--) {
         AlienDefense.Shot var25 = this.shots.get(var21);
         var25.x = var25.x + var25.vx * var1;
         var25.y = var25.y + var25.vy * var1;
         if (var25.y < 16.0F || var25.y > 304.0F || var25.x < -4.0F || var25.x > 384.0F) {
            this.shots.remove(var21);
         } else if (this.hitShield(var25.x, var25.y)) {
            this.shots.remove(var21);
            this.fx.burst(var25.x, var25.y, 5, var25.enemy ? -33835 : -8587265, 50.0F, 1.6F, 0.3F, 0.0F, 1);
         } else if (!var25.enemy) {
            boolean var30 = false;

            for (AlienDefense.Alien var35 : this.aliens) {
               if (var35.alive) {
                  float var13 = this.fX + var35.col * 30.0F;
                  float var14 = this.fY + var35.row * 22.0F;
                  if (var25.x > var13 && var25.x < var13 + 22.0F && var25.y > var14 && var25.y < var14 + 16.0F) {
                     this.kill(var35, var13 + 11.0F, var14 + 8.0F);
                     var30 = true;
                     break;
                  }
               }
            }

            if (!var30 && this.ufoX > -900.0F && Math.abs(var25.x - this.ufoX) < 16.0F && Math.abs(var25.y - 28.0F) < 7.0F) {
               this.score = this.score + this.ufoPts;
               this.fx.burst(this.ufoX, 28.0F, 30, -41816, 160.0F, 2.6F, 0.7F, 0.0F, 1);
               this.fx.ring(this.ufoX, 28.0F, 40.0F, -41816, 0.5F);
               this.fx.popup("Mutterschiff! +" + this.ufoPts, this.ufoX, 40.0F, -41816, 12.0F);
               this.fx.shake(0.4F);
               Sound.play("minecraft:entity.generic.explode", 1.3F, 0.4F);
               this.ufoX = -999.0F;
               this.ufoT = 16.0F + this.rnd.nextFloat() * 10.0F;
               var30 = true;
            }

            if (var30) {
               this.shots.remove(var21);
            }
         } else if (!this.dead && this.invul <= 0.0F && Math.abs(var25.x - this.px) < 12.0F && var25.y > 264.0F && var25.y < 280.0F) {
            this.shots.remove(var21);
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

      for (AlienDefense.Shot var26 : this.shots) {
         if (!var26.enemy) {
            for (AlienDefense.Shot var34 : this.shots) {
               if (var34.enemy && Math.abs(var26.x - var34.x) < 3.0F && Math.abs(var26.y - var34.y) < 6.0F) {
                  var26.y = -99.0F;
                  var34.y = 399.0F;
                  this.fx.burst(var26.x, (var26.y + var34.y) / 2.0F, 4, -1, 40.0F, 1.5F, 0.2F, 0.0F, 2);
               }
            }
         }
      }

      for (int var23 = this.powers.size() - 1; var23 >= 0; var23--) {
         AlienDefense.Power var27 = this.powers.get(var23);
         var27.t += var1;
         var27.y += 60.0F * var1;
         if (!this.dead && Math.abs(var27.x - this.px) < 16.0F && Math.abs(var27.y - 272.0F) < 10.0F) {
            this.powers.remove(var23);
            this.collect(var27);
         } else if (var27.y > 310.0F) {
            this.powers.remove(var23);
         }
      }

      if (var2 == 0) {
         this.nextWave();
      }
   }

   private void fire() {
      int var1 = 0;

      for (AlienDefense.Shot var3 : this.shots) {
         if (!var3.enemy) {
            var1++;
         }
      }

      boolean var6 = this.rapidT > 0.0F;
      boolean var7 = this.tripleT > 0.0F;
      if (var1 < (var6 ? 6 : (var7 ? 3 : 2))) {
         this.fireT = var6 ? 0.14F : 0.38F;

         for (int var4 = var7 ? -1 : 0; var4 <= (var7 ? 1 : 0); var4++) {
            AlienDefense.Shot var5 = new AlienDefense.Shot();
            var5.x = this.px + var4 * 4;
            var5.y = 262.0F;
            var5.vy = -340.0F;
            var5.vx = var4 * 60;
            this.shots.add(var5);
         }

         this.fx.add(this.px, 261.0F, 0.0F, 0.0F, 0.1F, 4.0F, -8587265, 2, 0.0F, 0.0F);
         Sound.play("minecraft:block.note_block.bit", 1.8F, 0.2F);
      }
   }

   private void kill(AlienDefense.Alien var1, float var2, float var3) {
      var1.alive = false;
      int var4 = TYPE_PTS[var1.row];
      this.score += var4;
      int var5 = TYPE_COL[var1.row];
      this.fx.burst(var2, var3, 16, var5, 120.0F, 2.4F, 0.55F, 60.0F, 1);
      this.fx.ring(var2, var3, 16.0F, var5, 0.3F);
      this.fx.popup("+" + var4, var2, var3 - 6.0F, var5, 8.0F);
      this.fx.shake(0.07F);
      Sound.play("minecraft:entity.generic.explode", 2.0F, 0.12F);
      if (this.rnd.nextFloat() < 0.07F) {
         AlienDefense.Power var6 = new AlienDefense.Power();
         var6.x = var2;
         var6.y = var3;
         float var7 = this.rnd.nextFloat();
         var6.kind = var7 < 0.35F ? 0 : (var7 < 0.7F ? 1 : (var7 < 0.9F ? 2 : 3));
         this.powers.add(var6);
      }
   }

   private void collect(AlienDefense.Power var1) {
      this.fx.popup(switch (var1.kind) {
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
      }, this.px, 248.0F, P_COL[var1.kind], 11.0F);
      this.fx.burst(this.px, 266.0F, 18, P_COL[var1.kind], 120.0F, 2.4F, 0.6F, 0.0F, 2);
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

   private boolean hitShield(float var1, float var2) {
      for (int var3 = 0; var3 < 4; var3++) {
         float var4 = this.shieldX(var3);
         int var5 = (int)Math.floor((var1 - var4) / 3.2F);
         int var6 = (int)Math.floor((var2 - 226.0F) / 3.2F);
         if (var5 >= 0 && var5 < 12 && var6 >= 0 && var6 < 8 && this.shields[var3][var6][var5]) {
            this.shields[var3][var6][var5] = false;

            for (int var7 = 0; var7 < 4; var7++) {
               int var8 = var6 + this.rnd.nextInt(3) - 1;
               int var9 = var5 + this.rnd.nextInt(3) - 1;
               if (var8 >= 0 && var8 < 8 && var9 >= 0 && var9 < 12) {
                  this.shields[var3][var8][var9] = false;
               }
            }

            return true;
         }
      }

      return false;
   }

   private void eraseShield(float var1, float var2, float var3) {
      for (int var4 = 0; var4 < 4; var4++) {
         float var5 = this.shieldX(var4);

         for (int var6 = 0; var6 < 8; var6++) {
            for (int var7 = 0; var7 < 12; var7++) {
               if (this.shields[var4][var6][var7] && Math.abs(var5 + var7 * 3.2F + 1.6F - var1) < var3 && Math.abs(226.0F + var6 * 3.2F + 1.6F - var2) < var3) {
                  this.shields[var4][var6][var7] = false;
               }
            }
         }
      }
   }

   @Override
   protected void render() {
      float var1 = Math.min(0.05F, Gx.dt);
      this.anim += var1;
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
            var1x -> paintMother(var1x, (int)(this.anim * 6.0F) % 2),
            -1
         );
      }

      for (AlienDefense.Alien var3 : this.aliens) {
         if (var3.alive) {
            float var4 = this.fX + var3.col * 30.0F;
            float var5 = this.fY + var3.row * 22.0F;
            int var6 = var3.type;
            int var7 = this.frame;
            int var8 = var3.row;
            float var9 = (float)Math.sin(this.anim * 3.0F + var3.col * 0.6F) * 0.8F;
            Gx.glow(this.X(var4 + 11.0F), this.Y(var5 + 8.0F), this.S(14.0F), Gx.withAlpha(TYPE_COL[var8], 0.14F));
            GameFx.sprite(
               this, "ad_alien" + var6 + var7 + var8, var4, var5 + var9, 22.0F, 16.0F, 22.0F, 16.0F, var3x -> paintAlien(var3x, var6, var7, TYPE_COL[var8]), -1
            );
         }
      }

      for (int var10 = 0; var10 < 4; var10++) {
         float var15 = this.shieldX(var10);

         for (int var20 = 0; var20 < 8; var20++) {
            for (int var24 = 0; var24 < 12; var24++) {
               if (this.shields[var10][var20][var24]) {
                  this.box(
                     var15 + var24 * 3.2F,
                     226.0F + var20 * 3.2F,
                     3.4F,
                     3.4F,
                     0.0F,
                     var20 != 0 && this.shields[var10][var20 - 1][var24] ? Gx.mix(-4201473, -8410920, var20 / 8.0F) : -722689
                  );
               }
            }
         }
      }

      for (AlienDefense.Shot var16 : this.shots) {
         if (var16.enemy) {
            float var21 = (int)(var16.y / 5.0F) % 2 == 0 ? -1.0F : 1.0F;
            Gx.glow(this.X(var16.x), this.Y(var16.y), this.S(6.0F), 1728011432);
            this.box(var16.x - 1.0F + var21 * 0.6F, var16.y - 5.0F, 2.0F, 3.0F, 0.5F, -25907);
            this.box(var16.x - 1.0F - var21 * 0.6F, var16.y - 2.0F, 2.0F, 3.0F, 0.5F, -25907);
         } else {
            Gx.glow(this.X(var16.x), this.Y(var16.y), this.S(6.0F), 1719465983);
            this.box(var16.x - 1.0F, var16.y - 5.0F, 2.0F, 8.0F, 1.0F, -1507841);
         }
      }

      for (AlienDefense.Power var17 : this.powers) {
         float var22 = 1.0F + 0.1F * (float)Math.sin(var17.t * 8.0F);
         Gx.glow(this.X(var17.x), this.Y(var17.y), this.S(14.0F), Gx.withAlpha(P_COL[var17.kind], 0.4F));
         this.circle(var17.x, var17.y, 7.0F * var22, -15723478);
         Gx.ringAt(this.X(var17.x), this.Y(var17.y), this.S(7.0F * var22), Math.max(1, this.S(1.2F)), P_COL[var17.kind]);
         Gx.icon(P_ICON[var17.kind], this.X(var17.x), this.Y(var17.y), 8.0F * this.u, P_COL[var17.kind]);
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

      for (int var13 = 0; var13 < this.lives; var13++) {
         GameFx.sprite(this, "ad_player", 8 + var13 * 17, 6.0F, 15.0F, 10.4F, 26.0F, 18.0F, AlienDefense::paintPlayer, -570425345);
      }

      GameFx.outlined(this, "Welle " + this.wave, 348.0F, 12.0F, 7.5F, -4653233, -1442840576);
      float var14 = 24.0F;
      if (this.tripleT > 0.0F) {
         this.bar(372.0F, var14, P_ICON[0], P_COL[0], this.tripleT / 9.0F);
         var14 += 12.0F;
      }

      if (this.rapidT > 0.0F) {
         this.bar(372.0F, var14, P_ICON[1], P_COL[1], this.rapidT / 9.0F);
      }

      if (this.bannerT >= 0.0F) {
         float var18 = this.bannerT / 1.8F;
         float var23 = Ease.outBack(Math.min(1.0F, this.bannerT / 0.35F));
         float var25 = var18 < 0.7F ? 1.0F : 1.0F - (var18 - 0.7F) / 0.3F;
         Gx.pushAlpha(var25);
         Gx.push();
         Gx.scaleAt(this.X(190.0F), this.Y(165.0F), var23);
         GameFx.outlined(this, "WELLE " + this.wave, 190.0F, 165.0F, 20.0F, -4653233, -871753216);
         Gx.pop();
         Gx.popAlpha();
      }

      if (this.state == GameView.State.PLAYING && this.hintT < 3.5F) {
         float var19 = Math.min(1.0F, 3.5F - this.hintT) * (0.6F + 0.4F * (float)Math.sin(this.anim * 5.0F));
         this.text("← → bewegen  •  Leertaste schießen", 190.0F, 198.00002F, 7.6F, 3, Gx.withAlpha(-1, var19));
      }

      this.fx.drawPopups(this, 0.0F, 0.0F);
      this.fx.drawFlash(this, 380.0F, 300.0F);
   }

   private void bar(float var1, float var2, String var3, int var4, float var5) {
      float var6 = 48.0F;
      this.box(var1 - var6, var2 - 5.0F, var6, 10.0F, 5.0F, 1711276032);
      Gx.icon(var3, this.X(var1 - var6 + 6.0F), this.Y(var2), 6.5F * this.u, var4);
      this.box(var1 - var6 + 12.0F, var2 - 1.2F, var6 - 17.0F, 2.4F, 1.2F, 872415231);
      this.box(var1 - var6 + 12.0F, var2 - 1.2F, (var6 - 17.0F) * var5, 2.4F, 1.2F, var4);
   }

   private static void paintAlien(Graphics2D var0, int var1, int var2, int var3) {
      String[] var4 = ART[var1][var2];
      Color var5 = new Color(Gx.mix(var3, -1, 0.35F));
      Color var6 = new Color(Gx.mix(var3, -16777216, 0.25F));
      float var7 = 2.0F;

      for (int var8 = 0; var8 < var4.length; var8++) {
         for (int var9 = 0; var9 < var4[var8].length(); var9++) {
            char var10 = var4[var8].charAt(var9);
            if (var10 != '.') {
               float var11 = var9 * var7;
               float var12 = var8 * var7;
               if (var10 == 'X') {
                  var0.setPaint(new GradientPaint(0.0F, 0.0F, var5, 0.0F, 16.0F, var6));
               } else if (var10 == 'W') {
                  var0.setColor(Color.WHITE);
               } else {
                  var0.setColor(new Color(1183774));
               }

               var0.fill(new Float(var11, var12, var7 + 0.15F, var7 + 0.15F, 0.8F, 0.8F));
            }
         }
      }
   }

   private static void paintPlayer(Graphics2D var0) {
      var0.setPaint(new GradientPaint(0.0F, 8.0F, new Color(10483711), 0.0F, 18.0F, new Color(2002864)));
      GeneralPath var1 = new GeneralPath();
      var1.moveTo(1.0F, 17.0F);
      var1.lineTo(3.0F, 9.0F);
      var1.lineTo(23.0F, 9.0F);
      var1.lineTo(25.0F, 17.0F);
      var1.closePath();
      var0.fill(var1);
      var0.setColor(new Color(937574));
      var0.setStroke(new BasicStroke(0.7F));
      var0.draw(var1);
      var0.setPaint(new GradientPaint(0.0F, 3.0F, new Color(16777215), 0.0F, 10.0F, new Color(8189951)));
      var0.fill(new Float(10.0F, 2.0F, 6.0F, 9.0F, 2.0F, 2.0F));
      var0.setColor(new Color(16777215));
      var0.fill(new Float(11.5F, 0.0F, 3.0F, 3.0F, 1.0F, 1.0F));
      var0.setColor(new Color(12581887));

      for (int var2 = 0; var2 < 3; var2++) {
         var0.fill(new Float(4.5F + var2 * 6.8F + (var2 == 1 ? 20 : 0) * 0, 7.2F, 3.4F, 2.0F, 1.0F, 1.0F));
      }

      var0.setColor(new Color(255, 80, 120));
      var0.fill(new java.awt.geom.Ellipse2D.Float(5.0F, 12.0F, 2.4F, 2.4F));
      var0.fill(new java.awt.geom.Ellipse2D.Float(18.6F, 12.0F, 2.4F, 2.4F));
   }

   private static void paintMother(Graphics2D var0, int var1) {
      GeneralPath var2 = new GeneralPath();
      var2.moveTo(1.0F, 10.0F);
      var2.curveTo(4.0F, 5.0F, 30.0F, 5.0F, 33.0F, 10.0F);
      var2.curveTo(30.0F, 14.0F, 4.0F, 14.0F, 1.0F, 10.0F);
      var2.closePath();
      var0.setPaint(new GradientPaint(0.0F, 5.0F, new Color(16751309), 0.0F, 14.0F, new Color(10494042)));
      var0.fill(var2);
      var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(15269375), 0.0F, 7.0F, new Color(8189951)));
      var0.fill(new java.awt.geom.Ellipse2D.Float(11.0F, 1.0F, 12.0F, 9.0F));

      for (int var3 = 0; var3 < 5; var3++) {
         var0.setColor((var3 + var1) % 2 == 0 ? new Color(16773754) : new Color(6091519));
         var0.fill(new java.awt.geom.Ellipse2D.Float(5.0F + var3 * 5.5F, 9.3F, 2.2F, 2.2F));
      }
   }

   private static void paintBg(Graphics2D var0) {
      var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(328718), 0.0F, 300.0F, new Color(1380402)));
      var0.fillRect(0, 0, 380, 300);
      Random var1 = new Random(8L);

      for (int var2 = 0; var2 < 180; var2++) {
         float var3 = 0.4F + var1.nextFloat() * 1.1F;
         var0.setColor(new Color(255, 255, 255, 30 + var1.nextInt(140)));
         var0.fill(new java.awt.geom.Ellipse2D.Float(var1.nextFloat() * 380.0F, var1.nextFloat() * 280.0F, var3, var3));
      }

      var0.setPaint(
         new RadialGradientPaint(
            new java.awt.geom.Point2D.Float(300.0F, 90.0F),
            120.0F,
            new float[]{0.0F, 1.0F},
            new Color[]{new Color(180, 60, 140, 50), new Color(180, 60, 140, 0)}
         )
      );
      var0.fillRect(0, 0, 380, 300);
      var0.setPaint(new GradientPaint(0.0F, 284.0F, new Color(2767470), 0.0F, 300.0F, new Color(1186362)));
      var0.fill(new java.awt.geom.Ellipse2D.Float(-200.0F, 284.0F, 780.0F, 200.0F));
      var0.setColor(new Color(200, 225, 255, 160));
      var0.setStroke(new BasicStroke(1.2F));
      var0.draw(new java.awt.geom.Ellipse2D.Float(-200.0F, 284.0F, 780.0F, 200.0F));
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
