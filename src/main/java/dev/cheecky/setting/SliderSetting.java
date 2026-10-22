package dev.cheecky.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class SliderSetting extends Setting {
	public final double min;
	public final double max;
	public final double step;
	private final String unit;
	private double value;

	public SliderSetting(String name, String description, double value, double min, double max, double step, String unit) {
		super(name, description);
		this.min = min;
		this.max = max;
		this.step = step;
		this.unit = unit;
		setValue(value);
	}

	public double getValue() {
		return value;
	}

	public float getValueF() {
		return (float) value;
	}

	public void setValue(double value) {
		double snapped = Math.round((value - min) / step) * step + min;
		this.value = Math.clamp(snapped, min, max);
	}

	/** Position des Reglers zwischen 0 und 1. */
	public double getPercent() {
		return (value - min) / (max - min);
	}

	public void setPercent(double percent) {
		setValue(min + Math.clamp(percent, 0, 1) * (max - min));
	}

	public String getValueString() {
		boolean whole = step >= 1;
		String number = whole ? Long.toString(Math.round(value)) : String.format("%.2f", value);
		return number + unit;
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(value);
	}

	@Override
	public void fromJson(JsonElement json) {
		if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber())
			setValue(json.getAsDouble());
	}
}
