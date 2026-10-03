package dev.spotifyhud;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class Json {
   private final String s;
   private int i;

   private Json(String var1) {
      this.s = var1;
   }

   public static Object parse(String var0) {
      if (var0 == null) {
         return null;
      } else {
         Json var1 = new Json(var0);
         var1.ws();
         return var1.i >= var1.s.length() ? null : var1.value();
      }
   }

   private void ws() {
      while (this.i < this.s.length()) {
         char var1 = this.s.charAt(this.i);
         if (var1 == ' ' || var1 == '\n' || var1 == '\r' || var1 == '\t') {
            this.i++;
            continue;
         }
         break;
      }
   }

   private Object value() {
      this.ws();
      if (this.i >= this.s.length()) {
         throw new IllegalArgumentException("Unexpected end of JSON");
      } else {
         char var1 = this.s.charAt(this.i);
         switch (var1) {
            case '"':
               return this.string();
            case '[':
               return this.array();
            case 'f':
               this.expect("false");
               return Boolean.FALSE;
            case 'n':
               this.expect("null");
               return null;
            case 't':
               this.expect("true");
               return Boolean.TRUE;
            case '{':
               return this.object();
            default:
               return this.number();
         }
      }
   }

   private void expect(String var1) {
      if (!this.s.startsWith(var1, this.i)) {
         throw new IllegalArgumentException("Bad JSON at " + this.i);
      } else {
         this.i = this.i + var1.length();
      }
   }

   private Map<String, Object> object() {
      LinkedHashMap var1 = new LinkedHashMap();
      this.i++;
      this.ws();
      if (this.i < this.s.length() && this.s.charAt(this.i) == '}') {
         this.i++;
         return var1;
      } else {
         char var3;
         do {
            this.ws();
            String var2 = this.string();
            this.ws();
            if (this.s.charAt(this.i) != ':') {
               throw new IllegalArgumentException("Expected : at " + this.i);
            }

            this.i++;
            var1.put(var2, this.value());
            this.ws();
            var3 = this.s.charAt(this.i++);
            if (var3 == '}') {
               return var1;
            }
         } while (var3 == ',');

         throw new IllegalArgumentException("Expected , at " + (this.i - 1));
      }
   }

   private List<Object> array() {
      ArrayList var1 = new ArrayList();
      this.i++;
      this.ws();
      if (this.i < this.s.length() && this.s.charAt(this.i) == ']') {
         this.i++;
         return var1;
      } else {
         char var2;
         do {
            var1.add(this.value());
            this.ws();
            var2 = this.s.charAt(this.i++);
            if (var2 == ']') {
               return var1;
            }
         } while (var2 == ',');

         throw new IllegalArgumentException("Expected , at " + (this.i - 1));
      }
   }

   private String string() {
      if (this.s.charAt(this.i) != '"') {
         throw new IllegalArgumentException("Expected string at " + this.i);
      } else {
         this.i++;
         StringBuilder var1 = new StringBuilder();

         while (true) {
            char var2 = this.s.charAt(this.i++);
            if (var2 == '"') {
               return var1.toString();
            }

            if (var2 == '\\') {
               char var3 = this.s.charAt(this.i++);
               switch (var3) {
                  case 'b':
                     var1.append('\b');
                     break;
                  case 'c':
                  case 'd':
                  case 'e':
                  case 'g':
                  case 'h':
                  case 'i':
                  case 'j':
                  case 'k':
                  case 'l':
                  case 'm':
                  case 'o':
                  case 'p':
                  case 'q':
                  case 's':
                  default:
                     var1.append(var3);
                     break;
                  case 'f':
                     var1.append('\f');
                     break;
                  case 'n':
                     var1.append('\n');
                     break;
                  case 'r':
                     var1.append('\r');
                     break;
                  case 't':
                     var1.append('\t');
                     break;
                  case 'u':
                     var1.append((char)Integer.parseInt(this.s.substring(this.i, this.i + 4), 16));
                     this.i += 4;
               }
            } else {
               var1.append(var2);
            }
         }
      }
   }

   private Object number() {
      int var1 = this.i;

      while (this.i < this.s.length() && "+-0123456789.eE".indexOf(this.s.charAt(this.i)) >= 0) {
         this.i++;
      }

      String var2 = this.s.substring(var1, this.i);
      if (var2.isEmpty()) {
         throw new IllegalArgumentException("Bad JSON value at " + var1);
      } else if (!var2.contains(".") && !var2.contains("e") && !var2.contains("E")) {
         try {
            return Long.parseLong(var2);
         } catch (NumberFormatException var4) {
            return Double.parseDouble(var2);
         }
      } else {
         return Double.parseDouble(var2);
      }
   }

   public static Map<String, Object> obj(Object var0, String var1) {
      if (var0 instanceof Map) {
         Object var2 = ((Map)var0).get(var1);
         if (var2 instanceof Map) {
            return (Map<String, Object>)var2;
         }
      }

      return null;
   }

   public static List<Object> arr(Object var0, String var1) {
      if (var0 instanceof Map) {
         Object var2 = ((Map)var0).get(var1);
         if (var2 instanceof List) {
            return (List<Object>)var2;
         }
      }

      return null;
   }

   public static String str(Object var0, String var1, String var2) {
      if (var0 instanceof Map) {
         Object var3 = ((Map)var0).get(var1);
         if (var3 instanceof String) {
            return (String)var3;
         }
      }

      return var2;
   }

   public static long num(Object var0, String var1, long var2) {
      if (var0 instanceof Map) {
         Object var4 = ((Map)var0).get(var1);
         if (var4 instanceof Number) {
            return ((Number)var4).longValue();
         }
      }

      return var2;
   }

   public static double dbl(Object var0, String var1, double var2) {
      if (var0 instanceof Map) {
         Object var4 = ((Map)var0).get(var1);
         if (var4 instanceof Number) {
            return ((Number)var4).doubleValue();
         }
      }

      return var2;
   }

   public static boolean bool(Object var0, String var1, boolean var2) {
      if (var0 instanceof Map) {
         Object var3 = ((Map)var0).get(var1);
         if (var3 instanceof Boolean) {
            return (Boolean)var3;
         }
      }

      return var2;
   }

   public static String write(Object var0) {
      StringBuilder var1 = new StringBuilder();
      write(var1, var0, 0);
      return var1.toString();
   }

   private static void write(StringBuilder var0, Object var1, int var2) {
      if (var1 == null) {
         var0.append("null");
      } else if (var1 instanceof String) {
         quote(var0, (String)var1);
      } else if (var1 instanceof Boolean || var1 instanceof Long || var1 instanceof Integer) {
         var0.append(var1);
      } else if (var1 instanceof Number) {
         var0.append(((Number)var1).doubleValue());
      } else if (var1 instanceof Map var3) {
         var0.append("{\n");
         int var4 = 0;

         for (Entry var6 : var3.entrySet()) {
            var0.append("  ".repeat(var2 + 1));
            quote(var0, (String)var6.getKey());
            var0.append(": ");
            write(var0, var6.getValue(), var2 + 1);
            if (++var4 < var3.size()) {
               var0.append(',');
            }

            var0.append('\n');
         }

         var0.append("  ".repeat(var2)).append('}');
      } else if (var1 instanceof List var7) {
         var0.append('[');

         for (int var8 = 0; var8 < var7.size(); var8++) {
            if (var8 > 0) {
               var0.append(", ");
            }

            write(var0, var7.get(var8), var2);
         }

         var0.append(']');
      } else {
         quote(var0, var1.toString());
      }
   }

   private static void quote(StringBuilder var0, String var1) {
      var0.append('"');

      for (int var2 = 0; var2 < var1.length(); var2++) {
         char var3 = var1.charAt(var2);
         switch (var3) {
            case '\t':
               var0.append("\\t");
               break;
            case '\n':
               var0.append("\\n");
               break;
            case '\r':
               var0.append("\\r");
               break;
            case '"':
               var0.append("\\\"");
               break;
            case '\\':
               var0.append("\\\\");
               break;
            default:
               if (var3 < ' ') {
                  var0.append(String.format("\\u%04x", Integer.valueOf(var3)));
               } else {
                  var0.append(var3);
               }
         }
      }

      var0.append('"');
   }
}
