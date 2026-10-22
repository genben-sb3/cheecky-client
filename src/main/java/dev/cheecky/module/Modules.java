package dev.cheecky.module;

import java.util.List;

import dev.cheecky.modules.AntiKnockback;
import dev.cheecky.modules.AutoSprint;
import dev.cheecky.modules.BunnyHop;
import dev.cheecky.modules.Flight;
import dev.cheecky.modules.Fullbright;
import dev.cheecky.modules.KillAura;
import dev.cheecky.modules.NoFall;
import dev.cheecky.modules.XRay;
import net.minecraft.client.Minecraft;

/** Alle Module als statische Felder, damit Mixins direkt darauf zugreifen können. */
public final class Modules {
	public static final AntiKnockback ANTI_KNOCKBACK = new AntiKnockback();
	public static final AutoSprint AUTO_SPRINT = new AutoSprint();
	public static final BunnyHop BUNNY_HOP = new BunnyHop();
	public static final Flight FLIGHT = new Flight();
	public static final Fullbright FULLBRIGHT = new Fullbright();
	public static final KillAura KILL_AURA = new KillAura();
	public static final NoFall NO_FALL = new NoFall();
	public static final XRay XRAY = new XRay();

	public static final List<Module> ALL = List.of(
		ANTI_KNOCKBACK, AUTO_SPRINT, BUNNY_HOP, FLIGHT, FULLBRIGHT, KILL_AURA, NO_FALL, XRAY);

	private Modules() {}

	public static List<Module> inCategory(Category category) {
		return ALL.stream().filter(m -> m.category == category).toList();
	}

	public static void onTick(Minecraft mc) {
		if (mc.player != null && mc.level != null)
			for (Module module : ALL)
				if (module.isEnabled())
					module.onTick();

		Fullbright.updateGamma();
	}

	/** Wird vom Tastatur-Mixin aufgerufen, wenn im Spiel (ohne offenes Menü) eine Taste gedrückt wird. */
	public static void onKeyPressed(int key) {
		for (Module module : ALL)
			if (module.getKey() != 0 && module.getKey() == key)
				module.toggle();
	}
}
