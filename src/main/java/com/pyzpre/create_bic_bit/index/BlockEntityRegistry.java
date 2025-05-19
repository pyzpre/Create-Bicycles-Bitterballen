package com.pyzpre.create_bic_bit.index;

import com.pyzpre.create_bic_bit.block.mechanicalfryer.FryerInstance;
import com.pyzpre.create_bic_bit.block.mechanicalfryer.MechanicalFryerEntity;
import com.pyzpre.create_bic_bit.block.mechanicalfryer.MechanicalFryerRenderer;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import static com.pyzpre.create_bic_bit.CreateBitterballen.REGISTRATE;
public class BlockEntityRegistry {
    public static final BlockEntityEntry<MechanicalFryerEntity> MECHANICAL_FRYER =
            REGISTRATE.blockEntity("mechanical_fryer", MechanicalFryerEntity::new)
                      .instance(() -> FryerInstance::new)
                      .validBlocks(BlockRegistry.MECHANICAL_FRYER)
                      .renderer(() -> MechanicalFryerRenderer::new)
                      .register();
    public static void register() {
    }
}
