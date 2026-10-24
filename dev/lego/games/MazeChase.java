package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;

public final class MazeChase extends GameView {
   private static final String[] MAP = new String[]{
      "###################",
      "#........#........#",
      "#o##.###.#.###.##o#",
      "#.................#",
      "#.##.#.#####.#.##.#",
      "#....#...#...#....#",
      "####.###.#.###.####",
      "####.#.......#.####",
      "####.#.##-##.#.####",
      "    ...#GGG#...    ",
      "####.#.#####.#.####",
      "####.#.......#.####",
      "####.#.#####.#.####",
      "#........#........#",
      "#.##.###.#.###.##.#",
      "#o.#.....P.....#.o#",
      "##.#.#.#####.#.#.##",
      "#....#...#...#....#",
      "#.######.#.######.#",
      "#.................#",
      "###################"
   };
   private static final int C = 19;
   private static final int R = 21;
   private static final float T = 13.0F;
   private static final float W = 380.0F;
   private static final float H = 290.0F;
   private static final float OX = 66.5F;
   private static final float OY = 8.5F;
   private static final int[] DX = new int[]{1, 0, -1, 0};
   private static final int[] DY = new int[]{0, 1, 0, -1};
   private static final int[] HAT = new int[]{-1890757, -12616705, -14703780, -5157377};
   private static final String[] NAMES = new String[]{"Frosti", "Flocke", "Eisbert", "Kufe"};
   private static final int HOUSE = 0;
   private static final int LEAVING = 1;
   private static final int NORMAL = 2;
   private static final int EATEN = 3;
   private static final int ENTERING = 4;
   private static final int[][] CORNER = new int[][]{{17, -1}, {1, -1}, {17, 21}, {1, 21}};
   private final char[][] grid = new char[21][19];
   private final MazeChase.Ent player = new MazeChase.Ent();
   private final MazeChase.Ent[] ghosts = new MazeChase.Ent[4];
   private final GameFx fx = new GameFx();
   private int pellets;
   private int eatenChain;
   private int lives;
   private int level;
   private int pelletsEaten;
   private float frightT;
   private float modeT;
   private float anim;
   private float deathT = -1.0F;
   private float clearT = -1.0F;
   private float readyT;
   private float bonusT;
   private float chomp;
   private float hintT;
   private boolean chase;
   private int modeIdx;
   private static final float[] MODES = new float[]{7.0F, 20.0F, 7.0F, 20.0F, 5.0F, 9999.0F};

   public MazeChase() {
      super("maze");
   }

   @Override
   protected float boardW() {
      return 380.0F;
   }

   @Override
   protected float boardH() {
      return 290.0F;
   }

   @Override
   protected void reset() {
      this.lives = 3;
      this.level = 1;
      this.fx.clear();
      this.hintT = 0.0F;
      this.loadLevel();
   }

   private void loadLevel() {
      this.pellets = 0;
      this.pelletsEaten = 0;

      for (int r = 0; r < 21; r++) {
         for (int c = 0; c < 19; c++) {
            char ch = MAP[r].charAt(c);
            this.grid[r][c] = ch;
            if (ch == '.' || ch == 'o') {
               this.pellets++;
            }
         }
      }

      this.bonusT = 0.0F;
      this.clearT = -1.0F;
      this.resetPositions();
   }

   private void resetPositions() {
      this.player.x = 9.0F;
      this.player.y = 15.0F;
      this.player.dir = 2;
      this.player.want = 2;
      this.player.moving = true;

      for (int i = 0; i < 4; i++) {
         MazeChase.Ent g = this.ghosts[i] == null ? (this.ghosts[i] = new MazeChase.Ent()) : this.ghosts[i];
         g.x = i == 0 ? 9.0F : 7 + i;
         g.y = i == 0 ? 7.0F : 9.0F;
         g.dir = 2;
         g.mode = i == 0 ? 2 : 0;
         g.houseT = i == 0 ? 0.0F : i * 3.2F / (1.0F + (this.level - 1) * 0.3F);
      }

      this.frightT = 0.0F;
      this.eatenChain = 0;
      this.modeT = 0.0F;
      this.modeIdx = 0;
      this.chase = false;
      this.deathT = -1.0F;
      this.readyT = 1.6F;
   }

   private boolean open(int c, int r, boolean door) {
      if (r < 0 || r >= 21) {
         return false;
      } else if (c >= 0 && c < 19) {
         char ch = this.grid[r][c];
         return ch != '#' && (ch != '-' || door) && (ch != 'G' || door);
      } else {
         return r == 9;
      }
   }

