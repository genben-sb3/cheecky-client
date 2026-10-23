package dev.cheecky.modules;

import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.CheckboxSetting;
import dev.cheecky.setting.SliderSetting;
import dev.cheecky.util.InvUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Komfort für den echten Elytra-Flug, wie Wursts ExtraElytra. Ändert nichts an der Elytra-Physik:
 * kein zusätzliches Tempo, keine vorgetäuschten Bewegungen.
 */
public class ElytraFly extends Module {
	private static final int FIREWORK_COOLDOWN = 20;

	private final CheckboxSetting easyStart = add(new CheckboxSetting("Einfacher Start",
		"Öffnet die Elytra beim Springen automatisch, sobald du fällst. Kein Doppelsprung nötig.", true));
	private final CheckboxSetting stopInWater = add(new CheckboxSetting("Stopp im Wasser",
		"Schließt die Elytra, wenn du ins Wasser fliegst.", true));
	private final CheckboxSetting autoFirework = add(new CheckboxSetting("AutoFirework",
		"Zündet eine Rakete aus der Hotbar, wenn du zu langsam wirst.", true));
	private final SliderSetting minSpeed = add(new SliderSetting("Mindesttempo",
		"Unter diesem Tempo (Blöcke pro Tick) zündet AutoFirework. 1,0 = 20 Blöcke/s.", 1, 0.3, 2, 0.1, ""));
	private final CheckboxSetting autoSwap = add(new CheckboxSetting("Elytra wechseln",
		"Legt automatisch eine Ersatz-Elytra aus dem Inventar an, bevor die getragene kaputtgeht.", true));
	private final SliderSetting minDurability = add(new SliderSetting("Haltbarkeit",
		"Ab so wenig restlicher Haltbarkeit wird gewechselt.", 10, 2, 50, 1, ""));

	private int fireworkCooldown;
	private int swapCooldown;

	public ElytraFly() {
		super("ElytraFly", "Einfacher Elytra-Start, automatische Raketen, Stopp im Wasser und Ersatz-Elytra. Echte Elytra-Physik.",
			Category.MOVEMENT, 0);
	}

	@Override
	public void onTick() {
		LocalPlayer player = mc.player;
		if (fireworkCooldown > 0)
			fireworkCooldown--;
		if (swapCooldown > 0)
			swapCooldown--;

		if (autoSwap.isChecked())
			swapWornOutElytra(player);

		boolean jumpDown = mc.options.keyJump.isDown();
		if (player.isFallFlying()) {
			if (stopInWater.isChecked() && player.isInWater()) {
				// Derselbe Befehl wie beim Öffnen: der Server schließt die Elytra, wenn sie schon offen ist
				sendStartFallFlying(player);
			} else if (autoFirework.isChecked()) {
				useFireworkIfSlow(player);
			}
		} else if (easyStart.isChecked() && jumpDown && !player.onGround() && player.getDeltaMovement().y < 0
			&& !player.isInWater() && player.tryToStartFallFlying()) {
			sendStartFallFlying(player);
		}
	}

	private static void sendStartFallFlying(LocalPlayer player) {
		player.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
	}

	private void useFireworkIfSlow(LocalPlayer player) {
		Vec3 motion = player.getDeltaMovement();
		if (fireworkCooldown > 0 || motion.length() >= minSpeed.getValue())
			return;

		int slot = InvUtil.findHotbar(stack -> stack.is(Items.FIREWORK_ROCKET));
		if (slot < 0)
			return;

		int previous = player.getInventory().getSelectedSlot();
		player.getInventory().setSelectedSlot(slot);
		mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
		player.getInventory().setSelectedSlot(previous);
		fireworkCooldown = FIREWORK_COOLDOWN;
	}

	private void swapWornOutElytra(LocalPlayer player) {
		ItemStack worn = player.getItemBySlot(EquipmentSlot.CHEST);
		if (swapCooldown > 0 || !worn.is(Items.ELYTRA) || InvUtil.durabilityLeft(worn) > minDurability.getValue()
			|| !InvUtil.canClick())
			return;

		int spare = InvUtil.findAnywhere(stack -> stack.is(Items.ELYTRA) && InvUtil.durabilityLeft(stack) > minDurability.getValue());
		if (spare < 0)
			return;

		InvUtil.swap(InvUtil.toMenuSlot(spare), InvUtil.armorMenuSlot(EquipmentSlot.CHEST));
		swapCooldown = 10;
	}
}
