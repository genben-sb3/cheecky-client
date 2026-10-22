package dev.cheecky.modules;

import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.SliderSetting;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Abilities;

public class Flight extends Module {
	private static final float VANILLA_FLY_SPEED = 0.05F;

	private final SliderSetting speed = add(new SliderSetting("Tempo",
		"Fluggeschwindigkeit als Vielfaches des Kreativ-Flugs.", 2, 0.5, 10, 0.5, "x"));

	public Flight() {
		super("Flight", "Lässt dich wie im Kreativmodus fliegen. Auf Servern nur mit allow-flight=true.",
			Category.MOVEMENT, InputConstants.KEY_G);
	}

	@Override
	public void onTick() {
		// Der Server kann die Fähigkeiten zurücksetzen (z. B. beim Spielmoduswechsel), deshalb jeden Tick
		Abilities abilities = mc.player.getAbilities();
		abilities.mayfly = true;
		abilities.flying = true;
		abilities.setFlyingSpeed(VANILLA_FLY_SPEED * speed.getValueF());
	}

	@Override
	protected void onDisable() {
		LocalPlayer player = mc.player;
		if (player == null)
			return;

		Abilities abilities = player.getAbilities();
		abilities.setFlyingSpeed(VANILLA_FLY_SPEED);
		if (!player.isCreative() && !player.isSpectator()) {
			abilities.flying = false;
			abilities.mayfly = false;
		}
	}
}
