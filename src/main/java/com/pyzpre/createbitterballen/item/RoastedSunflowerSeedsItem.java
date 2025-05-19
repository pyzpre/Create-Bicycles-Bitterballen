
package com.pyzpre.createbitterballen.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RoastedSunflowerSeedsItem extends Item {
	public RoastedSunflowerSeedsItem(Properties properties) {
		super(properties);
	}

	@Override
	public int getUseDuration(ItemStack itemstack, LivingEntity entity){
		return 16;
	}
}
