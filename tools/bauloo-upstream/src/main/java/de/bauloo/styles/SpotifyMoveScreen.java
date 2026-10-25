package de.bauloo.styles;

import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class SpotifyMoveScreen extends class_437 {
   private final class_437 parent;
   private boolean dragging;
   private double grabX;
   private double grabY;

   public SpotifyMoveScreen(class_437 parent) {
      super(class_2561.method_43470("Move music card"));
      this.parent = parent;
   }

   public void method_25394(class_332 ctx, int mouseX, int mouseY, float delta) {
      ctx.method_25294(0, 0, this.field_22789, this.field_22790, 1711276032);
      int x = SpotifyHud.x(this.field_22789);
      int y = SpotifyHud.y(this.field_22790);
      String title = SpotifyNowPlaying.title != null ? SpotifyNowPlaying.title : "Song name";
      String artist = SpotifyNowPlaying.title != null ? SpotifyNowPlaying.artist : "Artist";
      SpotifyHud.drawScaled(ctx, this.field_22793, x, y, title, artist, 1.0F);
      int edge = !this.dragging && !this.inside(mouseX, mouseY) ? -1996488705 : -1;
      ctx.method_25294(x - 2, y - 2, x + SpotifyHud.w() + 2, y - 1, edge);
      ctx.method_25294(x - 2, y + SpotifyHud.h() + 1, x + SpotifyHud.w() + 2, y + SpotifyHud.h() + 2, edge);
      ctx.method_25294(x - 2, y - 1, x - 1, y + SpotifyHud.h() + 1, edge);
      ctx.method_25294(x + SpotifyHud.w() + 1, y - 1, x + SpotifyHud.w() + 2, y + SpotifyHud.h() + 1, edge);
      ctx.method_25300(
         this.field_22793,
         "Drag to move.  Scroll = size (" + Math.round(StylesUiConfig.spotifyScale * 100.0F) + "%).  Esc = save,  R = reset",
         this.field_22789 / 2,
         this.field_22790 - 24,
         -1
      );
   }

   private boolean inside(double mx, double my) {
      int x = SpotifyHud.x(this.field_22789);
      int y = SpotifyHud.y(this.field_22790);
      return mx >= x && mx < x + SpotifyHud.w() && my >= y && my < y + SpotifyHud.h();
   }

   public boolean method_25402(class_11909 click, boolean doubled) {
      if (this.inside(click.comp_4798(), click.comp_4799())) {
         this.dragging = true;
         this.grabX = click.comp_4798() - SpotifyHud.x(this.field_22789);
         this.grabY = click.comp_4799() - SpotifyHud.y(this.field_22790);
         return true;
      } else {
         return super.method_25402(click, doubled);
      }
   }

   public boolean method_25403(class_11909 click, double offsetX, double offsetY) {
      if (!this.dragging) {
         return super.method_25403(click, offsetX, offsetY);
      }

      int freeW = Math.max(1, this.field_22789 - SpotifyHud.w());
      int freeH = Math.max(1, this.field_22790 - SpotifyHud.h());
      StylesUiConfig.spotifyX = (float)Math.max(0.0, Math.min(1.0, (click.comp_4798() - this.grabX) / freeW));
      StylesUiConfig.spotifyY = (float)Math.max(0.0, Math.min(1.0, (click.comp_4799() - this.grabY) / freeH));
      return true;
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontal, double vertical) {
      if (vertical == 0.0) {
         return false;
      }

      float next = StylesUiConfig.spotifyScale + (vertical > 0.0 ? 0.05F : -0.05F);
      StylesUiConfig.spotifyScale = Math.round(Math.max(0.5F, Math.min(1.5F, next)) * 20.0F) / 20.0F;
      return true;
   }

   public boolean method_25406(class_11909 click) {
      this.dragging = false;
      return super.method_25406(click);
   }

   public boolean method_25404(class_11908 input) {
      if (input.comp_4795() == 82) {
         StylesUiConfig.spotifyX = -1.0F;
         StylesUiConfig.spotifyY = -1.0F;
         StylesUiConfig.spotifyScale = 1.0F;
         return true;
      } else {
         return super.method_25404(input);
      }
   }

   public void method_25419() {
      StylesUiConfig.save();
      class_310.method_1551().method_1507(this.parent);
   }

   public boolean method_25421() {
      return false;
   }
}
