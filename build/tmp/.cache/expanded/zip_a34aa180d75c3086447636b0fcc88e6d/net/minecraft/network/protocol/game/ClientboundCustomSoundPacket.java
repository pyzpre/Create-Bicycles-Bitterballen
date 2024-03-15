package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class ClientboundCustomSoundPacket implements Packet<ClientGamePacketListener> {
   public static final float LOCATION_ACCURACY = 8.0F;
   private final ResourceLocation name;
   private final SoundSource source;
   private final int x;
   private final int y;
   private final int z;
   private final float volume;
   private final float pitch;
   private final long seed;

   public ClientboundCustomSoundPacket(ResourceLocation p_237699_, SoundSource p_237700_, Vec3 p_237701_, float p_237702_, float p_237703_, long p_237704_) {
      this.name = p_237699_;
      this.source = p_237700_;
      this.x = (int)(p_237701_.x * 8.0D);
      this.y = (int)(p_237701_.y * 8.0D);
      this.z = (int)(p_237701_.z * 8.0D);
      this.volume = p_237702_;
      this.pitch = p_237703_;
      this.seed = p_237704_;
   }

   public ClientboundCustomSoundPacket(FriendlyByteBuf p_178839_) {
      this.name = p_178839_.readResourceLocation();
      this.source = p_178839_.readEnum(SoundSource.class);
      this.x = p_178839_.readInt();
      this.y = p_178839_.readInt();
      this.z = p_178839_.readInt();
      this.volume = p_178839_.readFloat();
      this.pitch = p_178839_.readFloat();
      this.seed = p_178839_.readLong();
   }

   public void write(FriendlyByteBuf p_132068_) {
      p_132068_.writeResourceLocation(this.name);
      p_132068_.writeEnum(this.source);
      p_132068_.writeInt(this.x);
      p_132068_.writeInt(this.y);
      p_132068_.writeInt(this.z);
      p_132068_.writeFloat(this.volume);
      p_132068_.writeFloat(this.pitch);
      p_132068_.writeLong(this.seed);
   }

   public ResourceLocation getName() {
      return this.name;
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

   public void handle(ClientGamePacketListener p_132065_) {
      p_132065_.handleCustomSoundEvent(this);
   }
}