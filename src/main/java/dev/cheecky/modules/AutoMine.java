package dev.cheecky.modules;

import java.util.function.Predicate;

import dev.cheecky.command.ChatOutput;
import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.ModeSetting;
import dev.cheecky.util.BaritoneBridge;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

/** Steuert Baritone: Einschalten startet das Abbauen, Ausschalten stoppt es. */
public class AutoMine extends Module {
	public enum Target {
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
		ANCIENT_DEBRIS("Antiker Schrott", path -> path.equals("ancient_debris"));

		private final String displayName;
		private final Predicate<String> matches;

		Target(String displayName, Predicate<String> matches) {
			this.displayName = displayName;
			this.matches = matches;
		}

		Block[] blocks() {
			return BuiltInRegistries.BLOCK.stream()
				.filter(block -> matches.test(BuiltInRegistries.BLOCK.getKey(block).getPath()))
				.toArray(Block[]::new);
		}

		@Override
		public String toString() {
			return displayName;
		}
	}

	private final ModeSetting<Target> target = add(new ModeSetting<>("Ziel",
		"Was Baritone abbauen soll. Holz umfasst alle Stämme, auch Karmesin- und Wirrstiele.", Target.WOOD));

	private Target running;

	public AutoMine() {
		super("AutoMine", "Lässt Baritone automatisch hinlaufen und abbauen. Braucht Baritone als eigenen Mod im mods-Ordner.",
			Category.WORLD, 0);
	}

	@Override
	public boolean isAvailable() {
		return BaritoneBridge.isAvailable();
	}

	@Override
	public String getUnavailableReason() {
		return "Baritone ist nicht installiert. Anleitung in der README.";
	}

	@Override
	public boolean rememberEnabled() {
		return false;
	}

	@Override
	protected void onEnable() {
		running = null;
	}

	@Override
	public void onTick() {
		// Startet beim Einschalten und neu, wenn im ClickGUI ein anderes Ziel gewählt wird
		Target selected = target.getSelected();
		if (selected == running)
			return;

		running = selected;
		if (BaritoneBridge.mine(selected.blocks()))
			ChatOutput.info("Baritone baut jetzt " + selected + " ab.");
		else {
			ChatOutput.error("Baritone konnte nicht gestartet werden, Details im Log.");
			setEnabled(false);
		}
	}

	@Override
	protected void onDisable() {
		if (running != null)
			BaritoneBridge.stop();
		running = null;
	}
}
