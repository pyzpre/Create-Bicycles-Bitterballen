package com.pyzpre.createbitterballen.block.mechanicalfryer;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pyzpre.createbitterballen.index.RecipeRegistry;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.ArrayList;
import java.util.List;

public class DeepFryingRecipeSerializer extends ProcessingRecipeSerializer<DeepFryingRecipe> {

    public static final DeepFryingRecipeSerializer INSTANCE = new DeepFryingRecipeSerializer();

    private DeepFryingRecipeSerializer() {
        super(DeepFryingRecipe::new);
    }

    @Override
    public MapCodec<DeepFryingRecipe> codec() {
        return RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                                // 1) ingredients: mix of item-ingredients & fluid-ingredients
                                Codec.either(Ingredient.CODEC, FluidIngredient.CODEC)
                                        .listOf()
                                        .fieldOf("ingredients")
                                        .forGetter(r -> {
                                            List<Either<Ingredient, FluidIngredient>> both = new ArrayList<>();
                                            r.getIngredients().forEach(i -> both.add(Either.left(i)));
                                            r.getFluidIngredients().forEach(f -> both.add(Either.right(f)));
                                            return both;
                                        }),

                                // 2) results: mix of fluid-results & item-outputs
                                Codec.either(FluidStack.CODEC, ProcessingOutput.CODEC)
                                        .listOf()
                                        .fieldOf("results")
                                        .forGetter(r -> {
                                            List<Either<FluidStack, ProcessingOutput>> both = new ArrayList<>();
                                            r.getFluidResults().forEach(f -> both.add(Either.left(f)));
                                            r.getRollableResults().forEach(o -> both.add(Either.right(o)));
                                            return both;
                                        }),

                                // 3) processing time
                                ExtraCodecs.NON_NEGATIVE_INT
                                        .optionalFieldOf("processing_time", 0)
                                        .forGetter(ProcessingRecipe::getProcessingDuration),

                                // 4) heat requirement
                                HeatCondition.CODEC
                                        .optionalFieldOf("heat_requirement", HeatCondition.NONE)
                                        .forGetter(ProcessingRecipe::getRequiredHeat)
                        )
                        .apply(instance, (ingredients, results, time, heat) -> {
                            // Build a new recipe from the codec data
                            ProcessingRecipeBuilder<DeepFryingRecipe> builder =
                                    new ProcessingRecipeBuilder<>(INSTANCE.getFactory(), RecipeRegistry.DEEP_FRYING.id);

                            // split inputs back out
                            NonNullList<Ingredient>  items  = NonNullList.create();
                            NonNullList<FluidIngredient> fluids = NonNullList.create();
                            for (Either<Ingredient, FluidIngredient> e : ingredients) {
                                e.left().ifPresent(items::add);
                                e.right().ifPresent(fluids::add);
                            }

                            NonNullList<ProcessingOutput> outputs = NonNullList.create();
                            NonNullList<FluidStack> fluidOuts     = NonNullList.create();
                            for (Either<FluidStack, ProcessingOutput> e : results) {
                                e.left().ifPresent(fluidOuts::add);
                                e.right().ifPresent(outputs::add);
                            }

                            builder.withItemIngredients(items)
                                    .withItemOutputs(outputs)
                                    .withFluidIngredients(fluids)
                                    .withFluidOutputs(fluidOuts)
                                    .duration(time)
                                    .requiresHeat(heat);

                            return builder.build();
                        })
        );
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, DeepFryingRecipe> streamCodec() {
        return super.STREAM_CODEC;
    }
}
