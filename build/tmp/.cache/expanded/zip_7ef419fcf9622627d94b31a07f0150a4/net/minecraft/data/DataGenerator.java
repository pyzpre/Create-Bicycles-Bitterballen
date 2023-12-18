package net.minecraft.data;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.WorldVersion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import org.slf4j.Logger;

public class DataGenerator {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Collection<Path> inputFolders;
   private final Path outputFolder;
   private final List<DataProvider> allProviders = Lists.newArrayList();
   private final List<DataProvider> providersToRun = Lists.newArrayList();
   private final WorldVersion version;
   private final boolean alwaysGenerate;
   private final List<DataProvider> providerView = java.util.Collections.unmodifiableList(allProviders);

   public DataGenerator(Path p_236030_, Collection<Path> p_236031_, WorldVersion p_236032_, boolean p_236033_) {
      this.outputFolder = p_236030_;
      this.version = p_236032_;
      this.alwaysGenerate = p_236033_;
      this.inputFolders = Lists.newArrayList(p_236031_);
   }

   public Collection<Path> getInputFolders() {
      return this.inputFolders;
   }

   public Path getOutputFolder() {
      return this.outputFolder;
   }

   public Path getOutputFolder(DataGenerator.Target p_236035_) {
      return this.getOutputFolder().resolve(p_236035_.directory);
   }

   public void run() throws IOException {
      HashCache hashcache = new HashCache(this.outputFolder, this.allProviders, this.version);
      Stopwatch stopwatch = Stopwatch.createStarted();
      Stopwatch stopwatch1 = Stopwatch.createUnstarted();

      for(DataProvider dataprovider : this.providersToRun) {
         if (!this.alwaysGenerate && !hashcache.shouldRunInThisVersion(dataprovider)) {
            LOGGER.debug("Generator {} already run for version {}", dataprovider.getName(), this.version.getName());
         } else {
            LOGGER.info("Starting provider: {}", (Object)dataprovider.getName());
            net.minecraftforge.fml.StartupMessageManager.addModMessage("Generating: " + dataprovider.getName());
            stopwatch1.start();
            dataprovider.run(hashcache.getUpdater(dataprovider));
            stopwatch1.stop();
            LOGGER.info("{} finished after {} ms", dataprovider.getName(), stopwatch1.elapsed(TimeUnit.MILLISECONDS));
            stopwatch1.reset();
         }
      }

      LOGGER.info("All providers took: {} ms", (long)stopwatch.elapsed(TimeUnit.MILLISECONDS));
      hashcache.purgeStaleAndWrite();
   }

   public void addProvider(boolean p_236040_, DataProvider p_236041_) {
      if (p_236040_) {
         this.providersToRun.add(p_236041_);
      }

      this.allProviders.add(p_236041_);
   }

   public DataGenerator.PathProvider createPathProvider(DataGenerator.Target p_236037_, String p_236038_) {
      return new DataGenerator.PathProvider(this, p_236037_, p_236038_);
   }

   public List<DataProvider> getProviders() {
       return this.providerView;
   }

   public void addInput(Path value) {
      this.inputFolders.add(value);
   }

   static {
      Bootstrap.bootStrap();
   }

   public static class PathProvider {
      private final Path root;
      private final String kind;

      PathProvider(DataGenerator p_236045_, DataGenerator.Target p_236046_, String p_236047_) {
         this.root = p_236045_.getOutputFolder(p_236046_);
         this.kind = p_236047_;
      }

      public Path file(ResourceLocation p_236051_, String p_236052_) {
         return this.root.resolve(p_236051_.getNamespace()).resolve(this.kind).resolve(p_236051_.getPath() + "." + p_236052_);
      }

      public Path json(ResourceLocation p_236049_) {
         return this.root.resolve(p_236049_.getNamespace()).resolve(this.kind).resolve(p_236049_.getPath() + ".json");
      }
   }

   public static enum Target {
      DATA_PACK("data"),
      RESOURCE_PACK("assets"),
      REPORTS("reports");

      final String directory;

      private Target(String p_236062_) {
         this.directory = p_236062_;
      }
   }
}
