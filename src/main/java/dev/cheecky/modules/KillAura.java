package dev.cheecky.modules;

import dev.cheecky.compat.Compat;
import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.CheckboxSetting;
import dev.cheecky.setting.SliderSetting;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

public class KillAura extends Module {
	private final SliderSetting range = add(new SliderSetting("Reichweite",
		"Wie weit entfernt Ziele angegriffen werden.", 4.25, 1, 6, 0.05, ""));
	private final CheckboxSetting players = add(new CheckboxSetting("Spieler", "Greift Spieler an.", true));
	private final CheckboxSetting monsters = add(new CheckboxSetting("Monster", "Greift Monster an.", true));
	private final CheckboxSetting animals = add(new CheckboxSetting("Tiere", "Greift Tiere an.", false));

	public KillAura() {
		super("KillAura", "Greift automatisch das nächste Ziel in Reichweite an, sobald dein Schlag voll aufgeladen ist.",
			Category.COMBAT, InputConstants.KEY_R);
	}

	@Override
	public void onTick() {
		LocalPlayer player = mc.player;
		if (mc.gameMode == null || player.isSpectator())
			return;

		// Vanilla-Cooldown abwarten, sonst macht der Schlag kaum Schaden
		if (player.getAttackStrengthScale(0) < 1)
			return;

		LivingEntity target = findTarget(player);
		if (target == null)
			return;

		mc.gameMode.attack(player, target);
		Compat.swingAfterAttack(player);
	}

	private LivingEntity findTarget(LocalPlayer player) {
		double maxRange = range.getValue();
		LivingEntity best = null;
		double bestDistance = Double.MAX_VALUE;

		for (Entity entity : mc.level.entitiesForRendering()) {
			if (!(entity instanceof LivingEntity living) || living == player || !living.isAlive())
				continue;
			if (!isWantedType(living))
				continue;

			double distance = player.distanceTo(living);
			if (distance > maxRange || distance >= bestDistance || !player.hasLineOfSight(living))
				continue;

			best = living;
			bestDistance = distance;
		}
		return best;
	}

	private boolean isWantedType(LivingEntity entity) {
		if (entity instanceof Player p)
			return players.isChecked() && !p.isSpectator() && !p.isCreative();
		if (entity instanceof Enemy)
			return monsters.isChecked();
		if (entity instanceof Animal)
			return animals.isChecked();
		return false;
	}
}
