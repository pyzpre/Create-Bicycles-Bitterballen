package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class InBedChatScreen extends ChatScreen {
   private Button leaveBedButton;

   public InBedChatScreen() {
      super("");
   }

   protected void init() {
      super.init();
      this.leaveBedButton = this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 40, 200, 20, Component.translatable("multiplayer.stopSleeping"), (p_96074_) -> {
         this.sendWakeUp();
      }));
   }

   public void render(PoseStack p_242941_, int p_242857_, int p_242871_, float p_242925_) {
      this.leaveBedButton.visible = this.getDisplayedPreviewText() == null;
      super.render(p_242941_, p_242857_, p_242871_, p_242925_);
   }

   public void onClose() {
      this.sendWakeUp();
   }

   public boolean keyPressed(int p_96070_, int p_96071_, int p_96072_) {
      if (p_96070_ == 256) {
         this.sendWakeUp();
      } else if (p_96070_ == 257 || p_96070_ == 335) {
         if (this.handleChatInput(this.input.getValue(), true)) {
            this.minecraft.setScreen((Screen)null);
            this.input.setValue("");
            this.minecraft.gui.getChat().resetChatScroll();
         }

         return true;
      }

      return super.keyPressed(p_96070_, p_96071_, p_96072_);
   }

   private void sendWakeUp() {
      ClientPacketListener clientpacketlistener = this.minecraft.player.connection;
      clientpacketlistener.send(new ServerboundPlayerCommandPacket(this.minecraft.player, ServerboundPlayerCommandPacket.Action.STOP_SLEEPING));
   }

   public void onPlayerWokeUp() {
      if (this.input.getValue().isEmpty()) {
         this.minecraft.setScreen((Screen)null);
      } else {
         this.minecraft.setScreen(new ChatScreen(this.input.getValue()));
      }

   }
}