package dev.cheecky.module;

import java.util.List;

import dev.cheecky.modules.AntiKnockback;
import dev.cheecky.modules.AutoAnchor;
import dev.cheecky.modules.AutoArmor;
import dev.cheecky.modules.AutoCrystal;
import dev.cheecky.modules.AutoMine;
import dev.cheecky.modules.AutoSprint;
import dev.cheecky.modules.AutoTool;
import dev.cheecky.modules.AutoTotem;
import dev.cheecky.modules.BlockFinder;
import dev.cheecky.modules.BunnyHop;
import dev.cheecky.modules.Criticals;
import dev.cheecky.modules.ElytraFly;
import dev.cheecky.modules.FastBreak;
import dev.cheecky.modules.Flight;
import dev.cheecky.modules.Freecam;
import dev.cheecky.modules.Fullbright;
import dev.cheecky.modules.Jesus;
import dev.cheecky.modules.KillAura;
import dev.cheecky.modules.NoFall;
import dev.cheecky.modules.NoSlow;
import dev.cheecky.modules.Speed;
import dev.cheecky.modules.Spider;
import dev.cheecky.modules.Step;
import dev.cheecky.modules.TriggerBot;
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
	public static final AutoAnchor AUTO_ANCHOR = new AutoAnchor();
	public static final AutoArmor AUTO_ARMOR = new AutoArmor();
	public static final AutoCrystal AUTO_CRYSTAL = new AutoCrystal();
	public static final BlockFinder BLOCK_FINDER = new BlockFinder();
	public static final AutoMine AUTO_MINE = new AutoMine();
	public static final AutoTool AUTO_TOOL = new AutoTool();
	public static final AutoTotem AUTO_TOTEM = new AutoTotem();
	public static final Criticals CRITICALS = new Criticals();
	public static final ElytraFly ELYTRA_FLY = new ElytraFly();
	public static final FastBreak FAST_BREAK = new FastBreak();
	public static final Freecam FREECAM = new Freecam();
	public static final Jesus JESUS = new Jesus();
	public static final NoSlow NO_SLOW = new NoSlow();
	public static final Speed SPEED = new Speed();
	public static final Spider SPIDER = new Spider();
	public static final Step STEP = new Step();
	public static final TriggerBot TRIGGER_BOT = new TriggerBot();

	/** Alphabetisch, so erscheinen sie in ClickGUI und Hack-Liste. */
	public static final List<Module> ALL = List.of(
		ANTI_KNOCKBACK, AUTO_ANCHOR, AUTO_ARMOR, AUTO_CRYSTAL, AUTO_MINE, AUTO_SPRINT, AUTO_TOOL, AUTO_TOTEM, BLOCK_FINDER,
		BUNNY_HOP, CRITICALS,
		ELYTRA_FLY, FAST_BREAK, FLIGHT, FREECAM, FULLBRIGHT, JESUS, KILL_AURA, NO_FALL, NO_SLOW, SPEED, SPIDER,
		STEP, TRIGGER_BOT, XRAY);

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
