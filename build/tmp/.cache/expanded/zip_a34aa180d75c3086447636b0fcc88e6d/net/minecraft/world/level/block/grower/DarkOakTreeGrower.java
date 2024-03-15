package net.minecraft.world.level.block.grower;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class DarkOakTreeGrower extends AbstractMegaTreeGrower {
   @Nullable
   protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource p_222924_, boolean p_222925_) {
      return null;
   }

   @Nullable
   protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource p_222922_) {
      return TreeFeatures.DARK_OAK;
   }
}