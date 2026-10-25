package de.bauloo.styles;

import java.util.List;

/**
 * Inside the LEGO Client the Bauloo self-updater is left out: the LEGO Launcher updates the client
 * itself, and nothing downloads jars into the mods folder. Only the fields the menu reads remain.
 */
public final class StylesUpdater {
   public static volatile String availableVersion;
   public static volatile List<String[]> availableNotes = List.of();
   public static volatile String installedVersion = "1.6.0";

   private StylesUpdater() {
   }
}
