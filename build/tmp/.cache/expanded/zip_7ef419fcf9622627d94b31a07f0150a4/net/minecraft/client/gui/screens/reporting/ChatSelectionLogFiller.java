package net.minecraft.client.gui.screens.reporting;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class ChatSelectionLogFiller<T extends LoggedChatMessage> {
   private static final int CONTEXT_FOLDED_SIZE = 4;
   private final ChatLog log;
   private final Predicate<T> canReport;
   private int nextMessageId;
   final Class<T> tClass;

   public ChatSelectionLogFiller(ChatLog p_242897_, Predicate<T> p_242845_, Class<T> p_242904_) {
      this.log = p_242897_;
      this.canReport = p_242845_;
      this.nextMessageId = p_242897_.newest();
      this.tClass = p_242904_;
   }

   public void fillNextPage(int p_239016_, ChatSelectionLogFiller.Output<T> p_239017_) {
      int i = 0;

      while(i < p_239016_) {
         ChatLogSegmenter.Results<T> results = this.nextSegment();
         if (results == null) {
            break;
         }

         if (results.type().foldable()) {
            i += this.addFoldedMessagesTo(results.messages(), p_239017_);
         } else {
            p_239017_.acceptMessages(results.messages());
            i += results.messages().size();
         }
      }

   }

   private int addFoldedMessagesTo(List<ChatLog.Entry<T>> p_239253_, ChatSelectionLogFiller.Output<T> p_239254_) {
      int i = 8;
      if (p_239253_.size() > 8) {
         int j = p_239253_.size() - 8;
         p_239254_.acceptMessages(p_239253_.subList(0, 4));
         p_239254_.acceptDivider(Component.translatable("gui.chatSelection.fold", j));
         p_239254_.acceptMessages(p_239253_.subList(p_239253_.size() - 4, p_239253_.size()));
         return 9;
      } else {
         p_239254_.acceptMessages(p_239253_);
         return p_239253_.size();
      }
   }

   @Nullable
   private ChatLogSegmenter.@Nullable Results<T> nextSegment() {
      ChatLogSegmenter<T> chatlogsegmenter = new ChatLogSegmenter<>((p_242051_) -> {
         return this.getMessageType(p_242051_.event());
      });
      OptionalInt optionalint = this.log.selectBefore(this.nextMessageId).entries().map((p_242687_) -> {
         return p_242687_.tryCast(this.tClass);
      }).filter(Objects::nonNull).takeWhile(chatlogsegmenter::accept).mapToInt(ChatLog.Entry::id).reduce((p_240038_, p_240039_) -> {
         return p_240039_;
      });
      if (optionalint.isPresent()) {
         this.nextMessageId = this.log.before(optionalint.getAsInt());
      }

      return chatlogsegmenter.build();
   }

   private ChatLogSegmenter.MessageType getMessageType(T p_242252_) {
      return this.canReport.test(p_242252_) ? ChatLogSegmenter.MessageType.REPORTABLE : ChatLogSegmenter.MessageType.CONTEXT;
   }

   @OnlyIn(Dist.CLIENT)
   public interface Output<T extends LoggedChatMessage> {
      default void acceptMessages(Iterable<ChatLog.Entry<T>> p_240091_) {
         for(ChatLog.Entry<T> entry : p_240091_) {
            this.acceptMessage(entry.id(), entry.event());
         }

      }

      void acceptMessage(int p_239762_, T p_242411_);

      void acceptDivider(Component p_239557_);
   }
}