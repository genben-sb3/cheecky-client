package dev.cheecky;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.blaze3d.platform.InputConstants;

import dev.cheecky.command.Commands;
import dev.cheecky.config.Config;
import dev.cheecky.gui.ClickGui;
import dev.cheecky.gui.ClickGuiScreen;
import dev.cheecky.gui.Hud;
import dev.cheecky.module.Modules;
import dev.cheecky.modules.Fullbright;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class Cheecky implements ClientModInitializer {
	public static final String MOD_ID = "cheecky";
	public static final String NAME = "Cheecky";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	public static KeyMapping clickGuiKey;

	@Override
	public void onInitializeClient() {
		KeyMapping.Category category = KeyMapping.Category.register(id("main"));
		clickGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.cheecky.clickgui", InputConstants.Type.KEYBOARD, InputConstants.KEY_RSHIFT, category));

		Config.load();

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (clickGuiKey.consumeClick())
				mc.gui.setScreen(new ClickGuiScreen(ClickGui.INSTANCE));
			Modules.onTick(mc);
		});

		HudElementRegistry.addLast(id("hud"), new Hud());

		// Chat-Befehle abfangen, bevor sie an den Server gehen
		ClientSendMessageEvents.ALLOW_CHAT.register(message -> !Commands.handle(message));

		ScreenEvents.AFTER_INIT.register((mc, screen, width, height) -> {
			if (screen instanceof PauseScreen)
				addPauseMenuButton(screen);
		});

		ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> {
			// Gamma zurücksetzen, damit options.txt nicht mit Gamma 16 gespeichert wird
			Fullbright.restoreGamma();
			Config.save();
		});
	}

	/**
	 * Fügt im Pausenmenü unter der Zeile "Optionen..." einen breiten "Cheecky"-Knopf ein
	 * und schiebt alles darunter (z. B. "Verlassen") eine Zeile nach unten.
	 */
	private static void addPauseMenuButton(Screen screen) {
		List<AbstractWidget> widgets = Screens.getWidgets(screen);
		String optionsLabel = I18n.get("menu.options");
		AbstractWidget options = widgets.stream()
			.filter(w -> w.getMessage().getString().equals(optionsLabel))
			.findFirst().orElse(null);
		// Das Pausenmenü ohne Optionen-Knopf (z. B. "Spiel pausiert" per F3+Esc) bekommt keinen Knopf
		if (options == null)
			return;

		int rowHeight = options.getHeight() + 4;
		int newY = options.getY() + rowHeight;
		for (AbstractWidget widget : widgets)
			if (widget.getY() >= newY)
				widget.setY(widget.getY() + rowHeight);

		widgets.add(Button.builder(Component.literal(NAME), b -> Minecraft.getInstance().gui.setScreen(new ClickGuiScreen(ClickGui.INSTANCE)))
			.bounds(options.getX(), newY, 204, options.getHeight())
			.build());
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
