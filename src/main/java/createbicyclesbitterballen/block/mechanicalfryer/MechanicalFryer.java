package createbicyclesbitterballen.block.mechanicalfryer;


import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import createbicyclesbitterballen.index.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import createbicyclesbitterballen.block.mechanicalfryer.MechanicalFryerEntity;
public class MechanicalFryer extends HorizontalKineticBlock implements IBE<MechanicalFryerEntity> {
    public MechanicalFryer(Properties properties) {
        super(properties);
    }


    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return !AllBlocks.BASIN.has(worldIn.getBlockState(pos.below()));
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        ItemStack itemInHand = player.getItemInHand(handIn);

        if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS; // Only execute on the server side
        }

        BlockEntity be = worldIn.getBlockEntity(pos);
        if (!(be instanceof MechanicalFryerEntity fryer)) {
            return InteractionResult.FAIL;
        }

        if (!itemInHand.isEmpty()) {
            // Check if the item can be processed using the fryer's method
            if (fryer.canProcess(itemInHand)) {
                // Attempt to insert the item into the input inventory
                if (fryer.inputInv.insertItem(0, itemInHand.copy(), true).isEmpty()) {
                    fryer.inputInv.insertItem(0, itemInHand.split(1), false);
                    fryer.setChanged(); // Mark the entity as changed
                    fryer.sendData(); // Send updated data to the client
                    return InteractionResult.CONSUME;
                }
            }
        } else {
            // Handle empty hand logic to retrieve items from the fryer
            boolean emptyOutput = true;
            IItemHandlerModifiable inv = fryer.outputInv;
            for (int slot = 0; slot < inv.getSlots(); slot++) {
                ItemStack stackInSlot = inv.getStackInSlot(slot);
                if (!stackInSlot.isEmpty()) {
                    emptyOutput = false;
                    ItemHandlerHelper.giveItemToPlayer(player, stackInSlot);
                    inv.setStackInSlot(slot, ItemStack.EMPTY);
                }
            }

            if (emptyOutput) {
                inv = fryer.inputInv;
                for (int slot = 0; slot < inv.getSlots(); slot++) {
                    ItemHandlerHelper.giveItemToPlayer(player, inv.getStackInSlot(slot));
                    inv.setStackInSlot(slot, ItemStack.EMPTY);
                }
            }

            fryer.setChanged();
            fryer.sendData();
        }

        return InteractionResult.SUCCESS;
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext
                && ((EntityCollisionContext) context).getEntity() instanceof Player)
            return AllShapes.CASING_14PX.get(Direction.DOWN);

        return AllShapes.MECHANICAL_PROCESSOR_SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction prefferedSide = getPreferredHorizontalFacing(context);
        if (prefferedSide != null)
            return defaultBlockState().setValue(HORIZONTAL_FACING, prefferedSide);
        return super.getStateForPlacement(context);
    }


    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(HORIZONTAL_FACING)
                .getAxis();
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING)
                .getAxis();
    }

    @Override
    public BlockEntityType<? extends MechanicalFryerEntity> getBlockEntityType() {
        return BlockEntityRegistry.MECHANICAL_FRYER.get();
    }

    @Override
    public Class<MechanicalFryerEntity> getBlockEntityClass() {
        return MechanicalFryerEntity.class;
    }


    @Override
    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
        return false;
    }

}