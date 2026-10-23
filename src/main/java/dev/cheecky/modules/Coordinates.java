package dev.cheecky.modules;

import dev.cheecky.module.Category;
import dev.cheecky.module.Module;

/** Zeigt im HUD unten rechts die Position samt umgerechneter Nether- bzw. Oberwelt-Koordinate. Zeichnen: {@link dev.cheecky.gui.Hud}. */
public class Coordinates extends Module {
	public Coordinates() {
		super("Koordinaten", "Zeigt unten rechts deine Position und die passende Nether- bzw. Oberwelt-Koordinate (× bzw. ÷ 8) und die Blickrichtung.",
			Category.RENDER, 0);
		// Standardmäßig an; danach gilt, was in der Config steht
		setEnabled(true);
	}
}
