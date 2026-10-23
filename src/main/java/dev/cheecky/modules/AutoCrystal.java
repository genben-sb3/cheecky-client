package dev.cheecky.modules;

import dev.cheecky.compat.Compat;
import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.CheckboxSetting;
import dev.cheecky.setting.SliderSetting;
import dev.cheecky.util.ExplosionDamage;
import dev.cheecky.util.Friends;
import dev.cheecky.util.InvUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Setzt End-Kristalle auf Obsidian oder Grundgestein neben dem Gegner und schlägt sie kaputt.
 * Jede Stelle wird mit der Vanilla-Explosionsformel bewertet: viel Schaden beim Gegner, wenig bei dir.
 */
public class AutoCrystal extends Module {
	private final SliderSetting range = add(new SliderSetting("Reichweite",
		"Wie weit das Ziel höchstens entfernt sein darf.", 8, 3, 12, 0.5, ""));
	private final CheckboxSetting onlyPlayers = add(new CheckboxSetting("Nur Spieler",
		"Aus: greift auch Monster und Tiere an. Freunde (.friend) werden nie angegriffen.", true));
	private final CheckboxSetting place = add(new CheckboxSetting("Platzieren",
		"Setzt selbst Kristalle. Aus: zerstört nur Kristalle, die schon liegen.", true));
	private final CheckboxSetting autoSwitch = add(new CheckboxSetting("Hand wechseln",
		"Nimmt Kristalle aus der Hotbar in die Hand. Aus: nur, wenn du sie schon in der Haupt- oder Zweithand hältst.", true));
	private final SliderSetting minDamage = add(new SliderSetting("Min. Schaden",
		"So viel Schaden muss ein Kristall beim Ziel mindestens machen.", 3, 0, 10, 0.5, " Herzen"));
	private final SliderSetting maxSelfDamage = add(new SliderSetting("Max. Eigenschaden",
		"So viel Schaden darf ein Kristall dir höchstens machen.", 4, 0, 10, 0.5, " Herzen"));
	private final SliderSetting selfProtect = add(new SliderSetting("Selbstschutz",
		"Nie so, dass du danach weniger als so viele Herzen hättest.", 4, 0, 10, 0.5, " Herzen"));
	private final SliderSetting delay = add(new SliderSetting("Verzögerung",
		"Mindestzeit zwischen zwei Aktionen. 6b6t erlaubt höchstens 10 Kristalle pro Sekunde, also mindestens 100 ms.", 100, 100, 1000, 50, " ms"));

	private int cooldownTicks;

	public AutoCrystal() {
		super("AutoCrystal", "Setzt End-Kristalle neben Gegner und zündet sie. Achtet darauf, dich selbst nicht zu töten.",
			Category.COMBAT, 0);
	}

	@Override
	public void onTick() {
		if (cooldownTicks > 0) {
			cooldownTicks--;
			return;
		}
		LocalPlayer player = mc.player;
		if (mc.gameMode == null || mc.gui.screen() != null || player.isSpectator())
			return;

		LivingEntity target = findTarget(player);
		if (target == null)
			return;

		// Erst liegende Kristalle zünden, dann neue setzen
		if (breakCrystal(player, target) || !place.isChecked())
			return;
		placeCrystal(player, target);
	}

	private boolean breakCrystal(LocalPlayer player, LivingEntity target) {
		double reach = player.entityInteractionRange();
		EndCrystal best = null;
		float bestDamage = 0;
		for (Entity entity : mc.level.entitiesForRendering()) {
			if (!(entity instanceof EndCrystal crystal) || !crystal.isAlive())
				continue;
			if (player.getEyePosition().distanceTo(crystal.position()) > reach)
				continue;

			float damage = damageIfWorthIt(player, target, crystal.position());
			if (damage > bestDamage) {
				best = crystal;
				bestDamage = damage;
			}
		}
		if (best == null)
			return false;

		mc.gameMode.attack(player, best);
		Compat.swingAfterAttack(player);
		startCooldown();
		return true;
	}

