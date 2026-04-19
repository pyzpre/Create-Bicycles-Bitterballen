package com.pyzpre.createbitterballen.block.mechanicalfryer;


import com.mojang.serialization.MapCodec;
import com.pyzpre.createbitterballen.index.RecipeRegistry;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class DeepFryingRecipe extends BasinRecipe {
    public static final MapCodec<DeepFryingRecipe> CODEC =
            ProcessingRecipe.codec(
                    (ProcessingRecipe.Factory<ProcessingRecipeParams, DeepFryingRecipe>) DeepFryingRecipe::new,
                    ProcessingRecipeParams.CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, DeepFryingRecipe> STREAM_CODEC =
            ProcessingRecipe.streamCodec(
                    (ProcessingRecipe.Factory<ProcessingRecipeParams, DeepFryingRecipe>) DeepFryingRecipe::new,
                    ProcessingRecipeParams.STREAM_CODEC);
    public static boolean match(BasinBlockEntity basin, MechanicalFryerEntity fryer, Recipe<?> recipe) {
        FilteringBehaviour filter = basin.getFilter();
        if (filter == null)
            return false;

        boolean filterTest = filter.test(recipe.getResultItem(basin.getLevel()
                .registryAccess()));
        if (recipe instanceof BasinRecipe) {
            BasinRecipe basinRecipe = (BasinRecipe) recipe;
            if (basinRecipe.getRollableResults()
                    .isEmpty()
                    && !basinRecipe.getFluidResults()
                    .isEmpty())
                filterTest = filter.test(basinRecipe.getFluidResults()
                        .get(0));
        }

        if (!filterTest)
            return false;

        return apply(basin, fryer, recipe, true);
    }
    public static boolean apply(SmartInventory inv, BasinBlockEntity basin, MechanicalFryerEntity fryer, Recipe<?> recipe) {
        boolean isDeepFryingRecipe = recipe instanceof DeepFryingRecipe;

        IItemHandler availableItems = fryer.inputInv;
        IFluidHandler availableFluids = basin.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, basin.getBlockPos(), null);

        if (availableItems == null || availableFluids == null) {
            return false;
        }

        BlazeBurnerBlock.HeatLevel heat = BasinBlockEntity.getHeatLevelOf(basin.getLevel().getBlockState(basin.getBlockPos().below(1)));
        if (isDeepFryingRecipe && !((DeepFryingRecipe) recipe).getRequiredHeat().testBlazeBurner(heat)) {
            return false;
        }

        ItemStack inputStack = availableItems.getStackInSlot(0);
        int itemCount = inputStack.getCount(); // Total items available to process

        if (itemCount <= 0) {
            return false; // No items to process
        }

        // Calculate the maximum number of items that can be processed based on fluid requirements
        List<SizedFluidIngredient> fluidIngredients = ((DeepFryingRecipe) recipe).getFluidIngredients();
        int maxProcessableItems = itemCount; // Start with item count, will be limited by fluids

        for (SizedFluidIngredient fluidIngredient : fluidIngredients) {
            int requiredAmount = fluidIngredient.amount();
            int totalMatchingAmount = 0;

            // Calculate total fluid amount available for this ingredient
            for (int tank = 0; tank < availableFluids.getTanks(); tank++) {
                FluidStack fluidInTank = availableFluids.getFluidInTank(tank);

                // Use fluidIngredient.test(fluidInTank) to include NBT data
                if (fluidIngredient.test(fluidInTank)) {
                    totalMatchingAmount += fluidInTank.getAmount();
                }
            }

            // Calculate the maximum items that can be processed with this fluid ingredient
            maxProcessableItems = Math.min(maxProcessableItems, totalMatchingAmount / requiredAmount);
        }

        if (maxProcessableItems <= 0) {
            return false; // Not enough fluids to process any items
        }

        // Consume fluids and items for the maximum processable items
        for (SizedFluidIngredient fluidIngredient : fluidIngredients) {
            int amountToConsume = fluidIngredient.amount() * maxProcessableItems;

            for (int tank = 0; tank < availableFluids.getTanks(); tank++) {
                FluidStack fluidInTank = availableFluids.getFluidInTank(tank);

                if (fluidIngredient.test(fluidInTank)) {
                    // call copyWithAmount() on fluidInTank!
                    int drainedAmount = availableFluids.drain(fluidInTank.copyWithAmount(amountToConsume), IFluidHandler.FluidAction.EXECUTE).getAmount();

                    amountToConsume -= drainedAmount;

                    if (amountToConsume <= 0)
                        break;
                }
            }
        }


        // Consume items and generate outputs
        inputStack.shrink(maxProcessableItems);

        // Generate and handle outputs
        List<ItemStack> recipeOutputItems = generateOutputs(recipe, basin, maxProcessableItems); // Multiply outputs by processed items
        for (ItemStack itemStack : recipeOutputItems) {
            ItemStack remaining = ItemHandlerHelper.insertItemStacked(fryer.outputInv, itemStack, false);
            if (!remaining.isEmpty()) {
                return false; // Output inventory is full, stop processing
            }
        }

        // Update states
        basin.setChanged();
        basin.sendData();
        fryer.sendData();
        fryer.setChanged();

        return true;
    }




    private static boolean apply(BasinBlockEntity basin, MechanicalFryerEntity fryer, Recipe<?> recipe, boolean test) {
        boolean isDeepFryingRecipe = recipe instanceof DeepFryingRecipe;

        IItemHandler availableItems = fryer.inputInv;
        IFluidHandler availableFluids = basin.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, basin.getBlockPos(), null);

        if (availableItems == null || availableFluids == null) {
            return false;
        }

        BlazeBurnerBlock.HeatLevel heat = BasinBlockEntity.getHeatLevelOf(basin.getLevel().getBlockState(basin.getBlockPos().below(1)));
        if (isDeepFryingRecipe && !((DeepFryingRecipe) recipe).getRequiredHeat().testBlazeBurner(heat)) {
            return false;
        }

        ItemStack inputStack = availableItems.getStackInSlot(0);
        int itemCount = inputStack.getCount(); // Get initial item count

        if (itemCount <= 0) {
            return false;  // No items to process
        }

        List<SizedFluidIngredient> fluidIngredients = isDeepFryingRecipe
                ? ((DeepFryingRecipe) recipe).getFluidIngredients()
                : Collections.emptyList();

        // Calculate the maximum number of items that can be processed based on fluid requirements
        int maxProcessableItems = itemCount; // Start with item count, will be limited by fluids
        for (SizedFluidIngredient fluidIngredient : fluidIngredients) {
            int requiredAmount = fluidIngredient.amount();
            int totalMatchingAmount = 0;

            // Calculate total fluid amount available for this ingredient
            for (int tank = 0; tank < availableFluids.getTanks(); tank++) {
                FluidStack fluidInTank = availableFluids.getFluidInTank(tank);

                if (fluidIngredient.test(fluidInTank)) {
                    totalMatchingAmount += fluidInTank.getAmount();
                }
            }

            maxProcessableItems = Math.min(maxProcessableItems, totalMatchingAmount / requiredAmount);
        }

        if (maxProcessableItems <= 0) {
            return false; // Not enough fluids to process any items
        }

        // Process multiple items if conditions allow
        inputStack.shrink(maxProcessableItems);

        for (SizedFluidIngredient fluidIngredient : fluidIngredients) {
            int amountToConsume = fluidIngredient.amount() * maxProcessableItems;

            for (int tank = 0; tank < availableFluids.getTanks(); tank++) {
                FluidStack fluidInTank = availableFluids.getFluidInTank(tank);

                if (fluidIngredient.test(fluidInTank)) {
                    int drainedAmount = availableFluids.drain(
                            fluidInTank.copyWithAmount(amountToConsume),
                            IFluidHandler.FluidAction.EXECUTE
                    ).getAmount();

                    amountToConsume -= drainedAmount;

                    if (amountToConsume <= 0)
                        break;
                }
            }
        }


        // Generate and handle outputs
        List<ItemStack> recipeOutputItems = generateOutputs(recipe, basin, maxProcessableItems); // Process multiple items
        for (ItemStack itemStack : recipeOutputItems) {
            ItemStack remaining = ItemHandlerHelper.insertItemStacked(fryer.outputInv, itemStack, false);
            if (!remaining.isEmpty()) {
                return false;
            }
        }

        basin.setChanged();
        basin.sendData();
        fryer.sendData();
        fryer.setChanged();

        return true;
    }


    private static List<ItemStack> generateOutputs(Recipe<?> recipe, BasinBlockEntity basin, int quantity) {
        List<ItemStack> outputs = new ArrayList<>();

        if (recipe instanceof BasinRecipe basinRecipe) {
            // Use the new rollResults(RandomSource) method
            for (ItemStack result : basinRecipe.rollResults(basin.getLevel().random)) {
                ItemStack stack = result.copy();
                stack.setCount(result.getCount() * quantity);
                outputs.add(stack);
            }

            // Handle output fluids and ensure state is synced
            List<FluidStack> fluidResults = basinRecipe.getFluidResults();
            for (FluidStack fluidResult : fluidResults) {
                FluidStack outputFluidStack = fluidResult.copy();
                outputFluidStack.setAmount(outputFluidStack.getAmount() * quantity);
                if (inputFluidsToBasin(basin, outputFluidStack, false)) {
                    basin.setChanged();
                    basin.sendData();  // Sync fluid state
                }
            }
        } else {
            ItemStack result = recipe.getResultItem(basin.getLevel().registryAccess()).copy();
            result.setCount(result.getCount() * quantity);
            outputs.add(result);
        }

        return outputs;
    }





    public static boolean consumeFluids(SizedFluidIngredient fluidIngredient,
                                        IFluidHandler fluidHandler,
                                        BasinBlockEntity basin,
                                        boolean simulate) {
        int amountRequired = fluidIngredient.amount();

        for (FluidStack matchingFluid : fluidIngredient.getFluids()) {
            FluidStack fluidToDrain = matchingFluid.copyWithAmount(amountRequired);

            FluidStack drained = fluidHandler.drain(
                    fluidToDrain,
                    simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE
            );

            if (!drained.isEmpty() && FluidStack.matches(drained, fluidToDrain)) {
                if (!simulate) {
                    basin.setChanged();
                    basin.sendData();
                }
                return true;
            }
        }

        return false;
    }







    private static boolean inputFluidsToBasin(BasinBlockEntity basin, FluidStack fluidStack, boolean simulate) {
        IFluidHandler basinFluidHandler = basin.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, basin.getBlockPos(), null);
        if (basinFluidHandler == null) return false;

        int filled = basinFluidHandler.fill(fluidStack, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);

        // Sync after modifying the basin's fluid contents
        if (!simulate && filled == fluidStack.getAmount()) {
            basin.setChanged();
            basin.sendData();
        }

        return filled == fluidStack.getAmount();
    }



    protected DeepFryingRecipe(IRecipeTypeInfo type, ProcessingRecipeParams params) {
        super(type, params);
    }

    public DeepFryingRecipe(ProcessingRecipeParams params) {
        this(RecipeRegistry.DEEP_FRYING, params);
    }




    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 1;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 2;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 2;
    }

    @Override
    protected boolean canRequireHeat() {
        return true;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    @Override
    public boolean matches(RecipeInput input, @Nonnull Level worldIn) {
        if (input.isEmpty())
            return false;

        // You can get the first item via input.getItems().get(0)
        return ingredients.get(0).test(input.getItem(0));
    }


    // Ensure hasMatchingNBT is accessible or replicate it here
    private static boolean hasMatchingComponents(SizedFluidIngredient fluidIngredient, FluidStack fluidInTank) {
        for (FluidStack matchingFluid : fluidIngredient.getFluids()) {
            boolean bothHaveComponents =
                    !fluidInTank.getComponents().isEmpty() && !matchingFluid.getComponents().isEmpty();
            boolean neitherHaveComponents =
                    fluidInTank.getComponents().isEmpty() && matchingFluid.getComponents().isEmpty();

            if (bothHaveComponents) {
                if (fluidInTank.getComponents().equals(matchingFluid.getComponents())) {
                    return true;
                }
            } else if (neitherHaveComponents) {
                return true;
            }
        }
        return false;
    }





}
