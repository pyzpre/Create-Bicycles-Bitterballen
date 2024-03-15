package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.Nullable;

public record ClientboundChatPreviewPacket(int queryId, @Nullable Component preview) implements Packet<ClientGamePacketListener> {
   public ClientboundChatPreviewPacket(FriendlyByteBuf p_237600_) {
      this(p_237600_.readInt(), p_237600_.readNullable(FriendlyByteBuf::readComponent));
   }

   public void write(FriendlyByteBuf p_237602_) {
      p_237602_.writeInt(this.queryId);
      p_237602_.writeNullable(this.preview, FriendlyByteBuf::writeComponent);
   }

   public boolean isSkippable() {
      return true;
   }

   public void handle(ClientGamePacketListener p_237606_) {
      p_237606_.handleChatPreview(this);
   }
}