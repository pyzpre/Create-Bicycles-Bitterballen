package com.pyzpre.create_bic_bit.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HerringRenderer extends MobRenderer<HerringEntity, HerringModel<HerringEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("create_bic_bit", "textures/entity/herring.png");


    public HerringRenderer(EntityRendererProvider.Context context) {
        super(context, new HerringModel<>(context.bakeLayer(HerringModel.LAYER_LOCATION)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(HerringEntity entity) {
        return TEXTURE;
    }
}
