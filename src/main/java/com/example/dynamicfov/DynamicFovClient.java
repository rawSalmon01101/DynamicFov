package com.example.dynamicfov;

import com.example.dynamicfov.config.DynamicFovConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

public class DynamicFovClient implements ClientModInitializer 
{
    
    public static boolean needsFovAnimation = false;
    public static long worldLoadTime = -1;
    public static long animationStartTime = -1;

    @Override
    public void onInitializeClient() 
    {
        AutoConfig.register(DynamicFovConfig.class, JanksonConfigSerializer::new);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> 
        {
            needsFovAnimation = true;
            worldLoadTime = System.currentTimeMillis();
            animationStartTime = -1; 
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> 
        {
            DynamicFovConfig config = AutoConfig.getConfigHolder(DynamicFovConfig.class).getConfig();
            if (!config.fadeFromBlack) return;

            MinecraftClient client = MinecraftClient.getInstance();
            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();

            long currentTime = System.currentTimeMillis();

            // 1. Draw solid black screen immediately during world load cooldown
            if (needsFovAnimation && worldLoadTime > 0) 
            {
                if ((currentTime - worldLoadTime) < config.worldLoadCooldownMs) 
                {
                    // Full black screen (alpha 255)
                    drawContext.fill(0, 0, width, height, 0xFF000000);
                    return;
                }
            }

            // 2. Render smooth black fade-out during animation
            long startTime = animationStartTime;
            if (startTime > 0) 
            {
                long elapsed = currentTime - startTime;

                if (elapsed < config.overallAnimationDurationMs) 
                {
                    double t = (double) elapsed / config.overallAnimationDurationMs;
                    
                    // Dynamic easing power using config order
                    int order = Math.max(1, Math.min(10, config.easingOrder));
                    double easeOut = 1.0 - Math.pow(1.0 - t, order);

                    float alpha = (float) (1.0 - easeOut);
                    int a = (int) (alpha * 255.0f);
                    
                    if (a > 0) 
                    {
                        int color = (a << 24) | 0x000000; 
                        drawContext.fill(0, 0, width, height, color);
                    }
                }
            }
        });
    }
}
