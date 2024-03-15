package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingPlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;

public class MsgCommand {
   public static void register(CommandDispatcher<CommandSourceStack> p_138061_) {
      LiteralCommandNode<CommandSourceStack> literalcommandnode = p_138061_.register(Commands.literal("msg").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("message", MessageArgument.message()).executes((p_138063_) -> {
         MessageArgument.ChatMessage messageargument$chatmessage = MessageArgument.getChatMessage(p_138063_, "message");

         try {
            return sendMessage(p_138063_.getSource(), EntityArgument.getPlayers(p_138063_, "targets"), messageargument$chatmessage);
         } catch (Exception exception) {
            messageargument$chatmessage.consume(p_138063_.getSource());
            throw exception;
         }
      }))));
      p_138061_.register(Commands.literal("tell").redirect(literalcommandnode));
      p_138061_.register(Commands.literal("w").redirect(literalcommandnode));
   }

   private static int sendMessage(CommandSourceStack p_214523_, Collection<ServerPlayer> p_214524_, MessageArgument.ChatMessage p_214525_) {
      ChatType.Bound chattype$bound = ChatType.bind(ChatType.MSG_COMMAND_INCOMING, p_214523_);
      p_214525_.resolve(p_214523_, (p_243178_) -> {
         OutgoingPlayerChatMessage outgoingplayerchatmessage = OutgoingPlayerChatMessage.create(p_243178_);
         boolean flag = p_243178_.isFullyFiltered();
         Entity entity = p_214523_.getEntity();
         boolean flag1 = false;

         for(ServerPlayer serverplayer : p_214524_) {
            ChatType.Bound chattype$bound1 = ChatType.bind(ChatType.MSG_COMMAND_OUTGOING, p_214523_).withTargetName(serverplayer.getDisplayName());
            p_214523_.sendChatMessage(outgoingplayerchatmessage, false, chattype$bound1);
            boolean flag2 = p_214523_.shouldFilterMessageTo(serverplayer);
            serverplayer.sendChatMessage(outgoingplayerchatmessage, flag2, chattype$bound);
            flag1 |= flag && flag2 && serverplayer != entity;
         }

         if (flag1) {
            p_214523_.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
         }

         outgoingplayerchatmessage.sendHeadersToRemainingPlayers(p_214523_.getServer().getPlayerList());
      });
      return p_214524_.size();
   }
}