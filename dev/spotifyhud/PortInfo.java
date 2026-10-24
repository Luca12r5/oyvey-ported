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

   static String owner(int port) {
      if (!SmtcBridge.isWindows()) {
         return null;
      } else {
         try {
            String pid = null;

            for (String line : run("netstat", "-ano", "-p", "TCP")) {
               String[] tk = line.trim().split("\\s+");
               if (tk.length >= 5 && tk[0].equalsIgnoreCase("TCP") && tk[1].endsWith(":" + port)) {
                  boolean listening = tk[2].endsWith(":0") || tk[2].endsWith(":*");
                  if (listening || pid == null) {
                     pid = tk[tk.length - 1];
                  }

                  if (listening) {
                     break;
                  }
               }
            }

            if (pid != null && pid.matches("\\d+")) {
               for (String linex : run("tasklist", "/FI", "PID eq " + pid, "/FO", "CSV", "/NH")) {
                  linex = linex.trim();
                  if (linex.startsWith("\"")) {
                     int end = linex.indexOf(34, 1);
                     if (end > 1) {
                        return linex.substring(1, end) + " (PID " + pid + ")";
                     }
                  }
               }

               return "PID " + pid;
            } else {
               return null;
            }
         } catch (Exception var6) {
            return null;
         }
      }
   }

   static int findFree(int from, int to) {
      for (int p = from; p <= to && p <= 65535; p++) {
         try {
            int var4;
            try (ServerSocket s = new ServerSocket()) {
               s.setReuseAddress(false);
               s.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), p));
               var4 = p;
            }

            return var4;
         } catch (Exception var8) {
         }
      }

      return -1;
   }

   private static List<String> run(String... cmd) throws Exception {
      Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
      List<String> out = new ArrayList<>();

      String l;
      try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
         while ((l = r.readLine()) != null) {
            out.add(l);
         }
      }

      p.waitFor(5L, TimeUnit.SECONDS);
      return out;
   }
}
