package net.minecraft.client.multiplayer;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.logging.LogUtils;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.SignedMessageValidator;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class PlayerInfo {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final GameProfile profile;
   private final Map<MinecraftProfileTexture.Type, ResourceLocation> textureLocations = Maps.newEnumMap(MinecraftProfileTexture.Type.class);
   private GameType gameMode;
   private int latency;
   private boolean pendingTextures;
   @Nullable
   private String skinModel;
   @Nullable
   private Component tabListDisplayName;
   private int lastHealth;
   private int displayHealth;
   private long lastHealthTime;
   private long healthBlinkTime;
   private long renderVisibilityId;
   @Nullable
   private final ProfilePublicKey profilePublicKey;
   private final SignedMessageValidator messageValidator;

   public PlayerInfo(ClientboundPlayerInfoPacket.PlayerUpdate p_233762_, SignatureValidator p_233763_, boolean p_242970_) {
      this.profile = p_233762_.getProfile();
      this.gameMode = p_233762_.getGameMode();
      this.latency = p_233762_.getLatency();
      this.tabListDisplayName = p_233762_.getDisplayName();
      ProfilePublicKey profilepublickey = null;

      try {
         ProfilePublicKey.Data profilepublickey$data = p_233762_.getProfilePublicKey();
         if (profilepublickey$data != null) {
            profilepublickey = ProfilePublicKey.createValidated(p_233763_, this.profile.getId(), profilepublickey$data, ProfilePublicKey.EXPIRY_GRACE_PERIOD);
         }
      } catch (Exception exception) {
         LOGGER.error("Failed to validate publicKey property for profile {}", this.profile.getId(), exception);
      }

      this.profilePublicKey = profilepublickey;
      this.messageValidator = SignedMessageValidator.create(profilepublickey, p_242970_);
   }

   public GameProfile getProfile() {
      return this.profile;
   }

   @Nullable
   public ProfilePublicKey getProfilePublicKey() {
      return this.profilePublicKey;
   }

   public SignedMessageValidator getMessageValidator() {
      return this.messageValidator;
   }

   @Nullable
   public GameType getGameMode() {
      return this.gameMode;
   }

   protected void setGameMode(GameType p_105318_) {
      net.minecraftforge.client.ForgeHooksClient.onClientChangeGameType(this, this.gameMode, p_105318_);
      this.gameMode = p_105318_;
   }

   public int getLatency() {
      return this.latency;
   }

   protected void setLatency(int p_105314_) {
      this.latency = p_105314_;
   }

   public boolean isCapeLoaded() {
      return this.getCapeLocation() != null;
   }

   public boolean isSkinLoaded() {
      return this.getSkinLocation() != null;
   }

   public String getModelName() {
      return this.skinModel == null ? DefaultPlayerSkin.getSkinModelName(this.profile.getId()) : this.skinModel;
   }

   public ResourceLocation getSkinLocation() {
      this.registerTextures();
      return MoreObjects.firstNonNull(this.textureLocations.get(Type.SKIN), DefaultPlayerSkin.getDefaultSkin(this.profile.getId()));
   }

   @Nullable
   public ResourceLocation getCapeLocation() {
      this.registerTextures();
      return this.textureLocations.get(Type.CAPE);
   }

   @Nullable
   public ResourceLocation getElytraLocation() {
      this.registerTextures();
      return this.textureLocations.get(Type.ELYTRA);
   }

   @Nullable
   public PlayerTeam getTeam() {
      return Minecraft.getInstance().level.getScoreboard().getPlayersTeam(this.getProfile().getName());
   }

   protected void registerTextures() {
      synchronized(this) {
         if (!this.pendingTextures) {
            this.pendingTextures = true;
            Minecraft.getInstance().getSkinManager().registerSkins(this.profile, (p_105320_, p_105321_, p_105322_) -> {
               this.textureLocations.put(p_105320_, p_105321_);
               if (p_105320_ == Type.SKIN) {
                  this.skinModel = p_105322_.getMetadata("model");
                  if (this.skinModel == null) {
                     this.skinModel = "default";
                  }
               }

            }, true);
         }

      }
   }

   public void setTabListDisplayName(@Nullable Component p_105324_) {
      this.tabListDisplayName = p_105324_;
   }

   @Nullable
   public Component getTabListDisplayName() {
      return this.tabListDisplayName;
   }

   public int getLastHealth() {
      return this.lastHealth;
   }

   public void setLastHealth(int p_105327_) {
      this.lastHealth = p_105327_;
   }

   public int getDisplayHealth() {
      return this.displayHealth;
   }

   public void setDisplayHealth(int p_105332_) {
      this.displayHealth = p_105332_;
   }

   public long getLastHealthTime() {
      return this.lastHealthTime;
   }

   public void setLastHealthTime(long p_105316_) {
      this.lastHealthTime = p_105316_;
   }

   public long getHealthBlinkTime() {
      return this.healthBlinkTime;
   }

   public void setHealthBlinkTime(long p_105329_) {
      this.healthBlinkTime = p_105329_;
   }

   public long getRenderVisibilityId() {
      return this.renderVisibilityId;
   }

   public void setRenderVisibilityId(long p_105334_) {
      this.renderVisibilityId = p_105334_;
   }
}
