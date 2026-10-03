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

   HudInteraction(Config var1, HudRenderer var2) {
      this.cfg = var1;
      this.hud = var2;
   }

   boolean dragging() {
      return this.drag != HudInteraction.Drag.NONE;
   }

   private int[] hb(int var1, int var2) {
      return this.hud.bounds(var1, var2, this.cfg);
   }

   private int[] lb(int[] var1, int var2, int var3) {
      return SpotifyHudMod.lyricsVisible() ? this.hud.lyricsBounds(var1, var2, var3, this.cfg) : null;
   }

   private static boolean inside(double var0, double var2, int[] var4) {
      return var4 != null && var0 >= var4[0] && var2 >= var4[1] && var0 < var4[0] + var4[2] && var2 < var4[1] + var4[3];
   }

   static HudLayout.Region regionAt(double var0, double var2, int[] var4) {
      if (!inside(var0, var2, var4)) {
         return null;
      } else {
         double var5 = (var0 - var4[0]) * 207.0 / var4[2];
         double var7 = (var2 - var4[1]) * 70.0 / var4[3];
         return HudLayout.hit(var5, var7);
      }
   }

   private static double designX(double var0, int[] var2) {
      return (var0 - var2[0]) * 207.0 / var2[2];
   }

   boolean isOver(double var1, double var3, int var5, int var6) {
      int[] var7 = this.hb(var5, var6);
      return inside(var1, var3, var7) || inside(var1, var3, this.lb(var7, var5, var6));
   }

   boolean mouseDown(double var1, double var3, int var5, int var6, int var7) {
      if (!this.cfg.visible) {
         return false;
      } else {
         int[] var8 = this.hb(var6, var7);
         int[] var9 = this.lb(var8, var6, var7);
         boolean var10 = inside(var1, var3, var9);
         HudLayout.Region var11 = regionAt(var1, var3, var8);
         if (var11 == null && !var10) {
            return false;
         } else if (var5 == 1) {
            SpotifyHudMod.toggleLyrics();
            return true;
         } else if (var5 != 0) {
            return true;
         } else {
            Media var12 = SpotifyHudMod.media();
            if (var10) {
               var11 = HudLayout.Region.PANEL;
            }

            switch (var11) {
               case PREV:
                  var12.previous();
                  break;
               case PLAY:
                  var12.togglePlay();
                  break;
               case NEXT:
                  var12.next();
                  break;
               case REPEAT:
                  var12.cycleRepeat();
                  break;
               case SHUFFLE:
                  var12.toggleShuffle();
                  break;
               case LIKE:
                  var12.toggleLike();
                  break;
               case SETTINGS:
                  SpotifyHudMod.requestSettings();
                  break;
               case SPEAKER:
                  var12.toggleMute();
                  break;
               case PROGRESS:
                  PlayerState var13 = var12.state();
                  if (var13.hasTrack && var13.durationMs > 0L) {
                     this.drag = HudInteraction.Drag.SEEK;
                     this.previewProgress = HudLayout.progressFraction(designX(var1, var8));
                  } else {
                     this.startMove(var1, var3, var8);
                  }
                  break;
               case VOLUME:
                  this.drag = HudInteraction.Drag.VOLUME;
                  this.previewVolume = (int)Math.round(HudLayout.volumeFraction(designX(var1, var8)) * 100.0);
                  break;
               default:
                  this.startMove(var1, var3, var8);
            }

            return true;
         }
      }
   }

   private void startMove(double var1, double var3, int[] var5) {
      this.drag = HudInteraction.Drag.PENDING_MOVE;
      this.offX = var1 - var5[0];
      this.offY = var3 - var5[1];
      this.downX = var1;
      this.downY = var3;
   }

   boolean mouseDrag(double var1, double var3, int var5, int var6) {
      if (this.drag == HudInteraction.Drag.NONE) {
         return false;
      } else {
         int[] var7 = this.hb(var5, var6);
         switch (this.drag) {
            case PENDING_MOVE:
               if (Math.abs(var1 - this.downX) + Math.abs(var3 - this.downY) < 2.0) {
                  break;
               }

               this.drag = HudInteraction.Drag.MOVE;
            case MOVE:
               int var8 = var5 - var7[2];
               int var9 = var6 - var7[3];
               this.cfg.posX = var8 > 0 ? Config.clamp01((var1 - this.offX) / var8) : 0.5;
               this.cfg.posY = var9 > 0 ? Config.clamp01((var3 - this.offY) / var9) : 0.0;
               break;
            case SEEK:
               this.previewProgress = HudLayout.progressFraction(designX(var1, var7));
               break;
            case VOLUME:
               this.previewVolume = (int)Math.round(HudLayout.volumeFraction(designX(var1, var7)) * 100.0);
         }

         return true;
      }
   }

   boolean mouseUp() {
      if (this.drag == HudInteraction.Drag.NONE) {
         return false;
      } else {
         Media var1 = SpotifyHudMod.media();
         switch (this.drag) {
            case MOVE:
               this.cfg.save();
               break;
            case SEEK:
               if (this.previewProgress >= 0.0) {
                  var1.seek((long)(this.previewProgress * var1.state().durationMs));
               }
               break;
            case VOLUME:
               if (this.previewVolume >= 0) {
                  var1.setVolume(this.previewVolume, true);
               }
         }

         this.drag = HudInteraction.Drag.NONE;
         this.previewProgress = -1.0;
         this.previewVolume = -1;
         return true;
      }
   }

   boolean scroll(double var1, double var3, double var5, int var7, int var8) {
      if (this.cfg.visible && var5 != 0.0 && this.isOver(var1, var3, var7, var8)) {
         int[] var9 = this.hb(var7, var8);
         HudLayout.Region var10 = regionAt(var1, var3, var9);
         if (var10 == HudLayout.Region.VOLUME) {
            Media var24 = SpotifyHudMod.media();
            int var12 = var24.state().volume;
            if (var12 >= 0) {
               var24.setVolume(var12 + (var5 > 0.0 ? 5 : -5), false);
            }

            return true;
         } else {
            double var11 = (var1 - var9[0]) / var9[2];
            double var13 = (var3 - var9[1]) / var9[3];
            double var15 = Math.round(Math.max(0.5, Math.min(3.0, this.cfg.scale + (var5 > 0.0 ? 0.05 : -0.05))) * 100.0) / 100.0;
            if (var15 == this.cfg.scale) {
               return true;
            } else {
               this.cfg.scale = var15;
               int[] var17 = this.hb(var7, var8);
               double var18 = var1 - var11 * var17[2];
               double var20 = var3 - var13 * var17[3];
               int var22 = var7 - var17[2];
               int var23 = var8 - var17[3];
               this.cfg.posX = var22 > 0 ? Config.clamp01(var18 / var22) : 0.5;
               this.cfg.posY = var23 > 0 ? Config.clamp01(var20 / var23) : 0.0;
               SpotifyHudMod.scheduleSave();
               return true;
            }
         }
      } else {
         return false;
      }
   }

   void render(GuiGraphics var1, int var2, int var3, int var4, int var5) {
      if (this.cfg.visible) {
         int[] var6 = this.hb(var4, var5);
         int[] var7 = this.lb(var6, var4, var5);
         HudLayout.Region var8 = null;
         boolean var9 = false;
         if (this.drag == HudInteraction.Drag.NONE || this.drag == HudInteraction.Drag.PENDING_MOVE) {
            var8 = regionAt(var2, var3, var6);
            var9 = inside(var2, var3, var7);
         } else if (this.drag == HudInteraction.Drag.MOVE) {
            var8 = HudLayout.Region.PANEL;
            var9 = true;
         }

         SpotifyHudMod.drawAll(var1, var4, var5, true, var8, var9, this.previewProgress, this.previewVolume);
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
