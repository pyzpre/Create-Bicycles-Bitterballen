package pyzpre.createbicyclesbitterballen.item;

import io.github.fabricators_of_create.porting_lib.common.util.IPlantable;
import pyzpre.createbicyclesbitterballen.index.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;


import javax.annotation.Nonnull;


public class SunflowerSeedsItem extends Item {

    public SunflowerSeedsItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(@Nonnull ItemStack itemstack) {
        return 16;
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack itemstack = context.getItemInHand();
        BlockState state = world.getBlockState(pos);


        if (state.getBlock() instanceof DoublePlantBlock && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {

        }

        else if (state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.GRASS || state.getBlock() == Blocks.TALL_GRASS ) {
            pos = pos.below();
        }

        state = world.getBlockState(pos);

        if (state.getBlock() instanceof DoublePlantBlock && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
            return InteractionResult.FAIL;
        }


        if (state.getBlock() == Blocks.TALL_GRASS || state.getBlock() == Blocks.LARGE_FERN || state.getBlock() == Blocks.GRASS || state.getBlock() == Blocks.FERN) {
            if (canPlantAbove(world, pos)) {
                world.setBlockAndUpdate(pos, BlockRegistry.SUNFLOWERSTEM.getDefaultState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER));
                return handlePlantingSuccess(context, itemstack);
            }
        } else if (state.canSustainPlant(world, pos, Direction.UP, (IPlantable) Blocks.SUNFLOWER) && canPlantAbove(world, pos)) {
            world.setBlockAndUpdate(pos.above(), BlockRegistry.SUNFLOWERSTEM.getDefaultState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER));
            return handlePlantingSuccess(context, itemstack);
        }

        return InteractionResult.PASS;
    }

    private boolean canPlantAbove(Level world, BlockPos pos) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = world.getBlockState(abovePos);
        boolean isReplaceableBlock = aboveState.getBlock() == Blocks.GRASS || aboveState.getBlock() == Blocks.FERN || aboveState.getBlock() == Blocks.TALL_GRASS || aboveState.getBlock() == Blocks.LARGE_FERN;
        return (world.isEmptyBlock(abovePos) || isReplaceableBlock) && world.isEmptyBlock(abovePos.above());
    }

    private InteractionResult handlePlantingSuccess(UseOnContext context, ItemStack itemstack) {
        net.minecraft.world.entity.player.Player player = context.getPlayer();
        if (player != null && !player.isCreative()) {
            itemstack.shrink(1);
        }
        context.getLevel().playSound(null, context.getClickedPos().above(), SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
        return InteractionResult.SUCCESS;
    }

}