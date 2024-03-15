package net.minecraft.client.multiplayer;

import com.google.common.primitives.Longs;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InsufficientPrivilegesException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.exceptions.UserBannedException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.logging.LogUtils;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.Crypt;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.Signer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientHandshakePacketListenerImpl implements ClientLoginPacketListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Minecraft minecraft;
   @Nullable
   private final Screen parent;
   private final Consumer<Component> updateStatus;
   private final Connection connection;
   private GameProfile localGameProfile;

   public ClientHandshakePacketListenerImpl(Connection p_104526_, Minecraft p_104527_, @Nullable Screen p_104528_, Consumer<Component> p_104529_) {
      this.connection = p_104526_;
      this.minecraft = p_104527_;
      this.parent = p_104528_;
      this.updateStatus = p_104529_;
   }

   public void handleHello(ClientboundHelloPacket p_104549_) {
      Cipher cipher;
      Cipher cipher1;
      String s;
      ServerboundKeyPacket serverboundkeypacket;
      try {
         SecretKey secretkey = Crypt.generateSecretKey();
         PublicKey publickey = p_104549_.getPublicKey();
         s = (new BigInteger(Crypt.digestData(p_104549_.getServerId(), publickey, secretkey))).toString(16);
         cipher = Crypt.getCipher(2, secretkey);
         cipher1 = Crypt.getCipher(1, secretkey);
         byte[] abyte = p_104549_.getNonce();
         Signer signer = this.minecraft.getProfileKeyPairManager().signer();
         if (signer == null) {
            serverboundkeypacket = new ServerboundKeyPacket(secretkey, publickey, abyte);
         } else {
            long i = Crypt.SaltSupplier.getLong();
            byte[] abyte1 = signer.sign((p_233598_) -> {
               p_233598_.update(abyte);
               p_233598_.update(Longs.toByteArray(i));
            });
            serverboundkeypacket = new ServerboundKeyPacket(secretkey, publickey, i, abyte1);
         }
      } catch (Exception exception) {
         throw new IllegalStateException("Protocol error", exception);
      }

      this.updateStatus.accept(Component.translatable("connect.authorizing"));
      HttpUtil.DOWNLOAD_EXECUTOR.submit(() -> {
         Component component = this.authenticateServer(s);
         if (component != null) {
            if (this.minecraft.getCurrentServer() == null || !this.minecraft.getCurrentServer().isLan()) {
               this.connection.disconnect(component);
               return;
            }

            LOGGER.warn(component.getString());
         }

         this.updateStatus.accept(Component.translatable("connect.encrypting"));
         this.connection.send(serverboundkeypacket, PacketSendListener.thenRun(() -> {
            this.connection.setEncryptionKey(cipher, cipher1);
         }));
      });
   }

   @Nullable
   private Component authenticateServer(String p_104532_) {
      try {
         this.getMinecraftSessionService().joinServer(this.minecraft.getUser().getGameProfile(), this.minecraft.getUser().getAccessToken(), p_104532_);
         return null;
      } catch (AuthenticationUnavailableException authenticationunavailableexception) {
         return Component.translatable("disconnect.loginFailedInfo", Component.translatable("disconnect.loginFailedInfo.serversUnavailable"));
      } catch (InvalidCredentialsException invalidcredentialsexception) {
         return Component.translatable("disconnect.loginFailedInfo", Component.translatable("disconnect.loginFailedInfo.invalidSession"));
      } catch (InsufficientPrivilegesException insufficientprivilegesexception) {
         return Component.translatable("disconnect.loginFailedInfo", Component.translatable("disconnect.loginFailedInfo.insufficientPrivileges"));
      } catch (UserBannedException userbannedexception) {
         return Component.translatable("disconnect.loginFailedInfo", Component.translatable("disconnect.loginFailedInfo.userBanned"));
      } catch (AuthenticationException authenticationexception) {
         return Component.translatable("disconnect.loginFailedInfo", authenticationexception.getMessage());
      }
   }

   private MinecraftSessionService getMinecraftSessionService() {
      return this.minecraft.getMinecraftSessionService();
   }

   public void handleGameProfile(ClientboundGameProfilePacket p_104547_) {
      this.updateStatus.accept(Component.translatable("connect.joining"));
      this.localGameProfile = p_104547_.getGameProfile();
      this.connection.setProtocol(ConnectionProtocol.PLAY);
      net.minecraftforge.network.NetworkHooks.handleClientLoginSuccess(this.connection);
      this.connection.setListener(new ClientPacketListener(this.minecraft, this.parent, this.connection, this.localGameProfile, this.minecraft.createTelemetryManager()));
   }

   public void onDisconnect(Component p_104543_) {
      if (this.parent != null && this.parent instanceof RealmsScreen) {
         this.minecraft.setScreen(new DisconnectedRealmsScreen(this.parent, CommonComponents.CONNECT_FAILED, p_104543_));
      } else {
         this.minecraft.setScreen(net.minecraftforge.network.NetworkHooks.getModMismatchData(connection) != null ? new net.minecraftforge.client.gui.ModMismatchDisconnectedScreen(this.parent, CommonComponents.CONNECT_FAILED, p_104543_, net.minecraftforge.network.NetworkHooks.getModMismatchData(connection)) : new DisconnectedScreen(this.parent, CommonComponents.CONNECT_FAILED, p_104543_));
      }

   }

   public Connection getConnection() {
      return this.connection;
   }

   public void handleDisconnect(ClientboundLoginDisconnectPacket p_104553_) {
      this.connection.disconnect(p_104553_.getReason());
   }

   public void handleCompression(ClientboundLoginCompressionPacket p_104551_) {
      if (!this.connection.isMemoryConnection()) {
         this.connection.setupCompression(p_104551_.getCompressionThreshold(), false);
      }

   }

   public void handleCustomQuery(ClientboundCustomQueryPacket p_104545_) {
      if (net.minecraftforge.network.NetworkHooks.onCustomPayload(p_104545_, this.connection)) return;
      this.updateStatus.accept(Component.translatable("connect.negotiating"));
      this.connection.send(new ServerboundCustomQueryPacket(p_104545_.getTransactionId(), (FriendlyByteBuf)null));
   }
}
