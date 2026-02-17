package com.pyzpre.createbitterballen.index;


import com.pyzpre.createbitterballen.CreateBitterballen;
import com.pyzpre.createbitterballen.effect.OiledUpEffect;
import com.pyzpre.createbitterballen.effect.UnanchoredEffect;
import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class EffectRegistry {
    public static final LazyRegistrar<MobEffect> MOB_EFFECTS
            = LazyRegistrar.create(BuiltInRegistries.MOB_EFFECT, CreateBitterballen.MOD_ID);

    public static final RegistryObject<MobEffect> UNANCHORED = MOB_EFFECTS.register("unanchored",
            () -> new UnanchoredEffect(MobEffectCategory.NEUTRAL, 800000980));
    public static final RegistryObject<MobEffect> OILED_UP = MOB_EFFECTS.register("oiled_up",
            () -> new OiledUpEffect(MobEffectCategory.NEUTRAL, 15249764));

    public static void register() {
        MOB_EFFECTS.register();
    }
}

