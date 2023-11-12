package createbicyclesbitterballen.index;

import com.jozufozu.flywheel.core.PartialModel;

import createbicyclesbitterballen.CreateBicBitMod;


public class PartialsRegistry {
    public static final PartialModel
    MECHANICAL_FRYER_HEAD = block("mechanicalfryer/head");
    private static PartialModel block(String path) {
        return new PartialModel(CreateBicBitMod.asResource("block/" + path));
    }
    public static void load() {
        // init static fields
    }
}