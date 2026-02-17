package com.pyzpre.createbitterballen.events;

import com.pyzpre.createbitterballen.CreateBitterballen;
import com.pyzpre.createbitterballen.index.BlockRegistry;
import net.fabricmc.fabric.api.loot.v2.LootTableSource;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class LootTables {

    public static final ResourceLocation FISH = new ResourceLocation(CreateBitterballen.MOD_ID, "gameplay/fishing/fish");
    private static final ResourceLocation SUNFLOWER = new ResourceLocation("minecraft", "blocks/sunflower");

    public static void onLootTableLoad(ResourceManager resourceManager, LootDataManager lootManager, ResourceLocation name, LootTable.Builder tableBuilder, LootTableSource source) {
        if (name.equals(BuiltInLootTables.FISHING)) {
            boolean first = true;
            tableBuilder.modifyPools(poolBuilder -> {
                if(first) {
                    poolBuilder.add(getInjectEntry(FISH, 25, -1));
                }
            });
        }
        if (name.equals(SUNFLOWER)) {
            StatePropertiesPredicate.Builder lowerHalfCondition = StatePropertiesPredicate.Builder.properties()
                    .hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);

            LootItemBlockStatePropertyCondition.Builder condition =
                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.SUNFLOWER)
                            .setProperties(lowerHalfCondition);

            LootPool.Builder sunflowerStemPool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .when(condition)
                    .add(LootItem.lootTableItem(BlockRegistry.SUNFLOWERSTEM.get()));

            tableBuilder.withPool(sunflowerStemPool);
        }
    }


    private static LootPoolEntryContainer.Builder<?> getInjectEntry(ResourceLocation location, int weight, int quality) {
        return LootTableReference.lootTableReference(location).setWeight(weight).setQuality(quality);
    }
}

