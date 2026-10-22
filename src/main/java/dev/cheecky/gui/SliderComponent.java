package dev.cheecky.gui;

import dev.cheecky.setting.SliderSetting;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Oben Name und Wert, darunter die Schiene mit Regler – wie in Wurst. */
public class SliderComponent extends GuiComponent {
	private static final int KNOB_WIDTH = 8;

	private final SliderSetting setting;
	private boolean dragging;

	public SliderComponent(SliderSetting setting) {
		this.setting = setting;
	}

	@Override
	public int getHeight() {
		return 22;
	}

	@Override
	public int getPreferredWidth() {
		return font().width(setting.name) + font().width(setting.getValueString()) + 10;
	}

	@Override
	public void render(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		int x2 = x + width;
		int trackY1 = y + 13;
		int trackY2 = y + getHeight() - 2;
		boolean hoveringTrack = isHovering(mouseX, mouseY) && mouseY >= y + 11;

		g.fill(x, trackY1, x2, trackY2, Theme.background(hoveringTrack || dragging));
		drawOutline(g, x, trackY1, x2, trackY2);

		int knobX = x + (int) Math.round(setting.getPercent() * (width - KNOB_WIDTH));
		g.fill(knobX, trackY1 - 2, knobX + KNOB_WIDTH, trackY2 + 2,
			Theme.withAlpha(Theme.ENABLED, dragging || hoveringTrack ? 1 : 0.75F));
		drawOutline(g, knobX, trackY1 - 2, knobX + KNOB_WIDTH, trackY2 + 2);

		String value = setting.getValueString();
		g.text(font(), setting.name, x, y + 1, Theme.TEXT, false);
		g.text(font(), value, x2 - font().width(value), y + 1, Theme.TEXT, false);
	}

	@Override
	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0)
			return;
		dragging = true;
		mouseDragged(mouseX, mouseY);
	}

	@Override
	public void mouseDragged(double mouseX, double mouseY) {
		if (dragging)
			setting.setPercent((mouseX - x - KNOB_WIDTH / 2.0) / (width - KNOB_WIDTH));
	}

	@Override
	public void mouseReleased() {
		dragging = false;
	}

	@Override
	public String getTooltip() {
		return setting.description;
	}
}
