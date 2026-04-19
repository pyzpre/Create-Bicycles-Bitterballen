package com.pyzpre.createbitterballen.block.mechanicalfryer;

import com.pyzpre.createbitterballen.CreateBitterballen;
import com.pyzpre.createbitterballen.index.BlockEntityRegistry;
import com.pyzpre.createbitterballen.index.RecipeRegistry;
import com.pyzpre.createbitterballen.index.SoundsRegistry;
import com.simibubi.create.content.fluids.FluidFX;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;



public class MechanicalFryerEntity extends FryerOperatingBlockEntity {
    private static final Object DeepFryingRecipesKey = new Object();
    private boolean shouldRecalculateProcessingTicks;

    public SmartInventory inputInv;
    public SmartInventory outputInv;

    public int timer;
    private DeepFryingRecipe lastRecipe;
    public int runningTicks;
    public int processingTicks;
    public boolean running;


    // Declare the field at the class level
    private final FryerInventoryHandler inventoryHandler;

    public MechanicalFryerEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inputInv = new SmartInventory(1, this);
        outputInv = new SmartInventory(9, this);

        // Initialize the handler here (without 'private')
        inventoryHandler = new FryerInventoryHandler(inputInv, outputInv);

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
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        running = compound.getBoolean("Running");
        runningTicks = compound.getInt("Ticks");
        timer = compound.getInt("Timer");
        inputInv.deserializeNBT(registries, compound.getCompound("InputInventory"));
        outputInv.deserializeNBT(registries, compound.getCompound("OutputInventory"));
        shouldRecalculateProcessingTicks = compound.getBoolean("ShouldRecalculate");
        super.read(compound, registries, clientPacket);

