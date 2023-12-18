package net.minecraft.data;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.DimensionTypes;
import net.minecraft.data.worldgen.NoiseData;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.data.worldgen.StructureSets;
import net.minecraft.data.worldgen.Structures;
import net.minecraft.data.worldgen.biome.Biomes;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPresets;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.slf4j.Logger;

public class BuiltinRegistries {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Map<ResourceLocation, Supplier<? extends Holder<?>>> LOADERS = Maps.newLinkedHashMap();
   private static final WritableRegistry<WritableRegistry<?>> WRITABLE_REGISTRY = new MappedRegistry<>(ResourceKey.createRegistryKey(new ResourceLocation("root")), Lifecycle.experimental(), (Function<WritableRegistry<?>, Holder.Reference<WritableRegistry<?>>>)null);
   public static final Registry<? extends Registry<?>> REGISTRY = WRITABLE_REGISTRY;
   public static final Registry<DimensionType> DIMENSION_TYPE = registerSimple(Registry.DIMENSION_TYPE_REGISTRY, DimensionTypes::bootstrap);
   public static final Registry<ConfiguredWorldCarver<?>> CONFIGURED_CARVER = registerSimple(Registry.CONFIGURED_CARVER_REGISTRY, (p_236013_) -> {
      return Carvers.CAVE;
   });
   public static final Registry<ConfiguredFeature<?, ?>> CONFIGURED_FEATURE = registerSimple(Registry.CONFIGURED_FEATURE_REGISTRY, (RegistryBootstrap)FeatureUtils::bootstrap);
   public static final Registry<PlacedFeature> PLACED_FEATURE = registerSimple(Registry.PLACED_FEATURE_REGISTRY, PlacementUtils::bootstrap);
   public static final Registry<Structure> STRUCTURES = registerSimple(Registry.STRUCTURE_REGISTRY, Structures::bootstrap);
   public static final Registry<StructureSet> STRUCTURE_SETS = registerSimple(Registry.STRUCTURE_SET_REGISTRY, StructureSets::bootstrap);
   public static final Registry<StructureProcessorList> PROCESSOR_LIST = registerSimple(Registry.PROCESSOR_LIST_REGISTRY, (p_236008_) -> {
      return ProcessorLists.ZOMBIE_PLAINS;
   });
   public static final Registry<StructureTemplatePool> TEMPLATE_POOL = registerSimple(Registry.TEMPLATE_POOL_REGISTRY, Pools::bootstrap);
   @Deprecated public static final Registry<Biome> BIOME = forge(Registry.BIOME_REGISTRY, Biomes::bootstrap);
   public static final Registry<NormalNoise.NoiseParameters> NOISE = registerSimple(Registry.NOISE_REGISTRY, NoiseData::bootstrap);
   public static final Registry<DensityFunction> DENSITY_FUNCTION = registerSimple(Registry.DENSITY_FUNCTION_REGISTRY, NoiseRouterData::bootstrap);
   public static final Registry<NoiseGeneratorSettings> NOISE_GENERATOR_SETTINGS = registerSimple(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, NoiseGeneratorSettings::bootstrap);
   public static final Registry<WorldPreset> WORLD_PRESET = registerSimple(Registry.WORLD_PRESET_REGISTRY, WorldPresets::bootstrap);
   public static final Registry<FlatLevelGeneratorPreset> FLAT_LEVEL_GENERATOR_PRESET = registerSimple(Registry.FLAT_LEVEL_GENERATOR_PRESET_REGISTRY, FlatLevelGeneratorPresets::bootstrap);
   public static final Registry<ChatType> CHAT_TYPE = registerSimple(Registry.CHAT_TYPE_REGISTRY, ChatType::bootstrap);
   public static final RegistryAccess ACCESS;

   private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> p_236002_, BuiltinRegistries.RegistryBootstrap<T> p_236003_) {
      return registerSimple(p_236002_, Lifecycle.stable(), p_236003_);
   }

   private static <T> Registry<T> forge(ResourceKey<? extends Registry<T>> key, BuiltinRegistries.RegistryBootstrap<T> bootstrap) {
      return internalRegister(key, net.minecraftforge.registries.GameData.getWrapper(key, Lifecycle.stable()), bootstrap, Lifecycle.stable());
   }

   private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> p_235993_, Lifecycle p_235994_, BuiltinRegistries.RegistryBootstrap<T> p_235995_) {
      return internalRegister(p_235993_, new MappedRegistry<>(p_235993_, p_235994_, (Function<T, Holder.Reference<T>>)null), p_235995_, p_235994_);
   }

   private static <T, R extends WritableRegistry<T>> R internalRegister(ResourceKey<? extends Registry<T>> p_235997_, R p_235998_, BuiltinRegistries.RegistryBootstrap<T> p_235999_, Lifecycle p_236000_) {
      ResourceLocation resourcelocation = p_235997_.location();
      LOADERS.put(resourcelocation, () -> {
         return p_235999_.run(p_235998_);
      });
      WRITABLE_REGISTRY.register((ResourceKey)p_235997_, p_235998_, p_236000_);
      return p_235998_;
   }

   public static <V extends T, T> Holder<V> registerExact(Registry<T> p_206381_, String p_206382_, V p_206383_) {
      return register((Registry<V>)p_206381_, new ResourceLocation(p_206382_), p_206383_);
   }

   public static <T> Holder<T> register(Registry<T> p_206397_, String p_206398_, T p_206399_) {
      return register(p_206397_, new ResourceLocation(p_206398_), p_206399_);
   }

   public static <T> Holder<T> register(Registry<T> p_206389_, ResourceLocation p_206390_, T p_206391_) {
      return register(p_206389_, ResourceKey.create(p_206389_.key(), p_206390_), p_206391_);
   }

   public static <T> Holder<T> register(Registry<T> p_206385_, ResourceKey<T> p_206386_, T p_206387_) {
      return ((WritableRegistry)p_206385_).register(p_206386_, p_206387_, Lifecycle.stable());
   }

   public static void bootstrap() {
   }

   static {
      LOADERS.forEach((p_236005_, p_236006_) -> {
         if (!p_236006_.get().isBound()) {
            LOGGER.error("Unable to bootstrap registry '{}'", (Object)p_236005_);
         }

      });
      Registry.checkRegistry(WRITABLE_REGISTRY);
      ACCESS = RegistryAccess.fromRegistryOfRegistries(REGISTRY);
   }

   @FunctionalInterface
   interface RegistryBootstrap<T> {
      Holder<? extends T> run(Registry<T> p_236015_);
   }
}
