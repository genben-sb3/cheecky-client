package dev.cheecky.modules;

import dev.cheecky.mixin.OptionInstanceAccessor;
import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.module.Modules;

public class Fullbright extends Module {
	private static final double BRIGHT_GAMMA = 16.0;
	/** Gamma-Wert des Spielers, bevor wir ihn überschrieben haben; null = nicht überschrieben. */
	private static Double savedGamma;

	public Fullbright() {
		super("Fullbright", "Macht alles taghell, auch Höhlen und die Nacht.", Category.RENDER, 0);
	}

	/**
	 * Fullbright und X-Ray brauchen beide volle Helligkeit. Wird jeden Tick aufgerufen und setzt
	 * Gamma auf 16, solange eins der beiden an ist, und danach wieder auf den alten Wert.
	 */
	public static void updateGamma() {
		boolean wanted = Modules.FULLBRIGHT.isEnabled() || Modules.XRAY.isEnabled();
		if (wanted) {
			if (savedGamma == null)
				savedGamma = mc.options.gamma().get();
			forceGamma(BRIGHT_GAMMA);
		} else {
			restoreGamma();
		}
	}

	public static void restoreGamma() {
		if (savedGamma == null)
			return;
		forceGamma(savedGamma);
		savedGamma = null;
	}

	// OptionInstance.set() lehnt Werte über 1.0 ab, deshalb setzen wir das Feld direkt.
	@SuppressWarnings("unchecked")
	private static void forceGamma(double value) {
		((OptionInstanceAccessor<Double>) (Object) mc.options.gamma()).cheecky$setValue(value);
	}
}
