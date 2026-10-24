package dev.lego.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class IntroView extends View {
   private static final String[][] FONT = new String[][]{
      {"L", "10000", "10000", "10000", "10000", "10000", "10000", "11111"},
      {"E", "11111", "10000", "10000", "11110", "10000", "10000", "11111"},
      {"G", "01111", "10000", "10000", "10011", "10001", "10001", "01111"},
      {"O", "01110", "10001", "10001", "10001", "10001", "10001", "01110"},
      {"C", "01111", "10000", "10000", "10000", "10000", "10000", "01111"},
      {"I", "11111", "00100", "00100", "00100", "00100", "00100", "11111"},
      {"N", "10001", "11001", "10101", "10011", "10001", "10001", "10001"},
      {"T", "11111", "00100", "00100", "00100", "00100", "00100", "00100"}
   };
   private static final int[] LEGO_COLS = new int[]{-1900533, -13053, -16749385, -11821238};
   private static final int[] LEGO_COLS_HW = new int[]{-38118, -7655482, -12916643, -3924450};
   private static final float BURST = 2.2F;
   private static final float SLAM = 5.6F;
   private static final float FADE = 8.6F;
   private static final float END = 9.4F;
   private static final double FINAL_YAW = 0.0;
   private static final double FINAL_PITCH = 22.0;
   private static final float ROW = 0.556F;
   private static final float SPRITE = 2.3F;
   private final List<IntroView.Brick> bricks = new ArrayList<>();
   private final List<float[]> sparks = new ArrayList<>();
   private long start = -1L;
   private int lastW = -1;
   private int lastH = -1;
   private int landed;
   private int beats;
   private boolean done;
   private boolean slammed;
   private boolean burst;
   private float logoTop;
   private float logoBottom;
   public static int created;
   public static int started;
   private final List<IntroView.Brick> order = new ArrayList<>();

   public IntroView() {
      BrickModel.prepare();
      created++;
   }

   @Override
   public boolean blur() {
      return false;
   }

   @Override
   public boolean backdrop() {
      return false;
   }

   @Override
   public float openMs() {
      return 1.0F;
   }

   @Override
   public float closeMs() {
      return 1.0F;
   }

   @Override
   public float designW() {
      return 1000.0F;
   }

   @Override
   public float designH() {
      return 600.0F;
   }

   private static String[] glyph(char c) {
      for (String[] g : FONT) {
         if (g[0].charAt(0) == c) {
            return g;
         }
      }

      return null;
   }

   private void build() {
      this.bricks.clear();
      Random rnd = new Random(11L);
      float c1 = Math.min(this.W * 0.6F / 23.0F, this.H * 0.36F / 7.784F);
      float c2 = c1 * 0.5F;
      float y1 = this.H * 0.13F;
      float y2 = y1 + 7.784F * c1 + c1 * 0.9F;
      this.logoTop = y1;
      this.addWord("LEGO", this.W / 2.0F, y1, c1, true, rnd);
      this.addWord("CLIENT", this.W / 2.0F, y2, c2, false, rnd);
      this.logoBottom = y2 + 7.784F * c2;
      float fs = c1 * 0.8F;
      float fy = this.logoBottom + c1 * 0.6F;
      int n = Math.round(this.W * 0.64F / fs);

      for (int r = 0; r < 2; r++) {
         for (int i = 0; i < n; i++) {
            IntroView.Brick b = new IntroView.Brick();
            b.tx = this.W / 2.0F - n * fs / 2.0F + i * fs + (r == 0 ? fs / 2.0F : 0.0F) + fs / 2.0F;
            b.ty = fy + (1 - r) * fs * 0.556F + fs * 0.556F / 2.0F;
            b.sx = b.tx;
            b.sy = this.H + fs * 4.0F;
            b.s = fs;
            b.row = 100 - r;
            b.color = Gx.mix(Halloween.on() ? -14019264 : -13023649, Halloween.on() ? -12969899 : -11180918, rnd.nextFloat());
            b.delay = 5.5F + Math.abs(i - n / 2.0F) / (n / 2.0F) * 0.9F + r * 0.2F;
            b.floor = true;
            this.bricks.add(b);
         }
      }
   }

   private void addWord(String word, float cx, float top, float cell, boolean big, Random rnd) {
      int cols = word.length() * 6 - 1;
      float left = cx - cols * cell / 2.0F;
      float rh = cell * 0.556F;

      for (int li = 0; li < word.length(); li++) {
         String[] g = glyph(word.charAt(li));
         if (g != null) {
            for (int r = 0; r < 7; r++) {
               for (int c = 0; c < 5; c++) {
                  if (g[1 + r].charAt(c) == '1') {
                     for (int half = 0; half < 2; half++) {
                        int row = r * 2 + half;
                        IntroView.Brick b = new IntroView.Brick();
                        b.tx = left + (li * 6 + c) * cell + cell / 2.0F;
                        b.ty = top + row * rh + rh / 2.0F;
                        b.row = row;
                        b.sx = this.W * (0.06F + 0.88F * rnd.nextFloat());
                        b.sy = this.H * (0.84F + 0.12F * rnd.nextFloat());
                        b.s = cell;
                        int[] pal = Halloween.on() ? LEGO_COLS_HW : LEGO_COLS;
                        b.color = Gx.mix(big ? pal[li % pal.length] : (Halloween.on() ? -2305816 : -854792), -16777216, rnd.nextFloat() * 0.08F);
                        float fromBottom = (13 - row) / 13.0F;
                        float order = Math.abs((b.tx - cx) / (cols * cell));
                        b.delay = 3.2F + (big ? 0.0F : 0.5F) + fromBottom * 1.5F + order * 0.35F + rnd.nextFloat() * 0.2F;
                        b.orbitA = (float)(rnd.nextFloat() * Math.PI * 2.0);
                        b.orbitR = 0.75F + 0.45F * rnd.nextFloat();
                        b.orbitH = (rnd.nextFloat() - 0.5F) * 0.5F;
                        b.spinY = (rnd.nextFloat() - 0.5F) * 900.0F;
                        b.spinP = (rnd.nextFloat() - 0.5F) * 500.0F;
                        b.yaw0 = rnd.nextFloat() * 360.0F;
                        b.pitch0 = 35.0F + (rnd.nextFloat() - 0.5F) * 30.0F;
                        this.bricks.add(b);
                     }
                  }
               }
            }
         }
      }
   }

   private static float easeOutBack(float x) {
      float c1 = 1.5F;
      float c3 = c1 + 1.0F;
      return 1.0F + c3 * (float)Math.pow(x - 1.0F, 3.0) + c1 * (float)Math.pow(x - 1.0F, 2.0);
   }

   private static float easeOutCubic(float x) {
      return 1.0F - (float)Math.pow(1.0F - x, 3.0);
   }

   private static float easeInOut(float x) {
      return x < 0.5F ? 4.0F * x * x * x : 1.0F - (float)Math.pow(-2.0F * x + 2.0F, 3.0) / 2.0F;
   }

   @Override
   public void draw() {
      Gx.fill(0, 0, this.W, this.H, -16777216);
      if (!Env.loading.getAsBoolean() && BrickModel.ready()) {
         if (BrickModel.upload(24)) {
            if (this.start < 0L) {
               this.start = System.currentTimeMillis();
               started++;
            }

            if (this.W != this.lastW || this.H != this.lastH) {
               this.lastW = this.W;
               this.lastH = this.H;
               this.build();
            }

            float t = (float)(System.currentTimeMillis() - this.start) / 1000.0F;
            if (t > 9.4F && !this.done) {
               this.done = true;
               this.host.close();
            } else {
               float fade = t < 0.6F ? t / 0.6F : (t > 8.6F ? Math.max(0.0F, 1.0F - (t - 8.6F) / 0.79999924F) : 1.0F);
               int beat = t < 2.2F ? (int)(t * (1.2F + t * 0.9F)) : this.beats;
               if (beat > this.beats && t < 2.1000001F) {
                  this.beats = beat;
                  Sound.play("minecraft:entity.warden.heartbeat", 0.9F + t * 0.15F, 0.55F);
               }

               if (!this.burst && t >= 2.2F) {
                  this.burst = true;
                  Sound.play("minecraft:entity.generic.explode", 0.6F, 0.5F);
                  Sound.play("minecraft:entity.firework_rocket.blast", 0.8F, 0.4F);
               }

               if (!this.slammed && t >= 5.6F) {
                  this.slammed = true;
                  Sound.play("minecraft:block.anvil.land", 0.55F, 0.55F);
                  Sound.play("minecraft:ui.toast.challenge_complete", 1.0F, 0.45F);
                  this.spawnSparks();
               }

               float shake = 0.0F;
               if (t > 0.8F && t < 2.2F) {
                  shake = (t - 0.8F) / 1.4000001F * 0.006F;
               }

               if (t >= 2.2F && t < 2.8000002F) {
                  shake = 0.014F * (1.0F - (t - 2.2F) / 0.6F);
               }

               if (t >= 5.6F && t < 6.4F) {
                  shake = 0.018F * (1.0F - (t - 5.6F) / 0.8F);
               }

               float camX = (float)Math.sin(t * 61.0F) * shake * this.W;
               float camY = (float)Math.cos(t * 47.0F) * shake * this.H;
               float zoom = t < 5.6F ? 1.25F - 0.17F * easeInOut(Math.min(1.0F, t / 5.6F)) : 1.08F - 0.08F * easeOutCubic(Math.min(1.0F, (t - 5.6F) / 3.0F));
               boolean hw = Halloween.on();
               Gx.gradientV(0, 0, this.W, this.H, Gx.withAlpha(hw ? -16120296 : -16051162, fade), Gx.withAlpha(hw ? -16580088 : -16645370, fade));
               float spot = t < 2.2F ? 0.5F + 0.5F * (float)Math.sin(t * 2.2) : 1.0F;
               int spotX = Math.round(this.W / 2.0F + (float)Math.sin(t * 1.3) * this.W * (t < 2.2F ? 0.3F : 0.05F));
               Gx.glow(
                  spotX,
                  Math.round(t < 2.2F ? this.H * 0.85F : this.H * 0.38F),
                  Math.round(Math.max(this.W, this.H) * (t < 2.2F ? 0.28F : 0.5F)),
                  Gx.withAlpha(hw ? -7655482 : -12948481, 0.16F * spot * fade)
               );
               Gx.glow(this.W / 2, Math.round(this.H * 0.95F), Math.round(Math.max(this.W, this.H) * 0.35F), Gx.withAlpha(hw ? -38118 : -1900533, 0.07F * fade));
               Gx.push();
               Gx.translate(camX, camY);
               Gx.scaleAt(this.W / 2.0F, this.H * 0.45F, zoom);
               int land = 0;
               float vx = this.W / 2.0F;
               float vy = this.H * 0.42F;

               for (IntroView.Brick b : this.bricks) {
                  if (b.floor) {
                     if (t < b.delay) {
                        b.scale = 0.0F;
                     } else {
                        float f = Math.min(1.0F, (t - b.delay) / 0.4F);
                        b.x = b.tx;
                        b.y = b.sy + (b.ty - b.sy) * easeOutCubic(f);
                        b.yaw = 0.0F;
                        b.pitch = 22.0F;
                        b.scale = 1.0F;
                        b.depth = 2.0F;
                        if (f >= 1.0F) {
                           land++;
                        }
                     }
                  } else if (t < 2.2F) {
                     float tr = Math.max(0.0F, (t - 0.8F) / 1.4000001F);
                     b.x = b.sx + (float)Math.sin(t * 40.0F + b.sx) * b.s * 0.08F * tr;
                     b.y = b.sy + (float)Math.cos(t * 37.0F + b.sy) * b.s * 0.1F * tr;
                     b.yaw = b.yaw0 + (float)Math.sin(t * 30.0F + b.sy) * 8.0F * tr;
                     b.pitch = b.pitch0;
                     b.scale = 1.0F;
                     b.depth = 0.0F;
                  } else {
                     float ot = t - 2.2F;
                     float a = b.orbitA + ot * (1.6F + 0.4F * b.orbitR);
                     float rr = b.orbitR * (1.0F - 0.25F * Math.min(1.0F, ot / 3.0F));
                     float oz = (float)Math.sin(a);
                     float ox = vx + (float)Math.cos(a) * this.W * 0.36F * rr;
                     float oy = vy + oz * this.H * 0.06F + b.orbitH * this.H * 0.5F;
                     float oscale = 0.75F + 0.35F * (oz + 1.0F) / 2.0F;
                     float up = Math.min(1.0F, ot / 0.9F);
                     float bx = b.sx + (ox - b.sx) * easeOutCubic(up);
                     float by = b.sy + (oy - b.sy) * easeOutCubic(up) - (float)Math.sin(up * Math.PI) * this.H * 0.25F;
                     float bs = 1.0F + (oscale - 1.0F) * up;
                     float f = Math.max(0.0F, Math.min(1.0F, (t - b.delay) / 0.85F));
                     float e = easeOutCubic(Math.min(1.0F, f / 0.8F));
                     float hoverY = b.ty - b.s * 0.556F * 1.6F;
                     b.x = bx + (b.tx - bx) * e;
                     b.y = by + (hoverY - by) * e;
                     if (f > 0.8F) {
                        float p = (f - 0.8F) / 0.2F;
                        b.y = hoverY + (b.ty - hoverY) * (p * p);
                     }

                     b.scale = bs + (1.0F - bs) * Math.min(1.0F, e);
                     float sp = 1.0F - Math.min(1.0F, f * 1.15F);
                     b.yaw = (float)(0.0 + (b.yaw0 + ot * b.spinY) * sp);
                     b.pitch = (float)(22.0 + (b.pitch0 - 22.0 + ot * b.spinP) * sp);
                     b.depth = f >= 1.0F ? 1.0F : oz * (1.0F - f);
                     if (f >= 1.0F) {
                        land++;
                        float since = t - b.delay - 0.85F;
                        if (since < 0.12F) {
                           b.y = b.y + (float)Math.sin(since / 0.12F * Math.PI) * b.s * 0.04F;
                        }
                     }
                  }
               }

               if (land > this.landed) {
                  if (land / 45 > this.landed / 45) {
                     Sound.play("minecraft:block.stone.place", 1.2F + land % 7 * 0.06F, 0.25F);
                  }

                  this.landed = land;
               }

               if (this.order.size() != this.bricks.size()) {
                  this.order.clear();
                  this.order.addAll(this.bricks);
               }

               this.order.sort((p, q) -> p.depth != q.depth ? Float.compare(p.depth, q.depth) : Float.compare(q.ty + q.row * 0.001F, p.ty + p.row * 0.001F));

               for (IntroView.Brick bxx : this.order) {
                  if (!(bxx.scale <= 0.0F)) {
                     float size = bxx.s * 2.3F * bxx.scale;
                     float dim = bxx.depth < 0.0F ? 0.55F + 0.45F * (1.0F + bxx.depth) : 1.0F;
                     int col = Gx.mix(-16777216, bxx.color, dim);
                     Gx.image(
                        BrickModel.sprite(bxx.yaw, bxx.pitch),
                        Math.round(bxx.x - size / 2.0F),
                        Math.round(bxx.y - size / 2.0F),
                        Math.round(size),
                        Math.round(size),
                        fade >= 1.0F ? col : Gx.withAlpha(col, fade)
                     );
                  }
               }

               if (t >= 5.6F) {
                  float s = t - 5.6F;
                  if (s < 0.4F) {
                     Gx.fill(-this.W, -this.H, this.W * 3, this.H * 3, Gx.withAlpha(-1, 0.6F * (1.0F - s / 0.4F) * fade));
                  }

                  int cy = Math.round((this.logoTop + this.logoBottom) / 2.0F);

                  for (int k = 0; k < 2; k++) {
                     float sk = s - k * 0.18F;
                     if (!(sk < 0.0F) && !(sk > 1.1F)) {
                        float rrx = Math.max(this.W, this.H) * 0.9F * easeOutCubic(sk / 1.1F);
                        int dot = Math.max(1, Math.round(Math.max(this.W, this.H) * 0.0028F * (1.0F - sk / 1.1F) + 1.0F));
                        int col = Gx.withAlpha(k == 0 ? -1 : -8407809, 0.55F * (1.0F - sk / 1.1F) * fade);

                        for (int d = 0; d < 90; d++) {
                           double ang = (Math.PI * 2) * d / 90.0;
                           Gx.circle(Math.round(this.W / 2.0F + (float)Math.cos(ang) * rrx), Math.round(cy + (float)Math.sin(ang) * rrx), dot, col);
                        }
                     }
                  }

                  float sweep = (s - 0.4F) / 1.4F;
                  if (sweep > 0.0F && sweep < 1.2F) {
                     Gx.glow(Math.round(this.W * (0.1F + 0.85F * sweep)), cy, Math.round(Math.max(this.W, this.H) * 0.15F), Gx.withAlpha(-1, 0.28F * fade));
                  }
               }

               for (float[] p : this.sparks) {
                  float age = t - p[4];
                  if (!(age > 1.6F)) {
                     float px = p[0] + p[2] * age;
                     float py = p[1] + p[3] * age + 0.5F * this.H * 1.5F * age * age;
                     int sparkCol = hw ? (age < 0.3F ? -2047745 : -38118) : (age < 0.3F ? -3136 : -20434);
                     Gx.circle(
                        Math.round(px),
                        Math.round(py),
                        Math.max(1, Math.round(Math.max(this.W, this.H) * 0.0025F * (1.0F - age / 1.6F) + 1.0F)),
                        Gx.withAlpha(sparkCol, (1.0F - age / 1.6F) * fade)
                     );
                  }
               }

               Gx.pop();
               if (t > 6.1F) {
                  float g = Math.min(1.0F, (t - 5.6F - 0.5F) / 0.6F) * fade;
                  float fs = Math.max(8.0F, this.H * 0.02F);
                  Gx.textSpaced("MINECRAFT 1.21.11", this.W / 2.0F, this.H * 0.9F, fs, 3, Gx.withAlpha(hw ? -3098400 : -4667680, g), fs * 0.35F);
               }

               if (t < 5.6F) {
                  float fs = Math.max(7.0F, this.H * 0.016F);
                  Gx.textCenter("Klick, Leertaste oder Esc = überspringen", this.W / 2.0F, this.H * 0.97F, fs, 2, Gx.withAlpha(1157627903, fade));
               }
            }
         }
      } else {
         if (this.start >= 0L) {
            this.start += 16L;
         }
      }
   }

   private void spawnSparks() {
      Random r = new Random(3L);
      float cy = (this.logoTop + this.logoBottom) / 2.0F;

      for (int i = 0; i < 90; i++) {
         float a = (float)(r.nextFloat() * Math.PI * 2.0);
         float sp = Math.max(this.W, this.H) * (0.25F + r.nextFloat() * 0.65F);
         this.sparks
            .add(
               new float[]{
                  this.W / 2.0F + (r.nextFloat() - 0.5F) * this.W * 0.5F,
                  cy + (r.nextFloat() - 0.5F) * this.H * 0.2F,
                  (float)Math.cos(a) * sp,
                  (float)Math.sin(a) * sp - this.H * 0.4F,
                  5.6F
               }
            );
      }
   }

   private void skip() {
      if (!this.done) {
         this.done = true;
         this.host.close();
      }
   }

   @Override
   public boolean mouseDown(float x, float y, int button, boolean dbl) {
      this.skip();
      return true;
   }

   @Override
   public boolean key(int key, int mods) {
      if (key != 32 && key != 256 && key != 257) {
         return false;
      } else {
         this.skip();
         return true;
      }
   }

   @Override
   public void onEscape() {
      this.skip();
   }

   private static final class Brick {
      float sx;
      float sy;
      float tx;
      float ty;
      float s;
      float delay;
      float orbitA;
      float orbitR;
      float orbitH;
      float spinY;
      float spinP;
      float yaw0;
      float pitch0;
      int row;
      int color;
      boolean floor;
      float x;
      float y;
      float scale;
      float yaw;
      float pitch;
      float depth;
   }
}
