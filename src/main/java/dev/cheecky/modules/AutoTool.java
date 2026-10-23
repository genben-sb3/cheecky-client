package dev.cheecky.modules;

import java.util.Set;

import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.CheckboxSetting;
import dev.cheecky.util.InvUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AutoTool extends Module {
	/** Blöcke, die nur mit Behutsamkeit als Block droppen (sonst gar nicht oder als etwas anderes). */
	private static final Set<String> SILK_BLOCKS = Set.of("ice", "packed_ice", "blue_ice", "bookshelf", "glowstone",
		"sea_lantern", "ender_chest", "turtle_egg", "bee_nest", "beehive", "grass_block", "mycelium", "podzol",
		"campfire", "soul_campfire", "sculk", "sculk_vein", "sculk_catalyst", "sculk_sensor", "sculk_shrieker",
		"calibrated_sculk_sensor", "mushroom_stem", "brown_mushroom_block", "red_mushroom_block");

	private final CheckboxSetting preferSilk = add(new CheckboxSetting("Behutsamkeit",
		"Nimmt für Glas, Eis, Glowstone, Bücherregale usw. ein Werkzeug mit Behutsamkeit, damit der Block selbst droppt.", true));
	private final CheckboxSetting protectTools = add(new CheckboxSetting("Werkzeug schonen",
		"Nimmt keine Werkzeuge mit 5 oder weniger Haltbarkeit, damit sie nicht kaputtgehen.", true));
	private final CheckboxSetting dropHint = add(new CheckboxSetting("Drop-Hinweis",
		"Warnt unter dem Fadenkreuz, wenn der anvisierte Block mit deinem Werkzeug nichts oder nur etwas anderes droppt.", true));

	public AutoTool() {
		super("AutoTool", "Wechselt beim Abbauen automatisch zum besten Werkzeug in der Hotbar.", Category.WORLD, 0);
	}

	/** Vom Mixin beim Start und während des Abbauens aufgerufen. */
	public void onDestroyBlock(BlockState state) {
		if (!isEnabled() || mc.player == null)
			return;

		LocalPlayer player = mc.player;
		int current = player.getInventory().getSelectedSlot();
		int best = current;
		double bestScore = score(player.getInventory().getItem(current), state);
		for (int i = 0; i < 9; i++) {
			double score = score(player.getInventory().getItem(i), state);
			if (score > bestScore) {
				best = i;
				bestScore = score;
			}
		}
		if (best != current)
			player.getInventory().setSelectedSlot(best);
	}

	private double score(ItemStack stack, BlockState state) {
		if (protectTools.isChecked() && stack.isDamageableItem() && InvUtil.durabilityLeft(stack) <= 5)
			return Double.NEGATIVE_INFINITY;

		double score = stack.getDestroySpeed(state);
		if (!state.requiresCorrectToolForDrops() || stack.isCorrectToolForDrops(state))
			score += 1000;

		boolean silk = hasSilkTouch(stack);
		if (preferSilk.isChecked() && needsSilkTouch(state) && silk)
			score += 500;
		else if (silk)
			// Bei Erzen usw. lieber ohne Behutsamkeit, sonst kommt nur der Erzblock statt der Erze
			score -= 0.5;
		return score;
	}

	public static boolean needsSilkTouch(BlockState state) {
		String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
		return SILK_BLOCKS.contains(path) || path.contains("glass") || path.endsWith("_leaves");
	}

	private static boolean hasSilkTouch(ItemStack stack) {
		if (stack.isEmpty() || mc.level == null)
			return false;
		Holder<Enchantment> silk = mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
			.get(Enchantments.SILK_TOUCH).orElse(null);
		return silk != null && EnchantmentHelper.getItemEnchantmentLevel(silk, stack) > 0;
	}

	/** Text für den HUD-Hinweis unter dem Fadenkreuz, null = alles in Ordnung. */
	public String getDropWarning() {
		if (!isEnabled() || !dropHint.isChecked() || mc.player == null || mc.level == null
			|| !(mc.hitResult instanceof BlockHitResult hit))
			return null;

		BlockState state = mc.level.getBlockState(hit.getBlockPos());
		if (state.isAir())
			return null;
		if (state.requiresCorrectToolForDrops() && !mc.player.hasCorrectToolForDrops(state))
			return "Kein Drop mit diesem Werkzeug!";
		if (needsSilkTouch(state) && !hasSilkTouch(mc.player.getMainHandItem()))
			return "Droppt nur mit Behutsamkeit als Block";
		return null;
	}
}
