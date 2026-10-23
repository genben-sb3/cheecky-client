package dev.cheecky.gui;

import dev.cheecky.setting.ModeSetting;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Auswahlfeld: Linksklick nächster Wert, Rechtsklick voriger Wert. */
public class ModeComponent extends GuiComponent {
	private final ModeSetting<?> setting;

	public ModeComponent(ModeSetting<?> setting) {
		this.setting = setting;
	}

	@Override
	public int getHeight() {
		return 11;
	}

	@Override
	public int getPreferredWidth() {
		int widest = 0;
		for (Enum<?> value : setting.getValues())
			widest = Math.max(widest, font().width(value.toString()));
		return font().width(setting.name + ": ") + widest + 16;
	}

	@Override
	public void render(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		g.fill(x, y, x + width, y + getHeight(), Theme.background(isHovering(mouseX, mouseY)));
		drawOutline(g, x, y, x + width, y + getHeight());

		String value = setting.getSelected().toString();
		g.text(font(), setting.name + ":", x + 2, y + 2, Theme.TEXT, false);
		g.text(font(), "< " + value + " >", x + width - font().width("< " + value + " >") - 2, y + 2, Theme.TEXT, false);
	}

	@Override
	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0)
			setting.cycle(1);
		else if (button == 1)
			setting.cycle(-1);
	}

	@Override
	public String getTooltip() {
		return setting.description;
	}
}
