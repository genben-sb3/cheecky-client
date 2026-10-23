package dev.cheecky.modules;

import dev.cheecky.module.Category;
import dev.cheecky.module.Module;

/** Die eigentliche Arbeit machen LocalPlayerMixin und EntityMixin. */
public class NoSlow extends Module {
	public NoSlow() {
		super("NoSlow", "Kein Abbremsen beim Essen, Bogenspannen, Blocken und in Spinnweben, Seelensand oder Honig.",
			Category.MOVEMENT, 0);
	}
}
