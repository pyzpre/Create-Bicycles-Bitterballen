package com.pyzpre.createbitterballen.mixin;

import com.pyzpre.createbitterballen.block.sunflower.VanillaSunflowerBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Blocks.class)
public class BlocksMixin {
    @Redirect(method = "<clinit>", at = @At(value = "NEW", target = "net/minecraft/world/level/block/TallFlowerBlock", ordinal = 0))
    private static TallFlowerBlock replaceSunflowerBlock(BlockBehaviour.Properties properties) {
        return new VanillaSunflowerBlock(properties);
    }
}
