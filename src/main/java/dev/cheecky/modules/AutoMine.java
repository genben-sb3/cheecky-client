package dev.cheecky.modules;

import dev.cheecky.command.ChatOutput;
import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.ModeSetting;
import dev.cheecky.util.BaritoneBridge;
import dev.cheecky.util.BlockTarget;
import net.minecraft.world.level.block.Block;

/** Steuert Baritone: Einschalten startet das Abbauen, Ausschalten stoppt es. */
public class AutoMine extends Module {
	private final ModeSetting<BlockTarget> target = add(new ModeSetting<>("Ziel",
		"Was Baritone abbauen soll. Holz umfasst alle Stämme, auch Karmesin- und Wirrstiele. Eigene Liste: siehe .finder add.", BlockTarget.WOOD));

	private BlockTarget running;

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
		BlockTarget selected = target.getSelected();
		if (selected == running)
			return;

		running = selected;
		Block[] blocks = selected.blocks().toArray(Block[]::new);
		if (blocks.length == 0) {
			ChatOutput.error("Die eigene Liste ist leer. Blöcke eintragen mit .finder add <block>.");
			setEnabled(false);
			return;
		}
		if (BaritoneBridge.mine(blocks))
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
