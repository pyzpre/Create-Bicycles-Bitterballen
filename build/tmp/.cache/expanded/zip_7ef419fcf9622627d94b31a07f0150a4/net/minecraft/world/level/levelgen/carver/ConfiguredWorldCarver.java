package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;

public record ConfiguredWorldCarver<WC extends CarverConfiguration>(WorldCarver<WC> worldCarver, WC config) {
   public static final Codec<ConfiguredWorldCarver<?>> DIRECT_CODEC = Registry.CARVER.byNameCodec().dispatch((p_64867_) -> {
      return p_64867_.worldCarver;
   }, WorldCarver::configuredCodec);
   public static final Codec<Holder<ConfiguredWorldCarver<?>>> CODEC = RegistryFileCodec.create(Registry.CONFIGURED_CARVER_REGISTRY, DIRECT_CODEC);
   public static final Codec<HolderSet<ConfiguredWorldCarver<?>>> LIST_CODEC = RegistryCodecs.homogeneousList(Registry.CONFIGURED_CARVER_REGISTRY, DIRECT_CODEC);

   public boolean isStartChunk(RandomSource p_224897_) {
      return this.worldCarver.isStartChunk(this.config, p_224897_);
   }

   public boolean carve(CarvingContext p_224899_, ChunkAccess p_224900_, Function<BlockPos, Holder<Biome>> p_224901_, RandomSource p_224902_, Aquifer p_224903_, ChunkPos p_224904_, CarvingMask p_224905_) {
      return SharedConstants.debugVoidTerrain(p_224900_.getPos()) ? false : this.worldCarver.carve(p_224899_, this.config, p_224900_, p_224901_, p_224902_, p_224903_, p_224904_, p_224905_);
   }
}