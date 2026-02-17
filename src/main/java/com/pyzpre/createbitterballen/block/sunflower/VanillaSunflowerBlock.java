package com.pyzpre.createbitterballen.block.sunflower;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VanillaSunflowerBlock extends TallFlowerBlock {
    public VanillaSunflowerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        // Get the random visual offset for this block
        Vec3 offset = state.getOffset(level, pos);

        // Get the block half (LOWER or UPPER)
        DoubleBlockHalf half = state.getValue(HALF);

        // Determine the shape based on the block half
        VoxelShape shape;
        if (half == DoubleBlockHalf.LOWER) {
            shape = Shapes.box(5 / 16D, 0 / 16D, 5 / 16D, 11 / 16D, 16 / 16D, 11 / 16D); // Bottom part
        } else {
            shape = Shapes.box(5 / 16D, 0 / 16D, 5 / 16D, 11 / 16D, 11 / 16D, 11 / 16D); // Top part
        }

        // Apply the random offset
        return shape.move(offset.x, offset.y, offset.z);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return false;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return false;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
    }
}