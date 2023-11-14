
package createbicyclesbitterballen.fluid;

import createbicyclesbitterballen.CreateBicBitModTabs;
import createbicyclesbitterballen.index.BlockRegistry;
import createbicyclesbitterballen.index.CreateBicBitModFluids;
import createbicyclesbitterballen.index.CreateBicBitModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.block.state.StateDefinition;

import createbicyclesbitterballen.index.FluidTypesRegistry;
import org.jetbrains.annotations.NotNull;

import static createbicyclesbitterballen.CreateBicBitMod.REGISTRATE;


public abstract class FryingOilFluid extends ForgeFlowingFluid {
    static { REGISTRATE.creativeModeTab(() -> CreateBicBitModTabs.CREATIVE_TAB); }
    public static final ForgeFlowingFluid.Properties PROPERTIES = (new ForgeFlowingFluid.Properties(() -> {
        return (FluidType)FluidTypesRegistry.FRYING_OIL_TYPE.get();
    }, () -> {
        return (Fluid)CreateBicBitModFluids.FRYING_OIL.get();
    }, () -> {
        return (Fluid)CreateBicBitModFluids.FLOWING_FRYING_OIL.get();
    })).explosionResistance(100.0F).tickRate(4).levelDecreasePerBlock(2).slopeFindDistance(8).bucket(() -> {
        return (Item) CreateBicBitModItems.FRYING_OIL_BUCKET.get();
    });

    private FryingOilFluid() {
        super(PROPERTIES);
    }

    public static class Flowing extends FryingOilFluid {
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

    public static class Source extends FryingOilFluid {
        public Source() {
        }

        public int getAmount(FluidState state) {
            return 8;
        }

        public boolean isSource(FluidState state) {
            return true;
        }
    }
}
