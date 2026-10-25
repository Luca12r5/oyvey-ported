package dev.lego.cosmetic;

import dev.lego.LegoClient;
import dev.lego.ui.Tx;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

/**
 * Moving capes drawn in code (own art): each one is a function of position and time that fills
 * the 64x32 cape layout at 4x (256x128). The texture is repainted 12 times a second while the
 * cape is in use, so nothing is stored as frames.
 */
public final class MotionCapes {
   public static final String[][] LIST = {
      {"lx_galaxy", "Galaxie-Strudel"},
      {"lx_lava", "Lava-Fluss"},
      {"lx_aurora", "Polarlicht"},
      {"lx_lightning", "Gewitter"},
      {"lx_matrix", "Matrix-Regen"},
      {"lx_ocean", "Ozean-Wellen"},
      {"lx_plasma", "Plasma"},
      {"lx_rainbow", "Regenbogen-Welle"},
      {"lx_neon", "Neon-Gitter"},
      {"lx_stars", "Sternenflug"},
      {"lx_diamond", "Diamant-Funkeln"},
      {"lx_ender", "Ender-Nebel"},
      {"lx_fire", "Feuer"},
      {"lx_ice", "Eiskristall"},
      {"lx_pulse", "Herzschlag"},
      {"lx_bricks", "Stein-Regen"}
   };
   private static final int S = 4;
   private static final int W = 64 * S;
   private static final int H = 32 * S;
   private static final long FRAME_MS = 83L;
   private static final Map<String, Slot> SLOTS = new HashMap<>();

   private MotionCapes() {
   }

   public static boolean has(String key) {
      if (key == null || !key.startsWith("lx_")) {
         return false;
      }
      for (String[] e : LIST) {
         if (e[0].equals(key)) {
            return true;
         }
      }
      return false;
   }

   /** The texture of this cape, repainted when its next frame is due (render thread). */
   public static Identifier current(String key) {
      if (!has(key)) {
         return null;
      }
      try {
         Slot slot = SLOTS.get(key);
         long now = System.currentTimeMillis();
         if (slot == null) {
            slot = new Slot();
            slot.id = Identifier.fromNamespaceAndPath("legoclient", "motioncape/" + key.substring(3));
            paint(key, now / 1000.0F, slot.px);
            slot.tex = Tx.register(slot.id, slot.px, W, H, false);
            slot.frame = now / FRAME_MS;
            SLOTS.put(key, slot);
         } else if (now / FRAME_MS != slot.frame) {
            slot.frame = now / FRAME_MS;
            paint(key, now / 1000.0F, slot.px);
            Tx.update(slot.tex, slot.px, W, H);
         }
         return slot.id;
      } catch (Throwable e) {
         LegoClient.LOG("Bewegtes Cape " + key + ": " + e);
         return null;
      }
   }

   private static final class Slot {
      Identifier id;
      DynamicTexture tex;
      long frame;
      final int[] px = new int[W * H];
   }

   // ------------------------------------------------------------------ painting

   /** Fills the whole cape layout; u/v run over the front face (0..1), the other faces continue it. */
   static void paint(String key, float t, int[] out) {
      for (int py = 0; py < H; py++) {
         for (int px = 0; px < W; px++) {
            float u = (px / (float)S - 1.0F) / 10.0F;
            float v = (py / (float)S - 1.0F) / 16.0F;
            int c = pixel(key, u, v, t, px, py);
            // back and sides a bit darker, so the front reads as the front
            if (px >= 11 * S && px < 22 * S && py < 17 * S || px < S || py < S) {
               c = shade(c, 0.72F);
            }
            out[py * W + px] = 0xFF000000 | c;
         }
      }
   }

