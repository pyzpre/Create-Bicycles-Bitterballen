package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.tree.CommandNode;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.chat.ChatPreviewAnimator;
import net.minecraft.client.gui.chat.ClientChatPreview;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.chat.ChatPreviewStatus;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PreviewableCommand;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class ChatScreen extends Screen {
   private static final int CHAT_SIGNING_PENDING_INDICATOR_COLOR = 15118153;
   private static final int CHAT_SIGNING_READY_INDICATOR_COLOR = 7844841;
   private static final int PREVIEW_HIGHLIGHT_COLOR = 10533887;
   public static final double MOUSE_SCROLL_SPEED = 7.0D;
   private static final Component USAGE_TEXT = Component.translatable("chat_screen.usage");
   private static final int PREVIEW_MARGIN_SIDES = 2;
   private static final int PREVIEW_PADDING = 2;
   private static final int PREVIEW_MARGIN_BOTTOM = 15;
   private static final Component PREVIEW_WARNING_TITLE = Component.translatable("chatPreview.warning.toast.title");
   private static final Component PREVIEW_WARNING_TOAST = Component.translatable("chatPreview.warning.toast");
   private static final Component PREVIEW_INPUT_HINT = Component.translatable("chat.previewInput", Component.translatable("key.keyboard.enter")).withStyle(ChatFormatting.DARK_GRAY);
   private static final int TOOLTIP_MAX_WIDTH = 260;
   private String historyBuffer = "";
   private int historyPos = -1;
   protected EditBox input;
   private String initial;
   CommandSuggestions commandSuggestions;
   private ClientChatPreview chatPreview;
   private ChatPreviewStatus chatPreviewStatus;
   private boolean previewNotRequired;
   private final ChatPreviewAnimator chatPreviewAnimator = new ChatPreviewAnimator();

   public ChatScreen(String p_95579_) {
      super(Component.translatable("chat_screen.title"));
      this.initial = p_95579_;
   }

   protected void init() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
      this.historyPos = this.minecraft.gui.getChat().getRecentChat().size();
      this.input = new EditBox(this.minecraft.fontFilterFishy, 4, this.height - 12, this.width - 4, 12, Component.translatable("chat.editBox")) {
         protected MutableComponent createNarrationMessage() {
            return super.createNarrationMessage().append(ChatScreen.this.commandSuggestions.getNarrationMessage());
         }
      };
      this.input.setMaxLength(256);
      this.input.setBordered(false);
      this.input.setValue(this.initial);
      this.input.setResponder(this::onEdited);
      this.addWidget(this.input);
      this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.input, this.font, false, false, 1, 10, true, -805306368);
      this.commandSuggestions.updateCommandInfo();
      this.setInitialFocus(this.input);
      this.chatPreviewAnimator.reset(Util.getMillis());
      this.chatPreview = new ClientChatPreview(this.minecraft);
      this.updateChatPreview(this.input.getValue());
      ServerData serverdata = this.minecraft.getCurrentServer();
      this.chatPreviewStatus = serverdata != null && !serverdata.previewsChat() ? ChatPreviewStatus.OFF : this.minecraft.options.chatPreview().get();
      if (serverdata != null && this.chatPreviewStatus != ChatPreviewStatus.OFF) {
         ServerData.ChatPreview serverdata$chatpreview = serverdata.getChatPreview();
         if (serverdata$chatpreview != null && serverdata.previewsChat() && serverdata$chatpreview.showToast()) {
            ServerList.saveSingleServer(serverdata);
            SystemToast systemtoast = SystemToast.multiline(this.minecraft, SystemToast.SystemToastIds.CHAT_PREVIEW_WARNING, PREVIEW_WARNING_TITLE, PREVIEW_WARNING_TOAST);
            this.minecraft.getToasts().addToast(systemtoast);
         }
      }

      if (this.chatPreviewStatus == ChatPreviewStatus.CONFIRM) {
         this.previewNotRequired = this.initial.startsWith("/") && !this.minecraft.player.commandHasSignableArguments(this.initial.substring(1));
      }

   }

   public void resize(Minecraft p_95600_, int p_95601_, int p_95602_) {
      String s = this.input.getValue();
      this.init(p_95600_, p_95601_, p_95602_);
      this.setChatLine(s);
      this.commandSuggestions.updateCommandInfo();
   }

   public void removed() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
      this.minecraft.gui.getChat().resetChatScroll();
   }

   public void tick() {
      this.input.tick();
      this.chatPreview.tick();
   }

   private void onEdited(String p_95611_) {
      String s = this.input.getValue();
      this.commandSuggestions.setAllowSuggestions(!s.equals(this.initial));
      this.commandSuggestions.updateCommandInfo();
      if (this.chatPreviewStatus == ChatPreviewStatus.LIVE) {
         this.updateChatPreview(s);
      } else if (this.chatPreviewStatus == ChatPreviewStatus.CONFIRM && !this.chatPreview.queryEquals(s)) {
         this.previewNotRequired = s.startsWith("/") && !this.minecraft.player.commandHasSignableArguments(s.substring(1));
         this.chatPreview.update("");
      }

   }

   private void updateChatPreview(String p_232719_) {
      String s = this.normalizeChatMessage(p_232719_);
      if (this.sendsChatPreviewRequests()) {
         this.requestPreview(s);
      } else {
         this.chatPreview.disable();
      }

   }

   private void requestPreview(String p_232721_) {
      if (p_232721_.startsWith("/")) {
         this.requestCommandArgumentPreview(p_232721_);
      } else {
         this.requestChatMessagePreview(p_232721_);
      }

   }

   private void requestChatMessagePreview(String p_232723_) {
      this.chatPreview.update(p_232723_);
   }

   private void requestCommandArgumentPreview(String p_232725_) {
      ParseResults<SharedSuggestionProvider> parseresults = this.commandSuggestions.getCurrentContext();
      CommandNode<SharedSuggestionProvider> commandnode = this.commandSuggestions.getNodeAt(this.input.getCursorPosition());
      if (parseresults != null && commandnode != null && PreviewableCommand.of(parseresults).isPreviewed(commandnode)) {
         this.chatPreview.update(p_232725_);
      } else {
         this.chatPreview.disable();
      }

   }

   private boolean sendsChatPreviewRequests() {
      if (this.minecraft.player == null) {
         return false;
      } else if (this.minecraft.isLocalServer()) {
         return true;
      } else if (this.chatPreviewStatus == ChatPreviewStatus.OFF) {
         return false;
      } else {
         ServerData serverdata = this.minecraft.getCurrentServer();
         return serverdata != null && serverdata.previewsChat();
      }
   }

   public boolean keyPressed(int p_95591_, int p_95592_, int p_95593_) {
      if (this.commandSuggestions.keyPressed(p_95591_, p_95592_, p_95593_)) {
         return true;
      } else if (super.keyPressed(p_95591_, p_95592_, p_95593_)) {
         return true;
      } else if (p_95591_ == 256) {
         this.minecraft.setScreen((Screen)null);
         return true;
      } else if (p_95591_ != 257 && p_95591_ != 335) {
         if (p_95591_ == 265) {
            this.moveInHistory(-1);
            return true;
         } else if (p_95591_ == 264) {
            this.moveInHistory(1);
            return true;
         } else if (p_95591_ == 266) {
            this.minecraft.gui.getChat().scrollChat(this.minecraft.gui.getChat().getLinesPerPage() - 1);
            return true;
         } else if (p_95591_ == 267) {
            this.minecraft.gui.getChat().scrollChat(-this.minecraft.gui.getChat().getLinesPerPage() + 1);
            return true;
         } else {
            return false;
         }
      } else {
         if (this.handleChatInput(this.input.getValue(), true)) {
            this.minecraft.setScreen((Screen)null);
         }

         return true;
      }
   }

   public boolean mouseScrolled(double p_95581_, double p_95582_, double p_95583_) {
      p_95583_ = Mth.clamp(p_95583_, -1.0D, 1.0D);
      if (this.commandSuggestions.mouseScrolled(p_95583_)) {
         return true;
      } else {
         if (!hasShiftDown()) {
            p_95583_ *= 7.0D;
         }

         this.minecraft.gui.getChat().scrollChat((int)p_95583_);
         return true;
      }
   }

   public boolean mouseClicked(double p_95585_, double p_95586_, int p_95587_) {
      if (this.commandSuggestions.mouseClicked((double)((int)p_95585_), (double)((int)p_95586_), p_95587_)) {
         return true;
      } else {
         if (p_95587_ == 0) {
            ChatComponent chatcomponent = this.minecraft.gui.getChat();
            if (chatcomponent.handleChatQueueClicked(p_95585_, p_95586_)) {
               return true;
            }

            Style style = this.getComponentStyleAt(p_95585_, p_95586_);
            if (style != null && this.handleComponentClicked(style)) {
               this.initial = this.input.getValue();
               return true;
            }
         }

         return this.input.mouseClicked(p_95585_, p_95586_, p_95587_) ? true : super.mouseClicked(p_95585_, p_95586_, p_95587_);
      }
   }

   protected void insertText(String p_95606_, boolean p_95607_) {
      if (p_95607_) {
         this.input.setValue(p_95606_);
      } else {
         this.input.insertText(p_95606_);
      }

   }

   public void moveInHistory(int p_95589_) {
      int i = this.historyPos + p_95589_;
      int j = this.minecraft.gui.getChat().getRecentChat().size();
      i = Mth.clamp(i, 0, j);
      if (i != this.historyPos) {
         if (i == j) {
            this.historyPos = j;
            this.input.setValue(this.historyBuffer);
         } else {
            if (this.historyPos == j) {
               this.historyBuffer = this.input.getValue();
            }

            this.input.setValue(this.minecraft.gui.getChat().getRecentChat().get(i));
            this.commandSuggestions.setAllowSuggestions(false);
            this.historyPos = i;
         }
      }
   }

   public void render(PoseStack p_95595_, int p_95596_, int p_95597_, float p_95598_) {
      this.setFocused(this.input);
      this.input.setFocus(true);
      fill(p_95595_, 2, this.height - 14, this.width - 2, this.height - 2, this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE));
      this.input.render(p_95595_, p_95596_, p_95597_, p_95598_);
      super.render(p_95595_, p_95596_, p_95597_, p_95598_);
      boolean flag = this.minecraft.getProfileKeyPairManager().signer() != null;
      ChatPreviewAnimator.State chatpreviewanimator$state = this.chatPreviewAnimator.get(Util.getMillis(), this.getDisplayedPreviewText());
      if (chatpreviewanimator$state.preview() != null) {
         this.renderChatPreview(p_95595_, chatpreviewanimator$state.preview(), chatpreviewanimator$state.alpha(), flag);
         this.commandSuggestions.renderSuggestions(p_95595_, p_95596_, p_95597_);
      } else {
         this.commandSuggestions.render(p_95595_, p_95596_, p_95597_);
         if (flag) {
            p_95595_.pushPose();
            fill(p_95595_, 0, this.height - 14, 2, this.height - 2, -8932375);
            p_95595_.popPose();
         }
      }

      Style style = this.getComponentStyleAt((double)p_95596_, (double)p_95597_);
      if (style != null && style.getHoverEvent() != null) {
         this.renderComponentHoverEffect(p_95595_, style, p_95596_, p_95597_);
      } else {
         GuiMessageTag guimessagetag = this.minecraft.gui.getChat().getMessageTagAt((double)p_95596_, (double)p_95597_);
         if (guimessagetag != null && guimessagetag.text() != null) {
            this.renderTooltip(p_95595_, this.font.split(guimessagetag.text(), 260), p_95596_, p_95597_);
         }
      }

   }

   @Nullable
   protected Component getDisplayedPreviewText() {
      String s = this.input.getValue();
      if (s.isBlank()) {
         return null;
      } else {
         Component component = this.peekPreview();
         return this.chatPreviewStatus == ChatPreviewStatus.CONFIRM && !this.previewNotRequired ? Objects.requireNonNullElse(component, (Component)(this.chatPreview.queryEquals(s) && !s.startsWith("/") ? Component.literal(s) : PREVIEW_INPUT_HINT)) : component;
      }
   }

   public boolean isPauseScreen() {
      return false;
   }

   private void setChatLine(String p_95613_) {
      this.input.setValue(p_95613_);
   }

   protected void updateNarrationState(NarrationElementOutput p_169238_) {
      p_169238_.add(NarratedElementType.TITLE, this.getTitle());
      p_169238_.add(NarratedElementType.USAGE, USAGE_TEXT);
      String s = this.input.getValue();
      if (!s.isEmpty()) {
         p_169238_.nest().add(NarratedElementType.TITLE, Component.translatable("chat_screen.message", s));
      }

   }

   public void renderChatPreview(PoseStack p_242432_, Component p_242318_, float p_242443_, boolean p_242189_) {
      int i = (int)(255.0D * (this.minecraft.options.chatOpacity().get() * (double)0.9F + (double)0.1F) * (double)p_242443_);
      int j = (int)((double)(this.chatPreview.hasScheduledRequest() ? 127 : 255) * this.minecraft.options.textBackgroundOpacity().get() * (double)p_242443_);
      int k = this.chatPreviewWidth();
      List<FormattedCharSequence> list = this.splitChatPreview(p_242318_);
      int l = this.chatPreviewHeight(list);
      int i1 = this.chatPreviewTop(l);
      RenderSystem.enableBlend();
      p_242432_.pushPose();
      p_242432_.translate((double)this.chatPreviewLeft(), (double)i1, 0.0D);
      fill(p_242432_, 0, 0, k, l, j << 24);
      if (i > 0) {
         p_242432_.translate(2.0D, 2.0D, 0.0D);

         for(int j1 = 0; j1 < list.size(); ++j1) {
            FormattedCharSequence formattedcharsequence = list.get(j1);
            int k1 = j1 * 9;
            this.renderChatPreviewHighlights(p_242432_, formattedcharsequence, k1, i);
            this.font.drawShadow(p_242432_, formattedcharsequence, 0.0F, (float)k1, i << 24 | 16777215);
         }
      }

      p_242432_.popPose();
      RenderSystem.disableBlend();
      if (p_242189_ && this.chatPreview.peek() != null) {
         int l1 = this.chatPreview.hasScheduledRequest() ? 15118153 : 7844841;
         int i2 = (int)(255.0F * p_242443_);
         p_242432_.pushPose();
         fill(p_242432_, 0, i1, 2, this.chatPreviewBottom(), i2 << 24 | l1);
         p_242432_.popPose();
      }

   }

   private void renderChatPreviewHighlights(PoseStack p_242454_, FormattedCharSequence p_242367_, int p_242163_, int p_242358_) {
      int i = p_242163_ + 9;
      int j = p_242358_ << 24 | 10533887;
      Predicate<Style> predicate = (p_242204_) -> {
         return p_242204_.getHoverEvent() != null || p_242204_.getClickEvent() != null;
      };

      for(StringSplitter.Span stringsplitter$span : this.font.getSplitter().findSpans(p_242367_, predicate)) {
         int k = Mth.floor(stringsplitter$span.left());
         int l = Mth.ceil(stringsplitter$span.right());
         fill(p_242454_, k, p_242163_, l, i, j);
      }

   }

   @Nullable
   private Style getComponentStyleAt(double p_232702_, double p_232703_) {
      Style style = this.minecraft.gui.getChat().getClickedComponentStyleAt(p_232702_, p_232703_);
      if (style == null) {
         style = this.getChatPreviewStyleAt(p_232702_, p_232703_);
      }

      return style;
   }

   @Nullable
   private Style getChatPreviewStyleAt(double p_232716_, double p_232717_) {
      if (this.minecraft.options.hideGui) {
         return null;
      } else {
         Component component = this.peekPreview();
         if (component == null) {
            return null;
         } else {
            List<FormattedCharSequence> list = this.splitChatPreview(component);
            int i = this.chatPreviewHeight(list);
            if (!(p_232716_ < (double)this.chatPreviewLeft()) && !(p_232716_ > (double)this.chatPreviewRight()) && !(p_232717_ < (double)this.chatPreviewTop(i)) && !(p_232717_ > (double)this.chatPreviewBottom())) {
               int j = this.chatPreviewLeft() + 2;
               int k = this.chatPreviewTop(i) + 2;
               int l = (Mth.floor(p_232717_) - k) / 9;
               if (l >= 0 && l < list.size()) {
                  FormattedCharSequence formattedcharsequence = list.get(l);
                  return this.minecraft.font.getSplitter().componentStyleAtWidth(formattedcharsequence, (int)(p_232716_ - (double)j));
               } else {
                  return null;
               }
            } else {
               return null;
            }
         }
      }
   }

   @Nullable
   private Component peekPreview() {
      return Util.mapNullable(this.chatPreview.peek(), ClientChatPreview.Preview::response);
   }

   private List<FormattedCharSequence> splitChatPreview(Component p_242266_) {
      return this.font.split(p_242266_, this.chatPreviewWidth());
   }

   private int chatPreviewWidth() {
      return this.minecraft.screen.width - 4;
   }

   private int chatPreviewHeight(List<FormattedCharSequence> p_232714_) {
      return Math.max(p_232714_.size(), 1) * 9 + 4;
   }

   private int chatPreviewBottom() {
      return this.minecraft.screen.height - 15;
   }

   private int chatPreviewTop(int p_232709_) {
      return this.chatPreviewBottom() - p_232709_;
   }

   private int chatPreviewLeft() {
      return 2;
   }

   private int chatPreviewRight() {
      return this.minecraft.screen.width - 2;
   }

   public boolean handleChatInput(String p_242400_, boolean p_242161_) {
      p_242400_ = this.normalizeChatMessage(p_242400_);
      if (p_242400_.isEmpty()) {
         return true;
      } else {
         if (this.chatPreviewStatus == ChatPreviewStatus.CONFIRM && !this.previewNotRequired) {
            this.commandSuggestions.hide();
            if (!this.chatPreview.queryEquals(p_242400_)) {
               this.updateChatPreview(p_242400_);
               return false;
            }
         }

         if (p_242161_) {
            this.minecraft.gui.getChat().addRecentChat(p_242400_);
         }

         Component component = Util.mapNullable(this.chatPreview.pull(p_242400_), ClientChatPreview.Preview::response);
         if (p_242400_.startsWith("/")) {
            this.minecraft.player.commandSigned(p_242400_.substring(1), component);
         } else {
            this.minecraft.player.chatSigned(p_242400_, component);
         }

         return true;
      }
   }

   public String normalizeChatMessage(String p_232707_) {
      return StringUtils.normalizeSpace(p_232707_.trim());
   }

   public ClientChatPreview getChatPreview() {
      return this.chatPreview;
   }
}