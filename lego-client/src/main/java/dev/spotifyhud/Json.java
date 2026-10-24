package dev.spotifyhud;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class Json {
   private final String s;
   private int i;

   private Json(String s) {
      this.s = s;
   }

   public static Object parse(String text) {
      if (text == null) {
         return null;
      } else {
         Json p = new Json(text);
         p.ws();
         return p.i >= p.s.length() ? null : p.value();
      }
   }

   private void ws() {
      while (this.i < this.s.length()) {
         char c = this.s.charAt(this.i);
         if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
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
         char c = this.s.charAt(this.i);
         switch (c) {
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

   private void expect(String w) {
      if (!this.s.startsWith(w, this.i)) {
         throw new IllegalArgumentException("Bad JSON at " + this.i);
      } else {
         this.i = this.i + w.length();
      }
   }

   private Map<String, Object> object() {
      Map<String, Object> m = new LinkedHashMap<>();
      this.i++;
      this.ws();
      if (this.i < this.s.length() && this.s.charAt(this.i) == '}') {
         this.i++;
         return m;
      } else {
         char c;
         do {
            this.ws();
            String k = this.string();
            this.ws();
            if (this.s.charAt(this.i) != ':') {
               throw new IllegalArgumentException("Expected : at " + this.i);
            }

            this.i++;
            m.put(k, this.value());
            this.ws();
            c = this.s.charAt(this.i++);
            if (c == '}') {
               return m;
            }
         } while (c == ',');

         throw new IllegalArgumentException("Expected , at " + (this.i - 1));
      }
   }

   private List<Object> array() {
      List<Object> l = new ArrayList<>();
      this.i++;
      this.ws();
      if (this.i < this.s.length() && this.s.charAt(this.i) == ']') {
         this.i++;
         return l;
      } else {
         char c;
         do {
            l.add(this.value());
            this.ws();
            c = this.s.charAt(this.i++);
            if (c == ']') {
               return l;
            }
         } while (c == ',');

         throw new IllegalArgumentException("Expected , at " + (this.i - 1));
      }
   }

   private String string() {
      if (this.s.charAt(this.i) != '"') {
         throw new IllegalArgumentException("Expected string at " + this.i);
      } else {
         this.i++;
         StringBuilder b = new StringBuilder();

         while (true) {
            char c = this.s.charAt(this.i++);
            if (c == '"') {
               return b.toString();
            }

            if (c == '\\') {
               char e = this.s.charAt(this.i++);
               switch (e) {
                  case 'b':
                     b.append('\b');
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
                     b.append(e);
                     break;
                  case 'f':
                     b.append('\f');
                     break;
                  case 'n':
                     b.append('\n');
                     break;
                  case 'r':
                     b.append('\r');
                     break;
                  case 't':
                     b.append('\t');
                     break;
                  case 'u':
                     b.append((char)Integer.parseInt(this.s.substring(this.i, this.i + 4), 16));
                     this.i += 4;
               }
            } else {
               b.append(c);
            }
         }
      }
   }

   private Object number() {
      int st = this.i;

      while (this.i < this.s.length() && "+-0123456789.eE".indexOf(this.s.charAt(this.i)) >= 0) {
         this.i++;
      }

      String n = this.s.substring(st, this.i);
      if (n.isEmpty()) {
         throw new IllegalArgumentException("Bad JSON value at " + st);
      } else if (!n.contains(".") && !n.contains("e") && !n.contains("E")) {
         try {
            return Long.parseLong(n);
         } catch (NumberFormatException var4) {
            return Double.parseDouble(n);
         }
      } else {
         return Double.parseDouble(n);
      }
   }

   public static Map<String, Object> obj(Object o, String k) {
      if (o instanceof Map) {
         Object v = ((Map)o).get(k);
         if (v instanceof Map) {
            return (Map<String, Object>)v;
         }
      }

      return null;
   }

   public static List<Object> arr(Object o, String k) {
      if (o instanceof Map) {
         Object v = ((Map)o).get(k);
         if (v instanceof List) {
            return (List<Object>)v;
         }
      }

      return null;
   }

   public static String str(Object o, String k, String def) {
      if (o instanceof Map) {
         Object v = ((Map)o).get(k);
         if (v instanceof String) {
            return (String)v;
         }
      }

      return def;
   }

   public static long num(Object o, String k, long def) {
      if (o instanceof Map) {
         Object v = ((Map)o).get(k);
         if (v instanceof Number) {
            return ((Number)v).longValue();
         }
      }

      return def;
   }

   public static double dbl(Object o, String k, double def) {
      if (o instanceof Map) {
         Object v = ((Map)o).get(k);
         if (v instanceof Number) {
            return ((Number)v).doubleValue();
         }
      }

      return def;
   }

   public static boolean bool(Object o, String k, boolean def) {
      if (o instanceof Map) {
         Object v = ((Map)o).get(k);
         if (v instanceof Boolean) {
            return (Boolean)v;
         }
      }

      return def;
   }

   public static String write(Object v) {
      StringBuilder b = new StringBuilder();
      write(b, v, 0);
      return b.toString();
   }

   private static void write(StringBuilder b, Object v, int ind) {
      if (v == null) {
         b.append("null");
      } else if (v instanceof String) {
         quote(b, (String)v);
      } else if (v instanceof Boolean || v instanceof Long || v instanceof Integer) {
         b.append(v);
      } else if (v instanceof Number) {
         b.append(((Number)v).doubleValue());
      } else if (v instanceof Map<String, Object> m) {
         b.append("{\n");
         int n = 0;

         for (Entry<String, Object> e : m.entrySet()) {
            b.append("  ".repeat(ind + 1));
            quote(b, e.getKey());
            b.append(": ");
            write(b, e.getValue(), ind + 1);
            if (++n < m.size()) {
               b.append(',');
            }

            b.append('\n');
         }

         b.append("  ".repeat(ind)).append('}');
      } else if (v instanceof List<Object> l) {
         b.append('[');

         for (int k = 0; k < l.size(); k++) {
            if (k > 0) {
               b.append(", ");
            }

            write(b, l.get(k), ind);
         }

         b.append(']');
      } else {
         quote(b, v.toString());
      }
   }

   private static void quote(StringBuilder b, String s) {
      b.append('"');

      for (int k = 0; k < s.length(); k++) {
         char c = s.charAt(k);
         switch (c) {
            case '\t':
               b.append("\\t");
               break;
            case '\n':
               b.append("\\n");
               break;
            case '\r':
               b.append("\\r");
               break;
            case '"':
               b.append("\\\"");
               break;
            case '\\':
               b.append("\\\\");
               break;
            default:
               if (c < ' ') {
                  b.append(String.format("\\u%04x", Integer.valueOf(c)));
               } else {
                  b.append(c);
               }
         }
      }

      b.append('"');
   }
}
