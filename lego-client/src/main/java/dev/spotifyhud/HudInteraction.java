package dev.spotifyhud;

import net.minecraft.client.gui.GuiGraphics;

final class HudInteraction {
   private final Config cfg;
   private final HudRenderer hud;
   private HudInteraction.Drag drag = HudInteraction.Drag.NONE;
   private double offX;
   private double offY;
   private double downX;
   private double downY;
   private double previewProgress = -1.0;
   private int previewVolume = -1;

   HudInteraction(Config cfg, HudRenderer hud) {
      this.cfg = cfg;
      this.hud = hud;
   }

   boolean dragging() {
      return this.drag != HudInteraction.Drag.NONE;
   }

   private int[] hb(int sw, int sh) {
      return this.hud.bounds(sw, sh, this.cfg);
   }

   private int[] lb(int[] hb, int sw, int sh) {
      return SpotifyHudMod.lyricsVisible() ? this.hud.lyricsBounds(hb, sw, sh, this.cfg) : null;
   }

   private static boolean inside(double mx, double my, int[] b) {
      return b != null && mx >= b[0] && my >= b[1] && mx < b[0] + b[2] && my < b[1] + b[3];
   }

   static HudLayout.Region regionAt(double mx, double my, int[] b) {
      if (!inside(mx, my, b)) {
         return null;
      } else {
         double dx = (mx - b[0]) * 207.0 / b[2];
         double dy = (my - b[1]) * 70.0 / b[3];
         return HudLayout.hit(dx, dy);
      }
   }

   private static double designX(double mx, int[] b) {
      return (mx - b[0]) * 207.0 / b[2];
   }

   boolean isOver(double mx, double my, int sw, int sh) {
      int[] h = this.hb(sw, sh);
      return inside(mx, my, h) || inside(mx, my, this.lb(h, sw, sh));
   }

   boolean mouseDown(double mx, double my, int button, int sw, int sh) {
      if (!this.cfg.visible) {
         return false;
      } else {
         int[] h = this.hb(sw, sh);
         int[] l = this.lb(h, sw, sh);
         boolean onLyrics = inside(mx, my, l);
         HudLayout.Region r = regionAt(mx, my, h);
         if (r == null && !onLyrics) {
            return false;
         } else if (button == 1) {
            SpotifyHudMod.toggleLyrics();
            return true;
         } else if (button != 0) {
            return true;
         } else {
            Media media = SpotifyHudMod.media();
            if (onLyrics) {
               r = HudLayout.Region.PANEL;
            }

            switch (r) {
               case PREV:
                  media.previous();
                  break;
               case PLAY:
                  media.togglePlay();
                  break;
               case NEXT:
                  media.next();
                  break;
               case REPEAT:
                  media.cycleRepeat();
                  break;
               case SHUFFLE:
                  media.toggleShuffle();
                  break;
               case LIKE:
                  media.toggleLike();
                  break;
               case SETTINGS:
                  SpotifyHudMod.requestSettings();
                  break;
               case SPEAKER:
                  media.toggleMute();
                  break;
               case PROGRESS:
                  PlayerState st = media.state();
                  if (st.hasTrack && st.durationMs > 0L) {
                     this.drag = HudInteraction.Drag.SEEK;
                     this.previewProgress = HudLayout.progressFraction(designX(mx, h));
                  } else {
                     this.startMove(mx, my, h);
                  }
                  break;
               case VOLUME:
                  this.drag = HudInteraction.Drag.VOLUME;
                  this.previewVolume = (int)Math.round(HudLayout.volumeFraction(designX(mx, h)) * 100.0);
                  break;
               default:
                  this.startMove(mx, my, h);
            }

            return true;
         }
      }
   }

   private void startMove(double mx, double my, int[] h) {
      this.drag = HudInteraction.Drag.PENDING_MOVE;
      this.offX = mx - h[0];
      this.offY = my - h[1];
      this.downX = mx;
      this.downY = my;
   }

