package dev.cheecky.util;

import java.lang.reflect.Method;

import dev.cheecky.Cheecky;
import net.minecraft.world.level.block.Block;

/**
 * Spricht Baritone zur Laufzeit per Reflection an, falls es als eigener Mod installiert ist.
 * Cheecky bringt Baritone nicht mit und braucht es nicht zum Bauen.
 */
public final class BaritoneBridge {
	private static Boolean available;

	private BaritoneBridge() {}

	public static boolean isAvailable() {
		if (available == null) {
			try {
				Class.forName("baritone.api.BaritoneAPI");
				available = true;
			} catch (ClassNotFoundException e) {
				available = false;
			}
		}
		return available;
	}

	/** Entspricht {@code BaritoneAPI.getProvider().getPrimaryBaritone()}. */
	private static Object primaryBaritone() throws ReflectiveOperationException {
		Object provider = Class.forName("baritone.api.BaritoneAPI").getMethod("getProvider").invoke(null);
		return call("baritone.api.IBaritoneProvider", provider, "getPrimaryBaritone");
	}

	/** Baritone baut diese Blöcke ab und läuft dafür selbst los. */
	public static boolean mine(Block... blocks) {
		try {
			Object baritone = primaryBaritone();
			Object mineProcess = call("baritone.api.IBaritone", baritone, "getMineProcess");
			Method mine = Class.forName("baritone.api.process.IMineProcess").getMethod("mine", Block[].class);
			mine.invoke(mineProcess, (Object) blocks);
			return true;
		} catch (ReflectiveOperationException | RuntimeException e) {
			Cheecky.LOGGER.error("Baritone konnte nicht gestartet werden", e);
			return false;
		}
	}

	/** Bricht alles ab, was Baritone gerade tut. */
	public static void stop() {
		if (!isAvailable())
			return;
		try {
			Object pathing = call("baritone.api.IBaritone", primaryBaritone(), "getPathingBehavior");
			call("baritone.api.behavior.IPathingBehavior", pathing, "cancelEverything");
		} catch (ReflectiveOperationException | RuntimeException e) {
			Cheecky.LOGGER.error("Baritone konnte nicht gestoppt werden", e);
		}
	}

	/**
	 * Methode über das öffentliche API-Interface aufrufen. Die Implementierungsklassen sind in
	 * Baritone nicht öffentlich, ein Aufruf über sie würde an den Zugriffsrechten scheitern.
	 */
	private static Object call(String apiInterface, Object target, String method) throws ReflectiveOperationException {
		return Class.forName(apiInterface).getMethod(method).invoke(target);
	}
}
