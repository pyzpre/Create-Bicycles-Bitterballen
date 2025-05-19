package com.pyzpre.create_bic_bit.block.sunflower;

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
}