   @Override
   protected void onKey(int k) {
      int d = k == 262 || k == 68 ? 0 : (k == 264 || k == 83 ? 1 : (k != 263 && k != 65 ? (k != 265 && k != 87 ? -1 : 3) : 2));
      if (d >= 0) {
         this.player.want = d;
         if (d == (this.player.dir + 2) % 4) {
            this.player.dir = d;
            this.player.moving = true;
         }
      }
   }

   @Override
   protected void update(float dt) {
      this.fx.update(dt);
      this.hintT += dt;
      this.chomp += dt;
      if (this.readyT > 0.0F) {
         this.readyT -= dt;
      } else if (this.clearT >= 0.0F) {
         this.clearT += dt;
         if (this.clearT > 2.2F) {
            this.level++;
            this.loadLevel();
            this.fx.popup("Level " + this.level, 190.0F, 145.0F, -736942, 18.0F);
         }
      } else if (this.deathT >= 0.0F) {
         this.deathT += dt;
         if (this.deathT > 1.8F) {
            if (this.lives <= 0) {
               this.gameOver();
               return;
            }

            this.resetPositions();
         }
      } else {
         float lvl = Math.min(1.0F, (this.level - 1) / 6.0F);
         if (this.frightT > 0.0F) {
            this.frightT -= dt;
            if (this.frightT <= 0.0F) {
               this.eatenChain = 0;
            }
         } else {
            this.modeT += dt;
            if (this.modeT > MODES[this.modeIdx]) {
               this.modeT = 0.0F;
               this.modeIdx = Math.min(MODES.length - 1, this.modeIdx + 1);
               this.chase = this.modeIdx % 2 == 1;

               for (MazeChase.Ent g : this.ghosts) {
                  if (g.mode == 2) {
                     g.dir = (g.dir + 2) % 4;
                  }
               }
            }
         }

         float ps = (7.0F + lvl * 1.2F) * (this.frightT > 0.0F ? 1.1F : 1.0F);
         this.move(this.player, ps * dt, -1);
         int pc = Math.round(this.player.x);
         int pr = Math.round(this.player.y);
         if (pc >= 0 && pc < 19 && Math.abs(this.player.x - pc) < 0.3F && Math.abs(this.player.y - pr) < 0.3F) {
            char ch = this.grid[pr][pc];
            if (ch == '.') {
               this.grid[pr][pc] = ' ';
               this.score += 10L;
               this.pellets--;
               this.pelletsEaten++;
               if (this.pelletsEaten % 2 == 0) {
                  Sound.play("minecraft:block.note_block.hat", 1.6F + this.pelletsEaten % 4 * 0.1F, 0.12F);
               }

               this.chomp = 0.0F;
            } else if (ch == 'o') {
               this.grid[pr][pc] = ' ';
               this.score += 50L;
               this.pellets--;
               this.pelletsEaten++;
               this.frightT = Math.max(2.5F, 7.0F - (this.level - 1) * 0.8F);
               this.eatenChain = 0;

               for (MazeChase.Ent gx : this.ghosts) {
                  if (gx.mode == 2) {
                     gx.dir = (gx.dir + 2) % 4;
                  }
               }

               this.fx.ring(this.cx(pc), this.cy(pr), 40.0F, -45715, 0.5F);
               this.fx.burst(this.cx(pc), this.cy(pr), 16, -1, 90.0F, 2.0F, 0.5F, 0.0F, 2);
               this.fx.shake(0.2F);
               this.fx.popup("Zuckerstange!", this.cx(pc), this.cy(pr) - 10.0F, -33892, 9.0F);
               Sound.play("minecraft:block.amethyst_block.chime", 1.0F, 0.9F);
            }

            if (this.pelletsEaten == 70 || this.pelletsEaten == 150) {
               this.bonusT = 9.0F;
            }

            if (this.bonusT > 0.0F
               && pc == 9
               && pr == 15
               && this.pelletsEaten > 0
               && Math.abs(this.player.x - 9.0F) < 0.4F
               && Math.abs(this.player.y - 15.0F) < 0.4F) {
               int pts = 100 * this.level + 100;
               this.score += pts;
               this.bonusT = 0.0F;
               this.fx.confetti(this.cx(9.0F), this.cy(15.0F), 20, new int[]{-1890757, -736942, -14703780}, 120.0F, 3.0F);
               this.fx.popup("Geschenk! +" + pts, this.cx(9.0F), this.cy(15.0F) - 10.0F, -736942, 10.0F);
               Sound.play("minecraft:entity.player.levelup", 1.6F, 0.4F);
            }

            if (this.pellets <= 0) {
               this.clearT = 0.0F;
               Sound.play("minecraft:ui.toast.challenge_complete", 1.3F, 0.35F);
               this.fx.popup("Geschafft!", 190.0F, 135.0F, -4653233, 16.0F);
               return;
            }
         }

         this.bonusT = Math.max(0.0F, this.bonusT - dt);

         for (int i = 0; i < 4; i++) {
            MazeChase.Ent gxx = this.ghosts[i];
            switch (gxx.mode) {
               case 0:
                  gxx.houseT -= dt;
                  gxx.y = 9.0F + (float)Math.sin(this.anim * 5.0F + i) * 0.25F;
                  if (gxx.houseT <= 0.0F) {
                     gxx.mode = 1;
                     gxx.y = 9.0F;
                  }
                  break;
               case 1:
                  float sp = 3.5F * dt;
                  if (Math.abs(gxx.x - 9.0F) > 0.01F) {
                     gxx.x = gxx.x + Math.signum(9.0F - gxx.x) * Math.min(sp, Math.abs(9.0F - gxx.x));
                  } else {
                     gxx.y -= sp;
                     if (gxx.y <= 7.0F) {
                        gxx.y = 7.0F;
                        gxx.x = 9.0F;
                        gxx.mode = 2;
                        gxx.dir = 2;
                     }
                  }
                  break;
               case 2:
               case 3:
               default:
                  boolean fright = this.frightT > 0.0F && gxx.mode == 2;
                  float gs = gxx.mode == 3 ? 15.0F : (fright ? 4.2F : 6.4F + lvl * 1.4F);
                  if (Math.round(gxx.y) == 9 && (gxx.x < 4.0F || gxx.x > 14.0F) && gxx.mode != 3) {
                     gs *= 0.55F;
                  }

                  this.move(gxx, gs * dt, i);
                  if (gxx.mode == 3 && Math.abs(gxx.x - 9.0F) < 0.1F && Math.abs(gxx.y - 7.0F) < 0.1F) {
                     gxx.x = 9.0F;
                     gxx.y = 7.0F;
                     gxx.mode = 4;
                  }

                  if (gxx.mode == 2 && Math.abs(gxx.x - this.player.x) + Math.abs(gxx.y - this.player.y) < 0.8F) {
                     if (!fright) {
                        this.die();
                        return;
                     }

                     gxx.mode = 3;
                     this.eatenChain++;
                     int pts = 100 << Math.min(4, this.eatenChain);
                     this.score += pts;
                     this.fx.burst(this.cx(gxx.x), this.cy(gxx.y), 26, -1, 140.0F, 2.6F, 0.6F, 60.0F, 4);
                     this.fx.ring(this.cx(gxx.x), this.cy(gxx.y), 26.0F, -6301441, 0.4F);
                     this.fx.popup("+" + pts, this.cx(gxx.x), this.cy(gxx.y) - 8.0F, -6301441, 11.0F);
                     this.fx.shake(0.3F);
                     Sound.play("minecraft:block.snow.break", 1.2F, 0.9F);
                     Sound.play("minecraft:entity.experience_orb.pickup", 0.8F + this.eatenChain * 0.2F, 0.4F);
                  }
                  break;
               case 4:
                  gxx.y += 6.0F * dt;
                  if (gxx.y >= 9.0F) {
                     gxx.y = 9.0F;
                     gxx.mode = 1;
                  }
            }
         }
      }
   }

