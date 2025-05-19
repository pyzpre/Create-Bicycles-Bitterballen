package com.pyzpre.createbitterballen.index;


import com.pyzpre.createbitterballen.CreateBitterballen;
import com.pyzpre.createbitterballen.effect.OiledUpEffect;
import com.pyzpre.createbitterballen.effect.UnanchoredEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EffectRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS
            = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, CreateBitterballen.MOD_ID);

    public static final RegistryObject<MobEffect> UNANCHORED = MOB_EFFECTS.register("unanchored",
            () -> new UnanchoredEffect(MobEffectCategory.NEUTRAL, 800000980));
    public static final RegistryObject<MobEffect> OILED_UP = MOB_EFFECTS.register("oiled_up",
            () -> new OiledUpEffect(MobEffectCategory.NEUTRAL, 15249764));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}

