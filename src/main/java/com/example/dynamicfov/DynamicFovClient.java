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
    public static long animationStartTime = -1;

    @Override
    public void onInitializeClient() 
    {
        AutoConfig.register(DynamicFovConfig.class, JanksonConfigSerializer::new);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) 
        -> 
        {
            needsFovAnimation = true;
            animationStartTime = -1; 
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) 
        -> 
        {
            long startTime = animationStartTime;
            if (startTime > 0) 
            {
                DynamicFovConfig config = AutoConfig.getConfigHolder(DynamicFovConfig.class).getConfig();
                
                if (!config.fadeFromBlack) return;

                long currentTime = System.currentTimeMillis();
                long elapsed = currentTime - startTime;

                // Updated variable name here
                if (elapsed < config.overallAnimationDurationMs) 
                {
                    double t = (double) elapsed / config.overallAnimationDurationMs;
                    double invT = 1.0 - t;
                    double easeOut = 1.0 - (invT * invT * invT);

                    float alpha = (float) (1.0 - easeOut);
                    int a = (int) (alpha * 255.0f);
                    
                    if (a > 0) 
                    {
                        int color = (a << 24) | 0x000000; 
                        MinecraftClient client = MinecraftClient.getInstance();
                        int width = client.getWindow().getScaledWidth();
                        int height = client.getWindow().getScaledHeight();

                        drawContext.fill(0, 0, width, height, color);
                    }
                }
            }
        });
    }
}
