package com.pyzpre.create_bic_bit.index;


import com.pyzpre.create_bic_bit.ponder.FryerScenes;
import com.simibubi.create.foundation.ponder.PonderRegistrationHelper;

public class PonderIndex {
    static final PonderRegistrationHelper HELPER = new PonderRegistrationHelper("create_bic_bit");

    public static void register() {
        HELPER.forComponents(BlockRegistry.MECHANICAL_FRYER)
                .addStoryBoard("frying", FryerScenes::frying);
    }
}

