package pyzpre.createbicyclesbitterballen.index;

import com.jozufozu.flywheel.core.PartialModel;
import pyzpre.createbicyclesbitterballen.CreateBitterballen;


public class PartialsRegistry {
    public static final PartialModel
    MECHANICAL_FRYER_HEAD = block("mechanicalfryer/head");
    private static PartialModel block(String path) {
        return new PartialModel(CreateBitterballen.asResource("block/" + path));
    }
    public static void init() {
    }
}