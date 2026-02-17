package com.pyzpre.createbitterballen.index;

import com.pyzpre.createbitterballen.item.*;
import com.pyzpre.createbitterballen.item.wrapped.WrappedItem;
import com.pyzpre.createbitterballen.item.wrapped.WrappedKetchupItem;
import com.pyzpre.createbitterballen.item.wrapped.WrappedKetchupMayoItem;
import com.pyzpre.createbitterballen.item.wrapped.WrappedMayoItem;
import com.simibubi.create.AllTags;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.material.Fluids;

import static com.pyzpre.createbitterballen.CreateBitterballen.REGISTRATE;

public class ItemRegistry {
    public static final ItemEntry<Item> SWEET_DOUGH = REGISTRATE.item("sweet_dough", Item::new).register();
    public static final ItemEntry<Item> KRUIDNOTEN = REGISTRATE.item("kruidnoten", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(4).saturationMod(0.4f).build()))
            .register();
    public static final ItemEntry<Item> SPECULAAS = REGISTRATE.item("speculaas", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(8).saturationMod(0.2f).build()))
            .register();
    public static final ItemEntry<Item> UNBAKED_STROOPWAFEL = REGISTRATE.item("unbaked_stroopwafel", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(1).saturationMod(0.3f).build()))
            .register();
    public static final ItemEntry<Item> STROOPWAFEL = REGISTRATE.item("stroopwafel", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(9).saturationMod(0.5f).build()))
            .register();
    public static final ItemEntry<WrappedItem> WRAPPED_STROOPWAFEL = REGISTRATE.item("wrapped_stroopwafel", WrappedItem::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(9).saturationMod(0.5f).build()))
            .register();
    public static final ItemEntry<Item> CHOCOLATE_GLAZED_STROOPWAFEL = REGISTRATE.item("chocolate_glazed_stroopwafel", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(11).saturationMod(0.7f).build()))
            .register();
    public static final ItemEntry<WrappedItem> WRAPPED_COATED_STROOPWAFEL = REGISTRATE.item("wrapped_chocolate_glazed_stroopwafel", WrappedItem::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(11).saturationMod(0.7f).build()))
            .register();
    public static final ItemEntry<Item> OLIEBOLLEN = REGISTRATE.item("oliebollen", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(8).saturationMod(0.3f).build()))
            .register();
    public static final ItemEntry<Item> COATED_OLIEBOLLEN = REGISTRATE.item("coated_oliebollen", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(9).saturationMod(0.4f).build()))
            .register();
    public static final ItemEntry<Item> RAW_CHEESE_SOUFFLE = REGISTRATE.item("raw_cheese_souffle", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(3).saturationMod(0.5f).build()))
            .register();
    public static final ItemEntry<Item> CHEESE_SOUFFLE = REGISTRATE.item("cheese_souffle", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(11).saturationMod(0.5f).build()))
            .register();
    public static final ItemEntry<Item> RAW_KROKET = REGISTRATE.item("raw_kroket", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(4).saturationMod(0.2f).meat().effect(new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.3F).build()))
            .register();
    public static final ItemEntry<Item> KROKET = REGISTRATE.item("kroket", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(11).saturationMod(0.4f).meat().build()))
            .register();
    public static final ItemEntry<Item> KROKET_SANDWICH = REGISTRATE.item("kroket_sandwich", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(13).saturationMod(0.4f).meat().build()))
            .register();
    public static final ItemEntry<KetchupItem> KETCHUP_TOPPED_KROKET_SANDWICH = REGISTRATE.item("ketchup_topped_kroket_sandwich", KetchupItem::new)
            .properties(p -> p.food((new FoodProperties.Builder()).alwaysEat().nutrition(13).saturationMod(0.5f).meat().effect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0), 1F).build()))
            .register();
    public static final ItemEntry<MayoItem> MAYONNAISE_TOPPED_KROKET_SANDWICH = REGISTRATE.item("mayonnaise_topped_kroket_sandwich", MayoItem::new)
            .properties(p -> p.food((new FoodProperties.Builder()).alwaysEat().nutrition(13).saturationMod(0.5f).meat().effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0), 1F).build()))
            .register();
    public static final ItemEntry<KetchupMayoItem> MAYONNAISE_KETCHUP_TOPPED_KROKET_SANDWICH = REGISTRATE.item("mayonnaise_ketchup_topped_kroket_sandwich", KetchupMayoItem::new)
            .properties(p -> p.food((new FoodProperties.Builder()).alwaysEat().nutrition(14).saturationMod(0.5f).meat().effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0), 1F).effect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0), 1F).build()))
            .register();
    public static final ItemEntry<Item> RAW_BITTERBALLEN = REGISTRATE.item("raw_bitterballen", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(3).saturationMod(0.2f).meat().effect(new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.3F).build()))
            .register();
    public static final ItemEntry<Item> BITTERBALLEN = REGISTRATE.item("bitterballen", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(10).saturationMod(0.4f).meat().build()))
            .register();
    public static final ItemEntry<Item> RAW_EGGBALL = REGISTRATE.item("raw_eggball", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(1).saturationMod(0.2f).effect(new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.3F).build()))
            .register();
    public static final ItemEntry<Item> EGGBALL = REGISTRATE.item("eggball", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(6).saturationMod(0.2f).build()))
            .register();
    public static final ItemEntry<Item> RAW_FRIKANDEL = REGISTRATE.item("raw_frikandel", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(4).saturationMod(0.2f).meat().effect(new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.3F).build()))
            .register();
    public static final ItemEntry<Item> FRIKANDEL = REGISTRATE.item("frikandel", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(11).saturationMod(0.4f).meat().build()))
            .register();
    public static final ItemEntry<Item> FRIKANDEL_SANDWICH = REGISTRATE.item("frikandel_sandwich", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(13).saturationMod(0.4f).meat().build()))
            .register();
    public static final ItemEntry<KetchupItem> KETCHUP_TOPPED_FRIKANDEL_SANDWICH = REGISTRATE.item("ketchup_topped_frikandel_sandwich", KetchupItem::new)
            .properties(p -> p.food((new FoodProperties.Builder()).alwaysEat().nutrition(13).saturationMod(0.5f).meat().effect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0), 1F).build()))
            .register();
    public static final ItemEntry<MayoItem> MAYONNAISE_TOPPED_FRIKANDEL_SANDWICH = REGISTRATE.item("mayonnaise_topped_frikandel_sandwich", MayoItem::new)
            .properties(p -> p.food((new FoodProperties.Builder()).alwaysEat().nutrition(13).saturationMod(0.5f).meat().effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0), 1F).build()))
            .register();
    public static final ItemEntry<KetchupMayoItem> MAYONNAISE_KETCHUP_TOPPED_FRIKANDEL_SANDWICH = REGISTRATE.item("mayonnaise_ketchup_topped_frikandel_sandwich", KetchupMayoItem::new)
            .properties(p -> p.food((new FoodProperties.Builder()).alwaysEat().nutrition(14).saturationMod(0.5f).meat().effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0), 1F).effect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0), 1F).build()))
            .register();
    public static final ItemEntry<Item> RAW_FRIES = REGISTRATE.item("raw_fries", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(1).saturationMod(0.3f).build()))
            .register();
    public static final ItemEntry<Item> FRIES = REGISTRATE.item("fries", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(6).saturationMod(0.5f).build()))
            .register();
    public static final ItemEntry<WrappedItem> WRAPPED_FRIES = REGISTRATE.item("wrapped_fries", WrappedItem::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(6).saturationMod(0.5f).build()))
            .register();
    public static final ItemEntry<WrappedKetchupItem> WRAPPED_KETCHUP_TOPPED_FRIES = REGISTRATE.item("wrapped_ketchup_topped_fries", WrappedKetchupItem::new)
            .properties(p -> p.food((new FoodProperties.Builder()).alwaysEat().nutrition(6).saturationMod(0.6f).effect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0), 1F).build()))
            .register();
    public static final ItemEntry<WrappedMayoItem> WRAPPED_MAYONNAISE_TOPPED_FRIES = REGISTRATE.item("wrapped_mayonnaise_topped_fries", WrappedMayoItem::new)
            .properties(p -> p.food((new FoodProperties.Builder()).alwaysEat().nutrition(6).saturationMod(0.6f).effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0), 1F).build()))
            .register();
    public static final ItemEntry<WrappedKetchupMayoItem> WRAPPED_MAYONNAISE_KETCHUP_TOPPED_FRIES = REGISTRATE.item("wrapped_mayonnaise_ketchup_topped_fries", WrappedKetchupMayoItem::new)
            .properties(p -> p.food((new FoodProperties.Builder()).alwaysEat().nutrition(7).saturationMod(0.6f).effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0), 1F).effect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0), 1F).build()))
            .register();
    public static final ItemEntry<Item> RAW_CHURROS = REGISTRATE.item("raw_churros", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(2).saturationMod(0.3f).build()))
            .register();
    public static final ItemEntry<Item> CHURROS = REGISTRATE.item("churros", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(6).saturationMod(0.6f).build()))
            .register();
    public static final ItemEntry<WrappedItem> WRAPPED_CHURROS = REGISTRATE.item("wrapped_churros", WrappedItem::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(6).saturationMod(0.6f).build()))
            .register();
    public static final ItemEntry<Item> COATED_CHURROS = REGISTRATE.item("coated_churros", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(10).saturationMod(0.8f).build()))
            .register();
    public static final ItemEntry<WrappedItem> WRAPPED_COATED_CHURROS = REGISTRATE.item("wrapped_coated_churros", WrappedItem::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(10).saturationMod(0.8f).build()))
            .register();
    public static final ItemEntry<Item> RAW_HERRING = REGISTRATE.item("raw_herring", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(3).saturationMod(0.2f).meat().build()))
            .register();
    public static final ItemEntry<Item> COOKED_HERRING = REGISTRATE.item("cooked_herring", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(6).saturationMod(0.4f).build()))
            .register();
    public static final ItemEntry<BowlItem> STAMPPOT_BOWL = REGISTRATE.item("stamppot_bowl", BowlItem::new)
            .tag(AllTags.AllItemTags.UPRIGHT_ON_BELT.tag)
            .properties(p -> p
                    .food((new FoodProperties.Builder()).nutrition(15).saturationMod(0.5f).build())
                    .stacksTo(1)
            )
            .register();
    public static final ItemEntry<EnderballItem> ENDERBALL = REGISTRATE.item("enderball", EnderballItem::new)
            .properties(p -> p.food((new FoodProperties.Builder()).alwaysEat().nutrition(10).saturationMod(0.10f).effect(new MobEffectInstance(EffectRegistry.UNANCHORED.get(), 600, 0), 1F).build()))
            .register();
    public static final ItemEntry<Item> UNRIPE_CHEESE_WEDGE = REGISTRATE.item("unripe_cheese_wedge", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(3).saturationMod(0.3f).build()))
            .register();
    public static final ItemEntry<Item> YOUNG_CHEESE_WEDGE = REGISTRATE.item("young_cheese_wedge", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(6).saturationMod(0.5f).build()))
            .register();
    public static final ItemEntry<Item> AGED_CHEESE_WEDGE = REGISTRATE.item("aged_cheese_wedge", Item::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(6).saturationMod(0.8f).build()))
            .register();
    public static final ItemEntry<Item> CRUSHED_NETHERWART = REGISTRATE.item("crushed_nether_wart", Item::new).register();
    public static final ItemEntry<BottleItem> KETCHUP_BOTTLE = REGISTRATE.item("ketchup_bottle", BottleItem::new)
            .tag(AllTags.AllItemTags.UPRIGHT_ON_BELT.tag)
            .properties(p -> p.stacksTo(16))
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(1).saturationMod(0.2f).build()))
            .lang("Ketchup Bottle")
            .register();
    public static final ItemEntry<BottleItem> MAYONNAISE_BOTTLE = REGISTRATE.item("mayonnaise_bottle", BottleItem::new)
            .tag(AllTags.AllItemTags.UPRIGHT_ON_BELT.tag)
            .properties(p -> p.stacksTo(16))
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(1).saturationMod(0.2f).build()))
            .lang("Mayonnaise Bottle")
            .register();
    public static final ItemEntry<Item> CRUSHED_SUNFLOWER_SEEDS = REGISTRATE.item("crushed_sunflower_seeds", Item::new)
            .register();
    public static final ItemEntry<RoastedSunflowerSeedsItem> ROASTED_SUNFLOWER_SEEDS = REGISTRATE.item("roasted_sunflower_seeds", RoastedSunflowerSeedsItem::new)
            .properties(p -> p.food((new FoodProperties.Builder()).nutrition(2).saturationMod(0.2f).build()))
            .register();
    public static final ItemEntry<FryingOilBottleItem> FRYING_OIL_BOTTLE = REGISTRATE.item("frying_oil_bottle", FryingOilBottleItem::new)
            .tag(AllTags.AllItemTags.UPRIGHT_ON_BELT.tag)
            .properties(p -> p.stacksTo(16))
            .properties(p -> p.food((new FoodProperties.Builder()).alwaysEat().effect(new MobEffectInstance(EffectRegistry.OILED_UP.get(), 1200, 0), 1F).effect(new MobEffectInstance(MobEffects.POISON, 200, 0), 1F).build()))
            .lang("Frying Oil Bottle")
            .register();
    public static final ItemEntry<Item> BASKET = REGISTRATE.item("andesite_basket", Item::new).register();
    public static final ItemEntry<Item> DIRTY_PAPER = REGISTRATE.item("dirty_paper", Item::new).register();
    public static final ItemEntry<SpawnEggItem> HERRING_SPAWN_EGG = REGISTRATE.item("herring_spawn_egg",
            properties -> new SpawnEggItem(
                    EntityRegistry.HERRING.get(),
                    0x649999,
                    0xb18a53,
                    properties
            )
    ).register();
    public static final ItemEntry<MobBucketItem> HERRING_BUCKET = REGISTRATE.item("herring_bucket",
            properties -> new MobBucketItem(
                    EntityRegistry.HERRING.get(), // The Herring entity type as a Supplier
                    Fluids.WATER, // The fluid contained in the bucket as a Supplier
                    SoundEvents.BUCKET_EMPTY_FISH, // Sound when the bucket is emptied as a Supplier
                    properties.stacksTo(1) // Standard bucket properties
            )
    ).register();

    public static void register() {
    }
}
