package pyzpre.createbicyclesbitterballen.index;


import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionResult;
import pyzpre.createbicyclesbitterballen.block.sunflower.SunflowerStem;
import net.minecraft.advancements.Advancement;
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


import static net.minecraft.world.level.block.Block.popResource;

public class SunflowerInteractionHandler {


    static {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

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

                world.setBlock(pos, BlockRegistry.SUNFLOWERSTEM.get().defaultBlockState().setValue(SunflowerStem.AGE, 1), 3);
                world.setBlock(pos.above(), BlockRegistry.SUNFLOWERSTEM.get().defaultBlockState().setValue(SunflowerStem.HALF, DoubleBlockHalf.UPPER), 3);

                world.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (player instanceof ServerPlayer) {
                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    grantAdvancementCriterion(serverPlayer, "create_bic_bit:lawn_defender", "harvested_sunflower");
                }

                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
    }
    public static void grantAdvancementCriterion(ServerPlayer player, String advancementID, String criterionKey) {
        PlayerAdvancements playerAdvancements = player.getAdvancements();
        Advancement advancement = player.server.getAdvancements().getAdvancement(new ResourceLocation(advancementID));

        if (advancement != null && advancement.getCriteria().containsKey(criterionKey)) {
            AdvancementProgress advancementProgress = playerAdvancements.getOrStartProgress(advancement);

            if (!advancementProgress.isDone()) {
                playerAdvancements.award(advancement, criterionKey);
            }
        }
    }

    private static void dropSunflowerHead(Level world, BlockPos pos, Player player) {
        ItemStack sunflowerHead = new ItemStack(Items.SUNFLOWER);
        popResource(world, pos, sunflowerHead);

    }

    public static void init() {

    }
}



