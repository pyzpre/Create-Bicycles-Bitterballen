package com.pyzpre.createbitterballen.entity;

import com.pyzpre.createbitterballen.index.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FollowFlockLeaderGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nonnull;

public class HerringEntity extends AbstractSchoolingFish {

    private boolean isSwimming = false;

    public int getMaxSchoolSize() {
        return 5;
    }
    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.6)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(ForgeMod.SWIM_SPEED.get(), 1.0);
    }

    public HerringEntity(EntityType<? extends AbstractSchoolingFish> entityType, Level world) {
        super(entityType, world);

        this.refreshDimensions(); // Forces recalculation of hitbox size
    }



    @Override
    protected void registerGoals() {
        super.registerGoals();
        // Random swimming
        this.goalSelector.addGoal(1, new RandomSwimmingGoal(this, 1.0D, 1));


        // Schooling behavior
        this.goalSelector.addGoal(2, new FollowFlockLeaderGoal(this));
    }

    @Override
    public ItemStack getPickedResult(HitResult target) {
        return this.getBucketItemStack();
    }

    @Override
    @Nonnull
    public ItemStack getBucketItemStack() {
        return new ItemStack(ItemRegistry.HERRING_BUCKET.get());
    }

    @Override
    @Nonnull
    protected SoundEvent getFlopSound() {
        return SoundEvents.SALMON_FLOP;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SALMON_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SALMON_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(@Nonnull DamageSource damageSource) {
        return SoundEvents.SALMON_HURT;
    }

    @Override
    @Nonnull
    public EntityDimensions getDimensions(@Nonnull Pose pose) {
        // Adjust width and height to desired hitbox size
        float width = 0.65F;
        float height = 0.4F;
        return EntityDimensions.scalable(width, height);
    }
    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);

        // Offset the bounding box
        double offsetX = 0; // Move left/right
        double offsetY = 0; // Move up/down
        double offsetZ = 0.2;  // Small offset in Z
        this.setBoundingBox(this.getBoundingBox().move(offsetX, offsetY, offsetZ));
    }


    public static boolean canSpawnHere(EntityType<? extends AbstractSchoolingFish> fish, LevelAccessor world, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        return world.getBlockState(pos).getBlock() == Blocks.WATER;
    }

    @Override
    public void tick() {
        super.tick();

        // Check if the herring is in water and has any meaningful movement
        if (this.isInWater() && this.getDeltaMovement().lengthSqr() > 0.0001) {
            this.isSwimming = true;
        } else {
            this.isSwimming = false;
        }
    }
    @Override
    public void stopFollowing() {
        if (this.isFollower()) {
            // Safely clear the follower state
            super.stopFollowing(); // This calls the superclass method to nullify the leader
        }
    }




    public boolean isSwimming() {
        return this.isSwimming;
    }
}
