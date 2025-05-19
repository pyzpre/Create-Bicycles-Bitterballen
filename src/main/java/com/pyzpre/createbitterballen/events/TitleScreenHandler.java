package com.pyzpre.createbitterballen.events;

import com.pyzpre.createbitterballen.util.ConfigHandler;
import com.pyzpre.createbitterballen.util.WarningMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TitleScreenHandler {

    @SubscribeEvent
    public static void onTitleScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof TitleScreen && !ConfigHandler.isWarningShown()) {
            Minecraft mc = Minecraft.getInstance();

            // Show the warning message screen
            mc.setScreen(new WarningMessage(event.getScreen(), ""));

            // Set the warning as shown in the persistent config
            ConfigHandler.setWarningShown(true);
        }
    }
}