	private void placeCrystal(LocalPlayer player, LivingEntity target) {
		InteractionHand hand = crystalHand(player);
		if (hand == null)
			return;

		double reach = player.blockInteractionRange();
		int radius = (int) Math.ceil(reach);
		BlockPos eye = BlockPos.containing(player.getEyePosition());
		BlockPos best = null;
		float bestDamage = 0;
		for (BlockPos pos : BlockPos.betweenClosed(eye.offset(-radius, -radius, -radius), eye.offset(radius, radius, radius))) {
			if (!canPlaceOn(pos) || player.getEyePosition().distanceTo(Vec3.atCenterOf(pos)) > reach)
				continue;

			float damage = damageIfWorthIt(player, target, Vec3.atBottomCenterOf(pos.above()));
			if (damage > bestDamage) {
				best = pos.immutable();
				bestDamage = damage;
			}
		}
		if (best == null)
			return;

		Vec3 hit = Vec3.atCenterOf(best).add(0, 0.5, 0);
		mc.gameMode.useItemOn(player, hand, new BlockHitResult(hit, Direction.UP, best, false));
		Compat.swingHand(player, hand);
		startCooldown();
	}

	/** Wie Vanilla: Obsidian oder Grundgestein, darüber Luft, und dort steht kein Wesen im Weg. */
	private boolean canPlaceOn(BlockPos pos) {
		BlockState state = mc.level.getBlockState(pos);
		if (!state.is(Blocks.OBSIDIAN) && !state.is(Blocks.BEDROCK))
			return false;
		BlockPos above = pos.above();
		if (!mc.level.getBlockState(above).isAir())
			return false;
		AABB space = new AABB(above.getX(), above.getY(), above.getZ(), above.getX() + 1, above.getY() + 2, above.getZ() + 1);
		return mc.level.getEntitiesOfClass(Entity.class, space).isEmpty();
	}

	/** @return Schaden am Ziel, oder 0, wenn die Stelle zu wenig bringt oder dir zu gefährlich ist */
	private float damageIfWorthIt(LocalPlayer player, LivingEntity target, Vec3 crystal) {
		float targetDamage = ExplosionDamage.estimate(target, crystal, ExplosionDamage.CRYSTAL_POWER);
		if (targetDamage < minDamage.getValue() * 2)
			return 0;

		float selfDamage = ExplosionDamage.estimate(player, crystal, ExplosionDamage.CRYSTAL_POWER);
		if (selfDamage > maxSelfDamage.getValue() * 2)
			return 0;
		if (player.getHealth() + player.getAbsorptionAmount() - selfDamage < selfProtect.getValue() * 2)
			return 0;
		return targetDamage;
	}

	private InteractionHand crystalHand(LocalPlayer player) {
		if (player.getOffhandItem().is(Items.END_CRYSTAL))
			return InteractionHand.OFF_HAND;
		if (player.getMainHandItem().is(Items.END_CRYSTAL))
			return InteractionHand.MAIN_HAND;
		if (!autoSwitch.isChecked())
			return null;

		int slot = InvUtil.findHotbar(s -> s.is(Items.END_CRYSTAL));
		if (slot < 0)
			return null;
		player.getInventory().setSelectedSlot(slot);
		return InteractionHand.MAIN_HAND;
	}

	private LivingEntity findTarget(LocalPlayer player) {
		LivingEntity best = null;
		double bestDistance = range.getValue();
		for (Entity entity : mc.level.entitiesForRendering()) {
			if (!(entity instanceof LivingEntity living) || living == player || !living.isAlive() || Friends.isFriend(living))
				continue;
			if (living instanceof Player p ? p.isCreative() || p.isSpectator() : onlyPlayers.isChecked())
				continue;

			double distance = player.distanceTo(living);
			if (distance <= bestDistance) {
				best = living;
				bestDistance = distance;
			}
		}
		return best;
	}

	private void startCooldown() {
		// Aufrunden plus ein Tick Puffer: lieber knapp unter dem Server-Limit bleiben als drüber
		cooldownTicks = (int) Math.ceil(delay.getValue() / 50);
	}
}
