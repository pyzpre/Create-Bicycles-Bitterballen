package com.pyzpre.createbitterballen.compat.rei;

import com.pyzpre.createbitterballen.block.mechanicalfryer.DeepFryingRecipe;
import com.pyzpre.createbitterballen.compat.rei.animations.AnimatedFryer;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.rei.category.CreateRecipeCategory;
import com.simibubi.create.compat.rei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.createmod.catnip.data.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class FryingCategory extends CreateRecipeCategory<DeepFryingRecipe> {

    private final AnimatedFryer fryer = new AnimatedFryer();
    private final AnimatedBlazeBurner heater = new AnimatedBlazeBurner();
    FryingType type;



    enum FryingType {
        FRYING
    }

    public static FryingCategory standard(Info<DeepFryingRecipe> info) {
        return new FryingCategory(info, FryingType.FRYING);
    }

    protected FryingCategory(Info<DeepFryingRecipe> info, FryingType type) {
        super(info);
        this.type = type;
    }



    @Override
    public void addWidgets(CreateDisplay<DeepFryingRecipe> display, List<Widget> widgets, Point origin, Rectangle bounds) {
        DeepFryingRecipe recipe = display.getRecipe();
        NonNullList<FluidIngredient> fluidIngredients = recipe.getFluidIngredients();
        List<Pair<Ingredient, MutableInt>> ingredients = ItemHelper.condenseIngredients(recipe.getIngredients());
        List<ProcessingOutput> itemOutputs = recipe.getRollableResults();
        List<ProcessingOutput> results = display.getRecipe().getRollableResults();
        NonNullList<FluidStack> fluidOutputs = recipe.getFluidResults();

        int size = ingredients.size() + fluidIngredients.size();
        int xOffset = size < 3 ? (3 - size) * 19 / 2 : 0;
        int yOffset = 0;

        // Input slot with background
        Point inputSlotPoint = new Point(origin.x + 26, origin.y + 51);
        widgets.add(Widgets.createSlotBackground(inputSlotPoint));

        Point inputFluidSlotPoint = new Point(origin.x + 45, origin.y + 51);
        widgets.add(Widgets.createSlotBackground(inputFluidSlotPoint));

        Point outputSlotPoint = new Point(origin.x + 142, origin.y + 51);
        widgets.add(Widgets.createSlotBackground(outputSlotPoint));




        int i;
        for (i = 0; i < ingredients.size(); i++) {
            List<ItemStack> stacks = new ArrayList<>();
            Pair<Ingredient, MutableInt> pair = ingredients.get(i);
            Ingredient ingredient = pair.getFirst();
            MutableInt amount = pair.getSecond();

            for (ItemStack itemStack : ingredient.getItems()) {
                ItemStack stack = itemStack.copy();
                stack.setCount(amount.getValue());
                stacks.add(stack);
            }

            widgets.add(basicSlot(origin.x + 17 + xOffset + (i % 3) * 19, origin.y + 51 - (i / 3) * 19 + yOffset).markInput().entries(EntryIngredients.ofItemStacks(stacks)));
        }

        int j;
        for (j = 0; j < fluidIngredients.size(); j++) {
            int i2 = i + j;
            List<FluidStack> stacks = fluidIngredients.get(j)
                    .getMatchingFluidStacks();
            Slot fluidSlot = basicSlot(origin.x + 17 + xOffset + (i2 % 3) * 19, origin.y + 51 - (i2 / 3) * 19 + yOffset)
                    .markInput()
                    .entries(EntryIngredients.of(CreateRecipeCategory.convertToREIFluid(stacks.get(0))));
            CreateRecipeCategory.setFluidRenderRatio(fluidSlot);
            widgets.add(fluidSlot);
        }

        int outSize = fluidOutputs.size() + itemOutputs.size();
        int outputIndex = 0;

        for (; outputIndex < outSize; outputIndex++) {
            int xPosition = 141 - (outSize % 2 != 0 && outputIndex == outSize - 1 ? 0 : outputIndex % 2 == 0 ? 10 : -9);
            int yPosition = -19 * (outputIndex / 2) + 50 + yOffset;

            if (itemOutputs.size() > outputIndex) {
                ProcessingOutput result = itemOutputs.get(outputIndex);
                Slot outputSlot = basicSlot(origin.x + xPosition + 1, origin.y + yPosition + yOffset + 1)
                        .markOutput()
                        .entries(EntryIngredients.of(result.getStack()));
                widgets.add(outputSlot);
                addStochasticTooltip(outputSlot, result);
                i++;
            } else {
                Slot fluidSlot = basicSlot(origin.x + xPosition + 1, origin.y + yPosition + 1 + yOffset)
                        .markOutput()
                        .entries(EntryIngredients.of(CreateRecipeCategory.convertToREIFluid(fluidOutputs.get(outputIndex - itemOutputs.size()))));
                CreateRecipeCategory.setFluidRenderRatio(fluidSlot);
                widgets.add(fluidSlot);
                j++;
            }

        }

        addFluidTooltip(widgets, fluidIngredients, fluidOutputs);

        HeatCondition requiredHeat = recipe.getRequiredHeat();
        if (!requiredHeat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.NONE)) {
            widgets.add(basicSlot(origin.x + 134, origin.y + 81).markInput().entries(EntryIngredients.of(AllBlocks.BLAZE_BURNER.asStack())));
            i++;
        }
        if (!requiredHeat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.KINDLED)) {
            widgets.add(basicSlot(origin.x + 153, origin.y + 81).markOutput().entries(EntryIngredients.of(AllItems.BLAZE_CAKE.asStack())));
            i++;
        }
    }

    @Override
    public void draw(DeepFryingRecipe recipe, GuiGraphics graphics, double mouseX, double mouseY) {
        super.draw(recipe, graphics, mouseX, mouseY);

        HeatCondition requiredHeat = recipe.getRequiredHeat();
        boolean noHeat = requiredHeat == HeatCondition.NONE;

        // Additional graphics from the second method
        int vRows = (1 + recipe.getFluidResults().size() + recipe.getRollableResults().size()) / 2;
        if (vRows <= 2)
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 136, -19 * (vRows - 1) + 32);

        AllGuiTextures shadow = noHeat ? AllGuiTextures.JEI_SHADOW : AllGuiTextures.JEI_LIGHT;
        shadow.render(graphics, 81, 58 + (noHeat ? 10 : 30));

        // Heat bar and translation, always drawn
        AllGuiTextures heatBar = noHeat ? AllGuiTextures.JEI_NO_HEAT_BAR : AllGuiTextures.JEI_HEAT_BAR;
        heatBar.render(graphics, 4, 80);
        graphics.drawString(Minecraft.getInstance().font, CreateLang.translateDirect(requiredHeat.getTranslationKey()), 9,
                86, requiredHeat.getColor(), false);

        // Original graphics related to heat and fryer
        if (!noHeat)
            heater.withHeat(requiredHeat.visualizeAsBlazeBurner())
                    .draw(graphics, getDisplayWidth(null) / 2 + 3, 55);
       fryer.draw(graphics, getDisplayWidth(null) / 2 + 3, 34);
    }


}