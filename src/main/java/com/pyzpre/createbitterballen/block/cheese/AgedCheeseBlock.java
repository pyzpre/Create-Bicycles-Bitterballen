package com.pyzpre.createbitterballen.block.cheese;

import com.pyzpre.createbitterballen.index.BlockRegistry;
import com.pyzpre.createbitterballen.index.ItemRegistry;
import com.simibubi.create.foundation.item.TooltipHelper;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
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

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static com.pyzpre.createbitterballen.block.cheese.YoungCheeseBlock.TOOLS_KNIVES;

public class AgedCheeseBlock extends Block implements IBlockExtension {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
    public static final BooleanProperty WAXED = BooleanProperty.create("waxed");
    private static final VoxelShape SHAPE = makeShape();
    public AgedCheeseBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 2).setValue(WAXED, false));
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }
    public static VoxelShape makeShape() {
        return Shapes.box(1 / 16D, 0 / 16D, 1 / 16D, 15 / 16D, 8 / 16D, 15 / 16D);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isClientSide) {
            if (stack.is(Items.HONEYCOMB)) {
                if (handleWaxing(world, pos, state, player, stack)) {
                    return ItemInteractionResult.CONSUME;
                }
            }  else if (stack.is(Tags.Items.TOOLS_SHEAR) || stack.is(TOOLS_KNIVES)) {
                if (handleShearing(world, pos, state)) {
                    player.swing(hand, true);
                    playShearingEffect(world, pos);
                    return ItemInteractionResult.CONSUME;
                }
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }






    private boolean handleWaxing(Level world, BlockPos pos, BlockState state, Player player, ItemStack itemStack) {
        BlockState waxedState = BlockRegistry.WAXED_AGED_CHEESE.get().defaultBlockState()
                .setValue(WaxedAgedCheeseBlock.AGE, state.getValue(WaxedAgedCheeseBlock.AGE))
                .setValue(WaxedAgedCheeseBlock.WAXED, true);

        world.setBlock(pos, waxedState, 3);

        if (!player.isCreative()) {
            itemStack.shrink(1);
        }

        playWaxOnEffect(world, pos);
        world.scheduleTick(pos, BlockRegistry.WAXED_AGED_CHEESE.get(), 1);

        return true;
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        super.tick(state, world, pos, random);
    }

    private boolean handleShearing(Level world, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        if (age == 1 || age == 2) {
            dropCheeseProducts(world, pos, age);
            world.removeBlock(pos, false);
            playShearingEffect(world, pos);
            return true;
        }
        return false;
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
    private void playWaxOffEffect(Level world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
    private void dropCheeseProducts(Level world, BlockPos pos, int age) {
        ItemStack dropItem = (age == 1) ? new ItemStack(ItemRegistry.YOUNG_CHEESE_WEDGE.get()) : new ItemStack(ItemRegistry.AGED_CHEESE_WEDGE.get());
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
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        FontHelper.Palette palette = FontHelper.Palette.STANDARD_CREATE;

        // Add a "Hold Shift" message for extended tooltip using the new translation method
        tooltip.add(TooltipHelper.holdShift(FontHelper.Palette.STANDARD_CREATE, true));

        // Check if the player is holding Shift
        if (Screen.hasShiftDown()) {
            MutableComponent part1 = Component.translatable("item.create_bic_bit.cheese.tooltip.part1")
                    .withStyle(palette.primary());
            MutableComponent part2 = Component.translatable("item.create_bic_bit.cheese.tooltip.part2")
                    .withStyle(palette.highlight());
            MutableComponent part3 = Component.translatable("item.create_bic_bit.cheese.tooltip.part3")
                    .withStyle(palette.primary());

            tooltip.add(part1.append(part2).append(part3));
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(world, pos, state, entity, stack);

        if (!world.isClientSide && entity instanceof ServerPlayer serverPlayer) {
            grantAdvancementCriterion(serverPlayer, "create_bic_bit:cheese", "placed_cheese");
        }
    }


    private static void grantAdvancementCriterion(ServerPlayer player, String advancementID, String criterionKey) {
        PlayerAdvancements playerAdvancements = player.getAdvancements();
        ResourceLocation id = ResourceLocation.parse(advancementID);

        Optional<AdvancementHolder> optionalHolder = Optional.ofNullable(player.server.getAdvancements().get(id));

        optionalHolder.ifPresent(holder -> {
            if (holder.value().criteria().containsKey(criterionKey)) {
                AdvancementProgress progress = playerAdvancements.getOrStartProgress(holder);
                if (!progress.isDone()) {
                    playerAdvancements.award(holder, criterionKey);
                }
            }
        });
    }
}
