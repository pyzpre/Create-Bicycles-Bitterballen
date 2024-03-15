package net.minecraft.data.tags;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.FlatLevelGeneratorPresetTags;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPresets;

public class FlatLevelGeneratorPresetTagsProvider extends TagsProvider<FlatLevelGeneratorPreset> {
   /** @deprecated Forge: Use the {@link #FlatLevelGeneratorPresetTagsProvider(DataGenerator, String, net.minecraftforge.common.data.ExistingFileHelper) mod id variant} */
   @Deprecated
   public FlatLevelGeneratorPresetTagsProvider(DataGenerator p_236423_) {
      super(p_236423_, BuiltinRegistries.FLAT_LEVEL_GENERATOR_PRESET);
   }
   public FlatLevelGeneratorPresetTagsProvider(DataGenerator p_236423_, String modId, @org.jetbrains.annotations.Nullable net.minecraftforge.common.data.ExistingFileHelper existingFileHelper) {
      super(p_236423_, BuiltinRegistries.FLAT_LEVEL_GENERATOR_PRESET, modId, existingFileHelper);
   }

   protected void addTags() {
      this.tag(FlatLevelGeneratorPresetTags.VISIBLE).add(FlatLevelGeneratorPresets.CLASSIC_FLAT).add(FlatLevelGeneratorPresets.TUNNELERS_DREAM).add(FlatLevelGeneratorPresets.WATER_WORLD).add(FlatLevelGeneratorPresets.OVERWORLD).add(FlatLevelGeneratorPresets.SNOWY_KINGDOM).add(FlatLevelGeneratorPresets.BOTTOMLESS_PIT).add(FlatLevelGeneratorPresets.DESERT).add(FlatLevelGeneratorPresets.REDSTONE_READY).add(FlatLevelGeneratorPresets.THE_VOID);
   }
}
