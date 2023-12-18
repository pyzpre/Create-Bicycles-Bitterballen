package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.levelgen.DensityFunction;

public class TheEndBiomeSource extends BiomeSource {
   public static final Codec<TheEndBiomeSource> CODEC = RecordCodecBuilder.create((p_220686_) -> {
      return p_220686_.group(RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter((p_151890_) -> {
         return null;
      })).apply(p_220686_, p_220686_.stable(TheEndBiomeSource::new));
   });
   private final Holder<Biome> end;
   private final Holder<Biome> highlands;
   private final Holder<Biome> midlands;
   private final Holder<Biome> islands;
   private final Holder<Biome> barrens;

   public TheEndBiomeSource(Registry<Biome> p_220684_) {
      this(p_220684_.getOrCreateHolderOrThrow(Biomes.THE_END), p_220684_.getOrCreateHolderOrThrow(Biomes.END_HIGHLANDS), p_220684_.getOrCreateHolderOrThrow(Biomes.END_MIDLANDS), p_220684_.getOrCreateHolderOrThrow(Biomes.SMALL_END_ISLANDS), p_220684_.getOrCreateHolderOrThrow(Biomes.END_BARRENS));
   }

   private TheEndBiomeSource(Holder<Biome> p_220678_, Holder<Biome> p_220679_, Holder<Biome> p_220680_, Holder<Biome> p_220681_, Holder<Biome> p_220682_) {
      super(ImmutableList.of(p_220678_, p_220679_, p_220680_, p_220681_, p_220682_));
      this.end = p_220678_;
      this.highlands = p_220679_;
      this.midlands = p_220680_;
      this.islands = p_220681_;
      this.barrens = p_220682_;
   }

   protected Codec<? extends BiomeSource> codec() {
      return CODEC;
   }

   public Holder<Biome> getNoiseBiome(int p_204292_, int p_204293_, int p_204294_, Climate.Sampler p_204295_) {
      int i = QuartPos.toBlock(p_204292_);
      int j = QuartPos.toBlock(p_204293_);
      int k = QuartPos.toBlock(p_204294_);
      int l = SectionPos.blockToSectionCoord(i);
      int i1 = SectionPos.blockToSectionCoord(k);
      if ((long)l * (long)l + (long)i1 * (long)i1 <= 4096L) {
         return this.end;
      } else {
         int j1 = (SectionPos.blockToSectionCoord(i) * 2 + 1) * 8;
         int k1 = (SectionPos.blockToSectionCoord(k) * 2 + 1) * 8;
         double d0 = p_204295_.erosion().compute(new DensityFunction.SinglePointContext(j1, j, k1));
         if (d0 > 0.25D) {
            return this.highlands;
         } else if (d0 >= -0.0625D) {
            return this.midlands;
         } else {
            return d0 < -0.21875D ? this.islands : this.barrens;
         }
      }
   }
}