package com.pyzpre.createbitterballen;

import com.pyzpre.createbitterballen.events.EntityEffectHandler;

import com.pyzpre.createbitterballen.events.SunflowerInteractionHandler;
import com.pyzpre.createbitterballen.index.*;
import com.pyzpre.createbitterballen.ponder.BitterOrbPonderPlugin;
import com.pyzpre.createbitterballen.util.ConfigHandler;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Random;

@Mod(CreateBitterballen.MOD_ID)
public class CreateBitterballen {

	public static final String MOD_ID = "create_bic_bit";
	public static final Logger LOGGER = LogManager.getLogger();
	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(CreateBitterballen.MOD_ID)
			.setTooltipModifierFactory(item ->
					new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
							.andThen(TooltipModifier.mapNull(KineticStats.create(item)))
			);

	public static final Random RANDOM = new Random();

	public CreateBitterballen() {

		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		EntityRegistry.register(eventBus);
		SoundsRegistry.prepare();
		PartialsRegistry.init();
		BlockRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
		BlockEntityRegistry.register();
		MinecraftForge.EVENT_BUS.register(new FluidRegistry());
		MinecraftForge.EVENT_BUS.register(new EntityEffectHandler());
		ItemRegistry.register();
		EffectRegistry.register(eventBus);
		RecipeRegistry.register(eventBus);
		CreateBitterballenTabs.register(eventBus);
		REGISTRATE.registerEventListeners(eventBus);
		ConfigHandler.loadConfig(FMLPaths.CONFIGDIR.get());
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);

		eventBus.addListener(SoundsRegistry::register);

		MinecraftForge.EVENT_BUS.register(SunflowerInteractionHandler.class);



	}

	private void setup(final FMLCommonSetupEvent event) {
		FluidRegistry.registerFluidInteractions();

	}

	private void setupClient(final FMLClientSetupEvent event) {
		ItemBlockRenderTypes.setRenderLayer(FluidRegistry.FRYING_OIL.get(), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(FluidRegistry.FRYING_OIL.getSource(), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(BlockRegistry.CRYSTALLISED_OIL.get(), RenderType.translucent());
		PonderIndex.addPlugin(new BitterOrbPonderPlugin());
	}
	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(MOD_ID, path);
	}


}
