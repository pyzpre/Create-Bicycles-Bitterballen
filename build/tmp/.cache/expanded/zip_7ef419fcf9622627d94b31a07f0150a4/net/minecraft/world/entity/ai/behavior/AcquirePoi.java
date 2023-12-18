package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.pathfinder.Path;

public class AcquirePoi extends Behavior<PathfinderMob> {
   private static final int BATCH_SIZE = 5;
   private static final int RATE = 20;
   public static final int SCAN_RANGE = 48;
   private final Predicate<Holder<PoiType>> poiType;
   private final MemoryModuleType<GlobalPos> memoryToAcquire;
   private final boolean onlyIfAdult;
   private final Optional<Byte> onPoiAcquisitionEvent;
   private long nextScheduledStart;
   private final Long2ObjectMap<AcquirePoi.JitteredLinearRetry> batchCache = new Long2ObjectOpenHashMap<>();

   public AcquirePoi(Predicate<Holder<PoiType>> p_217087_, MemoryModuleType<GlobalPos> p_217088_, MemoryModuleType<GlobalPos> p_217089_, boolean p_217090_, Optional<Byte> p_217091_) {
      super(constructEntryConditionMap(p_217088_, p_217089_));
      this.poiType = p_217087_;
      this.memoryToAcquire = p_217089_;
      this.onlyIfAdult = p_217090_;
      this.onPoiAcquisitionEvent = p_217091_;
   }

   public AcquirePoi(Predicate<Holder<PoiType>> p_217093_, MemoryModuleType<GlobalPos> p_217094_, boolean p_217095_, Optional<Byte> p_217096_) {
      this(p_217093_, p_217094_, p_217094_, p_217095_, p_217096_);
   }

   private static ImmutableMap<MemoryModuleType<?>, MemoryStatus> constructEntryConditionMap(MemoryModuleType<GlobalPos> p_22362_, MemoryModuleType<GlobalPos> p_22363_) {
      ImmutableMap.Builder<MemoryModuleType<?>, MemoryStatus> builder = ImmutableMap.builder();
      builder.put(p_22362_, MemoryStatus.VALUE_ABSENT);
      if (p_22363_ != p_22362_) {
         builder.put(p_22363_, MemoryStatus.VALUE_ABSENT);
      }

      return builder.build();
   }

   protected boolean checkExtraStartConditions(ServerLevel p_22347_, PathfinderMob p_22348_) {
      if (this.onlyIfAdult && p_22348_.isBaby()) {
         return false;
      } else if (this.nextScheduledStart == 0L) {
         this.nextScheduledStart = p_22348_.level.getGameTime() + (long)p_22347_.random.nextInt(20);
         return false;
      } else {
         return p_22347_.getGameTime() >= this.nextScheduledStart;
      }
   }

   protected void start(ServerLevel p_22350_, PathfinderMob p_22351_, long p_22352_) {
      this.nextScheduledStart = p_22352_ + 20L + (long)p_22350_.getRandom().nextInt(20);
      PoiManager poimanager = p_22350_.getPoiManager();
      this.batchCache.long2ObjectEntrySet().removeIf((p_22338_) -> {
         return !p_22338_.getValue().isStillValid(p_22352_);
      });
      Predicate<BlockPos> predicate = (p_22335_) -> {
         AcquirePoi.JitteredLinearRetry acquirepoi$jitteredlinearretry = this.batchCache.get(p_22335_.asLong());
         if (acquirepoi$jitteredlinearretry == null) {
            return true;
         } else if (!acquirepoi$jitteredlinearretry.shouldRetry(p_22352_)) {
            return false;
         } else {
            acquirepoi$jitteredlinearretry.markAttempt(p_22352_);
            return true;
         }
      };
      Set<Pair<Holder<PoiType>, BlockPos>> set = poimanager.findAllClosestFirstWithType(this.poiType, predicate, p_22351_.blockPosition(), 48, PoiManager.Occupancy.HAS_SPACE).limit(5L).collect(Collectors.toSet());
      Path path = findPathToPois(p_22351_, set);
      if (path != null && path.canReach()) {
         BlockPos blockpos = path.getTarget();
         poimanager.getType(blockpos).ifPresent((p_217105_) -> {
            poimanager.take(this.poiType, (p_217108_, p_217109_) -> {
               return p_217109_.equals(blockpos);
            }, blockpos, 1);
            p_22351_.getBrain().setMemory(this.memoryToAcquire, GlobalPos.of(p_22350_.dimension(), blockpos));
            this.onPoiAcquisitionEvent.ifPresent((p_147369_) -> {
               p_22350_.broadcastEntityEvent(p_22351_, p_147369_);
            });
            this.batchCache.clear();
            DebugPackets.sendPoiTicketCountPacket(p_22350_, blockpos);
         });
      } else {
         for(Pair<Holder<PoiType>, BlockPos> pair : set) {
            this.batchCache.computeIfAbsent(pair.getSecond().asLong(), (p_22360_) -> {
               return new AcquirePoi.JitteredLinearRetry(p_22351_.level.random, p_22352_);
            });
         }
      }

   }

   @Nullable
   public static Path findPathToPois(Mob p_217098_, Set<Pair<Holder<PoiType>, BlockPos>> p_217099_) {
      if (p_217099_.isEmpty()) {
         return null;
      } else {
         Set<BlockPos> set = new HashSet<>();
         int i = 1;

         for(Pair<Holder<PoiType>, BlockPos> pair : p_217099_) {
            i = Math.max(i, pair.getFirst().value().validRange());
            set.add(pair.getSecond());
         }

         return p_217098_.getNavigation().createPath(set, i);
      }
   }

   static class JitteredLinearRetry {
      private static final int MIN_INTERVAL_INCREASE = 40;
      private static final int MAX_INTERVAL_INCREASE = 80;
      private static final int MAX_RETRY_PATHFINDING_INTERVAL = 400;
      private final RandomSource random;
      private long previousAttemptTimestamp;
      private long nextScheduledAttemptTimestamp;
      private int currentDelay;

      JitteredLinearRetry(RandomSource p_217111_, long p_217112_) {
         this.random = p_217111_;
         this.markAttempt(p_217112_);
      }

      public void markAttempt(long p_22381_) {
         this.previousAttemptTimestamp = p_22381_;
         int i = this.currentDelay + this.random.nextInt(40) + 40;
         this.currentDelay = Math.min(i, 400);
         this.nextScheduledAttemptTimestamp = p_22381_ + (long)this.currentDelay;
      }

      public boolean isStillValid(long p_22383_) {
         return p_22383_ - this.previousAttemptTimestamp < 400L;
      }

      public boolean shouldRetry(long p_22385_) {
         return p_22385_ >= this.nextScheduledAttemptTimestamp;
      }

      public String toString() {
         return "RetryMarker{, previousAttemptAt=" + this.previousAttemptTimestamp + ", nextScheduledAttemptAt=" + this.nextScheduledAttemptTimestamp + ", currentDelay=" + this.currentDelay + "}";
      }
   }
}