package net.minecraft.network.protocol.game;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.Validate;

public class ClientboundSoundEntityPacket implements Packet<ClientGamePacketListener> {
   private final SoundEvent sound;
   private final SoundSource source;
   private final int id;
   private final float volume;
   private final float pitch;
   private final long seed;

   public ClientboundSoundEntityPacket(SoundEvent p_237831_, SoundSource p_237832_, Entity p_237833_, float p_237834_, float p_237835_, long p_237836_) {
      Validate.notNull(p_237831_, "sound");
      this.sound = p_237831_;
      this.source = p_237832_;
      this.id = p_237833_.getId();
      this.volume = p_237834_;
      this.pitch = p_237835_;
      this.seed = p_237836_;
   }

   public ClientboundSoundEntityPacket(FriendlyByteBuf p_179419_) {
      this.sound = p_179419_.readById(Registry.SOUND_EVENT);
      this.source = p_179419_.readEnum(SoundSource.class);
      this.id = p_179419_.readVarInt();
      this.volume = p_179419_.readFloat();
      this.pitch = p_179419_.readFloat();
      this.seed = p_179419_.readLong();
   }

   public void write(FriendlyByteBuf p_133428_) {
      p_133428_.writeId(Registry.SOUND_EVENT, this.sound);
      p_133428_.writeEnum(this.source);
      p_133428_.writeVarInt(this.id);
      p_133428_.writeFloat(this.volume);
      p_133428_.writeFloat(this.pitch);
      p_133428_.writeLong(this.seed);
   }

   public SoundEvent getSound() {
      return this.sound;
   }

   public SoundSource getSource() {
      return this.source;
   }

   public int getId() {
      return this.id;
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

   public void handle(ClientGamePacketListener p_133425_) {
      p_133425_.handleSoundEntityEvent(this);
   }
}