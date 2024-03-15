package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.ChatMessageContent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.players.PlayerList;
import org.slf4j.Logger;

public class MessageArgument implements SignedArgument<MessageArgument.Message> {
   private static final Collection<String> EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");
   private static final Logger LOGGER = LogUtils.getLogger();

   public static MessageArgument message() {
      return new MessageArgument();
   }

   public static Component getMessage(CommandContext<CommandSourceStack> p_96836_, String p_96837_) throws CommandSyntaxException {
      MessageArgument.Message messageargument$message = p_96836_.getArgument(p_96837_, MessageArgument.Message.class);
      return messageargument$message.resolveComponent(p_96836_.getSource());
   }

   public static MessageArgument.ChatMessage getChatMessage(CommandContext<CommandSourceStack> p_232164_, String p_232165_) throws CommandSyntaxException {
      MessageArgument.Message messageargument$message = p_232164_.getArgument(p_232165_, MessageArgument.Message.class);
      Component component = messageargument$message.resolveComponent(p_232164_.getSource());
      CommandSigningContext commandsigningcontext = p_232164_.getSource().getSigningContext();
      PlayerChatMessage playerchatmessage = commandsigningcontext.getArgument(p_232165_);
      if (playerchatmessage == null) {
         ChatMessageContent chatmessagecontent = new ChatMessageContent(messageargument$message.text, component);
         return new MessageArgument.ChatMessage(PlayerChatMessage.system(chatmessagecontent));
      } else {
         return new MessageArgument.ChatMessage(ChatDecorator.attachIfNotDecorated(playerchatmessage, component));
      }
   }

