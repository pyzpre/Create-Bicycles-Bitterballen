package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;

public class SetRoarTarget<E extends Warden> extends Behavior<E> {
   private final Function<E, Optional<? extends LivingEntity>> targetFinderFunction;

   public SetRoarTarget(Function<E, Optional<? extends LivingEntity>> p_217609_) {
      super(ImmutableMap.of(MemoryModuleType.ROAR_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED));
      this.targetFinderFunction = p_217609_;
   }

   protected boolean checkExtraStartConditions(ServerLevel p_217618_, E p_217619_) {
      return this.targetFinderFunction.apply(p_217619_).filter(p_217619_::canTargetEntity).isPresent();
   }

   protected void start(ServerLevel p_217621_, E p_217622_, long p_217623_) {
      this.targetFinderFunction.apply(p_217622_).ifPresent((p_217626_) -> {
         p_217622_.getBrain().setMemory(MemoryModuleType.ROAR_TARGET, p_217626_);
         p_217622_.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      });
   }
}