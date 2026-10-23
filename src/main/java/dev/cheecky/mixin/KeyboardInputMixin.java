package dev.cheecky.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.cheecky.module.Modules;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;

/** Freecam: der Körper bekommt keine Tasteneingaben, er bleibt stehen. */
@Mixin(KeyboardInput.class)
public class KeyboardInputMixin extends ClientInput {
	@Inject(method = "tick()V", at = @At("RETURN"))
	private void cheecky$freezeBody(CallbackInfo ci) {
		if (!Modules.FREECAM.isActive())
			return;
		keyPresses = Input.EMPTY;
		moveVector = Vec2.ZERO;
	}
}
