package createbicyclesbitterballen.effect;

import createbicyclesbitterballen.CreateBicBitMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS
            = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, CreateBicBitMod.MOD_ID);

    public static final RegistryObject<MobEffect> UNANCHORED = MOB_EFFECTS.register("unanchored",
            () -> new UnanchoredEffect(MobEffectCategory.NEUTRAL, 800000980));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
