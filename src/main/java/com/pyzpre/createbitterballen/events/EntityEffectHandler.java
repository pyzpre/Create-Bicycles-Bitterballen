package com.pyzpre.createbitterballen.events;

import com.pyzpre.createbitterballen.index.BlockRegistry;
import com.pyzpre.createbitterballen.index.EffectRegistry;
import com.pyzpre.createbitterballen.index.FluidRegistry;
import com.pyzpre.createbitterballen.mixin.MobAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityEffectHandler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean isInitialized = false;

    public static void onLevelTick(Level level) {
        if (!level.isClientSide) {

            // Iterate over all players in the level
            for (Player player : level.players()) {
                // Define a dynamic bounding box centered around the player
                BlockPos playerPos = player.blockPosition();
                AABB dynamicBounds = new AABB(
                        playerPos.getX() - 16, playerPos.getY() - 16, playerPos.getZ() - 16,
                        playerPos.getX() + 16, playerPos.getY() + 16, playerPos.getZ() + 16
                );

                for (Entity entity : level.getEntities(null, dynamicBounds)) {
                    if (entity instanceof LivingEntity livingEntity && isInFryingOil(livingEntity, level)) {
                        applyOilEffect(livingEntity);
                    }
                }
            }
        }
    }
    private static boolean isInFryingOil(LivingEntity entity, Level level) {
        BlockPos pos = entity.blockPosition();
        FluidState fluidState = level.getFluidState(pos);

        Fluid fluid = fluidState.getType();
        boolean isFryingOil = fluid.isSame(FluidRegistry.FRYING_OIL.get());

        return isFryingOil;
    }

    private static void applyOilEffect(LivingEntity entity) {
        // Apply the OILED_UP effect to the entity
        MobEffectInstance oiledup = new MobEffectInstance(EffectRegistry.OILED_UP.get(), 200, 1);

        if (!entity.hasEffect(EffectRegistry.OILED_UP.get())) {
            entity.addEffect(oiledup);
        }
    }

    public static boolean onEntityJoinLevel(Entity entity, Level world, boolean loadedFromDisk) {
        if (entity instanceof Chicken chicken) {
            // Add a new TemptGoal for sunflower seeds
            ((MobAccessor)chicken).create_bic_bit$getGoalSelector().addGoal(3, new TemptGoal(chicken, 1.0D, Ingredient.of(BlockRegistry.SUNFLOWERSTEM.get().asItem()), false));
        }
        return true;
    }

    public static InteractionResult onEntityInteract(Player player, Entity target, InteractionHand hand) {
        if (!player.level().isClientSide()) {
            ItemStack heldItem = player.getItemInHand(hand);

            if (target instanceof Chicken chicken) {
                if (heldItem.getItem() == BlockRegistry.SUNFLOWERSTEM.get().asItem()) {
                    if (chicken.isAlive() && !chicken.isBaby() && !chicken.isInLove()) {
                        if (!player.getAbilities().instabuild) {
                            heldItem.shrink(1);
                        }
                        chicken.setInLove(player);
                        player.swing(hand, true);
                        Level level = player.level();
                        level.broadcastEntityEvent(chicken, (byte)18);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return null;
    }
}
