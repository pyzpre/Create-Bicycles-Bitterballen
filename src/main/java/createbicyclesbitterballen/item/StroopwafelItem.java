
package createbicyclesbitterballen.item;


import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;


public class StroopwafelItem extends Item {
	public StroopwafelItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public int getUseDuration(ItemStack itemstack) {
		return 25;
	}
}
