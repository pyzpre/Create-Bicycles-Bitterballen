package net.minecraft.network.chat;

import java.util.Arrays;
import java.util.Collection;

public class CommonComponents {
   public static final Component EMPTY = Component.empty();
   public static final Component OPTION_ON = Component.translatable("options.on");
   public static final Component OPTION_OFF = Component.translatable("options.off");
   public static final Component GUI_DONE = Component.translatable("gui.done");
   public static final Component GUI_CANCEL = Component.translatable("gui.cancel");
   public static final Component GUI_YES = Component.translatable("gui.yes");
   public static final Component GUI_NO = Component.translatable("gui.no");
   public static final Component GUI_PROCEED = Component.translatable("gui.proceed");
   public static final Component GUI_BACK = Component.translatable("gui.back");
   public static final Component GUI_ACKNOWLEDGE = Component.translatable("gui.acknowledge");
   public static final Component CONNECT_FAILED = Component.translatable("connect.failed");
   public static final Component NEW_LINE = Component.literal("\n");
   public static final Component NARRATION_SEPARATOR = Component.literal(". ");
   public static final Component ELLIPSIS = Component.literal("...");

   public static MutableComponent days(long p_239423_) {
      return Component.translatable("gui.days", p_239423_);
   }

   public static MutableComponent hours(long p_240042_) {
      return Component.translatable("gui.hours", p_240042_);
   }

   public static MutableComponent minutes(long p_239878_) {
      return Component.translatable("gui.minutes", p_239878_);
   }

   public static Component optionStatus(boolean p_130667_) {
      return p_130667_ ? OPTION_ON : OPTION_OFF;
   }

   public static MutableComponent optionStatus(Component p_130664_, boolean p_130665_) {
      return Component.translatable(p_130665_ ? "options.on.composed" : "options.off.composed", p_130664_);
   }

   public static MutableComponent optionNameValue(Component p_178394_, Component p_178395_) {
      return Component.translatable("options.generic_value", p_178394_, p_178395_);
   }

   public static MutableComponent joinForNarration(Component p_178399_, Component p_178400_) {
      return Component.empty().append(p_178399_).append(NARRATION_SEPARATOR).append(p_178400_);
   }

   public static Component joinLines(Component... p_178397_) {
      return joinLines(Arrays.asList(p_178397_));
   }

   public static Component joinLines(Collection<? extends Component> p_178392_) {
      return ComponentUtils.formatList(p_178392_, NEW_LINE);
   }
}