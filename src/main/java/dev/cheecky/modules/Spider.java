package dev.cheecky.modules;

import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.SliderSetting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class Spider extends Module {
	private final SliderSetting climbSpeed = add(new SliderSetting("Tempo",
		"Wie schnell du die Wand hochkletterst.", 0.2, 0.1, 0.5, 0.05, ""));

	public Spider() {
		super("Spider", "Klettert Wände hoch, wenn du gegen sie läufst. Fällt auf Servern mit Anti-Cheat auf.",
			Category.MOVEMENT, 0);
	}

	@Override
	public void onTick() {
		LocalPlayer player = mc.player;
		if (!player.horizontalCollision || player.input.getMoveVector().lengthSquared() < 1e-4)
			return;

		Vec3 motion = player.getDeltaMovement();
		player.setDeltaMovement(motion.x, climbSpeed.getValue(), motion.z);
	}
}
