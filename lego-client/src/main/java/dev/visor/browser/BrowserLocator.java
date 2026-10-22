package dev.visor.browser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jspecify.annotations.Nullable;

/**
 * Finds a Chromium based browser on this computer (Chrome, Edge, Brave or Chromium). The headset
 * browser drives that installation through the DevTools protocol with its own, separate profile;
 * nothing of the user's normal browser profile is touched.
 */
public final class BrowserLocator {
	public record Found(Path executable, String name) {
	}

	private record Candidate(String name, Path path) {
	}

	private BrowserLocator() {
	}

	public static @Nullable Found find(@Nullable String override) {
		if (override != null && !override.isBlank()) {
			Path path = Path.of(override.trim());
			if (Files.isRegularFile(path)) {
				return new Found(path, path.getFileName().toString());
			}
		}
		for (Candidate candidate : candidates()) {
			if (Files.isRegularFile(candidate.path())) {
				return new Found(candidate.path(), candidate.name());
			}
		}
		return null;
	}

	public static boolean isWindows() {
		return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
	}

	private static List<Candidate> candidates() {
		List<Candidate> list = new ArrayList<>();
		String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
		if (os.contains("win")) {
			List<String> roots = new ArrayList<>();
			for (String variable : new String[]{"ProgramFiles", "ProgramFiles(x86)", "LOCALAPPDATA", "ProgramW6432"}) {
				String value = System.getenv(variable);
				if (value != null && !value.isBlank() && !roots.contains(value)) {
					roots.add(value);
				}
			}
			for (String root : roots) {
				list.add(new Candidate("Google Chrome", Path.of(root, "Google", "Chrome", "Application", "chrome.exe")));
			}
			for (String root : roots) {
				list.add(new Candidate("Microsoft Edge", Path.of(root, "Microsoft", "Edge", "Application", "msedge.exe")));
			}
			for (String root : roots) {
				list.add(new Candidate("Brave", Path.of(root, "BraveSoftware", "Brave-Browser", "Application", "brave.exe")));
				list.add(new Candidate("Chromium", Path.of(root, "Chromium", "Application", "chrome.exe")));
			}
		} else if (os.contains("mac")) {
			String home = System.getProperty("user.home", "");
			for (String base : new String[]{"/Applications", home + "/Applications"}) {
				list.add(new Candidate("Google Chrome", Path.of(base, "Google Chrome.app", "Contents", "MacOS", "Google Chrome")));
				list.add(new Candidate("Microsoft Edge", Path.of(base, "Microsoft Edge.app", "Contents", "MacOS", "Microsoft Edge")));
				list.add(new Candidate("Brave", Path.of(base, "Brave Browser.app", "Contents", "MacOS", "Brave Browser")));
				list.add(new Candidate("Chromium", Path.of(base, "Chromium.app", "Contents", "MacOS", "Chromium")));
			}
		} else {
			String[][] names = {
				{"Google Chrome", "google-chrome"}, {"Google Chrome", "google-chrome-stable"}, {"Chromium", "chromium"},
				{"Chromium", "chromium-browser"}, {"Microsoft Edge", "microsoft-edge"}, {"Microsoft Edge", "microsoft-edge-stable"},
				{"Brave", "brave-browser"}
			};
			List<String> dirs = new ArrayList<>(List.of("/usr/bin", "/usr/local/bin", "/snap/bin"));
			String path = System.getenv("PATH");
			if (path != null) {
				for (String dir : path.split(":")) {
					if (!dir.isBlank() && !dirs.contains(dir)) {
						dirs.add(dir);
					}
				}
			}
			for (String[] name : names) {
				for (String dir : dirs) {
					list.add(new Candidate(name[0], Path.of(dir, name[1])));
				}
			}
		}
		return list;
	}
}
