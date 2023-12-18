package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.apache.commons.lang3.mutable.MutableObject;

public class NoiseBasedChunkGenerator extends ChunkGenerator {
   public static final Codec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create((p_224323_) -> {
      return commonCodec(p_224323_).and(p_224323_.group(RegistryOps.retrieveRegistry(Registry.NOISE_REGISTRY).forGetter((p_188716_) -> {
         return p_188716_.noises;
      }), BiomeSource.CODEC.fieldOf("biome_source").forGetter((p_188711_) -> {
         return p_188711_.biomeSource;
      }), NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter((p_224278_) -> {
         return p_224278_.settings;
      }))).apply(p_224323_, p_224323_.stable(NoiseBasedChunkGenerator::new));
   });
   private static final BlockState AIR = Blocks.AIR.defaultBlockState();
   protected final BlockState defaultBlock;
   private final Registry<NormalNoise.NoiseParameters> noises;
   protected final Holder<NoiseGeneratorSettings> settings;
   private final Aquifer.FluidPicker globalFluidPicker;

   public NoiseBasedChunkGenerator(Registry<StructureSet> p_224206_, Registry<NormalNoise.NoiseParameters> p_224207_, BiomeSource p_224208_, Holder<NoiseGeneratorSettings> p_224209_) {
      super(p_224206_, Optional.empty(), p_224208_);
      this.noises = p_224207_;
      this.settings = p_224209_;
      NoiseGeneratorSettings noisegeneratorsettings = this.settings.value();
      this.defaultBlock = noisegeneratorsettings.defaultBlock();
      Aquifer.FluidStatus aquifer$fluidstatus = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
      int i = noisegeneratorsettings.seaLevel();
      Aquifer.FluidStatus aquifer$fluidstatus1 = new Aquifer.FluidStatus(i, noisegeneratorsettings.defaultFluid());
      Aquifer.FluidStatus aquifer$fluidstatus2 = new Aquifer.FluidStatus(DimensionType.MIN_Y * 2, Blocks.AIR.defaultBlockState());
      this.globalFluidPicker = (p_224274_, p_224275_, p_224276_) -> {
         return p_224275_ < Math.min(-54, i) ? aquifer$fluidstatus : aquifer$fluidstatus1;
      };
   }

