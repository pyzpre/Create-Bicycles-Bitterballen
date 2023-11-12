package createbicyclesbitterballen.index;


import com.simibubi.create.foundation.ponder.PonderRegistrationHelper;
import createbicyclesbitterballen.ponder.FryerScenes;

public class PonderIndex {
    static final PonderRegistrationHelper HELPER = new PonderRegistrationHelper("create_bic_bit");

    public static void register() {
        HELPER.forComponents(BlockRegistry.MECHANICAL_FRYER)
                .addStoryBoard("frying", FryerScenes::frying);
    }
}

