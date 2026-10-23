package dev.cheecky.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;

/**
 * Schätzt den Explosionsschaden wie Vanilla: Abstand, Deckung, Schwierigkeit, Rüstung,
 * Resistenz und Schutz-/Explosionsschutz-Verzauberungen.
 */
public final class ExplosionDamage {
	public static final float CRYSTAL_POWER = 6;

	private ExplosionDamage() {}

	public static float estimate(LivingEntity entity, Vec3 center, float power) {
		if (entity instanceof Player player && (player.isCreative() || player.isSpectator()))
			return 0;

		double diameter = power * 2;
		double distance = Math.sqrt(entity.distanceToSqr(center)) / diameter;
		if (distance > 1)
			return 0;

		double impact = (1 - distance) * ServerExplosion.getSeenPercent(center, entity);
		float damage = (float) ((impact * impact + impact) / 2 * 7 * diameter + 1);

		Minecraft mc = Minecraft.getInstance();
		// Die Schwierigkeit zählt nur bei Spielern
		if (entity instanceof Player) {
			Difficulty difficulty = mc.level.getDifficulty();
			if (difficulty == Difficulty.PEACEFUL)
				return 0;
			if (difficulty == Difficulty.EASY)
				damage = Math.min(damage / 2 + 1, damage);
			else if (difficulty == Difficulty.HARD)
				damage *= 1.5F;
		}

		damage = CombatRules.getDamageAfterAbsorb(entity, damage, mc.level.damageSources().explosion(null, null),
			entity.getArmorValue(), (float) entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS));

		MobEffectInstance resistance = entity.getEffect(MobEffects.RESISTANCE);
		if (resistance != null)
			damage *= Math.max(0, 1 - 0.2F * (resistance.getAmplifier() + 1));

		return CombatRules.getDamageAfterMagicAbsorb(damage, protectionPoints(entity));
	}

	/** Schutz zählt 1 Punkt pro Stufe, Explosionsschutz 2, höchstens 20 (wie Vanilla). */
	private static float protectionPoints(LivingEntity entity) {
		Holder<Enchantment> protection = enchantment(Enchantments.PROTECTION);
		Holder<Enchantment> blastProtection = enchantment(Enchantments.BLAST_PROTECTION);
		int points = 0;
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR)
				continue;
			var stack = entity.getItemBySlot(slot);
			if (protection != null)
				points += EnchantmentHelper.getItemEnchantmentLevel(protection, stack);
			if (blastProtection != null)
				points += 2 * EnchantmentHelper.getItemEnchantmentLevel(blastProtection, stack);
		}
		return Math.min(points, 20);
	}

	private static Holder<Enchantment> enchantment(ResourceKey<Enchantment> key) {
		try {
			return Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
		} catch (RuntimeException e) {
			return null;
		}
	}
}
