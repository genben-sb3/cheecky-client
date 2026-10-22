package dev.cheecky.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class CheckboxSetting extends Setting {
	private boolean checked;

	public CheckboxSetting(String name, String description, boolean checked) {
		super(name, description);
		this.checked = checked;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(checked);
	}

	@Override
	public void fromJson(JsonElement json) {
		if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isBoolean())
			checked = json.getAsBoolean();
	}
}
