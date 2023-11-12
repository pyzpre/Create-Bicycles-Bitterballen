
package createbicyclesbitterballen.item;

import createbicyclesbitterballen.index.CreateBicBitModFluids;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BucketItem;

public class FryingOilItem extends BucketItem {
	public FryingOilItem(Item.Properties properties) {
		super(CreateBicBitModFluids.FRYING_OIL, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).rarity(Rarity.COMMON));
	}

}
