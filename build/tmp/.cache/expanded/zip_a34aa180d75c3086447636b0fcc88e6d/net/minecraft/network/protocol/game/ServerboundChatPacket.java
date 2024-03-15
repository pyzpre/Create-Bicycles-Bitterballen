package net.minecraft.network.protocol.game;

import java.time.Instant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSigner;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;

public record ServerboundChatPacket(String message, Instant timeStamp, long salt, MessageSignature signature, boolean signedPreview, LastSeenMessages.Update lastSeenMessages) implements Packet<ServerGamePacketListener> {
   public ServerboundChatPacket(FriendlyByteBuf p_179545_) {
      this(p_179545_.readUtf(256), p_179545_.readInstant(), p_179545_.readLong(), new MessageSignature(p_179545_), p_179545_.readBoolean(), new LastSeenMessages.Update(p_179545_));
   }

   public void write(FriendlyByteBuf p_133839_) {
      p_133839_.writeUtf(this.message, 256);
      p_133839_.writeInstant(this.timeStamp);
      p_133839_.writeLong(this.salt);
      this.signature.write(p_133839_);
      p_133839_.writeBoolean(this.signedPreview);
      this.lastSeenMessages.write(p_133839_);
   }

   public void handle(ServerGamePacketListener p_133836_) {
      p_133836_.handleChat(this);
   }

   public MessageSigner getSigner(ServerPlayer p_241405_) {
      return new MessageSigner(p_241405_.getUUID(), this.timeStamp, this.salt);
   }
}