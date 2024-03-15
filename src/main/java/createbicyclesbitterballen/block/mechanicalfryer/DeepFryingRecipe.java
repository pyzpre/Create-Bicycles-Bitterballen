package createbicyclesbitterballen.block.mechanicalfryer;


import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.recipe.DummyCraftingContainer;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.simibubi.create.foundation.utility.Iterate;
import createbicyclesbitterballen.index.BlockRegistry;
import createbicyclesbitterballen.index.RecipeRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;

public class DeepFryingRecipe extends ProcessingRecipe<SmartInventory> {

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
        return apply(basin, fryer, recipe, false);
    }


    private static boolean apply(BasinBlockEntity basin, MechanicalFryerEntity fryer, Recipe<?> recipe, boolean test) {
        boolean isDeepFryingRecipe = recipe instanceof DeepFryingRecipe;
        IItemHandler availableItems = fryer.inputInv;
        IFluidHandler availableFluids = basin.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElse(null);

        if (availableItems == null || availableFluids == null)
            return false;

        BlazeBurnerBlock.HeatLevel heat = BasinBlockEntity.getHeatLevelOf(basin.getLevel()
                .getBlockState(basin.getBlockPos()
                        .below(1)));
        if (isDeepFryingRecipe && !((DeepFryingRecipe) recipe).getRequiredHeat()
                .testBlazeBurner(heat))
            return false;


        List<ItemStack> recipeOutputItems = new ArrayList<>();
        List<FluidStack> recipeOutputFluids = new ArrayList<>();

        List<Ingredient> ingredients = new LinkedList<>(recipe.getIngredients());
        List<FluidIngredient> fluidIngredients =
                isDeepFryingRecipe ? ((DeepFryingRecipe) recipe).getFluidIngredients() : Collections.emptyList();

        for (boolean simulate : Iterate.trueAndFalse) {

            if (!simulate && test)
                return true;

            int[] extractedItemsFromSlot = new int[availableItems.getSlots()];
            int[] extractedFluidsFromTank = new int[availableFluids.getTanks()];

            Ingredients: for (int i = 0; i < ingredients.size(); i++) {
                Ingredient ingredient = ingredients.get(i);

                for (int slot = 0; slot < availableItems.getSlots(); slot++) {
                    if (simulate && availableItems.getStackInSlot(slot)
                            .getCount() <= extractedItemsFromSlot[slot])
                        continue;
                    ItemStack extracted = availableItems.extractItem(slot, 1, true);
                    if (!ingredient.test(extracted))
                        continue;
                    if (!simulate)
                        availableItems.extractItem(slot, 1, false);
                    extractedItemsFromSlot[slot]++;
                    continue Ingredients;
                }

                // something wasn't found
                return false;
            }

            boolean fluidsAffected = false;
            FluidIngredients: for (int i = 0; i < fluidIngredients.size(); i++) {
                FluidIngredient fluidIngredient = fluidIngredients.get(i);
                int amountRequired = fluidIngredient.getRequiredAmount();

                for (int tank = 0; tank < availableFluids.getTanks(); tank++) {
                    FluidStack fluidStack = availableFluids.getFluidInTank(tank);
                    if (simulate && fluidStack.getAmount() <= extractedFluidsFromTank[tank])
                        continue;
                    if (!fluidIngredient.test(fluidStack))
                        continue;
                    int drainedAmount = Math.min(amountRequired, fluidStack.getAmount());
                    if (!simulate) {
                        fluidStack.shrink(drainedAmount);
                        fluidsAffected = true;
                    }
                    amountRequired -= drainedAmount;
                    if (amountRequired != 0)
                        continue;
                    extractedFluidsFromTank[tank] += drainedAmount;
                    continue FluidIngredients;
                }

                // something wasn't found
                return false;
            }

            if (fluidsAffected) {
                basin.getBehaviour(SmartFluidTankBehaviour.INPUT)
                        .forEach(TankSegment::onFluidStackChanged);
                basin.getBehaviour(SmartFluidTankBehaviour.OUTPUT)
                        .forEach(TankSegment::onFluidStackChanged);
            }

            if (!simulate) {
                // Clear the list to prepare for actual output collection
                recipeOutputItems.clear();

                if (recipe instanceof BasinRecipe basinRecipe) {
                    recipeOutputItems.addAll(basinRecipe.rollResults());
                } else {
                    recipeOutputItems.add(recipe.getResultItem(basin.getLevel().registryAccess()));
                }

                // Now attempt to insert these collected items into the fryer's output
                for (ItemStack itemStack : recipeOutputItems) {
                    ItemStack remaining = ItemHandlerHelper.insertItemStacked(fryer.outputInv, itemStack.copy(), false);
                    if (!remaining.isEmpty()) {
                        LOGGER.warn("Output inventory full, could not insert item: {}", itemStack);
                        // Handle the case where the item cannot be inserted
                        // E.g., abort processing or handle overflow
                        return false; // Optional: Decide on behavior for full inventory
                    }
                }
            } else {
                // Actual insertion into the fryer's output inventory
                for (ItemStack itemStack : recipeOutputItems) {
                    ItemStack remaining = ItemHandlerHelper.insertItemStacked(fryer.outputInv, itemStack, false); // false to actually insert
                    if (!remaining.isEmpty()) {
                        LOGGER.warn("Failed to insert item into fryer's output inventory: {}", itemStack);
                        // Optionally handle overflow, e.g., dropping items in the world
                    }
                }
            }
        }

        return true; // Recipe successfully applied
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
    public boolean matches(SmartInventory inv, @Nonnull Level worldIn) {
        if (inv.isEmpty())
            return false;
        return ingredients.get(0)
                .test(inv.getItem(0));
    }


}