   private void die() {
      this.deathT = 0.0F;
      this.lives--;
      this.fx.burst(this.cx(this.player.x), this.cy(this.player.y), 30, -3638726, 120.0F, 3.0F, 0.9F, 80.0F, 1);
      this.fx.burst(this.cx(this.player.x), this.cy(this.player.y), 12, -1, 90.0F, 2.0F, 0.7F, 0.0F, 2);
      this.fx.shake(0.8F);
      this.fx.flash(-50354, 0.5F);
      Sound.play("minecraft:entity.player.hurt_freeze", 0.8F, 0.6F);
   }

   private void move(MazeChase.Ent e, float dist, int ghostIdx) {
      for (int guard = 0; dist > 1.0E-5F && guard < 10; guard++) {
         int cT = Math.round(e.x);
         int rT = Math.round(e.y);
         if (Math.abs(e.x - cT) < 0.001F && Math.abs(e.y - rT) < 0.001F) {
            e.x = cT;
            e.y = rT;
            if (ghostIdx < 0) {
               if (this.open(cT + DX[e.want], rT + DY[e.want], false)) {
                  e.dir = e.want;
               }

               if (!this.open(cT + DX[e.dir], rT + DY[e.dir], false)) {
                  e.moving = false;
                  return;
               }

               e.moving = true;
            } else {
               e.dir = this.chooseDir(e, cT, rT, ghostIdx);
            }
         }

         float pos = DX[e.dir] != 0 ? e.x : e.y;
         int sgn = DX[e.dir] != 0 ? DX[e.dir] : DY[e.dir];
         float target = sgn > 0 ? (float)Math.floor(pos + 0.001F) + 1.0F : (float)Math.ceil(pos - 0.001F) - 1.0F;
         float st = Math.min(dist, Math.abs(target - pos));
         e.x = e.x + DX[e.dir] * st;
         e.y = e.y + DY[e.dir] * st;
         dist -= st;
         if (e.x < -1.001F) {
            e.x += 20.0F;
         }

         if (e.x > 19.001F) {
            e.x -= 20.0F;
         }
      }
   }

