package com.pyzpre.createbitterballen.events;

import com.pyzpre.createbitterballen.block.sunflower.SunflowerStem;
import com.pyzpre.createbitterballen.index.BlockRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Optional;

import static net.minecraft.world.level.block.Block.popResource;

public class SunflowerInteractionHandler {


    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockPos pos = event.getPos();
        BlockState state = event.getLevel().getBlockState(pos);
        Player player = event.getEntity();
        Level world = event.getLevel();

        if (state.getBlock() == Blocks.SUNFLOWER) {

            dropSunflowerHead(world, pos, player);


            boolean isUpperPart = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER;


            if (isUpperPart) {
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
                pos = pos.below();
            }

            if (!isUpperPart) {
                BlockPos upperPartPos = pos.above();
                world.setBlock(upperPartPos, Blocks.AIR.defaultBlockState(), 18);
            }


            world.setBlock(pos, BlockRegistry.SUNFLOWERSTEM.get().defaultBlockState().setValue(SunflowerStem.AGE, 2), 3);

            world.setBlock(
                    pos.above(),
                    BlockRegistry.SUNFLOWERSTEM.get().defaultBlockState()
                            .setValue(SunflowerStem.HALF, DoubleBlockHalf.UPPER)
                            .setValue(SunflowerStem.AGE, 2),
                    3
            );

            world.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer) player;
                grantAdvancementCriterion(serverPlayer, "create_bic_bit:lawn_defender", "harvested_sunflower");
            }
            event.setCanceled(true);
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

    private static void dropSunflowerHead(Level world, BlockPos pos, Player player) {
        ItemStack sunflowerHead = new ItemStack(Items.SUNFLOWER);
        popResource(world, pos, sunflowerHead);

    }
}



