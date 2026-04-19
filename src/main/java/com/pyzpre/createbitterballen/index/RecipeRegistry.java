package com.pyzpre.createbitterballen.index;

import com.pyzpre.createbitterballen.CreateBitterballen;
import com.pyzpre.createbitterballen.block.mechanicalfryer.DeepFryingRecipe;
import com.pyzpre.createbitterballen.block.mechanicalfryer.DeepFryingRecipeSerializer;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public enum RecipeRegistry implements IRecipeTypeInfo, StringRepresentable {
    DEEP_FRYING(() -> DeepFryingRecipeSerializer.INSTANCE);

    public final ResourceLocation id;
    private final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> serializerObject;
    private final DeferredHolder<RecipeType<?>, RecipeType<?>>       typeObject;
    private final Supplier<RecipeType<?>>                            type;

    RecipeRegistry(Supplier<RecipeSerializer<?>> serializerSupplier) {
        String name = Lang.asId(name());
        id = CreateBitterballen.asResource(name);

        // register your custom serializer
        serializerObject = Registers.SERIALIZER_REGISTER.register(name, serializerSupplier);

        // register the recipe-type so JSON parser knows “create_bic_bit:deep_frying”
        typeObject       = Registers.TYPE_REGISTER      .register(name, () -> RecipeType.simple(id));
        type             = typeObject;
    }

    public static void register(IEventBus modEventBus) {
        Registers.SERIALIZER_REGISTER.register(modEventBus);
        Registers.TYPE_REGISTER      .register(modEventBus);
    }

    @Override public RecipeSerializer<?> getSerializer() { return serializerObject.get(); }
    @Override public <I extends RecipeInput, R extends Recipe<I>> RecipeType<R> getType() {
        @SuppressWarnings("unchecked")
        RecipeType<R> t = (RecipeType<R>) type.get();
        return t;
    }
    @SuppressWarnings("unchecked")
    public RecipeType<DeepFryingRecipe> get() {
        return (RecipeType<DeepFryingRecipe>) type.get();
    }
    @Override public ResourceLocation getId() { return id; }
    @Override public @NotNull String getSerializedName() { return id.toString(); }

    private static class Registers {
        static final DeferredRegister<RecipeSerializer<?>> SERIALIZER_REGISTER =
                DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, "create_bic_bit");
        static final DeferredRegister<RecipeType<?>>       TYPE_REGISTER       =
                DeferredRegister.create(Registries.RECIPE_TYPE,      "create_bic_bit");
    }
}
