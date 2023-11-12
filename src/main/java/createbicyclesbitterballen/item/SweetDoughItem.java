
package createbicyclesbitterballen.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class SweetDoughItem extends Item {
	public SweetDoughItem() {
		super(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON));
	}
}
