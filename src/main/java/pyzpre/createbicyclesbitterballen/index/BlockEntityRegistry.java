package pyzpre.createbicyclesbitterballen.index;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import pyzpre.createbicyclesbitterballen.block.mechanicalfryer.FryerInstance;
import pyzpre.createbicyclesbitterballen.block.mechanicalfryer.MechanicalFryerEntity;
import pyzpre.createbicyclesbitterballen.block.mechanicalfryer.MechanicalFryerRenderer;

import static pyzpre.createbicyclesbitterballen.CreateBitterballen.REGISTRATE;

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
