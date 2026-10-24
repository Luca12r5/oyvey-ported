package dev.lego.ui;

public abstract class View {
   public View.Host host;
   public int W;
   public int H;
   public float mx = -1.0F;
   public float my = -1.0F;
   public float open = 0.0F;
   public float P = 2.0F;

   public float openMs() {
      return 300.0F;
   }

   public float closeMs() {
      return 180.0F;
   }

   public boolean blur() {
      return UiSettings.blur;
   }

   public boolean backdrop() {
      return true;
   }

   public float designW() {
      return 700.0F;
   }

   public float designH() {
      return 430.0F;
   }

   public final void layout(int w, int h) {
      this.W = w;
      this.H = h;
      this.P = Math.max(0.75F, Math.min(this.W / (this.designW() + 40.0F), this.H / (this.designH() + 40.0F)) * (float)UiSettings.uiScale);
   }

   public int p(double u) {
      return (int)Math.round(u * this.P);
   }

   public float pf(double u) {
      return (float)(u * this.P);
   }

   public abstract void draw();

   public boolean mouseDown(float x, float y, int button, boolean dbl) {
      return Kit.down(x, y, button);
   }

   public boolean mouseUp(float x, float y, int button) {
      return Kit.up();
   }

   public boolean mouseDrag(float x, float y, int button) {
      return Kit.drag(x, y);
   }

   public boolean scroll(float x, float y, double amount) {
      return Kit.scroll(x, y, amount);
   }

   public boolean key(int key, int mods) {
      return false;
   }

   public boolean chr(String s) {
      return false;
   }

   public void onEscape() {
      if (this.host != null) {
         this.host.close();
      }
   }

   public void tick() {
   }

   public void removed() {
   }

   public boolean hover(int rx, int ry, int rw, int rh) {
      return (this.host == null || !this.host.closing()) && this.mx >= rx && this.my >= ry && this.mx < rx + rw && this.my < ry + rh;
   }

   public static boolean in(float x, float y, int rx, int ry, int rw, int rh) {
      return x >= rx && y >= ry && x < rx + rw && y < ry + rh;
   }

   public interface Host {
      void close();

      void closeThen(Runnable var1);

      void open(View var1);

      boolean closing();
   }
}
