package dev.cheecky.util;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

/** Blockgruppen, nach denen AutoMine und BlockFinder suchen. */
public enum BlockTarget {
	WOOD("Holz", path -> (path.endsWith("_log") || path.endsWith("_stem")) && !path.startsWith("stripped_")
		&& !path.contains("mushroom")),
	DIAMOND("Diamant", path -> path.endsWith("diamond_ore")),
	IRON("Eisen", path -> path.endsWith("iron_ore")),
	GOLD("Gold", path -> path.endsWith("gold_ore")),
	COAL("Kohle", path -> path.endsWith("coal_ore")),
	COPPER("Kupfer", path -> path.endsWith("copper_ore")),
	REDSTONE("Redstone", path -> path.endsWith("redstone_ore")),
	LAPIS("Lapis", path -> path.endsWith("lapis_ore")),
	EMERALD("Smaragd", path -> path.endsWith("emerald_ore")),
	QUARTZ("Quarz", path -> path.equals("nether_quartz_ore")),
	ANCIENT_DEBRIS("Antiker Schrott", path -> path.equals("ancient_debris")),
	/** Die Blöcke, die man mit ".finder add" selbst eingetragen hat. */
	CUSTOM("Eigene Liste", null);

	private final String displayName;
	private final Predicate<String> matches;

	BlockTarget(String displayName, Predicate<String> matches) {
		this.displayName = displayName;
		this.matches = matches;
	}

	public Set<Block> blocks() {
		if (this == CUSTOM)
			return CustomBlocks.get();
		return BuiltInRegistries.BLOCK.stream()
			.filter(block -> matches.test(BuiltInRegistries.BLOCK.getKey(block).getPath()))
			.collect(Collectors.toSet());
	}

	@Override
	public String toString() {
		return displayName;
	}
}