   private int chooseDir(MazeChase.Ent g, int c, int r, int idx) {
      boolean fright = this.frightT > 0.0F && g.mode == 2;
      int tx;
      int ty;
      if (g.mode == 3) {
         tx = 9;
         ty = 7;
      } else if (!this.chase) {
         tx = CORNER[idx][0];
         ty = CORNER[idx][1];
      } else {
         int pc = Math.round(this.player.x);
         int pr = Math.round(this.player.y);
         switch (idx) {
            case 1:
               tx = pc + DX[this.player.dir] * 4;
               ty = pr + DY[this.player.dir] * 4;
               break;
            case 2:
               float d = (pc - c) * (pc - c) + (pr - r) * (pr - r);
               if (d > 64.0F) {
                  tx = pc;
                  ty = pr;
               } else {
                  tx = CORNER[2][0];
                  ty = CORNER[2][1];
               }
               break;
            case 3:
               int ax = pc + DX[this.player.dir] * 2;
               int ay = pr + DY[this.player.dir] * 2;
               tx = ax * 2 - Math.round(this.ghosts[0].x);
               ty = ay * 2 - Math.round(this.ghosts[0].y);
               break;
            default:
               tx = pc;
               ty = pr;
         }
      }

      int best = -1;
      float bd = Float.MAX_VALUE;
      int start = fright ? this.rnd.nextInt(4) : 0;

      for (int k = 0; k < 4; k++) {
         int d = fright ? (start + k) % 4 : new int[]{3, 2, 1, 0}[k];
         if (d != (g.dir + 2) % 4) {
            int nc = c + DX[d];
            int nr = r + DY[d];
            if (this.open(nc, nr, false)) {
               if (fright) {
                  return d;
               }

               float dd = (nc - tx) * (nc - tx) + (nr - ty) * (nr - ty);
               if (dd < bd) {
                  bd = dd;
                  best = d;
               }
            }
         }
      }

      if (best < 0) {
         best = (g.dir + 2) % 4;
      }

      return best;
   }

   private float cx(float c) {
      return 66.5F + c * 13.0F + 6.5F;
   }

   private float cy(float r) {
      return 8.5F + r * 13.0F + 6.5F;
   }

