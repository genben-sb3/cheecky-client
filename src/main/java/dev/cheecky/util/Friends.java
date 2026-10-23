package dev.cheecky.util;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** Spieler, die Kampf-Module nie angreifen, z. B. der eigene Clan. */
public final class Friends {
	private static final Set<String> NAMES = new TreeSet<>();

	private Friends() {}

	public static boolean isFriend(Entity entity) {
		return entity instanceof Player player && NAMES.contains(key(player.getName().getString()));
	}

	public static boolean add(String name) {
		return NAMES.add(key(name));
	}

	public static boolean remove(String name) {
		return NAMES.remove(key(name));
	}

	public static Set<String> all() {
		return Collections.unmodifiableSet(NAMES);
	}

	/** Minecraft-Namen sind nicht groß-/kleinschreibungsabhängig. */
	private static String key(String name) {
		return name.toLowerCase(Locale.ROOT);
	}
}
