package createbicyclesbitterballen.index;


import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import createbicyclesbitterballen.CreateBicBitModTabs;
import createbicyclesbitterballen.block.mechanicalfryer.MechanicalFryer;
import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;

import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static createbicyclesbitterballen.CreateBicBitMod.REGISTRATE;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;

public class BlockRegistry {
	static { REGISTRATE.creativeModeTab(() -> CreateBicBitModTabs.CREATIVE_TAB); }
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



		public static void register() {

	}
}