   @Override
   protected void render() {
      float dt = Math.min(0.05F, Gx.dt);
      this.anim += dt;
      this.boxV(0.0F, 0.0F, 380.0F, 290.0F, 0.0F, -16116944, -15457718);
      boolean flashWalls = this.clearT >= 0.0F && (int)(this.clearT * 6.0F) % 2 == 0 && this.clearT > 0.3F;
      GameFx.sprite(this, "mz_walls" + (flashWalls ? 1 : 0), 63.5F, 5.5F, 253.0F, 279.0F, 253.0F, 279.0F, gr -> paintWalls(gr, flashWalls), -1);
      Gx.push();
      Gx.translate(this.S(this.fx.sx), this.S(this.fx.sy));

      for (int r = 0; r < 21; r++) {
         for (int c = 0; c < 19; c++) {
            char ch = this.grid[r][c];
            if (ch == '.') {
               this.circle(this.cx(c), this.cy(r), 1.35F, -722689);
            } else if (ch == 'o') {
               float s = 1.0F + 0.12F * (float)Math.sin(this.anim * 6.0F + c);
               Gx.glow(this.X(this.cx(c)), this.Y(this.cy(r)), this.S(11.0F * s), 1442794861);
               GameFx.sprite(this, "mz_cane", this.cx(c) - 6.0F * s, this.cy(r) - 6.0F * s, 12.0F * s, 12.0F * s, 24.0F, 24.0F, MazeChase::paintCane, -1);
            }
         }
      }

      if (this.bonusT > 0.0F) {
         float s = 1.0F + 0.08F * (float)Math.sin(this.anim * 8.0F);
         Gx.glow(this.X(this.cx(9.0F)), this.Y(this.cy(15.0F)), this.S(12.0F), 1442103634);
         GameFx.sprite(
            this,
            "sj_gift0",
            this.cx(9.0F) - 6.5F * s,
            this.cy(15.0F) - 6.5F * s,
            13.0F * s,
            13.0F * s,
            40.0F,
            40.0F,
            gr -> SantaJump.paintGift(gr, 0),
            Gx.withAlpha(-1, Math.min(1.0F, this.bonusT))
         );
      }

      for (int i = 0; i < 4; i++) {
         this.ghost(this.ghosts[i], i);
      }

      this.playerSprite();
      this.fx.draw(this, 0.0F, 0.0F);
      Gx.pop();
      this.text("LEVEL", 33.25F, 24.0F, 6.0F, 3, -1711276033);
      this.text(String.valueOf(this.level), 33.25F, 38.0F, 13.0F, 3, -736942);
      this.text("LEBEN", 33.25F, 62.0F, 6.0F, 3, -1711276033);

      for (int i = 0; i < this.lives; i++) {
         GameFx.sprite(this, "mz_ginger0", 26.25F, 70 + i * 17, 14.0F, 14.0F, 30.0F, 30.0F, gr -> paintGinger(gr, 0), -1);
      }

      float rx = 346.75F;
      this.text("PERLEN", rx, 24.0F, 6.0F, 3, -1711276033);
      this.text(String.valueOf(this.pellets), rx, 38.0F, 11.0F, 3, -722689);
      if (this.frightT > 0.0F) {
         this.text("POWER", rx, 62.0F, 6.0F, 3, -1711276033);
         float f = this.frightT / Math.max(2.5F, 7.0F - (this.level - 1) * 0.8F);
         this.box(rx - 3.0F, 70.0F, 6.0F, 60.0F, 3.0F, 1157627903);
         this.box(rx - 3.0F, 70.0F + 60.0F * (1.0F - f), 6.0F, 60.0F * f, 3.0F, this.frightT < 2.0F && (int)(this.anim * 8.0F) % 2 == 0 ? -1 : -45715);
      }

      for (int i = 0; i < 4; i++) {
         float y = 220.0F + i * 15;
         this.circle(rx - 16.0F, y, 3.0F, HAT[i]);
         Gx.textMid(NAMES[i], this.X(rx - 10.0F), this.Y(y), 5.8F * this.u, 2, -1426063361);
      }

      if (this.readyT > 0.0F && this.state == GameView.State.PLAYING) {
         float a = Math.min(1.0F, this.readyT * 2.0F);
         GameFx.outlined(this, "Bereit?", this.cx(9.0F), this.cy(11.0F), 12.0F, Gx.withAlpha(-736942, a), Gx.withAlpha(-16777216, a * 0.8F));
      }

      if (this.state == GameView.State.PLAYING && this.hintT < 4.0F) {
         float a = Math.min(1.0F, 4.0F - this.hintT);
         this.text("Pfeiltasten / WASD", this.cx(9.0F), this.cy(13.0F) + 1.0F, 6.5F, 3, Gx.withAlpha(-1, a * 0.8F));
      }

      this.fx.drawPopups(this, 0.0F, 0.0F);
      this.fx.drawFlash(this, 380.0F, 290.0F);
   }

