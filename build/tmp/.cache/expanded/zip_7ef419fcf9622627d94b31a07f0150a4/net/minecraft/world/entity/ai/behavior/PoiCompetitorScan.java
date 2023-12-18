package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

public class PoiCompetitorScan extends Behavior<Villager> {
   final VillagerProfession profession;

   public PoiCompetitorScan(VillagerProfession p_23710_) {
      super(ImmutableMap.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
      this.profession = p_23710_;
   }

   protected void start(ServerLevel p_23716_, Villager p_23717_, long p_23718_) {
      GlobalPos globalpos = p_23717_.getBrain().getMemory(MemoryModuleType.JOB_SITE).get();
      p_23716_.getPoiManager().getType(globalpos.pos()).ifPresent((p_217328_) -> {
         BehaviorUtils.getNearbyVillagersWithCondition(p_23717_, (p_217339_) -> {
            return this.competesForSameJobsite(globalpos, p_217328_, p_217339_);
         }).reduce(p_23717_, PoiCompetitorScan::selectWinner);
      });
   }

   private static Villager selectWinner(Villager p_23725_, Villager p_23726_) {
      Villager villager;
      Villager villager1;
      if (p_23725_.getVillagerXp() > p_23726_.getVillagerXp()) {
         villager = p_23725_;
         villager1 = p_23726_;
      } else {
         villager = p_23726_;
         villager1 = p_23725_;
      }

      villager1.getBrain().eraseMemory(MemoryModuleType.JOB_SITE);
      return villager;
   }

   private boolean competesForSameJobsite(GlobalPos p_217330_, Holder<PoiType> p_217331_, Villager p_217332_) {
      return this.hasJobSite(p_217332_) && p_217330_.equals(p_217332_.getBrain().getMemory(MemoryModuleType.JOB_SITE).get()) && this.hasMatchingProfession(p_217331_, p_217332_.getVillagerData().getProfession());
   }

   private boolean hasMatchingProfession(Holder<PoiType> p_217334_, VillagerProfession p_217335_) {
      return p_217335_.heldJobSite().test(p_217334_);
   }

   private boolean hasJobSite(Villager p_23723_) {
      return p_23723_.getBrain().getMemory(MemoryModuleType.JOB_SITE).isPresent();
   }
}