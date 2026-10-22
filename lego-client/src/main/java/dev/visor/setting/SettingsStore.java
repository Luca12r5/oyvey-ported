package dev.visor.setting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.visor.VisorClient;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Persists all settings to {@code config/visor/*.json}. Changes mark the store dirty; it is
 * written at most a couple of times per second from the client tick, and once more on exit.
 */
public final class SettingsStore {
	/** Version of the defaults; files from before a change get the new defaults (see Setting.replacesDefault). */
	public static final int VERSION = 2;
	private static final String VERSION_KEY = "config_version";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final List<Entry> FILES = new ArrayList<>();
	private static boolean dirty;
	private static long lastSave;

	private SettingsStore() {
	}

	/** Registers a config file made of a serializer and a deserializer. */
	public static void register(String fileName, Supplier<JsonObject> writer, Consumer<JsonObject> reader) {
		Entry entry = new Entry(directory().resolve(fileName), writer, reader);
		FILES.add(entry);
		entry.load();
	}

	public static void markDirty() {
		dirty = true;
	}

	public static void tick() {
		if (dirty && System.currentTimeMillis() - lastSave > 600) {
			saveNow();
		}
	}

	public static void saveNow() {
		dirty = false;
		lastSave = System.currentTimeMillis();
		for (Entry entry : FILES) {
			entry.save();
		}
	}

	private static Path directory() {
		return FabricLoader.getInstance().getConfigDir().resolve(VisorClient.MOD_ID);
	}

	/** Writes the values of {@code settings} into {@code json}, keyed by id. */
	public static JsonObject write(Iterable<? extends Setting<?>> settings) {
		JsonObject json = new JsonObject();
		for (Setting<?> setting : settings) {
			json.add(setting.id(), setting.toJson());
		}
		return json;
	}

	public static void read(JsonObject json, Iterable<? extends Setting<?>> settings) {
		read(json, settings, VERSION);
	}

	/** @param version the version the values were saved with, see {@link #version} */
	public static void read(JsonObject json, Iterable<? extends Setting<?>> settings, int version) {
		for (Setting<?> setting : settings) {
			JsonElement element = json.get(setting.id());
			if (element != null) {
				setting.fromJson(element);
				if (version < VERSION) {
					setting.upgrade();
				}
			}
		}
	}

	/** The version a file was saved with: 1 for files from before versions were written. */
	public static int version(JsonObject root) {
		JsonElement version = root.get(VERSION_KEY);
		return version != null && version.isJsonPrimitive() && version.getAsJsonPrimitive().isNumber() ? version.getAsInt() : 1;
	}

	/** Marks a file's root object with the current version. */
	public static JsonObject stamp(JsonObject root) {
		root.addProperty(VERSION_KEY, VERSION);
		return root;
	}

	private record Entry(Path path, Supplier<JsonObject> writer, Consumer<JsonObject> reader) {
		void load() {
			if (!Files.exists(this.path)) {
				return;
			}
			try (Reader in = Files.newBufferedReader(this.path, StandardCharsets.UTF_8)) {
				this.reader.accept(JsonParser.parseReader(in).getAsJsonObject());
			} catch (Exception e) {
				VisorClient.LOGGER.warn("Could not read {}, using defaults", this.path, e);
			}
		}

		void save() {
			try {
				Files.createDirectories(this.path.getParent());
				Path temp = this.path.resolveSibling(this.path.getFileName() + ".tmp");
				try (Writer out = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
					GSON.toJson(this.writer.get(), out);
				}
				try {
					Files.move(temp, this.path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
				} catch (java.nio.file.AtomicMoveNotSupportedException e) {
					Files.move(temp, this.path, StandardCopyOption.REPLACE_EXISTING);
				}
			} catch (IOException e) {
				VisorClient.LOGGER.warn("Could not save {}", this.path, e);
			}
		}
	}
}
