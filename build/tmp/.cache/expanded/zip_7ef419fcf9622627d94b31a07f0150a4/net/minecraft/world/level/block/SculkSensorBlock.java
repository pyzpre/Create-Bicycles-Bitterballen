package net.minecraft.world.level.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SculkSensorBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final int ACTIVE_TICKS = 40;
   public static final int COOLDOWN_TICKS = 1;
   public static final Object2IntMap<GameEvent> VIBRATION_FREQUENCY_FOR_EVENT = Object2IntMaps.unmodifiable(Util.make(new Object2IntOpenHashMap<>(), (p_238254_) -> {
      p_238254_.put(GameEvent.STEP, 1);
      p_238254_.put(GameEvent.FLAP, 2);
      p_238254_.put(GameEvent.SWIM, 3);
      p_238254_.put(GameEvent.ELYTRA_GLIDE, 4);
      p_238254_.put(GameEvent.HIT_GROUND, 5);
      p_238254_.put(GameEvent.TELEPORT, 5);
      p_238254_.put(GameEvent.SPLASH, 6);
      p_238254_.put(GameEvent.ENTITY_SHAKE, 6);
      p_238254_.put(GameEvent.BLOCK_CHANGE, 6);
      p_238254_.put(GameEvent.NOTE_BLOCK_PLAY, 6);
      p_238254_.put(GameEvent.PROJECTILE_SHOOT, 7);
      p_238254_.put(GameEvent.DRINK, 7);
      p_238254_.put(GameEvent.PRIME_FUSE, 7);
      p_238254_.put(GameEvent.PROJECTILE_LAND, 8);
      p_238254_.put(GameEvent.EAT, 8);
      p_238254_.put(GameEvent.ENTITY_INTERACT, 8);
      p_238254_.put(GameEvent.ENTITY_DAMAGE, 8);
      p_238254_.put(GameEvent.EQUIP, 9);
      p_238254_.put(GameEvent.SHEAR, 9);
      p_238254_.put(GameEvent.ENTITY_ROAR, 9);
      p_238254_.put(GameEvent.BLOCK_CLOSE, 10);
      p_238254_.put(GameEvent.BLOCK_DEACTIVATE, 10);
      p_238254_.put(GameEvent.BLOCK_DETACH, 10);
      p_238254_.put(GameEvent.DISPENSE_FAIL, 10);
      p_238254_.put(GameEvent.BLOCK_OPEN, 11);
      p_238254_.put(GameEvent.BLOCK_ACTIVATE, 11);
      p_238254_.put(GameEvent.BLOCK_ATTACH, 11);
      p_238254_.put(GameEvent.ENTITY_PLACE, 12);
      p_238254_.put(GameEvent.BLOCK_PLACE, 12);
      p_238254_.put(GameEvent.FLUID_PLACE, 12);
      p_238254_.put(GameEvent.ENTITY_DIE, 13);
      p_238254_.put(GameEvent.BLOCK_DESTROY, 13);
      p_238254_.put(GameEvent.FLUID_PICKUP, 13);
      p_238254_.put(GameEvent.ITEM_INTERACT_FINISH, 14);
      p_238254_.put(GameEvent.CONTAINER_CLOSE, 14);
      p_238254_.put(GameEvent.PISTON_CONTRACT, 14);
      p_238254_.put(GameEvent.PISTON_EXTEND, 15);
      p_238254_.put(GameEvent.CONTAINER_OPEN, 15);
      p_238254_.put(GameEvent.ITEM_INTERACT_START, 15);
      p_238254_.put(GameEvent.EXPLODE, 15);
      p_238254_.put(GameEvent.LIGHTNING_STRIKE, 15);
      p_238254_.put(GameEvent.INSTRUMENT_PLAY, 15);
   }));
   public static final EnumProperty<SculkSensorPhase> PHASE = BlockStateProperties.SCULK_SENSOR_PHASE;
   public static final IntegerProperty POWER = BlockStateProperties.POWER;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
   private final int listenerRange;

   public SculkSensorBlock(BlockBehaviour.Properties p_154393_, int p_154394_) {
      super(p_154393_);
      this.registerDefaultState(this.stateDefinition.any().setValue(PHASE, SculkSensorPhase.INACTIVE).setValue(POWER, Integer.valueOf(0)).setValue(WATERLOGGED, Boolean.valueOf(false)));
      this.listenerRange = p_154394_;
   }

   public int getListenerRange() {
      return this.listenerRange;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext p_154396_) {
      BlockPos blockpos = p_154396_.getClickedPos();
      FluidState fluidstate = p_154396_.getLevel().getFluidState(blockpos);
      return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
   }

   public FluidState getFluidState(BlockState p_154479_) {
      return p_154479_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_154479_);
   }

   public void tick(BlockState p_222137_, ServerLevel p_222138_, BlockPos p_222139_, RandomSource p_222140_) {
      if (getPhase(p_222137_) != SculkSensorPhase.ACTIVE) {
         if (getPhase(p_222137_) == SculkSensorPhase.COOLDOWN) {
            p_222138_.setBlock(p_222139_, p_222137_.setValue(PHASE, SculkSensorPhase.INACTIVE), 3);
         }

      } else {
         deactivate(p_222138_, p_222139_, p_222137_);
      }
   }

   public void stepOn(Level p_222132_, BlockPos p_222133_, BlockState p_222134_, Entity p_222135_) {
      if (!p_222132_.isClientSide() && canActivate(p_222134_) && p_222135_.getType() != EntityType.WARDEN) {
         BlockEntity blockentity = p_222132_.getBlockEntity(p_222133_);
         if (blockentity instanceof SculkSensorBlockEntity) {
            SculkSensorBlockEntity sculksensorblockentity = (SculkSensorBlockEntity)blockentity;
            sculksensorblockentity.setLastVibrationFrequency(VIBRATION_FREQUENCY_FOR_EVENT.get(GameEvent.STEP));
         }

         activate(p_222135_, p_222132_, p_222133_, p_222134_, 15);
      }

      super.stepOn(p_222132_, p_222133_, p_222134_, p_222135_);
   }

   public void onPlace(BlockState p_154471_, Level p_154472_, BlockPos p_154473_, BlockState p_154474_, boolean p_154475_) {
      if (!p_154472_.isClientSide() && !p_154471_.is(p_154474_.getBlock())) {
         if (p_154471_.getValue(POWER) > 0 && !p_154472_.getBlockTicks().hasScheduledTick(p_154473_, this)) {
            p_154472_.setBlock(p_154473_, p_154471_.setValue(POWER, Integer.valueOf(0)), 18);
         }

         p_154472_.scheduleTick(new BlockPos(p_154473_), p_154471_.getBlock(), 1);
      }
   }

   public void onRemove(BlockState p_154446_, Level p_154447_, BlockPos p_154448_, BlockState p_154449_, boolean p_154450_) {
      if (!p_154446_.is(p_154449_.getBlock())) {
         if (getPhase(p_154446_) == SculkSensorPhase.ACTIVE) {
            updateNeighbours(p_154447_, p_154448_);
         }

         super.onRemove(p_154446_, p_154447_, p_154448_, p_154449_, p_154450_);
      }
   }

   public BlockState updateShape(BlockState p_154457_, Direction p_154458_, BlockState p_154459_, LevelAccessor p_154460_, BlockPos p_154461_, BlockPos p_154462_) {
      if (p_154457_.getValue(WATERLOGGED)) {
         p_154460_.scheduleTick(p_154461_, Fluids.WATER, Fluids.WATER.getTickDelay(p_154460_));
      }

      return super.updateShape(p_154457_, p_154458_, p_154459_, p_154460_, p_154461_, p_154462_);
   }

   private static void updateNeighbours(Level p_154405_, BlockPos p_154406_) {
      p_154405_.updateNeighborsAt(p_154406_, Blocks.SCULK_SENSOR);
      p_154405_.updateNeighborsAt(p_154406_.relative(Direction.UP.getOpposite()), Blocks.SCULK_SENSOR);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos p_154466_, BlockState p_154467_) {
      return new SculkSensorBlockEntity(p_154466_, p_154467_);
   }

   @Nullable
   public <T extends BlockEntity> GameEventListener getListener(ServerLevel p_222123_, T p_222124_) {
      return p_222124_ instanceof SculkSensorBlockEntity ? ((SculkSensorBlockEntity)p_222124_).getListener() : null;
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_154401_, BlockState p_154402_, BlockEntityType<T> p_154403_) {
      return !p_154401_.isClientSide ? createTickerHelper(p_154403_, BlockEntityType.SCULK_SENSOR, (p_154417_, p_154418_, p_154419_, p_154420_) -> {
         p_154420_.getListener().tick(p_154417_);
      }) : null;
   }

   public RenderShape getRenderShape(BlockState p_154477_) {
      return RenderShape.MODEL;
   }

   public VoxelShape getShape(BlockState p_154432_, BlockGetter p_154433_, BlockPos p_154434_, CollisionContext p_154435_) {
      return SHAPE;
   }

   public boolean isSignalSource(BlockState p_154484_) {
      return true;
   }

   public int getSignal(BlockState p_154437_, BlockGetter p_154438_, BlockPos p_154439_, Direction p_154440_) {
      return p_154437_.getValue(POWER);
   }

   public static SculkSensorPhase getPhase(BlockState p_154488_) {
      return p_154488_.getValue(PHASE);
   }

   public static boolean canActivate(BlockState p_154490_) {
      return getPhase(p_154490_) == SculkSensorPhase.INACTIVE;
   }

   public static void deactivate(Level p_154408_, BlockPos p_154409_, BlockState p_154410_) {
      p_154408_.setBlock(p_154409_, p_154410_.setValue(PHASE, SculkSensorPhase.COOLDOWN).setValue(POWER, Integer.valueOf(0)), 3);
      p_154408_.scheduleTick(p_154409_, p_154410_.getBlock(), 1);
      if (!p_154410_.getValue(WATERLOGGED)) {
         p_154408_.playSound((Player)null, p_154409_, SoundEvents.SCULK_CLICKING_STOP, SoundSource.BLOCKS, 1.0F, p_154408_.random.nextFloat() * 0.2F + 0.8F);
      }

      updateNeighbours(p_154408_, p_154409_);
   }

   public static void activate(@Nullable Entity p_222126_, Level p_222127_, BlockPos p_222128_, BlockState p_222129_, int p_222130_) {
      p_222127_.setBlock(p_222128_, p_222129_.setValue(PHASE, SculkSensorPhase.ACTIVE).setValue(POWER, Integer.valueOf(p_222130_)), 3);
      p_222127_.scheduleTick(p_222128_, p_222129_.getBlock(), 40);
      updateNeighbours(p_222127_, p_222128_);
      p_222127_.gameEvent(p_222126_, GameEvent.SCULK_SENSOR_TENDRILS_CLICKING, p_222128_);
      if (!p_222129_.getValue(WATERLOGGED)) {
         p_222127_.playSound((Player)null, (double)p_222128_.getX() + 0.5D, (double)p_222128_.getY() + 0.5D, (double)p_222128_.getZ() + 0.5D, SoundEvents.SCULK_CLICKING, SoundSource.BLOCKS, 1.0F, p_222127_.random.nextFloat() * 0.2F + 0.8F);
      }

   }

   public void animateTick(BlockState p_222148_, Level p_222149_, BlockPos p_222150_, RandomSource p_222151_) {
      if (getPhase(p_222148_) == SculkSensorPhase.ACTIVE) {
         Direction direction = Direction.getRandom(p_222151_);
         if (direction != Direction.UP && direction != Direction.DOWN) {
            double d0 = (double)p_222150_.getX() + 0.5D + (direction.getStepX() == 0 ? 0.5D - p_222151_.nextDouble() : (double)direction.getStepX() * 0.6D);
            double d1 = (double)p_222150_.getY() + 0.25D;
            double d2 = (double)p_222150_.getZ() + 0.5D + (direction.getStepZ() == 0 ? 0.5D - p_222151_.nextDouble() : (double)direction.getStepZ() * 0.6D);
            double d3 = (double)p_222151_.nextFloat() * 0.04D;
            p_222149_.addParticle(DustColorTransitionOptions.SCULK_TO_REDSTONE, d0, d1, d2, 0.0D, d3, 0.0D);
         }
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_154464_) {
      p_154464_.add(PHASE, POWER, WATERLOGGED);
   }

   public boolean hasAnalogOutputSignal(BlockState p_154481_) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState p_154442_, Level p_154443_, BlockPos p_154444_) {
      BlockEntity blockentity = p_154443_.getBlockEntity(p_154444_);
      if (blockentity instanceof SculkSensorBlockEntity sculksensorblockentity) {
         return getPhase(p_154442_) == SculkSensorPhase.ACTIVE ? sculksensorblockentity.getLastVibrationFrequency() : 0;
      } else {
         return 0;
      }
   }

   public boolean isPathfindable(BlockState p_154427_, BlockGetter p_154428_, BlockPos p_154429_, PathComputationType p_154430_) {
      return false;
   }

   public boolean useShapeForLightOcclusion(BlockState p_154486_) {
      return true;
   }

   public void spawnAfterBreak(BlockState p_222142_, ServerLevel p_222143_, BlockPos p_222144_, ItemStack p_222145_, boolean p_222146_) {
      super.spawnAfterBreak(p_222142_, p_222143_, p_222144_, p_222145_, p_222146_);

   }

   @Override
   public int getExpDrop(BlockState state, net.minecraft.world.level.LevelReader level, RandomSource randomSource, BlockPos pos, int fortuneLevel, int silkTouchLevel) {
      return silkTouchLevel == 0 ? 5 : 0;
   }
}
