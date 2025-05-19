package com.pyzpre.create_bic_bit;

//import com.pyzpre.createbitterballen.events.EntityEffectHandler;
//
//import com.pyzpre.createbitterballen.events.SunflowerInteractionHandler;

import com.pyzpre.create_bic_bit.events.EntityEffectHandler;
import com.pyzpre.create_bic_bit.events.SunflowerInteractionHandler;
import com.pyzpre.create_bic_bit.index.*;
import com.pyzpre.create_bic_bit.util.ConfigHandler;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

@Mod(CreateBitterballen.MOD_ID)
public class CreateBitterballen {

  public static final String MOD_ID = "create_bic_bit";
  public static final Logger LOGGER = LogManager.getLogger();
  public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(CreateBitterballen.MOD_ID);
  public static final Random RANDOM = new Random();

  public CreateBitterballen() {

    IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

    EntityRegistry.register(eventBus);
    SoundsRegistry.prepare();
    PartialsRegistry.init();
    BlockEntityRegistry.register();
    BlockRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
    MinecraftForge.EVENT_BUS.register(new FluidRegistry());
    MinecraftForge.EVENT_BUS.register(new EntityEffectHandler());
    ItemRegistry.register();
    EffectRegistry.register(eventBus);
    RecipeRegistry.register(eventBus);
    CreateBitterballenTabs.register();
    REGISTRATE.registerEventListeners(eventBus);
    ConfigHandler.loadConfig(FMLPaths.CONFIGDIR.get());
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);

    eventBus.addListener(SoundsRegistry::register);

    MinecraftForge.EVENT_BUS.register(SunflowerInteractionHandler.class);



  }

  private void setup(final FMLCommonSetupEvent event) {
    PotatoCannonProjectiles.register();
    FluidRegistry.registerFluidInteractions();
  }

  private void setupClient(final FMLClientSetupEvent event) {
    ItemBlockRenderTypes.setRenderLayer(FluidRegistry.FRYING_OIL.get(), RenderType.translucent());
    ItemBlockRenderTypes.setRenderLayer(FluidRegistry.FRYING_OIL.getSource(), RenderType.translucent());
    PonderIndex.register();
  }
  public static ResourceLocation asResource(String path) {
    return new ResourceLocation(MOD_ID, path);
  }


}
