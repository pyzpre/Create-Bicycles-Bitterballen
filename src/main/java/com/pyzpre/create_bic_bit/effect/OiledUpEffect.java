package com.pyzpre.create_bic_bit.effect;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Random;


public class OiledUpEffect extends MobEffect {
    private static final Random random = new Random();
    private static final double DROP_ITEM_CHANCE = 0.01;
    private static final double FALL_OFF_LADDER_CHANCE = 0.02;

    public OiledUpEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        Level world = entity.getCommandSenderWorld();

        // Level 0: Sliding + Floating in Water
        maybeFloatInWater(entity);

        // Level 1+: Sliding + Dropping Items + Floating in Rain + Falling off Ladders
        if (amplifier >= 1) {
            if (entity instanceof Player player) {
                maybeDropHeldItem(player, amplifier); // Apply the new level-based drop logic for players
            }
            maybeFallOffLadder(entity);

            if (world.isRaining()) {
                applyLevitationEffect(entity);
            }
        }
    }


    private void maybeDropHeldItem(Player player, int amplifier) {
        double baseDivisor = 2.0;
        double adjustedDropChance = DROP_ITEM_CHANCE / (baseDivisor + Math.sqrt(amplifier + 1));
        if (!player.getLevel().isClientSide) {
        if (random.nextDouble() < adjustedDropChance) {
            ItemStack itemStack = player.getMainHandItem();
            if (!itemStack.isEmpty()) {
                player.drop(itemStack, false, false);  // Drop the item immediately
                player.setItemInHand(player.getUsedItemHand(), ItemStack.EMPTY);  // Empty the hand
            }
        }
    }
    }

    private void maybeFloatInWater(LivingEntity entity) {
        if (entity.isInWater() && !entity.getLevel().getBlockState(entity.blockPosition().above()).isAir()) {
            // Apply an upward force if the entity is in water and there's no air block directly above
            double upwardForce = 0.1;  // Adjust this value to control the strength of the float effect
            entity.setDeltaMovement(entity.getDeltaMovement().add(0, upwardForce, 0));
        }
    }
    private void applyLevitationEffect(LivingEntity entity) {
        MobEffectInstance levitation = new MobEffectInstance(MobEffects.LEVITATION, 20, 0);
        entity.addEffect(levitation);

        // Only grant the advancement criterion if the entity is a player
        if (entity instanceof ServerPlayer serverPlayer) {
            grantAdvancementCriterion(serverPlayer, "create_bic_bit:step_3", "got_levitation");
        }
    }
    private void grantAdvancementCriterion(ServerPlayer player, String advancementID, String criterionKey) {
        PlayerAdvancements playerAdvancements = player.getAdvancements();
        Advancement advancement = player.server.getAdvancements().getAdvancement(new ResourceLocation(advancementID));

        if (advancement != null && advancement.getCriteria().containsKey(criterionKey)) {
            AdvancementProgress advancementProgress = playerAdvancements.getOrStartProgress(advancement);

            if (!advancementProgress.isDone()) {
                playerAdvancements.award(advancement, criterionKey);
            }
        }
    }


    private void maybeFallOffLadder(LivingEntity entity) {
        // Check if the block at the entity's feet or the block they are moving into is a ladder
        boolean isOnLadder = entity.getLevel().getBlockState(entity.blockPosition()).is(BlockTags.CLIMBABLE) ||
                entity.getLevel().getBlockState(entity.blockPosition().above()).is(BlockTags.CLIMBABLE);

        if (isOnLadder && random.nextDouble() < FALL_OFF_LADDER_CHANCE) {
            // Get the entity's facing direction
            Vec3 lookVector = entity.getLookAngle();
            // Apply a force opposite to the look direction (negating the vector), but only affecting the X and Z axes (horizontal push)
            Vec3 pushVector = new Vec3(-lookVector.x, 0, -lookVector.z).normalize().scale(0.15); // Scale determines the strength of the push
            entity.setDeltaMovement(entity.getDeltaMovement().add(pushVector));
        }
    }


}
