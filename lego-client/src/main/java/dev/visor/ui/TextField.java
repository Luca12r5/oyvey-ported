package dev.visor.ui;

import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

/** Minimal single-line text input (search boxes, the address bar). Keyboard events are routed here while focused. */
public final class TextField {
	private final StringBuilder text = new StringBuilder();
	private final int maxLength;
	private long lastEdit;
	private boolean allSelected;
	private @Nullable Consumer<String> onSubmit;

	public TextField(int maxLength) {
		this.maxLength = maxLength;
	}

	public String text() {
		return this.text.toString();
	}

	public boolean isEmpty() {
		return this.text.isEmpty();
	}

	/** Replaces the text, e.g. with the current address when the address bar is clicked. */
	public void setText(String value) {
		this.text.setLength(0);
		this.text.append(value, 0, Math.min(value.length(), this.maxLength));
		this.allSelected = false;
		this.lastEdit = System.currentTimeMillis();
	}

	/** Selects everything: the next typed character replaces the whole text. */
	public void selectAll() {
		this.allSelected = !this.text.isEmpty();
	}

	public boolean allSelected() {
		return this.allSelected;
	}

	/** Called with the text when Enter is pressed. */
	public TextField onSubmit(Consumer<String> action) {
		this.onSubmit = action;
		return this;
	}

	/** @return true when the field handles Enter itself */
	public boolean submit() {
		this.allSelected = false;
		if (this.onSubmit == null) {
			return false;
		}
		this.onSubmit.accept(this.text());
		return true;
	}

	public void type(String characters) {
		if (this.allSelected) {
			this.text.setLength(0);
			this.allSelected = false;
		}
		for (int i = 0; i < characters.length() && this.text.length() < this.maxLength; i++) {
			char c = characters.charAt(i);
			if (c >= 32 && c != 127) {
				this.text.append(c);
			}
		}
		this.lastEdit = System.currentTimeMillis();
	}

	public void backspace(boolean word) {
		if (this.allSelected) {
			this.clear();
			return;
		}
		if (this.text.isEmpty()) {
			return;
		}
		if (word) {
			int end = this.text.length();
			int start = end;
			while (start > 0 && this.text.charAt(start - 1) == ' ') {
				start--;
			}
			while (start > 0 && this.text.charAt(start - 1) != ' ') {
				start--;
			}
			this.text.delete(start, end);
		} else {
			this.text.deleteCharAt(this.text.length() - 1);
		}
		this.lastEdit = System.currentTimeMillis();
	}

	public void clear() {
		this.text.setLength(0);
		this.allSelected = false;
		this.lastEdit = System.currentTimeMillis();
	}

	/** Caret blinks, but stays solid for a moment after typing. */
	public boolean caretVisible() {
		long now = System.currentTimeMillis();
		return now - this.lastEdit < 500 || (now / 530) % 2 == 0;
	}
}
