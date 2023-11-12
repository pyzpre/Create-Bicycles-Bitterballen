package createbicyclesbitterballen.compat.jei;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.compat.jei.category.BasinCategory;

import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;
import createbicyclesbitterballen.compat.jei.animations.AnimatedFryer;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

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
    public void draw(BasinRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        super.draw(recipe, iRecipeSlotsView, graphics, mouseX, mouseY);

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
        graphics.drawString(Minecraft.getInstance().font, Lang.translateDirect(requiredHeat.getTranslationKey()), 9,
                86, requiredHeat.getColor(), false);

        // Original graphics related to heat and fryer
        if (!noHeat)
            heater.withHeat(requiredHeat.visualizeAsBlazeBurner())
                    .draw(graphics, getBackground().getWidth() / 2 + 3, 55);
        fryer.draw(graphics, getBackground().getWidth() / 2 + 3, 34);
    }

}