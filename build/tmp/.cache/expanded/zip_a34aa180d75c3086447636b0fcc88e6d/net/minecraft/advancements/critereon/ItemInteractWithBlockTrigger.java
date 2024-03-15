package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ItemInteractWithBlockTrigger extends SimpleCriterionTrigger<ItemInteractWithBlockTrigger.TriggerInstance> {
   final ResourceLocation id;

   public ItemInteractWithBlockTrigger(ResourceLocation p_220038_) {
      this.id = p_220038_;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public ItemInteractWithBlockTrigger.TriggerInstance createInstance(JsonObject p_220045_, EntityPredicate.Composite p_220046_, DeserializationContext p_220047_) {
      LocationPredicate locationpredicate = LocationPredicate.fromJson(p_220045_.get("location"));
      ItemPredicate itempredicate = ItemPredicate.fromJson(p_220045_.get("item"));
      return new ItemInteractWithBlockTrigger.TriggerInstance(this.id, p_220046_, locationpredicate, itempredicate);
   }

   public void trigger(ServerPlayer p_220041_, BlockPos p_220042_, ItemStack p_220043_) {
      BlockState blockstate = p_220041_.getLevel().getBlockState(p_220042_);
      this.trigger(p_220041_, (p_220053_) -> {
         return p_220053_.matches(blockstate, p_220041_.getLevel(), p_220042_, p_220043_);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final LocationPredicate location;
      private final ItemPredicate item;

      public TriggerInstance(ResourceLocation p_220061_, EntityPredicate.Composite p_220062_, LocationPredicate p_220063_, ItemPredicate p_220064_) {
         super(p_220061_, p_220062_);
         this.location = p_220063_;
         this.item = p_220064_;
      }

      public static ItemInteractWithBlockTrigger.TriggerInstance itemUsedOnBlock(LocationPredicate.Builder p_220066_, ItemPredicate.Builder p_220067_) {
         return new ItemInteractWithBlockTrigger.TriggerInstance(CriteriaTriggers.ITEM_USED_ON_BLOCK.id, EntityPredicate.Composite.ANY, p_220066_.build(), p_220067_.build());
      }

      public static ItemInteractWithBlockTrigger.TriggerInstance allayDropItemOnBlock(LocationPredicate.Builder p_220076_, ItemPredicate.Builder p_220077_) {
         return new ItemInteractWithBlockTrigger.TriggerInstance(CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.id, EntityPredicate.Composite.ANY, p_220076_.build(), p_220077_.build());
      }

      public boolean matches(BlockState p_220071_, ServerLevel p_220072_, BlockPos p_220073_, ItemStack p_220074_) {
         return !this.location.matches(p_220072_, (double)p_220073_.getX() + 0.5D, (double)p_220073_.getY() + 0.5D, (double)p_220073_.getZ() + 0.5D) ? false : this.item.matches(p_220074_);
      }

      public JsonObject serializeToJson(SerializationContext p_220069_) {
         JsonObject jsonobject = super.serializeToJson(p_220069_);
         jsonobject.add("location", this.location.serializeToJson());
         jsonobject.add("item", this.item.serializeToJson());
         return jsonobject;
      }
   }
}