package dev.cheecky.compat;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;

/** Was sich zwischen den Minecraft-Versionen unterscheidet. Diese Datei gilt für 26.2. */
public final class Compat {
	/** Tastatur-Typ der Tastencodes: bis 26.2 GLFW-Keycodes. */
	public static final InputConstants.Type KEYBOARD = InputConstants.Type.KEYSYM;

	private Compat() {}

	/** Arm schwingen nach einem Angriff, so wie es Vanilla bei einem Linksklick tut. */
	public static void swingAfterAttack(LocalPlayer player) {
		player.swing(InteractionHand.MAIN_HAND);
	}

	/** Arm schwingen nach dem Benutzen eines Items auf einem Block. */
	public static void swingHand(LocalPlayer player) {
		player.swing(InteractionHand.MAIN_HAND);
	}
}
