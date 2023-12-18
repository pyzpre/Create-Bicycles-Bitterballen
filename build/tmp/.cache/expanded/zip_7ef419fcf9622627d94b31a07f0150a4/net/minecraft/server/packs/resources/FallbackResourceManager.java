package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;

public class FallbackResourceManager implements ResourceManager {
   static final Logger LOGGER = LogUtils.getLogger();
   public final List<FallbackResourceManager.PackEntry> fallbacks = Lists.newArrayList();
   final PackType type;
   private final String namespace;

   public FallbackResourceManager(PackType p_10605_, String p_10606_) {
      this.type = p_10605_;
      this.namespace = p_10606_;
   }

   public void push(PackResources p_215378_) {
      this.pushInternal(p_215378_.getName(), p_215378_, (Predicate<ResourceLocation>)null);
   }

   public void push(PackResources p_215383_, Predicate<ResourceLocation> p_215384_) {
      this.pushInternal(p_215383_.getName(), p_215383_, p_215384_);
   }

   public void pushFilterOnly(String p_215400_, Predicate<ResourceLocation> p_215401_) {
      this.pushInternal(p_215400_, (PackResources)null, p_215401_);
   }

   private void pushInternal(String p_215396_, @Nullable PackResources p_215397_, @Nullable Predicate<ResourceLocation> p_215398_) {
      this.fallbacks.add(new FallbackResourceManager.PackEntry(p_215396_, p_215397_, p_215398_));
   }

   public Set<String> getNamespaces() {
      return ImmutableSet.of(this.namespace);
   }

   public Optional<Resource> getResource(ResourceLocation p_215419_) {
      if (!this.isValidLocation(p_215419_)) {
         return Optional.empty();
      } else {
         for(int i = this.fallbacks.size() - 1; i >= 0; --i) {
            FallbackResourceManager.PackEntry fallbackresourcemanager$packentry = this.fallbacks.get(i);
            PackResources packresources = fallbackresourcemanager$packentry.resources;
            if (packresources != null && packresources.hasResource(this.type, p_215419_)) {
               return Optional.of(new Resource(packresources.getName(), this.createResourceGetter(p_215419_, packresources), this.createStackMetadataFinder(p_215419_, i)));
            }

            if (fallbackresourcemanager$packentry.isFiltered(p_215419_)) {
               LOGGER.warn("Resource {} not found, but was filtered by pack {}", p_215419_, fallbackresourcemanager$packentry.name);
               return Optional.empty();
            }
         }

         return Optional.empty();
      }
   }

   Resource.IoSupplier<InputStream> createResourceGetter(ResourceLocation p_215375_, PackResources p_215376_) {
      return LOGGER.isDebugEnabled() ? () -> {
         InputStream inputstream = p_215376_.getResource(this.type, p_215375_);
         return new FallbackResourceManager.LeakedResourceWarningInputStream(inputstream, p_215375_, p_215376_.getName());
      } : () -> {
         return p_215376_.getResource(this.type, p_215375_);
      };
   }

   private boolean isValidLocation(ResourceLocation p_10629_) {
      return !p_10629_.getPath().contains("..");
   }

   public List<Resource> getResourceStack(ResourceLocation p_215367_) {
      if (!this.isValidLocation(p_215367_)) {
         return List.of();
      } else {
         List<FallbackResourceManager.SinglePackResourceThunkSupplier> list = Lists.newArrayList();
         ResourceLocation resourcelocation = getMetadataLocation(p_215367_);
         String s = null;

         for(FallbackResourceManager.PackEntry fallbackresourcemanager$packentry : this.fallbacks) {
            if (fallbackresourcemanager$packentry.isFiltered(p_215367_)) {
               if (!list.isEmpty()) {
                  s = fallbackresourcemanager$packentry.name;
               }

               list.clear();
            } else if (fallbackresourcemanager$packentry.isFiltered(resourcelocation)) {
               list.forEach(FallbackResourceManager.SinglePackResourceThunkSupplier::ignoreMeta);
            }

            PackResources packresources = fallbackresourcemanager$packentry.resources;
            if (packresources != null && packresources.hasResource(this.type, p_215367_)) {
               list.add(new FallbackResourceManager.SinglePackResourceThunkSupplier(p_215367_, resourcelocation, packresources));
            }
         }

         if (list.isEmpty() && s != null) {
            LOGGER.info("Resource {} was filtered by pack {}", p_215367_, s);
         }

         return list.stream().map(FallbackResourceManager.SinglePackResourceThunkSupplier::create).toList();
      }
   }

