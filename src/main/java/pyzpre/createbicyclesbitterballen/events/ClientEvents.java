package pyzpre.createbicyclesbitterballen.events;

import com.mojang.blaze3d.shaders.FogShape;
import com.simibubi.create.infrastructure.config.AllConfigs;
import io.github.fabricators_of_create.porting_lib.event.client.FogEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import pyzpre.createbicyclesbitterballen.index.FluidsRegistry;

public class ClientEvents {
    public static boolean getFogDensity(FogRenderer.FogMode mode, FogType type, Camera camera, float partialTick, float renderDistance, float nearDistance, float farDistance, FogShape shape, FogEvents.FogData fogData) {
        Level level = Minecraft.getInstance().level;
        BlockPos blockPos = camera.getBlockPosition();
        FluidState fluidState = level.getFluidState(blockPos);
        if (camera.getPosition().y >= blockPos.getY() + fluidState.getHeight(level, blockPos))
            return false;
        Fluid fluid = fluidState.getType();
        Entity entity = camera.getEntity();

        if (FluidsRegistry.FRYING_OIL.get()
                .isSame(fluid)) {
            fogData.scaleFarPlaneDistance(0.5f);
            return true;
        }


        if (entity.isSpectator())
            return false;

        return false;
    }

    public static void getFogColor(FogEvents.ColorData event, float partialTicks) {
        Camera info = event.getCamera();
        Level level = Minecraft.getInstance().level;
        BlockPos blockPos = info.getBlockPosition();
        FluidState fluidState = level.getFluidState(blockPos);
        if (info.getPosition().y > blockPos.getY() + fluidState.getHeight(level, blockPos))
            return;

        Fluid fluid = fluidState.getType();

        if (FluidsRegistry.FRYING_OIL.get()
                .isSame(fluid)) {
            event.setRed(237 / 255f);
            event.setGreen(196 / 255f);
            event.setBlue(131 / 255f);
            return;
        }

    }
    public static void register() {
        com.simibubi.create.foundation.events.ClientEvents.ModBusEvents.registerClientReloadListeners();

        FogEvents.RENDER_FOG.register(ClientEvents::getFogDensity);
        FogEvents.SET_COLOR.register(ClientEvents::getFogColor);

    }
}
