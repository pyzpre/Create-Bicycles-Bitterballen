package net.minecraft.server.packs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.slf4j.Logger;

public class VanillaPackResources implements PackResources {
   @Nullable
   public static Path generatedDir;
   private static final Logger LOGGER = LogUtils.getLogger();
   public static Class<?> clientObject;
   private static final Map<PackType, Path> ROOT_DIR_BY_TYPE = Util.make(() -> {
      synchronized(VanillaPackResources.class) {
         ImmutableMap.Builder<PackType, Path> builder = ImmutableMap.builder();

         for(PackType packtype : PackType.values()) {
            String s = "/" + packtype.getDirectory() + "/.mcassetsroot";
            URL url = VanillaPackResources.class.getResource(s);
            if (url == null) {
               LOGGER.error("File {} does not exist in classpath", (Object)s);
            } else {
               try {
                  URI uri = url.toURI();
                  String s1 = uri.getScheme();
                  if (!"jar".equals(s1) && !"file".equals(s1)) {
                     LOGGER.warn("Assets URL '{}' uses unexpected schema", (Object)uri);
                  }

                  Path path = safeGetPath(uri);
                  builder.put(packtype, path.getParent());
               } catch (Exception exception) {
                  LOGGER.error("Couldn't resolve path to vanilla assets", (Throwable)exception);
               }
            }
         }

         return builder.build();
      }
   });
   public final PackMetadataSection packMetadata;
   public final Set<String> namespaces;

   private static Path safeGetPath(URI p_182298_) throws IOException {
      try {
         return Paths.get(p_182298_);
      } catch (FileSystemNotFoundException filesystemnotfoundexception) {
      } catch (Throwable throwable) {
         LOGGER.warn("Unable to get path for: {}", p_182298_, throwable);
      }

      try {
         FileSystems.newFileSystem(p_182298_, Collections.emptyMap());
      } catch (FileSystemAlreadyExistsException filesystemalreadyexistsexception) {
      }

      return Paths.get(p_182298_);
   }

   public VanillaPackResources(PackMetadataSection p_143761_, String... p_143762_) {
      this.packMetadata = p_143761_;
      this.namespaces = ImmutableSet.copyOf(p_143762_);
   }

   public InputStream getRootResource(String p_10358_) throws IOException {
      if (!p_10358_.contains("/") && !p_10358_.contains("\\")) {
         if (generatedDir != null) {
            Path path = generatedDir.resolve(p_10358_);
            if (Files.exists(path)) {
               return Files.newInputStream(path);
            }
         }

         return this.getResourceAsStream(p_10358_);
      } else {
         throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
      }
   }

   public InputStream getResource(PackType p_10330_, ResourceLocation p_10331_) throws IOException {
      InputStream inputstream = this.getResourceAsStream(p_10330_, p_10331_);
      if (inputstream != null) {
         return inputstream;
      } else {
         throw new FileNotFoundException(p_10331_.getPath());
      }
   }

   public Collection<ResourceLocation> getResources(PackType p_215346_, String p_215347_, String p_215348_, Predicate<ResourceLocation> p_215349_) {
      Set<ResourceLocation> set = Sets.newHashSet();
      if (generatedDir != null) {
         try {
            getResources(set, p_215347_, generatedDir.resolve(p_215346_.getDirectory()), p_215348_, p_215349_);
         } catch (IOException ioexception2) {
         }

         if (p_215346_ == PackType.CLIENT_RESOURCES) {
            Enumeration<URL> enumeration = null;

            try {
               enumeration = clientObject.getClassLoader().getResources(p_215346_.getDirectory() + "/");
            } catch (IOException ioexception1) {
            }

            while(enumeration != null && enumeration.hasMoreElements()) {
               try {
                  URI uri = enumeration.nextElement().toURI();
                  if ("file".equals(uri.getScheme())) {
                     getResources(set, p_215347_, Paths.get(uri), p_215348_, p_215349_);
                  }
               } catch (IOException | URISyntaxException urisyntaxexception) {
               }
            }
         }
      }

      try {
         Path path = ROOT_DIR_BY_TYPE.get(p_215346_);
         if (path != null) {
            if (net.minecraftforge.resource.ResourceCacheManager.shouldUseCache() && this.cacheManager.hasCached(p_215346_, p_215347_)) {
               set.addAll(this.cacheManager.getResources(p_215346_, p_215347_, path.getFileSystem().getPath(p_215348_), p_215349_));
            } else
            getResources(set, p_215347_, path, p_215348_, p_215349_);
         } else {
            LOGGER.error("Can't access assets root for type: {}", (Object)p_215346_);
         }
      } catch (NoSuchFileException | FileNotFoundException filenotfoundexception) {
      } catch (IOException ioexception) {
         LOGGER.error("Couldn't get a list of all vanilla resources", (Throwable)ioexception);
      }

      return set;
   }

