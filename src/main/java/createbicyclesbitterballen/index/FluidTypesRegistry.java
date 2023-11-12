
package createbicyclesbitterballen.index;

import createbicyclesbitterballen.fluid.types.FryingOilFluidType;
import createbicyclesbitterballen.fluid.types.StamppotFluidType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fluids.FluidType;

import createbicyclesbitterballen.CreateBicBitMod;

public class FluidTypesRegistry {
    public static final DeferredRegister<FluidType> REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, CreateBicBitMod.MODID);
    public static final RegistryObject<FluidType> STAMPPOT_TYPE = REGISTRY.register("stamppot", () -> new StamppotFluidType());
    public static final RegistryObject<FluidType> FRYING_OIL_TYPE = REGISTRY.register("frying_oil", () -> new FryingOilFluidType());
}
