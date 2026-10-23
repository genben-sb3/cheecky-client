package dev.cheecky.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.cheecky.Cheecky;
import dev.cheecky.gui.ClickGui;
import dev.cheecky.gui.Window;
import dev.cheecky.module.Module;
import dev.cheecky.module.Modules;
import dev.cheecky.setting.Setting;
import dev.cheecky.util.CustomBlocks;
import dev.cheecky.util.Friends;
import dev.cheecky.util.Homes;
import net.fabricmc.loader.api.FabricLoader;

/** Speichert Module, Tasten, Einstellungen, Fensterpositionen, Freunde, Homes und die eigene Blockliste in config/cheecky.json. */
public final class Config {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("cheecky.json");

	private Config() {}

	public static void load() {
		if (!Files.exists(FILE))
			return;

		try {
			JsonObject root = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
			loadModules(root.getAsJsonObject("modules"));
			loadWindows(root.getAsJsonObject("windows"));
			loadList(root.getAsJsonArray("friends"), Friends::add);
			loadList(root.getAsJsonArray("finderBlocks"),
				name -> CustomBlocks.parse(name).ifPresent(CustomBlocks::add));
			loadHomes(root.getAsJsonObject("homes"));
		} catch (IOException | RuntimeException e) {
			Cheecky.LOGGER.error("Konnte {} nicht laden, nutze Standardwerte", FILE, e);
		}
	}

	private static void loadModules(JsonObject modules) {
		if (modules == null)
			return;

		for (Module module : Modules.ALL) {
			JsonObject json = modules.getAsJsonObject(module.name);
			if (json == null)
				continue;

			if (json.has("key"))
				module.setKey(json.get("key").getAsInt());

			JsonObject settings = json.getAsJsonObject("settings");
			if (settings != null)
				for (Setting setting : module.getSettings()) {
					JsonElement value = settings.get(setting.name);
					if (value != null)
						setting.fromJson(value);
				}

			// Erst nach den Einstellungen aktivieren, damit onEnable die geladenen Werte sieht
			if (json.has("enabled") && module.rememberEnabled())
				module.setEnabled(json.get("enabled").getAsBoolean());
		}
	}

	private static void loadList(JsonArray array, Consumer<String> add) {
		if (array != null)
			for (JsonElement element : array)
				add.accept(element.getAsString());
	}

	private static void loadHomes(JsonObject homes) {
		if (homes == null)
			return;
		for (String name : homes.keySet()) {
			JsonObject home = homes.getAsJsonObject(name);
			Homes.put(name, new Homes.Home(home.get("x").getAsInt(), home.get("y").getAsInt(), home.get("z").getAsInt(),
				home.get("nether").getAsBoolean()));
		}
	}

	private static void loadWindows(JsonObject windows) {
		if (windows == null)
			return;

		for (Window window : ClickGui.INSTANCE.getCategoryWindows()) {
			JsonObject json = windows.getAsJsonObject(window.title);
			if (json == null)
				continue;
			window.x = json.get("x").getAsInt();
			window.y = json.get("y").getAsInt();
			window.minimized = json.get("minimized").getAsBoolean();
		}
	}

	public static void save() {
		JsonObject modules = new JsonObject();
		for (Module module : Modules.ALL) {
			JsonObject json = new JsonObject();
			json.addProperty("enabled", module.isEnabled());
			json.addProperty("key", module.getKey());
			JsonObject settings = new JsonObject();
			for (Setting setting : module.getSettings())
				settings.add(setting.name, setting.toJson());
			json.add("settings", settings);
			modules.add(module.name, json);
		}

		JsonObject windows = new JsonObject();
		for (Window window : ClickGui.INSTANCE.getCategoryWindows()) {
			JsonObject json = new JsonObject();
			json.addProperty("x", window.x);
			json.addProperty("y", window.y);
			json.addProperty("minimized", window.minimized);
			windows.add(window.title, json);
		}

		JsonObject root = new JsonObject();
		root.add("modules", modules);
		root.add("windows", windows);
		JsonArray friends = new JsonArray();
		Friends.all().forEach(friends::add);
		root.add("friends", friends);
		JsonArray finderBlocks = new JsonArray();
		CustomBlocks.get().forEach(block -> finderBlocks.add(CustomBlocks.name(block)));
		root.add("finderBlocks", finderBlocks);
		JsonObject homes = new JsonObject();
		Homes.all().forEach((name, home) -> {
			JsonObject json = new JsonObject();
			json.addProperty("x", home.x());
			json.addProperty("y", home.y());
			json.addProperty("z", home.z());
			json.addProperty("nether", home.nether());
			homes.add(name, json);
		});
		root.add("homes", homes);

		try {
			Files.writeString(FILE, GSON.toJson(root));
		} catch (IOException e) {
			Cheecky.LOGGER.error("Konnte {} nicht speichern", FILE, e);
		}
	}
}
