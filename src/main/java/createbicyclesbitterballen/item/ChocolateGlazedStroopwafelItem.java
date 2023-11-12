
package createbicyclesbitterballen.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

public class ChocolateGlazedStroopwafelItem extends Item {
	public ChocolateGlazedStroopwafelItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public int getUseDuration(ItemStack itemstack) {
		return 25;
	}
}
