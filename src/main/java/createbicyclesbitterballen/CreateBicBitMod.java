
package createbicyclesbitterballen;

import java.util.Random;

import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import createbicyclesbitterballen.index.BlockEntityRegistry;
import createbicyclesbitterballen.config.ConfigRegistry;
import createbicyclesbitterballen.index.CreateBicBitModFluids;
import createbicyclesbitterballen.index.FluidTypesRegistry;
import createbicyclesbitterballen.index.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import com.simibubi.create.foundation.item.TooltipHelper.Palette;



import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.MinecraftForge;


import createbicyclesbitterballen.index.BlockRegistry;

import com.simibubi.create.foundation.data.CreateRegistrate;

@Mod("create_bic_bit")
public class CreateBicBitMod {


	public static final Logger LOGGER = LogManager.getLogger(CreateBicBitMod.class);
	public static final String MODID = "create_bic_bit";
	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MODID);
	@Deprecated
	public static final Random RANDOM = new Random();

	static {
		REGISTRATE.setTooltipModifierFactory(item -> {
			return new ItemDescription.Modifier(item, Palette.STANDARD_CREATE)
					.andThen(TooltipModifier.mapNull(KineticStats.create(item)));
		});
	}
	public CreateBicBitMod() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
		MinecraftForge.EVENT_BUS.register(this);

		// Register components using Registrate

		SoundsRegistry.prepare();
		BlockRegistry.register();
		CreateBicBitModItems.register();
		CreateBicBitModTabs.register(modEventBus);
		RecipeRegistry.register(modEventBus);
		BlockEntityRegistry.register();
		PonderIndex.register();
		CreateBicBitModFluids.REGISTRY.register(bus);
		FluidTypesRegistry.REGISTRY.register(bus);
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
				() -> PartialsRegistry::load);
		modEventBus.addListener(SoundsRegistry::register);


		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ConfigRegistry.SERVER_SPEC, "create_bic_bit-server.toml");
		MinecraftForge.EVENT_BUS.register(this);
		REGISTRATE.registerEventListeners(modEventBus);




	}
	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(MODID, path);
	}
}