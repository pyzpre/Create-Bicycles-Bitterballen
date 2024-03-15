package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class ProcessorRule {
   public static final Codec<ProcessorRule> CODEC = RecordCodecBuilder.create((p_74246_) -> {
      return p_74246_.group(RuleTest.CODEC.fieldOf("input_predicate").forGetter((p_163747_) -> {
         return p_163747_.inputPredicate;
      }), RuleTest.CODEC.fieldOf("location_predicate").forGetter((p_163745_) -> {
         return p_163745_.locPredicate;
      }), PosRuleTest.CODEC.optionalFieldOf("position_predicate", PosAlwaysTrueTest.INSTANCE).forGetter((p_163743_) -> {
         return p_163743_.posPredicate;
      }), BlockState.CODEC.fieldOf("output_state").forGetter((p_163741_) -> {
         return p_163741_.outputState;
      }), CompoundTag.CODEC.optionalFieldOf("output_nbt").forGetter((p_163739_) -> {
         return Optional.ofNullable(p_163739_.outputTag);
      })).apply(p_74246_, ProcessorRule::new);
   });
   private final RuleTest inputPredicate;
   private final RuleTest locPredicate;
   private final PosRuleTest posPredicate;
   private final BlockState outputState;
   @Nullable
   private final CompoundTag outputTag;

   public ProcessorRule(RuleTest p_74223_, RuleTest p_74224_, BlockState p_74225_) {
      this(p_74223_, p_74224_, PosAlwaysTrueTest.INSTANCE, p_74225_, Optional.empty());
   }

   public ProcessorRule(RuleTest p_74227_, RuleTest p_74228_, PosRuleTest p_74229_, BlockState p_74230_) {
      this(p_74227_, p_74228_, p_74229_, p_74230_, Optional.empty());
   }

   public ProcessorRule(RuleTest p_74232_, RuleTest p_74233_, PosRuleTest p_74234_, BlockState p_74235_, Optional<CompoundTag> p_74236_) {
      this.inputPredicate = p_74232_;
      this.locPredicate = p_74233_;
      this.posPredicate = p_74234_;
      this.outputState = p_74235_;
      this.outputTag = p_74236_.orElse((CompoundTag)null);
   }

   public boolean test(BlockState p_230310_, BlockState p_230311_, BlockPos p_230312_, BlockPos p_230313_, BlockPos p_230314_, RandomSource p_230315_) {
      return this.inputPredicate.test(p_230310_, p_230315_) && this.locPredicate.test(p_230311_, p_230315_) && this.posPredicate.test(p_230312_, p_230313_, p_230314_, p_230315_);
   }

   public BlockState getOutputState() {
      return this.outputState;
   }

   @Nullable
   public CompoundTag getOutputTag() {
      return this.outputTag;
   }
}