package com.pyzpre.createbitterballen.index;


import com.pyzpre.createbitterballen.ponder.FryerScenes;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import com.tterrag.registrate.util.entry.RegistryEntry;

public class PonderIndex {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {

        PonderSceneRegistrationHelper<ItemProviderEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.forComponents(BlockRegistry.MECHANICAL_FRYER)
                .addStoryBoard("frying", FryerScenes::frying);
    }
}

