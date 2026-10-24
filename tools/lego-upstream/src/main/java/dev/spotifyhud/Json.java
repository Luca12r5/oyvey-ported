package dev.spotifyhud;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Minimal, dependency-free JSON parser/writer (objects -> LinkedHashMap, arrays -> ArrayList). */
public final class Json {
    private final String s;
    private int i;

    private Json(String s) { this.s = s; }

    public static Object parse(String text) {
        if (text == null) return null;
        Json p = new Json(text);
        p.ws();
        if (p.i >= p.s.length()) return null;
        Object v = p.value();
        return v;
    }

    private void ws() {
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == ' ' || c == '\n' || c == '\r' || c == '\t') i++; else break;
        }
    }

    private Object value() {
        ws();
        if (i >= s.length()) throw new IllegalArgumentException("Unexpected end of JSON");
        char c = s.charAt(i);
        switch (c) {
            case '{': return object();
            case '[': return array();
            case '"': return string();
            case 't': expect("true"); return Boolean.TRUE;
            case 'f': expect("false"); return Boolean.FALSE;
            case 'n': expect("null"); return null;
            default: return number();
        }
    }

    private void expect(String w) {
        if (!s.startsWith(w, i)) throw new IllegalArgumentException("Bad JSON at " + i);
        i += w.length();
    }

    private Map<String, Object> object() {
        Map<String, Object> m = new LinkedHashMap<>();
        i++; ws();
        if (i < s.length() && s.charAt(i) == '}') { i++; return m; }
        while (true) {
            ws();
            String k = string();
            ws();
            if (s.charAt(i) != ':') throw new IllegalArgumentException("Expected : at " + i);
            i++;
            m.put(k, value());
            ws();
            char c = s.charAt(i++);
            if (c == '}') return m;
            if (c != ',') throw new IllegalArgumentException("Expected , at " + (i - 1));
        }
    }

    private List<Object> array() {
        List<Object> l = new ArrayList<>();
        i++; ws();
        if (i < s.length() && s.charAt(i) == ']') { i++; return l; }
        while (true) {
            l.add(value());
            ws();
            char c = s.charAt(i++);
            if (c == ']') return l;
            if (c != ',') throw new IllegalArgumentException("Expected , at " + (i - 1));
        }
    }

    private String string() {
        if (s.charAt(i) != '"') throw new IllegalArgumentException("Expected string at " + i);
        i++;
        StringBuilder b = new StringBuilder();
        while (true) {
            char c = s.charAt(i++);
            if (c == '"') return b.toString();
            if (c == '\\') {
                char e = s.charAt(i++);
                switch (e) {
                    case 'n': b.append('\n'); break;
                    case 't': b.append('\t'); break;
                    case 'r': b.append('\r'); break;
                    case 'b': b.append('\b'); break;
                    case 'f': b.append('\f'); break;
                    case 'u': b.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; break;
                    default: b.append(e);
                }
            } else b.append(c);
        }
    }

    private Object number() {
        int st = i;
        while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
        String n = s.substring(st, i);
        if (n.isEmpty()) throw new IllegalArgumentException("Bad JSON value at " + st);
        if (n.contains(".") || n.contains("e") || n.contains("E")) return Double.parseDouble(n);
        try { return Long.parseLong(n); } catch (NumberFormatException ex) { return Double.parseDouble(n); }
    }

    // ---------- helpers ----------
    @SuppressWarnings("unchecked")
    public static Map<String, Object> obj(Object o, String k) {
        if (o instanceof Map) { Object v = ((Map<String, Object>) o).get(k); if (v instanceof Map) return (Map<String, Object>) v; }
        return null;
    }
    @SuppressWarnings("unchecked")
    public static List<Object> arr(Object o, String k) {
        if (o instanceof Map) { Object v = ((Map<String, Object>) o).get(k); if (v instanceof List) return (List<Object>) v; }
        return null;
    }
    @SuppressWarnings("unchecked")
    public static String str(Object o, String k, String def) {
        if (o instanceof Map) { Object v = ((Map<String, Object>) o).get(k); if (v instanceof String) return (String) v; }
        return def;
    }
    @SuppressWarnings("unchecked")
    public static long num(Object o, String k, long def) {
        if (o instanceof Map) { Object v = ((Map<String, Object>) o).get(k); if (v instanceof Number) return ((Number) v).longValue(); }
        return def;
    }
    @SuppressWarnings("unchecked")
    public static double dbl(Object o, String k, double def) {
        if (o instanceof Map) { Object v = ((Map<String, Object>) o).get(k); if (v instanceof Number) return ((Number) v).doubleValue(); }
        return def;
    }
    @SuppressWarnings("unchecked")
    public static boolean bool(Object o, String k, boolean def) {
        if (o instanceof Map) { Object v = ((Map<String, Object>) o).get(k); if (v instanceof Boolean) return (Boolean) v; }
        return def;
    }

    public static String write(Object v) {
        StringBuilder b = new StringBuilder();
        write(b, v, 0);
        return b.toString();
    }

    @SuppressWarnings("unchecked")
    private static void write(StringBuilder b, Object v, int ind) {
        if (v == null) b.append("null");
        else if (v instanceof String) quote(b, (String) v);
        else if (v instanceof Boolean || v instanceof Long || v instanceof Integer) b.append(v);
        else if (v instanceof Number) b.append(((Number) v).doubleValue());
        else if (v instanceof Map) {
            Map<String, Object> m = (Map<String, Object>) v;
            b.append("{\n");
            int n = 0;
            for (Map.Entry<String, Object> e : m.entrySet()) {
                b.append("  ".repeat(ind + 1));
                quote(b, e.getKey());
                b.append(": ");
                write(b, e.getValue(), ind + 1);
                if (++n < m.size()) b.append(',');
                b.append('\n');
            }
            b.append("  ".repeat(ind)).append('}');
        } else if (v instanceof List) {
            List<Object> l = (List<Object>) v;
            b.append('[');
            for (int k = 0; k < l.size(); k++) { if (k > 0) b.append(", "); write(b, l.get(k), ind); }
            b.append(']');
        } else quote(b, v.toString());
    }

    private static void quote(StringBuilder b, String s) {
        b.append('"');
        for (int k = 0; k < s.length(); k++) {
            char c = s.charAt(k);
            switch (c) {
                case '"': b.append("\\\""); break;
                case '\\': b.append("\\\\"); break;
                case '\n': b.append("\\n"); break;
                case '\r': b.append("\\r"); break;
                case '\t': b.append("\\t"); break;
                default:
                    if (c < 0x20) b.append(String.format("\\u%04x", (int) c)); else b.append(c);
            }
        }
        b.append('"');
    }
}
