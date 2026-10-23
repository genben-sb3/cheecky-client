package dev.cheecky.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** Auswahl aus festen Werten, z. B. die Modi von Speed. */
public class ModeSetting<E extends Enum<E>> extends Setting {
	private final E[] values;
	private E selected;

	public ModeSetting(String name, String description, E selected) {
		super(name, description);
		this.values = selected.getDeclaringClass().getEnumConstants();
		this.selected = selected;
	}

	public E getSelected() {
		return selected;
	}

	public void setSelected(E selected) {
		this.selected = selected;
	}

	/** Zum nächsten (oder bei negativem step zum vorigen) Wert springen. */
	public void cycle(int step) {
		int index = Math.floorMod(selected.ordinal() + step, values.length);
		selected = values[index];
	}

	public E[] getValues() {
		return values;
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(selected.name());
	}

	@Override
	public void fromJson(JsonElement json) {
		if (!json.isJsonPrimitive())
			return;
		for (E value : values)
			if (value.name().equals(json.getAsString()))
				selected = value;
	}
}
