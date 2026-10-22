package dev.cheecky.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.cheecky.module.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	/** AntiKnockback: Rückstoß durch Treffer kommt als neue Geschwindigkeit vom Server. */
	@WrapOperation(method = "handleSetEntityMotion",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;lerpMotion(Lnet/minecraft/world/phys/Vec3;)V"))
	private void cheecky$onSetEntityMotion(Entity entity, Vec3 movement, Operation<Void> original) {
		if (entity == Minecraft.getInstance().player && Modules.ANTI_KNOCKBACK.isEnabled())
			movement = Modules.ANTI_KNOCKBACK.modifyVelocity(entity.getDeltaMovement(), movement);
		original.call(entity, movement);
	}

	/** AntiKnockback: Explosionen schicken einen eigenen Stoß mit. */
	@ModifyExpressionValue(method = "handleExplosion",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundExplodePacket;playerKnockback()Ljava/util/Optional;"))
	private Optional<Vec3> cheecky$onExplosionKnockback(Optional<Vec3> knockback) {
		if (!Modules.ANTI_KNOCKBACK.isEnabled())
			return knockback;
		return knockback.map(Modules.ANTI_KNOCKBACK::modifyPush);
	}
}
