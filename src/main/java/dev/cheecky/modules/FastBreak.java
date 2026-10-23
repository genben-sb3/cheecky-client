package dev.cheecky.modules;

import dev.cheecky.mixin.MultiPlayerGameModeAccessor;
import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.CheckboxSetting;
import dev.cheecky.setting.SliderSetting;

public class FastBreak extends Module {
	private final CheckboxSetting noDelay = add(new CheckboxSetting("Kein Delay",
		"Streicht die Pause von 5 Ticks zwischen zwei abgebauten Blöcken. Bringt beim Abbauen vieler Blöcke am meisten.", true));
	private final SliderSetting multiplier = add(new SliderSetting("Tempo",
		"Baut jeden Block schneller ab, als dein Werkzeug erlaubt. Server mit Anti-Cheat setzen den Block dann oft zurück. 1,0 = aus.",
		1, 1, 2, 0.05, "x"));

	public FastBreak() {
		super("FastBreak", "Baut Blöcke schneller ab.", Category.WORLD, 0);
	}

	@Override
	public void onTick() {
		if (noDelay.isChecked() && mc.gameMode != null)
			((MultiPlayerGameModeAccessor) mc.gameMode).cheecky$setDestroyDelay(0);
	}

	/** Vom Mixin: Fortschritt pro Tick mit dem eingestellten Tempo multiplizieren. */
	public float modifyProgress(float progress) {
		return isEnabled() ? progress * multiplier.getValueF() : progress;
	}
}