   private void ghost(MazeChase.Ent g, int i) {
      float x = this.cx(g.x);
      float y = this.cy(g.y) + (g.mode == 2 ? (float)Math.sin(this.anim * 8.0F + i) * 0.5F : 0.0F);
      float s = 17.0F;
      boolean fright = this.frightT > 0.0F && (g.mode == 2 || g.mode == 0 || g.mode == 1);
      if (g.mode == 3 || g.mode == 4) {
         int d = g.dir;
         GameFx.sprite(this, "mz_eyes" + d + i, x - s / 2.0F, y - s / 2.0F, s, s, 30.0F, 30.0F, gr -> paintEyes(gr, d, HAT[i]), -1);
      } else if (!fright) {
         int d = g.dir;
         Gx.glow(this.X(x), this.Y(y), this.S(12.0F), Gx.withAlpha(HAT[i], 0.18F));
         GameFx.sprite(this, "mz_snow" + i + d, x - s / 2.0F, y - s / 2.0F - 1.0F, s, s, 30.0F, 30.0F, gr -> paintSnowman(gr, HAT[i], d, false, false), -1);
      } else {
         boolean white = this.frightT < 2.0F && (int)(this.anim * 8.0F) % 2 == 0;
         GameFx.sprite(
            this, "mz_fright" + (white ? 1 : 0), x - s / 2.0F, y - s / 2.0F - 1.0F, s, s, 30.0F, 30.0F, gr -> paintSnowman(gr, -12616705, 0, true, white), -1
         );
      }
   }

   private void playerSprite() {
      float x = this.cx(this.player.x);
      float y = this.cy(this.player.y);
      float s = 16.0F;
      if (this.deathT >= 0.0F) {
         float t = Ease.clamp(this.deathT / 1.2F);
         if (!(t >= 1.0F)) {
            Gx.push();
            Gx.scaleAt(this.X(x), this.Y(y), 1.0F - t);
            GameFx.sprite(this, "mz_ginger1", x - s / 2.0F, y - s / 2.0F, s, s, 30.0F, 30.0F, gr -> paintGinger(gr, 1), Gx.withAlpha(-1, 1.0F - t));
            Gx.pop();
         }
      } else {
         int fr = this.player.moving && this.chomp < 0.12F ? 1 : 0;
         float bob = this.player.moving && this.state == GameView.State.PLAYING ? (float)Math.abs(Math.sin(this.anim * 14.0F)) * 1.2F : 0.0F;
         float lean = DX[this.player.dir] * 1.2F;
         if (this.frightT > 0.0F) {
            Gx.glow(this.X(x), this.Y(y), this.S(14.0F), 1157582189);
         }

         GameFx.sprite(this, "mz_ginger" + fr, x - s / 2.0F + lean, y - s / 2.0F - bob, s, s, 30.0F, 30.0F, gr -> paintGinger(gr, fr), -1);
      }
   }

   private static void paintWalls(Graphics2D g, boolean flash) {
      g.translate(3, 3);
      Area a = new Area();

      for (int r = 0; r < 21; r++) {
         for (int c = 0; c < 19; c++) {
            if (MAP[r].charAt(c) == '#') {
               a.add(new Area(new java.awt.geom.Rectangle2D.Float(c * 13.0F, r * 13.0F, 13.0F, 13.0F)));
            }
         }
      }

      g.setColor(flash ? new Color(3824288) : new Color(1452122));
      g.fill(a);
      g.setStroke(new BasicStroke(5.0F, 1, 1));
      g.setColor(new Color(159, 216, 255, flash ? 120 : 45));
      g.draw(a);
      g.setStroke(new BasicStroke(1.6F, 1, 1));
      g.setColor(flash ? Color.WHITE : new Color(10475775));
      g.draw(a);
      g.setColor(new Color(244, 248, 255, 215));

      for (int r = 0; r < 21; r++) {
         for (int cx = 0; cx < 19; cx++) {
            if (cap(r, cx)) {
               int e = cx;

               while (e + 1 < 19 && cap(r, e + 1)) {
                  e++;
               }

               g.fill(new java.awt.geom.RoundRectangle2D.Float(cx * 13.0F + 1.0F, r * 13.0F + 0.9F, (e - cx + 1) * 13.0F - 2.0F, 3.2F, 3.0F, 3.0F));
               cx = e;
            }
         }
      }

      g.setColor(new Color(16743381));
      g.fill(new java.awt.geom.Rectangle2D.Float(117.0F, 109.3F, 13.0F, 2.4F));
   }

   private static boolean cap(int r, int c) {
      return MAP[r].charAt(c) == '#' && (r == 0 || MAP[r - 1].charAt(c) != '#');
   }

