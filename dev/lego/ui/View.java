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

   public final void layout(int var1, int var2) {
      this.W = var1;
      this.H = var2;
      this.P = Math.max(0.75F, Math.min(this.W / (this.designW() + 40.0F), this.H / (this.designH() + 40.0F)) * (float)UiSettings.uiScale);
   }

   public int p(double var1) {
      return (int)Math.round(var1 * this.P);
   }

   public float pf(double var1) {
      return (float)(var1 * this.P);
   }

   public abstract void draw();

   public boolean mouseDown(float var1, float var2, int var3, boolean var4) {
      return Kit.down(var1, var2, var3);
   }

   public boolean mouseUp(float var1, float var2, int var3) {
      return Kit.up();
   }

   public boolean mouseDrag(float var1, float var2, int var3) {
      return Kit.drag(var1, var2);
   }

   public boolean scroll(float var1, float var2, double var3) {
      return Kit.scroll(var1, var2, var3);
   }

   public boolean key(int var1, int var2) {
      return false;
   }

   public boolean chr(String var1) {
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

   public boolean hover(int var1, int var2, int var3, int var4) {
      return (this.host == null || !this.host.closing()) && this.mx >= var1 && this.my >= var2 && this.mx < var1 + var3 && this.my < var2 + var4;
   }

   public static boolean in(float var0, float var1, int var2, int var3, int var4, int var5) {
      return var0 >= var2 && var1 >= var3 && var0 < var2 + var4 && var1 < var3 + var5;
   }

   public interface Host {
      void close();

      void closeThen(Runnable var1);

      void open(View var1);

      boolean closing();
   }
}
