package com.pyzpre.createbitterballen.events;

import com.pyzpre.createbitterballen.index.ItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;

public class LootTables {

    private static final ResourceLocation FISHING_FISH = ResourceLocation.fromNamespaceAndPath("minecraft", "gameplay/fishing/fish");

    public static void register() {
        NeoForge.EVENT_BUS.addListener(LootTables::onLootTableLoad);
    }

    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (event.getName().equals(FISHING_FISH)) {
            LootPool.Builder poolBuilder = LootPool.lootPool()
                    .name("bitterballen_injected_fish")
                    .add(LootItem.lootTableItem(ItemRegistry.RAW_HERRING.get()))
                    .setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(0.10f)); // 10% chance

            event.getTable().addPool(poolBuilder.build());
        }
    }
}
