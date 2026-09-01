package com.example.dynamicfov.mixin;

import com.example.dynamicfov.DynamicFovClient;
import com.example.dynamicfov.config.DynamicFovConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin 
{

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void applyDynamicFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) 
    {
        DynamicFovConfig config = AutoConfig.getConfigHolder(DynamicFovConfig.class).getConfig();
        long currentTime = System.currentTimeMillis();

        // Check if cooldown applies (only if worldLoadCount == 0)
        if (DynamicFovClient.needsFovAnimation) 
        {
            if (DynamicFovClient.worldLoadCount == 0 && 
                DynamicFovClient.worldLoadTime > 0 && 
               (currentTime - DynamicFovClient.worldLoadTime) < config.worldLoadCooldownMs) 
            {
                // First load of launch: hold static offset during cooldown
                float baseFov = cir.getReturnValue();
                cir.setReturnValue(baseFov - config.initialFovOffset);
                return;
            } 
            else 
            {
                // Subsequent loads or completed cooldown: start animation immediately
                DynamicFovClient.animationStartTime = currentTime;
                DynamicFovClient.needsFovAnimation = false;
            }
        }

        // Run smooth FOV transition
        long startTime = DynamicFovClient.animationStartTime;
        if (startTime > 0) 
        {
            long elapsed = currentTime - startTime;

            if (elapsed < config.overallAnimationDurationMs) 
            {
                float baseFov = cir.getReturnValue(); 
                float startFov = baseFov - config.initialFovOffset;

                float t = (float) elapsed / config.overallAnimationDurationMs;
                int order = Math.max(1, Math.min(10, config.easingOrder));
                float easeOut = 1.0f - (float) Math.pow(1.0 - t, order);

                float currentFov = startFov + ((baseFov - startFov) * easeOut);
                cir.setReturnValue(currentFov);
            } 
            else 
            {
                // Increment session counter on animation end
                DynamicFovClient.worldLoadCount++;
                DynamicFovClient.animationStartTime = -1;
            }
        }
    }
}
