package com.pyzpre.createbitterballen.events;

import com.pyzpre.createbitterballen.CreateBitterballen;
import com.pyzpre.createbitterballen.index.BlockRegistry;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = CreateBitterballen.MOD_ID)
public class LootTables {

    public static final ResourceLocation FISH = register("gameplay/fishing/fish");
    private static final ResourceLocation SUNFLOWER = new ResourceLocation("minecraft", "blocks/sunflower");

    private static ResourceLocation register(String path) {
        return BuiltInLootTables.register(new ResourceLocation(CreateBitterballen.MOD_ID, path));
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation name = event.getName();
        if (name.equals(BuiltInLootTables.FISHING)) {
            LootPool pool = event.getTable().getPool("main");
            if (pool != null) {
                addEntry(pool, getInjectEntry(FISH, 25, -1));
            }
        }
        if (event.getName().equals(SUNFLOWER)) {
            StatePropertiesPredicate.Builder lowerHalfCondition = StatePropertiesPredicate.Builder.properties()
                    .hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);

            LootItemBlockStatePropertyCondition.Builder condition =
                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.SUNFLOWER)
                            .setProperties(lowerHalfCondition);

            LootPool sunflowerStemPool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .when(condition)
                    .add(LootItem.lootTableItem(BlockRegistry.SUNFLOWERSTEM.get()))
                    .build();

            LootTable table = event.getTable();
            table.addPool(sunflowerStemPool);
        }
    }


    private static LootPoolEntryContainer getInjectEntry(ResourceLocation location, int weight, int quality) {
        return LootTableReference.lootTableReference(location).setWeight(weight).setQuality(quality).build();
    }

    private static void addEntry(LootPool pool, LootPoolEntryContainer entry) {
        try {
            Field entries = ObfuscationReflectionHelper.findField(LootPool.class, "f_79023_");
            entries.setAccessible(true);

            LootPoolEntryContainer[] lootPoolEntriesArray = (LootPoolEntryContainer[]) entries.get(pool);
            ArrayList<LootPoolEntryContainer> newLootEntries = new ArrayList<>(List.of(lootPoolEntriesArray));

            if (newLootEntries.stream().anyMatch(e -> e == entry)) {
                throw new RuntimeException("Attempted to add a duplicate entry to pool: " + entry);
            }

            newLootEntries.add(entry);

            LootPoolEntryContainer[] newLootEntriesArray = new LootPoolEntryContainer[newLootEntries.size()];
            newLootEntries.toArray(newLootEntriesArray);
            entries.set(pool, newLootEntriesArray);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

