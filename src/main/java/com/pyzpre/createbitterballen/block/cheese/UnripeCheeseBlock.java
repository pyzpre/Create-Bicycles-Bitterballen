package com.pyzpre.createbitterballen.block.cheese;

import com.pyzpre.createbitterballen.index.BlockRegistry;
import com.pyzpre.createbitterballen.index.ItemRegistry;
import com.simibubi.create.foundation.item.TooltipHelper;
import net.createmod.catnip.lang.FontHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UnripeCheeseBlock extends Block {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_1;
    public static final BooleanProperty WAXED = BooleanProperty.create("waxed");
    private static final VoxelShape SHAPE = makeShape();
    public UnripeCheeseBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0).setValue(WAXED, false));
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }
    public static VoxelShape makeShape() {
        return Shapes.box(1 / 16D, 0 / 16D, 1 / 16D, 15 / 16D, 8 / 16D, 15 / 16D);
    }


    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        super.randomTick(state, world, pos, random);
        if (!world.isAreaLoaded(pos, 1)) return;
        if (random.nextInt(20) == 0) {
            this.age(world, pos, state);
        }
    }

    private void age(Level world, BlockPos pos, BlockState state) {
        if (state.getValue(WAXED)) {
            return;
        }
        int age = state.getValue(AGE);
        if (age < 1) {
            world.setBlock(pos, state.setValue(AGE, age + 1), 3);
            BlockState youngCheeseState = BlockRegistry.YOUNG_CHEESE.get().defaultBlockState().setValue(WAXED, state.getValue(WAXED));
            world.setBlock(pos, youngCheeseState, 3);
        }
    }
    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(hand);
        Item usedItem = itemStack.getItem();

        if (!world.isClientSide) {
            if (usedItem == Items.HONEYCOMB) {
                if (handleWaxing(world, pos, state, player, itemStack)) {
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

    private boolean handleWaxing(Level world, BlockPos pos, BlockState state, Player player, ItemStack itemStack) {
        if (!state.getValue(WAXED)) {
            world.setBlock(pos, state.setValue(WAXED, true), 3);
            if (!player.isCreative()) {
                itemStack.shrink(1);
            }
            playWaxOnEffect(world, pos);
            world.scheduleTick(pos, this, 1);

            return true;
        }
        return false;
    }
    private void transition(ServerLevel world, BlockPos pos, BlockState state) {
        BlockState agedCheeseState = BlockRegistry.WAXED_UNRIPE_CHEESE.get().defaultBlockState().setValue(AGE, 0).setValue(WAXED, true);
        world.setBlockAndUpdate(pos, agedCheeseState);
    }
    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        super.tick(state, world, pos, random);
        if (state.getValue(WAXED)) {
            transition(world, pos, state);
        }
    }
    private boolean handleShearing(Level world, BlockPos pos, BlockState state) {
        {
            dropCheeseProducts(world, pos);
            world.removeBlock(pos, false);
            playShearingEffect(world, pos);
            return true;
        }
    }

    private void playWaxOnEffect(Level world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);

        if (world instanceof ServerLevel) {
            ServerLevel serverWorld = (ServerLevel) world;
            for (int i = 0; i < 20; i++) {
                double d0 = serverWorld.random.nextGaussian() * 0.1D;
                double d1 = serverWorld.random.nextGaussian() * 0.1D;
                double d2 = serverWorld.random.nextGaussian() * 0.1D;
                double x = pos.getX() + 0.5 + serverWorld.random.nextGaussian() * 0.5;
                double y = pos.getY() + 0.6;
                double z = pos.getZ() + 0.5 + serverWorld.random.nextGaussian() * 0.5;
                serverWorld.sendParticles(ParticleTypes.WAX_ON, x, y, z, 1, d0, d1, d2, 0.0D);
            }
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

    @Override
    protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, WAXED);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        FontHelper.Palette palette = FontHelper.Palette.STANDARD_CREATE;
        // Add a "Hold Shift" message for extended tooltip using the new translation method
        tooltip.add(TooltipHelper.holdShift(FontHelper.Palette.STANDARD_CREATE, true));

        // Check if the player is holding Shift
        if (Screen.hasShiftDown()) {
            // Use CreateLang to translate the tooltip correctly
            MutableComponent part1 = Component.translatable("item.create_bic_bit.cheese.tooltip.part1")
                    .withStyle(palette.primary());
            MutableComponent part2 = Component.translatable("item.create_bic_bit.cheese.tooltip.part2")
                    .withStyle(palette.highlight());
            MutableComponent part3 = Component.translatable("item.create_bic_bit.cheese.tooltip.part3")
                    .withStyle(palette.primary());

            // Combine the parts of the detailed tooltip
            tooltip.add(part1.append(part2).append(part3));
        }
    }

}
