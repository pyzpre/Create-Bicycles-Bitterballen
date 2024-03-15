package pyzpre.createbicyclesbitterballen;

import com.simibubi.create.AllItems;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.include.com.google.common.collect.Lists;
import pyzpre.createbicyclesbitterballen.index.BlockRegistry;
import pyzpre.createbicyclesbitterballen.index.CreateBicBitModItems;
import pyzpre.createbicyclesbitterballen.index.FluidsRegistry;

import java.util.List;


public class CreateBicBitModTabs {
	public static final ResourceKey<CreativeModeTab> CREATIVE_TAB =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(CreateBitterballen.MOD_ID, "group"));

	public static void register() {
		List<Item> ITEMS = Lists.newArrayList();
		ITEMS.add(CreateBicBitModItems.SWEET_DOUGH.get());
		ITEMS.add(CreateBicBitModItems.KRUIDNOTEN.get());
		ITEMS.add(CreateBicBitModItems.SPECULAAS.get());
		ITEMS.add(CreateBicBitModItems.UNBAKED_STROOPWAFEL.get());
		ITEMS.add(CreateBicBitModItems.STROOPWAFEL.get());
		ITEMS.add(CreateBicBitModItems.WRAPPED_STROOPWAFEL.get());
		ITEMS.add(CreateBicBitModItems.CHOCOLATE_GLAZED_STROOPWAFEL.get());
		ITEMS.add(CreateBicBitModItems.WRAPPED_COATED_STROOPWAFEL.get());
		ITEMS.add(CreateBicBitModItems.OLIEBOLLEN.get());
		ITEMS.add(CreateBicBitModItems.RAW_KROKET.get());
		ITEMS.add(CreateBicBitModItems.KROKET.get());
		ITEMS.add(CreateBicBitModItems.KROKET_SANDWICH.get());
		ITEMS.add(CreateBicBitModItems.KETCHUP_TOPPED_KROKET_SANDWICH.get());
		ITEMS.add(CreateBicBitModItems.MAYONNAISE_TOPPED_KROKET_SANDWICH.get());
		ITEMS.add(CreateBicBitModItems.MAYONNAISE_KETCHUP_TOPPED_KROKET_SANDWICH.get());
		ITEMS.add(CreateBicBitModItems.RAW_BITTERBALLEN.get());
		ITEMS.add(CreateBicBitModItems.BITTERBALLEN.get());
		ITEMS.add(CreateBicBitModItems.RAW_FRIKANDEL.get());
		ITEMS.add(CreateBicBitModItems.FRIKANDEL.get());
		ITEMS.add(CreateBicBitModItems.FRIKANDEL_SANDWICH.get());
		ITEMS.add(CreateBicBitModItems.KETCHUP_TOPPED_FRIKANDEL_SANDWICH.get());
		ITEMS.add(CreateBicBitModItems.MAYONNAISE_TOPPED_FRIKANDEL_SANDWICH.get());
		ITEMS.add(CreateBicBitModItems.MAYONNAISE_KETCHUP_TOPPED_FRIKANDEL_SANDWICH.get());
		ITEMS.add(CreateBicBitModItems.RAW_FRIES.get());
		ITEMS.add(CreateBicBitModItems.FRIES.get());
		ITEMS.add(CreateBicBitModItems.WRAPPED_FRIES.get());
		ITEMS.add(CreateBicBitModItems.WRAPPED_KETCHUP_TOPPED_FRIES.get());
		ITEMS.add(CreateBicBitModItems.WRAPPED_MAYONNAISE_TOPPED_FRIES.get());
		ITEMS.add(CreateBicBitModItems.WRAPPED_MAYONNAISE_KETCHUP_TOPPED_FRIES.get());
		ITEMS.add(CreateBicBitModItems.RAW_CHURROS.get());
		ITEMS.add(CreateBicBitModItems.CHURROS.get());
		ITEMS.add(CreateBicBitModItems.WRAPPED_CHURROS.get());
		ITEMS.add(CreateBicBitModItems.STAMPPOT_BOWL.get());
		ITEMS.add(CreateBicBitModItems.ENDERBALL.get());
		ITEMS.add(CreateBicBitModItems.CRUSHED_NETHERWART.get());
		ITEMS.add(FluidsRegistry.KETCHUP.getBucket().get());
		ITEMS.add(FluidsRegistry.MAYONNAISE.getBucket().get());
		ITEMS.add(CreateBicBitModItems.CRUSHED_SUNFLOWER_SEEDS.get());
		ITEMS.add(CreateBicBitModItems.SUNFLOWER_SEEDS.get());
		ITEMS.add(CreateBicBitModItems.ROASTED_SUNFLOWER_SEEDS.get());
		ITEMS.add(BlockRegistry.CRYSTALLISED_OIL.get().asItem());
		ITEMS.add(FluidsRegistry.FRYING_OIL.getBucket().get());
		ITEMS.add(BlockRegistry.MECHANICAL_FRYER.get().asItem());
		ITEMS.add(CreateBicBitModItems.BASKET.get());
		ITEMS.add(CreateBicBitModItems.DIRTY_PAPER.get());


		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_TAB, FabricItemGroup.builder()
				.icon(() -> new ItemStack(CreateBicBitModItems.STROOPWAFEL))
				.title(Component.translatable(CreateBitterballen.MOD_ID + ".group.main"))
				.build());

		ItemGroupEvents.modifyEntriesEvent(CREATIVE_TAB).register(entries -> {
			ITEMS.stream()
					.map(Item::getDefaultInstance)
					.filter(stack -> !stack.isEmpty())
					.forEach(entries::accept);


		});


	}

}