   private static void getResources(Collection<ResourceLocation> p_215358_, String p_215359_, Path p_215360_, String p_215361_, Predicate<ResourceLocation> p_215362_) throws IOException {
      Path path = p_215360_.resolve(p_215359_);
      Stream<Path> stream = Files.walk(path.resolve(p_215361_));

      try {
         stream.filter((p_215351_) -> {
            return !p_215351_.endsWith(".mcmeta") && Files.isRegularFile(p_215351_);
         }).<ResourceLocation>mapMulti((p_215355_, p_215356_) -> {
            String s = path.relativize(p_215355_).toString().replaceAll("\\\\", "/");
            ResourceLocation resourcelocation = ResourceLocation.tryBuild(p_215359_, s);
            if (resourcelocation == null) {
               Util.logAndPauseIfInIde(String.format(Locale.ROOT, "Invalid path in datapack: %s:%s, ignoring", p_215359_, s));
            } else {
               p_215356_.accept(resourcelocation);
            }

         }).filter(p_215362_).forEach(p_215358_::add);
      } catch (Throwable throwable1) {
         if (stream != null) {
            try {
               stream.close();
            } catch (Throwable throwable) {
               throwable1.addSuppressed(throwable);
            }
         }

         throw throwable1;
      }

      if (stream != null) {
         stream.close();
      }

   }

   @Nullable
   protected InputStream getResourceAsStream(PackType p_10359_, ResourceLocation p_10360_) {
      String s = createPath(p_10359_, p_10360_);
      if (generatedDir != null) {
         Path path = generatedDir.resolve(p_10359_.getDirectory() + "/" + p_10360_.getNamespace() + "/" + p_10360_.getPath());
         if (Files.exists(path)) {
            try {
               return Files.newInputStream(path);
            } catch (IOException ioexception1) {
            }
         }
      }

      try {
         URL url = VanillaPackResources.class.getResource(s);
         return isResourceUrlValid(s, url) ? getExtraInputStream(p_10359_, s) : null;
      } catch (IOException ioexception) {
         return VanillaPackResources.class.getResourceAsStream(s);
      }
   }

   private static String createPath(PackType p_10363_, ResourceLocation p_10364_) {
      return "/" + p_10363_.getDirectory() + "/" + p_10364_.getNamespace() + "/" + p_10364_.getPath();
   }

   private static boolean isResourceUrlValid(String p_10336_, @Nullable URL p_10337_) throws IOException {
      return p_10337_ != null && (p_10337_.getProtocol().equals("jar") || FolderPackResources.validatePath(new File(p_10337_.getFile()), p_10336_));
   }

   @Nullable
   protected InputStream getResourceAsStream(String p_10334_) {
      return getExtraInputStream(PackType.SERVER_DATA, "/" + p_10334_);
   }

   public boolean hasResource(PackType p_10355_, ResourceLocation p_10356_) {
      String s = createPath(p_10355_, p_10356_);
      if (generatedDir != null) {
         Path path = generatedDir.resolve(p_10355_.getDirectory() + "/" + p_10356_.getNamespace() + "/" + p_10356_.getPath());
         if (Files.exists(path)) {
            return true;
         }
      }

      try {
         URL url = VanillaPackResources.class.getResource(s);
         return isResourceUrlValid(s, url);
      } catch (IOException ioexception) {
         return false;
      }
   }

   public Set<String> getNamespaces(PackType p_10322_) {
      return this.namespaces;
   }

   @Nullable
   public <T> T getMetadataSection(MetadataSectionSerializer<T> p_10333_) throws IOException {
      try {
         InputStream inputstream = this.getRootResource("pack.mcmeta");

         Object object;
         label59: {
            try {
               if (inputstream != null) {
                  T t = AbstractPackResources.getMetadataFromStream(p_10333_, inputstream);
                  if (t != null) {
                     object = t;
                     break label59;
                  }
               }
            } catch (Throwable throwable1) {
               if (inputstream != null) {
                  try {
                     inputstream.close();
                  } catch (Throwable throwable) {
                     throwable1.addSuppressed(throwable);
                  }
               }

               throw throwable1;
            }

            if (inputstream != null) {
               inputstream.close();
            }

            return (T)(p_10333_ == PackMetadataSection.SERIALIZER ? this.packMetadata : null);
         }

         if (inputstream != null) {
            inputstream.close();
         }

         return (T)object;
      } catch (FileNotFoundException | RuntimeException runtimeexception) {
         return (T)(p_10333_ == PackMetadataSection.SERIALIZER ? this.packMetadata : null);
      }
   }

   public String getName() {
      return "Default";
   }

   public void close() {
   }

   //Vanilla used to just grab from the classpath, this breaks dev environments, and Forge runtime
   //as forge ships vanilla assets in an 'extra' jar with no classes.
   //So find that extra jar using the .mcassetsroot marker.
   private InputStream getExtraInputStream(PackType type, String resource) {
      try {
         Path rootDir = ROOT_DIR_BY_TYPE.get(type);
         if (rootDir != null)
            return Files.newInputStream(rootDir.resolve(resource));
         return VanillaPackResources.class.getResourceAsStream(resource);
      } catch (IOException e) {
         return VanillaPackResources.class.getResourceAsStream(resource);
      }
   }

   public ResourceProvider asProvider() {
      return (p_215344_) -> {
         return Optional.of(new Resource(this.getName(), () -> {
            return this.getResource(PackType.CLIENT_RESOURCES, p_215344_);
         }));
      };
   }

   private final net.minecraftforge.resource.ResourceCacheManager cacheManager = new net.minecraftforge.resource.ResourceCacheManager(false, "indexVanillaPackCachesOnThread", (packType, namespace) -> ROOT_DIR_BY_TYPE.get(packType).resolve(namespace));

   @Override
   public void initForNamespace(final String nameSpace) {
      if (net.minecraftforge.resource.ResourceCacheManager.shouldUseCache())
         this.cacheManager.index(nameSpace);
   }

   @Override
   public void init(final PackType packType) {
      initForNamespace("minecraft"); //Default namespaces.
      initForNamespace("realms");
   }
}
