package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class VibrationListener implements GameEventListener {
   protected final PositionSource listenerSource;
   protected final int listenerRange;
   protected final VibrationListener.VibrationListenerConfig config;
   @Nullable
   protected VibrationListener.ReceivingEvent receivingEvent;
   protected float receivingDistance;
   protected int travelTimeInTicks;

   public static Codec<VibrationListener> codec(VibrationListener.VibrationListenerConfig p_223782_) {
      return RecordCodecBuilder.create((p_223785_) -> {
         return p_223785_.group(PositionSource.CODEC.fieldOf("source").forGetter((p_223802_) -> {
            return p_223802_.listenerSource;
         }), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("range").forGetter((p_223800_) -> {
            return p_223800_.listenerRange;
         }), VibrationListener.ReceivingEvent.CODEC.optionalFieldOf("event").forGetter((p_223798_) -> {
            return Optional.ofNullable(p_223798_.receivingEvent);
         }), Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("event_distance").orElse(0.0F).forGetter((p_223796_) -> {
            return p_223796_.receivingDistance;
         }), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_delay").orElse(0).forGetter((p_223794_) -> {
            return p_223794_.travelTimeInTicks;
         })).apply(p_223785_, (p_223788_, p_223789_, p_223790_, p_223791_, p_223792_) -> {
            return new VibrationListener(p_223788_, p_223789_, p_223782_, p_223790_.orElse((VibrationListener.ReceivingEvent)null), p_223791_, p_223792_);
         });
      });
   }

   public VibrationListener(PositionSource p_223760_, int p_223761_, VibrationListener.VibrationListenerConfig p_223762_, @Nullable VibrationListener.ReceivingEvent p_223763_, float p_223764_, int p_223765_) {
      this.listenerSource = p_223760_;
      this.listenerRange = p_223761_;
      this.config = p_223762_;
      this.receivingEvent = p_223763_;
      this.receivingDistance = p_223764_;
      this.travelTimeInTicks = p_223765_;
   }

   public void tick(Level p_157899_) {
      if (p_157899_ instanceof ServerLevel serverlevel) {
         if (this.receivingEvent != null) {
            --this.travelTimeInTicks;
            if (this.travelTimeInTicks <= 0) {
               this.travelTimeInTicks = 0;
               this.config.onSignalReceive(serverlevel, this, new BlockPos(this.receivingEvent.pos), this.receivingEvent.gameEvent, this.receivingEvent.getEntity(serverlevel).orElse((Entity)null), this.receivingEvent.getProjectileOwner(serverlevel).orElse((Entity)null), this.receivingDistance);
               this.receivingEvent = null;
            }
         }
      }

   }

   public PositionSource getListenerSource() {
      return this.listenerSource;
   }

   public int getListenerRadius() {
      return this.listenerRange;
   }

   public boolean handleGameEvent(ServerLevel p_223767_, GameEvent.Message p_223768_) {
      if (this.receivingEvent != null) {
         return false;
      } else {
         GameEvent gameevent = p_223768_.gameEvent();
         GameEvent.Context gameevent$context = p_223768_.context();
         if (!this.config.isValidVibration(gameevent, gameevent$context)) {
            return false;
         } else {
            Optional<Vec3> optional = this.listenerSource.getPosition(p_223767_);
            if (optional.isEmpty()) {
               return false;
            } else {
               Vec3 vec3 = p_223768_.source();
               Vec3 vec31 = optional.get();
               if (!this.config.shouldListen(p_223767_, this, new BlockPos(vec3), gameevent, gameevent$context)) {
                  return false;
               } else if (isOccluded(p_223767_, vec3, vec31)) {
                  return false;
               } else {
                  this.scheduleSignal(p_223767_, gameevent, gameevent$context, vec3, vec31);
                  return true;
               }
            }
         }
      }
   }

   private void scheduleSignal(ServerLevel p_223770_, GameEvent p_223771_, GameEvent.Context p_223772_, Vec3 p_223773_, Vec3 p_223774_) {
      this.receivingDistance = (float)p_223773_.distanceTo(p_223774_);
      this.receivingEvent = new VibrationListener.ReceivingEvent(p_223771_, this.receivingDistance, p_223773_, p_223772_.sourceEntity());
      this.travelTimeInTicks = Mth.floor(this.receivingDistance);
      p_223770_.sendParticles(new VibrationParticleOption(this.listenerSource, this.travelTimeInTicks), p_223773_.x, p_223773_.y, p_223773_.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
      this.config.onSignalSchedule();
   }

   private static boolean isOccluded(Level p_223776_, Vec3 p_223777_, Vec3 p_223778_) {
      Vec3 vec3 = new Vec3((double)Mth.floor(p_223777_.x) + 0.5D, (double)Mth.floor(p_223777_.y) + 0.5D, (double)Mth.floor(p_223777_.z) + 0.5D);
      Vec3 vec31 = new Vec3((double)Mth.floor(p_223778_.x) + 0.5D, (double)Mth.floor(p_223778_.y) + 0.5D, (double)Mth.floor(p_223778_.z) + 0.5D);

      for(Direction direction : Direction.values()) {
         Vec3 vec32 = vec3.relative(direction, (double)1.0E-5F);
         if (p_223776_.isBlockInLine(new ClipBlockStateContext(vec32, vec31, (p_223780_) -> {
            return p_223780_.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS);
         })).getType() != HitResult.Type.BLOCK) {
            return false;
         }
      }

      return true;
   }

   public static record ReceivingEvent(GameEvent gameEvent, float distance, Vec3 pos, @Nullable UUID uuid, @Nullable UUID projectileOwnerUuid, @Nullable Entity entity) {
      public static final Codec<VibrationListener.ReceivingEvent> CODEC = RecordCodecBuilder.create((p_223835_) -> {
         return p_223835_.group(Registry.GAME_EVENT.byNameCodec().fieldOf("game_event").forGetter(VibrationListener.ReceivingEvent::gameEvent), Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("distance").forGetter(VibrationListener.ReceivingEvent::distance), Vec3.CODEC.fieldOf("pos").forGetter(VibrationListener.ReceivingEvent::pos), ExtraCodecs.UUID.optionalFieldOf("source").forGetter((p_223850_) -> {
            return Optional.ofNullable(p_223850_.uuid());
         }), ExtraCodecs.UUID.optionalFieldOf("projectile_owner").forGetter((p_223843_) -> {
            return Optional.ofNullable(p_223843_.projectileOwnerUuid());
         })).apply(p_223835_, (p_223837_, p_223838_, p_223839_, p_223840_, p_223841_) -> {
            return new VibrationListener.ReceivingEvent(p_223837_, p_223838_, p_223839_, p_223840_.orElse((UUID)null), p_223841_.orElse((UUID)null));
         });
      });

      public ReceivingEvent(GameEvent p_223817_, float p_223818_, Vec3 p_223819_, @Nullable UUID p_223820_, @Nullable UUID p_223821_) {
         this(p_223817_, p_223818_, p_223819_, p_223820_, p_223821_, (Entity)null);
      }

      public ReceivingEvent(GameEvent p_223812_, float p_223813_, Vec3 p_223814_, @Nullable Entity p_223815_) {
         this(p_223812_, p_223813_, p_223814_, p_223815_ == null ? null : p_223815_.getUUID(), getProjectileOwner(p_223815_), p_223815_);
      }

      @Nullable
      private static UUID getProjectileOwner(@Nullable Entity p_223833_) {
         if (p_223833_ instanceof Projectile projectile) {
            if (projectile.getOwner() != null) {
               return projectile.getOwner().getUUID();
            }
         }

         return null;
      }

      public Optional<Entity> getEntity(ServerLevel p_223831_) {
         return Optional.ofNullable(this.entity).or(() -> {
            return Optional.ofNullable(this.uuid).map(p_223831_::getEntity);
         });
      }

      public Optional<Entity> getProjectileOwner(ServerLevel p_223846_) {
         return this.getEntity(p_223846_).filter((p_223855_) -> {
            return p_223855_ instanceof Projectile;
         }).map((p_223848_) -> {
            return (Projectile)p_223848_;
         }).map(Projectile::getOwner).or(() -> {
            return Optional.ofNullable(this.projectileOwnerUuid).map(p_223846_::getEntity);
         });
      }
   }

   public interface VibrationListenerConfig {
      default TagKey<GameEvent> getListenableEvents() {
         return GameEventTags.VIBRATIONS;
      }

      default boolean canTriggerAvoidVibration() {
         return false;
      }

      default boolean isValidVibration(GameEvent p_223878_, GameEvent.Context p_223879_) {
         if (!p_223878_.is(this.getListenableEvents())) {
            return false;
         } else {
            Entity entity = p_223879_.sourceEntity();
            if (entity != null) {
               if (entity.isSpectator()) {
                  return false;
               }

               if (entity.isSteppingCarefully() && p_223878_.is(GameEventTags.IGNORE_VIBRATIONS_SNEAKING)) {
                  if (this.canTriggerAvoidVibration() && entity instanceof ServerPlayer) {
                     ServerPlayer serverplayer = (ServerPlayer)entity;
                     CriteriaTriggers.AVOID_VIBRATION.trigger(serverplayer);
                  }

                  return false;
               }

               if (entity.dampensVibrations()) {
                  return false;
               }
            }

            if (p_223879_.affectedState() != null) {
               return !p_223879_.affectedState().is(BlockTags.DAMPENS_VIBRATIONS);
            } else {
               return true;
            }
         }
      }

      boolean shouldListen(ServerLevel p_223872_, GameEventListener p_223873_, BlockPos p_223874_, GameEvent p_223875_, GameEvent.Context p_223876_);

      void onSignalReceive(ServerLevel p_223865_, GameEventListener p_223866_, BlockPos p_223867_, GameEvent p_223868_, @Nullable Entity p_223869_, @Nullable Entity p_223870_, float p_223871_);

      default void onSignalSchedule() {
      }
   }
}