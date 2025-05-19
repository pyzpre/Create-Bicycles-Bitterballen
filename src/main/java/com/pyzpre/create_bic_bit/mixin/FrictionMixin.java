package com.pyzpre.create_bic_bit.mixin;

import com.pyzpre.create_bic_bit.index.EffectRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(LivingEntity.class)
public abstract class FrictionMixin  {

    /**
     * Redirects the call to BlockState.getFriction to modify the friction value
     * for entities with the OILED_UP effect.
     *
     * @param blockState The block state
     * @param level The level reader
     * @param pos The block position
     * @param entity The entity interacting with the block
     * @return The friction value
     */
    @Redirect(method = "travel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getFriction(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)F"))
    private float redirectGetFriction(BlockState blockState, LevelReader level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity livingEntity && livingEntity.hasEffect(EffectRegistry.OILED_UP.get())) {
            // modified friction value
            return 0.99f;
        }
        return blockState.getFriction(level, pos, entity);
    }
}


