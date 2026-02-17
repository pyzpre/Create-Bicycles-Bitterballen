package com.pyzpre.createbitterballen;

import com.pyzpre.createbitterballen.entity.HerringEntity;
import com.pyzpre.createbitterballen.events.EntityEffectHandler;

import com.pyzpre.createbitterballen.events.LootTables;
import com.pyzpre.createbitterballen.events.SunflowerInteractionHandler;
import com.pyzpre.createbitterballen.index.*;
import com.pyzpre.createbitterballen.util.ConfigHandler;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import io.github.fabricators_of_create.porting_lib.entity.events.EntityEvents;
import io.github.fabricators_of_create.porting_lib.entity.events.PlayerInteractionEvents;
import net.createmod.catnip.lang.FontHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.entity.EntityPickInteractionAware;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class CreateBitterballen implements ModInitializer {

	public static final String MOD_ID = "create_bic_bit";
	public static final Logger LOGGER = LogManager.getLogger();
	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(CreateBitterballen.MOD_ID)
			.setTooltipModifierFactory(item ->
					new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
							.andThen(TooltipModifier.mapNull(KineticStats.create(item)))
			);

	public static final Random RANDOM = new Random();

	public void onInitialize() {
		BlockRegistry.register();
		BlockEntityRegistry.register();
		FluidRegistry.register();
		ItemRegistry.register();
		REGISTRATE.register();

		EntityRegistry.register();
		SoundsRegistry.prepare();
		PartialsRegistry.init();

		FluidRegistry.registerFluidInteractions();
		ServerTickEvents.START_WORLD_TICK.register(EntityEffectHandler::onLevelTick);
		PlayerInteractionEvents.INTERACT_ENTITY_GENERAL.register(EntityEffectHandler::onEntityInteract);
		EntityEvents.ON_JOIN_WORLD.register(EntityEffectHandler::onEntityJoinLevel);
		EffectRegistry.register();
		RecipeRegistry.register();
		CreateBitterballenTabs.register();
		ConfigHandler.loadConfig(FabricLoader.getInstance().getConfigDir());

		SoundsRegistry.register();

		UseBlockCallback.EVENT.register(SunflowerInteractionHandler::onRightClickBlock);
		LootTableEvents.MODIFY.register(LootTables::onLootTableLoad);

		SpawnPlacements.register(EntityRegistry.HERRING.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.WORLD_SURFACE, HerringEntity::canSpawnHere);
		BiomeModifications.addSpawn(
				context -> context.hasTag(TagKey.create(Registries.BIOME, new ResourceLocation(CreateBitterballen.MOD_ID, "herring_spawn"))),
				MobCategory.WATER_AMBIENT,
				EntityRegistry.HERRING.get(),
				35,
				3,
				5
		);
	}

	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(MOD_ID, path);
	}


}
