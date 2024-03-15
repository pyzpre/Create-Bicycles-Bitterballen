package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import org.slf4j.Logger;

public class SculkSensorBlockEntity extends BlockEntity implements VibrationListener.VibrationListenerConfig {
   private static final Logger LOGGER = LogUtils.getLogger();
   private VibrationListener listener;
   private int lastVibrationFrequency;

   public SculkSensorBlockEntity(BlockPos p_155635_, BlockState p_155636_) {
      super(BlockEntityType.SCULK_SENSOR, p_155635_, p_155636_);
      this.listener = new VibrationListener(new BlockPositionSource(this.worldPosition), ((SculkSensorBlock)p_155636_.getBlock()).getListenerRange(), this, (VibrationListener.ReceivingEvent)null, 0.0F, 0);
   }

   public void load(CompoundTag p_155649_) {
      super.load(p_155649_);
      this.lastVibrationFrequency = p_155649_.getInt("last_vibration_frequency");
      if (p_155649_.contains("listener", 10)) {
         VibrationListener.codec(this).parse(new Dynamic<>(NbtOps.INSTANCE, p_155649_.getCompound("listener"))).resultOrPartial(LOGGER::error).ifPresent((p_222817_) -> {
            this.listener = p_222817_;
         });
      }

   }

   protected void saveAdditional(CompoundTag p_187511_) {
      super.saveAdditional(p_187511_);
      p_187511_.putInt("last_vibration_frequency", this.lastVibrationFrequency);
      VibrationListener.codec(this).encodeStart(NbtOps.INSTANCE, this.listener).resultOrPartial(LOGGER::error).ifPresent((p_222820_) -> {
         p_187511_.put("listener", p_222820_);
      });
   }

   public VibrationListener getListener() {
      return this.listener;
   }

   public int getLastVibrationFrequency() {
      return this.lastVibrationFrequency;
   }

   public boolean canTriggerAvoidVibration() {
      return true;
   }

   public boolean shouldListen(ServerLevel p_222811_, GameEventListener p_222812_, BlockPos p_222813_, GameEvent p_222814_, @Nullable GameEvent.Context p_222815_) {
      return !this.isRemoved() && (!p_222813_.equals(this.getBlockPos()) || p_222814_ != GameEvent.BLOCK_DESTROY && p_222814_ != GameEvent.BLOCK_PLACE) ? SculkSensorBlock.canActivate(this.getBlockState()) : false;
   }

   public void onSignalReceive(ServerLevel p_222803_, GameEventListener p_222804_, BlockPos p_222805_, GameEvent p_222806_, @Nullable Entity p_222807_, @Nullable Entity p_222808_, float p_222809_) {
      BlockState blockstate = this.getBlockState();
      if (SculkSensorBlock.canActivate(blockstate)) {
         this.lastVibrationFrequency = SculkSensorBlock.VIBRATION_FREQUENCY_FOR_EVENT.getInt(p_222806_);
         SculkSensorBlock.activate(p_222807_, p_222803_, this.worldPosition, blockstate, getRedstoneStrengthForDistance(p_222809_, p_222804_.getListenerRadius()));
      }

   }

   public void onSignalSchedule() {
      this.setChanged();
   }

   public static int getRedstoneStrengthForDistance(float p_222798_, int p_222799_) {
      double d0 = (double)p_222798_ / (double)p_222799_;
      return Math.max(1, 15 - Mth.floor(d0 * 15.0D));
   }

   public void setLastVibrationFrequency(int p_222801_) {
      this.lastVibrationFrequency = p_222801_;
   }
}