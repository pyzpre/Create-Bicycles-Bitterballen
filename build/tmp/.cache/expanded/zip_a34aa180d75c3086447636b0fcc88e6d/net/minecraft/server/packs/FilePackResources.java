package net.minecraft.server.packs;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class FilePackResources extends AbstractPackResources {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Splitter SPLITTER = Splitter.on('/').omitEmptyStrings().limit(3);
   @Nullable
   private ZipFile zipFile;

   public FilePackResources(File p_10236_) {
      super(p_10236_);
   }

   private ZipFile getOrCreateZipFile() throws IOException {
      if (this.zipFile == null) {
         this.zipFile = new ZipFile(this.file);
      }

      return this.zipFile;
   }

   protected InputStream getResource(String p_10246_) throws IOException {
      ZipFile zipfile = this.getOrCreateZipFile();
      ZipEntry zipentry = zipfile.getEntry(p_10246_);
      if (zipentry == null) {
         throw new ResourcePackFileNotFoundException(this.file, p_10246_);
      } else {
         return zipfile.getInputStream(zipentry);
      }
   }

   public boolean hasResource(String p_10249_) {
      try {
         return this.getOrCreateZipFile().getEntry(p_10249_) != null;
      } catch (IOException ioexception) {
         return false;
      }
   }

   public Set<String> getNamespaces(PackType p_10238_) {
      ZipFile zipfile;
      try {
         zipfile = this.getOrCreateZipFile();
      } catch (IOException ioexception) {
         return Collections.emptySet();
      }

      Enumeration<? extends ZipEntry> enumeration = zipfile.entries();
      Set<String> set = Sets.newHashSet();

      while(enumeration.hasMoreElements()) {
         ZipEntry zipentry = enumeration.nextElement();
         String s = zipentry.getName();
         if (s.startsWith(p_10238_.getDirectory() + "/")) {
            List<String> list = Lists.newArrayList(SPLITTER.split(s));
            if (list.size() > 1) {
               String s1 = list.get(1);
               if (s1.equals(s1.toLowerCase(Locale.ROOT))) {
                  set.add(s1);
               } else {
                  this.logWarning(s1);
               }
            }
         }
      }

      return set;
   }

   protected void finalize() throws Throwable {
      this.close();
      super.finalize();
   }

   public void close() {
      if (this.zipFile != null) {
         IOUtils.closeQuietly((Closeable)this.zipFile);
         this.zipFile = null;
      }

   }

   public Collection<ResourceLocation> getResources(PackType p_215324_, String p_215325_, String p_215326_, Predicate<ResourceLocation> p_215327_) {
      ZipFile zipfile;
      try {
         zipfile = this.getOrCreateZipFile();
      } catch (IOException ioexception) {
         return Collections.emptySet();
      }

      Enumeration<? extends ZipEntry> enumeration = zipfile.entries();
      List<ResourceLocation> list = Lists.newArrayList();
      String s = p_215324_.getDirectory() + "/" + p_215325_ + "/";
      String s1 = s + p_215326_ + "/";

      while(enumeration.hasMoreElements()) {
         ZipEntry zipentry = enumeration.nextElement();
         if (!zipentry.isDirectory()) {
            String s2 = zipentry.getName();
            if (!s2.endsWith(".mcmeta") && s2.startsWith(s1)) {
               String s3 = s2.substring(s.length());
               ResourceLocation resourcelocation = ResourceLocation.tryBuild(p_215325_, s3);
               if (resourcelocation == null) {
                  LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", p_215325_, s3);
               } else if (p_215327_.test(resourcelocation)) {
                  list.add(resourcelocation);
               }
            }
         }
      }

      return list;
   }
}