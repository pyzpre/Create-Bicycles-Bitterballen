package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class BlobFoliagePlacer extends FoliagePlacer {
   public static final Codec<BlobFoliagePlacer> CODEC = RecordCodecBuilder.create((p_68427_) -> {
      return blobParts(p_68427_).apply(p_68427_, BlobFoliagePlacer::new);
   });
   protected final int height;

   protected static <P extends BlobFoliagePlacer> Products.P3<RecordCodecBuilder.Mu<P>, IntProvider, IntProvider, Integer> blobParts(RecordCodecBuilder.Instance<P> p_68414_) {
      return foliagePlacerParts(p_68414_).and(Codec.intRange(0, 16).fieldOf("height").forGetter((p_68412_) -> {
         return p_68412_.height;
      }));
   }

   public BlobFoliagePlacer(IntProvider p_161356_, IntProvider p_161357_, int p_161358_) {
      super(p_161356_, p_161357_);
      this.height = p_161358_;
   }

   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.BLOB_FOLIAGE_PLACER;
   }

   protected void createFoliage(LevelSimulatedReader p_225520_, BiConsumer<BlockPos, BlockState> p_225521_, RandomSource p_225522_, TreeConfiguration p_225523_, int p_225524_, FoliagePlacer.FoliageAttachment p_225525_, int p_225526_, int p_225527_, int p_225528_) {
      for(int i = p_225528_; i >= p_225528_ - p_225526_; --i) {
         int j = Math.max(p_225527_ + p_225525_.radiusOffset() - 1 - i / 2, 0);
         this.placeLeavesRow(p_225520_, p_225521_, p_225522_, p_225523_, p_225525_.pos(), j, i, p_225525_.doubleTrunk());
      }

   }

   public int foliageHeight(RandomSource p_225516_, int p_225517_, TreeConfiguration p_225518_) {
      return this.height;
   }

   protected boolean shouldSkipLocation(RandomSource p_225509_, int p_225510_, int p_225511_, int p_225512_, int p_225513_, boolean p_225514_) {
      return p_225510_ == p_225513_ && p_225512_ == p_225513_ && (p_225509_.nextInt(2) == 0 || p_225511_ == 0);
   }
}