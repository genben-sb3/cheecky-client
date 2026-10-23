package dev.cheecky.util;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

/** Die eigene Blockliste für BlockFinder und AutoMine ("Ziel: Eigene Liste"). */
public final class CustomBlocks {
	private static final Set<Block> BLOCKS = new LinkedHashSet<>();

	private CustomBlocks() {}

	public static Set<Block> get() {
		return Collections.unmodifiableSet(BLOCKS);
	}

	public static boolean add(Block block) {
		return BLOCKS.add(block);
	}

	public static boolean remove(Block block) {
		return BLOCKS.remove(block);
	}

	/** "diamond_ore" oder "minecraft:diamond_ore"; leer, wenn es den Block nicht gibt. */
	public static Optional<Block> parse(String name) {
		Identifier id = Identifier.tryParse(name.toLowerCase(Locale.ROOT));
		if (id == null)
			return Optional.empty();
		return BuiltInRegistries.BLOCK.getOptional(id);
	}

	public static String name(Block block) {
		return BuiltInRegistries.BLOCK.getKey(block).getPath();
	}
}
