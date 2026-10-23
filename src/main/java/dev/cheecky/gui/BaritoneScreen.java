package dev.cheecky.gui;

import java.util.List;
import java.util.Locale;

import dev.cheecky.module.Modules;
import dev.cheecky.util.BaritoneBridge;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * Knöpfe für die wichtigsten Baritone-Befehle, damit man sie nicht im Chat tippen muss.
 * Alles läuft über Baritones eigene Befehle, die Antworten erscheinen wie gewohnt im Chat.
 */
public class BaritoneScreen extends Screen {
	private static final int WIDTH = 300;
	private static final int ROW = 24;
	private static final int GAP = 4;

	/** Baritone-Einstellung, Anzeigename, Erklärung. */
	private record Toggle(String setting, String label, String description) {}

	private static final List<Toggle> TOGGLES = List.of(
		new Toggle("allowBreak", "Abbauen", "Baritone darf Blöcke abbauen, um durchzukommen."),
		new Toggle("allowPlace", "Platzieren", "Baritone darf Blöcke setzen, z. B. für Brücken und Türme."),
		new Toggle("allowParkour", "Parkour", "Baritone darf über Lücken springen."),
		new Toggle("allowSprint", "Sprinten", "Baritone sprintet beim Laufen."),
		new Toggle("allowInventory", "Inventar", "Baritone darf Werkzeuge und Blöcke aus dem Inventar in die Hotbar holen."),
		new Toggle("freeLook", "Freier Blick", "Du kannst dich umsehen, während Baritone läuft."));

	// Eingaben bleiben erhalten, wenn man den Bildschirm schließt und wieder öffnet
	private static String lastX = "", lastY = "", lastZ = "", lastPlayer = "", lastBlock = "";

	private final Screen parent;
	private EditBox x, y, z, player, block;
	private String message = "";

