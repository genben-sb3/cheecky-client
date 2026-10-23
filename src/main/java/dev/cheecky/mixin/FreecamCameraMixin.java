package dev.cheecky.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.cheecky.module.Modules;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.phys.Vec3;

/** Freecam: Kameraposition und -drehung nach dem normalen Ausrichten überschreiben. */
@Mixin(Camera.class)
public abstract class FreecamCameraMixin {
	@Shadow
	private boolean detached;

	@Shadow
	protected abstract void setPosition(Vec3 pos);

	@Shadow
	protected abstract void setRotation(float yaw, float pitch);

	@Inject(method = "update(Lnet/minecraft/client/DeltaTracker;)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;alignWithEntity(F)V", shift = At.Shift.AFTER))
	private void cheecky$freecam(DeltaTracker deltaTracker, CallbackInfo ci) {
		if (!Modules.FREECAM.isActive())
			return;
		// detached = true sorgt dafür, dass der eigene Körper gezeichnet wird
		detached = true;
		setPosition(Modules.FREECAM.getCamPos(deltaTracker.getGameTimeDeltaPartialTick(true)));
		setRotation(Modules.FREECAM.getCamYaw(), Modules.FREECAM.getCamPitch());
	}
}
