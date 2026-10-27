package dev.cheecky.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import dev.cheecky.command.ChatOutput;
import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.SliderSetting;
import dev.cheecky.util.Coords;
import dev.cheecky.util.Homes;
import dev.cheecky.util.InvUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Rechnet zu einem Ziel ({@code .route x z}, Oberwelt) aus, welcher Weg am schnellsten ist: zu Fuß
 * oder mit Elytra, direkt oder durch den Nether, ab hier oder über einen gemerkten /home. Die Zeiten
 * sind Schätzungen aus festen Durchschnittstempos. Zeichnen im HUD: {@link dev.cheecky.gui.Hud}.
 */
public class RoutePlanner extends Module {
	/** Sprinten auf ebenem Boden, Blöcke pro Sekunde. */
	private static final double WALK_SPEED = 5.6;
	private static final int REPLAN_INTERVAL = 20;
	private static final int ARRIVED_DISTANCE = 16;

	/** Ein Weg zum Ziel; {@code steps} sind die Schritte in Reihenfolge, der erste ist der nächste. */
	public record Route(String name, double seconds, List<Step> steps) {}

	/**
	 * Ein Schritt. Hat er ein Ziel ({@code hasTarget}), liegt es in den Koordinaten der Dimension,
	 * in der der Schritt stattfindet ({@code nether}).
	 */
	public record Step(String text, boolean hasTarget, boolean nether, int x, int z) {
		static Step action(String text) {
			return new Step(text, false, false, 0, 0);
		}
	}

	private final SliderSetting elytraSpeed = add(new SliderSetting("Elytra-Tempo",
		"Angenommenes Durchschnittstempo mit Elytra und Raketen.", 30, 10, 60, 1, " Blöcke/s"));
	private final SliderSetting portalTime = add(new SliderSetting("Portal-Zeit",
		"Wie lange es dauert, ein Netherportal zu bauen oder zu finden und durchzugehen.", 45, 10, 300, 5, " s"));
	private final SliderSetting homeTime = add(new SliderSetting("Home-Wartezeit",
		"Cooldown plus Wartezeit für /home. Auf 6b6t je nach Votes 15 s bis mehrere Minuten.", 60, 0, 900, 15, " s"));

	private int targetX, targetZ;
	private boolean hasTarget;
	private List<Route> routes = List.of();
	private int replanTimer;

	public RoutePlanner() {
		super("Route", "Zeigt den schnellsten Weg zum Ziel aus .route <x> <z>: zu Fuß oder Elytra, direkt, durch den Nether oder über einen /home.",
			Category.RENDER, 0);
	}

	@Override
	public boolean rememberEnabled() {
		return false;
	}

	public void setTarget(int overworldX, int overworldZ) {
		targetX = overworldX;
		targetZ = overworldZ;
		hasTarget = true;
		replanTimer = 0;
	}

	public boolean hasTarget() {
		return hasTarget;
	}

	public int getTargetX() {
		return targetX;
	}

	public int getTargetZ() {
		return targetZ;
	}

	/** Alle Wege, der schnellste zuerst. Leer, solange nichts geplant ist. */
	public List<Route> getRoutes() {
		return routes;
	}

	@Override
	protected void onEnable() {
		if (!hasTarget) {
			ChatOutput.error("Erst ein Ziel setzen: .route <x> <z> (Oberwelt-Koordinaten).");
			setEnabled(false);
			return;
		}
		routes = plan();
		ChatOutput.info("Route nach " + targetX + " " + targetZ + " (Oberwelt), Entfernung " + Math.round(overworldDistance()) + " Blöcke:");
		for (int i = 0; i < Math.min(4, routes.size()); i++) {
			Route route = routes.get(i);
			ChatOutput.info((i + 1) + ". " + route.name() + ": ca. " + formatTime(route.seconds()));
		}
		if (!routes.isEmpty())
			ChatOutput.info("Los geht's: " + routes.getFirst().steps().getFirst().text());
	}

	@Override
	public void onTick() {
		if (--replanTimer <= 0) {
			replanTimer = REPLAN_INTERVAL;
			routes = plan();
			if (!Coords.inNether() && overworldDistance() <= ARRIVED_DISTANCE) {
				ChatOutput.info("Route: angekommen.");
				setEnabled(false);
				return;
			}
		}

		// Linie in Richtung des nächsten Schritts, wenn er in dieser Dimension liegt
		Step step = currentStep();
		if (step != null && step.hasTarget() && step.nether() == Coords.inNether()) {
			Vec3 eye = mc.player.getEyePosition();
			Vec3 toward = new Vec3(step.x() + 0.5 - eye.x, 0, step.z() + 0.5 - eye.z);
			if (toward.length() > 1)
				Gizmos.line(eye.add(mc.player.getLookAngle()), eye.add(toward.normalize().scale(20)), 0xFF55FFFF, 2F).setAlwaysOnTop();
		}
	}

	/** Der nächste Schritt des schnellsten Wegs, oder null. Schon erreichte Schrittziele werden übersprungen. */
	public Step currentStep() {
		if (routes.isEmpty())
			return null;
		List<Step> steps = routes.getFirst().steps();
		for (Step step : steps)
			if (!(step.hasTarget() && step.nether() == Coords.inNether()
				&& Math.hypot(step.x() + 0.5 - mc.player.getX(), step.z() + 0.5 - mc.player.getZ()) < ARRIVED_DISTANCE / 2.0))
				return step;
		return steps.getLast();
	}

