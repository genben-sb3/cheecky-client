package dev.cheecky.modules;

import dev.cheecky.command.ChatOutput;
import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.module.Modules;
import dev.cheecky.setting.CheckboxSetting;
import dev.cheecky.setting.SliderSetting;
import dev.cheecky.util.BaritoneBridge;
import dev.cheecky.util.InvUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Komfort für den echten Elytra-Flug, wie Wursts ExtraElytra. Ändert nichts an der Elytra-Physik:
 * kein zusätzliches Tempo, keine vorgetäuschten Bewegungen.
 * Fliegt Baritone ({@code #elytra}), hält sich ElytraFly raus und hebt nur für Baritone ab.
 */
public class ElytraFly extends Module {
	private static final int FIREWORK_COOLDOWN = 20;

	private final CheckboxSetting easyStart = add(new CheckboxSetting("Einfacher Start",
		"Öffnet die Elytra beim Springen automatisch, sobald du fällst. Kein Doppelsprung nötig. Beim Baritone-Elytra-Flug springt es selbst ab.", true));
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

	private final CheckboxSetting crashGuard = add(new CheckboxSetting("Crash-Schutz",
		"Erkennt Wände voraus und bremst rechtzeitig, indem du die Nase hochnimmst. Vanilla-Schaden entsteht nur, wenn beim Aufprall mehr als 0,3 Blöcke/Tick Tempo verloren gehen.", true));

	/** Unter diesem waagerechten Tempo macht ein Aufprall keinen Schaden: Schaden = Tempoverlust × 10 − 3. */
	private static final double SAFE_IMPACT_SPEED = 0.3;
	/** So viele Ticks voraus wird nach Wänden gesucht. */
	private static final int LOOKAHEAD_TICKS = 15;
	/** Steigt man schneller, wird nicht weiter hochgezogen (6b6t erlaubt höchstens 3,0 Blöcke/Tick nach oben). */
	private static final double MAX_CLIMB = 2.0;

	/** So lange wird nach einem Absprung gewartet, bevor es noch einmal versucht wird. */
	private static final int TAKEOFF_RETRY = 20;

	private int fireworkCooldown;
	private boolean braking;
	private int swapCooldown;
	private int takeoffCooldown;
	/** War man in diesem Baritone-Flug schon in der Luft? Danach nicht wieder abheben, z. B. nach der Landung. */
	private boolean airborneForBaritone;

	public ElytraFly() {
		super("ElytraFly", "Einfacher Elytra-Start, automatische Raketen, Crash-Schutz, Stopp im Wasser und Ersatz-Elytra. Echte Elytra-Physik.",
			Category.MOVEMENT, 0);
	}

	@Override
	public void onTick() {
		LocalPlayer player = mc.player;
		if (fireworkCooldown > 0)
			fireworkCooldown--;
		if (swapCooldown > 0)
			swapCooldown--;
		if (takeoffCooldown > 0)
			takeoffCooldown--;

		// Fliegt Baritone, lenkt und zündet es selbst. Eigene Eingriffe würden dagegen arbeiten.
		if (BaritoneBridge.isElytraActive()) {
			if (player.isFallFlying())
				airborneForBaritone = true;
			else if (easyStart.isChecked() && !airborneForBaritone)
				takeOffForBaritone(player);
			return;
		}
		airborneForBaritone = false;

		if (autoSwap.isChecked())
			swapWornOutElytra(player);

		boolean jumpDown = mc.options.keyJump.isDown();
		if (player.isFallFlying()) {
			if (stopInWater.isChecked() && player.isInWater()) {
				// Derselbe Befehl wie beim Öffnen: der Server schließt die Elytra, wenn sie schon offen ist
				sendStartFallFlying(player);
			} else {
				braking = crashGuard.isChecked() && brakeBeforeWall(player);
				// Beim Bremsen keine Rakete zünden, sonst gibt es wieder Tempo
				if (autoFirework.isChecked() && !braking)
					useFireworkIfSlow(player);
			}
		} else if (easyStart.isChecked() && jumpDown && !player.onGround() && player.getDeltaMovement().y < 0
			&& !player.isInWater() && player.tryToStartFallFlying()) {
			sendStartFallFlying(player);
		}
	}

	/**
	 * Liegt eine Wand auf der Flugbahn und ist man zu schnell für einen schadlosen Aufprall, wird
	 * die Nase hochgenommen: Das wandelt waagerechtes Tempo in Höhe um, wie beim Bremsen von Hand.
	 */
	private boolean brakeBeforeWall(LocalPlayer player) {
		Vec3 motion = player.getDeltaMovement();
		double horizontal = motion.horizontalDistance();
		if (horizontal <= SAFE_IMPACT_SPEED)
			return false;

		Vec3 start = player.position().add(0, 0.4, 0);
		Vec3 end = start.add(motion.x * LOOKAHEAD_TICKS, motion.y * LOOKAHEAD_TICKS, motion.z * LOOKAHEAD_TICKS);
		HitResult hit = mc.level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		if (hit.getType() == HitResult.Type.MISS)
			return false;

		// Wie stark hochziehen hängt davon ab, wie nah die Wand ist
		double ticksToWall = hit.getLocation().distanceTo(start) / Math.max(motion.length(), 0.01);
		float pitch = ticksToWall < 5 ? -60 : -35;
		player.setXRot(motion.y > MAX_CLIMB ? 0 : pitch);
		return true;
	}

	/**
	 * Macht alles fertig, was Baritone vor dem Elytra-Flug verlangt: Elytra anziehen, genug Raketen
	 * ohne Sterne, Raketen in der Hotbar. Schaltet ElytraFly ein, damit es abhebt, und den Autopilot
	 * aus, damit er nicht gegenlenkt.
	 * @return Grund, warum es nicht geht, oder null
	 */
	public String prepareForBaritone() {
		LocalPlayer player = mc.player;
		if (player == null)
			return "Erst eine Welt betreten.";
		if (!InvUtil.canClick())
			return "Erst die Truhe oder das Menü schließen.";

		// Mit weniger Haltbarkeit oder Raketen weigert sich Baritone abzuheben ("Not taking off ...")
		int minDurability = BaritoneBridge.getIntSetting("elytraMinimumDurability").orElse(5);
		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
		if (!chest.is(Items.ELYTRA) || InvUtil.durabilityLeft(chest) < minDurability) {
			int spare = InvUtil.findAnywhere(stack -> stack.is(Items.ELYTRA) && InvUtil.durabilityLeft(stack) >= minDurability);
			if (spare < 0)
				return "Keine Elytra im Inventar, die noch mindestens " + minDurability + " Haltbarkeit hat.";
			InvUtil.swap(InvUtil.toMenuSlot(spare), InvUtil.armorMenuSlot(EquipmentSlot.CHEST));
			ChatOutput.info("Elytra angezogen.");
		}

		int minFireworks = BaritoneBridge.getIntSetting("elytraMinFireworksBeforeLanding").orElse(5);
		int fireworks = 0;
		for (int i = 0; i < 36; i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (isPlainFirework(stack))
				fireworks += stack.getCount();
		}
		if (fireworks <= minFireworks)
			return "Zu wenig Raketen: " + fireworks + ". Baritone braucht mehr als " + minFireworks
				+ ", und zwar ohne Feuerwerkssterne.";
		if (InvUtil.findHotbar(ElytraFly::isPlainFirework) < 0) {
			int hotbarSlot = InvUtil.findHotbar(ItemStack::isEmpty);
			InvUtil.swap(InvUtil.toMenuSlot(InvUtil.findAnywhere(ElytraFly::isPlainFirework)),
				InvUtil.toMenuSlot(hotbarSlot < 0 ? 8 : hotbarSlot));
		}

		if (Modules.AUTOPILOT.isEnabled()) {
			Modules.AUTOPILOT.setEnabled(false);
			ChatOutput.info("Autopilot ausgeschaltet, Baritone lenkt.");
		}
		if (!isEnabled()) {
			setEnabled(true);
			ChatOutput.info("ElytraFly eingeschaltet, es hebt für Baritone ab.");
		}
		if (!easyStart.isChecked())
			ChatOutput.info("\"Einfacher Start\" ist aus: Spring selbst ab und öffne die Elytra, dann übernimmt Baritone.");
		return null;
	}

	/** Baritone zündet nur Raketen ohne Sterne, die anderen explodieren und machen Schaden. */
	private static boolean isPlainFirework(ItemStack stack) {
		if (!stack.is(Items.FIREWORK_ROCKET))
			return false;
		Fireworks fireworks = stack.get(DataComponents.FIREWORKS);
		return fireworks == null || fireworks.explosions().isEmpty();
	}

	/**
	 * Baritone wartet am Boden, bis man gleitet: hochspringen und im Fallen die Elytra öffnen.
	 * Die erste Rakete zündet dann Baritone, weil man zu langsam ist.
	 */
	private void takeOffForBaritone(LocalPlayer player) {
		// Mit elytraAutoJump sucht Baritone sich selbst eine Absprungstelle, dann nicht dazwischenfunken
		if (player.isInWater() || player.isInLava() || BaritoneBridge.getBooleanSetting("elytraAutoJump").orElse(false))
			return;
		if (player.onGround()) {
			if (takeoffCooldown == 0) {
				player.jumpFromGround();
				takeoffCooldown = TAKEOFF_RETRY;
			}
		} else if (player.getDeltaMovement().y < 0 && player.tryToStartFallFlying()) {
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