   public Map<ResourceLocation, Resource> listResources(String p_215413_, Predicate<ResourceLocation> p_215414_) {
      Object2IntMap<ResourceLocation> object2intmap = new Object2IntOpenHashMap<>();
      int i = this.fallbacks.size();

      for(int j = 0; j < i; ++j) {
         FallbackResourceManager.PackEntry fallbackresourcemanager$packentry = this.fallbacks.get(j);
         fallbackresourcemanager$packentry.filterAll(object2intmap.keySet());
         if (fallbackresourcemanager$packentry.resources != null) {
            for(ResourceLocation resourcelocation : fallbackresourcemanager$packentry.resources.getResources(this.type, this.namespace, p_215413_, p_215414_)) {
               object2intmap.put(resourcelocation, j);
            }
         }
      }

      Map<ResourceLocation, Resource> map = Maps.newTreeMap();

      for(Object2IntMap.Entry<ResourceLocation> entry : Object2IntMaps.fastIterable(object2intmap)) {
         int k = entry.getIntValue();
         ResourceLocation resourcelocation1 = entry.getKey();
         PackResources packresources = (this.fallbacks.get(k)).resources;
         map.put(resourcelocation1, new Resource(packresources.getName(), this.createResourceGetter(resourcelocation1, packresources), this.createStackMetadataFinder(resourcelocation1, k)));
      }

      return map;
   }