        if (clientPacket && hasLevel())
            getBasin().ifPresent(bte -> bte.setAreFluidsMoving(running && runningTicks <= 20));
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        compound.putBoolean("Running", running);
        compound.putInt("Ticks", runningTicks);
        compound.putInt("Timer", timer);
        compound.put("InputInventory", inputInv.serializeNBT(registries));
        compound.put("OutputInventory", outputInv.serializeNBT(registries));
        compound.putBoolean("ShouldRecalculate", shouldRecalculateProcessingTicks); // Serialize the flag
        super.write(compound,registries, clientPacket);
    }


    private boolean applyRecipe(DeepFryingRecipe recipe) {
        Optional<BasinBlockEntity> basinOpt = getBasin();
        if (basinOpt.isEmpty()) {
            return false;
        }
        BasinBlockEntity basin = basinOpt.get();

        IFluidHandler fluidHandler = level.getCapability(
                Capabilities.FluidHandler.BLOCK,
                basin.getBlockPos(),
                null
        );
        if (fluidHandler == null) {
            return false;
        }

        int maxProcessableItems = inputInv.getStackInSlot(0).getCount();

        // Limit by available input fluid
        for (SizedFluidIngredient fluidIngredient : recipe.getFluidIngredients()) {
            int requiredAmount = fluidIngredient.amount();
            int totalMatchingAmount = 0;

            for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
                FluidStack fluidInTank = fluidHandler.getFluidInTank(tank);
                if (fluidIngredient.test(fluidInTank)) {
                    totalMatchingAmount += fluidInTank.getAmount();
                }
            }

            maxProcessableItems = Math.min(maxProcessableItems, totalMatchingAmount / requiredAmount);
        }

        // Limit by available fluid output space
        if (recipe instanceof BasinRecipe basinRecipe) {
            IFluidHandler basinFluidHandler = level.getCapability(
                    Capabilities.FluidHandler.BLOCK,
                    basin.getBlockPos(),
                    null
            );
            if (basinFluidHandler == null) {
                return false;
            }

            for (FluidStack fluidResult : basinRecipe.getFluidResults()) {
                int perItem = fluidResult.getAmount();
                int best = 0;

                for (int i = maxProcessableItems; i >= 1; i--) {
                    FluidStack scaled = fluidResult.copy();
                    scaled.setAmount(perItem * i);

                    int fill = basinFluidHandler.fill(scaled, IFluidHandler.FluidAction.SIMULATE);
                    if (fill >= scaled.getAmount()) {
                        best = i;
                        break;
                    }
                }

                maxProcessableItems = Math.min(maxProcessableItems, best);
                if (maxProcessableItems == 0) {
                    return false;
                }
            }
        }

        if (maxProcessableItems <= 0) {
            return false; // Not enough fluids to process even a single item
        }

        // Consume the required amount of fluids for the batch
        for (SizedFluidIngredient fluidIngredient : recipe.getFluidIngredients()) {
            int amountToConsume = fluidIngredient.amount() * maxProcessableItems;
            for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
                FluidStack fluidInTank = fluidHandler.getFluidInTank(tank);
                if (fluidIngredient.test(fluidInTank)) {
                    int drainedAmount = fluidHandler.drain(
                            fluidInTank.copyWithAmount(amountToConsume),
                            IFluidHandler.FluidAction.EXECUTE
                    ).getAmount();
                    amountToConsume -= drainedAmount;
                    if (amountToConsume <= 0) break;
                }
            }
        }

        // Consume items in the input inventory
        ItemStack inputStack = inputInv.getStackInSlot(0);
        inputStack.shrink(maxProcessableItems);
        inputInv.setStackInSlot(0, inputStack);

        // Produce item outputs for the processed batch
        List<ItemStack> outputs = recipe.rollResults(level.random);
        for (ItemStack output : outputs) {
            output.setCount(output.getCount() * maxProcessableItems);
            ItemStack remaining = ItemHandlerHelper.insertItemStacked(outputInv, output.copy(), false);
            if (!remaining.isEmpty()) {
                return false; // Stop processing if output inventory is full
            }
        }

        // Handle fluid outputs
        if (recipe instanceof BasinRecipe basinRecipe) {
            List<FluidStack> fluidResults = basinRecipe.getFluidResults();

            for (FluidStack fluidResult : fluidResults) {
                FluidStack outputFluidStack = fluidResult.copy();
                outputFluidStack.setAmount(outputFluidStack.getAmount() * maxProcessableItems);

                IFluidHandler basinFluidHandler = level.getCapability(
                        Capabilities.FluidHandler.BLOCK,
                        basin.getBlockPos(),
                        null
                );

                if (basinFluidHandler == null) {
                    return false;
                }

                int filled = basinFluidHandler.fill(outputFluidStack, IFluidHandler.FluidAction.EXECUTE);

                if (filled < outputFluidStack.getAmount()) {
                    return false; // early return to prevent item/fluid desync
                }
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
        ResourceLocation iceTag = ResourceLocation.fromNamespaceAndPath("c", "ice");
        return stack.is(net.minecraft.tags.ItemTags.create(iceTag));
    }
    private void resetAnimationAndProcessing() {
        running = false;
        processingTicks = 0;
        runningTicks = 0;
        }


    private static void grantAdvancementCriterion(ServerPlayer player, String advancementID, String criterionKey) {
        PlayerAdvancements playerAdvancements = player.getAdvancements();
        ResourceLocation id = ResourceLocation.parse(advancementID);

        Optional.ofNullable(player.server.getAdvancements().get(id)).ifPresent(holder -> {
            if (holder.value().criteria().containsKey(criterionKey)) {
                AdvancementProgress progress = playerAdvancements.getOrStartProgress(holder);
                if (!progress.isDone()) {
                    playerAdvancements.award(holder, criterionKey);
                }
            }
        });
    }






    private void causeExplosion() {
        if (!level.isClientSide()) {
            level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 4.0F, false, Level.ExplosionInteraction.MOB);
            double radius = 10.0;
            AABB area = new AABB(worldPosition).inflate(radius);
            List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, area);
            for (ServerPlayer player : players) {
                grantAdvancementCriterion(player, "create_bic_bit:fry_about_it", "ice_exploded");
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
        // Fluid output space check (scale to how many items can be processed)
        if (recipe instanceof BasinRecipe basinRecipe) {
            Optional<BasinBlockEntity> basinOpt = getBasin();
            if (basinOpt.isEmpty()) {
                currentRecipe = null;
                return false;
            }

            IFluidHandler fluidHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, basinOpt.get().getBlockPos(), null);
            if (fluidHandler == null) {
                currentRecipe = null;
                return false;
            }

            // Start with full stack, reduce if not enough room
            int inputCount = inputInv.getStackInSlot(0).getCount();
            int maxProcessable = inputCount;

            for (FluidStack fluidResult : basinRecipe.getFluidResults()) {
                int amountPerItem = fluidResult.getAmount();

                int maxForThisFluid = 0;
                for (int i = inputCount; i >= 1; i--) {
                    FluidStack simulated = fluidResult.copy();
                    simulated.setAmount(amountPerItem * i);

                    int fill = fluidHandler.fill(simulated, IFluidHandler.FluidAction.SIMULATE);
                    if (fill >= simulated.getAmount()) {
                        maxForThisFluid = i;
                        break;
                    }
                }

                maxProcessable = Math.min(maxProcessable, maxForThisFluid);
                if (maxProcessable == 0) {
                    currentRecipe = null;
                    return false;
                }
            }
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

        for (RecipeHolder<? extends Recipe<?>> holder : level.getRecipeManager().getAllRecipesFor(RecipeRegistry.DEEP_FRYING.getType())) {
            Recipe<?> recipe = holder.value();
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

        Optional<BasinBlockEntity> basinOpt = getBasin();
        if (basinOpt.isEmpty()) {
            return false;
        }
        BasinBlockEntity basin = basinOpt.get();

        IFluidHandler fluidHandler = level.getCapability(
                Capabilities.FluidHandler.BLOCK,
                basin.getBlockPos(),
                null
        );
        if (fluidHandler == null) {
            return false;
        }

        // Check if fluid ingredients match
        for (SizedFluidIngredient fluidIngredient : recipe.getFluidIngredients()) {
            int requiredAmount = fluidIngredient.amount();
            int totalMatchingAmount = 0;

            for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
                FluidStack fluidInTank = fluidHandler.getFluidInTank(tank);

                if (fluidIngredient.test(fluidInTank)) {
                    totalMatchingAmount += fluidInTank.getAmount();
                }

                if (totalMatchingAmount >= requiredAmount)
                    break;
            }

            if (totalMatchingAmount < requiredAmount) {
                return false;
            }
        }

        return true;
    }

    private boolean areBasinFluidsMatching(BasinBlockEntity basin, DeepFryingRecipe recipe) {
        IFluidHandler fluidHandler = basin.getLevel().getCapability(
                Capabilities.FluidHandler.BLOCK,
                basin.getBlockPos(),
                null
        );

        if (fluidHandler == null) {
            return false;
        }

        for (SizedFluidIngredient fluidIngredient : recipe.getFluidIngredients()) {
            int requiredAmount = fluidIngredient.amount();
            int totalMatchingAmount = 0;

            for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
                FluidStack fluidInTank = fluidHandler.getFluidInTank(tank);

                if (!fluidIngredient.test(fluidInTank))
                    continue;

                if (!hasMatchingNBT(fluidIngredient, fluidInTank))
                    continue;

                totalMatchingAmount += fluidInTank.getAmount();

                if (totalMatchingAmount >= requiredAmount) {
                    break;
                }
            }

            if (totalMatchingAmount < requiredAmount) {
                return false;
            }
        }

        return true;
    }

    private static boolean hasMatchingNBT(SizedFluidIngredient fluidIngredient, FluidStack fluidInTank) {
        for (FluidStack matchingFluid : fluidIngredient.getFluids()) {
            boolean bothHaveTag =
                    !fluidInTank.getComponents().isEmpty() && !matchingFluid.getComponents().isEmpty();
            boolean neitherHaveTag =
                    fluidInTank.getComponents().isEmpty() && matchingFluid.getComponents().isEmpty();

            if (bothHaveTag) {
                if (fluidInTank.getComponents().equals(matchingFluid.getComponents())) {
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
    protected boolean matchStaticFilters(RecipeHolder<? extends Recipe<?>> holder) {
        return holder.value().getType() == RecipeRegistry.DEEP_FRYING.getType();
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

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                BlockEntityRegistry.MECHANICAL_FRYER.get(),
                (blockEntity, context) -> blockEntity.inventoryHandler
        );
    }


    public boolean canProcess(ItemStack stack) {
        if (lastRecipe != null && isItemValidForRecipe(lastRecipe, stack))
            return true;

        List<DeepFryingRecipe> recipes = level.getRecipeManager()
                .getAllRecipesFor(RecipeRegistry.DEEP_FRYING.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();

        for (DeepFryingRecipe recipe : recipes)
            if (isItemValidForRecipe(recipe, stack))
                return true;

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