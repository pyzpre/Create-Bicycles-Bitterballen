package net.minecraft.client.gui.font.providers;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class LegacyUnicodeBitmapsProvider implements GlyphProvider {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int UNICODE_SHEETS = 256;
   private static final int CODEPOINTS_PER_SHEET = 256;
   private static final int TEXTURE_SIZE = 256;
   private static final byte NO_GLYPH = 0;
   private final ResourceManager resourceManager;
   private final byte[] sizes;
   private final String texturePattern;
   private final Map<ResourceLocation, NativeImage> textures = Maps.newHashMap();

   public LegacyUnicodeBitmapsProvider(ResourceManager p_95429_, byte[] p_95430_, String p_95431_) {
      this.resourceManager = p_95429_;
      this.sizes = p_95430_;
      this.texturePattern = p_95431_;

      for(int i = 0; i < 256; ++i) {
         int j = i * 256;
         ResourceLocation resourcelocation = this.getSheetLocation(j);

         try {
            InputStream inputstream = this.resourceManager.open(resourcelocation);

            label90: {
               try {
                  NativeImage nativeimage;
                  label104: {
                     nativeimage = NativeImage.read(NativeImage.Format.RGBA, inputstream);

                     try {
                        if (nativeimage.getWidth() == 256 && nativeimage.getHeight() == 256) {
                           int k = 0;

                           while(true) {
                              if (k >= 256) {
                                 break label104;
                              }

                              byte b0 = p_95430_[j + k];
                              if (b0 != 0 && getLeft(b0) > getRight(b0)) {
                                 p_95430_[j + k] = 0;
                              }

                              ++k;
                           }
                        }
                     } catch (Throwable throwable2) {
                        if (nativeimage != null) {
                           try {
                              nativeimage.close();
                           } catch (Throwable throwable1) {
                              throwable2.addSuppressed(throwable1);
                           }
                        }

                        throw throwable2;
                     }

                     if (nativeimage != null) {
                        nativeimage.close();
                     }
                     break label90;
                  }

                  if (nativeimage != null) {
                     nativeimage.close();
                  }
               } catch (Throwable throwable3) {
                  if (inputstream != null) {
                     try {
                        inputstream.close();
                     } catch (Throwable throwable) {
                        throwable3.addSuppressed(throwable);
                     }
                  }

                  throw throwable3;
               }

               if (inputstream != null) {
                  inputstream.close();
               }
               continue;
            }

            if (inputstream != null) {
               inputstream.close();
            }
         } catch (IOException ioexception) {
         }

         Arrays.fill(p_95430_, j, j + 256, (byte)0);
      }

   }

   public void close() {
      this.textures.values().forEach(NativeImage::close);
   }

   private ResourceLocation getSheetLocation(int p_95443_) {
      ResourceLocation resourcelocation = new ResourceLocation(String.format(Locale.ROOT, this.texturePattern, String.format(Locale.ROOT, "%02x", p_95443_ / 256)));
      return new ResourceLocation(resourcelocation.getNamespace(), "textures/" + resourcelocation.getPath());
   }

   @Nullable
   public GlyphInfo getGlyph(int p_232668_) {
      if (p_232668_ >= 0 && p_232668_ < this.sizes.length) {
         byte b0 = this.sizes[p_232668_];
         if (b0 != 0) {
            NativeImage nativeimage = this.textures.computeIfAbsent(this.getSheetLocation(p_232668_), this::loadTexture);
            if (nativeimage != null) {
               int i = getLeft(b0);
               return new LegacyUnicodeBitmapsProvider.Glyph(p_232668_ % 16 * 16 + i, (p_232668_ & 255) / 16 * 16, getRight(b0) - i, 16, nativeimage);
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public IntSet getSupportedGlyphs() {
      IntSet intset = new IntOpenHashSet();

      for(int i = 0; i < this.sizes.length; ++i) {
         if (this.sizes[i] != 0) {
            intset.add(i);
         }
      }

      return intset;
   }

   @Nullable
   private NativeImage loadTexture(ResourceLocation p_95438_) {
      try {
         InputStream inputstream = this.resourceManager.open(p_95438_);

         NativeImage nativeimage;
         try {
            nativeimage = NativeImage.read(NativeImage.Format.RGBA, inputstream);
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

         return nativeimage;
      } catch (IOException ioexception) {
         LOGGER.error("Couldn't load texture {}", p_95438_, ioexception);
         return null;
      }
   }

   private static int getLeft(byte p_95434_) {
      return p_95434_ >> 4 & 15;
   }

   private static int getRight(byte p_95441_) {
      return (p_95441_ & 15) + 1;
   }

   @OnlyIn(Dist.CLIENT)
   public static class Builder implements GlyphProviderBuilder {
      private final ResourceLocation metadata;
      private final String texturePattern;

      public Builder(ResourceLocation p_95448_, String p_95449_) {
         this.metadata = p_95448_;
         this.texturePattern = p_95449_;
      }

      public static GlyphProviderBuilder fromJson(JsonObject p_95453_) {
         return new LegacyUnicodeBitmapsProvider.Builder(new ResourceLocation(GsonHelper.getAsString(p_95453_, "sizes")), getTemplate(p_95453_));
      }

      private static String getTemplate(JsonObject p_182570_) {
         String s = GsonHelper.getAsString(p_182570_, "template");

         try {
            String.format(Locale.ROOT, s, "");
            return s;
         } catch (IllegalFormatException illegalformatexception) {
            throw new JsonParseException("Invalid legacy unicode template supplied, expected single '%s': " + s);
         }
      }

      @Nullable
      public GlyphProvider create(ResourceManager p_95451_) {
         try {
            InputStream inputstream = Minecraft.getInstance().getResourceManager().open(this.metadata);

            LegacyUnicodeBitmapsProvider legacyunicodebitmapsprovider;
            try {
               byte[] abyte = inputstream.readNBytes(65536);
               legacyunicodebitmapsprovider = new LegacyUnicodeBitmapsProvider(p_95451_, abyte, this.texturePattern);
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

            return legacyunicodebitmapsprovider;
         } catch (IOException ioexception) {
            LegacyUnicodeBitmapsProvider.LOGGER.error("Cannot load {}, unicode glyphs will not render correctly", (Object)this.metadata);
            return null;
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   static record Glyph(int sourceX, int sourceY, int width, int height, NativeImage source) implements GlyphInfo {
      public float getAdvance() {
         return (float)(this.width / 2 + 1);
      }

      public float getShadowOffset() {
         return 0.5F;
      }

      public float getBoldOffset() {
         return 0.5F;
      }

      public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> p_232670_) {
         return p_232670_.apply(new SheetGlyphInfo() {
            public float getOversample() {
               return 2.0F;
            }

            public int getPixelWidth() {
               return Glyph.this.width;
            }

            public int getPixelHeight() {
               return Glyph.this.height;
            }

            public void upload(int p_232685_, int p_232686_) {
               Glyph.this.source.upload(0, p_232685_, p_232686_, Glyph.this.sourceX, Glyph.this.sourceY, Glyph.this.width, Glyph.this.height, false, false);
            }

            public boolean isColored() {
               return Glyph.this.source.format().components() > 1;
            }
         });
      }
   }
}