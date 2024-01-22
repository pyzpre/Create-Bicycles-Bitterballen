package pyzpre.createbicyclesbitterballen.events;

import com.simibubi.create.AllFluids;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import io.github.fabricators_of_create.porting_lib.event.common.FluidPlaceBlockCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import pyzpre.createbicyclesbitterballen.index.FluidsRegistry;

public class CommonEvents {

    public static BlockState whenFluidsMeet(LevelAccessor world, BlockPos pos, BlockState blockState) {
        FluidState fluidState = blockState.getFluidState();

        if (fluidState.isSource() && FluidHelper.isLava(fluidState.getType()))
            return null;

        for (Direction direction : Iterate.directions) {
            FluidState metFluidState =
                    fluidState.isSource() ? fluidState : world.getFluidState(pos.relative(direction));
            if (!metFluidState.is(FluidTags.WATER))
                continue;
            BlockState lavaInteraction = FluidsRegistry.getLavaInteraction(metFluidState);
            if (lavaInteraction == null)
                continue;
            return lavaInteraction;
        }
        return null;
    }



    public static void register() {

        FluidPlaceBlockCallback.EVENT.register(CommonEvents::whenFluidsMeet);
    }
}
