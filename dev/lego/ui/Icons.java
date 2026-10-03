package dev.lego.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D.Double;

public final class Icons {
   private static Graphics2D g;
   private static double k;

   private Icons() {
   }

   private static void stroke(double var0) {
      g.setStroke(new BasicStroke((float)(var0 * k), 1, 1));
   }

   private static void line(double var0, double var2, double var4, double var6) {
      g.draw(new Double(var0 * k, var2 * k, var4 * k, var6 * k));
   }

   private static void circ(double var0, double var2, double var4) {
      g.draw(new java.awt.geom.Ellipse2D.Double((var0 - var4) * k, (var2 - var4) * k, 2.0 * var4 * k, 2.0 * var4 * k));
   }

   private static void disc(double var0, double var2, double var4) {
      g.fill(new java.awt.geom.Ellipse2D.Double((var0 - var4) * k, (var2 - var4) * k, 2.0 * var4 * k, 2.0 * var4 * k));
   }

   private static void rr(double var0, double var2, double var4, double var6, double var8) {
      g.draw(new java.awt.geom.RoundRectangle2D.Double(var0 * k, var2 * k, var4 * k, var6 * k, 2.0 * var8 * k, 2.0 * var8 * k));
   }

   private static void rrf(double var0, double var2, double var4, double var6, double var8) {
      g.fill(new java.awt.geom.RoundRectangle2D.Double(var0 * k, var2 * k, var4 * k, var6 * k, 2.0 * var8 * k, 2.0 * var8 * k));
   }

   private static void arc(double var0, double var2, double var4, double var6, double var8) {
      g.draw(new java.awt.geom.Arc2D.Double((var0 - var4) * k, (var2 - var4) * k, 2.0 * var4 * k, 2.0 * var4 * k, var6, var8, 0));
   }

   private static java.awt.geom.Path2D.Double p(double... var0) {
      java.awt.geom.Path2D.Double var1 = new java.awt.geom.Path2D.Double();
      var1.moveTo(var0[0] * k, var0[1] * k);

      for (byte var2 = 2; var2 < var0.length; var2 += 2) {
         var1.lineTo(var0[var2] * k, var0[var2 + 1] * k);
      }

      return var1;
   }

   private static void poly(double... var0) {
      g.draw(p(var0));
   }

   private static void polyC(double... var0) {
      java.awt.geom.Path2D.Double var1 = p(var0);
      var1.closePath();
      g.draw(var1);
   }

   private static void polyF(double... var0) {
      java.awt.geom.Path2D.Double var1 = p(var0);
      var1.closePath();
      g.fill(var1);
   }

