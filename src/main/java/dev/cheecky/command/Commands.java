package dev.cheecky.command;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import com.mojang.blaze3d.platform.InputConstants;

import dev.cheecky.config.Config;
import dev.cheecky.module.Module;
import dev.cheecky.module.Modules;
import dev.cheecky.setting.CheckboxSetting;
import dev.cheecky.setting.ModeSetting;
import dev.cheecky.setting.Setting;
import dev.cheecky.setting.SliderSetting;

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
			return key.getType() == InputConstants.Type.KEYBOARD ? key.getValue() : 0;
		} catch (RuntimeException e) {
			return 0;
		}
	}

	private static String keyDisplayName(int key) {
		return InputConstants.Type.KEYBOARD.getOrCreate(key).getDisplayName().getString();
	}
}
