package de.bauloo.styles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class SpotifyMoveScreen extends Screen {
   private final Screen parent;
   private boolean dragging;
   private double grabX;
   private double grabY;

   public SpotifyMoveScreen(Screen parent) {
      super(Component.literal("Move music card"));
      this.parent = parent;
   }

   public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
      ctx.fill(0, 0, this.width, this.height, 1711276032);
      int x = SpotifyHud.x(this.width);
      int y = SpotifyHud.y(this.height);
      String title = SpotifyNowPlaying.title != null ? SpotifyNowPlaying.title : "Song name";
      String artist = SpotifyNowPlaying.title != null ? SpotifyNowPlaying.artist : "Artist";
      SpotifyHud.drawScaled(ctx, this.font, x, y, title, artist, 1.0F);
      int edge = !this.dragging && !this.inside(mouseX, mouseY) ? -1996488705 : -1;
      ctx.fill(x - 2, y - 2, x + SpotifyHud.w() + 2, y - 1, edge);
      ctx.fill(x - 2, y + SpotifyHud.h() + 1, x + SpotifyHud.w() + 2, y + SpotifyHud.h() + 2, edge);
      ctx.fill(x - 2, y - 1, x - 1, y + SpotifyHud.h() + 1, edge);
      ctx.fill(x + SpotifyHud.w() + 1, y - 1, x + SpotifyHud.w() + 2, y + SpotifyHud.h() + 1, edge);
      ctx.centeredText(
         this.font,
         "Drag to move.  Scroll = size (" + Math.round(StylesUiConfig.spotifyScale * 100.0F) + "%).  Esc = save,  R = reset",
         this.width / 2,
         this.height - 24,
         -1
      );
   }

   private boolean inside(double mx, double my) {
      int x = SpotifyHud.x(this.width);
      int y = SpotifyHud.y(this.height);
      return mx >= x && mx < x + SpotifyHud.w() && my >= y && my < y + SpotifyHud.h();
   }

   public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
      if (this.inside(click.x(), click.y())) {
         this.dragging = true;
         this.grabX = click.x() - SpotifyHud.x(this.width);
         this.grabY = click.y() - SpotifyHud.y(this.height);
         return true;
      } else {
         return super.mouseClicked(click, doubled);
      }
   }

   public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
      if (!this.dragging) {
         return super.mouseDragged(click, offsetX, offsetY);
      } else {
         int freeW = Math.max(1, this.width - SpotifyHud.w());
         int freeH = Math.max(1, this.height - SpotifyHud.h());
         StylesUiConfig.spotifyX = (float)Math.max(0.0, Math.min(1.0, (click.x() - this.grabX) / freeW));
         StylesUiConfig.spotifyY = (float)Math.max(0.0, Math.min(1.0, (click.y() - this.grabY) / freeH));
         return true;
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
      if (vertical == 0.0) {
         return false;
      } else {
         float next = StylesUiConfig.spotifyScale + (vertical > 0.0 ? 0.05F : -0.05F);
         StylesUiConfig.spotifyScale = Math.round(Math.max(0.5F, Math.min(1.5F, next)) * 20.0F) / 20.0F;
         return true;
      }
   }

   public boolean mouseReleased(MouseButtonEvent click) {
      this.dragging = false;
      return super.mouseReleased(click);
   }

   public boolean keyPressed(KeyEvent input) {
      if (input.key() == 82) {
         StylesUiConfig.spotifyX = -1.0F;
         StylesUiConfig.spotifyY = -1.0F;
         StylesUiConfig.spotifyScale = 1.0F;
         return true;
      } else {
         return super.keyPressed(input);
      }
   }

   public void onClose() {
      StylesUiConfig.save();
      Minecraft.getInstance().gui.setScreen(this.parent);
   }

   public boolean isPauseScreen() {
      return false;
   }
}
