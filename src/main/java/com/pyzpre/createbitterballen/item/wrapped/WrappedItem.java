package com.pyzpre.createbitterballen.item.wrapped;

import com.pyzpre.createbitterballen.index.ItemRegistry;
import com.simibubi.create.foundation.item.TooltipHelper;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.ChatFormatting;
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
        ItemStack retval = new ItemStack(ItemRegistry.DIRTY_PAPER);
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
        FontHelper.Palette palette = FontHelper.Palette.STANDARD_CREATE;
        // Add a message prompting the user to hold Shift for more information
        tooltip.add(TooltipHelper.holdShift(FontHelper.Palette.STANDARD_CREATE, true));

        // Check if the Shift key is held down
        if (Screen.hasShiftDown()) {
            // Add detailed tooltip information with appropriate formatting
            MutableComponent part1 = Component.translatable("item.create_bic_bit.wrapped.tooltip.part1")
                    .withStyle(palette.highlight());
            MutableComponent part2 = Component.translatable("item.create_bic_bit.wrapped.tooltip.part2")
                    .withStyle(palette.primary());
            MutableComponent part3 = Component.translatable("item.create_bic_bit.wrapped.tooltip.part3")
                    .withStyle(palette.primary());

            // Combine the parts and add to the tooltip
            tooltip.add(part1.append(part2).append(part3));
        }
    }

}
