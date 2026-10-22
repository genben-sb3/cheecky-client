package dev.cheecky.gui;

import dev.cheecky.module.Module;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Wie Wursts FeatureButton: links an/aus, rechts ein Pfeil, der das Einstellungsfenster öffnet. */
public class ModuleButton extends GuiComponent {
	private static final int ARROW_WIDTH = 11;

	private final Module module;

	public ModuleButton(Module module) {
		this.module = module;
	}

	@Override
	public int getHeight() {
		return 11;
	}

	@Override
	public int getPreferredWidth() {
		return font().width(module.name) + ARROW_WIDTH + 6;
	}

	private int arrowX() {
		return x + width - ARROW_WIDTH;
	}

	@Override
	public void render(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		int x2 = x + width;
		int x3 = arrowX();
		int y2 = y + getHeight();
		boolean hovering = isHovering(mouseX, mouseY);
		boolean hoverModule = hovering && mouseX < x3;
		boolean hoverArrow = hovering && mouseX >= x3;

		int moduleColor = module.isEnabled()
			? Theme.withAlpha(Theme.ENABLED, Theme.OPACITY * (hoverModule ? 1.5F : 1))
			: Theme.background(hoverModule);
		g.fill(x, y, x3, y2, moduleColor);
		g.fill(x3, y, x2, y2, Theme.background(hoverArrow));
		drawOutline(g, x, y, x2, y2);
		g.verticalLine(x3, y, y2 - 1, Theme.outline());

		String arrow = ClickGui.INSTANCE.isSettingsOpen(module) ? "<" : ">";
		g.text(font(), arrow, x3 + (ARROW_WIDTH - font().width(arrow)) / 2 + 1, y + 2, Theme.TEXT, false);
		g.text(font(), module.name, x + (x3 - x - font().width(module.name)) / 2, y + 2, Theme.TEXT, false);
	}

	@Override
	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (mouseX >= arrowX() || button == 1)
			ClickGui.INSTANCE.toggleSettings(module, this);
		else if (button == 0)
			module.toggle();
	}

	@Override
	public String getTooltip() {
		return module.description;
	}
}
