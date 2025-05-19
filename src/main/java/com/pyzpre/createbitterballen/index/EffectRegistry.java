package com.pyzpre.createbitterballen.index;

import com.pyzpre.createbitterballen.CreateBitterballen;
import com.pyzpre.createbitterballen.effect.OiledUpEffect;
import com.pyzpre.createbitterballen.effect.UnanchoredEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class EffectRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, CreateBitterballen.MOD_ID);

    public static final DeferredHolder<MobEffect, UnanchoredEffect> UNANCHORED =
            MOB_EFFECTS.register("unanchored", () -> new UnanchoredEffect(MobEffectCategory.NEUTRAL, 800000980));

    public static final DeferredHolder<MobEffect, OiledUpEffect> OILED_UP =
            MOB_EFFECTS.register("oiled_up", () -> new OiledUpEffect(MobEffectCategory.NEUTRAL, 15249764));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
