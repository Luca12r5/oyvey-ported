package dev.lego.hud;

/**
 * Speed measurement from real movement, independent of Minecraft classes so it
 * can be unit tested.
 *
 * Assumptions (documented for calibration): one block is one metre, and the
 * speed is position change divided by real elapsed time between client ticks
 * (not by the nominal 50 ms), so it stays correct if the tick rate changes.
 * Sprinting on flat ground measures about 5.6 m/s (≈ 20 km/h), walking about
 * 4.3 m/s (≈ 15.5 km/h), which matches the vanilla movement speeds.
 */
public final class SpeedMath {
   public static final double KMH_PER_MS = 3.6;
   public static final double MPH_PER_MS = 2.2369362920544;

   private double lastX;
   private double lastY;
   private double lastZ;
   private long lastNanos = -1L;
   private double speed;      // smoothed m/s
   private double accel;      // smoothed m/s²
   private double peak;
   private final double alpha;

   public SpeedMath(double smoothing) {
      this.alpha = Math.max(0.05, Math.min(1.0, smoothing));
   }

   /** Feeds a new position sample. Teleports (> 100 m in one sample) reset the meter. */
   public void sample(double x, double y, double z, long nanos, boolean horizontalOnly) {
      if (this.lastNanos < 0L) {
         this.set(x, y, z, nanos);
         return;
      }
      double dt = (nanos - this.lastNanos) / 1.0E9;
      if (dt <= 0.0) return;
      double dx = x - this.lastX;
      double dy = horizontalOnly ? 0.0 : y - this.lastY;
      double dz = z - this.lastZ;
      double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      this.set(x, y, z, nanos);
      if (dist > 100.0 || dt > 1.0) {
         this.speed = 0.0;
         this.accel = 0.0;
         return;
      }
      double raw = dist / dt;
      double prev = this.speed;
      this.speed += (raw - this.speed) * this.alpha;
      double rawAccel = (this.speed - prev) / dt;
      this.accel += (rawAccel - this.accel) * this.alpha;
      this.peak = Math.max(this.peak, this.speed);
   }

   private void set(double x, double y, double z, long nanos) {
      this.lastX = x;
      this.lastY = y;
      this.lastZ = z;
      this.lastNanos = nanos;
   }

   public void reset() {
      this.lastNanos = -1L;
      this.speed = 0.0;
      this.accel = 0.0;
      this.peak = 0.0;
   }

   public double metresPerSecond() {
      return this.speed < 0.01 ? 0.0 : this.speed;
   }

   public double accel() {
      return Math.abs(this.accel) < 0.05 ? 0.0 : this.accel;
   }

   public double peak() {
      return this.peak;
   }

   public boolean braking() {
      return this.accel < -1.0 && this.speed > 0.5;
   }

   public static double convert(double ms, int unit) {
      return switch (unit) {
         case 1 -> ms;
         case 2 -> ms * MPH_PER_MS;
         default -> ms * KMH_PER_MS;
      };
   }

   public static String unitName(int unit) {
      return switch (unit) {
         case 1 -> "m/s";
         case 2 -> "mph";
         default -> "km/h";
      };
   }
}
