package net.minecraft.world.effect;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class MobEffectUtil {
   public static String formatDuration(MobEffectInstance p_19582_, float p_19583_) {
      if (p_19582_.isNoCounter()) {
         return "**:**";
      } else {
         int i = Mth.floor((float)p_19582_.getDuration() * p_19583_);
         return StringUtil.formatTickDuration(i);
      }
   }

   public static boolean hasDigSpeed(LivingEntity p_19585_) {
      return p_19585_.hasEffect(MobEffects.DIG_SPEED) || p_19585_.hasEffect(MobEffects.CONDUIT_POWER);
   }

   public static int getDigSpeedAmplification(LivingEntity p_19587_) {
      int i = 0;
      int j = 0;
      if (p_19587_.hasEffect(MobEffects.DIG_SPEED)) {
         i = p_19587_.getEffect(MobEffects.DIG_SPEED).getAmplifier();
      }

      if (p_19587_.hasEffect(MobEffects.CONDUIT_POWER)) {
         j = p_19587_.getEffect(MobEffects.CONDUIT_POWER).getAmplifier();
      }

      return Math.max(i, j);
   }

   public static boolean hasWaterBreathing(LivingEntity p_19589_) {
      return p_19589_.hasEffect(MobEffects.WATER_BREATHING) || p_19589_.hasEffect(MobEffects.CONDUIT_POWER);
   }

   public static List<ServerPlayer> addEffectToPlayersAround(ServerLevel p_216947_, @Nullable Entity p_216948_, Vec3 p_216949_, double p_216950_, MobEffectInstance p_216951_, int p_216952_) {
      MobEffect mobeffect = p_216951_.getEffect();
      List<ServerPlayer> list = p_216947_.getPlayers((p_238228_) -> {
         return p_238228_.gameMode.isSurvival() && (p_216948_ == null || !p_216948_.isAlliedTo(p_238228_)) && p_216949_.closerThan(p_238228_.position(), p_216950_) && (!p_238228_.hasEffect(mobeffect) || p_238228_.getEffect(mobeffect).getAmplifier() < p_216951_.getAmplifier() || p_238228_.getEffect(mobeffect).getDuration() < p_216952_);
      });
      list.forEach((p_238232_) -> {
         p_238232_.addEffect(new MobEffectInstance(p_216951_), p_216948_);
      });
      return list;
   }
}