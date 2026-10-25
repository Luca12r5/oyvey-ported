package dev.lego.txt;

import de.bauloo.styles.KillEffect;
import de.bauloo.styles.StylesUiConfig;
import dev.lego.core.Category;
import dev.lego.core.Module;
import dev.lego.core.Setting;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * The extras of Custom TxT (the same ones as in the Bauloo menu) as tiles for the work bench:
 * each extra is a module-like card whose switch and settings read and write the Bauloo values
 * (StylesUiConfig). They are not registered as modules; the bench plans their changes and
 * {@link Extra#push()} writes them when ANWENDEN is pressed.
 */
public final class TxtExtras {
   private static List<Extra> all;

   private TxtExtras() {
   }

   public static List<Extra> all() {
      if (all == null) {
         all = build();
      }
      return all;
   }

   /** Reads the current Bauloo values into every card (unplanned values follow the real ones). */
   public static void pullAll() {
      StylesUiConfig.load();
      for (Extra e : all()) {
         e.pull();
      }
   }

   public static final class Extra extends Module {
      private final BooleanSupplier getOn;
      private final Consumer<Boolean> setOn;
      private final List<Runnable> pulls = new ArrayList<>();
      private final List<Runnable> pushes = new ArrayList<>();

      Extra(String id, String name, String desc, String icon, BooleanSupplier getOn, Consumer<Boolean> setOn) {
         super("txtx_" + id, name, desc, Category.CUSTOM_TXT);
         this.getOn = getOn;
         this.setOn = setOn;
         this.icon(icon);
      }

      @Override
      public boolean isToggleable() {
         return this.getOn != null;
      }

      @Override
      public void onEnable() {
         if (this.setOn != null) {
            this.setOn.accept(true);
            StylesUiConfig.save();
         }
      }

      @Override
      public void onDisable() {
         if (this.setOn != null) {
            this.setOn.accept(false);
            StylesUiConfig.save();
         }
      }

      void pull() {
         this.enabled = this.getOn != null && this.getOn.getAsBoolean();
         for (Runnable r : this.pulls) {
            r.run();
         }
      }

      /** Writes the (applied) settings back into the Bauloo values. */
      public void push() {
         for (Runnable r : this.pushes) {
            r.run();
         }
         StylesUiConfig.save();
      }

      Extra num(String name, double min, double max, double step, String unit, IntSupplier get, IntConsumer set) {
         Setting.Num n = this.add(new Setting.Num(name, min, max, step, get.getAsInt(), unit));
         this.pulls.add(() -> n.value = get.getAsInt());
         this.pushes.add(() -> set.accept((int)Math.round(n.value)));
         return this;
      }

      Extra bool(String name, BooleanSupplier get, Consumer<Boolean> set) {
         Setting.Bool b = this.add(new Setting.Bool(name, get.getAsBoolean()));
         this.pulls.add(() -> b.value = get.getAsBoolean());
         this.pushes.add(() -> set.accept(b.value));
         return this;
      }

      Extra color(String name, IntSupplier get, IntConsumer set) {
         Setting.Color c = this.add(new Setting.Color(name, Setting.Color.CUSTOM));
         this.pulls.add(() -> c.load(String.format("#%06X", get.getAsInt() & 0xFFFFFF)));
         this.pushes.add(() -> set.accept(c.argb() & 0xFFFFFF));
         c.load(String.format("#%06X", get.getAsInt() & 0xFFFFFF));
         return this;
      }

      Extra mode(String name, String[] keys, String[] labels, Supplier<String> get, Consumer<String> set) {
         Setting.Mode m = this.add(new Setting.Mode(name, 0, labels));
         Runnable pull = () -> {
            for (int i = 0; i < keys.length; i++) {
               if (keys[i].equals(get.get())) {
                  m.index = i;
               }
            }
         };
         pull.run();
         this.pulls.add(pull);
         this.pushes.add(() -> set.accept(keys[Math.max(0, Math.min(keys.length - 1, m.index))]));
         return this;
      }
   }

   private static List<Extra> build() {
      List<Extra> l = new ArrayList<>();
      l.add(new Extra("outline", "Spieler-Umriss", "Leuchtender Umriss um Spieler", "user", () -> StylesUiConfig.outline, v -> StylesUiConfig.outline = v)
         .bool("Bei mir", () -> StylesUiConfig.outlineSelf, v -> StylesUiConfig.outlineSelf = v)
         .bool("Bei anderen", () -> StylesUiConfig.outlineOthers, v -> StylesUiConfig.outlineOthers = v)
         .bool("Regenbogen", () -> StylesUiConfig.outlineRainbow, v -> StylesUiConfig.outlineRainbow = v)
         .color("Farbe", () -> StylesUiConfig.outlineColor, v -> StylesUiConfig.outlineColor = v)
         .num("Dicke", 1, 6, 1, "", () -> StylesUiConfig.outlineWidth, v -> StylesUiConfig.outlineWidth = v));
      l.add(new Extra("crystal", "Kristall", "Drehgeschwindigkeit und Kernfarbe", "crystal", null, null)
         .num("Drehung", 0, 300, 5, "%", () -> StylesUiConfig.crystalSpin, v -> StylesUiConfig.crystalSpin = v)
         .bool("Eigene Kernfarbe", () -> StylesUiConfig.coreCustom, v -> StylesUiConfig.coreCustom = v)
         .color("Kernfarbe", () -> StylesUiConfig.coreColor, v -> StylesUiConfig.coreColor = v));
      l.add(new Extra("totem", "Totem", "Größe in der Hand und beim Platzen", "totem", null, null)
         .num("Größe in der Hand", 20, 200, 5, "%", () -> StylesUiConfig.totemSize, v -> StylesUiConfig.totemSize = v)
         .num("Größe beim Platzen", 20, 200, 5, "%", () -> StylesUiConfig.totemPopSize, v -> StylesUiConfig.totemPopSize = v));
      l.add(new Extra("shield", "Schild", "Durchsichtig, Größe, Farbe", "shield", () -> StylesUiConfig.shieldCustom, v -> StylesUiConfig.shieldCustom = v)
         .num("Deckkraft", 10, 100, 5, "%", () -> StylesUiConfig.shieldOpacity, v -> StylesUiConfig.shieldOpacity = v)
         .num("Größe", 20, 200, 5, "%", () -> StylesUiConfig.shieldSize, v -> StylesUiConfig.shieldSize = v)
         .color("Farbe", () -> StylesUiConfig.shieldColor, v -> StylesUiConfig.shieldColor = v));
      l.add(new Extra("gap", "Goldapfel", "Größe in der Hand", "food", null, null)
         .num("Größe", 20, 200, 5, "%", () -> StylesUiConfig.gapSize, v -> StylesUiConfig.gapSize = v));
      l.add(new Extra("anchor", "Anker-Ladung", "Leuchtfarbe, immer voll anzeigen", "respawn", () -> StylesUiConfig.anchorGlowCustom, v -> StylesUiConfig.anchorGlowCustom = v)
         .color("Leuchtfarbe", () -> StylesUiConfig.anchorGlowColor, v -> StylesUiConfig.anchorGlowColor = v)
         .bool("Immer voll", () -> StylesUiConfig.anchorShowFull, v -> StylesUiConfig.anchorShowFull = v)
         .bool("Pixel-Leuchten", () -> StylesUiConfig.anchorPixelGlow, v -> StylesUiConfig.anchorPixelGlow = v));
      String[] killKeys = new String[KillEffect.EFFECTS.length + 1];
      String[] killNames = new String[KillEffect.EFFECTS.length + 1];
      killKeys[0] = "off";
      killNames[0] = "Aus";
      for (int i = 0; i < KillEffect.EFFECTS.length; i++) {
         killKeys[i + 1] = KillEffect.EFFECTS[i][0];
         killNames[i + 1] = KillEffect.EFFECTS[i][1];
      }
      l.add(new Extra("kill", "Kill-Effekt", "Effekt und Sound beim Kill", "skull", null, null)
         .mode("Effekt", killKeys, killNames, () -> StylesUiConfig.killEffect, v -> StylesUiConfig.killEffect = v)
         .bool("Nur meine Kills", () -> StylesUiConfig.killOnlyMine, v -> StylesUiConfig.killOnlyMine = v)
         .bool("Kill-Sound", () -> StylesUiConfig.killSound, v -> StylesUiConfig.killSound = v)
         .num("Lautstärke", 0, 100, 5, "%", () -> StylesUiConfig.killVolume, v -> StylesUiConfig.killVolume = v)
         .color("Farbe", () -> StylesUiConfig.killColor, v -> StylesUiConfig.killColor = v));
      l.add(new Extra("hitbox", "Hitbox", "Farbe der F3+B-Boxen", "box", () -> StylesUiConfig.hitboxCustom, v -> StylesUiConfig.hitboxCustom = v)
         .color("Farbe", () -> StylesUiConfig.hitboxColor, v -> StylesUiConfig.hitboxColor = v));
      l.add(new Extra("crystalbox", "Kristall-Box", "Box um unsichtbare Kristalle", "box", () -> StylesUiConfig.crystalBoxCustom, v -> StylesUiConfig.crystalBoxCustom = v)
         .color("Farbe", () -> StylesUiConfig.crystalBoxColor, v -> StylesUiConfig.crystalBoxColor = v));
      l.add(new Extra("hit", "Treffer-Farbe", "Farbe, wenn jemand getroffen wird", "hit", () -> StylesUiConfig.hitCustom, v -> StylesUiConfig.hitCustom = v)
         .color("Farbe", () -> StylesUiConfig.hitColor, v -> StylesUiConfig.hitColor = v)
         .num("Stärke", 0, 100, 5, "%", () -> StylesUiConfig.hitStrength, v -> StylesUiConfig.hitStrength = v)
         .bool("Auch Rüstung", () -> StylesUiConfig.hitArmor, v -> StylesUiConfig.hitArmor = v)
         .bool("Regenbogen", () -> StylesUiConfig.hitRainbow, v -> StylesUiConfig.hitRainbow = v));
      l.add(new Extra("cobweb", "Spinnennetz", "Umriss um Spinnennetze", "outline", () -> StylesUiConfig.cobwebOutline, v -> StylesUiConfig.cobwebOutline = v)
         .num("Dicke", 1, 6, 1, "", () -> StylesUiConfig.cobwebOutlineWidth, v -> StylesUiConfig.cobwebOutlineWidth = v));
      l.add(new Extra("fire", "Feuer", "Eigene Feuerfarbe und Höhe", "fire", () -> StylesUiConfig.fireCustom, v -> StylesUiConfig.fireCustom = v)
         .color("Farbe", () -> StylesUiConfig.fireColor, v -> StylesUiConfig.fireColor = v)
         .num("Höhe", 10, 100, 5, "%", () -> StylesUiConfig.fireHeight, v -> StylesUiConfig.fireHeight = v));
      l.add(new Extra("block", "Block-Umriss", "Farbe und Dicke", "outline", () -> StylesUiConfig.blockOutlineCustom, v -> StylesUiConfig.blockOutlineCustom = v)
         .color("Farbe", () -> StylesUiConfig.blockOutlineColor, v -> StylesUiConfig.blockOutlineColor = v)
         .num("Dicke", 1, 8, 1, "", () -> StylesUiConfig.blockOutlineWidth, v -> StylesUiConfig.blockOutlineWidth = v));
      l.add(new Extra("crosshair", "Fadenkreuz", "Farbe und Verlauf", "crosshair", () -> StylesUiConfig.crosshairCustom, v -> StylesUiConfig.crosshairCustom = v)
         .color("Farbe", () -> StylesUiConfig.crosshairColor, v -> StylesUiConfig.crosshairColor = v)
         .bool("Verlauf", () -> StylesUiConfig.crosshairGradient, v -> StylesUiConfig.crosshairGradient = v)
         .color("Zweite Farbe", () -> StylesUiConfig.crosshairColor2, v -> StylesUiConfig.crosshairColor2 = v));
      l.add(new Extra("glint", "Verzauberungs-Glanz", "Farbe, Verlauf, Regenbogen", "sparkle", () -> StylesUiConfig.glintCustom, v -> StylesUiConfig.glintCustom = v)
         .color("Farbe", () -> StylesUiConfig.glintColor, v -> StylesUiConfig.glintColor = v)
         .bool("Verlauf", () -> StylesUiConfig.glintGradient, v -> StylesUiConfig.glintGradient = v)
         .color("Zweite Farbe", () -> StylesUiConfig.glintColor2, v -> StylesUiConfig.glintColor2 = v)
         .bool("Regenbogen", () -> StylesUiConfig.glintRainbow, v -> StylesUiConfig.glintRainbow = v));
      l.add(new Extra("sky", "Eigene Uhrzeit", "Feste Tageszeit nur für dich", "sky", () -> StylesUiConfig.timeOverride >= 0, v -> StylesUiConfig.timeOverride = v ? Math.max(0, StylesUiConfig.timeOverride) : -1)
         .num("Uhrzeit", 0, 23999, 250, "", () -> Math.max(0, StylesUiConfig.timeOverride), v -> {
            if (StylesUiConfig.timeOverride >= 0) StylesUiConfig.timeOverride = v;
         }));
      l.add(new Extra("fog", "Nebel", "Nebel aus oder weiter weg", "cloud", () -> StylesUiConfig.fogOff, v -> StylesUiConfig.fogOff = v)
         .num("Sichtweite", 50, 300, 10, "%", () -> StylesUiConfig.fogDistance, v -> StylesUiConfig.fogDistance = v));
      l.add(new Extra("spotify", "Spotify-HUD", "Song-Anzeige von Bauloo", "music", () -> StylesUiConfig.spotifyHud, v -> StylesUiConfig.spotifyHud = v)
         .bool("Popup bei neuem Song", () -> StylesUiConfig.spotifyPopup, v -> StylesUiConfig.spotifyPopup = v));
      l.add(new Extra("watermark", "Wasserzeichen", "Kleines Logo im Bild", "tag", () -> StylesUiConfig.watermark, v -> StylesUiConfig.watermark = v));
      l.add(new Extra("tabicon", "Tab-Icon", "Symbol neben deinem Namen", "list", () -> StylesUiConfig.showInTab, v -> {
         StylesUiConfig.showInTab = v;
         try {
            de.bauloo.styles.StylesPresence.settingChanged();
         } catch (Throwable ignored) {
         }
      }));
      l.add(new Extra("inventory", "Klares Inventar", "Kein dunkler Hintergrund hinter dem Inventar", "backpack", () -> StylesUiConfig.invClearBackground, v -> StylesUiConfig.invClearBackground = v));
      l.add(new Extra("explosion", "Leise Explosionen", "Kein Explosions-Sound", "mute", () -> StylesUiConfig.noExplosionSound, v -> StylesUiConfig.noExplosionSound = v));
      return l;
   }
}
