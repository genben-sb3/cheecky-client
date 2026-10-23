package dev.cheecky.modules;

import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.util.InvUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;

public class AutoArmor extends Module {
	private static final EquipmentSlot[] ARMOR_SLOTS = {
		EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
	private static final int COOLDOWN = 5;
	private int cooldown;

	public AutoArmor() {
		super("AutoArmor", "Zieht automatisch die beste Rüstung aus dem Inventar an. Elytren werden nie ausgezogen.",
			Category.COMBAT, 0);
	}

	@Override
	public void onTick() {
		if (cooldown > 0) {
			cooldown--;
			return;
		}
		if (!InvUtil.canClick())
			return;

		for (EquipmentSlot slot : ARMOR_SLOTS) {
			ItemStack worn = mc.player.getItemBySlot(slot);
			// Wer mit Elytra fliegt, will sie nicht gegen einen Brustpanzer getauscht bekommen
			if (worn.is(Items.ELYTRA))
				continue;

			int best = -1;
			double bestScore = score(worn, slot);
			for (int i = 0; i < 36; i++) {
				ItemStack stack = mc.player.getInventory().getItem(i);
				double score = score(stack, slot);
				if (score > bestScore) {
					best = i;
					bestScore = score;
				}
			}
			if (best < 0)
				continue;

			InvUtil.swap(InvUtil.toMenuSlot(best), InvUtil.armorMenuSlot(slot));
			cooldown = COOLDOWN;
			return;
		}
	}

	/** Rüstung + halbe Härte; -1 für Gegenstände, die nicht in diesen Slot gehören. */
	private static double score(ItemStack stack, EquipmentSlot slot) {
		Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
		if (stack.isEmpty() || equippable == null || equippable.slot() != slot)
			return -1;

		ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		double armor = modifiers.compute(Attributes.ARMOR, 0, slot);
		double toughness = modifiers.compute(Attributes.ARMOR_TOUGHNESS, 0, slot);
		return armor + toughness / 2;
	}
}
