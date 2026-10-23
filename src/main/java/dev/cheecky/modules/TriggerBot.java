package dev.cheecky.modules;

import dev.cheecky.compat.Compat;
import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.module.Modules;
import dev.cheecky.setting.CheckboxSetting;
import dev.cheecky.util.Friends;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public class TriggerBot extends Module {
	private final CheckboxSetting players = add(new CheckboxSetting("Spieler", "Greift Spieler an.", true));
	private final CheckboxSetting monsters = add(new CheckboxSetting("Monster", "Greift Monster an.", true));
	private final CheckboxSetting animals = add(new CheckboxSetting("Tiere", "Greift Tiere an.", false));

	public TriggerBot() {
		super("TriggerBot", "Schlägt automatisch zu, sobald dein Fadenkreuz auf einem Ziel liegt und der Schlag voll aufgeladen ist.",
			Category.COMBAT, 0);
	}

	@Override
	public void onTick() {
		LocalPlayer player = mc.player;
		if (mc.gameMode == null || player.isSpectator() || mc.gui.screen() != null)
			return;
		if (!(mc.hitResult instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity target))
			return;
		if (!target.isAlive() || !isWantedType(target) || Friends.isFriend(target) || player.getAttackStrengthScale(0) < 1)
			return;
		if (Modules.CRITICALS.shouldWaitForCrit(player))
			return;

		mc.gameMode.attack(player, target);
		Compat.swingAfterAttack(player);
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
