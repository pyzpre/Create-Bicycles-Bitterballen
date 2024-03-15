package createbicyclesbitterballen.block.mechanicalfryer;


import java.util.List;
import java.util.Optional;

import com.simibubi.create.content.fluids.FluidFX;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.VecHelper;

import createbicyclesbitterballen.index.RecipeRegistry;
import createbicyclesbitterballen.index.SoundsRegistry;

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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;


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
        // Use the custom handler with a lambda that calls inventoryChanged
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
        if (running) {
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


    @Override
    public void tick() {
        super.tick();

        boolean isOutputFull = false;
        for (int i = 0; i < outputInv.getSlots(); i++) {
            if (outputInv.getStackInSlot(i).getCount() < outputInv.getSlotLimit(i)) {
                isOutputFull = false;
                break;
            } else {
                isOutputFull = true;
                break;
            }
        }

        if (getSpeed() == 0 || isOutputFull) {
            return;
        }

        float speed = Math.abs(getSpeed());
        boolean canStartProcessing = hasMatchingRecipe();
        float recipeSpeed = 1;

        if (!running && canStartProcessing) {
            running = true;
            runningTicks = 0;
            shouldRecalculateProcessingTicks = true;
            if (currentRecipe instanceof ProcessingRecipe) {
                int t = ((ProcessingRecipe<?>) currentRecipe).getProcessingDuration();
                if (t != 0)
                    recipeSpeed = t / 100f;
            }
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
            if (runningTicks == 20 ) {
                if (processingTicks == 1) {
                    Optional<BasinBlockEntity> basinOpt = getBasin();
                    basinOpt.ifPresent(basin -> {
                        if (lastRecipe == null || !lastRecipe.matches(inputInv, level)) {
                            Optional<DeepFryingRecipe> recipeOpt = RecipeRegistry.DEEP_FRYING.find(inputInv, level);
                            lastRecipe = recipeOpt.orElse(null);
                        }

                        if (lastRecipe != null && DeepFryingRecipe.apply(inputInv, basin, this, lastRecipe)) {
                            ItemStack stackInSlot = inputInv.getStackInSlot(0);
                            if (!stackInSlot.isEmpty()) {
                                stackInSlot.shrink(1);
                                inputInv.setStackInSlot(0, stackInSlot);
                                lastRecipe.rollResults().forEach(stack -> ItemHandlerHelper.insertItemStacked(outputInv, stack, false));
                                sendData();
                                setChanged();
                            }
                        }
                    });
                }


                if (runningTicks == 20 && shouldRecalculateProcessingTicks) {
                    if (canStartProcessing) {
                        if (lastRecipe != null && lastRecipe instanceof ProcessingRecipe) {
                            int duration = ((ProcessingRecipe<?>) lastRecipe).getProcessingDuration();
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
        ResourceLocation iceTag = new ResourceLocation("create_bic_bit", "ice");
        return stack.is(net.minecraft.tags.ItemTags.create(iceTag));
    }

    private void grantAdvancementCriterion(ServerPlayer player, String advancementID, String criterionKey) {
        PlayerAdvancements playerAdvancements = player.getAdvancements();
        Advancement advancement = player.server.getAdvancements().getAdvancement(new ResourceLocation(advancementID));

        if (advancement != null && advancement.getCriteria().containsKey(criterionKey)) {
            AdvancementProgress advancementProgress = playerAdvancements.getOrStartProgress(advancement);

            if (!advancementProgress.isDone()) {
                playerAdvancements.award(advancement, criterionKey);
            }
        }
    }

    private void causeExplosion() {
        if (!level.isClientSide()) {
            level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 4.0F, Explosion.BlockInteraction.DESTROY);
            double radius = 10.0;
            AABB area = new AABB(worldPosition).inflate(radius);
            List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, area);
            for (ServerPlayer player : players) {
                grantAdvancementCriterion(player, "create_bic_bit:fry_about_it", "ice_exploded");
            }
        }
    }




    private boolean hasMatchingRecipe() {

        if (inputInv.getStackInSlot(0).isEmpty()) return false;

        Optional<DeepFryingRecipe> recipeOpt = findMatchingRecipeForItem(inputInv.getStackInSlot(0), level);
        if (!recipeOpt.isPresent()) {
            return false;
        }


        Optional<BasinBlockEntity> basinOpt = getBasin();
        if (!basinOpt.isPresent()) {

            return false;
        }


        DeepFryingRecipe recipe = recipeOpt.get();
        if (!areBasinFluidsMatching(basinOpt.get(), recipe)) {

            return false;
        }
        HeatCondition requiredHeat = recipeOpt.get().getRequiredHeat();


        if (!isBlazeBurnerConfigured(requiredHeat)) {
            return false;
        }

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

        if (level == null || worldPosition == null) {
            return false;
        }

        BlockPos posBelowBasin = worldPosition.below(3);
        BlockState blockStateBelow = level.getBlockState(posBelowBasin);


        if (!(blockStateBelow.getBlock() instanceof BlazeBurnerBlock)) {
            return false;
        }

        BlazeBurnerBlock.HeatLevel actualHeat = BlazeBurnerBlock.getHeatLevelOf(blockStateBelow);

        return requiredHeat.testBlazeBurner(actualHeat);
    }

    private Optional<DeepFryingRecipe> findMatchingRecipeForItem(ItemStack stack, Level level) {
        for (Recipe<?> recipe : level.getRecipeManager().getAllRecipesFor(RecipeRegistry.DEEP_FRYING.getType())) {
            if (recipe instanceof DeepFryingRecipe deepFryingRecipe && deepFryingRecipe.matches(inputInv, level)) {
                return Optional.of(deepFryingRecipe);
            }
        }
        return Optional.empty();
    }

    private boolean areBasinFluidsMatching(BasinBlockEntity basin, DeepFryingRecipe recipe) {
        IFluidHandler fluidHandler = basin.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
        if (fluidHandler == null) return false;

        for (FluidIngredient fluidIngredient : recipe.getFluidIngredients()) {
            boolean ingredientMatched = false;

            int totalFluidAmount = 0;
            for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
                FluidStack fluidInTank = fluidHandler.getFluidInTank(tank);
                if (fluidIngredient.test(fluidInTank)) {
                    totalFluidAmount += fluidInTank.getAmount();
                }
            }

            if (totalFluidAmount >= fluidIngredient.getRequiredAmount()) {
                ingredientMatched = true;
            }

            if (!ingredientMatched) return false;
        }

        return true;
    }




    public void renderParticles() {
        Optional<BasinBlockEntity> basin = getBasin();
        if (!basin.isPresent() || level == null)
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
    public boolean continueWithPreviousRecipe() {
        runningTicks = 20;
        return true;
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

    public int getProcessingSpeed() {
        return Mth.clamp((int) Math.abs(getSpeed() / 16f), 1, 512);
    }
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
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
        // This method needs to check if the ItemStack is valid for the given recipe.
        // This is a simplistic approach; you'll likely need to extend it based on your actual recipe requirements.
        return recipe.getIngredients().stream().anyMatch(ingredient -> ingredient.test(stack));
    }


    private class FryerInventoryHandler extends CombinedInvWrapper {

        public FryerInventoryHandler(SmartInventory inputInv, SmartInventory outputInv) {
            super(inputInv, outputInv);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (outputInv == getHandlerFromIndex(getIndexForSlot(slot)))
                return false;
            return canProcess(stack) && super.isItemValid(slot, stack);
        }


        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
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