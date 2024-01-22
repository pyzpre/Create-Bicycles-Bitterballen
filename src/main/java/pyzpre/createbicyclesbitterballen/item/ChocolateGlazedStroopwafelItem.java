
package pyzpre.createbicyclesbitterballen.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ChocolateGlazedStroopwafelItem extends Item {
	public ChocolateGlazedStroopwafelItem(Properties properties) {
		super(properties);
	}

	@Override
	public int getUseDuration(ItemStack itemstack) {
		return 25;
	}
}
