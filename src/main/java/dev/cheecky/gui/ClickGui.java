package dev.cheecky.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.module.Modules;
import dev.cheecky.setting.CheckboxSetting;
import dev.cheecky.setting.Setting;
import dev.cheecky.setting.SliderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

/**
 * Hält alle Fenster des ClickGUI: eins pro Kategorie plus die geöffneten Einstellungsfenster.
 * Lebt über das Schließen des Screens hinaus, damit Positionen erhalten bleiben.
 */
public final class ClickGui {
	public static final ClickGui INSTANCE = new ClickGui();

	private static final int TOOLTIP_WIDTH = 200;

	private final List<Window> categoryWindows = new ArrayList<>();
	/** Alle Fenster in Zeichenreihenfolge, das letzte liegt oben. */
	private final List<Window> windows = new ArrayList<>();
	private final Map<Module, Window> settingsWindows = new HashMap<>();
	private KeybindComponent listeningKeybind;

	private ClickGui() {
		// Feste Abstände statt gemessener Breite: beim Laden der Mod gibt es noch keine Schrift
		int x = 5;
		for (Category category : Category.values()) {
			Window window = new Window(category.displayName, false);
			for (Module module : Modules.inCategory(category))
				window.add(new ModuleButton(module));
			window.x = x;
			window.y = 5;
			x += 100;
			categoryWindows.add(window);
			windows.add(window);
		}
	}

	public List<Window> getCategoryWindows() {
		return categoryWindows;
	}

	public boolean isSettingsOpen(Module module) {
		return settingsWindows.containsKey(module);
	}

	public void toggleSettings(Module module, ModuleButton button) {
		Window open = settingsWindows.remove(module);
		if (open != null) {
			windows.remove(open);
			return;
		}

		Window window = new Window(module.name, true);
		for (Setting setting : module.getSettings()) {
			if (setting instanceof SliderSetting slider)
				window.add(new SliderComponent(slider));
			else if (setting instanceof CheckboxSetting checkbox)
				window.add(new CheckboxComponent(checkbox));
		}
		window.add(new KeybindComponent(module));
		window.x = button.x + button.width + 5;
		window.y = button.y;
		settingsWindows.put(module, window);
		windows.add(window);
	}

	public KeybindComponent getListeningKeybind() {
		return listeningKeybind;
	}

	public void setListeningKeybind(KeybindComponent component) {
		listeningKeybind = component;
	}

	/** @return true, wenn die Taste verbraucht wurde */
	public boolean keyPressed(int key) {
		if (listeningKeybind == null)
			return false;
		listeningKeybind.onKey(key);
		return true;
	}

	public void render(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		for (Window window : windows) {
			window.layout();
			// Jedes Fenster auf eine eigene Ebene, damit überlappende Fenster richtig liegen
			g.nextStratum();
			window.render(g, mouseX, mouseY);
		}
		renderTooltip(g, mouseX, mouseY);
	}

	private void renderTooltip(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		Window top = getWindowAt(mouseX, mouseY);
		GuiComponent hovered = top == null ? null : top.getHoveredComponent(mouseX, mouseY);
		String tooltip = hovered == null ? null : hovered.getTooltip();
		if (tooltip == null)
			return;

		Font font = Minecraft.getInstance().font;
		List<FormattedCharSequence> lines = font.split(FormattedText.of(tooltip), TOOLTIP_WIDTH);
		int width = 0;
		for (FormattedCharSequence line : lines)
			width = Math.max(width, font.width(line));
		int height = lines.size() * font.lineHeight;

		int x1 = mouseX + 8;
		int y1 = mouseY + 8;
		// Am rechten oder unteren Rand auf die andere Seite der Maus klappen
		if (x1 + width + 4 > g.guiWidth())
			x1 = mouseX - width - 8;
		if (y1 + height + 4 > g.guiHeight())
			y1 = mouseY - height - 8;

		g.nextStratum();
		g.fill(x1, y1, x1 + width + 4, y1 + height + 3, Theme.withAlpha(Theme.BACKGROUND, 0.9F));
		g.outline(x1, y1, width + 4, height + 3, Theme.outline());
		for (int i = 0; i < lines.size(); i++)
			g.text(font, lines.get(i), x1 + 2, y1 + 2 + i * font.lineHeight, Theme.TEXT, false);
	}

	private Window getWindowAt(double mouseX, double mouseY) {
		for (int i = windows.size() - 1; i >= 0; i--)
			if (windows.get(i).isHovering(mouseX, mouseY))
				return windows.get(i);
		return null;
	}

	public void mouseClicked(double mouseX, double mouseY, int button) {
		Window window = getWindowAt(mouseX, mouseY);
		if (window == null)
			return;

		// Angeklicktes Fenster nach vorne holen
		windows.remove(window);
		windows.add(window);

		boolean close = window.mouseClicked(mouseX, mouseY, button);
		if (close) {
			windows.remove(window);
			settingsWindows.values().remove(window);
		}
	}

	public void mouseDragged(double mouseX, double mouseY) {
		for (Window window : windows)
			window.mouseDragged(mouseX, mouseY);
	}

	public void mouseReleased() {
		for (Window window : windows)
			window.mouseReleased();
	}
}
