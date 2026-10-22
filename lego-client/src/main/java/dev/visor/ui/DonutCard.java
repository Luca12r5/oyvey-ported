package dev.visor.ui;

import dev.visor.donut.DonutPreview;
import dev.visor.donut.DonutRenderer;
import dev.visor.donut.DrDonutt;
import dev.visor.render.UiCanvas;
import dev.visor.render.text.FontAtlas;
import dev.visor.sound.VisorSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Doctor Donut's own card at the top of his settings: a live picture of him, whether he is out
 * there and where, and big buttons to summon him, bring him back or switch him off.
 */
final class DonutCard {
	static final float HEIGHT = 178.0F;
	private static final float RADIUS = 16.0F;
	private static final float BUTTON_H = 40.0F;
	private static @Nullable DonutCard instance;

	private final DonutPreview preview = new DonutPreview(DonutRenderer.Slot.CARD);

	static DonutCard get() {
		if (instance == null) {
			instance = new DonutCard();
		}
		return instance;
	}

	/** Draws the card with its top left corner at (x, y) and returns the y below it. */
	float render(UiCanvas c, UiInput in, UiAnimations anim, FontAtlas f, float x, float y, float w, float time) {
		DrDonutt donut = DrDonutt.get();
		boolean on = donut.enabled();
		float lit = anim.get("donut.card.on", on, 10.0F);
		float h = HEIGHT;

		// The frosting-pink art of the banner on the home page, a little calmer while he is off.
		c.shadow(x, y + 6, w, h, RADIUS, 18, 0x59000000);
		c.aurora(x, y, w, h, RADIUS, 0.62F, 1, Theme.mix(0xFFA0A0A0, 0xFFFFFFFF, 0.45F + 0.55F * lit));
		c.roundRectGradient(x, y, w, h * 0.55F, RADIUS, 0x1CFFFFFF, 0x00FFFFFF);
		c.pushClip(x + 6, y + 3, w - 12, h - 6);
		HomePage.sprinkles(c, x, y, w, h, time);
		c.popClip();

		// Him, live, in front of a giant donut, doing what his settings say.
		float pw = 176;
		float px = x + w - pw - 4;
		float cx = px + pw * 0.5F;
		float cy = y + h * 0.5F;
		HomePage.frostedDonut(c, cx, cy, 60, time);
		c.shadow(cx - 32, y + h - 13, 64, 7, 3.5F, 9, 0x70000000);
		String clip = donut.animation();
		double clipTime = clip.equals("Dance") ? donut.danceTime(time) : time;
		float yaw = (float) Math.sin(time * 0.65F) * 26.0F;
		if (!this.preview.draw(c, px, y + 4, pw, h - 6, clip, clipTime, yaw)) {
			c.arc(cx, cy, 15, 3.5F, time * 5.0F, 4.2F, 0xCCFFFFFF);
		}

		// What he is up to, his name, where he is, and the buttons.
		float tx = x + 24;
		float chipY = y + 18;
		String chip = on ? status(clip) : "SWITCHED OFF";
		float chipW = f.bold.width(chip, 11, 1.4F) + 42;
		c.roundRect(tx - 4, chipY, chipW, 24, 12, 0x52000000);
		if (on) {
			float pulse = 0.5F + 0.5F * (float) Math.sin(time * 5.0F);
			c.circle(tx + 10, chipY + 12, 4.0F + pulse * 3.0F, Theme.alpha(Theme.ONLINE, 0.35F * (1.0F - pulse)));
			c.circle(tx + 10, chipY + 12, 4.0F, Theme.ONLINE);
		} else {
			c.circle(tx + 10, chipY + 12, 4.0F, 0xFFB8BBC0);
		}
		c.text(f.bold, chip, tx + 23, chipY + 12 + f.bold.capHeight * 5.5F, 11, Theme.TEXT, 0, 1.4F, 0);

		float titleY = y + 84;
		c.text(f.bold, "Doctor Donut", tx, titleY + 2, 32, 0x59000000, 0, -0.4F, 4.0F);
		c.text(f.bold, "Doctor Donut", tx, titleY, 32, Theme.TEXT, 0, -0.4F, 0);
		String line = on ? where(donut) : "Summon him and he dances right in front of you";
		c.textFit(f.medium, line, tx, y + 108, 14, w - pw - 44, 0xF0FFFFFF);

		float by = y + h - BUTTON_H - 16;
		if (!on) {
			if (button(c, in, anim, f, "donut.card.summon", "sparkle", "Summon", tx, by, true)) {
				donut.summon();
				VisorSounds.play(VisorSounds.NOTIFY, 0.9F);
			}
		} else {
			if (button(c, in, anim, f, "donut.card.here", "map-pin", "Bring here", tx, by, true)) {
				donut.summon();
				VisorSounds.play(VisorSounds.RECENTER, 0.8F);
			}
			float next = tx + buttonWidth(f, "Bring here") + 10;
			if (button(c, in, anim, f, "donut.card.off", "power", "Turn off", next, by, false)) {
				donut.setEnabled(false);
				VisorSounds.play(VisorSounds.TOGGLE_OFF, 0.8F);
			}
		}
		return y + h;
	}

	private static String status(String clip) {
		return switch (clip) {
			case "Wave" -> "WAVING NOW";
			case "Idle" -> "STANDING BY";
			case "Walk" -> "WALKING NOW";
			case "Run" -> "RUNNING NOW";
			default -> "DANCING NOW";
		};
	}

	private static String where(DrDonutt donut) {
		Vec3 feet = donut.feet();
		LocalPlayer player = Minecraft.getInstance().player;
		if (feet == null || player == null) {
			return "On his way to you…";
		}
		long blocks = Math.round(Math.sqrt(feet.distanceToSqr(player.position())));
		String away = blocks <= 1 ? "Right next to you" : blocks + " blocks away";
		return away + (donut.following() ? " · follows you" : " · stays in place");
	}

	/** Pill shaped button with an icon, white for the main action; true when clicked. */
	private static boolean button(UiCanvas c, UiInput in, UiAnimations anim, FontAtlas f, String id, String icon, String label, float x, float y, boolean primary) {
		float w = buttonWidth(f, label);
		float h = BUTTON_H;
		boolean hovered = in.hover(id, x, y, w, h);
		float hover = anim.get(id + ".h", hovered, 18.0F);
		float press = anim.get(id + ".p", in.held(id), 30.0F);
		c.push();
		c.scale(1.0F + hover * 0.03F - press * 0.05F, x + w * 0.5F, y + h * 0.5F);
		c.shadow(x, y + 3, w, h, h * 0.5F, 8 + hover * 6, Theme.alpha(0xFF000000, 0.22F + hover * 0.12F));
		int fill = primary ? Theme.mix(0xF0FFFFFF, 0xFFFFFFFF, hover) : Theme.mix(0x59000000, Theme.alpha(Theme.DANGER, 0.92F), hover);
		int ink = primary ? HomePage.FROSTING_DARK : Theme.TEXT;
		c.roundRect(x, y, w, h, h * 0.5F, fill);
		c.icon(icon, true, x + 24, y + h * 0.5F, 17, ink);
		c.textMiddle(f.semibold, label, x + 40, y + h * 0.5F, 15, ink);
		c.pop();
		return in.clicked(id);
	}

	private static float buttonWidth(FontAtlas f, String label) {
		return f.semibold.width(label, 15) + 62;
	}
}
