package dev.cheecky.modules;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import dev.cheecky.command.ChatOutput;
import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.CheckboxSetting;
import dev.cheecky.setting.ModeSetting;
import dev.cheecky.setting.SliderSetting;
import dev.cheecky.util.BlockTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;

/**
 * Durchsucht die geladenen Chunks nach Blöcken (z. B. Holz) und markiert sie mit Box und Linie.
 * Pro Tick werden nur ein paar Chunks durchsucht, damit das Spiel nicht ruckelt.
 */
public class BlockFinder extends Module {
	private static final int CHUNKS_PER_TICK = 8;
	private static final int COLOR = 0xFF00E5FF;

	private final ModeSetting<BlockTarget> target = add(new ModeSetting<>("Ziel",
		"Wonach gesucht wird. Eigene Liste: Blöcke mit .finder add <block> eintragen.", BlockTarget.WOOD));
	private final SliderSetting maxShown = add(new SliderSetting("Max. Anzahl",
		"So viele der nächsten Funde werden markiert.", 50, 5, 300, 5, ""));
	private final CheckboxSetting tracers = add(new CheckboxSetting("Linien",
		"Zeichnet eine Linie von dir zu jedem markierten Fund.", true));
	private final CheckboxSetting chat = add(new CheckboxSetting("Chat-Meldung",
		"Schreibt Koordinaten in den Chat, sobald etwas gefunden wird.", true));

	/** Funde pro Chunk (ChunkPos als long). */
	private final Map<Long, List<BlockPos>> found = new HashMap<>();
	private final Deque<ChunkPos> queue = new ArrayDeque<>();
	private BlockTarget scanning;
	private Set<Block> blocks = Set.of();
	private boolean reported;

	public BlockFinder() {
		super("BlockFinder", "Findet Holz, Erze oder eigene Blöcke in den geladenen Chunks und markiert sie. .finder goto lässt Baritone hinlaufen.",
			Category.RENDER, 0);
	}

	@Override
	protected void onEnable() {
		reset();
	}

	@Override
	protected void onDisable() {
		found.clear();
		queue.clear();
	}

	/** Suche neu starten, z. B. nachdem die eigene Liste geändert wurde. */
	public void reset() {
		found.clear();
		queue.clear();
		scanning = null;
		reported = false;
	}

	@Override
	public void onTick() {
		if (target.getSelected() != scanning) {
			reset();
			scanning = target.getSelected();
			blocks = scanning.blocks();
		}

		if (queue.isEmpty())
			startPass();
		Predicate<BlockState> wanted = state -> blocks.contains(state.getBlock());
		for (int i = 0; i < CHUNKS_PER_TICK && !queue.isEmpty(); i++)
			scanChunk(queue.poll(), wanted);

		List<BlockPos> nearest = nearest((int) maxShown.getValue());
		if (!nearest.isEmpty() && !reported && chat.isChecked()) {
			BlockPos pos = nearest.getFirst();
			ChatOutput.info(scanning + " gefunden bei " + pos.toShortString() + " (" + distance(pos) + " m). "
				+ ".finder goto läuft mit Baritone hin.");
		}
		reported = !nearest.isEmpty();
		draw(nearest);
	}

	/** Alle Chunks im Sichtradius einreihen, nahe zuerst, und Funde außer Reichweite vergessen. */
	private void startPass() {
		ChunkPos center = mc.player.chunkPosition();
		int radius = mc.options.getEffectiveRenderDistance();
		found.keySet().removeIf(key -> {
			ChunkPos pos = ChunkPos.unpack(key);
			return Math.max(Math.abs(pos.x() - center.x()), Math.abs(pos.z() - center.z())) > radius;
		});

		List<ChunkPos> chunks = new ArrayList<>();
		for (int x = -radius; x <= radius; x++)
			for (int z = -radius; z <= radius; z++)
				chunks.add(new ChunkPos(center.x() + x, center.z() + z));
		chunks.sort(Comparator.comparingInt(pos -> Math.max(Math.abs(pos.x() - center.x()), Math.abs(pos.z() - center.z()))));
		queue.addAll(chunks);
	}

	private void scanChunk(ChunkPos pos, Predicate<BlockState> wanted) {
		LevelChunk chunk = mc.level.getChunkSource().getChunk(pos.x(), pos.z(), ChunkStatus.FULL, false);
		if (chunk == null) {
			found.remove(pos.pack());
			return;
		}

		List<BlockPos> hits = new ArrayList<>();
		LevelChunkSection[] sections = chunk.getSections();
		for (int i = 0; i < sections.length; i++) {
			LevelChunkSection section = sections[i];
			// Schneller Vorab-Test über die Palette: die meisten Abschnitte fallen hier schon raus
			if (section.hasOnlyAir() || !section.maybeHas(wanted))
				continue;

			int baseY = chunk.getSectionYFromSectionIndex(i) << 4;
			for (int y = 0; y < 16; y++)
				for (int z = 0; z < 16; z++)
					for (int x = 0; x < 16; x++)
						if (wanted.test(section.getBlockState(x, y, z)))
							hits.add(new BlockPos(pos.getMinBlockX() + x, baseY + y, pos.getMinBlockZ() + z));
		}

		if (hits.isEmpty())
			found.remove(pos.pack());
		else
			found.put(pos.pack(), hits);
	}

	public List<BlockPos> nearest(int limit) {
		BlockPos player = mc.player.blockPosition();
		return found.values().stream()
			.flatMap(List::stream)
			.sorted(Comparator.comparingDouble(pos -> pos.distSqr(player)))
			.limit(limit)
			.toList();
	}

	public BlockTarget getTarget() {
		return target.getSelected();
	}

	public void setTarget(BlockTarget value) {
		target.setSelected(value);
	}

	private int distance(BlockPos pos) {
		return (int) Math.round(Math.sqrt(pos.distSqr(mc.player.blockPosition())));
	}

	/** Gizmos aus dem Client-Tick landen in Minecrafts Pro-Tick-Sammlung und werden bis zum nächsten Tick gezeichnet. */
	private void draw(List<BlockPos> positions) {
		Vec3 start = mc.player.getEyePosition().add(mc.player.getLookAngle());
		for (BlockPos pos : positions) {
			Gizmos.cuboid(pos, GizmoStyle.stroke(COLOR, 2)).setAlwaysOnTop();
			if (tracers.isChecked())
				Gizmos.line(start, Vec3.atCenterOf(pos), COLOR, 1.5F).setAlwaysOnTop();
		}
	}
}
