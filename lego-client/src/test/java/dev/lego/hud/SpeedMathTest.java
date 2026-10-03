package dev.lego.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpeedMathTest {
   private static final long TICK = 50_000_000L; // 50 ms

   private static SpeedMath run(double perTick, int ticks, boolean horizontal) {
      SpeedMath m = new SpeedMath(1.0);
      for (int i = 0; i <= ticks; i++) m.sample(i * perTick, 64.0 + i * perTick, 0.0, i * TICK, horizontal);
      return m;
   }

   @Test
   void sprintSpeedMatchesVanilla() {
      // Vanilla sprinting moves ~0.2806 blocks per tick on flat ground.
      SpeedMath m = run(0.2806, 40, true);
      assertEquals(5.612, m.metresPerSecond(), 0.01);
      assertEquals(20.2, SpeedMath.convert(m.metresPerSecond(), 0), 0.1);
   }

   @Test
   void horizontalIgnoresVerticalMovement() {
      assertEquals(20.0, run(1.0, 10, true).metresPerSecond(), 1e-6);
      assertEquals(Math.sqrt(2) * 20.0, run(1.0, 10, false).metresPerSecond(), 1e-6);
   }

   @Test
   void usesRealElapsedTime() {
      SpeedMath m = new SpeedMath(1.0);
      m.sample(0, 0, 0, 0L, true);
      m.sample(1, 0, 0, 100_000_000L, true); // 1 block in 100 ms (slowed tick rate)
      assertEquals(10.0, m.metresPerSecond(), 1e-6);
   }

   @Test
   void teleportsResetInsteadOfSpiking() {
      SpeedMath m = new SpeedMath(1.0);
      m.sample(0, 0, 0, 0L, true);
      m.sample(5000, 0, 0, TICK, true);
      assertEquals(0.0, m.metresPerSecond(), 1e-9);
   }

   @Test
   void detectsBraking() {
      SpeedMath m = new SpeedMath(0.6);
      double x = 0;
      long t = 0;
      for (int i = 0; i < 20; i++) { x += 1.0; t += TICK; m.sample(x, 0, 0, t, true); }
      assertFalse(m.braking());
      for (int i = 0; i < 6; i++) { x += 0.1; t += TICK; m.sample(x, 0, 0, t, true); }
      assertTrue(m.braking());
   }

   @Test
   void unitConversions() {
      assertEquals(36.0, SpeedMath.convert(10.0, 0), 1e-9);
      assertEquals(10.0, SpeedMath.convert(10.0, 1), 1e-9);
      assertEquals(22.369, SpeedMath.convert(10.0, 2), 1e-3);
   }
}
