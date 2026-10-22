package dev.cheecky.modules;

import dev.cheecky.module.Category;
import dev.cheecky.module.Module;
import dev.cheecky.setting.CheckboxSetting;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.player.LocalPlayer;

public class BunnyHop extends Module {
	private final CheckboxSetting onlySprinting = add(new CheckboxSetting("Nur beim Sprinten",
		"Springt nur, wenn du sprintest. Aus: springt bei jeder Bewegung.", false));

	public BunnyHop() {
		super("BunnyHop", "Springt automatisch, sobald du auf dem Boden bist und dich bewegst.",
			Category.MOVEMENT, InputConstants.KEY_B);
	}

	@Override
	public void onTick() {
		LocalPlayer player = mc.player;
		if (!player.onGround() || player.isShiftKeyDown() || player.isInWater())
			return;

		boolean moving = player.zza != 0 || player.xxa != 0;
		if (moving && (!onlySprinting.isChecked() || player.isSprinting()))
			player.jumpFromGround();
	}
}
