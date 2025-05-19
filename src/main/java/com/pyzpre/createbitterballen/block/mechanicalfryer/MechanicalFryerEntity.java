package com.pyzpre.createbitterballen.block.mechanicalfryer;

import com.pyzpre.createbitterballen.index.RecipeRegistry;
import com.pyzpre.createbitterballen.index.SoundsRegistry;
import com.simibubi.create.content.fluids.FluidFX;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;



public class MechanicalFryerEntity extends FryerOperatingBlockEntity {
    private static final Object DeepFryingRecipesKey = new Object();
    private boolean shouldRecalculateProcessingTicks;

    public SmartInventory inputInv;
    public SmartInventory outputInv;
    public LazyOptional<IItemHandler> capability;

    public int timer;
    private DeepFryingRecipe lastRecipe;
    public int runningTicks;
    public int processingTicks;
    public boolean running;


    public MechanicalFryerEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inputInv  = new SmartInventory(1, this);
        outputInv = new SmartInventory(9, this);
        capability = LazyOptional.of(() -> new FryerInventoryHandler(inputInv, outputInv));
        shouldRecalculateProcessingTicks = true;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(this));
        super.addBehaviours(behaviours);
    }
    public float getRenderedHeadOffset(float partialTicks) {
        int localTick;
        float offset = 0;
        if (running && speed != 0) {
            if (runningTicks < 20) {
                localTick = runningTicks;
                float num = (localTick + partialTicks) / 20f;
                num = ((2 - Mth.cos((float) (num * Math.PI))) / 2);
                offset = num - .5f;
            } else if (runningTicks <= 20) {
                offset = 1;
            } else {
                localTick = 40 - runningTicks;
                float num = (localTick - partialTicks) / 20f;
                num = ((2 - Mth.cos((float) (num * Math.PI))) / 2);
                offset = num - .5f;
            }
        }
        return offset + 7 / 16f;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(worldPosition).expandTowards(0, -1.5, 0);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        running = compound.getBoolean("Running");
        runningTicks = compound.getInt("Ticks");
        timer = compound.getInt("Timer");
        inputInv.deserializeNBT(compound.getCompound("InputInventory"));
        outputInv.deserializeNBT(compound.getCompound("OutputInventory"));
        shouldRecalculateProcessingTicks = compound.getBoolean("ShouldRecalculate");
        super.read(compound, clientPacket);

        if (clientPacket && hasLevel())
            getBasin().ifPresent(bte -> bte.setAreFluidsMoving(running && runningTicks <= 20));
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        compound.putBoolean("Running", running);
        compound.putInt("Ticks", runningTicks);
        compound.putInt("Timer", timer);
        compound.put("InputInventory", inputInv.serializeNBT());
        compound.put("OutputInventory", outputInv.serializeNBT());
        compound.putBoolean("ShouldRecalculate", shouldRecalculateProcessingTicks); // Serialize the flag
        super.write(compound, clientPacket);
    }


    private boolean applyRecipe(DeepFryingRecipe recipe) {
        Optional<BasinBlockEntity> basinOpt = getBasin();
        if (basinOpt.isEmpty()) {
            return false;
        }
        BasinBlockEntity basin = basinOpt.get();

        // Get the fluid handler
        IFluidHandler fluidHandler = basin.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
        if (fluidHandler == null) {
            return false;
        }

        // Calculate the maximum items that can be processed based on available fluids
        int maxProcessableItems = inputInv.getStackInSlot(0).getCount();
        for (FluidIngredient fluidIngredient : recipe.getFluidIngredients()) {
            int requiredAmount = fluidIngredient.getRequiredAmount();
            int totalMatchingAmount = 0;

            for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
                FluidStack fluidInTank = fluidHandler.getFluidInTank(tank);
                if (fluidIngredient.test(fluidInTank)) {
                    totalMatchingAmount += fluidInTank.getAmount();
                }
            }

            // Calculate the maximum items based on available fluid for each ingredient
            maxProcessableItems = Math.min(maxProcessableItems, totalMatchingAmount / requiredAmount);
        }

        if (maxProcessableItems <= 0) {
            return false; // Not enough fluids to process even a single item
        }

        // Consume the required amount of fluids for the batch
        for (FluidIngredient fluidIngredient : recipe.getFluidIngredients()) {
            int amountToConsume = fluidIngredient.getRequiredAmount() * maxProcessableItems;
            for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
                FluidStack fluidInTank = fluidHandler.getFluidInTank(tank);
                if (fluidIngredient.test(fluidInTank)) {
                    int drainedAmount = fluidHandler.drain(new FluidStack(fluidInTank, amountToConsume), IFluidHandler.FluidAction.EXECUTE).getAmount();
                    amountToConsume -= drainedAmount;
                    if (amountToConsume <= 0) break;
                }
            }
        }

        // Consume items in the input inventory
        ItemStack inputStack = inputInv.getStackInSlot(0);
        inputStack.shrink(maxProcessableItems);
        inputInv.setStackInSlot(0, inputStack);

        // Produce outputs for the processed batch
        List<ItemStack> outputs = recipe.rollResults();
        for (ItemStack output : outputs) {
            output.setCount(output.getCount() * maxProcessableItems); // Multiply output by the batch size
            ItemStack remaining = ItemHandlerHelper.insertItemStacked(outputInv, output.copy(), false);
            if (!remaining.isEmpty()) {
                return false; // Stop processing if output inventory is full
            }
        }

        // Sync state
        basin.setChanged();
        basin.sendData();
        sendData();
        setChanged();

        return true;
    }


    @Override
    public void tick() {
        if (basinRemoved) {
            basinRemoved = false;
            onBasinRemoved();
            sendData();

            return;
        }
        super.tick();

        float speed = Math.abs(getSpeed());
        boolean canStartProcessing = hasMatchingRecipe();
        float recipeSpeed = 1;

        // Log the current state at the beginning of each tick



        if (getSpeed() == 0) {
            if (running) {

                resetAnimationAndProcessing();
            }
        }

        if (!running && canStartProcessing) {
            running = true;
            runningTicks = 0;
            shouldRecalculateProcessingTicks = true;



        }

        if (running) {
            if (processingTicks > 0) {
                if (runningTicks < 20) {
                    runningTicks++;
                }
                processingTicks--;


            } else {
                if (runningTicks < 40) {
                    runningTicks++;
                } else {
                    if (!canStartProcessing) {
                        running = false;
                        runningTicks = 0;
                        shouldRecalculateProcessingTicks = false;

                    } else {
                        shouldRecalculateProcessingTicks = true;
                        runningTicks = 0;

                    }
                }
            }

            if (runningTicks == 20 && processingTicks == 1) {
                for (int slot = 0; slot < inputInv.getSlots(); slot++) {
                    ItemStack stackInSlot = inputInv.getStackInSlot(slot);
                    if (isIce(stackInSlot)) {

                        causeExplosion();
                        inputInv.setStackInSlot(slot, ItemStack.EMPTY);
                        break;
                    }
                }
            }

            if (runningTicks == 20) {
                if (processingTicks == 1) {


                    if (lastRecipe == null || !matchesRecipe(lastRecipe)) {

                        Optional<DeepFryingRecipe> recipeOpt = findMatchingRecipe(level);
                        lastRecipe = recipeOpt.orElse(null);
                        if (lastRecipe != null) {

                        } else {

                        }
                    }

                    if (lastRecipe != null && applyRecipe(lastRecipe)) {

                    } else {

                    }
                }

                if (runningTicks == 20 && shouldRecalculateProcessingTicks) {
                    if (canStartProcessing) {
                        if (lastRecipe != null) {
                            int duration = lastRecipe.getProcessingDuration();
                            if (duration != 0) recipeSpeed = duration / 100f;
                        }
                        processingTicks = Mth.clamp((Mth.log2((int) (512 / speed))) * Mth.ceil(recipeSpeed * 15) + 1, 1, 512);
                        shouldRecalculateProcessingTicks = false;


                    }
                }
            }
        }
    }

    private boolean isIce(ItemStack stack) {
        ResourceLocation iceTag = new ResourceLocation("forge", "ice");
        return stack.is(net.minecraft.tags.ItemTags.create(iceTag));
    }
    private void resetAnimationAndProcessing() {
        running = false;
        processingTicks = 0;
        runningTicks = 0;
        }


    private void grantAdvancementCriterion(ServerPlayer player) {
        PlayerAdvancements playerAdvancements = player.getAdvancements();
        Advancement advancement = player.server.getAdvancements().getAdvancement(new ResourceLocation("create_bic_bit:fry_about_it"));

        if (advancement != null && advancement.getCriteria().containsKey("ice_exploded")) {
            AdvancementProgress advancementProgress = playerAdvancements.getOrStartProgress(advancement);

            if (!advancementProgress.isDone()) {
                playerAdvancements.award(advancement, "ice_exploded");
            }
        }
    }

    private void causeExplosion() {
        if (!level.isClientSide()) {
            level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 4.0F, false, Level.ExplosionInteraction.MOB);
            double radius = 10.0;
            AABB area = new AABB(worldPosition).inflate(radius);
            List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, area);
            for (ServerPlayer player : players) {
                grantAdvancementCriterion(player);
            }
        }
    }




    private boolean hasMatchingRecipe() {
        if (inputInv.getStackInSlot(0).isEmpty()) {
            currentRecipe = null;
            return false;
        }

        Optional<DeepFryingRecipe> recipeOpt = findMatchingRecipe(level);
        if (recipeOpt.isEmpty()) {
            currentRecipe = null;
            return false;
        }

        DeepFryingRecipe recipe = recipeOpt.get();

        // Heat condition check
        HeatCondition requiredHeat = recipe.getRequiredHeat();
        if (!isBlazeBurnerConfigured(requiredHeat)) {
            currentRecipe = null;
            return false;
        }

        currentRecipe = recipe;
        return true;
    }




    @Override
    public void destroy() {
        super.destroy();
        ItemHelper.dropContents(level, worldPosition, inputInv);
        ItemHelper.dropContents(level, worldPosition, outputInv);
    }
    private boolean isBlazeBurnerConfigured(HeatCondition requiredHeat) {
        if (requiredHeat == HeatCondition.NONE) {
            return true;
        }

        if (level == null) {
            return false;
        }

        BlockPos posBelowBasin = worldPosition.below(3);
        BlockState blockStateBelow = level.getBlockState(posBelowBasin);

        // Use the existing BasinBlockEntity logic
        BlazeBurnerBlock.HeatLevel actualHeat = BasinBlockEntity.getHeatLevelOf(blockStateBelow);
        return requiredHeat.testBlazeBurner(actualHeat);
    }



    private Optional<DeepFryingRecipe> findMatchingRecipe(Level level) {
        for (Recipe<?> recipe : level.getRecipeManager().getAllRecipesFor(RecipeRegistry.DEEP_FRYING.getType())) {
            if (recipe instanceof DeepFryingRecipe deepFryingRecipe && matchesRecipe(deepFryingRecipe)) {
                return Optional.of(deepFryingRecipe);
            }
        }
        return Optional.empty();
    }

    private boolean matchesRecipe(DeepFryingRecipe recipe) {
        // Check item ingredient
        ItemStack inputStack = inputInv.getStackInSlot(0);
        if (!recipe.getIngredients().get(0).test(inputStack)) {

            return false;
        }

        // Get the basin
        Optional<BasinBlockEntity> basinOpt = getBasin();
        if (basinOpt.isEmpty()) {

            return false;
        }
        BasinBlockEntity basin = basinOpt.get();

        // Get the fluids from the basin
        IFluidHandler fluidHandler = basin.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
        if (fluidHandler == null) {

            return false;
        }

        // Check if fluid ingredients match
        for (FluidIngredient fluidIngredient : recipe.getFluidIngredients()) {
            int requiredAmount = fluidIngredient.getRequiredAmount();
            int totalMatchingAmount = 0;

            for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
                FluidStack fluidInTank = fluidHandler.getFluidInTank(tank);

                // Use fluidIngredient.test(fluidInTank) to include NBT data
                if (fluidIngredient.test(fluidInTank)) {
                    totalMatchingAmount += fluidInTank.getAmount();

                } else {

                }

                // If we have enough fluid, we can stop checking further tanks
                if (totalMatchingAmount >= requiredAmount)
                    break;
            }

            // If the total matching amount is less than required, the recipe cannot proceed
            if (totalMatchingAmount < requiredAmount) {

                return false;
            }
        }


        return true;
    }


    private boolean areBasinFluidsMatching(BasinBlockEntity basin, DeepFryingRecipe recipe) {
        IFluidHandler fluidHandler = basin.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);

        for (FluidIngredient fluidIngredient : recipe.getFluidIngredients()) {
            int requiredAmount = fluidIngredient.getRequiredAmount();
            int totalMatchingAmount = 0;

            for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
                FluidStack fluidInTank = fluidHandler.getFluidInTank(tank);

                // First, check if the fluid types match
                if (!fluidIngredient.test(fluidInTank)) {
                    continue;
                }

                // Then, check if the NBT data matches
                if (!hasMatchingNBT(fluidIngredient, fluidInTank)) {
                    continue;
                }

                totalMatchingAmount += fluidInTank.getAmount();

                // If we have enough fluid, we can stop checking further tanks
                if (totalMatchingAmount >= requiredAmount) {
                    break;
                }
            }

            // If the total matching amount is less than required, the recipe cannot proceed
            if (totalMatchingAmount < requiredAmount) {
                return false;
            }
        }

        return true;
    }

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





    public void renderParticles() {
        Optional<BasinBlockEntity> basin = getBasin();
        if (basin.isEmpty() || level == null)
            return;

        for (SmartFluidTankBehaviour behaviour : basin.get()
                .getTanks()) {
            if (behaviour == null)
                continue;
            for (SmartFluidTankBehaviour.TankSegment tankSegment : behaviour.getTanks()) {
                if (tankSegment.isEmpty(0))
                    continue;
                spillParticle(FluidFX.getFluidParticle(tankSegment.getRenderedFluid()));
            }
        }
    }

    protected void spillParticle(ParticleOptions data) {
        float angle = level.random.nextFloat() * 360;
        Vec3 offset = new Vec3(0, 0, 0.25f);
        offset = VecHelper.rotate(offset, angle, Direction.Axis.Y);
        Vec3 target = VecHelper.rotate(offset, getSpeed() > 0 ? 25 : -25, Direction.Axis.Y)
                .add(0, .25f, 0);
        Vec3 center = offset.add(VecHelper.getCenterOf(worldPosition));
        target = VecHelper.offsetRandomly(target.subtract(offset), level.random, 1 / 128f);
        level.addParticle(data, center.x, center.y - 1.75f, center.z, target.x, target.y, target.z);
    }

    @Override
    protected <C extends Container> boolean matchStaticFilters(Recipe<C> recipe) {
        return recipe.getType() == RecipeRegistry.DEEP_FRYING.getType();
    }

    @Override
    public void startProcessingBasin() {
        if (running && runningTicks <= 20)
            return;
        super.startProcessingBasin();
        running = true;
        runningTicks = 0;
    }

    @Override
    protected void onBasinRemoved() {
        if (!running)
            return;
        runningTicks = 40;
        running = false;
    }

    @Override
    protected Object getRecipeCacheKey() {
        return DeepFryingRecipesKey;
    }

    @Override
    protected boolean isRunning() {
        return running;
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        super.tickAudio();

        boolean slow = Math.abs(getSpeed()) < 65;
        if (slow && AnimationTickHolder.getTicks() % 2 == 0)
            return;

        if (runningTicks == 20) {
            SoundsRegistry.FRYING.playAt(level, worldPosition, .75f, 1, true);
            renderParticles();
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (isItemHandlerCap(cap))
            return capability.cast();
        return super.getCapability(cap, side);
    }
    public boolean canProcess(ItemStack stack) {
        if (lastRecipe != null && isItemValidForRecipe(lastRecipe, stack)) {
            return true;
        }
        List<DeepFryingRecipe> recipes = level.getRecipeManager()
                .getAllRecipesFor(RecipeRegistry.DEEP_FRYING.get());
        for (DeepFryingRecipe recipe : recipes) {
            if (isItemValidForRecipe(recipe, stack)) {
                return true;
            }
        }
        return false;
    }

    private boolean isItemValidForRecipe(DeepFryingRecipe recipe, ItemStack stack) {
        return recipe.getIngredients().stream().anyMatch(ingredient -> ingredient.test(stack));
    }


    private class FryerInventoryHandler extends CombinedInvWrapper {

        public FryerInventoryHandler(SmartInventory inputInv, SmartInventory outputInv) {
            super(inputInv, outputInv);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (outputInv == getHandlerFromIndex(getIndexForSlot(slot)))
                return false;
            return canProcess(stack) && super.isItemValid(slot, stack);
        }


        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (outputInv == getHandlerFromIndex(getIndexForSlot(slot)))
                return stack;
            if (!isItemValid(slot, stack))
                return stack;
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (inputInv == getHandlerFromIndex(getIndexForSlot(slot)))
                return ItemStack.EMPTY;
            return super.extractItem(slot, amount, simulate);
        }

    }

}