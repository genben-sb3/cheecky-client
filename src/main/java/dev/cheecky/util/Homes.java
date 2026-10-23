package dev.cheecky.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Merkt sich die Koordinaten der eigenen /home-Punkte des Servers. Cheecky setzt keine Homes,
 * es weiß nur, wo sie liegen, um den nächsten zu einem Ziel zu finden.
 */
public final class Homes {
	/** Position in den Koordinaten der Dimension, in der der Home liegt. */
	public record Home(int x, int y, int z, boolean nether) {
		public int overworldX() {
			return nether ? x * Coords.NETHER_SCALE : x;
		}

		public int overworldZ() {
			return nether ? z * Coords.NETHER_SCALE : z;
		}

		public String describe() {
			return x + " " + y + " " + z + (nether ? " (Nether)" : "");
		}
	}

	private static final Map<String, Home> HOMES = new LinkedHashMap<>();

	private Homes() {}

	public static Map<String, Home> all() {
		return Collections.unmodifiableMap(HOMES);
	}

	public static void put(String name, Home home) {
		HOMES.put(name, home);
	}

	public static boolean remove(String name) {
		return HOMES.remove(name) != null;
	}
}
