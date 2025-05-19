package com.pyzpre.create_bic_bit;

import com.pyzpre.create_bic_bit.index.BlockRegistry;
import com.pyzpre.create_bic_bit.index.FluidRegistry;
import com.pyzpre.create_bic_bit.index.ItemRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;


public class CreateBitterballenTabs {
	public static final CreativeModeTab CREATIVE_TAB = new CreativeModeTab("item_group.create_bic_bit.tabs") {
		@Override
		public ItemStack makeIcon() {
			return new ItemStack(ItemRegistry.STROOPWAFEL.get());
		}

		@Override
		public void fillItemList(NonNullList<ItemStack> items) {
			items.clear();
								items.add(new ItemStack(ItemRegistry.SWEET_DOUGH.get()));
								items.add(new ItemStack(ItemRegistry.KRUIDNOTEN.get()));
								items.add(new ItemStack(ItemRegistry.SPECULAAS.get()));
								items.add(new ItemStack(ItemRegistry.UNBAKED_STROOPWAFEL.get()));
								items.add(new ItemStack(ItemRegistry.STROOPWAFEL.get()));
								items.add(new ItemStack(ItemRegistry.WRAPPED_STROOPWAFEL.get()));
								items.add(new ItemStack(ItemRegistry.CHOCOLATE_GLAZED_STROOPWAFEL.get()));
								items.add(new ItemStack(ItemRegistry.WRAPPED_COATED_STROOPWAFEL.get()));
								items.add(new ItemStack(ItemRegistry.OLIEBOLLEN.get()));
								items.add(new ItemStack(ItemRegistry.COATED_OLIEBOLLEN.get()));
								items.add(new ItemStack(ItemRegistry.RAW_CHEESE_SOUFFLE.get()));
								items.add(new ItemStack(ItemRegistry.CHEESE_SOUFFLE.get()));
								items.add(new ItemStack(ItemRegistry.RAW_KROKET.get()));
								items.add(new ItemStack(ItemRegistry.KROKET.get()));
								items.add(new ItemStack(ItemRegistry.KROKET_SANDWICH.get()));
								items.add(new ItemStack(ItemRegistry.KETCHUP_TOPPED_KROKET_SANDWICH.get()));
								items.add(new ItemStack(ItemRegistry.MAYONNAISE_TOPPED_KROKET_SANDWICH.get()));
								items.add(new ItemStack(ItemRegistry.MAYONNAISE_KETCHUP_TOPPED_KROKET_SANDWICH.get()));
								items.add(new ItemStack(ItemRegistry.RAW_BITTERBALLEN.get()));
								items.add(new ItemStack(ItemRegistry.BITTERBALLEN.get()));
								items.add(new ItemStack(ItemRegistry.RAW_EGGBALL.get()));
								items.add(new ItemStack(ItemRegistry.EGGBALL.get()));
								items.add(new ItemStack(ItemRegistry.RAW_FRIKANDEL.get()));
								items.add(new ItemStack(ItemRegistry.FRIKANDEL.get()));
								items.add(new ItemStack(ItemRegistry.FRIKANDEL_SANDWICH.get()));
								items.add(new ItemStack(ItemRegistry.KETCHUP_TOPPED_FRIKANDEL_SANDWICH.get()));
								items.add(new ItemStack(ItemRegistry.MAYONNAISE_TOPPED_FRIKANDEL_SANDWICH.get()));
								items.add(new ItemStack(ItemRegistry.MAYONNAISE_KETCHUP_TOPPED_FRIKANDEL_SANDWICH.get()));
								items.add(new ItemStack(ItemRegistry.RAW_FRIES.get()));
								items.add(new ItemStack(ItemRegistry.FRIES.get()));
								items.add(new ItemStack(ItemRegistry.WRAPPED_FRIES.get()));
								items.add(new ItemStack(ItemRegistry.WRAPPED_KETCHUP_TOPPED_FRIES.get()));
								items.add(new ItemStack(ItemRegistry.WRAPPED_MAYONNAISE_TOPPED_FRIES.get()));
								items.add(new ItemStack(ItemRegistry.WRAPPED_MAYONNAISE_KETCHUP_TOPPED_FRIES.get()));
								items.add(new ItemStack(ItemRegistry.RAW_CHURROS.get()));
								items.add(new ItemStack(ItemRegistry.CHURROS.get()));
								items.add(new ItemStack(ItemRegistry.WRAPPED_CHURROS.get()));
								items.add(new ItemStack(ItemRegistry.COATED_CHURROS.get()));
								items.add(new ItemStack(ItemRegistry.WRAPPED_COATED_CHURROS.get()));
								items.add(new ItemStack(ItemRegistry.RAW_HERRING.get()));
								items.add(new ItemStack(ItemRegistry.COOKED_HERRING.get()));
								items.add(new ItemStack(ItemRegistry.HERRING_SPAWN_EGG.get()));
								items.add(new ItemStack(ItemRegistry.HERRING_BUCKET.get()));
								items.add(new ItemStack(ItemRegistry.STAMPPOT_BOWL.get()));
								items.add(new ItemStack(ItemRegistry.ENDERBALL.get()));
								items.add(new ItemStack(FluidRegistry.CURDLED_MILK.getBucket().get()));
								items.add(new ItemStack(BlockRegistry.UNRIPE_CHEESE.get()));
								items.add(new ItemStack(BlockRegistry.WAXED_UNRIPE_CHEESE.get()));
								items.add(new ItemStack(BlockRegistry.YOUNG_CHEESE.get()));
								items.add(new ItemStack(BlockRegistry.WAXED_YOUNG_CHEESE.get()));
								items.add(new ItemStack(BlockRegistry.AGED_CHEESE.get()));
								items.add(new ItemStack(BlockRegistry.WAXED_AGED_CHEESE.get()));
								items.add(new ItemStack(ItemRegistry.UNRIPE_CHEESE_WEDGE.get()));
								items.add(new ItemStack(ItemRegistry.YOUNG_CHEESE_WEDGE.get()));
								items.add(new ItemStack(ItemRegistry.AGED_CHEESE_WEDGE.get()));
								items.add(new ItemStack(ItemRegistry.CRUSHED_NETHERWART.get()));
								items.add(new ItemStack(ItemRegistry.KETCHUP_BOTTLE.get()));
								items.add(new ItemStack(ItemRegistry.MAYONNAISE_BOTTLE.get()));
								items.add(new ItemStack(FluidRegistry.KETCHUP.getBucket().get()));
								items.add(new ItemStack(FluidRegistry.MAYONNAISE.getBucket().get()));
								items.add(new ItemStack(ItemRegistry.CRUSHED_SUNFLOWER_SEEDS.get()));
								items.add(new ItemStack(BlockRegistry.SUNFLOWERSTEM.get()));
								items.add(new ItemStack(ItemRegistry.ROASTED_SUNFLOWER_SEEDS.get()));
								items.add(new ItemStack(BlockRegistry.CRYSTALLISED_OIL.get()));
								items.add(new ItemStack(ItemRegistry.FRYING_OIL_BOTTLE.get()));
								items.add(new ItemStack(FluidRegistry.FRYING_OIL.getBucket().get()));
								items.add(new ItemStack(BlockRegistry.MECHANICAL_FRYER.get()));
								items.add(new ItemStack(ItemRegistry.BASKET.get()));
								items.add(new ItemStack(ItemRegistry.DIRTY_PAPER.get()));
		}
	};

	public static void register() {
	}
}