   public static void paint(Graphics2D var0, String var1, int var2, Color var3) {
      g = var0;
      k = var2 / 24.0;
      g.setColor(var3);
      stroke(2.0);
      switch (var1) {
         case "mods":
         case "grid":
            rr(3.0, 3.0, 7.0, 7.0, 1.5);
            rr(14.0, 3.0, 7.0, 7.0, 1.5);
            rr(3.0, 14.0, 7.0, 7.0, 1.5);
            rr(14.0, 14.0, 7.0, 7.0, 1.5);
            break;
         case "shirt":
         case "cosmetics":
            polyC(8.0, 3.0, 4.0, 5.0, 2.0, 10.0, 5.5, 11.5, 6.0, 21.0, 18.0, 21.0, 18.5, 11.5, 22.0, 10.0, 20.0, 5.0, 16.0, 3.0, 14.5, 5.0, 9.5, 5.0);
            break;
         case "emote":
         case "dance":
            circ(13.0, 4.0, 2.0);
            poly(4.0, 11.0, 9.0, 8.0, 14.0, 9.0, 17.0, 13.0);
            poly(12.0, 9.0, 10.0, 15.0, 14.0, 18.0, 14.0, 22.0);
            poly(10.0, 15.0, 6.0, 22.0);
            poly(17.0, 13.0, 21.0, 12.0);
            break;
         case "gamepad":
         case "games":
            g.draw(new java.awt.geom.RoundRectangle2D.Double(2.0 * k, 7.0 * k, 20.0 * k, 11.0 * k, 9.0 * k, 9.0 * k));
            line(6.5, 12.5, 10.5, 12.5);
            line(8.5, 10.5, 8.5, 14.5);
            disc(15.0, 11.5, 1.1);
            disc(18.0, 13.5, 1.1);
            break;
         case "layout":
         case "hud":
            rr(3.0, 3.0, 18.0, 18.0, 2.5);
            line(3.0, 9.0, 21.0, 9.0);
            line(9.0, 9.0, 9.0, 21.0);
            break;
         case "palette":
         case "design":
            g.draw(new java.awt.geom.Path2D.Double() {
               {
                  this.moveTo(12.0 * Icons.k, 3.0 * Icons.k);
                  this.curveTo(6.5 * Icons.k, 3.0 * Icons.k, 2.5 * Icons.k, 7.0 * Icons.k, 2.5 * Icons.k, 12.0 * Icons.k);
                  this.curveTo(2.5 * Icons.k, 17.5 * Icons.k, 7.0 * Icons.k, 21.5 * Icons.k, 12.0 * Icons.k, 21.5 * Icons.k);
                  this.curveTo(13.5 * Icons.k, 21.5 * Icons.k, 14.0 * Icons.k, 20.5 * Icons.k, 13.5 * Icons.k, 19.3 * Icons.k);
                  this.curveTo(12.8 * Icons.k, 17.6 * Icons.k, 13.8 * Icons.k, 16.0 * Icons.k, 15.5 * Icons.k, 16.0 * Icons.k);
                  this.lineTo(17.5 * Icons.k, 16.0 * Icons.k);
                  this.curveTo(20.0 * Icons.k, 16.0 * Icons.k, 21.5 * Icons.k, 14.5 * Icons.k, 21.5 * Icons.k, 12.0 * Icons.k);
                  this.curveTo(21.5 * Icons.k, 7.0 * Icons.k, 17.5 * Icons.k, 3.0 * Icons.k, 12.0 * Icons.k, 3.0 * Icons.k);
               }
            });
            disc(7.5, 11.0, 1.3);
            disc(10.5, 7.0, 1.3);
            disc(15.5, 7.5, 1.3);
            break;
         case "search":
            circ(10.5, 10.5, 6.5);
            line(15.5, 15.5, 21.0, 21.0);
            break;
         case "x":
         case "close":
            line(6.0, 6.0, 18.0, 18.0);
            line(18.0, 6.0, 6.0, 18.0);
            break;
         case "back":
         case "chevron-left":
            poly(15.0, 5.0, 8.0, 12.0, 15.0, 19.0);
            break;
         case "chevron-right":
            poly(9.0, 5.0, 16.0, 12.0, 9.0, 19.0);
            break;
         case "chevron-down":
            poly(5.0, 9.0, 12.0, 16.0, 19.0, 9.0);
            break;
         case "chevron-up":
            poly(5.0, 15.0, 12.0, 8.0, 19.0, 15.0);
            break;
         case "dots":
            disc(5.0, 12.0, 1.8);
            disc(12.0, 12.0, 1.8);
            disc(19.0, 12.0, 1.8);
            break;
         case "gear":
         case "settings":
            circ(12.0, 12.0, 3.2);

            for (int var16 = 0; var16 < 8; var16++) {
               double var19 = (Math.PI * 2) * var16 / 8.0;
               line(12.0 + Math.cos(var19) * 6.3, 12.0 + Math.sin(var19) * 6.3, 12.0 + Math.cos(var19) * 9.0, 12.0 + Math.sin(var19) * 9.0);
            }

            circ(12.0, 12.0, 6.6);
            break;
         case "sparkle":
         case "visuals":
            polyF(12.0, 2.0, 14.0, 10.0, 22.0, 12.0, 14.0, 14.0, 12.0, 22.0, 10.0, 14.0, 2.0, 12.0, 10.0, 10.0);
            polyF(19.0, 2.5, 19.8, 4.2, 21.5, 5.0, 19.8, 5.8, 19.0, 7.5, 18.2, 5.8, 16.5, 5.0, 18.2, 4.2);
            break;
         case "bolt":
            polyF(13.0, 2.0, 4.0, 14.0, 11.0, 14.0, 10.0, 22.0, 20.0, 9.0, 13.0, 9.0);
            break;
         case "wifi":
            arc(12.0, 19.0, 15.0, 45.0, 90.0);
            arc(12.0, 19.0, 10.0, 45.0, 90.0);
            arc(12.0, 19.0, 5.0, 45.0, 90.0);
            disc(12.0, 19.0, 1.4);
            break;
         case "mouse":
            rr(6.0, 2.5, 12.0, 19.0, 6.0);
            line(12.0, 6.5, 12.0, 10.0);
            break;
         case "pin":
         case "map-pin":
            g.draw(new java.awt.geom.Path2D.Double() {
               {
                  this.moveTo(12.0 * Icons.k, 21.5 * Icons.k);
                  this.curveTo(12.0 * Icons.k, 21.5 * Icons.k, 19.0 * Icons.k, 15.0 * Icons.k, 19.0 * Icons.k, 9.5 * Icons.k);
                  this.curveTo(19.0 * Icons.k, 5.5 * Icons.k, 16.0 * Icons.k, 2.5 * Icons.k, 12.0 * Icons.k, 2.5 * Icons.k);
                  this.curveTo(8.0 * Icons.k, 2.5 * Icons.k, 5.0 * Icons.k, 5.5 * Icons.k, 5.0 * Icons.k, 9.5 * Icons.k);
                  this.curveTo(5.0 * Icons.k, 15.0 * Icons.k, 12.0 * Icons.k, 21.5 * Icons.k, 12.0 * Icons.k, 21.5 * Icons.k);
               }
            });
            circ(12.0, 9.5, 2.5);
            break;
         case "compass":
            circ(12.0, 12.0, 9.5);
            polyF(15.5, 8.5, 13.5, 13.5, 8.5, 15.5, 10.5, 10.5);
            break;
         case "clock":
            circ(12.0, 12.0, 9.5);
            poly(12.0, 7.0, 12.0, 12.0, 15.5, 14.0);
            break;
         case "hourglass":
            line(6.0, 2.5, 18.0, 2.5);
            line(6.0, 21.5, 18.0, 21.5);
            poly(7.0, 2.5, 7.0, 6.0, 12.0, 12.0, 7.0, 18.0, 7.0, 21.5);
            poly(17.0, 2.5, 17.0, 6.0, 12.0, 12.0, 17.0, 18.0, 17.0, 21.5);
            break;
         case "car":
         case "vehicle":
            g.draw(new java.awt.geom.Path2D.Double() {
               {
                  this.moveTo(3.0 * Icons.k, 15.0 * Icons.k);
                  this.lineTo(4.5 * Icons.k, 10.0 * Icons.k);
                  this.lineTo(8.0 * Icons.k, 6.5 * Icons.k);
                  this.lineTo(16.0 * Icons.k, 6.5 * Icons.k);
                  this.lineTo(19.5 * Icons.k, 10.0 * Icons.k);
                  this.lineTo(21.0 * Icons.k, 15.0 * Icons.k);
                  this.lineTo(21.0 * Icons.k, 17.0 * Icons.k);
                  this.lineTo(3.0 * Icons.k, 17.0 * Icons.k);
                  this.closePath();
               }
            });
            line(4.5, 10.5, 19.5, 10.5);
            disc(7.5, 17.5, 2.2);
            disc(16.5, 17.5, 2.2);
            break;
         case "speed":
         case "gauge":
            arc(12.0, 14.0, 9.0, -20.0, 220.0);
            line(12.0, 14.0, 16.5, 9.0);
            disc(12.0, 14.0, 1.6);
            break;
         case "chip":
         case "memory":
         case "cpu":
            rr(5.5, 5.5, 13.0, 13.0, 2.0);
            rr(9.0, 9.0, 6.0, 6.0, 1.0);

            for (int var15 = 0; var15 < 3; var15++) {
               double var18 = 9 + var15 * 3;
               line(var18, 2.0, var18, 5.5);
               line(var18, 18.5, var18, 22.0);
               line(2.0, var18, 5.5, var18);
               line(18.5, var18, 22.0, var18);
            }
            break;
         case "globe":
         case "server":
            circ(12.0, 12.0, 9.5);
            g.draw(new java.awt.geom.Ellipse2D.Double(7.5 * k, 2.5 * k, 9.0 * k, 19.0 * k));
            line(2.5, 12.0, 21.5, 12.0);
            break;
         case "shield":
            g.draw(new java.awt.geom.Path2D.Double() {
               {
                  this.moveTo(12.0 * Icons.k, 2.5 * Icons.k);
                  this.lineTo(20.0 * Icons.k, 5.5 * Icons.k);
                  this.lineTo(20.0 * Icons.k, 11.0 * Icons.k);
                  this.curveTo(20.0 * Icons.k, 16.5 * Icons.k, 16.5 * Icons.k, 20.0 * Icons.k, 12.0 * Icons.k, 21.5 * Icons.k);
                  this.curveTo(7.5 * Icons.k, 20.0 * Icons.k, 4.0 * Icons.k, 16.5 * Icons.k, 4.0 * Icons.k, 11.0 * Icons.k);
                  this.lineTo(4.0 * Icons.k, 5.5 * Icons.k);
                  this.closePath();
               }
            });
            break;
         case "potion":
         case "flask":
            poly(9.0, 2.5, 9.0, 9.0, 4.5, 18.0, 5.5, 21.0, 18.5, 21.0, 19.5, 18.0, 15.0, 9.0, 15.0, 2.5);
            line(7.5, 2.5, 16.5, 2.5);
            line(6.5, 15.0, 17.5, 15.0);
            break;
         case "keys":
         case "keyboard":
            rr(2.0, 5.0, 20.0, 14.0, 2.5);

            for (int var14 = 0; var14 < 4; var14++) {
               disc(6 + var14 * 4, 9.5, 0.9);
               disc(6 + var14 * 4, 13.0, 0.9);
            }

            line(8.0, 16.0, 16.0, 16.0);
            break;
         case "sun":
            circ(12.0, 12.0, 4.2);

            for (int var13 = 0; var13 < 8; var13++) {
               double var17 = (Math.PI * 2) * var13 / 8.0;
               line(12.0 + Math.cos(var17) * 7.0, 12.0 + Math.sin(var17) * 7.0, 12.0 + Math.cos(var17) * 9.5, 12.0 + Math.sin(var17) * 9.5);
            }
            break;
         case "moon":
            g.draw(new java.awt.geom.Path2D.Double() {
               {
                  this.moveTo(20.0 * Icons.k, 14.5 * Icons.k);
                  this.curveTo(18.5 * Icons.k, 18.5 * Icons.k, 15.0 * Icons.k, 21.0 * Icons.k, 11.0 * Icons.k, 21.0 * Icons.k);
                  this.curveTo(6.0 * Icons.k, 21.0 * Icons.k, 3.0 * Icons.k, 17.0 * Icons.k, 3.0 * Icons.k, 12.5 * Icons.k);
                  this.curveTo(3.0 * Icons.k, 8.0 * Icons.k, 6.0 * Icons.k, 4.5 * Icons.k, 10.0 * Icons.k, 3.5 * Icons.k);
                  this.curveTo(8.0 * Icons.k, 9.5 * Icons.k, 13.0 * Icons.k, 16.5 * Icons.k, 20.0 * Icons.k, 14.5 * Icons.k);
               }
            });
            break;
         case "cloud":
         case "sky":
            g.draw(new java.awt.geom.Path2D.Double() {
               {
                  this.moveTo(7.0 * Icons.k, 19.0 * Icons.k);
                  this.curveTo(3.5 * Icons.k, 19.0 * Icons.k, 2.0 * Icons.k, 16.5 * Icons.k, 2.5 * Icons.k, 14.0 * Icons.k);
                  this.curveTo(3.0 * Icons.k, 11.5 * Icons.k, 5.5 * Icons.k, 10.5 * Icons.k, 7.0 * Icons.k, 11.0 * Icons.k);
                  this.curveTo(7.5 * Icons.k, 7.0 * Icons.k, 10.5 * Icons.k, 5.0 * Icons.k, 13.5 * Icons.k, 5.0 * Icons.k);
                  this.curveTo(17.5 * Icons.k, 5.0 * Icons.k, 20.0 * Icons.k, 8.0 * Icons.k, 19.5 * Icons.k, 11.5 * Icons.k);
                  this.curveTo(22.0 * Icons.k, 12.0 * Icons.k, 22.5 * Icons.k, 15.0 * Icons.k, 21.5 * Icons.k, 16.5 * Icons.k);
                  this.curveTo(20.8 * Icons.k, 18.2 * Icons.k, 19.5 * Icons.k, 19.0 * Icons.k, 17.5 * Icons.k, 19.0 * Icons.k);
                  this.closePath();
               }
            });
            break;
         case "leaf":
         case "biome":
            g.draw(new java.awt.geom.Path2D.Double() {
               {
                  this.moveTo(4.0 * Icons.k, 20.0 * Icons.k);
                  this.curveTo(4.0 * Icons.k, 10.0 * Icons.k, 10.0 * Icons.k, 4.0 * Icons.k, 20.0 * Icons.k, 4.0 * Icons.k);
                  this.curveTo(20.0 * Icons.k, 14.0 * Icons.k, 14.0 * Icons.k, 20.0 * Icons.k, 4.0 * Icons.k, 20.0 * Icons.k);
               }
            });
            line(4.0, 20.0, 13.0, 11.0);
            break;
         case "target":
         case "crosshair":
            circ(12.0, 12.0, 9.0);
            circ(12.0, 12.0, 4.5);
            disc(12.0, 12.0, 1.3);
            break;
         case "food":
            g.draw(new java.awt.geom.Ellipse2D.Double(10.0 * k, 3.0 * k, 11.0 * k, 11.0 * k));
            line(12.0, 12.0, 5.5, 18.5);
            circ(4.5, 19.5, 1.8);
            break;
         case "heart":
            g.draw(heart());
            break;
         case "heart-fill":
            g.fill(heart());
            break;
         case "pulse":
            poly(2.0, 12.0, 7.0, 12.0, 9.5, 5.0, 14.0, 19.0, 16.5, 12.0, 22.0, 12.0);
            break;
         case "list":
            line(9.0, 6.0, 21.0, 6.0);
            line(9.0, 12.0, 21.0, 12.0);
            line(9.0, 18.0, 21.0, 18.0);
            disc(4.5, 6.0, 1.2);
            disc(4.5, 12.0, 1.2);
            disc(4.5, 18.0, 1.2);
            break;
         case "gem":
         case "crystal":
            polyC(6.0, 3.0, 18.0, 3.0, 22.0, 9.0, 12.0, 21.5, 2.0, 9.0);
            poly(2.0, 9.0, 22.0, 9.0);
            poly(9.0, 3.0, 7.5, 9.0, 12.0, 21.5, 16.5, 9.0, 15.0, 3.0);
            break;
         case "play":
            polyF(7.0, 4.0, 20.0, 12.0, 7.0, 20.0);
            break;
         case "pause":
            rrf(6.0, 4.0, 4.0, 16.0, 1.0);
            rrf(14.0, 4.0, 4.0, 16.0, 1.0);
            break;
         case "star":
            polyC(12.0, 2.5, 14.9, 8.6, 21.5, 9.3, 16.6, 13.8, 17.9, 20.4, 12.0, 17.1, 6.1, 20.4, 7.4, 13.8, 2.5, 9.3, 9.1, 8.6);
            break;
         case "star-fill":
            polyF(12.0, 2.5, 14.9, 8.6, 21.5, 9.3, 16.6, 13.8, 17.9, 20.4, 12.0, 17.1, 6.1, 20.4, 7.4, 13.8, 2.5, 9.3, 9.1, 8.6);
            break;
         case "trophy":
            poly(7.0, 3.0, 17.0, 3.0, 17.0, 9.0);
            g.draw(new java.awt.geom.Arc2D.Double(7.0 * k, 3.0 * k, 10.0 * k, 12.0 * k, 180.0, 180.0, 0));
            line(7.0, 3.0, 7.0, 9.0);
            arc(5.0, 7.0, 2.5, 90.0, 180.0);
            arc(19.0, 7.0, 2.5, 270.0, 180.0);
            line(12.0, 15.0, 12.0, 18.5);
            line(8.0, 21.0, 16.0, 21.0);
            line(9.5, 18.5, 14.5, 18.5);
            break;
         case "eye":
            g.draw(new java.awt.geom.Path2D.Double() {
               {
                  this.moveTo(2.0 * Icons.k, 12.0 * Icons.k);
                  this.curveTo(5.0 * Icons.k, 6.0 * Icons.k, 9.0 * Icons.k, 4.5 * Icons.k, 12.0 * Icons.k, 4.5 * Icons.k);
                  this.curveTo(15.0 * Icons.k, 4.5 * Icons.k, 19.0 * Icons.k, 6.0 * Icons.k, 22.0 * Icons.k, 12.0 * Icons.k);
                  this.curveTo(19.0 * Icons.k, 18.0 * Icons.k, 15.0 * Icons.k, 19.5 * Icons.k, 12.0 * Icons.k, 19.5 * Icons.k);
                  this.curveTo(9.0 * Icons.k, 19.5 * Icons.k, 5.0 * Icons.k, 18.0 * Icons.k, 2.0 * Icons.k, 12.0 * Icons.k);
               }
            });
            circ(12.0, 12.0, 3.0);
            break;
         case "user":
            circ(12.0, 8.0, 4.0);
            g.draw(new java.awt.geom.Arc2D.Double(4.0 * k, 14.0 * k, 16.0 * k, 14.0 * k, 0.0, 180.0, 0));
            break;
         case "users":
            circ(9.0, 8.0, 3.5);
            g.draw(new java.awt.geom.Arc2D.Double(2.0 * k, 14.0 * k, 14.0 * k, 12.0 * k, 0.0, 180.0, 0));
            arc(16.5, 8.0, 3.0, -80.0, 170.0);
            g.draw(new java.awt.geom.Arc2D.Double(13.0 * k, 14.0 * k, 10.0 * k, 12.0 * k, 0.0, 110.0, 0));
            break;
         case "flame":
         case "fire":
            g.draw(new java.awt.geom.Path2D.Double() {
               {
                  this.moveTo(12.0 * Icons.k, 22.0 * Icons.k);
                  this.curveTo(7.0 * Icons.k, 22.0 * Icons.k, 4.5 * Icons.k, 18.5 * Icons.k, 4.5 * Icons.k, 15.0 * Icons.k);
                  this.curveTo(4.5 * Icons.k, 10.0 * Icons.k, 9.0 * Icons.k, 8.0 * Icons.k, 10.0 * Icons.k, 2.0 * Icons.k);
                  this.curveTo(14.0 * Icons.k, 5.0 * Icons.k, 15.0 * Icons.k, 8.0 * Icons.k, 14.5 * Icons.k, 11.0 * Icons.k);
                  this.curveTo(16.0 * Icons.k, 10.5 * Icons.k, 17.0 * Icons.k, 9.0 * Icons.k, 17.5 * Icons.k, 8.0 * Icons.k);
                  this.curveTo(19.0 * Icons.k, 10.0 * Icons.k, 19.5 * Icons.k, 12.5 * Icons.k, 19.5 * Icons.k, 15.0 * Icons.k);
                  this.curveTo(19.5 * Icons.k, 18.5 * Icons.k, 17.0 * Icons.k, 22.0 * Icons.k, 12.0 * Icons.k, 22.0 * Icons.k);
               }
            });
            break;
         case "snow":
            for (int var6 = 0; var6 < 3; var6++) {
               double var7 = Math.PI * var6 / 3.0;
               double var9 = Math.cos(var7) * 9.5;
               double var11 = Math.sin(var7) * 9.5;
               line(12.0 - var9, 12.0 - var11, 12.0 + var9, 12.0 + var11);
            }

            circ(12.0, 12.0, 2.0);
            break;
         case "wing":
         case "wings":
            g.draw(new java.awt.geom.Path2D.Double() {
               {
                  this.moveTo(3.0 * Icons.k, 6.0 * Icons.k);
                  this.curveTo(10.0 * Icons.k, 5.0 * Icons.k, 17.0 * Icons.k, 7.0 * Icons.k, 21.0 * Icons.k, 18.0 * Icons.k);
                  this.curveTo(16.0 * Icons.k, 16.5 * Icons.k, 12.0 * Icons.k, 17.5 * Icons.k, 9.0 * Icons.k, 19.0 * Icons.k);
                  this.curveTo(9.5 * Icons.k, 16.5 * Icons.k, 8.0 * Icons.k, 15.0 * Icons.k, 6.0 * Icons.k, 14.5 * Icons.k);
                  this.curveTo(6.5 * Icons.k, 12.5 * Icons.k, 5.5 * Icons.k, 10.5 * Icons.k, 3.5 * Icons.k, 10.0 * Icons.k);
                  this.closePath();
               }
            });
            line(8.0, 9.0, 16.5, 16.0);
            break;
         case "crown":
         case "hat":
            polyC(3.0, 8.0, 7.5, 12.0, 12.0, 5.0, 16.5, 12.0, 21.0, 8.0, 19.0, 18.0, 5.0, 18.0);
            line(5.0, 21.0, 19.0, 21.0);
            break;
         case "glasses":
            circ(6.5, 14.0, 4.0);
            circ(17.5, 14.0, 4.0);
            arc(12.0, 14.0, 1.8, 30.0, 120.0);
            line(2.5, 13.0, 1.5, 9.0);
            line(21.5, 13.0, 22.5, 9.0);
            break;
         case "paw":
         case "pet":
            disc(6.0, 10.0, 2.0);
            disc(10.0, 6.0, 2.0);
            disc(14.0, 6.0, 2.0);
            disc(18.0, 10.0, 2.0);
            g.fill(new java.awt.geom.Ellipse2D.Double(7.0 * k, 11.5 * k, 10.0 * k, 9.0 * k));
            break;
         case "backpack":
            rr(5.0, 6.0, 14.0, 16.0, 4.0);
            poly(9.0, 6.0, 9.0, 3.0, 15.0, 3.0, 15.0, 6.0);
            rr(8.5, 13.0, 7.0, 5.0, 1.5);
            break;
         case "aura":
            circ(12.0, 12.0, 4.0);
            arc(12.0, 12.0, 8.5, 20.0, 110.0);
            arc(12.0, 12.0, 8.5, 200.0, 110.0);
            disc(19.5, 5.5, 1.1);
            disc(4.5, 18.5, 1.1);
            break;
         case "music":
            poly(9.0, 18.0, 9.0, 5.0, 20.0, 3.0, 20.0, 16.0);
            circ(6.5, 18.0, 2.5);
            circ(17.5, 16.0, 2.5);
            break;
         case "brick":
         case "lego":
            rrf(2.0, 9.0, 20.0, 11.0, 2.0);
            rrf(5.0, 5.0, 5.0, 5.0, 1.2);
            rrf(14.0, 5.0, 5.0, 5.0, 1.2);
            break;
         case "trash":
            line(3.0, 6.0, 21.0, 6.0);
            poly(8.0, 6.0, 8.0, 3.0, 16.0, 3.0, 16.0, 6.0);
            poly(5.0, 6.0, 6.0, 21.0, 18.0, 21.0, 19.0, 6.0);
            line(10.0, 10.0, 10.0, 17.0);
            line(14.0, 10.0, 14.0, 17.0);
            break;
         case "reset":
         case "refresh":
            arc(12.0, 12.0, 8.5, 60.0, 290.0);
            polyF(3.5, 3.5, 3.5, 10.0, 10.0, 10.0);
            break;
         case "check":
            poly(4.0, 12.5, 9.5, 18.0, 20.0, 6.0);
            break;
         case "plus":
            line(12.0, 4.0, 12.0, 20.0);
            line(4.0, 12.0, 20.0, 12.0);
            break;
         case "minus":
            line(4.0, 12.0, 20.0, 12.0);
            break;
         case "lock":
            rr(4.5, 10.5, 15.0, 11.0, 2.5);
            arc(12.0, 7.5, 4.5, 0.0, 180.0);
            line(7.5, 7.5, 7.5, 10.5);
            line(16.5, 7.5, 16.5, 10.5);
            break;
         case "bell":
            g.draw(new java.awt.geom.Path2D.Double() {
               {
                  this.moveTo(6.0 * Icons.k, 16.0 * Icons.k);
                  this.lineTo(6.0 * Icons.k, 10.5 * Icons.k);
                  this.curveTo(6.0 * Icons.k, 7.0 * Icons.k, 8.7 * Icons.k, 4.0 * Icons.k, 12.0 * Icons.k, 4.0 * Icons.k);
                  this.curveTo(15.3 * Icons.k, 4.0 * Icons.k, 18.0 * Icons.k, 7.0 * Icons.k, 18.0 * Icons.k, 10.5 * Icons.k);
                  this.lineTo(18.0 * Icons.k, 16.0 * Icons.k);
                  this.lineTo(20.0 * Icons.k, 18.0 * Icons.k);
                  this.lineTo(4.0 * Icons.k, 18.0 * Icons.k);
                  this.closePath();
               }
            });
            arc(12.0, 19.5, 2.2, 180.0, 180.0);
            break;
         case "sword":
            line(4.0, 20.0, 17.0, 7.0);
            poly(17.0, 7.0, 21.0, 3.0, 21.5, 2.5, 20.8, 6.8);
            line(6.0, 14.0, 10.0, 18.0);
            line(3.0, 21.0, 5.5, 18.5);
            break;
         case "sprint":
         case "run":
            circ(14.5, 4.0, 2.0);
            poly(7.0, 10.0, 11.0, 7.5, 15.0, 8.5, 17.0, 12.0, 20.0, 12.0);
            poly(13.5, 8.5, 11.5, 14.0, 15.5, 16.5, 14.5, 21.5);
            poly(11.5, 14.0, 8.0, 19.0, 4.0, 19.0);
            break;
         case "sneak":
         case "crouch":
            circ(9.0, 5.0, 2.0);
            poly(9.0, 8.0, 12.0, 13.0, 18.0, 13.0, 18.0, 20.0);
            poly(12.0, 13.0, 10.0, 20.0, 6.0, 20.0);
            poly(9.0, 8.0, 5.0, 12.0);
            break;
         case "camera":
         case "freelook":
            rr(2.5, 7.0, 19.0, 13.0, 2.5);
            poly(8.0, 7.0, 9.5, 4.0, 14.5, 4.0, 16.0, 7.0);
            circ(12.0, 13.5, 3.8);
            break;
         case "chat":
            g.draw(new java.awt.geom.Path2D.Double() {
               {
                  this.moveTo(4.0 * Icons.k, 5.0 * Icons.k);
                  this.lineTo(20.0 * Icons.k, 5.0 * Icons.k);
                  this.lineTo(20.0 * Icons.k, 16.0 * Icons.k);
                  this.lineTo(10.0 * Icons.k, 16.0 * Icons.k);
                  this.lineTo(5.0 * Icons.k, 20.5 * Icons.k);
                  this.lineTo(5.5 * Icons.k, 16.0 * Icons.k);
                  this.lineTo(4.0 * Icons.k, 16.0 * Icons.k);
                  this.closePath();
               }
            });
            break;
         case "filter":
            polyC(3.0, 4.0, 21.0, 4.0, 14.0, 12.5, 14.0, 20.0, 10.0, 18.0, 10.0, 12.5);
            break;
         case "info":
            circ(12.0, 12.0, 9.5);
            line(12.0, 11.0, 12.0, 17.0);
            disc(12.0, 7.5, 1.2);
            break;
         case "gift":
            rr(3.0, 8.0, 18.0, 5.0, 1.0);
            rr(4.5, 13.0, 15.0, 8.5, 1.0);
            line(12.0, 8.0, 12.0, 21.5);
            arc(9.0, 6.0, 2.6, 0.0, 270.0);
            arc(15.0, 6.0, 2.6, -90.0, 270.0);
            break;
         case "image":
            rr(3.0, 4.0, 18.0, 16.0, 2.5);
            circ(8.5, 9.5, 1.8);
            poly(3.0, 17.0, 9.0, 12.0, 13.0, 15.5, 16.0, 13.0, 21.0, 17.5);
            break;
         case "waypoint":
         case "flag":
            line(5.0, 21.0, 5.0, 3.0);
            polyC(5.0, 4.0, 18.0, 4.0, 15.5, 8.5, 18.0, 13.0, 5.0, 13.0);
            break;
         case "skull":
         case "kill":
            g.draw(new java.awt.geom.Path2D.Double() {
               {
                  this.moveTo(7.0 * Icons.k, 20.0 * Icons.k);
                  this.lineTo(7.0 * Icons.k, 17.0 * Icons.k);
                  this.curveTo(4.5 * Icons.k, 15.5 * Icons.k, 3.5 * Icons.k, 13.0 * Icons.k, 3.5 * Icons.k, 10.5 * Icons.k);
                  this.curveTo(3.5 * Icons.k, 6.0 * Icons.k, 7.3 * Icons.k, 2.5 * Icons.k, 12.0 * Icons.k, 2.5 * Icons.k);
                  this.curveTo(16.7 * Icons.k, 2.5 * Icons.k, 20.5 * Icons.k, 6.0 * Icons.k, 20.5 * Icons.k, 10.5 * Icons.k);
                  this.curveTo(20.5 * Icons.k, 13.0 * Icons.k, 19.5 * Icons.k, 15.5 * Icons.k, 17.0 * Icons.k, 17.0 * Icons.k);
                  this.lineTo(17.0 * Icons.k, 20.0 * Icons.k);
                  this.closePath();
               }
            });
            disc(8.8, 11.0, 2.0);
            disc(15.2, 11.0, 2.0);
            line(10.5, 20.0, 10.5, 17.5);
            line(13.5, 20.0, 13.5, 17.5);
            break;
         case "wand":
            line(3.0, 21.0, 14.0, 10.0);
            line(12.5, 11.5, 14.5, 13.0);
            polyF(17.0, 2.0, 18.0, 5.0, 21.0, 6.0, 18.0, 7.0, 17.0, 10.0, 16.0, 7.0, 13.0, 6.0, 16.0, 5.0);
            break;
         case "totem":
            rr(6.5, 2.5, 11.0, 9.0, 3.0);
            disc(9.5, 7.0, 1.0);
            disc(14.5, 7.0, 1.0);
            poly(8.0, 11.5, 8.0, 15.0, 4.0, 13.0, 4.0, 16.0, 8.0, 18.5, 8.0, 21.5, 16.0, 21.5, 16.0, 18.5, 20.0, 16.0, 20.0, 13.0, 16.0, 15.0, 16.0, 11.5);
            break;
         case "circle-dot":
         case "jump":
            g.draw(new java.awt.geom.Ellipse2D.Double(2.0 * k, 13.0 * k, 20.0 * k, 8.0 * k));
            poly(12.0, 3.0, 12.0, 11.0);
            poly(9.0, 6.0, 12.0, 3.0, 15.0, 6.0);
            break;
         case "trail":
            g.draw(new java.awt.geom.Path2D.Double() {
               {
                  this.moveTo(3.0 * Icons.k, 19.0 * Icons.k);
                  this.curveTo(8.0 * Icons.k, 19.0 * Icons.k, 9.0 * Icons.k, 5.0 * Icons.k, 15.0 * Icons.k, 5.0 * Icons.k);
               }
            });
            disc(18.0, 5.0, 2.5);
            disc(9.5, 12.5, 0.9);
            disc(5.5, 17.5, 0.8);
            break;
         case "hit":
            polyC(12.0, 2.0, 14.0, 9.0, 21.0, 7.0, 16.0, 12.0, 21.0, 17.0, 14.0, 15.0, 12.0, 22.0, 10.0, 15.0, 3.0, 17.0, 8.0, 12.0, 3.0, 7.0, 10.0, 9.0);
            break;
         case "zoom":
            circ(10.5, 10.5, 6.5);
            line(15.5, 15.5, 21.0, 21.0);
            line(10.5, 7.5, 10.5, 13.5);
            line(7.5, 10.5, 13.5, 10.5);
            break;
         case "rain":
         case "weather":
            g.draw(new java.awt.geom.Path2D.Double() {
               {
                  this.moveTo(7.0 * Icons.k, 14.0 * Icons.k);
                  this.curveTo(4.0 * Icons.k, 14.0 * Icons.k, 2.5 * Icons.k, 12.0 * Icons.k, 3.0 * Icons.k, 10.0 * Icons.k);
                  this.curveTo(3.5 * Icons.k, 8.0 * Icons.k, 5.5 * Icons.k, 7.5 * Icons.k, 7.0 * Icons.k, 8.0 * Icons.k);
                  this.curveTo(7.5 * Icons.k, 5.0 * Icons.k, 10.0 * Icons.k, 3.0 * Icons.k, 13.0 * Icons.k, 3.0 * Icons.k);
                  this.curveTo(17.0 * Icons.k, 3.0 * Icons.k, 19.5 * Icons.k, 6.0 * Icons.k, 19.0 * Icons.k, 9.0 * Icons.k);
                  this.curveTo(21.0 * Icons.k, 9.5 * Icons.k, 21.5 * Icons.k, 12.0 * Icons.k, 20.5 * Icons.k, 13.0 * Icons.k);
                  this.curveTo(20.0 * Icons.k, 13.7 * Icons.k, 19.0 * Icons.k, 14.0 * Icons.k, 18.0 * Icons.k, 14.0 * Icons.k);
                  this.closePath();
               }
            });
            line(8.0, 17.0, 7.0, 20.0);
            line(12.0, 17.0, 11.0, 20.0);
            line(16.0, 17.0, 15.0, 20.0);
            break;
         case "box":
         case "outline":
            polyC(12.0, 2.5, 20.5, 7.0, 20.5, 17.0, 12.0, 21.5, 3.5, 17.0, 3.5, 7.0);
            poly(3.5, 7.0, 12.0, 11.5, 20.5, 7.0);
            line(12.0, 11.5, 12.0, 21.5);
            break;
         case "number":
         case "damage":
            line(9.0, 3.0, 7.0, 21.0);
            line(17.0, 3.0, 15.0, 21.0);
            line(4.0, 9.0, 21.0, 9.0);
            line(3.0, 15.0, 20.0, 15.0);
            break;
         case "film":
         case "cinema":
            rr(3.0, 3.0, 18.0, 18.0, 2.0);
            line(7.0, 3.0, 7.0, 21.0);
            line(17.0, 3.0, 17.0, 21.0);
            line(3.0, 12.0, 21.0, 12.0);
            line(3.0, 7.5, 7.0, 7.5);
            line(3.0, 16.5, 7.0, 16.5);
            line(17.0, 7.5, 21.0, 7.5);
            line(17.0, 16.5, 21.0, 16.5);
            break;
         case "vignette":
            rr(2.5, 4.5, 19.0, 15.0, 3.0);
            g.draw(new java.awt.geom.Ellipse2D.Double(6.0 * k, 7.5 * k, 12.0 * k, 9.0 * k));
            break;
         case "ring":
         case "rings":
            g.draw(new java.awt.geom.Ellipse2D.Double(3.0 * k, 14.0 * k, 18.0 * k, 7.0 * k));
            circ(12.0, 7.0, 3.0);
            line(12.0, 10.0, 12.0, 14.0);
            break;
         case "arrow":
         case "projectile":
            line(4.0, 20.0, 20.0, 4.0);
            poly(13.0, 4.0, 20.0, 4.0, 20.0, 11.0);
            poly(4.0, 16.0, 4.0, 20.0, 8.0, 20.0);
            break;
         case "firefly":
            disc(12.0, 12.0, 2.2);
            circ(12.0, 12.0, 5.0);
            disc(5.0, 6.0, 1.0);
            disc(19.0, 7.0, 1.0);
            disc(17.0, 19.0, 1.0);
            disc(6.0, 18.0, 0.8);
            break;
         case "tilt":
            rr(4.0, 6.0, 16.0, 12.0, 2.0);
            arc(12.0, 12.0, 10.5, 30.0, 40.0);
            arc(12.0, 12.0, 10.5, 210.0, 40.0);
            break;
         case "hitmarker":
            line(4.0, 4.0, 9.0, 9.0);
            line(20.0, 4.0, 15.0, 9.0);
            line(4.0, 20.0, 9.0, 15.0);
            line(20.0, 20.0, 15.0, 15.0);
            break;
         case "fps":
         case "boost":
            polyF(13.0, 2.0, 4.0, 14.0, 11.0, 14.0, 10.0, 22.0, 20.0, 9.0, 13.0, 9.0);
            break;
         case "entity":
            rr(6.0, 3.0, 12.0, 10.0, 2.0);
            rr(8.0, 13.0, 8.0, 8.0, 1.5);
            disc(10.0, 8.0, 1.0);
            disc(14.0, 8.0, 1.0);
            break;
         case "particles":
            disc(6.0, 6.0, 1.5);
            disc(12.0, 4.0, 1.0);
            disc(18.0, 7.0, 1.8);
            disc(8.0, 13.0, 1.2);
            disc(16.0, 14.0, 1.0);
            disc(11.0, 19.0, 1.8);
            disc(19.0, 19.0, 1.1);
            disc(4.0, 18.0, 0.9);
            break;
         case "pause-bell":
         case "break":
            circ(12.0, 13.0, 8.0);
            poly(12.0, 9.0, 12.0, 13.0, 15.0, 15.0);
            line(4.0, 4.0, 7.0, 2.0);
            line(20.0, 4.0, 17.0, 2.0);
            break;
         case "macro":
            rr(3.0, 6.0, 18.0, 12.0, 2.5);
            line(7.0, 10.0, 9.0, 12.0);
            line(7.0, 14.0, 9.0, 12.0);
            line(11.0, 14.0, 16.0, 14.0);
            break;
         case "clipboard":
         case "copy":
            rr(8.0, 8.0, 13.0, 13.0, 2.0);
            poly(16.0, 8.0, 16.0, 5.0, 13.5, 3.0, 5.0, 3.0, 3.0, 5.0, 3.0, 13.5, 5.0, 16.0, 8.0, 16.0);
            break;
         case "warning":
         case "alert":
            polyC(12.0, 3.0, 22.0, 20.5, 2.0, 20.5);
            line(12.0, 9.5, 12.0, 14.5);
            disc(12.0, 17.5, 1.1);
            break;
         case "armor":
            polyC(7.0, 3.0, 12.0, 5.0, 17.0, 3.0, 21.0, 6.0, 19.0, 10.0, 17.0, 9.0, 17.0, 21.0, 7.0, 21.0, 7.0, 9.0, 5.0, 10.0, 3.0, 6.0);
            break;
         case "respawn":
            arc(12.0, 12.0, 8.5, 90.0, 300.0);
            polyF(12.0, 1.5, 12.0, 7.0, 16.5, 4.2);
            break;
         case "gg":
            circ(12.0, 12.0, 9.5);
            arc(12.0, 13.0, 5.0, 200.0, 140.0);
            disc(9.0, 9.5, 1.1);
            disc(15.0, 9.5, 1.1);
            break;
         case "tv":
            rr(2.5, 4.0, 19.0, 13.0, 2.0);
            line(8.0, 21.0, 16.0, 21.0);
            line(12.0, 17.0, 12.0, 21.0);
            break;
         case "forward":
            poly(9.0, 5.0, 16.0, 12.0, 9.0, 19.0);
            break;
         case "home":
            polyC(3.5, 11.0, 12.0, 3.5, 20.5, 11.0, 20.5, 20.5, 14.5, 20.5, 14.5, 14.0, 9.5, 14.0, 9.5, 20.5, 3.5, 20.5);
            break;
         case "pip":
            rr(2.5, 4.5, 19.0, 15.0, 2.0);
            rrf(12.0, 12.0, 7.5, 5.5, 1.0);
            break;
         case "expand":
            poly(3.0, 9.0, 3.0, 3.0, 9.0, 3.0);
            poly(15.0, 3.0, 21.0, 3.0, 21.0, 9.0);
            poly(21.0, 15.0, 21.0, 21.0, 15.0, 21.0);
            poly(9.0, 21.0, 3.0, 21.0, 3.0, 15.0);
            break;
         case "shrink":
            poly(9.0, 3.0, 9.0, 9.0, 3.0, 9.0);
            poly(21.0, 9.0, 15.0, 9.0, 15.0, 3.0);
            poly(15.0, 21.0, 15.0, 15.0, 21.0, 15.0);
            poly(3.0, 15.0, 9.0, 15.0, 9.0, 21.0);
            break;
         case "bookmark":
            polyC(6.0, 3.0, 18.0, 3.0, 18.0, 21.0, 12.0, 16.5, 6.0, 21.0);
            break;
         case "light":
         case "ambilight":
            rr(5.0, 7.0, 14.0, 10.0, 1.5);
            line(2.0, 4.0, 4.0, 6.0);
            line(22.0, 4.0, 20.0, 6.0);
            line(2.0, 20.0, 4.0, 18.0);
            line(22.0, 20.0, 20.0, 18.0);
            line(12.0, 2.0, 12.0, 4.5);
            line(12.0, 22.0, 12.0, 19.5);
            break;
         case "mute":
            polyC(3.0, 9.0, 7.0, 9.0, 12.0, 4.5, 12.0, 19.5, 7.0, 15.0, 3.0, 15.0);
            line(15.5, 9.0, 21.0, 14.5);
            line(21.0, 9.0, 15.5, 14.5);
            break;
         case "sound":
         case "volume":
            polyC(3.0, 9.0, 7.0, 9.0, 12.0, 4.5, 12.0, 19.5, 7.0, 15.0, 3.0, 15.0);
            arc(12.0, 12.0, 5.0, -45.0, 90.0);
            arc(12.0, 12.0, 9.0, -50.0, 100.0);
            break;
         case "tag":
         case "profile":
            polyC(3.0, 3.0, 12.0, 3.0, 21.0, 12.0, 12.0, 21.0, 3.0, 12.0);
            disc(8.0, 8.0, 1.4);
            break;
         case "logo":
            polyF(3.0, 11.0, 12.0, 15.5, 21.0, 11.0, 21.0, 16.0, 12.0, 20.5, 3.0, 16.0);
            polyC(3.0, 11.0, 12.0, 6.5, 21.0, 11.0, 12.0, 15.5);
            break;
         default:
            circ(12.0, 12.0, 8.0);
      }

      g = null;
   }

   private static GeneralPath heart() {
      GeneralPath var0 = new GeneralPath();
      var0.moveTo(12.0 * k, 20.5 * k);
      var0.curveTo(12.0 * k, 20.5 * k, 2.5 * k, 14.5 * k, 2.5 * k, 8.5 * k);
      var0.curveTo(2.5 * k, 5.5 * k, 4.8 * k, 3.5 * k, 7.5 * k, 3.5 * k);
      var0.curveTo(9.5 * k, 3.5 * k, 11.0 * k, 4.6 * k, 12.0 * k, 6.2 * k);
      var0.curveTo(13.0 * k, 4.6 * k, 14.5 * k, 3.5 * k, 16.5 * k, 3.5 * k);
      var0.curveTo(19.2 * k, 3.5 * k, 21.5 * k, 5.5 * k, 21.5 * k, 8.5 * k);
      var0.curveTo(21.5 * k, 14.5 * k, 12.0 * k, 20.5 * k, 12.0 * k, 20.5 * k);
      var0.closePath();
      return var0;
   }
}