   private static int pixel(String key, float u, float v, float t, int px, int py) {
      switch (key) {
         case "lx_galaxy": {
            float dx = u - 0.5F, dy = (v - 0.5F) * 1.6F;
            float r = (float)Math.sqrt(dx * dx + dy * dy);
            float a = (float)Math.atan2(dy, dx);
            float arm = (float)(0.5 + 0.5 * Math.sin(a * 3.0 + r * 14.0 - t * 1.6));
            int base = mix(0x0B0620, mix(0x3A1C7A, 0x2C6BFF, arm), clamp(arm * (1.2F - r * 1.3F)));
            float core = clamp(1.0F - r * 5.0F);
            base = mix(base, 0xFFE9C8, core * core);
            return star(base, px, py, t, 0.012F);
         }
         case "lx_lava": {
            float n = fbm(u * 3.0F, v * 4.0F + t * 0.35F, 3);
            float crack = clamp(1.0F - Math.abs(n - 0.5F) * 9.0F);
            int rock = mix(0x1A0A06, 0x3B1408, n);
            return mix(rock, mix(0xFF4A00, 0xFFD24A, crack), crack);
         }
         case "lx_aurora": {
            float curtain = (float)(0.5 + 0.5 * Math.sin(u * 9.0 + Math.sin(v * 3.0 + t * 0.8) * 2.2 + t * 0.6));
            float glow = clamp(curtain * (1.0F - v * 0.9F)) * clamp(v * 3.0F);
            int sky = mix(0x02101A, 0x061E2A, v);
            int col = mix(0x2BFFB0, 0xA45CFF, clamp(v * 1.4F + 0.2F * (float)Math.sin(t + u * 4)));
            return star(mix(sky, col, glow), px, py, t, 0.006F);
         }
         case "lx_lightning": {
            int sky = mix(0x070B1C, 0x1A2140, v);
            float cycle = t % 2.2F;
            float flash = cycle < 0.15F ? 1.0F - cycle / 0.15F : 0.0F;
            sky = mix(sky, 0x9FB4FF, flash * 0.45F);
            float boltX = 0.5F + 0.18F * (float)Math.sin(Math.floor(t / 2.2F) * 7.3) + 0.08F * (float)Math.sin(v * 23.0 + Math.floor(t / 2.2F) * 3.1);
            float d = Math.abs(u - boltX);
            float bolt = cycle < 0.35F ? clamp(1.0F - d * 40.0F) : 0.0F;
            float halo = cycle < 0.35F ? clamp(1.0F - d * 9.0F) * 0.35F : 0.0F;
            return mix(mix(sky, 0x6E86FF, halo), 0xFFFFFF, bolt);
         }
         case "lx_matrix": {
            int col = px / S;
            float speed = 0.6F + hash(col, 7) * 0.9F;
            float head = (t * speed + hash(col, 3) * 5.0F) % 1.6F - 0.3F;
            float d = head - v;
            float trail = d >= 0.0F && d < 0.6F ? 1.0F - d / 0.6F : 0.0F;
            float glyph = hash(col, py / S + (int)(t * 6.0F) % 1000) > 0.35F ? 1.0F : 0.4F;
            int c = mix(0x020803, 0x16B83A, trail * glyph);
            return d >= 0.0F && d < 0.04F ? 0xC8FFD2 : c;
         }
         case "lx_ocean": {
            float w = 0.0F;
            for (int k = 1; k <= 3; k++) {
               w += (float)Math.sin(u * 6.0 * k + t * (0.9 + k * 0.4) + v * 3.0 * k) / k;
            }
            float h = clamp(0.5F + w * 0.18F);
            int c = mix(0x03264A, 0x1C8FD1, h);
            float foam = clamp((h - 0.78F) * 6.0F);
            return mix(mix(c, 0x0A4F80, v * 0.4F), 0xE8FBFF, foam);
         }
         case "lx_plasma": {
            float p = (float)(Math.sin(u * 8.0 + t) + Math.sin(v * 9.0 - t * 1.3) + Math.sin((u + v) * 7.0 + t * 0.7)
               + Math.sin(Math.sqrt((u - 0.5) * (u - 0.5) + (v - 0.5) * (v - 0.5)) * 14.0 - t * 2.0));
            return hsv(0.62F + p * 0.08F, 0.75F, 0.55F + 0.35F * (float)Math.abs(Math.sin(p)));
         }
         case "lx_rainbow": {
            float hue = u * 0.6F + v * 0.4F + 0.05F * (float)Math.sin(v * 12.0 + t * 3.0) + t * 0.25F;
            return hsv(hue - (float)Math.floor(hue), 0.8F, 0.95F);
         }
         case "lx_neon": {
            float horizon = 0.25F;
            if (v < horizon) {
               return mix(0x12001F, 0xFF3CAC, clamp(1.0F - (horizon - v) * 6.0F) * 0.6F);
            }
            float z = 1.0F / (v - horizon + 0.05F);
            float gx = Math.abs((u - 0.5F) * z * 1.4F % 1.0F);
            float gz = (z * 0.5F + t * 1.2F) % 1.0F;
            float line = Math.max(clamp(1.0F - Math.min(gx, 1.0F - gx) * 18.0F / Math.min(z, 6.0F)), clamp(1.0F - Math.min(gz, 1.0F - gz) * 22.0F / Math.min(z, 6.0F)));
            return mix(0x0A0014, mix(0x00E5FF, 0xFF2BD6, clamp(v)), line);
         }
         case "lx_stars": {
            int base = mix(0x02030A, 0x0B1030, v);
            float dx = u - 0.5F, dy = v - 0.5F;
            float r = (float)Math.sqrt(dx * dx + dy * dy);
            float a = (float)Math.atan2(dy, dx);
            int ray = (int)((a + Math.PI) / (Math.PI * 2.0) * 90.0);
            float pos = (t * (0.3F + hash(ray, 11) * 0.5F) + hash(ray, 5)) % 1.0F;
            float s = clamp(1.0F - Math.abs(r - pos * 0.8F) * 60.0F) * (hash(ray, 2) > 0.55F ? 1.0F : 0.0F);
            return mix(base, 0xEAF2FF, s * clamp(pos * 2.0F));
         }
         case "lx_diamond": {
            float d = Math.abs((u * 6.0F) % 1.0F - 0.5F) + Math.abs((v * 9.0F) % 1.0F - 0.5F);
            int c = mix(0x0C7C86, 0x5CF2E8, clamp(1.0F - d * 1.4F));
            c = mix(c, 0x0A4E57, clamp((d - 0.45F) * 8.0F));
            return star(c, px, py, t * 1.5F, 0.02F);
         }
         case "lx_ender": {
            float n = fbm(u * 2.5F + t * 0.1F, v * 3.0F - t * 0.15F, 3);
            int c = mix(0x07020D, 0x3B0E5C, n);
            int cell = (px / 6) * 131 + (py / 6) * 71;
            float life = (t * 0.6F + hash(cell, 9)) % 1.0F;
            float part = hash(cell, 4) > 0.93F ? (float)Math.sin(life * Math.PI) : 0.0F;
            return mix(c, 0xD97CFF, part);
         }
         case "lx_fire": {
            float n = fbm(u * 3.5F, v * 3.0F + t * 1.4F, 4);
            float heat = clamp(n * 1.4F - (1.0F - v) * 0.9F + 0.15F);
            int c = mix(0x120302, 0xC81E00, clamp(heat * 1.6F));
            c = mix(c, 0xFF9A1F, clamp((heat - 0.45F) * 3.0F));
            return mix(c, 0xFFF1A8, clamp((heat - 0.8F) * 5.0F));
         }
         case "lx_ice": {
            float a = (float)Math.atan2(v - 0.5F, u - 0.5F);
            float r = (float)Math.hypot(u - 0.5F, (v - 0.5F) * 1.6F);
            float flake = Math.abs((float)Math.cos(a * 3.0)) * clamp(1.0F - r * 2.0F);
            float shine = (float)(0.5 + 0.5 * Math.sin((u + v) * 10.0 - t * 2.4));
            int c = mix(0x6FB8E8, 0xDDF4FF, flake);
            return mix(c, 0xFFFFFF, clamp(shine - 0.82F) * 4.0F);
         }
         case "lx_pulse": {
            float r = (float)Math.hypot(u - 0.5F, (v - 0.45F) * 1.4F);
            float beat = (float)Math.pow(Math.max(0.0, Math.sin(t * 5.2)), 6.0);
            float ring = clamp(1.0F - Math.abs(r - (t * 0.5F) % 0.8F) * 25.0F);
            float hx = (u - 0.5F) * 2.6F, hy = (0.45F - v) * 2.6F + 0.15F;
            float heart = (float)(Math.pow(hx * hx + hy * hy - 0.25, 3.0) - hx * hx * hy * hy * hy) < 0.0 ? 1.0F : 0.0F;
            int c = mix(0x1A0207, 0x8C0A23, ring * 0.8F);
            return mix(c, mix(0xE0153B, 0xFF6F8A, beat), heart);
         }
         case "lx_bricks": {
            int col = (px / S) / 3;
            float fall = (t * (0.4F + hash(col, 1) * 0.5F) + hash(col, 8)) % 1.2F - 0.1F;
            float by = v - fall;
            int base = mix(0x10131A, 0x1D2230, v);
            if (by > -0.06F && by < 0.0F) {
               int[] lego = {0xE3000B, 0xFFCD03, 0x006CB7, 0x00A651, 0xFFFFFF};
               int c = lego[(int)(hash(col, (int)(t / 1.2F)) * lego.length) % lego.length];
               return (px % (3 * S)) < S / 2 ? shade(c, 0.7F) : c;
            }
            return base;
         }
         default:
            return 0x202020;
      }
   }

