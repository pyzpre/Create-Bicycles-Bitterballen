package net.minecraft.server.packs.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;

public class Resource {
   private final String packId;
   private final Resource.IoSupplier<InputStream> streamSupplier;
   private final Resource.IoSupplier<ResourceMetadata> metadataSupplier;
   @Nullable
   private ResourceMetadata cachedMetadata;

   public Resource(String p_215503_, Resource.IoSupplier<InputStream> p_215504_, Resource.IoSupplier<ResourceMetadata> p_215505_) {
      this.packId = p_215503_;
      this.streamSupplier = p_215504_;
      this.metadataSupplier = p_215505_;
   }

   public Resource(String p_215500_, Resource.IoSupplier<InputStream> p_215501_) {
      this.packId = p_215500_;
      this.streamSupplier = p_215501_;
      this.metadataSupplier = () -> {
         return ResourceMetadata.EMPTY;
      };
      this.cachedMetadata = ResourceMetadata.EMPTY;
   }

   public String sourcePackId() {
      return this.packId;
   }

   public InputStream open() throws IOException {
      return this.streamSupplier.get();
   }

   public BufferedReader openAsReader() throws IOException {
      return new BufferedReader(new InputStreamReader(this.open(), StandardCharsets.UTF_8));
   }

   public ResourceMetadata metadata() throws IOException {
      if (this.cachedMetadata == null) {
         this.cachedMetadata = this.metadataSupplier.get();
      }

      return this.cachedMetadata;
   }

   @FunctionalInterface
   public interface IoSupplier<T> {
      T get() throws IOException;
   }
}