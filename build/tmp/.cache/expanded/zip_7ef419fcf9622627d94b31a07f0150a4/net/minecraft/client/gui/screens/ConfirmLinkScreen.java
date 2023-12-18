package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConfirmLinkScreen extends ConfirmScreen {
   private static final Component COPY_BUTTON_TEXT = Component.translatable("chat.copy");
   private static final Component WARNING_TEXT = Component.translatable("chat.link.warning");
   private final String url;
   private final boolean showWarning;

   public ConfirmLinkScreen(BooleanConsumer p_95631_, String p_95632_, boolean p_95633_) {
      this(p_95631_, confirmMessage(p_95633_), Component.literal(p_95632_), p_95632_, p_95633_ ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_NO, p_95633_);
   }

   public ConfirmLinkScreen(BooleanConsumer p_238329_, Component p_238330_, String p_238331_, boolean p_238332_) {
      this(p_238329_, p_238330_, p_238331_, p_238332_ ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_NO, p_238332_);
   }

   public ConfirmLinkScreen(BooleanConsumer p_239991_, Component p_239992_, String p_239993_, Component p_239994_, boolean p_239995_) {
      this(p_239991_, p_239992_, confirmMessage(p_239995_, p_239993_), p_239993_, p_239994_, p_239995_);
   }

   public ConfirmLinkScreen(BooleanConsumer p_240191_, Component p_240192_, Component p_240193_, String p_240194_, Component p_240195_, boolean p_240196_) {
      super(p_240191_, p_240192_, p_240193_);
      this.yesButton = (Component)(p_240196_ ? Component.translatable("chat.link.open") : CommonComponents.GUI_YES);
      this.noButton = p_240195_;
      this.showWarning = !p_240196_;
      this.url = p_240194_;
   }

   protected static MutableComponent confirmMessage(boolean p_239180_, String p_239181_) {
      return confirmMessage(p_239180_).append(" ").append(Component.literal(p_239181_));
   }

   protected static MutableComponent confirmMessage(boolean p_240014_) {
      return Component.translatable(p_240014_ ? "chat.link.confirmTrusted" : "chat.link.confirm");
   }

   protected void addButtons(int p_169243_) {
      this.addRenderableWidget(new Button(this.width / 2 - 50 - 105, p_169243_, 100, 20, this.yesButton, (p_169249_) -> {
         this.callback.accept(true);
      }));
      this.addRenderableWidget(new Button(this.width / 2 - 50, p_169243_, 100, 20, COPY_BUTTON_TEXT, (p_169247_) -> {
         this.copyToClipboard();
         this.callback.accept(false);
      }));
      this.addRenderableWidget(new Button(this.width / 2 - 50 + 105, p_169243_, 100, 20, this.noButton, (p_169245_) -> {
         this.callback.accept(false);
      }));
   }

   public void copyToClipboard() {
      this.minecraft.keyboardHandler.setClipboard(this.url);
   }

   public void render(PoseStack p_95635_, int p_95636_, int p_95637_, float p_95638_) {
      super.render(p_95635_, p_95636_, p_95637_, p_95638_);
      if (this.showWarning) {
         drawCenteredString(p_95635_, this.font, WARNING_TEXT, this.width / 2, 110, 16764108);
      }

   }
}