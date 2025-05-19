package com.pyzpre.createbitterballen.index;

import com.pyzpre.createbitterballen.CreateBitterballen;
import com.pyzpre.createbitterballen.entity.HerringEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class EntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, CreateBitterballen.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<HerringEntity>> HERRING = ENTITY_TYPES.register("herring",
            () -> EntityType.Builder.of(HerringEntity::new, MobCategory.WATER_AMBIENT)
                    .sized(0.65f, 0.4f)
                    .build(CreateBitterballen.asResource("herring").toString())
    );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
        eventBus.addListener(EntityRegistry::onRegisterAttributes);
    }

    public static void onRegisterAttributes(EntityAttributeCreationEvent event) {
        event.put(HERRING.get(), HerringEntity.createAttributes().build());
    }

}
