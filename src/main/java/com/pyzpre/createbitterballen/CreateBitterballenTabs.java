package com.pyzpre.createbitterballen;

import com.pyzpre.createbitterballen.index.BlockRegistry;
import com.pyzpre.createbitterballen.index.FluidRegistry;
import com.pyzpre.createbitterballen.index.ItemRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CreateBitterballenTabs {

	private static final DeferredRegister<CreativeModeTab> TAB_REGISTER =
			DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "create_bic_bit");

	public static final RegistryObject<CreativeModeTab> BITTERBALLEN =
			TAB_REGISTER.register("tabs",
					() -> CreativeModeTab.builder()
							.title(Component.translatable("item_group.create_bic_bit.tabs"))
							.icon(() -> new ItemStack(ItemRegistry.STROOPWAFEL.get()))
							.displayItems((parameters, tabData) -> {
								tabData.accept(ItemRegistry.SWEET_DOUGH.get());
								tabData.accept(ItemRegistry.KRUIDNOTEN.get());
								tabData.accept(ItemRegistry.SPECULAAS.get());
								tabData.accept(ItemRegistry.UNBAKED_STROOPWAFEL.get());
								tabData.accept(ItemRegistry.STROOPWAFEL.get());
								tabData.accept(ItemRegistry.WRAPPED_STROOPWAFEL.get());
								tabData.accept(ItemRegistry.CHOCOLATE_GLAZED_STROOPWAFEL.get());
								tabData.accept(ItemRegistry.WRAPPED_COATED_STROOPWAFEL.get());
								tabData.accept(ItemRegistry.OLIEBOLLEN.get());
								tabData.accept(ItemRegistry.COATED_OLIEBOLLEN.get());
								tabData.accept(ItemRegistry.RAW_CHEESE_SOUFFLE.get());
								tabData.accept(ItemRegistry.CHEESE_SOUFFLE.get());
								tabData.accept(ItemRegistry.RAW_KROKET.get());
								tabData.accept(ItemRegistry.KROKET.get());
								tabData.accept(ItemRegistry.KROKET_SANDWICH.get());
								tabData.accept(ItemRegistry.KETCHUP_TOPPED_KROKET_SANDWICH.get());
								tabData.accept(ItemRegistry.MAYONNAISE_TOPPED_KROKET_SANDWICH.get());
								tabData.accept(ItemRegistry.MAYONNAISE_KETCHUP_TOPPED_KROKET_SANDWICH.get());
								tabData.accept(ItemRegistry.RAW_BITTERBALLEN.get());
								tabData.accept(ItemRegistry.BITTERBALLEN.get());
								tabData.accept(ItemRegistry.RAW_EGGBALL.get());
								tabData.accept(ItemRegistry.EGGBALL.get());
								tabData.accept(ItemRegistry.RAW_FRIKANDEL.get());
								tabData.accept(ItemRegistry.FRIKANDEL.get());
								tabData.accept(ItemRegistry.FRIKANDEL_SANDWICH.get());
								tabData.accept(ItemRegistry.KETCHUP_TOPPED_FRIKANDEL_SANDWICH.get());
								tabData.accept(ItemRegistry.MAYONNAISE_TOPPED_FRIKANDEL_SANDWICH.get());
								tabData.accept(ItemRegistry.MAYONNAISE_KETCHUP_TOPPED_FRIKANDEL_SANDWICH.get());
								tabData.accept(ItemRegistry.RAW_FRIES.get());
								tabData.accept(ItemRegistry.FRIES.get());
								tabData.accept(ItemRegistry.WRAPPED_FRIES.get());
								tabData.accept(ItemRegistry.WRAPPED_KETCHUP_TOPPED_FRIES.get());
								tabData.accept(ItemRegistry.WRAPPED_MAYONNAISE_TOPPED_FRIES.get());
								tabData.accept(ItemRegistry.WRAPPED_MAYONNAISE_KETCHUP_TOPPED_FRIES.get());
								tabData.accept(ItemRegistry.RAW_CHURROS.get());
								tabData.accept(ItemRegistry.CHURROS.get());
								tabData.accept(ItemRegistry.WRAPPED_CHURROS.get());
								tabData.accept(ItemRegistry.COATED_CHURROS.get());
								tabData.accept(ItemRegistry.WRAPPED_COATED_CHURROS.get());
								tabData.accept(ItemRegistry.RAW_HERRING.get());
								tabData.accept(ItemRegistry.COOKED_HERRING.get());
								tabData.accept(ItemRegistry.HERRING_SPAWN_EGG.get());
								tabData.accept(ItemRegistry.HERRING_BUCKET.get());
								tabData.accept(ItemRegistry.STAMPPOT_BOWL.get());
								tabData.accept(ItemRegistry.ENDERBALL.get());
								tabData.accept(FluidRegistry.CURDLED_MILK.getBucket().get());
								tabData.accept(BlockRegistry.UNRIPE_CHEESE.get());
								tabData.accept(BlockRegistry.WAXED_UNRIPE_CHEESE.get());
								tabData.accept(BlockRegistry.YOUNG_CHEESE.get());
								tabData.accept(BlockRegistry.WAXED_YOUNG_CHEESE.get());
								tabData.accept(BlockRegistry.AGED_CHEESE.get());
								tabData.accept(BlockRegistry.WAXED_AGED_CHEESE.get());
								tabData.accept(ItemRegistry.UNRIPE_CHEESE_WEDGE.get());
								tabData.accept(ItemRegistry.YOUNG_CHEESE_WEDGE.get());
								tabData.accept(ItemRegistry.AGED_CHEESE_WEDGE.get());
								tabData.accept(ItemRegistry.CRUSHED_NETHERWART.get());
								tabData.accept(ItemRegistry.KETCHUP_BOTTLE.get());
								tabData.accept(ItemRegistry.MAYONNAISE_BOTTLE.get());
								tabData.accept(FluidRegistry.KETCHUP.getBucket().get());
								tabData.accept(FluidRegistry.MAYONNAISE.getBucket().get());
								tabData.accept(ItemRegistry.CRUSHED_SUNFLOWER_SEEDS.get());
								tabData.accept(BlockRegistry.SUNFLOWERSTEM.get());
								tabData.accept(ItemRegistry.ROASTED_SUNFLOWER_SEEDS.get());
								tabData.accept(BlockRegistry.CRYSTALLISED_OIL.get());
								tabData.accept(ItemRegistry.FRYING_OIL_BOTTLE.get());
								tabData.accept(FluidRegistry.FRYING_OIL.getBucket().get());
								tabData.accept(BlockRegistry.MECHANICAL_FRYER.get());
								tabData.accept(ItemRegistry.BASKET.get());
								tabData.accept(ItemRegistry.DIRTY_PAPER.get());
							})
							.build());

	public static void register(IEventBus eventBus) {
		TAB_REGISTER.register(eventBus);
	}
}