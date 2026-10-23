package dev.cheecky.modules;

import java.util.ArrayList;
import java.util.List;

import dev.cheecky.command.ChatOutput;
import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.CheckboxSetting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Zielt mit der Enderperle auf eine festgelegte Stelle. Berechnet die Flugbahn mit der Vanilla-Physik
 * (Startgeschwindigkeit 1,5, Schwerkraft 0,03, Luftwiderstand 0,99 pro Tick) und zeigt sie an.
 */
public class PearlAim extends Module {
	private static final double SPEED = 1.5;
	private static final double GRAVITY = 0.03;
	private static final double DRAG = 0.99;
	private static final int MAX_TICKS = 400;
	/** So nah muss die vorhergesagte Landung am Ziel sein, damit die Bahn als frei gilt. */
	private static final double TOLERANCE = 1.5;

	private final CheckboxSetting highArc = add(new CheckboxSetting("Hoher Bogen",
		"Wirft im hohen Bogen über Hindernisse. Aus: flacher Wurf, landet schneller. Ist ein Bogen blockiert, wird der andere genommen.", false));
	private final CheckboxSetting autoThrow = add(new CheckboxSetting("Automatisch werfen",
		"Wirft sofort, sobald das Ziel erreichbar ist, und vergisst das Ziel danach.", false));

	private Vec3 target;
	private boolean warnedUnreachable;

	public PearlAim() {
		super("PearlAim", "Zielt mit der Enderperle genau auf eine Stelle, die du mit .pearl festlegst. Zielt nur, solange du eine Perle in der Hand hältst.",
			Category.PLAYER, 0);
	}

	public void setTarget(Vec3 target) {
		this.target = target;
		warnedUnreachable = false;
	}

	public Vec3 getTarget() {
		return target;
	}

	@Override
	public void onTick() {
		if (target == null)
			return;
		Gizmos.cuboid(BlockPos.containing(target.subtract(0, 0.5, 0)), GizmoStyle.stroke(0xFFFF55FF, 2)).setAlwaysOnTop();

		LocalPlayer player = mc.player;
		InteractionHand hand = pearlHand(player);
		if (hand == null || mc.gui.screen() != null)
			return;

		Vec3 start = player.getEyePosition().subtract(0, 0.1, 0);
		float yaw = (float) Math.toDegrees(Math.atan2(target.z - start.z, target.x - start.x)) - 90;
		// Nur Würfe, deren Bahn wirklich am Ziel endet. Der hohe Bogen hat die kleinste Neigung (nach oben),
		// der flache die größte; ist der bevorzugte blockiert, bleibt der andere übrig.
		Throw best = null;
		for (float pitch : solvePitches(start)) {
			Throw candidate = simulate(start, yaw, pitch);
			if (candidate.landing.distanceTo(target) > TOLERANCE)
				continue;
			if (best == null || (highArc.isChecked() ? pitch < best.pitch : pitch > best.pitch))
				best = candidate;
		}

		if (best == null) {
			if (!warnedUnreachable)
				ChatOutput.error("PearlAim: Ziel nicht erreichbar, zu weit weg oder beide Bögen blockiert.");
			warnedUnreachable = true;
			return;
		}
		warnedUnreachable = false;

		player.setYRot(yaw);
		player.setXRot(best.pitch);
		for (int i = 1; i < best.path.size(); i++)
			Gizmos.line(best.path.get(i - 1), best.path.get(i), 0xFFFF55FF, 1.5F);

		if (autoThrow.isChecked() && mc.gameMode != null) {
			mc.gameMode.useItem(player, hand);
			ChatOutput.info("Perle geworfen, Landung bei " + BlockPos.containing(best.landing).toShortString() + ".");
			target = null;
		}
	}

