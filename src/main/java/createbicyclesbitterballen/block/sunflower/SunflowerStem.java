package createbicyclesbitterballen.block.sunflower;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class SunflowerStem extends DoublePlantBlock implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;

    public SunflowerStem(Properties properties) {
        super(properties.offsetType(OffsetType.XZ));
    }



    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
        super.createBlockStateDefinition(builder);
    }

    private boolean isMaxAge(BlockState state) {
        return state.getValue(AGE) >= 2;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER && !this.isMaxAge(state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) != 0) {
            return;
        }
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            int currentAge = state.getValue(AGE);

            if (currentAge < 2) {
                int newAge = currentAge + 1;
                world.setBlockAndUpdate(pos, state.setValue(AGE, newAge));


                if (newAge == 1 && world.isEmptyBlock(pos.above())) {
                    world.setBlockAndUpdate(pos.above(), this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER).setValue(AGE, 0));
                }
            }

            if (currentAge == 1) {
                BlockPos upperPartPos = pos.above();
                boolean AboveBlockRemoved = world.setBlock(upperPartPos, Blocks.AIR.defaultBlockState(), 18);
                if (AboveBlockRemoved) {
                    DoublePlantBlock.placeAt(world, Blocks.SUNFLOWER.defaultBlockState(), pos, 18);
                }

            }
        }
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter p_50897_, BlockPos p_50898_, BlockState p_50899_, boolean p_50900_){
        if (p_50899_.getValue(HALF) == DoubleBlockHalf.LOWER) {
        return !isMaxAge(p_50899_);
    } else {
        // Check if the lower part is not at max age
        BlockState lowerPartState = p_50897_.getBlockState(p_50898_.below());
        return lowerPartState.getBlock() == this && !isMaxAge(lowerPartState);
    }
}

    @Override
    public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            growStemOrSunflower(world, pos, state);
        } else {
            // Handling bonemeal on the upper part
            BlockPos lowerPartPos = pos.below();
            BlockState lowerPartState = world.getBlockState(lowerPartPos);
            if (lowerPartState.getBlock() == this) {
                growStemOrSunflower(world, lowerPartPos, lowerPartState);
            }
        }
    }

    private void growStemOrSunflower(ServerLevel world, BlockPos pos, BlockState state) {
        int currentAge = state.getValue(AGE);
        if (currentAge < 2) {
            int newAge = currentAge + 1;
            world.setBlockAndUpdate(pos, state.setValue(AGE, newAge));

            if (newAge == 1 && world.isEmptyBlock(pos.above())) {
                world.setBlockAndUpdate(pos.above(), this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER).setValue(AGE, 0));
            }
        }

        if (currentAge == 1) {
            boolean isAboveBlockRemoved = world.removeBlock(pos.above(), false);
            if (isAboveBlockRemoved) {
                DoublePlantBlock.placeAt(world, Blocks.SUNFLOWER.defaultBlockState(), pos, 3);
            }
        }
    }






}
