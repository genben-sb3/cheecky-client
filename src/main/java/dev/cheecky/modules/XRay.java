package dev.cheecky.modules;

import java.util.Set;
import java.util.stream.Collectors;

import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.CheckboxSetting;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class XRay extends Module {
	private static final Set<String> EXTRA_BLOCKS = Set.of("ancient_debris", "spawner", "trial_spawner", "vault");
	private static final Set<String> FLUIDS = Set.of("lava", "water");

	private final CheckboxSetting showFluids = add(new CheckboxSetting("Flüssigkeiten",
		"Zeigt Lava und Wasser zusätzlich zu den Erzen an.", true));

	// Wird von den Chunk-Render-Threads gelesen, deshalb unveränderlich und volatile.
	private volatile Set<Block> visibleBlocks = Set.of();

	public XRay() {
		super("X-Ray", "Zeigt nur noch Erze und Spawner an, alles andere wird unsichtbar.",
			Category.RENDER, InputConstants.KEY_X);
	}

	@Override
	protected void onEnable() {
		boolean fluids = showFluids.isChecked();
		visibleBlocks = BuiltInRegistries.BLOCK.stream().filter(block -> {
			String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
			return path.endsWith("_ore") || EXTRA_BLOCKS.contains(path) || fluids && FLUIDS.contains(path);
		}).collect(Collectors.toUnmodifiableSet());
		reloadChunks();
	}

	@Override
	protected void onDisable() {
		reloadChunks();
	}

	private static void reloadChunks() {
		// Beim Laden der Config gibt es noch keine Welt
		if (mc.level != null)
			mc.levelExtractor.allChanged();
	}

	public boolean isVisible(Block block) {
		return visibleBlocks.contains(block);
	}

	/**
	 * Für die Render-Mixins: null = Vanilla entscheiden lassen, sonst ob die Fläche gezeichnet wird.
	 */
	public Boolean shouldDrawFace(BlockState state) {
		if (!isEnabled())
			return null;
		return isVisible(state.getBlock());
	}
}
