package createbicyclesbitterballen.index;

import createbicyclesbitterballen.index.BlockRegistry;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import createbicyclesbitterballen.block.mechanicalfryer.FryerInstance;
import createbicyclesbitterballen.block.mechanicalfryer.MechanicalFryerEntity;
import createbicyclesbitterballen.block.mechanicalfryer.MechanicalFryerRenderer;


import static createbicyclesbitterballen.CreateBicBitMod.REGISTRATE;

public class BlockEntityRegistry {

    public static final BlockEntityEntry<MechanicalFryerEntity> MECHANICAL_FRYER = REGISTRATE
            .blockEntity("mechanical_fryer", MechanicalFryerEntity::new)
            .instance(() -> FryerInstance::new)
            .validBlocks(BlockRegistry.MECHANICAL_FRYER)
            .renderer(() -> MechanicalFryerRenderer::new)
            .register();

    // You can add more BlockEntityEntries following the above format.

    public static void register() {
    }
}
