package com.pyzpre.create_bic_bit.item.wrapped;

import com.pyzpre.create_bic_bit.index.ItemRegistry;
import com.simibubi.create.foundation.item.TooltipHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WrappedItem extends Item{
    public WrappedItem(Properties properties) {
        super(properties);
    }
    @Override
    public int getUseDuration(ItemStack itemstack) {
        return 20;
    }
    @Override
    public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
        ItemStack retval = new ItemStack(ItemRegistry.DIRTY_PAPER.get());
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
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        tooltip.add(TooltipHelper.holdShift(TooltipHelper.Palette.STANDARD_CREATE, true));

        if (Screen.hasShiftDown()) {
            MutableComponent part1 = Component.translatable("item.create_bic_bit.wrapped.tooltip.part1")
                    .setStyle(TooltipHelper.Palette.STANDARD_CREATE.highlight());
            MutableComponent part2 = Component.translatable("item.create_bic_bit.wrapped.tooltip.part2")
                    .setStyle(TooltipHelper.Palette.STANDARD_CREATE.primary());
            MutableComponent part3 = Component.translatable("item.create_bic_bit.wrapped.tooltip.part3")
                    .setStyle(TooltipHelper.Palette.STANDARD_CREATE.primary());

            tooltip.add(part1.append(part2).append(part3));
        }
    }
}
