package com.pyzpre.createbitterballen.events;


import com.pyzpre.createbitterballen.CreateBitterballen;
import com.pyzpre.createbitterballen.entity.HerringModel;
import com.pyzpre.createbitterballen.entity.HerringRenderer;
import com.pyzpre.createbitterballen.index.EffectRegistry;
import com.pyzpre.createbitterballen.index.EntityRegistry;
import com.pyzpre.createbitterballen.util.DeepfriedSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;


import java.util.Random;

@EventBusSubscriber(modid = CreateBitterballen.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEventSubscriber {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.HERRING.get(), HerringRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(HerringModel.LAYER_LOCATION, HerringModel::createBodyLayer);
    }

}
