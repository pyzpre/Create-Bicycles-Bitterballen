package net.minecraft.network.protocol.game;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class ClientboundRespawnPacket implements Packet<ClientGamePacketListener> {
   private final ResourceKey<DimensionType> dimensionType;
   private final ResourceKey<Level> dimension;
   private final long seed;
   private final GameType playerGameType;
   @Nullable
   private final GameType previousPlayerGameType;
   private final boolean isDebug;
   private final boolean isFlat;
   private final boolean keepAllPlayerData;
   private final Optional<GlobalPos> lastDeathLocation;

   public ClientboundRespawnPacket(ResourceKey<DimensionType> p_238301_, ResourceKey<Level> p_238302_, long p_238303_, GameType p_238304_, @Nullable GameType p_238305_, boolean p_238306_, boolean p_238307_, boolean p_238308_, Optional<GlobalPos> p_238309_) {
      this.dimensionType = p_238301_;
      this.dimension = p_238302_;
      this.seed = p_238303_;
      this.playerGameType = p_238304_;
      this.previousPlayerGameType = p_238305_;
      this.isDebug = p_238306_;
      this.isFlat = p_238307_;
      this.keepAllPlayerData = p_238308_;
      this.lastDeathLocation = p_238309_;
   }

   public ClientboundRespawnPacket(FriendlyByteBuf p_179191_) {
      this.dimensionType = p_179191_.readResourceKey(Registry.DIMENSION_TYPE_REGISTRY);
      this.dimension = p_179191_.readResourceKey(Registry.DIMENSION_REGISTRY);
      this.seed = p_179191_.readLong();
      this.playerGameType = GameType.byId(p_179191_.readUnsignedByte());
      this.previousPlayerGameType = GameType.byNullableId(p_179191_.readByte());
      this.isDebug = p_179191_.readBoolean();
      this.isFlat = p_179191_.readBoolean();
      this.keepAllPlayerData = p_179191_.readBoolean();
      this.lastDeathLocation = p_179191_.readOptional(FriendlyByteBuf::readGlobalPos);
   }

   public void write(FriendlyByteBuf p_132954_) {
      p_132954_.writeResourceKey(this.dimensionType);
      p_132954_.writeResourceKey(this.dimension);
      p_132954_.writeLong(this.seed);
      p_132954_.writeByte(this.playerGameType.getId());
      p_132954_.writeByte(GameType.getNullableId(this.previousPlayerGameType));
      p_132954_.writeBoolean(this.isDebug);
      p_132954_.writeBoolean(this.isFlat);
      p_132954_.writeBoolean(this.keepAllPlayerData);
      p_132954_.writeOptional(this.lastDeathLocation, FriendlyByteBuf::writeGlobalPos);
   }

   public void handle(ClientGamePacketListener p_132951_) {
      p_132951_.handleRespawn(this);
   }

   public ResourceKey<DimensionType> getDimensionType() {
      return this.dimensionType;
   }

   public ResourceKey<Level> getDimension() {
      return this.dimension;
   }

   public long getSeed() {
      return this.seed;
   }

   public GameType getPlayerGameType() {
      return this.playerGameType;
   }

   @Nullable
   public GameType getPreviousPlayerGameType() {
      return this.previousPlayerGameType;
   }

   public boolean isDebug() {
      return this.isDebug;
   }

   public boolean isFlat() {
      return this.isFlat;
   }

   public boolean shouldKeepAllPlayerData() {
      return this.keepAllPlayerData;
   }

   public Optional<GlobalPos> getLastDeathLocation() {
      return this.lastDeathLocation;
   }
}