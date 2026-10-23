package dev.cheecky.modules;

import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.CheckboxSetting;
import dev.cheecky.setting.SliderSetting;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.phys.Vec3;

public class Flight extends Module {
	private static final float VANILLA_FLY_SPEED = 0.05F;

	private final SliderSetting speed = add(new SliderSetting("Tempo",
		"Fluggeschwindigkeit als Vielfaches des Kreativ-Flugs.", 2, 0.5, 10, 0.5, "x"));
	private final CheckboxSetting antiKick = add(new CheckboxSetting("Anti-Kick",
		"Sinkt regelmäßig ein winziges Stück ab, damit der Vanilla-Server dich nicht wegen \"Flying is not enabled\" rauswirft. Der Flug wird dabei nicht unterbrochen.", true));
	private final SliderSetting antiKickInterval = add(new SliderSetting("Intervall",
		"Alle wie viele Ticks Anti-Kick absinkt (20 Ticks = 1 Sekunde). Vanilla wirft nach 80 Ticks Schweben raus.", 40, 10, 70, 5, " Ticks"));
	private final SliderSetting antiKickDrop = add(new SliderSetting("Sinktiefe",
		"Wie weit Anti-Kick jeweils absinkt. Vanilla verlangt mindestens 0,03125.", 0.04, 0.035, 0.2, 0.005, " Blöcke"));

	private int ticksSinceDrop;

	public Flight() {
		super("Flight", "Lässt dich wie im Kreativmodus fliegen. Fällt auf Servern mit Anti-Cheat sehr wahrscheinlich auf.",
			Category.MOVEMENT, InputConstants.KEY_G);
	}

	@Override
	public void onTick() {
		// Der Server kann die Fähigkeiten zurücksetzen (z. B. beim Spielmoduswechsel), deshalb jeden Tick
		Abilities abilities = mc.player.getAbilities();
		abilities.mayfly = true;
		abilities.flying = true;
		abilities.setFlyingSpeed(VANILLA_FLY_SPEED * speed.getValueF());

		// Echte Abwärtsbewegung für einen Tick, nichts Vorgetäuschtes. Danach fliegt man normal weiter.
		ticksSinceDrop++;
		if (antiKick.isChecked() && ticksSinceDrop >= antiKickInterval.getValue() && !mc.player.onGround()) {
			Vec3 motion = mc.player.getDeltaMovement();
			mc.player.setDeltaMovement(motion.x, -antiKickDrop.getValue(), motion.z);
			ticksSinceDrop = 0;
		}
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
