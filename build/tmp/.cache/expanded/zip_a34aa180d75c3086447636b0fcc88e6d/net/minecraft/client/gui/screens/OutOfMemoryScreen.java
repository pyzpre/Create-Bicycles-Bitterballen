package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OutOfMemoryScreen extends Screen {
   private MultiLineLabel message = MultiLineLabel.EMPTY;

   public OutOfMemoryScreen() {
      super(Component.translatable("outOfMemory.error"));
   }

   protected void init() {
      this.addRenderableWidget(new Button(this.width / 2 - 155, this.height / 4 + 120 + 12, 150, 20, Component.translatable("gui.toTitle"), (p_96304_) -> {
         this.minecraft.setScreen(new TitleScreen());
      }));
      this.addRenderableWidget(new Button(this.width / 2 - 155 + 160, this.height / 4 + 120 + 12, 150, 20, Component.translatable("menu.quit"), (p_96300_) -> {
         this.minecraft.stop();
      }));
      this.message = MultiLineLabel.create(this.font, Component.translatable("outOfMemory.message"), 295);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public void render(PoseStack p_96295_, int p_96296_, int p_96297_, float p_96298_) {
      this.renderBackground(p_96295_);
      drawCenteredString(p_96295_, this.font, this.title, this.width / 2, this.height / 4 - 60 + 20, 16777215);
      this.message.renderLeftAligned(p_96295_, this.width / 2 - 145, this.height / 4, 9, 10526880);
      super.render(p_96295_, p_96296_, p_96297_, p_96298_);
   }
}