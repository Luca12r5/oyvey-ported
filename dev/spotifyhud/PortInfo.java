package dev.spotifyhud;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

final class PortInfo {
   private PortInfo() {
   }

   static String owner(int var0) {
      if (!SmtcBridge.isWindows()) {
         return null;
      } else {
         try {
            String var1 = null;

            for (String var3 : run("netstat", "-ano", "-p", "TCP")) {
               String[] var4 = var3.trim().split("\\s+");
               if (var4.length >= 5 && var4[0].equalsIgnoreCase("TCP") && var4[1].endsWith(":" + var0)) {
                  boolean var5 = var4[2].endsWith(":0") || var4[2].endsWith(":*");
                  if (var5 || var1 == null) {
                     var1 = var4[var4.length - 1];
                  }

                  if (var5) {
                     break;
                  }
               }
            }

            if (var1 != null && var1.matches("\\d+")) {
               for (String var8 : run("tasklist", "/FI", "PID eq " + var1, "/FO", "CSV", "/NH")) {
                  var8 = var8.trim();
                  if (var8.startsWith("\"")) {
                     int var10 = var8.indexOf(34, 1);
                     if (var10 > 1) {
                        return var8.substring(1, var10) + " (PID " + var1 + ")";
                     }
                  }
               }

               return "PID " + var1;
            } else {
               return null;
            }
         } catch (Exception var6) {
            return null;
         }
      }
   }

   static int findFree(int var0, int var1) {
      for (int var2 = var0; var2 <= var1 && var2 <= 65535; var2++) {
         try {
            int var4;
            try (ServerSocket var3 = new ServerSocket()) {
               var3.setReuseAddress(false);
               var3.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), var2));
               var4 = var2;
            }

            return var4;
         } catch (Exception var8) {
         }
      }

      return -1;
   }

   private static List<String> run(String... var0) throws Exception {
      Process var1 = new ProcessBuilder(var0).redirectErrorStream(true).start();
      ArrayList var2 = new ArrayList();

      String var4;
      try (BufferedReader var3 = new BufferedReader(new InputStreamReader(var1.getInputStream()))) {
         while ((var4 = var3.readLine()) != null) {
            var2.add(var4);
         }
      }

      var1.waitFor(5L, TimeUnit.SECONDS);
      return var2;
   }
}
