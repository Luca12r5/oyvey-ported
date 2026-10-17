package dev.lego.ui;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** In-game console buffer: Minecraft's log4j events plus anything printed to System.out / System.err. */
public final class ConsoleLog {
   public record Entry(String time, String level, String source, String text) {
   }

   private static final int MAX = 2000;
   private static final ArrayDeque<Entry> LINES = new ArrayDeque<>();
   private static final DateTimeFormatter HMS = DateTimeFormatter.ofPattern("HH:mm:ss");
   private static boolean installed;

   private ConsoleLog() {
   }

   public static void add(String level, String source, String text) {
      if (text == null) {
         return;
      }

      String time = LocalTime.now().format(HMS);
      synchronized (LINES) {
         for (String line : text.split("\\R")) {
            if (line.isBlank()) {
               continue;
            }

            LINES.addLast(new Entry(time, level, source, line.length() > 600 ? line.substring(0, 600) + "…" : line));
            if (LINES.size() > MAX) {
               LINES.removeFirst();
            }
         }
      }
   }

   public static List<Entry> snapshot() {
      synchronized (LINES) {
         return new ArrayList<>(LINES);
      }
   }

   /** Called first thing in the client initializer. */
   public static void install() {
      if (installed) {
         return;
      }

      installed = true;
      System.setOut(tee(System.out, "INFO", "stdout"));
      System.setErr(tee(System.err, "WARN", "stderr"));

      try {
         Log4jHook.attach();
      } catch (Throwable var1) {
         add("WARN", "console", "Minecraft-Log nicht verfügbar: " + var1);
      }

      add("INFO", "console", "LEGO Client Konsole bereit (F10)");
   }

   private static PrintStream tee(PrintStream original, String level, String source) {
      OutputStream out = new OutputStream() {
         private final StringBuilder buf = new StringBuilder();

         @Override
         public void write(int b) {
            original.write(b);
            synchronized (this.buf) {
               this.take((char)(b & 0xFF));
            }
         }

         @Override
         public void write(byte[] b, int off, int len) {
            original.write(b, off, len);
            String s = new String(b, off, len, StandardCharsets.UTF_8);
            synchronized (this.buf) {
               for (int i = 0; i < s.length(); i++) {
                  this.take(s.charAt(i));
               }
            }
         }

         @Override
         public void flush() {
            original.flush();
         }

         private void take(char c) {
            if (c == '\n') {
               String line = this.buf.toString();
               this.buf.setLength(0);
               if (!line.isEmpty()) {
                  ConsoleLog.add(ConsoleLog.guess(line, level), source, line);
               }
            } else if (c != '\r') {
               this.buf.append(c);
            }
         }
      };
      return new PrintStream(out, true, StandardCharsets.UTF_8);
   }

   static String guess(String line, String fallback) {
      String t = line.trim();
      if (t.startsWith("at ") || t.startsWith("Caused by:") || t.contains("Exception") || t.contains("Error:")) {
         return "ERROR";
      } else {
         return t.contains("WARN") ? "WARN" : fallback;
      }
   }
}
