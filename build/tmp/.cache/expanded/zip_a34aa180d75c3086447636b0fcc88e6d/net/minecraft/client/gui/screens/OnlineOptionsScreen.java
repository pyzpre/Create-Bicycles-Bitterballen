package net.minecraft.client.gui.screens;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OnlineOptionsScreen extends SimpleOptionsSubScreen {
   public OnlineOptionsScreen(Screen p_193843_, Options p_193844_) {
      super(p_193843_, p_193844_, Component.translatable("options.online.title"), new OptionInstance[]{p_193844_.realmsNotifications(), p_193844_.allowServerListing()});
   }

   protected void createFooter() {
      if (this.minecraft.level != null) {
         CycleButton<Difficulty> cyclebutton = this.addRenderableWidget(OptionsScreen.createDifficultyButton(this.smallOptions.length, this.width, this.height, "options.difficulty.online", this.minecraft));
         cyclebutton.active = false;
      }

      super.createFooter();
   }
}