package createbicyclesbitterballen.compat.jei;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.jei.category.BasinCategory;

import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;



import createbicyclesbitterballen.compat.jei.animations.AnimatedFryer;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;


@ParametersAreNonnullByDefault
public class FryingCategory extends BasinCategory{

    private final AnimatedFryer fryer = new AnimatedFryer();
    private final AnimatedBlazeBurner heater = new AnimatedBlazeBurner();
    FryingType type;

    enum FryingType {
        FRYING
    }

    public static FryingCategory standard(Info<BasinRecipe> info) {
        return new FryingCategory(info, FryingType.FRYING);
    }

    protected FryingCategory(Info<BasinRecipe> info, FryingType type) {
        super(info, type != FryingType.FRYING);
        this.type = type;
    }



    @Override
    public void draw(BasinRecipe recipe, IRecipeSlotsView iRecipeSlotsView, PoseStack matrixStack, double mouseX, double mouseY) {
        super.draw(recipe, iRecipeSlotsView, matrixStack, mouseX, mouseY);

        HeatCondition requiredHeat = recipe.getRequiredHeat();
        if (requiredHeat != HeatCondition.NONE)
            heater.withHeat(requiredHeat.visualizeAsBlazeBurner())
                    .draw(matrixStack, getBackground().getWidth() / 2 + 3, 55);
        fryer.draw(matrixStack, getBackground().getWidth() / 2 + 3, 34);
    }

}