package dev.visor.ui;

import dev.visor.setting.VisorSettings;
import dev.visor.sound.VisorSounds;
import org.jspecify.annotations.Nullable;

/**
 * Pointer state for one surface, in that surface's layout pixels. Widgets are identified by
 * strings; a click is a press and release on the same widget. Right clicks are delivered on
 * press and can be consumed by the first widget that uses them.
 */
public final class UiInput {
	/** An input that never hovers or clicks; used to draw content behind a modal sheet. */
	public static final UiInput INERT = new UiInput();

	public float x;
	public float y;
	/** Whether the pointer is on this surface at all. */
	public boolean active;
	public boolean down;
	public boolean pressed;
	public boolean released;
	public boolean secondaryPressed;
	public float scroll;

	private @Nullable String hoverId;
	private @Nullable String lastHoverId;
	private @Nullable String pressedId;
	private boolean secondaryConsumed;

	/** Call once per frame before drawing. */
	public void beginFrame() {
		this.lastHoverId = this.hoverId;
		this.hoverId = null;
		this.secondaryConsumed = false;
	}

	/** Call once per frame after drawing. */
	public void endFrame() {
		if (VisorSettings.HOVER_SOUNDS.on() && this.hoverId != null && !this.hoverId.equals(this.lastHoverId)) {
			VisorSounds.play(VisorSounds.HOVER, 0.55F, 1.0F);
		}
		if (this.released) {
			this.pressedId = null;
		}
		this.pressed = false;
		this.released = false;
		this.secondaryPressed = false;
		this.scroll = 0.0F;
	}

	public void clear() {
		this.active = false;
		this.down = false;
		this.pressed = false;
		this.released = false;
		this.secondaryPressed = false;
		this.scroll = 0.0F;
		this.hoverId = null;
		this.lastHoverId = null;
		this.pressedId = null;
	}

	public boolean inside(float x, float y, float w, float h) {
		return this.active && this.x >= x && this.x < x + w && this.y >= y && this.y < y + h;
	}

	/** Registers an interactive area; returns true while hovered. */
	public boolean hover(String id, float x, float y, float w, float h) {
		if (this.inside(x, y, w, h)) {
			this.hoverId = id;
			if (this.pressed) {
				this.pressedId = id;
			}
			return true;
		}
		return false;
	}

	/** True while this widget is being held down with the pointer on it. */
	public boolean held(String id) {
		return this.down && id.equals(this.pressedId) && id.equals(this.hoverId);
	}

	/** True while this widget is held, wherever the pointer is (sliders being dragged). */
	public boolean dragging(String id) {
		return this.down && id.equals(this.pressedId);
	}

	/** True on the frame the widget is clicked (must be called after {@link #hover}). */
	public boolean clicked(String id) {
		return this.released && id.equals(this.pressedId) && id.equals(this.hoverId);
	}

	/** A right click on this area, consumed so that outer handlers don't see it. */
	public boolean secondaryClicked(float x, float y, float w, float h) {
		if (this.secondaryPressed && !this.secondaryConsumed && this.inside(x, y, w, h)) {
			this.secondaryConsumed = true;
			return true;
		}
		return false;
	}

	/** A right click nobody consumed yet. */
	public boolean unhandledSecondary() {
		if (this.secondaryPressed && !this.secondaryConsumed && this.active) {
			this.secondaryConsumed = true;
			return true;
		}
		return false;
	}

	/** A left press that landed on no widget at all this frame. */
	public boolean pressedOnNothing() {
		return this.pressed && this.active && this.hoverId == null;
	}

	public @Nullable String hovered() {
		return this.hoverId;
	}
}
