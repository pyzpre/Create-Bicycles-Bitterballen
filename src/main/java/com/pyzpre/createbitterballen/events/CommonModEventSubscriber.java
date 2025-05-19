package com.pyzpre.createbitterballen.events;

import com.pyzpre.createbitterballen.CreateBitterballen;
import com.pyzpre.createbitterballen.entity.HerringEntity;
import com.pyzpre.createbitterballen.index.EntityRegistry;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = CreateBitterballen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonModEventSubscriber {
    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            SpawnPlacements.register(EntityRegistry.HERRING.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.WORLD_SURFACE, HerringEntity::canSpawnHere);
        });
    }
}
