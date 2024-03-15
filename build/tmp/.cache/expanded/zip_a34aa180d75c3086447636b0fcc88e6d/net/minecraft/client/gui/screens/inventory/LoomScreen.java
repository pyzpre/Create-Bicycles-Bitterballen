package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LoomScreen extends AbstractContainerScreen<LoomMenu> {
   private static final ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/loom.png");
   private static final int PATTERN_COLUMNS = 4;
   private static final int PATTERN_ROWS = 4;
   private static final int SCROLLER_WIDTH = 12;
   private static final int SCROLLER_HEIGHT = 15;
   private static final int PATTERN_IMAGE_SIZE = 14;
   private static final int SCROLLER_FULL_HEIGHT = 56;
   private static final int PATTERNS_X = 60;
   private static final int PATTERNS_Y = 13;
   private ModelPart flag;
   @Nullable
   private List<Pair<Holder<BannerPattern>, DyeColor>> resultBannerPatterns;
   private ItemStack bannerStack = ItemStack.EMPTY;
   private ItemStack dyeStack = ItemStack.EMPTY;
   private ItemStack patternStack = ItemStack.EMPTY;
   private boolean displayPatterns;
   private boolean hasMaxPatterns;
   private float scrollOffs;
   private boolean scrolling;
   private int startRow;

   public LoomScreen(LoomMenu p_99075_, Inventory p_99076_, Component p_99077_) {
      super(p_99075_, p_99076_, p_99077_);
      p_99075_.registerUpdateListener(this::containerChanged);
      this.titleLabelY -= 2;
   }

   protected void init() {
      super.init();
      this.flag = this.minecraft.getEntityModels().bakeLayer(ModelLayers.BANNER).getChild("flag");
   }

   public void render(PoseStack p_99104_, int p_99105_, int p_99106_, float p_99107_) {
      super.render(p_99104_, p_99105_, p_99106_, p_99107_);
      this.renderTooltip(p_99104_, p_99105_, p_99106_);
   }

   private int totalRowCount() {
      return Mth.positiveCeilDiv(this.menu.getSelectablePatterns().size(), 4);
   }

   protected void renderBg(PoseStack p_99099_, float p_99100_, int p_99101_, int p_99102_) {
      this.renderBackground(p_99099_);
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, BG_LOCATION);
      int i = this.leftPos;
      int j = this.topPos;
      this.blit(p_99099_, i, j, 0, 0, this.imageWidth, this.imageHeight);
      Slot slot = this.menu.getBannerSlot();
      Slot slot1 = this.menu.getDyeSlot();
      Slot slot2 = this.menu.getPatternSlot();
      Slot slot3 = this.menu.getResultSlot();
      if (!slot.hasItem()) {
         this.blit(p_99099_, i + slot.x, j + slot.y, this.imageWidth, 0, 16, 16);
      }

      if (!slot1.hasItem()) {
         this.blit(p_99099_, i + slot1.x, j + slot1.y, this.imageWidth + 16, 0, 16, 16);
      }

      if (!slot2.hasItem()) {
         this.blit(p_99099_, i + slot2.x, j + slot2.y, this.imageWidth + 32, 0, 16, 16);
      }

      int k = (int)(41.0F * this.scrollOffs);
      this.blit(p_99099_, i + 119, j + 13 + k, 232 + (this.displayPatterns ? 0 : 12), 0, 12, 15);
      Lighting.setupForFlatItems();
      if (this.resultBannerPatterns != null && !this.hasMaxPatterns) {
         MultiBufferSource.BufferSource multibuffersource$buffersource = this.minecraft.renderBuffers().bufferSource();
         p_99099_.pushPose();
         p_99099_.translate((double)(i + 139), (double)(j + 52), 0.0D);
         p_99099_.scale(24.0F, -24.0F, 1.0F);
         p_99099_.translate(0.5D, 0.5D, 0.5D);
         float f = 0.6666667F;
         p_99099_.scale(0.6666667F, -0.6666667F, -0.6666667F);
         this.flag.xRot = 0.0F;
         this.flag.y = -32.0F;
         BannerRenderer.renderPatterns(p_99099_, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, this.flag, ModelBakery.BANNER_BASE, true, this.resultBannerPatterns);
         p_99099_.popPose();
         multibuffersource$buffersource.endBatch();
      } else if (this.hasMaxPatterns) {
         this.blit(p_99099_, i + slot3.x - 2, j + slot3.y - 2, this.imageWidth, 17, 17, 16);
      }

      if (this.displayPatterns) {
         int k2 = i + 60;
         int l2 = j + 13;
         List<Holder<BannerPattern>> list = this.menu.getSelectablePatterns();

         label63:
         for(int l = 0; l < 4; ++l) {
            for(int i1 = 0; i1 < 4; ++i1) {
               int j1 = l + this.startRow;
               int k1 = j1 * 4 + i1;
               if (k1 >= list.size()) {
                  break label63;
               }

               RenderSystem.setShaderTexture(0, BG_LOCATION);
               int l1 = k2 + i1 * 14;
               int i2 = l2 + l * 14;
               boolean flag = p_99101_ >= l1 && p_99102_ >= i2 && p_99101_ < l1 + 14 && p_99102_ < i2 + 14;
               int j2;
               if (k1 == this.menu.getSelectedBannerPatternIndex()) {
                  j2 = this.imageHeight + 14;
               } else if (flag) {
                  j2 = this.imageHeight + 28;
               } else {
                  j2 = this.imageHeight;
               }

               this.blit(p_99099_, l1, i2, 0, j2, 14, 14);
               this.renderPattern(list.get(k1), l1, i2);
            }
         }
      }

      Lighting.setupFor3DItems();
   }

   private void renderPattern(Holder<BannerPattern> p_232825_, int p_232826_, int p_232827_) {
      CompoundTag compoundtag = new CompoundTag();
      ListTag listtag = (new BannerPattern.Builder()).addPattern(BannerPatterns.BASE, DyeColor.GRAY).addPattern(p_232825_, DyeColor.WHITE).toListTag();
      compoundtag.put("Patterns", listtag);
      ItemStack itemstack = new ItemStack(Items.GRAY_BANNER);
      BlockItem.setBlockEntityData(itemstack, BlockEntityType.BANNER, compoundtag);
      PoseStack posestack = new PoseStack();
      posestack.pushPose();
      posestack.translate((double)((float)p_232826_ + 0.5F), (double)(p_232827_ + 16), 0.0D);
      posestack.scale(6.0F, -6.0F, 1.0F);
      posestack.translate(0.5D, 0.5D, 0.0D);
      posestack.translate(0.5D, 0.5D, 0.5D);
      float f = 0.6666667F;
      posestack.scale(0.6666667F, -0.6666667F, -0.6666667F);
      MultiBufferSource.BufferSource multibuffersource$buffersource = this.minecraft.renderBuffers().bufferSource();
      this.flag.xRot = 0.0F;
      this.flag.y = -32.0F;
      List<Pair<Holder<BannerPattern>, DyeColor>> list = BannerBlockEntity.createPatterns(DyeColor.GRAY, BannerBlockEntity.getItemPatterns(itemstack));
      BannerRenderer.renderPatterns(posestack, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, this.flag, ModelBakery.BANNER_BASE, true, list);
      posestack.popPose();
      multibuffersource$buffersource.endBatch();
   }

   public boolean mouseClicked(double p_99083_, double p_99084_, int p_99085_) {
      this.scrolling = false;
      if (this.displayPatterns) {
         int i = this.leftPos + 60;
         int j = this.topPos + 13;

         for(int k = 0; k < 4; ++k) {
            for(int l = 0; l < 4; ++l) {
               double d0 = p_99083_ - (double)(i + l * 14);
               double d1 = p_99084_ - (double)(j + k * 14);
               int i1 = k + this.startRow;
               int j1 = i1 * 4 + l;
               if (d0 >= 0.0D && d1 >= 0.0D && d0 < 14.0D && d1 < 14.0D && this.menu.clickMenuButton(this.minecraft.player, j1)) {
                  Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_LOOM_SELECT_PATTERN, 1.0F));
                  this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, j1);
                  return true;
               }
            }
         }

         i = this.leftPos + 119;
         j = this.topPos + 9;
         if (p_99083_ >= (double)i && p_99083_ < (double)(i + 12) && p_99084_ >= (double)j && p_99084_ < (double)(j + 56)) {
            this.scrolling = true;
         }
      }

      return super.mouseClicked(p_99083_, p_99084_, p_99085_);
   }

   public boolean mouseDragged(double p_99087_, double p_99088_, int p_99089_, double p_99090_, double p_99091_) {
      int i = this.totalRowCount() - 4;
      if (this.scrolling && this.displayPatterns && i > 0) {
         int j = this.topPos + 13;
         int k = j + 56;
         this.scrollOffs = ((float)p_99088_ - (float)j - 7.5F) / ((float)(k - j) - 15.0F);
         this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
         this.startRow = Math.max((int)((double)(this.scrollOffs * (float)i) + 0.5D), 0);
         return true;
      } else {
         return super.mouseDragged(p_99087_, p_99088_, p_99089_, p_99090_, p_99091_);
      }
   }

   public boolean mouseScrolled(double p_99079_, double p_99080_, double p_99081_) {
      int i = this.totalRowCount() - 4;
      if (this.displayPatterns && i > 0) {
         float f = (float)p_99081_ / (float)i;
         this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
         this.startRow = Math.max((int)(this.scrollOffs * (float)i + 0.5F), 0);
      }

      return true;
   }

   protected boolean hasClickedOutside(double p_99093_, double p_99094_, int p_99095_, int p_99096_, int p_99097_) {
      return p_99093_ < (double)p_99095_ || p_99094_ < (double)p_99096_ || p_99093_ >= (double)(p_99095_ + this.imageWidth) || p_99094_ >= (double)(p_99096_ + this.imageHeight);
   }

   private void containerChanged() {
      ItemStack itemstack = this.menu.getResultSlot().getItem();
      if (itemstack.isEmpty()) {
         this.resultBannerPatterns = null;
      } else {
         this.resultBannerPatterns = BannerBlockEntity.createPatterns(((BannerItem)itemstack.getItem()).getColor(), BannerBlockEntity.getItemPatterns(itemstack));
      }

      ItemStack itemstack1 = this.menu.getBannerSlot().getItem();
      ItemStack itemstack2 = this.menu.getDyeSlot().getItem();
      ItemStack itemstack3 = this.menu.getPatternSlot().getItem();
      CompoundTag compoundtag = BlockItem.getBlockEntityData(itemstack1);
      this.hasMaxPatterns = compoundtag != null && compoundtag.contains("Patterns", 9) && !itemstack1.isEmpty() && compoundtag.getList("Patterns", 10).size() >= 6;
      if (this.hasMaxPatterns) {
         this.resultBannerPatterns = null;
      }

      if (!ItemStack.matches(itemstack1, this.bannerStack) || !ItemStack.matches(itemstack2, this.dyeStack) || !ItemStack.matches(itemstack3, this.patternStack)) {
         this.displayPatterns = !itemstack1.isEmpty() && !itemstack2.isEmpty() && !this.hasMaxPatterns && !this.menu.getSelectablePatterns().isEmpty();
      }

      if (this.startRow >= this.totalRowCount()) {
         this.startRow = 0;
         this.scrollOffs = 0.0F;
      }

      this.bannerStack = itemstack1.copy();
      this.dyeStack = itemstack2.copy();
      this.patternStack = itemstack3.copy();
   }
}