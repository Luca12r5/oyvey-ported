package dev.visor.render.text;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import java.util.Map;

/**
 * One face (weight or icon set) inside the SDF font atlas. All metrics are in em units, so a
 * glyph drawn at size {@code s} pixels is simply scaled by {@code s}.
 */
public final class SdfFont {
	public final String name;
	public final float ascender;
	public final float descender;
	public final float capHeight;
	public final float xHeight;
	public final float lineHeight;
	private final Int2ObjectOpenHashMap<Glyph> glyphs;
	private final Long2FloatOpenHashMap kerning;
	private final Map<String, Integer> iconNames;
	private final Glyph fallback;

	SdfFont(
		String name,
		float ascender,
		float descender,
		float lineGap,
		float capHeight,
		float xHeight,
		Int2ObjectOpenHashMap<Glyph> glyphs,
		Long2FloatOpenHashMap kerning,
		Map<String, Integer> iconNames
	) {
		this.name = name;
		this.ascender = ascender;
		this.descender = descender;
		this.capHeight = capHeight;
		this.xHeight = xHeight;
		this.lineHeight = ascender - descender + lineGap;
		this.glyphs = glyphs;
		this.kerning = kerning;
		this.iconNames = iconNames;
		Glyph question = glyphs.get('?');
		this.fallback = question != null ? question : new Glyph(0.5F, false, 0, 0, 0, 0, 0, 0, 0, 0);
	}

	public Glyph glyph(int codepoint) {
		Glyph glyph = this.glyphs.get(codepoint);
		return glyph != null ? glyph : this.fallback;
	}

	public boolean has(int codepoint) {
		return this.glyphs.containsKey(codepoint);
	}

	public float kerning(int left, int right) {
		return this.kerning.get(((long) left << 32) | (right & 0xFFFFFFFFL));
	}

	/** Codepoint of a named icon, or -1 when this face has no such icon. */
	public int icon(String name) {
		Integer codepoint = this.iconNames.get(name);
		return codepoint == null ? -1 : codepoint;
	}

	/** Width of a single line of text in pixels, kerning included. */
	public float width(String text, float size) {
		return this.width(text, size, 0.0F);
	}

	public float width(String text, float size, float tracking) {
		float width = 0.0F;
		int previous = -1;
		for (int i = 0; i < text.length(); ) {
			int codepoint = text.codePointAt(i);
			i += Character.charCount(codepoint);
			if (previous >= 0) {
				width += this.kerning(previous, codepoint) * size + tracking;
			}
			width += this.glyph(codepoint).advance() * size;
			previous = codepoint;
		}
		return width;
	}

	/** Longest prefix of {@code text} that fits in {@code maxWidth}, with an ellipsis when cut. */
	public String ellipsize(String text, float size, float maxWidth) {
		if (this.width(text, size) <= maxWidth) {
			return text;
		}
		String ellipsis = "…";
		float budget = maxWidth - this.width(ellipsis, size);
		float width = 0.0F;
		int previous = -1;
		int end = 0;
		for (int i = 0; i < text.length(); ) {
			int codepoint = text.codePointAt(i);
			float advance = this.glyph(codepoint).advance() * size + (previous >= 0 ? this.kerning(previous, codepoint) * size : 0.0F);
			if (width + advance > budget) {
				break;
			}
			width += advance;
			previous = codepoint;
			i += Character.charCount(codepoint);
			end = i;
		}
		return text.substring(0, end).stripTrailing() + ellipsis;
	}

	/**
	 * @param left   left edge of the quad relative to the pen position (em)
	 * @param bottom bottom edge relative to the baseline, y up (em)
	 */
	public record Glyph(
		float advance, boolean visible, float left, float bottom, float right, float top, float u0, float v0, float u1, float v1
	) {
	}
}
