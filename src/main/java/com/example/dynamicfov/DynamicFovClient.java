package com.example.dynamicfov;

import com.example.dynamicfov.config.DynamicFovConfig;
import com.example.dynamicfov.util.BezierUtil;
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

    // Session counter: tracks world joins since client launch
    public static int worldLoadCount = 0;

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

            long totalDurationMs = config.worldLoadCooldownMs + config.overallAnimationDurationMs;

            // 1. Draw solid black during cooldown ONLY on the first world load of the session
            if (needsFovAnimation && worldLoadTime > 0) 
            {
                if ((currentTime - worldLoadTime) >= totalDurationMs)
                {
                    worldLoadCount += 1;
                    needsFovAnimation = false;
                    animationStartTime = -1;
                }
            }

            // 2. Render smooth black fade-out during animation
            long startTime = animationStartTime;
            if (startTime > 0) 
            {
                long elapsed = currentTime - startTime;

                if (elapsed < config.overallAnimationDurationMs) 
                {
                    float t = (float) elapsed / config.overallAnimationDurationMs;
                    float easeFactor;

                    // Branch between CSS Bézier curve and polynomial order
                    if (config.useBezierCurve) 
                    {
                        easeFactor = BezierUtil.evalBezier(config.bezierCurveValues, t);
                    } 
                    else 
                    {
                        int order = Math.max(1, Math.min(10, config.easingOrder));
                        easeFactor = 1.0f - (float) Math.pow(1.0 - t, order);
                    }

                    float alpha = 1.0f - easeFactor;
                    int a = (int) (alpha * 255.0f);
                    
                    if (a > 0) 
                    {
                        int color = (a << 24) | 0x000000; 
                        drawContext.fill(0, 0, width, height, color);
                    }
                } 
                else 
                {
                    // Increment session counter after completion
                    worldLoadCount++;
                }
            }
        });
    }
}
