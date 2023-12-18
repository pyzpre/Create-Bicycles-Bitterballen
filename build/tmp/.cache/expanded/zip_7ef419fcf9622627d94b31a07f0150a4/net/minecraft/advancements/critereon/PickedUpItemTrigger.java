package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class PickedUpItemTrigger extends SimpleCriterionTrigger<PickedUpItemTrigger.TriggerInstance> {
   private final ResourceLocation id;

   public PickedUpItemTrigger(ResourceLocation p_221296_) {
      this.id = p_221296_;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   protected PickedUpItemTrigger.TriggerInstance createInstance(JsonObject p_221308_, EntityPredicate.Composite p_221309_, DeserializationContext p_221310_) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(p_221308_.get("item"));
      EntityPredicate.Composite entitypredicate$composite = EntityPredicate.Composite.fromJson(p_221308_, "entity", p_221310_);
      return new PickedUpItemTrigger.TriggerInstance(this.id, p_221309_, itempredicate, entitypredicate$composite);
   }

   public void trigger(ServerPlayer p_221299_, ItemStack p_221300_, @Nullable Entity p_221301_) {
      LootContext lootcontext = EntityPredicate.createContext(p_221299_, p_221301_);
      this.trigger(p_221299_, (p_221306_) -> {
         return p_221306_.matches(p_221299_, p_221300_, lootcontext);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;
      private final EntityPredicate.Composite entity;

      public TriggerInstance(ResourceLocation p_221318_, EntityPredicate.Composite p_221319_, ItemPredicate p_221320_, EntityPredicate.Composite p_221321_) {
         super(p_221318_, p_221319_);
         this.item = p_221320_;
         this.entity = p_221321_;
      }

      public static PickedUpItemTrigger.TriggerInstance thrownItemPickedUpByEntity(EntityPredicate.Composite p_221327_, ItemPredicate p_221328_, EntityPredicate.Composite p_221329_) {
         return new PickedUpItemTrigger.TriggerInstance(CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.getId(), p_221327_, p_221328_, p_221329_);
      }

      public static PickedUpItemTrigger.TriggerInstance thrownItemPickedUpByPlayer(EntityPredicate.Composite p_221333_, ItemPredicate p_221334_, EntityPredicate.Composite p_221335_) {
         return new PickedUpItemTrigger.TriggerInstance(CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER.getId(), p_221333_, p_221334_, p_221335_);
      }

      public boolean matches(ServerPlayer p_221323_, ItemStack p_221324_, LootContext p_221325_) {
         if (!this.item.matches(p_221324_)) {
            return false;
         } else {
            return this.entity.matches(p_221325_);
         }
      }

      public JsonObject serializeToJson(SerializationContext p_221331_) {
         JsonObject jsonobject = super.serializeToJson(p_221331_);
         jsonobject.add("item", this.item.serializeToJson());
         jsonobject.add("entity", this.entity.toJson(p_221331_));
         return jsonobject;
      }
   }
}