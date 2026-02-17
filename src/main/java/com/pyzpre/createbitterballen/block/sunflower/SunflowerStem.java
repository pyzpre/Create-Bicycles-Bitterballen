package com.pyzpre.createbitterballen.block.sunflower;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class SunflowerStem extends DoublePlantBlock implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    public SunflowerStem(Properties properties) {
        super(properties.offsetType(OffsetType.XZ));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
        super.createBlockStateDefinition(builder);
    }
    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER);
    }
    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        world.setBlock(pos, this.defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER), 3);
    }

    private boolean isMaxAge(BlockState state) {
        return state.getValue(AGE) >= 3;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER && !this.isMaxAge(state);
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        Vec3 offset = state.getOffset(level, pos); // Get the random offset
        int age = state.getValue(AGE);
        DoubleBlockHalf half = state.getValue(HALF); // Get the half property (LOWER or UPPER)

        VoxelShape shape;
        switch (age) {
            case 0: // First growth stage: 3x3 wide and 5 tall
                shape = Shapes.box(5 / 16D, 0 / 16D, 5 / 16D, 11 / 16D, 5 / 16D, 11 / 16D);
                break;
            case 1: // Second growth stage: slightly taller
                shape = Shapes.box(5 / 16D, 0 / 16D, 5 / 16D, 11 / 16D, 12 / 16D, 11 / 16D);
                break;
            case 2: // Third growth stage: bottom and top halves have different shapes
                if (half == DoubleBlockHalf.LOWER) {
                    shape = Shapes.box(5 / 16D, 0 / 16D, 5 / 16D, 11 / 16D, 16 / 16D, 11 / 16D); // Bottom part for age 2
                } else {
                    shape = Shapes.box(5 / 16D, 0 / 16D, 5 / 16D, 11 / 16D, 11 / 16D, 11 / 16D); // Top part for age 2
                }
                break;
            case 3: // Fully grown
                if (half == DoubleBlockHalf.LOWER) {
                    shape = Shapes.box(5 / 16D, 0 / 16D, 5 / 16D, 11 / 16D, 16 / 16D, 11 / 16D); // Bottom part for age 2
                } else {
                    shape = Shapes.box(5 / 16D, 0 / 16D, 5 / 16D, 11 / 16D, 11 / 16D, 11 / 16D);
                }
                break;
            default: // Fallback (shouldn't happen)
                shape = Shapes.block();
        }

        return shape.move(offset.x, offset.y, offset.z); // Apply the random offset
    }

    public static VoxelShape makeShape() {
        return Shapes.box(5 / 16D, 0 / 16D, 5 / 16D, 11 / 16D, 16 / 16D, 11 / 16D);
    }



    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) != 0) {
            return;
        }
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            int currentAge = state.getValue(AGE);

            if (currentAge < 3) {
                int newAge = currentAge + 1;
                world.setBlockAndUpdate(pos, state.setValue(AGE, newAge));


                if (newAge == 2 && world.isEmptyBlock(pos.above())) {
                    world.setBlockAndUpdate(pos.above(), this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER).setValue(AGE, 2));
                }
            }

            if (currentAge == 2) {
                BlockPos upperPartPos = pos.above();
                boolean AboveBlockRemoved = world.setBlock(upperPartPos, Blocks.AIR.defaultBlockState(), 18);
                if (AboveBlockRemoved) {
                    DoublePlantBlock.placeAt(world, Blocks.SUNFLOWER.defaultBlockState(), pos, 18);
                }

            }
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state, boolean isClient) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return !isMaxAge(state);
        } else {
            BlockState lowerPartState = world.getBlockState(pos.below());
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
            BlockPos lowerPartPos = pos.below();
            BlockState lowerPartState = world.getBlockState(lowerPartPos);
            if (lowerPartState.getBlock() == this) {
                growStemOrSunflower(world, lowerPartPos, lowerPartState);
            }
        }
    }

    private void growStemOrSunflower(ServerLevel world, BlockPos pos, BlockState state) {
        int currentAge = state.getValue(AGE);
        if (currentAge < 3) {
            int newAge = currentAge + 1;
            world.setBlockAndUpdate(pos, state.setValue(AGE, newAge));

            if (newAge == 2 && world.isEmptyBlock(pos.above())) {
                world.setBlockAndUpdate(pos.above(), this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER).setValue(AGE, 2));
            }
        }

        if (currentAge == 2) {
            boolean AboveBlockRemoved = world.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 18);
            if (AboveBlockRemoved) {
                DoublePlantBlock.placeAt(world, Blocks.SUNFLOWER.defaultBlockState(), pos, 18);
            }
        }
    }
}
