package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public class TreeFeature extends Feature<TreeConfiguration> {
   private static final int BLOCK_UPDATE_FLAGS = 19;

   public TreeFeature(Codec<TreeConfiguration> p_67201_) {
      super(p_67201_);
   }

   private static boolean isVine(LevelSimulatedReader p_67278_, BlockPos p_67279_) {
      return p_67278_.isStateAtPosition(p_67279_, (p_225299_) -> {
         return p_225299_.is(Blocks.VINE);
      });
   }

   public static boolean isBlockWater(LevelSimulatedReader p_67283_, BlockPos p_67284_) {
      return p_67283_.isStateAtPosition(p_67284_, (p_225297_) -> {
         return p_225297_.is(Blocks.WATER);
      });
   }

   public static boolean isAirOrLeaves(LevelSimulatedReader p_67268_, BlockPos p_67269_) {
      return p_67268_.isStateAtPosition(p_67269_, (p_225295_) -> {
         return p_225295_.isAir() || p_225295_.is(BlockTags.LEAVES);
      });
   }

   private static boolean isReplaceablePlant(LevelSimulatedReader p_67289_, BlockPos p_67290_) {
      return p_67289_.isStateAtPosition(p_67290_, (p_225293_) -> {
         Material material = p_225293_.getMaterial();
         return material == Material.REPLACEABLE_PLANT || material == Material.REPLACEABLE_WATER_PLANT || material == Material.REPLACEABLE_FIREPROOF_PLANT;
      });
   }

   private static void setBlockKnownShape(LevelWriter p_67257_, BlockPos p_67258_, BlockState p_67259_) {
      p_67257_.setBlock(p_67258_, p_67259_, 19);
   }

   public static boolean validTreePos(LevelSimulatedReader p_67273_, BlockPos p_67274_) {
      return isAirOrLeaves(p_67273_, p_67274_) || isReplaceablePlant(p_67273_, p_67274_) || isBlockWater(p_67273_, p_67274_);
   }

   private boolean doPlace(WorldGenLevel p_225258_, RandomSource p_225259_, BlockPos p_225260_, BiConsumer<BlockPos, BlockState> p_225261_, BiConsumer<BlockPos, BlockState> p_225262_, BiConsumer<BlockPos, BlockState> p_225263_, TreeConfiguration p_225264_) {
      int i = p_225264_.trunkPlacer.getTreeHeight(p_225259_);
      int j = p_225264_.foliagePlacer.foliageHeight(p_225259_, i, p_225264_);
      int k = i - j;
      int l = p_225264_.foliagePlacer.foliageRadius(p_225259_, k);
      BlockPos blockpos = p_225264_.rootPlacer.map((p_225286_) -> {
         return p_225286_.getTrunkOrigin(p_225260_, p_225259_);
      }).orElse(p_225260_);
      int i1 = Math.min(p_225260_.getY(), blockpos.getY());
      int j1 = Math.max(p_225260_.getY(), blockpos.getY()) + i + 1;
      if (i1 >= p_225258_.getMinBuildHeight() + 1 && j1 <= p_225258_.getMaxBuildHeight()) {
         OptionalInt optionalint = p_225264_.minimumSize.minClippedHeight();
         int k1 = this.getMaxFreeTreeHeight(p_225258_, i, blockpos, p_225264_);
         if (k1 >= i || !optionalint.isEmpty() && k1 >= optionalint.getAsInt()) {
            if (p_225264_.rootPlacer.isPresent() && !p_225264_.rootPlacer.get().placeRoots(p_225258_, p_225261_, p_225259_, p_225260_, blockpos, p_225264_)) {
               return false;
            } else {
               List<FoliagePlacer.FoliageAttachment> list = p_225264_.trunkPlacer.placeTrunk(p_225258_, p_225262_, p_225259_, k1, blockpos, p_225264_);
               list.forEach((p_225279_) -> {
                  p_225264_.foliagePlacer.createFoliage(p_225258_, p_225263_, p_225259_, p_225264_, k1, p_225279_, j, l);
               });
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private int getMaxFreeTreeHeight(LevelSimulatedReader p_67216_, int p_67217_, BlockPos p_67218_, TreeConfiguration p_67219_) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int i = 0; i <= p_67217_ + 1; ++i) {
         int j = p_67219_.minimumSize.getSizeAtHeight(p_67217_, i);

         for(int k = -j; k <= j; ++k) {
            for(int l = -j; l <= j; ++l) {
               blockpos$mutableblockpos.setWithOffset(p_67218_, k, i, l);
               if (!p_67219_.trunkPlacer.isFree(p_67216_, blockpos$mutableblockpos) || !p_67219_.ignoreVines && isVine(p_67216_, blockpos$mutableblockpos)) {
                  return i - 2;
               }
            }
         }
      }

      return p_67217_;
   }

   protected void setBlock(LevelWriter p_67221_, BlockPos p_67222_, BlockState p_67223_) {
      setBlockKnownShape(p_67221_, p_67222_, p_67223_);
   }

   public final boolean place(FeaturePlaceContext<TreeConfiguration> p_160530_) {
      WorldGenLevel worldgenlevel = p_160530_.level();
      RandomSource randomsource = p_160530_.random();
      BlockPos blockpos = p_160530_.origin();
      TreeConfiguration treeconfiguration = p_160530_.config();
      Set<BlockPos> set = Sets.newHashSet();
      Set<BlockPos> set1 = Sets.newHashSet();
      Set<BlockPos> set2 = Sets.newHashSet();
      Set<BlockPos> set3 = Sets.newHashSet();
      BiConsumer<BlockPos, BlockState> biconsumer = (p_160555_, p_160556_) -> {
         set.add(p_160555_.immutable());
         worldgenlevel.setBlock(p_160555_, p_160556_, 19);
      };
      BiConsumer<BlockPos, BlockState> biconsumer1 = (p_160548_, p_160549_) -> {
         set1.add(p_160548_.immutable());
         worldgenlevel.setBlock(p_160548_, p_160549_, 19);
      };
      BiConsumer<BlockPos, BlockState> biconsumer2 = (p_160543_, p_160544_) -> {
         set2.add(p_160543_.immutable());
         worldgenlevel.setBlock(p_160543_, p_160544_, 19);
      };
      BiConsumer<BlockPos, BlockState> biconsumer3 = (p_225290_, p_225291_) -> {
         set3.add(p_225290_.immutable());
         worldgenlevel.setBlock(p_225290_, p_225291_, 19);
      };
      boolean flag = this.doPlace(worldgenlevel, randomsource, blockpos, biconsumer, biconsumer1, biconsumer2, treeconfiguration);
      if (flag && (!set1.isEmpty() || !set2.isEmpty())) {
         if (!treeconfiguration.decorators.isEmpty()) {
            TreeDecorator.Context treedecorator$context = new TreeDecorator.Context(worldgenlevel, biconsumer3, randomsource, set1, set2, set);
            treeconfiguration.decorators.forEach((p_225282_) -> {
               p_225282_.place(treedecorator$context);
            });
         }

         return BoundingBox.encapsulatingPositions(Iterables.concat(set, set1, set2, set3)).map((p_225270_) -> {
            DiscreteVoxelShape discretevoxelshape = updateLeaves(worldgenlevel, p_225270_, set1, set3, set);
            StructureTemplate.updateShapeAtEdge(worldgenlevel, 3, discretevoxelshape, p_225270_.minX(), p_225270_.minY(), p_225270_.minZ());
            return true;
         }).orElse(false);
      } else {
         return false;
      }
   }

   private static DiscreteVoxelShape updateLeaves(LevelAccessor p_225252_, BoundingBox p_225253_, Set<BlockPos> p_225254_, Set<BlockPos> p_225255_, Set<BlockPos> p_225256_) {
      List<Set<BlockPos>> list = Lists.newArrayList();
      DiscreteVoxelShape discretevoxelshape = new BitSetDiscreteVoxelShape(p_225253_.getXSpan(), p_225253_.getYSpan(), p_225253_.getZSpan());
      int i = 6;

      for(int j = 0; j < 6; ++j) {
         list.add(Sets.newHashSet());
      }

      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(BlockPos blockpos : Lists.newArrayList(Sets.union(p_225255_, p_225256_))) {
         if (p_225253_.isInside(blockpos)) {
            discretevoxelshape.fill(blockpos.getX() - p_225253_.minX(), blockpos.getY() - p_225253_.minY(), blockpos.getZ() - p_225253_.minZ());
         }
      }

      for(BlockPos blockpos1 : Lists.newArrayList(p_225254_)) {
         if (p_225253_.isInside(blockpos1)) {
            discretevoxelshape.fill(blockpos1.getX() - p_225253_.minX(), blockpos1.getY() - p_225253_.minY(), blockpos1.getZ() - p_225253_.minZ());
         }

         for(Direction direction : Direction.values()) {
            blockpos$mutableblockpos.setWithOffset(blockpos1, direction);
            if (!p_225254_.contains(blockpos$mutableblockpos)) {
               BlockState blockstate = p_225252_.getBlockState(blockpos$mutableblockpos);
               if (blockstate.hasProperty(BlockStateProperties.DISTANCE)) {
                  list.get(0).add(blockpos$mutableblockpos.immutable());
                  setBlockKnownShape(p_225252_, blockpos$mutableblockpos, blockstate.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(1)));
                  if (p_225253_.isInside(blockpos$mutableblockpos)) {
                     discretevoxelshape.fill(blockpos$mutableblockpos.getX() - p_225253_.minX(), blockpos$mutableblockpos.getY() - p_225253_.minY(), blockpos$mutableblockpos.getZ() - p_225253_.minZ());
                  }
               }
            }
         }
      }

      for(int l = 1; l < 6; ++l) {
         Set<BlockPos> set = list.get(l - 1);
         Set<BlockPos> set1 = list.get(l);

         for(BlockPos blockpos2 : set) {
            if (p_225253_.isInside(blockpos2)) {
               discretevoxelshape.fill(blockpos2.getX() - p_225253_.minX(), blockpos2.getY() - p_225253_.minY(), blockpos2.getZ() - p_225253_.minZ());
            }

            for(Direction direction1 : Direction.values()) {
               blockpos$mutableblockpos.setWithOffset(blockpos2, direction1);
               if (!set.contains(blockpos$mutableblockpos) && !set1.contains(blockpos$mutableblockpos)) {
                  BlockState blockstate1 = p_225252_.getBlockState(blockpos$mutableblockpos);
                  if (blockstate1.hasProperty(BlockStateProperties.DISTANCE)) {
                     int k = blockstate1.getValue(BlockStateProperties.DISTANCE);
                     if (k > l + 1) {
                        BlockState blockstate2 = blockstate1.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(l + 1));
                        setBlockKnownShape(p_225252_, blockpos$mutableblockpos, blockstate2);
                        if (p_225253_.isInside(blockpos$mutableblockpos)) {
                           discretevoxelshape.fill(blockpos$mutableblockpos.getX() - p_225253_.minX(), blockpos$mutableblockpos.getY() - p_225253_.minY(), blockpos$mutableblockpos.getZ() - p_225253_.minZ());
                        }

                        set1.add(blockpos$mutableblockpos.immutable());
                     }
                  }
               }
            }
         }
      }

      return discretevoxelshape;
   }
}