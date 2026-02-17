package com.pyzpre.createbitterballen.index;

import com.pyzpre.createbitterballen.block.mechanicalfryer.DeepFryingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public enum RecipeRegistry implements IRecipeTypeInfo {

    DEEP_FRYING(DeepFryingRecipe::new);

    private final ResourceLocation id = new ResourceLocation("create_bic_bit");
    private final RegistryObject<RecipeSerializer<?>> serializerObject;
    @Nullable
    private final RegistryObject<RecipeType<?>> typeObject;
    private final Supplier<RecipeType<?>> type;

    RecipeRegistry(Supplier<RecipeSerializer<?>> serializerSupplier) {
        String name = Lang.asId(name());
        serializerObject = Registers.SERIALIZER_REGISTER.register(name, serializerSupplier);
        typeObject = Registers.TYPE_REGISTER.register(name, () -> new RecipeType<>() {});
        type = typeObject;
    }
    RecipeRegistry(ProcessingRecipeBuilder.ProcessingRecipeFactory<?> processingFactory) {
        this(() -> new ProcessingRecipeSerializer<>(processingFactory));
    }
    public static void register() {
        Registers.SERIALIZER_REGISTER.register();
        Registers.TYPE_REGISTER.register();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public <T extends RecipeSerializer<?>> T getSerializer() {

        return (T) serializerObject.get();
    }

    @Override
    public <T extends RecipeType<?>> T getType() {

        return (T) type.get();
    }

    public <C extends Container, T extends Recipe<C>> Optional<T> find(C inv, Level world) {
        return world.getRecipeManager()
                .getRecipeFor(getType(), inv, world);
    }

    public RecipeType<DeepFryingRecipe> get() {
        return (RecipeType<DeepFryingRecipe>) type.get();
    }


    private static class Registers {
        private static final LazyRegistrar<RecipeSerializer<?>> SERIALIZER_REGISTER = LazyRegistrar.create(BuiltInRegistries.RECIPE_SERIALIZER, "create_bic_bit");
        private static final LazyRegistrar<RecipeType<?>> TYPE_REGISTER = LazyRegistrar.create(Registries.RECIPE_TYPE, "create_bic_bit");
    }
}
