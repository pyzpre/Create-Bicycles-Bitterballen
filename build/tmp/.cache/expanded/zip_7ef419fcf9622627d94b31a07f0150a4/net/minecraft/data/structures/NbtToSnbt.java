package net.minecraft.data.structures;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import org.slf4j.Logger;

public class NbtToSnbt implements DataProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final DataGenerator generator;

   public NbtToSnbt(DataGenerator p_126425_) {
      this.generator = p_126425_;
   }

   public void run(CachedOutput p_236376_) throws IOException {
      Path path = this.generator.getOutputFolder();

      for(Path path1 : this.generator.getInputFolders()) {
         Files.walk(path1).filter((p_126430_) -> {
            return p_126430_.toString().endsWith(".nbt");
         }).forEach((p_236390_) -> {
            convertStructure(p_236376_, p_236390_, this.getName(path1, p_236390_), path);
         });
      }

   }

   public String getName() {
      return "NBT to SNBT";
   }

   private String getName(Path p_126436_, Path p_126437_) {
      String s = p_126436_.relativize(p_126437_).toString().replaceAll("\\\\", "/");
      return s.substring(0, s.length() - ".nbt".length());
   }

   @Nullable
   public static Path convertStructure(CachedOutput p_236382_, Path p_236383_, String p_236384_, Path p_236385_) {
      try {
         InputStream inputstream = Files.newInputStream(p_236383_);

         Path path1;
         try {
            Path path = p_236385_.resolve(p_236384_ + ".snbt");
            writeSnbt(p_236382_, path, NbtUtils.structureToSnbt(NbtIo.readCompressed(inputstream)));
            LOGGER.info("Converted {} from NBT to SNBT", (Object)p_236384_);
            path1 = path;
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

         return path1;
      } catch (IOException ioexception) {
         LOGGER.error("Couldn't convert {} from NBT to SNBT at {}", p_236384_, p_236383_, ioexception);
         return null;
      }
   }

   public static void writeSnbt(CachedOutput p_236378_, Path p_236379_, String p_236380_) throws IOException {
      ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
      HashingOutputStream hashingoutputstream = new HashingOutputStream(Hashing.sha1(), bytearrayoutputstream);
      hashingoutputstream.write(p_236380_.getBytes(StandardCharsets.UTF_8));
      hashingoutputstream.write(10);
      p_236378_.writeIfNeeded(p_236379_, bytearrayoutputstream.toByteArray(), hashingoutputstream.hash());
   }
}