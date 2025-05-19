package com.pyzpre.create_bic_bit.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HerringModel<T extends Entity> extends EntityModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "herring"), "main");
    private final ModelPart HerringBackBody;
    private final ModelPart HerringBackFins;
    private final ModelPart HerringBodyBack;
    private final ModelPart HerringBody;


    public HerringModel(ModelPart root) {
        this.HerringBackBody = root.getChild("HerringBackBody");
        this.HerringBackFins = root.getChild("HerringBackFins");
        this.HerringBodyBack = root.getChild("HerringBodyBack");
        this.HerringBody = root.getChild("HerringBody");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition HerringBackBody = partdefinition.addOrReplaceChild("HerringBackBody", CubeListBuilder.create().texOffs(20, 10).addBox(-0.5F, -2.0F, 0.0005F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 23.0F, 12.0F));

        PartDefinition HerringBackFins = partdefinition.addOrReplaceChild("HerringBackFins", CubeListBuilder.create().texOffs(22, 24).addBox(0.0F, -1.5F, 1.0015F, 0.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(24, 24).addBox(0.0F, 0.5F, 2.0015F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(26, 15).addBox(0.0F, -2.5F, 2.0015F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 23.0F, 12.0F));

        PartDefinition HerringBodyBack = partdefinition.addOrReplaceChild("HerringBodyBack", CubeListBuilder.create().texOffs(0, 10).addBox(-1.5F, 1.0F, 0.0F, 3.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -2.0F, 0.0F, 3.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(0.0F, -3.0F, 0.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(18, 24).addBox(0.0F, 2.0F, 1.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(24, 12).addBox(0.0F, -4.0F, 1.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 23.0F, 5.0F));

        PartDefinition HerringBody = partdefinition.addOrReplaceChild("HerringBody", CubeListBuilder.create().texOffs(0, 18).addBox(-1.5F, -2.0F, -7.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(18, 18).addBox(-1.5F, 1.0F, -6.0F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(20, 5).addBox(-1.0F, -2.0F, -9.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 23.0F, 6.0F));

        PartDefinition HerringFinLeft_r1 = HerringBody.addOrReplaceChild("HerringFinLeft_r1", CubeListBuilder.create().texOffs(24, 10).addBox(0.0F, 0.0F, -1.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 1.0F, -5.0F, 0.0F, 0.0F, 2.5307F));

        PartDefinition HerringFinRight_r1 = HerringBody.addOrReplaceChild("HerringFinRight_r1", CubeListBuilder.create().texOffs(20, 15).addBox(0.0F, 0.0F, -1.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 1.0F, -5.0F, 0.0F, 0.0F, 0.7854F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(entity instanceof HerringEntity herring)) {
            return;
        }

        // Reset rotations
        this.HerringBody.setRotation(0.0F, 0.0F, 0.0F);
        this.HerringBackBody.setRotation(0.0F, 0.0F, 0.0F);
        this.HerringBackFins.setRotation(0.0F, 0.0F, 0.0F);
        this.HerringBodyBack.setRotation(0.0F, 0.0F, 0.0F);

        float degToRad = (float)Math.PI / 180F;

        if (herring.isSwimming()) {
            // Slow down the animation by dividing ageInTicks
            float animationSpeed = 5.0F; // Adjust this value to control speed (higher = slower)
            float cycle = (ageInTicks / animationSpeed) % 1.0F; // Modulo ensures it loops

            // Interpolate rotation for the main body
            float bodyYRot = interpolateKeyframes(
                    cycle,
                    0.0F, 0.25F, 0.5F, 0.75F, 1.0F,
                    0.0F, -10.0F, 0.0F, 10.0F, 0.0F
            );

            // Back body
            float backBodyYRot = interpolateKeyframes(
                    cycle,
                    0.0F, 0.25F, 0.5F, 0.75F, 1.0F,
                    0.0F, -40.0F, 0.0F, 40.0F, 0.0F
            );

            // Back fins
            float backFinsYRot = interpolateKeyframes(
                    cycle,
                    0.0F, 0.25F, 0.5F, 0.75F, 1.0F,
                    0.0F, -40.0F, 0.0F, 40.0F, 0.0F
            );

            this.HerringBody.yRot = bodyYRot * degToRad;
            this.HerringBackBody.yRot = backBodyYRot * degToRad;
            this.HerringBackFins.yRot = backFinsYRot * degToRad;
        }

        // If not in fluid, lay on its side but still animate
        if (!herring.isInFluidType()) {
            // Lay the fish on its side by adjusting the roll.
            // You can choose xRot or zRot depending on how your model is oriented.
            // For example, if we assume rotating around Z makes it look like it's lying on its side:
            this.HerringBody.zRot = 90.0F * degToRad;
            this.HerringBackBody.zRot = 90.0F * degToRad;
            this.HerringBackFins.zRot = 90.0F * degToRad;
            this.HerringBodyBack.zRot = 90.0F * degToRad;

            // Optionally apply the swimming animation at a slower pace on land
            float landAnimationSpeed = 8.0F; // Slower or different from the in-water speed
            float cycle = (ageInTicks / landAnimationSpeed) % 1.0F;

            // Reuse the same interpolation logic to "wriggle"
            float bodyYRot = interpolateKeyframes(
                    cycle,
                    0.0F, 0.25F, 0.5F, 0.75F, 1.0F,
                    0.0F, -5.0F, 0.0F, 5.0F, 0.0F
            );
            float backBodyYRot = interpolateKeyframes(
                    cycle,
                    0.0F, 0.25F, 0.5F, 0.75F, 1.0F,
                    0.0F, -20.0F, 0.0F, 20.0F, 0.0F
            );
            float backFinsYRot = interpolateKeyframes(
                    cycle,
                    0.0F, 0.25F, 0.5F, 0.75F, 1.0F,
                    0.0F, -20.0F, 0.0F, 20.0F, 0.0F
            );

            // Apply these rotations (these rotations are still around the Y-axis,
            // which might make it look like it’s flopping around)
            this.HerringBody.yRot = bodyYRot * degToRad;
            this.HerringBackBody.yRot = backBodyYRot * degToRad;
            this.HerringBackFins.yRot = backFinsYRot * degToRad;
        }
    }



    // Helper method with logging
    private float interpolateKeyframes(float cycle, float k0, float k1, float k2, float k3, float k4,
                                       float v0, float v1, float v2, float v3, float v4) {
        float value;
        if (cycle < k1) {
            float t = (cycle - k0) / (k1 - k0);
            value = Mth.lerp(t, v0, v1);
        } else if (cycle < k2) {
            float t = (cycle - k1) / (k2 - k1);
            value = Mth.lerp(t, v1, v2);
        } else if (cycle < k3) {
            float t = (cycle - k2) / (k3 - k2);
            value = Mth.lerp(t, v2, v3);
        } else {
            float t = (cycle - k3) / (k4 - k3);
            value = Mth.lerp(t, v3, v4);
        }
        return value;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        HerringBackBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        HerringBackFins.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        HerringBodyBack.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        HerringBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
