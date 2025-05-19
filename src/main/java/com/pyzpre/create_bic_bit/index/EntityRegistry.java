package com.pyzpre.create_bic_bit.index;

import com.pyzpre.create_bic_bit.CreateBitterballen;
import com.pyzpre.create_bic_bit.entity.HerringEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CreateBitterballen.MOD_ID);

    public static final RegistryObject<EntityType<HerringEntity>> HERRING = ENTITY_TYPES.register("herring",
            () -> EntityType.Builder.of(HerringEntity::new, MobCategory.WATER_AMBIENT)
                    .sized(0.3f, 0.3f)
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