   private static void paintCane(Graphics2D g) {
      g.setStroke(new BasicStroke(5.0F, 1, 1));
      GeneralPath p = new GeneralPath();
      p.moveTo(9.0F, 22.0F);
      p.lineTo(9.0F, 9.0F);
      p.curveTo(9.0F, 2.0F, 18.0F, 2.0F, 18.0F, 9.0F);
      g.setColor(Color.WHITE);
      g.draw(p);
      g.setColor(new Color(14886459));
      g.setStroke(new BasicStroke(5.0F, 0, 1, 1.0F, new float[]{2.6F, 2.6F}, 0.0F));
      g.draw(p);
   }

   private static void paintGinger(Graphics2D g, int frame) {
      Color dough = new Color(13138234);
      Color doughD = new Color(9326368);
      Color icing = Color.WHITE;
      Area body = GameFx.union(
         new java.awt.geom.Ellipse2D.Float(9.0F, 1.0F, 12.0F, 12.0F),
         new java.awt.geom.RoundRectangle2D.Float(10.0F, 11.0F, 10.0F, 11.0F, 5.0F, 5.0F),
         new java.awt.geom.RoundRectangle2D.Float(3.0F, 12.0F, 24.0F, 5.0F, 5.0F, 5.0F),
         new java.awt.geom.RoundRectangle2D.Float(9.0F, 18.0F, 5.0F, 11.0F, 5.0F, 5.0F),
         new java.awt.geom.RoundRectangle2D.Float(16.0F, 18.0F, 5.0F, 11.0F, 5.0F, 5.0F)
      );
      g.setPaint(new GradientPaint(0.0F, 0.0F, dough, 0.0F, 30.0F, doughD));
      g.fill(body);
      g.setColor(new Color(6960400));
      g.setStroke(new BasicStroke(0.8F));
      g.draw(body);
      g.setColor(icing);
      g.setStroke(new BasicStroke(1.1F, 1, 1));
      g.drawPolyline(new int[]{4, 5, 6, 7}, new int[]{14, 15, 14, 15}, 4);
      g.drawPolyline(new int[]{23, 24, 25, 26}, new int[]{14, 15, 14, 15}, 4);
      g.drawLine(10, 26, 13, 26);
      g.drawLine(17, 26, 20, 26);
      g.setColor(new Color(1776418));
      g.fill(new java.awt.geom.Ellipse2D.Float(11.6F, 4.5F, 2.2F, 2.4F));
      g.fill(new java.awt.geom.Ellipse2D.Float(16.2F, 4.5F, 2.2F, 2.4F));
      if (frame == 1) {
         g.setColor(new Color(5905940));
         g.fill(new java.awt.geom.Ellipse2D.Float(12.5F, 8.0F, 5.0F, 3.4F));
      } else {
         g.setColor(icing);
         g.drawArc(12, 7, 6, 3, 200, 140);
      }

      g.setColor(new Color(14886459));
      g.fill(new java.awt.geom.Ellipse2D.Float(13.8F, 13.0F, 2.6F, 2.6F));
      g.setColor(new Color(2073436));
      g.fill(new java.awt.geom.Ellipse2D.Float(13.8F, 17.0F, 2.6F, 2.6F));
   }

