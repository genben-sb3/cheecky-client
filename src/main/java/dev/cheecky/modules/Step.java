package dev.cheecky.modules;

import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.SliderSetting;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class Step extends Module {
	private static final double VANILLA_STEP = 0.6;

	private final SliderSetting height = add(new SliderSetting("Höhe",
		"Wie hoch du ohne Springen hochlaufen kannst.", 1, 0.6, 2.5, 0.1, " Blöcke"));

	public Step() {
		super("Step", "Läuft Blöcke hoch wie eine Treppenstufe, ohne zu springen. Fällt auf Servern mit Anti-Cheat auf.",
			Category.MOVEMENT, 0);
	}

	@Override
	public void onTick() {
		setStepHeight(height.getValue());
	}

	@Override
	protected void onDisable() {
		setStepHeight(VANILLA_STEP);
	}

	private static void setStepHeight(double value) {
		if (mc.player == null)
			return;
		AttributeInstance attribute = mc.player.getAttribute(Attributes.STEP_HEIGHT);
		if (attribute != null)
			attribute.setBaseValue(value);
	}
}
