package com.pyzpre.createbitterballen.index;

import com.pyzpre.createbitterballen.CreateBitterballen;
import com.pyzpre.createbitterballen.entity.HerringEntity;
import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class EntityRegistry {
    public static final LazyRegistrar<EntityType<?>> ENTITY_TYPES = LazyRegistrar.create(BuiltInRegistries.ENTITY_TYPE, CreateBitterballen.MOD_ID);

    public static final RegistryObject<EntityType<HerringEntity>> HERRING = ENTITY_TYPES.register("herring",
            () -> EntityType.Builder.of(HerringEntity::new, MobCategory.WATER_AMBIENT)
                    .sized(0.3f, 0.3f)
                    .build(CreateBitterballen.asResource("herring").toString())
    );

    public static void register() {
        ENTITY_TYPES.register();
        FabricDefaultAttributeRegistry.register(HERRING.get(), HerringEntity.createAttributes().build());
    }
}
