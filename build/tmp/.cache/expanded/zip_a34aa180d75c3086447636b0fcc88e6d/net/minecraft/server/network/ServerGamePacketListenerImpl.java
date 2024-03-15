package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.ChatMessageContent;
import net.minecraft.network.chat.ChatPreviewCache;
import net.minecraft.network.chat.ChatPreviewThrottler;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.LastSeenMessagesValidator;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSigner;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.PreviewableCommand;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundChatPreviewPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatPreviewPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FutureChain;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class ServerGamePacketListenerImpl implements ServerPlayerConnection, TickablePacketListener, ServerGamePacketListener {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int LATENCY_CHECK_INTERVAL = 15000;
   /**
    * Forge: Deprecated in favor of range/reach attributes.
    * @see net.minecraftforge.common.ForgeMod#REACH_DISTANCE
    * @see net.minecraftforge.common.ForgeMod#ATTACK_RANGE
    */
   @Deprecated
   public static final double MAX_INTERACTION_DISTANCE = Mth.square(6.0D);
   private static final int NO_BLOCK_UPDATES_TO_ACK = -1;
   private static final int PENDING_MESSAGE_DISCONNECT_THRESHOLD = 4096;
   public final Connection connection;
   private final MinecraftServer server;
   public ServerPlayer player;
   private int tickCount;
   private int ackBlockChangesUpTo = -1;
   private long keepAliveTime;
   private boolean keepAlivePending;
   private long keepAliveChallenge;
   private int chatSpamTickCount;
   private int dropSpamTickCount;
   private double firstGoodX;
   private double firstGoodY;
   private double firstGoodZ;
   private double lastGoodX;
   private double lastGoodY;
   private double lastGoodZ;
   @Nullable
   private Entity lastVehicle;
   private double vehicleFirstGoodX;
   private double vehicleFirstGoodY;
   private double vehicleFirstGoodZ;
   private double vehicleLastGoodX;
   private double vehicleLastGoodY;
   private double vehicleLastGoodZ;
   @Nullable
   private Vec3 awaitingPositionFromClient;
   private int awaitingTeleport;
   private int awaitingTeleportTime;
   private boolean clientIsFloating;
   private int aboveGroundTickCount;
   private boolean clientVehicleIsFloating;
   private int aboveGroundVehicleTickCount;
   private int receivedMovePacketCount;
   private int knownMovePacketCount;
   private final ChatPreviewCache chatPreviewCache = new ChatPreviewCache();
   private final ChatPreviewThrottler chatPreviewThrottler = new ChatPreviewThrottler();
   private final AtomicReference<Instant> lastChatTimeStamp = new AtomicReference<>(Instant.EPOCH);
   private final SignedMessageChain.Decoder signedMessageDecoder;
   private final LastSeenMessagesValidator lastSeenMessagesValidator = new LastSeenMessagesValidator();
   private final FutureChain chatMessageChain;

   public ServerGamePacketListenerImpl(MinecraftServer p_9770_, Connection p_9771_, ServerPlayer p_9772_) {
      this.server = p_9770_;
      this.connection = p_9771_;
      p_9771_.setListener(this);
      this.player = p_9772_;
      p_9772_.connection = this;
      this.keepAliveTime = Util.getMillis();
      p_9772_.getTextFilter().join();
      ProfilePublicKey profilepublickey = p_9772_.getProfilePublicKey();
      if (profilepublickey != null) {
         this.signedMessageDecoder = (new SignedMessageChain()).decoder();
      } else {
         this.signedMessageDecoder = SignedMessageChain.Decoder.UNSIGNED;
      }

      this.chatMessageChain = new FutureChain(p_9770_);
   }

   public void tick() {
      if (this.ackBlockChangesUpTo > -1) {
         this.send(new ClientboundBlockChangedAckPacket(this.ackBlockChangesUpTo));
         this.ackBlockChangesUpTo = -1;
      }

      this.resetPosition();
      this.player.xo = this.player.getX();
      this.player.yo = this.player.getY();
      this.player.zo = this.player.getZ();
      this.player.doTick();
      this.player.absMoveTo(this.firstGoodX, this.firstGoodY, this.firstGoodZ, this.player.getYRot(), this.player.getXRot());
      ++this.tickCount;
      this.knownMovePacketCount = this.receivedMovePacketCount;
      if (this.clientIsFloating && !this.player.isSleeping() && !this.player.isPassenger()) {
         if (++this.aboveGroundTickCount > 80) {
            LOGGER.warn("{} was kicked for floating too long!", (Object)this.player.getName().getString());
            this.disconnect(Component.translatable("multiplayer.disconnect.flying"));
            return;
         }
      } else {
         this.clientIsFloating = false;
         this.aboveGroundTickCount = 0;
      }

      this.lastVehicle = this.player.getRootVehicle();
      if (this.lastVehicle != this.player && this.lastVehicle.getControllingPassenger() == this.player) {
         this.vehicleFirstGoodX = this.lastVehicle.getX();
         this.vehicleFirstGoodY = this.lastVehicle.getY();
         this.vehicleFirstGoodZ = this.lastVehicle.getZ();
         this.vehicleLastGoodX = this.lastVehicle.getX();
         this.vehicleLastGoodY = this.lastVehicle.getY();
         this.vehicleLastGoodZ = this.lastVehicle.getZ();
         if (this.clientVehicleIsFloating && this.player.getRootVehicle().getControllingPassenger() == this.player) {
            if (++this.aboveGroundVehicleTickCount > 80) {
               LOGGER.warn("{} was kicked for floating a vehicle too long!", (Object)this.player.getName().getString());
               this.disconnect(Component.translatable("multiplayer.disconnect.flying"));
               return;
            }
         } else {
            this.clientVehicleIsFloating = false;
            this.aboveGroundVehicleTickCount = 0;
         }
      } else {
         this.lastVehicle = null;
         this.clientVehicleIsFloating = false;
         this.aboveGroundVehicleTickCount = 0;
      }

      this.server.getProfiler().push("keepAlive");
      long i = Util.getMillis();
      if (i - this.keepAliveTime >= 15000L) {
         if (this.keepAlivePending) {
            this.disconnect(Component.translatable("disconnect.timeout"));
         } else {
            this.keepAlivePending = true;
            this.keepAliveTime = i;
            this.keepAliveChallenge = i;
            this.send(new ClientboundKeepAlivePacket(this.keepAliveChallenge));
         }
      }

      this.server.getProfiler().pop();
      if (this.chatSpamTickCount > 0) {
         --this.chatSpamTickCount;
      }

      if (this.dropSpamTickCount > 0) {
         --this.dropSpamTickCount;
      }

      if (this.player.getLastActionTime() > 0L && this.server.getPlayerIdleTimeout() > 0 && Util.getMillis() - this.player.getLastActionTime() > (long)(this.server.getPlayerIdleTimeout() * 1000 * 60)) {
         this.disconnect(Component.translatable("multiplayer.disconnect.idling"));
      }

      this.chatPreviewThrottler.tick();
   }

   public void resetPosition() {
      this.firstGoodX = this.player.getX();
      this.firstGoodY = this.player.getY();
      this.firstGoodZ = this.player.getZ();
      this.lastGoodX = this.player.getX();
      this.lastGoodY = this.player.getY();
      this.lastGoodZ = this.player.getZ();
   }

   public Connection getConnection() {
      return this.connection;
   }

   private boolean isSingleplayerOwner() {
      return this.server.isSingleplayerOwner(this.player.getGameProfile());
   }

   public void disconnect(Component p_9943_) {
      this.connection.send(new ClientboundDisconnectPacket(p_9943_), PacketSendListener.thenRun(() -> {
         this.connection.disconnect(p_9943_);
      }));
      this.connection.setReadOnly();
      this.server.executeBlocking(this.connection::handleDisconnection);
   }

   private <T, R> CompletableFuture<R> filterTextPacket(T p_243240_, BiFunction<TextFilter, T, CompletableFuture<R>> p_243271_) {
      return p_243271_.apply(this.player.getTextFilter(), p_243240_).thenApply((p_243192_) -> {
         if (!this.getConnection().isConnected()) {
            LOGGER.debug("Ignoring packet due to disconnection");
            throw new CancellationException("disconnected");
         } else {
            return p_243192_;
         }
      });
   }

   private CompletableFuture<FilteredText> filterTextPacket(String p_243213_) {
      return this.filterTextPacket(p_243213_, TextFilter::processStreamMessage);
   }

   private CompletableFuture<List<FilteredText>> filterTextPacket(List<String> p_243258_) {
      return this.filterTextPacket(p_243258_, TextFilter::processMessageBundle);
   }

   public void handlePlayerInput(ServerboundPlayerInputPacket p_9893_) {
      PacketUtils.ensureRunningOnSameThread(p_9893_, this, this.player.getLevel());
      this.player.setPlayerInput(p_9893_.getXxa(), p_9893_.getZza(), p_9893_.isJumping(), p_9893_.isShiftKeyDown());
   }

   private static boolean containsInvalidValues(double p_143664_, double p_143665_, double p_143666_, float p_143667_, float p_143668_) {
      return Double.isNaN(p_143664_) || Double.isNaN(p_143665_) || Double.isNaN(p_143666_) || !Floats.isFinite(p_143668_) || !Floats.isFinite(p_143667_);
   }

   private static double clampHorizontal(double p_143610_) {
      return Mth.clamp(p_143610_, -3.0E7D, 3.0E7D);
   }

   private static double clampVertical(double p_143654_) {
      return Mth.clamp(p_143654_, -2.0E7D, 2.0E7D);
   }

   public void handleMoveVehicle(ServerboundMoveVehiclePacket p_9876_) {
      PacketUtils.ensureRunningOnSameThread(p_9876_, this, this.player.getLevel());
      if (containsInvalidValues(p_9876_.getX(), p_9876_.getY(), p_9876_.getZ(), p_9876_.getYRot(), p_9876_.getXRot())) {
         this.disconnect(Component.translatable("multiplayer.disconnect.invalid_vehicle_movement"));
      } else {
         Entity entity = this.player.getRootVehicle();
         if (entity != this.player && entity.getControllingPassenger() == this.player && entity == this.lastVehicle) {
            ServerLevel serverlevel = this.player.getLevel();
            double d0 = entity.getX();
            double d1 = entity.getY();
            double d2 = entity.getZ();
            double d3 = clampHorizontal(p_9876_.getX());
            double d4 = clampVertical(p_9876_.getY());
            double d5 = clampHorizontal(p_9876_.getZ());
            float f = Mth.wrapDegrees(p_9876_.getYRot());
            float f1 = Mth.wrapDegrees(p_9876_.getXRot());
            double d6 = d3 - this.vehicleFirstGoodX;
            double d7 = d4 - this.vehicleFirstGoodY;
            double d8 = d5 - this.vehicleFirstGoodZ;
            double d9 = entity.getDeltaMovement().lengthSqr();
            double d10 = d6 * d6 + d7 * d7 + d8 * d8;
            if (d10 - d9 > 100.0D && !this.isSingleplayerOwner()) {
               LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", entity.getName().getString(), this.player.getName().getString(), d6, d7, d8);
               this.connection.send(new ClientboundMoveVehiclePacket(entity));
               return;
            }

            boolean flag = serverlevel.noCollision(entity, entity.getBoundingBox().deflate(0.0625D));
            d6 = d3 - this.vehicleLastGoodX;
            d7 = d4 - this.vehicleLastGoodY - 1.0E-6D;
            d8 = d5 - this.vehicleLastGoodZ;
            boolean flag1 = entity.verticalCollisionBelow;
            entity.move(MoverType.PLAYER, new Vec3(d6, d7, d8));
            d6 = d3 - entity.getX();
            d7 = d4 - entity.getY();
            if (d7 > -0.5D || d7 < 0.5D) {
               d7 = 0.0D;
            }

            d8 = d5 - entity.getZ();
            d10 = d6 * d6 + d7 * d7 + d8 * d8;
            boolean flag2 = false;
            if (d10 > 0.0625D) {
               flag2 = true;
               LOGGER.warn("{} (vehicle of {}) moved wrongly! {}", entity.getName().getString(), this.player.getName().getString(), Math.sqrt(d10));
            }

            entity.absMoveTo(d3, d4, d5, f, f1);
            this.player.absMoveTo(d3, d4, d5, this.player.getYRot(), this.player.getXRot()); // Forge - Resync player position on vehicle moving
            boolean flag3 = serverlevel.noCollision(entity, entity.getBoundingBox().deflate(0.0625D));
            if (flag && (flag2 || !flag3)) {
               entity.absMoveTo(d0, d1, d2, f, f1);
               this.player.absMoveTo(d3, d4, d5, this.player.getYRot(), this.player.getXRot()); // Forge - Resync player position on vehicle moving
               this.connection.send(new ClientboundMoveVehiclePacket(entity));
               return;
            }

            this.player.getLevel().getChunkSource().move(this.player);
            this.player.checkMovementStatistics(this.player.getX() - d0, this.player.getY() - d1, this.player.getZ() - d2);
            this.clientVehicleIsFloating = d7 >= -0.03125D && !flag1 && !this.server.isFlightAllowed() && !entity.isNoGravity() && this.noBlocksAround(entity);
            this.vehicleLastGoodX = entity.getX();
            this.vehicleLastGoodY = entity.getY();
            this.vehicleLastGoodZ = entity.getZ();
         }

      }
   }

   private boolean noBlocksAround(Entity p_9794_) {
      return BlockPos.betweenClosedStream(p_9794_.getBoundingBox().inflate(0.0625D).expandTowards(0.0D, -0.55D, 0.0D)).allMatch(b -> p_9794_.level.getBlockState(b).isAir());
   }

   public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket p_9835_) {
      PacketUtils.ensureRunningOnSameThread(p_9835_, this, this.player.getLevel());
      if (p_9835_.getId() == this.awaitingTeleport) {
         if (this.awaitingPositionFromClient == null) {
            this.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
            return;
         }

         this.player.absMoveTo(this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.getYRot(), this.player.getXRot());
         this.lastGoodX = this.awaitingPositionFromClient.x;
         this.lastGoodY = this.awaitingPositionFromClient.y;
         this.lastGoodZ = this.awaitingPositionFromClient.z;
         if (this.player.isChangingDimension()) {
            this.player.hasChangedDimension();
         }

         this.awaitingPositionFromClient = null;
      }

   }

   public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket p_9897_) {
      PacketUtils.ensureRunningOnSameThread(p_9897_, this, this.player.getLevel());
      this.server.getRecipeManager().byKey(p_9897_.getRecipe()).ifPresent(this.player.getRecipeBook()::removeHighlight);
   }

   public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket p_9895_) {
      PacketUtils.ensureRunningOnSameThread(p_9895_, this, this.player.getLevel());
      this.player.getRecipeBook().setBookSetting(p_9895_.getBookType(), p_9895_.isOpen(), p_9895_.isFiltering());
   }

   public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket p_9903_) {
      PacketUtils.ensureRunningOnSameThread(p_9903_, this, this.player.getLevel());
      if (p_9903_.getAction() == ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
         ResourceLocation resourcelocation = p_9903_.getTab();
         Advancement advancement = this.server.getAdvancements().getAdvancement(resourcelocation);
         if (advancement != null) {
            this.player.getAdvancements().setSelectedTab(advancement);
         }
      }

   }

   public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket p_9847_) {
      PacketUtils.ensureRunningOnSameThread(p_9847_, this, this.player.getLevel());
      StringReader stringreader = new StringReader(p_9847_.getCommand());
      if (stringreader.canRead() && stringreader.peek() == '/') {
         stringreader.skip();
      }

      ParseResults<CommandSourceStack> parseresults = this.server.getCommands().getDispatcher().parse(stringreader, this.player.createCommandSourceStack());
      this.server.getCommands().getDispatcher().getCompletionSuggestions(parseresults).thenAccept((p_238197_) -> {
         this.connection.send(new ClientboundCommandSuggestionsPacket(p_9847_.getId(), p_238197_));
      });
   }

   public void handleSetCommandBlock(ServerboundSetCommandBlockPacket p_9911_) {
      PacketUtils.ensureRunningOnSameThread(p_9911_, this, this.player.getLevel());
      if (!this.server.isCommandBlockEnabled()) {
         this.player.sendSystemMessage(Component.translatable("advMode.notEnabled"));
      } else if (!this.player.canUseGameMasterBlocks()) {
         this.player.sendSystemMessage(Component.translatable("advMode.notAllowed"));
      } else {
         BaseCommandBlock basecommandblock = null;
         CommandBlockEntity commandblockentity = null;
         BlockPos blockpos = p_9911_.getPos();
         BlockEntity blockentity = this.player.level.getBlockEntity(blockpos);
         if (blockentity instanceof CommandBlockEntity) {
            commandblockentity = (CommandBlockEntity)blockentity;
            basecommandblock = commandblockentity.getCommandBlock();
         }

         String s = p_9911_.getCommand();
         boolean flag = p_9911_.isTrackOutput();
         if (basecommandblock != null) {
            CommandBlockEntity.Mode commandblockentity$mode = commandblockentity.getMode();
            BlockState blockstate = this.player.level.getBlockState(blockpos);
            Direction direction = blockstate.getValue(CommandBlock.FACING);
            BlockState blockstate1;
            switch (p_9911_.getMode()) {
               case SEQUENCE:
                  blockstate1 = Blocks.CHAIN_COMMAND_BLOCK.defaultBlockState();
                  break;
               case AUTO:
                  blockstate1 = Blocks.REPEATING_COMMAND_BLOCK.defaultBlockState();
                  break;
               case REDSTONE:
               default:
                  blockstate1 = Blocks.COMMAND_BLOCK.defaultBlockState();
            }

            BlockState blockstate2 = blockstate1.setValue(CommandBlock.FACING, direction).setValue(CommandBlock.CONDITIONAL, Boolean.valueOf(p_9911_.isConditional()));
            if (blockstate2 != blockstate) {
               this.player.level.setBlock(blockpos, blockstate2, 2);
               blockentity.setBlockState(blockstate2);
               this.player.level.getChunkAt(blockpos).setBlockEntity(blockentity);
            }

            basecommandblock.setCommand(s);
            basecommandblock.setTrackOutput(flag);
            if (!flag) {
               basecommandblock.setLastOutput((Component)null);
            }

            commandblockentity.setAutomatic(p_9911_.isAutomatic());
            if (commandblockentity$mode != p_9911_.getMode()) {
               commandblockentity.onModeSwitch();
            }

            basecommandblock.onUpdated();
            if (!StringUtil.isNullOrEmpty(s)) {
               this.player.sendSystemMessage(Component.translatable("advMode.setCommand.success", s));
            }
         }

      }
   }

   public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket p_9913_) {
      PacketUtils.ensureRunningOnSameThread(p_9913_, this, this.player.getLevel());
      if (!this.server.isCommandBlockEnabled()) {
         this.player.sendSystemMessage(Component.translatable("advMode.notEnabled"));
      } else if (!this.player.canUseGameMasterBlocks()) {
         this.player.sendSystemMessage(Component.translatable("advMode.notAllowed"));
      } else {
         BaseCommandBlock basecommandblock = p_9913_.getCommandBlock(this.player.level);
         if (basecommandblock != null) {
            basecommandblock.setCommand(p_9913_.getCommand());
            basecommandblock.setTrackOutput(p_9913_.isTrackOutput());
            if (!p_9913_.isTrackOutput()) {
               basecommandblock.setLastOutput((Component)null);
            }

            basecommandblock.onUpdated();
            this.player.sendSystemMessage(Component.translatable("advMode.setCommand.success", p_9913_.getCommand()));
         }

      }
   }

   public void handlePickItem(ServerboundPickItemPacket p_9880_) {
      PacketUtils.ensureRunningOnSameThread(p_9880_, this, this.player.getLevel());
      this.player.getInventory().pickSlot(p_9880_.getSlot());
      this.player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, this.player.getInventory().selected, this.player.getInventory().getItem(this.player.getInventory().selected)));
      this.player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, p_9880_.getSlot(), this.player.getInventory().getItem(p_9880_.getSlot())));
      this.player.connection.send(new ClientboundSetCarriedItemPacket(this.player.getInventory().selected));
   }

   public void handleRenameItem(ServerboundRenameItemPacket p_9899_) {
      PacketUtils.ensureRunningOnSameThread(p_9899_, this, this.player.getLevel());
      AbstractContainerMenu $$2 = this.player.containerMenu;
      if ($$2 instanceof AnvilMenu anvilmenu) {
         if (!anvilmenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, anvilmenu);
            return;
         }

         String s = SharedConstants.filterText(p_9899_.getName());
         if (s.length() <= 50) {
            anvilmenu.setItemName(s);
         }
      }

   }

   public void handleSetBeaconPacket(ServerboundSetBeaconPacket p_9907_) {
      PacketUtils.ensureRunningOnSameThread(p_9907_, this, this.player.getLevel());
      AbstractContainerMenu abstractcontainermenu = this.player.containerMenu;
      if (abstractcontainermenu instanceof BeaconMenu beaconmenu) {
         if (!this.player.containerMenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
            return;
         }

         beaconmenu.updateEffects(p_9907_.getPrimary(), p_9907_.getSecondary());
      }

   }

   public void handleSetStructureBlock(ServerboundSetStructureBlockPacket p_9919_) {
      PacketUtils.ensureRunningOnSameThread(p_9919_, this, this.player.getLevel());
      if (this.player.canUseGameMasterBlocks()) {
         BlockPos blockpos = p_9919_.getPos();
         BlockState blockstate = this.player.level.getBlockState(blockpos);
         BlockEntity blockentity = this.player.level.getBlockEntity(blockpos);
         if (blockentity instanceof StructureBlockEntity) {
            StructureBlockEntity structureblockentity = (StructureBlockEntity)blockentity;
            structureblockentity.setMode(p_9919_.getMode());
            structureblockentity.setStructureName(p_9919_.getName());
            structureblockentity.setStructurePos(p_9919_.getOffset());
            structureblockentity.setStructureSize(p_9919_.getSize());
            structureblockentity.setMirror(p_9919_.getMirror());
            structureblockentity.setRotation(p_9919_.getRotation());
            structureblockentity.setMetaData(p_9919_.getData());
            structureblockentity.setIgnoreEntities(p_9919_.isIgnoreEntities());
            structureblockentity.setShowAir(p_9919_.isShowAir());
            structureblockentity.setShowBoundingBox(p_9919_.isShowBoundingBox());
            structureblockentity.setIntegrity(p_9919_.getIntegrity());
            structureblockentity.setSeed(p_9919_.getSeed());
            if (structureblockentity.hasStructureName()) {
               String s = structureblockentity.getStructureName();
               if (p_9919_.getUpdateType() == StructureBlockEntity.UpdateType.SAVE_AREA) {
                  if (structureblockentity.saveStructure()) {
                     this.player.displayClientMessage(Component.translatable("structure_block.save_success", s), false);
                  } else {
                     this.player.displayClientMessage(Component.translatable("structure_block.save_failure", s), false);
                  }
               } else if (p_9919_.getUpdateType() == StructureBlockEntity.UpdateType.LOAD_AREA) {
                  if (!structureblockentity.isStructureLoadable()) {
                     this.player.displayClientMessage(Component.translatable("structure_block.load_not_found", s), false);
                  } else if (structureblockentity.loadStructure(this.player.getLevel())) {
                     this.player.displayClientMessage(Component.translatable("structure_block.load_success", s), false);
                  } else {
                     this.player.displayClientMessage(Component.translatable("structure_block.load_prepare", s), false);
                  }
               } else if (p_9919_.getUpdateType() == StructureBlockEntity.UpdateType.SCAN_AREA) {
                  if (structureblockentity.detectSize()) {
                     this.player.displayClientMessage(Component.translatable("structure_block.size_success", s), false);
                  } else {
                     this.player.displayClientMessage(Component.translatable("structure_block.size_failure"), false);
                  }
               }
            } else {
               this.player.displayClientMessage(Component.translatable("structure_block.invalid_structure_name", p_9919_.getName()), false);
            }

            structureblockentity.setChanged();
            this.player.level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
         }

      }
   }

   public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket p_9917_) {
      PacketUtils.ensureRunningOnSameThread(p_9917_, this, this.player.getLevel());
      if (this.player.canUseGameMasterBlocks()) {
         BlockPos blockpos = p_9917_.getPos();
         BlockState blockstate = this.player.level.getBlockState(blockpos);
         BlockEntity blockentity = this.player.level.getBlockEntity(blockpos);
         if (blockentity instanceof JigsawBlockEntity) {
            JigsawBlockEntity jigsawblockentity = (JigsawBlockEntity)blockentity;
            jigsawblockentity.setName(p_9917_.getName());
            jigsawblockentity.setTarget(p_9917_.getTarget());
            jigsawblockentity.setPool(ResourceKey.create(Registry.TEMPLATE_POOL_REGISTRY, p_9917_.getPool()));
            jigsawblockentity.setFinalState(p_9917_.getFinalState());
            jigsawblockentity.setJoint(p_9917_.getJoint());
            jigsawblockentity.setChanged();
            this.player.level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
         }

      }
   }

   public void handleJigsawGenerate(ServerboundJigsawGeneratePacket p_9868_) {
      PacketUtils.ensureRunningOnSameThread(p_9868_, this, this.player.getLevel());
      if (this.player.canUseGameMasterBlocks()) {
         BlockPos blockpos = p_9868_.getPos();
         BlockEntity blockentity = this.player.level.getBlockEntity(blockpos);
         if (blockentity instanceof JigsawBlockEntity) {
            JigsawBlockEntity jigsawblockentity = (JigsawBlockEntity)blockentity;
            jigsawblockentity.generate(this.player.getLevel(), p_9868_.levels(), p_9868_.keepJigsaws());
         }

      }
   }

   public void handleSelectTrade(ServerboundSelectTradePacket p_9905_) {
      PacketUtils.ensureRunningOnSameThread(p_9905_, this, this.player.getLevel());
      int i = p_9905_.getItem();
      AbstractContainerMenu abstractcontainermenu = this.player.containerMenu;
      if (abstractcontainermenu instanceof MerchantMenu merchantmenu) {
         if (!merchantmenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, merchantmenu);
            return;
         }

         merchantmenu.setSelectionHint(i);
         merchantmenu.tryMoveItems(i);
      }

   }

   public void handleEditBook(ServerboundEditBookPacket p_9862_) {
      int i = p_9862_.getSlot();
      if (Inventory.isHotbarSlot(i) || i == 40) {
         List<String> list = Lists.newArrayList();
         Optional<String> optional = p_9862_.getTitle();
         optional.ifPresent(list::add);
         p_9862_.getPages().stream().limit(100L).forEach(list::add);
         Consumer<List<FilteredText>> consumer = optional.isPresent() ? (p_238198_) -> {
            this.signBook(p_238198_.get(0), p_238198_.subList(1, p_238198_.size()), i);
         } : (p_143627_) -> {
            this.updateBookContents(p_143627_, i);
         };
         this.filterTextPacket(list).thenAcceptAsync(consumer, this.server);
      }
   }

   private void updateBookContents(List<FilteredText> p_9813_, int p_9814_) {
      ItemStack itemstack = this.player.getInventory().getItem(p_9814_);
      if (itemstack.is(Items.WRITABLE_BOOK)) {
         this.updateBookPages(p_9813_, UnaryOperator.identity(), itemstack);
      }
   }

   private void signBook(FilteredText p_215209_, List<FilteredText> p_215210_, int p_215211_) {
      ItemStack itemstack = this.player.getInventory().getItem(p_215211_);
      if (itemstack.is(Items.WRITABLE_BOOK)) {
         ItemStack itemstack1 = new ItemStack(Items.WRITTEN_BOOK);
         CompoundTag compoundtag = itemstack.getTag();
         if (compoundtag != null) {
            itemstack1.setTag(compoundtag.copy());
         }

         itemstack1.addTagElement("author", StringTag.valueOf(this.player.getName().getString()));
         if (this.player.isTextFilteringEnabled()) {
            itemstack1.addTagElement("title", StringTag.valueOf(p_215209_.filteredOrEmpty()));
         } else {
            itemstack1.addTagElement("filtered_title", StringTag.valueOf(p_215209_.filteredOrEmpty()));
            itemstack1.addTagElement("title", StringTag.valueOf(p_215209_.raw()));
         }

         this.updateBookPages(p_215210_, (p_238206_) -> {
            return Component.Serializer.toJson(Component.literal(p_238206_));
         }, itemstack1);
         this.player.getInventory().setItem(p_215211_, itemstack1);
      }
   }

   private void updateBookPages(List<FilteredText> p_143635_, UnaryOperator<String> p_143636_, ItemStack p_143637_) {
      ListTag listtag = new ListTag();
      if (this.player.isTextFilteringEnabled()) {
         p_143635_.stream().map((p_238209_) -> {
            return StringTag.valueOf(p_143636_.apply(p_238209_.filteredOrEmpty()));
         }).forEach(listtag::add);
      } else {
         CompoundTag compoundtag = new CompoundTag();
         int i = 0;

         for(int j = p_143635_.size(); i < j; ++i) {
            FilteredText filteredtext = p_143635_.get(i);
            String s = filteredtext.raw();
            listtag.add(StringTag.valueOf(p_143636_.apply(s)));
            if (filteredtext.isFiltered()) {
               compoundtag.putString(String.valueOf(i), p_143636_.apply(filteredtext.filteredOrEmpty()));
            }
         }

         if (!compoundtag.isEmpty()) {
            p_143637_.addTagElement("filtered_pages", compoundtag);
         }
      }

      p_143637_.addTagElement("pages", listtag);
   }

   public void handleEntityTagQuery(ServerboundEntityTagQuery p_9864_) {
      PacketUtils.ensureRunningOnSameThread(p_9864_, this, this.player.getLevel());
      if (this.player.hasPermissions(2)) {
         Entity entity = this.player.getLevel().getEntity(p_9864_.getEntityId());
         if (entity != null) {
            CompoundTag compoundtag = entity.saveWithoutId(new CompoundTag());
            this.player.connection.send(new ClientboundTagQueryPacket(p_9864_.getTransactionId(), compoundtag));
         }

      }
   }

   public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQuery p_9837_) {
      PacketUtils.ensureRunningOnSameThread(p_9837_, this, this.player.getLevel());
      if (this.player.hasPermissions(2)) {
         BlockEntity blockentity = this.player.getLevel().getBlockEntity(p_9837_.getPos());
         CompoundTag compoundtag = blockentity != null ? blockentity.saveWithoutMetadata() : null;
         this.player.connection.send(new ClientboundTagQueryPacket(p_9837_.getTransactionId(), compoundtag));
      }
   }

   public void handleMovePlayer(ServerboundMovePlayerPacket p_9874_) {
      PacketUtils.ensureRunningOnSameThread(p_9874_, this, this.player.getLevel());
      if (containsInvalidValues(p_9874_.getX(0.0D), p_9874_.getY(0.0D), p_9874_.getZ(0.0D), p_9874_.getYRot(0.0F), p_9874_.getXRot(0.0F))) {
         this.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
      } else {
         ServerLevel serverlevel = this.player.getLevel();
         if (!this.player.wonGame) {
            if (this.tickCount == 0) {
               this.resetPosition();
            }

            if (this.awaitingPositionFromClient != null) {
               if (this.tickCount - this.awaitingTeleportTime > 20) {
                  this.awaitingTeleportTime = this.tickCount;
                  this.teleport(this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.getYRot(), this.player.getXRot());
               }

            } else {
               this.awaitingTeleportTime = this.tickCount;
               double d0 = clampHorizontal(p_9874_.getX(this.player.getX()));
               double d1 = clampVertical(p_9874_.getY(this.player.getY()));
               double d2 = clampHorizontal(p_9874_.getZ(this.player.getZ()));
               float f = Mth.wrapDegrees(p_9874_.getYRot(this.player.getYRot()));
               float f1 = Mth.wrapDegrees(p_9874_.getXRot(this.player.getXRot()));
               if (this.player.isPassenger()) {
                  this.player.absMoveTo(this.player.getX(), this.player.getY(), this.player.getZ(), f, f1);
                  this.player.getLevel().getChunkSource().move(this.player);
               } else {
                  double d3 = this.player.getX();
                  double d4 = this.player.getY();
                  double d5 = this.player.getZ();
                  double d6 = this.player.getY();
                  double d7 = d0 - this.firstGoodX;
                  double d8 = d1 - this.firstGoodY;
                  double d9 = d2 - this.firstGoodZ;
                  double d10 = this.player.getDeltaMovement().lengthSqr();
                  double d11 = d7 * d7 + d8 * d8 + d9 * d9;
                  if (this.player.isSleeping()) {
                     if (d11 > 1.0D) {
                        this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), f, f1);
                     }

                  } else {
                     ++this.receivedMovePacketCount;
                     int i = this.receivedMovePacketCount - this.knownMovePacketCount;
                     if (i > 5) {
                        LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", this.player.getName().getString(), i);
                        i = 1;
                     }

                     if (!this.player.isChangingDimension() && (!this.player.getLevel().getGameRules().getBoolean(GameRules.RULE_DISABLE_ELYTRA_MOVEMENT_CHECK) || !this.player.isFallFlying())) {
                        float f2 = this.player.isFallFlying() ? 300.0F : 100.0F;
                        if (d11 - d10 > (double)(f2 * (float)i) && !this.isSingleplayerOwner()) {
                           LOGGER.warn("{} moved too quickly! {},{},{}", this.player.getName().getString(), d7, d8, d9);
                           this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYRot(), this.player.getXRot());
                           return;
                        }
                     }

                     AABB aabb = this.player.getBoundingBox();
                     d7 = d0 - this.lastGoodX;
                     d8 = d1 - this.lastGoodY;
                     d9 = d2 - this.lastGoodZ;
                     boolean flag = d8 > 0.0D;
                     if (this.player.isOnGround() && !p_9874_.isOnGround() && flag) {
                        this.player.jumpFromGround();
                     }

                     boolean flag1 = this.player.verticalCollisionBelow;
                     this.player.move(MoverType.PLAYER, new Vec3(d7, d8, d9));
                     d7 = d0 - this.player.getX();
                     d8 = d1 - this.player.getY();
                     if (d8 > -0.5D || d8 < 0.5D) {
                        d8 = 0.0D;
                     }

                     d9 = d2 - this.player.getZ();
                     d11 = d7 * d7 + d8 * d8 + d9 * d9;
                     boolean flag2 = false;
                     if (!this.player.isChangingDimension() && d11 > 0.0625D && !this.player.isSleeping() && !this.player.gameMode.isCreative() && this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
                        flag2 = true;
                        LOGGER.warn("{} moved wrongly!", (Object)this.player.getName().getString());
                     }

                     this.player.absMoveTo(d0, d1, d2, f, f1);
                     if (this.player.noPhysics || this.player.isSleeping() || (!flag2 || !serverlevel.noCollision(this.player, aabb)) && !this.isPlayerCollidingWithAnythingNew(serverlevel, aabb)) {
                        this.clientIsFloating = d8 >= -0.03125D && !flag1 && this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR && !this.server.isFlightAllowed() && !this.player.getAbilities().mayfly && !this.player.hasEffect(MobEffects.LEVITATION) && !this.player.isFallFlying() && !this.player.isAutoSpinAttack() && this.noBlocksAround(this.player);
                        this.player.getLevel().getChunkSource().move(this.player);
                        this.player.doCheckFallDamage(this.player.getY() - d6, p_9874_.isOnGround());
                        this.player.setOnGround(p_9874_.isOnGround());
                        if (flag) {
                           this.player.resetFallDistance();
                        }

                        this.player.checkMovementStatistics(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5);
                        this.lastGoodX = this.player.getX();
                        this.lastGoodY = this.player.getY();
                        this.lastGoodZ = this.player.getZ();
                     } else {
                        this.teleport(d3, d4, d5, f, f1);
                     }
                  }
               }
            }
         }
      }
   }

   private boolean isPlayerCollidingWithAnythingNew(LevelReader p_9796_, AABB p_9797_) {
      Iterable<VoxelShape> iterable = p_9796_.getCollisions(this.player, this.player.getBoundingBox().deflate((double)1.0E-5F));
      VoxelShape voxelshape = Shapes.create(p_9797_.deflate((double)1.0E-5F));

      for(VoxelShape voxelshape1 : iterable) {
         if (!Shapes.joinIsNotEmpty(voxelshape1, voxelshape, BooleanOp.AND)) {
            return true;
         }
      }

      return false;
   }

   public void dismount(double p_143612_, double p_143613_, double p_143614_, float p_143615_, float p_143616_) {
      this.teleport(p_143612_, p_143613_, p_143614_, p_143615_, p_143616_, Collections.emptySet(), true);
   }

   public void teleport(double p_9775_, double p_9776_, double p_9777_, float p_9778_, float p_9779_) {
      this.teleport(p_9775_, p_9776_, p_9777_, p_9778_, p_9779_, Collections.emptySet(), false);
   }

   public void teleport(double p_9781_, double p_9782_, double p_9783_, float p_9784_, float p_9785_, Set<ClientboundPlayerPositionPacket.RelativeArgument> p_9786_) {
      this.teleport(p_9781_, p_9782_, p_9783_, p_9784_, p_9785_, p_9786_, false);
   }

   public void teleport(double p_143618_, double p_143619_, double p_143620_, float p_143621_, float p_143622_, Set<ClientboundPlayerPositionPacket.RelativeArgument> p_143623_, boolean p_143624_) {
      double d0 = p_143623_.contains(ClientboundPlayerPositionPacket.RelativeArgument.X) ? this.player.getX() : 0.0D;
      double d1 = p_143623_.contains(ClientboundPlayerPositionPacket.RelativeArgument.Y) ? this.player.getY() : 0.0D;
      double d2 = p_143623_.contains(ClientboundPlayerPositionPacket.RelativeArgument.Z) ? this.player.getZ() : 0.0D;
      float f = p_143623_.contains(ClientboundPlayerPositionPacket.RelativeArgument.Y_ROT) ? this.player.getYRot() : 0.0F;
      float f1 = p_143623_.contains(ClientboundPlayerPositionPacket.RelativeArgument.X_ROT) ? this.player.getXRot() : 0.0F;
      this.awaitingPositionFromClient = new Vec3(p_143618_, p_143619_, p_143620_);
      if (++this.awaitingTeleport == Integer.MAX_VALUE) {
         this.awaitingTeleport = 0;
      }

      this.awaitingTeleportTime = this.tickCount;
      this.player.absMoveTo(p_143618_, p_143619_, p_143620_, p_143621_, p_143622_);
      this.player.connection.send(new ClientboundPlayerPositionPacket(p_143618_ - d0, p_143619_ - d1, p_143620_ - d2, p_143621_ - f, p_143622_ - f1, p_143623_, this.awaitingTeleport, p_143624_));
   }

   public void handlePlayerAction(ServerboundPlayerActionPacket p_9889_) {
      PacketUtils.ensureRunningOnSameThread(p_9889_, this, this.player.getLevel());
      BlockPos blockpos = p_9889_.getPos();
      this.player.resetLastActionTime();
      ServerboundPlayerActionPacket.Action serverboundplayeractionpacket$action = p_9889_.getAction();
      switch (serverboundplayeractionpacket$action) {
         case SWAP_ITEM_WITH_OFFHAND:
            if (!this.player.isSpectator()) {
               ItemStack itemstack = this.player.getItemInHand(InteractionHand.OFF_HAND);
               this.player.setItemInHand(InteractionHand.OFF_HAND, this.player.getItemInHand(InteractionHand.MAIN_HAND));
               this.player.setItemInHand(InteractionHand.MAIN_HAND, itemstack);
               this.player.stopUsingItem();
            }

            return;
         case DROP_ITEM:
            if (!this.player.isSpectator()) {
               this.player.drop(false);
            }

            return;
         case DROP_ALL_ITEMS:
            if (!this.player.isSpectator()) {
               this.player.drop(true);
            }

            return;
         case RELEASE_USE_ITEM:
            this.player.releaseUsingItem();
            return;
         case START_DESTROY_BLOCK:
         case ABORT_DESTROY_BLOCK:
         case STOP_DESTROY_BLOCK:
            this.player.gameMode.handleBlockBreakAction(blockpos, serverboundplayeractionpacket$action, p_9889_.getDirection(), this.player.level.getMaxBuildHeight(), p_9889_.getSequence());
            this.player.connection.ackBlockChangesUpTo(p_9889_.getSequence());
            return;
         default:
            throw new IllegalArgumentException("Invalid player action");
      }
   }

   private static boolean wasBlockPlacementAttempt(ServerPlayer p_9791_, ItemStack p_9792_) {
      if (p_9792_.isEmpty()) {
         return false;
      } else {
         Item item = p_9792_.getItem();
         return (item instanceof BlockItem || item instanceof BucketItem) && !p_9791_.getCooldowns().isOnCooldown(item);
      }
   }

   public void handleUseItemOn(ServerboundUseItemOnPacket p_9930_) {
      PacketUtils.ensureRunningOnSameThread(p_9930_, this, this.player.getLevel());
      this.player.connection.ackBlockChangesUpTo(p_9930_.getSequence());
      ServerLevel serverlevel = this.player.getLevel();
      InteractionHand interactionhand = p_9930_.getHand();
      ItemStack itemstack = this.player.getItemInHand(interactionhand);
      BlockHitResult blockhitresult = p_9930_.getHitResult();
      Vec3 vec3 = blockhitresult.getLocation();
      BlockPos blockpos = blockhitresult.getBlockPos();
      Vec3 vec31 = Vec3.atCenterOf(blockpos);
      if (this.player.canInteractWith(blockpos, 3)) {
         Vec3 vec32 = vec3.subtract(vec31);
         double d0 = 1.0000001D;
         if (Math.abs(vec32.x()) < 1.0000001D && Math.abs(vec32.y()) < 1.0000001D && Math.abs(vec32.z()) < 1.0000001D) {
            Direction direction = blockhitresult.getDirection();
            this.player.resetLastActionTime();
            int i = this.player.level.getMaxBuildHeight();
            if (blockpos.getY() < i) {
               if (this.awaitingPositionFromClient == null && serverlevel.mayInteract(this.player, blockpos)) {
                  InteractionResult interactionresult = this.player.gameMode.useItemOn(this.player, serverlevel, itemstack, interactionhand, blockhitresult);
                  if (direction == Direction.UP && !interactionresult.consumesAction() && blockpos.getY() >= i - 1 && wasBlockPlacementAttempt(this.player, itemstack)) {
                     Component component = Component.translatable("build.tooHigh", i - 1).withStyle(ChatFormatting.RED);
                     this.player.sendSystemMessage(component, true);
                  } else if (interactionresult.shouldSwing()) {
                     this.player.swing(interactionhand, true);
                  }
               }
            } else {
               Component component1 = Component.translatable("build.tooHigh", i - 1).withStyle(ChatFormatting.RED);
               this.player.sendSystemMessage(component1, true);
            }

            this.player.connection.send(new ClientboundBlockUpdatePacket(serverlevel, blockpos));
            this.player.connection.send(new ClientboundBlockUpdatePacket(serverlevel, blockpos.relative(direction)));
         } else {
            LOGGER.warn("Rejecting UseItemOnPacket from {}: Location {} too far away from hit block {}.", this.player.getGameProfile().getName(), vec3, blockpos);
         }
      }
   }

   public void handleUseItem(ServerboundUseItemPacket p_9932_) {
      PacketUtils.ensureRunningOnSameThread(p_9932_, this, this.player.getLevel());
      this.ackBlockChangesUpTo(p_9932_.getSequence());
      ServerLevel serverlevel = this.player.getLevel();
      InteractionHand interactionhand = p_9932_.getHand();
      ItemStack itemstack = this.player.getItemInHand(interactionhand);
      this.player.resetLastActionTime();
      if (!itemstack.isEmpty()) {
         InteractionResult interactionresult = this.player.gameMode.useItem(this.player, serverlevel, itemstack, interactionhand);
         if (interactionresult.shouldSwing()) {
            this.player.swing(interactionhand, true);
         }

      }
   }

   public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket p_9928_) {
      PacketUtils.ensureRunningOnSameThread(p_9928_, this, this.player.getLevel());
      if (this.player.isSpectator()) {
         for(ServerLevel serverlevel : this.server.getAllLevels()) {
            Entity entity = p_9928_.getEntity(serverlevel);
            if (entity != null) {
               this.player.teleportTo(serverlevel, entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
               return;
            }
         }
      }

   }

   public void handleResourcePackResponse(ServerboundResourcePackPacket p_9901_) {
      PacketUtils.ensureRunningOnSameThread(p_9901_, this, this.player.getLevel());
      if (p_9901_.getAction() == ServerboundResourcePackPacket.Action.DECLINED && this.server.isResourcePackRequired()) {
         LOGGER.info("Disconnecting {} due to resource pack rejection", (Object)this.player.getName());
         this.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
      }

   }

   public void handlePaddleBoat(ServerboundPaddleBoatPacket p_9878_) {
      PacketUtils.ensureRunningOnSameThread(p_9878_, this, this.player.getLevel());
      Entity entity = this.player.getVehicle();
      if (entity instanceof Boat) {
         ((Boat)entity).setPaddleState(p_9878_.getLeft(), p_9878_.getRight());
      }

   }

   public void handlePong(ServerboundPongPacket p_143652_) {
   }

   public void onDisconnect(Component p_9825_) {
      LOGGER.info("{} lost connection: {}", this.player.getName().getString(), p_9825_.getString());
      this.server.invalidateStatus();
      this.server.getPlayerList().broadcastSystemMessage(Component.translatable("multiplayer.player.left", this.player.getDisplayName()).withStyle(ChatFormatting.YELLOW), false);
      this.player.disconnect();
      this.server.getPlayerList().remove(this.player);
      this.player.getTextFilter().leave();
      if (this.isSingleplayerOwner()) {
         LOGGER.info("Stopping singleplayer server as player logged out");
         this.server.halt(false);
      }

   }

   public void ackBlockChangesUpTo(int p_215202_) {
      if (p_215202_ < 0) {
         throw new IllegalArgumentException("Expected packet sequence nr >= 0");
      } else {
         this.ackBlockChangesUpTo = Math.max(p_215202_, this.ackBlockChangesUpTo);
      }
   }

   public void send(Packet<?> p_9830_) {
      this.send(p_9830_, (PacketSendListener)null);
   }

   public void send(Packet<?> p_243227_, @Nullable PacketSendListener p_243273_) {
      try {
         this.connection.send(p_243227_, p_243273_);
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Sending packet");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Packet being sent");
         crashreportcategory.setDetail("Packet class", () -> {
            return p_243227_.getClass().getCanonicalName();
         });
         throw new ReportedException(crashreport);
      }
   }

   public void handleSetCarriedItem(ServerboundSetCarriedItemPacket p_9909_) {
      PacketUtils.ensureRunningOnSameThread(p_9909_, this, this.player.getLevel());
      if (p_9909_.getSlot() >= 0 && p_9909_.getSlot() < Inventory.getSelectionSize()) {
         if (this.player.getInventory().selected != p_9909_.getSlot() && this.player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
            this.player.stopUsingItem();
         }

         this.player.getInventory().selected = p_9909_.getSlot();
         this.player.resetLastActionTime();
      } else {
         LOGGER.warn("{} tried to set an invalid carried item", (Object)this.player.getName().getString());
      }
   }

   public void handleChat(ServerboundChatPacket p_9841_) {
      if (isChatMessageIllegal(p_9841_.message())) {
         this.disconnect(Component.translatable("multiplayer.disconnect.illegal_characters"));
      } else {
         if (this.tryHandleChat(p_9841_.message(), p_9841_.timeStamp(), p_9841_.lastSeenMessages())) {
            this.server.submit(() -> {
               PlayerChatMessage playerchatmessage = this.getSignedMessage(p_9841_);
               if (this.verifyChatMessage(playerchatmessage)) {
                  this.chatMessageChain.append(() -> {
                     CompletableFuture<FilteredText> completablefuture = this.filterTextPacket(playerchatmessage.signedContent().plain());
                     CompletableFuture<PlayerChatMessage> completablefuture1 = net.minecraftforge.common.ForgeHooks.getServerChatSubmittedDecorator().decorate(this.player, playerchatmessage);
                     return CompletableFuture.allOf(completablefuture, completablefuture1).thenAcceptAsync((p_243197_) -> {
                        FilterMask filtermask = completablefuture.join().mask();
                        PlayerChatMessage playerchatmessage1 = completablefuture1.join();
                        if (playerchatmessage1 == null) return; // Forge: ServerChatEvent was canceled if this is null.
                        playerchatmessage1 = playerchatmessage1.filter(filtermask);
                        this.broadcastChatMessage(playerchatmessage1);
                     }, this.server);
                  });
               }
            });
         }

      }
   }

   public void handleChatCommand(ServerboundChatCommandPacket p_215225_) {
      if (isChatMessageIllegal(p_215225_.command())) {
         this.disconnect(Component.translatable("multiplayer.disconnect.illegal_characters"));
      } else {
         if (this.tryHandleChat(p_215225_.command(), p_215225_.timeStamp(), p_215225_.lastSeenMessages())) {
            this.server.submit(() -> {
               this.performChatCommand(p_215225_);
               this.detectRateSpam();
            });
         }

      }
   }

   private void performChatCommand(ServerboundChatCommandPacket p_242859_) {
      ParseResults<CommandSourceStack> parseresults = this.parseCommand(p_242859_.command());
      Map<String, PlayerChatMessage> map = this.collectSignedArguments(p_242859_, PreviewableCommand.of(parseresults));

      for(PlayerChatMessage playerchatmessage : map.values()) {
         if (!this.verifyChatMessage(playerchatmessage)) {
            return;
         }
      }

      CommandSigningContext commandsigningcontext = new CommandSigningContext.SignedArguments(map);
      parseresults = Commands.mapSource(parseresults, (p_242749_) -> {
         return p_242749_.withSigningContext(commandsigningcontext);
      });
      this.server.getCommands().performCommand(parseresults, p_242859_.command());
   }

   private Map<String, PlayerChatMessage> collectSignedArguments(ServerboundChatCommandPacket p_242876_, PreviewableCommand<?> p_242848_) {
      Component component = this.chatPreviewCache.pull(p_242876_.command());
      MessageSigner messagesigner = new MessageSigner(this.player.getUUID(), p_242876_.timeStamp(), p_242876_.salt());
      LastSeenMessages lastseenmessages = p_242876_.lastSeenMessages().lastSeen();
      Map<String, PlayerChatMessage> map = new Object2ObjectOpenHashMap<>();
      SignedMessageChain.Decoder signedmessagechain$decoder = this.player.connection.signedMessageDecoder();

      for(Pair<String, String> pair : ArgumentSignatures.collectPlainSignableArguments(p_242848_)) {
         String s = pair.getFirst();
         String s1 = pair.getSecond();
         MessageSignature messagesignature = p_242876_.argumentSignatures().get(s);
         ChatMessageContent chatmessagecontent;
         if (p_242876_.signedPreview() && component != null) {
            chatmessagecontent = new ChatMessageContent(s1, component);
         } else {
            chatmessagecontent = new ChatMessageContent(s1);
         }

         SignedMessageChain.Link signedmessagechain$link = new SignedMessageChain.Link(messagesignature);
         map.put(s, signedmessagechain$decoder.unpack(signedmessagechain$link, messagesigner, chatmessagecontent, lastseenmessages));
      }

      return map;
   }

   private ParseResults<CommandSourceStack> parseCommand(String p_242938_) {
      CommandDispatcher<CommandSourceStack> commanddispatcher = this.server.getCommands().getDispatcher();
      return commanddispatcher.parse(p_242938_, this.player.createCommandSourceStack());
   }

   private boolean tryHandleChat(String p_242372_, Instant p_242311_, LastSeenMessages.Update p_242217_) {
      if (!this.updateChatOrder(p_242311_)) {
         LOGGER.warn("{} sent out-of-order chat: '{}'", this.player.getName().getString(), p_242372_);
         this.disconnect(Component.translatable("multiplayer.disconnect.out_of_order_chat"));
         return false;
      } else if (this.player.getChatVisibility() == ChatVisiblity.HIDDEN) {
         this.send(new ClientboundSystemChatPacket(Component.translatable("chat.disabled.options").withStyle(ChatFormatting.RED), false));
         return false;
      } else {
         Set<LastSeenMessagesValidator.ErrorCondition> set;
         synchronized(this.lastSeenMessagesValidator) {
            set = this.lastSeenMessagesValidator.validateAndUpdate(p_242217_);
         }

         if (!set.isEmpty()) {
            this.handleValidationFailure(set);
            return false;
         } else {
            this.player.resetLastActionTime();
            return true;
         }
      }
   }

   private boolean updateChatOrder(Instant p_215237_) {
      Instant instant;
      do {
         instant = this.lastChatTimeStamp.get();
         if (p_215237_.isBefore(instant)) {
            return false;
         }
      } while(!this.lastChatTimeStamp.compareAndSet(instant, p_215237_));

      return true;
   }

   private static boolean isChatMessageIllegal(String p_215215_) {
      for(int i = 0; i < p_215215_.length(); ++i) {
         if (!SharedConstants.isAllowedChatCharacter(p_215215_.charAt(i))) {
            return true;
         }
      }

      return false;
   }

   private PlayerChatMessage getSignedMessage(ServerboundChatPacket p_242875_) {
      MessageSigner messagesigner = p_242875_.getSigner(this.player);
      SignedMessageChain.Link signedmessagechain$link = new SignedMessageChain.Link(p_242875_.signature());
      LastSeenMessages lastseenmessages = p_242875_.lastSeenMessages().lastSeen();
      ChatMessageContent chatmessagecontent = this.getSignedContent(p_242875_);
      return this.signedMessageDecoder.unpack(signedmessagechain$link, messagesigner, chatmessagecontent, lastseenmessages);
   }

   private ChatMessageContent getSignedContent(ServerboundChatPacket p_242906_) {
      Component component = this.chatPreviewCache.pull(p_242906_.message());
      return p_242906_.signedPreview() && component != null ? new ChatMessageContent(p_242906_.message(), component) : new ChatMessageContent(p_242906_.message());
   }

   private void broadcastChatMessage(PlayerChatMessage p_243277_) {
      this.server.getPlayerList().broadcastChatMessage(p_243277_, this.player, ChatType.bind(ChatType.CHAT, this.player));
      this.detectRateSpam();
   }

   private boolean verifyChatMessage(PlayerChatMessage p_242942_) {
      ChatSender chatsender = this.player.asChatSender();
      ProfilePublicKey profilepublickey = chatsender.profilePublicKey();
      if (profilepublickey != null) {
         if (profilepublickey.data().hasExpired()) {
            this.player.sendSystemMessage(Component.translatable("chat.disabled.expiredProfileKey").withStyle(ChatFormatting.RED));
            return false;
         }

         if (!p_242942_.verify(chatsender)) {
            this.disconnect(Component.translatable("multiplayer.disconnect.unsigned_chat"));
            return false;
         }
      }

      if (p_242942_.hasExpiredServer(Instant.now())) {
         LOGGER.warn("{} sent expired chat: '{}'. Is the client/server system time unsynchronized?", this.player.getName().getString(), p_242942_.signedContent().plain());
      }

      return true;
   }

   private void detectRateSpam() {
      this.chatSpamTickCount += 20;
      if (this.chatSpamTickCount > 200 && !this.server.getPlayerList().isOp(this.player.getGameProfile())) {
         this.disconnect(Component.translatable("disconnect.spam"));
      }

   }

   public void handleChatPreview(ServerboundChatPreviewPacket p_215230_) {
      if (this.handlesPreviewRequests()) {
         this.chatPreviewThrottler.schedule(() -> {
            int i = p_215230_.queryId();
            String s = p_215230_.query();
            return this.queryPreview(s).thenAccept((p_238195_) -> {
               this.sendPreviewResponse(i, p_238195_);
            });
         });
      }

   }

   private boolean handlesPreviewRequests() {
      return this.server.previewsChat() || this.connection.isMemoryConnection();
   }

   private void sendPreviewResponse(int p_238389_, Component p_238390_) {
      this.send(new ClientboundChatPreviewPacket(p_238389_, p_238390_), PacketSendListener.exceptionallySend(() -> {
         return new ClientboundChatPreviewPacket(p_238389_, (Component)null);
      }));
   }

   private CompletableFuture<Component> queryPreview(String p_215235_) {
      String s = StringUtils.normalizeSpace(p_215235_);
      return s.startsWith("/") ? this.queryCommandPreview(s.substring(1)) : this.queryChatPreview(p_215235_);
   }

   private CompletableFuture<Component> queryChatPreview(String p_215247_) {
      Component component = Component.literal(p_215247_);
      CompletableFuture<Component> completablefuture = net.minecraftforge.common.ForgeHooks.getServerChatPreviewDecorator().decorate(this.player, component).thenApply((p_238202_) -> {
         return !component.equals(p_238202_) ? p_238202_ : null;
      });
      completablefuture.thenAcceptAsync((p_242747_) -> {
         this.chatPreviewCache.set(p_215247_, p_242747_);
      }, this.server);
      return completablefuture;
   }

   private CompletableFuture<Component> queryCommandPreview(String p_215249_) {
      CommandSourceStack commandsourcestack = this.player.createCommandSourceStack();
      ParseResults<CommandSourceStack> parseresults = this.server.getCommands().getDispatcher().parse(p_215249_, commandsourcestack);
      CompletableFuture<Component> completablefuture = this.getPreviewedArgument(commandsourcestack, PreviewableCommand.of(parseresults));
      completablefuture.thenAcceptAsync((p_242743_) -> {
         this.chatPreviewCache.set(p_215249_, p_242743_);
      }, this.server);
      return completablefuture;
   }

   private CompletableFuture<Component> getPreviewedArgument(CommandSourceStack p_242862_, PreviewableCommand<CommandSourceStack> p_242851_) {
      List<PreviewableCommand.Argument<CommandSourceStack>> list = p_242851_.arguments();
      if (list.isEmpty()) {
         return CompletableFuture.completedFuture((Component)null);
      } else {
         for(int i = list.size() - 1; i >= 0; --i) {
            PreviewableCommand.Argument<CommandSourceStack> argument = list.get(i);

            try {
               CompletableFuture<Component> completablefuture = argument.previewType().resolvePreview(p_242862_, argument.parsedValue());
               if (completablefuture != null) {
                  return completablefuture;
               }
            } catch (CommandSyntaxException commandsyntaxexception) {
               return CompletableFuture.completedFuture((Component)null);
            }
         }

         return CompletableFuture.completedFuture((Component)null);
      }
   }

   public void handleChatAck(ServerboundChatAckPacket p_242387_) {
      Set<LastSeenMessagesValidator.ErrorCondition> set;
      synchronized(this.lastSeenMessagesValidator) {
         set = this.lastSeenMessagesValidator.validateAndUpdate(p_242387_.lastSeenMessages());
      }

      if (!set.isEmpty()) {
         this.handleValidationFailure(set);
      }

   }

   private void handleValidationFailure(Set<LastSeenMessagesValidator.ErrorCondition> p_242382_) {
      LOGGER.warn("Failed to validate message from {}, reasons: {}", this.player.getName().getString(), p_242382_.stream().map(LastSeenMessagesValidator.ErrorCondition::message).collect(Collectors.joining(",")));
      this.disconnect(Component.translatable("multiplayer.disconnect.chat_validation_failed"));
   }

   public void handleAnimate(ServerboundSwingPacket p_9926_) {
      PacketUtils.ensureRunningOnSameThread(p_9926_, this, this.player.getLevel());
      this.player.resetLastActionTime();
      this.player.swing(p_9926_.getHand());
   }

   public void handlePlayerCommand(ServerboundPlayerCommandPacket p_9891_) {
      PacketUtils.ensureRunningOnSameThread(p_9891_, this, this.player.getLevel());
      this.player.resetLastActionTime();
      switch (p_9891_.getAction()) {
         case PRESS_SHIFT_KEY:
            this.player.setShiftKeyDown(true);
            break;
         case RELEASE_SHIFT_KEY:
            this.player.setShiftKeyDown(false);
            break;
         case START_SPRINTING:
            this.player.setSprinting(true);
            break;
         case STOP_SPRINTING:
            this.player.setSprinting(false);
            break;
         case STOP_SLEEPING:
            if (this.player.isSleeping()) {
               this.player.stopSleepInBed(false, true);
               this.awaitingPositionFromClient = this.player.position();
            }
            break;
         case START_RIDING_JUMP:
            if (this.player.getVehicle() instanceof PlayerRideableJumping) {
               PlayerRideableJumping playerrideablejumping1 = (PlayerRideableJumping)this.player.getVehicle();
               int i = p_9891_.getData();
               if (playerrideablejumping1.canJump() && i > 0) {
                  playerrideablejumping1.handleStartJump(i);
               }
            }
            break;
         case STOP_RIDING_JUMP:
            if (this.player.getVehicle() instanceof PlayerRideableJumping) {
               PlayerRideableJumping playerrideablejumping = (PlayerRideableJumping)this.player.getVehicle();
               playerrideablejumping.handleStopJump();
            }
            break;
         case OPEN_INVENTORY:
            Entity $$2 = this.player.getVehicle();
            if ($$2 instanceof HasCustomInventoryScreen hascustominventoryscreen) {
               hascustominventoryscreen.openCustomInventoryScreen(this.player);
            }
            break;
         case START_FALL_FLYING:
            if (!this.player.tryToStartFallFlying()) {
               this.player.stopFallFlying();
            }
            break;
         default:
            throw new IllegalArgumentException("Invalid client command!");
      }

   }

   public SignedMessageChain.Decoder signedMessageDecoder() {
      return this.signedMessageDecoder;
   }

   public void addPendingMessage(PlayerChatMessage p_242439_) {
      LastSeenMessages.Entry lastseenmessages$entry = p_242439_.toLastSeenEntry();
      if (lastseenmessages$entry != null) {
         int i;
         synchronized(this.lastSeenMessagesValidator) {
            this.lastSeenMessagesValidator.addPending(lastseenmessages$entry);
            i = this.lastSeenMessagesValidator.pendingMessagesCount();
         }

         if (i > 4096) {
            this.disconnect(Component.translatable("multiplayer.disconnect.too_many_pending_chats"));
         }

      }
   }

   public void handleInteract(ServerboundInteractPacket p_9866_) {
      PacketUtils.ensureRunningOnSameThread(p_9866_, this, this.player.getLevel());
      ServerLevel serverlevel = this.player.getLevel();
      final Entity entity = p_9866_.getTarget(serverlevel);
      this.player.resetLastActionTime();
      this.player.setShiftKeyDown(p_9866_.isUsingSecondaryAction());
      if (entity != null) {
         if (!serverlevel.getWorldBorder().isWithinBounds(entity.blockPosition())) {
            return;
         }

         if (true) { //Forge: Perform distance checks below since interactions and attacks differ.
            p_9866_.dispatch(new ServerboundInteractPacket.Handler() {
               private void performInteraction(InteractionHand p_143679_, ServerGamePacketListenerImpl.EntityInteraction p_143680_) {
                  if(!ServerGamePacketListenerImpl.this.player.canInteractWith(entity, 1.5D)) return; //Forge: If the entity cannot be reached, do nothing. Original check was dist < 6, range is 4.5, so vanilla used padding=1.5
                  ItemStack itemstack = ServerGamePacketListenerImpl.this.player.getItemInHand(p_143679_).copy();
                  InteractionResult interactionresult = p_143680_.run(ServerGamePacketListenerImpl.this.player, entity, p_143679_);
                  if (interactionresult.consumesAction()) {
                     CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(ServerGamePacketListenerImpl.this.player, itemstack, entity);
                     if (interactionresult.shouldSwing()) {
                        ServerGamePacketListenerImpl.this.player.swing(p_143679_, true);
                     }
                  }

               }

               public void onInteraction(InteractionHand p_143677_) {
                  this.performInteraction(p_143677_, Player::interactOn);
               }

               public void onInteraction(InteractionHand p_143682_, Vec3 p_143683_) {
                  this.performInteraction(p_143682_, (p_143686_, p_143687_, p_143688_) -> {
                     InteractionResult onInteractEntityAtResult = net.minecraftforge.common.ForgeHooks.onInteractEntityAt(player, entity, p_143683_, p_143682_);
                     if (onInteractEntityAtResult != null) return onInteractEntityAtResult;
                     return p_143687_.interactAt(p_143686_, p_143683_, p_143688_);
                  });
               }

               public void onAttack() {
                  if (!(entity instanceof ItemEntity) && !(entity instanceof ExperienceOrb) && !(entity instanceof AbstractArrow) && entity != ServerGamePacketListenerImpl.this.player) {
                     //Forge: Perform attack range check. Original check was dist < 6, range is 3, so vanilla used padding=3
                     if(ServerGamePacketListenerImpl.this.player.canHit(entity, 3))
                     ServerGamePacketListenerImpl.this.player.attack(entity);
                  } else {
                     ServerGamePacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.invalid_entity_attacked"));
                     ServerGamePacketListenerImpl.LOGGER.warn("Player {} tried to attack an invalid entity", (Object)ServerGamePacketListenerImpl.this.player.getName().getString());
                  }
               }
            });
         }
      }

   }

   public void handleClientCommand(ServerboundClientCommandPacket p_9843_) {
      PacketUtils.ensureRunningOnSameThread(p_9843_, this, this.player.getLevel());
      this.player.resetLastActionTime();
      ServerboundClientCommandPacket.Action serverboundclientcommandpacket$action = p_9843_.getAction();
      switch (serverboundclientcommandpacket$action) {
         case PERFORM_RESPAWN:
            if (this.player.wonGame) {
               this.player.wonGame = false;
               this.player = this.server.getPlayerList().respawn(this.player, true);
               CriteriaTriggers.CHANGED_DIMENSION.trigger(this.player, Level.END, Level.OVERWORLD);
            } else {
               if (this.player.getHealth() > 0.0F) {
                  return;
               }

               this.player = this.server.getPlayerList().respawn(this.player, false);
               if (this.server.isHardcore()) {
                  this.player.setGameMode(GameType.SPECTATOR);
                  this.player.getLevel().getGameRules().getRule(GameRules.RULE_SPECTATORSGENERATECHUNKS).set(false, this.server);
               }
            }
            break;
         case REQUEST_STATS:
            this.player.getStats().sendStats(this.player);
      }

   }

   public void handleContainerClose(ServerboundContainerClosePacket p_9858_) {
      PacketUtils.ensureRunningOnSameThread(p_9858_, this, this.player.getLevel());
      this.player.doCloseContainer();
   }

   public void handleContainerClick(ServerboundContainerClickPacket p_9856_) {
      PacketUtils.ensureRunningOnSameThread(p_9856_, this, this.player.getLevel());
      this.player.resetLastActionTime();
      if (this.player.containerMenu.containerId == p_9856_.getContainerId()) {
         if (this.player.isSpectator()) {
            this.player.containerMenu.sendAllDataToRemote();
         } else if (!this.player.containerMenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
         } else {
            int i = p_9856_.getSlotNum();
            if (!this.player.containerMenu.isValidSlotIndex(i)) {
               LOGGER.debug("Player {} clicked invalid slot index: {}, available slots: {}", this.player.getName(), i, this.player.containerMenu.slots.size());
            } else {
               boolean flag = p_9856_.getStateId() != this.player.containerMenu.getStateId();
               this.player.containerMenu.suppressRemoteUpdates();
               this.player.containerMenu.clicked(i, p_9856_.getButtonNum(), p_9856_.getClickType(), this.player);

               for(Int2ObjectMap.Entry<ItemStack> entry : Int2ObjectMaps.fastIterable(p_9856_.getChangedSlots())) {
                  this.player.containerMenu.setRemoteSlotNoCopy(entry.getIntKey(), entry.getValue());
               }

               this.player.containerMenu.setRemoteCarried(p_9856_.getCarriedItem());
               this.player.containerMenu.resumeRemoteUpdates();
               if (flag) {
                  this.player.containerMenu.broadcastFullState();
               } else {
                  this.player.containerMenu.broadcastChanges();
               }

            }
         }
      }
   }

   public void handlePlaceRecipe(ServerboundPlaceRecipePacket p_9882_) {
      PacketUtils.ensureRunningOnSameThread(p_9882_, this, this.player.getLevel());
      this.player.resetLastActionTime();
      if (!this.player.isSpectator() && this.player.containerMenu.containerId == p_9882_.getContainerId() && this.player.containerMenu instanceof RecipeBookMenu) {
         if (!this.player.containerMenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
         } else {
            this.server.getRecipeManager().byKey(p_9882_.getRecipe()).ifPresent((p_238200_) -> {
               ((RecipeBookMenu)this.player.containerMenu).handlePlacement(p_9882_.isShiftDown(), p_238200_, this.player);
            });
         }
      }
   }

   public void handleContainerButtonClick(ServerboundContainerButtonClickPacket p_9854_) {
      PacketUtils.ensureRunningOnSameThread(p_9854_, this, this.player.getLevel());
      this.player.resetLastActionTime();
      if (this.player.containerMenu.containerId == p_9854_.getContainerId() && !this.player.isSpectator()) {
         if (!this.player.containerMenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
         } else {
            boolean flag = this.player.containerMenu.clickMenuButton(this.player, p_9854_.getButtonId());
            if (flag) {
               this.player.containerMenu.broadcastChanges();
            }

         }
      }
   }

   public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket p_9915_) {
      PacketUtils.ensureRunningOnSameThread(p_9915_, this, this.player.getLevel());
      if (this.player.gameMode.isCreative()) {
         boolean flag = p_9915_.getSlotNum() < 0;
         ItemStack itemstack = p_9915_.getItem();
         CompoundTag compoundtag = BlockItem.getBlockEntityData(itemstack);
         if (!itemstack.isEmpty() && compoundtag != null && compoundtag.contains("x") && compoundtag.contains("y") && compoundtag.contains("z")) {
            BlockPos blockpos = BlockEntity.getPosFromTag(compoundtag);
            if (this.player.level.isLoaded(blockpos)) {
               BlockEntity blockentity = this.player.level.getBlockEntity(blockpos);
               if (blockentity != null) {
                  blockentity.saveToItem(itemstack);
               }
            }
         }

         boolean flag1 = p_9915_.getSlotNum() >= 1 && p_9915_.getSlotNum() <= 45;
         boolean flag2 = itemstack.isEmpty() || itemstack.getDamageValue() >= 0 && itemstack.getCount() <= 64 && !itemstack.isEmpty();
         if (flag1 && flag2) {
            this.player.inventoryMenu.getSlot(p_9915_.getSlotNum()).set(itemstack);
            this.player.inventoryMenu.broadcastChanges();
         } else if (flag && flag2 && this.dropSpamTickCount < 200) {
            this.dropSpamTickCount += 20;
            this.player.drop(itemstack, true);
         }
      }

   }

   public void handleSignUpdate(ServerboundSignUpdatePacket p_9921_) {
      List<String> list = Stream.of(p_9921_.getLines()).map(ChatFormatting::stripFormatting).collect(Collectors.toList());
      this.filterTextPacket(list).thenAcceptAsync((p_215245_) -> {
         this.updateSignText(p_9921_, p_215245_);
      }, this.server);
   }

   private void updateSignText(ServerboundSignUpdatePacket p_9923_, List<FilteredText> p_9924_) {
      this.player.resetLastActionTime();
      ServerLevel serverlevel = this.player.getLevel();
      BlockPos blockpos = p_9923_.getPos();
      if (serverlevel.hasChunkAt(blockpos)) {
         BlockState blockstate = serverlevel.getBlockState(blockpos);
         BlockEntity blockentity = serverlevel.getBlockEntity(blockpos);
         if (!(blockentity instanceof SignBlockEntity)) {
            return;
         }

         SignBlockEntity signblockentity = (SignBlockEntity)blockentity;
         if (!signblockentity.isEditable() || !this.player.getUUID().equals(signblockentity.getPlayerWhoMayEdit())) {
            LOGGER.warn("Player {} just tried to change non-editable sign", (Object)this.player.getName().getString());
            return;
         }

         for(int i = 0; i < p_9924_.size(); ++i) {
            FilteredText filteredtext = p_9924_.get(i);
            if (this.player.isTextFilteringEnabled()) {
               signblockentity.setMessage(i, Component.literal(filteredtext.filteredOrEmpty()));
            } else {
               signblockentity.setMessage(i, Component.literal(filteredtext.raw()), Component.literal(filteredtext.filteredOrEmpty()));
            }
         }

         signblockentity.setChanged();
         serverlevel.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
      }

   }

   public void handleKeepAlive(ServerboundKeepAlivePacket p_9870_) {
      if (this.keepAlivePending && p_9870_.getId() == this.keepAliveChallenge) {
         int i = (int)(Util.getMillis() - this.keepAliveTime);
         this.player.latency = (this.player.latency * 3 + i) / 4;
         this.keepAlivePending = false;
      } else if (!this.isSingleplayerOwner()) {
         this.disconnect(Component.translatable("disconnect.timeout"));
      }

   }

   public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket p_9887_) {
      PacketUtils.ensureRunningOnSameThread(p_9887_, this, this.player.getLevel());
      this.player.getAbilities().flying = p_9887_.isFlying() && this.player.getAbilities().mayfly;
   }

   public void handleClientInformation(ServerboundClientInformationPacket p_9845_) {
      PacketUtils.ensureRunningOnSameThread(p_9845_, this, this.player.getLevel());
      this.player.updateOptions(p_9845_);
   }

   public void handleCustomPayload(ServerboundCustomPayloadPacket p_9860_) {
      net.minecraftforge.network.NetworkHooks.onCustomPayload(p_9860_, this.connection);
   }

   public void handleChangeDifficulty(ServerboundChangeDifficultyPacket p_9839_) {
      PacketUtils.ensureRunningOnSameThread(p_9839_, this, this.player.getLevel());
      if (this.player.hasPermissions(2) || this.isSingleplayerOwner()) {
         this.server.setDifficulty(p_9839_.getDifficulty(), false);
      }
   }

   public void handleLockDifficulty(ServerboundLockDifficultyPacket p_9872_) {
      PacketUtils.ensureRunningOnSameThread(p_9872_, this, this.player.getLevel());
      if (this.player.hasPermissions(2) || this.isSingleplayerOwner()) {
         this.server.setDifficultyLocked(p_9872_.isLocked());
      }
   }

   public ServerPlayer getPlayer() {
      return this.player;
   }

   @FunctionalInterface
   interface EntityInteraction {
      InteractionResult run(ServerPlayer p_143695_, Entity p_143696_, InteractionHand p_143697_);
   }
}
