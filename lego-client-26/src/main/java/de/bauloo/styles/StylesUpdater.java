// Minecraft 26.3: Bauloo Styles 1.6.0 for 26.2 (decompiled from the clean jar), with the LEGO changes of the 1.21.11 copy.
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