	/** Grad, um die man sich nach rechts (positiv) oder links (negativ) drehen muss, um zum Schrittziel zu schauen. */
	public float turnTo(Step step) {
		LocalPlayer player = mc.player;
		double dx = step.x() + 0.5 - player.getX();
		double dz = step.z() + 0.5 - player.getZ();
		float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90;
		return Mth.wrapDegrees(yaw - player.getYRot());
	}

	/** Entfernung zum Ziel in Oberwelt-Blöcken, vom aktuellen Standort aus. */
	public double overworldDistance() {
		double scale = Coords.inNether() ? Coords.NETHER_SCALE : 1;
		return Math.hypot(mc.player.getX() * scale - targetX, mc.player.getZ() * scale - targetZ);
	}

	private List<Route> plan() {
		LocalPlayer player = mc.player;
		boolean nether = Coords.inNether();
		double scale = nether ? Coords.NETHER_SCALE : 1;
		List<Route> all = new ArrayList<>(routesFrom(nether, player.getX() * scale, player.getZ() * scale, "", 0, List.of()));

		for (Map.Entry<String, Homes.Home> entry : Homes.all().entrySet()) {
			Homes.Home home = entry.getValue();
			Step teleport = Step.action("/home " + entry.getKey() + " eingeben");
			// Nach dem Teleport startet man im Nether oder in der Oberwelt, je nachdem, wo der Home liegt
			double wait = homeTime.getValue() + 5;
			all.addAll(routesFrom(home.nether(), home.overworldX(), home.overworldZ(), "/home " + entry.getKey() + ", dann ", wait,
				List.of(teleport)));
		}
		all.sort(Comparator.comparingDouble(Route::seconds));
		return all;
	}

	/**
	 * Alle Wege ab einem Startpunkt (Oberwelt-Koordinaten). {@code prefix}, {@code extraSeconds} und
	 * {@code firstSteps} kommen von einem vorgeschalteten /home.
	 */
	private List<Route> routesFrom(boolean startNether, double sx, double sz, String prefix, double extraSeconds, List<Step> firstSteps) {
		double distance = Math.hypot(sx - targetX, sz - targetZ);
		int netherX = Math.floorDiv(targetX, Coords.NETHER_SCALE);
		int netherZ = Math.floorDiv(targetZ, Coords.NETHER_SCALE);
		double portal = portalTime.getValue();
		boolean elytra = hasElytra();
		List<Route> result = new ArrayList<>();

		// Oberwelt: wer im Nether startet, muss erst hinaus
		double overworldExtra = extraSeconds + (startNether ? portal : 0);
		List<Step> overworldSteps = new ArrayList<>(firstSteps);
		if (startNether)
			overworldSteps.add(Step.action("Portal bauen oder suchen und in die Oberwelt gehen"));
		Step walkOver = new Step("Zu Fuß nach " + targetX + " " + targetZ, true, false, targetX, targetZ);
		result.add(route(prefix + "Oberwelt zu Fuß", overworldExtra + distance / WALK_SPEED, overworldSteps, walkOver));
		if (elytra) {
			Step flyOver = new Step("Elytra nach " + targetX + " " + targetZ, true, false, targetX, targetZ);
			result.add(route(prefix + "Oberwelt Elytra", overworldExtra + distance / elytraSpeed.getValue(), overworldSteps, flyOver));
		}

		// Nether: hinein (falls nötig), 1/8 der Strecke, am Ziel wieder hinaus
		double netherExtra = extraSeconds + (startNether ? 0 : portal) + portal;
		double netherDistance = distance / Coords.NETHER_SCALE;
		List<Step> netherSteps = new ArrayList<>(firstSteps);
		if (!startNether)
			netherSteps.add(Step.action("Portal bauen oder suchen und in den Nether gehen"));
		Step exit = Step.action("Bei Nether " + netherX + " " + netherZ + " ein Portal bauen und hinausgehen");
		Step walkNether = new Step("Im Nether zu Fuß nach " + netherX + " " + netherZ, true, true, netherX, netherZ);
		result.add(route(prefix + "Nether zu Fuß", netherExtra + netherDistance / WALK_SPEED, netherSteps, walkNether, exit));
		if (elytra) {
			Step flyNether = new Step("Im Nether mit Elytra nach " + netherX + " " + netherZ + " (Baritone-Menü: Elytra-Flug)",
				true, true, netherX, netherZ);
			result.add(route(prefix + "Nether Elytra", netherExtra + netherDistance / elytraSpeed.getValue(), netherSteps, flyNether, exit));
		}
		return result;
	}

	private static Route route(String name, double seconds, List<Step> before, Step... after) {
		List<Step> steps = new ArrayList<>(before);
		steps.addAll(List.of(after));
		return new Route(name, seconds, steps);
	}

	private boolean hasElytra() {
		return mc.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA) || InvUtil.findAnywhere(stack -> stack.is(Items.ELYTRA)) >= 0;
	}

	public static String formatTime(double seconds) {
		long total = Math.round(seconds);
		if (total < 60)
			return total + " s";
		if (total < 3600)
			return total / 60 + " min " + total % 60 + " s";
		return total / 3600 + " h " + total % 3600 / 60 + " min";
	}
}