   private static void paintSnowman(Graphics2D g, int hat, int dir, boolean fright, boolean white) {
      Color snow = fright ? (white ? Color.WHITE : new Color(8366335)) : Color.WHITE;
      Color shade = fright ? (white ? new Color(13228786) : new Color(3102152)) : new Color(12176875);
      Area body = GameFx.union(new java.awt.geom.Ellipse2D.Float(5.0F, 12.0F, 20.0F, 16.0F), new java.awt.geom.Ellipse2D.Float(8.0F, 3.0F, 14.0F, 13.0F));
      if (fright) {
         body.add(new Area(new java.awt.geom.RoundRectangle2D.Float(4.0F, 22.0F, 22.0F, 7.0F, 6.0F, 6.0F)));
      }

      g.setPaint(new GradientPaint(0.0F, 4.0F, snow, 0.0F, 28.0F, shade));
      g.fill(body);
      if (fright) {
         g.setColor(shade);
         g.fill(new java.awt.geom.Ellipse2D.Float(6.0F, 26.0F, 4.0F, 4.0F));
         g.fill(new java.awt.geom.Ellipse2D.Float(14.0F, 27.0F, 3.0F, 3.0F));
         g.fill(new java.awt.geom.Ellipse2D.Float(21.0F, 26.0F, 4.0F, 4.0F));
         g.setColor(white ? new Color(14886459) : Color.WHITE);
         g.setStroke(new BasicStroke(1.2F, 1, 1));
         g.fill(new java.awt.geom.Ellipse2D.Float(11.0F, 8.0F, 2.6F, 2.6F));
         g.fill(new java.awt.geom.Ellipse2D.Float(17.0F, 8.0F, 2.6F, 2.6F));
         g.drawPolyline(new int[]{10, 12, 14, 16, 18, 20}, new int[]{14, 13, 14, 13, 14, 13}, 6);
      } else {
         g.setColor(new Color(hat));
         g.fill(new java.awt.geom.RoundRectangle2D.Float(7.5F, 13.5F, 15.0F, 3.4F, 3.0F, 3.0F));
         g.fill(new java.awt.geom.RoundRectangle2D.Float(17.0F, 15.0F, 3.4F, 7.0F, 2.0F, 2.0F));
         float ex = DX[dir] * 1.2F;
         float ey = DY[dir] * 1.0F;
         g.setColor(new Color(1776418));
         g.fill(new java.awt.geom.Ellipse2D.Float(11.0F + ex, 7.0F + ey, 2.8F, 2.8F));
         g.fill(new java.awt.geom.Ellipse2D.Float(16.2F + ex, 7.0F + ey, 2.8F, 2.8F));
         g.setStroke(new BasicStroke(1.0F, 1, 1));
         g.drawLine(10, 6, 13, 7);
         g.drawLine(20, 6, 17, 7);
         g.setColor(new Color(16747038));
         int nx = dir == 0 ? 1 : (dir == 2 ? -1 : 0);
         g.fillPolygon(new int[]{14, 16, 15 + nx * 6}, new int[]{10, 10, 12 + (dir == 1 ? 2 : 0)}, 3);
         g.setColor(new Color(2763312));
         g.fill(new java.awt.geom.Ellipse2D.Float(14.0F, 19.0F, 2.0F, 2.0F));
         g.fill(new java.awt.geom.Ellipse2D.Float(14.0F, 23.0F, 2.0F, 2.0F));
         g.setColor(new Color(1776418));
         g.fill(new java.awt.geom.RoundRectangle2D.Float(7.5F, 3.5F, 15.0F, 2.4F, 1.0F, 1.0F));
         g.fill(new java.awt.geom.RoundRectangle2D.Float(10.0F, -0.5F, 10.0F, 5.0F, 1.0F, 1.0F));
         g.setColor(new Color(hat));
         g.fill(new java.awt.geom.Rectangle2D.Float(10.0F, 2.4F, 10.0F, 1.6F));
      }
   }

   private static void paintEyes(Graphics2D g, int dir, int hat) {
      float ex = DX[dir] * 1.2F;
      float ey = DY[dir] * 1.0F;
      g.setColor(Color.WHITE);
      g.fill(new java.awt.geom.Ellipse2D.Float(9.0F, 8.0F, 6.0F, 6.0F));
      g.fill(new java.awt.geom.Ellipse2D.Float(15.5F, 8.0F, 6.0F, 6.0F));
      g.setColor(new Color(1776418));
      g.fill(new java.awt.geom.Ellipse2D.Float(10.5F + ex, 9.5F + ey, 3.0F, 3.0F));
      g.fill(new java.awt.geom.Ellipse2D.Float(17.0F + ex, 9.5F + ey, 3.0F, 3.0F));
      g.setColor(new Color(16747038));
      g.fillPolygon(new int[]{14, 16, 15}, new int[]{14, 14, 19}, 3);
      g.setColor(new Color(1776418));
      g.fill(new java.awt.geom.RoundRectangle2D.Float(10.0F, 1.0F, 10.0F, 5.0F, 1.0F, 1.0F));
      g.fill(new java.awt.geom.RoundRectangle2D.Float(7.5F, 5.0F, 15.0F, 2.0F, 1.0F, 1.0F));
      g.setColor(new Color(hat));
      g.fill(new java.awt.geom.Rectangle2D.Float(10.0F, 3.8F, 10.0F, 1.4F));
      g.setPaint(
         new RadialGradientPaint(
            new java.awt.geom.Point2D.Float(15.0F, 15.0F),
            10.0F,
            new float[]{0.0F, 1.0F},
            new Color[]{new Color(255, 255, 255, 40), new Color(255, 255, 255, 0)}
         )
      );
      g.fill(new java.awt.geom.Ellipse2D.Float(5.0F, 5.0F, 20.0F, 20.0F));
   }

   private static final class Ent {
      float x;
      float y;
      int dir = 2;
      int want = 2;
      int mode;
      float houseT;
      float speedMul = 1.0F;
      boolean moving = true;
   }
}
