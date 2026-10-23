package dev.cheecky.modules;

import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import net.minecraft.client.player.LocalPlayer;

/**
 * Echte kritische Treffer: springt vor dem Angriff und schlägt erst im Fallen zu. Wirkt auf
 * KillAura und TriggerBot. Es werden keine Positionen vorgetäuscht.
 */
public class Criticals extends Module {
	public Criticals() {
		super("Criticals", "KillAura und TriggerBot springen vor dem Schlag und treffen im Fallen kritisch (+50 % Schaden).",
			Category.COMBAT, 0);
	}

	/**
	 * Fragt KillAura und TriggerBot vor jedem Schlag.
	 * @return true, wenn mit dem Schlag noch gewartet werden soll
	 */
	public boolean shouldWaitForCrit(LocalPlayer player) {
		if (!isEnabled() || player.isInWater() || player.onClimbable() || player.isPassenger()
			|| player.getAbilities().flying || player.isFallFlying())
			return false;

		if (player.onGround()) {
			player.jumpFromGround();
			return true;
		}
		// Kritisch nur beim Fallen, also bis zum Scheitelpunkt warten
		return player.getDeltaMovement().y >= 0;
	}
}
