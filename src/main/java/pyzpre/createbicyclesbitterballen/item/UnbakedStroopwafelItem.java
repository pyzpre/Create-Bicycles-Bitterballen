package pyzpre.createbicyclesbitterballen.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class UnbakedStroopwafelItem extends Item {
	public UnbakedStroopwafelItem(Properties properties) {
		super(properties);
	}

	@Override
	public int getUseDuration(@Nonnull ItemStack itemstack) {
		return 25;
	}

}
