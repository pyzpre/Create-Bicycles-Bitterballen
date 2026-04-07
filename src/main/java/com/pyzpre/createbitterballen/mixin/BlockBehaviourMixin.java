package com.pyzpre.createbitterballen.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.pyzpre.createbitterballen.block.sunflower.VanillaSunflowerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
    @ModifyReturnValue(method = "getShape", at = @At("RETURN"))
    private VoxelShape modifyShape(VoxelShape original, BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if((Object) this == Blocks.SUNFLOWER) {
            return VanillaSunflowerBlock.getShape(state, level, pos, context);
        }
        return original;
    }
}
