package dev.cheecky.gui;

import dev.cheecky.Cheecky;
import dev.cheecky.module.Module;
import dev.cheecky.module.Modules;
import dev.cheecky.modules.Autopilot;
import dev.cheecky.util.Coords;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.SharedConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.fabricmc.loader.api.FabricLoader;

/** Logo-Leiste oben links, darunter die Liste der aktiven Module (wie bei Wurst), unten rechts die Koordinaten. */
public class Hud implements HudElement {
	private static final int LOGO_BACKGROUND = 0x80FFFFFF;
	private static final int LOGO_COLOR = 0xFF00A000;
	private static final int VERSION_COLOR = 0xFF000000;
	private static final int LIST_COLOR = 0xFFFFFFFF;
	private static final int WARNING_COLOR = 0xFFFF5555;

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

		String warning = Modules.AUTO_TOOL.getDropWarning();
		if (warning != null)
			g.text(font, warning, (g.guiWidth() - font.width(warning)) / 2, g.guiHeight() / 2 + 12, WARNING_COLOR, true);

		if (Modules.COORDINATES.isEnabled())
			drawCoordinates(g, mc, font);

		int y = 22;
		for (Module module : Modules.ALL) {
			if (!module.isEnabled())
				continue;
			g.text(font, module.name, 2, y, LIST_COLOR, true);
			y += font.lineHeight;
		}
	}

	/** Unten rechts: eigene Position, umgerechnete Position der anderen Dimension, Blickrichtung. */
	private static void drawCoordinates(GuiGraphicsExtractor g, Minecraft mc, Font font) {
		if (mc.player == null)
			return;
		BlockPos pos = mc.player.blockPosition();
		boolean nether = Coords.inNether();
		String other = nether
			? "Oberwelt: " + pos.getX() * Coords.NETHER_SCALE + " " + pos.getZ() * Coords.NETHER_SCALE
			: "Nether: " + Math.floorDiv(pos.getX(), Coords.NETHER_SCALE) + " " + Math.floorDiv(pos.getZ(), Coords.NETHER_SCALE);
		String[] lines = {
			"XYZ: " + pos.getX() + " " + pos.getY() + " " + pos.getZ(),
			other,
			Autopilot.directionName(mc.player.getYRot())};

		int y = g.guiHeight() - 2 - lines.length * font.lineHeight;
		for (String line : lines) {
			g.text(font, line, g.guiWidth() - 2 - font.width(line), y, LIST_COLOR, true);
			y += font.lineHeight;
		}
	}
}
