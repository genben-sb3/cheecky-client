package dev.cheecky.module;

import java.util.ArrayList;
import java.util.List;

import dev.cheecky.command.ChatOutput;
import dev.cheecky.setting.Setting;
import net.minecraft.client.Minecraft;

public abstract class Module {
	protected static final Minecraft mc = Minecraft.getInstance();

	public final String name;
	public final String description;
	public final Category category;
	private final List<Setting> settings = new ArrayList<>();
	private boolean enabled;
	/** Scancode der Taste, 0 = keine Taste. */
	private int key;

	protected Module(String name, String description, Category category, int defaultKey) {
		this.name = name;
		this.description = description;
		this.category = category;
		this.key = defaultKey;
	}

	protected <T extends Setting> T add(T setting) {
		settings.add(setting);
		return setting;
	}

	public List<Setting> getSettings() {
		return settings;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		if (this.enabled == enabled)
			return;
		if (enabled && !isAvailable()) {
			ChatOutput.error(name + ": " + getUnavailableReason());
			return;
		}

		this.enabled = enabled;
		if (enabled)
			onEnable();
		else
			onDisable();
	}

	public void toggle() {
		setEnabled(!enabled);
	}

	/** false, wenn eine Voraussetzung fehlt (z. B. ein anderer Mod). Das Modul lässt sich dann nicht einschalten. */
	public boolean isAvailable() {
		return true;
	}

	public String getUnavailableReason() {
		return "";
	}

	/** false für Module, die nach einem Neustart nicht von selbst wieder an sein sollen. */
	public boolean rememberEnabled() {
		return true;
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	protected void onEnable() {}

	protected void onDisable() {}

	/** Wird jeden Client-Tick aufgerufen, solange das Modul an ist und eine Welt geladen ist. */
	public void onTick() {}
}
