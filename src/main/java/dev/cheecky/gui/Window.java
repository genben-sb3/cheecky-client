package dev.cheecky.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Ein verschiebbares Fenster mit Titelleiste, Minimieren- und optional Schließen-Knopf. */
public class Window {
	// Nicht als Konstante: beim Laden der Mod existiert die Schrift noch nicht
	private static Font font() {
		return Minecraft.getInstance().font;
	}
	private static final int TITLE_HEIGHT = 13;
	private static final int BUTTON_SIZE = 11;
	private static final int PADDING = 2;

	public final String title;
	public final boolean closable;
	public int x;
	public int y;
	public boolean minimized;
	private final List<GuiComponent> components = new ArrayList<>();
	private int width;

	private boolean dragging;
	private double dragOffsetX;
	private double dragOffsetY;
	private GuiComponent pressed;

	public Window(String title, boolean closable) {
		this.title = title;
		this.closable = closable;
	}

	public void add(GuiComponent component) {
		components.add(component);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		if (minimized)
			return TITLE_HEIGHT;
		int height = TITLE_HEIGHT + PADDING;
		for (GuiComponent c : components)
			height += c.getHeight() + PADDING;
		return height;
	}

	/** Breite und Positionen der Elemente neu berechnen. Wird vor jedem Frame aufgerufen. */
	public void layout() {
		int buttons = closable ? 2 : 1;
		width = font().width(title) + buttons * (BUTTON_SIZE + 1) + 6;
		for (GuiComponent c : components)
			width = Math.max(width, c.getPreferredWidth() + 2 * PADDING);

		int cy = y + TITLE_HEIGHT + PADDING;
		for (GuiComponent c : components) {
			c.x = x + PADDING;
			c.y = cy;
			c.width = width - 2 * PADDING;
			cy += c.getHeight() + PADDING;
		}
	}

	public boolean isHovering(double mouseX, double mouseY) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + getHeight();
	}

	private int minimizeButtonX() {
		return x + width - BUTTON_SIZE - 1;
	}

	private int closeButtonX() {
		return minimizeButtonX() - BUTTON_SIZE - 1;
	}

	private static boolean inButton(double mouseX, double mouseY, int bx, int by) {
		return mouseX >= bx && mouseX < bx + BUTTON_SIZE && mouseY >= by && mouseY < by + BUTTON_SIZE;
	}

	public void render(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		int x2 = x + width;
		int y2 = y + getHeight();
		int titleY2 = y + TITLE_HEIGHT;

		// Fensterinhalt
		if (!minimized) {
			g.fill(x, titleY2, x2, y2, Theme.withAlpha(Theme.BACKGROUND, Theme.OPACITY));
			for (GuiComponent c : components)
				c.render(g, mouseX, mouseY);
		}

		// Titelleiste
		g.fill(x, y, x2, titleY2, Theme.withAlpha(Theme.ACCENT, Theme.OPACITY));
		g.text(font(), title, x + 3, y + 3, Theme.TEXT, false);
		renderButton(g, minimizeButtonX(), mouseX, mouseY, minimized ? "+" : "-");
		if (closable)
			renderButton(g, closeButtonX(), mouseX, mouseY, "x");

		g.outline(x, y, width, y2 - y, Theme.outline());
		g.horizontalLine(x, x2 - 1, titleY2 - 1, Theme.outline());
	}

	private void renderButton(GuiGraphicsExtractor g, int bx, int mouseX, int mouseY, String symbol) {
		int by = y + 1;
		g.fill(bx, by, bx + BUTTON_SIZE, by + BUTTON_SIZE, Theme.background(inButton(mouseX, mouseY, bx, by)));
		g.outline(bx, by, BUTTON_SIZE, BUTTON_SIZE, Theme.outline());
		g.text(font(), symbol, bx + (BUTTON_SIZE - font().width(symbol)) / 2 + 1, by + 2, Theme.TEXT, false);
	}

	/** @return true, wenn das Fenster geschlossen werden soll */
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (mouseY < y + TITLE_HEIGHT) {
			if (button != 0)
				return false;
			if (inButton(mouseX, mouseY, minimizeButtonX(), y + 1))
				minimized = !minimized;
			else if (closable && inButton(mouseX, mouseY, closeButtonX(), y + 1))
				return true;
			else {
				dragging = true;
				dragOffsetX = mouseX - x;
				dragOffsetY = mouseY - y;
			}
			return false;
		}

		if (minimized)
			return false;
		for (GuiComponent c : components)
			if (c.isHovering(mouseX, mouseY)) {
				pressed = c;
				c.mouseClicked(mouseX, mouseY, button);
				break;
			}
		return false;
	}

	public void mouseDragged(double mouseX, double mouseY) {
		if (dragging) {
			x = (int) Math.round(mouseX - dragOffsetX);
			y = (int) Math.round(mouseY - dragOffsetY);
		} else if (pressed != null)
			pressed.mouseDragged(mouseX, mouseY);
	}

	public void mouseReleased() {
		dragging = false;
		if (pressed != null)
			pressed.mouseReleased();
		pressed = null;
	}

	public GuiComponent getHoveredComponent(double mouseX, double mouseY) {
		if (minimized)
			return null;
		for (GuiComponent c : components)
			if (c.isHovering(mouseX, mouseY))
				return c;
		return null;
	}
}
