package com.pyzpre.createbitterballen.index;


import com.pyzpre.createbitterballen.CreateBitterballen;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

public class PartialsRegistry {
    public static final PartialModel MECHANICAL_FRYER_HEAD = block("mechanicalfryer/head");

    private static PartialModel block(String path) {
        return PartialModel.of(CreateBitterballen.asResource("block/" + path));
    }

    public static void init() {
    }
}
