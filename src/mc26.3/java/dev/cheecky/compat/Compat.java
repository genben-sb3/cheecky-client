package dev.cheecky.compat;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPunchPacket;
import net.minecraft.world.InteractionHand;

/** Was sich zwischen den Minecraft-Versionen unterscheidet. Diese Datei gilt für 26.3. */
public final class Compat {
	/** Tastatur-Typ der Tastencodes: ab 26.3 SDL-Scancodes. */
	public static final InputConstants.Type KEYBOARD = InputConstants.Type.KEYBOARD;

	private Compat() {}

	/** Arm schwingen nach einem Angriff, so wie es Vanilla bei einem Linksklick tut. */
	public static void swingAfterAttack(LocalPlayer player) {
		player.swing(InteractionHand.MAIN_HAND, player.getMainHandItem().getAttackAnimation(), false);
		player.connection.send(ServerboundPunchPacket.INSTANCE);
	}
}
