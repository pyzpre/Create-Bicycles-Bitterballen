
package pyzpre.createbicyclesbitterballen.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RoastedSunflowerSeedsItem extends Item {
	public RoastedSunflowerSeedsItem(Properties properties) {
		super(properties);
	}

	@Override
	public int getUseDuration(ItemStack itemstack) {
		return 16;
	}
}
