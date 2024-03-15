package net.minecraft.client.gui.screens.worldselection;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DatapackLoadFailureScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class WorldOpenFlows {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Minecraft minecraft;
   private final LevelStorageSource levelSource;

   public WorldOpenFlows(Minecraft p_233093_, LevelStorageSource p_233094_) {
      this.minecraft = p_233093_;
      this.levelSource = p_233094_;
   }

   public void loadLevel(Screen p_233134_, String p_233135_) {
      this.doLoadLevel(p_233134_, p_233135_, false, true);
   }

   public void createFreshLevel(String p_233158_, LevelSettings p_233159_, RegistryAccess p_233160_, WorldGenSettings p_233161_) {
      LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess = this.createWorldAccess(p_233158_);
      if (levelstoragesource$levelstorageaccess != null) {
         PackRepository packrepository = createPackRepository(levelstoragesource$levelstorageaccess);
         DataPackConfig datapackconfig = p_233159_.getDataPackConfig();

         try {
            WorldLoader.PackConfig worldloader$packconfig = new WorldLoader.PackConfig(packrepository, datapackconfig, false);
            WorldStem worldstem = this.loadWorldStem(worldloader$packconfig, (p_233103_, p_233104_) -> {
               return Pair.of(new PrimaryLevelData(p_233159_, p_233161_, Lifecycle.stable()), p_233160_.freeze());
            });
            this.minecraft.doWorldLoad(p_233158_, levelstoragesource$levelstorageaccess, packrepository, worldstem);
         } catch (Exception exception) {
            LOGGER.warn("Failed to load datapacks, can't proceed with server load", (Throwable)exception);
            safeCloseAccess(levelstoragesource$levelstorageaccess, p_233158_);
         }

      }
   }

   @Nullable
   private LevelStorageSource.LevelStorageAccess createWorldAccess(String p_233156_) {
      try {
         return this.levelSource.createAccess(p_233156_);
      } catch (IOException ioexception) {
         LOGGER.warn("Failed to read level {} data", p_233156_, ioexception);
         SystemToast.onWorldAccessFailure(this.minecraft, p_233156_);
         this.minecraft.setScreen((Screen)null);
         return null;
      }
   }

   public void createLevelFromExistingSettings(LevelStorageSource.LevelStorageAccess p_233108_, ReloadableServerResources p_233109_, RegistryAccess.Frozen p_233110_, WorldData p_233111_) {
      PackRepository packrepository = createPackRepository(p_233108_);
      CloseableResourceManager closeableresourcemanager = (new WorldLoader.PackConfig(packrepository, p_233111_.getDataPackConfig(), false)).createResourceManager().getSecond();
      this.minecraft.doWorldLoad(p_233108_.getLevelId(), p_233108_, packrepository, new WorldStem(closeableresourcemanager, p_233109_, p_233110_, p_233111_));
   }

   private static PackRepository createPackRepository(LevelStorageSource.LevelStorageAccess p_233106_) {
      return new PackRepository(PackType.SERVER_DATA, new ServerPacksSource(), new FolderRepositorySource(p_233106_.getLevelPath(LevelResource.DATAPACK_DIR).toFile(), PackSource.WORLD));
   }

   private WorldStem loadWorldStem(LevelStorageSource.LevelStorageAccess p_233123_, boolean p_233124_, PackRepository p_233125_) throws Exception {
      DataPackConfig datapackconfig = p_233123_.getDataPacks();
      if (datapackconfig == null) {
         throw new IllegalStateException("Failed to load data pack config");
      } else {
         WorldLoader.PackConfig worldloader$packconfig = new WorldLoader.PackConfig(p_233125_, datapackconfig, p_233124_);
         return this.loadWorldStem(worldloader$packconfig, (p_233114_, p_233115_) -> {
            RegistryAccess.Writable registryaccess$writable = RegistryAccess.builtinCopy();
            DynamicOps<Tag> dynamicops = RegistryOps.createAndLoad(NbtOps.INSTANCE, registryaccess$writable, p_233114_);
            WorldData worlddata = p_233123_.getDataTag(dynamicops, p_233115_, registryaccess$writable.allElementsLifecycle());
            if (worlddata == null) {
               throw new IllegalStateException("Failed to load world");
            } else {
               return Pair.of(worlddata, registryaccess$writable.freeze());
            }
         });
      }
   }

   public WorldStem loadWorldStem(LevelStorageSource.LevelStorageAccess p_233120_, boolean p_233121_) throws Exception {
      PackRepository packrepository = createPackRepository(p_233120_);
      return this.loadWorldStem(p_233120_, p_233121_, packrepository);
   }

   private WorldStem loadWorldStem(WorldLoader.PackConfig p_233097_, WorldLoader.WorldDataSupplier<WorldData> p_233098_) throws Exception {
      WorldLoader.InitConfig worldloader$initconfig = new WorldLoader.InitConfig(p_233097_, Commands.CommandSelection.INTEGRATED, 2);
      CompletableFuture<WorldStem> completablefuture = WorldStem.load(worldloader$initconfig, p_233098_, Util.backgroundExecutor(), this.minecraft);
      this.minecraft.managedBlock(completablefuture::isDone);
      return completablefuture.get();
   }

   private void doLoadLevel(Screen p_233146_, String p_233147_, boolean p_233148_, boolean p_233149_) {
      // FORGE: Patch in overload to reduce further patching
      this.doLoadLevel(p_233146_, p_233147_, p_233148_, p_233149_, false);
   }

   // FORGE: Patch in confirmExperimentalWarning which confirms the experimental warning when true
   private void doLoadLevel(Screen p_233146_, String p_233147_, boolean p_233148_, boolean p_233149_, boolean confirmExperimentalWarning) {
      LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess = this.createWorldAccess(p_233147_);
      if (levelstoragesource$levelstorageaccess != null) {
         PackRepository packrepository = createPackRepository(levelstoragesource$levelstorageaccess);

         WorldStem worldstem;
         try {
            levelstoragesource$levelstorageaccess.readAdditionalLevelSaveData(); // Read extra (e.g. modded) data from the world before creating it
            worldstem = this.loadWorldStem(levelstoragesource$levelstorageaccess, p_233148_, packrepository);
            if (confirmExperimentalWarning && worldstem.worldData() instanceof PrimaryLevelData pld) {
               pld.withConfirmedWarning(true);
            }
         } catch (Exception exception) {
            LOGGER.warn("Failed to load datapacks, can't proceed with server load", (Throwable)exception);
            this.minecraft.setScreen(new DatapackLoadFailureScreen(() -> {
               this.doLoadLevel(p_233146_, p_233147_, true, p_233149_);
            }));
            safeCloseAccess(levelstoragesource$levelstorageaccess, p_233147_);
            return;
         }

         WorldData worlddata = worldstem.worldData();
         boolean flag = worlddata.worldGenSettings().isOldCustomizedWorld();
         boolean flag1 = worlddata.worldGenSettingsLifecycle() != Lifecycle.stable();
         // Forge: Skip confirmation if it has been done already for this world
         boolean skipConfirmation = worlddata instanceof PrimaryLevelData pld && pld.hasConfirmedExperimentalWarning();
         if (skipConfirmation || !p_233149_ || !flag && !flag1) {
            this.minecraft.getClientPackSource().loadBundledResourcePack(levelstoragesource$levelstorageaccess).thenApply((p_233177_) -> {
               return true;
            }).exceptionallyComposeAsync((p_233183_) -> {
               LOGGER.warn("Failed to load pack: ", p_233183_);
               return this.promptBundledPackLoadFailure();
            }, this.minecraft).thenAcceptAsync((p_233168_) -> {
               if (p_233168_) {
                  this.minecraft.doWorldLoad(p_233147_, levelstoragesource$levelstorageaccess, packrepository, worldstem);
               } else {
                  worldstem.close();
                  safeCloseAccess(levelstoragesource$levelstorageaccess, p_233147_);
                  this.minecraft.getClientPackSource().clearServerPack().thenRunAsync(() -> {
                     this.minecraft.setScreen(p_233146_);
                  }, this.minecraft);
               }

            }, this.minecraft).exceptionally((p_233175_) -> {
               this.minecraft.delayCrash(CrashReport.forThrowable(p_233175_, "Load world"));
               return null;
            });
         } else {
            if (flag) // Forge: For legacy world options, let vanilla handle it.
            this.askForBackup(p_233146_, p_233147_, flag, () -> {
               this.doLoadLevel(p_233146_, p_233147_, p_233148_, false);
            });
            else net.minecraftforge.client.ForgeHooksClient.createWorldConfirmationScreen(() -> this.doLoadLevel(p_233146_, p_233147_, p_233148_, false, true));
            worldstem.close();
            safeCloseAccess(levelstoragesource$levelstorageaccess, p_233147_);
         }
      }
   }

   private CompletableFuture<Boolean> promptBundledPackLoadFailure() {
      CompletableFuture<Boolean> completablefuture = new CompletableFuture<>();
      this.minecraft.setScreen(new ConfirmScreen(completablefuture::complete, Component.translatable("multiplayer.texturePrompt.failure.line1"), Component.translatable("multiplayer.texturePrompt.failure.line2"), CommonComponents.GUI_PROCEED, CommonComponents.GUI_CANCEL));
      return completablefuture;
   }

   private static void safeCloseAccess(LevelStorageSource.LevelStorageAccess p_233117_, String p_233118_) {
      try {
         p_233117_.close();
      } catch (IOException ioexception) {
         LOGGER.warn("Failed to unlock access to level {}", p_233118_, ioexception);
      }

   }

   private void askForBackup(Screen p_233141_, String p_233142_, boolean p_233143_, Runnable p_233144_) {
      Component component;
      Component component1;
      if (p_233143_) {
         component = Component.translatable("selectWorld.backupQuestion.customized");
         component1 = Component.translatable("selectWorld.backupWarning.customized");
      } else {
         component = Component.translatable("selectWorld.backupQuestion.experimental");
         component1 = Component.translatable("selectWorld.backupWarning.experimental");
      }

      this.minecraft.setScreen(new BackupConfirmScreen(p_233141_, (p_233172_, p_233173_) -> {
         if (p_233172_) {
            EditWorldScreen.makeBackupAndShowToast(this.levelSource, p_233142_);
         }

         p_233144_.run();
      }, component, component1, false));
   }

   public static void confirmWorldCreation(Minecraft p_233127_, CreateWorldScreen p_233128_, Lifecycle p_233129_, Runnable p_233130_) {
      BooleanConsumer booleanconsumer = (p_233154_) -> {
         if (p_233154_) {
            p_233130_.run();
         } else {
            p_233127_.setScreen(p_233128_);
         }

      };
      if (p_233129_ == Lifecycle.stable()) {
         p_233130_.run();
      } else if (p_233129_ == Lifecycle.experimental()) {
         p_233127_.setScreen(new ConfirmScreen(booleanconsumer, Component.translatable("selectWorld.import_worldgen_settings.experimental.title"), Component.translatable("selectWorld.import_worldgen_settings.experimental.question")));
      } else {
         p_233127_.setScreen(new ConfirmScreen(booleanconsumer, Component.translatable("selectWorld.import_worldgen_settings.deprecated.title"), Component.translatable("selectWorld.import_worldgen_settings.deprecated.question")));
      }

   }
}
