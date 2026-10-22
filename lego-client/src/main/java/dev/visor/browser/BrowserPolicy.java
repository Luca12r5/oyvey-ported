package dev.visor.browser;

import java.net.URI;
import java.util.Locale;
import org.jspecify.annotations.Nullable;

/**
 * What the headset browser may open, the same rules as the launcher's VR browser: web pages over
 * https only (plain http is upgraded), no local files or browser-internal pages, and safe search
 * forced on the big search engines (Google, Bing, DuckDuckGo) and restricted mode on YouTube.
 */
public final class BrowserPolicy {
	private BrowserPolicy() {
	}

	/** The decision for one top-level page load. */
	public record Decision(Kind kind, String url) {
		public static Decision allow(String url) {
			return new Decision(Kind.ALLOW, url);
		}
	}

	public enum Kind {
		/** Load as is. */
		ALLOW,
		/** Load this (same-origin) address instead, e.g. with safe search added. */
		REWRITE,
		/** Stop this load and open {@link Decision#url()} instead (http → https). */
		REDIRECT,
		/** Not allowed at all. */
		BLOCK
	}

	public static Decision check(@Nullable String raw) {
		if (raw == null || raw.isEmpty() || raw.equals("about:blank")) {
			return Decision.allow(raw == null ? "" : raw);
		}
		URI uri;
		try {
			uri = URI.create(raw);
		} catch (IllegalArgumentException e) {
			return new Decision(Kind.BLOCK, raw);
		}
		String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
		String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
		if (scheme.equals("http")) {
			if (host.isEmpty()) {
				return new Decision(Kind.BLOCK, raw);
			}
			return new Decision(Kind.REDIRECT, "https" + raw.substring(4));
		}
		if (!scheme.equals("https") || host.isEmpty()) {
			return new Decision(Kind.BLOCK, raw);
		}
		String safe = safeSearch(raw, host, uri.getRawPath() == null ? "" : uri.getRawPath());
		return safe.equals(raw) ? Decision.allow(raw) : new Decision(Kind.REWRITE, safe);
	}

	/** True for YouTube hosts, whose requests get the restricted-mode header. */
	public static boolean youtube(@Nullable String url) {
		if (url == null) {
			return false;
		}
		try {
			String host = URI.create(url).getHost();
			host = host == null ? "" : host.toLowerCase(Locale.ROOT);
			return host.equals("youtube.com") || host.endsWith(".youtube.com") || host.equals("youtube-nocookie.com") || host.endsWith(".youtube-nocookie.com");
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	static String safeSearch(String url, String host, String path) {
		boolean google = host.matches("(www\\.)?google\\.[a-z.]{2,8}");
		if (google && (path.isEmpty() || path.equals("/") || path.startsWith("/search") || path.startsWith("/webhp") || path.startsWith("/imghp"))) {
			return withParam(url, "safe", "active");
		}
		if ((host.equals("www.bing.com") || host.equals("bing.com")) && path.startsWith("/search")) {
			return withParam(url, "adlt", "strict");
		}
		if (host.equals("duckduckgo.com") || host.equals("html.duckduckgo.com")) {
			return withParam(url, "kp", "1");
		}
		return url;
	}

	/** Sets {@code key=value} in the query, replacing an existing value of that key. */
	static String withParam(String url, String key, String value) {
		int hash = url.indexOf('#');
		String fragment = hash >= 0 ? url.substring(hash) : "";
		String base = hash >= 0 ? url.substring(0, hash) : url;
		int q = base.indexOf('?');
		String path = q >= 0 ? base.substring(0, q) : base;
		String query = q >= 0 ? base.substring(q + 1) : "";
		StringBuilder out = new StringBuilder();
		boolean set = false;
		for (String part : query.split("&")) {
			if (part.isEmpty()) {
				continue;
			}
			String name = part.contains("=") ? part.substring(0, part.indexOf('=')) : part;
			if (name.equals(key)) {
				if (set) {
					continue;
				}
				part = key + "=" + value;
				set = true;
			}
			out.append(out.length() == 0 ? "" : "&").append(part);
		}
		if (!set) {
			out.append(out.length() == 0 ? "" : "&").append(key).append('=').append(value);
		}
		return path + "?" + out + fragment;
	}
}
