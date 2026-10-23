package dev.cheecky.util;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

/** Umrechnung zwischen Oberwelt- und Nether-Koordinaten (1 Block im Nether = 8 in der Oberwelt). */
public final class Coords {
	public static final int NETHER_SCALE = 8;

	private Coords() {}

	public static boolean inNether() {
		Minecraft mc = Minecraft.getInstance();
		return mc.level != null && mc.level.dimension() == Level.NETHER;
	}

	/** Wert aus Oberwelt- (overworld = true) oder Nether-Koordinaten in die aktuelle Dimension umrechnen. */
	public static int toCurrent(int value, boolean overworld) {
		if (inNether() && overworld)
			return Math.floorDiv(value, NETHER_SCALE);
		if (!inNether() && !overworld)
			return value * NETHER_SCALE;
		return value;
	}

	/** Wert aus der aktuellen Dimension in Oberwelt- oder Nether-Koordinaten umrechnen. */
	public static int fromCurrent(int value, boolean overworld) {
		if (inNether() && overworld)
			return value * NETHER_SCALE;
		if (!inNether() && !overworld)
			return Math.floorDiv(value, NETHER_SCALE);
		return value;
	}

	/** Spielt der Spieler gerade auf 6b6t? */
	public static boolean on6b6t() {
		var server = Minecraft.getInstance().getCurrentServer();
		return server != null && server.ip.toLowerCase(Locale.ROOT).contains("6b6t");
	}
}
