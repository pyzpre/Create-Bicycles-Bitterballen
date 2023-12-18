
package createbicyclesbitterballen.item;


import createbicyclesbitterballen.index.CreateBicBitModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public class WrappedCoatedStroopwafelItem extends Item {
    public WrappedCoatedStroopwafelItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack itemstack) {
        return 25;
    }
    @Override
    public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
        ItemStack retval = CreateBicBitModItems.DIRTY_PAPER.asStack();
        super.finishUsingItem(itemstack, world, entity);
        if (itemstack.isEmpty()) {
            return retval;
        } else {
            if (entity instanceof Player player && !player.getAbilities().instabuild) {
                if (!player.getInventory().add(retval))
                    player.drop(retval, false);
            }
            return itemstack;
        }
    }
}