	private InteractionHand pearlHand(LocalPlayer player) {
		if (player.getMainHandItem().is(Items.ENDER_PEARL))
			return InteractionHand.MAIN_HAND;
		if (player.getOffhandItem().is(Items.ENDER_PEARL))
			return InteractionHand.OFF_HAND;
		return null;
	}

	/** Alle Neigungen, bei denen die Perle (ohne Hindernisse) in Zielhöhe ankommt: meist ein flacher und ein hoher Bogen. */
	private List<Float> solvePitches(Vec3 start) {
		double distance = Math.hypot(target.x - start.x, target.z - start.z);
		double height = target.y - start.y;
		List<Float> roots = new ArrayList<>();
		double previous = Double.NaN;
		for (float pitch = -89.5F; pitch <= 89.5F; pitch += 0.5F) {
			double error = heightError(pitch, distance, height);
			if (!Double.isNaN(previous) && !Double.isNaN(error) && Math.signum(error) != Math.signum(previous))
				roots.add(refine(pitch - 0.5F, pitch, distance, height));
			previous = error;
		}
		return roots;
	}

	/** Bisektion zwischen zwei Neigungen mit unterschiedlichem Vorzeichen. */
	private float refine(float low, float high, double distance, double height) {
		double lowError = heightError(low, distance, height);
		for (int i = 0; i < 20; i++) {
			float mid = (low + high) / 2;
			double midError = heightError(mid, distance, height);
			if (Math.signum(midError) == Math.signum(lowError)) {
				low = mid;
				lowError = midError;
			} else
				high = mid;
		}
		return (low + high) / 2;
	}

	/** Höhe der Perle über/unter dem Ziel, wenn sie die waagerechte Entfernung erreicht; NaN, wenn sie nie so weit kommt. */
	private static double heightError(float pitch, double distance, double height) {
		double radians = Math.toRadians(pitch);
		double vh = Math.cos(radians) * SPEED;
		double vy = -Math.sin(radians) * SPEED;
		double h = 0;
		double y = 0;
		for (int tick = 0; tick < MAX_TICKS; tick++) {
			vy = (vy - GRAVITY) * DRAG;
			vh *= DRAG;
			double nextH = h + vh;
			double nextY = y + vy;
			if (nextH >= distance) {
				double t = (distance - h) / vh;
				return y + (nextY - y) * t - height;
			}
			h = nextH;
			y = nextY;
		}
		return Double.NaN;
	}

	private record Throw(float pitch, Vec3 landing, List<Vec3> path) {}

	/** Flug in 3D mit Blockkollision, so wie der Server ihn rechnet (ohne die winzige Zufallsstreuung). */
	private Throw simulate(Vec3 start, float yaw, float pitch) {
		float yawRad = yaw * Mth.DEG_TO_RAD;
		float pitchRad = pitch * Mth.DEG_TO_RAD;
		Vec3 velocity = new Vec3(-Mth.sin(yawRad) * Mth.cos(pitchRad), -Mth.sin(pitchRad), Mth.cos(yawRad) * Mth.cos(pitchRad))
			.scale(SPEED);
		LocalPlayer player = mc.player;
		Vec3 movement = player.getKnownMovement();
		velocity = velocity.add(movement.x, player.onGround() ? 0 : movement.y, movement.z);

		List<Vec3> path = new ArrayList<>();
		Vec3 pos = start;
		path.add(pos);
		for (int tick = 0; tick < MAX_TICKS; tick++) {
			velocity = new Vec3(velocity.x, velocity.y - GRAVITY, velocity.z).scale(DRAG);
			Vec3 next = pos.add(velocity);
			BlockHitResult hit = mc.level.clip(new ClipContext(pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
			if (hit.getType() != HitResult.Type.MISS) {
				path.add(hit.getLocation());
				return new Throw(pitch, hit.getLocation(), path);
			}
			pos = next;
			path.add(pos);
			if (pos.y < mc.level.getMinY())
				break;
		}
		return new Throw(pitch, pos, path);
	}
}