	public BaritoneScreen(Screen parent) {
		super(Component.literal("Baritone"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int left = width / 2 - WIDTH / 2;
		int top = Math.max(40, height / 2 - 110);
		boolean available = BaritoneBridge.isAvailable();
		// Beim Ändern der Fenstergröße wird init erneut aufgerufen, getippte Werte sollen bleiben
		if (x != null)
			remember();

		// Zeile 1: Koordinaten
		x = box(left, top, 60, "X", lastX);
		y = box(left + 64, top, 60, "Y (leer = egal)", lastY);
		z = box(left + 128, top, 60, "Z", lastZ);
		button("Hier", left + 192, top, 52, "Trägt deine aktuelle Position ein.", this::fillHere);
		button("Los", left + 248, top, 52, "Baritone läuft zu diesen Koordinaten.", this::gotoCoords)
			.active = available;

		// Zeile 2: Spieler folgen
		int row2 = top + ROW;
		player = box(left, row2, 196, "Spielername", lastPlayer);
		button("Folgen", left + 200, row2, 100, "Baritone läuft diesem Spieler hinterher.", () -> {
			String name = player.getValue().trim();
			if (name.isEmpty())
				message = "§cErst einen Spielernamen eintragen.";
			else
				run("follow player " + name);
		}).active = available;

		// Zeile 3: Block suchen oder abbauen
		int row3 = top + 2 * ROW;
		block = box(left, row3, 144, "Block, z. B. diamond_ore", lastBlock);
		button("Hinlaufen", left + 148, row3, 74, "Läuft zum nächsten Block dieser Art.", () -> runWithBlock("goto"))
			.active = available;
		button("Abbauen", left + 226, row3, 74, "Sucht und baut alle Blöcke dieser Art ab.", () -> runWithBlock("mine"))
			.active = available;

		// Zeile 4: Prozesse ohne Eingabe
		int row4 = top + 3 * ROW;
		int third = (WIDTH - 2 * GAP) / 3;
		button("Erkunden", left, row4, third, "Läuft in noch nicht besuchte Chunks.", () -> run("explore"))
			.active = available;
		button("Farmen", left + third + GAP, row4, third, "Erntet und pflanzt Felder in der Nähe neu.", () -> run("farm"))
			.active = available;
		button("§cStopp", left + 2 * (third + GAP), row4, third, "Bricht alles ab, was Baritone gerade tut.", this::stop)
			.active = available;

		// Zeilen 5 und 6: Einstellungen
		int toggleTop = top + 4 * ROW + 12;
		for (int i = 0; i < TOGGLES.size(); i++) {
			Toggle toggle = TOGGLES.get(i);
			int tx = left + i % 3 * (third + GAP);
			int ty = toggleTop + i / 3 * ROW;
			Button[] self = new Button[1];
			self[0] = button(toggleLabel(toggle), tx, ty, third, toggle.description(), () -> {
				BaritoneBridge.getBooleanSetting(toggle.setting())
					.ifPresent(value -> BaritoneBridge.setSetting(toggle.setting(), !value));
				self[0].setMessage(Component.literal(toggleLabel(toggle)));
			});
			self[0].active = available && BaritoneBridge.getBooleanSetting(toggle.setting()).isPresent();
		}

		button("Fertig", width / 2 - 100, toggleTop + 2 * ROW + 12, 200, null, this::onClose);

		if (!available)
			message = "§cBaritone ist nicht installiert. Anleitung in der README.";
	}

	private EditBox box(int bx, int by, int bw, String hint, String value) {
		EditBox box = new EditBox(font, bx, by, bw, 20, Component.literal(hint));
		box.setMaxLength(64);
		box.setHint(Component.literal(hint));
		box.setValue(value);
		return addRenderableWidget(box);
	}

	private Button button(String label, int bx, int by, int bw, String tooltip, Runnable action) {
		Button.Builder builder = Button.builder(Component.literal(label), b -> action.run()).bounds(bx, by, bw, 20);
		if (tooltip != null)
			builder.tooltip(Tooltip.create(Component.literal(tooltip)));
		return addRenderableWidget(builder.build());
	}

	private static String toggleLabel(Toggle toggle) {
		return BaritoneBridge.getBooleanSetting(toggle.setting())
			.map(on -> toggle.label() + ": " + (on ? "§aan" : "§caus"))
			.orElse(toggle.label());
	}

	private void fillHere() {
		if (minecraft.player == null)
			return;
		BlockPos pos = minecraft.player.blockPosition();
		x.setValue(String.valueOf(pos.getX()));
		y.setValue(String.valueOf(pos.getY()));
		z.setValue(String.valueOf(pos.getZ()));
	}

	private void gotoCoords() {
		try {
			int gx = Integer.parseInt(x.getValue().trim());
			int gz = Integer.parseInt(z.getValue().trim());
			String yText = y.getValue().trim();
			// Ohne Y sucht Baritone sich selbst die Höhe, das ist für weite Strecken meist besser
			run(yText.isEmpty() ? "goto " + gx + " " + gz : "goto " + gx + " " + Integer.parseInt(yText) + " " + gz);
		} catch (NumberFormatException e) {
			message = "§cX und Z müssen ganze Zahlen sein, Y ganze Zahl oder leer.";
		}
	}

	private void runWithBlock(String command) {
		String id = block.getValue().trim().toLowerCase(Locale.ROOT).replace("minecraft:", "");
		if (id.isEmpty())
			message = "§cErst einen Block eintragen, z. B. diamond_ore oder oak_log.";
		else
			run(command + " " + id);
	}

	private void stop() {
		// AutoMine mit ausschalten, sonst steht es grün im ClickGUI, obwohl nichts mehr läuft
		if (Modules.AUTO_MINE.isEnabled())
			Modules.AUTO_MINE.setEnabled(false);
		run("stop");
	}

	/**
	 * Befehl ausführen und bei Erfolg ins Spiel zurück, damit man sieht, wie Baritone loslegt.
	 * Nicht zurück ins Pausenmenü: das würde im Einzelspieler das Spiel anhalten.
	 */
	private void run(String command) {
		if (BaritoneBridge.execute(command))
			minecraft.gui.setScreen(null);
		else
			message = "§cBaritone hat \"" + command + "\" abgelehnt, Details im Chat.";
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
		super.extractRenderState(g, mouseX, mouseY, partialTicks);
		int top = Math.max(40, height / 2 - 110);
		g.centeredText(font, title, width / 2, top - 30, Theme.TEXT);
		g.centeredText(font, BaritoneBridge.status(), width / 2, top - 16, Theme.UNAVAILABLE_TEXT);
		if (!message.isEmpty())
			g.centeredText(font, message, width / 2, top + 4 * ROW + 2, Theme.TEXT);
	}

	@Override
	public void removed() {
		remember();
	}

	private void remember() {
		lastX = x.getValue();
		lastY = y.getValue();
		lastZ = z.getValue();
		lastPlayer = player.getValue();
		lastBlock = block.getValue();
	}

	@Override
	public void onClose() {
		minecraft.gui.setScreen(parent);
	}
}
