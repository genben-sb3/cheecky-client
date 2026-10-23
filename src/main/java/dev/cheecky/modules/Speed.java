package dev.cheecky.modules;

import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.ModeSetting;
import dev.cheecky.setting.SliderSetting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class Speed extends Module {
	public enum Mode {
		STRAFE("Strafe"),
		VANILLA("Vanilla");

		private final String displayName;

		Mode(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String toString() {
			return displayName;
		}
	}

	private final ModeSetting<Mode> mode = add(new ModeSetting<>("Modus",
		"Strafe: hält dein Tempo in der Luft und lenkt es in Laufrichtung (stark mit BunnyHop). Vanilla: feste Geschwindigkeit.",
		Mode.STRAFE));
	private final SliderSetting speed = add(new SliderSetting("Tempo",
		"Geschwindigkeit in Blöcken pro Tick (Sprinten in Vanilla: etwa 0,28).", 0.4, 0.2, 1.5, 0.05, ""));

	public Speed() {
		super("Speed", "Läuft schneller. Fällt auf Servern mit Anti-Cheat sehr wahrscheinlich auf.", Category.MOVEMENT, 0);
	}

	@Override
	public void onTick() {
		LocalPlayer player = mc.player;
		if (player.isShiftKeyDown() || player.isInWater() || player.isInLava() || player.isFallFlying()
			|| player.getAbilities().flying || player.isPassenger())
			return;

		Vec3 direction = inputDirection(player);
		if (direction == null)
			return;

		Vec3 motion = player.getDeltaMovement();
		double current = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
		double target = switch (mode.getSelected()) {
			case STRAFE -> player.onGround() ? Math.max(current, speed.getValue()) : current;
			case VANILLA -> speed.getValue();
		};
		player.setDeltaMovement(direction.x * target, motion.y, direction.z * target);
	}

	/** Laufrichtung aus WASD in Weltkoordinaten, null wenn keine Taste gedrückt ist. */
	static Vec3 inputDirection(LocalPlayer player) {
		Vec2 input = player.input.getMoveVector();
		if (input.lengthSquared() < 1e-4)
			return null;

		float yaw = player.getYRot() * Mth.DEG_TO_RAD;
		double sin = Mth.sin(yaw);
		double cos = Mth.cos(yaw);
		double x = input.x * cos - input.y * sin;
		double z = input.x * sin + input.y * cos;
		return new Vec3(x, 0, z).normalize();
	}
}
