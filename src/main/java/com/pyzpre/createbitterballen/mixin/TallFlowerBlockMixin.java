package com.pyzpre.createbitterballen.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallFlowerBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TallFlowerBlock.class)
public class TallFlowerBlockMixin {
    @ModifyReturnValue(method = {"isValidBonemealTarget", "isBonemealSuccess"}, at = @At("RETURN"))
    private boolean modifySunflowerBonemeal(boolean original) {
        if((Object) this == Blocks.SUNFLOWER) {
            return false;
        }
        return original;
    }

    @WrapWithCondition(method = "performBonemeal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/TallFlowerBlock;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"))
    private boolean preventSunflowerBonemeal(Level level, BlockPos blockPos, ItemStack itemStack) {
        return (Object) this != Blocks.SUNFLOWER;
    }
}
