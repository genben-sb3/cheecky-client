package dev.cheecky.gui;

import dev.cheecky.setting.CheckboxSetting;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class CheckboxComponent extends GuiComponent {
	private final CheckboxSetting setting;

	public CheckboxComponent(CheckboxSetting setting) {
		this.setting = setting;
	}

	@Override
	public int getHeight() {
		return 11;
	}

	@Override
	public int getPreferredWidth() {
		return font().width(setting.name) + 15;
	}

	@Override
	public void render(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		boolean hovering = isHovering(mouseX, mouseY);
		int boxX2 = x + 11;
		int y2 = y + 11;

		g.fill(x, y, boxX2, y2, Theme.background(hovering));
		drawOutline(g, x, y, boxX2, y2);
		if (setting.isChecked())
			g.fill(x + 3, y + 3, boxX2 - 3, y2 - 3, Theme.withAlpha(Theme.ENABLED, 1));

		g.text(font(), setting.name, boxX2 + 3, y + 2, Theme.TEXT, false);
	}

	@Override
	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0)
			setting.setChecked(!setting.isChecked());
	}

	@Override
	public String getTooltip() {
		return setting.description;
	}
}
