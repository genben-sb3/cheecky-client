package dev.cheecky.modules;

import java.util.HashSet;
import java.util.Set;

import dev.cheecky.command.ChatOutput;
import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.CheckboxSetting;
import dev.cheecky.util.Friends;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Markiert Endermen, Wither und auf Wunsch Spieler durch Wände und meldet neue im Chat.
 * Der Server schickt Wesen nur in einem begrenzten Umkreis, weiter weg sieht auch das Radar nichts.
 */
public class EntityRadar extends Module {
	private static final int ENDERMAN_COLOR = 0xFFC04CFF;
	private static final int WITHER_COLOR = 0xFFFF3030;
	private static final int PLAYER_COLOR = 0xFFFFD700;

	private final CheckboxSetting endermen = add(new CheckboxSetting("Endermen", "Markiert Endermen, z. B. zum Perlen-Farmen.", true));
	private final CheckboxSetting withers = add(new CheckboxSetting("Wither", "Markiert Wither und warnt im Chat.", true));
	private final CheckboxSetting players = add(new CheckboxSetting("Spieler", "Markiert andere Spieler (Freunde nicht).", false));
	private final CheckboxSetting tracers = add(new CheckboxSetting("Linien", "Zeichnet eine Linie von dir zu jedem markierten Wesen.", true));
	private final CheckboxSetting chat = add(new CheckboxSetting("Chat-Meldung", "Meldet neue Wesen mit Koordinaten und Entfernung.", true));

	/** IDs der Wesen, die schon gemeldet wurden und noch da sind. */
	private Set<Integer> known = new HashSet<>();

	public EntityRadar() {
		super("EntityRadar", "Zeigt Endermen, Wither und Spieler durch Wände und meldet neue im Chat.", Category.RENDER, 0);
	}

	@Override
	protected void onEnable() {
		known.clear();
	}

	@Override
	public void onTick() {
		Vec3 start = mc.player.getEyePosition().add(mc.player.getLookAngle());
		Set<Integer> present = new HashSet<>();
		for (Entity entity : mc.level.entitiesForRendering()) {
			int color = colorFor(entity);
			if (color == 0 || !entity.isAlive())
				continue;

			present.add(entity.getId());
			if (chat.isChecked() && !known.contains(entity.getId()))
				ChatOutput.info(nameFor(entity) + " bei " + entity.blockPosition().toShortString()
					+ " (" + Math.round(mc.player.distanceTo(entity)) + " m)");

			Gizmos.cuboid(entity.getBoundingBox(), GizmoStyle.stroke(color, 2)).setAlwaysOnTop();
			if (tracers.isChecked())
				Gizmos.line(start, entity.getBoundingBox().getCenter(), color, 1.5F).setAlwaysOnTop();
		}
		known = present;
	}

	/** @return Farbe, oder 0, wenn das Wesen nicht markiert werden soll */
	private int colorFor(Entity entity) {
		if (entity == mc.player)
			return 0;
		if (entity.getType() == EntityTypes.ENDERMAN)
			return endermen.isChecked() ? ENDERMAN_COLOR : 0;
		if (entity.getType() == EntityTypes.WITHER)
			return withers.isChecked() ? WITHER_COLOR : 0;
		if (entity instanceof Player player)
			return players.isChecked() && !player.isSpectator() && !Friends.isFriend(player) ? PLAYER_COLOR : 0;
		return 0;
	}

	private static String nameFor(Entity entity) {
		if (entity.getType() == EntityTypes.ENDERMAN)
			return "Enderman";
		if (entity.getType() == EntityTypes.WITHER)
			return "§cWither§r";
		return "Spieler " + entity.getName().getString();
	}
}
