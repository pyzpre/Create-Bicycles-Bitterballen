
package createbicyclesbitterballen.index;

import createbicyclesbitterballen.CreateBicBitModTabs;
import createbicyclesbitterballen.item.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import com.tterrag.registrate.util.entry.ItemEntry;


import static createbicyclesbitterballen.CreateBicBitMod.REGISTRATE;

public class CreateBicBitModItems {
	static { REGISTRATE.creativeModeTab(() -> CreateBicBitModTabs.CREATIVE_TAB); }
	public static final ItemEntry<ChocolateGlazedStroopwafelItem> CHOCOLATE_GLAZED_STROOPWAFEL = REGISTRATE.item("chocolate_glazed_stroopwafel", ChocolateGlazedStroopwafelItem::new)
			.properties(p -> p.food((new FoodProperties.Builder()).nutrition(9).saturationMod(0.7f).build()))
			.register();
	public static final ItemEntry<StroopwafelItem> STROOPWAFEL = REGISTRATE.item("stroopwafel", StroopwafelItem::new)
			.properties(p -> p.food((new FoodProperties.Builder()).nutrition(7).saturationMod(0.5f).build()))
			.register();
	public static final ItemEntry<UnbakedStroopwafelItem> UNBAKED_STROOPWAFEL = REGISTRATE.item("unbaked_stroopwafel", UnbakedStroopwafelItem::new)
			.properties(p -> p.food((new FoodProperties.Builder()).nutrition(1).saturationMod(0.3f).build()))
			.register();
	public static final ItemEntry<Item> SWEET_DOUGH = REGISTRATE.item("sweet_dough", Item::new).register();
	public static final ItemEntry<RawFrikandelItem> RAW_FRIKANDEL = REGISTRATE.item("raw_frikandel", RawFrikandelItem::new)
			.properties(p -> p.food((new FoodProperties.Builder()).nutrition(4).saturationMod(0.2f).meat().effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.3F).build()))
			.register();
	public static final ItemEntry<FrikandelItem> FRIKANDEL = REGISTRATE.item("frikandel", FrikandelItem::new)
			.properties(p -> p.food((new FoodProperties.Builder()).nutrition(8).saturationMod(0.2f).meat().build()))
			.register();
	//public static final ItemEntry<FrikandelSandwichItem> FRIKANDEL_SANDWICH = REGISTRATE.item("frikandel_sandwich", FrikandelSandwichItem::new)
			//.properties(p -> p.food((new FoodProperties.Builder()).nutrition(9).saturationMod(0.3f).meat().build()))
			//.register();
	public static final ItemEntry<Item> CRUSHED_SUNFLOWER_SEEDS = REGISTRATE.item("crushed_sunflower_seeds", Item::new).register();
	public static final ItemEntry<SunflowerSeedsItem> SUNFLOWER_SEEDS = REGISTRATE.item("sunflower_seeds", SunflowerSeedsItem::new)
			.properties(p -> p.food((new FoodProperties.Builder()).nutrition(1).saturationMod(0.1f).build()))
			.register();
	public static final ItemEntry<RoastedSunflowerSeedsItem> ROASTED_SUNFLOWER_SEEDS = REGISTRATE.item("roasted_sunflower_seeds", RoastedSunflowerSeedsItem::new)
			.properties(p -> p.food((new FoodProperties.Builder()).nutrition(2).saturationMod(0.2f).build()))
			.register();
	public static final ItemEntry<FryingOilItem> FRYING_OIL_BUCKET = REGISTRATE.item("frying_oil_bucket", FryingOilItem::new)
			.register();

	public static final ItemEntry<StamppotBowlItem> STAMPPOT_BOWL = REGISTRATE.item("stamppot_bowl", StamppotBowlItem::new)
			.properties(p -> p.food((new FoodProperties.Builder()).nutrition(10).saturationMod(0.5f).build()))
			.register();
	public static final ItemEntry<SpeculaasItem> SPECULAAS = REGISTRATE.item("speculaas", SpeculaasItem::new)
			.properties(p -> p.food((new FoodProperties.Builder()).nutrition(5).saturationMod(0.3f).build()))
			.register();
	public static final ItemEntry<RawBitterballenItem> RAW_BITTERBALLEN = REGISTRATE.item("raw_bitterballen", RawBitterballenItem::new)
			.properties(p -> p.food((new FoodProperties.Builder()).nutrition(3).saturationMod(0.2f).meat().effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.3F).build()))
			.register();
	public static final ItemEntry<BitterballenItem> BITTERBALLEN = REGISTRATE.item("bitterballen", BitterballenItem::new)
			.properties(p -> p.food((new FoodProperties.Builder()).nutrition(7).saturationMod(0.2f).meat().build()))
			.register();
	public static final ItemEntry<KroketItem> KROKET = REGISTRATE.item("kroket", KroketItem::new)
			.properties(p -> p.food((new FoodProperties.Builder()).nutrition(8).saturationMod(0.2f).meat().build()))
			.register();
	public static final ItemEntry<RawKroketItem> RAW_KROKET = REGISTRATE.item("raw_kroket", RawKroketItem::new)
			.properties(p -> p.food((new FoodProperties.Builder()).nutrition(4).saturationMod(0.2f).meat().effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.3F).build()))
			.register();
	public static final ItemEntry<OliebollenItem> OLIEBOLLEN = REGISTRATE.item("oliebollen", OliebollenItem::new)
			.properties(p -> p.food((new FoodProperties.Builder()).nutrition(5).saturationMod(0.2f).build()))
			.register();
	public static final ItemEntry<RawFriesItem> RAW_FRIES = REGISTRATE.item("raw_fries", RawFriesItem::new)
			.properties(p -> p.food((new FoodProperties.Builder()).nutrition(1).saturationMod(0.3f).build()))
			.register();
	public static final ItemEntry<FriesItem> FRIES = REGISTRATE.item("fries", FriesItem::new)
			.properties(p -> p.food((new FoodProperties.Builder()).nutrition(6).saturationMod(0.5f).build()))
			.register();
	public static final ItemEntry<WrappedFriesItem> WRAPPED_FRIES = REGISTRATE.item("wrapped_fries", WrappedFriesItem::new)
			.properties(p -> p.food((new FoodProperties.Builder()).nutrition(6).saturationMod(0.5f).build()))
			.register();

	public static final ItemEntry<Item> DIRTY_PAPER = REGISTRATE.item("dirty_paper", Item::new).register();

	public static final ItemEntry<Item> BASKET = REGISTRATE.item("andesite_basket", Item::new).register();


	public static void register() {
	}
}



