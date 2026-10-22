package dev.cheecky;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.blaze3d.platform.InputConstants;

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
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
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

		ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> {
			// Gamma zurücksetzen, damit options.txt nicht mit Gamma 16 gespeichert wird
			Fullbright.restoreGamma();
			Config.save();
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
