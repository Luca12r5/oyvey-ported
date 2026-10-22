package dev.visor.browser;

import dev.visor.VisorClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;

/**
 * The browser process behind the headset browser: started with its own profile folder and a
 * DevTools port, and always stopped again together with Minecraft.
 */
final class BrowserProcess {
	private static final String LISTENING = "DevTools listening on ";
	private static final List<BrowserProcess> LIVE = new CopyOnWriteArrayList<>();

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			for (BrowserProcess process : LIVE) {
				kill(process.process, 500);
			}
		}, "Visor Browser Shutdown"));
	}

	final Process process;
	final URI devtools;

	private BrowserProcess(Process process, URI devtools) {
		this.process = process;
		this.devtools = devtools;
	}

	/**
	 * @param hidden true: a normal browser window kept off screen (Windows); false: headless
	 */
	static BrowserProcess launch(Path executable, Path profile, boolean hidden, String language) throws Exception {
		Files.createDirectories(profile);
		closeStale(profile);
		List<String> command = new ArrayList<>();
		command.add(executable.toString());
		command.add("--remote-debugging-port=0");
		command.add("--user-data-dir=" + profile.toAbsolutePath());
		command.add("--no-first-run");
		command.add("--no-default-browser-check");
		command.add("--disable-extensions");
		command.add("--disable-sync");
		command.add("--disable-default-apps");
		command.add("--deny-permission-prompts");
		command.add("--noerrdialogs");
		command.add("--hide-crash-restore-bubble");
		command.add("--disable-backgrounding-occluded-windows");
		command.add("--disable-renderer-backgrounding");
		command.add("--disable-background-timer-throttling");
		command.add("--disable-features=CalculateNativeWinOcclusion,Translate,MediaRouter,DialMediaRouteProvider");
		command.add("--autoplay-policy=no-user-gesture-required");
		command.add("--force-device-scale-factor=" + BrowserSession.DEVICE_SCALE);
		command.add("--lang=" + language);
		if (hidden) {
			command.add("--no-startup-window");
		} else {
			command.add("--headless=new");
			command.add("--window-size=1600,1000");
		}
		long started = System.currentTimeMillis();
		Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
		CompletableFuture<URI> endpoint = new CompletableFuture<>();
		Thread reader = new Thread(() -> {
			try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
				String line;
				while ((line = in.readLine()) != null) {
					int at = line.indexOf(LISTENING);
					if (at >= 0 && !endpoint.isDone()) {
						endpoint.complete(URI.create(line.substring(at + LISTENING.length()).trim()));
					}
				}
			} catch (IOException ignored) {
			}
			endpoint.completeExceptionally(new IOException("The browser closed while starting"));
		}, "Visor Browser Output");
		reader.setDaemon(true);
		reader.start();

		// The endpoint is printed on the output; the DevToolsActivePort file is the fallback.
		URI uri = null;
		long deadline = started + 20_000L;
		while (uri == null && System.currentTimeMillis() < deadline) {
			try {
				uri = endpoint.get(250, TimeUnit.MILLISECONDS);
			} catch (java.util.concurrent.TimeoutException e) {
				uri = readActivePort(profile, started);
			} catch (java.util.concurrent.ExecutionException e) {
				uri = readActivePort(profile, started);
				if (uri == null) {
					kill(process, 1000);
					throw new IOException("The browser closed while starting (is another copy using the Visor profile?)");
				}
			}
		}
		if (uri == null) {
			kill(process, 1000);
			throw new IOException("The browser did not start in time");
		}
		BrowserProcess browser = new BrowserProcess(process, uri);
		LIVE.add(browser);
		return browser;
	}

	long pid() {
		return this.process.pid();
	}

	boolean alive() {
		return this.process.isAlive();
	}

	/** Waits briefly for a graceful exit (after Browser.close), then ends the process tree. */
	void stop(long graceMillis) {
		LIVE.remove(this);
		try {
			if (this.process.waitFor(graceMillis, TimeUnit.MILLISECONDS)) {
				return;
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		kill(this.process, 1500);
	}

	private static void kill(Process process, long waitMillis) {
		try {
			process.descendants().forEach(ProcessHandle::destroy);
		} catch (RuntimeException ignored) {
		}
		process.destroy();
		try {
			if (!process.waitFor(waitMillis, TimeUnit.MILLISECONDS)) {
				process.descendants().forEach(ProcessHandle::destroyForcibly);
				process.destroyForcibly();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (RuntimeException ignored) {
		}
	}

	private static @Nullable URI readActivePort(Path profile, long notBefore) {
		Path file = profile.resolve("DevToolsActivePort");
		try {
			if (!Files.isRegularFile(file) || Files.getLastModifiedTime(file).toMillis() < notBefore - 2000L) {
				return null;
			}
			List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
			if (lines.size() < 2) {
				return null;
			}
			return URI.create("ws://127.0.0.1:" + Integer.parseInt(lines.get(0).trim()) + lines.get(1).trim());
		} catch (IOException | RuntimeException e) {
			return null;
		}
	}

	/**
	 * A browser left over from a crashed game still owns the profile folder; ask it to quit so a
	 * fresh one can start.
	 */
	private static void closeStale(Path profile) {
		Path file = profile.resolve("DevToolsActivePort");
		if (!Files.isRegularFile(file)) {
			return;
		}
		try {
			List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
			if (lines.size() >= 2) {
				URI uri = URI.create("ws://127.0.0.1:" + Integer.parseInt(lines.get(0).trim()) + lines.get(1).trim());
				CdpClient stale = CdpClient.connect(uri, event -> {
				}, () -> {
				});
				VisorClient.LOGGER.info("Closing a headset browser left over from a previous session");
				try {
					stale.send("Browser.close", null, null).get(3, TimeUnit.SECONDS);
				} catch (Exception ignored) {
				}
				stale.close();
				Thread.sleep(800);
			}
		} catch (Exception ignored) {
			// Nothing listens there any more: the file is just stale.
		}
		try {
			Files.deleteIfExists(file);
		} catch (IOException ignored) {
		}
	}
}
