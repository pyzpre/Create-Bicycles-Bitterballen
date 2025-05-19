package com.pyzpre.createbitterballen.block.sunflower;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VanillaSunflowerBlock extends DoublePlantBlock {
    public VanillaSunflowerBlock(Properties properties) {
        super(properties);
    }

    private VoxelShape getCustomShape(BlockState state, BlockGetter level, BlockPos pos) {
        Vec3 offset = state.getOffset(level, pos);
        DoubleBlockHalf half = state.getValue(HALF);

        VoxelShape shape = (half == DoubleBlockHalf.LOWER)
                ? Shapes.box(5 / 16D, 0, 5 / 16D, 11 / 16D, 1, 11 / 16D)
                : Shapes.box(5 / 16D, 0, 5 / 16D, 11 / 16D, 11 / 16D, 11 / 16D);

        return shape.move(offset.x, offset.y, offset.z);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return getCustomShape(state, level, pos);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return getCustomShape(state, level, pos);
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return getCustomShape(state, level, pos);
    }
}