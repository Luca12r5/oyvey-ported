package dev.visor.render.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.visor.VisorClient;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * Metrics of the baked SDF atlas (see tools/gen_font_atlas.py). The atlas texture itself is
 * owned by {@link dev.visor.render.UiResources}.
 */
public final class FontAtlas {
	public static final Identifier METRICS = VisorClient.id("ui/font_atlas.json");
	public static final Identifier TEXTURE = VisorClient.id("textures/ui/font_atlas.png");

	public final int width;
	public final int height;
	public final float range;
	public final SdfFont regular;
	public final SdfFont medium;
	public final SdfFont semibold;
	public final SdfFont bold;
	public final SdfFont icons;
	public final SdfFont iconsFill;

	private FontAtlas(int width, int height, float range, Map<String, SdfFont> fonts) {
		this.width = width;
		this.height = height;
		this.range = range;
		this.regular = require(fonts, "regular");
		this.medium = require(fonts, "medium");
		this.semibold = require(fonts, "semibold");
		this.bold = require(fonts, "bold");
		this.icons = require(fonts, "icons");
		this.iconsFill = require(fonts, "icons_fill");
	}

	private static SdfFont require(Map<String, SdfFont> fonts, String key) {
		SdfFont font = fonts.get(key);
		if (font == null) {
			throw new IllegalStateException("Font atlas is missing face '" + key + "'");
		}
		return font;
	}

	public static FontAtlas load(ResourceManager resources) throws IOException {
		JsonObject root;
		try (Reader reader = resources.openAsReader(METRICS)) {
			root = JsonParser.parseReader(reader).getAsJsonObject();
		}
		int width = root.get("width").getAsInt();
		int height = root.get("height").getAsInt();
		float range = root.get("range").getAsFloat();
		Map<String, SdfFont> fonts = new HashMap<>();
		for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("fonts").entrySet()) {
			fonts.put(entry.getKey(), parseFont(entry.getKey(), entry.getValue().getAsJsonObject(), width, height));
		}
		return new FontAtlas(width, height, range, fonts);
	}

	private static SdfFont parseFont(String name, JsonObject json, int atlasWidth, int atlasHeight) {
		Int2ObjectOpenHashMap<SdfFont.Glyph> glyphs = new Int2ObjectOpenHashMap<>();
		for (JsonElement element : json.getAsJsonArray("glyphs")) {
			JsonArray row = element.getAsJsonArray();
			int codepoint = row.get(0).getAsInt();
			float advance = row.get(1).getAsFloat();
			if (row.size() > 2) {
				float left = row.get(2).getAsFloat();
				float bottom = row.get(3).getAsFloat();
				float right = row.get(4).getAsFloat();
				float top = row.get(5).getAsFloat();
				float x = row.get(6).getAsFloat();
				float y = row.get(7).getAsFloat();
				float w = row.get(8).getAsFloat();
				float h = row.get(9).getAsFloat();
				glyphs.put(codepoint, new SdfFont.Glyph(
					advance, true, left, bottom, right, top, x / atlasWidth, y / atlasHeight, (x + w) / atlasWidth, (y + h) / atlasHeight
				));
			} else {
				glyphs.put(codepoint, new SdfFont.Glyph(advance, false, 0, 0, 0, 0, 0, 0, 0, 0));
			}
		}
		Long2FloatOpenHashMap kerning = new Long2FloatOpenHashMap();
		kerning.defaultReturnValue(0.0F);
		for (JsonElement element : json.getAsJsonArray("kerning")) {
			JsonArray pair = element.getAsJsonArray();
			long key = ((long) pair.get(0).getAsInt() << 32) | (pair.get(1).getAsInt() & 0xFFFFFFFFL);
			kerning.put(key, pair.get(2).getAsFloat());
		}
		Map<String, Integer> names = new HashMap<>();
		if (json.has("names")) {
			for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("names").entrySet()) {
				names.put(entry.getKey(), entry.getValue().getAsInt());
			}
		}
		return new SdfFont(
			name,
			json.get("ascender").getAsFloat(),
			json.get("descender").getAsFloat(),
			json.get("lineGap").getAsFloat(),
			json.get("capHeight").getAsFloat(),
			json.get("xHeight").getAsFloat(),
			glyphs,
			kerning,
			names
		);
	}
}
