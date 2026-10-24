package dev.spotifyhud;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Finds out which program blocks a local port (Windows: netstat + tasklist) and finds a free one. */
final class PortInfo {
    private PortInfo() { }

    static String owner(int port) {
        if (!SmtcBridge.isWindows()) return null;
        try {
            String pid = null;
            for (String line : run("netstat", "-ano", "-p", "TCP")) {
                String[] tk = line.trim().split("\\s+");
                if (tk.length < 5 || !tk[0].equalsIgnoreCase("TCP")) continue;
                if (!tk[1].endsWith(":" + port)) continue;
                boolean listening = tk[2].endsWith(":0") || tk[2].endsWith(":*");
                if (listening || pid == null) pid = tk[tk.length - 1];
                if (listening) break;
            }
            if (pid == null || !pid.matches("\\d+")) return null;
            for (String line : run("tasklist", "/FI", "PID eq " + pid, "/FO", "CSV", "/NH")) {
                line = line.trim();
                if (line.startsWith("\"")) {
                    int end = line.indexOf('"', 1);
                    if (end > 1) return line.substring(1, end) + " (PID " + pid + ")";
                }
            }
            return "PID " + pid;
        } catch (Exception e) {
            return null;
        }
    }

    static int findFree(int from, int to) {
        for (int p = from; p <= to && p <= 65535; p++) {
            try (ServerSocket s = new ServerSocket()) {
                s.setReuseAddress(false);
                s.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), p));
                return p;
            } catch (Exception ignored) { }
        }
        return -1;
    }

    private static List<String> run(String... cmd) throws Exception {
        Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        List<String> out = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String l;
            while ((l = r.readLine()) != null) out.add(l);
        }
        p.waitFor(5, TimeUnit.SECONDS);
        return out;
    }
}
