package dev.cheecky.util;

import java.lang.reflect.Method;
import java.util.Optional;

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
	 * Führt einen Baritone-Befehl aus, wie "#goto 100 64 200" im Chat, nur ohne "#".
	 * Läuft lokal und geht nicht an den Server.
	 */
	public static boolean execute(String command) {
		if (!isAvailable())
			return false;
		try {
			Object commandManager = call("baritone.api.IBaritone", primaryBaritone(), "getCommandManager");
			return (boolean) Class.forName("baritone.api.command.manager.ICommandManager")
				.getMethod("execute", String.class).invoke(commandManager, command);
		} catch (ReflectiveOperationException | RuntimeException e) {
			Cheecky.LOGGER.error("Baritone-Befehl \"{}\" fehlgeschlagen", command, e);
			return false;
		}
	}

	/** Liest eine Ja/Nein-Einstellung aus Baritones Settings, z. B. "allowBreak". Leer, wenn es sie nicht gibt. */
	public static Optional<Boolean> getBooleanSetting(String name) {
		if (!isAvailable())
			return Optional.empty();
		try {
			Object settings = Class.forName("baritone.api.BaritoneAPI").getMethod("getSettings").invoke(null);
			Object setting = settings.getClass().getField(name).get(settings);
			Object value = setting.getClass().getField("value").get(setting);
			return value instanceof Boolean b ? Optional.of(b) : Optional.empty();
		} catch (ReflectiveOperationException | RuntimeException e) {
			return Optional.empty();
		}
	}

	/** Ändert eine Einstellung über Baritones "set"-Befehl, damit Baritone sie auch selbst speichert. */
	public static void setSetting(String name, Object value) {
		execute("set " + name + " " + value);
	}

	/** Kurzer Text, was Baritone gerade tut, für die Statuszeile. */
	public static String status() {
		if (!isAvailable())
			return "Baritone ist nicht installiert";
		try {
			Object pathing = call("baritone.api.IBaritone", primaryBaritone(), "getPathingBehavior");
			Class<?> pathingApi = Class.forName("baritone.api.behavior.IPathingBehavior");
			Object goal = pathingApi.getMethod("getGoal").invoke(pathing);
			if (goal == null)
				return "Baritone wartet";

			boolean isPathing = (boolean) pathingApi.getMethod("isPathing").invoke(pathing);
			String text = (isPathing ? "Läuft zu " : "Ziel: ") + goal;
			Optional<?> ticks = (Optional<?>) pathingApi.getMethod("estimatedTicksToGoal").invoke(pathing);
			if (isPathing && ticks.isPresent() && ticks.get() instanceof Double t)
				text += " (noch ca. " + Math.round(t / 20) + " s)";
			return text;
		} catch (ReflectiveOperationException | RuntimeException e) {
			return "Status nicht lesbar";
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
