package dev.cheecky.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.cheecky.module.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/** X-Ray: Truhen, Schilder usw. ausblenden. */
@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
	@Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
		at = @At("HEAD"), cancellable = true)
	private <S extends BlockEntityRenderState> void cheecky$hideBlockEntity(S state, PoseStack poseStack,
		SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
		if (!Modules.XRAY.isEnabled())
			return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null && !Modules.XRAY.isVisible(mc.level.getBlockState(state.blockPos).getBlock()))
			ci.cancel();
	}
}
