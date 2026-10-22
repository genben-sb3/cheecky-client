package dev.cheecky.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Ein Element in einem ClickGUI-Fenster. Position und Breite setzt das Fenster beim Layout. */
public abstract class GuiComponent {
	// Nicht als Konstante: beim Laden der Mod existiert die Schrift noch nicht
	protected static Font font() {
		return Minecraft.getInstance().font;
	}

	public int x;
	public int y;
	public int width;

	public abstract int getHeight();

	public abstract int getPreferredWidth();

	public abstract void render(GuiGraphicsExtractor g, int mouseX, int mouseY);

	public void mouseClicked(double mouseX, double mouseY, int button) {}

	public void mouseDragged(double mouseX, double mouseY) {}

	public void mouseReleased() {}

	/** Text für den Tooltip, null = keiner. */
	public String getTooltip() {
		return null;
	}

	public boolean isHovering(double mouseX, double mouseY) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + getHeight();
	}

	protected static void drawOutline(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2) {
		g.outline(x1, y1, x2 - x1, y2 - y1, Theme.outline());
	}
}
