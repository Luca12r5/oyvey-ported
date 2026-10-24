package dev.lego.cosmetic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Pets {
   public static final String[] EMOTIONS = new String[]{"happy", "love", "sleepy", "sad", "excited", "angry", "curious", "dizzy"};
   private static final Map<String, Float> HEIGHT = new ConcurrentHashMap<>();

   private Pets() {
   }

   public static float height(String id) {
      return height(id, 0.5F);
   }

   public static float height(String id, float growth) {
      float gq = Math.round(Math.max(0.0F, Math.min(1.0F, growth)) * 20.0F) / 20.0F;
      String key = id + "@" + gq;
      Float h = HEIGHT.get(key);
      if (h != null) {
         return h;
      } else {
         Cos.Item it = Cos.get(id);
         float v = 10.0F;
         if (it != null && it.slot == Cos.Slot.PET) {
            float[] max = new float[]{0.0F};
            G g = new G((tex, p, uv, argb, glow, nx, ny, nz) -> {
               if (!tex.equals("pet_star") && !tex.equals("pet_heart")) {
                  for (int i = 1; i < 12; i += 3) {
                     max[0] = Math.max(max[0], p[i]);
                  }
               }
            });
            Cos.A a = new Cos.A();
            a.emotion = "happy";
            a.time = 0.35F;
            a.growth = gq;
            a.level = 1 + Math.round(gq * 4.0F);

            try {
               it.model.render(g, a);
               v = Math.max(3.0F, max[0]);
            } catch (RuntimeException var11) {
            }

            HEIGHT.put(key, v);
         }

         return v;
      }
   }

   static void registerAll() {
      Cos.add("pet_cat", "Kätzchen", Cos.Slot.PET, Cos.Rarity.COMMON, PetModels::cat);
      Cos.add("pet_shiba", "Shiba", Cos.Slot.PET, Cos.Rarity.RARE, PetModels::shiba);
      Cos.add("pet_bunny", "Häschen", Cos.Slot.PET, Cos.Rarity.COMMON, PetModels::bunny);
      Cos.add("pet_fox", "Füchschen", Cos.Slot.PET, Cos.Rarity.RARE, PetModels::fox);
      Cos.add("pet_redpanda", "Roter Panda", Cos.Slot.PET, Cos.Rarity.EPIC, PetModels::redPanda);
      Cos.add("pet_panda", "Pandabär", Cos.Slot.PET, Cos.Rarity.RARE, PetModels::panda);
      Cos.add("pet_bear", "Teddybär", Cos.Slot.PET, Cos.Rarity.COMMON, PetModels::bear);
      Cos.add("pet_hamster", "Hamster", Cos.Slot.PET, Cos.Rarity.COMMON, PetModels::hamster);
      Cos.add("pet_penguin", "Pinguin", Cos.Slot.PET, Cos.Rarity.RARE, grow(PetModels::penguin, 1.55F, 3.4F));
      Cos.add("pet_chick", "Küken", Cos.Slot.PET, Cos.Rarity.COMMON, grow(PetModels::chick, 1.5F, 3.6F));
      Cos.add("pet_owl", "Eule", Cos.Slot.PET, Cos.Rarity.RARE, grow(PetModels::owl, 1.6F, 3.4F));
      Cos.add("pet_frog", "Fröschchen", Cos.Slot.PET, Cos.Rarity.COMMON, grow(PetModels::frog, 1.55F, 3.8F));
      Cos.add("pet_axolotl", "Axolotl", Cos.Slot.PET, Cos.Rarity.EPIC, grow(PetModels::axolotl, 1.55F, 4.0F));
      Cos.add("pet_turtle", "Schildkröte", Cos.Slot.PET, Cos.Rarity.RARE, grow(PetModels::turtle, 1.6F, 4.2F));
      Cos.add("pet_bee", "Bienchen", Cos.Slot.PET, Cos.Rarity.RARE, grow(PetModels::bee, 1.45F, 3.4F));
      Cos.add("pet_bat", "Fledermaus", Cos.Slot.PET, Cos.Rarity.EPIC, grow(PetModels::bat, 1.5F, 3.6F));
      Cos.add("pet_slime", "Schleimi", Cos.Slot.PET, Cos.Rarity.COMMON, grow(PetModels::slime, 1.6F, 4.2F));
      Cos.add("pet_ghost", "Geistchen", Cos.Slot.PET, Cos.Rarity.RARE, grow(PetModels::ghostPet, 1.5F, 3.8F));
      Cos.add("pet_jelly", "Qualle", Cos.Slot.PET, Cos.Rarity.EPIC, grow(PetModels::jelly, 1.45F, 3.8F));
      Cos.add("pet_fish", "Blubberfisch", Cos.Slot.PET, Cos.Rarity.EPIC, grow(PetModels::fish, 1.4F, 4.4F));
      Cos.add("pet_mushroom", "Pilzchen", Cos.Slot.PET, Cos.Rarity.RARE, grow(PetModels::mushroom, 1.55F, 4.2F));
      Cos.add("pet_brick", "Stein-Freund", Cos.Slot.PET, Cos.Rarity.EPIC, grow(PetModels::brickBuddy, 1.5F, 4.4F));
      Cos.add("pet_cloud", "Wölkchen", Cos.Slot.PET, Cos.Rarity.EPIC, grow(PetModels::cloud, 1.5F, 4.8F));
      Cos.add("pet_robot", "Robo", Cos.Slot.PET, Cos.Rarity.EPIC, grow(PetModels::robot, 1.45F, 3.8F));
      Cos.add("pet_dragon", "Mini-Drache", Cos.Slot.PET, Cos.Rarity.LEGENDARY, PetModels::miniDragon);
      Cos.add("pet_unicorn", "Einhorn", Cos.Slot.PET, Cos.Rarity.LEGENDARY, PetModels::unicorn);
      Cos.add("pet_wolf", "Wolf", Cos.Slot.PET, Cos.Rarity.RARE, PetReal::wolf);
      Cos.add("pet_retriever", "Golden Retriever", Cos.Slot.PET, Cos.Rarity.RARE, PetReal::retriever);
      Cos.add("pet_arcticfox", "Polarfuchs", Cos.Slot.PET, Cos.Rarity.RARE, PetReal::arcticFox);
      Cos.add("pet_horse", "Pferd", Cos.Slot.PET, Cos.Rarity.EPIC, PetReal::horse);
      Cos.add("pet_deer", "Hirsch", Cos.Slot.PET, Cos.Rarity.EPIC, PetReal::deer);
      Cos.add("pet_tiger", "Tiger", Cos.Slot.PET, Cos.Rarity.EPIC, PetReal::tiger);
      Cos.add("pet_eagle", "Adler", Cos.Slot.PET, Cos.Rarity.EPIC, PetReal::eagle);
      Cos.add("pet_lion", "Löwe", Cos.Slot.PET, Cos.Rarity.LEGENDARY, PetReal::lion);
      Cos.add("pet_drake", "Drache", Cos.Slot.PET, Cos.Rarity.LEGENDARY, PetReal::drake);
   }

   static Cos.Model grow(Cos.Model m, float max, float ring) {
      return (g, a) -> {
         float k = Math.max(0.0F, Math.min(1.0F, a.growth));
         float s = a.preview ? 1.0F : 0.92F + (max - 0.92F) * k;
         g.push();
         g.scale(s);

         try {
            m.render(g, a);
         } finally {
            g.pop();
         }

         if (a.level >= 5 && !a.preview) {
            Pets.P p = new Pets.P();
            p.t = a.time;
            p.lvl = a.level;
            PetRig.sparkles(g, p, ring * s);
         }
      };
   }

   static String emotion(String e) {
      if (e != null) {
         for (String s : EMOTIONS) {
            if (s.equals(e)) {
               return s;
            }
         }
      }

      return "happy";
   }

   static Pets.P pose(Cos.A a) {
      Pets.P p = new Pets.P();
      float t = p.t = a.time;
      float sp = p.sp = Math.max(0.0F, Math.min(1.0F, a.speed));
      p.emo = emotion(a.emotion);
      p.lookX = Math.max(-1.0F, Math.min(1.0F, a.lookX));
      p.lookY = Math.max(-1.0F, Math.min(1.0F, a.lookY));
      float ph = t * 11.0F;
      p.walk = PetGeo.sin(ph) * 38.0F * sp;
      p.breathe = PetGeo.sin(t * 2.2F);
      p.stepBob = Math.abs(PetGeo.sin(ph)) * 0.45F * sp;
      p.gr = Math.max(0.0F, Math.min(1.0F, a.growth));
      p.lvl = Math.max(1, Math.min(5, a.level));
      p.preview = a.preview;
      p.sy = 1.0F + p.breathe * 0.018F;
      p.sx = 1.0F - p.breathe * 0.008F;
      p.headYaw = p.lookX * 24.0F;
      p.headPitch = -p.lookY * 14.0F;
      p.earTwitch = (float)Math.pow(Math.max(0.0, Math.sin(t * 0.9)), 40.0) * 14.0F;
      float tailAmp = 18.0F;
      float tailSpd = 7.0F;
      p.flapSpd = 13.0F;
      String doze = p.emo;
      switch (doze) {
         case "happy":
            p.headRoll = PetGeo.sin(t * 1.7F) * 5.0F;
            p.y = p.y + Math.abs(PetGeo.sin(t * 3.4F)) * 0.2F;
            tailAmp = 26.0F;
            tailSpd = 10.0F;
            break;
         case "love":
            p.rotZ = PetGeo.sin(t * 2.3F) * 7.0F;
            p.headRoll = PetGeo.sin(t * 2.3F + 0.5F) * 10.0F;
            tailAmp = 22.0F;
            tailSpd = 5.0F;
            break;
         case "sleepy":
            p.lie = 1.0F - Math.min(1.0F, sp * 2.0F);
            p.breathe = PetGeo.sin(t * 1.2F);
            p.sy = 1.0F + p.breathe * 0.035F;
            p.sx = 1.0F - p.breathe * 0.012F;
            p.headPitch = p.headPitch + 7.0F * p.lie;
            p.headRoll = 11.0F * p.lie;
            p.ear = 0.35F;
            tailAmp = 3.0F;
            tailSpd = 1.0F;
            p.tailLift = -25.0F;
            p.hover = 0.45F;
            p.flapSpd = 5.0F;
            break;
         case "sad":
            p.headPitch += 14.0F;
            p.ear = 0.0F;
            tailAmp = 4.0F;
            tailSpd = 2.0F;
            p.tailLift = -35.0F;
            p.y -= 0.08F;
            p.sy *= 0.97F;
            p.hover = 0.7F;
            p.flapSpd = 7.0F;
            break;
         case "excited":
            float h = Math.abs(PetGeo.sin(t * 6.8F));
            p.y += h * 2.0F;
            p.sy *= 1.0F + (h - 0.35F) * 0.16F;
            p.sx *= 1.0F - (h - 0.35F) * 0.08F;
            p.headRoll = PetGeo.sin(t * 6.8F) * 6.0F;
            p.ear = 1.1F;
            tailAmp = 36.0F;
            tailSpd = 20.0F;
            p.hover = 1.1F;
            p.flapSpd = 22.0F;
            break;
         case "angry":
            p.rootX = PetGeo.sin(t * 40.0F) * 0.2F;
            p.headPitch += 7.0F;
            p.ear = 0.45F;
            tailAmp = 12.0F;
            tailSpd = 24.0F;
            p.sx *= 1.05F;
            p.sy *= 0.97F;
            p.flapSpd = 18.0F;
            break;
         case "curious":
            p.headRoll = 18.0F + PetGeo.sin(t * 1.4F) * 4.0F;
            p.headYaw = p.headYaw + PetGeo.sin(t * 0.6F) * 12.0F;
            p.headPitch -= 5.0F;
            p.ear = 1.12F;
            tailAmp = 12.0F;
            tailSpd = 3.0F;
            p.tailLift = 10.0F;
            break;
         case "dizzy":
            p.rotZ = PetGeo.sin(t * 3.1F) * 9.0F;
            p.rotX = PetGeo.cos(t * 3.1F) * 6.0F;
            p.headRoll = PetGeo.sin(t * 3.1F + 1.2F) * 14.0F;
            p.headYaw = p.headYaw + PetGeo.cos(t * 3.1F) * 10.0F;
            p.ear = 0.6F;
            tailAmp = 8.0F;
            tailSpd = 2.0F;
      }

      p.tail = PetGeo.sin(t * tailSpd) * tailAmp;
      PetRig.gait(p);
      p.flap = PetGeo.sin(t * p.flapSpd);
      boolean doze = p.is("sleepy") && sp < 0.3F && (t * 0.3F % 1.0F + 1.0F) % 1.0F < 0.78F;
      p.closed = a.blink > 0.5F || doze;
      return p;
   }

   static void root(G g, Pets.P p) {
      g.translate(p.rootX, p.y + p.stepBob, 0.0F);
      if (p.rotZ != 0.0F) {
         g.rotZ(p.rotZ);
      }

      if (p.rotX != 0.0F) {
         g.rotX(p.rotX);
      }

      g.scale(p.sx, p.sy, p.sx);
   }

   static void headTurn(G g, Pets.P p) {
      g.rotY(p.headYaw);
      g.rotX(p.headPitch);
      g.rotZ(p.headRoll);
   }

   static String fur(String k) {
      return "pet_fur:" + k;
   }

   static String head(String k) {
      return "pet_head:" + k;
   }

   static String col(int rgb) {
      return "pet_c:" + String.format("%06X", rgb & 16777215);
   }

   static String gl(int rgb) {
      return "pet_gl:" + String.format("%06X", rgb & 16777215);
   }

   static void face(G g, Pets.P p, String style, float cx, float cy, float cz, float rx, float ry, float rz, float span, float el0) {
      boolean og = g.glow;
      int oc = g.color;
      boolean led = style.equals("led");
      g.color(-1).glow(led);
      PetGeo.decal(g, "pet_face:" + style + ":" + p.emo, cx, cy, cz, rx, ry, rz, 0.0F, el0, span, 0.0F, 0.0F, 1.0F, 1.0F, 1.035F);
      PetGeo.decal(g, eyes(style, p), cx, cy, cz, rx, ry, rz, p.lookX * 0.09F, el0 + p.lookY * 0.06F, span, 0.0F, 0.0F, 1.0F, 1.0F, 1.05F);
      g.color(oc).glow(og);
   }

   static void faceFlat(G g, Pets.P p, String style, float cx, float cy, float z, float w, float h) {
      boolean og = g.glow;
      int oc = g.color;
      g.color(-1).glow(style.equals("led"));
      PetGeo.flat(g, "pet_face:" + style + ":" + p.emo, cx, cy, z, w, h);
      PetGeo.flat(g, eyes(style, p), cx + p.lookX * w * 0.05F, cy + p.lookY * h * 0.05F, z + 0.03F, w, h);
      g.color(oc).glow(og);
   }

   static String eyes(String style, Pets.P p) {
      return "pet_eyes:" + style + ":" + p.emo + ":" + (p.closed ? 1 : 0);
   }

   static void nose(G g, int rgb, float rx, float ry, float rz, float el, float r) {
      PetGeo.blob(g, gl(rgb), 0.0F, PetGeo.sin(el) * ry, PetGeo.cos(el) * rz * 0.99F, r * 1.25F, r * 0.85F, r * 0.8F, 6, 4);
   }

   static void earPointy(G g, Pets.P p, String tex, String inner, float x, float y, float z, int side, float w, float h, float tilt) {
      earPointy(g, p, tex, inner, null, x, y, z, side, w, h, tilt);
   }

   static void earPointy(G g, Pets.P p, String tex, String inner, String tip, float x, float y, float z, int side, float w, float h, float tilt) {
      g.push();
      g.translate(x, y, z);
      float droop = Math.max(0.0F, 1.0F - p.ear);
      float perk = Math.max(0.0F, p.ear - 1.0F) * 60.0F;
      g.rotZ(-side * (tilt + droop * 58.0F - perk + p.earTwitch * (side > 0 ? 1 : 0)));
      g.rotX(-droop * 22.0F);
      g.push();
      g.scale(1.0F, 1.0F, 0.42F);
      PetGeo.cone(g, tex, w, h, 8, false);
      if (tip != null) {
         g.translate(0.0F, h * 0.6F, 0.0F);
         PetGeo.cone(g, tip, w * 0.43F, h * 0.41F, 8, false);
      }

      g.pop();
      if (inner != null) {
         g.push();
         g.translate(0.0F, h * 0.08F, w * 0.13F);
         g.scale(1.0F, 1.0F, 0.42F);
         PetGeo.cone(g, inner, w * 0.62F, h * 0.76F, 7, false);
         g.pop();
      }

      g.pop();
   }

   static void earRound(G g, Pets.P p, String tex, String inner, float x, float y, float z, int side, float r) {
      g.push();
      g.translate(x, y, z);
      float droop = Math.max(0.0F, 1.0F - p.ear);
      g.rotZ(-side * (15.0F + droop * 35.0F + p.earTwitch * (side > 0 ? 1 : 0)));
      g.rotX(-droop * 15.0F);
      PetGeo.blob(g, tex, 0.0F, r * 0.55F, 0.0F, r, r, r * 0.55F, 7, 4);
      if (inner != null) {
         PetGeo.blob(g, inner, 0.0F, r * 0.5F, r * 0.26F, r * 0.62F, r * 0.62F, r * 0.33F, 5, 2);
      }

      g.pop();
   }

   static void tail(G g, Pets.P p, String[] tex, float[] r, float segLen, float curl, float lift, float wag) {
      g.push();
      g.rotY(p.tail * wag + p.tailSway);
      g.rotX(lift + p.tailLift);

      for (int i = 0; i < r.length; i++) {
         g.rotX(curl);
         g.rotY(p.tail * 0.35F * wag);
         PetGeo.blob(g, tex[Math.min(i, tex.length - 1)], 0.0F, 0.0F, -segLen * 0.5F, r[i], r[i], segLen * 0.62F + r[i] * 0.3F, 7, 4);
         g.translate(0.0F, 0.0F, -segLen * 0.82F);
      }

      g.pop();
   }

   static void leg(G g, String tex, float x, float z, float r, float h, float swing, float drop) {
      g.push();
      g.translate(x, h - drop, z);
      g.rotX(swing);
      PetGeo.blob(g, tex, 0.0F, -h * 0.45F, 0.0F, r, h * 0.56F, r * 1.05F, 6, 4);
      g.pop();
   }

   static void legs4(G g, Pets.P p, String tex, float x, float zf, float zb, float r, float h) {
      float drop = p.lie * h * 0.72F;

      for (int s = -1; s <= 1; s += 2) {
         leg(g, tex, s * x, zf, r, h, s * p.walk - p.lie * 72.0F, drop);
         leg(g, tex, s * x, zb, r, h, -s * p.walk + p.lie * 72.0F, drop);
      }
   }

   static void fx(G g, Pets.P p, float top) {
      if (p.is("dizzy")) {
         for (int k = 0; k < 3; k++) {
            double an = p.t * 3.2 + k * Math.PI * 2.0 / 3.0;
            PetGeo.sprite(g, "pet_star", PetGeo.cos(an) * 2.8F, top + 0.4F + PetGeo.sin(an * 2.0) * 0.2F, PetGeo.sin(an) * 2.8F, 1.5F, -1);
         }
      } else if (p.is("love")) {
         for (int k = 0; k < 2; k++) {
            float ph = ((p.t * 0.55F + k * 0.5F) % 1.0F + 1.0F) % 1.0F;
            int al = (int)(255.0 * Math.sin(ph * Math.PI));
            PetGeo.sprite(
               g, "pet_heart", (k == 0 ? 1.6F : -1.8F) + PetGeo.sin(ph * 6.0F + k) * 0.5F, top + ph * 3.2F, 0.5F, 1.2F + 0.5F * ph, al << 24 | 16777215
            );
         }
      }
   }

   static void quad4(G g, Pets.P p, Pets.Q4 q) {
      float k = p.gr;
      float S = p.preview ? 0.9F : 0.88F + 0.46F * k;
      float legF = 1.0F + 1.25F * k;
      float lenF = 1.0F + 0.4F * k;
      float headF = 1.0F - 0.3F * k;
      float legH = q.legH * legF;
      float lift = legH - q.legH;
      float bodyRz = q.bodyRz * lenF;
      float bodyY = q.bodyY + lift;
      float bodyZ = q.bodyZ * lenF;
      float zf = q.legZf * lenF;
      float zb = q.legZb * lenF;
      float headY = q.headY + lift - q.headRy * (1.0F - headF) * 0.9F + 0.35F * k;
      float headZ = q.headZ + bodyRz * 0.3F * k;
      float belly = bodyY - q.bodyRy;
      p.stepBob = 0.0F;
      g.push();
      g.scale(S);
      root(g, p);
      PetRig.Frame f = PetRig.frame(p, legH, zf, zb, legH, belly);
      float lr = q.legR * (1.0F - 0.12F * k);
      PetRig.LegStyle ls = new PetRig.LegStyle(k > 0.3F ? q.body : q.leg, q.leg, q.leg, lr, lr * 0.88F, lr * 0.95F);
      ls.pawLen = 1.15F;
      PetRig.legs(g, p, f, ls, ls, q.legX, zf, zb, legH, legH, 0.52F, 0.5F);
      g.push();
      f.apply(g);
      g.rotZ(p.roll);
      g.push();
      g.translate(0.0F, bodyY, bodyZ);
      g.scale(1.0F, 1.0F, p.stretch);
      PetGeo.blob(g, q.body, 0.0F, 0.0F, 0.0F, q.bodyRx, q.bodyRy, bodyRz, 10, 5);
      if (q.bodyExtra != null) {
         q.bodyExtra.draw(g, p);
      }

      if (k > 0.3F) {
         float cf = Math.min(1.0F, (k - 0.3F) / 0.5F);
         PetGeo.blob(g, q.body, 0.0F, q.bodyRy * 0.25F, bodyRz * 0.72F, q.bodyRx * 0.62F * cf, q.bodyRy * 0.62F * cf, bodyRz * 0.32F * cf, 7, 4);
      }

      if (q.tail != null) {
         g.push();
         g.translate(0.0F, -q.bodyRy * 0.1F + 0.2F * k, -bodyRz * 0.88F);
         g.scale(1.0F + 0.35F * k);
         q.tail.draw(g, p);
         g.pop();
      }

      g.pop();
      if (k > 0.15F) {
         float ny = (bodyY + headY) * 0.5F;
         float nz = (bodyZ + bodyRz * 0.55F + headZ) * 0.5F;
         PetGeo.blob(g, q.body, 0.0F, ny, nz, q.bodyRx * 0.6F, (headY - bodyY) * 0.42F, q.bodyRx * 0.62F, 8, 4);
      }

      g.push();
      g.translate(0.0F, bodyY + q.bodyRy * 0.62F + 0.2F * k, bodyZ + bodyRz * 0.5F + 0.2F * k);
      g.rotX(28.0F);
      PetRig.collar(g, p, q.bodyRx * (0.66F + 0.05F * k), 0.24F);
      g.pop();
      g.push();
      g.translate(0.0F, headY - p.lie * 0.35F - p.bob * legH * 0.5F, headZ);
      headTurn(g, p);
      g.rotX(p.nod - p.sit * 6.0F);
      g.scale(headF);
      String style = PetRig.style(q.style, k);
      PetGeo.blob(g, q.head, 0.0F, 0.0F, 0.0F, q.headRx, q.headRy, q.headRz, 14, 9);
      face(g, p, style, 0.0F, 0.0F, 0.0F, q.headRx, q.headRy, q.headRz, q.span, q.faceEl);
      if (q.nose >= 0) {
         nose(g, q.nose, q.headRx, q.headRy, q.headRz, q.noseEl, q.noseR * (1.0F + 0.4F * k));
      }

      if (k > 0.4F) {
         float tf = Math.min(1.0F, (k - 0.4F) / 0.4F);

         for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * q.headRx * 0.9F, -q.headRy * 0.35F, -0.2F);
            g.rotZ(-s * 115);
            g.scale(1.0F, 1.0F, 0.5F);
            PetGeo.cone(g, q.head, 0.7F * tf, 1.3F * tf, 5, false);
            g.pop();
         }
      }

      if (q.ears != null) {
         g.push();
         g.scale(1.0F + 0.12F * k);
         q.ears.draw(g, p);
         g.pop();
      }

      if (q.headExtra != null) {
         q.headExtra.draw(g, p);
      }

      g.pop();
      fx(g, p, headY + q.headRy * headF + 1.2F);
      g.pop();
      PetRig.sparkles(g, p, Math.max(q.bodyRx, bodyRz) + 1.2F);
      g.pop();
   }

   static final class P {
      float t;
      float sp;
      float lookX;
      float lookY;
      String emo;
      boolean closed;
      float y;
      float rootX;
      float rotZ;
      float rotX;
      float sx = 1.0F;
      float sy = 1.0F;
      float headYaw;
      float headPitch;
      float headRoll;
      float ear = 1.0F;
      float earTwitch;
      float tail;
      float tailLift;
      float walk;
      float lie;
      float breathe;
      float hover = 1.0F;
      float flap;
      float flapSpd;
      float gr;
      int lvl = 3;
      boolean preview;
      float walkW;
      float runW;
      float idle;
      float cw;
      float cr;
      float bob;
      float roll;
      float pitch;
      float stretch = 1.0F;
      float nod;
      float tailSway;
      float waddle;
      float waddleBob;
      float sit;
      float bow;
      float sniff;
      float crouch;
      float stepBob;

      boolean is(String e) {
         return this.emo.equals(e);
      }
   }

   interface Part {
      void draw(G var1, Pets.P var2);
   }

   static final class Q4 {
      String body;
      String head;
      String leg;
      String style = "std";
      float bodyY = 2.55F;
      float bodyRx = 2.2F;
      float bodyRy = 1.95F;
      float bodyRz = 2.45F;
      float bodyZ = -0.35F;
      float headY = 6.1F;
      float headZ = 0.95F;
      float headRx = 3.35F;
      float headRy = 2.95F;
      float headRz = 2.8F;
      float legR = 0.72F;
      float legH = 1.15F;
      float legX = 1.2F;
      float legZf = 1.05F;
      float legZb = -1.75F;
      float span = 1.0F;
      float faceEl = 0.0F;
      int nose = -1;
      float noseR = 0.3F;
      float noseEl = -0.14F;
      Pets.Part ears;
      Pets.Part tail;
      Pets.Part headExtra;
      Pets.Part bodyExtra;
   }
}
