package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.features.NetherFeatures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.lighting.LayerLightEngine;

public class NyliumBlock extends Block implements BonemealableBlock {
   public NyliumBlock(BlockBehaviour.Properties p_55057_) {
      super(p_55057_);
   }

   private static boolean canBeNylium(BlockState p_55079_, LevelReader p_55080_, BlockPos p_55081_) {
      BlockPos blockpos = p_55081_.above();
      BlockState blockstate = p_55080_.getBlockState(blockpos);
      int i = LayerLightEngine.getLightBlockInto(p_55080_, p_55079_, p_55081_, blockstate, blockpos, Direction.UP, blockstate.getLightBlock(p_55080_, blockpos));
      return i < p_55080_.getMaxLightLevel();
   }

   public void randomTick(BlockState p_221835_, ServerLevel p_221836_, BlockPos p_221837_, RandomSource p_221838_) {
      if (!canBeNylium(p_221835_, p_221836_, p_221837_)) {
         p_221836_.setBlockAndUpdate(p_221837_, Blocks.NETHERRACK.defaultBlockState());
      }

   }

   public boolean isValidBonemealTarget(BlockGetter p_55064_, BlockPos p_55065_, BlockState p_55066_, boolean p_55067_) {
      return p_55064_.getBlockState(p_55065_.above()).isAir();
   }

   public boolean isBonemealSuccess(Level p_221830_, RandomSource p_221831_, BlockPos p_221832_, BlockState p_221833_) {
      return true;
   }

   public void performBonemeal(ServerLevel p_221825_, RandomSource p_221826_, BlockPos p_221827_, BlockState p_221828_) {
      BlockState blockstate = p_221825_.getBlockState(p_221827_);
      BlockPos blockpos = p_221827_.above();
      ChunkGenerator chunkgenerator = p_221825_.getChunkSource().getGenerator();
      if (blockstate.is(Blocks.CRIMSON_NYLIUM)) {
         NetherFeatures.CRIMSON_FOREST_VEGETATION_BONEMEAL.value().place(p_221825_, chunkgenerator, p_221826_, blockpos);
      } else if (blockstate.is(Blocks.WARPED_NYLIUM)) {
         NetherFeatures.WARPED_FOREST_VEGETATION_BONEMEAL.value().place(p_221825_, chunkgenerator, p_221826_, blockpos);
         NetherFeatures.NETHER_SPROUTS_BONEMEAL.value().place(p_221825_, chunkgenerator, p_221826_, blockpos);
         if (p_221826_.nextInt(8) == 0) {
            NetherFeatures.TWISTING_VINES_BONEMEAL.value().place(p_221825_, chunkgenerator, p_221826_, blockpos);
         }
      }

   }
}