package net.minecraft.network.protocol.login;

import com.mojang.datafixers.util.Either;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Optional;
import javax.crypto.SecretKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.world.entity.player.ProfilePublicKey;

public class ServerboundKeyPacket implements Packet<ServerLoginPacketListener> {
   private final byte[] keybytes;
   private final Either<byte[], Crypt.SaltSignaturePair> nonceOrSaltSignature;

   public ServerboundKeyPacket(SecretKey p_134856_, PublicKey p_134857_, byte[] p_134858_) throws CryptException {
      this.keybytes = Crypt.encryptUsingKey(p_134857_, p_134856_.getEncoded());
      this.nonceOrSaltSignature = Either.left(Crypt.encryptUsingKey(p_134857_, p_134858_));
   }

   public ServerboundKeyPacket(SecretKey p_238057_, PublicKey p_238058_, long p_238059_, byte[] p_238060_) throws CryptException {
      this.keybytes = Crypt.encryptUsingKey(p_238058_, p_238057_.getEncoded());
      this.nonceOrSaltSignature = Either.right(new Crypt.SaltSignaturePair(p_238059_, p_238060_));
   }

   public ServerboundKeyPacket(FriendlyByteBuf p_179829_) {
      this.keybytes = p_179829_.readByteArray();
      this.nonceOrSaltSignature = p_179829_.readEither(FriendlyByteBuf::readByteArray, Crypt.SaltSignaturePair::new);
   }

   public void write(FriendlyByteBuf p_134870_) {
      p_134870_.writeByteArray(this.keybytes);
      p_134870_.writeEither(this.nonceOrSaltSignature, FriendlyByteBuf::writeByteArray, Crypt.SaltSignaturePair::write);
   }

   public void handle(ServerLoginPacketListener p_134866_) {
      p_134866_.handleKey(this);
   }

   public SecretKey getSecretKey(PrivateKey p_134860_) throws CryptException {
      return Crypt.decryptByteToSecretKey(p_134860_, this.keybytes);
   }

   public boolean isChallengeSignatureValid(byte[] p_238072_, ProfilePublicKey p_238073_) {
      return this.nonceOrSaltSignature.map((p_238066_) -> {
         return false;
      }, (p_238064_) -> {
         return p_238073_.createSignatureValidator().validate((p_238070_) -> {
            p_238070_.update(p_238072_);
            p_238070_.update(p_238064_.saltAsBytes());
         }, p_238064_.signature());
      });
   }

   public boolean isNonceValid(byte[] p_238075_, PrivateKey p_238076_) {
      Optional<byte[]> optional = this.nonceOrSaltSignature.left();

      try {
         return optional.isPresent() && Arrays.equals(p_238075_, Crypt.decryptUsingKey(p_238076_, optional.get()));
      } catch (CryptException cryptexception) {
         return false;
      }
   }
}