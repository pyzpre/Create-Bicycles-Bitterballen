package net.minecraft.network.chat;

import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;

public record ChatMessageContent(String plain, Component decorated) {
   public ChatMessageContent(String p_242420_) {
      this(p_242420_, Component.literal(p_242420_));
   }

   public boolean isDecorated() {
      return !this.decorated.equals(Component.literal(this.plain));
   }

   public static ChatMessageContent read(FriendlyByteBuf p_242370_) {
      String s = p_242370_.readUtf(256);
      Component component = p_242370_.readNullable(FriendlyByteBuf::readComponent);
      return new ChatMessageContent(s, Objects.requireNonNullElse(component, Component.literal(s)));
   }

   public static void write(FriendlyByteBuf p_242211_, ChatMessageContent p_242235_) {
      p_242211_.writeUtf(p_242235_.plain(), 256);
      Component component = p_242235_.isDecorated() ? p_242235_.decorated() : null;
      p_242211_.writeNullable(component, FriendlyByteBuf::writeComponent);
   }
}