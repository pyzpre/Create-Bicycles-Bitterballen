package createbicyclesbitterballen.block.mechanicalfryer;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.mojang.math.Vector3f;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import createbicyclesbitterballen.index.PartialsRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.util.RandomSource;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MechanicalFryerRenderer extends ShaftRenderer<MechanicalFryerEntity> {

    public MechanicalFryerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }
    @Override
    public boolean shouldRenderOffScreen(MechanicalFryerEntity fryerEntity) {
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
        double basePosY = -0.05;

        // Apply head movement and an additional offset to lower the items
        double inputPosY = basePosY - headMovement; // Adjust -0.1 as needed
        double outputPosY = basePosY - headMovement; // Adjust -0.1 as needed

        // Adjust positions and rotations based on inventory states
        if (!inputStack.isEmpty() && !outputStack.isEmpty()) {
            // If both input and output items are present
            inputPosZ = 0.65; // Adjust input position to the back
            outputPosZ = 0.35; // Adjust output position to the front
            inputRotation = 45.0f; // Rotate input item
            outputRotation = -45.0f; // Rotate output item
        } else if (!inputStack.isEmpty()) {
            // If only the input item is present
            inputRotation = 90.0f; // Rotate input item to sit upright
        } else if (!outputStack.isEmpty()) {
            // If only the output item is present
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


        SuperByteBuffer poleRender = CachedBufferer.partial(AllPartialModels.MECHANICAL_MIXER_POLE, blockState);
        poleRender.translate(0, -renderedHeadOffset, 0)
                .light(light)
                .renderInto(poseStack, vb);

        // Render the mixer head
        VertexConsumer vbCutout = bufferSource.getBuffer(RenderType.cutoutMipped());
        SuperByteBuffer headRender = CachedBufferer.partial(PartialsRegistry.MECHANICAL_FRYER_HEAD, blockState);
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

            long seed = stack.getCount() * 31 + stack.getItem().hashCode() + i;
            random.setSeed(seed);

            Vec3 position = new Vec3(posX, posY - (i * offsetIncrement), posZ); // Adjust posY based on loop index
            double offsetX = (random.nextDouble() - 0.5) * 0.1; // Width offset
            double offsetY = 0; // No additional height offset needed
            double offsetZ = (random.nextDouble() - 0.5) * 0.1; // Depth offset

            poseStack.translate(position.x + offsetX, position.y + offsetY, position.z + offsetZ);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(rotationAngle));

            float scale = 0.5f; // Maintain the original scale for each item
            poseStack.scale(scale, scale, scale);

            itemRenderer.renderStatic(stack, TransformType.FIXED, light, overlay, poseStack, bufferSource, 0);


            poseStack.popPose();
        }


        poseStack.popPose();
    }
}