package dev.cheecky.command;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.mojang.blaze3d.platform.InputConstants;

import dev.cheecky.Cheecky;
import dev.cheecky.compat.Compat;
import dev.cheecky.config.Config;
import dev.cheecky.gui.BaritoneScreen;
import dev.cheecky.module.Module;
import dev.cheecky.module.Modules;
import dev.cheecky.setting.CheckboxSetting;
import dev.cheecky.setting.ModeSetting;
import dev.cheecky.setting.Setting;
import dev.cheecky.modules.BlockFinder;
import dev.cheecky.setting.SliderSetting;
import dev.cheecky.util.BaritoneBridge;
import dev.cheecky.util.BlockTarget;
import dev.cheecky.util.CustomBlocks;
import dev.cheecky.util.Friends;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Chat-Befehle, die mit "." beginnen. Sie werden lokal ausgeführt und nie an den Server geschickt. */
public final class Commands {
	public static final String PREFIX = ".";

	private Commands() {}

	/** @return true, wenn die Nachricht ein Chat-Befehl war und nicht gesendet werden darf */
	public static boolean handle(String message) {
		if (!message.startsWith(PREFIX))
			return false;

		String[] args = message.substring(PREFIX.length()).trim().split("\\s+");
		String name = args[0].toLowerCase(Locale.ROOT);
		String[] rest = Arrays.copyOfRange(args, 1, args.length);

		switch (name) {
			case "help", "" -> help();
			case "list" -> list();
			case "toggle", "t" -> toggle(rest);
			case "bind" -> bind(rest);
			case "binds" -> binds();
			case "settings" -> settings(rest);
			case "set" -> set(rest);
			case "baritone", "b" -> openBaritone();
			case "friend", "f" -> friend(rest);
			case "finder" -> finder(rest);
			default -> ChatOutput.error("Unbekannter Befehl \"" + name + "\". Alle Befehle: .help");
		}
		return true;
	}

	private static void help() {
		ChatOutput.info(".list – alle Module mit Status");
		ChatOutput.info(".toggle <modul> – Modul an/aus (kurz: .t)");
		ChatOutput.info(".bind <modul> <taste|none> – Taste belegen");
		ChatOutput.info(".binds – alle belegten Tasten");
		ChatOutput.info(".settings <modul> – Einstellungen anzeigen");
		ChatOutput.info(".set <modul> <einstellung> <wert> – Einstellung ändern");
		ChatOutput.info(".baritone – Baritone-Menü öffnen (kurz: .b)");
		ChatOutput.info(".friend add|remove <name> / .friend list – Freunde, die nie angegriffen werden (kurz: .f)");
		ChatOutput.info(".finder – BlockFinder-Funde, .finder add|remove <block>, .finder list, .finder goto");
	}

