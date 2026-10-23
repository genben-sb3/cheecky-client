package dev.cheecky.modules;

import dev.cheecky.command.ChatOutput;
import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.module.Modules;
import dev.cheecky.setting.CheckboxSetting;
import dev.cheecky.setting.ModeSetting;
import dev.cheecky.setting.SliderSetting;
import dev.cheecky.util.InvUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Hält auf dem Highway die Richtung: zu Fuß mit Sprint oder im Elytra-Flug (Raketen über ElytraFly).
 * Bleibt mit echter Vanilla-Bewegung und hält an, bevor es gefährlich wird.
 */
public class Autopilot extends Module {
	public enum Mode {
		WALK("Laufen"), ELYTRA("Elytra");

		private final String displayName;

		Mode(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String toString() {
			return displayName;
		}
	}

	private final ModeSetting<Mode> mode = add(new ModeSetting<>("Modus",
		"Laufen: sprintet geradeaus. Elytra: hält Richtung und Höhe, Raketen zündet ElytraFly.", Mode.WALK));
	private final CheckboxSetting snap = add(new CheckboxSetting("Einrasten",
		"Rastet die Richtung beim Einschalten auf 45° ein, also genau auf die Highway-Achsen und Diagonalen.", true));
	private final SliderSetting pitch = add(new SliderSetting("Neigung",
		"Blickneigung im Elytra-Flug. 0 = waagerecht, negativ = leicht nach oben.", -2, -20, 20, 1, "°"));
	private final SliderSetting witherDistance = add(new SliderSetting("Wither-Stopp",
		"Hält an, wenn ein Wither näher ist. 0 = aus.", 64, 0, 128, 8, " Blöcke"));
	private final CheckboxSetting obstacleStop = add(new CheckboxSetting("Hindernis-Stopp",
		"Hält vor Wänden und Löchern an (zu Fuß) bzw. warnt vor Hindernissen voraus (im Flug).", true));
	private final SliderSetting minFood = add(new SliderSetting("Hunger-Stopp",
		"Hält an, wenn der Hunger so tief fällt (20 = voll).", 6, 0, 20, 1, ""));
	private final SliderSetting minElytra = add(new SliderSetting("Elytra-Stopp",
		"Hält an, wenn die Elytra nur noch so viel Haltbarkeit hat und keine Ersatz-Elytra da ist.", 20, 0, 100, 5, ""));

	private float heading;

	public Autopilot() {
		super("Autopilot", "Hält auf dem Highway die Richtung, zu Fuß oder mit Elytra, und stoppt bei Withern, Hindernissen, Hunger oder kaputter Elytra.",
			Category.MOVEMENT, 0);
	}

	@Override
	public boolean rememberEnabled() {
		return false;
	}

	@Override
	protected void onEnable() {
		if (mc.player == null)
			return;
		float yaw = Mth.wrapDegrees(mc.player.getYRot());
		heading = snap.isChecked() ? Math.round(yaw / 45) * 45F : yaw;
		if (mode.getSelected() == Mode.ELYTRA && !Modules.ELYTRA_FLY.isEnabled()) {
			Modules.ELYTRA_FLY.setEnabled(true);
			ChatOutput.info("ElytraFly eingeschaltet, es zündet die Raketen.");
		}
		ChatOutput.info("Autopilot: Richtung " + directionName(heading) + ".");
	}

	@Override
	protected void onDisable() {
		mc.options.keyUp.setDown(false);
		mc.options.keySprint.setDown(false);
	}

	@Override
	public void onTick() {
		LocalPlayer player = mc.player;
		String danger = findDanger(player);
		if (danger != null) {
			ChatOutput.error("Autopilot gestoppt: " + danger);
			setEnabled(false);
			return;
		}

		player.setYRot(heading);
		if (mode.getSelected() == Mode.ELYTRA) {
			if (player.isFallFlying())
				player.setXRot(pitch.getValueF());
			return;
		}

		mc.options.keyUp.setDown(true);
		mc.options.keySprint.setDown(true);
	}

	/** @return Grund zum Anhalten oder null */
	private String findDanger(LocalPlayer player) {
		if (player.getFoodData().getFoodLevel() <= minFood.getValue())
			return "Hunger, erst essen.";

		double witherRange = witherDistance.getValue();
		if (witherRange > 0)
			for (Entity entity : mc.level.entitiesForRendering())
				if (entity.getType() == EntityTypes.WITHER && entity.isAlive() && player.distanceTo(entity) < witherRange)
					return "Wither bei " + entity.blockPosition().toShortString() + " (" + Math.round(player.distanceTo(entity)) + " m).";

		if (mode.getSelected() == Mode.ELYTRA) {
			ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
			if (player.isFallFlying() && chest.isDamageableItem()
				&& chest.getMaxDamage() - chest.getDamageValue() <= minElytra.getValue()
				&& InvUtil.findAnywhere(s -> s.is(Items.ELYTRA) && InvUtil.durabilityLeft(s) > minElytra.getValue()) < 0)
				return "Elytra fast kaputt und keine Ersatz-Elytra im Inventar.";
			if (obstacleStop.isChecked() && player.isFallFlying() && blockedAhead(player, player.getDeltaMovement().scale(20)))
				return "Hindernis direkt voraus, übernimm selbst.";
			return null;
		}

		if (obstacleStop.isChecked() && player.onGround()) {
			Vec3 ahead = player.position().add(Vec3.directionFromRotation(0, heading).scale(0.9));
			BlockPos front = BlockPos.containing(ahead);
			boolean wall = !mc.level.getBlockState(front).getCollisionShape(mc.level, front).isEmpty()
				|| !mc.level.getBlockState(front.above()).getCollisionShape(mc.level, front.above()).isEmpty();
			boolean hole = mc.level.getBlockState(front.below()).getCollisionShape(mc.level, front.below()).isEmpty()
				&& mc.level.getBlockState(front.below(2)).getCollisionShape(mc.level, front.below(2)).isEmpty();
			if (wall)
				return "Wand voraus bei " + front.toShortString() + ".";
			if (hole)
				return "Loch voraus bei " + front.toShortString() + ".";
		}
		return null;
	}

	/** Trifft die Flugbahn in der nächsten Sekunde (grob: aktuelle Geschwindigkeit × 20) einen Block? */
	private boolean blockedAhead(LocalPlayer player, Vec3 offset) {
		Vec3 start = player.position();
		return mc.level.clip(new ClipContext(start, start.add(offset), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player))
			.getType() != HitResult.Type.MISS;
	}

	/** Himmelsrichtung wie im F3-Menü: Yaw 0 = Süden (+Z), 90 = Westen (−X), 180 = Norden, −90 = Osten. */
	public static String directionName(float yaw) {
		String[] names = {"Süden (+Z)", "Südwesten (−X +Z)", "Westen (−X)", "Nordwesten (−X −Z)",
			"Norden (−Z)", "Nordosten (+X −Z)", "Osten (+X)", "Südosten (+X +Z)"};
		int index = Math.floorMod(Math.round(Mth.wrapDegrees(yaw) / 45), 8);
		return names[index];
	}
}
