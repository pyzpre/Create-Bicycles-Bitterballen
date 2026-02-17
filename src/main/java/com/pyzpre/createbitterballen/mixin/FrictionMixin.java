package com.pyzpre.createbitterballen.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.pyzpre.createbitterballen.index.EffectRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
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
     * @return The friction value
     */
    @WrapOperation(method = "travel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;getFriction()F"))
    private float redirectGetFriction(Block instance, Operation<Float> original) {
        if (((Object)this) instanceof LivingEntity livingEntity && livingEntity.hasEffect(EffectRegistry.OILED_UP.get())) {
            // modified friction value
            return 0.99f;
        }
        return original.call(instance);
    }
}