   private Resource.IoSupplier<ResourceMetadata> createStackMetadataFinder(ResourceLocation p_215369_, int p_215370_) {
      return () -> {
         ResourceLocation resourcelocation = getMetadataLocation(p_215369_);

         for(int i = this.fallbacks.size() - 1; i >= p_215370_; --i) {
            FallbackResourceManager.PackEntry fallbackresourcemanager$packentry = this.fallbacks.get(i);
            PackResources packresources = fallbackresourcemanager$packentry.resources;
            if (packresources != null && packresources.hasResource(this.type, resourcelocation)) {
               InputStream inputstream = packresources.getResource(this.type, resourcelocation);

               ResourceMetadata resourcemetadata;
               try {
                  resourcemetadata = ResourceMetadata.fromJsonStream(inputstream);
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

               return resourcemetadata;
            }

            if (fallbackresourcemanager$packentry.isFiltered(resourcelocation)) {
               break;
            }
         }

         return ResourceMetadata.EMPTY;
      };
   }

   private static void applyPackFiltersToExistingResources(FallbackResourceManager.PackEntry p_215393_, Map<ResourceLocation, FallbackResourceManager.EntryStack> p_215394_) {
      Iterator<Map.Entry<ResourceLocation, FallbackResourceManager.EntryStack>> iterator = p_215394_.entrySet().iterator();

      while(iterator.hasNext()) {
         Map.Entry<ResourceLocation, FallbackResourceManager.EntryStack> entry = iterator.next();
         ResourceLocation resourcelocation = entry.getKey();
         FallbackResourceManager.EntryStack fallbackresourcemanager$entrystack = entry.getValue();
         if (p_215393_.isFiltered(resourcelocation)) {
            iterator.remove();
         } else if (p_215393_.isFiltered(fallbackresourcemanager$entrystack.metadataLocation())) {
            fallbackresourcemanager$entrystack.entries.forEach(FallbackResourceManager.SinglePackResourceThunkSupplier::ignoreMeta);
         }
      }

   }

   private void listPackResources(FallbackResourceManager.PackEntry p_215388_, String p_215389_, Predicate<ResourceLocation> p_215390_, Map<ResourceLocation, FallbackResourceManager.EntryStack> p_215391_) {
      PackResources packresources = p_215388_.resources;
      if (packresources != null) {
         for(ResourceLocation resourcelocation : packresources.getResources(this.type, this.namespace, p_215389_, p_215390_)) {
            ResourceLocation resourcelocation1 = getMetadataLocation(resourcelocation);
            p_215391_.computeIfAbsent(resourcelocation, (p_215373_) -> {
               return new FallbackResourceManager.EntryStack(resourcelocation1, Lists.newArrayList());
            }).entries().add(new FallbackResourceManager.SinglePackResourceThunkSupplier(resourcelocation, resourcelocation1, packresources));
         }

      }
   }

   public Map<ResourceLocation, List<Resource>> listResourceStacks(String p_215416_, Predicate<ResourceLocation> p_215417_) {
      Map<ResourceLocation, FallbackResourceManager.EntryStack> map = Maps.newHashMap();

      for(FallbackResourceManager.PackEntry fallbackresourcemanager$packentry : this.fallbacks) {
         applyPackFiltersToExistingResources(fallbackresourcemanager$packentry, map);
         this.listPackResources(fallbackresourcemanager$packentry, p_215416_, p_215417_, map);
      }

      TreeMap<ResourceLocation, List<Resource>> treemap = Maps.newTreeMap();
      map.forEach((p_215404_, p_215405_) -> {
         treemap.put(p_215404_, p_215405_.createThunks());
      });
      return treemap;
   }

   public Stream<PackResources> listPacks() {
      return this.fallbacks.stream().map((p_215386_) -> {
         return p_215386_.resources;
      }).filter(Objects::nonNull);
   }

   static ResourceLocation getMetadataLocation(ResourceLocation p_10625_) {
      return new ResourceLocation(p_10625_.getNamespace(), p_10625_.getPath() + ".mcmeta");
   }

   static record EntryStack(ResourceLocation metadataLocation, List<FallbackResourceManager.SinglePackResourceThunkSupplier> entries) {
      List<Resource> createThunks() {
         return this.entries().stream().map(FallbackResourceManager.SinglePackResourceThunkSupplier::create).toList();
      }
   }

   static class LeakedResourceWarningInputStream extends FilterInputStream {
      private final String message;
      private boolean closed;

      public LeakedResourceWarningInputStream(InputStream p_10633_, ResourceLocation p_10634_, String p_10635_) {
         super(p_10633_);
         ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
         (new Exception()).printStackTrace(new PrintStream(bytearrayoutputstream));
         this.message = "Leaked resource: '" + p_10634_ + "' loaded from pack: '" + p_10635_ + "'\n" + bytearrayoutputstream;
      }

      public void close() throws IOException {
         super.close();
         this.closed = true;
      }

      protected void finalize() throws Throwable {
         if (!this.closed) {
            FallbackResourceManager.LOGGER.warn(this.message);
         }

         super.finalize();
      }
   }

   static record PackEntry(String name, @Nullable PackResources resources, @Nullable Predicate<ResourceLocation> filter) {
      public void filterAll(Collection<ResourceLocation> p_215443_) {
         if (this.filter != null) {
            p_215443_.removeIf(this.filter);
         }

      }

      public boolean isFiltered(ResourceLocation p_215441_) {
         return this.filter != null && this.filter.test(p_215441_);
      }
   }

   class SinglePackResourceThunkSupplier {
      private final ResourceLocation location;
      private final ResourceLocation metadataLocation;
      private final PackResources source;
      private boolean shouldGetMeta = true;

      SinglePackResourceThunkSupplier(ResourceLocation p_215457_, ResourceLocation p_215458_, PackResources p_215459_) {
         this.source = p_215459_;
         this.location = p_215457_;
         this.metadataLocation = p_215458_;
      }

      public void ignoreMeta() {
         this.shouldGetMeta = false;
      }

      public Resource create() {
         String s = this.source.getName();
         return this.shouldGetMeta ? new Resource(s, FallbackResourceManager.this.createResourceGetter(this.location, this.source), () -> {
            if (this.source.hasResource(FallbackResourceManager.this.type, this.metadataLocation)) {
               InputStream inputstream = this.source.getResource(FallbackResourceManager.this.type, this.metadataLocation);

               ResourceMetadata resourcemetadata;
               try {
                  resourcemetadata = ResourceMetadata.fromJsonStream(inputstream);
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

               return resourcemetadata;
            } else {
               return ResourceMetadata.EMPTY;
            }
         }) : new Resource(s, FallbackResourceManager.this.createResourceGetter(this.location, this.source));
      }
   }
}