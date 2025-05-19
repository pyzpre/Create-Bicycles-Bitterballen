package com.pyzpre.createbitterballen.index;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.pyzpre.createbitterballen.CreateBitterballen.REGISTRATE;

public class FluidRegistry {
    public static final FluidEntry<BaseFlowingFluid.Flowing.Flowing> FRYING_OIL =
            REGISTRATE.standardFluid("frying_oil",
                            TransparentRenderedPlaceableFluidType.create(0xEDC483,
                                    () -> 1f / 8f))
                    .lang("Frying Oil")
                    .properties(b -> b
                            .viscosity(1500)
                            .density(500))
                    .fluidProperties(p -> p
                            .levelDecreasePerBlock(1)
                            .tickRate(5)
                            .slopeFindDistance(5)
                            .explosionResistance(100f))
                    .register();

    public static final FluidEntry<BaseFlowingFluid.Flowing> KETCHUP =
            REGISTRATE.standardFluid("ketchup",
                            SolidRenderedPlaceableFluidType.create(0x9B1C1D,
                                    () -> 1f / 8f))
                    .lang("Ketchup")
                    .properties(b -> b
                            .viscosity(1500)
                            .density(1400))
                    .fluidProperties(p -> p
                            .levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(3)
                            .explosionResistance(100f))
                    .register();
    public static final FluidEntry<BaseFlowingFluid.Flowing> MAYONNAISE =
            REGISTRATE.standardFluid("mayonnaise",
                            SolidRenderedPlaceableFluidType.create(0xC9C79C,
                                    () -> 1f / 8f))
                    .lang("Mayonnaise")
                    .properties(b -> b
                            .viscosity(1500)
                            .density(1400))
                    .fluidProperties(p -> p
                            .levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(3)
                            .explosionResistance(100f))
                    .register();
    public static final FluidEntry<BaseFlowingFluid.Flowing> CURDLED_MILK =
            REGISTRATE.standardFluid("curdled_milk",
                            SolidRenderedPlaceableFluidType.create(0xC9C79C,
                                    () -> 1f / 8f))
                    .lang("Curdled Milk")
                    .properties(b -> b
                            .viscosity(1500)
                            .density(900))
                    .fluidProperties(p -> p
                            .levelDecreasePerBlock(1)
                            .tickRate(5)
                            .slopeFindDistance(4)
                            .explosionResistance(100f))
                    .register();
    public static final FluidEntry<VirtualFluid> STAMPPOT =
            REGISTRATE.virtualFluid("stamppot")
                    .lang("Stamppot")
                    .register();

    public static void register() {}
    public static void registerFluidInteractions() {
        FluidInteractionRegistry.addInteraction(NeoForgeMod.LAVA_TYPE.value(), new FluidInteractionRegistry.InteractionInformation(
                FRYING_OIL.get().getFluidType(),
                fluidState -> {
                    if (fluidState.isSource()) {
                        return Blocks.OBSIDIAN.defaultBlockState();
                    } else {
                        return BlockRegistry.CRYSTALLISED_OIL.get()
                                .defaultBlockState();
                    }
                }
        ));
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
        @SuppressWarnings("removal")
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
            public @NotNull Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level,
                                                    int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                Vector3f customFogColor = TintedFluidType.this.getCustomFogColor();
                return customFogColor == null ? fluidFogColor : customFogColor;
            }

            @Override
            public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick,
                                        float nearDistance, float farDistance, FogShape shape) {
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

    protected Vector3f getCustomFogColor() {
        return null;
    }

    protected float getFogDistanceModifier() {
        return 1f;
    }

}

    private static class TransparentRenderedPlaceableFluidType extends TintedFluidType {

        private Vector3f fogColor;
        private Supplier<Float> fogDistance;

        public static FluidBuilder.FluidTypeFactory create(int fogColor, Supplier<Float> fogDistance) {
            return (p, s, f) -> {
                TransparentRenderedPlaceableFluidType fluidType = new TransparentRenderedPlaceableFluidType(p, s, f);
                fluidType.fogColor = new Color(fogColor, true).asVectorF();
                fluidType.fogDistance = fogDistance;
                return fluidType;
            };
        }

        private TransparentRenderedPlaceableFluidType(Properties properties, ResourceLocation stillTexture,
                                                ResourceLocation flowingTexture) {
            super(properties, stillTexture, flowingTexture);
        }

        @Override
        protected int getTintColor(FluidStack stack) {
            return NO_TINT;
        }

        @Override
        public int getTintColor(FluidState state, BlockAndTintGetter world, BlockPos pos) {
            return 0xCCFFFFFF; // 80% transparent white
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

    private static class SolidRenderedPlaceableFluidType extends TintedFluidType {

        private Vector3f fogColor;
        private Supplier<Float> fogDistance;

        public static FluidBuilder.FluidTypeFactory create(int fogColor, Supplier<Float> fogDistance) {
            return (p, s, f) -> {
                SolidRenderedPlaceableFluidType fluidType = new SolidRenderedPlaceableFluidType(p, s, f);
                fluidType.fogColor = new Color(fogColor, false).asVectorF();
                fluidType.fogDistance = fogDistance;
                return fluidType;
            };
        }

        private SolidRenderedPlaceableFluidType(Properties properties, ResourceLocation stillTexture,
                                                ResourceLocation flowingTexture) {
            super(properties, stillTexture, flowingTexture);
        }

        @Override
        protected int getTintColor(FluidStack stack) {
            return NO_TINT;
        }

        @Override
        public int getTintColor(FluidState state, BlockAndTintGetter world, BlockPos pos) {
            return 0x00ffffff;
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
}
