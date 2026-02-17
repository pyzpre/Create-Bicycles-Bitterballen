package com.pyzpre.createbitterballen.events;


import com.pyzpre.createbitterballen.CreateBitterballen;
import com.pyzpre.createbitterballen.entity.HerringModel;
import com.pyzpre.createbitterballen.entity.HerringRenderer;
import com.pyzpre.createbitterballen.index.BlockRegistry;
import com.pyzpre.createbitterballen.index.EffectRegistry;
import com.pyzpre.createbitterballen.index.EntityRegistry;
import com.pyzpre.createbitterballen.index.FluidRegistry;
import com.pyzpre.createbitterballen.ponder.BitterOrbPonderPlugin;
import com.pyzpre.createbitterballen.util.DeepfriedSoundInstance;
import io.github.fabricators_of_create.porting_lib.client_events.event.client.PlaySoundCallback;
import net.createmod.ponder.foundation.PonderIndex;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Random;

public class ClientModEventSubscriber implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.CRYSTALLISED_OIL.get(), RenderType.translucent());
        PonderIndex.addPlugin(new BitterOrbPonderPlugin());

        ClientTickEvents.END_CLIENT_TICK.register(ClientSetup::onClientTick);
        PlaySoundCallback.EVENT.register(SoundModifier::onPlaySound);

        EntityRendererRegistry.register(EntityRegistry.HERRING.get(), HerringRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(HerringModel.LAYER_LOCATION, HerringModel::createBodyLayer);
        ScreenEvents.AFTER_INIT.register(TitleScreenHandler::onTitleScreenInit);
    }

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

        public static void onRenderLevelStage(float partialTick) {
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
                                grayscaleShader.process(partialTick);
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
        public static void onClientTick(Minecraft minecraft) {
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

    public class SoundModifier {
        public static SoundInstance onPlaySound(SoundEngine engine, SoundInstance sound, SoundInstance originalSound) {
            Minecraft mc = Minecraft.getInstance();

            if (mc.player != null) {
                MobEffectInstance effect = mc.player.getEffect(EffectRegistry.OILED_UP.get());

                if (effect != null && effect.getAmplifier() >= 2) {
                    if (originalSound != null) {
                        ResourceLocation soundLocation = originalSound.getLocation();

                        if (soundLocation.toString().equals("minecraft:entity.minecart.inside") ||
                                soundLocation.toString().equals("minecraft:entity.minecart.riding") ||
                                soundLocation.toString().equals("create:cogs") ||
                                soundLocation.toString().equals("minecraft:entity.minecart.inside.underwater")) {
                            return null;
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
                        return modifiedSound;
                    }
                }
            }
            return sound;
        }
    }
}
