package createbicyclesbitterballen;


import createbicyclesbitterballen.index.BlockRegistry;
import createbicyclesbitterballen.index.CreateBicBitModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;


public class CreateBicBitModTabs {
	public static final CreativeModeTab CREATIVE_TAB = new CreativeModeTab("cbb_creative_tab") {
		@Override
		public ItemStack makeIcon() {
			return new ItemStack(CreateBicBitModItems.STROOPWAFEL.get());
		}

		@Override
		public void fillItemList(NonNullList<ItemStack> items) {
			items.clear();


			items.add(new ItemStack(CreateBicBitModItems.SWEET_DOUGH.get()));
			items.add(new ItemStack(CreateBicBitModItems.KRUIDNOTEN.get()));
			items.add(new ItemStack(CreateBicBitModItems.SPECULAAS.get()));
			items.add(new ItemStack(CreateBicBitModItems.UNBAKED_STROOPWAFEL.get()));
			items.add(new ItemStack(CreateBicBitModItems.STROOPWAFEL.get()));
			items.add(new ItemStack(CreateBicBitModItems.WRAPPED_STROOPWAFEL.get()));
			items.add(new ItemStack(CreateBicBitModItems.CHOCOLATE_GLAZED_STROOPWAFEL.get()));
			items.add(new ItemStack(CreateBicBitModItems.WRAPPED_COATED_STROOPWAFEL.get()));
			items.add(new ItemStack(CreateBicBitModItems.OLIEBOLLEN.get()));
			items.add(new ItemStack(CreateBicBitModItems.RAW_KROKET.get()));
			items.add(new ItemStack(CreateBicBitModItems.KROKET.get()));
			items.add(new ItemStack(CreateBicBitModItems.KROKET_SANDWICH.get()));
			items.add(new ItemStack(CreateBicBitModItems.KETCHUP_TOPPED_KROKET_SANDWICH.get()));
			items.add(new ItemStack(CreateBicBitModItems.MAYONNAISE_TOPPED_KROKET_SANDWICH.get()));
			items.add(new ItemStack(CreateBicBitModItems.MAYONNAISE_KETCHUP_TOPPED_KROKET_SANDWICH.get()));
			items.add(new ItemStack(CreateBicBitModItems.RAW_BITTERBALLEN.get()));
			items.add(new ItemStack(CreateBicBitModItems.BITTERBALLEN.get()));
			items.add(new ItemStack(CreateBicBitModItems.RAW_FRIKANDEL.get()));
			items.add(new ItemStack(CreateBicBitModItems.FRIKANDEL.get()));
			items.add(new ItemStack(CreateBicBitModItems.FRIKANDEL_SANDWICH.get()));
			items.add(new ItemStack(CreateBicBitModItems.KETCHUP_TOPPED_FRIKANDEL_SANDWICH.get()));
			items.add(new ItemStack(CreateBicBitModItems.MAYONNAISE_TOPPED_FRIKANDEL_SANDWICH.get()));
			items.add(new ItemStack(CreateBicBitModItems.MAYONNAISE_KETCHUP_TOPPED_FRIKANDEL_SANDWICH.get()));
			items.add(new ItemStack(CreateBicBitModItems.RAW_FRIES.get()));
			items.add(new ItemStack(CreateBicBitModItems.FRIES.get()));
			items.add(new ItemStack(CreateBicBitModItems.WRAPPED_FRIES.get()));
			items.add(new ItemStack(CreateBicBitModItems.WRAPPED_KETCHUP_TOPPED_FRIES.get()));
			items.add(new ItemStack(CreateBicBitModItems.WRAPPED_MAYONNAISE_TOPPED_FRIES.get()));
			items.add(new ItemStack(CreateBicBitModItems.WRAPPED_MAYONNAISE_KETCHUP_TOPPED_FRIES.get()));
			items.add(new ItemStack(CreateBicBitModItems.RAW_CHURROS.get()));
			items.add(new ItemStack(CreateBicBitModItems.CHURROS.get()));
			items.add(new ItemStack(CreateBicBitModItems.WRAPPED_CHURROS.get()));
			items.add(new ItemStack(CreateBicBitModItems.RAW_HERRING.get()));
			items.add(new ItemStack(CreateBicBitModItems.COOKED_HERRING.get()));
			items.add(new ItemStack(CreateBicBitModItems.STAMPPOT_BOWL.get()));
			items.add(new ItemStack(CreateBicBitModItems.ENDERBALL.get()));
			items.add(new ItemStack(CreateBicBitModItems.CRUSHED_NETHERWART.get()));
			items.add(new ItemStack(CreateBicBitModItems.KETCHUP_BUCKET.get()));
			items.add(new ItemStack(CreateBicBitModItems.MAYONNAISE_BUCKET.get()));
			items.add(new ItemStack(CreateBicBitModItems.CRUSHED_SUNFLOWER_SEEDS.get()));
			items.add(new ItemStack(CreateBicBitModItems.SUNFLOWER_SEEDS.get()));
			items.add(new ItemStack(CreateBicBitModItems.ROASTED_SUNFLOWER_SEEDS.get()));
			items.add(new ItemStack(BlockRegistry.CRYSTALLISED_OIL.get()));
			items.add(new ItemStack(CreateBicBitModItems.FRYING_OIL_BUCKET.get()));
			items.add(new ItemStack(BlockRegistry.MECHANICAL_FRYER.get()));
			items.add(new ItemStack(CreateBicBitModItems.BASKET.get()));
			items.add(new ItemStack(CreateBicBitModItems.DIRTY_PAPER.get()));
		}
	};

	public static void register() {
	}
}
