package dev.cheecky.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.OptionInstance;

@Mixin(OptionInstance.class)
public interface OptionInstanceAccessor<T> {
	/** Setzt den Wert ohne Gültigkeitsprüfung, damit Gamma über 1.0 gehen kann. */
	@Accessor("value")
	void cheecky$setValue(T value);
}
