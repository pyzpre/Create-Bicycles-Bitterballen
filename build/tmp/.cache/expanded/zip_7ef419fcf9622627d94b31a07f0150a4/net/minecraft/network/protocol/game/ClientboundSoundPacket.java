package net.minecraft.network.protocol.game;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.apache.commons.lang3.Validate;

public class ClientboundSoundPacket implements Packet<ClientGamePacketListener> {
   public static final float LOCATION_ACCURACY = 8.0F;
   private final SoundEvent sound;
   private final SoundSource source;
   private final int x;
   private final int y;
   private final int z;
   private final float volume;
   private final float pitch;
   private final long seed;

   public ClientboundSoundPacket(SoundEvent p_237840_, SoundSource p_237841_, double p_237842_, double p_237843_, double p_237844_, float p_237845_, float p_237846_, long p_237847_) {
      Validate.notNull(p_237840_, "sound");
      this.sound = p_237840_;
      this.source = p_237841_;
      this.x = (int)(p_237842_ * 8.0D);
      this.y = (int)(p_237843_ * 8.0D);
      this.z = (int)(p_237844_ * 8.0D);
      this.volume = p_237845_;
      this.pitch = p_237846_;
      this.seed = p_237847_;
   }

   public ClientboundSoundPacket(FriendlyByteBuf p_179422_) {
      this.sound = p_179422_.readById(Registry.SOUND_EVENT);
      this.source = p_179422_.readEnum(SoundSource.class);
      this.x = p_179422_.readInt();
      this.y = p_179422_.readInt();
      this.z = p_179422_.readInt();
      this.volume = p_179422_.readFloat();
      this.pitch = p_179422_.readFloat();
      this.seed = p_179422_.readLong();
   }

   public void write(FriendlyByteBuf p_133457_) {
      p_133457_.writeId(Registry.SOUND_EVENT, this.sound);
      p_133457_.writeEnum(this.source);
      p_133457_.writeInt(this.x);
      p_133457_.writeInt(this.y);
      p_133457_.writeInt(this.z);
      p_133457_.writeFloat(this.volume);
      p_133457_.writeFloat(this.pitch);
      p_133457_.writeLong(this.seed);
   }

   public SoundEvent getSound() {
      return this.sound;
   }

   public SoundSource getSource() {
      return this.source;
   }

   public double getX() {
      return (double)((float)this.x / 8.0F);
   }

   public double getY() {
      return (double)((float)this.y / 8.0F);
   }

   public double getZ() {
      return (double)((float)this.z / 8.0F);
   }

   public float getVolume() {
      return this.volume;
   }

   public float getPitch() {
      return this.pitch;
   }

   public long getSeed() {
      return this.seed;
   }

   public void handle(ClientGamePacketListener p_133454_) {
      p_133454_.handleSoundEvent(this);
   }
}