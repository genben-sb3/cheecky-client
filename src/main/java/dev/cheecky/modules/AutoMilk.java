package dev.cheecky.modules;

import dev.cheecky.command.ChatOutput;
import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.CheckboxSetting;
import dev.cheecky.util.InvUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;

/**
 * Trinkt einen Milcheimer aus der Hotbar, sobald Wither (oder Gift) wirkt. Milch entfernt in Vanilla
 * alle Effekte, also auch gute wie Feuerresistenz.
 */
public class AutoMilk extends Module {
	private final CheckboxSetting wither = add(new CheckboxSetting("Wither", "Trinkt bei Wither-Effekt.", true));
	private final CheckboxSetting poison = add(new CheckboxSetting("Gift", "Trinkt bei Vergiftung.", false));
	private final CheckboxSetting keepFireResistance = add(new CheckboxSetting("Feuerresistenz behalten",
		"Trinkt nicht, solange Feuerresistenz wirkt (Milch würde sie entfernen, gefährlich über Lava).", true));

	/** Hotbar-Slot vor dem Trinken, -1 = trinkt gerade nicht. */
	private int previousSlot = -1;

	public AutoMilk() {
		super("AutoMilk", "Trinkt automatisch Milch aus der Hotbar, wenn der Wither-Effekt (optional Gift) wirkt. Milch entfernt alle Effekte.",
			Category.PLAYER, 0);
	}

	@Override
	protected void onDisable() {
		finishDrinking();
	}

	@Override
	public void onTick() {
		LocalPlayer player = mc.player;
		if (previousSlot >= 0) {
			// Taste gedrückt halten, bis der Eimer leer ist; danach zurück zum alten Slot
			if (player.isUsingItem() && player.getUseItem().is(Items.MILK_BUCKET))
				mc.options.keyUse.setDown(true);
			else
				finishDrinking();
			return;
		}

		if (!needsMilk(player) || mc.gameMode == null || mc.gui.screen() != null || player.isUsingItem())
			return;
		int slot = InvUtil.findHotbar(stack -> stack.is(Items.MILK_BUCKET));
		if (slot < 0)
			return;

		previousSlot = player.getInventory().getSelectedSlot();
		player.getInventory().setSelectedSlot(slot);
		mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
		mc.options.keyUse.setDown(true);
		ChatOutput.info("AutoMilk: trinke Milch gegen " + (player.hasEffect(MobEffects.WITHER) ? "Wither" : "Gift") + ".");
	}

	private boolean needsMilk(LocalPlayer player) {
		if (keepFireResistance.isChecked() && player.hasEffect(MobEffects.FIRE_RESISTANCE))
			return false;
		return wither.isChecked() && player.hasEffect(MobEffects.WITHER)
			|| poison.isChecked() && player.hasEffect(MobEffects.POISON);
	}

	private void finishDrinking() {
		if (previousSlot < 0)
			return;
		mc.options.keyUse.setDown(false);
		if (mc.player != null)
			mc.player.getInventory().setSelectedSlot(previousSlot);
		previousSlot = -1;
	}
}
