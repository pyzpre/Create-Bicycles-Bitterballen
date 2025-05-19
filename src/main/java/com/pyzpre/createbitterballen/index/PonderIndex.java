package com.pyzpre.createbitterballen.index;


import com.pyzpre.createbitterballen.ponder.FryerScenes;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.resources.ResourceLocation;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;

public class PonderIndex {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        // Register the scene for your block
        HELPER.forComponents(BlockRegistry.MECHANICAL_FRYER)
                .addStoryBoard("frying", FryerScenes::frying);
    }
}

