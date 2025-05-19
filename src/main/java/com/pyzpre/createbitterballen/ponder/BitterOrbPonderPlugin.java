package com.pyzpre.createbitterballen.ponder;

import com.pyzpre.createbitterballen.CreateBitterballen;
import com.pyzpre.createbitterballen.index.PonderIndex;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class BitterOrbPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return CreateBitterballen.MOD_ID; // Your mod's ID
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        // Your scene registrations
        PonderIndex.register(helper);
    }

}
