package dev.cheecky.modules;

import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import net.minecraft.client.player.LocalPlayer;

public class AutoSprint extends Module {
	public AutoSprint() {
		super("AutoSprint", "Sprintet automatisch, sobald du vorwärts läufst.", Category.MOVEMENT, 0);
	}

	@Override
	public void onTick() {
		LocalPlayer player = mc.player;
		if (player.zza <= 0 || player.isShiftKeyDown() || player.horizontalCollision || player.isUsingItem())
			return;
		if (!player.getFoodData().hasEnoughFood() && !player.getAbilities().mayfly)
			return;

		player.setSprinting(true);
	}
}
