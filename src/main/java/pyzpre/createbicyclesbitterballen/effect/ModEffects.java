package pyzpre.createbicyclesbitterballen.effect;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import org.intellij.lang.annotations.Identifier;
import pyzpre.createbicyclesbitterballen.CreateBitterballen;

public class ModEffects {
    public static final MobEffect UNANCHORED = new UnanchoredEffect(MobEffectCategory.HARMFUL, 800000980);

    public static void register() {
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation(CreateBitterballen.MOD_ID, "unanchored"), UNANCHORED);
    }
}

