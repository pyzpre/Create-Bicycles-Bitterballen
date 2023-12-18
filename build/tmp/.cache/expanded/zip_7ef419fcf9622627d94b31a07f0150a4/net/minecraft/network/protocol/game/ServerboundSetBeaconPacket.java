package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;

public class ServerboundSetBeaconPacket implements Packet<ServerGamePacketListener> {
   private final Optional<MobEffect> primary;
   private final Optional<MobEffect> secondary;

   public ServerboundSetBeaconPacket(Optional<MobEffect> p_237989_, Optional<MobEffect> p_237990_) {
      this.primary = p_237989_;
      this.secondary = p_237990_;
   }

   public ServerboundSetBeaconPacket(FriendlyByteBuf p_179749_) {
      this.primary = p_179749_.readOptional((p_238002_) -> {
         return p_238002_.readById(Registry.MOB_EFFECT);
      });
      this.secondary = p_179749_.readOptional((p_237996_) -> {
         return p_237996_.readById(Registry.MOB_EFFECT);
      });
   }

   public void write(FriendlyByteBuf p_134486_) {
      p_134486_.writeOptional(this.primary, (p_237998_, p_237999_) -> {
         p_237998_.writeId(Registry.MOB_EFFECT, p_237999_);
      });
      p_134486_.writeOptional(this.secondary, (p_237992_, p_237993_) -> {
         p_237992_.writeId(Registry.MOB_EFFECT, p_237993_);
      });
   }

   public void handle(ServerGamePacketListener p_134483_) {
      p_134483_.handleSetBeaconPacket(this);
   }

   public Optional<MobEffect> getPrimary() {
      return this.primary;
   }

   public Optional<MobEffect> getSecondary() {
      return this.secondary;
   }
}