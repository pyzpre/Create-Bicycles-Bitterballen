package com.pyzpre.create_bic_bit.block.mechanicalfryer;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.pyzpre.create_bic_bit.index.PartialsRegistry;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
public class MechanicalFryerRenderer extends ShaftRenderer<MechanicalFryerEntity> {

    public MechanicalFryerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }
    @Override
    public boolean shouldRenderOffScreen(@NotNull MechanicalFryerEntity fryerEntity) {
        return true;
    }
    @Override
    protected void renderSafe(MechanicalFryerEntity fryerEntity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        RandomSource random = RandomSource.create();

        ItemStack inputStack = fryerEntity.inputInv.getStackInSlot(0);
        ItemStack outputStack = fryerEntity.outputInv.getStackInSlot(0);

        double inputPosX = 0.5, inputPosZ = 0.5; // Center position for input
        double outputPosX = 0.5, outputPosZ = 0.5; // Center position for output
        float inputRotation = 0.0f; // Default rotation for input
        float outputRotation = 0.0f; // Default rotation for output

        // Movement of the fryer head
        float headMovement = fryerEntity.getRenderedHeadOffset(partialTicks);

        // Initial Y positions before applying head movement
        double basePosY = 0.15;

        // Apply head movement and an additional offset to lower the items
        double inputPosY = basePosY - headMovement;
        double outputPosY = basePosY - headMovement;

        // Adjust positions and rotations based on inventory states
        if (!inputStack.isEmpty() && !outputStack.isEmpty()) {
            // If both input and output items are present
            inputPosZ = 0.70; // Adjust input position to the back
            outputPosZ = 0.30; // Adjust output position to the front
            inputRotation = 35.0f; // Rotate input item
            outputRotation = -35.0f; // Rotate output item
        } else if (!inputStack.isEmpty()) {
            // If only the input item is present
            basePosY = -0.05;  // Change basePosY only for input
            inputPosY = basePosY - headMovement; // Adjust input Y based on the lower basePosY
            inputRotation = 90.0f; // Rotate input item to sit upright
        } else if (!outputStack.isEmpty()) {
            // If only the output item is present
            basePosY = -0.05;  // Change basePosY only for output
            outputPosY = basePosY - headMovement; // Adjust output Y based on the lower basePosY
            outputRotation = 90.0f; // Rotate output item to sit upright
        }

        // Render input item with rotation, if present
        if (!inputStack.isEmpty()) {
            renderStack(inputStack, poseStack, bufferSource, light, overlay, itemRenderer, random, inputPosX, inputPosY, inputPosZ, inputRotation);
        }

        // Render output item with rotation, if present
        if (!outputStack.isEmpty()) {
            renderStack(outputStack, poseStack, bufferSource, light, overlay, itemRenderer, random, outputPosX, outputPosY, outputPosZ, outputRotation);
        }
        if (Backend.canUseInstancing(fryerEntity.getLevel())) return;
        super.renderSafe(fryerEntity, partialTicks, poseStack, bufferSource, light, overlay);

        BlockState blockState = fryerEntity.getBlockState();
        VertexConsumer vb = bufferSource.getBuffer(RenderType.solid());

        float renderedHeadOffset = fryerEntity.getRenderedHeadOffset(partialTicks);


        SuperByteBuffer poleRender = CachedBufferer.partialFacing(AllPartialModels.MECHANICAL_MIXER_POLE, blockState,
                blockState.getValue(HORIZONTAL_FACING));
        poleRender.translate(0, -renderedHeadOffset, 0)
                .light(light)
                .renderInto(poseStack, vb);

        // Render the mixer head
        VertexConsumer vbCutout = bufferSource.getBuffer(RenderType.cutoutMipped());
        SuperByteBuffer headRender = CachedBufferer.partialFacing(PartialsRegistry.MECHANICAL_FRYER_HEAD, blockState,
                blockState.getValue(HORIZONTAL_FACING));
        headRender.translate(0, -renderedHeadOffset, 0)
                .light(light)
                .renderInto(poseStack, vbCutout);
    }


    private void renderStack(ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay, ItemRenderer itemRenderer, RandomSource random, double posX, double posY, double posZ, float rotationAngle) {
        poseStack.pushPose();

        final double offsetIncrement = 0.03; // Slightly larger vertical offset increment for visibility
        int renderCount = Math.min(stack.getCount(), 4); // Cap the number of items rendered to 4

        for (int i = 0; i < renderCount; i++) {
            poseStack.pushPose();

            long seed = stack.getCount() * 31L + stack.getItem().hashCode() + i;
            random.setSeed(seed);

            Vec3 position = new Vec3(posX, posY - (i * offsetIncrement), posZ); // Adjust posY based on loop index
            double offsetX = (random.nextDouble() - 0.5) * 0.1; // Width offset
            double offsetY = 0; // No additional height offset needed
            double offsetZ = (random.nextDouble() - 0.5) * 0.1; // Depth offset

            poseStack.translate(position.x + offsetX, position.y + offsetY, position.z + offsetZ);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(rotationAngle));
            float scale = 0.5f; // Maintain the original scale for each item
            poseStack.scale(scale, scale, scale);

            itemRenderer.renderStatic(stack, ItemTransforms.TransformType.FIXED, light, overlay, poseStack, bufferSource, 0);

            poseStack.popPose();
        }


        poseStack.popPose();
    }
}