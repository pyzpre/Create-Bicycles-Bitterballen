
package createbicyclesbitterballen.index;

import createbicyclesbitterballen.fluid.FryingOilFluid;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;

import createbicyclesbitterballen.fluid.StamppotFluid;

import createbicyclesbitterballen.CreateBicBitMod;

public class CreateBicBitModFluids {
    public static final DeferredRegister<Fluid> REGISTRY = DeferredRegister.create(ForgeRegistries.FLUIDS, CreateBicBitMod.MODID);
    public static final RegistryObject<FlowingFluid> FRYING_OIL = REGISTRY.register("frying_oil", () -> new FryingOilFluid.Source());
    public static final RegistryObject<FlowingFluid> FLOWING_FRYING_OIL = REGISTRY.register("flowing_frying_oil", () -> new FryingOilFluid.Flowing());
    public static final RegistryObject<FlowingFluid> STAMPPOT = REGISTRY.register("stamppot", () -> new StamppotFluid.Source());
    public static final RegistryObject<FlowingFluid> FLOWING_STAMPPOT = REGISTRY.register("flowing_stamppot", () -> new StamppotFluid.Flowing());

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientSideHandler {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            ItemBlockRenderTypes.setRenderLayer(FRYING_OIL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(FLOWING_FRYING_OIL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(STAMPPOT.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(FLOWING_STAMPPOT.get(), RenderType.translucent());
        }
    }
}
