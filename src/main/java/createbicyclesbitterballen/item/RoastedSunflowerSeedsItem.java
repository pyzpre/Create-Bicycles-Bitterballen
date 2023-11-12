
package createbicyclesbitterballen.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

public class RoastedSunflowerSeedsItem extends Item {
	public RoastedSunflowerSeedsItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public int getUseDuration(ItemStack itemstack) {
		return 16;
	}
}
