package com.pyzpre.create_bic_bit.index;

import com.pyzpre.create_bic_bit.CreateBitterballenTabs;
import com.pyzpre.create_bic_bit.block.cheese.*;
import com.pyzpre.create_bic_bit.block.mechanicalfryer.MechanicalFryer;
import com.pyzpre.create_bic_bit.block.sunflower.SunflowerStem;
import com.pyzpre.create_bic_bit.block.sunflower.VanillaSunflowerBlock;
import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.pyzpre.create_bic_bit.CreateBitterballen.REGISTRATE;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class BlockRegistry {
    static { REGISTRATE.creativeModeTab(() -> CreateBitterballenTabs.CREATIVE_TAB); }
    public static final BlockEntry<MechanicalFryer> MECHANICAL_FRYER =
            REGISTRATE.block("mechanical_fryer", MechanicalFryer::new)
                    .initialProperties(SharedProperties::copperMetal)
                    .properties(p -> p.noOcclusion().strength(2.0f))
                    .transform(pickaxeOnly())
                    .blockstate(BlockStateGen.horizontalBlockProvider(true))
                    .transform(BlockStressDefaults.setImpact(4.0))
                    .item(AssemblyOperatorBlockItem::new)
                    .transform(customItemModel())
                    .register();
    public static final BlockBehaviour.Properties SUNFLOWER_PROPERTIES =
            BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ);

    public static final BlockEntry<SunflowerStem> SUNFLOWERSTEM =
            REGISTRATE.block("sunflower_seeds", SunflowerStem::new)
                    .properties(p -> SUNFLOWER_PROPERTIES)
                    .item()
                    .build()
                    .lang("Sunflower Seeds")
                    .register();

    public static final BlockEntry<UnripeCheeseBlock> UNRIPE_CHEESE =
            REGISTRATE.block("unripe_cheese", UnripeCheeseBlock::new)
                    .initialProperties(() -> Blocks.CAKE)
                    .properties(p -> p.sound(SoundType.WOOD))
                    .transform(pickaxeOnly())
                    .transform(BlockStressDefaults.setImpact(4.0))
                    .item()
                    .build()
                    .lang("Unripe Cheese")
                    .item(AssemblyOperatorBlockItem::new)
                    .transform(customItemModel())
                    .register();
    public static final BlockEntry<WaxedUnripeCheeseBlock> WAXED_UNRIPE_CHEESE =
            REGISTRATE.block("waxed_unripe_cheese", WaxedUnripeCheeseBlock::new)
                    .initialProperties(() -> Blocks.CAKE)
                    .properties(p -> p.sound(SoundType.WOOD))
                    .transform(pickaxeOnly())
                    .transform(BlockStressDefaults.setImpact(4.0))
                    .item()
                    .build()
                    .lang("Waxed Unripe Cheese")
                    .item(AssemblyOperatorBlockItem::new)
                    .transform(customItemModel())
                    .register();
    public static final BlockEntry<YoungCheeseBlock> YOUNG_CHEESE =
            REGISTRATE.block("young_cheese", YoungCheeseBlock::new)
                    .initialProperties(() -> Blocks.CAKE)
                    .properties(p -> p.sound(SoundType.WOOD))
                    .transform(pickaxeOnly())
                    .transform(BlockStressDefaults.setImpact(4.0))
                    .item()
                    .build()
                    .lang("Young Cheese")
                    .item(AssemblyOperatorBlockItem::new)
                    .transform(customItemModel())
                    .register();
    public static final BlockEntry<WaxedYoungCheeseBlock> WAXED_YOUNG_CHEESE =
            REGISTRATE.block("waxed_young_cheese", WaxedYoungCheeseBlock::new)
                    .initialProperties(() -> Blocks.CAKE)
                    .properties(p -> p.sound(SoundType.WOOD))
                    .transform(pickaxeOnly())
                    .transform(BlockStressDefaults.setImpact(4.0))
                    .item()
                    .build()
                    .lang("Waxed Young Cheese")
                    .item(AssemblyOperatorBlockItem::new)
                    .transform(customItemModel())
                    .register();
    public static final BlockEntry<AgedCheeseBlock> AGED_CHEESE =
            REGISTRATE.block("aged_cheese", AgedCheeseBlock::new)
                    .initialProperties(() -> Blocks.CAKE)
                    .properties(p -> p.sound(SoundType.WOOD))
                    .transform(pickaxeOnly())
                    .transform(BlockStressDefaults.setImpact(4.0))
                    .item()
                    .build()
                    .lang("Aged Cheese")
                    .item(AssemblyOperatorBlockItem::new)
                    .transform(customItemModel())
                    .register();
    public static final BlockEntry<WaxedAgedCheeseBlock> WAXED_AGED_CHEESE =
            REGISTRATE.block("waxed_aged_cheese", WaxedAgedCheeseBlock::new)
                    .initialProperties(() -> Blocks.CAKE)
                    .properties(p -> p.sound(SoundType.WOOD))
                    .transform(pickaxeOnly())
                    .transform(BlockStressDefaults.setImpact(4.0))
                    .item()
                    .build()
                    .lang("Waxed Aged Cheese")
                    .item(AssemblyOperatorBlockItem::new)
                    .transform(customItemModel())
                    .register();


    public static final BlockEntry<GlassBlock> CRYSTALLISED_OIL =
            REGISTRATE.block("crystallised_oil", GlassBlock::new)
                    .properties(p -> p.lightLevel(s -> 10))
                    .properties(p -> p.strength(1F).sound(SoundType.GLASS).noOcclusion().isValidSpawn((state, reader, pos, entity) -> false).isRedstoneConductor((state, world, pos) -> false) .isSuffocating((state, reader, pos) -> false).isViewBlocking((state, reader, pos) -> false))
                    .transform(pickaxeOnly())
                    .transform(BlockStressDefaults.setImpact(4.0))
                    .item()
                    .build()
                    .lang("Crystallised Oil")
                    .register();


    // Creating a DeferredRegister to overwrite vanilla blocks
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "minecraft");

    // Replacing the vanilla sunflower block with my own
    public static final RegistryObject<Block> SUNFLOWER = BLOCKS.register("sunflower",
            () -> new VanillaSunflowerBlock(BlockBehaviour.Properties.copy(Blocks.SUNFLOWER))
    );
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
