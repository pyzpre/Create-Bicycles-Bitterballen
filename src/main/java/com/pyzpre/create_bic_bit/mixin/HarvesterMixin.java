package com.pyzpre.create_bic_bit.mixin;

import com.pyzpre.create_bic_bit.block.sunflower.SunflowerStem;
import com.pyzpre.create_bic_bit.index.BlockRegistry;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HarvesterMovementBehaviour.class, remap = false)
public abstract class HarvesterMixin {

    @Inject(
            method = "visitNewPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/contraptions/actors/harvester/HarvesterMovementBehaviour;isValidCrop(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"
            ),
            cancellable = true
    )
    private void onVisitNewPosition(MovementContext context, BlockPos pos, CallbackInfo ci) {
        Level world = context.world;

        if (world.isClientSide()) {
            return;
        }

        BlockState stateVisited = world.getBlockState(pos);

        if (stateVisited.is(Blocks.SUNFLOWER)) {
            handleHarvesterInteraction(world, pos, context, stateVisited);
            ci.cancel();
        }
    }

    private void handleHarvesterInteraction(Level world, BlockPos pos, MovementContext context, BlockState state) {
        ItemStack sunflowerHead = new ItemStack(Items.SUNFLOWER);

        IItemHandler itemHandler = context.contraption.getSharedInventory();

        ItemStack remaining = ItemHandlerHelper.insertItem(itemHandler, sunflowerHead, false);

        if (!remaining.isEmpty()) {
            Block.popResource(world, pos, remaining);
        }

        replaceWithSunflowerStem(world, pos, state);

        world.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private void replaceWithSunflowerStem(Level world, BlockPos pos, BlockState state) {
        DoubleBlockHalf half = state.getValue(DoublePlantBlock.HALF);
        BlockPos lowerPos = half == DoubleBlockHalf.LOWER ? pos : pos.below();
        BlockPos upperPos = lowerPos.above();

        world.setBlock(lowerPos, Blocks.AIR.defaultBlockState(), 18);
        world.setBlock(upperPos, Blocks.AIR.defaultBlockState(), 18);

        BlockState lowerStemState = BlockRegistry.SUNFLOWERSTEM.get().defaultBlockState()
                .setValue(SunflowerStem.AGE, 2)
                .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER);

        BlockState upperStemState = BlockRegistry.SUNFLOWERSTEM.get().defaultBlockState()
                .setValue(SunflowerStem.AGE, 2)
                .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);

        world.setBlock(lowerPos, lowerStemState, 3);
        world.setBlock(upperPos, upperStemState, 3);
    }
}
