package dev.visor.ui;

import dev.visor.render.UiCanvas;
import dev.visor.render.UiResources;
import dev.visor.render.text.FontAtlas;

/** Logo shown on black while the headset "boots", drawn in screen space. */
public final class BootScreen {
	private BootScreen() {
	}

	/**
	 * @param alpha visibility 0..1
	 * @param time  seconds since the logo started showing (drives the loading dots)
	 */
	public static void render(UiCanvas c, float width, float height, float alpha, float time, String name) {
		if (alpha <= 0.001F) {
			return;
		}
		FontAtlas f = UiResources.atlas();
		float unit = height / 1080.0F;
		float cx = width * 0.5F;
		float cy = height * 0.46F;
		float grow = 0.94F + 0.06F * Ease.outCubic(Math.min(1.0F, time / 0.6F));
		c.pushAlpha(alpha);
		c.push();
		c.scale(grow, cx, cy);
		float logo = 120 * unit;
		c.shadow(cx - logo * 0.6F, cy - logo * 0.45F, logo * 1.2F, logo * 0.9F, logo * 0.4F, logo * 0.5F, Theme.alpha(Theme.accentBright(), 0.22F));
		c.icon("virtual-reality", true, cx, cy, logo, Theme.TEXT);
		c.textCentered(f.semibold, name, cx, cy + logo * 0.78F, 34 * unit, Theme.TEXT);
		// Three dots breathing in sequence.
		float dotY = cy + logo * 1.28F;
		for (int i = 0; i < 3; i++) {
			float phase = (float) Math.sin((time * 5.0F) - i * 0.9F) * 0.5F + 0.5F;
			float r = (4.0F + phase * 2.0F) * unit;
			c.circle(cx + (i - 1) * 22 * unit, dotY, r, Theme.alpha(Theme.TEXT, 0.35F + phase * 0.65F));
		}
		c.pop();
		c.popAlpha();
	}
}
