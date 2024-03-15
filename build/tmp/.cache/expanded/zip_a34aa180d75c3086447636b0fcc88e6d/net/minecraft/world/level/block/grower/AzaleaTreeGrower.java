package net.minecraft.world.level.block.grower;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class AzaleaTreeGrower extends AbstractTreeGrower {
   @Nullable
   protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource p_222916_, boolean p_222917_) {
      return TreeFeatures.AZALEA_TREE;
   }
}