package createbicyclesbitterballen.block.mechanicalfryer;

import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import createbicyclesbitterballen.index.RecipeRegistry;

public class DeepFryingRecipe extends BasinRecipe {
    public DeepFryingRecipe(ProcessingRecipeParams params) {

        super(RecipeRegistry.DEEP_FRYING, params);
    }
}