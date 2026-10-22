package dev.cheecky.module;

public enum Category {
	COMBAT("Combat"),
	MOVEMENT("Movement"),
	RENDER("Render");

	public final String displayName;

	Category(String displayName) {
		this.displayName = displayName;
	}
}