	private static void friend(String[] args) {
		if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
			ChatOutput.info(Friends.all().isEmpty() ? "Keine Freunde eingetragen." : "Freunde: " + String.join(", ", Friends.all()));
			return;
		}
		if (args.length < 2) {
			ChatOutput.error("Benutzung: .friend add|remove <name>");
			return;
		}
		String name = args[1];
		switch (args[0].toLowerCase(Locale.ROOT)) {
			case "add" -> ChatOutput.info(Friends.add(name) ? name + " ist jetzt Freund und wird nicht angegriffen."
				: name + " ist schon Freund.");
			case "remove", "del" -> ChatOutput.info(Friends.remove(name) ? name + " ist kein Freund mehr."
				: name + " war kein Freund.");
			default -> {
				ChatOutput.error("Benutzung: .friend add|remove <name>");
				return;
			}
		}
		Config.save();
	}

	private static void finder(String[] args) {
		BlockFinder finder = Modules.BLOCK_FINDER;
		String action = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
		switch (action) {
			case "add", "remove", "del" -> {
				if (args.length < 2) {
					ChatOutput.error("Benutzung: .finder " + action + " <block>, z. B. .finder add ancient_debris");
					return;
				}
				Block block = CustomBlocks.parse(args[1]).orElse(null);
				if (block == null || block == Blocks.AIR) {
					ChatOutput.error("Unbekannter Block \"" + args[1] + "\".");
					return;
				}
				boolean changed = action.equals("add") ? CustomBlocks.add(block) : CustomBlocks.remove(block);
				String name = CustomBlocks.name(block);
				if (!changed)
					ChatOutput.info(name + (action.equals("add") ? " ist schon in der Liste." : " war nicht in der Liste."));
				else {
					ChatOutput.info(name + (action.equals("add") ? " hinzugefügt." : " entfernt.") + " BlockFinder sucht jetzt die eigene Liste.");
					finder.setTarget(BlockTarget.CUSTOM);
					finder.reset();
					Config.save();
				}
			}
			case "list" -> ChatOutput.info(CustomBlocks.get().isEmpty() ? "Die eigene Liste ist leer."
				: "Eigene Liste: " + CustomBlocks.get().stream().map(CustomBlocks::name).collect(Collectors.joining(", ")));
			case "goto" -> {
				List<BlockPos> nearest = finderResults(finder, 1);
				if (nearest == null)
					return;
				if (!BaritoneBridge.isAvailable()) {
					ChatOutput.error("Dafür muss Baritone installiert sein. Ziel: " + nearest.getFirst().toShortString());
					return;
				}
				BlockPos pos = nearest.getFirst();
				// Nur in die Nähe, sonst würde Baritone den Block abbauen, um darin zu stehen
				if (BaritoneBridge.gotoNear(pos, 2))
					ChatOutput.info("Baritone läuft zu " + pos.toShortString() + ". Stoppen mit #stop oder im Baritone-Menü.");
				else
					ChatOutput.error("Baritone konnte nicht losgeschickt werden, Details im Log.");
			}
			case "" -> {
				List<BlockPos> nearest = finderResults(finder, 5);
				if (nearest == null)
					return;
				ChatOutput.info(finder.getTarget() + ", die nächsten Funde:");
				BlockPos player = Minecraft.getInstance().player.blockPosition();
				for (BlockPos pos : nearest)
					ChatOutput.info(pos.toShortString() + " (" + Math.round(Math.sqrt(pos.distSqr(player))) + " m)");
			}
			default -> ChatOutput.error("Benutzung: .finder, .finder add|remove <block>, .finder list, .finder goto");
		}
	}

	/** @return die nächsten Funde, oder null (mit Fehlermeldung), wenn es keine gibt */
	private static List<BlockPos> finderResults(BlockFinder finder, int limit) {
		if (!finder.isEnabled()) {
			ChatOutput.error("BlockFinder ist aus. Einschalten mit .t blockfinder");
			return null;
		}
		List<BlockPos> nearest = finder.nearest(limit);
		if (nearest.isEmpty()) {
			ChatOutput.info("Noch nichts gefunden (" + finder.getTarget() + ").");
			return null;
		}
		return nearest;
	}

	private static void openBaritone() {
		// Der Chat schließt sich nach dem Absenden selbst und würde einen sofort geöffneten Bildschirm mitschließen
		Cheecky.openNextTick(new BaritoneScreen(null));
	}

	private static void list() {
		String text = Modules.ALL.stream()
			.map(m -> (m.isEnabled() ? "§a" : "§7") + m.name)
			.collect(Collectors.joining("§r, "));
		ChatOutput.info(text);
	}

	private static void toggle(String[] args) {
		Module module = requireModule(args, 1, ".toggle <modul>");
		if (module == null)
			return;
		module.toggle();
		ChatOutput.info(module.name + (module.isEnabled() ? " §aan" : " §caus"));
		Config.save();
	}

	private static void bind(String[] args) {
		Module module = requireModule(args, 2, ".bind <modul> <taste|none>");
		if (module == null)
			return;

		String keyName = args[1].toLowerCase(Locale.ROOT);
		if (keyName.equals("none")) {
			module.setKey(0);
			ChatOutput.info(module.name + " hat jetzt keine Taste mehr.");
		} else {
			int key = parseKey(keyName);
			if (key == 0) {
				ChatOutput.error("Unbekannte Taste \"" + args[1] + "\". Beispiele: r, x, f6, right.shift");
				return;
			}
			module.setKey(key);
			ChatOutput.info(module.name + " liegt jetzt auf " + keyDisplayName(key) + ".");
		}
		Config.save();
	}

	private static void binds() {
		boolean any = false;
		for (Module module : Modules.ALL)
			if (module.getKey() != 0) {
				ChatOutput.info(module.name + ": " + keyDisplayName(module.getKey()));
				any = true;
			}
		if (!any)
			ChatOutput.info("Keine Tasten belegt.");
	}

	private static void settings(String[] args) {
		Module module = requireModule(args, 1, ".settings <modul>");
		if (module == null)
			return;
		if (module.getSettings().isEmpty()) {
			ChatOutput.info(module.name + " hat keine Einstellungen.");
			return;
		}
		for (Setting setting : module.getSettings())
			ChatOutput.info(setting.name + " = " + valueString(setting));
	}

	private static void set(String[] args) {
		Module module = requireModule(args, 3, ".set <modul> <einstellung> <wert>");
		if (module == null)
			return;

		// Einstellungsnamen dürfen Leerzeichen enthalten: alles zwischen Modul und Wert ist der Name
		String settingName = String.join(" ", Arrays.copyOfRange(args, 1, args.length - 1));
		String value = args[args.length - 1];
		Setting setting = module.getSettings().stream()
			.filter(s -> normalize(s.name).equals(normalize(settingName)))
			.findFirst().orElse(null);
		if (setting == null) {
			ChatOutput.error(module.name + " hat keine Einstellung \"" + settingName + "\". Siehe .settings " + args[0]);
			return;
		}

		if (!applyValue(setting, value)) {
			ChatOutput.error("Ungültiger Wert \"" + value + "\" für " + setting.name + ".");
			return;
		}
		ChatOutput.info(module.name + ": " + setting.name + " = " + valueString(setting));
		Config.save();
	}

	private static boolean applyValue(Setting setting, String value) {
		if (setting instanceof SliderSetting slider) {
			try {
				slider.setValue(Double.parseDouble(value.replace(',', '.')));
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}
		if (setting instanceof CheckboxSetting checkbox) {
			switch (value.toLowerCase(Locale.ROOT)) {
				case "on", "an", "true", "ja" -> checkbox.setChecked(true);
				case "off", "aus", "false", "nein" -> checkbox.setChecked(false);
				default -> {
					return false;
				}
			}
			return true;
		}
		if (setting instanceof ModeSetting<?> mode)
			return setMode(mode, value);
		return false;
	}

	private static <E extends Enum<E>> boolean setMode(ModeSetting<E> mode, String value) {
		for (E option : mode.getValues())
			if (normalize(option.toString()).equals(normalize(value)) || option.name().equalsIgnoreCase(value)) {
				mode.setSelected(option);
				return true;
			}
		return false;
	}

	private static String valueString(Setting setting) {
		if (setting instanceof SliderSetting slider)
			return slider.getValueString();
		if (setting instanceof CheckboxSetting checkbox)
			return checkbox.isChecked() ? "an" : "aus";
		if (setting instanceof ModeSetting<?> mode)
			return mode.getSelected().toString();
		return "?";
	}

	private static Module requireModule(String[] args, int minArgs, String usage) {
		if (args.length < minArgs) {
			ChatOutput.error("Benutzung: " + usage);
			return null;
		}
		Module module = findModule(args[0]);
		if (module == null)
			ChatOutput.error("Unbekanntes Modul \"" + args[0] + "\". Alle Module: .list");
		return module;
	}

	/** Groß-/Kleinschreibung, Leerzeichen und Bindestriche egal: "xray" findet "X-Ray". */
	private static Module findModule(String name) {
		String wanted = normalize(name);
		return Modules.ALL.stream().filter(m -> normalize(m.name).equals(wanted)).findFirst().orElse(null);
	}

	private static String normalize(String text) {
		return text.toLowerCase(Locale.ROOT).replace(" ", "").replace("-", "").replace("_", "");
	}

	private static int parseKey(String name) {
		try {
			InputConstants.Key key = InputConstants.getKey("key.keyboard." + name);
			return key.getType() == Compat.KEYBOARD ? key.getValue() : 0;
		} catch (RuntimeException e) {
			return 0;
		}
	}

	private static String keyDisplayName(int key) {
		return Compat.KEYBOARD.getOrCreate(key).getDisplayName().getString();
	}
}
