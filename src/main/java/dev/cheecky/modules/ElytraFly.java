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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
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

	private final CheckboxSetting crashGuard = add(new CheckboxSetting("Crash-Schutz",
		"Erkennt Wände voraus und bremst rechtzeitig, indem du die Nase hochnimmst. Vanilla-Schaden entsteht nur, wenn beim Aufprall mehr als 0,3 Blöcke/Tick Tempo verloren gehen.", true));

	/** Unter diesem waagerechten Tempo macht ein Aufprall keinen Schaden: Schaden = Tempoverlust × 10 − 3. */
	private static final double SAFE_IMPACT_SPEED = 0.3;
	/** So viele Ticks voraus wird nach Wänden gesucht. */
	private static final int LOOKAHEAD_TICKS = 15;
	/** Steigt man schneller, wird nicht weiter hochgezogen (6b6t erlaubt höchstens 3,0 Blöcke/Tick nach oben). */
	private static final double MAX_CLIMB = 2.0;

	private int fireworkCooldown;
	private boolean braking;
	private int swapCooldown;

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
