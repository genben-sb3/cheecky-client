package dev.cheecky.modules;

import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.util.InvUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;

public class AutoTotem extends Module {
	/** Ticks zwischen zwei Tauschversuchen, damit der Server hinterherkommt. */
	private static final int COOLDOWN = 3;
	private int cooldown;

	public AutoTotem() {
		super("AutoTotem", "Legt automatisch ein Totem der Unsterblichkeit in die Zweithand, sobald dort keins liegt.",
			Category.COMBAT, 0);
	}

	@Override
	public void onTick() {
		if (cooldown > 0) {
			cooldown--;
			return;
		}
		if (mc.player.getItemBySlot(EquipmentSlot.OFFHAND).is(Items.TOTEM_OF_UNDYING) || !InvUtil.canClick())
			return;

		int totem = InvUtil.findAnywhere(stack -> stack.is(Items.TOTEM_OF_UNDYING));
		if (totem < 0)
			return;

		InvUtil.swap(InvUtil.toMenuSlot(totem), InvUtil.MENU_OFFHAND);
		cooldown = COOLDOWN;
	}
}
