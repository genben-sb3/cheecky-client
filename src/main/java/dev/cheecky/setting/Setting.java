package dev.cheecky.setting;

import com.google.gson.JsonElement;

public abstract class Setting {
	public final String name;
	public final String description;

	protected Setting(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public abstract JsonElement toJson();

	public abstract void fromJson(JsonElement json);
}
