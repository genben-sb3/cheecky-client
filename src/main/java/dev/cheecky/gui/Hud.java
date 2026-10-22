package dev.cheecky.gui;

import dev.cheecky.Cheecky;
import dev.cheecky.module.Module;
import dev.cheecky.module.Modules;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.SharedConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.fabricmc.loader.api.FabricLoader;

/** Logo-Leiste oben links und darunter die Liste der aktiven Module, wie bei Wurst. */
public class Hud implements HudElement {
	private static final int LOGO_BACKGROUND = 0x80FFFFFF;
	private static final int LOGO_COLOR = 0xFF00A000;
	private static final int VERSION_COLOR = 0xFF000000;
	private static final int LIST_COLOR = 0xFFFFFFFF;

	private final String version = "v" + FabricLoader.getInstance().getModContainer(Cheecky.MOD_ID)
		.map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("?")
		+ " MC" + SharedConstants.getCurrentVersion().name();

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		// F1 blendet HUD-Elemente schon selbst aus, nur bei F3 machen wir Platz
		if (mc.debugEntries.isOverlayVisible())
			return;

		Font font = mc.font;
		String logo = "§l" + Cheecky.NAME;
		int logoWidth = font.width(logo);

		g.fill(0, 6, logoWidth + font.width(version) + 10, 17, LOGO_BACKGROUND);
		g.text(font, logo, 3, 8, LOGO_COLOR, false);
		g.text(font, version, logoWidth + 7, 8, VERSION_COLOR, false);

		int y = 22;
		for (Module module : Modules.ALL) {
			if (!module.isEnabled())
				continue;
			g.text(font, module.name, 2, y, LIST_COLOR, true);
			y += font.lineHeight;
		}
	}
}
