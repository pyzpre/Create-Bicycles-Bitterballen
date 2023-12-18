package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.material.Fluids;

public abstract class FoliagePlacer {
   public static final Codec<FoliagePlacer> CODEC = Registry.FOLIAGE_PLACER_TYPES.byNameCodec().dispatch(FoliagePlacer::type, FoliagePlacerType::codec);
   protected final IntProvider radius;
   protected final IntProvider offset;

   protected static <P extends FoliagePlacer> Products.P2<RecordCodecBuilder.Mu<P>, IntProvider, IntProvider> foliagePlacerParts(RecordCodecBuilder.Instance<P> p_68574_) {
      return p_68574_.group(IntProvider.codec(0, 16).fieldOf("radius").forGetter((p_161449_) -> {
         return p_161449_.radius;
      }), IntProvider.codec(0, 16).fieldOf("offset").forGetter((p_161447_) -> {
         return p_161447_.offset;
      }));
   }

   public FoliagePlacer(IntProvider p_161411_, IntProvider p_161412_) {
      this.radius = p_161411_;
      this.offset = p_161412_;
   }

   protected abstract FoliagePlacerType<?> type();

   public void createFoliage(LevelSimulatedReader p_225605_, BiConsumer<BlockPos, BlockState> p_225606_, RandomSource p_225607_, TreeConfiguration p_225608_, int p_225609_, FoliagePlacer.FoliageAttachment p_225610_, int p_225611_, int p_225612_) {
      this.createFoliage(p_225605_, p_225606_, p_225607_, p_225608_, p_225609_, p_225610_, p_225611_, p_225612_, this.offset(p_225607_));
   }

   protected abstract void createFoliage(LevelSimulatedReader p_225613_, BiConsumer<BlockPos, BlockState> p_225614_, RandomSource p_225615_, TreeConfiguration p_225616_, int p_225617_, FoliagePlacer.FoliageAttachment p_225618_, int p_225619_, int p_225620_, int p_225621_);

   public abstract int foliageHeight(RandomSource p_225601_, int p_225602_, TreeConfiguration p_225603_);

   public int foliageRadius(RandomSource p_225593_, int p_225594_) {
      return this.radius.sample(p_225593_);
   }

   private int offset(RandomSource p_225592_) {
      return this.offset.sample(p_225592_);
   }

   protected abstract boolean shouldSkipLocation(RandomSource p_225595_, int p_225596_, int p_225597_, int p_225598_, int p_225599_, boolean p_225600_);

   protected boolean shouldSkipLocationSigned(RandomSource p_225639_, int p_225640_, int p_225641_, int p_225642_, int p_225643_, boolean p_225644_) {
      int i;
      int j;
      if (p_225644_) {
         i = Math.min(Math.abs(p_225640_), Math.abs(p_225640_ - 1));
         j = Math.min(Math.abs(p_225642_), Math.abs(p_225642_ - 1));
      } else {
         i = Math.abs(p_225640_);
         j = Math.abs(p_225642_);
      }

      return this.shouldSkipLocation(p_225639_, i, p_225641_, j, p_225643_, p_225644_);
   }

   protected void placeLeavesRow(LevelSimulatedReader p_225629_, BiConsumer<BlockPos, BlockState> p_225630_, RandomSource p_225631_, TreeConfiguration p_225632_, BlockPos p_225633_, int p_225634_, int p_225635_, boolean p_225636_) {
      int i = p_225636_ ? 1 : 0;
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int j = -p_225634_; j <= p_225634_ + i; ++j) {
         for(int k = -p_225634_; k <= p_225634_ + i; ++k) {
            if (!this.shouldSkipLocationSigned(p_225631_, j, p_225635_, k, p_225634_, p_225636_)) {
               blockpos$mutableblockpos.setWithOffset(p_225633_, j, p_225635_, k);
               tryPlaceLeaf(p_225629_, p_225630_, p_225631_, p_225632_, blockpos$mutableblockpos);
            }
         }
      }

   }

   protected static void tryPlaceLeaf(LevelSimulatedReader p_225623_, BiConsumer<BlockPos, BlockState> p_225624_, RandomSource p_225625_, TreeConfiguration p_225626_, BlockPos p_225627_) {
      if (TreeFeature.validTreePos(p_225623_, p_225627_)) {
         BlockState blockstate = p_225626_.foliageProvider.getState(p_225625_, p_225627_);
         if (blockstate.hasProperty(BlockStateProperties.WATERLOGGED)) {
            blockstate = blockstate.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(p_225623_.isFluidAtPosition(p_225627_, (p_225638_) -> {
               return p_225638_.isSourceOfType(Fluids.WATER);
            })));
         }

         p_225624_.accept(p_225627_, blockstate);
      }

   }

   public static final class FoliageAttachment {
      private final BlockPos pos;
      private final int radiusOffset;
      private final boolean doubleTrunk;

      public FoliageAttachment(BlockPos p_68585_, int p_68586_, boolean p_68587_) {
         this.pos = p_68585_;
         this.radiusOffset = p_68586_;
         this.doubleTrunk = p_68587_;
      }

      public BlockPos pos() {
         return this.pos;
      }

      public int radiusOffset() {
         return this.radiusOffset;
      }

      public boolean doubleTrunk() {
         return this.doubleTrunk;
      }
   }
}