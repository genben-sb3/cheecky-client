package dev.cheecky.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import dev.cheecky.Cheecky;
import dev.cheecky.command.ChatOutput;
import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.CheckboxSetting;
import dev.cheecky.setting.SliderSetting;
import dev.cheecky.util.BaritoneBridge;
import dev.cheecky.util.Friends;
import dev.cheecky.util.InvUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/**
 * Arbeitet eine Einkaufsliste ab ({@code .sammle}): Baritone baut jeden Eintrag ab, bis genug davon
 * im Inventar ist. Gedacht für unbeaufsichtigtes Sammeln, deshalb isst es selbst und loggt bei
 * Gefahr aus.
 */
public class Gatherer extends Module {
	/** Ein Eintrag der Liste: so viele {@code item} sammeln, indem {@code blocks} abgebaut werden. */
	public record Order(String name, Item item, Set<Block> blocks, int amount) {}

	private static final int CHECK_INTERVAL = 20;
	/** Essen, das schadet oder wegteleportiert, wird nie gegessen. */
	private static final Set<Item> BAD_FOOD = Set.of(Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.POISONOUS_POTATO,
		Items.PUFFERFISH, Items.CHORUS_FRUIT, Items.SUSPICIOUS_STEW);

	private final SliderSetting eatBelow = add(new SliderSetting("Essen ab",
		"Isst Essen aus der Hotbar, sobald der Hunger so tief fällt (20 = voll). Baritone pausiert solange.", 14, 1, 19, 1, ""));
	private final CheckboxSetting keepGoldenApples = add(new CheckboxSetting("Goldäpfel aufheben",
		"Isst keine Goldäpfel, die sind für den Kampf.", true));
	private final SliderSetting minHealth = add(new SliderSetting("Gefahr: Leben",
		"Gilt als Gefahr, wenn das Leben so tief fällt (20 = voll).", 10, 2, 19, 1, ""));
	private final SliderSetting playerDistance = add(new SliderSetting("Gefahr: Spieler",
		"Gilt als Gefahr, wenn ein Spieler, der kein Freund ist, näher kommt. 0 = aus.", 64, 0, 128, 8, " Blöcke"));
	private final CheckboxSetting logoutOnDanger = add(new CheckboxSetting("Bei Gefahr ausloggen",
		"Trennt die Verbindung bei Gefahr. Aus: hält nur Baritone an.", true));
	private final CheckboxSetting logoutWhenDone = add(new CheckboxSetting("Am Ende ausloggen",
		"Trennt die Verbindung, wenn alles gesammelt oder das Inventar voll ist, statt AFK stehen zu bleiben.", true));

	private final List<Order> orders = new ArrayList<>();
	private Order running;
	private int checkTimer;
	/** Hotbar-Slot vor dem Essen, -1 = isst gerade nicht. */
	private int previousSlot = -1;

	public Gatherer() {
		super("Sammler", "Baritone arbeitet die Liste aus .sammle allein ab: abbauen, bis genug im Inventar ist. Isst selbst, loggt bei Gefahr aus. Braucht Baritone.",
			Category.WORLD, 0);
	}

	@Override
	public boolean isAvailable() {
		return BaritoneBridge.isAvailable();
	}

	@Override
	public String getUnavailableReason() {
		return "Baritone ist nicht installiert. Anleitung in der README.";
	}

	@Override
	public boolean rememberEnabled() {
		return false;
	}

	public List<Order> getOrders() {
		return Collections.unmodifiableList(orders);
	}

	/** Ersetzt einen vorhandenen Eintrag für dasselbe Item. */
	public void addOrder(Order order) {
		orders.removeIf(existing -> existing.item() == order.item());
		orders.add(order);
	}

	public boolean removeOrder(Item item) {
		return orders.removeIf(order -> order.item() == item);
	}

	public void clearOrders() {
		orders.clear();
	}

	/** Wie viele davon im Inventar sind, Hotbar und Zweithand mitgezählt. */
	public int count(Item item) {
		LocalPlayer player = mc.player;
		int total = player.getOffhandItem().is(item) ? player.getOffhandItem().getCount() : 0;
		for (int i = 0; i < 36; i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (stack.is(item))
				total += stack.getCount();
		}
		return total;
	}

