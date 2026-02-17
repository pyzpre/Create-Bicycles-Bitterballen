package com.pyzpre.createbitterballen.block.mechanicalfryer;


import com.pyzpre.createbitterballen.index.RecipeRegistry;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class DeepFryingRecipe extends BasinRecipe {

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

        SmartInventory availableItems = fryer.inputInv;
        Storage<FluidVariant> availableFluids = basin.getFluidStorage(null);

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
        List<FluidIngredient> fluidIngredients = ((DeepFryingRecipe) recipe).getFluidIngredients();
        int maxProcessableItems = itemCount; // Start with item count, will be limited by fluids

        for (FluidIngredient fluidIngredient : fluidIngredients) {
            long requiredAmount = fluidIngredient.getRequiredAmount();
            int totalMatchingAmount = 0;

            // Calculate total fluid amount available for this ingredient
            for (StorageView<FluidVariant> view : availableFluids.nonEmptyViews()) {
                FluidStack fluidInTank = new FluidStack(view.getResource(), view.getAmount());

                // Use fluidIngredient.test(fluidInTank) to include NBT data
                if (fluidIngredient.test(fluidInTank)) {
                    totalMatchingAmount += fluidInTank.getAmount();
                }
            }

            // Calculate the maximum items that can be processed with this fluid ingredient
            maxProcessableItems = (int) (Math.min(maxProcessableItems, totalMatchingAmount / requiredAmount));
        }

        if (maxProcessableItems <= 0) {
            return false; // Not enough fluids to process any items
        }
        try (Transaction t = Transaction.openOuter()) {
            // Consume fluids and items for the maximum processable items
            for (FluidIngredient fluidIngredient : fluidIngredients) {
                long amountToConsume = fluidIngredient.getRequiredAmount() * maxProcessableItems;
                for (StorageView<FluidVariant> view : availableFluids.nonEmptyViews()) {
                    FluidStack fluidInTank = new FluidStack(view.getResource(), view.getAmount());
                    if (fluidIngredient.test(fluidInTank)) {
                        long drainedAmount = view.extract(view.getResource(), amountToConsume, t);

                        amountToConsume -= drainedAmount;
                        if (amountToConsume <= 0) break;
                    }
                }
            }

            // Generate and handle outputs
            List<ItemStack> recipeOutputItems = generateOutputs(recipe, basin, maxProcessableItems); // Multiply outputs by processed items
            for (ItemStack itemStack : recipeOutputItems) {
                long transferred = fryer.outputInv.insert(ItemVariant.of(itemStack), itemStack.getCount(), t);
                if (transferred != itemStack.getCount()) {
                    return false; // Output inventory is full, stop processing
                }
            }

            t.commit();
        }

        // Consume items and generate outputs
        inputStack.shrink((int) maxProcessableItems);

        // Update states
        basin.setChanged();
        basin.sendData();
        fryer.sendData();
        fryer.setChanged();

        return true;
    }




    private static boolean apply(BasinBlockEntity basin, MechanicalFryerEntity fryer, Recipe<?> recipe, boolean test) {
        boolean isDeepFryingRecipe = recipe instanceof DeepFryingRecipe;

        SmartInventory availableItems = fryer.inputInv;
        Storage<FluidVariant> availableFluids = basin.getFluidStorage(null);

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

        List<FluidIngredient> fluidIngredients = isDeepFryingRecipe ? ((DeepFryingRecipe) recipe).getFluidIngredients() : Collections.emptyList();

        // Calculate the maximum number of items that can be processed based on fluid requirements
        int maxProcessableItems = itemCount; // Start with item count, will be limited by fluids
        for (FluidIngredient fluidIngredient : fluidIngredients) {
            long requiredAmount = fluidIngredient.getRequiredAmount();
            int totalMatchingAmount = 0;

            // Calculate total fluid amount available for this ingredient
            for (StorageView<FluidVariant> view : availableFluids.nonEmptyViews()) {
                FluidStack fluidInTank = new FluidStack(view.getResource(), view.getAmount());

                // Use fluidIngredient.test(fluidInTank) to include NBT data
                if (fluidIngredient.test(fluidInTank)) {
                    totalMatchingAmount += fluidInTank.getAmount();
                }
            }

            // Calculate the maximum items that can be processed with this fluid ingredient
            maxProcessableItems = (int) (Math.min(maxProcessableItems, totalMatchingAmount / requiredAmount));
        }

        if (maxProcessableItems <= 0) {
            return false; // Not enough fluids to process any items
        }

        // Process multiple items if conditions allow

        try (Transaction t = Transaction.openOuter()) {
            for (FluidIngredient fluidIngredient : fluidIngredients) {
                long amountToConsume = fluidIngredient.getRequiredAmount() * maxProcessableItems;
                for (StorageView<FluidVariant> view : availableFluids.nonEmptyViews()) {
                    FluidStack fluidInTank = new FluidStack(view.getResource(), view.getAmount());
                    if (fluidIngredient.test(fluidInTank)) {
                        long drainedAmount;
                        drainedAmount = view.extract(view.getResource(), amountToConsume, t);

                        amountToConsume -= drainedAmount;
                        if (amountToConsume <= 0) break;
                    }
                }
            }

            // Generate and handle outputs
            List<ItemStack> recipeOutputItems = generateOutputs(recipe, basin, maxProcessableItems); // Process multiple items
            for (ItemStack itemStack : recipeOutputItems) {
                long transferred = fryer.outputInv.insert(ItemVariant.of(itemStack), itemStack.getCount(), t);
                if (transferred != itemStack.getCount()) {
                    return false; // Output inventory is full, stop processing
                }
            }

            t.commit();
        }

        inputStack.shrink(maxProcessableItems);

        basin.setChanged();
        basin.sendData();
        fryer.sendData();
        fryer.setChanged();

        return true;
    }


    private static List<ItemStack> generateOutputs(Recipe<?> recipe, BasinBlockEntity basin, int quantity) {
        List<ItemStack> outputs = new ArrayList<>();

        if (recipe instanceof BasinRecipe) {
            for (ItemStack result : ((BasinRecipe) recipe).rollResults()) {
                ItemStack stack = result.copy();
                stack.setCount(result.getCount() * quantity);
                outputs.add(stack);
            }

            // Handle output fluids and ensure state is synced
            List<FluidStack> fluidResults = ((BasinRecipe) recipe).getFluidResults();
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




    public static boolean consumeFluids(FluidIngredient fluidIngredient, Storage<FluidVariant> fluidHandler, BasinBlockEntity basin, boolean simulate) {
        long amountRequired = fluidIngredient.getRequiredAmount();

        for (FluidStack matchingFluid : fluidIngredient.getMatchingFluidStacks()) {
            FluidStack fluidToDrain = matchingFluid.copy();
            fluidToDrain.setAmount(amountRequired);

            // Attempt to drain the exact fluid with matching NBT data
            long drained;
            try(Transaction t = Transaction.openOuter()) {
                drained = fluidHandler.extract(fluidToDrain.getType(), fluidToDrain.getAmount(), t);
                if(!simulate && drained == amountRequired) {
                    t.commit();
                }
            }

            if (drained == amountRequired) {
                if (!simulate) {
                    basin.setChanged();
                    basin.sendData();  // Sync fluid state after consumption
                }
                return true;
            }
        }
        return false;  // No matching fluid found in the tanks.
    }





    private static boolean inputFluidsToBasin(BasinBlockEntity basin, FluidStack fluidStack, boolean simulate) {
        Storage<FluidVariant> basinFluidHandler = basin.getFluidStorage(null);
        if (basinFluidHandler == null) return false;

        long filled;
        try(Transaction t = Transaction.openOuter()) {
            filled = basinFluidHandler.insert(fluidStack.getType(), fluidStack.getAmount(), t);
            if(!simulate && filled == fluidStack.getAmount()) {
                t.commit();
            }
        }

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
    public boolean matches(Container inv, @Nonnull Level worldIn) {
        if (inv.isEmpty())
            return false;

        // Only check the item ingredient
        return ingredients.get(0).test(inv.getItem(0));
    }

    // Ensure hasMatchingNBT is accessible or replicate it here
    private static boolean hasMatchingNBT(FluidIngredient fluidIngredient, FluidStack fluidInTank) {
        for (FluidStack matchingFluid : fluidIngredient.getMatchingFluidStacks()) {
            boolean bothHaveTag = fluidInTank.hasTag() && matchingFluid.hasTag();
            boolean neitherHaveTag = !fluidInTank.hasTag() && !matchingFluid.hasTag();

            if (bothHaveTag) {
                if (fluidInTank.getTag().equals(matchingFluid.getTag())) {
                    return true;
                }
            } else if (neitherHaveTag) {
                return true;
            }
        }
        return false;
    }



}
