package com.pyzpre.create_bic_bit.block.mechanicalfryer;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.simple.DeferralBehaviour;
import com.simibubi.create.foundation.recipe.RecipeFinder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public abstract class FryerOperatingBlockEntity extends KineticBlockEntity {

    public DeferralBehaviour basinChecker;
    public boolean basinRemoved;
    protected Recipe<?> currentRecipe;


    public FryerOperatingBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }
    public void sendData() {
        if (level == null || level.isClientSide) {
            return;
        }

        // Create a block update packet
        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, 3);

        // Send the block entity data to the client
        level.getChunkAt(worldPosition).setBlockEntity(this);
    }



    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        basinChecker = new DeferralBehaviour(this, this::updateBasin);
        behaviours.add(basinChecker);
    }

    @Override
    public void onSpeedChanged(float prevSpeed) {
        super.onSpeedChanged(prevSpeed);
        getSpeed();
        basinRemoved = false;
        basinChecker.scheduleUpdate();
    }

    protected boolean updateBasin() {
        if (!isSpeedRequirementFulfilled())
            return true;
        if (getSpeed() == 0)
            return true;
        if (isRunning())
            return true;
        if (level == null || level.isClientSide)
            return true;
        Optional<BasinBlockEntity> basin = getBasin();
        if (basin.filter(BasinBlockEntity::canContinueProcessing)
                .isEmpty())
            return true;

        List<Recipe<?>> recipes = getMatchingRecipes();
        if (recipes.isEmpty())
            return true;
        currentRecipe = recipes.get(0);
        startProcessingBasin();
        sendData();
        return true;
    }

    protected abstract boolean isRunning();

    public void startProcessingBasin() {}


    protected <C extends Container> boolean matchFryingRecipe(Recipe<C> recipe) {

        if (recipe == null) {
            return false;
        }

        Optional<BasinBlockEntity> basinOptional = getBasin();
        if (basinOptional.isEmpty()) {
            return false;
        }

        BasinBlockEntity basin = basinOptional.get();

        if (!(this instanceof MechanicalFryerEntity fryer)) {
            return false;
        }

        return DeepFryingRecipe.match(basin, fryer, recipe);
    }





    protected List<Recipe<?>> getMatchingRecipes() {


        Optional<BasinBlockEntity> basinOptional = getBasin();
        if (basinOptional.map(BasinBlockEntity::isEmpty).orElse(true)) {
            return new ArrayList<>();
        }


        List<Recipe<?>> list = RecipeFinder.get(getRecipeCacheKey(), level, this::matchStaticFilters);


        return list.stream()
                .filter(this::matchFryingRecipe)
                .sorted((r1, r2) -> r2.getIngredients().size() - r1.getIngredients().size())
                .collect(Collectors.toList());
    }


    protected abstract void onBasinRemoved();

    protected Optional<BasinBlockEntity> getBasin() {
        if (level == null)
            return Optional.empty();
        BlockEntity basinBE = level.getBlockEntity(worldPosition.below(2));
        if (!(basinBE instanceof BasinBlockEntity))
            return Optional.empty();
        return Optional.of((BasinBlockEntity) basinBE);
    }

    protected abstract <C extends Container> boolean matchStaticFilters(Recipe<C> recipe);

    protected abstract Object getRecipeCacheKey();
}