	@Override
	protected void onEnable() {
		running = null;
		checkTimer = 0;
		if (orders.isEmpty()) {
			ChatOutput.error("Die Sammelliste ist leer. Eintragen mit .sammle <was> <menge>, z. B. .sammle sand 256");
			setEnabled(false);
		}
	}

	@Override
	protected void onDisable() {
		if (previousSlot >= 0)
			finishEating();
		if (running != null)
			BaritoneBridge.stop();
		running = null;
	}

	@Override
	public void onTick() {
		LocalPlayer player = mc.player;
		String danger = danger(player);
		if (danger != null) {
			stop("Gefahr: " + danger, logoutOnDanger.isChecked());
			return;
		}

		if (previousSlot >= 0) {
			// Taste halten, bis fertig gegessen ist; danach macht Baritone weiter
			if (player.isUsingItem())
				mc.options.keyUse.setDown(true);
			else {
				finishEating();
				BaritoneBridge.execute("resume");
			}
			return;
		}
		if (player.getFoodData().getFoodLevel() <= eatBelow.getValue() && startEating(player))
			return;

		if (--checkTimer > 0)
			return;
		checkTimer = CHECK_INTERVAL;

		Order next = orders.stream().filter(order -> count(order.item()) < order.amount()).findFirst().orElse(null);
		if (next == null) {
			stop("Alles gesammelt.", logoutWhenDone.isChecked());
			return;
		}
		if (player.getInventory().getFreeSlot() < 0) {
			stop("Das Inventar ist voll.", logoutWhenDone.isChecked());
			return;
		}
		if (next == running)
			return;

		running = next;
		if (BaritoneBridge.mine(next.blocks().toArray(Block[]::new)))
			ChatOutput.info("Sammler: Baritone sammelt jetzt " + next.name() + " (" + count(next.item()) + "/" + next.amount() + ").");
		else {
			ChatOutput.error("Baritone konnte nicht gestartet werden, Details im Log.");
			setEnabled(false);
		}
	}

	/** @return Grund, warum es gefährlich ist, oder null */
	private String danger(LocalPlayer player) {
		if (player.getHealth() <= minHealth.getValue())
			return "nur noch " + Math.round(player.getHealth()) + " Leben";

		double range = playerDistance.getValue();
		if (range > 0)
			for (Player other : mc.level.players())
				if (other != player && !Friends.isFriend(other) && other.distanceTo(player) <= range)
					return "Spieler " + other.getName().getString() + " ist " + Math.round(other.distanceTo(player)) + " Blöcke entfernt";

		// Verhungern kann man nicht, aber ohne Essen verliert man bei Hunger 0 auf Schwer Leben
		if (player.getFoodData().getFoodLevel() <= 6 && findFood() < 0)
			return "Hunger und kein Essen in der Hotbar";
		return null;
	}

	private int findFood() {
		return InvUtil.findHotbar(stack -> stack.has(DataComponents.FOOD)
			&& !BAD_FOOD.contains(stack.getItem())
			&& !(keepGoldenApples.isChecked() && (stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE))));
	}

	private boolean startEating(LocalPlayer player) {
		int slot = findFood();
		if (slot < 0 || mc.gameMode == null || mc.gui.screen() != null || player.isUsingItem())
			return false;

		// Baritone würde sonst weiter Tasten drücken und den Slot wechseln
		BaritoneBridge.execute("pause");
		previousSlot = player.getInventory().getSelectedSlot();
		player.getInventory().setSelectedSlot(slot);
		mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
		mc.options.keyUse.setDown(true);
		return true;
	}

	private void finishEating() {
		mc.options.keyUse.setDown(false);
		if (mc.player != null)
			mc.player.getInventory().setSelectedSlot(previousSlot);
		previousSlot = -1;
	}

	private void stop(String reason, boolean logout) {
		setEnabled(false);
		BaritoneBridge.execute("resume");
		if (!logout) {
			ChatOutput.info("Sammler gestoppt: " + reason);
			return;
		}
		// Nach dem Trennen ist der Chat weg, deshalb steht der Grund im Trenn-Bildschirm und im Log
		Cheecky.LOGGER.info("Sammler loggt aus: {}", reason);
		mc.getConnection().getConnection().disconnect(Component.literal("Cheecky Sammler: " + reason));
	}
}
