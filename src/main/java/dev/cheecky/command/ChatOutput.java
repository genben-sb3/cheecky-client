package dev.cheecky.command;

import dev.cheecky.Cheecky;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Nachrichten, die nur im eigenen Chat erscheinen und nie an den Server gehen. */
public final class ChatOutput {
	private ChatOutput() {}

	public static void info(String message) {
		send(Component.literal(message));
	}

	public static void error(String message) {
		send(Component.literal(message).withStyle(ChatFormatting.RED));
	}

	private static void send(Component message) {
		Component line = Component.literal("[" + Cheecky.NAME + "] ").withStyle(ChatFormatting.GREEN).append(message);
		Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(line);
	}
}
