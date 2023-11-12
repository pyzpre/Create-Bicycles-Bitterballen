package createbicyclesbitterballen.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import javax.annotation.Nonnull;

public class UnbakedStroopwafelItem extends Item {
	public UnbakedStroopwafelItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public int getUseDuration(@Nonnull ItemStack itemstack) {
		return 25;
	}

}
