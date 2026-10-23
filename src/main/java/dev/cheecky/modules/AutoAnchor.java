package dev.cheecky.modules;

import dev.cheecky.compat.Compat;
import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.CheckboxSetting;
import dev.cheecky.setting.SliderSetting;
import dev.cheecky.util.Friends;
import dev.cheecky.util.InvUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Setzt einen Respawn-Anker neben das Ziel, lädt ihn mit Glowstone und zündet ihn.
 * Funktioniert nur, wo Anker explodieren (Oberwelt, End), nicht im Nether.
 */
public class AutoAnchor extends Module {
	private static final float ANCHOR_POWER = 5;
	private static final double REACH = 4.5;

	private final SliderSetting range = add(new SliderSetting("Reichweite",
		"Wie weit das Ziel höchstens entfernt sein darf.", 5, 2, 6, 0.5, ""));
	private final CheckboxSetting onlyPlayers = add(new CheckboxSetting("Nur Spieler",
		"Aus: greift auch Monster und Tiere an. Freunde (.friend) werden nie angegriffen.", true));
	private final SliderSetting selfProtect = add(new SliderSetting("Selbstschutz",
		"Zündet nicht, wenn du danach weniger als so viele Herzen hättest (Rüstung nicht eingerechnet).", 4, 0, 10, 0.5, " Herzen"));
	private final SliderSetting delay = add(new SliderSetting("Verzögerung",
		"Mindestzeit zwischen zwei Aktionen (6b6t: mindestens 100 ms).", 100, 100, 1000, 50, " ms"));

	private BlockPos anchorPos;
	private int cooldownTicks;

	public AutoAnchor() {
		super("AutoAnchor", "Setzt Respawn-Anker neben Gegner, lädt sie mit Glowstone und zündet sie. Braucht Anker und Glowstone in der Hotbar.",
			Category.COMBAT, 0);
	}

	@Override
	protected void onDisable() {
		anchorPos = null;
	}

	@Override
	public void onTick() {
		if (cooldownTicks > 0) {
			cooldownTicks--;
			return;
		}
		if (mc.gameMode == null || mc.gui.screen() != null)
			return;

		LocalPlayer player = mc.player;
		int anchorSlot = InvUtil.findHotbar(s -> s.is(Items.RESPAWN_ANCHOR));
		int glowstoneSlot = InvUtil.findHotbar(s -> s.is(Items.GLOWSTONE));
		if (anchorSlot < 0 || glowstoneSlot < 0)
			return;

		if (anchorPos != null && !mc.level.getBlockState(anchorPos).is(Blocks.RESPAWN_ANCHOR))
			anchorPos = null;

		if (anchorPos == null) {
			placeAnchor(player, anchorSlot);
			return;
		}

		BlockState anchor = mc.level.getBlockState(anchorPos);
		if (anchor.getValue(RespawnAnchorBlock.CHARGE) == 0) {
			useOn(glowstoneSlot, anchorPos, Direction.UP);
			return;
		}

		// Zünden: mit etwas anderem als Glowstone in der Hand anklicken (hier der Anker selbst)
		if (isSafe(player, Vec3.atCenterOf(anchorPos))) {
			useOn(anchorSlot, anchorPos, Direction.UP);
			anchorPos = null;
		}
	}

	private void placeAnchor(LocalPlayer player, int anchorSlot) {
		LivingEntity target = findTarget(player);
		if (target == null)
			return;

		BlockPos feet = target.blockPosition();
		BlockPos[] candidates = {feet.north(), feet.south(), feet.east(), feet.west(), feet.above(2)};
		for (BlockPos pos : candidates) {
			if (!canPlaceAt(player, target, pos) || !isSafe(player, Vec3.atCenterOf(pos)))
				continue;

			// An einen festen Nachbarblock klicken, damit der Anker an pos landet
			for (Direction side : Direction.values()) {
				BlockPos neighbor = pos.relative(side);
				BlockState neighborState = mc.level.getBlockState(neighbor);
				if (neighborState.canBeReplaced() || !neighborState.getFluidState().isEmpty())
					continue;

				useOn(anchorSlot, neighbor, side.getOpposite());
				anchorPos = pos;
				return;
			}
		}
	}

	private boolean canPlaceAt(LocalPlayer player, LivingEntity target, BlockPos pos) {
		if (!mc.level.getBlockState(pos).canBeReplaced())
			return false;
		// Wo Anker als Spawnpunkt funktionieren (Nether), explodieren sie nicht
		if (mc.level.environmentAttributes().getValue(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, pos))
			return false;
		if (player.getEyePosition().distanceTo(Vec3.atCenterOf(pos)) > REACH)
			return false;

		AABB block = new AABB(pos);
		return !block.intersects(target.getBoundingBox()) && !block.intersects(player.getBoundingBox());
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

	/** Grobe Vanilla-Explosionsformel, Deckung und Rüstung nicht eingerechnet (also eher zu vorsichtig). */
	private boolean isSafe(LocalPlayer player, Vec3 center) {
		double distance = player.position().distanceTo(center) / (ANCHOR_POWER * 2);
		if (distance > 1)
			return true;

		double impact = 1 - distance;
		double damage = (impact * impact + impact) / 2 * 7 * ANCHOR_POWER * 2 + 1;
		Difficulty difficulty = mc.level.getDifficulty();
		if (difficulty == Difficulty.PEACEFUL)
			damage = 0;
		else if (difficulty == Difficulty.EASY)
			damage = Math.min(damage / 2 + 1, damage);
		else if (difficulty == Difficulty.HARD)
			damage *= 1.5;

		double healthAfter = player.getHealth() + player.getAbsorptionAmount() - damage;
		return healthAfter >= selfProtect.getValue() * 2;
	}

	private void useOn(int hotbarSlot, BlockPos pos, Direction side) {
		mc.player.getInventory().setSelectedSlot(hotbarSlot);
		Vec3 hit = Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(side.getUnitVec3i()).scale(0.5));
		mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(hit, side, pos, false));
		Compat.swingHand(mc.player);
		cooldownTicks = (int) Math.ceil(delay.getValue() / 50);
	}
}
