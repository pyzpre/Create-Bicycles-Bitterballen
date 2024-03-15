package net.minecraft.client.multiplayer;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ServerData {
   private static final Logger LOGGER = LogUtils.getLogger();
   public String name;
   public String ip;
   public Component status;
   public Component motd;
   public long ping;
   public int protocol = SharedConstants.getCurrentVersion().getProtocolVersion();
   public Component version = Component.literal(SharedConstants.getCurrentVersion().getName());
   public boolean pinged;
   public List<Component> playerList = Collections.emptyList();
   private ServerData.ServerPackStatus packStatus = ServerData.ServerPackStatus.PROMPT;
   @Nullable
   private String iconB64;
   private boolean lan;
   @Nullable
   private ServerData.ChatPreview chatPreview;
   private boolean chatPreviewEnabled = true;
   private boolean enforcesSecureChat;
   public net.minecraftforge.client.ExtendedServerListData forgeData = null;

   public ServerData(String p_105375_, String p_105376_, boolean p_105377_) {
      this.name = p_105375_;
      this.ip = p_105376_;
      this.lan = p_105377_;
   }

   public CompoundTag write() {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putString("name", this.name);
      compoundtag.putString("ip", this.ip);
      if (this.iconB64 != null) {
         compoundtag.putString("icon", this.iconB64);
      }

      if (this.packStatus == ServerData.ServerPackStatus.ENABLED) {
         compoundtag.putBoolean("acceptTextures", true);
      } else if (this.packStatus == ServerData.ServerPackStatus.DISABLED) {
         compoundtag.putBoolean("acceptTextures", false);
      }

      if (this.chatPreview != null) {
         ServerData.ChatPreview.CODEC.encodeStart(NbtOps.INSTANCE, this.chatPreview).result().ifPresent((p_233812_) -> {
            compoundtag.put("chatPreview", p_233812_);
         });
      }

      return compoundtag;
   }

   public ServerData.ServerPackStatus getResourcePackStatus() {
      return this.packStatus;
   }

   public void setResourcePackStatus(ServerData.ServerPackStatus p_105380_) {
      this.packStatus = p_105380_;
   }

   public static ServerData read(CompoundTag p_105386_) {
      ServerData serverdata = new ServerData(p_105386_.getString("name"), p_105386_.getString("ip"), false);
      if (p_105386_.contains("icon", 8)) {
         serverdata.setIconB64(p_105386_.getString("icon"));
      }

      if (p_105386_.contains("acceptTextures", 1)) {
         if (p_105386_.getBoolean("acceptTextures")) {
            serverdata.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
         } else {
            serverdata.setResourcePackStatus(ServerData.ServerPackStatus.DISABLED);
         }
      } else {
         serverdata.setResourcePackStatus(ServerData.ServerPackStatus.PROMPT);
      }

      if (p_105386_.contains("chatPreview", 10)) {
         ServerData.ChatPreview.CODEC.parse(NbtOps.INSTANCE, p_105386_.getCompound("chatPreview")).resultOrPartial(LOGGER::error).ifPresent((p_233807_) -> {
            serverdata.chatPreview = p_233807_;
         });
      }

      return serverdata;
   }

   @Nullable
   public String getIconB64() {
      return this.iconB64;
   }

   public static String parseFavicon(String p_233809_) throws ParseException {
      if (p_233809_.startsWith("data:image/png;base64,")) {
         return p_233809_.substring("data:image/png;base64,".length());
      } else {
         throw new ParseException("Unknown format", 0);
      }
   }

   public void setIconB64(@Nullable String p_105384_) {
      this.iconB64 = p_105384_;
   }

   public boolean isLan() {
      return this.lan;
   }

   public void setPreviewsChat(boolean p_233814_) {
      if (p_233814_ && this.chatPreview == null) {
         this.chatPreview = new ServerData.ChatPreview(false, false);
      } else if (!p_233814_ && this.chatPreview != null) {
         this.chatPreview = null;
      }

   }

   @Nullable
   public ServerData.ChatPreview getChatPreview() {
      return this.chatPreview;
   }

   public void setChatPreviewEnabled(boolean p_233816_) {
      this.chatPreviewEnabled = p_233816_;
   }

   public boolean previewsChat() {
      return this.chatPreviewEnabled && this.chatPreview != null;
   }

   public void setEnforcesSecureChat(boolean p_242972_) {
      this.enforcesSecureChat = p_242972_;
   }

   public boolean enforcesSecureChat() {
      return this.enforcesSecureChat;
   }

   public void copyNameIconFrom(ServerData p_233804_) {
      this.ip = p_233804_.ip;
      this.name = p_233804_.name;
      this.iconB64 = p_233804_.iconB64;
   }

   public void copyFrom(ServerData p_105382_) {
      this.copyNameIconFrom(p_105382_);
      this.setResourcePackStatus(p_105382_.getResourcePackStatus());
      this.lan = p_105382_.lan;
      this.chatPreview = Util.mapNullable(p_105382_.chatPreview, ServerData.ChatPreview::copy);
      this.enforcesSecureChat = p_105382_.enforcesSecureChat;
   }

   @OnlyIn(Dist.CLIENT)
   public static class ChatPreview {
      public static final Codec<ServerData.ChatPreview> CODEC = RecordCodecBuilder.create((p_233828_) -> {
         return p_233828_.group(Codec.BOOL.optionalFieldOf("acknowledged", Boolean.valueOf(false)).forGetter((p_233833_) -> {
            return p_233833_.acknowledged;
         }), Codec.BOOL.optionalFieldOf("toastShown", Boolean.valueOf(false)).forGetter((p_233830_) -> {
            return p_233830_.toastShown;
         })).apply(p_233828_, ServerData.ChatPreview::new);
      });
      private boolean acknowledged;
      private boolean toastShown;

      ChatPreview(boolean p_233824_, boolean p_233825_) {
         this.acknowledged = p_233824_;
         this.toastShown = p_233825_;
      }

      public void acknowledge() {
         this.acknowledged = true;
      }

      public boolean showToast() {
         if (!this.toastShown) {
            this.toastShown = true;
            return true;
         } else {
            return false;
         }
      }

      public boolean isAcknowledged() {
         return this.acknowledged;
      }

      private ServerData.ChatPreview copy() {
         return new ServerData.ChatPreview(this.acknowledged, this.toastShown);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static enum ServerPackStatus {
      ENABLED("enabled"),
      DISABLED("disabled"),
      PROMPT("prompt");

      private final Component name;

      private ServerPackStatus(String p_105399_) {
         this.name = Component.translatable("addServer.resourcePack." + p_105399_);
      }

      public Component getName() {
         return this.name;
      }
   }
}
