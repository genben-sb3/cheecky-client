package dev.cheecky.modules;

import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import net.minecraft.client.player.LocalPlayer;

/** Wasser und Lava werden für dich fest. Die Kollision kommt aus BlockStateBaseMixin. */
public class Jesus extends Module {
	public Jesus() {
		super("Jesus", "Läuft über Wasser und Lava. Schleichen, um einzutauchen. Fällt auf Servern mit Anti-Cheat auf.",
			Category.MOVEMENT, 0);
	}

	/** Nur fest, solange du nicht schon drin bist, nicht schleichst und nicht tief fällst. */
	public boolean shouldBeSolid() {
		LocalPlayer player = mc.player;
		return isEnabled() && player != null && !player.isInLiquid() && !player.isShiftKeyDown()
			&& player.fallDistance <= 3;
	}
}
