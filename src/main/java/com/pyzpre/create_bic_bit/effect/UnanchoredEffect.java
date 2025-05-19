package com.pyzpre.create_bic_bit.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

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
        if (!entity.getLevel().isClientSide) {
            if (--cooldown <= 0) {
                if (RANDOM.nextInt(100) < 20) {
                    for (int i = 0; i < 10; i++) {
                        double dX = entity.getX() + (RANDOM.nextDouble() - 0.5) * 2 * 20;
                        double dZ = entity.getZ() + (RANDOM.nextDouble() - 0.5) * 2 * 20;
                        double dY = entity.getY() + RANDOM.nextInt(7) - 3;

                        if (isTeleportDestinationSafe(entity, dX, dY, dZ)) {
                            entity.getLevel().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                            BlockPos newPos = new BlockPos((int) dX, (int) dY, (int) dZ);
                            spawnParticles(entity.getLevel(), newPos);
                            entity.teleportTo(dX, dY, dZ);
                            entity.getLevel().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                            spawnParticles(entity.getLevel(), newPos);

                            cooldown = MIN_COOLDOWN;
                            break;
                        }
                    }
                }
            }
        }
    }

    private void spawnParticles(Level world, BlockPos pos) {
        if (world instanceof ServerLevel) {
            ServerLevel serverWorld = (ServerLevel) world;
            for (int i = 0; i < 20; i++) {
                double d0 = serverWorld.random.nextGaussian() * 0.02D;
                double d1 = serverWorld.random.nextGaussian() * 0.02D;
                double d2 = serverWorld.random.nextGaussian() * 0.02D;
                double x = pos.getX() + 0.5 + serverWorld.random.nextGaussian() * 0.5;
                double y = pos.getY() + 0.6;
                double z = pos.getZ() + 0.5 + serverWorld.random.nextGaussian() * 0.5;
                serverWorld.sendParticles(ParticleTypes.PORTAL, x, y, z, 1, d0, d1, d2, 0.0D);
            }
        }
    }

    private boolean isTeleportDestinationSafe(LivingEntity entity, double x, double y, double z) {
        var destinationPos = new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
        var belowDestinationPos = destinationPos.below();
        var aboveDestinationPos = destinationPos.above();

        var world = entity.getLevel();
        boolean isDestinationBlockSafe = world.getBlockState(destinationPos).isAir() && world.getBlockState(aboveDestinationPos).isAir();
        boolean isBelowDestinationSolid = world.getBlockState(belowDestinationPos).canOcclude();

        return isDestinationBlockSafe && isBelowDestinationSolid;
    }


    @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
    }

