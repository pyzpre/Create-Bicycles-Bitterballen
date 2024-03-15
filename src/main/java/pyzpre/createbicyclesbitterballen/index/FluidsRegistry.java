package pyzpre.createbicyclesbitterballen.index;



import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.decoration.palettes.AllPaletteBlocks;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.tterrag.registrate.fabric.SimpleFlowableFluid;
import com.tterrag.registrate.util.entry.FluidEntry;
import io.github.fabricators_of_create.porting_lib.event.common.FluidPlaceBlockCallback;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;


import javax.annotation.Nullable;


import static com.mojang.text2speech.Narrator.LOGGER;
import static net.minecraft.world.item.Items.BOWL;
import static pyzpre.createbicyclesbitterballen.CreateBitterballen.REGISTRATE;

public class FluidsRegistry {
    public static final long STAMPPOT_AMOUNT = FluidConstants.BLOCK / 4;
    public static final FluidEntry<SimpleFlowableFluid.Flowing> FRYING_OIL =
            REGISTRATE.fluid("frying_oil", new ResourceLocation("create_bic_bit:block/fryingoil_still"), new ResourceLocation("create_bic_bit:block/fryingoil_flow"))
                    .lang("Frying Oil")
                    .tag(FluidTags.WATER)
                    .fluidProperties(p -> p.levelDecreasePerBlock(1)
                            .tickRate(5)
                            .flowSpeed(2)
                            .blastResistance(100f))
                    .fluidAttributes(() -> new CreateBitterballenAttributeHandler("fluid.create_bic_bit.frying_oil", 1500, 1400))
                    .register();
    public static final FluidEntry<SimpleFlowableFluid.Flowing> KETCHUP =
            REGISTRATE.fluid("ketchup", new ResourceLocation("create_bic_bit:block/ketchup_still"), new ResourceLocation("create_bic_bit:block/ketchup_flow"))
                    .lang("Ketchup")
                    .tag(FluidTags.WATER)
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .flowSpeed(3)
                            .blastResistance(100f))
                    .fluidAttributes(() -> new CreateBitterballenAttributeHandler("fluid.create_bic_bit.ketchup", 1500, 1400))
                    .register();
    public static final FluidEntry<SimpleFlowableFluid.Flowing> MAYONNAISE =
            REGISTRATE.fluid("mayonnaise", new ResourceLocation("create_bic_bit:block/mayo_still"), new ResourceLocation("create_bic_bit:block/mayo_flow"))
                    .lang("Mayonnaise")
                    .tag(FluidTags.WATER)
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .flowSpeed(3)
                            .blastResistance(100f))
                    .fluidAttributes(() -> new CreateBitterballenAttributeHandler("fluid.create_bic_bit.ketchup", 1500, 1400))
                    .register();
    public static final FluidEntry<VirtualFluid> STAMPPOT =
            REGISTRATE.virtualFluid("stamppot", new ResourceLocation("create_bic_bit:block/stamppot_still"), new ResourceLocation("create_bic_bit:block/stamppot_still"))
                    .lang("Stamppot")
                    .onRegisterAfter(Registries.ITEM, stamppot -> {
                        Fluid still = stamppot.getSource();
                        FluidStorage.combinedItemApiProvider(CreateBicBitModItems.STAMPPOT_BOWL.get()).register(context ->
                                new FullItemFluidStorage(context, bottle -> ItemVariant.of(BOWL), FluidVariant.of(still), STAMPPOT_AMOUNT));
                        FluidStorage.combinedItemApiProvider(BOWL).register(context ->
                                new EmptyItemFluidStorage(context, bottle -> ItemVariant.of(CreateBicBitModItems.STAMPPOT_BOWL.get()), still, STAMPPOT_AMOUNT));
                    })
                    .register();



    public static void register() {}

    private record CreateBitterballenAttributeHandler(Component name, int viscosity, boolean lighterThanAir) implements FluidVariantAttributeHandler {
    private CreateBitterballenAttributeHandler(String key, int viscosity, int density) {
        this(Component.translatable(key), viscosity, density <= 0);
    }


    @Override
    public Component getName(FluidVariant fluidVariant) {
        return name.copy();
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
        LOGGER.info("registerFluidInteractions method called");
        // fabric: no fluid interaction API, use legacy method
        FluidPlaceBlockCallback.EVENT.register(FluidsRegistry::whenFluidsMeet);
    }
    public static BlockState whenFluidsMeet(LevelAccessor world, BlockPos pos, BlockState blockState) {
        LOGGER.info("whenFluidsMeet method called");
        FluidState fluidState = blockState.getFluidState();

        if (fluidState.isSource() && FluidHelper.isLava(fluidState.getType()))
            return null;

        for (Direction direction : Iterate.directions) {
            FluidState metFluidState =
                    fluidState.isSource() ? fluidState : world.getFluidState(pos.relative(direction));
            if (!metFluidState.is(FluidTags.WATER))
                continue;
            BlockState lavaInteraction = FluidsRegistry.getLavaInteraction(metFluidState);
            if (lavaInteraction == null)
                continue;
            return lavaInteraction;
        }
        return null;
    }
    @Nullable
    public static BlockState getLavaInteraction (FluidState fluidState)  {
        LOGGER.info("getLavaInteraction method called");
        Fluid fluid = fluidState.getType();
        if (fluid.isSame(FRYING_OIL.get())) {
            LOGGER.info("Scoria Placed");
            return BlockRegistry.CRYSTALLISED_OIL
                    .get()
                    .defaultBlockState();

        }
        LOGGER.info("Scoria Not Placed");
        return null;
    }
}