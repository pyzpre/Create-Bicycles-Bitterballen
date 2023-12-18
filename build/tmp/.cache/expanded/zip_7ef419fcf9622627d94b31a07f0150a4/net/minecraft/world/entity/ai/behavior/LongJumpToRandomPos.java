package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LongJumpToRandomPos<E extends Mob> extends Behavior<E> {
   protected static final int FIND_JUMP_TRIES = 20;
   private static final int PREPARE_JUMP_DURATION = 40;
   protected static final int MIN_PATHFIND_DISTANCE_TO_VALID_JUMP = 8;
   private static final int TIME_OUT_DURATION = 200;
   private static final List<Integer> ALLOWED_ANGLES = Lists.newArrayList(65, 70, 75, 80);
   private final UniformInt timeBetweenLongJumps;
   protected final int maxLongJumpHeight;
   protected final int maxLongJumpWidth;
   protected final float maxJumpVelocity;
   protected List<LongJumpToRandomPos.PossibleJump> jumpCandidates = Lists.newArrayList();
   protected Optional<Vec3> initialPosition = Optional.empty();
   @Nullable
   protected Vec3 chosenJump;
   protected int findJumpTries;
   protected long prepareJumpStart;
   private Function<E, SoundEvent> getJumpSound;
   private final Predicate<BlockState> acceptableLandingSpot;

   public LongJumpToRandomPos(UniformInt p_147637_, int p_147638_, int p_147639_, float p_147640_, Function<E, SoundEvent> p_147641_) {
      this(p_147637_, p_147638_, p_147639_, p_147640_, p_147641_, (p_217311_) -> {
         return false;
      });
   }

   public LongJumpToRandomPos(UniformInt p_217293_, int p_217294_, int p_217295_, float p_217296_, Function<E, SoundEvent> p_217297_, Predicate<BlockState> p_217298_) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT), 200);
      this.timeBetweenLongJumps = p_217293_;
      this.maxLongJumpHeight = p_217294_;
      this.maxLongJumpWidth = p_217295_;
      this.maxJumpVelocity = p_217296_;
      this.getJumpSound = p_217297_;
      this.acceptableLandingSpot = p_217298_;
   }

   protected boolean checkExtraStartConditions(ServerLevel p_147650_, Mob p_147651_) {
      boolean flag = p_147651_.isOnGround() && !p_147651_.isInWater() && !p_147651_.isInLava() && !p_147650_.getBlockState(p_147651_.blockPosition()).is(Blocks.HONEY_BLOCK);
      if (!flag) {
         p_147651_.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample(p_147650_.random) / 2);
      }

      return flag;
   }

   protected boolean canStillUse(ServerLevel p_147653_, Mob p_147654_, long p_147655_) {
      boolean flag = this.initialPosition.isPresent() && this.initialPosition.get().equals(p_147654_.position()) && this.findJumpTries > 0 && !p_147654_.isInWaterOrBubble() && (this.chosenJump != null || !this.jumpCandidates.isEmpty());
      if (!flag && p_147654_.getBrain().getMemory(MemoryModuleType.LONG_JUMP_MID_JUMP).isEmpty()) {
         p_147654_.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample(p_147653_.random) / 2);
         p_147654_.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
      }

      return flag;
   }

   protected void start(ServerLevel p_147676_, E p_147677_, long p_147678_) {
      this.chosenJump = null;
      this.findJumpTries = 20;
      this.initialPosition = Optional.of(p_147677_.position());
      BlockPos blockpos = p_147677_.blockPosition();
      int i = blockpos.getX();
      int j = blockpos.getY();
      int k = blockpos.getZ();
      this.jumpCandidates = BlockPos.betweenClosedStream(i - this.maxLongJumpWidth, j - this.maxLongJumpHeight, k - this.maxLongJumpWidth, i + this.maxLongJumpWidth, j + this.maxLongJumpHeight, k + this.maxLongJumpWidth).filter((p_217317_) -> {
         return !p_217317_.equals(blockpos);
      }).map((p_217314_) -> {
         return new LongJumpToRandomPos.PossibleJump(p_217314_.immutable(), Mth.ceil(blockpos.distSqr(p_217314_)));
      }).collect(Collectors.toCollection(Lists::newArrayList));
   }

   protected void tick(ServerLevel p_147680_, E p_147681_, long p_147682_) {
      if (this.chosenJump != null) {
         if (p_147682_ - this.prepareJumpStart >= 40L) {
            p_147681_.setYRot(p_147681_.yBodyRot);
            p_147681_.setDiscardFriction(true);
            double d0 = this.chosenJump.length();
            double d1 = d0 + p_147681_.getJumpBoostPower();
            p_147681_.setDeltaMovement(this.chosenJump.scale(d1 / d0));
            p_147681_.getBrain().setMemory(MemoryModuleType.LONG_JUMP_MID_JUMP, true);
            p_147680_.playSound((Player)null, p_147681_, this.getJumpSound.apply(p_147681_), SoundSource.NEUTRAL, 1.0F, 1.0F);
         }
      } else {
         --this.findJumpTries;
         this.pickCandidate(p_147680_, p_147681_, p_147682_);
      }

   }

   protected void pickCandidate(ServerLevel p_217319_, E p_217320_, long p_217321_) {
      while(true) {
         if (!this.jumpCandidates.isEmpty()) {
            Optional<LongJumpToRandomPos.PossibleJump> optional = this.getJumpCandidate(p_217319_);
            if (optional.isEmpty()) {
               continue;
            }

            LongJumpToRandomPos.PossibleJump longjumptorandompos$possiblejump = optional.get();
            BlockPos blockpos = longjumptorandompos$possiblejump.getJumpTarget();
            if (!this.isAcceptableLandingPosition(p_217319_, p_217320_, blockpos)) {
               continue;
            }

            Vec3 vec3 = Vec3.atCenterOf(blockpos);
            Vec3 vec31 = this.calculateOptimalJumpVector(p_217320_, vec3);
            if (vec31 == null) {
               continue;
            }

            p_217320_.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(blockpos));
            PathNavigation pathnavigation = p_217320_.getNavigation();
            Path path = pathnavigation.createPath(blockpos, 0, 8);
            if (path != null && path.canReach()) {
               continue;
            }

            this.chosenJump = vec31;
            this.prepareJumpStart = p_217321_;
            return;
         }

         return;
      }
   }

   protected Optional<LongJumpToRandomPos.PossibleJump> getJumpCandidate(ServerLevel p_217299_) {
      Optional<LongJumpToRandomPos.PossibleJump> optional = WeightedRandom.getRandomItem(p_217299_.random, this.jumpCandidates);
      optional.ifPresent(this.jumpCandidates::remove);
      return optional;
   }

   protected boolean isAcceptableLandingPosition(ServerLevel p_217300_, E p_217301_, BlockPos p_217302_) {
      BlockPos blockpos = p_217301_.blockPosition();
      int i = blockpos.getX();
      int j = blockpos.getZ();
      if (i == p_217302_.getX() && j == p_217302_.getZ()) {
         return false;
      } else if (!p_217301_.getNavigation().isStableDestination(p_217302_) && !this.acceptableLandingSpot.test(p_217300_.getBlockState(p_217302_.below()))) {
         return false;
      } else {
         return p_217301_.getPathfindingMalus(WalkNodeEvaluator.getBlockPathTypeStatic(p_217301_.level, p_217302_.mutable())) == 0.0F;
      }
   }

   @Nullable
   protected Vec3 calculateOptimalJumpVector(Mob p_217304_, Vec3 p_217305_) {
      List<Integer> list = Lists.newArrayList(ALLOWED_ANGLES);
      Collections.shuffle(list);

      for(int i : list) {
         Vec3 vec3 = this.calculateJumpVectorForAngle(p_217304_, p_217305_, i);
         if (vec3 != null) {
            return vec3;
         }
      }

      return null;
   }

   @Nullable
   private Vec3 calculateJumpVectorForAngle(Mob p_217307_, Vec3 p_217308_, int p_217309_) {
      Vec3 vec3 = p_217307_.position();
      Vec3 vec31 = (new Vec3(p_217308_.x - vec3.x, 0.0D, p_217308_.z - vec3.z)).normalize().scale(0.5D);
      p_217308_ = p_217308_.subtract(vec31);
      Vec3 vec32 = p_217308_.subtract(vec3);
      float f = (float)p_217309_ * (float)Math.PI / 180.0F;
      double d0 = Math.atan2(vec32.z, vec32.x);
      double d1 = vec32.subtract(0.0D, vec32.y, 0.0D).lengthSqr();
      double d2 = Math.sqrt(d1);
      double d3 = vec32.y;
      double d4 = Math.sin((double)(2.0F * f));
      double d5 = 0.08D;
      double d6 = Math.pow(Math.cos((double)f), 2.0D);
      double d7 = Math.sin((double)f);
      double d8 = Math.cos((double)f);
      double d9 = Math.sin(d0);
      double d10 = Math.cos(d0);
      double d11 = d1 * 0.08D / (d2 * d4 - 2.0D * d3 * d6);
      if (d11 < 0.0D) {
         return null;
      } else {
         double d12 = Math.sqrt(d11);
         if (d12 > (double)this.maxJumpVelocity) {
            return null;
         } else {
            double d13 = d12 * d8;
            double d14 = d12 * d7;
            int i = Mth.ceil(d2 / d13) * 2;
            double d15 = 0.0D;
            Vec3 vec33 = null;

            for(int j = 0; j < i - 1; ++j) {
               d15 += d2 / (double)i;
               double d16 = d7 / d8 * d15 - Math.pow(d15, 2.0D) * 0.08D / (2.0D * d11 * Math.pow(d8, 2.0D));
               double d17 = d15 * d10;
               double d18 = d15 * d9;
               Vec3 vec34 = new Vec3(vec3.x + d17, vec3.y + d16, vec3.z + d18);
               if (vec33 != null && !this.isClearTransition(p_217307_, vec33, vec34)) {
                  return null;
               }

               vec33 = vec34;
            }

            return (new Vec3(d13 * d10, d14, d13 * d9)).scale((double)0.95F);
         }
      }
   }

   private boolean isClearTransition(Mob p_147664_, Vec3 p_147665_, Vec3 p_147666_) {
      EntityDimensions entitydimensions = p_147664_.getDimensions(Pose.LONG_JUMPING);
      Vec3 vec3 = p_147666_.subtract(p_147665_);
      double d0 = (double)Math.min(entitydimensions.width, entitydimensions.height);
      int i = Mth.ceil(vec3.length() / d0);
      Vec3 vec31 = vec3.normalize();
      Vec3 vec32 = p_147665_;

      for(int j = 0; j < i; ++j) {
         vec32 = j == i - 1 ? p_147666_ : vec32.add(vec31.scale(d0 * (double)0.9F));
         AABB aabb = entitydimensions.makeBoundingBox(vec32);
         if (!p_147664_.level.noCollision(p_147664_, aabb)) {
            return false;
         }
      }

      return true;
   }

   public static class PossibleJump extends WeightedEntry.IntrusiveBase {
      private final BlockPos jumpTarget;

      public PossibleJump(BlockPos p_217323_, int p_217324_) {
         super(p_217324_);
         this.jumpTarget = p_217323_;
      }

      public BlockPos getJumpTarget() {
         return this.jumpTarget;
      }
   }
}