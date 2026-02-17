package com.pyzpre.createbitterballen.block.cheese;

import com.pyzpre.createbitterballen.index.BlockRegistry;
import com.pyzpre.createbitterballen.index.ItemRegistry;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WaxedUnripeCheeseBlock extends Block {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_1;
    public static final BooleanProperty WAXED = BooleanProperty.create("waxed");
    private static final VoxelShape SHAPE = makeShape();
    public WaxedUnripeCheeseBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0).setValue(WAXED, true));
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }
    public static VoxelShape makeShape() {
        return Shapes.box(1 / 16D, 0 / 16D, 1 / 16D, 15 / 16D, 8 / 16D, 15 / 16D);
    }


    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(hand);
        Item usedItem = itemStack.getItem();

        if (!world.isClientSide) {
            if (usedItem instanceof AxeItem) {
                if (handleDewaxing(world, pos, state)) {
                    return InteractionResult.SUCCESS;
                }
            } else if (itemStack.is(ConventionalItemTags.SHEARS) ||
                    itemStack.is(TagKey.create(Registries.ITEM, new ResourceLocation("c", "knives")))) {
                if (handleShearing(world, pos, state)) {
                    playShearingEffect(world, pos);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return super.use(state, world, pos, player, hand, hit);
    }


    private boolean handleDewaxing(Level world, BlockPos pos, BlockState state) {
        if (state.getValue(WAXED)) {
            BlockState youngCheeseState = BlockRegistry.UNRIPE_CHEESE.get().defaultBlockState()
                    .setValue(AGE, 0)
                    .setValue(WAXED, false);
            world.setBlock(pos, youngCheeseState, 3);

            playWaxOffEffect(world, pos);

            return true;
        }
        return false;
    }
    private boolean handleShearing(Level world, BlockPos pos, BlockState state) {
        {
            dropCheeseProducts(world, pos);
            world.removeBlock(pos, false);
            playShearingEffect(world, pos);
            return true;
        }
    }
    private void dropCheeseProducts(Level world, BlockPos pos) {
        ItemStack dropItem = new ItemStack(ItemRegistry.UNRIPE_CHEESE_WEDGE);
        RandomSource random = world.random;

        for (int i = 0; i < 4; i++) {
            double d0 = random.nextFloat() * 0.7F + 0.15F;
            double d1 = random.nextFloat() * 0.7F + 0.060000002F + 0.6D;
            double d2 = random.nextFloat() * 0.7F + 0.15F;

            ItemStack itemStackCopy = dropItem.copy();
            ItemEntity itemEntity = new ItemEntity(world, pos.getX() + d0, pos.getY() + d1, pos.getZ() + d2, itemStackCopy);
            itemEntity.setDefaultPickUpDelay();
            world.addFreshEntity(itemEntity);
        }
    }

    private void playShearingEffect(Level world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private void playWaxOffEffect(Level world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, WAXED);
    }

}
