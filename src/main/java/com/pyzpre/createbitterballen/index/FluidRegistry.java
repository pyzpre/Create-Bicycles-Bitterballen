package com.pyzpre.createbitterballen.index;

import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.tterrag.registrate.fabric.SimpleFlowableFluid;
import com.tterrag.registrate.util.entry.FluidEntry;
import io.github.fabricators_of_create.porting_lib.event.common.FluidPlaceBlockCallback;
import net.createmod.catnip.data.Iterate;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import static com.pyzpre.createbitterballen.CreateBitterballen.REGISTRATE;
import static net.minecraft.world.item.Items.BOWL;

public class FluidRegistry {
    public static final long STAMPPOT_AMOUNT = FluidConstants.BLOCK / 4;
    public static final FluidEntry<SimpleFlowableFluid.Flowing> FRYING_OIL =
            REGISTRATE.standardFluid("frying_oil")
                    .lang("Frying Oil")
                    .tag(FluidTags.WATER)
                    .renderType(() -> RenderType::translucent)
                    .fluidProperties(p -> p.levelDecreasePerBlock(1)
                            .tickRate(5)
                            .flowSpeed(2)
                            .blastResistance(100f))
                    .fluidAttributes(() -> new CreateBitterballenAttributeHandler(1500, 500))
                    .register();
    public static final FluidEntry<SimpleFlowableFluid.Flowing> KETCHUP =
            REGISTRATE.standardFluid("ketchup")
                    .lang("Ketchup")
                    .tag(FluidTags.WATER)
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .flowSpeed(3)
                            .blastResistance(100f))
                    .fluidAttributes(() -> new CreateBitterballenAttributeHandler(1500, 1400))
                    .register();
    public static final FluidEntry<SimpleFlowableFluid.Flowing> MAYONNAISE =
            REGISTRATE.standardFluid("mayonnaise")
                    .lang("Mayonnaise")
                    .tag(FluidTags.WATER)
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .flowSpeed(3)
                            .blastResistance(100f))
                    .fluidAttributes(() -> new CreateBitterballenAttributeHandler(1500, 1400))
                    .register();
    public static final FluidEntry<SimpleFlowableFluid.Flowing> CURDLED_MILK =
            REGISTRATE.standardFluid("curdled_milk")
                    .lang("Curdled Milk")
                    .tag(FluidTags.WATER)
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .flowSpeed(3)
                            .blastResistance(100f))
                    .fluidAttributes(() -> new CreateBitterballenAttributeHandler(1500, 900))
                    .register();
    public static final FluidEntry<VirtualFluid> STAMPPOT =
            REGISTRATE.virtualFluid("stamppot")
                    .lang("Stamppot")
                    .onRegisterAfter(Registries.ITEM, stamppot -> {
                        Fluid still = stamppot.getSource();
                        FluidStorage.combinedItemApiProvider(ItemRegistry.STAMPPOT_BOWL.get()).register(context ->
                                new FullItemFluidStorage(context, bottle -> ItemVariant.of(BOWL), FluidVariant.of(still), STAMPPOT_AMOUNT));
                        FluidStorage.combinedItemApiProvider(BOWL).register(context ->
                                new EmptyItemFluidStorage(context, bottle -> ItemVariant.of(ItemRegistry.STAMPPOT_BOWL.get()), still, STAMPPOT_AMOUNT));
                    })
                    .register();



    public static void register() {}

    private record CreateBitterballenAttributeHandler(int viscosity, boolean lighterThanAir) implements FluidVariantAttributeHandler {
        private CreateBitterballenAttributeHandler(int viscosity, int density) {
            this(viscosity, density <= 0);
        }

        @Override
        public int getViscosity(FluidVariant variant, @Nullable Level world) {
            return viscosity;
        }

        @Override
        public boolean isLighterThanAir(FluidVariant variant) {
            return lighterThanAir;
        }
    }
    public static void registerFluidInteractions() {
        // fabric: no fluid interaction API, use legacy method
        FluidPlaceBlockCallback.EVENT.register(FluidRegistry::whenFluidsMeet);
    }
    public static BlockState whenFluidsMeet(LevelAccessor world, BlockPos pos, BlockState blockState) {
        FluidState fluidState = blockState.getFluidState();

        if (fluidState.isSource() && FluidHelper.isLava(fluidState.getType()))
            return null;

        for (Direction direction : Iterate.directions) {
            FluidState metFluidState =
                    fluidState.isSource() ? fluidState : world.getFluidState(pos.relative(direction));
            if (!metFluidState.is(FluidTags.WATER))
                continue;
            BlockState lavaInteraction = FluidRegistry.getLavaInteraction(metFluidState);
            if (lavaInteraction == null)
                continue;
            return lavaInteraction;
        }
        return null;
    }
    @Nullable
    public static BlockState getLavaInteraction (FluidState fluidState)  {
        Fluid fluid = fluidState.getType();
        if (fluid.isSame(FRYING_OIL.get())) {
            return BlockRegistry.CRYSTALLISED_OIL
                    .get()
                    .defaultBlockState();

        }
        return null;
    }
}
