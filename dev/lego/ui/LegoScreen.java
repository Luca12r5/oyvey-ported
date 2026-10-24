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

   public LegoScreen(View var1, Screen var2) {
      super(Component.literal("Lego Client"));
      this.view = var1;
      this.parent = var2;
      var1.host = this;
   }

   public static void show(View var0) {
      Minecraft.getInstance().setScreen(new LegoScreen(var0, null));
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
   public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
      if (this.view.blur()) {
         try {
            super.renderBackground(var1, var2, var3, var4);
         } catch (Throwable var6) {
         }
      }
   }

   @Override
   public void render(GuiGraphics var1, int var2, int var3, float var4) {
      long var5 = System.currentTimeMillis();
      if (this.openedAt == 0L) {
         this.openedAt = var5;
      }

      if (this.closing) {
         float var7 = (float)(var5 - this.closeAt) / (this.view.closeMs() / UiSettings.anim());
         this.view.open = Math.max(0.0F, 1.0F - var7) * this.openAtClose;
         if (var7 >= 1.0F) {
            this.finish();
            return;
         }
      } else {
         this.view.open = Math.min(1.0F, (float)(var5 - this.openedAt) / (this.view.openMs() / UiSettings.anim()));
      }

      McBackend.begin(var1);
      Perf.begin("Menü");

      try {
         Style.update();
         Kit.frame();
         this.view.layout(this.width * Gx.S, this.height * Gx.S);
         this.updateMouse(var2, var3);
         if (this.view.backdrop()) {
            Xmas.backdrop(this.view.W, this.view.H, Math.min(1.0F, this.view.open * 1.4F));
         }

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

   private void updateMouse(int var1, int var2) {
      try {
         Minecraft var3 = Minecraft.getInstance();
         Window var4 = var3.getWindow();
         double var5 = (double)var4.getWidth() / Math.max(1, var4.getScreenWidth());
         double var7 = (double)var4.getHeight() / Math.max(1, var4.getScreenHeight());
         this.view.mx = (float)(var3.mouseHandler.xpos() * var5);
         this.view.my = (float)(var3.mouseHandler.ypos() * var7);
      } catch (Throwable var9) {
         this.view.mx = var1 * Gx.S;
         this.view.my = var2 * Gx.S;
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
   public void closeThen(Runnable var1) {
      if (!this.closing) {
         this.closing = true;
         this.openAtClose = this.view.open;
         this.closeAt = System.currentTimeMillis();
         this.afterClose = var1;
      }
   }

   @Override
   public void open(View var1) {
      Minecraft.getInstance().setScreen(new LegoScreen(var1, this.parent));
   }

   @Override
   public boolean closing() {
      return this.closing;
   }

   private void finish() {
      Minecraft var1 = Minecraft.getInstance();
      if (this.afterClose != null) {
         Runnable var2 = this.afterClose;
         this.afterClose = null;
         var1.setScreen(null);
         var2.run();
      } else {
         var1.setScreen(this.parent);
      }
   }

   @Override
   public void onClose() {
      this.close();
   }

   private float px(double var1) {
      return (float)(var1 * Gx.S);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent var1, boolean var2) {
      if (this.closing) {
         return true;
      } else {
         try {
            this.view.mouseDown(this.px(var1.x()), this.px(var1.y()), var1.button(), var2);
         } catch (Throwable var4) {
            LegoClient.LOG("Klick: " + var4);
         }

         return true;
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent var1) {
      try {
         this.view.mouseUp(this.px(var1.x()), this.px(var1.y()), var1.button());
      } catch (Throwable var3) {
         LegoClient.LOG("Klick: " + var3);
      }

      return true;
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent var1, double var2, double var4) {
      try {
         this.view.mouseDrag(this.px(var1.x()), this.px(var1.y()), var1.button());
      } catch (Throwable var7) {
         LegoClient.LOG("Ziehen: " + var7);
      }

      return true;
   }

   @Override
   public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
      try {
         this.view.scroll(this.px(var1), this.px(var3), var7);
      } catch (Throwable var10) {
         LegoClient.LOG("Scroll: " + var10);
      }

      return true;
   }

   @Override
   public boolean keyPressed(KeyEvent var1) {
      int var2 = var1.key();
      if (this.closing) {
         return true;
      } else {
         try {
            if (this.view.key(var2, var1.modifiers())) {
               return true;
            }

            if (var2 == 256) {
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
   public boolean keyReleased(KeyEvent var1) {
      try {
         return this.view.key(-1 - var1.key(), var1.modifiers());
      } catch (Throwable var3) {
         return false;
      }
   }

   @Override
   public boolean charTyped(CharacterEvent var1) {
      try {
         return this.view.chr(var1.codepointAsString());
      } catch (Throwable var3) {
         return false;
      }
   }
}
