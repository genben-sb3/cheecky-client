package dev.cheecky.gui;

import com.mojang.blaze3d.platform.InputConstants;

import dev.cheecky.compat.Compat;
import dev.cheecky.module.Module;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Klick, dann Taste drücken. Entf, Backspace oder Esc entfernt die Taste. */
public class KeybindComponent extends GuiComponent {
	private final Module module;

	public KeybindComponent(Module module) {
		this.module = module;
	}

	@Override
	public int getHeight() {
		return 11;
	}

	@Override
	public int getPreferredWidth() {
		return font().width("Taste: Drücke eine Taste...") + 6;
	}

	private boolean isListening() {
		return ClickGui.INSTANCE.getListeningKeybind() == this;
	}

	@Override
	public void render(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		boolean listening = isListening();
		int color = listening ? Theme.withAlpha(Theme.ENABLED, Theme.OPACITY) : Theme.background(isHovering(mouseX, mouseY));
		g.fill(x, y, x + width, y + getHeight(), color);
		drawOutline(g, x, y, x + width, y + getHeight());

		String text = "Taste: " + (listening ? "Drücke eine Taste..." : keyName(module.getKey()));
		g.text(font(), text, x + (width - font().width(text)) / 2, y + 2, Theme.TEXT, false);
	}

	private static String keyName(int key) {
		if (key == 0)
			return "keine";
		return Compat.KEYBOARD.getOrCreate(key).getDisplayName().getString();
	}

	@Override
	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0)
			ClickGui.INSTANCE.setListeningKeybind(isListening() ? null : this);
	}

	public void onKey(int key) {
		boolean clear = key == InputConstants.KEY_ESCAPE || key == InputConstants.KEY_DELETE
			|| key == InputConstants.KEY_BACKSPACE;
		module.setKey(clear ? 0 : key);
		ClickGui.INSTANCE.setListeningKeybind(null);
	}

	@Override
	public String getTooltip() {
		return "Taste zum Ein- und Ausschalten von " + module.name + ".";
	}
}
