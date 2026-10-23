package dev.cheecky.util;

import java.util.function.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

/**
 * Inventar-Helfer. Indizes: 0-8 Hotbar, 9-35 Inventar (wie {@code Inventory.getItem}).
 * Klicks laufen über das Spieler-Inventarmenü, genau wie echte Mausklicks.
 */
public final class InvUtil {
	/** Slot-Nummern im Inventarmenü des Spielers. */
	public static final int MENU_OFFHAND = 45;
	private static final int MENU_HOTBAR_START = 36;

	private InvUtil() {}

	private static Minecraft mc() {
		return Minecraft.getInstance();
	}

	/** @return Hotbar-Slot 0-8 oder -1 */
	public static int findHotbar(Predicate<ItemStack> filter) {
		LocalPlayer player = mc().player;
		for (int i = 0; i < 9; i++)
			if (filter.test(player.getInventory().getItem(i)))
				return i;
		return -1;
	}

	/** @return Inventar-Index 0-35 oder -1. Durchsucht erst das Inventar, dann die Hotbar. */
	public static int findAnywhere(Predicate<ItemStack> filter) {
		LocalPlayer player = mc().player;
		for (int i = 9; i < 36; i++)
			if (filter.test(player.getInventory().getItem(i)))
				return i;
		return findHotbar(filter);
	}

	/** Inventar-Index (0-35) in die Slot-Nummer des Inventarmenüs umrechnen. */
	public static int toMenuSlot(int inventoryIndex) {
		return inventoryIndex < 9 ? MENU_HOTBAR_START + inventoryIndex : inventoryIndex;
	}

	public static int armorMenuSlot(EquipmentSlot slot) {
		return switch (slot) {
			case HEAD -> 5;
			case CHEST -> 6;
			case LEGS -> 7;
			case FEET -> 8;
			default -> throw new IllegalArgumentException("Kein Rüstungsslot: " + slot);
		};
	}

	/**
	 * Inventar-Klicks nur, wenn kein fremdes Menü (Truhe, Ofen ...) offen ist. Sonst würden
	 * die Slot-Nummern nicht passen.
	 */
	public static boolean canClick() {
		var screen = mc().gui.screen();
		return !(screen instanceof AbstractContainerScreen<?>) || screen instanceof InventoryScreen;
	}

	public static void click(int menuSlot, ContainerInput input, int button) {
		LocalPlayer player = mc().player;
		mc().gameMode.handleContainerInput(player.inventoryMenu.containerId, menuSlot, button, input, player);
	}

	/** Tauscht zwei Slots über drei Linksklicks, so wie man es von Hand machen würde. */
	public static void swap(int menuSlotA, int menuSlotB) {
		click(menuSlotA, ContainerInput.PICKUP, 0);
		click(menuSlotB, ContainerInput.PICKUP, 0);
		click(menuSlotA, ContainerInput.PICKUP, 0);
	}

	/** Restliche Haltbarkeit; Gegenstände ohne Haltbarkeit zählen als unendlich. */
	public static int durabilityLeft(ItemStack stack) {
		return stack.isDamageableItem() ? stack.getMaxDamage() - stack.getDamageValue() : Integer.MAX_VALUE;
	}
}
