package com.pyzpre.createbitterballen.events;

import com.pyzpre.createbitterballen.util.ConfigHandler;
import com.pyzpre.createbitterballen.util.WarningMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

public class TitleScreenHandler {
    public static void onTitleScreenInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
        if (screen instanceof TitleScreen && !ConfigHandler.isWarningShown()) {
            Minecraft mc = Minecraft.getInstance();

            // Show the warning message screen
            mc.setScreen(new WarningMessage(screen, ""));

            // Set the warning as shown in the persistent config
            ConfigHandler.setWarningShown(true);
        }
    }
}
