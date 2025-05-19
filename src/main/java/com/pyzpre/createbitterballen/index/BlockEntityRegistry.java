package com.pyzpre.createbitterballen.index;

import com.pyzpre.createbitterballen.block.mechanicalfryer.FryerInstance;
import com.pyzpre.createbitterballen.block.mechanicalfryer.MechanicalFryerEntity;
import com.pyzpre.createbitterballen.block.mechanicalfryer.MechanicalFryerRenderer;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import static com.pyzpre.createbitterballen.CreateBitterballen.REGISTRATE;
public class BlockEntityRegistry {
    public static final BlockEntityEntry<MechanicalFryerEntity> MECHANICAL_FRYER =
            REGISTRATE.blockEntity("mechanical_fryer", MechanicalFryerEntity::new)
                      .visual(() -> FryerInstance::new)
                      .validBlocks(BlockRegistry.MECHANICAL_FRYER)
                      .renderer(() -> MechanicalFryerRenderer::new)
                      .register();
    public static void register() {
    }
}
