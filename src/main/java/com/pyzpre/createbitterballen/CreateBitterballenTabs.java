package com.pyzpre.createbitterballen;


import com.pyzpre.createbitterballen.index.BlockRegistry;
import com.pyzpre.createbitterballen.index.FluidRegistry;
import com.pyzpre.createbitterballen.index.ItemRegistry;
import com.simibubi.create.AllCreativeModeTabs;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.pyzpre.createbitterballen.index.ItemRegistry.STROOPWAFEL;

public class CreateBitterballenTabs {

	private static final DeferredRegister<CreativeModeTab> TAB_REGISTER = DeferredRegister
			.create(Registries.CREATIVE_MODE_TAB, "create_bic_bit");
	public static final Holder<CreativeModeTab> BASE = TAB_REGISTER.register("base", CreateBitterballenTabs::base);

	public static void register(IEventBus modBus) {
		TAB_REGISTER.register(modBus);
	}
	private static CreativeModeTab base(ResourceLocation id) {
		return CreativeModeTab.builder()
				.title(Component.translatable("item_group.create_bic_bit.tabs"))
				.icon(STROOPWAFEL::asStack)
				.displayItems(CreateBitterballenTabs::buildBaseContents)
				.build();
	}

	private static void buildBaseContents(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
		output.accept(ItemRegistry.STROOPWAFEL.get());
		output.accept(ItemRegistry.SWEET_DOUGH.get());
		output.accept(ItemRegistry.KRUIDNOTEN.get());
		output.accept(ItemRegistry.SPECULAAS.get());
		output.accept(ItemRegistry.UNBAKED_STROOPWAFEL.get());
		output.accept(ItemRegistry.STROOPWAFEL.get());
		output.accept(ItemRegistry.WRAPPED_STROOPWAFEL.get());
		output.accept(ItemRegistry.CHOCOLATE_GLAZED_STROOPWAFEL.get());
		output.accept(ItemRegistry.WRAPPED_COATED_STROOPWAFEL.get());
		output.accept(ItemRegistry.OLIEBOLLEN.get());
		output.accept(ItemRegistry.COATED_OLIEBOLLEN.get());
		output.accept(ItemRegistry.RAW_CHEESE_SOUFFLE.get());
		output.accept(ItemRegistry.CHEESE_SOUFFLE.get());
		output.accept(ItemRegistry.RAW_KROKET.get());
		output.accept(ItemRegistry.KROKET.get());
		output.accept(ItemRegistry.KROKET_SANDWICH.get());
		output.accept(ItemRegistry.KETCHUP_TOPPED_KROKET_SANDWICH.get());
		output.accept(ItemRegistry.MAYONNAISE_TOPPED_KROKET_SANDWICH.get());
		output.accept(ItemRegistry.MAYONNAISE_KETCHUP_TOPPED_KROKET_SANDWICH.get());
		output.accept(ItemRegistry.RAW_BITTERBALLEN.get());
		output.accept(ItemRegistry.BITTERBALLEN.get());
		output.accept(ItemRegistry.RAW_EGGBALL.get());
		output.accept(ItemRegistry.EGGBALL.get());
		output.accept(ItemRegistry.RAW_FRIKANDEL.get());
		output.accept(ItemRegistry.FRIKANDEL.get());
		output.accept(ItemRegistry.FRIKANDEL_SANDWICH.get());
		output.accept(ItemRegistry.KETCHUP_TOPPED_FRIKANDEL_SANDWICH.get());
		output.accept(ItemRegistry.MAYONNAISE_TOPPED_FRIKANDEL_SANDWICH.get());
		output.accept(ItemRegistry.MAYONNAISE_KETCHUP_TOPPED_FRIKANDEL_SANDWICH.get());
		output.accept(ItemRegistry.RAW_FRIES.get());
		output.accept(ItemRegistry.FRIES.get());
		output.accept(ItemRegistry.WRAPPED_FRIES.get());
		output.accept(ItemRegistry.WRAPPED_KETCHUP_TOPPED_FRIES.get());
		output.accept(ItemRegistry.WRAPPED_MAYONNAISE_TOPPED_FRIES.get());
		output.accept(ItemRegistry.WRAPPED_MAYONNAISE_KETCHUP_TOPPED_FRIES.get());
		output.accept(ItemRegistry.RAW_CHURROS.get());
		output.accept(ItemRegistry.CHURROS.get());
		output.accept(ItemRegistry.WRAPPED_CHURROS.get());
		output.accept(ItemRegistry.COATED_CHURROS.get());
		output.accept(ItemRegistry.WRAPPED_COATED_CHURROS.get());
		output.accept(ItemRegistry.RAW_HERRING.get());
		output.accept(ItemRegistry.COOKED_HERRING.get());
		output.accept(ItemRegistry.HERRING_SPAWN_EGG.get());
		output.accept(ItemRegistry.HERRING_BUCKET.get());
		output.accept(ItemRegistry.STAMPPOT_BOWL.get());
		output.accept(ItemRegistry.ENDERBALL.get());
		output.accept(FluidRegistry.CURDLED_MILK.getBucket().get());
		output.accept(BlockRegistry.UNRIPE_CHEESE.get());
		output.accept(BlockRegistry.WAXED_UNRIPE_CHEESE.get());
		output.accept(BlockRegistry.YOUNG_CHEESE.get());
		output.accept(BlockRegistry.WAXED_YOUNG_CHEESE.get());
		output.accept(BlockRegistry.AGED_CHEESE.get());
		output.accept(BlockRegistry.WAXED_AGED_CHEESE.get());
		output.accept(ItemRegistry.UNRIPE_CHEESE_WEDGE.get());
		output.accept(ItemRegistry.YOUNG_CHEESE_WEDGE.get());
		output.accept(ItemRegistry.AGED_CHEESE_WEDGE.get());
		output.accept(ItemRegistry.CRUSHED_NETHERWART.get());
		output.accept(ItemRegistry.KETCHUP_BOTTLE.get());
		output.accept(ItemRegistry.MAYONNAISE_BOTTLE.get());
		output.accept(FluidRegistry.KETCHUP.getBucket().get());
		output.accept(FluidRegistry.MAYONNAISE.getBucket().get());
		output.accept(ItemRegistry.CRUSHED_SUNFLOWER_SEEDS.get());
		output.accept(BlockRegistry.SUNFLOWERSTEM.get());
		output.accept(ItemRegistry.ROASTED_SUNFLOWER_SEEDS.get());
		output.accept(BlockRegistry.CRYSTALLISED_OIL.get());
		output.accept(ItemRegistry.FRYING_OIL_BOTTLE.get());
		output.accept(FluidRegistry.FRYING_OIL.getBucket().get());
		output.accept(BlockRegistry.MECHANICAL_FRYER.get());
		output.accept(ItemRegistry.BASKET.get());
		output.accept(ItemRegistry.DIRTY_PAPER.get());
	}
}