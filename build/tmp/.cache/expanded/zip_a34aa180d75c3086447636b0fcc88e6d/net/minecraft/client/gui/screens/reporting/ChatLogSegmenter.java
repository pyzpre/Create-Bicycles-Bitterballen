package net.minecraft.client.gui.screens.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatLogSegmenter<T extends LoggedChatMessage> {
   private final Function<ChatLog.Entry<T>, ChatLogSegmenter.MessageType> typeFunction;
   private final List<ChatLog.Entry<T>> messages = new ArrayList<>();
   @Nullable
   private ChatLogSegmenter.MessageType segmentType;

   public ChatLogSegmenter(Function<ChatLog.Entry<T>, ChatLogSegmenter.MessageType> p_239532_) {
      this.typeFunction = p_239532_;
   }

   public boolean accept(ChatLog.Entry<T> p_242182_) {
      ChatLogSegmenter.MessageType chatlogsegmenter$messagetype = this.typeFunction.apply(p_242182_);
      if (this.segmentType != null && chatlogsegmenter$messagetype != this.segmentType) {
         return false;
      } else {
         this.segmentType = chatlogsegmenter$messagetype;
         this.messages.add(p_242182_);
         return true;
      }
   }

   @Nullable
   public ChatLogSegmenter.Results<T> build() {
      return !this.messages.isEmpty() && this.segmentType != null ? new ChatLogSegmenter.Results<>(this.messages, this.segmentType) : null;
   }

   @OnlyIn(Dist.CLIENT)
   public static enum MessageType {
      REPORTABLE,
      CONTEXT;

      public boolean foldable() {
         return this == CONTEXT;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static record Results<T extends LoggedChatMessage>(List<ChatLog.Entry<T>> messages, ChatLogSegmenter.MessageType type) {
   }
}