   // ------------------------------------------------------------------ helpers

   private static int star(int base, int px, int py, float t, float density) {
      int cell = (px / 2) * 7919 + (py / 2) * 104729;
      if (hash(cell, 1) < density) {
         float tw = (float)(0.5 + 0.5 * Math.sin(t * (2.0 + hash(cell, 2) * 4.0) + hash(cell, 3) * 6.28));
         return mix(base, 0xFFFFFF, tw);
      }
      return base;
   }

   private static float hash(int a, int b) {
      int h = a * 374761393 + b * 668265263;
      h = (h ^ (h >>> 13)) * 1274126177;
      h ^= h >>> 16;
      return (h & 0xFFFFFF) / (float)0x1000000;
   }

   private static float noise(float x, float y) {
      int xi = (int)Math.floor(x), yi = (int)Math.floor(y);
      float xf = x - xi, yf = y - yi;
      float a = hash(xi, yi), b = hash(xi + 1, yi), c = hash(xi, yi + 1), d = hash(xi + 1, yi + 1);
      float u = xf * xf * (3 - 2 * xf), v = yf * yf * (3 - 2 * yf);
      return a + (b - a) * u + (c - a) * v + (a - b - c + d) * u * v;
   }

   private static float fbm(float x, float y, int octaves) {
      float sum = 0.0F, amp = 0.5F, f = 1.0F;
      for (int i = 0; i < octaves; i++) {
         sum += noise(x * f, y * f) * amp;
         f *= 2.0F;
         amp *= 0.5F;
      }
      return sum / (1.0F - (float)Math.pow(0.5, octaves));
   }

   private static float clamp(float v) {
      return v < 0.0F ? 0.0F : (v > 1.0F ? 1.0F : v);
   }

   private static int mix(int a, int b, float t) {
      t = clamp(t);
      int r = (int)((a >> 16 & 255) + ((b >> 16 & 255) - (a >> 16 & 255)) * t);
      int g = (int)((a >> 8 & 255) + ((b >> 8 & 255) - (a >> 8 & 255)) * t);
      int bl = (int)((a & 255) + ((b & 255) - (a & 255)) * t);
      return r << 16 | g << 8 | bl;
   }

   private static int shade(int c, float f) {
      return mix(0, c, f);
   }

   private static int hsv(float h, float s, float v) {
      return java.awt.Color.HSBtoRGB(h, clamp(s), clamp(v)) & 0xFFFFFF;
   }
}
