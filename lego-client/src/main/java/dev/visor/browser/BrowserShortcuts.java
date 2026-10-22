package dev.visor.browser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.visor.VisorClient;
import dev.visor.setting.VisorSettings;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;
import org.jspecify.annotations.Nullable;

/**
 * Quick links of the headset browser (home screen and start page) and its small config file,
 * {@code config/visor/browser.json}, where the links and the browser to use can be changed.
 */
public final class BrowserShortcuts {
	public record Shortcut(String name, String url, String icon, int color) {
	}

	private static final List<Shortcut> DEFAULTS = List.of(
		new Shortcut("YouTube", "https://www.youtube.com", "youtube-logo", 0xFFFF0033),
		new Shortcut("Google", "https://www.google.com", "google-logo", 0xFF4285F4),
		new Shortcut("Twitch", "https://www.twitch.tv", "twitch-logo", 0xFF9146FF),
		new Shortcut("MC Wiki", "https://minecraft.wiki", "cube", 0xFF4F9E2D),
		new Shortcut("Discord", "https://discord.com/app", "discord-logo", 0xFF5865F2),
		new Shortcut("Reddit", "https://www.reddit.com", "reddit-logo", 0xFFFF4500),
		new Shortcut("Modrinth", "https://modrinth.com", "package", 0xFF1BB566),
		new Shortcut("Wikipedia", "https://www.wikipedia.org", "book-open-text", 0xFF6B7280)
	);
	private static final Pattern SCHEME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*:.*");
	private static final Pattern HOST = Pattern.compile("^(localhost|[\\w-]+(\\.[\\w-]+)+)(:\\d+)?([/?#].*)?$");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static List<Shortcut> shortcuts = DEFAULTS;
	private static String executable = "";

	private BrowserShortcuts() {
	}

	public static List<Shortcut> all() {
		return shortcuts;
	}

	public static @Nullable String executableOverride() {
		return executable.isBlank() ? null : executable;
	}

	/** Turns what was typed in a search bar into a URL: a web address as is, anything else a search. */
	public static @Nullable String resolve(String input) {
		String text = input.trim();
		if (text.isEmpty()) {
			return null;
		}
		if (!text.contains(" ") && SCHEME.matcher(text).matches() && (text.contains("://") || text.startsWith("about:"))) {
			return text;
		}
		if (!text.contains(" ") && HOST.matcher(text).matches()) {
			return (text.startsWith("localhost") ? "http://" : "https://") + text;
		}
		return searchUrl(text);
	}

	public static String searchUrl(String query) {
		String q = URLEncoder.encode(query, StandardCharsets.UTF_8);
		return switch (VisorSettings.SEARCH_ENGINE.get()) {
			case "Bing" -> "https://www.bing.com/search?q=" + q;
			case "DuckDuckGo" -> "https://duckduckgo.com/?q=" + q;
			default -> "https://www.google.com/search?q=" + q;
		};
	}

	public static String searchPlaceholder() {
		return "Search " + VisorSettings.SEARCH_ENGINE.get() + " or type a URL";
	}

	public static void init() {
		Path file = FabricLoader.getInstance().getConfigDir().resolve("visor").resolve("browser.json");
		try {
			if (Files.isRegularFile(file)) {
				JsonObject json = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
				List<Shortcut> loaded = new ArrayList<>();
				if (json.has("shortcuts") && json.get("shortcuts").isJsonArray()) {
					for (JsonElement element : json.getAsJsonArray("shortcuts")) {
						Shortcut shortcut = parse(element);
						if (shortcut != null) {
							loaded.add(shortcut);
						}
					}
				}
				if (!loaded.isEmpty()) {
					shortcuts = List.copyOf(loaded.subList(0, Math.min(8, loaded.size())));
				}
				if (json.has("browser_executable") && json.get("browser_executable").isJsonPrimitive()) {
					executable = json.get("browser_executable").getAsString();
				}
			} else {
				Files.createDirectories(file.getParent());
				Files.writeString(file, GSON.toJson(defaultsJson()), StandardCharsets.UTF_8);
			}
		} catch (Exception e) {
			VisorClient.LOGGER.warn("Could not read {}; using the default browser shortcuts", file, e);
		}
	}

	private static @Nullable Shortcut parse(JsonElement element) {
		if (!element.isJsonObject()) {
			return null;
		}
		JsonObject object = element.getAsJsonObject();
		if (!object.has("name") || !object.has("url")) {
			return null;
		}
		String icon = object.has("icon") ? object.get("icon").getAsString() : "globe-simple";
		int color = 0xFF3B82F6;
		if (object.has("color")) {
			try {
				String hex = object.get("color").getAsString().replace("#", "");
				long value = Long.parseLong(hex, 16);
				color = hex.length() <= 6 ? (int) (0xFF000000L | value) : (int) value;
			} catch (RuntimeException ignored) {
			}
		}
		return new Shortcut(object.get("name").getAsString(), object.get("url").getAsString(), icon, color);
	}

	private static JsonObject defaultsJson() {
		JsonObject json = new JsonObject();
		json.addProperty("_comment", "Quick links of the headset browser (max 8, icons are Phosphor icon names). "
			+ "browser_executable: optional path to chrome.exe / msedge.exe; empty = find one automatically.");
		JsonArray array = new JsonArray();
		for (Shortcut shortcut : DEFAULTS) {
			JsonObject entry = new JsonObject();
			entry.addProperty("name", shortcut.name());
			entry.addProperty("url", shortcut.url());
			entry.addProperty("icon", shortcut.icon());
			entry.addProperty("color", String.format(Locale.ROOT, "#%06X", shortcut.color() & 0xFFFFFF));
			array.add(entry);
		}
		json.add("shortcuts", array);
		json.addProperty("browser_executable", "");
		return json;
	}
}
