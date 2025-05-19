package com.pyzpre.create_bic_bit.events;


import com.pyzpre.create_bic_bit.CreateBitterballen;
import com.pyzpre.create_bic_bit.entity.HerringModel;
import com.pyzpre.create_bic_bit.entity.HerringRenderer;
import com.pyzpre.create_bic_bit.index.EffectRegistry;
import com.pyzpre.create_bic_bit.index.EntityRegistry;
import com.pyzpre.create_bic_bit.util.DeepfriedSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = CreateBitterballen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEventSubscriber {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.HERRING.get(), HerringRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(HerringModel.LAYER_LOCATION, HerringModel::createBodyLayer);
    }
    @Mod.EventBusSubscriber(modid = CreateBitterballen.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public class ClientSetup {

        private static PostChain grayscaleShader;
        private static int lastWidth = -1;
        private static int lastHeight = -1;

        public static void initShader() {
            try {
                Minecraft mc = Minecraft.getInstance();
                ResourceLocation shaderRL = new ResourceLocation(CreateBitterballen.MOD_ID, "shaders/post/deepfried_effect.json");

                grayscaleShader = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), shaderRL);

                if (grayscaleShader == null) {
                    return;
                }

                grayscaleShader.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
                lastWidth = mc.getWindow().getWidth();
                lastHeight = mc.getWindow().getHeight();
            } catch (Exception e) {
                e.printStackTrace();
                grayscaleShader = null;
            }
        }

        @SubscribeEvent
        public static void onRenderLevelStage(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
                return;
            }

            Minecraft mc = Minecraft.getInstance();

            if (mc.player != null) {
                MobEffectInstance effect = mc.player.getEffect(EffectRegistry.OILED_UP.get());

                if (effect != null) {
                    int amplifier = effect.getAmplifier();

                    if (amplifier >= 2) {

                        if (grayscaleShader == null) {
                            initShader();
                        }

                        resizeShaderIfNeeded();

                        if (grayscaleShader != null) {
                            try {
                                grayscaleShader.process(event.getPartialTick());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        return; // Exit here since the shader was processed
                    }
                }

                // If the effect is not present or amplifier is less than 2, clean up the shader
                if (grayscaleShader != null) {
                    grayscaleShader.close();
                    grayscaleShader = null;
                }
            }
        }

        /**
         * Handles window resizing manually by checking the current window size during client ticks.
         */
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return; // Only run at the end of the tick
            }

            resizeShaderIfNeeded();
        }

        /**
         * Resizes the shader if the window dimensions have changed.
         */
        private static void resizeShaderIfNeeded() {
            Minecraft mc = Minecraft.getInstance();

            if (grayscaleShader != null) {
                int currentWidth = mc.getWindow().getWidth();
                int currentHeight = mc.getWindow().getHeight();

                // Check if the resolution has changed
                if (currentWidth != lastWidth || currentHeight != lastHeight) {
                    lastWidth = currentWidth;
                    lastHeight = currentHeight;

                    try {
                        // Resizing the shader to match the window dimensions
                        grayscaleShader.resize(currentWidth, currentHeight);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = CreateBitterballen.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public class SoundModifier {

        @SubscribeEvent
        public static void onPlaySound(PlaySoundEvent event) {
            Minecraft mc = Minecraft.getInstance();

            if (mc.player != null) {
                MobEffectInstance effect = mc.player.getEffect(EffectRegistry.OILED_UP.get());

                if (effect != null && effect.getAmplifier() >= 2) {
                    SoundInstance originalSound = event.getOriginalSound();

                    if (originalSound != null) {
                        ResourceLocation soundLocation = originalSound.getLocation();

                        if (soundLocation.toString().equals("minecraft:entity.minecart.inside") ||
                                soundLocation.toString().equals("minecraft:entity.minecart.riding") ||
                                soundLocation.toString().equals("create:cogs") ||
                                soundLocation.toString().equals("minecraft:entity.minecart.inside.underwater")) {
                            event.setSound(null); // Suppress the sound completely
                            return;
                        }



                        // Apply the deep-fried effect to all other sounds
                        Random random = new Random();

                        float basePitch = 0.5F;
                        float pitchVariation = 0.2F;
                        float baseVolume = 1.2F;
                        float volumeVariation = 0.3F;

                        // Dynamically modify pitch and volume
                        float newPitch = basePitch + (random.nextFloat() * pitchVariation);
                        float newVolume = baseVolume + (random.nextFloat() * volumeVariation);

                        // Create a custom sound instance with modified properties
                        SoundInstance modifiedSound = new DeepfriedSoundInstance(
                                originalSound.getLocation(),          // Sound location
                                originalSound.getSource(),            // Sound category
                                newVolume,                            // Modified volume
                                newPitch,                             // Modified pitch
                                originalSound.isLooping(),            // Preserve looping for other looping sounds
                                originalSound.getDelay(),             // Delay before playing
                                SoundInstance.Attenuation.LINEAR,     // Attenuation type
                                originalSound.getX(),                 // X position
                                originalSound.getY(),                 // Y position
                                originalSound.getZ()                  // Z position
                        );

                        // Resolve the sound before playback
                        modifiedSound.resolve(mc.getSoundManager());

                        // Set the modified sound to play
                        event.setSound(modifiedSound);
                    }
                }
            }
        }
    }
}