   public CompletableFuture<ChunkAccess> createBiomes(Registry<Biome> p_224297_, Executor p_224298_, RandomState p_224299_, Blender p_224300_, StructureManager p_224301_, ChunkAccess p_224302_) {
      return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
         this.doCreateBiomes(p_224300_, p_224299_, p_224301_, p_224302_);
         return p_224302_;
      }), Util.backgroundExecutor());
   }

   private void doCreateBiomes(Blender p_224292_, RandomState p_224293_, StructureManager p_224294_, ChunkAccess p_224295_) {
      NoiseChunk noisechunk = p_224295_.getOrCreateNoiseChunk((p_224340_) -> {
         return this.createNoiseChunk(p_224340_, p_224294_, p_224292_, p_224293_);
      });
      BiomeResolver biomeresolver = BelowZeroRetrogen.getBiomeResolver(p_224292_.getBiomeResolver(this.biomeSource), p_224295_);
      p_224295_.fillBiomesFromNoise(biomeresolver, noisechunk.cachedClimateSampler(p_224293_.router(), this.settings.value().spawnTarget()));
   }

   private NoiseChunk createNoiseChunk(ChunkAccess p_224257_, StructureManager p_224258_, Blender p_224259_, RandomState p_224260_) {
      return NoiseChunk.forChunk(p_224257_, p_224260_, Beardifier.forStructuresInChunk(p_224258_, p_224257_.getPos()), this.settings.value(), this.globalFluidPicker, p_224259_);
   }

   protected Codec<? extends ChunkGenerator> codec() {
      return CODEC;
   }

   public Holder<NoiseGeneratorSettings> generatorSettings() {
      return this.settings;
   }

   public boolean stable(ResourceKey<NoiseGeneratorSettings> p_224222_) {
      return this.settings.is(p_224222_);
   }

   public int getBaseHeight(int p_224216_, int p_224217_, Heightmap.Types p_224218_, LevelHeightAccessor p_224219_, RandomState p_224220_) {
      return this.iterateNoiseColumn(p_224219_, p_224220_, p_224216_, p_224217_, (MutableObject<NoiseColumn>)null, p_224218_.isOpaque()).orElse(p_224219_.getMinBuildHeight());
   }

   public NoiseColumn getBaseColumn(int p_224211_, int p_224212_, LevelHeightAccessor p_224213_, RandomState p_224214_) {
      MutableObject<NoiseColumn> mutableobject = new MutableObject<>();
      this.iterateNoiseColumn(p_224213_, p_224214_, p_224211_, p_224212_, mutableobject, (Predicate<BlockState>)null);
      return mutableobject.getValue();
   }

   public void addDebugScreenInfo(List<String> p_224304_, RandomState p_224305_, BlockPos p_224306_) {
      DecimalFormat decimalformat = new DecimalFormat("0.000");
      NoiseRouter noiserouter = p_224305_.router();
      DensityFunction.SinglePointContext densityfunction$singlepointcontext = new DensityFunction.SinglePointContext(p_224306_.getX(), p_224306_.getY(), p_224306_.getZ());
      double d0 = noiserouter.ridges().compute(densityfunction$singlepointcontext);
      p_224304_.add("NoiseRouter T: " + decimalformat.format(noiserouter.temperature().compute(densityfunction$singlepointcontext)) + " V: " + decimalformat.format(noiserouter.vegetation().compute(densityfunction$singlepointcontext)) + " C: " + decimalformat.format(noiserouter.continents().compute(densityfunction$singlepointcontext)) + " E: " + decimalformat.format(noiserouter.erosion().compute(densityfunction$singlepointcontext)) + " D: " + decimalformat.format(noiserouter.depth().compute(densityfunction$singlepointcontext)) + " W: " + decimalformat.format(d0) + " PV: " + decimalformat.format((double)NoiseRouterData.peaksAndValleys((float)d0)) + " AS: " + decimalformat.format(noiserouter.initialDensityWithoutJaggedness().compute(densityfunction$singlepointcontext)) + " N: " + decimalformat.format(noiserouter.finalDensity().compute(densityfunction$singlepointcontext)));
   }

   protected OptionalInt iterateNoiseColumn(LevelHeightAccessor p_224240_, RandomState p_224241_, int p_224242_, int p_224243_, @Nullable MutableObject<NoiseColumn> p_224244_, @Nullable Predicate<BlockState> p_224245_) {
      NoiseSettings noisesettings = this.settings.value().noiseSettings().clampToHeightAccessor(p_224240_);
      int i = noisesettings.getCellHeight();
      int j = noisesettings.minY();
      int k = Mth.intFloorDiv(j, i);
      int l = Mth.intFloorDiv(noisesettings.height(), i);
      if (l <= 0) {
         return OptionalInt.empty();
      } else {
         BlockState[] ablockstate;
         if (p_224244_ == null) {
            ablockstate = null;
         } else {
            ablockstate = new BlockState[noisesettings.height()];
            p_224244_.setValue(new NoiseColumn(j, ablockstate));
         }

         int i1 = noisesettings.getCellWidth();
         int j1 = Math.floorDiv(p_224242_, i1);
         int k1 = Math.floorDiv(p_224243_, i1);
         int l1 = Math.floorMod(p_224242_, i1);
         int i2 = Math.floorMod(p_224243_, i1);
         int j2 = j1 * i1;
         int k2 = k1 * i1;
         double d0 = (double)l1 / (double)i1;
         double d1 = (double)i2 / (double)i1;
         NoiseChunk noisechunk = new NoiseChunk(1, p_224241_, j2, k2, noisesettings, DensityFunctions.BeardifierMarker.INSTANCE, this.settings.value(), this.globalFluidPicker, Blender.empty());
         noisechunk.initializeForFirstCellX();
         noisechunk.advanceCellX(0);

         for(int l2 = l - 1; l2 >= 0; --l2) {
            noisechunk.selectCellYZ(l2, 0);

            for(int i3 = i - 1; i3 >= 0; --i3) {
               int j3 = (k + l2) * i + i3;
               double d2 = (double)i3 / (double)i;
               noisechunk.updateForY(j3, d2);
               noisechunk.updateForX(p_224242_, d0);
               noisechunk.updateForZ(p_224243_, d1);
               BlockState blockstate = noisechunk.getInterpolatedState();
               BlockState blockstate1 = blockstate == null ? this.defaultBlock : blockstate;
               if (ablockstate != null) {
                  int k3 = l2 * i + i3;
                  ablockstate[k3] = blockstate1;
               }

               if (p_224245_ != null && p_224245_.test(blockstate1)) {
                  noisechunk.stopInterpolation();
                  return OptionalInt.of(j3 + 1);
               }
            }
         }

         noisechunk.stopInterpolation();
         return OptionalInt.empty();
      }
   }

   public void buildSurface(WorldGenRegion p_224232_, StructureManager p_224233_, RandomState p_224234_, ChunkAccess p_224235_) {
      if (!SharedConstants.debugVoidTerrain(p_224235_.getPos())) {
         WorldGenerationContext worldgenerationcontext = new WorldGenerationContext(this, p_224232_);
         this.buildSurface(p_224235_, worldgenerationcontext, p_224234_, p_224233_, p_224232_.getBiomeManager(), p_224232_.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), Blender.of(p_224232_));
      }
   }

   @VisibleForTesting
   public void buildSurface(ChunkAccess p_224262_, WorldGenerationContext p_224263_, RandomState p_224264_, StructureManager p_224265_, BiomeManager p_224266_, Registry<Biome> p_224267_, Blender p_224268_) {
      NoiseChunk noisechunk = p_224262_.getOrCreateNoiseChunk((p_224321_) -> {
         return this.createNoiseChunk(p_224321_, p_224265_, p_224268_, p_224264_);
      });
      NoiseGeneratorSettings noisegeneratorsettings = this.settings.value();
      p_224264_.surfaceSystem().buildSurface(p_224264_, p_224266_, p_224267_, noisegeneratorsettings.useLegacyRandomSource(), p_224263_, p_224262_, noisechunk, noisegeneratorsettings.surfaceRule());
   }

   public void applyCarvers(WorldGenRegion p_224224_, long p_224225_, RandomState p_224226_, BiomeManager p_224227_, StructureManager p_224228_, ChunkAccess p_224229_, GenerationStep.Carving p_224230_) {
      BiomeManager biomemanager = p_224227_.withDifferentSource((p_224281_, p_224282_, p_224283_) -> {
         return this.biomeSource.getNoiseBiome(p_224281_, p_224282_, p_224283_, p_224226_.sampler());
      });
      WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
      int i = 8;
      ChunkPos chunkpos = p_224229_.getPos();
      NoiseChunk noisechunk = p_224229_.getOrCreateNoiseChunk((p_224250_) -> {
         return this.createNoiseChunk(p_224250_, p_224228_, Blender.of(p_224224_), p_224226_);
      });
      Aquifer aquifer = noisechunk.aquifer();
      CarvingContext carvingcontext = new CarvingContext(this, p_224224_.registryAccess(), p_224229_.getHeightAccessorForGeneration(), noisechunk, p_224226_, this.settings.value().surfaceRule());
      CarvingMask carvingmask = ((ProtoChunk)p_224229_).getOrCreateCarvingMask(p_224230_);

      for(int j = -8; j <= 8; ++j) {
         for(int k = -8; k <= 8; ++k) {
            ChunkPos chunkpos1 = new ChunkPos(chunkpos.x + j, chunkpos.z + k);
            ChunkAccess chunkaccess = p_224224_.getChunk(chunkpos1.x, chunkpos1.z);
            BiomeGenerationSettings biomegenerationsettings = chunkaccess.carverBiome(() -> {
               return this.getBiomeGenerationSettings(this.biomeSource.getNoiseBiome(QuartPos.fromBlock(chunkpos1.getMinBlockX()), 0, QuartPos.fromBlock(chunkpos1.getMinBlockZ()), p_224226_.sampler()));
            });
            Iterable<Holder<ConfiguredWorldCarver<?>>> iterable = biomegenerationsettings.getCarvers(p_224230_);
            int l = 0;

            for(Holder<ConfiguredWorldCarver<?>> holder : iterable) {
               ConfiguredWorldCarver<?> configuredworldcarver = holder.value();
               worldgenrandom.setLargeFeatureSeed(p_224225_ + (long)l, chunkpos1.x, chunkpos1.z);
               if (configuredworldcarver.isStartChunk(worldgenrandom)) {
                  configuredworldcarver.carve(carvingcontext, p_224229_, biomemanager::getBiome, worldgenrandom, aquifer, chunkpos1, carvingmask);
               }

               ++l;
            }
         }
      }

   }

   public CompletableFuture<ChunkAccess> fillFromNoise(Executor p_224312_, Blender p_224313_, RandomState p_224314_, StructureManager p_224315_, ChunkAccess p_224316_) {
      NoiseSettings noisesettings = this.settings.value().noiseSettings().clampToHeightAccessor(p_224316_.getHeightAccessorForGeneration());
      int i = noisesettings.minY();
      int j = Mth.intFloorDiv(i, noisesettings.getCellHeight());
      int k = Mth.intFloorDiv(noisesettings.height(), noisesettings.getCellHeight());
      if (k <= 0) {
         return CompletableFuture.completedFuture(p_224316_);
      } else {
         int l = p_224316_.getSectionIndex(k * noisesettings.getCellHeight() - 1 + i);
         int i1 = p_224316_.getSectionIndex(i);
         Set<LevelChunkSection> set = Sets.newHashSet();

         for(int j1 = l; j1 >= i1; --j1) {
            LevelChunkSection levelchunksection = p_224316_.getSection(j1);
            levelchunksection.acquire();
            set.add(levelchunksection);
         }

         return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("wgen_fill_noise", () -> {
            return this.doFill(p_224313_, p_224315_, p_224314_, p_224316_, j, k);
         }), Util.backgroundExecutor()).whenCompleteAsync((p_224309_, p_224310_) -> {
            for(LevelChunkSection levelchunksection1 : set) {
               levelchunksection1.release();
            }

         }, p_224312_);
      }
   }

   private ChunkAccess doFill(Blender p_224285_, StructureManager p_224286_, RandomState p_224287_, ChunkAccess p_224288_, int p_224289_, int p_224290_) {
      NoiseChunk noisechunk = p_224288_.getOrCreateNoiseChunk((p_224255_) -> {
         return this.createNoiseChunk(p_224255_, p_224286_, p_224285_, p_224287_);
      });
      Heightmap heightmap = p_224288_.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
      Heightmap heightmap1 = p_224288_.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
      ChunkPos chunkpos = p_224288_.getPos();
      int i = chunkpos.getMinBlockX();
      int j = chunkpos.getMinBlockZ();
      Aquifer aquifer = noisechunk.aquifer();
      noisechunk.initializeForFirstCellX();
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      int k = noisechunk.cellWidth();
      int l = noisechunk.cellHeight();
      int i1 = 16 / k;
      int j1 = 16 / k;

      for(int k1 = 0; k1 < i1; ++k1) {
         noisechunk.advanceCellX(k1);

         for(int l1 = 0; l1 < j1; ++l1) {
            LevelChunkSection levelchunksection = p_224288_.getSection(p_224288_.getSectionsCount() - 1);

            for(int i2 = p_224290_ - 1; i2 >= 0; --i2) {
               noisechunk.selectCellYZ(i2, l1);

               for(int j2 = l - 1; j2 >= 0; --j2) {
                  int k2 = (p_224289_ + i2) * l + j2;
                  int l2 = k2 & 15;
                  int i3 = p_224288_.getSectionIndex(k2);
                  if (p_224288_.getSectionIndex(levelchunksection.bottomBlockY()) != i3) {
                     levelchunksection = p_224288_.getSection(i3);
                  }

                  double d0 = (double)j2 / (double)l;
                  noisechunk.updateForY(k2, d0);

                  for(int j3 = 0; j3 < k; ++j3) {
                     int k3 = i + k1 * k + j3;
                     int l3 = k3 & 15;
                     double d1 = (double)j3 / (double)k;
                     noisechunk.updateForX(k3, d1);

                     for(int i4 = 0; i4 < k; ++i4) {
                        int j4 = j + l1 * k + i4;
                        int k4 = j4 & 15;
                        double d2 = (double)i4 / (double)k;
                        noisechunk.updateForZ(j4, d2);
                        BlockState blockstate = noisechunk.getInterpolatedState();
                        if (blockstate == null) {
                           blockstate = this.defaultBlock;
                        }

                        blockstate = this.debugPreliminarySurfaceLevel(noisechunk, k3, k2, j4, blockstate);
                        if (blockstate != AIR && !SharedConstants.debugVoidTerrain(p_224288_.getPos())) {
                           if (blockstate.getLightEmission() != 0 && p_224288_ instanceof ProtoChunk) {
                              blockpos$mutableblockpos.set(k3, k2, j4);
                              ((ProtoChunk)p_224288_).addLight(blockpos$mutableblockpos);
                           }

                           levelchunksection.setBlockState(l3, l2, k4, blockstate, false);
                           heightmap.update(l3, k2, k4, blockstate);
                           heightmap1.update(l3, k2, k4, blockstate);
                           if (aquifer.shouldScheduleFluidUpdate() && !blockstate.getFluidState().isEmpty()) {
                              blockpos$mutableblockpos.set(k3, k2, j4);
                              p_224288_.markPosForPostprocessing(blockpos$mutableblockpos);
                           }
                        }
                     }
                  }
               }
            }
         }

         noisechunk.swapSlices();
      }

      noisechunk.stopInterpolation();
      return p_224288_;
   }

   private BlockState debugPreliminarySurfaceLevel(NoiseChunk p_198232_, int p_198233_, int p_198234_, int p_198235_, BlockState p_198236_) {
      return p_198236_;
   }

   public int getGenDepth() {
      return this.settings.value().noiseSettings().height();
   }

   public int getSeaLevel() {
      return this.settings.value().seaLevel();
   }

   public int getMinY() {
      return this.settings.value().noiseSettings().minY();
   }

   public void spawnOriginalMobs(WorldGenRegion p_64379_) {
      if (!this.settings.value().disableMobGeneration()) {
         ChunkPos chunkpos = p_64379_.getCenter();
         Holder<Biome> holder = p_64379_.getBiome(chunkpos.getWorldPosition().atY(p_64379_.getMaxBuildHeight() - 1));
         WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
         worldgenrandom.setDecorationSeed(p_64379_.getSeed(), chunkpos.getMinBlockX(), chunkpos.getMinBlockZ());
         NaturalSpawner.spawnMobsForChunkGeneration(p_64379_, holder, chunkpos, worldgenrandom);
      }
   }
}