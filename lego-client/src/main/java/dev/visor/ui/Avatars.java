package dev.visor.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import dev.visor.render.UiCanvas;
import java.util.OptionalDouble;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/** Round player heads (face + hat layer) cut from skin textures. */
public final class Avatars {
	private static @Nullable GpuSampler sampler;

	private Avatars() {
	}

	/** Skin of the local player, or a default skin before one is known. */
	public static Identifier localSkin() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player != null) {
			return minecraft.player.getSkin().body().texturePath();
		}
		return DefaultPlayerSkin.getDefaultTexture();
	}

	/** One of the built-in default skins, picked deterministically from a name. */
	public static Identifier defaultSkin(String name) {
		return DefaultPlayerSkin.get(UUID.nameUUIDFromBytes(name.getBytes())).body().texturePath();
	}

	public static void draw(UiCanvas canvas, Identifier skin, float cx, float cy, float size) {
		float r = size * 0.5F;
		canvas.circle(cx, cy, r, 0xFF3A3C3F);
		GpuTextureView view = view(skin);
		if (view == null) {
			return;
		}
		GpuSampler sampler = sampler();
		float px = 1.0F / 64.0F;
		canvas.image(view, sampler, cx - r, cy - r, size, size, 8 * px, 8 * px, 16 * px, 16 * px, r, 0xFFFFFFFF);
		canvas.image(view, sampler, cx - r, cy - r, size, size, 40 * px, 8 * px, 48 * px, 16 * px, r, 0xFFFFFFFF);
	}

	private static @Nullable GpuTextureView view(Identifier skin) {
		try {
			return Minecraft.getInstance().getTextureManager().getTexture(skin).getTextureView();
		} catch (RuntimeException e) {
			return null;
		}
	}

	/** Smooth when small, crisp pixels when large: the face is pixel art after all. */
	private static GpuSampler sampler() {
		if (sampler == null) {
			sampler = RenderSystem.getDevice().createSampler(
				AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.NEAREST, 1, OptionalDouble.of(0.0)
			);
		}
		return sampler;
	}
}