   public MessageArgument.Message parse(StringReader p_96834_) throws CommandSyntaxException {
      return MessageArgument.Message.parseText(p_96834_, true);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public String getSignableText(MessageArgument.Message p_242423_) {
      return p_242423_.getText();
   }

   public CompletableFuture<Component> resolvePreview(CommandSourceStack p_232147_, MessageArgument.Message p_232148_) throws CommandSyntaxException {
      return p_232148_.resolveDecoratedComponent(p_232147_);
   }

   public Class<MessageArgument.Message> getValueType() {
      return MessageArgument.Message.class;
   }

   static void logResolutionFailure(CommandSourceStack p_232156_, CompletableFuture<?> p_232157_) {
      p_232157_.exceptionally((p_232154_) -> {
         LOGGER.error("Encountered unexpected exception while resolving chat message argument from '{}'", p_232156_.getDisplayName().getString(), p_232154_);
         return null;
      });
   }

   public static record ChatMessage(PlayerChatMessage signedArgument) {
      public void resolve(CommandSourceStack p_242313_, Consumer<PlayerChatMessage> p_242409_) {
         MinecraftServer minecraftserver = p_242313_.getServer();
         p_242313_.getChatMessageChainer().append(() -> {
            CompletableFuture<FilteredText> completablefuture = this.filterPlainText(p_242313_, this.signedArgument.signedContent().plain());
            CompletableFuture<PlayerChatMessage> completablefuture1 = minecraftserver.getChatDecorator().decorate(p_242313_.getPlayer(), this.signedArgument);
            return CompletableFuture.allOf(completablefuture, completablefuture1).thenAcceptAsync((p_243162_) -> {
               PlayerChatMessage playerchatmessage = completablefuture1.join().filter(completablefuture.join().mask());
               p_242409_.accept(playerchatmessage);
            }, minecraftserver);
         });
      }

      private CompletableFuture<FilteredText> filterPlainText(CommandSourceStack p_241399_, String p_241465_) {
         ServerPlayer serverplayer = p_241399_.getPlayer();
         return serverplayer != null && this.signedArgument.hasSignatureFrom(serverplayer.getUUID()) ? serverplayer.getTextFilter().processStreamMessage(p_241465_) : CompletableFuture.completedFuture(FilteredText.passThrough(p_241465_));
      }

      public void consume(CommandSourceStack p_241491_) {
         if (!this.signedArgument.signer().isSystem()) {
            this.resolve(p_241491_, (p_243158_) -> {
               PlayerList playerlist = p_241491_.getServer().getPlayerList();
               playerlist.broadcastMessageHeader(p_243158_, Set.of());
            });
         }

      }
   }

   public static class Message {
      final String text;
      private final MessageArgument.Part[] parts;

      public Message(String p_96844_, MessageArgument.Part[] p_96845_) {
         this.text = p_96844_;
         this.parts = p_96845_;
      }

      public String getText() {
         return this.text;
      }

      public MessageArgument.Part[] getParts() {
         return this.parts;
      }

      CompletableFuture<Component> resolveDecoratedComponent(CommandSourceStack p_232195_) throws CommandSyntaxException {
         Component component = this.resolveComponent(p_232195_);
         CompletableFuture<Component> completablefuture = p_232195_.getServer().getChatDecorator().decorate(p_232195_.getPlayer(), component);
         MessageArgument.logResolutionFailure(p_232195_, completablefuture);
         return completablefuture;
      }

      Component resolveComponent(CommandSourceStack p_232197_) throws CommandSyntaxException {
         return this.toComponent(p_232197_, net.minecraftforge.common.ForgeHooks.canUseEntitySelectors(p_232197_));
      }

      public Component toComponent(CommandSourceStack p_96850_, boolean p_96851_) throws CommandSyntaxException {
         if (this.parts.length != 0 && p_96851_) {
            MutableComponent mutablecomponent = Component.literal(this.text.substring(0, this.parts[0].getStart()));
            int i = this.parts[0].getStart();

            for(MessageArgument.Part messageargument$part : this.parts) {
               Component component = messageargument$part.toComponent(p_96850_);
               if (i < messageargument$part.getStart()) {
                  mutablecomponent.append(this.text.substring(i, messageargument$part.getStart()));
               }

               if (component != null) {
                  mutablecomponent.append(component);
               }

               i = messageargument$part.getEnd();
            }

            if (i < this.text.length()) {
               mutablecomponent.append(this.text.substring(i));
            }

            return mutablecomponent;
         } else {
            return Component.literal(this.text);
         }
      }

      public static MessageArgument.Message parseText(StringReader p_96847_, boolean p_96848_) throws CommandSyntaxException {
         String s = p_96847_.getString().substring(p_96847_.getCursor(), p_96847_.getTotalLength());
         if (!p_96848_) {
            p_96847_.setCursor(p_96847_.getTotalLength());
            return new MessageArgument.Message(s, new MessageArgument.Part[0]);
         } else {
            List<MessageArgument.Part> list = Lists.newArrayList();
            int i = p_96847_.getCursor();

            while(true) {
               int j;
               EntitySelector entityselector;
               while(true) {
                  if (!p_96847_.canRead()) {
                     return new MessageArgument.Message(s, list.toArray(new MessageArgument.Part[0]));
                  }

                  if (p_96847_.peek() == '@') {
                     j = p_96847_.getCursor();

                     try {
                        EntitySelectorParser entityselectorparser = new EntitySelectorParser(p_96847_);
                        entityselector = entityselectorparser.parse();
                        break;
                     } catch (CommandSyntaxException commandsyntaxexception) {
                        if (commandsyntaxexception.getType() != EntitySelectorParser.ERROR_MISSING_SELECTOR_TYPE && commandsyntaxexception.getType() != EntitySelectorParser.ERROR_UNKNOWN_SELECTOR_TYPE) {
                           throw commandsyntaxexception;
                        }

                        p_96847_.setCursor(j + 1);
                     }
                  } else {
                     p_96847_.skip();
                  }
               }

               list.add(new MessageArgument.Part(j - i, p_96847_.getCursor() - i, entityselector));
            }
         }
      }
   }

   public static class Part {
      private final int start;
      private final int end;
      private final EntitySelector selector;

      public Part(int p_96856_, int p_96857_, EntitySelector p_96858_) {
         this.start = p_96856_;
         this.end = p_96857_;
         this.selector = p_96858_;
      }

      public int getStart() {
         return this.start;
      }

      public int getEnd() {
         return this.end;
      }

      public EntitySelector getSelector() {
         return this.selector;
      }

      @Nullable
      public Component toComponent(CommandSourceStack p_96861_) throws CommandSyntaxException {
         return EntitySelector.joinNames(this.selector.findEntities(p_96861_));
      }
   }
}
