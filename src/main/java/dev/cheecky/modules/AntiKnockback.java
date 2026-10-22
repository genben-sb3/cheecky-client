package dev.cheecky.modules;

import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.SliderSetting;
import net.minecraft.world.phys.Vec3;

public class AntiKnockback extends Module {
	private final SliderSetting horizontal = add(new SliderSetting("Horizontal",
		"Wie viel seitlicher Rückstoß noch ankommt.", 0, 0, 100, 1, "%"));
	private final SliderSetting vertical = add(new SliderSetting("Vertikal",
		"Wie viel Rückstoß nach oben noch ankommt.", 0, 0, 100, 1, "%"));

	public AntiKnockback() {
		super("AntiKnockback", "Verhindert, dass du durch Treffer und Explosionen weggeschleudert wirst.",
			Category.COMBAT, 0);
	}

	/** Skaliert eine vom Server gesetzte Geschwindigkeit relativ zur aktuellen. */
	public Vec3 modifyVelocity(Vec3 current, Vec3 incoming) {
		double h = horizontal.getValue() / 100;
		double v = vertical.getValue() / 100;
		return new Vec3(
			current.x + (incoming.x - current.x) * h,
			current.y + (incoming.y - current.y) * v,
			current.z + (incoming.z - current.z) * h);
	}

	/** Skaliert einen Explosions-Stoß, der zur Geschwindigkeit addiert wird. */
	public Vec3 modifyPush(Vec3 push) {
		double h = horizontal.getValue() / 100;
		double v = vertical.getValue() / 100;
		return new Vec3(push.x * h, push.y * v, push.z * h);
	}
}
