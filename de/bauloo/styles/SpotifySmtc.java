package de.bauloo.styles;

final class SpotifySmtc {
   private SpotifySmtc() {
   }

   static SpotifySmtc.Info read() {
      return null;
   }

   static Object[] cover() {
      return null;
   }

   record Info(String title, String artist, boolean playing, long positionMs, long lengthMs) {
   }
}
