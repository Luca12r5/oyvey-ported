package dev.lego.ui;

import com.mojang.blaze3d.platform.Window;
import dev.lego.LegoClient;
import dev.lego.perf.Perf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class LegoScreen extends Screen implements View.Host {
   private final View view;
   private final Screen parent;
   private long openedAt = 0L;
   private long closeAt = 0L;
   private boolean closing = false;
   private float openAtClose = 1.0F;
   private Runnable afterClose;

   public LegoScreen(View view, Screen parent) {
      super(Component.literal("Lego Client"));
      this.view = view;
      this.parent = parent;
      view.host = this;
   }

   public static void show(View v) {
      Minecraft.getInstance().setScreen(new LegoScreen(v, null));
   }

   public View view() {
      return this.view;
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public boolean shouldCloseOnEsc() {
      return false;
   }

   @Override
   public void renderBackground(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
      if (this.view.blur()) {
         try {
            super.renderBackground(ctx, mouseX, mouseY, delta);
         } catch (Throwable var6) {
         }
      }
   }

   @Override
   public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
      long now = System.currentTimeMillis();
      if (this.openedAt == 0L) {
         this.openedAt = now;
      }

      if (this.closing) {
         float t = (float)(now - this.closeAt) / (this.view.closeMs() / UiSettings.anim());
         this.view.open = Math.max(0.0F, 1.0F - t) * this.openAtClose;
         if (t >= 1.0F) {
            this.finish();
            return;
         }
      } else {
         this.view.open = Math.min(1.0F, (float)(now - this.openedAt) / (this.view.openMs() / UiSettings.anim()));
      }

      McBackend.begin(ctx);
      Perf.begin("Menü");

      try {
         Style.update();
         Kit.frame();
         this.view.layout(this.width * Gx.S, this.height * Gx.S);
         this.updateMouse(mouseX, mouseY);
         if (this.view.backdrop()) {
            if (Halloween.on()) {
               Halloween.backdrop(this.view.W, this.view.H, Math.min(1.0F, this.view.open * 1.4F));
            } else {
               Xmas.backdrop(this.view.W, this.view.H, Math.min(1.0F, this.view.open * 1.4F));
            }
         }

         BgImage.render(this.view.W, this.view.H);
         this.view.draw();
      } catch (Throwable var11) {
         LegoClient.LOG("GUI-Fehler (" + this.view.getClass().getSimpleName() + "): " + var11);
         if (LegoClient.DEBUG) {
            var11.printStackTrace();
         }
      } finally {
         Gx.end();
         Perf.end("Menü");
      }
   }

   private void updateMouse(int mouseX, int mouseY) {
      try {
         Minecraft mc = Minecraft.getInstance();
         Window w = mc.getWindow();
         double fx = (double)w.getWidth() / Math.max(1, w.getScreenWidth());
         double fy = (double)w.getHeight() / Math.max(1, w.getScreenHeight());
         this.view.mx = (float)(mc.mouseHandler.xpos() * fx);
         this.view.my = (float)(mc.mouseHandler.ypos() * fy);
      } catch (Throwable var9) {
         this.view.mx = mouseX * Gx.S;
         this.view.my = mouseY * Gx.S;
      }
   }

   @Override
   public void tick() {
      try {
         this.view.tick();
      } catch (Throwable var2) {
         LegoClient.LOG("GUI-Tick: " + var2);
      }
   }

   @Override
   public void removed() {
      try {
         this.view.removed();
      } catch (Throwable var2) {
      }
   }

   @Override
   public void close() {
      this.closeThen(null);
   }

   @Override
   public void closeThen(Runnable after) {
      if (!this.closing) {
         this.closing = true;
         this.openAtClose = this.view.open;
         this.closeAt = System.currentTimeMillis();
         this.afterClose = after;
      }
   }

   @Override
   public void open(View next) {
      Minecraft.getInstance().setScreen(new LegoScreen(next, this.parent));
   }

   @Override
   public boolean closing() {
      return this.closing;
   }

   private void finish() {
      Minecraft mc = Minecraft.getInstance();
      if (this.afterClose != null) {
         Runnable r = this.afterClose;
         this.afterClose = null;
         mc.setScreen(null);
         r.run();
      } else {
         mc.setScreen(this.parent);
      }
   }

   @Override
   public void onClose() {
      this.close();
   }

   private float px(double v) {
      return (float)(v * Gx.S);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent click, boolean dbl) {
      if (this.closing) {
         return true;
      } else {
         try {
            this.view.mouseDown(this.px(click.x()), this.px(click.y()), click.button(), dbl);
         } catch (Throwable var4) {
            LegoClient.LOG("Klick: " + var4);
         }

         return true;
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent click) {
      try {
         this.view.mouseUp(this.px(click.x()), this.px(click.y()), click.button());
      } catch (Throwable var3) {
         LegoClient.LOG("Klick: " + var3);
      }

      return true;
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent click, double dx, double dy) {
      try {
         this.view.mouseDrag(this.px(click.x()), this.px(click.y()), click.button());
      } catch (Throwable var7) {
         LegoClient.LOG("Ziehen: " + var7);
      }

      return true;
   }

   @Override
   public boolean mouseScrolled(double x, double y, double h, double v) {
      try {
         this.view.scroll(this.px(x), this.px(y), v);
      } catch (Throwable var10) {
         LegoClient.LOG("Scroll: " + var10);
      }

      return true;
   }

   @Override
   public boolean keyPressed(KeyEvent key) {
      int k = key.key();
      if (this.closing) {
         return true;
      } else {
         try {
            if (this.view.key(k, key.modifiers())) {
               return true;
            }

            if (k == 256) {
               this.view.onEscape();
               return true;
            }
         } catch (Throwable var4) {
            LegoClient.LOG("Taste: " + var4);
         }

         return true;
      }
   }

   @Override
   public boolean keyReleased(KeyEvent key) {
      try {
         return this.view.key(-1 - key.key(), key.modifiers());
      } catch (Throwable var3) {
         return false;
      }
   }

   @Override
   public boolean charTyped(CharacterEvent in) {
      try {
         return this.view.chr(in.codepointAsString());
      } catch (Throwable var3) {
         return false;
      }
   }
}
