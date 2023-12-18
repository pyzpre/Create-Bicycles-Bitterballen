package net.minecraft.data.structures;

import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class SnbtToNbt implements DataProvider {
   @Nullable
   private static final Path DUMP_SNBT_TO = null;
   private static final Logger LOGGER = LogUtils.getLogger();
   private final DataGenerator generator;
   private final List<SnbtToNbt.Filter> filters = Lists.newArrayList();

   public SnbtToNbt(DataGenerator p_126448_) {
      this.generator = p_126448_;
   }

   public SnbtToNbt addFilter(SnbtToNbt.Filter p_126476_) {
      this.filters.add(p_126476_);
      return this;
   }

   private CompoundTag applyFilters(String p_126461_, CompoundTag p_126462_) {
      CompoundTag compoundtag = p_126462_;

      for(SnbtToNbt.Filter snbttonbt$filter : this.filters) {
         compoundtag = snbttonbt$filter.apply(p_126461_, compoundtag);
      }

      return compoundtag;
   }

   public void run(CachedOutput p_236392_) throws IOException {
      Path path = this.generator.getOutputFolder();
      List<CompletableFuture<SnbtToNbt.TaskResult>> list = Lists.newArrayList();

      for(Path path1 : this.generator.getInputFolders()) {
         Files.walk(path1).filter((p_126464_) -> {
            return p_126464_.toString().endsWith(".snbt");
         }).forEach((p_126474_) -> {
            list.add(CompletableFuture.supplyAsync(() -> {
               return this.readStructure(p_126474_, this.getName(path1, p_126474_));
            }, Util.backgroundExecutor()));
         });
      }

      boolean flag = false;

      for(CompletableFuture<SnbtToNbt.TaskResult> completablefuture : list) {
         try {
            this.storeStructureIfChanged(p_236392_, completablefuture.get(), path);
         } catch (Exception exception) {
            LOGGER.error("Failed to process structure", (Throwable)exception);
            flag = true;
         }
      }

      if (flag) {
         throw new IllegalStateException("Failed to convert all structures, aborting");
      }
   }

   public String getName() {
      return "SNBT -> NBT";
   }

   private String getName(Path p_126469_, Path p_126470_) {
      String s = p_126469_.relativize(p_126470_).toString().replaceAll("\\\\", "/");
      return s.substring(0, s.length() - ".snbt".length());
   }

   private SnbtToNbt.TaskResult readStructure(Path p_126466_, String p_126467_) {
      try {
         BufferedReader bufferedreader = Files.newBufferedReader(p_126466_);

         SnbtToNbt.TaskResult snbttonbt$taskresult;
         try {
            String s = IOUtils.toString((Reader)bufferedreader);
            CompoundTag compoundtag = this.applyFilters(p_126467_, NbtUtils.snbtToStructure(s));
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
            HashingOutputStream hashingoutputstream = new HashingOutputStream(Hashing.sha1(), bytearrayoutputstream);
            NbtIo.writeCompressed(compoundtag, hashingoutputstream);
            byte[] abyte = bytearrayoutputstream.toByteArray();
            HashCode hashcode = hashingoutputstream.hash();
            String s1;
            if (DUMP_SNBT_TO != null) {
               s1 = NbtUtils.structureToSnbt(compoundtag);
            } else {
               s1 = null;
            }

            snbttonbt$taskresult = new SnbtToNbt.TaskResult(p_126467_, abyte, s1, hashcode);
         } catch (Throwable throwable1) {
            if (bufferedreader != null) {
               try {
                  bufferedreader.close();
               } catch (Throwable throwable) {
                  throwable1.addSuppressed(throwable);
               }
            }

            throw throwable1;
         }

         if (bufferedreader != null) {
            bufferedreader.close();
         }

         return snbttonbt$taskresult;
      } catch (Throwable throwable2) {
         throw new SnbtToNbt.StructureConversionException(p_126466_, throwable2);
      }
   }

   private void storeStructureIfChanged(CachedOutput p_236394_, SnbtToNbt.TaskResult p_236395_, Path p_236396_) {
      if (p_236395_.snbtPayload != null) {
         Path path = DUMP_SNBT_TO.resolve(p_236395_.name + ".snbt");

         try {
            NbtToSnbt.writeSnbt(CachedOutput.NO_CACHE, path, p_236395_.snbtPayload);
         } catch (IOException ioexception1) {
            LOGGER.error("Couldn't write structure SNBT {} at {}", p_236395_.name, path, ioexception1);
         }
      }

      Path path1 = p_236396_.resolve(p_236395_.name + ".nbt");

      try {
         p_236394_.writeIfNeeded(path1, p_236395_.payload, p_236395_.hash);
      } catch (IOException ioexception) {
         LOGGER.error("Couldn't write structure {} at {}", p_236395_.name, path1, ioexception);
      }

   }

   @FunctionalInterface
   public interface Filter {
      CompoundTag apply(String p_126480_, CompoundTag p_126481_);
   }

   static class StructureConversionException extends RuntimeException {
      public StructureConversionException(Path p_176820_, Throwable p_176821_) {
         super(p_176820_.toAbsolutePath().toString(), p_176821_);
      }
   }

   static record TaskResult(String name, byte[] payload, @Nullable String snbtPayload, HashCode hash) {
   }
}