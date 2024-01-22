package pyzpre.createbicyclesbitterballen.index;


import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import pyzpre.createbicyclesbitterballen.block.mechanicalfryer.MechanicalFryer;
import pyzpre.createbicyclesbitterballen.block.sunflower.SunflowerStem;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static pyzpre.createbicyclesbitterballen.CreateBitterballen.REGISTRATE;

public class BlockRegistry {
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

	public static final BlockEntry<GlassBlock> CRYSTALLISED_OIL =
			REGISTRATE.block("crystallised_oil", GlassBlock::new)
					.properties(p -> p.lightLevel(s -> 10))
					.properties(p -> p.instrument(NoteBlockInstrument.HAT).strength(1F).sound(SoundType.GLASS).noOcclusion().isValidSpawn((state, reader, pos, entity) -> false).isRedstoneConductor((state, world, pos) -> false) .isSuffocating((state, reader, pos) -> false).isViewBlocking((state, reader, pos) -> false))
					.transform(pickaxeOnly())
					.transform(BlockStressDefaults.setImpact(4.0))
					.item()
					.build()
					.lang("Crystallised Oil")
					.register();
	public static final BlockEntry<SunflowerStem> SUNFLOWERSTEM =
			REGISTRATE.block("sunflowerstem", SunflowerStem::new)
					.properties(p -> p.noOcclusion().strength(1.0f))
					.properties(p -> p.sound(SoundType.GRASS))
					.properties(p -> p.offsetType(BlockBehaviour.OffsetType.XZ).ignitedByLava().pushReaction(PushReaction.DESTROY).noCollission().instabreak().mapColor(MapColor.PLANT))
					.item()
					.build()
					.lang("Sunflower Stem")
					.register();



	public static void register() {

	}
}