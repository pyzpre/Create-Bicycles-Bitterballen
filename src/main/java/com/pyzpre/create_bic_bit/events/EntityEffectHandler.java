package com.pyzpre.create_bic_bit.events;

import com.pyzpre.create_bic_bit.index.BlockRegistry;
import com.pyzpre.create_bic_bit.index.EffectRegistry;
import com.pyzpre.create_bic_bit.index.FluidRegistry;
import net.minecraft.core.BlockPos;
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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityEffectHandler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean isInitialized = false;

    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        Level level = event.level;

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
    private boolean isInFryingOil(LivingEntity entity, Level level) {
        BlockPos pos = entity.blockPosition();
        FluidState fluidState = level.getFluidState(pos);

        Fluid fluid = fluidState.getType();
        boolean isFryingOil = fluid.isSame(FluidRegistry.FRYING_OIL.get());

        return isFryingOil;
    }

    private void applyOilEffect(LivingEntity entity) {
        // Apply the OILED_UP effect to the entity
        MobEffectInstance oiledup = new MobEffectInstance(EffectRegistry.OILED_UP.get(), 200, 1);

        if (!entity.hasEffect(EffectRegistry.OILED_UP.get())) {
            entity.addEffect(oiledup);
        }
    }
    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Chicken chicken) {
            // Add a new TemptGoal for sunflower seeds
            chicken.goalSelector.addGoal(3, new TemptGoal(chicken, 1.0D, Ingredient.of(BlockRegistry.SUNFLOWERSTEM.get().asItem()), false));
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onEntityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!event.getLevel().isClientSide()) {
            Entity target = event.getTarget();
            Player player = event.getEntity();
            ItemStack heldItem = player.getItemInHand(event.getHand());

            if (target instanceof Chicken chicken) {
                if (heldItem.getItem() == BlockRegistry.SUNFLOWERSTEM.get().asItem()) {
                    if (chicken.isAlive() && !chicken.isBaby() && !chicken.isInLove()) {
                        if (!player.getAbilities().instabuild) {
                            heldItem.shrink(1);
                        }
                        chicken.setInLove(player);
                        player.swing(event.getHand(), true);
                        Level level = player.getLevel();
                        level.broadcastEntityEvent(chicken, (byte)18);
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
}
