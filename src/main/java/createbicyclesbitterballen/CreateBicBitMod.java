
package createbicyclesbitterballen;

import java.util.Random;

import createbicyclesbitterballen.fluid.FluidsRegistry;
import createbicyclesbitterballen.index.BlockEntityRegistry;
import createbicyclesbitterballen.index.BlockRegistry;
import createbicyclesbitterballen.index.*;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.MinecraftForge;

import com.simibubi.create.foundation.data.CreateRegistrate;

@Mod(CreateBicBitMod.MOD_ID)
public class CreateBicBitMod
{

	public static final String MOD_ID = "create_bic_bit";

	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(CreateBicBitMod.MOD_ID);
	@Deprecated
	public static final Random RANDOM = new Random();

	public CreateBicBitMod()
	{
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		REGISTRATE.registerEventListeners(eventBus);

		CreateBicBitModTabs.register(modEventBus);
		SoundsRegistry.prepare();
		PartialsRegistry.init();
		BlockEntityRegistry.register();
		BlockRegistry.register();
		CreateBicBitModItems.register();
		FluidsRegistry.register();

		RecipeRegistry.register(eventBus);
		eventBus.addListener(this::clientSetup);
		modEventBus.addListener(CreateBicBitMod::clientInit);
		modEventBus.addListener(SoundsRegistry::register);
		MinecraftForge.EVENT_BUS.register(new FluidsRegistry());
		eventBus.addListener(CreateBicBitMod::init);
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(SunflowerInteractionHandler.class);


	}

	private void clientSetup(final FMLClientSetupEvent event) {
		ItemBlockRenderTypes.setRenderLayer(BlockRegistry.CRYSTALLISED_OIL.get(), RenderType.translucent());
	}

	public static void clientInit(final FMLClientSetupEvent event) {
		ItemBlockRenderTypes.setRenderLayer(FluidsRegistry.FRYING_OIL.get(), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(FluidsRegistry.FRYING_OIL.getSource(), RenderType.translucent());
		PonderIndex.register();
		PartialsRegistry.init();
	}
	public static void init(final FMLCommonSetupEvent event) {
		PotatoCannonProjectiles.register();
		FluidsRegistry.registerFluidInteractions();
	}


	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(CreateBicBitMod.MOD_ID, path);
	}

}