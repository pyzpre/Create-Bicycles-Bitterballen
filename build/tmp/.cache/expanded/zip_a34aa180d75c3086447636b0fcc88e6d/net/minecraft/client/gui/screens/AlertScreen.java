package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AlertScreen extends Screen {
   private static final int LABEL_Y = 90;
   private final Component messageText;
   private MultiLineLabel message = MultiLineLabel.EMPTY;
   private final Runnable callback;
   private final Component okButton;
   private final boolean shouldCloseOnEsc;

   public AlertScreen(Runnable p_95519_, Component p_95520_, Component p_95521_) {
      this(p_95519_, p_95520_, p_95521_, CommonComponents.GUI_BACK, true);
   }

   public AlertScreen(Runnable p_239327_, Component p_239328_, Component p_239329_, Component p_239330_, boolean p_239331_) {
      super(p_239328_);
      this.callback = p_239327_;
      this.messageText = p_239329_;
      this.okButton = p_239330_;
      this.shouldCloseOnEsc = p_239331_;
   }

   public Component getNarrationMessage() {
      return CommonComponents.joinForNarration(super.getNarrationMessage(), this.messageText);
   }

   protected void init() {
      super.init();
      this.message = MultiLineLabel.create(this.font, this.messageText, this.width - 50);
      int i = this.message.getLineCount() * 9;
      int j = Mth.clamp(90 + i + 12, this.height / 6 + 96, this.height - 24);
      int k = 150;
      this.addRenderableWidget(new Button((this.width - 150) / 2, j, 150, 20, this.okButton, (p_95533_) -> {
         this.callback.run();
      }));
   }

   public void render(PoseStack p_95528_, int p_95529_, int p_95530_, float p_95531_) {
      this.renderBackground(p_95528_);
      drawCenteredString(p_95528_, this.font, this.title, this.width / 2, 70, 16777215);
      this.message.renderCentered(p_95528_, this.width / 2, 90);
      super.render(p_95528_, p_95529_, p_95530_, p_95531_);
   }

   public boolean shouldCloseOnEsc() {
      return this.shouldCloseOnEsc;
   }
}