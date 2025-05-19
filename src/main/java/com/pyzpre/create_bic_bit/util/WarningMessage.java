package com.pyzpre.create_bic_bit.util;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class WarningMessage extends Screen {
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("create_bic_bit", "textures/screens/tulipbanner.png");
    private static final ResourceLocation CENTER_TEXTURE = new ResourceLocation("create_bic_bit", "textures/screens/blue.png");
    private final Screen parentScreen;
    private final String message;
    public WarningMessage(Screen parentScreen, String message) {
        super(Component.literal("Mod Message"));
        this.parentScreen = parentScreen;
        this.message = message;
    }

    @Override
    protected void init() {
        // Add an "Okay" button
        int buttonWidth = 100;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        Button okayButton = new Button(centerX - buttonWidth / 2, centerY + 50, buttonWidth, buttonHeight,
                Component.literal("Okay"), button -> this.onClose());

        this.addRenderableWidget(okayButton);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderTulipBackground(poseStack);

        // Define the font and color
        String title = "Important Notice:";
        int titleColor = 0xFF0000; // Red color
        int centerX = this.width / 2; // Perfectly centered horizontally
        int titleY = this.height / 4 - 20; // Positioned at the top quarter of the screen

        // Push a new PoseStack for scaling
        poseStack.pushPose();
        float scale = 2.0f; // Adjust the scale as needed for larger text
        poseStack.scale(scale, scale, scale);


        // Recalculate center position for scaled text
        int scaledCenterX = (int) (centerX / scale);
        int scaledTitleY = (int) (titleY / scale);

        // Draw the title with the scaled size
        drawCenteredString(poseStack, this.font, title, scaledCenterX, scaledTitleY, titleColor);

        // Pop the PoseStack to reset scaling
        poseStack.popPose();

        // Render the message text
        int messageY = this.height / 2 - 50; // Start higher up for multi-line text
        int lineSpacing = 12; // Space between lines

        drawCenteredString(poseStack, this.font, "This version of Create Bitterballen is a major rewrite.", centerX, messageY, 0xFFFFFF);
        drawCenteredString(poseStack, this.font, "If you have worlds using version 0.86 or below,", centerX, messageY + lineSpacing, 0xFFFFFF);
        drawCenteredString(poseStack, this.font, "it is highly recommended to backup your world.", centerX, messageY + lineSpacing * 2, 0xFFFFFF);
        drawCenteredString(poseStack, this.font, "Loading older worlds with this version may result in", centerX, messageY + lineSpacing * 4, 0xFFFFFF);
        drawCenteredString(poseStack, this.font, "corrupted saves due to item ID changes.", centerX, messageY + lineSpacing * 5, 0xFFFFFF);
        drawCenteredString(poseStack, this.font, "Please proceed with caution.", centerX, messageY + lineSpacing * 6, 0xFFFFFF);

        // Render the button and other widgets
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }
    private void renderTulipBackground(PoseStack poseStack) {
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        int textureWidth = 32;
        int textureHeight = 32;

        for (int x = 0; x < this.width; x += textureWidth) {
            blit(poseStack, x, 0, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
            blit(poseStack, x, this.height - textureHeight, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
        }
        for (int y = 0; y < this.height; y += textureHeight) {
            blit(poseStack, 0, y, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
            blit(poseStack, this.width - textureWidth, y, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
        }

        RenderSystem.setShaderTexture(0, CENTER_TEXTURE);
        int centerStartX = textureWidth;
        int centerStartY = textureHeight;
        int centerEndX = this.width - textureWidth;
        int centerEndY = this.height - textureHeight;

        for (int x = centerStartX; x < centerEndX; x += textureWidth) {
            for (int y = centerStartY; y < centerEndY; y += textureHeight) {
                blit(poseStack, x, y, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
            }
        }
    }


    @Override
    public void onClose() {
        // Return to the parent screen when the GUI is closed
        this.minecraft.setScreen(parentScreen);
    }
}
