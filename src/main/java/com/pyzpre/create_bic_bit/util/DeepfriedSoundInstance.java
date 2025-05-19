package com.pyzpre.create_bic_bit.util;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public class DeepfriedSoundInstance implements SoundInstance {

    private final ResourceLocation location;
    private final SoundSource source;
    private final float volume;
    private final float pitch;
    private final double x, y, z;
    private final boolean looping;
    private final int delay;
    private final Attenuation attenuation;
    private Sound resolvedSound;

    public DeepfriedSoundInstance(ResourceLocation location, SoundSource source, float volume, float pitch, boolean looping, int delay, Attenuation attenuation, double x, double y, double z) {
        this.location = location;
        this.source = source;
        this.volume = volume;
        this.pitch = pitch;
        this.looping = looping;
        this.delay = delay;
        this.attenuation = attenuation;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public ResourceLocation getLocation() {
        return location;
    }

    @Nullable
    @Override
    public WeighedSoundEvents resolve(SoundManager soundManager) {
        WeighedSoundEvents weighedSoundEvents = soundManager.getSoundEvent(location);
        if (weighedSoundEvents != null) {
            resolvedSound = weighedSoundEvents.getSound(RandomSource.create());
        }
        return weighedSoundEvents;
    }

    @Override
    public Sound getSound() {
        if (resolvedSound == null) {
            throw new IllegalStateException("Sound has not been resolved. Ensure resolve() is called before playback.");
        }
        return resolvedSound;
    }

    @Override
    public SoundSource getSource() {
        return source;
    }

    @Override
    public boolean isLooping() {
        return looping;
    }

    @Override
    public boolean isRelative() {
        return false; // Adjust if needed
    }

    @Override
    public int getDelay() {
        return delay;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public float getPitch() {
        return pitch;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getZ() {
        return z;
    }

    @Override
    public Attenuation getAttenuation() {
        return attenuation;
    }
}