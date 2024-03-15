package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.protocol.Packet;

public record ServerboundChatAckPacket(LastSeenMessages.Update lastSeenMessages) implements Packet<ServerGamePacketListener> {
   public ServerboundChatAckPacket(FriendlyByteBuf p_242339_) {
      this(new LastSeenMessages.Update(p_242339_));
   }

   public void write(FriendlyByteBuf p_242345_) {
      this.lastSeenMessages.write(p_242345_);
   }

   public void handle(ServerGamePacketListener p_242391_) {
      p_242391_.handleChatAck(this);
   }
}