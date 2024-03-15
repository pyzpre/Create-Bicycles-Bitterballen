package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShareToLanScreen extends Screen {
   private static final Component ALLOW_COMMANDS_LABEL = Component.translatable("selectWorld.allowCommands");
   private static final Component GAME_MODE_LABEL = Component.translatable("selectWorld.gameMode");
   private static final Component INFO_TEXT = Component.translatable("lanServer.otherPlayers");
   private final Screen lastScreen;
   private GameType gameMode = GameType.SURVIVAL;
   private boolean commands;

   public ShareToLanScreen(Screen p_96650_) {
      super(Component.translatable("lanServer.title"));
      this.lastScreen = p_96650_;
   }

   protected void init() {
      this.addRenderableWidget(CycleButton.builder(GameType::getShortDisplayName).withValues(GameType.SURVIVAL, GameType.SPECTATOR, GameType.CREATIVE, GameType.ADVENTURE).withInitialValue(this.gameMode).create(this.width / 2 - 155, 100, 150, 20, GAME_MODE_LABEL, (p_169429_, p_169430_) -> {
         this.gameMode = p_169430_;
      }));
      this.addRenderableWidget(CycleButton.onOffBuilder(this.commands).create(this.width / 2 + 5, 100, 150, 20, ALLOW_COMMANDS_LABEL, (p_169432_, p_169433_) -> {
         this.commands = p_169433_;
      }));
      this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 28, 150, 20, Component.translatable("lanServer.start"), (p_96660_) -> {
         this.minecraft.setScreen((Screen)null);
         int i = HttpUtil.getAvailablePort();
         Component component;
         if (this.minecraft.getSingleplayerServer().publishServer(this.gameMode, this.commands, i)) {
            component = Component.translatable("commands.publish.started", i);
         } else {
            component = Component.translatable("commands.publish.failed");
         }

         this.minecraft.gui.getChat().addMessage(component);
         this.minecraft.updateTitle();
      }));
      this.addRenderableWidget(new Button(this.width / 2 + 5, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, (p_96657_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }));
   }

   public void render(PoseStack p_96652_, int p_96653_, int p_96654_, float p_96655_) {
      this.renderBackground(p_96652_);
      drawCenteredString(p_96652_, this.font, this.title, this.width / 2, 50, 16777215);
      drawCenteredString(p_96652_, this.font, INFO_TEXT, this.width / 2, 82, 16777215);
      super.render(p_96652_, p_96653_, p_96654_, p_96655_);
   }
}