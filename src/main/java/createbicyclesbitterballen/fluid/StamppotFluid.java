
package createbicyclesbitterballen.fluid;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import createbicyclesbitterballen.index.CreateBicBitModFluids;
import createbicyclesbitterballen.index.CreateBicBitModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.block.state.StateDefinition;

import createbicyclesbitterballen.index.FluidTypesRegistry;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.InteractionHand;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;


@Mod.EventBusSubscriber(modid = "create_bic_bit")
public abstract class StamppotFluid extends ForgeFlowingFluid {
    public static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(FluidTypesRegistry.STAMPPOT_TYPE, CreateBicBitModFluids.STAMPPOT, CreateBicBitModFluids.FLOWING_STAMPPOT)
            .explosionResistance(100f).tickRate(1).slopeFindDistance(1);

    private StamppotFluid() {

        super(PROPERTIES);
    }

    public static class Flowing extends StamppotFluid {
        public Flowing() {
        }

        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }


        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static class Source extends StamppotFluid {
        public Source() {
        }

        public int getAmount(FluidState state) {
            return 8;
        }

        public boolean isSource(FluidState state) {
            return true;
        }
    }


    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockPos pos = event.getPos();
        BlockState state = event.getLevel().getBlockState(pos);

        if (state.getBlock() == AllBlocks.BASIN.get()) {
            BlockEntity blockEntity = event.getLevel().getBlockEntity(pos);
            Player player = event.getEntity();
            InteractionHand hand = event.getHand();
            ItemStack heldItem = player.getItemInHand(hand);

            if (blockEntity instanceof BasinBlockEntity basinEntity && heldItem.getItem() == Items.BOWL) {
                SmartFluidTank tank = basinEntity.getTanks().getSecond().getPrimaryHandler();
                FluidStack fluidInOutputTank = tank.getFluid();
                float totalUnits = fluidInOutputTank.getAmount();

                if (fluidInOutputTank.getFluid()!= CreateBicBitModFluids.STAMPPOT.get() || totalUnits < 250) return;

                heldItem.shrink(1);
                if (!player.getInventory().add(new ItemStack(CreateBicBitModItems.STAMPPOT_BOWL.get()))) {
                    player.drop(new ItemStack(CreateBicBitModItems.STAMPPOT_BOWL.get()), false);
                }

                tank.drain(250, IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }
}



