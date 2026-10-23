package dev.cheecky.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;

@Mixin(MultiPlayerGameMode.class)
public interface MultiPlayerGameModeAccessor {
	/** FastBreak: Pause zwischen zwei abgebauten Blöcken. */
	@Accessor("destroyDelay")
	void cheecky$setDestroyDelay(int delay);
}
