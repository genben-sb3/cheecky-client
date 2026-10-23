package dev.cheecky.compat;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPunchPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.SwingAnimation;

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

	/** Arm schwingen nach dem Benutzen eines Items auf einem Block. */
	public static void swingHand(LocalPlayer player) {
		swingHand(player, InteractionHand.MAIN_HAND);
	}

	public static void swingHand(LocalPlayer player, InteractionHand hand) {
		player.swing(hand, SwingAnimation.DEFAULT, false);
	}
}
