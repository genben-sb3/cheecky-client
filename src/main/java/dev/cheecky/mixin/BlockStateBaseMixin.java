package dev.cheecky.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.cheecky.module.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** X-Ray (Schattierung) und Jesus (Wasser und Lava werden fest). */
@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockStateBaseMixin {
	/** X-Ray: keine Schattierung, damit Erze im Dunkeln gut sichtbar sind. */
	@Inject(method = "getShadeBrightness(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F",
		at = @At("RETURN"), cancellable = true)
	private void cheecky$fullShade(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
		if (!isInGame())
			return;
		if (Modules.XRAY.isEnabled())
			cir.setReturnValue(1F);
	}

	/** Jesus: Flüssigkeiten bekommen für den eigenen Spieler eine volle Block-Kollision. */
	@Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
		at = @At("HEAD"), cancellable = true)
	private void cheecky$solidFluids(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
		if (((BlockBehaviour.BlockStateBase) (Object) this).getFluidState().isEmpty() || !isInGame()
			|| !Modules.JESUS.shouldBeSolid())
			return;
		if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() == Minecraft.getInstance().player)
			cir.setReturnValue(Shapes.block());
	}

	/**
	 * Minecraft fragt Kollisionsformen schon beim Hochfahren ab, bevor es das Spiel überhaupt gibt.
	 * Vorher darf Modules nicht angefasst werden, sonst stürzt der Start ab.
	 */
	private static boolean isInGame() {
		Minecraft mc = Minecraft.getInstance();
		return mc != null && mc.player != null;
	}
}
