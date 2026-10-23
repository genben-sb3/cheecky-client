package dev.cheecky.modules;

import com.mojang.blaze3d.platform.InputConstants;

import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.SliderSetting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Die Kamera löst sich vom Körper. Der Körper bleibt stehen; Schlagen, Abbauen und Benutzen
 * sind gesperrt, damit man nichts neben dem Körper trifft. Siehe Freecam*-Mixins.
 */
public class Freecam extends Module {
	private final SliderSetting speed = add(new SliderSetting("Tempo",
		"Fluggeschwindigkeit der Kamera in Blöcken pro Tick.", 1, 0.1, 5, 0.1, ""));

	private Vec3 camPos;
	private Vec3 prevCamPos;
	private float camYaw;
	private float camPitch;

	public Freecam() {
		super("Freecam", "Löst die Kamera vom Körper, damit du dich umsehen kannst. Der Körper bleibt stehen.",
			Category.MOVEMENT, InputConstants.KEY_V);
	}

	@Override
	protected void onEnable() {
		if (mc.player == null) {
			setEnabled(false);
			return;
		}
		camPos = mc.player.getEyePosition();
		prevCamPos = camPos;
		camYaw = mc.player.getYRot();
		camPitch = mc.player.getXRot();
	}

	@Override
	public void onTick() {
		prevCamPos = camPos;
		if (mc.gui.screen() != null)
			return;

		// Tasten direkt abfragen: die Eingaben des Körpers sind während Freecam auf null gesetzt
		double forward = axis(mc.options.keyUp.isDown(), mc.options.keyDown.isDown());
		double strafe = axis(mc.options.keyLeft.isDown(), mc.options.keyRight.isDown());
		double vertical = axis(mc.options.keyJump.isDown(), mc.options.keyShift.isDown());

		float yaw = camYaw * Mth.DEG_TO_RAD;
		double sin = Mth.sin(yaw);
		double cos = Mth.cos(yaw);
		Vec3 horizontal = new Vec3(strafe * cos - forward * sin, 0, strafe * sin + forward * cos);
		if (horizontal.lengthSqr() > 1)
			horizontal = horizontal.normalize();

		camPos = camPos.add(horizontal.add(0, vertical, 0).scale(speed.getValue()));
	}

	private static double axis(boolean positive, boolean negative) {
		return (positive ? 1 : 0) - (negative ? 1 : 0);
	}

	public Vec3 getCamPos(float partialTicks) {
		return prevCamPos.lerp(camPos, partialTicks);
	}

	public float getCamYaw() {
		return camYaw;
	}

	public float getCamPitch() {
		return camPitch;
	}

	/** Mausbewegung dreht die Kamera statt den Körper. Faktor wie in LocalPlayer.turn. */
	public void turn(double deltaYaw, double deltaPitch) {
		camYaw += (float) (deltaYaw * 0.15);
		camPitch = Math.clamp(camPitch + (float) (deltaPitch * 0.15), -90, 90);
	}

	/** Für Mixins: Körper stillhalten und Interaktion sperren. */
	public boolean isActive() {
		LocalPlayer player = mc.player;
		return isEnabled() && player != null && camPos != null;
	}
}
