package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerFaceRenderer {
   public static final int SKIN_HEAD_U = 8;
   public static final int SKIN_HEAD_V = 8;
   public static final int SKIN_HEAD_WIDTH = 8;
   public static final int SKIN_HEAD_HEIGHT = 8;
   public static final int SKIN_HAT_U = 40;
   public static final int SKIN_HAT_V = 8;
   public static final int SKIN_HAT_WIDTH = 8;
   public static final int SKIN_HAT_HEIGHT = 8;
   public static final int SKIN_TEX_WIDTH = 64;
   public static final int SKIN_TEX_HEIGHT = 64;

   public static void draw(PoseStack p_240072_, int p_240073_, int p_240074_, int p_240075_) {
      draw(p_240072_, p_240073_, p_240074_, p_240075_, true, false);
   }

   public static void draw(PoseStack p_240133_, int p_240134_, int p_240135_, int p_240136_, boolean p_240137_, boolean p_240138_) {
      int i = 8 + (p_240138_ ? 8 : 0);
      int j = 8 * (p_240138_ ? -1 : 1);
      GuiComponent.blit(p_240133_, p_240134_, p_240135_, p_240136_, p_240136_, 8.0F, (float)i, 8, j, 64, 64);
      if (p_240137_) {
         drawHat(p_240133_, p_240134_, p_240135_, p_240136_, p_240138_);
      }

   }

   private static void drawHat(PoseStack p_240214_, int p_240215_, int p_240216_, int p_240217_, boolean p_240218_) {
      int i = 8 + (p_240218_ ? 8 : 0);
      int j = 8 * (p_240218_ ? -1 : 1);
      RenderSystem.enableBlend();
      GuiComponent.blit(p_240214_, p_240215_, p_240216_, p_240217_, p_240217_, 40.0F, (float)i, 8, j, 64, 64);
      RenderSystem.disableBlend();
   }
}