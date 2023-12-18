package createbicyclesbitterballen.fluid;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.util.entry.FluidEntry;

import createbicyclesbitterballen.index.BlockRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.function.Consumer;
import java.util.function.Supplier;

import static createbicyclesbitterballen.CreateBicBitMod.REGISTRATE;

public class FluidsRegistry {
    private static final float FRYING_OIL_FOG_DISTANCE = 0.2f;
    public static final ResourceLocation FRYINGOIL_STILL_RL = new ResourceLocation("create_bic_bit", "block/fryingoil_still");
    public static final ResourceLocation FRYINGOIL_FLOW_RL = new ResourceLocation("create_bic_bit", "block/fryingoil_flow");

    private static class CustomSolidRenderedFluidType extends TintedFluidType {
        private Vector3f fogColor;
        private Supplier<Float> fogDistance;

        public CustomSolidRenderedFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
            super(properties, stillTexture, flowingTexture);
        }

        @Override
        protected int getTintColor(FluidStack stack) {
            return NO_TINT;
        }

        @Override
        protected int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
            return NO_TINT;
        }

        @Override
        protected Vector3f getCustomFogColor() {
            return fogColor;
        }

        @Override
        protected float getFogDistanceModifier() {
            return fogDistance.get();
        }
    }


    private static final FluidBuilder.FluidTypeFactory FRYING_OIL_TYPE_FACTORY = (properties, still, flowing) -> {
        CustomSolidRenderedFluidType fluidType = new CustomSolidRenderedFluidType(properties, FRYINGOIL_STILL_RL, FRYINGOIL_FLOW_RL);
        fluidType.fogColor = new Vector3f(237 / 255f, 196 / 255f, 131 / 255f);
        fluidType.fogDistance = () -> FRYING_OIL_FOG_DISTANCE;
        return fluidType;
    };


    public static final FluidEntry<ForgeFlowingFluid.Flowing> FRYING_OIL =
            REGISTRATE.standardFluid("frying_oil", FRYING_OIL_TYPE_FACTORY)
                    .lang("Frying Oil")
                    .properties(b -> b.viscosity(1500).density(500))
                    .fluidProperties(p -> p
                            .levelDecreasePerBlock(1)
                            .tickRate(5)
                            .slopeFindDistance(5)
                            .explosionResistance(100f))
                    .register();

    public static final ResourceLocation STAMPPOT_STILL_RL = new ResourceLocation("create_bic_bit", "block/stamppot");
    public static final ResourceLocation STAMPPOT_FLOW_RL = new ResourceLocation("create_bic_bit", "block/stamppot");
    public static final FluidEntry<VirtualFluid> STAMPPOT =
            REGISTRATE.virtualFluid("stamppot", STAMPPOT_STILL_RL, STAMPPOT_FLOW_RL, CreateRegistrate::defaultFluidType, VirtualFluid::new)
                    .lang("Stamppot")
                    .register();


    public static void register() {}
    public static void registerFluidInteractions() {
        FluidInteractionRegistry.addInteraction(ForgeMod.LAVA_TYPE.get(), new FluidInteractionRegistry.InteractionInformation(
                FRYING_OIL.get().getFluidType(),
                fluidState -> {
                    if (fluidState.isSource()) {
                        return Blocks.OBSIDIAN.defaultBlockState();
                    } else {
                        return BlockRegistry.CRYSTALLISED_OIL
                                .get()
                                .defaultBlockState();
                    }
                }
        ));
    }

    @Nullable
    public static BlockState getLavaInteraction(FluidState fluidState) {
        Fluid fluid = fluidState.getType();
        if (fluid.isSame(FRYING_OIL.get()))
            return BlockRegistry.CRYSTALLISED_OIL
                    .get()
                    .defaultBlockState();
        return null;
    }

    public static abstract class TintedFluidType extends FluidType {
        protected static final int NO_TINT = 0xffffffff;
        private ResourceLocation stillTexture;
        private ResourceLocation flowingTexture;

        public TintedFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
            super(properties);
            this.stillTexture = stillTexture;
            this.flowingTexture = flowingTexture;
        }

        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return stillTexture;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return flowingTexture;
                }

                @Override
                public int getTintColor(FluidStack stack) {
                    return TintedFluidType.this.getTintColor(stack);
                }

                @Override
                public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                    return TintedFluidType.this.getTintColor(state, getter, pos);
                }

                @Override
                public @NotNull Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                    Vector3f customFogColor = TintedFluidType.this.getCustomFogColor();
                    return customFogColor == null ? fluidFogColor : customFogColor;
                }

                @Override
                public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
                    float modifier = TintedFluidType.this.getFogDistanceModifier();
                    float baseWaterFog = 96.0f;
                    if (modifier != 1f) {
                        RenderSystem.setShaderFogShape(FogShape.CYLINDER);
                        RenderSystem.setShaderFogStart(-8);
                        RenderSystem.setShaderFogEnd(baseWaterFog * modifier);
                    }
                }
            });
        }

        protected abstract int getTintColor(FluidStack stack);

        protected abstract int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos);

        protected abstract Vector3f getCustomFogColor();

        protected abstract float getFogDistanceModifier();
    }

    @SubscribeEvent
    public void onPlayerStep(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        Level world = player.getCommandSenderWorld(); // Access the level directly

        if (isInFryingOil(player, world) && world.isRaining()) {
            applyLevitationEffect(player);

        }
    }

    private boolean isInFryingOil(Player player, Level world) {
        BlockPos pos = player.blockPosition();
        FluidState fluidState = world.getFluidState(pos);

        // Get the fluid type from the FluidState
        Fluid fluid = fluidState.getType();

        // Check if the fluid at the position is the same as your custom frying oil fluid
        return fluid.isSame(FluidsRegistry.FRYING_OIL.get());
    }

    private void applyLevitationEffect(Player player) {
        MobEffectInstance levitation = new MobEffectInstance(MobEffects.LEVITATION, 100, 0);
        player.addEffect(levitation);
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            grantAdvancementCriterion(serverPlayer, "create_bic_bit:step_3", "got_levitation");
        }
    }
    private void grantAdvancementCriterion(ServerPlayer player, String advancementID, String criterionKey) {
        PlayerAdvancements playerAdvancements = player.getAdvancements();
        Advancement advancement = player.server.getAdvancements().getAdvancement(new ResourceLocation(advancementID));

        if (advancement != null && advancement.getCriteria().containsKey(criterionKey)) {
            AdvancementProgress advancementProgress = playerAdvancements.getOrStartProgress(advancement);

            if (!advancementProgress.isDone()) {
                playerAdvancements.award(advancement, criterionKey);
            }
        }
    }

}
