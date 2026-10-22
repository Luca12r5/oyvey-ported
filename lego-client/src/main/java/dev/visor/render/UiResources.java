package dev.visor.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import dev.visor.VisorClient;
import dev.visor.render.text.FontAtlas;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;

/**
 * GPU resources of the Visor UI: the SDF font atlas, a white fallback texture and the UI images.
 * Reloaded together with the resource packs, always on the render thread.
 */
public final class UiResources {
	private static @Nullable FontAtlas atlas;
	private static @Nullable UiTexture atlasTexture;
	private static @Nullable UiTexture white;
	private static final Map<Identifier, UiTexture> IMAGES = new HashMap<>();
	private static final Set<Identifier> PRELOAD = new LinkedHashSet<>();
	private static @Nullable GpuSampler smoothSampler;

	private UiResources() {
	}

	public static void reload(ResourceManager resources) {
		close();
		try {
			atlas = FontAtlas.load(resources);
			try (InputStream stream = resources.open(FontAtlas.TEXTURE); NativeImage image = NativeImage.read(stream)) {
				atlasTexture = UiTexture.upload("font atlas", image, false, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
			}
			try (NativeImage pixel = new NativeImage(4, 4, false)) {
				pixel.fillRect(0, 0, 4, 4, 0xFFFFFFFF);
				white = UiTexture.upload("white", pixel, false, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
			}
			for (Identifier location : PRELOAD) {
				image(location);
			}
		} catch (Exception e) {
			VisorClient.LOGGER.error("Failed to load the Visor UI resources", e);
			close();
		}
	}

	/** Loads a big picture during the resource reload, so its first use does not stutter. */
	public static void preload(Identifier location) {
		PRELOAD.add(location);
	}

	/** A mip-mapped UI picture from {@code assets/<namespace>/<path>}; loaded on first use. */
	public static @Nullable UiTexture image(Identifier location) {
		UiTexture texture = IMAGES.get(location);
		if (texture == null && !IMAGES.containsKey(location)) {
			try (InputStream stream = net.minecraft.client.Minecraft.getInstance().getResourceManager().open(location);
				NativeImage image = NativeImage.read(stream)) {
				texture = UiTexture.upload(location.toString(), image, true, smoothSampler());
			} catch (Exception e) {
				VisorClient.LOGGER.warn("Missing Visor UI image {}", location, e);
			}
			IMAGES.put(location, texture);
		}
		return texture;
	}

	/** Linear + mipmaps + anisotropic filtering: panels are often seen at an angle. */
	public static GpuSampler smoothSampler() {
		if (smoothSampler == null) {
			GpuDevice device = RenderSystem.getDevice();
			int anisotropy = Math.max(1, Math.min(8, device.getMaxSupportedAnisotropy()));
			smoothSampler = device.createSampler(
				AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, anisotropy, OptionalDouble.empty()
			);
		}
		return smoothSampler;
	}

	public static boolean ready() {
		return atlas != null && atlasTexture != null && white != null;
	}

	public static FontAtlas atlas() {
		if (atlas == null) {
			throw new IllegalStateException("Visor UI resources are not loaded");
		}
		return atlas;
	}

	public static UiTexture atlasTexture() {
		if (atlasTexture == null) {
			throw new IllegalStateException("Visor UI resources are not loaded");
		}
		return atlasTexture;
	}

	public static UiTexture white() {
		if (white == null) {
			throw new IllegalStateException("Visor UI resources are not loaded");
		}
		return white;
	}

	private static void close() {
		if (atlasTexture != null) {
			atlasTexture.close();
			atlasTexture = null;
		}
		if (white != null) {
			white.close();
			white = null;
		}
		for (UiTexture texture : IMAGES.values()) {
			if (texture != null) {
				texture.close();
			}
		}
		IMAGES.clear();
		atlas = null;
	}
}