   boolean mouseDrag(double mx, double my, int sw, int sh) {
      if (this.drag == HudInteraction.Drag.NONE) {
         return false;
      } else {
         int[] h = this.hb(sw, sh);
         switch (this.drag) {
            case PENDING_MOVE:
               if (Math.abs(mx - this.downX) + Math.abs(my - this.downY) < 2.0) {
                  break;
               }

               this.drag = HudInteraction.Drag.MOVE;
            case MOVE:
               int freeX = sw - h[2];
               int freeY = sh - h[3];
               this.cfg.posX = freeX > 0 ? Config.clamp01((mx - this.offX) / freeX) : 0.5;
               this.cfg.posY = freeY > 0 ? Config.clamp01((my - this.offY) / freeY) : 0.0;
               break;
            case SEEK:
               this.previewProgress = HudLayout.progressFraction(designX(mx, h));
               break;
            case VOLUME:
               this.previewVolume = (int)Math.round(HudLayout.volumeFraction(designX(mx, h)) * 100.0);
         }

         return true;
      }
   }

   boolean mouseUp() {
      if (this.drag == HudInteraction.Drag.NONE) {
         return false;
      } else {
         Media media = SpotifyHudMod.media();
         switch (this.drag) {
            case MOVE:
               this.cfg.save();
               break;
            case SEEK:
               if (this.previewProgress >= 0.0) {
                  media.seek((long)(this.previewProgress * media.state().durationMs));
               }
               break;
            case VOLUME:
               if (this.previewVolume >= 0) {
                  media.setVolume(this.previewVolume, true);
               }
         }

         this.drag = HudInteraction.Drag.NONE;
         this.previewProgress = -1.0;
         this.previewVolume = -1;
         return true;
      }
   }

   boolean scroll(double mx, double my, double vertical, int sw, int sh) {
      if (this.cfg.visible && vertical != 0.0 && this.isOver(mx, my, sw, sh)) {
         int[] h = this.hb(sw, sh);
         HudLayout.Region r = regionAt(mx, my, h);
         if (r == HudLayout.Region.VOLUME) {
            Media media = SpotifyHudMod.media();
            int cur = media.state().volume;
            if (cur >= 0) {
               media.setVolume(cur + (vertical > 0.0 ? 5 : -5), false);
            }

            return true;
         } else {
            double relX = (mx - h[0]) / h[2];
            double relY = (my - h[1]) / h[3];
            double ns = Math.round(Math.max(0.5, Math.min(3.0, this.cfg.scale + (vertical > 0.0 ? 0.05 : -0.05))) * 100.0) / 100.0;
            if (ns == this.cfg.scale) {
               return true;
            } else {
               this.cfg.scale = ns;
               int[] n = this.hb(sw, sh);
               double newX = mx - relX * n[2];
               double newY = my - relY * n[3];
               int freeX = sw - n[2];
               int freeY = sh - n[3];
               this.cfg.posX = freeX > 0 ? Config.clamp01(newX / freeX) : 0.5;
               this.cfg.posY = freeY > 0 ? Config.clamp01(newY / freeY) : 0.0;
               SpotifyHudMod.scheduleSave();
               return true;
            }
         }
      } else {
         return false;
      }
   }

   void render(GuiGraphics ctx, int mx, int my, int sw, int sh) {
      if (this.cfg.visible) {
         int[] h = this.hb(sw, sh);
         int[] l = this.lb(h, sw, sh);
         HudLayout.Region hover = null;
         boolean lyricsHover = false;
         if (this.drag == HudInteraction.Drag.NONE || this.drag == HudInteraction.Drag.PENDING_MOVE) {
            hover = regionAt(mx, my, h);
            lyricsHover = inside(mx, my, l);
         } else if (this.drag == HudInteraction.Drag.MOVE) {
            hover = HudLayout.Region.PANEL;
            lyricsHover = true;
         }

         SpotifyHudMod.drawAll(ctx, sw, sh, true, hover, lyricsHover, this.previewProgress, this.previewVolume);
      }
   }

   private static enum Drag {
      NONE,
      PENDING_MOVE,
      MOVE,
      SEEK,
      VOLUME;
   }
}
