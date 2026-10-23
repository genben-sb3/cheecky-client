package dev.cheecky.module;

public enum Category {
	COMBAT("Combat"),
	MOVEMENT("Movement"),
	RENDER("Render"),
	WORLD("World"),
	PLAYER("Player");

	public final String displayName;

	Category(String displayName) {
		this.displayName = displayName;
	}
}
