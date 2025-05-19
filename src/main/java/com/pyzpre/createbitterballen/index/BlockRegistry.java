package com.pyzpre.createbitterballen.index;

import com.pyzpre.createbitterballen.block.cheese.*;
import com.pyzpre.createbitterballen.block.mechanicalfryer.MechanicalFryer;
import com.pyzpre.createbitterballen.block.sunflower.SunflowerStem;
import com.pyzpre.createbitterballen.block.sunflower.VanillaSunflowerBlock;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.infrastructure.config.CStress;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.pyzpre.createbitterballen.CreateBitterballen.REGISTRATE;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class BlockRegistry {
    public static final BlockEntry<MechanicalFryer> MECHANICAL_FRYER =
            REGISTRATE.block("mechanical_fryer", MechanicalFryer::new)
                    .initialProperties(SharedProperties::copperMetal)
                    .properties(p -> p.noOcclusion().strength(2.0f))
                    .transform(pickaxeOnly())
                    .onRegister((block) -> {
                        BlockStressValues.IMPACTS.register(block, () -> 4.0);
                    })
                    .blockstate(BlockStateGen.horizontalBlockProvider(true))
                    .item(AssemblyOperatorBlockItem::new)
                    .transform(customItemModel())
                    .register();
    public static final BlockEntry<SunflowerStem> SUNFLOWERSTEM =
            REGISTRATE.block("sunflower_seeds", SunflowerStem::new)
                    .properties(p -> p.noOcclusion().strength(1.0f))
                    .properties(p -> p.sound(SoundType.GRASS))
                    .properties(p -> p.offsetType(BlockBehaviour.OffsetType.XZ).ignitedByLava().pushReaction(PushReaction.DESTROY).noCollission().instabreak().mapColor(MapColor.PLANT))
                    .item()
                    .build()
                    .lang("Sunflower Seeds")
                    .register();
    public static final BlockEntry<UnripeCheeseBlock> UNRIPE_CHEESE =
            REGISTRATE.block("unripe_cheese", UnripeCheeseBlock::new)
                    .initialProperties(() -> Blocks.CAKE)
                    .properties(p -> p.sound(SoundType.WOOD))
                    .transform(pickaxeOnly())
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
                    .item()
                    .build()
                    .lang("Waxed Aged Cheese")
                    .item(AssemblyOperatorBlockItem::new)
                    .transform(customItemModel())
                    .register();


    public static final BlockEntry<GlassBlock> CRYSTALLISED_OIL =
            REGISTRATE.block("crystallised_oil", GlassBlock::new)
                    .properties(p -> p.lightLevel(s -> 10))
                    .properties(p -> p.instrument(NoteBlockInstrument.HAT).strength(1F).sound(SoundType.GLASS).noOcclusion().isValidSpawn((state, reader, pos, entity) -> false).isRedstoneConductor((state, world, pos) -> false) .isSuffocating((state, reader, pos) -> false).isViewBlocking((state, reader, pos) -> false))
                    .transform(pickaxeOnly())
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
