package createbicyclesbitterballen.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import java.util.Random;

public class UnanchoredEffect extends MobEffect {

        private static final Random RANDOM = new Random();
        private static final int MIN_COOLDOWN = 80;
        private int cooldown = MIN_COOLDOWN;

        public UnanchoredEffect(MobEffectCategory mobEffectCategory, int color) {
            super(mobEffectCategory, color);
        }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            if (--cooldown <= 0) {
                if (RANDOM.nextInt(100) < 20) {
                    for (int i = 0; i < 10; i++) {
                        double dX = entity.getX() + (RANDOM.nextDouble() - 0.5) * 2 * 20;
                        double dZ = entity.getZ() + (RANDOM.nextDouble() - 0.5) * 2 * 20;
                        double dY = entity.getY() + RANDOM.nextInt(7) - 3;

                        if (isTeleportDestinationSafe(entity, dX, dY, dZ)) {

                            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

                            entity.teleportTo(dX, dY, dZ);

                            entity.level().playSound(null, dX, dY, dZ, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

                            cooldown = MIN_COOLDOWN;
                            break;
                        }
                    }
                }
            }
        }
    }




    private boolean isTeleportDestinationSafe(LivingEntity entity, double x, double y, double z) {
        var destinationPos = new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
        var belowDestinationPos = destinationPos.below();
        var aboveDestinationPos = destinationPos.above();

        var world = entity.level();
        boolean isDestinationBlockSafe = world.getBlockState(destinationPos).isAir() && world.getBlockState(aboveDestinationPos).isAir();
        boolean isBelowDestinationSolid = world.getBlockState(belowDestinationPos).canOcclude();

        return isDestinationBlockSafe && isBelowDestinationSolid;
    }


    @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
    }

