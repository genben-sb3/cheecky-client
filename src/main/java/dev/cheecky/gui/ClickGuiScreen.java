package dev.cheecky.gui;

import dev.cheecky.Cheecky;
import dev.cheecky.config.Config;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ClickGuiScreen extends Screen {
	private final ClickGui gui;

	public ClickGuiScreen(ClickGui gui) {
		super(Component.literal("ClickGUI"));
		this.gui = gui;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
		gui.render(g, mouseX, mouseY);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
		// Kein Weichzeichner, man soll das Spiel dahinter sehen
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		gui.mouseClicked(event.x(), event.y(), event.button());
		return true;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		gui.mouseDragged(event.x(), event.y());
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		gui.mouseReleased();
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (gui.keyPressed(event.key()))
			return true;
		if (Cheecky.clickGuiKey.matches(event)) {
			onClose();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void removed() {
		gui.setListeningKeybind(null);
		gui.mouseReleased();
		Config.save();
	}
}
