package dev.cheecky.modules;

import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class NoFall extends Module {
	public NoFall() {
		super("NoFall", "Verhindert Fallschaden, indem dem Server gemeldet wird, dass du auf dem Boden stehst.",
			Category.MOVEMENT, 0);
	}

	@Override
	public void onTick() {
		LocalPlayer player = mc.player;
		if (player.isCreative() || player.isSpectator() || player.isFallFlying())
			return;

		if (player.fallDistance > 2)
			player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(true, player.horizontalCollision));
	}
}
